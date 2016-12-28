/**
 * Created by du on 16/11/21.
 */
dSpider("test", function(session,env,$){
    var count=100;
    session.showProgress();
    session.setProgressMsg("正在初始化");
    var timer=setInterval(function(){
      var cur=100-(--count);
      session.setProgress(100-(--count));
      session.setProgressMsg("正在爬取第"+cur+"条记录");
      if(count==0){
       clearInterval(timer);
       session.finish();
      }
    },50);
})
