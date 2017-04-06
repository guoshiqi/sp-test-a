package wendu.spidersdk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by du on 16/4/15.
 */
 class Helper {

    public static final String TAG = "spider";
    public static X509TrustManager trustManager;
    public static Context APP_CONTEXT;
    public static OnRetryListener retryListener;

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
                put("mac_id", getDeviceId(APP_CONTEXT));
                put("bundle_id", APP_CONTEXT.getPackageName());
                put("sdk_version", DSpider.SDK_VERSION);
                put("model", Build.MODEL);
                put("app_id",DSpider.APPID+"");
            }
        };
        try {
            PackageManager pm =APP_CONTEXT.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(APP_CONTEXT.getPackageName(), PackageManager.GET_ACTIVITIES);
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
        if(url.toLowerCase().startsWith("https")){
            return requestHttps(method, url, param);
        }
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


    private  static String requestHttps(String method, String url, String param) throws Exception {
        URL uri = new URL(url);
        method = method.toUpperCase();
        HttpsURLConnection urlCon = (HttpsURLConnection) uri.openConnection();
        urlCon.setRequestMethod(method);
        urlCon.setConnectTimeout(10000);
        urlCon.setSSLSocketFactory(getSSLSocketFactory());
        urlCon.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return  "api.dtworkroom.com".equals(hostname);
//                HostnameVerifier hv=HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify("*.dtworkroom.com",session);
            }
        });
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


    public static X509TrustManager getTrustManager(){
        try {
            if(trustManager == null){
                trustManager = trustManagerForCertificates(APP_CONTEXT
                 // .getAssets().open("www.dtworkroom.com.crt"));
                //trustManager =APP_CONTEXT trustManagerForCertificates(
                        .getAssets().open("dspider.der"));
            }
            return  trustManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static SSLSocketFactory getSSLSocketFactory(){
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { getTrustManager() }, new java.security.SecureRandom());
            return  sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try{
            keyStore.load(null, password);
        }catch (Exception e){
        }
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }

        return (X509TrustManager)trustManagers[0];
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

    public static void init(final Activity ctx, final int sid,final int retryCount, @NonNull final InitStateListener initStateListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> param = new HashMap<>();
                param.put("sid", sid + "");
                param.put("retry", retryCount + "");
                Helper.APP_CONTEXT = ctx.getApplicationContext();
                try {
                    final String response = Helper.post(DSpider.BASE_URL + "script", param);
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject ret = new JSONObject(response);
                                int code = ret.getInt("code");
                                if (code != 0) {
                                    initStateListener.onFail(ret.getString("msg"),
                                            DSpider.Result.STATE_ERROR_MSG);

                                } else {
                                    ret = ret.getJSONObject("data");
                                    initStateListener.onSucceed(ret.getInt("script_id"), ret.getString("login_url"),
                                            ret.getString("script"), ret.getInt("script_count"),
                                            ret.has("id") ? ret.getInt("id") : 0,ret.has("ua") ? ret.getInt("ua") : 1);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                initStateListener.onFail(e.getMessage(),
                                        DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
                            }
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e instanceof SSLHandshakeException) {
                                initStateListener.onFail("您当前网络环境不安全",
                                        DSpider.Result.STATE_ERROR_MSG);
                            } else {
                                initStateListener.onFail(e.getMessage(),
                                        DSpider.Result.STATE_DSPIDER_SERVER_ERROR);
                            }
                        }
                    });
                }
            }

        }).start();

    }



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
