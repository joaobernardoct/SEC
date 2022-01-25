package auxiliar;
import java.io.File;
import java.io.*;

public class FileOperation{

    public FileOperation(){
        //
    }


    //--- Methods to Create File ------------------------------------

    //method used to create a file for a post
    public String createFilePost(int postNumber, boolean general, String appendBeforeTxt, int port, String key){
        String filename = "";
        String multiplier = "";
        
        if(postNumber < 10)
            multiplier = "00";
        else if(postNumber < 100)
            multiplier = "0";

        String folder = "general" + port + "/";
        if(!general) //private announcement
            folder = "posts" + port + "/" + key + "/";
        
        filename = folder + "post" + multiplier + postNumber + appendBeforeTxt + ".txt";        

        try {
            File myObj = new File(filename);
            if (!myObj.createNewFile())
                System.out.println("Error: File " + myObj.getName() + " already exists.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return filename;
    }


    //method called to create a file in a specified path with a specified filename
    public String generalFileCreate(String path, String name){
        String filename = path + name;
        try {
            File myObj = new File(filename);
            if(myObj.exists())
                myObj.delete();
            myObj.createNewFile();
               
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return filename;

        //example: path = general/
        //          name = exemplo
        //    creates a file exemplo.txt in a directory called general
    }
    public void createServerState(String name){
        
        try {
            File myObj = new File(name);
            if(myObj.exists())
                myObj.delete();
            myObj.getParentFile().mkdirs();
            myObj.createNewFile();
               
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
  

        //example: path = general/
        //          name = exemplo
        //    creates a file exemplo.txt in a directory called general
    }


    //--- Methods to Write File ------------------------------------

    public String writePost(String filename, String message, String[] references, String publicKey){
        String statusMessage = "";

        try {
            // the char | will be interpreted by the client as a \n
            FileWriter myWriter = new FileWriter(filename);
            int refSize = references.length;
            myWriter.write("Announcements:|  ");

            if(refSize != 0){
                for (int i = 0; i < refSize; i++){
                    if(i == refSize - 1 )
                        myWriter.write(references[i] + "|");
                    else
                        myWriter.write(references[i] + ",");
                }
            } else {
                myWriter.write("-|"); // no announcement tags notation
            }
            
            myWriter.write("Author:|  ");
            myWriter.write(publicKey + "|");

            myWriter.write("Post:|  ");
            myWriter.write(message);
            myWriter.close();
            statusMessage = "User post successfully saved to file: " + filename + "\n";
            
            } catch (IOException e) {
                statusMessage = "An error occurred.";
                e.printStackTrace();
            }

            return statusMessage;
    }


    //general method to write to a particular file
    public String generalWriteFile(String filename, String toWrite){
        String statusMessage = "";
        try {
            // the char | will be interpreted by the client as a \n
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(toWrite);
            myWriter.close();
            statusMessage = filename + " was successfully written. \n";
        } catch (IOException e) {
            statusMessage = "An error occurred when writing to " + filename + ".\n";
            e.printStackTrace();
        }
            return statusMessage;
    }
    public void writePorts( String portNumber){
        try{
            FileWriter fileWriter = new FileWriter("./server_ports.txt", true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(portNumber);  //New line
            printWriter.close();
        }catch(IOException e){e.printStackTrace();}

    }


    //--- Rename File ----------------------------------------------        
    public boolean renameFile(String oldName, String newName){
        boolean success = false;
        File file = new File(oldName);
        File file2 = new File(newName);

        if (file2.exists()){
            System.out.println("The filename to which you're trying to rename your file is already in use\n");
        }
        else {
            // Rename file (or directory)
            success = file.renameTo(file2);
        }
        return success;
    }


    //--- Prevent Corrupted Files-----------------------------------        
    public String safelySavePostFile(int postNumber, boolean general,  String message, String[] references, String key, int port){        
        String statusMessage = "";

        //1) create and write to foo.data
        String filename1 = createFilePost(postNumber, general, "", port, key);
        String msg1 = writePost(filename1, message, references, key);
        if (msg1 == "An error occurred."){
            statusMessage += "An error occurred while safely storing a post [1]\n";
        }

        //2) create and write to foo.data.new
        String filename2 = createFilePost(postNumber, general, "-NEW", port, key);
        String msg2 = writePost(filename2, message, references, key);
        if (msg2 == "An error occurred."){
            statusMessage += "An error occurred while safely storing a post [2]\n";
        }

        //3) rename foo.data to foo.data.old
        String new_filename1 = filename1.replaceAll(".txt", "") + "-OLD.txt";
        renameFile(filename1, new_filename1);

        //4) rename foo.data.new to foo.data
        String new_filename2 = filename2.replaceAll("-NEW.txt", "") + ".txt";
        renameFile(filename2, new_filename2);
        
        //5) delete foo.data.old
        try {         
            File f = new File(new_filename1); //file to be delete  
            if(!f.delete()) { //returns Boolean value  
                statusMessage += "Failed to delete file while safely storing a post\n";
            }
        } catch(Exception e) { 
            e.printStackTrace();  
        }

        if (statusMessage == ""){
            statusMessage = "The post was successfully saved";
        }
        return statusMessage;
    }



    // ----------------------- IMPORTANT ----------------------------
    //| THE READER WILL HAVE TO USE THE FOLLOWING INSTRUCTIONS:      |
    //|  * if post.txt     and post-NEW.txt exist, load post.txt     |
    //|  * if post-OLD.txt and post-NEW.txt exist, load post-OLD.txt |
    //|  * if post.txt     and post-OLD.txt exist, load post.txt     |
    // --------------------------------------------------------------




    //deletes files in /servers
    public void cleanUpServerStates(){
        File directory = new File("servers/");
        if(directory.exists()){
            File[] files = directory.listFiles();
            for (File file : files)
                file.delete();
        }
       
    }

}
