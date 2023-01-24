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

define(['knockout', 'pubsub'], function(ko,PubSub) {
    return function AddWizardViewModel(options) {
	
		var self = this;
		self.options = options;
		
		self.constants = {
            "DEFAUL_TITLE":"Dialog",
            "SECONDARY_BUTTON_NAME": "Cancel",
			"SECONDARY_BUTTON_TEXT_SELECTOR": ".secondary-button > .ui-button-text",
			"PRIMARY_BUTTON_SELECTOR": ".primary-button",
            "NO_CONTENT_TYPE_SELECTED_ERROR": "Select type of content to add",
			"NO_SITE_AVAIABLE_MESSAGE": "You must first create a site"
        };
		
		self.isLoading = ko.observable(false);
		self.isAddWizardVisible = ko.observable(false);
		self.selectedContentType = ko.observable("");
        self.blogs = ko.observableArray();
        self.isBlogAvaiable = ko.computed(function() {
            return (self.blogs().length > 0);
        }, self); //TODO implement with cm1adaptor
        
		self.contentTypeError = ko.observable(false);
		self.addWizardErrorMessage = ko.observable(self.constants.NO_CONTENT_TYPE_SELECTED_ERROR);
        
        PubSub.subscribe("Add Content",function(dia, data){
			self.openAddWizard();
        });
		
		PubSub.subscribe("Next",function(){
			self.openNextWizardPanel();
		});
		
		PubSub.subscribe("Cancel",function(dia){
			self.closeDialog();
		});
		
		PubSub.subscribe("Close Dialog",function(){
			self.cleanWindow();
		});
		
		self.init = function(content){
        };
		
		self.openAddWizard = function() {
			$(self.constants.SECONDARY_BUTTON_TEXT_SELECTOR).text(self.constants.SECONDARY_BUTTON_NAME);
			self.options.cm1Adaptor.getSites().done(function(sites){
                ko.utils.arrayForEach(sites, function(siteData) {
                    self.options.cm1Adaptor.getBlogsForSite(siteData.name).done(function(blogs){
                       ko.utils.arrayForEach(blogs, function(blog) {
                           self.blogs.push(blog);
                       });
                    }).fail(function(message){
                        PubSub.publish("OpenErrorDialog", message);
                    }).always(function(){
                       self.isLoading(false);
                    });
               });
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            });
			self.isAddWizardVisible(true);
			self.contentTypeError(false);
		};
		
		self.openNextWizardPanel = function() {
			if(self.isAddWizardVisible()) {
				if(self.selectedContentType()){
					PubSub.publish(self.selectedContentType(), self);
                    self.cleanWindow();
				}
				else {
					self.contentTypeError(true);
				}
			}
		};
		
		self.closeDialog = function() {
			if(self.isAddWizardVisible()) {
				$(".base-dialog").dialog("close");
			}
		};
		
		self.cleanWindow = function() {
			self.blogs([]);
			self.isAddWizardVisible(false);
			self.contentTypeError(false);
            self.selectedContentType("");
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
