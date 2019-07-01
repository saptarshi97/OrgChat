package in.appslab.orgchat.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.appslab.orgchat.Adapters.MultiSelectAdapter;
import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.R;

public class CreateTeamActivity extends AppCompatActivity {
    public static String PREF_NAME="shared values";
    private static String TAG=CreateTeamActivity.class.getSimpleName();
    private FloatingActionButton fab;
    private EditText teamNameEditText;
    private RecyclerView teamSelectRV;
    private MultiSelectAdapter adapter;
    private List<CreateTeamModel>members;
    FirebaseFirestore db;
    List<String> leads;
    SharedPreferences prefs;
    private String orgID,orgName,parentTeam;
    Map<String, Object> data;
    private Activity activity=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Create Team");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        members=new ArrayList<>();
        leads=new ArrayList<>();
        data= new HashMap<>();
        prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        init();
        getMembersFromDB();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!teamNameEditText.getText().toString().isEmpty()) {
                    List<CreateTeamModel> teamMembersList = adapter.getSelected();
                    createTeamInDb(teamNameEditText.getText().toString(),teamMembersList);
                }else{
                    //Show an alert
                }

            }
        });
    }

    private void createTeamInDb(String s, List<CreateTeamModel> teamMembersList) {
        List<String> ids=new ArrayList<>();
        for(CreateTeamModel x: teamMembersList)
            ids.add(x.getUserID());
        db.collection("teams").whereArrayContains("subscribed_tokens",ids.get(0)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null){
                    for(DocumentSnapshot documentSnapshot: task.getResult()) {
                        parentTeam = documentSnapshot.getId();
                        Log.d(TAG, "onComplete: " +parentTeam);
                    }
                }
                data.put("parent_team",parentTeam);
            }
        });
        data.put("group_name",s);
        data.put("lead_id",ids.get(0));
        data.put("organization",orgName);
        data.put("organizationID",orgID);
        data.put("parent_team",parentTeam);
        data.put("subscribed_tokens",ids);

        db.collection("teams").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(CreateTeamActivity.class.getSimpleName(), "onSuccess: Team created with ID: "+documentReference.getId());
                activity.setResult(2);
                activity.finish();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                activity.setResult(1);
                activity.finish();
            }
        });
    }

    private void getMembersFromDB() {
        Query query=db.collection("teams").whereEqualTo("organizationID",orgID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult()!=null) {
                        for (DocumentSnapshot docSnap : task.getResult()) {
                            leads.add(docSnap.getString("lead_id"));
                        }
                    }
                    setMembersList();
                }
            }
        });
    }

    private void setMembersList() {
        db.collection("Users").whereEqualTo("organizationID",orgID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult()!=null){
                        for(DocumentSnapshot docSnap:task.getResult()){
                            if(!leads.contains(docSnap.getId()))
                                members.add(new CreateTeamModel(docSnap.getString("name"),docSnap.getId()));
                        }
                        if(members.size()>0)
                            adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void init() {
        db=FirebaseFirestore.getInstance();
        orgID=prefs.getString("organizationID","NA");
        orgName=prefs.getString("organization","NA");
        teamSelectRV=findViewById(R.id.team_selection_recyclerview);
        fab=findViewById(R.id.fab_create_team);
        teamNameEditText=findViewById(R.id.team_name_edit_text);
        MultiSelectAdapter.ModifyActivityViewVisibility inter = new MultiSelectAdapter.ModifyActivityViewVisibility() {
            @Override
            public void changeState(int state) {
                teamNameEditText.setVisibility(state);
                if(state==View.VISIBLE) {
                    fab.show();
                }
                else {
                    fab.hide();
                }
            }
        };
        adapter=new MultiSelectAdapter(this, members,inter);
        initRecyclerView();
    }

    private void initRecyclerView() {
        teamSelectRV.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        teamSelectRV.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        teamSelectRV.addItemDecoration(dividerItemDecoration);
    }
}
