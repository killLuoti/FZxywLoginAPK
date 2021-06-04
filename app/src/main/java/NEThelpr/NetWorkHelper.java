package NEThelpr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;


/**
 * 获取网络信息的工具类
 * 2020/5/6 10:43
 *
 * @author LiuWeiHao
 * Email 872235631@qq.com
 */
public class NetWorkHelper {
    private static final String TAG = "net";
    private static NetWorkHelper mInstance;
    /**
     * 是否已经注册回调
     */
    private static boolean isRegisted = false;
    private ConnectivityManager connectivityManager;
    /**
     * 网络是否可用
     */
    private boolean isNetWorkAvailable = false;
    /**
     * 网络类型
     */
    private NetType netType;
    private NetworkCallbackImpl networkCallbackImpl;

    /**
     * 获取NetWorkHelper实例
     */
    public static NetWorkHelper getInstance() {
        if (mInstance == null) {
            synchronized (NetWorkHelper.class) {
                if (mInstance == null) {
                    mInstance = new NetWorkHelper();
                }
            }
        }
        return mInstance;
    }

    private NetWorkHelper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallbackImpl = new NetworkCallbackImpl();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            isNetWorkAvailable = true;
            Log.d(TAG, "onAvailable");
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            isNetWorkAvailable = false;
            Log.d(TAG, "onLosing");
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            isNetWorkAvailable = false;
            Log.d(TAG, "onLost");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            isNetWorkAvailable = false;
            Log.d(TAG, "onUnavailable");
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isNetWorkAvailable = true;
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                netType = NetType.WIFI;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                netType = NetType.BLUETOOTH;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                netType = NetType.CELLULAR;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                netType = NetType.ETHERNET;
            }
            Log.d(TAG, "onCapabilitiesChanged");
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.d(TAG, "onLinkPropertiesChanged");
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            Log.d(TAG, "onBlockedStatusChanged");
        }
    }

    /**
     * 注册回调
     */
    @RequiresPermission(Manifest.permission.CHANGE_NETWORK_STATE)
    public void registerCallback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isRegisted) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                isRegisted = true;
                connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), networkCallbackImpl);
            }
        }
    }

    /**
     * 取消注册
     */

    public void unRegisterCallback() {
        if (connectivityManager != null && isRegisted) {
            isRegisted = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(networkCallbackImpl);
            }
        }
    }


    enum NetType {
        /**
         * wifi
         */
        WIFI,
        BLUETOOTH,
        CELLULAR,
        ETHERNET,
    }

    public boolean isNetWorkAvailable() {
        return isNetWorkAvailable;
    }

    public NetType getNetType() {
        return netType;
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    public boolean isAvailable(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return isNetWorkAvailable;
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * WIFI是否可用
     *
     * @param context 上下文
     * @return WIFI是否可用
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    /**
     * 获取当前WIFI名称
     *
     * @param context 上下文
     * @return 当前WIFI名称
     */
    @SuppressLint("ObsoleteSdkInt")
    public static String getCurrentSsid(Context context) {
        String ssid = "unknown id";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            assert wifiManager != null;
            WifiInfo info = wifiManager.getConnectionInfo();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                ssid = info.getSSID();
            } else {
                ssid = info.getSSID().replace("\"", "");
            }
            if (!TextUtils.isEmpty(ssid)) {
                Log.d(TAG, "ssid=" + ssid);
                return ssid;
            }
            //部分手机拿不到WiFi名称
            int networkId = info.getNetworkId();
            Log.d(TAG, "networkId=" + networkId);
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : configuredNetworks) {
                if (config.networkId == networkId) {
                    ssid = config.SSID;
                    break;
                }
            }
            //扫描到的网络
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults) {
                String bssid = scanResult.SSID;
            }
            return ssid;
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {

            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] allNetworks = connManager.getAllNetworks();
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    return networkInfo.getExtraInfo().replace("\"", "");
                }
            }
        }
        return ssid;
    }

    /**
     * 是否5G网络
     *
     * @param ssid    wifi名称
     * @param context 上下文
     * @return 是否5G网络
     */
    public static boolean is5GHz(String ssid, Context context) {
        WifiManager wifiManger = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManger == null) {
            return false;
        }
        WifiInfo wifiInfo = wifiManger.getConnectionInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int freq = wifiInfo.getFrequency();
            return freq > 4900 && freq < 5900;
        } else {
            return ssid.toUpperCase().endsWith("5G");
        }
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}