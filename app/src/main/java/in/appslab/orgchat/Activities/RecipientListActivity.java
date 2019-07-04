package in.appslab.orgchat.Activities;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.appslab.orgchat.Adapters.MultiSelectAdapter;
import in.appslab.orgchat.Models.ChatModel;
import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.Models.Data;
import in.appslab.orgchat.Models.Message;
import in.appslab.orgchat.Models.SendMessageResponse;
import in.appslab.orgchat.Models.SendTopicMessageResponse;
import in.appslab.orgchat.Network.APIClient;
import in.appslab.orgchat.R;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipientListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<CreateTeamModel> list;
    private List<CreateTeamModel> adminsList;
    private MultiSelectAdapter adapter;
    private FloatingActionButton fab;
    public static String TAG=RecipientListActivity.class.getSimpleName();
    public static String PREF_NAME="shared values";
    private String userID,company;
    private  SharedPreferences prefs;
    private FirebaseFirestore db;
    private Realm mDatabase;
    private List<ChatModel> forwardedChats;
    private String legacyServerKey="key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_list);
        prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        list=new ArrayList<>();
        adminsList=new ArrayList<>();
        forwardedChats=(List<ChatModel>)getIntent().getSerializableExtra("list");
        init();
    }

    private void init() {
        mDatabase=Realm.getDefaultInstance();
        db=FirebaseFirestore.getInstance();
        userID=prefs.getString("username","");
        company=prefs.getString("organization","");
        fab=findViewById(R.id.fab_forward_messages);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forwardMessages(adapter.getSelected());
            }
        });
        recyclerView=findViewById(R.id.recipient_rv);
        MultiSelectAdapter.ModifyActivityViewVisibility inter = new MultiSelectAdapter.ModifyActivityViewVisibility() {
            @Override
            public void changeState(int state) {
                if(state==View.VISIBLE) {
                    fab.show();
                }
                else {
                    fab.hide();
                }
            }
        };
        adapter=new MultiSelectAdapter(this,list,inter);
        initRv();
        loadCompany(userID,company);
        loadTeamsForUser(userID,company);
    }

    private void forwardMessages(List<CreateTeamModel> selected) {
        for(CreateTeamModel x: selected){
            if(x.getIsTopic()==0){
                for(ChatModel y:forwardedChats){
                    String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    sendPayload(y.getChatMessage(),time,userID,x.getUserToken(),x.getIsTopic(),y.getDownloadUri());
                }
            }else{
                for(ChatModel y:forwardedChats){
                    String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    sendPayload(y.getChatMessage(),time,userID,x.getUserToken(),x.getIsTopic(),y.getDownloadUri());
                }
            }
        }
        finish();
    }

    private void sendPayload(@Nullable final String msg,final  String time,final String selfID,final String destination,final  int isTopic,@Nullable final String downloadUri) {
        Message message=new Message();
        if(isTopic==0){
            Data data = new Data(msg, time, selfID, "", isTopic);
            data.setDownloadUri(downloadUri);
            message.setTo(destination);
            message.setData(data);
            Call<SendMessageResponse> call = APIClient.getAPIInterface().sendMessage(legacyServerKey, message);
            call.enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    if (response.isSuccessful() && response.body().getSuccess().equals("1")) {
                        try {
                            String messageId = response.body().getResults().get(0).getMessageId();
                            setChatObject(msg, time, selfID, destination, isTopic, messageId,downloadUri);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "onResponse: Error sending message");
                    }
                }
                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: error");
                }
            });
        }else{
            Data data=new Data(msg,time,selfID,destination,isTopic);
            data.setDownloadUri(downloadUri);
            message.setTo("/topics/"+destination);
            message.setData(data);
            Call<SendTopicMessageResponse> call= APIClient.getAPIInterface().sendTopicMessage(legacyServerKey, message);
            call.enqueue(new Callback<SendTopicMessageResponse>() {
                @Override
                public void onResponse(Call<SendTopicMessageResponse> call, Response<SendTopicMessageResponse> response) {
                    if(response.isSuccessful() && response.body().getMessageId()!=null) {
                        try {
                            String messageId = response.body().getMessageId();
                            setChatObject(msg, time,selfID,destination,isTopic,messageId,downloadUri);
                        }catch (Exception e){
                            Log.d(TAG, "onResponse: Error"+e.getLocalizedMessage());
                        }
                        Log.d(TAG, "onResponse: Successfully sent message to: "+destination);
                    }
                    else {
                        Log.d(TAG, "onResponse: Error sending message");
                    }
                }
                @Override
                public void onFailure(Call<SendTopicMessageResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: error");
                }
            });
        }
    }

    private void setChatObject(@Nullable String msg, String time, String selfID, String destination, int isTopic, String messageId,@Nullable String downloadUri) {
        List<ChatModel> chatModelList = new ArrayList<>();
        mDatabase.beginTransaction();
        if(isTopic==0){
            ChatModel chatModelObject = new ChatModel(msg, time, selfID, destination, "", isTopic);
            chatModelObject.setMessageId(messageId);
            chatModelObject.setDownloadUri(downloadUri);
            chatModelList.add(chatModelObject);
        }else{
            ChatModel chatModelObject = new ChatModel(msg, time, selfID, destination, destination, isTopic);
            chatModelObject.setMessageId(messageId);
            chatModelObject.setDownloadUri(downloadUri);
            chatModelList.add(chatModelObject);
        }
        mDatabase.insert(chatModelList);
        mDatabase.commitTransaction();
    }


    private void loadCompany(String userID, String company) {
        CollectionReference collectionReference=db.collection("companies");
        collectionReference.whereArrayContains("members",userID)
                .whereEqualTo("name",company).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult()!=null){
                        if (task.getResult().size() == 1) {
                            Log.d(TAG, "onComplete: list size one");
                            for(QueryDocumentSnapshot docSnap : task.getResult()) {
                                CreateTeamModel obj=new CreateTeamModel(docSnap.getString("name"),docSnap.getId());
                                obj.setIsTopic(1);
                                obj.setUserToken(docSnap.getId());
                                list.add(0,obj);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "onComplete: Member present in two companies");
                        }
                    }
                }
            }});
    }

    private void loadTeamsForUser(final String userID, final String company) {
        CollectionReference collectionReference=db.collection("teams");
        Query query=collectionReference.whereEqualTo("organization", company).whereArrayContains("subscribed_tokens",userID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult()!=null){
                        for(QueryDocumentSnapshot docSnap: task.getResult()){
                            CreateTeamModel obj=new CreateTeamModel(docSnap.getString("group_name"), docSnap.getId());
                            obj.setIsTopic(1);
                            obj.setUserToken(docSnap.getId());
                            if(docSnap.get("lead_id").toString().equals(userID)) {
                                loadTeamMembers(docSnap.getId(),company, userID);
                            }
                            else{
                                loadAdmins(docSnap.getString("lead_id"));
                            }
                            list.add(obj);
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, "Error retrieving docs with exception: " + task.getException());
                }
            }
        });
    }

    private void loadTeamMembers(String id, String company, final String userID) {
        Log.d(TAG, "loadTeamMembers: here");
        CollectionReference collectionReference=db.collection("Users");
        Query query=collectionReference.whereEqualTo("organization", company)
                .whereArrayContains("team",id);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult()!=null) {
                        List<CreateTeamModel> teamMembersList=new ArrayList<>();
                        for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                            if(!documentSnapshot.getId().equals(userID)){
                                CreateTeamModel obj=new CreateTeamModel(documentSnapshot.getString("name"),documentSnapshot.getId());
                                obj.setIsTopic(0);
                                obj.setUserToken(documentSnapshot.getString("token"));
                                if(!list.contains(obj))
                                    teamMembersList.add(obj);
                            }
                        }
                        list.addAll(teamMembersList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void loadAdmins(String lead_id) {
        DocumentReference docRef=db.collection("Users").document(lead_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot docSnap=task.getResult();
                    CreateTeamModel obj=new CreateTeamModel(docSnap.getString("name"),docSnap.getId());
                    obj.setIsTopic(0);
                    obj.setUserToken(docSnap.getString("token"));
                    if(!list.contains(obj))
                        adminsList.add(obj);
                }
                list.addAll(adminsList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initRv() {
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }
}
