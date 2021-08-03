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

(function($)
{
    $(document).ready(function(){
        $.PercTagListView.updateTagList();
    });
    $.PercTagListView = {
        updateTagList : updateTagList
    };
    function updateTagList()
    {
        $(".perc-tag-list").each(function(){
            var currentTagList = $(this);
            if ("" === currentTagList.attr("data-query")){ return;}
            var queryString = JSON.parse(currentTagList.attr("data-query"));
            //Get the render option: comma separated or list
            var renderOption = queryString.tag_render_option;
            delete queryString.tag_render_option;
            //Get the result page to display the pages for each tag
            var pageResult = queryString.tag_page_result;
            delete queryString.tag_page_result;
            //Get the order by for the list of tags
            var orderBy = queryString.orderBy;
            delete queryString.orderBy;
            //Set the base URL to create the href for each tag then
            //Set the base URL to create the href for each item then
            let isEditMode = queryString.isEditMode;
            var baseURL = "";
            if(isEditMode === "true" || isPreviewMode === "true"){
                var paths = window.location.pathname.split("/");
                baseURL = "/" + paths[1] + "/" + paths[2];
            }else{
                baseURL = window.location.protocol + '//' + window.location.host;
            }
            var strJSON = JSON.stringify(queryString);

            $.PercTagListService.getTagEntries(queryString, orderBy, function(status, tagEntries){
                if(status)
                {
                    if(typeof (tagEntries)!=="undefined"){
                        tagEntries = $.PercServiceUtils.toJSON(tagEntries);
                    }
                    if(tagEntries.properties.length === 0){
                        return false;
                    }

                    $('.perc-list-empy-title').remove();
                    //Get the structure and get the root element of the list
                    var listRoot = currentTagList.find(".perc-tag-list-structure .perc-list-main");
                    //Clone the li for future use
                    var listElem = listRoot.find("li").clone();
                    listElem.addClass("perc-tag-element");
                    //Empty the root element and add it to the actual list root.
                    listRoot.empty();
                    currentTagList.find(".perc-list-main-container").empty().remove();
                    currentTagList.find(".perc-comma-separated-main-container").empty().remove();
                    var separator = "";
                    if ("undefined" !== typeof (renderOption) && "" !== renderOption && "commaSeparated" === renderOption)
                    {
                        listRoot.addClass("perc-list-main-inline");
                        listElem.addClass("perc-list-elemment-inline");
                        separator = ',';
                    }
                    else{
                        listRoot.addClass("perc-list-vertical");
                    }
                    currentTagList.append(listRoot);
                    //Loop through the tag entries and build the new list element as per the structure.
                    //Then add the newly created element to the list root.
                    for(var i=0;i<tagEntries.properties.length;i++)
                    {
                        var tagEntry = tagEntries.properties[i];
                        var newListElem = listElem.clone();
                        var rowClass = 0 === i % 2 ? "perc-list-even":"perc-list-odd";
                        var spClass = "";
                        if (0 === i)
                        {
                            spClass = "perc-list-first";
                        }
                        else if (i===tagEntries.properties.length-1)
                        {
                            spClass = "perc-list-last";
                            separator = "";
                        }

                        newListElem.addClass(rowClass);
                        if("" !== spClass) {
                            newListElem.addClass(spClass);
                        }
                        var linkText = tagEntry.tagName + " (" + tagEntry.tagCount + ")" + separator;
                        //Set the link for the tag
                        if ("undefined" !== typeof (pageResult) && "" !== pageResult)
                        {
                            var query = JSON.parse( strJSON );
                            query.criteria.push("perc:tags = '" + tagEntry.tagName + "'");
                            var encodedQuery = "&query=" + encodeURIComponent(JSON.stringify(query));
                            newListElem.find("a").attr("href", baseURL + pageResult + "?filter="+tagEntry.tagName + encodedQuery).html(linkText);
                        }
                        else{
                            newListElem.find("a").html(linkText);
                        }
                        listRoot.append(newListElem);
                    }
                }
                else
                {
                    //Log the error and leave the original list entries as is
                    //TODO: Log the error?
                    console.error(status);
                }
            });

        });
    }
})(jQuery);
