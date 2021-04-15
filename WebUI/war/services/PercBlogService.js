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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 *  Blog related services
 */
(function($) {
    $.PercBlogService = {
        getBlogsForSite : getBlogsForSite,
        getPostsForBlog : getPostsForBlog
    };
    
    /**
     *  Returns a list of all blogs for a given siteName
     */
    function getBlogsForSite(siteName, callback) {
		var requestUrl = $.perc_paths.BLOG_LOAD;
		if(siteName !== "@all") {
			requestUrl += "/" + siteName;
		}
	    else {
			requestUrl = requestUrl.replace("blogs", "allBlogs");
		}
	    
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
    /**
     *  Returns a list of all posts for a given Blog
     */
    function getPostsForBlog(selectedBlogId, callback) {
		var requestUrl = $.perc_paths.POST_LOAD;
		requestUrl += "/" + selectedBlogId;
        
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
})(jQuery);