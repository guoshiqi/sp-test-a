dSpider("mobile", 60 * 3,function(session,env,$) {

    //常量
    var SessionLogTypeNotReporte = -1;  // log不上报
    var SessionProgressMax = 7;         // 最大进度
    var SessionSpiderMonthCount = 3;         // 爬取的月份，6个月为5，最大是5

    function hideElement(element) {
        if (element.length > 0) {
            element.hide();
        }
    }

    function checkLogin_second() {

        var cts = 'shop.10086.cn/i/?f=billdetailqry&welcome=';
        if (window.location.href.indexOf(cts) >= 0) {
            session.log('确定进入的是爬取页');
            var phone = window.xd_phone;
            //检测是否需要登陆短信
            session.showProgress();
            session.setProgressMsg('认证过程大约需要3分钟，请耐心等待');
            session.setProgressMax(SessionProgressMax);
            window.xd_progressMax = 1;
            session.setProgress(window.xd_progressMax);
            window.xd_data = {};
            window.xd_month_progress_count = 0;

            setTimeout(function() {

                // // TODO 测试
                // window.xd_month_progress_count = 1;
                // $('#switch-data li').eq(1).click();
                // $('#month-data li').eq(1).click();
                //
                // setTimeout(function() {
                //     startSpiderMonthData(1, 1);
                // }, 4000);

                checkSec();
            },10000);
        } else {
            //等8秒重定向时间
            setTimeout(function() {
                session.finish("没有进入到爬取页面", '', 3);
            },8000);
        }
    }

    function checkSec() {
        if ($('.all-site-loading div').is(':visible')) {
            location.reload();
            return;
        }

        //检测是否需要二次验证
        xd_check();
    }

    function getMyUserInfo(phone) {

        var url = 'http://shop.10086.cn/i/v1/cust/info/' + phone + '?time=' + new Date().getTime();
        $.get(url, function (result) {

            var data = result && result.data;

            if(!data){
                session.finish("获取用户数据为空", JSON.stringify({result:result,phone:session.getLocal("xd_phone")}), 3);
                return ;
            }

            var initD = data.inNetDate.toString();
            var reg_time = initD.substr(0, 4) + '-' + initD.substr(4, 2) + '-' + initD.substr(6, 2) + ' ' +
                initD.substr(8, 2) + ':' + initD.substr(10, 2) + ':' + initD.substr(12, 2);


            var xd_user_info = {
                'mobile':window.xd_phone,
                'name': data.name,
                'household_address': data.address,
                'contactNum': data.contactNum,
                'registration_time': reg_time,
                'rawRegistrationTime': initD, // 原数据
            };

            window.xd_data['user_info'] = xd_user_info;
            window.xd_progressMax++;
            session.setProgress(window.xd_progressMax);
            window.xd_month_progress_count++;
            xdProcessData();
        });
    }

    function spiderData() {

        //爬取用户信息
        getMyUserInfo(window.xd_phone);

        //爬取用户通话详单
        spiderData2();
    }

    function checkLogin_first() {

        // 检测400错误
        if ($('title').text().indexOf('400') >= 0) {
            session.finish($('title').text(), '', 3);
            return;
        }

        var cts = 'login.10086.cn';
        var cts2 = 'channelID';
        // 登陆页
        if (location.href.indexOf(cts) >= 0 && location.href.indexOf(cts2) >= 0) {

            session.log('进入登陆页');

            // 隐藏其他跳转元素
            hideElement($('#submit_help_info'));
            hideElement($('#link_info'));
            hideElement($('#forget_btn'));
            hideElement($('#go_home'));
            hideElement($('.back_btn'));
            hideElement($('#chk'));
            hideElement($('#chk').parent().find('label'));

            if ($('#getSMSpwd').length) {
                $('#getSMSpwd').click(function () {
                    session.set("firstSMSTime",new Date().getTime() + '');
                });
            }

            if ($('#getPhoneSMSpwd').length) {
                $('#getPhoneSMSpwd').click(function () {
                    session.set("firstSMSTime",new Date().getTime() + '');
                });
            }

            // 填充默认手机号
            var prePhone = null;
            prePhone = session.getArguments().phoneNo;
            session.log('默认手机号：' + prePhone);
            if (!prePhone) {
                prePhone = session.getLocal("xd_phone");
            }
            if (!!prePhone) {
                $('#p_phone_account').val(prePhone);
                $("#p_phone_account").attr({"disabled":true});

                $('#account_nav').click(function () {
                    if (prePhone) {
                        if (!$('#p_pwd').val()) {
                            window.jQuery("#p_phone_account").blur();
                        }
                    }
                });

                $('#p_phone').val(prePhone);
                $("#p_phone").attr({"disabled":true});
                window.jQuery("#p_phone").blur();

                $('#submit_bt').click(function () {
                    // 防止一开始没触发
                    window.jQuery("#p_phone").blur();
                });
            }

            $('#submit_bt').click(function () {

                //存储手机号
                if ($('#account_nav').attr('class') == 'on') {
                    session.setLocal("xd_phone",$('#p_phone_account').val());
                } else {
                    session.setLocal("xd_phone",$('#p_phone').val());
                }

                if ($('#p_sms').val() == '') {
                    session.set("firstSMSTime",'0');
                }
            });
        } else {
            // 爬取页
            session.log('进入爬取页');
            var xd_phone = session.getLocal("xd_phone");
            window.xd_phone = xd_phone;
            if (window.xd_phone) {
                checkLogin_second();
                return;
            } else {
                //手机号未就绪
                session.log('手机号未就绪');
                return;
            }
        }
    }

    function spiderData2()  {
        $('#switch-data li').eq(1).click();
        $('#month-data li').eq(0).click();

        setTimeout(function() {
            if ($('#switch-data li').eq(1).attr('class') == 'active') {
                startSpiderMonthData(0, 1);
            } else {
                spiderData2();
            }
        }, 4000);
    }

    function checkDataRepetition(month, fixMonth) {
        //标记上第一个，防止爬取重复
        if ($('#tbody').attr('data-marked') == null) {
            // 新的，可以爬取
            $('#tbody').attr('data-marked', true);
            return true;
        } else {
            $('#month-data li').eq(month).click();
            //查看是不是已经有此月份了
            for (var i = 0; i < window.xd_callBill.length; i++) {

                var obj = window.xd_callBill[i];
                if (obj.calldate.indexOf(fixMonth) >= 0) {
                    //有此月份
                    window.xd_callBill.pop(obj);
                    break;
                }
            }
            return false;
        }
    }

    function startSpiderMonthData(month, index) {

        // alert('' + month + '月 ' + index);

        // 会出现第三次认证的问题
        if ($('#show_vec_firstdiv').is(':visible')) {
            session.log('展示二次验证。多次认证');
            window.third_callbill_month = month;
            window.third_callbill_vertify = true;
            showMask(true);
            $('#sendSmsBtn').click(function () {
                $('#stc-send-sms').click();
            });
            return;
        } else {
            window.third_callbill_vertify = false;
        }

        var fixMonthValue = (function fixMonthValue(month) {
            var str = month;
            str=str.replace("年","");
            str=str.replace("月","");
            return str;
        })($('#month-data li').eq(month).text());

        //有详单记录
        if ($('#tbody').is(':visible')) {
            var logString = '有详单记录: ' + '日期-' + fixMonthValue + '页数-' + index;
            session.log(logString);

            var check = checkDataRepetition(month, fixMonthValue);
            if (check == false) {
                // 数据爬取重复
                setTimeout(function () {
                    startSpiderMonthData(month, 1);
                }, 3000);
                return;
            }

            //此月爬完push
            var obj = {};
            obj['month'] = fixMonthValue;
            obj['rawMonth'] = $('#month-data li').eq(month).text();
            var spiderdata = get_current_page_bill(fixMonthValue);
            obj['value'] = spiderdata.dataArr;
            obj['status'] = spiderdata.status;
            var total = $('#notes2').text();
            obj['total'] = total.substring(1, total.length - 1);
            pushCallDetailData(obj);

            var xd_page = $('#notes1').text().substring(1, $('#notes1').text().length - 1);
            var xd_page1 = xd_page.substring(0, xd_page.indexOf('/'));
            var xd_page2 = xd_page.substring(xd_page.indexOf('/') + 1);
            if (xd_page1 == xd_page2) {
                session.log('当前是最后一页');
                window.xd_progressMax++;
                session.setProgress(window.xd_progressMax);
                month++;
                if (month > SessionSpiderMonthCount) {
                    window.xd_month_progress_count++;
                    xdProcessData();
                    return;
                }
                $('#month-data li').eq(month).click();
                setTimeout(function () {
                    startSpiderMonthData(month, index);
                }, 3000);
            } else {
                var nextIndex = parseInt(xd_page1);
                nextIndex++;
                window.jQuery(".gs-page").eq(nextIndex - 1).click();
                setTimeout(function () {
                    startSpiderMonthData(month, nextIndex);
                }, 4000);
            }
            return;
        }

        //您选择时间段没有详单记录哦
        if ($('tbody.err tr td:eq(0) div:eq(0) div:eq(1) div:eq(0)').is(':visible')) {
            session.log('选择时间段没有详单记录');
            //此月爬完push
            var obj = {};
            obj['month'] = fixMonthValue;
            obj['value'] = [];
            obj['total'] = 0;
            obj['status'] = 4;
            pushCallDetailData(obj);
            window.xd_progressMax++;
            session.setProgress(window.xd_progressMax);
            month++;
            if (month > SessionSpiderMonthCount) {
                window.xd_month_progress_count++;
                xdProcessData();
                return;
            }
            $('#month-data li').eq(month).click();
            setTimeout(function () {
                startSpiderMonthData(month, index);
            }, 3000);
            return;
        }

        //还没出来，网络较差
        if ($('a.gs-search').is(':visible')) {
            window.jQuery(".gs-page").eq(index - 1).click();
        } else {
            $('#month-data li').eq(month).click();
        }
        setTimeout(function () {
            startSpiderMonthData(month, index);
        }, 6000);
    }

    function get_second_from_str(str) {
        // 1小时5分14秒
        var totalTime = 0;

        var h_index = str.indexOf('小时');
        if (h_index >= 0) {
            var h_str = str.substring(0, h_index);
            var h = parseInt(h_str);
            totalTime += h * 60 * 60;
            str = str.substr(h_index + 2);
        }

        var m_index = str.indexOf('分');
        if (m_index >= 0) {
            var m_str = str.substring(0, m_index);
            var m = parseInt(m_str);
            totalTime += m * 60;
            str = str.substr(m_index + 1);
        }

        var s_index = str.indexOf('秒');
        if (s_index >= 0) {
            var s_str = str.substring(0, s_index);
            var s = parseInt(s_str);
            totalTime += s;
        }
        return totalTime;
    }

    function get_current_page_bill(month) {
        var arr = [];
        var state = 0;
        var page_total = $('#tbody tr').length;
        for (var i = 0; i < page_total; i++) {
            var wrapCall = {};
            wrapCall['callFee'] = $('#tbody tr').eq(i).find('td').eq(7).text();
            wrapCall['remoteType'] = $('#tbody tr').eq(i).find('td').eq(5).text();
            wrapCall['callType'] = $('#tbody tr').eq(i).find('td').eq(2).text();
            var callTimeStr = $('#tbody tr').eq(i).find('td').eq(4).text();
            wrapCall['callTime'] = get_second_from_str(callTimeStr);
            // callTime原数据
            wrapCall['rawCallTime'] = callTimeStr;
            wrapCall['callAddress'] = $('#tbody tr').eq(i).find('td').eq(1).text();
            // month = 201703
            wrapCall['callBeginTime'] = month.substring(0, 4) + '-' + $('#tbody tr').eq(i).find('td').eq(0).text();
            // callBeginTime原数据
            wrapCall['rawCallBeginTime'] = $('#tbody tr').eq(i).find('td').eq(0).text();
            wrapCall['otherNo'] = $('#tbody tr').eq(i).find('td').eq(3).text();
            wrapCall['taocan'] = $('#tbody tr').eq(i).find('td').eq(6).text();
            session.log('month: ' + month, SessionLogTypeNotReporte);
            session.log('callBeginTime: ' + wrapCall['callBeginTime'], SessionLogTypeNotReporte);
            var month_page = month.substr(0, 4) + '-' + month.substr(4, 2);
            if (wrapCall['callBeginTime'].indexOf(month_page) >= 0) {
                arr.push(wrapCall);
            } else {
                // 网页没有刷新
                state = 2;
            }
        }
        return {
            "status" : state,
            "dataArr" : arr,
        };
    }

//整理详单数据
    function xdProcessData() {
        if (window.xd_month_progress_count == 2) {
            window.xd_data['month_status'] = window.xd_callBill;
            session.upload(window.xd_data);
            session.set('xd_hasEndSpider', 1);
            setTimeout(function () {
                session.log('点击退出按钮，清除缓存');
                window.jQuery("#logout").click();
                // 超时直接finish
                setTimeout(function () {
                    session.setProgress(SessionProgressMax);
                    session.finish();
                }, 5000);
            }, 1000);
        }
    }

//存储详单数据
    function pushCallDetailData(data) {

        if (!window.xd_callBill) {
            window.xd_callBill = [];
        }

        var monthData = null;
        if (window.xd_callBill.length > 0) {
            //查看是不是已经有此月份了
            for (var i = 0; i < window.xd_callBill.length; i++) {
                var obj = window.xd_callBill[i];
                if (obj.calldate.indexOf(data.month) >= 0) {
                    //有此月份
                    monthData = obj;
                    break;
                }
            }
        }

        //第一次添加月份
        if (monthData == null) {

            var time = new Date().getTime();
            var xd_cid = (function zfill(num, size) {
                var s = "000000000" + num;
                return s.substr(s.length - size);
            }(time, 10));

            monthData = {};
            monthData['data'] = [];
            monthData['calldate'] = data.month;
            // 原数据
            monthData['rawCalldate'] = data.rawMonth;
            monthData['totalCount'] = data.total;
            monthData['mobile'] = window.xd_phone;
            monthData['cid'] = xd_cid;
            monthData['status'] = 0;
            window.xd_callBill.push(monthData);
        }

        // 非0和undefined
        if (data.status){
            monthData['status'] = data.status;
        }

        if (data.value.length > 0) {
            for (var i = 0; i < data.value.length; i++) {
                var call = data.value[i];
                monthData.data.push(call);
            }
        }

        if (data.status == 0 && monthData.data.length == 0) {
            monthData['status'] = 2;
        }
    }

    function xd_check() {
        $('#month-data li').eq(0).click();

        var xd_startTriggerSecVertifiTime = session.get('xd_startTriggerSecVertifiTime');
        var xd_hasFitstSecReload = true;

        if (!xd_startTriggerSecVertifiTime) {
            xd_hasFitstSecReload = false;
            xd_startTriggerSecVertifiTime = (new Date()).getTime();
            session.set('xd_startTriggerSecVertifiTime', xd_startTriggerSecVertifiTime);
        }

        if (xd_startTriggerSecVertifiTime < (new Date()).getTime() - 60000) {
            if (!xd_hasFitstSecReload) {
                session.log('二次认证一直不出现，刷新一次试试');
                location.reload();
                return;
            }
        }

        if (window.xd_startTriggerSecVertifiTime < (new Date()).getTime() - 90000) {
            session.finish("二次验证请求, 许久没有出现", '', 3);
            return;
        }

        session.log('触发二次验证');
        setTimeout(function () {
            if ($('#show_vec_firstdiv').is(':visible')) {
                session.log('展示二次验证');
                showMask(true);
                $('#sendSmsBtn').click(function () {
                    // 验证是否超过50秒
                    var olddata = parseInt(session.get("firstSMSTime"));
                    if (olddata + 60000 > new Date().getTime()) {
                        alert('对不起，短信随机码暂时不能发送，请一分钟以后再试！');
                        return;
                    }
                    $('#stc-send-sms').click();
                });
                return;
            }
            xd_check();
        }, 15000);
    }

    function refreshImgVertify() {
        // 如果隐藏了，停止刷新
        if (!$('#imgVert').is(':visible')) {
            return;
        }

        // 显示对号
        if ($('#vec_imgcode').attr('class') && $('#vec_imgcode').attr('class').indexOf('yzm-true') >= 0) {
            session.log('验证码输入正确', SessionLogTypeNotReporte);
            $('#inputImg').css({"background":"#FFFFFF url(/i/nresource/image/icon-20.png) no-repeat",
                "background-position":"right center",
                "background-size":"30px 30px"});
        } else {
            $('#inputImg').css({"background":"#FFFFFF"});
        }

        // 显示错误信息
        if ($('#detailerrmsg').is(':visible')) {
            //认证失败,提示错误信息
            var errorMessage = $('#detailerrmsg').text();
            $('#xd_sec_errorMessage').text(errorMessage);

            // 如果按钮已经摁下，出现错误时清空输入框
            if(!!$("#certificateBtn").attr("disabled")) {
                // 图形验证码错误，清空图形验证码，刷新图形
                if (errorMessage.indexOf('验证码错误') >= 0) {
                    $('#inputImg').val('');
                    $('#imageVec').click();
                }

                if (errorMessage.indexOf('随机密码错误') >= 0) {
                    $('#inputSms').val('');
                    $('#inputImg').val('');
                    $('#imageVec').click();
                }

                if (errorMessage.indexOf('服务密码错误') >= 0) {
                    $('#inputPwd').val('');
                    $('#inputSms').val('');
                    $('#inputImg').val('');
                    $('#imageVec').click();
                }
            }
            $('#certificateBtn').removeAttr("disabled");
            session.log('错误信息： ' + errorMessage, SessionLogTypeNotReporte);
        } else {
            $('#xd_sec_errorMessage').text('');
        }

        setTimeout(function () {
            refreshImgVertify();
        }, 200);
    }

    function showMask(isShow) {

        if (isShow) {
            if (!window.flex) {

                !function (e) {
                    function t(a) {
                        if (i[a])return i[a].exports;
                        var n = i[a] = {exports: {}, id: a, loaded: !1};
                        return e[a].call(n.exports, n, n.exports, t), n.loaded = !0, n.exports
                    }

                    var i = {};
                    return t.m = e, t.c = i, t.p = "", t(0)
                }([function (e, t) {
                    "use strict";
                    Object.defineProperty(t, "__esModule", {value: !0});
                    var i = window;
                    t["default"] = i.flex = function (e, t) {
                        var a = e || 100, n = t || 1, r = i.document, o = navigator.userAgent;
                        var d = o.match(/Android[\S\s]+AppleWebkit\/(\d{3})/i), l = o.match(/U3\/((\d+|\.){5,})/i);
                        var c = l && parseInt(l[1].split(".").join(""), 10) >= 80;
                        var p = navigator.appVersion.match(/(iphone|ipad|ipod)/gi), s = i.devicePixelRatio || 1;
                        p || d && d[1] > 534 || c || (s = 1);
                        var u = 1 / s, m = r.querySelector('meta[name="viewport"]');
                        m || (m = r.createElement("meta"), m.setAttribute("name", "viewport"), r.head.appendChild(m));
                        var sss = "width=device-width,user-scalable=no,initial-scale=";
                        m.setAttribute("content", sss + u + ",maximum-scale=" + u + ",minimum-scale=" + u);
                        r.documentElement.style.fontSize = a / 2 * s * n + "px"
                    }, e.exports = t["default"]
                }]);
                flex(200, 1);
            }

            setTimeout(function () {
                window.scrollTo(0, 0);
            }, 500);

            var leftGapFloat = .15;
            var leftGap = leftGapFloat + 'rem';
            var webViewWidthFloat = screen.width / 100.;
            var webViewWidth = webViewWidthFloat + 'rem';

            var maskDiv = $('<div></div>');        //创建一个父div
            maskDiv.attr('id', 'maskDiv');        //给父div设置id
            $("body").append(maskDiv);
            $("#maskDiv").css({
                'opacity': 1,
                'position': 'fixed',
                'top': 0,
                'left': 0,
                'background-color': '#f6f6f6',
                'width': '100%',
                'height': '100%',
                'z-index': 214748364,
            });

            // cell 背景
            var cellBackgroundDiv = $('<div><div/>');
            var cellStyle = {
                'position': 'absolute',
                'top': '.08rem',
                'left': '0rem',
                'width': webViewWidth,
                'height': '1rem',
                'background-color': '#ffffff',
            };
            cellBackgroundDiv.css(cellStyle);
            $("#maskDiv").append(cellBackgroundDiv);

            // cell 间隔线
            var cellSeparator = $('<div><div/>');
            cellSeparator.css({
                'position': 'absolute',
                'top': '.5rem',
                'left': leftGap,
                'width': (webViewWidthFloat - leftGapFloat) + 'rem',
                'height':'0.015rem',
                'background-color': '#d4d7dd',
            });
            cellBackgroundDiv.append(cellSeparator);

            //提示1
            var title1 = $($('<p><p/>'));
            title1.text('服务密码');
            title1.css({
                'position': 'absolute',
                'line-height':'.5rem',
                'left': leftGap,
                'top': 0,
                'height': '.5rem',
                'width': '.7rem',
                'font-size': '.15rem',
            });
            cellBackgroundDiv.append(title1);

            var inputSmsWidth = 320. / 750. * webViewWidthFloat;
            var titleRightFloat = 0.85;
            //密码输入框
            var inputPwd = $('<input type="password" id="inputPwd"/>');
            inputPwd.css({
                'position': 'absolute',
                'left': titleRightFloat + 'rem',
                'top': '.1rem',
                'height': '.3rem',
                'line-height':'.3rem',
                'width': inputSmsWidth + 'rem',
                'font-size': '.15rem',
                'background-color': 'white',
            });
            inputPwd.attr('placeholder', '请输入服务密码');
            inputPwd.attr('maxlength','8');
            cellBackgroundDiv.append(inputPwd);

            //提示2
            var title2 = $($('<p><p/>'));
            title2.text('随机密码');
            title2.css({
                'position': 'absolute',
                'line-height':'.5rem',
                'left': leftGap,
                'top': '.5rem',
                'height': '.5rem',
                'width': '.7rem',
                'font-size': '.15rem',
            });
            cellBackgroundDiv.append(title2);

            //短信输入框
            var inputSms = $('<input type="text" id="inputSms"/>');
            inputSms.css({
                'position': 'absolute',
                'left': titleRightFloat + 'rem',
                'top': '.61rem',
                'height': '.29rem',
                'line-height':'.29rem',
                'width': inputSmsWidth + 'rem',
                'font-size': '.15rem',
                'background-color': 'white',
            });
            inputSms.attr('placeholder', '请输入短信验证码');
            inputSms.attr('maxlength','6');
            cellBackgroundDiv.append(inputSms);

            //发送短信
            var smssendwidthFloat = 194. / 750. * webViewWidthFloat;
            var smssendwidth = smssendwidthFloat + 'rem';
            var input = $('<input type="button" id="sendSmsBtn" value="获取验证码"/>');
            input.click(settime);
            var cssEnable = {
                'position': 'absolute',
                'border-radius':'0.025rem',
                'border-style':'solid',
                'border-color':'#4e73ed',
                'border-width':'0.01rem',
                'left': webViewWidthFloat - leftGapFloat - smssendwidthFloat + 'rem',
                'top': ((.50 - .28) / 2 + .5) + 'rem',
                'height': '.28rem',
                'width': smssendwidth,
                'font-size': '.13rem',
                'background-color':"white",
                'color': '#4e73ed',
            };

            var cssDisable = {
                'position': 'absolute',
                'border-radius':'0.025rem',
                'border-style':'none',
                'left': webViewWidthFloat - leftGapFloat - smssendwidthFloat + 'rem',
                'top': ((.50 - .28) / 2 + .5) + 'rem',
                'height': '.28rem',
                'width': smssendwidth,
                'font-size': '.13rem',
                'background-color':"#bcc0c9",
                'color': 'white',
            };

            input[0].cssEnable = cssEnable;
            input[0].cssDisable = cssDisable;
            input.css(cssEnable);
            cellBackgroundDiv.append(input);

            var errormessageTop = 0.08 + 1;
            // 如果有图形验证码
            if($('#imageVec').length > 0) {
                var errormessageTop = 0.08 + 1.5;

                // 增大背景
                cellBackgroundDiv.css('height', '1.5rem');
                // 多加一条线
                var cellSeparator2 = cellSeparator.clone();
                cellSeparator2.css({'top':'1rem'});
                cellBackgroundDiv.append(cellSeparator2);

                //提示3
                var title3 = title1.clone();
                title3.text('验证码');
                title3.css({'top': '1rem'});
                cellBackgroundDiv.append(title3);
                // 图形验证码输入
                var inputImg = $('<input type="text" id="inputImg"/>');
                inputImg.css({
                    'position': 'absolute',
                    'left': titleRightFloat + 'rem',
                    'top': '1.11rem',
                    'height': '.29rem',
                    'line-height':'.29rem',
                    'width': inputSmsWidth + 'rem',
                    'font-size': '.15rem',
                    'background-color': 'white',
                });
                inputImg.attr('placeholder', '请输入图形验证码');
                inputImg.attr('maxlength','6');
                cellBackgroundDiv.append(inputImg);

                // 添加图片
                var item = '<canvas id="imgVert">' +
                    'Your browser does not support the HTML5 canvas tag. </canvas>';
                var imgVert = $(item);
                imgVert.css({
                    'position': 'absolute',
                    'left': input.css('left'),
                    'top': ((.50 - .4) / 2 + 1) + 'rem',
                    'height': '.4rem',
                    'width': smssendwidth,
                    'borderWidth' : '1.5px',
                    'borderColor' : 'gray',
                    'borderStyle' : 'solid',
                });
                var canvasWidth = smssendwidthFloat * 200;
                var canvasHeight = .4 * 200;
                imgVert.attr({'height': canvasHeight + 'px', 'width': canvasWidth + 'px'});
                cellBackgroundDiv.append(imgVert);

                // 设置图片
                var c = document.getElementById("imgVert");
                var ctx = c.getContext("2d");
                var img = document.getElementById("imageVec");

                // 显示验证码
                if(img.complete) {
                    ctx.drawImage(img, 0, 0);
                }

                $(img).on("load",function () {
                    ctx.drawImage(img, 0, 0);
                })

                $('#imgVert').attr('my_src', $('#imgVec').src);
                // 设置定时刷新图片
                refreshImgVertify();

                // 绑定原图片事件
                $('#imgVert').click(function () {
                    $('#imageVec').click();
                });
                // 绑定输入事件
                $('#inputImg').on('keyup',function(){
                    // 验证码
                    session.log('验证码： ' + $('#inputImg').val());
                    $('#vec_imgcode').val('' + $('#inputImg').val());
                    window.jQuery('#vec_imgcode').keyup();
                });
            }

            //错误提示
            var errorMessage = $($('<p id="xd_sec_errorMessage"><p/>'));
            $("#maskDiv").append(errorMessage);
            $('#xd_sec_errorMessage').css({
                'position': 'absolute',
                'left': leftGap,
                'top': errormessageTop + 'rem',
                'height': '.2rem',
                'width': '3rem',
                'line-height':'.2rem',
                'font-size': '0.1rem',
                'color': 'red',
            });

            //认证
            var certificateBtn = $('<input type="button" id="certificateBtn" value="去认证"/>');
            certificateBtn.click(certificateBtnAction);
            $("#maskDiv").append(certificateBtn);

            $('#certificateBtn').css({
                'position': 'absolute',
                'border-radius':'0.025rem',
                'left': leftGap,
                'top': errormessageTop + .2 + 'rem',
                'height': '.5rem',
                'width': (webViewWidthFloat - leftGapFloat * 2) + 'rem',
                'font-size': '.17rem',
                'color': 'white',
                'background-color':'#4e73ed',
            });

            // 隐藏进度条
            session.showProgress(false);
        } else {
            session.showProgress();
            if ($('#maskDiv').lensgth != 0) {
                // 端上有动画，要延迟.3秒
                setTimeout(function () {
                    $("#maskDiv").remove();
                }, 300);
            }
        }
    }

    function certificateBtnAction() {
        window.xd_pwd = $('#inputPwd').val();

        //服务密码
        $('#vec_servpasswd').val('' + window.xd_pwd);
        // 随机密码
        $('#vec_smspasswd').val('' + $('#inputSms').val());

        // if ($('#imgVert').attr('class').indexOf('yzm-true')) {
        //     // 认证按钮可以按
        //     $('#certificateBtn').removeAttr("disabled");
        // }
        $('#vecbtn').click();

        $('#certificateBtn').attr({"disabled":true});

        setTimeout(function () {
            che_vertify_dismiss();
        }, 1000);
    }

    window.countdown = 60;
    function settime() {

        var obj = $('#sendSmsBtn')[0];
        if (!obj) {
            return;
        }

        if (window.countdown == 60) {
            $('#sendSmsBtn').css(obj.cssDisable);
        }
        if (window.countdown == 0) {
            obj.removeAttribute("disabled");
            $('#sendSmsBtn').css(obj.cssEnable);
            obj.value = "获取验证码";
            window.countdown = 60;
            return;
        } else {
            window.xd_pwd = $('#inputPwd').val();
            obj.setAttribute("disabled", true);
            obj.value = "重新发送(" + window.countdown + "s)";
            window.countdown--;
        }
        setTimeout(function () {
                settime();
            }
            , 1000);
    }

    function che_vertify_dismiss() {

        if (!!window.third_callbill_vertify) {
            //这是第二次进来的
            if (!$('#show_vec_firstdiv').is(':visible')) {
                showMask(false);
                $('#month-data li').eq(window.third_callbill_month).click();
                setTimeout(function () {
                    //继续爬取
                    startSpiderMonthData(window.third_callbill_month, 1);
                }, 5000);
                return;
            }

            if (!$('#detailerrmsg').is(':visible')) {
                setTimeout(function() {che_vertify_dismiss();}, 500);
            }
            return;
        }

        if (!$('#show_vec_firstdiv').is(':visible') && $('tbody').length > 0) {
            showMask(false);
            $('#switch-data li').eq(1).click();
            $('#month-data li').eq(0).click();
            setTimeout(function () {
                window.xd_month_progress_count = 0;
                spiderData();
            }, 3000);
            return;
        }

        if (!$('#detailerrmsg').is(':visible')) {
            setTimeout(function() {che_vertify_dismiss();}, 500);
        }
    }

    // -------------------------------------------
    var hasEndSpider = session.get('xd_hasEndSpider');
    if (hasEndSpider == 1) {
        session.setProgress(SessionProgressMax);
        session.finish();
    }

    $.onload(function(){
        checkLogin_first();
    });

    //设置当前页是登陆页
    if ($('#forget_btn').length && $('#forget_btn').length > 0) {
        session.setStartUrl();
    }
});