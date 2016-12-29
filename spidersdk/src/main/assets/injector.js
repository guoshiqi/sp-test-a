function init(){
    dQuery.noConflict();
    if (!window.inject) {
        window.inject=true;
        var js = document.createElement("script");
        js.setAttribute("type","application/javascript");
        js.src = "dspider/spider?t="+new Date().getTime();
        var parent= document.head||document.body;
        parent.appendChild(js);
        console.log("dSpider init succeed: "+location.href);
    };
};

var jq = document.createElement("script");
jq.src="dspider/dQuery";
jq.onload=function(){
    init();
};
var timer=setInterval(function(){
    var parent= document.head||document.body;
    if(parent){
     clearInterval(timer);
     parent.appendChild(jq);
    }
 },16);
