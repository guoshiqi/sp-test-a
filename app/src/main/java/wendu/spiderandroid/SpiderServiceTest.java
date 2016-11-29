package wendu.spiderandroid;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by du on 16/8/16.
 */
public interface SpiderServiceTest {
    @FormUrlEncoded
    @POST("inject.php")
    Observable<String> upload(@Field("data") String data);
}
