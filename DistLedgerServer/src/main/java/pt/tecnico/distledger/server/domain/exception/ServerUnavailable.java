package pt.tecnico.distledger.server.domain.exception;

public class ServerUnavailable extends Exception {
	public ServerUnavailable(String message) {
		super(message);
	}
}