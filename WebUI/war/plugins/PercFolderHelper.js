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
 * [PercFolderHelper.js]
 * Consists of the helper methods related to the folders.
 */ 
(function($){
    $.PercFolderHelper = function()
    {
        var folderHelperAPI = 
        {
            /**
             * Gets the accessLevel for the user for a given object(Folder, Page, Asset) specified by the path. This is
             * purely a folder access level and does not include the workflow access. Folder here could be a system
             * folder(//Assets/uploads etc...) or a site or a section or a regular folder.
             * 
             * @param path
             *            (String) must not be blank.
             * @param fromCache
             *            (boolean) if true, gets the value from finder path items if exists otherwise gets it from
             *            server
             * @callback (Function), callback function with status and result. if status is PERMISSION_SUCCESS the
             *           result would be one of PERMISSION_XXX value. if the status is PERMISSION_ERROR then result
             *           would be the error message.
             */
            getAccessLevelByPath : _getAccessLevelByPath,

            /**
             * Same as getAccessLevelByPath except for the accessLevel is retrieved by ID
             */
            getAccessLevelById : _getAccessLevelById

        };
        
        var folderHelperConstants = 
        {
            //Constant for read permission
            PERMISSION_READ:'READ',
            //Constant for write permission
            PERMISSION_WRITE:'WRITE',
            //Constant for admin permission
            PERMISSION_ADMIN:'ADMIN',
            //Constant for successful retrieval of permission
            PERMISSION_SUCCESS:'SUCCESS',
            //Constant for unsuccessful retrieval of permission
            PERMISSION_ERROR:'ERROR'
        };
        
        //Merge the api methods and constants and return.
        var obj = {};
        $.extend(obj, folderHelperAPI);
        $.extend(obj, folderHelperConstants);
        return obj;

        //Private methods //
        
        function _getAccessLevelByPath(path, fromCache, callback)
        {
            var pathItem = $.perc_finder().getPathItemByPath(path);
            if(!fromCache || !pathItem)
            {
               $.PercPathService.getPathItemForPath(path, function(status, result){
                   if(status===$.PercServiceUtils.STATUS_ERROR)
                   {
                       callback(folderHelperConstants.PERMISSION_ERROR, result);
                       return;
                   }
                   callback(folderHelperConstants.PERMISSION_SUCCESS,result.PathItem.accessLevel);
               });
            }
            else
            {
                callback(folderHelperConstants.PERMISSION_SUCCESS,pathItem.accessLevel);
            }
        }
        
        function _getAccessLevelById(objectId, fromCache, callback)
        {
            var pathItem = $.perc_finder().getPathItemById(objectId);
            if(!fromCache || !pathItem)
            {
                $.PercPathService.getPathItemById(objectId, function(status, result, errorCode){
                    if(status===$.PercServiceUtils.STATUS_ERROR)
                    {
                        callback(folderHelperConstants.PERMISSION_ERROR, result, errorCode);
                        return;
                    }
                    callback(folderHelperConstants.PERMISSION_SUCCESS,result.PathItem.accessLevel);
                });
            }
            else
            {
                callback(folderHelperConstants.PERMISSION_SUCCESS,pathItem.accessLevel);
            }
        }
    };
    
})(jQuery);
