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
 * Contains functions to decorate the editor page.
 * 
 * Usage:
 * 
 *   widgets.perc_decorate( visible, actions, pageId )
 *
 *   Options/Settings:
 *   
 *      pageId:   The id for the page.
 *       
 *      visible:    If true all gridlines and menubars will always show,
 *                  If false only show on mouseover for that region or 
 *                  widget.
 *                   
 *      actions:  A list of actions, with associated icons, for the given widgets.
 */ 

(function($){

   $.fn.perc_decorate = function( visible, actions, pageId ) {
      this.each( function(){ 
                    if( $(this).is( '.perc-widget' ) ) {
                       decorate_generic( $(this), visible, actions, pageId, add_widget_dec, rem_widget_dec );
                    }
                    else if( $(this).is( '.perc-region' ) ) {
                       decorate_generic( $(this), visible, actions, pageId, add_region_dec, rem_region_dec );
                    }
            });
      return this;
   };

   function decorate_generic( el, visible, actions, pageId, add, rem ) {
      rem( el );
      el.unbind( '.decorate' );
      if( visible ) {
         add( el, actions, pageId );
      }
      else {
         el.bind('mouseenter.decorate', function(){ add( el, actions, pageId ); });
         el.bind('mouseleave.decorate', function(){ rem( el ); });
      }
   }

   function add_widget_dec( el, actions, pageId ) {
      el.addClass('perc-widget-visible-grid perc-widget-puff');
      el.append( mk_menu( el, actions, pageId ) );
   }
   function rem_widget_dec( el ) {
      el.removeClass('perc-widget-visible-grid perc-widget-puff');
      el.find( '.perc-widget-menubar' ).remove();
   }
   function add_region_dec( el) {
      el.addClass('perc-region-visible-grid');
      //el.sortable();
   }
   function rem_region_dec( el ) {
      el.removeClass('perc-region-visible-grid');
   }

   function mk_menu( widget, actions, pageId ) {
      var bar = $('<div class="perc-widget-menubar"><ul/></div>');
      var ul = bar.find('ul');
      $.each( actions, function() {
		  var icon = this.icon(),
		  action = this.action;
		  ul.append($("<li/>").append(icon));
		  icon.bind("click.decorate",function(){ 
                               action(widget.attr('assetid'), widget.attr('widgetid'), widget.attr('widgetdefid'), pageId); }); 
              } );
      return bar;
   }

})(jQuery);
