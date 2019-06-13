package in.appslab.orgchat.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.appslab.orgchat.Models.TeamSpinnerModel;
import in.appslab.orgchat.R;

public class CreateInviteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ActionBar actionBar;
    EditText nameEditText,emailEditText,phoneEditText;
    Spinner team;
    String selectedTeam="NA",userID,token,company, selectedTeamID="NA", organizationID;
    List<TeamSpinnerModel> teamsList=new ArrayList<>();
    public static String PREF_NAME="shared values";
    public static String TAG="CreateInviteActivity";
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invite);
        init();

        actionBar = getSupportActionBar();

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void getInvite(View view){
        if(validateFields()){
            String name,email,phone;

            name=nameEditText.getText().toString();
            email=emailEditText.getText().toString();
            phone=phoneEditText.getText().toString();
            updateDB(name,email,phone);
            generateContentLink(constructURL(phone));
        }

    }

    private void updateDB(String name, String email, String phone) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",name);
        data.put("email",email);
        data.put("organization",company);
        data.put("organizationID",organizationID);
        data.put("token","NA");
        data.put("team", Arrays.asList(selectedTeamID));

        //Updating users collection here
        db.collection("Users").document(phone).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Users collection updated");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Error updating Users collection");
            }
        });

        //Updating teams collection here
        db.collection("teams").document(selectedTeamID).update("subscribed_tokens", FieldValue.arrayUnion(phone)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: teams collection updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onSuccess: error updating teams collection");
            }
        });

        //Updating companies collection here
        db.collection("companies").document(organizationID).update("members", FieldValue.arrayUnion(phone)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: companies collection updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Error updating companies collection");
            }
        });
    }

    private boolean validateFields(){
        if(nameEditText.getText().toString().isEmpty()){
            nameEditText.requestFocus();
            showAlert("Name field cannot be empty");
        }else if(emailEditText.getText().toString().isEmpty()){
            emailEditText.requestFocus();
            showAlert("Email field cannot be empty");
        }else if(phoneEditText.getText().toString().isEmpty() || phoneEditText.getText().toString().length()<10 || phoneEditText.getText().toString().length()>10){
            phoneEditText.requestFocus();
            showAlert("Phone field is either empty or length is incorrect");
        }else if(selectedTeamID.equals("NA")){
            showAlert("Please select team");
        }else{
            return true;
        }
        return false;
    }

    private void showAlert(String message){
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setIcon(R.drawable.ic_error)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }

    private Uri constructURL(String pNumber) {
        Uri baseURI=Uri.parse("https://salesdiary.in");
        Uri APP_URI = baseURI.buildUpon().appendQueryParameter("data", pNumber).build();
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

    public void generateContentLink(Uri uri) {
        Task<ShortDynamicLink> link=FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(uri)
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

    private void init(){
        db=FirebaseFirestore.getInstance();
        nameEditText=findViewById(R.id.invitee_name_edit_text);
        emailEditText=findViewById(R.id.invitee_email_edit_text);
        phoneEditText=findViewById(R.id.invitee_contact_edit_text);;
        team=findViewById(R.id.invitee_team_spinner);
        SharedPreferences prefs=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        userID = prefs.getString("username", "");
        token = prefs.getString("registration token", "");
        company = prefs.getString("organization", "");
        organizationID=prefs.getString("organizationID", "");
        setAdapterForSpinner();
        team.setOnItemSelectedListener(this);
    }

    private void setAdapterForSpinner() {
        Query query=db.collection("teams").whereEqualTo("company",company).whereEqualTo("lead_id",userID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot x: task.getResult()){
                        teamsList.add(new TeamSpinnerModel(x.get("group_name").toString(),x.getId()));
                    }
                    ArrayAdapter userAdapter = new ArrayAdapter(CreateInviteActivity.this, android.R.layout.simple_spinner_item, teamsList);
                    userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    team.setAdapter(userAdapter);
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        TeamSpinnerModel member=(TeamSpinnerModel)arg0.getSelectedItem();
        selectedTeam =member.getTeamName();
        selectedTeamID=member.getTeamID();
        Log.d(TAG, "onItemSelected: "+selectedTeam);
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
