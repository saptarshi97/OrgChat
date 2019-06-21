package in.appslab.orgchat.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Saptarshi on 6/21/2019.
 */
public class RemoveMemberModel {

    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("registration_tokens")
    @Expose
    private List<String> registrationTokens = null;

    public RemoveMemberModel(){
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getRegistrationTokens() {
        return registrationTokens;
    }

    public void setRegistrationTokens(List<String> registrationTokens) {
        this.registrationTokens = registrationTokens;
    }

}