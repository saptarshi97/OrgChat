package in.appslab.orgchat.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import in.appslab.orgchat.R;

import static android.content.Context.MODE_PRIVATE;


public class ProfileEditFragment extends Fragment {
    EditText nameEditText,emailEditText,positionEditText;
    private FirebaseFirestore db;
    private String userID;
    public static String PREF_NAME="shared values";
    boolean successFlag=false;
    public ProfileEditFragment() {
        // Required empty public constructor
    }


    public static ProfileEditFragment newInstance() {
        ProfileEditFragment fragment = new ProfileEditFragment();
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
        View view=inflater.inflate(R.layout.fragment_profile_edit, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        nameEditText=view.findViewById(R.id.name_edit_text);
        emailEditText=view.findViewById(R.id.email_edit_text);
        positionEditText=view.findViewById(R.id.position_edit_text);
        db=FirebaseFirestore.getInstance();
        SharedPreferences prefs=getActivity().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        userID=prefs.getString("username","");
    }

    public boolean storeDetails(){
        if(!nameEditText.getText().toString().isEmpty() && !emailEditText.getText().toString().isEmpty() && !positionEditText.getText().toString().isEmpty()){
            Map<String,Object> data= new HashMap<>();
            data.put("name",nameEditText.getText().toString());
            data.put("email",emailEditText.getText().toString());
            data.put("position",positionEditText.getText().toString());
            db.collection("Users").document(userID).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    successFlag=true;
                }
            });
            return successFlag;
        }
        return false;
    }

}
