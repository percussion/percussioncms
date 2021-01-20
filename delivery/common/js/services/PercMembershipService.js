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
    // Prevent AJAX caching bug in IE
    $.ajaxSetup({
        cache: false 
    });
    
    $.PercMembershipService = {
        register : register,
        getUser : getUser,
        login: login,
        validateResetPwKey : validateResetPwKey,
        resetPassword : resetPassword,
        resetPwRequest : resetPwRequest,
        confirmRegistration : confirmRegistration
    };
    
    /**
     * Register a user given its data.
     * @param registerObj
     * @param callback
     */
    function register(registerObj, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/user";

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                callback(status,results.data);
            }
            else{
              let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
              callback(status, defMsg);
            }
            
         }, registerObj); 
    }
    
    /**
     * Retrieves user's data from the server given a session's Id.
     * @param sessionId String
     * @param function callback
     */
    function getUser(sessionId, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/session";

        let sessionIdData = {"sessionId" : sessionId};
                            
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback(status,results.data);
            }
            else
            {
                let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
            }
        }, sessionIdData);
    }
    
    /**
     * Given an email and password, authenticates the user and if successful, returns the id of the session holding the data.
     * @param loginObj. Contains fields "email" and "password". The email and unencrypted password
     * of the registered user trying to login.
     * @param callback
     */
    function login(loginObj, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/login";
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
            }
        }, loginObj);
    }
    
    /**
     * Given an email and password, authenticates the user and if successful, updates the password and returns the id of the session holding the data
     * @param email The email of the user
     * @param newPassword The unencrypted password of the user.
     * @param resetkey
     * @param callback
     */
    function resetPassword(email, newPassword, resetkey, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/pwd/reset/" + resetkey;
        let pwResetObj = {"email":email, "password":newPassword};

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
            }
        }, pwResetObj);
    }
    
    /**
     * Given an email authenticates the user and if successful, system sends an email to user.
     * @param email The email of the user
     * @param pwResetPageUrl The absolute link to the reset password page.
     * @param callback
     */
    function resetPwRequest(email, pwResetPageUrl, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/pwd/requestReset";
        //TODO needs to be fixed...?
        pwResetPageUrl = window.location.protocol + '//' + window.location.host + pwResetPageUrl;
        
        let pwResetObj = {"email":email, "redirectPage":pwResetPageUrl};

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
            }
        }, pwResetObj);
    }

    /**
     * Authenticates the key and if valid, returns the user's email address.
     * @param key A one time usable key
     * @param callback
     */
    function validateResetPwKey(key, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/pwd/validate/" + key;
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
                
            }
        });
    }    

    /**
     * Confirms the users registration by sending the key to the server.
     * @param key A one time usable key
     * @param callback
     */
    function confirmRegistration(key, callback)
    {
        let serviceUrl = "/perc-membership-services/membership/registration/confirm/" + key;
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	let defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
                
            }
        });
    }     
    
})(jQuery);