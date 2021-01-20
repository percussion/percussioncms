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
 * 
 * JGA : NOTE !!!!
 * JGA : This file has been deprecated. It has been merged into PercAssetService.js 
 * @deprecated
 * 
 * define the assetmanager functions, to interface with services on the server side.
 */


(function($){

    function add_asset_to_folder( assetid, folderid, k, err ) {
       $.ajax({
             url: $.perc_paths.ASSET_ADD_TO_FOLDER + "/" + folderid + "/" + assetid, 
             type: 'GET',
             success: k,
             error: err });
    }

    function set_asset_relationship( assetid, widgetid, widgetdefid, pageid, assetOrder, isResource, folderPath, k, err ) {
         var resType = isResource?"shared":"local";
         var awr = {"AssetWidgetRelationship":{"ownerId":pageid,"widgetId":widgetid,"widgetName":widgetdefid,"assetId":assetid,"assetOrder":"0","resourceType":resType}};
         if(folderPath)
            awr.AssetWidgetRelationship.folderPath = folderPath;
         $.ajax({
                   url: $.perc_paths.ASSET_WIDGET_REL + "/",
                   dataType: "json",
                   contentType: "application/json",
                   type: "POST",
                   data: JSON.stringify(awr),
                   success: k,
                   error: err });
    }

    function clear_asset(ownerId, widgetId, widgetDefinitionId, assetId, callback ) {
       callback = callback || function(){};
       var awr = {"AssetWidgetRelationship":{"ownerId":ownerId,"widgetId":widgetId,"widgetName":widgetDefinitionId,"assetId":assetId,"assetOrder":"0"}};
       $.ajax({
             url: $.perc_paths.ASSET_WIDGET_REL_DEL + "/",
             dataType: "json",
             contentType: "application/json",
             type: "POST",
             data: JSON.stringify(awr),
             success: callback,
             error: function(request, textstatus, error){
                   alert("error");
             }  
       });
    }

    $.perc_assetmanager = {
       clear_asset : clear_asset,
       add_asset_to_folder : add_asset_to_folder,
       set_relationship: set_asset_relationship
    };

 })(jQuery);
