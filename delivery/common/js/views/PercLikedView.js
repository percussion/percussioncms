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

(function($){
    
    // constants
    var LIKED = 1, UNLIKED = 0, PAGE_TYPE = "page";
    var PERC_LIKE_WIDGET = ".perc-like-widget";
    var PERC_LIKE_TOTAL_LIKES = ".perc-like-total-likes";
    var PERC_UNLIKED = "perc-unliked";
    var PERC_LIKED = "perc-liked";
    
    // state variables
    var likePageUrl;
    var likeSite;
    var likePathname;
    var likeTotalLikes;
    var likeCookieState;
    //var likeService = $.PercLikedService;
    var likeWidgetState = UNLIKED;
    
    function initLikeWidget() {
    
        if (!($(PERC_LIKE_WIDGET).length )) {
            return;
        }

        $.PercLikedService.siteName = $(PERC_LIKE_WIDGET).attr("siteName");
        
        if($.PercLikedService.isThisLiked()) {
            likeWidgetState = LIKED;
        }
        else {
            likeWidgetState = UNLIKED;
        }
        
        $.PercLikedService.getTotalLikesForThisPage(function(success, data){
            if(success) {
                likeTotalLikes = data.totalLikes;
            }
			else {
				if(likeWidgetState === LIKED) {
					likeTotalLikes = 1;
				}
				else {
					likeTotalLikes = 0;
				}
			}
            renderLikeWidget(likeWidgetState, likeTotalLikes);
        });
    }
    
    function renderLikeWidget(likeWidgetState, likeTotalLikes) {
		if("undefined" === typeof (likeWidgetState) || null === likeWidgetState){ return false;}
		if("undefined" === typeof (likeTotalLikes) || null === likeTotalLikes) {return false;}
		
        var likeWidget = $(PERC_LIKE_WIDGET);
        var button = likeWidget.find("button");
        button.trigger("blur");
        var totalLikes = likeWidget.find(PERC_LIKE_TOTAL_LIKES);
        if(likeWidgetState === LIKED) {
            likeWidget
                .removeClass(PERC_UNLIKED)
                .addClass(PERC_LIKED);
            button
                .off('click').on('click',unlike)
                .attr("title", "Remove");
        } else {
            likeWidget
                .removeClass(PERC_LIKED)
                .addClass(PERC_UNLIKED);
            button
                .off('click').on('click', like)
                .attr("title", "Like");
        }
        if(0 !== likeTotalLikes && "undefined" !== typeof (likeTotalLikes) && null !== likeTotalLikes) {
            $(".perc-like-counter").show();
            totalLikes.text(likeTotalLikes);
			if(1 === likeTotalLikes)
			{
				$('.perc-like-people').text('person');
			}
			else
			{
				$('.perc-like-people').text('people');
			}
        } else {
            $(".perc-like-counter").hide();
        }
    }
    
    function like() {
        likeWidgetState = LIKED;
        $.PercLikedService.likeThis(function(success, data){
            if(success) {
                likeTotalLikes = data.totalLikes;
            } else {
				if(likeWidgetState === LIKED) {
                    likeTotalLikes = 1;
                }
				else {
                    likeTotalLikes = 0;
                }
            }
            renderLikeWidget(likeWidgetState, likeTotalLikes);
        });
    }
    
    function unlike() {
        $.PercLikedService.unlikeThis(function(success, data){
			if(success){
				likeWidgetState = UNLIKED;
				likeTotalLikes = data.totalLikes;
			} else {
				if(likeWidgetState === LIKED) {
                    likeTotalLikes = 1;
                }
				else {
                    likeTotalLikes = 0;
                }
			}
				
            renderLikeWidget(likeWidgetState, likeTotalLikes);
        });
    }
    
    $(document).ready(function(){
        initLikeWidget();
    });
})(jQuery);

