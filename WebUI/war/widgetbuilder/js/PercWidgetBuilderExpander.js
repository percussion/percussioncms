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

/**
 * The main javascript file for widget builder.
 * 
 */
(function($)
{
    var percDefsExpandedState = "perc-defs-expanded-state";
    $(function(){
        handleDefsCollapseExpander();
    });

    function handleDefsCollapseExpander(){
        // dim the ui when the user is not in the finder

        $("#perc-wb-defs-expander").on("click",function(){
            expandCollapseDefs(!$("#perc-wb-defs-container").is(":visible"));
        });
		 $("#perc-wb-defs-expander").on("keydown",function(eventHandler){
			 if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
					document.activeElement.click();
			}

        });

		var state = getDefsExpandedStateFromCookie();
        expandCollapseDefs(state !== 'collapsed');
    }
    
    function expandCollapseDefs (expand) {
        if(expand && $("#perc-wb-defs-container").is(":visible"))
            return;
        setDefsExpandedStateInCookie(expand);
        var $button = $("#perc-wb-defs-expander");
        if (expand) {
            $("#perc-wb-defs-container").slideDown(notifyResize);
            $button.removeClass('icon-plus-sign').addClass('icon-minus-sign');            
        }
        else {
            $("#perc-wb-defs-container").slideUp(notifyResize);
            $button.addClass('icon-plus-sign').removeClass('icon-minus-sign');            
        }
    }
    function notifyResize(){
        var wh;
        if( window.innerHeight )
            wh = window.innerHeight;
        // for IE case
        else if( document.documentElement.clientHeight )
            wh = document.documentElement.clientHeight - 4;
        $(".perc-widget-editing-container").height(wh-$("#perc-widget-menu").position().top-100);
    }
    function setDefsExpandedStateInCookie (isExpanded) {
        var state = isExpanded ? 'expanded' : 'collapsed';
        var options = {"sameSite": "Lax"};
        if (window.isSecureContext) {
            options.secure = true;
        }
        $.cookie(percDefsExpandedState, state, options);
    }
    function getDefsExpandedStateFromCookie () {
        return ('' + $.cookie(percDefsExpandedState));
    }

})(jQuery);
