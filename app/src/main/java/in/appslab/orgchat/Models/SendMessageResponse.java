package in.appslab.orgchat.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Saptarshi on 6/28/2019.
 */
    public class SendMessageResponse {

    @SerializedName("multicast_id")
    @Expose
    private String multicastId;
    @SerializedName("success")
    @Expose
    private String success;
    @SerializedName("failure")
    @Expose
    private String failure;
    @SerializedName("results")
    @Expose
    private List<FirebaseSendMessageResult> results = null;

    public String getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(String multicastId) {
        this.multicastId = multicastId;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public List<FirebaseSendMessageResult> getResults() {
        return results;
    }

    public void setResults(List<FirebaseSendMessageResult> results) {
        this.results = results;
    }

}
