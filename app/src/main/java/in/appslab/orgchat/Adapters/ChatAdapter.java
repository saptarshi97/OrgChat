package in.appslab.orgchat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import in.appslab.orgchat.Activities.TokenChatActivity;
import in.appslab.orgchat.Models.ChatModel;
import in.appslab.orgchat.R;

/**
 * Created by Saptarshi on 6/17/2019.
 */
public class ChatAdapter extends RecyclerView.Adapter {
    List<ChatModel> chatModelList;
    private static final String TAG ="TokenChatAdapter";
    private static final int VIEW_TYPE_MESSAGE_SENT = 0;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;
    public static String PREF_NAME="shared values";
    Activity activity;
    private String selfID;
    private ActionModeInterface actionModeInterface;
    public interface ActionModeInterface{
        public void onClickHandler(int position);
        public void onLongClickHandler(int position);
        public boolean setSelectionColor(ChatModel chat);
    }


    public ChatAdapter(List<ChatModel> chatModelList, Activity activity, ActionModeInterface actionModeInterface){
        this.chatModelList=chatModelList;
        this.activity=activity;
        this.actionModeInterface=actionModeInterface;
        SharedPreferences prefs=activity.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        this.selfID=prefs.getString("username","");

    }

    @Override
    public int getItemViewType(int position) {
        ChatModel chatObj=chatModelList.get(position);

        Log.d(TAG, "getItemViewType: position"+position);
        Log.d(TAG, "getItemViewType: "+selfID);
        Log.d(TAG, "getItemViewType: "+chatObj.getSender());
        Log.d(TAG, "getItemViewType: few of the data "+chatObj.getReceiver()+" "+chatObj.getTimestamp()+" "+chatObj.getChatMessage());

        if(chatObj.getSender().equals(selfID))
            return  VIEW_TYPE_MESSAGE_SENT;
        else
            return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view;
        if(i==VIEW_TYPE_MESSAGE_SENT){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent,parent,false);
            return new SentMessageHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_RECEIVED){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received,parent,false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatModel chatObj = chatModelList.get(position);
        Log.d(TAG, "onBindViewHolder: position: " + position);
        Log.d(TAG, "onBindViewHolder: list size: " + chatModelList.size());
        switch (holder.getItemViewType()){
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(chatObj);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        TextView messageText;
        View v;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_right_msg_text_view);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            actionModeInterface.onLongClickHandler(getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            actionModeInterface.onClickHandler(getAdapterPosition());
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        TextView messageText, userIDText;
        View v;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_left_msg_text_view);
            userIDText= itemView.findViewById(R.id.user_id);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            userIDText.setText(chat.getSender());
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            actionModeInterface.onLongClickHandler(getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            actionModeInterface.onClickHandler(getAdapterPosition());
        }
    }
}
