package wendu.spidersdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import wendu.spidersdk.third.DWebView;

/**
 * Created by du on 16/12/23.
 */

class DSWebview extends DWebView {

    private String userAgent;
    private boolean debug = false;
    private String taskId;
    private String scriptId;
    private String script;
    private int  descendantFocusability;
    private String debugSrc = "";
    private final String contentType = "application/javascript";

    public String getExceptUrl() {
        return exceptUrl;
    }

    public void setExceptUrl(String exceptUrl) {
        this.exceptUrl = exceptUrl;
    }

    private String exceptUrl="";

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setDebug(boolean isDebug) {
        debug = isDebug;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setInjectScript(String script) {
        this.script = script;
    }


    public void setDebugSrc(String debugSrc) {
        this.debugSrc = debugSrc;
    }

    public void enableFocus(boolean enable){
        if(enable){
            setDescendantFocusability(descendantFocusability);
        }else{
            setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        }
    }

    public DSWebview(Context context) {
        super(context);
        init(context);
    }

    public DSWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        descendantFocusability=getDescendantFocusability();
        setWebChromeClient(mWebChromeClient);
        setWebViewClient(mWebViewClient);
    }

    public void setUserAgent(String userAgent) {
        getSettings().setUserAgentString(userAgent);
    }

    @Override
    public void loadUrl(final String url) {

        if (webEventListener != null && url.startsWith("http")) {
            webEventListener.onPageStart(url);
        }
        DSWebview.super.loadUrl(url);


    }

    @SuppressLint("JavascriptInterface")
    public void addJavascriptInterface(JavaScriptBridge object) {
        super.setJavascriptInterface(object);
    }

    public void removeJavascriptInterface() {
        super.setJavascriptInterface(null);
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {

        String str = additionalHttpHeaders.get("User-Agent");
        if (!TextUtils.isEmpty(str)) {
            userAgent = getSettings().getUserAgentString();
            getSettings().setUserAgentString(str);
        }
        if (webEventListener != null && url.startsWith("http")) {
            webEventListener.onPageStart(url);
        }
        DSWebview.super.loadUrl(url, additionalHttpHeaders);

    }


    private WebEventListener webEventListener;

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("xy log", "shouldOverrideUrlLoading: " + url);
            if (webEventListener != null && url.startsWith("http")) {
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
            if (!TextUtils.isEmpty(userAgent)) {
                setUserAgent(userAgent);
                userAgent = null;
            }
            injectJs();
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            if (webEventListener != null && failingUrl.startsWith("http")) {
                webEventListener.onReceivedError(
                        String.format("{\"url\":\"%s\",\"msg\":\"%s\",\"code\":%d}", failingUrl, description, errorCode));
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
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
                        response = new WebResourceResponse(contentType, "UTF-8", Helper.getDebugScript(getContext(), debugSrc));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        response = new WebResourceResponse(contentType,
                                "UTF-8", new ByteArrayInputStream(script.getBytes()));
                    } catch (final Exception e) {
                        e.printStackTrace();
                        if (webEventListener != null) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    webEventListener.onSdkServerError(e);
                                }
                            });

                        }
                    }
                }
                if (webEventListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            webEventListener.onPageFinished(view.getOriginalUrl());
                        }
                    }, 200);
                }
            }
            return response;
        }
    };


    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (webEventListener != null) {
                webEventListener.onProgressChanged(newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (webEventListener != null) {
                webEventListener.onReceivedTitle(title);
            }
        }

    };


    public void autoLoadImg(final boolean load) {
        post(new Runnable() {
            @Override
            public void run() {
                getSettings().setLoadsImagesAutomatically(load);
            }
        });

    }

    private void injectJs() {
        String js = Helper.getFromAssets(getContext(), "injector.js");
        loadUrl("javascript:" + js);
    }


    public void setWebEventListener(WebEventListener eventListener) {
        webEventListener = eventListener;
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

        void onProgressChanged(int newProgress) {

        }

        void onReceivedTitle(String title) {

        }
    }


}
