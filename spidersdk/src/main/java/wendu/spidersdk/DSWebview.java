package wendu.spidersdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by du on 16/12/23.
 */

 class DSWebview extends WebView {

    private String userAgent;
    private String injectUrl="";
    private boolean debug=false;

    public  void setDebug(boolean isDebug){
        debug=isDebug;
    }
    public boolean isDebug(){
        return  debug;
    }
    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    private String reportUrl="";
    private String lastInjectUrl="";

    public void setDebugSrc(String debugSrc) {
        this.debugSrc = debugSrc;
    }

    private String debugSrc="";

    SharedPreferences sharedPreferences;
    private final String contentType = "application/javascript";

    public DSWebview(Context context) {
        super(context);
        init(context);
    }

    public DSWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        sharedPreferences=context.getSharedPreferences("spider", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("jscache").commit();
        WebSettings settings = getSettings();
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
        settings.setAllowFileAccess(false);
        settings.setAppCacheEnabled(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportMultipleWindows(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setUseWideViewPort(true);
        userAgent=settings.getUserAgentString();
        setWebChromeClient(mWebChromeClient);
        setWebViewClient(mWebViewClient);
    }

    public void setUserAgent(String userAgent) {
        getSettings().setUserAgentString(userAgent);
    }

    @Override
    public void loadUrl(final String url) {
        post(new Runnable() {
            @Override
            public void run() {
                if(webEventListener!=null){
                    webEventListener.onPageStart(url);
                }
                DSWebview.super.loadUrl(url);
            }
        });
    }

    @SuppressLint("JavascriptInterface")
    public void addJavascriptInterface(JavaScriptBridge object) {
        super.addJavascriptInterface(object, "_xy");
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        post(new Runnable() {
            @Override
            public void run() {
                String str = additionalHttpHeaders.get("User-Agent");
                if (!TextUtils.isEmpty(str)) {
                    userAgent=getSettings().getUserAgentString();
                    getSettings().setUserAgentString(str);
                }
                if(webEventListener!=null){
                    webEventListener.onPageStart(url);
                }
                DSWebview.super.loadUrl(url, additionalHttpHeaders);
            }
        });

     }


    private WebEventListener webEventListener;

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("xy log","shouldOverrideUrlLoading: "+url);
            if(webEventListener!=null){
                webEventListener.onPageStart(url);
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            if(!TextUtils.isEmpty(userAgent)){
                setUserAgent(userAgent);
                userAgent=null;
            }
            injectJs();
            if(webEventListener!=null){
                webEventListener.onPageFinished(url);
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            if(webEventListener!=null){
                webEventListener.onReceivedError(
                        String.format("{\"url\":\"%s\",\"msg\":\"%s\",\"code\":%d}", failingUrl, description, errorCode));
            }
            onReceivedError(view,errorCode,description,failingUrl);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(final WebView view, WebResourceRequest req, WebResourceError rerr) {
            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }


        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
            WebResourceResponse response = super.shouldInterceptRequest(view, url);

            if (url.indexOf("dspider/dQuery") != -1) {
                try {
                    //加载本地jquery
                    InputStream data = Helper.getDqueryScript(getContext());
                    response = new WebResourceResponse(contentType, "UTF-8", data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else if (url.indexOf("dspider/spider") != -1) {

                if (isDebug()) {
                    try {
                        response=new WebResourceResponse(contentType, "UTF-8", Helper.getDebugScript(getContext(),debugSrc));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String js= sharedPreferences.getString("jscache","");
                        if (TextUtils.isEmpty(js)){
                            js=Helper.post(injectUrl,"");
                            sharedPreferences.edit().putString("jscache",js);
                        }
                        response = new WebResourceResponse(contentType,
                                "UTF-8", new ByteArrayInputStream(js.getBytes()));
                    } catch (final Exception e) {
                        e.printStackTrace();
                        if(webEventListener!=null){
                            webEventListener.onSdkServerError(e);
                        }
                    }
                }
            }
            return response;
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if(webEventListener!=null){
                webEventListener.onProgressChanged(newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            injectJs();
            if(webEventListener!=null){
                webEventListener.onReceivedTitle(title);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.e("dspider sdk:","alert called");
            result.confirm();
            return true;
        }

    };


    public void  autoLoadImg(final boolean load){
        post(new Runnable() {
            @Override
            public void run() {
                getSettings().setLoadsImagesAutomatically(load) ;
            }
        });

    }

    private void injectJs() {
        post(new Runnable() {
            @Override
            public void run() {
                String url= getUrl();
                if(!lastInjectUrl.equals(url)){
                    lastInjectUrl=url;
                    String js = Helper.getFromAssets(getContext(), "injector.js");
                    loadUrl("javascript:" + js);
                }
            }
        });
    }


    public void setInjectUrl(String url){
       injectUrl=url;
    }




    public void setWebEventListener(WebEventListener eventListener){
        webEventListener=eventListener;
    }

    public static abstract class WebEventListener {
        void onPageStart(String url) {
        }

        void onReceivedError(String msg) {
        }

        void onPageFinished(String url) {
        }

        void onSdkServerError(Exception e) {
        }
        void onProgressChanged(int newProgress){

        }
        void onReceivedTitle(String title){

        }
    }


}
