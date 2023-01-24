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

/**
 * Page list service, makes a call to the server and gets the page list entries.
 */
(function($)
{
    $.PercPageListService = {
        getPageEntries : getPageEntries
    };
    function getPageEntries(queryString, callback)
    {
    	var deliveryUrl = "";

        //Skip DTS processing if we are in edit mode
        if(queryString.isEditMode === "true"){
            callback(false,"");
            return;
        }

    	try{
    		if ("undefined" !== typeof (queryString.deliveryurl)){
    		    deliveryUrl = queryString.deliveryurl;
    		    delete queryString.deliveryurl;
    	    }
    	}
        catch (err) {
		   console.error(err);
	    }
        var serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl, "/perc-metadata-services/metadata/get");

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                callback(true,results.data);
            }
            else{
              var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
              callback(false, defMsg);
            }
            
        }, queryString);
    }
})(jQuery);
