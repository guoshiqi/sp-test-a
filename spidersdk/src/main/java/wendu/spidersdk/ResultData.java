package wendu.spidersdk;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
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
    public static ResultData getResult(Context ctx,boolean clearResultCache){
        File file=new File(ctx.getCacheDir()+"/spider.dat") ;
        FileInputStream fileInputStream = null;
        ResultData resultData=null;
        try {
            fileInputStream = new FileInputStream(file.toString());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            resultData= (ResultData) objectInputStream.readObject();
            if(clearResultCache){
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData;
    }
    public static ResultData getResult(Context ctx){
        return getResult(ctx,true);
    }
    public static String getLog(Context ctx){
        return ctx.getSharedPreferences("spider", Context.MODE_PRIVATE).getString("_log","");
    }

}
