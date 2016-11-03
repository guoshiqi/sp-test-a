log("****************Debug model *******************")
//function dSpiderTaobao(,callback){
//   dSpider(sessionkey, function(session,env,$){
//       session.get("",function(data){
//          callback(session,env,$,data)
//       })
//   });
//}

//dSpiderTaobao("sessionkey", function(session,env,$,data){
dSpider("sessionkey", function(session,env,$){
   // session为会话对象
   // env为平台环境参数
   // $ 为dQuery
   //session.upload([string|object])
   //session.finish() 结束爬取

   log(session,env,$)



})