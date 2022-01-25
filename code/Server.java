
import java.net.*;
import java.security.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.io.File;
import auxiliar.*;
import java.net.ServerSocket;


public class Server{
    private PublicKey publicKey = null;
    private ServerSocket server = null;

    // create data structures
    private ServerDataStructures data               = null;
    private Announcements userAnnouncements         = null;
    private Announcements generalAnnouncements      = null;

    // counter total posts in general dashboard
    private AtomicInteger genPostCounter                    = null;
    private TreeMap<String,AtomicInteger> privPostCounter   = null;
    private String address                                  = null;
    private TreeMap<String,Integer> ridUsersPrivate         = null;
    private TreeMap<String,Integer> ridUsersGeneral         = null;
    private TreeMap<String,Integer> wtsUsersPrivate         = null;
    private TreeMap<String,Integer> wtsUsersGeneral         = null;

    private ServerRegistry sr = new ServerRegistry();

    private ServerState serverState = new ServerState();
    // constructor with port
    public Server(int port) {


        loadServerState(port);

       
        // starts server and waits for a connection
        try {
            
            this.sr.registerServer(address, port);

            server = new ServerSocket(port);
            setUpServer(port);

            System.out.println("Server started");
            System.out.println("Waiting for a clients ...");
            while (true) { 
                Socket s = null; 
                  
                try { 
                    // socket object to receive incoming client requests 
                    s = server.accept(); 
                    System.out.println("A new client is connected : " + s); 
                  
                    // obtaining input and out streams 
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); 
                    
                    System.out.println("Assigning new thread for this client"); 
    
                    // create a new thread object 
                    Thread t = new ClientHandler(s, ois, oos, this.publicKey, this.userAnnouncements, this.generalAnnouncements, this.genPostCounter, this.privPostCounter, port, this.data, this.wtsUsersPrivate, this.wtsUsersGeneral, this.ridUsersPrivate, this.ridUsersGeneral); 
    
                    // Invoking the start() method 
                    t.start(); 
                  
                } catch (Exception e){ 
                    s.close(); 
                    e.printStackTrace(); 
                } 


            }

          
        } catch (IOException i) {   System.out.println(i);}

            
    }
 

   
    public void setUpServer(int port) {

         //retrieve server's public key
         try{
            StoreKey skUser = new StoreKey("Server" + port);
            this.publicKey = skUser.pubKeyRetrieve();
        } catch(FilesClientException e){
            System.out.println("Invalid server's name.");
        }

        // creates dashboard directories
        File filePost = new File(System.getProperty("user.dir") + "/posts" + port);
        File fileGeneral = new File(System.getProperty("user.dir") + "/general" + port);

        if (!filePost.exists() && !fileGeneral.exists()) {
            if (filePost.mkdir() && fileGeneral.mkdir())
                System.out.println("Directory created");
            else
                System.out.println("Failed");
        }
    }


    public void loadServerState(int port){
        
        
        ServerDataStructures retrievedState;
        retrievedState = this.serverState.loadServerState(port);
        if(retrievedState == null){
            this.data = new ServerDataStructures();
            
            System.out.println("\nFirst time server is up\n"); 
        } else{
            this.data = retrievedState;
            System.out.println("\nLoaded server's previous state\n");
            
        }
        this.userAnnouncements      = data.getUserAnnouncements();
        this.generalAnnouncements   = data.getGeneralAnnouncements();
        this.genPostCounter         = data.getGenPostCounter();
        this.privPostCounter        = data.getPrivPostCounter();
        this.address                = data.getAddress();
        this.ridUsersPrivate        = data.getRidUsersPrivate();
        this.ridUsersGeneral        = data.getRidUsersGeneral();
        this.wtsUsersPrivate        = data.getWtsUsersPrivate();
        this.wtsUsersGeneral        = data.getWtsUsersGeneral();
           
    }

    

    
    //-------------------------------main-------------------------------------

    public static void main (final String args[]) {
        try{
            
        final Server server = new Server(Integer.parseInt(args[0]));
        
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("\n\n --------------- Please enter the server port number------------------------\n\n");
            e.printStackTrace();
        }
    
    }

}
