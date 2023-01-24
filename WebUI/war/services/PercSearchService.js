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
