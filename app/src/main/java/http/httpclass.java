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
                httpURLConnection.disconnect(); //关闭http
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
     * Function  :   发送Post请求到服务器
     * Param     :   params请求体内容，encode编码格式
     * Author    :   博客园-依旧淡然
     */
    public static String submitPostData(String Url,Map<String, String> params, String encode) {

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {
            url =new URL(Url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);        //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
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
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("origin", "http://10.0.0.10");
            httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //设置请求体的长度
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     * Author    :   博客园-依旧淡然
     */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }
    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     * Author    :   博客园-依旧淡然
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
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
