package Security;

import messages.LinkMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DigitalSignature {

    public static byte[] sign(LinkMessage message, String id) {
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

    public static Boolean verify(LinkMessage message, byte[] signature, String filename) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, ClassNotFoundException, InvalidKeySpecException {

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
}
