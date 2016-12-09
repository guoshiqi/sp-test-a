package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
public class DSpider implements Serializable{

    private Activity ctx;
    private HashMap<String,Object> arguments=new HashMap<>();
    private boolean isDebug=false;
    private String appKey="";
    public static int REQUEST=2000;
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

    public void start(int sid,String title,String debugSrcFileName,String debugStartUrl) {
        if (isDebug&& (TextUtils.isEmpty(debugSrcFileName)||TextUtils.isEmpty(debugStartUrl))){
            new AlertDialog.Builder(ctx).
                    setTitle("提示").
                    setMessage("该业务不持调试或却少调试参数").
                    setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            return;
        }
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


}
