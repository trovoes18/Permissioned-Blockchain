package messages;

public class ClientRequest extends Message{
    private final int messageId;
    private final String requestType;

    public ClientRequest(String requestType, int messageId, String senderId) {
        super("clientRequest", senderId, false);
        this.requestType = requestType;
        this.messageId = messageId;
    }

    public String getRequestType() {
        return requestType;
    }

    public int getMessageId() {
        return messageId;
    }
}
