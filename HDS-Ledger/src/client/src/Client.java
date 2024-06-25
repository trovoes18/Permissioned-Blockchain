import Security.DigitalSignature;
import links.ClientPerfectLink;
import messages.LinkMessage;
import messages.SignMessage;
import messages.ClientRequest;
import messages.Decision;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {


        // Read input argument
        int port = Integer.parseInt(args[0]);
        int messageId = 0;

        Map<String, String> pki = new HashMap<>();
        Scanner s;

        try {
            File keys = new File("./src/blockchain/src/configuration_keys");
            s = new Scanner(keys);
        } catch (Exception e) {
            System.out.println("Failed to read the configurations file");
            System.out.println(e.getMessage());
            return;
        }

        // Reads Keys config file
        while ((s.hasNextLine())) {
            String[] line = s.nextLine().split(" ");
            pki.put(line[0], line[1]);
        }

        DatagramSocket receiveSocket = new DatagramSocket(port);

        ClientPerfectLink pl = new ClientPerfectLink("10001");

        while (true) {
            System.out.println("What do you want to append to the blockchain?");
            Scanner scanner = new Scanner(System. in);
            String text = scanner.nextLine();

            ClientRequest request = new ClientRequest(text, messageId, Integer.toString(port));
            messageId++;

            pl.send(request);

            System.out.println("Waiting for server response...");
            byte[] buf = new byte[67000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            receiveSocket.receive(packet);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(buf);
            ObjectInputStream in = new ObjectInputStream(byteIn);
            SignMessage response = (SignMessage) in.readObject();

            // Verify signature
            LinkMessage message = response.getLinKMessage();
            byte[] signature = response.getSignature();

            if (DigitalSignature.verify(message, signature, pki.get(message.getMessage().getSenderId()))) {
                Decision d = pl.receive(message);
                System.out.println("Message received from server. Decided value: " + d.getValue());
            }
        }
    }
}
