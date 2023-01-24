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

/**
 * PercTemplateLibraryWidget.js
 *
 * Author: Jose Annunziato
 *
 * +-------------------------[W]+
 * |[Site Filter |V]            |
 * |+---------+ +---------+     |
 * ||   [T]   | |   [T]   | ... |
 * |+---------+ +---------+     |
 * |[Temp Name] [Temp Name]     |
 * +----------------------------+
 *
 * [W] - widget is rendered as an HTML TABLE
 * [T] - template is rendered as an HTML DIV inside an HTML TD
 *      Custom attribute data-base-template maintains the name of the original base template
 *
 * Gestures handled by this widget
 * [Site Filter |V]
 *      (*) Selects site and reloads templates for that site
 *      (*) id = perc-templates-filter
 *      (*) values = 'base' | site name
 *          (*) 'base' = loads base templates
 * [Temp Name] - click to change the template's name. Ordered alphabetically
 *
 */
(function($)
{
	$.widget("ui.template_library", {
		controller: null,
		templates: null,

		_init: function()
		{
			// Instantiate the controller with argument userMock = false
			this.controller = $.PercSiteTemplatesController(false);

			var self = this;

			// Bind the behavior to the Site Silter:
			// Whenever the Site Filter changes load the correponding templates
			$('#perc-templates-filter').on("change",function()
			{
				self._loadTemplates();
			});

			$.PercBlockUI();
			// Make the controller load all its data
			// When we got all the sites data, update the Sites Filter and unblock UI
			//self.controller.load(function()
			//{
			self.controller.getSites(false, function(percSiteTemplatesController, sites)
			{
				self._updateTemplatesFilter(percSiteTemplatesController, sites);
				self._loadTemplates();
				$.unblockUI();
			});
			//});
		},

		_loadTemplates: function()
		{
			// Retrieve the templates based on the selected site (or base templates)
			// We assume that the date is in cache, so we don't assign a callback to the
			// getters
			var self = this;
			var filterVal = $('#perc-templates-filter :selected').val();
			var baseTemplates = self.controller.getBaseTemplates();
			if(filterVal === '' || filterVal === 'base')
			{
				self.templates = jQuery.grep(baseTemplates, function( obj, index ) {
					return ( obj.getTemplateName().indexOf("perc.base.")==0);
				});

				displayTemplates(self);
			}
			else if(filterVal === 'resp')
			{
				self.templates = jQuery.grep(baseTemplates, function( obj, index ) {
					return ( obj.getTemplateName().indexOf("perc.resp.")==0);
				});
				displayTemplates(self);
			}
			else
			{
				// WE ARE LOOKING TO FILTER BY THE USER'S SITE SELECTION
				assignTemplates(filterVal);
			}

			function assignTemplates(siteName) {
				// IF THE CACHE CURRENTLY CONTAINS THE USER'S SITE
				if ($.PercSiteTemplatesController(false).getTemplates("site",filterVal) != '') {

					self.templates = $.PercSiteTemplatesController(false).getTemplates("site",filterVal);
					displayTemplates(self);

				}
				else {
					// SETUP A PROMISE TO WAIT FOR TRIP TO THE DATABASE AND BACK TO UPDATE CACHE WITH TEMPLATES LIST
					$.when(updateTemplates(filterVal)).then(
						function( status ) {
							displayTemplates(self);
						},
						function( status ) {
							console.log( status + ", sites not yet loaded." );
						},
						function( status ) {
							$( "body" ).append( status );
						}
					);
				}
			}

			function updateTemplates(siteName) {

				var defer = $.Deferred();
				$.PercSiteTemplatesController(false).load(function() {
					self.templates = $.PercSiteTemplatesController(false).getTemplates("site",filterVal);
					defer.resolve();
				},false,siteName);
				return defer.promise();
			}

			function properCase(something){
				const arr = something.split(" ");

				for (var i = 0; i < arr.length; i++) {
					arr[i] = arr[i].charAt(0).toUpperCase() + arr[i].slice(1);
				}
				return arr.join(" ");
			}

			function displayTemplates(loadTemplatesContext) {

				loadTemplatesContext.templates.sort(function(x, y)
				{
					var a = String(x.getTemplateName()).toUpperCase();
					var b = String(y.getTemplateName()).toUpperCase();
					if(a > b) return 1;
					if(a < b) return -1;
					return 0;
				});
				loadTemplatesContext.element.html("");
				if(!loadTemplatesContext.templates || loadTemplatesContext.templates.length === 0) return;
				loadTemplatesContext.element.append("<table ><tr>\n");
				global_templates = loadTemplatesContext.templates;

				// buff will hold the html markup to be appended to the table declared above
				var buff = '';
				var tLen = loadTemplatesContext.templates.length;
				for(var t = 0; t < tLen; t++)
				{
					template = loadTemplatesContext.templates[t];
					lastTemp = template;
					buff += '<td><button type="button" data-base-template="' + template.getTemplateName() + '" id="perc-template-' + template.getTemplateId() + '" class="template" style="display:table-cell; ">\n';
					buff += '<img style="border:1px solid #E6E6E9" src="' + template.getImageUrl() + '" class="perc-template-thumbnail"/>\n';
					var theName = template.getTemplateName().replace("perc.base.", "");
					theName = properCase(theName.replace("perc.resp.", ""));
					buff += '<span title="' + theName + '">';
					buff += $.PercTruncateText(theName, 22) + '</span>\n';
					buff += '</button></td>\n';
				}
				loadTemplatesContext.element.find("tr").append(buff);
				enableTemplateSelection();
			}

			function enableTemplateSelection() {
				// Append the selection behavior to each of the template listings
				// THIS ENABLES TEMPLATES UNDER THE ADD TEMPLATES DIALOG TO BE SELECTED
				// TODO: make a more specific selector (the parent should be the base element)
				var templates = $(".template").on({
					"click": function(event){
						// unselect selected div and then select the new div
						$("#perc-template-lib .perc-selected").removeClass("perc-selected");
						$(this).addClass("perc-selected");
					},
					"dblclick": function(event){
						event.stopPropagation();
						event.stopImmediatePropagation();

						$(this).click();
						$("#perc_add_template_dialog_save").trigger("click");

					},
					"keydown": function(event){
						event.stopPropagation();
						event.stopImmediatePropagation();

						if(event.code == "Enter"){
							$(this).dblclick();
						}
					}
				});

				// The first element will be the selected one by default (there will always be at least
				// one element)
				$(templates[0]).addClass('perc-selected');
			}
		},

		/**
		 * Updates the combo box listing all the sites
		 * @param Array(String) sites contains all the sites created in the system
		 */
		_updateTemplatesFilter: function(percSiteTemplatesController, sites)
		{
			$('#perc-templates-filter').html('<option value="base">Base</option><option value="resp">Responsive</option>');
			if (sites)
			{
				for(var s = 0; s < sites.length; s++)
				{
					$('#perc-templates-filter').append('<option value="' + sites[s] + '">' + sites[s] + '</option>');
				}
			}
		},

		destroy: function()
		{
			this._destroy();
		}
	});

	$.extend($.ui.template_library, {
		getter: "value length",
		defaults: {
			option1: "defaultValue",
			dummyvalue: "dummyvalue" // for IE.
		}
	});
})(jQuery);
