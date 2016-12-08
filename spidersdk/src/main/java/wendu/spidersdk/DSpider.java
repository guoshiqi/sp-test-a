package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

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
    private DSpider(Activity ctx){
        this.ctx=ctx;
    }

    public static DSpider init(Activity ctx){
        return new DSpider(ctx);
    }

    public  static Result getResult(Context ctx, boolean clearResultCache){
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
    public static Result getResult(Context ctx){
        return getResult(ctx,true);
    }


    public static String getLog(Context ctx){
        return ctx.getSharedPreferences("spider", Context.MODE_PRIVATE).getString("_log","");
    }

    public DSpider setArgument(String key,Object value){
        arguments.put(key,value);
        return this;
    }

    public DSpider setDebug(boolean debug ){
       isDebug=debug;
       return  this;
    }

    public void start(String startUrl, String scriptUrl,String title,String debugSrcFileName,boolean scriptCached) {
        if (isDebug&& TextUtils.isEmpty(debugSrcFileName)){
            //showDialog("该业务暂不支持调试！");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(ctx, SpiderActivity.class);

        //将要打开页面url
        intent.putExtra("url",startUrl);
        //注入url
        intent.putExtra("inject", scriptUrl);
        intent.putExtra("title", title);
        //调试模式
        intent.putExtra("debug", isDebug);
        intent.putExtra("cache",scriptCached );
        intent.putExtra("debugSrc",debugSrcFileName);
        ctx.startActivityForResult(intent, 1);
    }

    public static class Result{
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
