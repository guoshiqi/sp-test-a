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
    public static int DEVICE_ID;
    public static int REQUEST=2000;
    public static final String SDK_VERSION="1.0";
    public static final String  BASE_URL="http://172.19.23.62/dSpider-web/1.0/";
    //public static final String  BASE_URL="http://192.168.1.24/dSpider-web/1.0/";


    private DSpider(Activity ctx){
        this.ctx=ctx;
    }

    public static DSpider build(Activity ctx){
        return new DSpider(ctx);
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

    public void start(int sid){
        start(sid,"","");
    }



    public void start(final int sid,  final String debugSrcFileName, final String debugStartUrl) {
        if (isDebug&& (TextUtils.isEmpty(debugSrcFileName)||TextUtils.isEmpty(debugStartUrl))){
            showDialog("该业务不持调试或却少调试参数");
            return;
        }
        Helper.init(ctx, new InitStateListener() {
            @Override
            public void onSucceed(int deviceId) {
                DEVICE_ID=deviceId;
                start_(sid, debugSrcFileName, debugStartUrl);
            }

            @Override
            public void onFail(String msg,int code) {
              showDialog(msg);
            }
        });

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

    private void start_(int sid,String debugSrcFileName,String debugStartUrl) {

        Intent intent = new Intent();
        intent.setClass(ctx, SpiderActivity.class);
        //intent.putExtra("title", title);
        intent.putExtra("debug", isDebug);
        intent.putExtra("sid",sid);
        intent.putExtra("debugSrc",debugSrcFileName);
        intent.putExtra("startUrl",debugStartUrl);
        intent.putExtra("arguments",new JSONObject(arguments).toString());
        ctx.startActivityForResult(intent, REQUEST);
    }


    public static class Result implements Serializable{
        public static final int STATE_SUCCEED=0;
        public static final int STATE_WEB_ERROR=1;
        public static final int STATE_SCRIPT_ERROR=2;
        public static final int STATE_PAGE_CHANGED=3;
        public static final int STATE_DSPIDER_SERVER_ERROR=4;
        public static final int STATE_ERROR_MSG=5;
        public List<String>datas;
        public String sessionKey;
        public String errorMsg;
        public int  code;
        public Result(String sessionKey, List<String>datas, String errorMsg,int code){
            this.sessionKey=sessionKey;
            this.datas=datas;
            this.errorMsg=errorMsg;
            this.code=code;
        }
    }





}
