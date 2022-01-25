package auxiliar;

public class UnsignablePacketException extends Exception{

    /**
     * Thrown when SignatureOperation isn't able to sign their packet
     */
    private static final long serialVersionUID = 4186457917571862008L;

    public UnsignablePacketException (String errorMessage){
        super(errorMessage);
    }

}
