package in.appslab.orgchat.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import in.appslab.orgchat.R;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore db;
    private String userID;
    public static String PREF_NAME="shared values";
    private TextView nameTv,phoneTv,emailTv,positionTv,organizationTv;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
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
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        setData(userID);
        return view;
    }

    private void init(View view) {
        db=FirebaseFirestore.getInstance();
        nameTv=view.findViewById(R.id.display_details_name_text_view);
        phoneTv=view.findViewById(R.id.details_display_phone_text_view);
        emailTv=view.findViewById(R.id.details_display_email_text_view);
        positionTv=view.findViewById(R.id.details_display_position_text_view);
        organizationTv=view.findViewById(R.id.details_display_organization_text_view);
        positionTv.setVisibility(View.GONE);
        SharedPreferences prefs=getActivity().getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        userID=prefs.getString("username","");
    }

    public void setData(String userID){
        db.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot docSnap=task.getResult();
                    if(docSnap.exists()){
                        if(docSnap.get("name")!=null){
                            nameTv.setText(docSnap.get("name").toString());
                        }
                        if(docSnap.get("email")!=null){
                            emailTv.setText(docSnap.get("email").toString());
                        }
                        if(docSnap.get("organization")!=null){
                            organizationTv.setText(docSnap.get("organization").toString());
                        }
                        if(docSnap.get("position")!=null){
                            //TODO add code here
                        }
                        phoneTv.setText(docSnap.getId());
                    }
                }
            }
        });
    }

}
