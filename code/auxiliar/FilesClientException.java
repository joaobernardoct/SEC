
package auxiliar;
public class FilesClientException extends Exception{
    /**
     * thrown by store key to client
     */
    private static final long serialVersionUID = -3230631519326237797L;

    public FilesClientException(String errorMessage) {
        super(errorMessage);
    }
}
