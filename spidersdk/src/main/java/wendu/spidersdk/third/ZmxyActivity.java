package wendu.spidersdk.third;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.moblie.zmxy.antgroup.creditsdk.app.CreditApp;
import com.android.moblie.zmxy.antgroup.creditsdk.app.ICreditListener;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Set;

import wendu.spidersdk.Helper;
import wendu.spidersdk.R;


public class ZmxyActivity extends AppCompatActivity {

    private EditText idcard;
    private EditText name;
    private Button button;
    CreditApp creditApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zmxy);

        idcard = (EditText) findViewById(R.id.idcard);
        name = (EditText) findViewById(R.id.name);
        idcard.addTextChangedListener(textWatcher);
        name.addTextChangedListener(textWatcher);
        button = (Button) findViewById(R.id.btn);
        creditApp = CreditApp.getOrCreateInstance(getApplicationContext());
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = Helper.get(String.format("http://10.0.10.26:30010/auth_req?idcard=%s&name=%s",
                                    idcard.getText().toString().trim(),
                                    name.getText().toString().trim()));
                            JSONObject ret = new JSONObject(result);
                            if (ret.getInt("err_code") != 0) {
                                showMessage("错误");
                            } else {
                                final String extra = String.format("idcard=%s&name=%s", ret.getString("idcard"), ret.getString("name"));
                                creditApp.authenticate(ZmxyActivity.this,
                                        ret.getString("app_id"),
                                        null,
                                        ret.getString("params"),
                                        ret.getString("sign"),
                                        null,
                                        new ICreditListener() {
                                            @Override
                                            public void onComplete(Bundle result) {
                                                if (result != null) {
                                                    Set<String> keys = result.keySet();
                                                    String params = "";
                                                    for (String key : keys) {
                                                        try {
                                                            Log.d(Helper.TAG, params += (key + "=" + URLEncoder.encode(result.getString(key), "UTF-8") + "&"));
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            showMessage("授权失败");
                                                            return;
                                                        }
                                                    }
                                                    params += extra;
                                                    final String finalParams = params;
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                String result = Helper.get("http://10.0.10.26:30010/auth_resp?" + finalParams);
                                                                JSONObject ret = new JSONObject(result);
                                                                if (ret.getInt("err_code") == 0) {
                                                                    setResult(RESULT_OK);
                                                                    showMessage("成功了");
                                                                    onBackPressed();
                                                                } else {
                                                                    setResult(RESULT_CANCELED);
                                                                    showMessage("失败了");
                                                                }
                                                            } catch (Exception e) {
                                                                showMessage("网络错误");
                                                                setResult(RESULT_CANCELED);
                                                                e.printStackTrace();
                                                            }

                                                        }
                                                    }).start();
                                                }

                                            }

                                            @Override
                                            public void onError(Bundle bundle) {
                                                showMessage("授权失败");
                                                setResult(RESULT_CANCELED);
                                            }

                                            @Override
                                            public void onCancel() {
                                                setResult(RESULT_CANCELED);
                                                showMessage("授权取消");
                                            }
                                        });

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        creditApp.destroy();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ZmxyActivity.super.onBackPressed();
            }
        });

    }

    public void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CreditApp.onActivityResult(requestCode, resultCode, data);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String idn = idcard.getText().toString().trim();
            if (idn.isEmpty() || name.getText().toString().trim().isEmpty() || idn.length() < 15) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
            }
        }
    };
}
