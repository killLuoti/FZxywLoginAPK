package com.iotluo.baipiaoliuliang.Service;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;


public class BootstarpService extends Service {

    private Context con;
    public static String text="123";
    private NotificationUtil nu;


    @Override
    public void onCreate() {
        super.onCreate();
        con = getApplicationContext();
        if(MainActivity.getContext() != null){
            NotificationUtil.sendNotification(MainActivity.getContext(),"新消息通知",
                "校园网自动登录程序","通知",text);return;}
        startForeground("校园网");

        // stop self to clear the notification
//        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("luoluo", "123123");
        stopForeground(true);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void startForeground( String title) {
        //增加一個渠道，ID不重复即可
        String CHANNEL_ID = "fspt.net";
        String CHANNEL_NAME = "新消息通知";
        String description = "校园网自动登录程序";
        int notifiId = (int) System.currentTimeMillis();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID);
//Android 8.0需要增加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("luoluo", "tongzhi: #####");
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
//            channel.setDescription(description);
//            channel.setLightColor(Color.RED);
//            channel.enableVibration(true);
//            channel.setShowBadge(false);//是否显示角标
//            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//            notificationManager.createNotificationChannel(channel);
            channel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            channel.setShowBadge(true);//是否显示角标
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            //如果是小于5.0系统的，设置原图
            notificationBuilder.setSmallIcon(R.drawable.ic_action_name);
        }else{
            //如果是大于等于5.0系统的，设置透明图
            notificationBuilder.setSmallIcon(R.drawable.ic_action_name);
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
                //如果小于7.0系统,设置背景色
                notificationBuilder.setColor(Color.RED);
            }
        }


        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);



        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setContentTitle(text);
//        notificationBuilder.setContentTitle(title);
//        notificationBuilder.setContentText(text);
        notificationBuilder.setTicker(text);
        notificationBuilder.setAutoCancel(true);//点击之后消失
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND );
//        notificationManager.notify(notifiId, notificationBuilder.build());
        notificationManager.cancel(R.drawable.ic_action_name);
        startForeground(notifiId, notificationBuilder.build());

    }

}
