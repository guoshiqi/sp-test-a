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
    FrameLayout webviewLayout;
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
    ViewGroup toobar;

    public static String debugSrc="";
    private  String  BASE_URL="http://172.19.23.62/lara-test/api/";
    private   String injectUrl = BASE_URL+"script";
    public   String reportUrl =BASE_URL+"report";
    public  String arguments ="";
    private int max = 100;
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
        webviewLayout = getView(R.id.fragment);
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
        fm = getSupportFragmentManager();
        workProgress.setForegroundColor(Color.argb(70,19,94,148),Color.argb(170,19,94,148));

        String title = getIntent().getStringExtra("title");
        Helper.isDebug = getIntent().getBooleanExtra("debug", false);
        if (Helper.isDebug) {
            debugSrc=getIntent().getStringExtra("debugSrc");
        }
        arguments =getIntent().getStringExtra("arguments");
        titleTv.setText(TextUtils.isEmpty(title) ? "爬取" : title);
        init(getIntent().getIntExtra("sid",-1),getIntent().getStringExtra("appkey"));

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
                        fragment.showInput(false);
                        showLoadView((String) msg.obj);
                        break;
                    case 5:
                        fragment.showInput(true);
                        hideLoadView();
                        break;
                    case 6:
                        showLoadErrorView();
                        break;
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


    private void getFields(Class cls,String[] need,Map infos){
        Field[] fields=cls.getDeclaredFields();
        List<String> list= Arrays.asList(need);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String attr=field.getName();
                if(list.contains(attr)) {
                    infos.put(field.getName().toLowerCase(), field.get(null).toString());
                }
            } catch (Exception e) {
                Log.e("", "an error occured when collect crash info", e);
            }
        }
    }
    //用来存储设备信息和异常信息
    private String  collectDeviceInfo(Context ctx) {
        Map<String, Object> infos = new HashMap<>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("version_name", versionName);
                infos.put("version_code", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", "an error occured when collect package info", e);
        }

        getFields(Build.VERSION.class,new String[]{"SDK_INT","RELEASE"},infos);
        getFields(Build.class,new String[]{"ID","BRAND","BOARD","CPU_ABI","FINGERPRINT","MODEL","DEVICE"},infos);
        JSONObject jsonObject=new JSONObject(infos);
        return jsonObject.toString();
    }

    void init(final int sid, final String appkey){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String extra= URLEncoder.encode(collectDeviceInfo(getApplicationContext()),"UTF-8");
                    URL uri = new URL(BASE_URL+"task?platform=android&sid="+sid+"&appkey="+appkey+"&extra="+extra);
                    HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
                    urlCon.setRequestMethod("GET");
                    urlCon.setRequestProperty("X-Requested-With","XMLHttpRequest");
                    urlCon.setConnectTimeout(10000);
                    JSONObject ret=new JSONObject(Helper.inputStream2String(urlCon.getInputStream()));
                    int code=ret.getInt("code");
                    if(code!=0){
                        showDialog(ret.getString("msg"));
                    }else {
                        ret= ret.getJSONObject("data");
                        int taskId=ret.getInt("id");
                        String common="?id="+taskId+"&appkey="+appkey;
                        injectUrl=injectUrl+common;
                        reportUrl = reportUrl +common;
                        open(ret.getString("startUrl"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showDialog(e.getMessage());
                }
            }
        }).start();
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
        loading.setVisibility(View.VISIBLE);
        msg.setText(message);
    }

    void hideLoadView() {
        loading.setVisibility(View.GONE);
    }

    void showLoadErrorView() {
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void open(final String url) {
        webviewLayout.post(new Runnable() {
            @Override
            public void run() {
                fragment = SpiderFragment.newInstance(url, injectUrl);
                startFragment();
            }
        });

    }

    public void startFragment() {
        fm.beginTransaction()
                .replace(R.id.fragment, fragment, "")
                .commit();
    }

    public void loadUrl(String url) {
        fragment.loadUrl(url);
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders){
        fragment.loadUrl(url,additionalHttpHeaders);
    }

    public void setUserAgent(String userAgent) {
        fragment.setUserAgent(userAgent);
    }

    public void showProgress(boolean show) {
        fragment.showInput(!show);
        webviewLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        spider.setVisibility(show ? View.VISIBLE : View.GONE);
        toobar.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    public CircleProgress getProgress() {
        return workProgress;
    }


    public Handler getHandler() {
        return handler;
    }

    public void showBottomToast(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
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

    public void  autoLoadImg(final boolean load){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment.autoLoadImg(load);
            }
        });

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
