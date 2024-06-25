package messages;

public class ClientRequest2 extends Message{
    private final String value;
    private final int messageId;
    private final String requestType;
    public ClientRequest2(String value, int messageId, String senderId, String requestType) {
        super("clientRequest", senderId, false);
        this.value = value;
        this.messageId = messageId;
        this.requestType = requestType;
    }

    public String getValue() {
        return value;
    }

    public int getMessageId() {
        return messageId;
    }
}
