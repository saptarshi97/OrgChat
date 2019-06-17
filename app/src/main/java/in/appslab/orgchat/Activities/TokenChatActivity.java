package in.appslab.orgchat.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.appslab.orgchat.Adapters.ChatAdapter;
import in.appslab.orgchat.Models.ChatModel;
import in.appslab.orgchat.Models.Data;
import in.appslab.orgchat.Models.Message;
import in.appslab.orgchat.Network.APIClient;
import in.appslab.orgchat.R;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenChatActivity extends AppCompatActivity {
    private RecyclerView rv;
    private EditText inputEditText;
    private ImageView send;
    ChatAdapter adapter;
    private List<ChatModel> chatModelList = new ArrayList<>();
    BroadcastReceiver receiver;
    private static final String TAG = "TokenChatFragment";
    public static String PREF_NAME = "shared values";
    private String legacyServerKey = "key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";
    private String testDestinationToken;
    private String nameOfUser;
    private String selfID;
    private String destinationUserID;
    private Realm mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_chat);
        mDatabase = Realm.getDefaultInstance();
        Bundle mBundle=getIntent().getExtras();
        testDestinationToken = mBundle.getString("registrationToken");
        nameOfUser = mBundle.getString("name");
        destinationUserID = mBundle.getString("userID");
        Log.d(TAG, "onCreate: destinationUserID: "+destinationUserID);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        selfID = prefs.getString("username", "");
        initViews();
        Log.d(TAG, "onCreate: self ID " + selfID);
        Log.d(TAG, "onCreate: destination token: " + testDestinationToken);
        initReceiver();
        init();
    }

    private void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getIntExtra("newDataAvailable", 0) == 1) {
                    try {
                        //Call and a method and pass the messages+details to add to a list here
                        Log.d(TAG, "onReceive: data received");

                        List<ChatModel> results1 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                                .equalTo("isTopic", 0)
                                .equalTo("sender", destinationUserID)
                                .equalTo("receiver", selfID)
                                .sort("timestamp")
                                .findAll());

                        List<ChatModel> results2 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                                .equalTo("isTopic", 0)
                                .equalTo("sender", selfID)
                                .equalTo("receiver", destinationUserID)
                                .sort("timestamp")
                                .findAll());

                        Log.d(TAG, "onReceive: results1 size"+results1.size()+" results2 size"+results2.size());

                        results1.addAll(results2);
                        Collections.sort(results1, new SortByTimeStamp());

                        if (!results1.isEmpty()) {
                            chatModelList.clear();
                            chatModelList.addAll(results1);
                            adapter.notifyDataSetChanged();
                            rv.scrollToPosition(adapter.getItemCount()-1);
                            Log.d(TAG, "onReceive: adapter notified// list size: " + chatModelList.size());
                        } else {
                            Log.d(TAG, "onReceive: RealmResult empty");
                        }
//                        String message = intent.getStringExtra("message");
//                        String time = intent.getStringExtra("time");
//                        String senderToken = intent.getStringExtra("senderToken");
//                        Log.d(TAG, "onReceive in Token Chat: " + message + " " + time);
//                        setChatObject(message, time, 1, senderToken);
                        Log.d(TAG, "onReceive: outside if");
                    } catch (Exception e) {
                        Log.d(TAG, "onReceive: Exception:"+ e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }

            }
        };
    }

    private void init() {
        List<ChatModel> results1 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 0)
                .equalTo("sender", destinationUserID)
                .equalTo("receiver", selfID)
                .sort("timestamp")
                .findAll());

        List<ChatModel> results2 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 0)
                .equalTo("sender", selfID)
                .equalTo("receiver", destinationUserID)
                .sort("timestamp")
                .findAll());

        Log.d(TAG, "onCreateView: results1:"+results1.size()+" results2:"+results2.size());

        results1.addAll(results2);
        Collections.sort(results1, new SortByTimeStamp());

        if (!results1.isEmpty()) {
            chatModelList.clear();
            chatModelList.addAll(results1);
            Log.d(TAG, "onCreateView: adapter notified// list size: " + chatModelList.size());
        } else {
            Log.d(TAG, "onCreateView: RealmResult empty");
        }

        adapter = new ChatAdapter(chatModelList, this);
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputEditText.getText().toString().isEmpty())
                    return;
                String msg = inputEditText.getText().toString();
                inputEditText.setText("");
                inputEditText.setHint("Send Message");
                String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());

                sendPayload(msg, time, selfID, testDestinationToken);
            }
        });
    }

    private void sendPayload(final String msg, final String time, final String selfID, final String destinationToken) {
        //TODO Possible crash to empty topic name
        Data data = new Data(msg, time, selfID, "", 0);
        Message message = new Message(destinationToken, data);
        Call<Message> call = APIClient.getAPIInterface().sendMessage(legacyServerKey, message);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    setChatObject(msg, time);
                    Log.d(TAG, "onResponse: Successfully sent message");
                    Log.d(TAG, "onResponse: sent to: " + destinationToken);
                } else {
                    Log.d(TAG, "onResponse: Error sending message");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.d(TAG, "onFailure: error");
            }
        });
    }

    private void setChatObject(String message, String time) {
        mDatabase.beginTransaction();
        mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 0)
                .equalTo("sender", selfID)
                .equalTo("receiver", destinationUserID).findAll().deleteAllFromRealm();
        mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 0)
                .equalTo("sender", destinationUserID)
                .equalTo("receiver", selfID).findAll().deleteAllFromRealm();

        ChatModel chatModelObject = new ChatModel(message, time, selfID, destinationUserID, "", 0);
        chatModelList.add(chatModelObject);
        Log.d(TAG, "setChatObject: "+chatModelList.size());
        mDatabase.insert(chatModelList);
        adapter.notifyDataSetChanged();
        rv.scrollToPosition(adapter.getItemCount()-1);
        mDatabase.commitTransaction();
    }

    @Override
    public void onStart() { //Overriding onStart() to receive a LocalBroadcast from FirebaseMessagingService
        super.onStart();
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter("data-from-service")
            );
            Log.d(TAG, "onStart: service started");
        } catch (Exception e) {
            Log.d(TAG, "onStart: exception: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onStop() {  //Overriding onStop() to unregister the LocalBroadcast receiver
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            Log.d(TAG, "onStop: LocalBroadcastManager stopped");
        } catch (Exception e) {
            Log.d(TAG, "onStop: exception: " + e.getLocalizedMessage());
        }
    }

    private void initViews(){  // Method for initializing the views
        try {
            setTitle(nameOfUser);
        }catch (Exception e){
            e.printStackTrace();
        }
        rv=findViewById(R.id.topic_chat_rv);
        inputEditText=findViewById(R.id.topic_input);
        send=findViewById(R.id.topic_send);
    }

    class SortByTimeStamp implements Comparator<ChatModel> {
        public int compare(ChatModel a, ChatModel b) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            try {
                Date d1 = simpleDateFormat.parse(a.getTimestamp());
                Date d2 = simpleDateFormat.parse(b.getTimestamp());
                Calendar c1 = Calendar.getInstance();
                c1.setTime(d1);
                Calendar c2 = Calendar.getInstance();
                c2.setTime(d2);
                long diff = c1.getTimeInMillis() - c2.getTimeInMillis();
                if (diff > 0) return 1;
                else if (diff < 0) return -1;
                else return 0;
            } catch (Exception e) {

            }
            return 0;
        }
    }
}
