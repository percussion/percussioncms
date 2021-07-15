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

    var pagingStructure = null;
    /**
     * Pages a list of items.
     * @param loadCallback <code>jqXHR function(targetElement, selectedPage, options)</code>
     *                     A function which gets called immediately.  This function must handle
     *                     the load and display of the newly selected page.
     * @param callbackOptions An object containing options to be passed to the callback function.
     */
    let PercResultsPaging = function(loadCallback, callbackOptions)
    {
        var elem = this.get(0);
        if(null === pagingStructure)
        {
            buildPagingStructure();
        }
        elem.totalPages = 1;
        elem.totalEntries = 0;

        // Pulls the 'data' attr from the current element, and does a json parse on it.
        // Then, grabs the maxResults value, and parseInt on it, making sure to use base 10.        
        if(!(0 < parseInt(elem.settings.maxResults, 10))) // Might already be defined by the element itself, or might be defined, but NaN.
        {
            elem.settings.maxResults = parseInt($.parseJSON(this.attr('data-query')).maxResults, 10);
            if (isNaN(elem.settings.maxResults) || 1 > elem.settings.maxResults) {
                elem.settings.maxResults = 0;
            }
        }
        elem.settings.query.returnTotalEntries = true; //setting the flag to have server return totalEntries regardles on which page we are
        let navLoc = getPageFromLocation(); //overiding it with value from URL - befor was just comming as parametr.
        quickNav(elem, navLoc, loadCallback, callbackOptions, null);

        window.onpopstate = function(e) {
            window.location.reload();
        };
        return this;
    };

    let updateNav = function(target, navLoc, callback, callbackOptions)
    {
        let currDiv = $(target);

        let totalMaxResults = target.settings.totalMaxResults;
        if(!totalMaxResults) {
            totalMaxResults = -1;
        }

        let maxPerPage = target.settings.maxResults;
        if(!maxPerPage) {
            maxPerPage = -1;
        }

        let currPaging = currDiv.find('.perc-pagination-container');
        if(0 < currPaging.length) {
            currPaging.replaceWith(pagingStructure.clone());
        }
        else {
            currDiv.append(pagingStructure.clone());
        }

        // If we've decided that it's only one page long, don't bother paging.
        if(1 >= target.totalPages || !(parseInt(target.totalPages, 10) === target.totalPages) || (maxPerPage >= totalMaxResults))
        {
            currDiv.find('.perc-pagination-container').hide();
            return;
        }
        else
        {
            currDiv.find('.perc-pagination-container').show();
        }

        navLoc = +navLoc; // String to Number Coercion

        currDiv.find('.perc-current-page').removeClass('perc-current-page');

        // Clean invalid values.
        if (1 > navLoc) {
            navLoc = 1;
        }
        else if (navLoc > target.totalPages) {
            navLoc = target.totalPages;
        }
        // Sane defaults
        let center = 3;
        let navLocClass = '.perc-third';
        let navLocPrev = '.perc-second';
        let navLocNext = '.perc-fourth';

        // Do we need multi-paging?  (Scrolls with you as you advance through pages)
        if (4 < target.totalPages)
        {
            if (3 > navLoc)  // Special case to clamp beginning of paging to the left side.
            {
                center = 3;
                if (1 === navLoc){
                    navLocClass = '.perc-first';  // Which element gets perc-current class for bolding
                    navLocPrev = '.perc-goto-last';
                    navLocNext = '.perc-second';
                }
                else if (2 === navLoc){
                    navLocClass = '.perc-second';
                    navLocPrev = '.perc-first';
                    navLocNext = '.perc-third';
                }
            }
            else if (navLoc > (target.totalPages - 2)) // Special case to clamp end of paging to the right side.
            {
                center = target.totalPages - 2;
                if (navLoc === target.totalPages){
                    navLocClass = '.perc-fifth';
                    navLocPrev = '.perc-fourth';
                    navLocNext = '.perc-goto-first';
                }
                else if (navLoc === (target.totalPages - 1)){
                    navLocClass = '.perc-fourth';
                    navLocPrev = '.perc-third';
                    navLocNext = '.perc-fifth';
                }
            }
            else // Somewhere in the middle, everything is standard.
            {
                center = navLoc;
                navLocClass = '.perc-third';
                navLocPrev = '.perc-second';
                navLocNext = '.perc-fourth';
            }
        }
        else
        {
            // Simple paging is simple.
            center = 3;

            if(1 === navLoc){
                navLocClass = '.perc-first';
                navLocPrev = '.perc-goto-last';
                navLocNext = '.perc-second';
            }
            else if(2 === navLoc){
                navLocClass = '.perc-second';
                navLocPrev = '.perc-first';
                navLocNext  = '.prec-third';
            }else if(3 === navLoc){
                navLocClass = '.perc-third';
                navLocPrev = '.perc-second';
                navLocNext = '.perc-fourth';
            }
            else{
                navLocClass = '.perc-fourth';
                navLocPrev = '.perc-third';
                navLocNext = '.perc-fifth';
            }
        }

        // Hide all numbers first, in case we don't need to use them.
        currDiv.find('.perc-pagination-list-entry').hide();

        /*
           A lot of this is fairly boilerplate, however, since we've also got a lot of distinct
           values in each instance, I've written it out fully.  We could package these into smaller
           methods (indeed, they used to be like that), but we would end up passing in most of the
           function into the arguments, so I've left it separate for now.  In general, the ordering
           is 1) find the right div 2) show it 3) set it's text to the right number 4) unbind
           previous click events 5) bind a new click event.  The click even is just a call to
           quickNav with the second argument varying.  Second argument should always be equal to the
           .text()
        */

        // First and prev nav. Next and last nav. Also 1 and 2, since we've got at least two pages.
        if(1 < target.totalPages)
        {

            //if not on first page already
            if (1 < navLoc)
            {
                // Make sure to unbind click first, goes crazy otherwise.
                currDiv.find('.perc-goto-first').attr('aria-label', 'First page').attr('href', genPageUrl(1)).off('click').on("click", function(event) {
                    quickNav(target, 1, callback, callbackOptions, event);
                }).parent().show();
                currDiv.find('.perc-goto-pre').attr('aria-label', 'Previous page ' + (navLoc - 1)).attr('href', genPageUrl(navLoc -1)).off('click').on("click", function(event) {
                    quickNav(target, navLoc - 1, callback, callbackOptions, event);
                }).parent().show();

                updateLinkPrevTag(navLoc);

            } else {//need just any content in hidden link to make validation tool happy
                currDiv.find('.perc-goto-first').text('x');
                currDiv.find('.perc-goto-pre').text('x');

                updateLinkPrevTag(navLoc);
            }


            currDiv.find('.perc-first').attr('aria-label', 'Page ' + (center - 2)).attr('href', genPageUrl(center - 2)).text(center - 2).off('click').on("click",function(event) {
                quickNav(target, center - 2, callback, callbackOptions, event);
            }).parent().show();

            // Unbind so we don't conflict with earlier calls of the same thing.
            currDiv.find('.perc-second').attr('aria-label', 'Page ' + (center - 1)).attr('href', genPageUrl(center - 1)).text(center - 1).off('click').on('click',function(event){
                quickNav(target, center - 1, callback, callbackOptions, event);
            }).parent().show();

            //if not on last page already
            if (navLoc < target.totalPages)
            {
                currDiv.find('.perc-goto-next').attr('aria-label', 'Next page ' + (navLoc + 1)).attr('href', genPageUrl(navLoc + 1)).off('click').on("click",function(event) {
                    quickNav(target, navLoc + 1, callback, callbackOptions, event);
                }).parent().show();
                currDiv.find('.perc-goto-last').attr('aria-label', 'Last page ' + target.totalPages).attr('href', genPageUrl(target.totalPages)).off('click').on('click',function(event) {
                    quickNav(target, target.totalPages, callback, callbackOptions, event);
                }).parent().show();

                updateLinkNextTag(navLoc, target.totalPages);

            } else {//need just any content in hidden link to make validation tool happy
                currDiv.find('.perc-goto-next').text('x');
                currDiv.find('.perc-goto-last').text('x');

                updateLinkNextTag(navLoc, target.totalPages);
            }
        }
        if(2 < target.totalPages)
        {

            currDiv.find('.perc-third').attr('aria-label', 'Page ' + (center + 0)).attr('href', genPageUrl(center + 0)).text(center + 0).off('click').on('click',function(event){
                // We're passing in the same parameters to quickNav that we're getting from it,
                // so it becomes a user initiated recursive function.
                quickNav(target, center + 0, callback, callbackOptions, event);
            }).parent().show();
        }
        if(3 < target.totalPages)
        {
            currDiv.find('.perc-fourth').attr('aria-label', 'Page ' + (center + 1)).attr('href', genPageUrl(center + 1)).text(center + 1).off('click').on('click',function(event){
                quickNav(target, center + 1, callback, callbackOptions, event);
            }).parent().show();
        }
        if(4 < target.totalPages)
        {
            currDiv.find('.perc-fifth').attr('aria-label', 'Page ' + (center + 2)).attr('href', genPageUrl(center + 2)).text(center + 2).off('click').on('click',function(event){
                quickNav(target, center + 2, callback, callbackOptions, event);
            }).parent().show();
        }

        currDiv.find(navLocClass).attr('aria-label', 'Current page ' + navLoc).attr('href', genPageUrl(navLoc)).addClass('perc-current-page').off('click').on('click', function(event){
            event.preventDefault();});

        // Update page count.
        currDiv.find('.perc-page-count .perc-page-count-current').text(navLoc + " " + target.settings.pagingOfText);
        currDiv.find('.perc-page-count .perc-page-count-total').text(target.totalPages + " " + target.settings.pagingPagesText);
    };

    /**
     * This gets called every time a new page is clicked.  This function is a delegator
     * for the other functions which handle work.
     * @name quickNav
     * @param target Element to have paging applied to.
     * @param navLoc Page number to jump to.
     * @param callback Callback function to call before updating navigation.
     *                 Usually a refresh function for grabbing the results on the new page.
     *                 Signature is <code>callback(target, navLoc, callbackOptions)</code>
     * @param callbackOptions Options to pass to the callback function.
     *                        Usually an object containing name-value pairs.
     * @param event Event that triggered this call or null if called during the page loading.
     */
    let quickNav = function(target, navLoc, callback, callbackOptions, event) {

        function jqXHRsuccess(result, status)
        {
            target.settings.query.returnTotalEntries = false; //setting the flag to DO NOT have server return totalEntries regardles on which page we are (will return it only when requested page 1)
            if(0 < parseInt(result.totalEntries, 10))
            {
                target.totalEntries = parseInt(result.totalEntries, 10);
            }
            target.totalPages = Math.ceil((target.totalEntries / target.settings.maxResults));
            if(navLoc > target.totalPages)
            {
                navLoc = target.totalPages;
            }
            else if(navLoc < 1)
            {
                navLoc = 1;
            }
            updateNav(target, navLoc, callback, callbackOptions);
        }
        if (event) { // only if arrived here via onClick on pagination control
            event.preventDefault();
            try{window.history.pushState(null, document.title, genPageUrl(navLoc));}
            catch(e) {
                //None-HTML5 browser - browsing history for pages will not work
            }
        }

        let jqXHR = callback(target, navLoc, callbackOptions, jqXHRsuccess);
    };

    /**
     *
     * Returns the page param value out of the query part of the url.
     * Returns 1 if no number found or less then 1.
     *
     */
    function getPageFromLocation() {

        let pageNumber = 1;
        let queryStr = window.location.search;
        let pageParamName = "page=";

        if (queryStr.length > (pageParamName.length + 1) && 0 < queryStr.indexOf(pageParamName)){ //the string has at least "?page=x"

            let queryParams = queryStr.substring(1).split('&');

            for (let i = 0; i < queryParams.length; i++) { // reassembling without page param

                if (queryParams[i] && queryParams[i].indexOf(pageParamName.length)) {
                    let paramVal = parseInt(queryParams[i].substring(pageParamName.length));
                    if (paramVal && 0 < paramVal){
                        pageNumber = paramVal; //assigning page number only if we got meaningful value
                    }
                    break; // we've got "page" parameter so we done
                }
            }
        }

        return pageNumber;
    }

    /**
     *
     * Sets the page=x param of the query part of the url
     *
     */
    function genPageUrl(page) {

        let queryStr = window.location.search;
        let pageParamStr = "page=" + page;

        if (queryStr.length > "page=".length && 0 < queryStr.indexOf("page=")){ //the string has at least "?page="
            var queryParams = queryStr.substring(1).split('&');
            queryStr = "";
            for (var i = 0; i < queryParams.length; i++) { // reassambling without page param
                if (queryParams[i] && queryParams[i].indexOf("page=")) queryStr = queryStr + (queryStr?"&":"") + queryParams[i];
            }
        } else if (0 < queryStr.length) queryStr = queryStr.substring(1); //removing leading "?"

        queryStr = "?" + pageParamStr + (queryStr?"&":"") + queryStr;

        return window.location.protocol + "//" + window.location.hostname
            + (window.location.port ? (":" + window.location.port) : "")
            + window.location.pathname
            + queryStr
            + window.location.hash;
    }


    /**
     * Builds the necessary html structure to hold the paging.
     * Is called once upon load of first page, and not after.
     */
    function buildPagingStructure()
    {

        // We should add the <link> to the head in this place with appropriate value for the "rel"
        $('head').append($('<link />').attr('href', genPageUrl(1)).attr('rel','prev'));
        $('head').append($('<link />').attr('href', window.location.href + '#z').attr('rel','next'));

        let structure = $('<div />')
            .addClass('perc-pagination-container')
            .append($('<div />')
                .addClass('perc-page-count')
                .attr('role', 'navigation')
                .append($('<span />')
                    .addClass('perc-page-count-current')
                )
                .append(' ')
                .append($('<span />')
                    .addClass('perc-page-count-total')
                )
            )
            .append($('<ul />')
                .addClass('perc-page-nav')
                .addClass('perc-pagination-list')
                .attr('aria-label', 'Pagination') //FIXME: i18n
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a />')
                        .addClass('perc-goto-quick-nav')
                        .addClass('perc-goto-first')
                        .attr('href', window.location.href + '#00')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a />')
                        .addClass('perc-goto-quick-nav')
                        .addClass('perc-goto-pre')
                        .attr('href', window.location.href + '#11')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a>x</a>')
                        .addClass('perc-goto')
                        .addClass('perc-first')
                        .attr('href', window.location.href + '#1')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a>x</a>')
                        .addClass('perc-goto')
                        .addClass('perc-second')
                        .attr('href', window.location.href + '#2')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a>x</a>')
                        .addClass('perc-goto')
                        .addClass('perc-third')
                        .attr('href', window.location.href + '#3')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a>x</a>')
                        .addClass('perc-goto')
                        .addClass('perc-fourth')
                        .attr('href', window.location.href + '#4')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a>x</a>')
                        .addClass('perc-goto')
                        .addClass('perc-fifth')
                        .attr('href', window.location.href + '#5')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a />')
                        .addClass('perc-goto-quick-nav')
                        .addClass('perc-goto-next')
                        .attr('href', window.location.href + '#z')
                    )
                )
                .append($('<li />')
                    .addClass('perc-pagination-list-entry')
                    .append($('<a />')
                        .addClass('perc-goto-quick-nav')
                        .addClass('perc-goto-last')
                        .attr('href', window.location.href + '#zz')
                    )
                )
            );

        pagingStructure = structure;
    }

    function updateLinkPrevTag(navLocation) {

        let links = $("head").find('link');
        $(links).each(function() {

            var linkRel = $(this).attr('rel');

            if(null !== linkRel && linkRel === 'prev') {
                if(1 === navLocation) {
                    $(this).attr('href', genPageUrl(1));
                } else {
                    $(this).attr('href', genPageUrl(navLocation - 1));
                }
            }
        });
    }

    function updateLinkNextTag(navLocation, totalPages) {

        let links = $("head").find('link');
        $(links).each(function() {

            let linkRel = $(this).attr('rel');

            if(null !== linkRel && 'next' === linkRel) {
                if(navLocation === totalPages) {
                    $(this).attr('href', genPageUrl(navLocation));
                } else {
                    $(this).attr('href', genPageUrl(navLocation + 1));
                }
            }
        });
    }
    // Assign to jQuery.
    $.fn.PercResultsPaging = PercResultsPaging;
})(jQuery);
