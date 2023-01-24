/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function ( $ ) {
 
	$.PercEmsEventListService = {
        getPageEntries : getPageEntries
    };
	
	function getPageEntries(settings, callback)
    {
    	var deliveryUrl = "";
    	try{
    		if (query.deliveryurl!=undefined){
    		    //deliveryUrl = queryString.deliveryurl;
    		    delete settings.deliveryurl;
    	    }
    	}
        catch (err) {
			var _do = "";
	    }
        
        var serviceUrl = "/perc-integrations/integrations/ems/mc/events";
		if(settings.publish==true){
			return $.PercServiceUtils.makeXdmJsonRequest(null,serviceUrl,$.PercServiceUtils.TYPE_POST,function(status, results)
        {
            if(status == $.PercServiceUtils.STATUS_SUCCESS){
                callback(true,results.data, settings);
            }
            else{
              var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
              callback(false, defMsg);
            }
            
        }, settings.query);
		}else{
			//We are in the editor or preview so use the CMS proxy to get to the DTS
			$.ajax({
				type: "POST",
				url: "/Rhythmyx/services/integrations/ems/mc/events",
				data: JSON.stringify(settings.query),
				processData: false,
				contentType: "application/json; charset=UTF-8",
				dataType: "json",
				success: function( data, textStatus, jqXHR){
					callback(true, data, settings);
				},
				error: function (jqXHR, textStatus,errorThrown ) {
					console.error(errorThrown);
				}
			});
		}
    }
})(jQuery);
