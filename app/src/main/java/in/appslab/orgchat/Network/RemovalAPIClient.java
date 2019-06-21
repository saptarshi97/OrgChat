package in.appslab.orgchat.Network;

import in.appslab.orgchat.Models.RemoveMemberModel;
import in.appslab.orgchat.Models.RemoveMemberResponseModel;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Saptarshi on 6/21/2019.
 */
public class RemovalAPIClient {
    private static Retrofit retrofit = null;

    private static final String BASE_URL = "https://iid.googleapis.com/iid/";

    public static RemoveAPIInterface getAPIInterface(){

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RemoveAPIInterface.class);
    }

    public interface RemoveAPIInterface{
        @POST("v1:batchRemove")
        Call<RemoveMemberResponseModel> removeMember(@Header("Authorization") String legacyServerKey, @Body RemoveMemberModel removeMemberModel);
    }
}
