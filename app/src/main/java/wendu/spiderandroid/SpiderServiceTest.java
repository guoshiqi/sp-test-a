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
    @GET("inject.php?platform=android")
    Observable<String> getInjectJs(@Query("refer") String refer);

    @GET("inject.php")
    Observable<String> getInjectJsLikeIos(@Query("refer") String refer);

    @FormUrlEncoded
    @POST("status.php")
    Observable<SpiderResponse> sycTaskStatus(@Field("status") int status, @Field("bill_id") String id, @Field("email") String email,
                                             @Field("err_msg") String msg, @Field("count") Integer count, @Field("bank") String bank);
    @FormUrlEncoded
    @POST("upload.php")
    Observable<SpiderResponse> upload(@Field("bill_id") String id, @Field("bank") String bank,
                                      @Field("email") String email, @Field("data") String data);
}
