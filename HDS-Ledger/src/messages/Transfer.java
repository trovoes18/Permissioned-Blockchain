package messages;

import Security.DigitalSignature;
import links.ClientPerfectLink;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Transfer extends ClientRequest{
    private final PublicKey source;
    private final PublicKey destination;
    private final int amount;

    public Transfer(PublicKey source, PublicKey destination, int amount, int messageId, String senderId){
        super("transfer", messageId, senderId);
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    public PublicKey getSource() {
        return source;
    }

    public PublicKey getDestination() {
        return destination;
    }

    public int getAmount() {
        return amount;
    }

    public static void parseTransfer(int messageId, String senderId, ClientPerfectLink pl)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Scanner s = new Scanner(System.in);
        String number, fileName;

        System.out.println("Source Account: ");
        number = s.next();
        fileName = "./src/Keys/" + number + "_public_key.der";
        PublicKey source_account = DigitalSignature.Public_Key_reader(fileName);

        System.out.println("Destination Account: ");
        number = s.next();
        fileName = "./src/Keys/" + number + "_public_key.der";
        PublicKey destination_account = DigitalSignature.Public_Key_reader(fileName);

        System.out.println("Amount: ");
        int amount = Integer.parseInt(s.next());

        Transfer request = new Transfer(source_account,destination_account,amount, messageId, senderId);
        pl.send(request);

    }
}
