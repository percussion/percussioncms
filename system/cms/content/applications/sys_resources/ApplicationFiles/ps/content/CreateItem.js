/******************************************************************************
 *
 * [ ps.content.CreateItem.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 dojo.provide("ps.content.CreateItem");
 
 dojo.require("ps.io.Actions");
 dojo.require("ps.widget.ContentPaneProgress");
 dojo.require("ps.widget.PSImageGallery");
 
/**
 * Create new item dialog.
 */
ps.content.CreateItem = function()
{
   this.preferredHeight = 640;
   this.preferredWidth = 570;
 
   /**
    * Initializes this dialog. Should only call once.
    * @param (string} url the url of the content that this
    * dialog will display.
    */
   this.init = function(url)
   {
      this.url = url;
   },
   
   /**
    * Creates the new item dialog if it is not created yet.
    * Stores the dialog in the {@link #wgtDlg} field.
    */
   this._maybeCreateCreateItemDialog = function ()
   {
      if (this.wgtDlg)
      {
         return;
      }
      var dlgTitle = "Create Item";
      this.wgtDlg = ps.createDialog(
            {
               id: "ps.content.newItemDlg",
               title: dlgTitle,
               href: this.url
            }, this.preferredWidth, this.preferredHeight, false);

      new ps.widget.ContentPaneProgress(this.wgtDlg);

      var _this = this;
      //override the dialog close function
      // to not destroy the dialog
      this.wgtDlg.closeWindow = function()
      {
         _this.cancelCallback();
         this.hide();
      }     
 
      dojo.event.connect(this.wgtDlg, "onLoad", function()
      {
         _this._parseControls();
      });
   },

   /**
    * Retrieves the controls from the main content and connects
    * the necessary events and functions.
    */
   this._parseControls = function()
   {
      var _this = this;
      //Controls
      this.wgtButtonSelect = dojo.widget.byId("ps.createitem.wgtButtonSelect");
      this.wgtButtonCancel = dojo.widget.byId("ps.createitem.wgtButtonCancel");
      this.wgtTemplateGallery = dojo.widget.byId("ps.createitem.templateGallery");
      this.selectContentTypes = dojo.widget.byId("ps.createitem.contentType");
      this.itemTitle = dojo.byId("ps.createitem.itemTitle");
      this.folderPath = dojo.byId("ps.createitem.folderPath");
      //Connect events
      this.wgtButtonCancel.onClick = function()
      {
         _this.cancelCallback();
         _this.wgtDlg.hide();
      };
      
      this.wgtButtonSelect.onClick = function()
      {
         _this._onOK();
      };
      this.selectContentTypes.onValueChanged = function(value)
      {
         _this._initTemplateImages(value);
      };

      //Set the data
      this._initContentType();
      this.selectContentTypes.textInputNode.setAttribute("readonly","true");
      this._initPaths();
   } ,

   /**
    * Hanldes Create new item click.
    * Validates the data before calling the OK call back function.
    */
   this._onOK = function()
   {
      var ctid = this.selectContentTypes.getValue();
      var tempid = this.contentTypeImageData[ctid].templateIds[
         this.wgtTemplateGallery.imageIndex];
      var fpath = this.folderPath.value;
      var ititle = this.itemTitle.value;
      if(ps.util.trim(ititle).length<1)
      {
         alert("Title must not be empty.");
         return;
      }
      if(this.slotId && !tempid)
      {
         alert("Please select a template to insert into the slot."); 
         return;           
      }
      var response = ps.io.Actions.getIdByPath(fpath+"/"+ititle);
      if (response.isSuccess())
      {
         alert("Title should be unique under the specified folder.\n" + fpath );
         return;
      }
      var newData = {"sys_contenttypeid":ctid,"sys_templateid":tempid,
        "folderPath":fpath,"itemPath":null,"itemTitle":ititle};
      this.okCallback(this.slotId,this.itemId,this.position,newData);
      this.wgtDlg.hide();
   }   

   /**
    * Initializes the content type combo to the content type of itemId.
    * If itemId is null then defaults to the first item in the combo.
    */
   this._initContentType = function()
   {
      if(this.itemId == null)
      {
         this._initTemplateImages(this.selectContentTypes.getValue());
         return;
      }
      var ctid = this.itemId.getContentTypeId();
      var dataArray = this.selectContentTypes.dataProvider._data;
      for(var i = 0; i < dataArray.length; i++) 
      {
         if(dataArray[i][1] == ctid) 
         {
            this.selectContentTypes.setAllValues(dataArray[i][0], dataArray[i][1]);
         }
      }
   },
   
   /**
    * Initializes the template image gallery control by getting the data from
    * Server and preparing the data object to pass it to image gallery control.
    * @param (int) Content type id. The templates are defaulted to the supplied
    * content type id.
    */
   this._initTemplateImages = function(ctid)
   {
      this.wgtTemplateGallery.reset();
      if(this.contentTypeImageData != null && this.contentTypeImageData[ctid])
      {
         this.wgtTemplateGallery.setImages(this.contentTypeImageData[ctid].thumbUrls,
            this.contentTypeImageData[ctid].fullUrls,
            this.contentTypeImageData[ctid].imgTitles);
         return;
      }
      var objId = this.slotId==null?this.itemId:this.slotId;
      var response = ps.io.Actions.getTemplateImagesForContentType(ctid,objId);
      // handle failure
      if (!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }
      var tmpImages = response.getValue();
      var templateIds = new Array();
      var thumbUrls = new Array();
      var fullUrls = new Array();
      var imgTitles = new Array();
      var selectedIndex = 0;
      for(var i=0;i<tmpImages.length;i++)
      {
         if(this.itemId != null && 
           this.itemId.getTemplateId()==tmpImages[i].templateId)
         {
            selectedIndex = i;            
         }
         templateIds[i] = tmpImages[i].templateId +"";
         thumbUrls[i] = __rxroot  + "/" +  tmpImages[i].thumbUrl;
         fullUrls[i] = __rxroot + "/" + tmpImages[i].fullUrl;
         imgTitles[i] = tmpImages[i].templateName+"";
      }
      //Store it locally
      if(this.contentTypeImageData==null)
         this.contentTypeImageData = new Array();
      this.contentTypeImageData[ctid] = {"templateIds":templateIds,
        "thumbUrls": thumbUrls, "fullUrls": fullUrls, "imgTitles":imgTitles};
      this.wgtTemplateGallery.setImages(thumbUrls,fullUrls,imgTitles);
      this.wgtTemplateGallery.showImage(selectedIndex);
   },
   
   /**
    * Initializes the Folder Path with the folder path of the content item 
    * represented by page.
    */
   this._initPaths = function()
   {
      var objId = ps.aa.controller.treeModel.getRootNode().getObjectId();
      var response = ps.io.Actions.getItemPath(objId);
      // handle failure
      if (!response.isSuccess())
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }
      var path = response.getValue();
      this.folderPath.value=path.substring(0,path.lastIndexOf("/"));
      dojo.html.setStyle(this.folderPath,"background-color","ThreeDFace");
   },

   /**
    * Resets the content url and displays the dialog.
    * @param {function} okCallback the callback function that will be
    * called if the select button is clicked and a selection exists.
    * @param {function} cancelCallback  the callback function that will be
    * called if the cancel button is clicked.
    * @param {ps.aa.ObjectId} the objectid of the snippet in question
    * with its slot modified to be the target slotid.
    */
   this.open = function(okCallback, cancelCallback, slotId, itemId, position)
   {
      this.slotId = slotId;
      this.itemId = itemId;
      this.position = position;
      this.okCallback = okCallback;
      this.cancelCallback = cancelCallback;
      var newUrl = this.url;
      if(slotId!=null)
        newUrl += "?objectId=" + escape(slotId.serialize());
      this.contentTypeImageData = null;
      this._maybeCreateCreateItemDialog();
      this.wgtDlg.setUrl(newUrl);
      ps.util.setDialogSize(this.wgtDlg, this.preferredWidth, 
        this.preferredHeight);
      this.wgtDlg.show();
   }
 }