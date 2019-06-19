package in.appslab.orgchat.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Models.CreateTeamModel;
import in.appslab.orgchat.R;

/**
 * Created by Saptarshi on 6/19/2019.
 */
public class MultiSelectAdapter extends RecyclerView.Adapter<MultiSelectAdapter.MultiViewHolder> {

    private Context context;
    private List<CreateTeamModel> members;
    private ModifyActivityViewVisibility inter;

    public interface ModifyActivityViewVisibility{
        public void changeState(int state);
    }

    public MultiSelectAdapter(Context context, List<CreateTeamModel> members, ModifyActivityViewVisibility inter) {
        this.context = context;
        this.members = members;
        this.inter=inter;
    }

    @NonNull
    @Override
    public MultiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_team_member_select, viewGroup, false);
        return new MultiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiViewHolder multiViewHolder, int position) {
        multiViewHolder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MultiViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView userIDTextView;
        private ImageView imageView;

        MultiViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.team_select_name_text_view);
            userIDTextView = itemView.findViewById(R.id.team_select_phone_text_view);
            imageView = itemView.findViewById(R.id.selected_image_view);
        }

        void bind(final CreateTeamModel member) {
            imageView.setVisibility(member.isChecked() ? View.VISIBLE : View.GONE);
            nameTextView.setText(member.getName());
            userIDTextView.setText(member.getUserID());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    member.setChecked(!member.isChecked());
                    imageView.setVisibility(member.isChecked() ? View.VISIBLE : View.GONE);
                    if(getSelected().size()>0)
                        inter.changeState(View.VISIBLE);
                    else
                        inter.changeState(View.GONE);
                }
            });
        }
    }

    public List<CreateTeamModel> getSelected() {
        List<CreateTeamModel> selected = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).isChecked()) {
                selected.add(members.get(i));
            }
        }
        return selected;
    }
}