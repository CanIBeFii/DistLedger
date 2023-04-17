package pt.tecnico.distledger.server;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;

import pt.tecnico.distledger.server.domain.ServerState;

import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.tecnico.distledger.server.domain.exception.ServerUnavailable;
import pt.tecnico.distledger.server.domain.exception.IllegalOperation;
import pt.tecnico.distledger.server.domain.exception.AccountDuplicate;
import pt.tecnico.distledger.server.domain.exception.AccountDoesntExist;
import pt.tecnico.distledger.server.domain.exception.AccountHasBalance;
import pt.tecnico.distledger.server.domain.exception.InsufficientBalance;
import pt.tecnico.distledger.server.domain.exception.SameAccount;
import pt.tecnico.distledger.server.domain.exception.InvalidAmount;

import static io.grpc.Status.INVALID_ARGUMENT;

public class CrossServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
	
	private static boolean DEBUG_FLAG;

	private ServerState server;

	public CrossServerServiceImpl(ServerState server, boolean DEBUG_FLAG) {
		CrossServerServiceImpl.DEBUG_FLAG = DEBUG_FLAG;
		this.server = server;
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	@Override
	public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
		PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
		debug("Propagating state");

		try {
			server.propagateState(request.getState(), request.getReplicaTSList());
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (ServerUnavailable e) {
			System.out.println("Server unavailable");
		} catch (AccountDuplicate e) {
			System.out.println("Account already exists");
		} catch (AccountDoesntExist e) {
			System.out.println("Account doesn't exist");
		} catch (InsufficientBalance e) {
			System.out.println("Insufficient balance");
		} catch (SameAccount e) {
			System.out.println("Same account");
		} catch (InvalidAmount e) {
			System.out.println("Invalid amount");
		}
	}
}
