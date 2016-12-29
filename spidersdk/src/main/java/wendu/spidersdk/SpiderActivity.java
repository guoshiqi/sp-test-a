package wendu.spidersdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpiderActivity extends AppCompatActivity {

    View container;
    RelativeLayout webviewLayout;
    RelativeLayout spider;
    CircleProgress workProgress;
    TextView percentage;
    TextView initView;
    FragmentManager fm;
    BaseFragment fragment;
    Handler handler;
    TextView titleTv;
    RelativeLayout errorLayout;
    RelativeLayout loading;
    WaveProgress waveProgress;
    TextView msg;
    TextView progressMsg;
    ViewGroup toobar;

    private boolean isProgressShow=false;
    private DSWebview mWebView;
    public  String arguments ="";
    private int max = 100;
    private boolean showProgress=false;
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
        progressMsg=getView(R.id.progress_msg);
        fm = getSupportFragmentManager();
        workProgress.setForegroundColor(Color.argb(70,19,94,148),Color.argb(170,19,94,148));

        String title = getIntent().getStringExtra("title");
        arguments =getIntent().getStringExtra("arguments");
        titleTv.setText(TextUtils.isEmpty(title) ? "爬取" : title);
        boolean isDebug= getIntent().getBooleanExtra("debug", false);
        init();
        if (isDebug) {
            mWebView.loadUrl(getIntent().getStringExtra("startUrl"));
            mWebView.setDebugSrc(getIntent().getStringExtra("debugSrc"));
            mWebView.setDebug(true);
        }else {
            open(getIntent().getIntExtra("sid",-1));
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 7:
                        Intent intent = new Intent();
                        try {
                            String path = getCacheDir() + "/spider.dat";
                            File file = new File(path);
                            file.delete();
                            file.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(file.toString());
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                            objectOutputStream.writeObject(msg.obj);
                            intent.putExtra("result", path);
                            setResult(Activity.RESULT_OK, intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            setResult(Activity.RESULT_CANCELED, intent);

                        }
                        finish();

                }
            }
        };


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
        mWebView= (DSWebview) findViewById(R.id.webview);
        mWebView.setWebEventListener(new DSWebview.WebEventListener() {
            @Override
            void onPageStart(String url) {
                if(!isProgressShow) {
                    showLoadView();
                }
                super.onPageStart(url);
            }

            @Override
            void onReceivedError(String msg) {
                super.onReceivedError(msg);
                errorBack(msg,DSpider.Result.STATE_WEB_ERROR);
            }

            @Override
            void onPageFinished(String url) {
                hideLoadView();
                super.onPageFinished(url);
            }

            @Override
            void onSdkServerError(Exception e) {
                showDialog("服务器罢工了！"+e.getMessage());
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
                max=maxProgress;

            }

            @Override
            public void setProgressMsg(String msg) {
                progressMsg.setText(msg);
                super.setProgressMsg(msg);
            }

            @Override
            public void showProgress(boolean show) {
                isProgressShow=show;
                SpiderActivity.this.showProgress(show);
            }

            @Override
            public void finish(DSpider.Result result) {
                backResult(result);
            }

            @Override
            public String getArguments() {
                if(TextUtils.isEmpty(arguments)) {
                    arguments="{}";
                }
                return  arguments;
            }
        }));

    }




    public void open(final int sid) {
        Helper.init(this, new InitStateListener() {
            @Override
            public void onSucceed(int deviceId) {
                try {
                    String url = DSpider.BASE_URL + "task?sid=" + sid + Helper.getExtraInfo(SpiderActivity.this);
                    JSONObject ret = new JSONObject(Helper.post(url, ""));
                    int code = ret.getInt("code");
                    if (code != 0) {
                       showDialog(ret.getString("msg"));
                       errorBack(ret.getString("msg"),DSpider.Result.STATE_ERROR_MSG);
                    } else {
                        ret = ret.getJSONObject("data");
                        int taskId = ret.getInt("id");
                        String common = "?id=" + taskId + "&package=" + getPackageName() + "&appkey=" + ret.getInt("appkey");
                        mWebView.setInjectUrl(DSpider.BASE_URL + "script" + common + "&platform=1");
                        mWebView.setReportUrl(DSpider.BASE_URL + "report" + common);
                        mWebView.loadUrl(ret.getString("startUrl"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog(e.getMessage());
                    errorBack(e.getLocalizedMessage(),DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
                }
            }

            @Override
            public void onFail(final String msg, final int code) {
               showDialog(msg);
               errorBack(msg,code);
            }
        });
    }

    private void errorBack(String msg,int code){
        DSpider.Result result=new  DSpider.Result("",null,msg,code);
        backResult(result);
    }

    private void backResult(DSpider.Result result){
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

     public void showDialog(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SpiderActivity.this).
                        setTitle("提示").
                        setMessage(msg).
                        setCancelable(false).
                        setPositiveButton("返回", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                SpiderActivity.super.onBackPressed();
                            }
                        }).create().show();
            }
        });

    }


    void showLoadView(String message) {
        if(showProgress) return;
        loading.setVisibility(View.VISIBLE);
        msg.setText(message);
    }

    void showLoadView(){
        showLoadView("正在加载...");
    }

    void hideLoadView() {
        loading.setVisibility(View.GONE);
    }

    public void showInput(boolean show){
      mWebView.setDescendantFocusability(show?ViewGroup.FOCUS_AFTER_DESCENDANTS:ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    public void showProgress(boolean show) {
        if (show){
          hideLoadView();
        }
        showInput(!show);
        webviewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        spider.setVisibility(show ? View.VISIBLE : View.GONE);
        toobar.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (null != handler) {
            handler.removeMessages(1);
            handler.removeMessages(2);
            handler.removeMessages(3);
        }
        clearWebViewCache();
        super.onDestroy();
    }

    private static final String APP_CACAHE_DIRNAME = "/webcache";
    public void clearWebViewCache(){

        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME);
        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");

        //删除webview 缓存目录
        if(webviewCacheDir.exists()){
            deleteFile(webviewCacheDir);
        }
        //删除webview 缓存 缓存目录
        if(appCacheDir.exists()){
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
            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());
        }
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
