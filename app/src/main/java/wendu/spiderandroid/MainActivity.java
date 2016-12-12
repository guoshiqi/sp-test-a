package wendu.spiderandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wendu.common.base.BaseActivity;
import wendu.common.utils.KvStorage;
import wendu.spidersdk.DSpider;
import wendu.spidersdk.SpiderActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView result;
    TextView log;
    String billId = "";
    SwitchCompat debugSwitch;
    //private SpiderService spiderService = DataController.getUploadSerivce();
    private SpiderServiceTest spiderService=DataController.getUploadSerivceTest();
    boolean isDebug=false;
    private String scriptUrl="http://119.29.112.230:4832/?sid=";
    //private String scriptUrl="http://172.19.22.235/spider-script/?sid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.email).setOnClickListener(this);
        findViewById(R.id.am_tv_taobao).setOnClickListener(this);
        findViewById(R.id.jd).setOnClickListener(this);
        findViewById(R.id.mobile_unicom).setOnClickListener(this);
        debugSwitch=getView(R.id.debug);
        isDebug=KvStorage.getInstance().getBoolean("debug",false);
        debugSwitch.setChecked(isDebug);
        result = getView(R.id.result);
        result.setOnClickListener(this);
        log=getView(R.id.log);
        log.setOnClickListener(this);
        setActivityTitle("Spider Demon");
        result = getView(R.id.result);
        hideBackImg();
        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              isDebug=isChecked;
              KvStorage.getInstance().edit().putBoolean("debug", isDebug).commit();
            }
        });
    }

    void openJd() {
//        String startUrl="https://plogin.m.jd.com/user/login.action?appid=100";
//        startDspider(startUrl,scriptUrl+"jd","京东信息爬取", "jd.js");
    }

    void openTaoBaoActivity() {
        //String baseUrl="https://www.taobao.com/index.php?from=wap";
//        String baseUrl="https://login.m.taobao.com/login.htm";
//        startDspider(baseUrl,scriptUrl+"taobao","淘宝爬取", "taobao.js");
    }

    void openUnicomCall() {
//        String baseUrl="http://wap.10010.com/t/query/getPhoneByDetailTip.htm";
//        startDspider(baseUrl, scriptUrl+"unicom","联通通话详单爬去", "unicom.js");
    }

    void openEmail() {
        startDspider(1,"测试","","");
    }

    void startDspider(int sid,String title,String debugSrcFileName,String debugStartUrl) {
        DSpider.build(this,"1")
                //.addArgument("test",7)
                .setDebug(isDebug)
                .start(sid,title,debugSrcFileName,debugStartUrl);
    }

    @Override
    protected void onResume() {
        List<String> list = LatestResult.getInstance().getData();
        if (list.size() != 0) {
            result.setVisibility(View.VISIBLE);
            result.setText("查看上次爬取结果 " + list.size() + "条记录");
        } else {
            result.setVisibility(View.GONE);
        }
        if (DSpider.getLastLog(this).isEmpty()){
          log.setVisibility(View.GONE);
        }else {
          log.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DSpider.REQUEST ) {
            //获取爬取数据
            if(resultCode == RESULT_OK) {
                DSpider.Result resultData = DSpider.getLastResult(this);
                if (resultData != null) {
                    LatestResult.getInstance().getData().clear();
                    LatestResult.getInstance().getData().addAll(resultData.datas);
                    upload(resultData.datas, resultData.errorMsg);
                }
            } else {
                showToast("爬取任务取消");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email:
                openEmail();
                break;
            case R.id.am_tv_taobao:
                openTaoBaoActivity();
                break;
            case R.id.jd:
                openJd();
                break;
            case R.id.mobile_unicom:
                openUnicomCall();
                break;
            case R.id.result:
                startActivity(ResultActivity.class);
                break;
            case R.id.log:
                Intent intent=new Intent();
                intent.putExtra("log",true);
                intent.setClass(this,DataReadActivity.class);
                startActivity(intent);
                break;
        }
    }

    void upload(final List<String> list, final String errMsg) {
        final int size = list.size();
        if (size == 0) {
            if (TextUtils.isEmpty(errMsg)) {
                showDialog("提示", "没有符合条件的邮件");
            } else {
                ReportError(errMsg);
            }
            return;
        }
        Gson gson=new Gson();
        Report(gson.toJson(list));

        //// TODO: 16/10/20 测试环境不上传数据
//        if(true) return;
//
//        final Gson gson = new Gson();
//        showLoadDialog("正在上传数据...");
//        spiderService.sycTaskStatus(0, null, email, null, size, bank)
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<String, Observable<List<String>>>() {
//                    @Override
//                    public Observable<List<String>> call(String String) {
//                        billId = String.bill_id;
//                        List<List<String>> ll = new ArrayList<List<String>>();
//                        //分组上传，10条一组
//                        int x = size % 10;
//                        if (x != 0) {
//                            ll.add(list.subList(0, x));
//                        }
//                        for (int i = x; i < size; ) {
//                            ll.add(list.subList(i, i + 10));
//                            i += 10;
//                        }
//                        return Observable.from(ll);
//                    }
//                })
//                .flatMap(new Func1<List<String>, Observable<String>>() {
//                    @Override
//                    public Observable<String> call(List<String> list) {
//                        return spiderService.upload(billId, bank, email, gson.toJson(list));
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<String>() {
//                    @Override
//                    public void onCompleted() {
//                        int status = 2;//成功
//                        if (!TextUtils.isEmpty(errMsg)) {
//                            status = 3;//失败
//                        }
//                        spiderService.sycTaskStatus(status, billId, email, errMsg, size, bank)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribeOn(Schedulers.io())
//                                .subscribe(new Subscriber<String>() {
//                                    @Override
//                                    public void onCompleted() {
//                                        hideLoadDialog();
//                                        showDialog("提示", "爬取结束");
//                                    }
//
//                                    @Override
//                                    public void onError(Throwable e) {
//                                        hideLoadDialog();
//                                        showDialog("爬取失败", e.getMessage());
//                                    }
//
//                                    @Override
//                                    public void onNext(String String) {
//
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        hideLoadDialog();
//                        showDialog("失败", e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(String String) {
//
//                    }
//                });
    }

    private void ReportError(final String msg) {
        showLoadDialog("正在上报错误信息...");
        spiderService.upload(msg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                hideLoadDialog();
                showDialog("错误信息上报成功");
            }

            @Override
            public void onError(Throwable e) {
                hideLoadDialog();
                showDialog("提示", "错误上报失败" + e.getMessage());
            }

            @Override
            public void onNext(String String) {

            }
        });
    }

    private void Report(final String msg) {
        showLoadDialog("正在上报数据...");
        spiderService.upload(msg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                hideLoadDialog();
                showDialog("提示", "爬取成功");
            }

            @Override
            public void onError(Throwable e) {
                hideLoadDialog();
                showDialog("上传数据失败" + e.getMessage());
            }

            @Override
            public void onNext(String String) {

            }
        });

//        showLoadDialog("正在上报错误信息...");
//        spiderService.sycTaskStatus(0, null, email, null, 0, bank)
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<String, Observable<String>>() {
//                    @Override
//                    public Observable<String> call(String String) {
//                        return spiderService.sycTaskStatus(3, String.bill_id, email, errmsg, 0, bank);
//
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<String>() {
//                    @Override
//                    public void onCompleted() {
//                        hideLoadDialog();
//                        showDialog("提示", "爬取失败［脚本错误］－错误上报成功");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        hideLoadDialog();
//                        showDialog("提示", "爬取失败[脚本错误]－上报失败（网络错误）" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(String String) {
//
//                    }
//                });

    }


}
