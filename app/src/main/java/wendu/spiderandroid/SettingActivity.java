package wendu.spiderandroid;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import wendu.common.base.BaseActivity;
import wendu.common.utils.KvStorage;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setActivityTitle("设置");
        SwitchCompat debugSwitch = getView(R.id.debug_switch);
        debugSwitch.setChecked(KvStorage.getInstance().getBoolean("debug", false));
        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                KvStorage.getInstance().edit().putBoolean("debug", isChecked).commit();
            }
        });
    }
}
