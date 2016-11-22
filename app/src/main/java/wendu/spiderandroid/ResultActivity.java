package wendu.spiderandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wendu.common.base.BaseActivity;

public class ResultActivity extends BaseActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setActivityTitle("爬取结果");
        listView = getView(R.id.list);
        List<String> list = new ArrayList<>();
        Pattern phtml = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
        String cap;

        for (String s : LatestResult.getInstance().getData()) {
//            Matcher mhtml = phtml.matcher(s);
//            cap = mhtml.replaceAll("");
//            cap = cap.replaceAll("[(\\s+)｜(&nbsp;)]", "");
//
//            cap = cap.substring(0, cap.length() < 40 ? cap.length() : 40) + "...";
//            if (cap.equals("...")) {
//                cap = "[无文本内容]";
//            }
//            list.add(cap);
            list.add(s);
        }


        listView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item, list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(ResultActivity.this, DataReadActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }
        });
    }

}
