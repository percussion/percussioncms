/******************************************************************************
 *
 * [ ps.io.Actions.js ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.io.Actions");


dojo.require("ps.io.Response");
dojo.require("dojo.collections.Dictionary");
dojo.require("dojo.lang.assert");
dojo.require("dojo.lang.type");
dojo.require("dojo.string.extras");



/**
 * This javascript class contains a stub function for each action available on
 * the server side AA servlet.
 */
ps.io.Actions = new function()
{
   
   this.MIMETYPE_PLAIN = "text/plain";
   this.MIMETYPE_JSON = "text/json";
   this.MIMETYPE_HTML = "text/html";
   this.MIMETYPE_XML = "text/xml";   
   this.formSubmitResults = null;
   
   /**
    * The number of locales the server has enabled. Set the first time the
    * GetLocaleCount() method is called, which caches the returned value here.
    */
   this.localeCount = -1;

   /**
    * Moves a slot item within a slot
    * 
    * @param {ps.aa.ObjectId} objectId the objectId object for
    *  this slot item (Required).
    * @param {string} mode one of the following "up", "down", "reorder" 
    * (Required).
    * @param (string) index required if using reorder mode. Can be 
    * <code>null</code> or empty if mode is "up" or "down".
    * Required if mode is "reorder".
    * @return ps.io.Response object.
    */
   this.move = function(objectId, mode, index)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);      
      var action = "Move";
      var params = new dojo.collections.Dictionary();
      params.add("mode", mode);
      if(index != null && index != undefined)
      {
         params.add("index", index);
      }
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, objectId.serialize(), params);
   }
   
   /**
    * Moves a slot item to another slot.
    *
    * @param {ps.aa.ObjectId} objectId the objectId object for this slot item
    * (Required).
    * @param {ps.aa.ObjectId} targetslotid the target slot id (Required).
    * @param {string} newtempid the template id,
    * may be <code>null</code> if the method should use the template currently
    * associated with the snippet.
    * @param {string} index the sort rank for the item in the target slot.
    * May be <code>null</code> in which case the item will be appended
    * to the end of the slot items.
    * @return ps.io.Response object. If a template is needed then
    * the isSuccess method of the response will be <code>false</code> and the
    * error message will be {@link ps.io.Actions#NEEDS_TEMPLATE_ID}.
    */ 
   this.moveToSlot = function(objectId, targetslotid, newtempid, index)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);      
      var action = "MoveToSlot";
      var params = new dojo.collections.Dictionary();
      params.add("newslotid", targetslotid);
      if (newtempid)
      {
         params.add("newtemplate", newtempid);
      }
      if (index)
      {
         params.add("index", index);
      }
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, objectId.serialize(), params);
   }
   
   /**
    * Retrieves the url for the specified action name and content item.
    *
    * @param {ps.aa.ObjectId}objectId the objectId object for the 
    * content item (Required).
    * @param {string} actionname the action name that defines the type
    * of url to be returned. One of the following strings:
    * <pre>
    * CE_EDIT
    * CE_VIEW_CONTENT
    * CE_VIEW_PROPERTIES
    * CE_FIELDEDIT
    * CE_VIEW_REVISIONS
    * CE_VIEW_AUDIT_TRAIL
    * PREVIEW_PAGE
    * PREVIEW_MYPAGE
    * RC_SEARCH
    * TOOL_SHOW_AA_RELATIONSHIPS
    * TOOL_LINK_TO_PAGE
    * ACTION_xxx
    * </pre>
    * Names of the form ACTION_xxx are generic. Any PSAction name registered 
    * with the server can be used. The name should be supplied in place of the 
    * xxx. e.g. ACTION_Translate. When received, the server will search for an
    * action whose name is Translate and will load the command and params from
    * that action to build the url, performing substitutions as needed.
    * @return Json object, never <code>null</code> or empty.
    * The returned json object contains the following parameters:
    * <pre>
    * url = the requested url
    * dlg_height = the dialog height (Only exists for the field edit url)
    * dlg_width = the dialog height (Only exists for the field edit url)
    * </pre>
    */
   this.getUrl = function(objectId, actionname)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);      
      var action = "GetUrl";
      var params = new dojo.collections.Dictionary();
      params.add("actionname", actionname);
      try
      {
         if(___sys_aamode != undefined && ___sys_aamode != null)
            params.add("sys_aamode", new String(___sys_aamode));
      }
      catch(ignore){}
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), params);
   }

   /**
    * Looks up an action based on a name, then calculates whether the current
    * user should be able to see the action in this context. (This is all done
    * by the server.)
    * 
    * @param actionNames A single string or a set of strings, each of which is 
    * used to lookup a PSAction. If empty, a successful response 
    * with an empty object is returned. The names are case-insensitive.
    * 
    * @param objectId The ps.aa.ObjectId that identifies the context within
    * which to check the visibility. If not provided, a successful response 
    * with an empty object is returned.
    * 
    * @return ps.io.Response whose value (if successful) is a map. The names 
    * (lower-cased) of all the supplied actions are used as keys while the value 
    * is a boolean true for visible, false for not visible. Never null.
    */
   this.getActionVisibility = function(actionNames, objectId)
   {
      var o = this._normalizeNames(actionNames);
      if (o instanceof ps.io.Response)
         return o;
      if (typeof objectId == "undefined" || objectId == null)
      {
         var result = new ps.io.Response();
         result._m_success = true;
         result._m_value = new Object();
         return result;
      }

      var action = "GetActionVisibility";
      var params = new dojo.collections.Dictionary();
      params.add("names", o.names);   
      var result = this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), params);
      if (result.isSuccess())
      {
         result._m_value = result._m_value[0];
      }
      return result;
   }
   
   /**
    * Converts the supplied type to an Object whose properties are the action 
    * names, if necessary and assigns it to another Object as the value of its
    * 'names' property.
    * 
    * @param actionNames May be undefined, null, primitive string, String, Array
    * of string or Object.
    * 
    * @return Either a ps.io.Response, whose value is an empty Object, if 
    * actionNames is undefined or null, or an Object with a single property, 
    * names, whose value is an Object with a property for each name supplied 
    * in actionNames and whose value is null.
    */
   this._normalizeNames = function(actionNames)
   {
      if (typeof actionNames == "undefined" || actionNames == null
         || (typeof actionNames == "array" && empty(actionNames)))
      {
         var result = new ps.io.Response();
         result._m_success = true;
         result._m_value = new Object();
         return result;
      }
      
      var p = new Object();
      if (typeof actionNames == "string")
      {
         p.names = {actionNames:null};
      }
      else if (typeof actionNames == "object" )
      {
         if (actionNames instanceof String)
            p.names = {actionNames:null};
         else
            p.names = actionNames;
      }
      return p;
   }
   
   /**
    * Looks up an action based on a name, then retrieves its label.
    * 
    * @param names A single string or a set of strings, each of which is used 
    * to lookup a PSAction. If empty, a successful response 
    * with an empty object is returned.
    * 
    * @return ps.io.Response whose value is a map. The names of all the visible 
    * actions are used as keys while the value is the label of that action. Never 
    * null.
    */
   this.getActionLabels = function(actionNames)
   {
      var o = this._normalizeNames(actionNames);
      if (o instanceof ps.io.Response)
         return o;
      var action = "GetActionLabels";
      var params = new dojo.collections.Dictionary();
      params.add("names", o.names);      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Returns the allowed content types for the specified slot.
    * @param {ps.aa.ObjectId} the slot objectid (Required)
    * @return a Json array that will contain a Json object
    * for each content type.
    * <pre>
    * Each object contains the following parameters:
    * 
    * contenttypeid
    * name
    * description
    * </pre>
    */
   this.getAllowedContentTypeForSlot = function(objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetAllowedContentTypeForSlot";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), null);
   }

   /**
    * Retrieves the content type id for the content id passed in.
    *
    * @param {string} contentid of the content item in question.
    * 
    * @return Json object, never <code>null</code> or empty.
    * The returned json object contains the following parameters:
    * <pre>
    * sys_contenttypeid = the content type id
    * </pre>
    */
   this.getContentTypeByContentId = function(contentid)
   {
      var action = "GetContentTypeByContentId";
      var params = new dojo.collections.Dictionary();
      params.add("sys_contentid", contentid);      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Returns the urls of images of templates.
    * If the objectId consists of slot id then only image urls of allowed 
    * templates are returned.
    * @param {String} the contentTypeId (Required)
    * @param {ps.aa.ObjectId} the objectid (Required)
    * @return ps.io.Response object containing a array that will contain a map
    * for each content type.
    * <pre>
    * Each map contains the following parameters:
    * 
    * templateId
    * templateName
    * thumbUrl
    * fullUrl
    * </pre>
    */
   this.getTemplateImagesForContentType = function(contentTypeId, objectId)
   {
      dojo.lang.assert(contentTypeId);
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetTemplateImagesForContentType";
      var params = new dojo.collections.Dictionary();
      params.add("sys_contenttypeid", contentTypeId);      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), params);
   }
   
   /**
    * Creates an item of supplied content type. 
    * @param {int} contentTypeId content type id (Required)
    * @param {String} folderPath the path of the folder in which the item needs 
    *   to be created, if an item exists with the same name in the folder,
    *   newly created item is not added to the folder.
    * @param {String} itemPath the source item path, may be null or empty in 
    * which case a new item is created otherwise a new copy is created 
    * corresponding to this item.
    * @param {String} itemTitle title for the newly created item must not be 
    *   <code>null</code> or empty.
    * @return ps.io.Response consisting of a map consisting of the 
    *   following parameters.
    *   validationError -- only incase of validation errors and will have 
    *      the error message.
    *   itemId -- content id of newly created item
    *   folderId -- parent folder id of newly created. 
    *     may be -1 if failed to add the item to the folder. 
    */
   this.createItem = function(contentTypeId,folderPath,itemPath,itemTitle)
   {
      dojo.lang.assert(contentTypeId);
      dojo.lang.assert(folderPath);
      dojo.lang.assert(itemTitle);
      var action = "CreateItem";
      var params = new dojo.collections.Dictionary();
      params.add("sys_contenttypeid", contentTypeId);      
      params.add("folderPath", folderPath);      
      params.add("itemPath", itemPath);      
      params.add("itemTitle", itemTitle);      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
    
   /**
    * Returns the item path of the supplied object id. 
    * Gets the parent folder id from the supplied objectId, 
    * if none calculates from item folder relationsships.
    * Builds a path with parent folder path and item name. 
    * Throws exception if the folder id is invalid.
    * If fails to get the parent folder, assumes that the item does not exist 
    * in any folder and returns its name.
    * @return path of the item of supplied objectId.
    */
   this.getItemPath = function(objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetItemPath";
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, objectId.serialize(), null);
   }

   /**
    * Action to get the content id or folder id by path
    * @param {String} path of item or folder
    * @return JSONObject of id(int) and type(String either item or folder) of 
    * the content item or folder represented by the supplied path. 
    * If the supplied path does not correspond to any item or folder then the 
    * server throws PSAAClientException.
    */
   this.getIdByPath = function(path)
   {
      var action = "GetIdByPath";
      var params = new dojo.collections.Dictionary();
      params.add("path", path);
      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   

   /**
    * Returns allowed templates for the specified snippet and a template count. 
    * The snippet is assembled with each template and is decorated
    * with a surrounding div tag that contains an onclick action
    * to allow the template to be selected. This is meant to
    * be used within a dialog that pops up when a template choice
    * is required. A close button is also included after
    * the last template is rendered.
    * @param {ps.aa.ObjectId} the snippet objectid (Required)
    * @return rendered templates as html, decorated with 
    * special div tags.
    * <pre>
    * The JSON object will contain the following parameters:
    * 
    * templateHtml - The assembled template variations
    * count - The template count
    * </pre>
    */
   this.getAllowedSnippetTemplates = function(objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetAllowedSnippetTemplates";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), null);
   }
   
   /**
    * Returns the allowed templates for the specified slot.
    * @param {ps.aa.ObjectId} the slot objectid (Required).
    * @return a Json array that will contain a Json object
    * for each template.
    * <pre>
    * Each object contains the following parameters:
    * 
    * variantid
    * name
    * description
    * objectid
    * </pre>
    */
   this.getItemTemplatesForSlot = function(objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetItemTemplatesForSlot";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), null);
   }
   
   /**
    * Returns the assembled content for the specified field.
    * @param {ps.aa.ObjectId} the slot objectid (Required)
    * @param {boolean} flag indicating that the content
    * should be decorated for active assembly.
    * @return html content.
    */
   this.getFieldContent = function(objectId, isAAMode)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetFieldContent";
      var params = null; 
      if(isAAMode)
      {
         params = new dojo.collections.Dictionary();
         params.add("isaamode", "true");
         if(___sys_aamode != undefined && ___sys_aamode != null)
            params.add("sys_aamode", new String(___sys_aamode));   
      }
      return this._makeRequest(
         action, this.MIMETYPE_HTML, objectId.serialize(), params);
   }
   
   /**
    * Returns the assembled content for the specified slot.
    * This method should be used to get snippet content too,
    * because assembly API is not granular enough to get only a snippet.
    * @param {ps.aa.ObjectId} the slot objectid (Required)
    * @param {boolean} flag indicating that the content
    * @return html content.
    */
   this.getSlotContent = function(objectId, isAAMode)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetSlotContent";
      var params = null; 
      if(isAAMode)
      {
         params = new dojo.collections.Dictionary();
         params.add("isaamode", "true");
         if(___sys_aamode != undefined && ___sys_aamode != null)
            params.add("sys_aamode", new String(___sys_aamode));
      }
      return this._makeRequest(
         action, this.MIMETYPE_HTML, objectId.serialize(), params);
   }
   
   /**
    * Returns the assembled content for the specified snippet.
    * @param {ps.aa.ObjectId} the slot objectid (Required)
    * @param {boolean} flag indicating that the content
    * @param {String} selected text (Optional).
    * @return html content.
    */
   this.getSnippetContent = function(objectId, isAAMode, selectedtext)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetSnippetContent";
      var params = null;
		if(isAAMode)
      {
         params = new dojo.collections.Dictionary();
         params.add("isaamode", "true");
         if(___sys_aamode != undefined && ___sys_aamode != null)
            params.add("sys_aamode", new String(___sys_aamode));
      }
		if(selectedtext != undefined && 
		   selectedtext != null && selectedtext.length > 0)
		{
			if(params == null)
			   params = new dojo.collections.Dictionary();
			params.add("rxselectedtext", encodeURIComponent(selectedtext));	
		}      
      return this._makeRequest(
         action, this.MIMETYPE_HTML, objectId.serialize(), params);
   }
	
    /**
    * Return the mime type of the assembled snippet.
    * @param {ps.aa.ObjectId} the slot objectid (Required)
    * @return a Json array that will contain a Json object.
    * <pre>
    * The object contains the following parameter(s):
    * 
    * mimetype
    * </pre> 
    */
    this.getSnippetMimeType = function(objectId)
    {
        dojo.lang.assertType(objectId, ps.aa.ObjectId);
        var action = "GetSnippetMimeType";
         return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), null);
    }

   /**
   * Returns the assembled slot content for snippet picker dialog.
   * @param {ps.aa.ObjectId} the slot objectid (Required)
   * @param {boolean} flag if true gets titles of the snippets otherwise
   * gets the assembled snippets.
   * @return html content.
   */
   this.getRenderedSlotContent = function(objectId, isTitles)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "GetSnippetPickerSlotContent";
      var params = null; 
      if(isTitles)
      {
         params = new dojo.collections.Dictionary();
         params.add("isTitles", "true");
      }      
      return this._makeRequest(
      action, this.MIMETYPE_HTML, objectId.serialize(), params);
   }

  /**
    * Removes a specified snippet (or relationship).
    *
    * @param {int} relationshipIds the comma separated list relationships 
    *    ids to be removed. This is a required parameter.
    */
   this.removeSnippet = function(relationshipIds)
   {
      var action = "RemoveSnippet";
      var params = new dojo.collections.Dictionary();
      params.add("relationshipIds", relationshipIds);      

      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }
   
   /**
    * Adds a specified (new) snippet.
    *
    * @param {ps.aa.ObjectId} snippetId id of the new snippet.
    * This is a required parameter.
    * @param {ps.aa.ObjectId} slotId the slot id of the new snippet.
    * This is a required parameter.
    * @param {String} folderPath the folder path of the new snippet.
    * This is an optional parameter.
    * @param {String} siteName the site name of the new snippet.
    * This is an optional parameter.
    */
   this.addSnippet = function(snippetId, slotId, folderPath, siteName)
   {
      dojo.lang.assertType(snippetId, ps.aa.ObjectId);
      dojo.lang.assertType(slotId, ps.aa.ObjectId);
      folderPath && dojo.lang.assertType(folderPath, String);
      siteName && dojo.lang.assertType(siteName, String);
      var action = "AddSnippet";
      var params = new dojo.collections.Dictionary();
      
      // add the required parameters
      params.add("dependentId", snippetId.getContentId());
      params.add("templateId", snippetId.getTemplateId());
      params.add("ownerId", slotId.getContentId());
      params.add("slotId", slotId.getSlotId());

      // add the optional parameters
      this.addOptionalParam(params, "folderPath", folderPath);
      this.addOptionalParam(params, "siteName", siteName);
         
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Checks in the specified item.
    *
    * @param {int} contentId the id of the to be checked in item.
    * @param {String} commentText the (optional) comment for the checkin action.
    */
   this.checkInItem = function(contentId, commentText)
   {
      var action = "Workflow";
      var params = new dojo.collections.Dictionary();
      
      // add the required parameters
      params.add("operation", "checkIn");
      params.add("contentId", contentId);
      this.addOptionalParam(params, "comment", commentText);
         
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }

   /**
    * Checks out the specified item.
    *
    * @param {int} contentId the id of the to be checked out item.
    * @param {String} commentText the (optional) comment for the checkin action.
    */
   this.checkOutItem = function(contentId, commentText)
   {
      var action = "Workflow";
      var params = new dojo.collections.Dictionary();
      
      // add the required parameters
      params.add("operation", "checkOut");
      params.add("contentId", contentId);
      this.addOptionalParam(params, "comment", commentText);
         
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }

   /**
    * Transition and check out the specified item.
    *
    * @param {int} contentId the id of the to be transitioned item.
    * @param {String} trigger the (required) trigger name of the transition.
    * @param {String} commentText the (optional) comment for the transition operation.
    * @param {String} adhocUsers the (optional) adhoc users. It is ';' delimited user names.
    */
   this.transitionCheckOutItem = function(contentId, trigger, commentText, adhocUsers)
   {
      var action = "Workflow";
      var params = new dojo.collections.Dictionary();
      
      params.add("operation", "transition_checkout");
      params.add("contentId", contentId);
      params.add("triggerName", trigger);
      this.addOptionalParam(params, "comment", commentText);
      this.addOptionalParam(params, "adHocUsers", adhocUsers);
         
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }

   /**
    * Transition the specified item.
    *
    * @param {int} contentId the id of the to be transitioned item.
    * @param {String} trigger the (required) trigger name of the transition.
    * @param {String} commentText the (optional) comment for the transition operation.
    * @param {String} adhocUsers the (optional) adhoc users. It is ';' delimited user names.
    */
   this.transitionItem = function(contentId, trigger, commentText, adhocUsers)
   {
      var action = "Workflow";
      var params = new dojo.collections.Dictionary();
      
      params.add("operation", "transition");
      params.add("contentId", contentId);
      params.add("triggerName", trigger);
      this.addOptionalParam(params, "comment", commentText);
      this.addOptionalParam(params, "adHocUsers", adhocUsers);
         
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }

   /**
    * Adds the specified key/value to the given parameters if the
    * value is not null or undefined.
    * 
    * @param {dojo.collections.Dictionary} params the parameters
    * @param {String} the name of the optional parameter. It may not be null or
    *    undefined.
    * @param {Object} value the optional parameter. It may be null or undefined. 
    */
   this.addOptionalParam = function(params, key, value)
   {
      if (value != null && (!dojo.lang.isUndefined(value)))
         params.add(key, value);  
   }
   
   /**
    * Retrieves the sort rank for the items relationship.
    * @param {int} relid the relationship id (Required)
    * @return the sort rank
    */
   this.getItemSortRank = function(relid)
   {
      var action = "GetItemSortRank";
      var params = new dojo.collections.Dictionary();
      params.add("sys_relationshipid", relid);
      
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }

   /**
    * Get all server properties.
    * @return a JSON object that contains a property for each
    * corresponding server property.
    */
   this.getServerProperties = function()
   {
      var action = "GetServerProperties";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, null);
   }
   
   /**
    * Get all registered sites from the system.
    * @return a list of sites as a JSON array.
    */
   this.getSites = function()
   {
      var action = "GetSites";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, null);
   }
   
   /**
    * Get all root folders (Children of //Folders in the CX) from the system.
    * @return a list of root folders as a JSON array.
    */
   this.getRootFolders = function()
   {
      var action = "GetRootFolders";
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, null);
   }

   /**
    * Resolves the id values for the passed in site and site folder.
   * @return a JSON object with two properties sys_folderid and
   * sys_siteid both of which may be <code>null</code> or empty.
   */
   this.resolveSiteFolders = function(siteName, folderPath)
   {
       var action = "ResolveSiteFolders";
      var params = new dojo.collections.Dictionary();
       params.add("folderPath", folderPath);
      params.add("siteName", siteName);
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Create a folder with supplied name under the parent folder supplied (by
    * path). The security/permissions and other properties of the new folder
    * will be inherited from the parent folder.
    * @param {string} parentFolderPath the parent folder path. Cannot be
    * <code>null</code> or empty. Must meet the following requirements:
    * <ul>
    * <li>Must start with '/'</li>
    * <li>The part between first '/' and the next '/' (or end) must be a site
    *   name registered in the system</li>
    * </ul>
    * @param {string} name the folder name. Cannot be <code>null</code>
    * or empty.
    * @param {boolean} isSiteFolder flag indicating that the newly created
    * folder should or should not be a site folder.
    * @return  a JSON object representing the newly
    * created folder, never <code>null</code> or empty.
    */
   this.createFolder = function(parentFolderPath, name, isSiteFolder)   
   {
      var action = "CreateFolder";
      var params = new dojo.collections.Dictionary();
      params.add("parentFolderPath", parentFolderPath);
      params.add("folderName", name);
      params.add("category", isSiteFolder ? "sites" : "folders");
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
    /**
    * Get the children of the supplied  folder path filtered for
    * the supplied content type and if a site folder then the slotid.
    * 
    * @param {string} parentFolderPath the parent folder path. Cannot be
    * <code>null</code> or empty. Must meet the following requirements:
    * <ul>
    * <li>Must start with '/'</li>
    * <li>For the site folders the part between first '/' and the next '/'
    * (or end) must be a site name registered in the system</li>
    * </ul>
    * @param {int} ctypeid the contenttypeid. "-1" if the content type id
    * is not specified and results should be returned for all content types.
    * Cannot be <code>null</code> or empty.
    * @param {int} slotid the contenttypeid. Cannot be <code>null</code>
    * or empty if isSiteFolder is <code>true</code>.
    * @param {boolean} isSiteFolder flag indicating that the folder in
    * question is a site folder.
    * @return  child folders and items of the supplied folder as JSON array.
    * Never <code>null</code> or empty.
    */
   this.getFolderChildren = function(parentFolderPath, ctypeid, slotid, isSiteFolder)
   {
      var action = "GetChildren";
      var params = new dojo.collections.Dictionary();
      params.add("parentFolderPath", parentFolderPath);
      params.add("sys_contenttypeid", ctypeid);
      if(isSiteFolder)
         params.add("sys_slotid", slotid);
      params.add("category", isSiteFolder ? "sites" : "folders");
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
    /**
    * Get the children of the supplied  folder path filtered for
    * the supplied content type and if a site folder then the slotid.
    * 
    * @param {string} parentFolderPath the parent folder path. Cannot be
    * <code>null</code> or empty. Must meet the following requirements:
    * <ul>
    * <li>Must start with '/'</li>
    * <li>For the site folders the part between first '/' and the next '/'
    * (or end) must be a site name registered in the system</li>
    * </ul>
    * @param {int} ctypeid the contenttypeid. Must be a valid content type id.
    * @param {boolean} isSiteFolder flag indicating that the folder in
    * question is a site folder.
    * @return  URL to open a content editor to create item of specified content 
    * type. Never <code>null</code> or empty.
    */
   this.getCreateItemUrl = function(parentFolderPath, ctypeid, isSiteFolder)
   {
      var action = "GetCreateItemUrl";
      var params = new dojo.collections.Dictionary();
      params.add("parentFolderPath", parentFolderPath);
      params.add("sys_contenttypeid", ctypeid);
      params.add("category", isSiteFolder ? "sites" : "folders");
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Gets the inline link parent ids for the specified dependent id. 
    * 
    * @param {int} dependentId The dependent id.
    * @param {Array} managedIds The managed ids as array of integers.
    * 
    * @return the response which contains the parent ids described above, 
    *   they must exist in the specified managed ids. 
    */
   this.getInlinelinkParentIds = function(dependentId, managedIds)
   {
      var action = "GetInlinelinkParents";
      var params = new dojo.collections.Dictionary();
      params.add("dependentId", dependentId);
      params.add("managedIds", dojo.json.serialize(managedIds));
      return this._makeRequest(
         action, this.MIMETYPE_JSON, null, params);
   }
   
   /**
    * Gets the value of the field from the content editor.
    * @param {ps.aa.ObjectId} of the field.
    * This is a required parameter.
    */
   this.getContentEditorFieldValue = function(objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);      
      var action = "GetContentEditorFieldValue";
      return this._makeRequest(
      action, this.MIMETYPE_HTML, objectId.serialize(), null);
   }
   
   /**
    * Sets the supplied value to the supplied fieldname that is part of the 
    * supplied objectid
    * @param {ps.aa.ObjectId} of the field.
    * This is a required parameter.
    */
   this.setContentEditorFieldValue = function(objectId, fieldValue)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var action = "SetContentEditorFieldValue";
      var params = new dojo.collections.Dictionary();
      params.add("fieldValue", fieldValue);      
      return this._makeRequest(
         action, this.MIMETYPE_JSON, objectId.serialize(), params);
   }
   
   /**
   * Builds action url for getting the related content search results.
   */
   this.getUpdateItemUrl = function()
   {
      return this._buildRequestUrl("UpdateItem",null);
   }
   
   /**
    * Retrieves the server session max timeout in seconds.
    */
   this.getMaxTimeout = function()
   {
      var action = "GetMaxTimeout";            
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, null);
   }
   
   /**
    * How many locales does the server support. If the request to the server
    * fails for any reason, the value is conservatively set to 1. The value is
    * cached after the 1st request.
    
    * Returns a value >= 1. 
    */
   this.getLocaleCount = function()
   {
      if (this.localeCount > -1)
         return this.localeCount;
         
      var action = "GetLocaleCount";
      var response = this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, null);      
      if(response.isSuccess())
      {
         this.localeCount = parseInt(response.getValue());
      }
      else
      {
         localeCount = 1;
      }
      return this.localeCount;
   }
   
   /**
    * Builds action url for getting the related content search results.
    */
   this.getRcSearchUrl = function()
   {
      return this._buildRequestUrl("GetSearchResults",null);
   }
      
   /**
    * Calls onsubmit on the supplied form object. If the form is not bound to 
    * to the supplied formObject alerts the user and returns null.
    * @param formObj the form object that needs to be submitted.
    * @return ps.io.Response object.
    */
    this.submitForm = function(formObj)
    {
       this.formSubmitResults = null;
       var fn = this.rcFormBind.bindArgs.formNode;
       var fid = (typeof(fn) == "string")?fn:fn.id;
       if(formObj.id != fid)
       {
          alert("Error occured submitting the form. The form is not bound.");
          return this.formSubmitResults;
       }
       formObj.onsubmit();
       return this.formSubmitResults;
    }
    
    /**
     * Initializes the formbind object with the supplied action and form. 
     * This method must be called before calling the submitForm.
     * @param action, name of the action 
     */
    this.initFormBind = function(reqUrl,formId,mimetype)
    {
      var _this = this;
      var result = new ps.io.Response();
      this.rcFormBind = new dojo.io.FormBind({
      url:reqUrl,
      sync: true,
      useCache: false,
      preventCache: true,
      mimetype: mimetype,
      formNode:formId,
      load: function(load, data, e) {
            if(load == "error")
            {
               result._m_success = false;
               var msg = ps.io.Actions._parseError(data.message, e);
               result._m_value = msg.message;
               result._m_errorcode = msg.errorCode;                     
            }
            else
            {
               result._m_success = true;
               if(typeof(data) == "object")
                  result._m_value = ps.io.Actions._flattenArrayProperties(data);
               else
                  result._m_value = data;
            }
            _this.formSubmitResults = result;
         }
      });
    }

   /**
    * Function to test calling the server .
    *
    * @param {string} mode
    * @return
    */
   this.test = function(mode)
   {
      var action = "Test";
      var params = new dojo.collections.Dictionary();
      params.add("mode", mode);      
      return this._makeRequest(
         action, this.MIMETYPE_PLAIN, null, params);
   }
   
   /**
    * Sends a test request to the server to keep the session alive.
    *
    */
   this.keepAlive = function()
   {
      // make a test request to the server to keep alive
      var aresponse = this.getMaxTimeout();
      if(!aresponse.isSuccess())
         return;
      var delay = parseInt(aresponse.getValue()) * 900;
      setTimeout("ps.io.Actions.keepAlive()", delay);
   }
   
   // Private functions
   
   /**
    * Builds the server request url for the action.
    * Requires that __rxroot was set with the correct server root.
    * @param {string} action the action name, assumed not <code>null</code>
    * or empty.
    * @param {objectId} the object id object, may be <code>null</code>.
    */
   this._buildRequestUrl = function(action, objectId)
   {
      var base = __rxroot + "/contentui/aa?action=" + action;
      if(objectId != null && objectId != undefined)
      {
         base += "&objectId=" + escape(objectId);
      }      
      return base;
   }
   
   /**
    * Gets request url and makes the request
    * @param {string} action the action name, assumed not <code>null</code>
    * or empty.
    * @param {string} the mimetype, assumed not <code>null</code> or
    * empty.
    * @param {objectId} the object id object, may be <code>null</code>.
    * @param {dojo.collections.Dictionary} a dictionary map of parameters
    * for the request. May be <code>null</code> or empty.
    */
   this._makeRequest = function(action, mimetype, objectId, params)
   {
      var transError = "XMLHttpTransport Error:";      
      var result = new ps.io.Response();
      var requestUrl = this._buildRequestUrl(action, objectId);
      var paramsObj = new Object();
      if(params != null && params != undefined)
      {                
         var keys = params.getKeyList();
         for(i = 0; i < keys.length; i++)
         {
            var val = params.item(keys[i]);
            if(val == null || val == "" || val == undefined)
               continue;
            paramsObj[keys[i]] = val;   
         }
      }
      dojo.io.bind({
               url: requestUrl,
               useCache: false,
               preventCache: true,
               mimetype: mimetype,
               method : "POST",
               content: paramsObj,
               sync: true,
               handler: function(type, data, e) 
               {
                  if(type == "error")
                  {
                     result._m_success = false;
                     var msg = ps.io.Actions._parseError(data.message, e);
                     result._m_value = msg.message;
                     result._m_errorcode = msg.errorCode;                     
                  }
                  else
                  {
                     result._m_success = true;
                     result._m_value = ps.io.Actions._flattenArrayProperties(data);
                  }
               }
           });
           return result;
   }
    
   /**
    * Helper method that parses a response error string
    */
   this._parseError = function(error, e)
   {
      var results = new Object();
      var transError = "XMLHttpTransport Error: ";
      if(dojo.string.startsWith(error, transError))
      {
         var temp = error.substring(transError.length);
         var eCode = parseInt(temp.substring(0, 3));
         var msg = temp.substring(4);
         results.errorCode = eCode;
         if(eCode === 404 || eCode === 0)
         {
            msg = ps.io.Actions.ERROR_MSG_NO_SERVER;
            eCode = 404;
         }
         else if(eCode === 500 && 
             dojo.string.has(e.responseText, ps.io.Actions.SESS_NOTAUTH_TEXT))
         {
           msg = ps.io.Actions.ERROR_MSG_REQUIRES_AUTH;
         }
         results.message = msg;
      }
      else
      {
         results.errorCode = "unknown";
         results.message = error;
      }
      return results;
      
   }
   
   /**
    * Flattens any single value array properties
    * @param {object} the object to be flattened
    * @return the modified object, never <code>null</code>
    */
   this._flattenArrayProperties = function(obj)
   {
      if (typeof(obj) != "object")
         return obj;
      var newObj = dojo.lang.isArrayLike(obj) ? [] : new Object();
      for (var item in obj)
      {
         var prop = obj[item];
         if(dojo.lang.isArrayLike(prop) && prop.length == 1)
         {
            newObj[item] = prop[0];            
         }
         else
         {
            newObj[item] = prop;
         }
      }
      return newObj;
   }

   /**
    * If the provided action response indicates an error,
    * reports the error to the user.
    * Does nothing if the response indicates success.
    * @param response an instance of {@link ps.io.Response}.
    * Not <code>null</code>.
    */
   this.maybeReportActionError = function (response)
   {
      dojo.lang.assertType(response, ps.io.Response);
      if (!response.isSuccess())
      {
         ps.error(response.getValue());
      }
   }
   
   /**
    * Constant for needs template error
    */
   this.NEEDS_TEMPLATE_ID = "needs_template_id";
   
   /**
    * Text to find to determine if the internal server error
    * is a missing auth error.
    */
   this.SESS_NOTAUTH_TEXT = "Processing Error: Not Authenticated";
   
   /**
    * Error message to be displayed if authentication is needed
    * for current session.
    */
   this.ERROR_MSG_REQUIRES_AUTH = "This request requires authentication, but the " +   
      "current session is not authenticated.\nThe user " +
       "session may have expired or the server may have been "+
      "restarted.\nYou must log back into Rhythmyx to continue.";
   
   /**
    * Error message to be display if a 404 error code comes
    * back from a request.
    */
   this.ERROR_MSG_NO_SERVER = "Unable connect to the Rhythmyx server.\nThe server may be down." +
      "\nPlease contact your Rhythmyx administrator.";
};
