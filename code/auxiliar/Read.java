package auxiliar;

import java.io.*;
import java.util.ArrayList;

//Takes care of the read service: read and readGeneral
//read: publicKey has value
//readGeneral: doesn't have publicKey
public class Read {

    private int numberAnnouncementsToRead;
    private boolean gen;

    public Read(int num, boolean gen) {
        this.numberAnnouncementsToRead = num;
        this.gen = gen;
    }

    public Read() {
    }

    public int getNumberAnnouncementsToRead() {
        return numberAnnouncementsToRead;
    }

    public boolean getGen() {
        return gen;
    }

    // ----------------------- IMPORTANT ----------------------------
    // | THE READER WILL HAVE TO USE THE FOLLOWING INSTRUCTIONS: |
    // | * if post.txt and post-NEW.txt exist, load post.txt |
    // | * if post-OLD.txt and post-NEW.txt exist, load post-OLD.txt |
    // | * if post.txt and post-OLD.txt exist, load post.txt |
    // --------------------------------------------------------------
    public String readOperation(int counterPosts, int port, String author) throws Exception {
        String statusMessage = "";
        String records = "";

        String type = "/general" + port;
        if (!getGen())
            type = "/posts" + port + "/" + author;

        if (getNumberAnnouncementsToRead() != 0 && counterPosts < getNumberAnnouncementsToRead()) { // client wants too many posts
            statusMessage = "The general dashboard doesn't have that many posts";
            if (!getGen())
                statusMessage = "The dashboard doesn't have that many posts";
            return statusMessage; // TODO exception
        } else {

            String multiplier = "";

            int stop = counterPosts - getNumberAnnouncementsToRead();
            int p = counterPosts;
            if (getNumberAnnouncementsToRead() == 0) {
                stop = 0;
            }

            String path;

            while (p > stop) {
                if (p < 10)
                    multiplier = "00";
                else if (p < 100)
                    multiplier = "0";

                path = System.getProperty("user.dir") + type + "/";

                // Get the files
                File f1 = new File(path + "post" + multiplier + p + ".txt");
                File f2 = new File(path + "post" + multiplier + p + "-NEW.txt");
                File f3 = new File(path + "post" + multiplier + p + "-OLD.txt");

                if (f1.exists() && f2.exists()) {
                    records += getPostContent(type, path + "post" + multiplier + p + ".txt"); // load f1

                } else if (f3.exists() && f2.exists()) {
                    records += getPostContent(type, path + "post" + multiplier + p + "-OLD.txt"); // load f3
                } else if (f1.exists() && f3.exists()) {
                    records += getPostContent(type, path + "post" + multiplier + p + ".txt"); // load f1
                } else {
                    records += getPostContent(type, path + "post" + multiplier + p + ".txt"); // reads normal file
                }

                p--;
            }

        }
        statusMessage = records;
        return statusMessage;

    }

    public String getFilename(int counter) {
        String multiplier = "";
        if (counter < 10)
            multiplier = "00";
        else if (counter < 100)
            multiplier = "0";

        return "post" + multiplier + counter + ".txt";
    }

    // example
    // path = "./general/"
    // filename = "user.txt"
    public String readFileGeneral(String path, String filename) throws FileNotFoundException, IOException {
        String records = "";

        BufferedReader reader;

        reader = new BufferedReader(new FileReader(path + filename));
        String line;
        while ((line = reader.readLine()) != null) {
            records += line;
        }
        reader.close();     
        
        return records;

    }
    public ArrayList<Integer> readPorts(String path, String filename) throws FileNotFoundException, IOException {
        ArrayList<Integer> records = new ArrayList<Integer>(); 
       
        BufferedReader reader = new BufferedReader(new FileReader(path + filename));
        String line;
        while ((line = reader.readLine()) != null) {
            records.add(Integer.parseInt(line));
        }
        reader.close();
        
        return records;

    }


    //-----------------------| Aux Functions |-----------------------------
    public String getPostContent(String type, String file){
        String records = "@";
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    records += line;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            records += "\n";
        return records;
    }

    public String prepareForDisplay(String unpreparedString){
        String sendMe = unpreparedString.replace('|', '\n');
        return sendMe;
    }

}
