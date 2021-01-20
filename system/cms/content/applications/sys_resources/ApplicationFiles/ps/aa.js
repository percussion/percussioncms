/******************************************************************************
 *
 * [ ps.aa.js ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

dojo.provide("ps.aa");

dojo.require("dojo.json");
dojo.require("dojo.event");

// call the ps.aa.controller.init() after dojo has passed the HTML content
dojo.event.connect(dojo, "loaded", init);

function init()
{
   djConfig.isDebug = true;
   djConfig.debugAtAllCosts = true;
   if(window.__isAa){
      ps.aa.controller.init();
   }
}

/**
 * The constructor and the place holder for the singleton ps object.
 */
ps = new function(){}

/**
 * The constructor for the singleton ps.aa object.
 */
ps.aa = new function()
{
   /**
    * The 'class' attribute for a page node.
    */
   this.PAGE_CLASS = "PsAaPage";

   /**
    * The 'class' attribute for a slot node
    */
   this.SLOT_CLASS = "PsAaSlot";

   /**
    * The 'class' attribute for a snippet node.
    */
   this.SNIPPET_CLASS = "PsAaSnippet";

   /**
    * The 'class' attribute for a field node
    */
   this.FIELD_CLASS = "PsAaField";

   /**
    * The name of the attribute of <a> tag element that has the object id.
    */
   this.OBJECTID_ATTR = "PsAaObjectId";
}

/**
 * Constructs an object from a JSON string.
 * 
 * @param {String} idString The JSON string which is created by 
 *    {@link com.percussion.content.ui.aa.PSAAObjectId} on the server side.
 *    It is an array of pre-defined values, some like:
 *       '["2","372","3","503","301",null,"0","0","311","0","518","1728",null]'
 */
