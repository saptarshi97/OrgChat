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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Adapters.ModifyTeamAdapter;
import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.R;

public class ModifyTeamActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String selfID;
    private String topic;
    private String leadID;
    private RecyclerView recyclerView;
    private ModifyTeamAdapter adapter;
    private List<CreateTeamModel> list=new ArrayList<>();

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
        Intent intent=getIntent();
        selfID=intent.getStringExtra("selfID");
        topic=intent.getStringExtra("topic");
        recyclerView=findViewById(R.id.topic_members_rv);
        ModifyTeamAdapter.ModificationOptionsInterface optionsInter = new ModifyTeamAdapter.ModificationOptionsInterface() {
            @Override
            public void showOptions(String userID, String name) {
                loadOptionsDialog(userID,name);
            }
        };
        adapter=new ModifyTeamAdapter(this,list,optionsInter);
        initRv();
        getTeamMembersFromDb();
    }

    private void loadOptionsDialog(final String userID, String name) {
        if(!selfID.equals(leadID))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] opts={"Remove "+name+" from the Team"};
        builder.setItems(opts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        removeMember(userID);
                        break;
                    default:
                        Log.d(ModifyTeamActivity.class.getSimpleName(), "onClick: unknown case");
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeMember(String userID) {
        //TODO Write code to kick and unsubscribe the userID
        //TODO https://developers.google.com/instance-id/reference/server
        //TODO https://iid.googleapis.com/iid/v1:batchRemove(POST)
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
                }
                try {
                    if(!userIDs.isEmpty()) {
                        for(String x:userIDs){
                            db.collection("Users").document(x).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()&&task.getResult()!=null) {
                                        DocumentSnapshot docSnap = task.getResult();
                                        list.add(new CreateTeamModel(docSnap.getString("name"),docSnap.getId()));
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
