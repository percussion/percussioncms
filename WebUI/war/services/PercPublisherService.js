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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/***
 * Service calls for the Publish page.
 */
var regions;
var publishingServer;
(function($)
{
    $.PercPublisherService = function()
    {
        return {
            publishSite: publishSite,
            getJobStatus: getJobStatus,
            getPublishingLogs: getPublishingLogs,
            getPublishingLogDetails: getPublishingLogDetails,
            purgeJob: purgeJob,
            getSitePublishProperties: getSitePublishProperties,
            updateSitePublishProperties: updateSitePublishProperties,
            getServersList: getServersList,
            getServerProperties: getServerProperties,
            createUpdateSiteServer: createUpdateSiteServer,
            getAvailableDrivers: getAvailableDrivers,
            getAvailableDeliveryServers: getAvailableDeliveryServers,
            deleteSiteServer: deleteSiteServer,
            stopPubJob: stopPubJob,
            getLocalFolderPath: getLocalFolderPath,
            getIncrementalItems:getIncrementalItems,
            getIncrementalRelatedItems:getIncrementalRelatedItems,
            incrementalPublishSite: incrementalPublishSite,
            publishIncrementalWithApproval:publishIncrementalWithApproval,
            isEC2InstanceCheck :isEC2InstanceCheck,
            getAvailableRegions: getAvailableRegions,
            getAvailablePublishingServer:getAvailablePublishingServer,
            PUBLISHER_JOB_STATUS_FORBIDDEN: "FORBIDDEN",
            PUBLISHER_JOB_STATUS_BADCONFIG: "BADCONFIG"
        };
    };


    function isEC2InstanceCheck(callback) {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + 'isEC2Instance';
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET,false, callback);
    }

    /**
     *
     * @param siteName (string) the id of the site to be published
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function publishSite(siteName, serverName, callback)
    {
        var pubUrl = $.perc_paths.SITE_PUBLISH + "/" + siteName + "/" + serverName;

        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(pubUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Gets current job status for publish page.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getJobStatus(siteId, callback)
    {
        var pubUrl = $.perc_paths.PUBLISH_CURRENT_STATUS + "/" + siteId;

        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(pubUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback, "");
    }

    /*
     * A service to get the list of all available logs for a given site.
     * @param pubObject : Contains paramerters like day, serverId, how many logs to be return and siteId.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getPublishingLogs(pubObject, callback)
    {
        var pubUrl = $.perc_paths.PUBLISH_LOGS + "/";
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(pubUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, pubObject);
    }

    /**
     * Gets the FTP information from the server
     * @param site the sitename, assumed not <code>null</code> or empty.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getSitePublishProperties(site, callback)
    {
        $.PercServiceUtils.makeJsonRequest($.perc_paths.SITE_GET_PUBLISH_PROPERTIES + "/" + site, $.PercServiceUtils.TYPE_GET, false, function(status, result)
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
     * Send the FTP information to the server
     * @param sitePublishObj the publishing options defined for the selected site.
     *   sitePublishObj = {
     *       SitePublishProperties: {
     *           id : siteId,
     *           siteName : selectedSite,
     *           ftpServerName : ftpAddress,
     *           ftpUserName : ftpUser,
     *           ftpPassword : ftpPassword,
     *           privateKey: privateKey,
     *           ftpServerPort : ftpPort,
     *           deliveryRootPath : ftpLocation,
     *           publishType : publishType
     *           enableSiteSecurity: enableSiteSecurity
     *       }
     *   };
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function updateSitePublishProperties(sitePublishObj, callback)
    {
        $.PercServiceUtils.makeJsonRequest($.perc_paths.SITE_UPDATE_PUBLISH_PROPERTIES, $.PercServiceUtils.TYPE_POST, false, function(status, result)
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
        }, sitePublishObj);
    }


    /*
     * @param siteId (jobId) to get details of
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getPublishingLogDetails(jobId, callback)
    {
        var pubUrl = $.perc_paths.PUBLISH_LOGS_DETAILS + "/";
        var obj = {
            SitePublishLogDetailsRequest: {
                jobid: jobId
            }
        };
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(pubUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, obj);
    }

    /* @param jobList (List of string) of jobs ids to purge
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function purgeJob(jobList, callback)
    {
        var pubUrl = $.perc_paths.PUBLISH_PURGE + "/";
        var dataType = "text";
        var contentType = "application/json";
        var obj = {
            SitePublishPurgeRequest: {
                jobids: jobList
            }
        };
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(pubUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, obj, contentType, dataType);
    }

    /** Get the list of servers for supplied 'site'
     * @param siteId : The id of currently loaded site
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getServersList(siteId, callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + siteId;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {

                callback(true, [results.data, results.textstatus, siteId]);
            }
        };

        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Get the properties for selected 'server'
     * @param siteId (String): the id of selected Site
     * @param serverId (String): the id of currently selected server
     * @param callback (function): callback function to be invoked when ajax call returns
     */
    function getServerProperties(siteId, serverId, callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + siteId + "/" + serverId;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                var pubserver = results.data;
                callback(true, [pubserver, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     *  A service to create/update a new server for a given site based on 'serverId' value
     *  @param {String} 'server' : Name or Id of the site for which server is being created (name for create, Id for update)
     *  @param {Object} 'propObj' : Object containing all the properties for given server
     *  @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function createUpdateSiteServer(siteName, server, propObj, callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + siteName + "/" + server;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                //var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(false, results);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        //If serverId is not avaliable - Create a New Server
        if (propObj.serverInfo.serverId === null || propObj.serverInfo.serverId === '' || propObj.serverInfo.serverId === 'undefined')
        {
            $.PercServiceUtils.makeJsonRequest(serviceUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback, propObj);
        }
        //If serverId is avaliable - Update a Server
        else
        {
            $.PercServiceUtils.makeJsonRequest(serviceUrl, $.PercServiceUtils.TYPE_PUT, false, serviceCallback, propObj);
        }
    }

    /**
     *  A service to Delete a Server from specified Site
     *  @param {String} 'siteId' : ID of currently selected Site
     *  @param {Object} 'serverId' : ID of currently selected server
     *  @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function deleteSiteServer(siteId, serverId, callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + siteId + "/" + serverId;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                callback(false, defaultMsg);
                //callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_DELETE, false, serviceCallback);
    }



    /**
     * A method to get the available regions for EC2 Server
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function getAvailableRegions(callback)
    {
        if(regions != null){
            callback(true, [regions, ""]);
        }
        var serviceUrl = $.perc_paths.SERVER_DETAILS + 'availableRegions';
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {

                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                regions=results.data;
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }


    function getAvailablePublishingServer(callback,serverType)
    {

        var serviceUrl = $.perc_paths.SERVER_DETAILS + 'availablePublishingServer'+ "/" + serverType;
        
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {

                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                publishingServer=results.data;
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * A method to get the available database drivers
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function getAvailableDrivers(callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + 'availableDrivers';
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * A method to get the available Delivery Servers
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function getAvailableDeliveryServers(callback)
    {
        var serviceUrl = $.perc_paths.SERVER_DETAILS + 'availableDeliveryServers';
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * A service to stop the running publishing server
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function stopPubJob(pubId, callback)
    {
        var serviceUrl = $.perc_paths.STOP_PUB_SERVER + pubId;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(serviceUrl, $.PercServiceUtils.TYPE_POST, false, serviceCallback);
    }

    /**
     * A service to get the local default path(folder location) for the server
     * @param {function} 'callback' : Callback function to execute when ajax call returns
     */
    function getLocalFolderPath(siteId, type, callback)
    {

        var serviceUrl = $.perc_paths.DEFAULT_PUB_PATH  + siteId + '/File/Local/' +type.toUpperCase();
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Function to get incremental items for the given site and server.
     * @param {String} siteName assumed to be a valid site name, if not server throws an error.
     * @param {String} serverName assumed to be a valid server name, if not server throws an error.
     * @param {Function} callback assumed to be a function and calls this function with two parameters
     * boolean status if false, the second argument will be a string representing error message
     * Object results data that comes from the server.
     */
    function getIncrementalItems(siteName, serverName, startIndex, pageSize, callback)
    {
        _getIncrementalOrRelatedItems(true, siteName, serverName, startIndex, pageSize, callback);
    }

    /**
     * Function to get incremental related items for the given site and server.
     * @param {String} siteName assumed to be a valid site name, if not server throws an error.
     * @param {String} serverName assumed to be a valid server name, if not server throws an error.
     * @param {Function} callback assumed to be a function and calls this function with two parameters
     * boolean status if false, the second argument will be a string representing error message
     * Object results data that comes from the server.
     */
    function getIncrementalRelatedItems(siteName, serverName, startIndex, pageSize, callback)
    {
        _getIncrementalOrRelatedItems(false, siteName, serverName, startIndex, pageSize, callback);
    }

    /*
     * Private method to get the incremental or incremental related content.
     */
    function _getIncrementalOrRelatedItems(isIncremental, siteName, serverName, startIndex, pageSize, callback)
    {
        var basePath = isIncremental ? $.perc_paths.INCREMENTAL_LIST : $.perc_paths.INCREMENTAL_RELATED_LIST;
        var serviceUrl = basePath  + siteName + '/' + serverName + '?startIndex=' + startIndex + '&pageSize=' + pageSize;
        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, $.PercServiceUtils.extractDefaultErrorMessage(results.request));
            }
            else
            {
                callback(true, results.data);
            }
        };
        $.PercServiceUtils.makeJsonRequest(serviceUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Calls the server to incremental publish the supplied server of the supplied site
     * @param siteName (string) the name of the site to be published
     * @param serverName (string) the name of the server to be published
     * @param callback (Function) callback function assumed to be a function and calls this function with two parameters
     * boolean status if false, the second argument will be a string representing error message
     * if status is true then returns an array with first item being the data returned from server and the second item
     * is the status.
     *
     */
    function incrementalPublishSite(siteName, serverName, callback)
    {
        var pubUrl = $.perc_paths.INCREMENTAL_PUBLISH + siteName + "/" + serverName;

        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, $.PercServiceUtils.extractDefaultErrorMessage(results.request));
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(pubUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }



    /**
     * Calls the server to incremental publish the supplied server of the supplied site
     * @param siteName (string) the name of the site to be published
     * @param serverName (string) the name of the server to be published
     * @param relatedItems List that needs to be approved before publish.
     * @param callback (Function) callback function assumed to be a function and calls this function with two parameters
     * boolean status if false, the second argument will be a string representing error message
     * if status is true then returns an array with first item being the data returned from server and the second item
     * is the status.
     *
     */
    function publishIncrementalWithApproval(siteName, serverName, relatedItems, callback)
    {
        var pubUrl = $.perc_paths.INCREMENTAL_PUBLISH + siteName + "/" + serverName + "/" + relatedItems;

        var serviceCallback = function(status, results)
        {
            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, $.PercServiceUtils.extractDefaultErrorMessage(results.request));
            }
            else
            {
                callback(true, [results.data, results.textstatus]);
            }
        };
        $.PercServiceUtils.makeRequest(pubUrl, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

})(jQuery);
