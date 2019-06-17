package in.appslab.orgchat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.appslab.orgchat.Activities.MainActivity;
import in.appslab.orgchat.Activities.TokenChatActivity;
import in.appslab.orgchat.Activities.TopicChatActivity;
import in.appslab.orgchat.Models.UsersOfCompanyModel;
import in.appslab.orgchat.R;

/**
 * Created by Saptarshi on 5/21/2019.
 */
public class UsersOfCompanyAdapter extends RecyclerView.Adapter<UsersOfCompanyAdapter.UsersOfCompanyViewHolder> {
    private List<UsersOfCompanyModel> usersOfCompanyList;
    private Activity activity;
    private static final String TAG = "UsersOfCompanyAdapter";

    public UsersOfCompanyAdapter(List<UsersOfCompanyModel> usersOfCompanyList, Activity activity) {
        this.usersOfCompanyList = usersOfCompanyList;
        this.activity=activity;
    }

    @NonNull
    @Override
    public UsersOfCompanyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UsersOfCompanyViewHolder( LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_company_users,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(UsersOfCompanyViewHolder viewHolder, int i) {
        UsersOfCompanyModel usersOfCompanyObj=usersOfCompanyList.get(i);
        viewHolder.onBind(usersOfCompanyObj);
    }

    @Override
    public int getItemCount() {
        return usersOfCompanyList.size();
    }

    class UsersOfCompanyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView initialsTextView;
        TextView nameTextView;
        TextView subtitleTextView;
        public UsersOfCompanyViewHolder(View itemView){
            super(itemView);
            initialsTextView=itemView.findViewById(R.id.user_initial_text_view);
            nameTextView=itemView.findViewById(R.id.name_text_view);
            subtitleTextView=itemView.findViewById(R.id.subtitle_text_view);
            itemView.setOnClickListener(this);
        }
        public void onBind(UsersOfCompanyModel usersOfCompanyModelObj){
            nameTextView.setText(usersOfCompanyModelObj.getName());
            initialsTextView.setText(usersOfCompanyModelObj.getName().charAt(0)+"");
        }
        @Override
        public void onClick(View view){ //TODO replace fragment based on token or topic
            Log.d(TAG, "onClick: called");
            UsersOfCompanyModel user=usersOfCompanyList.get(getLayoutPosition());
            Fragment chatFragment;//
            Bundle bundle=new Bundle();
            Intent intent;

            //TODO Change bundle to intent
            if(user.getIsTopic()==0) {
                bundle.putString("registrationToken", user.getRegistrationToken());
                bundle.putString("name", user.getName());
                bundle.putString("userID",user.getUserID());
                intent=new Intent(activity,TokenChatActivity.class);
                intent.putExtras(bundle);
            }else{
                bundle.putString("topic", user.getUserID());
                bundle.putString("name", user.getName());
                bundle.putString("userID",user.getUserID());
                intent=new Intent(activity,TopicChatActivity.class);
                intent.putExtras(bundle);
            }

            activity.startActivity(intent);
        }
    }
}
