package wendu.spidersdk;

import android.support.v4.app.Fragment;
import android.view.ViewGroup;

/**
 * Created by du on 16/10/8.
 */
public abstract class BaseFragment extends Fragment {
    public void errorReload() {}
    public void showInput(boolean show){
       ViewGroup viewGroup= (ViewGroup) getView();
       if (viewGroup!=null)
       viewGroup.setDescendantFocusability(show?ViewGroup.FOCUS_AFTER_DESCENDANTS:ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }


}
