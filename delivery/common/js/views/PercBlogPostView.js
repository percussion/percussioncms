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
 * The delivery side blog post view class. Makes a call to the service to get previous and next blog post entries and renders them as
 * part of blog post navigation
 * On document ready loops through all perc-blog-navigation-container elements on the page and finds the query from the data attribute
 * on them. Passes the query and gets the entries from the service. If service returns an error, logs the error and
 * does nothing. 
 */
(function($)
{
    $(document).ready(function(){
        $.PercBlogPostView.updateBlogNav();
        $.PercBlogPostView.updateBlogLink();
        $.PercBlogPostView.trackBlogPost();
    });
    $.PercBlogPostView = {
        updateBlogNav : updateBlogNav,
        updateBlogLink : updateBlogLink,
        trackBlogPost : trackBlogPost
    };
    
    function updateBlogNav()
    {
        $(".perc-blog-navigation-container").each(function(){
            var currentBlogNavigation = $(".perc-blog");
            if ("" === currentBlogNavigation.attr("data-query")) {return;}
            var queryString = JSON.parse( currentBlogNavigation.attr("data-query"));
            
            //Get the blog nav context style
            var blogNavText = queryString.navType;
            delete queryString.navType;
            var blogPostNextPost = queryString.blogPostNextPost;
            delete queryString.blogPostNextPost;
            var blogPostPrePost = queryString.blogPostPrePost;
            delete queryString.blogPostPrePost;
                        
            //Get folder path and attach the pagename to make a complete pagepath.
            var folderPath = queryString.folderPath;
            delete queryString.folderPath;
            
            //Get the site name
            var siteName = queryString.siteName;
            delete queryString.siteName;
            
            // remove the index page name (used for blog link, not for query)
            delete queryString.blogIndexName;
              
            var pagePath = '/' + siteName + window.location.pathname;

            if(!(queryString.criteria.length))
            {
                $('.perc-blog').find('.perc-blog-nav-left-wrapper , .perc-blog-nav-right-wrapper').hide();
                return;
            }
            $.PercBlogPostService.getPostNavEntries(queryString, pagePath, function(status, navEntries){
                if(status)
                {
                    if(typeof navEntries ===  "string" ){
                        navEntries = JSON.parse(navEntries);
                    }
                    //If we have the next entry set the anchor href and if the setting is blogTitle set the link text also
                    if(null !== navEntries.next)
                    {
                        $('.perc-newer-post-wrapper a').attr('href', navEntries.next.pagepath.replace("/" + siteName, "")); 
                        if("blogTitle" === blogNavText)
                        {
                            $('.perc-newer-post-title').text(navEntries.next.linktext);
                        }
                        else
                        {
                            $('.perc-newer-post').text(blogPostNextPost);
                        }
                    }
                    else
                    {
                        //We don't have the next entry simply hide the div
                        $('.perc-blog').find('.perc-blog-nav-right-wrapper').html("&nbsp;");
                    }
                    //If we have the previous entry set the anchor href and if the setting is blogTitle set the link text also
                    if(null !== navEntries.previous)
                    {
                        $('.perc-older-post-wrapper a').attr('href', navEntries.previous.pagepath.replace("/" + siteName, "")); 
                        if("blogTitle" === blogNavText)
                        {
                            $('.perc-older-post-title').text(navEntries.previous.linktext);
                        }
                        else
                        {
                            $('.perc-older-post').text(blogPostPrePost);
                        }
                    }    
                    else
                    {
                        //We don't have the previous entry simply hide the div
                        $('.perc-blog').find('.perc-blog-nav-left-wrapper').html("&nbsp;");
                    }
                }
                else
                {
                    $('.perc-blog').find('.perc-blog-nav-left-wrapper , .perc-blog-nav-right-wrapper').hide();

                }
            }); 
        
        });
    }
    
    function updateBlogLink()
    {
        $('.perc-blog-wrapper').each(function(){
            var currentBlogNavigation = $(".perc-blog");
            if ("" === currentBlogNavigation.attr("data-query")){ return;}
            var queryString = JSON.parse( currentBlogNavigation.attr("data-query"));
            // Get the blog index page
            var siteName = "/" + queryString.siteName;
            var folderPath = queryString.folderPath;
            var indexPageName = queryString.blogIndexName;
            if ("" === indexPageName) {
                indexPageName = "index";
            }
            var blogIndexPage = folderPath.replace(siteName, "") + "/" + indexPageName;
            
            // Tags
            $('.perc-blog-post-tag-container').find('a').each(function(){
                var tag = ($(this).html().trim()).replace(",", "");
                var jsonQuery = {'criteria':["perc:tags LIKE '" + tag + "'"]};
                var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(jsonQuery));
                $(this).attr("href", blogIndexPage + "?filter="+ tag + encodedQuery);
            });
            
            // Categories
            $('.perc-blog-post-category-container').find('a').each(function(){
                var categoryPath = $(this).attr('data-categories');
                var category = ($(this).html().trim()).replace(",", "");
                var jsonQuery = {'criteria':["perc:category LIKE '" + categoryPath + "'"]};
                var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(jsonQuery));
                $(this).attr("href", blogIndexPage + "?filter="+ category + encodedQuery);
            });
        });
    }

    function trackBlogPost()
    {
        $('.perc-blog-wrapper').each(function(){
            var currentBlogElem = $(".perc-blog");
            if ("" === currentBlogElem.attr("data-query")){ return;}
            var queryString = JSON.parse(currentBlogElem.attr("data-query"));
            // Get the blog index page
            var trackBlogPost = queryString.trackBlogPost;
            if (!trackBlogPost) {
                return;
            }
            var blogPostFullPath = queryString.blogPostFullPath;
            var deliveryUrl = queryString.deliveryurl || "";
            if(queryString.isEditMode !== "true")
                $.PercMostReadBlogPostsService.trackBlogPost(blogPostFullPath, deliveryUrl, $.noop);
        });
    }
})(jQuery);
