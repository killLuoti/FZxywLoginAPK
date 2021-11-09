package com.iotluo.baipiaoliuliang;


import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.*;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

//import de.blinkt.openvpn.com.iotluo.baipiaoliuliang.api.APIVpnProfile;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import SP.SharedP;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.api.IOpenVPNStatusCallback;
import openvpn.api.APIVpnProfile;


public class VPNActivity extends AppCompatActivity implements Handler.Callback {


    private TextView tv_zt;
    private EditText edname,edpwd;
    private Button but_start,but_stop;
    private RadioButton radio1,radio2;

    private SharedP sharedP;
    private String flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn);
        tv_zt = findViewById(R.id.tv_zt);
        edname = findViewById(R.id.etname);
        edpwd = findViewById(R.id.etpwd);
        but_start = findViewById(R.id.but_start);
        but_stop = findViewById(R.id.but_stop);
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);
        but_start.setOnClickListener(new onClick());
        but_stop.setOnClickListener(new onClick());

        edpwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sharedP = new SharedP(VPNActivity.this,"vpnconfig");
                sharedP.setUsername(edname.getText().toString());
                sharedP.setPassword(edpwd.getText().toString());
                sharedP.setSharedP();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //        读取用户名和密码
        edname.setText(new SharedP(VPNActivity.this,"vpnconfig").getUsername());
        edpwd.setText(new SharedP(VPNActivity.this,"vpnconfig").getPassword());
    }

    private static final int MSG_UPDATE_STATE = 0;
    private static final int MSG_UPDATE_MYIP = 1;
    private static final int START_PROFILE_EMBEDDED = 2;
    private static final int START_PROFILE_BYUUID = 3;
    private static final int ICS_OPENVPN_PERMISSION = 7;
    private static final int PROFILE_ADD_NEW = 8;
    private static final int PROFILE_ADD_NEW_EDIT = 9;


    protected IOpenVPNAPIService mService=null;
    private Handler mHandler;




    private void startEmbeddedProfile(boolean addNew, boolean editable, boolean startAfterAdd)
    {
        try {
            InputStream conf = null;
            /* Try opening test.local.conf first */
            if(radio1.isChecked()){
                try {
                    conf = getAssets().open("vpnconfig1.local.conf");
                }
                catch (IOException e) {
                    conf = getAssets().open("vpnconfig1.conf");
                }}
            else if(radio2.isChecked()){
                try {
                    conf = getAssets().open("vpnconfig2.local.conf");
                }
                catch (IOException e) {
                    conf = getAssets().open("vpnconfig2.conf");
                }}

            BufferedReader br = new BufferedReader(new InputStreamReader(conf));
            StringBuilder config = new StringBuilder();
            String line;
            while(true) {
                line = br.readLine();
                if(line == null)
                    break;
                config.append(line).append("\n");
            }
            String name1 = new SharedP(this,"config").getUsername();
            String pwd1 = new SharedP(this,"config").getPassword();
            if(name1.equals("")||pwd1.equals("")){
                Toast.makeText(this, "请设置补贴宽带账号密码", Toast.LENGTH_SHORT).show();
                return;
            }
            config.append("<auth-user-pass>").append("\n");
            config.append(name1).append("\n");
            config.append(pwd1).append("\n");
            config.append("</auth-user-pass>").append("\n");
            br.close();
            conf.close();

            if (addNew) {
                String name = editable ? "来自远程应用程序的配置文件" : "不可编辑的外形";
//                APIVpnProfile profile = mService.addNewVPNProfile(name, editable, config.toString());
//                mService.startProfile(profile.mUUID);

            } else
                mService.startVPN(config.toString());
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "已启动", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        flag = "补贴宽带程序未启动";
        mHandler = new Handler(this);
        bindService();
    }


    private IOpenVPNStatusCallback mCallback = new IOpenVPNStatusCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */

        @Override
        public void newStatus(String uuid, String state, String message, String level)
                throws RemoteException {
            Message msg = Message.obtain(mHandler, MSG_UPDATE_STATE, state + "|" + message);
            msg.sendToTarget();

        }

    };


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
//             This is called when the connection with the service has been
//             established, giving us the service object we can use to
//             interact with the service.  We are communicating with our
//             service through an IDL interface, so get a client-side
//             representation of that from the raw service object.

            mService = IOpenVPNAPIService.Stub.asInterface(service);

            try {
                // Request permission to use the API
                Intent i = mService.prepare(getPackageName());
                if (i!=null) {
                    startActivityForResult(i, ICS_OPENVPN_PERMISSION);
                    Log.d("luoluo", "onServiceConnected: bu kong");
                } else {
                    onActivityResult(ICS_OPENVPN_PERMISSION, Activity.RESULT_OK,null);
                    Log.d("luoluo", "onServiceConnected: kong");
                }

            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

        }
    };
    private String mStartUUID=null;

    private void bindService() {

        Intent icsopenvpnService = new Intent(IOpenVPNAPIService.class.getName());
        icsopenvpnService.setPackage("de.blinkt.openvpn");

        bindService(icsopenvpnService, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void listVPNs() {

//        try {
//            List<APIVpnProfile> list = mService.getProfiles();
//            String all="List:";
//            for(APIVpnProfile vp:list.subList(0, Math.min(5, list.size()))) {
//                all = all + vp.mName + ":" + vp.mUUID + "\n";
//            }
//
//            if (list.size() > 5)
//                all +="\n And some profiles....";
//
//            if(list.size()> 0) {
////                Button b= but_start;
////                b.setOnClickListener(new onClick());
////                b.setVisibility(View.VISIBLE);
////                b.setText(list.get(0).mName);
//                mStartUUID = list.get(0).mUUID;
//            }
//
//
//
////            mHelloWorld.setText(all);
//
//        } catch (RemoteException e) {
//            // TODO Auto-generated catch block
////            mHelloWorld.setText(e.getMessage());
//            System.out.println(e.getMessage());
//        }
    }

    private void unbindService() {
        unbindService(mConnection);
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService();
    }

class onClick implements View.OnClickListener{

    //获取已安装应用的 uid，-1 表示未安装此应用或程序异常
    public  int getPackageUid(Context context, String packageName) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                return applicationInfo.uid;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    @Override
    public void onClick(View v) {
        String pName = "de.blinkt.openvpn";
        int uid = getPackageUid(VPNActivity.this, pName);
        if(uid > 0){
            if(flag.equals("补贴宽带程序未启动")){
                final String EXTRA_NAME = "de.blinkt.openvpn.api.profileName";
                Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
                shortcutIntent.setClassName("de.blinkt.openvpn", "de.blinkt.openvpn.LaunchVPN");
//                shortcutIntent.putExtra(EXTRA_NAME,"upb ssl");
                startActivity(shortcutIntent);
//                onStart();
                bindService();
                Toast.makeText(VPNActivity.this, "补贴宽带程序未启动,请再次点击连接！", Toast.LENGTH_LONG).show();
                return;
            }
        }else{
            //应用未安装
            Toast.makeText(VPNActivity.this, "补贴宽带未安装", Toast.LENGTH_LONG).show();
            return;
        }

        switch (v.getId()) {
//            case R.id.startVPN:
//                try {
//                    prepareStartProfile(START_PROFILE_BYUUID);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//                break;
            case R.id.but_stop:
                try {
                    mService.disconnect();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
//            case R.id.getMyIP:
//
//                // Socket handling is not allowed on main thread
//                new Thread() {
//
//                    @Override
//                    public void run() {
//                        try {
//                            String myip = getMyOwnIP();
//                            Message msg = Message.obtain(mHandler,MSG_UPDATE_MYIP,myip);
//                            msg.sendToTarget();
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                    }
//                }.start();
//
//                break;
            case R.id.but_start:
                try {
                    prepareStartProfile(START_PROFILE_EMBEDDED);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;

//            case R.id.addNewProfile:
//            case R.id.addNewProfileEdit:
//                int action = (v.getId() == R.id.addNewProfile) ? PROFILE_ADD_NEW : PROFILE_ADD_NEW_EDIT;
//                try {
//                    prepareStartProfile(action);
//                } catch (RemoteException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            default:
//                break;
        }

    }
}


    private void prepareStartProfile(int requestCode) throws RemoteException {
        Intent requestpermission = mService.prepareVPNService();
        if(requestpermission == null) {
            onActivityResult(requestCode, Activity.RESULT_OK, null);
        } else {
            // Have to call an external Activity since services cannot used onActivityResult
            startActivityForResult(requestpermission, requestCode);

        }
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == START_PROFILE_EMBEDDED)
                startEmbeddedProfile(false, false, false);
            if (requestCode == START_PROFILE_BYUUID)
                try {
                    mService.startProfile(mStartUUID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            if (requestCode == ICS_OPENVPN_PERMISSION) {
                listVPNs();
                try {
                    mService.registerStatusCallback(mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
//            CheckBox startCB = getView().findViewById(R.id.startafterAdding);
//            if (requestCode == PROFILE_ADD_NEW) {
//                startEmbeddedProfile(true, false, startCB.isSelected());
//            }
//            else if (requestCode == PROFILE_ADD_NEW_EDIT) {
//                startEmbeddedProfile(true, true, startCB.isSelected());
//            }
        }
    };

    String getMyOwnIP() throws UnknownHostException, IOException, RemoteException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        StringBuilder resp = new StringBuilder();

        URL url = new URL("https://icanhazip.com");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while (true) {
                String line = in.readLine();
                if( line == null)
                    return resp.toString();
                resp.append(line);
            }
        } finally {
            urlConnection.disconnect();
        }
    }



    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what == MSG_UPDATE_STATE) {
//            mStatus.setText((CharSequence) msg.obj);
            tv_zt.setText("状态："+(CharSequence) msg.obj);
            System.out.println(msg.obj.toString());
            flag= msg.obj.toString();
        } else if (msg.what == MSG_UPDATE_MYIP) {

//            mMyIp.setText((CharSequence) msg.obj);
            System.out.println((CharSequence)msg.obj);
        }
        return true;
    }
}