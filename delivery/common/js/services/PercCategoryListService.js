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
