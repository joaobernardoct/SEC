import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignedObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

import auxiliar.*;

public class SendReceive extends Thread{
    final int UNDEFINED = -1;

    private ContactServer contactServer                     = new ContactServer();
    private SignatureOperation sigOp                        = new SignatureOperation();

    private Socket socket                                   = null;
    private ObjectOutputStream objectOutputStream           = null;
    private ObjectInputStream objectInputStream             = null;

    private ConcurrentHashMap<Integer,Nonce> serverNonces    = null;

    private ArrayList<ObjectOutputStream> socketOutArray    = new ArrayList<ObjectOutputStream>();
    private ArrayList<ObjectInputStream> socketInArray      = new ArrayList<ObjectInputStream>();
    private ArrayList<Socket> socketArray                   = new ArrayList<Socket>();
    private ArrayList<Integer> socketPortArray              = new ArrayList<Integer>();

    private String address = "127.0.0.1";

    // Authenticated-Data Byzantine Quorum
    private AtomicInteger acks                              = new AtomicInteger();
    private AtomicInteger rid                               = new AtomicInteger();
    private AtomicInteger wts                               = new AtomicInteger();
    private ArrayList<ByzantinePacket> readList             = new ArrayList<>();
    

    private int majority                                    = (4+1)/2; // N=4, f=1 --> (N+f)/2
    private ArrayList<Integer> alreadyReceivedFrom          = new ArrayList<>();
    private Timer timer                                     = new Timer();
    private AtomicBoolean reading                           = new AtomicBoolean();
    private Nonce clientNonce                               = null;

    public SendReceive() {
        this.serverNonces = new ConcurrentHashMap<Integer,Nonce>();
    }

