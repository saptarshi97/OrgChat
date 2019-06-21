package in.appslab.orgchat.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.R;

/**
 * Created by Saptarshi on 6/20/2019.
 */
public class ModifyTeamAdapter extends RecyclerView.Adapter<ModifyTeamAdapter.ModViewHolder>{
    private Context context;
    private List<CreateTeamModel> members;
    private ModificationOptionsInterface optionsInterface;

    public interface ModificationOptionsInterface{
        public void showOptions(String userID, String name, String token);
    }

    public ModifyTeamAdapter(Context context, List<CreateTeamModel> members, ModificationOptionsInterface optionsInterface){
        this.context = context;
        this.members = members;
        this.optionsInterface=optionsInterface;
    }

    @NonNull
    @Override
    public ModViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_team_member_select, viewGroup, false);
        return new ModViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModViewHolder multiViewHolder, int position) {
        multiViewHolder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class ModViewHolder extends RecyclerView.ViewHolder{
        private TextView nameTextView;
        private TextView userIDTextView;

        ModViewHolder(@NonNull View itemView){
            super(itemView);
            nameTextView = itemView.findViewById(R.id.team_select_name_text_view);
            userIDTextView = itemView.findViewById(R.id.team_select_phone_text_view);
            userIDTextView.setVisibility(View.GONE);
        }
        void bind(final CreateTeamModel member){
            nameTextView.setText(member.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    optionsInterface.showOptions(member.getUserID(),member.getName(),member.getUserToken());
                }
            });
        }
    }
}
