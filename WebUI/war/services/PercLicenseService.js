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