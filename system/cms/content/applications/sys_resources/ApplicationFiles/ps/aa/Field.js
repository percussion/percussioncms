/******************************************************************************
 *
 * [ ps.aa.field.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.aa.Field");

dojo.require("ps.aa");
dojo.require("dojo.event");
dojo.require("ps.io.Actions");
dojo.require("dojo.widget.Manager");

/**
 * The field object. Manages the field editing.
 */
ps.aa.Field = function()
{
    this.objectId = null;
    this.refreshField = false;
    this.psCeFieldWindow = null;
    this.fieldModalDlg = null;
    this.renderer = null;
    this.ceUrl = null;
    this.divElem = null;
    this.inplaceEditing = null;
    
   /**
    * Initializion of Field object.
    */
   this.init = function()
   {
   }
   
   /**
    * Creates the field modal dialog if it is not created yet.
    * Stores the dialog in the {@link #fieldModalDlg} field.
    */
   this.maybeCreateFieldModalDlg = function ()
   {
      if (this.fieldModalDlg)
      {
         return;
      }

      this.fieldModalDlg = ps.createDialog(
            {
               id: "ps.Field.FieldEditingDlg",
               title: "Edit Field"
            }, "200px", "100px");
      
      var _this = this;

      //override the dialog close function
      // to not destroy the dialog
      this.fieldModalDlg.closeWindow = function()
      {
         _this.fieldModalDlg.hide();
      }
      dojo.event.connect(this.fieldModalDlg, "onLoad", function()
      {
         _this.parseControls();
      });
   }

   /**
    * Creates the inplace editing dialog if it is not created yet.
    * Stores the dialog in the {@link #inplaceDlg} field.
    */
   this.maybeCreateInplaceDlg = function ()
   {
      if (this.inplaceDlg)
      {
         // already created
         return;
      }

      var div = document.createElement('div');
      div.style.position = "absolute";
      div.style.border = "0px";
      document.body.appendChild(div);

      this.inplaceDlg = dojo.widget.createWidget("ModalFloatingPane",
            {
               id: "ps.field.inplaceTextBoxDiv",
               titleBarDisplay: false,
               bgColor: ps.DIALOG_BACKGROUND,
               bgOpacity: ps.DIALOG_BACKGROUND_OPACITY,
               executeScripts: true,
               resizable: false
             }, div);
      this.inplaceDlg.setContent(
            '<input type="text" style="border:0px; padding:0px; margin-top:1px"'
               + 'size="50" id="ps.field.inplaceTextBox" '
               + 'name="ps.field.inplaceTextBox"/>\n'
            + '<div class="PsAaFieldButtonsbox">\n'
            + '<table align="center" width="100%" border="0">\n'
            + '<tr>\n'
            + '<td align="right">\n'
            + '<button dojoType="Button" id="ps.field.inplaceUpdateButton">'
               + 'Update</button>\n'
            + '</td>\n'
            + '<td align="left">\n'
            + '<button dojoType="Button" id="ps.field.inplaceCancelButton">'
               + 'Cancel</button>\n'
            + '</td>\n'
            + '</tr>\n'
            + '</table>\n'
            + '</div>');

      this.inplaceTextBox = dojo.byId("ps.field.inplaceTextBox");
      dojo.event.connect(
            this.inplaceTextBox, "onkeyup", this, "_onInplaceTextTyped");
      var updateButton = dojo.widget.byId("ps.field.inplaceUpdateButton");
      dojo.lang.assert(updateButton, "Update button could not be found");
      var cancelButon = dojo.widget.byId("ps.field.inplaceCancelButton");
      dojo.lang.assert(cancelButon, "Cancel button could not be found");

      dojo.event.connect(updateButton, "onClick", this, "updateField");
      dojo.event.connect(cancelButon, "onClick", this, "onInplaceCancel");
   }
   
   /**
    * Helper method called by init() to parse all the controls and connect 
    * events.
    */
   this.parseControls = function()
   {
      this.wgtButtonFullEditor = dojo.widget.byId("ps.Field.wgtButtonFullEditor");
      this.wgtButtonUpdate = dojo.widget.byId("ps.Field.wgtButtonUpdate");
      this.wgtButtonClose = dojo.widget.byId("ps.Field.wgtButtonClose");
      //Handle the buttons
      this.divRegularButtons = dojo.byId("psRegularButtons");
      this.divDojoButtons = dojo.byId("psDojoButtons");
      this.divRegularButtons.style.visibility = "hidden";
      this.divDojoButtons.style.visibility = "visible";

      dojo.event.connect(this.wgtButtonFullEditor, "onClick", this, "openFullEditor");
      dojo.event.connect(this.wgtButtonUpdate, "onClick", this, "updateField");
      dojo.event.connect(this.wgtButtonClose, "onClick", this, "_onDialogClose");
      var edfrm = dojo.byId("EditForm");
      edfrm.setAttribute("onsubmit","");
      var ceurl = this.ceUrl.split("?")[0];
      var suburl = ps.io.Actions.getUpdateItemUrl() + "&ceUrl=" + encodeURI(ceurl);
      ps.io.Actions.initFormBind(suburl,"EditForm",ps.io.Actions.MIMETYPE_JSON);
      this.initialCheckSum = ps_getAllFieldChecksums(document.EditForm,true);
   }

   /**
    * Dialog close.
    */
   this._onDialogClose = function()
   {
      var finalCheckSum =  ps_getAllFieldChecksums(document.EditForm,false);
      if(finalCheckSum != this.initialCheckSum)
      {
         if(confirm(this.FORM_CHANGE_WARNING_FOR_CLOSING))
         {
            this.updateField();
         }
      }
      this.fieldModalDlg.hide();
   }

   /**
    * Function to open the full editor.
    */
   this.openFullEditor = function()
   {
      var finalCheckSum =  ps_getAllFieldChecksums(document.EditForm,false);
      if(finalCheckSum != this.initialCheckSum)
      {
         if(confirm(this.FORM_CHANGE_WARNING_FOR_FULLEDITOR))
         {
            this.updateField();
         }
      }
      ps.aa.controller.editAll();
      this.fieldModalDlg.hide();
   }
       
   /**
    * Function to edit the filed
    * Checks whether alt key is pressed or not if it is, then simply returns true.
    * Calls controller to activate the element.
    * Calls content editor url server action to get the content editor url
    * Opens the editor dialog with the URL.
    */
   this.editField = function(divElem, e)
   {
      if(this.checkClickEvent(e))
         return true;
      ps.aa.controller.activate(divElem);
      //From the div element get the new objectid
      var newObjId = ps.aa.Page.getObjectId(divElem);
      //Check whether the item is checked out or not
      if(newObjId.isCheckoutByMe() == 0)
      {
         alert(ps.aa.controller.CHECKOUT_MSG);
         return false;
      }
      
      //If a full editor window is open for editing an item, propmt user
      //whether he wants to open this field and cancel editing of the item?
      if(ps.aa.controller.psCeWindow && !ps.aa.controller.psCeWindow.closed)
      {
         if(!confirm(ps.aa.controller.EDITOROPEN_MSG))
         {
            ps.aa.controller.psCeWindow.focus();
            return false;
         }
         ps.aa.controller.psCeWindow.close();
      }
      //If a field editor window is open for editing a field, propmt user
      //whether he wants to open this field and cancel editing of the field?
      else if(this.psCeFieldWindow && !this.psCeFieldWindow.closed)
      {
         if(newObjId.equals(this.objectId) || !confirm(ps.aa.controller.EDITOROPEN_MSG))
         {
            this.psCeFieldWindow.focus();
            return false;
         }
         this.psCeFieldWindow.close();
      }
      //If a field is opened for inplace editing prompt user
      //whether he wants to open this field and cancel editing of the inplace field?
      else if(this.inplaceEditing)
      {
         if(!confirm(ps.aa.controller.INPLACE_EDITOROPEN_MSG))
            return false;
         this.onInplaceCancel();
      }
      
      this.objectId = ps.aa.controller.activeId;
      this.divElem = divElem;
      ps.aa.controller.editObjectId = this.objectId;
      //Get the content editor url and open a dialog with it.
      var response = ps.io.Actions.getUrl(this.objectId,"CE_FIELDEDIT");
      // handle failure         
      if (!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return false;
      }
      var value = response.getValue();
      dojo.lang.assert(dojo.lang.has(value, "url"));
      this.ceUrl = value.url;
      
      var dlgw = value.dlg_width?value.dlg_width:this.DEFAULT_CONTROL_WIDTH;
      var dlgh = value.dlg_height?value.dlg_height:this.DEFAULT_CONTROL_HEIGHT;
      this.renderer = value.aarenderer?value.aarenderer:this.DEFAULT_FIELD_RENDERER;

      if(this.renderer == this.FIELD_RENDERER_NONE)
      {
         if(confirm(this.FIELD_RENDERER_NONE_MESSAGE))
         {
            ps.aa.controller.editAll();
         }
         return;
      }
      else if(this.renderer == this.FIELD_RENDERER_MODAL)
      {
         this.maybeCreateFieldModalDlg();
         this.fieldModalDlg.setUrl(this.ceUrl);
         this.fieldModalDlg.show();
         this.fieldModalDlg.resizeTo(dlgw,dlgh);
      }
      else if(this.renderer == this.FIELD_RENDERER_INPLACE_TEXT)
      {
         var response = ps.io.Actions.getContentEditorFieldValue(this.objectId);
         if(!response.isSuccess())
         {
            ps.io.Actions.maybeReportActionError(response);
            return false;
         }
         this.onInplaceEdit(response.getValue());
      }
      else
      {
         var wstyle = "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=" + dlgw + ",height=" + dlgh ;
         this.psCeFieldWindow = window.open(this.ceUrl,ps.aa.controller.CE_EDIT_ITEM_WINDOW,wstyle);
         this.psCeFieldWindow.focus();
         return false;
      }
   },

   /**
    * Utility method to check the click event and returns true if any of the
    * control or shift or alt keys are pressed. It also stops the propagation 
    * of the Event.
    */
   this.checkClickEvent = function(e)
   {
      if(e)
      {
         //For ctrl and shift keys simply return
         if(e.ctrlKey || e.shiftKey)
            return true;
         //Stop the propagation of event
         dojo.event.browser.stopEvent(e);
         //Handle the alt key clicks
         if(e.altKey)
         {
            var tgt = e.target;
            if(typeof(tgt) == "undefined")
               tgt = e.srcElement;
            if (dojo.html.isTag(tgt, 'a'))
            {
               window.location.href = tgt.href;
               return true;
            }
            else if(dojo.html.isTag(tgt, 'div'))
            {
               var fieldLink = dojo.html.getAttribute(tgt, "fieldLink");
               if(typeof(fieldLink) != "undefined" && fieldLink.length > 0)
               {
                  window.location.href = fieldLink;
                  return true;
               }
            }
            return true;
         }
      }
      return false;
   }
   
   /**
    * Is called when on onkeyup event for the inline editing text box.
    * Handles Enter and Esc keys in this control.
    */
   this._onInplaceTextTyped = function(e)
   {
      e_v = e;
      if (e.ctrlKey || e.shiftKey || e.altKey || e.metaKey)
      {
         return;
      }
      if (e.keyCode === e.KEY_ENTER)
      {
         this.updateField();
      }
      else if (e.keyCode === e.KEY_ESCAPE)
      {
         this.onInplaceCancel();
      }
   }

   /**
    * Cancels inplace editing
    */
   this.onInplaceCancel = function()
   {
      this.inplaceEditing = false;
      this.inplaceDlg.hide();
      dojo.html.show(this.divElem);
   }

   /**
    * Opens the field for inplace editing.
    * @param {String} value the initial textbox value.
    */
   this.onInplaceEdit = function (value)
   {
      this.inplaceEditing = true;
      this.maybeCreateInplaceDlg();
      this.inplaceTextBox.value = value;

      var dn = this.inplaceDlg.domNode;
      
      // place the editor over the editing element
      var elemPos = dojo.html.getAbsolutePosition(this.divElem);
      var dlgPadding = 0;
      dn.style.left = (elemPos.left - dlgPadding) + "px";
      dn.style.top = (elemPos.top - dlgPadding) + "px";

      var width = dojo.html.getBorderBox(this.divElem).width;
      this.inplaceTextBox.style.width = width + "px";

      // to disable automatic dialog placement at the center of the page
      this.inplaceDlg.placeModalDialog = function () {};

      this.inplaceDlg.show();

      if (!this.inplaceDlgHeight)
      {
         this.inplaceDlgHeight =
             dojo.html.getBorderBox(this.inplaceDlg.domNode).height + 10;
      }
      this.inplaceDlg.resizeTo(width, this.inplaceDlgHeight);
      this.inplaceTextBox.focus();

      dojo.html.hide(this.divElem);
   }

   /**
    * Updates the fields and refreshes the page. 
    * If the renderer is POPUP the update takes place in popup window, we
    * just refresh the page. 
    * If the renderer is MODAL dialog box then we submit the form and refresh 
    * the page if the submission succeeds.  
    */
   this.updateField = function()
   {
      if(this.renderer == this.FIELD_RENDERER_MODAL)
      {
         var response = ps.io.Actions.submitForm(document.EditForm);
         if(!response.isSuccess())
         {
            ps.io.Actions.maybeReportActionError(response);
            return false;
         }
         var value = response.getValue();
         //Check for the cmsErrors first and warn the user about it.
         if(dojo.lang.has(value, "cmsError"))
         {
            alert(value.cmsError);
            return false;         
         }
         else if(dojo.lang.has(value, "validationError"))
         {
            if(!confirm(value.validationError + this.FIELD_VALIDATION_CONFIRM_MSG_PART2))
               return false;
            this.fieldModalDlg.hide();
            ps.aa.controller.editAll(value.ceCachedPageUrl);
            return false;
         }
         else
         {
            this.initialCheckSum = ps_getAllFieldChecksums(document.EditForm,true);
         }
      }
      else if(this.renderer == this.FIELD_RENDERER_INPLACE_TEXT)
      {
         var response = ps.io.Actions.setContentEditorFieldValue(this.objectId,this.inplaceTextBox.value);
         if(!response.isSuccess())
         {
            ps.io.Actions.maybeReportActionError(response);
            return false;
         }
         var value = response.getValue();
         //Check for the cmsErrors first and warn the user about it.
         if(dojo.lang.has(value, "cmsError"))
         {
            alert(value.cmsError);
            return false;         
         }
         else if(dojo.lang.has(value, "validationError"))
         {
            if(!confirm(value.validationError + this.FIELD_VALIDATION_CONFIRM_MSG_PART2))
               return false;
            this.onInplaceCancel();
            ps.aa.controller.editAll();
            return false;
         }
         this.onInplaceCancel();
      }
      ps.aa.controller.refreshFieldsOnPage(this.objectId.getContentId(),this.objectId.getFieldName(),this.psCeFieldWindow);
   }
   
   /**
    * Constant for defualt field height for the controls
    */
   this.DEFAULT_CONTROL_HEIGHT = 300;
   
   /**
    * Constant for default field width for the controls
    */
   this.DEFAULT_CONTROL_WIDTH = 400;
   
   /**
    * Constant for defualt field renderer
    */
   this.DEFAULT_FIELD_RENDERER = this.FIELD_RENDERER_POPUP;
   
   /**
    * Constant for field renderer modal dialog box
    */
   this.FIELD_RENDERER_MODAL = "MODAL";
   
   /**
    * Constant for field renderer popup dialog box
    */
   this.FIELD_RENDERER_POPUP = "POPUP";
   
   /**
    * Constant for field renderer none
    */
   this.FIELD_RENDERER_NONE = "NONE";

   /**
    * Constant for field renderer inplace text box
    */
   this.FIELD_RENDERER_INPLACE_TEXT = "INPLACE_TEXT";
   
   /**
    * Constant for field validation error confirmation message part2.
    */
   this.FIELD_VALIDATION_CONFIRM_MSG_PART2 = "\nClick OK to open the full content editor or Cancel to continue editing.";
   
   /**
    * Constant for form change warning when user clicks close button.
    */
   this.FORM_CHANGE_WARNING_FOR_CLOSING = "Changes have been made.\nDo you want to save before closing?"; 

   /**
    * Constant for form change warning when user clicks close button.
    */
   this.FORM_CHANGE_WARNING_FOR_FULLEDITOR = "Changes have been made.\nDo you want to save before opening full editor?";
   
   /**
    * Constant for message shown when the field renderer is none.
    */ 
    this.FIELD_RENDERER_NONE_MESSAGE = "The source of the data comes from a hidden field.\nIt can probably be modified by editing another field in the content item.\n\nClick OK to open the full editor.";

};

