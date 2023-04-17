package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceBlockingStub;
import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.server.domain.ServerState;

public class ServerMain {

	private static boolean DEBUG_FLAG;

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}
    public static void main(String[] args) throws IOException, InterruptedException {
		// check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: mvn exec:java -Dexec.args=<port> <server>");
			return ;
		}

		if (args.length == 3) {
			if (args[2].equals("debug")) {
				DEBUG_FLAG = true;
			}
			else
				DEBUG_FLAG = false;
		}

		debug(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			debug(String.format("arg[%d] = %s", i, args[i]));
		}
		final int port = Integer.parseInt(args[0]);
		final String serverQualifier = args[1];

		debug("Creating channel and stub to NamingServer");
		ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();

		NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

		debug("Registing server to NamingServer");
		stub.registerServer(RegisterRequest.newBuilder()
			.setAddress(Integer.toString(port))
			.setService("DistLedger")
			.setQualifier(serverQualifier)
			.build());
		
		final ServerState serverState = new ServerState(serverQualifier, DEBUG_FLAG);
		final BindableService impl = new UserServiceImpl(serverState, DEBUG_FLAG);
		final BindableService impl2 = new AdminServiceImpl(serverState, DEBUG_FLAG);
		final BindableService impl3 = new CrossServerServiceImpl(serverState, DEBUG_FLAG);

		debug("\nCreating server");
		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port)
			.addService(impl)
			.addService(impl2)
			.addService(impl3)
			.build();

		
		debug("Server started");
		// Start the server
		server.start();

		debug("Server awaiting termination\n");
		// Do not exit the main thread. Wait until server is terminated.
		System.out.println("Press ENTER to leave:");
		System.in.read();

		stub.deleteServer(DeleteRequest.newBuilder()
			.setService("DistLedger")
			.setAddress(Integer.toString(port))
			.build());
		channel.shutdown();
		server.shutdown();
    }
}
