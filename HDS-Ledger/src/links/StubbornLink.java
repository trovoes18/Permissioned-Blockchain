package links;


import messages.ACKMessage;
import messages.Message;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StubbornLink {

    private final Map<String, AtomicBoolean> messages;
    private AtomicBoolean lastAck = null;
    private final String destination;
    final String senderId;
    private SecureRandom prng;
    private final Udp udp;

    public StubbornLink(String destination, String senderId) {
        messages = new HashMap<>();
        this.destination = destination;
        this.senderId = senderId;
        udp = new Udp(destination);
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String send(Message message){
        AtomicBoolean flag = new AtomicBoolean(true);
        String nonce = Integer.toString(prng.nextInt());
        Thread t = new Thread(new StubbornThread(message, flag, nonce, udp));
        messages.put(nonce, flag);
        t.start();
        return nonce;
    }

    public void sendAck(ACKMessage message, String nonce) {
        udp.send(message, "ACK" + nonce, senderId);
    }

    public void receive(Message message, String nonce){
        if (message.getType().equals("decided")){
            messages.get(nonce).set(false);
        }
        else if (!message.getType().equals("ACK")) {
            sendAck(new ACKMessage("ACK"+nonce, senderId), nonce);
        }
        else {
            try {
                messages.get(nonce.replace("ACK", "")).set(false);
            } catch (Exception e) {
                System.out.println("Invalid ACK. There is no message with such nonce");
            }
        }
    }
}
