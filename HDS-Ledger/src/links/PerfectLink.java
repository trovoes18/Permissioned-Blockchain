package links;


import messages.ACKMessage;
import messages.LinkMessage;
import messages.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerfectLink extends StubbornLink {

    public static HashMap<String, List<String>> received;

    public PerfectLink(String destination, String senderId){
        super(destination, senderId);
        received = new HashMap<>();
    }

    public String send(Message message){
        super.send(message);
        return null;
    }

    public Message receive(LinkMessage message){
        String senderId = message.getMessage().getSenderId();

        Message m = message.getMessage();
        if(!received.containsKey(senderId)){
            List<String> aux = new ArrayList<>();
            aux.add(message.getNonce());
            received.put(senderId, aux);
            super.receive(m, message.getNonce());
            return m.getType().equals("ACK") ? null : m;
        } else if (!received.get(senderId).contains(message.getNonce())){
            received.get(senderId).add(message.getNonce());
            super.receive(m, message.getNonce());
            return m.getType().equals("ACK") ? null : m;
        }
        if (!message.getMessage().getType().equals("ACK"))
            super.sendAck(new ACKMessage("ACK" + message.getNonce(), super.senderId), message.getNonce());
        return null;
    }
}
