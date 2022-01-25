package auxiliar;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class ServerDataStructures implements Serializable{

    private static final long serialVersionUID = 1L;
    // create data structures
    private Announcements userAnnouncements = new Announcements();
    private Announcements generalAnnouncements = new Announcements();

    // counter total posts in general dashboard
    private AtomicInteger genPostCounter    = new AtomicInteger();
    private String address                  = "127.0.0.1";
    private TreeMap<String,AtomicInteger> privPostCounter = new TreeMap<String,AtomicInteger>();
    private TreeMap<String,Integer> wtsUsersPrivate       = new TreeMap<String,Integer>();
    private TreeMap<String,Integer> wtsUsersGeneral       = new TreeMap<String,Integer>();
    private TreeMap<String,Integer> ridUsersPrivate       = new TreeMap<String,Integer>();
    private TreeMap<String,Integer> ridUsersGeneral       = new TreeMap<String,Integer>();

    public ServerDataStructures(){

    }
    public Announcements getUserAnnouncements()                     {return this.userAnnouncements; }
    public Announcements getGeneralAnnouncements()                  {return this.generalAnnouncements; }
    public AtomicInteger getGenPostCounter()                        {return this.genPostCounter; }
    public TreeMap<String,AtomicInteger> getPrivPostCounter()       {return this.privPostCounter; }
    public String getAddress()                                      {return this.address;}
    public TreeMap<String,Integer> getWtsUsersPrivate()             {return this.wtsUsersPrivate;}
    public TreeMap<String,Integer> getWtsUsersGeneral()             {return this.wtsUsersGeneral;}
    public TreeMap<String,Integer> getRidUsersPrivate()             {return this.ridUsersPrivate;}
    public TreeMap<String,Integer> getRidUsersGeneral()             {return this.ridUsersGeneral;}


}