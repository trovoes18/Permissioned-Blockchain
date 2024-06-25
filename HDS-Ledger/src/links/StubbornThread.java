package links;


import messages.Message;

import java.util.concurrent.atomic.AtomicBoolean;

public class StubbornThread implements Runnable{

    private final Message message;
    private final AtomicBoolean flag;
    private final String nonce;
    private final Udp udp;


    public StubbornThread(Message message, AtomicBoolean flag, String nonce, Udp udp){
        this.message = message;
        this.flag = flag;
        this.nonce = nonce;
        this.udp = udp;
    }

    @Override
    public void run() {
        long timeout = 2;
        System.out.println("Start sending " + message.getType() + " with nonce " + nonce + " to " + udp.getDestination());
        String realId = message.getSenderId();
        if (message.isFakeId())
            message.setSenderId("2");
        while(flag.get()){
            if (timeout < Long.MAX_VALUE - 1)
                timeout = timeout*4;
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                System.out.println("Thread Error");
            }
            udp.send(message, nonce, realId);
        }
        System.out.println("Stop sending " + message.getType() + " with nonce " + nonce + " to " + udp.getDestination());
    }
}
