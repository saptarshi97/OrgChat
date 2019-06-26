package in.appslab.orgchat.Models;

/**
 * Created by Saptarshi on 6/3/2019.
 */
public class Message {
    private String to;
    private Data data;
    private String message_id; // Success response for topic-based messages sent
    private String error; // Error response for topic-based messages sent
    private int success; // Success response for token-based messages sent
    private int failure; // Error response for token-based messages sent

    public Message(){}

    public Message(String to,Data data){
        this.to=to;
        this.data=data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }
}
