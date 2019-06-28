package in.appslab.orgchat.Network;

import in.appslab.orgchat.Models.Message;
import in.appslab.orgchat.Models.SendMessageResponse;
import in.appslab.orgchat.Models.SendTopicMessageResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Saptarshi on 6/17/2019.
 */
public class APIClient {
    private static Retrofit retrofit = null;

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    public static APIInterface getAPIInterface(){

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(APIInterface.class);
    }

    public interface APIInterface{
        @POST("/fcm/send")
        Call<SendMessageResponse> sendMessage(@Header("Authorization") String legacyServerKey, @Body Message message);

        @POST("/fcm/send")
        Call<SendTopicMessageResponse> sendTopicMessage(@Header("Authorization") String legacyServerKey, @Body Message message);
    }
}