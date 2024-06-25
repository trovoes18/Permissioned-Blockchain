package messages;

import java.io.Serializable;

public abstract class SecureMessage implements Serializable {
    private LinkMessage message;
    String type;

    public SecureMessage(LinkMessage message, String type){
        this.message = message;
        this.type = type;
    }

    public LinkMessage getLinKMessage() {
        return message;
    }
}
