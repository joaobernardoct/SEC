
package auxiliar;

/**
 * Exception for when the user is already registered in the system
 */
public class UserAlreadyRegisteredException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 9164025384968553747L;

    public UserAlreadyRegisteredException(String errorMessage) {
        super(errorMessage);
    }

}