
package auxiliar;

public class ServerRegistry {

    public ServerRegistry(){
    }
    
    public void registerServer(String address, int port){
        System.out.println("\n----- server registry -----\n");
        System.out.println("New server registered:");
        System.out.println("address: " + address);
        System.out.println("port: " + port);        
        
        FileOperation fo = new FileOperation();
        fo.writePorts(Integer.toString(port));

    }
}