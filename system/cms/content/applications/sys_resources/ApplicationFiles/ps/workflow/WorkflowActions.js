/******************************************************************************
 *
 * [ ps.workflow.Workflow.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.workflow.WorkflowActions");

dojo.require("dojo.collections.ArrayList");
dojo.require("dojo.event");
dojo.require("dojo.lang.assert");
dojo.require("dojo.json");
dojo.require("dojo.widget.Manager");


dojo.require("ps.aa");
dojo.require("ps.aa.controller");
dojo.require("ps.io.Actions");
dojo.require("ps.io.Response");
dojo.require("ps.util");

/**
 * The workflow object. Manages the workflow actions of an item.
 */
ps.workflow.WorkflowActions = function()
{
   /**
    * The workflow action id of the currently selected action. Initialized in
    * @see #onWfActionChanged method which gets called on the onload.
    */
   this.actionId = null;

   /**
    * The dialog object of workflow dialog.
    */
   this.wfDlg = null;

   /**
    * The content id of the item for which the workflow dialog is opened.
    * Initialized in dialog open method.
    */
    this.contentId = null;
    
   /**
    * Base URL for workflow action dialog.
    */ 
    this.workflowActionsUrl = "/Rhythmyx/ui/activeassembly/workflow/workflowactions.jsp";
    
   /**
    * Base URL for adhoc user search pane.
    */ 
    this.adhocSearchUrl = "/Rhythmyx/ui/activeassembly/workflow/adhocsearch.jsp";

   /**
    * Base URL for adhoc user results pane.
    */ 
    this.adhocResultsUrl = "/Rhythmyx/ui/activeassembly/workflow/adhocresults.jsp";

   /**
    * This must be called after construction of the dialog. This actually parses 
    * all controls/widgets within the dialog and connects appropriate events.
    */
   this.init = function()
   {
   }

   /**
    * Creates the workflow dialog if it is not created yet.
    * Stores the dialog in the {@link #wfDlg} field.
    */
   this.maybeCreateWorkflowDialog = function ()
   {
      if (this.wfDlg)
      {
         return;
      }
      
      this.wfDlg = ps.createDialog(
            {
               id: "ps.workflow.WorkflowActionsDlg",
               title: "Workflow Actions"
            }, "420px", "250px");
      

      var _this = this;
      //override the dialog close function
      // to not destroy the dialog
      this.wfDlg.closeWindow = function()
      {
         _this.wfDlg.hide();
      }
      dojo.event.connect(this.wfDlg, "onLoad", function()
      {
         _this.parseControls();
         if(_this._isUserAuthorized())
         {
            _this.onWfActionChanged();
         }
      });
   }

   /**
    * Helper method called by init() to parse all the controls and connect 
    * events.
    */
   this.parseControls = function()
   {
      var _this = this;
      //Workflow Action Pane Stuff
      //Form elements
      this.wfActionPane = dojo.byId("ps.workflow.actionPane");
      if (this._isUserAuthorized())
      {         
         this.wfActionSelector = dojo.byId("ps.workflow.workflowActionSelect");
         this.wfCommentText = dojo.byId("ps.workflow.commentText");
         this.wfAdhocUsers = dojo.byId("ps.workflow.adhocUsers");

         //Buttons
         this.wgtAdhocSearch = dojo.widget.byId("ps.workflow.wgtButtonAdhocSearch");
         var submit = dojo.widget.byId("ps.workflow.wgtButtonSubmit");
         var cancel = dojo.widget.byId("ps.workflow.wgtButtonCancel");

         //Resize the dialog
         if (this.wfCommentText && this.wgtAdhocSearch)
         {
            ps.util.setDialogSize(this.wfDlg, 420, 370);
         }
         else if (this.wfCommentText || this.wgtAdhocSearch)
         {
            ps.util.setDialogSize(this.wfDlg, 420, 250);
         }
         else
         {
            ps.util.setDialogSize(this.wfDlg, 420, 125);
         }

         //Event Connectors
         dojo.event.connect(this.wfActionSelector, "onchange",
              this, "onWfActionChanged");
         if (this.wgtAdhocSearch)
         {
            dojo.event.connect(this.wgtAdhocSearch, "onClick",
                  this, "openAdhocSearchDialog");
         }
         dojo.event.connect(submit, "onClick", this, "executeWorkflowAction");
         dojo.event.connect(cancel, "onClick", this, "onWfActionCancelled");
      }
      else
      {
         this.wgtButtonClose = dojo.widget.byId("ps.workflow.wgtButtonClose");
         dojo.event.connect(this.wgtButtonClose, "onClick", this, "onWfActionCancelled");
         ps.util.setDialogSize(this.wfDlg, 420, 125);
      }
   }

   /**
    * Opens the dialog box.
    */
   this.open = function()
   {
      this.maybeCreateWorkflowDialog();
      this.contentId = ps.aa.controller.activeId.getContentId();
      var wfurl = ps.util.addParamToUrl(this.workflowActionsUrl,"sys_contentid",this.contentId);
      this.wfDlg.setUrl(wfurl);      
      this.wfDlg.show();
   }

   /**
    * Function to update the dialog content when the workflow action is changed.
    */
   this.onWfActionChanged = function()
   {
       var index = this.wfActionSelector.selectedIndex;
       if(index < 0) index=0;
       var idStr = this.wfActionSelector.options[index].value;
       this.actionId = new ps.workflow.ActionId(idStr);
       
       // update comment UI
       if (this.wfCommentText)
       {
          if (!this.wfCommentRequiredStar)
          {
             this.wfCommentRequiredStar = dojo.byId("ps.workflow.commentStar");
          }
          
          if(this.actionId.isCommentBoxNeeded())
          {
             this.wfCommentText.disabled = false;
             this.wfCommentText.style.bgcolor = "white";

             if(this.actionId.isCommentRequired())
             {
                 this.wfCommentRequiredStar.style.visibility = "visible";
             }
             else
             {
                 this.wfCommentRequiredStar.style.visibility = "hidden";
             }
          }
          else
          {
             this.wfCommentText.disabled = true;
             this.wfCommentText.style.bgcolor = "grey";
             this.wfCommentRequiredStar.style.visibility = "hidden";
          }
       }
       
       // update the adhoc UI
       if (this.wgtAdhocSearch)
       {
          if(this.actionId.isAdhocBoxNeeded())
          {
             this.wfAdhocUsers.disabled = false;
             this.wfAdhocUsers.style.bgcolor = "white";
             this.wgtAdhocSearch.setDisabled(false);
          }
          else
          {
             this.wfAdhocUsers.disabled = true;
             this.wfAdhocUsers.style.bgcolor = "gray";
             this.wgtAdhocSearch.setDisabled(true);
          }
       }
   }
    
    /**
     * Opens the adhoc search dialog. 
     */
    this.openAdhocSearchDialog = function()
    {
       this._maybeCreateAdhocSearchDialog();

       dojo.html.hide(this.wfActionPane);
       this.adhocResultsPane.hide();
       var wfurl = ps.util.addParamToUrl(this.adhocSearchUrl,"sys_contentid",this.contentId);
       wfurl = ps.util.addParamToUrl(wfurl,"sys_transitionid",this.actionId.getTransitionId());
       this.adhocSearchPane.cacheContent = false;
       var _this = this;
       dojo.event.connect(this.adhocSearchPane, "onLoad", function()
       {
         _this.adhocRoleSelect = dojo.byId("ps.workflow.adhocRole");
         _this.nameFilterText = dojo.byId("ps.workflow.nameFilter");
         _this.wgtButtonSearch = dojo.widget.byId("ps.workflow.wgtButtonSearch");
         _this.wgtButtonAdd = dojo.widget.byId("ps.workflow.wgtButtonAdd");
         _this.wgtButtonClose = dojo.widget.byId("ps.workflow.wgtButtonClose");
         //If the button exists already setting the disabled multiple times 
         //causing multiple dojoButtonDisabled classes to be added to the element
         //and enable code is not working properly.
         if(!dojo.html.hasClass(_this.wgtButtonAdd.domNode,"dojoButtonDisabled"))
            _this.wgtButtonAdd.setDisabled(true);
         
         dojo.event.connect(_this.wgtButtonSearch, "onClick", _this, "onSearchClicked");
         dojo.event.connect(_this.wgtButtonAdd, "onClick", _this, "onAddClicked");
         dojo.event.connect(_this.wgtButtonClose, "onClick", _this, "onSearchClosed");
       });
       this.adhocSearchPane.setUrl(wfurl);
       this.wfAdhocPane.show();
    }
    
   /**
    * Creates the adhoc search pane if it is not created yet.
    * Stores the pane in {@link #wfAdhocPane} field.
    */
   this._maybeCreateAdhocSearchDialog = function ()
   {
      if (this.wfAdhocPane)
      {
         return;
      }
      var div = document.createElement('div');
      var style = div.style;
      style.overflow = "auto";
      style.border = "0px solid black";
      this.wfActionPane.parentNode.appendChild(div);
      
      this.wfAdhocPane = dojo.widget.createWidget("ContentPane",
            {
               titleBarDisplay: false,
               executeScripts: true
            }, div);

      this.wfAdhocPane.setContent(
         '<table width="100%">\n'
         + '<tr><td width="100%">\n'
         + '<div dojoType="ContentPane" id="ps.workflow.adhocSearchPane" executeScripts="true" style="border: 1px solid #e3edfa;"/>\n'
         + '</td></tr>\n'
         + '<tr><td><table width="100%">\n'
         + '<tr><td bgcolor="#e3edfa" width="100%">Search Results</td></tr>\n'
         + '<tr><td width="100%">\n'
         + '<div dojoType="ContentPane" id="ps.workflow.adhocResultsPane" executeScripts="true" style="border: 1px solid #e3edfa;">\n'
         + '</div></td></tr>\n'
         + '</table></td></tr><tr>\n'

         + '<td align="center" width="100%">\n'
         + '<table width="100%" align="left" cellpadding="1"><tr>\n'
         + '<td align="right">\n'
         + '<button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonAdd">Add</button>\n'
         + '</td>\n'
         + '<td align="left">\n'
         + '<button style="border: 1px solid black;" dojoType="ps:PSButton" id="ps.workflow.wgtButtonClose">Close</button>\n'
         + '</td>\n'
         + '</tr></table></td>\n'

         + '</tr>\n'
         + '<tr><td width="100%" height="100%"/></tr>\n'
         + '</table>');

      this.adhocSearchPane = dojo.widget.byId("ps.workflow.adhocSearchPane");
      dojo.lang.assert(this.adhocSearchPane, "Expected adhocSearchPane")
      this.adhocResultsPane = dojo.widget.byId("ps.workflow.adhocResultsPane");
      dojo.lang.assert(this.adhocResultsPane, "Expected adhocResultsPane")
   }
    
    /**
     * Calls appropriate workflow server action based on the selected action. 
     * If successful calls the @see #handleObjectIdModifications() to update the 
     * checkout status of the objectids on the page.  
     */
    this.executeWorkflowAction = function()
    {
        var atype = this.actionId.getActionType();
        var response = null;
        var checkOutStatusChanged = false;
        var newStatus = null;
        if(this.actionId.isCommentRequired() && this._getComment() === "")
        {
           alert("Comment must be entered for workflow transition <" + this.actionId.getActionLabel() +">.");
           return false;
        }
        if(atype == ps.workflow.ActionId.ACTION_TYPE_CHECKIN || atype == ps.workflow.ActionId.ACTION_TYPE_FORCE_CHECKIN)
        {
           response = ps.io.Actions.checkInItem(this.contentId, this._getComment());
           checkOutStatusChanged = true;
           newStatus = "0";
        }
        else if(atype == ps.workflow.ActionId.ACTION_TYPE_CHECKOUT)
        {
           response = ps.io.Actions.checkOutItem(this.contentId, this._getComment());
           checkOutStatusChanged = true;
           newStatus = "1";
        }
        else if(atype == ps.workflow.ActionId.ACTION_TYPE_TRANSITION_CHECKOUT)
        {
           response = ps.io.Actions.transitionCheckOutItem(this.contentId,
                 this.actionId.getWfAction(), this._getComment(),
                 this._getAdhocUsers());
           checkOutStatusChanged = true;
           newStatus = "1";
        } 
        else
        {
           response = ps.io.Actions.transitionItem(this.contentId,
                 this.actionId.getWfAction(), this._getComment(),
                 this._getAdhocUsers());
           if(ps.aa.controller.activeId.isCheckout() || ps.aa.controller.activeId.isCheckoutByMe())
           {
              checkOutStatusChanged = true;
              newStatus = "0";
           }
        }

        if (!response.isSuccess())
        {
           ps.io.Actions.maybeReportActionError(response);
           return false;
        }
        if(checkOutStatusChanged)
        {
           this.handleObjectIdModifications(newStatus);
         }
        ps.aa.controller.refreshOpener(this.contentId);        
        this.wfDlg.hide();
        if(this.wfAdhocPane)
           location.reload();
        
    }

   /**
    * Updates the objectids on the page.
    * Gets all the ids from tree model. Creates new array of ids by cloning
    * the items in the original array and changes the checkout status on the new
    * ids. Replaces the old ids with the new ids. 
    */
   this.handleObjectIdModifications = function(checkOutStatus)
   {
      dojo.lang.assert((checkOutStatus === "0" || checkOutStatus === "1" || checkOutStatus === "2"), "checkOutStatus must be 0, 1, or 2");
      
      //get all ids from tree model by passing the active contentid
     var results = ps.aa.controller.treeModel.getAllIdsByContentId(this.contentId);
     dojo.lang.assertType(results, dojo.collections.ArrayList);
     var oldIds = new Array();
     var newIds = new Array();
     var slotIds = new Array();
     for(var i=0; i<results.count; i++)
     {
        var result = results.item(i);
        dojo.lang.assertType(result, ps.aa.ObjectId);
        oldIds[i] = result;
        var newId = result.clone();
        newId.setCheckoutStatus(checkOutStatus);
        newIds[i] = newId;
        if(result.isSlotNode())
           slotIds.push(newId);
     }
     ps.aa.controller.replaceIds(oldIds,newIds);
     if(checkOutStatus == 1)
     {
        for(var j=0; j<slotIds.length; j++)
        {
           ps.aa.controller.refreshSlot(slotIds[j]);
        }
        if(slotIds.length > 0)
           ps.aa.controller.updateTreeWidget();
     }
   }
    
    /**
     * Closes the workflow dialog.
     */
    this.onWfActionCancelled = function()
    {
        this.wfDlg.hide();
        if(this.wfAdhocPane)
           location.reload();
    }
    
    /**
     * Sets the adhoc results pane with the adhoc results url. 
     * The base adhoc results url is appended with sys_contentid, rolename and 
     * sys_transitionid parameters before setting onto thepane.
     */
    this.onSearchClicked = function()
    {
       this.adhocResultsPane.show();
       var wfurl = ps.util.addParamToUrl(this.adhocResultsUrl,"sys_contentid",this.contentId);
       wfurl = ps.util.addParamToUrl(wfurl,"sys_transitionid",this.actionId.getTransitionId());
       wfurl = ps.util.addParamToUrl(wfurl,"rolename",this.adhocRoleSelect.value);
       wfurl = ps.util.addParamToUrl(wfurl,"namefilter","%"+this.nameFilterText.value+"%");
       var mm = this;
       dojo.event.connect(this.adhocResultsPane, "onLoad", function()
       {
         var count = dojo.byId("ps.workflow.adhocusercount").value;
         mm.adhocUsersChk = new Array();
         for(var i=0; i<count; i++)
         {
            mm.adhocUsersChk[i] = dojo.byId("ps.workflow.adhocusercheckbox_"+i);
         }
       });
       this.adhocResultsPane.cacheContent = false;
       this.adhocResultsPane.setUrl(wfurl);
    }
    
    /**
     * Enables or disables the Add button based on the user selection
     */
    this.onUserChecked = function()
    {
       var _self = this;
       setTimeout(function(){    
         var disableAdd = true;
         for (var i=0; i<_self.adhocUsersChk.length; i++)
         {
            if (_self.adhocUsersChk[i].checked) 
            {
               disableAdd = false;
               break;
            }
         }
         _self.wgtButtonAdd.setDisabled(disableAdd);
       }, 250);
    }

    /**
     * Adds the users to the list of adhoc users.
     * Gets the current list and adds the new users by seperating them with ;.
     * Avoids the duplicate user names.
     */
    this.onAddClicked = function()
    {
      var newUsers = new Array();
      var count = 0;
      for (var i=0; i<this.adhocUsersChk.length; i++)
      {
         if (this.adhocUsersChk[i].checked) 
         {
            newUsers[count++] = this.adhocUsersChk[i].value;
         }
      }
       if(newUsers.length < 1)
       {
          alert("Please select at least one user to add.");
          return false;
       }
   	// First copy the original values
   	var newval = "";
      var oldUsers = this.wfAdhocUsers.value;
      var originalArray = oldUsers.split(";");
   	for(var i = 0; i < originalArray.length; i++)
   	{
   		var val = originalArray[i];
   		if (val != "")
   		{
   			newval = this._appendWithDel(newval,";",val);
   		}
   	}
   	// Now copy new values that don't exist in the existing values
   	for(i = 0; i < newUsers.length; i++)
   	{
   		var val = newUsers[i];
   		if (val.length > 0 && !this._contains(originalArray, val))
   		{
   			newval = this._appendWithDel(newval,";",val);
   		}
   	}

	    // Reset to new value
   	 this.wfAdhocUsers.value = newval;
   	 dojo.html.show(this.wfActionPane);
   	 this.wfAdhocPane.hide();
    }
    
    /**
     * Show the workflow actions pane and hide the adhoc pane.
     */
    this.onSearchClosed = function()
    {
       dojo.html.show(this.wfActionPane);
   	 this.wfAdhocPane.hide();
    }

   /**
    * Returns true if the given value is in the passed vector
    * @param vector a set of values
    * @param value a value to search the vector for
    */
   this._contains = function(vector, value)
   {
      var len = vector.length;
      var i = 0;
      while(i < len)
      {
      if (vector[i] == value) return true;
      i++;
      }
      return false;
   }
   // Append the new element using the delimiter. Omit the delimeter
   // if the string is empty
   this._appendWithDel = function(string, del, newelement)
   {
   	if (string.length == 0)
   	{
   		return newelement;
   	}
   	else
   	{
   		return string + del + newelement;
   	}
   }
   
   /**
    * Returns a trimmed comment string, entered by the user,
    * or "" if the comment field does not exist.
    */
   this._getComment = function ()
   {
      return this.wfCommentText
            ? dojo.string.trim(this.wfCommentText.value)
            : "";
   }
   
   /**
    * Returns a trimmed adhoc users string,
    * or "" if the adhoc users field does not exist.
    */
   this._getAdhocUsers = function ()
   {
      return this.wfAdhocUsers
            ? dojo.string.trim(this.wfAdhocUsers.value)
            : "";
   }
   
   /**
    * Indicates whether the user is authorized to perform workflow actions.
    */
   this._isUserAuthorized = function ()
   {
      return this.wfActionPane;
   }
};

