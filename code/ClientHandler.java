 
import java.io.*; 
import java.net.*; 
import java.security.*;
import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import auxiliar.*;

public class ClientHandler extends Thread {
    final int UNDEFINED = -1;

    private ObjectOutputStream objectOutputStream   = null;
    private ObjectInputStream objectInputStream     = null;
    private PublicKey publicKey                     = null;
    private int port; 

    //to assure accountability
    private ServerDataStructures data                     = null;
    private Announcements userAnnouncements               = null;
    private Announcements generalAnnouncements            = null; 
    private AtomicInteger genPostCounter                  = null;
    private TreeMap<String,AtomicInteger> privPostCounter = null;
    private TreeMap<String,Integer> ridUsersPrivate       = null;
    private TreeMap<String,Integer> ridUsersGeneral       = null;
    private TreeMap<String,Integer> wtsUsersPrivate       = null;
    private TreeMap<String,Integer> wtsUsersGeneral       = null;
    private ConcurrentHashMap<String,Nonce> clientNonces  = null;

    //to assure freshness
    private Nonce myNonce  = null;

    //used to save the server's state
    private ServerState serverState = new ServerState();

      
  
    // Constructor 
    public ClientHandler(Socket s, ObjectInputStream ois, ObjectOutputStream oos, PublicKey publicKey, 
                        Announcements userAnnouncements, Announcements generalAnnouncements, AtomicInteger genPostCounter,
                        TreeMap<String, AtomicInteger> privPostCounter, int port, ServerDataStructures data, 
                        TreeMap<String,Integer> wtsUsersPrivate, TreeMap<String,Integer> wtsUsersGeneral,
                        TreeMap<String,Integer> ridUsersPrivate, TreeMap<String,Integer> ridUsersGeneral)  { 
        this.objectInputStream              = ois; 
        this.objectOutputStream             = oos; 
        this.publicKey                      = publicKey;
        this.userAnnouncements              = userAnnouncements;
        this.generalAnnouncements           = generalAnnouncements;
        this.genPostCounter                 = genPostCounter;
        this.privPostCounter                = privPostCounter;
        this.port                           = port;
        this.data                           = data;
        this.ridUsersPrivate                = ridUsersPrivate;
        this.ridUsersGeneral                = ridUsersGeneral;
        this.wtsUsersPrivate                = wtsUsersPrivate;
        this.wtsUsersGeneral                = wtsUsersGeneral;
        this.clientNonces                   = new ConcurrentHashMap<String,Nonce>();

    } 
  
