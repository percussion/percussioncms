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
        }
		
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
		}
		
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
		}
		
		self.closeDialog = function() {
			if(self.isAddWizardVisible()) {
				$(".base-dialog").dialog("close");
			}
		}
		
		self.cleanWindow = function() {
			self.blogs([]);
			self.isAddWizardVisible(false);
			self.contentTypeError(false);
            self.selectedContentType("");
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