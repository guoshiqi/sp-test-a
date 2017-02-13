package wendu.spider;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wendu.common.base.BaseActivity;
import wendu.common.utils.KvStorage;
import wendu.spidersdk.DSpider;

public class EmailActivity extends BaseActivity {

    ListView listView;
    List<Map<String, Object>> datas = getData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        setActivityTitle("信用卡账单");
        listView = getView(R.id.list_view);
        SimpleAdapter adapter = new SimpleAdapter(this, datas, R.layout.email_item,
                new String[]{"img"},
                new int[]{R.id.icon});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, Object> map = datas.get(position);
                final int sid = (int) map.get("sid");
                final EditText editText = new EditText(EmailActivity.this);
                float dpi = getResources().getDisplayMetrics().density;
                new AlertDialog.Builder(EmailActivity.this)
                        .setTitle("请输入关键字")
                        .setView(editText)
                        .setCancelable(true)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DSpider dSpider = DSpider.build(EmailActivity.this)
                                        .addArgument("wd", editText.getText().toString());

                                if (KvStorage.getInstance().getBoolean("debug", false)) {
                                    dSpider.startDebug(sid, map.get("title").toString(), "", "");
                                } else {
                                    dSpider.start(sid, map.get("title").toString());
                                }

                            }
                        })
                        .show();

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                int t = (int) (dpi * 16);
                layoutParams.setMargins(t, 0, t, 0);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                editText.setLayoutParams(layoutParams);
                int padding = (int) (15 * dpi);
                editText.setPadding(padding - (int) (5 * dpi), padding, padding, padding);

            }
        });
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("img", R.drawable.emqq);
        map.put("title", "QQ邮箱");
        map.put("sid", 5);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.em163);
        map.put("title", "163邮箱");
        map.put("sid", 6);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.em126);
        map.put("title", "126邮箱");
        map.put("sid", 7);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.emsina);
        map.put("title", "新浪邮箱");
        map.put("sid", 8);
        list.add(map);
        return list;
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
                    }
                }
            } else {
                showBottomToast("爬取任务取消");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
