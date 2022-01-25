package auxiliar;

import java.io.Serializable;
import java.security.SecureRandom;

public class Nonce implements Serializable{

    private static final long serialVersionUID = 1L;
    private final static SecureRandom randomGenerator = new SecureRandom();
    private final int random;
        
    public Nonce() {
        this.random = randomGenerator.nextInt();
    }
    public int getNonce(){
        return random;
    }

    public boolean equals(Nonce n){
        return this.random == n.getNonce();
    }


}