package wendu.spider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wendu.common.base.BaseActivity;
import wendu.common.utils.DpiHelper;
import wendu.common.utils.KvStorage;
import wendu.spidersdk.DSpider;
import wendu.spidersdk.third.ZmxyActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView result;
    TextView log;
    String billId = "";
    SwitchCompat debugSwitch;
    //private SpiderService spiderService = DataController.getUploadSerivce();
    private SpiderServiceTest spiderService=DataController.getUploadSerivceTest();
    private String scriptUrl="http://119.29.112.230:4832/?sid=";
    LinearLayout root;
    //private String scriptUrl="http://172.19.22.235/spider-script/?sid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setActivityTitle("数据爬虫");
        setRightImg(R.drawable.setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SettingActivity.class);
            }
        });
        root = getView(R.id.root);
        hideBackImg();
        initFromLocal();

//        DSpiderView dSpiderView= getView(R.id.dspier_view);
//        dSpiderView.start(1, null, new SpiderEventListener() {
//            @Override
//            public void onResult(String sessionKey, List<String> data) {
//                super.onResult(sessionKey, data);
//            }
//
//            @Override
//            public void onProgress(int progress, int max) {
//                super.onProgress(progress, max);
//            }
//
//            @Override
//            public void onError(int code, String msg) {
//                super.onError(code, msg);
//            }
//        });
    }

    void startDspider(int sid, String title, String debugSrcFileName, String debugStartUrl) {
        if (sid == 0) {
            showDialog("暂未上线，敬请期待！");
            return;
        }
        DSpider.build(this)
                //.addArgument("test",7)
                .setDebug(KvStorage.getInstance().getBoolean("debug", false))
                .start(sid,title, debugSrcFileName, debugStartUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DSpider.REQUEST) {
            //获取爬取数据
            if (resultCode == RESULT_OK) {
                DSpider.Result resultData = DSpider.getLastResult(this);
                if (resultData != null) {
                    if (resultData.code != resultData.STATE_SUCCEED) {
                        showDialog("失败了，" + resultData.errorMsg);
                    } else {
                        LatestResult.getInstance().getData().clear();
                        LatestResult.getInstance().getData().addAll(resultData.datas);
                        startActivity(ResultActivity.class);
                        //upload(resultData.datas, resultData.errorMsg);
                    }
                }
            } else {
                showBottomToast("爬取任务取消");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }


    public void initFromLocal() {
        List<SpiderCategory> items = new ArrayList<>();
        SpiderCategory category = new SpiderCategory();
        category.name = "消费信息认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(2, R.drawable.taobao, "淘宝", "https://login.m.taobao.com/login.htm", "taobao.js"));
            add(new SpiderItem(3, R.drawable.alipay, "支付宝", "https://custweb.alipay.com/account/index.htm", "alipay.js"));
            add(new SpiderItem(1, R.drawable.jd, "京东", "https://plogin.m.jd.com/user/login.action?appid=100", "jd.js"));
        }};
        items.add(category);

        category = new SpiderCategory();
        category.name = "资产资质认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(Util.EMAIL, R.drawable.email, "邮箱"));
            add(new SpiderItem(6, R.drawable.gjj, "公积金", "http://www.bjgjj.gov.cn/wsyw/wscx/gjjcx-login.jsp", "gongjijin.js"));
        }};
        items.add(category);

        category = new SpiderCategory();
        category.name = "身份信息认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(4, R.drawable.unicom, "联通", "http://wap.10010.com/t/query/getPhoneByDetailTip.htm", "unicom.js"));
            add(new SpiderItem(5, R.drawable.mobile, "移动", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=billdetailqry", "mobile.js"));
            add(new SpiderItem(6, R.mipmap.ic_launcher, "广东电信", "https://gd.189.cn/TS/login.htm", "telecom_gd.js"));
            add(new SpiderItem(0, R.drawable.shebao, "社保"));
        }};
        items.add(category);

        category = new SpiderCategory();
        category.name = "征信信息认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(Util.ZHIMA, R.drawable.zmf, "芝麻分"));
            add(new SpiderItem(0, R.drawable.zx, "简版征信"));
            add(new SpiderItem(1, R.mipmap.ic_launcher, "测试", "https://www.baidu.com", "test.js"));

        }};
        items.add(category);
        parseCategories(items);

    }

    public void parseCategories(List<SpiderCategory> items) {
        for (final SpiderCategory category : items) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.categroy_item, null);
            TextView title = (TextView) view.findViewById(R.id.title);
            GridView gridView = (GridView) view.findViewById(R.id.grid_view);
            title.setText(category.name);
            GridViewAdapter adapter = new GridViewAdapter(category.spiders);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SpiderItem item = category.spiders.get(position);
                    switch (item.sid) {
                        case Util.ZHIMA:
                            startActivity(ZmxyActivity.class);
                            break;
                        case Util.EMAIL:
                            startActivity(EmailActivity.class);
                            break;
                        default:
                            startDspider(item.sid, item.name, item.debugSrc, item.startUrl);
                    }
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = DpiHelper.dip2px(8);
            root.addView(view, layoutParams);
        }
    }


    public class GridViewAdapter extends BaseAdapter {

        private List<SpiderItem> items;

        public GridViewAdapter(List<SpiderItem> list) {
            items = list;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.spider_item, null);
            }
            SpiderItem item = items.get(position);
            TextView view = (TextView) convertView;
            view.setCompoundDrawablesWithIntrinsicBounds(0, item.icon, 0, 0);
            view.setText(item.name);
            return convertView;
        }
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

    }

    void upload(final List<String> list, final String errMsg) {
        final int size = list.size();
        if (size == 0) {
            if (TextUtils.isEmpty(errMsg)) {
                showDialog("提示", "没有");
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



}
