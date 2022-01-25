package auxiliar;

public class UserDoesntExistException extends Exception {

    /**
     * Exception for when the user isn't registered in the system
     */
    public UserDoesntExistException(String errorMessage) {
        super(errorMessage);
    }
    
    private static final long serialVersionUID = 1L;

}