package auxiliar;

import java.io.Serializable;

public class ByzantineMessage implements Serializable {

    private static final int serverPort = 4;
    private static final int highestPort = 7000;
    private static final long serialVersionUID = 1L;

    //We use -1 for omission
    private int rid;
    private int wts;
    private Packet value;
    private int rank;

    public ByzantineMessage(int rid, int wts, Packet value){
        this.rid = rid;
        this.wts = wts;
        this.value = value;
    }

    public int getRID()                 { return this.rid;   }
    public int getWTS()                 { return this.wts;   }
    public Packet getPacket()           { return this.value; }

    public void setRank(int rank)       { this.rank = rank;  }
    public int getRank()                { return this.rank;  }
}