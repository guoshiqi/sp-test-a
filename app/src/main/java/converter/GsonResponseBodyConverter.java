package converter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by duwen on 2016/3/8.
 */
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;
    private final Gson gson;

    GsonResponseBodyConverter(TypeAdapter<T> adapter, Gson gson) {
        this.adapter = adapter;
        this.gson = gson;
    }


    @Override public T convert(ResponseBody value) throws IOException {

        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        try {
            return adapter.read(jsonReader);
        } finally {
            value.close();
        }
//
//        String body=value.toString();
//
//        Type typeRes = new TypeToken<RetrofitUtil.SimpleRespond>(){}.getType();
//        RetrofitUtil.SimpleRespond respon =  gson.fromJson(body, typeRes);
//        //执行成功才去解析data字段
//        if(respon.errcode == 0){
//            T response = adapter.fromJson(body);
//            return response;
//        }else {
//            RetrofitUtil.Respond<Object> response =  new RetrofitUtil.Respond<Object>();
//            response.errstr = respon.errstr;
//            response.errcode = respon.errcode;
//            return  (T)response;
//        }

    }
}
