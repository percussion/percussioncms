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

// assumed dependencies:
//  - require.js

define
(
    [
       'jquery'
    ],
    function ($) {
        function openLibrary(){
            window.parent.location.search = "?view=home&initialScreen=library";
        }
        function openItem(path, id){
            return api.uiAdaptor.openItem(path, id);
        }
        function previewItem(path, id){
            return api.uiAdaptor.previewItem(path, id);
        }
        function copyItem(path, id){
            return api.uiAdaptor.copyItem(path, id);
        }
        function getBlogsForSite(siteName){
            return api.uiAdaptor.getBlogsForSite(siteName);
        }
        function deleteItem(path, id, name){
            return api.uiAdaptor.deleteItem(path, id, name);
        }
        function forceDeleteItem(path, id, name){
            return api.uiAdaptor.forceDeleteItem(path, id, name);
        }
        function bookMarkItem(path, id){
            return api.uiAdaptor.bookMarkItem(path, id);
        }
        function recentList(type, site){
            return api.uiAdaptor.getRecentItems(type,site);
        }
		function createDummyData(startingIndex, numberOfItems){
			var deferred = $.Deferred();
            var dummyItem = {
                "id":"16777215-101-2478",
                "commentsCount":0,
                "lastModifiedDate":"2014-10-08T10:22:41.000-04:00",
                "lastModifier":"Admin",
                "lastPublishedDate":"",
                "newCommentsCount":0,
                "path":"\/Sites\/test\/test1",
                "postDate":"",
                "scheduledPublishDate":"",
                "scheduledUnpublishDate":"",
                "status":"Draft",
                "thumbnailPath":"/Rhythmyx/rx_resources/images/TemplateImages/www.samples.com/16777215-101-805-page.jpg",
                "type":"plain - Copy",
                "workflow":"Default Workflow"
            };
            var resultItems = [];
            for(var i = startingIndex; i < (startingIndex + numberOfItems); i++) {
				var nameObj = {"name":i}; 
				var resultItem = $.extend({},dummyItem,nameObj);
                resultItems.push(resultItem);
            }
			deferred.resolve(resultItems);
			return deferred.promise();  
        }
        function myBookmarks(){
            return api.uiAdaptor.getMyContent();
        }
        function getSites(){
            return api.uiAdaptor.getSites();
        }
        function getSiteProperties(site, callback){
            return api.uiAdaptor.getSiteProperties(site, callback);
        }
        function createPage(name, linkTitle, templateId, folderPath){
            return api.uiAdaptor.createPage(name, linkTitle, templateId, folderPath);
        }
        function createAsset(folderPath, widgetId){
            return api.uiAdaptor.createAsset(folderPath, widgetId);
        }
        function getTemplates(siteName){
            return api.uiAdaptor.getTemplates(siteName);
        }
        function getRecentTemplates(siteName){
            return api.uiAdaptor.getRecentTemplates(siteName);
        }
        function getRecentSiteFolders(siteName){
            return api.uiAdaptor.getRecentSiteFolders(siteName);
        }
        function getRecentAssetFolders(){
            return api.uiAdaptor.getRecentAssetFolders();
        }
        function setRecent(type, data, site){
            return api.uiAdaptor.setRecent(type, data, site);
        }
        function getAssetTypes(filterDisabledWidgets){
            return api.uiAdaptor.getAssetTypes(filterDisabledWidgets);
        }
        function getRecentAssetTypes(){
            return api.uiAdaptor.getRecentAssetTypes();
        }
        function getSearchResults(searchCriteria){
            return api.uiAdaptor.getSearchResults(searchCriteria);
        }
		function getChildNodes(path){
			var childNodes = [ 
                {title: "LazyFolder1", "isFolder": true, "isLazy": true },    
                {title: "LazyFolder2", "isFolder": true, "isLazy": true } 
            ];
            return childNodes;
		}
        function getStates(workflow){
            return api.uiAdaptor.getStates(workflow);
        }
		function getWorkflows(){
            return api.uiAdaptor.getWorkflows();
        }
        function getFolders(path){
            return api.uiAdaptor.getFolders(path);
        }
        var api = {
            uiAdaptor:window.parent.jQuery.PercContributorUiAdaptor(),
            openLibrary : openLibrary,
            openItem : openItem,
            previewItem : previewItem,
            copyItem : copyItem,
            bookMarkItem : bookMarkItem,
            recentList : recentList,
            myBookmarks : myBookmarks,
            getBlogsForSite:getBlogsForSite,
            getSites: getSites,
            getSiteProperties: getSiteProperties,
			createPage: createPage,
            getTemplates:getTemplates,
            getRecentTemplates:getRecentTemplates,
            getRecentSiteFolders:getRecentSiteFolders,
            setRecent:setRecent,
			getChildNodes:getChildNodes,
            createAsset:createAsset,
            getAssetTypes:getAssetTypes,
            getRecentAssetTypes:getRecentAssetTypes,
            getRecentAssetFolders: getRecentAssetFolders,
            getSearchResults:getSearchResults,
			createDummyData:createDummyData,
			getStates:getStates,
			getWorkflows:getWorkflows,
            getFolders:getFolders,
            deleteItem:deleteItem,
            forceDeleteItem:forceDeleteItem
        };
        return api;
    }
);
