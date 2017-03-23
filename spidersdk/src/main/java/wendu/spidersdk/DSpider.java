package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by du on 16/10/8.
 */
public class DSpider  {

    private Activity ctx;
    private HashMap<String, Object> arguments = new HashMap<>();
    private boolean isDebug = false;
    public static int DEVICE_ID;
    public static int REQUEST = 2000;
    public static final String SDK_VERSION = "1.0.0";
    public static  int APPID=0;


    //public static final String  BASE_URL="http://172.19.23.62/dSpider-web/1.0/";
    public static final String BASE_URL = "https://api.dtworkroom.com/1.0/";
    public static final String REPORT_URL = "report";

    private String debugSrcFileName;
    private String debugStartUrl;

    public static void setPersistenceImp(IPersistence iPersistence) {
        DSpider.sPersistence= iPersistence;
    }

    static IPersistence sPersistence;

//    public static final String BASE_URL = "http://119.29.112.230:8589/partner/crawl/";
//    public static final String REPORT_URL = "scriptReport";


    private DSpider(Activity ctx) {
        this.ctx=ctx;
        Helper.APP_CONTEXT = ctx.getApplicationContext();
    }

    public static void init(Context ctx,int appId){
        APPID=appId;
        Helper.APP_CONTEXT=ctx;
    }

    public static DSpider build(Activity ctx) {
        return new DSpider(ctx);
    }

    public static Result getLastResult() {
        if(Helper.APP_CONTEXT== null) return null;
        File file = new File(Helper.APP_CONTEXT.getCacheDir() + "/spider.dat");
        FileInputStream fileInputStream = null;
        Result resultData = null;
        try {
            fileInputStream = new FileInputStream(file.toString());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            resultData = (Result) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultData;
    }

    public static String getLastLog() {
        if(Helper.APP_CONTEXT== null) return null;
        return Helper.APP_CONTEXT.getSharedPreferences("spider", Context.MODE_PRIVATE).getString("_log", "");
    }

    public DSpider addArgument(String key, Object value) {
        arguments.put(key, value);
        return this;
    }


    public void start(int sid,String title) {
        start(sid, title, "");
    }

    private DSpider start(int sid, String title, String startUrl) {
        start_(sid, title, startUrl);
        isDebug=false;
        return this;
    }

    public void startDebug(String title, String debugSrcFileName, String debugStartUrl) {
        isDebug = true;
        this.debugSrcFileName = debugSrcFileName;
        this.debugStartUrl = debugStartUrl;
        start(0, title, "");
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

    private void start_(int sid, String title, String startUrl) {
        if(APPID<1){
            showDialog("SDK未初始化!");
            return;
        }
        if (isDebug) {
            if (TextUtils.isEmpty(debugSrcFileName) || TextUtils.isEmpty(debugStartUrl)) {
                showDialog("缺少调试参数");
                return;
            }
        }
        Intent intent = new Intent();
        intent.setClass(ctx, SpiderActivity.class);
        intent.putExtra("debug", isDebug);
        intent.putExtra("sid", sid);
        intent.putExtra("debugSrc", debugSrcFileName);
        intent.putExtra("startUrl", startUrl);
        intent.putExtra("debugStartUrl", debugStartUrl);
        intent.putExtra("title",title);
        intent.putExtra("arguments", new JSONObject(arguments).toString());
        ctx.startActivityForResult(intent, REQUEST);
    }


    public static class Result implements Serializable {
        public static final int STATE_SUCCEED = 0;
        public static final int STATE_WEB_ERROR = 1;
        public static final int STATE_SCRIPT_ERROR = 2;
        public static final int STATE_PAGE_CHANGED = 3;
        public static final int STATE_TIMEOUT=4;
        public static final int STATE_DSPIDER_SERVER_ERROR = 5;
        public static final int STATE_ERROR_MSG = 6;
        public List<String> datas;
        public String sessionKey;
        public String errorMsg;
        public int code;

        public Result(int errorCode,String msg){
            this.code=errorCode;
            this.errorMsg=msg;
        }
        public Result(String sessionKey, List<String> datas, String errorMsg, int code) {
            this.sessionKey = sessionKey;
            this.datas = datas;
            this.errorMsg = errorMsg;
            this.code = code;
        }
    }


}
