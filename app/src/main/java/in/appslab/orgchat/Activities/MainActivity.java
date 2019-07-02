package in.appslab.orgchat.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.appslab.orgchat.Adapters.ChatPagerAdapter;
import in.appslab.orgchat.Fragments.ChatFragment;
import in.appslab.orgchat.Fragments.ProfileFragment;
import in.appslab.orgchat.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static String PREF_NAME="shared values";
    FirebaseFirestore db;
    String userID,token,company;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ChatPagerAdapter adapter;
    private FloatingActionButton fab;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        init();

        if (prefs.getInt("updateAvailable", 0) == 1)
            sendTokenToDB(userID, token);

        getTeams(userID);
    }




    private void init(){
        db=FirebaseFirestore.getInstance();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String nameOfUser=prefs.getString("name","");
        if(getSupportActionBar()!=null)
            getSupportActionBar().setTitle(nameOfUser);

        userID = prefs.getString("username", "");
        token = prefs.getString("registration token", "");
        company = prefs.getString("organization", "");

        adapter = new ChatPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab = (FloatingActionButton)findViewById(R.id.fab_invite);
    }

    private void setupViewPager(ViewPager viewPager){
        adapter.addFragment(ChatFragment.newInstance(),"My Team",0);
        viewPager.setAdapter(adapter);
    }

    private void sendTokenToDB(String userID, String token) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("token",token);
        db=FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(userID)
                .update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Registration Token updated");
                    SharedPreferences.Editor editor=getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                    editor.putInt("updateAvailable",0);
                    editor.apply();
                }
                else
                    Log.d(TAG, "onComplete: Task failed with exception: "+task.getException());
            }
        });
    }

    private void getTeams(String userID) {
        Log.d(TAG, "getTeams: "+userID);
        CollectionReference collectionReference=db.collection("Users");
        collectionReference.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot=task.getResult();
                    try {
                        ArrayList<String> teams=(ArrayList<String>)documentSnapshot.get("team");
                        if (!teams.isEmpty()) {
                            subToTeams(teams);
                        }
                    }catch (Exception e){
                        Log.d(TAG, "onComplete: "+e.getLocalizedMessage());
                    }
                }
            }
        });
    }

    private  void subToTeams(List<String> teams){
        for(String x: teams) {
            final String y=x;
            FirebaseMessaging.getInstance().subscribeToTopic(x)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscription Unsuccessful";
                            if (task.isSuccessful()) {
                                msg = "Subscription Successful to:"+y;
                            }
                            Log.d(TAG, msg);
                        }
                    });
        }
    }
}
