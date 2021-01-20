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

/**
 * [PercContributorUiAdaptor.js]
 */
(function($){
    $.PercContributorUiAdaptor = function(){
        function openItem(path, id){
            var deferred = $.Deferred();
            if (!path) {
                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Path Empty"));
            }
            else {
                path = getNormalizedPath(path);
                $.PercNavigationManager.openPathItem(path);
            }
            return deferred.promise();
        }
        function previewItem(path, id){
            var deferred = $.Deferred();
            var pathType = getPathType(path);
            if (!path || !id || pathType == "unknown") {
                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Path Preview"));
            }
            else {
                if (pathType == "site") {
                    $.perc_finder().launchPagePreviewByPath(path, id);
                }
                else {
                    $.perc_finder().launchAssetPreview(id);
                }
            }
            return deferred.promise();
        }
        function copyItem(path, id){
            var deferred = $.Deferred();
            var pathType = getPathType(path);
            if (!path || !id || pathType == "unknown") {
                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Path and ID Copy"));
            }
            else {
                if (pathType == "site") {
                    $.PercPageService.copyPage(id, function(status, result){
                        if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                            if (result.data == "" || typeof result.data != 'string') {
                                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Failed Copy"));
                            }
                            else {
                                deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Copied Page") + id + I18N.message("perc.ui.contributor.ui.adaptor@Added Page To Recent"));
                            }
                        }
                        else {
                            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                            deferred.reject(defaultMsg);
                        }
                    });
                }
                else {
                    deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Asset Copy"));
                }
            }
            return deferred.promise();
        }
        function deleteItem(path, id, name){
            var deferred = $.Deferred();
            var pathType = getPathType(path);
            if (!path || !id || pathType == "unknown") {
                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Path and ID delete"));
            }
            else {
                if (pathType == "site") {
                    api.isLandingPage(path).done(function(result){
                        if(result){
                            var retObj = {
                                "content": I18N.message("perc.ui.contributor.ui.adaptor@Landing Page Delete"),
                                "canForceDelete": false,
                            };                            
                            deferred.reject(retObj);
                        }
                        else{
                          $.PercRedirectHandler.createRedirect(path, "", "page").fail(function(errMsg){
                        	  $.perc_utils.alert_dialog({
                        		  title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"),
                        		  content: errMsg,
                        		  okCallBack: function(){
                        			  $.perc_pagemanager.delete_page(id, function(){
                        				  deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Deleted Selected Item"));
                        			  }, function(data){
                        				  var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, name, "page");
                        				  deferred.reject(result);
                        			  });
                        		  }
                        	  });
                     	}).done(function(){
                          $.perc_pagemanager.delete_page(id, function(){
                                deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Deleted Selected Item"));
                            }, function(data){
                               var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, name, "page");
                               deferred.reject(result);
                            });
                      });
                    }
                    }).fail(function(message){
                       deferred.reject(message);
                    });
                }
                else {
                    $.PercAssetService.deleteAsset(id, function(){
                        deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Deleted Selected Item"));
                    }, function(data){
                        var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, name, "asset");
                        deferred.reject(result);
                    });
                }
            }
            return deferred.promise();
            
        }
        function forceDeleteItem(path, id, name){
            var deferred = $.Deferred();
            var pathType = getPathType(path);
            if (!path || !id || pathType == "unknown") {
                deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Path and ID delete"));
            }
            else {
                if (pathType == "site") {
                    $.PercPageService.forceDeletePage(id, function(){
                        deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Deleted Selected Item"));
                    }, function(data){
                       var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, name, "page");
                       deferred.reject(result.content);
                    });
                }
                else {
                    $.PercAssetService.forceDeleteAsset(id, function(){
                        deferred.resolve(I18N.message("perc.ui.contributor.ui.adaptor@Deleted Selected Item"));
                    }, function(data){
                        var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, name, "asset");
                        deferred.reject(result.content);
                    });
                }
            }
            return deferred.promise();
            
        }
        function bookMarkItem(path, id){
            var deferred = $.Deferred();
            $.PercPageService.addToMyPages(id,function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    deferred.resolve(result);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function getRecentItems(type, site){
            return $.PercRecentListService.getRecentList(type, site);
        }
        function getPathType(path){
            var pathType = "unknown";
            if (!path) 
                return pathType;
            var pathLower = path.toLowerCase();
            if (pathLower.match("^//sites/") || pathLower.match("^/sites/") ||
            pathLower.match("^sites/")) {
                pathType = "site";
            }
            else 
                if (pathLower.match("^//assets/") || pathLower.match("^/assets/") ||
                pathLower.match("^assets/")) {
                    pathType = "asset";
                }
            return pathType;
        }
        function getNormalizedPath(path){
            var normPath = path, pathLower = path.toLowerCase();
            
            if (pathLower.match("^//sites/") || pathLower.match("^//assets/") || pathLower.match("^/sites/") || pathLower.match("^/assets/") || pathLower.match("^sites/") || pathLower.match("^assets/")) {
                if (pathLower.match("^sites/") || pathLower.match("^assets/")) 
                    normPath = "/" + normPath;
                else 
                    if (pathLower.match("^//sites/") || pathLower.match("^//assets/")) 
                        normPath = normPath.substring(1);
            }
            return normPath;
        }
        function getSites(){
            var deferred = $.Deferred();
            $.PercSiteService.getSites(function(status, data){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var sites = [];
                    $.each(data.SiteSummary, function(){
                        sites.push({
                            "name": this.name,
                            "id": this.siteId
                        });
                    })
                    deferred.resolve(sites);
                }
                else {
                    deferred.reject(I18N.message("perc.ui.contributor.ui.adaptor@Failed To Get Sites"));
                }
            });
            
            return deferred.promise();
        }
		function getSiteProperties(site, callback) {
			return $.PercSiteService.getSiteProperties(site, callback);
		}
        function getBlogsForSite(siteName){
            var deferred = $.Deferred();
            $.PercBlogService.getBlogsForSite(siteName, function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var blogs = [];
                    $.each(result.SiteBlogProperties, function(){
                        var temp = this.path, folderPath = temp.substring(0, temp.lastIndexOf("/"));
                        var blog = {
                            "title": this.title,
                            "folderPath": folderPath,
                            "templateId": this.blogPostTemplateId
                        };
                        blogs.push(blog);
                    })
                    deferred.resolve(blogs);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function createPage(name, linkTitle, templateId, folderPath){
            var deferred = $.Deferred();
            var dataObj = [];
            dataObj.push({
                "name": "page_name",
                "value": name
            });
            dataObj.push({
                "name": "page_title",
                "value": linkTitle
            });
            dataObj.push({
                "name": "template",
                "value": templateId
            });
            dataObj.push({
                "name": "page_linktext",
                "value": linkTitle
            });
            folderPath = getNormalizedPath(folderPath);
            var tempPath = "/" + folderPath;
            var fullPath = tempPath.substring(tempPath.length - 1) == "/" ? tempPath + name : tempPath + "/" + name;
            
            //Check whether user has access to create page
            
            $.PercFolderHelper().getAccessLevelByPath(folderPath, true, function(status, result){
                if (status == $.PercFolderHelper().PERMISSION_ERROR || result == $.PercFolderHelper().PERMISSION_READ) {
                    deferred.reject("NotAuthorized");
                }
                else {
                    $.PercUserService.getAccessLevel("percPage", -1, function(status, result){
                        if (status == $.PercServiceUtils.STATUS_ERROR || result == $.PercUserService.ACCESS_READ || result == $.PercUserService.ACCESS_NONE) {
                            deferred.reject("NotAuthorized");
                        }
                        else {
                            $.perc_pagemanager.createPage(tempPath, dataObj, function(page){
                                $.PercNavigationManager.openPage(getNormalizedPath(fullPath), true);
                            }, function(request){
                                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                                deferred.reject(defaultMsg);
                            });
                        }
                    }, folderPath);
                }
            });
            
            return deferred.promise();
        }
        function getTemplates(siteName){
            var deferred = $.Deferred();
            $.PercSiteService.getTemplates(siteName, function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var templates = [];
                    $.each(result.TemplateSummary, function(){
                        var template = {
                            "id": this.id,
                            "name": this.name,
                            "thumbPath": this.imageThumbPath
                        };
                        templates.push(template);
                    })
                    deferred.resolve(templates);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function setRecent(type, data, site){
            return $.PercRecentListService.setRecent(type, data, site);
        }
        function getRecentTemplates(siteName){
            var deferred = $.Deferred();
            api.getRecentItems("template", siteName).done(function(result){
                var templates = [];
                if (result.data) {
                    $.each(result.data.TemplateSummary, function(){
                        var template = {
                            "id": this.id,
                            "name": this.name,
                            "thumbPath": this.imageThumbPath
                        };
                        templates.push(template);
                    })
                }
                deferred.resolve(templates);
            }).fail(function(message){
                deferred.reject(message);
            });
            return deferred.promise();
            
        }
        function getRecentSiteFolders(siteName){
            var deferred = $.Deferred();
            api.getRecentItems("site-folder", siteName).done(function(result){
                var sitefolders = [];
                if (result.data) {
                    $.each(result.data.PathItem, function(){
                        var sitefolder = {
                            "id": this.id,
                            "name": this.name,
                            "category": this.category,
                            "path": this.path
                        };
                        sitefolders.push(sitefolder);
                    })
                }
                deferred.resolve(sitefolders);
            }).fail(function(message){
                deferred.reject(message);
            });
            return deferred.promise();
        }
        function getRecentAssetFolders(){
            var deferred = $.Deferred();
            api.getRecentItems("asset-folder").done(function(result){
                var sitefolders = [];
                if (result.data) {
                    $.each(result.data.PathItem, function(){
                        var sitefolder = {
                            "id": this.id,
                            "name": this.name,
                            "category": this.category,
                            "path": this.path
                        };
                        sitefolders.push(sitefolder);
                    })
                }
                deferred.resolve(sitefolders);
            }).fail(function(message){
                deferred.reject(message);
            });
            return deferred.promise();
        }
        function getAssetTypes(filterDisabledWidgets){
            var deferred = $.Deferred();
            $.PercAssetService.getAssetTypes(filterDisabledWidgets, function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var widgetTypes = [];
                    $.each(result, function(){
                        var widgetType = {
                            "id": this.widgetId,
                            "name": this.widgetLabel,
                            "contenttypeid": this.contentTypeId,
                            "contenttypename": this.contentTypeName,
                            "icon": this.icon
                        };
                        widgetTypes.push(widgetType);
                    })
                    deferred.resolve(widgetTypes);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function getRecentAssetTypes(){
            var deferred = $.Deferred();
            api.getRecentItems("asset-type").done(function(result){
                var widgetTypes = [];
                $.each(result.data.WidgetContentType, function(){
                    var widgetType = {
                        "id": this.widgetId,
                        "name": this.widgetLabel,
                        "icon": this.icon
                    };
                    widgetTypes.push(widgetType);
                })
                deferred.resolve(widgetTypes);
            }).fail(function(message){
                deferred.reject(message);
            });
            return deferred.promise();
        }
        function createAsset(folderPath, widgetId){
            var deferred = $.Deferred();
            $.PercFolderHelper().getAccessLevelByPath(folderPath, true, function(status, result){
                if (status == $.PercFolderHelper().PERMISSION_ERROR || result == $.PercFolderHelper().PERMISSION_READ) {
                    deferred.reject("NotAuthorized");
                }
                else {
                    $.PercUserService.getAccessLevel("percAsset", -1, function(status, result){
                        if (status == $.PercServiceUtils.STATUS_ERROR || result == $.PercUserService.ACCESS_READ || result == $.PercUserService.ACCESS_NONE) {
                            deferred.reject("NotAuthorized");
                        }
                        else {
                            $.PercRecentListService.setRecent("asset-type", widgetId);
                            var recFolderPath = folderPath.substring(folderPath.length - 1) == "/" ? folderPath.substring(0, folderPath.length - 1) : folderPath;
                            $.PercRecentListService.setRecent("asset-folder", recFolderPath);
                            $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_EDIT_ASSET, $.PercNavigationManager.getSiteName(), 'edit', null, null, $.PercNavigationManager.getPath(), $.PercNavigationManager.PATH_TYPE_ASSET, {
                                "widgetId": widgetId,
                                "folderPath": folderPath
                            });
                        }
                    }, folderPath);
                }
            });
            return deferred.promise();
        }
        function getMyContent(){
            var deferred = $.Deferred();
            $.PercPageService.getMyContent(function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    deferred.resolve(result);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        
        function getSearchResults(searchCriteria){
            var deferred = $.Deferred();
            $.PercSearchService.getAsyncSearchExtendedResult(searchCriteria, function(status, result){
                if (status) {
                    deferred.resolve(result);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function getStates(workflow){
            var deferred = $.Deferred();
            $.PercReusableSearchService.getStates(workflow, function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var states = [];
                    $.each(result, function(){
                        var state = {
                            "name": this.value,
                            "id": this.displayValue
                        };
                        states.push(state);
                    })
                    deferred.resolve(states);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function getWorkflows(){
            var deferred = $.Deferred();
            $.PercReusableSearchService.getWorkflows(function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var workflows = [];
                    $.each(result, function(){
                        var workflow = {
                            "name": this.value,
                            "id": this.displayValue
                        };
                        workflows.push(workflow);
                    })
                    deferred.resolve(workflows);
                }
                else {
                    deferred.reject(result);
                }
            });
            return deferred.promise();
        }
        function getFolders(path){
            path = getNormalizedPath(path);
            var deferred = $.Deferred(), url = $.perc_paths.PATH_FOLDER + path;
            $.PercServiceUtils.makeJsonRequest(url, $.PercServiceUtils.GET, false, function(status, result){
                if (status == $.PercServiceUtils.STATUS_ERROR) {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                    deferred.reject(defaultMsg);
                }
                else {
                    deferred.resolve(result.data);
                }
            }, null);
            return deferred.promise();
        }
        function isLandingPage(path){
            path = getNormalizedPath(path);
            var deferred = $.Deferred();
            $.PercPathService.getPathItemForPath(path, function(status, result){
                if (status == $.PercServiceUtils.STATUS_ERROR) {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                    deferred.reject(defaultMsg);
                }
                else {
                    deferred.resolve(result.PathItem.category == "LANDING_PAGE");
                }
            }, null);
            return deferred.promise();
        }
        var api = {
            openItem: openItem,
            previewItem: previewItem,
            copyItem: copyItem,
            bookMarkItem: bookMarkItem,
            getRecentItems: getRecentItems,
            getBlogsForSite: getBlogsForSite,
            getSites: getSites,
			getSiteProperties: getSiteProperties,
            createPage: createPage,
            getTemplates: getTemplates,
            getRecentTemplates: getRecentTemplates,
            getRecentSiteFolders: getRecentSiteFolders,
            getAssetTypes: getAssetTypes,
            getRecentAssetTypes: getRecentAssetTypes,
            createAsset: createAsset,
            setRecent: setRecent,
            getMyContent: getMyContent,
            getRecentAssetFolders: getRecentAssetFolders,
            getSearchResults: getSearchResults,
            getStates: getStates,
            getWorkflows: getWorkflows,
            getFolders : getFolders,
            deleteItem:deleteItem,
            forceDeleteItem:forceDeleteItem,
            isLandingPage:isLandingPage
        };
        return api;
    }
    
})(jQuery);
