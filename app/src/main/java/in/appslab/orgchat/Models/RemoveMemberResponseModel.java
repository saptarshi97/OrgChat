package in.appslab.orgchat.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Saptarshi on 6/21/2019.
 */
public class RemoveMemberResponseModel {
    @SerializedName("results")
    @Expose
    private List<RemoveMemberResultModel> results = null;

    public List<RemoveMemberResultModel> getResults() {
        return results;
    }

    public void setResults(List<RemoveMemberResultModel> results) {
        this.results = results;
    }

}
