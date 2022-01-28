package com.iotluo.baipiaoliuliang.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;
public final class NotificationUtil {
    private static final String CHANNEL_ONE_ID = "fspt.net"; //唯一性
    private static final int NOTIFY_ID = 0x111;
    private static final int FOREGROUND_ID = 0x112;

    /**
     * 公开使用
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void sendNotification(Context context, String channelName, String channelDesc, String contentTitle, String contentText) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.notify(NOTIFY_ID, getNotificationO(context, manager, channelName, channelDesc, contentTitle, contentText));
        } else {
            manager.notify(NOTIFY_ID, getNotification(context, contentTitle, contentText));
        }
    }

    /**
     * onStartCommand调用
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void startForeground(Service service, String channelName, String channelDesc, String contentTitle, String contentText) {
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = getNotificationO(service, manager, channelName, channelDesc, contentTitle, contentText);
        } else {
            notification = getNotification(service, contentTitle, contentText);
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        service.startForeground(FOREGROUND_ID, notification);
    }

    /**
     * onDestory之前调用
     */
    public static void stopForegound(Service service) {
        service.stopForeground(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Notification getNotificationO(Context context, NotificationManager manager, String name, String desc, String contentTitle, String contentText) {
        Notification.Builder builder;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ONE_ID, name,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(desc);

        //channel.enableLights(true);
        //channel.setLightColor(color);

        //Uri mUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        //channel.setSound(mUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);

        // Register the channel with system; you can't change the importance
        // or other notification behaviors after this
        manager.createNotificationChannel(channel);
        builder = new Notification.Builder(context, CHANNEL_ONE_ID);
        builder.setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(getPendingIntent(context))
                .setAutoCancel(true)//点击之后消失
                .setSmallIcon(R.drawable.ic_action_fzy_logo); //todo

        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Notification getNotification(Context context, String contentTitle, String contentText) {
        Notification.Builder builder = new Notification.Builder(context)
                .setPriority(Notification.PRIORITY_DEFAULT)
                //.setLights(color, 1000, 0)
                //.setSound(null, null);
                ;

        builder.setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(getPendingIntent(context))
                .setAutoCancel(true)//点击之后消失
                .setSmallIcon(R.mipmap.ic_launcher); //todo

        return builder.build();
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

}
