package wendu.spidersdk;
import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * Created by du on 16/8/17.
 */

 class JavaScriptBridge {

    private JavaScriptBridgeImp jsbImp;
    public JavaScriptBridge(DSWebview webview, JavaScriptHandler javaScriptHandler) {
        jsbImp=new JavaScriptBridgeImp(webview,javaScriptHandler);
    }

    @JavascriptInterface
    public void start(String sessionKey) {
        jsbImp.start(sessionKey);
    }

    @JavascriptInterface
    public void set(String sessionKey, String value) {
        jsbImp.set(sessionKey, value);
    }

    @JavascriptInterface
    public String get(String key) {
        return jsbImp.get(key);
    }

    @JavascriptInterface
    public void save(String key, String value) {
        jsbImp.save(key, value);
    }
    @JavascriptInterface
    public String read(String key){
        return jsbImp.read(key);
    }
    @JavascriptInterface
    public String clear(String sessionKey) {
        return jsbImp.clear(sessionKey);
    }

    @JavascriptInterface
    public String getExtraData() {
       return jsbImp.getExtraData();
    }

    @JavascriptInterface
      public boolean push(String sessionKey, String value) {
        return jsbImp.push(sessionKey, value);
    }

    @JavascriptInterface
    public void setProgress(int progress) {
       jsbImp.setProgress(progress);
    }

    @JavascriptInterface
    public void setProgressMax(int progress) {
        jsbImp.setProgressMax(progress);
    }


    @JavascriptInterface
    public void finish(String name,int res,String msg) {
        jsbImp.finish(name,res,msg);
    }

    @JavascriptInterface
    public void showProgress(boolean show) {
        jsbImp.showProgress(show);
    }

    @JavascriptInterface
    public String getArguments(){return jsbImp.getArguments();}

    @JavascriptInterface
    public void load(String url, String headers){
       jsbImp.load(url,headers);
    }

    @JavascriptInterface
    public void setUserAgent(String userAgent) {
        jsbImp.setUserAgent(userAgent);
    }

    @JavascriptInterface
    public void autoLoadImg(boolean load) {
        jsbImp.autoLoadImg(load);
    }

    @JavascriptInterface
    public void log(String msg,int type) {
        jsbImp.log(msg,type);
    }

    @JavascriptInterface
    public void setProgressMsg(String msg) {
        jsbImp.setProgressMsg(msg);
    }

}
