function init(){
    dQuery.noConflict();
    if (!window.inject) {
        window.inject=true;
        var js = document.createElement("script");
        js.setAttribute("type","application/javascript");
        js.src = "xiaoying/inject.php?t="+new Date().getTime()+"&refer="+encodeURIComponent(location.href);
        var parent= document.head||document.body;
        parent.appendChild(js);
        console.log("inject succeed!");
    };
};

var jq = document.createElement("script");
jq.src="xiaoying/jquery.min.js";
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
