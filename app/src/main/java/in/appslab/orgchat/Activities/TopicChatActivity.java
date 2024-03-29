package in.appslab.orgchat.Activities;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
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
import in.appslab.orgchat.Models.SendTopicMessageResponse;
import in.appslab.orgchat.Network.APIClient;
import in.appslab.orgchat.R;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicChatActivity extends AppCompatActivity {
    private RecyclerView rv;
    private EditText inputEditText;
    private ImageView send,attach;
    ChatAdapter adapter;
    private List<ChatModel> chatModelList;
    BroadcastReceiver receiver;
    private String selfID;
    private String topic;
    private Toolbar toolbar;
    private static final int PICK_IMAGE_REQUEST=1;
    private static final String TAG = "TopicChatActivity";
    public static String PREF_NAME="shared values";
    private String legacyServerKey="key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";
    private String fragmentTitle;
    private Realm mDatabase;
    private RelativeLayout topicReplyLayout;
    private TextView topicReplyText;
    private ImageView topicDismissReply;
    public static boolean isInActionMode = false;
    public static ArrayList<ChatModel> selectionList;
    private String quotedTextId=null;
    private Uri imageUri=null;
    private StorageReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_chat);
        SharedPreferences prefs=getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        ref=FirebaseStorage.getInstance().getReference("uploads");
        chatModelList=new ArrayList<>();
        selectionList = new ArrayList<>();
        mDatabase = Realm.getDefaultInstance();
        Bundle mBundle=getIntent().getExtras();
        topic=mBundle.getString("topic");
        fragmentTitle=mBundle.getString("name");
        selfID = prefs.getString("username", "");
        initViews();
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TopicChatActivity.this, ModifyTeamActivity.class).putExtra("topic",topic).putExtra("selfID",selfID));
            }
        });
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

        ChatAdapter.ActionModeInterface actionModeInterface= new ChatAdapter.ActionModeInterface() {
            @Override
            public void onClickHandler(int position) {
                if(isInActionMode){
                    prepareSelection(position);
                    if(adapter!=null){
                        adapter.notifyDataSetChanged();
                        //adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onLongClickHandler(int position) {
                prepareToolbar(position);
            }

            @Override
            public boolean setSelectionColor(ChatModel chat) {
                try {
                    if(isInActionMode){
                        if(selectionList.contains(chat))
                            return true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public void replyClickHandler(int position) {
                if(!isInActionMode)
                    rv.scrollToPosition(position);
            }
        };
        adapter = new ChatAdapter(chatModelList,this,actionModeInterface);
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);
        rv.scrollToPosition(adapter.getItemCount()-1);
        topicDismissReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topicReplyLayout.setVisibility(View.GONE);
                quotedTextId=null;
            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputEditText.getText().toString().isEmpty())
                    return;
                String msg=inputEditText.getText().toString();
                inputEditText.setText("");
                inputEditText.setHint("Send Message");
                String time=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());

                sendPayload(msg,time, selfID,topic,null);
            }
        });
    }

    private void chooseImage() {
        startActivityForResult(new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT),PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_REQUEST && resultCode== RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();
            uploadFile(imageUri);
        }
    }

    private void uploadFile(Uri imageUri) {
        if(imageUri!=null){
            //TODO append child ref with userID (Phone number or Firebase UID) to avoid name conflict
            final StorageReference fileRef=ref.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

            Task<Uri> urlTask=fileRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful() && task.getResult()!=null){
                        Uri downloadUri=task.getResult();
                        String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        sendPayload(null, time, selfID, topic,downloadUri.toString());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                }
            });
        }
    }

    private void initViews(){  // Method for initializing the views
        toolbar=findViewById(R.id.topic_chat_toolbar);
        try {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(fragmentTitle);
        }catch (Exception e){
            e.printStackTrace();
        }
        rv=findViewById(R.id.topic_chat_rv);
        inputEditText=findViewById(R.id.topic_input);
        send=findViewById(R.id.topic_send);
        attach=findViewById(R.id.topic_attach);
        topicReplyLayout=findViewById(R.id.topic_reply_layout);
        topicReplyText=findViewById(R.id.topic_reply_text);
        topicDismissReply=findViewById(R.id.topic_dismiss_reply);
        init();
        initReceiver();
    }

    private void sendPayload(@Nullable final String msg, final  String time,final String selfID,final String destinationTopic,@Nullable final String downloadUri) {
        Data data=new Data(msg,time,selfID,destinationTopic,1);
        data.setQuotedMessageId(quotedTextId);
        if(downloadUri!=null){
            data.setDownloadUri(downloadUri);
        }
        Log.d(TAG, "sendPayload: quoted text in Data object"+data.getQuotedMessageId());
        final Message message=new Message("/topics/"+destinationTopic,data);
        Call<SendTopicMessageResponse> call= APIClient.getAPIInterface().sendTopicMessage(legacyServerKey, message);
        call.enqueue(new Callback<SendTopicMessageResponse>() {
            @Override
            public void onResponse(Call<SendTopicMessageResponse> call, Response<SendTopicMessageResponse> response) {
                if(response.isSuccessful() && response.body().getMessageId()!=null) {
                    try {
                        String messageId = response.body().getMessageId();
                        if(topicReplyLayout.getVisibility()==View.VISIBLE){
                            topicReplyLayout.setVisibility(View.GONE);
                            setChatObject(msg, time,destinationTopic,messageId,quotedTextId,downloadUri);
                            quotedTextId=null;
                        }else {
                            setChatObject(msg, time, destinationTopic,messageId,null,downloadUri);
                        }
                    }catch (Exception e){
                        Log.d(TAG, "onResponse: Error"+e.getLocalizedMessage());
                    }
                    Log.d(TAG, "onResponse: Successfully sent message to: "+destinationTopic);
                    Log.d(TAG, "onResponse: Successfully sent message with id: "+response.body().getMessageId());
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

    private void setChatObject(@Nullable String message, String time, String topicName, String messageId, @Nullable String quotedTextId,@Nullable final String downloadUri) {
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
        chatModelObject.setMessageId(messageId);
        if(quotedTextId!=null){
            chatModelObject.setQuotedMessageId(quotedTextId);
        }
        if(downloadUri!=null){
            chatModelObject.setDownloadUri(downloadUri);
        }
        chatModelList.add(chatModelObject);
        Log.d(TAG, "setChatObject: "+chatModelList.size());
        mDatabase.insert(chatModelList);
        adapter.notifyDataSetChanged();
        rv.scrollToPosition(adapter.getItemCount()-1);
        mDatabase.commitTransaction();
    }

    public void prepareToolbar(int position) {
        // prepare action mode
        try {
            toolbar.getMenu().clear();
        }catch (Exception e){
            e.printStackTrace();
        }
        toolbar.inflateMenu(R.menu.menu_action_mode);
        isInActionMode = true;
        adapter.notifyDataSetChanged();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        prepareSelection(position);
    }

    public void prepareSelection(int position) {

        if (!selectionList.contains(chatModelList.get(position))) {
            selectionList.add(chatModelList.get(position));
        } else {
            selectionList.remove(chatModelList.get(position));
        }
        updateViewCounter();
    }

    private void updateViewCounter() {
        int counter = selectionList.size();
        if(counter ==0){
            clearActionMode();
        }
        else if (counter == 1) {
            // reply
            toolbar.getMenu().getItem(0).setVisible(true);
        } else {
            toolbar.getMenu().getItem(0).setVisible(false);
        }

        if(counter>0)
            toolbar.setTitle(""+counter);
    }

    public void clearActionMode() {
        isInActionMode = false;
        try {
            toolbar.getMenu().clear();
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(fragmentTitle);
        }catch(Exception e){
            e.printStackTrace();
        }
        selectionList.clear();
        adapter.notifyDataSetChanged();
        toolbar.setTitle(fragmentTitle);
    }

    @Override
    public void onBackPressed() {
        if (isInActionMode) {
            clearActionMode();
            adapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.reply_action:
                setReply(selectionList.get(0));
                clearActionMode();
                return true;
            case R.id.copy_action:
                copyToClip(selectionList);
                clearActionMode();
                return true;
            case R.id.delete_action:
                clearActionMode();
                return true;
            case R.id.forward_action:
                forwardMessage(selectionList);
                clearActionMode();
                return true;
            case android.R.id.home:
                if(isInActionMode){
                    clearActionMode();
                    adapter.notifyDataSetChanged();
                }else {
                    onBackPressed();
                }
                return true;
            default:
                clearActionMode();
                adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setReply(ChatModel chatModel) {
        topicReplyLayout.setVisibility(View.VISIBLE);
        quotedTextId=chatModel.getMessageId();
        Log.d(TAG, "setReply: with quotedTextID:"+quotedTextId);
        if( chatModel.getChatMessage()!=null && !chatModel.getChatMessage().isEmpty())
            topicReplyText.setText(chatModel.getChatMessage());
        else
            topicReplyText.setText("Photo");
    }

    private void forwardMessage(ArrayList<ChatModel> selectionList) {
        startActivity(new Intent(this,RecipientListActivity.class).putExtra("list",(Serializable)selectionList));
    }

    private void copyToClip(ArrayList<ChatModel> selectionList) {
        String x="";
        for (ChatModel y:selectionList) {
            if (selectionList.size() != 1)
                x+=y.getSender() + ":" + y.getChatMessage() + "\n";
            else
                x+=y.getChatMessage();
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", x);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied to clipboard", Toast.LENGTH_SHORT).show();
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
