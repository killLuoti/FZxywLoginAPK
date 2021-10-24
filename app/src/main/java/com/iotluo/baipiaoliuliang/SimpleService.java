package com.iotluo.baipiaoliuliang;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class SimpleService extends Service {
    private static final String TAG = "luoluo";
    private SimpleBinder mBinder;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SS_onCreate: ______");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.d(TAG, "run: ");
                    Looper.prepare();
                    Toast.makeText(SimpleService.this, "runThread", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        mBinder = new SimpleBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mBinder != null){
            return mBinder;
        }
        return null;
    }
    class SimpleBinder extends Binder{
        public void doTask(){
            Log.d(TAG, "doTask: ");
        }
    }
}