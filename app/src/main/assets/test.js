/**
 * Created by du on 16/11/21.
 */
dSpider("test", function(session,env,$){
    var count=100;
    log(env)
    session.showProgress();
    session.setProgressMsg("正在初始化");
    var timer=setInterval(function(){
      var cur=100-(--count);
      session.setProgress(cur);
      session.setProgressMsg("正在爬取第"+cur+"条记录");
      session.upload({title:"数据"+cur, data:"hi, I am data "+cur})
      if(count==0){
       clearInterval(timer);
       session.finish();
      }
    },50);
})
