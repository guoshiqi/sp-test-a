log("****************Debug model *******************")
dSpider("sessionkey", function(session,env,$){
    log("current page: "+location.href)


// sendRandombySms();
// $.ajax({
// 			type : "POST",
// 			url : "/iframe/feequery/smsRandCodeSend.action",
// 			cache : false,
// 			data: {accNum:"18911137181"},
// 			dataType : "json",
// 			success : function(json) {
// 			log(json)
// 				if (json.tip == null || json.tip == "") {
// 					//成功后,要隐藏,还原
// 					timer_count = setInterval(countNum, 1000);
// 					alert("随机码短信发送成功，请查收。");
// 				} else {//失败了提示和计时器隐藏,链接显示
// 					alert(json.tip);
// 				}
// 			},
// 			error : function(json) {
// 				alert("对不起，随机码短信发送失败！请稍后重试。");
// 			}
// 		});

    /**
     * body ifram
     *
     * @param url
     *            跳转链接
     */
//function gotoIfremBody(url, fastcode) {
//    if ("#" != url) {
//        // 跳转之前判断Session是否为空
//        $.ajax({
//            type: "POST",
//            url: "/dqmh/my189/checkMy189Session.do",
//            data: {
//                fastcode: fastcode
//            },
//            cache: false,
//            dataType: "json",
//            success: function (obj) {
//                log(obj.status)
//                // 业务异常处理
//                if (obj.status == 2) {
//                    var redirectUrlSb = url;
//                    // 定义数组，获取连接
//                    var arrRedirectUrl = url.split("toStUrl=");
//                    // 包含单点登录
//                    if (arrRedirectUrl.length > 1) {
//                        // 包含问号
//                        if (arrRedirectUrl[1].indexOf("?") > -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
//                            // 包含问号传参用&符号拼接
//                            redirectUrlSb = url + "&fastcode=" + fastcode;
//                        } else if (arrRedirectUrl[1].indexOf("?") <= -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
//                            // 不包含问号，直接拼接问号
//                            redirectUrlSb = url + "?fastcode=" + fastcode;
//                        }
//                    } else {
//                        // 包含问号
//                        if (arrRedirectUrl[0].indexOf("?") > -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
//                            // 包含问号传参用&符号拼接
//                            redirectUrlSb = url + "&fastcode=" + fastcode;
//
//                        } else if (arrRedirectUrl[0].indexOf("?") <= -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
//                            // 不包含问号，直接拼接问号
//                            redirectUrlSb = url + "?fastcode=" + fastcode;
//                        }
//                    }
//                    // 4g的场合拼接CityCode
//                    if ("4g" == $("#menuType").val()) {
//                        redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val() + "&type=4g";
//                    }else{
//                        redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val();
//                    }
//
//log(redirectUrlSb);
//                    $("#bodyIframe").attr("src", redirectUrlSb);
//                } else if ("4g" == $("#menuType").val()) {
//                    location.href = "/dqmh/my189/initMy189home4g.do?fastcode="
//                    + fastcode;
//                } else {
//                    location.href = "/dqmh/my189/initMy189home.do?fastcode="
//                    + fastcode;
//                }
//            }
//        });
//    }
//}
//
//gotoIfremBody('/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action','01390638');
//$("#bodyIframe").onload = function(){
//log("Local iframe is now loaded.");
//sendRandombySms();
//};
//if ($("#bodyIframe").attachEvent) {
//    $("#bodyIframe").attachEvent("onload", function() {
//                //以下操作必须在iframe加载完后才可进行
//        log("Local iframe is now loaded.");
//                 sendRandombySms();
//    });
//} else {
//    $("#bodyIframe").onload = function() {
//                //以下操作必须在iframe加载完后才可进行
//        log("Local iframe is now loaded.");
//                 sendRandombySms();
//    };
//}

//        if(location.href.indexOf("http://www.189.cn/dqmh/my189/initMy189home.do")!=-1) {
//                location.href="http://www.189.cn/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action?fastcode=01390638&cityCode=bj"
//            } else if(location.href.indexOf("189.cn/iframe/feequery/detailBillIndex.action")!=-1) {
//                waitDomAvailable("#smsRandCode", function(dom,timeSpan) {
//                    log("wait success");
//                    var  options = {
//                        'attributes':true
//                    } ;
//                    var target = document.getElementById("smsRandCode");
//                    Observe(target, options, function(mutations) {
//                        log($("#smsRandCode").val());
//                    });
//
//                    var userDetailLst = $('#userDetailLst');//the element I want to monitor
//                    userDetailLst.bind('DOMNodeInserted', function(e) {
//                        if($(".ued-table tbody").length>=1) {
//                            userDetailLst.unbind('DOMNodeInserted');
//                            alert('element now contains: ' + userDetailLst.html());
//
//                            $.ajax({
//                                type: "POST",
//                                url: func_rootpath+"/iframe/feequery/billDetailQuery.action",
//                                data: {
//                                    requestFlag: "synchronization",
//                                    billDetailType:"1",
//                                    qryMonth:"2016年10月",
//                                    startTime:"1",
//                                    accNum:$("#qryAccNo").html(),
//                                    endTime:"31"
//                                },
//                                dataType: "html",
//                                success: function(page){
//                                    var s="";
//                                    $("<div>").append($(page)).find(".ued-table tbody tr:gt(0):not(.trlast)").each(function(i){
//                                        log("class name:" + $(this).className);
//                                        if($(this).className) {
//                                            return true;
//                                        }
//                                        $(this).children("td").each(function(i){
//                                            s+="\t"+$(this).text();
//                                        }); s+="\n"
//                                    });
//                                    log(s);
//                                    if($("<div>").append($(page)).find('a:contains("下一页")').length>=1){
//                                    }
//                                },
//                                error:function(){
//                                    alert("对不起，详单记录查询失败，请稍后重试！");
//                                }
//                            });
//                        }
//
//                    });
//
//                    sendRandombySms();
//                },function() {
//                    log("wait fail");
//                    log($('#smsRandCode').val());
//                });
//            }

//    if(location.href.indexOf("http://www.189.cn/dqmh/my189/initMy189home.do")!=-1) {
//        location.href="http://www.189.cn/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action?fastcode=01390638&cityCode=bj"
//    } else if(location.href.indexOf("189.cn/iframe/feequery/detailBillIndex.action")!=-1) {
//        waitDomAvailable("#smsRandCode", function(dom,timeSpan) {
//            log("wait success");
//            var  options = {
//                'attributes':true
//            } ;
//            var target = document.getElementById("smsRandCode");
//            Observe(target, options, function(mutations) {
//                log($("#smsRandCode").val());
//            });
//
//            var userDetailLst = $('#userDetailLst');//the element I want to monitor
//            userDetailLst.bind('DOMNodeInserted', function(e) {
//                if($(".ued-table tbody").length>=1) {
//                    userDetailLst.unbind('DOMNodeInserted');
////                    alert('element now contains: ' + userDetailLst.html());
//                    loadData("2016年10月",1);
//                    loadData("2016年09月",1);
//                    loadData("2016年08月",1);
//                    loadData("2016年07月",1);
//                    loadData("2016年06月",1);
//                    loadData("2016年05月",1);
//                }
//
//            });
//
//            sendRandombySms();
//        },function() {
//            log("wait fail");
//            log($('#smsRandCode').val());
//        });
//    }
//
//    function loadData(month, pageNum) {
//        $.ajax({
//            type: "POST",
//            url: func_rootpath+"/iframe/feequery/billDetailQuery.action",
//            data: {
//                requestFlag: "synchronization",
//                billDetailType:"1",
//                qryMonth:month,
//                startTime:"1",
//                accNum:$("#qryAccNo").html(),
//                endTime:"30",
//                billPage:pageNum
//            },
//            dataType: "html",
//            success: function(page){
//                var s="";
//                $("<div>").append($(page)).find(".ued-table tbody tr:gt(0):not(.trlast)").each(function(i){
//                    log("class name:" + $(this).class);
//                    if($(this).class) {
//                        return true;
//                    }
//                    $(this).children("td").each(function(i){
//                        s+="\t"+$(this).text();
//                    }); s+="\n"
//                });
//                session.get(month,function(content){
//                    if(pageNum <= 1) {
//                        content=s;
//                    } else {
//                        content+=s;
//                    }
//                    session.set(month,content);
//                    if($("<div>").append($(page)).find('a:contains("下一页")').length>=1){
//                        loadData(month,pageNum +1);
//                    } else {
//                        log(month+"\n"+content);
//                    }
//                })
//            },
//            error:function(){
//                alert("对不起，详单记录查询失败，请稍后重试！");
//            }
//        });
//    }

//    if(location.href.indexOf("http://www.189.cn/dqmh/my189/initMy189home.do")!=-1) {
//        waitDomAvailable("a:contains('我的详单')", function(dom,timeSpan) {
//            log("wait 我的详单 success");
//            var detailPath=$('a:contains("我的详单")').attr("href");
//            var arrDetailPath = detailPath.split(/[,']/);
//            var fastcode="01390638";
//            if (arrDetailPath.length > 5) {
//                var url;
//                var redirectUrlSb = url = arrDetailPath[1];
//                fastcode = arrDetailPath[4];
//                // 定义数组，获取连接
//                var arrRedirectUrl = url.split("toStUrl=");
//                // 包含单点登录
//                if (arrRedirectUrl.length > 1) {
//                    // 包含问号
//                    if (arrRedirectUrl[1].indexOf("?") > -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
//                        // 包含问号传参用&符号拼接
//                        redirectUrlSb = url + "&fastcode=" + fastcode;
//                    } else if (arrRedirectUrl[1].indexOf("?") <= -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
//                        // 不包含问号，直接拼接问号
//                        redirectUrlSb = url + "?fastcode=" + fastcode;
//                    }
//                } else {
//                    // 包含问号
//                    if (arrRedirectUrl[0].indexOf("?") > -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
//                        // 包含问号传参用&符号拼接
//                        redirectUrlSb = url + "&fastcode=" + fastcode;
//
//                    } else if (arrRedirectUrl[0].indexOf("?") <= -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
//                        // 不包含问号，直接拼接问号
//                        redirectUrlSb = url + "?fastcode=" + fastcode;
//                    }
//                }
//                // 4g的场合拼接CityCode
//                if ("4g" == $("#menuType").val()) {
//                    redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val() + "&type=4g";
//                }else{
//                    redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val();
//                }
//
//                // location.href="http://www.189.cn/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action?fastcode=01390638&cityCode=bj"
//                location.href="http://www.189.cn"+redirectUrlSb;
//                log("src:" + redirectUrlSb);
//            } else if ("4g" == $("#menuType").val()) {
//                location.href = "http://www.189.cn/dqmh/my189/initMy189home4g.do?fastcode="
//                    + fastcode;
//                    log("4g src:" + redirectUrlSb);
//            } else {
//                location.href = "http://www.189.cn/dqmh/my189/initMy189home.do?fastcode="
//                    + fastcode;
//                 log("not 4g src:" + redirectUrlSb);
//}
//        },function() {
//            log("wait fail");
//        });
//    }


    var count = 0;
    var results = [];

    if(location.href.indexOf("http://www.189.cn/dqmh/my189/initMy189home.do")!=-1) {
//            session.showProgress(true);
//            session.setProgressMax(100);
//            session.setProgress(0);
//            session.hideLoading();

        log("waiting 我的详单");
        waitDomAvailable("a:contains('详单')", function(dom,timeSpan) {
            log("wait 我的详单 success");
            var detailPath=$('a:contains("详单")').attr("href");
            var arrDetailPath = detailPath.split(/[,']/);
            var fastcode="01390638";
            if (arrDetailPath.length > 5) {
                var url;
                var redirectUrlSb = url = arrDetailPath[1];
                fastcode = arrDetailPath[4];
                // 定义数组，获取连接
                var arrRedirectUrl = url.split("toStUrl=");
                // 包含单点登录
                if (arrRedirectUrl.length > 1) {
                    // 包含问号
                    if (arrRedirectUrl[1].indexOf("?") > -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
                        // 包含问号传参用&符号拼接
                        redirectUrlSb = url + "&fastcode=" + fastcode;
                    } else if (arrRedirectUrl[1].indexOf("?") <= -1 && arrRedirectUrl[1].indexOf("fastcode") <= -1) {
                        // 不包含问号，直接拼接问号
                        redirectUrlSb = url + "?fastcode=" + fastcode;
                    }
                } else {
                    // 包含问号
                    if (arrRedirectUrl[0].indexOf("?") > -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
                        // 包含问号传参用&符号拼接
                        redirectUrlSb = url + "&fastcode=" + fastcode;

                    } else if (arrRedirectUrl[0].indexOf("?") <= -1 && arrRedirectUrl[0].indexOf("fastcode") <= -1) {
                        // 不包含问号，直接拼接问号
                        redirectUrlSb = url + "?fastcode=" + fastcode;
                    }
                }
                // 4g的场合拼接CityCode
                if ("4g" == $("#menuType").val()) {
                    redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val() + "&type=4g";
                }else{
                    redirectUrlSb = redirectUrlSb + "&cityCode=" + $("#cityCode").val();
                }

                // location.href="http://www.189.cn/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action?fastcode=01390638&cityCode=bj"
                location.href="http://www.189.cn"+redirectUrlSb;
                log("src:" + redirectUrlSb);
            } else if ("4g" == $("#menuType").val()) {
                location.href = "http://www.189.cn/dqmh/my189/initMy189home4g.do?fastcode="
                    + fastcode;
            } else {
                location.href = "http://www.189.cn/dqmh/my189/initMy189home.do?fastcode="
                    + fastcode;
            }
        },function() {
            log("wait fail");
        });
    } else if(location.href.indexOf("189.cn/iframe/feequery/detailBillIndex.action")!=-1) {
        waitDomAvailable("#smsRandCode", function(dom,timeSpan) {
            log("wait success");
//            session.setProgress(10);

//            var userDetailLst = $('#userDetailLst');//the element I want to monitor
//            userDetailLst.bind('DOMNodeInserted', function(e) {
//                if($(".ued-table tbody").length>=1) {
//                    userDetailLst.unbind('DOMNodeInserted');
//
//                    var now = new Date();
//
//                     var year;
//                     var month;
//                     var endDate;
//                     var billDate;
//                     for(var i=now.getMonth()+1;i>=now.getMonth()-5;i--){
//                         if(i==now.getMonth()+1) {
//                             year=now.getFullYear();
//                             month=i;
//                             endDate=now.getDate();
//                         } else {
//                             billDate = new Date(now.getFullYear(),i,0);
//                             year=billDate.getFullYear();
//                             month=billDate.getMonth()+1;
//                             endDate=billDate.getDate();
//                         }
//                         if(month < 10){
//                             month = "0" + month;
//                         }
//                         console.log(year + "年" + month + "月" + "|" + endDate);
//                         loadData(year + "年" + month + "月", endDate, 1)
//                     }
//                }
//
//            });
//            getRandombySms();
//            sendRandombySms();

            waitVarAvailable(func_rootpath, function (timeSpan) {
                log("wait func_rootpath success");
                session.setProgress(16);
                var now = new Date();

                 var year;
                 var month;
                 var endDate;
                 var billDate;
//                 var progressMap = new Map();
                 for(var i=now.getMonth()+1;i>=now.getMonth()-5;i--){
                     if(i==now.getMonth()+1) {
                         year=now.getFullYear();
                         month=i;
                         endDate=now.getDate();
                     } else {
                         billDate = new Date(now.getFullYear(),i,0);
                         year=billDate.getFullYear();
                         month=billDate.getMonth()+1;
                         endDate=billDate.getDate();
                     }
                     if(month < 10){
                         month = "0" + month;
                     }
                     var result = new Result();
                     result.month = year + "年" + month + "月";
                     result.list = [];
                     results.push(result);
//                     progressMap.put(month, 0);
                     console.log(year + "年" + month + "月" + "|" + endDate);
                     loadData(year + "年" + month + "月", endDate, 1)
                 }
            }, function () {
                log("wait func_rootpath fail")
            });
        },function() {
            log("wait fail");
            log($('#smsRandCode').val());
        });
    }

    function getRandombySms() {
        $.ajax({
            type: "POST",
            url: "/iframe/feequery/smsRandCodeSend.action",
            cache: false,
            data: {accNum:$("#qryAccNo").html()},
            dataType: "json",
            success: function (json) {

                if (json.tip == null || json.tip == "") {
                    log("sms:"+json.SRandomCode);
                    //成功后,要隐藏,还原
                    //                        timer_count = setInterval(countNum, 1000);
                    //                        alert("随机码短信发送成功，请查收。");
                } else {//失败了提示和计时器隐藏,链接显示
                    alert(json.tip);
                }
            },
            error: function (json) {
                alert("对不起，随机码短信发送失败！请稍后重试。");
            }
        });
    }


    function loadData(month, endTime, pageNum) {
        $.ajax({
            type: "POST",
            url: func_rootpath+"/iframe/feequery/billDetailQuery.action",
            data: {
                requestFlag: "synchronization",
                billDetailType:"1",
                qryMonth:month,
                startTime:"1",
                accNum:$("#qryAccNo").html(),
                endTime:endTime,
                billPage:pageNum
            },
            dataType: "html",
            success: function(page){

                for(var j = 0; j < results.length; ++j) {
                    if(results[j].month == month) {
//                        var s="";
                        $("<div>").append($(page)).find(".ued-table tbody tr:gt(0):not(.trlast)").each(function(k){
                            log("class name:" + $(this).class);
                            if($(this).class) {
                                return true;
                            }
                            var record = new Record();
                            $(this).children("td").each(function(i){
                                switch(i) {
                                    case 1:
                                        record.callType = $(this).text();
                                        break;
                                    case 2:
                                        record.callingType = $(this).text();
                                        break;
                                    case 3:
                                        record.callLoc = $(this).text();
                                        break;
                                    case 4:
                                        record.phone = $(this).text();
                                        break;
                                    case 5:
                                        record.begin = $(this).text();
                                        break;
                                    case 6:
                                        record.baseCost = $(this).text();
                                        break;
                                    case 7:
                                        record.longCost = $(this).text();
                                        break;
                                    case 8:
                                        record.duration = $(this).text();
                                        break;
                                    case 9:
                                        record.totalCost = $(this).text();
                                        break;
                                }
                                if(i == 1) {
                                    log("i==1:" + $(this).text());
                                }

//                                s+="\t"+$(this).text();
                            });
                            results[j].list.push(record);
//                            s+="\n"
                        });
                        content = session.get(month);
                        if(pageNum <= 1) {
                            content=s;
                        } else {
                            content+=s;
                        }
                        session.set(month,content);
                        if($("<div>").append($(page)).find('a:contains("下一页")').length>=1){
                            loadData(month,endTime,pageNum +1);
                        } else {

                            ++count;
                            log(content);
                            if(count >= 7) {
                                session.upload(results);
                                session.finish();
                            }
                        }
                    }
                }

//                var progress = progressMap.get(month);
//                progress = progress+2;
//                if(progress > 12) {
//                    progress = 12;
//                }
//                progressMap.put(month, progress);

            },
            error:function(){
                alert("对不起，详单记录查询失败，请稍后重试！");
            }
        });
    }


    function waitVarAvailable(variable, success, fail) {
        var timeout = 20000;
        var t = setInterval(function () {
            timeout -= 100;
            log("timeout" + timeout);
            if(variable != undefined) {
                clearInterval(t)
                success(10000 - timeout)
            } else if (timeout == 0) {
                clearInterval(t)
                var f = fail || DomNotFindReport;
                f(selector)
            }
        }, 100);
    }



//    $("#bodyIframe").attr("src", "/dqmh/ssoLink.do?method=linkTo&platNo=10001&toStUrl=http://bj.189.cn/iframe/feequery/detailBillIndex.action?fastcode=01390638&cityCode=" + $("#cityCode").val());
////$("#bodyIframe").load(function() {
////log("Local iframe is now loaded.");
//////         sendRandombySms();
////});
//
//    function waitDomAvailable(success, fail) {
//        var timeout = 200000;
//        var t = setInterval(function () {
//            timeout -= 10;
//            log(timeout);
//            if ($("#smsRandCode").length>=1) {
//                clearInterval(t)
//                success(ob, 10000 - timeout)
//            } else if (timeout == 0) {
//                clearInterval(t)
//                var f = fail || DomNotFindReport;
//                f()
//            }
//        }, 10);
//    }
//
//    $('#bodyIframe').on('load', function(){
//        console.log('load the iframe');
//        log($('#smsRandCode'));
//        waitDomAvailable(function(dom,timeSpan) {
//            log("wait success");
//            var  options = {
//                'attributes':true
//            } ;
//            var target = document.getElementById("smsRandCode");
//            Observe(target, options, function(mutations) {
//                log($("#smsRandCode").val());
//            });
//
//
//            $.ajax({
//                type : "POST",
//                url : "/iframe/feequery/smsRandCodeSend.action",
//                cache : false,
//                data: {accNum:$("#qryAccNo").html()},
//                dataType : "json",
//                success : function(json) {
//                    log(json)
//                    if (json.tip == null || json.tip == "") {
//                        //成功后,要隐藏,还原
//                        timer_count = setInterval(countNum, 1000);
//                        alert("随机码短信发送成功，请查收。");
//                    } else {//失败了提示和计时器隐藏,链接显示
//                        alert(json.tip);
//                    }
//                },
//                error : function(json) {
//                    log(json);
//                    alert("对不起，随机码短信发送失败！请稍后重试。");
//                }
//            });
//        },function() {
//            log("wait fail");
//            log($('#smsRandCode').val());
//        });
//    });

function Result() {
    var month;
    var list;
}

function Record() {
    var callType;
    var callingType;
    var callLoc;
    var phone;
    var begin;
    var baseCost;
    var longCost;
    var duration;
    var totalCost;
}


})