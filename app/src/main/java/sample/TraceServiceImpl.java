package sample;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;
import com.xdandroid.hellodaemon.*;

import java.util.concurrent.*;

import SP.SharedP;
import io.reactivex.*;
import io.reactivex.disposables.*;
import support.NC;
import wifiMeage.getWiFiMeage;
import xyw.xyw_jb;

public class TraceServiceImpl extends AbsWorkService {

    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;
    private Context context;
    private String text;
    private String url2 = "http://10.10.10.10/";
    private String url3 = "http://10.150.2.21:8080/Self/dashboard";
    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null) sDisposable.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }
    public void tongzhi(String text){
        if(this.text==text){return;}else {
        this.text = text;
        tongzhi(text, "通知");}
    }
    public  void tongzhi(String text, String title){
        //增加一個渠道，ID不重复即可
        String CHANNEL_ID = "fspt.net";
        String CHANNEL_NAME = "新消息通知";
        String description = "校园网自动登录程序";
        int notifiId = (int) System.currentTimeMillis();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//Android 8.0需要增加渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("luoluo", "tongzhi: #####");
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,CHANNEL_ID);
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
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setTicker(text);
        notificationBuilder.setAutoCancel(true);//点击之后消失
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setDefaults( Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND );
//        notificationManager.notify(notifiId, notificationBuilder.build());
        startForeground(notifiId, notificationBuilder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sDisposable = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
                    System.out.println("保存数据到磁盘2。");
                    cancelJobAlarmSub();
                })
                .subscribe(count -> {
                    getWiFiMeage getWiFiMeage = new getWiFiMeage(this);
                    if (getWiFiMeage.getWiFiName().equals("FZ-Student")) {
                        if (!getWiFiMeage.isConnByHttp(url3)&& getWiFiMeage.isConnByHttp(url2)) {
                            String name = new SharedP(this,"config").getUsername();
                            String pwd = new SharedP(this,"config").getPassword();
                            if(name.equals("")||pwd.equals("")){
                                tongzhi("请设置校园网账号密码");
                                return;}
                            tongzhi("开始自动登录!");
                            xyw_jb xyw_jb = new xyw_jb(name, pwd, getWiFiMeage.getWifiIp(), this);
                            if(xyw_jb.lianjie())
                                tongzhi("登录成功");
                            else
                                tongzhi("请重连WiFi");
                        }else {
                            if(getWiFiMeage.isConnByHttp("http://www.baidu.com"))
                                tongzhi("网络正常");
                        }
                    } else {
                        tongzhi("未连接FZ-Student");
                    }
//                    System.out.println("每 3 秒采集一次数据... count = " + count);
//                    if (count > 0 && count % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
                });
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }

    /**
     * 任务是否正在运行?
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
    }
}
