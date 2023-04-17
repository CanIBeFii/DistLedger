package pt.tecnico.distledger.server.domain.exception;

public class AccountDuplicate extends Exception{
	public AccountDuplicate(String message) {
		super(message);
	}
}
