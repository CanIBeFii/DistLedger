package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceBlockingStub;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceBlockingStub;
import pt.tecnico.distledger.server.domain.VectorClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class UserService {

	private HashMap<String, ManagedChannel> channels = new HashMap<String, ManagedChannel>();
	private HashMap<String, UserServiceBlockingStub> stubs = new HashMap<String, UserServiceBlockingStub>();

	private ManagedChannel namingChannel;
	private NamingServerServiceBlockingStub namingStub;

	private VectorClock prev;

	private static boolean DEBUG_FLAG;
	
	public UserService() {
	}

	public UserService(boolean DEBUG_FLAG) {
		List<String> servers;
		ManagedChannel channel;
		UserServiceBlockingStub stub;
		
		UserService.DEBUG_FLAG = DEBUG_FLAG;
		namingChannel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();
		namingStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);

		servers = lookupServer("A");
		channel = ManagedChannelBuilder.forTarget("localhost:" + servers.get(0)).usePlaintext().build();
		stub = UserServiceGrpc.newBlockingStub(channel);
		channels.put("A", channel);
		stubs.put("A", stub);
		
		servers = lookupServer("B");
		channel = ManagedChannelBuilder.forTarget("localhost:" + servers.get(0)).usePlaintext().build();
		stub = UserServiceGrpc.newBlockingStub(channel);
		channels.put("B", channel);
		stubs.put("B", stub);

		this.prev = new VectorClock(2);
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.out.println(message);
	}

	public void shutdownChannel() {
		for (ManagedChannel channel : channels.values()) {
			if (channel != null)
				channel.shutdown();
		}
		if (namingChannel != null)
			namingChannel.shutdown();
	}

	public String createAccount(String userId, String server) {
		List<Integer> newTS;

		CreateAccountResponse response = stubs.get(server)
			.createAccount(CreateAccountRequest.newBuilder()
			.setUserId(userId)
			.addAllPrevTS(prev.getClock())
			.build());
		newTS = response.getTSList();
		prev.setClock(newTS);
		return "OK";
	}

	public String deleteAccount(String userId, String server) {
		stubs.get(server).deleteAccount(DeleteAccountRequest.newBuilder()
				.setUserId(userId)
				.build());
		return "OK";
	}

	public String balance(String userId, String server) {
		int balance;
		List<Integer> newTS;
		BalanceResponse response = stubs.get(server)
				.balance(BalanceRequest.newBuilder()
				.setUserId(userId)
				.addAllPrevTS(prev.getClock())
				.build());
		balance = response.getValue();
		newTS = response.getValueTSList();
		prev.setClock(newTS);
		return "OK\n" + balance;
	}

	public String transferTo(String fromUserId, String toUserId, int amount, String server) {
		List<Integer> newTS;

		TransferToResponse response = stubs.get(server).transferTo(TransferToRequest.newBuilder()
				.setAccountFrom(fromUserId)
				.setAccountTo(toUserId)
				.setAmount(amount)
				.addAllPrevTS(prev.getClock())
				.build());
		newTS = response.getTSList();
		prev.setClock(newTS);
		return "OK";
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
}
	