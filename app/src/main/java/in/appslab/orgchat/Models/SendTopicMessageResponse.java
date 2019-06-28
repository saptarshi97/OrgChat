package in.appslab.orgchat.Models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Saptarshi on 6/28/2019.
 */
public class SendTopicMessageResponse {

    @SerializedName("message_id")
    @Expose
    @Nullable
    private String messageId;
    @SerializedName("error")
    @Expose
    @Nullable
    private String error;

    @Nullable
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@Nullable String messageId) {
        this.messageId = messageId;
    }

    @Nullable
    public String getError() {
        return error;
    }

    public void setError(@Nullable String error) {
        this.error = error;
    }
}
