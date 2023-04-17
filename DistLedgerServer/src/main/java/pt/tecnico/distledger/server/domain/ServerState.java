package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.exception.AccountDoesntExist;
import pt.tecnico.distledger.server.domain.exception.AccountDuplicate;
import pt.tecnico.distledger.server.domain.exception.AccountHasBalance;
import pt.tecnico.distledger.server.domain.exception.InsufficientBalance;
import pt.tecnico.distledger.server.domain.exception.InvalidAmount;
import pt.tecnico.distledger.server.domain.exception.ReadNotPossible;
import pt.tecnico.distledger.server.domain.exception.SameAccount;
import pt.tecnico.distledger.server.domain.exception.ServerAlreadyActive;
import pt.tecnico.distledger.server.domain.exception.ServerAlreadyDeactivated;
import pt.tecnico.distledger.server.domain.exception.ServerUnavailable;
import pt.tecnico.distledger.server.domain.exception.IllegalOperation;

import pt.tecnico.distledger.server.CrossServerServiceImpl;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerStateOrBuilder;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState.Builder;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerState {

	private String qualifier;

	private List<String> qualifiersList;

	private List<Operation> ledger;

	private HashMap<String, Integer> users;

	private VectorClock valueTS;

	private VectorClock replicaTS;

	private ManagedChannel namingChannel;

	private NamingServerServiceBlockingStub namingStub;

	private HashMap<String, ManagedChannel> channels;

	private HashMap<String, DistLedgerCrossServerServiceBlockingStub> stubs;

	private boolean isActive;

	private static boolean DEBUG_FLAG;

	public ServerState(String qualifier, boolean DEBUG_FLAG) {
		ServerState.DEBUG_FLAG = DEBUG_FLAG;
		
		this.qualifiersList = new ArrayList<>();
		this.qualifiersList.add("A");
		this.qualifiersList.add("B");

		this.qualifier = qualifier;
		
		this.ledger = new ArrayList<>();
		this.users = new HashMap<>();
		
		this.valueTS = new VectorClock(this.qualifiersList.size());
		this.replicaTS = new VectorClock(this.qualifiersList.size());

		this.isActive = true;
		users.put("broker", 1000);

		this.namingChannel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();
		this.namingStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);

		channels = new HashMap<String, ManagedChannel>();
		stubs = new HashMap<String, DistLedgerCrossServerServiceBlockingStub>();
	}

	private void initializeServersLookup() {
		List<String> servers;
		ManagedChannel channel;
		DistLedgerCrossServerServiceBlockingStub stub;

		this.namingChannel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();
		this.namingStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);

		for (int i = 0; i < qualifiersList.size(); i += 1) {
			String server = qualifiersList.get(i);
			if (server.equals(qualifier) == false) {
				servers = lookupServer(server);
				if (servers.size() != 0) {
					channel = ManagedChannelBuilder.forTarget("localhost:" + servers.get(0))
						.usePlaintext()
						.build();
					stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
					channels.put(server, channel);
					stubs.put(server, stub);
				}
			}
		}
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	public void shutdown_server() {
		namingChannel.shutdown();
		for (ManagedChannel channel : channels.values()) {
			channel.shutdown();
		}
	}
	
	private boolean getIsActive() {
		return isActive;
	}
	
	public synchronized String getQualifier() {
		return qualifier;
	}

	public synchronized boolean isServerActive() throws ServerUnavailable{
		if (getIsActive() == false)
			throw new ServerUnavailable("Server is not active");
		return true;
	}

	public synchronized void activateServer() throws ServerAlreadyActive {
		debug("Activating server");
		if (this.isActive == true)
			throw new ServerAlreadyActive("Server is already active");
		this.isActive = true;
	}

	public synchronized void deactivateServer() throws ServerAlreadyDeactivated {
		debug("Deactivating server in serverState");
		if (this.isActive == false)
			throw new ServerAlreadyDeactivated("Server is already deactivated");
		this.isActive = false;
	}

	public synchronized List<Operation> getLedger() {
		debug("Getting Ledger");
		return new ArrayList<>(ledger);
	}

	public synchronized VectorClock getValueTS() {
		debug("Getting ValueTS");
		return new VectorClock(valueTS.getClock());
	}

	public synchronized VectorClock getReplicaTS() {
		debug("Getting ReplicaTS");
		return new VectorClock(replicaTS.getClock());
	}

	public synchronized void setReplicaTS(VectorClock newTS) {
		this.replicaTS = newTS;
	}

	public synchronized void setValueTS(VectorClock newValueTS) {
		this.valueTS = newValueTS;
	}

	public synchronized void updateReplicaTS() {
		this.replicaTS.incrementClock(this.qualifiersList.indexOf(this.qualifier));
	}

	public synchronized void setupdateValueTS(List<Integer> newTS) {
		this.valueTS.setClock(newTS);
	}

	public boolean addOperation(Operation operation) {
		debug("Adding Operation");
		return ledger.add(operation);
	}

	public synchronized HashMap<String, Integer> getUsers() {
		debug("Getting Users");
		return new HashMap<>(users);
	}

	private void doAddAccount(String Id) {
		debug("DoAddAccount:" + Id);
		users.put(Id, 0);
	}

	private void doDeleteAccount(String Id, boolean stable) {
		debug("DoDeleteAccount:" + Id);
		users.remove(Id);
	}

	private void doTransfer(String from, String to, int amount) {
		debug("DoTransfer of " + amount + " from " + from + " to " + to);
		int from_balance = users.get(from);
		int to_balance = users.get(to);
		users.put(from, from_balance - amount);
		users.put(to, to_balance + amount);
	}

	public synchronized void addAccount(String Id) throws ServerUnavailable, AccountDuplicate{
		debug("Adding Account:" + Id);
		if (this.isActive == false)
			throw new ServerUnavailable("Server is not active");
		if (users.get(Id) != null)
			throw new AccountDuplicate("Account already exists");
		doAddAccount(Id);
	}

	public synchronized void deleteAccount(String Id) throws ServerUnavailable, IllegalOperation, AccountDoesntExist, AccountHasBalance {
		boolean stable = true;
		
		debug("Deleting Account:" + Id);
		if (this.isActive == false)
			throw new ServerUnavailable("Server is not active");
		if (users.get(Id) == null)
			throw new AccountDoesntExist(Id + " doesn't exist");
		if (users.get(Id) != 0)
			throw new AccountHasBalance(Id + " has balance: " + users.get(Id));
		doDeleteAccount(Id, stable);
	}

	public synchronized int getBalanceAccount(String Id, VectorClock prev) throws ServerUnavailable, ReadNotPossible, AccountDoesntExist {
		debug("Getting Account Balance");
		if (this.isActive == false)
			throw new ServerUnavailable("Server is not active");
		if (valueTS.isGreater(prev) == false)
			throw new ReadNotPossible("Read not possible");
		if (users.get(Id) == null)
			throw new AccountDoesntExist(Id + " doesn't exist");
		return users.get(Id);
	}

	public synchronized void transferTo(String from, String to, int amount) throws ServerUnavailable, 
		AccountDoesntExist, InsufficientBalance, SameAccount, InvalidAmount {
		
		debug("Transfering From" + from + "To" + to);
		if (this.isActive == false)
			throw new ServerUnavailable("Server is not active");
		if (from.compareTo(to) == 0)
			throw new SameAccount("Transfering to the same account");
		if (users.get(from) == null)
			throw new AccountDoesntExist(from + " doesn't exist");
		if (users.get(to) == null)
			throw new AccountDoesntExist(to + " doesn't exist");
		if (users.get(from) < amount)
			throw new InsufficientBalance("Insufficient balance");
		if (amount <= 0)
			throw new InvalidAmount("Invalid amount: negative or zero");
		doTransfer(from, to, amount);
	}

	public synchronized int getIndexServer() {
		return this.qualifiersList.indexOf(this.qualifier);
	}

	public synchronized void gossip() {
		List<DistLedgerCommonDefinitions.Operation> transformedLedger = new ArrayList<>();
		
		initializeServersLookup();
		for (Operation op : ledger) {
			transformedLedger.add(op.transformOp());
		}
		
		for (DistLedgerCrossServerServiceBlockingStub stub : stubs.values()) {
			LedgerState newState = LedgerState.newBuilder()
				.addAllLedger(transformedLedger)
				.build();
			PropagateStateRequest request = PropagateStateRequest.newBuilder()
				.setState(newState)
				.addAllReplicaTS(this.getReplicaTS().getClock())
				.build();
			stub.propagateState(request);
		}

		shutdown_server();
	}

	public synchronized void propagateState(LedgerState state, List<Integer> replicaTS) throws ServerUnavailable, AccountDuplicate,
		SameAccount, AccountDoesntExist, InsufficientBalance, InvalidAmount {
		debug("Propagating State");

		if (this.isActive == false)
			throw new ServerUnavailable("Server is not active");

		VectorClock newReplicaTS = new VectorClock(replicaTS);
		for (DistLedgerCommonDefinitions.Operation op : state.getLedgerList()) {
			VectorClock newTs = new VectorClock(op.getTSList());
			VectorClock prev = new VectorClock(op.getPrevTSList());
			boolean isStable;
			if (!(newTs.isSmallerOrEqual(this.replicaTS) == true)) {
				isStable = prev.isSmallerOrEqual(this.valueTS);
				Operation newOp = translateOperation(op, isStable);
				this.addOperation(newOp);
				if (isStable == true) {
					this.doOperation(newOp);
					VectorClock newValueTS = this.getValueTS();
					newValueTS.mergeClock(newTs.getClock());
					this.setValueTS(newValueTS);
				}
			}
		}

		VectorClock updatedReplicaTs = this.getReplicaTS();
		updatedReplicaTs.mergeClock(newReplicaTS.getClock());
		this.setReplicaTS(updatedReplicaTs);
		
		for (Operation op : ledger) {
			if (op.isStable() == false && op.getPrev().isSmallerOrEqual(this.valueTS)) {
				op.setStable(true);
				this.doOperation(op);
				VectorClock newValueTS = this.getValueTS();
				newValueTS.mergeClock(op.getOpTS().getClock());
				this.setValueTS(newValueTS);
			}
		}
	}

	private Operation translateOperation(DistLedgerCommonDefinitions.Operation op, boolean isStable) {
		if (op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT) {
			return new CreateOp(op.getUserId(),
					isStable,
					new VectorClock(op.getPrevTSList()),
					new VectorClock(op.getTSList()));
		} else if (op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO) {
			return new TransferOp(op.getUserId(),
					op.getDestUserId(),
					op.getAmount(),
					isStable,
					new VectorClock(op.getPrevTSList()),
					new VectorClock(op.getTSList()));
		} else {
			return null;
		}
	}

	private void doOperation(Operation op) throws ServerUnavailable, AccountDuplicate,
				SameAccount, AccountDoesntExist, InsufficientBalance, InvalidAmount{
		if (op instanceof CreateOp) {
			this.addAccount(op.getAccount());
		} else if (op instanceof TransferOp) {
			this.transferTo(op.getAccount(), op.getDestAccount(), op.getAmount());
		}
	}

	private List<String> lookupServer(String server) {
		debug("Looking up server " + server + " Address");
		LookupResponse response = namingStub.lookupServer(LookupRequest.newBuilder()
				.setQualifier(server)
				.setService("DistLedger")
				.build());
		debug("Lookup response received");
		return response.getServersList();
	}

	@Override
	public String toString() {
		debug("Turning into String\n");
		String save = "ledgerState {";

		for (Operation operation : ledger) {
			save += "\n\t" + operation.toString();
		}
		save += "\n}";
		return save;
	}
}
