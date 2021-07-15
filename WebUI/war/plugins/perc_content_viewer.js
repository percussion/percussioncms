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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * Content viewer plugin is responsible for handling the actions that are related to the content viewer. It also holds the page object.
 * 
 * Usage:
 * 
 *   var contentViewer = perc_content_viewer();
 *                  
 *    Functions:
 *    
 *    isPageDirty(): Returns true if the page is dirty otherwise false..
 *    setPageDirty(dirty):  Sets the page dirty flag to either true or false depending on the supplied flag.
 *    openPage(id): Opens the page with the supplied id. Warns the user if the current page is dirty.
 *    savePage(): Saves the current page that is displayed in the viewer.
 *    saveAsset(): Saves the asset object
 *    getPageId() : returns the current page id.
 *    previewPage() : Opens a window with the preview of the page
 *     
 */ 
(function($){

var ut = $.perc_utils;

$.perc_load_template = function( template_id, div ) {
   //load the template into the div; set widget drops. Content only.

   load_content();


    function set_widget_drops() {
       function set_drops( widget_ctypes ) {
          all_widgets().each( function(){
                var widgetid = $(this).attr('widgetid');//$(this).data('widget').widgetid;
                $(this).attr('id', widgetid );
                //console.log( $(this).data('widget') );
                var ctypes = widget_ctypes[widgetid];
                function acceptor(item) {
                   var ctype = item.data('spec') && item.data( 'spec' ).type;
                   return ctype && ctypes && ( ctype === ctypes || $.grep( ctypes, function(ct) { return ct === ctype; } ).length );
                }
                $(this).droppable('option', 'accept', acceptor );

                var widgetData = { 
                            widgetid: widgetid, 
                            widgetdefid: $(this).attr('widgetdefid'), 
                            widgetName: $(this).attr('widgetName')
                       };
                $(this).droppable('option', 'drop', function(evt, ui) {
                                     var assetid = ui.draggable.data('spec').id;
                                     if( assetid ) {
                                        $.PercAssetService.set_relationship( assetid, widgetData, template_id, "1", true,null,
                                           load_content,
                                           ut.show_error);
                                     }
                                  } );
             } );
       }

       $.perc_pagemanager.get_widget_ctypes( template_id, false, set_drops, ut.show_error );
    }

   function load_content() {
      //Load the template HTML using its id.
      $.perc_templatemanager.load_template( template_id, function(xml) {
            var root_region_id = $.xmlDOM( xml ).find( "rootRegion>children>region>regionId" ).text();
            $.perc_templatemanager.render_region( root_region_id, xml, 
                    function(data){ 
                       div.html( $(data).find('result').text() );

                             $.xmlDOM( xml ).find( 'widgetItem' ).each( function(){
                                                                     div.find("#" + $(this).find('id').text() ).data('widget', 
                                                                        {widgetid: $(this).find('id').text(),
                                                                         widgetdefid: $(this).find('definitionId').text()} );
                                                                  } );

                       after_load();
                    });
            });
   }

   function after_load() {
      var actions = [
        {
            icon : function() { return $("<a class='perc-widget-delete' href='#'><img src='"+ $.perc_paths.IMAGE_ROOT + "/icons/editor/delete.png'></img></div>"); },
            action : function(assetid, widgetid,  widgetdefid, id) { clear_asset(assetid, widgetid,  widgetdefid, id); }
        },
        { 
            icon : function() { return $("<a class='perc-widget-edit' href='#'><img src='" + $.perc_paths.IMAGE_ROOT + "/icons/editor/edit.png'></img></div>"); },
            action : function( assetid, widgetid, widgetdefid, id ) { $.perc_asset_edit_dialog(load_content, function(){},assetid, widgetid, widgetdefid, id, "template"); } 
        }
      ];
      all_widgets().perc_decorate(true, actions, template_id)
            .droppable({'accept': '#nothing', 'scope':$.perc_iframe_scope, activeClass: 'perc-widget-active', hoverClass: 'perc-widget-hover'});
      set_widget_drops();
   }

   function clear_asset( assetid, widgetid, widgetdefid, id ) {
      var awr = {"AssetWidgetRelationship":{"ownerId":id,"widgetId":widgetid,"widgetName":widgetdefid,"assetId":assetid,"assetOrder":"0"}};
      $.ajax({
                url: $.perc_paths.ASSET_WIDGET_REL_DEL + "/",
                dataType: "json",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify(awr),
                success: function(textstatus){
                   load_content();
                },
                error: function(request, textstatus, error){
                   var defaultMsg = 
                      $.PercServiceUtils.extractDefaultErrorMessage(request);
                   $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: error});
                }  
             });
   }

   function all_widgets() {
      return div.find( '.perc-widget' );
   }

   
};

