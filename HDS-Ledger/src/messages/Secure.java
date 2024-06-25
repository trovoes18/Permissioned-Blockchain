package messages;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class Secure {

    public static byte[] sign_message(LinkMessage message, String id) {
        byte[] signed_message = new byte[0];
        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");

            String filename = "./src/keys/" + id + "_private_key.der";
            PrivateKey privateKey = Private_Key_reader(filename); // Read the private key from a pre-loaded file

            // Converts the message object into a byte array
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(message);
            byte[] byteMessage = byteOut.toByteArray();

            // Sign the message
            privateSignature.initSign(privateKey);
            privateSignature.update(byteMessage);
            signed_message = privateSignature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException |
                 InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return signed_message;

    }

    public static Boolean check_signature(LinkMessage message, byte[] signature, String filename) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, ClassNotFoundException, InvalidKeySpecException {

        PublicKey publicKey = Public_Key_reader(filename);

        // Verifies signature using the respective public key
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);

        // Converts the message object into a byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(message);
        byte[] byteMessage = byteOut.toByteArray();

        publicSignature.update(byteMessage);

        return publicSignature.verify(signature);
    }

    public static byte[] creates_mac(LinkMessage message, Key key)  {

        Mac mac = null;
        byte[] byteMessage = new byte[0];
        try {
            // Converts the message object into a byte array
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(message);
            byteMessage = byteOut.toByteArray();

            // Initializes and computes MAC
            mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            e.printStackTrace();
        }

        assert mac != null;
        return mac.doFinal(byteMessage);
    }

    public static Boolean check_mac(LinkMessage message, byte[] mac, Key key) {

        byte [] calculated_mac = creates_mac(message,key);
        return Arrays.equals(calculated_mac, mac);
    }

    public static Key generate_key() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("DES");
            SecureRandom secRandom = new SecureRandom();
            keyGen.init(secRandom);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyGen != null;
        return keyGen.generateKey();
    }


    public static PrivateKey Private_Key_reader(String filename) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey Public_Key_reader(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }



    public static byte[] mac(LinkMessage message, String senderId, String destination)  {

        Mac mac = null;
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
            mac = Mac.getInstance("HmacSHA256");
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

    public static Boolean verify_mac(LinkMessage message, byte[] mac, String source, String destination) {

        byte [] calculated_mac = mac(message,destination,source);
        return Arrays.equals(calculated_mac, mac);
    }

}
