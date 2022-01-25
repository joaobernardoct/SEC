package auxiliar;
import java.util.concurrent.*;
import java.io.Serializable;
import java.util.ArrayList;


/*Used to register posts made in the general dashboard*/
public class Announcements implements Serializable{
   
    private static final long serialVersionUID = 2171073319522213343L;
    private ConcurrentHashMap<String, ArrayList<Integer>> announc;

    public Announcements(){
        announc = new ConcurrentHashMap<String, ArrayList<Integer>> ();
    }

    public void registerNewUser(String publicKey){
        announc.put(publicKey, new ArrayList<Integer>());
    }

    //Used when user makes a new announcement
    public void registerNewPost(String publicKey,int numPost){
        ArrayList<Integer> postsByUser = announc.get(publicKey);
        postsByUser.add(numPost);
    }

    //Number of announcements made by a user
    public Integer getNumberAnnouncementsUser(String publicKey){
        ArrayList<Integer> postsByUser = announc.get(publicKey);
        return postsByUser.size();
    }

    //user exists if user in announc
    public boolean userExists(String publicKey){
        return announc.containsKey(publicKey);
    }

}