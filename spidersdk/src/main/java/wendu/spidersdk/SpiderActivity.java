package wendu.spidersdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class SpiderActivity extends AppCompatActivity {

    View container;
    RelativeLayout webviewLayout;
    RelativeLayout spider;
    CircleProgress workProgress;
    TextView percentage;
    TextView initView;
    FragmentManager fm;
    BaseFragment fragment;
    TextView titleTv;
    RelativeLayout errorLayout;
    RelativeLayout loading;
    WaveProgress waveProgress;
    TextView msg;
    TextView progressMsg;
    ViewGroup toobar;

    private boolean isProgressShow = false;
    private DSWebview mWebView;
    public String arguments = "";
    private int max = 100;
    private boolean showProgress = false;
    private static final String TAG = "SpiderActivity";
    public <T extends View> T getView(int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spider);
        container = getView(R.id.container);
        webviewLayout = getView(R.id.webview_container);
        waveProgress=getView(R.id.wave);
        waveProgress.setProgress(90);
        waveProgress.setAmplitudeRatio(.05f);
        waveProgress.setShapeType(WaveProgress.ShapeType.SQUARE);
        waveProgress.setWaveSpeed(1200);
        boolean style=getIntent().getBooleanExtra("style", false);
        if(style) {
            int waveColor = Helper.getColor(this, R.color.colorPrimaryDark);
            if (waveColor != -1) {
                int waveColor2 = Color.argb(130, Color.red(waveColor),
                        Color.green(waveColor), Color.blue(waveColor));
                waveProgress.setWaveColor(waveColor2, waveColor);
            }
        }
        toobar=getView(R.id.toolbar);
        spider = getView(R.id.spider);
        workProgress = getView(R.id.work_progress);
        percentage = getView(R.id.percentage);
        initView = getView(R.id.init);
        titleTv = getView(R.id.title);
        errorLayout = getView(R.id.error_layout);
        loading = getView(R.id.loading);
        msg = getView(R.id.msg);
        progressMsg = getView(R.id.progress_msg);
        fm = getSupportFragmentManager();
        workProgress.setForegroundColor(Color.argb(70,19,94,148),Color.argb(170,19,94,148));
        String title = getIntent().getStringExtra("title");
        arguments = getIntent().getStringExtra("arguments");
        titleTv.setText(TextUtils.isEmpty(title) ? "爬取" : title);
        boolean isDebug = getIntent().getBooleanExtra("debug", false);
        String startUrl = getIntent().getStringExtra("startUrl");
        init();
        if (isDebug) {
            mWebView.setDebugSrc(getIntent().getStringExtra("debugSrc"));
            mWebView.setDebug(true);
            mWebView.loadUrl(startUrl);
        } else {
            String taskId = getIntent().getStringExtra("taskId");
            String script = getSharedPreferences("spider", Context.MODE_PRIVATE).getString(taskId, "");
            getSharedPreferences("spider", Context.MODE_PRIVATE).edit().remove(taskId);
            mWebView.setInjectScript(script);
            mWebView.setTaskId(taskId);
            mWebView.loadUrl(startUrl);
        }

        ImageView back=getView(R.id.back) ;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onBackPressed();
            }
        });
        getView(R.id.back_gray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onBackPressed();

            }
        });
        errorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // showLoadView("正在加载...");
                errorLayout.setVisibility(View.GONE);
                fragment.errorReload();
            }
        });

    }

    public void init() {
        mWebView = (DSWebview) findViewById(R.id.webview);
        mWebView.setWebEventListener(new DSWebview.WebEventListener() {
            @Override
            void onPageStart(String url) {
                if (!isProgressShow) {
                    showLoadView();
                }
                super.onPageStart(url);
            }

            @Override
            void onReceivedError(String msg) {
                super.onReceivedError(msg);
                //errorBack(msg, DSpider.Result.STATE_WEB_ERROR);
            }

            @Override
            void onPageFinished(String url) {
                hideLoadView();
                super.onPageFinished(url);
            }

            @Override
            void onSdkServerError(Exception e) {
                errorBack(e.getMessage(), DSpider.Result.STATE_ERROR_MSG);
                super.onSdkServerError(e);
            }
        });

        mWebView.addJavascriptInterface(new JavaScriptBridge(mWebView, new JavaScriptHandler() {
            @Override
            public void setProgress(int progress) {
                workProgress.setProgress(progress);
                percentage.setText((int) (progress / (float) max * 100) + "%");
            }

            @Override
            public void setProgressMax(int maxProgress) {
                workProgress.setMax(maxProgress);
                max = maxProgress;

            }

            @Override
            public void setProgressMsg(String msg) {
                progressMsg.setText(msg);
                super.setProgressMsg(msg);
            }

            @Override
            public void showProgress(boolean show) {
                isProgressShow = show;
                SpiderActivity.this.showProgress(show);
            }

            @Override
            public void finish(DSpider.Result result) {
                backResult(result);
            }

            @Override
            public String getArguments() {
                if (TextUtils.isEmpty(arguments)) {
                    arguments = "{}";
                }
                return arguments;
            }
        }));

    }


    private void errorBack(String msg, int code) {
        DSpider.Result result = new DSpider.Result("", null, msg, code);
        backResult(result);
    }

    private void backResult(DSpider.Result result) {
        Intent intent = new Intent();
        try {
            String path = getCacheDir() + "/spider.dat";
            File file = new File(path);
            file.delete();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file.toString());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(result);
            intent.putExtra("result", path);
            setResult(Activity.RESULT_OK, intent);
        } catch (Exception e) {
            e.printStackTrace();
            setResult(Activity.RESULT_CANCELED, intent);
        }

        SpiderActivity.this.finish();
    }

    void showLoadView(String message) {
        if (showProgress) return;
        loading.setVisibility(View.VISIBLE);
        msg.setText(message);
    }

    void showLoadView() {
        showLoadView("正在加载...");
    }

    void hideLoadView() {
        loading.setVisibility(View.GONE);
    }

    public void showInput(boolean show) {
        mWebView.setDescendantFocusability(show ? ViewGroup.FOCUS_AFTER_DESCENDANTS : ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public void showProgress(boolean show) {
        if (show) {
            hideLoadView();
        }
        showInput(!show);
        webviewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        spider.setVisibility(show ? View.VISIBLE : View.GONE);
        toobar.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        mWebView.clearCache();
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {

        Dialog alertDialog = new AlertDialog.Builder(SpiderActivity.this).
                    setTitle("提示").
                setMessage("确定要退出吗？").
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SpiderActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
    }

}
