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
    $.perc_iframe_fix = function( frame )
    {
        //special scope for droppables which are inside the iframe, so that
        //they do not have spurious interactions with draggables outside the
        //iframe
        $.perc_iframe_scope = 'perc-iframe-scope';
        $.dragDelay = 10;
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
            var droppables = frame.contents().find( ':data(ui-droppable)' );

            droppables.each( function()
            {
                var orig = $(this);
                var orig_drop = $.data( this, 'ui-droppable' );
                orig_drop.options.disabled=true;
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
                        iframeFix: true,
                        scope: orig_drop.options.scope,
                        over: function(evt,ui){
                            evt.preventDefault();
                            orig_drop._over.call(orig_drop, [evt,ui]);
                        },
                        activate: function(evt,ui){
                            evt.preventDefault();
                            orig_drop._activate.call(orig_drop, [evt,ui]);
                        },
                        deactivate: function(evt,ui){
                            evt.preventDefault();
                            orig_drop._deactivate.call(orig_drop, [evt,ui]);
                        },
                        out: function(evt,ui){
                            evt.preventDefault();
                            orig_drop._out.call(orig_drop, [evt,ui]);
                        },
                        drop: function(evt,ui){
                            evt.preventDefault();
                            orig_drop._drop.call( orig_drop,[evt,ui]);
                        }
                    });
            });
        }

        //Get rid of the added droppables.
        function removeDroppables()
        {
            overlay.empty();
            var droppables = frame.contents().find( ':data(ui-droppable)' );

            droppables.each( function() {
                var orig = $(this);
                var orig_drop = $.data(this, 'ui-droppable');
                orig_drop.options.disabled = false;
            });
        }

        function onDragStart()
        {
            if( !dragging )
            {
                dragging = true;
                addOverlay();
                liftDroppables();
            }
        }

        function onDragStop()
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
                    scope: $.perc_iframe_scope,
                    tolerance : 'pointer',
                    iframeFix: true,
                    activate: function(event,ui)
                    {
                        onDragStart(ui.draggable);
                    },
                    deactivate: function(event,ui)
                    {
                        setTimeout( onDragStop, 100 );
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
