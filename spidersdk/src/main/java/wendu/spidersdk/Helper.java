package wendu.spidersdk;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by du on 16/4/15.
 */
class Helper {
    public static boolean isDebug=false;
    public static int getColor(Context ctx,int resId) {
        int color=-1;
        try {
           color= ContextCompat.getColor(ctx, resId);
        }catch (Exception e){

        }
        return color;
    }
    public static String getFromAssets(Context ctx,String ...fileName) {
        String result = "";
        for(String file : fileName) {
            try {
                InputStreamReader inputReader = new InputStreamReader(ctx.getResources().getAssets().open(file));
                BufferedReader bufReader = new BufferedReader(inputReader);
                String line = "";
                while ((line = bufReader.readLine()) != null)
                    result += line+"\r\n";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public static InputStream getStreamFromAssets(Context ctx,String ...fileName) throws UnsupportedEncodingException {
       return new ByteArrayInputStream(getFromAssets(ctx, fileName).getBytes("utf8"));

    }
    public static InputStream getDebugScript(Context ctx) throws UnsupportedEncodingException {
        InputStream inputStream;
        return getStreamFromAssets(ctx, "_spider_start.js","spider-android-debug.js",SpiderActivity.debugSrc,"_spider_end.js");
    }
    public static InputStream getDqueryScript(Context ctx) throws UnsupportedEncodingException {
        return getStreamFromAssets(ctx, "jquery-3.1.0.min.js");
    }

    public static Map<String, String> getMapForJson(String jsonStr){
        JSONObject jsonObject ;
        try {
            jsonObject = new JSONObject(jsonStr);

            Iterator<String> keyIter= jsonObject.keys();
            String key;
            String value ;
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




    public static  class ColorGradientHelper{
        private int startColor= Color.WHITE;

        public int getEndColor() {
            return endColor;
        }

        public int getStartColor() {
            return startColor;
        }

        private int endColor=Color.BLACK;
        private float partition=100;
        private float deltaAlpha=0;
        private float deltaRed=0;
        private float deltaGreen=0;
        private float deltaBlue=0;
        public ColorGradientHelper(int startColor,int endColor){
            setColorSpan(startColor,endColor);
        }
        public ColorGradientHelper(){}
        private void init() {
            deltaAlpha = (Color.alpha(endColor) - Color.alpha(startColor)) / partition;
            deltaRed = (Color.red(endColor) - Color.red(startColor)) / partition;
            deltaGreen = (Color.green(endColor) - Color.green(startColor)) / partition;
            deltaBlue = (Color.blue(endColor) - Color.blue(startColor)) / partition;
        }


        /**
         *
         * @param partition 总的渐变色段
         */
        public void setPartition(int partition){
            this.partition=partition;
            init();
        }

        public int getColor(int progress){
            int color=startColor;
            if (progress!=0) {
                color = Color.argb(Color.alpha(startColor) + (int) (deltaAlpha * progress),
                        Color.red(startColor) + (int) (deltaRed * progress),
                        Color.green(startColor) + (int) (deltaGreen * progress),
                        Color.blue(startColor) + (int) (deltaBlue * progress));
            }
            return color;
        }

        public void setColorSpan(int startColor,int endColor){
            this.startColor=startColor;
            this.endColor=endColor;
            init();
        }

    }



}
