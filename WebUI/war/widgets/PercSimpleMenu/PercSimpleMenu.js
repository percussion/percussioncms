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

/**
 *  PercSimpleMenu.js
 *  @author Jose Annunziato
 *
 *  Implements a simple dropdown menu
 */
(function($) {

    /**
     *  PercSimpleMenu()
     *  plugin implementation
     *
     *  @param config
     *  {   title : "Menu Title",         Clickable title at top to show/hide menu below. Omit if using an background image (optional)
     *       menuItemsAlign : "left",     Render menu to the left of title. Default position is below and right of title (optional)
     *       stayInsideOf : "#selector",  jQuery selector of container to stay inside. If menu doesnt fit, it will render to the left and/or above. Overules menuItemsAlign (optional)
     *       items : [                    Array of menu items (required)
     *           {   label : "Open",      Label of menu item. At least one. Can be HTML (required)
     *               callback : open,     Callback function to call whem menu item is clicked (optional)
     *               data : "page-id-1"   Data to pass when callback. Can be object/anything (optional)
     *           },
     *           {   label : "Preview",   Label of menu item. At least one. Can be HTML (required)
     *               callback : preview,  Callback function to call whem menu item is clicked (optional)
     *               data : "page-path-1" Data to pass when callback. Can be object/anything (optional)
     *           }, etc...
     *  ]};
     *
     *  Generates the following DOM
     *
     *   <pre>
     *   <div class="perc-simplemenu">
     *       <div class="perc-simplemenu-title perc-simplemenu-title-deselected">Title</div>
     *       <div class="perc-simplemenu-menuitems" style="display: none;">
     *           <div class="perc-simplemenu-menuitem perc-index-0">Open</div>
     *           <div class="perc-simplemenu-menuitem perc-index-1">Preview</div>
     *       </div>
     *   </div>
     *   </pre>
     *
     *  +-----+
     *  |Title|         click: show menu items, click again: hide menu items
     *  +-----+-----+
     *  |Open       |   click: open() and hide menu items
     *  +-----------+
     *  |Preview    |   click: preview() and hide menu items
     *  +-----------+
     */
    $.fn.PercSimpleMenu = function(config) {
        var title = config.title;
        var items = config.items;
        
        var menu = $("<div class='perc-simplemenu'>");
        var menuTitle = $("<div class='perc-simplemenu-title perc-simplemenu-title-deselected'>")
            .append(title);
        menu.append(menuTitle);
        var menuItems = $("<div class='perc-simplemenu-menuitems'>");
        menu.append(menuItems);
        
        menu.data("config", config);
        
        $.each(items, function(index, menuItem){
            var menuItemClass = "perc-menu-item";
            if(index == items.length - 1) {
                menuItemClass = "perc-menu-last-item";
            }
            var label = menuItem.label;
            var callback = menuItem.callback;
            var data = menuItem.data;
            
            var menuItemDom = $("<div class='perc-simplemenu-menuitem perc-index-"+index+"'>")
                .append(label)
                .addClass(menuItemClass)
                .data("callback",callback)
                .on("click",function(event){
                    menuItemClicked(event);
                    var callback = $(this).data("callback");
                    var data = menu.data("data");
                    event.data = data;
                    callback(event);
                })
                .on("mouseenter",function () {
                    $(this).css('color', '#000');})
                .on("mouseleave",function(){ $(this).css('color', '#fff');});
                
            if(data)
                menuItemDom.data("data",data);
                
            menuItems.append(menuItemDom);
        });
        
        $(this).append(menu);
        
        menuTitle.on("click",function(e){
            menuTitleClicked(e);
        });
        menu.on("mouseenter",function(e){
            menuHoverIn(e);
        }).on("mouseleave",function(e){
            menuHoverOut(e);
        });
        
        return $(menu);
    };

    /**
     *  menuTitleClicked()
     *
     *  Handles clicking of title
     *
     *  Toggles between classes:
     *  perc-simplemenu-title-deselected and hides the menu items
     *  perc-simplemenu-title-selected   and shows the menu items
     */
    function menuTitleClicked(event) {
        var menuTitle = $(event.currentTarget);
        if(menuTitle.hasClass("perc-simplemenu-title-deselected")) {
            menuTitle
                .addClass("perc-simplemenu-title-selected")
                .removeClass("perc-simplemenu-title-deselected");
            showMenuItems(event);
        } else {
            menuTitle
                .removeClass("perc-simplemenu-title-selected")
                .addClass("perc-simplemenu-title-deselected");
            hideMenuItems(event);
        }
    }
    
    var _hidemenu = true;
    var _lastCurrentTarget = null;
    function menuHoverIn(event) {
        if(_lastCurrentTarget == event.currentTarget)
            _hidemenu = false;
    }
    
    function menuHoverOut(event) {
        _hidemenu = true;
        _lastCurrentTarget = event.currentTarget;
        setTimeout(function()
                {
                    if(_hidemenu)
                        hideMenuItems(event);
                }
        ,500);
    }

    /**
     *  showMenuItems()
     *
     *  Displays the menu items when the title is clicked.
     *  By default positions the menu items below and to the right of the title.
     *  Unless configured to show to the left of the title.
     *  Unless configured to stay above and to the left of the right and bottom edgets of an ancestor containing element.
     *
     */
    function showMenuItems(event) {
        var menuTitle = $(event.currentTarget);
        var menuItems = menuTitle.next();
        var menuItemsWidth = menuItems.width();
        
        var menu = menuTitle.parents(".perc-simplemenu");
        var config = menu.data("config");
        
        var menuItemsTop  = menuTitle.offset().top + menuTitle.height();
        var menuItemsLeft = menuTitle.offset().left;
        if(config.menuItemsAlign == "left")
            menuItemsLeft = menuItemsLeft-menuItemsWidth+menuTitle.width();
        
        if(config.stayInsideOf) {
            // calculate bottom position of container and menu items
            var container = menu.parents(config.stayInsideOf);
            
            var containerTop = container.offset().top;
            var containerHeight = container.height();
            var containerBottom = containerTop + containerHeight;
            
            var menuItemsHeight = menuItems.height();
            var menuItemsBottom = menuItemsTop + menuItemsHeight;
            // if menu items dont fit, display them above the title
            var menuItemsTopNew; 
            if(menuItemsBottom > containerBottom)
                menuItemsTopNew = menuItemsTop - menuItemsHeight - menuTitle.height();

            // but dont flip the menu if it's going to show in negative vertical position
            if(menuItemsTopNew > containerTop)
                menuItemsTop = menuItemsTopNew;

            // if already configure to render to the left, dont bother checking right edge of container
            // other wise if right edge of menu spills over the right edge of the container, render it to the left of the title
            if(config.menuItemsAlign != "left") {
                var containerLeft = container.offset().left;
                var containerWidth = container.width();
                var containerRight = containerLeft + containerWidth;
                
                var menuItemsRight = menuItemsLeft + menuItemsWidth;
                
                if(menuItemsRight > containerRight)
                    menuItemsLeft = menuItemsLeft - menuItemsWidth + menuTitle.width();
            }
        }
        
        menuItems
            .css("position","absolute")
//            .css("top", "15px")
  //          .css("left", "-15px");
            .css("top", menuItemsTop+"px")
            .css("left", menuItemsLeft+"px");
        
        menuItems.show();
    }
    
    function hideMenuItems(event) {
        var currentTarget = $(event.currentTarget);
        var menu = currentTarget;
        if(!currentTarget.hasClass("perc-simplemenu"))
            menu = currentTarget.parents(".perc-simplemenu");
        var menuTitle = menu.find(".perc-simplemenu-title");
        menuTitle
            .removeClass("perc-simplemenu-title-selected")
            .addClass("perc-simplemenu-title-deselected");
        var menuItems = menu.find(".perc-simplemenu-menuitems");
        menuItems.hide();
    }
    
    function menuItemClicked(event) {
        hideMenuItems(event);
    }

})(jQuery);
