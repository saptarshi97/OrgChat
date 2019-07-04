package in.appslab.orgchat.Models;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Saptarshi on 6/3/2019.
 */
public class ChatModel extends RealmObject implements Serializable {
    private String messageId;
    @Nullable
    private String quotedMessageId;
    @Nullable
    private String chatMessage;
    private String timestamp;
    private String sender;
    private String receiver;
    private String topicName;
    private int isTopic;
    @Nullable
    private String downloadUri;

    public ChatModel(){

    }

    public ChatModel(@Nullable String chatMessage, String timestamp, String sender, String receiver, String topicName, int isTopic) {
        this.chatMessage = chatMessage;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.topicName = topicName;
        this.isTopic = isTopic;
    }

    @Nullable
    public String getQuotedMessageId() {
        return quotedMessageId;
    }

    public void setQuotedMessageId(@Nullable String quotedMessageId) {
        this.quotedMessageId = quotedMessageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Nullable
    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(@Nullable String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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

    @Nullable
    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(@Nullable String downloadUri) {
        this.downloadUri = downloadUri;
    }
}
