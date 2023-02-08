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
