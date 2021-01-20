/******************************************************************************
 *
 * [ ps.aa.Page.js ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.aa.Page");

dojo.require("dojo.lang.assert");
dojo.require("dojo.lang.common");
dojo.require("dojo.html");

dojo.require("ps.aa");


/**
 * The page (view) controller. It manages the viewable element of the Active 
 * Assembly page.
 */
ps.aa.Page = new function()
{
   /**
    * The actived div element.
    */
   this.activeDiv = null;

   /**
    * Initialize the properties of the page. This should be called right after 
    * the dojo is done passing the HTML content.
    */
   this.init = function()
   {
   },
   
   /**
    * Activate the specified div element and deactivate the previous activated
    * div element.
    *
    * @param {HTMLDivElement} div The to be activated div element.
    * Not <code>null</code>.
    * @return <code>true</code> if the selected element was changed,
    * otherwise returns <code>false</code>.
    */
   this.activate = function(div)
   {
      dojo.lang.assert(div);
      if (div === this.activeDiv)
         return false;

      // reset it back the previous activated div element if any
      if (this.activeDiv!=null)
      {
         this.activeDiv.style.border = "1px dotted";
         if(___sys_aamode == 1)
         {
            this.activeDiv.style.borderColor = "transparent";
         }
         else
         {
            this.activeDiv.style.borderColor = "gray";
         }
         
      }
      
      // activate the specified div element
      var pxSize = dojo.render.html.ie ? "3" : "2";
      div.style.border = pxSize + "px dotted";
      div.style.borderColor = "gray";
      
      this.activeDiv = div;
      return true;
      
      // FIXME: the following code does not work with IE7
      // if(activeDiv!=null)
      //   dojo.html.scrollIntoView(activeDiv);
   },
   
   /**
    * Gets the parent id (on the immidiate parent/slot node) for the specified 
    * snippet node. 
    * 
    * @param {HTMLDivElement} snippetNode The snippet node.
    * @param {ps.aa.ObjectId} objId The object id of the snippet node.
    */
   this.getParentId = function(snippetNode, objId)
   {
      var parentNode = null;
      var childNode = snippetNode;
      while (parentNode == null)
      {
         var node = childNode.parentNode;
         if (node == null || dojo.lang.isUndefined(node))
            break;

         if (dojo.html.getClass(node) === ps.aa.SLOT_CLASS
               && dojo.html.isTag(node, 'div'))
            parentNode = node;
            
         childNode = node;            
      }
      
      var parentId = null;
      if (parentNode != null)
         parentId = this.getObjectId(parentNode);
         
      if (parentId == null)
      {
         alert("Cannot find parent node for snippet: " + objId.serialize());
         return null;
      }
      else
      {
         return parentId;
      }
   },

   /**
    * Gets the object id from the given HTML element.
    * 
    * @param {HTMLElement} htmlElem the HTML element that may contains the
    *    object id as an attribute.
    * 
    * @return the ps.aa.ObjectId if it contains one; null otherwise.
    */
   this.getObjectId = function (htmlElem)
   {
      if (dojo.lang.isUndefined(htmlElem) || htmlElem == null)
         return null;
         
      var idString = null;
      
      // the object id is specified in the <a ...> tag at 'id' attribute with "img." prefix
      // it is also specified in <div ...> tag at 'id' attribute
      idString = dojo.html.getAttribute(htmlElem, "id");

      if (idString != null && (dojo.lang.isString(idString)))
      {
         var objId = new ps.aa.ObjectId(idString);
         if (objId != null && (!dojo.lang.isUndefined(objId)))
            return objId;
      }
      return null;
   },
   
   /**
    * Gets the element corresponding to the given object Id.
    * Throws an exception if the element with this id does not exist.
    * 
    * @param {ps.aa.ObjectId} objectId
    * @return {HTMLElement}
    */
   this.getElement = function (objectId)
   {
      dojo.lang.assertType(objectId, ps.aa.ObjectId);
      var element = document.getElementById(objectId.toString());
      dojo.lang.assert(element, "No element found for id " + objectId.toString());
      return element;
   }
   
};

