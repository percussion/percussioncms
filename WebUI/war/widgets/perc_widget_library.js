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
   Widget Library Widget
**/
(function($) {


//Creates a button to open/close the Widget Library.
$.perc_create_widget_library = function( btn ) {
    btn.off();
    btn.removeClass("ui-state-disabled").perc_button();
    function close_lib() {
	$("#perc-pageEditor-toolbar-content").empty();
	btn.removeClass("perc-widget-library-opened");
    }

    function mk_widget(w) {
       var wdg;
       wdg = $("<div/>") 
	    .css({'position': 'relative', 'width': '200px'})
	    .addClass("perc-widget")
	    .append($("<span/>").append(w.label).addClass("perc-widget-label") )
	    .append( $("<img src=\"" + w.icon + "\" alt=\"\"></img>") )
	    .attr('id',"widget-" + w.id + "-" + $('.perc-widget').length)
            .draggable({
                  appendTo: 'body',
                  helper: 'clone',
                  start: function() { $.perc_iframe_drag.start(wdg); },
                  stop: $.perc_iframe_drag.stop
                  });
       //wdg.find('span').textOverflow('...', true);
       wdg.data( 'widget', w );
       return wdg;
    }

    function mk_sortable_items( specs ){
	return $.map(specs.WidgetSummary, function(spec) {
		   var sortDiv = $("<div/>").append( mk_widget( spec ) ).css({ 'float': 'left' } );
                   return sortDiv; });
    }
    function open_lib() {
	$.getJSON($.perc_paths.WIDGETS_ALL, function(js) {
	    		region_tool='<div id="region-tool"><img src="../images/templates/perc-new-region-tool.gif"></div>';
				  $("#perc-pageEditor-toolbar-content").append($(region_tool));
		      $.each( mk_sortable_items(js), function() {
				  $("#perc-pageEditor-toolbar-content").append(this);
			      });
		       });
	btn.addClass( "perc-widget-library-opened" );
    }
    btn.on("click",function() {
		   if( btn.is( '.perc-widget-library-opened' ) )
		       close_lib();
		   else
		       open_lib(); 
	       } ); 
    return { open: open_lib, close: close_lib };
};



})(jQuery);

