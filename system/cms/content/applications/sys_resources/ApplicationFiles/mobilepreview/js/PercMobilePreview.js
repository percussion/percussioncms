document.addEventListener("DOMContentLoaded", function(event) {
    (function($){
        $(document).ready(function(){
            var prurl = window.location.href;
            var baseurl = window.location.protocol + "//" + window.location.host;
            if(prurl.indexOf("percmobilepreview=true") > 0)
            {
                prurl = prurl.replace("percmobilepreview=true","");
                javascript:void((function(){var d=document;d.write('<!DOCTYPE html><html><head><meta charset="UTF-8"><title>'+d.title+'</title><link rel="stylesheet" href="' + baseurl + '/cm/css/FontAwesome/css/font-awesome.min.css" rel="stylesheet"><link rel="stylesheet" href="' + baseurl + '/Rhythmyx/sys_resources/mobilepreview/css/PercMobileApp.css"><script src="' + baseurl + '/Rhythmyx/sys_resources/js/href.js"></script><script src="' + baseurl + '/Rhythmyx/sys_resources/mobilepreview/js/PercViewport.js"></script></head><body onload=""><header><div class="percclose" title="Collapse Toolbar"><a href="#"><i id="perccollapse" class="icon-double-angle-down fas fa-angle-double-down fa-2x"></i></a></div><div id="percsize"></div><div id="percdevices"><a href="#" id="largetabletportrait" class="perc-large-tablet-portrait" title="Large Tablet(Portrait)"><span><i class="icon-tablet icon-large"></i></span></a><a href="#" id="largetabletlandscape" class="perc-large-tablet-landscape" title="Large Tablet(Landscape)"><span><i class="icon-tablet icon-large"></i></span></a><a href="#" id="smalltabletportrait" class="perc-small-tablet-portrait" title="Small Tablet(Portrait)"><span><i class="icon-tablet icon-2x"></i></span></a><a href="#" id="smalltabletlandscape" class="perc-small-tablet-landscape" title="Small Tablet(Landscape)"><span><i class="icon-tablet icon-2x"></i></span></a><a href="#" id="smartphoneportrait" class="perc-smartphone-portrait" title="Mobile Phone(Portrait)"><span><i class="icon-mobile-phone icon-2x"></i></span></a><a href="#" id="smartphonelandscape" class="perc-smartphone-landscape" title="Mobile Phone(Landscape)"><span><i class="icon-mobile-phone icon-2x"></i></span></a><a href="#" id="auto" class="perc-auto active"><span>Auto</span></a></div></header><section><div id="percwrapper"><iframe id="percmobilepreviewframe" name="percmobilepreviewframe" onload="javascript:window:mobilePreviewFrameOnload();" src="'+prurl+'"></iframe></div></section></body></html>');d.close();})());
            }
        });
    })(jQuery);
});
