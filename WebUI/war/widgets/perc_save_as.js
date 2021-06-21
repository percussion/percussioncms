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
 * This module defines the Browser, a component which is used to
 * navigate the directory structure and choose a folder or item within
 * it. It can be used either to open or to save a file, depending on
 * the options used.
 *
 * Schematic:
 *
    +--------------------------------------------+
    |                            +-----+ +-----+ |
    |  ~~~~~~~~~~~[1]            | [2] | | [3] | |
    |                            +-----+ +-----+ |
    +--------------------------------------------+
    |                                            |
    |                                            |
    |    ~~~~~~~~~~[4]                           |
    |    +-----------------+[5]                  |
    |    +-----------------+                     |
    |                                            |
    |    ~~~~~~~~~~[6]                           |
    |    +-----------------+[7]                  |
    |    | ~~~~~         |v|                     |
    |    +-----------------+                     |
    |                                            |
    |    +-------------------------+             |
    |    | # ~~~~ ~~~~~~[8]           |             |
    |    |                         |             |
    |    | # ~~~~~~~~~~            |             |
    |    |                         |             |
    |    | % ~~~~~~~~~~~~~~        |             |
    |    |                         |             |
    |    |                         |             |
    |    |                         |             |
    |    |                         |             |
    |    |                         |             |
    |    +-------------------------+             |
    |                                            |
    +--------------------------------------------+

   [1] - "Title". Can be set using the << title >> option

   [2] - "Cancel button". Closes the browser.

   [3] - "Save button". Could also mean 'select' or something
   else. The button class can be set with << save_class >> - if the
   class is perc-save (default), then a background image is given
   using CSS. If << save_class >> is false, an unstyled default of
   "OK" will be used.

   [4],[5] - "Asset name". This is a text field, which is labeled
   according to the option << asset_name >>. The text field is used to
   enter a new asset name if << new_asset_option >> is true. Otherwise
   it holds the currently selected item's name. Which items can be
   selected is set by << selectable_object >> to either "folder",
   "leaf", or "all." The asset name is initially set equal to 
   << initial_val >>.

   [6],[7] - "Location". Drop-down list of parent folders to the
   currently viewed folder. Labeled according to the << location >>
   option. This will go as far back as the root directory which has
   been set for the browser - this is set by the
   << displayed_containers >> option to be either "Assets", "Sites",
   or "All". Selecting an element in the dropdown jumps back to 
   that folder.

   [8] - "Listing". Click to select; double-click to open. Which
   listings appear is set by << selection_types >>, which is either a
   list of shown types or is a predicate on types (when the function
   returns true, the type will be shown).
 */
