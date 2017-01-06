package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by du on 16/4/15.
 */
public class Helper {

    public static final String TAG = "spider";

    public static int getColor(Context ctx, int resId) {
        int color = -1;
        try {
            color = ContextCompat.getColor(ctx, resId);
        } catch (Exception e) {

        }
        return color;
    }

    public static String getFromAssets(Context ctx, String... fileName) {
        String result = "";
        for (String file : fileName) {
            try {
                if (ctx == null) {
                    Log.e("xy log:", "getFromAssets context null");
                    break;
                }
                result += inputStream2String(ctx.getResources().getAssets().open(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            result += "\r\n";
        }
        return result;
    }

    public static String inputStream2String(InputStream is) {
        String result = "";
        String line;
        InputStreamReader inputReader = new InputStreamReader(is);
        BufferedReader bufReader = new BufferedReader(inputReader);
        try {
            while ((line = bufReader.readLine()) != null)
                result += line + "\r\n";
            bufReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static InputStream getStreamFromAssets(Context ctx, String... fileName) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(getFromAssets(ctx, fileName).getBytes("utf8"));

    }

    public static InputStream getDebugScript(Context ctx, String debugSrc) throws UnsupportedEncodingException {
        InputStream inputStream;
        return getStreamFromAssets(ctx, "_spider_start.js", "spider-android-debug.js", debugSrc, "_spider_end.js");
    }

    public static InputStream getDqueryScript(Context ctx) throws UnsupportedEncodingException {
        return getStreamFromAssets(ctx, "jquery-3.1.0.min.js");
    }

    public static Map<String, String> getMapForJson(String jsonStr) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonStr);

            Iterator<String> keyIter = jsonObject.keys();
            String key;
            String value;
            Map<String, String> valueMap = new HashMap<>();
            while (keyIter.hasNext()) {
                key = keyIter.next();
                value = jsonObject.getString(key);
                valueMap.put(key, value);
            }
            return valueMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get(String url) throws Exception {
        return request("GET", url, "");
    }

    public static String post(String url, String param) throws Exception {
        return request("POST", url, param);
    }

    static String addSign(Map<String, String> param) throws UnsupportedEncodingException {
        String key = "dad2488d7a06ezde3d933d";
        Map<String, String> commonPosts = new HashMap<String, String>() {
            {
                put("os_version", android.os.Build.VERSION.RELEASE);
                put("os", "android");
                put("mac_id", getDeviceId(DSpider.APP_CONTEXT));
                put("bundle_id", DSpider.APP_CONTEXT.getPackageName());
                put("sdk_version", DSpider.SDK_VERSION);
            }
        };
        try {
            PackageManager pm = DSpider.APP_CONTEXT.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(DSpider.APP_CONTEXT.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "" : pi.versionName;
                commonPosts.put("soft_version", versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        commonPosts.putAll(param);
        String msg = commonPosts.remove("msg");
        List<Map.Entry<String, String>> params = new ArrayList<>();
        Iterator iter = commonPosts.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            entry.setValue(URLEncoder.encode(entry.getValue(), "UTF-8"));
            params.add(entry);
        }
        Collections.sort(params, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
                return lhs.getKey().compareTo(rhs.getKey());
            }
        });

        StringBuffer sb = new StringBuffer();
        sb.append(key);
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getKey() + "=" + params.get(i).getValue());
            if (i < params.size() - 1) {
                sb.append("&");
            }
        }
        sb.append(key);
        String sign = md5Encode(sb.toString());
        commonPosts.put("sign", sign);
        if (!TextUtils.isEmpty(msg)) {
            commonPosts.put("msg", URLEncoder.encode(msg, "UTF-8"));
        }

        sb = new StringBuffer();

        iter = commonPosts.entrySet().iterator();
        int size = commonPosts.size();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            sb.append(entry.getKey() + "=" + entry.getValue());
            if (--size != 0) {
                sb.append("&");
            }
        }

        return sb.toString();


    }

