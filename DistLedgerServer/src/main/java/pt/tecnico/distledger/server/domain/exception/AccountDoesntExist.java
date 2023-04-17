package pt.tecnico.distledger.server.domain.exception;

public class AccountDoesntExist extends Exception{
	public AccountDoesntExist(String message) {
		super(message);
	}
}
