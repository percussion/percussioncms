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
 *  PercDropdown.js
 *  @author Jose Annunziato
 *
 *  +-----------------------------+
 *  |percDropdownOptionLabels[0]  | ---> percDropdownCallbacks[0](percDropdownCallbackData[0])
 *  +-----------------------------+
 *  |percDropdownOptionLabels[1]  | ---> percDropdownCallbacks[1](percDropdownCallbackData[1])
 *  |percDropdownOptionLabels[2]  | ---> percDropdownCallbacks[2](percDropdownCallbackData[2])
 *  |percDropdownOptionLabels[3]  | ---> percDropdownCallbacks[3](percDropdownCallbackData[3])
 *  |...                          |
 *  +-----------------------------+
 *
 *  (*) Configuration (refer to diagram above)
 *  @param percDropdownRootClass (string) add class to the root element to style the whole component
 *  @param percDropdownOptionLabels (string[]) labels for the dropdown. 1st is the title label
 *  @param percDropdownOptionElements (object[]) HTML elements for the dropdown. The labels option must be specified anyway
 *  @param percDropdownCallbacks (function[]) functions to call when selecting a dropdown item. Element 0 is callback when clicking on title
 *  @param percDropdownCallbackData (object[]) data to pass back when calling callbacks for each dropdown item. Element 0 is callback when clicking on title
 *  @param percDropdownTitleImage (string) path and filename of image to show by default. If not null, then percDropdownOptionLabels[0] is not used as title
 *  @param percDropdownTitleImageOver (string) path and filename of image to show when hover over title
 *  @param percDropdownShowExpandIcon (boolean) whether to show or not the drop down arrow next to the title
 *  @param percDropdownResizeToElement (string) id or class of parent div containing split button
 *  @param percDropdownItemImage (string) URL path for the image that will appear on left side of the menu item when menu is Enabled
 *  @param percDropdownDisabledItemImage (string) URL path for the image that will appear on left side of the menu item when menu is Disabled
 *
 *
 *  (*) Usage:
 *  // declare callback functions receiving callback data
 *  function publish(callbackData) {...}
 *  function takeDown(callbackData) {...}
 *  ...
 *  // configure the dropdown
 *  var dropdownConfig = {
 *      percDropdownRootClass      : "perc-view",
 *      percDropdownOptionLabels   : ["Publish", "Takedown","Approve","Close","Open"],
 *      percDropdownCallbacks      : [publish, takeDown, approve, close, function(data){open(data);}],
 *      percDropdownCallbackData   : ["Publish !!!", "Take Down", {pageId : 123}, "Close !!!", [123,234]],
 *      percDropdownTitleImage     : null,
 *      percDropdownItemImage      : ['', '../images/images/menuIconStacked.png', '../images/images/menuIconSide.png'],
 *      percDropdownDisabledItemImage : ['', '../images/images/menuIconStackedGray.png', '../images/images/menuIconSideGray.png'],
 *      percDropdownTitleImageOver : null,
 *      percDropdownShowExpandIcon : true,
 *      percDropdownResizeToElement: "#parent-split-div"
 *  }
 *  // apply the plugin to the container passing configuration
 *  $("#someDiv").PercDropdown(config1);
 *
 *  (*) Dependencies
 *  /css/PercDropdown.css - overrides superfish's default styling to implement style guide
 *  /css/superfish.css
 *  /jslib/superfish.js
 *  /jslib/hoverIntent.js
 *  /jslib/PSJSUtils.js
 *
 *  (*) Styling
 *  To style declare percDropdownRootClass in config and then use in your own CSS.
 *  Example:
 *  1) Declare percDropdownRootClass in config
 *
 *      var config1 = {
 *          percDropdownRootClass : "perc-workflow"
 *          ...
 *      }
 *
 *  2) Create your own CSS, i.e., /cm/css/PercWorkflowDropdown.css:
 *
 *      .perc-workflow.sf-menu a.perc-dropdown-title {
 *          background: url(../images/images/buttonWfpublish.png) no-repeat;
 *          width : 68px;
 *      }
 *      .perc-workflow.sf-menu a.perc-dropdown-title:hover {
 *          background: url(../images/images/buttonWfpublishOver.png) no-repeat;
 *          width : 68px;
 *      }
 *
 *  (*) Unit Testing/Example: /widgets/PercDropdownTest.html
 *
 */
