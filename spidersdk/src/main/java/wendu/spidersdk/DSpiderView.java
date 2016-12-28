package wendu.spidersdk;

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
                }catch (Exception e) {
                    return "{}";
                }
            }
        }));

    }



    public void start(final int sid, Map<String, Object> arguments, @NonNull final SpiderEventListener spiderEventListener) {
        this.arguments=arguments;
        this.spiderEventListener = spiderEventListener;
        final Context ctx = getContext();
        Helper.init(ctx, new InitStateListener() {
            @Override
            public void onSucceed(int deviceId) {
                try {
                    String url = DSpider.BASE_URL + "task?sid=" + sid + Helper.getExtraInfo(ctx);
                    JSONObject ret = new JSONObject(Helper.post(url, ""));
                    int code = ret.getInt("code");
                    if (code != 0) {
                        spiderEventListener.onError(DSpider.Result.STATE_ERROR_MSG,ret.getString("msg"));

                    } else {
                        ret = ret.getJSONObject("data");
                        int taskId = ret.getInt("id");
                        String common = "?id=" + taskId + "&package=" + ctx.getPackageName() + "&appkey=" + ret.getInt("appkey");
                        webview.setInjectUrl(DSpider.BASE_URL + "script" + common + "&platform=1");
                        webview.setReportUrl(DSpider.BASE_URL + "report" + common);
                        webview.loadUrl(ret.getString("startUrl"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    spiderEventListener.onError(DSpider.Result.STATE_DSPIDER_SERVER_ERROR,e.getMessage());
                }
            }

            @Override
            public void onFail(final String msg, final int code) {

                post(new Runnable() {
                    @Override
                    public void run() {
                        spiderEventListener.onError(code, msg);
                    }
                });

            }
        });
    }

}
