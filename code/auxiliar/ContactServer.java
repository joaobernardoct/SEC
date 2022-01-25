package auxiliar;
import java.util.*;
import java.security.*;

public class ContactServer  {

    public ContactServer(){
        //
    }

    public Message createMsg(String message, PublicKey pub){
        String[] tokens = message.split("[,()]+");
        Message msg = null;
        switch (tokens[0]) {

            case "register":
                msg = new Message(tokens[0], pub, null);
                return msg;

            case "post":
                msg = new  Message(tokens[0], pub, tokens[1], Arrays.copyOfRange(tokens, 2, tokens.length), null);
                return msg;

            case "postGeneral":
                msg = new  Message(tokens[0], pub, tokens[1], Arrays.copyOfRange(tokens, 2, tokens.length), null);
                return msg;

            case "read":
                msg = new Message(tokens[0], tokens[2], pub, Integer.parseInt(tokens[1]), null);
                return msg;

            case "readGeneral":
                msg = new Message(tokens[0], null, pub, Integer.parseInt(tokens[1]), null);
                return msg;
        }
        return null;
    }

    public SignedObject signMessageToServer(Message message, PrivateKey privKey) throws UnsignablePacketException{
        SignatureOperation signature = new SignatureOperation();

        int count = 0;
        int maxTries = 3; 
        boolean successful = false;
        SignedObject signedMessage = null;
        //try maximum 3 times to sign the packet to send to client
        while(!successful) {
            try{
               signedMessage = signature.signData(message, privKey);
            }
            catch (SignDataException e) {
                e.getMessage();
                if (++count == maxTries) throw new UnsignablePacketException("Could not sign.");
            }
            successful = true;
        }
        return signedMessage;
    }

    public SignedObject signByzMessageToServer(ByzantineMessage byzMessage, PrivateKey privKey) throws UnsignablePacketException{
        SignatureOperation signature = new SignatureOperation();

        int count = 0;
        int maxTries = 3; 
        boolean successful = false;
        SignedObject signedMessage = null;
        //try maximum 3 times to sign the packet to send to client
        while(!successful) {
            try{
               signedMessage = signature.signByzData(byzMessage, privKey);
            }
            catch (SignDataException e) {
                e.getMessage();
                if (++count == maxTries) throw new UnsignablePacketException("Could not sign.");
            }
            successful = true;
        }
        return signedMessage;
    }
}