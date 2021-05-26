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
 * PercContentBrowserWidget.js
 * 
 * Author: Jose Annunziato
 * Date: 02/05/2010
 * 
 * Creates the top-level Save As dialog.
 * 
 * See test_PercAssetBrowserWidget.jsp for all dependent files.
 */
(function($)
{
    var ut = $.perc_utils;
    var sa_tabindex = 20;

    /**
     * Creates the top-level Save As dialog.
     */
    $.PercAssetBrowserWidget = function(options)
    {
        var settings = {
			siteIcon : "",
        	//callback registered for click notifications
        	on_click : function(){},
        	
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
            location: I18N.message( "perc.ui.saveasdialog.label@Location" ),
            title: I18N.message( "perc.ui.saveasdialog.title@Save As" ),
            no_name_error: I18N.message( "perc.ui.saveasdialog.error@Name required" ),
            save_class: 'perc-save'
        };
    
        $.extend(settings, options);
    
        var type_filter = settings.selection_types;
    
        //If the type filter is a list, make it into a function which tests
        //for membership in that list. If a function has been passed in,
        //use that directly.
        if( typeof type_filter !== "function" )
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
            "Assets": ["",$.perc_paths.ASSETS_ROOT_NO_SLASH]
        };
        
        var root_path = root_paths[settings.displayed_containers];
    
        var leaf_selectable = settings.selectable_object == "leaf" || settings.selectable_object == "all";
        var folder_selectable = settings.selectable_object == "folder" || settings.selectable_object == "all";
    
        var top = jQuery("<div>"
            //Selection box for the location drop-down, which provides a
            //list of parent directories.
            
            
            + (settings.new_asset_option?
                "<select name='location' id='perc-asset-browser-location-dropdown'></select>":
                "<select name='location' id='perc-asset-browser-location-dropdown' ></select>")
            + (settings.new_asset_option?
                "<input type='button' id='perc-new-folder-button' value=' + '>":"")
            //Space for the directory navigation.
            + "<div id='perc-asset-browser-dialog-direc'></div>"
            + "</div>"
        );
        
        $("#"+options.placeHolder).append(top);
    
        var root_direc = top.find( '#perc-asset-browser-dialog-direc' );
    
        var path_select = top.find( '#perc-asset-browser-location-dropdown' ).on("change",function()
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
    
//        alert(settings.new_folder_opt);
        if( settings.new_folder_opt )
        {
            top.parent().find('#perc-new-folder-button').on("click",function()
            {
                new_folder_callback();
            });
        }
        else
        {
            top.parent().find('#perc-new-folder-button').remove();
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
                    if( type_filter( this['type'] ) )
                    {
                        insert_item( root_direc, make_item( this ) );
                    }
                });
            }
        }
    
        function set_path_select( new_path )
        {
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
                $("<option value='"+ current_path.join('/') +"'></option>")
                    .append( indent + current_path[current_path.length-1] ) );
            }
            path_select.val( current_path.join('/') );
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
    
        function err(errorMessage)
        {
            alert(errorMessage);
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
            var item_path = ut.extract_path( spec['path'] );
            var icon = ut.choose_icon( spec['type'], spec['icon'],    item_path  );
			
			if(settings.siteIcon != "" && spec['type'] == "site")
				icon = settings.siteIcon;
			
            var anchor = $("<a href='#'/>")
            
            //    JGA { 1/7/2010 : removed dependency from perc_mcol.css
            //    .addClass('mcol-listing')
                .addClass('perc-saveas-dialog-listing')
            //    } JGA
                .attr('id',"perc-saveas-dialog-listing"+ ut.path_id( item_path ))
                .append($("<img src='"+ icon.src +"' style='float:left' alt='"+ icon.alt + "' title='" + icon.title + "' aria-hidden='" + icon.decorative + "' />" ))
                .append( spec[ 'name' ] )
                .data( 'name', path_end )
                .data( 'tag', spec['name'] );

            var sclass = 'perc-saveas-dialog-selected';
            function selectAnchor()
            {
            	// JGA { 1/11/2010
            	// notify registered function of selection
            	settings.on_click(spec);
            	// } JGA
                anchor.siblings( '.'+sclass ).removeClass( sclass );
                anchor.addClass( sclass );
                if( spec.leaf )
                {
                    asset_name.val( spec['name'] );
                    current_spec = spec;
                }
                else
                {
                    current_path = ut.acop( item_path );
                }
            }
            if( spec.leaf )
            {
                if( leaf_selectable )
                {
                    anchor.on("click", selectAnchor );
                    anchor.on("dblclick", function()
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
                    anchor.trigger("click", selectAnchor );
                }
                anchor.on("dblclick", function()
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
            if( $.data( this, 'tag' ) > $.data( item.get(0), 'tag' ) )
            {
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
