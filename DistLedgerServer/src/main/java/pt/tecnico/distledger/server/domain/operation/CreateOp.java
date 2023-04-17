package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.VectorClock;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;

public class CreateOp extends Operation {

    public CreateOp(String account, boolean stable, VectorClock prev, VectorClock opTS) {
        super(account, stable, prev, opTS);
    }

	@Override
	public DistLedgerCommonDefinitions.Operation transformOp() {
		return DistLedgerCommonDefinitions.Operation
			.newBuilder()
			.setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
			.setUserId(this.getAccount())
			.addAllPrevTS(this.getPrev().getClock())
			.addAllTS(this.getOpTS().getClock())
			.build();
	}

    @Override
    public String toString() {
	return "ledger {\ntype: OP_CREATE_ACCOUNT\nuserId: \""
		+ getAccount() + "\"\n}";
    }
}
