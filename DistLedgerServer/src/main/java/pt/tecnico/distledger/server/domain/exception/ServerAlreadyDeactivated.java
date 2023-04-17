package pt.tecnico.distledger.server.domain.exception;

public class ServerAlreadyDeactivated extends Exception{
	public ServerAlreadyDeactivated(String message) {
		super(message);
	}
}
