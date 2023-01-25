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
 * Category list service, makes a call to the server and gets the category list entries.
 */
(function($)
{
    $.PercCategoryListService = {
        getCategories : getCategories
    };
    function getCategories(queryString, callback)
    {
        /*
	    var mock = [
            {category : "QQ"},
        	{"category":"B","children":[
            		{"category":"C","children":null,"count":{"second":2,"first":2}},
            		{"category":"D","children":[
            			{"category":"E","children":[{category:"E1",children:[{category:"E2", children:[{category:"E3", children:null}]}]}],"count":{"second":1,"first":1}}],"count":{"second":4,"first":3}},
            		{"category":"F","children":null,"count":{"second":4,"first":4}}],"count":{"second":10,"first":1}}];
    	*/
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
        var serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl, "/perc-metadata-services/metadata/categories/get");
        
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
