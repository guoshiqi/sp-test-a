package converter;
import java.io.IOException;
import java.io.Reader;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by duwen on 2016/3/8.
 */
public class StringResponseBodyConverter implements Converter<ResponseBody, String> {

    @Override
    public String convert(ResponseBody value) throws IOException {
        try {
           return  readString(value.charStream());
        } finally {
            value.close();
        }
    }

    String readString(Reader is) throws IOException {
        char buff[]=new char[500];
        StringBuilder sb=new StringBuilder();
        for(int n;   (n= is.read(buff))!=-1;)   {
            sb.append(new String(buff,0,n));
        }
        return   sb.toString();
    }
}