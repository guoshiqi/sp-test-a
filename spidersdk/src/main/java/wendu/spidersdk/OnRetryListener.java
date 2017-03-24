package wendu.spidersdk;

/**
 * Created by du on 17/3/24.
 */

public interface OnRetryListener {
    //if true retry, else exit.
    boolean onRetry(int code,String msg);
}
