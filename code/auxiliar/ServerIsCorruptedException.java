package auxiliar;

public class ServerIsCorruptedException extends Exception{
  
    private static final long serialVersionUID = 1L;

    public ServerIsCorruptedException(String message) {
        super(message);
    }
    
}
