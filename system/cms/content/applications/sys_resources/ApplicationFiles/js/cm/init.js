 jQuery(function($){
     
     //Remove white space (if any) from end of Name value    
    $("#perc-content-edit-sys_title, #perc-content-edit-displaytitle").on("change", function() {
        var updateTitleValue = $(this).val();
        updateTitleValue = updateTitleValue.trim();
        $(this).val(updateTitleValue);
    });
}); 

function findTopMostJQuery() {
    // look for the top most jQuery instance
    // start at the enclosing window
    var parent = window.parent;

    // iterate over the parent windows
    // looking for a window that has an iframe named "frame"
    //while(parent != undefined &amp;&amp; parent.frame == undefined)
    while(parent != undefined && parent.frame == undefined)
        parent = parent.parent;

    // if we still have a valid window
    // and that window has a frame named "frame"
    //if(parent != undefined &amp;&amp; parent.frame != undefined)
    if(parent != undefined && parent.frame != undefined)
        jQuery.topFrameJQuery = parent.jQuery;
}

function addKeyPressDirtyEvents() {
    jQuery("input").on("keypress",function(){
        jQuery.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
    });
    jQuery("textarea").on("keypress",function(){
        jQuery.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
    });
    jQuery("select").on("keypress",function(){
        jQuery.topFrameJQuery.PercDirtyController.setDirty(true, "asset");
    });
}

/**
 * @param jqRef need to pass in the jQuery reference, see CML-2889.
 */
function fixIE(jqRef){
   var pTop = "3px";
   var pBot = "5px";
   var pHeight = "15px";
   if(jqRef.browser.mozilla)
   {
      pTop = "4px";
      pBot = "4px";
	  pHeight = "auto";
   }
   jqRef('input[type="text"]')
      .css("height", pHeight)
      .css("padding-top", pTop)
      .css("padding-bottom", pBot);

}

