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
* PercIFrameView.js
* 
* Handles user interaction with the iFrame.
* 
*/
(function($) {
    $.PercIFrameView = {
       renderAssetEditor : renderAssetEditor,
       saveContent : saveContent
    };
    
    // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
    var dirtyController = $.PercDirtyController;
   var initialized = false; 
   var assetPath = null;
   var newAsset, workflowId, frame, folderId;

   function renderAssetEditor(finder, wfId, editorUrl, path, pathArray, isNewAsset)
   {
       // get the frame at the bottom
       frame = $("#frame");
       frame.contents().remove(); 
       frame.off( ".reload" );
       assetPath = path;
       newAsset = isNewAsset;
       workflowId = wfId;
       // set the content to the url param
       // we only use sys_contentid and sys_revision when editing
       // but we dont need them when creating a brand new one
       frame[0].src = editorUrl;
       /*
        * However we do need the folderid (Adam Gent)
        */
       var _re = /sys_folderid=([0-9]+)/;
       _re = _re.exec(editorUrl);
       folderId = _re && _re.length > 0 ? _re[1] : null;

         //We are now loading the form, the clear the content presubmit handlers.
        $.PercContentPreSubmitHandlers.clearHandlers();
   
       // after whole form has loaded, override the workflowid, add url filter to name field
       frame.on("load",function(evt)
       {
           if(initialized === false){
               initialized = true;
               onIntialFrameLoad();
           }
           else{
                onLaterFrameLoads();
           }
       });
       
      
       fixIframeHeight();
       
        // render the save button
       if(newAsset)
       {
           var menuId = $("#perc-layout-menu").length > 0 ?
               '#perc-layout-menu'
              : '#perc-content-menu';
           $(menuId).html("");
           $('<button name="perc_wizard_save" class="btn btn-primary" id="perc-save-content" style="float:right; background-color: #00a8df; border-color: #00a3d9; color: #ffffff; border-radius: 4px; display:inline-block; cursor:pointer; padding-top: 6px; padding-bottom: 6px; padding-left: 12px; padding-right: 12px; text-align: center; font: 13.333px Arial !important; font-weight: normal; white-space: normal; vertical-align: middle; margin-top:11.5px; border-style:outset; border-width:2px;">' +I18N.message("perc.ui.common.label@Save")+' </button>')
               .appendTo(menuId);
   
           // render cancel button
           $('<button class="btn btn-primary" id="perc-cancel-content" style="float:right; background-color: #00a8df; border-color: #00a3d9; color: #ffffff; border-radius: 4px; display:inline-block; cursor:pointer; padding-top: 6px; padding-bottom: 6px; padding-left: 12px; padding-right: 12px; text-align: center; font: 13.333px Arial !important; font-weight: normal; white-space: normal; vertical-align: middle;margin-top:11.5px; border-style:outset; border-width:2px ">' +I18N.message("perc.ui.change.pw@Close") +  '</button>')
               .appendTo(menuId);
   
           // submit the form when save button is clicked
           $("#perc-save-content").off('click').on("click",function() { saveContent(true); });
   
           // reset the form when cancel button is clicked
           $("#perc-cancel-content").off('click').on("click",function() { cancel(); });
   

       }


   }

    // cancel and clear content of form
    function cancel() {
        $.PercNavigationManager.goToDashboard();
    }
   function onLaterFrameLoads(){
       //Make sure there are no errors.
       if(frame.contents().find("#perc-content-edit-errors").length === 0)
       {
          if(newAsset)
          {
             addAssetToFolder(frame);
             $.unblockUI();
          }
          else
          {
             $.PercPathService.getPathItemById($.PercNavigationManager.getId(), 
                function(status, result){
                   if(status === $.PercServiceUtils.STATUS_SUCCESS)
                   {
                      var name = result.PathItem.name;
                      $.PercNavigationManager.setReopenAllowed(true);
                      $.PercNavigationManager.goToLocation(
                         $.PercNavigationManager.VIEW_EDIT_ASSET,
                         $.PercNavigationManager.getSiteName(),
                         $.PercNavigationManager.getMode(),
                         $.PercNavigationManager.getId(),
                         name,
                         $.PercNavigationManager.getPath(),
                         $.PercNavigationManager.PATH_TYPE_ASSET);
                      $.unblockUI();
                   }
                   else
                   {
                      $.unblockUI();
                      $.perc_utils.alert_dialog({title: 'Error', content: result});
                   }
             });
          }
       }
       else
       {
           // re-attach the url filter to the name field, disable 'Enter' on input fields
           updateContentForm(frame.contents().find("#perc-content-form"));
           $.unblockUI();
       }
   }
   function onIntialFrameLoad(){
           if(newAsset && workflowId)
           {
               frame.contents().find("[name=sys_workflowid]").val(workflowId);
           }

           var contentForm = frame.contents().find("#perc-content-form");
           // attach the url filter to the name field, disable 'Enter' on input fields
           updateContentForm(contentForm);

           if(!newAsset)
           {
                frame.contents().find("#perc-site-impact-panel").show();
                $.PercSiteImpactView.renderSiteImpact($.PercNavigationManager.getId(), $.PercSiteImpactView.ITEM_TYPE_ASSET,frame.contents().find("#perc-site-impact-panel"));
           }
   }
   /**
    * Saves the asset content by submitting the form of the iframe. If it is new asset then gets the content id from 
    * the during the iframe reload and adds it to the folder. Then reloads the browser by calling the navigation manager
    * with new path.
    * @param isNew(boolean) If true the asset is saved and added to the folder and the browser is reloaded. Otherwise
    * the asset is saved.
    */
   function saveContent(isNew)
   {
       dirtyController.setDirty(false, "asset");
       $.PercBlockUI();
       
         //call all the pre submit handlers if nothing returns flase, submit the form.
         var dosubmit = true;
         $.each($.PercContentPreSubmitHandlers.getHandlers(),function(){
            if(!this()){
                dosubmit = false;
            }
         });

       var showMandatoryFieldAlertPopUp=false;
       showMandatoryFieldAlertPopUp = $.perc_utils.checkMandatoryFieldsEmpty(frame);
       if(showMandatoryFieldAlertPopUp){
           dosubmit = false;
       }

         if(!dosubmit)
         {
            $.unblockUI();
            return;
         }
         //We are done processing the handlers, as we are submitting the form, clear all handlers.
         $.PercContentPreSubmitHandlers.clearHandlers();

       // the form is in the frame.
       // the form submits to containing document, i.e., submits to itself and frame is reloaded
       $(window).removeData();
       frame.removeData();
       frame.contents().find("#perc-content-form").trigger("submit");
    }
    
    /**
     * Helper method to load the asset with the given parameters. This is a browser reload.
     * @param folderPath assumed not null.
     * @param assetName assumed not null.
     * @param assetId assumed not null.
     * 
     */
    function loadAsset(folderPath, assetName, assetId)
    {
      $.PercNavigationManager.goToLocation(
        $.PercNavigationManager.VIEW_EDIT_ASSET,
        $.PercNavigationManager.getSiteName(),
        $.PercNavigationManager.getMode(),
        assetId,assetName,folderPath + "/" + assetName,$.PercNavigationManager.PATH_TYPE_ASSET);
    }

    /**
     * Helper method to update the content form.  Adds a url filter to the name field and
     * also disables the 'Enter' key on all input fields.
     * @param form the content form assumed not null.
     */
    function updateContentForm(form)
    {
      if (folderId) {
            /*
             * We need to put the folder id in the forms action for asset renaming to work.
             */
            var oldUrl = form.attr("action");
            oldUrl = oldUrl + "?sys_folderid=" + folderId + "&sys_asset_folderid=" + folderId;
            form.attr("action", oldUrl);
      }
      var nameField = form.find("[name=sys_title]");
      if(nameField.length > 0)
      {
         $.perc_filterField(nameField, $.perc_textFilters.URL);
      }
 
      form.find("[type=text]").on("keypress",function(event) {
         if(event.keyCode === 13)
         {
            return false;
         }
      });
    }

    /**
     * Helper method to add an asset contained in the content form of the specified frame to the
     * current folder.  After the asset is added to the folder, the finder is opened to show the
     * asset.
     * @param frame the frame which is being loaded and contains the content form assumed not null.
     */
    function addAssetToFolder(frame)
    {
      // a hidden field contains the content id, retrieve it
      var assetContentId = frame.contents().find("[name=sys_contentid]").val();
      if(assetContentId === "")
      {
         $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.iframe.view@Unable To Create Asset")});
         return;
      }
       
      // put the asset in the current folder
      assetContentId = "-1-101-" + assetContentId;
      let path = "//Folders/$System$/Assets" + assetPath;
      $.PercAssetController.putAssetInFolder(assetContentId, path, function(status, res)
      {
         // after putting the asset in the folder, open the finder
         // in the current folder to show the new asset
         loadAsset($.perc_paths.ASSETS_ROOT + assetPath,frame.contents().find("[name=sys_title]").val(),res.AssetFolderRelationship.assetId);
      });
    }
})(jQuery);
