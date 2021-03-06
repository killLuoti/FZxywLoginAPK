package http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


public class httpclass {
    private static final String TAG = "LLLL";
    private static URL url;
    public String get=null;
    public String post=null;
    public httpclass(String Url) {
        super();
        get = obget(Url,"GET");
        //post = obpost(Url,"POST");
    }
    public httpclass() {
        super();
    }
    //get
    private  String obget(String Url,String method){
        try {
            url =new URL(Url);
            HttpURLConnection httpURLConnection =(HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(1000);
            httpURLConnection.setRequestMethod(method);
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "obget: "+responseCode);
            if(200 == httpURLConnection.getResponseCode()){
//                Log.d(TAG, "obget: "+httpURLConnection.getURL());
//                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(httpURLConnection.getInputStream()));
//                StringBuffer stringBuffer =new StringBuffer();
//                String str = "";
//                while ((str=bufferedReader.readLine())!=null) {
//                    stringBuffer.append(str);
//                }
//
//                bufferedReader.close();
//                return stringBuffer.toString();
                return httpURLConnection.getURL().toString();

            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "ob: "+e.getMessage());
        }
        return "Err";

    }
//post
    private  String obpost(String Url,String method){
        try {
            url =new URL(Url);
            HttpURLConnection httpURLConnection =(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod(method);
            httpURLConnection.connect();
            if(200 == httpURLConnection.getResponseCode()){
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(httpURLConnection.getInputStream()));
                httpURLConnection.disconnect(); //??????http
                StringBuffer stringBuffer =new StringBuffer();
                String str = "";
                while ((str=bufferedReader.readLine())!=null) {
                    stringBuffer.append(str);
                }

                bufferedReader.close();
                return stringBuffer.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "ob: "+e.getMessage());
        }
        return "Err";

    }

    /*
     * Function  :   ??????Post??????????????????
     * Param     :   params??????????????????encode????????????
     * Author    :   ?????????-????????????
     */
    public static String submitPostData(String Url,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//???????????????
        try {
            url =new URL(Url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(2000);        //????????????????????????
            httpURLConnection.setDoInput(true);                  //????????????????????????????????????????????????
            httpURLConnection.setDoOutput(true);                 //????????????????????????????????????????????????
            httpURLConnection.setRequestMethod("POST");     //?????????Post??????????????????
            httpURLConnection.setUseCaches(false);               //??????Post????????????????????????
//            Host: 10.0.0.10:801
//            Connection: keep-alive
//            Content-Length: 168
//            Cache-Control: max-age=0
//            Upgrade-Insecure-Requests: 1
//            User-Agent: Mozilla/5.0 (Linux; Android 10; HLK-AL00; HMSCore 5.3.0.312) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.93 HuaweiBrowser/11.1.1.300 Mobile Safari/537.36
//            Content-Type: application/x-www-form-urlencoded
//            Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
//origin: http://10.0.0.10
//Referer: http://10.0.0.10/a70.htm?wlanuserip=10.13.213.85&wlanacip=10.110.110.114&wlanacname=null&vlanid=0&ip=10.13.213.85&ssid=null&areaID=null&mac=00-00-00-00-00-00&switch_url=null&ap_mac=null&client_mac=null&wlan=null
//Accept-Encoding: gzip, deflate
//Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7
//Cookie: PHPSESSID=j2dpi0ansipj0qgr595jji1tn5
            //???????????????????????????????????????
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("origin", "http://10.0.0.10");
            httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //????????????????????????
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //??????????????????????????????????????????
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            int response = httpURLConnection.getResponseCode();            //???????????????????????????
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //??????????????????????????????
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Function  :   ?????????????????????
     * Param     :   params??????????????????encode????????????
     * Author    :   ?????????-????????????
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //?????????????????????????????????
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //?????????????????????"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }
    /*
     * Function  :   ??????????????????????????????????????????????????????????????????
     * Param     :   inputStream???????????????????????????
     * Author    :   ?????????-????????????
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //??????????????????
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

}
