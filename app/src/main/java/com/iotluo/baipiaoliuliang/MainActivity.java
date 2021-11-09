package com.iotluo.baipiaoliuliang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.iotluo.baipiaoliuliang.vpnlibActivity.vpnLibActivity;
import com.xdandroid.hellodaemon.*;
import org.apache.commons.lang3.StringEscapeUtils;


import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yanzhenjie.permission.runtime.PermissionDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import NEThelpr.NetWorkHelper;
import NetUtil.netutiltest;
import SP.SharedP;
import SharedPreferencesUtils.SharedPreferencesUtils;
import http.httpclass;
import sample.TraceServiceImpl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "luoluo";
    private static final String TODO = "TODO";
    private static final int REQUEST_CODE_SETTING = 1;
    private CheckBox cb_xs;
    private Button but1;
    private Button but2,but_vpn;
    private EditText editTextname;
    private EditText editTextpwd;
    private ProgressBar pb;
    private Context mContext;
    private boolean reper = false;//定位权限 ，默认值为未获得权限
    private SimpleService.SimpleBinder mBinder;
    private SharedP sharedP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = findViewById(R.id.pgbzt);
        cb_xs = findViewById(R.id.cb_xs);
        editTextname = findViewById(R.id.etname);
        editTextpwd = findViewById(R.id.etpwd);
        but1 = findViewById(R.id.butdian);
        but1.setOnClickListener(new onclick());
        but2 = findViewById(R.id.butwifi);
        but2.setOnClickListener(new onclick());
        but_vpn = findViewById(R.id.but_vpn);
        but_vpn.setOnClickListener(new onclick());
        requestPermission(this);
        cb_xs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editTextpwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editTextpwd.setSelection(editTextpwd.getText().length());
                }else {
                    editTextpwd.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editTextpwd.setSelection(editTextpwd.getText().length());
                }
            }
        });
        editTextpwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedP = new SharedP(MainActivity.this,"config");
                sharedP.setUsername(editTextname.getText().toString());
                sharedP.setPassword(editTextpwd.getText().toString());
                sharedP.setSharedP();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        读取用户名和密码
        editTextname.setText(new SharedP(MainActivity.this,"config").getUsername());
        editTextpwd.setText(new SharedP(MainActivity.this,"config").getPassword());
//        if(isWorked())
//        {but1.setText("关闭服务");}
    }

    /*
     * 是否健在
     * */
    public boolean isWorked()
    {
        ActivityManager myManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for(int i = 0 ; i<runningService.size();i++)
        {
            System.out.println(runningService.get(i).service.getClassName());
            if(runningService.get(i).service.getClassName().toString().equals("sample.TraceServiceImpl"))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * bind
     * **/
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: "+name);
            mBinder =(SimpleService.SimpleBinder) service;
            mBinder.doTask();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: "+name);
        }
    };

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this).runtime().setting().start(REQUEST_CODE_SETTING);
    }


    /**
     * Display setting dialog.
     * 多次拒绝权限后触发跳转设置界面
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Request permissions.
     */
    private void requestPermission(Context con) {
        AndPermission.with(con)
                .runtime()
                .permission(Permission.Group.LOCATION,Permission.Group.STORAGE)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
//                        Toast.makeText(MainActivity.this, "lala", Toast.LENGTH_SHORT).show();
                        reper = true; //权限已开
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
//                        Toast.makeText(MainActivity.this, "请允许权限", Toast.LENGTH_SHORT).show();
                        reper = false;
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            showSettingDialog(MainActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    class onclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.butdian: {
                    requestPermission(MainActivity.this);
                    Log.d(TAG, "onClick: dianle");
                    pb.setProgress(0);
//                    String ip = getWifiIp();
//                    String wifiname = getWiFiName();
//                    if(but1.getText().equals("开启服务")) {
////                        Intent startIntent = new Intent(MainActivity.this, SimpleService.class);
////                        startService(startIntent);
//                        TraceServiceImpl.sShouldStopService = false;
//                        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
//                        but1.setText("关闭服务");
//                    }
//                    else {
////                        Intent stopIntent = new Intent(MainActivity.this, SimpleService.class);
////                        stopService(stopIntent);
//                        TraceServiceImpl.stopService();
//                        but1.setText("开启服务");
//                    }
                    TraceServiceImpl.sShouldStopService = false;
                    DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
                    break;
                }
                case R.id.butwifi: {
                    pb.setProgress(100);
                    IntentWrapper.whiteListMatters(MainActivity.this, "校园网自动登录服务的持续运行");
                    break;
                }
                case R.id.but_vpn:{
                    Intent intent = new Intent(MainActivity.this, vpnLibActivity.class);
                    startActivity(intent);
                    break;
                }
            }
        }

    }




    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }

    //Toast
    private void Toast(String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }






    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d(TAG, wifiInfo.toString());
        Log.d(TAG, wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }




}