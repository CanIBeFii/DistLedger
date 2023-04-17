package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class Operation {
	private String account;
	private boolean isStable;
	private VectorClock prev;
	private VectorClock opTS;

	public Operation(String fromAccount, boolean stable, VectorClock prev, VectorClock opTS) {
		this.account = fromAccount;
		this.isStable = stable;
		this.prev = prev;
		this.opTS = opTS;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getDestAccount() {
		return null;
	}

	public int getAmount() {
		return 0;
	}

	public boolean isStable() {
		return isStable;
	}

	public void setStable(boolean stable) {
		isStable = stable;
	}

	public VectorClock getPrev() {
		return this.prev;
	}

	public void setPrev(VectorClock prev) {
		this.prev = prev;
	}

	public VectorClock getOpTS() {
		return opTS;
	}

	public void setReplicaTS(VectorClock opTS) {
		this.opTS = opTS;
	}

	public DistLedgerCommonDefinitions.Operation transformOp(){
		return DistLedgerCommonDefinitions.Operation.getDefaultInstance();
	}
}
