package messages;

public class SignMessage extends SecureMessage {
    private byte[] signature;

    public SignMessage(LinkMessage message, byte[] signature){
        super(message, "signed_message");
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature;
    }

}