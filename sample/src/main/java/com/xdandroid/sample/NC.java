package com.xdandroid.sample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NC {
    private   String CHANNEL_ID;
    private Context con;

    public NC(String CHANNEL_ID, Context con ) {
        this.CHANNEL_ID = CHANNEL_ID;
        this.con = con;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(con,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("通知")
                .setContentText("wifi上线!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//        Bitmap bitmap = BitmapFactory.decodeResource(con.getResources(),
//                R.mipmap.ic_launcher, null);
//        Notification notification = new NotificationCompat.Builder(con, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .setContentTitle("imageTitle")
//                .setContentText("imageDescription")
//                .setStyle(new NotificationCompat.BigPictureStyle()
//                        .bigPicture(bitmap))
//                .build();

//        Intent resultIntent = new Intent(con, MainActivity.class);
//        resultIntent.setAction(Intent.ACTION_MAIN);
//        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(con, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
////building the notification
//        builder.setContentIntent(resultPendingIntent);
//        //更新或创建通知,并注明通知的id
////下面一句是悬浮通知与一般通知的唯一区别
//        builder.setFullScreenIntent(resultPendingIntent,true);
//
//        mNotificationManager.notify(0, builder.build());

    }

    private int id = 1;

    public void notification() {
        Drawable drawable = ContextCompat.getDrawable(con, R.mipmap.ic_launcher);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(con,"0");
        //设置小图标
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        //设置大图标
        mBuilder.setLargeIcon(bitmap);
        //设置标题
        mBuilder.setContentTitle("这是标题");
        //设置通知正文
        mBuilder.setContentText("这是正文，当前ID是：" + id);
        //设置摘要
        mBuilder.setSubText("这是摘要");
        //设置是否点击消息后自动clean
        mBuilder.setAutoCancel(true);
        //在通知的右边设置大的文本。
        mBuilder.setContentInfo("右侧文本");
        //与setContentInfo类似，但如果设置了setContentInfo则无效果
        //用于当显示了多个相同ID的Notification时，显示消息总数
        mBuilder.setNumber(2);
        //通知在状态栏显示时的文本
        mBuilder.setTicker("在状态栏上显示的文本");
        //设置优先级
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //自定义消息时间，以毫秒为单位，当前设置为比系统时间少一小时
        mBuilder.setWhen(System.currentTimeMillis() - 3600000);
        //设置为一个正在进行的通知，此时用户无法清除通知
        mBuilder.setOngoing(true);
        //设置消息的提醒方式，震动提醒：DEFAULT_VIBRATE     声音提醒：NotificationCompat.DEFAULT_SOUND
        //三色灯提醒NotificationCompat.DEFAULT_LIGHTS     以上三种方式一起：DEFAULT_ALL
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        //设置震动方式，延迟零秒，震动一秒，延迟一秒、震动一秒
        mBuilder.setVibrate(new long[]{0, 1000, 1000, 1000});

        Intent intent = new Intent(con, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(con, 0, intent, 0);
        mBuilder.setContentIntent(pIntent);

        NotificationManager mNotificationManager = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id++, mBuilder.build());
    }


    public void cleanNotification(View view) {
        NotificationManager mNotificationManager =
                (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mNotificationManager.cancel(1);
    }



//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = con.getString(R.string.channel_name);
//            String description = con.getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = con.getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }



}
