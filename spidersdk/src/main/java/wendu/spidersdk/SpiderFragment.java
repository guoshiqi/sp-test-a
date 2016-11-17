package wendu.spidersdk;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SpiderFragment extends BaseFragment {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private SpiderActivity context;
    private String url;
    private String userAgent;
    private final String contentType = "application/javascript";

    public static SpiderFragment newInstance(String url, boolean showBack) {
        SpiderFragment fragment = new SpiderFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putBoolean("showBack", showBack);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spider, container, false);
        Log.e("spider", "system webview loaded!");
        url = getArguments().getString("url");
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        showLoadProgress();
        context= (SpiderActivity) getActivity();

        mWebView = (WebView) rootView.findViewById(R.id.webview);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.addJavascriptInterface(new JavaScriptBridge(getActivity()), "_xy");
        WebSettings settings = mWebView.getSettings();
        mWebView.getSettings().setDomStorageEnabled(true);
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
        loadUrl(url);

        return rootView;
    }



    private void showLoadProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    public void loadUrl(String url) {
        mWebView.loadUrl(url);
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

    @Override
    void setUserAgent(String userAgent) {
        mWebView.getSettings().setUserAgentString(userAgent);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
            injectJs(view);
            //context.hideLoadView();
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
                        URL uri = new URL(SpiderActivity.INJECT_URL + "&platform=android&refer=" + url.substring(url.indexOf("refer=") + 6));
                        HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                        urlCon.setRequestMethod("GET");
                        response = new WebResourceResponse(contentType,
                                "UTF-8", urlCon.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                context.showLoadErrorView();
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

    void injectJs(WebView webView) {
        String js = Helper.getFromAssets(getContext(), "injector.js");
        webView.loadUrl("javascript:" + js);
    }

}
