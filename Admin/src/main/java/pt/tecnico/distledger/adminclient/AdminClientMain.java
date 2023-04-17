package pt.tecnico.distledger.adminclient;

public class AdminClientMain {
    
    private static boolean DEBUG_FLAG;

    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    public static void main(String[] args) {

        // check arguments
        if (args.length > 1) {
            System.err.println("Too many Arguments!");
            System.err.println("Usage: mvn exec:java -debug");
            return;
        }

		if (args.length == 1) {
			if (args[0].equals("debug"))
				DEBUG_FLAG = true;
			else
				DEBUG_FLAG = false;
		}
		
        debug(AdminClientMain.class.getSimpleName());

        CommandParser parser = new CommandParser(DEBUG_FLAG);
        parser.parseInput();
    }
}
