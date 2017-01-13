package wendu.spidersdk;

/**
 * Created by du on 16/12/24.
 */

public interface InitStateListener {
    void onSucceed(int taskId, String startUrl, String script,int scriptCount);
    void onFail(String msg, int code);
}
