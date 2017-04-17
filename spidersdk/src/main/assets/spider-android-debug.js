/**
 * Created by du on 16/9/1.
 */

var $ = dQuery;
$.onload=function(cb){
    if(document.readyState=="complete"){
        cb();
    }else {
        window.addEventListener("load",function(){
            cb();
        })
    }
}

String.prototype.format = function () {
    var args = Array.prototype.slice.call(arguments);
    var count = 0;
    return this.replace(/%s/g, function (s, i) {
        return args[count++];
    });
};

function _logstr(str) {
    str = str || " "
    return typeof str == "object" ? JSON.stringify(str) : (new String(str)).toString()
}

function log(str) {
    var s = window.curSession
    if (s) {
        s.log(str)
    } else {
        console.log("dSpider: " + _logstr(str))
    }
}

//异常捕获
function errorReport(e) {
    var msg = "语法错误: " + e.message + "\nscript_url:" + _su + "\n" + e.stack
    if (window.curSession) {
        curSession.log(msg,-1);
        curSession.finish(e.message, "", 2, msg);
    }
}

String.prototype.endWith = function (str) {
    if (!str) return false;
    return this.substring(this.length - str.length) === str;
};

//queryString helper
window.qs = [];
var s = location.search.substr(1);
var a = s.split('&');
for (var b = 0; b < a.length; ++b) {
    var temp = a[b].split('=');
    qs[temp[0]] = temp[1] ? temp[1] : null;
}
MutationObserver = window.MutationObserver || window.WebKitMutationObserver

function safeCallback(f) {
    if (!(f instanceof Function)) return f;
    return function () {
        try {
            f.apply(this, arguments)
        } catch (e) {
            errorReport(e)
        }
    }
}
//设置$异常处理器
$.safeCallback = safeCallback;
$.errorReport = errorReport;

function hook(fun) {
    return function () {
        if (!(arguments[0] instanceof Function)) {
            var t = arguments[0];
            log("warning: " + fun.name + " first argument should be function not string ")
            arguments[0] = function () {
                eval(t)
            };
        }
        arguments[0] = safeCallback(arguments[0]);
        return fun.apply(this, arguments)
    }
}

//hook setTimeout,setInterval异步回调
var setTimeout = hook(window.setTimeout);
var setInterval = hook(window.setInterval);

//dom 监控
function DomNotFindReport(selector) {
    var msg = "元素不存在[%s]".format(selector)
    log(msg)
}

function waitDomAvailable(selector, success, fail) {
    var timeout = 10000;
    var t = setInterval(function () {
        timeout -= 10;
        var ob = $(selector)
        if (ob[0]) {
            clearInterval(t)
            success(ob, 10000 - timeout)
        } else if (timeout == 0) {
            clearInterval(t)
            var f = fail || DomNotFindReport;
            f(selector)
        }
    }, 10);
}

function Observe(ob, options, callback) {
    var mo = new MutationObserver(callback);
    mo.observe(ob, options);
    return mo;
}

var _timer,_timeOut=-1;

function _startTimer(s){
    var left=_timeOut*1000- (s.get("_pass")||0)
    if(left<0) left=0;
    _timer=setTimeout(function(){
        log("time out");
        if (!s.finished) {
            s.finish("timeout ["+_timeOut+"s] ", "",4)
        }
    },left);
    log("_Timer:"+left/1000+"s left");
}
function _resetTimer(show){
    var s=window.curSession;
    if(_timeOut==-1) return;
    var key=show?"_show":"_hide";
    var last=s.get("_last");
    last=last||"_hide";
    //显示状态没有改变则什么也不做
    if(last==key) return;
    var now=new Date().getTime()
    var passed;
    if(key=="_show"){
        _startTimer(s)
    }else{
        passed=now- (s.get("_show")||now);
        s.set("_pass", (s.get("_pass")||0)+passed);
        clearTimeout(_timer)
    }
    s.set("_last",key);
    s.set(key,now)
}
//爬取入口
function dSpider(sessionKey, timeOut, callback) {
    //判断调用源,如果是在onSpiderInited中调用,则下发脚本中的dSpider函数不执行
    if (window.onSpiderInited && this != 5) {
        return;
    }
    var session = new DataSession(sessionKey);
    var onclose = function () {
        log("onNavigate:" + location.href)
        session._save()
        if (session.onNavigate) {
            session.onNavigate(location.href);
        }
    }
    $(window).on("beforeunload", onclose)
    window.curSession = session;
    session._init()
    if (!callback) {
        callback = timeOut;
        timeOut = -1;
    }

    if (timeOut != -1) {
        _timeOut = timeOut;
        if (session.get("_last") == "_show") {
            var now = new Date().getTime()
            var passed = now - (session.get("_show") || now);
            session.set("_pass", (session.get("_pass") || 0) + passed);
            session.set("_show", now);
            _startTimer(session)
        }
    }
    var extras = session.getEnv()
    $(safeCallback(function () {
        $("body").on("click", "a", function () {
            $(this).attr("target", function (_, v) {
                if (v == "_blank") return "_self"
            })
        })
        log("dSpider start!")
        session.getConfig = function () {
            return typeof _config === "object" ? _config : {}
        }
        callback(session, extras, $);
    }))
}

