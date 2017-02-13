package wendu.spider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import wendu.common.base.BaseActivity;
import wendu.common.utils.DpiHelper;
import wendu.common.utils.KvStorage;
import wendu.spidersdk.DSpider;
import wendu.spidersdk.third.ZmxyActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    LinearLayout root;
    SpiderCategory spiderCategory;

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




    public void initFromLocal() {
        List<SpiderCategory> items = new ArrayList<>();
        SpiderCategory category = new SpiderCategory();
        category.name = "消费信息认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(2, R.drawable.taobao, "淘宝", "https://login.m.taobao.com/login.htm", "taobao.js"));
            add(new SpiderItem(4, R.drawable.alipay, "支付宝", "https://custweb.alipay.com/account/index.htm", "alipay.js"));
            add(new SpiderItem(3, R.drawable.jd, "京东", "https://plogin.m.jd.com/user/login.action?appid=100", "jd.js"));
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
            add(new SpiderItem(9, R.drawable.unicom, "联通", "http://wap.10010.com/t/query/getPhoneByDetailTip.htm", "unicom.js"));
            add(new SpiderItem(10, R.drawable.mobile, "移动", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=billdetailqry", "mobile.js"));
            add(new SpiderItem(11, R.drawable.telecom, "广东电信", "https://gd.189.cn/TS/login.htm", "telecom_gd.js"));
            add(new SpiderItem(0, R.drawable.shebao, "社保"));
        }};
        items.add(category);

        category = new SpiderCategory();
        category.name = "征信信息认证";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(0, R.drawable.zx, "简版征信"));

        }};
        items.add(category);

        category = new SpiderCategory();
        category.name = "小说";
        category.spiders = new ArrayList<SpiderItem>() {{
            add(new SpiderItem(12, R.mipmap.ic_launcher, "顶点小说", "http://m.23us.com/", "dingdian.js"));
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
                    spiderCategory = category;
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

    void startDspider(int sid, String title, String debugSrcFileName, String debugStartUrl) {
        if (sid == 0) {
            showDialog("暂未上线，敬请期待！");
            return;
        }
        DSpider dSpider = DSpider.build(this);
                //.addArgument("test",7)
        if (KvStorage.getInstance().getBoolean("debug", false)) {
            dSpider.startDebug(sid, title, debugSrcFileName, debugStartUrl);
        } else {
            dSpider.start(sid, title);
        }
    }

    public String saveToSDCard(String filename, String filecontent) throws Exception {
        String dir = Environment.getExternalStorageDirectory() + "/dspider";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(dir, filename);
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(filecontent.getBytes());
        outStream.close();
        return file.getPath();
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
                        if (spiderCategory.name.equals("小说")) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String str : resultData.datas) {
                                stringBuilder.append(str);
                                stringBuilder.append("\r\n");
                            }
                            try {
                                String path = saveToSDCard(resultData.sessionKey + ".txt", stringBuilder.toString());
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                File file = new File(path);
                                intent.setDataAndType(Uri.fromFile(file), "text/*");
                                startActivity(intent);
                            } catch (Exception e) {
                                showDialog("存储卡保存失败，请检查是否已授权。\r\n" + e.getMessage());
                                e.printStackTrace();
                            }

                        } else {
                            LatestResult.getInstance().getData().clear();
                            LatestResult.getInstance().getData().addAll(resultData.datas);
                            startActivity(ResultActivity.class);
                        }
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

}
