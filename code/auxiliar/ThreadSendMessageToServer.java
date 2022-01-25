package auxiliar;
import java.io.*;

public class ThreadSendMessageToServer extends Thread{
    private ByzantinePacket byzPacket;
    private ObjectOutputStream out;

    public ThreadSendMessageToServer(ByzantinePacket byzPacket, ObjectOutputStream out){
        this.byzPacket = byzPacket;
        this.out = out;
    }


    @Override
    public void run() 
    { 
        try{ 
            out.writeObject(byzPacket);
        } 
        catch (IOException e) {      
                  
            e.printStackTrace(); } 
    } 

}