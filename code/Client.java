import java.io.*;
import java.security.*;
import java.util.TreeMap;

import auxiliar.*;

public class Client {
    // initialize socket and input output streams
    private TreeMap<String, String> mapping = new TreeMap<String, String>();
    private String clientConsole    = "";
    private PublicKey publicKey     = null;
    private String owner            = null;

    // constructor to put ip address and port
    public Client() {
        boolean userWasValid        = false;
        // authenticate user
        boolean correctUser         = false;
        String user                 = "";

        // Fistly: we check if the user is has a valid client name
        while (!userWasValid) {

            while (!correctUser) {
                System.out.println("Authentication: Type your client name");
                user = System.console().readLine();
                System.out.println("Is " + " your client name? (y/n)");
                String check = System.console().readLine();
                if (check.equals("y") || check.equals("Y"))
                    correctUser = true;
            }

            try {
                StoreKey skUser = new StoreKey(user);
                publicKey = skUser.pubKeyRetrieve();
            } catch (FilesClientException e) {
                System.out.println("Invalid client's name.");
            }

            if (publicKey != null) {
                userWasValid = true;
                owner = user;
                System.out.println("Correct user!");
            } else {
                correctUser = false;
            }

            // ----- new private dashboard reader -----
            try{
                Read readOp = new Read();
                String fileToParse = readOp.readFileGeneral(System.getProperty("user.dir") + "/", "nameMapping.txt");
                String[] map = fileToParse.split("/");
                for (int i = 0; i < map.length; i++){
                    String[] submap = map[i].split(";");
                    mapping.put(submap[0], submap[1]);
                }
        
            } catch (FileNotFoundException e) { 
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }   
        }
 
       
        connectionWithServer(); // iterate connection with several servers
    }

    public void shutdown() {
        System.out.println("Incorrect authentication. Please, try again later!");
    }

    public void connectionWithServer() {
        // keep reading until "Over" is input
        ContactServer contactServer = new ContactServer();
        SendReceive sr = new SendReceive(); 
        sr.iterateRegisteredPorts();

        while (!clientConsole.equals("Over")) {
            clientConsole = System.console().readLine();

            // client retrieves private key to make request to server
            StoreKey sk = new StoreKey(owner);
            String password = askForPassword();
            PrivateKey privKey = null;
            try {
                privKey = sk.privKeyRetrieve(password);


                // ----- new private dashboard reader -----
                String[] tokens = clientConsole.split("[,()]+");
                
                if (tokens.length == 3 && tokens[0].equals("read")){ //comando + nrPosts + user 
                    
                    if(mapping.containsKey(tokens[2].replace(" ", ""))){
                        clientConsole = tokens[0] + "(" + tokens[1] + "," + mapping.get(tokens[2].replace(" ", "")) + ")";
                    } else {
                        clientConsole = tokens[0] + "(" + tokens[1] + "," + "NO SUCH AUTHOR" + ")";
                    }
                }


                Message msgWithoutSignature = contactServer.createMsg(clientConsole, publicKey);

                
                Packet response = sr.broadcast(msgWithoutSignature, msgWithoutSignature.getCommand(), privKey, publicKey);

                try {
                    Message msg = serverResponseReader(response);

                    if(msg.getCommand().equals("read")){
                        String[] msgStripped = msg.getServerMessage().split("@");
                        String msgToPrint = "";  
                        for (int i = 1; i< msgStripped.length; i++){
                            msgToPrint += "-------------------Post-------------------\n";
                            msgToPrint += msgStripped[i].replace('|', '\n');
                        }
                        System.out.println("Input received from server: \n" + msgToPrint + "\n");
                    } else {
                        System.out.println("Input received from server: \n" + msg.getServerMessage() + "\n");
                    }

                } catch (InterruptedIOException | ServerIsCorruptedException | MessageNotFreshException e) {
                    e.printStackTrace();
                }
                
                sk = null; //delete StoreKey

            }catch(UnsignablePacketException e){ //the packet was not signed ---> client needs to repeat request
                e.getMessage();
                System.out.println("Please try again.");
            }catch(WrongPasswordException e){
                System.out.println("Wrong password.\n Please repeat the command.");

            }
                        
        }
        sr.closeConnections();
    }

    // ------------------socket abstractions---------------------------------

    public Message serverResponseReader(Packet packet) throws InterruptedIOException, ServerIsCorruptedException, MessageNotFreshException{
        Message msg = null;
        SignatureOperation sigOp = new SignatureOperation();

        boolean isFromServer = sigOp.verifySignature(packet.getSignature(), packet.getPublicKey());

        if(isFromServer){
            msg = sigOp.retrieveObject(packet.getSignature(), packet.getPublicKey());

            

        } else {
            throw new ServerIsCorruptedException("Server is corrupted\nSignature failed to verify");
        }
                 
        return msg;
    }

    public String prepareForDisplay(String unpreparedString){
        String sendMe = unpreparedString.replace('|', '\n');
        return sendMe;
    }

    public String askForPassword(){
        System.out.println("Authentication: Type your password");
        return System.console().readLine();
    }
    
    // ----------------------------main------------------------------

    public static void main(final String args[]) {
        final Client client = new Client(); 
    } 
    
} 