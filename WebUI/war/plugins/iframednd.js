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

(function($)
{
    $.perc_iframe_fix = function( frame )
    {
        //special scope for droppables which are inside the iframe, so that
        //they do not have spurious interactions with draggables outside the
        //iframe
        $.perc_iframe_scope = 'perc-iframe-scope';
    
        var dragging = false;



        //Create an invisible div to put over the iframe
        var overlay = $("<div class=\"perc-iframe-overlay-dnd-container\"/>");
        $('body').append( overlay );
        
        overlay
            .height( frame.height() )
            .width( frame.width() )
            .addClass('ui-layout-ignore')
            .css(
            {
                overflow: 'hidden',
                position: 'absolute', 
                left: '-10000px',
                top: '0px',
                zIndex: 1000
            });
            
        addDragSupportDroppable();

        //Move the div over the iframe
        function addOverlay()
        {
            overlay.css({ left: frame.position().left, top: frame.position().top  });
            overlay.height( frame.height() );
            overlay.width( frame.width() );
        }

        //Move the div back offscreen
        function removeOverlay()
        {
            overlay.css({ left: '-10000px', top: '0px' });
        }

        //Add droppable targets to the overlay div - this allows draggables outside the iframe to communicate with
        //droppables inside the iframe
        function liftDroppables( )
        {
            var droppables = frame.contents().find( ':data(droppable)' );

            droppables.each( function()
            {
                var orig = $(this);
                var orig_drop = $.data( this, 'droppable' );
                var clone = $("<div/>").addClass("allDroppablesHelpers").addClass("perc-iframe-dnd-overlay-droppable").attr("for", orig.attr("id")).width( orig.outerWidth() ).height( orig.outerHeight() );
                overlay.append( clone );
                var iframeLeft, iframeTop;
                var fr = frame;
                if( $.browser.msie )
                {
                    //Of *course* the scroll offsets would be in frame.contentWindow.document.documentElement - where else???
                    var contentWindow = fr[0].contentWindow;
                    var documentElement = contentWindow.document.documentElement;
                    iframeLeft = documentElement.scrollLeft;
                    iframeTop = documentElement.scrollTop;
                }
                else
                {
                    //Oh, you crazy other browsers, what a pathetically obvious place to put your scroll offsets!
                    iframeLeft = frame[0].contentWindow.scrollX;
                    iframeTop = frame[0].contentWindow.scrollY;
                }
                var left = $(this).offset().left - iframeLeft;
                var top = $(this).offset().top - iframeTop;
                if( $.browser.mozilla || $.browser.safari )
                {
                    //Fix offsets for scrolled window
                    left -= window.scrollX;
                    top -= window.scrollY;
                }
                clone.css( { position: 'absolute', left: left + "px", top: top + "px" } );

                //Make the clone droppable, with event functions which
                //call through to the original droppable's events
                clone.droppable(
                { 
                    greedy: orig_drop.options.greedy,
                    tolerance: orig_drop.options.tolerance,
                    accept: orig_drop.options.accept,
                    over: function(evt){ orig_drop._over.call(orig_drop, evt); },
                    activate: function(evt){ orig_drop._activate.call(orig_drop, evt); },
                    deactivate: function(evt){ orig_drop._deactivate.call(orig_drop, evt); },
                    out: function(evt){ orig_drop._out.call(orig_drop, evt); },
                    drop: function(evt){ orig_drop._drop.call( orig_drop, evt); }
                });
            });
        }

        //Get rid of the added droppables.
        function removeDroppables()
        {
            overlay.empty();
        }

        function startDrag()
        {
            if( !dragging )
            {
                dragging = true;
                addOverlay();
                liftDroppables();
            }
        }

        function stopDrag()
        {
            removeOverlay();
            removeDroppables();
            dragging = false;
        }

        function addDragSupportDroppable()
        {
            var d = $("<div/>")
	            .addClass('ui-layout-ignore')
	            .droppable({
                    addClasses: false,
	                activate: function(event,ui)
	                {
	                    startDrag(ui.draggable);
	                }, 
	                deactivate: function()
	                {
	                    setTimeout( stopDrag, 100 );
	                }
	            });
            //.css({'position':'absolute', 'left':-1000});
            $('body').append(d);
        }

        //If a draggable needs to drag onto the iframe, it must call
        //startDrag() when dragging starts, end stopDrag() when dragging
        //stops

	};
})(jQuery);
