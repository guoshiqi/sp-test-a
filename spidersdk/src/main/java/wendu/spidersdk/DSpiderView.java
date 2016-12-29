package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by du on 16/4/19.
 */
public class DSpiderView extends LinearLayout {

    private DSWebview webview;
    private SpiderEventListener spiderEventListener;
    private int max = 100;
    Map<String, Object> arguments;

    public DSpiderView(Context context) {
        super(context);
    }

    public DSpiderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        webview = new DSWebview(context);
        this.addView(webview, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        webview.setWebEventListener(new DSWebview.WebEventListener() {
            @Override
            void onPageStart(String url) {
                super.onPageStart(url);
            }

            @Override
            void onReceivedError(String msg) {
                super.onReceivedError(msg);
                if (spiderEventListener != null) {
                    spiderEventListener.onError(DSpider.Result.STATE_WEB_ERROR, msg);
                }
            }

            @Override
            void onSdkServerError(Exception e) {
                super.onSdkServerError(e);
                if (spiderEventListener != null) {
                    spiderEventListener.onError(DSpider.Result.STATE_DSPIDER_SERVER_ERROR, e.getMessage());
                }
            }

        });

        webview.addJavascriptInterface(new JavaScriptBridge(webview, new JavaScriptHandler() {
            @Override
            public void setProgress(int progress) {
                if (spiderEventListener != null) {
                    spiderEventListener.onProgress(progress, max);
                }
            }

            @Override
            public void setProgressMax(int progress) {
                max = progress;
            }

            @Override
            public void finish(DSpider.Result result) {
                if (spiderEventListener != null) {
                    if (result.code == DSpider.Result.STATE_SUCCEED) {
                        spiderEventListener.onResult(result.sessionKey, result.datas);
                    } else {
                        spiderEventListener.onError(result.code, result.errorMsg);
                    }
                }
            }

            @Override
            public String getArguments() {
                try {
                    return new JSONObject(arguments).toString();
                } catch (Exception e) {
                    return "{}";
                }
            }
        }));

    }

    public void startDebug(String startUrl, String debugSrc) {
        webview.setDebug(true);
        webview.setDebugSrc(debugSrc);
        webview.loadUrl(startUrl);
    }

    public void start(final int sid, Map<String, Object> arguments, @NonNull final SpiderEventListener spiderEventListener) {

        this.arguments = arguments;
        this.spiderEventListener = spiderEventListener;
        final Context ctx = getContext();
        Helper.init((Activity) ctx, sid, new InitStateListener() {
            @Override
            public void onSucceed(int taskId, String startUrl, String script) {
                webview.setDebug(false);
                webview.setTaskId(taskId + "");
                webview.setInjectScript(script);
                webview.loadUrl(startUrl);
            }

            @Override
            public void onFail(final String msg, final int code) {
                spiderEventListener.onError(code, msg);
            }
        });
    }

}
