package pt.tecnico.distledger.server.domain.operation;

import pt.tecnico.distledger.server.domain.VectorClock;

public class DeleteOp extends Operation {

    public DeleteOp(String account, boolean stable, VectorClock prev, VectorClock opTS) {
        super(account, stable, prev, opTS);
    }

    @Override
    public String toString() {
	return "ledger {\ntype: OP_DELETE_ACCOUNT\nuserId: \""
		+ getAccount() + "\"\n}";
    }
}
