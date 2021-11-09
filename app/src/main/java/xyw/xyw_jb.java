package xyw;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import http.httpclass;
import sample.TraceServiceImpl;
import wifiMeage.getWiFiMeage;

public class xyw_jb {
    private String url1 = "http://10.0.0.10:801/eportal/";
    private String url2 = "http://10.10.10.10/";
    private String url3 = "http://10.150.2.21:8080/Self/dashboard";
    private String baiduurl = "https://www.baidu.com";
    private String patter = "\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b";
    private static final String TAG = "luoluo";
    private String editTextname;
    private String editTextpwd;
    private String ip;
    private Context con;

    public xyw_jb(String name, String pwd, String ip,Context con) {
        this.editTextname = name;
        this.editTextpwd = pwd;
        this.ip = ip;
        this.con = con;
    }


    public boolean lianjie(){
        try
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("DDDDD", ",1," + editTextname);
            params.put("upass", editTextpwd);
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
            Log.d(TAG, "lianjie: __________________");
//                getWiFiMeage.isConnByHttp2();
            httpclass httpclass = new httpclass(url2);
            Log.d(TAG, "run: " + httpclass.get);
            Pattern pattern = Pattern.compile(patter);
            Matcher matcher = pattern.matcher(httpclass.get);
            List<String> list = new ArrayList<String>();
            while (matcher.find()) {
                //Log.d(TAG, "run: "+matcher.group(0));
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
                Thread.sleep(200);
                if (getWiFiMeage.isConnByHttp(url3)){

//                        Log.d(TAG, "lianjie: 可以上网了，草");
                    if (getWiFiMeage.isConnByHttp(baiduurl)) {
                        System.out.println("lianjie: 可以上网了！");
                        Toast("自动登录成功！");
                    }else {Toast("自动登录成功，可惜无法上网，草");}
//                            Toast("可以上网了，草");
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast("请重连WIFI");
                return false;
            }

        } catch(
                Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    public void Toast(String s){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(con, s, Toast.LENGTH_SHORT).show();
                Looper.loop();//这种情况下，Runnable对象是运行在子线程中的，可以进行联网操作，但是不能更新UI
            }
        }).start();
    }
}
