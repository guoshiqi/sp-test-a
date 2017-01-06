package wendu.spider;


import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.ActivityLifecycleProvider;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.FragmentLifecycleProvider;
import com.trello.rxlifecycle.kotlin.RxlifecycleKt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import converter.GsonConverterFactory;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observable.Transformer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import wendu.common.utils.ApplicationUtils;
import wendu.common.utils.ResUtil;
import wendu.common.utils.Utils;


/**
 * Created by duwen on 2016/3/4.
 */

public class RetrofitUtil {
    //public static final String BASE_URL = "https://cardloan.xiaoying.com/1.0/";
//    public static final String BASE_HOST = "119.29.112.230";
//    public static final Integer BASE_PORT = 8122;

    public static final String BASE_HOST = BuildConfig.DEBUG? "119.29.112.230":"cardloan.xiaoying.com";
    public static final Integer BASE_PORT =  BuildConfig.DEBUG? 8122:null;


    public static  String BASE_VERSION = "1.1";
    public static  String BASE_URL =( BASE_PORT == null? String.format("https://%s/%s/", BASE_HOST,BASE_VERSION) :
            String.format("http://%s:%s/%s/", BASE_HOST,BASE_PORT,BASE_VERSION));

    static final String CUSTOM_ERROR_TAG = "custom_error";
    //Timeout in minute
    static final int DEFAULT_TIME_OUT = 50; //默认超时
    static final int UPLOAD_TIME_OUT = 5;
    static final int DOWNLOAD_TIME_OUT = 5;
    public static final int ERROR_CODE_NEED_RELOGIN = -4;

    private static final String MD5KEY = "xiaoyingkadai";

    static volatile Retrofit instance;
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            //.cookieJar(new CookiesManager())
                    //.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.MINUTES)
            .addInterceptor(new SignInterceptor())
           // .addNetworkInterceptor(new StethoInterceptor())
            .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
            .build();

    public static Map<String, String> commonPosts = new HashMap<String, String>() {
        {
            put("soft_version", "1.0");
            put("os_version", android.os.Build.VERSION.RELEASE);
            put("os", "android");
            put("mac_id", Utils.getDeviceId(ApplicationUtils.getContext()));
            put("ut", "");
            put("language", "zh-Hans-CN");
        }
    };

    public static class Respond<T> {
        public static final int SUCCESS = 0;
        public static final int NETWORK_ERROR = 1;
        public static final int RESPONSE_ERROR = -2;
        public static final String serverErr = "服务器错误，请稍后再试";
        public int errcode = RESPONSE_ERROR;
        public String errstr = "respond data malformed!";
        public T data;
    }

    public static class SimpleRespond {
        public String errstr = "respond data malformed!";
        public int errcode  = -2;
        Respond<Object> a = new Respond<Object>();

    }



    public static Observable<String> download(final String url, final String savePath) {
        return download(url, savePath, null);
    }

    public static Observable<String> download(final String url, final String savePath, final Map<String, String> postMap) {

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                FormBody.Builder builder = new FormBody.Builder();
                if (postMap != null) {
                    for (String key : postMap.keySet()) {
                        builder.add(key, postMap.get(key));
                    }
                }

                final Request request = new Request.Builder()
                        .url(url)
                        .post(builder.build())
                        .build();
                try {

                    OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder();
                    clientBuilder.interceptors().clear();
                    clientBuilder.cookieJar(null);
                    clientBuilder
                            //.connectTimeout(DOWNLOAD_TIME_OUT, TimeUnit.MINUTES)
                            .readTimeout(DOWNLOAD_TIME_OUT, TimeUnit.MINUTES)
                            .writeTimeout(DOWNLOAD_TIME_OUT, TimeUnit.MINUTES)
                            .build();

                    okhttp3.Response response = clientBuilder.build().newCall(request).execute();
                    if (response.isSuccessful()) {
                        InputStream in = response.body().byteStream();
                        File f = new File(savePath);
                        OutputStream os = new FileOutputStream(f);
                        byte buff[] = new byte[500];
                        for (int n; (n = in.read(buff)) != -1; ) {
                            os.write(buff, 0, n);
                        }
                        os.close();
                        subscriber.onNext(savePath);
                    } else {
                        subscriber.onError(new Exception(CUSTOM_ERROR_TAG + "|" + response.code() + "|" + response.message()));
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).compose(RetrofitUtil.<String>threadSwitcher());

    }



    public static <T> T create(Class<T> cls) {
        return defaultInstance().create(cls);
    }

    public static <T> T create(String baseUrl, Class<T> cls) {
        return createInstance(baseUrl).create(cls);
    }

    public static Retrofit createInstance(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }


    public static Retrofit defaultInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (RetrofitUtil.class) {
            if (instance == null) {
                instance = createInstance(BASE_URL);
            }
            return instance;
        }

    }


    //for retrofit callback
    public abstract static class Callback<T> implements retrofit2.Callback<Respond<T>> {

        public abstract void onResponse(T data);

        public abstract void onFailure(int code, String msg);

