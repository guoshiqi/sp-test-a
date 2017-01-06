package wendu.spider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.Map;

import wendu.common.base.BaseActivity;
import wendu.common.utils.DpiHelper;
import wendu.common.widget.MenuItemView;
import wendu.common.widget.SectionVerticalLayout;

public class ResultActivity extends BaseActivity {
    LinearLayout root;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title)) {
            title = "结果";
        }
        setActivityTitle(title);
        root = getView(R.id.root);
        String json = getIntent().getStringExtra("json");
        if (TextUtils.isEmpty(json)) {
            List<String> result = LatestResult.getInstance().getData();
            int count = result.size();
            if (count > 0) {
                json = "[";
                for (int i = 0; i < result.size(); ++i) {
                    json += result.get(i);
                    if (i == count - 1) {
                        json += "]";
                    } else {
                        json += ",";
                    }
                }
            }
        }
        if (TextUtils.isEmpty(json)) {
            showDialog("木有数据！");
        } else {
            render(json);
        }
    }

    private void render(String json) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = null;
        try {
            jsonObject = parser.parse(json).getAsJsonObject();
        } catch (Exception e) {

        }
        if (jsonObject != null) {
            renderJsonObject(jsonObject);
        } else {
            try {
                JsonArray jsonArray = parser.parse(json).getAsJsonArray();
                renderJsonArray(jsonArray);
            } catch (Exception e) {
                showDialog("Json 格式错误");
            }
        }

    }

    private void renderJsonObject(JsonObject jsonObject) {
        SectionVerticalLayout group = new SectionVerticalLayout(this);
        group.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams gl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gl.topMargin = DpiHelper.dip2px(8f);
        group.setLayoutParams(gl);
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            MenuItemView menuItemView = new MenuItemView(this);
            menuItemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            menuItemView.setTitle(entry.getKey());
            JsonElement jsonElement = entry.getValue();
            if (jsonElement.isJsonPrimitive()) {
                menuItemView.setText(jsonElement.toString());
                menuItemView.setEnabled(false);
                menuItemView.showArrow(false);
            } else {
                menuItemView.showArrow(true);
                menuItemView.setTag(jsonElement.toString());
                if (jsonElement.isJsonArray()) {
                    menuItemView.setText("[数组 " + jsonElement.getAsJsonArray().size() + "条记录]");
                }
            }
            group.addView(menuItemView);
        }

        if (group.getChildCount() > 0) {
            root.addView(group);
            group.setOnItemClickListener(onJsonMenuItemClickListener);
        }
    }

    private void renderJsonArray(JsonArray jsonArray) {

        for (JsonElement jsonElement : jsonArray) {

            if (jsonElement.isJsonPrimitive()) {

                TextView textView = new TextView(this);
                textView.setText(jsonElement.toString());
                textView.setBackgroundResource(R.drawable.border_bottom_press_selector);
                textView.setEnabled(false);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                int paddingL = DpiHelper.dip2px(16f);
                int paddingT = DpiHelper.dip2px(10f);
                textView.setPadding(paddingL, paddingT, paddingL, paddingT);
                textView.setTextColor(Color.GRAY);
                root.addView(textView);

            } else if (jsonElement.isJsonObject()) {
                renderJsonObject(jsonElement.getAsJsonObject());
            } else if (jsonElement.isJsonArray()) {
                //仍然是数组
                MenuItemView menuItemView = new MenuItemView(this);
                menuItemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                menuItemView.setTitle("数组");
                int count = jsonElement.getAsJsonArray().size();
                if (count == 0) {
                    menuItemView.setText("空数组");
                    menuItemView.setEnabled(false);
                    menuItemView.showArrow(false);
                } else {
                    menuItemView.setText("[" + count + "条记录]");
                    menuItemView.setTag(jsonElement.toString());
                    menuItemView.setOnClickListener(onJsonMenuItemClickListener);

                }
                menuItemView.setBackgroundResource(R.drawable.white_press_selector);
                root.addView(menuItemView);
                View divider = new View(this);
                divider.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                root.addView(divider);
            }

        }
    }

    View.OnClickListener onJsonMenuItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MenuItemView target = (MenuItemView) v;
            String json = (String) v.getTag();
            if (TextUtils.isEmpty(json)) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("json", json);
            intent.putExtra("title", title + "-" + target.getTitle());
            intent.setClass(ResultActivity.this, ResultActivity.class);
            startActivity(intent);
        }
    };


}
