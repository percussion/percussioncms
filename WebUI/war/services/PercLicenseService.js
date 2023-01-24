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

(function($) {
    $.PercLicenseService =  {
        activate: activate,
        getStatus: getStatus,
        getNetSuiteMethod : getNetSuiteMethod,
        getAllModuleLicenses : getAllModuleLicenses,
        getModuleLicense:getModuleLicense,
        saveModuleLicense : saveModuleLicense
    };

    /**
     * Gets the method information and checks that the url returns a HTTP 200 status.
     * @param callback function to be called after license activation is performed.
     * @param String methodName: the name of the method to get the url. Eg: REGISTER_URL
     */
    function getNetSuiteMethod(callback, methodName)
    {
        var url = $.perc_paths.LICENSE_GET_NETSUITE_METHOD + '/' + methodName;
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defMsg);
                }
            }
        );
    }
    
    /**
     * Perform the license activation.
     * @param callback function to be called after license activation is performed.
     * @param String activationCode:
     */
    function activate(callback, activationCode)
    {
        var url = $.perc_paths.LICENSE_ACTIVATE + '/' + activationCode;
        $.PercServiceUtils.makeJsonRequest(
            url,
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
                    var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defMsg);
                }
            },
            undefined,
            function(status)
            {
                // On abort(timeout) callback
                callback(status);
            }
        );
    }
    
    /**
     * Retrieves the license information status.
     * @param function
     * @param boolean ignoreCache
     */
    function getStatus(callback, ignoreCache)
    {
        var url = $.perc_paths.LICENSE_GET_INFORMATION;
        if (ignoreCache !== undefined && ignoreCache === true)
        {
            ignoreCache = 'true';
        }
        else
        {
            ignoreCache = 'false';
        }
        url += '/' + ignoreCache;

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.licenseStatus);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(status, defaultMsg);
                }
            },
            undefined,
            function(status)
            {
                // On abort(timeout) callback
                callback(status);
            }
        );
    }
    /**
     * Gets the all module licenses info from server.
     * @param function
     * @param boolean ignoreCache
     */
    function getAllModuleLicenses(callback)
    {
        getModuleLicenses("all", callback);
    }
    
    /**
     * Gets the named module license info from server.
     * @param moduleName assumed not blank
     * @param function
     * @param boolean ignoreCache
     */
    function getModuleLicense(moduleName, callback)
    {
        getModuleLicenses(moduleName, callback);
    }

    function getModuleLicenses(type, callback)
    {
        var url = $.perc_paths.LICENSE_GET_MODULE + type;
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(true, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
            },
            undefined,
            function(status)
            {
                // On abort(timeout) callback
                callback(false, I18N.message("perc.ui.licence.service@Lisensing Service Timed Out"));
            }
        );
        
    }    
    /**
     * Perform the license activation.
     * @param callback function to be called after license activation is performed.
     * @param String activationCode:
     */
    function saveModuleLicense(moduleLicenseData, callback)
    {
        var url = $.perc_paths.LICENSE_SAVE_MODULE;
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(true, result.data);
                }
                else
                {
                    var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defMsg);
                }
            },
            moduleLicenseData,
            function(status)
            {
                // On abort(timeout) callback
                callback(false,I18N.message("perc.ui.lisence.service@Module License Save Failed"));
            }
        );
    }
})(jQuery);
