package auxiliar;

/*
    Validates the parameters for each command

*/
public class VerifyParameters {
    
    private Message message = null;

    public VerifyParameters(Message message){
        this.message = message;
    }

    
    public void verifyPost() throws WrongParametersException{
        if( message.getPost()== null || !(message.getPost() instanceof String) || !(message.getAnnouncements() instanceof String[])){
            throw new WrongParametersException("Wrong post input");
        }
    }
    public void verifyPostGeneral() throws WrongParametersException{
        if( message.getPost()== null || !(message.getPost() instanceof String) || !(message.getAnnouncements() instanceof String[])){
            throw new WrongParametersException("Wrong postGeneral input");
        }
    }
    public void verifyRead() throws WrongParametersException{

        if(message.getNrOfPosts() < 0 ){
            throw new WrongParametersException("Wrong read input - no number of posts defined");
        } else if(message.getAuthor().equals("NO SUCH AUTHOR")){
            throw new WrongParametersException("Wrong read input - such author does not exist");
        }
    }
    public void verifyReadGeneral() throws WrongParametersException{
        if( message.getNrOfPosts() < 0 ){
            throw new WrongParametersException("Wrong readGeneral input");
        }
    }

}
