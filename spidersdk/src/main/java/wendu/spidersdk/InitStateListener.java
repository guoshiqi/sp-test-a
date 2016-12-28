package wendu.spidersdk;

/**
 * Created by du on 16/12/24.
 */

public interface InitStateListener {
    void onSucceed(int deviceId);
    void onFail(String msg,int code);
}
