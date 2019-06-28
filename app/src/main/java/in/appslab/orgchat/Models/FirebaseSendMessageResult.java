package in.appslab.orgchat.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Saptarshi on 6/28/2019.
 */
public class FirebaseSendMessageResult {

    @SerializedName("message_id")
    @Expose
    private String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}
