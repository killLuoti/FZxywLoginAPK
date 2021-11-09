package SP;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedP {
    private String username;
    private String password;
    private Context con;
    private String config;


    public SharedP(){}
    public SharedP(Context con,String config) {
        //        读取用户名和密码
        SharedPreferences sharedPre = con.getSharedPreferences(config, MODE_PRIVATE);
        this.con = con;
        this.config = config;
        this.username = sharedPre.getString("username", "");
        this.password = sharedPre.getString("password", "");
    }

    public void setSharedP(){
        //保存账号密码
        SharedPreferences sharedPre = con.getSharedPreferences(config, MODE_PRIVATE);

        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        //提交
        editor.commit();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
