package in.appslab.orgchat.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Activities.CreateTeamActivity;
import in.appslab.orgchat.Activities.LoginActivity;
import in.appslab.orgchat.Activities.ProfileActivity;
import in.appslab.orgchat.Adapters.UsersOfCompanyAdapter;
import in.appslab.orgchat.Models.UsersOfCompanyModel;
import in.appslab.orgchat.R;

import static android.content.Context.MODE_PRIVATE;


public class ChatFragment extends Fragment {
    public static String PREF_NAME="shared values";
    List<UsersOfCompanyModel> list;
    String userID,company;
    List<UsersOfCompanyModel> adminsList;
    private static final String TAG = "CompanyUsersFragment";
    UsersOfCompanyAdapter adapter;
    FirebaseFirestore db;
    SharedPreferences prefs=getActivity().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
    boolean isLead=false;
    private LinearLayout rootLayout;
    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chat, container, false);
        rootLayout=view.findViewById(R.id.root_ll);
        String nameOfUser=prefs.getString("name","");
        try {
            getActivity().setTitle(nameOfUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        init();
        userID=prefs.getString("username","");
        company=prefs.getString("organization","");
        String myToken=prefs.getString("registration token","");
        loadRecyclerView(view);
        loadCompany(userID,company);
        loadTeamsForUser(userID,company);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        if(prefs.getBoolean("isOrgAdmin",false)) {
            inflater.inflate(R.menu.orgadmin_menu, menu);
        }else{
            inflater.inflate(R.menu.general_menu, menu);
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(prefs.getBoolean("isOrgAdmin",false)) {
            switch (item.getItemId()) {
                case R.id.logout_menu_item:
                    logout();
                    break;
                case R.id.profile_menu_item:
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                    break;
                case R.id.create_team_menu_item:
                    startActivityForResult(new Intent(getActivity(), CreateTeamActivity.class), 2);
                    break;
            }
        }else{
            switch (item.getItemId()) {
                case R.id.logout_menu_item:
                    logout();
                    break;
                case R.id.profile_menu_item:
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2){
            loadTeamsForUser(userID,company);
        }else{
            Snackbar.make(rootLayout,"Unable to create team, try again",Snackbar.LENGTH_SHORT).show();
        }
    }

    private void loadCompany(final String userID, final String company) {
        CollectionReference collectionReference=db.collection("companies");
        collectionReference.whereArrayContains("members",userID)
                .whereEqualTo("name",company).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    try {
                        if (task.getResult().size() == 1) {
                            Log.d(TAG, "onComplete: list size one");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                UsersOfCompanyModel usersOfCompanyModel = new UsersOfCompanyModel();
                                usersOfCompanyModel.setUserID(documentSnapshot.getId());
                                usersOfCompanyModel.setIsTopic(1);
                                usersOfCompanyModel.setTopicName(documentSnapshot.get("name").toString());
                                usersOfCompanyModel.setRegistrationToken(documentSnapshot.getId());
                                usersOfCompanyModel.setName(documentSnapshot.get("name").toString());
                                list.add(0,usersOfCompanyModel);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "onComplete: Member present in two companies");
                        }
                    }catch (Exception e){
                        Log.d(TAG, "onComplete: Error: "+e.getLocalizedMessage());
                    }
                }
            }
        });
    }

    private void loadTeamsForUser(final String userID, final String company) {
        CollectionReference collectionReference=db.collection("teams");
        Query query=collectionReference.whereEqualTo("organization", company)
                .whereArrayContains("subscribed_tokens",userID);
        try {
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                            UsersOfCompanyModel usersOfCompanyModel=new UsersOfCompanyModel();
                            usersOfCompanyModel.setUserID(documentSnapshot.getId());
                            usersOfCompanyModel.setIsTopic(1);
                            usersOfCompanyModel.setTopicName(documentSnapshot.get("group_name").toString());
                            usersOfCompanyModel.setRegistrationToken(documentSnapshot.getId());
                            usersOfCompanyModel.setName(documentSnapshot.get("group_name").toString());
                            if(documentSnapshot.get("lead_id").toString().equals(userID)) {
                                usersOfCompanyModel.setUserIsAdmin(true);
                                loadTeamMembers(documentSnapshot.getId(),company, userID);
                            }
                            else{
                                loadAdmins(documentSnapshot.get("lead_id").toString());
                            }
                            list.add(usersOfCompanyModel);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error retrieving docs with exception: " + task.getException());
                    }
                }
            });
        }catch (Exception e){
            Log.d(TAG, "loadTeamsForUser: Exception");
            e.printStackTrace();
        }
    }

    private void loadAdmins(String lead_id) {
        DocumentReference docRef=db.collection("Users").document(lead_id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot docSnap=task.getResult();
                    UsersOfCompanyModel usersOfCompanyModel=new UsersOfCompanyModel();
                    usersOfCompanyModel.setUserID(docSnap.getId());
                    usersOfCompanyModel.setIsTopic(0);
                    usersOfCompanyModel.setTopicName(" ");
                    usersOfCompanyModel.setRegistrationToken(docSnap.get("token").toString());
                    usersOfCompanyModel.setName(docSnap.get("name").toString());
                    //adminsList as global may cause duplicates
                    if(!list.contains(usersOfCompanyModel))
                        adminsList.add(usersOfCompanyModel);
                }
                list.addAll(adminsList);
                adapter.notifyDataSetChanged();
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
                    List<UsersOfCompanyModel> teamMembersList=new ArrayList<>();
                    for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        if(!documentSnapshot.getId().equals(userID)){
                            UsersOfCompanyModel usersOfCompanyModel=new UsersOfCompanyModel();
                            usersOfCompanyModel.setUserID(documentSnapshot.getId());
                            usersOfCompanyModel.setIsTopic(0);
                            usersOfCompanyModel.setTopicName(" ");
                            usersOfCompanyModel.setRegistrationToken(documentSnapshot.get("token").toString());
                            usersOfCompanyModel.setName(documentSnapshot.get("name").toString());
                            if(!list.contains(usersOfCompanyModel))
                                teamMembersList.add(usersOfCompanyModel);
                        }
                    }
                    list.addAll(teamMembersList);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(),LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void init() {
        db=FirebaseFirestore.getInstance();
        list=new ArrayList<>();
        adminsList=new ArrayList<>();
        adapter = new UsersOfCompanyAdapter(list, getActivity());
    }

    private void loadRecyclerView(View view) {
        RecyclerView rv=view.findViewById(R.id.company_users_recycler_view);
        rv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);
        Log.d(TAG, "loadRecyclerView: finished");
    }

}
