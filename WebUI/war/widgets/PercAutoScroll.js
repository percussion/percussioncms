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

(function($) {
    var opts;
    var view;
    var NONE=-1,UP=0,DOWN=1,LEFT=2,RIGHT=3;
    var currentDirection = NONE;
    var SCROLLBAR_WIDTH = 20;
    var tagType;
    var iFrameWindow;
    var iFrameDocument;
    var iFrameWindowJquery;
    
    $.fn.percAutoScroll = function(options) {
        var defaults = {
            directions : 'n, s, e, w',
            offsetX : 0,
            offsetY : 0,
            width : 30,
            backgroundColor : 'blue',
            opacity : 0.5,
            speed : 10,
            debug : false,
            callback : function(){}
        };
        opts = $.extend(defaults, options);
        view = $(this);
        tagType = view[0].tagName;
        if(tagType==="IFRAME") {
            iFrameWindow = view[0].contentWindow;
            iFrameDocument = view[0].contentWindow.document;
            //iFrameWindow.onresize = createScrollDivs;
            iFrameWindowJquery = $(iFrameWindow);
        }
        
        createScrollDivs();
    };

    $.fn.percAutoScroll.remove = function() {
        
        $(".autoscroll").remove();
        clearInterval(interval);
    };

    function createScrollDivs() {
        $(".autoscroll").remove();
        
        opts.offsetY = view.position().top;

        var viewPosition = view.position();
        var viewY = 0;
        var viewX = viewPosition.left;
        var viewW = view.width();
        var viewH = view.height();

        if(opts.directions.indexOf('n')!=-1) {
            var scrollUpDiv    = createScrollDiv(UP,    viewX,opts.offsetY+viewY,viewW-SCROLLBAR_WIDTH, opts.width);
            $("body").append(scrollUpDiv);
        }

        if(opts.directions.indexOf('s')!=-1) {
            var scrollDownDiv  = createScrollDiv(DOWN,  viewX,opts.offsetY+viewY+viewH-SCROLLBAR_WIDTH-opts.width,viewW-SCROLLBAR_WIDTH, opts.width+SCROLLBAR_WIDTH);
            $("body").append(scrollDownDiv);
        }

        if(opts.directions.indexOf('w')!=-1) {
            var scrollLeftDiv  = createScrollDiv(LEFT,  viewX,opts.offsetY+viewY+opts.width,opts.width, viewH-SCROLLBAR_WIDTH-opts.width*2);
            $("body").append(scrollLeftDiv);
        }

        if(opts.directions.indexOf('e')!=-1) {
            var scrollRightDiv = createScrollDiv(RIGHT, viewX+viewW-SCROLLBAR_WIDTH-opts.width,opts.offsetY+viewY+opts.width,opts.width, viewH-SCROLLBAR_WIDTH-opts.width*2);
            $("body").append(scrollRightDiv);
        }
    }
    
    var interval;
    function createScrollDiv(direction, x,y,width, height) {
        var scrollDiv = $("<div class='autoscroll' style='z-index:500;'>")
            .width(width)
            .height(height)
            .css("opacity",opts.opacity)
            .css("position","absolute")
            .css("top",y)
            .css("left",x)
            .droppable({
                addClasses: false,
                tolerance : "pointer",
                over : function(){
                    currentDirection = direction;
                    interval = setInterval(function(){scrollView();},15);
                },
                out : function(){
                    currentDirection = NONE;
                    clearInterval(interval);
                },
                drop : function(){
                    currentDirection = NONE;
                    clearInterval(interval);
                }
            });
        if(opts.debug)
            scrollDiv.css("background-color",opts.backgroundColor)
        return scrollDiv;
    }
    function scrollView() {
        var x = 0;
        var y = 0;
        var scroller = view;
        var position = getViewScroll();
        var scrollTop  = position.top;
        var scrollLeft = position.left;
        switch(currentDirection){
            case UP:
                y = scrollTop-opts.speed;
                break;
            case DOWN:
                y = scrollTop+opts.speed;
                break;
            case LEFT:
                x = scrollLeft-opts.speed;
                break;
            case RIGHT:
                x = scrollLeft+opts.speed;
                break;
            default:
                clearInterval(interval);
        }
        setViewScroll(x,y);
        var newPosition = getViewScroll();
        var newScrollTop  = newPosition.top;
        var newScrollLeft = newPosition.left;

        $(this).percAutoScroll.postScrollView( newScrollLeft-scrollLeft, newScrollTop-scrollTop);
    }
    
    $.fn.percAutoScroll.postScrollView = function() {}    
    
    function getViewScroll() {
        var scrollTop;
        var scrollLeft;
        if(tagType == "IFRAME") {
            /*
            scrollTop  = iFrameDocument.body.scrollTop;
            scrollLeft = iFrameDocument.body.scrollLeft;
            */
            scrollTop  = iFrameWindowJquery.scrollTop();
            scrollLeft  = iFrameWindowJquery.scrollLeft();
        } else {
            scrollTop  = view.scrollTop();
            scrollLeft = view.scrollLeft();
        }
        return {top : scrollTop, left : scrollLeft};
    }
    
    function setViewScroll(x, y) {
        if(tagType == "IFRAME") {
            iFrameWindowJquery[0].scrollTo(x,y);
        } else {
            view.scrollTop(y);
            view.scrollLeft(x);
        }
    }
    
})(jQuery);
