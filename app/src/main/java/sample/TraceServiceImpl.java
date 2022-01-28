package sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;
import com.iotluo.baipiaoliuliang.Service.BootstarpService;
import com.iotluo.baipiaoliuliang.Service.NotificationUtil;
import com.iotluo.baipiaoliuliang.Service.PushService;
import com.tapadoo.alerter.Alerter;
import com.xdandroid.hellodaemon.*;
import com.yanzhenjie.permission.Boot;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public void tongzhi(String text){
        if(this.text==text){return;}else {
            this.text = text;
            //通知1
//        BootstarpService.text = text;
            if (MainActivity.getContext() != null) {
                NotificationUtil.sendNotification(MainActivity.getContext(), "新消息通知",
                        "校园网自动登录程序", "通知", text);
            }
        }


        // start BootstrapService to remove notification
//        Intent intent = new Intent(this, BootstarpService.class);
////        intent.setAction("com.android.iotluo");
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                //android8.0以上通过startForegroundService启动service
//                startForegroundService(intent);
//                Log.d("luoluo", "Android8以上启动服务 ");
//            } else {
//                startService(intent);
//                Log.d("luoluo", "Android8以下启动服务 ");
//            }
//        }
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
                    if (getWiFiMeage.getWiFiName().equals("FZ-Student")) { //FZ-Student
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
