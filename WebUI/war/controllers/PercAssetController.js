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
 * PercAssetController.js
 * @author Jose Annunziato
 * @see PercAssetEditorModel.js
 * @see PercAssetService.js
 * @see PercPathService.js
 * 
 * 
 */
(function($)
{
	$.PercAssetController = {	
			getAssetEditorLibrary: getAssetEditorLibrary,
			getAssetEditorForAssetId: getAssetEditorForAssetId,
			getAssetViewForAssetId: getAssetViewForAssetId,
			getPathItemForPath : getPathItemForPath,
			putAssetInFolder : putAssetInFolder
	};

    /* =========================================================
     * Public Functions
     * ========================================================= */
     
    /**
     * Retrieves a list of editor objects used to render a library of editors in a dialog for the user to choose from.
     * Editor object contains icon and URL. URL points to location of HTML form for editing/creating the type of asset.
     * Editor object schema:
     * {AssetEditor  : [
     *      {"icon"       : "/path/to/image.png",
     *       "title"      : "Title String",
     *       "url"        : "http://URL/to/html/editor/for/editing/asset.html",
     *       "workflowId" : 4   (some number)
     *      }
     * ]}
     */
    function getAssetEditorLibrary(currentFolderPath, controllerCallback)
    {
        function serviceCallback(status, assetEditorLibrary)
        {
        	// iterate over the JSON response containing an array of asset editor objects
        	// create array of PercAssetEditorModel instances and return array
            var assetEditors = [];
            var assetEditor  = assetEditorLibrary.AssetEditor;
            for(i in assetEditor)
            {
                var icon       = assetEditor[i].icon;
                var title      = assetEditor[i].title;
                var url        = assetEditor[i].url;
                var workflowId = assetEditor[i].workflowId;
                var editor     = new $.PercAssetEditorModel(icon, title, url, workflowId);
                assetEditors.push(editor);
            }
            controllerCallback(assetEditors);
        }
        $.PercAssetService.getAssetEditorLibrary(currentFolderPath, serviceCallback);
    }
    
    /**
     * Retrieves the URL of the editor for a given asset id
     * @param assetId(String) the id of the asset we want to edit
     * @param callback(function(status(String), assetEditorUrl(String)))
     */
    function getAssetEditorForAssetId(assetId, callback)
    {
        $.PercAssetService.getAssetEditorForAssetId(assetId, function(status, assetEditorUrl){
        	callback(status, assetEditorUrl);
        });
    }
    
    /**
     * Retrieves the URL of the readonly view for a given asset id
     * @param assetId(String) the id of the asset we want to edit
     * @param callback(function(status(String), assetViewUrl(String)))
     */
    function getAssetViewForAssetId(assetId, callback)
    {
        $.PercAssetService.getAssetViewForAssetId(assetId, function(status, assetViewUrl){
         callback(status, assetViewUrl);
        });
    }
    
    /**
     * Gets the path item object from the path service given a path from the finder
     * @param path we want path item for
     * @param controllerCallback is method we call after building the path item instance and pass it back
     */ 

    function getPathItemForPath(path, controllerCallback)
    {
        function serviceCallback(status, pathItemJson)
        {
            var pathItemObj = new $.PercPathItemModel(
                                    pathItemJson.PathItem.id,
                                    pathItemJson.PathItem.folderPaths,
                                    pathItemJson.PathItem.icon,
                                    pathItemJson.PathItem.name,
                                    pathItemJson.PathItem.type,
                                    pathItemJson.PathItem.folderPath,
                                    pathItemJson.PathItem.leaf,
                                    pathItemJson.PathItem.path);
            controllerCallback(pathItemObj);
        }
        $.PercPathService.getPathItemForPath(path, serviceCallback);
    }
    
    /**
     * 
     */
    function putAssetInFolder(assetId, folderPath, controllerCallback)
    {
    	var assetFolderRelationship = {"AssetFolderRelationship" : {"assetId" : assetId,    "folderPath" : folderPath}};
    	$.PercAssetService.putAssetInFolder(assetFolderRelationship, controllerCallback);
    }
})(jQuery);
