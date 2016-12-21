log("****************Debug model *******************")
dSpider("sessionkey", function(session,env,$){
    log("current page: "+location.href)

//    if(location.href.indexOf("gd.189.cn/TS/login")!=-1) {
           //            waitDomAvailable("#select_area", function(dom,timeSpan) {
           //                log("wait select_area success");
           //                $("#select_area").find("span").eq(0).text("深圳");
           //                $("#area").val("0755");
           //            },function() {
           //                log("wait fail");
           //            });
           //        } else if(location.href.indexOf("gd.189.cn/TS/?SESSIONID=") != -1) {
           ////            waitDomAvailable("a:contains('详单查询')", function(dom,timeSpan) {
           ////                log("wait 我的详单 success");
           ////                var detailPath=$('a:contains("详单查询")').attr("href");
           ////                location.href = detailPath;
           ////            },function() {
           ////                log("wait fail");
           ////            });
           //             location.href = "http://gd.189.cn/TS/cx/xiangdan_chaxun.htm?cssid=sy-kscx-xdcx";
           //        }

           if(location.href.indexOf("http://gd.189.cn/TS/index.htm") != -1) {
               var thxd = session.get("thxd")
               if(!thxd) {
                   thxd = {};
               }
               var userInfo = thxd["user_info"];
               if(!userInfo) {
                   userInfo = {};
               }
               userInfo["name"] = $("#user_name").text();
               thxd["user_info"] = userInfo;
               session.set("thxd", thxd);
               location.href = "http://gd.189.cn/TS/wode-wangting-sec.htm?cssid=sy-dh-top-wdwt";
           } else if(location.href.indexOf("gd.189.cn/TS/wode-wangting-sec.htm?cssid=sy-dh-top-wdwt") != -1) {
               var thxd = session.get("thxd")
               if(!thxd) {
                   thxd = {};
               }
               var userInfo = thxd["user_info"];
               if(!userInfo) {
                   userInfo = {};
               }

               userInfo["name"] = $("#custName").text();

               thxd["user_info"] = userInfo;
               session.set("thxd", thxd);

               location.href = "http://gd.189.cn/TS/cx/dqtccx.htm?cssid=wdwt-xgcx-dqtccx";
           } else if(location.href.indexOf("gd.189.cn/TS/cx/dqtccx.htm?cssid=wdwt-xgcx-dqtccx") != -1) {
               var thxd = session.get("thxd")
               if(!thxd) {
                   thxd = {};
               }
               var userInfo = thxd["user_info"];
               if(!userInfo) {
                   userInfo = {};
               }

               var taocan = "";
               $("#main_list").find("li a div p").each(function (i) {
                   if (i % 2 == 0) {
                       taocan+="|" + $(this).text();
                   }
               });
               userInfo["taocan"] = taocan.substr(1, taocan.length-1);

               thxd["user_info"] = userInfo;
               session.set("thxd", thxd);

               location.href = "http://gd.189.cn/transaction/taocanapply1.jsp?operCode=ChangeCustInfoNew";
           } else if (location.href.indexOf("http://gd.189.cn/OperationInitAction2.do?OperCode=ChangeCustInfoNew") != -1) {
               var thxd = session.get("thxd")
               if(!thxd) {
                   thxd = {};
               }
               var userInfo = thxd["user_info"];
               if(!userInfo) {
                   userInfo = {};
               }

               userInfo["name"]=$("#cust_name_id").val();
               userInfo["idcard_no"]=$("#id_num_id").val();
               userInfo["contactNum"]=$("#moblie_id").val();
               var address = $("#post_addr_id").val();
               if(address.length == 0) {
                   address = $("#id_addr_id").val();
               }
               userInfo["household_address"] = address;

               thxd["user_info"] = userInfo;
               session.set("thxd", thxd);

               location.href = "http://gd.189.cn/TS/cx/puk_chaxun.htm?cssid=wdwt-xgcx-puk_pincx";
           } else if (location.href.indexOf("gd.189.cn/TS/cx/puk_chaxun.htm?cssid=wdwt-xgcx-puk_pincx") != -1) {
               var thxd = session.get("thxd")
               if(!thxd) {
                   thxd = {};
               }
               var userInfo = thxd["user_info"];
               if(!userInfo) {
                   userInfo = {};
               }

               userInfo["name"]=$("#phone").text();

               thxd["user_info"] = userInfo;
               session.set("thxd", thxd);
               console.log(JSON.stringify(userInfo));
           }

})