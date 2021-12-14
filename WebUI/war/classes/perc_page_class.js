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

var regionWidgetAssociationXmlString = "";

(function($)
{
	$.Perc_Page_class = function()
	{
		this.id = null;
		this.name = null;
		this.templateId = null;
		this.templateRootRegion = null;
		this.templateObjectXml;
		this.pageXml;
		
		// overrides
		this.pageRegions = new Array();	// region overrides
		this.pageWidgets = new Array();	// widget overrides

		//---------------------------------
		//	Loading and Parsing Page Object
		//---------------------------------

		/**
		 *	Retrieve the page object from the REST service in XML format
		 *	After loading the page from the REST service,
		 *	retrieve the page id, template id, page name,
		 *	parse regionWidgetAssociations 	
		 */

		this.load = function(pageId, callback)
		{
			var self = this;
			
			$.ajax(
			{
				url: $.perc_paths.PAGE_CREATE + "/" + pageId,
				success: function(xml)
				{
					self.pageXml = xml;
					
					$pageObject = $(self.pageXml);	//	jQuery wrap around Raw XML
					
					self.id = $pageObject.find("Page id:first").text();
					self.templateId = $pageObject.find("Page templateId:first").text();
					self.name = $pageObject.find("Page name:first").text();

					self.getPageRegionHtmlsFromRest();

					callback(self.templateId);
				},
				type: 'GET',
				dataType: 'xml'
			});
			
		}

		this.helper = null;
		this.setHelper = function(helper)
		{
			this.helper = helper;
		}
		
		//----------------------------------------------------------------
		//	Merge Template XML with Page XML into Template XML for Loading
		//----------------------------------------------------------------

		// modifies the templateXml by inserting the page regions and widgets into the templateXml.
		// returns the modified templateXml object
		this.mergePageXmlIntoTemplateXml = function(templateXml)
		{
			// find the overridable regions
			this.findOverridableAndLockedTemplateRegions(templateXml);
			
			var $pageObject = $(this.pageXml);	//	jQuery wrap around Raw XML
			var self = this;
			
			// make a mapping of all the page regions indexed by regionId
			var pageRegions = new Object();
			$pageObject.find('region').each(function()
			{
				pageRegionId = $(this).children('regionId').text();
				pageRegions[pageRegionId] = $(this);
			});
			
			// replace overridable template regions with page corresponding page regions
			for(overridableTemplateRegionId in this.overridableTemplateRegions)
			{
				var pageRegion = pageRegions[overridableTemplateRegionId];
				if(pageRegion)
				{
					var templateRegion	= this.overridableTemplateRegions[overridableTemplateRegionId];
					templateRegion.parent()[0].replaceChild(pageRegion[0], templateRegion[0]);
					self.overrideTemplateRegion(overridableTemplateRegionId);
				}
			}
			
			// append page region widget associations to the template region widget associations
			var $templateWidgets = $(templateXml).find('Template regionTree regionWidgetAssociations');
			$pageObject.find('Page regionBranches regionWidgetAssociations regionWidget').each(function()
			{
				$templateWidgets.append($(this));
				var pageRegionId = $(this).children('regionId').text();
				self.overrideTemplateWidget(pageRegionId);
			});
		};
		
		// retrieve HTML for all the page regions and make a map indexed by regionId
		this.pageRegionHtml = Object();
		this.getPageRegionHtmlsFromRest = function()
		{
			var self = this;
			var $pageObject = $(this.pageXml);	//	jQuery wrap around Raw XML
			$pageRegionWidgets = $pageObject.find("Page regionBranches regionWidgetAssociations regionWidget");
			$pageRegionWidgets.children("regionId").each(function()
			{
				var regionId = $(this).text();
				
				$.ajax(
				{
					url: $.perc_paths.PAGE_PREVIEW + regionId,
					type: "POST",
					contentType: "application/xml",
					dataType: "xml",
					data: self.pageXml,
					processData: false,
					async: false,
					success: function(xml)
					{
						self.pageRegionHtml[regionId] = $(xml).find('result').text();
					},
					error: function(request, textstatus, error)
					{
						alert("Unable to retrieve HTML from REST service: "+error);
					}  
				});
			});
		};

		// finds the template regions that are leafs, i.e., that have no subregions and no widgets
		// returns an array of regions that can be overriden by the page
		this.overridableTemplateRegions = new Object();
		this.lockedTemplateRegions = new Array();
		this.findOverridableAndLockedTemplateRegions = function(templateXml)
		{
			var $templateRootRegion	= $(templateXml).find("Template regionTree rootRegion children region:first");
			this.recursivelyFindTemplateLeafRegions($templateRootRegion);
			this.removeTemplateRegionsWithWidgets(templateXml);
		}
		
		this.recursivelyFindTemplateLeafRegions = function($templateRegion)
		{
			var self = this;
			var regionID = $templateRegion.children("regionId").text();
			var $children = $templateRegion.children("children");
			if($children.children('region').length == 0)
			{
				this.overridableTemplateRegions[regionID] = $templateRegion;
			}
			else
			{
				self.lockedTemplateRegions.push(regionID);
			}
			$children.children("region").each(function()
			{
				self.recursivelyFindTemplateLeafRegions($(this));
			});
		}
		
		this.removeTemplateRegionsWithWidgets = function(templateXml)
		{
			var $template = $(templateXml);
			var $regionWidgetAssociations = $template.find("Template regionTree regionWidgetAssociations");
			var self = this;
			$regionWidgetAssociations.find("regionWidget").each(function()
			{
				var regionId = $(this).find("regionId").text();
				delete self.overridableTemplateRegions[regionId];
				self.lockedTemplateRegions.push(regionId);
			});
		}
		
		// to keep track of the regions that have been added to the page 
		this.overriddenTemplateRegionIds = new Array();
		this.overrideTemplateRegion = function(regionId)
		{
			if(-1 == $.inArray(regionId, this.overriddenTemplateRegionIds))
				this.overriddenTemplateRegionIds.push(regionId);
		}
		
		// to keep track of the widgets that have been added to the page
		this.overriddenTemplateWidgetIds = new Array();
		this.overrideTemplateWidget = function(regionId)
		{
			if(-1 == $.inArray(regionId, this.overriddenTemplateWidgetIds))
				this.overriddenTemplateWidgetIds.push(regionId);
		}
		
		this.setOriginalTemplate = function(templateObjectXml, templateRootRegion)
		{
			this.templateObjectXml  = templateObjectXml;
			this.templateRootRegion = templateRootRegion;
		}

		//-----------------------------------
		//	Saving and Overriding Page Object
		//-----------------------------------
		
		this.clear = function(callback)
		{
			var self = this;
			
			$(self.pageXml).find("Page regionBranches regions").empty();
			$(self.pageXml).find("Page regionBranches regionWidgetAssociations").empty();
		
			$.ajax(
			{
				url: $.perc_paths.PAGE_CREATE + "/",
				type: 'POST',
				contentType: 'application/xml',
				data: self.pageXml,
				dataType: "xml",
				processData: false,
				success: function(data)
				{
					alert('Your page has been cleared and saved successfully.');
					callback();
				},
				error: function(data)
				{
					alert('There was an error clearing your page.');
				}
			});		
		}
		
		this.save = function(postCallback)
		{
			var self = this;
			
			// clear widget ids before saving so that server will create the real ids
			$(self.pageXml).find("Page regionBranches regionWidgetAssociations regionWidget widgetItems widgetItem id").each(function()
			{
				var id = $(this).text();
				var idInt = parseInt(id);
				if(isNaN(idInt) || idInt == 0)
				{
					// fix for IE 7 & 8
//					$(this).remove();
					this.parentNode.removeChild(this);
				}
			});
			
			$.ajax(
			{
			//	url: $.perc_paths.PAGE_CREATE + "/validate",
				url: $.perc_paths.PAGE_CREATE + "/",
				type: 'POST',
				contentType: 'application/xml',
				data: self.pageXml,
				dataType: "xml",
				processData: false,
				success: function(data)
				{
					alert('Your page has been saved successfully.');
					postCallback();
				},
				error: function(data)
				{
					alert('There was an error saving your page.');
				}
			});
		}
		
		//
		//	Extract Page XML from Template XML for Save
		//

		this.extractPageXmlFromTemplateXml = function(templateXml)
		{
			var $template	= $(templateXml);
			var $page		= $(this.pageXml);
			var self		= this;

			// extract regions

			// make a mapping of all the template regions indexed by regionId
			var templateRegions = new Object();
			$template.find('region').each(function()
			{
				templateRegionId = $(this).children('regionId').text();
				templateRegions[templateRegionId] = $(this);
			});
			
			
			// make a mapping of all the page regions indexed by regionId
			var pageRegions = new Object();
			$page.find('region').each(function()
			{
				pageRegionId = $(this).children('regionId').text();
				pageRegions[pageRegionId] = $(this);
			});
			
			// replace page regions with template regions that have been overriden
			// iterate over the overridable template regions,
			// i.e., those regions in the template that have no subregions or widget.
			// if they now have child sub-regions or widgets, then they have been overridden
			for(regionId in this.overridableTemplateRegions)
			{
				var $templateRegion = templateRegions[regionId];
				if($templateRegion && $templateRegion.find('region').length > 0)
				{
					// if the region already existed in the page, then replace it
					// otherwise, append it to the page regions
					var pageRegion = pageRegions[regionId];
					if(pageRegion)
						pageRegion.parent()[0].replaceChild($templateRegion[0], pageRegion[0]);
					else
						$page.find('Page regionBranches regions').append(templateRegions[regionId]);
				}
			}
			
			// extract widgets
			
			// make a mapping of all the template widgets indexed by regionId
			var $templateWidgets = new Object();
			$template.find('regionWidget').each(function()
			{
				regionId = $(this).children('regionId').text();
				$templateWidgets[regionId] = $(this);
			});
			
			
			// make a mapping of all the page widgets indexed by regionId
			var $pageWidgets = new Object();
			$page.find('regionWidget').each(function()
			{
				regionId = $(this).children('regionId').text();
				$pageWidgets[regionId] = $(this);
			});


			// calculate if template region widget regionIds are descendants of overridable regions
			// create a map of template region widgets that should go into the page region widgets 
			var $templateRegionWidgetsForPage = new Array();
			for(regionId in $templateWidgets)
			{
				var $templateRegion = templateRegions[regionId];
				
				// check if this template region widget is overridable
				if(self.isRegionIdOverridable(regionId))
					$templateRegionWidgetsForPage[regionId] = $templateWidgets[regionId];
				
				// chech if ancestors template regions are overridable
				$templateRegion.parents("region").each(function()
				{
					var parentId = $(this).children("regionId").text();
					if(self.isRegionIdOverridable(parentId))
						$templateRegionWidgetsForPage[regionId] = $templateWidgets[regionId];
				});
			} 

			var $pageRegionWidgetAssociations		= $page.find('Page regionBranches regionWidgetAssociations');
			for(var regionId in $templateRegionWidgetsForPage)
			{
				var templateRegionWidget	= $templateRegionWidgetsForPage[regionId];
				var pageRegionWidget		= $pageWidgets[regionId];
				if(templateRegionWidget)
				{
					// if the region widget is already existed in the page, then replace it
					// otherwise, append it to the page region widget associations widgets
					if(pageRegionWidget)
						pageRegionWidget.parent()[0].replaceChild(templateRegionWidget[0], pageRegionWidget[0]);
					else
						$pageRegionWidgetAssociations.append(templateRegionWidget);
				}
			}
		}
		
		this.isRegionIdOverridable = function(regionId)
		{
			for(var id in this.overridableTemplateRegions)
				if(id == regionId)
					return true;
			return false;
		}
		
		this.isAncestor = function(overridableTemplateRegionId, templateWithWidgetRegionId)
		{
			if(overridableTemplateRegionId == templateWithWidgetRegionId)
				return true;
				
				
			
			return false;
		}

		this.htmlToRegionTree = function(html, postCallback)
		{
			return $.ajax(
			{
				url: $.perc_paths.TEMPLATE_HTML_PARSE + "/",
				dataType: "xml",
				async: false,
				type: "POST",
				contentType: "application/xml",
				data: html,
				processData: false
			});
		}
			
		//------------------
		//	
		//------------------
		
		this.getPageObjectXml = function()
		{
			return this.pageXml;
		}
		
		this.getPageId = function()
		{
			return this.pageId;
		}
		
		this.getTemplateId = function()
		{
			return this.templateId;
		}
		
		this.getPageName = function()
		{
			return this.pageName;
		}

		//-------------------
		//	Utility Functions
		//-------------------
		
		/**
		 *	Helper function to be used by the 
		 */
		this.findRegionByIdHelper = function(theRegion, args, results)
		{
			findId = args[0];
			if(theRegion.getId() == findId)
				results.push(theRegion);
		}
		
		this.findRegionById = function(regionId)
		{
			foundRegions = new Array();
			this.traverseRegion(this.templateRootRegion, this.findRegionByIdHelper, [regionId], foundRegions);
			return foundRegions[0];
		}
		
		/**
		 *	Generic function that recursuvely traverses region objects
		 */
		this.traverseRegion = function(region, visit, args, results)
		{
			visit(region, args, results);
			if(!region.hasSubRegions())
				return;
			else
			{
				var subRegions = region.getSubRegions();
				if(subRegions != null)
					for(r in subRegions)
						this.traverseRegion(subRegions[r], visit, args, results);
			}
		}
	}
})(jQuery);
