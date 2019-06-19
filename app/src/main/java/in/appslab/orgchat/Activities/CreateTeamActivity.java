package in.appslab.orgchat.Activities;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Adapters.MultiSelectAdapter;
import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.R;

public class CreateTeamActivity extends AppCompatActivity {
    public static String PREF_NAME="shared values";
    private FloatingActionButton fab;
    private EditText teamNameEditText;
    private RecyclerView teamSelectRV;
    private MultiSelectAdapter adapter;
    private List<CreateTeamModel>members=new ArrayList<>();
    FirebaseFirestore db;
    List<String> leads=new ArrayList<>();
    SharedPreferences prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
    private String orgID;

    //TODO setResult when work here is done
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        init();
        getMembersFromDB();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO call a method to do the work
            }
        });
    }

    private void getMembersFromDB() {
        db.collection("teams").whereEqualTo("organizationID",orgID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
        orgID=prefs.getString("organizationID","NA");
        teamSelectRV=findViewById(R.id.team_selection_recyclerview);
        fab=findViewById(R.id.fab_create_team);
        teamNameEditText=findViewById(R.id.team_name_edit_text);
        MultiSelectAdapter.ModifyActivityViewVisibility inter =new MultiSelectAdapter.ModifyActivityViewVisibility() {
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
