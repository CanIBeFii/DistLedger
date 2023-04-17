package pt.tecnico.distledger.server.domain.exception;

public class AccountHasBalance extends Exception{
	public AccountHasBalance(String message) {
		super(message);
	}
}
