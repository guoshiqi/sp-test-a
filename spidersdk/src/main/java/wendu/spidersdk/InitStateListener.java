package wendu.spidersdk;

/**
 * Created by du on 16/12/24.
 */

public interface InitStateListener {
    void onSucceed(int scriptId, String startUrl, String script,int scriptCount,int taskId,int ua);
    void onFail(String msg, int code);
}
