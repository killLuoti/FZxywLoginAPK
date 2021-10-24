package sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.*;
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

    //创建通知
    public void CreateInform() {
        //定义一个PendingIntent，当用户点击通知时，跳转到某个Activity(也可以发送广播等)
        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //创建一个通知
//        Notification notification = new Notification(R.drawable.nihao, "巴拉巴拉~~", System.currentTimeMillis());
//        notification(context, "点击查看", "点击查看详细内容", pendingIntent);
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle("收到聊天消息")
                .setContentText("今天晚上吃什么")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置红色
                .setColor(Color.parseColor("#F00606"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
        //用NotificationManager的notify方法通知用户生成标题栏消息通知
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(0, notification);//id是应用中通知的唯一标识
        //如果拥有相同id的通知已经被提交而且没有被移除，该方法会用更新的信息来替换之前的通知。
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void startWork(Intent intent, int flags, int startId) {
//        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sDisposable = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
//                    System.out.println("保存数据到磁盘。");
                    cancelJobAlarmSub();
                })
                .subscribe(count -> {
                    getWiFiMeage getWiFiMeage = new getWiFiMeage(this);
                    if (getWiFiMeage.getWiFiName().equals("FZ-Student")) {
                        System.out.println("wifi上线!");
//                        new NC().notification();
//                        CreateInform();
                        xyw_jb xyw_jb = new xyw_jb(new SharedP(this).getUsername(), new SharedP(this).getPassword(), getWiFiMeage.getWifiIp(),this);
                        xyw_jb.lianjie();
                    } else {
                        System.out.println("WIFI连接错误!!");
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
