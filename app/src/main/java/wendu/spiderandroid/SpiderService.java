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
public interface SpiderService {

    @FormUrlEncoded
    @POST("status?channel=1")
    Observable<SpiderResponse> sycTaskStatus(@Field("status") int status, @Field("bill_id") String id, @Field("email") String email,
                                             @Field("err_msg") String msg, @Field("count") Integer count, @Field("bank") String bank);
    @FormUrlEncoded
    @POST("upload?channel=1")
    Observable<SpiderResponse> upload(@Field("bill_id") String id, @Field("bank") String bank,
                                      @Field("email") String email, @Field("data") String data);
}
