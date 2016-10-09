package wendu.spiderandroid;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import wendu.common.base.BaseActivity;

public class DataReadActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_read);
        setActivityTitle("内容");
        final int index=getIntent().getIntExtra("index",-1);
        final TextView textView=getView(R.id.text);
        if(index!=-1){
            textView.setText(LatestResult.getInstance().getData().get(index));
        }else {
            textView.setText("no data");
        }
        final TextView  toggle=getView(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggle.getText().equals("Text")){
                    String content= LatestResult.getInstance().getData().get(index);
                    content=content.replaceAll("<[^>]+>","");
                    content = content.replaceAll("[(\\s+)｜(&nbsp;)]", "");
                    if (TextUtils.isEmpty(content)) {
                        content = "[无文本内容]";
                    }
                   textView.setText(content);
                   toggle.setText("Html");
                }else {
                   textView.setText(LatestResult.getInstance().getData().get(index));
                   toggle.setText("Text");
                }
            }
        });
    }
}
