import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import auxiliar.*;

public class Run {

    //vetor de Clientes
    private ArrayList<String> clientNames = new ArrayList<String>();
    
    //vetor de Passwords
    private ArrayList<String> clientPasswords = new ArrayList<String>();

    //state Servers
    private ServerState serverState = new ServerState();

    public Run() {
        //create directories for key stores
        File file = new File(System.getProperty("user.dir") + "/storekeys");
        if (!file.exists()) {
            if (!file.mkdir())
                System.out.println("Failed to create directory!");
        }

        clientNames.add("Alice");
        clientNames.add("Bob");
        clientNames.add("Charlie");
        clientNames.add("Server4000");
        clientNames.add("Server5000");
        clientNames.add("Server6000");
        clientNames.add("Server7000");

        clientPasswords.add("BH2020");
        clientPasswords.add("PC2020");
        clientPasswords.add("MP2020");
        clientPasswords.add("MrPeanutbutter2020");
        clientPasswords.add("Penguin2020");
        clientPasswords.add("Diane2020");
        clientPasswords.add("Pickels2020");

        int nrOfKeys = clientNames.size();
        
        String nameMapping = "";

        try {
            Hash hash = new Hash();
            
            for (int i = 0; i < nrOfKeys; i++) {
                StoreKey keystore = new StoreKey(clientNames.get(i));
                keystore.createStoreKey();
                
                KeyPairOperation pair = new KeyPairOperation();
                keystore.storeKey(pair.getKeyPair(), clientPasswords.get(i));

                // User's PublicKey -hash-> UserID
                // (MDR char set includes / that isnt allowed on a folder name. We replace it with -)
                String key = hash.createHash(pair.getPublicKey()).replaceAll("/", "-"); 
                nameMapping += clientNames.get(i) + ";" + key + "/";

                FileEncryption encrypt = new FileEncryption(keystore.getOwner(),  clientPasswords.get(i));
                encrypt.deletePrivate(); //deletes  _private.txt
            }
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }


        FileOperation fo = new FileOperation();
        fo.generalFileCreate("./", "server_ports.txt");
        fo.cleanUpServerStates(); 

        String filename = fo.generalFileCreate(System.getProperty("user.dir") + "/", "nameMapping.txt");
        fo.generalWriteFile(filename, nameMapping);
    }

   

    public static void main (final String args[]) {
        final Run run = new Run();
    }
}