$(function () {
    var f = window.onSpiderInited;
    f && f(dSpider.bind(5))
})

var bridge = getJsBridge();
function callHandler() {
    var f = arguments[2];
    if (f) {
        arguments[2] = safeCallback(f)
    }
    return bridge.call.apply(bridge, arguments);
}
function DataSession(key) {
    this.key = key;
    this.finished = false;
    callHandler("start", {sessionKey: key})
}

var getArguments =function () {
    return JSON.parse(callHandler("getArguments")||'{}')
};

DataSession.prototype = {
    _save: function () {
        callHandler("set", {key: this.key, value: JSON.stringify(this.data)})
    },
    _init: function () {
        var data = callHandler("get", {key: this.key});
        this.data = JSON.parse(data || "{}");
        this.local = JSON.parse(callHandler("read", {key: this.key}) || "{}");
    },

    getArguments :getArguments,

    addArgument: function(key,value){
        var t=this.getArguments();
        t[key]=value;
        this.setArguments(t);
    },

    setArguments: function(object){
        callHandler("setArguments",{args:JSON.stringify(object)})
    },

    get: function (key) {
        return this.data[key];
    },
    set: function (key, value) {
        this.data[key] = value;
        this._save();
    },

    showProgress: function (isShow) {
        isShow=isShow === undefined ? true : !!isShow;
        _resetTimer(isShow)
        callHandler("showProgress", {show: isShow});
    },
    setProgressMax: function (max) {
        callHandler("setProgressMax", {progress: max});
    },
    setProgress: function (progress) {
        callHandler("setProgress", {progress: progress});
    },
    setProgressMsg: function (msg) {
        if (!msg) return;
        callHandler("setProgressMsg", {msg: msg})
    },
    finish: function (errmsg, content, code, stack) {
        var ret = {sessionKey: this.key, result: 0, msg: ""}
        var _log=this.get("__log");
        _log=_log&&("\nLOG: \n"+_log);
        if (errmsg) {
            var ob = {
                url: location.href,
                msg: errmsg,
                args: this.getArguments(),
                log:_log,
                network:this.getEnv().network,
                content: content||undefined,
            }
            stack && (ob.stack = stack);
            ret.result = code || 2;
            ret.msg = JSON.stringify(ob);
        }
        this.finished = true;
        callHandler("finish", ret);

    },
    getEnv: function(){
        return JSON.parse(callHandler("getExtraData")||"{}");
    },
    upload: function (value) {
        if (value instanceof Object) {
            value = JSON.stringify(value);
        }
        callHandler("push", {"sessionKey": this.key, "value": value});
    },
    push:function(value){
        this.upload(value)
    },
    load: function (url, headers) {
        headers = headers || {}
        if (typeof headers !== "object") {
            alert("the second argument of function load  must be Object!")
            return
        }
        callHandler("load", {url: url, headers: headers});
    },
    setUserAgent: function (str) {
        callHandler("setUserAgent", {userAgent: str})
    },

    autoLoadImg: function (load) {
        callHandler("autoLoadImg", {load: load === true})
    },

    string: function () {
        log(this.data)
    },
    log: function (str, type) {
        str = _logstr(str);
        console.log("dSpider: " + str)
        if(type!==-1) {
            this.set("__log", (this.get("__log")||"") + "> " + str+"\n");
        }
        callHandler("log", {type: type || 1, msg: str})
    },
    setLocal: function (k, v) {
        this.local[k] = v;
        callHandler("save", {key: this.key, value: JSON.stringify(this.local)})
    },
    getLocal: function (k) {
        return this.local[k];
    },
    showProgressExcept:function(url){
        this.setStartUrl(url);
    },
    setStartUrl:function(url){
        url=url||location.href;
        callHandler("showProgressExcept", {url: url})
    }
};
var withCheck = function (attr) {
    var f = DataSession.prototype[attr];
    return function () {
        if (this.finished) {
            console.log("dSpider: call " + attr + " ignored, since finish has been called! ")
        } else {
            return f.apply(this, arguments);
        }
    }
}
for (var attr in DataSession.prototype) {
    DataSession.prototype[attr] = withCheck(attr);
}