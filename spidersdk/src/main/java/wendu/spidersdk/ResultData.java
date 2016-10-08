package wendu.spidersdk;

import java.io.Serializable;
import java.util.List;

/**
 * Created by du on 16/10/8.
 */
public class ResultData implements Serializable{
    public List<String>datas;
    public String sessionKey;
    public String errorMsg;

    public ResultData(String sessionKey,List<String>datas,String errorMsg){
        this.sessionKey=sessionKey;
        this.datas=datas;
        this.errorMsg=errorMsg;
    }
}
