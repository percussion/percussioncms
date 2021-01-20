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
 * PercDataList.js
 * @author Luis Mendez
 *
 * Generate the HTML and handle the events for a list of items.
 * The main purpose of this plugin is handle with a common code the user list, role list and group list in the workflow section. 
 */
(function($) {

    // interface
    $.PercDataList = {
        init           : init,
        selectItem     : selectItem,
        updateList     : updateList,
        getCurrentItem : getCurrentItem,
        disableButtons : disableButtons,
        enableButtons  : enableButtons,
        enableDeleteButton : enableDeleteButton,
        unhighlightAllItems: unhighlightAllItems,
        disableDeleteButton : disableDeleteButton,
        getCurrentList : getCurrentList,
        hideButtons : hideButtons
    };
    var defaultConfig =
        {
            listItem: [],
            title: "",
            addTitle: "Add",
            deleteTitle: "Delete",
            enableDelete: true,
            enableAdd: true,
            collapsible:false,
            createItem: function(){},
            deleteItem: function(){},
            selectedItem: function(){},
            truncateEntries:false,
            truncateEntriesCount:5
         }   
    var currentItemList = [];
    var currentItemIds = new Object();
    var currentItemSelected = "";
        
    function init(container, config) {
        options = $.extend(true, {}, defaultConfig, config);
        container.data('options', options);
        var head = $('<div class="perc-item-list-label" />')
                   .html(options.title);
        container.append(head)                   
        //At the top we could show a add or delete button
        if (options.enableDelete)
            container.find('.perc-item-list-label').append($('<div class="perc-item-delete-button" />').attr('title', options.deleteTitle));
        if (options.enableAdd)
            container.find('.perc-item-list-label').append($('<div class="perc-item-add-button"/>').attr('title', options.addTitle));
        if(options.collapsible) {
            container.find('.perc-item-list-label').append($('<span style="float: left;" id="perc-wf-min-max" class = "perc-items-minimizer" />'));
        }    
        
        var list = $('<div class="perc-itemname-list" />').append($("<ul/>"));
        container.append(list);
        updateList(container, [])
        
        //bind create event
        container.find(".perc-item-add-button").unbind().click(function(){
            var options = container.data('options');
            if (typeof(options.createItem) == 'function') {                
                options.createItem();
            }    
        });        
        //bind collapsible event

        container.find("#perc-wf-min-max").unbind().click(function() {
            $(this).toggleClass('perc-items-minimizer').toggleClass('perc-items-maximizer');
           container.find('.perc-itemname-list').slideToggle("fast");
        
        });
        
        //bind the delete event for each element         
        container.find(".perc-item-delete-button").unbind().click(function(event) {
            var options = container.data('options');
            event.stopPropagation(); // stop event because it is nested inside an element that is already bound
            if (typeof(options.deleteItem) == 'function')
                    options.deleteItem(currentItemSelected);
        });
    }
    
    //Generate and update the HTML list.
    function updateList(container, itemList){
        currentItemList = itemList;

        var options = container.data('options');
        var ulRoot = container.find(".perc-itemname-list>ul");
        ulRoot.html(""); // clear the item list
        $.each(itemList, function(i){
                var item = itemList[i].toString();
                var id = "perc-item-id-" + i;
                currentItemIds[item] = id;
                var htmlLi = $('<li class="perc-itemname" />')
							.attr('data-id', id)
                            .attr('title', item)
                            .html(item)
                            .data('item', item)
                            .addClass('perc-ellipsis');
                            
        // If  @param options.truncateEntries is set true, add the class perc-more-list to all list items greater than @param 'options.truncateEntriesCount'.
        // Also add the 'more link' after @param 'options.truncateEntriesCount' and 'less link' after the lest item
                if(options.truncateEntries && i>=options.truncateEntriesCount)
                {
                    htmlLi.addClass("perc-more-list perc-hidden").hide();
                    
                }
                ulRoot.append(htmlLi);
                if(options.truncateEntries && i==options.truncateEntriesCount)
                {
                    ulRoot.append('<li class="perc-moreLink perc-visible" style = "display:block" title = "more">more</li>');                
                }
                
                if(options.truncateEntries && i==itemList.length-1)
                {
                    ulRoot.append('<li class="perc-lessLink perc-hidden" style = "display:none" title = "less">less</li>');         
                }                
        });

        activateMoreLessLink(container);
                
        //bind select event on each element
        ulRoot.find(".perc-itemname").unbind().click(function(event){
            var item = $(this).data('item');
            dirtyController.confirmIfDirty(function(){
                if (typeof(options.selectedItem) == 'function')
                    options.selectedItem(item);
            });
        });
    }
    
    // Bind the click event to more/less link. Toggle the display of 'truncated entries', 'more link' and 'less link' on click.
    function activateMoreLessLink(container) {
           $(container).find(".perc-moreLink, .perc-lessLink").click(function(){
            $(container).find('.perc-more-list, .perc-moreLink, .perc-lessLink').toggle().toggleClass('perc-visible perc-hidden');
        });
    }
            
    function getCurrentItem(){
        return currentItemSelected;
    }
    
    function getCurrentList(){
        return currentItemList;
    }

    function selectItem(container, itemname) {
		var originalItemname = itemname;
		itemname = itemname.replace(/\\/g, "\\\\").replace(/\'/g, "\\'");
		var id = container.find(".perc-itemname[title='" + itemname + "']").attr('data-id');
		currentItemSelected = originalItemname;
        unhighlightAllItems(container);
        
        container.find("[data-id='"+id+"']")
            .css("background-color","#caf589")
            .addClass("perc-item-selected");
    }

    function unhighlightAllItems(container) {
        container.find("ul li")
            .css("background-color","")
            .removeClass("perc-item-selected");
        container.find("ul li div").css("background-color","");
    }
    
    function enableAddButton(container){
        var options = container.data('options');
        if (options.enableAdd){
            container.find(".perc-item-add-button")
                .unbind()
                .click(function(){
                    if (typeof(options.createItem) == 'function')
                        options.createItem();
                })
                .removeClass("perc-item-disabled")
                .addClass("perc-item-enabled");
        }
    }
    
    function enableDeleteButton(container){
        var options = container.data('options');
        if (options.enableDelete){
            container.find(".perc-item-delete-button")
                .unbind()
                .click(function(event){
                    event.stopPropagation(); // stop event because it is nested inside an element that is already bound
                    if (typeof(options.deleteItem) == 'function')
                            options.deleteItem(currentItemSelected);
                })
                .removeClass("perc-item-disabled")
                .addClass("perc-item-enabled");
        }
    }
    
    function enableButtons(container){
        enableAddButton(container);
        enableDeleteButton(container);
    }
    
    function disableAddButton(container){
        var options = container.data('options');
        if (options.enableAdd){
            container.find(".perc-item-add-button")
                .unbind()
                .addClass("perc-item-disabled")
                .removeClass("perc-item-enabled");
        }
    }
    
    function disableDeleteButton(container){
        var options = container.data('options');
        if (options.enableDelete){
            container.find(".perc-item-delete-button")
                .unbind()
                .removeClass("perc-item-enabled")
                .addClass("perc-item-disabled");
        }
    }
    
    function disableButtons(container){
        disableAddButton(container);
        disableDeleteButton(container);
    }
    
    function hideButtons(container){
        container.find(".perc-item-add-button").hide();
        container.find(".perc-item-delete-button").hide();
    }
})(jQuery);
