package consensusMessages;

public class Commit extends ConsensusMessage {
    public Commit(int consensus_instance, int round, Block value, String senderId, boolean fakeId) {
        super("commit", consensus_instance, round, senderId, value, fakeId);
    }
}
