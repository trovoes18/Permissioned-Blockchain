package links;


import messages.Decision;
import messages.LinkMessage;
import messages.Message;

import java.util.ArrayList;
import java.util.List;

public class ClientPerfectLink extends StubbornLink{

    private static List<String> received;
    private String lastNonce;


    public ClientPerfectLink(String destination) {
        super(destination, "");
        received = new ArrayList<>();
    }

    public String send(Message message){
        lastNonce = super.send(message);
        return lastNonce;
    }

    public Decision receive(LinkMessage message) {
        if(!received.contains(message.getNonce())){
            received.add(message.getNonce());
            super.receive(message.getMessage(), lastNonce);
            return (Decision) message.getMessage();
        }
        return null;
    }
}
