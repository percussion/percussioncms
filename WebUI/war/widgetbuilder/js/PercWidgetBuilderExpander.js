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

/**
 * The main javascript file for widget builder.
 * 
 */
(function($)
{
    var percDefsExpandedState = "perc-defs-expanded-state";
    $(document).ready(function(){
        handleDefsCollapseExpander();
    });

    function handleDefsCollapseExpander(){
        // dim the ui when the user is not in the finder
        $('.perc-wb-defs-outer').hover(function highligh_actions () {
            $(this).removeClass('ui-disabled');
        }, function dim_actions () {
            $(this).addClass('ui-disabled');
        });
        $("#perc-wb-defs-expander").click(function(){
            expandCollapseDefs(!$("#perc-wb-defs-container").is(":visible"));
        });
        var state = getDefsExpandedStateFromCookie();
        expandCollapseDefs(state != 'collapsed');
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
        $.cookie(percDefsExpandedState, state);
    }
    function getDefsExpandedStateFromCookie () {
        return ('' + $.cookie(percDefsExpandedState));
    }

})(jQuery);