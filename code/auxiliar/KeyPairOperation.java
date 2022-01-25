package auxiliar;

import java.security.*;

// This class generates a Private/Public key pairing

public class KeyPairOperation {

    private KeyPair pair;

    public KeyPairOperation() throws NoSuchAlgorithmException {
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); 
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); 
            keyGen.initialize(1024, random); 

            pair = keyGen.generateKeyPair();

        } catch(NoSuchAlgorithmException alg){ 
            alg.getMessage(); //TODO throw to server
        }
       
    }

    public KeyPair getKeyPair(){
        return pair;
    }

    public PrivateKey getPrivateKey(){
        return pair.getPrivate();
    }

    public PublicKey getPublicKey(){
        return pair.getPublic();
    }
  
}