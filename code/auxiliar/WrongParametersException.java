package auxiliar;

public class WrongParametersException extends Exception{

    /**
     * thrown when wrong parameters are sent to server
     */
    private static final long serialVersionUID = -3724305127782850632L;

    public WrongParametersException(String errorMessage) {
        super(errorMessage);
    }

}
