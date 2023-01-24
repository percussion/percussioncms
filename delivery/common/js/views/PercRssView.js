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

    $(document).ready(function() {
        $.PercRssView.updateRssFeed();
    });
    
    $.PercRssView = {
        updateRssFeed : updateRssFeed
    };
    
    function updateRssFeed()
    {
        $('.perc-feed-widget').each(function(){
            var currentFeedWidget = $(this);
            if ("" === currentFeedWidget.attr("data-query")){ return;}
            var queryString = JSON.parse(currentFeedWidget.attr("data-query") );
            
            $.PercRssService.getExternalFeed(queryString.urlFeed, queryString, function(status, result){
                var feed = new JFeed(result);
                
                if(queryString.showTitle && null !== feed.title && "undefined" !== typeof (feed.title))
                {
                    var feedTitleId = queryString.feedId +"_title";
                    currentFeedWidget.attr('aria-labelledby', feedTitleId);
                    var titleElement = queryString.titleElement;
                    var feedTitle = '<' + titleElement + ' id="' + feedTitleId + '" ';
                    feedTitle += ' title="' + feed.title + '" class="perc-feed-title"> <a href="' + feed.link + '" target="_blank" rel="noopener noreferrer">' + feed.title + '</a> </'+titleElement+'>';
                    currentFeedWidget.append(feedTitle);
                } else  if (null !== feed.title && "undefined" !== typeof (feed.title)) {
                    currentFeedWidget.attr('aria-label', feed.title);
                } else {
                    currentFeedWidget.attr('aria-label', queryString.feedId);
                }

                var itemLimit = parseInt(queryString.itemLimit.substring(queryString.itemLimit.indexOf("_") + 1));

                var feedItem = '';
                if("undefined" !== typeof feed.items && null !== feed.items) {
                    if (0 > itemLimit)
                    {
                        itemLimit=feed.items.length;
                    }
                    feedItem = renderFeedItems(feed.items, queryString, itemLimit);
                } else {
                    // Handle no items or error here
                }
                currentFeedWidget.append(feedItem);
                currentFeedWidget.attr("aria-busy","false");
            });
        });
    }
    
    //Render feed items
    function renderFeedItems(feedItems, queryString, itemLimit) {
        var feedItem = '';
        var itemAriaRole = queryString.itemAriaRole;
        var wrappingElement = queryString.wrappingElement;
        var itemElement = queryString.itemElement;
        var itemTitleElement = queryString.itemTitleElement;
        var itemDateElement = queryString.itemDateElement; 
        var itemDescriptionElement = queryString.itemDescriptionElement;
        var itemIdPrefix = queryString.feedId +"_item_";
        var wrappingElemAriaLabel = queryString.wrappingElemAriaLabel;

        if ('none' !== wrappingElement)
        {
            feedItem += '<' +wrappingElement;
            if ("ul" !== wrappingElement && "li" !== wrappingElement)
            {
                 feedItem += ' role="list" ';
            }
            if (null !== wrappingElemAriaLabel && "undefined" !== typeof (wrappingElemAriaLabel) && ("" !== wrappingElemAriaLabel)) {
                feedItem += ' aria-label="' + wrappingElemAriaLabel + '" >';
            }else {
                feedItem += ' >';
            }
        }
        for(var i = 0; i < feedItems.length && i < itemLimit; i++)
        {
            var item = feedItems[i];
            var id=itemIdPrefix+i;
            var title_id = id+"_title";
            var label="";

            if(queryString.showItemTitle && null !== item.title)
            {
                label='aria-labelledby="' + title_id + '"';
            }
            else
            {
                label='aria-label="' + item.title + '"';
            }

            feedItem += '<' +itemElement+' ';
            if ("li" !== itemElement && "none" !== wrappingElement)
            {
                feedItem += 'role="listitem" ';
            }

            feedItem += 'id="'+id+'" class="perc-feed-item"' + label + '>';
            
            
            if(queryString.showItemTitle && item.title)
            {
                feedItem += '<'+itemTitleElement+' id="'+title_id+'" class="perc-feed-item-title"><a href="' + item.link + '" target="_blank" title="' + item.title + '" rel="noopener noreferrer">' + item.title + '</a> </'+itemTitleElement+'>';
            }
            
            if(queryString.showItemDate && item.updated)
            {
                var formattedDate = "";
                var formattedTime = "";
                var date = new Date(item.updated);
                
                formattedDate = $.datepicker.formatDate(queryString.itemDateFormat, date);
                formattedDate = formattedDate.replace('hh', date.getHours());
                formattedDate = formattedDate.replace('nn', date.getMinutes());

                feedItem += '<'+itemDateElement+' class="perc-feed-item-date">' + formattedDate + '</'+itemDateElement+'>';
            }
            
            if(queryString.showItemDescription && item.description)
            {
                var description = $("<"+itemDescriptionElement+">").html(item.description);
                
                if(queryString.itemRemoveHtml)
                {
                    description.html(description.text());
                }
                
                if(!queryString.itemDescriptionEmpty)
                {
                    //Truncate description
                    description.html(description.html().substring(0, queryString.itemDescriptionLength));
                    //If there are more description
                    if('' !== item.description.substring(queryString.itemDescriptionLength + 1))
                    {
                        description.html(description.html().trim() + "...");
                    }
                }

                feedItem += '<'+itemDescriptionElement+' class="perc-feed-item-description">' + description.html() + '</'+itemDescriptionElement+'>';
            }
            feedItem += '</'+itemElement+'>';
        }
        if ('none' !== wrappingElement)
        {
            feedItem += '</' +wrappingElement+ '>';
        }
        return feedItem;
    }
    
})(jQuery);
