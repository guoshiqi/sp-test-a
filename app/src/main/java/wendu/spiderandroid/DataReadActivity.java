package wendu.spiderandroid;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import wendu.common.base.BaseActivity;
import wendu.spidersdk.DSpider;

public class DataReadActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_read);
        setActivityTitle("内容");
        final int index=getIntent().getIntExtra("index",-1);
        final TextView textView=getView(R.id.text);
        if(getIntent().getBooleanExtra("log",false)){
            textView.setText(DSpider.getLastLog(this));
        }else {
            if (index != -1) {
                textView.setText(LatestResult.getInstance().getData().get(index));
            } else {
                textView.setText("no data");
            }
        }
    }
}
