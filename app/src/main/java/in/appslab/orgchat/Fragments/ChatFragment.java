package in.appslab.orgchat.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Adapters.UsersOfCompanyAdapter;
import in.appslab.orgchat.Models.UsersOfCompanyModel;
import in.appslab.orgchat.R;

import static android.content.Context.MODE_PRIVATE;


public class ChatFragment extends Fragment {
    public static String PREF_NAME="shared values";
    List<UsersOfCompanyModel> list;
    List<UsersOfCompanyModel> adminsList;
    private static final String TAG = "CompanyUsersFragment";
    UsersOfCompanyAdapter adapter;
    FirebaseFirestore db;
    boolean isLead=false;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chat, container, false);
        SharedPreferences prefs=getActivity().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        String nameOfUser=prefs.getString("name","");
        try {
            getActivity().setTitle(nameOfUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        init();
        String userID=prefs.getString("username","");
        String company=prefs.getString("organization","");
        String myToken=prefs.getString("registration token","");
        loadRecyclerView(view);
        loadCompany(userID,company);
        loadTeamsForUser(userID,company);
        return view;
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
