/******************************************************************************
 *
 * [ ps.content.SelectTemplates.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 dojo.provide("ps.content.SelectTemplates");
 
 dojo.require("ps.io.Actions");
 dojo.require("ps.widget.ContentPaneProgress");
 
 /**
  * The javascript code needed to make the select templates
  * dialog work.
  */
 ps.content.SelectTemplates = function()
 {
 	this.isAsDialog = true;
 	this.preferredHeight = 450;
    this.preferredWidth = 750;
    this.parentMode = "";
 
   /**
    * Initializes this dialog. Should only call once.
    * @param (string} url the url of the content that this
    * dialog will display.
    */
   this.init = function(url)
   {
      this.url = url;
   }
   
   /**
    * Creates the template selection dialog if it is not created yet.
    * Stores the dialog in the {@link #wgtDlg} field.
    */
   this.maybeCreateSelectTemplateDialog = function ()
   {
      if (this.wgtDlg)
      {
         return;
      }
      this.wgtDlg = ps.createDialog(
            {
               id: "ps.content.SelectTemplatesDlg",
               title: psxGetLocalMessage("javascript.ps.content.selecttemplates@Templates"),
               href: this.url
            }, "750px", "450px");
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
         _this.parseControls();
         var temps = _this.wgtTemplates;
         if(temps.options.length > 0)
         {
            // figure out which template is currently selected by
            // passed in snippet
            var idx = 0;
            var currentTemp = _this.snippetId.getTemplateId();
            for(i = 0; i < temps.options.length; i++)
            {
               var val = temps.options[i].value;
               var objId = new ps.aa.ObjectId(val);
               if(objId.getTemplateId() == currentTemp)
               {
                  idx = i;
                  break;
               }
            }            
            
            temps.selectedIndex = idx;
            _this.onTemplateChoice();
         }
         else
         {
            _this.wgtPreviewPane.setContent("No templates associated to the slot.");            
         }         
      });
   }

   /**
    * Use when the select templates dialog contents is used in a panel.
    * The contents must be loaded before calling this for it to work.
    */
   this.initAsPanel = function(parentMode)
   {
      if(parentMode != undefined && parentMode != null)
		  this.parentMode = parentMode;
	  this.isAsDialog = false;
      this.parseControls();
      var temps = this.wgtTemplates;
      if(temps.options.length == 0)
      {
         this.wgtPreviewPane.setContent("No templates associated to the slot.");            
      }
      else
      {
         this.wgtTemplates.selectedIndex = 0;
         this.onTemplateChoice();
      } 
   }
   
   /**
    * Retrieves the controls from the main content and connects
    * the necessary events and functions.
    */
   this.parseControls = function()
   {
      var _this = this;
      this.wgtTemplates = document.getElementById("ps.select.templates.wgtTemplates");
      this.wgtPreviewPane = dojo.widget.byId("ps.select.templates.wgtPreviewPane");
      this.wgtTemplates.onchange = function()
      {
         _this.onTemplateChoice();
      }
      if(this.isAsDialog)
      {
         this.wgtButtonSelect = dojo.widget.byId("ps.select.templates.wgtButtonSelect");
         this.wgtButtonCancel = dojo.widget.byId("ps.select.templates.wgtButtonCancel");
         this.wgtButtonCancel.onClick = function()
         {
            _this.cancelCallback();
            _this.wgtDlg.hide();
         };
         
         this.wgtButtonSelect.onClick = function()
         {
            var id = _this.getSelectedId();
            if(id)
            {
               _this.okCallback(id, _this.snippetId);
               _this.wgtDlg.hide();
            }
         }
      }      
          
      
   }   
  
   /**
    * Resets the content url and displays the dialog.
    * @param {function} okCallback the callback function that will be
    * called if the select button is clicked and a selection exists.
    * @param {function} cancelCallback  the callback function that will be
    * called if the cancel button is clicked.
    * @param {ps.aa.ObjectId} the objectid of the snippet in question
    * with its slot modified to be the target slotid.
    */
   this.open = function(okCallback, cancelCallback, snippetId)
   {
      if(!this.isAsDialog)
         return;
      dojo.lang.assertType(snippetId, ps.aa.ObjectId);
      this.snippetId = snippetId;
      this.okCallback = okCallback;
      this.cancelCallback = cancelCallback;
      var newUrl = this.url + "?objectId=" + escape(snippetId.serialize());
      
      this.maybeCreateSelectTemplateDialog();
      this.wgtDlg.setUrl(newUrl);
      ps.util.setDialogSize(this.wgtDlg, this.preferredWidth, this.preferredHeight);
      this.wgtDlg.show();
      
   }
   
   /**
    * Callback used when a template is highlighted in the
    * selection list. Causes the template snippet combination to
    * be assembled and previewed.
    */  
   this.onTemplateChoice = function()
   {
      var id = this.getSelectedId();
      if(id)
         this.loadPreviewPane(id);
   }
   
   /**
    * Returns the selected template id.
    */
   this.getSelectedId = function()
   {
      var index = this.wgtTemplates.selectedIndex;
      if(index == -1)
         return null;
      var option =  this.wgtTemplates.options[index];
      return new ps.aa.ObjectId(option.value); 
  }
   
   /**
    * Assembles and loads the snippet template combination
    * in the preview pane.
    */
   this.loadPreviewPane = function(snippetId)
   {
     
     dojo.lang.assertType(snippetId, ps.aa.ObjectId);
     var tempId = snippetId.getTemplateId();
     var isBinary = false;
     var content = "";
	  var response = null;
     // load template list
      var response = ps.io.Actions.getItemTemplatesForSlot(snippetId);
		if(response.isSuccess())
		{
         var res = response.getValue();
         for(i = 0; i < res.length; i++)
         {
            if(res[i].variantid == tempId)
            {
               if(res[i].outputformat == "Binary")
                  isBinary = true;
               break;
            }
         }
		}
     
	  if(this.parentMode == ps.util.BROWSE_MODE_RTE_INLINE_IMAGE)
	  {
        response = ps.io.Actions.getUrl(snippetId, "CE_LINK");
		  if(response.isSuccess())
		  {
              content = "<img src=\"" + response.getValue().url + "\">";
		  }
	  }
     else if(this.parentMode == ps.util.BROWSE_MODE_RTE_INLINE_LINK && isBinary)
     {
        var isImage = false;
		  response = ps.io.Actions.getSnippetMimeType(snippetId);
		  if(response.isSuccess())
		  {
		     mType = response.getValue().mimetype;
			  isImage = mType.toLowerCase().indexOf("image/") != -1;	
		  }
		  
		  response = ps.io.Actions.getUrl(snippetId, "CE_LINK");
		  if(response.isSuccess())
		  {
           if (isImage)
			  {
			     content = "<img src=\"" + response.getValue().url + "\">";
			  }
			  else 
			  {
			  	   content = "<table width=\"100%\" height=\"100%\"><tr><td valign=\"middle\" align=\"center\">";
			  	   content += "<a target=\"_new\" href=\"" + response.getValue().url + "\">" +
			  	   "<u>Click To View Binary</u>" +
			  	   "</a>";
			  	   content += "</td></tr></table>";
			  }
		  }
     }
	  else
	  {
       if(!isBinary)
       {
          var selectedContent = null;
          try
          {
            selectedContent = ___selectedContent;
          }
          catch (ignore){}
          
          response = ps.io.Actions.getSnippetContent(snippetId, false,  selectedContent);
		    if(response.isSuccess())
		    {
		       content = response.getValue();
		    }
       }

	  }
	  if(content == "")
	     content = "No preview available.";
     this.wgtPreviewPane.setContent(content);
      
   }
 
 }