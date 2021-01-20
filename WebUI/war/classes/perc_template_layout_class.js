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

var regionWidgetAssociationXmlString = "";

(function($){

	// I'm refactoring most of the code dealing with template manipulation in perc_template_layout_helper to this class
	// but this code is not yet functional
	$.Perc_Template_class = function()
	{
		// loads template xml representation from REST service.
		// invokes parser to convert into a tree of regions
		// root region is this.rootRegion
		this.loadTemplate = function(templateId, type, postCallback)
		{
			var self = this;
	
			$.ajax({
				headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
				url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/" + templateId,
				type: "GET",
				success: function(data, textstatus)
				{
					self.parseTemplateObject(data);
					postCallback("");					
				},
				error : function()
				{
					alert('unable to load template ' + templateId);
				}
			});
		};

		this.parseTemplateObject = function(data)
		{
			this.rootRegion = null;
			var $temp = $(data);
			this.templateObject = data;
			
			// Find region tree first
			var $regionTree = $temp.find("Template regionTree");
			var $rootRegion = $regionTree.find("rootRegion children region:first");
			var $regionWidgetAssociations = $regionTree.find("regionWidgetAssociations");
			var $css = $temp.find("Template cssMarkup");
			
			this.parseRegions($rootRegion, null);
			      
			//TODO: Process widget associations
			this.parseRegionWidgetAssociations($regionWidgetAssociations);
		};

		this.parseRegions = function($region, parent)
		{
			var self = this;
			var prefix =  this.objectType = this.Type.TEMPLATE
				? this.regionTempPrefix
				: this.regionPagePrefix;

			var regionid = $region.children("regionId").text();
			if(prefix == regionid.substr(0, prefix.length))
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
			$children.children("region").each(function()
			{
				self.parseRegions($(this), newRegion);
			});
		};

		// Parse Region Widget Associations and Create Local Data Model 
		this.parseRegionWidgetAssociations = function(regionWidgetAssociations)
		{
			append = true;
			var self = this;
	
			regionWidgetAssociations.find("regionWidget").each(function()
			{
				regionId = $(this).find("regionId").text();
				widgetItems = $(this).find("widgetItems");
				widgetItems.find("widgetItem").each(function()
				{
					definitionId = $(this).find("definitionId").text();
					var widget = self.createNewWidget(definitionId);
			//		var widget = self.createNewWidget("PSWidget_TestProperties");
					widget.setFromXml($(this));
					self.addWidget(widget, regionId, true);
				});
			});
		};
		//===================
		//
		//	Save
		//
		//===================

		this.save = function(type, postCallback)
		{
			var self = this;
				
			var rTree = this.htmlToRegionTree(this.rootRegion.generateHtml(false));	// parse UI's HTML into XML Template Object representation
			var $rTree = $(rTree.responseXML);										// convert XML into jQuery object
			var $root = $rTree.find("Region children region:first");				// get Template Object root region
	
			var $regionTree = $(self.templateObject).find("Template regionTree");	// get local copy of Template Object
			var $rootRegion = $regionTree.find("rootRegion children region:first");	// get Template Object root region
			$rootRegion.replaceWith($root);																	// copy remote root Template Object into local Template Object

			// put region widget associations into local template object
			var regionWidgetAssociationsXmlString = this.rootRegion.generateWidgetAssociationsXml(true);
			var regionWidgetAssociationsXmlDom = $.xmlDOM( regionWidgetAssociationsXmlString );
			var element = regionWidgetAssociationsXmlDom.context.documentElement;
			var $regionWidgetAssociations = $regionTree.find("regionWidgetAssociations");
			$regionWidgetAssociations.replaceWith(element);
	
			$.ajax({
				headers: { 
					'Accept': 'application/xml',
					'Content-Type': 'application/xml' 
				},
				url: $.perc_paths.TEMPLATE_LOAD_SAVE + "/",
				type: "POST",
				data: self.templateObject,
				processData: false,
				success: function(data, textstatus){
					postCallback("success", data);   
				},
				error: function(request, textstatus, error){
					postCallback("error", error);
				}  
			});
		};
	}
      
   $.Perc_Region_class = function(regionid){
	  this.lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur eleifend velit quis risus adipiscing ullamcorper. ";
      this.regionid = regionid;
      this.css = new Object();
      this.vertical = true;
      this.subRegions = null;
      this.widgets = null;
      this.regionparent = null;
      this.dirty = false;
      this.locked = false;
      this.overridable = false;
      this.overriden = false;
      this.pageRegion = false;
		this.widgetHtml = "";

      this.getId = function(){
         return this.regionid;
      }
      
      this.getParent = function(){
         return this.regionparent;
      }
      
      this.nextSibling = function(){
         if(this.regionparent == null)
            return null;
         var len = this.regionparent.getSubRegions().length;
         if(len < 2)
            return null;
         for(var i = 0; i < this.regionparent.getSubRegions().length; i++)
         {
            if(this.regionparent.getSubRegions()[i].regionid == this.regionid)
            {
               if(i < (len - 2))
                  return this.regionparent.getSubRegions()[i + 1];
               return null;      
            }
         }   
      }
      
      this.previousSibling = function(){
         if(this.regionparent == null)
            return null;
         var len = this.regionparent.getSubRegions().length;
         if(len < 2)
            return null;
         for(var i = 0; i < this.regionparent.getSubRegions().length; i++)
         {
            if(this.regionparent.getSubRegions()[i].regionid == this.regionid)
            {
               if(i > 0)
                  return this.regionparent.getSubRegions()[i - 1];
               return null;      
            }
         }   
      }
      
      this.setOverridable = function(overridable)
      {
      	this.overridable = overridable;
      }
      
      this.isOverridable = function()
      {
      	return this.overridable;
      }
      
      this.setOverridden = function(overridden)
      {
      	this.overridden = overridden;
      }
      
      this.isOverridden = function()
      {
      	return this.overridden;
      }
      
      this.setPageRegion = function(pageRegion)
      {
      	this.pageRegion = pageRegion;
      }

      this.isPageRegion = function()
      {
      	return this.pageRegion;
      }
      
      this.isLeaf = function()
      {
      	return (!this.hasWidgets() && !this.hasSubRegions());
      }      
      
      this.setParent = function(theparent){
         this.regionparent = theparent;
      }
      
      this.isDirty = function(){
         return this.dirty;
      }
      
      this.setDirty = function(dirty){
         this.dirty = dirty;
      }
      
      this.isVertical = function(){
         return this.vertical;
      }
      
      this.getSubRegions = function(){
         return this.subRegions;
      }
      
      this.getWidgets = function(){
         return this.widgets;
      }
      
      this.hasSubRegions = function(){
         return (this.subRegions != null && this.subRegions.length > 0);
      }
      
      this.hasWidgets = function(){
         return  (this.widgets != null && this.widgets.length > 0);
      }
      
      this.addSubRegion = function(region, append){
         if(this.hasWidgets())
         {
            alert("Cannot add a region if widgets exist.");
            return;
         }
         if(this.subRegions == null)
            this.subRegions = new Array();
         if(append)
            this.subRegions.push(region);
         else
            this.subRegions.unshift(region);
         region.setParent(this);                           
      }
      
      this.insertSubRegion = function(region, index){
         if(this.hasWidgets())
         {
            alert("Cannot insert a region if widgets exist.");
            return;
         }
         if(this.subRegions == null)
            this.subRegions = new Array();
         if(this.subRegions.length == 0 || this.subRegions.length < index)
            this.subRegions.push(region);
         else
            this.subRegions.splice(index, 0, region);
         region.setParent(this);                           
      }         
         
      this.addWidget = function(widget, append)
      {
         if(this.hasSubRegions())
         {
            alert("Cannot add a widget if sub-regions exist.");
            return;
         } 
         if(this.widgets == null)
            this.widgets = new Array();
         if(append)   
            this.widgets.push(widget);
         else
            this.widgets.unshift(widget);
      }
      
      this.insertWidget = function(widget, index){
         if(this.hasSubRegions())
         {
            alert("Cannot insert a region if widgets exist.");
            return;
         }
         if(this.widgets == null)
            this.widgets = new Array();
         if(this.widgets.length == 0 || this.widgets.length < index)
            this.widgets.push(widget);
         else
            this.widgets.splice(index, 0, widget);         
      }   
      
      this.getSubRegion = function(regionid){
         if(this.subRegions == null)
            return null;
         for(var i = 0; i < this.subRegions.length; i++)
         {
            var sub = this.subRegions[i];
            if(sub.regionid == regionid)
            {
               return sub;
            }  
         }
         return null;       
      }
      
      this.getIndex = function(){
         if(this.regionparent != null)
         {
            var subs = this.regionparent.getSubRegions();
            for(var i = 0; i < subs.length; i++)
            {
               var sub = subs[i];
               if(sub.regionid == this.regionid)
                  return i;
            }
         }
         return -1;
      }
      
		this.getWidget = function(widgetid)
		{
			if(this.widgets == null)
				return null;
			for(var i = 0; i < this.widgets.length; i++)
			{
				var widget = this.widgets[i];
				if(widget.widgetid == widgetid)
				{
					return widget;
				}  
			}
			return null;       
		}
      
      this.removeSubRegion = function(regionid){
         if(this.subRegions == null)
            return;
         for(var i = 0; i < this.subRegions.length; i++)
         {
            var reg = this.subRegions[i];
            if(reg.regionid == regionid)
            {
               this.subRegions.splice(i, 1);
               reg.setParent(null);
               return reg;
            }  
         }            
      }
      
      this.removeWidget = function(widgetid){
         if(this.widgets == null)
            return;
         for(var i = 0; i < this.widgets.length; i++)
         {
            var widget = this.widgets[i];
            if(widget.widgetid == widgetid)
            {
               this.widgets.splice(i, 1);
               return widget;
            }  
         }    
      }
      
      this.setVertical = function(bool){
         this.vertical = bool;
      }
      
      this.getCss = function(){
         return this.css;
      }
      
      this.setCss = function(css){
         if(css == null)
            return;
         this.css = css;   
      }
      
      this.setCssAttribute = function(name, val){
         this.css[name] = val;
      }
      
      this.clearCss = function(){
         this.css = new Object();
      }
      
      this.generateInnerVelocity = function(){
         var buff = "";
         buff += "<div class=\"";
         buff += this.vertical ? "perc-vertical" : "perc-horizontal";
         buff += "\">";
         if(this.hasSubRegions())
         {
             for(var reg in this.subRegions)
             {
                buff += this.subRegions[reg].generateInnerVelocity();
             }
             // Add a clear float if horizontal
             if(!this.vertical)
                buff += "<div class=\"clear-float\"></div>";
                        
         }
         else
         {
             //leaf
		//	buff += "#perc_region('" + this.regionid + "' '' '' '' '')";
			buff += "#region('" + this.regionid + "' '' '' '' '')";
         }
         buff += "</div>";
         return buff;
      }
	// generate regionWidgetAssociations XML element starting from this region as the root
	this.generateWidgetAssociationsXml = function(wrap)
	{
		regionWidgetAssociationXmlString = "";
		if(wrap)
			regionWidgetAssociationXmlString = "<regionWidgetAssociations>\n";
		
		// if this region has widgets, then there are no more subregions since you cant mix regions and widgets
		// generate the regionWidgetXml for this root region and return
		if(this.hasWidgets())
		{
			regionWidgetAssociationXmlString += this.getRegionWidgetXmlString();
			if(wrap)
				regionWidgetAssociationXmlString += "</regionWidgetAssociations>\n";
			return regionWidgetAssociationXmlString;
		}
		
		// if this root region does not have a widget then visit the subregions recursively
		// getting the regionWidget associations for each of the regions and append them to the buffer 
		for(r in this.subRegions)
		{
			this.getRegionWidgetsXmlString(this.subRegions[r]);
		}
		if(wrap)
			regionWidgetAssociationXmlString += "</regionWidgetAssociations>\n";
		return regionWidgetAssociationXmlString;
	}

	this.getRegionWidgetsXmlString = function(region)
	{
		regionWidgetAssociationXmlString += region.getRegionWidgetXmlString();
		for(r in region.subRegions)
		{
			this.getRegionWidgetsXmlString(region.subRegions[r]);
		}
	} 

	this.getRegionWidgetXmlString = function()
	{
		if(!this.hasWidgets())
			return "";
			
		var buff = "";
		buff  = "	<regionWidget>\n";
		buff += "		<regionId>"+this.regionid+"</regionId>\n";
		buff += "		<widgetItems>\n";
		for(w in this.widgets)
		{	
			buff += "			<widgetItem>\n";
			buff += "				<definitionId>"+this.widgets[w].getWidgetDefinitionId()+"</definitionId>\n";
			buff += "				<id>"+this.widgets[w].getId()+"</id>\n";
			buff += "				<name>"+this.widgets[w].getId()+"</name>\n";
			buff += "				<properties>\n";
			for(property in this.widgets[w].properties)
			{
				buff += "					<property>\n";
				buff += "						<name>"+property+"</name>\n";
				buff += "						<value>"+this.widgets[w].properties[property]+"</value>\n";
				buff += "					</property>\n";
			}
			buff += "				</properties>\n";
			buff += "			</widgetItem>\n";
		}
		buff += "		</widgetItems>\n";
		buff += "	</regionWidget>\n";
		return buff;
	}

		this.innerHtml = "";

		this.setInnerHtml = function(html)
		{
			this.innerHtml = html;
		}

		this.render = function(html)
		{
			alert(['#'+this.regionid, html]);
			$('#'+this.regionid).replaceWith(html);
		}

		this.generateHtml = function(isForServerRendering)
		{
			var buff = "";
			var clazz = "perc-region";
			if(!this.hasSubRegions())
				clazz += " perc-region-leaf";
			buff += "<div class=\"";
			buff += clazz;
			buff += "\" ";
			buff += ("id=\"" + this.regionid + "\" title=\"" + this.regionid + "\">");
	
			buff += "<div class=\"";
			buff += this.vertical ? "perc-vertical" : "perc-horizontal";
			buff += "\">";
	         
			if(this.hasSubRegions())
			{
				for(var reg in this.subRegions)
				{
					buff += this.subRegions[reg].generateHtml(isForServerRendering);
				}
				// Add a clear float if horizontal
				if(!this.vertical)
					buff += "<div class=\"clear-float\"></div>";           
			}
			else if(this.hasWidgets())
			{
				if(isForServerRendering)
				{
					buff += "#region('" + this.regionid + "' '' '' '' '')";
				}
				else
				{
					for(var widget in this.widgets)
					{
						widgetId = this.widgets[widget].getId();
						buff += this.widgets[widget].render();
					}
				}
			}
			buff += "</div></div>";
			return buff;
		}

		this.setWidgetHtml = function(html)
		{
			this.widgetHtml = html;
		}

		this.getWidgetHtml = function()
		{
			return this.widgetHtml;
		}
	}

	//==============================
	//
	//	Widget Class
	//
	//==============================

	$.Perc_Widget_class = function(widgetid, widgetDefId, parentRegion)
	{
		this.parentRegion = parentRegion;
		this.widgetid = widgetid;
		this.widgetname = null;
		this.widgetDefinitionId = widgetDefId;
		this.html = null;
		this.assetIds = new Array();
		this.properties = new Object();
		this.widgetItemXml = null;
		
		this.getParentRegion = function()
		{
			return this.parentRegion;
		}
		
		//	parse xml to initialize this widget.
		//	pass in the widgetId element of the template or page object
		this.setFromXml = function(xml)
		{
			var self = this;
			this.widgetItemXml = xml;
			// retrieve the definitionId
			this.widgetDefinitionId = xml.find("definitionId").text();
			this.widgetid = xml.find("id").text();

			// retrieve the properties
			xml.find("properties property").each(function()
			{
				var propertyName  = $(this).find('name').text();
				var propertyValue = JSON.parse($(this).find('value').text());
				if(propertyName != null && propertyName != "")
					self.properties[propertyName] = propertyValue;
			});
		}
		
		this.setProperty = function(propertyName, propertyValue)
		{
			this.properties[propertyName] = propertyValue;
		}
		
		this.alert = function()
		{
			var msg = "";
			for(p in this.properties)
			{
				msg += "" + p + " = " + this.properties[p] + "\n";
			}
			alert("Widget Properties: \n" + msg);
		}
		
		this.setHtml = function(html)
		{
			this.html = html;
		}

		this.render = function()
		{
			var buff = "<div title='"+this.widgetid+"' id='"+this.widgetid+"' class='perc-widget'>";
			if(this.html != null)
			{
				buff += this.html;
			}
			else
			{
				buff += "&nbsp;";
			}
			buff += "</div>";
			return buff;
		}

		this.getHtml = function(regionid)
		{
			var buff = "<div class=\"perc-widget\" ";
			buff += ("id=\"" + this.widgetid + "\">");
			if(this.html != null)
			{
				buff += this.html;
			//	buff += "#region('" + regionid + "' '' '' '' '')";
			}
			else
			{
			//	buff += "#region('" + regionid + "' '' '' '' '')";
				buff += "<b>Empty ";
				buff += this.widgetDefinitionId;
				buff += " widget</b>";
			}
			buff += "</div>";
			return buff;
		}
      
		this.getProperties = function()
		{
			return this.properties;
		}

		this.getId = function()
		{
			return this.widgetid;
		}

		this.getName = function()
		{
			return this.widgetname;
		}
		
		this.setName = function(name)
		{
			this.widgetname = name;
		}

		this.getWidgetDefinitionId = function()
		{
			return this.widgetDefinitionId;
		}
		
		this.setWidgetDefinitionId = function(widgetDefinitionId)
		{
			this.widgetDefinitionId = widgetDefinitionId;
		}

		this.getAssetIds = function()
		{
			return this.assetIds;
		}
	}
})(jQuery);