package wendu.spider;

import android.os.Bundle;
import android.widget.TextView;

import wendu.common.base.BaseActivity;
import wendu.spidersdk.DSpider;

public class LogActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_read);
        setActivityTitle("上次爬取日志页");
        final TextView textView = getView(R.id.text);
        textView.setText(DSpider.getLastLog(this));

    }
}
