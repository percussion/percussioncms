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
 *  PercSimpleMenu.js
 *  @author Jose Annunziato
 *
 *  +-------------------+
 *  |menuTitleExpanded  |
 *  +-------------------+
 *  |menuLabels[0]      | ---> callbacks[0](callbackData[0])
 *  |menuLabels[1]      | ---> callbacks[1](callbackData[1])
 *  |menuLabels[2]      | ---> callbacks[2](callbackData[2])
 *  +-------------------+
 *
 *  (*) Configuration (refer to diagram above)
 *  @param menuTitle (string) can be HTML
 *  @param menuLabels (string[]) can be HTML
 *  @param callbacks (function[]) functions to call when selecting a menu item
 *  @param callbackData (object[]) data to pass back when calling callbacks for each menu item
 *  @param menuTitleExpanded (string) can be HTML shown when menu is expanded
 *  @param menuTitleCollapsed (string) can be HTML shown when menu is collapsed
 *
 *  (*) Usage:
 *  // declare callback functions
 *  function edit() {...}
 *  function preview() {...}
 *  // get dom of where you want to put the menu inside
 *  var menuContainer = $("<div>");
 *  // configure the menu
 *  var menuConfig = {
 *      menuTitleExpanded  : "<img src='expandedIcon.png'>",
 *      menuTitleCollapsed : "<img src='collapsedIcon.png'>",
 *      menuLabels : ["Edit", "Preview"],
 *      callbacks : [edit, preview],
 *      callbackData : [{someProperty : "someValue"},"some id 123"]
 *  }
 *  // apply the plugin to the container passing configuration
 *  menuContainer.percSimpleMenu(menuConfig);
 *
 *  var body = $("body");
 *  body.append(menuContainer);
 *
 */
