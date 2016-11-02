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

   //place your code here!
if (window.location.pathname.indexOf("mlapp/mytaobao") != -1) {
    //taobaoState    0:爬账单  1:爬地址   2:爬个人信息   3:结束
    session.set("taobaoState",1);//调试爬取地址---------------调试
    session.get("taobaoState",function(count){
        if(count!=1&&count!=2&&count!=3){
            if(count == 0){

            }else{
                session.set("taobaoState",0);
                session.set("orderArray",[]);
            }
            document.getElementsByClassName("label-act")[0].children[0].children[0].click();//点击订单
        }else if(count==1){
            //跳转到网页版   www.taobao.com
            session.set("AddressData",[]);
            location.href="//www.taobao.com/index.php?from=wap"
        }
    });
}



//获取订单列表
if (window.location.pathname.indexOf("mlapp/olist") != -1) {
    session.get("OrderItemPosition",function(count){
        if(count == undefined){
            getOrderList();
        }else{
            intoOrderDetail(count);
        }
    });
}
function getOrderList(){
    var myInterval;
    //循环调用获取订单的方法
    function getOrder() {
        if (window.getComputedStyle($("div.order-more")[0], '::after').getPropertyValue('content')||$(".order-list>li").length>=5) {//限制订单爬取的数量
            log("加载完成");
            clearInterval(myInterval);
            //进入订单
            session.set("OrderItemPosition",0);
            intoOrderDetail(0);
        } else {
            //需要继续请求订单
            document.getElementsByClassName("order-more")[0].click();//点击加载更多
            log("正在获取订单");
        }
    }
    myInterval = setInterval(getOrder, 3000);
}
/**
 * 进入订单的爬取操作
 */
function intoOrderDetail(position) {
    if(position >= $(".order-list>li").length){
        //更新状态并退回到个人页
        session.set("taobaoState",1);
        setTimeout(location.url = history.go(-1),1000);
    }else{
        //进入订单详情页
        ($($(".order-list>li")[position]).children()[3].children[0].children[0]).click();
    }
}
/**
 * 爬取订单数据
 */
