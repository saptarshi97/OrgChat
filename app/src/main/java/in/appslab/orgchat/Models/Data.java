package in.appslab.orgchat.Models;

/**
 * Created by Saptarshi on 6/3/2019.
 */
public class Data {
    private String message;
    private String time;
    private String senderID;
    private String topicName;
    private int isTopic;

    public Data(String message,String time, String senderID, String topicName, int isTopic){
        this.message=message;
        this.time=time;
        this.senderID=senderID;
        this.topicName=topicName;
        this.isTopic=isTopic;
    }
}
