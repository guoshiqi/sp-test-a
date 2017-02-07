
dSpider("jd", function(session,env,$){

    var infokey = "infokey";
    var sid = "";
    var max_order_num = 30;
    var max_order_date = 1000;
    var globalInfo;

    sid = session.get("sid");

    if (location.href.indexOf("://m.jd.com") !== -1 ) {
        session.showProgress(true);
        session.setProgressMax(100);
        session.autoLoadImg(false);
        session.setProgress(5);

        if($(".jd-search-form-input")[0] !== undefined){
            sid  = $(".jd-search-form-input")[0].children[0].value;
            session.set("sid",  sid);
         }

        session.set(infokey, new info({},{},{}));
        globalInfo = session.get(infokey);
        globalInfo.base_info.username  = $("[report-eventid$='MCommonHTail_Account']").text().replace(/\n/g,"").replace(/\t/g,"");
        saveInfo();
        session.setProgress(10);
        location.href="http://home.m.jd.com/maddress/address.action?";
    }

    if (location.href.indexOf("://home.m.jd.com/maddress") != -1) {
        session.setProgress(20);

        globalInfo = session.get(infokey);

        global_contact_info = new contact_info([]);
        var taskAddr = [];
        var urlarray = $(".ia-r");
        for(var i=0;i<urlarray.length;i++){
                                    taskAddr.push($.get(urlarray[i],function(response,status){
                                    var node = $("<div>").append($(response));
                                    var name = $.trim(node.find("#uersNameId")[0].value);
                                    var phone = $.trim(node.find("#mobilePhoneId")[0].value);
                                    var addr = $.trim(node.find("#addressLabelId")[0].innerHTML);
                                    var detail = $.trim(node.find("#address_where")[0].innerHTML);

                                    global_contact_info.contact_detail.push(new contact(name,addr,detail,phone, ""));
                                    }) );

            }


          $.when.apply($,taskAddr).done(
       // $.when(taskAddr).done(
                  function(){
                        globalInfo.contact_info = global_contact_info;
                        saveInfo();
                        session.setProgress(30);
                        getOrder();
                        });


    }


    function getOrder(){
        session.setProgress(40);
        globalInfo = session.get(infokey);
        var orders = new order_info([]);
        globalInfo.order_info = new order_info([]);
        globalInfo.order_info.order_detail = [];
        function getPageOrder(page){
           $.getJSON("https://home.m.jd.com//newAllOrders/newAllOrders.json?sid="+sid+"&page="+page,function(d){
               page++;
               if( globalInfo.order_info.order_detail.length <=  max_order_num && d.orderList.length!==0 && (orders.order_detail.length === 0 || d.orderList[d.orderList.length-1].orderId !== orders.order_detail[orders.order_detail.length-1].orderId) ){
                   orders.order_detail = orders.order_detail.concat(d.orderList);
                   var task = [];
                   if(globalInfo.order_info.order_detail.length < max_order_num){
                        if(d.orderList.length + globalInfo.order_info.order_detail.length > max_order_num){
                           d.orderList = d.orderList.slice(0, max_order_num -  globalInfo.order_info.order_detail.length);
                        }
                        task.push($.each(d.orderList,function(i,e){
                                            log("task push orderId: " + d.orderList[i].orderId);

//                                           $.get("https://home.m.jd.com/newAllOrders/queryOrderDetailInfo.action?orderId="+ d.orderList[i].orderId+"&from=newUserAllOrderList&passKey="+d.passKeyList[i]+"&sid="+sid,
//                                                   function(response,status){
//                                                        log("orderId: " + d.orderList[i].orderId);
//                                                        var addr = $("<div>").append($(response)).find(".step2-in-con").text();
//                                                        var orderitem = new order(d.orderList[i].orderId,d.orderList[i].dataSubmit,d.orderList[i].price,addr);
//
//                                                        orderitem.products = [];
//                                                        var products = $("<div>").append($(response)).find(".pdiv");
//                                                        $.each(products,function(k, e){
//                                                                                                       var name = $("<div>").append(products[k]).find(".sitem-m-txt").text();
//                                                                                                       var price = $("<div>").append(products[k]).find(".sitem-r").text();
//                                                                                                       var num = $("<div>").append(products[k]).find(".s3-num").text();
//                                                                                                       orderitem.products.push(new product(name,  num ,price));
//                                                         });
//                                                         if(Date.parse(new Date()) < ((new Date(orderitem.time.split(" ")[0])).getTime() + max_order_date * 24 * 60 * 60 * 1000)){
//                                                              if(globalInfo.order_info.order_detail.length < max_order_num){
//                                                                   globalInfo.order_info.order_detail.push(orderitem);
//                                                              }
//                                                            }
//                                                        });
                                            $.ajax({
                                                      type : "get",
                                                      url : "https://home.m.jd.com/newAllOrders/queryOrderDetailInfo.action?orderId="+ d.orderList[i].orderId+"&from=newUserAllOrderList&passKey="+d.passKeyList[i]+"&sid="+sid,
                                                      async : false,
                                                      success : function(response){
                                                        log("orderId: " + d.orderList[i].orderId);
                                                         var addr = $.trim($("<div>").append($(response)).find(".step2-in-con").text());
                                                         var orderitem = new order(d.orderList[i].orderId,d.orderList[i].dataSubmit,d.orderList[i].price,addr);

                                                         orderitem.products = [];
                                                         var products = $("<div>").append($(response)).find(".pdiv");
                                                         $.each(products,function(k, e){
                                                                var name = $.trim($("<div>").append(products[k]).find(".sitem-m-txt").text());
                                                                var price = $.trim($("<div>").append(products[k]).find(".sitem-r").text());
                                                                var num = $.trim($("<div>").append(products[k]).find(".s3-num").text());
                                                                orderitem.products.push(new product(name,  num ,price));
                                                          });
                                                          if(Date.parse(new Date()) < ((new Date(orderitem.time.split(" ")[0])).getTime() + max_order_date * 24 * 60 * 60 * 1000)){
                                                              if(globalInfo.order_info.order_detail.length < max_order_num){
                                                                   globalInfo.order_info.order_detail.push(orderitem);
                                                              }
                                                          }
                                                      }
                                                      });
                                         }));
                    }


                      $.when(task).done(function(){
                           log("get page :" + page);
                           log("count: " +globalInfo.order_info.order_detail.length );
                           getPageOrder(page);
                           globalInfo.order_info.order_detail.sort(compare());
                      });

               }else {
                  log("finish");
                  saveInfo();
                  session.setProgress(60);
                  getUserInfo();
                  return;
               }
           });
       }
       getPageOrder(1);
    }

    function compare(){
        return function(a,b){
            var value1 = (new Date(a.time.split(" ")[0])).getTime();
            var value2 = (new Date(b.time.split(" ")[0])).getTime();
            return value2 - value1;
    };
    }

    function getUserInfo(){
           location.href = "http://home.m.jd.com/user/accountCenter.action";
    }
    if (location.href.indexOf("://home.m.jd.com/user/accountCenter.action") !== -1 && location.href.indexOf("loginpage") == -1) {
        session.setProgress(70);
        if($('#shimingrenzheng')[0] !== undefined){
           $('#shimingrenzheng')[0].click();
        }
    }

    //已实名用户
    if (location.href.indexOf("msc.jd.com/auth/loginpage/wcoo/toAuthInfoPage") !== -1) {
        session.setProgress(90);
        globalInfo = session.get(infokey);
        if( $(".pos-ab")[0] !== undefined){
            globalInfo.base_info.name  = $(".pos-ab")[0].innerHTML;
        }
        if($(".pos-ab")[1] !== undefined){
            globalInfo.base_info.idcard_no  = $(".pos-ab")[1].innerHTML;
        }
        saveInfo();
        logout();


    }

    function logout(){

        //alert("爬取订单总计:" + session.get(infokey).order_info.order_detail.length);
        //location.href = "https://passport.m.jd.com/user/logout.action?sid="+session.get("sid");
        session.setProgress(100);
        session.upload(session.get(infokey));
        session.finish();
    }
    //快捷卡实名用户
    if (location.href.indexOf("msc.jd.com/auth/loginpage/wcoo/toAuthPage") != -1 ) {
        session.setProgress(90);
        globalInfo = session.get(infokey);
        if($("#username")[0] !==undefined){
            globalInfo.base_info.name  = $("#username")[0].innerHTML;
        }
        if($(".info-user-name")[0] !==undefined){
                    globalInfo.base_info.name  = $(".info-user-name")[0].innerHTML;
        }
        if($("#idcard")[0] !==undefined){
            globalInfo.base_info.idcard_no  = $("#idcard")[0].innerHTML;
        }
        if($(".pos-ab[data-cardno]") !==undefined){
                    globalInfo.base_info.idcard_no  = $(".pos-ab[data-cardno]").attr("data-cardno");
        }

        saveInfo();
        logout();
    }


    if (location.href.indexOf("https://plogin.m.jd.com/user/login.action?appid=100") != -1 ) {
            var n = !0;
            log("xxxxxxxx " + session.getLocal("username"));
             $("#username").val(session.getLocal("username"));
             $("#password").val(session.getLocal("password"));
             $("#loginBtn").on("click",
                    function() {
                        return !! $(this).hasClass("btn-active") && void o()
                    })

            function refreshAuth() {
                var t = "/cgi-bin/m/authcode?mod=login&v=" + Math.random();
                $("#imgCode").attr("src", t)
            }
             function a() {
                        var t = {};
                        t.username = $("#username").val(),
                        t.pwd = $("#password").val();
                        var e = str_rsaString;
                        setMaxDigits(131);
                        var i = new RSAKeyPair("3", "10001", e, 1024),
                        o = window.btoa(encryptedString(i, t.pwd, RSAAPP.PKCS1Padding, RSAAPP.RawEncoding));
                        t.pwd = o,
                        t.remember = n,
                        t.s_token = str_kenString;
                        try {
                            t.dat = getDat(t.username, t.pwd)
                        } catch(a) {}
                        return _need_ck && (t.authcode = $("#code").val()),
                        t
                    }

            function t(t) {
                        $(".pop-msg").html(t.msg),
                        $(".btn-continue").attr("href", t.url).html(t.btn),
                        $(".pop-dialog").show()
            }
            function e() {
                $(".txt-input").on("input",
                function() {
                    $(this).siblings("i").show(),
                    i() ? $("#loginBtn").addClass("btn-active") : $("#loginBtn").removeClass("btn-active")
                })
            }
            function i() {
                var t = !0;
                return $(".txt-input").each(function() {
                            if (!$(this).val()) return t = !1,
                            !1
                        }),
                        t
                    }

            function o() {
                    $(".input-container").removeClass("input-error"),
                    $("#loginBtn").addClass("btn-active-disable").html("登录中"),
                    $(".notice").html(" ");

                    var n = a();
                    log("set local xxxx" + $("#username").val());
                    session.setLocal("password", $("#password").val());
                    session.setLocal("username", $("#username").val());
                    log("xxxxxxxx ins get  " + session.getLocal("username"));


                    n.risk_jd = "";
                    try {
                        n.risk_jd = getJdEid()
                    } catch(i) {}
                    var o = (new Date).getTime();
                    $.ajax({
                        url: "/cgi-bin/m/domlogin",
                        type: "POST",
                        data: n,
                        dataType: "json",
                        success: function(i) {
                            $("#loginBtn").removeClass("btn-active-disable").html("登录");
                            var a = (new Date).getTime() - o;
                            if (window.pl_report({
                                interfaceID: 393217,
                                loginName: n.username,
                                callTime: a,
                                status: i.errcode
                            }), 0 == i.errcode) i.hk_autologin ? $.ajax({
                                url: i.hk_url,
                                type: "get",
                                dataType: "jsonp",
                                timeout: 15e3,
                                complete: function() {
                                    setTimeout(function() {
                                        location.href = i.succcb
                                    },
                                    200)
                                }
                            }) : setTimeout(function() {
                                location.href = i.succcb
                            },
                            200);
                            else switch (i.needauth ? ($("#loginBtn").removeClass("btn-active"), $("#input-code").show().find("input").addClass("txt-input"), _need_ck = !0, refreshAuth(), e()) : ($("#input-code").hide().find("input").removeClass("txt-input"), _need_ck = !1, e()), i.errcode) {
                            case 6:
                                $("#password").parent().addClass("input-error"),
                                $(".notice").html(i.message);
                                break;
                            case 7:
                                $("#username").parent().addClass("input-error"),
                                $(".notice").html(i.message);
                                break;
                            case 257:
                                $("#code").val("").parent().addClass("input-error"),
                                $(".notice").html(i.message);
                                break;
                            case 128:
                            case 129:
                            case 130:
                            case 131:
                            case 132:
                            case 133:
                            case 134:
                            case 135:
                            case 136:
                            case 137:
                            case 138:
                            case 139:
                            case 140:
                            case 141:
                            case 142:
                            case 143:
                                t({
                                    msg:
                                    i.message,
                                    btn: "确定",
                                    url: i.succcb
                                });
                                break;
                            case 100:
                                t({
                                    msg:
                                    "您的账号存在安全风险，请前往电脑版京东验证账号，有问题请致电京东客服4006065500",
                                    btn: "确定",
                                    url: "tel:4006065500"
                                });
                                break;
                            case 103:
                                t({
                                    msg:
                                    i.message,
                                    btn: "找回密码",
                                    url: "https://passport.m.jd.com/findloginpassword/fillAccountName.action"
                                });
                                break;
                            case 105:
                                location.href = i.succcb;
                                break;
                            default:
                                $(".notice").html(i.message)
                            }
                        },
                        error: function() {
                            $(".notice").html("服务器开小差，请稍后重试"),
                            $("#loginBtn").removeClass("btn-active-disable").html("登录");
                            var t = (new Date).getTime() - o;
                            window.pl_report({
                                interfaceID: 393217,
                                loginName: n.username,
                                callTime: t,
                                status: "255"
                            })
                        }
                    })
                }

    }


    function saveInfo(){
        session.set(infokey, globalInfo);
    }



    function addr(name,phone,addrdetail) {
        this.name = name;
        this.phone = phone;
        this.addrdetail = addrdetail;
    }

    var address = [];
    var global_contact_info;


    function info(base_info,contact_info,order_info ){
        this.site_id = 2;
    　　 this.base_info = base_info;
    　　 this.contact_info = contact_info;
    　　 this.order_info  = order_info;
    }

    function base_info(username, name, idcard_no, phone){
        this.username = username;
        this.name = name;
        this.idcard_no = idcard_no;
        this.phone = phone;
    }


    function contact_info(contact_detail){
        this.contact_detail = contact_detail;
    }

    function contact(name, location ,address, phone, zipcode){
        this.name  = name;
        this.location  = location;
        this.address  = address;
        this.phone  = phone;
        this.zipcode  = zipcode;
    }

    function order_info(order_detail){
        this.order_detail  = order_detail;
    }

    function order(id, time , total, address){
        this.id  = id;
        this.time  = time;
        this.total  = total;
        this.address  = address;
    }

    function product(name, number, price){
        this.name  = name;
        this.number  = number;
        this.price  = price;
    }

    // 增加判断当前页面是否是登录页  modify by renxin 2017.1.17
    if ($("#loginOneStep").length && $("#loginOneStep").length > 0) {
      session.setStartUrl();
    }


//end
});