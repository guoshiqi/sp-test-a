/**
 * Created by du on 16/11/21.
 */

dSpider("jianshu",20, function(session,env,$){
    var $items=$("div.title");
    var count=$items.length;
    if(!count) session.finish();
    alert(count)
    session.showProgress()
    session.setProgressMax(count)
    session.setProgressMsg("正在初始化");
    var i=1;
    var timer=setInterval(function(){
      session.setProgress(i);
      session.setProgressMsg("正在爬取第"+i+"条记录");
      session.push({title:$items.eq(i-1).text(), url:$items.eq(i-1).parent().attr("href")})
      if(++i==count){
       clearInterval(timer);
       session.finish();
      }
    },50);
})
