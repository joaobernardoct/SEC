package auxiliar;
import java.util.*;

public class PasswordServers{
    
    private TreeMap<Integer, String> serverPasswords = new TreeMap<Integer, String>();

    public PasswordServers(){
        serverPasswords.put(4000,"MrPeanutbutter2020");
        serverPasswords.put(5000, "Penguin2020");
        serverPasswords.put(6000, "Diane2020");
        serverPasswords.put(7000, "Pickels2020");

    }
    public String getServerPassword(int port){
        return serverPasswords.get(port);
    }


}