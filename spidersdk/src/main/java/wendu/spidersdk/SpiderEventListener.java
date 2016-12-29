package wendu.spidersdk;

import java.util.List;

/**
 * Created by du on 16/12/23.
 */

public abstract class SpiderEventListener {
    public void onResult(String sessionKey, List<String> data) {

    }

    public void onProgress(int progress, int max) {

    }

    public void onError(int code, String msg) {

    }

}
