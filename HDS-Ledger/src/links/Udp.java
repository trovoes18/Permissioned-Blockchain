package links;


import Security.DigitalSignature;
import Security.Mac;
import messages.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.Key;

public class Udp {

    private final String destination;
    private DatagramSocket socket;

    public Udp(String destination) {
        this.destination = destination;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            socket = null;
            e.printStackTrace();
        }
    }


    public void send(Message message, String nonce, String realId)  {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            LinkMessage m = new LinkMessage(message, nonce);

            SignMessage signMessage = null;
            byte[] signature = new byte[0];
            byte[] mac = new byte[0];
            Key key = null;

            if (message.getType().equals("ACK") | message.getType().equals("clientRequest")) {

                mac = Mac.create(m, m.getMessage().getSenderId(), destination);

                // Creates message with mac
                MacMessage secureMessage = new MacMessage(m,mac);

                out.writeObject(secureMessage);
                byte[] buf = byteOut.toByteArray();

                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), Integer.parseInt(destination));
                socket.send(packet);
            } else if (!message.getType().equals("clientRequest")) {

                // Signs Message
                signature = DigitalSignature.sign(m, realId);
                SignMessage secureMessage = new SignMessage(m,signature);

                out.writeObject(secureMessage);
                byte[] buf = byteOut.toByteArray();

                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), Integer.parseInt(destination));
                socket.send(packet);
            }


        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDestination() {
        return destination;
    }
}
