package wendu.spider;
/**
 * Created by du on 16/3/16.
 */
public class DataController {

    public  static String testUrl="http://test.iguoxue.org/spider/emails/";
    //public  static String testUrl= "http://172.19.22.235/h5Test/dist/";

    public static SpiderServiceTest getUploadSerivceTest(){
            return    RetrofitUtil.createInstance(testUrl )
                        .create(SpiderServiceTest.class);
    }


    public static SpiderService getUploadSerivce(){
        return    RetrofitUtil.createInstance("http://119.29.112.230:9300/client/credit_bill/" )
                .create(SpiderService.class);
    }

//    public static SpiderServiceTest getSpiderSerivce(){
//        return    RetrofitUtil.createInstance(testUrl )
//                .create(SpiderServiceTest.class);
//    }
//
//    public static SpiderServiceTest getUploadSerivce(){
//        return    RetrofitUtil.createInstance(testUrl)
//                .create(SpiderServiceTest.class);
//    }
}
