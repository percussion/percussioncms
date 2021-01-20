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

define(['knockout', 'pubsub', 'utils', 'dynatree'], function(ko,PubSub,utils,dynatree) {
    return function AssetWizardViewModel(options) {
	
		var self = this;
		self.options = options;
		self.isLoading = ko.observable(false);
		self.isAssetWizardVisible = ko.observable(false);
		
		//Form checking
		self.invalidAssetTypeSelected = ko.observable();
		self.invalidAssetFolderSelected = ko.observable();
		self.emptyContentMessage = ko.observable();
		self.assetRootSelected = ko.observable(false);
	
		//Asset type control observables
		self.assetType = ko.observable();
		self.assetTypes = ko.observableArray();
		self.selectedAssetTypeFilter = ko.observable("");
		self.initialAssetTypesLoad = true;
		self.initialAssetFoldersLoad = true;
		self.emptyRecentAssetTypes = ko.computed(function(){
            return self.selectedAssetTypeFilter() == "Recent" && self.assetTypes().length == 0;
        },self);
		
		//Asset folder control observables
		self.assetFolder = ko.observable();
		self.assetFolders = ko.observableArray();
		self.selectedAssetFolderFilter = ko.observable("");
		self.recentFoldersSelected = ko.computed(function(){
			return self.selectedAssetFolderFilter() == "Recent";
		}, this);
		self.allFoldersSelected = ko.computed(function(){
			return self.selectedAssetFolderFilter() == "All"
		}, this);
        self.dynatreeExists = ko.observable(false);
		self.emptyRecentAssetFolders = ko.computed(function(){
            return self.selectedAssetFolderFilter() == "Recent" && self.assetFolders().length == 0;
        },self);
	
		//Initial setup of the asset window
        PubSub.subscribe("Asset",function(dia, data){
            self.openAddAsset();
        });
	
        //when the primary button is pressed attempt to create the asset
        PubSub.subscribe("Next",function(){
            self.createAsset();
        });
        
        //when the dialog closes clear the window so that it is empty if reopened without refresh
        PubSub.subscribe("Close Dialog",function(){
            self.cleanWindow();
        });
		
		//when the secondary button is pressed go back
        PubSub.subscribe("Cancel",function(dia){
            self.goBack();
        });
		
		self.init = function(content){
		}
		
		self.constants = {
            "SECONDARY_BUTTON_NAME": "Back",
            "SECONDARY_BUTTON_SELECTOR": ".secondary-button > .ui-button-text",
            "MY_RECENT_EMPTY_MSG": "This section will list recent asset types",
            "NOT_AUTHORIZED_ERROR_MSG": "NotAuthorized",
            "NOT_AUTHORIZED_ERROR_MSG_RESPONSE": "You are not authorized to create a page in folder: ",
            "DYNATREE_ID":"#asset-folder-tree"
        };
        
        //sets invalid field observables and returns true if all fields are valid
        function validFields() {
            self.invalidAssetTypeSelected(!self.assetType());
            self.invalidAssetFolderSelected(!self.assetFolder());
			self.assetRootSelected(self.assetFolder() == "/Assets");
            return !self.invalidAssetFolderSelected() && !self.invalidAssetTypeSelected() && !self.assetRootSelected();
        }
        
        self.selectedAssetTypeFilter.subscribe(function() {
			self.assetFolder("");
            if(self.selectedAssetTypeFilter() == "Recent"){
                getRecentAssetTypes();
            }
            else if(self.selectedAssetTypeFilter() == "All"){
                getAllAssetTypes();      
            }
        });
        
		self.selectedAssetFolderFilter.subscribe(function() {
			self.assetType("");
			if (self.selectedAssetFolderFilter() == "Recent") {
				getRecentFolders();
			}
			else if (self.selectedAssetFolderFilter() == "All") {
                self.initDynatree();      
			}
        }); 

        self.cleanWindow = function() {
			self.isAssetWizardVisible(false);
			self.invalidAssetTypeSelected(false);
			self.invalidAssetFolderSelected(false);
			self.emptyContentMessage("");
			self.assetType("");
			self.assetTypes([]);
			self.selectedAssetTypeFilter("");
			self.assetFolder("");
			self.assetFolders([]);
			self.selectedAssetFolderFilter("");
	        self.emptyContentMessage("");
			self.isLoading(false);
			self.initialAssetTypesLoad = true;
			self.initialAssetFoldersLoad = true;
			self.assetRootSelected(false);
        }
        
        self.openAddAsset = function() {
			self.initialAssetTypesLoad = true;
			self.initialAssetFoldersLoad = true;
			self.selectedAssetTypeFilter("Recent");
        	self.selectedAssetFolderFilter("Recent");
            $(self.constants.SECONDARY_BUTTON_SELECTOR).text(self.constants.SECONDARY_BUTTON_NAME);
            self.invalidAssetTypeSelected(false);
            self.invalidAssetFolderSelected(false);
			self.assetRootSelected(false);
			self.isAssetWizardVisible(true);
        }
        
        self.goBack = function() {
            if(self.isAssetWizardVisible()) {
                self.cleanWindow();
                PubSub.publish("Add Content", self);
            }
        }
        
		//createAsset method
        self.createAsset = function() {
            if(self.isAssetWizardVisible()){
                if(validFields()) {
					self.isLoading(true);
                    self.options.cm1Adaptor.createAsset(self.assetFolder(),self.assetType()).fail(function(message){
                        if(message == self.constants.NOT_AUTHORIZED_ERROR_MSG){
                            message = self.constants.NOT_AUTHORIZED_ERROR_MSG_RESPONSE + self.assetFolder();
                        }
                        PubSub.publish("OpenErrorDialog", message);
                    }).always(function(){self.isLoading(false)});
                }
            }
        }
		
		function getRecentFolders(){
            self.isLoading(true);
            self.options.cm1Adaptor.getRecentAssetFolders().done(function(folders){
                self.assetFolders([]);
                ko.utils.arrayForEach(folders, function(folder) {
                    self.assetFolders.push(folder);
                });
                if(folders.length == 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
					if(self.initialAssetFoldersLoad) {
						self.initialAssetFoldersLoad = false;
                        self.selectedAssetFolderFilter("All");
                    }
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        
		//Get recent assets without using site name
		function getRecentAssetTypes(){
            self.isLoading(true);
            self.options.cm1Adaptor.getRecentAssetTypes().done(function(types){
                self.assetTypes([]);
                ko.utils.arrayForEach(types, function(type) {
                    self.assetTypes.push(type);
                });
                if(types.length == 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
					if(self.initialAssetTypesLoad) {
						self.initialAssetTypesLoad = false;
						self.selectedAssetTypeFilter("All");
					}
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        
		//get all asset types
        function getAllAssetTypes(){
            self.isLoading(true);
            self.options.cm1Adaptor.getAssetTypes("yes").done(function(types){
                self.assetTypes([]);
                ko.utils.arrayForEach(types, function(type) {
                    self.assetTypes.push(type);
                });
				if(types.length == 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        
        self.initDynatree = function(){
               // --- Initialize sample trees
            if (self.allFoldersSelected()) {
                self.dynatreeExists(true);
                $(self.constants.DYNATREE_ID).dynatree({
                    children: [{
                        title: "Assets",
                        "isFolder": true,
                        "isLazy": true,
						"noLink": true,
                        "key": "/Assets"
                    }],
                    clickFolderMode: 1,
                    onLazyRead: function(node){
                        self.options.cm1Adaptor.getFolders(node.data.key).done(function(folders){
                            var childNodes=[];
                            ko.utils.arrayForEach(folders.PathItem, function(folder) {
                                if(!folder.leaf)
                                    childNodes.push({title: folder.name, "isLazy": true, "noLink": true, "isFolder": true, key: folder.path});
                            });
                            node.addChild(childNodes);
                            node.setLazyNodeStatus(DTNodeStatus_Ok);
                        }).fail(function(){
                            
                        });
                    },
                    onActivate:function(node){
                        self.assetFolder(node.data.key);
                    },
                    classNames: {
                        expander:"dynatree-expander fa"
                    }
                });
            }
        }
		
		
		 ko.bindingHandlers.slideVisible = {
            init: function(element, valueAccessor) {
                var value = valueAccessor();
                $(element).toggle(ko.unwrap(value));
            },
            update: function(element, valueAccessor) {
                var value = valueAccessor();
                ko.unwrap(value) ? $(element).slideDown() : $(element).slideUp();
            }
        };
    }
});