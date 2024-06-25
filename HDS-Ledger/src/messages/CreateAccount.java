package messages;

import Security.DigitalSignature;
import links.ClientPerfectLink;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class CreateAccount extends ClientRequest{

    private final PublicKey account;

    public CreateAccount(PublicKey account, int messageId, String senderId){
        super("create_account", messageId, senderId);
        this.account = account;
    }

    public PublicKey getAccount() {
        return account;
    }

    public static void parse_account(int messageId, String senderId, ClientPerfectLink pl)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Scanner s = new Scanner(System.in);
        System.out.println("account address: ");
        String number = s.next();
        String fileName = "./src/Keys/" + number + "_public_key.der";
        PublicKey account = DigitalSignature.Public_Key_reader(fileName);
        CreateAccount request = new CreateAccount(account, messageId,senderId);
        pl.send(request);
    }
}
