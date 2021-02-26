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
package com.percussion.server.webservices;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemComponent;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.security.SecureStringUtils;
import com.percussion.server.PSRequest;
import com.percussion.services.purge.IPSSqlPurgeHelper;
import com.percussion.services.purge.PSSqlPurgeHelperLocator;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to handle all content data related operations for 
 * webservices. These operations are specified in the "ContentData" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSContentData}.
 */
public class PSContentDataHandler extends PSSearchHandler
{
   /**
    * This operation is used to retrieve a content item in standard item format.
    * see sys_StandardItem.xsd for more info.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void openItemAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      // get the rest of the paramters
      el = PSXMLDomUtil.getNextElementSibling(el, EL_INCLUDECHILDREN);
      boolean includeChildren = PSXMLDomUtil.getBooleanElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_INCLUDERELATED);
      boolean includeRelated = PSXMLDomUtil.getBooleanElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_INCLUDEBINARY);
      boolean includeBinary = PSXMLDomUtil.getBooleanElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_CHECKOUT);
      boolean checkOut = PSXMLDomUtil.getBooleanElementData(el);

      if (checkOut)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);

      PSServerItem theItem = openItem(request, includeBinary);

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(
         theItem.toMinXml(
            parent,
            true,
            includeChildren,
            includeRelated,
            includeBinary));
   }

   /**
    * This operation is used to retrieve a child table of a specific
    * content id in standard item format.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void openChildAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      // get the rest of the paramters
      el = PSXMLDomUtil.getNextElementSibling(el, EL_CHILDNAME);
      String childName = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_CHECKOUT);
      boolean checkOut = PSXMLDomUtil.getBooleanElementData(el);

      if (checkOut)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);

      PSServerItem theItem = openItem(request, false);
      PSItemChild theChild = theItem.getChildByName(childName);

      if (theChild == null)
      {
         String[] args = 
         {
            childName,
            PSItemDefManager.getInstance().contentTypeIdToName(
                  theItem.getContentTypeId())
         };
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_ITEM_CHILD_NOT_FOUND, args);
      }

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(theItem.toMinXml(parent, false, false, false, false));

      respEl.appendChild(theChild.toXml(parent));
   }

   /**
    * This operation is used to retrieve all related content of a specific
    * content id in standard item format.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void openRelatedAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      // get the rest of the paramters
      el = PSXMLDomUtil.getNextElementSibling(el, EL_CHECKOUT);
      boolean checkOut = PSXMLDomUtil.getBooleanElementData(el);

      if (checkOut)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);

      PSServerItem theItem = openItem(request, false);

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(theItem.toMinXml(parent, false, false, true, false));
   }

   /**
    * This operation is used to retrieve a specific field of a specific
    * content id in standard item format.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void openFieldAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      Element el = validateContentKey(request);

      // get the rest of the paramters
      el = PSXMLDomUtil.getNextElementSibling(el, EL_FIELDNAME);
      String fieldName = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_ATTACH);
      //      not used yet -mgb
      //      String attach = PSXMLDomUtil.getElementData(el);

      el = PSXMLDomUtil.getNextElementSibling(el, EL_CHECKOUT);
      boolean checkOut = PSXMLDomUtil.getBooleanElementData(el);

      if (checkOut)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);

      PSServerItem theItem = openItem(request, false);
      PSItemField theField = theItem.getFieldByName(fieldName);

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(theItem.toMinXml(parent, false, false, false, false));

      respEl.appendChild(theField.toXml(parent));
   }

   /**
    * Private helper function to return a loaded PSServerItem object.
    *
    * @param request the original request sent to the handler,
    *    assumed not <code>null</code>
    * 
    * @param includeBinary flag indicating that binary data types should be 
    *    loaded
    *
    * @return PSServerItem completely loaded if the item exists,
    *    never <code>null</code>
    *    
    * @throws PSException
    */
   private PSServerItem openItem(PSRequest request, boolean includeBinary) 
      throws PSException
   {
      long contentTypeId = lookupContentTypeId(request);

      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef =
         mgr.getItemDef(contentTypeId, request.getSecurityToken());


      String id = SecureStringUtils.srp(
              request.getParameter(IPSHtmlParameters.SYS_CONTENTID));

      String revision = SecureStringUtils.srp(
              request.getParameter(IPSHtmlParameters.SYS_REVISION));

      if(!NumberUtils.isCreatable(id) || !NumberUtils.isCreatable(revision)){
         throw new PSException("Not a valid id.");
      }

      PSLocator loc =
         new PSLocator(id,revision);

      // get the content item
      PSServerItem theItem = new PSServerItem(itemDef);
      theItem.load(loc, request, includeBinary);

      return theItem;
   }

