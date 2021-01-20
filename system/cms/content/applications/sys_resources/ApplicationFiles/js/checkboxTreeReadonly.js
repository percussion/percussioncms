/**
 * Javascript to fetch the selected values for the categories in the page metadata.
 * Convert these selected value strings to user readable information and set in the page meta data view screen.
 * 
 * See sys_Templates for the checkboxTree control. It is a replacement
 * for the applet checkbox tree control.
 * Options:
 * @url is an XML resource that dictates the structure of the tree. See sys_CheckboxTreeControl.dtd
 * @selected The values in comma separated list where the separator is ';' thus the data (node ids) cannot have semicolons in.
 * @paramName The paramter name used to update the hidden input field after values are selected.
 * @readonly Read only mode.
 *  Usage:
    var opts = {url : treeSrcUrl, selected : selectedValues, paramName : paramName, readonly=true};
    $('#my-tree').perc_checkboxTree(opts);
 * 
 */
(function($) {

	$.fn.perc_checkboxTreeReadonly = function(opts){
	
		var defaults = {
		        separator: ";",
		        readonly: false
		};

		var options = $.extend(defaults, opts);
		
		var $this = $(this);
		var paramName = opts.paramName;
		var treeSrcUrl = opts.url;
		var selectedValues = opts.selected;
		var readonly = opts.readonly;
		
		function getTreeXml(url, callback) {
		    $.ajax({
		        type: "GET",
		        url: url,
		        dataType: "xml",
		        success: callback
		    });
		}
		
		function displaySelectedCategory(data) {
			var container = $("#datadisplay-"+paramName);
			container.text(data);
		}
		
		function findSelectedTitle(selected, tree, returnString, categoryCount) {
			
			var selectedArray = selected.split('/');
			
			if(returnString == null)
				returnString = "/" + selectedArray[1];
			
			for (i = 0; i < tree.length; i++) { 
				if(((returnString.split('/')).length) == selectedArray.length)
					break;
				if (tree[i].nodeType == 1) {
					if(selectedArray.indexOf(tree[i].attributes.getNamedItem("id").value) != -1) {
						returnString = returnString + "/" + tree[i].attributes.getNamedItem("label").value;
						
						if(selectedArray.length > categoryCount) {
							returnString = findSelectedTitle(selected, tree[i].childNodes, returnString, categoryCount+1);
						}
					} 
				}
			}
			
			return returnString;
		}
		
		getTreeXml(treeSrcUrl, function(xml) {

			var selected = selectedValues.split(';');
			var value = "";

			if(selected[0] === "") {
				value = "";
				displaySelectedCategory(value);
			} else {
				for(k = 0; k < selected.length; k++) {
					if(value != "")
						value = value + separator + " " + findSelectedTitle(selected[k], xml.documentElement.childNodes, null, 3);
					else
						value = findSelectedTitle(selected[k], xml.documentElement.childNodes, null, 3);
				}
				displaySelectedCategory(value);
			}
	    });
	};
})(jQuery);