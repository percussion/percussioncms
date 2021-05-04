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