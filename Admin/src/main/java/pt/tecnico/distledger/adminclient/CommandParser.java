package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;

import java.util.Scanner;

import io.grpc.StatusRuntimeException;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private AdminService adminService;

	private static boolean DEBUG_FLAG;

    public CommandParser(boolean DEBUG_FLAG) {
		CommandParser.DEBUG_FLAG = DEBUG_FLAG;
		adminService = new AdminService(DEBUG_FLAG);
    }

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.out.println(message);
	}

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];
			debug("line read!");

            switch (cmd) {
                case ACTIVATE:

                    this.activate(line);
                    break;

                case DEACTIVATE:
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    this.dump(line);
                    break;

                case GOSSIP:
                    this.gossip(line);
                    break;

                case HELP:
                    this.printUsage();
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    break;
            }

        }
		scanner.close();
		adminService.shutdownChannel();
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

		try {
			adminService.activateServer(server);
			System.out.println("OK");
		}
		catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
    }

    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

		try {
			debug("Asking service to deactivate server ");
			adminService.deactivateServer(server);
			System.out.println("OK");
		}
		catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
    }

	private void dump(String line){
		String[] split = line.split(SPACE);

		if (split.length != 2){
			this.printUsage();
			return;
		}
		String server = split[1];

		try {
			String response = "ledgerState {\n" + adminService.getLedgerState(server);
            //to showcase correct indentation when printing
			response = response.replace("\n", "\n\t");
			response = response.substring(0, response.length() - 1) + "}";
			System.out.println("OK\n" + response);
		}
		catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
	}

    @SuppressWarnings("unused")
    private void gossip(String line){
        String[] split = line.split(SPACE);

		if (split.length != 2){
			this.printUsage();
			return;
		}
		String server = split[1];

		try {
			debug("Asking " + server +" to gossip");
			adminService.gossip(server);
			System.out.println("OK\n");
		} catch (StatusRuntimeException e) {
			System.out.println(e.getStatus().getDescription());
		}
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}
