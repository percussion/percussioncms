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

(function($)
{
    $.PercMostReadBlogPostsService = {
		getMostReadPostsEntries : getMostReadPostsEntries,
		trackBlogPost: trackBlogPost
	};
	
    function trackBlogPost(pagePath, deliveryUrl, callback)
    {
		var serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl,"/perc-metadata-services/metadata/trackblogpost");
		$.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
			// Ignoring the callback;
        }, {pagePath:pagePath});
 	}

	function getMostReadPostsEntries(queryString, callback)
    {
		var deliveryUrl = queryString.deliveryurl || "";
		var timePeriod = queryString.timePeriod || "WEEK";
		var limit = queryString.numberOfResults || "R-5";
		var sectionPath = queryString.sectionPath || "";
		var promotedPagePaths = queryString.promotedPagePaths || "";
       	var serviceUrl = $.PercServiceUtils.joinURL(deliveryUrl,"/perc-metadata-services/metadata/topblogposts");
       	var sortOrder = queryString.sortOrderByHits || "desc";
		$.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                callback(true, results.data);
            }
            else{
              var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
              callback(false, defMsg);
            }
            
		}, {sectionPath: sectionPath, limit: limit, timePeriod: timePeriod, sortOrder: sortOrder, promotedPagePaths: promotedPagePaths});
        /** Sample data
		var results = getSampleResults(queryData);
		callback(true, results.data);
        */
	}
	/** Sample Data 
	function getSampleResults(queryData) {
		var numRes = queryData.numberOfResults || 5;
		data = [];
		for (i=0; i<numRes; i++) {
			var prep1 = Math.floor(Math.random() * 90000) + 10000;
			var prep2 = Math.floor(Math.random() * 90000) + 10000;
			var prep3 = Math.floor(Math.random() * 90000) + 10000;
			var res = {"site":"SiteTest", "pagepath":"/SiteTestapps/ROOT/BlogPost/" + prep1 + ".html", "folder":"/", "linktext":"BlogPost" + prep2, "name":"BlogPost " + prep3, "type":"page"}
			if (queryData.summary) {
				res.summary = "This is a page summary, that will be displayed only when the show summary is on.";
				if (prep1 % 2 == 1) {
					res.summary = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
				}
			}
			data.push(res);
			
		}
		var results = {data: data};
		return results;
	}
	*/
})(jQuery);
