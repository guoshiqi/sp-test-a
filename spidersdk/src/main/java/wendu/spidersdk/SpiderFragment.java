package wendu.spidersdk;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
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
import android.widget.ProgressBar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SpiderFragment extends BaseFragment {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private SpiderActivity context;
    private String mUrl;
    private String userAgent;
    private String lastInjectUrl="";
    private String injectUrl="";
    private final String contentType = "application/javascript";
    SharedPreferences sharedPreferences;

    public static SpiderFragment newInstance(String url,String injectUrl ) {
        SpiderFragment fragment = new SpiderFragment();
        Bundle args = new Bundle();
        args.putString("mUrl", url);
        args.putString("injectUrl", injectUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_spider, container, false);

        mUrl = getArguments().getString("mUrl");
        injectUrl=getArguments().getString("injectUrl");

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        showLoadProgress();
        context= (SpiderActivity) getActivity();
        sharedPreferences=context.getSharedPreferences("spider", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove("jscache").commit();
        mWebView = (WebView) rootView.findViewById(R.id.webview);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.addJavascriptInterface(new JavaScriptBridge(getActivity()), "_xy");
        WebSettings settings = mWebView.getSettings();
        mWebView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView,true);
        }
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAppCacheEnabled(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportMultipleWindows(true);
        if (Build.VERSION.SDK_INT >= 21) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setUseWideViewPort(true);
        loadUrl(mUrl);
        return rootView;
    }



    private void showLoadProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    public void loadUrl(final String url) {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(url);
            }
        });
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders){
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                String str=additionalHttpHeaders.get("User-Agent");
                if(!TextUtils.isEmpty(str)){
                    userAgent=mWebView.getSettings().getUserAgentString();
                   mWebView.getSettings().setUserAgentString(str);
                }
                mWebView.loadUrl(url,additionalHttpHeaders);
            }
        });

    }

    public void  autoLoadImg(final boolean load){
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.getSettings().setLoadsImagesAutomatically(load) ;
            }
        });

    }

    @Override
    void setUserAgent(String userAgent) {
        mWebView.getSettings().setUserAgentString(userAgent);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("xy log","shouldOverrideUrlLoading: "+url);
            showLoadProgress();
            return super.shouldOverrideUrlLoading(view, url);
        }


        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            if(!TextUtils.isEmpty(userAgent)){
                setUserAgent(userAgent);
                userAgent=null;
            }
            injectJs();
        }



        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            if (view.getUrl().equals(failingUrl)) {
                context.showLoadErrorView();
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(final WebView view, WebResourceRequest req, WebResourceError rerr) {
            if (view.getUrl().equals(req.getRequestHeaders())) {
                context.showLoadErrorView();
            }
            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
             WebResourceResponse response = super.shouldInterceptRequest(view, url);

            if (url.indexOf("xiaoying/jquery.min.js") != -1) {
                try {
                    //加载本地jquery
                    InputStream data = Helper.getDqueryScript(getContext());
                    response = new WebResourceResponse(contentType, "UTF-8", data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else if (url.indexOf("xiaoying/inject.php") != -1) {
                if (Helper.isDebug) {
                    try {
                        response=new WebResourceResponse(contentType, "UTF-8", Helper.getDebugScript(getContext()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String js= sharedPreferences.getString("jscache","");
                        if (TextUtils.isEmpty(js)){
                            URL uri = new URL(injectUrl);
                            HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                            urlCon.setRequestMethod("GET");
                            urlCon.setConnectTimeout(10000);
                            urlCon.setRequestProperty("X-Requested-With","XMLHttpRequest");
                            js=Helper.inputStream2String(urlCon.getInputStream());
                            sharedPreferences.edit().putString("jscache",js);
                        }
                        response = new WebResourceResponse(contentType,
                                "UTF-8", new ByteArrayInputStream(js.getBytes()));
                    } catch (final Exception e) {
                        e.printStackTrace();
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                Dialog alertDialog = new AlertDialog.Builder(context).
                                        setTitle("提示").
                                        setMessage("服务器罢工了！"+e.getMessage()).
                                        setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                context.onBackPressed();
                                            }
                                        }).create();
                              alertDialog.show();
                            }
                       });
                    }
                }
            }
            return response;
        }
    };

    @Override
    public void errorReload() {
        mWebView.reload();
    }

    @Override
    public Boolean goBack() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
            return true;
        }else {
            return false;
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setProgress(newProgress);
            }

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            injectJs();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.e("dspider sdk:","alert called");
            result.confirm();
            return true;
        }

    };

    @Override
    public void onDestroyView() {

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();//移除
        cookieManager.removeAllCookie();
        mWebView.clearCache(true);
        //mWebView.destroy();
        super.onDestroyView();
    }

    void injectJs() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
               String url= mWebView.getUrl();
                if(!lastInjectUrl.equals(url)){
                    lastInjectUrl=url;
                    String js = Helper.getFromAssets(getContext(), "injector.js");
                    mWebView.loadUrl("javascript:" + js);
                }
            }
        });
    }

}