(function($) {

    var defaultConfig = {
        menuTitle : "Menu!!!",
        menuTitleExpanded : "[-]",
        menuTitleCollapsed : "[+]",
        optionClasses : [],
		menuWidth : 65 // IE has issues with width, supply an explicit value here to force it.
    };
 
    /**
     *  percSimpleMenu()
     *  plugin implementation
     */
    $.fn.percSimpleMenu = function(config) {
        config = $.extend({}, defaultConfig, config);

        var callbacks = config.callbacks;
        var callbackData = config.callbackData;
        var menuTitle = config.menuTitle;
        var menuTitleExpanded  = config.menuTitleExpanded;
        var menuTitleCollapsed = config.menuTitleCollapsed;
        var menuLabels = config.menuLabels;
		var optionClasses = config.optionClasses?config.optionClasses:[];
        
        // create menu dynamically
        // create menu container, add title
        var $menu      = $("<div class='perc-simplemenu-menu'>");
        var $menuTitle = $("<div class='perc-simplemenu-title'>");
        $menuTitle.append(menuTitleCollapsed);

        // add menu items
        var $menuItems = $("<div class='perc-simplemenu-menuitems'>");
        for(ml=0; ml<menuLabels.length; ml++) {
            var $menuItem = $("<div class='perc-simplemenu-menuitem'>");
			var $menuLabel = $(menuLabels[ml]);
            
			if ($menuLabel.is("a")) {
				$menuItem.css("padding", "0");
				
				if (callbackData[ml].formSummary.totalSubmissions > 0) {
					$menuLabel.attr("href", escape(percJQuery.perc_paths.ASSET_FORMS_EXPORT + "/" +
						callbackData[ml].formSummary.name + ".csv"));
					$menuLabel.attr("target", "_blank");
				}
				
				$menuLabel.attr("style", "text-decoration: none; padding: 0; " +
					"font-family: Verdana; font-size: 11px; " +
					"color: white; display: block; width: 100%; padding: 5px");
			}
			else {
				$menuLabel = menuLabels[ml];
			}
			// add the new class, use with identifier unique
            $menuItem.addClass(optionClasses[ml]);
            // configure each menu item
            $menuItem
                .data("callbackData", callbackData[ml])
                .data("callback", callbacks[ml])
                .append($menuLabel)
                // on click event
                .on("click",function(evt) {
                    let menuItem = $(this);
                    var data = menuItem.data("callbackData");
                    var callback = menuItem.data("callback");
                    var menuItems = menuItem.parent();
                    menuItems.hide();
                    callback(data);
                })
                .on("mouseenter",function() {
                    let menuItem = $(this);
                    menuItem.addClass("perc-simplemenu-menuitem-hover");
                }).on("mouseleave",function() {
                    let menuItem = $(this);
                    menuItem.removeClass("perc-simplemenu-menuitem-hover");
                });
            $menuItems.append($menuItem);
        }
        $menu.append($menuTitle)
             .append($menuItems);
        this.append($menu);
        
        // event handling
        $menu.data("config", config);
        $menuItems.hide();
        
        $menuTitle.find("*").on("click",menuTitleClick);
        
        $menuTitle
            .on("click",menuTitleClick)
            .on("mouseenter",(
                function() {
                    var menuItem = $(this);
                    menuItem.addClass("perc-simplemenu-title-hover");
                }).on("mouseleave", function() {
                    var menuItem = $(this);
                    menuItem.removeClass("perc-simplemenu-title-hover");
                });

        // hide all menus if you exit the containing document
        // useful if used inside an iframe like a gadget
        $(document).on("mouseenter",null).on("mouseleave",
            function() {
                hideAllMenus();
            }
        );
        
        // hide all menus when clicking on body
        $("body").on("click",function(event){
            var target = $(event.target);
            var noParents = target.parents().length == 0;
            var menuParent = target.parent().hasClass("perc-simplemenu-menu");
            if(!noParents && !menuParent)
                hideAllMenus();
        });
    };
    
    function hideAllMenus() {
        var allMenus = $(".perc-simplemenu-menu");
        
        $.each(allMenus, function() {
            var menu = $(this);
            collapseMenu(menu);
        });
    }
    
    // show the menu when you click on the title
    function menuTitleClick(event) {
        var menu = $($(event.target).parents(".perc-simplemenu-menu")[0]);
        var config = menu.data("config");
        if(config === undefined)
            return;
        var menuItems = menu.find(".perc-simplemenu-menuitems");
        var menuTitleExpanded  = config.menuTitleExpanded;
        var menuTitleCollapsed = config.menuTitleCollapsed;
        var menuTitle = menu.find(".perc-simplemenu-title");
        
        var visible = menuItems.css("display") === "block";
        
        hideAllMenus();
        
        if(visible) {
            collapseMenu(menu);
        } else {
            expandMenu(menu);
        }
    }
    
    function expandMenu(menu) {
        var config = menu.data("config");
        var menuItems = menu.find(".perc-simplemenu-menuitems");
        var menuTitleExpanded  = config.menuTitleExpanded;
        var menuTitleCollapsed = config.menuTitleCollapsed;
        var menuTitle = menu.find(".perc-simplemenu-title");
        
        menuTitle.html(menuTitleExpanded);
        menuItems.css("position","absolute");
		// Get pagination control as a reference to calculate menu's position (upwards or downwards)
		var oPaginate = $('#' + menu.parents('table').attr('id') + '_paginate');
		if (oPaginate){
			// Recalculate the position of Action menu when this appears behind the paging controls 
			// fix: 5 pixel of diference between IE and FF
			if (menu.position()['top'] + menu.outerHeight() + menuItems.outerHeight() + 5 > oPaginate.position()['top'] ){
				menuItems.css("margin-top",(menu.outerHeight() + menuItems.outerHeight()) * -1);
			}else
			{
				menuItems.css("margin-top", 0);
			}
		}
        menuItems.show();
        
        // TODO: make this a configuration. for some reason in IE it loses the width of the menu and it shrinks.
        if($.browser.msie)
            menuItems.width(config.menuWidth);
    }
    
    function collapseMenu(menu) {
        var config = menu.data("config");
        var menuItems = menu.find(".perc-simplemenu-menuitems");
        var menuTitleExpanded  = config.menuTitleExpanded;
        var menuTitleCollapsed = config.menuTitleCollapsed;
        var menuTitle = menu.find(".perc-simplemenu-title");
        
        menuTitle.html(menuTitleCollapsed);
        menuItems.hide();
    }
})(jQuery);
