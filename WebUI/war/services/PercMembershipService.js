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
