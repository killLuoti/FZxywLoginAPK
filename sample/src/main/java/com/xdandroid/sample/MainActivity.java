package com.xdandroid.sample;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.xdandroid.hellodaemon.*;

public class MainActivity extends Activity {
    private NotificationManager manager;
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

         manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                TraceServiceImpl.sShouldStopService = false;
                DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                break;
            case R.id.btn_stop:
//                TraceServiceImpl.stopService();

                Notification notification = new NotificationCompat.Builder(this, "chat")
                        .setAutoCancel(true)
                        .setContentTitle("收到聊天消息")
                        .setContentText("今天晚上吃什么")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //设置红色
                        .setColor(Color.parseColor("#F00606"))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                        .setContentIntent(pendingIntentGet)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build();
                manager.notify(2, notification);
                break;
        }
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }
}
