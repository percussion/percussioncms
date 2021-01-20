/**
 * Feature to retrieve the column index of the current gadget
 */
gadgets.window = gadgets.window || {};

/**
 * Helper function to get a query string paramater.
 */
function __gup( name )
{
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( window.location.href );
  if( results == null )
    return "";
  else
    return results[1];
}

/**
 * Return the index of the column that the current gadget exists in.
 * @return the gadget index
 * @type {int}
 */
gadgets.window.getDashboardColumn = function() {
   var __mid = __gup("mid");
   var __gad = percJQuery("#gid_" + __mid);
   var __columnRawId = __gad.parent().attr("id");
   return parseInt(__columnRawId.substr(4));
}


