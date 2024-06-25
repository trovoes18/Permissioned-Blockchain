package messages;

import messages.Message;

import java.io.Serializable;

public class LinkMessage implements Serializable {

    private final String nonce;
    private final Message message;

    public LinkMessage(Message message, String nonce) {
        this.message = message;
        this.nonce = nonce;
    }


    public Message getMessage() {
        return message;
    }

    public String getNonce() {
        return nonce;
    }

}
