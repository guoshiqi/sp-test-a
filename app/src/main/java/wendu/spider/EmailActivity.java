package wendu.spider;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
                Map<String, Object> map = datas.get(position);
                int sid = (int) map.get("sid");
                DSpider.build(EmailActivity.this)
                        //.addArgument("test",7)
                        .setDebug(KvStorage.getInstance().getBoolean("debug", false))
                        .start(sid,map.get("title").toString());
                finish();
            }
        });
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("img", R.drawable.emqq);
        map.put("title", "QQ邮箱");
        map.put("sid", 6);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.em163);
        map.put("title", "163邮箱");
        map.put("sid", 9);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.em126);
        map.put("title", "126邮箱");
        map.put("sid", 8);
        list.add(map);

        map = new HashMap<>();
        map.put("img", R.drawable.emsina);
        map.put("title", "新浪邮箱");
        map.put("sid", 7);
        list.add(map);
        return list;
    }
}
