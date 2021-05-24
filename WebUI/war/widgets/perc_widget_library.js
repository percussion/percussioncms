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
	    .append($("<span/>").append(w['label']).addClass("perc-widget-label") )
	    .append( $("<img src=\"" + w['icon'] + "\" alt=\"\"></img>") )
	    .attr('id',"widget-" + w['id'] + "-" + $('.perc-widget').length)
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
	return $.map(specs['WidgetSummary'], function(spec) {
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

