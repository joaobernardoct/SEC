package auxiliar;
import java.util.*;

import java.security.*;


public class ContactClient  {

    public void ContactClient(){
        //
    }
    
    public Message createMsg(String message, PublicKey pub, Nonce myNonce, String author,int serverPort){
        Message msg = new Message(message, pub, myNonce, author, serverPort);
        return msg;
    }

    public SignedObject signMessageToClient(Message message, PrivateKey privKey) throws UnsignablePacketException{
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

    public SignedObject signByzMessageToClient(ByzantineMessage byzMessage, PrivateKey privKey) throws UnsignablePacketException{
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

    public boolean verifyMessageFromClient(Packet packet) {
        // Validate signature:
        SignatureOperation sigOp = new SignatureOperation();
        boolean isFromClient = sigOp.verifySignature(packet.getSignature(), packet.getPublicKey());
     
        return isFromClient;
    }

    public boolean verifyByzMessageFromClient(ByzantinePacket byzPacket) {
        // Validate signature:
        SignatureOperation sigOp = new SignatureOperation();
        boolean isFromClient = sigOp.verifySignature(byzPacket.getSignature(), byzPacket.getPublicKey());
     
        return isFromClient;
    }

}