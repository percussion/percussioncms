
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * Dialog for creating and editing the content from pages and templates through a widget. 
 * Creates a dialog with an Iframe.
 * Makes an ajax call to get the content edit criteria. Ex: Object
 * {"ContentEditCriteria":{
 *   "contentName":"LocalContent - 1023",
 *   "preferredEditorHeight":200,
 *   "preferredEditorWidth":800,
 *   "producesResource":false,
 *   "url":"/Rhythmyx/psx_cePSWidget_RawHtml/PSWidget_RawHtml.html?sys_command=edit&sys_view=sys_HiddenFields:",
 *   "workflowId":6}}
 * Refer to PSContentEditCriteria.java class for the details.
 * After getting the criteria creates the dialog using the height and width.
 * Sets the src of the iframe to the url.
 * While creating new content, if the content type produces resource type asset
 * opens a save as dialog to collect name and the location from asset library to store the asset.
 * Otherwise creats the asset with the contentName from criteria.
 */
(function ($) {
   $.perc_asset_edit_dialog = function (reload_callback, cancelCallback, assetid, widgetData, parentId, ownerType) {
      ownerType = ownerType || "page";
      var ut = $.perc_utils;
      var dialog;
      //Content edit criteria object initialized in the call back of ajax call.
      var contentEditCriteria;
      var assetId = assetid;
      var initialized = false; 
      var maximized = false;
      var saved_left=0;
      var saved_top=0;
      var saved_height=0;
      var saved_width=0;
      //Create asset edit url request to get the asset edit criteria
      var urlReq = {
        "AssetEditUrlRequest":{
            "type":ownerType, 
            "assetId":assetid, 
            "widgetId":widgetData.widgetid, 
            "parentId":parentId,
            "widgetDefinition":widgetData.widgetdefid}
      };

        //On success creates the dialog by calling create Dialog method.
        $.ajax({url:$.perc_paths.CONTENT_EDIT_CRITERIA + "/", 
                dataType:"json", 
                contentType:"application/json", 
                type:"POST", 
                data:JSON.stringify(urlReq), 
                success:function (editCriteriaData) {
                    contentEditCriteria = editCriteriaData.ContentEditCriteria;
                    createContentEditDialog();
                }, 
                error:function (request, textstatus, error) {

            if($.PercServiceUtils.extractDefaultErrorMessage(request)=='BlankContent'){
                        ut.alert_dialog({
                            id:"perc-content_info_error",
                            title : I18N.message("perc.ui.page.label@Editing Page"),
                            content: I18N.message("perc.ui.asset.info.dialog@Error Opening Content Editor"),
                            width: 400
                        });
                    }
            else{
                    ut.alert_dialog({
                        id:"perc-content_edit_error",
                        title : I18N.message("perc.ui.asset.edit.dialog@Error Opening Content Editor"),
                        content : $.PercServiceUtils.extractDefaultErrorMessage(request),
                        width: 400
                    }); }
                }
        });
      
      /**
       * Creats the content editor dialog. Sets the height and widths as per the criteria
       * Sets the src of the Iframe to the url from the criteria.
       */
    function createContentEditDialog() {
        //Create a dialog html with iframe, set the src to the content edit criteria url
        var maximized = false;
        var saved_left=0;
        var saved_top=0;
        var saved_height=0;
        var saved_width=0;
        clearTimeout($.data(this, 'resizeTimer'));

        var url = contentEditCriteria.url +
            '&sys_folderid=' + contentEditCriteria.legacyFolderId +
            "&percpageid=" + parentId +
            "&sys_workflowid=" + contentEditCriteria.workflowId;
        
        var dlgHtml = "<div id='edit-widget-content'>" + 
        "<iframe name='edit-widget-content-frame' id='edit-widget-content-frame' height='100%' FRAMEBORDER='0' width='100%' src='" + 
        url + "'></iframe>" + "</div>";
        //Create dialog and set the preferred height and width from the criteria
        dialog = $(dlgHtml).perc_dialog({
            title:I18N.message("perc.ui.asset.edit.dialog@Edit Widget Content"), 
            resizable:true, modal:true, 
            height:contentEditCriteria.preferredEditorHeight, 
            width:contentEditCriteria.preferredEditorWidth, 
             open: function() { 
           $('#edit-widget-content').height($(this).parent().height()-120); 
                           $('#edit-widget-content').width($(this).parent().width()); 
                           $('#edit-widget-content-frame').height($(this).parent().height()-120); 
                           $('#edit-widget-content-frame').width($(this).parent().width()-3); 
                           maximize = $( '<a id="ui-dialog-titlebar-resize" class="ui-dialog-titlebar-resize ui-corner-all" role="button" aria-label="full screen" href="#"><i class="icon-resize-full"/></a>');
                           maximized=false;
                           $(this).parent().find('.ui-dialog-title').after(maximize)
                           
                      

                           

              }, 
        resize: function() {
    
          $('#edit-widget-content-frame').hide();
    
        }, 
        resizeStop: function() { 
      
          $('#edit-widget-content-frame').show(); 

           $('#edit-widget-content').height($(this).parent().height()-120); 
                           $('#edit-widget-content').width($(this).parent().width()); 
                           $('#edit-widget-content-frame').height($(this).parent().height()-120); 
                           $('#edit-widget-content-frame').width($(this).parent().width()-5); 
                  
         },  
            percButtons:{   
                "Save": {
                    click: function()   {
                        //Adam Gent STORY-93
                        //openSaveAs(assetid, widgetid, widgetdefid, dialog);
                        saveAssetContent(contentEditCriteria.contentName,null);
                        $.PercDirtyController.setDirty(false);
                    },
                    id: "perc-content-edit-save-button"
                },
                "Cancel":   {
                    click: function(event)   {
                        if (event.originalEvent.detail === 0) { // if cancle button enabled through tab
                            var options = {
                                id: 'perc-finder-delete-confirm',
                                title: I18N.message("perc.ui.closeEditor.title@Close Content Editor"),
                                question:I18N.message("perc.ui.closeEditor.question@Close Content Editor"),
                                success: function () {
                                    closeContentEditor();
                                },
                                yes: I18N.message("perc.ui.common.label@OK")
                            };
                            ut.confirm_dialog(options);
                        } else {
                            closeContentEditor();
                        }
                    },
                    id: "perc-content-edit-cancel-button"
                 }          
            },      
            id:'perc-asset-edit-dialog'
        });

        function resizer(){
          console.log("Resized")
           clearTimeout($.data(this, 'resizeTimer'));
        $.data(this, 'resizeTimer', setTimeout(function() {
           console.log("Resize timer")
             offset = $('#edit-widget-content').parent().offset()
          
             console.log("left="+offset.left+" top=",offset.top);
             if (offset.left==0 && offset.top==0 && maximized)
             {
             dialog.dialog("option","draggable", false );
             dialog.dialog("option","resizable", false );
             
             $('#edit-widget-content').parent().height($( window ).height());  
             $('#edit-widget-content').height($( window ).height()-120);  
                 $('#edit-widget-content').parent().width($( window ).width()-5); 
                 $('#edit-widget-content-frame').height($( window ).height()-120); 
                 $('#edit-widget-content-frame').width($( window ).width()-10); 
              } else 
              {
                 clearTimeout($.data(this, 'resizeTimer'));
              }
        }, 200));
            
        }

          dialog.parent().find( '.ui-dialog-titlebar-resize' ).on("click", 
         
          function(){
            dialog_frame = dialog.parent();
            
            if (maximized)
            {
                dialog_frame.animate({
                          top: saved_top,
                          left: saved_left,
                          height: saved_height,
                          width: saved_width,
                   }, 200, function() {
                            $('#edit-widget-content').height(saved_height-120); 
                           $('#edit-widget-content').width(saved_width); 
                           $('#edit-widget-content-frame').height(saved_height-120); 
                           $('#edit-widget-content-frame').width(saved_width-3); 
                             $(this).find(".icon-resize-small").addClass("icon-resize-full").removeClass('icon-resize-small');
                            $(this).find(".ui-dialog-titlebar-resize").attr('aria-label',"exit full screen");
                            dialog.dialog("option","draggable", true );
                             dialog.dialog("option","resizable", true );
                            $(window).off('resize',resizer);
                            maximized=false;
                          });

             
            } else {
              var o = dialog_frame.offset();
              saved_top=o.top;
              saved_left=o.left;
              saved_width=dialog_frame.width();
              saved_height=dialog_frame.height();

              dialog_frame.animate({
                          top:0,
                          left:0,
                          height: $(window).height(),
                          width: $(window).width()-15,
                          
                   }, 200,  function() {
                           $('#edit-widget-content').height($(this).parent().height()-120); 
                           $('#edit-widget-content').width($(this).parent().width()); 
                           $('#edit-widget-content-frame').height($(this).parent().height()-120); 
                           $('#edit-widget-content-frame').width($(this).parent().width()-5); 
                             $(this).find(".icon-resize-full").addClass("icon-resize-small").removeClass('icon-resize-full');
                         
                            $(this).find(".ui-dialog-titlebar-resize").attr('aria-label',"full screen");
                            dialog.dialog("option","draggable", false );
                            dialog.dialog("option","resizable", false );
                            maximized=true;
                          });
       
              $(window).trigger("resize",resizer);
            }
          });

        // If we are editing a widget that does not produce shared content by default, and no 
        // assetId is specified, then we are editing or creating local content for a widget.
        // Also, check if the create_share_asset widget preference is true.
        // Attach the "Share" checkbox to the dialog at the same level of its buttons
        if (contentEditCriteria.producesResource === false &&
            contentEditCriteria.assetType !== 'shared' &&
            contentEditCriteria.createSharedAsset === true)
        {
            var checkboxHtml = '<div class="perc-content-edit-share">';
            checkboxHtml += '<form action="">';
            checkboxHtml += '<input type="checkbox" id="perc-content-edit-share-checkbox" name="perc-content-edit-share-checkbox" />';
            checkboxHtml += '<label for="perc-content-edit-share-checkbox">' + I18N.message('perc.ui.saveasdialog.label@Shared Asset') + '</label>';
            checkboxHtml += '</form>';
            checkboxHtml += '</div>';
            dialog.parent().find( '.ui-dialog-buttonpane' ).append( checkboxHtml );
        }

         //We are now loading the form, the clear the handlers.
        $.PercContentPreSubmitHandlers.clearHandlers();

        // Bind the frame load to execute different methods based on whether the frame is loaded initially or through form submit.
        $("#edit-widget-content-frame").load(function()
        {
            if(initialized == false){
                initialized = true;
                onIntialFrameLoad();
            }
            else{
                onLaterFrameLoads();
            }
        });

         /*
          *Set the basic data on the top level div of the dialog to make it accessible for editors.
          */
         var ediv = $("#edit-widget-content").get(0);
         $.data(ediv, "pageid", parentId);
         $.data(ediv, "assetid", assetid);
         $.data(ediv, "widgetData", widgetData);
      }

       /**
        * This method gets called  on clicking cancle button of content editor
        */
       function closeContentEditor(){
           $.PercBlockUI();
           cancelCallback(assetid);
           dialog.remove();
           $.unblockUI();
           $.PercDirtyController.setDirty(false);
       }
      
      /**
       * This method gets called from frame load binding, it gets called only at the time of initial load
       * to set some initialization settings. 
       */
      function onIntialFrameLoad(){
        if(contentEditCriteria.assetType == "shared")
        {
            // show the warning message for shared asset
            var warning = 'This is a shared Asset.  Your edits will take effect on all pages that use this Asset.';
            var $topPlaceHolder = $contentFrameElem("#perc-content-edit-content-top-placeholder");
            $topPlaceHolder.addClass("perc_shared_asset_edit_warning perc-asset-edit-info-line");
            $topPlaceHolder.html(warning);
            $topPlaceHolder.show();
            
            // add the folder path for an existing asset
            if (assetId != undefined && assetId != '')
            {
                var pathField = '<label for="perc-content-edit-meta-data-item_folder_path" >Folder path:</label> <br/>' + 
                    '<div id="perc-content-edit-meta-data-item_folder_path" class="datadisplay">' +
                    contentEditCriteria.folderPath + '</div> <br/>';

                $contentFrameElem("#perc-content-edit-meta-data-top-placeholder").html(pathField);
                $contentFrameElem("#perc-content-edit-metadata-panel").show();
                //Render site impact for the asset.
                $contentFrameElem("#perc-site-impact-panel").show();
                $.PercSiteImpactView.renderSiteImpact(assetId, $.PercSiteImpactView.ITEM_TYPE_ASSET, $contentFrameElem("#perc-site-impact-panel"));
            }
        }
      }
      /**
       * Opens the save as dialog if asset id is empty otherwise calls the save asset content.
       */
      function openSaveAs() {
         var ediv = $("#edit-widget-content").get(0);
         if ($.data(ediv, "assetid")) {
            saveAssetContent(null,null);
         } else {
            if (contentEditCriteria.producesResource) {
               $.perc_browser({on_save:onSave, initial_val:""});
            } else {
               saveAssetContent(contentEditCriteria.contentName,null);
            }
         }
      }

      /**
       * Call back function gets called from the save as dialog.
       * Shows an error if the asset already exists, otherwise calls the 
       * saveAssetContent with name and folder id
       * @param path folder path of selected folder
       * @name name Name of the asset
       * @param k call back function to be called on success
       * @param err The error function to be called if there is an error
       */
      function onSave(path, name, k, err) {
         $.perc_pathmanager.open_containing_folder(path, function (folder_spec) {
            var sames = $.grep(folder_spec["PathItem"], function (x) {
               return x.name == name;
            });
            if (sames.length) {
               err(I18N.message("perc.ui.saveasdialog.error@Duplicate asset"));
            } else {
               $.perc_pathmanager.open_path(ut.acop(path), false, function (fs) {
                  saveAssetContent(name, fs["PathItem"]["folderPath"]);
                  k();
               }, err);
            }
         }, err);
      }

      /**
       * Submits the form content to the server by using the ajax form.
       * Sets the iframe option to true to submit it as multi part form data.
       * On success the frame gets reloaded, onLaterFrameLoads method gets executed after the frame reload.
       * onLaterFrameLoads is bound to frame load method.
       * @param name Name of the asset, may be null
       * @param folderPath the path of the folder to which the asset needs to be added.
       * May be null.
       */
      function saveAssetContent(name, folderPath) {
         $.PercBlockUI();
         var path = contentEditCriteria.url;
         $.data($("#edit-widget-content").get(0), "folderPath", folderPath);
         path = path.split("?")[0];
         if (name && name != "") {
            $contentFrameElem("#perc-content-edit-sys_title").val(name);
         }
         var ediv = $("#edit-widget-content").get(0);
         if(!($.data(ediv, "assetid")) && contentEditCriteria.workflowId)
         {
            $contentFrameElem("#perc-content-edit-sys_workflowid").val(contentEditCriteria.workflowId);
         }
         
         /*
          * See PSAddNewItemToFolder.java
          * It expects psredirect to have the folder id.
          */
         $.perc_utils.debug(I18N.message("perc.ui.asset.edit.dialog@Doing A Save Asset"));
         $.perc_utils.debug(contentEditCriteria);
         if (contentEditCriteria.producesResource) {
             var fid = contentEditCriteria.legacyFolderId;
             path = path + "?sys_folderid=" + fid + "&sys_asset_folderid=" + fid;
         }
         if (!$contentFrameElem('#perc-content-form').attr('action').includes('sys_asset_folderid')) {
            $contentFrameElem("#perc-content-form").attr("action", path);
         }
         //call all the pre submit handlers if nothing returns flase, submit the form.
         var dosubmit = true;
         $.each($.PercContentPreSubmitHandlers.getHandlers(),function(){
            if(!this()){
                dosubmit = false;
            }
         });

         var frameToCheckForMandatoryField = $("#edit-widget-content-frame");
          var showMandatoryFieldAlertPopUp = false;
          showMandatoryFieldAlertPopUp = $.perc_utils.checkMandatoryFieldsEmpty(frameToCheckForMandatoryField);
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

          //we are checking for valid user session, to check if user has logged out from another tab in the browser.
          $.perc_utils.checkValidUserSession(
              function(data, textStatus){
                    location.reload();
                  },
              function(data, textStatus){
                    $("#edit-widget-content-frame").contents().find("#perc-content-form").trigger("submit");
              }
          );

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
      function onLaterFrameLoads() {
         if ($("#edit-widget-content-frame").contents().find("#perc-content-edit-errors").length > 0) {
            $.unblockUI();
            return;
         }
         var ediv = $("#edit-widget-content").get(0);
         var assetid = $.data(ediv, "assetid");
         var widgetData = $.data(ediv, "widgetData");
         var folderPath = $.data(ediv, "folderPath");
         var producesResource = contentEditCriteria.producesResource;
         //Create a relation ship if there is no asset id
         if (!assetid || assetid == "") {
            assetid = "-1-101-" + $("#edit-widget-content-frame").contents().find("[name=sys_contentid]").val();
            $.PercAssetService.set_relationship(assetid, widgetData, parentId, "1", producesResource, folderPath, function () {
               reload_callback(assetid, producesResource);
               $.unblockUI();
            }, function(request){
                  $.unblockUI();
                  var defaultMsg = 
                     $.PercServiceUtils.extractDefaultErrorMessage(request);
                  $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: defaultMsg});
            });
         } else {
            $.PercAssetService.updateAsset(parentId, assetid, function() {
                reload_callback(assetid,producesResource);
                $.unblockUI();
            });
         }
         setTimeout(removeEditAssetDialog, 500);
      }

        /**
         *  This fixes a bug where the asset dialog complains that
         *  tinyMCE has not yet loaded when it returns from the form
         *  submission and it is removed before it is given time to fully load
         */
        function removeEditAssetDialog() {
            var openSaveAsSharedAsset = dialog.parent().find( '#perc-content-edit-share-checkbox:checked' );
            var sharedChecked = openSaveAsSharedAsset.length > 0;

            $("#edit-widget-content-frame").remove();
            $("#edit-widget-content").remove();

            // Open the Save As Shared Asset if the Share checkbox is checked 
            if ( sharedChecked ) {
                // We use the variables in the scope to pass the needed data.
                // assetId could be null when creating the new content, the dialog's code will
                // select it using jQuery before saving.
                $.perc_save_as_shared_asset_dialog.createDialog({
                    'widgetData' : widgetData,
                    'assetId' : assetId,
                    'parentId' : parentId,
                    'onSave' : function(result) {
                        // If we saved something using the Save As Shared Asset dialog, refresh 
                        // content editor
                        reload_callback( result, false );
                    }  
                });
            }
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
            if (frames[i].frameElement.name == "edit-widget-content-frame") {
               contentFrameDoc = frames[i].document;
               break;
            }
         }
         return contentFrameDoc;
      }
   };
})(jQuery);
