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
