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

(function($) {

    var defaultConfig = {
        expandControl   : "<b>[+]" +I18N.message("perc.ui.collapsible.more@More") + "</b>",
        collapseControl : "[-]" +I18N.message("perc.ui.collapsible.more@Less"),
        maxLines : 3
    };
    
    $.fn.percCollapsibleMore = function(config) {

        config = $.extend({}, defaultConfig, config);
        
        var collapsible = this;//config.collapsible;
            
        collapsible.addClass("perc-collapsible-more-content");
            
        var toggleControl = $("<div>");
        toggleControl
            .append(config.expandControl)
            .addClass("perc-collapsible-more-toggle");
            
        var maxLines = config.maxLines;
        var content = config.content;
            
        if(content)
            collapsible.append(content);
        
        var contentHeightExpanded = collapsible.height();
        collapsible.css("white-space", "nowrap");
        var contentHeightOneLine = collapsible.height();
        collapsible.css("white-space", "normal");
        
        collapsible.wrap("<div class='perc-collapsible-more'>");
        var wrapper = collapsible.parent();
        
            
        wrapper.append(toggleControl);
        toggleControl.on("click",function(evt){
            collapse(evt);
        });

        collapsible.data("contentHeightOneLine", contentHeightOneLine);
        collapsible.data("contentHeightExpanded", contentHeightExpanded);
        collapsible.data("config", config);

        if(contentDoesntFit(collapsible)) {
            toggleControl.trigger("click");
        } else {
            toggleControl.hide();
        }
    };
    
    function collapse(event) {
        var target = $(event.target);
        var wrapper = $(target.parents(".perc-collapsible-more")[0]);
        
        var collapsible   = $(wrapper.find(".perc-collapsible-more-content")[0]);
        var toggleControl = $(wrapper.find(".perc-collapsible-more-toggle")[0]);
        
        var config = collapsible.data("config");
        var contentHeightOneLine = collapsible.data("contentHeightOneLine");
        var maxLines = config.maxLines;
        var toggleIcon = config.expandControl;
        
        var newHeight = contentHeightOneLine * (maxLines - 1);
        collapsible
            .height(newHeight)
            .css("overflow", "hidden");
        toggleControl
            .html(toggleIcon)
            .off()
            .on("click",function(evt){
                expand(evt);
            });
    }

    function expand(event) {
        
        var target = $(event.target);
        var wrapper = $(target.parents(".perc-collapsible-more")[0]);
        
        var collapsible   = $(wrapper.find(".perc-collapsible-more-content")[0]);
        var toggleControl = $(wrapper.find(".perc-collapsible-more-toggle")[0]);
        
        var config = collapsible.data("config");
        var toggleIcon = config.collapseControl;
        
        collapsible.height("auto");
        
        toggleControl
            .html(toggleIcon)
            .off("click")
            .on("click",function(evt){
                collapse(evt);
            } );
    }

    function contentDoesntFit(collapsible) {
        var config = collapsible.data("config");
        var contentHeightExpanded = collapsible.data("contentHeightExpanded");
        var contentHeightOneLine = collapsible.data("contentHeightOneLine");
        var maxLines = config.maxLines;
        var numberOfLines = contentHeightExpanded/contentHeightOneLine;
        if(numberOfLines > maxLines)
            return true;
        return false;
    }
    
    function handleCollapse(event) {
        var target = $(event.target);
        var collapseControl = null;
        if(target.is(".perc-collapse-control"))
            collapseControl = target;
        if(!collapseControl)
            collapseControl = $(target.parents(".perc-collapse-control")[0]);
        
        var secondLine = collapseControl.parent("div").next("div");
        var isCollapsed = secondLine.attr("collapsed") === "true";

        var secondLineHeightCollapsed = secondLine.data("secondLineHeightCollapsed");
        var expandIcon = secondLine.data("expandIcon");
        var collapseIcon = secondLine.data("collapseIcon");
        var collapsedLines = secondLine.data("collapsedLines");

        if(isCollapsed) {
            collapseControl.html(collapseIcon);
            secondLine.attr("collapsed", "false");
            secondLine.height("auto");
        } else {
            collapseControl.html(expandIcon);
            secondLine.attr("collapsed", "true");
            secondLine
                .height(secondLineHeightCollapsed * collapsedLines)
                .css("overflow", "hidden");
        }
    }
})(jQuery);
