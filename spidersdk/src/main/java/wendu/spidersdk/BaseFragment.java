package wendu.spidersdk;

import android.support.v4.app.Fragment;

import java.util.Map;

/**
 * Created by du on 16/10/8.
 */
public abstract class BaseFragment extends Fragment {
    public void errorReload() {}
    public Boolean goBack(){
        return false;
    }
    abstract void loadUrl(String url, Map<String, String> additionalHttpHeaders);
    abstract void loadUrl(String url);
    abstract void setUserAgent(String userAgent);

}
