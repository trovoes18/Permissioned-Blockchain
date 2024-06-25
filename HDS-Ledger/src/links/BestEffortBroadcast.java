package links;

import messages.LinkMessage;
import messages.Message;

import java.util.HashMap;
import java.util.Map;

public class BestEffortBroadcast {

    private final Map<String, PerfectLink> links;


    public BestEffortBroadcast(Map<String, String> servers, String senderId) {
        links = new HashMap<>();
        servers.forEach((id, dest) -> links.put(id, new PerfectLink(dest, senderId)));
    }

    public void send(Message message) {
        links.forEach((source, link) -> link.send(message));
    }

    public Message receive(LinkMessage message) {
        try {
            return links.get(message.getMessage().getSenderId()).receive(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