if (window.location.pathname.indexOf("mymovie/pages") != -1) {//特殊订单的处理
    session.get("orderArray",function(count){
            if(count == undefined){
                session.set("taobaoState",-1);
                //关闭当前页面
                closeOrderDetail();
            }else{
                currentOrderData = count;
                session.get("OrderItemPosition",function(count){//因为是异步获取position所以要在拿到position后开始爬取
                    session.set("OrderItemPosition",count+1);
                    setTimeout(location.url = history.go(-1),1000);
                });
            }
        });
}
if (window.location.pathname.indexOf("mlapp/odetail") != -1) {
    var currentPosition;
    var currentOrderData = [];
    function getOrderDetail() {
        //当前订单需要爬取的数据的对象
        var tbOrderDetailInfo = {};
        //存放多个商品的数组
        var totalProductArray = [];
        //创建订单详情的列表
        var orderInfoList = [];
        //拿到页面列表中所有的div
        var ol = $(".order-list>li>div");
        for (var i = 0; i < ol.length; i++) {
            if (ol[i].className.indexOf("orderinfo") != -1) {
                //订单编号
                for (var j = 0; j < ol[i].children[0].childElementCount; j++) {
                    var infoItem = $(ol[i].children[0].children[j]).text();
                    if (infoItem.indexOf("订单编号") != -1) {
                        //截取订单编号
                        var orderNamber = infoItem.substring(infoItem.indexOf(":") + 1, infoItem.length);
                        tbOrderDetailInfo.id = orderNamber;
                    }
                }
                //订单创建时间
                for (var j = 0; j < ol[i].children[0].childElementCount; j++) {
                    var infoItem = $(ol[i].children[0].children[j]).text();
                    if (infoItem.indexOf("创建时间") != -1) {
                        //截取订单创建时间
                        var orderCreatTime = infoItem.substring(infoItem.indexOf(":") + 1, infoItem.length);
                        tbOrderDetailInfo.time = orderCreatTime;
                    }
                }
            }
            //商品名称
            if (ol[i].className.indexOf("item") != -1) {//---所有商品的item
                var productInfo = ol[i].children;//产品信息下所有的布局
                for (var k = 0; k < productInfo.length; k++) {
                    //创建一个对象存放产品列表
                    var myProductInfo = {};
                    if (productInfo[k].className.indexOf("item-list") != -1) {
                        for (var m = 0; m < productInfo[k].children.length; m++) {
                            if (productInfo[k].children[m].className.indexOf("item-info") != -1) {//----item中产品信息
                                var order_product_info = productInfo[k].children[m];
                                for (var opi = 0; opi < order_product_info.children.length; opi++) {
                                    var opiChildren = order_product_info.children[opi];
                                    if (opiChildren.className.indexOf("title") != -1) {
                                        //商品名称
                                        myProductInfo.name = $(opiChildren).text();
                                    }
                                }
                            }
                            if (productInfo[k].children[m].className.indexOf("item-pay") != -1) {//----item中产品价格和数量
                                var item_pay = productInfo[k].children[m];
                                for (var ip = 0; ip < item_pay.children.length; ip++) {
                                    var ipChildren = item_pay.children[ip];
                                    if (ipChildren.className.indexOf("item-pay-data") != -1) {
                                        var priceInfoArray = ipChildren.children;
                                        for (var pia = 0; pia < priceInfoArray.length; pia++) {
                                            if (priceInfoArray[pia].className.indexOf("price") != -1) {
                                                //商品价格
                                                var productPrice = $(priceInfoArray[pia]).text();
                                                if (productPrice.indexOf("￥") != -1) {
                                                    productPrice = productPrice.substring(productPrice.indexOf("￥") + 1, productPrice.length)
                                                }
                                                myProductInfo.price = productPrice;
                                                break;
                                            }
                                        }
                                        for (var pia = 0; pia < priceInfoArray.length; pia++) {
                                            if (priceInfoArray[pia].className.indexOf("nums") != -1) {
                                                //商品数量
                                                var productNumber = $(priceInfoArray[pia]).text();
                                                if (productNumber.indexOf("x") != -1) {
                                                    productNumber = productNumber.substring(productNumber.indexOf("x") + 1, productNumber.length)
                                                }
                                                myProductInfo.number = productNumber;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                orderInfoList.push(myProductInfo);
            }
            //总计消费
            if (ol[i].className.indexOf("paydetail") != -1) {
                var priceInfoArray = ol[i].children[0].children;
                for (var pa = 0; pa < priceInfoArray.length; pa++) {
                    var cc = priceInfoArray[pa].children;
                    for (var cci = 0; cci < cc.length; cci++) {
                        if ($(cc[cci]).text().indexOf("实付款") != -1) {
                            var totalPrice = $(cc[cci + 1]).text();
                            if (totalPrice.indexOf("￥") != -1) {
                                totalPrice = totalPrice.substring(totalPrice.indexOf("￥") + 1, totalPrice.length)
                            }
                            tbOrderDetailInfo.total = totalPrice;
                            if (orderInfoList.length > 1) {//说明此订单中有多个商品
                                for (var oil = 0; oil < orderInfoList.length; oil++) {
                                    var tempTbOrderDetailInfo = {};
                                    tempTbOrderDetailInfo.id = tbOrderDetailInfo.id;
                                    tempTbOrderDetailInfo.time = tbOrderDetailInfo.time;
                                    tempTbOrderDetailInfo.name = orderInfoList[oil].name;
                                    tempTbOrderDetailInfo.price = orderInfoList[oil].price;
                                    tempTbOrderDetailInfo.number = orderInfoList[oil].number;
                                    tempTbOrderDetailInfo.total = tbOrderDetailInfo.total;
                                    tempTbOrderDetailInfo.address = tbOrderDetailInfo.address;
                                    totalProductArray.push(tempTbOrderDetailInfo);
                                }
                            } else if (orderInfoList.length == 1) {//此订单中只有一个商品
                                tbOrderDetailInfo.name = orderInfoList[0].name;
                                tbOrderDetailInfo.price = orderInfoList[0].price;
                                tbOrderDetailInfo.number = orderInfoList[0].number;
                                totalProductArray.push(tbOrderDetailInfo);
                            }
                            //存放数据
                            for(var sd = 0 ; sd < totalProductArray.length ; sd++ ){
                                currentOrderData.push(totalProductArray[sd]);
                            }
                            //保存数据
                            session.set("orderArray",currentOrderData);
                            //更新position
                            session.set("OrderItemPosition",currentPosition+1);
                            //关闭详情页
                            closeOrderDetail();
                        }
                    }
                }
            }
            //订单地址
            if (ol[i].className.indexOf("address") != -1) {
                var addressArray = ol[i].children[0].children;
                for (var aa = 0; aa < addressArray.length; aa++) {
                    if (addressArray[aa].className.indexOf("cont") != -1) {
                        var myAdArray = addressArray[aa].children;
                        for (var maa = 0; maa < myAdArray.length; maa++) {
                            if (myAdArray[maa].className.indexOf("submsg") != -1) {
                                var myAddress = $(myAdArray[maa]).text();
                                myAddress = myAddress.substring(myAddress.indexOf("：") + 1, myAddress.length);
                                tbOrderDetailInfo.address = myAddress;
                            }
                        }
                    }
                }
            }
        }
    }
    session.get("orderArray",function(count){
        if(count == undefined){
            session.set("taobaoState",-1);
            //关闭当前页面
            closeOrderDetail();
        }else{
            currentOrderData = count;
            session.get("OrderItemPosition",function(count){//因为是异步获取position所以要在拿到position后开始爬取
                currentPosition = count;
                getOrderDetail();
            });
        }
    });
    function closeOrderDetail(){
        $("div.back").click();//订单详情页的返回
    }
}
//------------------------------------------------------------------------------------爬取收货地址----------------------------------------------------------------------------
/**
 * 爬取收货地址
 */
if((window.location.hostname.indexOf("www.taobao.com") != -1)){
    location.href = "//i.taobao.com/my_taobao.htm";
}

if((window.location.href.indexOf("i.taobao.com/my_taobao") != -1)){
    log("----------------------------进入到账号管理界面----------------------------");
    dQuery("li.J_MtNavSubTrigger").children()[0].click();//进入到账号管理界面
    log("----------------------------click事件已经执行----------------------------");
}

if((window.location.hostname.indexOf("member1.taobao.com") != -1)){
    if((window.location.href.indexOf("deliver_address") != -1)){
        if((window.location.href.indexOf("addrId") != -1)){
            session.get("AddressData",function(addressData){
                session.get("addressUrlArray",function(urlArray){//取出href和位置并发起请求
                    session.get("addressUrlPosition",function(urlPosition){
                        //取出爬到的数据并保存
                        var tempAddaress =  {};
                        tempAddaress.location = dQuery("div.city-title").text();//所在区域
                        tempAddaress.address = dQuery("textarea#J_Street").text();//详细地址
                        tempAddaress.zipcode = dQuery("input#J_PostCode").attr("value");//邮编
                        tempAddaress.name = dQuery("input#J_Name").attr("value");//姓名
                        tempAddaress.phone = dQuery("input#J_Mobile").attr("value");//电话号
                        addressData.push(tempAddaress);
                        session.set("AddressData",addressData);
                        if(urlPosition<urlArray.length){
                            session.set("addressUrlPosition",urlPosition+1);
                            lcoation.href = urlArray[urlPosition+1];
                        }else{
                            log("----------------------------地址爬取完成----------------------------");
                            session.set("taobaoState",2);
                            //爬取个人信息
                        }
                    })
                })
            })
        }else {
            //dQuery("tbody>tr>td").map(function(){return dQuery(this).find("a")[0] }) 找出当前元素中的第一个子元素a
            //开始爬取地址
            var trList = dQuery("tbody>tr>td>a");
            var modifyUrlArray = [];
            for (var tl = 0; tl < trList.length; tl++) {
                if ("修改" == dQuery(trList[tl]).text()) {
                    modifyUrlArray.push(dQuery(trList[tl]).attr("href"));
                }
            }
            //保存数据
            session.set("addressUrlArray",modifyUrlArray);
            //记录位置
            session.set("addressUrlPosition",0);
            lcoation.href = modifyUrlArray[0];
        }
    }else{
        location.href="https://member1.taobao.com/member/fresh/deliver_address.htm"
    }
}
//------------------------------------------------------------------------------------爬取个人资料----------------------------------------------------------------------------

})