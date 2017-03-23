package wendu.spidersdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by du on 16/8/17.
 */

class JavaScriptBridgeImp {

    private HashMap<String, String> session = new HashMap<>();
    private HashMap<String, List<String>> datas = new HashMap<>();
    private SharedPreferences sharedPreferences;
    JavaScriptHandler mJavaScriptHandler;
    DSWebview mWebview;

    public JavaScriptBridgeImp(DSWebview webview, JavaScriptHandler javaScriptHandler) {
        mWebview = webview;
        mJavaScriptHandler = javaScriptHandler;
        sharedPreferences = webview.getContext().getSharedPreferences("spider", Context.MODE_PRIVATE);

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
        if (DSpider.sPersistence == null) {
            if (TextUtils.isEmpty(value)) {
                sharedPreferences.edit().remove(key).commit();
            } else {
                sharedPreferences.edit().putString(key, value).commit();
            }
        }else {
            DSpider.sPersistence.save(key,value);
        }
    }

    public String read(String key) {
        if (DSpider.sPersistence == null) {
            return sharedPreferences.getString(key, "");
        }else {
            return DSpider.sPersistence.read(key);
        }
    }

    public String clear(String sessionKey) {
        return session.remove(sessionKey);
    }


    public String getExtraData() {
        Map<String, Object> info = new HashMap<>();
        info.put("os_version", Build.VERSION.RELEASE);
        info.put("os", "android");
        info.put("sdk_version", DSpider.SDK_VERSION);
        JSONObject jsonObject = new JSONObject(info);
        return jsonObject.toString();

    }

    public String getArguments() {
        return mJavaScriptHandler.getArguments();
    }

    public void setArguments(String json) {
        mJavaScriptHandler.setArguments(json);
    }


    public boolean push(String sessionKey, String value) {
        List<String> list = datas.get(sessionKey);
        if (list == null) {
            return false;
        }
        return list.add(value);
    }


    public void setProgress(final int progress) {
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mJavaScriptHandler.setProgress(progress);
            }
        });

    }


    public void setProgressMax(final int progress) {
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mJavaScriptHandler.setProgressMax(progress);
            }
        });

    }

    public void showProgressExcept(String url) {
        mWebview.setExceptUrl(url);
    }

    public void finish(final String sessionKey, final int code, final String msg) {
        //已经关闭
        if (datas.get(sessionKey) == null) {
            return;
        }

        if (!mWebview.isDebug()) {
            reportState(code, msg);
        }
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mWebview.removeJavascriptInterface();
                CookieManager.getInstance().removeAllCookie();
                CookieSyncManager.getInstance().sync();
                mJavaScriptHandler.finish(new DSpider.Result(sessionKey, datas.get(sessionKey), msg, code));
                mWebview.loadUrl("javascript: window.close()");
                datas.remove(sessionKey);
            }
        });

    }

    public void reportState(final int result, final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("state", result + "");
                    map.put("script_id", mWebview.getScriptId());
                    map.put("task_id",mWebview.getTaskId());
                    map.put("msg", msg);
                    String s = Helper.post(DSpider.BASE_URL+DSpider.REPORT_URL, map);
                    Log.d("dspider finish!", s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void showProgress(final boolean show) {
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mJavaScriptHandler.showProgress(show);
            }
        });
    }


    public void setProgressMsg(final String msg) {
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mJavaScriptHandler.setProgressMsg(msg);
            }
        });

    }


    public void load(String url, String headers) {
        if (!TextUtils.isEmpty(headers)) {
            mWebview.loadUrl(url, Helper.getMapForJson(headers));
        }
    }


    public void setUserAgent(String userAgent) {
        mWebview.setUserAgent(userAgent);
    }


    public void autoLoadImg(boolean load) {
        mWebview.autoLoadImg(load);
    }


    public void log(final String msg, final int type) {
        String str = read("_log");
        save("_log", str + "dSpider: " + msg + "\n\n");
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mJavaScriptHandler.log(msg, type);
            }
        });

    }

}