ps.aa.ObjectId = function(idString)
{

   /**
    * widget flag. This is used to see if the objectId 
    * is created from a widget or image.
    */
    this.widget = null;
    
   /**
    * Stores the id in serialized format as in 'string'
    */
    //FIXME: make this more generic.
   this.idString = idString;
   // gets the real id if has image marker.
   if (dojo.string.startsWith(idString, ps.aa.ObjectId.IMG_PREFIX, false)) 
   {
      this.idString = idString.substring(ps.aa.ObjectId.IMG_PREFIX.length, 
         idString.length);
      this.widget = ps.aa.ObjectId.IMG_PREFIX;
   }
   else if (dojo.string.startsWith(idString, 
            ps.aa.ObjectId.TREE_NODE_WIDGET, false)) 
   {
      this.idString = idString.substring(ps.aa.ObjectId.TREE_NODE_WIDGET.length, 
      idString.length);
      this.widget = ps.aa.ObjectId.TREE_NODE_WIDGET;
      
   }
    
   /**
    * Stores the list of values into array.
    */
   this.idobj = dojo.json.evalJson(this.idString);
   
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
   this.serialize = function ()
   {
      return this.idString;
   }
   
   /**
    * Clones the object id.
    * @return a deep copy of the current object id
    */
   this.clone = function ()
   {
      return new ps.aa.ObjectId(this.serialize());
   }
   
   /**
    * Call this.serialize()
    */
   this.toString = function ()
   {
      return this.serialize();
   }
   
   /**
    * Determines the specified object id belongs to the same item as this one.
    * Both ids should be slot ids.
    *
    * @param {ps.aa.ObjectId} otherId the object id in question.
    * Not null.
    * 
    * @return 'true' if both ids describe objects belonging to the same item.
    */
   this.belongsToTheSameItem = function (otherId)
   {
      dojo.lang.assertType(otherId, ps.aa.ObjectId);
      dojo.lang.assert(otherId.isSlotNode(),
            "Expected slot node, but got " + otherId);
      dojo.lang.assert(this.isSlotNode(),
            "Can be called only on a slot node, not on " + this);

      return this.getRelationshipId() || otherId.getRelationshipId()
            ? this.getRelationshipId() === otherId.getRelationshipId()
            : this.getContentId() === otherId.getContentId();
   }
   
   this.isPageNode = function()
   {
      return this.idobj[ps.aa.ObjectId.NODE_TYPE] == 0;
   }

   this.isSlotNode = function()
   {
      return this.idobj[ps.aa.ObjectId.NODE_TYPE] == 1;
   }

   this.isSnippetNode = function()
   {
      return this.idobj[ps.aa.ObjectId.NODE_TYPE] == 2;
   }
   
   /**
    * Indicates that this id is a snippet id.
    * After this method is called {@link #isSnippetNode} will return
    * <code>true</code>.
    */
   this.setSnippetNode = function()
   {
      this.idobj[ps.aa.ObjectId.NODE_TYPE] = 2;
      this._resetIdString();
   }
   
   this.isFieldNode = function()
   {
      return this.idobj[ps.aa.ObjectId.NODE_TYPE] == 3;
   }

   this.isCheckout = function()
   {
      if (this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS] != "0")
         return true;
      return false;
   }

   this.isCheckoutByMe = function()
   {
      return this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS] == "1";
   }

   this.getContentId = function()
   {
      return this.idobj[ps.aa.ObjectId.CONTENT_ID];
   }
   
   this.setContentId = function(id)
   {
      this.idobj[ps.aa.ObjectId.CONTENT_ID] = id;
      this._resetIdString();
   }

   /**
    * Sets the checkout status to the specified value.
    * 
    * @param {Number} status the new status, which must be 0, 1, or 2.
    */
   this.setCheckoutStatus = function(status)
   {
      dojo.lang.assert((status === "0" || status === "1" || status === "2"), "status must be 0, 1, or 2");
      this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS] = status;
      this._resetIdString();
   }
   
   /**
    * Gets the checkout status.
    * 
    * @return {Number} 
    */
   this.getCheckoutStatus = function(status)
   {
      return this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS];
   }

   this.getTemplateId = function()
   {
      return this.idobj[ps.aa.ObjectId.TEMPLATE_ID];
   }

   this.setTemplateId = function(templateId)
   {
      this.idobj[ps.aa.ObjectId.TEMPLATE_ID] = templateId;
      this._resetIdString();
   }

   this.getSlotId = function()
   {
      return this.idobj[ps.aa.ObjectId.SLOT_ID];
   }
   
   this.setSlotId = function(slotId)
   {
      this.idobj[ps.aa.ObjectId.SLOT_ID] = slotId;
      this._resetIdString();
   }

   this._resetIdString = function ()
   {
      this.idString = dojo.json.serialize(this.idobj);      
   }   

   this.getRelationshipId = function()
   {
      return this.idobj[ps.aa.ObjectId.RELATIONSHIP_ID];
   }

   this.getContext = function()
   {
      return this.idobj[ps.aa.ObjectId.CONTEXT];
   }

   this.getAuthType = function()
   {
      return this.idobj[ps.aa.ObjectId.AUTHTYPE];
   }

   this.getSiteId = function()
   {
      return this.idobj[ps.aa.ObjectId.SITE_ID];
   }

   this.setSiteId = function(id)
   {
      this.idobj[ps.aa.ObjectId.SITE_ID] = id;
	  this._resetIdString();
   }

   this.getFolderId = function()
   {
      return this.idobj[ps.aa.ObjectId.FOLDER_ID];
   }

   this.setFolderId = function(id)
   {
      this.idobj[ps.aa.ObjectId.FOLDER_ID] = id;
	  this._resetIdString();
   }

   this.getContentTypeId = function()
   {
      return this.idobj[ps.aa.ObjectId.CONTENTTYPE_ID];
   }
   
   this.getFieldName = function()
   {
      return this.idobj[ps.aa.ObjectId.FIELD_NAME];
   }
   
   this.getFieldLabel = function()
   {
      return this.idobj[ps.aa.ObjectId.FIELD_LABEL];
   }
   
   /**
    * Sort rank of the object in its container.
    * Can be null.
    */
   this.getSortRank = function()
   {
      return this.idobj[ps.aa.ObjectId.SORT_RANK];
   }

   this.setSortRank = function(sortRank)
   {
      this.idobj[ps.aa.ObjectId.SORT_RANK] = sortRank + "";
      this._resetIdString();
   }

   /**
    * @return {int} the content id of the parent. It may be null if not defined.
    */
   this.getParentId = function()
   {
      return this.idobj[ps.aa.ObjectId.PARENT_ID];
   }
   
   /**
    * Gets the dojo widget id of the node in the aa tree corresponding
    * to this object .
    * @return {string} dojo widget id.
    */
   this.getTreeNodeWidgetId = function ()
   {
      return ps.aa.ObjectId.TREE_NODE_WIDGET + this.serialize();
   }
   
   /**
    * Build the image path according to the rules below:
    * 
    * 1. If supplied path is null make treat it as empty string.
    * 2. Pick image file name for appropriate for the object class that is page, 
    *    snippet, slot or field.
    * 3. Build the result by concatenating
    *    1. the path from step1
    *    2. image name from step 2
    *    3. '_'
    *    4. check out status
    *    5. ".gif".
    *
    * @param path path relative to the server root or absolute path, may be 
    * null in which case it is treated as empty string. Please note that it 
    * makes sure a "/" exists before the file name. If you want the image path 
    * to be relative to current location, supply path as ".".
    * @return image path as mentioned above, never null or empty. The return 
    * value will be something like "../sys_resources/images/slot0.gif";
    */
   this.getImagePath = function(path)
   {
      if(path == null)
         path = "";
      
      if(path.substring(path.length-1)!='/')
         path = path+'/';
      var objClass;
      if(this.isPageNode())
      {
         objClass = ps.aa.PAGE_CLASS;
      }
      else if(this.isSnippetNode())
      {
         objClass = ps.aa.SNIPPET_CLASS;
      }
      else if(this.isSlotNode())
      {
         objClass = ps.aa.SLOT_CLASS;
      }
      else if(this.isFieldNode())
      {
         objClass = ps.aa.FIELD_CLASS;
      }
      return path + ps.aa.ObjectId.ImageNames[objClass] + "_" + 
         this.idobj[ps.aa.ObjectId.CHECKOUT_STATUS] + ".gif"; 
   }
   
   /**
    * 
    */
   this.getAnchorId = function()
   {
      return "img." + this.serialize();
   }
}

