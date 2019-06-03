package in.appslab.orgchat.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import in.appslab.orgchat.Activities.LoginActivity;
import in.appslab.orgchat.Models.ChatModel;
import in.appslab.orgchat.R;
import io.realm.Realm;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    public static String PREF_NAME="shared values";
    private LocalBroadcastManager broadcaster;
    private String selfID;
    private final String LAUNCH_APPLICATION = "Launch Chat";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref=getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        selfID=pref.getString("username","");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            updateRealm(remoteMessage, selfID);
        }

        //Messages must positively not have a notification object
    }

    private void updateRealm(final RemoteMessage remoteMessage, final String selfID) {
        Handler handler=new Handler(Looper.getMainLooper());
        final  Runnable runnable=new Runnable() {
            @Override
            public void run() {
                final Realm realm=Realm.getDefaultInstance();
                realm.executeTransactionAsync(new Realm.Transaction() {
                                                  @Override
                                                  public void execute(Realm realm) {
                                                      realm.where(ChatModel.class).findAll();
                                                      List<ChatModel> chatModelList=new ArrayList<>();
                                                      if(remoteMessage.getData().get("isTopic").equals("0")) {
                                                          chatModelList.add(new ChatModel(remoteMessage.getData().get("message"), remoteMessage.getData().get("time"), remoteMessage.getData().get("senderID"), selfID, "", 0));
                                                          Log.d(TAG, "execute: message from: "+remoteMessage.getData().get("senderID"));
                                                      }
                                                      else
                                                          chatModelList.add(new ChatModel(remoteMessage.getData().get("message"), remoteMessage.getData().get("time"), remoteMessage.getData().get("senderID"),selfID,remoteMessage.getData().get("topicName"),1));
                                                      realm.insert(chatModelList);
                                                  }
                                              },
                        new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                Intent intent = new Intent("data-from-service");
                                intent.putExtra("newDataAvailable",1);
                                broadcaster.sendBroadcast(intent);
                                realm.close();
                                sendNotification(remoteMessage.getData().get("message"));
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {

                            }
                        });

            }
        };
        handler.postDelayed(runnable,250);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int notifyId = 0;
            String channelId = "AppsLabs_Chat";
            CharSequence name = "Chat Channel";
            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("New Message")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_stat_ic_notifications)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .addAction(new NotificationCompat.Action(0, LAUNCH_APPLICATION, pendingIntent))
                    .setChannelId(channelId)
                    .build();

            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(notifyId, notification);

        } else {
            Notification notify = new NotificationCompat.Builder(this)
                    .setContentTitle("New Message")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_stat_ic_notifications)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .addAction(new NotificationCompat.Action(0, LAUNCH_APPLICATION, pendingIntent))
                    .build();

            notificationManager.notify(0, notify);
        }
    }


    @Override
    public void onNewToken(String s) {
        SharedPreferences.Editor editor=getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putInt("updateAvailable",1);
        editor.putString("registration token",s);
        editor.apply();
        Log.d(TAG, "onNewToken: "+s);
    }
}