    public static String md5Encode(String str) {
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            byte bytes[] = md5.digest();
            for (int i = 0; i < bytes.length; i++) {
                String s = Integer.toHexString(bytes[i] & 0xff);
                if (s.length() == 1) {
                    buf.append("0");
                }
                buf.append(s);
            }

        } catch (Exception ex) {
        }
        return buf.toString();
    }

    public static String post(String url, Map<String, String> param) throws Exception {
        return request("POST", url, addSign(param));
    }

    public static String request(String method, String url, String param) throws Exception {
        URL uri = new URL(url);
        method = method.toUpperCase();
        HttpURLConnection urlCon = (HttpURLConnection) uri.openConnection();
        urlCon.setRequestMethod(method);
        urlCon.setConnectTimeout(10000);
        urlCon.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        if (method.equals("POST")) {
            urlCon.setDoOutput(true);
            urlCon.setDoInput(true);
            if (!param.trim().isEmpty()) {
                PrintWriter pw = new PrintWriter(urlCon.getOutputStream());
                pw.print(param);
                pw.flush();
                pw.close();
            }
        }
        return inputStream2String(urlCon.getInputStream());

    }


    public static String getDeviceId(Context ctx) {

        try {
            TelephonyManager tm = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = tm.getDeviceId();
            WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure
                        .getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }
            return device_id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }


    public static void init(final Activity ctx, final int sid, @NonNull final InitStateListener initStateListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> param = new HashMap<>();
                param.put("sid", sid + "");
                try {
                    final String response = Helper.post(DSpider.BASE_URL + "script", param);
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject ret = new JSONObject(response);
                                int code = ret.getInt("errcode");
                                if (code != 0) {
                                    initStateListener.onFail(ret.getString("errmsg"),
                                            DSpider.Result.STATE_ERROR_MSG);

                                } else {
                                    ret = ret.getJSONObject("data");
                                    initStateListener.onSucceed(ret.getInt("script_id"),
                                            ret.getString("login_url"), ret.getString("script"));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                initStateListener.onFail(e.getMessage(),
                                        DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    initStateListener.onFail(e.getMessage(),
                            DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
                }
            }

        }).start();

    }


//    public static void initSpider(final Context ctx, @NonNull final InitStateListener initStateListener) {
//        final int device_id = ctx.getSharedPreferences("spider",
//                Context.MODE_PRIVATE).getInt("device_id", 0);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (device_id == 0) {
//                    try {
//                        JSONObject ret = new JSONObject(Helper.post(DSpider.BASE_URL + "device/save",
//                                collectDeviceInfo(ctx)));
//                        int code = ret.getInt("code");
//                        if (code != 0) {
//                            initStateListener.onFail(ret.getString("msg"),
//                                    DSpider.Result.STATE_ERROR_MSG);
//
//                        } else {
//                            int deviceId = ret.getInt("data");
//                            ctx.getSharedPreferences("spider", Context.MODE_PRIVATE)
//                                    .edit().putInt("device_id", deviceId).commit();
//                            DSpider.DEVICE_ID = deviceId;
//                            initStateListener.onSucceed(deviceId);
//
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        initStateListener.onFail(e.getMessage(),
//                                DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
//
//                    }
//                } else {
//                    DSpider.DEVICE_ID = device_id;
//                    initStateListener.onSucceed(device_id);
//                }
//            }
//        }).start();
//
//    }


    public static class ColorGradientHelper {
        private int startColor = Color.WHITE;

        public int getEndColor() {
            return endColor;
        }

        public int getStartColor() {
            return startColor;
        }

        private int endColor = Color.BLACK;
        private float partition = 100;
        private float deltaAlpha = 0;
        private float deltaRed = 0;
        private float deltaGreen = 0;
        private float deltaBlue = 0;

        public ColorGradientHelper(int startColor, int endColor) {
            setColorSpan(startColor, endColor);
        }

        public ColorGradientHelper() {
        }

        private void init() {
            deltaAlpha = (Color.alpha(endColor) - Color.alpha(startColor)) / partition;
            deltaRed = (Color.red(endColor) - Color.red(startColor)) / partition;
            deltaGreen = (Color.green(endColor) - Color.green(startColor)) / partition;
            deltaBlue = (Color.blue(endColor) - Color.blue(startColor)) / partition;
        }


        /**
         * @param partition 总的渐变色段
         */
        public void setPartition(int partition) {
            this.partition = partition;
            init();
        }

        public int getColor(int progress) {
            int color = startColor;
            if (progress != 0) {
                color = Color.argb(Color.alpha(startColor) + (int) (deltaAlpha * progress),
                        Color.red(startColor) + (int) (deltaRed * progress),
                        Color.green(startColor) + (int) (deltaGreen * progress),
                        Color.blue(startColor) + (int) (deltaBlue * progress));
            }
            return color;
        }

        public void setColorSpan(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            init();
        }

    }


}
