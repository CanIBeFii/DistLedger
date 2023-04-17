package pt.tecnico.distledger.namingserver;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.tecnico.distledger.namingserver.domain.NamingServerState;

import java.io.IOException;

public class NamingServer {

	private static boolean DEBUG_FLAG;

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

    public static void main(String[] args) throws IOException, InterruptedException {
		// check arguments
		if (args.length > 2) {
			System.err.println("Too many arguments");
			System.err.printf("Usage: mvn exec:java");
			return;
		}

		if (args.length == 2) {
			if (args[1].equals("debug"))
				DEBUG_FLAG = true;
			else
				DEBUG_FLAG = false;
		}
		debug(NamingServer.class.getSimpleName());

		final int port = Integer.parseInt(args[0]);

		final NamingServerState namingServerState = new NamingServerState(DEBUG_FLAG);
		final BindableService impl = new NamingServerImpl(namingServerState, DEBUG_FLAG);

		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// Start the server
		server.start();
		debug("Server started");
		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }
}
