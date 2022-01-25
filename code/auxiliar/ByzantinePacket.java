package auxiliar;
import java.io.Serializable;
import java.security.*;


// must implement Serializable in order to be sent
public class ByzantinePacket implements Serializable{
    
    private static final long serialVersionUID = 8430005448494058437L;
    private PublicKey pubKey = null;
    private SignedObject signature = null;
    

    public ByzantinePacket(PublicKey pubKey, SignedObject signature) {
        this.pubKey   = pubKey;
        this.signature = signature;
    }

    
    public PublicKey getPublicKey(){
        return pubKey;
    }

    public SignedObject getSignature(){
        return signature;
    }
}