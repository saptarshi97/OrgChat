package in.appslab.orgchat.Models;

import java.io.Serializable;

/**
 * Created by Saptarshi on 6/19/2019.
 */
public class CreateTeamModel implements Serializable {
    private boolean isChecked=false;
    private String name;
    private String userID;

    public CreateTeamModel(String name, String userID) {
        this.name = name;
        this.userID = userID;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public String getUserID() {
        return userID;
    }
}
