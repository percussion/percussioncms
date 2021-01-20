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
    return function BlogpostWizardViewModel(options) {
    
        var self = this;
        self.options = options;
        
        self.constants = {
            "SECONDARY_BUTTON_NAME": "Back",
            "SECONDARY_BUTTON_SELECTOR": ".secondary-button > .ui-button-text",
			"BLOG_TITLE_INPUT_ID": "#blog-title-input",
            "BLOG_FILE_INPUT_ID": "#blog-file-input",
			"NOT_AUTHORIZED_ERROR_MSG": "NotAuthorized",
            "NOT_AUTHORIZED_ERROR_MSG_RESPONSE": "You are not authorized to post in this blog.",
			"NOT_AUTHORIZED_ERROR_MSG_RESPONSE_MULTIPLE": "You are not authorized to post in the blog: "
        };
        
        self.isLoading = ko.observable(false);
        self.isBlogWizardVisible = ko.observable(false);
        self.blogTitle = ko.observable("");
        self.blogFile = ko.observable("");
        self.blogs = ko.observableArray();
        self.invalidBlogTitle = ko.observable(false);
		self.blogFileTooLong = ko.observable(false);
        self.invalidBlogFile = ko.observable(false);
        self.invalidBlogSelected = ko.observable(false);
        self.selectedBlog = ko.observable();   
        self.sitesList = ko.observableArray();
        self.multipleSites = ko.computed(function() {
            return (self.sitesList().length > 1);
        }, self);
        
        self.multipleBlogs = ko.computed(function() {
            return self.blogs().length > 1;
        }, self);
		
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
		self.blogTitle.subscribe(function(){
            if (!self.autfillFocusLost()) {
                self.blogFile(self.blogTitle().replace( /[ \_]/g, '-' ).replace( /[^a-zA-Z0-9\-\_]/g, '' ).replace(/[-]+/g, '-').toLowerCase());
            }
        });
        self.blogFile.subscribe(function(){
			self.blogFile(self.blogFile().replace( /[ ]/g, '-' ).replace( /[\\\/\:\*\?\"<\>\|\#\;\%\']/g, '' ));
            if(self.fileHasFocus())
                self.autfillFocusLost(true);
        });
		
        //sets invalid field observables and returns true if all fields are valid
        function validFields() {
            self.invalidBlogSelected(!self.selectedBlog());
            self.invalidBlogFile(self.blogFile().length < 1);
            self.invalidBlogTitle(self.blogTitle().length < 1);
			self.blogFileTooLong(self.blogFile().length > 255);
            return !self.invalidBlogFile() && !self.invalidBlogTitle() && !self.invalidBlogSelected() && !self.blogFileTooLong();
        }
        
        //Initial setup of the blog post window
        PubSub.subscribe("Blog Post",function(dia, data){
            self.openAddBlogPost();
        });
        
        //when the secondary button is pressed go back
        PubSub.subscribe("Cancel",function(dia){
            self.goBack();
        });
        
        //when the primary button is pressed attempt to create the blog
        PubSub.subscribe("Next",function(){
            self.createBlog();
        });
        
        //when the dialog closes clear the window so that it is empty if the reopened without refresh
        PubSub.subscribe("Close Dialog",function(){
            self.cleanWindow();
        });
		
        
        self.init = function(content){
        }
        
        self.cleanWindow = function() {
            self.isBlogWizardVisible(false);
			self.blogFileTooLong(false);
            self.invalidBlogTitle(false);
            self.invalidBlogFile(false);
            self.invalidBlogSelected(false);
            self.blogTitle("");
            self.blogFile("");
            self.selectedBlog("");
			self.titleHasFocus(false);
            self.titleGainedFocus(false);
            self.autfillFocusLost(false);
			self.fileHasFocus(false);
        }
        
        self.openAddBlogPost = function() {
            $(self.constants.SECONDARY_BUTTON_SELECTOR).text(self.constants.SECONDARY_BUTTON_NAME);
            self.invalidBlogTitle(false);
			self.blogFileTooLong(false);
            self.invalidBlogFile(false);
            self.blogs([]);
            self.sitesList([]);
            self.options.cm1Adaptor.getSites().done(function(sites){
                ko.utils.arrayForEach(sites, function(siteData) {
                    self.sitesList.push(siteData);
                    self.options.cm1Adaptor.getBlogsForSite(siteData.name).done(function(blogs){
                        ko.utils.arrayForEach(blogs, function(blog) {
                            blog.site = siteData.name;
                            self.blogs.push(blog);
                        });
                    }).fail(function(message){
                        PubSub.publish("OpenErrorDialog", message);
                    }).always(function(){
                       self.isLoading(false);
                       self.isBlogWizardVisible(true);
                    });
               });
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            });
        }
        
        self.goBack = function() {
            if(self.isBlogWizardVisible()) {
                self.cleanWindow();
                PubSub.publish("Add Content", self);
            }
        }
        
        self.createBlog = function() {
            if(self.isBlogWizardVisible()){
                // If there are not multiple blogs then the selector is disabled and only one option exists for selected blog
                if(!self.multipleBlogs() && self.blogs()[0]) {
                    self.selectedBlog(self.blogs()[0]);
                }
                if(validFields()) {
					self.options.cm1Adaptor.getSiteProperties(self.selectedBlog().site, function(status, result) {
						if(status == "success") { //replace it with constant $.PercServiceUtils.STATUS_SUCCESS
							var fileName = self.blogFile();
							var fileExt = result.SiteProperties.defaultFileExtention;
							if (fileExt && fileName.lastIndexOf(".") < 0) {
								if (fileName.length + fileExt.length < 255){ //consider a dot as one more char
									fileName += "." + fileExt;
								} else {
									fileName = fileName.substring(0, 254 - fileExt.length) + "." + fileExt; //consider a dot as one more char
								}
								self.blogFile(fileName);
							}
						} else {
							console.error('title: Error content: ' + result); //replace it with $.perc_utils.alert_dialog({title: 'Error', content: result});
						}
						self.options.cm1Adaptor.createPage(self.blogFile(), self.blogTitle(), self.selectedBlog().templateId, self.selectedBlog().folderPath).fail(function(message){
							if(message == self.constants.NOT_AUTHORIZED_ERROR_MSG){
								if(self.multipleBlogs())
									 message = self.constants.NOT_AUTHORIZED_ERROR_MSG_RESPONSE_MULTIPLE + self.selectedBlog().title;
								else
									 message = self.constants.NOT_AUTHORIZED_ERROR_MSG_RESPONSE;
							}
							PubSub.publish("OpenErrorDialog", message);
						});
					});
                }
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