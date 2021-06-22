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

(function($)
{
    // imports
    var service = $.PercServiceUtils;
    
    $.PercPageService = {
        forceDeletePage : forceDeletePage,
        validateDeletePage : validateDeletePage,
        getNonSEOPages : getNonSEOPages,
        savePageMetadata : savePageMetadata,
        copyPage : copyPage,
        getPagesWithTemplate : getPagesWithTemplate,
        getUnassignedPagesBySite : getUnassignedPagesBySite,
        checkForEmptyMigrationWidgets : checkForEmptyMigrationWidgets,
        clearFlagShowMigrationEmptyMessage : clearFlagShowMigrationEmptyMessage,
        addToMyPages : addToMyPages,
        removeFromMyPages : removeFromMyPages,
        isMyPage : isMyPage,
        getMyContent : getMyContent
    };

    /**
     * Determine if page has empty migration widgets for the given content id. Invoke callback only if it does.
     * Note: any returned errors are ignored.
     *
     * @param {Object} contentId
     * @param {Object} callback
     */
    function checkForEmptyMigrationWidgets (contentId, onPageHasEmptyMigrationWidgets) {
        var url = $.perc_paths.MIGRATION_EMPTY_FLAG + "/" + contentId;
        function serviceCallback (status, results)
        {
            if (status === service.STATUS_SUCCESS)
            {
               if (results.data === true) 
               {
                   onPageHasEmptyMigrationWidgets();
               }
            }
            // nothing to do in case of error
        }
        service.makeJsonRequest(url, service.TYPE_GET, false, serviceCallback);
    }
    
    /**
     * Tell server to clear the migration empty flag.
     * Note: any returned errors are ignored.
     * 
     * @param {Object} contentId
     * @param {Object} callback
     */
    function clearFlagShowMigrationEmptyMessage (contentId) {
        var url = $.perc_paths.CLEAR_MIGRATION_EMPTY_FLAG + "/" + contentId;
        function serviceCallback (status, results)
        {
            //if (status === service.STATUS_ERROR)
            //{
                // nothing to do in case of error
            //}
        }
        service.makeJsonRequest(url, service.TYPE_POST, false, serviceCallback);
    }
    
    /**
     * Deletes a page without validation.
     * @param id of the page we want to delete.
     * @param callback handles success.
     * @param errorCallBack handles errors.
     */
    function forceDeletePage(id, callback, errorCallBack)
    {
        $.ajax(
            {
                url: $.perc_paths.PAGE_FORCE_DELETE + "/" + id, 
                type: 'GET',
                success: callback,
                error: errorCallBack
            });
        
        callback();
    }

    /**
     * Validates that a page may be deleted by the current user.
     * @param id of the page we want to delete.
     * @param callback handles success.
     * @param errorCallBack handles errors and validation warnings.
     */
    function validateDeletePage(id, callback, errorCallBack)
    {
        $.ajax(
            {
                url: $.perc_paths.PAGE_VALIDATE_DELETE + "/" + id, 
                type: 'GET',
                success: callback,
                error: errorCallBack
            });
    }

    /**
     * Retrieves page seo statistics based on specified path, workflow, state, and severity.
     * @param path {string} the finder path where the search should be based in,
     *  cannot be <code>null</code>.
     * @param workflow {string} the workflow to be used for search, cannot be <code>null</code>.
     * @param state {string} the workflow state from the specified workflow, may be
     * <code>null</code> in which case items in any workflow state will be returned.
     * @param severity {string} the minimum seo severity level of the pages.  Pages with a
     * severity greater than or equal to this severity will be returned.
     * @param keyword {string} the keyword to search for , may be <code>null</code> or
     * empty.
     * @param callback {function} the callback function to be called when request 
     * is done. First arg is status second is the following object:
     * <pre>
     *
     * {"SEOStatistics":
     *    [{"issues":["DEFAULT_TITLE","MISSING_DESCRIPTION"],
     *      "linkTitle":"Home",
     *      "path":"\/Sites\/Test\/index",
     *      "severity":100,
     *      "title":"Home",
     *      "description":"The home page"}
     *    ]
     * }
     * </pre>         
     */                   
    function getNonSEOPages(path, workflow, state, severity, keyword, callback)
    {
       if(state === null)
          state = "";
       if(keyword === null)
          keyword = "";   
       var obj = {NonSEOPagesRequest: {
          keyword: keyword,
          path: path,
          workflow: workflow,
          state: state,
          severity: severity
       }};
       $.PercServiceUtils.makeJsonRequest(
          $.perc_paths.PAGE_NONSEO,
          $.PercServiceUtils.TYPE_POST,
          false,
          function(status, result)
          {
             if(status === $.PercServiceUtils.STATUS_SUCCESS)
             {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
             }
             else
             {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
             }
          },
          obj
       
       );
    }
    
    /**
     * Executes a request to update the page title value 
     * @param pageId the id of the page
     */
    function savePageMetadata(pageId,callback)
    {
       var getUrl = $.perc_paths.SAVE_PAGE_METADATA + "/" + pageId;
       
       var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,results.data);
            }
        };        
        $.PercServiceUtils.makeRequest(getUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback);

    }     
    
    /**
     * Executes a request to copy a page 
     * @param pageId the id of the page
     */
    function copyPage(pageId, callback)
    {
       var url = $.perc_paths.PAGE_COPY + "/" + pageId + "?addToRecent=true";
       // url example: http://localhost:9992/Rhythmyx/services/pagemanagement/page/copy/16777215-101-324
       
       $.PercServiceUtils.makeRequest(
            url,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS){
                    callback($.PercServiceUtils.STATUS_SUCCESS, result);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
    	            callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },null,null,null,true
        );
    }     
    
    /**
     * Retrieves the pages that use template.
     * @param templateId {string} the template id.
     * @param serviceParams {object}. the parameters for the service
     * The object should look like this
     * serviceParams = {
     *     startIndex : 1,
     *     maxResults : 5,
     *     sortColumn : 'name',
     *     sortOrder : 'asc'
     * };
     * @param callback {function} the success callback function.
     * @param errorCallback {function} the error callback function.
     */                                 
    function getPagesWithTemplate(templateId, serviceParams, callback, errorCallback)
    {
        // If there are missing values in serviceParams, use default ones
        var defaultServiceParams = {
            startIndex : 1,
            maxResults : 5,
            sortColumn : 'name',
            sortOrder : 'asc',
            pageId : null
        };
        $.extend(defaultServiceParams, serviceParams);
        
        var requestURL = $.perc_paths.PAGES_WITH_TEMPLATE + '/' + templateId;
        requestURL += '?startIndex=' + defaultServiceParams.startIndex;
        requestURL += '&maxResults=' + defaultServiceParams.maxResults;
        requestURL += '&sortColumn=' + defaultServiceParams.sortColumn;
        requestURL += '&sortOrder=' + defaultServiceParams.sortOrder;
        if (defaultServiceParams.pageId != null)
            requestURL += '&pageId=' + defaultServiceParams.pageId;
        
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.PagedItemList);
                }
            }
        );
    }
    
    function getUnassignedPagesBySite(siteName, startIndex, maxResults, callback){
        var requestURL = $.perc_paths.UNASSIGNED_PAGES_BY_SITE + "/" + siteName + "?startIndex=" + startIndex + "&maxResults=" + maxResults;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
            }
        );
    }

    /**
     * Makes a call to the server to add supplied pageId to logged in user pages.
     * @param {Object} pageId assumed to be string format of page guid.
     * @param {Object} callback function that gets called after AJAX request is completed, the first parameter is status and the second 
     * parameter is server supplied message for success case and extracted error message for the error case.
     */
    function addToMyPages(pageId, callback){
        var requestURL = $.perc_paths.ADD_TO_MYPAGES + "/" + pageId;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_PUT,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
            }
        );
    }
    
    /**
     * Makes a call to the server to remove supplied pageId from logged in user pages.
     * @param {Object} pageId assumed to be string format of page guid.
     * @param {Object} callback function that gets called after AJAX request is completed, the first parameter is status and the second 
     * parameter is server supplied message for success case and extracted error message for the error case.
     */
    function removeFromMyPages(pageId, callback){
        var requestURL = $.perc_paths.REMOVE_FROM_MYPAGES + "/" + pageId;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_DELETE,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
            }
        );
    }
    
    /**
     * Makes a call to the server to check whether supplied pageId is in logged in user pages.
     * @param {Object} pageId assumed to be string format of page guid.
     * @param {Object} callback function that gets called after AJAX request is completed, the first parameter is status and the second 
     * parameter is true or false success case and extracted error message for the error case.
     */
    function isMyPage(pageId, callback){
        var requestURL = $.perc_paths.IS_MY_PAGE + "/" + pageId;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
            }
        );
    }

     /**
     * Makes a call to the server to get my content.
     * @param {Object} callback function that gets called after AJAX request is completed, the first parameter is status and the second 
     * parameter is error message for the error case and for success case it will be a array of PSItemProperties returned by server.
     */
    function getMyContent(callback){
        var requestURL = $.perc_paths.MY_CONTENT;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if (status === $.PercServiceUtils.STATUS_ERROR)
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
                else
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
            }
        );
        
    }
})(jQuery);
