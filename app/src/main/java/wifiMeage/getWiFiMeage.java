package wifiMeage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class getWiFiMeage {
    public String wifiname;
    private Context con;

    public getWiFiMeage(Context con) {
        this.con = con;
    }


    /*
    *
    * 网络连接判断1
    * */
    public static boolean netWorkCheck(Context context){
        ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if( info != null ){
            return info.isConnected();
        } else {
            return false;
        }
    }

    /*
     *
     * 网络连接判断2
     * */
    public static boolean isConnByHttp(){
        boolean isConn = false;
        URL url;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://10.150.2.21:8080/Self/dashboard");
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(1000*5);
            if(conn.getResponseCode()==200){
                isConn = true;
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            conn.disconnect();
        }
        return isConn;
    }

    /*
     *
     * 网络连接判断2
     * */
    public static boolean isConnByHttp2(){
        boolean isConn = false;
        URL url;
        HttpURLConnection conn = null;
        try {
            url = new URL("http://10.10.10.10/");
            conn = (HttpURLConnection)url.openConnection();
//            conn.setConnectTimeout(1000*5);
            conn.setReadTimeout(500);
            if(conn.getResponseCode()==200){
                isConn = true;
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            conn.disconnect();
        }
        return isConn;
    }
    /*
     * 判断当前 WIFI 是否连接
     * */
    public boolean isWifiEnabled() {
        Context myContext = con.getApplicationContext();
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

    /*
     * 获取 WiFi 的 IP 地址
     * */
    public String getWifiIp() {
        Context myContext = con.getApplicationContext();
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getWiFiName() {
        WifiManager wm = ((WifiManager) con.getApplicationContext().getSystemService(con.WIFI_SERVICE));
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
}
