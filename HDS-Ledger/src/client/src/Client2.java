import Security.DigitalSignature;
import links.ClientPerfectLink;
import messages.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client2 {

    public static void main(String[] args) throws Exception {


        // Read input argument
        int port = Integer.parseInt(args[0]);
        String senderId = Integer.toString(port);
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
            System.out.println("What operation do you want to perform?");
            System.out.println("    1 - Create an account");
            System.out.println("    2 - Transfer money");
            System.out.println("    3 - Check account balance");
            Scanner scanner = new Scanner(System. in);
            String operation = scanner.nextLine();

            switch (operation){
                case "1":
                    CreateAccount.parse_account(messageId, senderId, pl);
                    break;
                case "2":
                    Transfer.parseTransfer(messageId,senderId,pl);
                case "3":
                    CheckBalance.parse_check(messageId, senderId, pl);
                    break;
            }

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
