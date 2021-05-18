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
 * Define the pathmanager functions.
 */
(function($){

var ut = $.perc_utils;

/**
 * If 'path' is a leaf, run if_leaf on its summary JSON; if it is a
 * folder, run if_folder; if any call fails, run if_error.
 * 
 * This version does some pre-processing on the returned contents.
 */
function is_leaf( path, if_leaf, if_folder, if_error ) {

    _is_leaf( path, function(spec) { if_leaf( spec['PathItem'] ); }, prepare_folder_items( if_folder ), if_error );

    function prepare_folder_items( callback ) {
    return function( pathitems ) {
        var specs = $.map( pathitems['PathItem'], function(s) {
                var paths = s.path.split('/');
                if( paths[paths.length - 1] === "" ) {
                    paths.pop();
                }

                s.path = $.map( paths, function(p) { return p; });
                s.path_component = paths[ paths.length - 1 ];
                return s;
        });
        specs = $.grep( specs, function(s) { return s['type'] !== 'percNavTree' && s['name'] !== '.system'; } );
        callback( specs ); 
    };
    }
}

function _is_leaf(path, if_leaf, if_folder, if_error ) {
    if( path.length <= 2 ) {
    open_path(path, true, if_folder, if_error);
    }
    else {
    var pathclone = $.map( path, function(x){ return x; } );
    var path_end = pathclone.pop();
    function check_results(specs) {
        var it = $.grep( specs['PathItem'], function( p ) { 
                 var path_components = p.path.split('/');
                 var e = path_components.pop();
                 if( e === "" )
                     e = path_components.pop();
                 return e === path_end;
                 } );
        if( it.length === 0 ) {
          if_error( I18N.message("perc.ui.path.manager@Item Not Found") ); //I18N
        }
        else {
        if( it[0].leaf === true ) {
            open_path( path, false, if_leaf, if_error );
        }
        else {
            open_path( path, true, if_folder, if_error );
        }
        }
    }

    open_path( pathclone, true, check_results, if_error);
    }
}


function get_site_id( path, k, err ) {
    //XXX get this from id.
    k( path[1] );
}

function get_folder_path( path, k, err ) {
   open_path( path, false, function(pathItem) { 
         k( pathItem['PathItem']['folderPath'] );
          }, err );
}


/**
 * Decide if the path is a leaf; if it is, open its parent folder; otherwise, open the folder itself.
 */
function open_containing_folder( path, k, err ) {
    var pathclone = $.map( path, function(x) { return x; } );
    _is_leaf( path, 
          function() {
          pathclone.pop();
          open_path( pathclone, true, function( spec ) { k( spec, pathclone ); }, err );
          },
          function(spec) {
          k(spec, pathclone);
          },
          err);
}

function add_folder( path, k, err ) {
    var path_str = path.join('/') + '/';
    var parent = $.perc_utils.acop( path );
    var nm = parent.pop();
    open_path( parent, true, function( folder_spec ) {
               var matches = $.grep( folder_spec['PathItem'], function(fs) {
                   return fs['name'] === nm;
               });
               if( matches.length > 0 ) {
               err( I18N.message( "perc.ui.saveasdialog.error@Duplicate folder" ) );
               }
               else {
               path_str = $.perc_utils.encodeURL(path_str);
               $.ajax( {
                   type: 'GET',
                   success: k,
                   error: err,
                   url: $.perc_paths.PATH_ADD_FOLDER + path_str,
                   dataType: 'json'
                   });
               }
           }, err );
        
}

/**
 * Open path, sending the response directly to callback; if the call fails
 * call err. folder determines if it is opened as a folder or an item.
 * Added the last param to retrieve paged results
 */
function open_path( path, folder, callback, err, paging ) {
    var path_str;
    var serviceUrl;
    //Check if we need paged results and change the service URL.
    if (paging){
        path_str = path;
        serviceUrl = $.perc_paths.PATH_PAGINATED_FOLDER + path_str;
    }
    else{
        path_str = $.perc_utils.encodeURL(path.join("/") + "/");
        serviceUrl = $.perc_paths.PATH_FOLDER + path_str;
    }

    if( ! $.perc_fakes.path_service ) {
        if( folder ) {
            $.ajax( {
                type: 'GET',
                success: callback,
                error:  function(request, textstatus, error){
                            if(request.status !== 0)
                                err(I18N.message("perc.ui.path.manager@No Path")+path_str);
                        },
                url: serviceUrl, 
                dataType: 'json',
                cache: false
                });
        }
        else {        
            $.ajax( {
                type: 'GET',
                success: callback,
                error:  function(request, textstatus, error){
                            if(request.status !== 0)
                                err(I18N.message("perc.ui.path.manager@No Path")+path_str);
                        },
                url: $.perc_paths.PATH_ITEM + path_str,
                dataType: 'json',
                cache: false
                });
        }
    }
    else {
        var fakes = {
            '/' : {"PathItem":[{"name":"Sites","leaf":"false","path":"\/Sites\/"}]},
            '/Sites/' : {"PathItem":[{"name":"Test","leaf":"false","path":"\/Sites\/Test\/"}, 
                        {"name":"TestTwo","leaf":"false","path":"\/Sites\/TestTwo\/"}]},
            '/Sites/Test/' : {"PathItem":[{"name":"Test","leaf":"true","path":"\/Sites\/Test\/Test\/"}]},
            '/Sites/Test/Test/' : {"PathItem":{"name":"Test","leaf":"true","path":"\/Sites\/Test\/Test\/"}},
            '/Sites/TestTwo/' : {"PathItem":[{"name":"TestTwo","leaf":"true","path":"\/Sites\/TestTwo\/TestTwo\/"}]},
            '/Sites/TestTwo/TestTwo/' : {"PathItem":{"name":"TestTwo","leaf":"true","path":"\/Sites\/TestTwo\/TestTwo\/"}} };
    
        callback( fakes[ path_str ] );
    }
}

/**
 * Makes an AJAX call to the server and gets the item properties and calls the supplied call back function with this object.
 * {"ItemProperties":{"name":"Home Page","status":"Live", "lastAccessedBy":"Some User", "lastAccessedDate": "2010/01/22:10:30AM"}}
 * @param path The full path of the item for which the properties are needed.
 * @param callback The callback function that gets called with true and ItemProperties object if succeeds, otherwise falls.
 * Shows the error message in case of error.  
 */
function getItemProperties(path, callback)
{
   /** Testing code
   var itemProps = {"ItemProperties":{"name":"Home Page", "status":"Live", "lastAccessedBy":"Some User", "lastAccessedDate": "2010/01/22:10:30AM"}};
   callback(true, itemProps);
   */
   var successCallback = function(data){
        callback(true, data.ItemProperties);
    };
   var errorCallback = function(request, status, error){
        callback(false,$.PercServiceUtils.extractDefaultErrorMessage(request));
   };
   var path_str = $.perc_utils.encodeURL(path);
   
   $.ajax( {
      type: 'GET',
      success: successCallback,
      error: errorCallback,
      url: $.perc_paths.PATH_ITEM_PROPERTIES + path_str,
      dataType: 'json',
      cache: false
   });
}     

$.perc_pathmanager = {
    'is_leaf' : is_leaf,
    'open_path' : open_path,
    'get_folder_path' : get_folder_path,
    'get_site_id' :  get_site_id,
    'open_containing_folder' : open_containing_folder,
    'add_folder' : add_folder,
    'getItemProperties' : getItemProperties 
};

})(jQuery);