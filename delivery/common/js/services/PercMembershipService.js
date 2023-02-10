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
        var serviceUrl = "/perc-membership-services/membership/user";

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                callback(status,results.data);
            }
            else{
              var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/session";

        var sessionIdData = {"sessionId" : sessionId};
                            
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback(status,results.data);
            }
            else
            {
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/login";
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/pwd/reset/" + resetkey;
        var pwResetObj = {"email":email, "password":newPassword};

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/pwd/requestReset";
        //TODO needs to be fixed...?
        pwResetPageUrl = window.location.protocol + '//' + window.location.host + pwResetPageUrl;
        
        var pwResetObj = {"email":email, "redirectPage":pwResetPageUrl};

        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/pwd/validate/" + key;
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
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
        var serviceUrl = "/perc-membership-services/membership/registration/confirm/" + key;
        
        $.PercServiceUtils.makeXdmJsonRequest(null, serviceUrl, $.PercServiceUtils.TYPE_POST, function(status, results)
        {
        	if(status === $.PercServiceUtils.STATUS_SUCCESS)
        	{
        		callback(status,results.data);
    		}
            else
            {
            	var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(status, defMsg);
                
            }
        });
    }     
    
})(jQuery);
