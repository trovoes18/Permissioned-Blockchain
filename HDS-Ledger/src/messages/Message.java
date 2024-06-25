package messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private final String type;
    private String senderId;
    final boolean fakeId;


    public Message(String type, String senderId, boolean fakeId) {
        this.type = type;
        this.senderId = senderId;
        this.fakeId = fakeId;
    }

    public String getType() {
        return type;
    }

    public String getSenderId() {
        return senderId;
    }

    public boolean isFakeId() {
        return fakeId;
    }

    public void setSenderId(String id) {
        this.senderId = id;
    }
}