    @Override
    public void run()  { 
        
            try { 
                //retrieve server's public key
                try {
                    StoreKey skUser = new StoreKey("Server" + this.port);
                    publicKey = skUser.pubKeyRetrieve();
                } catch(FilesClientException e){
                    System.out.println("Invalid server's name.");
                }

                Packet clientPacket             = null;
                ByzantinePacket clientByzPacket = null;
                Message clientMsg               = null;
                ByzantineMessage clientByzMsg   = null;
                boolean open                    = true; //status of connection to client
                ContactClient cc                = new ContactClient();
                // reads message from client
                while (open) {
                    clientByzPacket = receiveByzFromClient();
                    SignatureOperation sigOp = new SignatureOperation();

                    //----------------------ENTREGA 2----------------------
                    int rid = -1;
                    int wts = -1;
                    boolean byzPacketSignVerified = cc.verifyByzMessageFromClient(clientByzPacket);
                    if(byzPacketSignVerified){
                        clientByzMsg = sigOp.retrieveByzObject(clientByzPacket.getSignature(), clientByzPacket.getPublicKey());
                        clientPacket = clientByzMsg.getPacket();
                        rid = clientByzMsg.getRID();
                        wts = clientByzMsg.getWTS();    
                    } else {
                        System.out.println("ByzantinePacket was NOT verified!");
                    }
                    //-----------------------------------------------------

                    
                    if(cc.verifyMessageFromClient(clientPacket) && byzPacketSignVerified){
                        clientMsg = sigOp.retrieveObject(clientPacket.getSignature(), clientPacket.getPublicKey());
                        // User's PublicKey ---hash---> UserID
                        Hash hash = new Hash();
                        String key = hash.createHash(clientMsg.getPublicKey());
                        key = key.replaceAll("/", "-"); // MDR char set includes / that isnt allowed on a folder name

                        if(!clientNonces.containsKey(key)) //first time client sends message
                            clientNonces.put(key, clientMsg.getNonceSender());
                        else{ //need to verify client has same nonce sent by server
                            Nonce nonceToVerify = clientMsg.getNonceSender();
                            if(clientNonces.get(key).equals((nonceToVerify))) {
                                open = false;  //immediatly closes connection   
                                System.out.println("Client sent the wrong nonce!\n");
                            }
                            else {
                                clientNonces.put(key, clientMsg.getNonceSender()); //update nonce sent by the client
                                System.out.println("The client sent the right nonce");
                            }
                        }
                        
                        System.out.println("  \n\nInput received from client: " + clientMsg.clientMsgDisplay());
                        try {
                            parseCommands(clientMsg, rid, wts);
                        } catch(WrongParametersException wp) {
                            sendMessageToClient("Error: incorrect input for command.\n", rid, wts, "NULL", null);
                        }
                    } else {
                        open = false; //immediatly closes connection
                    }
                    
                }
                System.out.println("Closing connection");
        
                // closing resources 
                this.objectInputStream.close(); 
                this.objectOutputStream.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    }


     // --------------------input processing function---------------------------

     public void parseCommands(Message inputClient, int rid, int wts) throws WrongParametersException{
        String command = inputClient.getCommand();

        // User's PublicKey ---hash---> UserID
        Hash hash = new Hash();
        String key = hash.createHash(inputClient.getPublicKey());
        key = key.replaceAll("/", "-"); // MDR char set includes / that isnt allowed on a folder name

        VerifyParameters verifyParameters = new VerifyParameters(inputClient);
        switch (command) {

            case "register":
                register(key, rid, wts);
                break;

            case "post":
                try {
                    verifyParameters.verifyPost();
                    post(key, inputClient.getPost(), inputClient.getAnnouncements(), rid, wts);
                } catch (UserDoesntExistException userExp) {
                    sendMessageToClient(userExp.getMessage(), rid, wts, "NULL", null);
                    System.out.println(userExp.getMessage());
                } 
                break;

            case "postGeneral":
                try {
                    System.out.println("Writing the post");
                    verifyParameters.verifyPostGeneral();
                    postGeneral(key, inputClient.getPost(), inputClient.getAnnouncements(), rid, wts);
                } catch (UserDoesntExistException userExp) {
                    sendMessageToClient(userExp.getMessage(), rid, wts, "NULL", null);
                    System.out.println(userExp.getMessage());
                } 
                break;
            case "postGeneralWrite":
                if(rid > ridUsersGeneral.get(key)) //verify read command has right id
                    ridUsersGeneral.put(key,rid);

                sendMessageToClient("RANK", rid, wtsUsersGeneral.get(key), "NULL", null); // response to give wts
                System.out.println("sent the last WTS and rank to client");
                break;
                

            case "read":
                try {
                    verifyParameters.verifyRead();
                    read(key, inputClient.getNrOfPosts(), rid, wts, inputClient.getAuthor());
                } catch (UserDoesntExistException userExp) {
                    System.out.println(userExp.getMessage());
                } 
                break;

            case "readGeneral":
                verifyParameters.verifyReadGeneral();
                readGeneral(key, inputClient.getNrOfPosts(), rid, wts);
                break;

            case "readWriteback":
                //TODO: verifyParameters.verifyReadWriteback();
                ridUsersPrivate.put(key,rid);
                if(wts > wtsUsersPrivate.get(key)){
                    readWriteback(inputClient.getServerMessage(), key, rid, wts, inputClient.getAuthor());
                    sendMessageToClient("ACK UPDATED\n", rid, wts, "NULL", null);
                } else {
                    sendMessageToClient("ACK NOT UPDATED\n", rid, wts, "NULL", null);
                }
                break;
                
            default:
                System.out.println("No such command\n"); 
                sendMessageToClient("Error: No such command\n", rid, wts, "NULL", null);
                break;
        }
    }

    // -----------------------command functions---------------------------------

    // Register a new user to the Announcements Server
    public void register(String key, int rid, int wts) {

        if (userAnnouncements.userExists(key)) {
            sendMessageToClient("That user is already registered\n", UNDEFINED, UNDEFINED, "NULL", null);
        } else {
            //creates user dir inside postsPORT folder
            File file = new File( System.getProperty("user.dir") + "/posts" + port + "/" + key );
            if (!file.exists()){
                if (!file.mkdir())
                    System.out.println("Failed to create " + key + " directory!");
            }

            // created all data structures for that user
            AtomicInteger count = new AtomicInteger();
            privPostCounter.put(key, count);
            userAnnouncements.registerNewUser(key);
            generalAnnouncements.registerNewUser(key);
            wtsUsersPrivate.put(key,-1);
            wtsUsersGeneral.put(key,-1);
            ridUsersPrivate.put(key,-1);
            ridUsersGeneral.put(key,-1);
            String registerStatus = "\nACK: Server successfully registered user.\n";
            sendMessageToClient(registerStatus, UNDEFINED, UNDEFINED, "NULL", null);
            serverState.saveServerState(this.data, this.port);
        }
    }

    // Post an Annoucement to the user's Announcement Board
    public void post(String key, String message, String[] a, int rid, int wts) throws UserDoesntExistException {
        if (!userAnnouncements.userExists(key)) {
            throw new UserDoesntExistException("That user does not exist\n");
        } else {     
            if(wtsUsersPrivate.get(key) < wts){
                FileOperation fo = new FileOperation();
                String messageStatus = fo.safelySavePostFile(privPostCounter.get(key).incrementAndGet(), false,  message, a, key, port);
                
                // the post is assigned to the user in the generalAnnounc
                userAnnouncements.registerNewPost(key, privPostCounter.get(key).get());
                
                // send to client
                String messageToClient = "ACK: The post was successfully saved in your private dashboard!";
                sendMessageToClient(messageToClient, UNDEFINED, wts, "NULL", null);
                
                wtsUsersPrivate.put(key, wts); //updates new timestamp
                serverState.saveServerState(this.data, this.port);
            }    
            
        }
    }

    // Post an Annoucement to the general Announcement Board
    public void postGeneral(String key, String message, String[] a, int rid, int wts) throws UserDoesntExistException {
        if (!userAnnouncements.userExists(key)) {
            throw new UserDoesntExistException("That user does not exist\n");
        } else {
            if(wtsUsersGeneral.get(key) < wts){
                FileOperation fo = new FileOperation();
                String filename = fo.createFilePost(genPostCounter.incrementAndGet(), true, "", port, null);
                String messageStatus = fo.writePost(filename, message, a, key);

                // the post is assigned to the user in the generalAnnounc
                generalAnnouncements.registerNewPost(key, genPostCounter.get());

                // send to client
                String messageToClient = "ACK: The post was successfully saved in the general dashboard!";
                sendMessageToClient(messageToClient, UNDEFINED, wts, "NULL", null);
                wtsUsersGeneral.put(key, wts); //updates new timestamp
                serverState.saveServerState(this.data, this.port);
            }
        }
    }

    // Client obtains the last 'num' of announcements by the user 'key
    public void read(String key, int num, int rid, int wts, String readerKey) throws UserDoesntExistException {

        Read readOperation = new Read(num, false);
        try {
            String readStatus = readOperation.readOperation(privPostCounter.get(readerKey).get(), this.port, readerKey);
            ridUsersPrivate.put(key,rid);
            sendMessageToClient(readStatus, rid, wtsUsersPrivate.get(key), "read", readerKey); //value, rid, timestamp
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Client obtains the last 'num' of general announcements
    public void readGeneral(String key, int num, int rid, int wts) {
        Read readOperation = new Read(num, true);
        try {
            String readStatus = readOperation.readOperation(genPostCounter.get(),this.port, key);
            String readResponse = readOperation.prepareForDisplay(readStatus); 
            ridUsersGeneral.put(key,rid);
            sendMessageToClient(readResponse, rid, UNDEFINED, "NULL", null);
        } catch (Exception e) {
            e.printStackTrace();
        } 

    }

    public void readWriteback(String posts, String key, int rid, int wts, String author) {
        String[] msgStripped = posts.split("@");
        Read readOp = new Read();
        FileOperation fileOp = new FileOperation();
        boolean notEqual = true;
        String filename = readOp.getFilename(privPostCounter.get(author).get());
        String path = System.getProperty("user.dir") + "/posts" + port + "/" + author + "/";
        String post = readOp.getPostContent(null, path + filename).replace("@", "");
        ArrayList<String> toUpdate = new ArrayList<>();

        //----------------------------------------------------------------
        while(notEqual){
            
            for (int i = 1; i < msgStripped.length; i++){ // Percorre o que recebeu do READ
                if (i == msgStripped.length - 1){
                    notEqual = false;
                    break;
                }
                if(post.equals(msgStripped[i])){
                    notEqual = false;
                    break;
                } else {
                    toUpdate.add(msgStripped[i]);
                }
            }
        }

        for(String st : toUpdate) {
            filename = fileOp.generalFileCreate(path, readOp.getFilename(privPostCounter.get(key).incrementAndGet()));
            fileOp.generalWriteFile(filename, st);
        }

        System.out.println("The writeback updated " + toUpdate.size() + " posts");
        //----------------------------------------------------------------
    }


    // ------------------socket abstractions---------------------------------
    public void sendMessageToClient(String message, int rid, int wts, String command, String author) {
        PasswordServers ps = new PasswordServers();
        ContactClient contactClient = new ContactClient(); 

        //server retrieves private key to make request to server
        StoreKey sk = new StoreKey("Server" + port);
        String password = ps.getServerPassword(port);
        PrivateKey privKey = null;
        try{
            privKey = sk.privKeyRetrieve(password); 
        }catch(WrongPasswordException e){
            e.getMessage();
        }
        
        //create, sign and send message
        //----------------------------NONCES -----------------------------
        this.myNonce = new Nonce();
        Message msgWithoutSignature = contactClient.createMsg(message, publicKey, this.myNonce, null, port);
      
        if(command.equals("read")){
            msgWithoutSignature = contactClient.createMsg(message, publicKey, this.myNonce, author, port);
        }

        SignedObject msgSigned = null;

        int count = 0;
        int maxTries = 3;
        boolean keepTry = true;
        while(keepTry) { 
            try {
                msgSigned = contactClient.signMessageToClient(msgWithoutSignature, privKey); //throws UnsignablePacketException
                Packet packet = new Packet(msgWithoutSignature.getPublicKey(), msgSigned);

                sk = null; //delete StoreKey

                //----------------------ENTREGA 2----------------------
                ByzantineMessage byzMsg = new ByzantineMessage(rid, wts, packet);
                if(message.equals("RANK"))
                    byzMsg.setRank(this.port);   
                    
                SignedObject byzMsgSigned = contactClient.signByzMessageToClient(byzMsg, privKey);

                ByzantinePacket byzPacket = new ByzantinePacket(publicKey, byzMsgSigned);

                //-----------------------------------------------------
                
                try {
                    objectOutputStream.writeObject(byzPacket); // ENTREGA2: Sending byzPacket to Client
                } catch (IOException i) {
                    System.out.println(i);
                }
                keepTry = false;
            } catch(UnsignablePacketException e){ //the packet was not signed 
                e.getMessage();
                System.out.println("Error: Server wasn't able to reply to Client's request.\n We will try again.");

                if (++count == maxTries) {
                    keepTry = false;
                    System.out.println("Error: Server wasn't able to reply to Client's request.\n ");
                 }
            }
        }        
    }


    
    public ByzantinePacket receiveByzFromClient() {
        ByzantinePacket byzPacket = null;
        try {
            byzPacket = (ByzantinePacket) objectInputStream.readObject();
        } catch (IOException i) {System.out.println(i);}
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return byzPacket;
    }
}
