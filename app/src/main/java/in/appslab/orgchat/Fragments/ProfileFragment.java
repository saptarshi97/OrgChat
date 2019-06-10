package in.appslab.orgchat.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import in.appslab.orgchat.R;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore db;
    private String userID;
    public static String PREF_NAME="shared values";
    private TextView nameTv,phoneTv,emailTv,positionTv;
    private String currentUserID;

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
        return view;
    }

    private void init(View view) {
        nameTv=view.findViewById(R.id.display_details_name_text_view);
        phoneTv=view.findViewById(R.id.details_display_phone_text_view);
        emailTv=view.findViewById(R.id.details_display_email_text_view);
        positionTv=view.findViewById(R.id.details_display_position_text_view);
    }

    public void setData(String userID){

    }

}
