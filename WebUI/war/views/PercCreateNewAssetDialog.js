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

(function ($) {
    $.PercCreateNewAssetDialog = function(assetType, successCallback, cancelCallback){
        $.PercCreateNewAssetDialogData = {};
        $.PercCreateNewAssetDialogData.successCallback = successCallback;
        $.PercCreateNewAssetDialogData.cancelCallback = cancelCallback;
        
		var assetFolderPath = $('#perc_selected_path').text();
		
        $.PercAssetService.getAssetEditor(assetType, assetFolderPath, function(status, result){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.create.new.asset.dialog@Error New Asset"), content: result});
            }
            if($.PercFinderTreeConstants.FOLDER_ID != null){
            	result.AssetEditor.legacyFolderId = $.PercFinderTreeConstants.FOLDER_ID;
        	}
            $.PercCreateNewAssetDialogData.assetEditor = result.AssetEditor;
            createContentEditDialog();
        });
        /**
         * Creats the content editor dialog. Sets the height and widths as per the criteria
         * Sets the src of the Iframe to the url from the criteria.
         */
        function createContentEditDialog(){
            var assetEditor = $.PercCreateNewAssetDialogData.assetEditor;
            //Create a dialog html with iframe, set the src to the content edit criteria url
            var url = assetEditor.url +
                        "&sys_folderid=" + assetEditor.legacyFolderId +
                        "&sys_workflowid=" + assetEditor.workflowId;
            
            var dlgHtml = "<div id='create-new-asset-content-dlg'>" +
                                "<iframe name='create-new-asset-content-frame' id='create-new-asset-content-frame'" +
                                " height='100%' FRAMEBORDER='0' width='100%' src='" + url +
                                "'></iframe>" +
                           "</div>";
            //Create dialog and set the preferred height and width from the criteria
            dialog = $(dlgHtml).perc_dialog({
                title: I18N.message("perc.ui.create.new.asset.dialog@Edit Widget Content"),
                resizable: true,
                modal: true,
                height: 615,
                width: 965,
                percButtons: {
                    "Save": {
                        click: function(){
                            saveAssetContent(successCallback);
                        },
                        id: "perc-create-new-asset-save-button"
                    },
                    "Cancel": {
                        click: function(){
                            dialog.remove();
                            $.PercCreateNewAssetDialogData.cancelCallback();
                        },
                        id: "perc-create-new-asset-cancel-button"
                    }
                },
                id: 'perc-create-new-asset-dialog'
            });
        }
    };
      /**
       * Submits the form content to the server by using the ajax form.
       * Sets the iframe option to true to submit it as multi part form data.
       * The success call back is assigned to saveAssetResponse function.
       */
      function saveAssetContent(successCallback) {
         $.PercBlockUI();
         var assetEditor = $.PercCreateNewAssetDialogData.assetEditor;
         $contentFrameElem("#perc-content-edit-sys_workflowid").val(assetEditor.workflowId);
         var path = assetEditor.url;
         path = path.split("?")[0];
         var fid = assetEditor.legacyFolderId;
         path = path + "?sys_folderid=" + fid + "&sys_asset_folderid=" + fid;
         $contentFrameElem("#perc-content-form").attr("action", path);
         //call all the pre submit handlers if nothing returns flase, submit the form.
         var dosubmit = true;
         $.each($.PercContentPreSubmitHandlers.getHandlers(),function(){
            if(!this()){
                dosubmit = false;
            }
         });
         if(!dosubmit)
         {
			$.unblockUI();            
            return;
         }
         //We are done processing the handlers, as we are submitting the form, clear all handlers.
         $.PercContentPreSubmitHandlers.clearHandlers();
         $("#create-new-asset-content-frame").contents().find("#perc-content-form").trigger("submit");
         //Unbind the function to avoid an acumulation of calls to the same function
         $("#create-new-asset-content-frame").off('load', saveAssetResponse).on("load",saveAssetResponse);
      }
      
      /**
       * This function gets called before the form gets submitted.
       */
      function beforeAssetSubmit(formData, jqForm, options) {

         return true;
      }
      
      /**
       * Callback that gets called after the content editor form has been submitted.
       * Checks the response text for any errors, if yes resets the form content with the response text and returns.
       * Makes an ajax call to relate the asset to the parent (page or template)
       * and add to asset library folder and page folder if it is a resource.
       */
      function saveAssetResponse() {
            if ($("#create-new-asset-content-frame").contents().find("#perc-content-edit-errors").length > 0){
                $.unblockUI();
                return;
            }
            var assetid = "-1-101-" + $("#create-new-asset-content-frame").contents().find("[name=sys_contentid]").val();
            $("#create-new-asset-content-frame").remove();
            $("#create-new-asset-content-dlg").remove();
            $.PercPathService.getPathItemById(assetid, function(status, result){
                $.unblockUI();
                if(status===$.PercServiceUtils.STATUS_ERROR)
                {
                    //This should not happen as we just created the asset we should be able to get the asset path item.
                    $.perc_utils.alert_dialog({title: 'Error Selecting Asset', content: "The asset has been saved, but failed to select it."});
                    $.PercCreateNewAssetDialogData.cancelCallback();
                    return;
                }
                $.PercCreateNewAssetDialogData.successCallback(result.PathItem);
            });
        }
		
      /**
       * Returns the jquery object by selecting supplied selector from the content frame document 
       * rather than the main document.
       * @param any Jquery selector which needs to be found from edit widget content iframe.
       * @return Jquery object.
       */
      function $contentFrameElem(selector) {
         var contentFrameDoc = getFrameDocument();
         return $(selector, contentFrameDoc);
      }
      
      /**
       * Content editor lives in an iframe and this method returns the document object of the content edit iframe. 
       * @return document object
       */
      function getFrameDocument() {
         var contentFrameDoc = null;
         for (i = 0; i < frames.length; i++) {
            if (frames[i].name === "create-new-asset-content-frame") {
               contentFrameDoc = frames[i].document;
               break;
            }
         }
         return contentFrameDoc;
      }

})(jQuery);
