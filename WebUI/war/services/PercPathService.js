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
    $.PercPathService = {
        getPathItemById: getPathItemById,
        getFolderPathItem: getFolderPathItem,
        getPathItemForPath : getPathItemForPath,
        getItemPropertiesByWorkflowState: getItemPropertiesByWorkflowState,
        deleteFolder : deleteFolder,
        deleteSection : deleteSection,
        deleteFSFolder : deleteFSFolder,
        moveItem: moveItem,
        renameFolder: renameFolder,
        createNewFolder: createNewFolder,
        getLastExistingPath: getLastExistingPath,
        validatePath:validatePath,
        getDisplayFormat : getDisplayFormat,
        getContentForPath :getContentForPath,
        getFolderProperties: getFolderProperties,
        saveFolderProperties: saveFolderProperties,
        deleteFolderSkipValidation : deleteFolderSkipValidation,
        getInlineRenderLink : getInlineRenderLink
    };

    function getDisplayFormat(callback)
    {
        // Retrieve the corresponding displayformat for the current path
        var displayFormatName = $.perc_utils.getDisplayFormat($.PercNavigationManager.getPath());
        // Search View has no path asigned, so it should always be the default one
        if ($.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_SEARCH_RESULTS || $.Percussion.getCurrentFinderView() == $.Percussion.PERC_FINDER_RESULT)
            displayFormatName = $.perc_utils.getDisplayFormat("/");
        //var url = "http://localhost:9982/Rhythmyx/services/pathmanagement/path/item/displayFormat?userid=100";
        var url = $.perc_paths.DISPLAY_FORMAT + displayFormatName;

        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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

    function getContentForPath(displayFormat, config, callback)
    {

        var str_path = $.perc_utils.encodeURL(config.path) + "/?startIndex=" + config.startIndex +
            "&maxResults=" + config.maxResults + "&displayFormatId=" + displayFormat.id +
            "&sortColumn=" + config.sortColumn + "&sortOrder=" + config.sortOrder;
        var url = $.perc_paths.PATH_PAGINATED_FOLDER + str_path;
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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
     * Get detailed information about a path.
     * @param path we want information about
     * @param callback function to be called when path information is retrieved
     * return JSON object has the following format:
     *
     *   {"PathItem":
     *       {
     *           "id":"16777215-101-713",
     *           "folderPaths":"\/\/Folders\/$System$\/Assets\/Folder1",
     *           "icon":"\/Rhythmyx\/sys_resources\/images\/folder.gif",
     *           "name":"Folder2",
     *           "type":"Folder",
     *           "folderPath":"\/\/Folders\/$System$\/Assets\/Folder1\/Folder2\/",
     *           "leaf":false,
     *           "path":"\/Assets\/Folder1\/Folder2\/"
     *       }
     *   }
     */
    function getPathItemForPath(path, callback)
    {
        var self = this;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_ITEM + path,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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
     * Get detailed information about an item.
     * @param id {string} the object id, cannot be <code>null</code> or empty.
     * @param callback function to be called when information is retrieved
     * return JSON object has the following format:
     *
     *   {"PathItem":
     *       {
     *           "id":"16777215-101-713",
     *           "folderPaths":"\/\/Folders\/$System$\/Assets\/Folder1",
     *           "icon":"\/Rhythmyx\/sys_resources\/images\/folder.gif",
     *           "name":"Folder2",
     *           "type":"Folder",
     *           "folderPath":"\/\/Folders\/$System$\/Assets\/Folder1\/Folder2\/",
     *           "leaf":false,
     *           "path":"\/Assets\/Folder1\/Folder2\/"
     *       }
     *   }
     */
    function getPathItemById(id, callback)
    {
        var self = this;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_ITEM_BY_ID + "/" + id,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    var errorCode = $.PercServiceUtils.extractGlobalErrorCode(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, errorCode);
                }
            }
        );
    }

    /**
     * Retrieves item Properties based on specified path, workflow and state.
     * @param path {string} the finder path where the search should be based in,
     *  cannot be <code>null</code>.
     * @param workflow {string} the workflow to be used for search, cannot be <code>null</code>.
     * @param state {string} the workflow state from the specified workflow, may be
     * <code>null</code> in which case items in any workflow state will be returned.
     * @param callback {function} the callback function to be called when request
     * is done. Fisrt arg is status second is the following object:
     * <pre>
     *
     * {"ItemProperties":
     *    [{"id":"16777215-101-321",
     *      "lastModifiedDate":"Jun 29, 2010 1:18:14 PM",
     *      "lastModifier":"Admin",
     *      "lastPublishedDate":"",
     *      "name":"Home",
     *      "path":"/Sites/test2/index",
     *      "status": "",
     *      "type":"hkjhkh"}
     *    ]
     * }
     * </pre>
     */
    function getItemPropertiesByWorkflowState(path, workflow, state, callback)
    {
        if(state == null)
            state = "";
        var obj = {ItemByWfStateRequest: {
                path: path,
                workflow: workflow,
                state: state
            }};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_ITEM_SUMMARY_BY_WORKFLOW_STATE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            obj

        );
    }

    /**
     * Move item to another location.
     * @param sourcepath {string} the source item path, cannot be <code>null</code>.
     * @param targetpath {string} the target path, cannot be <code>null</code>.
     * @param callback {function} the callback function to be called when request
     * is done.
     */
    function moveItem(sourcepath, targetpath, callback)
    {
        var obj = {MoveFolderItem: {
                itemPath: sourcepath,
                targetFolderPath: targetpath
            }};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_ITEM_MOVE,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            obj

        );
    }
    /**
     * Creates a new folder on the server.
     * @param path {string} the path of the parent folder, cannot be <code>null</code>
     * or empty.
     * @param callback {function} the function that will be called when the server
     * request is complete, successful or not. Cannot be <code>null</code>. The
     * callback will be passed a PathItem if successful or the error string
     * if not.
     */
    function createNewFolder(path, callback)
    {
        var self = this;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_ADD_NEW_FOLDER + path,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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
     * Request the pathItem object for the specified folder path.
     * @param path {string} the folder path string, cannot be <code>null</code>,
     * or empty.
     * @param callback {function} the function to be called when the server request
     * returns. Cannot be <code>null</code> or empty.
     */
    function getFolderPathItem(path, callback)
    {
        var self = this;
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_FOLDER +  $.perc_utils.encodeURL(path),
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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
     * Renames the specified folder.
     * @param path {string} the folder path, cannot be <code>null</code> or empty.
     * @param newName {string} the new name for the folder, cannot be <code>null</code> or
     * empty.
     * @param callback {function} the function to call after rename completes
     * or has error on the server. The
     * callback will be passed a PathItem if successful or the error string
     */
    function renameFolder(path, newName, callback){
        var obj = {"RenameFolderItem":{"path": path,"name": newName}};
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_RENAME_FOLDER,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    var defaultCode = $.PercServiceUtils.extractFieldErrorCode(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg, defaultCode);
                }
            },
            obj
        );
    }

    /**
     * Deletes a folder and its contents.  The folder is first validated to
     * determine if all child items can be deleted.  Appropriate dialogs are
     * displayed.
     *
     * @param path of the folder as it appears in the finder, i.e., /Sites/MySite/MyFolder.
     * @param name of the folder.
     * @param type of folder (asset, site, section).
     * @param callback function to be executed after a successful deletion.
     */
    function deleteFolder(path, name, type, callback){
        $.ajax({
            url: $.perc_paths.PATH_VALIDATE_DELETE_FOLDER + path,
            dataType: "text",
            type: 'GET',
            success: function(data) {
                cbVdfSuccess(data, path, name, type, callback)},
            error: cbVdfErrors });
    }

    /**
     * Deletes a section and all contents.  Calls deleteFolder(path, name, "section", callback).
     */
    function deleteSection(path, name, callback){
        var shouldPurge = path.indexOf($.perc_paths.RECYCLING_ROOT_NO_SLASH) !== -1;
        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
        //var guid = $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
        //var delCriteria  = {"DeleteFolderCriteria":{"path":path,"skipItems":skipItems, "shouldPurge": shouldPurge, "guid":guid}};
        //var delCriteria  = {"DeleteFolderCriteria":{"path":path,"skipItems":"NO", "shouldPurge": shouldPurge}};
        var dataJson = JSON.parse($('div.perc-site-map-box-selected').attr("data"));
        var guid = dataJson.id;
        var delCriteria  = {"DeleteFolderCriteria":{"path":path,"skipItems":"NO", "shouldPurge": shouldPurge, "guid":guid}};
        var timeoutMillis = 3600000;
        $.ajax({
            url: $.perc_paths.PATH_DELETE_FOLDER,
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(delCriteria),
            timeout: timeoutMillis,
            success: callback,
            error: cbDfErrors
        });
        $.unblockUI();
    }
    /**
     * Deletes a Folder in the filesystem and all of it's files and subfolders.
     * Calls deleteFolder(path, name, "fsfolder", callback).
     */
    function deleteFSFolder(path, name, callback)
    {
        deleteFolder(path, name, "fsfolder", callback);
    }

    /**
     * Checks for the existence of a path and finds the last portion of the path which exists.
     * @param path as it appears in the finder, i.e., /Sites/MySite/MyFolder.
     * @param callback {function} the function to be called when the server request
     * returns. Cannot be <code>null</code> or empty.
     * return last existing path as it appears in the finder (no leading/trailing forward slashes)
     * or empty for root paths ("/Assets", "/Sites").
     */
    function getLastExistingPath(path, callback, sync)
    {
        $.PercServiceUtils.makeRequest(
            $.perc_paths.PATH_LAST_EXISTING + path,
            $.PercServiceUtils.TYPE_GET,
            (sync != null && sync) ? true : false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
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
     * Checks for the existence of a path.
     * @param path as it appears in the finder, i.e., /Sites/MySite/MyFolder.
     * @param callback {function} the function to be called when the server request
     * returns. Cannot be <code>null</code> or empty.
     */
    function validatePath(path, callback)
    {
        $.PercServiceUtils.makeRequest(
            $.perc_paths.PATH_VALIDATE_EXIST + path,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            null,
            null,
            null,
            true
        );
    }

    /**
     * Validate folder delete success callback, shows the warning message and makes a call to the server if user clicks ok.
     */
    function cbVdfSuccess(data, path, name, type, callback)
    {
        var shouldPurge = path.indexOf($.perc_paths.RECYCLING_ROOT_NO_SLASH) !== -1;

        var title;
        if (type == "section")
        {
            title = I18N.message( "perc.ui.finder.section.delete@Title" );
        }
        else
        {
            if (shouldPurge) {
                title = I18N.message("perc.ui.finder.folder.purge@Title");
            } else {
                title = I18N.message("perc.ui.finder.folder.delete@Title");
            }
        }

        /**
         * Set the timeout to one hour (3600 seconds) because of possible large folders.
         */
        var timeoutMillis = 3600000;

        $.perc_utils.confirm_dialog({
            id: 'perc-finder-delete-folder',
            title: title,
            question: createDelWarning(data, name, type, shouldPurge),
            success: function(){
                var skipItems = "EMPTY";
                if($("#perc_delete_folder_force").length > 0)
                {
                    skipItems=$("#perc_delete_folder_force").get(0).checked?"NO":"YES";
                }
                var guid;
                /*if(typeof $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id")==='undefined'){
                    guid = $('a.perc-listing-category-SECTION_FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }else{
                    guid = $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }*/

                if(typeof $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id")!=='undefined'){
                    guid = $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }else if(typeof $('a.perc-listing-category-SECTION_FOLDER.perc_last_selected').attr("id")!=='undefined'){
                    guid = $('a.perc-listing-category-SECTION_FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }else if(typeof $('a.perc-listing-category-SYSTEM.perc_last_selected').attr("id")!=='undefined'){
                    guid = $('a.perc-listing-category-SYSTEM.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }else{
                    guid = $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                }

                //var guid = $('a.perc-listing-category-FOLDER.perc_last_selected').attr("id").split("perc-finder-listing-")[1];
                var delCriteria  = {"DeleteFolderCriteria":{"path":path,"skipItems":skipItems, "shouldPurge": shouldPurge, "guid":guid}};
                //var delCriteria  = {"DeleteFolderCriteria":{"path":path,"skipItems":skipItems, "shouldPurge": shouldPurge}};
                $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                $.ajax({
                    url: $.perc_paths.PATH_DELETE_FOLDER,
                    type: 'POST',
                    dataType:"json",
                    contentType:"application/json",
                    data:JSON.stringify(delCriteria),
                    timeout: timeoutMillis,
                    success: callback,
                    error: cbDfErrors });
                $.unblockUI();
            },
            width:500});
    }

    /**
     * Delete a folder without making first a request to validations.
     */
    function deleteFolderSkipValidation(path, name, type, callback)
    {
        var data = '';
        cbVdfSuccess(data, path, name, type, callback);
    }

    /**
     * Validate folder delete error callback, shows the error message to the user.
     */
    function cbVdfErrors(error)
    {
        var errorMsg = $.PercServiceUtils.extractDefaultErrorMessage(errors);
        var defMessage = I18N.message("perc.ui.path.service@Failed to Delete Folder")
        $.perc_utils.alert_dialog( {
            id: 'perc-finder-delete-error',
            title: I18N.message("perc.ui.path.service@Delete Folder Error"),
            content: (errorMsg != "")? errorMsg : defMessage});
    }

    /**
     * Delete folder error callback, shows the error message to the user.
     */
    function cbDfErrors(errors)
    {
        var errorMsg = $.PercServiceUtils.extractDefaultErrorMessage(errors);
        var defMessage = I18N.message("perc.ui.path.service@Failed to Delete Folder")
        $.perc_utils.alert_dialog( {
            id: 'perc-finder-delete-error',
            title: I18N.message("perc.ui.path.service@Delete Folder Error"),
            content: (errorMsg != "")? errorMsg : defMessage});
    }

    /**
     * Creates a custom delete warning message based on the supplied message type, folder name, and folder type.
     */
    function createDelWarning(type, name, folderType, shouldPurge)
    {
        var confirm;
        var warning;
        var middle = "";
        var del = "";
        if (folderType == "Assets")
        {
            if (shouldPurge) {
                warning = I18N.message("perc.ui.finder.folder.purge@WarningAssets",[name]);
                confirm = I18N.message("perc.ui.finder.folder.purge@ConfirmAssets",[name]);
            } else {
                warning = I18N.message("perc.ui.finder.folder.delete@WarningAssets",[name]);
                confirm = I18N.message("perc.ui.finder.folder.delete@ConfirmAssets",[name]);
            }

            if (type.indexOf("AssetsNotAuthorized") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@AssetNotAuthorized" ) + "<br/><br/>";
            }

            if (type.indexOf("AssetsInUseTemplates") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@AssetInUseTemplates" ) + "<br/><br/>";
            }

            if (type.indexOf("AssetsInUsePages") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@AssetInUsePages" ) + "<br/>";
                del = "<br/><input type='checkbox' id='perc_delete_folder_force' style='width:15px'/> <label class='perc_dialog_label'>" +
                    I18N.message( "perc.ui.finder.folder.delete@DeleteLiveAssets" ) + "</label>";
            }
        }
        else if (folderType == "fsfolder")
        {
            confirm = "perc.ui.finder.fsfolder.delete@Confirm";
            warning = "perc.ui.finder.fsfolder.delete@Warning";
        }
        else
        {
            if (folderType == "Sites")
            {
                if (shouldPurge) {
                    confirm = "perc.ui.finder.folder.purge@ConfirmPages";
                    warning = "perc.ui.finder.folder.purge@WarningPages";
                } else {
                    confirm = "perc.ui.finder.folder.delete@ConfirmPages";
                    warning = "perc.ui.finder.folder.delete@WarningPages";
                }
            }
            else
            {
                confirm = "perc.ui.finder.section.delete@Confirm";
                warning = "perc.ui.finder.section.delete@Warning";
            }

            if (type.indexOf("PagesNotAuthorized") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@PageNotAuthorized" ) + "<br/><br/>";
            }

            if (type.indexOf("PagesInUseTemplates") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@PageInUseTemplates" ) + "<br/><br/>";
            }

            if (type.indexOf("PagesInUsePages") > -1)
            {
                middle += I18N.message( "perc.ui.finder.folder.delete@PageInUsePages" ) + "<br/>";
                del = "<br/><input type='checkbox' id='perc_delete_folder_force' style='width:15px'/> <label class='perc_dialog_label'>" +
                    I18N.message( "perc.ui.finder.folder.delete@DeleteLinkedPages" ) + "</label>";
            }
        }

        var first;
        if (type == "Success")
        {
            first = I18N.message( confirm, [name] );
        }
        else
        {
            first = I18N.message( warning, [name] ) + "<br/><br/>";
        }

        return first + middle + del;
    }

    /**
     * Get detailed information about a folder, given its id.
     * @param id {string} the object id, cannot be <code>null</code> or empty.
     * @param callback {function} the callback function to be called when request is done, cannot
     * be <code>null</code> or empty. Fisrt arg is status second (result) is the following object:
     * <pre>
     * {
     *    "FolderProperties" : {
     *      "id" : "16777215-101-716",
     *      "name" : "mynewfolder",
     *      "permission" : {
     *        "accessLevel" : "WRITE"
     *      },
     *      "workflowId" : -1,
     *      "allowedSites" : "301,302"
     *    }
     * }
     * </pre>
     */
    function getFolderProperties(id, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_GET_FOLDER_PROPERTIES + "/" + id,
            $.PercServiceUtils.TYPE_GET,
            true,
            function(status, result) {
                callback(status, result);
            }
        );
    }

    /**
     * Saves folder properties.
     * @param folderProps {object} that contains the folder properties:
     * <pre>
     * {
     *   "FolderProperties" : {
     *     "name": "mynewfolder",
     *     "id": "16777215-101-703",
     *     "permission": {
     *       "accessLevel": "READ",
     *       "writePrincipals": []
     *       },
     *     "allowedSites":"302,307"
     *   }
     * }
     * </pre>
     * @param callback {function} the callback function to be called when request
     * is done, cannot be <code>null</code>. Fisrt arg is status second is the following object:
     * <pre>
     * {
     *    "NoContent" : {
     *      "operation" :"saveFolderProperties"
     *    }
     * }
     * </pre>
     */
    function saveFolderProperties(folderProps, callback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.PATH_SAVE_FOLDER_PROPERTIES,
            $.PercServiceUtils.TYPE_POST,
            true,
            function(status, result) {
                callback(status, result);
            },
            folderProps
        );
    }

    /**
     * Gets item render link details from server for the given item id, see com.percussion.pagemanagement.service.impl.PSRenderLinkService#renderPreviewLink
     * method for details.
     * @param {Object} itemId assumed to be a valid itemId
     * @param {Object} callback The function that will be called with two arguments,
     *     status (first arg) -- boolean true if service call succeeds otherwise false
     *     data (second arg) -- this will be String error message if status is false, otherwise the data object returned by service call.
     *
     */
    function getInlineRenderLink(itemId, callback)
    {
        var svcUrl = $.perc_paths.RENDER_LINK_PREVIEW + "/" + itemId + "/default";

        $.PercServiceUtils.makeJsonRequest(
            svcUrl,
            $.PercServiceUtils.TYPE_GET,
            true,
            function(status, result) {
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(true, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
            }
        );
    }

})(jQuery);
