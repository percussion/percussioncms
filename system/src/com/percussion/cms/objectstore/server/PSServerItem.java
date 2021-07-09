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

package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.cms.handlers.PSPreviewCommandHandler;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.IPSVisitor;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidChildTypeException;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSDisplayError;
import com.percussion.design.objectstore.PSFieldValidationException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSConsole;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.transformation.converter.PSItemConverterUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A PSCoreItem that is Rhythmyx server aware.  Meaning it can persist itself
 * to the system.
 */
public class PSServerItem extends PSCoreItem implements IPSPersister
{
   /**
    * Construct a new object from the definition, default field values will not
    * be provided.
    *
    * @param itemDefinition must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the supplied
    * <code>itemDefinition</code> is <code>null</code>.
    * @throws PSCmsException - if an extraction error occurs while the
    * definition is being extracted.
    */
   public PSServerItem(PSItemDefinition itemDefinition) throws PSCmsException
   {
      super(itemDefinition);
   }

   /**
    * Constructs and loads this item in one step.  See
    * {@link #load(PSLocator, PSRequest)} for important information. Resulting
    * item data will not include binary field values.
    *
    * @param itemDef Defines the content type this item will represent.  May not
    * be <code>null</code>.
    * @param itemId The locator of the item to load, may be <code>null</code> to
    * load an item's default field values.
    * @param tok Used to determine access to the item.  May not be
    * <code>null</code>.
    *
    * @throws SecurityException if the user is not authorized.
    * @throws PSCmsException if error occurs processing the item definition or
    * loading the item.
    * @throws PSInvalidContentTypeException if content type specified by the
    * item def is invalid.
    */
   public PSServerItem(
      PSItemDefinition itemDef,
      PSLocator itemId,
      PSSecurityToken tok)
      throws PSCmsException, PSInvalidContentTypeException
   {
      this(itemDef);

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
       
      load(itemId, new PSRequest(tok));
   }

   /**
    * Constructs and loads this item in one step.  Same as 
    * {@link #PSServerItem(PSItemDefinition, PSLocator, PSSecurityToken)} with
    * an extra param that controls what type of data is loaded.  Only the extra
    * parameter is described below, see that method for other parameter and 
    * exception information.
    *
    * @param loadFlags Flags to indicate what types of data should be loaded.  
    * Any of the <code>TYPE_XXX</code> flags Or'd together.  
    *
    */
   public PSServerItem(
      PSItemDefinition itemDef,
      PSLocator itemId,
      PSSecurityToken tok,
      int loadFlags)
      throws PSCmsException, PSInvalidContentTypeException
   {
      this(itemDef);

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      load(itemId, new PSRequest(tok), loadFlags);
   }

   /**
    * Convenience constructor. Same as 
    * {@link #PSServerItem(PSItemDefinition, PSLocator, PSSecurityToken, int)}
    * with the 3nd parameter as a request object instead of security token.
    *   
    * @param req the request object, never <code>null</code>.
    */
   public PSServerItem(PSItemDefinition itemDef, PSLocator itemId,
      PSRequest req, int loadFlags) throws PSCmsException,
      PSInvalidContentTypeException
      {
         this(itemDef);

         if (req == null)
            throw new IllegalArgumentException("req may not be null");
         
         load(itemId, req, loadFlags);
      }

   
   /**
    * Factory method that obtains the appropriate item def for the item
    * referenced by the supplied locator and then constructs and returns an
    * instance of this class representing that item.  The resulting item will
    * not include binary field data.
    *
    * @param itemId The locator of the item to load, may not be
    * <code>null</code>.
    * @param tok Used to determine access to the item.  May not be
    * <code>null</code>.
    *
    * @return The item, never <code>null</code>.
    *
    * @throws SecurityException if the supplied token does not allow access.
    * @throws PSCmsException if error occurs loading the item.
    * @throws PSInvalidContentTypeException if the supplied item id is not
    * valid.
    */
   public static PSServerItem loadItem(PSLocator itemId, PSSecurityToken tok)
      throws PSCmsException, PSInvalidContentTypeException
   {
      if (itemId == null)
         throw new IllegalArgumentException("itemId may not be null");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition def = mgr.getItemDef(itemId, tok);

      return new PSServerItem(def, itemId, tok);
   }
   
   /**
    * Same as {@link #loadItem(PSLocator, PSSecurityToken)} with an extra param 
    * that controls what type of data is loaded.  Only the extra parameter is 
    * described below, see that method for other parameter and exception 
    * information.
    *
    * @param loadFlags Flags to indicate what types of data should be loaded.  
    * Any of the <code>TYPE_XXX</code> flags Or'd together.  
    */
   public static PSServerItem loadItem(PSLocator itemId, PSSecurityToken tok, 
      int loadFlags) throws PSCmsException, PSInvalidContentTypeException
   {
      if (itemId == null)
         throw new IllegalArgumentException("itemId may not be null");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition def = mgr.getItemDef(itemId, tok);

      return new PSServerItem(def, itemId, tok, loadFlags);
   }

