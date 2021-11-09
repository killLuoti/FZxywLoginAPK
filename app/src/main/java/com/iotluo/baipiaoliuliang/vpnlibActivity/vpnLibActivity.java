package com.iotluo.baipiaoliuliang.vpnlibActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.iotluo.baipiaoliuliang.*;
import com.iotluo.baipiaoliuliang.vpnlibActivity.model.Server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import SP.SharedP;
import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

public class vpnLibActivity extends AppCompatActivity implements View.OnClickListener{

    private Server server;
    private CheckInternetConnection connection;

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    private SharedPreference preference;

    private TextView    durationTv;
    private TextView    lastPacketReceiveTv;
    private TextView    byteInTv;
    private TextView    byteOutTv;
    private RadioGroup rg_xlu;
    private RadioButton rb_xil1,rb_xil2;
    private ArrayList<Server> serverLists;
//    private FragmentMainBinding binding;
    private Button but_vpn;
    boolean vpnStart = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_lib);
        durationTv =findViewById(R.id.durationTv);
        lastPacketReceiveTv = findViewById(R.id.lastPacketReceiveTv);
        byteInTv= findViewById(R.id.byteInTv);
        byteOutTv= findViewById(R.id.byteOutTv);;
        but_vpn = findViewById(R.id.vpnBtn);
        serverLists = getServerList();
        but_vpn.setOnClickListener(this::onClick);
        rb_xil1 = findViewById(R.id.radio1);
        rb_xil2 = findViewById(R.id.radio2);
        rg_xlu = findViewById(R.id.radiogroup1);
        rg_xlu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String name1 = new SharedP(vpnLibActivity.this,"config").getUsername();
                String pwd1 = new SharedP(vpnLibActivity.this,"config").getPassword();
                if(name1.equals("")||pwd1.equals("")){
                    return;
                }
//                Toast.makeText(vpnLibActivity.this, "选择了："+i, Toast.LENGTH_SHORT).show();
                if(rb_xil1.isChecked()) {
                    newServer(serverLists.get(0));
                }else if(rb_xil2.isChecked()){
                    newServer(serverLists.get(1));
                }
            }
        });
        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getCacheDir());

        preference = new SharedPreference(vpnLibActivity.this);
        server = preference.getServer();
        connection = new CheckInternetConnection();
//        newServer(serverLists.get(index));
    }

    /**
     * Generate server array list
     */
    private ArrayList getServerList() {

        ArrayList<Server> servers = new ArrayList<>();

        servers.add(new Server("线路1",
                "线路1.ovpn",
                new SharedP(this,"config").getUsername(),
                new SharedP(this,"config").getPassword()
        ));
        servers.add(new Server("线路2",
                "线路2.ovpn",
                new SharedP(this,"config").getUsername(),
                new SharedP(this,"config").getPassword()
        ));
        return servers;
    }
    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(vpnLibActivity.this).registerReceiver(broadcastReceiver,
                new IntentFilter("connectionState"));


        if (server == null) {
            server = preference.getServer();
        }
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vpnBtn:
                String name1 = new SharedP(this,"config").getUsername();
                String pwd1 = new SharedP(this,"config").getPassword();
                if(name1.equals("")||pwd1.equals("")){
                    Toast.makeText(this, "请设置补贴宽带账号密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Vpn is running, user would like to disconnect current connection.
                if (vpnStart) {
                    confirmDisconnect();
                }else {
                    prepareVpn();
                }
        }
    }

    /**
     * Show show disconnect confirm dialog
     */
    public void confirmDisconnect(){
        AlertDialog.Builder builder = new AlertDialog.Builder(vpnLibActivity.this);
        builder.setMessage(vpnLibActivity.this.getString(R.string.connection_close_confirm));

        builder.setPositiveButton(vpnLibActivity.this.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopVpn();
            }
        });
        builder.setNegativeButton(vpnLibActivity.this.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /**
     * Prepare for vpn connect with required permission
     */
    private void prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {

                // Checking permission for network monitor
                Intent intent = VpnService.prepare(this);

                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else startVpn();//have already permission

                // Update confection status
                status("connecting");

            } else {

                // No internet connection available
                showToast("you have no internet connection !!");
            }

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }
    /**
     * Internet connection status.
     */
    public boolean getInternetStatus() {
        return connection.netCheck(this);
    }
    /**
     * Get service status
     */
    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }
    /**
     * Stop vpn
     * @return boolean: VPN status
     */
    public boolean stopVpn() {
        try {
            vpnThread.stop();

            status("connect");
            vpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    /**
     * Start the VPN
     */
    private void startVpn() {
        try {
            // .ovpn file
            InputStream conf = getAssets().open(server.getOvpn());
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            String config = "";
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }

            br.readLine();
            OpenVpnApi.startVpn(this, config, server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

            // Update log
            but_vpn.setText("连接中...");
            vpnStart = true;

        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change button background color and text
     * @param status: VPN current status
     */
    public void status(String status) {

        if (status.equals("connect")) {
            but_vpn.setText(getString(R.string.connect));
        } else if (status.equals("connecting")) {
            but_vpn.setText(getString(R.string.connecting));
        } else if (status.equals("connected")) {

            but_vpn.setText(getString(R.string.disconnect));

        } else if (status.equals("tryDifferentServer")) {

            but_vpn.setBackgroundResource(R.drawable.button_connected);
            but_vpn.setText("Try Different\nServer");
        } else if (status.equals("loading")) {
            but_vpn.setBackgroundResource(R.drawable.button);
            but_vpn.setText("Loading Server..");
        } else if (status.equals("invalidDevice")) {
            but_vpn.setBackgroundResource(R.drawable.button_connected);
            but_vpn.setText("Invalid Device");
        } else if (status.equals("authenticationCheck")) {
            but_vpn.setBackgroundResource(R.drawable.button_connecting);
            but_vpn.setText("Authentication \n Checking...");
        }
    }
    /**
     * Status change with corresponding vpn connection status
     * @param connectionState
     */
    public void setStatus(String connectionState) {
        if (connectionState!= null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    vpnStart = false;
                    vpnService.setDefaultStatus();
                    but_vpn.setText("开始连接");
                    break;
                case "CONNECTED":
                    vpnStart = true;// it will use after restart this activity
                    status("connected");
                    but_vpn.setText("连接成功");
                    break;
                case "WAIT":
                    but_vpn.setText("正在等待服务器连接！！");
                    break;
                case "AUTH":
                    but_vpn.setText("服务器验证！！");
                    break;
                case "RECONNECTING":
                    status("connecting");
                    but_vpn.setText("重新连接。。。");
                    break;
                case "NONETWORK":
                    but_vpn.setText("没有网络连接");
                    break;
            }

    }
    /**
     * Change server when user select new server
     * @param server ovpn server details
     */
    public void newServer(Server server) {
        this.server = server;
//        updateCurrentServerIcon(server.getFlagUrl());

        // Stop previous connection
        if (vpnStart) {
            stopVpn();
        }

        prepareVpn();
    }
    /**
     * Receive broadcast message
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    /**
     * Update status UI
     * @param duration: running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn: incoming data
     * @param byteOut: outgoing data
     */
    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        durationTv.setText("时间: " + duration);
        lastPacketReceiveTv.setText("收到的数据包: " + lastPacketReceive + " second ago");
        byteInTv.setText("接收: " + byteIn);
        byteOutTv.setText("发送: " + byteOut);
    }
    /**
     * Show toast message
     * @param message: toast message
     */
    public void showToast(String message) {
        Toast.makeText(vpnLibActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}