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
             error: function(){
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
