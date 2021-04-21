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
    return function PageWizardViewModel(options) {
    
        var self = this;
        self.options = options;
        
        self.constants = {
            "SECONDARY_BUTTON_NAME": "Back",
            "SECONDARY_BUTTON_SELECTOR": ".secondary-button > .ui-button-text",
            "MY_RECENT_EMPTY_MSG": "This section will list recent templates",
			"PAGE_TITLE_INPUT_ID": "#page-title-input",
			"PAGE_FILE_INPUT_ID": "#page-file-input",
			"NOT_AUTHORIZED_ERROR_MSG": "NotAuthorized",
			"NOT_AUTHORIZED_ERROR_MSG_RESPONSE": "You are not authorized to create a page in folder: ",
			"DYNATREE_ID":"#page-folder-tree"
        };
        
        self.isLoading = ko.observable(false);
        self.isPageWizardVisible = ko.observable(false);
        self.pageTitle = ko.observable("");
        self.pageFile = ko.observable("");
        self.pageTemplate = ko.observable();
        self.pageFolder = ko.observable();
        self.templates = ko.observableArray();
        self.selectedTemplateFilter = ko.observable("");
        self.selectedFolderFilter = ko.observable("");
		self.recentFolderSelected = ko.computed(function(){
			return self.selectedFolderFilter() === "Recent";
		}, this);
		self.allFoldersSelected = ko.computed(function(){
			return self.selectedFolderFilter() === "All";
		}, this);
        self.invalidPageTitle = ko.observable(false);
        self.invalidPageFile = ko.observable(false);
        self.invalidSiteSelection = ko.observable(false);
        self.invalidTemplateSelected = ko.observable(false);
        self.invalidFolderSelection = ko.observable(false);
		self.fileNameTooLong = ko.observable(false);
        self.selectedSite = ko.observable();
        self.emptyContentMessage = ko.observable();
        self.sites = ko.observableArray();
		self.folders = ko.observableArray();
        self.multipleSites = ko.computed(function() {
            return (self.sites().length > 1);
        }, self);
		self.dynatreeExists = ko.observable(false);
		self.initialTemplatesLoad = true;
		self.initialFoldersLoad = true;
		
		self.emptyRecentFolders = ko.computed(function(){
			return self.selectedFolderFilter() === "Recent" && self.folders().length === 0;
		},self);
		
		self.emptyRecentTemplates = ko.computed(function(){
            return self.selectedTemplateFilter() === "Recent" && self.templates().length === 0;
        },self);

		//TODO Autofill logic for title and filename
        self.fileHasFocus = ko.observable(false);
        self.titleHasFocus = ko.observable(false);
        self.titleGainedFocus = ko.observable(false);
        self.autfillFocusLost = ko.observable(false);
        self.titleHasFocus.subscribe(function(){
            if (self.titleHasFocus()) {
                self.titleGainedFocus(true);
            }
            if(self.titleGainedFocus() && !self.titleHasFocus()){
                self.autfillFocusLost(true);
            }
        });
        self.pageTitle.subscribe(function(){
            if (!self.autfillFocusLost()) {
                self.pageFile(self.pageTitle().replace( /[ _]/g, '-' ).replace( /[^a-zA-Z0-9\-_.]/g, '' ).replace(/[-]+/g, '-').toLowerCase());
            }
        });
        self.pageFile.subscribe(function(){
			self.pageFile(self.pageFile().replace( /[ ]/g, '-' ).replace( /[\\\/:*?"<>|#;%']/g, '' ));//.replace( /\.*$/g, '' )
            if(self.fileHasFocus())
                self.autfillFocusLost(true);
        });
        
        //sets invalid field observables and returns true if all fields are valid
        function validFields() {
            self.invalidSiteSelection(!self.selectedSite());
            self.invalidFolderSelection(!self.pageFolder());
            self.invalidTemplateSelected(!self.pageTemplate());
            self.invalidPageFile(!self.pageFile());
            self.invalidPageTitle(!self.pageTitle());
			self.fileNameTooLong(self.pageFile().length > 255);
            return !self.invalidPageFile() && !self.invalidPageTitle() && !self.invalidTemplateSelected() && !self.invalidFolderSelection() && !self.invalidSiteSelection() && !self.fileNameTooLong();
        }
        
        //Initial setup of the page post window
        PubSub.subscribe("Page",function(dia, data){
            self.openAddPage();
        });
        
		//Update template and folder lists when a site is selected
        self.selectedSite.subscribe(function() {
			self.pageTemplate("");
            self.pageFolder("");
			if (self.selectedSite()) {
				if (self.selectedTemplateFilter() === "Recent") {
					getRecentTemplates();
				}
				else if (self.selectedTemplateFilter() === "All") {
					getAllTemplates();
				}
				else if(self.multipleSites()){
					self.selectedTemplateFilter("Recent");
				}
				if (self.selectedFolderFilter() === "Recent") {
					getRecentFolders();
				}
				else if (self.selectedFolderFilter() === "All") {
					self.initDynatree();      
				}
                else if(self.multipleSites()){
                    self.selectedFolderFilter("Recent");
                }
			}
			else{
				self.templates([]);
                self.folders([]);
				self.selectedTemplateFilter("");
				self.selectedFolderFilter("");
			}
        });
        
		//Update template list when the filter changes
        self.selectedTemplateFilter.subscribe(function() {
			self.pageTemplate("");
            if(self.selectedSite()){
                if(self.selectedTemplateFilter() === "Recent"){
                    getRecentTemplates();
                }
                else if(self.selectedTemplateFilter() === "All"){
                    getAllTemplates();      
                }
            }
        });
		
		//Update folder list when the filter changes
		self.selectedFolderFilter.subscribe(function() {
			self.pageFolder("");
			if (self.selectedSite()) {
				if (self.selectedFolderFilter() === "Recent") {
					getRecentFolders();
				}
				else if (self.selectedFolderFilter() === "All") {
					self.initDynatree();      
				}
			}
        });
        
        function getRecentTemplates(){
            self.isLoading(true);
            self.options.cm1Adaptor.getRecentTemplates(self.selectedSite().name).done(function(templates){
                self.templates([]);
                ko.utils.arrayForEach(templates, function(template) {
                    self.templates.push(template);
                });
				if(templates.length === 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
					if(self.initialTemplatesLoad){
						self.initialTemplatesLoad = false;
						self.selectedTemplateFilter("All");
					}
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        
        function getAllTemplates(){
            self.isLoading(true);
            self.options.cm1Adaptor.getTemplates(self.selectedSite().name).done(function(templates){
                self.templates([]);
                ko.utils.arrayForEach(templates, function(template) {
                    self.templates.push(template);
                });
				if(templates.length === 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
		
        function getRecentFolders(){
            self.isLoading(true);
            self.options.cm1Adaptor.getRecentSiteFolders(self.selectedSite().name).done(function(folders){
                self.folders([]);
                ko.utils.arrayForEach(folders, function(folder) {
                    self.folders.push(folder);
                });
				if(folders.length === 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
					if(self.initialFoldersLoad){
						self.initialFoldersLoad = false;
						self.selectedFolderFilter("All");
					}
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
						title: self.selectedSite().name,
						"isFolder": true,
						"isLazy": true,
						"noLink": true,
						"key": "/Sites/" + self.selectedSite().name
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
                        self.pageFolder(node.data.key);
                    },
					classNames: {
						expander:"dynatree-expander fa"
					}
				});
			}
        };
        
        self.switchTemplateFilter = function() {};
        
        //when the secondary button is pressed go back
        PubSub.subscribe("Cancel",function(dia){
            self.goBack();
        });
        
        //when the primary button is pressed attempt to create the page
        PubSub.subscribe("Next",function(){
            self.createPage();
        });
        
        //when the dialog closes clear the window so that it is empty if reopened without refresh
        PubSub.subscribe("Close Dialog",function(){
            self.cleanWindow();
        });
        
        self.init = function(content){};
        
        self.cleanWindow = function() {
            self.isPageWizardVisible(false);
            self.invalidPageTitle(false);
            self.invalidPageFile(false);
            self.invalidTemplateSelected(false);
            self.invalidSiteSelection(false);
            self.invalidFolderSelection(false);
            self.pageTitle("");
            self.pageFile("");
            self.pageTemplate("");
            self.pageFolder("");
			self.isLoading(false);
	        self.templates([]);
	        self.selectedSite("");
	        self.emptyContentMessage("");
	        self.sites([]);
			self.folders([]);
			self.titleHasFocus(false);
            self.titleGainedFocus(false);
            self.autfillFocusLost(false);
            self.fileHasFocus(false);
			self.selectedTemplateFilter("");
            self.selectedFolderFilter("");
			self.initialTemplatesLoad = true;
			self.initialFoldersLoad = true;
			if(self.dynatreeExists()){
				$(self.constants.DYNATREE_ID).dynatree("destroy");
				$(self.constants.DYNATREE_ID).empty();
			}
			self.dynatreeExists(false);
			self.fileNameTooLong(false);
        };
        
        self.openAddPage = function() {
			self.initialTemplatesLoad = true;
			self.initialFoldersLoad = true;
            $(self.constants.SECONDARY_BUTTON_SELECTOR).text(self.constants.SECONDARY_BUTTON_NAME);
			self.options.cm1Adaptor.getSites().done(function(sites){
                ko.utils.arrayForEach(sites, function(siteData) {
                    self.sites.push(siteData);
                });
                // If there are not multiple sites then the selector is disabled and only one option exists for selected site
				// The recent filter will be selected by default unless there are no items in the recent list
                if(!self.multipleSites() && self.sites()[0]) {
                    self.selectedSite(self.sites()[0]);
					self.selectedTemplateFilter("Recent");
                    self.selectedFolderFilter("Recent");
                }
				else {
					 self.initialTemplatesLoad = false;
                     self.initialFoldersLoad = false;
				}
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            });
            self.invalidPageTitle(false);
            self.invalidPageFile(false);
			self.fileNameTooLong(false);
			self.isPageWizardVisible(true);
        };
        
        self.goBack = function() {
            if(self.isPageWizardVisible()) {
                self.cleanWindow();
                PubSub.publish("Add Content", self);
            }
        };
        
        self.createPage = function() {
            if (self.isPageWizardVisible()) {
                if (validFields()) {
                    self.isLoading(true);
                    
					self.options.cm1Adaptor.getSiteProperties(self.selectedSite().name, function(status, result) {
						if(status === "success") { //replace it with constant $.PercServiceUtils.STATUS_SUCCESS
							var fileName = self.pageFile();
							var fileExt = result.SiteProperties.defaultFileExtention;
							if (fileExt && fileName.lastIndexOf(".") < 0) {
								if (fileName.length + fileExt.length < 255){ //consider a dot as one more char
									fileName += "." + fileExt;
								} else {
									fileName = fileName.substring(0, 254 - fileExt.length) + "." + fileExt; //consider a dot as one more char
								}
								self.pageFile(fileName);
							}
						} else {
							console.error('title: Error content: ' + result); //replace it with $.perc_utils.alert_dialog({title: 'Error', content: result});
						}
						
						self.options.cm1Adaptor.createPage(self.pageFile(), self.pageTitle(), self.pageTemplate(), self.pageFolder()).fail(function(message){
							if(message === self.constants.NOT_AUTHORIZED_ERROR_MSG){
								message = self.constants.NOT_AUTHORIZED_ERROR_MSG_RESPONSE+ self.pageFolder();
							}
							PubSub.publish("OpenErrorDialog", message);
						}).always(function(){
							self.isLoading(false);
						});
					});
                }
            }
        };
		
		
        
        ko.bindingHandlers.slideVisible = {
            init: function(element, valueAccessor) {
                var value = valueAccessor();
                $(element).toggle(ko.unwrap(value));
            },
            update: function(element, valueAccessor) {
                var value = valueAccessor();
                if(ko.unwrap(value)) {
                    $(element).slideDown();
                } else {
                    $(element).slideUp();
                }
            }
        };
    };
});
