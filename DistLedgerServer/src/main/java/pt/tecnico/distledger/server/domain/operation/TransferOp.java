package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount, boolean stable, VectorClock prev, VectorClock opTS) {
        super(fromAccount, stable, prev, opTS);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    @Override
    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

	@Override
	public DistLedgerCommonDefinitions.Operation transformOp() {
		return DistLedgerCommonDefinitions.Operation
			.newBuilder()
			.setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
			.setUserId(this.getAccount())
			.setDestUserId(this.getDestAccount())
			.setAmount(this.getAmount())
			.addAllPrevTS(this.getPrev().getClock())
			.addAllTS(this.getOpTS().getClock())
			.build();
	}

    @Override
    public String toString() {
	return "ledger {\ntype: OP_TRANSFER_TO\nuserId: \""
		+ getAccount() + "\"\ndestUserId: \"" + getDestAccount()
		+"\"\namount: " + getAmount() + "\n}";
    }
}
