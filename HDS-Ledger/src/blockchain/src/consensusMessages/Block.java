package consensusMessages;

import messages.ClientRequest;

import java.io.Serializable;
import java.util.List;

public /*final*/ class Block implements Serializable {
    private final List<ClientRequest> transactions;

    public Block(List<ClientRequest> transactions) {
        this.transactions = transactions;
    }
}
