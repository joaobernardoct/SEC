package auxiliar;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.*;
import javax.crypto.spec.*;

public class FileDecryption {
    private String owner = "";

    public FileDecryption(String ownerName, String password) throws BadPaddingException{
        this.owner = ownerName;


        String path = System.getProperty("user.dir") + "/storekeys/keyStore" + ownerName + "/";
        String decryptFile = ownerName + "_private.des";
        String decryptFilePath = path + decryptFile;
        String outputFileName = ownerName + "_decrypted.txt";
        String outputFilePath = path + outputFileName;

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

        FileInputStream fis                 = null;
        byte[] salt                         = null;
        PBEParameterSpec pbeParameterSpec   = null;
        Cipher cipher                       = null;
        FileOutputStream fos                = null;
        byte[] in                           = null;
        try {
            fis = new FileInputStream(decryptFilePath);
            salt = new byte[8];
            fis.read(salt);
            pbeParameterSpec = new PBEParameterSpec(salt, 100);
            cipher = Cipher.getInstance("PBEWithMD5AndTripleDES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParameterSpec);
            fos = new FileOutputStream(outputFilePath);
            in = new byte[64];

            int read;
            while ((read = fis.read(in)) != -1) {
                byte[] output = cipher.update(in, 0, read);
                if (output != null)
                    fos.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                fos.write(output);

            fis.close();
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } 
		

		
    }
    public void deleteDecrypted(){
        String path= System.getProperty("user.dir") + "/storekeys/keyStore" + owner + "/" ;
        String fileDecrypted =  owner + "_decrypted.txt" ;
        String deleteMe = path + fileDecrypted;
        File file = new File(deleteMe); 
        file.delete();
    }
}