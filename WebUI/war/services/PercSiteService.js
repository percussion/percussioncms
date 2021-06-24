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
    $.PercSiteService = {
        getSiteProperties: getSiteProperties,
        updateSiteProperties: updateSiteProperties,
        copySite: copySite,
        copySiteInfo: copySiteInfo,
        getTemplates: getTemplates,
        getBaseTemplates: getBaseTemplates,
        getSites: getSites,
        validateCopySiteFolders: validateCopySiteFolders,
        createSiteFromUrl: createSiteFromUrl,
        createSiteFromUrlAsync: createSiteFromUrlAsync,
        createSiteFromUrlStatus: createSiteFromUrlStatus,
        createSiteFromUrlResult: createSiteFromUrlResult, 
        isSiteBeingImported : isSiteBeingImported,
        getSaaSSiteNames : getSaaSSiteNames
    };

    /**
     * Update site properties. This is a JSON only call and returns json in
     * the callback.
     * @param siteProps {Object} the SiteProperties object. Cannot be <code>null</code>.
     * @param callback the callback function to be called when the request completes.
     */
    function updateSiteProperties(siteProps, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_UPDATE_PROPERTIES, $.PercServiceUtils.TYPE_POST, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        },
        siteProps);
    }

    /**
     * Get the properties for the site specified.
     * @param site the sitename, assumed not <code>null</code> or empty.
     * @param callback function to be called when section is retrieved, the
     * section object will be the sole argument passsed to the callback.
     */
    function getSiteProperties(site, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_GET_PROPERTIES + "/" + site, $.PercServiceUtils.TYPE_GET, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
    }

    /**
     * Copy site.
     * @param postObject {object} the json object to be passed to the server, assumed not <code>null</code> or empty.
     * Format: {"SiteCopyRequest":{"srcSite":"Site1","copySite":"Site1-copy15","assetFolder":""}}
     * @param callback the callback function to be called when the request completes.
     */
    function copySite(postObject, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_COPY, $.PercServiceUtils.TYPE_POST, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                var defaultCode = $.PercServiceUtils.extractGlobalErrorCode(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, defaultCode);
            }
        },
        postObject);
    }

    /**
     * Validate Copy Site asset folders
     * @param postObject {object} the json object to be passed to the server, assumed not <code>null</code> or empty.
     * Format: {"SiteCopyRequest":{"srcSite":"Site1","copySite":"Site1-copy15","assetFolder":""}}
     * @param callback the callback function to be called when the request completes.
     */
    function validateCopySiteFolders(postObject, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_COPY_VALIDATE_FOLDERS, $.PercServiceUtils.TYPE_POST, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                var defaultCode = $.PercServiceUtils.extractGlobalErrorCode(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, defaultCode);
            }
        },
        postObject);
    }

    /**
     * Get info of copy site in progress.
     * @param callback the callback function to be called when the request completes.
     */
    function copySiteInfo(callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_COPY_INFO, $.PercServiceUtils.TYPE_GET, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
    }

    /**
     * Retrieves a list of templates for the given site.
     * @param siteName the name of the site for which we want a list of templates
     * @param callback the callback function
     * @return array of objects representing templates used in this site:
     * <pre>
     *    {"TemplateSummary":[
     *     {    "id":"16777215-101-1957",
     *         "imageThumbPath":"\/Rhythmyx\/rx_resources\/images\/TemplateImages\/AnySite\/perc.base.cClampBottom_Thumb.png",
     *         "label":"\"C\" Clamp Bottom",
     *         "name":"template name",
     *         "readOnly":false,
     *         "sourceTemplateName":"perc.base.cClampBottom"}]}
     * </pre>
     */
    function getTemplates(siteName, callback, widgetDefId)
    {
        var sendURL;

        if (widgetDefId != null)
        {
            sendURL = $.perc_paths.TEMPLATES_BY_SITE + "/" + siteName + "/" + widgetDefId;
        }
        else
        {
            sendURL = $.perc_paths.TEMPLATES_BY_SITE + "/" + siteName + "";
        }

        $.PercServiceUtils.makeJsonRequest(
        sendURL, $.PercServiceUtils.TYPE_GET, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
    }
    
    function getBaseTemplates(type, callback){
        var templUrl = $.perc_paths.TEMPLATES_READONLY + "?type=" + type;
        //Load regular base templates
        $.PercServiceUtils.makeJsonRequest(templUrl,$.PercServiceUtils.TYPE_GET,false,function(status, result){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(true, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
        });
     }

    /**
     * Retrieves a list of all sites in the CM sytem.
     * @param callback the callback function that will pass an array
     * of objects with the following properties:
     * <pre>
     *    sitename
     * </pre>
     */
    function getSites(callback)
    {
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITES_ALL + "/", $.PercServiceUtils.TYPE_GET, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });
    }

    /**
     * Crete a site, template and page based in a given URL.
     * @param siteProps {Object} The basic properties needed to ceate the site. Cannot be <code>null</code>.
     * The object will have the following structure:
     * <pre>
     *     {
     *         "name" : "a valida site name",
     *         "baseUrl" : "URL"
     *     }
     * </pre>
     * @param callback the callback function to be called when the request completes.
     */
    function createSiteFromUrl(siteProps, callback)
    {
        // Redefine temporarly timeout to be 10 minutes (importing proccess might take too long)
        $.ajaxSetup({timeout: 600000});

        // Before sending the data to the server-side service, we have to adapt it
        var serviceParam = {Site: siteProps};
        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SITE_CREATE_FROM_URL, $.PercServiceUtils.TYPE_POST, false, function(status, result)
        {
            // Re-Set default timeout
            $.ajaxSetup({
                timeout: 60000
            });
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        },
        serviceParam);
    }

    /**
     * Starts a site creation job (async), given a site properties specification
     * @param Object siteProps
     * @param function callback.
     */
    function createSiteFromUrlAsync(siteProps, callback)
    {
        // Before sending the data to the server-side service, we have to adapt it
        var serviceParam = {Site: siteProps};

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITE_CREATE_FROM_URL_ASYNC,
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
                    var defaultMsg = I18N.message("perc.ui.site.service@Unexpected Error Importing");
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            serviceParam
        );
    }

    /**
     * Retrieves the status of a previously started site creation job.
     * @param String siteCreationJobId returned by a createTemplateFromUrlStatus
     * @param function callback.
     */
    function createSiteFromUrlStatus(siteCreationJobId, callback)
    {
        var jobId  = siteCreationJobId.Long === undefined ? siteCreationJobId : siteCreationJobId.Long;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.JOB_STATUS + '/' + jobId,
            $.PercServiceUtils.TYPE_GET,
            false,
            function templateCreateFromUrlAsyncCallback(status, result)
            {
                var data,
                    serviceStatus = $.PercServiceUtils.STATUS_SUCCESS;

                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    // NOTE that the progress could be -1, but that is something that the user of
                    // the service must check, the request has been successful
                    data = result.data.asyncJobStatus;
                }
                else
                {
                    // If an unhandled error happened in the server, the answer to the request will
                    // be an error
                    serviceStatus = $.PercServiceUtils.STATUS_ERROR;
                    data = I18N.message("perc.ui.site.service@Unexpected Error Importing");
                }

                callback(serviceStatus, data);
            }
        );
    }

    /**
     * Retrieves the result of a previously started site creation job.
     * @param String siteCreationJobId returned by a createTemplateFromUrlStatus
     * @param function callback.
     */
    function createSiteFromUrlResult(siteCreationJobId, callback)
    {
        var jobId  = siteCreationJobId.Long === undefined ? siteCreationJobId : siteCreationJobId.Long;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITE_CREATE_FROM_URL_RESULT + '/'+ jobId,
            $.PercServiceUtils.TYPE_GET,
            false,
            function templateCreateFromUrlAsyncCallback(status, result)
            {
                var data,
                    serviceStatus = $.PercServiceUtils.STATUS_SUCCESS;

                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    data = result.data;
                }
                else
                {
                    // If an unhandled error happened in the server, the answer to the request will
                    // be an error
                    serviceStatus = $.PercServiceUtils.STATUS_ERROR;
                    data = I18N.message("perc.ui.site.service@Unexpected Error Importing");
                }

                callback(serviceStatus, data);
            }
        );
    }
    
    /**
     * Checks if the given site is being imported.
     * @param String sitename the name of the site.
     */
    function isSiteBeingImported(sitename, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITE_IS_BEING_IMPORTED + '/'+ sitename,
            $.PercServiceUtils.TYPE_GET,
            false,
            function (status, result)
            {
                var data, serviceStatus = $.PercServiceUtils.STATUS_SUCCESS;
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                	data = result.data;
                }
                else
                {
                    // If an unhandled error happened in the server, the answer to the request will
                    // be an error
                    serviceStatus = $.PercServiceUtils.STATUS_ERROR;
                    data = I18N.message("perc.ui.site.service@Unexpected Error Importing");
                }
                callback(serviceStatus, data);
            }
        );
    }
    
    /**
     * Retrieves a map of all saas sitenames and associated config file names.
     * @param filterUsedSites if it is true, then returns unused sites only
     * the map may be empty if no valid sites found.
     * Uses jQuery deferred, on success resolves with site name map and on
     * failure rejects with error message.
     * 
     */
    function getSaaSSiteNames(filterUsedSites)
    {
        var deferred = $.Deferred();

        $.PercServiceUtils.makeJsonRequest(
        $.perc_paths.SAAS_SITES_NAMES + "?filterUsedSites=" + filterUsedSites, $.PercServiceUtils.TYPE_GET, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                deferred.resolve(result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                deferred.reject(defaultMsg);
            }
        });
        return deferred.promise();
    }
    
    
})(jQuery);
