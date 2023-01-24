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
