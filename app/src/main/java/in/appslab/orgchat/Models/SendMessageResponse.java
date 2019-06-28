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
    private Integer multicastId;
    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("failure")
    @Expose
    private Integer failure;
    @SerializedName("results")
    @Expose
    private List<FirebaseSendMessageResult> results = null;

    public Integer getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Integer multicastId) {
        this.multicastId = multicastId;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getFailure() {
        return failure;
    }

    public void setFailure(Integer failure) {
        this.failure = failure;
    }

    public List<FirebaseSendMessageResult> getResults() {
        return results;
    }

    public void setResults(List<FirebaseSendMessageResult> results) {
        this.results = results;
    }

}
