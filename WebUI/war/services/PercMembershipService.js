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
 * Service call for getting account results.
 */
(function($)
{
    $.PercMembershipService =
    {
        getAccounts: getAccounts,
        activateUser: activateUser,
        blockUser: blockUser,
        deleteUser: deleteUser,
        saveUser: saveUser
        
        
    };

    /**
     * Executes a request to get the accounts registered.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getAccounts(siteName,callback)
    {
        var requestUrl = $.perc_paths.MEMBERSHIP_GET_ALL+ "/" + siteName;
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
     /**
     * Executes a request to Activate the given account.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function activateUser(siteName,userObj, callback)
    {
        var requestUrl = $.perc_paths.MEMBERSHIP_ACT_BLK_DEL_ACCOUNT+ "/" + siteName;
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_PUT,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            userObj
        );
    }
    
     /**
     * Executes a request to block the given account.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function blockUser(siteName,userObj, callback)
    {
        var requestUrl = $.perc_paths.MEMBERSHIP_ACT_BLK_DEL_ACCOUNT+ "/" + siteName;
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_PUT,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }, 
            userObj
        );
    }
    
    /**
     * Executes a request to delete the given account.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function deleteUser(siteName,userEmail, callback)
    {
        var requestUrl = $.perc_paths.MEMBERSHIP_ACT_BLK_DEL_ACCOUNT + "/" + userEmail+ "/" + siteName;
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_DELETE,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }

    /**
     * Executes a request to delete the given account.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function saveUser(siteName,userObject, callback)
    {
        var requestUrl = $.perc_paths.MEMBERSHIP_ACT_SAVE_GROUP+ "/" + siteName;
        $.PercServiceUtils.makeJsonRequest(
            requestUrl,
            $.PercServiceUtils.TYPE_PUT,
            false,
            function(status, result) {
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                } else {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
			userObject
        );
    }
})(jQuery);
