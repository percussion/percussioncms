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

(function($){

    
    /**
     * Constant for admin permission
     */
    var ACCESS_ADMIN = "ADMIN";

    $.PercCategoryService =  {
        ACCESS_ADMIN        	: ACCESS_ADMIN,
        getCategories			: getCategories,
        editCategories			: editCategories,
        getTabLockData			: getTabLockData,
        lockCategoryTab			: lockCategoryTab,
        removeCatTabLock		: removeCatTabLock,
        publishToDTS			: publishToDTS
    };

    
    function getCategories(sitename, callback){
         
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.CAT_ALL + "/" + sitename,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } 
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
    function editCategories(categories, sitename, callback) {
        
    	$.PercServiceUtils.makeJsonRequest(
            $.perc_paths.CAT_UPDATE + "/" + sitename,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } 
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            categories
        );
    }

    function getTabLockData(callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.CAT_LOCK_INFO,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } 
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
    function lockCategoryTab(callback) {
        var currentDate = (new Date()).toString();
         
     	$.PercServiceUtils.makeJsonRequest(
             $.perc_paths.CAT_LOCK_TAB + "/" + currentDate,
             $.PercServiceUtils.TYPE_POST,
             false,
             function(status, result) {
                 if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                     callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                 } 
                 else {
                     var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                     callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                 }
             }
         );
    }
    
    function removeCatTabLock(callback) {
     	$.PercServiceUtils.makeJsonRequest(
             $.perc_paths.CAT_REMOVE_TAB_LOCK,
             $.PercServiceUtils.TYPE_POST,
             false,
             function(status, result) {
                 if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                     callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                 } 
                 else {
                     var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                     callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                 }
             }
         );
    }
    
    function publishToDTS(deliveryServer, sitename, callback) {
        if (sitename==null || typeof sitename === "undefined" || sitename.length === 0)
        {
            sitename="all";
        }
    	$.PercServiceUtils.makeJsonRequest(
            $.perc_paths.CAT_UPDATE_IN_DTS + "/" + sitename + "/" + deliveryServer,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS);
                } 
                else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
})(jQuery);
