package wendu.spidersdk;

import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.ProgressBar;

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class SpiderCrossWalkFragment extends BaseFragment {

    private XWalkView mWebView;
    private ProgressBar mProgressBar;
    private SpiderActivity context;
    private String url;
    private String userAgent;
    private final String contentType = "application/javascript";

    public static SpiderCrossWalkFragment newInstance(String url, boolean showBack) {
        SpiderCrossWalkFragment fragment = new SpiderCrossWalkFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View  onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spider_crosswalk, container, false);
        Log.e("spider", "crosswalk loaded!");
        url = getArguments().getString("url");
        mWebView = (XWalkView) rootView.findViewById(R.id.webview);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        showLoadProgress();
        context= (SpiderActivity) getActivity();
        initXWalks();
        return rootView;
    }

    public void loadUrl(String url){
        mWebView.load(url,null);
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
                mWebView.load(url,null,additionalHttpHeaders);
            }
        });

    }

    @Override
    void setUserAgent(String userAgent) {
        mWebView.getSettings().setUserAgentString(userAgent);
    }

    void initXWalks(){
        //置是否允许通过file url加载的Javascript可以访问其他的源,包括其他的文件和http,https等其他的源
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, false);
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, false);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        mWebView.setUIClient(new WalkUIClient(mWebView));
        mWebView.setResourceClient(new WalkResourceClient(mWebView));
        mWebView.setDrawingCacheEnabled(false);
        //mWebView.clearCache();
        mWebView.addJavascriptInterface(new JavaScriptBridgeForCrossWalk(getActivity()), "_xy");
        XWalkSettings settings = mWebView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        userAgent=settings.getUserAgentString();
        mWebView.load(url, null);
    }



    private void showLoadProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    private class WalkUIClient extends XWalkUIClient {
        public WalkUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, String url) {
            showLoadProgress();
            super.onPageLoadStarted(view, url);
        }

        @Override
        public void onReceivedTitle(XWalkView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
            super.onPageLoadStopped(view, url, status);
            if(!TextUtils.isEmpty(userAgent)){
                setUserAgent(userAgent);
                userAgent=null;
            }
            injectJs(view);
        }
    }



    private  class WalkResourceClient  extends XWalkResourceClient {

        public WalkResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
            super.onReceivedSslError(view, callback, error);
        }

        @Override
        public void onProgressChanged(XWalkView view, int progressInPercent) {
            if (progressInPercent >= 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                mProgressBar.setProgress(progressInPercent);
            }
            super.onProgressChanged(view, progressInPercent);
        }

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
            if (view.getUrl().equals(failingUrl)) {
                context.showLoadErrorView();
            }
            super.onReceivedLoadError(view, errorCode, description, failingUrl);
        }



        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            showLoadProgress();
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(final XWalkView view, XWalkWebResourceRequest request) {
            String url=request.getUrl().toString();
            XWalkWebResourceResponse response=null;

            if (url.indexOf("xiaoying/jquery.min.js") != -1) {
                try {
                    //加载本地jquery
                    InputStream data =  Helper.getDqueryScript(getContext());
                    response = createXWalkWebResourceResponse(contentType, "UTF-8", data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            } else if (url.indexOf("xiaoying/inject.php") != -1) {
                if (Helper.isDebug) {
                    try {
                        response = createXWalkWebResourceResponse(contentType, "UTF-8", Helper.getDebugScript(getContext()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {

                    try {
                        URL uri = new URL(SpiderActivity.INJECT_URL + "?platform=android&refer=" + url.substring(url.indexOf("refer=") + 6));
                        HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                        urlCon.setRequestMethod("GET");
                        response = createXWalkWebResourceResponse(contentType,
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
            return response==null?super.shouldInterceptLoadRequest(view,request):response;
        }
    }


    @Override
    public void errorReload() {
        mWebView.reload(mWebView.RELOAD_IGNORE_CACHE);
    }

    @Override
    public Boolean goBack() {
        XWalkNavigationHistory history=mWebView.getNavigationHistory();
        if (history.canGoBack()){
            history.navigate(XWalkNavigationHistory.Direction.BACKWARD,1);
            return true;
        }else {
            return false;
        }
    }

    void injectJs(XWalkView webView) {
        String js = Helper.getFromAssets(getContext(), "injector.js");
        webView.load("javascript:" + js,null);
    }

    @Override
    public void onDestroyView() {
        XWalkCookieManager cookieManager=new XWalkCookieManager();
        cookieManager.removeSessionCookie();//移除
        cookieManager.removeAllCookie();
        cookieManager.flushCookieStore();
        mWebView.onDestroy();
        super.onDestroyView();
    }
}
