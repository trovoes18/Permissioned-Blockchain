import consensusMessages.*;
import links.BestEffortBroadcast;
import messages.LinkMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockchainInstance {
    private final String id;
    private final int consensusInstance;
    private int round;
    private int preparedRound;
    private Block preparedValue;
    private final Block inputValue;
    private final int nrServers;
    private final Map<byte[], List<String>> preparesReceived;
    private final Map<byte[], List<String>> commitsReceived;
    private final int nrToleratedFaults;

    private final String behaviour;

    private Block isDecided = null;

    private final BestEffortBroadcast broadcast;

    public BlockchainInstance(String id, int consensusInstance, Block value, Map<String, String> serversAddresses, int nrServers, int nrToleratedFaults, String behaviour){
        this.id = id;
        this.consensusInstance = consensusInstance;
        round = 1;
        preparedRound = 0;
        preparedValue = null;
        inputValue = value;
        this.nrServers = nrServers;
        this.nrToleratedFaults = nrToleratedFaults;
        broadcast = new BestEffortBroadcast(serversAddresses, id);
        preparesReceived = new HashMap<>();
        commitsReceived = new HashMap<>();
        this.behaviour = behaviour;
        if (id.equals(leader()) || behaviour.equals("B2"))
            prePrepare();
    }

    public void readMessage(ConsensusMessage message) {
        if (message != null) {
            try {
                switch (message.getType()) {
                    case "prePrepare" -> handlePrePrepare((PrePrepare) message);
                    case "prepare" -> handlePrepare((Prepare) message);
                    case "commit" -> handleCommit((Commit) message);
                    default -> System.out.println("Default");
                }
            } catch (Exception e) { //Guarantee that a random message with a valid type doesn't raise an exception
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(LinkMessage m) {
        ConsensusMessage mes = (ConsensusMessage) broadcast.receive(m);
        readMessage(mes);
    }

    private void prePrepare() {
        PrePrepare message = new PrePrepare(consensusInstance, round, inputValue, id, behaviour.equals("B4"));
        broadcast.send(message);
    }

    private void handlePrePrepare(PrePrepare message) {
        if (behaviour.equals("B3")) {
            Prepare m = new Prepare(consensusInstance, 5, new Block(new ArrayList<>()), id, false);
            broadcast.send(m);
        }
        if (!message.getConsensus_instance().equals(Integer.toString(consensusInstance)))
            return;
        if(!message.getSenderId().equals(leader())) {
            System.out.println("Invalid leader sent PrePrepare");
            return;
        }
        if (!behaviour.equals("B3")) {
            Prepare m = new Prepare(consensusInstance, message.getRound(), message.getValue(), id, behaviour.equals("B4"));
            broadcast.send(m);
            System.out.println("Broadcasting Prepare!!");
        }
    }

    public byte[] calculateHash(Block block) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out;
            out = new ObjectOutputStream(bos);
            out.writeObject(block);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            return md.digest(yourBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] mapContains(Map<byte[], List<String>> map, byte[] hash) {
        for (byte[] key : map.keySet())
            if (Arrays.equals(key, hash))
                return key;
        return null;
    }

    public byte[] handleConsensusMessage(ConsensusMessage message, Map<byte[], List<String>> map, byte[] hash) {
        if (!message.getConsensus_instance().equals(Integer.toString(consensusInstance)))
            return null;
        byte[] mapKey = mapContains(map, hash);
        if (mapKey == null){
            List<String> aux = new ArrayList<>();
            aux.add(message.getSenderId());
            map.put(hash, aux);
        } else if (!map.get(mapKey).contains(message.getSenderId())) {
            map.get(mapKey).add(message.getSenderId());
        } else
            return null;
        return mapKey;
    }

    private void handlePrepare(Prepare message) {
        byte[] hash = calculateHash(message.getValue());
        if (behaviour.equals("B3")) {
            Commit m = new Commit(consensusInstance, 5, new Block(new ArrayList<>()), id, false);
            broadcast.send(m);
        }
        hash = handleConsensusMessage(message, preparesReceived, hash);
        if (hash == null)
            return;
        if (preparesReceived.get(hash).size() > (nrServers + nrToleratedFaults)/2){
            preparesReceived.remove(hash);
            preparedRound = message.getRound();
            preparedValue = message.getValue();
            if (!behaviour.equals("B3")) {
                Commit m = new Commit(consensusInstance, preparedRound, preparedValue, id, behaviour.equals("B4"));
                broadcast.send(m);
                System.out.println("Broadcasting Commit!!");
            }
        }
    }

    private void handleCommit(Commit message) {
        byte[] hash = calculateHash(message.getValue());
        hash = handleConsensusMessage(message, commitsReceived, hash);
        if (hash == null)
            return;
        if (commitsReceived.get(hash).size() > (nrServers + nrToleratedFaults)/2){
            commitsReceived.remove(hash);
            decide(message.getValue());
        }
    }

    private void decide(Block value) {
        System.out.println("Value: " + value + " decided!");
        isDecided = value;
    }

    public Block getIsDecided() {
        return isDecided;
    }

    private String leader() {
        return "10001";
    }
}
