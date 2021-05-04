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
    $.PercTemplateService = function()
    {
        return {
            getThemeList: getThemeList,
            getThemeCSS: getThemeCSS,
            getRegionCSS: getRegionCSS,
            deleteTemplate: deleteTemplate,
            deleteRegionCSS: deleteRegionCSS,
            saveTemplates: saveTemplates,
            saveRegionCSS: saveRegionCSS,
            regionCSSPrepareForEdit: regionCSSPrepareForEdit,
            regionCSSClearCache: regionCSSClearCache,
            regionCSSMerge: regionCSSMerge,
            getAssetDropCriteria: getAssetDropCriteria,
            loadTemplateMetadata: loadTemplateMetadata,
            saveTemplateMetadata: saveTemplateMetadata,
            exportTemplate: exportTemplate,
            getSiteProperties: getSiteProperties,
            updateInspectedElements: updateInspectedElements,
            checkImportLogExists : checkImportLogExists,
            createTemplateFromUrl: createTemplateFromUrl,
            createTemplateFromUrlAsync: createTemplateFromUrlAsync,
            createTemplateFromUrlStatus: createTemplateFromUrlStatus,
            createTemplateFromUrlResult: createTemplateFromUrlResult,
            createTemplateFromPage: createTemplateFromPage,
            assignTemplateAndMigrateContent: assignTemplateAndMigrateContent,
            applyTemplate: applyTemplate
        };
    };

    function getThemeList(callback)
    {
        $.ajax({
                url: $.perc_paths.THEME_SUMMARY_ALL,
                // replace with constant.
                dataType: "json",
                type: "GET",
                success: function(data, textstatus)
                {
                    callback(true, data);
                },
                error: function(request, textstatus, error)
                {
                    callback(false, [textstatus, error]);
                }
            });
    }

    function getThemeCSS(themeName, callback)
    {
        $.ajax(
            {
                url: $.perc_paths.THEME_CSS + "/" + themeName,
                // replace with constant.
                dataType: "xml",
                type: "GET",
                success: function(data, textstatus)
                {
                    var $cssObject = $(data);
                    var $cssData = $cssObject.find("ThemeCSS CSS");
                    callback(true, $cssData);

                },
                error: function(request, textstatus, error)
                {
                    callback(false, [textStatus, error]);
                }
            });
    }

    /**
     * Retrieves the css properties for a given region
     * @param themeName (string) the name of the team for the current template
     * @param templateName (string) current template name
     * @param outerRegionName the name of the outer most region container.
     *        The css rules have the following format:
     *        Eg:#<outerMostRegionName>.perc-region #<regionName>.perc-region
     *        Eg:#container.perc-region #header1.perc-region
     * @param regionName the name of the region to retrieve the properties
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function getRegionCSS(themeName, templateName, outerRegionName, regionName, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS + "/" + themeName  + "/" + templateName + "/" + outerRegionName + "/" + regionName,
            $.PercServiceUtils.TYPE_GET, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
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
     * Calls the service to copy the master perc-region.css file to the corresponding
     * temp cache file (to perform the actions without saving in the original file)
     * @param themeName (string) the name of the team for the current template
     * @param templateName (string) current template name
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function regionCSSPrepareForEdit(themeName, templateName, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS_PREPARE_FOR_EDIT + "/" + themeName  + "/" + templateName,
            $.PercServiceUtils.TYPE_POST, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
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
     * Deletes the temp cache file
     * @param themeName (string) the name of the team for the current template
     * @param templateName (string) current template name
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function regionCSSClearCache(themeName, templateName, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS_CLEAR_CACHE + "/" + themeName  + "/" + templateName,
            $.PercServiceUtils.TYPE_DELETE, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
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
     * Merges the changes done to the temp cache file into the master perc-region.css file
     * @param themeName (string) the name of the team for the current template
     * @param templateName (string) current template name
     * @param deletedRegionsJSON (object) an object containing a group of regions that are marked for
     *        deletion. This regions are erased only if there's no rule for them in the cache file.
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function regionCSSMerge(themeName, templateName, deletedRegionsJSON, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS_MERGE + "/" + themeName  + "/" + templateName,
            $.PercServiceUtils.TYPE_POST, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }, deletedRegionsJSON);
    }

    /**
     * Merges the changes done to the temp cache file into the master perc-region.css file
     * @param themeName (string) the name of the team for the current template
     * @param templateName (string) current template name
     * @param regionJSON (object) an object containing a group of css properties that need to be saved
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function saveRegionCSS(themeName, templateName, regionJSON, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS + "/" + themeName  + "/" + templateName,
            $.PercServiceUtils.TYPE_POST, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }, regionJSON);
    }

    /**
     * Deletes a region rule from the temp cache file
     * @param themeName (string) the name of the theme for the current template
     * @param templateName (string) current template name
     * @param outerRegionName the name of the outer most region container.
     *        The css rules have the following format:
     *        Eg:#<outerMostRegionName>.perc-region #<regionName>.perc-region
     *        Eg:#container.perc-region #header1.perc-region
     * @param regionName the name of the region to delete the rule
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function deleteRegionCSS(themeName, templateName, outerRegionName, regionName, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.REGION_CSS + "/" + themeName  + "/" + templateName + "/" + outerRegionName + "/" + regionName,
            $.PercServiceUtils.TYPE_DELETE, false, function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
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
     * Deletes a template from the server.
     * @param templateId (string) the id of the template to be removed
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function deleteTemplate(templateId, callback)
    {
        $.ajax({
                url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateId,
                type: "DELETE",
                success: function(data, textStatus)
                {
                    var results = [data, textStatus];
                    callback(true, results);
                },
                error: function(request, textStatus, error)
                {
                    var results = [request, textStatus, error];
                    callback(false, results);
                }
            });
    }

    /**
     * Saves template site associations.
     * @param templates to be created or updated.
     *    Here's an example of the structure of templates parameter
     *
     *    {"SiteTemplates":{
     *        "assignTemplates":[{
     *            "templateId":"16777215-101-722",
     *            "name":"t2",
     *            "siteIds":["s2","s1"]
     *        }],
     *        "createTemplates":[
     *            {
     *                "sourceTemplateId":"0-4-563",
     *                "name":"Copy of perc.base.cClampRight",
     *                    "siteIds":["s1"]
     *            },
     *            {
     *                "sourceTemplateId":"0-4-565",
     *                "name":"Copy of perc.base.cClampTop",
     *                "siteIds":["s1"]
     *            }
     *        ]
     *    }}
     *
     * @param callback (function) callback function to be invoked when ajax call returns
     */
    function saveTemplates(templates, callback)
    {
        $.ajax(
            {
                url: $.perc_paths.TEMPLATES_SAVE + "/",
                dataType: "json",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify(templates),
                success: function(data, textStatus)
                {
                    var results = [data, textStatus];
                    callback(true, results);
                },
                error: function(request, textStatus, error)
                {
                    var results = [request, textStatus, error];
                    callback(false, results);
                }
            });
    }

    /**
     * Retrieves asset info needed for drop criteria and other.
     * @param objectId {string} the page or template id.
     * @param isPage {boolean} flag indicating if objectId is for
     * a page.
     * @param callback {function} the success callback function.
     * @param errorCallback {function} the error callback function.
     */
    function getAssetDropCriteria(objectId, isPage, callback, errorCallback)
    {
        function parseAssetDropCriteria(json)
        {
            var assetDropCriteria = {};
            $.each(json.AssetDropCriteria, function()
            {
                assetDropCriteria[this.widgetId] = new $.PercAssetDropCriteriaModel(
                    this.widgetId,
                    this.appendSupport,
                    this.existingAsset,
                    this.multiItemSupport,
                    this.ownerId,
                    this.supportedCtypes,
                    this.assetShared,
                    this.relationshipId);
            });
            callback(assetDropCriteria);
        }

        $.ajax(
            {
                url: $.perc_paths.ASSET_WIDGET_DROP_CRITERIA + objectId + "/" + isPage,
                type: 'GET',
                dataType: 'json',
                success: parseAssetDropCriteria,
                error: errorCallback
            });
    }

    //LT
    //Calls load template metadata service
    function loadTemplateMetadata(templateId, callback, errorCallback)
    {
        function parseTemplateMetadata(data)
        {
            callback(true, data);
        }

        $.ajax(
            {
                url: $.perc_paths.TEMPLATE_LOAD_METADATA + "/" + templateId,
                type: 'GET',
                dataType: 'json',
                success: parseTemplateMetadata,
                error: errorCallback
            });
    }

    //LT
    //Calls save template metadata service
    function saveTemplateMetadata(templateMetadata, callback)
    {
        $.ajax(
            {
                url: $.perc_paths.TEMPLATE_SAVE_METADATA + "/",
                dataType: "json",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify(templateMetadata),
                success: function(data, textStatus)
                {
                    var results = [data, textStatus];
                    callback(true, results);
                },
                error: function(request, textStatus, error)
                {
                    var results = [request, textStatus, error];
                    callback(false, results);
                }
            });
    }

    //Export template
    function exportTemplate(templateId, templateName, callback)
    {
        var getUrl = $.perc_paths.TEMPLATE_EXPORT + "/" + templateId + "/" + templateName + ".xml";

        var serviceCallback = function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, results.data);
            }
        };
        $.PercServiceUtils.makeRequest(
            getUrl,
            $.PercServiceUtils.TYPE_GET,
            true,
            serviceCallback);
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
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
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
     * Save the newly created wiget data
     * @param newWidgetObj - the object containing the ownerId, widgetId and its content
     * @param callback - callback function.
     */
    function updateInspectedElements(newWidgetObj, callback)
    {
        var url = $.perc_paths.UPDATE_INSPECTED_ELEMENT;
        var serviceCallback = function(status, results)
        {
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false, [results.request, results.textstatus, results.error]);
            }
            else
            {
                callback(true, results.data);
            }
        };
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_POST,
            true,
            serviceCallback,
            newWidgetObj);
    }

    /**
     * Checks if the import log exists for the template (was created using url import option, or url create
     * site option)
     * @param templateId - the id of the template
     * @param callback - callback function.
     */
    function checkImportLogExists(templateId, callback)
    {
        var url = $.perc_paths.VIEW_IMPORT_LOG + "?templateId=" + templateId + "&exists=true";
        var serviceCallback = function(status, results)
        {
            callback(status, results);
        };
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            true,
            serviceCallback);
    }

    /**
     * Creates a new template given a URL.
     * @param String URL
     * @param String sitename the site that will have the new template
     * @param function callback.
     */
    function createTemplateFromUrl(url, sitename, callback)
    {
        var serviceParam = {
            "SiteTemplates": {
                "importTemplate": [{
                    "url": url,
                    "siteIds": [sitename]
                }]
            }
        };

        // Redefine temporarly timeout to be 10 minutes (importing proccess might take too long)
        $.ajaxSetup({timeout: 600000});

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_CREATE_FROM_URL,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.TemplateSummary);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            serviceParam
        );
    }

    /**
     * Startes a template creation job (async) given a URL.
     * @param String URL
     * @param String sitename the site that will have the new template
     * @param function callback.
     */
    function createTemplateFromUrlAsync(url, sitename, callback)
    {
        var serviceParam = {
            "SiteTemplates": {
                "importTemplate": {
                    "url": url,
                    "siteIds": [sitename]
                }
            }
        };

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_CREATE_FROM_URL_ASYNC,
            $.PercServiceUtils.TYPE_POST,
            false,
            function templateCreateFromUrlAsyncCallback(status, result)
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
     * Retrieves the status of a previously started template creation job.
     * @param String templateCreationJobId returned by a createTemplateFromUrlStatus
     * @param function callback.
     */
    function createTemplateFromUrlStatus(templateCreationJobId, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.JOB_STATUS + '/' + templateCreationJobId.Long,
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
     * Retrieves the template creation job result
     * @param String templateCreationJobId returned by a createTemplateFromUrlStatus
     * @param function callback.
     */
    function createTemplateFromUrlResult(templateCreationJobId, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_CREATE_FROM_URL_RESULT + '/'+ templateCreationJobId.Long,
            $.PercServiceUtils.TYPE_GET,
            false,
            function templateCreateFromUrlAsyncCallback(status, result)
            {
                var data,
                    serviceStatus = $.PercServiceUtils.STATUS_SUCCESS;

                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    data = result.data.TemplateSummary;
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
     * Creates the template from page, if the page is not checked out to the current user throws an error.
     * @param {Object} pageId The id of the page.
     * @param {Object} callback The callback function that gets called with status and result
     * The status is a boolean object
     * result is a String, it is an error message if the status is false and template name if the status is true.
     */
    function createTemplateFromPage(pageId, siteName, callback)
    {
        var serviceParam = {"PageToTemplatePair": {"pageId": pageId, "siteId": siteName}};

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_CREATE_FROM_PAGE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data.Template);
                }
                else
                {
                    var defaultMsg = I18N.message("perc.ui.site.service@Error Creating Template");
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            serviceParam
        );
    }

    /**
     * Assigns the template to dropped page/s and also migrates the content.
     * @param {Object} migrateContentRestData contains templatId, referencePageId, and array of pagesId's whose content need to be migrated
     * @param {function} callback - Callback function to re-paint the page list for a given template
     */

    function assignTemplateAndMigrateContent(migrateContentRestData, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_MIGRATE_CONTENT,
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
                    var defaultMsg = I18N.message("perc.ui.template.service@Error Migrating Content");
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            migrateContentRestData
        );
    }

    /**
     * Migrate the content for the supplied page/pages.
     * @param {string} templateId - Id of the selected template
     * @param {string} itemId - itemId can be a single page Id or a string 'ALL' (ALL === Apply template change to all dirty pages)
     * @param {function} callback - Callback function to re-paint the page list for a given template
     */

    function applyTemplate(templateId, itemId, callback) {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATE_MIGRATE_CONTENT + '/' + templateId + '/' + itemId,
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
                    var defaultMsg = I18N.message("perc.ui.publish.title@Error");
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }



})(jQuery);