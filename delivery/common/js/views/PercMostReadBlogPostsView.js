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
 * Most visited blog post view
 */
(function($)
{
    $(document).ready(function(){
        $.PercMostReadBlogPostsView.updateMostReadBlogList();
    });
    $.PercMostReadBlogPostsView = {
        updateMostReadBlogList : updateMostReadBlogList
    };
    function updateMostReadBlogList()
    {
        $(".perc-most-read-blog-posts-view").each(function(){
            var currentElem = $(this);
            if ("" === currentElem.attr("data-query"))
            {
                return true; //skip to the next block
            }
            var data = currentElem.attr("data-query");
            var dataObj = {};
            if('string' === typeof (data) && 0 < data.length)
            {
                dataObj = JSON.parse(data);
            }
            var rootElem = $("<div/>");
            $.PercMostReadBlogPostsService.getMostReadPostsEntries(dataObj, function(status, blogList)
            {
                if ("undefined" !== typeof(blogList) && blogList.length)
                {
                    rootElem.append(createTitleHtml(dataObj));
                    var listElem = $("<ol>").addClass("perc-most-read-list perc-list-main");
                    if(!Array.isArray(blogList)){
                        blogList = JSON.parse(blogList);
                    }
                    $.each(blogList, function(index, entry){
                        listElem.append(createEntryHtml(dataObj, entry));
                    });
                    rootElem.append(listElem);
                    currentElem.append(rootElem);
                }
            });

        });
    }

    function createTitleHtml(settings)
    {
        var title = "";
        var elemName = settings.headingStyle || "h2";
        if (settings.listTitle) {
            title = $("<div/>")
                    .append($("<" + elemName + "/>")
                    		.addClass("perc-most-read-list-title")
                            .text( settings.listTitle)
                    );
        }
        return title;
    }

    /**
    * Create the page item html from the pageData returned from
    * the server.
    */
    function createEntryHtml(settings, entry)
    {
        var headingElem = settings.headingStyle || 'h3';
        var pagePath = entry.folder + entry.name;
        var promotedPaths = settings.promotedPagePaths || "";
        promotedPaths = promotedPaths.split(";");
        var isPromotedItem = $.contains(pagePath, promotedPaths);
        var pageItem = $("<li/>").addClass('perc-most-read-page-item');
        if (isPromotedItem) {
            pageItem.addClass('perc-promoted-blog-post');
        }
        var locale = "en-US";
        var dateFormat = 'EEE MMM d, yyyy hh:mm a';
        if (settings.locale) {
            locale = settings.locale;
        }

        if (settings.dateFormat) {
            dateFormat = settings.dateFormat;
        }

        //Page title
        var title = ("undefined" === typeof (entry.linktext)) ? "" : entry.linktext;
        pageItem.append($("<" + headingElem + "/>")
                        .addClass("perc-most-read-page-title")
                        .text(title)
                        .css("cursor", "pointer")
                        .on("click",function(){window.location = pagePath})
        );

        //Page date
        var datePage = "";
        if (("undefined" !== typeof (entry.properties["dcterms:created"])) && ("" !== entry.properties["dcterms:created"]))
        {
            moment.locale(locale);
            datePage = moment(entry.properties["dcterms:created"]).formatWithJDF(dateFormat);
        }
        if ("false" === settings.hideDate)
        {
            pageItem.append($("<p/>")
                            .addClass("perc-most-read-list-date-container")
                            .text(datePage)
            )
        }
        // Page summary
        if (settings.summary){
            var pageSummary = ("undefined" === typeof (entry.properties["dcterms:abstract"])) ? "" : entry.properties["dcterms:abstract"];
            var summary="";
            var parser = new DOMParser();
            var data = parser.parseFromString(pageSummary, "text/html");
            var link = $(data).find("A[class='perc-more-link']");
            if(null != link){
	              $(link)
	              	.attr('class','perc-no-update-link-text perc-most-read-list-more-link')
	              	.attr('href', pagePath)
                  .attr('title', entry.linktext)
	              	.text(settings.readMoreLink);
	              if(null !== data && typeof(data) !== undefined){
	              		summary = data.xml ? data.xml : (new XMLSerializer()).serializeToString(data);
            	}
            }
            pageItem.append($("<div/>")
                            .addClass("perc-most-read-page-summary")
                            .html(summary)
            )
        }
        return pageItem;
    }

})(jQuery);
