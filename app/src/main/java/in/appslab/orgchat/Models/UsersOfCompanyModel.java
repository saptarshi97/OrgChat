package in.appslab.orgchat.Models;

/**
 * Created by Saptarshi on 6/12/2019.
 */
public class UsersOfCompanyModel {
    private String userID;
    private String name;
    private String registrationToken;
    private String topicName;
    private int isTopic;
    private boolean userIsAdmin=false;

    public UsersOfCompanyModel(){

    }

    public UsersOfCompanyModel(String userID,String name, String registrationToken) {
        this.userID=userID;
        this.name = name;
        this.registrationToken = registrationToken;
    }

    public boolean isUserIsAdmin() {
        return userIsAdmin;
    }

    public void setUserIsAdmin(boolean userIsAdmin) {
        this.userIsAdmin = userIsAdmin;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getIsTopic() {
        return isTopic;
    }

    public void setIsTopic(int isTopic) {
        this.isTopic = isTopic;
    }

    @Override
    public int hashCode() {
        return 31*userID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  UsersOfCompanyModel))
            return false;
        UsersOfCompanyModel uObj=(UsersOfCompanyModel)obj;
        return userID.equals(uObj.userID) && name.equals(uObj.name);
    }
}