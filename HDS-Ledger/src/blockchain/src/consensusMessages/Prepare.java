package consensusMessages;

public class Prepare extends ConsensusMessage {
    public Prepare(int consensus_instance, int round, Block value, String senderId, boolean fakeId) {
        super("prepare", consensus_instance, round, senderId, value, fakeId);
    }
}
