package wendu.spidersdk;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SpiderX5Fragment extends BaseFragment {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private SpiderActivity  context;
    private String mTitle;
    private String url;
    private boolean showBack;
    private String userAgent;
    private final String contentType = "application/javascript";

    public static SpiderX5Fragment newInstance(String url, boolean showBack) {
        SpiderX5Fragment fragment = new SpiderX5Fragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putBoolean("showBack", showBack);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_x5, container, false);
        Log.e("spider", "x5 loaded!");
        url = getArguments().getString("url");
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        showLoadProgress();
        context= (SpiderActivity) getActivity();
        mWebView = (WebView) rootView.findViewById(R.id.webview);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.addJavascriptInterface(new JavaScriptBridge(getActivity()), "_xy");
        mWebView.clearCache(true);
        initWebViewSettings();
        loadUrl(url);
        return rootView;
    }


    private void initWebViewSettings() {
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(false);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(false);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setSavePassword(false);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setMixedContentMode(0);

    }

    View.OnClickListener onClickBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                getActivity().finish();
            }
        }
    };


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

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, final String failingUrl) {
            if (view.getUrl().equals(failingUrl)) {
                context.showLoadErrorView();
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
            if (view.getUrl().equals(webResourceRequest.getRequestHeaders())) {
                context.showLoadErrorView();
            }
            super.onReceivedHttpError(view, webResourceRequest, webResourceResponse);
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
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
                        response = new WebResourceResponse(contentType, "UTF-8", Helper.getDebugScript(getContext()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {

                    try {
                        URL uri = new URL(SpiderActivity.INJECT_URL + "?platform=android&refer=" + url.substring(url.indexOf("refer=") + 6));
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
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                mTitle = title;
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setProgress(newProgress);
            }
        }

    };

    void injectJs(WebView webView) {
        String js = Helper.getFromAssets(this.getContext(), "injector.js");
        webView.loadUrl("javascript:" + js);
    }

    @Override
    public void onDestroyView() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();//移除
        cookieManager.removeAllCookie();
        cookieManager.flush();
        mWebView.destroy();
        super.onDestroyView();
    }
}
