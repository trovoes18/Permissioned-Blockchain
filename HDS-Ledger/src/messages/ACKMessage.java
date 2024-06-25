package messages;

import messages.Message;

public class ACKMessage extends Message {

    private final String nonce;

    public ACKMessage(String nonce, String senderId) {
        super("ACK", senderId, false);
        this.nonce = nonce;
    }

    public String getNonce() {
        return nonce;
    }
}
