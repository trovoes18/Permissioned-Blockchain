import consensusMessages.Block;
import consensusMessages.ConsensusMessage;
import Security.DigitalSignature;
import Security.Mac;
import messages.*;
import links.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockchainServer {

    private static String id;
    private static int nrServers;
    private static int nrToleratedFaults;
    private static String behaviour;
    private static Map<String, String> serversAddresses = new HashMap<>();
    private static Map<PublicKey, Integer> accounts = new HashMap<>();
    private static final Map<PublicKey, Integer> pendingAccounts = new HashMap<>();
    private static int consensusInstance = 0;
    private static BlockchainInstance instance = null;
    private static final List<Integer> closedInstances = new ArrayList<>();
    private static final Map<Integer, Integer> pastRequests = new HashMap<>();
    private static final List<LinkMessage> pendingMessages = new ArrayList<>();
    private static final List<Block> decidedBlocks = new ArrayList<>();
    private static final List<ClientRequest> currentTransactions = new ArrayList<>();
    private static final DatagramSocket sendSocket;

    static {
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }


    private static String  leader() {
        return "10001";
    }

    private static void byzantine5(int replyAux, Map<String, String> serversAddresses, LinkMessage message) {
        if (replyAux % 10 == 0) {
            System.out.println(("Replying message"));
            serversAddresses.forEach((sourceId, port) -> {
                Thread t = new Thread(new StubbornThread(message.getMessage(),
                        new AtomicBoolean(true), Integer.toString(replyAux), new Udp(port)));
                t.start();
            });
        }
    }

    private static boolean handleClientRequest(ClientRequest request) {
        boolean result = false;
        try {
            switch (request.getRequestType()) {
                case "transfer" -> {result = handleTransfer((Transfer) request);}
                case "create_account" -> {result = handleCreate((CreateAccount) request);}
                case "check_balance" -> {result = handleRead((CheckBalance) request);}
                default -> System.out.println("Default");
            };
        } catch (Exception e) { //Guarantee that a random message with a valid type doesn't raise an exception
            e.printStackTrace();
        }
        if (result){
            currentTransactions.add(request);
            if (currentTransactions.size() == 2) {
                System.out.println("consensusMessages.Block completed. Starting consensus...");  //TODO: Start consensus
                consensusInstance++;
                instance = new BlockchainInstance(id, consensusInstance,
                        new Block(currentTransactions.stream().toList()),serversAddresses, nrServers,
                        nrToleratedFaults, behaviour);
            }
        }
        return result;
    }

    private static boolean handleTransfer(Transfer request) {
        if (pendingAccounts.containsKey(request.getSource()) && pendingAccounts.containsKey(request.getDestination()) &&
                pendingAccounts.get(request.getSource()) >= request.getAmount()) {
            pendingAccounts.put(request.getSource(), pendingAccounts.get(request.getSource()) - request.getAmount());
            pendingAccounts.put(request.getDestination(), pendingAccounts.get(request.getDestination()) + request.getAmount());
            return true;
        }
        return false;
    }

    private static boolean handleCreate(CreateAccount request) {
        //accounts.put(request.getAccount(), 20);  commands are not yet executed
        if (!pendingAccounts.containsKey(request.getAccount())) {
            pendingAccounts.put(request.getAccount(), 20);
            return true;
        }
        return false;
    }

    private static boolean handleRead(CheckBalance request) {
        //TODO: perform read. It's not included in the block
        return false;
    }

    private static void performTransfer(Transfer request) {
        accounts.put(request.getSource(), accounts.get(request.getSource()) - request.getAmount());
        accounts.put(request.getDestination(), accounts.get(request.getDestination()) + request.getAmount());
    }

    private static void performCreate(CreateAccount request) {
        accounts.put(request.getAccount(), 20);
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Starting...");


        //Read arguments
        String[] validBehaviours = {"N", "B1", "B2", "B3", "B4", "B5"};
        try {
            id = args[0];
            if (Arrays.asList(validBehaviours).contains(args[1]))
                behaviour = args[1];
            else
                throw new Exception();
        } catch (Exception e) {
            System.out.println("Two argument needed: Id of the server and behaviour type (N or Bx with x between 1 and 5)");
            System.out.println(e.getMessage());
            return;
        }
        if (behaviour.equals("B1"))
            return;

        //Read Configuration File
        Scanner scanner;
        Scanner s;
        serversAddresses = new HashMap<>();
        Map<String, String> pki = new HashMap<>();
        nrToleratedFaults = 0;

        try {
            File conf = new File("./src/blockchain/src/configuration_servers.txt");
            File keys = new File("./src/blockchain/src/configuration_keys");
            scanner = new Scanner(conf);
            s = new Scanner(keys);
        } catch (Exception e) {
            System.out.println("Failed to read the configurations file");
            System.out.println(e.getMessage());
            return;
        }
        while ((s.hasNextLine())) {
            String[] line = s.nextLine().split(" ");
            pki.put(line[0], line[1]);
        }
        DatagramSocket socket = null;
        while ((scanner.hasNextLine())) {
            String[] line = scanner.nextLine().split(" ");
            if (line[0].equals("P")) {
                serversAddresses.put(line[1], line[2].split(":")[2]);
                if (line[1].equals(id))
                    socket = new DatagramSocket(Integer.parseInt(line[2].split(":")[2]));
            } else
                nrToleratedFaults = Integer.parseInt(line[1]);
        }
        nrServers = serversAddresses.size();

        int replyAux = 10;

        //Receive messages
        while (true) {
            byte[] buf = new byte[67000];
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                //assert socket != null;
                socket.receive(packet);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(buf);
                ObjectInputStream in = new ObjectInputStream(byteIn);
                SecureMessage messg = (SecureMessage) in.readObject();
                LinkMessage message = messg.getLinKMessage();
                boolean isAuthenticated;

                if (messg.getLinKMessage().getMessage().getType().equals("ACK") ||
                        messg.getLinKMessage().getMessage().getType().equals("clientRequest")){
                    MacMessage macMessage = (MacMessage) messg;
                    isAuthenticated = Mac.verify(message,macMessage.getMac(), message.getMessage().getSenderId(), id);
                }
                else {
                    // Verify signature
                    SignMessage signedMessage = (SignMessage) messg;
                    byte[] signature = signedMessage.getSignature();
                    isAuthenticated = DigitalSignature.verify(message, signature, pki.get(message.getMessage().getSenderId()));
                }
                if (!isAuthenticated) {
                    System.out.println("Invalid message signature or MAC with type " + message.getMessage().getType() +
                            " and nonce " + message.getNonce() + " from " + message.getMessage().getSenderId());
                    continue;
                }

                //Deal with transactionRequest messages, from clients
                if (message.getMessage().getType().equals("clientRequest")) {
                    ClientRequest r = ((ClientRequest) message.getMessage());
                    //if request wasn't received yet
                    if (!pastRequests.containsKey(Integer.parseInt(r.getSenderId())) ||
                            pastRequests.get(Integer.parseInt(r.getSenderId())).compareTo(r.getMessageId()) < 0) {
                        if (instance == null) {
                            System.out.println("Client Request Received");
                            pastRequests.put(Integer.parseInt(r.getSenderId()), r.getMessageId());
                            if (!handleClientRequest(r))
                                System.out.println("transacao rejeitada"); //TODO: Send to client
                        } else
                            pendingMessages.add(message);
                    }
                }
                // Deal with consensus related messages
                else {
                    if (!message.getMessage().getType().equals("ACK")) {
                        if (behaviour.equals("B5")) {
                            byzantine5(replyAux, serversAddresses, message);
                            replyAux++;
                        }
                        int msgInstance = Integer.parseInt(((ConsensusMessage) message.getMessage()).getConsensus_instance());
                        if (instance == null) {
                            if (closedInstances.contains(msgInstance)) {
                                continue;
                            }
                            consensusInstance = msgInstance;
                            System.out.println("New blockchain instance");
                            instance = new BlockchainInstance(id, consensusInstance,
                                    ((ConsensusMessage) message.getMessage()).getValue(),
                                    serversAddresses, nrServers, nrToleratedFaults, behaviour);
                        }
                    } else if (instance == null)
                        continue; //TODO: check if all messages are stopping
                    instance.handleMessage(message);
                }
                //If the value was decided in this while iteration
                if (instance != null && instance.getIsDecided() != null) {
                    if (leader().equals(id)) {
                        //Send decided to client
                        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(byteOut);

                        //TODO: Change decision to transaction successfully completed for each transaction
                        System.out.println(currentTransactions.size());
                        while (currentTransactions.size() > 0) {
                            LinkMessage msg = new LinkMessage(new Decision(id, currentTransactions.get(0).getRequestType()), Integer.toString(consensusInstance));
                            byte[] signature = DigitalSignature.sign(msg, msg.getMessage().getSenderId());
                            SignMessage signMessage = new SignMessage(msg, signature);
                            out.writeObject(signMessage);
                            buf = byteOut.toByteArray();
                            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"),
                                    Integer.parseInt(currentTransactions.get(0).getSenderId()));
                            sendSocket.send(packet);
                            currentTransactions.remove(0);
                        }
                        instance = null;
                        while (pendingMessages.size() > 0 && currentTransactions.size() < 2) {
                            ClientRequest request = ((ClientRequest) pendingMessages.get(0).getMessage());
                            pastRequests.put(Integer.valueOf(request.getSenderId()), request.getMessageId());
                            handleClientRequest(request);
                            pendingMessages.remove(0);
                            closedInstances.add(consensusInstance);
                        }
                        continue;
                    }
                    instance = null;  //TODO: verify this
                    closedInstances.add(consensusInstance);
                    System.out.println("accounts before -> " + accounts);
                    accounts = new HashMap<>(pendingAccounts);
                    System.out.println("accounts after -> " + accounts);
                    System.out.println("pendingAccounts -> " + pendingAccounts);
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
