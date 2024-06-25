package messages;

public class Decision extends Message{
    private final String value;
    public Decision(String senderId, String value) {
        super("decided", senderId, false);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
