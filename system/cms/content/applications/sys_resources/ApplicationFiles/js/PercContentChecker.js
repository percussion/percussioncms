//These arrays are populated by push functions in sys_Templates.xsl. Only populated by text areas that are detected to have critical classes(classes present in CM1) and all tinyMCE controls
var PercHtmlFieldsContentCheckerArray = [];
var PercTinymceFieldsContentCheckerArray = [];

//Adds a handler that fires when the user submits/saves the text area. 
$(document).ready(function ($){
	if (window.location !== window.parent.location) {
		window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(reservedClassesWarning);
	}
});

//Method to loop through both arrays and clean each item.
function reservedClassesWarning(){
    for(i=0;i<PercHtmlFieldsContentCheckerArray.length; i++){
	    if(htmlFieldChecker(PercHtmlFieldsContentCheckerArray[i])){
            showWarningMessage();
            return false;
        }
	}
	
	for(j=0;j<PercTinymceFieldsContentCheckerArray.length; j++){
	    if(tinyMceFieldChecker(PercTinymceFieldsContentCheckerArray[j])){
            showWarningMessage();
            return false;
        }
	}
	return true;
}
function showWarningMessage(){
  window.parent.jQuery.perc_utils.alert_dialog({
      title: 'Warning', 
      content: 'This content contains a reserved class name ("perc-widget", "perc-region", "perc-vertical", "perc-fixed", "perc-region-leaf", "perc-horizontal", "perc-itool-selectable-elem", "perc-itool-region-elem", "perc-zero-size-elem").  Please review your HTML and remove these references before submitting.', 
      okCallBack: function(){
          return true;
      }
  });
    
}
//Method for accessing and replacing content from a tinyMCE instance
function tinyMceFieldChecker(currentfield){
 var tinyMCEcontents = tinyMCE.get(currentfield).getContent();
 return hasPercClass(tinyMCEcontents);
}

//Method for accessing and replacing content from a textarea instance
function htmlFieldChecker(currentfield){
     var HTMLcontents = document.getElementsByName(currentfield)[0].value;
     return hasPercClass(HTMLcontents);
}

//Method for stripping critical classes from the given html string
function hasPercClass(htmlString){
    return false;
}
