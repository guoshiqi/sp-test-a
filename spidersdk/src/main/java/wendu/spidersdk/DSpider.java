package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by du on 16/10/8.
 */
public class DSpider implements Serializable{

    private Activity ctx;
    private HashMap<String,Object> arguments=new HashMap<>();
    private boolean isDebug=false;
    private String appKey="";
    public static int REQUEST=2000;
    public static int DEVICE_ID;
    public static String SDK_VERSION="1.0";
    public static String  BASE_URL="http://172.19.23.62/dSpider-web/api/";

    private DSpider(Activity ctx,String appKey){
        this.ctx=ctx;
        this.appKey=appKey;
    }

    public static DSpider build(Activity ctx,String appKey){
        return new DSpider(ctx,appKey);
    }

    public  static Result getLastResult(Context ctx, boolean clearResultCache){
        File file=new File(ctx.getCacheDir()+"/spider.dat") ;
        FileInputStream fileInputStream = null;
        Result resultData=null;
        try {
            fileInputStream = new FileInputStream(file.toString());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            resultData= (Result) objectInputStream.readObject();
            if(clearResultCache){
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultData;
    }
    public static Result getLastResult(Context ctx){
        return getLastResult(ctx,true);
    }

    public static String getLastLog(Context ctx){
        return ctx.getSharedPreferences("spider", Context.MODE_PRIVATE).getString("_log","");
    }

    public DSpider addArgument(String key,Object value){
        arguments.put(key,value);
        return this;
    }

    public DSpider setDebug(boolean debug){
       isDebug=debug;
       return  this;
    }

    public void start(int sid,String title){
        start(sid,title,"","");
    }

    public void start(final int sid, final String title, final String debugSrcFileName, final String debugStartUrl) {
        if (isDebug&& (TextUtils.isEmpty(debugSrcFileName)||TextUtils.isEmpty(debugStartUrl))){
            showDialog("该业务不持调试或却少调试参数");
            return;
        }
        final int device_id=ctx.getSharedPreferences("spider", Context.MODE_PRIVATE).getInt("device_id",0);
        if(device_id==0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //String extra= URLEncoder.encode(collectDeviceInfo(),"UTF-8");
                        URL uri = new URL(BASE_URL+"device/save?"+collectDeviceInfo());
                        HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                        urlCon.setRequestMethod("GET");
                        urlCon.setRequestProperty("X-Requested-With","XMLHttpRequest");
                        urlCon.setConnectTimeout(10000);
                        JSONObject ret=new JSONObject(Helper.inputStream2String(urlCon.getInputStream()));
                        int code=ret.getInt("code");
                        if(code!=0){
                            showDialog(ret.getString("msg"));
                        }else {
                            DEVICE_ID=ret.getInt("data");
                            ctx.getSharedPreferences("spider", Context.MODE_PRIVATE).edit().putInt("device_id",DEVICE_ID).commit();
                            start_(sid, title, debugSrcFileName, debugStartUrl);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showDialog(e.getMessage());
                    }
                }
            }).start();
        }else {
           DEVICE_ID=device_id;
           start_(sid, title, debugSrcFileName, debugStartUrl);
        }
    }

    private void showDialog(final String msg) {
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(ctx).
                        setTitle("提示").
                        setMessage(msg).
                        setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

    }

    private void start_(int sid,String title,String debugSrcFileName,String debugStartUrl) {

        Intent intent = new Intent();
        intent.setClass(ctx, SpiderActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("debug", isDebug);
        intent.putExtra("sid",sid);
        intent.putExtra("appkey",appKey);
        intent.putExtra("debugSrc",debugSrcFileName);
        intent.putExtra("startUrl",debugStartUrl);
        intent.putExtra("arguments",new JSONObject(arguments).toString());
        ctx.startActivityForResult(intent, REQUEST);
    }


    public static class Result implements Serializable{
        public List<String>datas;
        public String sessionKey;
        public String errorMsg;
        public Result(String sessionKey, List<String>datas, String errorMsg){
            this.sessionKey=sessionKey;
            this.datas=datas;
            this.errorMsg=errorMsg;
        }
    }


    public  String getDeviceId() {
        try{
            TelephonyManager tm = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();

            if( TextUtils.isEmpty(device_id) ){
                device_id = mac;
            }

            if( TextUtils.isEmpty(device_id) ){
                device_id = android.provider.Settings.Secure.getString(ctx.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            }

            return device_id;
        }catch(Exception e){
            e.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }

    //用来存储设备信息和异常信息
    private String  collectDeviceInfo() {
        try {
            return String.format("os_version=%s&os_type=1&model=%s&identifier=%s",
                    Build.VERSION.RELEASE,URLEncoder.encode(Build.MODEL,"UTF-8"),getDeviceId());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
