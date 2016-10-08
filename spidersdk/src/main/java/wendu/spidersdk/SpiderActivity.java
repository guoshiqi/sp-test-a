package wendu.spidersdk;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;

public class SpiderActivity extends AppCompatActivity {

    FrameLayout container;
    FrameLayout webviewLayout;
    RelativeLayout spider;
    CircleProgress workProgress;
    TextView percentage;
    TextView initView;
    FragmentManager fm;
    BaseFragment fragment;
    Handler handler;
    TextView titleTv;
    CrossWalkInitializer crossWalkInitializer;
    RelativeLayout errorLayout;
    RelativeLayout loading;
    TextView msg;

    public String getCurrentCore() {
        return currentCore;
    }

    String currentCore="";
    public  static String INJECT_URL="";
    private int max = 100;
    private static final String TAG = "SpiderActivity";
    private String url;
    boolean isInit = false;

    public <T extends View> T getView(int viewId) {
        View  view = findViewById(viewId);
        return (T) view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spider);
        QbSdk.preInit(this);
        container=getView(R.id.container);
        webviewLayout=getView(R.id.fragment);
        spider=getView(R.id.spider);
        workProgress=getView(R.id.work_progress);
        percentage=getView(R.id.percentage);
        initView=getView(R.id.init);
        titleTv=getView(R.id.title);
        errorLayout=getView(R.id.error_layout);
        loading=getView(R.id.loading);
        msg=getView(R.id.msg);
        fm = getSupportFragmentManager();
        crossWalkInitializer = CrossWalkInitializer.create(SpiderActivity.this);
        crossWalkInitializer.init(false);
        url = getIntent().getStringExtra("url");
        INJECT_URL=getIntent().getStringExtra("inject");
        String title=getIntent().getStringExtra("title");
        titleTv.setText(TextUtils.isEmpty(title) ? "爬取" : title);
        //open(url, "x5|sys|cs");
        open(url, "sys|x5|cs");
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        workProgress.setProgress(msg.arg1);
                        percentage.setText((int) (msg.arg1 / (float) max * 100) + "%");
                        break;
                    case 2:
                        workProgress.setMax(msg.arg1);
                        max = msg.arg1;
                        break;
                    case 3:
                        showProgress((boolean) msg.obj);
                        break;
                    case 4:
                        showLoadView((String) msg.obj);
                        break;
                    case 5:
                        hideLoadView();
                        break;
                    case 6:
                        showLoadErrorView();
                        break;

                }
            }
        };

        getView(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpiderActivity.super.onBackPressed();
            }
        });

        errorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadView("正在加载...");
                errorLayout.setVisibility(View.GONE);
                fragment.errorReload();
            }
        });


    }

    void showLoadView(String message){
        loading.setVisibility(View.VISIBLE);
        msg.setText(message);
    }
    void hideLoadView(){
        loading.setVisibility(View.GONE);
    }
    void showLoadErrorView(){
        errorLayout.setVisibility(View.VISIBLE);
    }



    public void open(final String url, final String webCore) {
        this.url = url;
        webviewLayout.post(new Runnable() {
            @Override
            public void run() {
                String[] cores = webCore.split("\\|");
                for (String core : cores) {
                    currentCore=core;
                    switch (core) {
                        case "sys":
                            openInDefault();

                            return;
                        case "cs":
                            openInCrossWalk();
                            return;
                        case "x5": {
                            if (openInX5()) return;
                            continue;
                        }
                    }
                }
            }
        });

    }

    private void init(final String url) {
        webviewLayout.post(new Runnable() {
            @Override
            public void run() {
                if (isX5()) {
                    fragment = SpiderX5Fragment.newInstance(url, true);
                    startFragment();
                } else {
                    openInCrossWalk();
                }
            }
        });
    }

    public boolean openInX5() {
        if (isX5()) {
            fragment = SpiderX5Fragment.newInstance(url, true);
            startFragment();
            return true;
        }
        return false;
    }

    public boolean openInDefault() {
        fragment = SpiderFragment.newInstance(url, true);
        //fragment =SpiderIosLikeFragment.newInstance(url, true);
        startFragment();
        return true;
    }

    public void openInCrossWalk() {
        if (crossWalkInitializer.isAvaiable()) {
            fragment = SpiderCrossWalkFragment.newInstance(url, true);
            startFragment();
        } else {
            Dialog alertDialog=new AlertDialog.Builder(this).
                    setTitle("提示").
                    setMessage("首次加载需要下载数据，确定？").
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadCrossWalkAndOpen();
                        }
                    }).
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.show();
        }
    }

    private void downloadCrossWalkAndOpen() {
        initView.setVisibility(View.VISIBLE);
        isInit = true;
        crossWalkInitializer.setInitListener(new CrossWalkInitializer.InitListener() {
            @Override
            void onSuccess() {
                fragment = SpiderCrossWalkFragment.newInstance(url, true);
                startFragment();
                initView.setVisibility(View.GONE);
                isInit = false;
            }

            @Override
            void onFailed() {
                Dialog alertDialog=new AlertDialog.Builder(SpiderActivity.this).
                        setTitle("提示").
                        setMessage("加载失败").
                        setPositiveButton("返回",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        }).create();
                alertDialog.show();
            }

            @Override
            void onDownloadStart() {
                initView.setText("首次加载初始化中：");
            }

            @Override
            void onProgress(int progress) {
                if (progress > 95) {
                    initView.setText("正在验证...");
                } else {
                    initView.setText("首次加载初始化中：" + progress + "%");
                }
            }
        });
        crossWalkInitializer.init(true);
    }


    public void startFragment() {
        fm.beginTransaction()
                .replace(R.id.fragment, fragment, "")
                .commit();
    }

    boolean isX5() {
        boolean isX5 = false;
        if (QbSdk.getTbsVersion(this) != 0) {
            WebView webView = new WebView(this);
            isX5 = (webView.getX5WebViewExtension() != null);
        }
        return isX5;
    }


    public void showProgress(boolean show) {
        webviewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        spider.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public CircleProgress getProgress() {
        return workProgress;
    }


    public Handler getHandler() {
        return handler;
    }

    public void showBottomToast(String msg){
        Toast toast= Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM,0,0);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        if (null != handler) {
            handler.removeMessages(1);
            handler.removeMessages(2);
            handler.removeMessages(3);
        }
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        if (isInit) {
            Dialog alertDialog=new AlertDialog.Builder(SpiderActivity.this).
                    setTitle("提示").
                    setMessage("正在初始化，确定要退出吗？").
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            crossWalkInitializer.cancel();
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
        } else {
            if(!fragment.goBack()) {
                super.onBackPressed();
            }
        }

    }

}
