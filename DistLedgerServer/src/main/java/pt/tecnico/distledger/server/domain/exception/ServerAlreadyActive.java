package pt.tecnico.distledger.server.domain.exception;

public class ServerAlreadyActive extends Exception{
	public ServerAlreadyActive(String message) {
		super(message);
	}
}
