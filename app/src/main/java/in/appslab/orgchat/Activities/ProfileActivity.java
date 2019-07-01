package in.appslab.orgchat.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import in.appslab.orgchat.Adapters.ProfilePagerAdapter;
import in.appslab.orgchat.Custom.CustomPager;
import in.appslab.orgchat.Fragments.ProfileEditFragment;
import in.appslab.orgchat.Fragments.ProfileFragment;
import in.appslab.orgchat.R;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private ActionBar actionBar;
    private CustomPager pager;
    private FloatingActionButton fab;
    private final long ID_DETAILS_INPUT=0;
    private final long ID_DETAILS_DISPLAY=1;
    public static String PREF_NAME="shared values";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        actionBar = getSupportActionBar();

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        final String userID=prefs.getString("username","");

        fab = (FloatingActionButton)findViewById(R.id.profile_fab);
        pager = (CustomPager)findViewById(R.id.profile_view_pager);
        final RelativeLayout parentLayout = (RelativeLayout)findViewById(R.id.activity_profile);
        final ProfileFragment profileFragment= new ProfileFragment();
        final ProfileEditFragment profileEditFragment= new ProfileEditFragment();

        final Button button = (Button)findViewById(R.id.save_button);

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(profileFragment, ID_DETAILS_DISPLAY);
        adapter.addFragment(profileEditFragment, ID_DETAILS_INPUT);
        pager.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileEditFragment.storeDetails()) {
                    profileFragment.setData(userID);
                    pager.setCurrentItem(0);
                    button.setVisibility(View.GONE);
                    fab.show();
                }
                else{
                    Toast.makeText(ProfileActivity.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1);
                fab.hide();
                button.setVisibility(View.VISIBLE);
            }
        });

        if(prefs.getString("organization","").isEmpty() || prefs.getString("name","").isEmpty()){
            pager.setCurrentItem(1);
            fab.hide();
            button.setVisibility(View.VISIBLE);
        }else{
            pager.setCurrentItem(0);
            fab.show();
            button.setVisibility(View.GONE);
        }

    }
}
