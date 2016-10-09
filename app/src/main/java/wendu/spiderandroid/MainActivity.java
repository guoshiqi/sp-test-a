package wendu.spiderandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import wendu.common.base.BaseActivity;
import wendu.common.utils.KvStorage;
import wendu.spidersdk.ResultData;
import wendu.spidersdk.SpiderActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView result;
    String billId = "";
    private SpiderServiceTest spiderService = DataController.getUploadSerivce();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   x.Ext.init(getApplication());
        setContentView(R.layout.activity_main);
        findViewById(R.id.email).setOnClickListener(this);
        result = getView(R.id.result);
        result.setOnClickListener(this);
        setActivityTitle("Spider Demon");
        result = getView(R.id.result);
        hideBackImg();
    }

    void openActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SpiderActivity.class);
        intent.putExtra("url", "http://172.19.22.235/h5Test/dist/emails/email.html?t=" + System.currentTimeMillis());
        intent.putExtra("inject", "http://172.19.22.235/h5Test/dist/");
        intent.putExtra("title", "邮箱爬取");
        intent.putExtra("debug",true);
        startActivityForResult(intent, 1);
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
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ResultData resultData = (ResultData) data.getSerializableExtra("result");
            LatestResult.getInstance().getData().clear();
            LatestResult.getInstance().getData().addAll(resultData.datas);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.email:
                openActivity();
                break;
            case R.id.result:
                startActivity(ResultActivity.class);
                break;
        }
    }

    void upload(final List<String> list, final String errMsg) {
        final int size = list.size();
        final String bank = "CMB";
        final String email = KvStorage.getInstance().getString("u", "");
        if (size == 0) {
            if (TextUtils.isEmpty(errMsg)) {
                showDialog("提示", "没有符合条件的邮件", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            } else {
                errorReport(email, bank, errMsg);
            }
            return;
        }

        final Gson gson = new Gson();
        spiderService.sycTaskStatus(0, null, email, null, size, bank)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<SpiderResponse, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(SpiderResponse spiderResponse) {
                        billId = spiderResponse.bill_id;
                        List<List<String>> ll = new ArrayList<List<String>>();
                        //分组上传，10条一组
                        int x = size % 10;
                        if (x != 0) {
                            ll.add(list.subList(0, x));
                        }
                        for (int i = x; i < size; ) {
                            ll.add(list.subList(i, i + 10));
                            i += 10;
                        }
                        return Observable.from(ll);
                    }
                })
                .flatMap(new Func1<List<String>, Observable<SpiderResponse>>() {
                    @Override
                    public Observable<SpiderResponse> call(List<String> list) {
                        return spiderService.upload(billId, bank, email, gson.toJson(list));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SpiderResponse>() {
                    @Override
                    public void onCompleted() {
                        int status = 2;//成功
                        if (!TextUtils.isEmpty(errMsg)) {
                            status = 3;//失败
                        }
                        spiderService.sycTaskStatus(status, billId, email, errMsg, size, bank)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Subscriber<SpiderResponse>() {
                                    @Override
                                    public void onCompleted() {
                                        showDialog("提示", "爬取结束", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                onBackPressed();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        showDialog("爬取失败", e.getMessage(), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                onBackPressed();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onNext(SpiderResponse spiderResponse) {

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDialog("失败", e.getMessage(), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(SpiderResponse spiderResponse) {

                    }
                });
    }

    private void errorReport(final String email, final String bank, final String errmsg) {
        spiderService.sycTaskStatus(0, null, email, null, 0, bank)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<SpiderResponse, Observable<SpiderResponse>>() {
                    @Override
                    public Observable<SpiderResponse> call(SpiderResponse spiderResponse) {
                        return spiderService.sycTaskStatus(3, spiderResponse.bill_id, email, errmsg, 0, bank);

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SpiderResponse>() {
                    @Override
                    public void onCompleted() {
                        showDialog("提示", "爬取失败［脚本错误］－错误上报成功", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDialog("提示", "爬取失败[脚本错误]－上报失败（网络错误）" + e.getMessage(), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    }

                    @Override
                    public void onNext(SpiderResponse spiderResponse) {

                    }
                });

    }


}