(function($) {

    var defaultConfig = {
        percDropdownRootClass      : "perc-view",
        percDropdownOptionLabels   : ["Title","Option1","Option2"],
        percDropdownOptionElements : null,
        percDropdownCallbacks      : [function(param){alert(param);},function(param){alert(param);},function(param){alert(param);}],
        percDropdownCallbackData   : ["Title","Option1","Option2"],
        percDropdownDisabledFlag   : [true,true,true,true,true],
        percDropdownItemImage      : null,
        percDropdownTitleImage     : null,
        percDropdownTitleImageOver : null,
        percDropdownShowExpandIcon : true,
        percDropdownResizeToElement: null,
        percDropdownDisabledItemImage: null,
        delay:         50,
        dropShadows:   true
    };

    var dropdownHtml = "" +
        "<ul class='sf-menu perc-dropdown'>" +
        "<li class='current'>" +
        "<a class='perc-dropdown-title'>_TITLE_</a>" +
        "<ul class='perc-dropdown-option-list'>" +
        "<li class='perc-dropdown-option-item-template '>" +
        "<a href='#' class='perc-dropdown-option'>" + I18N.message("perc.ui.edit.workflow.step.dialog@" + "_OPTION_") + "</a>" +
        "</li>" +
        "</ul>" +
        "</li>" +
        "</ul>";
    var dropdown;
    var SPLIT_BUTTON_BORDER = 4;

    /**
     *  Implement plugin and add it to the element the plugin is applied to
     */
    $.fn.PercDropdown = function(config) {
        // merge default and custom config
        config = $.extend({}, defaultConfig, config);

        dropdown = $(dropdownHtml);
        dropdown
            .addClass(config.percDropdownRootClass)
            .data("config", config);

        if(config.percDropdownResizeToElement) {
            config.onBeforeShow = resizeDropdown;
        }

        var callbacks = config.percDropdownCallbacks;
        var callbackData = config.percDropdownCallbackData;
        var labels = config.percDropdownOptionLabels;
        var disabledFlags = config.percDropdownDisabledFlag;
        var listImage = config.percDropdownItemImage;
        var disabledListImage = config.percDropdownDisabledItemImage;
        var title = dropdown.find(".perc-dropdown-title");
        if(config.percDropdownTitleImage == null) {
            title
                .css("cursor","default")
                .html(labels[0]);
        } else {
            var arrowClass = config.percDropdownRootClass ? config.percDropdownRootClass + "-split-button-arrow" : "perc-split-button-arrow"
            title
                .html("&nbsp;")
                .css("cursor","pointer")
                .addClass(arrowClass)
                .addClass('btn btn-primary perc-workflow-split-button-right')
                .hover(
                    function(){
                        $(this)
                            .css("overflow","visible");
                    },
                    function(){
                        $(this)
                            .css("overflow","visible");
                    });

            // TODO: should this styling be on a CSS? the margin should be based on the size of the image.
            var optionsList = dropdown.find(".perc-dropdown-option-list");
            optionsList.css("margin", "4px 0 0 0");
        }
        title
            .addClass("perc-dropdown-title-"+labels[0])
            .data("callback", callbacks[0])
            .data("callbackData", callbackData[0])
            .click(clickDropdown);

        var dropdownOptionList = dropdown.find(".perc-dropdown-option-list");
        var dropdownOptionItemTemplate = dropdown.find(".perc-dropdown-option-item-template");
        var k = 0;
        for(l=1; l<labels.length; l++){
            k = l;
            var dropdownOptionLabel = labels[l];
            var dropdownOptionItem = dropdownOptionItemTemplate.clone();
            dropdownOptionItem.removeClass("perc-dropdown-option-item-template");

            // If percDropdownOptionElements is not defined use the labels
            var optionAElem;
            if (config.percDropdownOptionElements != null)
            {
                // Note that the element will have a class with the corresponding label
                dropdownOptionItem.find("a").replaceWith(config.percDropdownOptionElements[l]);
                optionAElem = config.percDropdownOptionElements[l].addClass("perc-dropdown-option-"+$.perc_textFilters.IDNAMECDATA(dropdownOptionLabel));
            }
            else
            {
                optionAElem = dropdownOptionItem.find("a")
                    .html(I18N.message("perc.ui.edit.workflow.step.dialog@" + dropdownOptionLabel))
                    .addClass("perc-dropdown-option-"+$.perc_textFilters.IDNAMECDATA(dropdownOptionLabel));
            }

            if(listImage != null)
            {
                optionAElem.css('background-image', 'url(' + listImage[l] + ')')
                    .css('background-repeat', 'no-repeat')
                    .css('background-position', '10px 8px')
                    .css('padding-left', 28);
            }

            if(!(disabledFlags[l])){
                k = 0;
                optionAElem.addClass('perc-drop-disabled').css('color', '#9FA3AA');
                if(disabledListImage != null) {
                    optionAElem.css('background-image', 'url(' + disabledListImage[l] + ')')
                        .css('background-repeat', 'no-repeat')
                        .css('background-position', '10px 8px')
                        .css('padding-left', 28);
                }
            }

            dropdownOptionList.append(dropdownOptionItem);
            dropdownOptionItem
                .data("callback", callbacks[k])
                .data("callbackData", callbackData[k])
                .click(clickDropdown);
        }
        dropdownOptionItemTemplate.remove();
        dropdown.superfish(config);

        if(config.percDropdownShowExpandIcon === false) {
            title.find(".sf-sub-indicator").hide();
        } else {

        }

        this.append(dropdown);

        if(config.percDropdownRootClass == "perc-dropdown-template-pages-items-dropdown"){
            $('ul.perc-dropdown-template-pages-items-dropdown > li > ul.perc-dropdown-option-list').css('max-height', '66px');
            $('ul.perc-dropdown-template-pages-items-dropdown > li > ul.perc-dropdown-option-list').css('overflow-y', 'auto');
        }
    }


    /**
     *  Handle callbacks when clicking on dropdown options or title
     */
    function clickDropdown(event) {
        var callback = $(this).data("callback");
        var callbackData = $(this).data("callbackData");
        callback(callbackData);
    }

    /**
     *  Resizes the dropdown to the same width as the parent div of a split button
     */
    function resizeDropdown() {
        var menu = $(this);
        var percDropdown = $(menu.parents(".perc-dropdown")[0]);
        var config = percDropdown.data("config");
        if(typeof(config) != "undefined")
        {
            var resizeToElement = $(config.percDropdownResizeToElement);
            var dropdownWidth = resizeToElement.width();
            menu.width(dropdownWidth - SPLIT_BUTTON_BORDER);
        }
    }

})(jQuery);
