package wendu.spidersdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AlertDialog;
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
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by du on 16/12/23.
 */

class DSWebview extends WebView {

    private String userAgent;
    private boolean debug = false;
    private String taskId;
    private String script;

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

    private String debugSrc = "";
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
        WebSettings settings = getSettings();
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        }
        CookieManager.getInstance().removeAllCookie();
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
        userAgent = settings.getUserAgentString();
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
                if (webEventListener != null && url.startsWith("http")) {
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
                    userAgent = getSettings().getUserAgentString();
                    getSettings().setUserAgentString(str);
                }
                if (webEventListener != null&& url.startsWith("http")) {
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
            Log.e("xy log", "shouldOverrideUrlLoading: " + url);
            if (webEventListener != null) {
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
            if (webEventListener != null) {
                webEventListener.onPageFinished(url);
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            if (webEventListener != null) {
                webEventListener.onReceivedError(
                        String.format("{\"url\":\"%s\",\"msg\":\"%s\",\"code\":%d}", failingUrl, description, errorCode));
            }
            super.onReceivedError(view, errorCode, description, failingUrl);
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
                            webEventListener.onSdkServerError(e);
                        }
                    }
                }
            }
            return response;
        }
    };

    public void clear() {

    }

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

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
            Log.e("dspider sdk:", "alert called");
            result.confirm();
            post(new Runnable() {
                @Override
                public void run() {
                    Dialog alertDialog = new AlertDialog.Builder(getContext()).
                            setTitle("提示").
                            setMessage(message).
                            setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
            return true;
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
        post(new Runnable() {
            @Override
            public void run() {
                String js = Helper.getFromAssets(getContext(), "injector.js");
                loadUrl("javascript:" + js);
            }
        });
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

    private static final String APP_CACAHE_DIRNAME = "/webcache";

    public void clearCache() {
        CookieManager.getInstance().removeAllCookie();
        Context context = getContext();
        //清理Webview缓存数据库
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(context.getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME);
        File webviewCacheDir = new File(context.getCacheDir().getAbsolutePath() + "/webviewCache");

        //删除webview 缓存目录
        if (webviewCacheDir.exists()) {
            deleteFile(webviewCacheDir);
        }
        //删除webview 缓存 缓存目录
        if (appCacheDir.exists()) {
            deleteFile(appCacheDir);
        }


    }

    public void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e("Webview", "delete file no exists " + file.getAbsolutePath());
        }
    }


}
