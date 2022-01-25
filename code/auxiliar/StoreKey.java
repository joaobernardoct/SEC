package auxiliar;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import javax.crypto.BadPaddingException;




public class StoreKey {

    private String ownerName; // Alice
    private String pathToKeyStore; // ./storekeys/KeyStoreAlice/
    private String privFilename; // alice_private.txt
    private String pubFilename; // alice_public.txt

    public StoreKey() {
        ownerName = "";
        pathToKeyStore = "";
        privFilename = "";
        pubFilename = "";
    }

    public StoreKey(String owner) {
        ownerName = owner;
        pathToKeyStore = System.getProperty("user.dir") + "/storekeys/keyStore" + ownerName + "/";
        privFilename = owner + "_private.txt";
        pubFilename = owner + "_public.txt";
    }
    public String getOwner(){
        return this.ownerName;
    }

    public void createStoreKey() {
        File file = new File(pathToKeyStore);
        if (!file.exists()) {
            if (!file.mkdir())
                System.out.println("Failed to create directory!");
        }
    }

    // TODO ---> encriptar ficheiro txt da passe privada
    public void storeKey(KeyPair pair, String password) {
        try { // for private key
            FileOperation privCreateFile = new FileOperation();
            privCreateFile.generalFileCreate(pathToKeyStore, privFilename); // create file

            byte[] privKeyBytes = pair.getPrivate().getEncoded();
            String privKeyString = new String(Base64.getEncoder().encode(privKeyBytes), "UTF-8");

            FileOperation privWriteFile = new FileOperation();
            privWriteFile.generalWriteFile(pathToKeyStore + privFilename, privKeyString); // write file

            // txt.encript(password)

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            // for public key
            FileOperation pubCreateFile = new FileOperation();
            pubCreateFile.generalFileCreate(pathToKeyStore, pubFilename); // create file

            byte[] pubKeyBytes = pair.getPublic().getEncoded();
            String pubKeyString = new String(Base64.getEncoder().encode(pubKeyBytes), "UTF-8");

            FileOperation pubWriteFile = new FileOperation();
            pubWriteFile.generalWriteFile(pathToKeyStore + pubFilename, pubKeyString); // write file
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // TODO ---> desencriptar ficheiro txt da passe privada
    public PrivateKey privKeyRetrieve(String password) throws WrongPasswordException{
        PrivateKey privateKey = null;
        try {
            FileDecryption fileDecryption = new FileDecryption(this.ownerName, password);
           
            String path= System.getProperty("user.dir") + "/storekeys/keyStore" + ownerName + "/" ;
            String fileDecrypted =  ownerName + "_decrypted.txt" ;
            Read readFile = new Read();
            String privKeyString = readFile.readFileGeneral(path, fileDecrypted);
            byte[] privKeyBytes = Base64.getDecoder().decode(privKeyString.getBytes());

            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
            fileDecryption.deleteDecrypted(); // deletes _decrypted.txt
        }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        catch (InvalidKeySpecException e)  { e.printStackTrace(); }
        catch(FileNotFoundException e)     { e.printStackTrace(); }
        catch(IOException e)               { e.printStackTrace(); }
        catch(BadPaddingException e) {throw new WrongPasswordException("Incorrect password!");}
        return privateKey;
    }

    public PublicKey pubKeyRetrieve() throws FilesClientException {
        PublicKey publicKey = null;
        try {
            Read readFile = new Read();
            String pubKeyString;
            pubKeyString = readFile.readFileGeneral(pathToKeyStore, pubFilename); //throws IOException
            byte[] pubKeyBytes = Base64.getDecoder().decode(pubKeyString.getBytes());
        
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubKeyBytes));
        }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        catch (InvalidKeySpecException e)  { e.printStackTrace(); }
        catch (FileNotFoundException e){ throw new FilesClientException("ERROR");}
        catch (IOException e) {throw new FilesClientException("ERROR");}

        return publicKey;
    }

    public PublicKey pubKeyDecoder(String encodedKey){ //key.txt --> PublicKey
        PublicKey publicKey = null;
        try {
            byte[] pubKeyBytes = Base64.getDecoder().decode(encodedKey.getBytes());
        
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubKeyBytes));
        }
        catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        catch (InvalidKeySpecException e)  { e.printStackTrace(); }
        return publicKey;
    }
}