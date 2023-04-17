package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.exception.ServerAlreadyActive;
import pt.tecnico.distledger.server.domain.exception.ServerAlreadyDeactivated;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationOrBuilder;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.*;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;

import static io.grpc.Status.INVALID_ARGUMENT;

import com.google.rpc.context.AttributeContext.ResponseOrBuilder;

import io.grpc.stub.StreamObserver;
import java.util.*;
// import java.util.List;

public class AdminServiceImpl extends AdminServiceGrpc.AdminServiceImplBase {

	private ServerState server;
	private static boolean DEBUG_FLAG;

	public AdminServiceImpl(ServerState server, boolean DEBUG_FLAG) {
		this.server = server;
		AdminServiceImpl.DEBUG_FLAG = DEBUG_FLAG;
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	@Override
	public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
		ActivateResponse response = ActivateResponse.newBuilder().build();

		debug("Activating server");
		
		try {
			server.activateServer();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (ServerAlreadyActive e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is already active").asRuntimeException());
		}
	}

	@Override
	public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
		DeactivateResponse response = DeactivateResponse.newBuilder().build();

		debug("Deactivating server");
		
		try {
			server.deactivateServer();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (ServerAlreadyDeactivated e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is already deactivated").asRuntimeException());
		}
	}

	@Override
	public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
		List<Operation> operations = server.getLedger();
		List<DistLedgerCommonDefinitions.Operation> ret_operations = new ArrayList<>();

		debug("Getting LedgerState");
		
		for(Operation op: operations) {
			ret_operations.add(op.transformOp());
		}
		LedgerState ledger = LedgerState.newBuilder().addAllLedger(ret_operations).build();
		getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledger).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
		GossipResponse response = GossipResponse.newBuilder().build();

		debug("Gossiping ledger");
		server.gossip();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
