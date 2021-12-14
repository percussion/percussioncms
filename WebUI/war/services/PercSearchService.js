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

/****
 * Service call for getting search results.
 */
(function($)
{
    $.PercSearchService =
    {
        getSearchResult: getSearchResult,
        getAsyncSearchResult: getAsyncSearchResult,
        getAsyncSearchExtendedResult:getAsyncSearchExtendedResult
    };

    /**
     * Executes a request to get the search results based on entered keyword.
     * @param setUrl the url used to get the search results
     * @param serviceCallback (function) callback function to be invoked when ajax call returns
     */
function getSearchResult(searchCriteriaObj, callback)
    {
	   var setUrl = $.perc_paths.FINDER_SEARCH;

       var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(false,defaultMsg);
            }
            else
            {
                callback(true,results.data);
            }
         };

        $.PercServiceUtils.makeJsonRequest(setUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, searchCriteriaObj);

    }	

function getAsyncSearchResult(searchCriteriaObj, callback)
{
    var setUrl = $.perc_paths.SEARCH_PAGE_ASSETS_BY_STATUS;
    
    var serviceCallback = function(status, results){
        if(status === $.PercServiceUtils.STATUS_ERROR)
        {
            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
            callback(false,defaultMsg);
        }
        else if(status === $.PercServiceUtils.STATUS_ABORT)
        {
            callback(false,I18N.message('perc.ui.search.service@Server Taking Too Long'));
        }
        else
        {
            callback(true,results.data);
        }
    };
    $.PercServiceUtils.makeJsonRequest(setUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, searchCriteriaObj, serviceCallback);
    
}
function getAsyncSearchExtendedResult(searchCriteriaObj, callback)
{
    var setUrl = $.perc_paths.FINDER_SEARCH + '/extendedresults';
    
    var serviceCallback = function(status, results){
        if(status === $.PercServiceUtils.STATUS_ERROR)
        {
            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
            callback(false,defaultMsg);
        }
        else if(status === $.PercServiceUtils.STATUS_ABORT)
        {
            callback(false,I18N.message('perc.ui.search.service@Server Taking Too Long'));
        }
        else
        {
            callback(true,results.data);
        }
    };
    $.PercServiceUtils.makeJsonRequest(setUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, searchCriteriaObj, serviceCallback);
    
}

})(jQuery);
