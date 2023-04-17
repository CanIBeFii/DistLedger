package pt.tecnico.distledger.server.domain.exception;

public class InsufficientBalance extends Exception{
	public InsufficientBalance(String message) {
		super(message);
	}
}
