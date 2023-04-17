package pt.tecnico.distledger.server.domain.exception;

public class SameAccount extends Exception {
	public SameAccount(String message) {
		super(message);
	}
}
