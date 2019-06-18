package in.appslab.orgchat.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import in.appslab.orgchat.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG="LoginActivity";
    public static String PREF_NAME="shared values";
    private EditText phoneNumber,vCode;
    private Button sendButton,resendButton, verCodeButton;
    private LinearLayout sendButtonLayout,verifyPhoneLayout;
    private String phoneVerificationID;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;
    private Dialog dialog;
    private FirebaseFirestore db;
    private String currentUserID;
    private TextView mobileTextView, verTextView;
    private boolean isNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db=FirebaseFirestore.getInstance();
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("isNewUser",false));
        }
        initViews();
    }

    private void getDynamicLink(final String pNumber) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        Log.d(TAG, "onSuccess: "+pendingDynamicLinkData);
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            String inviterToken = deepLink.getQueryParameter("data");
                            Log.d(TAG, "onSuccess: "+inviterToken);
                            if(pNumber.equals(inviterToken)) {
                                verifyPhoneLayout.setVisibility(View.GONE);
                                sendButtonLayout.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    public void verifyNumber(View view){
        //TODO Make a call to the database to see if the fellow exists or not
        //TODO inviteStatus -1: Not sent; 0: Sent, Not opened; 1:Verified;
        final String pNumber=phoneNumber.getText().toString();
        DocumentReference docRef=db.collection("Users").document(pNumber);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot docSnap=task.getResult();
                    if(docSnap.exists()){
                        try{
                            if(docSnap.get("isInvited")==null){
                                getDynamicLink(pNumber);
                            }else{
                                if(docSnap.get("isInvited").toString().equals("0")){
                                    getDynamicLink(pNumber);
                                }else if(docSnap.get("isInvited").toString().equals("1")){
                                    verifyPhoneLayout.setVisibility(View.GONE);
                                    sendButtonLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        getDynamicLink(pNumber);
                    }
                }
            }
        });
    }


    public void sendCode(View view){
        Log.d(TAG, "sendCode: Inside");
        String pNumber=phoneNumber.getText().toString();
        Log.d(TAG, "sendCode: "+pNumber);
        setUpVerificationCallbacks();
        setDialog("Requesting OTP");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                pNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);

    }

    private void setUpVerificationCallbacks() {
        Log.d(TAG, "setUpVerificationCallbacks: inside");

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {

                        //resendButton.setEnabled(false);
                        //verCodeButton.setEnabled(false);
                        //Log.d(TAG, "onVerificationCompleted: ");
                        //signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                            if(dialog.isShowing()&& dialog!=null)
                                dialog.dismiss();
                            showAlert(false, R.drawable.ic_error, "SMS Quota exceeded");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: ");
                        phoneVerificationID = verificationId;
                        resendToken = token;

                        verCodeButton.setEnabled(true);
                        sendButton.setEnabled(false);
                        resendButton.setEnabled(true);

                        if(dialog.isShowing()&& dialog!=null)
                            dialog.dismiss();
                    }
                };
    }

    public void resendCode(View view){
        String phoneNumber=this.phoneNumber.getText().toString();
        setUpVerificationCallbacks();
        setDialog("Requesting OTP");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks,
                resendToken);

    }

    public void verifyCode(View view){
        String code=vCode.getText().toString().trim();
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(phoneVerificationID, code);
        setDialog("Verifying OTP");
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            resendButton.setEnabled(false);
                            verCodeButton.setEnabled(false);
                            FirebaseUser user = task.getResult().getUser();
                            //isNew=task.getResult().getAdditionalUserInfo().isNewUser();
                            currentUserID = phoneNumber.getText().toString();//user.getUid();
                            if(dialog.isShowing() && dialog!=null)
                                dialog.dismiss();
                            showAlert(true,R.drawable.ic_done, "OTP Verified !");

                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                if(dialog.isShowing()&& dialog!=null)
                                    dialog.dismiss();
                                showAlert(false,R.drawable.ic_error,"Incorrect OTP !");
                            }
                        }
                    }
                });
    }

    private void initViews(){
        phoneNumber=findViewById(R.id.phone_number);
        vCode=findViewById(R.id.verification_code);

        sendButtonLayout=findViewById(R.id.send_code_layout);
        verifyPhoneLayout=findViewById(R.id.verify_number_layout);

        sendButton=findViewById(R.id.send_code);
        resendButton=findViewById(R.id.resend_code);
        verCodeButton=findViewById(R.id.verify_code);

        verCodeButton.setEnabled(false);
        resendButton.setEnabled(false);
        mobileTextView=findViewById(R.id.mobile_tv);
        verTextView=findViewById(R.id.ver_tv);
        fbAuth=FirebaseAuth.getInstance();

    }

    private void setDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.item_progress,null);
        builder.setView(view);
        TextView t=view.findViewById(R.id.loading_msg);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showAlert(final boolean moveForward, int icon, String message){
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setIcon(icon)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(moveForward) {
                            checkAndStart(currentUserID);
                            SharedPreferences.Editor editor=getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                            editor.putString("username",currentUserID);
                            editor.apply();
                        }
                    }
                }).show();
    }

    private void checkAndStart(final String uid) {
        DocumentReference docRef = db.collection("Users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        try {
                            SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                            editor.putString("username", uid);
                            editor.putString("organization", document.get("organization").toString());
                            editor.putString("organizationID", document.get("organizationID").toString());
                            editor.putString("name", document.get("name").toString());
                            if(document.get("isOrgAdmin")!=null){
                                if(document.getBoolean("isOrgAdmin")){
                                    editor.putBoolean("isOrgAdmin", true);
                                }
                            }
                            editor.apply();
                        }catch (Exception e){
                            Log.d(TAG, "onComplete: Error: "+e.getLocalizedMessage());
                        }
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        Log.d("MainActivity", "onComplete: ");
                    } else {
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        Log.d("MainActivity", "No such document");
                    }
                } else {
                    Log.d("MainActivity", "get failed with ", task.getException());
                }
            }
        });
    }
}