(function($)
{
    var ut = $.perc_utils;
    var sa_tabindex = 20;

    /**
     * Creates the top-level Save As dialog.
     */
    $.perc_browser = function(options)
    {
        var settings = {
            
            customRootSite : "",
            
            //are we creating a new asset?
            new_asset_option: true,
    
            //initial value of the asset name textfield
            initial_val : "",
            
            //function called on save
            on_save : function(){},
        
            //what can be selected (folder, leaf, or all)
            selectable_object : "folder",
        
            //is the new folder button active?
            new_folder_opt : true,
        
            //which folder is displayed as the top level or root directory?
            displayed_containers : "Assets",
        
            //which types of items show up and can be selected?
            selection_types : function(){ return true; } ,
        
            //labels
            asset_name: I18N.message( "perc.ui.saveasdialog.label@Save as" ),
            location: I18N.message( "perc.ui.saveasdialog.label@Location:" ),
            title: I18N.message( "perc.ui.saveasdialog.title@Save As" ),
            no_name_error: I18N.message( "perc.ui.saveasdialog.error@Name required" ),
            save_class: 'perc-save'
        };
    
        $.extend(settings, options);
    
        var type_filter = settings.selection_types;
    
        //If the type filter is a list, make it into a function which tests
        //for membership in that list. If a function has been passed in,
        //use that directly.
        if(  typeof  type_filter !== "function" )
        {
            type_filter = function(x)
            {
                return ut.elem( x, settings.selection_types );
            };
        }
    
        //Map option values for the << displayed_containers >> option into
        //path service paths.
        var root_paths = {
            "All" : [""],
            "Sites": ["",$.perc_paths.SITES_ROOT_NO_SLASH],
            "Assets": ["",$.perc_paths.ASSETS_ROOT_NO_SLASH],
            "CustomSite":["",$.perc_paths.SITES_ROOT_NO_SLASH, "<sitename>"]
        };
        
        var root_path = root_paths[settings.displayed_containers];
        if (typeof(root_path[2]) != "undefined")
        {
            root_path[2] = root_path[2].replace("<sitename>", settings.customRootSite);
        }
        
        var leaf_selectable = settings.selectable_object == "leaf" || settings.selectable_object == "all";
        var folder_selectable = settings.selectable_object == "folder" || settings.selectable_object == "all";
    
        var top = window.parent.parent.jQuery("<div>"

            //A field at the top of the dialog to hold error messages.
            + "<p id='perc-saveas-dialog-error' class='perc-field-error' />"

            //Text field for the name under which to save the asset
            + ut.input( settings.asset_name ,
                'asset-name', 'perc-saveas-dialog-assetname',
                sa_tabindex )

            //Selection box for the location drop-down, which provides a
            //list of parent directories.
            + ut.select( settings.location,
                'location', 'perc-saveas-dialog-location',
                sa_tabindex )

            //Space for the directory navigation.
            + "<div id='perc-saveas-dialog-direc'></div>"
            + "</div>"
        );
    
        var root_direc = top.find( '#perc-saveas-dialog-direc' );
    
        var path_select = top.find( '#perc-saveas-dialog-location' ).on("change",function()
        {
            //Navigate to the path selected in the drop-down.
            var new_path = this.value.split('/');
            set_path( new_path );
        });
        
        var asset_name = top.find( '#perc-saveas-dialog-assetname' );
        var current_path = null;
        var current_spec = null;
        var new_folder_callback = null;
    
        //Set the initial text of the asset_name textfield to the value that
        //was passed in by the function calling perc_browser
        asset_name.val( settings.initial_val );
    
        if( !settings.new_asset_option )
        {
            asset_name.attr('readonly','readonly');
        }
    
        top.perc_dialog(
        {
            modal : true,
            title: settings.title,
            resizable: false,
            width: 320,
            zIndex: 400000000,
            buttons: {},
            percButtons:    {
                "Save":    {
                    click: function()    {save_callback();},
                    id: 'perc-save-as-save'
                },
                "Cancel":    {
                    click: function()    {top.remove();},
                    id: 'perc-save-as-cancel'
                }
            },
            id: "perc-save-as-dialog"
        });//.appendTo(topBody);
    
        if( settings.new_folder_opt )
        {
            top.parent().find('#perc-saveas-dialog-new-folder').on("click", function(e)
            {
                new_folder_callback();
            });
        }
        else
        {
            top.parent().find('#perc-saveas-dialog-new-folder').remove();
        }
    
        set_path( root_path );
    
        function finishSelection( path )
        {
            top.remove();
            alert( path );
        }
    
        function set_path( path ) 
        {
            //Set the new folder callback to create a folder at the current path.
            new_folder_callback = create_folder_at( ut.acop( path ) );
        
            if( !settings.new_asset_option )
            {
                //If the asset name reflects the selected asset, it should be cleared
                //when the folder changes.
                asset_name.val( "" );
            }
        
            //Set the location dropdown based on the current path.
            set_path_select( ut.acop( path ) );
        
            //Populate the directory view
            populate_direc( ut.acop( path ) );
        }
    
        function populate_direc( path )
        {
            root_direc.empty();
            //TODO: show a loading animation.

            $.perc_pathmanager.open_path( path, true, addChildren, err );

            function addChildren(folder_spec)
            {
                $.each( folder_spec['PathItem'], function()
                {
                    if( type_filter( this['type'] )
                        && this['path'] != $.perc_paths.SEARCH_ROOT + "/"
                        && this['path'] != $.perc_paths.DESIGN_ROOT + "/" )
                    {
                        insert_item( root_direc, make_item( this ) );
                    }
                });
            }
        }
    
        function set_path_select( new_path )
		{
            var item_location = top.find('#perc-saveas-dialog-location');
            item_location.attr("size","2");
			path_select.empty();          
			var indent = "";
			current_path = [];
			for( var ii = 0; ii < root_path.length - 1; ii++ )
			{
				current_path.push( new_path.shift() );
			}
           
			while( new_path.length )
			{
				current_path.push( new_path.shift() );
				indent += "&nbsp;&nbsp;";               
				path_select.append(
				$("<option  style = 'color:#000' value='"+ current_path.join('/') +"'></option>")
					.append( indent + current_path[current_path.length-1] ) );
			}
			path_select.val( current_path.join('/') );
            item_location.attr("size","1");
		}
    
        function create_folder_at( path )
        {
            return function()
            {
                ut.prompt_dialog(
                {
                    title: I18N.message( "perc.ui.saveasdialog.title@New Folder" ),
                    question: I18N.message( "perc.ui.saveasdialog.text@New folder prompt" ),
                    yes: I18N.message( "perc.ui.common.label@Save" ),
                    no: I18N.message( "perc.ui.common.label@Cancel" ),
                    success: function( fname )
                    {
                        //Upon returning from the prompt dialog with a chosen
                        //folder name (fname), create a folder with that
                        //name at the current path.
                        $.perc_pathmanager.add_folder( ut.acat( path, [fname] ), function()
                        {
                            //After creating the folder, reopen the current
                            //path (this will reload the view, so that
                            //the new folder is seen).
                            set_path( ut.acop( path ) );
                        }, err );
                    }
                });
            };
        }
    
        function err(x)
        {
            //When there is an error, such as an AJAX error, put the text at the
            //top of the save as dialog and call attention to it by a pulsating
            //effect.
            $("#perc-saveas-dialog-error").text( x ).effect('pulsate', {times: 1});
        }
        function save_callback()
        {
            var aname = asset_name.val();
            if( !aname || aname == "" )
            {
                err(settings.no_name_error);
                return;
            }
            
            //If the name is present, call the onSave callback with the current path
            //and the selected asset name. The third and fourth arguments allow the
            //calling function to do further validation on the result: if the
            //validation fails, err is called to alert the user; if validation
            //succeeds, the third function is called to remove the save as dialog.
            if( !settings.new_asset_option )
            {
                settings.on_save( current_spec, function(){ top.remove(); }, err );
            }
            else
            {
                settings.on_save( ut.acop( current_path ), aname, function() { top.remove(); }, err );
            }
        }
    
        function make_item( spec )
        {
            var path_end = ut.extract_path_end( spec['path'] );
            var pref = (spec['type'] == 'Folder') ? 'a' : 'z';
            var item_path = ut.extract_path( spec['path'] );
            var icon = ut.choose_icon( spec['type'], spec['icon'],    item_path  );
            var anchor = $("<a href='#'/>")
                .addClass('mcol-listing')
                .attr('id',"perc-saveas-dialog-listing"+ ut.path_id( item_path ))
                .append($("<img src='"+ icon.src +"' style='float:left' alt='"+ icon.alt + "' title='" + icon.title + "' aria-hidden='" + icon.decorative + "' />" ))
                .append( spec[ 'name' ] )
                .data( 'name', path_end )
                .data( 'tag', pref + (spec['name'] + "").toLowerCase() );

            var sclass = 'perc-saveas-dialog-selected';
            function selectAnchor(evt)
            {
                anchor.siblings( '.'+sclass ).removeClass( sclass );
                anchor.addClass( sclass );
                if( spec['leaf'] )
                {
                    asset_name.val( spec['name'] );
                    current_spec = spec;
                }
                else
                {
                    current_path = ut.acop( item_path );
                }
            }
            if( spec['leaf'] )
            {
                if( leaf_selectable )
                {
                    anchor.on("click",  function(evt){
                        selectAnchor(evt);
                    } );
                    anchor.on("dblclick", function(evt)
                    {
                        anchor.trigger("click");
                        $("#perc-saveas-dialog-save-button").trigger("click");
                    });
                }
            }
            else
            {
                if( folder_selectable )
                {
                    anchor.on("click", function(evt){
                        selectAnchor(evt);
                    } );
                }
                anchor.on("dblclick", function(evt)
                {
                    set_path( item_path );
                });
            }
            return anchor;
        }
    };
    
    
    function insert_item( container, item )
    {
        var next_child = null;
        container.prepend( item );
        
        container.children().each( function()
        {
            if( $.data( this, 'tag' ) > $.data( item.get(0), 'tag' ) ) {
            next_child = this;
            return false;
            }
            return true;
        });
        
        if( next_child )
        {
            $(next_child).before( item );
        }
        else
        {
            container.append( item );
        }
    }
    
})(jQuery);
