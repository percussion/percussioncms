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
      el.off( '.decorate' );
      if( visible ) {
         add( el, actions, pageId );
      }
      else {
         el.on('mouseenter.decorate', function(){ add( el, actions, pageId ); });
         el.on('mouseleave.decorate', function(){ rem( el ); });
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
		  icon.on("click.decorate",function(){
                               action(widget.attr('assetid'), widget.attr('widgetid'), widget.attr('widgetdefid'), pageId); }); 
              } );
      return bar;
   }

})(jQuery);
