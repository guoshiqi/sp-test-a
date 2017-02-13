dSpider("unicom", 60*5, function(session,env,$){

    function parseThxd(msg) {
        $(msg).find("tr.tips_dial").each(function() {
            var data = {}
            data["otherNo"] = $(this).find("label.telphone").text();
            data["callTime"] = $(this).find("td:eq(1) p").first().text();
            data["callFee"] = $(this).find("p.time:eq(0)").text().replace(/[\n|\s]/g, "").replace();
            data["callBeginTime"] = $(this).find("p.time:eq(1)").text().replace(/[\n|\s]/g, "").replace();
            data["callType"] = ($(this).find(".call_out").length == 1) ? "主叫" : "被叫";

            var monthData = session.get("curMonthData");
            var datas = monthData["data"];
            if(!datas) {
                datas = [];
            }
            datas.push(data);
            monthData["data"] = datas;
            session.set("curMonthData", monthData);
            log("获取一条数据：" + JSON.stringify(data));
        })
    }

    function getThxdByAjax(year, month) {
        log("开始爬取【" + month + "】月份通话详单：");
        //session.upload("开始爬取【" + month + "】月份通话详单：")
        var curMonthData = {};
        curMonthData["calldate"] = year + "" + month;
        curMonthData["cid"] = parseInt(new Date().getTime()/1000).toString();
        session.set("curMonthData", curMonthData);
        $.ajax({
            url: '/mobileService/query/getPhoneByDetailContent.htm',
            type: 'post',
            data: 't=' + new Date().getTime() + '&YYYY=' + year + '&MM=' + month + '&DD=&queryMonthAndDay=month&menuId=',
            //dataType: 'html',
            //async: true,
            //cache: false,
            success: function(msg) {
                log("请求" + month + "月份数据成功....");
                parseThxd(msg);
                $('#pageNew').html(msg);
                //保存数据
                var data = session.get("curMonthData");
                data["totalCount"] = window.totalrow;
                data["status"] = 4;
                session.set("curMonthData", data);

                //是否需要加载更多
                if (window.endrow < window.totalrow) {
                    loadMore();
                } else {
                    var thxd = session.get("thxd");
                    if(!thxd["month_status"]) {
                        thxd["month_status"] = [];
                    }
                    thxd["month_status"].push(data);
                    session.set("thxd", thxd);
                    session.set("curMonthData", "");
                    log("爬取数据完成..." + JSON.stringify(data));
                    spide();
                }
            },
            error: function(xhr, msg){
                //保存数据
                var data = session.get("curMonthData");
                data["totalCount"] = 0;
                data["status"] = 2;
                data["data"] = [];
                var thxd = session.get("thxd");
                if(!thxd["month_status"]) {
                    thxd["month_status"] = [];
                }
                thxd["month_status"].push(data);
                session.set("thxd", thxd);
                session.set("curMonthData", "");

                log("爬取【" + month + "】月份通话详单失败！");
                spide();
            }
        });
    }

    function loadMore() {
        log("加载更多...")
        var _this = $(this);
        var beginrow = window.endrow;
        window.endrow = window.beginrow + window.perrow;
        if (window.endrow > window.totalrow) {
            window.endrow = window.totalrow;
        }
        _this.html('<img src="http://img.client.10010.com/mobileService/view/client/images/loading.gif" width="16">');
        var href = '/mobileService/view/client/query/xdcx/thxd_more_list.jsp?1=1&t=' + window.getrandom();
        var params = '&beginrow=' + beginrow + '&endrow=' + window.endrow + '&pagenum=' + (window.pagenum + 1);
        $('.moredetail' + window.pagenum).load(href + params, function(msg) {
            parseThxd(msg);
            var thxd = session.get("thxd");
            if(!thxd["month_status"]) {
                thxd["month_status"] = [];
            }
            thxd["month_status"].push(session.get("curMonthData"));
            session.set("thxd", thxd);
            session.set("curMonthData", "");
            //继续爬取
            spide();
        });
    }

    function spide() {
        var monthArr = session.get("months");
        if (monthArr && monthArr.length > 0) {
            session.setProgress(session.get("max") - monthArr.length - 1);
            var monthObj = monthArr.shift();
            session.set("months", monthArr);
            getThxdByAjax(monthObj.year, monthObj.month);
        } else {
            var thxd = session.get("thxd");
            log("爬取通话详单完毕----------" + JSON.stringify(thxd));
            //跳转到服务首页
            window.location.href = "http://wap.10010.com/mobileService/siteMap.htm";
        }
    }

    function endSpide(thxd) {
        log("爬取完毕----------" + JSON.stringify(thxd));
        session.upload(JSON.stringify(thxd));
        session.setProgress(session.get("max") - 0);
        session.finish();
        session.showProgress(false);
    }

    if(window.location.href.indexOf("/uac.10010.com/oauth2/new_auth") != -1) {
        var footer = $("div.footer:eq(0)");
        if (footer) {
            footer.css("visibility", "hidden");
        }
        var header = $("div.header");
        if(header) {
            header.css("display", "none");
        }
        var passFind = $("div.interval2:eq(0)");
        if(passFind) {
            passFind.css("visibility", "hidden");
        }
        var loginBtn = $("a#login1:eq(0)");
        if(loginBtn) {
            loginBtn.click(function(){
                var unameInput = $("input#userName:eq(0)");
                if(unameInput) {
                    var userName = unameInput.val();
                    session.setLocal("userName", userName);
                }
                var passInput = $("input#userPwd:eq(0)");
                if(passInput) {
                    var pass = passInput.val();
                    session.setLocal("password", pass);
                }
            });
        }

        $("input#userName:eq(0)").val(session.getLocal("userName"));
        $("input#userPwd:eq(0)").val(session.getLocal("password"));

        session.setStartUrl();
        session.showProgress(false);
    } if(window.location.href.indexOf('query/getPhoneByDetailTip.htm') != -1){
        //显示loading
        session.showProgress();

        //计算月份信息
        //var date = new Date();
        //var curMonth = date.getMonth() + 1;
        //var curYear = date.getFullYear();
        var curMonth, curYear;

        var ul = $(".month_list");
        if (ul) {
        	if (ul.has("li")) {
        		var li = ul.find("li.active:eq(0)");
        		var month = li.find("p:eq(0)").text();
        		month = month.replace("月", "");
        		month = month.trim();
        		try {
        			curMonth = parseInt(month);
        		} catch (e) {
       				log("月份获取失败...");
        		}
        		var year = li.find("p:eq(1)").text();
        		year = year.trim();
        		try {
        			curYear = parseInt(year);
        		} catch (e) {
       				log("年获取失败...");
        		}
        	};
        };
        var monthArr = []
        var max = 0;
        if (curMonth && curYear) {
        	for (var i = 0; i < 6; i++) {
	            var month = curMonth - i;
	            var year = curYear;
	            if (month < 1) {
	                month += 12;
	                year -= 1;
	            }
	            if (month < 10) {
	                month = "0" + month;
	            }
	            monthArr.push({ "year": year, "month": month });
	        }
	        max = monthArr.length + 1;
	        log("开始爬取....." + JSON.stringify(monthArr));
        } else {
        	log("没有通话时间，可能刚开卡..");
        }
        //设置月份信息
        session.set("months", monthArr);
        session.set("max", max);
        session.setProgressMax(max);
        var thxd = session.get("thxd");
        if (!thxd) {
            session.set("thxd", {});
        };

        spide();
    } else if(window.location.href.indexOf('mobileService/siteMap.htm') != -1){//服务界面，获取个人信息跳转
        var infoTag = "";
        $(".checklistcontainer.newmore").find("li").each(function(){
            if($(this).html().indexOf("基本信息") != -1) {
                infoTag = $(this);
                return;
            }
        });
        if(infoTag) {
            //跳转到我的基本信息页面
            window.location.href = infoTag.attr("name");
        } else {
            log("用户信息获取失败.....");
            var thxd = session.get("thxd");
            thxd["user_info"] = {};
            endSpide(thxd);
        }
    } else if(window.location.href.indexOf("t/operationservice/getUserinfo.htm") !=-1 ) {//获取个人信息
        log("开始爬取用户信息----------");
        var userInfo = {};
        try {
            userInfo["mobile"] = $(".clientInfo4_top").find("p:eq(0)").html().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        try{
            userInfo["name"] = $(".clientInfo4_list").find("li:eq(0)").find("span:eq(1)").html().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        try{
            userInfo["taocan"] = $(".clientInfo4_list").find("li:eq(1)").find("span:eq(1)").html().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        try{
            userInfo["registration_time"] = $(".detail_con.con_ft:eq(0)").find("p:eq(6)").find("span:eq(1)").text().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        try{
            userInfo["idcard_no"] = $(".detail_con.con_ft:eq(1)").find("p:eq(4)").find("span:eq(1)").text().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        try{
            userInfo["household_address"] = $(".detail_con.con_ft:eq(1)").find("p:eq(18)").find("span:eq(1)").text().replace(/[\n|\s]/g, "").replace();
        } catch (e) {
        }
        log("爬取用户信息结束-----" + JSON.stringify(userInfo));
        var thxd = session.get("thxd");
        thxd["user_info"] = userInfo;
        if (thxd["month_status"]) {
            var len = thxd["month_status"].length;
            if (len > 0) {
                for(var i = 0; i < len; i ++) {
                    thxd["month_status"][i]["mobile"] = userInfo["mobile"];
                }
            }
        }
        endSpide(thxd);
    }
})