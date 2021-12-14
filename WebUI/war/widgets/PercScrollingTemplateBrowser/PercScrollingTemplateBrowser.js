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
 * @author Jose Annunziato
 */
(function($){
    $.fn.PercScrollingTemplateBrowser = function(config){
        
        var scrollable = $("<div class='perc-scrollable'>");
        var items = $("<div class='perc-items'>");
        scrollable.append(items);
        
        var prev = $("<a style = 'margin:50px 0px' class='prevPage browse left' ></a>");
        var next = $("<a style = 'margin:50px 0px' class='nextPage browse right'></a>");
        var clearboth = $("<div style='clear:both'>");
        
        $(this)
            .append(prev)
            .append(scrollable)
            .append(next)
            .append(clearboth);
        
        if(config.width)
            scrollable.width(config.width);
        
        var widgetDefId = null;
        if (config.widgetDefId)
            widgetDefId = config.widgetDefId;

        
        var siteName = config.siteName;
        var hiddenFieldId = config.hiddenFieldId?config.hiddenFieldId:"perc-select-template";
        var calbackfn = function(status, data){
            if(data.TemplateSummary.length === 0) {
                var empty = $("<div class='perc-empty'>" +I18N.message("perc.ui.scrolling.template.browser@No Templates Found") + "</div>")
                    .css("margin-top","70px");
                scrollable
                    .css("text-align","center")
                    .css("background","white")
                    .append(empty);
            } else {
            
                $.each( data.TemplateSummary, function(index, template) {
                    // add template instance to scrollable items
                    items.append(createTemplateEntry(this, config));
                    
                    // hide the id that appears at the top of the template
                    items.find(".item .item-id").hide();
                    
                    // bind click event to each item to handle selection
                    items.find(".item").on('click', function(){
                        var itemId = $(this).find(".item-id").text();
                        $("#" + config.hiddenFieldId).val(itemId);
                        items.find(".item").removeClass("perc-selected-item");
                        $(this).addClass("perc-selected-item");
                    });
                    
                    // select first item by default
                    $firstItem = items.find(".item:first");
                    $("#" + config.hiddenFieldId).val($firstItem.find(".item-id").text());
                    $firstItem.addClass("perc-selected-item");
                });
                
                // make it scollable
                scrollable.scrollable({
                    items: items,
                    size: 4,
                    keyboard: true
                });
                
                // after adding all the template entries, truncate the labels if they dont fit
                // $.PercTextOverflow($("div.perc-text-overflow"), 122);
            }
        };
        if(!config.isBase)
        {
        	$.PercSiteService.getTemplates(siteName, calbackfn, widgetDefId);
        }
        else
        {
        	$.PercSiteService.getBaseTemplates(config.baseType, calbackfn);
        }
            //Load template selector
        return $(this);
            
    };

    function createTemplateEntry(data, config){
        var temp = "<div for=\"@ITEM_ID@\" class=\"item\">"
         + "<div class=\"item-id\">@ITEM_ID@</div>"
         + "    <table>"
         + "        <tr><td align='left'>"
         + "            <img style='border:1px solid #E6E6E9' height = '86px' width = '122px' src=\"@IMG_SRC@\"/>" 
         + "        </td></tr>"
         + "        <tr><td>"
         + "            <div class='perc-text-overflow-container' style='text-overflow:ellipsis;width:122px;overflow:hidden;white-space:nowrap'>"
         + "                <div class='perc-text-overflow' style='float:none' title='@ITEM_TT@' alt='@ITEM_TT@'>@ITEM_LABEL@</div>"
         + "        </td></tr>"
         + "    </table>"        
         + "</div>";
        var tplName = data.name;
        var tplId = data.id;
        if(config.isBase){
        	tplName = tplName.replace("perc." + config.baseType + ".", "");
        	tplId = data.name;
        }
        return temp.replace(/@IMG_SRC@/, data.imageThumbPath)
            .replace(/@ITEM_ID@/g, tplId)
            .replace(/@ITEM_LABEL@/, tplName)
            .replace(/@ITEM_TT@/g, tplName);
    }

})(jQuery);
