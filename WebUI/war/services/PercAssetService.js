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

(function($)
{
    $.PercAssetService = {
        getAssetEditorForAssetId: getAssetEditorForAssetId,
        getAssetViewForAssetId: getAssetViewForAssetId,
        getAssetEditorLibrary   : getAssetEditorLibrary,
        getAssetEditor          : getAssetEditor,
        getAssetEditorForWidgetAndFolder:getAssetEditorForWidgetAndFolder,
        putAssetInFolder        : putAssetInFolder,
        clear_asset             : clear_asset,
        clear_orphan_assets     : clear_orphan_assets,
        set_relationship        : set_asset_relationship,
        update_relationship     : update_asset_relationship,
        asset_from_local_content : asset_from_local_content,
        deleteAsset             : deleteAsset,
        forceDeleteAsset        : forceDeleteAsset,
        validateDeleteAsset     : validateDeleteAsset,
        updateAsset             : updateAsset,
        getUnusedAssets         : getUnusedAssets,
        promoteAsset            : promoteAsset,
        getAssetTypes           : getAssetTypes
    };

    /**
     * Get an editor for an asset
     * @param assetId of the asset we want to edit with the editor URL we are asking for.
     * The URL accesses a form that already knows about the asset and how to edit it by submitting the form. 
     * @type string
     * 
     */
    function getAssetEditorForAssetId(assetId, callback) {
        $.ajax({
             url: $.perc_paths.ASSET_EDITOR_URL_FOR_ASSET_ID + "/" + assetId,
             success: function(data) {

              callback($.PercServiceUtils.STATUS_SUCCESS, data);
            }, 
              error: function(request, textstatus, error){

               var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
               callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              
            }
        });
    }
    
    /**
     * Get read only view url for an asset.
     * @param assetId of the asset we want to view with the view URL.
     * @return the URL string 
     * @type string
     * 
     */
    function getAssetViewForAssetId(assetId, callback) {
        $.ajax({
             url: $.perc_paths.ASSET_VIEW_URL_FOR_ASSET_ID + "/" + assetId,
             success: function(data){

              callback($.PercServiceUtils.STATUS_SUCCESS, data);
            }, 
              error: function(request, textstatus, error){

               var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(request);
               callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
              
            }
        });
    }

    /**
     * Get a list of AssetEditors.
     * @param callback function to be called when library is retrieved
     * return JSON object has the following format:
     * 
     * {"AssetEditor":[
     *  { "icon":"/rx_resources/widgets/simpleList/images/theIconImage.png",
     *    "title":"The Editor Label",
     *    "url":"/Rhythmyx/psx_cepercSimpleAutoList/theEditorPage.html?sys_command=edit&sys_view=sys_HiddenFields:"},
     *    "workflowId":4,
     *    "contentType":"AssetContentType"
     *  }
     * ...
     * ]}
     * 
     */
    function getAssetEditorLibrary(currentFolderPath, callback)
    {
        getAssetEditors(currentFolderPath, "", callback);
    }
    function getAssetEditors(currentFolderPath, widgetId, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_EDITOR_LIBRARY + currentFolderPath + "?filterDisabledWidgets=yes&widgetId=" + widgetId,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    /**
     * Get an asset editor for the supplied widgetId
     * @param callback function to be called when library is retrieved
     * return JSON object has the following format:
     * 
     * {"AssetEditor":[
     *  { "icon":"/rx_resources/widgets/simpleList/images/theIconImage.png",
     *    "title":"The Editor Label",
     *    "url":"/Rhythmyx/psx_cepercSimpleAutoList/theEditorPage.html?sys_command=edit&sys_view=sys_HiddenFields:"},
     *    "workflowId":4,
     *    "contentType":"AssetContentType"
     *  }
     * ...
     * ]}
     * 
     */
    function getAssetEditor(widgetId, assetFolderPath, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_EDITOR + "/" + widgetId + "?parentFolderPath=" + assetFolderPath,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
    /**
     * Get an asset editor for the supplied widgetId and folder path.
     * @param callback function to be called when library is retrieved
     * return JSON object has the following format:
     * 
     * {"AssetEditor":[
     *  { "icon":"/rx_resources/widgets/simpleList/images/theIconImage.png",
     *    "title":"The Editor Label",
     *    "url":"/Rhythmyx/psx_cepercSimpleAutoList/theEditorPage.html?sys_command=edit&sys_view=sys_HiddenFields:"},
     *    "workflowId":4,
     *    "contentType":"AssetContentType"
     *  }
     * ...
     * ]}
     * 
     */
    function getAssetEditorForWidgetAndFolder(folderPath, widgetId, callback)
    {
        getAssetEditors(folderPath, widgetId, callback);
    }

    /**
     * Put an asset in a folder (virtual)
     * @param assetFolderRelationship JSON object containing association of asset id and the folder path.
     * assetFolderRelationship has the following schema:
     * 
     * {"AssetFolderRelationship" : {"assetId" : assetId,    "folderPath" : folderPath}}
     * 
     */
    function putAssetInFolder(assetFolderRelationship, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_ADD_TO_FOLDER,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            assetFolderRelationship
        );
    }

    /**
     * Deletes an asset.  The asset will be validated to ensure it is safe to delete.
     * @param id of the asset we want to delete.
     * @param callback handles success.
     * @param errorCallBack handles errors and validation warnings.
     */
    function deleteAsset(id, callback, errorCallBack)
    {
        $.ajax({
            url     : $.perc_paths.ASSET_DELETE + "/" + id, 
            type    : 'DELETE',
            success : function() {
                callback();
            },
            error   : errorCallBack
        });
    }
    
    /**
     * Deletes an asset without validation.
     * @param id of the asset we want to delete.
     * @param callback handles success.
     * @param errorCallBack handles errors.
     */
    function forceDeleteAsset(id, callback, errorCallBack)
    {
        $.ajax(
            {
                url: $.perc_paths.ASSET_FORCE_DELETE + "/" + id, 
                type: 'GET',
                success: callback,
                error: errorCallBack
            });
    }

    /**
     * Validates that an asset may be deleted by the current user.
     * @param id of the asset we want to delete.
     * @param callback handles success.
     * @param errorCallBack handles errors and validation warnings.
     */
    function validateDeleteAsset(id, callback, errorCallBack)
    {
       $.ajax(
            {
                url: $.perc_paths.ASSET_VALIDATE_DELETE + "/" + id, 
                type: 'GET',
				dataType: "json",
                contentType: "application/json",
                success: callback,
                error: errorCallBack
            });
    }

    /**
     * set_asset_relationship()
     * 
     * Moved here from perc_asset_manager.js to merge functionality into one file.
     *  
     */
    function set_asset_relationship( assetid, widgetData, pageid, assetOrder, isResource, folderPath, k, err ) {
         var resType = isResource?"shared":"local";
         var relationshipId = (typeof(widgetData.relationshipId)!== "undefined") ? widgetData.relationshipId : -1;
         var awr = {
            "AssetWidgetRelationship":{
                "ownerId":pageid,
                "widgetId":widgetData.widgetid,
                "widgetName":widgetData.widgetdefid,
                "widgetInstanceName":widgetData.widgetName,
                "replacedRelationshipId": relationshipId,
                "assetId":assetid,
                "assetOrder":"0",
                "resourceType":resType
             }
         };
         if(folderPath)
            awr.AssetWidgetRelationship.folderPath = folderPath;
         $.ajax({
                   url: $.perc_paths.ASSET_WIDGET_REL + "/",
                   dataType: "text",
                   contentType: "application/json",
                   type: "POST",
                   data: JSON.stringify(awr),
                   success: k,
                   error: err });
    }
    
    /**
     * Creates an asset from local content.
     * @param assetid
     * @param widgetData
     * @param pageid
     * @param name
     * @param path
     * @param callback
     */
    function asset_from_local_content( assetid, widgetData, pageid, name, path, callback) {
        // path variable already comes with a leading '/'
        var url = $.perc_paths.ASSET_FROM_LOCALCONTENT + '/' + name + path;
        var awr = {
           'AssetWidgetRelationship' : {
               'ownerId' : pageid,
               'widgetId' : widgetData.widgetid,
               'widgetName' : widgetData.widgetdefid,
               'widgetInstanceName' : widgetData.widgetName,
               'assetId' : assetid,
               'assetOrder' : '0',
               'resourceType' : 'local'
            }
        };
        
        $.PercServiceUtils.makeRequest(
            url,
            $.PercServiceUtils.TYPE_POST,
            false,
            function( status, result ) {
                if ( status === $.PercServiceUtils.STATUS_SUCCESS )
                {
                    callback( $.PercServiceUtils.STATUS_SUCCESS, result.data );
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage( result.request );
                    callback( $.PercServiceUtils.STATUS_ERROR, defaultMsg );
                }
            },
            awr,
            "application/json",
            "text"
        );
    }

    function update_asset_relationship( assetid, relationshipId, widgetData, pageid, k, err) {
        var replacedRelationshipId = (typeof(widgetData.relationshipId)!== "undefined") ? widgetData.relationshipId : -1;
        var awr = {
            "AssetWidgetRelationship":{
                "ownerId":pageid,
                "widgetId":widgetData.widgetid,
                "widgetName":widgetData.widgetdefid,
                "widgetInstanceName":widgetData.widgetName,
                "replacedRelationshipId": replacedRelationshipId,
                "assetId":assetid,
                "assetOrder":"0",
                "resourceType":"local",
                "relationshipId":relationshipId
             }
         };
         $.PercServiceUtils.makeJsonRequest($.perc_paths.ASSET_WIDGET_REL_UPDATE + "/", 
                $.PercServiceUtils.TYPE_POST, true, function(status, results) 
         {
            if (status === $.PercServiceUtils.STATUS_SUCCESS) 
            {
                // the call returns the id of the relationship that was updated, or -1 if it does not find it
                var relId = results.data;
                if(relId === '-1' && err!=null)
                {
                    var msg = I18N.message("perc.ui.asset.service@Removed Asset");
                    err(false, msg);
                }
                else 
                {
                    k(true, results.data);
                }
            } 
            else 
            {
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                err(false, defMsg);
            }
        }, awr);
    }
    
    /**
     * clear_asset()
     * 
     * Moved here from perc_asset_manager.js to merge functionality into one file.
     *  
     */
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
    
    /**
     * Executes a request to delete selected orphan assets
     * @param ownerId
     * @param widgetId
     * @param widgetDefinitionId
     * @param assetId
     */
    function clear_orphan_assets(ownerId, widgetIds, widgetDefinitionIds, assetIds, callback ) {
	callback = callback || function(){};
	
	var assets = [];
	for (var i = 0; i < assetIds.length; i++)
	{
	    var asset = "";
	    asset = "{\"assetId\":" + "\"" + assetIds[i] + "\"," +
			"\"assetOrder\":" + "0" + "," +
			"\"ownerId\":" + "\"" + ownerId + "\"," +
			"\"widgetId\":" + widgetIds[i] + "," +
			"\"widgetName\":" + "\"" + widgetDefinitionIds[i] + "\"}";
	    assets.push(asset);
	}
	
	var json = "{\"OrphanAssetsSummary\":{\"assetWidgetRelationship\":["+ assets +"]}}";
	var awr = $.parseJSON(json);
	
	$.ajax({
             url: $.perc_paths.ASSET_ORPHAN_WIDGET_REL_DEL + "/",
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
    
     /**
     * Executes a request to update the page title value 
     * @param pageId the id of the page
     * @param assetId the id of the asset
     */
    function updateAsset(pageId, assetId, callback )
    {
	   var getUrl = $.perc_paths.ASSET_UPDATE + "/" + pageId + "/" + assetId;
       
       var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,results.data);
            }
        };
        $.PercServiceUtils.makeRequest(getUrl, $.PercServiceUtils.TYPE_POST, true, serviceCallback);

    }
    
    /**
     * Makes a call to the server to promote the asset to the template.
     * @param {Object} assetid guid of the asset that needs to be promoted, assumed not null.
     * @param {Object} widgetData widgetData Expected to be an object that provides widgetId, widgetdefid and widgetName.
     * @param {Object} ownerid only template guid is supported now.
     * @param {Object} assetOrder order of the asset.
     * @param {Object} isResource flag to indicate whether the asset is a shared asset or not.
     * @param {Object} callback function to be called after the ajax call. Calls the method with first argument as 
     * $.PercServiceUtils.STATUS_SUCCESS incase of success or $.PercServiceUtils.STATUS_ERROR in case of error. The second argument is
     * error message in case of error.
     */
    function promoteAsset(assetid, widgetData, ownerid, assetOrder, isResource, callback ) {
         var resType = isResource?"shared":"local";
         var awr = {
            "AssetWidgetRelationship":{
                "ownerId":ownerid,
                "widgetId":widgetData.widgetid,
                "widgetName":widgetData.widgetdefid,
                "widgetInstanceName": widgetData.widgetName,
                "assetId":assetid,
                "assetOrder":assetOrder,
                "resourceType":resType
             }
         };
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_PROMOTE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            awr
        );      
    }
    
    /**
     * Get all unused Assets that could be used in a specified page
     * @param pageId Id Page of the current edited page.
     */
    function getUnusedAssets(pageId, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.ASSET_UNUSED + "/" + pageId,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, $.perc_utils.convertCXFArray(result.data.UnusedAssetSummary));
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    
    /**
     * Get all unused Assets that could be used in a specified page
     * @param pageId Id Page of the current edited page.
     */
    function getAssetTypes(filterDisabledWidgets, callback){
        var url = $.perc_paths.ASSET_TYPES;
        if(filterDisabledWidgets && filterDisabledWidgets === "yes"){
            url += url.indexOf("?") === -1?"?filterDisabledWidgets=yes":"&filterDisabledWidgets=yes";
        }
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, $.perc_utils.convertCXFArray(result.data.WidgetContentType));
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
        
})(jQuery);
