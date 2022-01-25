package auxiliar;
public class ParseArguments {

    private final String[] arguments;

    public ParseArguments(final String[] tokens) {
        arguments = tokens;
        
    }

    public String[] getAllArguments() {
        return arguments;
    }

    // returns specific argument in vector
    public String getIndexArgument(final int num) {
        return arguments[num];
    }

    public int sizeArguments() {
        return arguments.length;
    }

    // used to verify function call format
    public void isThisSizeArguments(final int num) throws WrongOperationFormatException {
        if (arguments.length != num) {
            throw new WrongOperationFormatException("Wrong Operation Format. The function only has "
                    + Integer.toString(num - 1) + " parameters.");
        }
    }

    // used to check the number of arguments called (by post and postGeneral)
    public boolean checkSizeArguments(final int num) {
        return arguments.length == num;
    }
    


 }