   /**
    * This operation is used to retrieve a blank copy of a new
    * content item, specified by the content type id.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void newItemAction(PSRequest request, Document parent) throws PSException
   {
      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element el = PSXMLDomUtil.getFirstElementChild(root, EL_CONTENTTYPE);
      String contentType = PSXMLDomUtil.getElementData(el);

      PSItemDefinition itemDef = getItemDefinition(request, contentType);

      // get a new server item
      PSServerItem newItem = new PSServerItem(itemDef);

      // load the defaults for this type
      newItem.load(null, request);

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(newItem.toXml(parent));
   }

   /**
    * This operation is used to make a clone of the specified content item.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void newCopyAction(PSRequest request, Document parent) throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request);

      newCopy(request);

      // since request has now been updated with the proper
      // content id, just call open item to get the new content item
      PSServerItem theItem = openItem(request, false);

      // append the xml from the PSServerItem
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(theItem.toMinXml(parent, true, true, true, false));
   }

   /**
    * Create a 'New Copy' for a specified item. This calls the relationship
    * handler to create a 'New Copy' relationship according to the 'New Copy'
    * relationship definition.
    * <p>Note: This was made public for testing purposes. It should not be
    * accessed by production code (previously it was package).
    * 
    * @param request The request object. It may not be <code>null</code>.
    *    Its parameters contain the locator of the item for which to create a 
    *    'New Copy', <code>IPSHtmlParameters.SYS_CONTENTID</code> and
    *    <code>IPSHtmlParameters.SYS_REVISION</code>.
    *
    * @return the original request which contains the locator of the 
    *    'New Copy' item, never <code>null</code>. 
    *    
    * @throws PSException
    */
   public void newCopy(PSRequest request) throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      String path =
         getContentEditorURL(request)
            + "?"
            + IPSHtmlParameters.SYS_COMMAND
            + "="
            + PSRelationshipCommandHandler.COMMAND_NAME
            + "&"
            + IPSHtmlParameters.SYS_RELATIONSHIPTYPE
            + "="
            + PSRelationshipConfig.TYPE_NEW_COPY;

