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
    $.PercSiteTemplatesController = function(useMock)
    {
        return {
            getTemplates: getTemplates,
            getTemplateById: getTemplateById,
            getSites: getSites,
            getAssignedSitesByTemplate: getAssignedSitesByTemplate,
            saveTemplateChanges: saveTemplateChanges,
            isDirty: isDirty,
            load: load,
            clearChanges: clearChanges,
            renameTemplate: renameTemplate,
            getAllTemplates: getAllTemplates,
            addTemplate: addTemplate,
            getBaseTemplates: getBaseTemplates,
            getTemplatesForSite: getTemplatesForSite,
            assignTemplateToSite: assignTemplateToSite,
            unassignTemplateFromSite: unassignTemplateFromSite,
            copyTemplate: copyTemplate,
            getSitesWithTemplates: getSitesWithTemplates,
            deleteTemplate: deleteTemplate,
            loadTemplateMetadata: loadTemplateMetadata,
            saveTemplateMetadata: saveTemplateMetadata,
            createTemplateFromUrl: createTemplateFromUrl
        };
    };

    var _cache = {};
    var _pseudoIdPrefix = "perc-pseudo-temp-";
    var _pseudoIdIncrement = 0;
    var _currentSiteId = null;
    var _useMock = false;
    var templateService = $.PercTemplateService();

    /* =========================================================
     * Public Functions
     * ========================================================= */

    var allTemplates = null;
    var baseTemplates = null;
    var siteTemplates = null;

    function getSitesWithTemplates()
    {
        sites = [];
        for(var t in _cache.templates)
        {
            assignedSites = _cache.templates[t].getAssignedSites();
            for(var s in assignedSites)
            {
                contains = false;
                for (var ss in sites)
                    if (assignedSites[s] == sites[ss]) contains = true;
                if (!contains) sites.push(assignedSites[s]);
            }
        }
        return sites;
    }

    function getAllTemplates()
    {
        return getTemplates("all");
    }

    function addTemplate(template)
    {
        _cache.templates[template.getTemplateId()] = template;
    }

    function getBaseTemplates()
    {
        return getTemplates("base");
    }

    function getTemplatesForSite(siteId)
    {
        return getTemplates("site", siteId);
    }

    function getTemplateById(templateId)
    {
        return _cache.templates[templateId];
    }

    function assignTemplateToSite(template, siteId)
    {
        template.assignToSite(siteId);
        template.setPersisted(false);
    }

    function unassignTemplateFromSite(template, siteId)
    {
        template.unassignFromSite(siteId);
        template.setPersisted(false);
    }

    function copyTemplate(sourceTemplateId, siteId)
    {
        sourceTemplateId = sourceTemplateId.replace('perc-template-', '');
        var sourceTemplate = this.getTemplateById(sourceTemplateId);
        var clonedTemplate = sourceTemplate.clone();
        clonedTemplate.setSourceId(sourceTemplate.getTemplateId());
        clonedTemplate.setTemplateId(_createPseudoId());
        // strip out perc.base
        var newName = sourceTemplate.getTemplateName().replace("perc.base.", "");
        newName = newName.replace("perc.resp.", "");
        clonedTemplate.setTemplateName(_createCopyOfName(newName, siteId));
        if(siteId != null)
        {
            clonedTemplate.setAssignedSites(null);
            clonedTemplate.assignToSite(siteId);
        }
        addTemplate(clonedTemplate);
        return clonedTemplate;
    }

    function deleteTemplate(templateId, callback)
    {
        // if the id of the template is not a number then it's not in the database
        // just remove it from the local cache and re render
        var self = this;
        var idInt = parseInt(templateId.charAt(0));
        if(isNaN(idInt) || idInt == 0)
        {
            delete(_cache.templates[templateId]);
            callback();
        }
        else
        {
            var template = getTemplateById(templateId);
            // Post a DELETE to templates service to delte from database
            templateService.deleteTemplate(templateId, function(success, result)
            {
                if(success)
                {
                    delete(_cache.templates[templateId]);
                    callback(true);
                }
                else
                {
                    var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                    $.perc_utils.alert_dialog(
                        {
                            title: 'Error',
                            content: defMsg
                        });
                    callback(false);
                }
            });
        }
    }

    //load template meta data
    function loadTemplateMetadata(templateId, controllerCallback)
    {
        function serviceCallback(status, metadataJson)
        {
            controllerCallback(metadataJson);
        }
        templateService.loadTemplateMetadata(templateId, serviceCallback);
    }

    //save template metadata
    function saveTemplateMetadata(metadataObj, controllerCallback)
    {
        templateService.saveTemplateMetadata(metadataObj, controllerCallback);
    }

    /**
     * Retrieves templates from the server or client cache.
     * @param mode (string) filter mode, one of the following
     * ('all','base', 'other', 'site','dirty').
     * @param siteid (string) the site id if the mode is 'site' otherwise
     * should be <code>null</code>.
     * @return (Array) of <code>Perc_Template_Summary</code>.
     */
    function getTemplates(mode, siteid)
    {
        var path = null;
        if(mode == "all")
        {
            return _grepArrayHash(_cache.templates, function(val)
            {
                return !val.isBaseTemplate();
            });
        }
        else if(mode == "base")
        {
            return _grepArrayHash(_cache.templates, function(val)
            {
                return val.isBaseTemplate();
            });
        }
        else if(mode == "other")
        {
            return _grepArrayHash(_cache.templates, function(val)
            {
                return val.isOrphan();
            });
        }
        else if(mode == "site")
        {
            return _grepArrayHash(_cache.templates, function(val)
            {
                return val.containsSite(siteid);
            });
        }
        else if(mode == "dirty")
        {
            return _grepArrayHash(_cache.templates, function(val)
            {
                return !val.isPersisted();
            });
        }
    }

    /**
     * Retrieves a list of all sites in the CM sytem.
     * @param force (boolean) force a call to the server instead of trying
     * to use the cache.
     * @param callback the callback function that will pass an array
     * of objects with the following properties:
     * <pre>
     *    sitename
     * </pre>
     */
    function getSites(force, callback)
    {
        if(force) _cache.sites = null;
        if(_cache.sites != null && _cache.sites != 'undefined')
        {
            callback(this,[$.merge([], _cache.sites)]);
            return;
        }

        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITES_ALL + "/", $.PercServiceUtils.TYPE_GET, true,
            function(status, result){
                getSitesCallback(status,result,callback);
            }
            ,null, null // abort callback function not needed in this case
        );
    }

    function getSitesCallback(status, result,callback){

        if(status == $.PercServiceUtils.STATUS_SUCCESS)
        {
            var sites = result.data.SiteSummary;
            var results = [];
            for(let site of sites)
            {
                results.push(site.name);
            }
            _cache.sites = $.merge([], results);
            if(typeof callback !== 'undefined'){
                callback(results);
            }
        }
        else
        {
            var error = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
            var defMsg = "Unable to get sites. Error " + error;
            $.perc_utils.alert_dialog(
                {
                    title: 'Error',
                    content: defMsg
                });
        }
    }


    /**
     * Based on a template id in the current site, return all assigned sites.
     * @return (array) of siteid assigned to the specified template. May
     * be empty.
     */
    function getAssignedSitesByTemplate(templateid)
    {
        var temp = _getAssignedTemplateById(templateid);
        if(temp == null) return [];
        var sites = temp.getAssignedSites();
        if(sites == null || sites == 'undefined') return [];
        return sites;
    }

    /**
     * Save Templates
     */
    function saveTemplateChanges(postCallback)
    {
        var data = {};
        var status = "success";
        var errormsg = null;
        data.SiteTemplates = {};
        var pseudoTemplates = [];
        // get those templates that are dirty, i.e., that have been modified, i.e., that have been assigned to new sites
        var dirtyTemps = getTemplates("dirty");
        // if there are no dirty template, then there's nothing to do. call back with success
        if(dirtyTemps.length == 0)
        {
            postCallback(status);
            return;
        }

        // figure out which templates need to be created new and which just assigned
        var assigns = [];
        var creates = [];

        // iterate over dirty templates
        for(var i = 0; i < dirtyTemps.length; i++)
        {
            var temp = dirtyTemps[i];
            if(_hasPseudoId(temp))
            {
                pseudoTemplates.push(temp.getTemplateId());
                // if the template has been copied since the last save,
                // then it has a pseudo id, not a real id. add to the creates array for creation
                creates.push(
                    {
                        sourceTemplateId: temp.getSourceId(),
                        name: temp.getTemplateName(),
                        siteIds: temp.getAssignedSites()
                    });
            }
            else
            {
                // if the template has already been persisted before in a previous save,
                // then the id is a real id, not pseudo.
                // it is dirty because it has been associated to new sites and it needs to be updated in the database
                // add to assigns array
                assigns.push(
                    {
                        templateId: temp.getTemplateId(),
                        name: temp.getTemplateName(),
                        siteIds: temp.getAssignedSites()
                    });
            }
        }
        // add the assigns and creates arrays to the JSON data structure to be POSTed to the template save service
        if(assigns.length > 0) data.SiteTemplates.assignTemplates = assigns;
        if(creates.length > 0) data.SiteTemplates.createTemplates = creates;

        // post a JSON data structure that contains templates that need to be created and template that need
        // to be updated with new assigned sites
        templateService.saveTemplates(data, function(success, results)
        {
            if (success)
            {
                var textstatus = results[1];
                $.each(pseudoTemplates, function()
                {
                    delete(_cache.templates[this]);
                });
                _updateCache(results[0].TemplateSummary, function()
                {
                    postCallback(textstatus);
                });
            }
            else
            {
                var error = results[2];
                postCallback("Unable to create or assign templates to sites.\nError: ", error);
            }
        });
    }

    /**
     * Will indicate if the current ui is dirty (i.e. changes were made and not
     * persisted).
     */
    function isDirty()
    {
        return getTemplates("dirty").length > 0;
    }

    var loadPostCallbacks = [];

    /**
     * Loads the data from the server into a cache. This
     * MUST happen before any other function call is made to the helper.
     * @param postCallback the callback function that will be executed
     * when load is complete.
     * @param clearCache true if we want to re-load all the data from the server.
     * @param siteRequest accepts the name of the site requested from the add
     * templates filter dialog.
     */
    function load(postCallback, clearCache, siteRequest)
    {
        if($.inArray(postCallback, loadPostCallbacks) == -1)
        {
            loadPostCallbacks.push(postCallback);
        }

        if (clearCache === true) _cache.templates = null;

        if(_cache.templates != null && siteRequest === '') return;

        if(_cache.templates == null) _cache.templates = [];

        var currentSiteName;

        if ((currentSiteName = $.PercNavigationManager.getSiteName()) != null && siteRequest == null) {
            currentSiteName = "/" + currentSiteName;
        }

        else if (siteRequest != null) {
            currentSiteName = "/" + siteRequest;
        }

        else {
            currentSiteName = "/unknown";
        }

        // load all the template summaries
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.TEMPLATES_ALL + currentSiteName, $.PercServiceUtils.TYPE_GET, true,
            function(status,result){
                loadCallback(status,result,loadPostCallbacks);
            }, null, null // abort callback function not needed in this case
        );
    }

    function loadCallback(status, result,loadPostCallbacks){
        if(status == $.PercServiceUtils.STATUS_SUCCESS)
        {
            var summaries = result.data.TemplateSummary;
            _updateCache(summaries, function()
            {
                for(var i = 0; i < loadPostCallbacks.length; i++)
                {
                    loadPostCallbacks[i]();
                }
            });
        }
        else
        {
            var error = $.PercServiceUtils.extractDefaultErrorMessage(jsonresult);
            var defMsg = "Unable to load templates.\nError: " + error;
            $.perc_utils.alert_dialog(
                {
                    title: 'Error',
                    content: defMsg
                });
        }
    }

    /**
     * Builds or updates the cache
     * @param {Object} templateSummaries assumed to be an array of TemplateSummary(PSTemplateSummary JSON) objects.
     */
    function _updateCache(templateSummaries, callback)
    {
        for(var i = 0; i < templateSummaries.length; i++)
        {
            // create template summary object from JSON data
            var sum = templateSummaries[i];
            var isBase = sum.readOnly == true;
            var result = new $.Perc_Template_Summary(null, sum.id, sum.name);
            result.setImageUrl(sum.imageThumbPath);
            result.setPersisted(true);
            result.setBaseTemplate(isBase);
            result.setContentMigrationVersion(sum.contentMigrationVersion);
            if(!isBase)
            {
                _getTemplateSites(sum.id, function(sites)
                {

                    result.setAssignedSites(sites);

                });

            }
            _cache.templates[sum.id] = result;
        }

        callback();
    }



    function clearChanges()
    {
        //TODO: impl this function
    }

    /**
     * Rename a template in the current site.
     * @param templateid
     * @param name, the new template name, cannot be
     * <code>null</code> or empty.
     */
    function renameTemplate(templateId, name)
    {
        if(name == "" || name == null)
        {
            var defMsg = "Template names can not be blank.";
            $.perc_utils.alert_dialog(
                {
                    title: 'Error',
                    content: defMsg
                });
            return;
        }

        var currentSite = $.PercNavigationManager.getSiteName();
        var nameLowerCase = name.toLowerCase();

        // check to see if that name is already in use and alert appropriately
        for(var tempId in _cache.templates)
        {
            var template = _cache.templates[tempId];
            if(template.containsSite(currentSite) && nameLowerCase == template.getTemplateNameLowerCase())
            {
                var defMsg1 = "Template name '" + name + "' is already in use in the current site.\nPlease use a different name for the template." + "\nNote that template names are case insensitive.";
                return defMsg1;
            }
        }

        // if the name is valid, get
        // the template and change its name
        var temp = _cache.templates[templateId];
        temp.setTemplateName(name);
        temp.setPersisted(false);
        return null;
    }

    /* =====================================================
     *  Private Functions
     * ===================================================== */

    function _createPseudoId()
    {
        return _pseudoIdPrefix + (_pseudoIdIncrement++);
    }

    function _hasPseudoId(template)
    {
        var id = template.getTemplateId();
        return id.substr(0, _pseudoIdPrefix.length) === _pseudoIdPrefix;
    }

    /**
     * Creates a unique copy of name. Looks at all existing
     * templates to create a unique name. Appends an incremental
     * number if needed.
     * @param sourcename the name of the source template, assumed
     * not <code>null</code> or empty.
     */
    function _createCopyOfName(sourcename, siteId)
    {
        var alltemps = getTemplates("site", siteId);
        var dirtytemps = getTemplates("dirty");
        var temps = alltemps.concat(dirtytemps);
        var count = 2;
        var isUnique = true;

        // check if the sourcename is a copy
        var copyPostfixLocation = sourcename.indexOf("-Copy");

        // if it is a copy, get the original name
        if(copyPostfixLocation != -1)
        {
            sourcename = sourcename.substring(0, copyPostfixLocation);
        }

        var copyname = sourcename + "-Copy";
        do {
            isUnique = true;
            for(var i = 0; i < temps.length; i++)
            {
                if(temps[i].templateName == copyname)
                {
                    copyname = sourcename + "-Copy(" + count + ")";
                    count++;
                    isUnique = false;
                    break;
                }

            }
        }
        while (!isUnique);

        return copyname;
    }

    function _getTemplateSites(templateid, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.SITES_BY_TEMPLATE + "/" + templateid,
            $.PercServiceUtils.TYPE_GET,
            true,function(status,result){
                getTemplateSitesCallback(status,result,callback);
            },
            null,
            null // abort callback function not needed in this case
        );
    }

    function getTemplateSitesCallback(status, result,callback){
        if(status == $.PercServiceUtils.STATUS_SUCCESS)
        {
            var sites = result.data.SiteSummary;
            var results = [];
            for(i = 0; i < sites.length; i++)
            {
                var site = sites[i];
                results.push(site.name);
            }
            if(typeof callback !== 'undefined'){
                callback(results);
            }
        }
        else
        {
            var error = $.PercServiceUtils.extractDefaultErrorMessage(result.error);
            var defMsg = "Unable to load sites for template " + templateid + "\nError: " + error;
            $.perc_utils.alert_dialog(
                {
                    title: 'Error',
                    content: defMsg
                });
        }
    }

    function _grepArrayHash(elems, callback, inv)
    {
        var ret = [];

        // Go through the array, only saving the items
        // that pass the validator function
        for(var elem in elems) {
            if (!inv !== !callback(elems[elem])) {
                ret.push(elems[elem]);
            }
        }

        return ret;
    }

    /**
     * Creates a new template from a URL for the site selected invoking the corresponding service.
     * @param String url the url used to create the corresponding template
     * @param function postCreationCallback (optional) callback function invoked on unsucess/error
     */
    function createTemplateFromUrl(url, postCreationCallback)
    {
        // Get the current selected site form the PercNavigationManager
        var currentSiteName = $.PercNavigationManager.getSiteName();
        // Holds the information needed to redirect the user to the template editor, the object
        // will be completed with the corresponding callbacks data
        var memento = {
            view: $.Percussion.getCurrentTemplatesView(),
            tabId: 'perc-tab-layout'
        };

        /**
         * Retrieves the template summary data
         * @param String status string that represent XHR success
         * @param Object percTemplateServiceData template summary data
         */
        function createTemplateFromUrlResultCallback(status, percTemplateServiceData)
        {
            if (status != $.PercServiceUtils.STATUS_SUCCESS)
            {
                if (postCreationCallback !== undefined)
                    postCreationCallback(percTemplateServiceData);
                return;
            }

            // Fill the memento object with the needed data
            memento.templateName = percTemplateServiceData.name;
            memento.templateId = percTemplateServiceData.id;

            $.PercPageService.getPagesWithTemplate(
                percTemplateServiceData.id,
                {maxResults: 1},
                getPagetIdFromTemplate,
                function(pageServiceStatus, percPageServiceData)
                {
                    postCreationCallback(percPageServiceData);
                }
            );
        }

        /**
         * Retrieves the pageId for the recently created page. We need it for a memento object that
         * will open the template editor using the corresponding page
         * @param String status string that represent XHR success
         * @param Object having exactly one element (page) according to a specified templateId
         */
        function getPagetIdFromTemplate(XHRstatus, percPageServiceData)
        {
            var querystring = $.deparam.querystring();
            // Add the last element needed in the memento and redirect
            memento.pageId = percPageServiceData.firstItemId;
            $.PercNavigationManager.goToLocation(
                $.PercNavigationManager.VIEW_EDIT_TEMPLATE,
                querystring.site,
                null,
                null,
                null,
                querystring.path,
                null,
                memento
            );
        }

        ////////////////////////////////////////////////////////////
        // createTemplateFromUrl function execution starts from here
        ////////////////////////////////////////////////////////////
        $.PercImportProgressDialog({
            backgroundRefreshCallback: function()
            {
                // TODO: we are cheating here, because we should tell the view to update or
                // something similar.
                // Refresh the templates carrousel
                load(
                    function loadAndClearCacheCallback()
                    {
                        $('#perc-assigned-templates').template_selected('refresh');
                    },
                    true
                );
            },
            onSuccessCallback: function(importData)
            {
                // Retrieve the new template data to redirect to the template editor
                createTemplateFromUrlResultCallback('success', importData);
            },
            startProgressCallback: function(callbackJobIdHandler)
            {
                $.PercTemplateService().createTemplateFromUrlAsync(
                    url,
                    currentSiteName,
                    function(status, jobId)
                    {
                        callbackJobIdHandler(status, jobId);
                    }
                );
            },
            pollingProgressCallback: $.PercTemplateService().createTemplateFromUrlStatus,
            importResultCallback: $.PercTemplateService().createTemplateFromUrlResult
        });
    }
})(jQuery);
