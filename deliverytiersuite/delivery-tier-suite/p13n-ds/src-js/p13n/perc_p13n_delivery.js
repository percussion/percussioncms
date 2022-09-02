
function P13NListItem(id, header, before, after, footer) {
	this.id = id;
	this.jquery = '.p13nListItemId_' + id;
	this.data = null;
	this.htmlHeader = header || '';
	this.htmlBefore = before || '';
	this.htmlAfter = after || '';
	this.htmlFooter = footer || '';
}

P13NListItem.prototype = jQuery.extend(P13NListItem.prototype,
{	
	run : function () {
		jQuery(function(){});
		var ruleItem = this;
		this.getData(function(data) {
			var html = ruleItem.dataToHtml.call(ruleItem,data);
			jQuery(ruleItem.jquery).replaceWith(html);
			jQuery('html *').trigger("p13nAfterDelivery", [data]); 
			ruleItem.postRender();
		});
	},
	
	rest_post : function(url, data, success, error) {
		error = error ? error : function(xhr, response, error) { alert(error); }; 
		jQuery.ajax({
			type: 'POST',
			url: url,
			contentType: "application/json; charset=utf-8", 
			dataType : "json",
			data: data, 
			success : success,
			error : error
		});
	},
	
	dataToHtml : function(data) {
    	if(data.status == "ERROR") {
    		return data.message;
    	}
    	var header = this.htmlHeader;
    	var body = '';
        var before = this.htmlBefore;
        var after = this.htmlAfter;
    	jQuery.each(data.snippetItems, function(i, item) {
    		body = body + before;
    		if (item.style != '') { 
    			body = body + '<div class="' + item.style + '"' + '>';
    		}
    		body = body + item.rendering;
    		if (item.style != '') {
    			body = body + '<' + '/div>';
    		}
    		body = body + after;
    	});
    	var footer = this.htmlFooter;
    	return header + body + footer;
	},
	
	postRender : function () { },
	
	getData : function(renderCallback) { 
		if (this.data) {
			this.__getDataLocal(renderCallback);	
		}
		else {
			this.__getDataRemote(renderCallback);
		}
	},
	
	__getDataLocal : function(renderCallback) {
		this.rest_post(this.url, this.data, renderCallback, null);
	},
	
	__getDataRemote : function(renderCallback) {
        var ruleItem = this;
		jQuery.getJSON( this.url + "?jsoncallback=?", 
			{ listItemId: this.id},
			function(d) { 
				jQuery(document).ready( function() {
					renderCallback.call(ruleItem,d);
				});
			}
		);
	}

});