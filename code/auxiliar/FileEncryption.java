package auxiliar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class FileEncryption {
    String owner = "";
    public FileEncryption(String ownerName, String password) {
        this.owner = ownerName;
        FileInputStream inFile = null;
        FileOutputStream outFile = null;
        String path = System.getProperty("user.dir") + "/storekeys/keyStore" + ownerName + "/";
        String privFilename = ownerName + "_private.txt";
        String outputFileName = ownerName + "_private.des";
        String encryptMe = path + privFilename;
        String outputFilePath = path + outputFileName;
        try {
            inFile = new FileInputStream(encryptMe);
            outFile = new FileOutputStream(outputFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());

        SecretKeyFactory secretKeyFactory = null;
        SecretKey secretKey = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES");
            secretKey = secretKeyFactory.generateSecret(pbeKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        byte[] salt = new byte[8];
        Random random = new Random();
        random.nextBytes(salt);

        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("PBEWithMD5AndTripleDES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParameterSpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        byte[] input = new byte[64];
        int bytesRead;
        try {
            outFile.write(salt);
            while ((bytesRead = inFile.read(input)) != -1) {
                byte[] output = cipher.update(input, 0, bytesRead);
                if (output != null)
                    outFile.write(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

  
        byte[] output = null;
        try {
            output = cipher.doFinal();
            if (output != null)
                outFile.write(output);
            inFile.close();
            outFile.flush();
            outFile.close();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
          
        
		
    }

    //called after encrypting private files
    //deletes _private.txt to secure privacy of PrivateKey
    public void deletePrivate(){
        String path= System.getProperty("user.dir") + "/storekeys/keyStore" + owner + "/" ;
        String fileDecrypted =  owner + "_private.txt" ;
        String deleteMe = path + fileDecrypted;
        File file = new File(deleteMe); 
        file.delete();
    }

}