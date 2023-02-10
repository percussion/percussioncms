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
 * Blog list service, makes a call to the server and gets the blog list entries.
 */
(function($)
{
    $.PercBlogPostService = {
        getPostNavEntries : getPostNavEntries
    };
    function getPostNavEntries(queryString, pagePath, callback)
    {
        //var pagePathId = "?currentPageId=" + pagePath;
    	var deliveryUrl = "";
    	try{
    		if ("undefined" !== typeof (queryString.deliveryurl)){
    		    deliveryUrl = queryString.deliveryurl;
    		    delete queryString.deliveryurl;
    	    }
    	}    
        catch (err) {
		    console.error(err);
	    }

        if('undefined' === typeof (pagePath)){
            queryString.currentPageId = "undefined";
        }else{
            queryString.currentPageId = pagePath;
        }

        var serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl,"/perc-metadata-services/metadata/blog/getCurrent");
        
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
        
        /*var results = [
            {"site":"SiteTest","pagepath":"/SiteTestapps/ROOT/BlogPost1","folder":"/","linktext":"BlogPost1","name":"BlogPost1","type":"page"},
            {"site":"SiteTest","pagepath":"/SiteTestapps/ROOT/BlogPost2","folder":"/","linktext":"BlogPost2","name":"BlogPost2","type":"page"},
            {"site":"SiteTest","pagepath":"/SiteTestapps/ROOT/BlogPost3","folder":"/","linktext":"BlogPost3","name":"PageTest2","type":"page"}
        ];
        callback(true,results);*/
    }
})(jQuery);
