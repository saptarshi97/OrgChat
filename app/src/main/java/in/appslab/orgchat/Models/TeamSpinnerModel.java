package in.appslab.orgchat.Models;

/**
 * Created by Saptarshi on 6/12/2019.
 */
public class TeamSpinnerModel {
    private String teamName;
    private String teamID;

    public TeamSpinnerModel(String teamName, String teamID){
        this.teamID=teamID;
        this.teamName=teamName;
    }

    public String getTeamID() {
        return teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    @Override
    public String toString(){
        return this.teamName;
    }
}
