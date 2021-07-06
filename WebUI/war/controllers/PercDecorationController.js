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

/**
 * decorator.js
 * 
 * Author: Jason Priestly
 * 12/31/2009
 * 
 */
(function($,P) {
    /**
     * Decorates regions and widgets across various views: content, layout, and style
     * 
     * @param allElements (function(){}) retrieves a list of widgets or region DIVs.
     * widgets for all views but also regions for layout view
     * 
     * @param decorationClasses (String) space separated list of CSS classes to be
     * applied to allElements in that particular view, e.g., perc-widget-puff, perc-region-puff
     *
     * @param selectionClasses (String) space separated list of CSS classes to be
     * applied to allElements when a widget or region is active, e.g., perc-widget-active, perc-region-active
     * 
     * @param menuItems (array of Objects) array of menus that decorate a the top right of a widget or region DIV
     * when you select it. Format is as follows:
     * 
     * [    {   widgetType : 'widget Type',
     *          name       : 'uniqueName',
     *          img        : '/path/to/imageWithNoExtension' | function(){},
     *          callback   : function(widgetDiv|regionDiv) {},
     *          tooltip    : 'Text appears on hover'
     *      },
     *      {...anotherMenu...},
     *      {...anotherMenu...}
     * ]
     * 
     * Attribute description:
     * widgettype : specify the type of the widget that will have the menuItem.  undefined widgettype means all widget.
     * name       : a string identifying name
     * img        : a string denoting a relative path to a PNG image
     *              or a function that returns a string denoting a relative path to a PNG image.
     *              Leave out the PNG extension
     * callback : function(htmlElement) a function that will be invoked when menu is clicked.
     *              The HTML element will be passed as an argument to the callback
     *              The HTML element will be either the widget DIV or the region DIV being decorated
     * tooltip  : a string displayed as a tooltip when you hover over the menu
     */
    P.decorationController = function(allElements, decorationClasses, selectionClasses, menuItems) {
        var _visible = false, _selected = null;
	var _dblclickItem;
	$.each( menuItems, function() {
		var name = this.name;
		if (name === 'edit' || name === 'configure' || this.tooltip === 'Configure region')
		{
        
			_dblclickItem = this;

		}
	} );

        // let this decorator know about other decorator so that they can unselect each other
        var otherDecorator = null;
        visible( true );

        return {
            visible : visible,
            refresh : refresh,
            unselectAll : unselectAll,
            setOtherDecorator : setOtherDecorator,
            removeDecorations : removeDecorations,
            addClicks: addClicks
        };

        function setOtherDecorator(decorator) {
            otherDecorator = decorator;
        }

        function refresh() {
            
            _selected = null;
            
            if( _visible ) {
                
                addDecorations();
                
            } else {
                
                removeDecorations();
                
            }
        }

        function visible( isVisible )
        {
            if( typeof isVisible === 'boolean') {
                if( isVisible !== _visible ) {
                    _visible = isVisible;
                }
            }
            return _visible;
        }

        function addClicks() {
            
            allElements().off('click.decorate').on( 'click.decorate', function(evt) {
                
                // unselect the other decorator if this is selected
                if(otherDecorator != null)
                    otherDecorator.unselectAll();
                
                evt.stopPropagation();
                
                if( _selected )
                {
                    if( !_visible )
                    {
                        _selected.removeClass( decorationClasses );
                    }
                    _selected.removeClass( selectionClasses );
                    removeMenu( _selected );
                }
                _selected = $(this);
                if( !_visible )
                {
                    _selected.addClass( decorationClasses );
                }
                addMenu( _selected );
                _selected.addClass( selectionClasses );
            });

            $.each( allElements(), function() {
                var checkLock = $(this).hasClass('perc-locked');
                if(!checkLock)
                {    
                    $(this).off('dblclick.decorate').on( 'dblclick.decorate', function(evt) {
                       evt.stopPropagation();
                       if ( _dblclickItem )
                       {
                           _dblclickItem.callback($(this));
                       }
                    } );
                }    
            } );
        }

        /**
         * Function to de-select all regions and widgets
         * Used in PercLayoutView when clicking on the body element to unselect all elements
         */
        function unselectAll() {
            if(_selected) {
                _selected.removeClass( selectionClasses );
                removeMenu( _selected );
            }
        }

        function removeClicks()
        {
            allElements().off( '.decorate' );
        }

        function addDecorations()
        {
            addClicks();
            if( _visible )
            {
                allElements().addClass( decorationClasses );
            }
        }

        function removeDecorations()
        {
            removeClicks();
            if( !_visible )
            {
                allElements().removeClass( decorationClasses );
            }
        }

        function addMenu( elem )
        {
            //If the supplied elem has a perc-locked class, ignores all the menuItems and shows the lock.
            var menu = createMenu( elem );
            if(elem.hasClass("perc-locked"))
            {
               var item = {name:'locked', img: '/cm/images/icons/editor/locked', 
                           callback: function(widget) {},
                           tooltip: 'Locked'};
               addMenuItem( elem, menu, item );
            }
            else
            {
                $.each( menuItems, function()
                {
                    //Custom menuItem for widgetType. undefined this.widgetType means all widgets.
                    if(typeof(this.widgetType) === 'undefined' || this.widgetType === elem.attr("widgetdefid"))
                        addMenuItem( elem, menu, this );
                });
            }
        }

        function createMenu( elem )
        {
            var menu = $("<div class='perc-ui-menu'/>");
            elem.append( menu );
            return menu;
        }

        function addMenuItem( elem, menu, item ) {

             //If the img is function then the gets the img src by calling it by passing the elem, otherwise treats it 
             //as the img src.
             var iconSrc = typeof item.img === "function" ?item.img(elem):item.img;

             // if the icon is null then do not add the menu to the item
             if(iconSrc === null)
                return;
             
             var isInactive = iconSrc.indexOf("Inactive", iconSrc.length - "Inactive".length) !== -1;     

             var tooltip = typeof item.tooltip === "function" ?item.tooltip(elem):item.tooltip;
             var normalImg = iconSrc + ".png"; 
             var overImg = iconSrc + "Over.png"; 
             var icon = $("<img src='"+ normalImg +"'/>")
                .addClass('perc-ui-menu-icon')
                .attr("alt", tooltip)
                .attr("title", tooltip); 
             if(isInactive)
                icon.css("cursor", "default");     
             icon.on("click", function()
             {
                 item.callback( elem );
             });
             icon.on("mouseenter", function(){
                $(this).attr("src", overImg);
             }).
                on("mouseleave", function(){
                   $(this).attr("src", normalImg);
                });
             menu.append( icon );
        }

        function removeMenu( elem )
        {
            elem.children('.perc-ui-menu').remove();
        }
    };
})(jQuery, jQuery.Percussion);
