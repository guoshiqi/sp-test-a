
dSpider("test", function(session,env,$) {

 log(env)
 session.upload("Hi, I am the test data!")
 session.finish()

})