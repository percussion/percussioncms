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

define(['knockout', 'pubsub'], function(ko,PubSub) {
    return function SearchViewModel(options) {
    
        var self = this;
        self.options = options;
        
        self.constants = {
            "DEFAUL_TITLE":"Dialog",
            "SECONDARY_BUTTON_NAME": "Cancel",
            "SECONDARY_BUTTON_TEXT_SELECTOR": ".secondary-button > .ui-button-text",
            "PRIMARY_BUTTON_TEXT_SELECTOR": ".primary-button > .ui-button-text",
            "PRIMARY_BUTTON_NAME": "Search",
            "NO_CONTENT_TYPE_SELECTED_ERROR": "Select a content type",
            "NO_SITE_AVAIABLE_MESSAGE": "You must first create a site"
        };
        
        self.isLoading = ko.observable(false);
        self.isSearchVisible = ko.observable(false);
		self.invalidSearch = ko.observable(false);
        
        //Keyword filter observables
        self.keyword = ko.observable("");
        
        //File Name filter observables
        self.fileName = ko.observable("");
        
        //Last Modified filter observables
        self.lastModified = ko.observable("");
        
        //Workflow filter observables
        self.workflow = ko.observable("");
        self.workflows = ko.observableArray([]);
        
        //Status filter observables
        self.status = ko.observable("");
        self.enableStatusSelect = ko.computed(function() {
            return (self.workflow());
        },this);
        self.statuses = ko.observableArray([]);
		self.statusCaption = ko.computed(function(){
			return self.enableStatusSelect() ? "All" : "Select Workflow to enable";
		}, self);
        
        //Type filter observables
        self.selectedTypeFilter = ko.observable("");
        
        //Site filter observables
        self.site = ko.observable("");
        self.sites = ko.observableArray([]);
        self.multipleSites = ko.computed(function() {
            return (self.sites().length > 1);
        }, self);
        
        //Template filter observables
        self.template = ko.observable("");
        self.enableTemplateSelect = ko.computed(function() {
            return (self.site());
        },this);
        self.templates = ko.observableArray([]);
		self.templateCaption = ko.computed(function(){
            return self.enableTemplateSelect() ? "All" : "Select Site to enable";
        }, self);
        
        //Asset Type filter observables
        self.assetType = ko.observable("");
        self.assetTypes = ko.observableArray([]);
        
        //Advanced search observables
        self.isAdvancedSearch = ko.observable(false);
        
        //Display search dialog on button click
        PubSub.subscribe("Search",function(dia, data){
            self.openSearch();
        });
        
        PubSub.subscribe("Next",function(){
           if (self.isSearchVisible() && validFields()) {
               var randomNum = Math.floor(Math.random() * 100000);
               var criteria = generateSearchCriteria();
               var searchInfo = {
                   "id": randomNum,
                   "criteria": criteria
               };
               PubSub.publish("Search Executed", searchInfo);
               self.closeDialog();
           }
        });
        
        PubSub.subscribe("Cancel",function(dia){
            self.closeDialog();
        });
        
        PubSub.subscribe("Close Dialog",function(){
            self.cleanWindow();
        });
        
        self.init = function(content){
        }
        
        self.site.subscribe(function() {
            self.template("");
            if (self.site()) {
                getTemplates();
            }
        });
        
        self.workflow.subscribe(function() {
            self.status("");
            if (self.workflow()) {
                getStatuses();
            }
        });
        
        
        self.openSearch = function() {
            $(self.constants.SECONDARY_BUTTON_TEXT_SELECTOR).text(self.constants.SECONDARY_BUTTON_NAME);
            $(self.constants.PRIMARY_BUTTON_TEXT_SELECTOR).text(self.constants.PRIMARY_BUTTON_NAME);
            //get Sites
            self.options.cm1Adaptor.getSites().done(function(sites){
                ko.utils.arrayForEach(sites, function(siteData) {
                    self.sites.push(siteData);
                });
                // If there are not multiple sites then the selector is disabled and only one option exists for selected site
                if(!self.multipleSites() && self.sites()[0]) {
                    self.site(self.sites()[0]);
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            });
            getWorkflows();
            getAssetTypes();
            self.isSearchVisible(true);
        }
        
        self.closeDialog = function() {
            if(self.isSearchVisible()) {
                self.cleanWindow();
                $(".base-dialog").dialog("close");
            }
        }
        
        self.cleanWindow = function() {
            self.isSearchVisible(false);
			self.invalidSearch(false);
            self.keyword("");
            self.fileName("");
            self.lastModified("");
            self.workflow("");
            self.workflows([]);
            self.status("");
            self.statuses([]);
            self.selectedTypeFilter("");
            self.site("");
            self.sites([]);
            self.template("");
            self.templates([]);
            self.assetType("");
            self.assetTypes([]);
        }
        
        function generateSearchCriteria(){
            var type = self.selectedTypeFilter();
            var folderPath = "";
            if(type=="Assets"){
                folderPath = "//Folders/$System$/Assets";
            }
            else if(type=="Pages"){
                folderPath = "//Sites/";
                if(self.site()) {
                    folderPath += self.site().name
                }
            }
            var searchCriteriaObj = {"SearchCriteria":{"query":self.keyword(),"folderPath":folderPath, "sortColumn":"sys_title","sortOrder":"asc","formatId":-1}};
            var searchFields = [];
            if (self.fileName() != "")
                searchFields.push({"key": "sys_title", "value" : self.fileName()});        
            if (self.lastModified() != "")
                searchFields.push({"key": "sys_contentlastmodifier", "value" : self.lastModified()});        
            if (self.workflow())
                searchFields.push({"key": "sys_workflowid", "value" : self.workflow().id});
            if (self.status())
                searchFields.push({"key": "sys_contentstateid", "value" : self.status().id});        
            if(type=="Pages"){
                if (self.template())
                    searchFields.push({"key": "templateid", "value" : self.template().id});
            }
            else if(type=="Assets"){
                if (self.assetType())
                    searchFields.push({"key": "sys_contenttypeid", "value" : self.assetType().contenttypeid});
            }
            searchCriteriaObj.SearchCriteria["searchFields"] = {"entry":searchFields};
            return searchCriteriaObj;
        }       
        function validFields(){
            self.invalidSearch(!self.keyword() && !self.fileName() && !self.lastModified() && !self.workflow() && !self.status() && !self.template() && !self.assetType() && !self.site());
            return !self.invalidSearch();
        }
        
        function getTemplates(){
            self.isLoading(true);
            self.options.cm1Adaptor.getTemplates(self.site().name).done(function(templates){
                self.templates([]);
                ko.utils.arrayForEach(templates, function(template) {
                    self.templates.push(template);
                });
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
		

		function getStatuses(){
            self.isLoading(true);
            self.options.cm1Adaptor.getStates(self.workflow().id).done(function(statuses){
                self.statuses([]);
                ko.utils.arrayForEach(statuses, function(status) {
                    self.statuses.push(status);
                });
                if(statuses.length == 0){
                    //TODO empty logic
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
		

		function getWorkflows() {
            self.isLoading(true);
            self.options.cm1Adaptor.getWorkflows().done(function(workflows){
                self.workflows([]);
                ko.utils.arrayForEach(workflows, function(workflow) {
                    self.workflows.push(workflow);
                });
                if(workflows.length == 0){
                    //TODO empty logic
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
		}
		
		//Load all assets
        function getAssetTypes(){
            self.isLoading(true);
            self.options.cm1Adaptor.getAssetTypes("no").done(function(types){
                self.assetTypes([]);
                ko.utils.arrayForEach(types, function(type) {
                    self.assetTypes.push(type);
                });
                if(types.length == 0){
                    //TODO empty logic
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
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