   /**
    * Convenience method. Same as 
    * {@link #loadItem(PSLocator, PSSecurityToken, int)} with the 2nd parameter
    * as a request object.
    * 
    * @param req the request object, never <code>null</code>.
    */
   public static PSServerItem loadItem(PSLocator itemId, PSRequest req, 
         int loadFlags) throws PSCmsException, PSInvalidContentTypeException
      {
         if (itemId == null)
            throw new IllegalArgumentException("itemId may not be null");
         if (req == null)
            throw new IllegalArgumentException("req may not be null");

         PSItemDefManager mgr = PSItemDefManager.getInstance();
         PSItemDefinition def = mgr.getItemDef(itemId, req.getSecurityToken());

         return new PSServerItem(def, itemId, req, loadFlags);
      }

   
   /**
    * Same as {@link #load(PSLocator, PSRequest)} except that it takes a
    * security token instead of a request object.
    *
    * @param tok Used to determine access to the item.  May not be
    * <code>null</code>.
    */
   public void load(PSLocator itemId, PSSecurityToken tok)
      throws PSCmsException, PSInvalidContentTypeException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      load(itemId, new PSRequest(tok));
   }

   /**
    * Convenience method that calls 
    * {@link #load(PSLocator, PSRequest, boolean) load(itemId, request, false)}.
    */
   public void load(PSLocator itemId, PSRequest request)
      throws PSCmsException, PSInvalidContentTypeException
   {
      load(itemId, request, false);
   }
   
   /**
    * Convenience version of {@link #load(PSLocator, PSRequest, int)} that
    * includes the {@link #TYPE_BINARY} flag in the loadFlags only if 
    * <code>includeBinary</code> is <code>true</code>.  Includes all other 
    * <code>TYPE_xxx</code> flags.
    */
   public void load(PSLocator itemId, PSRequest request, boolean includeBinary)
      throws PSCmsException, PSInvalidContentTypeException
   {
      int loadFlags = TYPE_FIELDS | TYPE_CHILD | TYPE_RELATED;
      if (includeBinary)
         loadFlags |= TYPE_BINARY;
         
      load(itemId, request, loadFlags);
   }
   
   /**
    * Convenience version of {@link #load ##load(PSLocator, PSRequest)} that
    * includes the {@link #TYPE_FIELDS} flag always and the other load flags
    * depending on the supplied booleans.
    * 
    * @param loadRelatedItems this will turn on the {@link #TYPE_RELATED_ITEM}
    * flag. See {@link #TYPE_RELATED_ITEM} for detail.
    */
   public void load(PSLocator itemId, PSRequest request,
         boolean includeBinary, boolean includeChildren,
         boolean includeRelated, boolean loadRelatedItems)
      throws PSCmsException, PSInvalidContentTypeException
   {
      int loadFlags = TYPE_FIELDS;
      if (includeBinary)
         loadFlags |= TYPE_BINARY;
      if (includeChildren)
         loadFlags |= TYPE_CHILD;
      if (includeRelated)
         loadFlags |= TYPE_RELATED;
      if (loadRelatedItems)
         loadFlags |= TYPE_RELATED_ITEM;

      load(itemId, request, loadFlags);
   }

   /**
    * Convenience version of {@link #load(PSLocator, PSRequest, int)} that
    * includes the {@link #TYPE_FIELDS} flag always and the other loadd flags
    * depending on the supplied booleans.
    */
   public void load (PSLocator itemId, PSRequest request, 
      boolean includeBinary, boolean includeChildren, boolean includeRelated)
      throws PSCmsException, PSInvalidContentTypeException
   {
      int loadFlags = TYPE_FIELDS;
      if (includeBinary)
         loadFlags |= TYPE_BINARY;
      if (includeChildren)
         loadFlags |= TYPE_CHILD;
      if (includeRelated)
         loadFlags |= TYPE_RELATED;
         
      load(itemId, request, loadFlags);
   }
   
   /**
    * Convenience method that calls {@link #load(PSLocator, PSRequest, int, 
    * int)} with binary fields as specified and all other field types enabled. 
    */
   public void load(PSLocator itemId, PSRequest request, boolean includeBinary, 
      int communityId) throws PSCmsException, PSInvalidContentTypeException
   {
      int loadFlags = TYPE_FIELDS | TYPE_CHILD | TYPE_RELATED;
      if (includeBinary)
         loadFlags |= TYPE_BINARY;

      load(itemId, request, loadFlags, communityId);
   }

   /**
    * Convenience method that calls {@link #load(PSLocator, PSRequest, int, 
    * int) load(itemId, request, loadFlags, 
    * request.getSecurityToken().getCommunityId())}.
    */
   public void load(PSLocator itemId, PSRequest request, int loadFlags)
      throws PSCmsException, PSInvalidContentTypeException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      load (itemId, request, loadFlags, 
         request.getSecurityToken().getCommunityId());
   }
   
   /**
    * Loads this item from the storage component. This will populate the
    * item with definition and data.
    * 
    * @param itemId the item locator. May be <code>null</code> to load an 
    *    items defaults.
    * @param request the original request that prompted this update/insert, 
    *    not <code>null</code>.
    * @param loadFlags flags to indicate what types of data should be loaded.  
    *    Any of the <code>TYPE_XXX</code> flags Or'd together.  
    * @param communityId the community id to filter by, -1 to ignore this 
    *    filter.
    * @throws PSCmsException if an error loading the item occurs.
    * @throws PSInvalidContentTypeException if the content type is invalid.
    */
   public void load(PSLocator itemId, PSRequest request, int loadFlags,
      int communityId) throws PSCmsException, PSInvalidContentTypeException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      // TODO: remove once partial field loading is supported
      boolean includeFields = (m_loadFlags & TYPE_FIELDS) == TYPE_FIELDS;
      if (!includeFields)
         throw new UnsupportedOperationException(
            "loading without the TYPE_FIELDS flag is not supported.");

      boolean isNewItem = itemId == null ? true : false;
      m_loadFlags = loadFlags;

      if (!isNewItem)
      {
         setContentId(itemId.getId());
         setRevision(itemId.getRevision());
         setRequestedRevision(itemId.getRevision());
      }

      long id = getItemDefinition().getTypeId();

      try
      {
         getData(request, id, isNewItem, communityId);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSCmsException(
            IPSCmsErrors.MALFORMED_XML_DOCUMENT_UKNOWN_NODE_TYPE, e
               .getMessage());
      }
   }

   /**
    * Create new child entry for the specified child name that includes default
    * field values by making a new request to the child editor page.
    * 
    * @param request The request to use, may not be <code>null</code>.
    * @param name The child name, may not be <code>null</code> and must be an
    * existing child.
    * 
    * @return The entry, never <code>null</code>, will not have been added to
    * this item.
    * 
    * @throws PSCmsException if there are any errors. 
    * @throws PSUnknownNodeTypeException If the requested child editor is doc
    * is malformed.
    * @throws PSInvalidChildTypeException If the specified child name is 
    * invalid.
    */
   public PSItemChildEntry createChildEntry(PSRequest request, String name) 
      throws PSCmsException, PSUnknownNodeTypeException, 
      PSInvalidChildTypeException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSItemDefinition itemDef = getItemDefinition();
      PSItemChild child = getChildByName(name);
      if (child == null)
      {
         throw new PSInvalidChildTypeException(name, itemDef.getName());
      }
      
      PSItemChildEntry entry = child.createChildEntry();
      entry.setGUID(new PSLegacyGuid(getContentTypeId(), child.getChildId(), 
         entry.getChildRowId()));
      
      // the following code loads the choice values for all fields of the
      // child entry from Content Editor. This is consistent with the 
      // XML schema of the WSDL. However, the choice values have not been
      // loaded from other part of this API.
      //
      // The following code caused 2 issues:
      // [1] the returned object cannot be used to update/save (which caused
      //     failures for PSItemFieldMeta.equals(), which is called by 
      //     PSServerItem.save(PSRequest)
      // [2] the returned object is inconsistent with other part of the API
      //     in PSServerItem
      // 
      // Because the above, turn off the following code for now until we "fix"
      // the above 2 issues (in RX-14436).
      
      // TURN OFF the code that populates the choice values for all fields
      /**
       * String requestUrl = stripUrlExtras(itemDef.getEditorUrl());
       *
       * // calculate page id and add offset to get child edit page data
       * int pageId = itemDef.getPageId(name) + 1;
       *
       * // see if there are any entries to make:
       * // do request to get all non-binary entry values:
       * Document childDoc =
       *    getChildDocument(request, "" + pageId, requestUrl);      
       *
       * // load the child item field data
       * extractControlElementData(request, childDoc.getElementsByTagName(
       *   "Control"), entry, requestUrl, true);
       */
      return entry;
   }
   
   /**
    * Get's the data (default or otherwise) from the system via a request.
    * 
    * @param request the request used to get the data, assumed not 
    *    <code>null</code>.
    * @param contentTypeId the content type id of the data to fetch.
    * @param isNewItem <code>true</code> to indicate the data is loaded for
    *    a new (not persisted) item, <code>false</code> otherwise.
    * @param communityId the community id to filter by, -1 to ignore this 
    *    filter.
    */
   private void getData(PSRequest request, long contentTypeId,
      boolean isNewItem, int communityId) throws PSCmsException,
      PSInvalidContentTypeException, PSUnknownNodeTypeException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefSummary ids = mgr.getSummary(contentTypeId, communityId);

      PSCmsObject cmsObject = mgr.getCmsObject(contentTypeId);

      // if null, can't do anything
      if (ids == null)
         throw new PSCmsException(
            IPSCmsErrors.INVALID_CONTENT_TYPE_ID,
            mgr.contentTypeIdToName(contentTypeId));

      String requestUrl = stripUrlExtras(ids.getEditorUrl());

      // set parameters:
      Map<String, String> params = new HashMap<String, String>();
      if (!isNewItem)
      {
         params.put(IPSHtmlParameters.SYS_CONTENTID, "" + getContentId());
         params.put(IPSHtmlParameters.SYS_REVISION, "" + getRevision());

         // override the "showInPreview" attribute to "yes" for "preview" 
         // command
         params.put(IPSHtmlParameters.SYS_SHOW_IN_PREVIEW,
            IPSConstants.BOOLEAN_TRUE);
      
         params.put(IPSHtmlParameters.SYS_COMMAND,
            PSPreviewCommandHandler.COMMAND_NAME);
      }
      else
      {
         // cannot request new item with preview, and would only need one with
         // default values if editing anyhow
         params.put(IPSHtmlParameters.SYS_COMMAND, 
            PSEditCommandHandler.COMMAND_NAME);
      }

      // get parent doc:
      Document doc = makeRequest(requestUrl, request, params, false);

      if (doc == null)
         throw new PSCmsException(
            IPSCmsErrors.REQUIRED_DOCUMENT_MISSING_ERROR,
            requestUrl);

      // get data from control elements:
      extractControlElementData(
         request,
         doc.getElementsByTagName("Control"),
         this,
         requestUrl,
         isNewItem);

      if (!isNewItem)
      {
         // get data from content status elements:
         NodeList contentStatusList = doc.getElementsByTagName("ContentStatus");
         if (contentStatusList != null)
            extractContentStatusData(contentStatusList, cmsObject);

         // get Lock request
         extractLock(request);

         // get related data
         addRelatedContentData(request);
      }

      // get data from user session:
      extractLoginLocale(request);
   }

   /**
    * Makes a request to an app and extraces the Lock value and sets to
    * isRevisionLock.
    */
   private void extractLock(PSRequest request) throws PSCmsException
   {
      Map<String, Integer> params = new HashMap<String, Integer>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, new Integer(getContentId()));

      // get parent doc:
      Document doc =
         makeRequest(CONTENT_STATUS_APP_URL, request, params, false);

      if (doc == null)
         return;

      NodeList lockList = doc.getElementsByTagName("Lock");
      if (lockList != null)
      {
         Node lock = lockList.item(0);
         if (lock != null)
         {
            boolean isLocked;

            String theLock = PSXMLDomUtil.getElementData(lock);
            if (theLock == null
               || theLock.length() == 0
               || theLock.equals("N"))
               isLocked = false;
            else
               isLocked = true;

            isRevisionLocked(isLocked);
         }
      }
   }

   /**
    * Gets the Locale object from the user session.
    *
    * @param request  assumed not <code>null</code>
    */
   private void extractLoginLocale(PSRequest request)
   {
      PSUserSession session = request.getUserSession();
      if (session != null)
      {
         Object o =
            session.getPrivateObject(PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);

         if (o instanceof String && (((String) o).length() != 0))
         {
            Locale loc = PSI18nUtils.getLocaleFromString((String) o);

            if (loc != null)
               setSystemLocale(loc);
         }
      }
   }

   /**
    * Get the data from teh ContentStatus node.
    * @param contentStatus  assumed not <code>null</code>
    */
   private void extractContentStatusData(
      NodeList contentStatus,
      PSCmsObject cmsObject)
      throws PSUnknownNodeTypeException
   {
      if (contentStatus == null || contentStatus.getLength() < 0)
         throw new IllegalArgumentException("contentStatus node must be in xml and not be empty");

      // get first occurance only, ignore all others.
      Node cStatus = contentStatus.item(0);

      if (cmsObject.isRevisionable())
      {
         setCurrentRevision(
            PSXMLDomUtil.checkAttributeInt(
               (Element) cStatus,
               "currentRevision",
               false));

         setEditRevision(
            PSXMLDomUtil.checkAttributeInt(
               (Element) cStatus,
               "editRevision",
               false));

         // loop through chilren and grab username:
         Element child = PSXMLDomUtil.getFirstElementChild(cStatus);
         while (child != null)
         {
            if (!child.getNodeName().equalsIgnoreCase("CheckOutUserName"))
            {
               child = PSXMLDomUtil.getNextElementSibling(child);
               continue;
            }

            String user = PSXMLDomUtil.getElementData(child);
            setCheckedOutByName(user);

            break;
         }
      }

   }

   /**
    * Takes a <code>NodeList</code> of <code>Control</code> elements as 
    * described in the sys_ContentEditor.dtd and extracts field values from
    * them, setting the values on the <code>PSItemField</code>s of this item.
    * Binary field values are not loaded at this time, and instead are loaded
    * lazily when the binary value's data is requested (See 
    * {@link PSBinaryValue}) for more info.
    * @param nl   assumed not <code>null</code>
    * @param item   assumed not <code>null</code>
    * @throws PSUnknownNodeTypeException
    */
   private void extractControlElementData(
      PSRequest request,
      NodeList nl,
      IPSItemAccessor item,
      String requestUrl,
      boolean isNewItem)
      throws PSUnknownNodeTypeException, PSCmsException
   {
      boolean includeFields = (m_loadFlags & TYPE_FIELDS) == TYPE_FIELDS;
      boolean includeChildren = (m_loadFlags & TYPE_CHILD) == TYPE_CHILD;
      Node n = null;
      String paramName = null;
      PSItemDefinition itemDef = getItemDefinition();

      for (int i = 0; i < nl.getLength(); i++)
      {
         n = nl.item(i);
         if (n != null)
         {
            // get the paramName:
            paramName =
               PSXMLDomUtil.checkAttribute((Element) n, "paramName", true);


            if (itemDef.isComplexChild(paramName))
            {
               // if new item or not loading children, ignore children.
               if (isNewItem || !includeChildren)
                  continue;

               // calculate page id and add offset to get complete child data
               int pageId = itemDef.getPageId(paramName) + 
                  PSEditCommandHandler.COMPLETE_CHILD_PAGEID_OFFSET;

               // see if there are any entries to make:
               // do request to get all non-binary entry values:
               Document childDoc =
                  getChildDocument(request, "" + pageId, requestUrl);

               // get child by name:
               if (childDoc != null)
               {
                  NodeList rows = childDoc.getElementsByTagName("Row");
                  Node rowNode = null;
                  for (int k = 0; k < rows.getLength(); k++)
                  {
                     rowNode = rows.item(k);
                     if (rowNode != null)
                     {
                        PSItemChild child = getChildByName(paramName);
                        // no child... no service
                        if (child == null)
                           continue;

                        PSItemChildEntry entry = child.createAndAddChildEntry();

                        // get rowid:
                        int rowId =
                           PSXMLDomUtil.checkAttributeInt(
                              (Element) rowNode,
                              "childkey",
                              false);

                        // setRowId
                        entry.accept(new PSChildEntrySetter(rowId));
                        entry.setGUID(new PSLegacyGuid(getContentTypeId(), 
                           child.getChildId(), entry.getChildRowId()));


                        extractControlElementData(
                           request,
                           ((Element) rowNode).getElementsByTagName("Control"),
                           entry,
                           requestUrl,
                           isNewItem);
                     }
                  }
               }
            }
            else
            {
               // if not loading fields, skip unless for a child                              
               if (!((item instanceof PSItemChildEntry) || includeFields))
                  continue;
               
               // get field by name:
               PSItemField field = item.getFieldByName(paramName);

               // field doesn't exist so skip it
               if (field == null)
                  continue;

               // I know you can have options and not multi-value, but not
               // sure about the other way, so I check for both:
               if (field.isMultiValue()
                  || field.getItemFieldMeta().hasOptions())
               {
                  extractMultiValueData(field, (Element) n);
               }

               // else it's field, before getting a field, see if it's even
               // worth it see if there's a value node:
               Element valueNode =
                  PSXMLDomUtil.getFirstElementChild(n);

               if (valueNode != null
                  && valueNode.getNodeName().equals("Value"))
               {
                  String theValue =
                     PSXMLDomUtil.getElementData(valueNode);

                  if (theValue != null)
                  {
                     // set the value
                     field.addValue(field.createFieldValue(theValue));

                     // set the locale
                     if (paramName
                        .equalsIgnoreCase(IPSHtmlParameters.SYS_LANG))
                     {
                        Locale loc = PSI18nUtils.getLocaleFromString(theValue);
                        if (loc != null)
                           setDataLocale(loc);
                     }
                  }
               }
               else if (field.getItemFieldMeta().isBinary() && 
                  ((m_loadFlags & TYPE_BINARY) == TYPE_BINARY))
               {
                  int childRowId = -1;
                  if (item instanceof PSItemChildEntry)
                  {
                     PSItemChildEntry childitem = (PSItemChildEntry) item;
                     childRowId = childitem.getChildRowId();
                  }

                  //Create locator to get Binary data lazily
                  PSServerBinaryLocator blocator =
                     new PSServerBinaryLocator(
                        request,
                        getKey(),
                        field.getName(),
                        getContentTypeId(), childRowId);
                  PSBinaryValue binaryValue =
                     new PSBinaryValue(blocator);                  
                  field.addValue(binaryValue);                  
               }
            }
         }
      }
   }

   /**
    * Extracts the many values for the field.
    */
   private void extractMultiValueData(PSItemField field, Element control)
      throws PSUnknownNodeTypeException
   {
      NodeList displayEntryList = control.getElementsByTagName("DisplayEntry");

      Element displayEntry = null;
      String selectedVal = null;
      Element valueElement = null;
      String theValue = null;
      Element labelElement = null;
      String theLabel = null;
      for (int i = 0; i < displayEntryList.getLength(); i++)
      {
         // get the display entry:
         displayEntry = (Element) displayEntryList.item(i);

         // get get the value element:
         valueElement = PSXMLDomUtil.getFirstElementChild(displayEntry);

         // get value
         if (valueElement.getNodeName().equalsIgnoreCase("Value"))
            theValue = PSXMLDomUtil.getElementData(valueElement);

         // get label
         labelElement = PSXMLDomUtil.getNextElementSibling(valueElement);
         if (labelElement.getNodeName().equalsIgnoreCase("DisplayLabel"))
            theLabel = PSXMLDomUtil.getElementData(labelElement);

         // is it selected
         selectedVal =
            PSXMLDomUtil.checkAttribute(displayEntry, "selected", false);

         // for now all are text values:
         if (field.isMultiValue() && selectedVal.equalsIgnoreCase("yes"))
            field.addValue(new PSTextValue(theValue));

         field.getItemFieldMeta().addOptions(theLabel, theValue);
      }
   }

   /**
    * This operation is used to add all related content to the standard
    * item document passed in.  If {@link #m_loadFlags} does not include
    * {@link #TYPE_RELATED}, then this method returns without doing anything.
    *
    * @param request the request to use while retrieving the related content,
    *    assumed not <code>null</code>.
    * @throws PSCmsException when the related content lookup fails.
    */
   private void addRelatedContentData(PSRequest request) throws PSCmsException
   {
      // don't load if not requested
      if ((TYPE_RELATED & m_loadFlags) != TYPE_RELATED)
         return;
         
      PSActiveAssemblerProcessor processor = PSActiveAssemblerProcessor.getInstance();
      
      PSRelationshipSet rset = processor.getRelatedContent(
         new PSLocator(getContentId(), getRevision()));
      Iterator iter = rset.iterator();
      while (iter.hasNext())
      {
         PSRelationship rs = (PSRelationship) iter.next();
         PSItemRelatedItem relatedItem = new PSItemRelatedItem();
         relatedItem.setRelationship(new PSAaRelationship(rs));
         relatedItem.setRelatedType(rs.getConfig().getName());
         relatedItem.setRelationshipId(rs.getId());
         relatedItem.setDependentId(rs.getDependent().getId());
         Map propertyMap = rs.getUserProperties();
         Iterator mapIter = propertyMap.keySet().iterator();
         while (mapIter.hasNext())
         {
            String key = (String) mapIter.next();
            String value = (String) propertyMap.get(key);
            relatedItem.addProperty(key, value);
         }
         
         if ((TYPE_RELATED_ITEM & m_loadFlags) == TYPE_RELATED_ITEM)
         {
            Element relatedItemData = loadRelatedItem(rs.getDependent()
                  .getId(), request);
            relatedItem.setRelatedItemData(relatedItemData);
         }
         m_relatedItemsMap.put("" + rs.getId(), relatedItem);
      }
   }

   /**
    * Load the related server item from a given content ID. However, it only
    * loads the fields and its child entries if there is any, but it does not
    * load related items or binary data (if there is any).
    * 
    * @param contentId the ID of the content in question.
    * @param request the request to use while retrieving the related content,
    *    assumed not <code>null</code>.
    * 
    * @return the server item in XML format, never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private Element loadRelatedItem(int contentId, PSRequest request) 
      throws PSCmsException
   {
      try
      {
         PSComponentSummary sum = PSWebserviceUtils.getItemSummary(contentId);

         PSItemDefinition def = PSItemConverterUtils.getItemDefinition(sum
               .getContentTypeId());
         PSLegacyGuid guidId = new PSLegacyGuid(sum.getContentId());
         PSLocator loc = PSWebserviceUtils.getItemLocator(guidId);
         int loadFlags = PSServerItem.TYPE_FIELDS | PSServerItem.TYPE_CHILD;

         PSServerItem item = new PSServerItem(def, loc, request, loadFlags);

         Element xmlData = item
               .toXml(PSXmlDocumentBuilder.createXmlDocument());
         return xmlData;
      }
      catch (Exception e)
      {
         throw new PSCmsException(IPSCmsErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
   }

   /**
    * This class allows us to set members in <code>PSChildEntry</code>
    * through a visitor this sets the rowid.
    */
   private class PSChildEntrySetter implements IPSVisitor
   {
      /**
       * Create instance.
       * @param theRowId - the int to use as id.
       */
      PSChildEntrySetter(int theRowId)
      {
         rowId = theRowId;
      }

      /**
       * Called by the visitor.
       * 
       * @return the row id.
       */
      public Object getObject()
      {
         return new Integer(rowId);
      }

      /**
       * Holds rowid.
       */
      int rowId;
   }

   /**
    * Get's the child document.  This makes a request so taht all of the
    * complex child values will be return regardless of show in preview
    * or show in summary values.
    *
    * @param request assumed not <code>null</code>.
    * @param pageId
    * @param requestUrl assumed not <code>null</code>.
    * @return doc with the children.
    */
   private Document getChildDocument(
      PSRequest request,
      String pageId,
      String requestUrl)
      throws PSCmsException
   {
      // set parameters:
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(IPSHtmlParameters.SYS_CONTENTID, new Integer(getContentId()));
      params.put(IPSHtmlParameters.SYS_REVISION, new Integer(getRevision()));
      params.put(IPSHtmlParameters.SYS_COMMAND, "edit");
      params.put("sys_pageid", pageId);

      Document doc = makeRequest(requestUrl, request, params, false);

      return doc;
   }

   /**
    * Removes the "../" and ".html" from the content editor url.
    * 
    * @param theUrl Assumed not <code>null</code> and containing "." and "/".
    * @return the substring as described, never <code>null</code> or empty.
    */
   private String stripUrlExtras(String theUrl)
   {
      String ax = theUrl.substring(0, theUrl.lastIndexOf("."));
      return ax.substring(ax.indexOf("/") + 1, ax.length());
   }

   /**
    * Simplifies making an internal request by wrapping the request call in
    * a try/catch block. The supplied params are passed directly to the 
    * {@link PSServer#getInternalRequest(String, PSRequest, Map, boolean)}. See
    * that method for a description of the params.
    * 
    * @return The document returned by {@link PSInternalRequest#getResultDoc()}
    * (which is the object returned by the above mentioned method).
    * 
    * @throws PSCmsException If {@link PSInternalRequest#getResultDoc()} throws
    * and exception.
    */
   private Document makeRequest(String path, PSRequest request, Map params,
      boolean inherit) throws PSCmsException
   {
      try
      {
         PSInternalRequest iReq = PSServer.getInternalRequest(path, request, 
            params, inherit);

         return iReq.getResultDoc();
      }
      catch (PSInternalRequestCallException e)
      {
         Object[] args = 
         {
            path,PSServer.stackToString(e)
         };
         throw new PSCmsException(
            IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
   }

   /**
    * Convenience method that calls {@link #save(PSRequest) 
    * save(new PSRequest(tok)}.
    */
   public void save(PSSecurityToken tok) throws PSCmsException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      save(new PSRequest(tok));
   }

   /**
    * Convenience method that calls {@link #save(PSRequest, int) save(request, 
    * request.getSecurityToken().getCommunityId())}.
    */
   public void save(PSRequest request) throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      
      save(request, request.getSecurityToken().getCommunityId());
   }
   
   /**
    * Once a standard item object has been created use this function to
    * update the data for that item on the server. For an update, this
    * function assumes that we have a full standard item with all
    * the fields.
    *
    * @param request the request used to execute the save request.
    * @param communityId the community for which to save the item, -1 to 
    *    ignore the community.
    * @throws PSCmsException if there is any problem saving the item.
    */
   public void save(PSRequest request, int communityId) throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      try
      {
         validateRelatedItems(request);
         
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         PSItemDefSummary ids = mgr.getSummary(getContentTypeId(), communityId);

         String path = stripUrlExtras(ids.getEditorUrl());

         request.setParameter(IPSHtmlParameters.SYS_COMMAND, "modify");

         if (getContentId() != -1)
         {
            // we are doing an update set the appropriate values
            request.setParameter(
               IPSHtmlParameters.SYS_CONTENTID,
               "" + getContentId());
            request.setParameter(
               IPSHtmlParameters.SYS_REVISION,
               "" + getRevision());
         }
         saveAllFields(request, path);
         saveAllChilds(request, path);
         saveAllRelated(request, path);
         
         if (getContentId() == -1)
         {
            try
            {
               int contentid = Integer.parseInt(
                  request.getParameter(IPSHtmlParameters.SYS_CONTENTID));
               int revision = Integer.parseInt(
                  request.getParameter(IPSHtmlParameters.SYS_REVISION));
               
               setContentId(contentid);
               setRevision(revision);
               setCurrentRevision(revision);
               setEditRevision(revision);
            }
            catch (Exception e)
            {
               //Can this happen???
               return;
            }
         }
      }
      catch (PSInvalidContentTypeException ex)
      {
         throw new PSCmsException(IPSCmsErrors.INVALID_CONTENT_TYPE, 
            "" + getContentTypeId());
      }
   }
   
   /**
    * Validates the related type of all related items. Related types must be 
    * existing relationship configurations of category 
    * <code>rs_activeassembly</code>.
    * 
    * @param request the request used for the relationship processor, assumed
    *    not <code>null</code>.
    * @throws PSCmsException if the relationship processor cannot be created
    *    or any related item specifies an unknown or invalid related type.
    */
   private void validateRelatedItems(PSRequest request) throws PSCmsException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      Iterator relatedItems = getAllRelatedItems();
      while (relatedItems.hasNext())
      {
         PSItemRelatedItem relatedItem = 
            (PSItemRelatedItem) relatedItems.next();
         
         PSRelationshipConfig config = processor.getConfig(
            relatedItem.getRelatedType());
         if (config == null)
            throw new PSCmsException(IPSCmsErrors.UNKNOWN_RELATED_TYPE, 
               relatedItem.getRelatedType());
         
         if (!config.getCategory().equals(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
            throw new PSCmsException(IPSCmsErrors.INVALID_RELATED_TYPE, 
               relatedItem.getRelatedType());
      }
   }

   /**
    * Update all the fields of the specified content item, if the content id is
    * not set, we insert this as a new piece of content.
    *
    * @param request the request to work on, parameters already set to contentid
    * and revision when appropriate, assumed not <code>null</code>
    *
    * @param path the path to the content editor to access for the update
    * request, assumed not <code>null</code>
    *
    * @throws PSCmsException if it cannot get the value from the field
    */
   private void saveAllFields(PSRequest request, String path)
      throws PSCmsException
   {
      // don't do anything if we never loaded any fields      
      if ((m_loadFlags & TYPE_FIELDS) != TYPE_FIELDS)
         return;
         
      // update the fields
      Map<String, Object> fieldParams = new HashMap<String, Object>();
      fieldParams.put(PSContentEditorHandler.PAGE_ID_PARAM_NAME, "0");
      fieldParams.put(
         "DBActionType",
         (getContentId() == -1) ? "INSERT" : "UPDATE");

      Iterator fieldIter = getAllFields();
      populateFieldParams(fieldParams, fieldIter);

      processUpdateAction(path, request, fieldParams);
   }

   /**
    * Iterates over the supplied fields and adds entries to the params in 
    * preparation for calling 
    * {@link #processUpdateAction(String, PSRequest, Map)}.
    * 
    * @param fieldParams Map of html params to which field values are added,
    * assumed not <code>null</code>, key are <code>String</code> objects and
    * values are objects.
    * @param fieldIter An iterator over zero or more {@link PSItemField} 
    * objects, assumed not <code>null</code>.
    * 
    * @throws PSCmsException If there is an error converting a field value to 
    * it's string representation
    */
   private void populateFieldParams(Map fieldParams, Iterator fieldIter) 
      throws PSCmsException
   {
      while (fieldIter.hasNext())
      {
         PSItemField field = (PSItemField) fieldIter.next();

         // ignore the binary field if it has no value and never loaded.         
         if (field.getItemFieldMeta().isBinary() && field.getValue() == null
               && (m_loadFlags & TYPE_BINARY) != TYPE_BINARY)
         {
            continue;
         }
         else if (field.getValue() instanceof PSBinaryFileValue)
         {
            PSBinaryFileValue val = (PSBinaryFileValue) field.getValue();
            fieldParams.put(field.getName(), val.getTempFile());
         }
         else if (field.getValue() instanceof PSBinaryValue) // CMS-7974 : For Image type asset. No case for Binary value field type caused filed type to be set as String instead of PSPurgableTempFile and thus gave class cast exception in ImageAssetInputTranslation.
         {
            PSBinaryValue val = (PSBinaryValue) field.getValue();
            fieldParams.put(field.getName(), val.getValueFile());
         }
         else if (field.getItemFieldMeta().isMultiValueField())
         {
            List<String> newVals = new ArrayList<String>();
            Iterator values = field.getAllValues();
            while (values.hasNext())
            {
               IPSFieldValue val = (IPSFieldValue)values.next(); 
               newVals.add(val.getValueAsString());
            }
            fieldParams.put(field.getName(), newVals);
         }
         else
         {
            fieldParams.put(
               field.getName(),
               (field.getValue() == null)
                  ? ""
                  : field.getValue().getValueAsString());
         }
      }
   }

   /**
    * Update all the children fields of the specified content item.
    *
    * @param request the request to work on, parameters already set to contentid
    * and revision when appropriate, assumed not <code>null</code>
    *
    * @param path the path to the content editor to access for the update
    * request, assumed not <code>null</code>
    *
    * @throws PSCmsException if it cannot get the value from the child field
    */
   private void saveAllChilds(PSRequest request, String path)
      throws PSCmsException
   {
      // update the children
      Iterator childIter = getAllChildren();
      while (childIter.hasNext())
      {
         Map<String, String> baseParams = new HashMap<String, String>();

         PSItemChild child = (PSItemChild) childIter.next();
         baseParams.put(
            PSContentEditorHandler.CHILD_ID_PARAM_NAME,
            String.valueOf(child.getChildId()));

         Iterator childEntryIter = child.getAllEntries();
         while (childEntryIter.hasNext())
         {
            Map<String, String> childParams = 
               new HashMap<String, String>(baseParams);
            PSItemChildEntry childEntry =
               (PSItemChildEntry) childEntryIter.next();

            String action = childEntry.getAction();
            if (action != null
               && !action.equals("")
               && !action.equalsIgnoreCase("ignore"))
            {
               childParams.put("DBActionType", action.toUpperCase());
               int childRowId = childEntry.getChildRowId();
               if (childRowId != -1)
               {
                  childParams.put(
                     PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
                     Integer.toString(childRowId));
               }

               Iterator childFieldIter = childEntry.getAllFields();
               populateFieldParams(childParams, childFieldIter);
               processUpdateAction(path, request, childParams);

               // if inserting a new child item and the current entry has
               // not been set yet (row id == -1), update the child row id
               if (action.equalsIgnoreCase("insert")
                  && childEntry.getChildRowId() == -1)
               {
                  try
                  {
                     childRowId =
                        Integer.parseInt(
                           request.getParameter(
                              PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME));
                     request.removeParameter(
                        PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
                     childEntry.setChildRowId(childRowId);
                  }
                  catch (Exception e)
                  {
                     // ignore
                  }
               }
               //Do not leave the old param
               request.removeParameter(
                PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
            }
         }

         // now that they have been updated see if they need to be resequenced
         // if so then collect the rowIds and sortRanks and then update
         if (child.isSequenced())
         {
            String childIdRows = "";
            Iterator seqIter = child.getAllEntries();
            while (seqIter.hasNext())
            {
               PSItemChildEntry childEntry = (PSItemChildEntry) seqIter.next();

               if (!childEntry
                  .getAction()
                  .equalsIgnoreCase(PSItemChildEntry.CHILD_ACTION_DELETE))
               {
                  childIdRows += String.valueOf(childEntry.getChildRowId());

                  if (seqIter.hasNext())
                     childIdRows += ",";
               }
            }
            if (childIdRows.length() > 0)
            {
               Map<String, String> seqParams = new HashMap<String, String>(baseParams);
               seqParams.put(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME,
                  childIdRows);
               seqParams.put(
                  "DBActionType",
                  PSContentEditorHandler.DB_ACTION_RESEQUENCE);

               processUpdateAction(path, request, seqParams);

               // after processing the sequence be sure to remove the child
               // id parameter in case there are more child tables to process
               request.removeParameter(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);
            }
         }
      }
   }

   /**
    * Updates all the related items for this save request. First inserts any
    * new content into the system and next applies any actions on the related
    * items within this content.
    *
    * @param request the request to work on, parameters already set to contentid
    * and revision when appropriate, assumed not <code>null</code>
    *
    * @param path the path to the content editor to access for the update
    * request, assumed not <code>null</code>
    *
    * @throws PSCmsException if it cannot create the server item from the def
    */
   private void saveAllRelated(PSRequest request, String path)
      throws PSCmsException
   {
      // update the related items
      Iterator relIter = getAllRelatedItems();

      // insert any new content first
      while (relIter.hasNext())
      {
         PSItemRelatedItem related = (PSItemRelatedItem) relIter.next();
         if (related.getAction().equalsIgnoreCase(
            PSItemRelatedItem.PSRelatedItemAction.INSERT.toString()))
         {
            Element itemData = related.getRelatedItemData();
            if (itemData == null)
               continue;

            try
            {
               // get the content id, if it exists, we do not need to
               // insert this piece of content, it must already exist
               Element key = PSXMLDomUtil.getFirstElementChild(itemData);
               if (key == null)
                  continue;

               int contentId =
                  PSXMLDomUtil.checkAttributeInt(
                     itemData,
                     ATTR_CONTENT_ID,
                     false);

               if (contentId > 0)
               {
                  // get the content type from the item data
                  int contentTypeId =
                     PSXMLDomUtil.checkAttributeInt(
                        itemData,
                        ATTR_CONTENTTYPE,
                        false);

                  if (contentTypeId == -1)
                     continue;

                  // create def from the content type
                  PSItemDefinition itemDef =
                     getItemDefinition(request, "" + contentTypeId);

                  // get the original object
                  PSServerItem newItem = new PSServerItem(itemDef);

                  // now overlay the item data into the original item
                  newItem.loadXmlData(itemData);

                  // save the completed item
                  newItem.save(request);

                  // clear related item data
                  related.setRelatedItemData(null);

                  // set the contentId to be added
                  related.setDependentId(newItem.getKey().getId());
               }
            }
            catch (PSUnknownNodeTypeException ex)
            {
               /**
               * @todo is this right? do we really just want to ignore errors
               * and continue on? this also catches the error when we are doing
               * an insert and already have a content id
               */
               continue;
            }
         }
      }

      // now do the proper relationship actions (insert, update, delete)
      relIter = getAllRelatedItems();
      while (relIter.hasNext())
      {
         Map<String, String> relatedParams = new HashMap<String, String>();

         PSItemRelatedItem related = (PSItemRelatedItem) relIter.next();
         String action = related.getAction();
         if (action.equalsIgnoreCase(
            PSItemRelatedItem.PSRelatedItemAction.INSERT.toString()))
         {
            relatedParams.put(
               IPSHtmlParameters.SYS_COMMAND,
               PSRelationshipCommandHandler.COMMAND_NAME
                  + "/"
                  + PSRelationshipCommandHandler.COMMAND_INSERT);
         }
         else if (action.equalsIgnoreCase(
            PSItemRelatedItem.PSRelatedItemAction.UPDATE.toString()))
         {
            relatedParams.put(
               IPSHtmlParameters.SYS_COMMAND,
               PSRelationshipCommandHandler.COMMAND_NAME);
         }
         else if (action.equalsIgnoreCase(
            PSItemRelatedItem.PSRelatedItemAction.DELETE.toString()))
         {
            relatedParams.put(
               IPSHtmlParameters.SYS_COMMAND,
               PSRelationshipCommandHandler.COMMAND_NAME
                  + "/"
                  + PSRelationshipCommandHandler.COMMAND_REMOVE);
         }
         else
         {
            continue; // no action specified, skip this one.
         }

         int rId = related.getRelationshipId();
         if (rId != -1)
         {
            relatedParams.put(IPSHtmlParameters.SYS_RELATIONSHIPID, "" + rId);
         }
         relatedParams.put(
            IPSHtmlParameters.SYS_DEPENDENTID,
            "" + related.getDependentId());
         relatedParams.put(
            IPSHtmlParameters.SYS_RELATIONSHIPTYPE,
            related.getRelatedType());

         // put all the properties into the param map
         Iterator propIter = related.getAllProperties();
         while (propIter.hasNext())
         {
            String key = (String) propIter.next();
            String val = related.getProperty(key);

            relatedParams.put(key, val);
         }
         processUpdateAction(path, request, relatedParams);
      }
   }

   /**
    * private convenience helper to return the item def given a contentTypeId
    *
    * @param request the original request
    * @param contentTypeId a content type id as either a numeral or a
    * string name, assumed not <code>null</code>
    * @return an item definition based on the content type, may be 
    * <code>null</code> if a valid type id is not specified.
    *
    * @todo move to common location, used by webservices as well
    */
   private PSItemDefinition getItemDefinition(
      PSRequest request,
      String contentTypeId)
   {
      PSSecurityToken tok = request.getSecurityToken();

      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = null;

      try
      {
         int typeId = Integer.parseInt(contentTypeId);
         try
         {
            itemDef = mgr.getItemDef(typeId, tok);
         }
         catch (PSInvalidContentTypeException ex)
         {
            // ignore, we catch below
         }
      }
      catch (NumberFormatException nfe)
      {
         try
         {
            itemDef = mgr.getItemDef(contentTypeId, tok);
         }
         catch (PSInvalidContentTypeException ex)
         {
            // ignore, we catch below
         }
      }

      if (itemDef == null)
      {
         /** @todo use PSInvalidContentTypeException here */
         // content type does not exist
         //         throw new PSException(IPSWebServicesErrors.WEB_SERVICE_CONTENT_TYPE_NOT_FOUND,
         //            contentTypeId);
      }
      return itemDef;
   }

   /**
    * Returns the PSLocator object of this item.  This is useful for new items
    * being persisted.  This will then have the PSLocator object with the
    * new content id and revision.
    *
    * @return a locator of the current content item
    */
   public PSLocator getKey()
   {
      return new PSLocator("" + getContentId(), "" + getRevision());
   }

   /**
    * Convienence method for performing internal updates. Has the side effect
    * of updating the content id and revision on this server item.
    *
    * @param path the location of the app/resource to execute, assumed not
    * <code>null</code>
    *
    * @param request the original request being worked on, assumed not
    * <code>null</code>
    *
    * @param params a set of extra parameters to pass with the request, may be
    * <code>null</code>
    */
   private void processUpdateAction(String path, PSRequest request, Map params)
      throws PSCmsException
   {
      try
      {
         PSInternalRequest iReq = PSServer.getInternalRequest(path, request,
               params, true);
         PSRequest updatedRequest = iReq.getRequest();

         // reset validation error object
         updatedRequest.setParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR,
               null);
         updatedRequest.setParameter(IPSHtmlParameters.SYS_CE_CACHED_PAGEURL,
               null);

         iReq.performUpdate();

         // check if there is an validation error
         String validateError = updatedRequest
               .getParameter(IPSHtmlParameters.SYS_VALIDATION_ERROR);
         if (validateError != null && validateError.trim().length() > 0)
         {
            PSConsole.printMsg(getClass().getName(),
                  IPSCmsErrors.VALIDATION_ERROR, new Object[]
                  {path, validateError});
            try
            {
               Document errorDoc = PSXmlDocumentBuilder.createXmlDocument(
                     new ByteArrayInputStream(validateError.getBytes()), false);
               PSDisplayError error = new PSDisplayError(errorDoc
                     .getDocumentElement());
               throw new PSFieldValidationException(
                     IPSCmsErrors.VALIDATION_ERROR,
                     new Object[]
                     {path, validateError},
                     error,
                     updatedRequest
                           .getParameter(IPSHtmlParameters.SYS_CE_CACHED_PAGEURL));
            }
            catch (PSInvalidXmlException e)
            {
               throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR, e
                     .getErrorArguments());
            }
            catch (IOException e)
            {
               throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR, e
                     .getLocalizedMessage());
            }
            catch (SAXException e)
            {
               throw new PSCmsException(IPSCmsErrors.VALIDATION_ERROR, e
                     .getLocalizedMessage());
            }
         }

         // update the current contentId and revision
         String contentId = updatedRequest
               .getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String revision = updatedRequest
               .getParameter(IPSHtmlParameters.SYS_REVISION);

         if (contentId != null && contentId.trim().length() > 0)
         {
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID, contentId);
            setContentId(Integer.parseInt(contentId));
         }
         if (revision != null && revision.trim().length() > 0)
         {
            request.setParameter(IPSHtmlParameters.SYS_REVISION, revision);
            setRevision(Integer.parseInt(revision));
         }

         // if we have a child row id, update the request with that value
         String childRowId = updatedRequest
               .getParameter(PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME);

         if (childRowId != null && childRowId.trim().length() > 0)
            request.setParameter(
                  PSContentEditorHandler.CHILD_ROW_ID_PARAM_NAME, childRowId);
      }
      catch (PSException ex)
      {
         if(ex instanceof PSCmsException)
            throw (PSCmsException)ex;
         throw new PSCmsException(ex);
      }
   }
   
   /**
    * Flag to indicate binary fields should be loaded.  Binary field values
    * will only be loaded if this flag is included.  If not loaded, item may
    * be saved without affecting these field values.  Also, if not loaded, new 
    * binary field values may be set, but existing values cannot be retrieved
    * from the server.  If loaded, data is not actually retrieved from the
    * server until the value is requested from the {@link PSBinaryValue} object.
    */
   public final static int TYPE_BINARY = 0x1;
   
   /**
    * Flag to indicate fields should be loaded.  Absence of this flag means that
    * no field values will be loaded, even if {@link #TYPE_BINARY} is also 
    * specified.  If not loaded, item may still be saved without affecting
    * any field values.  TODO: This flag is not yet supported
    */
   public final static int TYPE_FIELDS = 0x2;
   
   /**
    * Flag to indicate if child items should be loaded.  If not loaded, new 
    * child items may still be added and saved without affecting any existing 
    * child items. 
    */
   public final static int TYPE_CHILD = 0x4;
   
   /**
    * Flag to indicate if the relationships of the related items should be 
    * loaded. If not loaded, new item relationships may still be added and 
    * saved without affecting any existing item relationships.
    */
   public final static int TYPE_RELATED = 0x08;

   /**
    * Flag to indicate if the related (or dependent) items of the relationships
    * should be loaded. This has to be used in conjunction with 
    * {@LInk #TYPE_RELATED} is on. If both {@link #TYPE_RELATED} and this flag
    * are on, then load the relationships as well as the related items of the
    * relationships. 
    * <p>
    * Loading related items is an expensive operation and should be avoid if is
    * not needed. The loaded items contain non-binary fields and child entries, 
    * but do not contain binary fields and the AA relationships with their
    * dependent items. In other words, the related items are load with
    * {@link #TYPE_FIELDS} and {@link #TYPE_CHILD} flags on, but the other
    * TYPE_XXX flags are off. 
    */
   public final static int TYPE_RELATED_ITEM = 0x10;

   /**
    * Flag to indicate what types of data to load.  Any of the TYPE_XXX flags 
    * Or'd together, except {@link #TYPE_RELATED_ITEM}.  Initial value is all 
    * such flags Or'd together, modified by 
    * {@link #load(PSLocator, PSRequest, int)}.
    */   
   private int m_loadFlags = TYPE_BINARY | TYPE_FIELDS | TYPE_CHILD | 
      TYPE_RELATED;

   /**
    * Lookup app resource for content status information.
    */
   private static String CONTENT_STATUS_APP_URL = "sys_psxCms/contentStatus";
}
