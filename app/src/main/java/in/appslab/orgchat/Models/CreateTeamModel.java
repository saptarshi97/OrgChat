package in.appslab.orgchat.Models;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Saptarshi on 6/19/2019.
 */
public class CreateTeamModel implements Serializable {
    private boolean isChecked=false;
    private String name;
    private String userID;
    @Nullable
    private String userToken=null;

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

    @Nullable
    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(@Nullable String userToken) {
        this.userToken = userToken;
    }
}
