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
                .off('click').on('click',
                function(evt){
                    unlike(evt);
                }).attr("title", "Remove");
        } else {
            likeWidget
                .removeClass(PERC_LIKED)
                .addClass(PERC_UNLIKED);
            button
                .off('click').on('click', function(evt){
                    like(evt);
            })
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
    
    function like(event) {
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
    
    function unlike(event) {
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

