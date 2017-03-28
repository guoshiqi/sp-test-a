package wendu.spidersdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class SpiderActivity extends AppCompatActivity {

    View container;
    RelativeLayout webviewLayout;
    RelativeLayout spider;
    CircleProgress workProgress;
    TextView percentage;
    FragmentManager fm;
    BaseFragment fragment;
    TextView titleTv;
    RelativeLayout errorLayout;
    RelativeLayout loading;
    WaveProgress waveProgress;
    TextView msg;
    TextView progressMsg;
    String tipMsg;
    int showType;


    private boolean isProgressShow = false;
    private DSpiderView spiderView;
    public String arguments = "";
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
        spiderView=getView(R.id.dspider_view);
        spider = getView(R.id.spider);
        workProgress = getView(R.id.work_progress);
        percentage = getView(R.id.percentage);
        titleTv = getView(R.id.title);
        errorLayout = getView(R.id.error_layout);
        loading = getView(R.id.loading);
        msg = getView(R.id.msg);
        progressMsg = getView(R.id.progress_msg);
        fm = getSupportFragmentManager();
        workProgress.setForegroundColor(Color.parseColor("#2189bf"),Color.parseColor("#64C0F0"));
        String title = getIntent().getStringExtra("title");
        arguments = getIntent().getStringExtra("arguments");
        titleTv.setText(TextUtils.isEmpty(title) ? "爬取" : title);
        boolean isDebug = getIntent().getBooleanExtra("debug", false);
        showType=getIntent().getIntExtra("showType",DSpider.TYPE_TOAST);
        tipMsg=getIntent().getStringExtra("retryTip");
        if (TextUtils.isEmpty(arguments)) {
            arguments = "{}";
        }
        spiderView.setArguments(arguments);
        if (isDebug) {
            spiderView.startDebug(getIntent().getStringExtra("debugStartUrl"),
                    getIntent().getStringExtra("debugSrc"), spiderEventListener);
        } else {
            int  sid = getIntent().getIntExtra("sid",-1);
            spiderView.start(sid, spiderEventListener);
        }
        ImageView back=getView(R.id.back) ;
        back.setOnClickListener(new View.OnClickListener() {
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

    SpiderEventListener spiderEventListener=new SpiderEventListener() {
        @Override
        public void onResult(String sessionKey, List<String> data) {
            backResult(new DSpider.Result(sessionKey,data,"",0));
        }

        @Override
        public void onProgress(int progress, int max) {
            workProgress.setMax(max);
            workProgress.setProgress(progress);
            percentage.setText((int) (progress / (float) max * 100) + "%");
        }

        @Override
        public void onProgressShow(boolean isShow) {
            isProgressShow = isShow;
            showProgress(isShow);
        }


        @Override
        public void onProgressMsg(String msg) {
            progressMsg.setText(msg);
        }

        @Override
        public void onScriptLoaded(int scriptIndex) {
            if(scriptIndex>1 && showType==DSpider.TYPE_TOAST) {
                String msg = tipMsg;
                if (TextUtils.isEmpty(msg)) {
                    msg = String.format("出错了，检测到新方案，正在进行第%d次重试", scriptIndex - 1);
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onError(final int code, final String msg) {
            String tipMsg=getIntent().getStringExtra("retryTip");
            if(TextUtils.isEmpty(tipMsg)){
               tipMsg= "遇到点问题，正在尝试新的方案" ;
            }
           if(spiderView.canRetry()){
               if(Helper.retryListener!=null){
                   if(Helper.retryListener.onRetry(code,msg)){
                       retry();
                   }else {
                       backResult(new DSpider.Result(code,msg));
                   }
               }else {
                   if(showType==DSpider.TYPE_DIALOG) {
                       Dialog alertDialog = new AlertDialog.Builder(SpiderActivity.this).
                               setTitle("提示").
                               setMessage(tipMsg).
                               setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       retry();
                                   }
                               })
                               .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       backResult(new DSpider.Result(code, msg));
                                   }
                               })
                               .create();
                       alertDialog.show();
                       return;
                   }
                   retry();
               }
           } else{
               backResult(new DSpider.Result(code, msg));
           }
        }
    };

    private void backResult(DSpider.Result result) {
        Helper.retryListener=null;
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

    private void retry(){
        spiderView.retry();
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
        spiderView.getWebview().enableFocus(show);
    }

    public void showProgress(boolean show) {
        if (show) {
            hideLoadView();
        }
        showInput(!show);
        webviewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        spider.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        spiderView.clearCache();
        spiderView.stop();
        spiderView.getWebview().destroy();
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
