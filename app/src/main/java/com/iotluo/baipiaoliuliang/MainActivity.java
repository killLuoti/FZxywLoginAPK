package com.iotluo.baipiaoliuliang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import SharedPreferencesUtils.SharedPreferencesUtils;
import http.httpclass;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LLLL";
    private static final String TODO = "TODO";
    private static final int REQUEST_CODE_SETTING = 1;
    private Button but1;
    private Button but2;
    private EditText editTextname;
    private EditText editTextpwd;
    private ProgressBar pb;
    private String url1 = "http://10.0.0.10:801/eportal/";
    private String url2 = "http://10.10.10.10/";
    private String patter = "\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b";
    private Context mContext;
    private boolean reper = false;//定位权限 ，默认值为未获得权限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = findViewById(R.id.pgbzt);
        editTextname = findViewById(R.id.etname);
        editTextpwd = findViewById(R.id.etpwd);
        but1 = findViewById(R.id.butdian);
        but1.setOnClickListener(new onclick());
        but2 = findViewById(R.id.butwifi);
        but2.setOnClickListener(new onclick());
        requestPermission(Permission.Group.LOCATION);
//        //存储权限
//        if(ContextCompat.checkSelfPermission
//                (this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED)
//        //根据返回的结果，判断对应的权限是否有。
//        {
//            ActivityCompat.requestPermissions
//                    (this,
//                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            0);
//        }
//        读取用户名和密码
        SharedPreferences sharedPre = getSharedPreferences("config", MODE_PRIVATE);
        String username = sharedPre.getString("username", "");
        String password = sharedPre.getString("password", "");

        editTextname.setText(username);
        editTextpwd.setText(password);



    }


    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this).runtime().setting().start(REQUEST_CODE_SETTING);
    }


    /**
     * Display setting dialog.
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
    private void requestPermission(@PermissionDef String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .rationale(new com.iotluo.baipiaoliuliang.RuntimeRationale())
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
                        Toast.makeText(MainActivity.this, "请允许权限", Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG, "onClick: dianle");
                    pb.setProgress(0);
//                    SharedPreferencesUtils sharedPreferencesUtils =new SharedPreferencesUtils();
                    //保存账号密码
                    SharedPreferences sharedPre = getSharedPreferences("config", MODE_PRIVATE);
                    //获取Editor对象
                    SharedPreferences.Editor editor = sharedPre.edit();
                    editor.putString("username", editTextname.getText().toString());
                    editor.putString("password", editTextpwd.getText().toString());
                    //提交
                    editor.commit();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (reper && !netutiltest.ping("114.114.114.114")) {
                                    String ip = getWifiIp();
                                    String wifiname = getWiFiName();
                                    String[] ips = null;
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("DDDDD", ",1," + editTextname.getText().toString());
                                    params.put("upass", editTextpwd.getText().toString());
                                    params.put("R1", "0");
                                    params.put("R2", "0");
                                    params.put("R3", "0");
                                    params.put("R6", "1");
                                    params.put("para", "00");
                                    params.put("0MKKey", "123456");
                                    params.put("buttonClicked", "");
                                    params.put("redirect_url", "");
                                    params.put("err_flag", "");
                                    params.put("username", "");
                                    params.put("password", "");
                                    params.put("user", "");
                                    params.put("cmd", "");
                                    params.put("Login", "");
                                    params.put("v6ip", "");
                                    if (wifiname.equals("FZ-Student")) {
                                        httpclass httpclass = new httpclass(url2);
                                        Log.d(TAG, "run: " + httpclass.get);
                                        Pattern pattern = Pattern.compile(patter);
                                        Matcher matcher = pattern.matcher(httpclass.get);
                                        List<String> list = new ArrayList<String>();
                                        while (matcher.find()) {
                                            //                                        Log.d(TAG, "run: "+matcher.group(0));
                                            list.add(matcher.group(0));
                                        }
                                        try {
                                            Log.d(TAG, "run: " + list.get(1));
                                            String s = httpclass.submitPostData(url1 + "?c=ACSetting&a=Login" +
                                                            "&protocol=http:&hostname=10.0.0.10&iTermType=8" +
                                                            "&wlanuserip=" + ip + "&wlanacip=" + list.get(1) + "" +
                                                            "&wlanacname=null&mac=00-00-00-00-00-00&ip=" + ip + "" +
                                                            "&enAdvert=0&queryACIP=0&jsVersion=2.4.3&loginMethod=1",
                                                    params, "utf-8");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "请重连WIFI", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        //                                    Log.d(TAG, "run: " + s);

                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, "WIFI连接错误", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        Log.d(TAG, "WIFI连接错误");
                                    }
                                } else {
                                    requestPermission(Permission.Group.LOCATION);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < 10; i++) {
                                        pb.incrementProgressBy(10);
                                        try {
                                            if (netutiltest.ping("114.114.114.114")) {
                                                Toast.makeText(MainActivity.this, "可以上网了", Toast.LENGTH_SHORT).show();
                                                pb.setProgress(100);
                                                break;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }).start();
                    break;
                }
                case R.id.butwifi: {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + getWifiIp());
                            Log.d(TAG, "run: " + getWiFiName());
                        }
                    }).start();
                    if (netutiltest.isNetworkAvailable(MainActivity.this) == 1) {
                        Log.d(TAG, "run: 222222222222222222222");
                    }
//                    getConnectWifiSsid();
//                    Log.d(TAG, "onClick: wifi dianle");
//                    NetWorkHelper.getInstance();
//                    Log.d(TAG, "SSID: "+NetWorkHelper.getCurrentSsid(new MainActivity()));
                    break;
                }
            }
        }

    }


    /*
     * 获取 WiFi 的 IP 地址
     * */
    public String getWifiIp() {
        Context myContext = getApplicationContext();
        if (myContext == null) {
            throw new NullPointerException("上下文 context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
        if (isWifiEnabled()) {
            int ipAsInt = wifiMgr.getConnectionInfo().getIpAddress();
            String ip = Formatter.formatIpAddress(ipAsInt);
            if (ipAsInt == 0) {
                return "未能获取到IP地址";
            } else {
                return ip;
            }
        } else {
            return "WiFi 未连接";
        }
    }

    /*
     * 获取 WIFI 的名称
     * */
    public String getWiFiName() {
        WifiManager wm = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "Wifi 未获取到";
    }

    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d(TAG, wifiInfo.toString());
        Log.d(TAG, wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }


    /*
     * 判断当前 WIFI 是否连接
     * */
    public boolean isWifiEnabled() {
        Context myContext = getApplicationContext();
        if (myContext == null) {
            throw new NullPointerException("上下文 context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConnectivityManager connManager = (ConnectivityManager) myContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiInfo = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            return wifiInfo.isConnected();
        } else {
            return false;
        }
    }

}