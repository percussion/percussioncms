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
        useEllipsisInTitle : true,
        useEllipsisInContent : true,
        toggable : true,
        marginLeft : 0,
        expandIcon : "",
        collapseIcon : "",
        isCollapsed : true,
        isToggleVisible : false,
        collapsedLines : 0
    };
    
    $.fn.percCollapsibleTitle = function(config) {

        config = $.extend({}, defaultConfig, config);
        
        var collapsible = this;
        var expandIcon = config.expandIcon;
        var collapseIcon = config.collapseIcon;
        var marginLeft = config.marginLeft + "px";
        var isCollapsed = config.isCollapsed;
        var title = config.title;
        var content = config.content;
        var collapsedLines = config.collapsedLines;
        var toggable = config.toggable;
        var firstLine;
        var secondLine;
        var isToggleVisible = config.isToggleVisible;

        if(typeof title !== 'undefined' && typeof content !== 'undefined') {
            firstLine = $("<div>");
            firstLine.append(title);
            firstLine.attr("title", title);
            secondLine = $("<div>");
            secondLine.append(content);
            secondLine.attr("title", content);
            collapsible.append(firstLine);
            collapsible.append(secondLine);
        } else {
            var lines = collapsible.find("div");
            firstLine = $(lines[0]);
            firstLine.attr("title", firstLine.text());
            secondLine = $(lines[1]);
            secondLine.attr("title", secondLine.text());
        }
        
        firstLine.addClass('perc-collapsibletitle-title');
        secondLine.addClass('perc-collapsibletitle-content');
        
        var secondLineHeightExpanded = secondLine.height();
        secondLine.css("white-space", "nowrap");
        var secondLineHeightCollapsed = secondLine.height();
        secondLine.css("white-space", "normal");
        var needsCollapsable = false;
        if(secondLineHeightExpanded > secondLineHeightCollapsed)
            needsCollapsable = true;

        secondLine.data("secondLineHeightCollapsed", secondLineHeightCollapsed);
        secondLine.data("expandIcon", expandIcon);
        secondLine.data("collapseIcon", collapseIcon);
        secondLine.data("collapsedLines", collapsedLines);
        
        firstLine.css("margin-left", marginLeft);
        secondLine.css("margin-left", marginLeft);

        if(!toggable) {
            firstLine.css("cursor","text");
            secondLine.css("cursor","text");
        }

        if(needsCollapsable) {
            var collapseControl = $("<div class='perc-collapsibletitle-toggle'>");
            collapseControl
                .css("position","absolute")
                .css("left", "15px");
            firstLine.prepend(collapseControl);
            if(toggable) {
                firstLine.on("click",function(evt){
                    handleCollapse(evt);
                });
                collapseControl.on("click",function(evt){
                    handleCollapse(evt);
                });
            }
            
            if(isCollapsed) {
                collapseControl.append(expandIcon);
                secondLine
                    .height(secondLineHeightCollapsed * collapsedLines)
                    .css("overflow", "hidden")
                    .attr("collapsed", "true");
            } else {
                collapseControl.append(collapseIcon);
                secondLine
                    .attr("collapsed", "false");
            }
            
            if(!isToggleVisible) {
                collapseControl.hide();
            }
        }
        
        var parentWidth = secondLine.parent().width();
        parentWidth += "px";

        if(config.useEllipsisInContent) {
            secondLine
                .css("white-space","nowrap")
                .css("text-overflow","ellipsis")
                .css("overflow","hidden")
                .css("display","block")
                .css("width",parentWidth);
        }
        /*
        if(config.useEllipsisInTitle) {
            firstLine
                .css("white-space","nowrap")
                .css("text-overflow","ellipsis")
                .css("overflow","hidden")
                .css("display","block")
                .css("width",parentWidth);
        }
        */
    };
    
    function handleCollapse(event) {
        var target = $(event.target);
        
        var collapseControl = null;
        
        if(target.is(".perc-collapsibletitle-toggle"))
            collapseControl = target;
        if(!collapseControl)
            collapseControl = $(target.parents(".perc-collapsibletitle-toggle")[0]);
        
        if(target.is(".perc-collapsibletitle-title")) {
            collapseControl = target.find(".perc-collapsibletitle-toggle");
        }
        
        var secondLine = collapseControl.parent("div").next("div");
        var isCollapsed = secondLine.attr("collapsed") == "true";
        

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
            var newHeight = secondLineHeightCollapsed * collapsedLines;
            
            // cant set height to 0 in IE (???)
            if($.browser.msie && newHeight <= 0)
                newHeight = 1;
            secondLine
                .height(newHeight)
                .css("overflow", "hidden");
        }
    }
})(jQuery);
