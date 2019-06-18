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

public class TopicChatActivity extends AppCompatActivity {
    private RecyclerView rv;
    private EditText inputEditText;
    private ImageView send;
    ChatAdapter adapter;
    private List<ChatModel> chatModelList=new ArrayList<>();
    BroadcastReceiver receiver;
    private String selfID;
    private String topic;
    private static final String TAG = "TopicChatFragment";
    public static String PREF_NAME="shared values";
    private String legacyServerKey="key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";
    private String fragmentTitle;
    private Realm mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_chat);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences prefs=getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        mDatabase = Realm.getDefaultInstance();
        Bundle mBundle=getIntent().getExtras();
        topic=mBundle.getString("topic");
        fragmentTitle=mBundle.getString("name");
        selfID = prefs.getString("username", "");
        initViews();
        init();
    }

    private void initReceiver() {
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //TODO Add a check if the topic name matches the topic intended for the fragment passed by activity via bundle
                if(intent.getIntExtra("newDataAvailable", 0) == 1) {
                    try {
                        //Call and a method and pass the messages+details to add to a list here
                        List<ChatModel> results1 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                                .equalTo("isTopic", 1)
                                .equalTo("topicName",topic)
                                .equalTo("receiver", selfID)
                                .sort("timestamp")
                                .findAll());

                        List<ChatModel> results2 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                                .equalTo("isTopic", 1)
                                .equalTo("topicName",topic)
                                .equalTo("sender", selfID)
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void init(){
        try {
            //Call and a method and pass the messages+details to add to a list here
            List<ChatModel> results1 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                    .equalTo("isTopic", 1)
                    .equalTo("topicName",topic)
                    .equalTo("receiver", selfID)
                    .sort("timestamp")
                    .findAll());

            List<ChatModel> results2 = mDatabase.copyFromRealm(mDatabase.where(ChatModel.class)
                    .equalTo("isTopic", 1)
                    .equalTo("topicName",topic)
                    .equalTo("sender", selfID)
                    .sort("timestamp")
                    .findAll());

            Log.d(TAG, "onReceive: results1 size"+results1.size()+" results2 size"+results2.size());

            results1.addAll(results2);
            Collections.sort(results1, new SortByTimeStamp());

            if (!results1.isEmpty()) {
                chatModelList.clear();
                chatModelList.addAll(results1);
                Log.d(TAG, "onReceive: adapter notified// list size: " + chatModelList.size());
            } else {
                Log.d(TAG, "onReceive: RealmResult empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new ChatAdapter(chatModelList,this);
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);
        rv.scrollToPosition(adapter.getItemCount()-1);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputEditText.getText().toString().isEmpty())
                    return;
                String msg=inputEditText.getText().toString();
                inputEditText.setText("");
                inputEditText.setHint("Send Message");
                String time=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());

                sendPayload(msg,time, selfID,topic);
            }
        });
    }

    private void initViews(){  // Method for initializing the views
        try {
            setTitle(fragmentTitle);
        }catch (Exception e){
            e.printStackTrace();
        }
        rv=findViewById(R.id.topic_chat_rv);
        inputEditText=findViewById(R.id.topic_input);
        send=findViewById(R.id.topic_send);
        initReceiver();
    }

    private void sendPayload(final String msg, final  String time,final String selfID,final String destinationTopic) {
        Data data=new Data(msg,time,selfID,destinationTopic,1);
        Message message=new Message("/topics/"+destinationTopic,data);
        Call<Message> call= APIClient.getAPIInterface().sendMessage(legacyServerKey, message);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if(response.isSuccessful()) {
                    setChatObject(msg,time,destinationTopic);
                    Log.d(TAG, "onResponse: Successfully sent message to: "+destinationTopic);
                }
                else {
                    Log.d(TAG, "onResponse: Error sending message");
                }
            }
            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Log.d(TAG, "onFailure: error");
            }
        });
    }

    private void setChatObject(String message, String time, String topicName) {
        mDatabase.beginTransaction();
        mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 1)
                .equalTo("topicName",topic)
                .equalTo("receiver", selfID).findAll().deleteAllFromRealm();
        mDatabase.where(ChatModel.class)
                .equalTo("isTopic", 1)
                .equalTo("topicName",topic)
                .equalTo("sender", selfID).findAll().deleteAllFromRealm();
        ChatModel chatModelObject = new ChatModel(message, time, selfID, topicName, topicName, 1);
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
        try{
            LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                    new IntentFilter("data-from-service")
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {  //Overriding onStop() to unregister the LocalBroadcast receiver
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
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