      PSRequest newReq = makeInternalRequest(request, path).getRequest();
      request.setParameter(
         IPSHtmlParameters.SYS_CONTENTID,
         newReq.getParameter(IPSHtmlParameters.SYS_CONTENTID));
      request.setParameter(
         IPSHtmlParameters.SYS_REVISION,
         newReq.getParameter(IPSHtmlParameters.SYS_REVISION));
   }
   
   /**
    * This operation is used purge one or more content items.
    *
    * @param request the request object through which the contentid of all
    *    items to be purged are supplied, not <code>null</code>.
    * @param parent the parent document to add the response element to,
    *    not <code>null</code>.
    * @throws PSException if any error occurs.
    */
   void purgeItemsAction(PSRequest request, Document parent) throws PSException, PSValidationException {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
         
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      
      validatePurgeKeys(request);

      purgeItems(request);

      addResultResponseXml("success", 0, null, parent);
   }
   
   /**
    * Purge all content items for the supplied request.
    * 
    * @param request the request to be executed, not <code>null</code>. The 
    *    request must supply the <code>IPSHtmlParameters.SYS_CONTENTID</code>
    *    parameter either as single value or list with all content id's for
    *    which to purge the items. If the parameter
    *    <code>IPSHtmlParameters.SYS_CONTENTID</code> is not supplied the
    *    request does nothing.
    * @throws PSException if anything goes wrong making the request.
    */
   static void purgeItems(PSRequest request) throws PSException, PSValidationException {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
      Object[] idList = request.getParameterList(IPSHtmlParameters.SYS_CONTENTID);
      List<PSLocator> locList = new ArrayList<>();
      if (idList!=null) 
      {
         for (Object idObj : idList) 
         {
            int id = (idObj instanceof String) ? NumberUtils.toInt((String)idObj) : 0;
            if (id > 0) 
               locList.add(new PSLocator(id));
         }
      }
      purgeHelper.purgeAll(locList);
   }
   

   /**
    * Purge the specified content items.
    * 
    * @param request the request used for the purge operation, 
    *    not <code>null</code>.
    * @param itemIds the to be purged item ids, not <code>null</code>, may
    *    be empty. Do nothing if it is empty.
    *    
    * @throws PSException if anything goes wrong making the request.
    */
   public static void purgeItems(PSRequest request, List<String>itemIds)
           throws PSException, PSValidationException {
      if (request == null)
         throw new IllegalArgumentException("request may not be null.");
      if (itemIds == null)
         throw new IllegalArgumentException("itemIds may not be null.");
      
      if (itemIds.isEmpty())
         return; // do nothing

      IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
      List<PSLocator> locatorList = new ArrayList<>();
      for (String item : itemIds) {
         int id = NumberUtils.toInt(item);
         if (id>0)
            locatorList.add(new PSLocator(id));
      }
      purgeHelper.purgeAll(locatorList);

   }


   /**
    * Creates an <code>ContentKey</code> element for the specified item locator.
    * The created element have the standard-item namespace.
    * 
    * @param doc The document used to create the element, assume not 
    *    <code>null</code>.
    * @param loc The item locator, assume not <code>null</code>.
    * 
    * @return The created element, never <code>null</code>.
    */
   private Element makeContentKeyElement(Document doc, PSLocator loc)
   {
      Element el;
      el = PSItemComponent.createStandardItemElement(doc, EL_CONTENTKEY);
      el.setAttribute(ATTR_CONTENTID, "" + loc.getId());
      el.setAttribute(ATTR_REVISION, "" + loc.getRevision());
      
      return el;
   }

   /**
    * This operation is used to update the contents of a specific content item. 
    * If there is not content key defined, then an insert is called.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void updateItemAction(PSRequest request, Document parent) throws PSException
   {
      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element item = PSXMLDomUtil.getFirstElementChild(root, EL_ITEM);

      Element el = PSXMLDomUtil.getNextElementSibling(item, EL_CHECKIN);
      boolean checkIn = PSXMLDomUtil.getBooleanElementData(el);

      Element key = PSXMLDomUtil.getFirstElementChild(item, EL_CONTENTKEY);
      int id = PSXMLDomUtil.checkAttributeInt(key, ATTR_CONTENTID, false);
      int rev = PSXMLDomUtil.checkAttributeInt(key, ATTR_REVISION, false);

      if (id == -1 || rev == -1)
      {
         // no content id, must be doing an insert
         processInsertItemAction(request, parent);
         return;
      }
      request.setParameter(IPSHtmlParameters.SYS_CONTENTID, "" + id);
      request.setParameter(IPSHtmlParameters.SYS_REVISION, "" + rev);

      long typeId = lookupContentTypeId(request);

      PSLocator loc =
         new PSLocator(
            request.getParameter(IPSHtmlParameters.SYS_CONTENTID),
            request.getParameter(IPSHtmlParameters.SYS_REVISION));

      // update the item
      PSServerItem updateItem = updateItem(request, item, loc, typeId);

      // get the new locator key
      PSLocator newLoc = updateItem.getKey();

      Element contentKeyEl = makeContentKeyElement(parent, newLoc); 
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(contentKeyEl);

      // make sure to update the original request with the new content
      // id and revision number after the insert
      request.setParameter(
         IPSHtmlParameters.SYS_CONTENTID,
         "" + newLoc.getId());
      request.setParameter(
         IPSHtmlParameters.SYS_REVISION,
         "" + newLoc.getRevision());

      if (checkIn)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKIN);
   }

   /**
    * A helper method to update a specified item into the database.
    *
    * @param request The current request object, it may not be
    *    <code>null</code>.
    * @param item The to be updated item in XML. It may not be
    *    <code>null</code>.
    * @param loc The locator of the to be updated item. It may not be
    *    <code>null</code>.
    * @param typeId The content type id of the to be updated item. It must be
    *    greater than <code>0</code>.
    *    
    * @return The updated item, never <code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   public PSServerItem updateItem(
      PSRequest request,
      Element item,
      PSLocator loc,
      long typeId)
      throws PSException
   {
      // we are doing an update, get all the data from
      // the existing item first
      PSItemDefinition itemDef = getItemDefinition(request, "" + typeId);

      // get the original object
      PSServerItem updateItem = new PSServerItem(itemDef);
      updateItem.load(loc, request);

      // now overlay the item data into the original item
      updateItem.loadXmlData(item);

      // process any keyfield searches
      executeKeyFieldSearch(request, updateItem);

      // save the completed item
      updateItem.save(request);

      return updateItem;
   }

   /**
    * This operation is used to insert a new content item, based on the
    * specified content type.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   private void processInsertItemAction(PSRequest request, Document parent)
      throws PSException
   {
      // get the required parameters from the input document
      Document inputDoc = request.getInputDocument();

      Element root = inputDoc.getDocumentElement();
      Element item = PSXMLDomUtil.getFirstElementChild(root, EL_ITEM);

      Element el = PSXMLDomUtil.getNextElementSibling(item, EL_CHECKIN);
      boolean checkIn = PSXMLDomUtil.getBooleanElementData(el);

      String contentType =
         PSXMLDomUtil.checkAttribute(item, ATTR_CONTENTTYPE, true);

      processInsertItem(request, contentType, item);

      PSLocator newLoc =
         new PSLocator(
            request.getParameter(IPSHtmlParameters.SYS_CONTENTID),
            request.getParameter(IPSHtmlParameters.SYS_REVISION));

      Element contentKeyEl = makeContentKeyElement(parent, newLoc);
      Element respEl = parent.getDocumentElement();
      respEl.appendChild(contentKeyEl);

      if (checkIn)
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKIN);
   }

   /**
    * Package protected operation to insert a new content item into the system.
    * 
    * @param request The orignal request object, assumed not <code>null</code>.
    * @param contentType The specific content type to create the new item with,
    *    this is used to create a new <code>PSServerItem</code> object, must not
    *    be <code>null</code>.
    * @param item The xml representation of the item to be inserted, must not be
    *    <code>null</code>.
    */
   void processInsertItem(PSRequest request, String contentType, Element item)
      throws PSException
   {
      PSItemDefinition itemDef = getItemDefinition(request, contentType);

      // get the original object
      PSServerItem newItem = new PSServerItem(itemDef);

      // now overlay the item data into the original item
      newItem.loadXmlData(item);

      // process any keyfield searches
      executeKeyFieldSearch(request, newItem);

      // save the completed item
      newItem.save(request);

      // get the new locator key
      PSLocator newLoc = newItem.getKey();

      // make sure to update the original request with the new content
      // id and revision number after the insert
      request.setParameter(
         IPSHtmlParameters.SYS_CONTENTID,
         "" + newLoc.getId());
      request.setParameter(
         IPSHtmlParameters.SYS_REVISION,
         "" + newLoc.getRevision());
   }

   /**
    * This operation is used to promote the specific content item to a new
    * revision. This is effectively done by checking out a specific revision
    * and then checking it back in, making sure that the checkout updates the
    * revision id.
    *
    * @param request The request object. It may not be <code>null</code>.
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException if any error occurs.
    */
   void promoteRevisionAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request);

      try
      {
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);
      }
      catch (Exception e)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_PROMOTE_FAILED_CHECKOUT, 
            e.getLocalizedMessage());
      }
      
      try
      {
         executeCheckInOut(request, IPSConstants.TRIGGER_CHECKIN);
      }
      catch (Exception e)
      {
         throw new PSException(
            IPSWebServicesErrors.WEB_SERVICE_PROMOTE_FAILED_CHECKIN, 
            e.getLocalizedMessage());
      }

      addResultResponseXml("success", 0, null, parent);
   }

   /**
    * The resource path used to purge content items.
    */
   public final static String PURGE_PATH = "sys_cxSupport/purgecontent.html";

   /**
    * Constants for XML elements/attributes defined in the 
    * schema <code>sys_ContentData.xsd</code>
    */
   private static final String EL_INCLUDECHILDREN = "IncludeChildren";
   private static final String EL_INCLUDERELATED = "IncludeRelated";
   private static final String EL_INCLUDEBINARY = "IncludeBinary";
   private static final String EL_CHECKOUT = "CheckOut";
   private static final String EL_CHECKIN = "CheckIn";
   private static final String EL_CHILDNAME = "ChildName";
   private static final String EL_FIELDNAME = "FieldName";
   private static final String ATTR_CONTENTTYPE = "contentType";
   private static final String EL_ATTACH = "AttachType";
   private static final String EL_ITEM = "Item";
}
