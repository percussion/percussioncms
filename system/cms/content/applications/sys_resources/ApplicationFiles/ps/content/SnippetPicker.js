/******************************************************************************
 *
 * [ ps.content.SnippetPicker.js ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 dojo.provide("ps.content.SnippetPicker");
 
 dojo.require("ps.io.Actions");
 dojo.require("ps.widget.ContentPaneProgress");
 
/**
 * The javascript code needed to make the select templates
 * dialog work.
 */
ps.content.SnippetPicker = function()
{
 	this.preferredHeight = 450;
   this.preferredWidth = 750;

 
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
    * Creates the snippet picker dialog if it is not created yet.
    * Stores the dialog in the {@link #wgtDlg} field.
    */
   this.maybeCreateSnippetPickerDialog = function ()
   {
      var dlgTitle = "Remove Snippet(s) from the Slot";
      if(this.dlgType == this.CREATE_SNIPPET_DLG)
         dlgTitle = "Choose Where to Place the Snippet";
      if (this.wgtDlg)
      {
         this.wgtDlg.titleBarText.innerHTML = dlgTitle;
         return;
      }
      this.wgtDlg = ps.createDialog(
            {
               id: "ps.content.SnippetPickerDlg",
               title: dlgTitle,
               href: this.url
            }, "750px", "450px", false);
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
      });
   }

   /**
    * Retrieves the controls from the main content and connects
    * the necessary events and functions.
    */
   this.parseControls = function()
   {
      var _this = this;
      this.wgtButtonSelect = dojo.widget.byId("ps.snippet.picker.wgtButtonSelect");
      this.wgtButtonCancel = dojo.widget.byId("ps.snippet.picker.wgtButtonCancel");
      this.wgtButtonShowtitles = dojo.widget.byId("ps.snippet.picker.wgtButtonShowTitles");
      this.wgtSnippetDisplayDiv = dojo.widget.byId("ps.snippet.picker.wgtSnippetDisplayDiv");
      this.tblRemoveSnippetBtns = dojo.byId("ps.snippet.picker.tblRemoveSnippetBtns");
      this.tblCreateSnippetBtns = dojo.byId("ps.snippet.picker.tblCreateSnippetBtns");

      this.wgtButtonCancel.onClick = function()
      {
         _this.cancelCallback();
         _this.wgtDlg.hide();
      };
      
      this.wgtButtonSelect.onClick = function()
      {
         var rids = _this.getSelectedRids();
         if(rids == "")
         {
            alert("Please select atleast one template to remove.");
            return;
         }
         if(_this.dlgType == _this.REMOVE_SNIPPETS_DLG)
         {
            _this.okCallback(_this.slotId,rids);
         }
         else
         {
            var option = "before";
            var rbs = document.getElementsByName("ps.snippet.picker.placeWhereRadio");
            for(var i=0; rb = rbs[i]; i++)
            {
               if(rb.checked)
               {
                  option = rb.value;
                  break;
               }
            }
            _this.okCallback(_this.slotId,rids,option);
         }
         _this.wgtDlg.hide();
      }

      this.wgtButtonShowtitles.onClick = function()
      {
         _this.toggleSnippetDisplay();

      }
      
      if(this.dlgType == this.REMOVE_SNIPPETS_DLG)
      {
         this.wgtButtonSelectAll = dojo.widget.byId("ps.snippet.picker.wgtButtonSelectAll");
         this.wgtButtonDeselectAll = dojo.widget.byId("ps.snippet.picker.wgtButtonDeselectAll");
         this.wgtButtonSelectAll.onClick = function()
         {
            _this.toggleSelection(true);
         }
         this.wgtButtonDeselectAll.onClick = function()
         {
            _this.toggleSelection(false);
         }
         dojo.html.hide(this.tblCreateSnippetBtns);
         dojo.html.setStyle(this.tblRemoveSnippetBtns.parentNode, "valign","top");
         dojo.html.setStyle(this.tblRemoveSnippetBtns.parentNode, "vertical-align","top");
         this.wgtButtonSelect.setCaption("Remove");
         this.wgtButtonSelect.setDisabled(true);
      }
      else
      {
         dojo.html.hide(this.tblRemoveSnippetBtns);
         dojo.html.setStyle(this.tblCreateSnippetBtns.parentNode, "valign","middle");
         dojo.html.setStyle(this.tblCreateSnippetBtns.parentNode, "vertical-valign","middle");
         this.wgtButtonSelect.setCaption("Next");
      }

      //Initialize the snippet display content
      var content = this.getSnippetPickerSlotContent(false);
      if(!content)
         return;
      this.snippetContent = content;
      this.wgtSnippetDisplayDiv.setContent(this.snippetContent);
      this.snippetDisplayType = this.SNIPPETS;
      this.snippetNodes = dojo.html.getElementsByClass("PSAASnippetPickerItem",this.wgtSnippetDisplayDiv.domNode,"div",dojo.html.classMatchType.IsOnly,false);
      //Connect onclick event to snippets
      for (var i = 0; node = this.snippetNodes[i]; i++) 
      { 
         dojo.event.connect(node, "onclick",_this,"toggleSingleSelection"); 
      }
      //If dialog type is create then select the first snippet
      if(this.dlgType == this.CREATE_SNIPPET_DLG)
      {
         var defnode = this.snippetNodes[0];
         if(this.refRelId)
         {
            for(var i=0; node=this.snippetNodes[i];i++)
            {
               if(this.refRelId == dojo.html.getAttribute(node,"rid"))
                  defnode = node;
            }
         }
         dojo.html.setStyle(defnode, "background-color",this.SELECTED_BGCOLOR);
         dojo.html.setStyle(defnode, "border",this.SELECTED_BORDER);
         defnode.setAttribute("selection",true);
         if(this.option)
            this._setOption(this.option);
      }     
   }   
  
  /**
   * Selects the where to place radio button based on the supplied option, if 
   * radio button group exists.
   * @param option, assumed to be either, "before", "after" or 
   * "replace".
   */
   this._setOption = function(option)
   {
      var rbg = document.getElementsByName("ps.snippet.picker.placeWhereRadio");
      if(!rbg)
         return;
      if(option == "before")
      {
         rbg[0].checked=true;
      }
      else if(option == "after")
      {
         rbg[1].checked=true;
      }
      else if(option=="replace")
      {
         rbg[2].checked=true;
      }
   }

  /**
   * @return a comma sepearated list of relationship ids of the selected snippets or 
   * empty string. 
   */
  this.getSelectedRids = function()
  {
      var rids = "";
      var nodes = this.getSelectedNodes();
      for(var i=0; node=nodes[i];i++)
      {
         rids += dojo.html.getAttribute(node,"rid");
         if(i<nodes.length-1)
            rids += ",";
      }
      return rids;
  }
  
  /**
   * Returns the array of selected nodes, may be empty if none is selected.
   */
   this.getSelectedNodes = function()
   {
      var selNodes = new Array();
      var nodes = this.snippetDisplayType == this.SNIPPETS?this.snippetNodes:this.titleNodes;
      for(var i=0; node=nodes[i];i++)
      {
         if(this.isNodeSelected(node))
            selNodes.push(node);
      }
      return selNodes;
   }

  /**
   * Function to toggle the snippet display area with snippets or titles.
   * Changes the title of the button and connects the events to snippet/titles div elements.
   */
  this.toggleSnippetDisplay = function()
  {
      var nodes = null;
      if(this.snippetDisplayType == this.SNIPPETS)
      {
         this.wgtButtonShowtitles.setCaption("Show Snippets");         
         this.snippetDisplayType = this.TITLES;
         if(this.titlesContent!=null)
         {
            this.wgtSnippetDisplayDiv.setContent(this.titlesContent);
         }
         else
         {
            var content = this.getSnippetPickerSlotContent(true);
            if(!content)
               return;
            this.titlesContent = content;
            this.wgtSnippetDisplayDiv.setContent(this.titlesContent);
         }
         this.titleNodes = dojo.html.getElementsByClass("PSAASnippetPickerTitle",this.wgtSnippetDisplayDiv.domNode,"div",dojo.html.classMatchType.IsOnly,false);
         nodes = this.titleNodes;
         this.transferSelection(this.snippetNodes,nodes);
      }
      else
      {
         this.wgtButtonShowtitles.setCaption("Show Titles");         
         this.snippetDisplayType = this.SNIPPETS;
         if(this.snippetContent!=null)
         {
            this.wgtSnippetDisplayDiv.setContent(this.snippetContent);
         }
         else
         {
            var content = this.getSnippetPickerSlotContent(false);
            if(!content)
               return;
            this.snippetContent = content;
            this.wgtSnippetDisplayDiv.setContent(this.snippetContent);
         }
         this.snippetNodes = dojo.html.getElementsByClass("PSAASnippetPickerItem",this.wgtSnippetDisplayDiv.domNode,"div",dojo.html.classMatchType.IsOnly,false);
         nodes = this.snippetNodes;
         this.transferSelection(this.titleNodes,nodes);
      }
      var _this = this;
      //Connect onclick event to snippets/titles
      for (var i = 0; node = nodes[i]; i++) 
      { 
         dojo.event.connect(node, "onclick",_this,"toggleSingleSelection"); 

      }     
  }
   
   /**
    * Updates the selection property of target nodes with the
    * source nodes.
    * @param nodeSet1 source nodes assumed not null.
    * @param nodeSet2 target nodes assumed not null.
    */
   this.transferSelection = function(nodeSet1, nodeSet2)
   {
      for(var i=0; i<nodeSet1.length;i++)
      {
         var isSelected = this.isNodeSelected(nodeSet1[i]);
         var bgc = isSelected?this.SELECTED_BGCOLOR:this.DESELECTED_BGCOLOR;
         var bdr = isSelected?this.SELECTED_BORDER:this.DESELECTED_BORDER;
         dojo.html.setStyle(nodeSet2[i], "background-color",bgc);
         dojo.html.setStyle(nodeSet2[i], "border",bdr);
         nodeSet2[i].setAttribute("selection",isSelected);
      }
   }

   /**
    * Gets the slot content for the this dialog.
    * @param isTitles boolean flg to indicate whether content needs to be
    * titles or snippets.
    * @return html content or false if there is any error from server while getting the content.
    */
   this.getSnippetPickerSlotContent = function(isTitles)
   {
      var response = ps.io.Actions.getRenderedSlotContent(this.slotId, isTitles);
      ps.io.Actions.maybeReportActionError(response);
      if (!response.isSuccess()) 
      {
         return false;
      }
      return response.getValue();
   }

   /**
    * Convenient method for checking whether a snippet/title node is selected or not.
    * Checks for the attribute "selection", if exists and if has a value "true" then
    return true otherwise false.
    * @param node The snippet or title node assumed not null.
    * @return true if the node is selected otherwise false.
    */
   this.isNodeSelected = function(node)
   {
         var sattr = dojo.html.getAttribute(node,"selection");
         return sattr?(sattr=="true"?true:false):false;
   }
   
   /**
    * Method to select/deselect all snippet/titles.
    * @param boolean flag to indicate whether to select all or deselect all.
    */
   this.toggleSelection = function(isSelectAll)
   {
      var bgc = isSelectAll?this.SELECTED_BGCOLOR:this.DESELECTED_BGCOLOR;
      var bdr = isSelectAll?this.SELECTED_BORDER:this.DESELECTED_BORDER;
      var nodes = this.snippetDisplayType == this.SNIPPETS?this.snippetNodes:this.titleNodes;
      for (var i = 0; node = nodes[i]; i++) 
      { 
         dojo.html.setStyle(node, "background-color",bgc); 
         dojo.html.setStyle(node, "border",bdr);
         node.setAttribute("selection",isSelectAll);
      }
      this.wgtButtonSelect.setDisabled(!isSelectAll);
   }

   /**
    * Toggles selection of a single snippet/title.
    * Sets the back ground color and border properties appropriately
    * and enables or disables the Remove button.
    * @param The click event assumed not null.
    */
   this.toggleSingleSelection = function(e)
   {
      var node = this.getSnippetNodeFromTarget(e.target);
      var isSelected = this.isNodeSelected(node);
      //Unselect the previously selected node if the dlgType is CREATE_SNIPPET_DLG
      //if the current node itself is selected node skip this
      if(this.dlgType == this.CREATE_SNIPPET_DLG && !isSelected)
      {
         var snodes = this.getSelectedNodes();
         for(var i=0;snode=snodes[i];i++)
         {
            dojo.html.setStyle(snode, "background-color",this.DESELECTED_BGCOLOR);
            dojo.html.setStyle(snode, "border",this.DESELECTED_BORDER);
            snode.setAttribute("selection",false);
         }

      }
      
      var bgc = isSelected?this.DESELECTED_BGCOLOR:this.SELECTED_BGCOLOR;
      var bdr = isSelected?this.DESELECTED_BORDER:this.SELECTED_BORDER;
      dojo.html.setStyle(node, "background-color",bgc);
      dojo.html.setStyle(node, "border",bdr);
      node.setAttribute("selection",!isSelected);

      if(this.getSelectedRids()=="")
         this.wgtButtonSelect.setDisabled(true);
      else
      {
         this.wgtButtonSelect.setDisabled(false);
         this.wgtButtonSelect.setDisabled(false);
      }
      if (e.stopPropagation) 
      {
         e.stopPropagation();
         e.preventDefault();
      }
      return false;
   }
   
   /**
    * Gets the parent snippet node or title node corresponding to
    * the supplied dom node.
    * @param node dom node for which the sniipet/title needs to be found.
    */
   this.getSnippetNodeFromTarget = function(node)
   {
      var cssCls = this.snippetDisplayType == this.SNIPPETS?"PSAASnippetPickerItem":"PSAASnippetPickerTitle";
      if(dojo.html.hasClass(node,cssCls))
         return node;
      node = node.parentNode;
      while(node)
      {
         if(dojo.html.hasClass(node,cssCls))
            return node;
         node = node.parentNode;   
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
    * @param dlgType, the type of dialog valid types are 
    *   this.REMOVE_SNIPPETS_DLG and this.CREATE_SNIPPET_DLG
    * @param option Where to place radion button group option, if supplied must 
    *    be either "before" or "after" or "replace".
    * @param refRelId, the relationship id of the reference snippet, if supplied
    *    and valid the snippet associated with this id is selected.
    * 
    */
   this.open = function(okCallback, cancelCallback, slotId, dlgType, option, refRelId)
   {
      dojo.lang.assertType(slotId, ps.aa.ObjectId);
      dojo.lang.assert((dlgType != this.REMOVE_SNIPPETS_DLG || dlgType != this.CREATE_SNIPPET_DLG),
         "The Snippet Picker Dialog must of type Remove Snippets or Create Snippet");
      this.dlgType = dlgType;
      this.slotId = slotId;
      this.option = option;
      if(this.option)
      {
         dojo.lang.assert((this.option!="before" || this.option!="after" || 
            this.option!="replace"),"Invalid option");
      }
      this.refRelId = refRelId;
      this.okCallback = okCallback;
      this.cancelCallback = cancelCallback;
      var newUrl = this.url + "?objectId=" + escape(slotId.serialize());

      this.snippetContent = null;
      this.snippetNodes = null;
      this.titlesContent = null;
      this.titleNodes = null;
      
      this.maybeCreateSnippetPickerDialog();
      this.wgtDlg.setUrl(newUrl);
      ps.util.setDialogSize(this.wgtDlg, this.preferredWidth, this.preferredHeight);
      this.wgtDlg.show();
      
   }
   
   
   /**
    * Constants for Dialog type
    */
   this.REMOVE_SNIPPETS_DLG = 0;
   this.CREATE_SNIPPET_DLG = 1;

   /**
    * Constants for Snippets display type
    */
   this.SNIPPETS = 0;
   this.TITLES = 1;

   /**
    * variable to store the snippet content initialized in parseControls method.
    */
   this.snippetContent = null;
   this.snippetNodes = null;
   
   /**
    * variable to store the titles content initialized in toggleSnippetDisplay method
    * which happens on the first click of Show Titles button.
    */
   this.titlesContent = null;
   this.titleNodes = null;

 	/**
    * variables to hold the dialog type snippet display type initialized with default values.
    */
   this.dlgType = this.REMOVE_SNIPPETS_DLG;
   this.snippetDisplayType = this.SNIPPETS;

   /**
    * Constants for bgcolor and border for the selected and deselected snippets
    */
   this.SELECTED_BGCOLOR = "#ffc";
   this.SELECTED_BORDER = "gray 2px dotted";
   this.DESELECTED_BGCOLOR="#fff";
   this.DESELECTED_BORDER = "gray 1px dotted";
}