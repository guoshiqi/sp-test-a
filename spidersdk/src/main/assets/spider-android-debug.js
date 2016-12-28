/**
 * Created by du on 16/9/1.
 */

var $ = dQuery;
function _logstr(str){
    str=str||" "
    return typeof str=="object"?JSON.stringify(str):(new String(str)).toString()
}
function log(str) {
    var s= window.curSession
    if(s){
        s.log(str)
    }else {
        console.log("dSpider: "+_logstr(str))
    }
}

//异常捕获
function errorReport(e) {
    var stack = e.stack ? e.stack.replace(/http.*?inject\.php.*?:/ig, " " + _su + ":") : e.toString();
    var msg = "语法错误: " + e.message + "\nscript_url:" + _su + "\n" + stack
    if (window.curSession) {
        curSession.log(msg);
        curSession.finish(e.message, "", 3, msg);
    }
}

String.prototype.endWith = function (str) {
    if (!str) return false;
    return this.substring(this.length - str.length) === str;
};

//queryString helper
window.qs = [];
var s = decodeURI(location.search.substr(1));
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
            t = arguments[0];
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

//$,api加载成功的标志是window.xyApiLoaded=true,所有操作都必须在初始化成功之后
function apiInit() {
    $.noConflict();
    var withCheck = function (attr) {
        var f = DataSession.prototype[attr];
        return function () {
            if (this.finished) {
                log("call " + attr + " ignored, finish has been called! ")
            } else {
                return f.apply(this, arguments);
            }
        }
    }

    for (var attr in DataSession.prototype) {
        DataSession.prototype[attr] = withCheck(attr);
    }
    var t = setInterval(function () {
        if (!(window._xy || window.bridge)) {
            return;
        }
        window.xyApiLoaded = true;
        clearInterval(t);
    }, 20);
}

//爬取入口
function dSpider(sessionKey, callback) {
    //判断调用源,如果是在onSpiderInited中调用,则下发脚本中的dSpider函数不执行
    if (window.onSpiderInited && this !=5) {
        return;
    }
    var t = setInterval(function () {
        if (window.xyApiLoaded) {
            clearInterval(t);
        } else {
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
        session._init(function () {
            DataSession.getExtraData(function (extras) {
                extras = JSON.parse(extras || "{}")
                DataSession.getArguments(function (args) {
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
                        session.getArguments = function () {
                            return JSON.parse(args)
                        }
                        callback(session, extras, $);
                    }))
                })
            })
        })
    }, 20);
}


$(function () {
    var f = window.onSpiderInited;
    f&&f(dSpider.bind(5))
})

//邮件爬取入口
function dSpiderMail(sessionKey, callback) {
    dSpider(sessionKey, function (session, env, $) {
        callback(session.getLocal("u"), session.getLocal("wd"), session, env, $);
    })
}

function DataSession(key) {
    this.key = key;
    this.finished = false;
    _xy.start(key);
}

DataSession.getExtraData = function (f) {
    f = safeCallback(f);
    f && f(_xy.getExtraData());
}

DataSession.getArguments = function (f) {
    f = safeCallback(f);
    f && f(_xy.getArguments());
}

DataSession.prototype = {
    _save: function () {
        _xy.set(this.key, JSON.stringify(this.data));
        _xy.save(this.key,JSON.stringify(this.local))
    },
    _init: function (f) {
        this.data = JSON.parse(_xy.get(this.key) || "{}");
        this.local=JSON.parse(_xy.read(this.key)|| "{}")
        f()
    },

    get: function (key) {
        return this.data[key];
    },
    set: function (key, value) {
        this.data[key] = value;
    },

    showProgress: function (isShow) {
        _xy.showProgress(isShow === undefined ? true : !!isShow);
    },
    setProgressMax: function (max) {
        _xy.setProgressMax(max);
    },
    setProgress: function (progress) {
        _xy.setProgress(progress);
    },
    setProgressMsg:function(str){
        if(!str) return;
        _xy.setProgressMsg(str);
    },

    finish: function (errmsg, content, code, stack) {
        this.finished = true;
        if (errmsg) {
            var ob = {
                url: location.href,
                msg: errmsg,
                //content: content || document.documentElement.outerHTML,
                args: this.getArguments()
            }
            stack && (ob.stack = stack);
            return _xy.finish(this.key || "", code || 2, JSON.stringify(ob));
        }
        return _xy.finish(this.key || "", 0, "")
    },
    upload: function (value) {
        if (value instanceof Object) {
            value = JSON.stringify(value);
        }
        return _xy.push(this.key, value)
    },
    load: function (url, headers) {
        headers = headers || {}
        if (typeof headers !== "object") {
            alert("the second argument of function load  must be Object!")
            return
        }
        _xy.load(url, JSON.stringify(headers));
    },
    setUserAgent: function (str) {
        _xy.setUserAgent(str)
    },
    autoLoadImg: function (load) {
        _xy.autoLoadImg(load === true)
    },
    string: function () {
        log(this.data)
    },
    log: function(str,type) {
        str=_logstr(str);
        console.log("dSpider: "+str)
        _xy.log(str,type||1)
    },
    setLocal: function (k, v) {
        this.local[k]=v
    },
    getLocal: function (k) {
        return this.local[k];
    }
};
apiInit();
