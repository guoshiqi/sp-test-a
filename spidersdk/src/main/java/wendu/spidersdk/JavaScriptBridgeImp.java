package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by du on 16/8/17.
 */

 class JavaScriptBridgeImp {
    private SpiderActivity mContxt;
    private HashMap<String, String> session = new HashMap<>();
    private HashMap<String, List<String>> datas = new HashMap<>();
    private SharedPreferences sharedPreferences;


    public JavaScriptBridgeImp(Context mContxt) {
        this.mContxt = (SpiderActivity) mContxt;
        sharedPreferences=mContxt.getSharedPreferences("spider", Context.MODE_PRIVATE);
    }

    public void start(String sessionKey) {
        if (datas.get(sessionKey) == null) {
            datas.put(sessionKey, new ArrayList<String>());
        }
    }

    public void set(String sessionKey, String value) {
        session.put(sessionKey, value);
    }
    public String get(String sessionKey) {
        return session.get(sessionKey);
    }
    public void save(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
        //KvStorage.getInstance().commit();
    }

    public String read(String key){
       return sharedPreferences.getString(key, "");
    }


    public String clear(String sessionKey) {
        return session.remove(sessionKey);
    }


    public String getExtraData() {
        return String.format("{\"webcore\":\"%s\"}",mContxt.getCurrentCore());
    }


      public boolean push(String sessionKey, String value) {
          List<String> list= datas.get(sessionKey);
          if (list==null){
              return false;
          }
          return list.add(value);
      }



    public void setProgress(int progress) {
        Message message = new Message();
        message.what = 1;
        message.arg1 = progress;
        mContxt.getHandler().sendMessage(message);
    }


    public void setProgressMax(int progress) {
        Message message = new Message();
        message.what = 2;
        message.arg1 = progress;
        mContxt.getHandler().sendMessage(message);
    }


    public int getProgress() {
        return mContxt.getProgress().getProgress();
    }


    public void finish(String sessionKey,int reslut,String msg) {

        //已经关闭
        if (datas.get(sessionKey)==null){
            return;
        }

        //网络错误
        if (reslut==1){
            mContxt.getHandler().sendEmptyMessage(6);
        }else {
            Message message = new Message();
            message.what = 7;
            message.obj=new ResultData(sessionKey,datas.get(sessionKey),msg);
            mContxt.getHandler().sendMessage(message);
//            Intent intent=new Intent();
//            intent.putExtra("result", resultData);
//            mContxt.setResult(Activity.RESULT_OK, intent);
//            mContxt.finish();
        }
        datas.remove(sessionKey);
    }


    public void showProgress(boolean show) {
        Message message = new Message();
        message.what = 3;
        message.obj = show;
        mContxt.getHandler().sendMessage(message);
    }


    public void showLoading(String s) {
        Message message = new Message();
        message.what = 4;
        message.obj = s;
        mContxt.getHandler().sendMessage(message);
    }

    public void hideLoading() {
        Message message = new Message();
        message.what = 5;
        mContxt.getHandler().sendMessage(message);
    }

    public void   openWithSpecifiedCore(String url, String webcore){
        if (TextUtils.isEmpty(webcore)){
            webcore="x5|sys|cs";
        }
        if (webcore.startsWith(mContxt.getCurrentCore())){
            mContxt.loadUrl(url);
            return;
        }
        mContxt.open(url,webcore);
    }

    public void load(String url, String headers){
        if (!TextUtils.isEmpty(headers)){
            mContxt.loadUrl(url,Helper.getMapForJson(headers));
        }
    }

    public void setUserAgent(String userAgent) {
        mContxt.setUserAgent(userAgent);
    }

}
