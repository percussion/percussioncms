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
         };
    var currentItemList = [];
    var currentItemIds = {};
    var currentItemSelected = "";
        
    function init(container, config) {
        options = $.extend(true, {}, defaultConfig, config);
        container.data('options', options);
        var head = $('<div class="perc-item-list-label" />')
                   .html(options.title);
        container.append(head);
        //At the top we could show a add or delete button
        if (options.enableDelete)
            container.find('.perc-item-list-label').append($('<div class="perc-item-delete-button" aria-disabled="false" role="button" tabindex="0" />').attr('title', options.deleteTitle));
        if (options.enableAdd)
            container.find('.perc-item-list-label').append($('<div class="perc-item-add-button" aria-disabled="false" role="button" tabindex="0"/>').attr('title', options.addTitle));
        if(options.collapsible) {
            container.find('.perc-item-list-label').append($('<span style="float: left;" role="button"  id="perc-wf-min-max" class = "perc-items-minimizer" />').attr('title', I18N.message("perc.ui.workflow.view@Minimize")));
        }    

        var list = $('<div class="perc-itemname-list" />').append($("<ul/>"));
        container.append(list);
        updateList(container, []);
        
        //bind create event
		container.find(".perc-item-add-button").off("keydown").on("keydown",function(eventHandler){
			if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
					document.activeElement.click();
			}
		});

        container.find(".perc-item-add-button").off("click").on("click",function(){
            var options = container.data('options');
            if (typeof(options.createItem) == 'function') {                
                options.createItem();
            }    
        });        
        //bind collapsible event

        container.find("#perc-wf-min-max").off("click").on("click",function() {
            $(this).toggleClass('perc-items-minimizer').toggleClass('perc-items-maximizer');
			var myclass = $(this).attr('class');
			if(myclass == "perc-items-minimizer"){
				$(this).attr('title',I18N.message("perc.ui.workflow.view@Minimize"));
			}else if(myclass == "perc-items-maximizer"){
				$(this).attr('title',I18N.message("perc.ui.workflow.view@Maximize"));
			}
		   container.find('.perc-itemname-list').slideToggle("fast");
        
        });

		container.find("#perc-wf-min-max").off("keydown").on("keydown",function(eventHandler) {
            if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				document.activeElement.click();
			}

        });

        //bind the delete event for each element
		container.find(".perc-item-delete-button").off("keydown").on("keydown",function(eventHandler) {
           if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
				document.activeElement.click();
			}
        });

        container.find(".perc-item-delete-button").off("click").on("click",function(event) {
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
							.attr('tabIndex', '0')
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
                if(options.truncateEntries && i===options.truncateEntriesCount)
                {
                    ulRoot.append('<li class="perc-moreLink perc-visible" style = "display:block" title = "more">more</li>');                
                }
                
                if(options.truncateEntries && i===itemList.length-1)
                {
                    ulRoot.append('<li class="perc-lessLink perc-hidden" style = "display:none" title = "less">less</li>');         
                }                
        });

        activateMoreLessLink(container);
                
        //bind select event on each element
        ulRoot.find(".perc-itemname").off("click").on("click",function(event){
            var item = $(this).data('item');
            dirtyController.confirmIfDirty(function(){
                if (typeof(options.selectedItem) == 'function')
                    options.selectedItem(item);
            });
        });
		ulRoot.find(".perc-itemname").off("keydown").on("keydown",function(event){
            if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
			}
        });
    }
    
    // Bind the click event to more/less link. Toggle the display of 'truncated entries', 'more link' and 'less link' on click.
    function activateMoreLessLink(container) {
           $(container).find(".perc-moreLink, .perc-lessLink").on("click",function(){
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
		var originalItemname = String(itemname);
		itemname = String(itemname).replace(/\\/g, "\\\\").replace(/\'/g, "\\'");
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
                .off("click")
                .on("click",function(){
                    if (typeof(options.createItem) == 'function')
                        options.createItem();
                })
				 .on("keydown",function(eventHandler){
                    if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
						document.activeElement.click();
					}
                })
                .removeClass("perc-item-disabled")
                .addClass("perc-item-enabled")
				.attr("aria-disabled","false");;
        }
    }
    
    function enableDeleteButton(container){
        var options = container.data('options');
        if (options.enableDelete){
            container.find(".perc-item-delete-button")
                .off("click")
                .on("click",function(event){
                    event.stopPropagation(); // stop event because it is nested inside an element that is already bound
                    if (typeof(options.deleteItem) == 'function')
                            options.deleteItem(currentItemSelected);
                })
				.on("keydown",function(eventHandler){
                    if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
						document.activeElement.click();
					}
                })
                .removeClass("perc-item-disabled")
                .addClass("perc-item-enabled")
				.attr("aria-disabled","false");;
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
                .off()
                .addClass("perc-item-disabled")
                .removeClass("perc-item-enabled")
				.attr("aria-disabled","true");
        }
    }
    
    function disableDeleteButton(container){
        var options = container.data('options');
        if (options.enableDelete){
            container.find(".perc-item-delete-button")
                .off()
                .removeClass("perc-item-enabled")
                .addClass("perc-item-disabled")
				.attr("aria-disabled","true");;
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
