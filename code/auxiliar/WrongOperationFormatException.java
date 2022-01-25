package auxiliar;
public class WrongOperationFormatException extends Exception{
    /**
	 *Thrown when wrong input format is received by the Server
	 */
	private static final long serialVersionUID = -9092114700139687643L;
	
    public WrongOperationFormatException(String errorMessage) {
        super(errorMessage);
    }

}