/**
 * The index of the Active Assembly node type
 *  0 - parent page node.
 *  1 - slot node.
 *  2 - snippet node.
 *  3 - field node.
 */
ps.aa.ObjectId.NODE_TYPE = 0;

/**
 * The index of the 'content id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.CONTENT_ID = 1;

/**
 * The index of the 'template id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.TEMPLATE_ID = 2;

/**
 * The index of the 'site id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.SITE_ID = 3;

/**
 * The index of the 'folder id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.FOLDER_ID = 4;

/**
 * The index of the 'context' used for the node. It is null if not defined.
 */
ps.aa.ObjectId.CONTEXT = 5;

/**
 * The index of the 'authtype' used for the node. It is null if not defined.
 */
ps.aa.ObjectId.AUTHTYPE = 6;

/**
 * The index of the 'content type id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.CONTENTTYPE_ID = 7;

/**
 * The index of the 'check out status' of the node. It is null if not defined.
 *  0 - if the item is not checked out by any user.
 *  1 - if the item is checked out by the current user.
 *  2 - if the item is checked by a user than current user
 */
ps.aa.ObjectId.CHECKOUT_STATUS = 8;

/**
 * The index of the 'slot id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.SLOT_ID = 9;

/**
 * The index of the 'relationship id' of the node.  It is null if not defined.
 */
ps.aa.ObjectId.RELATIONSHIP_ID = 10;

/**
 * The index of the 'field name' of the node. It is null if not defined.
 */
ps.aa.ObjectId.FIELD_NAME = 11;

/**
 * The index of the 'parent content id' of the node. It is null if not defined.
 */
ps.aa.ObjectId.PARENT_ID = 12;

/**
 * The index of the 'field label' of the node. It is null if not defined.
 */
ps.aa.ObjectId.FIELD_LABEL = 13;

/**
 * The index of the 'sort rank' of the node. It is null if not defined.
 */
ps.aa.ObjectId.SORT_RANK = 14;

/**
 * The prefix to be attached to the objectId.toString()
 */
ps.aa.ObjectId.TREE_NODE_WIDGET = 'aatree_';

/**
 * The prefix of the id for the anchor element that contains a managed node, 
 * slot, snippet or field.
 */
ps.aa.ObjectId.IMG_PREFIX = 'img.';

/**
 * Icon names. These are pure names only without any path or extension.
 */
ps.aa.ObjectId.ImageNames = new Object();
ps.aa.ObjectId.ImageNames[ps.aa.PAGE_CLASS] = "page";
ps.aa.ObjectId.ImageNames[ps.aa.SNIPPET_CLASS] = "snippet";
ps.aa.ObjectId.ImageNames[ps.aa.SLOT_CLASS] = "slot";
ps.aa.ObjectId.ImageNames[ps.aa.FIELD_CLASS] = "field";