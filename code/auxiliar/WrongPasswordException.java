package auxiliar;

public class WrongPasswordException extends Exception{
    /**
     * thrown when wrong password is given to decrypt private key file
     */
    private static final long serialVersionUID = -4578635830895210447L;

    public WrongPasswordException(String messageError) {
        super(messageError);
    }

}
