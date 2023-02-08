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
            var isEditMode = queryString.isEditMode;
            var baseURL = "";
            var isPreviewMode = queryString.isPreviewMode;
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
                    //currentTagList.append(listRoot);
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
                    currentTagList.append(listRoot);
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
