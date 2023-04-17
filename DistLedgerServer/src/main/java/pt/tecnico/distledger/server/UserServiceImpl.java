package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;	
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.VectorClock;
import pt.tecnico.distledger.server.domain.exception.AccountDoesntExist;
import pt.tecnico.distledger.server.domain.exception.AccountDuplicate;
import pt.tecnico.distledger.server.domain.exception.AccountHasBalance;
import pt.tecnico.distledger.server.domain.exception.IllegalOperation;
import pt.tecnico.distledger.server.domain.exception.InsufficientBalance;
import pt.tecnico.distledger.server.domain.exception.InvalidAmount;
import pt.tecnico.distledger.server.domain.exception.ReadNotPossible;
import pt.tecnico.distledger.server.domain.exception.SameAccount;
import pt.tecnico.distledger.server.domain.exception.ServerUnavailable;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import static io.grpc.Status.INVALID_ARGUMENT;

import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
	
	private ServerState server;
	private static boolean DEBUG_FLAG;

	public UserServiceImpl(ServerState server, boolean DEBUG_FLAG) {
		this.server = server;
		UserServiceImpl.DEBUG_FLAG = DEBUG_FLAG;
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	@Override
	public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
		CreateAccountResponse response;
		VectorClock prev = new VectorClock(request.getPrevTSList());
		VectorClock opTS = new VectorClock(request.getPrevTSList());
		int index = server.getIndexServer();
		boolean isStable;
		
		debug("Creating Account");
		try {
			server.updateReplicaTS();
			opTS.updateIndex(index, server.getReplicaTS().getClock().get(index));
			response = CreateAccountResponse.newBuilder()
				.addAllTS(opTS.getClock())
				.build();
			responseObserver.onNext(response);
			isStable = server.getValueTS().isGreater(prev);
			server.addOperation(new CreateOp(request.getUserId(), isStable, prev, opTS));
			if (isStable == true) {
				server.addAccount(request.getUserId());
				VectorClock newValueTS = server.getValueTS();
				newValueTS.mergeClock(opTS.getClock());
				server.setValueTS(newValueTS);
			}
			responseObserver.onCompleted();
		} catch (ServerUnavailable e) {
			System.out.println("Server is not active");
		} catch (AccountDuplicate e) {
			System.out.println("Account already exists");
		}
	}

	@Override
	public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
		debug("Deleting Account");
		
		DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();
		
		try {
			server.deleteAccount(request.getUserId());
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (ServerUnavailable e) {
			System.out.println("Server is not active");
		} catch (IllegalOperation e) {
			System.out.println("Secondary servers cannot delete account");
		} catch (AccountDoesntExist e) {
			System.out.println("Account doesn't exist");
		} catch (AccountHasBalance e) {
			System.out.println("Account has balance");
		}

	}

	@Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		VectorClock prev = new VectorClock(request.getPrevTSList());
		int index = server.getIndexServer();
		int balance;
		
		debug("Getting Balance");
		try {
			balance = server.getBalanceAccount(request.getUserId(), prev);
			prev.updateIndex(index, server.getValueTS().getClock().get(index));;
			BalanceResponse response =  BalanceResponse.newBuilder()
				.setValue(balance)
				.addAllValueTS(prev.getClock())
				.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (ServerUnavailable e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is not active").asRuntimeException());
		} catch (AccountDoesntExist e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Account doesn't exist").asRuntimeException());
		} catch (ReadNotPossible e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Read not possible").asRuntimeException());
		}
	}

	@Override
	public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
		TransferToResponse response;
		VectorClock prev = new VectorClock(request.getPrevTSList());
		VectorClock opTS = new VectorClock(request.getPrevTSList());
		String from = request.getAccountFrom();
		String to = request.getAccountTo();
		int amount = request.getAmount();
		int index = server.getIndexServer();
		boolean isStable;

		debug("Transfering From" + from + "To" + to);
		try {
			server.updateReplicaTS();
			opTS.updateIndex(index, server.getReplicaTS().getClock().get(index));
			response = TransferToResponse.newBuilder()
				.addAllTS(opTS.getClock())
				.build();
			responseObserver.onNext(response);
			isStable = server.getValueTS().isGreater(prev);
			server.addOperation(new TransferOp(from, to, amount, isStable, prev, opTS));
			if (isStable == true) {
				server.transferTo(from, to, amount);
				VectorClock newValueTS = server.getValueTS();
				newValueTS.mergeClock(opTS.getClock());
				server.setValueTS(newValueTS);
			}
			responseObserver.onCompleted();
		} catch (ServerUnavailable e) {
			System.out.println("Server is not active");
		} catch (AccountDoesntExist e) {
			System.out.println("Account doesn't exist");
		} catch (SameAccount e) {
			System.out.println("Same account");
		} catch (InsufficientBalance e) {
			System.out.println("Insufficient balance");
		} catch (InvalidAmount e) {
			System.out.println("Invalid amount: negative or zero");
		}
	}
}
