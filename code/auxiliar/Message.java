package auxiliar;
import java.io.Serializable;
import java.security.*;


// must implement Serializable in order to be sent
public class Message implements Serializable{
    
    private String command         = "NONE";
    private PublicKey pubKey       = null;
    private String author          = "NONE";
    private String post            = null;
    private String[] announcements = null;
    private int nrOfPosts          = -1;
    private SignedObject signature = null;
    private String serverMsg       = null;
    private Nonce nonceSender;
    private int serverPort         = -1;


    // Register
    public Message(String command, PublicKey pubKey, SignedObject signature) {
        this.command   = command;
        this.pubKey    = pubKey;
        this.signature = signature;
        
    }

    // Post or PostGeneral
    public Message(String command, PublicKey pubKey, String post, String[] announcements, SignedObject signature) {
        this.command       = command;
        this.pubKey        = pubKey;
        this.post          = post;
        this.announcements = announcements;
        this.signature     = signature;
    }

    // Read or ReadGeneral
    public Message(String command, String author, PublicKey pubKey, int nrOfPosts, SignedObject signature) {
        this.command   = command;
        this.pubKey    = pubKey;
        this.author    = author;
        this.nrOfPosts = nrOfPosts;
        this.signature = signature;
    }

    // Server
    public Message(String serverMsg, PublicKey pubKey, Nonce nonceSender, int port){
        this.serverMsg = serverMsg;
        this.pubKey    = pubKey;
        this.nonceSender = nonceSender;
        this.serverPort  = port;
    }

    // Server Read
    public Message(String serverMsg, PublicKey pubKey, Nonce nonceSender, String author, int port){
        this.serverMsg = serverMsg;
        this.pubKey    = pubKey;
        this.nonceSender = nonceSender;
        this.author = author;
        this.serverPort = port;
    }

    public String getCommand(){
        return command;
    }

    public PublicKey getPublicKey(){
        return pubKey;
    }
    public int getServerPort(){
        return this.serverPort;
    }

    public String getAuthor(){
        return author;
    }

    public String getPost(){
        return post;
    }

    public String[] getAnnouncements(){
        return announcements;
    }

    public int getNrOfPosts(){
        return nrOfPosts;
    }

    public SignedObject getSignature(){
        return signature;
    }

    public String getServerMessage(){
        return serverMsg;
    }
    
    public Nonce getNonceSender(){
        return nonceSender;
    }
    

    public String clientMsgDisplay() {
        String display = "";
        display += command;
        display += "(PublicKey";
        if(post != null)
            display += ',' + post;
        if(announcements != null)
            display += ',' + "announcements";
        if(nrOfPosts != -1)
            display += ',' + nrOfPosts;
        display += ')';

        return display;
    }

    public void setCommand(String command){
        this.command = command;
    }

    public void setNonceSender(Nonce nonceSender){
        this.nonceSender = nonceSender;
    }

    public void setServerPort(int port){
        this.serverPort = port;
    }

    public void setPublicKey(PublicKey pub){
        this.pubKey = pub;
    }
    

    private static final long serialVersionUID = 7174797758589418242L;
}