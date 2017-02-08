package wendu.spidersdk;

import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by du on 16/8/17.
 */

class JavaScriptBridge {

    private JavaScriptBridgeImp jsbImp;

    public JavaScriptBridge(DSWebview webview, JavaScriptHandler javaScriptHandler) {
        jsbImp = new JavaScriptBridgeImp(webview, javaScriptHandler);
    }

    @JavascriptInterface
    public void start(JSONObject jsonObject) throws JSONException {
        jsbImp.start(jsonObject.getString("sessionKey"));
    }

    @JavascriptInterface
    public void set(JSONObject jsonObject) throws JSONException {
        jsbImp.set(jsonObject.getString("key"), jsonObject.getString("value"));
    }

    @JavascriptInterface
    public String get(JSONObject jsonObject) throws JSONException {
        return jsbImp.get(jsonObject.getString("key"));
    }

    @JavascriptInterface
    public void save(JSONObject jsonObject) throws JSONException {
        jsbImp.save(jsonObject.getString("key"), jsonObject.getString("value"));
    }
    @JavascriptInterface
    public String read(JSONObject jsonObject) throws JSONException {
        return jsbImp.read(jsonObject.getString("key"));
    }

    @JavascriptInterface
    public String clear(JSONObject jsonObject) throws JSONException {
        return jsbImp.clear(jsonObject.getString("sessionKey"));
    }

    @JavascriptInterface
    public String getExtraData(JSONObject jsonObject) {
        return jsbImp.getExtraData();
    }

    @JavascriptInterface
    public void push(JSONObject jsonObject) throws JSONException {
        jsbImp.push(jsonObject.getString("sessionKey"), jsonObject.getString("value"));
    }

    @JavascriptInterface
    public void setProgress(JSONObject jsonObject) throws JSONException {
        jsbImp.setProgress(jsonObject.getInt("progress"));
    }

    @JavascriptInterface
    public void setProgressMax(JSONObject jsonObject) throws JSONException {
        jsbImp.setProgressMax(jsonObject.getInt("progress"));
    }


    @JavascriptInterface
    public void finish(JSONObject jsonObject) throws JSONException {
        jsbImp.finish(jsonObject.getString("sessionKey"), jsonObject.getInt("result"), jsonObject.getString("msg"));
    }

    @JavascriptInterface
    public void showProgress(JSONObject jsonObject) throws JSONException {
        jsbImp.showProgress(jsonObject.getBoolean("show"));
    }

    @JavascriptInterface
    public String getArguments(JSONObject jsonObject) {
        return jsbImp.getArguments();
    }

    @JavascriptInterface
    public void setArguments(JSONObject jsonObject) throws JSONException {
        jsbImp.setArguments(jsonObject.getString("args"));
    }

    @JavascriptInterface
    public void load(JSONObject jsonObject) throws JSONException {
        jsbImp.load(jsonObject.getString("url"), jsonObject.getString("headers"));
    }

    @JavascriptInterface
    public void setUserAgent(JSONObject jsonObject) throws JSONException {
        jsbImp.setUserAgent(jsonObject.getString("userAgent"));
    }

    @JavascriptInterface
    public void autoLoadImg(JSONObject jsonObject) throws JSONException {
        jsbImp.autoLoadImg(jsonObject.getBoolean("load"));
    }

    @JavascriptInterface
    public void log(JSONObject jsonObject) throws JSONException {

        jsbImp.log(jsonObject.getString("msg"), jsonObject.getInt("type"));
    }

    @JavascriptInterface
    public void setProgressMsg(JSONObject jsonObject) throws JSONException {
        jsbImp.setProgressMsg(jsonObject.getString("msg"));
    }

}
