package in.appslab.orgchat.Activities;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import in.appslab.orgchat.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static String PREF_NAME="shared values";
    FirebaseFirestore db;
    Toolbar toolbar;
    String userID,token,company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db=FirebaseFirestore.getInstance();

        SharedPreferences prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        userID=prefs.getString("username","");
        token=prefs.getString("registration token","");
        company=prefs.getString("company","");
        if(prefs.getInt("updateAvailable",0)==1)
            sendTokenToDB(userID,token);
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
}
