package messages;

import java.security.Key;

public class MacMessage extends SecureMessage{

    private byte[] mac;
    //private Key key; // Key used to calculate the MAC

    public MacMessage(LinkMessage message, byte[] mac){
        super(message, "mac_message");
        this.mac = mac;
        //this.key = key;
    }

    public byte[] getMac(){return mac; }

    //public Key getKey(){return key;}
}
