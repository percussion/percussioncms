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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

define(['knockout', 'pubsub'], function(ko,PubSub) {
    return function BaseDialogViewModel(options) {
	
		var self = this;
		self.options = options;
		
		self.constants = {
            "DEFAUL_TITLE":"Dialog",
            "DEFAULT_SECONDARY_BUTTON": "Cancel",
            "DEFAULT_PRIMARY_BUTTON":"Next",
            "CLOSE_ICON": "<i class=\"fa fa-times fa-3\"></i>",
            "NO_SITE_AVAIABLE_MESSAGE": "You must first create a site",
			"UNKNOWN_SERVER_ERROR": ".No message body writer found for response class : PSErrors.",
			"UNKNOWN_SERVER_ERROR_RESPONSE" : "Unknown Server Error. Please try again later."
        };
		
		self.isLoading = ko.observable();
		self.windowTitle = ko.observable(self.constants.DEFAUL_TITLE);
		self.secondaryButton = ko.observable(self.constants.DEFAULT_SECONDARY_BUTTON);
		self.primaryButton = ko.observable(self.constants.DEFAULT_PRIMARY_BUTTON);
		self.isDialogOpen = ko.observable(false);
        self.isErrorMsg = ko.observable();
		self.isConfirmationMsg = ko.observable();
        self.sitesList = ko.observableArray();
        self.errorMessage = ko.observable();
		self.confirmationMessage = ko.observable();
		
        PubSub.subscribe("OpenDialog",function(msg, title){
			self.options.cm1Adaptor.getSites().done(function(sites){
                ko.utils.arrayForEach(sites, function(siteData) {
                    self.sitesList.push(siteData);
				});
				self.windowTitle(title);
                if(title === "Add Content" && self.sitesList().length < 1) {
                    self.errorMessage(self.constants.NO_SITE_AVAIABLE_MESSAGE);
                    setupErrorDialog("Warning");
                }
                else {
                    setupDialog();
                    PubSub.publish(self.windowTitle(), self);
                }
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               //self.isLoading(false);
            });
            
        });
		
		PubSub.subscribe("OpenErrorDialog",function(msg, message){
		    if(message === self.constants.UNKNOWN_SERVER_ERROR) {
                self.errorMessage(self.constants.UNKNOWN_SERVER_ERROR_RESPONSE);
            } else {
                self.errorMessage(message);
            }
            setupErrorDialog("Error");
        });
		
        PubSub.subscribe("OpenConfirmationDialog",function(msg, data){
            setupConfirmationDialog(data);
        });
        PubSub.subscribe("OpenForceDeleteConfirmationDialog",function(msg, data){
            forceDeleteConfirmationDialog(data);
        });
		
		function setupDialog() {
			$( ".base-dialog" ).dialog({
				modal: true,
                position: {my: "center", at: "center", of: window.top},
				title: self.windowTitle(),
				//Change the close icon to fontawesome
				create: function(event, ui) {
					var widget = $(this).dialog("widget");
					$('.ui-dialog-titlebar-close', widget)
					  .empty()
					  .html(self.constants.CLOSE_ICON);
				},
				buttons: [
					{
						text: self.secondaryButton(),
						click: function() {
							PubSub.publish(self.secondaryButton(), self);
						},
						class:"secondary-button"
					},
					{
						text: self.primaryButton(),
						click: function() {
							PubSub.publish(self.primaryButton(), self);
						},
						class: "primary-button"
					}
				],
				beforeClose: function() {
					PubSub.publish("Close Dialog", self);
				},
				width:"800px"
			});	
		}		
		
        function setupErrorDialog(message) {
            self.isErrorMsg(true);
            $( ".base-error-dialog" ).dialog({
				modal: true,
				position: {my: "center", at: "center", of: window.top},
				title: message,
				//Change the close icon to fontawesome
				create: function(event, ui) {
					var widget = $(this).dialog("widget");
					$('.ui-dialog-titlebar-close', widget)
					  .empty()
					  .html(self.constants.CLOSE_ICON);
				},
				buttons: [
					{
						text: "close",
						click: function() {
							$(".base-error-dialog").dialog("close");
						},
						class: "secondary-button"
					}
				],
				width:"800px"
			});
        }
		
	    function setupConfirmationDialog(data) {
            self.confirmationMessage(data.message);
            self.isConfirmationMsg(true);
            var secButton = data.secondaryButton?data.secondaryButton:{"text":"No"};
            $( ".base-confirmation-dialog" ).dialog({
                modal: true,
                position: {my: "center", at: "center", of: window.top},
                title: data.title,
                //Change the close icon to fontawesome
                create: function(event, ui) {
                    var widget = $(this).dialog("widget");
                    $('.ui-dialog-titlebar-close', widget)
                      .empty()
                      .html(self.constants.CLOSE_ICON);
                },
                buttons: [
					{
                        text: secButton.text,
                        click: function() {
							$(".base-confirmation-dialog").dialog("close");
                            if($.isFunction(secButton.callback))
                                secButton.callback();
                        },
                        class: "secondary-button"
                    },
                    {
                        text: data.primaryButton.text,
                        click: function() {
							$(".base-confirmation-dialog").dialog("close");
                            if($.isFunction(data.primaryButton.callback))
                                data.primaryButton.callback();
                        },
                        class: "primary-button"
                    }
                ],
                width:"800px"
            });
        }
        
        function forceDeleteConfirmationDialog(data) {
            self.confirmationMessage(data.message);
            self.isConfirmationMsg(true);
            var secButton = data.secondaryButton?data.secondaryButton:{"text":"No"};
            var $dialog = $( ".base-confirmation-dialog" ).dialog({
                modal: true,
                position: {my: "center", at: "center", of: window.top},
                title: data.title,
                //Change the close icon to fontawesome
                create: function(event, ui) {
                    var widget = $(this).dialog("widget");
                    $('.ui-dialog-titlebar-close', widget)
                      .empty()
                      .html(self.constants.CLOSE_ICON);
                },
                buttons: [
                    {
                        text: secButton.text,
                        click: function() {
                            $(".base-confirmation-dialog").dialog("close");
                            if($.isFunction(secButton.callback))
                                secButton.callback();
                        },
                        class: "secondary-button"
                    },
                    {
                        text: data.primaryButton.text,
                        click: function() {
                            if($dialog.closest(".ui-dialog").find(".primary-button").hasClass("disabled")){
                                return;
                            }
                            $(".base-confirmation-dialog").dialog("close");
                            if($.isFunction(data.primaryButton.callback))
                                data.primaryButton.callback();
                        },
                        class: "primary-button disabled"
                    }
                ],
                width:"800px"
            });
            $dialog.find("#" + data.forceChkBoxId).click(function(){
                $dialog.closest(".ui-dialog").find(".primary-button").toggleClass("disabled");
            });
        }

		self.init = function(content){};
    };
});
