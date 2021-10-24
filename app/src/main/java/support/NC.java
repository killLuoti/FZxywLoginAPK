package support;

import static android.os.FileUtils.copy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

public class NC {
    private String CHANNEL_ID;
    private Context con;
    private NotificationManager mNotificationManager;

    public NC(String CHANNEL_ID, Context con ,NotificationManager mNotificationManager) {
        this.CHANNEL_ID = CHANNEL_ID;
        this.con = con;
        this.mNotificationManager = mNotificationManager;
    }
    public NC(){}

    public void notification() {
        Notification notification = new NotificationCompat.Builder(con, CHANNEL_ID)
                .setAutoCancel(true)
                .setContentTitle("收到聊天消息")
                .setContentText("今天晚上吃什么")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置红色
                .setColor(Color.parseColor("#F00606"))
                .setLargeIcon(BitmapFactory.decodeResource(con.getResources(), R.mipmap.ic_launcher))
//                        .setContentIntent(pendingIntentGet)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        mNotificationManager.notify(0, notification);
    }


    public void cleanNotification(View view) {
        NotificationManager mNotificationManager =
                (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mNotificationManager.cancel(0);
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
