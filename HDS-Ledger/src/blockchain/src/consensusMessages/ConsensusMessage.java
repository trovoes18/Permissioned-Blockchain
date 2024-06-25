package consensusMessages;

import messages.Message;

public abstract class ConsensusMessage extends Message {
    final String consensus_instance;
    final int round;
    final Block value;

    public ConsensusMessage(String type, int consensus_instance, int round, String senderId, Block value, boolean fakeId) {
        super(type, senderId, fakeId);
        this.consensus_instance = Integer.toString(consensus_instance);
        this.round = round;
        this.value = value;
    }

    public String getConsensus_instance() {
        return consensus_instance;
    }

    public int getRound() {
        return round;
    }

    public Block getValue() {
        return value;
    }
}
