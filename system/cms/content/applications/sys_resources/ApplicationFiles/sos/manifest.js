/**
*
* @author  Copyright 2007 Shane O Sullivan (shaneosullivan1@gmail.com)
* @license Licensed under the Academic Free License 3.0 http://www.opensource.org/licenses/afl-3.0.php
*/
dojo.provide("sos.manifest");
dojo.require("dojo.ns");

(function(){
	var map = {
		html: {
			"imagegallery": "sos.widget.ImageGallery"
		}
	};
	
	function sosNamespaceResolver(name, domain){
		if(!domain){ domain="html"; }
		if(!map[domain]){ return null; }
		return map[domain][name];    
	}

	dojo.registerNamespaceResolver("sos", sosNamespaceResolver);
})();
