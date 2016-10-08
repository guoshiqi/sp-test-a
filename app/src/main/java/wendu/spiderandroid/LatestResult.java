package wendu.spiderandroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by du on 16/9/1.
 */
public class LatestResult {
    private static LatestResult ourInstance = new LatestResult();
    List<String> result;
    public static LatestResult getInstance() {
        return ourInstance;
    }
    private LatestResult() {

    }
    public List<String> getData(){
        if (result==null){
            result=new ArrayList<>();
        }
        return result;
    }

}
