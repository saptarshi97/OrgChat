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
import in.appslab.orgchat.Models.SendMessageResponse;
import in.appslab.orgchat.Network.APIClient;
import in.appslab.orgchat.R;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenChatActivity extends AppCompatActivity {
    private RecyclerView rv;
    private EditText inputEditText;
    private ImageView send,attach;
    private ChatAdapter adapter;
    private List<ChatModel> chatModelList;
    BroadcastReceiver receiver;
    private static final String TAG = "TokenChatActivity";
    private static final int PICK_IMAGE_REQUEST=1;
    public static String PREF_NAME = "shared values";
    private String legacyServerKey = "key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";
    private String testDestinationToken;
    private String nameOfUser;
    private String selfID;
    private String destinationUserID;
    private Realm mDatabase;
    private Toolbar toolbar;
    private String quotedTextId=null;
    private RelativeLayout tokenReplyLayout;
    private TextView tokenReplyText;
    private ImageView tokenDismissReply;
    private Uri imageUri=null;
    public static boolean isInActionMode = false;
    public static ArrayList<ChatModel> selectionList;
    private StorageReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_chat);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ref=FirebaseStorage.getInstance().getReference("uploads");
        mDatabase = Realm.getDefaultInstance();
        selectionList = new ArrayList<>();
        chatModelList = new ArrayList<>();
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

    }

    private void initViews(){  // Method for initializing the views
        toolbar=findViewById(R.id.token_chat_toolbar);
        try {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(nameOfUser);
        }catch (Exception e){
            e.printStackTrace();
        }
        tokenReplyLayout= findViewById(R.id.token_reply_layout);
        tokenReplyText=findViewById(R.id.token_reply_text);
        tokenDismissReply=findViewById(R.id.token_dismiss_reply);
        rv=findViewById(R.id.token_chat_rv);
        inputEditText=findViewById(R.id.token_input_edit_text);
        attach=findViewById(R.id.token_attach);
        send=findViewById(R.id.token_send_icon);
        init();
        initReceiver();
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

        ChatAdapter.ActionModeInterface actionModeInterface= new ChatAdapter.ActionModeInterface() {
            @Override
            public void onClickHandler(int position) {
                if(isInActionMode){
                    prepareSelection(position);
                    Log.d(TAG, "onClickHandler: position:"+position);
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
        adapter = new ChatAdapter(chatModelList, this,actionModeInterface);
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);
        tokenDismissReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tokenReplyLayout.setVisibility(View.GONE);
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
                if (inputEditText.getText().toString().isEmpty())
                    return;
                String msg = inputEditText.getText().toString();
                inputEditText.setText("");
                inputEditText.setHint("Send Message");
                String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());

                sendPayload(msg, time, selfID, testDestinationToken,null);
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
            final StorageReference fileRef=ref.child(selfID+""+System.currentTimeMillis()+"."+getFileExtension(imageUri));

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
                        sendPayload(null, time, selfID, testDestinationToken,downloadUri.toString());
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                }
            });
        }
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

    private void sendPayload(@Nullable final String msg, final String time, final String selfID, final String destinationToken, @Nullable final String downloadUri) {
        Data data = new Data(msg, time, selfID, "", 0);
        if(tokenReplyLayout.getVisibility()==View.VISIBLE){
            data.setQuotedMessageId(quotedTextId);
        }
        if(downloadUri!=null){
            data.setDownloadUri(downloadUri);
        }
        Message message = new Message(destinationToken, data);
        Call<SendMessageResponse> call = APIClient.getAPIInterface().sendMessage(legacyServerKey, message);
        call.enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body().getSuccess().equals("1")) {
                    try {
                        String messageId = response.body().getResults().get(0).getMessageId();
                        if (tokenReplyLayout.getVisibility() == View.VISIBLE) {
                            tokenReplyLayout.setVisibility(View.GONE);
                            setChatObject(msg, time, messageId, quotedTextId,downloadUri);
                            quotedTextId=null;
                        } else {
                            setChatObject(msg, time, messageId, null,downloadUri);
                        }
                        Log.d(TAG, "onResponse: Successfully sent message, and id is: "+messageId+" isSuccess? "+response.body().getSuccess()+"isFailure?"+response.body().getFailure());
                        Log.d(TAG, "onResponse: sent to: " + destinationToken);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d(TAG, "onResponse: Error sending message");
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: error "+t.getLocalizedMessage());
            }
        });
    }

    private void setChatObject(@Nullable String message, String time, String messageId, @Nullable String quotedTextId,@Nullable String downloadUri) {
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
            if(tokenReplyLayout.getVisibility()==View.VISIBLE)
                tokenReplyLayout.setVisibility(View.GONE);
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
            getSupportActionBar().setTitle(nameOfUser);
        }catch(Exception e){
            e.printStackTrace();
        }
        selectionList.clear();
        adapter.notifyDataSetChanged();
        toolbar.setTitle(nameOfUser);
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
        tokenReplyLayout.setVisibility(View.VISIBLE);
        quotedTextId=chatModel.getMessageId();
        Log.d(TAG, "setReply: "+quotedTextId+"\nname:"+chatModel.getChatMessage());
        if(chatModel.getChatMessage()!=null && !chatModel.getChatMessage().isEmpty())
            tokenReplyText.setText(chatModel.getChatMessage());
        else
            tokenReplyText.setText("Photo");
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
