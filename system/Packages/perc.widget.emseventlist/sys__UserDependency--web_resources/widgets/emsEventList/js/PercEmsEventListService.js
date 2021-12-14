/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
