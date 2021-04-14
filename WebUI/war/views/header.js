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
 * JGA: refactored this so that we dont use a 100ms setInterval()
 * Instead we bind events that change the top part (finder, etc)
 * and change the iframe height based on that * 
 */

(function($)
{
    // Fixes the height and width of the Iframe
    fixIframeHeight = function() {
    	$("#perc-widget-library").width($("#tabs-3").width() - 45);
        var frame  = $('#frame');
        if(frame.length === 0)
            return;
             
        var header = $('.perc-main');
        var bottom = $('#bottom');
        
        var bot = bottom.position().top;
        header.height( bot );
		     
        var wh, ww;
        if( window.innerHeight ) {
            
            wh = window.innerHeight;
            ww = window.innerWidth;
            
        } else if( document.documentElement.clientHeight ) {
            //prevent IE freakout
            wh = document.documentElement.clientHeight - 4;
            ww = document.documentElement.clientWidth;
        }        
        frame.height( wh - bot);
        frame.width(ww);    
    };
    
    fixTemplateHeight = function(){
    	
    	fixBottomHeight();
    	
    };
    
    fixBottomHeight = function() {
    	var currentView = $.PercNavigationManager.getView();
    	var bottomContentDiv;
    	var bottomVerticalOffset;
    	
        if(currentView === $.PercNavigationManager.VIEW_DESIGN) {
            bottomContentDiv     = $('.perc-templates-layout');
            // 47px come from padding-top and padding bottom of the element selected
            bottomVerticalOffset = bottomContentDiv.position().top + 47;
        } else if(currentView === $.PercNavigationManager.VIEW_WORKFLOW) {
           bottomContentDiv     = $('.perc-whitebg');          
           bottomVerticalOffset = $('#tabs').position().top + 90; 
           // (90 = The difference between the start of the #tab div and .perc-whitebg div)
           $(".perc-finder-fix").css('padding-bottom', 0);
        } else if(currentView === $.PercNavigationManager.VIEW_PUBLISH) {
            bottomContentDiv     = $('.perc-whitebg');
            if ($("#tabs").length) 
            {
                bottomVerticalOffset = $('#tabs').position().top + 90;
            }
             else if($("#perc-pub-inline-help").length) {
                 bottomVerticalOffset = $('#perc-pub-inline-help').position().top + 90;
             }
        } else if(currentView === $.PercNavigationManager.VIEW_DASHBOARD) {
            bottomContentDiv     = $('.perc-body-background');
            bottomVerticalOffset = $('.perc-body-background').position().top;
        } else {
            return;
        }
    	
        if(bottomContentDiv.css('display')!=='none') {
            var wh, ww;
            if( window.innerHeight )
            {
                wh = window.innerHeight;
                ww = window.innerWidth; 
            }
            // for IE case
            else if( document.documentElement.clientHeight )
            {
                wh = document.documentElement.clientHeight - 4;
                ww = document.documentElement.clientWidth;    
            }
            bottomContentDiv.height(wh - bottomVerticalOffset);
        }
    
    };
    /**
     * Handles the Finder resize for pages that do not have the IFrames.
     */
    fixHeight = function(){
        
        var currentView = $.PercNavigationManager.getView();
        if( currentView === $.PercNavigationManager.VIEW_SITE_ARCH)
        {
            var percdropshadow = $('.perc-main');
            if(percdropshadow.length > 0)
            {
                var percdropshadowHeight = percdropshadow.height();    
                $('#perc_sa_container').css('margin-top', percdropshadowHeight + 3);
            }
        }

        // the resize event sets both the height and the width
        // but we want to leave the width auto so finder can grow/shrink when window is resized 
        $(".perc-finder").width("auto");
    };

})(jQuery);
