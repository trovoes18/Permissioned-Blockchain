package Security;

import messages.LinkMessage;

import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Mac {
    public static byte[] create(LinkMessage message, String senderId, String destination)  {

        javax.crypto.Mac mac = null;
        byte[] byteMessage = new byte[0];
        try {
            // Converts the message object into a byte array
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(message);
            byteMessage = byteOut.toByteArray();

            HashMap<String, String> keys = readKey(senderId);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keys.get(destination).getBytes(), "HmacSHA256");

            // Initializes and computes MAC
            mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            e.printStackTrace();
        }

        assert mac != null;
        return mac.doFinal(byteMessage);
    }

    public static HashMap<String, String> readKey(String sourceId) {
        // TODO -> Reads Key file and put in hashmap.

        HashMap<String, String> passwords = new HashMap<>();

        Scanner s = null;
        try {
            File keys = new File("./src/keys/mac_" + sourceId );
            s = new Scanner(keys);
        } catch (Exception e) {
            System.out.println("Failed to read the configurations file");
            System.out.println(e.getMessage());
        }

        // Reads Keys config file
        while (true) {
            assert s != null;
            if (!s.hasNextLine()) break;
            String[] line = s.nextLine().split(" ");
            passwords.put(line[0], line[1]);
        }
        return passwords;
    }

    public static Boolean verify(LinkMessage message, byte[] mac, String source, String destination) {

        byte [] calculated_mac = create(message,destination,source);
        return Arrays.equals(calculated_mac, mac);
    }
}
