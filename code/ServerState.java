
import java.io.*;

import auxiliar.FileOperation;
import auxiliar.ServerDataStructures;

/*
    This class handles the serialization of the server
*/
public class ServerState {
    public ServerState(){
       
    }

    //server's state is saved upon calling:
    //          * register()
    //          * post()
    //          * postGeneral()
    public void saveServerState(ServerDataStructures serverData, int portNumber){
        try {
            FileOperation fo = new FileOperation();
            String serverFilename = "servers/server" + portNumber  + ".ser";
            fo.createServerState(serverFilename);
            FileOutputStream fileOut = new FileOutputStream(serverFilename);
            ObjectOutputStream out    = new ObjectOutputStream(fileOut);
            out.writeObject(serverData);
            out.close();
            fileOut.close();
            System.out.println("Server's state saved");
         } catch (IOException i) {
            i.printStackTrace();
         }
    }

    public ServerDataStructures loadServerState(int portNumber){
        ServerDataStructures data = null;
        try {
            String serverFilename = "servers/server" + portNumber  + ".ser";
            File f = new File(serverFilename);
            if (f.exists()){
                FileInputStream fileIn = new FileInputStream(serverFilename);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                data = (ServerDataStructures) in.readObject();
                in.close();
                fileIn.close();
            }
            
            
         } catch (IOException i) {
            i.printStackTrace();
         } catch (ClassNotFoundException c) {
            return null;
         }
         return data;
         
    }
    

}