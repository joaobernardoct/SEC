package auxiliar;
import java.security.*;
import java.io.*;


public class SignatureOperation {

    public SignatureOperation(){
        //
    }  

    public SignedObject signData(Message unsingnedMessage, PrivateKey privateKey) throws SignDataException {
        SignedObject signedObject = null;
        try{
            Signature signature = Signature.getInstance("SHA256withRSA"); 
            signedObject = new SignedObject(unsingnedMessage, privateKey, signature);
        }
        catch(SignatureException e)        { throw new SignDataException(e.getMessage()); }
        catch(InvalidKeyException e)       { throw new SignDataException(e.getMessage()); }
        catch (NoSuchAlgorithmException e) { throw new SignDataException(e.getMessage()); }
        catch (IOException e)              { throw new SignDataException(e.getMessage()); }
        return signedObject;
    }

    public SignedObject signByzData(ByzantineMessage unsingnedMessage, PrivateKey privateKey) throws SignDataException {
        SignedObject signedObject = null;
        try{
            Signature signature = Signature.getInstance("SHA256withRSA"); 
            signedObject = new SignedObject(unsingnedMessage, privateKey, signature);
        }
        catch(SignatureException e)        { throw new SignDataException(e.getMessage()); }
        catch(InvalidKeyException e)       { throw new SignDataException(e.getMessage()); }
        catch (NoSuchAlgorithmException e) { throw new SignDataException(e.getMessage()); }
        catch (IOException e)              { throw new SignDataException(e.getMessage()); }
        return signedObject;
    }
   
    public boolean verifySignature(SignedObject signedObject, PublicKey publicKey) {
        boolean verifies = false;
        try{
            Signature sig = Signature.getInstance("SHA256withRSA");
            verifies = signedObject.verify(publicKey, sig);
        }
        catch(SignatureException e)        { return false; }
        catch (InvalidKeyException e)      { e.printStackTrace(); }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return verifies;
    }

    public Message retrieveObject(SignedObject signedObject, PublicKey publicKey) {
        try{
            Message msg = (Message) signedObject.getObject();
            return msg;
        }
        catch (IOException e)            { e.printStackTrace(); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return null;
    }

    public ByzantineMessage retrieveByzObject(SignedObject signedObject, PublicKey publicKey) {
        try{
            ByzantineMessage msg = (ByzantineMessage) signedObject.getObject();
            return msg;
        }
        catch (IOException e)            { e.printStackTrace(); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        return null;
    }
}