package consensusMessages;

public class PrePrepare extends ConsensusMessage {
    public PrePrepare(int consensus_instance, int round, Block value, String senderId, boolean fakeId) {
        super("prePrepare", consensus_instance, round, senderId, value, fakeId);
    }
}
