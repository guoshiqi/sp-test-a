package wendu.spidersdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
        sharedPreferences = mContxt.getSharedPreferences("spider", Context.MODE_PRIVATE);
    }

    public void start(String sessionKey) {
        save("_log", "");
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
        if (TextUtils.isEmpty(value)) {
            sharedPreferences.edit().remove(key).commit();
        } else {
            sharedPreferences.edit().putString(key, value).commit();
        }
    }

    public String read(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String clear(String sessionKey) {
        return session.remove(sessionKey);
    }


    public String getExtraData() {
        Map<String, Object> info = new HashMap<>();
        info.put("os_version", Build.VERSION.RELEASE );
        info.put("os", "android");
        info.put("sdk_version", DSpider.SDK_VERSION);
        JSONObject jsonObject = new JSONObject(info);
        return jsonObject.toString();
    }

    public String getArguments(){
        Map<String, Object> info = new HashMap<>();
        JSONObject arguments=null;
        try {
            arguments=new JSONObject(mContxt.arguments);
            return arguments.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{}";
    }


    public boolean push(String sessionKey, String value) {
        List<String> list = datas.get(sessionKey);
        if (list == null) {
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


    public void finish(String sessionKey, int reslut, String msg) {

        //已经关闭
        if (datas.get(sessionKey) == null) {
            return;
        }
        //网络错误
        if (reslut == 1) {
            mContxt.getHandler().sendEmptyMessage(6);
        } else {
            reportState(reslut,msg);
            Message message = new Message();
            message.what = 7;
            message.obj = new DSpider.Result(sessionKey, datas.get(sessionKey), msg);
            mContxt.getHandler().sendMessage(message);


        }
        datas.remove(sessionKey);

    }

    private void reportState(final int result, final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL  uri = new URL(mContxt.reportUrl);
                    HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                    urlCon.setRequestMethod("POST");
                    urlCon.setDoOutput(true);
                    urlCon.setDoInput(true);
                    urlCon.setRequestProperty("X-Requested-With","XMLHttpRequest");
                    urlCon.setConnectTimeout(30000);
                    PrintWriter pw = new PrintWriter(urlCon.getOutputStream());
                    String data=URLEncoder.encode(msg,"UTF-8");
                    pw.print(String.format("&state=%d&msg=%s",result,data));
                    pw.flush();
                    pw.close();
                    String s= Helper.inputStream2String(urlCon.getInputStream());
                    Log.d("dSpider sdk",s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void showProgress(boolean show) {
        Message message = new Message();
        message.what = 3;
        message.obj = show;
        mContxt.getHandler().sendMessage(message);
    }

    public void setProgressMsg(String msg) {

    }

    public void load(String url, String headers) {
        if (!TextUtils.isEmpty(headers)) {
            mContxt.loadUrl(url, Helper.getMapForJson(headers));
        }
    }

    public void setUserAgent(String userAgent) {
        mContxt.setUserAgent(userAgent);
    }

    public void autoLoadImg(boolean load) {
        mContxt.autoLoadImg(load);
    }

    public void log(String msg,int type) {
        String str = read("_log");
        save("_log", str + "dSpider: " + msg + "\n\n");
    }



}