/**
 * Constructs an object of workflow action from a JSON string.
 * @param {String} idString The JSON string for workflow actions. 
 *    It is an array of pre-defined values of
 * ACTION_NAME - name of the action
 * WORKFLOW_COMMENT - has three values 0 - hide the comment box, 
 * 1 - comment optional, 2 - comment required.
 * SHOW_ADHOC - has two values 0 - no adhoc and 1 - show adhoc.
 * ACTION_TYPE - the type of the action 0 - checkin 1 - forcecheckin 2 - checkout
 * 3 - transition_checkout 4 - transition.
 * WF_ACTION - the trigger name
 * WF_TRANSITIONID - the transition id.
 * 
 */
ps.workflow.ActionId = function(idString)
{
   /**
    * Stores the id in serialized format as in 'string'
    */
   this.idString = idString;
   
   /**
    * Stores the list of values into array.
    */
   this.idobj = dojo.json.evalJson(idString);
   
   /**
    * Determines if the specified object equals this object.
    * 
    * @param {ps.aa.ObjectId} other The object in question.
    * 
    * @return 'true' if both objects have the same value; 'false' otherwise.
    */
   this.equals = function(other)
   {
      if ((typeof other == 'undefined') || other == null)
         return false;
      else
         return this.idString == other.idString;
   }
   
   /**
    * Convert this object to a JSON string. It is the reverse operation of the
    * constructor.
    */
   this.serialize = function()
   {
      return this.idString;
   }
   
   /**
    * @return true if the comment box is needed otherwise false.
    */
   this.isCommentBoxNeeded = function()
   {
       return this.idobj[ps.workflow.ActionId.WORKFLOW_COMMENT] > 0;
   } 

   /**
    * @return true if the comment is required otherwise false.
    */
   this.isCommentRequired = function()
   {
       return this.idobj[ps.workflow.ActionId.WORKFLOW_COMMENT] == 2;
   }
   
   /**
    * @return true if the Adhoc box is needed otherwise false.
    */
   this.isAdhocBoxNeeded = function()
   {
       return this.idobj[ps.workflow.ActionId.SHOW_ADHOC] == 1;
   }
   
   /**
    * Returns the type of action.
    */
   this.getActionType = function()
   {
       return this.idobj[ps.workflow.ActionId.ACTION_TYPE];
   }
   
   /**
    * Returns the transition trigger name.
    */
   this.getWfAction = function()
   {
      return this.idobj[ps.workflow.ActionId.WF_ACTION];
   }
   
   /**
    * Returns the transition id.
    */
   this.getTransitionId = function()
   {
      return this.idobj[ps.workflow.ActionId.WF_TRANSITIONID];
   }

   /**
    * Returns the label of action.
    */
   this.getActionLabel = function()
   {
       return this.idobj[ps.workflow.ActionId.WF_ACTION_LABEL];
   }
};

ps.workflow.ActionId.ACTION_NAME=0;
ps.workflow.ActionId.WORKFLOW_COMMENT=1;
ps.workflow.ActionId.SHOW_ADHOC=2;
ps.workflow.ActionId.ACTION_TYPE=3;
ps.workflow.ActionId.WF_ACTION=4;
ps.workflow.ActionId.WF_TRANSITIONID=5;
ps.workflow.ActionId.WF_ACTION_LABEL=6;

ps.workflow.ActionId.ACTION_TYPE_CHECKIN=0;
ps.workflow.ActionId.ACTION_TYPE_FORCE_CHECKIN=1;
ps.workflow.ActionId.ACTION_TYPE_CHECKOUT=2;
ps.workflow.ActionId.ACTION_TYPE_TRANSITION_CHECKOUT=3;
ps.workflow.ActionId.ACTION_TYPE_TRANSITION=4;




