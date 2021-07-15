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
 * The delivery side page list view class. Makes a call to the service to get page list entries and renders them in the
 * list.
 * On document ready loops through all perc-page-auto-list elements on the page and finds the query from the data attribute
 * on them. Passes the query and gets the entries from the service. If service returns an error, logs the error and
 * does nothing. Otherwise loops through the each page list entry and creates a li element for each and appends it to
 * the list main element.
 */
(function($) {
    $(document).ready(function() {
        $.PercPageListView.updatePageList();
        $.PercPageListView.updateRSSLinks();
    });
    $.PercPageListView = {
        updatePageList: updatePageList,
        updateRSSLinks: updateRSSLinks
    };
    /**
     * Finds all the rss feed links by class perc-rss-icon and adds the version, and hostname to the url.
     */
    function updateRSSLinks() {
        var version = typeof($.getCMSVersion)=== "function" ? $.getCMSVersion() : "";
        var hostname = window.location.host;

        $(".perc-rss-icon").each(function() {
            var feedUrl = $(this).attr("href");
            feedUrl = feedUrl+hostname+"/";
            $(this).attr("href", feedUrl);

        });
    } // end updateRSSLinks

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
            for (var h = 0; h < list.length; h++) {
                var cats = list[h].split("/");
                for (var i = 0; i < cats.length; i++) {
                    if (null !== cats[i] && "" !== cats[i] && "Categories" !== cats[i]) {
                        temp = cats[i].replace(/[^a-zA-Z0-9]/g, "").trim().toLowerCase();
                        if ("" !== temp) {
                            if(catMap.indexOf(temp) === -1) {
                                catMap.push(temp);
                            }
                        }
                    }
                }
            }

            return catMap;
    } // end processCategoryClasses

    function updatePageList() {
        $(".perc-page-auto-list").each(function() {
            var currentAutoList = $(this);
            if ("" === currentAutoList.attr("data-query")){ return;}
            var locale = null;
            if ("" === currentAutoList.attr("data-locale")) {
                locale = "en-US";
            }
            else {
                locale = currentAutoList.attr("data-locale");
            }
            var dateFormat = null;
            if ("" === currentAutoList.attr("data-datefmt")) {
                dateFormat = "MMM dd YYYY";
            }
            else {
                dateFormat = currentAutoList.attr("data-datefmt");
            }

            var queryString = JSON.parse( currentAutoList.attr("data-query"));
            var hidePastResults = ("true" === $(currentAutoList).attr("data-hide-past-results"));
            if (hidePastResults) {
                var nowDate = new Date();
                var nowDateFormatted = nowDate.getFullYear() + "-" + ("0" + (nowDate.getMonth()+1)).slice(-2) + "-" + ("0" + nowDate.getDate()).slice(-2) + "T" + ("0" + nowDate.getHours()).slice(-2) + ":" + ("0" + nowDate.getMinutes()).slice(-2) + ":" + ("0" + nowDate.getSeconds()).slice(-2);

                var hideFilterDateType = $(currentAutoList).attr("data-hide-past-results-filter-type");
                var hideString =  hideFilterDateType + " > '" + nowDateFormatted + "'";
                queryString.criteria.push(hideString);

                var updatedQueryString = JSON.stringify(queryString);
                currentAutoList.attr("data-query", updatedQueryString);
            }

            var callback = function(target, navLoc, options, secondCallback) {
                // Needs to know about doResultsDisplay.
                return loadResults(target, navLoc, function(status, result) {
                    doResultsDisplay(status, result, options);
                    secondCallback(status, result);
                }, options);
            };

            //default settings
            this.settings = {

                criteria: [],

                dateFormat: "EEE MMM d, yyyy hh:mm a", // looks like Tuesday, March 15, 2011 11:11 PM

                summary: null,

                filter: null,

                query: {},

                pagingPagesText: 'pages',

                pagingOfText: 'of',

                totalEntries: 0,

                locale: 'en-US',

                rssLinkText: 'RSS Link'

            };

            var callbackOptions = {};

            this.settings.query.criteria = [];

            $.extend(this.settings, $.parseJSON($(this).attr('data-query')));

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

            function loadResults(target, startPage, callback, callbackOptions) {

                var currentPageList = $(target);

                if(!target.settings)
                {
                    target.settings = {};
                }

                // Get maxResults for later use, and set start index for the query.  Index is 0-based post number.
                if(isNaN(target.settings.maxResults) || 1 > target.settings.maxResults) {
                    target.settings.maxResults = 0;
                }

                target.settings.query.maxResults = target.settings.maxResults;
                
                target.settings.query.totalMaxResults = target.settings.totalMaxResults;

                target.settings.query.startIndex = (target.settings.maxResults*(startPage - 1));

                target.settings.baseURL = window.location.protocol + '//' + window.location.host + window.location.pathname;

                // Adding the filters from the query
                var urlstring = $.deparam.querystring();

                if ("undefined" !== typeof (urlstring.query) && "undefined" !== typeof (urlstring.filter))
                {
                    try
                    {
                        var obj = $.parseJSON(urlstring.query);
                        $.map(obj.criteria, function(n, i){
                            if ($.inArray(n, target.settings.query.criteria) === -1)
                            {
                                target.settings.query.criteria[target.settings.query.criteria.length] = n;
                            }
                        });
                    }
                    catch(e){}  // If criteria json is not well formed, then do nothing
                }

                return $.PercPageListService.getPageEntries(target.settings.query, function(status, results) {

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
            } // end loadResults

            function doResultsDisplay(target, returnData, options) {
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

            function displayResults(target, pageEntries, options) {

                var currentAutoList = $(target);

                if("undefined" !== typeof(pageEntries) && 0 < pageEntries.length) {
                    var structureListRoot = currentAutoList.find(".perc-page-auto-list-structure .perc-list-main");

                    //Clone the li for future use
                    var listElem = structureListRoot.find("li").clone();

                    //Find the original list root
                    var listRoot = currentAutoList.find(".perc-list-main").not('.perc-page-auto-list-structure .perc-list-main').empty();

                    currentAutoList.find(".perc-list-main-wrapper .perc-list-main").empty().remove();
                    currentAutoList.append(listRoot);

                    //Loop through the page entries and build the new list element as per the structure.
                    //Then add the newly created element to the list root.
                    for (var i = 0; i < pageEntries.length; i++) {
                        var pageEntry = pageEntries[i];
                        var newListElem = listElem.clone();
                        var rowClass = 0 === i % 2 ? "perc-list-even" : "perc-list-odd";
                        var spClass = 0 === i ? "perc-list-first" : i === pageEntries.length - 1 ? "perc-list-last" : "";

                        newListElem.addClass(rowClass);

                        if ("" !== spClass) {
                            newListElem.addClass(spClass);
                        }

                        var summary = null;
                        var createdDate = null;
                        var postDate = null;
                        var postDay = null;
                        var postMonth = null;
                        var timeZone = "";

                        $.each(pageEntry.properties, function(key, value) {
                            if("dcterms:contentpostdatetz" === key)
                            {
                                timeZone = value;
                            }
                            else if ("dcterms:abstract" === key) {
                                try {
                                    summary = value;
                                    var linkelem = summary.find("a.perc-more-link");
                                    if (0 < linkelem.length) {
                                        linkelem.addClass("perc-no-update-link-text perc-blog-list-more-link").attr('href', pageEntry.folder + pageEntry.name).text('...more');
                                        linkelem.find("a").attr('title', pageEntry.linktext);
                                        linkelem.find("a").attr('aria-label', 'Read more about ' + pageEntry.linktext);
                                    }
                                    summary = summary.appendTo('<div />').parent().html();
                                }
                                catch (err) {
                                    var parser = new DOMParser();
                                    var data = parser.parseFromString(value, "text/html");
                                    var link = $(data).find("A[class='perc-more-link']");
                                    if(null !== link && "undefined" !== typeof (link)) {
                                        $(link)
                                        .attr('class','perc-no-update-link-text perc-blog-list-more-link')
                                        .attr('href', pageEntry.folder + pageEntry.name)
                                        .attr('title', pageEntry.linktext)
                                        .attr('aria-label', 'Read more about ' + pageEntry.linktext)
                                        .text('...more');
                                        summary = data.xml ? data.xml : (new XMLSerializer()).serializeToString(data);
                                    }
                                } // end try/catch
                            } // end if

                            if ("dcterms:created" === key) {
                                try {
                                    createdDate = value;
                                    moment.locale(locale);
                                    postDate = moment(createdDate).formatWithJDF(dateFormat);
                                    postDay = moment(createdDate).formatWithJDF("dd");
                                    postMonth = moment(createdDate).formatWithJDF("MMM");
                                }
                                catch (err) {
                                    console.error(err);
                                    postDate = new Date();
                                }
                            }
                            else if ("perc:category" === key) {
                                var categoryClassList = processCategoryClasses(value);
                                for (var i = 0;i< categoryClassList.length;i++) {
                                        newListElem.addClass("perc-autolist-category-" + categoryClassList[i]);
                                }
                            }
                        }); // end $.each()

                        var linkText = pageEntry.linktext;
                        newListElem.find("a").attr("href", pageEntry.folder + pageEntry.name).html(linkText);
                        newListElem.find("a").attr("title", pageEntry.linktext);
                        newListElem.find("a").attr("aria-label", pageEntry.linktext);

                        if (summary) {
                            newListElem.find("div.perc-page-auto-list-summary-container").append(summary);
                        }

                        //Date & Calender Icon
                        newListElem.find("div.perc-page-auto-list-calicon-month").text(postMonth);
                        newListElem.find("div.perc-page-auto-list-calicon-day").text(postDay);
                        newListElem.find("span.perc-page-auto-list-date").text(postDate + " " + timeZone);

                        listRoot.append(newListElem[0]);
                        currentAutoList.find('.perc-list-main-wrapper').append(listRoot);
                    } // end for

                    currentAutoList.attr('aria-busy', 'false');

                } // end if(pageEntries.length > 0)
                else if ("true" === currentAutoList.attr("data-hide-past-results")) {
                    // then there are no results set in the future. need to make sure current  list is empty
                    console.log("This list is set to display events in the future but no results were returned.");
                    currentAutoList.find(".perc-list-main").not('.perc-page-auto-list-structure .perc-list-main').empty();
                }
                else {
                	if(status){
                		//Log the error and leave the original list entries as is
	                    console.error("No results returned by query. Server reported status: %s", status);
                	}else{
	                    //Log the no results and leave the original list entries as is
	                    console.log("No results returned by query.");
                	}
	                currentAutoList.attr('aria-busy', 'false');
                }
            } // end displayResults()
        }); // end $(".perc-page-auto-list").each(function()
    }// end updatePageList
})(jQuery);
