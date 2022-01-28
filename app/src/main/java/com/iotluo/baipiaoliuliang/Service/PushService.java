package com.iotluo.baipiaoliuliang.Service;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.iotluo.baipiaoliuliang.MainActivity;
import com.iotluo.baipiaoliuliang.R;

public class PushService extends Service {

	 static Timer timer = null;
	//清除通知
	public static void cleanAllNotification() {
		NotificationManager mn= (NotificationManager) MainActivity.getContext().getSystemService(NOTIFICATION_SERVICE);
		mn.cancelAll();	
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	//添加通知
	public static void addNotification(int delayTime,String tickerText,String contentTitle,String contentText)
	{			
		Intent intent = new Intent(MainActivity.getContext(), PushService.class);
		intent.putExtra("delayTime", delayTime);
		intent.putExtra("tickerText", tickerText);
		intent.putExtra("contentTitle", contentTitle);
		intent.putExtra("contentText", contentText);	   
		MainActivity.getContext().startService(intent);
	}
	
    public void onCreate() {
    	Log.e("addNotification", "===========create=======");
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int onStartCommand(final Intent intent, int flags, int startId) {
			
		long period = 24*60*60*1000; //24小时一个周期
		int delay=intent.getIntExtra("delayTime",0);
		if (null == timer ) {
			timer = new Timer();
		}
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				NotificationManager mn= (NotificationManager) PushService.this.getSystemService(NOTIFICATION_SERVICE);	
				Notification.Builder builder = new Notification.Builder(PushService.this);
				Intent notificationIntent = new Intent(PushService.this,MainActivity.class);//点击跳转位置				
				PendingIntent contentIntent = PendingIntent.getActivity(PushService.this,0,notificationIntent,0);				
				builder.setContentIntent(contentIntent);
				builder.setSmallIcon(R.drawable.ic_action_name);
				builder.setTicker(intent.getStringExtra("tickerText")); //测试通知栏标题
				builder.setContentText(intent.getStringExtra("contentText")); //下拉通知啦内容
				builder.setContentTitle(intent.getStringExtra("contentTitle"));//下拉通知栏标题
				builder.setAutoCancel(true);
				builder.setDefaults(Notification.DEFAULT_ALL);
				Notification notification = builder.build();
				mn.notify((int)System.currentTimeMillis(),notification);
			}
		},delay, period);
				
		return super.onStartCommand(intent, flags, startId);
	}
	
    @Override
    public void onDestroy(){
    	Log.e("addNotification", "===========destroy=======");
       super.onDestroy();
    }
}
