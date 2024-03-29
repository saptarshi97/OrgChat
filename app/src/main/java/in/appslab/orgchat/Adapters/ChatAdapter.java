package in.appslab.orgchat.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
    private static final int VIEW_TYPE_MESSAGE_SENT_REPLY=2;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_REPLY=3;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_IMAGE=4;
    private static final int VIEW_TYPE_MESSAGE_SENT_IMAGE=5;
    private static final int VIEW_TYPE_MESSAGE_SENT_IMAGE_REPLY=6;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED_IMAGE_REPLY=7;
    public static String PREF_NAME="shared values";
    Activity activity;
    private String selfID;
    private ActionModeInterface actionModeInterface;
    public interface ActionModeInterface{
        public void onClickHandler(int position);
        public void onLongClickHandler(int position);
        public boolean setSelectionColor(ChatModel chat);
        public void replyClickHandler(int position);
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

        if(chatObj.getSender().equals(selfID) && chatObj.getChatMessage()==null && chatObj.getDownloadUri()!=null)
            return VIEW_TYPE_MESSAGE_SENT_IMAGE;
        else if(!chatObj.getSender().equals(selfID) && chatObj.getChatMessage()==null && chatObj.getDownloadUri()!=null){
            return VIEW_TYPE_MESSAGE_RECEIVED_IMAGE;
        }
        else if(chatObj.getSender().equals(selfID) && chatObj.getQuotedMessageId()==null)
            return  VIEW_TYPE_MESSAGE_SENT;
        else if(!chatObj.getSender().equals(selfID) && chatObj.getQuotedMessageId()==null)
            return VIEW_TYPE_MESSAGE_RECEIVED;
        else if(chatObj.getSender().equals(selfID) && chatObj.getQuotedMessageId()!=null) {
            int pos = getObjPos(chatObj.getQuotedMessageId());
            if(chatModelList.get(pos).getDownloadUri()==null) {
                return VIEW_TYPE_MESSAGE_SENT_REPLY;
            }else
                return VIEW_TYPE_MESSAGE_SENT_IMAGE_REPLY;
        }
        else if(!chatObj.getSender().equals(selfID) && chatObj.getQuotedMessageId()!=null) {
            int pos = getObjPos(chatObj.getQuotedMessageId());
            try {
                if (chatModelList.get(pos).getDownloadUri() == null) {
                    return VIEW_TYPE_MESSAGE_RECEIVED_REPLY;
                } else
                    return VIEW_TYPE_MESSAGE_RECEIVED_IMAGE_REPLY;
            }catch (Exception e){
                Log.d(TAG, "getItemViewType: error"+e.getLocalizedMessage());
            }
            return VIEW_TYPE_MESSAGE_RECEIVED_REPLY;
        }
        else
            return -1;
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
        }else if(i==VIEW_TYPE_MESSAGE_SENT_REPLY){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_reply_sent,parent,false);
            return new SentReplyHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_RECEIVED_REPLY){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_reply_received,parent,false);
            return new ReceivedReplyHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_SENT_IMAGE){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_sent,parent,false);
            return new SentImageHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_RECEIVED_IMAGE){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_received,parent,false);
            return new ReceivedImageHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_SENT_IMAGE_REPLY){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_reply_sent,parent,false);
            return new SentImageReplyHolder(view);
        }else if(i==VIEW_TYPE_MESSAGE_RECEIVED_IMAGE_REPLY){
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_reply_received,parent,false);
            return new ReceivedImageReplyHolder(view);
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
            case VIEW_TYPE_MESSAGE_SENT_REPLY:
                ((SentReplyHolder)holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_REPLY:
                ((ReceivedReplyHolder)holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_SENT_IMAGE:
                ((SentImageHolder)holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_IMAGE:
                ((ReceivedImageHolder)holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_SENT_IMAGE_REPLY:
                ((SentImageReplyHolder)holder).bind(chatObj);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED_IMAGE_REPLY:
                ((ReceivedImageReplyHolder)holder).bind(chatObj);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    private int getObjPos(String id){
        try {
            for (int i = 0; i < chatModelList.size(); i++)
                if (chatModelList.get(i).getMessageId().equals(id))
                    return i;
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
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
            }else{
                v.setBackgroundResource(0);
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
            messageText = itemView.findViewById(R.id.chat_left_msg_text_view);
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
            }else{
                v.setBackgroundResource(0);
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

    private class ReceivedReplyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        LinearLayout leftReplyLayout;
        TextView messageText, userIDText,leftReplyTextView;
        View v,div;

        ReceivedReplyHolder(View itemView){
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.chat_reply_msg_text_view);
            userIDText= itemView.findViewById(R.id.reply_user_id);
            leftReplyLayout=itemView.findViewById(R.id.left_reply_layout);
            leftReplyTextView=itemView.findViewById(R.id.left_reply_textview);
            div=itemView.findViewById(R.id.left_reply_divider);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            userIDText.setText(chat.getSender());
            if(chat.getQuotedMessageId()!=null){
                final int pos=getObjPos(chat.getQuotedMessageId());
                if(pos!=-1) {
                    div.setVisibility(View.VISIBLE);
                    leftReplyLayout.setVisibility(View.VISIBLE);
                    try {
                        leftReplyTextView.setText(chatModelList.get(pos).getChatMessage());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    leftReplyLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actionModeInterface.replyClickHandler(pos);
                        }
                    });
                }else{
                    div.setVisibility(View.GONE);
                    leftReplyLayout.setVisibility(View.GONE);
                }
            }
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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

    private class SentReplyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        LinearLayout rightReplyLayout;
        TextView messageText,rightReplyTextview;
        View v,div;

        SentReplyHolder(View itemView){
            super(itemView);
            rightReplyLayout=itemView.findViewById(R.id.right_reply_layout);
            rightReplyTextview=itemView.findViewById(R.id.right_reply_textview);
            div=itemView.findViewById(R.id.right_reply_divider);
            messageText =itemView.findViewById(R.id.chat_right_reply_msg_text_view);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            if(chat.getQuotedMessageId()!=null){
                final int pos=getObjPos(chat.getQuotedMessageId());
                if(pos!=-1) {
                    div.setVisibility(View.VISIBLE);
                    try {
                        rightReplyTextview.setText(chatModelList.get(pos).getChatMessage());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    rightReplyLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actionModeInterface.replyClickHandler(pos);
                        }
                    });
                }else{
                    div.setVisibility(View.GONE);
                    rightReplyLayout.setVisibility(View.GONE);
                }
            }
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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

    private class SentImageHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        ImageView sentImage;
        View v;

        SentImageHolder(View itemView) {
            super(itemView);
            sentImage=itemView.findViewById(R.id.right_chat_image);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            Picasso.get().load(Uri.parse(chat.getDownloadUri())).into(sentImage);
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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

    private class ReceivedImageHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        ImageView receivedImage;
        TextView senderTextView;
        View v;

        ReceivedImageHolder(View itemView) {
            super(itemView);
            receivedImage=itemView.findViewById(R.id.left_chat_image);
            senderTextView=itemView.findViewById(R.id.received_image_user_id);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            senderTextView.setText(chat.getSender());
            Picasso.get().load(Uri.parse(chat.getDownloadUri())).into(receivedImage);
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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

    private class ReceivedImageReplyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        LinearLayout leftImageReplyLayout;
        TextView messageText, userIDText;
        ImageView replyImage;
        View v,div;

        ReceivedImageReplyHolder(View itemView){
            super(itemView);
            messageText=itemView.findViewById(R.id.left_image_reply_msg_text_view);
            userIDText= itemView.findViewById(R.id.image_reply_user_id);
            leftImageReplyLayout=itemView.findViewById(R.id.image_reply_left_layout);
            div=itemView.findViewById(R.id.left_image_reply_divider);
            replyImage=itemView.findViewById(R.id.left_image_view);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            userIDText.setText(chat.getSender());
            if(chat.getQuotedMessageId()!=null){
                final int pos=getObjPos(chat.getQuotedMessageId());
                if(pos!=-1) {
                    div.setVisibility(View.VISIBLE);
                    leftImageReplyLayout.setVisibility(View.VISIBLE);
                    Picasso.get().load(Uri.parse(chatModelList.get(pos).getDownloadUri())).into(replyImage);
                    leftImageReplyLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actionModeInterface.replyClickHandler(pos);
                        }
                    });
                }else{
                    div.setVisibility(View.GONE);
                    leftImageReplyLayout.setVisibility(View.GONE);
                }
            }
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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

    private class SentImageReplyHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{
        LinearLayout rightImageReplyLayout;
        TextView messageText;
        ImageView rightReplyImage;
        View v,div;

        SentImageReplyHolder(View itemView){
            super(itemView);
            rightImageReplyLayout=itemView.findViewById(R.id.right_image_reply_layout);
            div=itemView.findViewById(R.id.right_image_reply_divider);
            messageText =itemView.findViewById(R.id.right_image_reply_msg_text_view);
            rightReplyImage=itemView.findViewById(R.id.right_image_view);
            v=itemView;
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        void bind(ChatModel chat) {
            messageText.setText(chat.getChatMessage());
            if(chat.getQuotedMessageId()!=null){
                final int pos=getObjPos(chat.getQuotedMessageId());
                if(pos!=-1) {
                    div.setVisibility(View.VISIBLE);
                    Picasso.get().load(Uri.parse(chatModelList.get(pos).getDownloadUri())).into(rightReplyImage);
                    rightImageReplyLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actionModeInterface.replyClickHandler(pos);
                        }
                    });
                }else{
                    div.setVisibility(View.GONE);
                    rightImageReplyLayout.setVisibility(View.GONE);
                }
            }
            if(actionModeInterface.setSelectionColor(chat)){
                v.setBackgroundResource(R.color.selection);
            }else{
                v.setBackgroundResource(0);
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
