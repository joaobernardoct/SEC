package auxiliar;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;

public class Hash {

    public Hash(){
        //
    }

    public String createHash(PublicKey pubKey){
        String output = "";
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(pubKey.getEncoded());
            byte[] hash = sha.digest();
            output = new String(Base64.getEncoder().encode(hash), "UTF-8");
        }
        catch (NoSuchAlgorithmException e)     { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        return output;
    }

    public String createHashFromString(String text){
        String output = "";
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(text.getBytes());
            byte[] hash = sha.digest();
            output = new String(Base64.getEncoder().encode(hash), "UTF-8");
        }
        catch (NoSuchAlgorithmException e)     { e.printStackTrace(); }
        catch (UnsupportedEncodingException e) { e.printStackTrace(); }

        return output;
    }

}