        @Override
        public void onResponse(Call<Respond<T>> call, Response<Respond<T>> respond) {
            int code = respond.body().errcode;
            if (code == Respond.SUCCESS) {
                onResponse(respond.body().data);
            } else {
                onFailure(code, respond.body().errstr);
            }
        }

        @Override
        public void onFailure(Call<Respond<T>> call, Throwable throwable) {
            onFailure(Respond.NETWORK_ERROR, throwable.getMessage());
        }

    }

    //for  rxJava
    public abstract static class CustomSubscriber<T> extends rx.Subscriber<T> {

        public abstract void onResponse(T data);

        public abstract void onFailure(int code, String msg);

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.startsWith(CUSTOM_ERROR_TAG)) {
                String[] info = errMsg.split("\\|");
                int code = Integer.valueOf(info[1]);
                if(code == -4){
                    //YztApplication.getInstance().sendBroadcast(new Intent(BROADCAST_ACTION_NEED_RELOGIN));
                    //EventBus.getDefault().post(new MessageEvent(MessageEvent.MESSAGE_ACTION_NEED_RELOGIN));

                }
                onFailure(code, info.length > 2 ? info[2] : "");
            } else {
                //onFailure(Respond.NETWORK_ERROR, e.getMessage());
                onFailure(Respond.RESPONSE_ERROR, Respond.serverErr);

            }
        }

        @Override
        public void onNext(T respond) {
            try {
                if(respond != null){
                    onResponse(respond);
                }else {
                    onFailure(Respond.RESPONSE_ERROR, Respond.serverErr);
                }
            }catch (Exception e){
                e.printStackTrace();
                onFailure(Respond.RESPONSE_ERROR, e.getMessage());

            }
        }
    }

    public static <T> Transformer<T, T> threadSwitcher() {
        return new Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }


    public static <T> Observable<T> hull(Observable<Respond<T>> observable) {
        return hull(observable, true);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, boolean switchThread) {
        return hull(observable, null, null, switchThread);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, ActivityLifecycleProvider activity) {
        return hull(observable, activity, ActivityEvent.DESTROY);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, ActivityLifecycleProvider activity, boolean switchThread) {
        return hull(observable, activity, ActivityEvent.DESTROY, switchThread);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, ActivityLifecycleProvider activity, ActivityEvent event) {
        return hull(observable, activity, event, true);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, FragmentLifecycleProvider fragment) {
        return fragmentHull(observable, fragment, FragmentEvent.DESTROY, true);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, FragmentLifecycleProvider fragment, boolean switchThread) {
        return fragmentHull(observable, fragment, FragmentEvent.DESTROY, switchThread);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, FragmentLifecycleProvider fragment, FragmentEvent event) {
        return fragmentHull(observable, fragment, event, true);
    }

    public static <T> Observable<T> hull(Observable<Respond<T>> observable, ActivityLifecycleProvider activity, ActivityEvent event, boolean switchThread) {
        if (activity != null && event != null) {
            observable = RxlifecycleKt.bindUntilEvent(observable, activity, event);
        }
        Observable<T> nake = observable.map(new Func1<Respond<T>, T>() {
            @Override
            public T call(Respond<T> respond) {
                int code = respond.errcode;
                if (code == Respond.SUCCESS) {
                    return respond.data;
                } else {
                    throw new RuntimeException(CUSTOM_ERROR_TAG + "|" + respond.errcode + "|" + respond.errstr);
                }
            }
        });

        if (switchThread) {
            return nake.compose(RetrofitUtil.<T>threadSwitcher());
            //return nake.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return nake;


    }

    public static <T> Observable<T> fragmentHull(Observable<Respond<T>> observable, FragmentLifecycleProvider fragment, FragmentEvent event, boolean switchThread) {
        if (fragment != null && event != null) {
            observable = RxlifecycleKt.bindUntilEvent(observable, fragment, event);
        }
        Observable<T> nake = observable.map(new Func1<Respond<T>, T>() {
            @Override
            public T call(Respond<T> respond) {
                int code = respond.errcode;
                if (code == Respond.SUCCESS) {
                    return respond.data;
                } else {
                    throw new RuntimeException(CUSTOM_ERROR_TAG + "|" + respond.errcode + "|" + respond.errstr);
                }
            }
        });

        if (switchThread) {
            return nake.compose(RetrofitUtil.<T>threadSwitcher());
            //return nake.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return nake;


    }


    private static class SignInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Map<String,String> params=new HashMap<>();
            commonPosts.put("ut",  String.valueOf(System.currentTimeMillis() / 1000));
            params.putAll(commonPosts);
            for(int i=0;i<request.url().querySize();i++){
                params.put(request.url().queryParameterName(i), request.url().queryParameterValue(i));
            }

            HttpUrl.Builder builder = request.url().newBuilder();

            for (String key : params.keySet()) {
               builder.addQueryParameter(key,params.get(key));
            }
            request = request.newBuilder().url(builder.build()).build();
            okhttp3.Response response = null;
            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(CUSTOM_ERROR_TAG + "|" + Respond.NETWORK_ERROR + "|"
                        + ResUtil.getString(R.string.net_error));
            }
            return response;
        }


    }

}
