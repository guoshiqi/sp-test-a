dSpider("test", function(session,env,$) {

 log(env)
// session.upload("Hi, I am the test data!")
// session.finish()

    var companyNum = "pay_company_number";//缴存公司数量
    var companyList = "pay_company_list";//缴存公司列表
    var hasUserInfo = "has_user_info"; //1表示已获取个人信息
    var user_key = "user_info";
    var hasGrabIndex = "has_grab_pay_company_index"; //已爬取缴存单位下标
    var recordList = "record_list"; //明细列表

    //登录页面
    if (location.href.indexOf('gjjcx-login.jsp') != -1) {
        setUser();  //自动填充账号
     }

     //①登录成功页获取缴纳列表
     if(location.href.indexOf("gjjcx-choice.jsp") != -1) {
        session.showProgress()
        session.setProgressMax(100);
        savePayCompanies(); //保存缴纳公司列表
     }


     function savePayCompanies() {
        var companytrs = $('#new-mytable tbody').find('tr');
        if (companytrs && companytrs.length > 0) {
            session.set(companyNum, (companytrs.length - 1));
        }

        var companies = []; //单位列表
        for (var i = 0; i < companytrs.length; i++) {
            if (i == 0) {
                var ths = companytrs.eq(i).find('th');
                ths.each(function(){
                    log($(this).find('div').first().text());
                });
            } else {
                var tds = companytrs.eq(i).find('td');
                var id = tds.eq(0).find('div').text();
                var name = tds.eq(1).find('a').text();
                var clicktxt = tds.eq(1).find('a').attr("onclick");
                var start = clicktxt.indexOf('"');
                var end = clicktxt.indexOf('"', (start + 1));
                var link = clicktxt.substring((start + 1), end)
                var status = tds.eq(2).find('div').text();
                companies.push(new PayCompany(id, name, link, status)); //添加缴纳单位到集合中
            }
        }

        //保存缴存单位列表
        session.set(companyList, companies);

        //获取用户信息
        if (companies.length > 0) {
            location.href = companies[companies.length - 1].link;
        }
     }

     //②获取用户信息
     if (location.href.indexOf("gjj_cx.jsp") != -1) {
         var has = session.get(hasUserInfo);
         if (has == 1) {
            var num = session.get(companyNum);
            if (num >= 1) {
                var index = session.get(hasGrabIndex);
                if (index == undefined) {
                    index = 0;
                }  else if (index < num) {
                    session.setProgress(Math.max(1, index) / num * 100);
                }  else if (index >= num) {
//                     session.setProgress(100);
//                     session.showProgress(false);
//                     session.finish();
                     return;
                }

                document.getElementById('t3').click();
                getRecordList();
            }

         } else {
            getUserInfo();
         }
     }


     //解析用户信息
     function getUserInfo() {
         var name, sex, ID, cardNo, companyName, companyId, balance, lastDate, status;
         var trs = $('#tabContents').first().find('div').eq(1).find('table').first().find('tbody').first().find('tr');
         trs.each(function(i){
            var tds = $(this).find('td');
            if (i == 0) {
                tds.each(function(j){
                    if (j == 1) {
                        //姓名
                        name = $.trim($(this).text());
                    } else if (j == 3) {
                        //个人登记号
                        cardNo = $.trim($(this).text());
                    }
                });
            } else if (i == 1) {
                tds.each(function(j){
                    if (j == 3) {
                        //身份证号
                        ID = $.trim($(this).text());
                        //判断性别
                        sex = "男";
                    }
                });
            } else if (i == 2) {
                tds.each(function(j){
                    if (j == 1) {
                        //单位登记号
                        companyId = $.trim($(this).text());
                    } else if (j == 3) {
                        //单位名
                        companyName = $.trim($(this).text());
                    }
                });
            } else if (i == 4) {
                tds.each(function(j){
                    if (j == 1) {
                        //当前余额
                        balance = $.trim($(this).text());
                    } else if (j == 3) {
                        //账户状态
                        status = $.trim($(this).text());
                    }
                });
            } else if (i == 5) {
                tds.each(function(j){

                });
            } else if (i == 6) {
                tds.each(function(j){
                    if (j == 3) {
                        //最后操作日期
                        lastDate = $.trim($(this).text());
                    }
                });
            }
         });

         var user = new User(name, sex, ID, cardNo, companyName, companyId, balance, lastDate, status);
         session.set(user_key, user);
         session.set(hasUserInfo, 1);
         session.setProgress(10);

         //获取完个人信息去获取明细
         var companies = session.get(companyList);
         location.href = companies[0].link;
     }

     //获取个人明细账信息
     function getRecordList() {
        var records = session.get(recordList);
        if (records == undefined) {
            records = [];
        }

        var index = session.get(hasGrabIndex);

        if (index == undefined) {
            index = 0;
        } else {
            index = index + 1;
        }
        var companies = session.get(companyList);
        var companyName = companies[index].name;

        var trs = $('#tab-style').find('tbody').first().find('tr');
        if (trs.length > 1) {
            trs.each(function(i){
                if (i == 0) {
                    return;
                }
                var record = new Record();
                var tds = $(this).find('td');
                tds.each(function(j){
                    var value = $.trim($(this).text());
                    if (value.length == 0) {
                        return;
                    }

                    if (j == 0) {
                        record.date = value;
                    } else if (j == 1) {
                        record.month = value;
                    } else if (j == 2) {
                        record.describe = value;
                    } else if (j == 3) {
                        record.takeIn = value;
                    } else if (j == 4) {
                        record.takeOut = value;
                    } else if (j == 5) {
                        record.balance = value;
                    }
                });

                record.companyName = companyName;

                records.push(record);
            });

            session.set(recordList, records);

            //打印数据
            $.each(records,function(i, item){
                log("业务类型:" + item.describe + "---" + "时间:" + item.date + "---" + "入账:" + item.takeIn + "---" + "出账:" + item.takeOut + "---" + "余额:" + item.balance + "----" + "公司:" + item.companyName);
            });

            session.set(hasGrabIndex, index);

            var num = session.get(companyNum);
            if (num > 0 && (index <= (num - 2))) {
                location.href = companies[index + 1].link;
            } else {
                session.setProgress(100);
                session.showProgress(false);
                session.upload(session.get(user_key))
                session.finish();
            }
        } else {
            //点击查看历史明细账单
            var clicktxt = $('#tabContents').find('>div').eq(0).find('>div').eq(1).find('span').find('a').attr('onclick');
            var start = clicktxt.indexOf("'");
            var end = clicktxt.indexOf("'", (start + 1));
            var link = clicktxt.substring((start + 1), end)
            location.href = link;   //TODO 跳转到历史明细页
        }



     }

    //③获取历史缴存明细
    if (location.href.indexOf("gjj_cxls.jsp") != -1) {
        var records = session.get(recordList);
        if (records == undefined) {
            records = [];
        }

        var index = session.get(hasGrabIndex);
        if (index == undefined) {
            index = 0;
        } else {
            index = index + 1;
        }

        var companies = session.get(companyList);
        var companyName = companies[index].name;


        var trs = $('#new-mytable3').find('tbody').first().find('tr');
        if (trs.length > 1) {
                trs.each(function(i){
                    if (i == 0) {
                        return;
                    }
                    var record = new Record();
                    var tds = $(this).find('td');
                    tds.each(function(j){
                        var value = $.trim($(this).text());
                        if (value.length == 0) {
                            return;
                        }

                        if (j == 0) {
                            record.date = value;
                        } else if (j == 1) {
                            record.month = value;
                        } else if (j == 2) {
                            record.describe = value;
                        } else if (j == 3) {
                            record.takeIn = value;
                        } else if (j == 4) {
                            record.takeOut = value;
                        } else if (j == 5) {
                            record.balance = value;
                        }
                    });

                    record.companyName = companyName;

                    records.push(record);
                });

                session.set(recordList, records);
        }

        session.set(hasGrabIndex, index);
        var num = session.get(companyNum);
        if (num > 0 && (index <= (num - 2))) {
            location.href = companies[index + 1].link;
        } else {
            session.setProgress(index / num * 100);
        }

    }



    //自动填充账号*****************
    function setUser() {
        $('#bh1').attr('value', '1111');//填充内容
        $('#mm1').attr('value', '1111');//填充内容

    }

    //开户信息
    function PayCompany(id, name, link, status){
        this.id = id;//开户登记号
        this.name = name;//开户单位
        this.link = link;//具体缴纳信息链接
        this.status = status;//缴纳状态
    }

    //用户信息
    function User(name, sex, ID, cardNo, companyName, companyId, balance, lastDate, status){
        this.name = name;   //姓名
        this.sex = sex; //性别
        this.ID = ID;   //身份证号
        this.cardNo = cardNo;   //客户号
        this.companyName = companyName; //公司名
        this.companyId = companyId; //公司编号
        this.balance = balance; //当前余额
        this.lastDate = lastDate;   //最后操作日期
        this.status = status;   //当前状态
    }

    //操作类型
    function Operate(type, date){
        this.type = type;
        this.date = date;
    }

    //缴存记录
    function Record(month, takeIn, takeOut, describe, date, balance, companyName){
        this.month = month;     //缴存月份
        this.takeIn = takeIn;   //入账
        this.takeOut = takeOut; //出账
        this.describe = describe;   //描述
        this.date = date;   //时间
        this.balance = balance; //余额
        this.companyName;   //公司
    }
})