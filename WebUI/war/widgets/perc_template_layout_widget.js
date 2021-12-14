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

// TODO: remove this global variable
var gSelectTemp = null;

/**
 *	This widget adds decorations and behaviors to a set of nested DIVs
 *	to allow adding additional sibling and child DIVs, reordering DIVs,
 *	resizing DIVs, adding widgets, and configuring DIVs and widgets.
 */

(function($)
{
	$.widget("ui.template_layout",
	{
		helper : null,
		showBorders : true,
		pageId : null,
		type : null,
		controller : null,
		dropPosition : null,
		hoveringRegion : null,
		POSITION_NORTH : 0,
		POSITION_WEST : 3,
		selectedRegion : null,			// region clicked on to select
		selectedWidget : null,			// widget clicked on to select
		dropAction : null,				// DROP_ACTION_ADD_WIDGET or DROP_ACTION_ADD_REGION
		DROP_ACTION_ADD_REGION : 0,		// one of the dropAction actions
		DROP_ACTION_SORT_REGION : 1,	// one of the dropAction actions
		DROP_ACTION_ADD_WIDGET : 2,		// one of the dropAction actions
		borderWidth : 1,
		padding : 10,
		w1start : null,
		w1now : null,
		w2start : null,
		w2now : null,
		next : null,
		repositioning : false,
		originalRegionId : null,    // Used to store the original Region ID name when opening dialog.

		/**
		 *
		 */

		_init : function()
		{
			var self = this;
	
			// helper class that handles data model modification and communication with REST service, i.e., a controller
			this.helper = $.perc_template_layout_helper;
	
			// get singleton controller that handles events.
			// TODO: Currently only handles widget property events.
			// The helper currently does too much. Should refactor and move Helper event handlers to controller.
			this.controller = $.perc_layout_controller;
			this.controller.setHelper(this.helper);
			
			this.controller.getWidgetLibrary(function(widgetLibrary)
			{
				self.renderWidgetLibrary(widgetLibrary);
			});
						
			$('#perc-save').on("click",function()
			{
				self._save();
			});

			$('#perc-layout-tab').on("click",function()
			{
				self.initialize();
			});

			// TODO: remove this from the final release. This is only for testing purposes.
			$('#perc-clear-page').on("click",function()
			{
				self.helper.clearPage(function() { self.render(); });
			});

			// dialog for configuring region
			$('#perc-region-edit').perc_dialog(
			{
				modal : true,
				zIndex : 100000,
				width: "600px",
				autoOpen: false,
				open : function()
				{
				    self.originalRegionId = self.selectedRegion.attr('id');
					$('#perc-region-name').val(self.selectedRegion.attr('id'));
					$('#perc-region-width').val(self.selectedRegion.css('width'));
					$('#perc-region-height').val(self.selectedRegion.css('height'));
					$('#perc-region-padding').val(self.selectedRegion.css('padding'));
					$('#perc-region-margin').val(self.selectedRegion.css('margin'));
				},
				percButtons:
				{
					"Ok"	:
					{
						click : function()
						{
							self.selectedRegion.attr({id : $('#perc-region-name').val()});
							self.selectedRegion.css('width',$('#perc-region-width').val());
							self.selectedRegion.css('height',$('#perc-region-height').val());
							self.selectedRegion.css('padding',$('#perc-region-padding').val());
							self.selectedRegion.css('margin',$('#perc-region-margin').val());

							// Now serialize the data back into regionCSS
							
							if (self.helper.regionCSS[$('perc-region-name').val()] != self.originalRegionId)
							{
								// region name has changed, axe the original property.
								eval('delete self.helper.regionCSS.'+self.originalRegionId+';');   // HAAAAAAAAAAACK!
							}
							$('#perc-menu').css('display','none');
							$('#perc-region-edit').dialog("close");
						},
						id 		: 'perc-region-editor-dialog-save'
					},
					"Cancel" :
					{
						click: function()
						{
							$('#perc-menu').css('display','none');
							$(this).dialog("close");
						},
       						id 		: 'perc-region-editor-dialog-cancel'
					}
				}
			});
			
	
			// dialog for editing widget
			$('#perc-widget-edit').perc_dialog(
			{
				modal : true,
				zIndex: 100000,
				autoOpen: false,
				open : function()
				{
					// get the widget definition and render it			
					var html = self.controller.getCurrentWidgetDefinition().render();
					$('#perc-widget-property-form').html(html);
				},
				percButtons:
				{
					'Cancel' : 
					{
						click : function()	
						{
							$('#perc-menu').css('display','none');
							$('#perc-widget-edit').dialog('close');
						},
						id : 'perc-widget-editor-dialog-cancel'

					},
					'Ok' :
					{
						click : function()	
						{
							$('#perc-menu').css('display','none');
							self.controller.editWidgetOk(function()
							{
									self.render();
							});
							$('#perc-widget-edit').dialog('close');
						},
						id : 'perc-widget-editor-dialog-save'
					}
				}
			});
	
	
			
			// TODO: remove this. This is should only be called from Templates tab.
			// this.initialize();
		},
		
		renderWidgetLibrary : function(widgetLibrary)
		{
			$('#perc-widget-library').html(widgetLibrary.html());
		
			// add UI event handlers for adding, configuring widgets
			this._addWidgetSupport();
			
			$('#perc-widget-library-toggle').on("click",function()
			{
				$('#perc-widget-library').toggle();
			});
		},
		
		_addWidgetSupport : function()
		{
			var self = this;
			$(".perc-toolbar-item").draggable(
			{
				opacity:	0.7,
				helper:		function ()
				{
					return $(this).clone().appendTo('body').css('zIndex',5).show();
				},
				start: 		function (evt, ui)
				{
					self.dropAction = self.DROP_ACTION_ADD_WIDGET;
				}
			});
		},
		     
		_save : function()
		{
			var self = this;
			this.helper.save(this.type, function() { self.render(); });
		},
		setPageId : function(pageId)
		{
			this.pageId = pageId;
		},
	
		initialize : function()
		{
		
			if(this.pageId == null && gSelectTemp == null)
			{
				return;
			}
	
			var self = this;
			
			if(this.pageId)
			{
				this.type = this.helper.Type.PAGE;
				this.helper.load(this.pageId, this.helper.Type.PAGE, function(html)
				{
					self._load(html);
					self.renderCSS(false);
				});
			}
			else if(gSelectTemp)
			{
				this.type = this.helper.Type.TEMPLATE;
				this.helper.load(gSelectTemp, this.helper.Type.TEMPLATE, function(html)
				{
					self._load(html);
					self.renderCSS(false);
				});
			}
			else
			{
				alert(I18N.message("perc.ui.template.layout.widget@Unable To Determine Page Or Template ID"));
			}
		},
		_load : function(html)
		{
			this.render();
			
			this.renderCSS();

			var self = this;
			$('#showBorders').on("click",function()
			{
				self.showBorders = !self.showBorders;
				if(self.showBorders)
				{
					$('.perc-region').addClass('perc-region-puff');
				} else
				{
					$('.perc-region').removeClass('perc-region-puff');
				}
			});
			$("#region-tool").draggable(
			{
				opacity:	0.7,
				helper:		function () { return $(this).clone().appendTo('body').css('zIndex',5).show(); },
				start: 		function (evt, ui)
				{
					self.dropAction = self.DROP_ACTION_ADD_REGION;
				},
				stop:		function (evt, ui)
				{
					self.hoveringRegion = null;
				}
			});
		
			// bind close menu button
			$('#perc-close').on("click",function()
			{
				if(self.selectedRegion)
				{
					var regionId = self.selectedRegion.attr('id');
					self.helper.removeRegion(regionId);
					if(self.type == self.helper.Type.PAGE)
						self.helper.markTemplateRegionOverride(regionId);
				}
				else if(self.selectedWidget)
				{
					var regionId = self.selectedWidget.parent().parent().attr('id');
					var widgetId = self.selectedWidget.attr('id');
					self.helper.removeWidget(self.selectedWidget.attr('id'));
					if(self.type == self.helper.Type.PAGE)
						self.helper.markTemplateRegionOverride(regionId);
				}
				self.render();
			});
		
			// bind edit menu button
			$('#perc-edit').on("click",function()
			{
				if(self.selectedRegion)
				{
					$('#perc-region-edit').dialog('open');
				}
				else if(self.selectedWidget)
				{
					var widgetId = self.selectedWidget.attr('id');
					var widgetParentRegionId = self.selectedWidget.parent().parent().attr('id');
					var widgetParentRegion = self.helper.getRegion(widgetParentRegionId);
					var widget = widgetParentRegion.getWidget(widgetId);
					self.controller.editWidgetStart(widget, null);
				}
			});
	
			this._regionize();
	
		},
	
		_autoSizeHorizontal : function()
		{
			var self = this;
			$('.perc-horizontal').each(function()
			{
		        horizParent = $(this);
		        var depth = horizParent.parents(".perc-region").length;
		        var childNumber = horizParent.children().length - 1;
		        var parentWidth = horizParent.width();
		        var parentInnerWidth = parentWidth - (2*self.padding + 2*self.borderWidth)*depth;
			//	if(depth > 1)
		        	parentInnerWidth = parentWidth;
		        w = Math.floor((parentInnerWidth - (childNumber * 2 * (self.padding + self.borderWidth)))/childNumber);
		        if(isNaN(w) || w < 0)
		        	return;
		        horizParent.children().each(function() {$(this).width(w);});
	
				var maxHeight = -1;
				horizParent.children().each(function()
				{
					if($(this).height() > maxHeight)
						maxHeight = $(this).height();
				});
				horizParent.children().each(function()
				{
					if($(this).attr('class') != 'clear-float')
						$(this).css("min-height", maxHeight);
			//			$(this).height(maxHeight);
				});
			});
		},
	
		//===============================
		//
		//	Add decorations and behaviors
		//
		//===============================
	
		_regionize : function()
		{
			var self = this;
			
			//
			// decorate regions
			//
			$('.perc-region-leaf').css('background-color','#DDFFDD');
			$('.perc-region').addClass('perc-region-puff');
			$('.perc-widget').addClass('perc-widget-puff');
			$('.clear-float').remove();
			$('.perc-horizontal').append('<div class="clear-float"></div>');
			$('#perc-menu').css('display','none');


			// sortable widgets
			// widgets can be dragged from any region to any other region
			var moveWidgetFromRegion = null;
			var moveWidgetToRegion = null;
			var moveWidget = null;
			$('.perc-horizontal').addClass('perc-sortable');
			$('.perc-vertical').addClass('perc-sortable');
			$(".perc-region .perc-sortable").sortable(
			{
				connectWith: '.perc-region .perc-sortable',
				dropOnEmpty: true,
				start : function(event, ui)
				{
					$('#perc-menu').css('display','none');
					moveWidget = ui.item.attr('id');
					moveWidgetFromRegion = ui.item.parent().parent().attr('id');
				},
				stop: function(event, ui)
				{
					pos = 0;
					ui.item.parent().children().each(function()
					{
						if($(this).attr('id') != ui.item.attr('id'))
							pos++;
						else
							return false;
					});
					moveWidgetToRegion = ui.item.parent().parent().attr('id');
					self.helper.orderWidget(moveWidget, moveWidgetFromRegion, moveWidgetToRegion, pos);
					self._autoSizeHorizontal();
				}
			});

			// TODO: Merge Select Widget and Select Regions into One
			//
			// select widget
			//
			$('.perc-widget').on("click",function(event)
			{
				event.stopPropagation();					// dont propagate event to parents
				$('.perc-region').each(function()			// unhighlight other regions
				{
					$(this).removeClass('perc-selected');
				});
				$('.perc-widget').each(function()			// unhighlight other widgets
				{
					$(this).removeClass('perc-selected');
				});
				$(this).addClass('perc-selected');			// highlight this widget
				self.selectedRegion = null;						// unselect region
				self.selectedWidget = $(this);					// select this widget
				$('#perc-menu').css('position','absolute').css('left',$(this).width()+$(this).offset().left - 45).css('top',$(this).offset().top).css('zIndex','10').css('display', 'block');
			});
		
			//
			//	select regions
			//
			$('.perc-region').on("click",function(event)
			{
				event.stopPropagation();
				$('.perc-region').each(function()
				{
					$(this).removeClass('perc-selected');
				});
				$('.perc-widget').each(function()
				{
					$(this).removeClass('perc-selected');
				});
				$(this).addClass('perc-selected');
				self.selectedRegion = $(this);
				$('#perc-menu').css('position','absolute').css('left',$(this).width()+$(this).offset().left - 25).css('top',$(this).offset().top).css('zIndex','10').css('display', 'block');
			});
		
			//
			//	resizable horizontal regions
			//
			$('.perc-horizontal > .perc-region').resizable(
			{
				handles : 'e',
				start : function(event, ui)
				{
					$('#perc-menu').css('display','none');
					self.next = $(this).next();
					self.w1start = $(this).width();
					self.w2start = self.next.width();
				},
				resize : function(event, ui)
				{
					self.w1now = $(this).width();
					self.w2now = self.w2start - (self.w1now - self.w1start);
					self.next.width(self.w2now);
					$(this).css('position','relative').css('top','0px').css('left','0px');
					self.next.css('position','relative').css('top','0px').css('left','0px');
					$(this).height('');
				},
				stop : function(event, ui)
				{
					if(self.type == self.helper.Type.PAGE && !self.helper.isRegionResizable($(this).attr('id')))
					{
						$(this).width(self.w1start);
						self.next.width(self.w2start);
					}
					$(this).height('');
				}
			});
		
			//
			// resizable vertical regions
			//
			$('.perc-vertical > .perc-region').resizable(
			{
				handles : 's',
				start : function(event, ui)
				{
					$('#perc-menu').css('display','none');
					self.next = $(this).next();
					self.w1start = $(this).height();
					self.w2start = self.next.height();
				},
				resize : function(event, ui)
				{
					self.w1now = $(this).height();
					self.w2now = self.w2start - (self.w1now - self.w1start);
					self.next.height(self.w2now);
					$(this).css('position','relative').css('top','0px').css('left','0px');
					self.next.css('position','relative').css('top','0px').css('left','0px');
					self.next.height($(this).height());
				},
				stop : function(event, ui)
				{
	
				}
			});
		
			// drag regions up/down to reposition them within parent vertical region 
			$('.perc-vertical > .perc-region').draggable(
			{
				distance : 20,
				containment : 'parent',
				axis : 'y',
				start : function() {
					self.repositioning = true;
					$('#perc-menu').css('display','none');
				},
				helper: function () {var height =  $(this).height() + 2*self.padding + 2*self.borderWidth; var width = $(this).width() + 2*self.padding; return "<div style='width:"+width+"px;height:"+height+"px;background-color:grey;zIndex:5;opacity:0.4'></div>";},
				stop : function()
				{
					self.repositioning = false;
					$('.perc-region').removeClass('east').removeClass('').removeClass('north').removeClass('south').removeClass('hovering');
					if(self.dropPosition == self.POSITION_NORTH)
						self.helper.moveRegion($(this).attr('id'), self.hoveringRegion.attr('id'), true);
					else
						self.helper.moveRegion($(this).attr('id'), self.hoveringRegion.attr('id'), false);
					
					
					self.render();
				}
			});
		
			// drag regions left/right to reposition them within parent horizontal region 
			$('.perc-horizontal > .perc-region').draggable(
			{
				distance : 20,
				containment : 'parent',
				axis : 'x',
				start : function() {
					$('#perc-menu').css('display','none');
					self.repositioning = true;
				},
				helper: function () { var height =  $(this).height() + 2*self.padding + 2*self.borderWidth; var width = $(this).width() + 2*self.padding + 2*self.borderWidth; return "<div style='top:"+$(this).position().top+"px;width:"+width+"px;height:"+height+"px;background-color:grey;zIndex:5;opacity:0.4'></div>";},
				stop : function() {
					self.repositioning = false;
					$('.perc-region').removeClass('east').removeClass('').removeClass('north').removeClass('south').removeClass('hovering');
					if(self.dropPosition == self.POSITION_)
						self.helper.moveRegion($(this).attr('id'), self.hoveringRegion.attr('id'), true);
					else
						self.helper.moveRegion($(this).attr('id'), self.hoveringRegion.attr('id'), false);
						
						
					self.render();
						
				}
			});
		
           //Add divs to the top, left, right, and bottom of each region, to give feedback while using the region drag tool.
			$(".perc-region").each ( function()
			{
				var self = $(this);
				//remove old ones.
				self.children('perc-region-feedback').remove();
				var region_id = self.attr('id');
				var dirs = $.map( ["north","south","east","west"], function(dir){ return make_feedback_droppable( region_id, dir ); } );
                                var topwidth = (self.innerWidth() - 20) + 'px';
                                dirs[0].css( {top: '0', left: '10px', width: topwidth, height: '10px'} );
                                dirs[1].css( {bottom: '0', left: '10px', width: topwidth, height: '10px'} );
				dirs[2].css( {top: '0', right: '0', height: '100%', width: '10px'} );
				dirs[3].css( {top: '0', left: '0', height: '100%', width: '10px'} );
				$.each( dirs, function(){ self.append( this ); } );
			});
			function make_feedback_droppable( region_id, direction )
			{
				return $("<div/>").addClass("perc-region-feedback").css({'position':'absolute'}).droppable(
				{ 
					accept: '#region-tool',
					tolerance: 'pointer', 
					hoverClass: 'perc-show-feedback',
					greedy: true,
					drop: function()
					{
						if(self.type == self.helper.Type.PAGE && $.inArray(region_id, self.helper.page.lockedTemplateRegions)!=-1)
						{
							alert(I18N.message("perc.ui.template.layout.widget@Region Locked"));
							return;
						}
						
						self.helper.addRegion( region_id, direction );
						if(self.type == self.helper.Type.PAGE)
							self.helper.markTemplateRegionOverride(region_id);
						self.render();
                    }
				});
			}
		
			// you can drag region tool and widget onto regions
			$(".perc-region").droppable(
			{
				accept : '.perc-region, #region-tool, .perc-toolbar-item',
				greedy : true,
				tolerance : 'pointer',
				over : function(event, ui)
				{
					$(this).addClass('hovering');
					self.hoveringRegion = $(this);
				},
				out : function(event, ui) { $('.perc-region').removeClass('hovering'); },
				drop : function(event, ui)
				{
					var regionId = self.hoveringRegion.attr('id');
					
					if(self.type == self.helper.Type.PAGE && $.inArray(regionId, self.helper.page.lockedTemplateRegions)!=-1)
					{
						alert("This region is locked.");
						return;
					}
					
					if(self.dropAction == self.DROP_ACTION_ADD_WIDGET)
					{
						widgetDefinitionId = ui.draggable.attr('id');
	
						self.dropAction = null;
	
					//	newWidget = self.helper.createNewWidget("PSWidget_TestProperties", self.helper.getRegion(regionId));
						newWidget = self.helper.createNewWidget(widgetDefinitionId, self.helper.getRegion(regionId));
	
						widget = self.helper.addWidget(newWidget, regionId, true);
						if(self.type == self.helper.Type.PAGE)
							self.helper.markTemplateWidgetOverride(regionId);
						self.render();
					}
				}
			});
			this._autoSizeHorizontal();
		},
		renderCSS: function()
		{
		    
		    // TSCHAK's special sauce.
		    /////////////////////////////////////////////////////////////////////
		    // Now update the CSS bits.
		    
		    for (selector in this.helper.regionCSS)
			{
			    for (property in this.helper.regionCSS[selector])
				{
				    
				    // Okay, this part is a bit tricky. We have to take into account padding with borders toggled.
				    // If borders are toggled, then the appropriate regions need to be resized to accomodate the 
				    // additional space, otherwise we get a "staggered blocks" problem.
				    
				    // If a spatial attribute is specified, then we need to compensate when needed, otherwise simply
				    // set the CSS attribute and move on.
				    
				    //console.log("Setting #"+selector+" property "+property+":"+this.helper.regionCSS[selector][property]);
				    if (property === "width" || property === "height")
					{
					    $('#'+selector).css(property, this.helper.regionCSS[selector][property]);
					} else 
					{
					    $('#'+selector).css(property, this.helper.regionCSS[selector][property]);
					}
				    $('#'+selector).css(property, this.helper.regionCSS[selector][property]);
				}
			}
		    /////////////////////////////////////////////////////////////////////
		},
		render : function()
		{
			let html = this.helper.rootRegion.generateHtml(false);
		     
			this.element.html(html);
			this._regionize();
			
			this.renderCSS();
		},
		destroy: function() {
			 $.widget.prototype.apply(this, arguments); 
		}
	});
	$.extend($.ui.template_layout,
	{
		getter: "value length initialize setPageId",
		defaults:
		{
			option1: "defaultValue"
		}
	});
})(jQuery);


