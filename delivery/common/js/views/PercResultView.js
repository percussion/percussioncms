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
(function($){

   // Publically exposed methods
   var methods = {
        init: function(options)
            {
                return $(this).each(function()
                    {

                        this.settings = {
                            dateFormat: 'EEE MMM d, yyyy \'at\' hh:mm a', // looks like Tuesday, March 15, 2011 at 11:11 PM
                            dateFormatter: null,
                            summary: null,
                            filter: null,
                            query: null,
                            locale: 'en-US',
                            headerText: 'Results for',
                            readMoreLink:'more...',
                            pagingPagesText: 'pages',
                            pagingOfText: 'of'
                        };

                        if(options) {
                            $.extend(this.settings, options);
                        }

                        this.settings.dateFormatter = new SimpleDateFormat(this.settings.dateFormat);

                        var urlstring = $.deparam.querystring();

                        delete options.summary;

                        if ("undefined" !== typeof (urlstring.filter))
                        {
                            this.settings.filter = urlstring.filter;
                        }
                        if("undefined" !== typeof (urlstring.query))
                        {
                            this.settings.query = JSON.parse(urlstring.query);
                        }
                        if("undefined" !== typeof (options.orderBy) && "undefined" !== typeof (this.settings.query) && null !== this.settings.query)
                        {
                            this.settings.query.orderBy = options.orderBy;
                        }

                        if("undefined" !== typeof(this.settings)) {
                            if(undefined !== typeof(this.settings.query)) {
                                this.settings.query.deliveryurl = this.settings.deliveryurl;
                                this.settings.query.isEditMode = this.settings.isEditMode;
                            }
                        }

                        // Get maxResults for later use, and set start index for the query.  Index is 0-based post number.
                        if(!(0 < parseInt(this.settings.maxResults, 10))) // Might already be defined by the paging mechanism, if we're using one.
                        {
                            this.settings.maxResults = parseInt(this.settings.query.maxResults, 10); // Set
                            if (isNaN(this.settings.maxResults) || 1 > this.settings.maxResults) {
                                // Check
                                this.settings.maxResults = 0; // Reset
                            }
                        }
                    });
            },
        /**
         * Queries the server for an updated list of results.
         * @param startIndex The 0 based index of the first entry to ask the server for.
         */
        load: function(startIndex, secondCallback)
            {
                // We don't have a query, so what are we talking to the server for?
                if(!this.settings.query) {
                    return false;
                }

                if("undefined" !== typeof (startIndex))
                {
                    this.settings.query.startIndex = startIndex;
                }
                else
                {
                    this.settings.query.startIndex = 0;
                }

                this.settings.query.maxResults = this.settings.maxResults;
                
                this.settings.query.totalMaxResults = this.settings.totalMaxResults;

                var target = this;
                    // Return the jqXHR object as per contract.
                    return $.PercResultService.getPageEntries(this.settings.query, function(success, data){
                        data = $.PercServiceUtils.toJSON(data);
                        target.results = data.results;
                        $(target).PercResultView('updateDisplay', data.totalEntries);
         			    if("function" === typeof (secondCallback)) {
                            secondCallback(data, success);
                        }
                    });

            },
        updateDisplay: function(respTotalEntries)
            {
                // First, empty the list of all entries.
                var el = $(this);
                el.empty();

                // Number at the top
                var numEntries = this.totalEntries;
                if (!(0 < numEntries)) {
                    numEntries = respTotalEntries;
                }
                el.append(createTitleHtml(this.settings, numEntries));
                el.append($("<div/>").addClass("perc-result-divider"));
                if(typeof this.results !== 'undefined' && this.results.length >0)
                {
                    for(var c = 0; c < this.results.length; c++)
                    {
                        el.append(createEntryHtml(this.settings, this.results[c]));
                        if(c < (this.results.length - 1))
                        {
                            el.append($("<div/>").addClass("perc-result-divider"));
                        }
                    }
                }
                else
                {
                    console.warn('There are 0 results or there was an error retrieving results'
                            + 'from the metadata service.');
                }
            }
    };
    // Define the plugin on the jQuery namespace
    $.fn.PercResultView = function(method)
    {
        // Method calling logic
        if ( methods[method] ) {
            return methods[ method ].apply( this.get(0), Array.prototype.slice.call( arguments, 1 ));
        } else if ( 'object' === typeof method || ! method ) {
            return methods.init.apply( this.get(0), arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.PercResultRenderer' );
        }
    };

    function createTitleHtml(settings, count)
    {
        var title = $("<div/>")
                    .append($("<h2/>")
                            .addClass("perc-result-title")
                            .text( settings.headerText + ' ' + settings.filter)
                    )
                    .append($("<p/>")
                            .addClass("perc-result-count")
                            .text(count + (1 === count ? " " + settings.entriesTextSingular : " " + settings.entriesTextPlural))
                    );
        return title;
    }

    /**
    * Create the page item html from the pageData returned from
    * the server.
    */
    function createEntryHtml(settings, entry)
    {
        var pagePath = entry.folder + entry.name;
        var pageItem = $("<div/>").addClass('perc-result-page-item');
        var locale = settings.locale;
        //Page title
        var title = ("undefined" === typeof (entry.linktext)) ? "" : entry.linktext;
        pageItem.append($("<h3/>")
                        .addClass("perc-result-page-title")
                        .text(title)
                        .css("cursor", "pointer")
                        .on("click", function(){window.location = pagePath;
                        })
        );

        //Page date
        var datePage = "";
        var timeZone = "";
        if (("undefined" !== typeof (entry.properties["dcterms:created"])) && ("" !== entry.properties["dcterms:created"]))
        {
            moment.locale(locale);
            datePage = moment(entry.properties["dcterms:created"]).formatWithJDF(settings.dateFormat);
        }

        if (("undefined" !== typeof (entry.properties["dcterms:contentpostdatetz"])) && ("" !== entry.properties["dcterms:contentpostdatetz"]))
        {
            timeZone = entry.properties["dcterms:contentpostdatetz"];
        }

        pageItem.append($("<p/>")
                        .addClass("perc-result-page-date")
                        .text(datePage + " " + timeZone)
        );

        // Page summary
        if (settings.summary){
            var pageSummary = ("undefined" === typeof (entry.properties["dcterms:abstract"])) ? "" : entry.properties["dcterms:abstract"];
            var summary="";
            var parser = new DOMParser();
            var data = parser.parseFromString(pageSummary, "text/html");
            var link = $(data).find("A[class='perc-more-link']");
            if(null !== link && "undefined" !== typeof (link)){
	              $(link)
	                 .attr('class','perc-no-update-link-text perc-blog-list-more-link')
	              	 .attr('href', pagePath)
                   .attr('title', entry.linktext)
                   .attr('aria-label', 'Read more about ' + entry.linktext)
	              	.text(settings.readMoreLink);
	              if(null !== data && "undefined" !== typeof (data)){
	              		summary = data.xml ? data.xml : (new XMLSerializer()).serializeToString(data);
            	}
            }
            pageItem.append($("<div/>")
                            .addClass("perc-result-page-summary")
                            .html(summary)
            )
        } // end if
        return pageItem;
    }
    var doLoad = function(target, navLoc, options, secondCallback)
    {
        var startIndex = (target.settings.maxResults*(navLoc - 1));
        if(!(0 <= startIndex))
        {
            startIndex = 0;
        }
        return $(target).PercResultView('load', startIndex, secondCallback);
    };
    /* Auto bind Result instances for existing elements that have
    * a class of <code>perc-result-view</code>.
    */
    $(document).ready(function(){
        $('.perc-result-view').each(function(){
			try
			{
				var $el = $(this);
				var data = $el.attr("data-query");
				if('string' === typeof (data) && 0 < data.length)
				{
					var dataObj = JSON.parse(data);
				}
				else
				{
					dataObj = {};
				}
				$el.PercResultView(dataObj);
				$el.PercResultsPaging(doLoad, {});
			}
			catch(e)
			{
				// Probably a missing or bad query.  Nothing we can do.
			}
        });
    });

})(jQuery);
