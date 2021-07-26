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

 * The delivery side blog list view class. Makes a call to the service to get blog list entries and renders them in the

 * list.

 * On document ready loops through all perc-blog-list-container elements on the page and finds the query from the data attribute

 * on them. Passes the query and gets the entries from the service. If service returns an error, logs the error and

 * does nothing. Otherwise loops through the each blog list entry and creates a li element for each and appends it to

 * the list main element.

 */

(function($)
{
    $(document).ready(function(){
        $.PercBlogListView.updatePageLists();
    });
    $.PercBlogListView = {
        updatePageLists : updatePageLists
    };

    function updatePageLists()
    {
        $(".perc-blog-list-container").each(function()
        {
            var callback = function(target, navLoc, options, secondCallback)
            {
                // Needs to know about doResultsDisplay.
                return loadResults(target, navLoc, function(status, result)
                {
                    doResultsDisplay(status, result, options);
                    secondCallback(status, result)
                }, options);
            };

            // Sane defaults
            this.settings = {
                criteria: [],
                dateFormat: "EEE MMM d, yyyy hh:mm a", // looks like Tuesday, March 15, 2011 11:11 PM
                summary: null,
                filter: null,
                query: {},
                blogPostReadMoreText: 'more...',
                pagingPagesText: 'pages',
                pagingOfText: 'of',
                totalEntries: 0,
                blogPostCommentsText: 'Comments',
                locale: 'en-US',
                blogPostBylineText: 'by',
                rssLinkText: 'RSS Link'
            };

            var callbackOptions = {};

            this.settings.query.criteria = [];

            $.extend(this.settings, JSON.parse($(this).attr('data-query')));

            this.settings.query = $.extend(true, {}, this.settings.query); // Prevents overwrite of anything, but initializes it if it doesn't exist already.

            this.settings.query.criteria = this.settings.query.criteria.concat(this.settings.criteria);

            this.settings.query.maxResults = this.settings.maxResults;

            this.settings.query.totalMaxResults = this.settings.totalMaxResults;
            this.settings.query.deliveryurl = this.settings.deliveryurl;
            this.settings.query.isEditMode = this.settings.isEditMode;

            if ("undefined" !== typeof (this.settings.orderBy))
            {
                this.settings.query.orderBy = this.settings.orderBy;
            }

            $(this).PercResultsPaging(callback, callbackOptions);
        });
        if($("ol.perc-blog-list.perc-list-main").length >0)
            $("ol.perc-blog-list.perc-list-main")[0].style.visibility="visible";
    } // end updatePageLists

    function processTags(list, target)
    {
        if ("string" === typeof (list)){
            list = list.split();
        }

        list = list.sort();

        let strReturn = "";

        for (let i = 0; i < list.length; i++) {
            var jsonQuery = {
                'criteria': ["perc:tags = '" + list[i] + "'"]
            };

            var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(jsonQuery));

            let sep = (i === list.length - 1) ? "" : ",";
            strReturn += '<a href="' + target.settings.baseURL + "?filter=" + list[i] + encodedQuery + '"' + 'title="' + list[i] + '"' + 'aria-label="Tag: ' + list[i] + '"' + '>' + list[i] + sep + ' </a>';
        } // end for loop

        return strReturn;
    } // end processTags

    /**
     * Load a new set of blogs from the server, the call the callback function.
     * @name loadResults
     * @param target The element to load blog posts for.  Should contain a jQuery data object with the metadata query.
     * @param startPage The page number to load.  If 1 is passed, will update totalPages as well.
     * @param callback Which function to call after loading new blog posts.
     * @param callbackOptions Options to pass to the callback function.
     */
    function loadResults(target, startPage, callback, callbackOptions)
    {
        var currentBlogList = $(target);
        if(!target.settings)
        {
            target.settings = {};
        }

        //var queryString = $.parseJSON(currentBlogList.attr("data-query"));

        // Get maxResults for later use, and set start index for the query.  Index is 0-based post number.
        if(isNaN(target.settings.maxResults) || 1 > target.settings.maxResults){
                target.settings.maxResults = 0;
        }

        target.settings.query.maxResults = target.settings.maxResults;
        target.settings.query.totalMaxResults = target.settings.totalMaxResults;
        target.settings.query.startIndex = (target.settings.maxResults*(startPage - 1));
        target.settings.baseURL = window.location.protocol + '//' + window.location.host + window.location.pathname

        // Adding the filters from the query
        var urlstring = $.deparam.querystring();

        if ("undefined" !== typeof (urlstring.query) && "undefined" !==  typeof (urlstring.filter))
        {
            try
            {
                var obj = JSON.parse(urlstring.query);
                $.map(obj.criteria, function(n, i){
                    if ($.inArray(n, target.settings.query.criteria) === -1)
                    {
                        target.settings.query.criteria[target.settings.query.criteria.length] = n;
                    }
                });
            }
            catch(e){}  // If criteria json is not well formed, then do nothing
        }

        return $.PercBlogListService.getPageEntries(target.settings.query, function(status, results){
            if(status) {
                callback(target, results, callbackOptions);
            }
            else
            {
                if(console) {
                    console.debug(results);
                }
            }
        });
    }

    // Pre-processing on the results before handing it off to the display function
    function doResultsDisplay(target, returnData, options)
    {
        if(typeof returnData!=="undefined"){
            returnData = $.PercServiceUtils.toJSON(returnData);
        }
        //returnData = JSON.parse(returnData);
        // We only get totalEntries back if we've requested page 1.  Otherwise, we get null.
        if (returnData.totalEntries)
        {
            target.totalPages = Math.ceil(returnData.totalEntries/target.settings.maxResults);
            target.totalEntries = returnData.totalEntries;
        }

        var results = returnData.results;

        displayResults(target, results, options);
    }

    /**
     * Given a list of blog posts, display them.
     * @name displayResults
     * @param target The element to display the blog posts inside of.
     * @param pageEntries The list of blog posts.
     * @param options Options passed through to the function.
     */
    function displayResults(target, pageEntries, options)
    {
        var currentBlogList = $(target);
        if ("undefined" !== typeof(pageEntries) && (0 < pageEntries.length))
        {
            if(0 >= currentBlogList.find("li").length)
            {
                currentBlogList.find('.perc-no-post').show();
            }
            /*else
            {
                //If there is static list data and there is no result from DTS - don't clear the static list
                //- something is most likely wrong.
                return;
            }*/
        }
        /*else
        {
            //If there is static list data and there is no result from DTS - don't clear the static list
            //- something is most likely wrong.
            return;
        }*/

        var urlstring = $.deparam.querystring();

        if ("undefined" !==  typeof (urlstring.query) && "undefined" !== typeof (urlstring.filter))
        {
            try
            {
                JSON.parse(urlstring.query);

                var objData = JSON.parse(currentBlogList.attr("data-title"));

                currentBlogList.parent().find(".perc-result-divider").remove();
                currentBlogList.parent().find(".perc-bloglist-result-container").remove();
                currentBlogList.parent().prepend("<div class=\"perc-result-divider\"></div>").prepend(createTitleHtml(objData.resultsTitle + " " + urlstring.filter, target));
            }
            catch (e)
            {
                console.error(e);
            }
        }

        try
        {
            //Get the structure and get the root element of the list
            var structureListRoot = currentBlogList.find(".perc-blog-list-structure .perc-blog-list");

            //Clone the li for future use
            var listElem = structureListRoot.find("li").clone();

            //Find the original list root
            var listRoot = currentBlogList.find(".perc-blog-list").not('.perc-blog-list-structure .perc-blog-list').empty();

            scroll(0, 0);

            //Loop through the page entries and build the new list element as per the structure.
            //Then add the newly created element to the list root.
            for (var i = 0; i < pageEntries.length; i++)
            {
                var pageEntry = pageEntries[i];
                var timeZone = "";
                var newListElem = listElem.clone();
                var rowClass = 0 === i % 2 ? "perc-list-even" : "perc-list-odd";
                var spClass = 0 === i ? "perc-list-first" : i === pageEntries.length - 1 ? "perc-list-last" : "";

                newListElem.addClass(rowClass);

                if (spClass !== "")
                    newListElem.addClass(spClass);

                var summary = null;
                var author = null;
                var postDate = null;
                var tagList = null;
                var categoryList = null;
                var categoryClassList = null;
                var locale = null;
                let postDay = null;
                let postMonth = null;

                if (target.settings.locale){
                    locale = target.settings.locale;
                }

                $.each(pageEntry.properties, function(key, value)
                {
                    if("dcterms:contentpostdatetz" === key)
                    {
                        timeZone = value;
                    }
                    else if ("dcterms:abstract" === key)
                    {
                        try
                        {
                            var parser = new DOMParser();
                            var data = parser.parseFromString(value, "text/html");
                            var link = $(data).find("A[class='perc-more-link']");
                            if(null !== link && "undefined" !== typeof (link))
                            {
                                $(link)
                                .attr('class','perc-no-update-link-text perc-blog-list-more-link')
                                .attr('href', pageEntry.folder + pageEntry.name)
                                .attr('title', pageEntry.linktext)
                                .attr('aria-label', 'Read more about ' + pageEntry.linktext)
                                .text(target.settings.blogPostReadMoreText);
                                summary = data.xml ? data.xml : (new XMLSerializer()).serializeToString(data);
                            } // end if
                        }
                        catch(e)
                        {  //handle situations where the page summary is not stored as XML
                            if(value)
                            {
                                summary = value;
                            }
                        } // end try/catch
                    } // end if
                    else if("dcterms:author" === key)
                    {
                        author = value;
                    }
                    else if ("dcterms:created" === key)
                    {
                        moment.locale(locale);
                        postDate = moment(value).formatWithJDF(target.settings.dateFormat);
                        postDay = moment(value).formatWithJDF("dd");
                        postMonth = moment(value).formatWithJDF("MMM");
                    }
                    else if ("perc:tags" === key)
                    {
                        tagList = processTags(value, target);
                    }
                    else if ("perc:category" === key)
                    {
                        categoryList = processCategories(value, target);
                        categoryClassList = processCategoryClasses(value);
                        for (let j = 0; j < categoryClassList.length; j++)
                        {
                            newListElem.addClass("perc-bloglist-category-" + categoryClassList[j]);
                        }
                    }
                }); // end forEach

                let linkText = pageEntry.linktext;

                // First change all the href's for the links, the remove the ones which shouldn't have their innerHtml updated, then update innerHtml on everything that should.
                newListElem.find("a").attr("href", pageEntry.folder + pageEntry.name).not('.perc-no-update-link-text').html(linkText);
                newListElem.find("a").attr('aria-label', linkText);
                var commentText = newListElem.find(".perc-blog-list-comment-container a").attr('title');
                newListElem.find("a").attr("title", linkText);
                newListElem.find(".perc-blog-list-comment-container a").attr('title', linkText);
                newListElem.find(".perc-blog-list-comment-container a").attr('aria-label', 'Comments for: ' + linkText);

                if (summary) {
                    newListElem.find("div.perc-blog-list-summary-container").prepend(summary);
                }
                if (tagList) {
                    newListElem.find("div.perc-blog-list-tag-container").append(tagList);
                }
                else {
                    newListElem.find("div.perc-blog-list-tag-container").addClass('perc-blog-hide-container');
                }
                if (categoryList) {
                    newListElem.find("div.perc-blog-list-category-container").append(categoryList);
                }
                else {
                    newListElem.find("div.perc-blog-list-category-container").addClass('perc-blog-hide-container');
                }
                if (author) {
                    newListElem.find(".perc-blog-list-byline-container").text(target.settings.blogPostBylineText + " " + author);
                }
                if (postDate) {
                    newListElem.find(".perc-blog-list-date-container").text(postDate + " " + timeZone);
                }

                //Calendar Icon
                newListElem.find("div.perc-blog-list-calicon-month").text(postMonth);
                newListElem.find("div.perc-blog-list-calicon-day").text(postDay);
                newListElem.find("span.perc-blog-list-date").text(postDate);

                listRoot.append(newListElem);
            } // end for

            currentBlogList.attr('aria-busy', 'false');
        }
        catch(e)
        {
            console.error(e);
            currentBlogList.attr('aria-busy', 'false');
        }


    } // end displayResults


    function processCategories(list, target) {
        if ("string" === typeof (list)) {
            list = list.split();
        }
        list = list.sort(orderCategory);

        let strReturn = "";

        for (let i = 0; i < list.length; i++) {
            var jsonQuery = {
                'criteria': ["perc:category = '" + list[i] + "'"]
            }

            var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(jsonQuery));

            let sep = (i === list.length - 1) ? "" : ",";
            let cat = list[i].split('/');

            //FIXME:  This should not being mangling the URL.
            strReturn += '<a href="' + target.settings.baseURL + "?filter=" + cat[cat.length - 1] + encodedQuery + '"' + 'title="' + cat[cat.length-1] + '"' + 'aria-label="Category: ' + cat[cat.length-1] + '">' + cat[cat.length - 1] + sep + ' </a>';
        }

        return strReturn;
    } // end processCategories

    /***
     * Get a list of formatted Category CSS classes.
     * @returns {string|Array}
     * @param {string|Array} list array of categories in path format.
     */
    function processCategoryClasses(list) {
        var catMap = [];
        var temp = null;

        if ("string" === typeof (list)) {
            list = list.split(",");
        }
        //de-dupe the categories
        for (let h = 0; h < list.length; h++)
        {
            let cats = list[h].split("/");
            for (let i = 0; i < cats.length; i++)
            {
                if (null !== cats[i] && "" !== cats[i] && "Categories" !== cats[i])
                {
                    temp = cats[i].replace(/[^a-zA-Z0-9]/g, "").trim().toLowerCase();
                    if ("" !== temp)
                    {
                        if(catMap.indexOf(temp) === -1)
                        {
                            catMap.push(temp);
                        }
                    }
                }
            }
        }

        return catMap;
    } // end processCategoryClasses

    function orderCategory(a, b)
    {
        a = a.split('/');
        b = b.split('/');
        if (a[a.length - 1] === b[b.length - 1]) {
            return 0;
        }
        if (a[a.length - 1] > b[b.length - 1]) {
            return 1;
        }
        return -1
    }

    function createTitleHtml(text, target)
    {
        let count = target.totalEntries;
        return $("<div/>")
            .append($("<h2/>")
                .addClass("perc-bloglist-result-title")
                .text(text)
            )
            .append($("<p/>")
                .addClass("perc-bloglist-result-count")
                .text(count + (1 === count ? " " + target.settings.entriesTextSingular : " " + target.settings.entriesTextPlural))
            )
            .addClass("perc-bloglist-result-container");
    }


})(jQuery);
