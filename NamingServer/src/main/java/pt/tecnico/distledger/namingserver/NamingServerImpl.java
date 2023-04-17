package pt.tecnico.distledger.namingserver;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;

import pt.tecnico.distledger.namingserver.domain.exception.ServiceEntryDoesntExist;
import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDuplicate;
import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDoesntExist;

import pt.tecnico.distledger.namingserver.domain.ServerEntry;
import pt.tecnico.distledger.namingserver.domain.NamingServerState;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;

import io.grpc.stub.StreamObserver;

import static io.grpc.Status.INVALID_ARGUMENT;

public class NamingServerImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase{
	
	private NamingServerState state;

	private static boolean DEBUG_FLAG;

	public NamingServerImpl(NamingServerState state, boolean DEBUG_FLAG) {
		NamingServerImpl.DEBUG_FLAG = DEBUG_FLAG;
		this.state = state;
	}

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	@Override
	public void registerServer(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
		RegisterResponse response = RegisterResponse.newBuilder().build();
		debug("Registering server: " + request.getService() + " " + request.getQualifier());
		try {
			state.register(request.getService(), new ServerEntry(request.getAddress(), request.getQualifier()));
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (ServerEntryDuplicate e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Server is already registered").asRuntimeException());
		}
	}

	@Override
	public void deleteServer(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {
		DeleteResponse response = DeleteResponse.newBuilder().build();
		debug("Deleting server");
		try {
			state.delete(request.getService(), request.getAddress());
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (ServerEntryDoesntExist e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ServerEntry doesn't exist").asRuntimeException());
		}
		catch (ServiceEntryDoesntExist e) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("ServiceEntry doesn't exist").asRuntimeException());
		}
	}

	@Override
	public void lookupServer(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
		debug("Looking up servers");
		LookupResponse response = LookupResponse.newBuilder()
			.addAllServers(state.lookup(request.getService(), request.getQualifier()))
			.build();
		debug("Servers lookup done!");
		responseObserver.onNext(response);
		responseObserver.onCompleted();
		debug("Lookup response given!");
	}
}
