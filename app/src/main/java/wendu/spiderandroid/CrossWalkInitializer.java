/**
 * author duwen ,created 2016.8.23
 **/

package wendu.spiderandroid;

import android.app.Activity;

import org.xwalk.core.XWalkInitializer;
import org.xwalk.core.XWalkUpdater;

import wendu.common.utils.LogUtil;

public class CrossWalkInitializer
        implements XWalkInitializer.XWalkInitListener,XWalkUpdater.XWalkBackgroundUpdateListener {

    private XWalkInitializer mXWalkInitializer;
    private XWalkUpdater mXWalkUpdater;
    private Activity context;
    private  boolean isAvaiable=false;
    private InitListener initListener;
    private boolean withDownload=false;

    protected CrossWalkInitializer(Activity activity) {
        context=activity;
        mXWalkInitializer = new XWalkInitializer(this,context);
    }

    public void init(boolean withDownload){
        this.withDownload=withDownload;
        if (isAvaiable()){
            return;
        }
        mXWalkInitializer.initAsync();
    }

    public  boolean isAvaiable(){
        return  isAvaiable;
    }

    public static CrossWalkInitializer create(Activity activity){
        return new CrossWalkInitializer(activity);
    }

    public InitListener getInitListener() {
        return initListener;
    }

    public void setInitListener(InitListener initListener) {
        this.initListener = initListener;
    }


    @Override
    public void onXWalkInitStarted() {

    }

    @Override
    public void onXWalkInitCancelled() {

    }

    @Override
    public void onXWalkInitFailed() {
        if(withDownload) {
            if (mXWalkUpdater == null) {
                mXWalkUpdater = new XWalkUpdater(this, context);
            }
            if (initListener != null) {
                initListener.onDownloadStart();
            }
            mXWalkUpdater.updateXWalkRuntime();
        }
    }

    @Override
    public void onXWalkInitCompleted() {
         if (initListener!=null){
             initListener.onSuccess();
         }
        isAvaiable=true;
    }

    public void cancel(){
        mXWalkInitializer.cancelInit();
        if (mXWalkUpdater != null) {
           mXWalkUpdater.cancelBackgroundDownload();
        }
    }

    @Override
    public void onXWalkUpdateStarted() {
        LogUtil.e("onXWalkUpdateStarted");
    }

    @Override
    public void onXWalkUpdateProgress(int percentage) {
        if (initListener!=null){
         initListener.onProgress(percentage);
        }
        LogUtil.e( "XWalkUpdate progress: " + percentage);
    }


    @Override
    public void onXWalkUpdateCancelled() {

    }

    @Override
    public void onXWalkUpdateFailed() {
        if (initListener!=null){
            initListener.onFailed();
        }
        LogUtil.e( "onXWalkUpdateFailed: ");
    }

    @Override
    public void onXWalkUpdateCompleted() {
        if (initListener!=null){
            initListener.onProgress(100);
        }
        mXWalkInitializer.initAsync();
    }

    public static abstract class InitListener{
        abstract void onSuccess();
        abstract void onFailed();
        void onDownloadStart(){}
        void onProgress(int progress){}
    }
}
