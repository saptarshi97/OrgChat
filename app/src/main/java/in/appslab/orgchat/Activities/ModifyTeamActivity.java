package in.appslab.orgchat.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Adapters.ModifyTeamAdapter;
import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.Models.RemoveMemberModel;
import in.appslab.orgchat.Models.RemoveMemberResponseModel;
import in.appslab.orgchat.Network.RemovalAPIClient;
import in.appslab.orgchat.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModifyTeamActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String selfID;
    private String topic;
    private String leadID;
    private RecyclerView recyclerView;
    private ModifyTeamAdapter adapter;
    private LinearLayout deleteTeamLL;
    private List<CreateTeamModel> list=new ArrayList<>();
    private String legacyServerKey="key=AIzaSyCJsQ88WD_mqV0XYw9brGS9RJfOhXyOiKU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_team);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        init();
    }

    private void init() {
        db=FirebaseFirestore.getInstance();
        Intent intent=getIntent();
        selfID=intent.getStringExtra("selfID");
        topic=intent.getStringExtra("topic");
        deleteTeamLL=findViewById(R.id.delete_team_ll);
        recyclerView=findViewById(R.id.topic_members_rv);

        deleteTeamLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTeam();
            }
        });
        ModifyTeamAdapter.ModificationOptionsInterface optionsInter = new ModifyTeamAdapter.ModificationOptionsInterface() {
            @Override
            public void showOptions(String userID, String name, String token) {
                loadOptionsDialog(userID,name,token);
            }
        };

        adapter=new ModifyTeamAdapter(this,list,optionsInter);
        initRv();
        getTeamMembersFromDb();
    }

    private void deleteTeam() {
        //TODO Add a progress bar
        db.collection("teams").document(topic).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            List<String> userIDs=new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    DocumentSnapshot docSnap=task.getResult();
                    userIDs=(List<String>)docSnap.get("subscribed_tokens");
                }
                if(userIDs!=null) {
                    if (!userIDs.isEmpty()) {
                        db.collection("Users").whereArrayContains("team",topic).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful() && task.getResult()!=null){
                                    List<String> tokens=new ArrayList<>();
                                    for(DocumentSnapshot x:task.getResult())
                                        tokens.add(x.getString("token"));
                                    unSubFromTopic(tokens);
                                }
                            }
                        });
                        for(final String x : userIDs) {
                            db.collection("Users").document(x).update("team", FieldValue.arrayRemove(topic))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(ModifyTeamActivity.class.getSimpleName(), "onSuccess: Done!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: " + e.getLocalizedMessage());
                                        }
                                    });
                        }
                    }
                }
            }

            private void unSubFromTopic(List<String> tokens){
                RemoveMemberModel remove=new RemoveMemberModel();
                remove.setTo("/topics/"+topic);
                remove.setRegistrationTokens(tokens);

                Call<RemoveMemberResponseModel> call= RemovalAPIClient.getAPIInterface().removeMember(legacyServerKey,remove);
                call.enqueue(new Callback<RemoveMemberResponseModel>() {
                    @Override
                    public void onResponse(Call<RemoveMemberResponseModel> call, Response<RemoveMemberResponseModel> response) {
                        try {
                            if(response.isSuccessful())
                                Log.d(ModifyTeamActivity.class.getSimpleName(), "onResponse: " + response.body().getResults());
                            //TODO Show something to denote things didnt pan out as planned in an else-block
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<RemoveMemberResponseModel> call, Throwable t) {
                        //TODO Show something to denote things didnt pan out as planned
                        Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: Error");
                    }
                });
            }
        });

        db.collection("teams").document(topic).delete()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onSuccess: Deleted team successfully from db");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: Failed to delete team from db");
            }
        });
        //Since the damn thing doesn't exist anymore
        startActivity(new Intent(this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void loadOptionsDialog(final String userID, final String name, final String token) {
        if(!selfID.equals(leadID))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] opts={"Remove "+name+" from the Team", "Chat with "+name};
        builder.setItems(opts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        new AlertDialog.Builder(ModifyTeamActivity.this)
                                .setTitle("Alert")
                                .setIcon(R.drawable.ic_warn)
                                .setCancelable(false)
                                .setMessage("Do you really want to remove "+name)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        removeMember(userID,token);
                                    }
                                })
                                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).show();
                        break;
                    case 1:
                        Bundle bundle=new Bundle();
                        Intent intent=new Intent(ModifyTeamActivity.this,TokenChatActivity.class);
                        bundle.putString("registrationToken", token);
                        bundle.putString("name", name);
                        bundle.putString("userID",userID);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        Log.d(ModifyTeamActivity.class.getSimpleName(), "onClick: unknown case");
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeMember(final String userID, String token) {
        db.collection("teams").document(topic).update("subscribed_tokens",FieldValue.arrayRemove(userID))
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onSuccess: "+userID+" Deleted from team: "+topic);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: "+userID+" not deleted from team: "+topic);
            }
        });

        db.collection("Users").document(userID).update("team",FieldValue.arrayRemove(topic))
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onSuccess: "+topic+" deleted from profile of user: "+userID);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: "+topic+" not deleted from profile of user: "+userID);
            }
        });

        RemoveMemberModel remove=new RemoveMemberModel();
        remove.setTo("/topics/"+topic);
        List<String> tokens=new ArrayList<>();
        tokens.add(token);
        remove.setRegistrationTokens(tokens);

        Call<RemoveMemberResponseModel> call= RemovalAPIClient.getAPIInterface().removeMember(legacyServerKey,remove);
        call.enqueue(new Callback<RemoveMemberResponseModel>() {
            @Override
            public void onResponse(Call<RemoveMemberResponseModel> call, Response<RemoveMemberResponseModel> response) {
                try {
                    if(response.isSuccessful())
                        Log.d(ModifyTeamActivity.class.getSimpleName(), "onResponse: " + response.body().getResults());
                    //TODO Show something to denote things didnt pan out as planned in an else-block
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<RemoveMemberResponseModel> call, Throwable t) {
                //TODO Show something to denote things didnt pan out as planned
                Log.d(ModifyTeamActivity.class.getSimpleName(), "onFailure: Error");
            }
        });

        getTeamMembersFromDb();
    }

    private void getTeamMembersFromDb() {
        db.collection("teams").document(topic).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            List<String> userIDs=new ArrayList<>();
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    DocumentSnapshot docSnap=task.getResult();
                    userIDs=(List<String>)docSnap.get("subscribed_tokens");
                    leadID=docSnap.getString("lead_id");
                    if(selfID.equals(leadID)){
                        deleteTeamLL.setVisibility(View.VISIBLE);
                    }
                }
                try {
                    if(!userIDs.isEmpty()) {
                        for(String x:userIDs){
                            db.collection("Users").document(x).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()&&task.getResult()!=null) {
                                        DocumentSnapshot docSnap = task.getResult();
                                        CreateTeamModel team=new CreateTeamModel(docSnap.getString("name"),docSnap.getId());
                                        team.setUserToken(docSnap.getString("token"));
                                        list.add(team);
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Log.d(ModifyTeamActivity.class.getSimpleName(), "onComplete: Exception with message: "+e.getLocalizedMessage());
                }
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