    public void establishConnection(Integer port) {
        

        // establish a connection
        try {
            socket = new Socket(address, port);
            socket.setSoTimeout(10 * 1000); // the client will only wait 10 seconds for a server response
            System.out.println("Connected");

            try {
                // sends output to the server socket
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                socketOutArray.add(objectOutputStream);

                // takes input from server socket
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                socketInArray.add(objectInputStream);

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (final UnknownHostException u) {
            System.out.println(u);
        } catch (final IOException i) {
            System.out.println(i);
        }

        socketArray.add(socket);
        socketPortArray.add(port);
        
    }

    public Packet broadcast(Message unsignedClientMessage, String type, PrivateKey privKey, PublicKey pubKey) throws UnsignablePacketException {
        int readID  = -1;
        int writeTS = -1;
        ByzantineMessage byzMsg = null;
        

        //-------------------------------NONCES-----------------------------------
        this.clientNonce = new Nonce();
        unsignedClientMessage.setNonceSender(clientNonce);

        //------------------------------------------------------------------------

        SignedObject msgToSend = contactServer.signMessageToServer(unsignedClientMessage, privKey);
        Packet packet = new Packet(unsignedClientMessage.getPublicKey(), msgToSend);

        if (type.equals("read")){ //(1,N) Atomic Register
            // RID
            readID = rid.getAndIncrement();
            reading.getAndSet(true);
            byzMsg = new ByzantineMessage(readID, UNDEFINED, packet);
        }
        else if( type.equals("readGeneral")) {
            // RID
            readID = rid.getAndIncrement();
            byzMsg = new ByzantineMessage(readID, UNDEFINED, packet);
        }
        else if (type.equals("post") ) {
            // WTS
            writeTS = wts.getAndIncrement();
            byzMsg = new ByzantineMessage(UNDEFINED, writeTS, packet);
        }
        else if (type.equals("postGeneral") ) {
            // WTS
            readID = rid.getAndIncrement();
            Message msgToR = sigOp.retrieveObject(packet.getSignature(), packet.getPublicKey());

            //change command type for server
            msgToR.setCommand("postGeneralWrite");
            type = "postGeneralWrite"; //change type for rest of the send receive operations
            SignedObject msgRedefine = contactServer.signMessageToServer(msgToR, privKey);
            Packet packetRedefine = new Packet(msgToR.getPublicKey(), msgRedefine);
            byzMsg = new ByzantineMessage(readID, UNDEFINED, packetRedefine);

        }


 
        else if (type.equals("register")){
            byzMsg = new ByzantineMessage(UNDEFINED, UNDEFINED, packet);
        }

        Packet socketResponse = null;
        
        try{
            communicationWithServer(byzMsg, privKey, pubKey);

            //-------------------------------------------------------------------------------------
            
            //server collecting the responses
            if(type.equals("read")){ 
                ByzantineMessage bm = readMajority(readID);
                socketResponse = bm.getPacket();
            
         
                readID = rid.getAndIncrement();
                writeTS = wts.getAndIncrement();

                //Write Back Phase (broadcast writen value)
                readList = new ArrayList<ByzantinePacket>();

                
                broadcastWriteback(socketResponse, readID, writeTS, privKey, pubKey);

                if(sigOp.verifySignature(socketResponse.getSignature(), socketResponse.getPublicKey())){
                    Message msg = sigOp.retrieveObject(socketResponse.getSignature(), socketResponse.getPublicKey());
                    msg.setCommand("read");
                    SignedObject msgSigned = contactServer.signMessageToServer(msg, privKey);
                    socketResponse = new Packet(pubKey, msgSigned);
                   
                } else {
                    System.out.println("The packet signature was NOT verified!");
                }
                
            } else if (type.equals("readGeneral")){
                socketResponse = readGeneralMajority(readID);
            } else if(type.equals("post")){
                socketResponse = postMajority(writeTS);
            } else if(type.equals("postGeneral")){
                socketResponse = postGeneralMajority(writeTS);
            } else if(type.equals("register")){
                socketResponse = registerMajority(readList.get(0));
                
            } else if(type.equals("postGeneralWrite")){
                
                ByzantineMessage bmess = postGeneralWriteMajority(readID); //picks highest write timestamp
                //reset readList
                readList = new ArrayList<ByzantinePacket>();

                System.out.println("preparing to send post with timestamp + 1");

                //update the rid and wts timestamps to send to the server
                int ridIdToSend = bmess.getRID();
                int writeIdToSend = bmess.getWTS() + 1;
                this.wts.set(bmess.getWTS() + 1); //set the current wts timestamp to the desired wts
                System.out.println("rid: " + ridIdToSend + "  wts(incremented): " + writeIdToSend + " rank: " + bmess.getRank());

                //reset nonces
                this.clientNonce = new Nonce();
                unsignedClientMessage.setNonceSender(clientNonce);
                //sign message
                SignedObject msgToSendPostGeneral = contactServer.signMessageToServer(unsignedClientMessage, privKey);
                Packet packetWithPostGeneral = new Packet(unsignedClientMessage.getPublicKey(), msgToSendPostGeneral);
                ByzantineMessage byzMsgPostGeneral = new ByzantineMessage(ridIdToSend, writeIdToSend, packetWithPostGeneral);

                //send post general message to server
                communicationWithServer(byzMsgPostGeneral, privKey, pubKey); //command type = postGeneral

                socketResponse = postGeneralMajority(wts.get()); //the current writeID

                
            }
            readList = new ArrayList<ByzantinePacket>();

        } catch (UnsignablePacketException e){
            e.printStackTrace();
        } catch (MessageNotFreshException e) {
            e.printStackTrace();
        }
            

        return socketResponse;  //returns to client

    }

    public void closeConnections(){
        try {
            socket.close();
        } catch (final IOException i) {
            System.out.println(i);
        }

    }

    // -----------------majority functions aux-------------------------------
    public ByzantineMessage readMajority(int readID){
        ByzantineMessage serverByzMsg    = null;
        ByzantineMessage retrievedPacket = null;
        ByzantineMessage toSend          = null;
        int receivedId                   = -1;
        int id                           = -1;
        int bestId                       = -1;

        for (ByzantinePacket bp : readList){
            boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
            if(isByzFromServer){
                serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
                retrievedPacket = serverByzMsg;
                receivedId = serverByzMsg.getRID();

                //get server port to update Nonces
                Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
                try{
                    verifyAndSetNonce(msg);
                }catch(MessageNotFreshException e){ e.printStackTrace();}
                

                if (receivedId > bestId && receivedId == readID){
                    bestId = receivedId;
                    toSend = retrievedPacket;
                }
            } else {
                System.out.println("ByzantinePacket was NOT verified!");
            }
        }
        
        return toSend;
    }

    public Packet readGeneralMajority(int readID){
        ByzantineMessage serverByzMsg = null;
        Packet retrievedPacket        = null;
        Packet toSend                 = null;
        int receivedId                = -1;
        int id                        = -1;
        int bestId                    = -1;

        for (ByzantinePacket bp : readList){
            if(bp == null){
                //
            } else {
                boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
                if(isByzFromServer){
                    serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
                    retrievedPacket = serverByzMsg.getPacket();
                
                    receivedId = serverByzMsg.getRID();
                    //get server port to update Nonces
                    Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
                    try{
                        verifyAndSetNonce(msg);
                    }catch(MessageNotFreshException e){ e.printStackTrace();}

                    if (receivedId > bestId && receivedId == readID){
                        bestId = receivedId;
                        toSend = retrievedPacket;
                    }
                } else {
                    System.out.println("ByzantinePacket was NOT verified!");
                }
            }
        }
        return toSend;
    }
    public ByzantineMessage postGeneralWriteMajority(int readID){
        ByzantineMessage serverByzMsg    = null;
        ByzantineMessage retrievedPacket = null;
        ByzantineMessage toSend          = null;
        int receivedId                   = -1;
        int receivedRId                   = -1;
        int receivedRank                 = -1;
        int id                           = -1;
        int bestId                       = -5;
        int bestRank                     = 9000;
        
        for (ByzantinePacket bp : readList){
            if(bp == null){
                System.out.println("A server crashed");
            } else {
                boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
                if(isByzFromServer){
                    serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
                    retrievedPacket = serverByzMsg;
                    receivedRId = serverByzMsg.getRID();
                    
                    receivedId = serverByzMsg.getWTS();
                    receivedRank = serverByzMsg.getRank();

                    //get server port to update Nonces
                    Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
                    try{
                        verifyAndSetNonce(msg);
                    }catch(MessageNotFreshException e){ e.printStackTrace();}
 
                    if (receivedRId == readID && receivedId > bestId && receivedRank < bestRank){ //pick highest timestamp and lowest rank
                        bestId = receivedId;
                        bestRank = receivedRank;
                        toSend = retrievedPacket;
                    }
                } else {
                    System.out.println("ByzantinePacket was NOT verified!");
                }
            }
        }
        
        return toSend;
    }

    public Packet postMajority(int writeTS){
        ByzantineMessage serverByzMsg = null;
        Packet retrievedPacket        = null;
        Packet toSend                 = null;
        int receivedId                = -1;
        int id                        = -1;
        int bestId                    = -1;

        for (ByzantinePacket bp : readList){
            boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
            if(isByzFromServer){

                serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
                retrievedPacket = serverByzMsg.getPacket();

                receivedId = serverByzMsg.getWTS(); 

                //get server port to update Nonces
                Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
                try{
                    verifyAndSetNonce(msg);
                }catch(MessageNotFreshException e){ e.printStackTrace();}

                if (receivedId > bestId && receivedId == writeTS){
                    bestId = receivedId;
                    toSend = retrievedPacket;

                }
            } else {
                System.out.println("ByzantinePacket was NOT verified!");
            }
        }
        return toSend;
    }

    public Packet postGeneralMajority(int writeTS){
        ByzantineMessage serverByzMsg = null;
        Packet retrievedPacket        = null;
        Packet toSend                 = null;
        int receivedId                = -1;
        int id                        = -1;
        int bestId                    = -5;

        for (ByzantinePacket bp : readList){
            if(bp == null){
                //
            } else {
                boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
                if(isByzFromServer){
                    serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
                    retrievedPacket = serverByzMsg.getPacket();
                
                
                    receivedId = serverByzMsg.getWTS(); 
                    //get server port to update Nonces
                    Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
                    try{
                        verifyAndSetNonce(msg);
                    }catch(MessageNotFreshException e){ e.printStackTrace();}
                    

                    if (receivedId > bestId && receivedId == writeTS){
                        bestId = receivedId;
                        toSend = retrievedPacket;
                    }
                } else {
                    System.out.println("ByzantinePacket was NOT verified!");
                }
            }
        }
        
        return toSend;
    }

    public Packet registerMajority(ByzantinePacket bp){
        ByzantineMessage serverByzMsg = null;
        Packet retrievedPacket        = null;

        boolean isByzFromServer = sigOp.verifySignature(bp.getSignature(), bp.getPublicKey());
        if(isByzFromServer){
            serverByzMsg = sigOp.retrieveByzObject(bp.getSignature(), bp.getPublicKey());
            retrievedPacket = serverByzMsg.getPacket();
        
        } else {
            System.out.println("ByzantinePacket was NOT verified!");
        }
        Message msg = sigOp.retrieveObject(serverByzMsg.getPacket().getSignature(),serverByzMsg.getPacket().getPublicKey());
    
        return retrievedPacket;
    }



    // -------------------------more abstractions---------------------------------

    public void broadcastWriteback(Packet socketResponse, int readID, int writeTS, PrivateKey privKey, PublicKey pubKey) throws MessageNotFreshException, UnsignablePacketException{
        SignatureOperation sigOp = new SignatureOperation();
        Packet packetSend = null;

        if(sigOp.verifySignature(socketResponse.getSignature(), socketResponse.getPublicKey())){
            Message msg = sigOp.retrieveObject(socketResponse.getSignature(), socketResponse.getPublicKey());
            msg.setCommand("readWriteback");
            msg.setPublicKey(pubKey);
            //-------------------------------NONCES-----------------------------------
            this.clientNonce = new Nonce();
            msg.setNonceSender(this.clientNonce);
            //------------------------------------------------------------------------
            
            SignedObject msgSigned = contactServer.signMessageToServer(msg, privKey);
            packetSend = new Packet(pubKey, msgSigned);

        } else {
            System.out.println("The packet signature was NOT verified!");
        }

        ByzantineMessage byzMsg = new ByzantineMessage(readID, writeTS, packetSend);

        communicationWithServer(byzMsg, privKey, pubKey);    
        
    }


    public void communicationWithServer(ByzantineMessage byzMsg, PrivateKey privKey, PublicKey pubKey) throws UnsignablePacketException {
        SignedObject byzMsgSigned = contactServer.signByzMessageToServer(byzMsg, privKey);
        ByzantinePacket byzPacketSend = new ByzantinePacket(pubKey, byzMsgSigned);
        
        for (int counter = 0; counter < socketArray.size(); counter++) {
            Thread sendByzMessageT = new ThreadSendMessageToServer(byzPacketSend,  socketOutArray.get(counter));
            sendByzMessageT.start();
            System.out.println("Contacted server" + socketPortArray.get(counter));
        }
    
        while(acks.get() < majority){ //--------------RECEIVE------------------------------------------
            try{
                for (int counter = 0; counter < socketArray.size(); counter++) {        
                    ByzantinePacket byzPacketReceived = receiveFromServer(socketInArray.get(counter));           
                    readList.add(byzPacketReceived); 

                    System.out.println("Received message from server " + socketPortArray.get(counter) + "\n");
                    alreadyReceivedFrom.add(socketPortArray.get(counter));
                    acks.getAndIncrement();

                }
                timer.schedule( new TimerTask() { 
                public void run() { 
                    for (int counter = 0; counter < socketArray.size(); counter++) {
                        if(!alreadyReceivedFrom.contains(socketPortArray.get(counter))){ //resend messages to servers that didnt respond
                            Thread sendByzMessageT = new ThreadSendMessageToServer(byzPacketSend,  socketOutArray.get(counter));
                            sendByzMessageT.start();
                            System.out.println("Contacted the servers again");
                        }
                        
                    }
                } 
            }, 0, 10*1000); 
            
            }   
            
            catch(InterruptedIOException e)        { e.printStackTrace(); }
            catch (ServerIsCorruptedException e)   { e.printStackTrace(); }  
        }
        timer.cancel();
        timer = new Timer();

        //cleanup acks
        acks = new AtomicInteger();
        alreadyReceivedFrom = new ArrayList<>();
    }

    // ------------------socket abstractions---------------------------------
    public ByzantinePacket receiveFromServer(ObjectInputStream in) throws InterruptedIOException, ServerIsCorruptedException{
        ByzantinePacket byzPacket = null;

        try {
            byzPacket = (ByzantinePacket) in.readObject();
        }
        catch (IOException i)              { System.out.println(i); }
        catch (ClassNotFoundException e)   { e.printStackTrace();   } 

        return byzPacket;
    }


    public void iterateRegisteredPorts(){

        ArrayList<Integer> aux = new ArrayList<>();
        Read r = new Read();

        try {
            aux = r.readPorts("./", "server_ports.txt"); // get list of ports from server_ports.txt
            for (Integer port: aux){
                if(! socketPortArray.contains(port)){
                    socketPortArray.add(port);
                    establishConnection(port);
                }
            }
                
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void verifyAndSetNonce(Message msg) throws MessageNotFreshException{
        if(!serverNonces.containsKey(msg.getServerPort())) //first time server sent message
            serverNonces.put(msg.getServerPort(), msg.getNonceSender()) ;
        else{ //need to verify server didnt repeat nonce
            Nonce nonceToVerify = msg.getNonceSender();
            if(serverNonces.get(msg.getServerPort()).equals((nonceToVerify))) {   
                System.out.println("Server sent the wrong nonce!\n"); 
            }
            else {
                serverNonces.put(msg.getServerPort(), msg.getNonceSender()) ;
                System.out.println("The server sent the right nonce");
            }
        }
    }


    
}
