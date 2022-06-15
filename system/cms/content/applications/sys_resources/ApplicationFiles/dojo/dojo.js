/*
	Copyright (c) 2004-2006, The Dojo Foundation
	All Rights Reserved.

	Licensed under the Academic Free License version 2.1 or above OR the
	modified BSD license. For more information on Dojo licensing, see:

		http://dojotoolkit.org/community/licensing.shtml
*/

/*
	This is a compiled version of Dojo, built for deployment and not for
	development. To get an editable version, please visit:

		http://dojotoolkit.org

	for documentation and information on getting the source.
*/

if(typeof dojo === "undefined"){
var dj_global=this;
var dj_currentContext=this;
function dj_undef(_1,_2){
return (typeof (_2||dj_currentContext)[_1]=="undefined");
}
if(dj_undef("djConfig",this)){
var djConfig={};
}
if(dj_undef("dojo",this)){
var dojo={};
}
dojo.global=function(){
return dj_currentContext;
};
dojo.locale=djConfig.locale;
dojo.version={major:0,minor:4,patch:3,flag:"release",revision:Number("$Rev: 8617 $".match(/[0-9]+/)[0]),toString:function(){
with(dojo.version){
return major+"."+minor+"."+patch+flag+" ("+revision+")";
}
}};
dojo.evalProp=function(_3,_4,_5){
if((!_4)||(!_3)){
return undefined;
}
if(!dj_undef(_3,_4)){
return _4[_3];
}
return (_5?(_4[_3]={}):undefined);
};
dojo.parseObjPath=function(_6,_7,_8){
var _9=(_7||dojo.global());
var _a=_6.split(".");
var _b=_a.pop();
for(var i=0,l=_a.length;i<l&&_9;i++){
_9=dojo.evalProp(_a[i],_9,_8);
}
return {obj:_9,prop:_b};
};
dojo.evalObjPath=function(_e,_f){
if(typeof _e!="string"){
return dojo.global();
}
if(_e.indexOf(".")==-1){
return dojo.evalProp(_e,dojo.global(),_f);
}
var ref=dojo.parseObjPath(_e,dojo.global(),_f);
if(ref){
return dojo.evalProp(ref.prop,ref.obj,_f);
}
return null;
};
dojo.errorToString=function(_11){
if(!dj_undef("message",_11)){
return _11.message;
}else{
if(!dj_undef("description",_11)){
return _11.description;
}else{
return _11;
}
}
};
dojo.raise=function(_12,_13){
if(_13){
_12=_12+": "+dojo.errorToString(_13);
}else{
_12=dojo.errorToString(_12);
}
try{
if(djConfig.isDebug){
dojo.hostenv.println("FATAL exception raised: "+_12);
}
}
catch(e){
}
throw _13||Error(_12);
};
dojo.debug=function(){
};
dojo.debugShallow=function(obj){
};
dojo.profile={start:function(){
},end:function(){
},stop:function(){
},dump:function(){
}};
function dj_eval(_15){
return dj_global.eval?dj_global.eval(_15):eval(_15);
}
dojo.unimplemented=function(_16,_17){
var _18="'"+_16+"' not implemented";
if(_17!=null){
_18+=" "+_17;
}
dojo.raise(_18);
};
dojo.deprecated=function(_19,_1a,_1b){
var _1c="DEPRECATED: "+_19;
if(_1a){
_1c+=" "+_1a;
}
if(_1b){
_1c+=" -- will be removed in version: "+_1b;
}
dojo.debug(_1c);
};
dojo.render=(function(){
function vscaffold(_1d,_1e){
var tmp={capable:false,support:{builtin:false,plugin:false},prefixes:_1d};
for(var i=0;i<_1e.length;i++){
tmp[_1e[i]]=false;
}
return tmp;
}
return {name:"",ver:dojo.version,os:{win:false,linux:false,osx:false},html:vscaffold(["html"],["ie","opera","khtml","safari","moz"]),svg:vscaffold(["svg"],["corel","adobe","batik"]),vml:vscaffold(["vml"],["ie"]),swf:vscaffold(["Swf","Flash","Mm"],["mm"]),swt:vscaffold(["Swt"],["ibm"])};
})();
dojo.hostenv=(function(){
var _21={isDebug:false,allowQueryConfig:false,baseScriptUri:"",baseRelativePath:"",libraryScriptUri:"",iePreventClobber:false,ieClobberMinimal:true,preventBackButtonFix:true,delayMozLoadingFix:false,searchIds:[],parseWidgets:true};
if(typeof djConfig=="undefined"){
djConfig=_21;
}else{
for(var _22 in _21){
if(typeof djConfig[_22]=="undefined"){
djConfig[_22]=_21[_22];
}
}
}
return {name_:"(unset)",version_:"(unset)",getName:function(){
return this.name_;
},getVersion:function(){
return this.version_;
},getText:function(uri){
dojo.unimplemented("getText","uri="+uri);
}};
})();
dojo.hostenv.getBaseScriptUri=function(){
if(djConfig.baseScriptUri.length){
return djConfig.baseScriptUri;
}
var uri=new String(djConfig.libraryScriptUri||djConfig.baseRelativePath);
if(!uri){
dojo.raise("Nothing returned by getLibraryScriptUri(): "+uri);
}
var _25=uri.lastIndexOf("/");
djConfig.baseScriptUri=djConfig.baseRelativePath;
return djConfig.baseScriptUri;
};
(function(){
var _26={pkgFileName:"__package__",loading_modules_:{},loaded_modules_:{},addedToLoadingCount:[],removedFromLoadingCount:[],inFlightCount:0,modulePrefixes_:{dojo:{name:"dojo",value:"src"}},setModulePrefix:function(_27,_28){
this.modulePrefixes_[_27]={name:_27,value:_28};
},moduleHasPrefix:function(_29){
var mp=this.modulePrefixes_;
return Boolean(mp[_29]&&mp[_29].value);
},getModulePrefix:function(_2b){
if(this.moduleHasPrefix(_2b)){
return this.modulePrefixes_[_2b].value;
}
return _2b;
},getTextStack:[],loadUriStack:[],loadedUris:[],post_load_:false,modulesLoadedListeners:[],unloadListeners:[],loadNotifying:false};
for(var _2c in _26){
dojo.hostenv[_2c]=_26[_2c];
}
})();
dojo.hostenv.loadPath=function(_2d,_2e,cb){
var uri;
if(_2d.charAt(0)=="/"||_2d.match(/^\w+:/)){
uri=_2d;
}else{
uri=this.getBaseScriptUri()+_2d;
}
if(djConfig.cacheBust&&dojo.render.html.capable){
uri+="?"+String(djConfig.cacheBust).replace(/\W+/g,"");
}
try{
return !_2e?this.loadUri(uri,cb):this.loadUriAndCheck(uri,_2e,cb);
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.hostenv.loadUri=function(uri,cb){
if(this.loadedUris[uri]){
return true;
}
var _33=this.getText(uri,null,true);
if(!_33){
return false;
}
this.loadedUris[uri]=true;
if(cb){
_33="("+_33+")";
}
var _34=dj_eval(_33);
if(cb){
cb(_34);
}
return true;
};
dojo.hostenv.loadUriAndCheck=function(uri,_36,cb){
var ok=true;
try{
ok=this.loadUri(uri,cb);
}
catch(e){
dojo.debug("failed loading ",uri," with error: ",e);
}
return Boolean(ok&&this.findModule(_36,false));
};
dojo.loaded=function(){
};
dojo.unloaded=function(){
};
dojo.hostenv.loaded=function(){
this.loadNotifying=true;
this.post_load_=true;
var mll=this.modulesLoadedListeners;
for(var x=0;x<mll.length;x++){
mll[x]();
}
this.modulesLoadedListeners=[];
this.loadNotifying=false;
dojo.loaded();
};
dojo.hostenv.unloaded=function(){
var mll=this.unloadListeners;
while(mll.length){
(mll.pop())();
}
dojo.unloaded();
};
dojo.addOnLoad=function(obj,_3d){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.modulesLoadedListeners.push(obj);
}else{
if(arguments.length>1){
dh.modulesLoadedListeners.push(function(){
obj[_3d]();
});
}
}
if(dh.post_load_&&dh.inFlightCount==0&&!dh.loadNotifying){
dh.callLoaded();
}
};
dojo.addOnUnload=function(obj,_40){
var dh=dojo.hostenv;
if(arguments.length==1){
dh.unloadListeners.push(obj);
}else{
if(arguments.length>1){
dh.unloadListeners.push(function(){
obj[_40]();
});
}
}
};
dojo.hostenv.modulesLoaded=function(){
if(this.post_load_){
return;
}
if(this.loadUriStack.length==0&&this.getTextStack.length==0){
if(this.inFlightCount>0){
dojo.debug("files still in flight!");
return;
}
dojo.hostenv.callLoaded();
}
};
dojo.hostenv.callLoaded=function(){
if(typeof setTimeout=="object"||(djConfig["useXDomain"]&&dojo.render.html.opera)){
setTimeout("dojo.hostenv.loaded();",0);
}else{
dojo.hostenv.loaded();
}
};
dojo.hostenv.getModuleSymbols=function(_42){
var _43=_42.split(".");
for(var i=_43.length;i>0;i--){
var _45=_43.slice(0,i).join(".");
if((i==1)&&!this.moduleHasPrefix(_45)){
_43[0]="../"+_43[0];
}else{
var _46=this.getModulePrefix(_45);
if(_46!=_45){
_43.splice(0,i,_46);
break;
}
}
}
return _43;
};
dojo.hostenv._global_omit_module_check=false;
dojo.hostenv.loadModule=function(_47,_48,_49){
if(!_47){
return;
}
_49=this._global_omit_module_check||_49;
var _4a=this.findModule(_47,false);
if(_4a){
return _4a;
}
if(dj_undef(_47,this.loading_modules_)){
this.addedToLoadingCount.push(_47);
}
this.loading_modules_[_47]=1;
var _4b=_47.replace(/\./g,"/")+".js";
var _4c=_47.split(".");
var _4d=this.getModuleSymbols(_47);
var _4e=((_4d[0].charAt(0)!="/")&&!_4d[0].match(/^\w+:/));
var _4f=_4d[_4d.length-1];
var ok;
if(_4f=="*"){
_47=_4c.slice(0,-1).join(".");
while(_4d.length){
_4d.pop();
_4d.push(this.pkgFileName);
_4b=_4d.join("/")+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,!_49?_47:null);
if(ok){
break;
}
_4d.pop();
}
}else{
_4b=_4d.join("/")+".js";
_47=_4c.join(".");
var _51=!_49?_47:null;
ok=this.loadPath(_4b,_51);
if(!ok&&!_48){
_4d.pop();
while(_4d.length){
_4b=_4d.join("/")+".js";
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
_4d.pop();
_4b=_4d.join("/")+"/"+this.pkgFileName+".js";
if(_4e&&_4b.charAt(0)=="/"){
_4b=_4b.slice(1);
}
ok=this.loadPath(_4b,_51);
if(ok){
break;
}
}
}
if(!ok&&!_49){
dojo.raise("Could not load '"+_47+"'; last tried '"+_4b+"'");
}
}
if(!_49&&!this["isXDomain"]){
_4a=this.findModule(_47,false);
if(!_4a){
dojo.raise("symbol '"+_47+"' is not defined after loading '"+_4b+"'");
}
}
return _4a;
};
dojo.hostenv.startPackage=function(_52){
var _53=String(_52);
var _54=_53;
var _55=_52.split(/\./);
if(_55[_55.length-1]=="*"){
_55.pop();
_54=_55.join(".");
}
var _56=dojo.evalObjPath(_54,true);
this.loaded_modules_[_53]=_56;
this.loaded_modules_[_54]=_56;
return _56;
};
dojo.hostenv.findModule=function(_57,_58){
var lmn=String(_57);
if(this.loaded_modules_[lmn]){
return this.loaded_modules_[lmn];
}
if(_58){
dojo.raise("no loaded module named '"+_57+"'");
}
return null;
};
dojo.kwCompoundRequire=function(_5a){
var _5b=_5a["common"]||[];
var _5c=_5a[dojo.hostenv.name_]?_5b.concat(_5a[dojo.hostenv.name_]||[]):_5b.concat(_5a["default"]||[]);
for(var x=0;x<_5c.length;x++){
var _5e=_5c[x];
if(_5e.constructor==Array){
dojo.hostenv.loadModule.apply(dojo.hostenv,_5e);
}else{
dojo.hostenv.loadModule(_5e);
}
}
};
dojo.require=function(_5f){
dojo.hostenv.loadModule.apply(dojo.hostenv,arguments);
};
dojo.requireIf=function(_60,_61){
var _62=arguments[0];
if((_62===true)||(_62=="common")||(_62&&dojo.render[_62].capable)){
var _63=[];
for(var i=1;i<arguments.length;i++){
_63.push(arguments[i]);
}
dojo.require.apply(dojo,_63);
}
};
dojo.requireAfterIf=dojo.requireIf;
dojo.provide=function(_65){
return dojo.hostenv.startPackage.apply(dojo.hostenv,arguments);
};
dojo.registerModulePath=function(_66,_67){
return dojo.hostenv.setModulePrefix(_66,_67);
};
if(djConfig["modulePaths"]){
for(var param in djConfig["modulePaths"]){
dojo.registerModulePath(param,djConfig["modulePaths"][param]);
}
}
dojo.setModulePrefix=function(_68,_69){
dojo.deprecated("dojo.setModulePrefix(\""+_68+"\", \""+_69+"\")","replaced by dojo.registerModulePath","0.5");
return dojo.registerModulePath(_68,_69);
};
dojo.exists=function(obj,_6b){
var p=_6b.split(".");
for(var i=0;i<p.length;i++){
if(!obj[p[i]]){
return false;
}
obj=obj[p[i]];
}
return true;
};
dojo.hostenv.normalizeLocale=function(_6e){
var _6f=_6e?_6e.toLowerCase():dojo.locale;
if(_6f=="root"){
_6f="ROOT";
}
return _6f;
};
dojo.hostenv.searchLocalePath=function(_70,_71,_72){
_70=dojo.hostenv.normalizeLocale(_70);
var _73=_70.split("-");
var _74=[];
for(var i=_73.length;i>0;i--){
_74.push(_73.slice(0,i).join("-"));
}
_74.push(false);
if(_71){
_74.reverse();
}
for(var j=_74.length-1;j>=0;j--){
var loc=_74[j]||"ROOT";
var _78=_72(loc);
if(_78){
break;
}
}
};
dojo.hostenv.localesGenerated;
dojo.hostenv.registerNlsPrefix=function(){
dojo.registerModulePath("nls","nls");
};
dojo.hostenv.preloadLocalizations=function(){
if(dojo.hostenv.localesGenerated){
dojo.hostenv.registerNlsPrefix();
function preload(_79){
_79=dojo.hostenv.normalizeLocale(_79);
dojo.hostenv.searchLocalePath(_79,true,function(loc){
for(var i=0;i<dojo.hostenv.localesGenerated.length;i++){
if(dojo.hostenv.localesGenerated[i]==loc){
dojo["require"]("nls.dojo_"+loc);
return true;
}
}
return false;
});
}
preload();
var _7c=djConfig.extraLocale||[];
for(var i=0;i<_7c.length;i++){
preload(_7c[i]);
}
}
dojo.hostenv.preloadLocalizations=function(){
};
};
dojo.requireLocalization=function(_7e,_7f,_80,_81){
dojo.hostenv.preloadLocalizations();
var _82=dojo.hostenv.normalizeLocale(_80);
var _83=[_7e,"nls",_7f].join(".");
var _84="";
if(_81){
var _85=_81.split(",");
for(var i=0;i<_85.length;i++){
if(_82.indexOf(_85[i])==0){
if(_85[i].length>_84.length){
_84=_85[i];
}
}
}
if(!_84){
_84="ROOT";
}
}
var _87=_81?_84:_82;
var _88=dojo.hostenv.findModule(_83);
var _89=null;
if(_88){
if(djConfig.localizationComplete&&_88._built){
return;
}
var _8a=_87.replace("-","_");
var _8b=_83+"."+_8a;
_89=dojo.hostenv.findModule(_8b);
}
if(!_89){
_88=dojo.hostenv.startPackage(_83);
var _8c=dojo.hostenv.getModuleSymbols(_7e);
var _8d=_8c.concat("nls").join("/");
var _8e;
dojo.hostenv.searchLocalePath(_87,_81,function(loc){
var _90=loc.replace("-","_");
var _91=_83+"."+_90;
var _92=false;
if(!dojo.hostenv.findModule(_91)){
dojo.hostenv.startPackage(_91);
var _93=[_8d];
if(loc!="ROOT"){
_93.push(loc);
}
_93.push(_7f);
var _94=_93.join("/")+".js";
_92=dojo.hostenv.loadPath(_94,null,function(_95){
var _96=function(){
};
_96.prototype=_8e;
_88[_90]=new _96();
for(var j in _95){
_88[_90][j]=_95[j];
}
});
}else{
_92=true;
}
if(_92&&_88[_90]){
_8e=_88[_90];
}else{
_88[_90]=_8e;
}
if(_81){
return true;
}
});
}
if(_81&&_82!=_84){
_88[_82.replace("-","_")]=_88[_84.replace("-","_")];
}
};
(function(){
var _98=djConfig.extraLocale;
if(_98){
if(!_98 instanceof Array){
_98=[_98];
}
var req=dojo.requireLocalization;
dojo.requireLocalization=function(m,b,_9c,_9d){
req(m,b,_9c,_9d);
if(_9c){
return;
}
for(var i=0;i<_98.length;i++){
req(m,b,_98[i],_9d);
}
};
}
})();
}
if(typeof window!="undefined"){
(function(){
if(djConfig.allowQueryConfig){
var _9f=document.location.toString();
var _a0=_9f.split("?",2);
if(_a0.length>1){
var _a1=_a0[1];
var _a2=_a1.split("&");
for(var x in _a2){
var sp=_a2[x].split("=");
if((sp[0].length>9)&&(sp[0].substr(0,9)=="djConfig.")){
var opt=sp[0].substr(9);
try{
djConfig[opt]=eval(sp[1]);
}
catch(e){
djConfig[opt]=sp[1];
}
}
}
}
}
if(((djConfig["baseScriptUri"]=="")||(djConfig["baseRelativePath"]==""))&&(document&&document.getElementsByTagName)){
var _a6=document.getElementsByTagName("script");
var _a7=/(__package__|dojo|bootstrap1)\.js([\?\.]|$)/i;
for(var i=0;i<_a6.length;i++){
var src=_a6[i].getAttribute("src");
if(!src){
continue;
}
var m=src.match(_a7);
if(m){
var _ab=src.substring(0,m.index);
if(src.indexOf("bootstrap1")>-1){
_ab+="../";
}
if(!this["djConfig"]){
djConfig={};
}
if(djConfig["baseScriptUri"]==""){
djConfig["baseScriptUri"]=_ab;
}
if(djConfig["baseRelativePath"]==""){
djConfig["baseRelativePath"]=_ab;
}
break;
}
}
}
var dr=dojo.render;
var drh=dojo.render.html;
var drs=dojo.render.svg;
var dua=(drh.UA=navigator.userAgent);
var dav=(drh.AV=navigator.appVersion);
var t=true;
var f=false;
drh.capable=t;
drh.support.builtin=t;
dr.ver=parseFloat(drh.AV);
dr.os.mac=dav.indexOf("Macintosh")>=0;
dr.os.win=dav.indexOf("Windows")>=0;
dr.os.linux=dav.indexOf("X11")>=0;
drh.opera=dua.indexOf("Opera")>=0;
drh.khtml=(dav.indexOf("Konqueror")>=0)||(dav.indexOf("Safari")>=0);
drh.safari=dav.indexOf("Safari")>=0;
var _b3=dua.indexOf("Gecko");
drh.mozilla=drh.moz=(_b3>=0)&&(!drh.khtml);
if(drh.mozilla){
drh.geckoVersion=dua.substring(_b3+6,_b3+14);
}
drh.ie=(document.all)&&(!drh.opera);
drh.ie50=drh.ie&&dav.indexOf("MSIE 5.0")>=0;
drh.ie55=drh.ie&&dav.indexOf("MSIE 5.5")>=0;
drh.ie60=drh.ie&&dav.indexOf("MSIE 6.0")>=0;
drh.ie70=drh.ie&&dav.indexOf("MSIE 7.0")>=0;
var cm=document["compatMode"];
drh.quirks=(cm=="BackCompat")||(cm=="QuirksMode")||drh.ie55||drh.ie50;
dojo.locale=dojo.locale||(drh.ie?navigator.userLanguage:navigator.language).toLowerCase();
dr.vml.capable=drh.ie;
drs.capable=f;
drs.support.plugin=f;
drs.support.builtin=f;
var _b5=window["document"];
var tdi=_b5["implementation"];
if((tdi)&&(tdi["hasFeature"])&&(tdi.hasFeature("org.w3c.dom.svg","1.0"))){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
if(drh.safari){
var tmp=dua.split("AppleWebKit/")[1];
var ver=parseFloat(tmp.split(" ")[0]);
if(ver>=420){
drs.capable=t;
drs.support.builtin=t;
drs.support.plugin=f;
}
}else{
}
})();
dojo.hostenv.startPackage("dojo.hostenv");
dojo.render.name=dojo.hostenv.name_="browser";
dojo.hostenv.searchIds=[];
dojo.hostenv._XMLHTTP_PROGIDS=["Msxml2.XMLHTTP","Microsoft.XMLHTTP","Msxml2.XMLHTTP.4.0"];
dojo.hostenv.getXmlhttpObject=function(){
var _b9=null;
var _ba=null;
try{
_b9=new XMLHttpRequest();
}
catch(e){
}
if(!_b9){
for(var i=0;i<3;++i){
var _bc=dojo.hostenv._XMLHTTP_PROGIDS[i];
try{
_b9=new ActiveXObject(_bc);
}
catch(e){
_ba=e;
}
if(_b9){
dojo.hostenv._XMLHTTP_PROGIDS=[_bc];
break;
}
}
}
if(!_b9){
return dojo.raise("XMLHTTP not available",_ba);
}
return _b9;
};
dojo.hostenv._blockAsync=false;
dojo.hostenv.getText=function(uri,_be,_bf){
if(!_be){
this._blockAsync=true;
}
var _c0=this.getXmlhttpObject();
function isDocumentOk(_c1){
var _c2=_c1["status"];
return Boolean((!_c2)||((200<=_c2)&&(300>_c2))||(_c2==304));
}
if(_be){
var _c3=this,_c4=null,gbl=dojo.global();
var xhr=dojo.evalObjPath("dojo.io.XMLHTTPTransport");
_c0.onreadystatechange=function(){
if(_c4){
gbl.clearTimeout(_c4);
_c4=null;
}
if(_c3._blockAsync||(xhr&&xhr._blockAsync)){
_c4=gbl.setTimeout(function(){
_c0.onreadystatechange.apply(this);
},10);
}else{
if(4==_c0.readyState){
if(isDocumentOk(_c0)){
_be(_c0.responseText);
}
}
}
};
}
_c0.open("GET",uri.toString(),_be?true:false);
try{
_c0.send(null);
if(_be){
return null;
}
if(!isDocumentOk(_c0)){
var err=Error("Unable to load "+uri+" status:"+_c0.status);
err.status=_c0.status;
err.responseText=_c0.responseText;
throw err;
}
}
catch(e){
this._blockAsync=false;
if((_bf)&&(!_be)){
return null;
}else{
throw e;
}
}
this._blockAsync=false;
return _c0.responseText;
};
dojo.hostenv.defaultDebugContainerId="dojoDebug";
dojo.hostenv._println_buffer=[];
dojo.hostenv._println_safe=false;
dojo.hostenv.println=function(_c8){
if(!dojo.hostenv._println_safe){
dojo.hostenv._println_buffer.push(_c8);
}else{
try{
var _c9=document.getElementById(djConfig.debugContainerId?djConfig.debugContainerId:dojo.hostenv.defaultDebugContainerId);
if(!_c9){
_c9=dojo.body();
}
var div=document.createElement("div");
div.appendChild(document.createTextNode(_c8));
_c9.appendChild(div);
}
catch(e){
try{
document.write("<div>"+_c8+"</div>");
}
catch(e2){
window.status=_c8;
}
}
}
};
dojo.addOnLoad(function(){
dojo.hostenv._println_safe=true;
while(dojo.hostenv._println_buffer.length>0){
dojo.hostenv.println(dojo.hostenv._println_buffer.shift());
}
});
function dj_addNodeEvtHdlr(_cb,_cc,fp){
var _ce=_cb["on"+_cc]||function(){
};
_cb["on"+_cc]=function(){
fp.apply(_cb,arguments);
_ce.apply(_cb,arguments);
};
return true;
}
dojo.hostenv._djInitFired=false;
function dj_load_init(e){
dojo.hostenv._djInitFired=true;
var _d0=(e&&e.type)?e.type.toLowerCase():"load";
if(arguments.callee.initialized||(_d0!="domcontentloaded"&&_d0!="load")){
return;
}
arguments.callee.initialized=true;
if(typeof (_timer)!="undefined"){
clearInterval(_timer);
delete _timer;
}
var _d1=function(){
if(dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
};
if(dojo.hostenv.inFlightCount==0){
_d1();
dojo.hostenv.modulesLoaded();
}else{
dojo.hostenv.modulesLoadedListeners.unshift(_d1);
}
}
if(document.addEventListener){
if(dojo.render.html.opera||(dojo.render.html.moz&&(djConfig["enableMozDomContentLoaded"]===true))){
document.addEventListener("DOMContentLoaded",dj_load_init,null);
}
window.addEventListener("load",dj_load_init,null);
}
if(dojo.render.html.ie&&dojo.render.os.win){
document.attachEvent("onreadystatechange",function(e){
if(document.readyState=="complete"){
dj_load_init();
}
});
}
if(/(WebKit|khtml)/i.test(navigator.userAgent)){
var _timer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
dj_load_init();
}
},10);
}
if(dojo.render.html.ie){
dj_addNodeEvtHdlr(window,"beforeunload",function(){
dojo.hostenv._unloading=true;
window.setTimeout(function(){
dojo.hostenv._unloading=false;
},0);
});
}
dj_addNodeEvtHdlr(window,"unload",function(){
dojo.hostenv.unloaded();
if((!dojo.render.html.ie)||(dojo.render.html.ie&&dojo.hostenv._unloading)){
dojo.hostenv.unloaded();
}
});
dojo.hostenv.makeWidgets=function(){
var _d3=[];
if(djConfig.searchIds&&djConfig.searchIds.length>0){
_d3=_d3.concat(djConfig.searchIds);
}
if(dojo.hostenv.searchIds&&dojo.hostenv.searchIds.length>0){
_d3=_d3.concat(dojo.hostenv.searchIds);
}
if((djConfig.parseWidgets)||(_d3.length>0)){
if(dojo.evalObjPath("dojo.widget.Parse")){
var _d4=new dojo.xml.Parse();
if(_d3.length>0){
for(var x=0;x<_d3.length;x++){
var _d6=document.getElementById(_d3[x]);
if(!_d6){
continue;
}
var _d7=_d4.parseElement(_d6,null,true);
dojo.widget.getParser().createComponents(_d7);
}
}else{
if(djConfig.parseWidgets){
var _d7=_d4.parseElement(dojo.body(),null,true);
dojo.widget.getParser().createComponents(_d7);
}
}
}
}
};
dojo.addOnLoad(function(){
if(!dojo.render.html.ie){
dojo.hostenv.makeWidgets();
}
});
try{
if(dojo.render.html.ie){
document.namespaces.add("v","urn:schemas-microsoft-com:vml");
document.createStyleSheet().addRule("v\\:*","behavior:url(#default#VML)");
}
}
catch(e){
}
dojo.hostenv.writeIncludes=function(){
};
if(!dj_undef("document",this)){
dj_currentDocument=this.document;
}
dojo.doc=function(){
return dj_currentDocument;
};
dojo.body=function(){
return dojo.doc().body||dojo.doc().getElementsByTagName("body")[0];
};
dojo.byId=function(id,doc){
if((id)&&((typeof id=="string")||(id instanceof String))){
if(!doc){
doc=dj_currentDocument;
}
var ele=doc.getElementById(id);
if(ele&&(ele.id!=id)&&doc.all){
ele=null;
eles=doc.all[id];
if(eles){
if(eles.length){
for(var i=0;i<eles.length;i++){
if(eles[i].id==id){
ele=eles[i];
break;
}
}
}else{
ele=eles;
}
}
}
return ele;
}
return id;
};
dojo.setContext=function(_dc,_dd){
dj_currentContext=_dc;
dj_currentDocument=_dd;
};
dojo._fireCallback=function(_de,_df,_e0){
if((_df)&&((typeof _de=="string")||(_de instanceof String))){
_de=_df[_de];
}
return (_df?_de.apply(_df,_e0||[]):_de());
};
dojo.withGlobal=function(_e1,_e2,_e3,_e4){
var _e5;
var _e6=dj_currentContext;
var _e7=dj_currentDocument;
try{
dojo.setContext(_e1,_e1.document);
_e5=dojo._fireCallback(_e2,_e3,_e4);
}
finally{
dojo.setContext(_e6,_e7);
}
return _e5;
};
dojo.withDoc=function(_e8,_e9,_ea,_eb){
var _ec;
var _ed=dj_currentDocument;
try{
dj_currentDocument=_e8;
_ec=dojo._fireCallback(_e9,_ea,_eb);
}
finally{
dj_currentDocument=_ed;
}
return _ec;
};
}
dojo.requireIf((djConfig["isDebug"]||djConfig["debugAtAllCosts"]),"dojo.debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&!djConfig["useXDomain"],"dojo.browser_debug");
dojo.requireIf(djConfig["debugAtAllCosts"]&&!window.widget&&djConfig["useXDomain"],"dojo.browser_debug_xd");
dojo.provide("dojo.collections.Collections");
dojo.collections.DictionaryEntry=function(k,v){
this.key=k;
this.value=v;
this.valueOf=function(){
return this.value;
};
this.toString=function(){
return String(this.value);
};
};
dojo.collections.Iterator=function(arr){
var a=arr;
var _f2=0;
this.element=a[_f2]||null;
this.atEnd=function(){
return (_f2>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_f2++];
return this.element;
};
this.map=function(fn,_f4){
var s=_f4||dj_global;
if(Array.map){
return Array.map(a,fn,s);
}else{
var arr=[];
for(var i=0;i<a.length;i++){
arr.push(fn.call(s,a[i]));
}
return arr;
}
};
this.reset=function(){
_f2=0;
this.element=a[_f2];
};
};
dojo.collections.DictionaryIterator=function(obj){
var a=[];
var _fa={};
for(var p in obj){
if(!_fa[p]){
a.push(obj[p]);
}
}
var _fc=0;
this.element=a[_fc]||null;
this.atEnd=function(){
return (_fc>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_fc++];
return this.element;
};
this.map=function(fn,_fe){
var s=_fe||dj_global;
if(Array.map){
return Array.map(a,fn,s);
}else{
var arr=[];
for(var i=0;i<a.length;i++){
arr.push(fn.call(s,a[i]));
}
return arr;
}
};
this.reset=function(){
_fc=0;
this.element=a[_fc];
};
};
dojo.provide("dojo.collections.ArrayList");
dojo.collections.ArrayList=function(arr){
var _103=[];
if(arr){
_103=_103.concat(arr);
}
this.count=_103.length;
this.add=function(obj){
_103.push(obj);
this.count=_103.length;
};
this.addRange=function(a){
if(a.getIterator){
var e=a.getIterator();
while(!e.atEnd()){
this.add(e.get());
}
this.count=_103.length;
}else{
for(var i=0;i<a.length;i++){
_103.push(a[i]);
}
this.count=_103.length;
}
};
this.clear=function(){
_103.splice(0,_103.length);
this.count=0;
};
this.clone=function(){
return new dojo.collections.ArrayList(_103);
};
this.contains=function(obj){
for(var i=0;i<_103.length;i++){
if(_103[i]==obj){
return true;
}
}
return false;
};
this.forEach=function(fn,_10b){
var s=_10b||dj_global;
if(Array.forEach){
Array.forEach(_103,fn,s);
}else{
for(var i=0;i<_103.length;i++){
fn.call(s,_103[i],i,_103);
}
}
};
this.getIterator=function(){
return new dojo.collections.Iterator(_103);
};
this.indexOf=function(obj){
for(var i=0;i<_103.length;i++){
if(_103[i]==obj){
return i;
}
}
return -1;
};
this.insert=function(i,obj){
_103.splice(i,0,obj);
this.count=_103.length;
};
this.item=function(i){
return _103[i];
};
this.remove=function(obj){
var i=this.indexOf(obj);
if(i>=0){
_103.splice(i,1);
}
this.count=_103.length;
};
this.removeAt=function(i){
_103.splice(i,1);
this.count=_103.length;
};
this.reverse=function(){
_103.reverse();
};
this.sort=function(fn){
if(fn){
_103.sort(fn);
}else{
_103.sort();
}
};
this.setByIndex=function(i,obj){
_103[i]=obj;
this.count=_103.length;
};
this.toArray=function(){
return [].concat(_103);
};
this.toString=function(_119){
return _103.join((_119||","));
};
};
dojo.provide("dojo.collections.Dictionary");
dojo.collections.Dictionary=function(_11a){
var _11b={};
this.count=0;
var _11c={};
this.add=function(k,v){
var b=(k in _11b);
_11b[k]=new dojo.collections.DictionaryEntry(k,v);
if(!b){
this.count++;
}
};
this.clear=function(){
_11b={};
this.count=0;
};
this.clone=function(){
return new dojo.collections.Dictionary(this);
};
this.contains=this.containsKey=function(k){
if(_11c[k]){
return false;
}
return (_11b[k]!=null);
};
this.containsValue=function(v){
var e=this.getIterator();
while(e.get()){
if(e.element.value==v){
return true;
}
}
return false;
};
this.entry=function(k){
return _11b[k];
};
this.forEach=function(fn,_125){
var a=[];
for(var p in _11b){
if(!_11c[p]){
a.push(_11b[p]);
}
}
var s=_125||dj_global;
if(Array.forEach){
Array.forEach(a,fn,s);
}else{
for(var i=0;i<a.length;i++){
fn.call(s,a[i],i,a);
}
}
};
this.getKeyList=function(){
return (this.getIterator()).map(function(_12a){
return _12a.key;
});
};
this.getValueList=function(){
return (this.getIterator()).map(function(_12b){
return _12b.value;
});
};
this.item=function(k){
if(k in _11b){
return _11b[k].valueOf();
}
return undefined;
};
this.getIterator=function(){
return new dojo.collections.DictionaryIterator(_11b);
};
this.remove=function(k){
if(k in _11b&&!_11c[k]){
delete _11b[k];
this.count--;
return true;
}
return false;
};
if(_11a){
var e=_11a.getIterator();
while(e.get()){
this.add(e.element.key,e.element.value);
}
}
};
dojo.provide("dojo.collections.Stack");
dojo.collections.Stack=function(arr){
var q=[];
if(arr){
q=q.concat(arr);
}
this.count=q.length;
this.clear=function(){
q=[];
this.count=q.length;
};
this.clone=function(){
return new dojo.collections.Stack(q);
};
this.contains=function(o){
for(var i=0;i<q.length;i++){
if(q[i]==o){
return true;
}
}
return false;
};
this.copyTo=function(arr,i){
arr.splice(i,0,q);
};
this.forEach=function(fn,_136){
var s=_136||dj_global;
if(Array.forEach){
Array.forEach(q,fn,s);
}else{
for(var i=0;i<q.length;i++){
fn.call(s,q[i],i,q);
}
}
};
this.getIterator=function(){
return new dojo.collections.Iterator(q);
};
this.peek=function(){
return q[(q.length-1)];
};
this.pop=function(){
var r=q.pop();
this.count=q.length;
return r;
};
this.push=function(o){
this.count=q.push(o);
};
this.toArray=function(){
return [].concat(q);
};
};
dojo.provide("dojo.dom");
dojo.dom.ELEMENT_NODE=1;
dojo.dom.ATTRIBUTE_NODE=2;
dojo.dom.TEXT_NODE=3;
dojo.dom.CDATA_SECTION_NODE=4;
dojo.dom.ENTITY_REFERENCE_NODE=5;
dojo.dom.ENTITY_NODE=6;
dojo.dom.PROCESSING_INSTRUCTION_NODE=7;
dojo.dom.COMMENT_NODE=8;
dojo.dom.DOCUMENT_NODE=9;
dojo.dom.DOCUMENT_TYPE_NODE=10;
dojo.dom.DOCUMENT_FRAGMENT_NODE=11;
dojo.dom.NOTATION_NODE=12;
dojo.dom.dojoml="http://www.dojotoolkit.org/2004/dojoml";
dojo.dom.xmlns={svg:"http://www.w3.org/2000/svg",smil:"http://www.w3.org/2001/SMIL20/",mml:"http://www.w3.org/1998/Math/MathML",cml:"http://www.xml-cml.org",xlink:"http://www.w3.org/1999/xlink",xhtml:"http://www.w3.org/1999/xhtml",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul",xbl:"http://www.mozilla.org/xbl",fo:"http://www.w3.org/1999/XSL/Format",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xi:"http://www.w3.org/2001/XInclude",xforms:"http://www.w3.org/2002/01/xforms",saxon:"http://icl.com/saxon",xalan:"http://xml.apache.org/xslt",xsd:"http://www.w3.org/2001/XMLSchema",dt:"http://www.w3.org/2001/XMLSchema-datatypes",xsi:"http://www.w3.org/2001/XMLSchema-instance",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",wsdl:"http://schemas.xmlsoap.org/wsdl/",AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/"};
dojo.dom.isNode=function(wh){
if(typeof Element=="function"){
try{
return wh instanceof Element;
}
catch(e){
}
}else{
return wh&&!isNaN(wh.nodeType);
}
};
dojo.dom.getUniqueId=function(){
var _13c=dojo.doc();
do{
var id1="dj_unique_"+(++arguments.callee._idIncrement);
}while(_13c.getElementById(id1));
return id1;
};
dojo.dom.getUniqueId._idIncrement=0;
dojo.dom.firstElement=dojo.dom.getFirstChildElement=function(_13e,_13f){
var node=_13e.firstChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.nextSibling;
}
if(_13f&&node&&node.tagName&&node.tagName.toLowerCase()!=_13f.toLowerCase()){
node=dojo.dom.nextElement(node,_13f);
}
return node;
};
dojo.dom.lastElement=dojo.dom.getLastChildElement=function(_141,_142){
var node=_141.lastChild;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.previousSibling;
}
if(_142&&node&&node.tagName&&node.tagName.toLowerCase()!=_142.toLowerCase()){
node=dojo.dom.prevElement(node,_142);
}
return node;
};
dojo.dom.nextElement=dojo.dom.getNextSiblingElement=function(node,_145){
if(!node){
return null;
}
do{
node=node.nextSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_145&&_145.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.nextElement(node,_145);
}
return node;
};
dojo.dom.prevElement=dojo.dom.getPreviousSiblingElement=function(node,_147){
if(!node){
return null;
}
if(_147){
_147=_147.toLowerCase();
}
do{
node=node.previousSibling;
}while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE);
if(node&&_147&&_147.toLowerCase()!=node.tagName.toLowerCase()){
return dojo.dom.prevElement(node,_147);
}
return node;
};
dojo.dom.moveChildren=function(_148,_149,trim){
var _14b=0;
if(trim){
while(_148.hasChildNodes()&&_148.firstChild.nodeType==dojo.dom.TEXT_NODE){
_148.removeChild(_148.firstChild);
}
while(_148.hasChildNodes()&&_148.lastChild.nodeType==dojo.dom.TEXT_NODE){
_148.removeChild(_148.lastChild);
}
}
while(_148.hasChildNodes()){
_149.appendChild(_148.firstChild);
_14b++;
}
return _14b;
};
dojo.dom.copyChildren=function(_14c,_14d,trim){
var _14f=_14c.cloneNode(true);
return this.moveChildren(_14f,_14d,trim);
};
dojo.dom.replaceChildren=function(node,_151){
var _152=[];
if(dojo.render.html.ie){
for(var i=0;i<node.childNodes.length;i++){
_152.push(node.childNodes[i]);
}
}
dojo.dom.removeChildren(node);
node.appendChild(_151);
for(var j=0;j<_152.length;j++){
dojo.dom.destroyNode(_152[j]);
}
};
dojo.dom.removeChildren=function(node){
var _155=node.childNodes.length;
while(node.hasChildNodes()){
dojo.dom.removeNode(node.firstChild);
}
return _155;
};
dojo.dom.replaceNode=function(node,_157){
return node.parentNode.replaceChild(_157,node);
};
dojo.dom.destroyNode=function(node){
if(node.parentNode){
node=dojo.dom.removeNode(node);
}
if(node.nodeType!=3){
if(dojo.evalObjPath("dojo.event.browser.clean",false)){
dojo.event.browser.clean(node);
}
if(dojo.render.html.ie){
node.outerHTML="";
}
}
};
dojo.dom.removeNode=function(node){
if(node&&node.parentNode){
return node.parentNode.removeChild(node);
}
};
dojo.dom.getAncestors=function(node,_15b,_15c){
var _15d=[];
var _15e=(_15b&&(_15b instanceof Function||typeof _15b=="function"));
while(node){
if(!_15e||_15b(node)){
_15d.push(node);
}
if(_15c&&_15d.length>0){
return _15d[0];
}
node=node.parentNode;
}
if(_15c){
return null;
}
return _15d;
};
dojo.dom.getAncestorsByTag=function(node,tag,_161){
tag=tag.toLowerCase();
return dojo.dom.getAncestors(node,function(el){
return ((el.tagName)&&(el.tagName.toLowerCase()==tag));
},_161);
};
dojo.dom.getFirstAncestorByTag=function(node,tag){
return dojo.dom.getAncestorsByTag(node,tag,true);
};
dojo.dom.isDescendantOf=function(node,_166,_167){
if(_167&&node){
node=node.parentNode;
}
while(node){
if(node==_166){
return true;
}
node=node.parentNode;
}
return false;
};
dojo.dom.innerXML=function(node){
if(node.innerXML){
return node.innerXML;
}else{
if(node.xml){
return node.xml;
}else{
if(typeof XMLSerializer!="undefined"){
return (new XMLSerializer()).serializeToString(node);
}
}
}
};
dojo.dom.createDocument=function(){
var doc=null;
var _16a=dojo.doc();
if(!dj_undef("ActiveXObject")){
var _16b=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;i<_16b.length;i++){
try{
doc=new ActiveXObject(_16b[i]+".XMLDOM");
}
catch(e){
}
if(doc){
break;
}
}
}else{
if((_16a.implementation)&&(_16a.implementation.createDocument)){
doc=_16a.implementation.createDocument("","",null);
}
}
return doc;
};
dojo.dom.createDocumentFromText=function(str,_16e){
if(!_16e){
_16e="text/xml";
}
if(!dj_undef("DOMParser")){
var _16f=new DOMParser();
return _16f.parseFromString(str,_16e);
}else{
if(!dj_undef("ActiveXObject")){
var _170=dojo.dom.createDocument();
if(_170){
_170.async=false;
_170.loadXML(str);
return _170;
}else{
dojo.debug("toXml didn't work?");
}
}else{
var _171=dojo.doc();
if(_171.createElement){
var tmp=_171.createElement("xml");
tmp.innerHTML=str;
if(_171.implementation&&_171.implementation.createDocument){
var _173=_171.implementation.createDocument("foo","",null);
for(var i=0;i<tmp.childNodes.length;i++){
_173.importNode(tmp.childNodes.item(i),true);
}
return _173;
}
return ((tmp.document)&&(tmp.document.firstChild?tmp.document.firstChild:tmp));
}
}
}
return null;
};
dojo.dom.prependChild=function(node,_176){
if(_176.firstChild){
_176.insertBefore(node,_176.firstChild);
}else{
_176.appendChild(node);
}
return true;
};
dojo.dom.insertBefore=function(node,ref,_179){
if((_179!=true)&&(node===ref||node.nextSibling===ref)){
return false;
}
var _17a=ref.parentNode;
_17a.insertBefore(node,ref);
return true;
};
dojo.dom.insertAfter=function(node,ref,_17d){
var pn=ref.parentNode;
if(ref==pn.lastChild){
if((_17d!=true)&&(node===ref)){
return false;
}
pn.appendChild(node);
}else{
return this.insertBefore(node,ref.nextSibling,_17d);
}
return true;
};
dojo.dom.insertAtPosition=function(node,ref,_181){
if((!node)||(!ref)||(!_181)){
return false;
}
switch(_181.toLowerCase()){
case "before":
return dojo.dom.insertBefore(node,ref);
case "after":
return dojo.dom.insertAfter(node,ref);
case "first":
if(ref.firstChild){
return dojo.dom.insertBefore(node,ref.firstChild);
}else{
ref.appendChild(node);
return true;
}
break;
default:
ref.appendChild(node);
return true;
}
};
dojo.dom.insertAtIndex=function(node,_183,_184){
var _185=_183.childNodes;
if(!_185.length||_185.length==_184){
_183.appendChild(node);
return true;
}
if(_184==0){
return dojo.dom.prependChild(node,_183);
}
return dojo.dom.insertAfter(node,_185[_184-1]);
};
dojo.dom.textContent=function(node,text){
if(arguments.length>1){
var _188=dojo.doc();
dojo.dom.replaceChildren(node,_188.createTextNode(text));
return text;
}else{
if(node.textContent!=undefined){
return node.textContent;
}
var _189="";
if(node==null){
return _189;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
_189+=dojo.dom.textContent(node.childNodes[i]);
break;
case 3:
case 2:
case 4:
_189+=node.childNodes[i].nodeValue;
break;
default:
break;
}
}
return _189;
}
};
dojo.dom.hasParent=function(node){
return Boolean(node&&node.parentNode&&dojo.dom.isNode(node.parentNode));
};
dojo.dom.isTag=function(node){
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName==String(arguments[i])){
return String(arguments[i]);
}
}
}
return "";
};
dojo.dom.setAttributeNS=function(elem,_18f,_190,_191){
if(elem==null||((elem==undefined)&&(typeof elem=="undefined"))){
dojo.raise("No element given to dojo.dom.setAttributeNS");
}
if(!((elem.setAttributeNS==undefined)&&(typeof elem.setAttributeNS=="undefined"))){
elem.setAttributeNS(_18f,_190,_191);
}else{
var _192=elem.ownerDocument;
var _193=_192.createNode(2,_190,_18f);
_193.nodeValue=_191;
elem.setAttributeNode(_193);
}
};
dojo.provide("dojo.lang.common");
dojo.lang.inherits=function(_194,_195){
if(!dojo.lang.isFunction(_195)){
dojo.raise("dojo.inherits: superclass argument ["+_195+"] must be a function (subclass: ["+_194+"']");
}
_194.prototype=new _195();
_194.prototype.constructor=_194;
_194.superclass=_195.prototype;
_194["super"]=_195.prototype;
};
dojo.lang._mixin=function(obj,_197){
var tobj={};
for(var x in _197){
if((typeof tobj[x]=="undefined")||(tobj[x]!=_197[x])){
obj[x]=_197[x];
}
}
if(dojo.render.html.ie&&(typeof (_197["toString"])=="function")&&(_197["toString"]!=obj["toString"])&&(_197["toString"]!=tobj["toString"])){
obj.toString=_197.toString;
}
return obj;
};
dojo.lang.mixin=function(obj,_19b){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(obj,arguments[i]);
}
return obj;
};
dojo.lang.extend=function(_19e,_19f){
for(var i=1,l=arguments.length;i<l;i++){
dojo.lang._mixin(_19e.prototype,arguments[i]);
}
return _19e;
};
dojo.inherits=dojo.lang.inherits;
dojo.mixin=dojo.lang.mixin;
dojo.extend=dojo.lang.extend;
dojo.lang.find=function(_1a2,_1a3,_1a4,_1a5){
if(!dojo.lang.isArrayLike(_1a2)&&dojo.lang.isArrayLike(_1a3)){
dojo.deprecated("dojo.lang.find(value, array)","use dojo.lang.find(array, value) instead","0.5");
var temp=_1a2;
_1a2=_1a3;
_1a3=temp;
}
var _1a7=dojo.lang.isString(_1a2);
if(_1a7){
_1a2=_1a2.split("");
}
if(_1a5){
var step=-1;
var i=_1a2.length-1;
var end=-1;
}else{
var step=1;
var i=0;
var end=_1a2.length;
}
if(_1a4){
while(i!=end){
if(_1a2[i]===_1a3){
return i;
}
i+=step;
}
}else{
while(i!=end){
if(_1a2[i]==_1a3){
return i;
}
i+=step;
}
}
return -1;
};
dojo.lang.indexOf=dojo.lang.find;
dojo.lang.findLast=function(_1ab,_1ac,_1ad){
return dojo.lang.find(_1ab,_1ac,_1ad,true);
};
dojo.lang.lastIndexOf=dojo.lang.findLast;
dojo.lang.inArray=function(_1ae,_1af){
return dojo.lang.find(_1ae,_1af)>-1;
};
dojo.lang.isObject=function(it){
if(typeof it=="undefined"){
return false;
}
return (typeof it=="object"||it===null||dojo.lang.isArray(it)||dojo.lang.isFunction(it));
};
dojo.lang.isArray=function(it){
return (it&&it instanceof Array||typeof it=="array");
};
dojo.lang.isArrayLike=function(it){
if((!it)||(dojo.lang.isUndefined(it))){
return false;
}
if(dojo.lang.isString(it)){
return false;
}
if(dojo.lang.isFunction(it)){
return false;
}
if(dojo.lang.isArray(it)){
return true;
}
if((it.tagName)&&(it.tagName.toLowerCase()=="form")){
return false;
}
if(dojo.lang.isNumber(it.length)&&isFinite(it.length)){
return true;
}
return false;
};
dojo.lang.isFunction=function(it){
return (it instanceof Function||typeof it=="function");
};
(function(){
if((dojo.render.html.capable)&&(dojo.render.html["safari"])){
dojo.lang.isFunction=function(it){
if((typeof (it)=="function")&&(it=="[object NodeList]")){
return false;
}
return (it instanceof Function||typeof it=="function");
};
}
})();
dojo.lang.isString=function(it){
return (typeof it=="string"||it instanceof String);
};
dojo.lang.isAlien=function(it){
if(!it){
return false;
}
return !dojo.lang.isFunction(it)&&/\{\s*\[native code\]\s*\}/.test(String(it));
};
dojo.lang.isBoolean=function(it){
return (it instanceof Boolean||typeof it=="boolean");
};
dojo.lang.isNumber=function(it){
return (it instanceof Number||typeof it=="number");
};
dojo.lang.isUndefined=function(it){
return ((typeof (it)=="undefined")&&(it==undefined));
};
dojo.provide("dojo.lang.array");
dojo.lang.mixin(dojo.lang,{has:function(obj,name){
try{
return typeof obj[name]!="undefined";
}
catch(e){
return false;
}
},isEmpty:function(obj){
if(dojo.lang.isObject(obj)){
var tmp={};
var _1be=0;
for(var x in obj){
if(obj[x]&&(!tmp[x])){
_1be++;
break;
}
}
return _1be==0;
}else{
if(dojo.lang.isArrayLike(obj)||dojo.lang.isString(obj)){
return obj.length==0;
}
}
},map:function(arr,obj,_1c2){
var _1c3=dojo.lang.isString(arr);
if(_1c3){
arr=arr.split("");
}
if(dojo.lang.isFunction(obj)&&(!_1c2)){
_1c2=obj;
obj=dj_global;
}else{
if(dojo.lang.isFunction(obj)&&_1c2){
var _1c4=obj;
obj=_1c2;
_1c2=_1c4;
}
}
if(Array.map){
var _1c5=Array.map(arr,_1c2,obj);
}else{
var _1c5=[];
for(var i=0;i<arr.length;++i){
_1c5.push(_1c2.call(obj,arr[i]));
}
}
if(_1c3){
return _1c5.join("");
}else{
return _1c5;
}
},reduce:function(arr,_1c8,obj,_1ca){
var _1cb=_1c8;
if(arguments.length==2){
_1ca=_1c8;
_1cb=arr[0];
arr=arr.slice(1);
}else{
if(arguments.length==3){
if(dojo.lang.isFunction(obj)){
_1ca=obj;
obj=null;
}
}else{
if(dojo.lang.isFunction(obj)){
var tmp=_1ca;
_1ca=obj;
obj=tmp;
}
}
}
var ob=obj||dj_global;
dojo.lang.map(arr,function(val){
_1cb=_1ca.call(ob,_1cb,val);
});
return _1cb;
},forEach:function(_1cf,_1d0,_1d1){
if(dojo.lang.isString(_1cf)){
_1cf=_1cf.split("");
}
if(Array.forEach){
Array.forEach(_1cf,_1d0,_1d1);
}else{
if(!_1d1){
_1d1=dj_global;
}
for(var i=0,l=_1cf.length;i<l;i++){
_1d0.call(_1d1,_1cf[i],i,_1cf);
}
}
},_everyOrSome:function(_1d4,arr,_1d6,_1d7){
if(dojo.lang.isString(arr)){
arr=arr.split("");
}
if(Array.every){
return Array[_1d4?"every":"some"](arr,_1d6,_1d7);
}else{
if(!_1d7){
_1d7=dj_global;
}
for(var i=0,l=arr.length;i<l;i++){
var _1da=_1d6.call(_1d7,arr[i],i,arr);
if(_1d4&&!_1da){
return false;
}else{
if((!_1d4)&&(_1da)){
return true;
}
}
}
return Boolean(_1d4);
}
},every:function(arr,_1dc,_1dd){
return this._everyOrSome(true,arr,_1dc,_1dd);
},some:function(arr,_1df,_1e0){
return this._everyOrSome(false,arr,_1df,_1e0);
},filter:function(arr,_1e2,_1e3){
var _1e4=dojo.lang.isString(arr);
if(_1e4){
arr=arr.split("");
}
var _1e5;
if(Array.filter){
_1e5=Array.filter(arr,_1e2,_1e3);
}else{
if(!_1e3){
if(arguments.length>=3){
dojo.raise("thisObject doesn't exist!");
}
_1e3=dj_global;
}
_1e5=[];
for(var i=0;i<arr.length;i++){
if(_1e2.call(_1e3,arr[i],i,arr)){
_1e5.push(arr[i]);
}
}
}
if(_1e4){
return _1e5.join("");
}else{
return _1e5;
}
},unnest:function(){
var out=[];
for(var i=0;i<arguments.length;i++){
if(dojo.lang.isArrayLike(arguments[i])){
var add=dojo.lang.unnest.apply(this,arguments[i]);
out=out.concat(add);
}else{
out.push(arguments[i]);
}
}
return out;
},toArray:function(_1ea,_1eb){
var _1ec=[];
for(var i=_1eb||0;i<_1ea.length;i++){
_1ec.push(_1ea[i]);
}
return _1ec;
}});
dojo.provide("dojo.lang.extras");
dojo.lang.setTimeout=function(func,_1ef){
var _1f0=window,_1f1=2;
if(!dojo.lang.isFunction(func)){
_1f0=func;
func=_1ef;
_1ef=arguments[2];
_1f1++;
}
if(dojo.lang.isString(func)){
func=_1f0[func];
}
var args=[];
for(var i=_1f1;i<arguments.length;i++){
args.push(arguments[i]);
}
return dojo.global().setTimeout(function(){
func.apply(_1f0,args);
},_1ef);
};
dojo.lang.clearTimeout=function(_1f4){
dojo.global().clearTimeout(_1f4);
};
dojo.lang.getNameInObj=function(ns,item){
if(!ns){
ns=dj_global;
}
for(var x in ns){
if(ns[x]===item){
return new String(x);
}
}
return null;
};
dojo.lang.shallowCopy=function(obj,deep){
var i,ret;
if(obj===null){
return null;
}
if(dojo.lang.isObject(obj)){
ret=new obj.constructor();
for(i in obj){
if(dojo.lang.isUndefined(ret[i])){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}
}else{
if(dojo.lang.isArray(obj)){
ret=[];
for(i=0;i<obj.length;i++){
ret[i]=deep?dojo.lang.shallowCopy(obj[i],deep):obj[i];
}
}else{
ret=obj;
}
}
return ret;
};
dojo.lang.firstValued=function(){
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]!="undefined"){
return arguments[i];
}
}
return undefined;
};
dojo.lang.getObjPathValue=function(_1fd,_1fe,_1ff){
with(dojo.parseObjPath(_1fd,_1fe,_1ff)){
return dojo.evalProp(prop,obj,_1ff);
}
};
dojo.lang.setObjPathValue=function(_200,_201,_202,_203){
dojo.deprecated("dojo.lang.setObjPathValue","use dojo.parseObjPath and the '=' operator","0.6");
if(arguments.length<4){
_203=true;
}
with(dojo.parseObjPath(_200,_202,_203)){
if(obj&&(_203||(prop in obj))){
obj[prop]=_201;
}
}
};
dojo.provide("dojo.lang.func");
dojo.lang.hitch=function(_204,_205){
var args=[];
for(var x=2;x<arguments.length;x++){
args.push(arguments[x]);
}
var fcn=(dojo.lang.isString(_205)?_204[_205]:_205)||function(){
};
return function(){
var ta=args.concat([]);
for(var x=0;x<arguments.length;x++){
ta.push(arguments[x]);
}
return fcn.apply(_204,ta);
};
};
dojo.lang.anonCtr=0;
dojo.lang.anon={};
dojo.lang.nameAnonFunc=function(_20b,_20c,_20d){
var nso=(_20c||dojo.lang.anon);
if((_20d)||((dj_global["djConfig"])&&(djConfig["slowAnonFuncLookups"]==true))){
for(var x in nso){
try{
if(nso[x]===_20b){
return x;
}
}
catch(e){
}
}
}
var ret="__"+dojo.lang.anonCtr++;
while(typeof nso[ret]!="undefined"){
ret="__"+dojo.lang.anonCtr++;
}
nso[ret]=_20b;
return ret;
};
dojo.lang.forward=function(_211){
return function(){
return this[_211].apply(this,arguments);
};
};
dojo.lang.curry=function(_212,func){
var _214=[];
_212=_212||dj_global;
if(dojo.lang.isString(func)){
func=_212[func];
}
for(var x=2;x<arguments.length;x++){
_214.push(arguments[x]);
}
var _216=(func["__preJoinArity"]||func.length)-_214.length;
function gather(_217,_218,_219){
var _21a=_219;
var _21b=_218.slice(0);
for(var x=0;x<_217.length;x++){
_21b.push(_217[x]);
}
_219=_219-_217.length;
if(_219<=0){
var res=func.apply(_212,_21b);
_219=_21a;
return res;
}else{
return function(){
return gather(arguments,_21b,_219);
};
}
}
return gather([],_214,_216);
};
dojo.lang.curryArguments=function(_21e,func,args,_221){
var _222=[];
var x=_221||0;
for(x=_221;x<args.length;x++){
_222.push(args[x]);
}
return dojo.lang.curry.apply(dojo.lang,[_21e,func].concat(_222));
};
dojo.lang.tryThese=function(){
for(var x=0;x<arguments.length;x++){
try{
if(typeof arguments[x]=="function"){
var ret=(arguments[x]());
if(ret){
return ret;
}
}
}
catch(e){
dojo.debug(e);
}
}
};
dojo.lang.delayThese=function(farr,cb,_228,_229){
if(!farr.length){
if(typeof _229=="function"){
_229();
}
return;
}
if((typeof _228=="undefined")&&(typeof cb=="number")){
_228=cb;
cb=function(){
};
}else{
if(!cb){
cb=function(){
};
if(!_228){
_228=0;
}
}
}
setTimeout(function(){
(farr.shift())();
cb();
dojo.lang.delayThese(farr,cb,_228,_229);
},_228);
};
dojo.provide("dojo.event.common");
dojo.event=new function(){
this._canTimeout=dojo.lang.isFunction(dj_global["setTimeout"])||dojo.lang.isAlien(dj_global["setTimeout"]);
function interpolateArgs(args,_22b){
var dl=dojo.lang;
var ao={srcObj:dj_global,srcFunc:null,adviceObj:dj_global,adviceFunc:null,aroundObj:null,aroundFunc:null,adviceType:(args.length>2)?args[0]:"after",precedence:"last",once:false,delay:null,rate:0,adviceMsg:false,maxCalls:-1};
switch(args.length){
case 0:
return;
case 1:
return;
case 2:
ao.srcFunc=args[0];
ao.adviceFunc=args[1];
break;
case 3:
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isString(args[1]))&&(dl.isString(args[2]))){
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
}else{
if((dl.isObject(args[0]))&&(dl.isString(args[1]))&&(dl.isFunction(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
var _22e=dl.nameAnonFunc(args[2],ao.adviceObj,_22b);
ao.adviceFunc=_22e;
}else{
if((dl.isFunction(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))){
ao.adviceType="after";
ao.srcObj=dj_global;
var _22e=dl.nameAnonFunc(args[0],ao.srcObj,_22b);
ao.srcFunc=_22e;
ao.adviceObj=args[1];
ao.adviceFunc=args[2];
}
}
}
}
break;
case 4:
if((dl.isObject(args[0]))&&(dl.isObject(args[2]))){
ao.adviceType="after";
ao.srcObj=args[0];
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isString(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isFunction(args[1]))&&(dl.isObject(args[2]))){
ao.adviceType=args[0];
ao.srcObj=dj_global;
var _22e=dl.nameAnonFunc(args[1],dj_global,_22b);
ao.srcFunc=_22e;
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
if((dl.isString(args[0]))&&(dl.isObject(args[1]))&&(dl.isString(args[2]))&&(dl.isFunction(args[3]))){
ao.srcObj=args[1];
ao.srcFunc=args[2];
var _22e=dl.nameAnonFunc(args[3],dj_global,_22b);
ao.adviceObj=dj_global;
ao.adviceFunc=_22e;
}else{
if(dl.isObject(args[1])){
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=dj_global;
ao.adviceFunc=args[3];
}else{
if(dl.isObject(args[2])){
ao.srcObj=dj_global;
ao.srcFunc=args[1];
ao.adviceObj=args[2];
ao.adviceFunc=args[3];
}else{
ao.srcObj=ao.adviceObj=ao.aroundObj=dj_global;
ao.srcFunc=args[1];
ao.adviceFunc=args[2];
ao.aroundFunc=args[3];
}
}
}
}
}
}
break;
case 6:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundFunc=args[5];
ao.aroundObj=dj_global;
break;
default:
ao.srcObj=args[1];
ao.srcFunc=args[2];
ao.adviceObj=args[3];
ao.adviceFunc=args[4];
ao.aroundObj=args[5];
ao.aroundFunc=args[6];
ao.once=args[7];
ao.delay=args[8];
ao.rate=args[9];
ao.adviceMsg=args[10];
ao.maxCalls=(!isNaN(parseInt(args[11])))?args[11]:-1;
break;
}
if(dl.isFunction(ao.aroundFunc)){
var _22e=dl.nameAnonFunc(ao.aroundFunc,ao.aroundObj,_22b);
ao.aroundFunc=_22e;
}
if(dl.isFunction(ao.srcFunc)){
ao.srcFunc=dl.getNameInObj(ao.srcObj,ao.srcFunc);
}
if(dl.isFunction(ao.adviceFunc)){
ao.adviceFunc=dl.getNameInObj(ao.adviceObj,ao.adviceFunc);
}
if((ao.aroundObj)&&(dl.isFunction(ao.aroundFunc))){
ao.aroundFunc=dl.getNameInObj(ao.aroundObj,ao.aroundFunc);
}
if(!ao.srcObj){
dojo.raise("bad srcObj for srcFunc: "+ao.srcFunc);
}
if(!ao.adviceObj){
dojo.raise("bad adviceObj for adviceFunc: "+ao.adviceFunc);
}
if(!ao.adviceFunc){
dojo.debug("bad adviceFunc for srcFunc: "+ao.srcFunc);
dojo.debugShallow(ao);
}
return ao;
}
this.connect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.connect(ao);
}
ao.srcFunc="onkeypress";
}
if(dojo.lang.isArray(ao.srcObj)&&ao.srcObj!=""){
var _230={};
for(var x in ao){
_230[x]=ao[x];
}
var mjps=[];
dojo.lang.forEach(ao.srcObj,function(src){
if((dojo.render.html.capable)&&(dojo.lang.isString(src))){
src=dojo.byId(src);
}
_230.srcObj=src;
mjps.push(dojo.event.connect.call(dojo.event,_230));
});
return mjps;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc);
if(ao.adviceFunc){
var mjp2=dojo.event.MethodJoinPoint.getForMethod(ao.adviceObj,ao.adviceFunc);
}
mjp.kwAddAdvice(ao);
return mjp;
};
this.log=function(a1,a2){
var _238;
if((arguments.length==1)&&(typeof a1=="object")){
_238=a1;
}else{
_238={srcObj:a1,srcFunc:a2};
}
_238.adviceFunc=function(){
var _239=[];
for(var x=0;x<arguments.length;x++){
_239.push(arguments[x]);
}
dojo.debug("("+_238.srcObj+")."+_238.srcFunc,":",_239.join(", "));
};
this.kwConnect(_238);
};
this.connectBefore=function(){
var args=["before"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectAround=function(){
var args=["around"];
for(var i=0;i<arguments.length;i++){
args.push(arguments[i]);
}
return this.connect.apply(this,args);
};
this.connectOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.once=true;
return this.connect(ao);
};
this.connectRunOnce=function(){
var ao=interpolateArgs(arguments,true);
ao.maxCalls=1;
return this.connect(ao);
};
this._kwConnectImpl=function(_241,_242){
var fn=(_242)?"disconnect":"connect";
if(typeof _241["srcFunc"]=="function"){
_241.srcObj=_241["srcObj"]||dj_global;
var _244=dojo.lang.nameAnonFunc(_241.srcFunc,_241.srcObj,true);
_241.srcFunc=_244;
}
if(typeof _241["adviceFunc"]=="function"){
_241.adviceObj=_241["adviceObj"]||dj_global;
var _244=dojo.lang.nameAnonFunc(_241.adviceFunc,_241.adviceObj,true);
_241.adviceFunc=_244;
}
_241.srcObj=_241["srcObj"]||dj_global;
_241.adviceObj=_241["adviceObj"]||_241["targetObj"]||dj_global;
_241.adviceFunc=_241["adviceFunc"]||_241["targetFunc"];
return dojo.event[fn](_241);
};
this.kwConnect=function(_245){
return this._kwConnectImpl(_245,false);
};
this.disconnect=function(){
if(arguments.length==1){
var ao=arguments[0];
}else{
var ao=interpolateArgs(arguments,true);
}
if(!ao.adviceFunc){
return;
}
if(dojo.lang.isString(ao.srcFunc)&&(ao.srcFunc.toLowerCase()=="onkey")){
if(dojo.render.html.ie){
ao.srcFunc="onkeydown";
this.disconnect(ao);
}
ao.srcFunc="onkeypress";
}
if(!ao.srcObj[ao.srcFunc]){
return null;
}
var mjp=dojo.event.MethodJoinPoint.getForMethod(ao.srcObj,ao.srcFunc,true);
mjp.removeAdvice(ao.adviceObj,ao.adviceFunc,ao.adviceType,ao.once);
return mjp;
};
this.kwDisconnect=function(_248){
return this._kwConnectImpl(_248,true);
};
};
dojo.event.MethodInvocation=function(_249,obj,args){
this.jp_=_249;
this.object=obj;
this.args=[];
for(var x=0;x<args.length;x++){
this.args[x]=args[x];
}
this.around_index=-1;
};
dojo.event.MethodInvocation.prototype.proceed=function(){
this.around_index++;
if(this.around_index>=this.jp_.around.length){
return this.jp_.object[this.jp_.methodname].apply(this.jp_.object,this.args);
}else{
var ti=this.jp_.around[this.around_index];
var mobj=ti[0]||dj_global;
var meth=ti[1];
return mobj[meth].call(mobj,this);
}
};
dojo.event.MethodJoinPoint=function(obj,_251){
this.object=obj||dj_global;
this.methodname=_251;
this.methodfunc=this.object[_251];
this.squelch=false;
};
dojo.event.MethodJoinPoint.getForMethod=function(obj,_253){
if(!obj){
obj=dj_global;
}
var ofn=obj[_253];
if(!ofn){
ofn=obj[_253]=function(){
};
if(!obj[_253]){
dojo.raise("Cannot set do-nothing method on that object "+_253);
}
}else{
if((typeof ofn!="function")&&(!dojo.lang.isFunction(ofn))&&(!dojo.lang.isAlien(ofn))){
return null;
}
}
var _255=_253+"$joinpoint";
var _256=_253+"$joinpoint$method";
var _257=obj[_255];
if(!_257){
var _258=false;
if(dojo.event["browser"]){
if((obj["attachEvent"])||(obj["nodeType"])||(obj["addEventListener"])){
_258=true;
dojo.event.browser.addClobberNodeAttrs(obj,[_255,_256,_253]);
}
}
var _259=ofn.length;
obj[_256]=ofn;
_257=obj[_255]=new dojo.event.MethodJoinPoint(obj,_256);
if(!_258){
obj[_253]=function(){
return _257.run.apply(_257,arguments);
};
}else{
obj[_253]=function(){
var args=[];
if(!arguments.length){
var evt=null;
try{
if(obj.ownerDocument){
evt=obj.ownerDocument.parentWindow.event;
}else{
if(obj.documentElement){
evt=obj.documentElement.ownerDocument.parentWindow.event;
}else{
if(obj.event){
evt=obj.event;
}else{
evt=window.event;
}
}
}
}
catch(e){
evt=window.event;
}
if(evt){
args.push(dojo.event.browser.fixEvent(evt,this));
}
}else{
for(var x=0;x<arguments.length;x++){
if((x==0)&&(dojo.event.browser.isEvent(arguments[x]))){
args.push(dojo.event.browser.fixEvent(arguments[x],this));
}else{
args.push(arguments[x]);
}
}
}
return _257.run.apply(_257,args);
};
}
obj[_253].__preJoinArity=_259;
}
return _257;
};
dojo.lang.extend(dojo.event.MethodJoinPoint,{squelch:false,unintercept:function(){
this.object[this.methodname]=this.methodfunc;
this.before=[];
this.after=[];
this.around=[];
},disconnect:dojo.lang.forward("unintercept"),run:function(){
var obj=this.object||dj_global;
var args=arguments;
var _25f=[];
for(var x=0;x<args.length;x++){
_25f[x]=args[x];
}
var _261=function(marr){
if(!marr){
dojo.debug("Null argument to unrollAdvice()");
return;
}
var _263=marr[0]||dj_global;
var _264=marr[1];
if(!_263[_264]){
dojo.raise("function \""+_264+"\" does not exist on \""+_263+"\"");
}
var _265=marr[2]||dj_global;
var _266=marr[3];
var msg=marr[6];
var _268=marr[7];
if(_268>-1){
if(_268==0){
return;
}
marr[7]--;
}
var _269;
var to={args:[],jp_:this,object:obj,proceed:function(){
return _263[_264].apply(_263,to.args);
}};
to.args=_25f;
var _26b=parseInt(marr[4]);
var _26c=((!isNaN(_26b))&&(marr[4]!==null)&&(typeof marr[4]!="undefined"));
if(marr[5]){
var rate=parseInt(marr[5]);
var cur=new Date();
var _26f=false;
if((marr["last"])&&((cur-marr.last)<=rate)){
if(dojo.event._canTimeout){
if(marr["delayTimer"]){
clearTimeout(marr.delayTimer);
}
var tod=parseInt(rate*2);
var mcpy=dojo.lang.shallowCopy(marr);
marr.delayTimer=setTimeout(function(){
mcpy[5]=0;
_261(mcpy);
},tod);
}
return;
}else{
marr.last=cur;
}
}
if(_266){
_265[_266].call(_265,to);
}else{
if((_26c)&&((dojo.render.html)||(dojo.render.svg))){
dj_global["setTimeout"](function(){
if(msg){
_263[_264].call(_263,to);
}else{
_263[_264].apply(_263,args);
}
},_26b);
}else{
if(msg){
_263[_264].call(_263,to);
}else{
_263[_264].apply(_263,args);
}
}
}
};
var _272=function(){
if(this.squelch){
try{
return _261.apply(this,arguments);
}
catch(e){
dojo.debug(e);
}
}else{
return _261.apply(this,arguments);
}
};
if((this["before"])&&(this.before.length>0)){
dojo.lang.forEach(this.before.concat(new Array()),_272);
}
var _273;
try{
if((this["around"])&&(this.around.length>0)){
var mi=new dojo.event.MethodInvocation(this,obj,args);
_273=mi.proceed();
}else{
if(this.methodfunc){
_273=this.object[this.methodname].apply(this.object,args);
}
}
}
catch(e){
if(!this.squelch){
dojo.debug(e,"when calling",this.methodname,"on",this.object,"with arguments",args);
dojo.raise(e);
}
}
if((this["after"])&&(this.after.length>0)){
dojo.lang.forEach(this.after.concat(new Array()),_272);
}
return (this.methodfunc)?_273:null;
},getArr:function(kind){
var type="after";
if((typeof kind=="string")&&(kind.indexOf("before")!=-1)){
type="before";
}else{
if(kind=="around"){
type="around";
}
}
if(!this[type]){
this[type]=[];
}
return this[type];
},kwAddAdvice:function(args){
this.addAdvice(args["adviceObj"],args["adviceFunc"],args["aroundObj"],args["aroundFunc"],args["adviceType"],args["precedence"],args["once"],args["delay"],args["rate"],args["adviceMsg"],args["maxCalls"]);
},addAdvice:function(_278,_279,_27a,_27b,_27c,_27d,once,_27f,rate,_281,_282){
var arr=this.getArr(_27c);
if(!arr){
dojo.raise("bad this: "+this);
}
var ao=[_278,_279,_27a,_27b,_27f,rate,_281,_282];
if(once){
if(this.hasAdvice(_278,_279,_27c,arr)>=0){
return;
}
}
if(_27d=="first"){
arr.unshift(ao);
}else{
arr.push(ao);
}
},hasAdvice:function(_285,_286,_287,arr){
if(!arr){
arr=this.getArr(_287);
}
var ind=-1;
for(var x=0;x<arr.length;x++){
var aao=(typeof _286=="object")?(new String(_286)).toString():_286;
var a1o=(typeof arr[x][1]=="object")?(new String(arr[x][1])).toString():arr[x][1];
if((arr[x][0]==_285)&&(a1o==aao)){
ind=x;
}
}
return ind;
},removeAdvice:function(_28d,_28e,_28f,once){
var arr=this.getArr(_28f);
var ind=this.hasAdvice(_28d,_28e,_28f,arr);
if(ind==-1){
return false;
}
while(ind!=-1){
arr.splice(ind,1);
if(once){
break;
}
ind=this.hasAdvice(_28d,_28e,_28f,arr);
}
return true;
}});
dojo.provide("dojo.event.topic");
dojo.event.topic=new function(){
this.topics={};
this.getTopic=function(_293){
if(!this.topics[_293]){
this.topics[_293]=new this.TopicImpl(_293);
}
return this.topics[_293];
};
this.registerPublisher=function(_294,obj,_296){
var _294=this.getTopic(_294);
_294.registerPublisher(obj,_296);
};
this.subscribe=function(_297,obj,_299){
var _297=this.getTopic(_297);
_297.subscribe(obj,_299);
};
this.unsubscribe=function(_29a,obj,_29c){
var _29a=this.getTopic(_29a);
_29a.unsubscribe(obj,_29c);
};
this.destroy=function(_29d){
this.getTopic(_29d).destroy();
delete this.topics[_29d];
};
this.publishApply=function(_29e,args){
var _29e=this.getTopic(_29e);
_29e.sendMessage.apply(_29e,args);
};
this.publish=function(_2a0,_2a1){
var _2a0=this.getTopic(_2a0);
var args=[];
for(var x=1;x<arguments.length;x++){
args.push(arguments[x]);
}
_2a0.sendMessage.apply(_2a0,args);
};
};
dojo.event.topic.TopicImpl=function(_2a4){
this.topicName=_2a4;
this.subscribe=function(_2a5,_2a6){
var tf=_2a6||_2a5;
var to=(!_2a6)?dj_global:_2a5;
return dojo.event.kwConnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this.unsubscribe=function(_2a9,_2aa){
var tf=(!_2aa)?_2a9:_2aa;
var to=(!_2aa)?null:_2a9;
return dojo.event.kwDisconnect({srcObj:this,srcFunc:"sendMessage",adviceObj:to,adviceFunc:tf});
};
this._getJoinPoint=function(){
return dojo.event.MethodJoinPoint.getForMethod(this,"sendMessage");
};
this.setSquelch=function(_2ad){
this._getJoinPoint().squelch=_2ad;
};
this.destroy=function(){
this._getJoinPoint().disconnect();
};
this.registerPublisher=function(_2ae,_2af){
dojo.event.connect(_2ae,_2af,this,"sendMessage");
};
this.sendMessage=function(_2b0){
};
};
dojo.provide("dojo.event.browser");
dojo._ie_clobber=new function(){
this.clobberNodes=[];
function nukeProp(node,prop){
try{
node[prop]=null;
}
catch(e){
}
try{
delete node[prop];
}
catch(e){
}
try{
node.removeAttribute(prop);
}
catch(e){
}
}
this.clobber=function(_2b3){
var na;
var tna;
if(_2b3){
tna=_2b3.all||_2b3.getElementsByTagName("*");
na=[_2b3];
for(var x=0;x<tna.length;x++){
if(tna[x]["__doClobber__"]){
na.push(tna[x]);
}
}
}else{
try{
window.onload=null;
}
catch(e){
}
na=(this.clobberNodes.length)?this.clobberNodes:document.all;
}
tna=null;
var _2b7={};
for(var i=na.length-1;i>=0;i=i-1){
var el=na[i];
try{
if(el&&el["__clobberAttrs__"]){
for(var j=0;j<el.__clobberAttrs__.length;j++){
nukeProp(el,el.__clobberAttrs__[j]);
}
nukeProp(el,"__clobberAttrs__");
nukeProp(el,"__doClobber__");
}
}
catch(e){
}
}
na=null;
};
};
if(dojo.render.html.ie){
dojo.addOnUnload(function(){
dojo._ie_clobber.clobber();
try{
if((dojo["widget"])&&(dojo.widget["manager"])){
dojo.widget.manager.destroyAll();
}
}
catch(e){
}
if(dojo.widget){
for(var name in dojo.widget._templateCache){
if(dojo.widget._templateCache[name].node){
dojo.dom.destroyNode(dojo.widget._templateCache[name].node);
dojo.widget._templateCache[name].node=null;
delete dojo.widget._templateCache[name].node;
}
}
}
try{
window.onload=null;
}
catch(e){
}
try{
window.onunload=null;
}
catch(e){
}
dojo._ie_clobber.clobberNodes=[];
});
}
dojo.event.browser=new function(){
var _2bc=0;
this.normalizedEventName=function(_2bd){
switch(_2bd){
case "CheckboxStateChange":
case "DOMAttrModified":
case "DOMMenuItemActive":
case "DOMMenuItemInactive":
case "DOMMouseScroll":
case "DOMNodeInserted":
case "DOMNodeRemoved":
case "RadioStateChange":
return _2bd;
break;
default:
var lcn=_2bd.toLowerCase();
return (lcn.indexOf("on")==0)?lcn.substr(2):lcn;
break;
}
};
this.clean=function(node){
if(dojo.render.html.ie){
dojo._ie_clobber.clobber(node);
}
};
this.addClobberNode=function(node){
if(!dojo.render.html.ie){
return;
}
if(!node["__doClobber__"]){
node.__doClobber__=true;
dojo._ie_clobber.clobberNodes.push(node);
node.__clobberAttrs__=[];
}
};
this.addClobberNodeAttrs=function(node,_2c2){
if(!dojo.render.html.ie){
return;
}
this.addClobberNode(node);
for(var x=0;x<_2c2.length;x++){
node.__clobberAttrs__.push(_2c2[x]);
}
};
this.removeListener=function(node,_2c5,fp,_2c7){
if(!_2c7){
var _2c7=false;
}
_2c5=dojo.event.browser.normalizedEventName(_2c5);
if(_2c5=="key"){
if(dojo.render.html.ie){
this.removeListener(node,"onkeydown",fp,_2c7);
}
_2c5="keypress";
}
if(node.removeEventListener){
node.removeEventListener(_2c5,fp,_2c7);
}
};
this.addListener=function(node,_2c9,fp,_2cb,_2cc){
if(!node){
return;
}
if(!_2cb){
var _2cb=false;
}
_2c9=dojo.event.browser.normalizedEventName(_2c9);
if(_2c9=="key"){
if(dojo.render.html.ie){
this.addListener(node,"onkeydown",fp,_2cb,_2cc);
}
_2c9="keypress";
}
if(!_2cc){
var _2cd=function(evt){
if(!evt){
evt=window.event;
}
var ret=fp(dojo.event.browser.fixEvent(evt,this));
if(_2cb){
dojo.event.browser.stopEvent(evt);
}
return ret;
};
}else{
_2cd=fp;
}
if(node.addEventListener){
node.addEventListener(_2c9,_2cd,_2cb);
return _2cd;
}else{
_2c9="on"+_2c9;
if(typeof node[_2c9]=="function"){
var _2d0=node[_2c9];
node[_2c9]=function(e){
_2d0(e);
return _2cd(e);
};
}else{
node[_2c9]=_2cd;
}
if(dojo.render.html.ie){
this.addClobberNodeAttrs(node,[_2c9]);
}
return _2cd;
}
};
this.isEvent=function(obj){
return (typeof obj!="undefined")&&(obj)&&(typeof Event!="undefined")&&(obj.eventPhase);
};
this.currentEvent=null;
this.callListener=function(_2d3,_2d4){
if(typeof _2d3!="function"){
dojo.raise("listener not a function: "+_2d3);
}
dojo.event.browser.currentEvent.currentTarget=_2d4;
return _2d3.call(_2d4,dojo.event.browser.currentEvent);
};
this._stopPropagation=function(){
dojo.event.browser.currentEvent.cancelBubble=true;
};
this._preventDefault=function(){
dojo.event.browser.currentEvent.returnValue=false;
};
this.keys={KEY_BACKSPACE:8,KEY_TAB:9,KEY_CLEAR:12,KEY_ENTER:13,KEY_SHIFT:16,KEY_CTRL:17,KEY_ALT:18,KEY_PAUSE:19,KEY_CAPS_LOCK:20,KEY_ESCAPE:27,KEY_SPACE:32,KEY_PAGE_UP:33,KEY_PAGE_DOWN:34,KEY_END:35,KEY_HOME:36,KEY_LEFT_ARROW:37,KEY_UP_ARROW:38,KEY_RIGHT_ARROW:39,KEY_DOWN_ARROW:40,KEY_INSERT:45,KEY_DELETE:46,KEY_HELP:47,KEY_LEFT_WINDOW:91,KEY_RIGHT_WINDOW:92,KEY_SELECT:93,KEY_NUMPAD_0:96,KEY_NUMPAD_1:97,KEY_NUMPAD_2:98,KEY_NUMPAD_3:99,KEY_NUMPAD_4:100,KEY_NUMPAD_5:101,KEY_NUMPAD_6:102,KEY_NUMPAD_7:103,KEY_NUMPAD_8:104,KEY_NUMPAD_9:105,KEY_NUMPAD_MULTIPLY:106,KEY_NUMPAD_PLUS:107,KEY_NUMPAD_ENTER:108,KEY_NUMPAD_MINUS:109,KEY_NUMPAD_PERIOD:110,KEY_NUMPAD_DIVIDE:111,KEY_F1:112,KEY_F2:113,KEY_F3:114,KEY_F4:115,KEY_F5:116,KEY_F6:117,KEY_F7:118,KEY_F8:119,KEY_F9:120,KEY_F10:121,KEY_F11:122,KEY_F12:123,KEY_F13:124,KEY_F14:125,KEY_F15:126,KEY_NUM_LOCK:144,KEY_SCROLL_LOCK:145};
this.revKeys=[];
for(var key in this.keys){
this.revKeys[this.keys[key]]=key;
}
this.fixEvent=function(evt,_2d7){
if(!evt){
if(window["event"]){
evt=window.event;
}
}
if((evt["type"])&&(evt["type"].indexOf("key")==0)){
evt.keys=this.revKeys;
for(var key in this.keys){
evt[key]=this.keys[key];
}
if(evt["type"]=="keydown"&&dojo.render.html.ie){
switch(evt.keyCode){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_LEFT_WINDOW:
case evt.KEY_RIGHT_WINDOW:
case evt.KEY_SELECT:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
case evt.KEY_NUMPAD_0:
case evt.KEY_NUMPAD_1:
case evt.KEY_NUMPAD_2:
case evt.KEY_NUMPAD_3:
case evt.KEY_NUMPAD_4:
case evt.KEY_NUMPAD_5:
case evt.KEY_NUMPAD_6:
case evt.KEY_NUMPAD_7:
case evt.KEY_NUMPAD_8:
case evt.KEY_NUMPAD_9:
case evt.KEY_NUMPAD_PERIOD:
break;
case evt.KEY_NUMPAD_MULTIPLY:
case evt.KEY_NUMPAD_PLUS:
case evt.KEY_NUMPAD_ENTER:
case evt.KEY_NUMPAD_MINUS:
case evt.KEY_NUMPAD_DIVIDE:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
case evt.KEY_PAGE_UP:
case evt.KEY_PAGE_DOWN:
case evt.KEY_END:
case evt.KEY_HOME:
case evt.KEY_LEFT_ARROW:
case evt.KEY_UP_ARROW:
case evt.KEY_RIGHT_ARROW:
case evt.KEY_DOWN_ARROW:
case evt.KEY_INSERT:
case evt.KEY_DELETE:
case evt.KEY_F1:
case evt.KEY_F2:
case evt.KEY_F3:
case evt.KEY_F4:
case evt.KEY_F5:
case evt.KEY_F6:
case evt.KEY_F7:
case evt.KEY_F8:
case evt.KEY_F9:
case evt.KEY_F10:
case evt.KEY_F11:
case evt.KEY_F12:
case evt.KEY_F12:
case evt.KEY_F13:
case evt.KEY_F14:
case evt.KEY_F15:
case evt.KEY_CLEAR:
case evt.KEY_HELP:
evt.key=evt.keyCode;
break;
default:
if(evt.ctrlKey||evt.altKey){
var _2d9=evt.keyCode;
if(_2d9>=65&&_2d9<=90&&evt.shiftKey==false){
_2d9+=32;
}
if(_2d9>=1&&_2d9<=26&&evt.ctrlKey){
_2d9+=96;
}
evt.key=String.fromCharCode(_2d9);
}
}
}else{
if(evt["type"]=="keypress"){
if(dojo.render.html.opera){
if(evt.which==0){
evt.key=evt.keyCode;
}else{
if(evt.which>0){
switch(evt.which){
case evt.KEY_SHIFT:
case evt.KEY_CTRL:
case evt.KEY_ALT:
case evt.KEY_CAPS_LOCK:
case evt.KEY_NUM_LOCK:
case evt.KEY_SCROLL_LOCK:
break;
case evt.KEY_PAUSE:
case evt.KEY_TAB:
case evt.KEY_BACKSPACE:
case evt.KEY_ENTER:
case evt.KEY_ESCAPE:
evt.key=evt.which;
break;
default:
var _2d9=evt.which;
if((evt.ctrlKey||evt.altKey||evt.metaKey)&&(evt.which>=65&&evt.which<=90&&evt.shiftKey==false)){
_2d9+=32;
}
evt.key=String.fromCharCode(_2d9);
}
}
}
}else{
if(dojo.render.html.ie){
if(!evt.ctrlKey&&!evt.altKey&&evt.keyCode>=evt.KEY_SPACE){
evt.key=String.fromCharCode(evt.keyCode);
}
}else{
if(dojo.render.html.safari){
switch(evt.keyCode){
case 25:
evt.key=evt.KEY_TAB;
evt.shift=true;
break;
case 63232:
evt.key=evt.KEY_UP_ARROW;
break;
case 63233:
evt.key=evt.KEY_DOWN_ARROW;
break;
case 63234:
evt.key=evt.KEY_LEFT_ARROW;
break;
case 63235:
evt.key=evt.KEY_RIGHT_ARROW;
break;
case 63236:
evt.key=evt.KEY_F1;
break;
case 63237:
evt.key=evt.KEY_F2;
break;
case 63238:
evt.key=evt.KEY_F3;
break;
case 63239:
evt.key=evt.KEY_F4;
break;
case 63240:
evt.key=evt.KEY_F5;
break;
case 63241:
evt.key=evt.KEY_F6;
break;
case 63242:
evt.key=evt.KEY_F7;
break;
case 63243:
evt.key=evt.KEY_F8;
break;
case 63244:
evt.key=evt.KEY_F9;
break;
case 63245:
evt.key=evt.KEY_F10;
break;
case 63246:
evt.key=evt.KEY_F11;
break;
case 63247:
evt.key=evt.KEY_F12;
break;
case 63250:
evt.key=evt.KEY_PAUSE;
break;
case 63272:
evt.key=evt.KEY_DELETE;
break;
case 63273:
evt.key=evt.KEY_HOME;
break;
case 63275:
evt.key=evt.KEY_END;
break;
case 63276:
evt.key=evt.KEY_PAGE_UP;
break;
case 63277:
evt.key=evt.KEY_PAGE_DOWN;
break;
case 63302:
evt.key=evt.KEY_INSERT;
break;
case 63248:
case 63249:
case 63289:
break;
default:
evt.key=evt.charCode>=evt.KEY_SPACE?String.fromCharCode(evt.charCode):evt.keyCode;
}
}else{
evt.key=evt.charCode>0?String.fromCharCode(evt.charCode):evt.keyCode;
}
}
}
}
}
}
if(dojo.render.html.ie){
if(!evt.target){
evt.target=evt.srcElement;
}
if(!evt.currentTarget){
evt.currentTarget=(_2d7?_2d7:evt.srcElement);
}
if(!evt.layerX){
evt.layerX=evt.offsetX;
}
if(!evt.layerY){
evt.layerY=evt.offsetY;
}
var doc=(evt.srcElement&&evt.srcElement.ownerDocument)?evt.srcElement.ownerDocument:document;
var _2db=((dojo.render.html.ie55)||(doc["compatMode"]=="BackCompat"))?doc.body:doc.documentElement;
if(!evt.pageX){
evt.pageX=evt.clientX+(_2db.scrollLeft||0);
}
if(!evt.pageY){
evt.pageY=evt.clientY+(_2db.scrollTop||0);
}
if(evt.type=="mouseover"){
evt.relatedTarget=evt.fromElement;
}
if(evt.type=="mouseout"){
evt.relatedTarget=evt.toElement;
}
this.currentEvent=evt;
evt.callListener=this.callListener;
evt.stopPropagation=this._stopPropagation;
evt.preventDefault=this._preventDefault;
}
return evt;
};
this.stopEvent=function(evt){
if(window.event){
evt.cancelBubble=true;
evt.returnValue=false;
}else{
evt.preventDefault();
evt.stopPropagation();
}
};
};
dojo.kwCompoundRequire({common:["dojo.event.common","dojo.event.topic"],browser:["dojo.event.browser"],dashboard:["dojo.event.browser"]});
dojo.provide("dojo.event.*");
dojo.provide("dojo.event");
dojo.deprecated("dojo.event","replaced by dojo.event.*","0.5");
dojo.provide("dojo.string.common");
dojo.string.trim=function(str,wh){
if(!str.replace){
return str;
}
if(!str.length){
return str;
}
var re=(wh>0)?(/^\s+/):(wh<0)?(/\s+$/):(/^\s+|\s+$/g);
return str.replace(re,"");
};
dojo.string.trimStart=function(str){
return dojo.string.trim(str,1);
};
dojo.string.trimEnd=function(str){
return dojo.string.trim(str,-1);
};
dojo.string.repeat=function(str,_2e3,_2e4){
var out="";
for(var i=0;i<_2e3;i++){
out+=str;
if(_2e4&&i<_2e3-1){
out+=_2e4;
}
}
return out;
};
dojo.string.pad=function(str,len,c,dir){
var out=String(str);
if(!c){
c="0";
}
if(!dir){
dir=1;
}
while(out.length<len){
if(dir>0){
out=c+out;
}else{
out+=c;
}
}
return out;
};
dojo.string.padLeft=function(str,len,c){
return dojo.string.pad(str,len,c,1);
};
dojo.string.padRight=function(str,len,c){
return dojo.string.pad(str,len,c,-1);
};
dojo.provide("dojo.string.extras");
dojo.string.substituteParams=function(_2f2,hash){
var map=(typeof hash=="object")?hash:dojo.lang.toArray(arguments,1);
return _2f2.replace(/\%\{(\w+)\}/g,function(_2f5,key){
if(typeof (map[key])!="undefined"&&map[key]!=null){
return map[key];
}
dojo.raise("Substitution not found: "+key);
});
};
dojo.string.capitalize=function(str){
if(!dojo.lang.isString(str)){
return "";
}
if(arguments.length==0){
str=this;
}
var _2f8=str.split(" ");
for(var i=0;i<_2f8.length;i++){
_2f8[i]=_2f8[i].charAt(0).toUpperCase()+_2f8[i].substring(1);
}
return _2f8.join(" ");
};
dojo.string.isBlank=function(str){
if(!dojo.lang.isString(str)){
return true;
}
return (dojo.string.trim(str).length==0);
};
dojo.string.encodeAscii=function(str){
if(!dojo.lang.isString(str)){
return str;
}
var ret="";
var _2fd=escape(str);
var _2fe,re=/%u([0-9A-F]{4})/i;
while((_2fe=_2fd.match(re))){
var num=Number("0x"+_2fe[1]);
var _301=escape("&#"+num+";");
ret+=_2fd.substring(0,_2fe.index)+_301;
_2fd=_2fd.substring(_2fe.index+_2fe[0].length);
}
ret+=_2fd.replace(/\+/g,"%2B");
return ret;
};
dojo.string.escape=function(type,str){
var args=dojo.lang.toArray(arguments,1);
switch(type.toLowerCase()){
case "xml":
case "html":
case "xhtml":
return dojo.string.escapeXml.apply(this,args);
case "sql":
return dojo.string.escapeSql.apply(this,args);
case "regexp":
case "regex":
return dojo.string.escapeRegExp.apply(this,args);
case "javascript":
case "jscript":
case "js":
return dojo.string.escapeJavaScript.apply(this,args);
case "ascii":
return dojo.string.encodeAscii.apply(this,args);
default:
return str;
}
};
dojo.string.escapeXml=function(str,_306){
str=str.replace(/&/gm,"&amp;").replace(/</gm,"&lt;").replace(/>/gm,"&gt;").replace(/"/gm,"&quot;");
if(!_306){
str=str.replace(/'/gm,"&#39;");
}
return str;
};
dojo.string.escapeSql=function(str){
return str.replace(/'/gm,"''");
};
dojo.string.escapeRegExp=function(str){
return str.replace(/\\/gm,"\\\\").replace(/([\f\b\n\t\r[\^$|?*+(){}])/gm,"\\$1");
};
dojo.string.escapeJavaScript=function(str){
return str.replace(/(["'\f\b\n\t\r])/gm,"\\$1");
};
dojo.string.escapeString=function(str){
return ("\""+str.replace(/(["\\])/g,"\\$1")+"\"").replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r");
};
dojo.string.summary=function(str,len){
if(!len||str.length<=len){
return str;
}
return str.substring(0,len).replace(/\.+$/,"")+"...";
};
dojo.string.endsWith=function(str,end,_30f){
if(_30f){
str=str.toLowerCase();
end=end.toLowerCase();
}
if((str.length-end.length)<0){
return false;
}
return str.lastIndexOf(end)==str.length-end.length;
};
dojo.string.endsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.endsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.startsWith=function(str,_313,_314){
if(_314){
str=str.toLowerCase();
_313=_313.toLowerCase();
}
return str.indexOf(_313)==0;
};
dojo.string.startsWithAny=function(str){
for(var i=1;i<arguments.length;i++){
if(dojo.string.startsWith(str,arguments[i])){
return true;
}
}
return false;
};
dojo.string.has=function(str){
for(var i=1;i<arguments.length;i++){
if(str.indexOf(arguments[i])>-1){
return true;
}
}
return false;
};
dojo.string.normalizeNewlines=function(text,_31a){
if(_31a=="\n"){
text=text.replace(/\r\n/g,"\n");
text=text.replace(/\r/g,"\n");
}else{
if(_31a=="\r"){
text=text.replace(/\r\n/g,"\r");
text=text.replace(/\n/g,"\r");
}else{
text=text.replace(/([^\r])\n/g,"$1\r\n").replace(/\r([^\n])/g,"\r\n$1");
}
}
return text;
};
dojo.string.splitEscaped=function(str,_31c){
var _31d=[];
for(var i=0,_31f=0;i<str.length;i++){
if(str.charAt(i)=="\\"){
i++;
continue;
}
if(str.charAt(i)==_31c){
_31d.push(str.substring(_31f,i));
_31f=i+1;
}
}
_31d.push(str.substr(_31f));
return _31d;
};
dojo.provide("dojo.AdapterRegistry");
dojo.AdapterRegistry=function(_320){
this.pairs=[];
this.returnWrappers=_320||false;
};
dojo.lang.extend(dojo.AdapterRegistry,{register:function(name,_322,wrap,_324,_325){
var type=(_325)?"unshift":"push";
this.pairs[type]([name,_322,wrap,_324]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
dojo.provide("dojo.json");
dojo.json={jsonRegistry:new dojo.AdapterRegistry(),register:function(name,_32d,wrap,_32f){
dojo.json.jsonRegistry.register(name,_32d,wrap,_32f);
},evalJson:function(json){
try{
return eval("("+json+")");
}
catch(e){
dojo.debug(e);
return json;
}
},serialize:function(o){
var _332=typeof (o);
if(_332=="undefined"){
return "undefined";
}else{
if((_332=="number")||(_332=="boolean")){
return o+"";
}else{
if(o===null){
return "null";
}
}
}
if(_332=="string"){
return dojo.string.escapeString(o);
}
var me=arguments.callee;
var _334;
if(typeof (o.__json__)=="function"){
_334=o.__json__();
if(o!==_334){
return me(_334);
}
}
if(typeof (o.json)=="function"){
_334=o.json();
if(o!==_334){
return me(_334);
}
}
if(_332!="function"&&typeof (o.length)=="number"){
var res=[];
for(var i=0;i<o.length;i++){
var val=me(o[i]);
if(typeof (val)!="string"){
val="undefined";
}
res.push(val);
}
return "["+res.join(",")+"]";
}
try{
window.o=o;
_334=dojo.json.jsonRegistry.match(o);
return me(_334);
}
catch(e){
}
if(_332=="function"){
return null;
}
res=[];
for(var k in o){
var _339;
if(typeof (k)=="number"){
_339="\""+k+"\"";
}else{
if(typeof (k)=="string"){
_339=dojo.string.escapeString(k);
}else{
continue;
}
}
val=me(o[k]);
if(typeof (val)!="string"){
continue;
}
res.push(_339+":"+val);
}
return "{"+res.join(",")+"}";
}};
dojo.provide("dojo.html.common");
dojo.lang.mixin(dojo.html,dojo.dom);
dojo.html.body=function(){
dojo.deprecated("dojo.html.body() moved to dojo.body()","0.5");
return dojo.body();
};
dojo.html.getEventTarget=function(evt){
if(!evt){
evt=dojo.global().event||{};
}
var t=(evt.srcElement?evt.srcElement:(evt.target?evt.target:null));
while((t)&&(t.nodeType!=1)){
t=t.parentNode;
}
return t;
};
dojo.html.getViewport=function(){
var _33c=dojo.global();
var _33d=dojo.doc();
var w=0;
var h=0;
if(dojo.render.html.mozilla){
w=_33d.documentElement.clientWidth;
h=_33c.innerHeight;
}else{
if(!dojo.render.html.opera&&_33c.innerWidth){
w=_33c.innerWidth;
h=_33c.innerHeight;
}else{
if(!dojo.render.html.opera&&dojo.exists(_33d,"documentElement.clientWidth")){
var w2=_33d.documentElement.clientWidth;
if(!w||w2&&w2<w){
w=w2;
}
h=_33d.documentElement.clientHeight;
}else{
if(dojo.body().clientWidth){
w=dojo.body().clientWidth;
h=dojo.body().clientHeight;
}
}
}
}
return {width:w,height:h};
};
dojo.html.getScroll=function(){
var _341=dojo.global();
var _342=dojo.doc();
var top=_341.pageYOffset||_342.documentElement.scrollTop||dojo.body().scrollTop||0;
var left=_341.pageXOffset||_342.documentElement.scrollLeft||dojo.body().scrollLeft||0;
return {top:top,left:left,offset:{x:left,y:top}};
};
dojo.html.getParentByType=function(node,type){
var _347=dojo.doc();
var _348=dojo.byId(node);
type=type.toLowerCase();
while((_348)&&(_348.nodeName.toLowerCase()!=type)){
if(_348==(_347["body"]||_347["documentElement"])){
return null;
}
_348=_348.parentNode;
}
return _348;
};
dojo.html.getAttribute=function(node,attr){
node=dojo.byId(node);
if((!node)||(!node.getAttribute)){
return null;
}
var ta=typeof attr=="string"?attr:new String(attr);
var v=node.getAttribute(ta.toUpperCase());
if((v)&&(typeof v=="string")&&(v!="")){
return v;
}
if(v&&v.value){
return v.value;
}
if((node.getAttributeNode)&&(node.getAttributeNode(ta))){
return (node.getAttributeNode(ta)).value;
}else{
if(node.getAttribute(ta)){
return node.getAttribute(ta);
}else{
if(node.getAttribute(ta.toLowerCase())){
return node.getAttribute(ta.toLowerCase());
}
}
}
return null;
};
dojo.html.hasAttribute=function(node,attr){
return dojo.html.getAttribute(dojo.byId(node),attr)?true:false;
};
dojo.html.getCursorPosition=function(e){
e=e||dojo.global().event;
var _350={x:0,y:0};
if(e.pageX||e.pageY){
_350.x=e.pageX;
_350.y=e.pageY;
}else{
var de=dojo.doc().documentElement;
var db=dojo.body();
_350.x=e.clientX+((de||db)["scrollLeft"])-((de||db)["clientLeft"]);
_350.y=e.clientY+((de||db)["scrollTop"])-((de||db)["clientTop"]);
}
return _350;
};
dojo.html.isTag=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
for(var i=1;i<arguments.length;i++){
if(node.tagName.toLowerCase()==String(arguments[i]).toLowerCase()){
return String(arguments[i]).toLowerCase();
}
}
}
return "";
};
if(dojo.render.html.ie&&!dojo.render.html.ie70){
if(window.location.href.substr(0,6).toLowerCase()!="https:"){
(function(){
var _355=dojo.doc().createElement("script");
_355.src="javascript:'dojo.html.createExternalElement=function(doc, tag){ return doc.createElement(tag); }'";
dojo.doc().getElementsByTagName("head")[0].appendChild(_355);
})();
}
}else{
dojo.html.createExternalElement=function(doc,tag){
return doc.createElement(tag);
};
}
dojo.html._callDeprecated=function(_358,_359,args,_35b,_35c){
dojo.deprecated("dojo.html."+_358,"replaced by dojo.html."+_359+"("+(_35b?"node, {"+_35b+": "+_35b+"}":"")+")"+(_35c?"."+_35c:""),"0.5");
var _35d=[];
if(_35b){
var _35e={};
_35e[_35b]=args[1];
_35d.push(args[0]);
_35d.push(_35e);
}else{
_35d=args;
}
var ret=dojo.html[_359].apply(dojo.html,args);
if(_35c){
return ret[_35c];
}else{
return ret;
}
};
dojo.html.getViewportWidth=function(){
return dojo.html._callDeprecated("getViewportWidth","getViewport",arguments,null,"width");
};
dojo.html.getViewportHeight=function(){
return dojo.html._callDeprecated("getViewportHeight","getViewport",arguments,null,"height");
};
dojo.html.getViewportSize=function(){
return dojo.html._callDeprecated("getViewportSize","getViewport",arguments);
};
dojo.html.getScrollTop=function(){
return dojo.html._callDeprecated("getScrollTop","getScroll",arguments,null,"top");
};
dojo.html.getScrollLeft=function(){
return dojo.html._callDeprecated("getScrollLeft","getScroll",arguments,null,"left");
};
dojo.html.getScrollOffset=function(){
return dojo.html._callDeprecated("getScrollOffset","getScroll",arguments,null,"offset");
};
dojo.provide("dojo.uri.Uri");
dojo.uri=new function(){
this.dojoUri=function(uri){
return new dojo.uri.Uri(dojo.hostenv.getBaseScriptUri(),uri);
};
this.moduleUri=function(_361,uri){
var loc=dojo.hostenv.getModuleSymbols(_361).join("/");
if(!loc){
return null;
}
if(loc.lastIndexOf("/")!=loc.length-1){
loc+="/";
}
var _364=loc.indexOf(":");
var _365=loc.indexOf("/");
if(loc.charAt(0)!="/"&&(_364==-1||_364>_365)){
loc=dojo.hostenv.getBaseScriptUri()+loc;
}
return new dojo.uri.Uri(loc,uri);
};
this.Uri=function(){
var uri=arguments[0];
for(var i=1;i<arguments.length;i++){
if(!arguments[i]){
continue;
}
var _368=new dojo.uri.Uri(arguments[i].toString());
var _369=new dojo.uri.Uri(uri.toString());
if((_368.path=="")&&(_368.scheme==null)&&(_368.authority==null)&&(_368.query==null)){
if(_368.fragment!=null){
_369.fragment=_368.fragment;
}
_368=_369;
}else{
if(_368.scheme==null){
_368.scheme=_369.scheme;
if(_368.authority==null){
_368.authority=_369.authority;
if(_368.path.charAt(0)!="/"){
var path=_369.path.substring(0,_369.path.lastIndexOf("/")+1)+_368.path;
var segs=path.split("/");
for(var j=0;j<segs.length;j++){
if(segs[j]=="."){
if(j==segs.length-1){
segs[j]="";
}else{
segs.splice(j,1);
j--;
}
}else{
if(j>0&&!(j==1&&segs[0]=="")&&segs[j]==".."&&segs[j-1]!=".."){
if(j==segs.length-1){
segs.splice(j,1);
segs[j-1]="";
}else{
segs.splice(j-1,2);
j-=2;
}
}
}
}
_368.path=segs.join("/");
}
}
}
}
uri="";
if(_368.scheme!=null){
uri+=_368.scheme+":";
}
if(_368.authority!=null){
uri+="//"+_368.authority;
}
uri+=_368.path;
if(_368.query!=null){
uri+="?"+_368.query;
}
if(_368.fragment!=null){
uri+="#"+_368.fragment;
}
}
this.uri=uri.toString();
var _36d="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$";
var r=this.uri.match(new RegExp(_36d));
this.scheme=r[2]||(r[1]?"":null);
this.authority=r[4]||(r[3]?"":null);
this.path=r[5];
this.query=r[7]||(r[6]?"":null);
this.fragment=r[9]||(r[8]?"":null);
if(this.authority!=null){
_36d="^((([^:]+:)?([^@]+))@)?([^:]*)(:([0-9]+))?$";
r=this.authority.match(new RegExp(_36d));
this.user=r[3]||null;
this.password=r[4]||null;
this.host=r[5];
this.port=r[7]||null;
}
this.toString=function(){
return this.uri;
};
};
};
dojo.provide("dojo.html.style");
dojo.html.getClass=function(node){
node=dojo.byId(node);
if(!node){
return "";
}
var cs="";
if(node.className){
cs=node.className;
}else{
if(dojo.html.hasAttribute(node,"class")){
cs=dojo.html.getAttribute(node,"class");
}
}
return cs.replace(/^\s+|\s+$/g,"");
};
dojo.html.getClasses=function(node){
var c=dojo.html.getClass(node);
return (c=="")?[]:c.split(/\s+/g);
};
dojo.html.hasClass=function(node,_374){
return (new RegExp("(^|\\s+)"+_374+"(\\s+|$)")).test(dojo.html.getClass(node));
};
dojo.html.prependClass=function(node,_376){
_376+=" "+dojo.html.getClass(node);
return dojo.html.setClass(node,_376);
};
dojo.html.addClass=function(node,_378){
if(dojo.html.hasClass(node,_378)){
return false;
}
_378=(dojo.html.getClass(node)+" "+_378).replace(/^\s+|\s+$/g,"");
return dojo.html.setClass(node,_378);
};
dojo.html.setClass=function(node,_37a){
node=dojo.byId(node);
var cs=new String(_37a);
try{
if(typeof node.className=="string"){
node.className=cs;
}else{
if(node.setAttribute){
node.setAttribute("class",_37a);
node.className=cs;
}else{
return false;
}
}
}
catch(e){
dojo.debug("dojo.html.setClass() failed",e);
}
return true;
};
dojo.html.removeClass=function(node,_37d,_37e){
try{
if(!_37e){
var _37f=dojo.html.getClass(node).replace(new RegExp("(^|\\s+)"+_37d+"(\\s+|$)"),"$1$2");
}else{
var _37f=dojo.html.getClass(node).replace(_37d,"");
}
dojo.html.setClass(node,_37f);
}
catch(e){
dojo.debug("dojo.html.removeClass() failed",e);
}
return true;
};
dojo.html.replaceClass=function(node,_381,_382){
dojo.html.removeClass(node,_382);
dojo.html.addClass(node,_381);
};
dojo.html.classMatchType={ContainsAll:0,ContainsAny:1,IsOnly:2};
dojo.html.getElementsByClass=function(_383,_384,_385,_386,_387){
_387=false;
var _388=dojo.doc();
_384=dojo.byId(_384)||_388;
var _389=_383.split(/\s+/g);
var _38a=[];
if(_386!=1&&_386!=2){
_386=0;
}
var _38b=new RegExp("(\\s|^)(("+_389.join(")|(")+"))(\\s|$)");
var _38c=_389.join(" ").length;
var _38d=[];
if(!_387&&_388.evaluate){
var _38e=".//"+(_385||"*")+"[contains(";
if(_386!=dojo.html.classMatchType.ContainsAny){
_38e+="concat(' ',@class,' '), ' "+_389.join(" ') and contains(concat(' ',@class,' '), ' ")+" ')";
if(_386==2){
_38e+=" and string-length(@class)="+_38c+"]";
}else{
_38e+="]";
}
}else{
_38e+="concat(' ',@class,' '), ' "+_389.join(" ') or contains(concat(' ',@class,' '), ' ")+" ')]";
}
var _38f=_388.evaluate(_38e,_384,null,XPathResult.ANY_TYPE,null);
var _390=_38f.iterateNext();
while(_390){
try{
_38d.push(_390);
_390=_38f.iterateNext();
}
catch(e){
break;
}
}
return _38d;
}else{
if(!_385){
_385="*";
}
_38d=_384.getElementsByTagName(_385);
var node,i=0;
outer:
while(node=_38d[i++]){
var _393=dojo.html.getClasses(node);
if(_393.length==0){
continue outer;
}
var _394=0;
for(var j=0;j<_393.length;j++){
if(_38b.test(_393[j])){
if(_386==dojo.html.classMatchType.ContainsAny){
_38a.push(node);
continue outer;
}else{
_394++;
}
}else{
if(_386==dojo.html.classMatchType.IsOnly){
continue outer;
}
}
}
if(_394==_389.length){
if((_386==dojo.html.classMatchType.IsOnly)&&(_394==_393.length)){
_38a.push(node);
}else{
if(_386==dojo.html.classMatchType.ContainsAll){
_38a.push(node);
}
}
}
}
return _38a;
}
};
dojo.html.getElementsByClassName=dojo.html.getElementsByClass;
dojo.html.toCamelCase=function(_396){
var arr=_396.split("-"),cc=arr[0];
for(var i=1;i<arr.length;i++){
cc+=arr[i].charAt(0).toUpperCase()+arr[i].substring(1);
}
return cc;
};
dojo.html.toSelectorCase=function(_39a){
return _39a.replace(/([A-Z])/g,"-$1").toLowerCase();
};
if(dojo.render.html.ie){
dojo.html.getComputedStyle=function(node,_39c,_39d){
node=dojo.byId(node);
if(!node||!node.currentStyle){
return _39d;
}
return node.currentStyle[dojo.html.toCamelCase(_39c)];
};
dojo.html.getComputedStyles=function(node){
return node.currentStyle;
};
}else{
dojo.html.getComputedStyle=function(node,_3a0,_3a1){
node=dojo.byId(node);
if(!node||!node.style){
return _3a1;
}
var s=document.defaultView.getComputedStyle(node,null);
return (s&&s[dojo.html.toCamelCase(_3a0)])||"";
};
dojo.html.getComputedStyles=function(node){
return document.defaultView.getComputedStyle(node,null);
};
}
dojo.html.getStyleProperty=function(node,_3a5){
node=dojo.byId(node);
return (node&&node.style?node.style[dojo.html.toCamelCase(_3a5)]:undefined);
};
dojo.html.getStyle=function(node,_3a7){
var _3a8=dojo.html.getStyleProperty(node,_3a7);
return (_3a8?_3a8:dojo.html.getComputedStyle(node,_3a7));
};
dojo.html.setStyle=function(node,_3aa,_3ab){
node=dojo.byId(node);
if(node&&node.style){
var _3ac=dojo.html.toCamelCase(_3aa);
node.style[_3ac]=_3ab;
}
};
dojo.html.setStyleText=function(_3ad,text){
try{
_3ad.style.cssText=text;
}
catch(e){
_3ad.setAttribute("style",text);
}
};
dojo.html.copyStyle=function(_3af,_3b0){
if(!_3b0.style.cssText){
_3af.setAttribute("style",_3b0.getAttribute("style"));
}else{
_3af.style.cssText=_3b0.style.cssText;
}
dojo.html.addClass(_3af,dojo.html.getClass(_3b0));
};
dojo.html.getUnitValue=function(node,_3b2,_3b3){
var s=dojo.html.getComputedStyle(node,_3b2);
if((!s)||((s=="auto")&&(_3b3))){
return {value:0,units:"px"};
}
var _3b5=s.match(/(\-?[\d.]+)([a-z%]*)/i);
if(!_3b5){
return dojo.html.getUnitValue.bad;
}
return {value:Number(_3b5[1]),units:_3b5[2].toLowerCase()};
};
dojo.html.getUnitValue.bad={value:NaN,units:""};
if(dojo.render.html.ie){
dojo.html.toPixelValue=function(_3b6,_3b7){
if(!_3b7){
return 0;
}
if(_3b7.slice(-2)=="px"){
return parseFloat(_3b7);
}
var _3b8=0;
with(_3b6){
var _3b9=style.left;
var _3ba=runtimeStyle.left;
runtimeStyle.left=currentStyle.left;
try{
style.left=_3b7||0;
_3b8=style.pixelLeft;
style.left=_3b9;
runtimeStyle.left=_3ba;
}
catch(e){
}
}
return _3b8;
};
}else{
dojo.html.toPixelValue=function(_3bb,_3bc){
return (_3bc&&(_3bc.slice(-2)=="px")?parseFloat(_3bc):0);
};
}
dojo.html.getPixelValue=function(node,_3be,_3bf){
return dojo.html.toPixelValue(node,dojo.html.getComputedStyle(node,_3be));
};
dojo.html.setPositivePixelValue=function(node,_3c1,_3c2){
if(isNaN(_3c2)){
return false;
}
node.style[_3c1]=Math.max(0,_3c2)+"px";
return true;
};
dojo.html.styleSheet=null;
dojo.html.insertCssRule=function(_3c3,_3c4,_3c5){
if(!dojo.html.styleSheet){
if(document.createStyleSheet){
dojo.html.styleSheet=document.createStyleSheet();
}else{
if(document.styleSheets[0]){
dojo.html.styleSheet=document.styleSheets[0];
}else{
return null;
}
}
}
if(arguments.length<3){
if(dojo.html.styleSheet.cssRules){
_3c5=dojo.html.styleSheet.cssRules.length;
}else{
if(dojo.html.styleSheet.rules){
_3c5=dojo.html.styleSheet.rules.length;
}else{
return null;
}
}
}
if(dojo.html.styleSheet.insertRule){
var rule=_3c3+" { "+_3c4+" }";
return dojo.html.styleSheet.insertRule(rule,_3c5);
}else{
if(dojo.html.styleSheet.addRule){
return dojo.html.styleSheet.addRule(_3c3,_3c4,_3c5);
}else{
return null;
}
}
};
dojo.html.removeCssRule=function(_3c7){
if(!dojo.html.styleSheet){
dojo.debug("no stylesheet defined for removing rules");
return false;
}
if(dojo.render.html.ie){
if(!_3c7){
_3c7=dojo.html.styleSheet.rules.length;
dojo.html.styleSheet.removeRule(_3c7);
}
}else{
if(document.styleSheets[0]){
if(!_3c7){
_3c7=dojo.html.styleSheet.cssRules.length;
}
dojo.html.styleSheet.deleteRule(_3c7);
}
}
return true;
};
dojo.html._insertedCssFiles=[];
dojo.html.insertCssFile=function(URI,doc,_3ca,_3cb){
if(!URI){
return;
}
if(!doc){
doc=document;
}
var _3cc=dojo.hostenv.getText(URI,false,_3cb);
if(_3cc===null){
return;
}
_3cc=dojo.html.fixPathsInCssText(_3cc,URI);
if(_3ca){
var idx=-1,node,ent=dojo.html._insertedCssFiles;
for(var i=0;i<ent.length;i++){
if((ent[i].doc==doc)&&(ent[i].cssText==_3cc)){
idx=i;
node=ent[i].nodeRef;
break;
}
}
if(node){
var _3d1=doc.getElementsByTagName("style");
for(var i=0;i<_3d1.length;i++){
if(_3d1[i]==node){
return;
}
}
dojo.html._insertedCssFiles.shift(idx,1);
}
}
var _3d2=dojo.html.insertCssText(_3cc,doc);
dojo.html._insertedCssFiles.push({"doc":doc,"cssText":_3cc,"nodeRef":_3d2});
if(_3d2&&djConfig.isDebug){
_3d2.setAttribute("dbgHref",URI);
}
return _3d2;
};
dojo.html.insertCssText=function(_3d3,doc,URI){
if(!_3d3){
return;
}
if(!doc){
doc=document;
}
if(URI){
_3d3=dojo.html.fixPathsInCssText(_3d3,URI);
}
var _3d6=doc.createElement("style");
_3d6.setAttribute("type","text/css");
var head=doc.getElementsByTagName("head")[0];
if(!head){
dojo.debug("No head tag in document, aborting styles");
return;
}else{
head.appendChild(_3d6);
}
if(_3d6.styleSheet){
var _3d8=function(){
try{
_3d6.styleSheet.cssText=_3d3;
}
catch(e){
dojo.debug(e);
}
};
if(_3d6.styleSheet.disabled){
setTimeout(_3d8,10);
}else{
_3d8();
}
}else{
var _3d9=doc.createTextNode(_3d3);
_3d6.appendChild(_3d9);
}
return _3d6;
};
dojo.html.fixPathsInCssText=function(_3da,URI){
if(!_3da||!URI){
return;
}
var _3dc,str="",url="",_3df="[\\t\\s\\w\\(\\)\\/\\.\\\\'\"-:#=&?~]+";
var _3e0=new RegExp("url\\(\\s*("+_3df+")\\s*\\)");
var _3e1=/(file|https?|ftps?):\/\//;
regexTrim=new RegExp("^[\\s]*(['\"]?)("+_3df+")\\1[\\s]*?$");
if(dojo.render.html.ie55||dojo.render.html.ie60){
var _3e2=new RegExp("AlphaImageLoader\\((.*)src=['\"]("+_3df+")['\"]");
while(_3dc=_3e2.exec(_3da)){
url=_3dc[2].replace(regexTrim,"$2");
if(!_3e1.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_3da.substring(0,_3dc.index)+"AlphaImageLoader("+_3dc[1]+"src='"+url+"'";
_3da=_3da.substr(_3dc.index+_3dc[0].length);
}
_3da=str+_3da;
str="";
}
while(_3dc=_3e0.exec(_3da)){
url=_3dc[1].replace(regexTrim,"$2");
if(!_3e1.exec(url)){
url=(new dojo.uri.Uri(URI,url).toString());
}
str+=_3da.substring(0,_3dc.index)+"url("+url+")";
_3da=_3da.substr(_3dc.index+_3dc[0].length);
}
return str+_3da;
};
dojo.html.setActiveStyleSheet=function(_3e3){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")){
a.disabled=true;
if(a.getAttribute("title")==_3e3){
a.disabled=false;
}
}
}
};
dojo.html.getActiveStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("title")&&!a.disabled){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.getPreferredStyleSheet=function(){
var i=0,a,els=dojo.doc().getElementsByTagName("link");
while(a=els[i++]){
if(a.getAttribute("rel").indexOf("style")!=-1&&a.getAttribute("rel").indexOf("alt")==-1&&a.getAttribute("title")){
return a.getAttribute("title");
}
}
return null;
};
dojo.html.applyBrowserClass=function(node){
var drh=dojo.render.html;
var _3ef={dj_ie:drh.ie,dj_ie55:drh.ie55,dj_ie6:drh.ie60,dj_ie7:drh.ie70,dj_iequirks:drh.ie&&drh.quirks,dj_opera:drh.opera,dj_opera8:drh.opera&&(Math.floor(dojo.render.version)==8),dj_opera9:drh.opera&&(Math.floor(dojo.render.version)==9),dj_khtml:drh.khtml,dj_safari:drh.safari,dj_gecko:drh.mozilla};
for(var p in _3ef){
if(_3ef[p]){
dojo.html.addClass(node,p);
}
}
};
dojo.kwCompoundRequire({common:["dojo.html.common","dojo.html.style"]});
dojo.provide("dojo.html.*");
dojo.provide("dojo.html");
dojo.deprecated("dojo.html","replaced by dojo.html.*","0.5");
dojo.provide("dojo.html.display");
dojo.html._toggle=function(node,_3f2,_3f3){
node=dojo.byId(node);
_3f3(node,!_3f2(node));
return _3f2(node);
};
dojo.html.show=function(node){
node=dojo.byId(node);
if(dojo.html.getStyleProperty(node,"display")=="none"){
dojo.html.setStyle(node,"display",(node.dojoDisplayCache||""));
node.dojoDisplayCache=undefined;
}
};
dojo.html.hide=function(node){
node=dojo.byId(node);
if(typeof node["dojoDisplayCache"]=="undefined"){
var d=dojo.html.getStyleProperty(node,"display");
if(d!="none"){
node.dojoDisplayCache=d;
}
}
dojo.html.setStyle(node,"display","none");
};
dojo.html.setShowing=function(node,_3f8){
dojo.html[(_3f8?"show":"hide")](node);
};
dojo.html.isShowing=function(node){
return (dojo.html.getStyleProperty(node,"display")!="none");
};
dojo.html.toggleShowing=function(node){
return dojo.html._toggle(node,dojo.html.isShowing,dojo.html.setShowing);
};
dojo.html.displayMap={tr:"",td:"",th:"",img:"inline",span:"inline",input:"inline",button:"inline"};
dojo.html.suggestDisplayByTagName=function(node){
node=dojo.byId(node);
if(node&&node.tagName){
var tag=node.tagName.toLowerCase();
return (tag in dojo.html.displayMap?dojo.html.displayMap[tag]:"block");
}
};
dojo.html.setDisplay=function(node,_3fe){
dojo.html.setStyle(node,"display",((_3fe instanceof String||typeof _3fe=="string")?_3fe:(_3fe?dojo.html.suggestDisplayByTagName(node):"none")));
};
dojo.html.isDisplayed=function(node){
return (dojo.html.getComputedStyle(node,"display")!="none");
};
dojo.html.toggleDisplay=function(node){
return dojo.html._toggle(node,dojo.html.isDisplayed,dojo.html.setDisplay);
};
dojo.html.setVisibility=function(node,_402){
dojo.html.setStyle(node,"visibility",((_402 instanceof String||typeof _402=="string")?_402:(_402?"visible":"hidden")));
};
dojo.html.isVisible=function(node){
return (dojo.html.getComputedStyle(node,"visibility")!="hidden");
};
dojo.html.toggleVisibility=function(node){
return dojo.html._toggle(node,dojo.html.isVisible,dojo.html.setVisibility);
};
dojo.html.setOpacity=function(node,_406,_407){
node=dojo.byId(node);
var h=dojo.render.html;
if(!_407){
if(_406>=1){
if(h.ie){
dojo.html.clearOpacity(node);
return;
}else{
_406=0.999999;
}
}else{
if(_406<0){
_406=0;
}
}
}
if(h.ie){
if(node.nodeName.toLowerCase()=="tr"){
var tds=node.getElementsByTagName("td");
for(var x=0;x<tds.length;x++){
tds[x].style.filter="Alpha(Opacity="+_406*100+")";
}
}
node.style.filter="Alpha(Opacity="+_406*100+")";
}else{
if(h.moz){
node.style.opacity=_406;
node.style.MozOpacity=_406;
}else{
if(h.safari){
node.style.opacity=_406;
node.style.KhtmlOpacity=_406;
}else{
node.style.opacity=_406;
}
}
}
};
dojo.html.clearOpacity=function(node){
node=dojo.byId(node);
var ns=node.style;
var h=dojo.render.html;
if(h.ie){
try{
if(node.filters&&node.filters.alpha){
ns.filter="";
}
}
catch(e){
}
}else{
if(h.moz){
ns.opacity=1;
ns.MozOpacity=1;
}else{
if(h.safari){
ns.opacity=1;
ns.KhtmlOpacity=1;
}else{
ns.opacity=1;
}
}
}
};
dojo.html.getOpacity=function(node){
node=dojo.byId(node);
var h=dojo.render.html;
if(h.ie){
var opac=(node.filters&&node.filters.alpha&&typeof node.filters.alpha.opacity=="number"?node.filters.alpha.opacity:100)/100;
}else{
var opac=node.style.opacity||node.style.MozOpacity||node.style.KhtmlOpacity||1;
}
return opac>=0.999999?1:Number(opac);
};
dojo.provide("dojo.html.layout");
dojo.html.sumAncestorProperties=function(node,prop){
node=dojo.byId(node);
if(!node){
return 0;
}
var _413=0;
while(node){
if(dojo.html.getComputedStyle(node,"position")=="fixed"){
return 0;
}
var val=node[prop];
if(val){
_413+=val-0;
if(node==dojo.body()){
break;
}
}
node=node.parentNode;
}
return _413;
};
dojo.html.setStyleAttributes=function(node,_416){
node=dojo.byId(node);
var _417=_416.replace(/(;)?\s*$/,"").split(";");
for(var i=0;i<_417.length;i++){
var _419=_417[i].split(":");
var name=_419[0].replace(/\s*$/,"").replace(/^\s*/,"").toLowerCase();
var _41b=_419[1].replace(/\s*$/,"").replace(/^\s*/,"");
switch(name){
case "opacity":
dojo.html.setOpacity(node,_41b);
break;
case "content-height":
dojo.html.setContentBox(node,{height:_41b});
break;
case "content-width":
dojo.html.setContentBox(node,{width:_41b});
break;
case "outer-height":
dojo.html.setMarginBox(node,{height:_41b});
break;
case "outer-width":
dojo.html.setMarginBox(node,{width:_41b});
break;
default:
node.style[dojo.html.toCamelCase(name)]=_41b;
}
}
};
dojo.html.boxSizing={MARGIN_BOX:"margin-box",BORDER_BOX:"border-box",PADDING_BOX:"padding-box",CONTENT_BOX:"content-box"};
dojo.html.getAbsolutePosition=dojo.html.abs=function(node,_41d,_41e){
node=dojo.byId(node,node.ownerDocument);
var ret={x:0,y:0};
var bs=dojo.html.boxSizing;
if(!_41e){
_41e=bs.CONTENT_BOX;
}
var _421=2;
var _422;
switch(_41e){
case bs.MARGIN_BOX:
_422=3;
break;
case bs.BORDER_BOX:
_422=2;
break;
case bs.PADDING_BOX:
default:
_422=1;
break;
case bs.CONTENT_BOX:
_422=0;
break;
}
var h=dojo.render.html;
var db=document["body"]||document["documentElement"];
if(h.ie){
with(node.getBoundingClientRect()){
ret.x=left-2;
ret.y=top-2;
}
}else{
if(document.getBoxObjectFor){
_421=1;
try{
var bo=document.getBoxObjectFor(node);
ret.x=bo.x-dojo.html.sumAncestorProperties(node,"scrollLeft");
ret.y=bo.y-dojo.html.sumAncestorProperties(node,"scrollTop");
}
catch(e){
}
}else{
if(node["offsetParent"]){
var _426;
if((h.safari)&&(node.style.getPropertyValue("position")=="absolute")&&(node.parentNode==db)){
_426=db;
}else{
_426=db.parentNode;
}
if(node.parentNode!=db){
var nd=node;
if(dojo.render.html.opera){
nd=db;
}
ret.x-=dojo.html.sumAncestorProperties(nd,"scrollLeft");
ret.y-=dojo.html.sumAncestorProperties(nd,"scrollTop");
}
var _428=node;
do{
var n=_428["offsetLeft"];
if(!h.opera||n>0){
ret.x+=isNaN(n)?0:n;
}
var m=_428["offsetTop"];
ret.y+=isNaN(m)?0:m;
_428=_428.offsetParent;
}while((_428!=_426)&&(_428!=null));
}else{
if(node["x"]&&node["y"]){
ret.x+=isNaN(node.x)?0:node.x;
ret.y+=isNaN(node.y)?0:node.y;
}
}
}
}
if(_41d){
var _42b=dojo.html.getScroll();
ret.y+=_42b.top;
ret.x+=_42b.left;
}
var _42c=[dojo.html.getPaddingExtent,dojo.html.getBorderExtent,dojo.html.getMarginExtent];
if(_421>_422){
for(var i=_422;i<_421;++i){
ret.y+=_42c[i](node,"top");
ret.x+=_42c[i](node,"left");
}
}else{
if(_421<_422){
for(var i=_422;i>_421;--i){
ret.y-=_42c[i-1](node,"top");
ret.x-=_42c[i-1](node,"left");
}
}
}
ret.top=ret.y;
ret.left=ret.x;
return ret;
};
dojo.html.isPositionAbsolute=function(node){
return (dojo.html.getComputedStyle(node,"position")=="absolute");
};
dojo.html._sumPixelValues=function(node,_430,_431){
var _432=0;
for(var x=0;x<_430.length;x++){
_432+=dojo.html.getPixelValue(node,_430[x],_431);
}
return _432;
};
dojo.html.getMargin=function(node){
return {width:dojo.html._sumPixelValues(node,["margin-left","margin-right"],(dojo.html.getComputedStyle(node,"position")=="absolute")),height:dojo.html._sumPixelValues(node,["margin-top","margin-bottom"],(dojo.html.getComputedStyle(node,"position")=="absolute"))};
};
dojo.html.getBorder=function(node){
return {width:dojo.html.getBorderExtent(node,"left")+dojo.html.getBorderExtent(node,"right"),height:dojo.html.getBorderExtent(node,"top")+dojo.html.getBorderExtent(node,"bottom")};
};
dojo.html.getBorderExtent=function(node,side){
return (dojo.html.getStyle(node,"border-"+side+"-style")=="none"?0:dojo.html.getPixelValue(node,"border-"+side+"-width"));
};
dojo.html.getMarginExtent=function(node,side){
return dojo.html._sumPixelValues(node,["margin-"+side],dojo.html.isPositionAbsolute(node));
};
dojo.html.getPaddingExtent=function(node,side){
return dojo.html._sumPixelValues(node,["padding-"+side],true);
};
dojo.html.getPadding=function(node){
return {width:dojo.html._sumPixelValues(node,["padding-left","padding-right"],true),height:dojo.html._sumPixelValues(node,["padding-top","padding-bottom"],true)};
};
dojo.html.getPadBorder=function(node){
var pad=dojo.html.getPadding(node);
var _43f=dojo.html.getBorder(node);
return {width:pad.width+_43f.width,height:pad.height+_43f.height};
};
dojo.html.getBoxSizing=function(node){
var h=dojo.render.html;
var bs=dojo.html.boxSizing;
if(((h.ie)||(h.opera))&&node.nodeName.toLowerCase()!="img"){
var cm=document["compatMode"];
if((cm=="BackCompat")||(cm=="QuirksMode")){
return bs.BORDER_BOX;
}else{
return bs.CONTENT_BOX;
}
}else{
if(arguments.length==0){
node=document.documentElement;
}
var _444;
if(!h.ie){
_444=dojo.html.getStyle(node,"-moz-box-sizing");
if(!_444){
_444=dojo.html.getStyle(node,"box-sizing");
}
}
return (_444?_444:bs.CONTENT_BOX);
}
};
dojo.html.isBorderBox=function(node){
return (dojo.html.getBoxSizing(node)==dojo.html.boxSizing.BORDER_BOX);
};
dojo.html.getBorderBox=function(node){
node=dojo.byId(node);
return {width:node.offsetWidth,height:node.offsetHeight};
};
dojo.html.getPaddingBox=function(node){
var box=dojo.html.getBorderBox(node);
var _449=dojo.html.getBorder(node);
return {width:box.width-_449.width,height:box.height-_449.height};
};
dojo.html.getContentBox=function(node){
node=dojo.byId(node);
var _44b=dojo.html.getPadBorder(node);
return {width:node.offsetWidth-_44b.width,height:node.offsetHeight-_44b.height};
};
dojo.html.setContentBox=function(node,args){
node=dojo.byId(node);
var _44e=0;
var _44f=0;
var isbb=dojo.html.isBorderBox(node);
var _451=(isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var ret={};
if(typeof args.width!="undefined"){
_44e=args.width+_451.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_44e);
}
if(typeof args.height!="undefined"){
_44f=args.height+_451.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_44f);
}
return ret;
};
dojo.html.getMarginBox=function(node){
var _454=dojo.html.getBorderBox(node);
var _455=dojo.html.getMargin(node);
return {width:_454.width+_455.width,height:_454.height+_455.height};
};
dojo.html.setMarginBox=function(node,args){
node=dojo.byId(node);
var _458=0;
var _459=0;
var isbb=dojo.html.isBorderBox(node);
var _45b=(!isbb?dojo.html.getPadBorder(node):{width:0,height:0});
var _45c=dojo.html.getMargin(node);
var ret={};
if(typeof args.width!="undefined"){
_458=args.width-_45b.width;
_458-=_45c.width;
ret.width=dojo.html.setPositivePixelValue(node,"width",_458);
}
if(typeof args.height!="undefined"){
_459=args.height-_45b.height;
_459-=_45c.height;
ret.height=dojo.html.setPositivePixelValue(node,"height",_459);
}
return ret;
};
dojo.html.getElementBox=function(node,type){
var bs=dojo.html.boxSizing;
switch(type){
case bs.MARGIN_BOX:
return dojo.html.getMarginBox(node);
case bs.BORDER_BOX:
return dojo.html.getBorderBox(node);
case bs.PADDING_BOX:
return dojo.html.getPaddingBox(node);
case bs.CONTENT_BOX:
default:
return dojo.html.getContentBox(node);
}
};
dojo.html.toCoordinateObject=dojo.html.toCoordinateArray=function(_461,_462,_463){
if(_461 instanceof Array||typeof _461=="array"){
dojo.deprecated("dojo.html.toCoordinateArray","use dojo.html.toCoordinateObject({left: , top: , width: , height: }) instead","0.5");
while(_461.length<4){
_461.push(0);
}
while(_461.length>4){
_461.pop();
}
var ret={left:_461[0],top:_461[1],width:_461[2],height:_461[3]};
}else{
if(!_461.nodeType&&!(_461 instanceof String||typeof _461=="string")&&("width" in _461||"height" in _461||"left" in _461||"x" in _461||"top" in _461||"y" in _461)){
var ret={left:_461.left||_461.x||0,top:_461.top||_461.y||0,width:_461.width||0,height:_461.height||0};
}else{
var node=dojo.byId(_461);
var pos=dojo.html.abs(node,_462,_463);
var _467=dojo.html.getMarginBox(node);
var ret={left:pos.left,top:pos.top,width:_467.width,height:_467.height};
}
}
ret.x=ret.left;
ret.y=ret.top;
return ret;
};
dojo.html.setMarginBoxWidth=dojo.html.setOuterWidth=function(node,_469){
return dojo.html._callDeprecated("setMarginBoxWidth","setMarginBox",arguments,"width");
};
dojo.html.setMarginBoxHeight=dojo.html.setOuterHeight=function(){
return dojo.html._callDeprecated("setMarginBoxHeight","setMarginBox",arguments,"height");
};
dojo.html.getMarginBoxWidth=dojo.html.getOuterWidth=function(){
return dojo.html._callDeprecated("getMarginBoxWidth","getMarginBox",arguments,null,"width");
};
dojo.html.getMarginBoxHeight=dojo.html.getOuterHeight=function(){
return dojo.html._callDeprecated("getMarginBoxHeight","getMarginBox",arguments,null,"height");
};
dojo.html.getTotalOffset=function(node,type,_46c){
return dojo.html._callDeprecated("getTotalOffset","getAbsolutePosition",arguments,null,type);
};
dojo.html.getAbsoluteX=function(node,_46e){
return dojo.html._callDeprecated("getAbsoluteX","getAbsolutePosition",arguments,null,"x");
};
dojo.html.getAbsoluteY=function(node,_470){
return dojo.html._callDeprecated("getAbsoluteY","getAbsolutePosition",arguments,null,"y");
};
dojo.html.totalOffsetLeft=function(node,_472){
return dojo.html._callDeprecated("totalOffsetLeft","getAbsolutePosition",arguments,null,"left");
};
dojo.html.totalOffsetTop=function(node,_474){
return dojo.html._callDeprecated("totalOffsetTop","getAbsolutePosition",arguments,null,"top");
};
dojo.html.getMarginWidth=function(node){
return dojo.html._callDeprecated("getMarginWidth","getMargin",arguments,null,"width");
};
dojo.html.getMarginHeight=function(node){
return dojo.html._callDeprecated("getMarginHeight","getMargin",arguments,null,"height");
};
dojo.html.getBorderWidth=function(node){
return dojo.html._callDeprecated("getBorderWidth","getBorder",arguments,null,"width");
};
dojo.html.getBorderHeight=function(node){
return dojo.html._callDeprecated("getBorderHeight","getBorder",arguments,null,"height");
};
dojo.html.getPaddingWidth=function(node){
return dojo.html._callDeprecated("getPaddingWidth","getPadding",arguments,null,"width");
};
dojo.html.getPaddingHeight=function(node){
return dojo.html._callDeprecated("getPaddingHeight","getPadding",arguments,null,"height");
};
dojo.html.getPadBorderWidth=function(node){
return dojo.html._callDeprecated("getPadBorderWidth","getPadBorder",arguments,null,"width");
};
dojo.html.getPadBorderHeight=function(node){
return dojo.html._callDeprecated("getPadBorderHeight","getPadBorder",arguments,null,"height");
};
dojo.html.getBorderBoxWidth=dojo.html.getInnerWidth=function(){
return dojo.html._callDeprecated("getBorderBoxWidth","getBorderBox",arguments,null,"width");
};
dojo.html.getBorderBoxHeight=dojo.html.getInnerHeight=function(){
return dojo.html._callDeprecated("getBorderBoxHeight","getBorderBox",arguments,null,"height");
};
dojo.html.getContentBoxWidth=dojo.html.getContentWidth=function(){
return dojo.html._callDeprecated("getContentBoxWidth","getContentBox",arguments,null,"width");
};
dojo.html.getContentBoxHeight=dojo.html.getContentHeight=function(){
return dojo.html._callDeprecated("getContentBoxHeight","getContentBox",arguments,null,"height");
};
dojo.html.setContentBoxWidth=dojo.html.setContentWidth=function(node,_47e){
return dojo.html._callDeprecated("setContentBoxWidth","setContentBox",arguments,"width");
};
dojo.html.setContentBoxHeight=dojo.html.setContentHeight=function(node,_480){
return dojo.html._callDeprecated("setContentBoxHeight","setContentBox",arguments,"height");
};
dojo.provide("dojo.lang.type");
dojo.lang.whatAmI=function(_481){
dojo.deprecated("dojo.lang.whatAmI","use dojo.lang.getType instead","0.5");
return dojo.lang.getType(_481);
};
dojo.lang.whatAmI.custom={};
dojo.lang.getType=function(_482){
try{
if(dojo.lang.isArray(_482)){
return "array";
}
if(dojo.lang.isFunction(_482)){
return "function";
}
if(dojo.lang.isString(_482)){
return "string";
}
if(dojo.lang.isNumber(_482)){
return "number";
}
if(dojo.lang.isBoolean(_482)){
return "boolean";
}
if(dojo.lang.isAlien(_482)){
return "alien";
}
if(dojo.lang.isUndefined(_482)){
return "undefined";
}
for(var name in dojo.lang.whatAmI.custom){
if(dojo.lang.whatAmI.custom[name](_482)){
return name;
}
}
if(dojo.lang.isObject(_482)){
return "object";
}
}
catch(e){
}
return "unknown";
};
dojo.lang.isNumeric=function(_484){
return (!isNaN(_484)&&isFinite(_484)&&(_484!=null)&&!dojo.lang.isBoolean(_484)&&!dojo.lang.isArray(_484)&&!/^\s*$/.test(_484));
};
dojo.lang.isBuiltIn=function(_485){
return (dojo.lang.isArray(_485)||dojo.lang.isFunction(_485)||dojo.lang.isString(_485)||dojo.lang.isNumber(_485)||dojo.lang.isBoolean(_485)||(_485==null)||(_485 instanceof Error)||(typeof _485=="error"));
};
dojo.lang.isPureObject=function(_486){
return ((_486!=null)&&dojo.lang.isObject(_486)&&_486.constructor==Object);
};
dojo.lang.isOfType=function(_487,type,_489){
var _48a=false;
if(_489){
_48a=_489["optional"];
}
if(_48a&&((_487===null)||dojo.lang.isUndefined(_487))){
return true;
}
if(dojo.lang.isArray(type)){
var _48b=type;
for(var i in _48b){
var _48d=_48b[i];
if(dojo.lang.isOfType(_487,_48d)){
return true;
}
}
return false;
}else{
if(dojo.lang.isString(type)){
type=type.toLowerCase();
}
switch(type){
case Array:
case "array":
return dojo.lang.isArray(_487);
case Function:
case "function":
return dojo.lang.isFunction(_487);
case String:
case "string":
return dojo.lang.isString(_487);
case Number:
case "number":
return dojo.lang.isNumber(_487);
case "numeric":
return dojo.lang.isNumeric(_487);
case Boolean:
case "boolean":
return dojo.lang.isBoolean(_487);
case Object:
case "object":
return dojo.lang.isObject(_487);
case "pureobject":
return dojo.lang.isPureObject(_487);
case "builtin":
return dojo.lang.isBuiltIn(_487);
case "alien":
return dojo.lang.isAlien(_487);
case "undefined":
return dojo.lang.isUndefined(_487);
case null:
case "null":
return (_487===null);
case "optional":
dojo.deprecated("dojo.lang.isOfType(value, [type, \"optional\"])","use dojo.lang.isOfType(value, type, {optional: true} ) instead","0.5");
return ((_487===null)||dojo.lang.isUndefined(_487));
default:
if(dojo.lang.isFunction(type)){
return (_487 instanceof type);
}else{
dojo.raise("dojo.lang.isOfType() was passed an invalid type");
}
}
}
dojo.raise("If we get here, it means a bug was introduced above.");
};
dojo.lang.getObject=function(str){
var _48f=str.split("."),i=0,obj=dj_global;
do{
obj=obj[_48f[i++]];
}while(i<_48f.length&&obj);
return (obj!=dj_global)?obj:null;
};
dojo.lang.doesObjectExist=function(str){
var _493=str.split("."),i=0,obj=dj_global;
do{
obj=obj[_493[i++]];
}while(i<_493.length&&obj);
return (obj&&obj!=dj_global);
};
dojo.provide("dojo.lang.assert");
dojo.lang.assert=function(_496,_497){
if(!_496){
var _498="An assert statement failed.\n"+"The method dojo.lang.assert() was called with a 'false' value.\n";
if(_497){
_498+="Here's the assert message:\n"+_497+"\n";
}
throw new Error(_498);
}
};
dojo.lang.assertType=function(_499,type,_49b){
if(dojo.lang.isString(_49b)){
dojo.deprecated("dojo.lang.assertType(value, type, \"message\")","use dojo.lang.assertType(value, type) instead","0.5");
}
if(!dojo.lang.isOfType(_499,type,_49b)){
if(!dojo.lang.assertType._errorMessage){
dojo.lang.assertType._errorMessage="Type mismatch: dojo.lang.assertType() failed.";
}
dojo.lang.assert(false,dojo.lang.assertType._errorMessage);
}
};
dojo.lang.assertValidKeywords=function(_49c,_49d,_49e){
var key;
if(!_49e){
if(!dojo.lang.assertValidKeywords._errorMessage){
dojo.lang.assertValidKeywords._errorMessage="In dojo.lang.assertValidKeywords(), found invalid keyword:";
}
_49e=dojo.lang.assertValidKeywords._errorMessage;
}
if(dojo.lang.isArray(_49d)){
for(key in _49c){
if(!dojo.lang.inArray(_49d,key)){
dojo.lang.assert(false,_49e+" "+key);
}
}
}else{
for(key in _49c){
if(!(key in _49d)){
dojo.lang.assert(false,_49e+" "+key);
}
}
}
};
dojo.provide("dojo.lang");
dojo.deprecated("dojo.lang","replaced by dojo.lang.common","0.5");
dojo.provide("dojo.lang.repr");
dojo.lang.reprRegistry=new dojo.AdapterRegistry();
dojo.lang.registerRepr=function(name,_4a1,wrap,_4a3){
dojo.lang.reprRegistry.register(name,_4a1,wrap,_4a3);
};
dojo.lang.repr=function(obj){
if(typeof (obj)=="undefined"){
return "undefined";
}else{
if(obj===null){
return "null";
}
}
try{
if(typeof (obj["__repr__"])=="function"){
return obj["__repr__"]();
}else{
if((typeof (obj["repr"])=="function")&&(obj.repr!=arguments.callee)){
return obj["repr"]();
}
}
return dojo.lang.reprRegistry.match(obj);
}
catch(e){
if(typeof (obj.NAME)=="string"&&(obj.toString==Function.prototype.toString||obj.toString==Object.prototype.toString)){
return obj.NAME;
}
}
if(typeof (obj)=="function"){
obj=(obj+"").replace(/^\s+/,"");
var idx=obj.indexOf("{");
if(idx!=-1){
obj=obj.substr(0,idx)+"{...}";
}
}
return obj+"";
};
dojo.lang.reprArrayLike=function(arr){
try{
var na=dojo.lang.map(arr,dojo.lang.repr);
return "["+na.join(", ")+"]";
}
catch(e){
}
};
(function(){
var m=dojo.lang;
m.registerRepr("arrayLike",m.isArrayLike,m.reprArrayLike);
m.registerRepr("string",m.isString,m.reprString);
m.registerRepr("numbers",m.isNumber,m.reprNumber);
m.registerRepr("boolean",m.isBoolean,m.reprNumber);
})();
dojo.provide("dojo.lang.declare");
dojo.lang.declare=function(_4a9,_4aa,init,_4ac){
if((dojo.lang.isFunction(_4ac))||((!_4ac)&&(!dojo.lang.isFunction(init)))){
var temp=_4ac;
_4ac=init;
init=temp;
}
var _4ae=[];
if(dojo.lang.isArray(_4aa)){
_4ae=_4aa;
_4aa=_4ae.shift();
}
if(!init){
init=dojo.evalObjPath(_4a9,false);
if((init)&&(!dojo.lang.isFunction(init))){
init=null;
}
}
var ctor=dojo.lang.declare._makeConstructor();
var scp=(_4aa?_4aa.prototype:null);
if(scp){
scp.prototyping=true;
ctor.prototype=new _4aa();
scp.prototyping=false;
}
ctor.superclass=scp;
ctor.mixins=_4ae;
for(var i=0,l=_4ae.length;i<l;i++){
dojo.lang.extend(ctor,_4ae[i].prototype);
}
ctor.prototype.initializer=null;
ctor.prototype.declaredClass=_4a9;
if(dojo.lang.isArray(_4ac)){
dojo.lang.extend.apply(dojo.lang,[ctor].concat(_4ac));
}else{
dojo.lang.extend(ctor,(_4ac)||{});
}
dojo.lang.extend(ctor,dojo.lang.declare._common);
ctor.prototype.constructor=ctor;
ctor.prototype.initializer=(ctor.prototype.initializer)||(init)||(function(){
});
var _4b3=dojo.parseObjPath(_4a9,null,true);
_4b3.obj[_4b3.prop]=ctor;
return ctor;
};
dojo.lang.declare._makeConstructor=function(){
return function(){
var self=this._getPropContext();
var s=self.constructor.superclass;
if((s)&&(s.constructor)){
if(s.constructor==arguments.callee){
this._inherited("constructor",arguments);
}else{
this._contextMethod(s,"constructor",arguments);
}
}
var ms=(self.constructor.mixins)||([]);
for(var i=0,m;(m=ms[i]);i++){
(((m.prototype)&&(m.prototype.initializer))||(m)).apply(this,arguments);
}
if((!this.prototyping)&&(self.initializer)){
self.initializer.apply(this,arguments);
}
};
};
dojo.lang.declare._common={_getPropContext:function(){
return (this.___proto||this);
},_contextMethod:function(_4b9,_4ba,args){
var _4bc,_4bd=this.___proto;
this.___proto=_4b9;
try{
_4bc=_4b9[_4ba].apply(this,(args||[]));
}
catch(e){
throw e;
}
finally{
this.___proto=_4bd;
}
return _4bc;
},_inherited:function(prop,args){
var p=this._getPropContext();
do{
if((!p.constructor)||(!p.constructor.superclass)){
return;
}
p=p.constructor.superclass;
}while(!(prop in p));
return (dojo.lang.isFunction(p[prop])?this._contextMethod(p,prop,args):p[prop]);
},inherited:function(prop,args){
dojo.deprecated("'inherited' method is dangerous, do not up-call! 'inherited' is slated for removal in 0.5; name your super class (or use superclass property) instead.","0.5");
this._inherited(prop,args);
}};
dojo.declare=dojo.lang.declare;
dojo.kwCompoundRequire({common:["dojo.lang.common","dojo.lang.assert","dojo.lang.array","dojo.lang.type","dojo.lang.func","dojo.lang.extras","dojo.lang.repr","dojo.lang.declare"]});
dojo.provide("dojo.lang.*");
dojo.provide("dojo.xml.Parse");
dojo.xml.Parse=function(){
var isIE=((dojo.render.html.capable)&&(dojo.render.html.ie));
function getTagName(node){
try{
return node.tagName.toLowerCase();
}
catch(e){
return "";
}
}
function getDojoTagName(node){
var _4c6=getTagName(node);
if(!_4c6){
return "";
}
if((dojo.widget)&&(dojo.widget.tags[_4c6])){
return _4c6;
}
var p=_4c6.indexOf(":");
if(p>=0){
return _4c6;
}
if(_4c6.substr(0,5)=="dojo:"){
return _4c6;
}
if(dojo.render.html.capable&&dojo.render.html.ie&&node.scopeName!="HTML"){
return node.scopeName.toLowerCase()+":"+_4c6;
}
if(_4c6.substr(0,4)=="dojo"){
return "dojo:"+_4c6.substring(4);
}
var djt=node.getAttribute("dojoType")||node.getAttribute("dojotype");
if(djt){
if(djt.indexOf(":")<0){
djt="dojo:"+djt;
}
return djt.toLowerCase();
}
djt=node.getAttributeNS&&node.getAttributeNS(dojo.dom.dojoml,"type");
if(djt){
return "dojo:"+djt.toLowerCase();
}
try{
djt=node.getAttribute("dojo:type");
}
catch(e){
}
if(djt){
return "dojo:"+djt.toLowerCase();
}
if((dj_global["djConfig"])&&(!djConfig["ignoreClassNames"])){
var _4c9=node.className||node.getAttribute("class");
if((_4c9)&&(_4c9.indexOf)&&(_4c9.indexOf("dojo-")!=-1)){
var _4ca=_4c9.split(" ");
for(var x=0,c=_4ca.length;x<c;x++){
if(_4ca[x].slice(0,5)=="dojo-"){
return "dojo:"+_4ca[x].substr(5).toLowerCase();
}
}
}
}
return "";
}
this.parseElement=function(node,_4ce,_4cf,_4d0){
var _4d1=getTagName(node);
if(isIE&&_4d1.indexOf("/")==0){
return null;
}
try{
var attr=node.getAttribute("parseWidgets");
if(attr&&attr.toLowerCase()=="false"){
return {};
}
}
catch(e){
}
var _4d3=true;
if(_4cf){
var _4d4=getDojoTagName(node);
_4d1=_4d4||_4d1;
_4d3=Boolean(_4d4);
}
var _4d5={};
_4d5[_4d1]=[];
var pos=_4d1.indexOf(":");
if(pos>0){
var ns=_4d1.substring(0,pos);
_4d5["ns"]=ns;
if((dojo.ns)&&(!dojo.ns.allow(ns))){
_4d3=false;
}
}
if(_4d3){
var _4d8=this.parseAttributes(node);
for(var attr in _4d8){
if((!_4d5[_4d1][attr])||(typeof _4d5[_4d1][attr]!="array")){
_4d5[_4d1][attr]=[];
}
_4d5[_4d1][attr].push(_4d8[attr]);
}
_4d5[_4d1].nodeRef=node;
_4d5.tagName=_4d1;
_4d5.index=_4d0||0;
}
var _4d9=0;
for(var i=0;i<node.childNodes.length;i++){
var tcn=node.childNodes.item(i);
switch(tcn.nodeType){
case dojo.dom.ELEMENT_NODE:
var ctn=getDojoTagName(tcn)||getTagName(tcn);
if(!_4d5[ctn]){
_4d5[ctn]=[];
}
_4d5[ctn].push(this.parseElement(tcn,true,_4cf,_4d9));
if((tcn.childNodes.length==1)&&(tcn.childNodes.item(0).nodeType==dojo.dom.TEXT_NODE)){
_4d5[ctn][_4d5[ctn].length-1].value=tcn.childNodes.item(0).nodeValue;
}
_4d9++;
break;
case dojo.dom.TEXT_NODE:
if(node.childNodes.length==1){
_4d5[_4d1].push({value:node.childNodes.item(0).nodeValue});
}
break;
default:
break;
}
}
return _4d5;
};
this.parseAttributes=function(node){
var _4de={};
var atts=node.attributes;
var _4e0,i=0;
while((_4e0=atts[i++])){
if(isIE){
if(!_4e0){
continue;
}
if((typeof _4e0=="object")&&(typeof _4e0.nodeValue=="undefined")||(_4e0.nodeValue==null)||(_4e0.nodeValue=="")){
continue;
}
}
var nn=_4e0.nodeName.split(":");
nn=(nn.length==2)?nn[1]:_4e0.nodeName;
_4de[nn]={value:_4e0.nodeValue};
}
return _4de;
};
};
dojo.provide("dojo.ns");
dojo.ns={namespaces:{},failed:{},loading:{},loaded:{},register:function(name,_4e4,_4e5,_4e6){
if(!_4e6||!this.namespaces[name]){
this.namespaces[name]=new dojo.ns.Ns(name,_4e4,_4e5);
}
},allow:function(name){
if(this.failed[name]){
return false;
}
if((djConfig.excludeNamespace)&&(dojo.lang.inArray(djConfig.excludeNamespace,name))){
return false;
}
return ((name==this.dojo)||(!djConfig.includeNamespace)||(dojo.lang.inArray(djConfig.includeNamespace,name)));
},get:function(name){
return this.namespaces[name];
},require:function(name){
var ns=this.namespaces[name];
if((ns)&&(this.loaded[name])){
return ns;
}
if(!this.allow(name)){
return false;
}
if(this.loading[name]){
dojo.debug("dojo.namespace.require: re-entrant request to load namespace \""+name+"\" must fail.");
return false;
}
var req=dojo.require;
this.loading[name]=true;
try{
if(name=="dojo"){
req("dojo.namespaces.dojo");
}else{
if(!dojo.hostenv.moduleHasPrefix(name)){
dojo.registerModulePath(name,"../"+name);
}
req([name,"manifest"].join("."),false,true);
}
if(!this.namespaces[name]){
this.failed[name]=true;
}
}
finally{
this.loading[name]=false;
}
return this.namespaces[name];
}};
dojo.ns.Ns=function(name,_4ed,_4ee){
this.name=name;
this.module=_4ed;
this.resolver=_4ee;
this._loaded=[];
this._failed=[];
};
dojo.ns.Ns.prototype.resolve=function(name,_4f0,_4f1){
if(!this.resolver||djConfig["skipAutoRequire"]){
return false;
}
var _4f2=this.resolver(name,_4f0);
if((_4f2)&&(!this._loaded[_4f2])&&(!this._failed[_4f2])){
var req=dojo.require;
req(_4f2,false,true);
if(dojo.hostenv.findModule(_4f2,false)){
this._loaded[_4f2]=true;
}else{
if(!_4f1){
dojo.raise("dojo.ns.Ns.resolve: module '"+_4f2+"' not found after loading via namespace '"+this.name+"'");
}
this._failed[_4f2]=true;
}
}
return Boolean(this._loaded[_4f2]);
};
dojo.registerNamespace=function(name,_4f5,_4f6){
dojo.ns.register.apply(dojo.ns,arguments);
};
dojo.registerNamespaceResolver=function(name,_4f8){
var n=dojo.ns.namespaces[name];
if(n){
n.resolver=_4f8;
}
};
dojo.registerNamespaceManifest=function(_4fa,path,name,_4fd,_4fe){
dojo.registerModulePath(name,path);
dojo.registerNamespace(name,_4fd,_4fe);
};
dojo.registerNamespace("dojo","dojo.widget");
dojo.provide("dojo.widget.Manager");
dojo.widget.manager=new function(){
this.widgets=[];
this.widgetIds=[];
this.topWidgets={};
var _4ff={};
var _500=[];
this.getUniqueId=function(_501){
var _502;
do{
_502=_501+"_"+(_4ff[_501]!=undefined?++_4ff[_501]:_4ff[_501]=0);
}while(this.getWidgetById(_502));
return _502;
};
this.add=function(_503){
this.widgets.push(_503);
if(!_503.extraArgs["id"]){
_503.extraArgs["id"]=_503.extraArgs["ID"];
}
if(_503.widgetId==""){
if(_503["id"]){
_503.widgetId=_503["id"];
}else{
if(_503.extraArgs["id"]){
_503.widgetId=_503.extraArgs["id"];
}else{
_503.widgetId=this.getUniqueId(_503.ns+"_"+_503.widgetType);
}
}
}
if(this.widgetIds[_503.widgetId]){
dojo.debug("widget ID collision on ID: "+_503.widgetId);
}
this.widgetIds[_503.widgetId]=_503;
};
this.destroyAll=function(){
for(var x=this.widgets.length-1;x>=0;x--){
try{
this.widgets[x].destroy(true);
delete this.widgets[x];
}
catch(e){
}
}
};
this.remove=function(_505){
if(dojo.lang.isNumber(_505)){
var tw=this.widgets[_505].widgetId;
delete this.topWidgets[tw];
delete this.widgetIds[tw];
this.widgets.splice(_505,1);
}else{
this.removeById(_505);
}
};
this.removeById=function(id){
if(!dojo.lang.isString(id)){
id=id["widgetId"];
if(!id){
dojo.debug("invalid widget or id passed to removeById");
return;
}
}
for(var i=0;i<this.widgets.length;i++){
if(this.widgets[i].widgetId==id){
this.remove(i);
break;
}
}
};
this.getWidgetById=function(id){
if(dojo.lang.isString(id)){
return this.widgetIds[id];
}
return id;
};
this.getWidgetsByType=function(type){
var lt=type.toLowerCase();
var _50c=(type.indexOf(":")<0?function(x){
return x.widgetType.toLowerCase();
}:function(x){
return x.getNamespacedType();
});
var ret=[];
dojo.lang.forEach(this.widgets,function(x){
if(_50c(x)==lt){
ret.push(x);
}
});
return ret;
};
this.getWidgetsByFilter=function(_511,_512){
var ret=[];
dojo.lang.every(this.widgets,function(x){
if(_511(x)){
ret.push(x);
if(_512){
return false;
}
}
return true;
});
return (_512?ret[0]:ret);
};
this.getAllWidgets=function(){
return this.widgets.concat();
};
this.getWidgetByNode=function(node){
var w=this.getAllWidgets();
node=dojo.byId(node);
for(var i=0;i<w.length;i++){
if(w[i].domNode==node){
return w[i];
}
}
return null;
};
this.byId=this.getWidgetById;
this.byType=this.getWidgetsByType;
this.byFilter=this.getWidgetsByFilter;
this.byNode=this.getWidgetByNode;
var _518={};
var _519=["dojo.widget"];
for(var i=0;i<_519.length;i++){
_519[_519[i]]=true;
}
this.registerWidgetPackage=function(_51b){
if(!_519[_51b]){
_519[_51b]=true;
_519.push(_51b);
}
};
this.getWidgetPackageList=function(){
return dojo.lang.map(_519,function(elt){
return (elt!==true?elt:undefined);
});
};
this.getImplementation=function(_51d,_51e,_51f,ns){
var impl=this.getImplementationName(_51d,ns);
if(impl){
var ret=_51e?new impl(_51e):new impl();
return ret;
}
};
function buildPrefixCache(){
for(var _523 in dojo.render){
if(dojo.render[_523]["capable"]===true){
var _524=dojo.render[_523].prefixes;
for(var i=0;i<_524.length;i++){
_500.push(_524[i].toLowerCase());
}
}
}
}
var _526=function(_527,_528){
if(!_528){
return null;
}
for(var i=0,l=_500.length,_52b;i<=l;i++){
_52b=(i<l?_528[_500[i]]:_528);
if(!_52b){
continue;
}
for(var name in _52b){
if(name.toLowerCase()==_527){
return _52b[name];
}
}
}
return null;
};
var _52d=function(_52e,_52f){
var _530=dojo.evalObjPath(_52f,false);
return (_530?_526(_52e,_530):null);
};
this.getImplementationName=function(_531,ns){
var _533=_531.toLowerCase();
ns=ns||"dojo";
var imps=_518[ns]||(_518[ns]={});
var impl=imps[_533];
if(impl){
return impl;
}
if(!_500.length){
buildPrefixCache();
}
var _536=dojo.ns.get(ns);
if(!_536){
dojo.ns.register(ns,ns+".widget");
_536=dojo.ns.get(ns);
}
if(_536){
_536.resolve(_531);
}
impl=_52d(_533,_536.module);
if(impl){
return (imps[_533]=impl);
}
_536=dojo.ns.require(ns);
if((_536)&&(_536.resolver)){
_536.resolve(_531);
impl=_52d(_533,_536.module);
if(impl){
return (imps[_533]=impl);
}
}
dojo.deprecated("dojo.widget.Manager.getImplementationName","Could not locate widget implementation for \""+_531+"\" in \""+_536.module+"\" registered to namespace \""+_536.name+"\". "+"Developers must specify correct namespaces for all non-Dojo widgets","0.5");
for(var i=0;i<_519.length;i++){
impl=_52d(_533,_519[i]);
if(impl){
return (imps[_533]=impl);
}
}
throw new Error("Could not locate widget implementation for \""+_531+"\" in \""+_536.module+"\" registered to namespace \""+_536.name+"\"");
};
this.resizing=false;
this.onWindowResized=function(){
if(this.resizing){
return;
}
try{
this.resizing=true;
for(var id in this.topWidgets){
var _539=this.topWidgets[id];
if(_539.checkSize){
_539.checkSize();
}
}
}
catch(e){
}
finally{
this.resizing=false;
}
};
if(typeof window!="undefined"){
dojo.addOnLoad(this,"onWindowResized");
dojo.event.connect(window,"onresize",this,"onWindowResized");
}
};
(function(){
var dw=dojo.widget;
var dwm=dw.manager;
var h=dojo.lang.curry(dojo.lang,"hitch",dwm);
var g=function(_53e,_53f){
dw[(_53f||_53e)]=h(_53e);
};
g("add","addWidget");
g("destroyAll","destroyAllWidgets");
g("remove","removeWidget");
g("removeById","removeWidgetById");
g("getWidgetById");
g("getWidgetById","byId");
g("getWidgetsByType");
g("getWidgetsByFilter");
g("getWidgetsByType","byType");
g("getWidgetsByFilter","byFilter");
g("getWidgetByNode","byNode");
dw.all=function(n){
var _541=dwm.getAllWidgets.apply(dwm,arguments);
if(arguments.length>0){
return _541[n];
}
return _541;
};
g("registerWidgetPackage");
g("getImplementation","getWidgetImplementation");
g("getImplementationName","getWidgetImplementationName");
dw.widgets=dwm.widgets;
dw.widgetIds=dwm.widgetIds;
dw.root=dwm.root;
})();
dojo.kwCompoundRequire({common:[["dojo.uri.Uri",false,false]]});
dojo.provide("dojo.uri.*");
dojo.provide("dojo.a11y");
dojo.a11y={imgPath:dojo.uri.moduleUri("dojo.widget","templates/images"),doAccessibleCheck:true,accessible:null,checkAccessible:function(){
if(this.accessible===null){
this.accessible=false;
if(this.doAccessibleCheck==true){
this.accessible=this.testAccessible();
}
}
return this.accessible;
},testAccessible:function(){
this.accessible=false;
if(dojo.render.html.ie||dojo.render.html.mozilla){
var div=document.createElement("div");
div.style.backgroundImage="url(\""+this.imgPath+"/tab_close.gif\")";
dojo.body().appendChild(div);
var _543=null;
if(window.getComputedStyle){
var _544=getComputedStyle(div,"");
_543=_544.getPropertyValue("background-image");
}else{
_543=div.currentStyle.backgroundImage;
}
var _545=false;
if(_543!=null&&(_543=="none"||_543=="url(invalid-url:)")){
this.accessible=true;
}
dojo.body().removeChild(div);
}
return this.accessible;
},setCheckAccessible:function(_546){
this.doAccessibleCheck=_546;
},setAccessibleMode:function(){
if(this.accessible===null){
if(this.checkAccessible()){
dojo.render.html.prefixes.unshift("a11y");
}
}
return this.accessible;
}};
dojo.provide("dojo.widget.Widget");
dojo.declare("dojo.widget.Widget",null,function(){
this.children=[];
this.extraArgs={};
},{parent:null,isTopLevel:false,disabled:false,isContainer:false,widgetId:"",widgetType:"Widget",ns:"dojo",getNamespacedType:function(){
return (this.ns?this.ns+":"+this.widgetType:this.widgetType).toLowerCase();
},toString:function(){
return "[Widget "+this.getNamespacedType()+", "+(this.widgetId||"NO ID")+"]";
},repr:function(){
return this.toString();
},enable:function(){
this.disabled=false;
},disable:function(){
this.disabled=true;
},onResized:function(){
this.notifyChildrenOfResize();
},notifyChildrenOfResize:function(){
for(var i=0;i<this.children.length;i++){
var _548=this.children[i];
if(_548.onResized){
_548.onResized();
}
}
},create:function(args,_54a,_54b,ns){
if(ns){
this.ns=ns;
}
this.satisfyPropertySets(args,_54a,_54b);
this.mixInProperties(args,_54a,_54b);
this.postMixInProperties(args,_54a,_54b);
dojo.widget.manager.add(this);
this.buildRendering(args,_54a,_54b);
this.initialize(args,_54a,_54b);
this.postInitialize(args,_54a,_54b);
this.postCreate(args,_54a,_54b);
return this;
},destroy:function(_54d){
if(this.parent){
this.parent.removeChild(this);
}
this.destroyChildren();
this.uninitialize();
this.destroyRendering(_54d);
dojo.widget.manager.removeById(this.widgetId);
},destroyChildren:function(){
var _54e;
var i=0;
while(this.children.length>i){
_54e=this.children[i];
if(_54e instanceof dojo.widget.Widget){
this.removeChild(_54e);
_54e.destroy();
continue;
}
i++;
}
},getChildrenOfType:function(type,_551){
var ret=[];
var _553=dojo.lang.isFunction(type);
if(!_553){
type=type.toLowerCase();
}
for(var x=0;x<this.children.length;x++){
if(_553){
if(this.children[x] instanceof type){
ret.push(this.children[x]);
}
}else{
if(this.children[x].widgetType.toLowerCase()==type){
ret.push(this.children[x]);
}
}
if(_551){
ret=ret.concat(this.children[x].getChildrenOfType(type,_551));
}
}
return ret;
},getDescendants:function(){
var _555=[];
var _556=[this];
var elem;
while((elem=_556.pop())){
_555.push(elem);
if(elem.children){
dojo.lang.forEach(elem.children,function(elem){
_556.push(elem);
});
}
}
return _555;
},isFirstChild:function(){
return this===this.parent.children[0];
},isLastChild:function(){
return this===this.parent.children[this.parent.children.length-1];
},satisfyPropertySets:function(args){
return args;
},mixInProperties:function(args,frag){
if((args["fastMixIn"])||(frag["fastMixIn"])){
for(var x in args){
this[x]=args[x];
}
return;
}
var _55d;
var _55e=dojo.widget.lcArgsCache[this.widgetType];
if(_55e==null){
_55e={};
for(var y in this){
_55e[((new String(y)).toLowerCase())]=y;
}
dojo.widget.lcArgsCache[this.widgetType]=_55e;
}
var _560={};
for(var x in args){
if(!this[x]){
var y=_55e[(new String(x)).toLowerCase()];
if(y){
args[y]=args[x];
x=y;
}
}
if(_560[x]){
continue;
}
_560[x]=true;
if((typeof this[x])!=(typeof _55d)){
if(typeof args[x]!="string"){
this[x]=args[x];
}else{
if(dojo.lang.isString(this[x])){
this[x]=args[x];
}else{
if(dojo.lang.isNumber(this[x])){
this[x]=new Number(args[x]);
}else{
if(dojo.lang.isBoolean(this[x])){
this[x]=(args[x].toLowerCase()=="false")?false:true;
}else{
if(dojo.lang.isFunction(this[x])){
if(args[x].search(/[^\w\.]+/i)==-1){
this[x]=dojo.evalObjPath(args[x],false);
}else{
var tn=dojo.lang.nameAnonFunc(new Function(args[x]),this);
dojo.event.kwConnect({srcObj:this,srcFunc:x,adviceObj:this,adviceFunc:tn});
}
}else{
if(dojo.lang.isArray(this[x])){
this[x]=args[x].split(";");
}else{
if(this[x] instanceof Date){
this[x]=new Date(Number(args[x]));
}else{
if(typeof this[x]=="object"){
if(this[x] instanceof dojo.uri.Uri){
this[x]=dojo.uri.dojoUri(args[x]);
}else{
var _562=args[x].split(";");
for(var y=0;y<_562.length;y++){
var si=_562[y].indexOf(":");
if((si!=-1)&&(_562[y].length>si)){
this[x][_562[y].substr(0,si).replace(/^\s+|\s+$/g,"")]=_562[y].substr(si+1);
}
}
}
}else{
this[x]=args[x];
}
}
}
}
}
}
}
}
}else{
this.extraArgs[x.toLowerCase()]=args[x];
}
}
},postMixInProperties:function(args,frag,_566){
},initialize:function(args,frag,_569){
return false;
},postInitialize:function(args,frag,_56c){
return false;
},postCreate:function(args,frag,_56f){
return false;
},uninitialize:function(){
return false;
},buildRendering:function(args,frag,_572){
dojo.unimplemented("dojo.widget.Widget.buildRendering, on "+this.toString()+", ");
return false;
},destroyRendering:function(){
dojo.unimplemented("dojo.widget.Widget.destroyRendering");
return false;
},addedTo:function(_573){
},addChild:function(_574){
dojo.unimplemented("dojo.widget.Widget.addChild");
return false;
},removeChild:function(_575){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_575){
this.children.splice(x,1);
_575.parent=null;
break;
}
}
return _575;
},getPreviousSibling:function(){
var idx=this.getParentIndex();
if(idx<=0){
return null;
}
return this.parent.children[idx-1];
},getSiblings:function(){
return this.parent.children;
},getParentIndex:function(){
return dojo.lang.indexOf(this.parent.children,this,true);
},getNextSibling:function(){
var idx=this.getParentIndex();
if(idx==this.parent.children.length-1){
return null;
}
if(idx<0){
return null;
}
return this.parent.children[idx+1];
}});
dojo.widget.lcArgsCache={};
dojo.widget.tags={};
dojo.widget.tags.addParseTreeHandler=function(type){
dojo.deprecated("addParseTreeHandler",". ParseTreeHandlers are now reserved for components. Any unfiltered DojoML tag without a ParseTreeHandler is assumed to be a widget","0.5");
};
dojo.widget.tags["dojo:propertyset"]=function(_57a,_57b,_57c){
var _57d=_57b.parseProperties(_57a["dojo:propertyset"]);
};
dojo.widget.tags["dojo:connect"]=function(_57e,_57f,_580){
var _581=_57f.parseProperties(_57e["dojo:connect"]);
};
dojo.widget.buildWidgetFromParseTree=function(type,frag,_584,_585,_586,_587){
dojo.a11y.setAccessibleMode();
var _588=type.split(":");
_588=(_588.length==2)?_588[1]:type;
var _589=_587||_584.parseProperties(frag[frag["ns"]+":"+_588]);
var _58a=dojo.widget.manager.getImplementation(_588,null,null,frag["ns"]);
if(!_58a){
throw new Error("cannot find \""+type+"\" widget");
}else{
if(!_58a.create){
throw new Error("\""+type+"\" widget object has no \"create\" method and does not appear to implement *Widget");
}
}
_589["dojoinsertionindex"]=_586;
var ret=_58a.create(_589,frag,_585,frag["ns"]);
return ret;
};
dojo.widget.defineWidget=function(_58c,_58d,_58e,init,_590){
if(dojo.lang.isString(arguments[3])){
dojo.widget._defineWidget(arguments[0],arguments[3],arguments[1],arguments[4],arguments[2]);
}else{
var args=[arguments[0]],p=3;
if(dojo.lang.isString(arguments[1])){
args.push(arguments[1],arguments[2]);
}else{
args.push("",arguments[1]);
p=2;
}
if(dojo.lang.isFunction(arguments[p])){
args.push(arguments[p],arguments[p+1]);
}else{
args.push(null,arguments[p]);
}
dojo.widget._defineWidget.apply(this,args);
}
};
dojo.widget.defineWidget.renderers="html|svg|vml";
dojo.widget._defineWidget=function(_593,_594,_595,init,_597){
var _598=_593.split(".");
var type=_598.pop();
var regx="\\.("+(_594?_594+"|":"")+dojo.widget.defineWidget.renderers+")\\.";
var r=_593.search(new RegExp(regx));
_598=(r<0?_598.join("."):_593.substr(0,r));
dojo.widget.manager.registerWidgetPackage(_598);
var pos=_598.indexOf(".");
var _59d=(pos>-1)?_598.substring(0,pos):_598;
_597=(_597)||{};
_597.widgetType=type;
if((!init)&&(_597["classConstructor"])){
init=_597.classConstructor;
delete _597.classConstructor;
}
dojo.declare(_593,_595,init,_597);
};
dojo.provide("dojo.widget.Parse");
dojo.widget.Parse=function(_59e){
this.propertySetsList=[];
this.fragment=_59e;
this.createComponents=function(frag,_5a0){
var _5a1=[];
var _5a2=false;
try{
if(frag&&frag.tagName&&(frag!=frag.nodeRef)){
var _5a3=dojo.widget.tags;
var tna=String(frag.tagName).split(";");
for(var x=0;x<tna.length;x++){
var ltn=tna[x].replace(/^\s+|\s+$/g,"").toLowerCase();
frag.tagName=ltn;
var ret;
if(_5a3[ltn]){
_5a2=true;
ret=_5a3[ltn](frag,this,_5a0,frag.index);
_5a1.push(ret);
}else{
if(ltn.indexOf(":")==-1){
ltn="dojo:"+ltn;
}
ret=dojo.widget.buildWidgetFromParseTree(ltn,frag,this,_5a0,frag.index);
if(ret){
_5a2=true;
_5a1.push(ret);
}
}
}
}
}
catch(e){
dojo.debug("dojo.widget.Parse: error:",e);
}
if(!_5a2){
_5a1=_5a1.concat(this.createSubComponents(frag,_5a0));
}
return _5a1;
};
this.createSubComponents=function(_5a8,_5a9){
var frag,_5ab=[];
for(var item in _5a8){
frag=_5a8[item];
if(frag&&typeof frag=="object"&&(frag!=_5a8.nodeRef)&&(frag!=_5a8.tagName)&&(!dojo.dom.isNode(frag))){
_5ab=_5ab.concat(this.createComponents(frag,_5a9));
}
}
return _5ab;
};
this.parsePropertySets=function(_5ad){
return [];
};
this.parseProperties=function(_5ae){
var _5af={};
for(var item in _5ae){
if((_5ae[item]==_5ae.tagName)||(_5ae[item]==_5ae.nodeRef)){
}else{
var frag=_5ae[item];
if(frag.tagName&&dojo.widget.tags[frag.tagName.toLowerCase()]){
}else{
if(frag[0]&&frag[0].value!=""&&frag[0].value!=null){
try{
if(item.toLowerCase()=="dataprovider"){
var _5b2=this;
this.getDataProvider(_5b2,frag[0].value);
_5af.dataProvider=this.dataProvider;
}
_5af[item]=frag[0].value;
var _5b3=this.parseProperties(frag);
for(var _5b4 in _5b3){
_5af[_5b4]=_5b3[_5b4];
}
}
catch(e){
dojo.debug(e);
}
}
}
switch(item.toLowerCase()){
case "checked":
case "disabled":
if(typeof _5af[item]!="boolean"){
_5af[item]=true;
}
break;
}
}
}
return _5af;
};
this.getDataProvider=function(_5b5,_5b6){
dojo.io.bind({url:_5b6,load:function(type,_5b8){
if(type=="load"){
_5b5.dataProvider=_5b8;
}
},mimetype:"text/javascript",sync:true});
};
this.getPropertySetById=function(_5b9){
for(var x=0;x<this.propertySetsList.length;x++){
if(_5b9==this.propertySetsList[x]["id"][0].value){
return this.propertySetsList[x];
}
}
return "";
};
this.getPropertySetsByType=function(_5bb){
var _5bc=[];
for(var x=0;x<this.propertySetsList.length;x++){
var cpl=this.propertySetsList[x];
var cpcc=cpl.componentClass||cpl.componentType||null;
var _5c0=this.propertySetsList[x]["id"][0].value;
if(cpcc&&(_5c0==cpcc[0].value)){
_5bc.push(cpl);
}
}
return _5bc;
};
this.getPropertySets=function(_5c1){
var ppl="dojo:propertyproviderlist";
var _5c3=[];
var _5c4=_5c1.tagName;
if(_5c1[ppl]){
var _5c5=_5c1[ppl].value.split(" ");
for(var _5c6 in _5c5){
if((_5c6.indexOf("..")==-1)&&(_5c6.indexOf("://")==-1)){
var _5c7=this.getPropertySetById(_5c6);
if(_5c7!=""){
_5c3.push(_5c7);
}
}else{
}
}
}
return this.getPropertySetsByType(_5c4).concat(_5c3);
};
this.createComponentFromScript=function(_5c8,_5c9,_5ca,ns){
_5ca.fastMixIn=true;
var ltn=(ns||"dojo")+":"+_5c9.toLowerCase();
if(dojo.widget.tags[ltn]){
return [dojo.widget.tags[ltn](_5ca,this,null,null,_5ca)];
}
return [dojo.widget.buildWidgetFromParseTree(ltn,_5ca,this,null,null,_5ca)];
};
};
dojo.widget._parser_collection={"dojo":new dojo.widget.Parse()};
dojo.widget.getParser=function(name){
if(!name){
name="dojo";
}
if(!this._parser_collection[name]){
this._parser_collection[name]=new dojo.widget.Parse();
}
return this._parser_collection[name];
};
dojo.widget.createWidget=function(name,_5cf,_5d0,_5d1){
var _5d2=false;
var _5d3=(typeof name=="string");
if(_5d3){
var pos=name.indexOf(":");
var ns=(pos>-1)?name.substring(0,pos):"dojo";
if(pos>-1){
name=name.substring(pos+1);
}
var _5d6=name.toLowerCase();
var _5d7=ns+":"+_5d6;
_5d2=(dojo.byId(name)&&!dojo.widget.tags[_5d7]);
}
if((arguments.length==1)&&(_5d2||!_5d3)){
var xp=new dojo.xml.Parse();
var tn=_5d2?dojo.byId(name):name;
return dojo.widget.getParser().createComponents(xp.parseElement(tn,null,true))[0];
}
function fromScript(_5da,name,_5dc,ns){
_5dc[_5d7]={dojotype:[{value:_5d6}],nodeRef:_5da,fastMixIn:true};
_5dc.ns=ns;
return dojo.widget.getParser().createComponentFromScript(_5da,name,_5dc,ns);
}
_5cf=_5cf||{};
var _5de=false;
var tn=null;
var h=dojo.render.html.capable;
if(h){
tn=document.createElement("span");
}
if(!_5d0){
_5de=true;
_5d0=tn;
if(h){
dojo.body().appendChild(_5d0);
}
}else{
if(_5d1){
dojo.dom.insertAtPosition(tn,_5d0,_5d1);
}else{
tn=_5d0;
}
}
var _5e0=fromScript(tn,name.toLowerCase(),_5cf,ns);
if((!_5e0)||(!_5e0[0])||(typeof _5e0[0].widgetType=="undefined")){
throw new Error("createWidget: Creation of \""+name+"\" widget failed.");
}
try{
if(_5de&&_5e0[0].domNode.parentNode){
_5e0[0].domNode.parentNode.removeChild(_5e0[0].domNode);
}
}
catch(e){
dojo.debug(e);
}
return _5e0[0];
};
dojo.provide("dojo.widget.DomWidget");
dojo.widget._cssFiles={};
dojo.widget._cssStrings={};
dojo.widget._templateCache={};
dojo.widget.defaultStrings={dojoRoot:dojo.hostenv.getBaseScriptUri(),dojoWidgetModuleUri:dojo.uri.moduleUri("dojo.widget"),baseScriptUri:dojo.hostenv.getBaseScriptUri()};
dojo.widget.fillFromTemplateCache=function(obj,_5e2,_5e3,_5e4){
var _5e5=_5e2||obj.templatePath;
var _5e6=dojo.widget._templateCache;
if(!_5e5&&!obj["widgetType"]){
do{
var _5e7="__dummyTemplate__"+dojo.widget._templateCache.dummyCount++;
}while(_5e6[_5e7]);
obj.widgetType=_5e7;
}
var wt=_5e5?_5e5.toString():obj.widgetType;
var ts=_5e6[wt];
if(!ts){
_5e6[wt]={"string":null,"node":null};
if(_5e4){
ts={};
}else{
ts=_5e6[wt];
}
}
if((!obj.templateString)&&(!_5e4)){
obj.templateString=_5e3||ts["string"];
}
if(obj.templateString){
obj.templateString=this._sanitizeTemplateString(obj.templateString);
}
if((!obj.templateNode)&&(!_5e4)){
obj.templateNode=ts["node"];
}
if((!obj.templateNode)&&(!obj.templateString)&&(_5e5)){
var _5ea=this._sanitizeTemplateString(dojo.hostenv.getText(_5e5));
obj.templateString=_5ea;
if(!_5e4){
_5e6[wt]["string"]=_5ea;
}
}
if((!ts["string"])&&(!_5e4)){
ts.string=obj.templateString;
}
};
dojo.widget._sanitizeTemplateString=function(_5eb){
if(_5eb){
_5eb=_5eb.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _5ec=_5eb.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_5ec){
_5eb=_5ec[1];
}
}else{
_5eb="";
}
return _5eb;
};
dojo.widget._templateCache.dummyCount=0;
dojo.widget.attachProperties=["dojoAttachPoint","id"];
dojo.widget.eventAttachProperty="dojoAttachEvent";
dojo.widget.onBuildProperty="dojoOnBuild";
dojo.widget.waiNames=["waiRole","waiState"];
dojo.widget.wai={waiRole:{name:"waiRole","namespace":"http://www.w3.org/TR/xhtml2",alias:"x2",prefix:"wairole:"},waiState:{name:"waiState","namespace":"http://www.w3.org/2005/07/aaa",alias:"aaa",prefix:""},setAttr:function(node,ns,attr,_5f0){
if(dojo.render.html.ie){
node.setAttribute(this[ns].alias+":"+attr,this[ns].prefix+_5f0);
}else{
node.setAttributeNS(this[ns]["namespace"],attr,this[ns].prefix+_5f0);
}
},getAttr:function(node,ns,attr){
if(dojo.render.html.ie){
return node.getAttribute(this[ns].alias+":"+attr);
}else{
return node.getAttributeNS(this[ns]["namespace"],attr);
}
},removeAttr:function(node,ns,attr){
var _5f7=true;
if(dojo.render.html.ie){
_5f7=node.removeAttribute(this[ns].alias+":"+attr);
}else{
node.removeAttributeNS(this[ns]["namespace"],attr);
}
return _5f7;
}};
dojo.widget.attachTemplateNodes=function(_5f8,_5f9,_5fa){
var _5fb=dojo.dom.ELEMENT_NODE;
function trim(str){
return str.replace(/^\s+|\s+$/g,"");
}
if(!_5f8){
_5f8=_5f9.domNode;
}
if(_5f8.nodeType!=_5fb){
return;
}
var _5fd=_5f8.all||_5f8.getElementsByTagName("*");
var _5fe=_5f9;
for(var x=-1;x<_5fd.length;x++){
var _600=(x==-1)?_5f8:_5fd[x];
var _601=[];
if(!_5f9.widgetsInTemplate||!_600.getAttribute("dojoType")){
for(var y=0;y<this.attachProperties.length;y++){
var _603=_600.getAttribute(this.attachProperties[y]);
if(_603){
_601=_603.split(";");
for(var z=0;z<_601.length;z++){
if(dojo.lang.isArray(_5f9[_601[z]])){
_5f9[_601[z]].push(_600);
}else{
_5f9[_601[z]]=_600;
}
}
break;
}
}
var _605=_600.getAttribute(this.eventAttachProperty);
if(_605){
var evts=_605.split(";");
for(var y=0;y<evts.length;y++){
if((!evts[y])||(!evts[y].length)){
continue;
}
var _607=null;
var tevt=trim(evts[y]);
if(evts[y].indexOf(":")>=0){
var _609=tevt.split(":");
tevt=trim(_609[0]);
_607=trim(_609[1]);
}
if(!_607){
_607=tevt;
}
var tf=function(){
var ntf=new String(_607);
return function(evt){
if(_5fe[ntf]){
_5fe[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_600,tevt,tf,false,true);
}
}
for(var y=0;y<_5fa.length;y++){
var _60d=_600.getAttribute(_5fa[y]);
if((_60d)&&(_60d.length)){
var _607=null;
var _60e=_5fa[y].substr(4);
_607=trim(_60d);
var _60f=[_607];
if(_607.indexOf(";")>=0){
_60f=dojo.lang.map(_607.split(";"),trim);
}
for(var z=0;z<_60f.length;z++){
if(!_60f[z].length){
continue;
}
var tf=function(){
var ntf=new String(_60f[z]);
return function(evt){
if(_5fe[ntf]){
_5fe[ntf](dojo.event.browser.fixEvent(evt,this));
}
};
}();
dojo.event.browser.addListener(_600,_60e,tf,false,true);
}
}
}
}
var _612=_600.getAttribute(this.templateProperty);
if(_612){
_5f9[_612]=_600;
}
dojo.lang.forEach(dojo.widget.waiNames,function(name){
var wai=dojo.widget.wai[name];
var val=_600.getAttribute(wai.name);
if(val){
if(val.indexOf("-")==-1){
dojo.widget.wai.setAttr(_600,wai.name,"role",val);
}else{
var _616=val.split("-");
dojo.widget.wai.setAttr(_600,wai.name,_616[0],_616[1]);
}
}
},this);
var _617=_600.getAttribute(this.onBuildProperty);
if(_617){
eval("var node = baseNode; var widget = targetObj; "+_617);
}
}
};
dojo.widget.getDojoEventsFromStr=function(str){
var re=/(dojoOn([a-z]+)(\s?))=/gi;
var evts=str?str.match(re)||[]:[];
var ret=[];
var lem={};
for(var x=0;x<evts.length;x++){
if(evts[x].length<1){
continue;
}
var cm=evts[x].replace(/\s/,"");
cm=(cm.slice(0,cm.length-1));
if(!lem[cm]){
lem[cm]=true;
ret.push(cm);
}
}
return ret;
};
dojo.declare("dojo.widget.DomWidget",dojo.widget.Widget,function(){
if((arguments.length>0)&&(typeof arguments[0]=="object")){
this.create(arguments[0]);
}
},{templateNode:null,templateString:null,templateCssString:null,preventClobber:false,domNode:null,containerNode:null,widgetsInTemplate:false,addChild:function(_61f,_620,pos,ref,_623){
if(!this.isContainer){
dojo.debug("dojo.widget.DomWidget.addChild() attempted on non-container widget");
return null;
}else{
if(_623==undefined){
_623=this.children.length;
}
this.addWidgetAsDirectChild(_61f,_620,pos,ref,_623);
this.registerChild(_61f,_623);
}
return _61f;
},addWidgetAsDirectChild:function(_624,_625,pos,ref,_628){
if((!this.containerNode)&&(!_625)){
this.containerNode=this.domNode;
}
var cn=(_625)?_625:this.containerNode;
if(!pos){
pos="after";
}
if(!ref){
if(!cn){
cn=dojo.body();
}
ref=cn.lastChild;
}
if(!_628){
_628=0;
}
_624.domNode.setAttribute("dojoinsertionindex",_628);
if(!ref){
cn.appendChild(_624.domNode);
}else{
if(pos=="insertAtIndex"){
dojo.dom.insertAtIndex(_624.domNode,ref.parentNode,_628);
}else{
if((pos=="after")&&(ref===cn.lastChild)){
cn.appendChild(_624.domNode);
}else{
dojo.dom.insertAtPosition(_624.domNode,ref,pos);
}
}
}
},registerChild:function(_62a,_62b){
_62a.dojoInsertionIndex=_62b;
var idx=-1;
for(var i=0;i<this.children.length;i++){
if(this.children[i].dojoInsertionIndex<=_62b){
idx=i;
}
}
this.children.splice(idx+1,0,_62a);
_62a.parent=this;
_62a.addedTo(this,idx+1);
delete dojo.widget.manager.topWidgets[_62a.widgetId];
},removeChild:function(_62e){
dojo.dom.removeNode(_62e.domNode);
return dojo.widget.DomWidget.superclass.removeChild.call(this,_62e);
},getFragNodeRef:function(frag){
if(!frag){
return null;
}
if(!frag[this.getNamespacedType()]){
dojo.raise("Error: no frag for widget type "+this.getNamespacedType()+", id "+this.widgetId+" (maybe a widget has set it's type incorrectly)");
}
return frag[this.getNamespacedType()]["nodeRef"];
},postInitialize:function(args,frag,_632){
var _633=this.getFragNodeRef(frag);
if(_632&&(_632.snarfChildDomOutput||!_633)){
_632.addWidgetAsDirectChild(this,"","insertAtIndex","",args["dojoinsertionindex"],_633);
}else{
if(_633){
if(this.domNode&&(this.domNode!==_633)){
this._sourceNodeRef=dojo.dom.replaceNode(_633,this.domNode);
}
}
}
if(_632){
_632.registerChild(this,args.dojoinsertionindex);
}else{
dojo.widget.manager.topWidgets[this.widgetId]=this;
}
if(this.widgetsInTemplate){
var _634=new dojo.xml.Parse();
var _635;
var _636=this.domNode.getElementsByTagName("*");
for(var i=0;i<_636.length;i++){
if(_636[i].getAttribute("dojoAttachPoint")=="subContainerWidget"){
_635=_636[i];
}
if(_636[i].getAttribute("dojoType")){
_636[i].setAttribute("isSubWidget",true);
}
}
if(this.isContainer&&!this.containerNode){
if(_635){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,_635);
frag["dojoDontFollow"]=true;
}
}else{
dojo.debug("No subContainerWidget node can be found in template file for widget "+this);
}
}
var _639=_634.parseElement(this.domNode,null,true);
dojo.widget.getParser().createSubComponents(_639,this);
var _63a=[];
var _63b=[this];
var w;
while((w=_63b.pop())){
for(var i=0;i<w.children.length;i++){
var _63d=w.children[i];
if(_63d._processedSubWidgets||!_63d.extraArgs["issubwidget"]){
continue;
}
_63a.push(_63d);
if(_63d.isContainer){
_63b.push(_63d);
}
}
}
for(var i=0;i<_63a.length;i++){
var _63e=_63a[i];
if(_63e._processedSubWidgets){
dojo.debug("This should not happen: widget._processedSubWidgets is already true!");
return;
}
_63e._processedSubWidgets=true;
if(_63e.extraArgs["dojoattachevent"]){
var evts=_63e.extraArgs["dojoattachevent"].split(";");
for(var j=0;j<evts.length;j++){
var _641=null;
var tevt=dojo.string.trim(evts[j]);
if(tevt.indexOf(":")>=0){
var _643=tevt.split(":");
tevt=dojo.string.trim(_643[0]);
_641=dojo.string.trim(_643[1]);
}
if(!_641){
_641=tevt;
}
if(dojo.lang.isFunction(_63e[tevt])){
dojo.event.kwConnect({srcObj:_63e,srcFunc:tevt,targetObj:this,targetFunc:_641});
}else{
alert(tevt+" is not a function in widget "+_63e);
}
}
}
if(_63e.extraArgs["dojoattachpoint"]){
this[_63e.extraArgs["dojoattachpoint"]]=_63e;
}
}
}
if(this.isContainer&&!frag["dojoDontFollow"]){
dojo.widget.getParser().createSubComponents(frag,this);
}
},buildRendering:function(args,frag){
var ts=dojo.widget._templateCache[this.widgetType];
if(args["templatecsspath"]){
args["templateCssPath"]=args["templatecsspath"];
}
var _647=args["templateCssPath"]||this.templateCssPath;
if(_647&&!dojo.widget._cssFiles[_647.toString()]){
if((!this.templateCssString)&&(_647)){
this.templateCssString=dojo.hostenv.getText(_647);
this.templateCssPath=null;
}
dojo.widget._cssFiles[_647.toString()]=true;
}
if((this["templateCssString"])&&(!dojo.widget._cssStrings[this.templateCssString])){
dojo.html.insertCssText(this.templateCssString,null,_647);
dojo.widget._cssStrings[this.templateCssString]=true;
}
if((!this.preventClobber)&&((this.templatePath)||(this.templateNode)||((this["templateString"])&&(this.templateString.length))||((typeof ts!="undefined")&&((ts["string"])||(ts["node"]))))){
this.buildFromTemplate(args,frag);
}else{
this.domNode=this.getFragNodeRef(frag);
}
this.fillInTemplate(args,frag);
},buildFromTemplate:function(args,frag){
var _64a=false;
if(args["templatepath"]){
args["templatePath"]=args["templatepath"];
}
dojo.widget.fillFromTemplateCache(this,args["templatePath"],null,_64a);
var ts=dojo.widget._templateCache[this.templatePath?this.templatePath.toString():this.widgetType];
if((ts)&&(!_64a)){
if(!this.templateString.length){
this.templateString=ts["string"];
}
if(!this.templateNode){
this.templateNode=ts["node"];
}
}
var _64c=false;
var node=null;
var tstr=this.templateString;
if((!this.templateNode)&&(this.templateString)){
_64c=this.templateString.match(/\$\{([^\}]+)\}/g);
if(_64c){
var hash=this.strings||{};
for(var key in dojo.widget.defaultStrings){
if(dojo.lang.isUndefined(hash[key])){
hash[key]=dojo.widget.defaultStrings[key];
}
}
for(var i=0;i<_64c.length;i++){
var key=_64c[i];
key=key.substring(2,key.length-1);
var kval=(key.substring(0,5)=="this.")?dojo.lang.getObjPathValue(key.substring(5),this):hash[key];
var _653;
if((kval)||(dojo.lang.isString(kval))){
_653=new String((dojo.lang.isFunction(kval))?kval.call(this,key,this.templateString):kval);
while(_653.indexOf("\"")>-1){
_653=_653.replace("\"","&quot;");
}
tstr=tstr.replace(_64c[i],_653);
}
}
}else{
this.templateNode=this.createNodesFromText(this.templateString,true)[0];
if(!_64a){
ts.node=this.templateNode;
}
}
}
if((!this.templateNode)&&(!_64c)){
dojo.debug("DomWidget.buildFromTemplate: could not create template");
return false;
}else{
if(!_64c){
node=this.templateNode.cloneNode(true);
if(!node){
return false;
}
}else{
node=this.createNodesFromText(tstr,true)[0];
}
}
this.domNode=node;
this.attachTemplateNodes();
if(this.isContainer&&this.containerNode){
var src=this.getFragNodeRef(frag);
if(src){
dojo.dom.moveChildren(src,this.containerNode);
}
}
},attachTemplateNodes:function(_655,_656){
if(!_655){
_655=this.domNode;
}
if(!_656){
_656=this;
}
return dojo.widget.attachTemplateNodes(_655,_656,dojo.widget.getDojoEventsFromStr(this.templateString));
},fillInTemplate:function(){
},destroyRendering:function(){
try{
dojo.dom.destroyNode(this.domNode);
delete this.domNode;
}
catch(e){
}
if(this._sourceNodeRef){
try{
dojo.dom.destroyNode(this._sourceNodeRef);
}
catch(e){
}
}
},createNodesFromText:function(){
dojo.unimplemented("dojo.widget.DomWidget.createNodesFromText");
}});
dojo.provide("dojo.html.util");
dojo.html.getElementWindow=function(_657){
return dojo.html.getDocumentWindow(_657.ownerDocument);
};
dojo.html.getDocumentWindow=function(doc){
if(dojo.render.html.safari&&!doc._parentWindow){
var fix=function(win){
win.document._parentWindow=win;
for(var i=0;i<win.frames.length;i++){
fix(win.frames[i]);
}
};
fix(window.top);
}
if(dojo.render.html.ie&&window!==document.parentWindow&&!doc._parentWindow){
doc.parentWindow.execScript("document._parentWindow = window;","Javascript");
var win=doc._parentWindow;
doc._parentWindow=null;
return win;
}
return doc._parentWindow||doc.parentWindow||doc.defaultView;
};
dojo.html.gravity=function(node,e){
node=dojo.byId(node);
var _65f=dojo.html.getCursorPosition(e);
with(dojo.html){
var _660=getAbsolutePosition(node,true);
var bb=getBorderBox(node);
var _662=_660.x+(bb.width/2);
var _663=_660.y+(bb.height/2);
}
with(dojo.html.gravity){
return ((_65f.x<_662?WEST:EAST)|(_65f.y<_663?NORTH:SOUTH));
}
};
dojo.html.gravity.NORTH=1;
dojo.html.gravity.SOUTH=1<<1;
dojo.html.gravity.EAST=1<<2;
dojo.html.gravity.WEST=1<<3;
dojo.html.overElement=function(_664,e){
_664=dojo.byId(_664);
var _666=dojo.html.getCursorPosition(e);
var bb=dojo.html.getBorderBox(_664);
var _668=dojo.html.getAbsolutePosition(_664,true,dojo.html.boxSizing.BORDER_BOX);
var top=_668.y;
var _66a=top+bb.height;
var left=_668.x;
var _66c=left+bb.width;
return (_666.x>=left&&_666.x<=_66c&&_666.y>=top&&_666.y<=_66a);
};
dojo.html.renderedTextContent=function(node){
node=dojo.byId(node);
var _66e="";
if(node==null){
return _66e;
}
for(var i=0;i<node.childNodes.length;i++){
switch(node.childNodes[i].nodeType){
case 1:
case 5:
var _670="unknown";
try{
_670=dojo.html.getStyle(node.childNodes[i],"display");
}
catch(E){
}
switch(_670){
case "block":
case "list-item":
case "run-in":
case "table":
case "table-row-group":
case "table-header-group":
case "table-footer-group":
case "table-row":
case "table-column-group":
case "table-column":
case "table-cell":
case "table-caption":
_66e+="\n";
_66e+=dojo.html.renderedTextContent(node.childNodes[i]);
_66e+="\n";
break;
case "none":
break;
default:
if(node.childNodes[i].tagName&&node.childNodes[i].tagName.toLowerCase()=="br"){
_66e+="\n";
}else{
_66e+=dojo.html.renderedTextContent(node.childNodes[i]);
}
break;
}
break;
case 3:
case 2:
case 4:
var text=node.childNodes[i].nodeValue;
var _672="unknown";
try{
_672=dojo.html.getStyle(node,"text-transform");
}
catch(E){
}
switch(_672){
case "capitalize":
var _673=text.split(" ");
for(var i=0;i<_673.length;i++){
_673[i]=_673[i].charAt(0).toUpperCase()+_673[i].substring(1);
}
text=_673.join(" ");
break;
case "uppercase":
text=text.toUpperCase();
break;
case "lowercase":
text=text.toLowerCase();
break;
default:
break;
}
switch(_672){
case "nowrap":
break;
case "pre-wrap":
break;
case "pre-line":
break;
case "pre":
break;
default:
text=text.replace(/\s+/," ");
if(/\s$/.test(_66e)){
text.replace(/^\s/,"");
}
break;
}
_66e+=text;
break;
default:
break;
}
}
return _66e;
};
dojo.html.createNodesFromText=function(txt,trim){
if(trim){
txt=txt.replace(/^\s+|\s+$/g,"");
}
var tn=dojo.doc().createElement("div");
tn.style.visibility="hidden";
dojo.body().appendChild(tn);
var _677="none";
if((/^<t[dh][\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody><tr>"+txt+"</tr></tbody></table>";
_677="cell";
}else{
if((/^<tr[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table><tbody>"+txt+"</tbody></table>";
_677="row";
}else{
if((/^<(thead|tbody|tfoot)[\s\r\n>]/i).test(txt.replace(/^\s+/))){
txt="<table>"+txt+"</table>";
_677="section";
}
}
}
tn.innerHTML=txt;
if(tn["normalize"]){
tn.normalize();
}
var _678=null;
switch(_677){
case "cell":
_678=tn.getElementsByTagName("tr")[0];
break;
case "row":
_678=tn.getElementsByTagName("tbody")[0];
break;
case "section":
_678=tn.getElementsByTagName("table")[0];
break;
default:
_678=tn;
break;
}
var _679=[];
for(var x=0;x<_678.childNodes.length;x++){
_679.push(_678.childNodes[x].cloneNode(true));
}
tn.style.display="none";
dojo.html.destroyNode(tn);
return _679;
};
dojo.html.placeOnScreen=function(node,_67c,_67d,_67e,_67f,_680,_681){
if(_67c instanceof Array||typeof _67c=="array"){
_681=_680;
_680=_67f;
_67f=_67e;
_67e=_67d;
_67d=_67c[1];
_67c=_67c[0];
}
if(_680 instanceof String||typeof _680=="string"){
_680=_680.split(",");
}
if(!isNaN(_67e)){
_67e=[Number(_67e),Number(_67e)];
}else{
if(!(_67e instanceof Array||typeof _67e=="array")){
_67e=[0,0];
}
}
var _682=dojo.html.getScroll().offset;
var view=dojo.html.getViewport();
node=dojo.byId(node);
var _684=node.style.display;
node.style.display="";
var bb=dojo.html.getBorderBox(node);
var w=bb.width;
var h=bb.height;
node.style.display=_684;
if(!(_680 instanceof Array||typeof _680=="array")){
_680=["TL"];
}
var _688,_689,_68a=Infinity,_68b;
for(var _68c=0;_68c<_680.length;++_68c){
var _68d=_680[_68c];
var _68e=true;
var tryX=_67c-(_68d.charAt(1)=="L"?0:w)+_67e[0]*(_68d.charAt(1)=="L"?1:-1);
var tryY=_67d-(_68d.charAt(0)=="T"?0:h)+_67e[1]*(_68d.charAt(0)=="T"?1:-1);
if(_67f){
tryX-=_682.x;
tryY-=_682.y;
}
if(tryX<0){
tryX=0;
_68e=false;
}
if(tryY<0){
tryY=0;
_68e=false;
}
var x=tryX+w;
if(x>view.width){
x=view.width-w;
_68e=false;
}else{
x=tryX;
}
x=Math.max(_67e[0],x)+_682.x;
var y=tryY+h;
if(y>view.height){
y=view.height-h;
_68e=false;
}else{
y=tryY;
}
y=Math.max(_67e[1],y)+_682.y;
if(_68e){
_688=x;
_689=y;
_68a=0;
_68b=_68d;
break;
}else{
var dist=Math.pow(x-tryX-_682.x,2)+Math.pow(y-tryY-_682.y,2);
if(_68a>dist){
_68a=dist;
_688=x;
_689=y;
_68b=_68d;
}
}
}
if(!_681){
node.style.left=_688+"px";
node.style.top=_689+"px";
}
return {left:_688,top:_689,x:_688,y:_689,dist:_68a,corner:_68b};
};
dojo.html.placeOnScreenPoint=function(node,_695,_696,_697,_698){
dojo.deprecated("dojo.html.placeOnScreenPoint","use dojo.html.placeOnScreen() instead","0.5");
return dojo.html.placeOnScreen(node,_695,_696,_697,_698,["TL","TR","BL","BR"]);
};
dojo.html.placeOnScreenAroundElement=function(node,_69a,_69b,_69c,_69d,_69e){
var best,_6a0=Infinity;
_69a=dojo.byId(_69a);
var _6a1=_69a.style.display;
_69a.style.display="";
var mb=dojo.html.getElementBox(_69a,_69c);
var _6a3=mb.width;
var _6a4=mb.height;
var _6a5=dojo.html.getAbsolutePosition(_69a,true,_69c);
_69a.style.display=_6a1;
for(var _6a6 in _69d){
var pos,_6a8,_6a9;
var _6aa=_69d[_6a6];
_6a8=_6a5.x+(_6a6.charAt(1)=="L"?0:_6a3);
_6a9=_6a5.y+(_6a6.charAt(0)=="T"?0:_6a4);
pos=dojo.html.placeOnScreen(node,_6a8,_6a9,_69b,true,_6aa,true);
if(pos.dist==0){
best=pos;
break;
}else{
if(_6a0>pos.dist){
_6a0=pos.dist;
best=pos;
}
}
}
if(!_69e){
node.style.left=best.left+"px";
node.style.top=best.top+"px";
}
return best;
};
dojo.html.scrollIntoView=function(node){
if(!node){
return;
}
if(dojo.render.html.ie){
if(dojo.html.getBorderBox(node.parentNode).height<=node.parentNode.scrollHeight){
node.scrollIntoView(false);
}
}else{
if(dojo.render.html.mozilla){
node.scrollIntoView(false);
}else{
var _6ac=node.parentNode;
var _6ad=_6ac.scrollTop+dojo.html.getBorderBox(_6ac).height;
var _6ae=node.offsetTop+dojo.html.getMarginBox(node).height;
if(_6ad<_6ae){
_6ac.scrollTop+=(_6ae-_6ad);
}else{
if(_6ac.scrollTop>node.offsetTop){
_6ac.scrollTop-=(_6ac.scrollTop-node.offsetTop);
}
}
}
}
};
dojo.provide("dojo.gfx.color");
dojo.gfx.color.Color=function(r,g,b,a){
if(dojo.lang.isArray(r)){
this.r=r[0];
this.g=r[1];
this.b=r[2];
this.a=r[3]||1;
}else{
if(dojo.lang.isString(r)){
var rgb=dojo.gfx.color.extractRGB(r);
this.r=rgb[0];
this.g=rgb[1];
this.b=rgb[2];
this.a=g||1;
}else{
if(r instanceof dojo.gfx.color.Color){
this.r=r.r;
this.b=r.b;
this.g=r.g;
this.a=r.a;
}else{
this.r=r;
this.g=g;
this.b=b;
this.a=a;
}
}
}
};
dojo.gfx.color.Color.fromArray=function(arr){
return new dojo.gfx.color.Color(arr[0],arr[1],arr[2],arr[3]);
};
dojo.extend(dojo.gfx.color.Color,{toRgb:function(_6b5){
if(_6b5){
return this.toRgba();
}else{
return [this.r,this.g,this.b];
}
},toRgba:function(){
return [this.r,this.g,this.b,this.a];
},toHex:function(){
return dojo.gfx.color.rgb2hex(this.toRgb());
},toCss:function(){
return "rgb("+this.toRgb().join()+")";
},toString:function(){
return this.toHex();
},blend:function(_6b6,_6b7){
var rgb=null;
if(dojo.lang.isArray(_6b6)){
rgb=_6b6;
}else{
if(_6b6 instanceof dojo.gfx.color.Color){
rgb=_6b6.toRgb();
}else{
rgb=new dojo.gfx.color.Color(_6b6).toRgb();
}
}
return dojo.gfx.color.blend(this.toRgb(),rgb,_6b7);
}});
dojo.gfx.color.named={white:[255,255,255],black:[0,0,0],red:[255,0,0],green:[0,255,0],lime:[0,255,0],blue:[0,0,255],navy:[0,0,128],gray:[128,128,128],silver:[192,192,192]};
dojo.gfx.color.blend=function(a,b,_6bb){
if(typeof a=="string"){
return dojo.gfx.color.blendHex(a,b,_6bb);
}
if(!_6bb){
_6bb=0;
}
_6bb=Math.min(Math.max(-1,_6bb),1);
_6bb=((_6bb+1)/2);
var c=[];
for(var x=0;x<3;x++){
c[x]=parseInt(b[x]+((a[x]-b[x])*_6bb));
}
return c;
};
dojo.gfx.color.blendHex=function(a,b,_6c0){
return dojo.gfx.color.rgb2hex(dojo.gfx.color.blend(dojo.gfx.color.hex2rgb(a),dojo.gfx.color.hex2rgb(b),_6c0));
};
dojo.gfx.color.extractRGB=function(_6c1){
var hex="0123456789abcdef";
_6c1=_6c1.toLowerCase();
if(_6c1.indexOf("rgb")==0){
var _6c3=_6c1.match(/rgba*\((\d+), *(\d+), *(\d+)/i);
var ret=_6c3.splice(1,3);
return ret;
}else{
var _6c5=dojo.gfx.color.hex2rgb(_6c1);
if(_6c5){
return _6c5;
}else{
return dojo.gfx.color.named[_6c1]||[255,255,255];
}
}
};
dojo.gfx.color.hex2rgb=function(hex){
var _6c7="0123456789ABCDEF";
var rgb=new Array(3);
if(hex.indexOf("#")==0){
hex=hex.substring(1);
}
hex=hex.toUpperCase();
if(hex.replace(new RegExp("["+_6c7+"]","g"),"")!=""){
return null;
}
if(hex.length==3){
rgb[0]=hex.charAt(0)+hex.charAt(0);
rgb[1]=hex.charAt(1)+hex.charAt(1);
rgb[2]=hex.charAt(2)+hex.charAt(2);
}else{
rgb[0]=hex.substring(0,2);
rgb[1]=hex.substring(2,4);
rgb[2]=hex.substring(4);
}
for(var i=0;i<rgb.length;i++){
rgb[i]=_6c7.indexOf(rgb[i].charAt(0))*16+_6c7.indexOf(rgb[i].charAt(1));
}
return rgb;
};
dojo.gfx.color.rgb2hex=function(r,g,b){
if(dojo.lang.isArray(r)){
g=r[1]||0;
b=r[2]||0;
r=r[0]||0;
}
var ret=dojo.lang.map([r,g,b],function(x){
x=new Number(x);
var s=x.toString(16);
while(s.length<2){
s="0"+s;
}
return s;
});
ret.unshift("#");
return ret.join("");
};
dojo.provide("dojo.lfx.Animation");
dojo.lfx.Line=function(_6d0,end){
this.start=_6d0;
this.end=end;
if(dojo.lang.isArray(_6d0)){
var diff=[];
dojo.lang.forEach(this.start,function(s,i){
diff[i]=this.end[i]-s;
},this);
this.getValue=function(n){
var res=[];
dojo.lang.forEach(this.start,function(s,i){
res[i]=(diff[i]*n)+s;
},this);
return res;
};
}else{
var diff=end-_6d0;
this.getValue=function(n){
return (diff*n)+this.start;
};
}
};
if((dojo.render.html.khtml)&&(!dojo.render.html.safari)){
dojo.lfx.easeDefault=function(n){
return (parseFloat("0.5")+((Math.sin((n+parseFloat("1.5"))*Math.PI))/2));
};
}else{
dojo.lfx.easeDefault=function(n){
return (0.5+((Math.sin((n+1.5)*Math.PI))/2));
};
}
dojo.lfx.easeIn=function(n){
return Math.pow(n,3);
};
dojo.lfx.easeOut=function(n){
return (1-Math.pow(1-n,3));
};
dojo.lfx.easeInOut=function(n){
return ((3*Math.pow(n,2))-(2*Math.pow(n,3)));
};
dojo.lfx.IAnimation=function(){
};
dojo.lang.extend(dojo.lfx.IAnimation,{curve:null,duration:1000,easing:null,repeatCount:0,rate:10,handler:null,beforeBegin:null,onBegin:null,onAnimate:null,onEnd:null,onPlay:null,onPause:null,onStop:null,play:null,pause:null,stop:null,connect:function(evt,_6e0,_6e1){
if(!_6e1){
_6e1=_6e0;
_6e0=this;
}
_6e1=dojo.lang.hitch(_6e0,_6e1);
var _6e2=this[evt]||function(){
};
this[evt]=function(){
var ret=_6e2.apply(this,arguments);
_6e1.apply(this,arguments);
return ret;
};
return this;
},fire:function(evt,args){
if(this[evt]){
this[evt].apply(this,(args||[]));
}
return this;
},repeat:function(_6e6){
this.repeatCount=_6e6;
return this;
},_active:false,_paused:false});
dojo.lfx.Animation=function(_6e7,_6e8,_6e9,_6ea,_6eb,rate){
dojo.lfx.IAnimation.call(this);
if(dojo.lang.isNumber(_6e7)||(!_6e7&&_6e8.getValue)){
rate=_6eb;
_6eb=_6ea;
_6ea=_6e9;
_6e9=_6e8;
_6e8=_6e7;
_6e7=null;
}else{
if(_6e7.getValue||dojo.lang.isArray(_6e7)){
rate=_6ea;
_6eb=_6e9;
_6ea=_6e8;
_6e9=_6e7;
_6e8=null;
_6e7=null;
}
}
if(dojo.lang.isArray(_6e9)){
this.curve=new dojo.lfx.Line(_6e9[0],_6e9[1]);
}else{
this.curve=_6e9;
}
if(_6e8!=null&&_6e8>0){
this.duration=_6e8;
}
if(_6eb){
this.repeatCount=_6eb;
}
if(rate){
this.rate=rate;
}
if(_6e7){
dojo.lang.forEach(["handler","beforeBegin","onBegin","onEnd","onPlay","onStop","onAnimate"],function(item){
if(_6e7[item]){
this.connect(item,_6e7[item]);
}
},this);
}
if(_6ea&&dojo.lang.isFunction(_6ea)){
this.easing=_6ea;
}
};
dojo.inherits(dojo.lfx.Animation,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Animation,{_startTime:null,_endTime:null,_timer:null,_percent:0,_startRepeatCount:0,play:function(_6ee,_6ef){
if(_6ef){
clearTimeout(this._timer);
this._active=false;
this._paused=false;
this._percent=0;
}else{
if(this._active&&!this._paused){
return this;
}
}
this.fire("handler",["beforeBegin"]);
this.fire("beforeBegin");
if(_6ee>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_6ef);
}),_6ee);
return this;
}
this._startTime=new Date().valueOf();
if(this._paused){
this._startTime-=(this.duration*this._percent/100);
}
this._endTime=this._startTime+this.duration;
this._active=true;
this._paused=false;
var step=this._percent/100;
var _6f1=this.curve.getValue(step);
if(this._percent==0){
if(!this._startRepeatCount){
this._startRepeatCount=this.repeatCount;
}
this.fire("handler",["begin",_6f1]);
this.fire("onBegin",[_6f1]);
}
this.fire("handler",["play",_6f1]);
this.fire("onPlay",[_6f1]);
this._cycle();
return this;
},pause:function(){
clearTimeout(this._timer);
if(!this._active){
return this;
}
this._paused=true;
var _6f2=this.curve.getValue(this._percent/100);
this.fire("handler",["pause",_6f2]);
this.fire("onPause",[_6f2]);
return this;
},gotoPercent:function(pct,_6f4){
clearTimeout(this._timer);
this._active=true;
this._paused=true;
this._percent=pct;
if(_6f4){
this.play();
}
return this;
},stop:function(_6f5){
clearTimeout(this._timer);
var step=this._percent/100;
if(_6f5){
step=1;
}
var _6f7=this.curve.getValue(step);
this.fire("handler",["stop",_6f7]);
this.fire("onStop",[_6f7]);
this._active=false;
this._paused=false;
return this;
},status:function(){
if(this._active){
return this._paused?"paused":"playing";
}else{
return "stopped";
}
return this;
},_cycle:function(){
clearTimeout(this._timer);
if(this._active){
var curr=new Date().valueOf();
var step=(curr-this._startTime)/(this._endTime-this._startTime);
if(step>=1){
step=1;
this._percent=100;
}else{
this._percent=step*100;
}
if((this.easing)&&(dojo.lang.isFunction(this.easing))){
step=this.easing(step);
}
var _6fa=this.curve.getValue(step);
this.fire("handler",["animate",_6fa]);
this.fire("onAnimate",[_6fa]);
if(step<1){
this._timer=setTimeout(dojo.lang.hitch(this,"_cycle"),this.rate);
}else{
this._active=false;
this.fire("handler",["end"]);
this.fire("onEnd");
if(this.repeatCount>0){
this.repeatCount--;
this.play(null,true);
}else{
if(this.repeatCount==-1){
this.play(null,true);
}else{
if(this._startRepeatCount){
this.repeatCount=this._startRepeatCount;
this._startRepeatCount=0;
}
}
}
}
}
return this;
}});
dojo.lfx.Combine=function(_6fb){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._animsEnded=0;
var _6fc=arguments;
if(_6fc.length==1&&(dojo.lang.isArray(_6fc[0])||dojo.lang.isArrayLike(_6fc[0]))){
_6fc=_6fc[0];
}
dojo.lang.forEach(_6fc,function(anim){
this._anims.push(anim);
anim.connect("onEnd",dojo.lang.hitch(this,"_onAnimsEnded"));
},this);
};
dojo.inherits(dojo.lfx.Combine,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Combine,{_animsEnded:0,play:function(_6fe,_6ff){
if(!this._anims.length){
return this;
}
this.fire("beforeBegin");
if(_6fe>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_6ff);
}),_6fe);
return this;
}
if(_6ff||this._anims[0].percent==0){
this.fire("onBegin");
}
this.fire("onPlay");
this._animsCall("play",null,_6ff);
return this;
},pause:function(){
this.fire("onPause");
this._animsCall("pause");
return this;
},stop:function(_700){
this.fire("onStop");
this._animsCall("stop",_700);
return this;
},_onAnimsEnded:function(){
this._animsEnded++;
if(this._animsEnded>=this._anims.length){
this.fire("onEnd");
}
return this;
},_animsCall:function(_701){
var args=[];
if(arguments.length>1){
for(var i=1;i<arguments.length;i++){
args.push(arguments[i]);
}
}
var _704=this;
dojo.lang.forEach(this._anims,function(anim){
anim[_701](args);
},_704);
return this;
}});
dojo.lfx.Chain=function(_706){
dojo.lfx.IAnimation.call(this);
this._anims=[];
this._currAnim=-1;
var _707=arguments;
if(_707.length==1&&(dojo.lang.isArray(_707[0])||dojo.lang.isArrayLike(_707[0]))){
_707=_707[0];
}
var _708=this;
dojo.lang.forEach(_707,function(anim,i,_70b){
this._anims.push(anim);
if(i<_70b.length-1){
anim.connect("onEnd",dojo.lang.hitch(this,"_playNext"));
}else{
anim.connect("onEnd",dojo.lang.hitch(this,function(){
this.fire("onEnd");
}));
}
},this);
};
dojo.inherits(dojo.lfx.Chain,dojo.lfx.IAnimation);
dojo.lang.extend(dojo.lfx.Chain,{_currAnim:-1,play:function(_70c,_70d){
if(!this._anims.length){
return this;
}
if(_70d||!this._anims[this._currAnim]){
this._currAnim=0;
}
var _70e=this._anims[this._currAnim];
this.fire("beforeBegin");
if(_70c>0){
setTimeout(dojo.lang.hitch(this,function(){
this.play(null,_70d);
}),_70c);
return this;
}
if(_70e){
if(this._currAnim==0){
this.fire("handler",["begin",this._currAnim]);
this.fire("onBegin",[this._currAnim]);
}
this.fire("onPlay",[this._currAnim]);
_70e.play(null,_70d);
}
return this;
},pause:function(){
if(this._anims[this._currAnim]){
this._anims[this._currAnim].pause();
this.fire("onPause",[this._currAnim]);
}
return this;
},playPause:function(){
if(this._anims.length==0){
return this;
}
if(this._currAnim==-1){
this._currAnim=0;
}
var _70f=this._anims[this._currAnim];
if(_70f){
if(!_70f._active||_70f._paused){
this.play();
}else{
this.pause();
}
}
return this;
},stop:function(){
var _710=this._anims[this._currAnim];
if(_710){
_710.stop();
this.fire("onStop",[this._currAnim]);
}
return _710;
},_playNext:function(){
if(this._currAnim==-1||this._anims.length==0){
return this;
}
this._currAnim++;
if(this._anims[this._currAnim]){
this._anims[this._currAnim].play(null,true);
}
return this;
}});
dojo.lfx.combine=function(_711){
var _712=arguments;
if(dojo.lang.isArray(arguments[0])){
_712=arguments[0];
}
if(_712.length==1){
return _712[0];
}
return new dojo.lfx.Combine(_712);
};
dojo.lfx.chain=function(_713){
var _714=arguments;
if(dojo.lang.isArray(arguments[0])){
_714=arguments[0];
}
if(_714.length==1){
return _714[0];
}
return new dojo.lfx.Chain(_714);
};
dojo.provide("dojo.html.color");
dojo.html.getBackgroundColor=function(node){
node=dojo.byId(node);
var _716;
do{
_716=dojo.html.getStyle(node,"background-color");
if(_716.toLowerCase()=="rgba(0, 0, 0, 0)"){
_716="transparent";
}
if(node==document.getElementsByTagName("body")[0]){
node=null;
break;
}
node=node.parentNode;
}while(node&&dojo.lang.inArray(["transparent",""],_716));
if(_716=="transparent"){
_716=[255,255,255,0];
}else{
_716=dojo.gfx.color.extractRGB(_716);
}
return _716;
};
dojo.provide("dojo.lfx.html");
dojo.lfx.html._byId=function(_717){
if(!_717){
return [];
}
if(dojo.lang.isArrayLike(_717)){
if(!_717.alreadyChecked){
var n=[];
dojo.lang.forEach(_717,function(node){
n.push(dojo.byId(node));
});
n.alreadyChecked=true;
return n;
}else{
return _717;
}
}else{
var n=[];
n.push(dojo.byId(_717));
n.alreadyChecked=true;
return n;
}
};
dojo.lfx.html.propertyAnimation=function(_71a,_71b,_71c,_71d,_71e){
_71a=dojo.lfx.html._byId(_71a);
var _71f={"propertyMap":_71b,"nodes":_71a,"duration":_71c,"easing":_71d||dojo.lfx.easeDefault};
var _720=function(args){
if(args.nodes.length==1){
var pm=args.propertyMap;
if(!dojo.lang.isArray(args.propertyMap)){
var parr=[];
for(var _724 in pm){
pm[_724].property=_724;
parr.push(pm[_724]);
}
pm=args.propertyMap=parr;
}
dojo.lang.forEach(pm,function(prop){
if(dj_undef("start",prop)){
if(prop.property!="opacity"){
prop.start=parseInt(dojo.html.getComputedStyle(args.nodes[0],prop.property));
}else{
prop.start=dojo.html.getOpacity(args.nodes[0]);
}
}
});
}
};
var _726=function(_727){
var _728=[];
dojo.lang.forEach(_727,function(c){
_728.push(Math.round(c));
});
return _728;
};
var _72a=function(n,_72c){
n=dojo.byId(n);
if(!n||!n.style){
return;
}
for(var s in _72c){
try{
if(s=="opacity"){
dojo.html.setOpacity(n,_72c[s]);
}else{
n.style[s]=_72c[s];
}
}
catch(e){
dojo.debug(e);
}
}
};
var _72e=function(_72f){
this._properties=_72f;
this.diffs=new Array(_72f.length);
dojo.lang.forEach(_72f,function(prop,i){
if(dojo.lang.isFunction(prop.start)){
prop.start=prop.start(prop,i);
}
if(dojo.lang.isFunction(prop.end)){
prop.end=prop.end(prop,i);
}
if(dojo.lang.isArray(prop.start)){
this.diffs[i]=null;
}else{
if(prop.start instanceof dojo.gfx.color.Color){
prop.startRgb=prop.start.toRgb();
prop.endRgb=prop.end.toRgb();
}else{
this.diffs[i]=prop.end-prop.start;
}
}
},this);
this.getValue=function(n){
var ret={};
dojo.lang.forEach(this._properties,function(prop,i){
var _736=null;
if(dojo.lang.isArray(prop.start)){
}else{
if(prop.start instanceof dojo.gfx.color.Color){
_736=(prop.units||"rgb")+"(";
for(var j=0;j<prop.startRgb.length;j++){
_736+=Math.round(((prop.endRgb[j]-prop.startRgb[j])*n)+prop.startRgb[j])+(j<prop.startRgb.length-1?",":"");
}
_736+=")";
}else{
_736=((this.diffs[i])*n)+prop.start+(prop.property!="opacity"?prop.units||"px":"");
}
}
ret[dojo.html.toCamelCase(prop.property)]=_736;
},this);
return ret;
};
};
var anim=new dojo.lfx.Animation({beforeBegin:function(){
_720(_71f);
anim.curve=new _72e(_71f.propertyMap);
},onAnimate:function(_739){
dojo.lang.forEach(_71f.nodes,function(node){
_72a(node,_739);
});
}},_71f.duration,null,_71f.easing);
if(_71e){
for(var x in _71e){
if(dojo.lang.isFunction(_71e[x])){
anim.connect(x,anim,_71e[x]);
}
}
}
return anim;
};
dojo.lfx.html._makeFadeable=function(_73c){
var _73d=function(node){
if(dojo.render.html.ie){
if((node.style.zoom.length==0)&&(dojo.html.getStyle(node,"zoom")=="normal")){
node.style.zoom="1";
}
if((node.style.width.length==0)&&(dojo.html.getStyle(node,"width")=="auto")){
node.style.width="auto";
}
}
};
if(dojo.lang.isArrayLike(_73c)){
dojo.lang.forEach(_73c,_73d);
}else{
_73d(_73c);
}
};
dojo.lfx.html.fade=function(_73f,_740,_741,_742,_743){
_73f=dojo.lfx.html._byId(_73f);
var _744={property:"opacity"};
if(!dj_undef("start",_740)){
_744.start=_740.start;
}else{
_744.start=function(){
return dojo.html.getOpacity(_73f[0]);
};
}
if(!dj_undef("end",_740)){
_744.end=_740.end;
}else{
dojo.raise("dojo.lfx.html.fade needs an end value");
}
var anim=dojo.lfx.propertyAnimation(_73f,[_744],_741,_742);
anim.connect("beforeBegin",function(){
dojo.lfx.html._makeFadeable(_73f);
});
if(_743){
anim.connect("onEnd",function(){
_743(_73f,anim);
});
}
return anim;
};
dojo.lfx.html.fadeIn=function(_746,_747,_748,_749){
return dojo.lfx.html.fade(_746,{end:1},_747,_748,_749);
};
dojo.lfx.html.fadeOut=function(_74a,_74b,_74c,_74d){
return dojo.lfx.html.fade(_74a,{end:0},_74b,_74c,_74d);
};
dojo.lfx.html.fadeShow=function(_74e,_74f,_750,_751){
_74e=dojo.lfx.html._byId(_74e);
dojo.lang.forEach(_74e,function(node){
dojo.html.setOpacity(node,0);
});
var anim=dojo.lfx.html.fadeIn(_74e,_74f,_750,_751);
anim.connect("beforeBegin",function(){
if(dojo.lang.isArrayLike(_74e)){
dojo.lang.forEach(_74e,dojo.html.show);
}else{
dojo.html.show(_74e);
}
});
return anim;
};
dojo.lfx.html.fadeHide=function(_754,_755,_756,_757){
var anim=dojo.lfx.html.fadeOut(_754,_755,_756,function(){
if(dojo.lang.isArrayLike(_754)){
dojo.lang.forEach(_754,dojo.html.hide);
}else{
dojo.html.hide(_754);
}
if(_757){
_757(_754,anim);
}
});
return anim;
};
dojo.lfx.html.wipeIn=function(_759,_75a,_75b,_75c){
_759=dojo.lfx.html._byId(_759);
var _75d=[];
dojo.lang.forEach(_759,function(node){
var _75f={};
var _760,_761,_762;
with(node.style){
_760=top;
_761=left;
_762=position;
top="-9999px";
left="-9999px";
position="absolute";
display="";
}
var _763=dojo.html.getBorderBox(node).height;
with(node.style){
top=_760;
left=_761;
position=_762;
display="none";
}
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:1,end:function(){
return _763;
}}},_75a,_75b);
anim.connect("beforeBegin",function(){
_75f.overflow=node.style.overflow;
_75f.height=node.style.height;
with(node.style){
overflow="hidden";
height="1px";
}
dojo.html.show(node);
});
anim.connect("onEnd",function(){
with(node.style){
overflow=_75f.overflow;
height=_75f.height;
}
if(_75c){
_75c(node,anim);
}
});
_75d.push(anim);
});
return dojo.lfx.combine(_75d);
};
dojo.lfx.html.wipeOut=function(_765,_766,_767,_768){
_765=dojo.lfx.html._byId(_765);
var _769=[];
dojo.lang.forEach(_765,function(node){
var _76b={};
var anim=dojo.lfx.propertyAnimation(node,{"height":{start:function(){
return dojo.html.getContentBox(node).height;
},end:1}},_766,_767,{"beforeBegin":function(){
_76b.overflow=node.style.overflow;
_76b.height=node.style.height;
with(node.style){
overflow="hidden";
}
dojo.html.show(node);
},"onEnd":function(){
dojo.html.hide(node);
with(node.style){
overflow=_76b.overflow;
height=_76b.height;
}
if(_768){
_768(node,anim);
}
}});
_769.push(anim);
});
return dojo.lfx.combine(_769);
};
dojo.lfx.html.slideTo=function(_76d,_76e,_76f,_770,_771){
_76d=dojo.lfx.html._byId(_76d);
var _772=[];
var _773=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_76e)){
dojo.deprecated("dojo.lfx.html.slideTo(node, array)","use dojo.lfx.html.slideTo(node, {top: value, left: value});","0.5");
_76e={top:_76e[0],left:_76e[1]};
}
dojo.lang.forEach(_76d,function(node){
var top=null;
var left=null;
var init=(function(){
var _778=node;
return function(){
var pos=_773(_778,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_773(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_773(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_778,true);
dojo.html.setStyleAttributes(_778,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:(_76e.top||0)},"left":{start:left,end:(_76e.left||0)}},_76f,_770,{"beforeBegin":init});
if(_771){
anim.connect("onEnd",function(){
_771(_76d,anim);
});
}
_772.push(anim);
});
return dojo.lfx.combine(_772);
};
dojo.lfx.html.slideBy=function(_77c,_77d,_77e,_77f,_780){
_77c=dojo.lfx.html._byId(_77c);
var _781=[];
var _782=dojo.html.getComputedStyle;
if(dojo.lang.isArray(_77d)){
dojo.deprecated("dojo.lfx.html.slideBy(node, array)","use dojo.lfx.html.slideBy(node, {top: value, left: value});","0.5");
_77d={top:_77d[0],left:_77d[1]};
}
dojo.lang.forEach(_77c,function(node){
var top=null;
var left=null;
var init=(function(){
var _787=node;
return function(){
var pos=_782(_787,"position");
top=(pos=="absolute"?node.offsetTop:parseInt(_782(node,"top"))||0);
left=(pos=="absolute"?node.offsetLeft:parseInt(_782(node,"left"))||0);
if(!dojo.lang.inArray(["absolute","relative"],pos)){
var ret=dojo.html.abs(_787,true);
dojo.html.setStyleAttributes(_787,"position:absolute;top:"+ret.y+"px;left:"+ret.x+"px;");
top=ret.y;
left=ret.x;
}
};
})();
init();
var anim=dojo.lfx.propertyAnimation(node,{"top":{start:top,end:top+(_77d.top||0)},"left":{start:left,end:left+(_77d.left||0)}},_77e,_77f).connect("beforeBegin",init);
if(_780){
anim.connect("onEnd",function(){
_780(_77c,anim);
});
}
_781.push(anim);
});
return dojo.lfx.combine(_781);
};
dojo.lfx.html.explode=function(_78b,_78c,_78d,_78e,_78f){
var h=dojo.html;
_78b=dojo.byId(_78b);
_78c=dojo.byId(_78c);
var _791=h.toCoordinateObject(_78b,true);
var _792=document.createElement("div");
h.copyStyle(_792,_78c);
if(_78c.explodeClassName){
_792.className=_78c.explodeClassName;
}
with(_792.style){
position="absolute";
display="none";
var _793=h.getStyle(_78b,"background-color");
backgroundColor=_793?_793.toLowerCase():"transparent";
backgroundColor=(backgroundColor=="transparent")?"rgb(221, 221, 221)":backgroundColor;
}
dojo.body().appendChild(_792);
with(_78c.style){
visibility="hidden";
display="block";
}
var _794=h.toCoordinateObject(_78c,true);
with(_78c.style){
display="none";
visibility="visible";
}
var _795={opacity:{start:0.5,end:1}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_795[type]={start:_791[type],end:_794[type]};
});
var anim=new dojo.lfx.propertyAnimation(_792,_795,_78d,_78e,{"beforeBegin":function(){
h.setDisplay(_792,"block");
},"onEnd":function(){
h.setDisplay(_78c,"block");
_792.parentNode.removeChild(_792);
}});
if(_78f){
anim.connect("onEnd",function(){
_78f(_78c,anim);
});
}
return anim;
};
dojo.lfx.html.implode=function(_798,end,_79a,_79b,_79c){
var h=dojo.html;
_798=dojo.byId(_798);
end=dojo.byId(end);
var _79e=dojo.html.toCoordinateObject(_798,true);
var _79f=dojo.html.toCoordinateObject(end,true);
var _7a0=document.createElement("div");
dojo.html.copyStyle(_7a0,_798);
if(_798.explodeClassName){
_7a0.className=_798.explodeClassName;
}
dojo.html.setOpacity(_7a0,0.3);
with(_7a0.style){
position="absolute";
display="none";
backgroundColor=h.getStyle(_798,"background-color").toLowerCase();
}
dojo.body().appendChild(_7a0);
var _7a1={opacity:{start:1,end:0.5}};
dojo.lang.forEach(["height","width","top","left"],function(type){
_7a1[type]={start:_79e[type],end:_79f[type]};
});
var anim=new dojo.lfx.propertyAnimation(_7a0,_7a1,_79a,_79b,{"beforeBegin":function(){
dojo.html.hide(_798);
dojo.html.show(_7a0);
},"onEnd":function(){
_7a0.parentNode.removeChild(_7a0);
}});
if(_79c){
anim.connect("onEnd",function(){
_79c(_798,anim);
});
}
return anim;
};
dojo.lfx.html.highlight=function(_7a4,_7a5,_7a6,_7a7,_7a8){
_7a4=dojo.lfx.html._byId(_7a4);
var _7a9=[];
dojo.lang.forEach(_7a4,function(node){
var _7ab=dojo.html.getBackgroundColor(node);
var bg=dojo.html.getStyle(node,"background-color").toLowerCase();
var _7ad=dojo.html.getStyle(node,"background-image");
var _7ae=(bg=="transparent"||bg=="rgba(0, 0, 0, 0)");
while(_7ab.length>3){
_7ab.pop();
}
var rgb=new dojo.gfx.color.Color(_7a5);
var _7b0=new dojo.gfx.color.Color(_7ab);
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:rgb,end:_7b0}},_7a6,_7a7,{"beforeBegin":function(){
if(_7ad){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+rgb.toRgb().join(",")+")";
},"onEnd":function(){
if(_7ad){
node.style.backgroundImage=_7ad;
}
if(_7ae){
node.style.backgroundColor="transparent";
}
if(_7a8){
_7a8(node,anim);
}
}});
_7a9.push(anim);
});
return dojo.lfx.combine(_7a9);
};
dojo.lfx.html.unhighlight=function(_7b2,_7b3,_7b4,_7b5,_7b6){
_7b2=dojo.lfx.html._byId(_7b2);
var _7b7=[];
dojo.lang.forEach(_7b2,function(node){
var _7b9=new dojo.gfx.color.Color(dojo.html.getBackgroundColor(node));
var rgb=new dojo.gfx.color.Color(_7b3);
var _7bb=dojo.html.getStyle(node,"background-image");
var anim=dojo.lfx.propertyAnimation(node,{"background-color":{start:_7b9,end:rgb}},_7b4,_7b5,{"beforeBegin":function(){
if(_7bb){
node.style.backgroundImage="none";
}
node.style.backgroundColor="rgb("+_7b9.toRgb().join(",")+")";
},"onEnd":function(){
if(_7b6){
_7b6(node,anim);
}
}});
_7b7.push(anim);
});
return dojo.lfx.combine(_7b7);
};
dojo.lang.mixin(dojo.lfx,dojo.lfx.html);
dojo.kwCompoundRequire({browser:["dojo.lfx.html"],dashboard:["dojo.lfx.html"]});
dojo.provide("dojo.lfx.*");
dojo.provide("dojo.lfx.toggle");
dojo.lfx.toggle.plain={show:function(node,_7be,_7bf,_7c0){
dojo.html.show(node);
if(dojo.lang.isFunction(_7c0)){
_7c0();
}
},hide:function(node,_7c2,_7c3,_7c4){
dojo.html.hide(node);
if(dojo.lang.isFunction(_7c4)){
_7c4();
}
}};
dojo.lfx.toggle.fade={show:function(node,_7c6,_7c7,_7c8){
dojo.lfx.fadeShow(node,_7c6,_7c7,_7c8).play();
},hide:function(node,_7ca,_7cb,_7cc){
dojo.lfx.fadeHide(node,_7ca,_7cb,_7cc).play();
}};
dojo.lfx.toggle.wipe={show:function(node,_7ce,_7cf,_7d0){
dojo.lfx.wipeIn(node,_7ce,_7cf,_7d0).play();
},hide:function(node,_7d2,_7d3,_7d4){
dojo.lfx.wipeOut(node,_7d2,_7d3,_7d4).play();
}};
dojo.lfx.toggle.explode={show:function(node,_7d6,_7d7,_7d8,_7d9){
dojo.lfx.explode(_7d9||{x:0,y:0,width:0,height:0},node,_7d6,_7d7,_7d8).play();
},hide:function(node,_7db,_7dc,_7dd,_7de){
dojo.lfx.implode(node,_7de||{x:0,y:0,width:0,height:0},_7db,_7dc,_7dd).play();
}};
dojo.provide("dojo.widget.HtmlWidget");
dojo.declare("dojo.widget.HtmlWidget",dojo.widget.DomWidget,{templateCssPath:null,templatePath:null,lang:"",toggle:"plain",toggleDuration:150,initialize:function(args,frag){
},postMixInProperties:function(args,frag){
if(this.lang===""){
this.lang=null;
}
this.toggleObj=dojo.lfx.toggle[this.toggle.toLowerCase()]||dojo.lfx.toggle.plain;
},createNodesFromText:function(txt,wrap){
return dojo.html.createNodesFromText(txt,wrap);
},destroyRendering:function(_7e5){
try{
if(this.bgIframe){
this.bgIframe.remove();
delete this.bgIframe;
}
if(!_7e5&&this.domNode){
dojo.event.browser.clean(this.domNode);
}
dojo.widget.HtmlWidget.superclass.destroyRendering.call(this);
}
catch(e){
}
},isShowing:function(){
return dojo.html.isShowing(this.domNode);
},toggleShowing:function(){
if(this.isShowing()){
this.hide();
}else{
this.show();
}
},show:function(){
if(this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.show(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onShow),this.explodeSrc);
},onShow:function(){
this.animationInProgress=false;
this.checkSize();
},hide:function(){
if(!this.isShowing()){
return;
}
this.animationInProgress=true;
this.toggleObj.hide(this.domNode,this.toggleDuration,null,dojo.lang.hitch(this,this.onHide),this.explodeSrc);
},onHide:function(){
this.animationInProgress=false;
},_isResized:function(w,h){
if(!this.isShowing()){
return false;
}
var wh=dojo.html.getMarginBox(this.domNode);
var _7e9=w||wh.width;
var _7ea=h||wh.height;
if(this.width==_7e9&&this.height==_7ea){
return false;
}
this.width=_7e9;
this.height=_7ea;
return true;
},checkSize:function(){
if(!this._isResized()){
return;
}
this.onResized();
},resizeTo:function(w,h){
dojo.html.setMarginBox(this.domNode,{width:w,height:h});
if(this.isShowing()){
this.onResized();
}
},resizeSoon:function(){
if(this.isShowing()){
dojo.lang.setTimeout(this,this.onResized,0);
}
},onResized:function(){
dojo.lang.forEach(this.children,function(_7ed){
if(_7ed.checkSize){
_7ed.checkSize();
}
});
}});
dojo.kwCompoundRequire({common:["dojo.xml.Parse","dojo.widget.Widget","dojo.widget.Parse","dojo.widget.Manager"],browser:["dojo.widget.DomWidget","dojo.widget.HtmlWidget"],dashboard:["dojo.widget.DomWidget","dojo.widget.HtmlWidget"],svg:["dojo.widget.SvgWidget"],rhino:["dojo.widget.SwtWidget"]});
dojo.provide("dojo.widget.*");
dojo.provide("dojo.string");
dojo.provide("dojo.io.common");
dojo.io.transports=[];
dojo.io.hdlrFuncNames=["load","error","timeout"];
dojo.io.Request=function(url,_7ef,_7f0,_7f1){
if((arguments.length==1)&&(arguments[0].constructor==Object)){
this.fromKwArgs(arguments[0]);
}else{
this.url=url;
if(_7ef){
this.mimetype=_7ef;
}
if(_7f0){
this.transport=_7f0;
}
if(arguments.length>=4){
this.changeUrl=_7f1;
}
}
};
dojo.lang.extend(dojo.io.Request,{url:"",mimetype:"text/plain",method:"GET",content:undefined,transport:undefined,changeUrl:undefined,formNode:undefined,sync:false,bindSuccess:false,useCache:false,preventCache:false,jsonFilter:function(_7f2){
if((this.mimetype=="text/json-comment-filtered")||(this.mimetype=="application/json-comment-filtered")){
var _7f3=_7f2.indexOf("/*");
var _7f4=_7f2.lastIndexOf("*/");
if((_7f3==-1)||(_7f4==-1)){
dojo.debug("your JSON wasn't comment filtered!");
return "";
}
return _7f2.substring(_7f3+2,_7f4);
}
dojo.debug("please consider using a mimetype of text/json-comment-filtered to avoid potential security issues with JSON endpoints");
return _7f2;
},load:function(type,data,_7f7,_7f8){
},error:function(type,_7fa,_7fb,_7fc){
},timeout:function(type,_7fe,_7ff,_800){
},handle:function(type,data,_803,_804){
},timeoutSeconds:0,abort:function(){
},fromKwArgs:function(_805){
if(_805["url"]){
_805.url=_805.url.toString();
}
if(_805["formNode"]){
_805.formNode=dojo.byId(_805.formNode);
}
if(!_805["method"]&&_805["formNode"]&&_805["formNode"].method){
_805.method=_805["formNode"].method;
}
if(!_805["handle"]&&_805["handler"]){
_805.handle=_805.handler;
}
if(!_805["load"]&&_805["loaded"]){
_805.load=_805.loaded;
}
if(!_805["changeUrl"]&&_805["changeURL"]){
_805.changeUrl=_805.changeURL;
}
_805.encoding=dojo.lang.firstValued(_805["encoding"],djConfig["bindEncoding"],"");
_805.sendTransport=dojo.lang.firstValued(_805["sendTransport"],djConfig["ioSendTransport"],false);
var _806=dojo.lang.isFunction;
for(var x=0;x<dojo.io.hdlrFuncNames.length;x++){
var fn=dojo.io.hdlrFuncNames[x];
if(_805[fn]&&_806(_805[fn])){
continue;
}
if(_805["handle"]&&_806(_805["handle"])){
_805[fn]=_805.handle;
}
}
dojo.lang.mixin(this,_805);
}});
dojo.io.Error=function(msg,type,num){
this.message=msg;
this.type=type||"unknown";
this.number=num||0;
};
dojo.io.transports.addTransport=function(name){
this.push(name);
this[name]=dojo.io[name];
};
dojo.io.bind=function(_80d){
if(!(_80d instanceof dojo.io.Request)){
try{
_80d=new dojo.io.Request(_80d);
}
catch(e){
dojo.debug(e);
}
}
var _80e="";
if(_80d["transport"]){
_80e=_80d["transport"];
if(!this[_80e]){
dojo.io.sendBindError(_80d,"No dojo.io.bind() transport with name '"+_80d["transport"]+"'.");
return _80d;
}
if(!this[_80e].canHandle(_80d)){
dojo.io.sendBindError(_80d,"dojo.io.bind() transport with name '"+_80d["transport"]+"' cannot handle this type of request.");
return _80d;
}
}else{
for(var x=0;x<dojo.io.transports.length;x++){
var tmp=dojo.io.transports[x];
if((this[tmp])&&(this[tmp].canHandle(_80d))){
_80e=tmp;
break;
}
}
if(_80e==""){
dojo.io.sendBindError(_80d,"None of the loaded transports for dojo.io.bind()"+" can handle the request.");
return _80d;
}
}
this[_80e].bind(_80d);
_80d.bindSuccess=true;
return _80d;
};
dojo.io.sendBindError=function(_811,_812){
if((typeof _811.error=="function"||typeof _811.handle=="function")&&(typeof setTimeout=="function"||typeof setTimeout=="object")){
var _813=new dojo.io.Error(_812);
setTimeout(function(){
_811[(typeof _811.error=="function")?"error":"handle"]("error",_813,null,_811);
},50);
}else{
dojo.raise(_812);
}
};
dojo.io.queueBind=function(_814){
if(!(_814 instanceof dojo.io.Request)){
try{
_814=new dojo.io.Request(_814);
}
catch(e){
dojo.debug(e);
}
}
var _815=_814.load;
_814.load=function(){
dojo.io._queueBindInFlight=false;
var ret=_815.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
var _817=_814.error;
_814.error=function(){
dojo.io._queueBindInFlight=false;
var ret=_817.apply(this,arguments);
dojo.io._dispatchNextQueueBind();
return ret;
};
dojo.io._bindQueue.push(_814);
dojo.io._dispatchNextQueueBind();
return _814;
};
dojo.io._dispatchNextQueueBind=function(){
if(!dojo.io._queueBindInFlight){
dojo.io._queueBindInFlight=true;
if(dojo.io._bindQueue.length>0){
dojo.io.bind(dojo.io._bindQueue.shift());
}else{
dojo.io._queueBindInFlight=false;
}
}
};
dojo.io._bindQueue=[];
dojo.io._queueBindInFlight=false;
dojo.io.argsFromMap=function(map,_81a,last){
var enc=/utf/i.test(_81a||"")?encodeURIComponent:dojo.string.encodeAscii;
var _81d=[];
var _81e=new Object();
for(var name in map){
var _820=function(elt){
var val=enc(name)+"="+enc(elt);
_81d[(last==name)?"push":"unshift"](val);
};
if(!_81e[name]){
var _823=map[name];
if(dojo.lang.isArray(_823)){
dojo.lang.forEach(_823,_820);
}else{
_820(_823);
}
}
}
return _81d.join("&");
};
dojo.io.setIFrameSrc=function(_824,src,_826){
try{
var r=dojo.render.html;
if(!_826){
if(r.safari){
_824.location=src;
}else{
frames[_824.name].location=src;
}
}else{
var idoc;
if(r.ie){
idoc=_824.contentWindow.document;
}else{
if(r.safari){
idoc=_824.document;
}else{
idoc=_824.contentWindow;
}
}
if(!idoc){
_824.location=src;
return;
}else{
idoc.location.replace(src);
}
}
}
catch(e){
dojo.debug(e);
dojo.debug("setIFrameSrc: "+e);
}
};
dojo.provide("dojo.undo.browser");
try{
if((!djConfig["preventBackButtonFix"])&&(!dojo.hostenv.post_load_)){
document.write("<iframe style='border: 0px; width: 1px; height: 1px; position: absolute; bottom: 0px; right: 0px; visibility: visible;' name='djhistory' id='djhistory' src='"+(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"'></iframe>");
}
}
catch(e){
}
if(dojo.render.html.opera){
dojo.debug("Opera is not supported with dojo.undo.browser, so back/forward detection will not work.");
}
dojo.undo.browser={initialHref:(!dj_undef("window"))?window.location.href:"",initialHash:(!dj_undef("window"))?window.location.hash:"",moveForward:false,historyStack:[],forwardStack:[],historyIframe:null,bookmarkAnchor:null,locationTimer:null,setInitialState:function(args){
this.initialState=this._createState(this.initialHref,args,this.initialHash);
},addToHistory:function(args){
this.forwardStack=[];
var hash=null;
var url=null;
if(!this.historyIframe){
if(djConfig["useXDomain"]&&!djConfig["dojoIframeHistoryUrl"]){
dojo.debug("dojo.undo.browser: When using cross-domain Dojo builds,"+" please save iframe_history.html to your domain and set djConfig.dojoIframeHistoryUrl"+" to the path on your domain to iframe_history.html");
}
this.historyIframe=window.frames["djhistory"];
}
if(!this.bookmarkAnchor){
this.bookmarkAnchor=document.createElement("a");
dojo.body().appendChild(this.bookmarkAnchor);
this.bookmarkAnchor.style.display="none";
}
if(args["changeUrl"]){
hash="#"+((args["changeUrl"]!==true)?args["changeUrl"]:(new Date()).getTime());
if(this.historyStack.length==0&&this.initialState.urlHash==hash){
this.initialState=this._createState(url,args,hash);
return;
}else{
if(this.historyStack.length>0&&this.historyStack[this.historyStack.length-1].urlHash==hash){
this.historyStack[this.historyStack.length-1]=this._createState(url,args,hash);
return;
}
}
this.changingUrl=true;
setTimeout("window.location.href = '"+hash+"'; dojo.undo.browser.changingUrl = false;",1);
this.bookmarkAnchor.href=hash;
if(dojo.render.html.ie){
url=this._loadIframeHistory();
var _82d=args["back"]||args["backButton"]||args["handle"];
var tcb=function(_82f){
if(window.location.hash!=""){
setTimeout("window.location.href = '"+hash+"';",1);
}
_82d.apply(this,[_82f]);
};
if(args["back"]){
args.back=tcb;
}else{
if(args["backButton"]){
args.backButton=tcb;
}else{
if(args["handle"]){
args.handle=tcb;
}
}
}
var _830=args["forward"]||args["forwardButton"]||args["handle"];
var tfw=function(_832){
if(window.location.hash!=""){
window.location.href=hash;
}
if(_830){
_830.apply(this,[_832]);
}
};
if(args["forward"]){
args.forward=tfw;
}else{
if(args["forwardButton"]){
args.forwardButton=tfw;
}else{
if(args["handle"]){
args.handle=tfw;
}
}
}
}else{
if(dojo.render.html.moz){
if(!this.locationTimer){
this.locationTimer=setInterval("dojo.undo.browser.checkLocation();",200);
}
}
}
}else{
url=this._loadIframeHistory();
}
this.historyStack.push(this._createState(url,args,hash));
},checkLocation:function(){
if(!this.changingUrl){
var hsl=this.historyStack.length;
if((window.location.hash==this.initialHash||window.location.href==this.initialHref)&&(hsl==1)){
this.handleBackButton();
return;
}
if(this.forwardStack.length>0){
if(this.forwardStack[this.forwardStack.length-1].urlHash==window.location.hash){
this.handleForwardButton();
return;
}
}
if((hsl>=2)&&(this.historyStack[hsl-2])){
if(this.historyStack[hsl-2].urlHash==window.location.hash){
this.handleBackButton();
return;
}
}
}
},iframeLoaded:function(evt,_835){
if(!dojo.render.html.opera){
var _836=this._getUrlQuery(_835.href);
if(_836==null){
if(this.historyStack.length==1){
this.handleBackButton();
}
return;
}
if(this.moveForward){
this.moveForward=false;
return;
}
if(this.historyStack.length>=2&&_836==this._getUrlQuery(this.historyStack[this.historyStack.length-2].url)){
this.handleBackButton();
}else{
if(this.forwardStack.length>0&&_836==this._getUrlQuery(this.forwardStack[this.forwardStack.length-1].url)){
this.handleForwardButton();
}
}
}
},handleBackButton:function(){
var _837=this.historyStack.pop();
if(!_837){
return;
}
var last=this.historyStack[this.historyStack.length-1];
if(!last&&this.historyStack.length==0){
last=this.initialState;
}
if(last){
if(last.kwArgs["back"]){
last.kwArgs["back"]();
}else{
if(last.kwArgs["backButton"]){
last.kwArgs["backButton"]();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("back");
}
}
}
}
this.forwardStack.push(_837);
},handleForwardButton:function(){
var last=this.forwardStack.pop();
if(!last){
return;
}
if(last.kwArgs["forward"]){
last.kwArgs.forward();
}else{
if(last.kwArgs["forwardButton"]){
last.kwArgs.forwardButton();
}else{
if(last.kwArgs["handle"]){
last.kwArgs.handle("forward");
}
}
}
this.historyStack.push(last);
},_createState:function(url,args,hash){
return {"url":url,"kwArgs":args,"urlHash":hash};
},_getUrlQuery:function(url){
var _83e=url.split("?");
if(_83e.length<2){
return null;
}else{
return _83e[1];
}
},_loadIframeHistory:function(){
var url=(djConfig["dojoIframeHistoryUrl"]||dojo.hostenv.getBaseScriptUri()+"iframe_history.html")+"?"+(new Date()).getTime();
this.moveForward=true;
dojo.io.setIFrameSrc(this.historyIframe,url,false);
return url;
}};
dojo.provide("dojo.io.BrowserIO");
if(!dj_undef("window")){
dojo.io.checkChildrenForFile=function(node){
var _841=false;
var _842=node.getElementsByTagName("input");
dojo.lang.forEach(_842,function(_843){
if(_841){
return;
}
if(_843.getAttribute("type")=="file"){
_841=true;
}
});
return _841;
};
dojo.io.formHasFile=function(_844){
return dojo.io.checkChildrenForFile(_844);
};
dojo.io.updateNode=function(node,_846){
node=dojo.byId(node);
var args=_846;
if(dojo.lang.isString(_846)){
args={url:_846};
}
args.mimetype="text/html";
args.load=function(t,d,e){
while(node.firstChild){
dojo.dom.destroyNode(node.firstChild);
}
node.innerHTML=d;
};
dojo.io.bind(args);
};
dojo.io.formFilter=function(node){
var type=(node.type||"").toLowerCase();
return !node.disabled&&node.name&&!dojo.lang.inArray(["file","submit","image","reset","button"],type);
};
dojo.io.encodeForm=function(_84d,_84e,_84f){
if((!_84d)||(!_84d.tagName)||(!_84d.tagName.toLowerCase()=="form")){
dojo.raise("Attempted to encode a non-form element.");
}
if(!_84f){
_84f=dojo.io.formFilter;
}
var enc=/utf/i.test(_84e||"")?encodeURIComponent:dojo.string.encodeAscii;
var _851=[];
for(var i=0;i<_84d.elements.length;i++){
var elm=_84d.elements[i];
if(!elm||elm.tagName.toLowerCase()=="fieldset"||!_84f(elm)){
continue;
}
var name=enc(elm.name);
var type=elm.type.toLowerCase();
if(type=="select-multiple"){
for(var j=0;j<elm.options.length;j++){
if(elm.options[j].selected){
_851.push(name+"="+enc(elm.options[j].value));
}
}
}else{
if(dojo.lang.inArray(["radio","checkbox"],type)){
if(elm.checked){
_851.push(name+"="+enc(elm.value));
}
}else{
_851.push(name+"="+enc(elm.value));
}
}
}
var _857=_84d.getElementsByTagName("input");
for(var i=0;i<_857.length;i++){
var _858=_857[i];
if(_858.type.toLowerCase()=="image"&&_858.form==_84d&&_84f(_858)){
var name=enc(_858.name);
_851.push(name+"="+enc(_858.value));
_851.push(name+".x=0");
_851.push(name+".y=0");
}
}
return _851.join("&")+"&";
};
dojo.io.FormBind=function(args){
this.bindArgs={};
if(args&&args.formNode){
this.init(args);
}else{
if(args){
this.init({formNode:args});
}
}
};
dojo.lang.extend(dojo.io.FormBind,{form:null,bindArgs:null,clickedButton:null,init:function(args){
var form=dojo.byId(args.formNode);
if(!form||!form.tagName||form.tagName.toLowerCase()!="form"){
throw new Error("FormBind: Couldn't apply, invalid form");
}else{
if(this.form==form){
return;
}else{
if(this.form){
throw new Error("FormBind: Already applied to a form");
}
}
}
dojo.lang.mixin(this.bindArgs,args);
this.form=form;
this.connect(form,"onsubmit","submit");
for(var i=0;i<form.elements.length;i++){
var node=form.elements[i];
if(node&&node.type&&dojo.lang.inArray(["submit","button"],node.type.toLowerCase())){
this.connect(node,"onclick","click");
}
}
var _85e=form.getElementsByTagName("input");
for(var i=0;i<_85e.length;i++){
var _85f=_85e[i];
if(_85f.type.toLowerCase()=="image"&&_85f.form==form){
this.connect(_85f,"onclick","click");
}
}
},onSubmit:function(form){
return true;
},submit:function(e){
if(e){
e.preventDefault();
}
if(this.onSubmit(this.form)){
dojo.io.bind(dojo.lang.mixin(this.bindArgs,{formFilter:dojo.lang.hitch(this,"formFilter")}));
}
},click:function(e){
var node=e.currentTarget;
if(node.disabled){
return;
}
this.clickedButton=node;
},formFilter:function(node){
var type=(node.type||"").toLowerCase();
var _866=false;
if(node.disabled||!node.name){
_866=false;
}else{
if(dojo.lang.inArray(["submit","button","image"],type)){
if(!this.clickedButton){
this.clickedButton=node;
}
_866=node==this.clickedButton;
}else{
_866=!dojo.lang.inArray(["file","submit","reset","button"],type);
}
}
return _866;
},connect:function(_867,_868,_869){
if(dojo.evalObjPath("dojo.event.connect")){
dojo.event.connect(_867,_868,this,_869);
}else{
var fcn=dojo.lang.hitch(this,_869);
_867[_868]=function(e){
if(!e){
e=window.event;
}
if(!e.currentTarget){
e.currentTarget=e.srcElement;
}
if(!e.preventDefault){
e.preventDefault=function(){
window.event.returnValue=false;
};
}
fcn(e);
};
}
}});
dojo.io.XMLHTTPTransport=new function(){
var _86c=this;
var _86d={};
this.useCache=false;
this.preventCache=false;
function getCacheKey(url,_86f,_870){
return url+"|"+_86f+"|"+_870.toLowerCase();
}
function addToCache(url,_872,_873,http){
_86d[getCacheKey(url,_872,_873)]=http;
}
function getFromCache(url,_876,_877){
return _86d[getCacheKey(url,_876,_877)];
}
this.clearCache=function(){
_86d={};
};
function doLoad(_878,http,url,_87b,_87c){
if(((http.status>=200)&&(http.status<300))||(http.status==304)||(http.status==1223)||(location.protocol=="file:"&&(http.status==0||http.status==undefined))||(location.protocol=="chrome:"&&(http.status==0||http.status==undefined))){
var ret;
if(_878.method.toLowerCase()=="head"){
var _87e=http.getAllResponseHeaders();
ret={};
ret.toString=function(){
return _87e;
};
var _87f=_87e.split(/[\r\n]+/g);
for(var i=0;i<_87f.length;i++){
var pair=_87f[i].match(/^([^:]+)\s*:\s*(.+)$/i);
if(pair){
ret[pair[1]]=pair[2];
}
}
}else{
if(_878.mimetype=="text/javascript"){
try{
ret=dj_eval(http.responseText);
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=null;
}
}else{
if(_878.mimetype.substr(0,9)=="text/json"||_878.mimetype.substr(0,16)=="application/json"){
try{
ret=dj_eval("("+_878.jsonFilter(http.responseText)+")");
}
catch(e){
dojo.debug(e);
dojo.debug(http.responseText);
ret=false;
}
}else{
if((_878.mimetype=="application/xml")||(_878.mimetype=="text/xml")){
ret=http.responseXML;
if(!ret||typeof ret=="string"||!http.getResponseHeader("Content-Type")){
ret=dojo.dom.createDocumentFromText(http.responseText);
}
}else{
ret=http.responseText;
}
}
}
}
if(_87c){
addToCache(url,_87b,_878.method,http);
}
_878[(typeof _878.load=="function")?"load":"handle"]("load",ret,http,_878);
}else{
var _882=new dojo.io.Error("XMLHttpTransport Error: "+http.status+" "+http.statusText);
_878[(typeof _878.error=="function")?"error":"handle"]("error",_882,http,_878);
}
}
function setHeaders(http,_884){
if(_884["headers"]){
for(var _885 in _884["headers"]){
if(_885.toLowerCase()=="content-type"&&!_884["contentType"]){
_884["contentType"]=_884["headers"][_885];
}else{
http.setRequestHeader(_885,_884["headers"][_885]);
}
}
}
}
this.inFlight=[];
this.inFlightTimer=null;
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
}
};
this.watchInFlight=function(){
var now=null;
if(!dojo.hostenv._blockAsync&&!_86c._blockAsync){
for(var x=this.inFlight.length-1;x>=0;x--){
try{
var tif=this.inFlight[x];
if(!tif||tif.http._aborted||!tif.http.readyState){
this.inFlight.splice(x,1);
continue;
}
if(4==tif.http.readyState){
this.inFlight.splice(x,1);
doLoad(tif.req,tif.http,tif.url,tif.query,tif.useCache);
}else{
if(tif.startTime){
if(!now){
now=(new Date()).getTime();
}
if(tif.startTime+(tif.req.timeoutSeconds*1000)<now){
if(typeof tif.http.abort=="function"){
tif.http.abort();
}
this.inFlight.splice(x,1);
tif.req[(typeof tif.req.timeout=="function")?"timeout":"handle"]("timeout",null,tif.http,tif.req);
}
}
}
}
catch(e){
try{
var _889=new dojo.io.Error("XMLHttpTransport.watchInFlight Error: "+e);
tif.req[(typeof tif.req.error=="function")?"error":"handle"]("error",_889,tif.http,tif.req);
}
catch(e2){
dojo.debug("XMLHttpTransport error callback failed: "+e2);
}
}
}
}
clearTimeout(this.inFlightTimer);
if(this.inFlight.length==0){
this.inFlightTimer=null;
return;
}
this.inFlightTimer=setTimeout("dojo.io.XMLHTTPTransport.watchInFlight();",10);
};
var _88a=dojo.hostenv.getXmlhttpObject()?true:false;
this.canHandle=function(_88b){
var mlc=_88b["mimetype"].toLowerCase()||"";
return _88a&&((dojo.lang.inArray(["text/plain","text/html","application/xml","text/xml","text/javascript"],mlc))||(mlc.substr(0,9)=="text/json"||mlc.substr(0,16)=="application/json"))&&!(_88b["formNode"]&&dojo.io.formHasFile(_88b["formNode"]));
};
this.multipartBoundary="45309FFF-BD65-4d50-99C9-36986896A96F";
this.bind=function(_88d){
if(!_88d["url"]){
if(!_88d["formNode"]&&(_88d["backButton"]||_88d["back"]||_88d["changeUrl"]||_88d["watchForURL"])&&(!djConfig.preventBackButtonFix)){
dojo.deprecated("Using dojo.io.XMLHTTPTransport.bind() to add to browser history without doing an IO request","Use dojo.undo.browser.addToHistory() instead.","0.4");
dojo.undo.browser.addToHistory(_88d);
return true;
}
}
var url=_88d.url;
var _88f="";
if(_88d["formNode"]){
var ta=_88d.formNode.getAttribute("action");
if((ta)&&(!_88d["url"])){
url=ta;
}
var tp=_88d.formNode.getAttribute("method");
if((tp)&&(!_88d["method"])){
_88d.method=tp;
}
_88f+=dojo.io.encodeForm(_88d.formNode,_88d.encoding,_88d["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
if(_88d["file"]){
_88d.method="post";
}
if(!_88d["method"]){
_88d.method="get";
}
if(_88d.method.toLowerCase()=="get"){
_88d.multipart=false;
}else{
if(_88d["file"]){
_88d.multipart=true;
}else{
if(!_88d["multipart"]){
_88d.multipart=false;
}
}
}
if(_88d["backButton"]||_88d["back"]||_88d["changeUrl"]){
dojo.undo.browser.addToHistory(_88d);
}
var _892=_88d["content"]||{};
if(_88d.sendTransport){
_892["dojo.transport"]="xmlhttp";
}
do{
if(_88d.postContent){
_88f=_88d.postContent;
break;
}
if(_892){
_88f+=dojo.io.argsFromMap(_892,_88d.encoding);
}
if(_88d.method.toLowerCase()=="get"||!_88d.multipart){
break;
}
var t=[];
if(_88f.length){
var q=_88f.split("&");
for(var i=0;i<q.length;++i){
if(q[i].length){
var p=q[i].split("=");
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+p[0]+"\"","",p[1]);
}
}
}
if(_88d.file){
if(dojo.lang.isArray(_88d.file)){
for(var i=0;i<_88d.file.length;++i){
var o=_88d.file[i];
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}else{
var o=_88d.file;
t.push("--"+this.multipartBoundary,"Content-Disposition: form-data; name=\""+o.name+"\"; filename=\""+("fileName" in o?o.fileName:o.name)+"\"","Content-Type: "+("contentType" in o?o.contentType:"application/octet-stream"),"",o.content);
}
}
if(t.length){
t.push("--"+this.multipartBoundary+"--","");
_88f=t.join("\r\n");
}
}while(false);
var _898=_88d["sync"]?false:true;
var _899=_88d["preventCache"]||(this.preventCache==true&&_88d["preventCache"]!=false);
var _89a=_88d["useCache"]==true||(this.useCache==true&&_88d["useCache"]!=false);
if(!_899&&_89a){
var _89b=getFromCache(url,_88f,_88d.method);
if(_89b){
doLoad(_88d,_89b,url,_88f,false);
return;
}
}
var http=dojo.hostenv.getXmlhttpObject(_88d);
var _89d=false;
if(_898){
var _89e=this.inFlight.push({"req":_88d,"http":http,"url":url,"query":_88f,"useCache":_89a,"startTime":_88d.timeoutSeconds?(new Date()).getTime():0});
this.startWatchingInFlight();
}else{
_86c._blockAsync=true;
}
if(_88d.method.toLowerCase()=="post"){
if(!_88d.user){
http.open("POST",url,_898);
}else{
http.open("POST",url,_898,_88d.user,_88d.password);
}
setHeaders(http,_88d);
http.setRequestHeader("Content-Type",_88d.multipart?("multipart/form-data; boundary="+this.multipartBoundary):(_88d.contentType||"application/x-www-form-urlencoded"));
try{
http.send(_88f);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_88d,{status:404},url,_88f,_89a);
}
}else{
var _89f=url;
if(_88f!=""){
_89f+=(_89f.indexOf("?")>-1?"&":"?")+_88f;
}
if(_899){
_89f+=(dojo.string.endsWithAny(_89f,"?","&")?"":(_89f.indexOf("?")>-1?"&":"?"))+"dojo.preventCache="+new Date().valueOf();
}
if(!_88d.user){
http.open(_88d.method.toUpperCase(),_89f,_898);
}else{
http.open(_88d.method.toUpperCase(),_89f,_898,_88d.user,_88d.password);
}
setHeaders(http,_88d);
try{
http.send(null);
}
catch(e){
if(typeof http.abort=="function"){
http.abort();
}
doLoad(_88d,{status:404},url,_88f,_89a);
}
}
if(!_898){
doLoad(_88d,http,url,_88f,_89a);
_86c._blockAsync=false;
}
_88d.abort=function(){
try{
http._aborted=true;
}
catch(e){
}
return http.abort();
};
return;
};
dojo.io.transports.addTransport("XMLHTTPTransport");
};
}
dojo.provide("dojo.io.cookie");
dojo.io.cookie.setCookie=function(name,_8a1,days,path,_8a4,_8a5){
var _8a6=-1;
if((typeof days=="number")&&(days>=0)){
var d=new Date();
d.setTime(d.getTime()+(days*24*60*60*1000));
_8a6=d.toGMTString();
}
_8a1=escape(_8a1);
document.cookie=name+"="+_8a1+";"+(_8a6!=-1?" expires="+_8a6+";":"")+(path?"path="+path:"")+(_8a4?"; domain="+_8a4:"")+(_8a5?"; secure":"");
};
dojo.io.cookie.set=dojo.io.cookie.setCookie;
dojo.io.cookie.getCookie=function(name){
var idx=document.cookie.lastIndexOf(name+"=");
if(idx==-1){
return null;
}
var _8aa=document.cookie.substring(idx+name.length+1);
var end=_8aa.indexOf(";");
if(end==-1){
end=_8aa.length;
}
_8aa=_8aa.substring(0,end);
_8aa=unescape(_8aa);
return _8aa;
};
dojo.io.cookie.get=dojo.io.cookie.getCookie;
dojo.io.cookie.deleteCookie=function(name){
dojo.io.cookie.setCookie(name,"-",0);
};
dojo.io.cookie.setObjectCookie=function(name,obj,days,path,_8b1,_8b2,_8b3){
if(arguments.length==5){
_8b3=_8b1;
_8b1=null;
_8b2=null;
}
var _8b4=[],_8b5,_8b6="";
if(!_8b3){
_8b5=dojo.io.cookie.getObjectCookie(name);
}
if(days>=0){
if(!_8b5){
_8b5={};
}
for(var prop in obj){
if(obj[prop]==null){
delete _8b5[prop];
}else{
if((typeof obj[prop]=="string")||(typeof obj[prop]=="number")){
_8b5[prop]=obj[prop];
}
}
}
prop=null;
for(var prop in _8b5){
_8b4.push(escape(prop)+"="+escape(_8b5[prop]));
}
_8b6=_8b4.join("&");
}
dojo.io.cookie.setCookie(name,_8b6,days,path,_8b1,_8b2);
};
dojo.io.cookie.getObjectCookie=function(name){
var _8b9=null,_8ba=dojo.io.cookie.getCookie(name);
if(_8ba){
_8b9={};
var _8bb=_8ba.split("&");
for(var i=0;i<_8bb.length;i++){
var pair=_8bb[i].split("=");
var _8be=pair[1];
if(isNaN(_8be)){
_8be=unescape(pair[1]);
}
_8b9[unescape(pair[0])]=_8be;
}
}
return _8b9;
};
dojo.io.cookie.isSupported=function(){
if(typeof navigator.cookieEnabled!="boolean"){
dojo.io.cookie.setCookie("__TestingYourBrowserForCookieSupport__","CookiesAllowed",90,null);
var _8bf=dojo.io.cookie.getCookie("__TestingYourBrowserForCookieSupport__");
navigator.cookieEnabled=(_8bf=="CookiesAllowed");
if(navigator.cookieEnabled){
this.deleteCookie("__TestingYourBrowserForCookieSupport__");
}
}
return navigator.cookieEnabled;
};
if(!dojo.io.cookies){
dojo.io.cookies=dojo.io.cookie;
}
dojo.kwCompoundRequire({common:["dojo.io.common"],rhino:["dojo.io.RhinoIO"],browser:["dojo.io.BrowserIO","dojo.io.cookie"],dashboard:["dojo.io.BrowserIO","dojo.io.cookie"]});
dojo.provide("dojo.io.*");
dojo.provide("dojo.widget.ContentPane");
dojo.widget.defineWidget("dojo.widget.ContentPane",dojo.widget.HtmlWidget,function(){
this._styleNodes=[];
this._onLoadStack=[];
this._onUnloadStack=[];
this._callOnUnload=false;
this._ioBindObj;
this.scriptScope;
this.bindArgs={};
},{isContainer:true,adjustPaths:true,href:"",extractContent:true,parseContent:true,cacheContent:true,preload:false,refreshOnShow:false,handler:"",executeScripts:false,scriptSeparation:true,loadingMessage:"Loading...",isLoaded:false,postCreate:function(args,frag,_8c2){
if(this.handler!==""){
this.setHandler(this.handler);
}
if(this.isShowing()||this.preload){
this.loadContents();
}
},show:function(){
if(this.refreshOnShow){
this.refresh();
}else{
this.loadContents();
}
dojo.widget.ContentPane.superclass.show.call(this);
},refresh:function(){
this.isLoaded=false;
this.loadContents();
},loadContents:function(){
if(this.isLoaded){
return;
}
if(dojo.lang.isFunction(this.handler)){
this._runHandler();
}else{
if(this.href!=""){
this._downloadExternalContent(this.href,this.cacheContent&&!this.refreshOnShow);
}
}
},setUrl:function(url){
this.href=url;
this.isLoaded=false;
if(this.preload||this.isShowing()){
this.loadContents();
}
},abort:function(){
var bind=this._ioBindObj;
if(!bind||!bind.abort){
return;
}
bind.abort();
delete this._ioBindObj;
},_downloadExternalContent:function(url,_8c6){
this.abort();
this._handleDefaults(this.loadingMessage,"onDownloadStart");
var self=this;
this._ioBindObj=dojo.io.bind(this._cacheSetting({url:url,mimetype:"text/html",handler:function(type,data,xhr){
delete self._ioBindObj;
if(type=="load"){
self.onDownloadEnd.call(self,url,data);
}else{
var e={responseText:xhr.responseText,status:xhr.status,statusText:xhr.statusText,responseHeaders:xhr.getAllResponseHeaders(),text:"Error loading '"+url+"' ("+xhr.status+" "+xhr.statusText+")"};
self._handleDefaults.call(self,e,"onDownloadError");
self.onLoad();
}
}},_8c6));
},_cacheSetting:function(_8cc,_8cd){
for(var x in this.bindArgs){
if(dojo.lang.isUndefined(_8cc[x])){
_8cc[x]=this.bindArgs[x];
}
}
if(dojo.lang.isUndefined(_8cc.useCache)){
_8cc.useCache=_8cd;
}
if(dojo.lang.isUndefined(_8cc.preventCache)){
_8cc.preventCache=!_8cd;
}
if(dojo.lang.isUndefined(_8cc.mimetype)){
_8cc.mimetype="text/html";
}
return _8cc;
},onLoad:function(e){
this._runStack("_onLoadStack");
this.isLoaded=true;
},onUnLoad:function(e){
dojo.deprecated(this.widgetType+".onUnLoad, use .onUnload (lowercased load)",0.5);
},onUnload:function(e){
this._runStack("_onUnloadStack");
delete this.scriptScope;
if(this.onUnLoad!==dojo.widget.ContentPane.prototype.onUnLoad){
this.onUnLoad.apply(this,arguments);
}
},_runStack:function(_8d2){
var st=this[_8d2];
var err="";
var _8d5=this.scriptScope||window;
for(var i=0;i<st.length;i++){
try{
st[i].call(_8d5);
}
catch(e){
err+="\n"+st[i]+" failed: "+e.description;
}
}
this[_8d2]=[];
if(err.length){
var name=(_8d2=="_onLoadStack")?"addOnLoad":"addOnUnLoad";
this._handleDefaults(name+" failure\n "+err,"onExecError","debug");
}
},addOnLoad:function(obj,func){
this._pushOnStack(this._onLoadStack,obj,func);
},addOnUnload:function(obj,func){
this._pushOnStack(this._onUnloadStack,obj,func);
},addOnUnLoad:function(){
dojo.deprecated(this.widgetType+".addOnUnLoad, use addOnUnload instead. (lowercased Load)",0.5);
this.addOnUnload.apply(this,arguments);
},_pushOnStack:function(_8dc,obj,func){
if(typeof func=="undefined"){
_8dc.push(obj);
}else{
_8dc.push(function(){
obj[func]();
});
}
},destroy:function(){
this.onUnload();
dojo.widget.ContentPane.superclass.destroy.call(this);
},onExecError:function(e){
},onContentError:function(e){
},onDownloadError:function(e){
},onDownloadStart:function(e){
},onDownloadEnd:function(url,data){
data=this.splitAndFixPaths(data,url);
this.setContent(data);
},_handleDefaults:function(e,_8e6,_8e7){
if(!_8e6){
_8e6="onContentError";
}
if(dojo.lang.isString(e)){
e={text:e};
}
if(!e.text){
e.text=e.toString();
}
e.toString=function(){
return this.text;
};
if(typeof e.returnValue!="boolean"){
e.returnValue=true;
}
if(typeof e.preventDefault!="function"){
e.preventDefault=function(){
this.returnValue=false;
};
}
this[_8e6](e);
if(e.returnValue){
switch(_8e7){
case true:
case "alert":
alert(e.toString());
break;
case "debug":
dojo.debug(e.toString());
break;
default:
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=false;
if(arguments.callee._loopStop){
dojo.debug(e.toString());
}else{
arguments.callee._loopStop=true;
this._setContent(e.toString());
}
}
}
arguments.callee._loopStop=false;
},splitAndFixPaths:function(s,url){
var _8ea=[],_8eb=[],tmp=[];
var _8ed=[],_8ee=[],attr=[],_8f0=[];
var str="",path="",fix="",_8f4="",tag="",_8f6="";
if(!url){
url="./";
}
if(s){
var _8f7=/<title[^>]*>([\s\S]*?)<\/title>/i;
while(_8ed=_8f7.exec(s)){
_8ea.push(_8ed[1]);
s=s.substring(0,_8ed.index)+s.substr(_8ed.index+_8ed[0].length);
}
if(this.adjustPaths){
var _8f8=/<[a-z][a-z0-9]*[^>]*\s(?:(?:src|href|style)=[^>])+[^>]*>/i;
var _8f9=/\s(src|href|style)=(['"]?)([\w()\[\]\/.,\\'"-:;#=&?\s@]+?)\2/i;
var _8fa=/^(?:[#]|(?:(?:https?|ftps?|file|javascript|mailto|news):))/;
while(tag=_8f8.exec(s)){
str+=s.substring(0,tag.index);
s=s.substring((tag.index+tag[0].length),s.length);
tag=tag[0];
_8f4="";
while(attr=_8f9.exec(tag)){
path="";
_8f6=attr[3];
switch(attr[1].toLowerCase()){
case "src":
case "href":
if(_8fa.exec(_8f6)){
path=_8f6;
}else{
path=(new dojo.uri.Uri(url,_8f6).toString());
}
break;
case "style":
path=dojo.html.fixPathsInCssText(_8f6,url);
break;
default:
path=_8f6;
}
fix=" "+attr[1]+"="+attr[2]+path+attr[2];
_8f4+=tag.substring(0,attr.index)+fix;
tag=tag.substring((attr.index+attr[0].length),tag.length);
}
str+=_8f4+tag;
}
s=str+s;
}
_8f7=/(?:<(style)[^>]*>([\s\S]*?)<\/style>|<link ([^>]*rel=['"]?stylesheet['"]?[^>]*)>)/i;
while(_8ed=_8f7.exec(s)){
if(_8ed[1]&&_8ed[1].toLowerCase()=="style"){
_8f0.push(dojo.html.fixPathsInCssText(_8ed[2],url));
}else{
if(attr=_8ed[3].match(/href=(['"]?)([^'">]*)\1/i)){
_8f0.push({path:attr[2]});
}
}
s=s.substring(0,_8ed.index)+s.substr(_8ed.index+_8ed[0].length);
}
var _8f7=/<script([^>]*)>([\s\S]*?)<\/script>/i;
var _8fb=/src=(['"]?)([^"']*)\1/i;
var _8fc=/.*(\bdojo\b\.js(?:\.uncompressed\.js)?)$/;
var _8fd=/(?:var )?\bdjConfig\b(?:[\s]*=[\s]*\{[^}]+\}|\.[\w]*[\s]*=[\s]*[^;\n]*)?;?|dojo\.hostenv\.writeIncludes\(\s*\);?/g;
var _8fe=/dojo\.(?:(?:require(?:After)?(?:If)?)|(?:widget\.(?:manager\.)?registerWidgetPackage)|(?:(?:hostenv\.)?setModulePrefix|registerModulePath)|defineNamespace)\((['"]).*?\1\)\s*;?/;
while(_8ed=_8f7.exec(s)){
if(this.executeScripts&&_8ed[1]){
if(attr=_8fb.exec(_8ed[1])){
if(_8fc.exec(attr[2])){
dojo.debug("Security note! inhibit:"+attr[2]+" from  being loaded again.");
}else{
_8eb.push({path:attr[2]});
}
}
}
if(_8ed[2]){
var sc=_8ed[2].replace(_8fd,"");
if(!sc){
continue;
}
while(tmp=_8fe.exec(sc)){
_8ee.push(tmp[0]);
sc=sc.substring(0,tmp.index)+sc.substr(tmp.index+tmp[0].length);
}
if(this.executeScripts){
_8eb.push(sc);
}
}
s=s.substr(0,_8ed.index)+s.substr(_8ed.index+_8ed[0].length);
}
if(this.extractContent){
_8ed=s.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_8ed){
s=_8ed[1];
}
}
if(this.executeScripts&&this.scriptSeparation){
var _8f7=/(<[a-zA-Z][a-zA-Z0-9]*\s[^>]*?\S=)((['"])[^>]*scriptScope[^>]*>)/;
var _900=/([\s'";:\(])scriptScope(.*)/;
str="";
while(tag=_8f7.exec(s)){
tmp=((tag[3]=="'")?"\"":"'");
fix="";
str+=s.substring(0,tag.index)+tag[1];
while(attr=_900.exec(tag[2])){
tag[2]=tag[2].substring(0,attr.index)+attr[1]+"dojo.widget.byId("+tmp+this.widgetId+tmp+").scriptScope"+attr[2];
}
str+=tag[2];
s=s.substr(tag.index+tag[0].length);
}
s=str+s;
}
}
return {"xml":s,"styles":_8f0,"titles":_8ea,"requires":_8ee,"scripts":_8eb,"url":url};
},_setContent:function(cont){
this.destroyChildren();
for(var i=0;i<this._styleNodes.length;i++){
if(this._styleNodes[i]&&this._styleNodes[i].parentNode){
this._styleNodes[i].parentNode.removeChild(this._styleNodes[i]);
}
}
this._styleNodes=[];
try{
var node=this.containerNode||this.domNode;
while(node.firstChild){
dojo.html.destroyNode(node.firstChild);
}
if(typeof cont!="string"){
node.appendChild(cont);
}else{
node.innerHTML=cont;
}
}
catch(e){
e.text="Couldn't load content:"+e.description;
this._handleDefaults(e,"onContentError");
}
},setContent:function(data){
this.abort();
if(this._callOnUnload){
this.onUnload();
}
this._callOnUnload=true;
if(!data||dojo.html.isNode(data)){
this._setContent(data);
this.onResized();
this.onLoad();
}else{
if(typeof data.xml!="string"){
this.href="";
data=this.splitAndFixPaths(data);
}
this._setContent(data.xml);
for(var i=0;i<data.styles.length;i++){
if(data.styles[i].path){
this._styleNodes.push(dojo.html.insertCssFile(data.styles[i].path,dojo.doc(),false,true));
}else{
this._styleNodes.push(dojo.html.insertCssText(data.styles[i]));
}
}
if(this.parseContent){
for(var i=0;i<data.requires.length;i++){
try{
eval(data.requires[i]);
}
catch(e){
e.text="ContentPane: error in package loading calls, "+(e.description||e);
this._handleDefaults(e,"onContentError","debug");
}
}
}
var _906=this;
function asyncParse(){
if(_906.executeScripts){
_906._executeScripts(data.scripts);
}
if(_906.parseContent){
var node=_906.containerNode||_906.domNode;
var _908=new dojo.xml.Parse();
var frag=_908.parseElement(node,null,true);
dojo.widget.getParser().createSubComponents(frag,_906);
}
_906.onResized();
_906.onLoad();
}
if(dojo.hostenv.isXDomain&&data.requires.length){
dojo.addOnLoad(asyncParse);
}else{
asyncParse();
}
}
},setHandler:function(_90a){
var fcn=dojo.lang.isFunction(_90a)?_90a:window[_90a];
if(!dojo.lang.isFunction(fcn)){
this._handleDefaults("Unable to set handler, '"+_90a+"' not a function.","onExecError",true);
return;
}
this.handler=function(){
return fcn.apply(this,arguments);
};
},_runHandler:function(){
var ret=true;
if(dojo.lang.isFunction(this.handler)){
this.handler(this,this.domNode);
ret=false;
}
this.onLoad();
return ret;
},_executeScripts:function(_90d){
var self=this;
var tmp="",code="";
for(var i=0;i<_90d.length;i++){
if(_90d[i].path){
dojo.io.bind(this._cacheSetting({"url":_90d[i].path,"load":function(type,_913){
dojo.lang.hitch(self,tmp=";"+_913);
},"error":function(type,_915){
_915.text=type+" downloading remote script";
self._handleDefaults.call(self,_915,"onExecError","debug");
},"mimetype":"text/plain","sync":true},this.cacheContent));
code+=tmp;
}else{
code+=_90d[i];
}
}
try{
if(this.scriptSeparation){
delete this.scriptScope;
this.scriptScope=new (new Function("_container_",code+"; return this;"))(self);
}else{
var djg=dojo.global();
if(djg.execScript){
djg.execScript(code);
}else{
var djd=dojo.doc();
var sc=djd.createElement("script");
sc.appendChild(djd.createTextNode(code));
(this.containerNode||this.domNode).appendChild(sc);
}
}
}
catch(e){
e.text="Error running scripts from content:\n"+e.description;
this._handleDefaults(e,"onExecError","debug");
}
}});
dojo.provide("dojo.html.iframe");
dojo.html.iframeContentWindow=function(_919){
var win=dojo.html.getDocumentWindow(dojo.html.iframeContentDocument(_919))||dojo.html.iframeContentDocument(_919).__parent__||(_919.name&&document.frames[_919.name])||null;
return win;
};
dojo.html.iframeContentDocument=function(_91b){
var doc=_91b.contentDocument||((_91b.contentWindow)&&(_91b.contentWindow.document))||((_91b.name)&&(document.frames[_91b.name])&&(document.frames[_91b.name].document))||null;
return doc;
};
dojo.html.BackgroundIframe=function(node){
if(dojo.render.html.ie55||dojo.render.html.ie60){
var html="<iframe src='javascript:false'"+" style='position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"+"z-index: -1; filter:Alpha(Opacity=\"0\");' "+">";
this.iframe=dojo.doc().createElement(html);
this.iframe.tabIndex=-1;
if(node){
node.appendChild(this.iframe);
this.domNode=node;
}else{
dojo.body().appendChild(this.iframe);
this.iframe.style.display="none";
}
}
};
dojo.lang.extend(dojo.html.BackgroundIframe,{iframe:null,onResized:function(){
if(this.iframe&&this.domNode&&this.domNode.parentNode){
var _91f=dojo.html.getMarginBox(this.domNode);
if(_91f.width==0||_91f.height==0){
dojo.lang.setTimeout(this,this.onResized,100);
return;
}
this.iframe.style.width=_91f.width+"px";
this.iframe.style.height=_91f.height+"px";
}
},size:function(node){
if(!this.iframe){
return;
}
var _921=dojo.html.toCoordinateObject(node,true,dojo.html.boxSizing.BORDER_BOX);
with(this.iframe.style){
width=_921.width+"px";
height=_921.height+"px";
left=_921.left+"px";
top=_921.top+"px";
}
},setZIndex:function(node){
if(!this.iframe){
return;
}
if(dojo.dom.isNode(node)){
this.iframe.style.zIndex=dojo.html.getStyle(node,"z-index")-1;
}else{
if(!isNaN(node)){
this.iframe.style.zIndex=node;
}
}
},show:function(){
if(this.iframe){
this.iframe.style.display="block";
}
},hide:function(){
if(this.iframe){
this.iframe.style.display="none";
}
},remove:function(){
if(this.iframe){
dojo.html.removeNode(this.iframe,true);
delete this.iframe;
this.iframe=null;
}
}});
dojo.provide("dojo.widget.Dialog");
dojo.declare("dojo.widget.ModalDialogBase",null,{isContainer:true,focusElement:"",bgColor:"black",bgOpacity:0.4,followScroll:true,closeOnBackgroundClick:false,trapTabs:function(e){
if(e.target==this.tabStartOuter){
if(this._fromTrap){
this.tabStart.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabStart){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabEnd.focus();
}
}else{
if(e.target==this.tabEndOuter){
if(this._fromTrap){
this.tabEnd.focus();
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}else{
if(e.target==this.tabEnd){
if(this._fromTrap){
this._fromTrap=false;
}else{
this._fromTrap=true;
this.tabStart.focus();
}
}
}
}
}
},clearTrap:function(e){
var _925=this;
setTimeout(function(){
_925._fromTrap=false;
},100);
},postCreate:function(){
with(this.domNode.style){
position="absolute";
zIndex=999;
display="none";
overflow="visible";
}
var b=dojo.body();
b.appendChild(this.domNode);
this.bg=document.createElement("div");
this.bg.className="dialogUnderlay";
with(this.bg.style){
position="absolute";
left=top="0px";
zIndex=998;
display="none";
}
b.appendChild(this.bg);
this.setBackgroundColor(this.bgColor);
this.bgIframe=new dojo.html.BackgroundIframe();
if(this.bgIframe.iframe){
with(this.bgIframe.iframe.style){
position="absolute";
left=top="0px";
zIndex=90;
display="none";
}
}
if(this.closeOnBackgroundClick){
dojo.event.kwConnect({srcObj:this.bg,srcFunc:"onclick",adviceObj:this,adviceFunc:"onBackgroundClick",once:true});
}
},uninitialize:function(){
this.bgIframe.remove();
dojo.html.removeNode(this.bg,true);
},setBackgroundColor:function(_927){
if(arguments.length>=3){
_927=new dojo.gfx.color.Color(arguments[0],arguments[1],arguments[2]);
}else{
_927=new dojo.gfx.color.Color(_927);
}
this.bg.style.backgroundColor=_927.toString();
return this.bgColor=_927;
},setBackgroundOpacity:function(op){
if(arguments.length==0){
op=this.bgOpacity;
}
dojo.html.setOpacity(this.bg,op);
try{
this.bgOpacity=dojo.html.getOpacity(this.bg);
}
catch(e){
this.bgOpacity=op;
}
return this.bgOpacity;
},_sizeBackground:function(){
if(this.bgOpacity>0){
var _929=dojo.html.getViewport();
var h=_929.height;
var w=_929.width;
with(this.bg.style){
width=w+"px";
height=h+"px";
}
var _92c=dojo.html.getScroll().offset;
this.bg.style.top=_92c.y+"px";
this.bg.style.left=_92c.x+"px";
var _929=dojo.html.getViewport();
if(_929.width!=w){
this.bg.style.width=_929.width+"px";
}
if(_929.height!=h){
this.bg.style.height=_929.height+"px";
}
}
this.bgIframe.size(this.bg);
},_showBackground:function(){
if(this.bgOpacity>0){
this.bg.style.display="block";
}
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="block";
}
},placeModalDialog:function(){
var _92d=dojo.html.getScroll().offset;
var _92e=dojo.html.getViewport();
var mb;
if(this.isShowing()){
mb=dojo.html.getMarginBox(this.domNode);
}else{
dojo.html.setVisibility(this.domNode,false);
dojo.html.show(this.domNode);
mb=dojo.html.getMarginBox(this.domNode);
dojo.html.hide(this.domNode);
dojo.html.setVisibility(this.domNode,true);
}
var x=_92d.x+(_92e.width-mb.width)/2;
var y=_92d.y+(_92e.height-mb.height)/2;
with(this.domNode.style){
left=x+"px";
top=y+"px";
}
},_onKey:function(evt){
if(evt.key){
var node=evt.target;
while(node!=null){
if(node==this.domNode){
return;
}
node=node.parentNode;
}
if(evt.key!=evt.KEY_TAB){
dojo.event.browser.stopEvent(evt);
}else{
if(!dojo.render.html.opera){
try{
this.tabStart.focus();
}
catch(e){
}
}
}
}
},showModalDialog:function(){
if(this.followScroll&&!this._scrollConnected){
this._scrollConnected=true;
dojo.event.connect(window,"onscroll",this,"_onScroll");
}
dojo.event.connect(document.documentElement,"onkey",this,"_onKey");
this.placeModalDialog();
this.setBackgroundOpacity();
this._sizeBackground();
this._showBackground();
this._fromTrap=true;
setTimeout(dojo.lang.hitch(this,function(){
try{
this.tabStart.focus();
}
catch(e){
}
}),50);
},hideModalDialog:function(){
if(this.focusElement){
dojo.byId(this.focusElement).focus();
dojo.byId(this.focusElement).blur();
}
this.bg.style.display="none";
this.bg.style.width=this.bg.style.height="1px";
if(this.bgIframe.iframe){
this.bgIframe.iframe.style.display="none";
}
dojo.event.disconnect(document.documentElement,"onkey",this,"_onKey");
if(this._scrollConnected){
this._scrollConnected=false;
dojo.event.disconnect(window,"onscroll",this,"_onScroll");
}
},_onScroll:function(){
var _934=dojo.html.getScroll().offset;
this.bg.style.top=_934.y+"px";
this.bg.style.left=_934.x+"px";
this.placeModalDialog();
},checkSize:function(){
if(this.isShowing()){
this._sizeBackground();
this.placeModalDialog();
this.onResized();
}
},onBackgroundClick:function(){
if(this.lifetime-this.timeRemaining>=this.blockDuration){
return;
}
this.hide();
}});
dojo.widget.defineWidget("dojo.widget.Dialog",[dojo.widget.ContentPane,dojo.widget.ModalDialogBase],{templatePath:dojo.uri.moduleUri("dojo.widget","templates/Dialog.html"),blockDuration:0,lifetime:0,closeNode:"",postMixInProperties:function(){
dojo.widget.Dialog.superclass.postMixInProperties.apply(this,arguments);
if(this.closeNode){
this.setCloseControl(this.closeNode);
}
},postCreate:function(){
dojo.widget.Dialog.superclass.postCreate.apply(this,arguments);
dojo.widget.ModalDialogBase.prototype.postCreate.apply(this,arguments);
},show:function(){
if(this.lifetime){
this.timeRemaining=this.lifetime;
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
if(this.blockDuration&&this.closeNode){
if(this.lifetime>this.blockDuration){
this.closeNode.style.visibility="hidden";
}else{
this.closeNode.style.display="none";
}
}
if(this.timer){
clearInterval(this.timer);
}
this.timer=setInterval(dojo.lang.hitch(this,"_onTick"),100);
}
this.showModalDialog();
dojo.widget.Dialog.superclass.show.call(this);
},onLoad:function(){
this.placeModalDialog();
dojo.widget.Dialog.superclass.onLoad.call(this);
},fillInTemplate:function(){
},hide:function(){
this.hideModalDialog();
dojo.widget.Dialog.superclass.hide.call(this);
if(this.timer){
clearInterval(this.timer);
}
},setTimerNode:function(node){
this.timerNode=node;
},setCloseControl:function(node){
this.closeNode=dojo.byId(node);
dojo.event.connect(this.closeNode,"onclick",this,"hide");
},setShowControl:function(node){
node=dojo.byId(node);
dojo.event.connect(node,"onclick",this,"show");
},_onTick:function(){
if(this.timer){
this.timeRemaining-=100;
if(this.lifetime-this.timeRemaining>=this.blockDuration){
if(this.closeNode){
this.closeNode.style.visibility="visible";
}
}
if(!this.timeRemaining){
clearInterval(this.timer);
this.hide();
}else{
if(this.timerNode){
this.timerNode.innerHTML=Math.ceil(this.timeRemaining/1000);
}
}
}
}});
dojo.provide("dojo.html.selection");
dojo.html.selectionType={NONE:0,TEXT:1,CONTROL:2};
dojo.html.clearSelection=function(){
var _938=dojo.global();
var _939=dojo.doc();
try{
if(_938["getSelection"]){
if(dojo.render.html.safari){
_938.getSelection().collapse();
}else{
_938.getSelection().removeAllRanges();
}
}else{
if(_939.selection){
if(_939.selection.empty){
_939.selection.empty();
}else{
if(_939.selection.clear){
_939.selection.clear();
}
}
}
}
return true;
}
catch(e){
dojo.debug(e);
return false;
}
};
dojo.html.disableSelection=function(_93a){
_93a=dojo.byId(_93a)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_93a.style.MozUserSelect="none";
}else{
if(h.safari){
_93a.style.KhtmlUserSelect="none";
}else{
if(h.ie){
_93a.unselectable="on";
}else{
return false;
}
}
}
return true;
};
dojo.html.enableSelection=function(_93c){
_93c=dojo.byId(_93c)||dojo.body();
var h=dojo.render.html;
if(h.mozilla){
_93c.style.MozUserSelect="";
}else{
if(h.safari){
_93c.style.KhtmlUserSelect="";
}else{
if(h.ie){
_93c.unselectable="off";
}else{
return false;
}
}
}
return true;
};
dojo.html.selectElement=function(_93e){
dojo.deprecated("dojo.html.selectElement","replaced by dojo.html.selection.selectElementChildren",0.5);
};
dojo.html.selectInputText=function(_93f){
var _940=dojo.global();
var _941=dojo.doc();
_93f=dojo.byId(_93f);
if(_941["selection"]&&dojo.body()["createTextRange"]){
var _942=_93f.createTextRange();
_942.moveStart("character",0);
_942.moveEnd("character",_93f.value.length);
_942.select();
}else{
if(_940["getSelection"]){
var _943=_940.getSelection();
_93f.setSelectionRange(0,_93f.value.length);
}
}
_93f.focus();
};
dojo.html.isSelectionCollapsed=function(){
dojo.deprecated("dojo.html.isSelectionCollapsed","replaced by dojo.html.selection.isCollapsed",0.5);
return dojo.html.selection.isCollapsed();
};
dojo.lang.mixin(dojo.html.selection,{getType:function(){
if(dojo.doc()["selection"]){
return dojo.html.selectionType[dojo.doc().selection.type.toUpperCase()];
}else{
var _944=dojo.html.selectionType.TEXT;
var oSel;
try{
oSel=dojo.global().getSelection();
}
catch(e){
}
if(oSel&&oSel.rangeCount==1){
var _946=oSel.getRangeAt(0);
if(_946.startContainer==_946.endContainer&&(_946.endOffset-_946.startOffset)==1&&_946.startContainer.nodeType!=dojo.dom.TEXT_NODE){
_944=dojo.html.selectionType.CONTROL;
}
}
return _944;
}
},isCollapsed:function(){
var _947=dojo.global();
var _948=dojo.doc();
if(_948["selection"]){
return _948.selection.createRange().text=="";
}else{
if(_947["getSelection"]){
var _949=_947.getSelection();
if(dojo.lang.isString(_949)){
return _949=="";
}else{
return _949.isCollapsed||_949.toString()=="";
}
}
}
},getSelectedElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
if(dojo.doc()["selection"]){
var _94a=dojo.doc().selection.createRange();
if(_94a&&_94a.item){
return dojo.doc().selection.createRange().item(0);
}
}else{
var _94b=dojo.global().getSelection();
return _94b.anchorNode.childNodes[_94b.anchorOffset];
}
}
},getParentElement:function(){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
var p=dojo.html.selection.getSelectedElement();
if(p){
return p.parentNode;
}
}else{
if(dojo.doc()["selection"]){
return dojo.doc().selection.createRange().parentElement();
}else{
var _94d=dojo.global().getSelection();
if(_94d){
var node=_94d.anchorNode;
while(node&&node.nodeType!=dojo.dom.ELEMENT_NODE){
node=node.parentNode;
}
return node;
}
}
}
},getSelectedText:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().text;
}else{
var _94f=dojo.global().getSelection();
if(_94f){
return _94f.toString();
}
}
},getSelectedHtml:function(){
if(dojo.doc()["selection"]){
if(dojo.html.selection.getType()==dojo.html.selectionType.CONTROL){
return null;
}
return dojo.doc().selection.createRange().htmlText;
}else{
var _950=dojo.global().getSelection();
if(_950&&_950.rangeCount){
var frag=_950.getRangeAt(0).cloneContents();
var div=document.createElement("div");
div.appendChild(frag);
return div.innerHTML;
}
return null;
}
},hasAncestorElement:function(_953){
return (dojo.html.selection.getAncestorElement.apply(this,arguments)!=null);
},getAncestorElement:function(_954){
var node=dojo.html.selection.getSelectedElement()||dojo.html.selection.getParentElement();
while(node){
if(dojo.html.selection.isTag(node,arguments).length>0){
return node;
}
node=node.parentNode;
}
return null;
},isTag:function(node,tags){
if(node&&node.tagName){
for(var i=0;i<tags.length;i++){
if(node.tagName.toLowerCase()==String(tags[i]).toLowerCase()){
return String(tags[i]).toLowerCase();
}
}
}
return "";
},selectElement:function(_959){
var _95a=dojo.global();
var _95b=dojo.doc();
_959=dojo.byId(_959);
if(_95b.selection&&dojo.body().createTextRange){
try{
var _95c=dojo.body().createControlRange();
_95c.addElement(_959);
_95c.select();
}
catch(e){
dojo.html.selection.selectElementChildren(_959);
}
}else{
if(_95a["getSelection"]){
var _95d=_95a.getSelection();
if(_95d["removeAllRanges"]){
var _95c=_95b.createRange();
_95c.selectNode(_959);
_95d.removeAllRanges();
_95d.addRange(_95c);
}
}
}
},selectElementChildren:function(_95e){
var _95f=dojo.global();
var _960=dojo.doc();
_95e=dojo.byId(_95e);
if(_960.selection&&dojo.body().createTextRange){
var _961=dojo.body().createTextRange();
_961.moveToElementText(_95e);
_961.select();
}else{
if(_95f["getSelection"]){
var _962=_95f.getSelection();
if(_962["setBaseAndExtent"]){
_962.setBaseAndExtent(_95e,0,_95e,_95e.innerText.length-1);
}else{
if(_962["selectAllChildren"]){
_962.selectAllChildren(_95e);
}
}
}
}
},getBookmark:function(){
var _963;
var _964=dojo.doc();
if(_964["selection"]){
var _965=_964.selection.createRange();
_963=_965.getBookmark();
}else{
var _966;
try{
_966=dojo.global().getSelection();
}
catch(e){
}
if(_966){
var _965=_966.getRangeAt(0);
_963=_965.cloneRange();
}else{
dojo.debug("No idea how to store the current selection for this browser!");
}
}
return _963;
},moveToBookmark:function(_967){
var _968=dojo.doc();
if(_968["selection"]){
var _969=_968.selection.createRange();
_969.moveToBookmark(_967);
_969.select();
}else{
var _96a;
try{
_96a=dojo.global().getSelection();
}
catch(e){
}
if(_96a&&_96a["removeAllRanges"]){
_96a.removeAllRanges();
_96a.addRange(_967);
}else{
dojo.debug("No idea how to restore selection for this browser!");
}
}
},collapse:function(_96b){
if(dojo.global()["getSelection"]){
var _96c=dojo.global().getSelection();
if(_96c.removeAllRanges){
if(_96b){
_96c.collapseToStart();
}else{
_96c.collapseToEnd();
}
}else{
dojo.global().getSelection().collapse(_96b);
}
}else{
if(dojo.doc().selection){
var _96d=dojo.doc().selection.createRange();
_96d.collapse(_96b);
_96d.select();
}
}
},remove:function(){
if(dojo.doc().selection){
var _96e=dojo.doc().selection;
if(_96e.type.toUpperCase()!="NONE"){
_96e.clear();
}
return _96e;
}else{
var _96e=dojo.global().getSelection();
for(var i=0;i<_96e.rangeCount;i++){
_96e.getRangeAt(i).deleteContents();
}
return _96e;
}
}});
dojo.provide("dojo.lfx.shadow");
dojo.lfx.shadow=function(node){
this.shadowPng=dojo.uri.moduleUri("dojo.html","images/shadow");
this.shadowThickness=8;
this.shadowOffset=15;
this.init(node);
};
dojo.extend(dojo.lfx.shadow,{init:function(node){
this.node=node;
this.pieces={};
var x1=-1*this.shadowThickness;
var y0=this.shadowOffset;
var y1=this.shadowOffset+this.shadowThickness;
this._makePiece("tl","top",y0,"left",x1);
this._makePiece("l","top",y1,"left",x1,"scale");
this._makePiece("tr","top",y0,"left",0);
this._makePiece("r","top",y1,"left",0,"scale");
this._makePiece("bl","top",0,"left",x1);
this._makePiece("b","top",0,"left",0,"crop");
this._makePiece("br","top",0,"left",0);
},_makePiece:function(name,_976,_977,_978,_979,_97a){
var img;
var url=this.shadowPng+name.toUpperCase()+".png";
if(dojo.render.html.ie55||dojo.render.html.ie60){
img=dojo.doc().createElement("div");
img.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"'"+(_97a?", sizingMethod='"+_97a+"'":"")+")";
}else{
img=dojo.doc().createElement("img");
img.src=url;
}
img.style.position="absolute";
img.style[_976]=_977+"px";
img.style[_978]=_979+"px";
img.style.width=this.shadowThickness+"px";
img.style.height=this.shadowThickness+"px";
this.pieces[name]=img;
this.node.appendChild(img);
},size:function(_97d,_97e){
var _97f=_97e-(this.shadowOffset+this.shadowThickness+1);
if(_97f<0){
_97f=0;
}
if(_97e<1){
_97e=1;
}
if(_97d<1){
_97d=1;
}
with(this.pieces){
l.style.height=_97f+"px";
r.style.height=_97f+"px";
b.style.width=(_97d-1)+"px";
bl.style.top=(_97e-1)+"px";
b.style.top=(_97e-1)+"px";
br.style.top=(_97e-1)+"px";
tr.style.left=(_97d-1)+"px";
r.style.left=(_97d-1)+"px";
br.style.left=(_97d-1)+"px";
}
}});
dojo.provide("dojo.widget.html.layout");
dojo.widget.html.layout=function(_980,_981,_982){
dojo.html.addClass(_980,"dojoLayoutContainer");
_981=dojo.lang.filter(_981,function(_983,idx){
_983.idx=idx;
return dojo.lang.inArray(["top","bottom","left","right","client","flood"],_983.layoutAlign);
});
if(_982&&_982!="none"){
var rank=function(_986){
switch(_986.layoutAlign){
case "flood":
return 1;
case "left":
case "right":
return (_982=="left-right")?2:3;
case "top":
case "bottom":
return (_982=="left-right")?3:2;
default:
return 4;
}
};
_981.sort(function(a,b){
return (rank(a)-rank(b))||(a.idx-b.idx);
});
}
var f={top:dojo.html.getPixelValue(_980,"padding-top",true),left:dojo.html.getPixelValue(_980,"padding-left",true)};
dojo.lang.mixin(f,dojo.html.getContentBox(_980));
dojo.lang.forEach(_981,function(_98a){
var elm=_98a.domNode;
var pos=_98a.layoutAlign;
with(elm.style){
left=f.left+"px";
top=f.top+"px";
bottom="auto";
right="auto";
}
dojo.html.addClass(elm,"dojoAlign"+dojo.string.capitalize(pos));
if((pos=="top")||(pos=="bottom")){
dojo.html.setMarginBox(elm,{width:f.width});
var h=dojo.html.getMarginBox(elm).height;
f.height-=h;
if(pos=="top"){
f.top+=h;
}else{
elm.style.top=f.top+f.height+"px";
}
if(_98a.onResized){
_98a.onResized();
}
}else{
if(pos=="left"||pos=="right"){
var w=dojo.html.getMarginBox(elm).width;
if(_98a.resizeTo){
_98a.resizeTo(w,f.height);
}else{
dojo.html.setMarginBox(elm,{width:w,height:f.height});
}
f.width-=w;
if(pos=="left"){
f.left+=w;
}else{
elm.style.left=f.left+f.width+"px";
}
}else{
if(pos=="flood"||pos=="client"){
if(_98a.resizeTo){
_98a.resizeTo(f.width,f.height);
}else{
dojo.html.setMarginBox(elm,{width:f.width,height:f.height});
}
}
}
}
});
};
dojo.html.insertCssText(".dojoLayoutContainer{ position: relative; display: block; overflow: hidden; }\n"+"body .dojoAlignTop, body .dojoAlignBottom, body .dojoAlignLeft, body .dojoAlignRight { position: absolute; overflow: hidden; }\n"+"body .dojoAlignClient { position: absolute }\n"+".dojoAlignClient { overflow: auto; }\n");
dojo.provide("dojo.dnd.DragAndDrop");
dojo.declare("dojo.dnd.DragSource",null,{type:"",onDragEnd:function(evt){
},onDragStart:function(evt){
},onSelected:function(evt){
},unregister:function(){
dojo.dnd.dragManager.unregisterDragSource(this);
},reregister:function(){
dojo.dnd.dragManager.registerDragSource(this);
}});
dojo.declare("dojo.dnd.DragObject",null,{type:"",register:function(){
var dm=dojo.dnd.dragManager;
if(dm["registerDragObject"]){
dm.registerDragObject(this);
}
},onDragStart:function(evt){
},onDragMove:function(evt){
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragEnd:function(evt){
},onDragLeave:dojo.lang.forward("onDragOut"),onDragEnter:dojo.lang.forward("onDragOver"),ondragout:dojo.lang.forward("onDragOut"),ondragover:dojo.lang.forward("onDragOver")});
dojo.declare("dojo.dnd.DropTarget",null,{acceptsType:function(type){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
if(!dojo.lang.inArray(this.acceptedTypes,type)){
return false;
}
}
return true;
},accepts:function(_999){
if(!dojo.lang.inArray(this.acceptedTypes,"*")){
for(var i=0;i<_999.length;i++){
if(!dojo.lang.inArray(this.acceptedTypes,_999[i].type)){
return false;
}
}
}
return true;
},unregister:function(){
dojo.dnd.dragManager.unregisterDropTarget(this);
},onDragOver:function(evt){
},onDragOut:function(evt){
},onDragMove:function(evt){
},onDropStart:function(evt){
},onDrop:function(evt){
},onDropEnd:function(){
}},function(){
this.acceptedTypes=[];
});
dojo.dnd.DragEvent=function(){
this.dragSource=null;
this.dragObject=null;
this.target=null;
this.eventStatus="success";
};
dojo.declare("dojo.dnd.DragManager",null,{selectedSources:[],dragObjects:[],dragSources:[],registerDragSource:function(_9a0){
},dropTargets:[],registerDropTarget:function(_9a1){
},lastDragTarget:null,currentDragTarget:null,onKeyDown:function(){
},onMouseOut:function(){
},onMouseMove:function(){
},onMouseUp:function(){
}});
dojo.provide("dojo.dnd.HtmlDragManager");
dojo.declare("dojo.dnd.HtmlDragManager",dojo.dnd.DragManager,{disabled:false,nestedTargets:false,mouseDownTimer:null,dsCounter:0,dsPrefix:"dojoDragSource",dropTargetDimensions:[],currentDropTarget:null,previousDropTarget:null,_dragTriggered:false,selectedSources:[],dragObjects:[],dragSources:[],dropTargets:[],currentX:null,currentY:null,lastX:null,lastY:null,mouseDownX:null,mouseDownY:null,threshold:7,dropAcceptable:false,cancelEvent:function(e){
e.stopPropagation();
e.preventDefault();
},registerDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _9a5=dp+"Idx_"+(this.dsCounter++);
ds.dragSourceId=_9a5;
this.dragSources[_9a5]=ds;
ds.domNode.setAttribute(dp,_9a5);
if(dojo.render.html.ie){
dojo.event.browser.addListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},unregisterDragSource:function(ds){
if(ds["domNode"]){
var dp=this.dsPrefix;
var _9a8=ds.dragSourceId;
delete ds.dragSourceId;
delete this.dragSources[_9a8];
ds.domNode.setAttribute(dp,null);
if(dojo.render.html.ie){
dojo.event.browser.removeListener(ds.domNode,"ondragstart",this.cancelEvent);
}
}
},registerDropTarget:function(dt){
this.dropTargets.push(dt);
},unregisterDropTarget:function(dt){
var _9ab=dojo.lang.find(this.dropTargets,dt,true);
if(_9ab>=0){
this.dropTargets.splice(_9ab,1);
}
},getDragSource:function(e){
var tn=e.target;
if(tn===dojo.body()){
return;
}
var ta=dojo.html.getAttribute(tn,this.dsPrefix);
while((!ta)&&(tn)){
tn=tn.parentNode;
if((!tn)||(tn===dojo.body())){
return;
}
ta=dojo.html.getAttribute(tn,this.dsPrefix);
}
return this.dragSources[ta];
},onKeyDown:function(e){
},onMouseDown:function(e){
if(this.disabled){
return;
}
if(dojo.render.html.ie){
if(e.button!=1){
return;
}
}else{
if(e.which!=1){
return;
}
}
var _9b1=e.target.nodeType==dojo.html.TEXT_NODE?e.target.parentNode:e.target;
if(dojo.html.isTag(_9b1,"button","textarea","input","select","option")){
return;
}
var ds=this.getDragSource(e);
if(!ds){
return;
}
if(!dojo.lang.inArray(this.selectedSources,ds)){
this.selectedSources.push(ds);
ds.onSelected();
}
this.mouseDownX=e.pageX;
this.mouseDownY=e.pageY;
e.preventDefault();
dojo.event.connect(document,"onmousemove",this,"onMouseMove");
},onMouseUp:function(e,_9b4){
if(this.selectedSources.length==0){
return;
}
this.mouseDownX=null;
this.mouseDownY=null;
this._dragTriggered=false;
e.dragSource=this.dragSource;
if((!e.shiftKey)&&(!e.ctrlKey)){
if(this.currentDropTarget){
this.currentDropTarget.onDropStart();
}
dojo.lang.forEach(this.dragObjects,function(_9b5){
var ret=null;
if(!_9b5){
return;
}
if(this.currentDropTarget){
e.dragObject=_9b5;
var ce=this.currentDropTarget.domNode.childNodes;
if(ce.length>0){
e.dropTarget=ce[0];
while(e.dropTarget==_9b5.domNode){
e.dropTarget=e.dropTarget.nextSibling;
}
}else{
e.dropTarget=this.currentDropTarget.domNode;
}
if(this.dropAcceptable){
ret=this.currentDropTarget.onDrop(e);
}else{
this.currentDropTarget.onDragOut(e);
}
}
e.dragStatus=this.dropAcceptable&&ret?"dropSuccess":"dropFailure";
dojo.lang.delayThese([function(){
try{
_9b5.dragSource.onDragEnd(e);
}
catch(err){
var _9b8={};
for(var i in e){
if(i=="type"){
_9b8.type="mouseup";
continue;
}
_9b8[i]=e[i];
}
_9b5.dragSource.onDragEnd(_9b8);
}
},function(){
_9b5.onDragEnd(e);
}]);
},this);
this.selectedSources=[];
this.dragObjects=[];
this.dragSource=null;
if(this.currentDropTarget){
this.currentDropTarget.onDropEnd();
}
}else{
}
dojo.event.disconnect(document,"onmousemove",this,"onMouseMove");
this.currentDropTarget=null;
},onScroll:function(){
for(var i=0;i<this.dragObjects.length;i++){
if(this.dragObjects[i].updateDragOffset){
this.dragObjects[i].updateDragOffset();
}
}
if(this.dragObjects.length){
this.cacheTargetLocations();
}
},_dragStartDistance:function(x,y){
if((!this.mouseDownX)||(!this.mouseDownX)){
return;
}
var dx=Math.abs(x-this.mouseDownX);
var dx2=dx*dx;
var dy=Math.abs(y-this.mouseDownY);
var dy2=dy*dy;
return parseInt(Math.sqrt(dx2+dy2),10);
},cacheTargetLocations:function(){
dojo.profile.start("cacheTargetLocations");
this.dropTargetDimensions=[];
dojo.lang.forEach(this.dropTargets,function(_9c1){
var tn=_9c1.domNode;
if(!tn||!_9c1.accepts([this.dragSource])){
return;
}
var abs={x:0,y:0};
try{
abs=dojo.html.getAbsolutePosition(tn,true);
}
catch(e){
}
var bb=dojo.html.getBorderBox(tn);
this.dropTargetDimensions.push([[abs.x,abs.y],[abs.x+bb.width,abs.y+bb.height],_9c1]);
},this);
dojo.profile.end("cacheTargetLocations");
},onMouseMove:function(e){
if((dojo.render.html.ie)&&(e.button!=1)){
this.currentDropTarget=null;
this.onMouseUp(e,true);
return;
}
if((this.selectedSources.length)&&(!this.dragObjects.length)){
var dx;
var dy;
if(!this._dragTriggered){
this._dragTriggered=(this._dragStartDistance(e.pageX,e.pageY)>this.threshold);
if(!this._dragTriggered){
return;
}
dx=e.pageX-this.mouseDownX;
dy=e.pageY-this.mouseDownY;
}
this.dragSource=this.selectedSources[0];
dojo.lang.forEach(this.selectedSources,function(_9c8){
if(!_9c8){
return;
}
var tdo=_9c8.onDragStart(e);
if(tdo){
tdo.onDragStart(e);
tdo.dragOffset.y+=dy;
tdo.dragOffset.x+=dx;
tdo.dragSource=_9c8;
this.dragObjects.push(tdo);
}
},this);
this.previousDropTarget=null;
this.cacheTargetLocations();
}
dojo.lang.forEach(this.dragObjects,function(_9ca){
if(_9ca){
_9ca.onDragMove(e);
}
});
if(this.currentDropTarget){
var c=dojo.html.toCoordinateObject(this.currentDropTarget.domNode,true);
var dtp=[[c.x,c.y],[c.x+c.width,c.y+c.height]];
}
if((!this.nestedTargets)&&(dtp)&&(this.isInsideBox(e,dtp))){
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}else{
var _9cd=this.findBestTarget(e);
if(_9cd.target===null){
if(this.currentDropTarget){
this.currentDropTarget.onDragOut(e);
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget=null;
}
this.dropAcceptable=false;
return;
}
if(this.currentDropTarget!==_9cd.target){
if(this.currentDropTarget){
this.previousDropTarget=this.currentDropTarget;
this.currentDropTarget.onDragOut(e);
}
this.currentDropTarget=_9cd.target;
e.dragObjects=this.dragObjects;
this.dropAcceptable=this.currentDropTarget.onDragOver(e);
}else{
if(this.dropAcceptable){
this.currentDropTarget.onDragMove(e,this.dragObjects);
}
}
}
},findBestTarget:function(e){
var _9cf=this;
var _9d0=new Object();
_9d0.target=null;
_9d0.points=null;
dojo.lang.every(this.dropTargetDimensions,function(_9d1){
if(!_9cf.isInsideBox(e,_9d1)){
return true;
}
_9d0.target=_9d1[2];
_9d0.points=_9d1;
return Boolean(_9cf.nestedTargets);
});
return _9d0;
},isInsideBox:function(e,_9d3){
if((e.pageX>_9d3[0][0])&&(e.pageX<_9d3[1][0])&&(e.pageY>_9d3[0][1])&&(e.pageY<_9d3[1][1])){
return true;
}
return false;
},onMouseOver:function(e){
},onMouseOut:function(e){
}});
dojo.dnd.dragManager=new dojo.dnd.HtmlDragManager();
(function(){
var d=document;
var dm=dojo.dnd.dragManager;
dojo.event.connect(d,"onkeydown",dm,"onKeyDown");
dojo.event.connect(d,"onmouseover",dm,"onMouseOver");
dojo.event.connect(d,"onmouseout",dm,"onMouseOut");
dojo.event.connect(d,"onmousedown",dm,"onMouseDown");
dojo.event.connect(d,"onmouseup",dm,"onMouseUp");
dojo.event.connect(window,"onscroll",dm,"onScroll");
})();
dojo.provide("dojo.dnd.HtmlDragAndDrop");
dojo.declare("dojo.dnd.HtmlDragSource",dojo.dnd.DragSource,{dragClass:"",onDragStart:function(){
var _9d8=new dojo.dnd.HtmlDragObject(this.dragObject,this.type);
if(this.dragClass){
_9d8.dragClass=this.dragClass;
}
if(this.constrainToContainer){
_9d8.constrainTo(this.constrainingContainer||this.domNode.parentNode);
}
return _9d8;
},setDragHandle:function(node){
node=dojo.byId(node);
dojo.dnd.dragManager.unregisterDragSource(this);
this.domNode=node;
dojo.dnd.dragManager.registerDragSource(this);
},setDragTarget:function(node){
this.dragObject=node;
},constrainTo:function(_9db){
this.constrainToContainer=true;
if(_9db){
this.constrainingContainer=_9db;
}
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragSource(this.dragObjects[i]));
}
},addDragObjects:function(el){
for(var i=0;i<arguments.length;i++){
this.dragObjects.push(dojo.byId(arguments[i]));
}
}},function(node,type){
node=dojo.byId(node);
this.dragObjects=[];
this.constrainToContainer=false;
if(node){
this.domNode=node;
this.dragObject=node;
this.type=(type)||(this.domNode.nodeName.toLowerCase());
dojo.dnd.DragSource.prototype.reregister.call(this);
}
});
dojo.declare("dojo.dnd.HtmlDragObject",dojo.dnd.DragObject,{dragClass:"",opacity:0.5,createIframe:true,disableX:false,disableY:false,createDragNode:function(){
var node=this.domNode.cloneNode(true);
if(this.dragClass){
dojo.html.addClass(node,this.dragClass);
}
if(this.opacity<1){
dojo.html.setOpacity(node,this.opacity);
}
var ltn=node.tagName.toLowerCase();
var isTr=(ltn=="tr");
if((isTr)||(ltn=="tbody")){
var doc=this.domNode.ownerDocument;
var _9e5=doc.createElement("table");
if(isTr){
var _9e6=doc.createElement("tbody");
_9e5.appendChild(_9e6);
_9e6.appendChild(node);
}else{
_9e5.appendChild(node);
}
var _9e7=((isTr)?this.domNode:this.domNode.firstChild);
var _9e8=((isTr)?node:node.firstChild);
var _9e9=_9e7.childNodes;
var _9ea=_9e8.childNodes;
for(var i=0;i<_9e9.length;i++){
if((_9ea[i])&&(_9ea[i].style)){
_9ea[i].style.width=dojo.html.getContentBox(_9e9[i]).width+"px";
}
}
node=_9e5;
}
if((dojo.render.html.ie55||dojo.render.html.ie60)&&this.createIframe){
with(node.style){
top="0px";
left="0px";
}
var _9ec=document.createElement("div");
_9ec.appendChild(node);
this.bgIframe=new dojo.html.BackgroundIframe(_9ec);
_9ec.appendChild(this.bgIframe.iframe);
node=_9ec;
}
node.style.zIndex=999;
return node;
},onDragStart:function(e){
dojo.html.clearSelection();
this.scrollOffset=dojo.html.getScroll().offset;
this.dragStartPosition=dojo.html.getAbsolutePosition(this.domNode,true);
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.dragClone=this.createDragNode();
this.containingBlockPosition=this.domNode.offsetParent?dojo.html.getAbsolutePosition(this.domNode.offsetParent,true):{x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
with(this.dragClone.style){
position="absolute";
top=this.dragOffset.y+e.pageY+"px";
left=this.dragOffset.x+e.pageX+"px";
}
dojo.body().appendChild(this.dragClone);
dojo.event.topic.publish("dragStart",{source:this});
},getConstraints:function(){
if(this.constrainingContainer.nodeName.toLowerCase()=="body"){
var _9ee=dojo.html.getViewport();
var _9ef=_9ee.width;
var _9f0=_9ee.height;
var _9f1=dojo.html.getScroll().offset;
var x=_9f1.x;
var y=_9f1.y;
}else{
var _9f4=dojo.html.getContentBox(this.constrainingContainer);
_9ef=_9f4.width;
_9f0=_9f4.height;
x=this.containingBlockPosition.x+dojo.html.getPixelValue(this.constrainingContainer,"padding-left",true)+dojo.html.getBorderExtent(this.constrainingContainer,"left");
y=this.containingBlockPosition.y+dojo.html.getPixelValue(this.constrainingContainer,"padding-top",true)+dojo.html.getBorderExtent(this.constrainingContainer,"top");
}
var mb=dojo.html.getMarginBox(this.domNode);
return {minX:x,minY:y,maxX:x+_9ef-mb.width,maxY:y+_9f0-mb.height};
},updateDragOffset:function(){
var _9f6=dojo.html.getScroll().offset;
if(_9f6.y!=this.scrollOffset.y){
var diff=_9f6.y-this.scrollOffset.y;
this.dragOffset.y+=diff;
this.scrollOffset.y=_9f6.y;
}
if(_9f6.x!=this.scrollOffset.x){
var diff=_9f6.x-this.scrollOffset.x;
this.dragOffset.x+=diff;
this.scrollOffset.x=_9f6.x;
}
},onDragMove:function(e){
this.updateDragOffset();
var x=this.dragOffset.x+e.pageX;
var y=this.dragOffset.y+e.pageY;
if(this.constrainToContainer){
if(x<this.constraints.minX){
x=this.constraints.minX;
}
if(y<this.constraints.minY){
y=this.constraints.minY;
}
if(x>this.constraints.maxX){
x=this.constraints.maxX;
}
if(y>this.constraints.maxY){
y=this.constraints.maxY;
}
}
this.setAbsolutePosition(x,y);
dojo.event.topic.publish("dragMove",{source:this});
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.dragClone.style.top=y+"px";
}
if(!this.disableX){
this.dragClone.style.left=x+"px";
}
},onDragEnd:function(e){
switch(e.dragStatus){
case "dropSuccess":
dojo.html.removeNode(this.dragClone);
this.dragClone=null;
break;
case "dropFailure":
var _9fe=dojo.html.getAbsolutePosition(this.dragClone,true);
var _9ff={left:this.dragStartPosition.x+1,top:this.dragStartPosition.y+1};
var anim=dojo.lfx.slideTo(this.dragClone,_9ff,300);
var _a01=this;
dojo.event.connect(anim,"onEnd",function(e){
dojo.html.removeNode(_a01.dragClone);
_a01.dragClone=null;
});
anim.play();
break;
}
dojo.event.topic.publish("dragEnd",{source:this});
},constrainTo:function(_a03){
this.constrainToContainer=true;
if(_a03){
this.constrainingContainer=_a03;
}else{
this.constrainingContainer=this.domNode.parentNode;
}
}},function(node,type){
this.domNode=dojo.byId(node);
this.type=type;
this.constrainToContainer=false;
this.dragSource=null;
dojo.dnd.DragObject.prototype.register.call(this);
});
dojo.declare("dojo.dnd.HtmlDropTarget",dojo.dnd.DropTarget,{vertical:false,onDragOver:function(e){
if(!this.accepts(e.dragObjects)){
return false;
}
this.childBoxes=[];
for(var i=0,_a08;i<this.domNode.childNodes.length;i++){
_a08=this.domNode.childNodes[i];
if(_a08.nodeType!=dojo.html.ELEMENT_NODE){
continue;
}
var pos=dojo.html.getAbsolutePosition(_a08,true);
var _a0a=dojo.html.getBorderBox(_a08);
this.childBoxes.push({top:pos.y,bottom:pos.y+_a0a.height,left:pos.x,right:pos.x+_a0a.width,height:_a0a.height,width:_a0a.width,node:_a08});
}
return true;
},_getNodeUnderMouse:function(e){
for(var i=0,_a0d;i<this.childBoxes.length;i++){
with(this.childBoxes[i]){
if(e.pageX>=left&&e.pageX<=right&&e.pageY>=top&&e.pageY<=bottom){
return i;
}
}
}
return -1;
},createDropIndicator:function(){
this.dropIndicator=document.createElement("div");
with(this.dropIndicator.style){
position="absolute";
zIndex=999;
if(this.vertical){
borderLeftWidth="1px";
borderLeftColor="black";
borderLeftStyle="solid";
height=dojo.html.getBorderBox(this.domNode).height+"px";
top=dojo.html.getAbsolutePosition(this.domNode,true).y+"px";
}else{
borderTopWidth="1px";
borderTopColor="black";
borderTopStyle="solid";
width=dojo.html.getBorderBox(this.domNode).width+"px";
left=dojo.html.getAbsolutePosition(this.domNode,true).x+"px";
}
}
},onDragMove:function(e,_a0f){
var i=this._getNodeUnderMouse(e);
if(!this.dropIndicator){
this.createDropIndicator();
}
var _a11=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
var hide=false;
if(i<0){
if(this.childBoxes.length){
var _a13=(dojo.html.gravity(this.childBoxes[0].node,e)&_a11);
if(_a13){
hide=true;
}
}else{
var _a13=true;
}
}else{
var _a14=this.childBoxes[i];
var _a13=(dojo.html.gravity(_a14.node,e)&_a11);
if(_a14.node===_a0f[0].dragSource.domNode){
hide=true;
}else{
var _a15=_a13?(i>0?this.childBoxes[i-1]:_a14):(i<this.childBoxes.length-1?this.childBoxes[i+1]:_a14);
if(_a15.node===_a0f[0].dragSource.domNode){
hide=true;
}
}
}
if(hide){
this.dropIndicator.style.display="none";
return;
}else{
this.dropIndicator.style.display="";
}
this.placeIndicator(e,_a0f,i,_a13);
if(!dojo.html.hasParent(this.dropIndicator)){
dojo.body().appendChild(this.dropIndicator);
}
},placeIndicator:function(e,_a17,_a18,_a19){
var _a1a=this.vertical?"left":"top";
var _a1b;
if(_a18<0){
if(this.childBoxes.length){
_a1b=_a19?this.childBoxes[0]:this.childBoxes[this.childBoxes.length-1];
}else{
this.dropIndicator.style[_a1a]=dojo.html.getAbsolutePosition(this.domNode,true)[this.vertical?"x":"y"]+"px";
}
}else{
_a1b=this.childBoxes[_a18];
}
if(_a1b){
this.dropIndicator.style[_a1a]=(_a19?_a1b[_a1a]:_a1b[this.vertical?"right":"bottom"])+"px";
if(this.vertical){
this.dropIndicator.style.height=_a1b.height+"px";
this.dropIndicator.style.top=_a1b.top+"px";
}else{
this.dropIndicator.style.width=_a1b.width+"px";
this.dropIndicator.style.left=_a1b.left+"px";
}
}
},onDragOut:function(e){
if(this.dropIndicator){
dojo.html.removeNode(this.dropIndicator);
delete this.dropIndicator;
}
},onDrop:function(e){
this.onDragOut(e);
var i=this._getNodeUnderMouse(e);
var _a1f=this.vertical?dojo.html.gravity.WEST:dojo.html.gravity.NORTH;
if(i<0){
if(this.childBoxes.length){
if(dojo.html.gravity(this.childBoxes[0].node,e)&_a1f){
return this.insert(e,this.childBoxes[0].node,"before");
}else{
return this.insert(e,this.childBoxes[this.childBoxes.length-1].node,"after");
}
}
return this.insert(e,this.domNode,"append");
}
var _a20=this.childBoxes[i];
if(dojo.html.gravity(_a20.node,e)&_a1f){
return this.insert(e,_a20.node,"before");
}else{
return this.insert(e,_a20.node,"after");
}
},insert:function(e,_a22,_a23){
var node=e.dragObject.domNode;
if(_a23=="before"){
return dojo.html.insertBefore(node,_a22);
}else{
if(_a23=="after"){
return dojo.html.insertAfter(node,_a22);
}else{
if(_a23=="append"){
_a22.appendChild(node);
return true;
}
}
}
return false;
}},function(node,_a26){
if(arguments.length==0){
return;
}
this.domNode=dojo.byId(node);
dojo.dnd.DropTarget.call(this);
if(_a26&&dojo.lang.isString(_a26)){
_a26=[_a26];
}
this.acceptedTypes=_a26||[];
dojo.dnd.dragManager.registerDropTarget(this);
});
dojo.kwCompoundRequire({common:["dojo.dnd.DragAndDrop"],browser:["dojo.dnd.HtmlDragAndDrop"],dashboard:["dojo.dnd.HtmlDragAndDrop"]});
dojo.provide("dojo.dnd.*");
dojo.provide("dojo.dnd.HtmlDragMove");
dojo.declare("dojo.dnd.HtmlDragMoveSource",dojo.dnd.HtmlDragSource,{onDragStart:function(){
var _a27=new dojo.dnd.HtmlDragMoveObject(this.dragObject,this.type);
if(this.constrainToContainer){
_a27.constrainTo(this.constrainingContainer);
}
return _a27;
},onSelected:function(){
for(var i=0;i<this.dragObjects.length;i++){
dojo.dnd.dragManager.selectedSources.push(new dojo.dnd.HtmlDragMoveSource(this.dragObjects[i]));
}
}});
dojo.declare("dojo.dnd.HtmlDragMoveObject",dojo.dnd.HtmlDragObject,{onDragStart:function(e){
dojo.html.clearSelection();
this.dragClone=this.domNode;
if(dojo.html.getComputedStyle(this.domNode,"position")!="absolute"){
this.domNode.style.position="relative";
}
var left=parseInt(dojo.html.getComputedStyle(this.domNode,"left"));
var top=parseInt(dojo.html.getComputedStyle(this.domNode,"top"));
this.dragStartPosition={x:isNaN(left)?0:left,y:isNaN(top)?0:top};
this.scrollOffset=dojo.html.getScroll().offset;
this.dragOffset={y:this.dragStartPosition.y-e.pageY,x:this.dragStartPosition.x-e.pageX};
this.containingBlockPosition={x:0,y:0};
if(this.constrainToContainer){
this.constraints=this.getConstraints();
}
dojo.event.connect(this.domNode,"onclick",this,"_squelchOnClick");
},onDragEnd:function(e){
},setAbsolutePosition:function(x,y){
if(!this.disableY){
this.domNode.style.top=y+"px";
}
if(!this.disableX){
this.domNode.style.left=x+"px";
}
},_squelchOnClick:function(e){
dojo.event.browser.stopEvent(e);
dojo.event.disconnect(this.domNode,"onclick",this,"_squelchOnClick");
}});
dojo.provide("dojo.widget.ResizeHandle");
dojo.widget.defineWidget("dojo.widget.ResizeHandle",dojo.widget.HtmlWidget,{targetElmId:"",templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/ResizeHandle.css"),templateString:"<div class=\"dojoHtmlResizeHandle\"><div></div></div>",postCreate:function(){
dojo.event.connect(this.domNode,"onmousedown",this,"_beginSizing");
},_beginSizing:function(e){
if(this._isSizing){
return false;
}
this.targetWidget=dojo.widget.byId(this.targetElmId);
this.targetDomNode=this.targetWidget?this.targetWidget.domNode:dojo.byId(this.targetElmId);
if(!this.targetDomNode){
return;
}
this._isSizing=true;
this.startPoint={"x":e.clientX,"y":e.clientY};
var mb=dojo.html.getMarginBox(this.targetDomNode);
this.startSize={"w":mb.width,"h":mb.height};
dojo.event.kwConnect({srcObj:dojo.body(),srcFunc:"onmousemove",targetObj:this,targetFunc:"_changeSizing",rate:25});
dojo.event.connect(dojo.body(),"onmouseup",this,"_endSizing");
e.preventDefault();
},_changeSizing:function(e){
try{
if(!e.clientX||!e.clientY){
return;
}
}
catch(e){
return;
}
var dx=this.startPoint.x-e.clientX;
var dy=this.startPoint.y-e.clientY;
var newW=this.startSize.w-dx;
var newH=this.startSize.h-dy;
if(this.minSize){
var mb=dojo.html.getMarginBox(this.targetDomNode);
if(newW<this.minSize.w){
newW=mb.width;
}
if(newH<this.minSize.h){
newH=mb.height;
}
}
if(this.targetWidget){
this.targetWidget.resizeTo(newW,newH);
}else{
dojo.html.setMarginBox(this.targetDomNode,{width:newW,height:newH});
}
e.preventDefault();
},_endSizing:function(e){
dojo.event.disconnect(dojo.body(),"onmousemove",this,"_changeSizing");
dojo.event.disconnect(dojo.body(),"onmouseup",this,"_endSizing");
this._isSizing=false;
}});
dojo.provide("dojo.widget.FloatingPane");
dojo.declare("dojo.widget.FloatingPaneBase",null,{title:"",iconSrc:"",hasShadow:false,constrainToContainer:false,taskBarId:"",resizable:true,titleBarDisplay:true,windowState:"normal",displayCloseAction:false,displayMinimizeAction:false,displayMaximizeAction:false,_max_taskBarConnectAttempts:5,_taskBarConnectAttempts:0,templatePath:dojo.uri.moduleUri("dojo.widget","templates/FloatingPane.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/FloatingPane.css"),fillInFloatingPaneTemplate:function(args,frag){
var _a3b=this.getFragNodeRef(frag);
dojo.html.copyStyle(this.domNode,_a3b);
dojo.body().appendChild(this.domNode);
if(!this.isShowing()){
this.windowState="minimized";
}
if(this.iconSrc==""){
dojo.html.removeNode(this.titleBarIcon);
}else{
this.titleBarIcon.src=this.iconSrc.toString();
}
if(this.titleBarDisplay){
this.titleBar.style.display="";
dojo.html.disableSelection(this.titleBar);
this.titleBarIcon.style.display=(this.iconSrc==""?"none":"");
this.minimizeAction.style.display=(this.displayMinimizeAction?"":"none");
this.maximizeAction.style.display=(this.displayMaximizeAction&&this.windowState!="maximized"?"":"none");
this.restoreAction.style.display=(this.displayMaximizeAction&&this.windowState=="maximized"?"":"none");
this.closeAction.style.display=(this.displayCloseAction?"":"none");
this.drag=new dojo.dnd.HtmlDragMoveSource(this.domNode);
if(this.constrainToContainer){
this.drag.constrainTo();
}
this.drag.setDragHandle(this.titleBar);
var self=this;
dojo.event.topic.subscribe("dragMove",function(info){
if(info.source.domNode==self.domNode){
dojo.event.topic.publish("floatingPaneMove",{source:self});
}
});
}
if(this.resizable){
this.resizeBar.style.display="";
this.resizeHandle=dojo.widget.createWidget("ResizeHandle",{targetElmId:this.widgetId,id:this.widgetId+"_resize"});
this.resizeBar.appendChild(this.resizeHandle.domNode);
}
if(this.hasShadow){
this.shadow=new dojo.lfx.shadow(this.domNode);
}
this.bgIframe=new dojo.html.BackgroundIframe(this.domNode);
if(this.taskBarId){
this._taskBarSetup();
}
dojo.body().removeChild(this.domNode);
},postCreate:function(){
if(dojo.hostenv.post_load_){
this._setInitialWindowState();
}else{
dojo.addOnLoad(this,"_setInitialWindowState");
}
},maximizeWindow:function(evt){
var mb=dojo.html.getMarginBox(this.domNode);
this.previous={width:mb.width||this.width,height:mb.height||this.height,left:this.domNode.style.left,top:this.domNode.style.top,bottom:this.domNode.style.bottom,right:this.domNode.style.right};
if(this.domNode.parentNode.style.overflow.toLowerCase()!="hidden"){
this.parentPrevious={overflow:this.domNode.parentNode.style.overflow};
dojo.debug(this.domNode.parentNode.style.overflow);
this.domNode.parentNode.style.overflow="hidden";
}
this.domNode.style.left=dojo.html.getPixelValue(this.domNode.parentNode,"padding-left",true)+"px";
this.domNode.style.top=dojo.html.getPixelValue(this.domNode.parentNode,"padding-top",true)+"px";
if((this.domNode.parentNode.nodeName.toLowerCase()=="body")){
var _a40=dojo.html.getViewport();
var _a41=dojo.html.getPadding(dojo.body());
this.resizeTo(_a40.width-_a41.width,_a40.height-_a41.height);
}else{
var _a42=dojo.html.getContentBox(this.domNode.parentNode);
this.resizeTo(_a42.width,_a42.height);
}
this.maximizeAction.style.display="none";
this.restoreAction.style.display="";
if(this.resizeHandle){
this.resizeHandle.domNode.style.display="none";
}
this.drag.setDragHandle(null);
this.windowState="maximized";
},minimizeWindow:function(evt){
this.hide();
for(var attr in this.parentPrevious){
this.domNode.parentNode.style[attr]=this.parentPrevious[attr];
}
this.lastWindowState=this.windowState;
this.windowState="minimized";
},restoreWindow:function(evt){
if(this.windowState=="minimized"){
this.show();
if(this.lastWindowState=="maximized"){
this.domNode.parentNode.style.overflow="hidden";
this.windowState="maximized";
}else{
this.windowState="normal";
}
}else{
if(this.windowState=="maximized"){
for(var attr in this.previous){
this.domNode.style[attr]=this.previous[attr];
}
for(var attr in this.parentPrevious){
this.domNode.parentNode.style[attr]=this.parentPrevious[attr];
}
this.resizeTo(this.previous.width,this.previous.height);
this.previous=null;
this.parentPrevious=null;
this.restoreAction.style.display="none";
this.maximizeAction.style.display=this.displayMaximizeAction?"":"none";
if(this.resizeHandle){
this.resizeHandle.domNode.style.display="";
}
this.drag.setDragHandle(this.titleBar);
this.windowState="normal";
}else{
}
}
},toggleDisplay:function(){
if(this.windowState=="minimized"){
this.restoreWindow();
}else{
this.minimizeWindow();
}
},closeWindow:function(evt){
dojo.html.removeNode(this.domNode);
this.destroy();
},onMouseDown:function(evt){
this.bringToTop();
},bringToTop:function(){
var _a49=dojo.widget.manager.getWidgetsByType(this.widgetType);
var _a4a=[];
for(var x=0;x<_a49.length;x++){
if(this.widgetId!=_a49[x].widgetId){
_a4a.push(_a49[x]);
}
}
_a4a.sort(function(a,b){
return a.domNode.style.zIndex-b.domNode.style.zIndex;
});
_a4a.push(this);
var _a4e=100;
for(x=0;x<_a4a.length;x++){
_a4a[x].domNode.style.zIndex=_a4e+x*2;
}
},_setInitialWindowState:function(){
if(this.isShowing()){
this.width=-1;
var mb=dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width,mb.height);
}
if(this.windowState=="maximized"){
this.maximizeWindow();
this.show();
return;
}
if(this.windowState=="normal"){
this.show();
return;
}
if(this.windowState=="minimized"){
this.hide();
return;
}
this.windowState="minimized";
},_taskBarSetup:function(){
var _a50=dojo.widget.getWidgetById(this.taskBarId);
if(!_a50){
if(this._taskBarConnectAttempts<this._max_taskBarConnectAttempts){
dojo.lang.setTimeout(this,this._taskBarSetup,50);
this._taskBarConnectAttempts++;
}else{
dojo.debug("Unable to connect to the taskBar");
}
return;
}
_a50.addChild(this);
},showFloatingPane:function(){
this.bringToTop();
},onFloatingPaneShow:function(){
var mb=dojo.html.getMarginBox(this.domNode);
this.resizeTo(mb.width,mb.height);
},resizeTo:function(_a52,_a53){
dojo.html.setMarginBox(this.domNode,{width:_a52,height:_a53});
dojo.widget.html.layout(this.domNode,[{domNode:this.titleBar,layoutAlign:"top"},{domNode:this.resizeBar,layoutAlign:"bottom"},{domNode:this.containerNode,layoutAlign:"client"}]);
dojo.widget.html.layout(this.containerNode,this.children,"top-bottom");
this.bgIframe.onResized();
if(this.shadow){
this.shadow.size(_a52,_a53);
}
this.onResized();
},checkSize:function(){
},destroyFloatingPane:function(){
if(this.resizeHandle){
this.resizeHandle.destroy();
this.resizeHandle=null;
}
}});
dojo.widget.defineWidget("dojo.widget.FloatingPane",[dojo.widget.ContentPane,dojo.widget.FloatingPaneBase],{fillInTemplate:function(args,frag){
this.fillInFloatingPaneTemplate(args,frag);
dojo.widget.FloatingPane.superclass.fillInTemplate.call(this,args,frag);
},postCreate:function(){
dojo.widget.FloatingPaneBase.prototype.postCreate.apply(this,arguments);
dojo.widget.FloatingPane.superclass.postCreate.apply(this,arguments);
},show:function(){
dojo.widget.FloatingPane.superclass.show.apply(this,arguments);
this.showFloatingPane();
},onShow:function(){
dojo.widget.FloatingPane.superclass.onShow.call(this);
this.onFloatingPaneShow();
},destroy:function(){
this.destroyFloatingPane();
dojo.widget.FloatingPane.superclass.destroy.apply(this,arguments);
}});
dojo.widget.defineWidget("dojo.widget.ModalFloatingPane",[dojo.widget.FloatingPane,dojo.widget.ModalDialogBase],{windowState:"minimized",displayCloseAction:true,postCreate:function(){
dojo.widget.ModalDialogBase.prototype.postCreate.call(this);
dojo.widget.ModalFloatingPane.superclass.postCreate.call(this);
},show:function(){
this.showModalDialog();
dojo.widget.ModalFloatingPane.superclass.show.apply(this,arguments);
this.bg.style.zIndex=this.domNode.style.zIndex-1;
},hide:function(){
this.hideModalDialog();
dojo.widget.ModalFloatingPane.superclass.hide.apply(this,arguments);
},closeWindow:function(){
this.hide();
dojo.widget.ModalFloatingPane.superclass.closeWindow.apply(this,arguments);
}});
dojo.provide("dojo.widget.PopupContainer");
dojo.declare("dojo.widget.PopupContainerBase",null,function(){
this.queueOnAnimationFinish=[];
},{isShowingNow:false,currentSubpopup:null,beginZIndex:1000,parentPopup:null,parent:null,popupIndex:0,aroundBox:dojo.html.boxSizing.BORDER_BOX,openedForWindow:null,processKey:function(evt){
return false;
},applyPopupBasicStyle:function(){
with(this.domNode.style){
display="none";
position="absolute";
}
},aboutToShow:function(){
},open:function(x,y,_a59,_a5a,_a5b,_a5c){
if(this.isShowingNow){
return;
}
if(this.animationInProgress){
this.queueOnAnimationFinish.push(this.open,arguments);
return;
}
this.aboutToShow();
var _a5d=false,node,_a5f;
if(typeof x=="object"){
node=x;
_a5f=_a5a;
_a5a=_a59;
_a59=y;
_a5d=true;
}
this.parent=_a59;
dojo.body().appendChild(this.domNode);
_a5a=_a5a||_a59["domNode"]||[];
var _a60=null;
this.isTopLevel=true;
while(_a59){
if(_a59!==this&&(_a59.setOpenedSubpopup!=undefined&&_a59.applyPopupBasicStyle!=undefined)){
_a60=_a59;
this.isTopLevel=false;
_a60.setOpenedSubpopup(this);
break;
}
_a59=_a59.parent;
}
this.parentPopup=_a60;
this.popupIndex=_a60?_a60.popupIndex+1:1;
if(this.isTopLevel){
var _a61=dojo.html.isNode(_a5a)?_a5a:null;
dojo.widget.PopupManager.opened(this,_a61);
}
if(this.isTopLevel&&!dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.isCollapsed)){
this._bookmark=dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.getBookmark);
}else{
this._bookmark=null;
}
if(_a5a instanceof Array){
_a5a={left:_a5a[0],top:_a5a[1],width:0,height:0};
}
with(this.domNode.style){
display="";
zIndex=this.beginZIndex+this.popupIndex;
}
if(_a5d){
this.move(node,_a5c,_a5f);
}else{
this.move(x,y,_a5c,_a5b);
}
this.domNode.style.display="none";
this.explodeSrc=_a5a;
this.show();
this.isShowingNow=true;
},move:function(x,y,_a64,_a65){
var _a66=(typeof x=="object");
if(_a66){
var _a67=_a64;
var node=x;
_a64=y;
if(!_a67){
_a67={"BL":"TL","TL":"BL"};
}
dojo.html.placeOnScreenAroundElement(this.domNode,node,_a64,this.aroundBox,_a67);
}else{
if(!_a65){
_a65="TL,TR,BL,BR";
}
dojo.html.placeOnScreen(this.domNode,x,y,_a64,true,_a65);
}
},close:function(_a69){
if(_a69){
this.domNode.style.display="none";
}
if(this.animationInProgress){
this.queueOnAnimationFinish.push(this.close,[]);
return;
}
this.closeSubpopup(_a69);
this.hide();
if(this.bgIframe){
this.bgIframe.hide();
this.bgIframe.size({left:0,top:0,width:0,height:0});
}
if(this.isTopLevel){
dojo.widget.PopupManager.closed(this);
}
this.isShowingNow=false;
if(this.parent){
setTimeout(dojo.lang.hitch(this,function(){
try{
if(this.parent["focus"]){
this.parent.focus();
}else{
this.parent.domNode.focus();
}
}
catch(e){
dojo.debug("No idea how to focus to parent",e);
}
}),10);
}
if(this._bookmark&&dojo.withGlobal(this.openedForWindow||dojo.global(),dojo.html.selection.isCollapsed)){
if(this.openedForWindow){
this.openedForWindow.focus();
}
try{
dojo.withGlobal(this.openedForWindow||dojo.global(),"moveToBookmark",dojo.html.selection,[this._bookmark]);
}
catch(e){
}
}
this._bookmark=null;
},closeAll:function(_a6a){
if(this.parentPopup){
this.parentPopup.closeAll(_a6a);
}else{
this.close(_a6a);
}
},setOpenedSubpopup:function(_a6b){
this.currentSubpopup=_a6b;
},closeSubpopup:function(_a6c){
if(this.currentSubpopup==null){
return;
}
this.currentSubpopup.close(_a6c);
this.currentSubpopup=null;
},onShow:function(){
dojo.widget.PopupContainer.superclass.onShow.apply(this,arguments);
this.openedSize={w:this.domNode.style.width,h:this.domNode.style.height};
if(dojo.render.html.ie){
if(!this.bgIframe){
this.bgIframe=new dojo.html.BackgroundIframe();
this.bgIframe.setZIndex(this.domNode);
}
this.bgIframe.size(this.domNode);
this.bgIframe.show();
}
this.processQueue();
},processQueue:function(){
if(!this.queueOnAnimationFinish.length){
return;
}
var func=this.queueOnAnimationFinish.shift();
var args=this.queueOnAnimationFinish.shift();
func.apply(this,args);
},onHide:function(){
dojo.widget.HtmlWidget.prototype.onHide.call(this);
if(this.openedSize){
with(this.domNode.style){
width=this.openedSize.w;
height=this.openedSize.h;
}
}
this.processQueue();
}});
dojo.widget.defineWidget("dojo.widget.PopupContainer",[dojo.widget.HtmlWidget,dojo.widget.PopupContainerBase],{isContainer:true,fillInTemplate:function(){
this.applyPopupBasicStyle();
dojo.widget.PopupContainer.superclass.fillInTemplate.apply(this,arguments);
}});
dojo.widget.PopupManager=new function(){
this.currentMenu=null;
this.currentButton=null;
this.currentFocusMenu=null;
this.focusNode=null;
this.registeredWindows=[];
this.registerWin=function(win){
if(!win.__PopupManagerRegistered){
dojo.event.connect(win.document,"onmousedown",this,"onClick");
dojo.event.connect(win,"onscroll",this,"onClick");
dojo.event.connect(win.document,"onkey",this,"onKey");
win.__PopupManagerRegistered=true;
this.registeredWindows.push(win);
}
};
this.registerAllWindows=function(_a70){
if(!_a70){
_a70=dojo.html.getDocumentWindow(window.top&&window.top.document||window.document);
}
this.registerWin(_a70);
for(var i=0;i<_a70.frames.length;i++){
try{
var win=dojo.html.getDocumentWindow(_a70.frames[i].document);
if(win){
this.registerAllWindows(win);
}
}
catch(e){
}
}
};
this.unRegisterWin=function(win){
if(win.__PopupManagerRegistered){
dojo.event.disconnect(win.document,"onmousedown",this,"onClick");
dojo.event.disconnect(win,"onscroll",this,"onClick");
dojo.event.disconnect(win.document,"onkey",this,"onKey");
win.__PopupManagerRegistered=false;
}
};
this.unRegisterAllWindows=function(){
for(var i=0;i<this.registeredWindows.length;++i){
this.unRegisterWin(this.registeredWindows[i]);
}
this.registeredWindows=[];
};
dojo.addOnLoad(this,"registerAllWindows");
dojo.addOnUnload(this,"unRegisterAllWindows");
this.closed=function(menu){
if(this.currentMenu==menu){
this.currentMenu=null;
this.currentButton=null;
this.currentFocusMenu=null;
}
};
this.opened=function(menu,_a77){
if(menu==this.currentMenu){
return;
}
if(this.currentMenu){
this.currentMenu.close();
}
this.currentMenu=menu;
this.currentFocusMenu=menu;
this.currentButton=_a77;
};
this.setFocusedMenu=function(menu){
this.currentFocusMenu=menu;
};
this.onKey=function(e){
if(!e.key){
return;
}
if(!this.currentMenu||!this.currentMenu.isShowingNow){
return;
}
var m=this.currentFocusMenu;
while(m){
if(m.processKey(e)){
e.preventDefault();
e.stopPropagation();
break;
}
m=m.parentPopup||m.parentMenu;
}
},this.onClick=function(e){
if(!this.currentMenu){
return;
}
var _a7c=dojo.html.getScroll().offset;
var m=this.currentMenu;
while(m){
if(dojo.html.overElement(m.domNode,e)||dojo.html.isDescendantOf(e.target,m.domNode)){
return;
}
m=m.currentSubpopup;
}
if(this.currentButton&&dojo.html.overElement(this.currentButton,e)){
return;
}
this.currentMenu.closeAll(true);
};
};
dojo.provide("dojo.widget.Menu2");
dojo.declare("dojo.widget.MenuBase",null,function(){
this.eventNames={open:""};
},{isContainer:true,isMenu:true,eventNaming:"default",templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/Menu2.css"),submenuDelay:500,initialize:function(args,frag){
if(this.eventNaming=="default"){
for(var _a80 in this.eventNames){
this.eventNames[_a80]=this.widgetId+"/"+_a80;
}
}
},_moveToNext:function(evt){
this._highlightOption(1);
return true;
},_moveToPrevious:function(evt){
this._highlightOption(-1);
return true;
},_moveToParentMenu:function(evt){
if(this._highlighted_option&&this.parentMenu){
if(evt._menu2UpKeyProcessed){
return true;
}else{
this._highlighted_option.onUnhover();
this.closeSubmenu();
evt._menu2UpKeyProcessed=true;
}
}
return false;
},_moveToChildMenu:function(evt){
if(this._highlighted_option&&this._highlighted_option.submenuId){
this._highlighted_option._onClick(true);
return true;
}
return false;
},_selectCurrentItem:function(evt){
if(this._highlighted_option){
this._highlighted_option._onClick();
return true;
}
return false;
},processKey:function(evt){
if(evt.ctrlKey||evt.altKey||!evt.key){
return false;
}
var rval=false;
switch(evt.key){
case evt.KEY_DOWN_ARROW:
rval=this._moveToNext(evt);
break;
case evt.KEY_UP_ARROW:
rval=this._moveToPrevious(evt);
break;
case evt.KEY_RIGHT_ARROW:
rval=this._moveToChildMenu(evt);
break;
case evt.KEY_LEFT_ARROW:
rval=this._moveToParentMenu(evt);
break;
case " ":
case evt.KEY_ENTER:
if(rval=this._selectCurrentItem(evt)){
break;
}
case evt.KEY_ESCAPE:
case evt.KEY_TAB:
this.close(true);
rval=true;
break;
}
return rval;
},_findValidItem:function(dir,_a89){
if(_a89){
_a89=dir>0?_a89.getNextSibling():_a89.getPreviousSibling();
}
for(var i=0;i<this.children.length;++i){
if(!_a89){
_a89=dir>0?this.children[0]:this.children[this.children.length-1];
}
if(_a89.onHover&&_a89.isShowing()){
return _a89;
}
_a89=dir>0?_a89.getNextSibling():_a89.getPreviousSibling();
}
},_highlightOption:function(dir){
var item;
if((!this._highlighted_option)){
item=this._findValidItem(dir);
}else{
item=this._findValidItem(dir,this._highlighted_option);
}
if(item){
if(this._highlighted_option){
this._highlighted_option.onUnhover();
}
item.onHover();
dojo.html.scrollIntoView(item.domNode);
try{
var node=dojo.html.getElementsByClass("dojoMenuItem2Label",item.domNode)[0];
node.focus();
}
catch(e){
}
}
},onItemClick:function(item){
},closeSubmenu:function(_a8f){
if(this.currentSubmenu==null){
return;
}
this.currentSubmenu.close(_a8f);
this.currentSubmenu=null;
this.currentSubmenuTrigger.is_open=false;
this.currentSubmenuTrigger._closedSubmenu(_a8f);
this.currentSubmenuTrigger=null;
}});
dojo.widget.defineWidget("dojo.widget.PopupMenu2",[dojo.widget.HtmlWidget,dojo.widget.PopupContainerBase,dojo.widget.MenuBase],function(){
this.targetNodeIds=[];
},{templateString:"<table class=\"dojoPopupMenu2\" border=0 cellspacing=0 cellpadding=0 style=\"display: none; position: absolute;\">"+"<tbody dojoAttachPoint=\"containerNode\"></tbody>"+"</table>",submenuOverlap:5,contextMenuForWindow:false,parentMenu:null,postCreate:function(){
if(this.contextMenuForWindow){
var doc=dojo.body();
this.bindDomNode(doc);
}else{
if(this.targetNodeIds.length>0){
dojo.lang.forEach(this.targetNodeIds,this.bindDomNode,this);
}
}
this._subscribeSubitemsOnOpen();
},_subscribeSubitemsOnOpen:function(){
var _a91=this.getChildrenOfType(dojo.widget.MenuItem2);
for(var i=0;i<_a91.length;i++){
dojo.event.topic.subscribe(this.eventNames.open,_a91[i],"menuOpen");
}
},getTopOpenEvent:function(){
var menu=this;
while(menu.parentMenu){
menu=menu.parentMenu;
}
return menu.openEvent;
},bindDomNode:function(node){
node=dojo.byId(node);
var win=dojo.html.getElementWindow(node);
if(dojo.html.isTag(node,"iframe")=="iframe"){
win=dojo.html.iframeContentWindow(node);
node=dojo.withGlobal(win,dojo.body);
}
dojo.widget.Menu2.OperaAndKonqFixer.fixNode(node);
dojo.event.kwConnect({srcObj:node,srcFunc:"oncontextmenu",targetObj:this,targetFunc:"onOpen",once:true});
if(dojo.render.html.moz&&win.document.designMode.toLowerCase()=="on"){
dojo.event.browser.addListener(node,"contextmenu",dojo.lang.hitch(this,"onOpen"));
}
dojo.widget.PopupManager.registerWin(win);
},unBindDomNode:function(_a96){
var node=dojo.byId(_a96);
dojo.event.kwDisconnect({srcObj:node,srcFunc:"oncontextmenu",targetObj:this,targetFunc:"onOpen",once:true});
dojo.widget.Menu2.OperaAndKonqFixer.cleanNode(node);
},_openAsSubmenu:function(_a98,_a99,_a9a){
if(this.isShowingNow){
return;
}
this.parentMenu=_a98;
this.open(_a99,_a98,_a99,_a9a);
},close:function(_a9b){
if(this.animationInProgress){
dojo.widget.PopupContainerBase.prototype.close.call(this,_a9b);
return;
}
if(this._highlighted_option){
this._highlighted_option.onUnhover();
}
dojo.widget.PopupContainerBase.prototype.close.call(this,_a9b);
this.parentMenu=null;
},closeAll:function(_a9c){
if(this.parentMenu){
this.parentMenu.closeAll(_a9c);
}else{
this.close(_a9c);
}
},_openSubmenu:function(_a9d,_a9e){
_a9d._openAsSubmenu(this,_a9e.arrow,{"TR":"TL","TL":"TR"});
this.currentSubmenu=_a9d;
this.currentSubmenuTrigger=_a9e;
this.currentSubmenuTrigger.is_open=true;
},focus:function(){
if(this.currentSubmenuTrigger){
if(this.currentSubmenuTrigger.caption){
try{
this.currentSubmenuTrigger.caption.focus();
}
catch(e){
}
}else{
try{
this.currentSubmenuTrigger.domNode.focus();
}
catch(e){
}
}
}
},onOpen:function(e){
this.openEvent=e;
if(e["target"]){
this.openedForWindow=dojo.html.getElementWindow(e.target);
}else{
this.openedForWindow=null;
}
var x=e.pageX,y=e.pageY;
var win=dojo.html.getElementWindow(e.target);
var _aa3=win._frameElement||win.frameElement;
if(_aa3){
var cood=dojo.html.abs(_aa3,true);
x+=cood.x-dojo.withGlobal(win,dojo.html.getScroll).left;
y+=cood.y-dojo.withGlobal(win,dojo.html.getScroll).top;
}
this.open(x,y,null,[x,y]);
dojo.event.browser.stopEvent(e);
}});
dojo.widget.defineWidget("dojo.widget.MenuItem2",dojo.widget.HtmlWidget,function(){
this.eventNames={engage:""};
},{templateString:"<tr class=\"dojoMenuItem2\" dojoAttachEvent=\"onMouseOver: onHover; onMouseOut: onUnhover; onClick: _onClick; onKey:onKey;\">"+"<td><div class=\"${this.iconClass}\" style=\"${this.iconStyle}\"></div></td>"+"<td tabIndex=\"-1\" class=\"dojoMenuItem2Label\" dojoAttachPoint=\"caption\">${this.caption}</td>"+"<td class=\"dojoMenuItem2Accel\">${this.accelKey}</td>"+"<td><div class=\"dojoMenuItem2Submenu\" style=\"display:${this.arrowDisplay};\" dojoAttachPoint=\"arrow\"></div></td>"+"</tr>",is_hovering:false,hover_timer:null,is_open:false,topPosition:0,caption:"Untitled",accelKey:"",iconSrc:"",disabledClass:"dojoMenuItem2Disabled",iconClass:"dojoMenuItem2Icon",submenuId:"",eventNaming:"default",highlightClass:"dojoMenuItem2Hover",postMixInProperties:function(){
this.iconStyle="";
if(this.iconSrc){
if((this.iconSrc.toLowerCase().substring(this.iconSrc.length-4)==".png")&&(dojo.render.html.ie55||dojo.render.html.ie60)){
this.iconStyle="filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+this.iconSrc+"', sizingMethod='image')";
}else{
this.iconStyle="background-image: url("+this.iconSrc+")";
}
}
this.arrowDisplay=this.submenuId?"block":"none";
dojo.widget.MenuItem2.superclass.postMixInProperties.apply(this,arguments);
},fillInTemplate:function(){
dojo.html.disableSelection(this.domNode);
if(this.disabled){
this.setDisabled(true);
}
if(this.eventNaming=="default"){
for(var _aa5 in this.eventNames){
this.eventNames[_aa5]=this.widgetId+"/"+_aa5;
}
}
},onHover:function(){
this.onUnhover();
if(this.is_hovering){
return;
}
if(this.is_open){
return;
}
if(this.parent._highlighted_option){
this.parent._highlighted_option.onUnhover();
}
this.parent.closeSubmenu();
this.parent._highlighted_option=this;
dojo.widget.PopupManager.setFocusedMenu(this.parent);
this._highlightItem();
if(this.is_hovering){
this._stopSubmenuTimer();
}
this.is_hovering=true;
this._startSubmenuTimer();
},onUnhover:function(){
if(!this.is_open){
this._unhighlightItem();
}
this.is_hovering=false;
this.parent._highlighted_option=null;
if(this.parent.parentMenu){
dojo.widget.PopupManager.setFocusedMenu(this.parent.parentMenu);
}
this._stopSubmenuTimer();
},_onClick:function(_aa6){
var _aa7=false;
if(this.disabled){
return false;
}
if(this.submenuId){
if(!this.is_open){
this._stopSubmenuTimer();
this._openSubmenu();
}
_aa7=true;
}else{
this.onUnhover();
this.parent.closeAll(true);
}
this.onClick();
dojo.event.topic.publish(this.eventNames.engage,this);
if(_aa7&&_aa6){
dojo.widget.getWidgetById(this.submenuId)._highlightOption(1);
}
return;
},onClick:function(){
this.parent.onItemClick(this);
},_highlightItem:function(){
dojo.html.addClass(this.domNode,this.highlightClass);
},_unhighlightItem:function(){
dojo.html.removeClass(this.domNode,this.highlightClass);
},_startSubmenuTimer:function(){
this._stopSubmenuTimer();
if(this.disabled){
return;
}
var self=this;
var _aa9=function(){
return function(){
self._openSubmenu();
};
}();
this.hover_timer=dojo.lang.setTimeout(_aa9,this.parent.submenuDelay);
},_stopSubmenuTimer:function(){
if(this.hover_timer){
dojo.lang.clearTimeout(this.hover_timer);
this.hover_timer=null;
}
},_openSubmenu:function(){
if(this.disabled){
return;
}
this.parent.closeSubmenu();
var _aaa=dojo.widget.getWidgetById(this.submenuId);
if(_aaa){
this.parent._openSubmenu(_aaa,this);
}
},_closedSubmenu:function(){
this.onUnhover();
},setDisabled:function(_aab){
this.disabled=_aab;
if(this.disabled){
dojo.html.addClass(this.domNode,this.disabledClass);
}else{
dojo.html.removeClass(this.domNode,this.disabledClass);
}
},enable:function(){
this.setDisabled(false);
},disable:function(){
this.setDisabled(true);
},menuOpen:function(_aac){
}});
dojo.widget.defineWidget("dojo.widget.MenuSeparator2",dojo.widget.HtmlWidget,{templateString:"<tr class=\"dojoMenuSeparator2\"><td colspan=4>"+"<div class=\"dojoMenuSeparator2Top\"></div>"+"<div class=\"dojoMenuSeparator2Bottom\"></div>"+"</td></tr>",postCreate:function(){
dojo.html.disableSelection(this.domNode);
}});
dojo.widget.defineWidget("dojo.widget.MenuBar2",[dojo.widget.HtmlWidget,dojo.widget.MenuBase],{menuOverlap:2,templateString:"<div class=\"dojoMenuBar2\" dojoAttachPoint=\"containerNode\" tabIndex=\"0\"></div>",close:function(_aad){
if(this._highlighted_option){
this._highlighted_option.onUnhover();
}
this.closeSubmenu(_aad);
},closeAll:function(_aae){
this.close(_aae);
},processKey:function(evt){
if(evt.ctrlKey||evt.altKey){
return false;
}
var rval=false;
switch(evt.key){
case evt.KEY_DOWN_ARROW:
rval=this._moveToChildMenu(evt);
break;
case evt.KEY_UP_ARROW:
rval=this._moveToParentMenu(evt);
break;
case evt.KEY_RIGHT_ARROW:
rval=this._moveToNext(evt);
break;
case evt.KEY_LEFT_ARROW:
rval=this._moveToPrevious(evt);
break;
default:
rval=dojo.widget.MenuBar2.superclass.processKey.apply(this,arguments);
break;
}
return rval;
},postCreate:function(){
dojo.widget.MenuBar2.superclass.postCreate.apply(this,arguments);
this.isShowingNow=true;
},_openSubmenu:function(_ab1,_ab2){
_ab1._openAsSubmenu(this,_ab2.domNode,{"BL":"TL","TL":"BL"});
this.currentSubmenu=_ab1;
this.currentSubmenuTrigger=_ab2;
this.currentSubmenuTrigger.is_open=true;
}});
dojo.widget.defineWidget("dojo.widget.MenuBarItem2",dojo.widget.MenuItem2,{templateString:"<span class=\"dojoMenuItem2\" dojoAttachEvent=\"onMouseOver: onHover; onMouseOut: onUnhover; onClick: _onClick;\">${this.caption}</span>"});
dojo.widget.Menu2.OperaAndKonqFixer=new function(){
var _ab3=true;
var _ab4=false;
if(!dojo.lang.isFunction(dojo.doc().oncontextmenu)){
dojo.doc().oncontextmenu=function(){
_ab3=false;
_ab4=true;
};
}
if(dojo.doc().createEvent){
try{
var e=dojo.doc().createEvent("MouseEvents");
e.initMouseEvent("contextmenu",1,1,dojo.global(),1,0,0,0,0,0,0,0,0,0,null);
dojo.doc().dispatchEvent(e);
}
catch(e){
}
}else{
_ab3=false;
}
if(_ab4){
delete dojo.doc().oncontextmenu;
}
this.fixNode=function(node){
if(_ab3){
if(!dojo.lang.isFunction(node.oncontextmenu)){
node.oncontextmenu=function(e){
};
}
if(dojo.render.html.opera){
node._menufixer_opera=function(e){
if(e.ctrlKey){
this.oncontextmenu(e);
}
};
dojo.event.connect(node,"onclick",node,"_menufixer_opera");
}else{
node._menufixer_konq=function(e){
if(e.button==2){
e.preventDefault();
this.oncontextmenu(e);
}
};
dojo.event.connect(node,"onmousedown",node,"_menufixer_konq");
}
}
};
this.cleanNode=function(node){
if(_ab3){
if(node._menufixer_opera){
dojo.event.disconnect(node,"onclick",node,"_menufixer_opera");
delete node._menufixer_opera;
}else{
if(node._menufixer_konq){
dojo.event.disconnect(node,"onmousedown",node,"_menufixer_konq");
delete node._menufixer_konq;
}
}
if(node.oncontextmenu){
delete node.oncontextmenu;
}
}
};
};
dojo.provide("dojo.widget.SplitContainer");
dojo.widget.defineWidget("dojo.widget.SplitContainer",dojo.widget.HtmlWidget,function(){
this.sizers=[];
},{isContainer:true,templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/SplitContainer.css"),activeSizing:false,sizerWidth:15,orientation:"horizontal",persist:true,postMixInProperties:function(){
dojo.widget.SplitContainer.superclass.postMixInProperties.apply(this,arguments);
this.isHorizontal=(this.orientation=="horizontal");
},fillInTemplate:function(){
dojo.widget.SplitContainer.superclass.fillInTemplate.apply(this,arguments);
dojo.html.addClass(this.domNode,"dojoSplitContainer");
if(dojo.render.html.moz){
this.domNode.style.overflow="-moz-scrollbars-none";
}
var _abb=dojo.html.getContentBox(this.domNode);
this.paneWidth=_abb.width;
this.paneHeight=_abb.height;
},onResized:function(e){
var _abd=dojo.html.getContentBox(this.domNode);
this.paneWidth=_abd.width;
this.paneHeight=_abd.height;
this._layoutPanels();
},postCreate:function(args,_abf,_ac0){
dojo.widget.SplitContainer.superclass.postCreate.apply(this,arguments);
for(var i=0;i<this.children.length;i++){
with(this.children[i].domNode.style){
position="absolute";
}
dojo.html.addClass(this.children[i].domNode,"dojoSplitPane");
if(i==this.children.length-1){
break;
}
this._addSizer();
}
if(typeof this.sizerWidth=="object"){
try{
this.sizerWidth=parseInt(this.sizerWidth.toString());
}
catch(e){
this.sizerWidth=15;
}
}
this.virtualSizer=document.createElement("div");
this.virtualSizer.style.position="absolute";
this.virtualSizer.style.display="none";
this.virtualSizer.style.zIndex=10;
this.virtualSizer.className=this.isHorizontal?"dojoSplitContainerVirtualSizerH":"dojoSplitContainerVirtualSizerV";
this.domNode.appendChild(this.virtualSizer);
dojo.html.disableSelection(this.virtualSizer);
if(this.persist){
this._restoreState();
}
this.resizeSoon();
},_injectChild:function(_ac2){
with(_ac2.domNode.style){
position="absolute";
}
dojo.html.addClass(_ac2.domNode,"dojoSplitPane");
},_addSizer:function(){
var i=this.sizers.length;
this.sizers[i]=document.createElement("div");
this.sizers[i].style.position="absolute";
this.sizers[i].className=this.isHorizontal?"dojoSplitContainerSizerH":"dojoSplitContainerSizerV";
var self=this;
var _ac5=(function(){
var _ac6=i;
return function(e){
self.beginSizing(e,_ac6);
};
})();
dojo.event.connect(this.sizers[i],"onmousedown",_ac5);
this.domNode.appendChild(this.sizers[i]);
dojo.html.disableSelection(this.sizers[i]);
},removeChild:function(_ac8){
if(this.sizers.length>0){
for(var x=0;x<this.children.length;x++){
if(this.children[x]===_ac8){
var i=this.sizers.length-1;
this.domNode.removeChild(this.sizers[i]);
this.sizers.length=i;
break;
}
}
}
dojo.widget.SplitContainer.superclass.removeChild.call(this,_ac8,arguments);
this.onResized();
},addChild:function(_acb){
dojo.widget.SplitContainer.superclass.addChild.apply(this,arguments);
this._injectChild(_acb);
if(this.children.length>1){
this._addSizer();
}
this._layoutPanels();
},_layoutPanels:function(){
if(this.children.length==0){
return;
}
var _acc=this.isHorizontal?this.paneWidth:this.paneHeight;
if(this.children.length>1){
_acc-=this.sizerWidth*(this.children.length-1);
}
var _acd=0;
for(var i=0;i<this.children.length;i++){
_acd+=this.children[i].sizeShare;
}
var _acf=_acc/_acd;
var _ad0=0;
for(var i=0;i<this.children.length-1;i++){
var size=Math.round(_acf*this.children[i].sizeShare);
this.children[i].sizeActual=size;
_ad0+=size;
}
this.children[this.children.length-1].sizeActual=_acc-_ad0;
this._checkSizes();
var pos=0;
var size=this.children[0].sizeActual;
this._movePanel(this.children[0],pos,size);
this.children[0].position=pos;
pos+=size;
for(var i=1;i<this.children.length;i++){
this._moveSlider(this.sizers[i-1],pos,this.sizerWidth);
this.sizers[i-1].position=pos;
pos+=this.sizerWidth;
size=this.children[i].sizeActual;
this._movePanel(this.children[i],pos,size);
this.children[i].position=pos;
pos+=size;
}
},_movePanel:function(_ad3,pos,size){
if(this.isHorizontal){
_ad3.domNode.style.left=pos+"px";
_ad3.domNode.style.top=0;
_ad3.resizeTo(size,this.paneHeight);
}else{
_ad3.domNode.style.left=0;
_ad3.domNode.style.top=pos+"px";
_ad3.resizeTo(this.paneWidth,size);
}
},_moveSlider:function(_ad6,pos,size){
if(this.isHorizontal){
_ad6.style.left=pos+"px";
_ad6.style.top=0;
dojo.html.setMarginBox(_ad6,{width:size,height:this.paneHeight});
}else{
_ad6.style.left=0;
_ad6.style.top=pos+"px";
dojo.html.setMarginBox(_ad6,{width:this.paneWidth,height:size});
}
},_growPane:function(_ad9,pane){
if(_ad9>0){
if(pane.sizeActual>pane.sizeMin){
if((pane.sizeActual-pane.sizeMin)>_ad9){
pane.sizeActual=pane.sizeActual-_ad9;
_ad9=0;
}else{
_ad9-=pane.sizeActual-pane.sizeMin;
pane.sizeActual=pane.sizeMin;
}
}
}
return _ad9;
},_checkSizes:function(){
var _adb=0;
var _adc=0;
for(var i=0;i<this.children.length;i++){
_adc+=this.children[i].sizeActual;
_adb+=this.children[i].sizeMin;
}
if(_adb<=_adc){
var _ade=0;
for(var i=0;i<this.children.length;i++){
if(this.children[i].sizeActual<this.children[i].sizeMin){
_ade+=this.children[i].sizeMin-this.children[i].sizeActual;
this.children[i].sizeActual=this.children[i].sizeMin;
}
}
if(_ade>0){
if(this.isDraggingLeft){
for(var i=this.children.length-1;i>=0;i--){
_ade=this._growPane(_ade,this.children[i]);
}
}else{
for(var i=0;i<this.children.length;i++){
_ade=this._growPane(_ade,this.children[i]);
}
}
}
}else{
for(var i=0;i<this.children.length;i++){
this.children[i].sizeActual=Math.round(_adc*(this.children[i].sizeMin/_adb));
}
}
},beginSizing:function(e,i){
this.paneBefore=this.children[i];
this.paneAfter=this.children[i+1];
this.isSizing=true;
this.sizingSplitter=this.sizers[i];
this.originPos=dojo.html.getAbsolutePosition(this.children[0].domNode,true,dojo.html.boxSizing.MARGIN_BOX);
if(this.isHorizontal){
var _ae1=(e.layerX?e.layerX:e.offsetX);
var _ae2=e.pageX;
this.originPos=this.originPos.x;
}else{
var _ae1=(e.layerY?e.layerY:e.offsetY);
var _ae2=e.pageY;
this.originPos=this.originPos.y;
}
this.startPoint=this.lastPoint=_ae2;
this.screenToClientOffset=_ae2-_ae1;
this.dragOffset=this.lastPoint-this.paneBefore.sizeActual-this.originPos-this.paneBefore.position;
if(!this.activeSizing){
this._showSizingLine();
}
dojo.event.connect(document.documentElement,"onmousemove",this,"changeSizing");
dojo.event.connect(document.documentElement,"onmouseup",this,"endSizing");
dojo.event.browser.stopEvent(e);
},changeSizing:function(e){
this.lastPoint=this.isHorizontal?e.pageX:e.pageY;
if(this.activeSizing){
this.movePoint();
this._updateSize();
}else{
this.movePoint();
this._moveSizingLine();
}
dojo.event.browser.stopEvent(e);
},endSizing:function(e){
if(!this.activeSizing){
this._hideSizingLine();
}
this._updateSize();
this.isSizing=false;
dojo.event.disconnect(document.documentElement,"onmousemove",this,"changeSizing");
dojo.event.disconnect(document.documentElement,"onmouseup",this,"endSizing");
if(this.persist){
this._saveState(this);
}
},movePoint:function(){
var p=this.lastPoint-this.screenToClientOffset;
var a=p-this.dragOffset;
a=this.legaliseSplitPoint(a);
p=a+this.dragOffset;
this.lastPoint=p+this.screenToClientOffset;
},legaliseSplitPoint:function(a){
a+=this.sizingSplitter.position;
this.isDraggingLeft=(a>0)?true:false;
if(!this.activeSizing){
if(a<this.paneBefore.position+this.paneBefore.sizeMin){
a=this.paneBefore.position+this.paneBefore.sizeMin;
}
if(a>this.paneAfter.position+(this.paneAfter.sizeActual-(this.sizerWidth+this.paneAfter.sizeMin))){
a=this.paneAfter.position+(this.paneAfter.sizeActual-(this.sizerWidth+this.paneAfter.sizeMin));
}
}
a-=this.sizingSplitter.position;
this._checkSizes();
return a;
},_updateSize:function(){
var pos=this.lastPoint-this.dragOffset-this.originPos;
var _ae9=this.paneBefore.position;
var _aea=this.paneAfter.position+this.paneAfter.sizeActual;
this.paneBefore.sizeActual=pos-_ae9;
this.paneAfter.position=pos+this.sizerWidth;
this.paneAfter.sizeActual=_aea-this.paneAfter.position;
for(var i=0;i<this.children.length;i++){
this.children[i].sizeShare=this.children[i].sizeActual;
}
this._layoutPanels();
},_showSizingLine:function(){
this._moveSizingLine();
if(this.isHorizontal){
dojo.html.setMarginBox(this.virtualSizer,{width:this.sizerWidth,height:this.paneHeight});
}else{
dojo.html.setMarginBox(this.virtualSizer,{width:this.paneWidth,height:this.sizerWidth});
}
this.virtualSizer.style.display="block";
},_hideSizingLine:function(){
this.virtualSizer.style.display="none";
},_moveSizingLine:function(){
var pos=this.lastPoint-this.startPoint+this.sizingSplitter.position;
if(this.isHorizontal){
this.virtualSizer.style.left=pos+"px";
}else{
var pos=(this.lastPoint-this.startPoint)+this.sizingSplitter.position;
this.virtualSizer.style.top=pos+"px";
}
},_getCookieName:function(i){
return this.widgetId+"_"+i;
},_restoreState:function(){
for(var i=0;i<this.children.length;i++){
var _aef=this._getCookieName(i);
var _af0=dojo.io.cookie.getCookie(_aef);
if(_af0!=null){
var pos=parseInt(_af0);
if(typeof pos=="number"){
this.children[i].sizeShare=pos;
}
}
}
},_saveState:function(){
for(var i=0;i<this.children.length;i++){
var _af3=this._getCookieName(i);
dojo.io.cookie.setCookie(_af3,this.children[i].sizeShare,null,null,null,null);
}
}});
dojo.lang.extend(dojo.widget.Widget,{sizeMin:10,sizeShare:10});
dojo.widget.defineWidget("dojo.widget.SplitContainerPanel",dojo.widget.ContentPane,{});
dojo.provide("dojo.widget.TreeCommon");
dojo.declare("dojo.widget.TreeCommon",null,{listenTreeEvents:[],listenedTrees:{},listenNodeFilter:null,listenTree:function(tree){
var _af5=this;
if(this.listenedTrees[tree.widgetId]){
return;
}
dojo.lang.forEach(this.listenTreeEvents,function(_af6){
var _af7="on"+_af6.charAt(0).toUpperCase()+_af6.substr(1);
dojo.event.topic.subscribe(tree.eventNames[_af6],_af5,_af7);
});
var _af8;
if(this.listenNodeFilter){
this.processDescendants(tree,this.listenNodeFilter,this.listenNode,true);
}
this.listenedTrees[tree.widgetId]=true;
},listenNode:function(){
},unlistenNode:function(){
},unlistenTree:function(tree,_afa){
var _afb=this;
if(!this.listenedTrees[tree.widgetId]){
return;
}
dojo.lang.forEach(this.listenTreeEvents,function(_afc){
var _afd="on"+_afc.charAt(0).toUpperCase()+_afc.substr(1);
dojo.event.topic.unsubscribe(tree.eventNames[_afc],_afb,_afd);
});
if(this.listenNodeFilter){
this.processDescendants(tree,this.listenNodeFilter,this.unlistenNode,true);
}
delete this.listenedTrees[tree.widgetId];
},checkPathCondition:function(_afe,_aff){
while(_afe&&!_afe.widgetId){
if(_aff.call(null,_afe)){
return true;
}
_afe=_afe.parentNode;
}
return false;
},domElement2TreeNode:function(_b00){
while(_b00&&!_b00.widgetId){
_b00=_b00.parentNode;
}
if(!_b00){
return null;
}
var _b01=dojo.widget.byId(_b00.widgetId);
if(!_b01.isTreeNode){
return null;
}
return _b01;
},processDescendants:function(elem,_b03,func,_b05){
var _b06=this;
if(!_b05){
if(!_b03.call(_b06,elem)){
return;
}
func.call(_b06,elem);
}
var _b07=[elem];
while(elem=_b07.pop()){
dojo.lang.forEach(elem.children,function(elem){
if(_b03.call(_b06,elem)){
func.call(_b06,elem);
_b07.push(elem);
}
});
}
}});
dojo.provide("dojo.widget.TreeWithNode");
dojo.declare("dojo.widget.TreeWithNode",null,function(){
},{loadStates:{UNCHECKED:"UNCHECKED",LOADING:"LOADING",LOADED:"LOADED"},state:"UNCHECKED",objectId:"",isContainer:true,lockLevel:0,lock:function(){
this.lockLevel++;
},unlock:function(){
if(!this.lockLevel){
dojo.raise(this.widgetType+" unlock: not locked");
}
this.lockLevel--;
},expandLevel:0,loadLevel:0,hasLock:function(){
return this.lockLevel>0;
},isLocked:function(){
var node=this;
while(true){
if(node.lockLevel){
return true;
}
if(!node.parent||node.isTree){
break;
}
node=node.parent;
}
return false;
},flushLock:function(){
this.lockLevel=0;
},actionIsDisabled:function(_b0a){
var _b0b=false;
if(dojo.lang.inArray(this.actionsDisabled,_b0a)){
_b0b=true;
}
if(this.isTreeNode){
if(!this.tree.allowAddChildToLeaf&&_b0a==this.actions.ADDCHILD&&!this.isFolder){
_b0b=true;
}
}
return _b0b;
},actionIsDisabledNow:function(_b0c){
return this.actionIsDisabled(_b0c)||this.isLocked();
},setChildren:function(_b0d){
if(this.isTreeNode&&!this.isFolder){
this.setFolder();
}else{
if(this.isTreeNode){
this.state=this.loadStates.LOADED;
}
}
var _b0e=this.children.length>0;
if(_b0e&&_b0d){
this.destroyChildren();
}
if(_b0d){
this.children=_b0d;
}
var _b0f=this.children.length>0;
if(this.isTreeNode&&_b0f!=_b0e){
this.viewSetHasChildren();
}
for(var i=0;i<this.children.length;i++){
var _b11=this.children[i];
if(!(_b11 instanceof dojo.widget.Widget)){
_b11=this.children[i]=this.tree.createNode(_b11);
var _b12=true;
}else{
var _b12=false;
}
if(!_b11.parent){
_b11.parent=this;
if(this.tree!==_b11.tree){
_b11.updateTree(this.tree);
}
_b11.viewAddLayout();
this.containerNode.appendChild(_b11.domNode);
var _b13={child:_b11,index:i,parent:this,childWidgetCreated:_b12};
delete dojo.widget.manager.topWidgets[_b11.widgetId];
dojo.event.topic.publish(this.tree.eventNames.afterAddChild,_b13);
}
if(this.tree.eagerWidgetInstantiation){
dojo.lang.forEach(this.children,function(_b14){
_b14.setChildren();
});
}
}
},doAddChild:function(_b15,_b16){
return this.addChild(_b15,_b16,true);
},addChild:function(_b17,_b18,_b19){
if(dojo.lang.isUndefined(_b18)){
_b18=this.children.length;
}
if(!_b17.isTreeNode){
dojo.raise("You can only add TreeNode widgets to a "+this.widgetType+" widget!");
return;
}
this.children.splice(_b18,0,_b17);
_b17.parent=this;
_b17.addedTo(this,_b18,_b19);
delete dojo.widget.manager.topWidgets[_b17.widgetId];
},onShow:function(){
this.animationInProgress=false;
},onHide:function(){
this.animationInProgress=false;
}});
dojo.provide("dojo.widget.TreeNodeV3");
dojo.widget.defineWidget("dojo.widget.TreeNodeV3",[dojo.widget.HtmlWidget,dojo.widget.TreeWithNode],function(){
this.actionsDisabled=[];
this.object={};
},{tryLazyInit:true,actions:{MOVE:"MOVE",DETACH:"DETACH",EDIT:"EDIT",ADDCHILD:"ADDCHILD",SELECT:"SELECT"},labelClass:"",contentClass:"",expandNode:null,labelNode:null,nodeDocType:"",selected:false,getnodeDocType:function(){
return this.nodeDocType;
},cloneProperties:["actionsDisabled","tryLazyInit","nodeDocType","objectId","object","title","isFolder","isExpanded","state"],clone:function(deep){
var ret=new this.constructor();
for(var i=0;i<this.cloneProperties.length;i++){
var prop=this.cloneProperties[i];
ret[prop]=dojo.lang.shallowCopy(this[prop],true);
}
if(this.tree.unsetFolderOnEmpty&&!deep&&this.isFolder){
ret.isFolder=false;
}
ret.toggleObj=this.toggleObj;
dojo.widget.manager.add(ret);
ret.tree=this.tree;
ret.buildRendering({},{});
ret.initialize({},{});
if(deep&&this.children.length){
for(var i=0;i<this.children.length;i++){
var _b1e=this.children[i];
if(_b1e.clone){
ret.children.push(_b1e.clone(deep));
}else{
ret.children.push(dojo.lang.shallowCopy(_b1e,deep));
}
}
ret.setChildren();
}
return ret;
},markProcessing:function(){
this.markProcessingSavedClass=dojo.html.getClass(this.expandNode);
dojo.html.setClass(this.expandNode,this.tree.classPrefix+"ExpandLoading");
},unmarkProcessing:function(){
dojo.html.setClass(this.expandNode,this.markProcessingSavedClass);
},buildRendering:function(args,_b20,_b21){
if(args.tree){
this.tree=dojo.lang.isString(args.tree)?dojo.widget.manager.getWidgetById(args.tree):args.tree;
}else{
if(_b21&&_b21.tree){
this.tree=_b21.tree;
}
}
if(!this.tree){
dojo.raise("Can't evaluate tree from arguments or parent");
}
this.domNode=this.tree.nodeTemplate.cloneNode(true);
this.expandNode=this.domNode.firstChild;
this.contentNode=this.domNode.childNodes[1];
this.labelNode=this.contentNode.firstChild;
if(this.labelClass){
dojo.html.addClass(this.labelNode,this.labelClass);
}
if(this.contentClass){
dojo.html.addClass(this.contentNode,this.contentClass);
}
this.domNode.widgetId=this.widgetId;
this.labelNode.innerHTML=this.title;
},isTreeNode:true,object:{},title:"",isFolder:null,contentNode:null,expandClass:"",isExpanded:false,containerNode:null,getInfo:function(){
var info={widgetId:this.widgetId,objectId:this.objectId,index:this.getParentIndex()};
return info;
},setFolder:function(){
this.isFolder=true;
this.viewSetExpand();
if(!this.containerNode){
this.viewAddContainer();
}
dojo.event.topic.publish(this.tree.eventNames.afterSetFolder,{source:this});
},initialize:function(args,frag,_b25){
if(args.isFolder){
this.isFolder=true;
}
if(this.children.length||this.isFolder){
this.setFolder();
}else{
this.viewSetExpand();
}
for(var i=0;i<this.actionsDisabled.length;i++){
this.actionsDisabled[i]=this.actionsDisabled[i].toUpperCase();
}
dojo.event.topic.publish(this.tree.eventNames.afterChangeTree,{oldTree:null,newTree:this.tree,node:this});
},unsetFolder:function(){
this.isFolder=false;
this.viewSetExpand();
dojo.event.topic.publish(this.tree.eventNames.afterUnsetFolder,{source:this});
},insertNode:function(_b27,_b28){
if(!_b28){
_b28=0;
}
if(_b28==0){
dojo.html.prependChild(this.domNode,_b27.containerNode);
}else{
dojo.html.insertAfter(this.domNode,_b27.children[_b28-1].domNode);
}
},updateTree:function(_b29){
if(this.tree===_b29){
return;
}
var _b2a=this.tree;
dojo.lang.forEach(this.getDescendants(),function(elem){
elem.tree=_b29;
});
if(_b2a.classPrefix!=_b29.classPrefix){
var _b2c=[this.domNode];
var elem;
var reg=new RegExp("(^|\\s)"+_b2a.classPrefix,"g");
while(elem=_b2c.pop()){
for(var i=0;i<elem.childNodes.length;i++){
var _b30=elem.childNodes[i];
if(_b30.nodeDocType!=1){
continue;
}
dojo.html.setClass(_b30,dojo.html.getClass(_b30).replace(reg,"$1"+_b29.classPrefix));
_b2c.push(_b30);
}
}
}
var _b31={oldTree:_b2a,newTree:_b29,node:this};
dojo.event.topic.publish(this.tree.eventNames.afterChangeTree,_b31);
dojo.event.topic.publish(_b29.eventNames.afterChangeTree,_b31);
},addedTo:function(_b32,_b33,_b34){
if(this.tree!==_b32.tree){
this.updateTree(_b32.tree);
}
if(_b32.isTreeNode){
if(!_b32.isFolder){
_b32.setFolder();
_b32.state=_b32.loadStates.LOADED;
}
}
var _b35=_b32.children.length;
this.insertNode(_b32,_b33);
this.viewAddLayout();
if(_b35>1){
if(_b33==0&&_b32.children[1] instanceof dojo.widget.Widget){
_b32.children[1].viewUpdateLayout();
}
if(_b33==_b35-1&&_b32.children[_b35-2] instanceof dojo.widget.Widget){
_b32.children[_b35-2].viewUpdateLayout();
}
}else{
if(_b32.isTreeNode){
_b32.viewSetHasChildren();
}
}
if(!_b34){
var _b36={child:this,index:_b33,parent:_b32};
dojo.event.topic.publish(this.tree.eventNames.afterAddChild,_b36);
}
},createSimple:function(args,_b38){
if(args.tree){
var tree=args.tree;
}else{
if(_b38){
var tree=_b38.tree;
}else{
dojo.raise("createSimple: can't evaluate tree");
}
}
tree=dojo.widget.byId(tree);
var _b3a=new tree.defaultChildWidget();
for(var x in args){
_b3a[x]=args[x];
}
_b3a.toggleObj=dojo.lfx.toggle[_b3a.toggle.toLowerCase()]||dojo.lfx.toggle.plain;
dojo.widget.manager.add(_b3a);
_b3a.buildRendering(args,{},_b38);
_b3a.initialize(args,{},_b38);
if(_b3a.parent){
delete dojo.widget.manager.topWidgets[_b3a.widgetId];
}
return _b3a;
},viewUpdateLayout:function(){
this.viewRemoveLayout();
this.viewAddLayout();
},viewAddContainer:function(){
this.containerNode=this.tree.containerNodeTemplate.cloneNode(true);
this.domNode.appendChild(this.containerNode);
},viewAddLayout:function(){
if(this.parent["isTree"]){
dojo.html.setClass(this.domNode,dojo.html.getClass(this.domNode)+" "+this.tree.classPrefix+"IsRoot");
}
if(this.isLastChild()){
dojo.html.setClass(this.domNode,dojo.html.getClass(this.domNode)+" "+this.tree.classPrefix+"IsLast");
}
},viewRemoveLayout:function(){
dojo.html.removeClass(this.domNode,this.tree.classPrefix+"IsRoot");
dojo.html.removeClass(this.domNode,this.tree.classPrefix+"IsLast");
},viewGetExpandClass:function(){
if(this.isFolder){
return this.isExpanded?"ExpandOpen":"ExpandClosed";
}else{
return "ExpandLeaf";
}
},viewSetExpand:function(){
var _b3c=this.tree.classPrefix+this.viewGetExpandClass();
var reg=new RegExp("(^|\\s)"+this.tree.classPrefix+"Expand\\w+","g");
dojo.html.setClass(this.domNode,dojo.html.getClass(this.domNode).replace(reg,"")+" "+_b3c);
this.viewSetHasChildrenAndExpand();
},viewGetChildrenClass:function(){
return "Children"+(this.children.length?"Yes":"No");
},viewSetHasChildren:function(){
var _b3e=this.tree.classPrefix+this.viewGetChildrenClass();
var reg=new RegExp("(^|\\s)"+this.tree.classPrefix+"Children\\w+","g");
dojo.html.setClass(this.domNode,dojo.html.getClass(this.domNode).replace(reg,"")+" "+_b3e);
this.viewSetHasChildrenAndExpand();
},viewSetHasChildrenAndExpand:function(){
var _b40=this.tree.classPrefix+"State"+this.viewGetChildrenClass()+"-"+this.viewGetExpandClass();
var reg=new RegExp("(^|\\s)"+this.tree.classPrefix+"State[\\w-]+","g");
dojo.html.setClass(this.domNode,dojo.html.getClass(this.domNode).replace(reg,"")+" "+_b40);
},viewUnfocus:function(){
dojo.html.removeClass(this.labelNode,this.tree.classPrefix+"LabelFocused");
},viewFocus:function(){
dojo.html.addClass(this.labelNode,this.tree.classPrefix+"LabelFocused");
},viewEmphasize:function(){
dojo.html.clearSelection(this.labelNode);
dojo.html.addClass(this.labelNode,this.tree.classPrefix+"NodeEmphasized");
},viewUnemphasize:function(){
dojo.html.removeClass(this.labelNode,this.tree.classPrefix+"NodeEmphasized");
},detach:function(){
if(!this.parent){
return;
}
var _b42=this.parent;
var _b43=this.getParentIndex();
this.doDetach.apply(this,arguments);
dojo.event.topic.publish(this.tree.eventNames.afterDetach,{child:this,parent:_b42,index:_b43});
},doDetach:function(){
var _b44=this.parent;
if(!_b44){
return;
}
var _b45=this.getParentIndex();
this.viewRemoveLayout();
dojo.widget.DomWidget.prototype.removeChild.call(_b44,this);
var _b46=_b44.children.length;
if(_b46>0){
if(_b45==0){
_b44.children[0].viewUpdateLayout();
}
if(_b45==_b46){
_b44.children[_b46-1].viewUpdateLayout();
}
}else{
if(_b44.isTreeNode){
_b44.viewSetHasChildren();
}
}
if(this.tree.unsetFolderOnEmpty&&!_b44.children.length&&_b44.isTreeNode){
_b44.unsetFolder();
}
this.parent=null;
},destroy:function(){
dojo.event.topic.publish(this.tree.eventNames.beforeNodeDestroy,{source:this});
this.detach();
return dojo.widget.HtmlWidget.prototype.destroy.apply(this,arguments);
},expand:function(){
if(this.isExpanded){
return;
}
if(this.tryLazyInit){
this.setChildren();
this.tryLazyInit=false;
}
this.isExpanded=true;
this.viewSetExpand();
this.showChildren();
},collapse:function(){
if(!this.isExpanded){
return;
}
this.isExpanded=false;
this.hideChildren();
},hideChildren:function(){
this.tree.toggleObj.hide(this.containerNode,this.tree.toggleDuration,this.explodeSrc,dojo.lang.hitch(this,"onHideChildren"));
},showChildren:function(){
this.tree.toggleObj.show(this.containerNode,this.tree.toggleDuration,this.explodeSrc,dojo.lang.hitch(this,"onShowChildren"));
},onShowChildren:function(){
this.onShow();
dojo.event.topic.publish(this.tree.eventNames.afterExpand,{source:this});
},onHideChildren:function(){
this.viewSetExpand();
this.onHide();
dojo.event.topic.publish(this.tree.eventNames.afterCollapse,{source:this});
},setTitle:function(_b47){
var _b48=this.title;
this.labelNode.innerHTML=this.title=_b47;
dojo.event.topic.publish(this.tree.eventNames.afterSetTitle,{source:this,oldTitle:_b48});
},toString:function(){
return "["+this.widgetType+", "+this.title+"]";
}});
dojo.provide("dojo.widget.TreeV3");
dojo.widget.defineWidget("dojo.widget.TreeV3",[dojo.widget.HtmlWidget,dojo.widget.TreeWithNode],function(){
this.eventNames={};
this.DndAcceptTypes=[];
this.actionsDisabled=[];
this.listeners=[];
this.tree=this;
},{DndMode:"",defaultChildWidget:null,defaultChildTitle:"New Node",eagerWidgetInstantiation:false,eventNamesDefault:{afterTreeCreate:"afterTreeCreate",beforeTreeDestroy:"beforeTreeDestroy",beforeNodeDestroy:"beforeNodeDestroy",afterChangeTree:"afterChangeTree",afterSetFolder:"afterSetFolder",afterUnsetFolder:"afterUnsetFolder",beforeMoveFrom:"beforeMoveFrom",beforeMoveTo:"beforeMoveTo",afterMoveFrom:"afterMoveFrom",afterMoveTo:"afterMoveTo",afterAddChild:"afterAddChild",afterDetach:"afterDetach",afterExpand:"afterExpand",beforeExpand:"beforeExpand",afterSetTitle:"afterSetTitle",afterCollapse:"afterCollapse",beforeCollapse:"beforeCollapse"},classPrefix:"Tree",style:"",allowAddChildToLeaf:true,unsetFolderOnEmpty:true,DndModes:{BETWEEN:1,ONTO:2},DndAcceptTypes:"",templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/TreeV3.css"),templateString:"<div style=\"${this.style}\">\n</div>",isExpanded:true,isTree:true,createNode:function(data){
data.tree=this.widgetId;
if(data.widgetName){
return dojo.widget.createWidget(data.widgetName,data);
}else{
if(this.defaultChildWidget.prototype.createSimple){
return this.defaultChildWidget.prototype.createSimple(data);
}else{
var ns=this.defaultChildWidget.prototype.ns;
var wt=this.defaultChildWidget.prototype.widgetType;
return dojo.widget.createWidget(ns+":"+wt,data);
}
}
},makeNodeTemplate:function(){
var _b4c=document.createElement("div");
dojo.html.setClass(_b4c,this.classPrefix+"Node "+this.classPrefix+"ExpandLeaf "+this.classPrefix+"ChildrenNo");
this.nodeTemplate=_b4c;
var _b4d=document.createElement("div");
var _b4e=this.classPrefix+"Expand";
if(dojo.render.html.ie){
_b4e=_b4e+" "+this.classPrefix+"IEExpand";
}
dojo.html.setClass(_b4d,_b4e);
this.expandNodeTemplate=_b4d;
var _b4f=document.createElement("span");
dojo.html.setClass(_b4f,this.classPrefix+"Label");
this.labelNodeTemplate=_b4f;
var _b50=document.createElement("div");
var _b4e=this.classPrefix+"Content";
if(dojo.render.html.ie&&!dojo.render.html.ie70){
_b4e=_b4e+" "+this.classPrefix+"IEContent";
}
dojo.html.setClass(_b50,_b4e);
this.contentNodeTemplate=_b50;
_b4c.appendChild(_b4d);
_b4c.appendChild(_b50);
_b50.appendChild(_b4f);
},makeContainerNodeTemplate:function(){
var div=document.createElement("div");
div.style.display="none";
dojo.html.setClass(div,this.classPrefix+"Container");
this.containerNodeTemplate=div;
},actions:{ADDCHILD:"ADDCHILD"},getInfo:function(){
var info={widgetId:this.widgetId,objectId:this.objectId};
return info;
},adjustEventNames:function(){
for(var name in this.eventNamesDefault){
if(dojo.lang.isUndefined(this.eventNames[name])){
this.eventNames[name]=this.widgetId+"/"+this.eventNamesDefault[name];
}
}
},adjustDndMode:function(){
var _b54=this;
var _b55=0;
dojo.lang.forEach(this.DndMode.split(";"),function(elem){
var mode=_b54.DndModes[dojo.string.trim(elem).toUpperCase()];
if(mode){
_b55=_b55|mode;
}
});
this.DndMode=_b55;
},destroy:function(){
dojo.event.topic.publish(this.tree.eventNames.beforeTreeDestroy,{source:this});
return dojo.widget.HtmlWidget.prototype.destroy.apply(this,arguments);
},initialize:function(args){
this.domNode.widgetId=this.widgetId;
for(var i=0;i<this.actionsDisabled.length;i++){
this.actionsDisabled[i]=this.actionsDisabled[i].toUpperCase();
}
if(!args.defaultChildWidget){
this.defaultChildWidget=dojo.widget.TreeNodeV3;
}else{
this.defaultChildWidget=dojo.lang.getObjPathValue(args.defaultChildWidget);
}
this.adjustEventNames();
this.adjustDndMode();
this.makeNodeTemplate();
this.makeContainerNodeTemplate();
this.containerNode=this.domNode;
dojo.html.setClass(this.domNode,this.classPrefix+"Container");
var _b5a=this;
dojo.lang.forEach(this.listeners,function(elem){
var t=dojo.lang.isString(elem)?dojo.widget.byId(elem):elem;
t.listenTree(_b5a);
});
},postCreate:function(){
dojo.event.topic.publish(this.eventNames.afterTreeCreate,{source:this});
},move:function(_b5d,_b5e,_b5f){
if(!_b5d.parent){
dojo.raise(this.widgetType+": child can be moved only while it's attached");
}
var _b60=_b5d.parent;
var _b61=_b5d.tree;
var _b62=_b5d.getParentIndex();
var _b63=_b5e.tree;
var _b5e=_b5e;
var _b64=_b5f;
var _b65={oldParent:_b60,oldTree:_b61,oldIndex:_b62,newParent:_b5e,newTree:_b63,newIndex:_b64,child:_b5d};
dojo.event.topic.publish(_b61.eventNames.beforeMoveFrom,_b65);
dojo.event.topic.publish(_b63.eventNames.beforeMoveTo,_b65);
this.doMove.apply(this,arguments);
dojo.event.topic.publish(_b61.eventNames.afterMoveFrom,_b65);
dojo.event.topic.publish(_b63.eventNames.afterMoveTo,_b65);
},doMove:function(_b66,_b67,_b68){
_b66.doDetach();
_b67.doAddChild(_b66,_b68);
},toString:function(){
return "["+this.widgetType+" ID:"+this.widgetId+"]";
}});
dojo.provide("dojo.widget.TreeTimeoutIterator");
dojo.declare("dojo.widget.TreeTimeoutIterator",null,function(elem,_b6a,_b6b){
var _b6c=this;
this.currentParent=elem;
this.callFunc=_b6a;
this.callObj=_b6b?_b6b:this;
this.stack=[];
},{maxStackDepth:Number.POSITIVE_INFINITY,stack:null,currentParent:null,currentIndex:0,filterFunc:function(){
return true;
},finishFunc:function(){
return true;
},setFilter:function(func,obj){
this.filterFunc=func;
this.filterObj=obj;
},setMaxLevel:function(_b6f){
this.maxStackDepth=_b6f-2;
},forward:function(_b70){
var _b71=this;
if(this.timeout){
var tid=setTimeout(function(){
_b71.processNext();
clearTimeout(tid);
},_b71.timeout);
}else{
return this.processNext();
}
},start:function(_b73){
if(_b73){
return this.callFunc.call(this.callObj,this.currentParent,this);
}
return this.processNext();
},processNext:function(){
var _b74;
var _b75=this;
var _b76;
var next;
if(this.maxStackDepth==-2){
return;
}
while(true){
var _b78=this.currentParent.children;
if(_b78&&_b78.length){
do{
next=_b78[this.currentIndex];
}while(this.currentIndex++<_b78.length&&!(_b76=this.filterFunc.call(this.filterObj,next)));
if(_b76){
if(next.isFolder&&this.stack.length<=this.maxStackDepth){
this.moveParent(next,0);
}
return this.callFunc.call(this.callObj,next,this);
}
}
if(this.stack.length){
this.popParent();
continue;
}
break;
}
return this.finishFunc.call(this.finishObj);
},setFinish:function(func,obj){
this.finishFunc=func;
this.finishObj=obj;
},popParent:function(){
var p=this.stack.pop();
this.currentParent=p[0];
this.currentIndex=p[1];
},moveParent:function(_b7c,_b7d){
this.stack.push([this.currentParent,this.currentIndex]);
this.currentParent=_b7c;
this.currentIndex=_b7d;
}});
dojo.provide("dojo.widget.TreeBasicControllerV3");
dojo.widget.defineWidget("dojo.widget.TreeBasicControllerV3",[dojo.widget.HtmlWidget,dojo.widget.TreeCommon],function(){
this.listenedTrees={};
},{listenTreeEvents:["afterSetFolder","afterTreeCreate","beforeTreeDestroy"],listenNodeFilter:function(elem){
return elem instanceof dojo.widget.Widget;
},editor:null,initialize:function(args){
if(args.editor){
this.editor=dojo.widget.byId(args.editor);
this.editor.controller=this;
}
},getInfo:function(elem){
return elem.getInfo();
},onBeforeTreeDestroy:function(_b81){
this.unlistenTree(_b81.source);
},onAfterSetFolder:function(_b82){
if(_b82.source.expandLevel>0){
this.expandToLevel(_b82.source,_b82.source.expandLevel);
}
if(_b82.source.loadLevel>0){
this.loadToLevel(_b82.source,_b82.source.loadLevel);
}
},_focusNextVisible:function(_b83){
if(_b83.isFolder&&_b83.isExpanded&&_b83.children.length>0){
_b84=_b83.children[0];
}else{
while(_b83.isTreeNode&&_b83.isLastChild()){
_b83=_b83.parent;
}
if(_b83.isTreeNode){
var _b84=_b83.parent.children[_b83.getParentIndex()+1];
}
}
if(_b84&&_b84.isTreeNode){
this._focusLabel(_b84);
return _b84;
}
},_focusPreviousVisible:function(_b85){
var _b86=_b85;
if(!_b85.isFirstChild()){
var _b87=_b85.parent.children[_b85.getParentIndex()-1];
_b85=_b87;
while(_b85.isFolder&&_b85.isExpanded&&_b85.children.length>0){
_b86=_b85;
_b85=_b85.children[_b85.children.length-1];
}
}else{
_b85=_b85.parent;
}
if(_b85&&_b85.isTreeNode){
_b86=_b85;
}
if(_b86&&_b86.isTreeNode){
this._focusLabel(_b86);
return _b86;
}
},_focusZoomIn:function(_b88){
var _b89=_b88;
if(_b88.isFolder&&!_b88.isExpanded){
this.expand(_b88);
}else{
if(_b88.children.length>0){
_b88=_b88.children[0];
}
}
if(_b88&&_b88.isTreeNode){
_b89=_b88;
}
if(_b89&&_b89.isTreeNode){
this._focusLabel(_b89);
return _b89;
}
},_focusZoomOut:function(node){
var _b8b=node;
if(node.isFolder&&node.isExpanded){
this.collapse(node);
}else{
node=node.parent;
}
if(node&&node.isTreeNode){
_b8b=node;
}
if(_b8b&&_b8b.isTreeNode){
this._focusLabel(_b8b);
return _b8b;
}
},onFocusNode:function(e){
var node=this.domElement2TreeNode(e.target);
if(node){
node.viewFocus();
dojo.event.browser.stopEvent(e);
}
},onBlurNode:function(e){
var node=this.domElement2TreeNode(e.target);
if(!node){
return;
}
var _b90=node.labelNode;
_b90.setAttribute("tabIndex","-1");
node.viewUnfocus();
dojo.event.browser.stopEvent(e);
node.tree.domNode.setAttribute("tabIndex","0");
},_focusLabel:function(node){
var _b92=node.tree.lastFocused;
var _b93;
if(_b92&&_b92.labelNode){
_b93=_b92.labelNode;
dojo.event.disconnect(_b93,"onblur",this,"onBlurNode");
_b93.setAttribute("tabIndex","-1");
dojo.html.removeClass(_b93,"TreeLabelFocused");
}
_b93=node.labelNode;
_b93.setAttribute("tabIndex","0");
node.tree.lastFocused=node;
dojo.html.addClass(_b93,"TreeLabelFocused");
dojo.event.connectOnce(_b93,"onblur",this,"onBlurNode");
dojo.event.connectOnce(_b93,"onfocus",this,"onFocusNode");
_b93.focus();
},onKey:function(e){
if(!e.key||e.ctrkKey||e.altKey){
return;
}
var _b95=this.domElement2TreeNode(e.target);
if(!_b95){
return;
}
var _b96=_b95.tree;
if(_b96.lastFocused&&_b96.lastFocused.labelNode){
_b95=_b96.lastFocused;
}
switch(e.key){
case e.KEY_TAB:
if(e.shiftKey){
_b96.domNode.setAttribute("tabIndex","-1");
}
break;
case e.KEY_RIGHT_ARROW:
this._focusZoomIn(_b95);
dojo.event.browser.stopEvent(e);
break;
case e.KEY_LEFT_ARROW:
this._focusZoomOut(_b95);
dojo.event.browser.stopEvent(e);
break;
case e.KEY_UP_ARROW:
this._focusPreviousVisible(_b95);
dojo.event.browser.stopEvent(e);
break;
case e.KEY_DOWN_ARROW:
this._focusNextVisible(_b95);
dojo.event.browser.stopEvent(e);
break;
}
},onFocusTree:function(e){
if(!e.currentTarget){
return;
}
try{
var _b98=this.getWidgetByNode(e.currentTarget);
if(!_b98||!_b98.isTree){
return;
}
var _b99=this.getWidgetByNode(_b98.domNode.firstChild);
if(_b99&&_b99.isTreeNode){
if(_b98.lastFocused&&_b98.lastFocused.isTreeNode){
_b99=_b98.lastFocused;
}
this._focusLabel(_b99);
}
}
catch(e){
}
},onAfterTreeCreate:function(_b9a){
var tree=_b9a.source;
dojo.event.browser.addListener(tree.domNode,"onKey",dojo.lang.hitch(this,this.onKey));
dojo.event.browser.addListener(tree.domNode,"onmousedown",dojo.lang.hitch(this,this.onTreeMouseDown));
dojo.event.browser.addListener(tree.domNode,"onclick",dojo.lang.hitch(this,this.onTreeClick));
dojo.event.browser.addListener(tree.domNode,"onfocus",dojo.lang.hitch(this,this.onFocusTree));
tree.domNode.setAttribute("tabIndex","0");
if(tree.expandLevel){
this.expandToLevel(tree,tree.expandLevel);
}
if(tree.loadLevel){
this.loadToLevel(tree,tree.loadLevel);
}
},onTreeMouseDown:function(e){
},onTreeClick:function(e){
var _b9e=e.target;
var node=this.domElement2TreeNode(_b9e);
if(!node||!node.isTreeNode){
return;
}
var _ba0=function(el){
return el===node.expandNode;
};
if(this.checkPathCondition(_b9e,_ba0)){
this.processExpandClick(node);
}
this._focusLabel(node);
},processExpandClick:function(node){
if(node.isExpanded){
this.collapse(node);
}else{
this.expand(node);
}
},batchExpandTimeout:20,expandAll:function(_ba3){
return this.expandToLevel(_ba3,Number.POSITIVE_INFINITY);
},collapseAll:function(_ba4){
var _ba5=this;
var _ba6=function(elem){
return (elem instanceof dojo.widget.Widget)&&elem.isFolder&&elem.isExpanded;
};
if(_ba4.isTreeNode){
this.processDescendants(_ba4,_ba6,this.collapse);
}else{
if(_ba4.isTree){
dojo.lang.forEach(_ba4.children,function(c){
_ba5.processDescendants(c,_ba6,_ba5.collapse);
});
}
}
},expandToNode:function(node,_baa){
n=_baa?node:node.parent;
s=[];
while(!n.isExpanded){
s.push(n);
n=n.parent;
}
dojo.lang.forEach(s,function(n){
n.expand();
});
},expandToLevel:function(_bac,_bad){
var _bae=this;
var _baf=function(elem){
var res=elem.isFolder||elem.children&&elem.children.length;
return res;
};
var _bb2=function(node,_bb4){
_bae.expand(node,true);
_bb4.forward();
};
var _bb5=new dojo.widget.TreeTimeoutIterator(_bac,_bb2,this);
_bb5.setFilter(_baf);
_bb5.timeout=this.batchExpandTimeout;
_bb5.setMaxLevel(_bac.isTreeNode?_bad-1:_bad);
return _bb5.start(_bac.isTreeNode);
},getWidgetByNode:function(node){
var _bb7;
var _bb8=node;
while(!(_bb7=_bb8.widgetId)){
_bb8=_bb8.parentNode;
if(_bb8==null){
break;
}
}
if(_bb7){
return dojo.widget.byId(_bb7);
}else{
if(node==null){
return null;
}else{
return dojo.widget.manager.byNode(node);
}
}
},expand:function(node){
if(node.isFolder){
node.expand();
}
},collapse:function(node){
if(node.isFolder){
node.collapse();
}
},canEditLabel:function(node){
if(node.actionIsDisabledNow(node.actions.EDIT)){
return false;
}
return true;
},editLabelStart:function(node){
if(!this.canEditLabel(node)){
return false;
}
if(!this.editor.isClosed()){
this.editLabelFinish(this.editor.saveOnBlur);
}
this.doEditLabelStart(node);
},editLabelFinish:function(save){
this.doEditLabelFinish(save);
},doEditLabelStart:function(node){
if(!this.editor){
dojo.raise(this.widgetType+": no editor specified");
}
this.editor.open(node);
},doEditLabelFinish:function(save,_bc0){
if(!this.editor){
dojo.raise(this.widgetType+": no editor specified");
}
var node=this.editor.node;
var _bc2=this.editor.getContents();
this.editor.close(save);
if(save){
var data={title:_bc2};
if(_bc0){
dojo.lang.mixin(data,_bc0);
}
if(node.isPhantom){
var _bc4=node.parent;
var _bc5=node.getParentIndex();
node.destroy();
dojo.widget.TreeBasicControllerV3.prototype.doCreateChild.call(this,_bc4,_bc5,data);
}else{
var _bc6=_bc0&&_bc0.title?_bc0.title:_bc2;
node.setTitle(_bc6);
}
}else{
if(node.isPhantom){
node.destroy();
}
}
},makeDefaultNode:function(_bc7,_bc8){
var data={title:_bc7.tree.defaultChildTitle};
return dojo.widget.TreeBasicControllerV3.prototype.doCreateChild.call(this,_bc7,_bc8,data);
},runStages:function(_bca,_bcb,make,_bcd,_bce,args){
if(_bca&&!_bca.apply(this,args)){
return false;
}
if(_bcb&&!_bcb.apply(this,args)){
return false;
}
var _bd0=make.apply(this,args);
if(_bcd){
_bcd.apply(this,args);
}
if(!_bd0){
return _bd0;
}
if(_bce){
_bce.apply(this,args);
}
return _bd0;
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{createAndEdit:function(_bd1,_bd2){
var data={title:_bd1.tree.defaultChildTitle};
if(!this.canCreateChild(_bd1,_bd2,data)){
return false;
}
var _bd4=this.doCreateChild(_bd1,_bd2,data);
if(!_bd4){
return false;
}
this.exposeCreateChild(_bd1,_bd2,data);
_bd4.isPhantom=true;
if(!this.editor.isClosed()){
this.editLabelFinish(this.editor.saveOnBlur);
}
this.doEditLabelStart(_bd4);
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{canClone:function(_bd5,_bd6,_bd7,deep){
return true;
},clone:function(_bd9,_bda,_bdb,deep){
return this.runStages(this.canClone,this.prepareClone,this.doClone,this.finalizeClone,this.exposeClone,arguments);
},exposeClone:function(_bdd,_bde){
if(_bde.isTreeNode){
this.expand(_bde);
}
},doClone:function(_bdf,_be0,_be1,deep){
var _be3=_bdf.clone(deep);
_be0.addChild(_be3,_be1);
return _be3;
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{canDetach:function(_be4){
if(_be4.actionIsDisabledNow(_be4.actions.DETACH)){
return false;
}
return true;
},detach:function(node){
return this.runStages(this.canDetach,this.prepareDetach,this.doDetach,this.finalizeDetach,this.exposeDetach,arguments);
},doDetach:function(node,_be7,_be8){
node.detach();
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{canDestroyChild:function(_be9){
if(_be9.parent&&!this.canDetach(_be9)){
return false;
}
return true;
},destroyChild:function(node){
return this.runStages(this.canDestroyChild,this.prepareDestroyChild,this.doDestroyChild,this.finalizeDestroyChild,this.exposeDestroyChild,arguments);
},doDestroyChild:function(node){
node.destroy();
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{canMoveNotANode:function(_bec,_bed){
if(_bec.treeCanMove){
return _bec.treeCanMove(_bed);
}
return true;
},canMove:function(_bee,_bef){
if(!_bee.isTreeNode){
return this.canMoveNotANode(_bee,_bef);
}
if(_bee.actionIsDisabledNow(_bee.actions.MOVE)){
return false;
}
if(_bee.parent!==_bef&&_bef.actionIsDisabledNow(_bef.actions.ADDCHILD)){
return false;
}
var node=_bef;
while(node.isTreeNode){
if(node===_bee){
return false;
}
node=node.parent;
}
return true;
},move:function(_bf1,_bf2,_bf3){
return this.runStages(this.canMove,this.prepareMove,this.doMove,this.finalizeMove,this.exposeMove,arguments);
},doMove:function(_bf4,_bf5,_bf6){
_bf4.tree.move(_bf4,_bf5,_bf6);
return true;
},exposeMove:function(_bf7,_bf8){
if(_bf8.isTreeNode){
this.expand(_bf8);
}
}});
dojo.lang.extend(dojo.widget.TreeBasicControllerV3,{canCreateChild:function(_bf9,_bfa,data){
if(_bf9.actionIsDisabledNow(_bf9.actions.ADDCHILD)){
return false;
}
return true;
},createChild:function(_bfc,_bfd,data){
if(!data){
data={title:_bfc.tree.defaultChildTitle};
}
return this.runStages(this.canCreateChild,this.prepareCreateChild,this.doCreateChild,this.finalizeCreateChild,this.exposeCreateChild,[_bfc,_bfd,data]);
},prepareCreateChild:function(){
return true;
},finalizeCreateChild:function(){
},doCreateChild:function(_bff,_c00,data){
var _c02=_bff.tree.createNode(data);
_bff.addChild(_c02,_c00);
return _c02;
},exposeCreateChild:function(_c03){
return this.expand(_c03);
}});
dojo.provide("dojo.widget.TreeSelectorV3");
dojo.widget.defineWidget("dojo.widget.TreeSelectorV3",[dojo.widget.HtmlWidget,dojo.widget.TreeCommon],function(){
this.eventNames={};
this.listenedTrees={};
this.selectedNodes=[];
this.lastClicked={};
},{listenTreeEvents:["afterTreeCreate","afterCollapse","afterChangeTree","afterDetach","beforeTreeDestroy"],listenNodeFilter:function(elem){
return elem instanceof dojo.widget.Widget;
},allowedMulti:true,dblselectTimeout:300,eventNamesDefault:{select:"select",deselect:"deselect",dblselect:"dblselect"},onAfterTreeCreate:function(_c05){
var tree=_c05.source;
dojo.event.browser.addListener(tree.domNode,"onclick",dojo.lang.hitch(this,this.onTreeClick));
if(dojo.render.html.ie){
dojo.event.browser.addListener(tree.domNode,"ondblclick",dojo.lang.hitch(this,this.onTreeDblClick));
}
dojo.event.browser.addListener(tree.domNode,"onKey",dojo.lang.hitch(this,this.onKey));
},onKey:function(e){
if(!e.key||e.ctrkKey||e.altKey){
return;
}
switch(e.key){
case e.KEY_ENTER:
var node=this.domElement2TreeNode(e.target);
if(node){
this.processNode(node,e);
}
}
},onAfterChangeTree:function(_c09){
if(!_c09.oldTree&&_c09.node.selected){
this.select(_c09.node);
}
if(!_c09.newTree||!this.listenedTrees[_c09.newTree.widgetId]){
if(this.selectedNode&&_c09.node.children){
this.deselectIfAncestorMatch(_c09.node);
}
}
},initialize:function(args){
for(var name in this.eventNamesDefault){
if(dojo.lang.isUndefined(this.eventNames[name])){
this.eventNames[name]=this.widgetId+"/"+this.eventNamesDefault[name];
}
}
},onBeforeTreeDestroy:function(_c0c){
this.unlistenTree(_c0c.source);
},onAfterCollapse:function(_c0d){
this.deselectIfAncestorMatch(_c0d.source);
},onTreeDblClick:function(_c0e){
this.onTreeClick(_c0e);
},checkSpecialEvent:function(_c0f){
return _c0f.shiftKey||_c0f.ctrlKey;
},onTreeClick:function(_c10){
var node=this.domElement2TreeNode(_c10.target);
if(!node){
return;
}
var _c12=function(_c13){
return _c13===node.labelNode;
};
if(this.checkPathCondition(_c10.target,_c12)){
this.processNode(node,_c10);
}
},processNode:function(node,_c15){
if(node.actionIsDisabled(node.actions.SELECT)){
return;
}
if(dojo.lang.inArray(this.selectedNodes,node)){
if(this.checkSpecialEvent(_c15)){
this.deselect(node);
return;
}
var _c16=this;
var i=0;
var _c18;
while(this.selectedNodes.length>i){
_c18=this.selectedNodes[i];
if(_c18!==node){
this.deselect(_c18);
continue;
}
i++;
}
var _c19=this.checkRecentClick(node);
eventName=_c19?this.eventNames.dblselect:this.eventNames.select;
if(_c19){
eventName=this.eventNames.dblselect;
this.forgetLastClicked();
}else{
eventName=this.eventNames.select;
this.setLastClicked(node);
}
dojo.event.topic.publish(eventName,{node:node});
return;
}
this.deselectIfNoMulti(_c15);
this.setLastClicked(node);
this.select(node);
},forgetLastClicked:function(){
this.lastClicked={};
},setLastClicked:function(node){
this.lastClicked.date=new Date();
this.lastClicked.node=node;
},checkRecentClick:function(node){
var diff=new Date()-this.lastClicked.date;
if(this.lastClicked.node&&diff<this.dblselectTimeout){
return true;
}else{
return false;
}
},deselectIfNoMulti:function(_c1d){
if(!this.checkSpecialEvent(_c1d)||!this.allowedMulti){
this.deselectAll();
}
},deselectIfAncestorMatch:function(_c1e){
var _c1f=this;
dojo.lang.forEach(this.selectedNodes,function(node){
var _c21=node;
node=node.parent;
while(node&&node.isTreeNode){
if(node===_c1e){
_c1f.deselect(_c21);
return;
}
node=node.parent;
}
});
},onAfterDetach:function(_c22){
this.deselectIfAncestorMatch(_c22.child);
},select:function(node){
var _c24=dojo.lang.find(this.selectedNodes,node,true);
if(_c24>=0){
return;
}
this.selectedNodes.push(node);
dojo.event.topic.publish(this.eventNames.select,{node:node});
},deselect:function(node){
var _c26=dojo.lang.find(this.selectedNodes,node,true);
if(_c26<0){
return;
}
this.selectedNodes.splice(_c26,1);
dojo.event.topic.publish(this.eventNames.deselect,{node:node});
},deselectAll:function(){
while(this.selectedNodes.length){
this.deselect(this.selectedNodes[0]);
}
}});
dojo.provide("dojo.widget.TreeEmphasizeOnSelect");
dojo.widget.defineWidget("dojo.widget.TreeEmphasizeOnSelect",dojo.widget.HtmlWidget,{selector:"",initialize:function(){
this.selector=dojo.widget.byId(this.selector);
dojo.event.topic.subscribe(this.selector.eventNames.select,this,"onSelect");
dojo.event.topic.subscribe(this.selector.eventNames.deselect,this,"onDeselect");
},onSelect:function(_c27){
_c27.node.viewEmphasize();
},onDeselect:function(_c28){
_c28.node.viewUnemphasize();
}});
dojo.provide("dojo.Deferred");
dojo.Deferred=function(_c29){
this.chain=[];
this.id=this._nextId();
this.fired=-1;
this.paused=0;
this.results=[null,null];
this.canceller=_c29;
this.silentlyCancelled=false;
};
dojo.lang.extend(dojo.Deferred,{getFunctionFromArgs:function(){
var a=arguments;
if((a[0])&&(!a[1])){
if(dojo.lang.isFunction(a[0])){
return a[0];
}else{
if(dojo.lang.isString(a[0])){
return dj_global[a[0]];
}
}
}else{
if((a[0])&&(a[1])){
return dojo.lang.hitch(a[0],a[1]);
}
}
return null;
},makeCalled:function(){
var _c2b=new dojo.Deferred();
_c2b.callback();
return _c2b;
},repr:function(){
var _c2c;
if(this.fired==-1){
_c2c="unfired";
}else{
if(this.fired==0){
_c2c="success";
}else{
_c2c="error";
}
}
return "Deferred("+this.id+", "+_c2c+")";
},toString:dojo.lang.forward("repr"),_nextId:(function(){
var n=1;
return function(){
return n++;
};
})(),cancel:function(){
if(this.fired==-1){
if(this.canceller){
this.canceller(this);
}else{
this.silentlyCancelled=true;
}
if(this.fired==-1){
this.errback(new Error(this.repr()));
}
}else{
if((this.fired==0)&&(this.results[0] instanceof dojo.Deferred)){
this.results[0].cancel();
}
}
},_pause:function(){
this.paused++;
},_unpause:function(){
this.paused--;
if((this.paused==0)&&(this.fired>=0)){
this._fire();
}
},_continue:function(res){
this._resback(res);
this._unpause();
},_resback:function(res){
this.fired=((res instanceof Error)?1:0);
this.results[this.fired]=res;
this._fire();
},_check:function(){
if(this.fired!=-1){
if(!this.silentlyCancelled){
dojo.raise("already called!");
}
this.silentlyCancelled=false;
return;
}
},callback:function(res){
this._check();
this._resback(res);
},errback:function(res){
this._check();
if(!(res instanceof Error)){
res=new Error(res);
}
this._resback(res);
},addBoth:function(cb,cbfn){
var _c34=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_c34=dojo.lang.curryArguments(null,_c34,arguments,2);
}
return this.addCallbacks(_c34,_c34);
},addCallback:function(cb,cbfn){
var _c37=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_c37=dojo.lang.curryArguments(null,_c37,arguments,2);
}
return this.addCallbacks(_c37,null);
},addErrback:function(cb,cbfn){
var _c3a=this.getFunctionFromArgs(cb,cbfn);
if(arguments.length>2){
_c3a=dojo.lang.curryArguments(null,_c3a,arguments,2);
}
return this.addCallbacks(null,_c3a);
return this.addCallbacks(null,cbfn);
},addCallbacks:function(cb,eb){
this.chain.push([cb,eb]);
if(this.fired>=0){
this._fire();
}
return this;
},_fire:function(){
var _c3d=this.chain;
var _c3e=this.fired;
var res=this.results[_c3e];
var self=this;
var cb=null;
while(_c3d.length>0&&this.paused==0){
var pair=_c3d.shift();
var f=pair[_c3e];
if(f==null){
continue;
}
try{
res=f(res);
_c3e=((res instanceof Error)?1:0);
if(res instanceof dojo.Deferred){
cb=function(res){
self._continue(res);
};
this._pause();
}
}
catch(err){
_c3e=1;
res=err;
}
}
this.fired=_c3e;
this.results[_c3e]=res;
if((cb)&&(this.paused)){
res.addBoth(cb);
}
}});
dojo.provide("dojo.dnd.TreeDragAndDropV3");
dojo.dnd.TreeDragSourceV3=function(node,_c46,type,_c48){
this.controller=_c46;
this.treeNode=_c48;
dojo.dnd.HtmlDragSource.call(this,node,type);
};
dojo.inherits(dojo.dnd.TreeDragSourceV3,dojo.dnd.HtmlDragSource);
dojo.dnd.TreeDropTargetV3=function(_c49,_c4a,type,_c4c){
this.treeNode=_c4c;
this.controller=_c4a;
dojo.dnd.HtmlDropTarget.call(this,_c49,type);
};
dojo.inherits(dojo.dnd.TreeDropTargetV3,dojo.dnd.HtmlDropTarget);
dojo.lang.extend(dojo.dnd.TreeDropTargetV3,{autoExpandDelay:1500,autoExpandTimer:null,position:null,indicatorStyle:"2px black groove",showIndicator:function(_c4d){
if(this.position==_c4d){
return;
}
this.hideIndicator();
this.position=_c4d;
var node=this.treeNode;
node.contentNode.style.width=dojo.html.getBorderBox(node.labelNode).width+"px";
if(_c4d=="onto"){
node.contentNode.style.border=this.indicatorStyle;
}else{
if(_c4d=="before"){
node.contentNode.style.borderTop=this.indicatorStyle;
}else{
if(_c4d=="after"){
node.contentNode.style.borderBottom=this.indicatorStyle;
}
}
}
},hideIndicator:function(){
this.treeNode.contentNode.style.borderBottom="";
this.treeNode.contentNode.style.borderTop="";
this.treeNode.contentNode.style.border="";
this.treeNode.contentNode.style.width="";
this.position=null;
},onDragOver:function(e){
var _c50=dojo.dnd.HtmlDropTarget.prototype.onDragOver.apply(this,arguments);
if(_c50&&this.treeNode.isFolder&&!this.treeNode.isExpanded){
this.setAutoExpandTimer();
}
if(_c50){
this.cacheNodeCoords();
}
return _c50;
},accepts:function(_c51){
var _c52=dojo.dnd.HtmlDropTarget.prototype.accepts.apply(this,arguments);
if(!_c52){
return false;
}
for(var i=0;i<_c51.length;i++){
var _c54=_c51[i].treeNode;
if(_c54===this.treeNode){
return false;
}
}
return true;
},setAutoExpandTimer:function(){
var _c55=this;
var _c56=function(){
if(dojo.dnd.dragManager.currentDropTarget===_c55){
_c55.controller.expand(_c55.treeNode);
dojo.dnd.dragManager.cacheTargetLocations();
}
};
this.autoExpandTimer=dojo.lang.setTimeout(_c56,_c55.autoExpandDelay);
},getAcceptPosition:function(e,_c58){
var _c59=this.treeNode.tree.DndMode;
if(_c59&dojo.widget.TreeV3.prototype.DndModes.ONTO&&this.treeNode.actionIsDisabledNow(this.treeNode.actions.ADDCHILD)){
_c59&=~dojo.widget.TreeV3.prototype.DndModes.ONTO;
}
var _c5a=this.getPosition(e,_c59);
if(_c5a=="onto"){
return _c5a;
}
for(var i=0;i<_c58.length;i++){
var _c5c=_c58[i].dragSource;
if(_c5c.treeNode&&this.isAdjacentNode(_c5c.treeNode,_c5a)){
continue;
}
if(!this.controller.canMove(_c5c.treeNode?_c5c.treeNode:_c5c,this.treeNode.parent)){
return false;
}
}
return _c5a;
},onDropEnd:function(e){
this.clearAutoExpandTimer();
this.hideIndicator();
},onDragOut:function(e){
this.clearAutoExpandTimer();
this.hideIndicator();
},clearAutoExpandTimer:function(){
if(this.autoExpandTimer){
clearTimeout(this.autoExpandTimer);
this.autoExpandTimer=null;
}
},onDragMove:function(e,_c60){
var _c61=this.getAcceptPosition(e,_c60);
if(_c61){
this.showIndicator(_c61);
}
},isAdjacentNode:function(_c62,_c63){
if(_c62===this.treeNode){
return true;
}
if(_c62.getNextSibling()===this.treeNode&&_c63=="before"){
return true;
}
if(_c62.getPreviousSibling()===this.treeNode&&_c63=="after"){
return true;
}
return false;
},cacheNodeCoords:function(){
var node=this.treeNode.contentNode;
this.cachedNodeY=dojo.html.getAbsolutePosition(node).y;
this.cachedNodeHeight=dojo.html.getBorderBox(node).height;
},getPosition:function(e,_c66){
var _c67=e.pageY||e.clientY+dojo.body().scrollTop;
var relY=_c67-this.cachedNodeY;
var p=relY/this.cachedNodeHeight;
var _c6a="";
if(_c66&dojo.widget.TreeV3.prototype.DndModes.ONTO&&_c66&dojo.widget.TreeV3.prototype.DndModes.BETWEEN){
if(p<=0.33){
_c6a="before";
}else{
if(p<=0.66||this.treeNode.isExpanded&&this.treeNode.children.length&&!this.treeNode.isLastChild()){
_c6a="onto";
}else{
_c6a="after";
}
}
}else{
if(_c66&dojo.widget.TreeV3.prototype.DndModes.BETWEEN){
if(p<=0.5||this.treeNode.isExpanded&&this.treeNode.children.length&&!this.treeNode.isLastChild()){
_c6a="before";
}else{
_c6a="after";
}
}else{
if(_c66&dojo.widget.TreeV3.prototype.DndModes.ONTO){
_c6a="onto";
}
}
}
return _c6a;
},getTargetParentIndex:function(_c6b,_c6c){
var _c6d=_c6c=="before"?this.treeNode.getParentIndex():this.treeNode.getParentIndex()+1;
if(_c6b.treeNode&&this.treeNode.parent===_c6b.treeNode.parent&&this.treeNode.getParentIndex()>_c6b.treeNode.getParentIndex()){
_c6d--;
}
return _c6d;
},onDrop:function(e){
var _c6f=this.position;
var _c70=e.dragObject.dragSource;
var _c71,_c72;
if(_c6f=="onto"){
_c71=this.treeNode;
_c72=0;
}else{
_c72=this.getTargetParentIndex(_c70,_c6f);
_c71=this.treeNode.parent;
}
var r=this.getDropHandler(e,_c70,_c71,_c72)();
return r;
},getDropHandler:function(e,_c75,_c76,_c77){
var _c78;
var _c79=this;
_c78=function(){
var _c7a;
if(_c75.treeNode){
_c7a=_c79.controller.move(_c75.treeNode,_c76,_c77,true);
}else{
if(dojo.lang.isFunction(_c75.onDrop)){
_c75.onDrop(_c76,_c77);
}
var _c7b=_c75.getTreeNode();
if(_c7b){
_c7a=_c79.controller.createChild(_c76,_c77,_c7b,true);
}else{
_c7a=true;
}
}
if(_c7a instanceof dojo.Deferred){
var _c7c=_c7a.fired==0;
if(!_c7c){
_c79.handleDropError(_c75,_c76,_c77,_c7a);
}
return _c7c;
}else{
return _c7a;
}
};
return _c78;
},handleDropError:function(_c7d,_c7e,_c7f,_c80){
dojo.debug("TreeDropTargetV3.handleDropError: DND error occured");
dojo.debugShallow(_c80);
}});
dojo.provide("dojo.experimental");
dojo.experimental=function(_c81,_c82){
var _c83="EXPERIMENTAL: "+_c81;
_c83+=" -- Not yet ready for use.  APIs subject to change without notice.";
if(_c82){
_c83+=" "+_c82;
}
dojo.debug(_c83);
};
dojo.provide("dojo.widget.TreeExtension");
dojo.widget.defineWidget("dojo.widget.TreeExtension",[dojo.widget.HtmlWidget,dojo.widget.TreeCommon],function(){
this.listenedTrees={};
},{});
dojo.provide("dojo.widget.TreeDocIconExtension");
dojo.widget.defineWidget("dojo.widget.TreeDocIconExtension",[dojo.widget.TreeExtension],{templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/TreeDocIcon.css"),listenTreeEvents:["afterChangeTree","afterSetFolder","afterUnsetFolder"],listenNodeFilter:function(elem){
return elem instanceof dojo.widget.Widget;
},getnodeDocType:function(node){
var _c86=node.getnodeDocType();
if(!_c86){
_c86=node.isFolder?"Folder":"Document";
}
return _c86;
},setnodeDocTypeClass:function(node){
var reg=new RegExp("(^|\\s)"+node.tree.classPrefix+"Icon\\w+","g");
var _c89=dojo.html.getClass(node.iconNode).replace(reg,"")+" "+node.tree.classPrefix+"Icon"+this.getnodeDocType(node);
dojo.html.setClass(node.iconNode,_c89);
},onAfterSetFolder:function(_c8a){
if(_c8a.source.iconNode){
this.setnodeDocTypeClass(_c8a.source);
}
},onAfterUnsetFolder:function(_c8b){
this.setnodeDocTypeClass(_c8b.source);
},listenNode:function(node){
node.contentIconNode=document.createElement("div");
var _c8d=node.tree.classPrefix+"IconContent";
if(dojo.render.html.ie){
_c8d=_c8d+" "+node.tree.classPrefix+"IEIconContent";
}
dojo.html.setClass(node.contentIconNode,_c8d);
node.contentNode.parentNode.replaceChild(node.contentIconNode,node.expandNode);
node.iconNode=document.createElement("div");
dojo.html.setClass(node.iconNode,node.tree.classPrefix+"Icon"+" "+node.tree.classPrefix+"Icon"+this.getnodeDocType(node));
node.contentIconNode.appendChild(node.expandNode);
node.contentIconNode.appendChild(node.iconNode);
dojo.dom.removeNode(node.contentNode);
node.contentIconNode.appendChild(node.contentNode);
},onAfterChangeTree:function(_c8e){
var _c8f=this;
if(!_c8e.oldTree||!this.listenedTrees[_c8e.oldTree.widgetId]){
this.processDescendants(_c8e.node,this.listenNodeFilter,this.listenNode);
}
}});
if(!this["dojo"]){
alert("\"dojo/__package__.js\" is now located at \"dojo/dojo.js\". Please update your includes accordingly");
}
dojo.provide("ps.aa");
dojo.event.connect(dojo,"loaded",init);
function init(){
djConfig.isDebug=true;
djConfig.debugAtAllCosts=true;
if(window.__isAa){
ps.aa.controller.init();
}
}
ps=new function(){
};
ps.aa=new function(){
this.PAGE_CLASS="PsAaPage";
this.SLOT_CLASS="PsAaSlot";
this.SNIPPET_CLASS="PsAaSnippet";
this.FIELD_CLASS="PsAaField";
this.OBJECTID_ATTR="PsAaObjectId";
};
ps.aa.ObjectId=function(_c90){
this.widget=null;
this.idString=_c90;
if(dojo.string.startsWith(_c90,ps.aa.ObjectId.IMG_PREFIX,false)){
this.idString=_c90.substring(ps.aa.ObjectId.IMG_PREFIX.length,_c90.length);
this.widget=ps.aa.ObjectId.IMG_PREFIX;
}else{
if(dojo.string.startsWith(_c90,ps.aa.ObjectId.TREE_NODE_WIDGET,false)){
this.idString=_c90.substring(ps.aa.ObjectId.TREE_NODE_WIDGET.length,_c90.length);
this.widget=ps.aa.ObjectId.TREE_NODE_WIDGET;
}
}
this.idobj=dojo.json.evalJson(this.idString);
this.equals=function(_c91){
if((typeof _c91=="undefined")||_c91==null){
return false;
}else{
return this.idString==_c91.idString;
}
};
this.serialize=function(){
return this.idString;
};
this.clone=function(){
return new ps.aa.ObjectId(this.serialize());
};
this.toString=function(){
return this.serialize();
};
this.belongsToTheSameItem=function(_c92){
dojo.lang.assertType(_c92,ps.aa.ObjectId);
dojo.lang.assert(_c92.isSlotNode(),"Expected slot node, but got "+_c92);
dojo.lang.assert(this.isSlotNode(),"Can be called only on a slot node, not on "+this);
return this.getRelationshipId()||_c92.getRelationshipId()?this.getRelationshipId()===_c92.getRelationshipId():this.getContentId()===_c92.getContentId();
};
this.isPageNode=function(){
return this.idobj[ps.aa.ObjectId.NODE_TYPE]==0;
};
this.isSlotNode=function(){
return this.idobj[ps.aa.ObjectId.NODE_TYPE]==1;
};
this.isSnippetNode=function(){
return this.idobj[ps.aa.ObjectId.NODE_TYPE]==2;
};
this.setSnippetNode=function(){
this.idobj[ps.aa.ObjectId.NODE_TYPE]=2;
this._resetIdString();
};
this.isFieldNode=function(){
return this.idobj[ps.aa.ObjectId.NODE_TYPE]==3;
};
this.isCheckout=function(){
if(this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS]!="0"){
return true;
}
return false;
};
this.isCheckoutByMe=function(){
return this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS]=="1";
};
this.getContentId=function(){
return this.idobj[ps.aa.ObjectId.CONTENT_ID];
};
this.setContentId=function(id){
this.idobj[ps.aa.ObjectId.CONTENT_ID]=id;
this._resetIdString();
};
this.setCheckoutStatus=function(_c94){
dojo.lang.assert((_c94==="0"||_c94==="1"||_c94==="2"),"status must be 0, 1, or 2");
this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS]=_c94;
this._resetIdString();
};
this.getCheckoutStatus=function(_c95){
return this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS];
};
this.getTemplateId=function(){
return this.idobj[ps.aa.ObjectId.TEMPLATE_ID];
};
this.setTemplateId=function(_c96){
this.idobj[ps.aa.ObjectId.TEMPLATE_ID]=_c96;
this._resetIdString();
};
this.getSlotId=function(){
return this.idobj[ps.aa.ObjectId.SLOT_ID];
};
this.setSlotId=function(_c97){
this.idobj[ps.aa.ObjectId.SLOT_ID]=_c97;
this._resetIdString();
};
this._resetIdString=function(){
this.idString=dojo.json.serialize(this.idobj);
};
this.getRelationshipId=function(){
return this.idobj[ps.aa.ObjectId.RELATIONSHIP_ID];
};
this.getContext=function(){
return this.idobj[ps.aa.ObjectId.CONTEXT];
};
this.getAuthType=function(){
return this.idobj[ps.aa.ObjectId.AUTHTYPE];
};
this.getSiteId=function(){
return this.idobj[ps.aa.ObjectId.SITE_ID];
};
this.setSiteId=function(id){
this.idobj[ps.aa.ObjectId.SITE_ID]=id;
this._resetIdString();
};
this.getFolderId=function(){
return this.idobj[ps.aa.ObjectId.FOLDER_ID];
};
this.setFolderId=function(id){
this.idobj[ps.aa.ObjectId.FOLDER_ID]=id;
this._resetIdString();
};
this.getContentTypeId=function(){
return this.idobj[ps.aa.ObjectId.CONTENTTYPE_ID];
};
this.getFieldName=function(){
return this.idobj[ps.aa.ObjectId.FIELD_NAME];
};
this.getFieldLabel=function(){
return this.idobj[ps.aa.ObjectId.FIELD_LABEL];
};
this.getSortRank=function(){
return this.idobj[ps.aa.ObjectId.SORT_RANK];
};
this.setSortRank=function(_c9a){
this.idobj[ps.aa.ObjectId.SORT_RANK]=_c9a+"";
this._resetIdString();
};
this.getParentId=function(){
return this.idobj[ps.aa.ObjectId.PARENT_ID];
};
this.getTreeNodeWidgetId=function(){
return ps.aa.ObjectId.TREE_NODE_WIDGET+this.serialize();
};
this.getImagePath=function(path){
if(path==null){
path="";
}
if(path.substring(path.length-1)!="/"){
path=path+"/";
}
var _c9c;
if(this.isPageNode()){
_c9c=ps.aa.PAGE_CLASS;
}else{
if(this.isSnippetNode()){
_c9c=ps.aa.SNIPPET_CLASS;
}else{
if(this.isSlotNode()){
_c9c=ps.aa.SLOT_CLASS;
}else{
if(this.isFieldNode()){
_c9c=ps.aa.FIELD_CLASS;
}
}
}
}
return path+ps.aa.ObjectId.ImageNames[_c9c]+"_"+this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS]+".gif";
};
this.getAnchorId=function(){
return "img."+this.serialize();
};
};
ps.aa.ObjectId.NODE_TYPE=0;
ps.aa.ObjectId.CONTENT_ID=1;
ps.aa.ObjectId.TEMPLATE_ID=2;
ps.aa.ObjectId.SITE_ID=3;
ps.aa.ObjectId.FOLDER_ID=4;
ps.aa.ObjectId.CONTEXT=5;
ps.aa.ObjectId.AUTHTYPE=6;
ps.aa.ObjectId.CONTENTTYPE_ID=7;
ps.aa.ObjectId.CHECKOUT_STATUS=8;
ps.aa.ObjectId.SLOT_ID=9;
ps.aa.ObjectId.RELATIONSHIP_ID=10;
ps.aa.ObjectId.FIELD_NAME=11;
ps.aa.ObjectId.PARENT_ID=12;
ps.aa.ObjectId.FIELD_LABEL=13;
ps.aa.ObjectId.SORT_RANK=14;
ps.aa.ObjectId.TREE_NODE_WIDGET="aatree_";
ps.aa.ObjectId.IMG_PREFIX="img.";
ps.aa.ObjectId.ImageNames=new Object();
ps.aa.ObjectId.ImageNames[ps.aa.PAGE_CLASS]="page";
ps.aa.ObjectId.ImageNames[ps.aa.SNIPPET_CLASS]="snippet";
ps.aa.ObjectId.ImageNames[ps.aa.SLOT_CLASS]="slot";
ps.aa.ObjectId.ImageNames[ps.aa.FIELD_CLASS]="field";
dojo.provide("ps.aa.dnd");
ps.aa.dnd=new function(){
this.init=function(){
dojo.dnd.dragManager.nestedTargets=true;
dojo.event.connect(ps.aa.controller.treeModel,"onBeforeDomChange",this,"_onBeforeDomChange");
dojo.event.connect(ps.aa.controller.treeModel,"onDomChanged",this,"_onDomChanged");
this._onDomChanged(ps.aa.controller.pageId);
};
this._onBeforeDomChange=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
function unregisterAll(ids,_c9f){
dojo.lang.forEach(ids,function(id){
dojo.lang.assert(id.serialize() in _c9f,"Following id is not registered: "+id.serialize());
_c9f[id.serialize()].unregister();
delete _c9f[id.serialize()];
});
}
unregisterAll(this._getSnippetIds(id),this.dragSources);
unregisterAll(this._getSlotIds(id),this.dropTargets);
};
this._onDomChanged=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
var _ca2=this;
dojo.lang.forEach(this._getSnippetIds(id),function(id){
dojo.lang.assert(!(id.serialize() in _ca2.dragSources));
var node=ps.aa.Page.getElement(id);
var _ca5=new dojo.dnd.HtmlDragSource(node,ps.aa.SNIPPET_CLASS);
_ca2.dragSources[id.serialize()]=_ca5;
});
dojo.lang.forEach(this._getSlotIds(id),function(id){
dojo.lang.assert(!(id.serialize() in _ca2.dropTargets));
var node=ps.aa.Page.getElement(id);
var _ca8=new dojo.dnd.HtmlDropTarget(node,ps.aa.SNIPPET_CLASS);
_ca2.dropTargets[id.serialize()]=_ca8;
dojo.event.connectAround(_ca8,"onDragMove",_ca2,"_resetDropTargetVertical");
dojo.event.connectAround(_ca8,"onDragOver",_ca2,"_onDragOver");
dojo.lang.assert(_ca8.insert);
dojo.event.connectAround(_ca8,"insert",_ca2,"_dropTargetInsert");
dojo.event.connectAround(_ca8,"createDropIndicator",_ca2,"_createDropIndicator");
dojo.event.connect(_ca8,"onDropEnd",_ca2,"_onDropEnd");
});
};
this._onDragOver=function(_ca9){
dojo.lang.assert(_ca9,"Invocation must be defined");
if(!_ca9.proceed()){
return false;
}
var _caa=_ca9.args[0];
dojo.lang.assert(_caa,"Event must be specified.");
var _cab=_caa.dragObjects[0];
var _cac=this._getParentSlotId(_cab.domNode);
var _cad=_ca9.object;
var _cae=this._getDropTargetId(_cad);
dojo.lang.assert(_cac);
dojo.lang.assert(_cae);
if(!_cac.belongsToTheSameItem(_cae)){
return false;
}
this._resetTargetChildBoxes(_cad);
_cad.vertical=this._isDropIndicatorVertical(_cad);
return true;
};
this._resetDropTargetVertical=function(_caf){
var _cb0=_caf.object;
var _cb1=this._getDropTargetId(_cb0);
dojo.lang.assert(_cb1,"Object id should be specified on the drop target");
_cb0.vertical=this._isDropIndicatorVertical(_cb0);
return _caf.proceed();
};
this._resetTargetChildBoxes=function(_cb2){
dojo.lang.assert(_cb2,"Drop target expected");
var _cb3=_cb2.childBoxes;
if(!_cb3||_cb3.length===0){
return;
}
var _cb4=this._getDropTargetId(_cb2);
var _cb5=dojo.lang.filter(this._getSnippetIds(_cb4),function(id){
return id.getSlotId()===_cb4.getSlotId();
});
var _cb7=dojo.lang.map(_cb5,function(id){
return id.serialize();
});
var divs=_cb2.domNode.getElementsByTagName("div");
var _cba=dojo.lang.filter(divs,function(div){
return div.id&&div.className==ps.aa.SNIPPET_CLASS&&dojo.lang.inArray(_cb7,div.id);
});
_cba.sort(function(n1,n2){
var id1=new ps.aa.ObjectId(n1.id);
var id2=new ps.aa.ObjectId(n2.id);
return id1.getSortRank()-id2.getSortRank();
});
_cb3.length=0;
var _cc0=this;
dojo.lang.forEach(_cba,function(_cc1){
_cb3.push(_cc0._getDropTargetChildBox(_cc1));
});
this._fillDropTargetChildBoxesGaps(_cb3);
};
this._fillDropTargetChildBoxesGaps=function(_cc2){
dojo.lang.forEach(_cc2,function(box){
var _cc4=null;
var _cc5=null;
dojo.lang.forEach(_cc2,function(box2){
if(box===box2){
return;
}
if(Math.max(box.top,box2.top)<Math.min(box.bottom,box2.bottom)&&box2.right>box.right){
if(!_cc4||_cc4>box2.left){
_cc4=box2.left;
}
}
if(Math.max(box.left,box2.left)<Math.min(box.right,box2.right)&&box2.bottom>box.bottom){
if(!_cc5||_cc5>box2.top){
_cc5=box2.top;
}
}
});
if(_cc4&&_cc4>box.right+1){
box.right=_cc4-1;
}
if(_cc5&&_cc5>box.bottom+1){
box.bottom=_cc5-1;
}
});
};
this._dropTargetInsert=function(_cc7){
dojo.lang.assert(_cc7);
var _cc8=_cc7.args[0];
var _cc9=_cc7.args[1];
var _cca=_cc7.args[2];
dojo.lang.assert(_cc8,"Event must be specified.");
dojo.lang.assert(_cc9,"Reference node must be specified.");
dojo.lang.assert(_cca,"Position must be specified.");
this._assertValidPosition(_cca);
var _ccb=_cc8.dragObject.domNode;
var _ccc=new ps.aa.ObjectId(_ccb.id);
var _ccd=this._getParentSlotId(_ccb);
var _cce=_cc7.object;
var _ccf=this._getDropTargetId(_cce);
var _cd0=this._getDropIndex(_cce,_ccc,_cc9,_cca);
if(_ccc.getSlotId()!==_ccf.getSlotId()){
dojo.lang.assert(!this._m_move);
this._m_move=new ps.aa.SnippetMove(_ccc,_ccd,_ccf,_cd0,true);
var _cd1=ps.aa.controller.moveToSlot(this._m_move);
}else{
if(ps.aa.controller.reorderSnippetInSlot(_ccc,_cd0)){
dojo.lang.assert(!this._m_move);
this._m_move=new ps.aa.SnippetMove(_ccc,_ccd,_ccf,_cd0,true);
this._m_move.setUiUpdateNeeded(true);
this._m_move.setSuccess(true);
var _cd1=true;
}else{
var _cd1=false;
}
}
if(_cce.dropIndicator){
dojo.html.removeNode(_cce.dropIndicator);
delete _cce.dropIndicator;
}
return _cd1&&_cc7.proceed();
};
this._onDropEnd=function(){
if(this._m_move){
dojo.lang.assertType(this._m_move.isSuccess(),Boolean);
this._m_move.setDontUpdatePage(false);
if(this._m_move.isUiUpdateNeeded()&&this._m_move.isSuccess()){
ps.aa.controller.maybeRefreshMovedSnippetNode(this._m_move,true);
var _cd2=this._m_move.getTargetSnippetId();
ps.aa.controller.activate(ps.aa.Page.getElement(_cd2));
}
this._m_move=null;
}
};
this._getDropIndex=function(_cd3,_cd4,_cd5,_cd6){
dojo.lang.assert(_cd3,"Target must be specified");
dojo.lang.assertType(_cd4,ps.aa.ObjectId);
dojo.lang.assert(_cd5,"Reference node must be specified");
this._assertValidPosition(_cd6);
var _cd7=_cd3.childBoxes.length;
for(var i=0,_cd9=1;i<_cd7;i++){
var _cda=_cd3.childBoxes[i];
if(_cda.node===_cd5&&_cd6!==this.POS_APPEND){
if(_cd6!=this.POS_BEFORE){
_cd9++;
}
return Math.min(_cd9,_cd7);
}
if(_cda.node.id!==_cd4.serialize()){
_cd9++;
}
}
if(_cd6===this.POS_APPEND){
return _cd9;
}
dojo.debug(_cd5);
dojo.lang.assert(false,"Could not find reference node in the list of nodes");
};
this._isDropIndicatorVertical=function(_cdb){
dojo.lang.assert(_cdb,"Drop target must be specified");
if(_cdb.childBoxes.length<2){
return false;
}
var box0=_cdb.childBoxes[0];
var box1=_cdb.childBoxes[1];
return Math.abs(box0.left-box1.left)>Math.abs(box0.top-box1.top);
};
this._createDropIndicator=function(_cde){
dojo.lang.assert(_cde);
_cde.proceed();
var _cdf=_cde.object;
dojo.lang.assert(_cdf.dropIndicator);
var _ce0=_cdf.dropIndicator.style;
var _ce1="gray";
var _ce2="3px";
if(_cdf.vertical){
_ce0.borderLeftWidth=_ce2;
_ce0.borderLeftColor=_ce1;
}else{
_ce0.borderTopWidth=_ce2;
_ce0.borderTopColor=_ce1;
}
};
this._getParentSlotId=function(_ce3){
dojo.lang.assert(_ce3,"Dom node is not specified");
var node=_ce3;
while(node){
if(node.nodeType===dojo.dom.ELEMENT_NODE&&node.className===ps.aa.SLOT_CLASS){
return new ps.aa.ObjectId(node.id);
}else{
node=node.parentNode;
}
}
return null;
};
this._assertValidPosition=function(_ce5){
dojo.lang.assert(_ce5===this.POS_BEFORE||_ce5===this.POS_AFTER||_ce5===this.POS_APPEND,"Unrecognized position: "+_ce5);
};
this._getDropTargetChildBox=function(_ce6){
var pos=dojo.html.getAbsolutePosition(_ce6,true);
var _ce8=dojo.html.getBorderBox(_ce6);
return {top:pos.y,bottom:pos.y+_ce8.height,left:pos.x,right:pos.x+_ce8.width,height:_ce8.height,width:_ce8.width,node:_ce6};
};
this._getSlotIds=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
var ids=ps.aa.controller.treeModel.getIdsFromNodeId(id).toArray();
return dojo.lang.filter(ids,function(id){
return id.isSlotNode();
});
};
this._getSnippetIds=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
var ids=ps.aa.controller.treeModel.getIdsFromNodeId(id).toArray();
return dojo.lang.filter(ids,function(id){
return id.isSnippetNode();
});
};
this._getDropTargetId=function(_cef){
dojo.lang.assert(_cef,"Expected dropTarget");
return new ps.aa.ObjectId(_cef.domNode.id);
};
this.POS_BEFORE="before";
this.POS_AFTER="after";
this.POS_APPEND="append";
this.dragSources={};
this.dropTargets={};
};
dojo.provide("ps.io.Response");
ps.io.Response=function(){
this.getValue=function(){
return this._m_value;
};
this.isSuccess=function(){
return this._m_success;
};
this.getErrorCode=function(){
return this._m.errorcode;
};
this._m_success=false;
this._m_value=null;
this._m_errorcode=null;
};
dojo.provide("ps.io.Actions");
ps.io.Actions=new function(){
this.MIMETYPE_PLAIN="text/plain";
this.MIMETYPE_JSON="text/json";
this.MIMETYPE_HTML="text/html";
this.MIMETYPE_XML="text/xml";
this.formSubmitResults=null;
this.localeCount=-1;
this.move=function(_cf0,mode,_cf2){
dojo.lang.assertType(_cf0,ps.aa.ObjectId);
var _cf3="Move";
var _cf4=new dojo.collections.Dictionary();
_cf4.add("mode",mode);
if(_cf2!=null&&_cf2!=undefined){
_cf4.add("index",_cf2);
}
return this._makeRequest(_cf3,this.MIMETYPE_PLAIN,_cf0.serialize(),_cf4);
};
this.moveToSlot=function(_cf5,_cf6,_cf7,_cf8){
dojo.lang.assertType(_cf5,ps.aa.ObjectId);
var _cf9="MoveToSlot";
var _cfa=new dojo.collections.Dictionary();
_cfa.add("newslotid",_cf6);
if(_cf7){
_cfa.add("newtemplate",_cf7);
}
if(_cf8){
_cfa.add("index",_cf8);
}
return this._makeRequest(_cf9,this.MIMETYPE_PLAIN,_cf5.serialize(),_cfa);
};
this.getUrl=function(_cfb,_cfc){
dojo.lang.assertType(_cfb,ps.aa.ObjectId);
var _cfd="GetUrl";
var _cfe=new dojo.collections.Dictionary();
_cfe.add("actionname",_cfc);
try{
if(___sys_aamode!=undefined&&___sys_aamode!=null){
_cfe.add("sys_aamode",new String(___sys_aamode));
}
}
catch(ignore){
}
return this._makeRequest(_cfd,this.MIMETYPE_JSON,_cfb.serialize(),_cfe);
};
this.getActionVisibility=function(_cff,_d00){
var o=this._normalizeNames(_cff);
if(o instanceof ps.io.Response){
return o;
}
if(typeof _d00=="undefined"||_d00==null){
var _d02=new ps.io.Response();
_d02._m_success=true;
_d02._m_value=new Object();
return _d02;
}
var _d03="GetActionVisibility";
var _d04=new dojo.collections.Dictionary();
_d04.add("names",o.names);
var _d02=this._makeRequest(_d03,this.MIMETYPE_JSON,_d00.serialize(),_d04);
if(_d02.isSuccess()){
_d02._m_value=_d02._m_value[0];
}
return _d02;
};
this._normalizeNames=function(_d05){
if(typeof _d05=="undefined"||_d05==null||(typeof _d05=="array"&&empty(_d05))){
var _d06=new ps.io.Response();
_d06._m_success=true;
_d06._m_value=new Object();
return _d06;
}
var p=new Object();
if(typeof _d05=="string"){
p.names={actionNames:null};
}else{
if(typeof _d05=="object"){
if(_d05 instanceof String){
p.names={actionNames:null};
}else{
p.names=_d05;
}
}
}
return p;
};
this.getActionLabels=function(_d08){
var o=this._normalizeNames(_d08);
if(o instanceof ps.io.Response){
return o;
}
var _d0a="GetActionLabels";
var _d0b=new dojo.collections.Dictionary();
_d0b.add("names",o.names);
return this._makeRequest(_d0a,this.MIMETYPE_JSON,null,_d0b);
};
this.getAllowedContentTypeForSlot=function(_d0c){
dojo.lang.assertType(_d0c,ps.aa.ObjectId);
var _d0d="GetAllowedContentTypeForSlot";
return this._makeRequest(_d0d,this.MIMETYPE_JSON,_d0c.serialize(),null);
};
this.getContentTypeByContentId=function(_d0e){
var _d0f="GetContentTypeByContentId";
var _d10=new dojo.collections.Dictionary();
_d10.add("sys_contentid",_d0e);
return this._makeRequest(_d0f,this.MIMETYPE_JSON,null,_d10);
};
this.getTemplateImagesForContentType=function(_d11,_d12){
dojo.lang.assert(_d11);
dojo.lang.assertType(_d12,ps.aa.ObjectId);
var _d13="GetTemplateImagesForContentType";
var _d14=new dojo.collections.Dictionary();
_d14.add("sys_contenttypeid",_d11);
return this._makeRequest(_d13,this.MIMETYPE_JSON,_d12.serialize(),_d14);
};
this.createItem=function(_d15,_d16,_d17,_d18){
dojo.lang.assert(_d15);
dojo.lang.assert(_d16);
dojo.lang.assert(_d18);
var _d19="CreateItem";
var _d1a=new dojo.collections.Dictionary();
_d1a.add("sys_contenttypeid",_d15);
_d1a.add("folderPath",_d16);
_d1a.add("itemPath",_d17);
_d1a.add("itemTitle",_d18);
return this._makeRequest(_d19,this.MIMETYPE_JSON,null,_d1a);
};
this.getItemPath=function(_d1b){
dojo.lang.assertType(_d1b,ps.aa.ObjectId);
var _d1c="GetItemPath";
return this._makeRequest(_d1c,this.MIMETYPE_PLAIN,_d1b.serialize(),null);
};
this.canManageNav=function(_d1d){
dojo.lang.assertType(_d1d,ps.aa.ObjectId);
var _d1e="CanManageNav";
return this._makeRequest(_d1e,this.MIMETYPE_PLAIN,_d1d.serialize(),null);
};
this.getWorkflowActions=function(_d1f){
dojo.lang.assertType(_d1f,ps.aa.ObjectId);
var _d20="GetWorkFlowActions";
return this._makeRequest(_d20,this.MIMETYPE_JSON,_d1f.serialize(),null);
};
this.getIdByPath=function(path){
var _d22="GetIdByPath";
var _d23=new dojo.collections.Dictionary();
_d23.add("path",path);
return this._makeRequest(_d22,this.MIMETYPE_JSON,null,_d23);
};
this.getAllowedSnippetTemplates=function(_d24){
dojo.lang.assertType(_d24,ps.aa.ObjectId);
var _d25="GetAllowedSnippetTemplates";
return this._makeRequest(_d25,this.MIMETYPE_JSON,_d24.serialize(),null);
};
this.getItemTemplatesForSlot=function(_d26){
dojo.lang.assertType(_d26,ps.aa.ObjectId);
var _d27="GetItemTemplatesForSlot";
return this._makeRequest(_d27,this.MIMETYPE_JSON,_d26.serialize(),null);
};
this.getFieldContent=function(_d28,_d29){
dojo.lang.assertType(_d28,ps.aa.ObjectId);
var _d2a="GetFieldContent";
var _d2b=null;
if(_d29){
_d2b=new dojo.collections.Dictionary();
_d2b.add("isaamode","true");
if(___sys_aamode!=undefined&&___sys_aamode!=null){
_d2b.add("sys_aamode",new String(___sys_aamode));
}
}
return this._makeRequest(_d2a,this.MIMETYPE_HTML,_d28.serialize(),_d2b);
};
this.getSlotContent=function(_d2c,_d2d){
dojo.lang.assertType(_d2c,ps.aa.ObjectId);
var _d2e="GetSlotContent";
var _d2f=null;
if(_d2d){
_d2f=new dojo.collections.Dictionary();
_d2f.add("isaamode","true");
if(___sys_aamode!=undefined&&___sys_aamode!=null){
_d2f.add("sys_aamode",new String(___sys_aamode));
}
}
return this._makeRequest(_d2e,this.MIMETYPE_HTML,_d2c.serialize(),_d2f);
};
this.getSnippetContent=function(_d30,_d31,_d32){
dojo.lang.assertType(_d30,ps.aa.ObjectId);
var _d33="GetSnippetContent";
var _d34=null;
if(_d31){
_d34=new dojo.collections.Dictionary();
_d34.add("isaamode","true");
if(___sys_aamode!=undefined&&___sys_aamode!=null){
_d34.add("sys_aamode",new String(___sys_aamode));
}
}
if(_d32!=undefined&&_d32!=null&&_d32.length>0){
if(_d34==null){
_d34=new dojo.collections.Dictionary();
}
_d34.add("rxselectedtext",encodeURIComponent(_d32));
}
return this._makeRequest(_d33,this.MIMETYPE_HTML,_d30.serialize(),_d34);
};
this.convertLinksToManaged=function(_d35,_d36,_d37){
var _d38="ConvertLinksToManaged";
var _d39=null;
_d36=_d36==undefined||_d36==null||_d36.length==0?"-1":_d36;
_d37=_d37==undefined||_d37==null||_d37.length==0?"-1":_d37;
_d39=new dojo.collections.Dictionary();
if(_d35!=undefined&&_d35!=null&&_d35.length>0){
_d39.add("content",encodeURIComponent(_d35));
}
_d39.add("sys_contentid",_d36);
_d39.add("sys_folderid",_d37);
return this._makeRequest(_d38,this.MIMETYPE_HTML,null,_d39);
};
this.getSnippetMimeType=function(_d3a){
dojo.lang.assertType(_d3a,ps.aa.ObjectId);
var _d3b="GetSnippetMimeType";
return this._makeRequest(_d3b,this.MIMETYPE_JSON,_d3a.serialize(),null);
};
this.getRenderedSlotContent=function(_d3c,_d3d){
dojo.lang.assertType(_d3c,ps.aa.ObjectId);
var _d3e="GetSnippetPickerSlotContent";
var _d3f=null;
if(_d3d){
_d3f=new dojo.collections.Dictionary();
_d3f.add("isTitles","true");
}
return this._makeRequest(_d3e,this.MIMETYPE_HTML,_d3c.serialize(),_d3f);
};
this.removeSnippet=function(_d40){
var _d41="RemoveSnippet";
var _d42=new dojo.collections.Dictionary();
_d42.add("relationshipIds",_d40);
return this._makeRequest(_d41,this.MIMETYPE_PLAIN,null,_d42);
};
this.addSnippet=function(_d43,_d44,_d45,_d46){
dojo.lang.assertType(_d43,ps.aa.ObjectId);
dojo.lang.assertType(_d44,ps.aa.ObjectId);
_d45&&dojo.lang.assertType(_d45,String);
_d46&&dojo.lang.assertType(_d46,String);
var _d47="AddSnippet";
var _d48=new dojo.collections.Dictionary();
_d48.add("dependentId",_d43.getContentId());
_d48.add("templateId",_d43.getTemplateId());
_d48.add("ownerId",_d44.getContentId());
_d48.add("slotId",_d44.getSlotId());
this.addOptionalParam(_d48,"folderPath",_d45);
this.addOptionalParam(_d48,"siteName",_d46);
return this._makeRequest(_d47,this.MIMETYPE_JSON,null,_d48);
};
this.checkInItem=function(_d49,_d4a){
var _d4b="Workflow";
var _d4c=new dojo.collections.Dictionary();
_d4c.add("operation","checkIn");
_d4c.add("contentId",_d49);
this.addOptionalParam(_d4c,"comment",_d4a);
return this._makeRequest(_d4b,this.MIMETYPE_PLAIN,null,_d4c);
};
this.checkOutItem=function(_d4d,_d4e){
var _d4f="Workflow";
var _d50=new dojo.collections.Dictionary();
_d50.add("operation","checkOut");
_d50.add("contentId",_d4d);
this.addOptionalParam(_d50,"comment",_d4e);
return this._makeRequest(_d4f,this.MIMETYPE_PLAIN,null,_d50);
};
this.transitionCheckOutItem=function(_d51,_d52,_d53,_d54){
var _d55="Workflow";
var _d56=new dojo.collections.Dictionary();
_d56.add("operation","transition_checkout");
_d56.add("contentId",_d51);
_d56.add("triggerName",_d52);
this.addOptionalParam(_d56,"comment",_d53);
this.addOptionalParam(_d56,"adHocUsers",_d54);
return this._makeRequest(_d55,this.MIMETYPE_PLAIN,null,_d56);
};
this.transitionItem=function(_d57,_d58,_d59,_d5a){
var _d5b="Workflow";
var _d5c=new dojo.collections.Dictionary();
_d5c.add("operation","transition");
_d5c.add("contentId",_d57);
_d5c.add("triggerName",_d58);
this.addOptionalParam(_d5c,"comment",_d59);
this.addOptionalParam(_d5c,"adHocUsers",_d5a);
return this._makeRequest(_d5b,this.MIMETYPE_PLAIN,null,_d5c);
};
this.addOptionalParam=function(_d5d,key,_d5f){
if(_d5f!=null&&(!dojo.lang.isUndefined(_d5f))){
_d5d.add(key,_d5f);
}
};
this.getItemSortRank=function(_d60){
var _d61="GetItemSortRank";
var _d62=new dojo.collections.Dictionary();
_d62.add("sys_relationshipid",_d60);
return this._makeRequest(_d61,this.MIMETYPE_PLAIN,null,_d62);
};
this.getServerProperties=function(){
var _d63="GetServerProperties";
return this._makeRequest(_d63,this.MIMETYPE_JSON,null,null);
};
this.getSites=function(){
var _d64="GetSites";
return this._makeRequest(_d64,this.MIMETYPE_JSON,null,null);
};
this.getRootFolders=function(){
var _d65="GetRootFolders";
return this._makeRequest(_d65,this.MIMETYPE_JSON,null,null);
};
this.resolveSiteFolders=function(_d66,_d67){
var _d68="ResolveSiteFolders";
var _d69=new dojo.collections.Dictionary();
_d69.add("folderPath",_d67);
_d69.add("siteName",_d66);
return this._makeRequest(_d68,this.MIMETYPE_JSON,null,_d69);
};
this.createFolder=function(_d6a,name,_d6c){
var _d6d="CreateFolder";
var _d6e=new dojo.collections.Dictionary();
_d6e.add("parentFolderPath",_d6a);
_d6e.add("folderName",name);
_d6e.add("category",_d6c?"sites":"folders");
return this._makeRequest(_d6d,this.MIMETYPE_JSON,null,_d6e);
};
this.getFolderChildren=function(_d6f,_d70,_d71,_d72){
var _d73="GetChildren";
var _d74=new dojo.collections.Dictionary();
_d74.add("parentFolderPath",_d6f);
_d74.add("sys_contenttypeid",_d70);
if(_d72){
_d74.add("sys_slotid",_d71);
}
_d74.add("category",_d72?"sites":"folders");
return this._makeRequest(_d73,this.MIMETYPE_JSON,null,_d74);
};
this.getCreateItemUrl=function(_d75,_d76,_d77){
var _d78="GetCreateItemUrl";
var _d79=new dojo.collections.Dictionary();
_d79.add("parentFolderPath",_d75);
_d79.add("sys_contenttypeid",_d76);
_d79.add("category",_d77?"sites":"folders");
return this._makeRequest(_d78,this.MIMETYPE_JSON,null,_d79);
};
this.getInlinelinkParentIds=function(_d7a,_d7b){
var _d7c="GetInlinelinkParents";
var _d7d=new dojo.collections.Dictionary();
_d7d.add("dependentId",_d7a);
_d7d.add("managedIds",dojo.json.serialize(_d7b));
return this._makeRequest(_d7c,this.MIMETYPE_JSON,null,_d7d);
};
this.getContentEditorFieldValue=function(_d7e){
dojo.lang.assertType(_d7e,ps.aa.ObjectId);
var _d7f="GetContentEditorFieldValue";
return this._makeRequest(_d7f,this.MIMETYPE_HTML,_d7e.serialize(),null);
};
this.setContentEditorFieldValue=function(_d80,_d81){
dojo.lang.assertType(_d80,ps.aa.ObjectId);
var _d82="SetContentEditorFieldValue";
var _d83=new dojo.collections.Dictionary();
_d83.add("fieldValue",_d81);
return this._makeRequest(_d82,this.MIMETYPE_JSON,_d80.serialize(),_d83);
};
this.getUpdateItemUrl=function(){
return this._buildRequestUrl("UpdateItem",null);
};
this.getMaxTimeout=function(){
var _d84="GetMaxTimeout";
return this._makeRequest(_d84,this.MIMETYPE_PLAIN,null,null);
};
this.getLocaleCount=function(){
if(this.localeCount>-1){
return this.localeCount;
}
var _d85="GetLocaleCount";
var _d86=this._makeRequest(_d85,this.MIMETYPE_PLAIN,null,null);
if(_d86.isSuccess()){
this.localeCount=parseInt(_d86.getValue());
}else{
localeCount=1;
}
return this.localeCount;
};
this.getRcSearchUrl=function(){
return this._buildRequestUrl("GetSearchResults",null);
};
this.submitForm=function(_d87){
this.formSubmitResults=null;
var fn=this.rcFormBind.bindArgs.formNode;
var fid=(typeof (fn)=="string")?fn:fn.id;
if(_d87.id!=fid){
alert("Error occured submitting the form. The form is not bound.");
return this.formSubmitResults;
}
_d87.onsubmit();
return this.formSubmitResults;
};
this.initFormBind=function(_d8a,_d8b,_d8c){
var _d8d=this;
var _d8e=new ps.io.Response();
this.rcFormBind=new dojo.io.FormBind({url:_d8a,sync:true,useCache:false,preventCache:true,mimetype:_d8c,formNode:_d8b,load:function(load,data,e){
if(load=="error"){
_d8e._m_success=false;
var msg=ps.io.Actions._parseError(data.message,e);
_d8e._m_value=msg.message;
_d8e._m_errorcode=msg.errorCode;
}else{
_d8e._m_success=true;
if(typeof (data)=="object"){
_d8e._m_value=ps.io.Actions._flattenArrayProperties(data);
}else{
_d8e._m_value=data;
}
}
_d8d.formSubmitResults=_d8e;
}});
};
this.test=function(mode){
var _d94="Test";
var _d95=new dojo.collections.Dictionary();
_d95.add("mode",mode);
return this._makeRequest(_d94,this.MIMETYPE_PLAIN,null,_d95);
};
this.keepAlive=function(){
var _d96=this.getMaxTimeout();
if(!_d96.isSuccess()){
return;
}
var _d97=parseInt(_d96.getValue())*900;
setTimeout("ps.io.Actions.keepAlive()",_d97);
};
this._buildRequestUrl=function(_d98,_d99){
var base=__rxroot+"/contentui/aa?action="+_d98;
if(_d99!=null&&_d99!=undefined){
base+="&objectId="+escape(_d99);
}
return base;
};
this._makeRequest=function(_d9b,_d9c,_d9d,_d9e){
var _d9f="XMLHttpTransport Error:";
var _da0=new ps.io.Response();
var _da1=this._buildRequestUrl(_d9b,_d9d);
var _da2=new Object();
if(_d9e!=null&&_d9e!=undefined){
var keys=_d9e.getKeyList();
for(i=0;i<keys.length;i++){
var val=_d9e.item(keys[i]);
if(val==null||val==""||val==undefined){
continue;
}
_da2[keys[i]]=val;
}
}
dojo.io.bind({url:_da1,useCache:false,preventCache:true,mimetype:_d9c,method:"POST",content:_da2,sync:true,handler:function(type,data,e){
if(type=="error"){
_da0._m_success=false;
var msg=ps.io.Actions._parseError(data.message,e);
_da0._m_value=msg.message;
_da0._m_errorcode=msg.errorCode;
}else{
_da0._m_success=true;
_da0._m_value=ps.io.Actions._flattenArrayProperties(data);
}
}});
return _da0;
};
this._parseError=function(_da9,e){
var _dab=new Object();
var _dac="XMLHttpTransport Error: ";
if(dojo.string.startsWith(_da9,_dac)){
var temp=_da9.substring(_dac.length);
var _dae=parseInt(temp.substring(0,3));
var msg=temp.substring(4);
_dab.errorCode=_dae;
if(_dae===404||_dae===0){
msg=ps.io.Actions.ERROR_MSG_NO_SERVER;
_dae=404;
}else{
if(_dae===500&&dojo.string.has(e.responseText,ps.io.Actions.SESS_NOTAUTH_TEXT)){
msg=ps.io.Actions.ERROR_MSG_REQUIRES_AUTH;
}
}
_dab.message=msg;
}else{
_dab.errorCode="unknown";
_dab.message=_da9;
}
return _dab;
};
this._flattenArrayProperties=function(obj){
if(typeof (obj)!="object"){
return obj;
}
var _db1=dojo.lang.isArrayLike(obj)?[]:new Object();
for(var item in obj){
var prop=obj[item];
if(dojo.lang.isArrayLike(prop)&&prop.length==1){
_db1[item]=prop[0];
}else{
_db1[item]=prop;
}
}
return _db1;
};
this.maybeReportActionError=function(_db4){
dojo.lang.assertType(_db4,ps.io.Response);
if(!_db4.isSuccess()){
ps.error(_db4.getValue());
}
};
this.NEEDS_TEMPLATE_ID="needs_template_id";
this.SESS_NOTAUTH_TEXT="Processing Error: Not Authenticated";
this.ERROR_MSG_REQUIRES_AUTH="This request requires authentication, but the "+"current session is not authenticated.\nThe user "+"session may have expired or the server may have been "+"restarted.\nYou must log back into Rhythmyx to continue.";
this.ERROR_MSG_NO_SERVER="Unable connect to the Rhythmyx server.\nThe server may be down."+"\nPlease contact your Rhythmyx administrator.";
};
dojo.provide("ps.aa.Menu");
ps.aa.Menu=new function(){
this.menubarIcon=null;
this.showTreeElem=null;
this.hideTreeElem=null;
this.showBordersElem=null;
this.hideBordersElem=null;
this.showPlaceholdersElem=null;
this.hidePlaceholdersElem=null;
this.addSnippetElem=null;
this.changeTemplateElem=null;
this.downElem=null;
this.upElem=null;
this.contentElem=null;
this.contentNewElem=null;
this.contentNewItemElem=null;
this.accountElem=null;
this.accountLogoutElem=null;
this.accountUserInfoElem=null;
this.editElem=null;
this.editAllElem=null;
this.editFieldElem=null;
this.removeElem=null;
this.viewElem=null;
this.toolElem=null;
this.workflow=null;
this.compare=null;
this.showRelationships=null;
this.translate=null;
this.createVersion=null;
this.showUrl=null;
this.pubNow=null;
this.separator=null;
this.viewContent=null;
this.viewProperties=null;
this.viewRevisions=null;
this.viewAuditTrail=null;
this.preview=null;
this.slotCtxMenu=null;
this.itemCtxMenu=null;
this.ctxChangeTemplate=null;
this.ctxUp=null;
this.ctxDown=null;
this.ctxRemove=null;
this.ctxNewFromSnippet=null;
this.ctxInsertFromSnippet=null;
this.ctxReplaceFromSnippet=null;
this.ctxOpenFromSnippet=null;
this.ctxItemSeparator1=null;
this.ctxItemSeparator2=null;
this.ctxEditAll=null;
this.ctxEditField=null;
this.ctxAddSnippet=null;
this.ctxRemoveSnippet=null;
this.ctxNewSnippet=null;
this.actionVisibilityChecker={getLabel:function(_db5){
if(this.m_labels==null){
var _db6=ps.io.Actions.getActionLabels(this.getNames());
if(!_db6.isSuccess()){
dojo.debug("Failed to retrieve labels for some menu actions. Reason: "+_db6.getValue());
return _db5;
}
this.m_labels={};
var resp=_db6.getValue()[0];
for(var name in resp){
this.m_labels[name.toLowerCase()]=resp[name];
}
}
var _db9=this.m_labels[_db5.toLowerCase()];
if(_db9==undefined||_db9==null){
return _db5;
}
return _db9;
},isVisible:function(_dba){
if(this.m_currentId==null||_dba==undefined||_dba==null){
return false;
}
var _dbb=this.m_cache[this.m_currentId.toString()];
if(_dbb==null){
var _dbc=ps.io.Actions.getActionVisibility(this.getNames(),this.m_currentId);
if(!_dbc.isSuccess()){
dojo.debug("Failed to retrieve visibility states for some menu actions. Reason: "+_dbc.getValue());
return false;
}
_dbb=_dbc.getValue();
this.m_cache[this.m_currentId.toString()]=_dbb;
}
return _dbb[_dba.toLowerCase()];
},flush:function(){
this.m_cache={};
},setCurrentId:function(_dbd){
this.m_currentId=_dbd;
},getNames:function(){
if(this.m_names==null){
this.m_names=["Edit_PromotableVersion","Publish_Now","Item_ViewDependents","View_Compare","Translate",];
}
return this.m_names;
},m_names:null,m_labels:null,m_currentId:null,m_cache:{},m_parent:this};
this.init=function(ids){
this._initMenuBar();
};
this.initAsynch=function(ids){
this._initContextMenu(ids);
var _dc0=dojo.widget.byId("ps.aa.Menubar");
this._addMenubarItems2(_dc0);
};
this.activate=function(_dc1,_dc2){
this._resetMenubar(_dc1,_dc2);
this._resetContextMenu(_dc1,_dc2);
};
this._resetMenubar=function(_dc3,_dc4){
this._resetMenubarParams={objId:_dc3,parentId:_dc4};
if(_dc3.isSlotNode()){
this._resetSlotBar(_dc3.isCheckoutByMe());
}else{
if(_dc3.isPageNode()||_dc3.isFieldNode()){
this._resetPageFieldBar(_dc3);
}else{
this._resetSnippetBar(_dc3,_dc4);
}
}
this._updateIconMenuItem(_dc3);
};
this._resetLastMenubar=function(){
var _dc5=this._resetMenubarParams;
if(_dc5){
this._resetMenubar(_dc5.objId,_dc5.parentId);
}
};
this._updateIconMenuItem=function(_dc6){
this.menubarIcon.setImage(_dc6.getImagePath(ps.aa.controller.IMAGE_ROOT_PATH));
var _dc7="";
if(_dc6.isFieldNode()){
_dc7=_dc6.getFieldName();
}else{
div=dojo.byId(_dc6.serialize());
_dc7=div.getAttribute("psAaLabel");
}
this.menubarIcon.setTitle(_dc7);
};
this._maybeShow=function(_dc8,_dc9){
var _dca=true;
if(_dc8){
if(_dc9){
if(_dc8.rx_actionName!==undefined){
this.actionVisibilityChecker.setCurrentId(_dc9);
_dca=this.actionVisibilityChecker.isVisible(_dc8.rx_actionName);
}
}
if(_dca){
dojo.html.show(_dc8.domNode);
}else{
dojo.html.hide(_dc8.domNode);
}
}
};
this._maybeHide=function(_dcb){
if(_dcb){
dojo.html.hide(_dcb.domNode);
}
};
this._resetSlotBar=function(_dcc){
this._maybeHide(this.changeTemplateElem);
this._maybeHide(this.upElem);
this._maybeHide(this.downElem);
dojo.html.hide(this.editElem.domNode);
this._maybeHide(this.editAllElem);
this._maybeHide(this.editFieldElem);
this._maybeHide(this.removeElem);
dojo.html.hide(this.toolElem.domNode);
this._maybeHide(this.workflow);
this._maybeShow(this.addSnippetElem);
if(this.addSnippetElem){
this.addSnippetElem.setDisabled(!_dcc);
}
},this._resetPageFieldBar=function(_dcd){
var _dce=ps.io.Actions.canManageNav(_dcd);
if(_dce.isSuccess()&&_dce.getValue()=="true"){
this._maybeShow(this.nav);
}else{
this._maybeHide(this.nav);
}
this._maybeHide(this.addSnippetElem);
this._maybeHide(this.changeTemplateElem);
this._maybeHide(this.upElem);
this._maybeHide(this.downElem);
this._maybeHide(this.removeElem);
dojo.html.show(this.editElem.domNode);
this._maybeShow(this.editAllElem);
dojo.html.show(this.toolElem.domNode);
this._maybeShow(this.viewContent);
this._maybeShow(this.viewProperties);
this._maybeShow(this.viewRevisions);
this._maybeShow(this.viewAuditTrail);
this._maybeShow(this.translate,_dcd);
this._maybeShow(this.createVersion,_dcd);
this._maybeShow(this.compare,_dcd);
this._maybeShow(this.showRelationships,_dcd);
this._maybeShow(this.workflow);
if(_dcd.isPageNode()){
this._maybeShow(this.pubNow,_dcd);
}else{
this._maybeHide(this.pubNow,_dcd);
}
if(_dcd.isFieldNode()){
this._maybeShow(this.editFieldElem);
}else{
this._maybeHide(this.editFieldElem);
}
var _dcf=!_dcd.isCheckoutByMe();
this.editElem.setDisabled(_dcf);
if(this.editAllElem){
this.editAllElem.setDisabled(_dcf);
}
if(_dcd.isFieldNode()&&this.editFieldElem){
this.editFieldElem.setDisabled(_dcf);
}
},this._resetSnippetBar=function(_dd0,_dd1){
this._maybeHide(this.addSnippetElem);
this._maybeHide(this.editFieldElem);
this._maybeHide(this.nav);
this._maybeHide(this.pubNow,_dd0);
this._maybeShow(this.changeTemplateElem);
this._maybeShow(this.upElem);
this._maybeShow(this.downElem);
dojo.html.show(this.editElem.domNode);
this._maybeShow(this.editAllElem);
this._maybeShow(this.removeElem);
dojo.html.show(this.toolElem.domNode);
this._maybeShow(this.workflow);
var _dd2=!_dd1.isCheckoutByMe();
if(this.changeTemplateElem){
this.changeTemplateElem.setDisabled(_dd2);
}
if(this.downElem){
this.downElem.setDisabled(_dd2);
}
if(this.upElem){
this.upElem.setDisabled(_dd2);
}
if(this.removeElem){
this.removeElem.setDisabled(_dd2);
}
if(_dd0.isCheckoutByMe()){
this.editElem.setDisabled(false);
if(this.editAllElem){
this.editAllElem.setDisabled(false);
}
}else{
if(_dd1.isCheckoutByMe()){
this.editElem.setDisabled(false);
if(this.editAllElem){
this.editAllElem.setDisabled(true);
}
}else{
this.editElem.setDisabled(true);
}
}
},this._initMenuBar=function(){
var _dd3=dojo.widget.byId("ps.aa.Menubar");
this.menubarIcon=dojo.widget.createWidget(ps.aa.Menu.MENUBARICON);
_dd3.addChild(this.menubarIcon);
this._addContentMenu(_dd3);
this._addEditMenu(_dd3);
this._addViewMenu(_dd3);
this.workflow=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM,{caption:"Workflow...",onClick:function(){
ps.aa.controller.workflowItem();
}});
_dd3.addChild(this.workflow);
this._addToolsMenu(_dd3);
this._addPreviewMenu(_dd3);
this._addAccountMenu(_dd3);
this._addNavigationMenu(_dd3);
this._addHelpMenu(_dd3);
this.toggleShowHidePlaceholders(true);
};
this._addMenubarItems2=function(_dd4){
dojo.lang.assert(_dd4,"Menu bar must be specified");
this.addSnippetElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM,{caption:"Insert Snippet...",onClick:function(){
ps.aa.controller.addSnippet(ps.aa.Menu.INSERT_FROM_SLOT);
}});
dojo.html.hide(this.addSnippetElem.domNode);
_dd4.addChild(this.addSnippetElem,null,"before",this.preview.domNode);
this.changeTemplateElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM,{caption:"Template...",onClick:function(){
ps.aa.controller.changeTemplate();
}});
dojo.html.hide(this.changeTemplateElem.domNode);
this.upElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM,{caption:"Up",onClick:function(){
ps.aa.controller.moveSnippetUp();
}});
dojo.html.hide(this.upElem.domNode);
this.downElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEM,{caption:"Down",onClick:function(){
ps.aa.controller.moveSnippetDown();
}});
dojo.html.hide(this.downElem.domNode);
_dd4.addChild(this.changeTemplateElem,null,"after",this.toolElem.domNode);
_dd4.addChild(this.upElem,null,"after",this.changeTemplateElem.domNode);
_dd4.addChild(this.downElem,null,"after",this.upElem.domNode);
};
this._addViewMenu=function(_dd5){
var _dd6=this;
this.showTreeElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Show Outline",onClick:function(){
ps.aa.controller.showTree();
}});
this.hideTreeElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Hide Outline",onClick:function(){
ps.aa.controller.hideTree();
}});
if(___sys_aamode==1){
this.showBordersElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Show Icons/Borders",onClick:function(){
ps.aa.controller.showBorders();
}});
}else{
this.hideBordersElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Hide Icons/Borders",onClick:function(){
ps.aa.controller.hideBorders();
}});
}
this.showPlaceholdersElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Show Place Holders",onClick:function(){
ps.aa.controller.showPlaceholders();
}});
this.hidePlaceholdersElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Hide Place Holders",onClick:function(){
ps.aa.controller.hidePlaceholders();
}});
function createSubmenu(){
var _dd7=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.ViewSubMenu"});
_dd7.addChild(_dd6.showTreeElem);
_dd7.addChild(_dd6.hideTreeElem);
if(___sys_aamode==1){
_dd7.addChild(_dd6.showBordersElem);
}else{
_dd7.addChild(_dd6.hideBordersElem);
}
_dd7.addChild(_dd6.showPlaceholdersElem);
_dd7.addChild(_dd6.hidePlaceholdersElem);
_dd6._resetLastMenubar();
}
this.viewElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"View",submenuId:"ps.aa.ViewSubMenu"});
this.viewElem.createSubmenu=createSubmenu;
_dd5.addChild(this.viewElem);
};
this._addToolsMenu=function(_dd8){
var _dd9=this;
function createSubmenu(){
var _dda=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.ToolSubMenu"});
var _ddb="View_Compare";
_dd9.compare=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:_dd9.actionVisibilityChecker.getLabel(_ddb),onClick:function(){
ps.aa.controller.compareItemRevisions();
}});
_dd9.compare.rx_actionName=_ddb;
_dda.addChild(_dd9.compare);
_ddb="Item_ViewDependents";
_dd9.showRelationships=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:_dd9.actionVisibilityChecker.getLabel(_ddb),onClick:function(){
ps.aa.controller.showItemRelationships();
}});
_dd9.showRelationships.rx_actionName=_ddb;
_dda.addChild(_dd9.showRelationships);
var _ddc=dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR);
_dda.addChild(_ddc);
_dd9.viewContent=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"View Content Item",onClick:function(){
ps.aa.controller.viewContent();
}});
_dda.addChild(_dd9.viewContent);
_dd9.viewProperties=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Properties",onClick:function(){
ps.aa.controller.viewProperties();
}});
_dda.addChild(_dd9.viewProperties);
_dd9.viewRevisions=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Revisions",onClick:function(){
ps.aa.controller.viewRevisions();
}});
_dda.addChild(_dd9.viewRevisions);
_dd9.viewAuditTrail=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Audit Trail",onClick:function(){
ps.aa.controller.viewAuditTrail();
}});
_dda.addChild(_dd9.viewAuditTrail);
_dd9.separator=dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR);
_dda.addChild(_dd9.separator);
if(ps.io.Actions.getLocaleCount()>1){
_ddb="Translate";
_dd9.translate=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:_dd9.actionVisibilityChecker.getLabel(_ddb),onClick:function(){
ps.aa.controller.createTranslation();
}});
_dd9.translate.rx_actionName=_ddb;
_dda.addChild(_dd9.translate);
}
_ddb="Edit_PromotableVersion";
_dd9.createVersion=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:_dd9.actionVisibilityChecker.getLabel(_ddb),onClick:function(){
ps.aa.controller.createVersion();
}});
_dd9.createVersion.rx_actionName=_ddb;
_dda.addChild(_dd9.createVersion);
_dd9.showUrl=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Link to Page",onClick:function(){
ps.aa.controller.showPageUrl();
}});
_dda.addChild(_dd9.showUrl);
_ddb="Publish_Now";
_dd9.pubNow=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:_dd9.actionVisibilityChecker.getLabel(_ddb),onClick:function(){
ps.aa.controller.publishPage();
}});
_dd9.pubNow.rx_actionName=_ddb;
_dda.addChild(_dd9.pubNow);
_dd9._maybeHide(_dd9.translate);
_dd9._maybeHide(_dd9.pubNow);
_dd9._maybeHide(_dd9.createVersion);
_dd9._maybeHide(_dd9.compare);
_dd9._maybeHide(_dd9.showRelationships);
_dd9._resetLastMenubar();
}
this.toolElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Tools",submenuId:"ps.aa.ToolSubMenu"});
this.toolElem.createSubmenu=createSubmenu;
_dd8.addChild(this.toolElem);
};
this._addContentMenu=function(_ddd){
var _dde=this;
function createSubmenu(){
var _ddf=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.ContentSubMenu"});
_dde.contentNewElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Copy",onClick:function(){
ps.aa.controller.createItem(ps.aa.Menu.COPY_FROM_CONTENT);
}});
_ddf.addChild(_dde.contentNewElem);
_dde.contentNewItemElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"New",onClick:function(){
ps.aa.controller.createItem(ps.aa.Menu.NEW_FROM_CONTENT);
}});
_ddf.addChild(_dde.contentNewItemElem);
_dde._resetLastMenubar();
}
this.contentElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Content",submenuId:"ps.aa.ContentSubMenu"});
this.contentElem.createSubmenu=createSubmenu;
_ddd.addChild(this.contentElem);
};
this._addAccountMenu=function(_de0){
var _de1=this;
function createSubmenu(){
var _de2=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.AccountSubMenu"});
_de1.accountUserInfoElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"User Info",onClick:function(){
ps.UserInfo.showInfo();
}});
_de2.addChild(_de1.accountUserInfoElem);
_de1.accountLogoutElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Logout",onClick:function(){
ps.aa.controller.logout();
}});
_de2.addChild(_de1.accountLogoutElem);
}
this.accountElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Account",submenuId:"ps.aa.AccountSubMenu"});
this.accountElem.createSubmenu=createSubmenu;
_de0.addChild(this.accountElem);
};
this._addEditMenu=function(_de3){
var _de4=this;
function createSubmenu(){
var _de5=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.EditSubMenu"});
_de4.editAllElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Content Item",onClick:function(){
ps.aa.controller.editAll();
}});
_de5.addChild(_de4.editAllElem);
_de4.editFieldElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Field",onClick:function(){
ps.aa.controller.editField();
}});
dojo.html.hide(_de4.editFieldElem.domNode);
_de5.addChild(_de4.editFieldElem);
_de4.removeElem=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Remove",onClick:function(){
ps.aa.controller.removeSnippet();
}});
dojo.html.hide(_de4.removeElem.domNode);
_de5.addChild(_de4.removeElem);
_de4._resetLastMenubar();
}
this.editElem=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Edit",submenuId:"ps.aa.EditSubMenu"});
this.editElem.createSubmenu=createSubmenu;
_de3.addChild(this.editElem);
};
this._addHelpMenu=function(_de6){
var _de7=this;
function createSubmenu(){
var _de8=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.HelpSubMenu"});
_de8.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Active Assembly Help",onClick:function(){
ps.aa.controller.openHelpWindow(ps.aa.Menu.AAHELP);
}}));
_de8.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Active Assembly Tutorial",onClick:function(){
ps.aa.controller.openHelpWindow(ps.aa.Menu.AATUTORIAL);
}}));
_de8.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"About Active Assembly",onClick:function(){
ps.aa.controller.openHelpWindow(ps.aa.Menu.AAABOUT);
}}));
_de7._resetLastMenubar();
}
var _de9=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Help",submenuId:"ps.aa.HelpSubMenu"});
_de9.createSubmenu=createSubmenu;
_de6.addChild(_de9);
};
this._addPreviewMenu=function(_dea){
var _deb=this;
function createSubmenu(){
var _dec=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.PreviewSubMenu"});
_dec.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Current Revisions",onClick:function(){
ps.aa.controller.previewWithCurrentRevisions();
}}));
_dec.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"With Edits",onClick:function(){
ps.aa.controller.previewWithEditRevisions();
}}));
_deb._resetLastMenubar();
}
this.preview=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Preview",submenuId:"ps.aa.PreviewSubMenu"});
this.preview.createSubmenu=createSubmenu;
_dea.addChild(this.preview);
};
this._addNavigationMenu=function(_ded){
var _dee=this;
function createSubmenu(){
var _def=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.NavigationSubMenu"});
_def.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Manage Navigation",onClick:function(){
ps.aa.controller.openManageNavigationDlg();
}}));
_dee._resetLastMenubar();
}
this.nav=dojo.widget.createWidget(ps.aa.Menu.MENUBARITEMDROPDOWN,{caption:"Navigation",submenuId:"ps.aa.NavigationSubMenu"});
this.nav.createSubmenu=createSubmenu;
_ded.addChild(this.nav);
};
this._initContextMenu=function(ids){
var _df1=this;
this.slotCtxMenu=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.SlotCtxMenu"});
var _df2=this.slotCtxMenu;
document.body.appendChild(_df2.domNode);
_df2.createMenuItems=function(){
_df1.ctxNewSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"New Snippet...",onClick:function(){
ps.aa.controller.createItem(ps.aa.Menu.NEW_FROM_SLOT);
}});
_df2.addChild(_df1.ctxNewSnippet);
_df1.ctxAddSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Insert Snippet...",onClick:function(){
ps.aa.controller.addSnippet(ps.aa.Menu.INSERT_FROM_SLOT);
}});
_df2.addChild(_df1.ctxAddSnippet);
_df1.ctxRemoveSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Remove Snippets...",onClick:function(){
ps.aa.controller.openRemoveSnippetsDlg();
}});
_df2.addChild(_df1.ctxRemoveSnippet);
};
this.itemCtxMenu=dojo.widget.createWidget(ps.aa.Menu.POPUPMENU,{id:"ps.aa.ItemCtxMenu"});
var _df3=this.itemCtxMenu;
document.body.appendChild(_df3.domNode);
_df3.createMenuItems=function(){
_df1.ctxChangeTemplate=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Template...",onClick:function(){
ps.aa.controller.changeTemplate();
}});
_df3.addChild(_df1.ctxChangeTemplate);
_df1.ctxUp=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Up",onClick:function(){
ps.aa.controller.moveSnippetUp();
}});
_df3.addChild(_df1.ctxUp);
_df1.ctxDown=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Down",onClick:function(){
ps.aa.controller.moveSnippetDown();
}});
_df3.addChild(_df1.ctxDown);
_df1.ctxRemove=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Remove",onClick:function(){
ps.aa.controller.removeSnippet();
}});
_df3.addChild(_df1.ctxRemove);
_df1.ctxItemSeparator1=dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR);
_df3.addChild(_df1.ctxItemSeparator1);
_df1.ctxNewFromSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"New...",onClick:function(){
ps.aa.controller.createItem(ps.aa.Menu.NEW_FROM_SNIPPET);
}});
_df3.addChild(_df1.ctxNewFromSnippet);
_df1.ctxInsertFromSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Insert...",onClick:function(){
ps.aa.controller.addSnippet(ps.aa.Menu.INSERT_FROM_SNIPPET);
}});
_df3.addChild(_df1.ctxInsertFromSnippet);
_df1.ctxReplaceFromSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Replace...",onClick:function(){
ps.aa.controller.createItem(ps.aa.Menu.REPLACE_FROM_SNIPPET);
}});
_df3.addChild(_df1.ctxReplaceFromSnippet);
_df1.ctxItemSeparator2=dojo.widget.createWidget(ps.aa.Menu.MENUSEPARATOR);
_df3.addChild(_df1.ctxItemSeparator2);
_df1.ctxOpenFromSnippet=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Open",onClick:function(){
ps.aa.controller.openSnippet();
}});
_df3.addChild(_df1.ctxOpenFromSnippet);
_df1.ctxWorkflow=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Workflow...",onClick:function(){
ps.aa.controller.workflowItem();
}});
_df3.addChild(_df1.ctxWorkflow);
_df1.ctxEditField=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Edit Field",onClick:function(){
ps.aa.controller.editField();
}});
_df3.addChild(_df1.ctxEditField);
_df1.ctxEditAll=dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Edit Content Item",onClick:function(){
ps.aa.controller.editAll();
}});
_df3.addChild(_df1.ctxEditAll);
_df3.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"View Content Item",onClick:function(){
ps.aa.controller.viewContent();
}}));
_df3.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Properties",onClick:function(){
ps.aa.controller.viewProperties();
}}));
_df3.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Revisions",onClick:function(){
ps.aa.controller.viewRevisions();
}}));
_df3.addChild(dojo.widget.createWidget(ps.aa.Menu.MENUITEM,{caption:"Audit Trail",onClick:function(){
ps.aa.controller.viewAuditTrail();
}}));
_df1._resetLastContextMenu();
};
if(___sys_aamode!=1){
this.bindContextMenu(ids);
}
},this.bindContextMenu=function(ids,_df5){
var _df6=new Array();
var _df7=new Array();
var _df8=0;
var _df9=0;
var _dfa=null;
var _dfb=___sys_aamode==1&&!_df5?"":"img.";
for(var i=0;i<ids.count;i++){
var _dfa=ids.item(i);
if(_dfa.isSlotNode()){
_df6[_df8++]=_dfb+_dfa.serialize();
}else{
_df7[_df9++]=_dfb+_dfa.serialize();
}
}
this.slotCtxMenu.bindTargetNodes(_df6);
this.itemCtxMenu.bindTargetNodes(_df7);
};
this.unBindContextMenu=function(ids,_dfe){
var _dff=new Array();
var _e00=new Array();
var _e01=0;
var _e02=0;
var _e03=null;
var _e04=___sys_aamode==1&&!_dfe?"":"img.";
for(var i=0;i<ids.count;i++){
var _e03=ids.item(i);
if(_e03.isSlotNode()){
_dff[_e01++]=_e04+_e03.serialize();
}else{
_e00[_e02++]=_e04+_e03.serialize();
}
}
this.slotCtxMenu.unBindTargetNodes(_dff);
this.itemCtxMenu.unBindTargetNodes(_e00);
};
this._resetContextMenu=function(_e06,_e07){
this._resetContextMenuParams={objId:_e06,parentId:_e07};
if(_e06.isSlotNode()){
this._resetCtxSlotMenu(_e06);
}else{
if(_e06.isSnippetNode()){
this._resetCtxSnippetMenu(_e06,_e07);
}else{
this._resetCtxPageFieldMenu(_e06);
}
}
};
this._resetLastContextMenu=function(){
var _e08=this._resetContextMenuParams;
if(_e08){
this._resetContextMenu(_e08.objId,_e08.parentId);
}
};
this._resetCtxSlotMenu=function(_e09){
if(this.ctxAddSnippet){
this.ctxAddSnippet.setDisabled(!_e09.isCheckoutByMe());
}
if(this.ctxNewSnippet){
this.ctxNewSnippet.setDisabled(!_e09.isCheckoutByMe());
}
if(this.ctxRemoveSnippet){
this.ctxRemoveSnippet.setDisabled(!_e09.isCheckoutByMe());
}
};
this._resetCtxSnippetMenu=function(_e0a,_e0b){
if(!this.ctxChangeTemplate){
return;
}
dojo.html.hide(this.ctxEditField.domNode);
dojo.html.show(this.ctxChangeTemplate.domNode);
dojo.html.show(this.ctxUp.domNode);
dojo.html.show(this.ctxDown.domNode);
dojo.html.show(this.ctxRemove.domNode);
dojo.html.show(this.ctxItemSeparator1.domNode);
dojo.html.show(this.ctxNewFromSnippet.domNode);
dojo.html.show(this.ctxInsertFromSnippet.domNode);
dojo.html.show(this.ctxReplaceFromSnippet.domNode);
dojo.html.show(this.ctxOpenFromSnippet.domNode);
dojo.html.show(this.ctxItemSeparator2.domNode);
dojo.html.show(this.ctxWorkflow.domNode);
dojo.html.show(this.ctxEditAll.domNode);
var _e0c=!_e0b.isCheckoutByMe();
this.ctxChangeTemplate.setDisabled(_e0c);
this.ctxUp.setDisabled(_e0c);
this.ctxDown.setDisabled(_e0c);
this.ctxRemove.setDisabled(_e0c);
this.ctxNewFromSnippet.setDisabled(_e0c);
this.ctxInsertFromSnippet.setDisabled(_e0c);
this.ctxReplaceFromSnippet.setDisabled(_e0c);
this.ctxOpenFromSnippet.setDisabled(_e0c);
this.ctxEditAll.setDisabled(!_e0a.isCheckoutByMe());
};
this._resetCtxPageFieldMenu=function(_e0d){
if(!this.ctxChangeTemplate){
return;
}
dojo.html.hide(this.ctxChangeTemplate.domNode);
dojo.html.hide(this.ctxUp.domNode);
dojo.html.hide(this.ctxDown.domNode);
dojo.html.hide(this.ctxRemove.domNode);
dojo.html.hide(this.ctxItemSeparator1.domNode);
dojo.html.hide(this.ctxNewFromSnippet.domNode);
dojo.html.hide(this.ctxInsertFromSnippet.domNode);
dojo.html.hide(this.ctxReplaceFromSnippet.domNode);
dojo.html.hide(this.ctxOpenFromSnippet.domNode);
dojo.html.hide(this.ctxItemSeparator2.domNode);
dojo.html.show(this.ctxEditAll.domNode);
if(_e0d.isPageNode()){
dojo.html.hide(this.ctxEditField.domNode);
}else{
dojo.html.show(this.ctxEditField.domNode);
}
dojo.html.show(this.ctxWorkflow.domNode);
var _e0e=!_e0d.isCheckoutByMe();
this.ctxEditAll.setDisabled(_e0e);
this.ctxEditField.setDisabled(_e0e);
};
this.toggleShowHideTree=function(_e0f){
if(_e0f){
dojo.html.show(this.hideTreeElem.domNode);
dojo.html.hide(this.showTreeElem.domNode);
}else{
dojo.html.hide(this.hideTreeElem.domNode);
dojo.html.show(this.showTreeElem.domNode);
}
};
this.toggleShowHidePlaceholders=function(_e10){
if(_e10){
dojo.html.show(this.hidePlaceholdersElem.domNode);
dojo.html.hide(this.showPlaceholdersElem.domNode);
}else{
dojo.html.hide(this.hidePlaceholdersElem.domNode);
dojo.html.show(this.showPlaceholdersElem.domNode);
}
};
this.getMenuItems=function(){
var _e11=[];
for(name in this){
var item=this[name];
if(item instanceof dojo.widget.MenuItem2){
_e11.push(item);
}
}
dojo.lang.assert(_e11.length>0);
return _e11;
};
};
ps.aa.Menu.PAGE_IMG_PATH="../sys_resources/images/page.gif";
ps.aa.Menu.SNIPPET_IMG_PATH="../sys_resources/images/item.gif";
ps.aa.Menu.SLOT_IMG_PATH="../sys_resources/images/relatedcontent/slot.gif";
ps.aa.Menu.FIELD_IMG_PATH="../sys_resources/images/pen.gif";
ps.aa.Menu.MENUBAR="ps:MenuBar2";
ps.aa.Menu.MENUBARICON="ps:MenuBarIcon";
ps.aa.Menu.MENUBARITEM="MenuBarItem2";
ps.aa.Menu.MENUBARITEMDROPDOWN="ps:MenuBarItemDropDown";
ps.aa.Menu.MENUITEM="MenuItem2";
ps.aa.Menu.MENUSEPARATOR="MenuSeparator2";
ps.aa.Menu.POPUPMENU="ps:PopupMenu";
ps.aa.Menu.AAHELP="AAHelp";
ps.aa.Menu.AATUTORIAL="AATutorial";
ps.aa.Menu.AAABOUT="AAAbout";
ps.aa.Menu.NEW_FROM_SLOT=0;
ps.aa.Menu.NEW_FROM_SNIPPET=1;
ps.aa.Menu.REPLACE_FROM_SNIPPET=2;
ps.aa.Menu.COPY_FROM_CONTENT=3;
ps.aa.Menu.NEW_FROM_CONTENT=4;
ps.aa.Menu.INSERT_FROM_SLOT=0;
ps.aa.Menu.INSERT_FROM_SNIPPET=1;
dojo.provide("ps.UserInfo");
ps.UserInfo=new function(){
this._userInfoDlg=null;
this.USER_INFO_PAGE_URL="/Rhythmyx/ui/activeassembly/UserStatus.jsp";
this.showInfo=function(){
var dlg=this._getUserInfoDialog();
dlg.show();
};
this._getUserInfoDialog=function(){
this._userInfoDlg=ps.createDialog({id:"ps.UserInfoDlg",title:"User Info"},"420px","150px");
var _e14=ps.aa.controller.getLinkToCurrentPage();
_e14=_e14.replace("/Rhythmyx","..");
var url=ps.util.addParamToUrl(this.USER_INFO_PAGE_URL,"sys_redirecturl",escape(_e14));
this._userInfoDlg.setUrl(url);
return this._userInfoDlg;
};
};
dojo.provide("ps.aa.Page");
ps.aa.Page=new function(){
this.activeDiv=null;
this.init=function(){
},this.activate=function(div){
dojo.lang.assert(div);
if(div===this.activeDiv){
return false;
}
if(this.activeDiv!=null){
this.activeDiv.style.border="1px dotted";
if(___sys_aamode==1){
this.activeDiv.style.borderColor="transparent";
}else{
this.activeDiv.style.borderColor="gray";
}
}
var _e17=dojo.render.html.ie?"3":"2";
div.style.border=_e17+"px dotted";
div.style.borderColor="gray";
this.activeDiv=div;
return true;
},this.getParentId=function(_e18,_e19){
var _e1a=null;
var _e1b=_e18;
while(_e1a==null){
var node=_e1b.parentNode;
if(node==null||dojo.lang.isUndefined(node)){
break;
}
if(dojo.html.getClass(node)===ps.aa.SLOT_CLASS&&dojo.html.isTag(node,"div")){
_e1a=node;
}
_e1b=node;
}
var _e1d=null;
if(_e1a!=null){
_e1d=this.getObjectId(_e1a);
}
if(_e1d==null){
alert("Cannot find parent node for snippet: "+_e19.serialize());
return null;
}else{
return _e1d;
}
},this.getObjectId=function(_e1e){
if(dojo.lang.isUndefined(_e1e)||_e1e==null){
return null;
}
var _e1f=null;
_e1f=dojo.html.getAttribute(_e1e,"id");
if(_e1f!=null&&(dojo.lang.isString(_e1f))){
var _e20=new ps.aa.ObjectId(_e1f);
if(_e20!=null&&(!dojo.lang.isUndefined(_e20))){
return _e20;
}
}
return null;
},this.getElement=function(_e21){
dojo.lang.assertType(_e21,ps.aa.ObjectId);
var _e22=document.getElementById(_e21.toString());
dojo.lang.assert(_e22,"No element found for id "+_e21.toString());
return _e22;
};
};
dojo.provide("ps.aa.Tree");
ps.aa.Tree=function(){
this.root=null;
this.init=function(){
var _e23=dojo.byId("ps.aa.ContentPane");
dojo.lang.assert(_e23!=null,"Cannot find DOM element id='ps.aa.ContentPane'");
this._createNodes(_e23,null);
this._sort(this.root);
};
this._sort=function(_e24){
if(_e24!=null&&!_e24.isLeafNode()){
_e24.childNodes.sort(function(o1,o2){
if((o1.objId.isFieldNode()&&o2.objId.isSlotNode())){
return -1;
}
if((o1.objId.isSlotNode()&&o2.objId.isFieldNode())){
return 1;
}
if((o1.objId.isFieldNode()&&o2.objId.isFieldNode())||(o1.objId.isSlotNode()&&o2.objId.isSlotNode())){
var s1=o1.getLabel().toLowerCase();
var s2=o2.getLabel().toLowerCase();
if(s1>s2){
return 1;
}else{
if(s1<s2){
return -1;
}
}
}
if(o1.objId.isSnippetNode()&&o2.objId.isSnippetNode()){
var s1=parseInt(o1.objId.getSortRank());
var s2=parseInt(o2.objId.getSortRank());
if(s1>s2){
return 1;
}else{
if(s1<s2){
return -1;
}
}
}
return 0;
});
dojo.lang.forEach(_e24.childNodes.toArray(),this._sort,this);
}
};
this._createNodes=function(_e29,_e2a){
var divs=_e29.getElementsByTagName("div");
var div=null;
var _e2d=_e2a;
for(var i=0;i<divs.length;i++){
div=divs[i];
if(div.className==ps.aa.PAGE_CLASS){
_e2d=this._addPageNode(div);
}else{
if(div.className==ps.aa.FIELD_CLASS){
_e2d=this._addFieldNode(div,_e2d);
}else{
if(div.className==ps.aa.SNIPPET_CLASS){
_e2d=this._addSnippetNode(div,_e2d);
}else{
if(div.className==ps.aa.SLOT_CLASS){
_e2d=this._addSlotNode(div,_e2d,null);
}
}
}
}
}
};
this._resetChildNodes=function(_e2f){
dojo.lang.assert(_e2f);
var id=ps.aa.Page.getObjectId(_e2f);
dojo.lang.assert(id,"Cannot find object id");
var node=this.getNodeById(id);
dojo.lang.assert(node,"Cannot find node");
node.removeChildNodes();
this._createNodes(_e2f,node);
this._sort(node);
};
this.fireBeforeDomChange=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
this.onBeforeDomChange(id);
};
this.onBeforeDomChange=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
};
this.fireDomChanged=function(_e34,id){
id&&dojo.lang.assertType(id,ps.aa.ObjectId);
_e34&&dojo.lang.assert(_e34,ps.aa.ObjectId);
if(id&&id.equals(this.root.objId)){
id=null;
}
if(_e34){
var _e36=ps.aa.Page.getElement(_e34);
this._resetChildNodes(_e36);
}else{
this.root.clear();
this.root=null;
this.init();
}
if(!id){
id=this.root.objId;
}
this.onDomChanged(id);
};
this.onDomChanged=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
};
this._addPageNode=function(div){
if(this.root!=null){
ps.util.error("Unknown page node, already got a root: "+this.root.objId.toString());
return null;
}
return this.root=this._getNodeRqd(div);
};
this._addSlotNode=function(div,_e3a,_e3b){
var node=_e3b;
if(node==null){
node=this._getNodeRqd(div);
}
var _e3d=this._getParentPageSnippet(node,_e3a);
dojo.lang.assert(_e3d!=null,"Cannot find parent node for slot node: "+node.toString());
_e3d.addChildNode(node);
return node;
};
this._getParentPageSnippet=function(_e3e,_e3f){
var _e40=_e3f;
while(_e40){
if((_e40.isPageNode()||_e40.isSnippetNode())&&_e40.objId.getContentId()==_e3e.objId.getContentId()){
return _e40;
}else{
_e40=_e40.parentNode;
}
}
return null;
};
this._addSnippetNode=function(div,_e42){
var node=this._getNodeRqd(div);
if(this.root==null){
this.root=node;
return this.root;
}else{
var _e44=this._getParentSlot(node,_e42);
if(_e44!=null){
_e44.addChildNode(node);
return node;
}
}
ps.util.error("Cannot find parent node for snippet node: "+node.toString());
return null;
};
this._getParentSlot=function(_e45,_e46){
var _e47=_e46;
while(_e47){
if(_e47.isSlotNode()&&_e47.objId.getSlotId()==_e45.objId.getSlotId()&&_e47.objId.getContentId()==_e45.objId.getParentId()){
return _e47;
}else{
_e47=_e47.parentNode;
}
}
return null;
};
this._addFieldNode=function(div,_e49){
var node=this._getNodeRqd(div);
var _e4b=this._getParentPageSnippet(node,_e49);
dojo.lang.assert(_e4b!=null,"Cannot find parent node for field node: "+node.toString());
_e4b.addChildNode(node);
return _e4b;
};
this._getNodeRqd=function(div){
var _e4d=ps.aa.Page.getObjectId(div);
dojo.lang.assert(_e4d!=null,"Malformed objectId for a node of class="+div.className);
return new ps.aa.TreeNode(_e4d);
};
this.getRootNode=function(){
return this.root;
};
this.getNodeById=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
return this._getNodeById(id,this.root);
};
this._getNodeById=function(id,_e50){
dojo.lang.assertType(id,ps.aa.ObjectId);
_e50&&dojo.lang.assertType(_e50,ps.aa.TreeNode);
if(_e50.equals(id)){
return _e50;
}
if(!_e50.isLeafNode()){
var node=null;
for(var i=0;i<_e50.childNodes.count;i++){
node=this._getNodeById(id,_e50.childNodes.item(i));
if(node!=null){
return node;
}
}
}
return null;
};
this.getIdsFromContentId=function(_e53,_e54){
if(this.root.isLeafNode()){
return _e55;
}
var _e55=null;
if(dojo.lang.isString(_e54)){
_e55=new dojo.collections.ArrayList();
this._getFieldIdsByContentIdName(_e53,_e54,this.root,_e55);
}else{
_e55=this._getIdsOfTopLevelFieldNode(_e53);
this._getSnippetIdsFromContentId(_e53,this.root,_e55);
}
return _e55;
};
this.getAllIdsByContentId=function(_e56){
var _e57=new dojo.collections.ArrayList();
if(this.root==null){
return _e57;
}
this._getAllIdsByContentId(_e56,this.root,_e57);
return _e57;
};
this.getIdsFromNodeId=function(_e58){
dojo.lang.assertType(_e58,ps.aa.ObjectId);
var ids=new dojo.collections.ArrayList();
var node=this.getNodeById(_e58);
if(node!=null){
this._getIdsFromNode(node,ids);
}
return ids;
};
this._getIdsFromNode=function(node,ids){
if(node==null){
return;
}
ids.add(node.objId);
if(!node.isLeafNode()){
for(var i=0;i<node.childNodes.count;i++){
this._getIdsFromNode(node.childNodes.item(i),ids);
}
}
};
this._getAllIdsByContentId=function(_e5e,node,_e60){
if(_e5e==null){
_e60.add(node.objId);
}
if(node.objId.getContentId()==_e5e){
_e60.add(node.objId);
}
if(node.isLeafNode()){
return;
}
for(var i=0;i<node.childNodes.count;i++){
this._getAllIdsByContentId(_e5e,node.childNodes.item(i),_e60);
}
};
this._getFieldIdsByContentIdName=function(_e62,_e63,_e64,_e65){
if(_e64.isLeafNode()){
return;
}
for(var i=0;i<_e64.childNodes.count;i++){
var node=_e64.childNodes.item(i);
if(node.isFieldNode()&&node.objId.getContentId()==_e62&&node.objId.getFieldName()==_e63){
_e65.add(node.objId);
}else{
if(!node.isLeafNode()){
this._getFieldIdsByContentIdName(_e62,_e63,node,_e65);
}
}
}
};
this._getIdsOfTopLevelFieldNode=function(_e68){
var _e69=new dojo.collections.ArrayList();
for(var i=0;i<this.root.childNodes.count;i++){
var node=this.root.childNodes.item(i);
if(node.isFieldNode()&&node.objId.getContentId()==_e68){
_e69.add(node.objId);
}
}
return _e69;
};
this._getSnippetIdsFromContentId=function(_e6c,_e6d,_e6e){
for(var i=0;i<_e6d.childNodes.count;i++){
var node=_e6d.childNodes.item(i);
if(node.isSnippetNode()&&node.objId.getContentId()==_e6c){
_e6e.add(node.objId);
}else{
if(!node.isLeafNode()){
this._getSnippetIdsFromContentId(_e6c,node,_e6e);
}
}
}
};
this.removeNode=function(id){
var node=this.getNodeById(id);
if(node==null){
return false;
}
var _e73=node.parentNode;
if(_e73!=null){
_e73.childNodes.remove(node);
}
node.clear();
return true;
};
this.getNextSiblingId=function(id){
var node=this.getNodeById(id);
dojo.lang.assert(node,"Cannot find node id="+id.serialize());
var _e76=node.parentNode;
if(_e76==null){
return null;
}
var len=_e76.childNodes.count;
var _e78=_e76.childNodes.indexOf(node);
var node=null;
if(_e78<(len-1)){
node=_e76.childNodes.item(_e78+1);
}else{
if(_e78>0){
node=_e76.childNodes.item(_e78-1);
}else{
node=_e76;
}
}
return node.objId;
};
this.toString=function(node){
if(dojo.lang.isUndefined(node)){
return this.toString(this.root);
}else{
var text=node.toString()+"\n";
if(node.isLeafNode()){
return text;
}
for(var i=0;i<node.childNodes.count;i++){
var _e7c=node.childNodes.item(i);
text+="   child["+i+"]: "+this.toString(_e7c)+"\n";
}
return text;
}
};
};
ps.aa.TreeNode=function(_e7d,_e7e,_e7f){
dojo.lang.assertType(_e7d,ps.aa.ObjectId);
_e7e&&dojo.lang.assertType(_e7e,ps.aa.TreeNode);
this.objId=_e7d;
if(dojo.lang.isUndefined(_e7e)){
this.parentNode=null;
}else{
this.parentNode=_e7e;
}
if(dojo.lang.isUndefined(_e7f)){
this.childNodes=null;
}else{
this.childNodes=_e7f;
}
this.nodeLabel=null;
this.isLeafNode=function(){
return this.childNodes==null||this.childNodes.count==0;
};
this.getLabel=function(){
if(this.nodeLabel==null){
if(this.isFieldNode()){
this.nodeLabel=this.objId.getFieldLabel().replace(/\:$/g,"");
}else{
var id=this.objId.serialize();
var _e81=dojo.byId(id);
dojo.lang.assert(_e81,"Cannot find DIV element with id="+id);
this.nodeLabel=dojo.html.getAttribute(_e81,"psAaLabel");
}
}
return this.nodeLabel;
};
this.equals=function(id){
dojo.lang.assertType(id,ps.aa.ObjectId);
return this.objId.serialize()==id.serialize();
};
this.clear=function(){
this.removeChildNodes();
this.objId=null;
this.parentNode=null;
this.nodeLabel=null;
};
this.clearLabel=function(){
this.nodeLabel=null;
};
this.removeChildNodes=function(){
if(this.childNodes==null){
return;
}
for(var i=0;i<this.childNodes.count;i++){
var node=this.childNodes.item(i);
node.clear();
}
this.childNodes.clear();
};
this.isSnippetNode=function(){
return this.objId.isSnippetNode();
};
this.getObjectId=function(){
return this.objId;
};
this.isPageNode=function(){
return this.objId.isPageNode();
};
this.isFieldNode=function(){
return this.objId.isFieldNode();
};
this.isSlotNode=function(){
return this.objId.isSlotNode();
};
this.addChildNode=function(node){
if(this.childNodes==null){
this.childNodes=new dojo.collections.ArrayList();
}
node.parentNode=this;
this.childNodes.add(node);
};
this.getIndex=function(){
dojo.lang.assert(this.parentNode);
var _e86=this.parentNode.childNodes.toArray();
for(var i in _e86){
var node=_e86[i];
if(node===this){
return parseInt(i);
}
}
dojo.lang.assert(false,"Inconsistent tree structure, could not find this node in the parent");
};
this.toString=function(){
if(this.parentNode==null&&this.childNodes==null){
return "id = "+this.objId.serialize();
}else{
if(this.parentNode==null){
return "id = "+this.objId.serialize()+"\n"+"childNodes.count = "+this.childNodes.count;
}else{
if(this.childNodes==null){
return "id = "+this.objId.serialize()+"\n"+"parent = "+this.parentNode.objId.serialize();
}else{
return "id = "+this.objId.serialize()+"\n"+"parent = "+this.parentNode.objId.serialize()+"\n"+"childNodes.count = "+this.childNodes.count;
}
}
}
};
};
dojo.provide("ps.aa.SnippetMove");
ps.aa.SnippetMove=function(_e89,_e8a,_e8b,_e8c,_e8d){
dojo.lang.assertType(_e89,ps.aa.ObjectId);
dojo.lang.assertType(_e8b,ps.aa.ObjectId);
dojo.lang.assert(dojo.lang.isNumeric(_e8c),"Should be interpreted as number");
_e8d&&dojo.lang.isBoolean(_e8d);
this._m_snippetId=_e89;
this._m_slotId=_e8a;
this._m_targetSlotId=_e8b;
this._m_targetIndex=_e8c;
this._m_dontUpdatePage=_e8d||false;
this._m_targetSnippetId=_e89.clone();
this._m_targetSnippetId.setSlotId(_e8b.getSlotId());
this._m_targetSnippetId.setSortRank(_e8c);
this._m_uiUpdateNeeded=false;
this._m_success=undefined;
this.getSnippetId=function(){
return this._m_snippetId;
};
this.getTargetSnippetId=function(){
return this._m_targetSnippetId;
};
this.setTargetSnippetId=function(_e8e){
dojo.lang.assertType(_e8e,ps.aa.ObjectId);
this._m_targetSnippetId=_e8e;
};
this.getSlotId=function(){
return this._m_slotId;
};
this.getTargetSlotId=function(){
return this._m_targetSlotId;
};
this.getTargetIndex=function(){
return this._m_targetIndex;
};
this.getDontUpdatePage=function(){
return this._m_dontUpdatePage;
};
this.setDontUpdatePage=function(_e8f){
this._m_dontUpdatePage=_e8f;
};
this.isUiUpdateNeeded=function(){
return this._m_uiUpdateNeeded;
};
this.setUiUpdateNeeded=function(_e90){
dojo.lang.assert(dojo.lang.isBoolean(_e90));
if(_e90){
dojo.lang.assert(this.getDontUpdatePage(),"uiUpdateNeedeed can be true only if getDontUpdatePage is true");
}
this._m_uiUpdateNeeded=_e90;
};
this.isSuccess=function(){
return this._m_success;
};
this.setSuccess=function(_e91){
dojo.lang.assertType(_e91,Boolean);
this._m_success=_e91;
};
};
dojo.provide("ps.content.History");
ps.content.History=function(_e92){
dojo.lang.assertType(_e92,String);
this._m_stack=new dojo.collections.Stack([_e92]);
this.add=function(_e93){
dojo.lang.assertType(_e93,String);
if(_e93!==this.getCurrent()){
this._m_stack.push(_e93);
}
};
this.back=function(){
dojo.lang.assert(this.canGoBack());
return this._m_stack.pop();
};
this.canGoBack=function(){
return this._m_stack.count>1;
};
this.getCurrent=function(){
return this._m_stack.peek();
};
};
dojo.provide("ps.widget.ContentPaneProgress");
ps.widget.ContentPaneProgress=function(_e94){
dojo.lang.assert(_e94&&_e94.onDownloadStart&&_e94.onDownloadEnd,"Content pane must be defined");
var _e95=_e94.domNode;
var _e96=_e95.style.cursor;
var _e97="wait";
function isIndicating(){
return _e95.style.cursor===_e97;
}
function start(){
if(isIndicating()){
return;
}
_e95.style.cursor=_e97;
}
if(!_e94.isLoaded){
start();
}
dojo.event.connect(_e94,"onDownloadStart",function(){
start();
});
dojo.event.connect(_e94,"onDownloadEnd",function(){
_e95.style.cursor=_e96===_e97?"":_e96;
});
};
dojo.provide("ps.content.SelectTemplates");
ps.content.SelectTemplates=function(){
this.isAsDialog=true;
this.preferredHeight=450;
this.preferredWidth=750;
this.parentMode="";
this.init=function(url){
this.url=url;
};
this.maybeCreateSelectTemplateDialog=function(){
if(this.wgtDlg){
return;
}
this.wgtDlg=ps.createDialog({id:"ps.content.SelectTemplatesDlg",title:psxGetLocalMessage("javascript.ps.content.selecttemplates@Templates"),href:this.url},"750px","450px");
new ps.widget.ContentPaneProgress(this.wgtDlg);
var _e99=this;
this.wgtDlg.closeWindow=function(){
_e99.cancelCallback();
this.hide();
};
dojo.event.connect(this.wgtDlg,"onLoad",function(){
_e99.parseControls();
var _e9a=_e99.wgtTemplates;
if(_e9a.options.length>0){
var idx=0;
var _e9c=_e99.snippetId.getTemplateId();
for(i=0;i<_e9a.options.length;i++){
var val=_e9a.options[i].value;
var _e9e=new ps.aa.ObjectId(val);
if(_e9e.getTemplateId()==_e9c){
idx=i;
break;
}
}
_e9a.selectedIndex=idx;
_e99.onTemplateChoice();
}else{
_e99.wgtPreviewPane.setContent("No templates associated to the slot.");
}
});
};
this.initAsPanel=function(_e9f){
if(_e9f!=undefined&&_e9f!=null){
this.parentMode=_e9f;
}
this.isAsDialog=false;
this.parseControls();
var _ea0=this.wgtTemplates;
if(_ea0.options.length==0){
this.wgtPreviewPane.setContent("No templates associated to the slot.");
}else{
this.wgtTemplates.selectedIndex=0;
this.onTemplateChoice();
}
};
this.parseControls=function(){
var _ea1=this;
this.wgtTemplates=document.getElementById("ps.select.templates.wgtTemplates");
this.wgtPreviewPane=dojo.widget.byId("ps.select.templates.wgtPreviewPane");
this.wgtTemplates.onchange=function(){
_ea1.onTemplateChoice();
};
if(this.isAsDialog){
this.wgtButtonSelect=dojo.widget.byId("ps.select.templates.wgtButtonSelect");
this.wgtButtonCancel=dojo.widget.byId("ps.select.templates.wgtButtonCancel");
this.wgtButtonCancel.onClick=function(){
_ea1.cancelCallback();
_ea1.wgtDlg.hide();
};
this.wgtButtonSelect.onClick=function(){
var id=_ea1.getSelectedId();
if(id){
_ea1.okCallback(id,_ea1.snippetId);
_ea1.wgtDlg.hide();
}
};
}
};
this.open=function(_ea3,_ea4,_ea5){
if(!this.isAsDialog){
return;
}
dojo.lang.assertType(_ea5,ps.aa.ObjectId);
this.snippetId=_ea5;
this.okCallback=_ea3;
this.cancelCallback=_ea4;
var _ea6=this.url+"?objectId="+escape(_ea5.serialize());
this.maybeCreateSelectTemplateDialog();
this.wgtDlg.setUrl(_ea6);
ps.util.setDialogSize(this.wgtDlg,this.preferredWidth,this.preferredHeight);
this.wgtDlg.show();
};
this.onTemplateChoice=function(){
var id=this.getSelectedId();
if(id){
this.loadPreviewPane(id);
}
};
this.getSelectedId=function(){
var _ea8=this.wgtTemplates.selectedIndex;
if(_ea8==-1){
return null;
}
var _ea9=this.wgtTemplates.options[_ea8];
return new ps.aa.ObjectId(_ea9.value);
};
this.loadPreviewPane=function(_eaa){
dojo.lang.assertType(_eaa,ps.aa.ObjectId);
var _eab=_eaa.getTemplateId();
var _eac=false;
var _ead="";
var _eae=null;
var _eae=ps.io.Actions.getItemTemplatesForSlot(_eaa);
if(_eae.isSuccess()){
var res=_eae.getValue();
for(i=0;i<res.length;i++){
if(res[i].variantid==_eab){
if(res[i].outputformat=="Binary"){
_eac=true;
}
break;
}
}
}
if(this.parentMode==ps.util.BROWSE_MODE_RTE_INLINE_IMAGE){
_eae=ps.io.Actions.getUrl(_eaa,"CE_LINK");
if(_eae.isSuccess()){
_ead="<img src=\""+_eae.getValue().url+"\">";
}
}else{
if(this.parentMode==ps.util.BROWSE_MODE_RTE_INLINE_LINK&&_eac){
var _eb0=false;
_eae=ps.io.Actions.getSnippetMimeType(_eaa);
if(_eae.isSuccess()){
mType=_eae.getValue().mimetype;
_eb0=mType.toLowerCase().indexOf("image/")!=-1;
}
_eae=ps.io.Actions.getUrl(_eaa,"CE_LINK");
if(_eae.isSuccess()){
if(_eb0){
_ead="<img src=\""+_eae.getValue().url+"\">";
}else{
_ead="<table width=\"100%\" height=\"100%\"><tr><td valign=\"middle\" align=\"center\">";
_ead+="<a target=\"_new\" href=\""+_eae.getValue().url+"\">"+"<u>Click To View Binary</u>"+"</a>";
_ead+="</td></tr></table>";
}
}
}else{
if(!_eac){
var _eb1=null;
try{
_eb1=___selectedContent;
}
catch(ignore){
}
_eae=ps.io.Actions.getSnippetContent(_eaa,false,_eb1);
if(_eae.isSuccess()){
_ead=_eae.getValue();
}
}
}
}
if(_ead==""){
_ead="No preview available.";
}
this.wgtPreviewPane.setContent(_ead);
};
};
dojo.provide("ps.util");
ps.util=new function(){
};
ps.util.addParamToUrl=function(url,name,_eb4){
url.indexOf("?")==-1?url+="?":url+="&";
url+=name+"="+_eb4;
return url;
};
ps.util.error=function(_eb5){
function hasProperty(o,_eb7){
for(var _eb8 in o){
if(_eb8===_eb7){
return true;
}
}
return false;
}
error_val=_eb5;
var _eb9;
if(!_eb5){
var _eba="An Error Occured!";
}else{
if(dojo.lang.isString(_eb5)){
var _eba=_eb5;
}else{
if(_eb5 instanceof ps.io.Response){
dojo.lang.assert(!_eb5.isSuccess(),"error() was called with ps.io.Response indicating success. "+"It should be called on an error only.");
ps.util.error(_eb5.getValue());
return;
}else{
if(hasProperty(_eb5,"message")){
ps.util.error(_eb5.message);
return;
}else{
_eb9=true;
var _eba="An Unrecognized Error Occured!";
}
}
}
}
dojo.debug(_eba);
if(_eb9){
dojo.debug(_eb5);
}
alert(_eba);
};
ps.util.findNodeById=function(_ebb,id){
dojo.lang.assert(dojo.lang.isArrayLike(_ebb));
dojo.lang.assertType(id,String);
for(var i in _ebb){
var node=_ebb[i];
if(node.id===id&&node.nodeType===dojo.html.ELEMENT_NODE){
return node;
}
}
for(var i in _ebb){
var node=_ebb[i];
if(node.nodeType===dojo.html.ELEMENT_NODE){
var _ebf=ps.util.findNodeById(node.childNodes,id);
if(_ebf){
return _ebf;
}
}
}
return null;
};
ps.util.swapNodes=function(_ec0,_ec1){
dojo.lang.assert(_ec0);
dojo.lang.assert(_ec1);
var _ec2=_ec0.parentNode;
var _ec3=_ec1.parentNode;
dojo.lang.assert(_ec2);
dojo.lang.assert(_ec3);
var _ec4=document.createElement("div");
_ec2.insertBefore(_ec4,_ec0);
_ec3.insertBefore(_ec0,_ec1);
_ec2.insertBefore(_ec1,_ec4);
dojo.html.destroyNode(_ec4);
};
ps.util.getScreenSize=function(win,_ec6){
if(win==null){
win=window;
}
var doc=win.document;
var dims=new Object();
if(typeof (win.innerWidth)=="number"){
if(_ec6){
dims.width=win.innerWidth;
dims.height=win.innerHeight;
}else{
dims.width=win.outerWidth;
dims.height=win.outerHeight;
}
}else{
if(doc.documentElement&&(doc.documentElement.clientWidth||doc.documentElement.clientHeight)){
dims.width=doc.documentElement.clientWidth;
dims.height=doc.documentElement.clientHeight;
}else{
if(doc.body&&(doc.body.clientWidth||doc.body.clientHeight)){
dims.width=doc.body.clientWidth;
dims.height=doc.body.clientHeight;
}
}
}
return dims;
};
ps.util.setDialogSize=function(_ec9,_eca,_ecb){
dojo.lang.assertType(_ec9,dojo.widget.FloatingPane);
var _ecc=ps.util.getScreenSize();
var _ecd=_ecb*0.96;
var _ece=_eca*0.96;
if(_ecd>=_ecc.height){
_ecb=_ecc.height*0.96;
}
if(_ece>=_ecc.width){
_eca=_ecc.width*0.96;
}
_ec9.resizeTo(_eca,_ecb);
};
ps.util.getDialogSize=function(_ecf){
dojo.lang.assertType(_ecf,dojo.widget.FloatingPane);
return dojo.html.getMarginBox(_ecf.domNode);
};
ps.util.forceDialogResize=function(_ed0,_ed1,_ed2){
dojo.lang.assertType(_ed0,dojo.widget.FloatingPane);
var size=ps.util.getDialogSize(_ed0);
var _ed4=size.width>0?size.width:_ed1;
var _ed5=size.height>0?size.height:_ed2;
ps.util.setDialogSize(_ed0,_ed4-1,_ed5-1);
ps.util.setDialogSize(_ed0,_ed4,_ed5);
};
ps.util.handleIE6FieldDivHover=function(div,_ed7){
if(!dojo.render.html.ie||dojo.render.html.ie70){
return;
}
var _ed8=_ed7?"#ffc":"";
div.style.backgroundColor=_ed8;
};
ps.util.getVisibleSides=function(_ed9){
dojo.lang.assert(_ed9,"Element must be specified");
var box=dojo.html.getBorderBox(_ed9);
var _edb=ps.util.getVisiblePosition(_ed9);
var top=_edb.y;
var left=_edb.x;
var _ede=top+box.height;
var _edf=left+box.width;
return {top:top,left:left,bottom:_ede,right:_edf};
};
ps.util.getVisiblePosition=function(node){
function visibleSize(node,_ee2){
var _ee3=0;
var n=node;
while(n){
var _ee5=dojo.html.getPixelValue(n,_ee2);
if(_ee5){
_ee3+=_ee5;
}
n=n.parentNode;
}
return _ee3;
}
return {x:visibleSize(node,"left"),y:visibleSize(node,"top")};
};
ps.util.createDialog=function(_ee6,_ee7,_ee8,_ee9){
dojo.lang.assert(_ee6,"Parameters should be specifed");
var _eea=true;
if(_ee9==false){
_eea=false;
}
var div=document.createElement("div");
if(_ee7){
div.style.width=_ee7;
}
if(_ee8){
div.style.height=_ee8;
}
div.style.position="absolute";
document.body.appendChild(div);
var p={bgColor:ps.util.DIALOG_BACKGROUND,bgOpacity:ps.util.DIALOG_BACKGROUND_OPACITY,toggle:"explode",toggleDuration:10,constrainToContainer:true,hasShadow:true,resizable:_eea,executeScripts:true,cacheContent:false};
dojo.lang.mixin(p,_ee6);
return dojo.widget.createWidget("ModalFloatingPane",p,div);
};
ps.util.selectAll=function(e){
var targ;
if(!e){
var e=window.event;
}
if(e.target){
targ=e.target;
}else{
if(e.srcElement){
targ=e.srcElement;
}
}
if(targ.nodeType==3){
targ=targ.parentNode;
}
targ.select();
};
ps.util.ShowPageLinkDialog=function(text){
var div=document.createElement("div");
div.style.position="absolute";
div.style.border="0px";
document.body.appendChild(div);
var dlg=dojo.widget.createWidget("ModalFloatingPane",{id:"ps.pageLinkDiv",title:"Paste link into email or IM ",titleBarDisplay:true,displayCloseAction:true,bgColor:ps.DIALOG_BACKGROUND,bgOpacity:ps.DIALOG_BACKGROUND_OPACITY,executeScripts:true,resizable:false},div);
dlg.setContent("<input onfocus=\"ps.util.selectAll(event)\" id=\"ps.util.wgtShowPageLink\" type=\"text\" size=\"60\" readonly=\"true\" value=\""+text+"\" />");
ps.util.setDialogSize(dlg,440,70);
dlg.show();
var foo=dojo.byId("ps.util.wgtShowPageLink");
foo.focus();
};
ps.util.CreatePromptDialog=function(_ef3){
var _ef4=this;
this.dlgTitle="Prompt Dialog";
this.promptTitle="Text";
this.promptText="";
this.textRequired=false;
this.okBtnText="OK";
this.cancelBtnText="Cancel";
this.okBtnCallBack=null;
this.cancelBtnCallBack=null;
if(_ef3!=null){
this.dlgTitle=_ef3.dlgTitle;
this.promptTitle=_ef3.promptTitle;
this.promptText=_ef3.promptText;
this.textRequired=_ef3.textRequired;
this.okBtnText=_ef3.okBtnText;
this.cancelBtnText=_ef3.cancelBtnText;
this.okBtnCallBack=_ef3.okBtnCallBack;
this.cancelBtnCallBack=_ef3.cancelBtnCallBack;
}
var div=document.createElement("div");
div.style.position="absolute";
div.style.border="0px";
document.body.appendChild(div);
this.wgtDlg=dojo.widget.createWidget("ModalFloatingPane",{id:"ps.promptDiv",title:this.dlgTitle,titleBarDisplay:true,displayCloseAction:true,bgColor:ps.DIALOG_BACKGROUND,bgOpacity:ps.DIALOG_BACKGROUND_OPACITY,executeScripts:true,resizable:false},div);
dojo.event.connect(this.wgtDlg,"onLoad",function(){
_ef4.wgtButtonOk=dojo.widget.byId("ps.util.promptButtonSelect");
_ef4.wgtButtonCancel=dojo.widget.byId("ps.util.promptButtonCancel");
_ef4.wgtPromptText=dojo.byId("ps.util.promptInput");
_ef4.wgtButtonCancel.onClick=function(){
if(_ef4.cancelBtnCallBack){
_ef4.cancelBtnCallBack(_ef4.wgtPromptText.value);
}else{
_ef4.wgtDlg.hide();
}
};
_ef4.wgtButtonOk.onClick=function(){
if(_ef4.textRequired&&ps.util.trim(_ef4.wgtPromptText.value).length<1){
alert(_ef4.promptTitle+" is required.");
_ef4.wgtDlg.focusTitle();
return;
}
if(_ef4.okBtnCallBack){
_ef4.okBtnCallBack(_ef4.wgtPromptText.value);
}
};
});
this.wgtDlg.focusTitle=function(){
_ef4.wgtPromptText.focus();
};
var _ef6="<div>"+this.promptTitle+"</div>"+"<div>"+"<input id='ps.util.promptInput' type='text' size='60' value='"+this.promptText+"' /></div>"+"<br />"+"<div class='PsAaButtonBox'>"+"<button dojoType='ps:PSButton' id='ps.util.promptButtonCancel'>"+this.cancelBtnText+"</button>"+"<button dojoType='ps:PSButton' id='ps.util.promptButtonSelect'>"+this.okBtnText+"</button>"+"</div>";
this.wgtDlg.setContent(_ef6);
ps.util.setDialogSize(this.wgtDlg,440,120);
return this.wgtDlg;
};
ps.util.compareIgnoreCase=function(s1,s2){
s1=s1.toLowerCase();
s2=s2.toLowerCase();
if(s1>s2){
return 1;
}else{
if(s1<s2){
return -1;
}else{
return 0;
}
}
};
ps.util.trim=function(str){
if(!str){
return "";
}
dojo.lang.assertType(str,String);
return str.replace(/^\s\s*/,"").replace(/\s\s*$/,"");
};
ps.util.getElementStyleSheetCss=function(_efa,_efb){
if(!_efa||ps.util.trim(_efa).length<1){
return "";
}
dojo.lang.assertType(_efa,String);
var _efc=_efb&&_efb==true;
var _efd="";
_efa=ps.util.trim(_efa).toLowerCase();
var _efe=document.styleSheets;
for(var i=0;i<_efe.length;i++){
var _f00=_efe[i].cssRules?_efe[i].cssRules:_efe[i].rules;
if(_f00.length>0){
for(var j=0;j<_f00.length;j++){
var s=_f00[j].style;
if(_f00[j].selectorText&&ps.util.trim(_f00[j].selectorText).toLowerCase()==_efa){
_efd=_efd==""?s.cssText:_efd+";"+s.cssText;
if(_efc){
s.cssText="";
}
}
}
}
}
return _efd;
};
ps.util.enableStyleSheet=function(_f03,_f04){
if(!_f03||ps.util.trim(_f03).length<1){
return;
}
dojo.lang.assertType(_f03,String);
var _f05=document.styleSheets;
_f03=_f03.toLowerCase();
for(var i=0;i<_f05.length;i++){
var _f07=_f05[i];
var href=ps.util.trim(_f07.href).toLowerCase();
if(dojo.string.endsWith(href,_f03)){
_f07.disabled=!_f04;
}
}
};
ps.util.addPlaceholders=function(doc){
var _f0a=doc.getElementsByTagName("div");
for(i=0;i<_f0a.length;i++){
var _f0b=_f0a[i].className;
var _f0c=_f0a[i].childNodes.length==0;
var _f0d=null;
if(_f0b=="PsAaPage"){
_f0d="Page";
}
if(_f0b=="PsAaSlot"){
_f0d="Slot";
}else{
if(_f0b=="PsAaSnippet"){
_f0d="Snippet";
}else{
if(_f0b=="PsAaField"){
_f0d="Field";
}
}
}
if(_f0d!=null&&_f0c){
var _f0e=document.createElement("div");
_f0e.className="PsAaPlaceholder";
var _f0f=document.createTextNode("Empty "+_f0d);
_f0e.appendChild(_f0f);
_f0a[i].appendChild(_f0e);
}
}
};
ps.util.getServerProperty=function(_f10,_f11){
dojo.lang.assertType(_f10,String);
if(!_f11){
_f11="";
}
if(ps.util._serverProperties==null){
var _f12=ps.io.Actions.getServerProperties();
if(!_f12.isSuccess()){
ps.io.Actions.maybeReportActionError(_f12);
return "";
}
ps.util._serverProperties=_f12.getValue();
}
if(ps.util._serverProperties[_f10]){
return ps.util._serverProperties[_f10];
}else{
return _f11;
}
};
ps.util.showHidePlaceholders=function(doc,_f14){
var _f15=doc.getElementsByTagName("div");
for(i=0;i<_f15.length;i++){
var _f16=_f15[i].className;
if(_f16=="PsAaPlaceholder"){
if(_f14){
_f15[i].style.display="block";
}else{
_f15[i].style.display="none";
}
}
}
};
ps.util._serverProperties=null;
ps.util.DIALOG_BACKGROUND="white";
ps.util.DIALOG_BACKGROUND_OPACITY=0.1;
ps.util.BROWSETAB_SITES_PANEL_PREF="ps.content.sitespanel";
ps.util.BROWSETAB_FOLDERS_PANEL_PREF="ps.content.folderspanel";
ps.util.BROWSETAB_SEARCH_PANEL_PREF="ps.content.searchpanel";
ps.util.CONTENT_BROWSE_URL="../../ui/content/ContentBrowserDialog.jsp";
ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY="activeAssembly";
ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR="activeAssemblyTable";
ps.util.BROWSE_MODE_RTE_INLINE="rteInline";
ps.util.BROWSE_MODE_RTE_INLINE_LINK="rteInlineLink";
ps.util.BROWSE_MODE_RTE_INLINE_IMAGE="rteInlineImage";
dojo.lang.mixin(ps,ps.util);
dojo.provide("ps.content.BrowseTabPanel");
dojo.declare("ps.content.BrowseTabPanel",null,function(_f17){
dojo.lang.assert(_f17,"Parent must be specified");
this.parent=_f17;
this.isSearchForm=true;
this.isSelectTemplateMode=false;
this.lastButtonState=new dojo.collections.Dictionary();
this.templatesPanelObj=null;
},{init:function(){
dojo.event.topic.subscribe(this.tab.parent.domNode.id+"-selectChild",this,"_onTabSelected");
this.tab.setUrl(this.url);
if(this._isTabLoaded()){
this._initOnLoad();
}else{
var _f18=this;
dojo.event.connect(this.tab,"onLoad",function(){
try{
_f18._initOnLoad();
}
catch(e){
dojo.debug(e);
}
});
}
},_redoLayout:function(){
var _f19=this;
dojo.lang.setTimeout(function(){
var p=_f19.tab.parent;
var w=p.width;
w=w%2?w+1:w-1;
var _f1c=p.domNode.style.width;
var _f1d=p.domNode.style.height;
p.resizeTo(w,p.height);
p.domNode.style.width=_f1c;
p.domNode.style.height=_f1d;
},1000);
},_isTabLoaded:function(){
return !!this._getWidgetById("okButton");
},_initOnLoad:function(){
this.rootContentActions=this._defineRootContentActions();
dojo.lang.assert(this._isTabLoaded(),"Tab "+this.prefix+" should be loaded already");
this.parseControls();
if(this.selectOnLoad){
this.selectOnLoad=false;
this._onTabSelected(this.tab);
}
this._redoLayout();
},_onTabSelected:function(tab){
if(tab!==this.tab){
return;
}
this.parent.currentTab=this;
if(!this.slotId||this.slotId.serialize()!==this.parent.slotId.serialize()){
if(!this._isTabLoaded()){
this.selectOnLoad=true;
return;
}
this.slotId=this.parent.slotId;
if(!this.pathHistory){
this.pathHistory=this._createInitialHistory();
}
this._refresh();
}
ps.util.forceDialogResize(this.parent.wgtDlg,this.parent.preferredWidth,this.parent.preferredHeight);
},_isTabLoaded:function(){
return !!this._getWidgetById("okButton");
},parseControls:function(){
},_parseCommonControls:function(){
this.okButton=this._getWidgetById("okButton");
this.cancelButton=this._getWidgetById("cancelButton");
this.mainSplitPane=this._getWidgetById("mainsplitpane");
this.contentSplitPane=this._getWidgetById("contentsplitpane");
this.commandPanel=this._getWidgetById("commandpanel");
dojo.event.connect(this.okButton,"onClick",this,"_onOk");
dojo.event.connect(this.cancelButton,"onClick",this,"_onCancel");
},_onOk:function(){
if(this.isSelectTemplateMode){
this._handleTemplateOk();
return true;
}
var row=this.contentTable.getSelectedData();
var a=this._getRowA(row);
a.onclick();
},_addContentTableColumn:function(_f21,_f22){
dojo.lang.assertType(_f21,String);
dojo.lang.assertType(_f22,String);
var col=this._cloneColumn(this.contentTableColumns[0]);
col.field=_f21;
col.label=_f22;
this.contentTable.columns.push(col);
},_getUISiteName:function(){
return null;
},_getUIFolderPath:function(){
return null;
},_mustById:function(id){
var node=dojo.byId(id);
dojo.lang.assert(node,"Could not find node "+id);
return node;
},_handleTemplateOk:function(){
var _f26=this.templatesPanelObj.getSelectedId();
var _f27=ps.io.Actions.resolveSiteFolders(this._getUISiteName(),this._getUIFolderPath());
if(_f27.isSuccess()){
var temp=_f27.getValue();
if(temp.sys_folderid!=undefined){
_f26.setFolderId(temp.sys_folderid);
}
if(temp.sys_siteid!=undefined){
_f26.setSiteId(temp.sys_siteid);
}
}else{
ps.io.Actions.maybeReportActionError(_f27);
}
var _f29=this.parent.slotId;
if(this.parent.mode==ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY||this.parent.mode==ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR){
var _f27=ps.io.Actions.addSnippet(_f26,_f29,this._getUIFolderPath(),this._getUISiteName());
if(_f27.isSuccess()){
this.parent.okCallback(_f29);
if(this.parent.mode==ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY_TABLE_EDITOR){
}else{
var _f2a=_f27.getValue();
var _f2b=this.parent.refRelId;
var _f2c=this.parent.position;
ps.aa.controller.repositionSnippet(_f29,_f2b,_f2a,_f2c);
this.parent.refRelId=_f2a;
this.parent.position="after";
}
window.open("","_self").close();
}else{
ps.io.Actions.maybeReportActionError(_f27);
}
}else{
this.parent.okCallback(_f26);
}
this._maybeRestoreButtonState("okButton");
this.cancelButton.setCaption(this.CLOSE_LABEL);
this.setSelectTemplateMode(false);
},_handleTemplateCancel:function(){
if(!this.isSelectTemplateMode){
return true;
}
this._maybeRestoreButtonState("okButton");
this.cancelButton.setCaption(this.CLOSE_LABEL);
this.setSelectTemplateMode(false);
return false;
},_onCancel:function(){
var _f2d=this._handleTemplateCancel();
if(_f2d){
window.open("","_self").close();
}
},_setButtonRememberState:function(name,_f2f,_f30,_f31){
var obj=new Object();
obj.disabled=_f2f.disabled;
obj.ref=_f2f;
obj.caption=_f2f.caption;
this._maybeSetButton(_f2f,_f30,_f31);
this.lastButtonState.add(name,obj);
},_maybeSetButton:function(_f33,_f34,_f35){
if(_f35!=null&&_f33.disabled!=_f35){
_f33.setDisabled(_f35);
if(!_f35){
_f33.setDisabled(_f35);
}
}
if(_f34!=null&&_f33.caption!=_f34){
_f33.setCaption(_f34);
}
},_maybeRestoreButtonState:function(name){
if(this.lastButtonState.containsKey(name)){
var obj=this.lastButtonState.item(name);
this._maybeSetButton(obj.ref,obj.caption,obj.disabled);
this.lastButtonState.remove(name);
}
},_onFilterTyped:function(){
var _f38=this;
if(this.lastFilterValue===this.filterText.value){
return;
}
this.lastFilterValue=this.filterText.value;
dojo.lang.setTimeout(function(){
_f38._filterContentTable();
},10);
},_filterContentTable:function(){
var _f39=this;
this.contentTable.setFilter(psxGetLocalMessage("javascript.ps.content.browse@Name"),function(_f3a){
var str=_f3a.toLowerCase();
var _f3c=_f39.filterText.value.toLowerCase();
return dojo.string.isBlank(_f3c)||_f39._getCellText(str).indexOf(_f3c)!==-1;
});
},_onContentTableSelect:function(obj){
this._maybeRestoreButtonState("okButton");
this._maybeSetButton(this.okButton,this.okButton.caption,!this.contentTable.getSelectedData());
},_onCTypeChanged:function(){
this._refresh();
},_onCTypeFocused:function(){
var list=this.getContentTypeList();
if(!list.slotId||list.slotId.serialize()!=this.slotId.serialize()){
list.slotId=this.slotId;
var _f3f=ps.io.Actions.getAllowedContentTypeForSlot(this.slotId);
ps.io.Actions.maybeReportActionError(_f3f);
var _f40=0;
if(_f3f.isSuccess()){
var _f41=list.options[0].nextSibling;
var _f42=_f3f.getValue();
_f40=_f42.length;
for(i=0;i<_f40;i++){
var _f43=document.createElement("option");
_f43.appendChild(document.createTextNode(_f42[i].name));
_f43.value=_f42[i].contenttypeid;
list.insertBefore(_f43,_f41);
}
list.options.length=1+_f40;
}
}
},_onPathChanged:function(){
this._setLastStoredPath(this.getFolder());
this.pathText.value=this.getFolder();
this._setButtonDisabledSpecial(this.backButton,!this.pathHistory.canGoBack());
this._setButtonDisabledSpecial(this.upButton,this.getPath()===this.ROOT);
this._maybeRestoreButtonState("okButton");
this._maybeSetButton(this.okButton,psxGetLocalMessage("javascript.ps.content.browse@Open"),true);
this._scrollPathText();
this.filterText.value="";
},_setButtonDisabledSpecial:function(_f44,_f45){
if(_f44.disabled==_f45){
return;
}
_f44.setDisabled(_f45);
var node=_f44.containerNode?_f44.containerNode:_f44.domNode;
var img=node.getElementsByTagName("img")[0];
dojo.lang.assert(img,"Expected to find an image in a button content.");
if(_f45){
var from=/16\.gif/;
var to="_disabled16.gif";
}else{
var from=/\_disabled16\.gif/;
var to="16.gif";
}
img.src=img.src.replace(from,to);
},_scrollPathText:function(){
try{
this._setCaretToEnd(this.pathText);
}
catch(ignore){
}
if(this.pathText.dispatchEvent){
try{
var e=document.createEvent("KeyboardEvent");
e.initKeyEvent("keypress",true,true,window,false,false,false,false,32,32);
this.pathText.dispatchEvent(e);
var e=document.createEvent("KeyboardEvent");
e.initKeyEvent("keypress",true,true,window,false,false,false,false,8,0);
this.pathText.dispatchEvent(e);
}
catch(ignore){
}
}else{
}
},_goTo:function(path,row){
dojo.lang.assertType(path,String);
var _f4d=this;
function maybeSetContent(_f4e){
if(_f4e.isSuccess()){
_f4d.pathHistory.add(path);
_f4d.setContent(_f4e.getValue());
}
}
var last=path.length-1;
if(last!==0&&path.charAt(last)==="/"){
path=path.substring(0,path.length-1);
}
if(path===this.ROOT){
var _f50=this.rootContentActions[this.prefix];
dojo.lang.assert(_f50,"Root content action for "+this.prefix+" should be specified");
var _f51=_f50();
maybeSetContent(_f51);
}else{
if(this.isItemPath(path)){
var _f52=this.parent.slotId.clone();
var cid=this.parseContentIdFromPath(path);
_f52.setContentId(cid);
_f52.setSnippetNode();
this.templatesPanelObj=this._loadTemplatesPanel(_f52);
if(row){
this.templatesPanelObj.siteName=row.Site;
this.templatesPanelObj.folderPath=row.Folder;
}
this.setSelectTemplateMode(true);
this._setButtonRememberState("okButton",this.okButton,psxGetLocalMessage("javascript.ps.content.browse@Select"),false);
this.cancelButton.setCaption(psxGetLocalMessage("javascript.ps.content.browse@Back"));
return;
}else{
var _f51=this._getFolderChildren(path);
maybeSetContent(_f51);
}
}
dojo.lang.assert(_f51,"Response should be defined");
ps.io.Actions.maybeReportActionError(_f51);
_f51.isSuccess()&&this._onPathChanged();
},_cloneColumns:function(_f54){
dojo.lang.assertType(_f54,Array);
var _f55=[];
dojo.lang.forEach(_f54,function(_f56){
_f55.push(this._cloneColumn(_f56));
},this);
return _f55;
},_cloneColumn:function(_f57){
return dojo.lang.shallowCopy(_f57,false);
},parseContentIdFromPath:function(path){
var temp=path.split("|");
if(temp.length<2){
return "";
}
return dojo.string.trim(temp[1]);
},refreshBrowser:function(){
this._refresh();
},_refresh:function(){
this._goTo(this.getPath());
},_assertValidRow:function(row){
dojo.lang.assertType(row.Name,String);
dojo.lang.assertType(row.Description,String);
dojo.lang.assertType(row.Type,Number);
},_defineRootContentActions:function(){
var _f5b={};
_f5b[ps.util.BROWSETAB_SITES_PANEL_PREF]=function(){
return ps.io.Actions.getSites();
};
_f5b[ps.util.BROWSETAB_FOLDERS_PANEL_PREF]=function(){
return ps.io.Actions.getRootFolders();
};
return _f5b;
},_createInitialHistory:function(){
if(this.isSearchTab()){
return new ps.content.History("");
}
if(this._getLastStoredPath()){
var path=this._getLastStoredPath();
}else{
if(this.isSiteTab()){
var path=this._getCurrentSitePath();
}else{
var path=this.ROOT;
}
}
dojo.lang.assert(path);
if(!this._isValidPath(path)){
path=this.ROOT;
}
return new ps.content.History(path);
},_getLastStoredPath:function(){
return dojo.io.cookie.getCookie(this.LAST_PATH_COOKIE);
},_setLastStoredPath:function(path){
dojo.lang.assertType(path,String);
var _f5e=100;
dojo.io.cookie.setCookie(this.LAST_PATH_COOKIE,path,_f5e);
},_isValidPath:function(path){
return path===this.ROOT||this._getFolderChildren(path).isSuccess();
},_getCurrentSitePath:function(){
if(this.mode!=ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY){
return this.ROOT;
}
var _f60=ps.aa.controller.pageId.getSiteId();
dojo.lang.assert(dojo.lang.isNumeric(_f60),"Can't get site id");
var _f61=ps.io.Actions.getSites();
ps.io.Actions.maybeReportActionError(_f61);
if(_f61.isSuccess()){
var _f62=_f61.getValue();
var _f63;
dojo.lang.forEach(_f62,function(row){
if(row.Id==_f60){
_f63=row.Name;
}
},this);
dojo.lang.assert(_f63,"Site name was not found");
var path=this.ROOT+this._getCellText(_f63);
}else{
var path=this.ROOT;
}
dojo.lang.assert(path,"path must be defined at this point");
return path;
},_getFolderChildren:function(path){
dojo.lang.assertType(path,String);
return ps.io.Actions.getFolderChildren(path,this._getContentType(),this.parent.slotId.getSlotId(),this.isSiteTab());
},_setSelectionRange:function(_f67,_f68,_f69){
if(_f67.setSelectionRange){
_f67.setSelectionRange(_f68,_f69);
}else{
if(_f67.createTextRange){
var _f6a=_f67.createTextRange();
_f6a.collapse(true);
_f6a.moveEnd("character",_f69);
_f6a.moveStart("character",_f68);
_f6a.select();
}
}
},_setCaretToEnd:function(_f6b){
this._setSelectionRange(_f6b,_f6b.value.length,_f6b.value.length);
},setContent:function(rows){
dojo.lang.assertType(rows,Array);
rows.length>0&&this._assertValidRow(rows[0]);
this._setNameImages(rows);
this.contentTable.store.setData(rows);
this._addContentClickHandlers(rows);
},_setNameImages:function(rows){
dojo.lang.assertType(rows,Array);
rows.length>0&&this._assertValidRow(rows[0]);
var _f6e="<img style=\"vertical-align: middle\" src=\"";
var _f6f=_f6e+this.parent.rxroot+"/sys_resources/images/folder.gif\"/>&nbsp;";
var _f70=_f6e+this.parent.rxroot+"/sys_resources/images/item.gif\"/>&nbsp;";
var _f71=this;
dojo.lang.forEach(rows,function(row){
var img=row.Type===_f71.ITEM_TYPE?_f70:_f6f;
if(row.IconPath&&row.IconPath.length>0){
var _f74=row.IconPath;
if(_f74.substring(0,3)=="../"){
_f74="/Rhythmyx"+_f74.substring(2);
}
img=_f6e+_f74+"\"/>&nbsp;";
}
row.Name=img+row.Name;
});
},_addContentClickHandlers:function(rows){
dojo.lang.assertType(rows,Array);
rows.length>0&&this._assertValidRow(rows[0]);
var path=this.getPath();
if(path!==this.ROOT){
path+="/";
}
this.includeSitesChecked=this.includeSitesCheckbox&&this.includeSitesCheckbox.checked;
this.includeFoldersChecked=this.includeFoldersCheckbox&&this.includeFoldersCheckbox.checked;
dojo.lang.forEach(rows,function(row){
var a=this._getRowA(row);
if(row.Type===this.ITEM_TYPE){
var _f79=(row.Id+"").split(":");
var _f7a=path+this.ITEM_SEPARATOR+_f79[0];
}else{
var _f7a=path+dojo.html.renderedTextContent(a);
}
dojo.lang.assert(_f7a);
var _f7b=this;
var _f7c={myClickFunc:function(){
_f7b._goTo(_f7a,row);
}};
dojo.event.connect(a,"onclick",_f7c,"myClickFunc");
},this);
},_getRowA:function(row){
this._assertValidRow(row);
var tr=this.contentTable.getRow(row);
dojo.lang.assert(tr,"Table row should exist for this data");
var _f7f=tr.getElementsByTagName("td")[0];
dojo.lang.assert(_f7f,"Name cell should exist");
var a=_f7f.getElementsByTagName("a")[0];
dojo.lang.assert(a,"Name should be a hyperlink to open it");
return a;
},setSelectTemplateMode:function(_f81){
dojo.lang.assertType(_f81,Boolean);
this.isSelectTemplateMode=_f81;
if(_f81){
this._maybeCreateTemplatesPanel();
this._setSplitPaneChildVisible(this.mainSplitPane,this.templatesPanel,true);
if(!this.isFolderTab()){
this._maybeCreateTemplatesSiteFolderParam();
this._setSplitPaneChildVisible(this.mainSplitPane,this.templatesSiteFolderParam,true);
}
this._setSplitPaneChildVisible(this.mainSplitPane,this.commandPanel,false);
this._setSplitPaneChildVisible(this.mainSplitPane,this.commandPanel,true);
if(this.isSearchTab()){
this._setSplitPaneChildVisible(this.mainSplitPane,this.contentSplitPane,false);
this.searchBackButton.hide();
}else{
this._setSplitPaneChildVisible(this.mainSplitPane,this.addressbarPanel,false);
this._setSplitPaneChildVisible(this.mainSplitPane,this.clientPanel,false);
}
}else{
if(this.isSearchTab()){
this._setSplitPaneChildVisible(this.mainSplitPane,this.contentSplitPane,true);
if(!this.isSearchForm){
this.searchBackButton.show();
}
}else{
this._setSplitPaneChildVisible(this.mainSplitPane,this.addressbarPanel,true);
this._setSplitPaneChildVisible(this.mainSplitPane,this.clientPanel,true);
}
this._setSplitPaneChildVisible(this.mainSplitPane,this.commandPanel,false);
this._setSplitPaneChildVisible(this.mainSplitPane,this.commandPanel,true);
if(this.templatesPanel){
this._setSplitPaneChildVisible(this.mainSplitPane,this.templatesPanel,false);
}
if(this.templatesSiteFolderParam){
this._setSplitPaneChildVisible(this.mainSplitPane,this.templatesSiteFolderParam,false);
}
}
ps.util.forceDialogResize(this.parent.wgtDlg,this.parent.preferredWidth,this.parent.preferredHeight);
},_maybeCreateTemplatesPanel:function(){
if(this.templatesPanel){
return;
}
var div=document.createElement("div");
div.style.position="absolute";
div.style.padding="10px";
document.body.appendChild(div);
var _f83={id:this.prefix+".templatespanel",sizeMin:200,sizeShare:95,executeScripts:true,cacheContent:false};
this.templatesPanel=dojo.widget.createWidget("ContentPane",_f83,div);
new ps.widget.ContentPaneProgress(this.templatesPanel);
},_maybeCreateTemplatesSiteFolderParam:function(){
if(this.templatesSiteFolderParam){
return;
}
var div=document.createElement("div");
div.style.position="absolute";
div.style.padding="10px";
document.body.appendChild(div);
var _f85={id:this.prefix+".templatessitefolderparam",sizeMin:30,sizeShare:10,executeScripts:true,cacheContent:false};
this.templatesSiteFolderParam=dojo.widget.createWidget("ContentPane",_f85,div);
new ps.widget.ContentPaneProgress(this.templatesSiteFolderParam);
},_loadTemplatesPanel:function(_f86){
dojo.lang.assertType(_f86,ps.aa.ObjectId);
if(!this.isFolderTab()){
this._maybeCreateTemplatesSiteFolderParam();
var _f87=__rxroot+"/ui/content/sitefolderparam.jsp"+"?idPrefix="+escape("ps.select.templates.")+"&includeSitesLabel="+escape(psxGetLocalMessage("javascript.ps.content.browse@Include_Site"))+"&includeFoldersLabel="+escape(psxGetLocalMessage("javascript.ps.content.browse@Include_Folder"));
this.templatesSiteFolderParam.setUrl(_f87);
dojo.event.connect(this.templatesSiteFolderParam,"onLoad",function(){
_f88._onTemplatesSiteFolderParamLoaded();
});
}
this._maybeCreateTemplatesPanel();
var _f87=__rxroot+"/ui/content/selecttemplate.jsp"+"?noButtons=false&objectId="+escape(_f86.serialize());
this.templatesPanel.setUrl(_f87);
var _f89=new ps.content.SelectTemplates();
var _f88=this;
dojo.event.connect(this.templatesPanel,"onLoad",function(){
_f89.initAsPanel(_f88.parent.mode);
});
return _f89;
},_setSplitPaneChildVisible:function(_f8a,_f8b,_f8c){
if(!_f8b){
return;
}
if(_f8c){
var _f8d=false;
for(var x=0;x<_f8a.children.length;x++){
if(_f8a.children[x]===_f8b){
_f8d=true;
break;
}
}
if(!_f8d){
_f8a.addChild(_f8b);
}
}else{
_f8a.removeChild(_f8b);
}
},_onTemplatesSiteFolderParamLoaded:function(){
var _f8f=ps.util.getServerProperty("slotContentIncludeSiteDefaultValue","");
var _f90=ps.util.getServerProperty("slotContentIncludeFolderDefaultValue","");
var _f91=this._mustById("ps.select.templates.includeSitesCheckbox");
_f91.checked=_f8f=="true"?true:false;
_f91.disabled=false;
var _f92=this._mustById("ps.select.templates.includeFoldersCheckbox");
_f92.checked=_f90=="true"?true:false;
_f92.disabled=false;
},_getElemById:function(id){
dojo.lang.assertType(id,String);
return dojo.byId(this._getQId(id));
},_getWidgetById:function(id){
dojo.lang.assertType(id,String);
return dojo.widget.byId(this._getQId(id));
},_getQId:function(s){
return this.prefix+"."+s;
},_getContentType:function(){
var _f96=this.getContentTypeList().value;
if(_f96){
return dojo.string.trim(_f96);
}else{
return "-1";
}
},getContentTypeList:function(){
dojo.lang.assert(!this.isSearchTab());
return this._ctypeList;
},isSiteTab:function(){
return this.prefix===ps.util.BROWSETAB_SITES_PANEL_PREF;
},isSearchTab:function(){
return this.prefix===ps.util.BROWSETAB_SEARCH_PANEL_PREF;
},isFolderTab:function(){
return this.prefix===ps.util.BROWSETAB_FOLDERS_PANEL_PREF;
},getPath:function(){
return this.pathHistory.getCurrent();
},isItemPath:function(path){
path&&dojo.lang.assertType(path,String);
if(!path){
path=this.getPath();
}
return /\|\d+$/.test(path);
},getParentFolder:function(path){
path&&dojo.lang.assertType(path,String);
if(!path){
path=this.getPath();
}
dojo.lang.assert(path!==this.ROOT,"Tried to get parent folder for a root directory");
var _f99=this.isItemPath(path)?this.ITEM_SEPARATOR:"/";
var _f9a=path.lastIndexOf(_f99);
var path=path.substring(0,_f9a);
if(path===""){
path=this.ROOT;
}
return path;
},getFolder:function(path){
path&&dojo.lang.assertType(path,String);
if(!path){
path=this.getPath();
}
return this.isItemPath(path)?this.getParentFolder(path):path;
},_getCellText:function(html){
var _f9d="<a href=\"#\">";
var from=html.lastIndexOf(_f9d)+_f9d.length;
var to=html.lastIndexOf("</a>");
dojo.lang.assert(from>0&&to>0&&from<=to,"Unexpected html of the string: "+html);
return html.substring(from,to);
},ROOT:"/",ITEM_SEPARATOR:"|",LAST_PATH_COOKIE:this.parent.mode+"."+this.prefix+".lastPath",ITEM_TYPE:1,FOLDER_TYPE:2,SITE_TYPE:9,CANCEL_LABEL:psxGetLocalMessage("javascript.ps.content.browse@Cancel"),CLOSE_LABEL:psxGetLocalMessage("javascript.ps.content.browse@Close"),OK_LABEL:psxGetLocalMessage("javascript.ps.content.browse@Ok")});
dojo.provide("ps.content.FolderSitesBaseTabPanel");
dojo.declare("ps.content.FolderSitesBaseTabPanel",ps.content.BrowseTabPanel,function(_fa0){
dojo.lang.assert(_fa0,"Parent must be specified");
this.parent=_fa0;
},{_parseAddressbarPanelControls:function(){
this.pathText=this._getElemById("pathText");
this.refreshButton=this._getWidgetById("refreshButton");
this.backButton=this._getWidgetById("backButton");
this.upButton=this._getWidgetById("upButton");
this.clientPanel=this._getWidgetById("clientpanel");
this.addressbarPanel=this._getWidgetById("addressbarpanel");
dojo.event.connect(this.pathText,"onchange",this,"_onPathTextChanged");
dojo.event.connect(this.refreshButton,"onClick",this,"_onRefresh");
dojo.event.connect(this.backButton,"onClick",this,"_onBack");
dojo.event.connect(this.upButton,"onClick",this,"_onUp");
},parseControls:function(){
this._parseCommonControls();
this._parseFilterPanelControls();
this.contentTable=this._getWidgetById("FilteringTable");
this.contentTableColumns=this._cloneColumns(this.contentTable.columns);
this._parseAddressbarPanelControls();
dojo.event.connect(this.contentTable,"onSelect",this,"_onContentTableSelect");
},_parseFilterPanelControls:function(){
this.filterPanel=this._getWidgetById("filterpanel");
this.filteringTablePanel=this._getWidgetById("tablepanel");
this.filteringTable=this._getWidgetById("FilteringTable");
this.filterText=this._getElemById("filterText");
dojo.event.connect(this.filterText,"onkeyup",this,"_onFilterTyped");
this._ctypeList=this._getElemById("ctypeList");
dojo.event.connect(this.getContentTypeList(),"onchange",this,"_onCTypeChanged");
dojo.event.connectBefore(this.getContentTypeList(),"onfocus",this,"_onCTypeFocused");
},_onPathTextChanged:function(){
this._goTo(this.pathText.value);
},_onRefresh:function(){
this._refresh();
},_onBack:function(){
this.pathHistory.back();
this._refresh();
},_onUp:function(){
this._goTo(this.getParentFolder());
}});
dojo.provide("ps.content.SitesTabPanel");
dojo.declare("ps.content.SitesTabPanel",ps.content.FolderSitesBaseTabPanel,function(_fa1){
dojo.lang.assert(_fa1,"Parent must be specified");
this.prefix=ps.util.BROWSETAB_SITES_PANEL_PREF;
this.parent=_fa1;
},{init:function(){
this.tabId=this.prefix+".tab";
this.tab=dojo.widget.byId(this.tabId);
dojo.lang.assert(this.tab,"Tab for "+this.prefix+" should exist");
this.url=this.parent.rxroot+"/ui/content/sitesfolderpanel.jsp?mode=sites";
ps.content.SitesTabPanel.superclass.init.apply(this);
},_getUISiteName:function(){
var id="ps.select.templates.includeSitesCheckbox";
if(this._mustById(id).checked){
var _fa3=dojo.string.splitEscaped(this.getFolder(),"/");
_fa3=dojo.lang.filter(_fa3,function(part){
return part.length>0;
},this);
return _fa3[0];
}else{
return null;
}
},_getUIFolderPath:function(){
var id="ps.select.templates.includeFoldersCheckbox";
return this._mustById(id).checked?this.getFolder():null;
}});
dojo.provide("ps.content.FoldersTabPanel");
dojo.declare("ps.content.FoldersTabPanel",ps.content.FolderSitesBaseTabPanel,function(_fa6){
dojo.lang.assert(_fa6,"Parent must be specified");
this.prefix=ps.util.BROWSETAB_FOLDERS_PANEL_PREF;
this.parent=_fa6;
},{init:function(){
this.tabId=this.prefix+".tab";
this.tab=dojo.widget.byId(this.tabId);
dojo.lang.assert(this.tab,"Tab for "+this.prefix+" should exist");
this.url=this.parent.rxroot+"/ui/content/sitesfolderpanel.jsp?mode=folders";
ps.content.FoldersTabPanel.superclass.init.apply(this);
},_onTemplatesSiteFolderParamLoaded:function(){
dojo.lang.assert(false,"Templates site folder params pane should not be loaded "+"on the folders tab");
}});
dojo.provide("ps.content.SearchTabPanel");
dojo.declare("ps.content.SearchTabPanel",ps.content.BrowseTabPanel,function(_fa7){
dojo.lang.assert(_fa7,"Parent must be specified");
this.prefix=ps.util.BROWSETAB_SEARCH_PANEL_PREF;
this.parent=_fa7;
this.searchScriptElem=null;
this.isSearchSimple=false;
},{init:function(){
this.tabId=this.prefix+".tab";
this.tab=dojo.widget.byId(this.tabId);
dojo.lang.assert(this.tab,"Tab for "+this.prefix+" should exist");
this.url=this.parent.rxroot+"/ui/content/searchpanel.jsp";
ps.content.SearchTabPanel.superclass.init.apply(this);
},setSearchMode:function(_fa8){
if(!this.isSearchTab()){
return;
}
this.isSearchForm=_fa8;
if(this.isSearchForm){
this._setSplitPaneChildVisible(this.contentSplitPane,this.searchformPanel,true);
this._setSplitPaneChildVisible(this.contentSplitPane,this.filterPanel,false);
this._setSplitPaneChildVisible(this.contentSplitPane,this.filterPanel,true);
this._setSplitPaneChildVisible(this.contentSplitPane,this.filteringTablePanel,false);
dojo.html.hide(this._getQId("nameAndCtypeFilterDiv"));
dojo.html.show(this._getQId("siteAndFolderFilterDiv"));
this.searchBackButton.hide();
this._maybeSetButton(this.okButton,psxGetLocalMessage("javascript.ps.content.browse@Search"),false);
}else{
this._setSplitPaneChildVisible(this.contentSplitPane,this.filteringTablePanel,true);
this._setSplitPaneChildVisible(this.contentSplitPane,this.filterPanel,false);
this._setSplitPaneChildVisible(this.contentSplitPane,this.filterPanel,true);
this._setSplitPaneChildVisible(this.contentSplitPane,this.searchformPanel,false);
dojo.html.show(this._getQId("nameAndCtypeFilterDiv"));
dojo.html.hide(this._getQId("siteAndFolderFilterDiv"));
this.searchBackButton.show();
this._maybeSetButton(this.okButton,psxGetLocalMessage("javascript.ps.content.browse@Open"),false);
}
},_showSearchFields:function(){
if(this.isSearchSimple){
dojo.html.show(dojo.byId("advancedfields"));
this.advancedButton.hide();
this.simpleButton.show();
}else{
dojo.html.hide(dojo.byId("advancedfields"));
this.advancedButton.show();
this.simpleButton.hide();
}
this.isSearchSimple=!this.isSearchSimple;
this._enableDisableSearch();
},_onOk:function(){
if(!this.isSearchForm||this.isSelectTemplateMode){
return ps.content.SearchTabPanel.superclass._onOk.apply(this);
}else{
var _fa9=this.includeSitesCheckbox.checked?"yes":"no";
}
var _faa=this.includeFoldersCheckbox.checked?"yes":"no";
var sf=document.searchQuery;
sf.includeSites.value=_fa9;
sf.includeFolders.value=_faa;
sf.sys_searchMode.value="simple";
if(!this.isSearchSimple){
sf.sys_searchMode.value="advanced";
}
if(!this._validateQueryForSynonymExp()){
return;
}
if(!this._validateQuery()){
return;
}
var _fac=ps.io.Actions.submitForm(document.searchQuery);
if(_fac==null){
return;
}
ps.io.Actions.maybeReportActionError(_fac);
if(!_fac.isSuccess()){
return false;
}
this.contentTable.reset();
var head=this.contentTable.domNode.getElementsByTagName("thead")[0];
dojo.dom.removeChildren(head);
this.contentTable.columns=this._cloneColumns(this.contentTableColumns);
if(this.includeSitesCheckbox.checked){
this._addContentTableColumn("Site","Site");
}
if(this.includeFoldersCheckbox.checked){
this._addContentTableColumn("Folder","Folder");
}
this.setContent(_fac.getValue());
this.setSearchMode(false);
dojo.event.connect(this.contentTable,"onSelect",this,"_onContentTableSelect");
this._setButtonRememberState("okButton",this.okButton,null,true);
return true;
},_onCancel:function(){
if(this.isSelectTemplateMode){
return this._handleTemplateCancel();
}
var _fae=this;
dojo.lang.setTimeout(function(){
_fae.setSearchMode(true);
},500);
if(typeof psSearch!="undefined"){
psSearch=null;
}
ps.content.SearchTabPanel.superclass._onCancel.apply(this);
},_validateQuery:function(){
if(!document.searchQuery.sys_fulltextquery){
return true;
}
var _faf=document.searchQuery.sys_fulltextquery.value;
var _fb0=_faf.charAt(0);
if(_fb0=="*"||_fb0=="?"){
var msg=psxGetLocalMessage("javascript.ps.content.browse@Invalid_First_Char");
alert(msg);
return false;
}else{
return true;
}
},_validateQueryForSynonymExp:function(){
if(!document.searchQuery.sys_synonymexpansion){
return true;
}
var _fb2=document.searchQuery.sys_synonymexpansion.checked;
if(_fb2){
var _fb3=document.searchQuery.sys_fulltextquery.value;
var _fb4="";
var _fb5=new Array("+","-","&&","||","!","(",")","{","}","[","]","^","\"","~","*","?",":","\\");
for(var i=0;i<_fb5.length;i++){
var _fb7=_fb5[i];
if(_fb3.indexOf(_fb7)!=-1){
if(_fb4.length==0){
_fb4=_fb7;
}else{
_fb4+=", "+_fb7;
}
}
}
if(_fb4.length>0){
var msg=psxGetLocalMessage("javascript.ps.content.browse@Invalid_Chars_Synonym_Exp");
alert(msg+" "+_fb4);
return false;
}
}
return true;
},loadSearchForm:function(){
if(!this.isSearchTab()){
return;
}
if(!this._isTabLoaded()){
this.selectOnLoad=true;
return;
}
var _fb9=this.parent.slotId;
if(this.searchformPanel.slotId&&this.searchformPanel.slotId.serialize()===_fb9.serialize()){
return;
}
this.searchformPanel.slotId=_fb9;
if(this.searchScriptElem){
this.searchformPanel.setContent("");
dojo.dom.removeNode(this.searchScriptElem,true);
this.searchScriptElem=null;
}
this.isSearchSimple=false;
var _fba=ps.io.Actions.getUrl(this.parent.slotId,"RC_SEARCH");
if(!_fba.isSuccess()){
ps.io.Actions.maybeReportActionError(_fba);
return;
}
this.searchFormUrl=_fba.getValue().url;
this.searchformPanel.setUrl(this.searchFormUrl+"&genMode=aaHTML");
},_onSearchFormPanelLoad:function(){
if(!dojo.byId("advancedfields")){
return;
}
this.advancedButton=dojo.widget.byId("ps.search.advanced");
this.simpleButton=dojo.widget.byId("ps.search.simple");
this.ftquery=dojo.byId("searchfor");
this._loadSearchScript();
ps.io.Actions.initFormBind(ps.io.Actions.getRcSearchUrl(),"searchQuery",ps.io.Actions.MIMETYPE_JSON);
if(this.advancedButton){
dojo.event.connect(this.advancedButton,"onClick",this,"_showSearchFields");
}
if(this.simpleButton){
dojo.event.connect(this.simpleButton,"onClick",this,"_showSearchFields");
}
if(this.advancedButton){
this._showSearchFields();
}
if(this.ftquery){
dojo.event.connect(this.ftquery,"onkeyup",this,"_enableDisableSearch");
}
this._enableDisableSearch();
},_loadSearchScript:function(){
var head=document.getElementsByTagName("head")[0];
var scr=document.getElementById("ps.content.search.searchScript");
if(scr){
head.removeChild(scr);
}
this.searchScriptElem=document.createElement("script");
this.searchScriptElem.id="ps.content.search.searchScript";
this.searchScriptElem.type="text/javascript";
this.searchScriptElem.src=this.searchFormUrl+"&genMode=aaJS";
head.appendChild(this.searchScriptElem);
},_enableDisableSearch:function(){
var q=this.ftquery;
this._maybeSetButton(this.okButton,null,this.isSearchSimple&&q&&dojo.string.isBlank(q.value));
},_onTabSelected:function(tab){
this.loadSearchForm();
ps.content.SearchTabPanel.superclass._onTabSelected.apply(this,[tab]);
},_initOnLoad:function(){
ps.content.SearchTabPanel.superclass._initOnLoad.apply(this);
this.setSearchMode(true);
},_onSearchAgain:function(){
this.setSearchMode(true);
},parseControls:function(){
this._parseCommonControls();
this._parseFilterPanelControls();
this.contentTable=this._getWidgetById("FilteringTable");
this.contentTableColumns=this._cloneColumns(this.contentTable.columns);
this._parseSearchPanelOnlyControls();
},_parseFilterPanelControls:function(){
this.includeSitesCheckbox=this._getElemById("includeSitesCheckbox");
this.includeFoldersCheckbox=this._getElemById("includeFoldersCheckbox");
this.filterPanel=this._getWidgetById("filterpanel");
this.filteringTablePanel=this._getWidgetById("tablepanel");
this.filteringTable=this._getWidgetById("FilteringTable");
this.filterText=this._getElemById("filterText");
dojo.event.connect(this.filterText,"onkeyup",this,"_onFilterTyped");
var _fbf=ps.util.getServerProperty("slotContentIncludeSiteDefaultValue","");
var _fc0=ps.util.getServerProperty("slotContentIncludeFolderDefaultValue","");
if(_fbf=="true"){
this.includeSitesCheckbox.checked=true;
}
if(_fc0=="true"){
this.includeFoldersCheckbox.checked=true;
}
this._ctypeList=this._getElemById("ctypeList");
dojo.html.hide(this._ctypeList.parentNode);
},_parseSearchPanelOnlyControls:function(){
this.searchformPanel=this._getWidgetById("searchformpanel");
new ps.widget.ContentPaneProgress(this.searchformPanel);
dojo.event.connect(this.searchformPanel,"onLoad",this,"_onSearchFormPanelLoad");
this.searchBackButton=this._getWidgetById("searchBackButton");
dojo.event.connect(this.searchBackButton,"onClick",this,"_onSearchAgain");
},_onTemplatesSiteFolderParamLoaded:function(){
var _fc1=this._mustById("ps.select.templates.includeSitesCheckbox");
_fc1.checked=this.includeSitesChecked;
_fc1.disabled=true;
var _fc2=this._mustById("ps.select.templates.includeFoldersCheckbox");
_fc2.checked=this.includeFoldersChecked;
_fc2.disabled=true;
},_getUISiteName:function(){
if(this.templatesPanelObj.siteName){
return this.templatesPanelObj.siteName;
}else{
return null;
}
},_getUIFolderPath:function(){
if(this.templatesPanelObj.folderPath){
return this.templatesPanelObj.folderPath;
}else{
return null;
}
},_refresh:function(){
}});
dojo.provide("ps.content.Browse");
ps.content.Browse=function(mode){
this.mode=mode;
this.preferredHeight=500;
this.preferredWidth=750;
this.isStandAlone=this.mode!=ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY;
this.init=function(_fc4){
this.rxroot=_fc4;
this.currentTab=null;
};
this.maybeCreateBrowseDialog=function(){
if(this.wgtDlg){
this.searchtab._loadSearchScript();
return;
}
var _fc5=ps.util.getScreenSize();
var _fc6=this.isStandAlone?_fc5.width:this.preferredWidth;
var _fc7=this.isStandAlone?_fc5.height:this.preferredHeight;
this.wgtDlg=ps.createDialog({id:"ps.content.BrowseDlg",title:psxGetLocalMessage("javascript.ps.content.browse@Active_Assembly_Browse_Content"),titleBarDisplay:!this.isStandAlone,hasShadow:!this.isStandAlone,resizable:!this.isStandAlone},_fc6+"px",_fc7+"px");
function tab(_fc8,_fc9){
return "<div dojoType=\"ContentPane\" id=\""+_fc8+".tab\" "+"label=\""+_fc9+"\" preload=\"true\"></div>\n";
}
var _fca="<div id=\"ps.content.mainTabContainer\" dojoType=\"TabContainer\" "+"style=\"width: 100%; height: 100%\">\n"+tab(ps.util.BROWSETAB_SITES_PANEL_PREF,psxGetLocalMessage("javascript.ps.content.browse@Sites"))+tab(ps.util.BROWSETAB_FOLDERS_PANEL_PREF,psxGetLocalMessage("javascript.ps.content.browse@Folders"))+tab(ps.util.BROWSETAB_SEARCH_PANEL_PREF,psxGetLocalMessage("javascript.ps.content.browse@Search"))+"</div>";
this.wgtDlg.setContent(_fca);
var _fcb=this;
this.wgtDlg.closeWindow=function(){
ps.aa.controller.enableConflictStyleSheets(true);
_fcb.currentTab._onCancel();
_fcb.close();
};
this.parseTabControls();
};
this.parseTabControls=function(){
var _fcc=new ps.content.SitesTabPanel(this);
var _fcd=new ps.content.FoldersTabPanel(this);
var _fce=new ps.content.SearchTabPanel(this);
this.searchtab=_fce;
var _fcf=this.rxroot;
_fcc.init();
dojo.lang.delayThese([function(){
_fcd.init();
},function(){
_fce.init();
}],500);
var _fd0=dojo.widget.byId("ps.content.mainTabContainer");
};
this.close=function(){
ps.aa.controller.enableConflictStyleSheets(true);
if(this.isStandAlone){
self.close();
}else{
this.wgtDlg.hide();
}
};
this.open=function(_fd1,_fd2,_fd3,_fd4){
dojo.lang.assert(_fd2,"slotId parameter must be specified");
dojo.lang.assert(_fd2.isSlotNode(),"Must pass a slot id");
ps.aa.controller.enableConflictStyleSheets(false);
if(_fd4==null){
_fd4="before";
}
this.slotId=_fd2;
this.okCallback=_fd1;
this.refRelId=_fd3;
this.position=_fd4;
this.maybeCreateBrowseDialog();
if(!this.isStandAlone){
ps.util.setDialogSize(this.wgtDlg,this.preferredWidth,this.preferredHeight);
}else{
var _fd5=this;
dojo.lang.setTimeout(function(){
_fd5.fillInParentWindow();
},500);
}
var _fd6=dojo.widget.byId("ps.content.mainTabContainer");
var tab0=_fd6.children[0];
_fd6.selectChild(tab0,_fd6);
this.wgtDlg.show();
if(dojo.render.html.ie55||dojo.render.html.ie60){
dojo.html.hide(_fd6.domNode);
dojo.html.show(_fd6.domNode);
}
if(dojo.render.html.safari){
dojo.lang.setTimeout(function(){
_fd6.selectChild(tab0,_fd6);
},500);
}
if(dojo.render.html.ie55||dojo.render.html.ie60){
dojo.lang.setTimeout(function(){
dojo.html.hide(_fd6.domNode);
dojo.html.show(_fd6.domNode);
},1000*5);
}
};
this.fillInParentWindow=function(){
var _fd8=ps.util.getScreenSize(null,true);
this.wgtDlg.domNode.style.top=0;
this.wgtDlg.domNode.style.left=0;
this.wgtDlg.resizeTo(_fd8.width,_fd8.height);
};
};
dojo.provide("ps.widget.ScrollableNodes");
ps.widget.ScrollableNodes=function(){
this.init=function(_fd9){
this.nodes=_fd9;
dojo.lang.forEach(this.nodes,function(node){
dojo.lang.assert(node,"Expected all scrollable nodes to be defined.");
});
};
this.getOverNode=function(e){
dojo.lang.assert(e,"Event must be specified");
for(var i=0;i<this.nodes.length;i++){
var n=this.nodes[i];
if(this._overElement(n,e)){
return n;
}
}
return null;
};
this._overElement=function(_fde,e){
dojo.lang.assert(_fde,"Element must be specified");
dojo.lang.assert(e,"Event must be specified");
var _fe0=ps.util.getVisibleSides(_fde);
return e.clientX>=_fe0.left&&e.clientX<=_fe0.right&&e.clientY>=_fe0.top&&e.clientY<=_fe0.bottom;
};
};
dojo.provide("ps.widget.Autoscroller");
ps.widget.Autoscroller=function(){
this.init=function(_fe1){
var _fe2=this;
this.scrollableNodes.init(_fe1);
dojo.event.connect(dojo.dnd.dragManager,"onMouseUp",function(){
_fe2._stopScroll();
});
dojo.event.connectAround(dojo.dnd.dragManager,"onMouseMove",this,"_onMouseMove");
};
this._onMouseMove=function(_fe3){
dojo.lang.assert(_fe3,"Invocation must be defined");
_fe3.proceed();
if(dojo.dnd.dragManager.dragObjects.length){
var e=_fe3.args[0];
var _fe5=this.scrollableNodes.getOverNode(e);
this._maybeAutoscroll(_fe5,e);
}
};
this._maybeAutoscroll=function(_fe6,_fe7){
dojo.lang.assert(_fe7,"Event for maybeAutoscroll should not be null.");
this._resetTimeout();
var _fe8=true;
if(_fe6){
var _fe9=this._detectAutoscrollArea(_fe6,_fe7);
if(_fe9){
this._scroll(_fe6,_fe9);
_fe8=false;
}
}
if(_fe8){
this._stopScroll();
}
};
this._stopScroll=function(){
this.element=null;
};
this._scroll=function(_fea,_feb){
this.direction=_feb;
if(this._isScrollingStopped(_fea)){
this.element=_fea;
this._doScroll();
}
};
this._doScroll=function(){
var _fec=this.element.scrollLeft;
var _fed=this.element.scrollTop;
this.element.scrollLeft+=this.direction.x*this.SCROLL_DISTANCE;
this.element.scrollTop+=this.direction.y*this.SCROLL_DISTANCE;
dojo.lang.setTimeout(this,"_continueScroll",this.SCROLL_TIME,this.element);
var _fee=_fec!==this.element.scrollLeft||_fed!==this.element.scrollTop;
if(_fee){
dojo.dnd.dragManager.onScroll();
}
return _fee;
};
this._continueScroll=function(_fef){
if(this._isScrollingStopped(_fef)){
return;
}
this._doScroll();
};
this._isScrollingStopped=function(_ff0){
return (!this.element&&_ff0!==this.element)||this._isTimeout();
};
this._resetTimeout=function(){
this.autoscrollTimeout=new Date().getTime()+this.SCROLL_TIMEOUT;
};
this._isTimeout=function(){
return new Date().getTime()>this.autoscrollTimeout;
};
this._detectAutoscrollArea=function(_ff1,e){
dojo.lang.assert(_ff1,"Element must be specified");
dojo.lang.assert(e,"Event must be specified");
var _ff3=ps.util.getVisibleSides(_ff1);
var _ff4=this;
function near(i1,i2){
var d=i2-i1;
return d>=0&&d<=_ff4.AUTOSCROLL_EDGE_DISTANCE;
}
var x=0;
if(near(_ff3.left,e.clientX)){
x-=1;
}
if(near(e.clientX,_ff3.right)){
x+=1;
}
var y=0;
if(near(_ff3.top,e.clientY)){
y-=1;
}
if(near(e.clientY,_ff3.bottom)){
y+=1;
}
return x||y?{x:x,y:y}:null;
};
this.element=null;
this.direction={x:0,y:0};
this.autoscrollTimeout=0;
this.scrollableNodes=new ps.widget.ScrollableNodes();
this.SCROLL_TIME=100;
this.SCROLL_DISTANCE=10;
this.SCROLL_TIMEOUT=5*1000;
this.AUTOSCROLL_EDGE_DISTANCE=50;
};
dojo.provide("dojo.widget.Button");
dojo.widget.defineWidget("dojo.widget.Button",dojo.widget.HtmlWidget,{isContainer:true,caption:"",templatePath:dojo.uri.moduleUri("dojo.widget","templates/ButtonTemplate.html"),templateCssPath:dojo.uri.moduleUri("dojo.widget","templates/ButtonTemplate.css"),inactiveImg:"templates/images/soriaButton-",activeImg:"templates/images/soriaActive-",pressedImg:"templates/images/soriaPressed-",disabledImg:"templates/images/soriaDisabled-",width2height:1/3,fillInTemplate:function(){
if(this.caption){
this.containerNode.appendChild(document.createTextNode(this.caption));
}
dojo.html.disableSelection(this.containerNode);
},postCreate:function(){
this._sizeMyself();
},_sizeMyself:function(){
if(this.domNode.parentNode){
var _ffa=document.createElement("span");
dojo.html.insertBefore(_ffa,this.domNode);
}
dojo.body().appendChild(this.domNode);
this._sizeMyselfHelper();
if(_ffa){
dojo.html.insertBefore(this.domNode,_ffa);
dojo.html.removeNode(_ffa);
}
},_sizeMyselfHelper:function(){
var mb=dojo.html.getMarginBox(this.containerNode);
this.height=mb.height;
this.containerWidth=mb.width;
var _ffc=this.height*this.width2height;
this.containerNode.style.left=_ffc+"px";
this.leftImage.height=this.rightImage.height=this.centerImage.height=this.height;
this.leftImage.width=this.rightImage.width=_ffc+1;
this.centerImage.width=this.containerWidth;
this.centerImage.style.left=_ffc+"px";
this._setImage(this.disabled?this.disabledImg:this.inactiveImg);
if(this.disabled){
dojo.html.prependClass(this.domNode,"dojoButtonDisabled");
this.domNode.removeAttribute("tabIndex");
dojo.widget.wai.setAttr(this.domNode,"waiState","disabled",true);
}else{
dojo.html.removeClass(this.domNode,"dojoButtonDisabled");
this.domNode.setAttribute("tabIndex","0");
dojo.widget.wai.setAttr(this.domNode,"waiState","disabled",false);
}
this.domNode.style.height=this.height+"px";
this.domNode.style.width=(this.containerWidth+2*_ffc)+"px";
},onMouseOver:function(e){
if(this.disabled){
return;
}
if(!dojo.html.hasClass(this.buttonNode,"dojoButtonHover")){
dojo.html.prependClass(this.buttonNode,"dojoButtonHover");
}
this._setImage(this.activeImg);
},onMouseDown:function(e){
if(this.disabled){
return;
}
dojo.html.prependClass(this.buttonNode,"dojoButtonDepressed");
dojo.html.removeClass(this.buttonNode,"dojoButtonHover");
this._setImage(this.pressedImg);
},onMouseUp:function(e){
if(this.disabled){
return;
}
dojo.html.prependClass(this.buttonNode,"dojoButtonHover");
dojo.html.removeClass(this.buttonNode,"dojoButtonDepressed");
this._setImage(this.activeImg);
},onMouseOut:function(e){
if(this.disabled){
return;
}
if(e.toElement&&dojo.html.isDescendantOf(e.toElement,this.buttonNode)){
return;
}
dojo.html.removeClass(this.buttonNode,"dojoButtonHover");
dojo.html.removeClass(this.buttonNode,"dojoButtonDepressed");
this._setImage(this.inactiveImg);
},onKey:function(e){
if(!e.key){
return;
}
var menu=dojo.widget.getWidgetById(this.menuId);
if(e.key==e.KEY_ENTER||e.key==" "){
this.onMouseDown(e);
this.buttonClick(e);
dojo.lang.setTimeout(this,"onMouseUp",75,e);
dojo.event.browser.stopEvent(e);
}
if(menu&&menu.isShowingNow&&e.key==e.KEY_DOWN_ARROW){
dojo.event.disconnect(this.domNode,"onblur",this,"onBlur");
}
},onFocus:function(e){
var menu=dojo.widget.getWidgetById(this.menuId);
if(menu){
dojo.event.connectOnce(this.domNode,"onblur",this,"onBlur");
}
},onBlur:function(e){
var menu=dojo.widget.getWidgetById(this.menuId);
if(!menu){
return;
}
if(menu.close&&menu.isShowingNow){
menu.close();
}
},buttonClick:function(e){
if(!this.disabled){
try{
this.domNode.focus();
}
catch(e2){
}
this.onClick(e);
}
},onClick:function(e){
},_setImage:function(_1009){
this.leftImage.src=dojo.uri.moduleUri("dojo.widget",_1009+"l.gif");
this.centerImage.src=dojo.uri.moduleUri("dojo.widget",_1009+"c.gif");
this.rightImage.src=dojo.uri.moduleUri("dojo.widget",_1009+"r.gif");
},_toggleMenu:function(_100a){
var menu=dojo.widget.getWidgetById(_100a);
if(!menu){
return;
}
if(menu.open&&!menu.isShowingNow){
var pos=dojo.html.getAbsolutePosition(this.domNode,false);
menu.open(pos.x,pos.y+this.height,this);
dojo.event.disconnect(this.domNode,"onblur",this,"onBlur");
}else{
if(menu.close&&menu.isShowingNow){
menu.close();
}else{
menu.toggle();
}
}
},setCaption:function(_100d){
this.caption=_100d;
this.containerNode.innerHTML=_100d;
this._sizeMyself();
},setDisabled:function(_100e){
this.disabled=_100e;
this._sizeMyself();
}});
dojo.widget.defineWidget("dojo.widget.DropDownButton",dojo.widget.Button,{menuId:"",downArrow:"templates/images/whiteDownArrow.gif",disabledDownArrow:"templates/images/whiteDownArrow.gif",fillInTemplate:function(){
dojo.widget.DropDownButton.superclass.fillInTemplate.apply(this,arguments);
this.arrow=document.createElement("img");
dojo.html.setClass(this.arrow,"downArrow");
dojo.widget.wai.setAttr(this.domNode,"waiState","haspopup",this.menuId);
},_sizeMyselfHelper:function(){
this.arrow.src=dojo.uri.moduleUri("dojo.widget",this.disabled?this.disabledDownArrow:this.downArrow);
this.containerNode.appendChild(this.arrow);
dojo.widget.DropDownButton.superclass._sizeMyselfHelper.call(this);
},onClick:function(e){
this._toggleMenu(this.menuId);
}});
dojo.widget.defineWidget("dojo.widget.ComboButton",dojo.widget.Button,{menuId:"",templatePath:dojo.uri.moduleUri("dojo.widget","templates/ComboButtonTemplate.html"),splitWidth:2,arrowWidth:5,_sizeMyselfHelper:function(e){
var mb=dojo.html.getMarginBox(this.containerNode);
this.height=mb.height;
this.containerWidth=mb.width;
var _1012=this.height/3;
if(this.disabled){
dojo.widget.wai.setAttr(this.domNode,"waiState","disabled",true);
this.domNode.removeAttribute("tabIndex");
}else{
dojo.widget.wai.setAttr(this.domNode,"waiState","disabled",false);
this.domNode.setAttribute("tabIndex","0");
}
this.leftImage.height=this.rightImage.height=this.centerImage.height=this.arrowBackgroundImage.height=this.height;
this.leftImage.width=_1012+1;
this.centerImage.width=this.containerWidth;
this.buttonNode.style.height=this.height+"px";
this.buttonNode.style.width=_1012+this.containerWidth+"px";
this._setImage(this.disabled?this.disabledImg:this.inactiveImg);
this.arrowBackgroundImage.width=this.arrowWidth;
this.rightImage.width=_1012+1;
this.rightPart.style.height=this.height+"px";
this.rightPart.style.width=this.arrowWidth+_1012+"px";
this._setImageR(this.disabled?this.disabledImg:this.inactiveImg);
this.domNode.style.height=this.height+"px";
var _1013=this.containerWidth+this.splitWidth+this.arrowWidth+2*_1012;
this.domNode.style.width=_1013+"px";
},_setImage:function(_1014){
this.leftImage.src=dojo.uri.moduleUri("dojo.widget",_1014+"l.gif");
this.centerImage.src=dojo.uri.moduleUri("dojo.widget",_1014+"c.gif");
},rightOver:function(e){
if(this.disabled){
return;
}
dojo.html.prependClass(this.rightPart,"dojoButtonHover");
this._setImageR(this.activeImg);
},rightDown:function(e){
if(this.disabled){
return;
}
dojo.html.prependClass(this.rightPart,"dojoButtonDepressed");
dojo.html.removeClass(this.rightPart,"dojoButtonHover");
this._setImageR(this.pressedImg);
},rightUp:function(e){
if(this.disabled){
return;
}
dojo.html.prependClass(this.rightPart,"dojoButtonHover");
dojo.html.removeClass(this.rightPart,"dojoButtonDepressed");
this._setImageR(this.activeImg);
},rightOut:function(e){
if(this.disabled){
return;
}
dojo.html.removeClass(this.rightPart,"dojoButtonHover");
dojo.html.removeClass(this.rightPart,"dojoButtonDepressed");
this._setImageR(this.inactiveImg);
},rightClick:function(e){
if(this.disabled){
return;
}
try{
this.domNode.focus();
}
catch(e2){
}
this._toggleMenu(this.menuId);
},_setImageR:function(_101a){
this.arrowBackgroundImage.src=dojo.uri.moduleUri("dojo.widget",_101a+"c.gif");
this.rightImage.src=dojo.uri.moduleUri("dojo.widget",_101a+"r.gif");
},onKey:function(e){
if(!e.key){
return;
}
var menu=dojo.widget.getWidgetById(this.menuId);
if(e.key==e.KEY_ENTER||e.key==" "){
this.onMouseDown(e);
this.buttonClick(e);
dojo.lang.setTimeout(this,"onMouseUp",75,e);
dojo.event.browser.stopEvent(e);
}else{
if(e.key==e.KEY_DOWN_ARROW&&e.altKey){
this.rightDown(e);
this.rightClick(e);
dojo.lang.setTimeout(this,"rightUp",75,e);
dojo.event.browser.stopEvent(e);
}else{
if(menu&&menu.isShowingNow&&e.key==e.KEY_DOWN_ARROW){
dojo.event.disconnect(this.domNode,"onblur",this,"onBlur");
}
}
}
}});
dojo.provide("ps.widget.PSButton");
dojo.widget.defineWidget("ps.widget.PSButton",dojo.widget.Button,{templateCssPath:dojo.uri.moduleUri("ps","widget/PSButton.css")});
dojo.provide("ps.aa.controller");
ps.aa.controller=new function(){
this.isShowTree=null;
this.pageId=null;
this.activeId=null;
this.contentBrowser=null;
this.snippetPickerDlg=null;
this.createItemDlg=null;
this.editObjectId=null;
this.psCeWindow=null;
this.treeModel=null;
this.treeWidget=null;
this.wfActions=null;
this.fieldEdit=null;
this.lastShowingTreePaneWidth=0;
this.MIN_TREE_WIDTH=20;
this.menuBarStyle=null;
this.isShowPlaceholders=true;
this.init=function(){
var _this=this;
ps.aa.Page.init();
this.treeModel=new ps.aa.Tree();
this.treeModel.init();
this.pageId=this.treeModel.getRootNode().objId;
dojo.event.connect(this.treeModel,"onBeforeDomChange",this,"_onBeforeDomChange");
dojo.event.connect(this.treeModel,"onDomChanged",this,"_onDomChanged");
var ids=new dojo.collections.ArrayList();
ids.add(this.pageId);
if(___sys_aamode!=1){
ids=this.treeModel.getIdsFromNodeId(this.pageId);
}
this._resetObjectIcons(ids);
this.isShowTree=this._loadTreePaneShowing();
ps.aa.Menu.init(ids);
this._maybeShowTree();
this.bottomPane=dojo.widget.byId("ps.aa.BottomPane");
dojo.event.connect(this.bottomPane,"endSizing",this,"_endTreeSizing");
this.treeWidget=dojo.widget.manager.getWidgetById("pageTree");
this.treeWidget.loadFromModel(this.treeModel);
this.activate(ps.aa.Page.getElement(this.pageId));
this.updateBodyStyles();
var _101f=dojo.render.html.ie55||dojo.render.html.ie60?500:600;
dojo.lang.setTimeout(function(){
_this.asynchInit(ids);
},_101f);
},this.updateBodyStyles=function(){
var _1020=document.getElementsByTagName("body")[0];
var _1021=dojo.byId("ps.aa.PageContent");
var _1022=ps.util.trim(_1020.style.cssText);
_1020.style.cssText="";
var _1023=ps.util.getElementStyleSheetCss("body",true);
var _1024="";
if(_1022.length>0&&_1023.length>0){
_1024=_1022+";"+_1023;
}else{
if(_1022.length>0){
_1024=_1022;
}else{
if(_1023.length>0){
_1024=_1023;
}
}
}
if(_1024.length>0){
_1021.style.cssText=_1024;
}
_1021.className=_1020.className;
var _1025=dojo.html.getAttribute(_1020,"id");
_1020.setAttribute("id",_1025);
_1020.className="PsAabody";
};
this.asynchInit=function(ids){
var _this=this;
ps.aa.Menu.initAsynch(ids);
this.treeWidget.loadFromModelAsynch(this.treeModel);
this.wfActions=new ps.workflow.WorkflowActions();
this.wfActions.init();
this.fieldEdit=new ps.aa.Field();
this.fieldEdit.init();
ps.aa.dnd.init();
this.treeWidget.dndInit();
this.contentBrowser=new ps.content.Browse(ps.util.BROWSE_MODE_ACTIVE_ASSEMBLY);
this.contentBrowser.init(__rxroot);
this.snippetPickerDlg=new ps.content.SnippetPicker();
this.snippetPickerDlg.init(__rxroot+"/ui/content/snippetpicker.jsp",1);
this.createItemDlg=new ps.content.CreateItem();
this.createItemDlg.init(__rxroot+"/ui/content/CreateItem.jsp");
this.templatesDlg=new ps.content.SelectTemplates();
this.templatesDlg.init(__rxroot+"/ui/content/selecttemplate.jsp");
this._handleMenuBarBackGround();
this.adjustLayout();
window.onresize=this.adjustLayout;
var nodes=[];
nodes.push(dojo.byId("ps.aa.ContentPane"));
nodes.push(dojo.widget.byId("pageTree").domNode);
this.autoscroller.init(nodes);
dojo.lang.delayThese([function(){
_this.wfActions.maybeCreateWorkflowDialog();
},function(){
_this.contentBrowser.maybeCreateBrowseDialog();
}],100);
};
this.adjustLayout=function(){
var _1029=dojo.widget.byId("ps.aa.mainSplitPane");
var _102a=dojo.html.getViewport();
_1029.resizeTo(_102a.width,_102a.height);
};
this.activate=function(_102b){
dojo.lang.assert(_102b);
var id=null;
var _102d=null;
if(dojo.lang.isOfType(_102b,ps.aa.ObjectId)){
id=_102b;
_102d=ps.aa.Page.getElement(id);
}else{
id=ps.aa.Page.getObjectId(_102b);
if(id.widget!=null||dojo.html.isTag(_102b,"a")){
_102d=ps.aa.Page.getElement(id);
}else{
_102d=_102b;
}
}
var _102e=null;
if(id.isSnippetNode()&&!this.treeModel.root.equals(id)){
_102e=ps.aa.Page.getParentId(_102d,id);
dojo.lang.assert(_102e);
}
this.activeId=id;
if(ps.aa.Page.activate(_102d)){
ps.aa.Menu.activate(id,_102e);
}
if(this.treeWidget){
this.treeWidget.activate(id);
}
};
this.updateTreeWidget=function(){
this.treeWidget.onModelChanged();
};
this._endTreeSizing=function(){
var wg=dojo.widget.byId("pageTree");
if(wg.sizeShare>this.MIN_TREE_WIDTH&&!this.isShowTree){
this.showTree();
}else{
if(wg.sizeShare<=this.MIN_TREE_WIDTH&&this.isShowTree){
this.hideTree();
}
}
},this.showTree=function(){
this.isShowTree=true;
this._maybeShowTree();
};
this.hideTree=function(){
this.isShowTree=false;
this._maybeShowTree();
};
this.showBorders=function(){
this._switchBorderMode(true);
};
this.hideBorders=function(){
this._switchBorderMode(false);
};
this._switchBorderMode=function(_1030){
var hash=PSHref2Hash(null);
hash["sys_aamode"]=_1030?"0":"1";
window.location.href=PSHash2Href(hash,null);
};
this.showPlaceholders=function(){
ps.aa.Menu.toggleShowHidePlaceholders(true);
ps.util.showHidePlaceholders(document,true);
this.isShowPlaceholders=true;
};
this.hidePlaceholders=function(){
ps.aa.Menu.toggleShowHidePlaceholders(false);
ps.util.showHidePlaceholders(document,false);
this.isShowPlaceholders=false;
};
this._maybeShowTree=function(){
ps.aa.Menu.toggleShowHideTree(this.isShowTree);
this.toggleTreePane(this.isShowTree);
};
this.toggleTreePane=function(_1032){
dojo.lang.assert(dojo.lang.isBoolean(_1032));
var wg=dojo.widget.byId("pageTree");
if(!_1032){
this.lastShowingTreePaneWidth=wg.sizeShare;
wg.sizeShare=0;
}else{
if(wg.sizeShare<1){
wg.sizeShare=this.lastShowingTreePaneWidth>this.MIN_TREE_WIDTH?this.lastShowingTreePaneWidth:290;
}
}
this._saveTreePaneShowing(_1032);
var _1034=dojo.widget.byId("ps.aa.mainSplitPane");
_1034._layoutPanels();
};
this._saveTreePaneShowing=function(_1035){
dojo.lang.assert(dojo.lang.isBoolean(_1035));
dojo.io.cookie.setCookie(this._TREE_PANE_SHOWING_COOKIE,_1035);
};
this._loadTreePaneShowing=function(){
var wg=dojo.widget.byId("pageTree");
var ck=dojo.io.cookie.getCookie(this._TREE_PANE_SHOWING_COOKIE);
return ck?eval(ck):wg.sizeShare>0;
};
this.refreshSlot=function(id){
return this._refreshX(id,undefined,false,ps.io.Actions,"getSlotContent");
};
this.refreshSnippet=function(id,newId){
return this._refreshX(id,newId,true,ps.io.Actions,"getSnippetContent");
};
this.refreshField=function(id){
return this._refreshX(id,undefined,true,ps.io.Actions,"getFieldContent");
};
this._refreshX=function(id,newId,_103e,_103f,_1040){
var _1041=null;
dojo.lang.assertType(id,ps.aa.ObjectId);
newId&&dojo.lang.assertType(newId,ps.aa.ObjectId);
dojo.lang.assertType(_103e,Boolean);
dojo.lang.assert(_103f);
dojo.lang.assertType(_1040,String);
if(!newId){
var newId=id;
}
var node=ps.aa.Page.getElement(id);
var _1043=dojo.lang.hitch(_103f,_1040)(newId,true);
ps.io.Actions.maybeReportActionError(_1043);
if(_1043.isSuccess()){
var _1044=this.treeModel;
var _1045=_1044.getNodeById(id);
_1044.fireBeforeDomChange(id);
if(_103e){
if(!id.equals(newId)){
this._replaceDomId(id,newId);
}
this._refreshNodeContent(node,newId,_1043.getValue(),newId.isSnippetNode());
}else{
this._refreshNode(node,newId,_1043.getValue());
}
if(id.equals(newId)){
_1041=id;
}else{
if(_1045.parentNode){
_1041=_1045.parentNode.objId;
}else{
_1041=null;
}
}
dojo.lang.assert(!dojo.lang.isUndefined(_1041));
_1044.fireDomChanged(_1041,newId);
}
ps.util.addPlaceholders(document);
if(this.isShowPlaceholders){
this.showPlaceholders();
}else{
this.hidePlaceholders();
}
if(___sys_aamode==1){
ps.DivActionHelper.reset();
}
return _1041;
};
this._refreshNode=function(node,objId,_1048){
dojo.lang.assert(node);
dojo.lang.assert(node.nodeType===dojo.dom.ELEMENT_NODE);
dojo.lang.assertType(objId,ps.aa.ObjectId);
dojo.lang.assertType(_1048,String);
var _1049=dojo.html.createNodesFromText(_1048,true);
var _104a=ps.util.findNodeById(_1049,objId.serialize());
dojo.lang.assert(_104a,"Expected html to contain a node with id "+objId.serialize()+".\nThe html: "+_1048);
var _104b=dojo.dom.replaceNode(node,_104a);
dojo.dom.destroyNode(_104b);
};
this._refreshNodeContent=function(pnode,id,_104e,_104f){
dojo.lang.assert(id,ps.aa.ObjectId);
if(___sys_aamode==0&&_104f){
var _1050=dojo.byId(id.getAnchorId());
dojo.lang.assert(_1050,"Could not find anchor with id "+id.getAnchorId());
var _1051=_1050.parentNode;
}else{
var _1050=null;
var _1051=pnode;
}
var _1052=new Array();
while(_1051.hasChildNodes()){
_1052.push(_1051.firstChild);
dojo.dom.removeNode(_1051.firstChild);
}
for(var i=0;i<_1052.length;i++){
if(_1052[i]!=_1050){
dojo.dom.destroyNode(_1052[i]);
}
}
if(_1050!=null){
_1051.appendChild(_1050);
}
var _1054=dojo.html.createNodesFromText(_104e,true);
for(var i=0;i<_1054.length;i++){
_1051.appendChild(_1054[i]);
}
};
this.addSnippet=function(_1055){
var _this=this;
switch(_1055){
case ps.aa.Menu.INSERT_FROM_SLOT:
var _1057=dojo.html.getElementsByClass("PsAaSnippet",document.getElementById(this.activeId.toString()),"div",dojo.html.classMatchType.IsOnly,false);
if(_1057.length<1){
this._openBrowseDlg(this.activeId,null,"before");
}else{
this.snippetPickerDlg.open(function(_1058,_1059,_105a){
_this._openBrowseDlg(_1058,_1059,_105a);
},function(){
},this.activeId,1);
}
break;
case ps.aa.Menu.INSERT_FROM_SNIPPET:
var _105b=this.treeModel.getNodeById(this.activeId);
var _105c=_105b.parentNode.objId;
this.snippetPickerDlg.open(function(_105d,relId,_105f){
_this._openBrowseDlg(_105d,relId,_105f);
},function(){
},_105c,1,"before",this.activeId.getRelationshipId());
break;
}
};
this._openBrowseDlg=function(_1060,_1061,_1062){
var _this=this;
this.contentBrowser.open(function(_1064){
_this.addSnippetToSlot(_1064);
},_1060,_1061,_1062);
};
this.openRemoveSnippetsDlg=function(){
var _1065=dojo.html.getElementsByClass("PsAaSnippet",document.getElementById(this.activeId.toString()),"div",dojo.html.classMatchType.IsOnly,false);
if(_1065.length<1){
alert("The slot is empty.");
return;
}
var _this=this;
this.snippetPickerDlg.open(function(_1067,_1068){
_this._handleRemoveSnippets(_1067,_1068);
},function(){
},this.activeId,0);
};
this.createItem=function(_1069){
var oeWin=null;
if(parent.newItemWindow&&!parent.newItemWindow.closed){
oeWin=parent.newItemWindow;
}else{
if(this.fieldEdit.psCeFieldWindow&&!this.fieldEdit.psCeFieldWindow.closed){
oeWin=this.fieldEdit.psCeFieldWindow;
}else{
if(this.psCeWindow&&!this.psCeWindow.closed){
oeWin=this.psCeWindow;
}
}
}
if(oeWin!=null){
var oeMsg="You have an open editor, "+"close that window before creating another item.";
alert(oeMsg);
oeWin.focus();
return false;
}
var _this=this;
switch(_1069){
case ps.aa.Menu.NEW_FROM_SLOT:
var _106d=dojo.html.getElementsByClass("PsAaSnippet",document.getElementById(this.activeId.toString()),"div",dojo.html.classMatchType.IsOnly,false);
if(_106d.length<1){
this._openNewItemDlg(this.activeId,null,"before");
}else{
this.snippetPickerDlg.open(function(_106e,relId,_1070){
_this._openNewItemDlg(_106e,_this._getSnippetIdFromRelId(_106e,relId),_1070);
},function(){
},this.activeId,1);
}
break;
case ps.aa.Menu.NEW_FROM_SNIPPET:
var _1071=this.activeId;
var node=this.treeModel.getNodeById(this.activeId);
var _1073=node.parentNode.objId;
this.snippetPickerDlg.open(function(_1074,relId,_1076){
_this._openNewItemDlg(_1074,_this._getSnippetIdFromRelId(_1074,relId),_1076);
},function(){
},_1073,1,"before",this.activeId.getRelationshipId());
break;
case ps.aa.Menu.REPLACE_FROM_SNIPPET:
var _1071=this.activeId;
var node=this.treeModel.getNodeById(this.activeId);
var _1073=node.parentNode.objId;
this.snippetPickerDlg.open(function(_1077,relId,_1079){
_this._openBrowseDlg(_1077,relId,_1079);
},function(){
},_1073,1,"replace",this.activeId.getRelationshipId());
break;
case ps.aa.Menu.COPY_FROM_CONTENT:
var _1071=this.treeModel.getRootNode().getObjectId();
var _107a=ps.io.Actions.getItemPath(_1071);
if(!_107a.isSuccess()){
ps.io.Actions.maybeReportActionError(_107a);
return;
}
var ipath=_107a.getValue();
var fpath=ipath.substring(0,ipath.lastIndexOf("/"));
var _107d={"sys_contenttypeid":_1071.getContentTypeId(),"sys_templateid":_1071.getTemplateId(),"folderPath":fpath,"itemPath":ipath,"itemTitle":""};
_this._handleCopyItem(_1071,_107d);
break;
case ps.aa.Menu.NEW_FROM_CONTENT:
var _1071=this.treeModel.getRootNode().getObjectId();
_this._openNewItemDlg(null,_1071,null);
break;
default:
alert("invalid source");
}
};
this._handleCopyItem=function(_107e,_107f){
var _this=this;
var _1081=function(_1082){
_107f.itemTitle=_1082;
var _1083=ps.io.Actions.getIdByPath(_107f.folderPath+"/"+_1082);
if(_1083.isSuccess()){
alert("Title should be unique under the specified folder.\n"+_107f.folderPath);
_this.dlg.hide();
_this._handleCopyItem(_107e,_107f);
}else{
_this.handleCreateItem(null,_107e,null,_107f);
}
};
var _1084=function(){
_this.dlg.hide();
};
var _1085={"dlgTitle":"Create Copy","promptTitle":"Title","promptText":_107f.itemTitle,"textRequired":true,"okBtnText":"Create","cancelBtnText":"Cancel","okBtnCallBack":_1081,"cancelBtnCallBack":_1084};
this.dlg=ps.util.CreatePromptDialog(_1085);
this.dlg.show();
this.dlg.focusTitle();
};
this._getSnippetIdFromRelId=function(_1086,relId){
if(!relId){
return null;
}
var _1088=null;
if(relId!=null){
var _1089=dojo.html.getElementsByClass("PsAaSnippet",document.getElementById(_1086.toString()),"div",dojo.html.classMatchType.IsOnly,false);
for(var i=0;i<_1089.length;i++){
var id=ps.aa.Page.getObjectId(_1089[i]);
if(id.getRelationshipId()==relId){
_1088=id;
break;
}
}
}
return _1088;
};
this._openNewItemDlg=function(_108c,_108d,_108e){
var _this=this;
this.createItemDlg.open(function(_1090,_1091,_1092,_1093){
_this.handleCreateItem(_1090,_1091,_1092,_1093);
},function(){
},_108c,_108d,_108e);
};
this.handleCreateItem=function(_1094,_1095,_1096,_1097){
var _1098=ps.io.Actions.createItem(_1097.sys_contenttypeid,_1097.folderPath,_1097.itemPath,_1097.itemTitle);
if(!_1098.isSuccess()){
ps.io.Actions.maybeReportActionError(_1098);
return;
}
var obj=_1098.getValue();
if(dojo.lang.has(obj,"validationError")){
if(!confirm("The following errors occured while creating the new item\n"+obj.validationError+"\nClick OK to open the full editor.")){
return;
}
var _109a=ps.io.Actions.getCreateItemUrl(_1097.folderPath,_1097.sys_contenttypeid,false);
ps.io.Actions.maybeReportActionError(_109a);
if(_109a.isSuccess()){
var url=_109a.getValue().url;
var _109c=ps.io.Actions.getIdByPath(_1097.folderPath);
if(!_109c.isSuccess){
alert("Failed to get the folderid for the supplied folder path."+"\nSkipping adding item to folder action.");
}else{
url+="&sys_folderid="+_109c.getValue().id;
}
this.newItemData=null;
var temp={"slotId":_1094,"itemId":_1095,"position":_1096,"newData":_1097};
this.newItemData=temp;
parent.newItemWindow=window.open(url,"PsAaCreateItem",this.PREVIEW_WINDOW_STYLE);
parent.newItemWindow.focus();
}
return;
}
var cid=obj.itemId;
var fid=obj.folderId;
if(fid==-1){
alert("Created the new item but failed to add it to the "+"folder. \n See console.log for more details.");
}
this.postCreateItem(_1094,_1095,_1096,_1097,cid);
};
this.postCreateItem=function(_10a0,_10a1,_10a2,_10a3,cid){
if(_10a0==null){
var _10a5=_10a1.clone();
_10a5.setContentId(cid);
_10a5.setTemplateId(_10a3.sys_templateid);
var _10a6=ps.io.Actions.getUrl(_10a5,"PREVIEW_MYPAGE");
ps.io.Actions.maybeReportActionError(_10a6);
if(!_10a6.isSuccess()){
return;
}
var value=_10a6.getValue();
dojo.lang.assert(dojo.lang.has(value,"url"));
var _10a8=value.url+"&sys_command=editrc";
window.location.href=_10a8;
}else{
var _10a9=_10a0.clone();
_10a9.setContentId(cid);
_10a9.setSnippetNode();
_10a9.setTemplateId(_10a3.sys_templateid);
var _10a6=ps.io.Actions.addSnippet(_10a9,_10a0,null,null);
ps.io.Actions.maybeReportActionError(_10a6);
if(!_10a6.isSuccess()){
return;
}
var _10aa=_10a6.getValue();
var _10ab=_10a1==null?null:_10a1.getRelationshipId();
this.repositionSnippet(_10a0,_10ab,_10aa,_10a2);
}
};
this.repositionSnippet=function(_10ac,_10ad,_10ae,_10af){
this.refreshSlot(_10ac);
var node=this.treeModel.getNodeById(_10ac);
this.updateTreeWidget(node);
if(_10ad==null){
return;
}
if(_10af==null){
_10af="before";
}
if(!(_10af=="before"||_10af=="after"||_10af=="replace")){
dojo.lang.assert(false,"position must be either before or after or relace");
}
dojo.lang.assert(_10ac,"Slot id must be provided for reposition of snippet");
dojo.lang.assert(_10ac.isSlotNode(),"slotId must represent a slot object id");
dojo.lang.assert(_10ae);
var _10b1=this._getSnippetIdFromRelId(_10ac,_10ae);
dojo.lang.assert(_10ae,"Invalid relationship id of new snippet");
var _10b2=this._getSnippetIdFromRelId(_10ac,_10ad);
dojo.lang.assert(_10ad,"Invalid relationship id of reference snippet");
var _10b3=_10b2.getSortRank();
var _10b4=_10b1.getSortRank();
var _10b5=-1;
if(_10af=="before"&&_10b4!=_10b3-1){
_10b5=parseInt(_10b3,10)+0;
}else{
if(_10af=="after"&&_10b4!=_10b3+1){
_10b5=parseInt(_10b3,10)+1;
}else{
if(_10af=="replace"&&(_10b4!=_10b3-1||_10b4!=_10b3+1)){
_10b5=parseInt(_10b3,10)+0;
}
}
}
if(_10b5!=-1){
var _10b6=ps.io.Actions.move(_10b1,"reorder",_10b5+"");
ps.io.Actions.maybeReportActionError(_10b6);
}
if(_10af=="replace"){
var _10b6=ps.io.Actions.removeSnippet(_10b2.getRelationshipId());
ps.io.Actions.maybeReportActionError(_10b6);
}
this.refreshSlot(_10ac);
var node=this.treeModel.getNodeById(_10ac);
this.updateTreeWidget(node);
this.activate(_10ac);
};
this.openSnippet=function(){
if(this.snippetOpenWindow&&!this.snippetOpenWindow.closed){
if(!confirm(this.SNIPPETOPEN_MSG)){
this.snippetOpenWindow.focus();
return false;
}else{
this.snippetOpenWindow.close();
}
}
this.snippetOpenWindow=this.openWindow("TOOL_LINK_TO_PAGE",this.PREVIEW_WINDOW_STYLE,this.activeId,"PSAaSnippetWindow");
};
this.refreshBrowseWindow=function(){
if(!this.contentBrowser.wgtDlg.isShowing()){
dojo.debug("No need to refresh, the content browser is closed.");
return;
}
this.contentBrowser.currentTab.refreshBrowser();
};
this.previewWithCurrentRevisions=function(){
var _10b7=this._parseUrlParams();
this.openWindow("PREVIEW_PAGE",this.PREVIEW_WINDOW_STYLE,this._previewObjIdFromParamMap(_10b7),this.PREVIEW_WINDOW_NAME);
};
this.previewWithEditRevisions=function(){
var _10b8=this._parseUrlParams();
var _10b9=new Array;
var param=new Object;
param.name="useEditRevisions";
param.value="yes";
_10b9[0]=param;
this.openWindow("PREVIEW_MYPAGE",this.PREVIEW_WINDOW_STYLE,this._previewObjIdFromParamMap(_10b8),this.PREVIEW_WINDOW_NAME,_10b9);
};
this._previewObjIdFromParamMap=function(_10bb){
var objId=new Array();
objId[ps.aa.ObjectId.NODE_TYPE]="";
objId[ps.aa.ObjectId.CONTENT_ID]=_10bb.sys_contentid;
objId[ps.aa.ObjectId.TEMPLATE_ID]=_10bb.sys_variantid;
objId[ps.aa.ObjectId.SITE_ID]=_10bb.sys_siteid;
objId[ps.aa.ObjectId.FOLDER_ID]=_10bb.sys_folderid;
objId[ps.aa.ObjectId.CONTEXT]=_10bb.sys_context;
objId[ps.aa.ObjectId.AUTHTYPE]=_10bb.sys_authtype;
objId[ps.aa.ObjectId.CONTENTTYPE_ID]="";
objId[ps.aa.ObjectId.CHECKOUT_STATUS]="";
objId[ps.aa.ObjectId.SLOT_ID]="";
objId[ps.aa.ObjectId.RELATIONSHIP_ID]="";
objId[ps.aa.ObjectId.FIELD_NAME]="";
objId[ps.aa.ObjectId.PARENT_ID]="";
objId[ps.aa.ObjectId.FIELD_LABEL]="";
objId[ps.aa.ObjectId.SORT_RANK]="";
return new ps.aa.ObjectId(dojo.json.serialize(objId));
};
this._parseUrlParams=function(url){
if(url==null){
url=window.location.toString();
}
loc=url.indexOf("#");
if(loc!=-1){
url=url.substring(0,loc);
}
url.match(/\?(.+)$/);
var _10be=RegExp.$1;
var _10be=_10be.split("&");
var _10bf={};
for(var i=0;i<_10be.length;i++){
var tmp=_10be[i].split("=");
_10bf[tmp[0]]=unescape(tmp[1]);
}
return _10bf;
};
this.openManageNavigationDlg=function(){
this.activeId;
var _10c2=this._parseUrlParams();
this.openWindow("MANAGE_NAVIGATION",this._getSizedStyle(null,800,750),this.activeId,"MANAGE_NAVIGATION");
};
this.showItemRelationships=function(){
this.openWindow("TOOL_SHOW_AA_RELATIONSHIPS",this._getSizedStyle(null,null,500));
};
this.createTranslation=function(){
this.openWindow("ACTION_Translate",this._getSizedStyle(null,300,225));
};
this.createVersion=function(){
this.openWindow("ACTION_Edit_PromotableVersion",this._getSizedStyle(null,800,700));
};
this.showPageUrl=function(){
var tmpId=this.activeId;
var url=this._getUrl("TOOL_LINK_TO_PAGE",tmpId);
if(url==null){
dojo.debug("Failed to get link for item id = "+this.activeId.getContentId());
return;
}
var loc=window.location;
var _10c6=loc.protocol+"//"+loc.host+url;
ps.util.ShowPageLinkDialog(_10c6);
};
this.getLinkToCurrentPage=function(){
var tmpId=this.treeModel.getRootNode().getObjectId();
var url=this._getUrl("TOOL_LINK_TO_PAGE",tmpId);
return url;
};
this.logout=function(){
var aaUrl=ps.aa.controller.getLinkToCurrentPage();
var loc=window.location;
var _10cb=loc.protocol+"//"+loc.host+"/Rhythmyx/logout";
var url=ps.util.addParamToUrl(_10cb,"sys_redirecturl",escape(aaUrl));
window.location=url;
};
this.publishPage=function(){
this.openWindow("TOOL_PUBLISH_NOW",this._getSizedStyle(null,700,450));
this.wfActions.handleObjectIdModifications("0");
var ids=new Array();
ids[0]=this.activeId;
this._resetObjectIcons(ids);
this.refreshImageForObject(this.activeId);
};
this.compareItemRevisions=function(){
this.openWindow("ACTION_View_Compare",this._getSizedStyle(null,900,700));
};
this.viewContent=function(){
this.openWindow("CE_VIEW_CONTENT",this._getSizedStyle(null,-1,700));
};
this.viewProperties=function(){
this.openWindow("CE_VIEW_PROPERTIES");
};
this.viewRevisions=function(){
this.openWindow("CE_VIEW_REVISIONS");
};
this.viewAuditTrail=function(){
this.openWindow("CE_VIEW_AUDIT_TRAIL");
};
this.openWindow=function(_10ce,_10cf,_10d0,wName,_10d2){
var url=this._getUrl(_10ce,_10d0);
if(url==null){
return;
}
if(_10d2!=null&&_10d2!=undefined){
dojo.lang.forEach(_10d2,function(param){
url=url+"&"+param.name+"="+param.value;
});
}
var ws=_10cf;
if(ws==null){
ws=this._getSizedStyle();
}
var wn=wName;
if(wn==null){
wn=this.CE_WINDOW_NAME;
}
var vwin=window.open(url,wn,ws);
var _10d8=ps.util.getScreenSize(vwin,false);
var _10d9=this._getSizeFromStyle(ws);
if(_10d8.width!=_10d9.width||_10d8.height!=_10d9.height){
vwin.resizeTo(_10d9.width,_10d9.height);
}
vwin.focus();
return vwin;
};
this.changeTemplate=function(){
this.templatesDlg.controller=this;
var _this=this;
this.templatesDlg.open(function(_10db,_10dc){
_this._handleTemplateChange(_10db,_10dc);
},function(){
},this.activeId);
};
this._getSizeFromStyle=function(style){
var size=new Object();
size.width=800;
size.height=400;
if(style==null){
return size;
}
var _10df=style.split(",");
var _10e0=false;
var _10e1=false;
for(var i=0;i<_10df.length&&!(_10e0&&_10e1);i++){
var prop=_10df[i].split("=");
if(prop[0]=="width"){
size.width=prop[1];
_10e0=true;
}else{
if(prop[0]=="height"){
size.height=prop[1];
_10e1=true;
}
}
}
return size;
};
this._getSizedStyle=function(style,width,_10e6){
if(style==null){
style=this.BASIC_WINDOW_STYLE;
}
if(width==null||width<0){
width=800;
}
if(_10e6==null||_10e6<0){
_10e6=400;
}
if(style.length>0){
style=style+",";
}
return style+"width="+width+",height="+_10e6;
};
this._handleTemplateChange=function(_10e7,_10e8){
var _10e9=ps.io.Actions.getItemSortRank(_10e7.getRelationshipId());
if(_10e9.isSuccess()){
var rank=parseInt(_10e9.getValue());
if(rank==0){
rank=1;
}
var resp=ps.io.Actions.moveToSlot(_10e7,_10e7.getSlotId(),_10e7.getTemplateId(),rank);
if(!resp.isSuccess()){
ps.io.Actions.maybeReportActionError(resp);
}else{
var _10ec=this.refreshSnippet(_10e8,_10e7);
this.updateTreeWidget(_10ec);
this.activate(_10e7);
}
}
};
this._handleRemoveSnippets=function(_10ed,_10ee){
var _10ef=ps.io.Actions.removeSnippet(_10ee);
ps.io.Actions.maybeReportActionError(_10ef);
if(_10ef.isSuccess()){
this.refreshSlot(_10ed);
var node=this.treeModel.getNodeById(_10ed);
this.updateTreeWidget(node);
this.activate(_10ed);
}
};
this.removeSnippet=function(){
var node=this.treeModel.getNodeById(this.activeId);
var _10f2=this.treeModel.getNextSiblingId(node.objId);
var _10f3=ps.io.Actions.removeSnippet(node.objId.getRelationshipId());
ps.io.Actions.maybeReportActionError(_10f3);
if(_10f3.isSuccess()){
var pid=node.parentNode.objId;
this.refreshSlot(pid);
this.updateTreeWidget(node.parentNode);
var div=ps.aa.Page.getElement(_10f2);
this.activate(div);
}
};
this.editAll=function(_10f6){
if(this.activeId.isCheckoutByMe()==0){
alert(this.CHECKOUT_MSG);
return false;
}
if(this.fieldEdit.psCeFieldWindow&&!this.fieldEdit.psCeFieldWindow.closed){
if(!confirm(ps.aa.controller.EDITOROPEN_MSG)){
this.fieldEdit.psCeFieldWindow.focus();
return false;
}else{
this.fieldEdit.psCeFieldWindow.close();
}
}else{
if(this.psCeWindow&&!this.psCeWindow.closed){
if(this.activeId.getContentId()==this.editObjectId.getContentId()||!confirm(ps.aa.controller.EDITOROPEN_MSG)){
ps.aa.controller.psCeWindow.focus();
return false;
}else{
ps.aa.controller.psCeWindow.close();
}
}else{
if(this.fieldEdit.inplaceEditing){
if(!confirm(this.INPLACE_EDITOROPEN_MSG)){
return false;
}
this.fieldEdit.onInplaceCancel();
}
}
}
this.editObjectId=this.activeId;
var ceurl="";
if(_10f6&&_10f6.length>0){
ceurl=_10f6;
}else{
var _10f8=ps.io.Actions.getUrl(this.activeId,"CE_EDIT");
ps.io.Actions.maybeReportActionError(_10f8);
if(!_10f8.isSuccess()){
return false;
}
var value=_10f8.getValue();
dojo.lang.assert(dojo.lang.has(value,"url"));
ceurl=value.url;
}
this.psCeWindow=window.open(ceurl,this.CE_EDIT_ITEM_WINDOW,this.PREVIEW_WINDOW_STYLE);
this.psCeWindow.focus();
};
this.updateAllFields=function(_10fa){
this.refreshFieldsOnPage(this.editObjectId.getContentId(),null,this.psCeWindow);
this._resetTreeLabel(_10fa);
};
this._getUrl=function(_10fb,_10fc){
var oid=_10fc;
if(oid==null){
oid=this.activeId;
}
var _10fe=ps.io.Actions.getUrl(oid,_10fb);
ps.io.Actions.maybeReportActionError(_10fe);
if(!_10fe.isSuccess()){
return null;
}
var value=_10fe.getValue();
dojo.lang.assert(dojo.lang.has(value,"url"));
return value.url;
};
this._resetTreeLabel=function(_1100){
if(_1100==null||_1100==""){
return;
}
if(this.editObjectId==null){
return;
}
var _1101=this.treeModel.getNodeById(this.editObjectId);
if(this.editObjectId.isFieldNode()){
_1101=_1101.parentNode;
}
_1101.clearLabel();
var id=_1101.objId.serialize();
var _1103=dojo.byId(id);
dojo.lang.assert(_1103,"Cannot find DIV element with id="+id);
var _1104=dojo.html.getAttribute(_1103,"psAaLabel");
if(_1100!=_1104){
_1103.setAttribute("psAaLabel",_1100);
this.updateTreeWidget();
}
};
this._getAffectedObjectIds=function(_1105,_1106){
var _1107=this.treeModel.getIdsFromContentId(_1105,_1106);
var _1108=this.treeModel.getAllIdsByContentId(null);
var _1109=new dojo.collections.ArrayList();
var cid;
for(var i=0;i<_1108.count;i++){
cid=_1108.item(i).getContentId();
if(!_1109.contains(cid)){
_1109.add(cid);
}
}
var _110c=ps.io.Actions.getInlinelinkParentIds(_1105,_1109.toArray());
ps.io.Actions.maybeReportActionError(_110c);
if(_110c.isSuccess()){
var _110d=_110c.getValue();
for(var i=0;i<_110d.length;i++){
if(_110d[i]!=_1105){
var _110e=this.treeModel.getIdsFromContentId(_110d[i],null);
_1107.addRange(_110e);
}
}
}
return _1107;
};
this.refreshFieldsOnPage=function(_110f,_1110,_1111){
var _1112=this._getAffectedObjectIds(_110f,_1110);
for(var i=0;i<_1112.count;i++){
var _1114=_1112.item(i);
dojo.lang.assertType(_1114,ps.aa.ObjectId);
if(_1114.isFieldNode()){
this.refreshField(_1114,null);
}else{
if(_1114.isSnippetNode()){
this.refreshSnippet(_1114,null);
}
}
}
if(_1111&&!_1111.closed){
_1111.focus();
}
this.refreshOpener(_110f);
};
this.editField=function(){
if(___sys_aamode==1){
ps.DivActionHelper.reset();
}
this.fieldEdit.editField(ps.aa.Page.activeDiv,null);
};
this.cutSnippet=function(){
alert("FIXME: cutSnippet()");
};
this.pasteSnippet=function(){
alert("FIXME: pasteSnippet()");
};
this.moveSnippetDown=function(){
var _1115=this._getSnippetNode(ps.aa.Page.activeDiv);
if(_1115){
var _1116=ps.aa.Page.getObjectId(_1115);
dojo.lang.assert(_1116);
var _1117=this.treeModel.getNodeById(_1116);
var _1118=_1117.getIndex();
var _1119=_1117.parentNode.childNodes.toArray();
if(_1118<_1119.length-1){
var _111a=ps.io.Actions.move(_1116,"down");
ps.io.Actions.maybeReportActionError(_111a);
if(_111a.isSuccess()){
var _111b=_1118+1;
var node2=_1119[_111b];
_1117.parentNode.childNodes.setByIndex(_1118,node2);
_1117.parentNode.childNodes.setByIndex(_111b,_1117);
var _111d=ps.aa.Page.getElement(node2.objId);
ps.util.swapNodes(_1115,_111d);
this.updateTreeWidget(_1117.parentNode);
this.activate(_1115);
}
}
}
};
this.moveSnippetUp=function(){
var _111e=this._getSnippetNode(ps.aa.Page.activeDiv);
if(_111e){
var _111f=ps.aa.Page.getObjectId(_111e);
dojo.lang.assert(_111f);
var _1120=this.treeModel.getNodeById(_111f);
var _1121=_1120.getIndex();
var _1122=_1120.parentNode.childNodes.toArray();
if(_1121>0){
var _1123=ps.io.Actions.move(_111f,"up");
ps.io.Actions.maybeReportActionError(_1123);
if(_1123.isSuccess()){
var _1124=_1121-1;
var node2=_1122[_1124];
_1120.parentNode.childNodes.setByIndex(_1121,node2);
_1120.parentNode.childNodes.setByIndex(_1124,_1120);
var _1126=ps.aa.Page.getElement(node2.objId);
ps.util.swapNodes(_111e,_1126);
this.updateTreeWidget(_1120.parentNode);
this.activate(_111e);
}
}
}
};
this.workflowItem=function(){
this.wfActions.open();
};
this.moveToSlot=function(move){
dojo.lang.assertType(move,ps.aa.SnippetMove);
var _1128=ps.io.Actions.moveToSlot(move.getSnippetId(),move.getTargetSlotId().getSlotId(),move.getTargetSnippetId().getTemplateId(),move.getTargetIndex());
var _1129=this._handleMoveToSlotResponse(_1128,move);
move.setSuccess(_1129);
return _1129;
};
this._handleMoveToSlotResponse=function(_112a,move){
dojo.lang.assertType(_112a,ps.io.Response);
dojo.lang.assertType(move,ps.aa.SnippetMove);
if(_112a.isSuccess()){
this.maybeRefreshMovedSnippetNode(move,true);
return true;
}else{
if(_112a.getValue()===ps.io.Actions.NEEDS_TEMPLATE_ID){
if(move.getSnippetId().getTemplateId()!==move.getTargetSnippetId().getTemplateId()){
ps.error("Template was already changed!");
return false;
}
var _112a=ps.io.Actions.getAllowedSnippetTemplates(move.getTargetSnippetId());
ps.io.Actions.maybeReportActionError(_112a);
if(!_112a.isSuccess()){
return false;
}
var value=_112a.getValue();
dojo.lang.assert(dojo.lang.has(value,"count"));
dojo.lang.assert(dojo.lang.has(value,"templateHtml"));
if(value.count==0){
ps.error("There are no templates, configured for the target slot!");
return false;
}else{
if(value.count==1){
var nodes=dojo.html.createNodesFromText(value.templateHtml,true);
dojo.lang.assert((nodes.length-1)===1,"Got more than 1 node from "+value.templateHtml);
var _112e=nodes[0];
move.setTargetSnippetId(ps.aa.Page.getObjectId(_112e));
dojo.dom.destroyNode(_112e);
delete _112e;
var _112f=this.moveToSlot(move);
this.maybeRefreshMovedSnippetNode(move,_112f);
return _112f;
}else{
this.templatesDlg.snippetMove=move;
this.templatesDlg.controller=this;
this.templatesDlg.open(this._onMoveToSlotTemplateSelected,this._onSnippetTemplateSelectionDialogCancelled,move.getTargetSnippetId());
return true;
}
}
}else{
ps.io.Actions.maybeReportActionError(_112a);
return false;
}
}
dojo.lang.assert(false,"Should not reach here");
};
this.maybeRefreshMovedSnippetNode=function(move,moved){
dojo.lang.assertType(move,ps.aa.SnippetMove);
if(moved){
if(move.getDontUpdatePage()){
move.setUiUpdateNeeded(true);
}else{
this.refreshSlot(move.getSlotId());
var _1132=[move.getSlotId()];
if(move.getSlotId().getSlotId()!==move.getTargetSlotId().getSlotId()){
this.refreshSlot(move.getTargetSlotId());
_1132.push(move.getTargetSlotId());
}
this.updateTreeWidget(_1132);
}
}
};
this._onMoveToSlotTemplateSelected=function(_1133,_1134){
dojo.lang.assert(_1133,ps.aa.ObjectId);
dojo.lang.assert(_1134,ps.aa.ObjectId);
var move=this.snippetMove;
dojo.lang.assert(move);
move.setTargetSnippetId(_1133);
var _1136=this.controller.moveToSlot(move);
if(!_1136){
this.cancelCallback();
}else{
this.controller.maybeRefreshMovedSnippetNode(move,true);
var _1134=move.getTargetSnippetId();
this.controller.activate(ps.aa.Page.getElement(_1134));
}
};
this._onSnippetTemplateSelectionDialogCancelled=function(){
var move=this.snippetMove;
dojo.lang.assert(move);
this.controller.refreshSlot(move.getSlotId());
this.controller.refreshSlot(move.getTargetSlotId());
this.controller.updateTreeWidget(move.getSlotId(),move.getTargetSlotId());
};
this._cancelSnippetTemplateSelectionDialog=function(){
var _1138=dojo.widget.byId(this._SELECT_TEMPLATE_DLG_ID);
if(!_1138){
return;
}
this._onSnippetTemplateSelectionDialogCancelled();
_1138.closeWindow();
};
this.reorderSnippetInSlot=function(_1139,_113a){
dojo.lang.assertType(_1139,ps.aa.ObjectId);
dojo.lang.assert(dojo.lang.isNumeric(_113a),"Can't be interpreted as number: \""+_113a+"\"");
var _113b=ps.io.Actions.move(_1139,"reorder",_113a);
ps.io.Actions.maybeReportActionError(_113b);
return _113b.isSuccess();
};
this.replaceId=function(oldId,newId){
dojo.lang.assert(dojo.lang.isOfType(oldId,ps.aa.ObjectId));
dojo.lang.assert(dojo.lang.isOfType(newId,ps.aa.ObjectId));
var _113e=new Array();
_113e[0]=oldId;
var _113f=new Array();
_113f[0]=newId;
this.replaceIds(_113e,_113f);
};
this.replaceIds=function(_1140,_1141){
dojo.lang.assert(dojo.lang.isArray(_1140));
dojo.lang.assert(dojo.lang.isArray(_1141));
dojo.lang.assert(_1140.length===_1141.length);
var _this=this;
function getRootId(){
return _this.treeModel.getRootNode().objId;
}
this.treeModel.fireBeforeDomChange(getRootId());
var _1143=this.activeId;
var _1144=false;
for(var i=0;i<_1140.length;i++){
var oldId=_1140[i];
var newId=_1141[i];
if(oldId.equals(_1143)){
_1143=newId;
_1144=true;
}
this._replaceDomId(oldId,newId);
this.refreshImageForObject(newId);
}
this.treeModel.fireDomChanged(null,null);
this.updateTreeWidget(getRootId());
if(_1144){
var _1148=getRootId();
if(_1148.equals(_1143)&&this.treeModel.getRootNode().childNodes.count>0){
_1148=this.treeModel.getRootNode().childNodes.item(0).objId;
}
this.activate(_1148);
this.activate(_1143);
}
};
this._replaceDomId=function(oldId,newId){
function changeElementId(id1,id2){
var elem=dojo.byId(id1);
dojo.lang.assert(elem!=null);
elem.id=id2;
}
changeElementId(oldId.serialize(),newId.serialize());
if(___sys_aamode==0){
changeElementId(oldId.getAnchorId(),newId.getAnchorId());
}
};
this.addSnippetToSlot=function(_114e){
this.refreshSlot(_114e);
this.updateTreeWidget(_114e);
};
this._resetObjectIcons=function(ids){
for(var i=0;i<ids.count;i++){
var id=ids.item(i);
this.refreshImageForObject(id);
}
};
this.refreshImageForObject=function(objId){
dojo.lang.assertType(objId,ps.aa.ObjectId);
if(___sys_aamode==1){
return;
}
var _1153=dojo.byId(objId.getAnchorId());
var _1154=dojo.dom.getFirstChildElement(_1153);
if(_1154==null){
dojo.debug("Image element for the objectid = "+objId.serialize()+"is not found");
return;
}else{
_1154.src=objId.getImagePath(this.IMAGE_ROOT_PATH);
}
};
this._getSnippetNode=function(node){
if(!node){
return null;
}
while(node){
if(node.className===ps.aa.SNIPPET_CLASS){
return node;
}
node=node.parentNode;
}
return null;
};
this._onBeforeDomChange=function(id){
var ids=this.treeModel.getIdsFromNodeId(id);
if(___sys_aamode!=1){
ps.aa.Menu.unBindContextMenu(ids);
}
};
this._onDomChanged=function(id){
var ids=this.treeModel.getIdsFromNodeId(id);
for(var i=0;i<ids.count;i++){
this.refreshImageForObject(ids.item(i));
}
if(___sys_aamode!=1){
ps.aa.Menu.bindContextMenu(ids);
}
};
this.refreshOpener=function(_115b){
try{
var _115c=window.opener;
if(_115c==null||_115c.closed){
return;
}
var wurl=_115c.location.href;
if(wurl.indexOf("sys_cx")!=-1){
refreshCxApplet(_115c,"Selected",_115b,null);
}else{
var h=PSHref2Hash(wurl);
if(h["sys_contentid"]==_115b){
_115c.location.href=_115c.location.href;
}
}
}
catch(e){
}
};
this._handleMenuBarBackGround=function(){
var _115f=dojo.widget.byId("ps.aa.Menubar");
this.menuBarStyle=_115f.domNode.style;
var _this=this;
dojo.widget.ModalFloatingPane.prototype.origShow=dojo.widget.ModalFloatingPane.prototype.show;
dojo.widget.ModalFloatingPane.prototype.show=function(){
_this._toggleMenuBarBackGround(false);
this.origShow();
};
dojo.widget.ModalFloatingPane.prototype.origHide=dojo.widget.ModalFloatingPane.prototype.hide;
dojo.widget.ModalFloatingPane.prototype.hide=function(){
_this._toggleMenuBarBackGround(true);
this.origHide();
};
};
this._toggleMenuBarBackGround=function(_1161){
if(this.menuBarStyle){
if(_1161){
this.menuBarStyle.background="#85aeec url(../sys_resources/images/aa/soriaBarBg.gif) repeat-x top left";
}else{
this.menuBarStyle.background="#85aeec url(../sys_resources/images/aa/soriaBarBgDisabled.gif) repeat-x top left";
}
}
};
this.openHelpWindow=function(_1162){
dojo.lang.assert((_1162===ps.aa.Menu.AAHELP||_1162===ps.aa.Menu.AATUTORIAL||_1162===ps.aa.Menu.AAABOUT),"Unexpected windowName "+_1162);
if(_1162==ps.aa.Menu.AAHELP){
var hwin=window.open(this.helpUrl,_1162);
hwin.focus();
}else{
if(_1162==ps.aa.Menu.AATUTORIAL){
var hwin=window.open(this.helpTutorialUrl,_1162);
hwin.focus();
}else{
if(_1162==ps.aa.Menu.AAABOUT){
var dlg=ps.createDialog({id:"ps.Help.AboutDlg",title:"About Rhythmyx Active Assembly",href:this.helpAboutUrl},"510px","400px");
dlg.show();
}else{
dojo.lang.assert(false,"Unhandled window "+_1162);
}
}
}
};
this.enableConflictStyleSheets=function(_1165){
if(this._aaConflictStyleSheets==null){
var ss=ps.util.getServerProperty("AaConflictStyleSheets","");
this._aaConflictStyleSheets=ss==""?[]:dojo.string.splitEscaped(ss,",");
}
for(var i=0;i<this._aaConflictStyleSheets.length;i++){
ps.util.enableStyleSheet(this._aaConflictStyleSheets[i],_1165);
}
};
this.autoscroller=new ps.widget.Autoscroller();
this.CHECKOUT_MSG="The item is not checked out to you, please check out the item to edit the field.";
this.EDITOROPEN_MSG="A window is already open for editing another field. Do you want to abort the changes to the open field and activate the field you have selected?";
this.SNIPPETOPEN_MSG="A window is already open for editing another "+"snippet. Do you want to replace it with current snippet?";
this.snippetOpenWindow=null;
this.INPLACE_EDITOROPEN_MSG="A field is already open for editing. Do you want to abort the changes to the open field and activate the field you have selected?";
this._SELECT_TEMPLATE_DLG_ID="selectTemplateDialog";
this.CE_WINDOW_NAME="Ce_Window";
this.BASIC_WINDOW_STYLE="toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1";
this.PREVIEW_WINDOW_NAME="Preview_Window";
this.PREVIEW_WINDOW_STYLE="toolbar=0,location=0,directories=0,status=1,menubar=0,scrollbars=1,resizable=1,width=800,height=700";
this.IMAGE_ROOT_PATH="../sys_resources/images/aa";
this.CE_EDIT_ITEM_WINDOW="PsAaEditItem";
this._TREE_PANE_SHOWING_COOKIE="treePaneShowing";
this._aaConflictStyleSheets=null;
this.helpUrl="../Docs/Rhythmyx/Active_Assembly_Interface/index.htm";
this.helpTutorialUrl="../Docs/Rhythmyx/Active_Assembly_Tutorial/index.htm";
this.helpAboutUrl="/Rhythmyx/ui/activeassembly/help/aboutaahelp.jsp";
};
dojo.provide("ps.workflow.WorkflowActions");
ps.workflow.WorkflowActions=function(){
this.actionId=null;
this.wfDlg=null;
this.contentId=null;
this.workflowActionsUrl="/Rhythmyx/ui/activeassembly/workflow/workflowactions.jsp";
this.adhocSearchUrl="/Rhythmyx/ui/activeassembly/workflow/adhocsearch.jsp";
this.adhocResultsUrl="/Rhythmyx/ui/activeassembly/workflow/adhocresults.jsp";
this.init=function(){
};
this.maybeCreateWorkflowDialog=function(){
if(this.wfDlg){
return;
}
this.wfDlg=ps.createDialog({id:"ps.workflow.WorkflowActionsDlg",title:"Workflow Actions"},"420px","250px");
var _this=this;
this.wfDlg.closeWindow=function(){
_this.wfDlg.hide();
};
dojo.event.connect(this.wfDlg,"onLoad",function(){
_this.parseControls();
if(_this._isUserAuthorized()){
_this.onWfActionChanged();
}
});
};
this.parseControls=function(){
var _this=this;
this.wfActionPane=dojo.byId("ps.workflow.actionPane");
if(this._isUserAuthorized()){
this.wfActionSelector=dojo.byId("ps.workflow.workflowActionSelect");
this.wfCommentText=dojo.byId("ps.workflow.commentText");
this.wfAdhocUsers=dojo.byId("ps.workflow.adhocUsers");
this.wgtAdhocSearch=dojo.widget.byId("ps.workflow.wgtButtonAdhocSearch");
var _116a=dojo.widget.byId("ps.workflow.wgtButtonSubmit");
var _116b=dojo.widget.byId("ps.workflow.wgtButtonCancel");
if(this.wfCommentText&&this.wgtAdhocSearch){
ps.util.setDialogSize(this.wfDlg,420,370);
}else{
if(this.wfCommentText||this.wgtAdhocSearch){
ps.util.setDialogSize(this.wfDlg,420,250);
}else{
ps.util.setDialogSize(this.wfDlg,420,125);
}
}
dojo.event.connect(this.wfActionSelector,"onchange",this,"onWfActionChanged");
if(this.wgtAdhocSearch){
dojo.event.connect(this.wgtAdhocSearch,"onClick",this,"openAdhocSearchDialog");
}
dojo.event.connect(_116a,"onClick",this,"executeWorkflowAction");
dojo.event.connect(_116b,"onClick",this,"onWfActionCancelled");
}else{
this.wgtButtonClose=dojo.widget.byId("ps.workflow.wgtButtonClose");
dojo.event.connect(this.wgtButtonClose,"onClick",this,"onWfActionCancelled");
ps.util.setDialogSize(this.wfDlg,420,125);
}
};
this.open=function(){
this.maybeCreateWorkflowDialog();
this.contentId=ps.aa.controller.activeId.getContentId();
var wfurl=ps.util.addParamToUrl(this.workflowActionsUrl,"sys_contentid",this.contentId);
this.wfDlg.setUrl(wfurl);
this.wfDlg.show();
};
this.onWfActionChanged=function(){
var index=this.wfActionSelector.selectedIndex;
if(index<0){
index=0;
}
var idStr=this.wfActionSelector.options[index].value;
this.actionId=new ps.workflow.ActionId(idStr);
if(this.wfCommentText){
if(!this.wfCommentRequiredStar){
this.wfCommentRequiredStar=dojo.byId("ps.workflow.commentStar");
}
if(this.actionId.isCommentBoxNeeded()){
this.wfCommentText.disabled=false;
this.wfCommentText.style.bgcolor="white";
if(this.actionId.isCommentRequired()){
this.wfCommentRequiredStar.style.visibility="visible";
}else{
this.wfCommentRequiredStar.style.visibility="hidden";
}
}else{
this.wfCommentText.disabled=true;
this.wfCommentText.style.bgcolor="grey";
this.wfCommentRequiredStar.style.visibility="hidden";
}
}
if(this.wgtAdhocSearch){
if(this.actionId.isAdhocBoxNeeded()){
this.wfAdhocUsers.disabled=false;
this.wfAdhocUsers.style.bgcolor="white";
this.wgtAdhocSearch.setDisabled(false);
}else{
this.wfAdhocUsers.disabled=true;
this.wfAdhocUsers.style.bgcolor="gray";
this.wgtAdhocSearch.setDisabled(true);
}
}
};
this.openAdhocSearchDialog=function(){
this._maybeCreateAdhocSearchDialog();
dojo.html.hide(this.wfActionPane);
this.adhocResultsPane.hide();
var wfurl=ps.util.addParamToUrl(this.adhocSearchUrl,"sys_contentid",this.contentId);
wfurl=ps.util.addParamToUrl(wfurl,"sys_transitionid",this.actionId.getTransitionId());
this.adhocSearchPane.cacheContent=false;
var _this=this;
dojo.event.connect(this.adhocSearchPane,"onLoad",function(){
_this.adhocRoleSelect=dojo.byId("ps.workflow.adhocRole");
_this.nameFilterText=dojo.byId("ps.workflow.nameFilter");
_this.wgtButtonSearch=dojo.widget.byId("ps.workflow.wgtButtonSearch");
_this.wgtButtonAdd=dojo.widget.byId("ps.workflow.wgtButtonAdd");
_this.wgtButtonClose=dojo.widget.byId("ps.workflow.wgtButtonClose");
if(!dojo.html.hasClass(_this.wgtButtonAdd.domNode,"dojoButtonDisabled")){
_this.wgtButtonAdd.setDisabled(true);
}
dojo.event.connect(_this.wgtButtonSearch,"onClick",_this,"onSearchClicked");
dojo.event.connect(_this.wgtButtonAdd,"onClick",_this,"onAddClicked");
dojo.event.connect(_this.wgtButtonClose,"onClick",_this,"onSearchClosed");
});
this.adhocSearchPane.setUrl(wfurl);
this.wfAdhocPane.show();
};
this._maybeCreateAdhocSearchDialog=function(){
if(this.wfAdhocPane){
return;
}
var div=document.createElement("div");
var style=div.style;
style.overflow="auto";
style.border="0px solid black";
this.wfActionPane.parentNode.appendChild(div);
this.wfAdhocPane=dojo.widget.createWidget("ContentPane",{titleBarDisplay:false,executeScripts:true},div);
this.wfAdhocPane.setContent("<table width=\"100%\">\n"+"<tr><td width=\"100%\">\n"+"<div dojoType=\"ContentPane\" id=\"ps.workflow.adhocSearchPane\" executeScripts=\"true\" style=\"border: 1px solid #e3edfa;\"/>\n"+"</td></tr>\n"+"<tr><td><table width=\"100%\">\n"+"<tr><td bgcolor=\"#e3edfa\" width=\"100%\">Search Results</td></tr>\n"+"<tr><td width=\"100%\">\n"+"<div dojoType=\"ContentPane\" id=\"ps.workflow.adhocResultsPane\" executeScripts=\"true\" style=\"border: 1px solid #e3edfa;\">\n"+"</div></td></tr>\n"+"</table></td></tr><tr>\n"+"<td align=\"center\" width=\"100%\">\n"+"<table width=\"100%\" align=\"left\" cellpadding=\"1\"><tr>\n"+"<td align=\"right\">\n"+"<button style=\"border: 1px solid black;\" dojoType=\"ps:PSButton\" id=\"ps.workflow.wgtButtonAdd\">Add</button>\n"+"</td>\n"+"<td align=\"left\">\n"+"<button style=\"border: 1px solid black;\" dojoType=\"ps:PSButton\" id=\"ps.workflow.wgtButtonClose\">Close</button>\n"+"</td>\n"+"</tr></table></td>\n"+"</tr>\n"+"<tr><td width=\"100%\" height=\"100%\"/></tr>\n"+"</table>");
this.adhocSearchPane=dojo.widget.byId("ps.workflow.adhocSearchPane");
dojo.lang.assert(this.adhocSearchPane,"Expected adhocSearchPane");
this.adhocResultsPane=dojo.widget.byId("ps.workflow.adhocResultsPane");
dojo.lang.assert(this.adhocResultsPane,"Expected adhocResultsPane");
};
this.executeWorkflowAction=function(){
var atype=this.actionId.getActionType();
var _1174=null;
var _1175=false;
var _1176=null;
if(this.actionId.isCommentRequired()&&this._getComment()===""){
alert("Comment must be entered for workflow transition <"+this.actionId.getActionLabel()+">.");
return false;
}
if(atype==ps.workflow.ActionId.ACTION_TYPE_CHECKIN||atype==ps.workflow.ActionId.ACTION_TYPE_FORCE_CHECKIN){
_1174=ps.io.Actions.checkInItem(this.contentId,this._getComment());
_1175=true;
_1176="0";
}else{
if(atype==ps.workflow.ActionId.ACTION_TYPE_CHECKOUT){
_1174=ps.io.Actions.checkOutItem(this.contentId,this._getComment());
_1175=true;
_1176="1";
}else{
if(atype==ps.workflow.ActionId.ACTION_TYPE_TRANSITION_CHECKOUT){
_1174=ps.io.Actions.transitionCheckOutItem(this.contentId,this.actionId.getWfAction(),this._getComment(),this._getAdhocUsers());
_1175=true;
_1176="1";
}else{
_1174=ps.io.Actions.transitionItem(this.contentId,this.actionId.getWfAction(),this._getComment(),this._getAdhocUsers());
if(ps.aa.controller.activeId.isCheckout()||ps.aa.controller.activeId.isCheckoutByMe()){
_1175=true;
_1176="0";
}
}
}
}
if(!_1174.isSuccess()){
ps.io.Actions.maybeReportActionError(_1174);
return false;
}
if(_1175){
this.handleObjectIdModifications(_1176);
}
ps.aa.controller.refreshOpener(this.contentId);
this.wfDlg.hide();
if(this.wfAdhocPane){
location.reload();
}
};
this.handleObjectIdModifications=function(_1177){
dojo.lang.assert((_1177==="0"||_1177==="1"||_1177==="2"),"checkOutStatus must be 0, 1, or 2");
var _1178=ps.aa.controller.treeModel.getAllIdsByContentId(this.contentId);
dojo.lang.assertType(_1178,dojo.collections.ArrayList);
var _1179=new Array();
var _117a=new Array();
var _117b=new Array();
for(var i=0;i<_1178.count;i++){
var _117d=_1178.item(i);
dojo.lang.assertType(_117d,ps.aa.ObjectId);
_1179[i]=_117d;
var newId=_117d.clone();
newId.setCheckoutStatus(_1177);
_117a[i]=newId;
if(_117d.isSlotNode()){
_117b.push(newId);
}
}
ps.aa.controller.replaceIds(_1179,_117a);
if(_1177==1){
for(var j=0;j<_117b.length;j++){
ps.aa.controller.refreshSlot(_117b[j]);
}
if(_117b.length>0){
ps.aa.controller.updateTreeWidget();
}
}
};
this.onWfActionCancelled=function(){
this.wfDlg.hide();
if(this.wfAdhocPane){
location.reload();
}
};
this.onSearchClicked=function(){
this.adhocResultsPane.show();
var wfurl=ps.util.addParamToUrl(this.adhocResultsUrl,"sys_contentid",this.contentId);
wfurl=ps.util.addParamToUrl(wfurl,"sys_transitionid",this.actionId.getTransitionId());
wfurl=ps.util.addParamToUrl(wfurl,"rolename",this.adhocRoleSelect.value);
wfurl=ps.util.addParamToUrl(wfurl,"namefilter","%"+this.nameFilterText.value+"%");
var mm=this;
dojo.event.connect(this.adhocResultsPane,"onLoad",function(){
var count=dojo.byId("ps.workflow.adhocusercount").value;
mm.adhocUsersChk=new Array();
for(var i=0;i<count;i++){
mm.adhocUsersChk[i]=dojo.byId("ps.workflow.adhocusercheckbox_"+i);
}
});
this.adhocResultsPane.cacheContent=false;
this.adhocResultsPane.setUrl(wfurl);
};
this.onUserChecked=function(){
var _self=this;
setTimeout(function(){
var _1185=true;
for(var i=0;i<_self.adhocUsersChk.length;i++){
if(_self.adhocUsersChk[i].checked){
_1185=false;
break;
}
}
_self.wgtButtonAdd.setDisabled(_1185);
},250);
};
this.onAddClicked=function(){
var _1187=new Array();
var count=0;
for(var i=0;i<this.adhocUsersChk.length;i++){
if(this.adhocUsersChk[i].checked){
_1187[count++]=this.adhocUsersChk[i].value;
}
}
if(_1187.length<1){
alert("Please select at least one user to add.");
return false;
}
var _118a="";
var _118b=this.wfAdhocUsers.value;
var _118c=_118b.split(";");
for(var i=0;i<_118c.length;i++){
var val=_118c[i];
if(val!=""){
_118a=this._appendWithDel(_118a,";",val);
}
}
for(i=0;i<_1187.length;i++){
var val=_1187[i];
if(val.length>0&&!this._contains(_118c,val)){
_118a=this._appendWithDel(_118a,";",val);
}
}
this.wfAdhocUsers.value=_118a;
dojo.html.show(this.wfActionPane);
this.wfAdhocPane.hide();
};
this.onSearchClosed=function(){
dojo.html.show(this.wfActionPane);
this.wfAdhocPane.hide();
};
this._contains=function(_118e,value){
var len=_118e.length;
var i=0;
while(i<len){
if(_118e[i]==value){
return true;
}
i++;
}
return false;
};
this._appendWithDel=function(_1192,del,_1194){
if(_1192.length==0){
return _1194;
}else{
return _1192+del+_1194;
}
};
this._getComment=function(){
return this.wfCommentText?dojo.string.trim(this.wfCommentText.value):"";
};
this._getAdhocUsers=function(){
return this.wfAdhocUsers?dojo.string.trim(this.wfAdhocUsers.value):"";
};
this._isUserAuthorized=function(){
return this.wfActionPane;
};
};
ps.workflow.ActionId=function(_1195){
this.idString=_1195;
this.idobj=dojo.json.evalJson(_1195);
this.equals=function(other){
if((typeof other=="undefined")||other==null){
return false;
}else{
return this.idString==other.idString;
}
};
this.serialize=function(){
return this.idString;
};
this.isCommentBoxNeeded=function(){
return this.idobj[ps.workflow.ActionId.WORKFLOW_COMMENT]>0;
};
this.isCommentRequired=function(){
return this.idobj[ps.workflow.ActionId.WORKFLOW_COMMENT]==2;
};
this.isAdhocBoxNeeded=function(){
return this.idobj[ps.workflow.ActionId.SHOW_ADHOC]==1;
};
this.getActionType=function(){
return this.idobj[ps.workflow.ActionId.ACTION_TYPE];
};
this.getWfAction=function(){
return this.idobj[ps.workflow.ActionId.WF_ACTION];
};
this.getTransitionId=function(){
return this.idobj[ps.workflow.ActionId.WF_TRANSITIONID];
};
this.getActionLabel=function(){
return this.idobj[ps.workflow.ActionId.WF_ACTION_LABEL];
};
};
ps.workflow.ActionId.ACTION_NAME=0;
ps.workflow.ActionId.WORKFLOW_COMMENT=1;
ps.workflow.ActionId.SHOW_ADHOC=2;
ps.workflow.ActionId.ACTION_TYPE=3;
ps.workflow.ActionId.WF_ACTION=4;
ps.workflow.ActionId.WF_TRANSITIONID=5;
ps.workflow.ActionId.WF_ACTION_LABEL=6;
ps.workflow.ActionId.ACTION_TYPE_CHECKIN=0;
ps.workflow.ActionId.ACTION_TYPE_FORCE_CHECKIN=1;
ps.workflow.ActionId.ACTION_TYPE_CHECKOUT=2;
ps.workflow.ActionId.ACTION_TYPE_TRANSITION_CHECKOUT=3;
ps.workflow.ActionId.ACTION_TYPE_TRANSITION=4;
dojo.provide("ps.aa.Field");
ps.aa.Field=function(){
this.objectId=null;
this.refreshField=false;
this.psCeFieldWindow=null;
this.fieldModalDlg=null;
this.renderer=null;
this.ceUrl=null;
this.divElem=null;
this.inplaceEditing=null;
this.init=function(){
};
this.maybeCreateFieldModalDlg=function(){
if(this.fieldModalDlg){
return;
}
this.fieldModalDlg=ps.createDialog({id:"ps.Field.FieldEditingDlg",title:"Edit Field"},"200px","100px");
var _this=this;
this.fieldModalDlg.closeWindow=function(){
_this.fieldModalDlg.hide();
};
dojo.event.connect(this.fieldModalDlg,"onLoad",function(){
_this.parseControls();
});
};
this.maybeCreateInplaceDlg=function(){
if(this.inplaceDlg){
return;
}
var div=document.createElement("div");
div.style.position="absolute";
div.style.border="0px";
document.body.appendChild(div);
this.inplaceDlg=dojo.widget.createWidget("ModalFloatingPane",{id:"ps.field.inplaceTextBoxDiv",titleBarDisplay:false,bgColor:ps.DIALOG_BACKGROUND,bgOpacity:ps.DIALOG_BACKGROUND_OPACITY,executeScripts:true,resizable:false},div);
this.inplaceDlg.setContent("<input type=\"text\" style=\"border:0px; padding:0px; margin-top:1px\""+"size=\"50\" id=\"ps.field.inplaceTextBox\" "+"name=\"ps.field.inplaceTextBox\"/>\n"+"<div class=\"PsAaFieldButtonsbox\">\n"+"<table align=\"center\" width=\"100%\" border=\"0\">\n"+"<tr>\n"+"<td align=\"right\">\n"+"<button dojoType=\"Button\" id=\"ps.field.inplaceUpdateButton\">"+"Update</button>\n"+"</td>\n"+"<td align=\"left\">\n"+"<button dojoType=\"Button\" id=\"ps.field.inplaceCancelButton\">"+"Cancel</button>\n"+"</td>\n"+"</tr>\n"+"</table>\n"+"</div>");
this.inplaceTextBox=dojo.byId("ps.field.inplaceTextBox");
dojo.event.connect(this.inplaceTextBox,"onkeyup",this,"_onInplaceTextTyped");
var _1199=dojo.widget.byId("ps.field.inplaceUpdateButton");
dojo.lang.assert(_1199,"Update button could not be found");
var _119a=dojo.widget.byId("ps.field.inplaceCancelButton");
dojo.lang.assert(_119a,"Cancel button could not be found");
dojo.event.connect(_1199,"onClick",this,"updateField");
dojo.event.connect(_119a,"onClick",this,"onInplaceCancel");
};
this.parseControls=function(){
this.wgtButtonFullEditor=dojo.widget.byId("ps.Field.wgtButtonFullEditor");
this.wgtButtonUpdate=dojo.widget.byId("ps.Field.wgtButtonUpdate");
this.wgtButtonClose=dojo.widget.byId("ps.Field.wgtButtonClose");
this.divRegularButtons=dojo.byId("psRegularButtons");
this.divDojoButtons=dojo.byId("psDojoButtons");
this.divRegularButtons.style.visibility="hidden";
this.divDojoButtons.style.visibility="visible";
dojo.event.connect(this.wgtButtonFullEditor,"onClick",this,"openFullEditor");
dojo.event.connect(this.wgtButtonUpdate,"onClick",this,"updateField");
dojo.event.connect(this.wgtButtonClose,"onClick",this,"_onDialogClose");
var edfrm=dojo.byId("EditForm");
edfrm.setAttribute("onsubmit","");
var ceurl=this.ceUrl.split("?")[0];
var _119d=ps.io.Actions.getUpdateItemUrl()+"&ceUrl="+encodeURI(ceurl);
ps.io.Actions.initFormBind(_119d,"EditForm",ps.io.Actions.MIMETYPE_JSON);
this.initialCheckSum=ps_getAllFieldChecksums(document.EditForm,true);
};
this._onDialogClose=function(){
var _119e=ps_getAllFieldChecksums(document.EditForm,false);
if(_119e!=this.initialCheckSum){
if(confirm(this.FORM_CHANGE_WARNING_FOR_CLOSING)){
this.updateField();
}
}
this.fieldModalDlg.hide();
};
this.openFullEditor=function(){
var _119f=ps_getAllFieldChecksums(document.EditForm,false);
if(_119f!=this.initialCheckSum){
if(confirm(this.FORM_CHANGE_WARNING_FOR_FULLEDITOR)){
this.updateField();
}
}
ps.aa.controller.editAll();
this.fieldModalDlg.hide();
};
this.editField=function(_11a0,e){
if(this.checkClickEvent(e)){
return true;
}
ps.aa.controller.activate(_11a0);
var _11a2=ps.aa.Page.getObjectId(_11a0);
if(_11a2.isCheckoutByMe()==0){
alert(ps.aa.controller.CHECKOUT_MSG);
return false;
}
if(ps.aa.controller.psCeWindow&&!ps.aa.controller.psCeWindow.closed){
if(!confirm(ps.aa.controller.EDITOROPEN_MSG)){
ps.aa.controller.psCeWindow.focus();
return false;
}
ps.aa.controller.psCeWindow.close();
}else{
if(this.psCeFieldWindow&&!this.psCeFieldWindow.closed){
if(_11a2.equals(this.objectId)||!confirm(ps.aa.controller.EDITOROPEN_MSG)){
this.psCeFieldWindow.focus();
return false;
}
this.psCeFieldWindow.close();
}else{
if(this.inplaceEditing){
if(!confirm(ps.aa.controller.INPLACE_EDITOROPEN_MSG)){
return false;
}
this.onInplaceCancel();
}
}
}
this.objectId=ps.aa.controller.activeId;
this.divElem=_11a0;
ps.aa.controller.editObjectId=this.objectId;
var _11a3=ps.io.Actions.getUrl(this.objectId,"CE_FIELDEDIT");
if(!_11a3.isSuccess()){
ps.io.Actions.maybeReportActionError(_11a3);
return false;
}
var value=_11a3.getValue();
dojo.lang.assert(dojo.lang.has(value,"url"));
this.ceUrl=value.url;
var dlgw=value.dlg_width?value.dlg_width:this.DEFAULT_CONTROL_WIDTH;
var dlgh=value.dlg_height?value.dlg_height:this.DEFAULT_CONTROL_HEIGHT;
this.renderer=value.aarenderer?value.aarenderer:this.DEFAULT_FIELD_RENDERER;
if(this.renderer==this.FIELD_RENDERER_NONE){
if(confirm(this.FIELD_RENDERER_NONE_MESSAGE)){
ps.aa.controller.editAll();
}
return;
}else{
if(this.renderer==this.FIELD_RENDERER_MODAL){
this.maybeCreateFieldModalDlg();
this.fieldModalDlg.setUrl(this.ceUrl);
this.fieldModalDlg.show();
this.fieldModalDlg.resizeTo(dlgw,dlgh);
}else{
if(this.renderer==this.FIELD_RENDERER_INPLACE_TEXT){
var _11a3=ps.io.Actions.getContentEditorFieldValue(this.objectId);
if(!_11a3.isSuccess()){
ps.io.Actions.maybeReportActionError(_11a3);
return false;
}
this.onInplaceEdit(_11a3.getValue());
}else{
var _11a7="toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width="+dlgw+",height="+dlgh;
this.psCeFieldWindow=window.open(this.ceUrl,ps.aa.controller.CE_EDIT_ITEM_WINDOW,_11a7);
this.psCeFieldWindow.focus();
return false;
}
}
}
},this.checkClickEvent=function(e){
if(e){
if(e.ctrlKey||e.shiftKey){
return true;
}
dojo.event.browser.stopEvent(e);
if(e.altKey){
var tgt=e.target;
if(typeof (tgt)=="undefined"){
tgt=e.srcElement;
}
if(dojo.html.isTag(tgt,"a")){
window.location.href=tgt.href;
return true;
}else{
if(dojo.html.isTag(tgt,"div")){
var _11aa=dojo.html.getAttribute(tgt,"fieldLink");
if(typeof (_11aa)!="undefined"&&_11aa.length>0){
window.location.href=_11aa;
return true;
}
}
}
return true;
}
}
return false;
};
this._onInplaceTextTyped=function(e){
e_v=e;
if(e.ctrlKey||e.shiftKey||e.altKey||e.metaKey){
return;
}
if(e.keyCode===e.KEY_ENTER){
this.updateField();
}else{
if(e.keyCode===e.KEY_ESCAPE){
this.onInplaceCancel();
}
}
};
this.onInplaceCancel=function(){
this.inplaceEditing=false;
this.inplaceDlg.hide();
dojo.html.show(this.divElem);
};
this.onInplaceEdit=function(value){
this.inplaceEditing=true;
this.maybeCreateInplaceDlg();
this.inplaceTextBox.value=value;
var dn=this.inplaceDlg.domNode;
var _11ae=dojo.html.getAbsolutePosition(this.divElem);
var _11af=0;
dn.style.left=(_11ae.left-_11af)+"px";
dn.style.top=(_11ae.top-_11af)+"px";
var width=dojo.html.getBorderBox(this.divElem).width;
this.inplaceTextBox.style.width=width+"px";
this.inplaceDlg.placeModalDialog=function(){
};
this.inplaceDlg.show();
if(!this.inplaceDlgHeight){
this.inplaceDlgHeight=dojo.html.getBorderBox(this.inplaceDlg.domNode).height+10;
}
this.inplaceDlg.resizeTo(width,this.inplaceDlgHeight);
this.inplaceTextBox.focus();
dojo.html.hide(this.divElem);
};
this.updateField=function(){
if(this.renderer==this.FIELD_RENDERER_MODAL){
var _11b1=ps.io.Actions.submitForm(document.EditForm);
if(!_11b1.isSuccess()){
ps.io.Actions.maybeReportActionError(_11b1);
return false;
}
var value=_11b1.getValue();
if(dojo.lang.has(value,"cmsError")){
alert(value.cmsError);
return false;
}else{
if(dojo.lang.has(value,"validationError")){
if(!confirm(value.validationError+this.FIELD_VALIDATION_CONFIRM_MSG_PART2)){
return false;
}
this.fieldModalDlg.hide();
ps.aa.controller.editAll(value.ceCachedPageUrl);
return false;
}else{
this.initialCheckSum=ps_getAllFieldChecksums(document.EditForm,true);
}
}
}else{
if(this.renderer==this.FIELD_RENDERER_INPLACE_TEXT){
var _11b1=ps.io.Actions.setContentEditorFieldValue(this.objectId,this.inplaceTextBox.value);
if(!_11b1.isSuccess()){
ps.io.Actions.maybeReportActionError(_11b1);
return false;
}
var value=_11b1.getValue();
if(dojo.lang.has(value,"cmsError")){
alert(value.cmsError);
return false;
}else{
if(dojo.lang.has(value,"validationError")){
if(!confirm(value.validationError+this.FIELD_VALIDATION_CONFIRM_MSG_PART2)){
return false;
}
this.onInplaceCancel();
ps.aa.controller.editAll();
return false;
}
}
this.onInplaceCancel();
}
}
ps.aa.controller.refreshFieldsOnPage(this.objectId.getContentId(),this.objectId.getFieldName(),this.psCeFieldWindow);
};
this.DEFAULT_CONTROL_HEIGHT=300;
this.DEFAULT_CONTROL_WIDTH=400;
this.DEFAULT_FIELD_RENDERER=this.FIELD_RENDERER_POPUP;
this.FIELD_RENDERER_MODAL="MODAL";
this.FIELD_RENDERER_POPUP="POPUP";
this.FIELD_RENDERER_NONE="NONE";
this.FIELD_RENDERER_INPLACE_TEXT="INPLACE_TEXT";
this.FIELD_VALIDATION_CONFIRM_MSG_PART2="\nClick OK to open the full content editor or Cancel to continue editing.";
this.FORM_CHANGE_WARNING_FOR_CLOSING="Changes have been made.\nDo you want to save before closing?";
this.FORM_CHANGE_WARNING_FOR_FULLEDITOR="Changes have been made.\nDo you want to save before opening full editor?";
this.FIELD_RENDERER_NONE_MESSAGE="The source of the data comes from a hidden field.\nIt can probably be modified by editing another field in the content item.\n\nClick OK to open the full editor.";
};
dojo.kwCompoundRequire({common:["ps.aa.controller","ps.aa.dnd","ps.aa.Field","ps.aa.Menu","ps.aa.Page","ps.aa.SnippetMove","ps.aa.Tree"]});
dojo.provide("ps.aa.*");
dojo.kwCompoundRequire({common:["ps.content.Browse","ps.content.History","ps.content.SelectTemplates"]});
dojo.provide("ps.content.*");
dojo.kwCompoundRequire({common:["ps.io.Actions","ps.io.Response"]});
dojo.provide("ps.io.*");
dojo.provide("ps.widget.MenuBar2");
dojo.widget.defineWidget("ps.widget.MenuBar2",dojo.widget.MenuBar2,{closeSubmenu:function(force){
if(this.currentSubmenu==null){
return;
}
if(this.currentSubmenu.parent){
this.currentSubmenu.parent=null;
}
this.currentSubmenu.close(force);
this.currentSubmenu=null;
this.currentSubmenuTrigger.is_open=false;
this.currentSubmenuTrigger._closedSubmenu(force);
this.currentSubmenuTrigger=null;
}});
dojo.provide("ps.widget.MenuBarIcon");
dojo.widget.defineWidget("ps.widget.MenuBarIcon",dojo.widget.MenuBarItem2,{templateString:"<span class=\"dojoMenuItem2\" dojoAttachEvent=\"onClick: _onClick;\">"+"<img src=\"../sys_resources/images/aa/page_1.gif\" alt=\"Icon\" title=\"\" verticalAlign=\"middle\"/>"+"</span>",imgDomNode:null,setImage:function(_11b4){
if(this.imgDomNode==null){
this.imgDomNode=dojo.dom.getFirstChildElement(this.domNode,"img");
}
this.imgDomNode.setAttribute("src",_11b4);
},setTitle:function(title){
if(this.imgDomNode==null){
this.imgDomNode=dojo.dom.getFirstChildElement(this.domNode,"img");
}
this.imgDomNode.setAttribute("title",title);
}});
dojo.provide("ps.widget.MenuBarItem2");
dojo.widget.defineWidget("ps.widget.MenuBarItem2",dojo.widget.MenuBarItem2,{});
dojo.provide("ps.widget.MenuBarItemDropDown");
dojo.widget.defineWidget("ps.widget.MenuBarItemDropDown",dojo.widget.MenuBarItem2,function(){
var _this=this;
function onCreateSubmenu(){
if(_this.submenuCreated){
return;
}
_this.submenuCreated=true;
if(_this.createSubmenu){
_this.createSubmenu();
}
}
dojo.event.connectBefore(this,"_onClick",onCreateSubmenu);
dojo.event.connectBefore(this,"_openSubmenu",onCreateSubmenu);
},{templateString:"<span class=\"dojoMenuItem2\" dojoAttachEvent=\"onMouseOver: onHover; "+"onMouseOut: onUnhover; onClick: _onClick;\">"+"${this.caption} <span><img src=\""+dojo.uri.dojoUri("../ps/widget/images/dropdownButtonsArrow.gif")+"\" verticalAlign=\"middle\"/></span></span>",createSubmenu:null});
dojo.provide("ps.widget.PopupMenu");
dojo.widget.defineWidget("ps.widget.PopupMenu",dojo.widget.PopupMenu2,{bindTargetNodes:function(_11b7){
for(var i=0;i<_11b7.length;i++){
this.bindDomNode(_11b7[i]);
}
},unBindTargetNodes:function(_11b9){
for(var i=0;i<_11b9.length;i++){
this.unBindDomNode(_11b9[i]);
}
},onOpen:function(e){
if(this.createMenuItems&&!this.itemsCreated){
this.createMenuItems();
}
this.itemsCreated=true;
if(e.currentTarget!=null){
ps.aa.controller.activate(e.currentTarget);
}
ps.widget.PopupMenu.superclass.onOpen.apply(this,arguments);
}});
dojo.provide("ps.widget.PSSplitContainer");
dojo.widget.defineWidget("ps.widget.PSSplitContainer",dojo.widget.SplitContainer,{sizerEnabled:true,sizerVisible:true,beginSizing:function(e,i){
if(this.sizerEnabled){
ps.widget.PSSplitContainer.superclass.beginSizing.apply(this,arguments);
}
},postCreate:function(args,_11bf,_11c0){
ps.widget.PSSplitContainer.superclass.postCreate.apply(this,arguments);
if(!this.sizerEnabled){
this.virtualSizer.style.cursor="default";
for(i=0;i<this.sizers.length;i++){
this.sizers[i].style.cursor="default";
}
}
if(!this.sizerVisible){
this.virtualSizer.style.visibility="hidden";
for(i=0;i<this.sizers.length;i++){
this.sizers[i].style.visibility="hidden";
}
}
}});
dojo.provide("ps.widget.Tree");
dojo.widget.defineWidget("ps.widget.Tree",dojo.widget.TreeV3,{selector:"",model:null,loaded:false,treeController:null,templateCssPath:dojo.uri.moduleUri("ps","widget/Tree.css"),delayedInitSlotNodes:[],delayedInitItemNodes:[],indexOfSupported:Array.indexOf,createWidgetFromModelNode:function(_11c1){
dojo.lang.assertType(_11c1,ps.aa.TreeNode);
var title=_11c1.getLabel();
var _11c3=_11c1.objId.getTreeNodeWidgetId();
var _11c4=dojo.widget.createWidget("TreeNodeV3",{title:title,tree:this.widgetId,id:_11c3,modelId:_11c1.objId,tryLazyInit:true,isFolder:!_11c1.isLeafNode()});
var _this=this;
_11c4.setChildren=function(){
var n=_this.model.getNodeById(_11c4.modelId);
if(!n.isLeafNode()){
for(var i=0;i<n.childNodes.count;i++){
var _11c8=n.childNodes.item(i);
_11c4.addChild(_this.createWidgetFromModelNode(_11c8));
}
}
};
var oid=_11c1.objId;
var _11ca=!oid.isSnippetNode();
var _11cb=_11c1.parentNode&&_11c1.parentNode.objId.isCheckoutByMe();
var _11cc=!(oid.isSlotNode()&&_11cb)||oid.isPageNode();
var _11cd=[];
if(_11cc){
_11cd.push("ADDCHILD");
}
if(_11ca){
_11cd.push("MOVE");
}
_11c4.actionsDisabled=_11cd;
_11c4.domNode.setAttribute("id",_11c3);
this._bindContextMenu(_11c4);
return _11c4;
},_bindContextMenu:function(_11ce){
var objId=_11ce.modelId;
if(objId.isSlotNode()){
if(ps.aa.Menu.slotCtxMenu){
ps.aa.Menu.slotCtxMenu.bindTargetNodes([_11ce.domNode]);
}else{
dojo.lang.assert(!this.indexOfSupported||this.delayedInitSlotNodes.indexOf(_11ce.domNode)===-1,"Slot node is registered more than once: "+_11ce.domNode);
this.delayedInitSlotNodes.push(_11ce.domNode);
}
}else{
if(ps.aa.Menu.itemCtxMenu){
ps.aa.Menu.itemCtxMenu.bindTargetNodes([_11ce.domNode]);
}else{
dojo.lang.assert(!this.indexOfSupported||this.delayedInitItemNodes.indexOf(_11ce.domNode)===-1,"Snippet node is registered more than once: "+_11ce.domNode);
this.delayedInitItemNodes.push(_11ce.domNode);
}
}
},_unBindContextMenu:function(_11d0){
var objId=_11d0.modelId;
if(objId.isSlotNode()){
ps.aa.Menu.slotCtxMenu.unBindTargetNodes([_11d0.domNode]);
}else{
ps.aa.Menu.itemCtxMenu.unBindTargetNodes([_11d0.domNode]);
}
},loadFromModel:function(model){
this.treeController=dojo.widget.manager.getWidgetById("treeController");
this.actionsDisabled.push("ADDCHILD");
this.model=model;
this._loadModel();
},loadFromModelAsynch:function(model){
dojo.lang.assertType(model,ps.aa.Tree);
if(this.delayedInitSlotNodes.length){
ps.aa.Menu.slotCtxMenu.bindTargetNodes(this.delayedInitSlotNodes);
}
if(this.delayedInitItemNodes.length){
ps.aa.Menu.itemCtxMenu.bindTargetNodes(this.delayedInitItemNodes);
}
},_loadModel:function(){
if(this.loaded){
dojo.lang.assert(this.children);
dojo.lang.assertType(this.children,Array);
if(this.children.length>0){
var child=this.children[0];
dojo.lang.assertType(child,dojo.widget.TreeNodeV3);
this.removeChild(child);
child.destroy();
}
var _11d5=dojo.widget.manager.getWidgetById("treeDndController");
if(_11d5){
_11d5.reset();
}
}
var _11d6=this.model.getRootNode();
var _11d7=this.createWidgetFromModelNode(_11d6);
this.addChild(_11d7);
this.treeController.expandToLevel(this,this.expandLevel);
this.loaded=true;
},dndInit:function(){
if(dojo.widget.manager.getWidgetById("treeDndController")){
return;
}
var _11d8=dojo.widget.createWidget("ps:TreeDndController",{id:"treeDndController",controller:"treeController"});
_11d8.listenTree(this);
},_updateWidgetFromModelNode:function(_11d9,_11da){
var _11db=this.getWidgetFromModelNode(_11d9);
if(_11db){
if(!_11da){
}else{
if(_11da==_11db.parent){
_11db.doDetach();
}else{
_11db.doDetach();
}
}
if(_11db.title!=_11d9.getLabel()){
_11db.setTitle(_11d9.getLabel());
}
_11db.modelId=_11d9.objId;
}else{
_11db=this.createWidgetFromModelNode(_11d9);
}
return _11db;
},_cleanTree:function(){
var _11dc=[];
var root=this.children[0];
var stack=[root];
while(wNode=stack.pop()){
var mNode=this.model.getNodeById(wNode.modelId);
if(mNode){
for(var i=0;i<wNode.children.length;i++){
stack.push(wNode.children[i]);
}
}else{
_11dc.push(wNode);
}
}
for(var i=0;i<_11dc.length;i++){
this._removeNodes(_11dc[i]);
}
},_removeNodes:function(node){
dojo.lang.assert(node,"Can't remove null node.");
while(node.children.length>0){
this._removeNodes(node.children[0]);
}
this._removeNode(node);
},_removeNode:function(node){
dojo.lang.assert(node,"Can't remove null node.");
dojo.lang.assert(!node.children.length,"Can't remove a node with children.");
this._unBindContextMenu(node);
node.destroy();
},_synchModel:function(){
this._cleanTree();
if(this.children.length==0){
this._loadModel();
return;
}
var _11e3=this.model.getRootNode();
var _11e4=this._updateWidgetFromModelNode(_11e3,null);
var child=this.children[0];
dojo.lang.assert(child==_11e4,"The root widget node should not have changed.");
var _11e6={model:_11e3,widget:_11e4};
var stack=[_11e6];
while(mw=stack.pop()){
var w=mw.widget;
var m=mw.model;
if(!m.isLeafNode()&&!w.tryLazyInit){
for(var i=0;i<m.childNodes.count;i++){
var _11eb=m.childNodes.item(i);
var _11ec=this._updateWidgetFromModelNode(_11eb,w);
stack.push({model:_11eb,widget:_11ec});
w.addChild(_11ec,i,false);
}
}else{
}
this._updateIsFolderFromModel(w,m);
}
this.loaded=true;
var _11ed=dojo.widget.manager.getWidgetById("treeDndController");
if(_11ed){
_11ed.reset();
}
},_updateIsFolderFromModel:function(_11ee,_11ef){
if(_11ee.tryLazyInit){
if(!_11ef.isLeafNode()!==_11ee.isFolder){
if(_11ef.isLeafNode()){
_11ee.unsetFolder();
}else{
_11ee.setFolder();
}
}
}else{
}
},getWidgetFromModelId:function(objId){
var _11f1=objId.getTreeNodeWidgetId();
var _11f2=dojo.widget.manager.getWidgetById(_11f1);
return _11f2;
},getWidgetFromModelNode:function(_11f3){
return this.getWidgetFromModelId(_11f3.objId);
},activate:function(_11f4){
var _this=this;
function expandTo(n){
dojo.lang.assert(n,"Tree model node is expected to be not null.");
if(n.parentNode){
expandTo(n.parentNode);
var w=_this.getWidgetFromModelId(n.parentNode.objId);
dojo.lang.assert(w,"Can't find a widget for  "+n.parentNode);
w.expand();
}
}
var _11f8=this.model.getNodeById(_11f4);
expandTo(_11f8);
var _11f9=this.getWidgetFromModelId(_11f8.objId);
var _11fa=dojo.widget.manager.getWidgetById(this.selector);
_11fa.deselectAll();
_11fa.select(_11f9);
},doMove:function(child,_11fc,index){
dojo.lang.assert(child.modelId,"Node being moved does not have a model");
dojo.lang.assert(_11fc.modelId,"Node being moved does not have a model");
var pid=_11fc.modelId;
var cid=child.modelId;
dojo.lang.assert(cid.isSnippetNode(),"child is not a snippet.");
var _1200=this.model.getNodeById(child.modelId);
var _1201=_1200.parentNode;
dojo.lang.assert(_1201,"Unable to get the parent model node.");
var sid=_1201.objId;
dojo.lang.assert(sid.isSlotNode(),"Original parent of child is not a slot.");
dojo.lang.assert(pid.isSlotNode(),"New parent is not a slot.");
var _1203=pid;
var _1204=cid;
var _1205=sid;
var move=new ps.aa.SnippetMove(_1204,_1205,_1203,(index+1),false);
var _1207=ps.aa.controller.moveToSlot(move);
if(_1207==true){
var _1208=move.getTargetSnippetId();
try{
ps.aa.controller.activate(_1208);
}
catch(e){
dojo.debug("Ignore on a template change request");
dojo.debug(e);
}
}else{
dojo.debug("Tree - move failed.");
}
},onModelChanged:function(){
if(this.loaded){
this._synchModel();
}else{
this._loadModel();
}
}});
dojo.provide("dojo.widget.TreeDndControllerV3");
dojo.experimental("Tree drag'n'drop' has lots of problems/bugs, it requires dojo drag'n'drop overhaul to work, probably in 0.5");
dojo.widget.defineWidget("dojo.widget.TreeDndControllerV3",[dojo.widget.HtmlWidget,dojo.widget.TreeCommon],function(){
this.dragSources={};
this.dropTargets={};
this.listenedTrees={};
},{listenTreeEvents:["afterChangeTree","beforeTreeDestroy","afterAddChild"],listenNodeFilter:function(elem){
return elem instanceof dojo.widget.Widget;
},initialize:function(args){
this.treeController=dojo.lang.isString(args.controller)?dojo.widget.byId(args.controller):args.controller;
if(!this.treeController){
dojo.raise("treeController must be declared");
}
},onBeforeTreeDestroy:function(_120b){
this.unlistenTree(_120b.source);
},onAfterAddChild:function(_120c){
this.listenNode(_120c.child);
},onAfterChangeTree:function(_120d){
if(!_120d.oldTree){
return;
}
if(!_120d.newTree||!this.listenedTrees[_120d.newTree.widgetId]){
this.processDescendants(_120d.node,this.listenNodeFilter,this.unlistenNode);
}
if(!this.listenedTrees[_120d.oldTree.widgetId]){
this.processDescendants(_120d.node,this.listenNodeFilter,this.listenNode);
}
},listenNode:function(node){
if(!node.tree.DndMode){
return;
}
if(this.dragSources[node.widgetId]||this.dropTargets[node.widgetId]){
return;
}
var _120f=null;
var _1210=null;
if(!node.actionIsDisabled(node.actions.MOVE)){
var _120f=this.makeDragSource(node);
this.dragSources[node.widgetId]=_120f;
}
var _1210=this.makeDropTarget(node);
this.dropTargets[node.widgetId]=_1210;
},makeDragSource:function(node){
return new dojo.dnd.TreeDragSourceV3(node.contentNode,this,node.tree.widgetId,node);
},makeDropTarget:function(node){
return new dojo.dnd.TreeDropTargetV3(node.contentNode,this.treeController,node.tree.DndAcceptTypes,node);
},unlistenNode:function(node){
if(this.dragSources[node.widgetId]){
dojo.dnd.dragManager.unregisterDragSource(this.dragSources[node.widgetId]);
delete this.dragSources[node.widgetId];
}
if(this.dropTargets[node.widgetId]){
dojo.dnd.dragManager.unregisterDropTarget(this.dropTargets[node.widgetId]);
delete this.dropTargets[node.widgetId];
}
}});
dojo.provide("ps.widget.TreeDndController");
dojo.widget.defineWidget("ps.widget.TreeDndController",dojo.widget.TreeDndControllerV3,function(){
this.dragSources={};
this.dropTargets={};
this.listenedTrees={};
},{onBeforeTreeDestroy:function(_1214){
dojo.debug("I would be not listening anymore but I am going to.");
},reset:function(){
this.dragSources={};
this.dropTargets={};
},makeDropTarget:function(node){
var _1216=dojo.widget.TreeDndControllerV3.prototype.makeDropTarget.apply(this,arguments);
dojo.event.connectAround(_1216,"onDragOver",this,"_onDragOver");
return _1216;
},_onDragOver:function(_1217){
var _1218=_1217.proceed();
var _1219=_1217.object.treeNode.modelId;
var _121a=null;
if(_1219.isSlotNode()){
_121a=_1219;
}else{
if(_1219.isSnippetNode()){
var _121b=ps.aa.controller.treeModel.getNodeById(_1219);
_121a=_121b.parentNode.objId;
}
}
if(_1218&&_121a){
var _121c=_1217.args[0].dragObjects[0].dragSource;
var _121d=_121c.treeNode.modelId;
var _121b=ps.aa.controller.treeModel.getNodeById(_121d);
var _121e=_121b.parentNode.objId;
return _121e.belongsToTheSameItem(_121a);
}else{
return false;
}
dojo.lang.assert(false,"Should not reach here");
}});
dojo.provide("ps.widget.TreeIcon");
dojo.widget.defineWidget("ps.widget.TreeIcon",dojo.widget.TreeDocIconExtension,{templateCssPath:dojo.uri.moduleUri("ps","widget/TreeIcon.css"),getnodeDocType:function(node){
dojo.lang.assert(node);
dojo.lang.assertType(node,dojo.widget.TreeNodeV3);
var oid=node.modelId;
dojo.lang.assert(oid,"node does not have model id attached to it.");
var _1221=this._getNodeType(oid);
return _1221;
},_getNodeType:function(objId){
dojo.lang.assertType(objId,ps.aa.ObjectId);
var _1223;
if(objId.isPageNode()){
_1223=ps.aa.PAGE_CLASS;
}else{
if(objId.isSnippetNode()){
_1223=ps.aa.SNIPPET_CLASS;
}else{
if(objId.isSlotNode()){
_1223=ps.aa.SLOT_CLASS;
}else{
if(objId.isFieldNode()){
_1223=ps.aa.FIELD_CLASS;
}
}
}
}
var _1224=ps.aa.ObjectId.ImageNames[_1223]+objId.getCheckoutStatus();
var _1225=_1224.substring(0,1);
var rest=_1224.substring(1,_1224.length);
var klass=_1225.toUpperCase()+rest;
return klass;
}});
dojo.provide("ps.widget.TreeSelector");
dojo.widget.defineWidget("ps.widget.TreeSelector",dojo.widget.TreeSelectorV3,function(){
dojo.event.connect(this,"processNode",this,"_nodeActivated");
},{_nodeActivated:function(node){
dojo.lang.assert(node);
var objId=node.modelId;
dojo.lang.assert(objId,"widget does not have a model id");
ps.aa.controller.activate(objId);
}});
dojo.provide("dojo.io.ScriptSrcIO");
dojo.io.ScriptSrcTransport=new function(){
this.preventCache=false;
this.maxUrlLength=1000;
this.inFlightTimer=null;
this.DsrStatusCodes={Continue:100,Ok:200,Error:500};
this.startWatchingInFlight=function(){
if(!this.inFlightTimer){
this.inFlightTimer=setInterval("dojo.io.ScriptSrcTransport.watchInFlight();",100);
}
};
this.watchInFlight=function(){
var _122a=0;
var _122b=0;
for(var param in this._state){
_122a++;
var _122d=this._state[param];
if(_122d.isDone){
_122b++;
delete this._state[param];
}else{
if(!_122d.isFinishing){
var _122e=_122d.kwArgs;
try{
if(_122d.checkString&&eval("typeof("+_122d.checkString+") != 'undefined'")){
_122d.isFinishing=true;
this._finish(_122d,"load");
_122b++;
delete this._state[param];
}else{
if(_122e.timeoutSeconds&&_122e.timeout){
if(_122d.startTime+(_122e.timeoutSeconds*1000)<(new Date()).getTime()){
_122d.isFinishing=true;
this._finish(_122d,"timeout");
_122b++;
delete this._state[param];
}
}else{
if(!_122e.timeoutSeconds){
_122b++;
}
}
}
}
catch(e){
_122d.isFinishing=true;
this._finish(_122d,"error",{status:this.DsrStatusCodes.Error,response:e});
}
}
}
}
if(_122b>=_122a){
clearInterval(this.inFlightTimer);
this.inFlightTimer=null;
}
};
this.canHandle=function(_122f){
return dojo.lang.inArray(["text/javascript","text/json","application/json"],(_122f["mimetype"].toLowerCase()))&&(_122f["method"].toLowerCase()=="get")&&!(_122f["formNode"]&&dojo.io.formHasFile(_122f["formNode"]))&&(!_122f["sync"]||_122f["sync"]==false)&&!_122f["file"]&&!_122f["multipart"];
};
this.removeScripts=function(){
var _1230=document.getElementsByTagName("script");
for(var i=0;_1230&&i<_1230.length;i++){
var _1232=_1230[i];
if(_1232.className=="ScriptSrcTransport"){
var _1233=_1232.parentNode;
_1233.removeChild(_1232);
i--;
}
}
};
this.bind=function(_1234){
var url=_1234.url;
var query="";
if(_1234["formNode"]){
var ta=_1234.formNode.getAttribute("action");
if((ta)&&(!_1234["url"])){
url=ta;
}
var tp=_1234.formNode.getAttribute("method");
if((tp)&&(!_1234["method"])){
_1234.method=tp;
}
query+=dojo.io.encodeForm(_1234.formNode,_1234.encoding,_1234["formFilter"]);
}
if(url.indexOf("#")>-1){
dojo.debug("Warning: dojo.io.bind: stripping hash values from url:",url);
url=url.split("#")[0];
}
var _1239=url.split("?");
if(_1239&&_1239.length==2){
url=_1239[0];
query+=(query?"&":"")+_1239[1];
}
if(_1234["backButton"]||_1234["back"]||_1234["changeUrl"]){
dojo.undo.browser.addToHistory(_1234);
}
var id=_1234["apiId"]?_1234["apiId"]:"id"+this._counter++;
var _123b=_1234["content"];
var _123c=_1234.jsonParamName;
if(_1234.sendTransport||_123c){
if(!_123b){
_123b={};
}
if(_1234.sendTransport){
_123b["dojo.transport"]="scriptsrc";
}
if(_123c){
_123b[_123c]="dojo.io.ScriptSrcTransport._state."+id+".jsonpCall";
}
}
if(_1234.postContent){
query=_1234.postContent;
}else{
if(_123b){
query+=((query)?"&":"")+dojo.io.argsFromMap(_123b,_1234.encoding,_123c);
}
}
if(_1234["apiId"]){
_1234["useRequestId"]=true;
}
var state={"id":id,"idParam":"_dsrid="+id,"url":url,"query":query,"kwArgs":_1234,"startTime":(new Date()).getTime(),"isFinishing":false};
if(!url){
this._finish(state,"error",{status:this.DsrStatusCodes.Error,statusText:"url.none"});
return;
}
if(_123b&&_123b[_123c]){
state.jsonp=_123b[_123c];
state.jsonpCall=function(data){
if(data["Error"]||data["error"]){
if(dojo["json"]&&dojo["json"]["serialize"]){
dojo.debug(dojo.json.serialize(data));
}
dojo.io.ScriptSrcTransport._finish(this,"error",data);
}else{
dojo.io.ScriptSrcTransport._finish(this,"load",data);
}
};
}
if(_1234["useRequestId"]||_1234["checkString"]||state["jsonp"]){
this._state[id]=state;
}
if(_1234["checkString"]){
state.checkString=_1234["checkString"];
}
state.constantParams=(_1234["constantParams"]==null?"":_1234["constantParams"]);
if(_1234["preventCache"]||(this.preventCache==true&&_1234["preventCache"]!=false)){
state.nocacheParam="dojo.preventCache="+new Date().valueOf();
}else{
state.nocacheParam="";
}
var _123f=state.url.length+state.query.length+state.constantParams.length+state.nocacheParam.length+this._extraPaddingLength;
if(_1234["useRequestId"]){
_123f+=state.idParam.length;
}
if(!_1234["checkString"]&&_1234["useRequestId"]&&!state["jsonp"]&&!_1234["forceSingleRequest"]&&_123f>this.maxUrlLength){
if(url>this.maxUrlLength){
this._finish(state,"error",{status:this.DsrStatusCodes.Error,statusText:"url.tooBig"});
return;
}else{
this._multiAttach(state,1);
}
}else{
var _1240=[state.constantParams,state.nocacheParam,state.query];
if(_1234["useRequestId"]&&!state["jsonp"]){
_1240.unshift(state.idParam);
}
var _1241=this._buildUrl(state.url,_1240);
state.finalUrl=_1241;
this._attach(state.id,_1241);
}
this.startWatchingInFlight();
};
this._counter=1;
this._state={};
this._extraPaddingLength=16;
this._buildUrl=function(url,_1243){
var _1244=url;
var _1245="?";
for(var i=0;i<_1243.length;i++){
if(_1243[i]){
_1244+=_1245+_1243[i];
_1245="&";
}
}
return _1244;
};
this._attach=function(id,url){
var _1249=document.createElement("script");
_1249.type="text/javascript";
_1249.src=url;
_1249.id=id;
_1249.className="ScriptSrcTransport";
document.getElementsByTagName("head")[0].appendChild(_1249);
};
this._multiAttach=function(state,part){
if(state.query==null){
this._finish(state,"error",{status:this.DsrStatusCodes.Error,statusText:"query.null"});
return;
}
if(!state.constantParams){
state.constantParams="";
}
var _124c=this.maxUrlLength-state.idParam.length-state.constantParams.length-state.url.length-state.nocacheParam.length-this._extraPaddingLength;
var _124d=state.query.length<_124c;
var _124e;
if(_124d){
_124e=state.query;
state.query=null;
}else{
var _124f=state.query.lastIndexOf("&",_124c-1);
var eqEnd=state.query.lastIndexOf("=",_124c-1);
if(_124f>eqEnd||eqEnd==_124c-1){
_124e=state.query.substring(0,_124f);
state.query=state.query.substring(_124f+1,state.query.length);
}else{
_124e=state.query.substring(0,_124c);
var _1251=_124e.substring((_124f==-1?0:_124f+1),eqEnd);
state.query=_1251+"="+state.query.substring(_124c,state.query.length);
}
}
var _1252=[_124e,state.idParam,state.constantParams,state.nocacheParam];
if(!_124d){
_1252.push("_part="+part);
}
var url=this._buildUrl(state.url,_1252);
this._attach(state.id+"_"+part,url);
};
this._finish=function(state,_1255,event){
if(_1255!="partOk"&&!state.kwArgs[_1255]&&!state.kwArgs["handle"]){
if(_1255=="error"){
state.isDone=true;
throw event;
}
}else{
switch(_1255){
case "load":
var _1257=event?event.response:null;
if(!_1257){
_1257=event;
}
state.kwArgs[(typeof state.kwArgs.load=="function")?"load":"handle"]("load",_1257,event,state.kwArgs);
state.isDone=true;
break;
case "partOk":
var part=parseInt(event.response.part,10)+1;
if(event.response.constantParams){
state.constantParams=event.response.constantParams;
}
this._multiAttach(state,part);
state.isDone=false;
break;
case "error":
state.kwArgs[(typeof state.kwArgs.error=="function")?"error":"handle"]("error",event.response,event,state.kwArgs);
state.isDone=true;
break;
default:
state.kwArgs[(typeof state.kwArgs[_1255]=="function")?_1255:"handle"](_1255,event,event,state.kwArgs);
state.isDone=true;
}
}
};
dojo.io.transports.addTransport("ScriptSrcTransport");
};
window.onscriptload=function(event){
var state=null;
var _125b=dojo.io.ScriptSrcTransport;
if(_125b._state[event.id]){
state=_125b._state[event.id];
}else{
var _125c;
for(var param in _125b._state){
_125c=_125b._state[param];
if(_125c.finalUrl&&_125c.finalUrl==event.id){
state=_125c;
break;
}
}
if(state==null){
var _125e=document.getElementsByTagName("script");
for(var i=0;_125e&&i<_125e.length;i++){
var _1260=_125e[i];
if(_1260.getAttribute("class")=="ScriptSrcTransport"&&_1260.src==event.id){
state=_125b._state[_1260.id];
break;
}
}
}
if(state==null){
throw "No matching state for onscriptload event.id: "+event.id;
}
}
var _1261="error";
switch(event.status){
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Continue:
_1261="partOk";
break;
case dojo.io.ScriptSrcTransport.DsrStatusCodes.Ok:
_1261="load";
break;
}
_125b._finish(state,_1261,event);
};
dojo.provide("sos.widget.ImageGallery");
dojo.declare("sos.widget.ImageGallery",dojo.widget.HtmlWidget,{ns:"sos",widgetType:"ImageGallery",imageStore:[],imageHeight:375,imageWidth:500,defaultTitle:"No Title",images:[],thumbs:[],thumbCounter:0,imageCounter:0,numberThumbs:5,thumbIndex:0,dataFn:"",slideshowInterval:3,twoConnectionsUsedForMain:false,templateString:"<div dojoAttachPoint='outerNode' class='image-gallery-wrapper'>"+"<div dojoAttachPoint='thumbsNode' class='image-gallery-thumbs-wrapper'>"+"<table><tr dojoAttachPoint='thumbsTableRow'></tr></table></div>"+"<div dojoAttachPoint='largeNode' class='image-gallery-image-wrapper'></div>"+"<div dojoAttachPoint='slideshowNode' class='image-gallery-slideshow'><a href='#'>Start Slideshow</a></div>"+"<div dojoAttachPoint='hiddenNode' class='image-gallery-hidden'></div>"+"</div>",templateCssPath:dojo.uri.moduleUri("sos","widget/templates/ImageGallery.css"),tempImgPath:dojo.uri.moduleUri("sos","widget/templates/images/1pixel.gif"),tmpImage:null,postCreate:function(){
var img=document.createElement("img");
img.setAttribute("width",this.imageWidth);
img.setAttribute("height",this.imageHeight);
img.setAttribute("src",this.tempImgPath);
var _1263=this;
dojo.event.browser.addListener(this.slideshowNode.firstChild,"onclick",function(evt){
_1263.toggleSlideshow();
dojo.event.browser.stopEvent(evt);
return false;
});
this.largeNode.appendChild(img);
this.tmpImage=img;
if(this.dataFn!=""&&typeof (dj_global[this.dataFn])=="function"){
var data=dj_global[this.dataFn]();
if(data){
this.setData(data);
}
}else{
this.init();
}
},init:function(){
if(this.isInitialized||this.imageStore.length<1){
return;
}
if(!this.thumbCells){
dojo.html.setStyle(this.hiddenNode,"position:absolute: left:-10000px;");
this.thumbCells=[];
this.thumbIndex=0;
this.numberThumbs=Number(this.numberThumbs);
for(var i=0;i<this.numberThumbs+2;i++){
var cell=document.createElement("td");
cell.setAttribute("id","img_cell_"+i);
cell.setAttribute("align","center");
this.thumbCells[this.thumbCells.length]=cell;
if(i>0&&i<this.numberThumbs+1){
dojo.html.setClass(cell,"image-thumb-placeholder");
}else{
dojo.html.addClass(cell,"image-thumb-nav");
if(i==0){
dojo.html.addClass(cell,"image-thumb-prev");
}else{
dojo.html.addClass(cell,"image-thumb-next");
}
}
cell.innerHTML="<img src='"+this.tempImgPath+"' class='image-gallery-thumb'/>";
this.thumbsTableRow.appendChild(cell);
}
var _1268=this;
dojo.event.browser.addListener(this.thumbCells[0],"onclick",function(evt){
_1268.showThumbs(_1268.thumbIndex-_1268.numberThumbs);
});
dojo.event.browser.addListener(this.thumbCells[this.numberThumbs+1],"onclick",function(evt){
_1268.showThumbs(_1268.thumbIndex+_1268.numberThumbs);
});
var width=100*this.numberThumbs+90;
dojo.html.insertCssText(".image-gallery-wrapper{text-align:center;width:"+width+"px;}");
}
this.isInitialized=true;
this.updateNavControls();
this.loadNextThumbnail();
this.showImage(0);
},setData:function(data){
this.imageStore=data;
this.init();
},reset:function(){
this.imageStore=[];
while(this.largeNode.firstChild){
this.largeNode.removeChild(this.largeNode.firstChild);
}
this.largeNode.appendChild(this.tmpImage);
while(this.hiddenNode.firstChild){
this.hiddenNode.removeChild(this.hiddenNode.firstChild);
}
var img;
for(var pos=0;pos<this.images.length;pos++){
img=this.images[pos];
if(img){
dojo.event.browser.clean(img);
if(img.parentNode){
img.parentNode.removeChild(img);
}
}
}
for(var pos=0;pos<this.thumbs.length;pos++){
img=this.thumbs[pos];
if(img){
dojo.event.browser.clean(img);
if(img.parentNode){
img.parentNode.removeChild(img);
}
}
}
this.images=[];
this.thumbs=[];
this.isInitialized=false;
this.thumbCounter=0;
this.twoConnectionsUsedForMain=false;
this.imageCounter=0;
this.thumbIndex=0;
},showThumbs:function(idx){
var _1270=this;
var idx=arguments.length==0?this.thumbIndex:arguments[0];
idx=Math.max(idx,0);
idx=Math.min(idx,this.imageStore.length);
if(idx==this.imageStore.length){
return;
}
var imgId;
var _1272=[];
var _1273=[];
for(var i=0;i<this.numberThumbs;i++){
imgId="img_"+(i+idx);
if(this.thumbCells[i+1].firstChild){
if(this.thumbCells[i+1].firstChild.getAttribute("id")==imgId){
continue;
}
_1272[_1272.length]=this.thumbCells[i+1].firstChild;
}
_1273[i]=imgId;
}
var page=this.page;
var _1276=function(){
if(page!=_1270.page){
return;
}
for(var i=0;i<_1270.numberThumbs;i++){
if(_1273[i]){
while(_1270.thumbCells[i+1].firstChild){
_1270.hiddenNode.appendChild(_1270.thumbCells[i+1].firstChild);
}
var node=dojo.byId(_1273[i]);
if(node){
if(node.parentNode!=_1270.thumbCells[i+1]){
_1270.thumbCells[i+1].appendChild(node);
}
if(dojo.html.getOpacity(node)!=100){
dojo.html.setOpacity(node,100);
}
}else{
var _1279=_1273[i].split("_")[1];
dojo.debug("No thumb '"+_1273[i]+"' loading from "+(_1279));
_1270.loadNextThumbnail(_1273[i].split("_")[1]);
}
}
}
_1270.thumbIndex=idx;
_1270.updateNavControls();
};
if(_1272.length>0){
dojo.lfx.html.fadeOut(_1272,300,null,_1276).play();
}else{
_1276();
}
},updateNavControls:function(){
var _127a=this.thumbsTableRow.cells[0];
if(this.thumbIndex<1){
dojo.html.removeClass(_127a,"image-thumb-prev");
}else{
if(!dojo.html.hasClass(_127a,"image-thumb-prev")){
dojo.html.addClass(_127a,"image-thumb-prev");
}
}
var _127b=this.thumbsTableRow.cells[this.thumbsTableRow.cells.length-1];
if(this.thumbIndex+this.numberThumbs>=this.imageStore.length){
dojo.html.removeClass(_127b,"image-thumb-next");
}else{
if(!dojo.html.hasClass(_127b,"image-thumb-next")){
dojo.html.addClass(_127b,"image-thumb-next");
}
}
},loadNextThumbnail:function(){
var _127c=arguments.length==0?-1:arguments[0];
var pos=arguments.length==0?this.thumbCounter++:arguments[0];
while(pos<this.thumbs.length&&this.thumbs[pos]){
pos++;
}
if(this.thumbCounter>=this.imageStore.length){
if(this.imageStore.length>0){
if(!this.twoConnectionsUsedForMain){
this["twoConnectionsUsedForMain"]=true;
this.loadNextImage();
}
}
}
if(pos>=this.imageStore.length){
return;
}
var url=this.imageStore[pos]["thumb"];
var img=document.createElement("img");
var _1280=document.createElement("div");
_1280.setAttribute("id","img_"+pos);
_1280.appendChild(img);
this.thumbs[pos]=_1280;
var _1281=document.createElement("div");
_1281.innerHTML="<!-- -->";
_1281.setAttribute("id","loadingDiv_"+pos);
dojo.html.setClass(_1281,"image-gallery-notifier");
if(this.images.length>pos&&this.images[pos]){
dojo.html.addClass(_1281,"image-gallery-loaded");
}
_1280.appendChild(_1281);
this.hiddenNode.appendChild(_1280);
var _1282=this;
var page=this.page;
dojo.event.browser.addListener(img,"onload",function(){
if(page!=_1282.page){
return;
}
if(pos>=_1282.thumbIndex&&pos<_1282.thumbIndex+_1282.numberThumbs){
_1282.showThumbs();
}
_1282.loadNextThumbnail();
return false;
});
dojo.event.browser.addListener(img,"onclick",function(){
if(page!=_1282.page){
return;
}
_1282.showImage(pos);
return false;
});
dojo.html.setClass(img,"image-gallery-thumb");
img.setAttribute("src",url);
if(this.imageStore[pos]["title"]){
img.setAttribute("title",this.imageStore[pos]["title"]);
}
this.showThumbs();
},loadNextImage:function(){
while(this.images.length>=this.imageCounter&&this.images[this.imageCounter]){
this.imageCounter++;
}
this.loadImage(this.imageCounter);
},loadImage:function(pos,_1285){
if(this.images[pos]||pos>=this.imageStore.length){
return;
}
var _1286=dojo.byId("loadingDiv_"+pos);
if(_1286&&!dojo.html.hasClass(_1286,"image-gallery-loading")){
dojo.html.addClass(_1286,"image-gallery-loading");
}
var url=this.imageStore[pos]["large"];
var img=document.createElement("img");
var div=document.createElement("div");
if(!this.imageStore[pos]["link"]){
div.appendChild(img);
}else{
var a=document.createElement("a");
a.setAttribute("href",this.imageStore[pos]["link"]);
a.setAttribute("target","_blank");
div.appendChild(a);
a.appendChild(img);
}
div.setAttribute("id",this.widgetId+"_imageDiv"+pos);
div.setAttribute("width",this.imageWidth);
div.setAttribute("height",this.imageHeight);
img.setAttribute("width",this.imageWidth);
img.setAttribute("height",this.imageHeight);
var _128b=this;
var page=this.page;
dojo.event.browser.addListener(img,"onload",function(){
if(page!=_128b.page){
return;
}
var divId="loadingDiv_"+pos;
var _128e=dojo.byId(divId);
if(_128e){
dojo.html.addClass(_128e,"image-gallery-loaded");
}
_128b.loadNextImage();
});
if(_1285){
dojo.event.browser.addListener(img,"onload",_1285);
}
this.hiddenNode.appendChild(div);
var _128f=document.createElement("div");
dojo.html.addClass(_128f,"image-gallery-title");
div.appendChild(_128f);
this.images[pos]=div;
img.setAttribute("src",url);
var title=this.imageStore[pos]["title"];
if(title){
img.setAttribute("title",title);
_128f.innerHTML=title;
}else{
_128f.innerHTML=this.defaultTitle;
}
},destroy:function(){
if(this._slideId){
this.cancelSlideshow();
}
},showNextImage:function(_1291){
if(Number(this.imageIndex)+1>=this.imageStore.length){
if(this._slideId){
this.cancelSlideshow();
}
return false;
}
var _1292=this;
this.showImage(Number(this.imageIndex+1),function(){
if(_1291){
_1292.startTimer();
}
});
return true;
},toggleSlideshow:function(){
if(this._slideId){
this.cancelSlideshow();
}else{
this.slideshowNode.firstChild.innerHTML="Stop Slideshow";
var _1293=this.showNextImage(true);
if(!_1293){
this.cancelSlideshow();
}
}
},cancelSlideshow:function(){
if(this._slideId){
clearTimeout(this._slideId);
}
this._slideId=null;
this.slideshowNode.firstChild.innerHTML="Start Slideshow";
},startTimer:function(){
this._slideId=setTimeout("dojo.widget.byId('"+this.widgetId+"').showNextImage(true);",this.slideshowInterval*1000);
},showImage:function(index,_1295){
if(!_1295&&this._slideId){
this.toggleSlideshow();
}
var _1296=this;
var _1297=this.largeNode.getElementsByTagName("div");
this.imageIndex=Number(index);
var _1298=function(){
if(_1296.images[index]){
while(_1296.largeNode.firstChild){
_1296.largeNode.removeChild(_1296.largeNode.firstChild);
}
_1296.images[index].style.opacity=0;
_1296.largeNode.appendChild(_1296.images[index]);
dojo.lfx.html.fadeIn(_1296.images[index],300,null,_1295).play();
}else{
_1296.loadImage(index,function(){
_1296.showImage(index);
});
}
};
var _1299=dojo.byId("loadingDiv_"+index);
if(_1299&&!dojo.html.hasClass(_1299,"image-gallery-loading")){
dojo.html.addClass(_1299,"image-gallery-loading");
}
if(_1297&&_1297.length>0){
dojo.lfx.html.fadeOut(_1297[0],300,null,function(){
_1296.hiddenNode.appendChild(_1297[0]);
_1298();
}).play();
}else{
_1298();
}
}});
dojo.provide("sos.widget.FlickrImageGallery");
dojo.declare("sos.widget.FlickrImageGallery",sos.widget.ImageGallery,{widgetType:"FlickrImageGallery",flickrUserId:"",flickrApiKey:"",flickrSetId:"",page:1,perPage:50,numPageLinks:8,useSetMenus:false,postCreate:function(){
sos.widget.ImageGallery.prototype.postCreate.call(this);
if(this.flickrUserId!=""&&this.flickrApiKey!=""){
this.loadFlickrPublicPhotos(this.flickrUserId,this.flickrApiKey);
}
this.pagingDiv=document.createElement("div");
this.outerNode.insertBefore(this.pagingDiv,this.slideshowNode);
dojo.html.setClass(this.pagingDiv,"image-gallery-pager");
if(!this.maxPages){
dojo.html.hide(this.pagingDiv);
}
if(this.useSetMenus!=false){
this.initSetMenus();
}
},generatePagingLinks:function(){
if(!this.maxPages){
return;
}
while(this.pagingDiv.firstChild){
this.pagingDiv.firstChild.parentNode.removeChild(this.pagingDiv.firstChild);
}
var _129a=this.numPageLinks-1;
var first=Math.max(1,Math.min(this.page,this.maxPages));
var diff=first%this.numPageLinks;
first=first-(diff==0?this.numPageLinks:diff)+1;
var html=["<ul id='"+this.widgetId+"_pageLinks'>","<li>Pages: </li>"];
var _129e="<li><a href='#' onclick='dojo.widget.byId(\""+this.widgetId+"\").setPage(";
var _129f="</a></li>";
if(first>1){
var _12a0=first-1;
html[html.length]=_129e+_12a0+"); return false;' "+"title='Page"+_12a0+"'"+">&lt;&lt;"+_129f;
}
var _12a1;
for(var i=first;i<=this.maxPages&&i<=first+_129a;i++){
html[html.length]=_129e+i+"); return false;' "+"title='Page "+i+"'"+((i==this.page)?" class='current-page'":"")+">"+i+_129f;
}
if(this.page+_129a<=this.maxPages){
var _12a0=Math.min(first+_129a+1,this.maxPages);
html[html.length]=_129e+_12a0+");  return false;' "+" title='Page "+_12a0+"'"+">&gt;&gt;"+_129f;
}
html[html.length]="</ul>";
this.pagingDiv.innerHTML=html.join("\n");
if(this._setMenu){
this.createSetButton();
}
},createSetButton:function(){
var _12a3=dojo.byId(this.widgetId+"_pageLinks");
if(!_12a3){
return;
}
var li=document.createElement("li");
li.innerHTML="<a href='#' id='"+this.widgetId+"_menuLink'>Sets</a>";
_12a3.appendChild(li);
var _12a5=this;
dojo.event.browser.addListener(li.firstChild,"onclick",function(e){
_12a5._setMenu.onOpen(e);
return false;
});
},addFlickrData:function(_12a7){
if(_12a7["stat"]!="ok"){
alert("Flickr data is not valid, stat = "+_12a7["stat"]);
return;
}
var _12a8="photos";
if(_12a7["photos"]){
_12a8="photos";
}else{
if(_12a7["photoset"]){
_12a8="photoset";
}else{
if(_12a7["photosets"]){
this.initSetMenus(_12a7);
return;
}
}
}
var _12a9=this.maxPages;
if(_12a7[_12a8]&&_12a7[_12a8]["pages"]){
this.maxPages=Number(_12a7[_12a8]["pages"]);
}
this.generatePagingLinks();
this.reset();
var _12aa=_12a7[_12a8]["owner"];
if(_12a7[_12a8]&&_12a7[_12a8]["photo"]){
_12a7=_12a7[_12a8]["photo"];
}
var _12ab=[];
for(var i=0;i<_12a7.length;i++){
var owner=_12a7[i]["owner"]||_12aa;
var _12ae="http://farm"+_12a7[i]["farm"]+".static.flickr.com/"+_12a7[i]["server"]+"/"+_12a7[i]["id"]+"_"+_12a7[i]["secret"];
var title=_12a7[i]["title"];
var link="http://www.flickr.com/photos/"+owner+"/"+_12a7[i]["id"];
_12ab[_12ab.length]={"thumb":_12ae+"_t.jpg","large":_12ae+".jpg","title":title,"link":link};
}
this.setData(_12ab);
if(!this.maxPages){
dojo.html.hide(this.pagingDiv);
}else{
dojo.html.show(this.pagingDiv);
}
this.init();
},loadFlickrPublicPhotos:function(_12b1,_12b2,setId,page,_12b5){
var _12b6="flickr.people.getPublicPhotos";
if(arguments.length==0){
if(this.flickrApiKey==""||this.flickrUserId==""){
dojo.debug("FlickrImageGallery: no userId or apiKey specified. Cannot load Flickr images.");
return;
}
}
if(!_12b1){
_12b1=this.flickrUserId;
}
if(!_12b2){
_12b2=this.flickrApiKey;
}
if(!page){
page=this.page;
}
if(!_12b5){
_12b5=this.perPage;
}
if(!setId){
var setId=this.flickrSetId;
}
var _12b7="";
if(setId&&setId!=""){
_12b6="flickr.photosets.getPhotos";
_12b7+="&photoset_id="+setId;
}
var _12b8=this;
dj_global["jsonFlickrApi"]=function(data){
_12b8.addFlickrData(data);
};
var url="http://www.flickr.com/services/rest/?method="+_12b6+"&format=json&api_key="+_12b2+"&user_id="+_12b1+"&page="+page+"&per_page="+_12b5+_12b7;
dojo.io.bind({url:url,transport:"ScriptSrcTransport",mimetype:"application/json"});
},initSetMenus:function(data){
if(arguments.length==0){
url="http://www.flickr.com/services/rest/?method=flickr.photosets.getList&format=json&api_key="+this.flickrApiKey+"&user_id="+this.flickrUserId;
var _12bc=this;
dj_global["jsonFlickrApi"]=function(data){
_12bc.addFlickrData(data);
};
dojo.io.bind({url:url,transport:"ScriptSrcTransport",mimetype:"application/json"});
return;
}
if(data["stat"]!="ok"||!data["photosets"]){
alert("Error: Invalid Flickr Set data.");
return;
}
data=data["photosets"]["photoset"];
var menu=dojo.widget.byId(this.widgetId+"_menu");
if(menu){
menu.destroy();
}
var _12bc=this;
function makeMenu(items,isTop,id){
var menu2=dojo.widget.createWidget("PopupMenu2",{contextMenuForWindow:isTop,widgetId:id});
menu2.addChild(dojo.widget.createWidget("MenuItem2",{caption:"All Photos",onClick:function(){
_12bc.flickrSetId=null;
_12bc.loadFlickrPublicPhotos();
}}));
menu2.addChild(dojo.widget.createWidget("MenuSeparator2"));
dojo.lang.forEach(items,function(_12c3){
var item=dojo.widget.createWidget("MenuItem2",_12c3);
menu2.addChild(item);
});
return menu2;
}
var _12c5=[];
function genFn(_12c6){
return function(){
_12bc.loadFlickrPublicPhotos(null,null,_12c6);
return true;
};
}
for(var count=0;count<data.length;count++){
var setId=data[count].id;
_12c5[_12c5.length]={caption:data[count]["title"]["_content"],onClick:genFn(setId)};
}
this._setMenu=makeMenu(_12c5,false,this.widgetId+"_menu");
this.createSetButton();
},nextPage:function(){
this.setPage(this.page+1);
},prevPage:function(){
this.setPage(this.page-1);
},setPage:function(num){
if(num<0||(this.maxPages&&num>this.maxPages)||num==this.page){
return;
}
this.page=num;
this.loadFlickrPublicPhotos();
}});
if(typeof (imageGalleryLoaded)=="function"){
imageGalleryLoaded();
}
dojo.provide("dojo.style");
dojo.kwCompoundRequire({browser:["dojo.html.style"]});
dojo.deprecated("dojo.style","replaced by dojo.html.style","0.5");
dojo.lang.mixin(dojo.style,dojo.html);
dojo.provide("ps.widget.PSImageGallery");
dojo.widget.defineWidget("ps.widget.PSImageGallery",sos.widget.ImageGallery,{templateString:"<div dojoAttachPoint='outerNode' class='image-gallery-wrapper'>"+"<div dojoAttachPoint='largeNode' class='image-gallery-image-wrapper'></div>"+"<div dojoAttachPoint='thumbsNode' class='image-gallery-thumbs-wrapper'>"+"<table><tr dojoAttachPoint='thumbsTableRow'></tr></table></div>"+"<div dojoAttachPoint='slideshowNode' class='image-gallery-slideshow'><a href='#'>Start Slideshow</a></div>"+"<div dojoAttachPoint='hiddenNode' class='image-gallery-hidden'></div>"+"</div>",numberThumbs:4,imageHeight:300,imageWidth:400,postCreate:function(){
sos.widget.ImageGallery.prototype.postCreate.call(this);
dojo.style.hide(this.slideshowNode);
},setImages:function(_12ca,_12cb,_12cc){
dojo.lang.assertType(_12ca,Array);
dojo.lang.assertType(_12cb,Array);
if(_12cc){
dojo.lang.assertType(_12cc,Array);
}
var _12cd=[];
for(var i=0;i<_12ca.length;i++){
_12cd[_12cd.length]={"thumb":_12ca[i],"large":_12cb[i],"title":_12cc[i]};
}
this.setData(_12cd);
}});
dojo.kwCompoundRequire({common:["ps.widget.Autoscroller","ps.widget.ContentPaneProgress","ps.widget.MenuBar2","ps.widget.MenuBarIcon","ps.widget.MenuBarItem2","ps.widget.MenuBarItemDropDown","ps.widget.PopupMenu","ps.widget.PSSplitContainer","ps.widget.ScrollableNodes","ps.widget.Tree","ps.widget.TreeDndController","ps.widget.TreeIcon","ps.widget.TreeSelector","ps.widget.PSImageGallery"]});
dojo.provide("ps.widget.*");