$.perc_content_viewer = function(options){

   var settings = {};
   var pageDirty = false;
   var pageId = null;
   var pageName = null;
   var pageObject = null;
   var decorated = true;
   var actions = [];
   var widgetLib = null;
   var decorateCallback = ut.id;
   var currentView = null;
   var TAB_NAMES = {"CONTENT":"CONTENT","LAYOUT":"LAYOUT"};
   var defaults = {
   };
   $.extend(settings, defaults, options); 
   var content_viewer = {
          isPageDirty: _isPageDirty,
          setPageDirty: _setPageDirty,
          reload: reload,
          openPage: openPage,
          resetPageName: resetPageName,          
          savePage: _savePage,
          getPageId: _getPageId,
          previewPage: _previewPage,
	     switchToContent: _switchToContent,
	     switchToLayout: _switchToLayout,
          addWidgetAssociation: _addWidgetAssociation,
          confirm_if_dirty: confirm_if_dirty,
          clear: clear
      };
   return content_viewer;


   function confirm_if_dirty( k, err, opts ) {
      opts = opts || {};
      err = err || function(){};
      var settings = {
            id: 'perc-editor-page-dirty',
            title: I18N.message("perc.ui.content.viewer@Unsaved Changes"),
            question: I18N.message("perc.ui.content.viewer@Unsaved Changes Question"),
            success: k,
            cancel: err,
            yes: I18N.message("perc.ui.content.viewer@Continue Anyway")
      };
      $.extend( settings, opts );
      if( pageDirty ) {
         ut.confirm_dialog(settings);
      } else {
         //Page is not dirty, proceed
         k();
      }
   }

   function _getPageId(){
      return pageId;
   }
   function resetPageName(){
      $.ajax({
            url: $.perc_paths.PAGE_CREATE + "/" + _getPageId(),
            success: function(data){
              $("#perc-pageEditor-menu-name").html(data.Page.name);
            },
            type: 'GET',
            dataType: 'json'
      });
   
   }
   function _isPageDirty(){
      return pageDirty;
   }
   function _setPageDirty(dirty){
      pageDirty = dirty;
   }
   function _previewPage(){
      var pageId = _getPageId();
      if(! pageId)
      {
         alert(I18N.message("perc.ui.content.viewer@No Page Open"));
         return;
      }
      if (_isPageDirty()) {
           alert(I18N.message("perc.ui.content.viewer@Page Changed"));
 	       $.fn.jmodal({
 		    data: { innerText: I18N.message("perc.ui.content.viewer@Yes") },
                     title: I18N.message("perc.ui.content.viewer@Information"),
                     content: I18N.message("perc.ui.content.viewer@No Preview Available"),
                     buttonText: { button1: I18N.message("perc.ui.content.viewer@Yes Button"), button2: I18N.message("perc.ui.content.viewer@No Button"),button3: ''},
                     fixed: true,
                     button1Event: function(obj, args) {
                         alert(obj.innerText);
                         args.complete();
 			},
 		    button2Event: function(obj, args) {
                         alert(obj.innerText);
                         args.complete();
 			},
                     button3Event: function(obj, args) {
                         alert(obj.innerText);
                         args.complete();
 			}
                 });
            return;
         }
      var previewPath = $.perc_paths.PAGE_PREVIEW + pageId;
       
        /* IE doesn't accept dashes '-' as part of the window name.
            The 2nd param needs to be "" and not null because IE will not show
            any bars when null. Both IE and FF show the same header in the new
            window as the original by passing "" and follow the user's preference
            as whether to open in a tab or window. */
      window.open(previewPath, pageId.replace(/-/g, "_"), "", true);
   }
   function _editPageMetadata()
   {
      $.perc_page_edit_dialog(options.mcol, content_viewer, _getPageId()); 
   }
   /**
    * Retrieve the given page from the server, and render it in the content area.
    */

   function openPage(id, name) {
      confirm_if_dirty( function(){ _openPage(id,name); } );
   }

   function _openPage(id, name) {
       pageId = id;
       pageName = name;

       $("#perc-pageEditor-menu-name").html(name);
       // Hook up the show/hide button to the decorator that we have initialized.
       $("#perc-pageEditor-show-hide")
         .off("click").perc_button().removeClass("ui-state-disabled")
         .html( I18N.message( "perc.ui.webmgt.pageeditor.menu@Hide" ) )
         .on("click", function() {
                 decorated = !decorated;
                 var label = decorated ? I18N.message( "perc.ui.webmgt.pageeditor.menu@Hide" ): I18N.message( "perc.ui.webmgt.pageeditor.menu@Show" );
                 $(this).html( label );
                 redecorate(); } );
      // Enable the preview button
      $("#perc-metadata-button")
        .off("click").perc_button().removeClass("ui-meta-pre-disabled")
	    .on("click", function() { _editPageMetadata(); });

      // Enable the preview button
      $("#perc-preview-button")
        .off("click").perc_button().removeClass("ui-meta-pre-disabled")
	    .on("click", function() { _previewPage(); });

      var tabs = $('#perc-pageEditor-tabs');
      tabs.tabs('enable');
      tabs.tabs(  "option", "active", 0 );

      var dummy = false;
      tabs.tabs( "option", "active",
         function(event) {
            if( dummy ) {
               dummy = false;
               return true;
            }
            if( ui.index === 0 ) {
               confirm_if_dirty( function() { _switchToContent(); dummy = true; tabs.tabs( "option", "active", 0); }  );
               return false;
            }
            else {
               confirm_if_dirty( function() { _switchToLayout(); dummy = true; tabs.tabs( "option", "active", 1); } );
               return false;
            }
         });
      _switchToContent();
   }

   function clear() {
      pageId = null;
      pageName = null;

       $("#perc-pageEditor-menu-name").html("");
       $("#perc-pageEditor-show-hide")
         .off().addClass("ui-state-disabled");
      $("#perc-metadata-button")
        .off().addClass("ui-meta-pre-disabled");
      $("#perc-preview-button")
        .off().addClass("ui-meta-pre-disabled");

      var tabs = $('#perc-pageEditor-tabs');
      tabs.tabs('disable');
      if( $.perc_use_iframe ) {
         $( '#perc-pageEditor-content' ).contents().find('body').empty();
      } else {
         $( '#perc-pageEditor-content' ).empty();
      }
   }

   function _switchToContent() {
      if( widgetLib )
	     widgetLib.close();
      _toggleMenus(TAB_NAMES.CONTENT);
      decorateCallback = _decorateContent;
      $('#perc-content').append( $('#perc-pageEditor-content') );
      reload();
   }
   function _decorateContent()
   {
       $('#perc-widget-library-button').off().addClass('ui-state-disabled');


      actions = [
         {
             icon : function() { return $("<a class='perc-widget-delete' href='#'><img src='"+ $.perc_paths.IMAGE_ROOT + "/icons/editor/delete.png'></img></div>"); },
             action : function(assetid, widgetid,  widgetdefid, pageid) { _clearAssetFromPage(assetid, widgetid,  widgetdefid, pageid); }
         },
         { 
             icon : function() { return $("<a class='perc-widget-edit' href='#'><img src='" + $.perc_paths.IMAGE_ROOT + "/icons/editor/edit.png'></img></div>"); },
             action : function( assetid, widgetid, widgetdefid, pageid ) { $.perc_asset_edit_dialog(reload, function(){}, assetid, widgetid, widgetdefid, pageid); } 
         }
       ];
   }
   function _toggleMenus(currentTab)
   {
      if(currentTab === TAB_NAMES.CONTENT)
      {
         $("#perc-widget-library-button").hide();
         $("#perc-metadata-button").show();
      }
      else if(currentTab === TAB_NAMES.LAYOUT)
      {
         $("#perc-widget-library-button").show();
         $("#perc-metadata-button").hide();
      }
   }
   function _clearAssetFromPage(assetid, widgetid, widgetdefid, pageid)
   {
      var awr = {"AssetWidgetRelationship":{"ownerId":pageid,"widgetId":widgetid,"widgetName":widgetdefid,"assetId":assetid,"assetOrder":"0"},};
      $.ajax({
                url: $.perc_paths.ASSET_WIDGET_REL_DEL + "/",
                dataType: "json",
                contentType: "application/json",
                type: "POST",
                data: JSON.stringify(awr),
                success: function(data, textstatus){
                   reload();
                },
                error: function(request, textstatus, error){
                   alert(error);
                }  
             });
   }
   function _switchToLayout() {
      _toggleMenus(TAB_NAMES.LAYOUT);
      decorateCallback = _decorateLayout;
      $('#perc-layout').append( $('#perc-pageEditor-content') );
      reload();
   }
   function _decorateLayout()
   {
       widgetLib = $.perc_create_widget_library( $('#perc-widget-library-button') );

        actions = [
        {
            icon : function() { return $("<a href='#'><img src='"+ $.perc_paths.IMAGE_ROOT + "/icons/editor/delete.png'></img></div>"); },
            action : function(widgetid, pageid) { alert( I18N.message("perc.ui.content.viewer@Delete not yet supported") ); }
        },
        { 
            icon : function() { return $("<a href='#'><img src='"+$.perc_paths.IMAGE_ROOT+"/icons/editor/configure.png'></img></a>"); },
            action : function( widgetid, pageid ) 
            {
                alert( I18N.message("perc.ui.content.viewer@Configuration is not yet defined") );
            }
        }
      ];
   }   

   function reload()
   {
      var renderPath = $.perc_paths.PAGE_EDIT + "/" + pageId;
      if( $.perc_use_iframe ) {
         var styles = '<style>';
                         '.perc-region{ min-height: 50px; }' +
                         ' .perc-region-visible-grid{ border: 1px dotted #000000; } ' +
                         ' .perc-widget{ height: 100px; width: 100%; position: relative; } ' +
                         ' .perc-widget-visible-grid{ border: 1px dashed blue; } ' +
                         ' div.perc-region-hover { border: thick dotted green; } ' +
                         ' .perc-region-active { border: thick dashed green; } ' +
                         ' div.perc-widget-hover { background-color: blue; } ' +
                         ' .perc-widget-active { background-color: yellow; } ' +
                         ' .perc-widget-menubar{ position: absolute; right: 0; top: 0; } ' +
                         ' .perc-widget-menubar ul{ } ' +
                         ' .perc-widget-menubar li{ font-family: Arial; font-size: 10px; display: inline; padding: 1px; } ' +
                         ' .perc-widget-menubar li a{ cursor: pointer; }' +
                         '.perc-show-feedback { background-color: grey; border: 2px solid green; }' +
                         '</style>';

         if( $.browser.mozilla ) {
            var fr = $("#perc-pageEditor-content");
            fr.off( ".reload" );
            fr.on( "load.reload", function(){
                        fr.contents().find('body').append( $(styles) );
                        redecorate();
                        set_widget_drops();
                     });
            fr[0].src = renderPath;
         }
         else {
            function load_iframe( page ) {
               var fr = $("#perc-pageEditor-content");
               fr.contents().remove(); 
               var doc = fr.contents()[0];
               doc.open();
               doc.write( page );
               doc.close();
               fr.contents().find('body').append( $(styles) );
            }
            $.perc_pagemanager.render_page( _getPageId(), function(page) { load_iframe( page ); redecorate(); set_widget_drops();} );
         }
      }
      else {
         function get_content(page) {
            $("#perc-pageEditor-content").empty().append($(page)); 
         }
         $.perc_pagemanager.render_page( _getPageId(), function(page) { get_content(page); redecorate(); set_widget_drops();} );
      }
   }


    function redecorate() {
       decorateCallback();
       all_widgets()
          .perc_decorate( decorated, actions, pageId )
          .droppable({'accept': '#nothing', 'scope':$.perc_iframe_scope, activeClass: 'perc-widget-active', hoverClass: 'perc-widget-hover'});
       all_regions()
          .perc_decorate( decorated, actions, pageId )
          .droppable({'accept': '.perc-widget', 'scope':$.perc_iframe_scope, activeClass: 'perc-region-active', hoverClass: 'perc-region-hover',
                               drop: function(event, ui){ 
                                  //This is a temporary solution to add the widget to the page, Once page model has been created this needs to be moved there.
                                  if( ui.draggable.data( 'widget' ) ) {
                                     var wid = ui.draggable.data( 'widget' ).id;
                                     _addWidgetAssociation(wid, $(this));
                                  }
                               }});
    }

    function all_widgets() {
       if( $.perc_use_iframe ) {
          return $('#perc-pageEditor-content').contents().find('.perc-widget');
       }
       return $('#perc-pageEditor-content').find('.perc-widget');
    }

    function all_regions() {
       if( $.perc_use_iframe ) {
          return $('#perc-pageEditor-content').contents().find('.perc-region');
       }
       return $('#perc-pageEditor-content').find('.perc-region');
    }

    function set_widget_drops() {
       function set_drops( widget_ctypes ) {
          all_widgets().each( function(){
                var widgetid = $(this).attr('widgetid');
                var ctypes = widget_ctypes[widgetid];
                function acceptor(item) {
                   var ctype = item.data('spec') && item.data( 'spec' ).type;
                   return ctype && ctypes && ( ctype === ctypes || $.grep( ctypes, function(ct) { return ct === ctype; } ).length );
                }
                $(this).droppable('option', 'accept', acceptor );

                var widgetdefid = $(this).attr('widgetdefid');
                $(this).droppable('option', 'drop', function(evt, ui) {
                                     var assetid = ui.draggable.data('spec').id;
                                     if( assetid ) {
                                        $.PercAssetService.set_relationship( assetid, widgetid, widgetdefid, pageId, "1",
                                           function(){ content_viewer.reload(); },
                                           ut.show_error);
                                     }
                                  } );
             } );
       }

       $.perc_pagemanager.get_widget_ctypes( pageId, true, set_drops, ut.show_error );
    }

   

   function _addWidgetAssociation(wdgName, region) {
      var regionId = region.attr("id");
      $.ajax({
            url: $.perc_paths.PAGE_CREATE + "/" + _getPageId(),
            success: function(data){
                   pageObject = ut.unxml( $.perc_schemata.page, $(data) );
                   add_widget(wdgName, regionId, function() { reload(); });
            },
            type: 'GET',
            dataType: 'xml'
      });
   }


   //This is a temporary hack to add a widget to the page, 
   //This ocde needs to be replaced. Look for end mark also.
   function add_widget(wdgName, regionId, k ) {
       var assocs = pageObject.Page.regionBranches.regionWidgetAssociations;

       var region_assoc = $.grep( assocs, function(x) { return x.regionId == regionId; });
       if( region_assoc.length > 0 ) {
          region_assoc = region_assoc[0];
       }
       else {
          region_assoc = { regionId: regionId, widgetItems: [], _tagName:'regionWidget' };
          assocs.push( region_assoc );
       }
       region_assoc.widgetItems.push( { definitionId: wdgName, _tagName: 'widgetItem' } );
       
        $.ajax({
            url: $.perc_paths.PAGE_CREATE + "/",
            dataType: "json",
            contentType: "application/xml",
            type: "POST",
            data: ut.rexml($.perc_schemata.page, pageObject),
            success: k,
            error: function(error){
               alert(I18N.message("perc.ui.content.viewer@Add Widget Error")+wdgName+ I18N.message("perc.ui.content.viewer@Region") + regionId +".");
            }  
        });
    }
    //Temporary code ends
   
   function _savePage()
   {
      //Call the rest service with the page object to save it.
   }
   function _saveAssetItem()
   {
      //Call the rest service with the asset object to save it.
   }      
};
})(jQuery);
