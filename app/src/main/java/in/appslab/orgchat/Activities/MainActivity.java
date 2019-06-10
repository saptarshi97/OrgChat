package in.appslab.orgchat.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        SharedPreferences prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        userID = prefs.getString("username", "");
        token = prefs.getString("registration token", "");
        company = prefs.getString("company", "");
        if (prefs.getInt("updateAvailable", 0) == 1)
            sendTokenToDB(userID, token);
    }

    private void init(){
        db=FirebaseFirestore.getInstance();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = new ChatPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager){
        adapter.addFragment(ChatFragment.newInstance(),"My Team",0);
        viewPager.setAdapter(adapter);
    }

    private void sendTokenToDB(String userID, String token) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("registration token",token);
        db=FirebaseFirestore.getInstance();
        db.collection("users")
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

    private Uri constructURL() {
        Uri baseURI=Uri.parse("https://salesdiary.in");
        Uri APP_URI = baseURI.buildUpon().appendQueryParameter("data", "9513160907").
                appendQueryParameter("extra1", "value").
                appendQueryParameter("extra2", "value").build();
        String encodedUri = null;
        try {
            encodedUri = URLEncoder.encode(APP_URI.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "constructURL: "+e.getLocalizedMessage());
        }
        Uri deepLink = Uri.parse("https://orgchat.page.link/?link="+encodedUri+"&apn="+"in.appslab.orgchat");
        Log.d(TAG, "constructURL: "+deepLink);
        return deepLink;
    }

    public void generateContentLink() {
        Task<ShortDynamicLink> link=FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(constructURL())
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(task.isSuccessful()){
                            Uri shortLink=task.getResult().getShortLink();
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            String msg=" Here's your invitation link: "+shortLink.toString();
                            intent.putExtra(Intent.EXTRA_TEXT, msg);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, "Share Link"));
                        }
                    }
                });
    }
}
