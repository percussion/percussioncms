
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

(function($){
   $.perc_template_layout_helper = function(){
   
   /*=============================================================
    *  Vars
    *============================================================*/    
   this.lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur eleifend velit quis risus adipiscing ullamcorper. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed laoreet molestie congue. In tincidunt enim at sem faucibus eu iaculis sem viverra. ";
   this.rootRegion = null;
   this.regionTempPrefix = "temp-region-";
   this.regionPagePrefix = "page-region-";
   this.pseudoWidgetPrefix = "pseudo-widget-id-";
   this.regionIdInc = 0;
   this.widgetIdInc = 0;
   this.templateObjectXml = null;
   this.objectId = null;
   this.objectType = null;
	this.utils = $.perc_utils;

	this.page = null;

   this.Layout = {
      NORTH: "north",
      SOUTH: "south",
      EAST: "east",
      WEST: "west",
      CENTER: "center"   
   };
   this.Type = {
      PAGE: "page",
      TEMPLATE: "template"
   };
   
   
	//===================
	//
	//	Load
	//
	//===================
	
   this.load = function(objectId, type, postCallback){
      var self = this;
      this.objectId = objectId;
      this.objectType = type;

      if(type === this.Type.TEMPLATE)
      {
			self.loadTemplate(objectId, this.Type.TEMPLATE, postCallback);
      }
      else if(type === this.Type.PAGE)
      {
			self = this;
			this.page = new $.Perc_Page_class();
			this.page.setHelper(this);
			this.page.load(objectId, function(templateId)
			{
				self.loadTemplate(templateId, type, postCallback);
			});
      
			// TODO: Apply page overrides to the template object before it is rendered
      }
   };

	// loads template xml representation from REST service.
	// invokes parser to convert into a tree of regions
	// root region is this.rootRegion
	this.loadTemplate = function(templateId, type, callback)
	{
		var self = this;

		$.ajax({
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateId,
			type: "GET",
			success: function(templateXml, textstatus)
			{
				// if we are editing a page, loadTemplate is being invoked right after loading the page.
				if(type === self.Type.PAGE)
				{
					// merge the Page XML and Template XML into the Template XMl and then treat just like editing a template. 
					self.page.mergePageXmlIntoTemplateXml(templateXml);
				}

				self.parseTemplateObjectXml(templateXml, callback);
				//	TODO: make template and page loading independent of each other
				//	create a controller to load page then related template (as an object)
				//	and then notify page of its template object
				if(type === self.Type.PAGE)
				{
					self.page.setOriginalTemplate(self.templateObjectXml, self.rootRegion);
				}
				
				// after the page and/or the template are loaded,
				// and after the page and template are merged,
				// get the html for each of the widgets and update the model 
			//	self.updateAllWidgetHtmlFromRest();
			//	self.templateObjectXml, 'container', null);
				
			},
			error : function()
			{
				alert(I18N.message("perc.ui.template.layout.helper@Load Error") + templateId);
			}
		});
	};

	this.updateAllWidgetHtmlFromRest = function()
	{
	//	alert('updateAllWidgetHtmlFromRest');
		$page = $(this.page.pageXml);
		alert(this.page.pageXml.textContent);
		$pageRegionWidgets = $(this.page.pageXml).find("Page regionBranches regionWidgetAssociations regionWidget regionId");
		$pageRegionWidgets.children("regionId").each(function()
		{
			alert($(this).text());
		});
	};

	this.updateWidgetsHtmlFromRest = function(pageOrTemplateXml, regionId, callback)
	{	
		// find out if the object passed in is a page or a template
		// retrieve the region widget associations element from either
		var $regionWidgetAssociations = null;
		$pageOrTemplateXml = $(pageOrTemplateXml);
		if($pageOrTemplateXml.find("Template").length > 0)
		{
			$regionWidgetAssociations = $pageOrTemplateXml.find("Template regionTree regionWidgetAssociations");
		}
		else
		{
			$regionWidgetAssociations = $pageOrTemplateXml.find("Page regionBranches regionWidgetAssociations");
		}

		
		// find the regionWidget for the regionId parameter from the region widget association
		var $regionWidget = null;
		$regionWidgetAssociations.find("regionWidget").each(function()
		{
			id = $(this).find("regionId").text();
			if(id === regionId)
				$regionWidget = $(this);
		});
		
		/*
		
		var self = this;
			parentRegion = self.getRegion(regionId, self.rootRegion);
			var widget = null;
			var widgets = new Object();
			widgetItems = $(this).find("widgetItems");
			widgetItems.find("widgetItem").each(function()
			{
				definitionId = $(this).find("definitionId").text();
			//	widget = self.createNewWidget("PSWidget_TestProperties", parentRegion);
				widget = self.createNewWidget(definitionId, parentRegion);
				widget.setFromXml($(this));
				self.addWidget(widget, regionId, true);
				// keep track of the widgets for this region
				widgets[widget.getId()] = widget;
			});
			// render each leaf region with widgets and extract the html for each of the widgets
			self.getRegionHtmlFromRest(regionId, function(html)
			{
				// iterate over the DIVs with perc-widget class and retrieve the inner html and set the widgets's html
				$(html).find('.perc-widget').each(function()
				{
					var id = $(this).attr('id');
					widget = widgets[id];
					if(widget != null)
						widget.setHtml($(this).html());
				});
			});
		});
		
		*/
	};

	this.parseTemplateObjectXml = function(xml, callback)
	{
		this.rootRegion = null;
		this.templateObjectXml = xml;
		var $template = $(this.templateObjectXml);
		     
		// Find region tree first
		var $rootRegion					= $template.find("Template regionTree rootRegion children region:first");
		var $regionWidgetAssociations	= $template.find("Template regionTree regionWidgetAssociations");
		var $css						= $template.find("Template cssMarkup");
		var $cssRegion = $template.find("Template cssRegion");
		// With this, the Region CSS is parsed and split into a nice structure.
	//	this.regionCSS = $.perc_css_utils.parse_region_css($cssRegion.text()); 

		// parse xml convert to data model
		this.parseRegions($rootRegion, null);
		this.parseRegionWidgetAssociations($regionWidgetAssociations, callback);
	};
	
   this.parseRegions = function($region, parent)
   {
   	var self = this;
      var prefix =  this.objectType = this.Type.TEMPLATE;
	   this.regionTempPrefix = "temp-region-";
	   this.regionPagePrefix = "page-region-";
      
      var regionid = $region.children("regionId").text();
      if(prefix === regionid.substr(0, prefix.length))
      {
         var idx = parseInt(regionid.substr(prefix.length));
         this.regionIdInc = Math.max(this.regionIdInc, idx);         
      }
      
      var newRegion = new $.Perc_Region_class(regionid);
      if(parent != null)
      {
         parent.addSubRegion(newRegion, true);
      }
      else
      {
         this.rootRegion = newRegion;
      }
      // Get Code
      var $children = $region.children("children");
      var $templateCode = $($children.find("code templateCode:first").text());
      newRegion.setVertical(!$templateCode.hasClass("perc-horizontal"));
      
      // Get regions
      $children.children("region").each(function(){
         self.parseRegions($(this), newRegion);
      });
   };

	// Parse Region Widget Associations and Create Local Data Model Widget Objects
	// adding them to their parent regions
	this.parseRegionWidgetAssociations = function(regionWidgetAssociations, callback)
	{
		append = true;
		var self = this;

		// iterate over the region widget elements extracting region id and widget definition
		regionWidgetAssociations.find("regionWidget").each(function()
		{
			regionId = $(this).find("regionId").text();
			parentRegion = self.getRegion(regionId, self.rootRegion);
			var widget = null;
			var widgets = {};
			widgetItems = $(this).find("widgetItems");
			widgetItems.find("widgetItem").each(function()
			{
				definitionId = $(this).find("definitionId").text();
			//	widget = self.createNewWidget("PSWidget_TestProperties", parentRegion);
				widget = self.createNewWidget(definitionId, parentRegion);
				widget.setFromXml($(this));
				self.addWidget(widget, regionId, true);
				// keep track of the widgets for this region
				widgets[widget.getId()] = widget;
			});

/*
			// render each leaf region with widgets and extract the html for each of the widgets
			$html = $(self.page.pageRegionHtml[regionId]);
			$html.find('.perc-widget').each(function()
			{
				var id = $(this).attr('widgetId');
				widget = widgets[id];
				if(widget != null)
					widget.setHtml($(this).html());
			});
*/
			
		});
		// calls back to the load method in the widget which then renders the regions
		callback();
	};
	
	this.markTemplateRegionOverride = function(regionId)
	{
		this.page.overrideTemplateRegion(regionId);
	};
	
	this.markTemplateWidgetOverride = function(regionId)
	{
		this.page.overrideTemplateWidget(regionId);
	};
	
	//===================
	//
	//	Save
	//
	//===================

	// to clear all regions and widgets from the page to start new
	this.clearPage = function(callback)
	{
	//	this.templateModelToTemplateXml(false);
	//	this.page.extractPageXmlFromTemplateXml(this.templateObjectXml);
		this.page.clear(callback);
	};

   	this.save = function(type, postCallback)
	{
		var self = this;
		
		if(type === this.Type.PAGE)
		{
			this.templateModelToTemplateXml(true);
			this.page.extractPageXmlFromTemplateXml(this.templateObjectXml);
			this.page.save(postCallback);
		}
		else if(type === this.Type.TEMPLATE)
		{
		
			self.templateModelToTemplateXml(true);
		//	self.serializeRegionCSS(false);
	
			// clear widget ids before saving so that server will create the real ids
			$(self.templateObjectXml).find("Template regionTree regionWidgetAssociations regionWidget widgetItems widgetItem id").each(function()
			{
				var id = $(this).text();
				var idInt = parseInt(id);
				if(isNaN(idInt) || idInt === 0)
				{
					// fix for IE 7 & 8
				//	$(this).remove();
					this.parentNode.removeChild(this);
				}
			});
		
			$.ajax(
			{
				 headers: { 
				'Accept': 'application/xml',
				'Content-Type': 'application/xml' 
			},
				url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/",
				type: "POST",
				data: self.templateObjectXml,
				processData: false,
				success: function(data, textstatus)
				{
					postCallback("success", data);   
				},
				error: function(request, textstatus, error)
				{
					postCallback("error", error);
				}  
			});
		}
		else
		{
			//TODO: I18N TEST ME
			alert(I18N.message("perc.ui.template.layout.helper@Save Error"));
		}
   };

	   //===================================
           //      Serializing the Region CSS
	   //===================================
	   
	//   this.serializeRegionCSS = function(isForServerRendering)
	//   {
	//       var self = this;
	//       var serializedCss = $.perc_css_utils.region_css_out(this.regionCSS);
	//       var $regionCSS = self.templateObjectXml.find("Template cssRegion");
	//       $regionCSS.text(serializedCss);	       
	//   }
	   
	//===================
	//
	//	Rendering Widgets
	//
	//===================
	 
	 
	this.templateModelToTemplateXml = function(isForServerRendering)
	{
		var self = this;

		var modelHtml = this.rootRegion.generateHtml(isForServerRendering);

		var rTree = this.htmlToRegionTree(modelHtml);		// parse UI's HTML into XML Template Object representation
		var $rTree = $(rTree.responseXML);										// convert XML into jQuery object

		var $root = $rTree.find("Region children region:first");				// get Template Object root region

		var $regionTree = $(self.templateObjectXml).find("Template regionTree");	// get local copy of Template Object

		var $rootRegion = $regionTree.find("rootRegion children region:first");	// get Template Object root region
		//$rootRegion.replaceWith($root);                        // copy remote root Template Object into local Template Object
		$rootRegion.parent()[0].replaceChild( $root[0], $rootRegion[0] );

		// put region widget associations into local template object
		var regionWidgetAssociationsXmlString = this.rootRegion.generateWidgetAssociationsXml(true);
		var regionWidgetAssociationsXmlDom = $.xmlDOM( regionWidgetAssociationsXmlString );
		var element = regionWidgetAssociationsXmlDom.context.documentElement;
		
		var $regionWidgetAssociations = $regionTree.find("regionWidgetAssociations");
		//$regionWidgetAssociations.replaceWith(element);
		$regionWidgetAssociations.parent()[0].replaceChild( element, $regionWidgetAssociations[0] );
	};

//	this.updateRegionHtmlFromRest = function(regionId)
//	{
//		this.templateModelToTemplateXml();
//		region = this.getRegion();
//	}

	// TODO: deprecated
	this.getTemplateHtmlFromRest = function(callback)
	{
		rootRegionId = this.rootRegion.getId();
		this.getRegionHtmlFromRest(this.templateObjectXml, rootRegionId, callback);
	};

	// TODO: deprecated
   	this.getRegionHtmlFromRest = function(pageOrTemplateXml, regionId, callback)
   	{
//   		callback("<DIV class='perc-raw-html'>Raw HTML Widget</DIV>");
 //  		return;
   	
		var self = this;

		$.ajax(
		{
			
			headers: { 
				'Accept': 'application/xml',
				'Content-Type': 'application/xml' 
			},
			url: $.perc_paths.TEMPLATE_RENDER + "/" + regionId,
			type: "POST",
			data: pageOrTemplateXml,
			processData: false,
			async: false,
			success: function(xml)
			{
				regionHtml = $(xml).find('result').text();
				callback(regionHtml);
			},
			error: function(request, textstatus, error)
			{
				//TODO: I18N TEST ME
				alert(I18N.message("perc.ui.template.layout.helper@REST Error") +error);
			}  
		});

	};
	
	//==============================================
	//
	//	Template, Region, Widget Setters and Getters
	//
	//==============================================
	
	this.addRegion = function(selectedRegionId, position){
      var reg = this.getRegion(selectedRegionId, this.rootRegion);
      if(reg == null)
         return null;
      reg.setOverridden(true);
      
      switch(position)
      {
         case this.Layout.NORTH:
            this._insertNewRegion(reg, true, false);             
            break;
         case this.Layout.SOUTH:
            this._insertNewRegion(reg, true, true);
            break;
         case this.Layout.EAST:
            this._insertNewRegion(reg, false, true);
            break;
         case this.Layout.WEST:
            this._insertNewRegion(reg, false, false);
            break;
         case this.Layout.CENTER:
            //TODO: What exactly does center do??
            break;
         
      }
      return reg;
   };

	// widget factory
	this.createNewWidget = function(widgetDefinitionId, region)
	{
		return new $.Perc_Widget_class(this._createPseudoWidgetId(), widgetDefinitionId, region);
	};
	   
	/**
	 *	Creates new widget object instance and adds it to the region object
	 */
//	this.addWidget = function(widgetname, regionid, append)
	//TODO: I18N TEST ME ALL BELOW
	this.addWidget = function(widget, regionid, append)
	{
		var reg = this.getRegion(regionid, this.rootRegion);	// Find the region for the region Id
		if(reg == null)											// Return null if there is no region
		{
			alert(I18N.message("perc.ui.template.layout.helper@Reigon Does Not Exist"));
			return null;
		}
		if(reg.hasSubRegions())									// Cant mix widgets and regions
		{
			alert(I18N.message("perc.ui.template.layout.helper@Cannot Add Widget"));
			return null;
		}
		reg.setOverridden(true);
		reg.addWidget(widget, append);						// Add widget to the region
		return widget;
	};

	/**
	 *	Recursively iterate over the tree of region objects searching for a region ID
	 */
	this.getRegion = function(regionId, theparent)
	{
		if(theparent == null)
			theparent = this.rootRegion;
		if(theparent.getId() === regionId)
			return theparent;
		if(theparent.hasSubRegions())
		{
			var subs = theparent.getSubRegions();
			for(var reg in subs)
			{
				var result = this.getRegion(regionId, subs[reg]);
				if(result != null)
				return result;
			}
		}
		return null;      
	};
   
   this.getWidgetRegion = function(widgetId, theparent)
   {
      if(theparent == null)
         return null;
      if(theparent.getWidget(widgetId) != null)
         return theparent;
      if(theparent.hasSubRegions())
      {
         var subs = theparent.getSubRegions();
         for(var reg in subs)
         {
            var result = this.getWidgetRegion(widgetId, subs[reg]);
            if(result != null)
               return result;
         }
      }
      return null;         
   };
   
   this.removeRegion = function(regionId){
      var reg = this.getRegion(regionId, this.rootRegion);
      if(reg == null)
         return null;
      var par = reg.getParent();
      if(par == null)
      {
         this.rootRegion = null;
         return reg;
      }
      else if(par.getSubRegions().length > 2)
      {
         return par.removeSubRegion(reg.regionid);
      }
      else
      {
         par.removeSubRegion(reg.getId());
         var temp = par.getSubRegions()[0];
         par.removeSubRegion(temp.regionid);
         par.setVertical(true);
         if(temp.hasSubRegions())
         {
            this._moveSubRegions(temp, par);            
         }
         else if(temp.hasWidgets())
         {
            this._moveWidgets(temp, par);
         }        
         //TODO: This css copy is probably too simple and will need to be changed 
         // to be a merge.
         par.setCss(temp.getCss());
         return reg;
      }
         
   };
   
   this.removeWidget = function(widgetId)
   {
      var reg = this.getWidgetRegion(widgetId, this.rootRegion);
      if(reg != null)
         return reg.removeWidget(widgetId);
      return null;   
   };
   
   this.moveRegion = function(moveRegionId, targetRegionId, before)
   {
      var target = this.getRegion(targetRegionId, this.rootRegion);
      if(target == null)
         throw(I18N.message("perc.ui.template.layout.helper@Target Reigon Does Not Exist"));
      var targetIndex = target.getIndex();
      var moveIndex = before ? targetIndex : targetIndex + 1;
      this.orderSubRegion(moveRegionId, moveIndex);       
   };
     
   this.orderSubRegion = function(regionId, index)
   {
      var reg = this.getRegion(regionId, this.rootRegion);
      var par = reg.getParent();
      if(reg != null && par != null)
      {
         par.insertSubRegion(par.removeSubRegion(regionId), index);
      }   
   };
   
	this.orderWidget = function(widgetId, fromRegionId, toRegionId, pos)
	{
		if(widgetId == null)
		{
			alert(I18N.message("perc.ui.template.layout.helper@Cannot Order Widget") +widgetId);
			return;
		}
		var regFrom = this.getRegion(fromRegionId, this.rootRegion);
		var regTo   = this.getRegion(toRegionId,   this.rootRegion);
		if(regFrom != null && regTo != null)
			regTo.insertWidget(regFrom.removeWidget(widgetId), pos);
		else
			alert(I18N.message("perc.ui.template.layout.helper@Unable To Reorder")+ widgetId+I18N.message("perc.ui.template.layout.helper@From Reigon") +fromRegionId+I18N.message("perc.ui.template.layout.helper@To Reigon")+toRegionId+ I18N.message("perc.ui.template.layout.helper@At Position")+pos+'.\n' + I18N.message("perc.ui.template.layout.helper@One Region Does Not Exist"));
	};
   
    
   /*=============================================================
    *  Private Functions
    *============================================================*/

    // create region ids when adding new regions.
	this._createNewRegionId = function()
	{
		var prefix =  this.objectType = this.Type.TEMPLATE;
		this.regionTempPrefix = "temp-region-";
		this.regionPagePrefix = "page-region-";
		var pseudoId = prefix + (++this.regionIdInc);
		return pseudoId; 
	};

	// create pseudo widget ids when adding new widgets. widgets loaded from SVC already have ids
	this._createPseudoWidgetId = function()
	{
		return this.pseudoWidgetPrefix + (this.widgetIdInc++); 
	};
   
   this._insertNewRegion = function(region, vertical, append){
      var newRegion = new $.Perc_Region_class(this._createNewRegionId(), null);
      newRegion.setPageRegion(true);
      var isOpposite = vertical ? !region.isVertical() : region.isVertical();
      if(region.hasSubRegions())
      {
         if(isOpposite)
         {
             var wrapper = this._wrapSubRegions(region);
             region.setVertical(vertical);
             region.addSubRegion(wrapper, true);             
         }
         region.addSubRegion(newRegion, append);
      }
      else
      {
         var newRegion2 = new $.Perc_Region_class(this._createNewRegionId(), null);
         newRegion2.setVertical(region.isVertical());
         region.setVertical(vertical);
         if(region.hasWidgets())
            this._moveWidgets(region, newRegion2);
         region.addSubRegion(newRegion, true);
         region.addSubRegion(newRegion2, !append);          
	      newRegion2.setPageRegion(true);
      }
   };  
   
   this._wrapSubRegions = function(region){
      var wrapper = new $.Perc_Region_class(this._createNewRegionId(), null);
      wrapper.setVertical(region.isVertical());
      this._moveSubRegions(region, wrapper);
      return wrapper;
   };
   
   this._moveWidgets = function(source, target){
      var widgets = source.getWidgets();
      if(widgets == null || widgets.length === 0)
         return;
      for(var i = widgets.length - 1; i > -1; i--)
      {
         target.addWidget(source.removeWidget(widgets[i].widgetid), false);
      }
   };
   
   this._moveSubRegions = function(source, target){
      var subs = source.getSubRegions();
      if(subs == null || subs.length === 0)
         return;
      for(var i = subs.length - 1; i > -1; i--)
      {
         target.addSubRegion(source.removeSubRegion(subs[i].regionid), false);
      }
   }; 
    
   /*=============================================================
    *  Page Region
    *============================================================*/           

	this.isWidgetAllowed = function(regionId)
	{
		var region = this.getRegion(regionId, this.rootRegion);
		return (region.isOverridable() || region.isPageRegion()) && !region.hasSubRegions();
	};
	
	this.isRegionAllowed = function(regionId)
	{
		var region = this.getRegion(regionId, this.rootRegion);
		return (region.isOverridable() || region.isPageRegion());
	};

	this.isRegionResizable = function(regionId)
	{
		var region = this.getRegion(regionId, this.rootRegion);
		return (region.isOverridable() || region.isPageRegion());
	};
	
	/**
	 *	REST Services
	 */
	 
	this.htmlToRegionTree = function(html, postCallback)
	{
		return $.ajax(
		{
			headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
			url: $.perc_paths.TEMPLATE_HTML_PARSE + "/",
			async: false,
			type: "POST",
			data: html,
			processData: false
		});     
   
	};
	};
})(jQuery);

