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

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.objectstore.IPSComponentProcessor;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSFolderProcessor;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.IPSVisitor;
import com.percussion.cms.objectstore.PSCloningOptions;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.cms.objectstore.PSFolderProcessorProxy;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSProcessingStatistics;
import com.percussion.cms.objectstore.PSProcessorCommon;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.cms.objectstore.server.PSFolderSecurityManager;
import com.percussion.cms.objectstore.server.PSInlineLinkProcessor;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSRelationshipDbProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.cms.objectstore.ws.PSLocatorWithName;
import com.percussion.cms.objectstore.ws.PSRemoteFolderAgent;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSRelationshipTracker;
import com.percussion.error.PSException;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.fastforward.managednav.PSNavFolderUtils;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.relationship.PSAttemptResult;
import com.percussion.relationship.PSEffectResult;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSFolderEntry;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.server.cache.PSItemSummaryCache;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderActionProcessor;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderMoveActionProcessor;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderRemoveActionProcessor;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.notification.PSMessageQueueServiceLocator;
import com.percussion.services.purge.IPSSqlPurgeHelper;
import com.percussion.services.purge.PSSqlPurgeHelperLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.util.PSHttpConnection;
import com.percussion.util.PSPathUtil;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSFolderStringUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jcr.query.InvalidQueryException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * This class is designed to be used locally on the server for folder related
 * operations. It uses <code>PSServerItem</code> to save, update and load the
 * folder component. It uses relationship processor to handle all relationship
 * related operations.
 * <p>
 * Note: It uses the folder entry in the PSX_OBJECTS table to determine whether
 * the revision is ignored for folders. If the revision is ignored, then the
 * revision of the locator may be set to <code>1</code> for folder specific
 * operations such as <code>load</code>, <code>delete</code>. However, the
 * revision of the returned locator will be <code>-1</code> for relationship
 * operations, such as <code>move</code>, <code>add</code>,
 * <code>getChildren</code>, <code>getParent</code>, ...etc.
 */
public class PSServerFolderProcessor extends PSProcessorCommon implements
   IPSRelationshipProcessor, IPSFolderProcessor
{

   public PSServerFolderProcessor(PSRemoteFolderAgent ctx, Map procConfig)
   {
      this();
      log.debug("Proxy constructor called");
   }
   
   /**
    * Creates an instance with a request and config properties. This is expected
    * to be called by the Proxy class.
    *
    */
   public PSServerFolderProcessor()
   {
      m_folderObject = PSServer.getCmsObjectRequired(PSCmsObject.TYPE_FOLDER);
   }

   /**
    * Returns the item cache object.
    *
    * @return the item cache object, may be <code>null</code> if the cache is
    * not initialized or instantiated.
    */
   private PSItemSummaryCache getItemCache()
   {
      return PSItemSummaryCache.getInstance();
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, List, PSLocator)
    * interface}
    */
   public void add(String componentType, @SuppressWarnings("unused")
   String relationshipType, List children, PSKey targetParent)
      throws PSCmsException
   {
      add(componentType, children, targetParent);
   }

   /**
    * See {@link IPSComponentProcessor#save(IPSDbComponent[]) interface} for
    * general description.
    */
   @Override
   public PSSaveResults save(IPSDbComponent[] components) throws PSCmsException
   {
      if (null == components)
         throw new IllegalArgumentException("Null array not allowed.");

      // check if the save operation will succeed
      checkHasSavePermission(components, true);

      // disable any more permission checking
      PSFolderSecurityManager.setCheckFolderPermissions(false);

      IPSDbComponent[] comps = new IPSDbComponent[components.length];
      PSProcessingStatistics statistics = null;
      try
      {
         int inserts = 0;
         int updates = 0;

         for (int i = 0; i < comps.length; i++)
         {
            if (components[i].getLocator().isPersisted())
               updates++;
            else
               inserts++;

            comps[i] = save((PSFolder) components[i]);
         }
         statistics = new PSProcessingStatistics(inserts, updates, 0, 0, 0);
      }
      finally
      {
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
      return new PSSaveResults(comps, statistics);
   }

   /**
    * Save a specified folder object to the database. Inserts the folder if the
    * its key is not persisted, otherwise, do update for the folder.
    *
    * @param folder The to be saved folder object, not <code>null</code>.
    *
    * @return The saved folder, which contains all persisted keys as well as the
    * data.
    *
    * @throws PSCmsException if an error occurs.
    */
   public PSFolder save(PSFolder folder) throws PSCmsException
   {
      if (folder == null)
         throw new IllegalArgumentException("folder cannot be null");

      PSFolder newFolder = null;
      try
      {
         PSLocator locator = folder.getLocator();

         validateFolderValues(folder);

         if (locator.isPersisted())
         {
            triggerFolderEffectIfNeeded(folder);
            updateFolder(folder);
         }
         else
         {
            locator = insertFolder(folder);
         }

         // load the inserted or updated folder to get valid key(s) or id(s)
         // along with its data
         newFolder = loadFolder(locator);
         if (getItemCache() != null)
            getItemCache().updateFolder(newFolder); // update folder cache
      }
      catch (PSException e)
      {
         log.error("Failed to save folder id=" + folder.getLocator(), e);
         throw new PSCmsException(e);
      }

      return newFolder;
   }

   /**
    * Determines if the folder needs to be saved and if the folder 
    * effects should be called.
    * <p>
    * If the target folder has not changed in name or properties 
    * then nothing will happen.
    *  
    * @param target never null.
    * @throws PSCmsException thrown if there is an repository error.
    */
   private void triggerFolderEffectIfNeeded(PSFolder target)
         throws PSCmsException
   {
      PSFolder existingFolder = openFolder(target.getLocator());
      if (existingFolder == target)
         throw new IllegalArgumentException(
               "Cannot modify the same folder object when updating it.");

      if (isSameFolderName(existingFolder, target))
         return;

      PSRelationshipSet relSet = getRelationships(
            FOLDER_RELATE_TYPE, target.getLocator(),
            false);
      if (relSet.size() < 1)
         return;

      PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();

      // trigger folder effect
      proc.save(relSet);
   }

   /**
    * Determines if the folder names are the same for the publish name
    * and system name.
    * @param origFolder not null.
    * @param target not null.
    * @return <code>true</code> if same.
    */
   private boolean isSameFolderName(PSFolder origFolder, PSFolder target)
   {
      boolean b1 = StringUtils.equals(origFolder.getName(), target.getName());
      boolean b2 = StringUtils.equals(origFolder.getPubFileName(), target
            .getPubFileName());

      return b1 && b2;
   }
   
   /**
    * Validates all the values in a given folder object.
    *
    * @param folder The to be validated folder object, assume not
    * <code>null</code>.
    *
    * @throws PSCmsException if one of the value is invalid
    */
   private void validateFolderValues(PSFolder folder) throws PSCmsException
   {
      if (folder.getName().length() > NAME_MAX)
         throwInvalidException("folder name", folder.getName().length(),
            NAME_MAX);

      if (folder.getDescription().length() > DESCRIPTION_MAX)
         throwInvalidException("folder description", folder.getDescription()
            .length(), DESCRIPTION_MAX);

      if (folder.getName().indexOf('/') > -1)
      {
         String args[] = new String[]
         {
            folder.getName()
         };
         throw new PSCmsException(IPSCmsErrors.INVALID_FOLDER_NAME, args);
      }

      Iterator props = folder.getProperties();
      while (props.hasNext())
      {
         PSFolderProperty prop = (PSFolderProperty) props.next();

         if (prop.getName().length() > PROP_NAME_MAX)
            throwInvalidException("property name", prop.getName().length(),
               PROP_NAME_MAX);

         if (prop.getValue().length() > PROP_VALUE_MAX)
            throwInvalidException("property value", prop.getValue().length(),
               PROP_VALUE_MAX);

         if (prop.getDescription().length() > PROP_DESC_MAX)
            throwInvalidException("property description", prop.getDescription()
               .length(), PROP_DESC_MAX);
      }
   }

   /**
    * Throws an exception from the given parameters.
    *
    * @param valueName The name of the invalid value. Assume not
    * <code>null</code> or empty.
    *
    * @param currLength The current length of the invalid value.
    *
    * @param max The maximum length for the given string value.
    *
    * @throws PSCmsException always throw this exception.
    */
   private void throwInvalidException(String valueName, int currLength, int max)
      throws PSCmsException
   {
      String[] args =
      {
         valueName, "" + currLength, "" + max
      };
      throw new PSCmsException(IPSCmsErrors.INVALID_FOLDER_VALUE, args);
   }

   /**
    * Updates a given folder to the database through the
    * <code>PSServerItem</code> object.
    *
    * @param folder The to be updated folder object, assume not
    * <code>null</code>.
    *
    * @throws PSException if an error occurs.
    */
   private void updateFolder(PSFolder folder) throws PSException
   {
      PSServerItem folderItem;

      folderItem = getServerItemFromFolder(folder);
      
      PSRequest request = PSThreadRequestUtils.getPSRequest();
      // prepare to do the update
      PSLocator locator = folder.getLocator();
      request = PSThreadRequestUtils.getPSRequest();
      request.setParameter(IPSHtmlParameters.SYS_CONTENTID, Integer
         .toString(locator.getId()));
      request.setParameter(IPSHtmlParameters.SYS_REVISION, Integer
         .toString(locator.getRevision()));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element folderEl = folderItem.toXml(doc);
      if (log.isDebugEnabled())
      {
         String folderString = PSXmlDocumentBuilder.toString(folderEl);
         log.info("FOLDER: " + folderString);
      }
      
      PSContentDataHandler dataHandler = new PSContentDataHandler();
      dataHandler.updateItem(request, folderEl, locator,
         getFolderTypeId());
   }

   /**
    * Insert a folder object to the database.
    *
    * @param folder The to be inserted folder object, assume not
    * <code>null</code>.
    *
    * @param folder The folder assume not <code>null</code>.
    *
    * @return The locator of the inserted folder, never <code>null</code>.
    *
    * @throws PSException if an error occurs during the inserting operation.
    */
   private PSLocator insertFolder(PSFolder folder)
      throws PSException
   {
      // Document doc = PSXmlDocumentBuilder.createXmlDocument();

      PSServerItem fItem;
      fItem = getServerItemFromFolder(folder);

      PSRequest request = PSThreadRequestUtils.getPSRequest();
      
      // do the actual insertion. make sure there is parameters set for no
      // contentId and revisionId before perform the insert item operation
      request.removeParameter(IPSHtmlParameters.SYS_CONTENTID);
      request.removeParameter(IPSHtmlParameters.SYS_REVISION);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      PSContentDataHandler dataHandler = new PSContentDataHandler();
      dataHandler.processInsertItem(request, FOLDER_CONTENTTYPE, fItem
         .toXml(doc));

      // get the id that just created from the above insert
      int id = Integer.parseInt(request
         .getParameter(IPSHtmlParameters.SYS_CONTENTID));

      return new PSLocator(id, 1);
   }

   /**
    * Load a folder that is specified by a locator. This is done by loading the
    * item of the folder, then creates a folder from the loaded item.
    *
    * @param locator The locator of the specified folder, assumed not
    * <code>null</code>.
    *
    * @param locator The locator object, assumed not <code>null</code>.
    *
    * @return The loaded folder object. Never <code>null</code>.
    *
    * @throws PSCmsException if any error occurs.
    */
   private PSFolder loadFolder(PSLocator locator)
      throws PSCmsException
   {
      try
      {
         // Make sure the folder revision is always 1;
         // otherwise, revision=-1 will be rejected by PSServer API
         locator.setRevision(1);
         PSRequest request = PSThreadRequestUtils.getPSRequest();
         // get the content item for the folder
         PSServerItem folderItem = newServerItem();
         folderItem.load(locator, request);

         // retrieve the folder data from the item
         PSItemField field = folderItem.getFieldByName(NAME);
         IPSFieldValue value = field.getValue();
         String name = (value == null) ? "" : value.getValueAsString();

         field = folderItem.getFieldByName(COMMUNITYID);
         value = field.getValue();
         // there should always be a community ID if the object was successfully
         // loaded. set to -1 if there is no community ID, then it is accessible
         // by all communities.
         int communityId = value == null ? -1 : Integer.parseInt(value
               .getValueAsString());

         field = folderItem.getFieldByName(DESCRIPTION);
         value = field.getValue();
         String description = (value == null) ? "" : value.getValueAsString();

         field = folderItem.getFieldByName(LOCALE);
         value = field.getValue();
         String locale = (value == null) ? PSI18nUtils.DEFAULT_LANG : value
            .getValueAsString();

         // load the folder ACL from the complex child data if exists
         PSFolderAcl folderAcl = new PSFolderAcl(locator.getId(), communityId);
         PSItemChild child = folderItem.getChildByName(CHILD_NAME_ACL);
         Iterator childs = child.getAllEntries();
         while (childs.hasNext())
         {
            PSItemChildEntry childEntry = (PSItemChildEntry) childs.next();

            // get the ACL type
            field = childEntry.getFieldByName(ACL_TYPE);
            value = field.getValue();
            // this should never be null
            String strAclTye = value.getValueAsString();
            int aclType = -1;
            try
            {
               aclType = Integer.parseInt(strAclTye);
            }
            catch (NumberFormatException nfe)
            {
               throw new PSException(nfe.getLocalizedMessage());
            }

            // get the user, role, virtual entry name
            field = childEntry.getFieldByName(ACL_NAME);
            value = field.getValue();

            // this should never be null
            String aclName = value.getValueAsString();

            // get the permissions for this ACL entry
            field = childEntry.getFieldByName(ACL_PERMISSIONS);
            value = field.getValue();
            // this should never be null
            int aclPerm = Integer.parseInt(value.getValueAsString());

            int id = childEntry.getChildRowId();
            PSObjectAclEntry aclEntry = new PSObjectAclEntry(id, aclType,
               aclName, aclPerm);
            aclEntry.setState(IPSDbComponent.DBSTATE_UNMODIFIED);
            folderAcl.add(aclEntry);
         }

         PSFolderPermissions folderPerms = new PSFolderPermissions(folderAcl);
         int permissions = folderPerms.getPermissions();

         PSFolder folder = new PSFolder(name, locator.getId(), communityId,
            permissions, description);
         folder.setAcl(folderAcl);
         folder.setLocale(locale);

         // get the folder properties from the complex child data if exists
         // should be there unless someone mucked w/ system app
         child = folderItem.getChildByName(CHILD_NAME_PROPERTIES);
         childs = child.getAllEntries();
         while (childs.hasNext())
         {
            PSItemChildEntry childEntry = (PSItemChildEntry) childs.next();

            // get the property name
            field = childEntry.getFieldByName(PROP_NAME);
            value = field.getValue();
            // this is guaranteed not-null by db because it is part of Primary
            // key
            String propName = value.getValueAsString();

            // get the property value
            field = childEntry.getFieldByName(PROP_VALUE);
            value = field.getValue();
            String propValue = (value == null) ? "" : value.getValueAsString();

            // get the property description
            field = childEntry.getFieldByName(PROP_DESC);
            value = field.getValue();
            String propDesc = (value == null) ? "" : value.getValueAsString();

            int id = childEntry.getChildRowId();

            PSFolderProperty prop;
            prop = new PSFolderProperty(id, propName, propValue, propDesc);
            prop.setState(IPSDbComponent.DBSTATE_UNMODIFIED);

            folder.addProperty(prop);
         }

         folder.setState(IPSDbComponent.DBSTATE_UNMODIFIED);

         return folder;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Get the content type id for the folder.
    *
    * @return the content type id with the name as
    * <code>FOLDER_CONTENTTYPE</code>
    *
    * @throws PSInvalidContentTypeException If the
    * <code>FOLDER_CONTENTTYPE</code> does not match any running content type.
    */
   private static long getFolderTypeId() throws PSInvalidContentTypeException
   {
      if (m_folderContentTypeId == -1)
      {
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         m_folderContentTypeId = mgr.contentTypeNameToId(FOLDER_CONTENTTYPE);
      }
      return m_folderContentTypeId;
   }

   /**
    * This class allows us to set members in <code>PSChildEntry</code> through
    * a visitor that sets the rowid.
    *
    * FIX ME this code is copied from PSServerItem.java, this needs to be
    * re-worked so that we don't duplicate the code in the future.
    */
   private class PSChildEntrySetter implements IPSVisitor
   {
      /**
       * Create instance.
       *
       * @param theRowId - the int to use as id.
       */
      PSChildEntrySetter(int theRowId)
      {
         rowId = theRowId;
      }

      /**
       * Called by the visitor. Return rowid.
       *
       * @return the row id, never <code>null</code>.
       */
      public Object getObject()
      {
         return rowId;
      }

      /**
       * Holds rowid.
       */
      int rowId;
   }

   /**
    * Creates an empty <code>PSServerItem</code> for the folder content type.
    * Assume the folder content type never change since it is used by the
    * system, should not be modified by end user.
    *
    *
    * @return The empty server item object, never <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   private PSServerItem newServerItem() throws PSCmsException
   {
      if (m_folderItemDef == null)
      {
         PSItemDefManager mgr = PSItemDefManager.getInstance();

         try
         {
            
            m_folderItemDef = mgr.getItemDef(getFolderTypeId(), PSThreadRequestUtils.getPSRequest()
               .getSecurityToken());
         }
         catch (PSException e)
         {
            throw new PSCmsException(e);
         }
      }

      return new PSServerItem(m_folderItemDef);
   }

   /**
    * Get a <code>PSServerItem</code> object from a folder object.
    *
    *
    * @param folder The folder object, assume not <code>null</code>.
    *
    * @return The <code>PSServerItem</code> object, never <code>null</code>.
    *
    * @throws PSException if any other error occurs.
    */
   private PSServerItem getServerItemFromFolder(
      PSFolder folder) throws PSException
   {
      PSServerItem fItem = newServerItem();

      // set folder name
      PSItemField field = fItem.getFieldByName(NAME);
      PSTextValue textVal = new PSTextValue(folder.getName());
      field.addValue(textVal);

      // set community id
      field = fItem.getFieldByName(COMMUNITYID);
      textVal = new PSTextValue(Integer.toString(folder.getCommunityId()));
      field.addValue(textVal);

      // set folder description
      field = fItem.getFieldByName(DESCRIPTION);
      textVal = new PSTextValue(folder.getDescription());
      field.addValue(textVal);

      // set folder locale
      field = fItem.getFieldByName(LOCALE);
      textVal = new PSTextValue(folder.getLocale());
      field.addValue(textVal);

      // set workflowid
      field = fItem.getFieldByName(IPSHtmlParameters.SYS_WORKFLOWID);
      field.addValue(new PSTextValue(Integer.toString(-1)));

      // set the properties / the child table
      PSItemChild child = fItem.getChildByName(CHILD_NAME_PROPERTIES);

      // set the inserted and modified properties
      Iterator childs = folder.getProperties();
      while (childs.hasNext())
      {
         PSFolderProperty prop = (PSFolderProperty) childs.next();

         if (prop.getState() == IPSDbComponent.DBSTATE_UNMODIFIED)
            continue;

         PSItemChildEntry childEntry = child.createChildEntry();

         if (prop.getState() == IPSDbComponent.DBSTATE_NEW)
         {
            childEntry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
         }
         else
         {
            childEntry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
            PSChildEntrySetter rowId = new PSChildEntrySetter(prop.getId());
            childEntry.accept(rowId);
         }

         field = childEntry.getFieldByName(PROP_NAME);
         textVal = new PSTextValue(prop.getName());
         field.addValue(textVal);

         field = childEntry.getFieldByName(PROP_VALUE);
         textVal = new PSTextValue(prop.getValue());
         field.addValue(textVal);

         field = childEntry.getFieldByName(PROP_DESC);
         textVal = new PSTextValue(prop.getDescription());
         field.addValue(textVal);

         child.addEntry(childEntry);
      }

      // take care the deleted properties
      childs = folder.getDeletedProperties();
      while (childs.hasNext())
      {
         PSFolderProperty prop = (PSFolderProperty) childs.next();

         PSItemChildEntry childEntry = child.createChildEntry();
         childEntry.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);
         PSChildEntrySetter rowId = new PSChildEntrySetter(prop.getId());
         childEntry.accept(rowId);

         child.addEntry(childEntry);
      }

      // Add the Folder ACL
      child = fItem.getChildByName(CHILD_NAME_ACL);

      // set the inserted and modified properties
      Iterator it = folder.getAcl().iterator();
      while (it.hasNext())
      {
         PSObjectAclEntry aclEntry = (PSObjectAclEntry) it.next();
         if (aclEntry.getState() == IPSDbComponent.DBSTATE_UNMODIFIED)
            continue;

         PSItemChildEntry childEntry = child.createChildEntry();

         if (aclEntry.getState() == IPSDbComponent.DBSTATE_NEW)
         {
            childEntry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
         }
         else
         {
            childEntry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
            PSKey key = aclEntry.getLocator();
            int id = key.getPartAsInt(PSObjectAclEntry.KEY_COL_ID);
            PSChildEntrySetter rowId = new PSChildEntrySetter(id);
            childEntry.accept(rowId);
         }

         field = childEntry.getFieldByName(ACL_TYPE);
         textVal = new PSTextValue("" + aclEntry.getType());
         field.addValue(textVal);

         field = childEntry.getFieldByName(ACL_NAME);
         textVal = new PSTextValue(aclEntry.getName());
         field.addValue(textVal);

         field = childEntry.getFieldByName(ACL_PERMISSIONS);
         textVal = new PSTextValue("" + aclEntry.getPermissions());
         field.addValue(textVal);

         child.addEntry(childEntry);
      }

      // take care the deleted ACL entries
      it = folder.getAcl().getDeletedAclEntries();
      while (it.hasNext())
      {
         PSObjectAclEntry aclEntry = (PSObjectAclEntry) it.next();

         PSItemChildEntry childEntry = child.createChildEntry();
         childEntry.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);

         PSKey key = aclEntry.getLocator();
         int id = key.getPartAsInt(PSObjectAclEntry.KEY_COL_ID);

         PSChildEntrySetter rowId = new PSChildEntrySetter(id);
         childEntry.accept(rowId);
         child.addEntry(childEntry);
      }

      return fItem;
   }

   /**
    * Just like the {@link #validateKeys(Iterator) validateKeys}, except it
    * validates a list of keys.
    */
   private void validateKeys(Iterator locators)
   {
      while (locators.hasNext())
      {
         PSKey key = (PSKey) locators.next();
         validateKey(key);
      }
   }

   /**
    * Just like the {@link #validateKeys(Iterator) validateKeys}, except it
    * also set the revision of the locators to <code>1</code> if the revision
    * is ignored and it is not <code>1</code>, see {@link #isRevisionable()}.
    */
   private void validateAndSetKeys(PSKey[] locators)
   {
      for (int i = 0; i < locators.length; i++)
      {
         validateKey(locators[i]);
         PSLocator locator = (PSLocator) locators[i];
         if ((!isRevisionable()) && (locator.getRevision() != 1))
            locator.setRevision(1);
      }
   }

   /**
    * Validates a single key.
    *
    * @param locator The to be validated key. The key may not be
    * <code>null</code> and it must be an instance of <code>PSLocator</code>
    * type. The values of "id" must be greater than <code>0</code>. The value
    * of "revision" may be ignored if folder revision is ignored, see
    * {@link #isRevisionable()}.
    *
    * @throws IllegalArgumentException if it is invalid.
    */
   private void validateKey(PSKey locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      if (!(locator instanceof PSLocator))
      {
         throw new IllegalArgumentException(
            "locator must be a PSLocator object.");
      }
      if (((PSLocator) locator).getId() < 0)
      {
         throw new IllegalArgumentException(
            "the \"id\" of the \"locator\" must be > 0.");
      }
      if (isRevisionable() && (((PSLocator) locator).getRevision() < 0))
      {
         throw new IllegalArgumentException(
            "the \"revision\" of the \"locator\" must be > 0.");
      }
   }

   /**
    * Validating a given component type.
    *
    * @param componentType The to be validated component type. It may not be
    * <code>null</code> or empty, and it must be <code>PSFolder</code>.
    */
   private void validateComponentType(String componentType)
   {
      if (null == componentType || componentType.trim().length() == 0
         || (!componentType.equalsIgnoreCase("PSFolder")))
         throw new IllegalArgumentException("Invalid component type.");
   }

    /**
     * Validating a given rel type.
     *
     * @param relType The to be validated component type. It may not be
     * <code>null</code> or empty, and it must be <code>PSFolder</code>.
     */
    private void validateRelType(String relType)
    {
        if (null == relType || relType.trim().length() == 0
                || (!relType.equals(FOLDER_RELATE_TYPE) && !relType.equals(RECYCLED_RELATE_TYPE)))
            throw new IllegalArgumentException("Invalid component type.");
    }

   /**
    * See {@link IPSComponentProcessor#load(String,PSKey[]) interface} for
    * general description, except the <code>locators</code> must not be
    * <code>null</code>. See {@link #validateAndSetKeys(PSKey[])} for the
    * requirement of the locators.
    *
    * @throws UnsupportedOperationException if locators is <code>null</code>
    */
   @Override
   public Element[] load(String componentType, PSKey[] locators)
      throws PSCmsException
   {
      validateComponentType(componentType);

      if (locators == null)
         throw new UnsupportedOperationException(
            "locators equals to null is not supported by folder processor.");

      validateAndSetKeys(locators);

      PSFolderSecurityManager.setCheckFolderPermissions(false);
      Element[] resultData = new Element[locators.length];
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      try
      {
         PSFolder[] folders = openFolder(locators);
         for (int i = 0; i < folders.length; i++)
            resultData[i] = folders[i].toXml(doc);
      }
      finally
      {
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
      return resultData;
   }

   /**
    * Loads a list of folder objects from the supplied folder locators. The
    * folder objects may load from item cache if available. The item cache will
    * be updated with the loaded folder objects if the item cache is enabled.
    *
    * @param locators the folder locators, not <code>null</code>.
    * @return array of folder objects, never <code>null</code>. The number of
    *    folder objects matches the number of locators.
    * @throws PSCmsException if an error occurs.
    */
   public PSFolder[] openFolder(PSKey[] locators) throws PSCmsException
   {
      if (locators == null)
         throw new IllegalArgumentException("locators cannot be null");

      PSFolder[] folders = new PSFolder[locators.length];
      for (int i = 0; i < locators.length; i++)
         folders[i] = openFolder(locators[i]);

      return folders;
   }
   
   /**
    * Loads the folder object from the supplied folder locator. The
    * folder object may be load from the item cache if available. The item 
    * cache will be updated with the loaded folder object if the item cache 
    * is enabled.
    *
    * @param locator the folder locator, not <code>null</code>.
    * 
    * @return the loaded folder object, never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public PSFolder openFolder(PSKey locator) throws PSCmsException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");

      PSItemSummaryCache cache = getItemCache();
      PSFolder folder = null;
      if (cache != null)
      {
         folder = getFolderFromCache(cache, ((PSLocator) locator).getId());
      }

      if (folder == null)
      {
         folder = loadFolder((PSLocator) locator);
         if (cache != null)
            cache.updateFolder(folder);
      }

      checkHasFolderPermission(folder, PSObjectPermissions.ACCESS_READ, true);

      return folder;
   }

   /**
    * Gets the specified folder from the item cache and return the cloned
    * object.
    *
    * @param cache the instance of the item cache, assumed not <code>null</code>.
    * @param id the specified folder id.
    *
    * @return the cloned folder object, may be <code>null</code> if the
    * specified folder does not exit.
    *
    * @throws PSCmsException if an error occurs.
    */
   private PSFolder getFolderFromCache(PSItemSummaryCache cache, int id)
      throws PSCmsException
   {
      PSFolder folder = cache.getFolder(id);
      if (folder == null)
         return null;

      // has to create an identical copy from the cached folder since its
      // permissions (transient data) property is based on the current
      // user and the ACL of the folder and needs to be reset
      folder = new PSFolder(folder);

      // reset folder permissions, which is based on the requester (current
      // user) and the ACL of the folder
      PSFolderAcl acl = cache.getFolderAcl(id);
      if (acl == null)
         acl = new PSFolderAcl(id, folder.getCommunityId());
      try
      {
         folder.setPermissions(new PSFolderPermissions(acl));
      }
      catch (PSAuthorizationException e)
      {
         log.error("An autorization exception occurred while setting folder permissions.",e);
         throw new PSCmsException(e);
      }

      return folder;
   }

   /**
    * Recursively delete a specified folder and the relationships between the
    * folder and its children. Its children can be item or folder. The same
    * operation also apply to all its folder child, but not the item child.
    *
    *
    * @param locator The to be deleted folder locator, assume not
    * <code>null</code>
    * @param relationshipTypeName
    *
    * @throws PSCmsException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void deleteFolderRecursive(PSLocator locator, String relationshipTypeName)
      throws PSCmsException
   {
      // delete the relationship between the folder and its children first
      int donotfilterby = PSRelationshipConfig.FILTER_TYPE_COMMUNITY
         | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      PSRelationshipSet childSet = processor.getDependents(relationshipTypeName,
         locator, donotfilterby);
      if (!childSet.isEmpty())
      {
         // do the delete operation from the bottom up

         // process its folder folder children (sub-folders)
         PSRelationship rel;
         Iterator children = childSet.iterator();
         Set<String> contentIdSet = new HashSet<>();
         List<PSLocator> subFolderLocators = new ArrayList<>();
         while (children.hasNext())
         {
            rel = (PSRelationship) children.next();
            PSLocator depLoc = rel.getDependent();
            if (rel.getDependentObjectType() == PSCmsObject.TYPE_FOLDER)
            {
               // delete all descendents of the subfolder (its locator is
               // depLoc)
               deleteFolderRecursive(depLoc, relationshipTypeName);
               subFolderLocators.add(depLoc);
            }
            else
               contentIdSet.add(depLoc.getPart(PSLocator.KEY_ID));
         }
         // delete the parent/child relationships
         processor.delete(childSet);
         // delete sub folder themself
         deleteFolders(subFolderLocators);

         PSNavConfig navConfig = null;
         try
         {
            navConfig = PSNavConfig.getInstance();
         }
         catch (Exception e)
         {
            // This could easily happen if FastForward is not installed
            getLogger().warn(
               "Failed to load Manage Nav Configuration. "
                  + "FastForward might not be installed", e);
         }
         /*
          * If FF is installed and folders children size is non zero, delete
          * Navon items
          */
         if (navConfig != null && !contentIdSet.isEmpty())
         {
            long navonContentTypeId = navConfig.getNavonType().getUUID();
            if (navonContentTypeId > 0)
            {
               List<String> cids = new ArrayList<>(contentIdSet);
               Map<String,Object> params = new HashMap<>();
               params.put(IPSHtmlParameters.SYS_CONTENTTYPEID, ""
                  + navonContentTypeId);
               params.put(IPSHtmlParameters.SYS_CONTENTID, cids);
               List<PSLocator> locators = new ArrayList<>();
               IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
               PSInternalRequest irq = null;
               ResultSet rs = null;
               try
               {
                  irq = PSServer.getInternalRequest(
                     "sys_cxSupport/filterByContentType", PSThreadRequestUtils.getPSRequest(), params,
                     false);
                  if (irq != null)
                  {
                     rs = irq.getResultSet();
                     // Re use the cid list
                     locators.clear();
                     while (rs.next()) {
                           int id = NumberUtils.toInt(rs.getString("CONTENTID"));
                           if (id>0)
                              locators.add(new PSLocator(id));
                     }
                       

                     if (!locators.isEmpty())
                     {
                        // Use server credentials for purging the navons
                        purgeHelper.purgeAll(locators);
                     }
                  }
               }
               catch (PSException | PSValidationException e)
               {
                  getLogger().error(
                     "Failed to delete Navon(s) with contentids: " + cids, e);
               }
               catch (SQLException e)
               {
                  getLogger().error(
                     "Failed to delete Navon(s) with contentids: " + cids, e);
               }
               finally
               {
                  if (irq != null)
                  {
                     irq.cleanUp();
                     irq = null;
                  }

                  if (rs != null)
                  {
                     try
                     {
                        rs.close();
                     }
                     catch (SQLException e)
                     { /* noop */
                     }
                     rs = null;
                  }
               }
            }
         }
      }
   }

   /**
    * Convenience method. Simply call
    * {@link #deleteFolder(PSLocator)}  for each element from the
    * supplied folders
    *
    * @param folders a list of zero or more <code>PSLocator</code> objects,
    * never <code>null</code>, may be empty.
    *
    * @throws PSCmsException if an error occurs.
    */
   private void deleteFolders(List<PSLocator> folders)
      throws PSCmsException
   {
      Iterator<PSLocator> itFolders = folders.iterator();
      PSLocator locator;
      while (itFolders.hasNext())
      {
         locator = itFolders.next();
         deleteFolder(locator);
      }
   }

   /**
    * Deletes one specified folder from the database.
    *
    * @param folderLocator The to be deleted folder locator.
    *
    * @throws PSCmsException if any other error occurs.
    */
   private void deleteFolder(PSLocator folderLocator)
      throws PSCmsException
   {
      IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
     
      try
      {
         purgeHelper.purge(folderLocator);
      }
      catch (PSException | PSValidationException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * For each of folder in a given list of folders, recursively deletes the
    * folder and its relationship with its children.
    *
    * See {@link IPSComponentProcessor#delete(IPSDbComponent[]) interface} for
    * general description.
    */
   @Override
   public int delete(IPSDbComponent[] comps) throws PSCmsException
   {
      PSKey[] locators = new PSKey[comps.length];
      for (int i = 0; i < comps.length; i++)
      {
         locators[i] = comps[i].getLocator();
      }
      return delete(FOLDER_PROXY_TYPE, locators);
   }

   /**
    * Recursively deletes a folder and its relationship with its children.
    *
    * See {@link IPSComponentProcessor#delete(IPSDbComponent) interface} for
    * general description.
    */
   @Override
   public int delete(IPSDbComponent comp) throws PSCmsException
   {
      return delete(FOLDER_PROXY_TYPE, new PSKey[]
      {
         comp.getLocator()
      });
   }

   /**
    * Just like {@link #delete(IPSDbComponent[])}, except the to be deleted
    * folders are specified by its locators.
    * <p>
    * See {@link IPSComponentProcessor#delete(String,PSKey[]) interface} for
    * general description. See {@link #validateAndSetKeys(PSKey[])} for the
    * requirement of the <code>locators</code>.
    *
    * NOTE: This method should be the only method which performs the delete
    * operation on folders. All other <code>delete()</code> methods should
    * delegate to this method. This method ensures that the delete operation
    * will succeed before performing the actual delete. If the delete operation
    * will fail because of folder permissions, then this method throws
    * <code>PSCmsException</code> and the delete operation does not start.
    */
   @Override
   @Deprecated
   public int delete(String componentType, PSKey[] locators)
      throws PSCmsException
   {
      return deleteFolders(locators, true);
   }

   /**
    *
    * @see PSServerFolderProcessor#deleteFolders(PSKey[], boolean, String)
    */
   public int deleteFolders(PSKey[] locators, boolean checkFolderPermission) throws PSCmsException
   {
      return deleteFolders(locators, checkFolderPermission, FOLDER_RELATE_TYPE);
   }
   
   /**
    * Deletes a list of specified folders.
    * <p>
    * See {@link IPSComponentProcessor#delete(String,PSKey[]) interface} for
    * general description. See {@link #validateAndSetKeys(PSKey[])} for the
    * requirement of the <code>locators</code>.
    *
    * NOTE: This method should be the only method which performs the delete
    * operation on folders. All other <code>delete()</code> methods should
    * delegate to this method. This method ensures that the delete operation
    * will succeed before performing the actual delete. If the delete operation
    * will fail because of folder permissions, then this method throws
    * <code>PSCmsException</code> and the delete operation does not start.
    * 
    * @param locators the IDs of the folders, not <code>null</code>.
    * @param checkFolderPermission determines if needs to validate the folder
    * permissions before the delete operations. If it is <code>true</code>,
    * then validates the folder permissions; otherwise ignore the folder
    * permission during the delete operation.
    * @param relationshipTypeName the relationship type name
    * e.g.{@link PSRelationshipConfig#TYPE_FOLDER_CONTENT}
    * 
    * @return the number of deleted folder.
    */
   public int deleteFolders(PSKey[] locators, boolean checkFolderPermission, String relationshipTypeName) throws PSCmsException
   {
      validateAndSetKeys(locators);
      validateFolderType(locators);
      validateRelationshipType(relationshipTypeName);

      // check if the delete operation will succeed
      if (checkFolderPermission)
         checkHasDeletePermission(locators, true);

      // disable any more permission checking
      PSFolderSecurityManager.setCheckFolderPermissions(false);
      int deleted = 0;
      try
      {
         for (int i = 0; i < locators.length; i++)
         {
            deleteFolderRecursive((PSLocator) locators[i], relationshipTypeName);
            deleteFolder((PSLocator) locators[i]);
            deleted++;
         }
      }
      finally
      {
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
      return deleted;
   }

   /**
    * Validating a list of locators, make sure it is a list of folders.
    *
    * @param locators The to be validated locators, assume not <code>null</code>.
    *
    * @throws PSCmsException if one of the locator is not folder.
    */
   @SuppressWarnings("unchecked")
   private void validateFolderType(PSKey[] locators) throws PSCmsException
   {
      PSItemSummaryCache cache = getItemCache();
      if (cache != null) // do validation from cache
      {
         IPSItemEntry item;
         for (int i = 0; i < locators.length; i++)
         {
            int id = locators[i].getPartAsInt(PSLocator.KEY_ID);
            item = cache.getItem(id);
            if (item == null)
            {
               throw new PSCmsException(
                  IPSCmsErrors.FAIL_GET_COMPONENT_SUMMARIES, String.valueOf(id));
            }
            else if (!item.isFolder())
            {
               throw new PSCmsException(IPSCmsErrors.FAIL_DELETE_NON_FOLDER,
                  item.getName());
            }
         }
      }
      else
      // do validation from the repository
      {
         // get the summary info for the to be deleted components
         List locatorList = new ArrayList();
         for (int i = 0; i < locators.length; i++)
            locatorList.add(locators[i]);
         PSComponentSummaries summaries = getComponentSummaries(
            locatorList.iterator(), null, false);

         // validating the type of the component, they must be folders
         List folderList = summaries.getComponentLocators(
            PSComponentSummary.TYPE_FOLDER,
            PSComponentSummary.GET_CURRENT_LOCATOR);

         if (folderList.size() != locators.length)
         {
            // get the 1st non-folder item
            String itemName = "";
            Iterator summaryIt = summaries.getSummaries();
            while (summaryIt.hasNext())
            {
               PSComponentSummary comp = (PSComponentSummary) summaryIt.next();
               if (!comp.isFolder())
               {
                  itemName = comp.getName();
                  break;
               }
            }

            throw new PSCmsException(IPSCmsErrors.FAIL_DELETE_NON_FOLDER,
               itemName);
         }
      }
   }

   /**
    * Not supported.
    */
   @SuppressWarnings("unused")
   public void reorder(int insertAt, List comp) throws PSCmsException
   {
      throw new IllegalStateException("reorder(int, List) is not supported");
   }

   /**
    * See {@link IPSRelationshipProcessor#add(String, List, PSLocator)} for
    * general description. See {@link #validateKey(PSKey)} for the requirement
    * of the locators, <code>children</code> and <code>targetParent</code>.
    */
   @SuppressWarnings("unchecked")
   public void add(String componentType, List children, PSKey targetParent)
      throws PSCmsException
   {
      validateComponentType(componentType);
      validateKeys(children.iterator());
      validateKey(targetParent);
      validateFolderType(new PSKey[] {targetParent});
      
      if (!children.isEmpty())
      {
         PSRelationshipProcessor relation = PSRelationshipProcessor.getInstance();

         PSComponentSummaries childSummaries = getComponentSummaries(
            children.iterator(), null, false);

         validateChildNames(childSummaries, targetParent);

         checkHasCopyPermission( children, targetParent, true);

         // disable any more permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(false);

         // make 2 groups, to be copied list & to be linked list
         ComponentGroup compGroup = new ComponentGroup(childSummaries);

         try
         {
            processFolderChildren(compGroup.copiedList,
               (PSLocator) targetParent, false);

            relation.add(FOLDER_RELATE_TYPE, compGroup.linkedList,
               (PSLocator) targetParent);
         }
         catch (PSException e)
         {
            throw new PSCmsException(e);
         }
         finally
         {
            // enable permission checking
            PSFolderSecurityManager.setCheckFolderPermissions(true);
         }
      }
   }

   /**
    * This class groups a list of summary components into 2 list. A to be copied
    * list and a to be linked list. The to be copied list is a list of folder
    * locators who has a parent. The to be linked list is a list of locators for
    * folder and non-folder items where the folder has no parent. See
    * {@link #copiedList} and {@link #linkedList}
    */
   private class ComponentGroup
   {
      /**
       * Construct a component group from a summaries object.
       *
       * @param summaries The summaries object, assume not <code>null</code>.
       *
       * @throws PSCmsException if an error occurs.
       */
      @SuppressWarnings("unchecked")
      public ComponentGroup(PSComponentSummaries summaries)
         throws PSCmsException
      {
         PSRelationshipProcessor relation = PSRelationshipProcessor.getInstance();

         Iterator componenties = summaries.getSummaries();
         while (componenties.hasNext())
         {
            PSComponentSummary comp = (PSComponentSummary) componenties.next();
            PSLocator locator = comp.getCurrentLocator();

            if (comp.isFolder())
            {
               List parents = relation.getParents(FOLDER_RELATE_TYPE, locator);

               // if has parent already, get the locator from a cloned folder
               if (parents.isEmpty())
                  linkedList.add(locator);
               else
                  copiedList.add(locator);
            }
            else
            {
               linkedList.add(locator);
            }
         }

      }

      /**
       * A list of to be copied folder locators. The list of folders who has a
       * parent already. Since a folder cannot have more than one parent, so
       * this list of folders will be cloned to a new parent. Initialized by
       * constructor, never <code>null</code>, but may be empty.
       */
      private List copiedList = new ArrayList();

      /**
       * A list of folders and non-folder items. Each folder components has no
       * parent. This list of items can be simply linked to a parent folder.
       * Initialized by constructor, never <code>null</code>, but may be
       * empty.
       */
      private List linkedList = new ArrayList();
   }

   /**
    * Validates all supplied children against the provided parent. This method
    * calls {@link #validateChildNames(PSComponentSummaries, PSKey)} See that
    * method for a description of the validation process.
    *
    * @param children a list of <code>PSLocator</code> objects of all children
    * to be validated, assumed not <code>null</code>. All objects that
    * already are linked to the supplied target are removed from this list.
    * @param childSummaries the component summaries of the children. Assume not
    * <code>null</code>.
    * @param target the target key against which to validate the supplied
    * children, assumed not <code>null</code>.
    *
    * @return a collection of <code>PSComponentSummary</code> objects with
    * children that are already linked to the target, never <code>null</code>,
    * may be empty.
    *
    * @throws PSCmsException if the validation fails.
    */
   private PSComponentSummaries validateChildren(List children,
      PSComponentSummaries childSummaries, PSKey target) throws PSCmsException
   {

      PSComponentSummaries existingObjectsSummaries = validateChildNames(
         childSummaries, target);

      // remove the components from the "children", where the components
      // already exist in the "target" folder
      Iterator walker = existingObjectsSummaries.iterator();
      while (walker.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) walker.next();
         for (int i = 0; i < children.size(); i++)
         {
            PSLocator locator = (PSLocator) children.get(i);
            if (locator.getId() == summary.getCurrentLocator().getId())
            {
               children.remove(i);
               break;
            }
         }
      }

      return existingObjectsSummaries;
   }

   /**
    * Method to test if a given (target) parent is a descendent of one of the
    * supplied children. throw exception if the test is successful.
    *
    * @param targetParent the target parent, assume not <code>null</code>.
    * @param folderChildren the folder children list, a list over zero or more
    * <code>PSComponentSummary</code> objects. Assume not <code>null</code>.
    * @param copyItem <code>true</code> if this validation is used for coy
    * item/folder operation; otherwise it is used for move item/folder.
    *
    * @throws PSCmsException if the test described above is successful.
    */
   @SuppressWarnings("unchecked")
   private void validateTargetParentIsNotDescendent(PSLocator targetParent,
      List folderChildren, boolean copyItem) throws PSCmsException
   {
      Iterator childs = folderChildren.iterator();
      PSComponentSummary subFolder;
      while (childs.hasNext())
      {
         subFolder = (PSComponentSummary) childs.next();

         // test if the subFolder is the parent of targetParent
         if (isDescendent(FOLDER_PROXY_TYPE, subFolder.getCurrentLocator(),
            targetParent, FOLDER_RELATE_TYPE))
         {
            // get the name of the targetParent for error message
            List pl = new ArrayList();
            pl.add(targetParent);
            PSComponentSummaries ps = getComponentSummaries( pl
               .iterator(), null, false);
            String targetParentName = "";
            PSComponentSummary[] pArray = ps.toArray();
            if (pArray.length == 1)
               targetParentName = pArray[0].getName();

            // throw the exception
            String[] args =
            {
               subFolder.getName(),
               String.valueOf(subFolder.getContentId()),
               targetParentName,
               String.valueOf(targetParent.getId())
            };

            if (copyItem)
               throw new PSCmsException(
                  IPSCmsErrors.CANNOT_COPY_FOLDER_TO_ITS_DESCENDENT, args);

            throw new PSCmsException(
               IPSCmsErrors.CANNOT_MOVE_FOLDER_TO_ITS_DESCENDENT, args);
         }
      }
   }

   /**
    * Validates all children supplied as component summaries against the
    * provided parent. The only rules checked is are follows: it is an error if
    * the target already contains a component (item or folder) with the same
    * name or if the supplied list of children contains two or more components
    * with the same name.
    * <p>
    * This method throws {@link IllegalArgumentException} if any of the child 
    * has no or empty title.
    * <p>
    * @param children the component summaries of all children to be validated,
    * assumed not <code>null</code>. All objects that already are linked to
    * the supplied target are removed from this collection. Also all components
    * which are duplicates based on their names will be removed.
    * @param target the target key against which to validate the supplied
    * children, assumed not <code>null</code>.
    * @return a collection of <code>PSComponentSummary</code> objects with
    * children that are already linked to the target or were duplicates based on
    * their names, never <code>null</code>, may be empty.
    * @throws PSCmsException if the validation fails.
    */
   @SuppressWarnings("unchecked")
   private PSComponentSummaries validateChildNames(
      PSComponentSummaries children, PSKey target) throws PSCmsException
   {
      PSComponentSummaries existingObjectSummaries = new PSComponentSummaries();

      // get all summaries of the target folder
      int doNotFilterBy = PSRelationshipConfig.FILTER_TYPE_COMMUNITY
         | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
      PSComponentSummaries parentSummaries = getFolderChildren(
         (PSLocator) target, doNotFilterBy, false, FOLDER_RELATE_TYPE);

      /*
       * Test if the target already contains a child with the same name and that
       * the supplied children does not contain duplicate names.
       */
      List sameNameSummaries = new ArrayList();
      List duplicateChildren = new ArrayList();
      Map currentMap = null;
      Map childMap = new HashMap();
      Iterator walker = children.iterator();
      while (walker.hasNext())
      {
         PSComponentSummary child = (PSComponentSummary) walker.next();
         //Can't work with a child with no title
         if (child.getName() == null || child.getName().length() == 0)
         {
            throw new IllegalArgumentException(
               "Item title must not be null or empty");
         }
         if (childMap.get(child.getName().toLowerCase()) == null)
            childMap.put(child.getName().toLowerCase(), child);
         else
            duplicateChildren.add(child);

         PSComponentSummary currentSummary = null;
         if (currentMap == null)
         {
            currentMap = new HashMap();

            Iterator currentWalker = parentSummaries.iterator();
            while (currentWalker.hasNext())
            {
               currentSummary = (PSComponentSummary) currentWalker.next();
               currentMap.put(currentSummary.getName().toLowerCase(),
                  currentSummary);
            }
         }

         currentSummary = (PSComponentSummary) currentMap.get(child.getName()
            .toLowerCase());

         if (currentSummary != null)
         {
            if (child.getName().equalsIgnoreCase(currentSummary.getName()))
            {
               if (child.getLocator().equals(currentSummary.getLocator()))
               {
                  /*
                   * The target already links to the same object. These might
                   * need special treatment in the calling method.
                   */
                  existingObjectSummaries.add(child);
               }
               else
               {
                  /*
                   * The caller tries to add a new object that has the same name
                   * as an existing child in the target.
                   */
                  sameNameSummaries.add(child);
               }
            }
         }
      }

      // report error
      if (!sameNameSummaries.isEmpty() || !duplicateChildren.isEmpty())
      {
         List parentList = new ArrayList();
         parentList.add(target);
         PSComponentSummaries parents = getComponentSummaries(
            parentList.iterator(), null, false);
         PSComponentSummary[] a = parents.toArray();
         String parentName = (a.length == 0) ? "Unknown" : a[0].getName();

         String[] args =
         {
            parentName,
            formatSummaryNames(sameNameSummaries.iterator()),
            formatSummaryNames(duplicateChildren.iterator())
         };
         throw new PSCmsException(IPSCmsErrors.DUPLICATE_ITEM_NAME, args);
      }

      // remove existing identical objects from supplied children
      walker = existingObjectSummaries.iterator();
      while (walker.hasNext())
         children.remove((PSComponentSummary) walker.next());

      return existingObjectSummaries;
   }

   /**
    * Formats a string of comma separated names for all supplied summaries.
    *
    * @param summaries the summaries for which to format a string of comma
    * separated name string, assumed not <code>null</code>, may be empty.
    * @return a string with all summary names of the supplied summaries
    * separated by comma, never <code>null</code>, may be empty.
    */
   private String formatSummaryNames(Iterator summaries)
   {
      StringBuffer names = new StringBuffer("");

      while (summaries.hasNext())
      {
         PSComponentSummary child = (PSComponentSummary) summaries.next();
         names.append(child.getName());
         if (summaries.hasNext())
            names.append(", ");
      }

      return names.toString();
   }

   /**
    * See {@link IPSRelationshipProcessor#move(String, PSKey, List, PSKey)}
    * interface} for general description. See {@link #validateKey(PSKey)} for
    * the requirement of the locators, <code>children</code> and
    * <code>sourceParent</code>.
    */
   @SuppressWarnings("unchecked")
   public void move(String componentType, PSKey sourceParent, List children,
      PSKey targetParent) throws PSCmsException
   {
      moveFolderChildren(sourceParent, children, targetParent, true);
   }

   /**
    * Move specified folder children from specified source folder to specified
    * target folder.
    * 
    * @param sourceParent the ID of the source folder, not <code>null</code>.
    * @param children the list of child IDs contained in the source folder, not
    * <code>null</code>.
    * @param targetParent the ID of the target folder, not <code>null</code>.
    * @param checkFolderPermission if <code>true</code>, then enforce folder
    * permission while moving the items; otherwise ignore the folder permission.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public void moveFolderChildren(PSKey sourceParent, List children,
      PSKey targetParent, boolean checkFolderPermission) throws PSCmsException
   {
      validateKeys(children.iterator());
      validateKey(sourceParent);
      validateKey(targetParent);

      // validating all ids, make sure they all exist in database
      List<PSLocator> allIds = new ArrayList<PSLocator>(children);
      allIds.add((PSLocator) sourceParent);
      allIds.add((PSLocator) targetParent);
      PSComponentSummaries summaries = getComponentSummaries( allIds
         .iterator(), null, false);

      // the moved children must not contain target parent
      int targetId = ((PSLocator) targetParent).getId();
      Iterator childIt = children.iterator();
      while (childIt.hasNext())
      {
         PSLocator child = (PSLocator) childIt.next();
         if (child.getId() == ((PSLocator) targetParent).getId())
            throw new IllegalArgumentException(
               "children must not contain targetParent, id=" + targetId);
      }

      // separate parents and children summaries
      PSComponentSummaries childSummaries = new PSComponentSummaries();
      PSComponentSummary[] allSummary = summaries.toArray();
      int srcId = ((PSLocator) sourceParent).getId();
      int tgtId = ((PSLocator) targetParent).getId();
      PSComponentSummary srcSummary = null;
      PSComponentSummary tgtSummary = null;
      for (int i = 0; i < allSummary.length; i++)
      {
         if (allSummary[i].getCurrentLocator().getId() == srcId)
            srcSummary = allSummary[i];
         else if (allSummary[i].getCurrentLocator().getId() == tgtId)
            tgtSummary = allSummary[i];
         else
            childSummaries.add(allSummary[i]);
      }

      // make sure both parents are folders
      if (!srcSummary.isFolder())
         throw new PSCmsException(IPSCmsErrors.INVALID_FOLDER_ID, srcId + "");
      if (!tgtSummary.isFolder())
         throw new PSCmsException(IPSCmsErrors.INVALID_FOLDER_ID, tgtId + "");

      // make sure we do not create a circular reference
      int donotfilterby = PSRelationshipConfig.FILTER_TYPE_COMMUNITY
         | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
      PSKey[] sources =
      {
         tgtSummary.getCurrentLocator()
      };
      PSKey[] parents = getFolderParents(sources, donotfilterby);
      
      List<PSLocator> filteredChildren = filterItems(children);
      
      if (testCircularReferences(parents, filteredChildren, donotfilterby))
      {
         // detected circular reference, throw exception
         StringBuilder names = new StringBuilder("");
         Iterator<?> sourceSummaries = childSummaries.iterator();
         while (sourceSummaries.hasNext())
         {
            PSComponentSummary sourceSummary = (PSComponentSummary) sourceSummaries
               .next();

            if (sourceSummary.isFolder())
            {
               names.append(sourceSummary.getName());
               if (sourceSummaries.hasNext())
                  names.append(", ");
            }
         }
         Object[] args =
         {
            tgtSummary.getName(), names
         };
         throw new PSCmsException(IPSCmsErrors.CIRCULAR_FOLDER_REFERENCE, args);
      }

      // validates permission, names, ..etc
      PSComponentSummaries existingObjectsSummaries = validateChildren(
         children, childSummaries, targetParent);

      if (checkFolderPermission)
      {
         checkHasMovePermission(sourceParent, children, targetParent,
               true);
      }

      // disable any more permission checking
      PSFolderSecurityManager.setCheckFolderPermissions(false);

      try
      {
         // First remove the site folder publish flag property
         // if this folder is being moved under the //Folders
         // root
         String[] targetPaths = getRelationshipOwnerPaths(PSDbComponent.getComponentType(PSFolder.class),
            (PSLocator) targetParent, FOLDER_RELATE_TYPE);

         if (targetPaths.length > 0
            && !PSPathUtil.isPathUnderSiteFolderRoot(targetPaths[0]))
         {
            Iterator<?> it = childSummaries.iterator();
            while (it.hasNext())
            {
               PSComponentSummary summary = (PSComponentSummary) it.next();
               PSLocator locator = new PSLocator(summary.getLocator().getPart(
                  PSLocator.KEY_ID), "1");

               if (summary.isFolder())
               {
                  PSFolder folder = openFolder(new PSLocator[]
                  {
                     locator
                  })[0];
                  if (folder.isPublishOnlyInSpecialEdition())
                  {
                     folder.setPublishOnlyInSpecialEdition(false);
                     save(folder);
                  }
               }
            }
         }
         
         setMoveParametersForEffects(PSThreadRequestUtils.getPSRequest(),sourceParent, targetParent);
         
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

         processor.move(FOLDER_RELATE_TYPE, sourceParent, children,
            targetParent);

         /*
          * Because already existing objects are not moved, we need to remove
          * them from the source in an extra step.
          */
         processor.delete(FOLDER_RELATE_TYPE, sourceParent,
            existingObjectsSummaries.getLocators());
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
      finally
      {
         removeMoveParametersForEffects(PSThreadRequestUtils.getPSRequest());
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
   }

   /**
    * Removes Source and Target parameters used for effects on move.
    * @see IPSHtmlParameters#SYS_MOVE_SOURCE_FOLDER_ID
    * @see IPSHtmlParameters#SYS_MOVE_TARGET_FOLDER_ID
    */
   private void removeMoveParametersForEffects(PSRequest request)
   {
      request.removeParameter(IPSHtmlParameters.SYS_MOVE_SOURCE_FOLDER_ID);
      request.removeParameter(IPSHtmlParameters.SYS_MOVE_TARGET_FOLDER_ID);
   }
   /**
    * Sets Source and Target parameters used for effects on move.
    * @param sourceParent source folder, never null.
    * @param targetParent target folder, never null.
    * @see IPSHtmlParameters#SYS_MOVE_SOURCE_FOLDER_ID
    * @see IPSHtmlParameters#SYS_MOVE_TARGET_FOLDER_ID
    */
   private void setMoveParametersForEffects(PSRequest request, PSKey sourceParent,
         PSKey targetParent)
   {
      request.setParameter(IPSHtmlParameters.SYS_MOVE_SOURCE_FOLDER_ID, sourceParent.getPart());
      request.setParameter(IPSHtmlParameters.SYS_MOVE_TARGET_FOLDER_ID, targetParent.getPart());
   }

   /**
    * For every folder in the supplied list, place it's locator in the new list.
    * 
    * @param source List of folder and item ids. Assumed not <code>null</code>.
    * 
    * @return A new list with just the folder ids. Never <code>null</code>.
    */
   private List<PSLocator> filterItems(List<PSLocator> source)
   {
      List<PSLocator> results = new ArrayList<PSLocator>();
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache == null)
      {
         List<Integer> ids = new ArrayList<Integer>();
         for (PSLocator l : source)
         {
            ids.add(l.getId());
         }
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         List<PSComponentSummary> summarylist = cms.loadComponentSummaries(ids);
         for (int i=0; i < summarylist.size(); i++)
         {
            if (summarylist.get(i).isFolder())
               results.add(source.get(i));
         }
      }
      else
      {
         for (PSLocator l : source)
         {
            IPSItemEntry e = cache.getItem(l.getId());
            if (e.isFolder())
               results.add(l);
         }
      }
      return results;
   }
   
  

   /**
    * Tests whether a circulur reference would be encountered if any of the
    * supplied children were moved to any of the supplied parents.
    *
    * @param parents the parents to test for circular references, assumed not
    * <code>null</code>, assumed to be folder ids.
    * @param children the children to test for circular references, assumed not
    * <code>null</code>, each entry assumed to be a <code>PSKey</code>.
    * @param filter the filter flags to use to lookup folder parents.
    * @return <code>true</code> if this would create a circular reference,
    * <code>false</code> otherwise.
    * @throws PSCmsException for any error.
    */
   private boolean testCircularReferences(PSKey[] parents, Collection children,
      int filter) throws PSCmsException
   {
      for (int i = 0; i < parents.length; i++)
      {
         PSKey parent = parents[i];
         Iterator walker = children.iterator();
         while (walker.hasNext())
         {
            PSKey child = (PSKey) walker.next();

            // only compare folder id's, revisions are not relevant for folders
            if (parent.getPartAsInt(parent.getDefinition()[0]) == child
               .getPartAsInt(parent.getDefinition()[0]))
               return true;

            PSKey[] sources =
            {
               parent
            };

            if (testCircularReferences(getFolderParents(sources,
               filter), children, filter))
               return true;
         }
      }

      return false;
   }

   /**
    * Recursively copy a list of components (items or folder) to a target
    * folder. This is done by creating relationships between the list of items
    * and the target folder. For each folder on the list of components, creates
    * a new folder and creates relationship between the new folder and the
    * target folder. Go through the same process for each folder component.
    * <p>
    * See {@link IPSRelationshipProcessor#copy(String,List,PSKey) interface} for
    * general description. See {@link #validateKey(PSKey)} for the requirement
    * of the locators, <code>children</code> and <code>targetParent</code>.
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#copyChildren(List, PSLocator)}.
    */
   public void copy(@SuppressWarnings("unused")
   String relationshipType, List children, PSKey targetParent)
      throws PSCmsException
   {
      checkHasCopyPermission(children, targetParent, true);

      // disable any more permission checking
      PSFolderSecurityManager.setCheckFolderPermissions(false);

      try
      {
         processFolderChildren(children, (PSLocator) targetParent,
            true);
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
      finally
      {
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
   }

   /**
    * The subfolders of the specified parent folder identified by its locator
    * <code>parent</code>, are not filtered by community (of the current
    * user).
    *
    * See {@link IPSRelationshipProcessor#getChildren(String,PSKey) interface}
    * for general description. See {@link #validateKey(PSKey)} for the
    * requirement of the <code>parent</code> locator.
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#getChildSummaries(PSLocator)}.
    */
   public PSComponentSummary[] getChildren(@SuppressWarnings("unused")
   String componentType, PSKey parent) throws PSCmsException
   {
      return getChildren(componentType, FOLDER_RELATE_TYPE, parent);
   }

   /**
    * Gets the children of <code>parentFolder</code>, plus their children
    * recursively and returns their ids.
    *
    * @param parentFolder The id of the folder for which you want the tree of
    * children. Never <code>null</code>. Must be a persisted key, meaning it
    * must be obtained from a query, not created manually.
    *
    * @param recurse If <code>true</code>, get all ids in the tree under the
    * parent, otherwise, only return the direct children.
    *
    * @return Never <code>null</code>, may be empty. Each entry is an
    * <code>Integer</code> containing the content id of the item or folder.
    *
    * @throws PSCmsException If any problems getting the children, including
    * parentFolder being invalid.
    */
   public Set<Integer> getChildIds(PSLocator parentFolder, boolean recurse)
      throws PSCmsException
   {
       return getChildIds(parentFolder, recurse, FOLDER_RELATE_TYPE);
   }

    public Set<Integer> getChildIds(PSLocator parentFolder, boolean recurse, String relationshipTypeName)
            throws PSCmsException {
        /*
         * Impl note: It is a goal that the ids returned by this method match the
         * ids (summaries) returned by getChildren. However, for performance
         * reasons, they don't share the same code path. Ideally,
         * getFolderChildren would call the local getDependentLocators rather than
         * calling the one in the processor. See that method for more details
         * about the performance issue.
         */
        if (null == parentFolder)
        {
            throw new IllegalArgumentException("locator cannot be null");
        }

        try
        {
            PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

            List children = getDependentLocators(processor, parentFolder,
                    getFilterFlags(), recurse, relationshipTypeName);

            Set<Integer> locators = new HashSet<Integer>();
            for (Iterator iter = children.iterator(); iter.hasNext();)
            {
                PSLocator loc = (PSLocator) iter.next();
                locators.add(new Integer(loc.getId()));
            }
            return locators;
        }
        catch (PSException e)
        {
            throw new PSCmsException(e);
        }
    }

   /**
    * Centralizes the hack described in the method.
    *
    * @return A set of <code>PSRelationshipConfig.FILTER_TYPE_</code> flags
    * appropriate for getting the children of a folder.
    */
   private int getFilterFlags()
   {
      int donotfilterby = PSRelationshipConfig.FILTER_TYPE_COMMUNITY;

      /*
       * Content explorer sends a special boundary as a hint to filter the
       * content by user community. This is really a hack but I had to do this
       * since I cannot change the interface until next release to include
       * community filter flag. TODO This must be resolved during next release.
       */
      String ctype = PSThreadRequestUtils.getPSRequest().getCgiVariable(IPSCgiVariables.CGI_CONTENT_TYPE);
      if (ctype != null && ctype.indexOf(PSHttpConnection.CX_BOUNDARY) != -1
            && !isCommunityFolderVisibilityOverride())
         donotfilterby = PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY;
      return donotfilterby;
   }

   /**
    * If the property mentioned below is set, then folders that are assigned to
    * a particular community will be visible to all communities as controlled by
    * the 'Folder Community' ACL entry. Otherwise, folders assigned to a 
    * community are only visible to users currently logged into that community
    * when requested from the CX.
    * <p>
    * See bug RX-13261 for details. 
    * 
    * @return <code>true</code> if the
    * <code>enableCommunityFolderVisibilityByAcl</code> server property is
    * <code>true</code>, <code>false</code> otherwise.
    */
   private boolean isCommunityFolderVisibilityOverride()
   {
      String value = PSServer.getServerProps()
         .getProperty("enableCommunityFolderVisibilityByAcl", "false")
         .toLowerCase();
      return value.equals("true") || value.equals("yes") || value.equals("1");
   }
   
   /**
    * Deletes relationship between the specified parent and a list of its
    * children. For each folder children, recursively delete the folder and the
    * relationship with its children. Do the same to each "grand" children.
    * <p>
    * See {@link IPSRelationshipProcessor#delete(String,PSKey,List) interface}
    * for general description. See {@link #validateKey(PSKey)} for the
    * requirement of the locators. <code>children</code> and
    * <code>sourceParent</code>.
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#removeChildren(PSLocator, List)}.
    */
   @SuppressWarnings("unchecked")
   public void delete(String relType, PSKey sourceParent, List children)
      throws PSCmsException
   {
      validateRelType(relType);
      validateKeys(children.iterator());
      validateKey(sourceParent);

      // recurvisely delete the child folder objects and its relationship with
      // its own children.
      PSComponentSummaries summaries = getComponentSummaries(children
         .iterator(), (PSLocator) sourceParent, false);
      List childFolders = summaries
         .getComponentLocators(PSComponentSummary.TYPE_FOLDER,
            PSComponentSummary.GET_CURRENT_LOCATOR);
      PSKey[] locators = new PSKey[childFolders.size()];
      Iterator it = childFolders.iterator();
      for (int i = 0; it.hasNext(); i++)
         locators[i] = (PSKey) it.next();

      // delete the descendents of the child folders and child folders
      // themselves
      // delete(FOLDER_PROXY_TYPE, locators);
      deleteFolders(locators, true, relType);

      // delete the relationship between the folder and its specified child
      // items
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      List childItems = summaries.getComponentLocators(
         PSComponentSummary.TYPE_ITEM, PSComponentSummary.GET_CURRENT_LOCATOR);
      processor.delete(relType, sourceParent, childItems);
   }

   /**
    * Get summary information for a parent and a list of its child locators. The
    * revision of the locator is not used.
    * <p>
    * <br>
    * This method sets the actuals permissions on the folders, which has a
    * performance penalty. If you do not need the actual permission values on
    * folders, use the
    * <code>getComponentSummaries(PSRequest, Iterator, PSLocator, boolean)</code>
    * method instead.
    * <p>
    * <br>
    * Convenience method that calls
    * {@link #getComponentSummaries(Iterator, PSLocator, boolean)}
    *
    * @param childIds A list of one or more <code>PSLocator</code> objects,
    * may not be <code>null</code>, but may be empty.
    *
    * @param plocator The parent locator, it may be <code>null</code> if don't
    * want to use the parent to filter the children, this has not been
    * implemented yet.
    *
    * @return The creates component summaries object, never <code>null</code>.
    * It contains the requested number of components, which is the length of
    * <code>childIds</code>.
    *
    * @throws PSCmsException if unable to get the number of requested components
    * which may caused by invalid <code>PSLocator</code> in <code>childIds
    *    </code>
    * or <code>plocator</code>.
    */
   public PSComponentSummaries getComponentSummaries(
      Iterator<PSLocator> childIds, PSLocator plocator) throws PSCmsException
   {
      return getComponentSummaries( childIds, plocator, true);
   }

   /**
    * Gets a list of folder acls from the supplied folder ids.
    *
    * @param ids the folder ids, which must be a list of existing folder ids.
    *
    * @return An array of folder acl objects, never <code>null</code>, may be
    * emtpy. Not guaranteed to contain an acl corresponding to each id supplied.
    *
    * @throws IllegalArgumentException if any of the folder ids not exist in the
    * folder cache when folder cache is on.
    * @throws PSCmsException if any other error occurs.
    */
   public PSFolderAcl[] getFolderAcls(int[] ids) throws PSCmsException
   {
      PSItemSummaryCache cache = getItemCache();
      PSFolderAcl[] acls = null;
      if (cache != null)
      {
         acls = new PSFolderAcl[ids.length];
         int j = 0;
         int i = 0;
         for (; i < ids.length; i++)
         {
            PSFolderAcl acl = cache.getFolderAcl(ids[i]);
            if (acl != null)
               acls[j++] = acl;
         }

         if (j < i) // shrink the array, so that there is no NULL element.
         {
            PSFolderAcl[] acls_new = new PSFolderAcl[j];
            for (i = 0; i < j; i++)
               acls_new[i] = acls[i];
            acls = acls_new;
         }
      }
      else
      {
         acls = PSFolderSecurityManager.loadFolderAcls(ids);
      }

      return acls;
   }

   /**
    * Returns the publishing file name for the supplied folder id. It is the
    * same value that is described in {@link PSFolder#getPubFileName()}.
    *
    * @param contentid the folder content id.
    *
    * @return the publishing file name, never <code>null</code>, but may be
    * empty if the folder does not exist.
    *
    * @throws PSCmsException if an error occurs.
    */
   public String getPubFileName(int contentid) throws PSCmsException
   {
      PSItemSummaryCache cache = getItemCache();
      String pubFileName = "";
      if (cache != null) // get the property value from cache
      {
         IPSItemEntry item = cache.getItem(contentid);
         if (item instanceof PSFolderEntry)
            pubFileName = ((PSFolderEntry) item).getPubFileName();
      }
      else
      // get the property value from the backend repository
      {
         PSLocator locator = new PSLocator(contentid, 1);
         PSFolder[] folders = openFolder(new PSKey[]
         {
            locator
         });
         if (folders.length > 0)
            pubFileName = folders[0].getPubFileName();
      }

      return pubFileName;
   }

   /**
    * Returns the global template property for the supplied folder id. It is the
    * same value that is described in
    * {@link PSFolder#getGlobalTemplateProperty()}.
    *
    * @param contentid the folder content id.
    *
    * @return the global template property, may be <code>null</code> or empty
    * if the folder does not exist or the global template property is not
    * defined in the folder.
    *
    * @throws PSCmsException if an error occurs.
    */
   public String getGlobalTemplateProperty(int contentid) throws PSCmsException
   {
      PSItemSummaryCache cache = getItemCache();
      String globalTemplate = null;
      if (cache != null) // get the property value from cache
      {
         IPSItemEntry item = cache.getItem(contentid);
         if (item instanceof PSFolderEntry)
            globalTemplate = ((PSFolderEntry) item).getGlobalTemplateProperty();
      }
      else
      // get the property value from the backend repository
      {
         PSLocator locator = new PSLocator(contentid, 1);
         PSFolder[] folders = openFolder(new PSKey[]
         {
            locator
         });
         if (folders.length > 0)
            globalTemplate = folders[0].getGlobalTemplateProperty();
      }

      return globalTemplate;

   }

   /**
    * Same as {@link #getComponentSummaries(Iterator, PSLocator, boolean)} except the additional parameter described below.
    *
    * @param setFolderPermissions if <code>true</code> and the component is of
    * folder type then it loads the folder to get its ACL, calculates the user's
    * permission on the folder and sets it in the component summary object. If
    * <code>false</code> and the component is a folder, then sets the folder
    * permission to <code>PSObjectPermissions.ACCESS_DENY</code>. Setting
    * this parameter to <code>true</code> has a performance penalty so use
    * <code>false</code> unless the actual permissions on the folder is
    * required. This parameter is ignored for non-folder objects.
    * @param plocator
    * @param setFolderPermissions
    */
   public PSComponentSummaries getComponentSummaries(
      Iterator<PSLocator> childIds,
      PSLocator plocator, boolean setFolderPermissions) throws PSCmsException
   {
      if (childIds == null)
         throw new IllegalArgumentException("childIds may not be null");

      PSComponentSummaries summaries = new PSComponentSummaries();

      if (!childIds.hasNext())
         return summaries;

      // Create a list of child content ids from the locators
      List<Integer> ids = new ArrayList<>();
      while (childIds.hasNext())
      {
         ids.add(childIds.next().getId());
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> summarylist = cms.loadComponentSummaries(ids);
      if (setFolderPermissions)
      {
         setFolderPermissions(summarylist);
      }
      summaries.addAll(summarylist);

      return summaries;
   }

   /**
    * Set the folder permissions for a group of component summaries for a
    * specific request. This will only modify the summaries that are folders.
    *
    * @param summarylist the list of component summaries, assumed never
    * <code>null</code>
    * @throws PSCmsException
    */
   private void setFolderPermissions(
      List<PSComponentSummary> summarylist) throws PSCmsException
   {
      List<Integer> folderids = new ArrayList<>();
      for (PSComponentSummary s : summarylist)
      {
         if (s.getObjectType() == 2)
         {
            folderids.add(s.getContentId());
         }
      }
      int fids[] = new int[folderids.size()];
      for (int i = 0; i < fids.length; i++)
      {
         fids[i] = folderids.get(i);
      }
      PSFolderAcl acls[] = getFolderAcls(fids);
      Map<Integer, PSFolderAcl> aclMap = new HashMap<Integer, PSFolderAcl>();
      for (int i = 0; i < acls.length; i++)
         aclMap.put(new Integer(acls[i].getContentId()), acls[i]);
      for (PSComponentSummary s : summarylist)
      {
         if (s.getObjectType() == 2)
         {
            PSFolderAcl acl = aclMap.get(s.getContentId());
            if (acl == null)
            {
               // no, acl, so they get fulll permissions
               s.setPermissions(new PSFolderPermissions(
                  PSObjectPermissions.ACCESS_ALL));
            }
            else
            {
               try
               {
                  PSFolderPermissions folderPerms = new PSFolderPermissions(
                     acl);
                  s.setPermissions(folderPerms);
               }
               catch (PSAuthorizationException e)
               {
                  throw new PSCmsException(e);
               }
            }
         }
      }
   }

   /**
    * A helper method to recursively copy or link a list of components to a
    * folder. See {@link #copy(String, List, PSKey)} for its detail description.
    *
    * @param children a list <code>PSlocator</code> or
    * <code>PSLocatorWithName</code> objects with all children that need to be
    * copied, assumed not <code>null</code> nor mixed objects.
    * @param targetParent the target folder locator, assume not
    * <code>null</code>.
    * @param copyItem <code>true</code> to copy both items and folders;
    * otherwise copy folders only and link items to the specified parent.
    * @throws PSException if an error occurs during the copy process.
    */
   private void processFolderChildren(List<PSLocator> children,
      PSLocator targetParent, boolean copyItem) throws PSException
   {
      validateKeys(children.iterator());
      validateKey(targetParent);

      if (!children.isEmpty())
      {
         boolean isOverrideName = false;
         if (children.get(0) instanceof PSLocatorWithName && copyItem)
            isOverrideName = true;

         PSComponentSummaries childSummaries = getComponentSummaries(
            children.iterator(), null, false);

         // make sure targetParent is not a descendent of the childen
         validateTargetParentIsNotDescendent(targetParent, childSummaries
            .getComponentList(PSComponentSummary.TYPE_FOLDER), copyItem);

         if (isOverrideName)
            setSummariesWithNewName(childSummaries, children);

         PSUserInfo userInfo = PSThreadRequestUtils.getUserInfo();
         processFolderChildrenRecursive(childSummaries, targetParent,
            copyItem, userInfo, isOverrideName);
      }
   }

   /**
    * Set the override name from the supplied locators to all component
    * summaries
    *
    * @param summaries the component summaries, assume not <code>null</code>,
    * but may be empty.
    * @param locators the locators of the summaries, assume not
    * <code>null</code>, but may be empty.
    */
   private void setSummariesWithNewName(PSComponentSummaries summaries,
      List locators)
   {
      PSComponentSummary[] sumArray = summaries.toArray();
      for (int i = 0; i < sumArray.length; i++)
      {
         // get the name from locators which has the same id as sumArray[i]
         Iterator locs = locators.iterator();
         int id = sumArray[i].getCurrentLocator().getId();
         String name = null;
         while (locs.hasNext())
         {
            PSLocatorWithName locName = (PSLocatorWithName) locs.next();
            if (locName.getId() == id)
               name = locName.getOverrideName();
         }
         sumArray[i].setName(name);
      }
   }

   /**
    * Just like {@link #processFolderChildren(List, PSLocator, boolean)}
    * except this takes component summaries instead of a list for the 2nd
    * parameter.
    *
    * @param summaries The to be copied child component summaries. Assume not
    * <code>null</code>. The name of the components will be used for cloned
    * items and folders if <code>isOverrideName</code> is <code>true</code>.
    * @param isOverrideName Indicates override the sys_title when clone items.
    * <code>true</code> if do override the sys_title.
    *
    * @throws PSException if an error occurs.
    */
   private void processFolderChildrenRecursive(
      PSComponentSummaries summaries, PSLocator parent, boolean copyItem,
      PSUserInfo userInfo, boolean isOverrideName) throws PSException
   {
      List<PSLocator> childItems = summaries.getComponentLocators(
         PSComponentSummary.TYPE_ITEM, PSComponentSummary.GET_CURRENT_LOCATOR);

      if (copyItem)
      {
         childItems = cloneItems( 
            summaries.getComponents(PSComponentSummary.TYPE_ITEM), 
            isOverrideName, null, true, false);
      }

      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      if (!childItems.isEmpty())
      {
         if (copyItem)
         {
            try
            {
               PSComponentSummaries childSummaries = getComponentSummaries( childItems.iterator(), null, false);

               validateChildren(childItems, childSummaries, parent);
            }
            catch (PSCmsException e)
            {
               if (e.getErrorCode() == IPSCmsErrors.DUPLICATE_ITEM_NAME)
               {
                  Object args[] =
                  {
                     e.getLocalizedMessage()
                  };
                  throw new PSCmsException(
                     IPSCmsErrors.DUPLICATE_ITEM_NAME_COPY_CREATED, args);
               }

               throw e;
            }
         }

         processor.add(FOLDER_RELATE_TYPE, childItems, parent);
      }

      /*
       * For each folder child, clone a new folder from it and insert the
       * relationship between the new folder child and its parent.
       */
      Iterator childFolders = summaries
         .getComponents(PSComponentSummary.TYPE_FOLDER);
      while (childFolders.hasNext())
      {
         // clone and insert a new folder
         PSComponentSummary origComp = (PSComponentSummary) childFolders.next();
         Element[] elements = load(FOLDER_PROXY_TYPE, new PSKey[]
         {
            origComp.getCurrentLocator()
         });
         PSFolder folder = new PSFolder(elements[0]);
         PSFolder newFolder = (PSFolder) folder.clone();

         // the folder name may be overridden
         newFolder.setName(origComp.getName());

         /*
          * Check if the user is an admin. If the user is not an admin, add an
          * ACL entry to make the current user an admin.
          */
         boolean isAdmin = newFolder.getPermissions().hasAdminAccess();
         if (!isAdmin)
         {
            // see if an acl entry for this user exists in the ACL
            PSObjectAclEntry newAclEntry = new PSObjectAclEntry(
               PSObjectAclEntry.ACL_ENTRY_TYPE_USER, userInfo.getUserName(),
               PSObjectAclEntry.ACCESS_ADMIN);

            PSObjectAclEntry aclEntry = (PSObjectAclEntry) newFolder.getAcl()
               .get(newAclEntry);
            if (aclEntry != null)
               aclEntry.setAdminAccess(true);
            else
               newFolder.getAcl().add(newAclEntry);
         }

         PSSaveResults results = save(new IPSDbComponent[]
         {
            newFolder
         });
         PSLocator newLocator = (PSLocator) results.getResults()[0]
            .getLocator();

         // add relationship between the inserted folder and the parent
         childItems.clear();
         childItems.add(newLocator);
         processor.add(FOLDER_RELATE_TYPE, childItems, parent);

         /*
          * Recursively add the children of the original folder to the new child
          * folder.
          */
         List grandChildren = processor.getDependentLocators(
            FOLDER_RELATE_TYPE, origComp.getCurrentLocator());
         if (grandChildren.size() > 0)
         {
            PSComponentSummaries gradChildSummaries = getComponentSummaries( grandChildren.iterator(), null, false);
            processFolderChildrenRecursive(gradChildSummaries,
               newLocator, copyItem, userInfo, isOverrideName);
         }
      }
   }

   /**
    * Clone a list of items.
    *
    * @param summaries A list of to be cloned <code>PSComponentSummary</code>
    * objectslocators. Assume not <code>null</code>, but may be empty.
    * @param isOverrideName Indicates override sys_title when an clone item.
    * <code>true</code> if do override.
    * @param communityMappings a map of source to target community mappings, may
    * be <code>null</code> or empty. The key is the source community as
    * <code>Integer</code>, the value is the target community as
    * <code>Integer</code>.
    * @param isUseUserCommunity <code>true</code> if the community of the
    *    cloned item is set to the current user community; otherwise the 
    *    community of the cloned item is resolved from the
    *    <code>communityMappings</code> parameter. The community of the cloned
    *    item will be set to the community of the source item if cannot find
    *    the community id from the session of the request or the mapping 
    *    parameter.   
    * @param useSrcWorfklow true to use the source item's workflow if valid for
    * the content type and community, <code>false</code> to calculate the workflow
    * as when creating a new item.
    * @return a list of new locators which have been cloned from the input
    * locators in the same order as requested. Never <code>null</code>, may
    * be empty.
    * @throws PSException if an error occurs.
    */
   private List cloneItems(Iterator<PSComponentSummary> summaries,
      boolean isOverrideName, Map<Integer,Integer> communityMappings, boolean isUseUserCommunity, boolean useSrcWorfklow)
      throws PSException
   {
      List<PSLocator> newLocators = new ArrayList<>();
      PSContentDataHandler dataHandler = new PSContentDataHandler();
      PSItemDefManager defManager = PSItemDefManager.getInstance();
      PSRequest request = PSThreadRequestUtils.getPSRequest();
      while (summaries.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) summaries.next();

         PSLocator source = summary.getCurrentLocator();
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID, ""
               + source.getId());
            request.setParameter(IPSHtmlParameters.SYS_REVISION, ""
               + source.getRevision());

         /*
          * override the name is requested
          */
         if (isOverrideName)
            request.setParameter(IPSHtmlParameters.SYS_TITLE_OVERRIDE,
               summary.getName());

         /*
          * override the community, always set the "sys_communityid_override"
          */
         int communityId = summary.getCommunityId(); // the default community id
         if (isUseUserCommunity)
         {
            Object objCommunityId = request.getUserSession().getPrivateObject(
                     IPSHtmlParameters.SYS_COMMUNITY);
            if (objCommunityId != null)
            {
               communityId = Integer.parseInt(objCommunityId.toString());
            }
         }
         else // get the communityid from the community mappings
         {
            if (communityMappings != null && (!communityMappings.isEmpty()))
            {
               Integer newCommunity = (Integer) communityMappings.get(
                     new Integer(communityId));
      
               if (newCommunity != null)
                  communityId = newCommunity.intValue();
            }
         }
         request.setParameter(IPSHtmlParameters.SYS_COMMUNITYID_OVERRIDE,
                  String.valueOf(communityId));

         /*
          * Create the new item copy in the default workflow for the specified
          * community unless use src is specified.
          */
         try
         {
            PSItemDefinition def = defManager.getItemDef(
                  summary.getContentTypeId(), communityId);
               PSContentEditor ce = def.getContentEditor();
            
            int tgtWfId;
            int srcWfId = summary.getWorkflowAppId();
            if (useSrcWorfklow && PSCms.isAllowedWorkflow(ce, String.valueOf(communityId), srcWfId))
            {
               tgtWfId = srcWfId;
            }
            else
            {
               tgtWfId = PSCms.getDefaultWorkflowId(request, ce, 
                     String.valueOf(communityId));
            }
            
            request.setParameter(IPSHtmlParameters.SYS_WORKFLOWID_OVERRIDE,
               Integer.toString(tgtWfId));
         }
         catch (RuntimeException e)
         {
            /*
             * This is thrown if no single default workflow id is found. Log
             * the error and continue cloning items.
             */
            getLogger().error(
               "Skipped item because no single workflow was "
                  + "found. The skipped item was [name (id)]: "
                  + summary.getName() + " ("
                  + summary.getCurrentLocator().getId() + ").");
            setHadErrors(true);
            continue;
         }

         dataHandler.newCopy(request);

         String id = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String rev = request.getParameter(IPSHtmlParameters.SYS_REVISION);
         PSLocator target = new PSLocator(id, rev);

         newLocators.add(target);

         getLogger().debug(
            "Copied item " + source.getId() + " --> " + target.getId());

         PSRelationshipTracker tracker = (PSRelationshipTracker) m_tracker
            .get();
         if (tracker != null)
            tracker.addItemMapping(request, source, target);
      }

      return newLocators;
   }

   /**
    * Get children for a specified folder. The child item can be either item or
    * folder.
    *
    * @param folderLocator The specified folder locator.
    *
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to
    * be performed.
    *
    * @param setFolderPermissions if <code>true</code> and the component is of
    * folder type then it loads the folder to get its ACL, calculates the user's
    * permission on the folder and sets it in the component summary object. If
    * <code>false</code> and the component is a folder, then sets the folder
    * permission to <code>PSObjectPermissions.ACCESS_DENY</code>. Setting
    * this parameter to <code>true</code> has a performance penalty so use
    * <code>false</code> unless the actual permissions on the folder is
    * required. This parameter is ignored for non-folder objects. Must be
    * <code>true</code> if the previous parameter (doNotApplyFilters) does not
    * include the flag
    * {@link PSRelationshipConfig#FILTER_TYPE_FOLDER_PERMISSIONS} else throws 
    * illegal argument exception.
    * 
    * @return The component summaries object, never <code>null</code>.
    *
    * @throws PSCmsException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private PSComponentSummaries getFolderChildren(
      PSLocator folderLocator, int doNotApplyFilters,
      boolean setFolderPermissions, String relationshipTypeName) throws PSCmsException
   {
      int folderPermissionFilterFlag = 
         PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
      boolean filterByPermissions = 
         (doNotApplyFilters & folderPermissionFilterFlag) != folderPermissionFilterFlag;
      if(!setFolderPermissions && filterByPermissions)
      {
         throw new IllegalArgumentException(
            "setFolderPermissions must be true to filter by permissions");
      }
      try
      {
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

         // Do not filter by folder permissions. If we need to filter it by
         // folder permissions then we will do it when calling
         // filterByFolderPermissions() method. This will have the benefit of
         // loading the folder ACLs only once.
         // Otherwise relationship processor will load them and
         // getComponentSummaries() will load them again.
         PSFolderSecurityManager.setCheckFolderPermissions(false);
         List children = null;
         try
         {
            children = processor.getDependentLocators(relationshipTypeName,
               folderLocator,
               PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS
                  | doNotApplyFilters);
         }
         finally
         {
            // enable permission checking
            PSFolderSecurityManager.setCheckFolderPermissions(true);
         }

         // get the component summaries along with the permissions
         PSComponentSummaries summaries = getComponentSummaries(
             children.iterator(), folderLocator,
            setFolderPermissions);

         if (filterByPermissions)
         {
            summaries = filterByFolderPermissions(summaries,
               PSObjectPermissions.ACCESS_READ);
         }

         return summaries;
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Gets all children of a folder (items and folders) and returns their
    * locators.
    *
    * @param proc Assumed not <code>null</code>. Used to get the child
    * relationships.
    *
    * @param folderId Must be a persisted key. Assumed not <code>null</code>.
    *
    * @param doNotApplyFilters A set of flags from the set of
    * <code>PSRelationshipConfig.FILTER_TYPE_xxx</code> flags.
    *
    * @param recursive If <code>true</code>, then calls itself for each child
    * that is a folder and adds those children to the list before adding the
    * current child folder.
    *
    * @return Each entry is a PSLocator. Never <code>null</code>, may be
    * empty.
    *
    * @throws PSCmsException If any problems getting the relationships.
    */
   @SuppressWarnings("unchecked")
   private static List getDependentLocators(PSRelationshipProcessor proc,
      PSKey folderId, int doNotApplyFilters, boolean recursive)
      throws PSCmsException
   {
        return getDependentLocators(proc, folderId, doNotApplyFilters, recursive, FOLDER_RELATE_TYPE);
   }

    private static List getDependentLocators(PSRelationshipProcessor proc,
                                             PSKey folderId, int doNotApplyFilters, boolean recursive,
                                             String relationshipTypeName)
            throws PSCmsException {
        PSRelationshipSet relationships = proc.getDependents(relationshipTypeName,
                folderId, doNotApplyFilters);

        List result = new ArrayList();
        Iterator it = relationships.iterator();
        while (it.hasNext())
        {
            PSRelationship relationship = (PSRelationship) it.next();
            if (recursive
                    && relationship.getDependentObjectType() == PSCmsObject.TYPE_FOLDER)
            {
                List tmpResults = getDependentLocators(proc, relationship
                        .getDependent(), doNotApplyFilters, recursive, relationshipTypeName);
                result.addAll(tmpResults);
            }

            result.add(relationship.getDependent());
        }

        return result;
    }

   /**
    * Filters out the component summaries for folders on which the user does
    * have the specified access. This does not filter out non-folder objects.
    *
    * @param summaries collection of component summaries which needs to be
    * filtered, assumed not <code>null</code>, may be empty
    *
    * @param permission the permission that the user must have on the folder
    * object, should be non-negative, assumed to be a valid access level
    *
    * @return the filtered component summary collection, this contains all the
    * non-folder components and all folder components on which the user has the
    * specified permission
    */
   private PSComponentSummaries filterByFolderPermissions(
      PSComponentSummaries summaries, int permission)
   {
      PSComponentSummaries retSummaries = new PSComponentSummaries();

      Iterator it = summaries.getSummaries();
      while (it.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) it.next();
         boolean addSummary = true;
         if (summary.isFolder())
         {
            if (!(summary.getPermissions().hasAccess(permission)))
               addSummary = false;
         }
         if (addSummary)
            retSummaries.add(summary);
      }
      return retSummaries;
   }

   /**
    * Helper method to recursively retrieve all descendant folder IDs
    *
    * @param parent A valid key that references the current owner of the
    * relationships to all the supplied children. A valid key is one that
    * references an existing object in the database. Never <code>null</code>.
    * @param results A List that will be used to store the results. This is a
    * list over zero or more {@link PSLocator} objects.
    * @param collectFolderWithItem if it is <code>true</code>, then collect
    * the folder IDs that contain items (object-type = TYPE_ITEM)
    * @param doNotApplyFilters mask to restrict the filtering of relationships
    * based on community or folder permissions (bitwise OR the FILTER_BY_xxx
    * constants defined in <code>PSRelationshipConfig</code> to restrict
    * filtering on both), should be set to <code>0</code> if filtering is to be
    * performed.
    *
    * @throws PSCmsException if any error occurs
    */
   @SuppressWarnings("unchecked")
   private void getDescendents(PSLocator parent, List<PSLocator> results, 
      boolean collectFolderWithItem, int doNotApplyFilters) throws PSCmsException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      PSRelationshipSet rels = processor.getDependents(FOLDER_RELATE_TYPE,
         parent, doNotApplyFilters);
      
      if (collectFolderWithItem && hasItemDependent(rels))
         results.add(parent);
      
      // recursive to sub-folders.
      Iterator itRels = rels.iterator();
      PSRelationship rel;
      PSLocator depLocator;
      while (itRels.hasNext())
      {
         rel = (PSRelationship) itRels.next();
         depLocator = rel.getDependent();

         // First check to be sure that this item has
         // not yet been processed, we don't want to
         // accidently fall into an infinite recursion loop if
         // a child some how links back to itself
         if (results.contains(depLocator))
            continue;

         if (rel.getDependentObjectType() == PSCmsObject.TYPE_FOLDER)
         {
            if (!collectFolderWithItem)
               results.add(depLocator);

            getDescendents(depLocator, results, collectFolderWithItem, doNotApplyFilters);
         }
      }
   }
   
   /**
    * Get locators for all immediate child folders of the supplied parent folder locator.  Only 
    * folders with Read access for the current user are returned. 
    * 
    * @param parent The parent locator, expected to reference a valid folder
    * 
    * @return The list of child folder summaries, never <code>null</code>, may be empty if there are none, or
    * if the supplied locator does not represent a valid folder.
    * 
    * @throws PSCmsException If there are any errors. 
    */
   public PSComponentSummaries getChildFolderSummaries(PSLocator parent) throws PSCmsException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      PSFolderSecurityManager.setCheckFolderPermissions(false);
      PSRelationshipSet rels;
      try
      {
         rels = processor.getDependents(FOLDER_RELATE_TYPE,
               parent, PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS
               | getFilterFlags());
      }
      finally
      {
         // enable permission checking
         PSFolderSecurityManager.setCheckFolderPermissions(true);
      }
      
      List<PSLocator> results = new ArrayList<PSLocator>();

      Iterator<PSRelationship> itRels = rels.iterator();
      while (itRels.hasNext())
      {
         PSRelationship rel = itRels.next();
         PSLocator depLocator = rel.getDependent();
         if (rel.getDependentObjectType() == PSCmsObject.TYPE_FOLDER)
         {
            results.add(depLocator);
         }
      }
      
      // get the component summaries along with the permissions
      PSComponentSummaries summaries = getComponentSummaries(
          results.iterator(), parent,
         true);

      summaries = filterByFolderPermissions(summaries,
            PSObjectPermissions.ACCESS_READ);
      
      return summaries;
   }
   
   /**
    * Determines if the specified relationship set contains any dependent which is item
    * object type {@link PSCmsObject#TYPE_ITEM}.
    * 
    * @param rels the relationship set in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the specified relationship contains an item dependent.
    */
   @SuppressWarnings("unchecked")
   private boolean hasItemDependent(PSRelationshipSet rels)
   {
      Iterator itRels = rels.iterator();
      while (itRels.hasNext())
      {
         PSRelationship rel = (PSRelationship) itRels.next();
         if (rel.getDependentObjectType() == PSCmsObject.TYPE_ITEM)
            return true;
      }
      return false;
   }
   
   /**
    * Finds all folders which matches the specified folder path. The path is a 
    * slash separated folder path using the '%' character as a wildcard.
    * 
    * @param path the path, never <code>null</code> or empty. It must contain
    *    zero or one match pattern.
    * @return zero or more guids that each identify a folder. It may be 
    *    <code>null</code> if the specified path does not contain a valid root
    *    path.
    *    
    * @throws PSCmsException if an error occurs.
    */
   public List<IPSGuid> findMatchingFolders(String path)
      throws PSCmsException
   {
      if (path == null || path.trim().length() == 0)
      {
         throw new IllegalArgumentException("path may not be null or empty");
      }
         
      String rootPath = PSFolderStringUtils.getFolderRootPathFromPattern(path);
      Pattern[] matchPatterns = PSFolderStringUtils.getFolderPatterns(path);

      // There must be only one match pattern
      if (matchPatterns.length > 1)
      {
         throw new IllegalArgumentException(
               "Invalid folder path contains a semicolon");
      }
      
      if (matchPatterns.length == 0)
      {
         return new ArrayList<IPSGuid>(); // empty
      }
      
      int rootID = getIdByPath(rootPath);
      if (rootID == -1)
         return null;  // cannot find root.
      
      PSItemSummaryCache itemCache = getItemCache();
      PSFolderRelationshipCache folderCache = PSFolderRelationshipCache.getInstance();
      if (itemCache != null && folderCache != null)
         return findMatchingFoldersFromCache(path, matchPatterns, rootID, rootPath, itemCache, folderCache);
      else
         return findMatchingFoldersFromDB(path, matchPatterns, rootID, rootPath);
   }

   /**
    * The same as the {@link #findMatchingFolders(String)}, except it will access
    * folder cache to look for the matching folders.
    * 
    * @param matchPath the matching path, assume not <code>null</code> or empty.
    * @param matchPatterns the matching pattens, assume not 
    *    <code>null</code> or empty.
    * @param rootID the root folder id.
    * @param rootPath the root path, assume not <code>null</code> or empty.
    * @param itemCache the item cache, assume not <code>null</code>.
    * @param folderCache the folder cache, assume not <code>null</code>.
    * 
    * @return zero or more guids that each identify a folder
    * 
    * @throws PSCmsException if an error occurs.
    */
   private List<IPSGuid> findMatchingFoldersFromCache(String matchPath,
         Pattern[] matchPatterns, int rootID, String rootPath, 
         PSItemSummaryCache itemCache, PSFolderRelationshipCache folderCache)
      throws PSCmsException
   {
      IPSItemEntry rootFolder = itemCache.getItem(rootID);
      if (! rootFolder.isFolder())
         return null;  // the root must be a folder

      // simply get all child folders if the patten is "//abc/foo/%"
      // and "//abc/foo" is the root path
      if (matchPath.equalsIgnoreCase(rootPath + "/%"))
      {
         IPSGuid rootGuid = new PSLegacyGuid(rootID, -1);
         List<IPSGuid> rval = folderCache.getFolderDescendants(rootGuid);
         rval.add(rootGuid);
         return rval;
      }

      // get the 1st part of the string, right before the wildcard
      // for example, if matchPath is "//abc/foo%bar%fee", then
      // the preWildCard is "//abc/foo"
      int firstWildCard = matchPath.indexOf('%');
      String preWildCard = firstWildCard > 0 ? matchPath.substring(0,
            firstWildCard) : matchPath;
      
      List<IPSGuid> rval = new ArrayList<IPSGuid>();
      String paths[] = new String[]{rootPath};
      findMatchingFoldersFromCache(preWildCard, matchPatterns, rootFolder,
            paths, rval, itemCache, folderCache);
      return rval;
   }
   
   /**
    * Process one folder, recurse into children. The current folder is
    * assumed to have been checked. The children have their paths extracted
    * and are checked before recursively calling this method. Because the
    * match pattern may have a number of segments, all folders below the root
    * much be checked.
    *
    * @param preWildCard the 1st part of the match path, right before the 1st
    *    wild card if there is one, assumed not <code>null</code> or emtpy.
    * @param matchPatterns patterns to match when looking at folder paths,
    *           assumed never <code>null</code>
    * @param folder the folder to process, assume not <code>null</code>.
    * @param paths the path of the specified folder, assume not <code>null</code>
    * @param rval the list of guids being built, assumed not <code>null</code>.
    * @param itemCache the item cache, assume not <code>null</code>.
    * @param folderCache the folder cache, assume not <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs in the folder processor
    */
   private void findMatchingFoldersFromCache(String preWildCard,
         Pattern[] matchPatterns, IPSItemEntry folder, String paths[],
         List<IPSGuid> rval, PSItemSummaryCache itemCache,
         PSFolderRelationshipCache folderCache) throws PSCmsException
   {
      if (PSFolderStringUtils.oneMatched(paths, matchPatterns))
      {
         rval.add(new PSLegacyGuid(folder.getContentId(), -1));
      }
      else
      {  
         if (! isContinueMatch(preWildCard, paths[0]))
            return;
      }

      List<Integer> children = folderCache.getChildIDs(folder.getContentId());
      for (Integer c : children)
      {
         IPSItemEntry item = itemCache.getItem(c);

         if (item.getContentTypeId() == PSFolder.FOLDER_CONTENT_TYPE_ID)
         {
            String cPaths[] = new String[] { paths[0] + "/" + item.getName() };
            findMatchingFoldersFromCache(preWildCard, matchPatterns, item,
                  cPaths, rval, itemCache, folderCache);
         }
      }
   }

   /**
    * Determines if needs to continue the matching process for the given 
    * folder path. This is used to avoid triverse down to a folder which
    * path does not event match the "preWildCard", so it will save time to 
    * match any of its descendents.
    * <p> 
    * It returns <code>true</code> for the following scenarios:
    * (1) preWildCard = "//Sites/EI", testPath = "//Sites"
    * (2) preWildCard = "//Sites/EI", testPath = "//Sites/E"
    * (3) preWildCard = "//Sites/EI", testPath = "//Sites/EI"
    * (4) preWildCard = "//Sites/EI", testPath = "//Sites/EI/Foo"
    * <p> 
    * It returns <code>false</code> for the following scenarios:
    * (1) preWildCard = "//Sites/EI", testPath = "//Folders"
    * (2) preWildCard = "//Sites/EI", testPath = "//Sites/C"
    * (3) preWildCard = "//Sites/EI", testPath = "//Sites/CI"
    * (4) preWildCard = "//Sites/EI", testPath = "//Sites/E/Foo"
    * 
    * @param preWildCard the 1st part of the original matching path, right 
    *    before the wild card. Assumed not <code>null</code> or empty.
    * @param testPath the tested folder path, assumed not <code>null</code> or
    *    empty.
    *    
    * @return <code>true</code> if continue the matching process for the
    *    specified folder; otherwise stop the process for this folder and its
    *    child folders.
    */
   private boolean isContinueMatch(String preWildCard, String testPath)
   {
      if (testPath.length() < preWildCard.length())
      {
         String s = preWildCard.substring(0, testPath.length());
         return s.equalsIgnoreCase(testPath);
      }
      else
      {
         String s = testPath.substring(0, preWildCard.length());
         return s.equalsIgnoreCase(preWildCard);
      }
   }

   /**
    * The same as the {@link #findMatchingFoldersFromCache(String, Pattern[], int, String, PSItemSummaryCache, PSFolderRelationshipCache)},
    * except this is processed without accessing folder cache.
    * 
    * @param path the path, assumed never <code>null</code> or empty
    * @param matchPatterns the to be matches pattens, assume not 
    *    <code>null</code> or empty.
    * @param rootID the root folder id.
    * @param rootPath the root path, assumed not <code>null</code> or empty.
    * 
    * @return zero or more guids that each identify a folder
    * @throws InvalidQueryException if a path is invalid or is not found
    */
   private List<IPSGuid> findMatchingFoldersFromDB(String path,
         Pattern[] matchPatterns, int rootID, String rootPath)
      throws PSCmsException
   {
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary rootFolder = cmsMgr.loadComponentSummary(rootID);
      if (! rootFolder.isFolder())
         return null;  // the root must be a folder

      List<IPSGuid> rval = new ArrayList<IPSGuid>();

      findMatchingFoldersFromDB(rootFolder, matchPatterns, rval);
      return rval;
   }

   /**
    * The same as the {@link #findMatchingFoldersFromCache(String, Pattern[], int, String, PSItemSummaryCache, PSFolderRelationshipCache)}
    * except this is processed without accessing folder cache.
    * 
    * @param folder the folder to process, assumed not <code>null</code>.
    * @param matchPatterns patterns to match when looking at folder paths,
    * assumed never <code>null</code>.
    * @param rval the list of guids being built, assumed never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs in the folder processor
    */
   private void findMatchingFoldersFromDB(PSComponentSummary folder, 
         Pattern[] matchPatterns, List<IPSGuid> rval)
      throws PSCmsException
   {
      String paths[] = getFolderPaths(folder.getCurrentLocator());

      paths = new String[]
      {(paths.length > 0 ? paths[0] : "") + "/" + folder.getName()};
      if (PSFolderStringUtils.oneMatched(paths, matchPatterns))
      {
         rval.add(new PSLegacyGuid(folder.getContentId(), -1));
      }

      PSComponentSummary[] children = getChildSummaries(folder
            .getCurrentLocator());

      for (PSComponentSummary c : children)
      {
         if (c.isFolder())
         {
            findMatchingFoldersFromDB(c, matchPatterns, rval);
         }
      }
   }
   
   
   /**
    * Return all descendent folders of the parent folder locator passed in. see
    * {@link IPSRelationshipProcessor#getDescendentsLocators(String, String, PSKey)}
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#getDescendentFolderLocators(PSLocator)}.
    */
   public PSKey[] getDescendentsLocators(@SuppressWarnings("unused")
   String type, @SuppressWarnings("unused")
   String relationshipType, PSKey parent) throws PSCmsException
   {
      if (! (parent instanceof PSLocator))
         throw new IllegalArgumentException("parent must be PSLocator type");
      
      List<PSLocator> results = new ArrayList<PSLocator>();
      getDescendents((PSLocator)parent, results, false, 0);

      return (PSKey[]) results.toArray(new PSKey[results.size()]);
   }

   /**
    * Gets all descendant folder IDs of the specified folder, where the folder
    * must contain items (object-type = TYPE_ITEM). The returned folder IDs may
    * contain the specified folder ID if it contains items.
    * 
    * @param folderId the ID of the specified folder, not <code>null</code>.
    * 
    * @return the list of folder IDs, never <code>null</code>, may be empty.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public Collection<PSLocator> getDescendantFoldersWithItems(PSLocator folderId) throws PSCmsException
   {
      List<PSLocator> results = new ArrayList<PSLocator>();
      getDescendents(folderId, results, true, PSRelationshipConfig.FILTER_TYPE_NONE);
      return results;
   }
   
   /**
    * See {@link IPSRelationshipProcessor#getChildren(String, String, PSKey)
    * interface} for description
    */
   @SuppressWarnings("deprecation")
   public PSComponentSummary[] getChildren(String type,
      String relationshipType, PSKey parent) throws PSCmsException
   {
      validateRelationshipType(relationshipType);

      validateKey(parent);

      PSComponentSummaries summaries = getFolderChildren(
              (PSLocator) parent, getFilterFlags(), true, relationshipType);
      return summaries.toArray();
   }

   /**
    * See {@link IPSRelationshipProcessor#getParents(String, String, PSKey)
    * interface} for description
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#getParentSummaries(PSLocator)}.
    */
   @SuppressWarnings("unchecked")
   public PSComponentSummary[] getParents(@SuppressWarnings("unused")
   String type, String relationshipType, PSKey locator) throws PSCmsException
   {
      validateRelationshipType(relationshipType);

      try
      {
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

         // Do not filter by folder permissions. We will do it when calling
         // filterByFolderPermissions() method. This will have the benefit of
         // loading the folder ACLs only once.
         // Otherwise relationship processor will load them and
         // getComponentSummaries() will load them again.
         PSFolderSecurityManager.setCheckFolderPermissions(false);
         List parents = null;
         try
         {
            parents = processor.getParents(FOLDER_RELATE_TYPE, locator,
               PSRelationshipConfig.FILTER_TYPE_ITEM_COMMUNITY);
         }
         finally
         {
            // enable permission checking
            PSFolderSecurityManager.setCheckFolderPermissions(true);
         }

         // get the component summaries along with the permissions
         PSComponentSummaries summaries = getComponentSummaries(
            parents.iterator(), null, true);

         summaries = filterByFolderPermissions(summaries,
            PSObjectPermissions.ACCESS_READ);

         return summaries.toArray();
      }
      catch (PSException e)
      {
         throw new PSCmsException(e);
      }
   }

   /**
    * Returns the locators for the parent folders for the specified child
    * folders.
    *
    * @param locators locators for the folders whose parent folders are to be
    * returned, assumed not <code>null</code>, may be empty
    *
    * @param donotfilterby mask to restrict the filtering of relationships based
    * on community or folder permissions (bitwise OR the FILTER_BY_xxx constants
    * defined in <code>PSRelationshipConfig</code> to restrict filtering on
    * both), should be set to <code>0</code> if filtering is to be performed.
    *
    * @return locators for the parent folders, never <code>null</code>, may
    * be empty
    *
    * @throws PSCmsException if an error occurs.
    */
   private PSKey[] getFolderParents(PSKey[] locators,
      int donotfilterby) throws PSCmsException
   {
      List parents = new ArrayList();
      PSRelationshipProcessor relation = PSRelationshipProcessor.getInstance();

      for (int i = 0; i < locators.length; i++)
      {
         parents.addAll(relation.getParents(FOLDER_RELATE_TYPE, locators[i],
            donotfilterby));
      }

      return (PSKey[]) parents.toArray(new PSKey[parents.size()]);
   }

   /**
    * Returns the locators in the specified list of locators which are for
    * folder objects. This method filters out the locators for non-folder
    * objects.
    *
    * @param locators list containing locators (<code>PSKey</code> objects)
    * for folders and items, assumed not <code>null</code>, may be empty
    *
    * @return an array of locators of folders, filters out the locators for
    * items, never <code>null</code>, may be empty
    *
    * @throws PSCmsException if any error occurs getting the component summaries
    * for the specified locators
    */
   @SuppressWarnings("unchecked")
   private PSKey[] getFolderLocators(List locators) throws PSCmsException
   {
      PSKey[] keys = new PSKey[0];
      PSItemSummaryCache cache = getItemCache();
      List folderIds = null;
      if (cache != null)
      {
         Iterator it = locators.iterator();
         PSLocator locator = null;
         folderIds = new ArrayList();
         while (it.hasNext())
         {
            locator = (PSLocator) it.next();
            if (cache.isFolderExist(locator.getId()))
               folderIds.add(locator);
         }
      }
      else
      {
         PSComponentSummaries summaries = getComponentSummaries(
            locators.iterator(), null, false);

         folderIds = summaries.getComponentLocators(
            PSComponentSummary.TYPE_FOLDER,
            PSComponentSummary.GET_CURRENT_LOCATOR);
      }

      if (!folderIds.isEmpty())
         keys = (PSKey[]) folderIds.toArray(new PSKey[folderIds.size()]);

      return keys;
   }

   /**
    * Checks if the user has permissions to delete the specified folders.
    * <p>
    * For deleting a folder, the user must have the following permissions: 1>
    * write access on the parent folder 2> admin access on the folder being
    * deleted 3> admin access on all the folders contained by the folder being
    * deleted
    * <p>
    * Returns <code>true</code> if the user has the permission to delete all
    * the specified folders, <code>false</code> otherwise. If
    * <code>throwException</code> is <code>true</code> and the permissions
    * required for deleting a folder is not met then a
    * <code>PSCmsException</code> is thrown.
    *
    *
    * @param locators locators for the folders on which the delete permissions
    * are to be verified, may not be <code>null</code>, may be empty
    *
    * @param throwException if <code>true</code> and the user does not the
    * permission to delete the folders, then a <code>PSCmsException</code> is
    * thrown, otherwise <code>false</code> is returned.
    *
    * @return <code>true</code> if the user has the permissions to delete all
    * the specified folders, <code>false</code> otherwise
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the permissions to delete
    * all the specified folders
    *
    * @throws IllegalArgumentException if <code>request</code> or
    * <code>locators</code> is <code>null</code>
    */
   private boolean checkHasDeletePermission(
      PSKey[] locators, boolean throwException) throws PSCmsException
   {
     
      if (locators == null)
         throw new IllegalArgumentException("locators may not be null");

      locators = getFolderLocators(Arrays.asList(locators));
      if (locators.length < 1)
         return true;

      // verify write access on the parent folders
      int donotfilterby = PSRelationshipConfig.FILTER_TYPE_COMMUNITY
         | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;

      PSKey[] parents = getFolderParents(locators, donotfilterby);

      boolean hasPermission = checkHasFolderPermission(parents,
         PSObjectPermissions.ACCESS_WRITE, false, throwException);

      if (hasPermission)
      {
         // verify admin access on the folder being deleted and
         // admin access on all the folders contained by the folder being
         // deleted
         hasPermission = checkHasFolderPermission(locators,
            PSObjectPermissions.ACCESS_ADMIN, true, throwException);
      }

      return hasPermission;
   }

   /**
    * Checks if the user has permissions to move the specified child folders
    * from the source to the target folder.
    * <p>
    * For moving a folder, the user must have the following permissions: 1>
    * write access on the source parent folder 2> write access on the target
    * parent folder 3> admin access on the folder being moved 4> admin access on
    * all the folders contained by the folder being moved
    * <p>
    * Returns <code>true</code> if the user has the permission to move all the
    * specified child folders from the source to the target folder,
    * <code>false</code> otherwise. If <code>throwException</code> is
    * <code>true</code> and the permissions required for moving the folders is
    * not met then a <code>PSCmsException</code> is thrown.
    *
    * @param sourceParent locator for source parent folder, may not be
    * <code>null</code>
    *
    * @param children contains the locators for the child folders to move from
    * the source to the target folder, may not be <code>null</code>, may be
    * empty
    *
    * @param targetParent locator for target parent folder, may not be
    * <code>null</code>
    *
    * @param throwException if <code>true</code> and the user does not the
    * permission to move the folders, then a <code>PSCmsException</code> is
    * thrown, otherwise <code>false</code> is returned.
    *
    * @return <code>true</code> if the user has the permissions to move all
    * the specified folders, <code>false</code> otherwise
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the permissions to move all
    * the specified folders
    *
    * @throws IllegalArgumentException if <code>request</code> or
    * <code>sourceParent</code> or <code>children</code> or
    * <code>targetParent</code> is <code>null</code>
    */
   private boolean checkHasMovePermission(
      PSKey sourceParent, List children, PSKey targetParent,
      boolean throwException) throws PSCmsException
   {
    
      if (sourceParent == null)
         throw new IllegalArgumentException("sourceParent may not be null");
      if (children == null)
         throw new IllegalArgumentException("children may not be null");
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent may not be null");

      PSKey[] locators = getFolderLocators(children);
      if (locators.length < 1)
         return true;

      // verify write access on the source parent folder
      boolean hasPermission = checkHasFolderPermission(
         (PSLocator) sourceParent, PSObjectPermissions.ACCESS_WRITE, false,
         throwException);

      if (!hasPermission)
         return false;

      // verify write access on the target parent folder
      hasPermission = checkHasFolderPermission(
         (PSLocator) targetParent, PSObjectPermissions.ACCESS_WRITE, false,
         throwException);

      if (!hasPermission)
         return false;

      if (hasPermission)
      {
         // verify admin access on the folder being moved and
         // admin access on all the folders contained by the folder being moved
         hasPermission = checkHasFolderPermission(locators,
            PSObjectPermissions.ACCESS_ADMIN, true, throwException);
      }

      return hasPermission;
   }

   /**
    * Checks if the user has permissions to copy the specified list of folders
    * to the target folder.
    * <p>
    * For copying a folder, the user must have the following permissions: 1>
    * write access on the target parent folder 2> read access on the folder
    * being copied 4> read access on all the folders contained by the folder
    * being copied
    * <p>
    * Returns <code>true</code> if the user has the permission to copy all the
    * specified folders to the target folder, <code>false</code> otherwise. If
    * <code>throwException</code> is <code>true</code> and the permissions
    * required for copying the folders is not met then a
    * <code>PSCmsException</code> is thrown.
    *
    * @param children contains the locators for the olders to move to the target
    * folder, may not be <code>null</code>, may be empty
    *
    * @param targetParent locator for target parent folder, may not be
    * <code>null</code>
    *
    * @param throwException if <code>true</code> and the user does not the
    * permission to copy the folders, then a <code>PSCmsException</code> is
    * thrown, otherwise <code>false</code> is returned.
    *
    * @return <code>true</code> if the user has the permissions to copy all
    * the specified folders, <code>false</code> otherwise
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the permissions to copy all
    * the specified folders
    *
    * @throws IllegalArgumentException if <code>request</code> or or
    * <code>children</code> or <code>targetParent</code> is
    * <code>null</code>
    */
   private boolean checkHasCopyPermission(
      List children, PSKey targetParent, boolean throwException)
      throws PSCmsException
   {
      if (children == null)
         throw new IllegalArgumentException("children may not be null");
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent may not be null");

      // verify write access on the target parent folder
      boolean hasPermission = checkHasFolderPermission(
         (PSLocator) targetParent, PSObjectPermissions.ACCESS_WRITE, false,
         throwException);

      if (!hasPermission)
         return false;

      PSKey[] locators = getFolderLocators(children);
      if (locators.length < 1)
         return true;

      // read access on the folder being copied and
      // read access on all the folders contained by the folder being copied
      hasPermission = checkHasFolderPermission(locators,
         PSObjectPermissions.ACCESS_READ, true, throwException);

      return hasPermission;
   }

   /**
    * Checks if the user has permissions to save the specified folder objects.
    *
    * @param comps array of folder (<code>PSFolder</code>) objects, assumed
    * not <code>null</code>, may be empty
    *
    * @param throwException if <code>true</code> and the user does not the
    * permission to save the folders, then a <code>PSCmsException</code> is
    * thrown, otherwise <code>false</code> is returned.
    *
    * @return <code>true</code> if the user has the permissions to save all
    * the specified folders, <code>false</code> otherwise
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the permissions to save all
    * the specified folders
    *
    * @throws IllegalArgumentException if <code>request</code> or or
    * <code>components</code> is <code>null</code>
    */
   private boolean checkHasSavePermission(
      IPSDbComponent[] comps, boolean throwException) throws PSCmsException
   {
      if (comps == null)
         throw new IllegalArgumentException("components may not be null");

      boolean hasPermission = true;
      for (int i = 0; i < comps.length; i++)
      {
         PSFolder folder = (PSFolder) comps[i];
         PSLocator locator = folder.getLocator();
         if (locator.isPersisted())
         {
            // updating a folder. Must have admin permission
            hasPermission = checkHasFolderPermission(folder
               .getLocator(), PSObjectPermissions.ACCESS_ADMIN, false,
               throwException);

            // if throwException is true then checkHasFolderPermission
            // will throw PSCmsException, so here just return false if
            // checkHasFolderPermission returned false
            if (!hasPermission)
               break;
         }
         else
         {
            // creating a new folder, must have read access to the folder being
            // created, otherwise trying to add it as a child of the parent
            // folder will fail, resulting in an orphaned folder.
            PSFolderAcl folderAcl = new PSFolderAcl(locator.getId(), folder
               .getCommunityId());

            Iterator it = folder.getAcl().iterator();
            while (it.hasNext())
            {
               PSObjectAclEntry aclEntry = (PSObjectAclEntry) it.next();
               if (aclEntry != null)
                  folderAcl.add(aclEntry);
            }

            try
            {
               PSFolderPermissions folderPerms = new PSFolderPermissions(
                  folderAcl);
               hasPermission = folderPerms.hasReadAccess();
               if ((!hasPermission) && throwException)
                  throw new PSCmsException(IPSCmsErrors.FOLDER_CREATE_ERROR);
            }
            catch (PSAuthorizationException ex)
            {
               throw new PSCmsException(ex.getErrorCode(), ex
                  .getErrorArguments());
            }
         }
      }
      return hasPermission;
   }

   /**
    * Checks if the user has the permission specified by
    * <code>accessLevel</code> on all the folders specfied by the
    * <code>locators</code> array. The permissions are verified recursively if
    * <code>recursive</code> is <code>true</code>, that is for all the
    * child folders of a given folder. If on any folder the user does not have
    * the specified permission then this method returns <code>false</code> if
    * <code>throwException</code> is false, otherwise a
    * <code>PSCmsException</code> is thrown.
    *
    * @param locators specifies the folders on which the permissions are to be
    * checked, may not be <code>null</code>, may be empty
    *
    * @param accessLevel the access level to be checked for on each folder,
    * should be non-negative, assumed valid access level
    *
    * @param recursive <code>true</code> if the permissions on all the
    * subfolders of the specified folders is to be verified, <code>false</code>
    * otherwise
    *
    * @param throwException if <code>true</code> then a
    * <code>PSCmsException</code> is thrown if the user does not have the
    * specified permission on any folder, otherwise <code>false</code> is
    * returned.
    *
    * @return <code>true</code> if the user has the specifed permission on all
    * the folders (and their subfolders), otherwise <code>false</code>
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the specified permission on
    * any folder
    *
    * @throw IllegalArgumentException if <code>request</code> or
    * <code>locators</code> is <code>null</code> or <code>accessLevel</code>
    * is invalid
    */
   private boolean checkHasFolderPermission(
      PSKey[] locators, int accessLevel, boolean recursive,
      boolean throwException) throws PSCmsException
   {
      if (locators == null)
         throw new IllegalArgumentException("locators may not be null");

      boolean hasPermission = true;
      for (int i = 0; i < locators.length; i++)
      {
         hasPermission = checkHasFolderPermission(
            (PSLocator) locators[i], accessLevel, recursive, throwException);

         // if throwException is true then checkHasFolderPermission
         // will throw PSCmsException, so here just return false if
         // checkHasFolderPermission returned false
         if (!hasPermission)
            break;
      }
      return hasPermission;
   }

   /**
    * Same as {@link #checkHasFolderPermission(PSLocator, int, boolean, boolean)} except that it verifies the permissions on a
    * single folder. This first checks the permission on the specified folder.
    * If the user has the specified permission, then it obtains all the child
    * folders if <code>recursive</code> is <code>true</code> and calls this
    * method itself for each child folder to verify the permissions on the child
    * folders. If the user does not have the specified permission, then it
    * returns <code>false</code> if <code>throwException</code> if
    * <code>false</code> otherwise it throws a <code>PSCmsException</code>
    */
   public boolean checkHasFolderPermission(PSLocator locator, int accessLevel, boolean recursive,
      boolean throwException) throws PSCmsException
   {
     
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      // check the user's permission on the specified folder
      PSObjectPermissions perm = null;

      PSItemSummaryCache cache = getItemCache();
      if (cache != null)
      {
            if (!cache.isItemExist(locator.getId()))
               log.error("Missing expected item in the cache. The item id was: {}",
                       locator.getId());


         // get the permission from the cache
         if (!cache.isFolderExist(locator.getId()))
            return true; // the locator is not a folder

         int[] ids = new int[1];
         ids[0] = locator.getId();
         PSFolderAcl[] acls = getFolderAcls(ids);

         if (acls.length != 0)
         {
            try
            {
               perm = new PSFolderPermissions(acls[0]);
            }
            catch (PSAuthorizationException e)
            {
               throw new PSCmsException(e);
            }
         }
      }
      else
      {
         // get the permission from the database
         List list = new ArrayList();
         list.add(locator);

         // get the component summaries along with permissions
         PSComponentSummaries summaries = getComponentSummaries( list
            .iterator(), null, true);

         list = summaries.getComponentList(PSComponentSummary.TYPE_FOLDER);
         if (list.isEmpty())
            return true; // the locator is not a folder

         Iterator it = list.iterator();
         PSComponentSummary summary = (PSComponentSummary) it.next();
         perm = summary.getPermissions();
      }

      if (perm != null && !perm.hasAccess(accessLevel))
      {
         if (!throwException)
            return false;

         throw new PSCmsException(IPSCmsErrors.FOLDER_PERMISSION_DENIED);
      }

      if (recursive)
      {
         // get all the folders, the folders list should not be filtered
         // by community or folder permissions
         int donotfilterby = PSRelationshipConfig.FILTER_TYPE_COMMUNITY
            | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;

         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

         List children = processor.getDependentLocators(FOLDER_RELATE_TYPE,
            locator, donotfilterby);
         Iterator folderChilds = children.iterator();

         while (folderChilds.hasNext())
         {
            PSLocator childLocator = (PSLocator) folderChilds.next();
            boolean hasPermission = checkHasFolderPermission(
               childLocator, accessLevel, recursive, throwException);
            if (!hasPermission)
               return false;
         }
      }

      return true;
   }

   /**
    * Checks if the user has the permission specified by
    * <code>permissions</code> on the folder specfied by <code>folder</code>
    * object. If on the folder the user does not have the specified permission
    * then this method returns <code>false</code> if
    * <code>throwException</code> is false, otherwise a
    * <code>PSCmsException</code> is thrown.
    *
    * @param folder the folder object on which the permissions should be
    * checked, assumed not <code>null</code>
    *
    * @param permissions the access level to be checked for the folder, should
    * be non-negative, assumed valid access level
    *
    * @param throwException <code>true</code> if an exception should be thrown
    * if the user does not have the specified permission, <code>false</code>
    * otherwise
    *
    * @return <code>true</code> if the user has the specified permission on
    * the folder, <code>false</code> otherwise
    *
    * @throws PSCmsException if <code>throwException</code> is
    * <code>true</code> and the user does not have the specified permission
    */
   private boolean checkHasFolderPermission(PSFolder folder, int permissions,
      boolean throwException) throws PSCmsException
   {
      boolean hasPerm = true;
      PSObjectPermissions perm = folder.getPermissions();
      if (!perm.hasAccess(permissions))
      {
         if (throwException)
            throw new PSCmsException(IPSCmsErrors.FOLDER_PERMISSION_DENIED);

         hasPerm = false;
      }

      return hasPerm;
   }

   /**
    * Validates a given relationship type.
    *
    * @param relationshipType The to be validated relationship type, it may not
    * be <code>null</code> or empty. It must be
    * <code>FOLDER_RELATE_TYPE</code>.
    *
    * @throws IllegalArgumentException if it is invalid.
    */
   private void validateRelationshipType(String relationshipType)
   {
      if (relationshipType == null || relationshipType.trim().length() == 0)
      {
         throw new IllegalArgumentException(
            "relationshipType may not be null or empty");
      }

      if (!relationshipType.equalsIgnoreCase(FOLDER_RELATE_TYPE) && !relationshipType.equalsIgnoreCase(RECYCLED_RELATE_TYPE))
      {
         throw new IllegalArgumentException("relationshipType must be \""
            + FOLDER_RELATE_TYPE + "\" or \"" + RECYCLED_RELATE_TYPE + "\"");
      }
   }

   // see interface for description
   @Override
   @SuppressWarnings("unused")
   public int[] allocateIds(String lookup, int count) throws PSCmsException
   {
      throw new IllegalStateException(
         "allocateId(String, int) is not supported");
   }

   // see interface for description
   @SuppressWarnings("unused")
   @Override
   public int allocateId(String lookup) throws PSCmsException
   {
      throw new IllegalStateException("allocateId(String) is not supported");
   }

   // see interface for description
   @Override
   public void setNextAllocationSize(@SuppressWarnings("unused")
   int count)
   {
      throw new IllegalStateException(
         "setNextAllocationSize(int) is not supported");
   }

   /**
    * Determines whether to ignore the revisions for all folder locators.
    *
    * @return <code>true</code> if not ignore revision for folder locators;
    * otherwise return <code>false</code>.
    */
   private boolean isRevisionable()
   {
      return m_folderObject.isRevisionable();
   }

   /**
    * @see IPSRelationshipProcessor#getSummaryByPath(String, String, String)
    *
    * @deprecated Use {@link PSFolderProcessorProxy#getSummary(String)}.
    */
   public PSComponentSummary getSummaryByPath(@SuppressWarnings("unused")
   String componentType, String path, String relationshipTypeName)
      throws PSCmsException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      return processor.getSummaryByPath(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, path,
         relationshipTypeName);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#delete(
    * com.percussion.cms.objectstore.PSKey, java.util.List)
    */
   @Transactional(propagation = Propagation.REQUIRES_NEW)
   public void delete(PSKey sourceParent, List children, String relationshipTypeName) throws PSCmsException
   {
      delete(relationshipTypeName, sourceParent, children);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getSummaries(java.lang.String, java.lang.String,
    * com.percussion.cms.objectstore.PSKey, boolean)
    */
   public PSComponentSummaries getSummaries(String componentType,
      String relationshipType, PSKey locator, boolean owner)
      throws PSCmsException
   {
      validateComponentType(componentType);

      return getSummaries(relationshipType, (PSLocator) locator, owner);
   }

   /**
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#add(
    * java.lang.String, java.util.List,
    * com.percussion.design.objectstore.PSLocator)
    *
    * @deprecated Use
    * {@link PSFolderProcessorProxy#addChildren(List, PSLocator)}.
    */
   public void add(String relationshipType, List children,
      PSLocator targetParent) throws PSCmsException
   {
      validateRelationshipType(relationshipType);
      add(FOLDER_PROXY_TYPE, children, (PSKey) targetParent);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(java.lang.String,
    * com.percussion.design.objectstore.PSLocator, boolean)
    */
   public PSRelationshipSet getRelationships(String relationshipType,
      PSLocator locator, boolean owner) throws PSCmsException
   {
      PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
      return proc.getRelationships(relationshipType, locator, owner);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getSummaries(
    * java.lang.String, com.percussion.design.objectstore.PSLocator, boolean)
    */
   public PSComponentSummaries getSummaries(String relationshipType,
      PSLocator locator, boolean owner) throws PSCmsException
   {
      validateRelationshipType(relationshipType);

      PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
      PSRelationshipSet rels = proc.getRelationships(relationshipType, locator,
         owner);
      return getSummaries(rels.iterator(), owner);
   }

   /**
    * Helper method to get summaries for owner or child of each relationship in
    * the relationship list.
    *
    * @param relationships iterator of {@link com.percussion.design.objectstore.PSRelationship relationship} objects, must not be
    * <code>null</code>.
    * @param owner specify <code>true</code> if the summaries required is for
    * owners of the relationships, <code>false</code> if for dependents.
    * @return summaries for all the owners/children of the relationships
    * supplied.
    * @throws PSCmsException if it could not fetch the summaries for any reason.
    */
   @SuppressWarnings("unchecked")
   private PSComponentSummaries getSummaries(Iterator relationships,
      boolean owner) throws PSCmsException
   {
      List locators = new ArrayList();
      while (relationships.hasNext())
      {
         PSRelationship element = (PSRelationship) relationships.next();
         PSLocator temp = null;
         if (owner)
            temp = element.getOwner();
         else
            temp = element.getDependent();
         locators.add(temp);
      }
      return getComponentSummaries( locators.iterator(), null);
   }

   public void move(@SuppressWarnings("unused")
   String relationshipType, PSLocator sourceParent, List children,
      PSLocator targetParent) throws PSCmsException
   {
      move(PSDbComponent.getComponentType(PSFolder.class),
         (PSKey) sourceParent, children, (PSKey) targetParent);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#
    * getRelationships(com.percussion.cms.objectstore.PSRelationshipFilter)
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter)
      throws PSCmsException
   {
      return PSRelationshipProcessor.getInstance().getRelationships(filter);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getSummaries(
    * com.percussion.cms.objectstore.PSRelationshipFilter, boolean)
    */
   public PSComponentSummaries getSummaries(PSRelationshipFilter filter,
      boolean owner) throws PSCmsException
   {
      PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
      PSRelationshipSet relationships = proc.getRelationships(filter);
      return getSummaries(relationships.iterator(), owner);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#save(
    * com.percussion.design.objectstore.PSRelationshipSet)
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException
   {
      PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();
      proc.save(relationships);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#remove(
    * com.percussion.design.objectstore.PSRelationshipSet)
    */
   public void delete(PSRelationshipSet relationships) throws PSCmsException
   {
      PSRelationshipProcessor.getInstance().delete(relationships);
   }

   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#getConfig(
    * java.lang.String)
    */
   public PSRelationshipConfig getConfig(String relationshipTypeName)
      throws PSCmsException
   {
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      return processor.getConfig(relationshipTypeName);
   }

   /**
    * Implements {@link IPSRelationshipProcessor#getRelationshipOwnerPaths(
    * String, PSLocator, String)} method similar to that implemented by {@link
    * PSRelationshipProcessor#getRelationshipOwnerPaths(String, PSLocator,
    * String)} with the following differences.
    * <ol>
    * <li>The relationship type name accepted is only
    * {@link #FOLDER_RELATE_TYPE Folder Content}</li>
    * <li>Each path in the result generated is modified to start with "//"
    * instead of "/Root". This is to be compataible with the path generation
    * scheme used everywhere.</li>
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor
    * #getRelationshipOwnerPaths(java.lang.String, com.percussion.design.
    * objectstore.PSLocator, java.lang.String)
    *
    * @deprecated Use {@link PSFolderProcessorProxy#getFolderPaths(PSLocator)}.
    */
   public String[] getRelationshipOwnerPaths(String componentType,
      PSLocator locator, String relationshipTypeName) throws PSCmsException
   {
      validateComponentType(componentType);
      validateRelationshipType(relationshipTypeName);

      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      String[] paths = processor.getRelationshipOwnerPaths(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, locator,
         relationshipTypeName);
      String path = null;
      String result[] = new String[paths.length];
      for (int i = 0; i < paths.length; i++)
      {
         path = paths[i];
         // For folder paths we always replace the hidden Root with "/"
         if (path.startsWith(ROOT_PATH_START))
            path = "/" + path.substring(ROOT_PATH_START.length());

         result[i] = path;
      }
      return result;
   }

   /**
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSRelationshipProcessor#isDescendent(
    * String, PSLocator, PSLocator, String)
    */
   public boolean isDescendent(String componentType, PSLocator parent,
      PSLocator child, String relationshipTypeName) throws PSCmsException
   {
      validateComponentType(componentType);

      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
      return processor.isDescendent(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, parent, child,
         relationshipTypeName);
   }

   // see IPSFolderProcessor
   public void addChildren(List children, PSLocator targetFolderId)
      throws PSCmsException
   {
      add(FOLDER_RELATE_TYPE, children, targetFolderId);
   }

   // see IPSFolderProcessor
   public PSComponentSummary[] getChildSummaries(PSLocator folderId)
      throws PSCmsException
   {
      return getChildSummaries(folderId, FOLDER_RELATE_TYPE);
   }

   // see IPSFolderProcessor
   public PSComponentSummary[] getChildSummaries(PSLocator folderId, String relationshipTypeName)
           throws PSCmsException {
      return getChildren(PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
              relationshipTypeName,
              folderId);
   }

   // see IPSFolderProcessor
   public void copyChildren(List children, PSLocator targetFolderId)
      throws PSCmsException
   {
      copy(FOLDER_RELATE_TYPE, children, targetFolderId);
   }

   // see IPSFolderProcessor
   @SuppressWarnings("unchecked")
   public void removeChildren(PSLocator sourceFolderId, List children,
      boolean force) throws PSCmsException, PSNotFoundException {
      validateKey(sourceFolderId);
      validateKeys(children.iterator());

      PSCrossSiteFolderActionProcessor proc = new PSCrossSiteFolderRemoveActionProcessor(sourceFolderId, children);

      /*
       * If we have many items to process we will the JMS Queue.
       */
      boolean queue = proc.getDependentItems().size() >= queueThreshold();

      if ( ! queue )
         proc.processLinks();

      // Actual action
      PSCmsException ex = null;
      try
      {
         delete(sourceFolderId, children, FOLDER_RELATE_TYPE);
      }
      catch (PSCmsException e)
      {
         getLogger().debug("An unexpected exception occurred while deleting folder children.",e);
         ex = e;
      }

      // Save the links
      try
      {
         if (ex != null)
         {
            getLogger().debug(
               "Action failed. Will save cross site "
                  + "relationships for successful items.");
         }
         if ( ! queue ) {
            proc.saveLinks();
         }
         else {
            PSMessageQueueServiceLocator.getMessageQueueService().sendMessage(proc.getData(), null);
         }
      }
      catch (PSCmsException e)
      {
         getLogger().debug("An unexpected error occurred while saving links.", e);
         PSCmsException eNew = null;
         if (ex != null)
         {
            // both action failed and save failed, let us report that
            eNew = new PSCmsException(
               IPSCmsErrors.CROSSSITE_LINK_PROCESS_MULTI_ERROR, new String[]
               {
                  ex.getLocalizedMessage(), e.getLocalizedMessage()
               });
         }
         else
         {
            eNew = new PSCmsException(IPSCmsErrors.ERROR_SAVING_RELATIONSHIPS,
               e.getLocalizedMessage());
         }
         throw eNew;
      }
      finally
      {
         Logger logger = getLogger();
         if (logger.isDebugEnabled())
         {
            getLogger().debug(
               PSXmlDocumentBuilder.toString(proc.getProcessReport()));
         }
      }
      if (ex != null)
         throw ex;
   }

   // see IPSFolderProcessor
   @SuppressWarnings("unchecked")
   public void removeChildren(PSLocator sourceFolderId, List children)
           throws PSCmsException, PSNotFoundException {
      removeChildren(sourceFolderId, children, false);
   }

   
   /**
    * Gets the cross site link queue threshold to determine
    * when the number of items is exceeded to use the queue.
    * @return the queue threshold <code>0</code> means the queue will always be used.
    */
   private static int queueThreshold()
   {
      if (PSServer.getServerProps() == null) return Integer.MAX_VALUE;
      String prop = PSServer.getServerProps().getProperty(CROSS_SITE_LINK_QUEUE_THRESHOLD, "50");
      Integer p = Integer.parseInt(prop);
      if (p < 0) return Integer.MAX_VALUE;
      return p;
   }
   
   // see IPSFolderProcessor
   @SuppressWarnings("unchecked")
   public void moveChildren(PSLocator sourceFolderId, List children,
      PSLocator targetFolderId, boolean force) throws PSCmsException
   {
      moveFolderChildren(sourceFolderId, children, targetFolderId, force, true);
   }

   /**
    * Moves specified folder children from a source folder to a target folder.
    * 
    * @param sourceFolderId the ID of the source folder, not <code>null</code>.
    * @param children a list of to be moved child items in source folder, not
    * <code>null</code>.
    * @param targetFolderId the ID of the target folder, not <code>null</code>.
    * @param force <code>true</code> to force the action to modify the active
    * assembly relations in which this item is dependent and the relationships
    * are with non empty siteid and/or folderid properties. <code>false</code>
    * not force action and throw error in such situation.
    * @param checkFolderPermission if <code>true</code>, then validates
    * the folder permissions before moving the child items; otherwise ignore
    * the folder permissions during the move operation. 
    * 
    * @throws PSCmsException if an error occurs.
    */
   public void moveFolderChildren(PSLocator sourceFolderId, List children,
         PSLocator targetFolderId, boolean force, boolean checkFolderPermission)
         throws PSCmsException
   {

      validateKeys(children.iterator());
      validateKey(sourceFolderId);
      validateKey(targetFolderId);

      PSCrossSiteFolderActionProcessor proc=null;

      try {
         proc =
                 new PSCrossSiteFolderMoveActionProcessor(sourceFolderId, children, targetFolderId);
      } catch (PSNotFoundException e) {
         throw new PSCmsException(e);
      }
      /*
       * If we have many items to process we will the JMS Queue.
       */
      boolean queue = proc.getDependentItems().size() >= queueThreshold();
      
      if ( ! queue )
         proc.processLinks();

      PSCmsException ex = null;
      try
      {
         moveFolderChildren(sourceFolderId, children, targetFolderId,
               checkFolderPermission);
      }
      catch (PSCmsException e)
      {
         getLogger().debug("An unexpected exception occurred while moving folder children.",e);
         ex = e;
      }

      // Save the links
      try
      {
         if (ex != null)
         {
            getLogger().debug(
               "Action failed. Will save cross site "
                  + "relationships for successful items.");
         }
         if ( ! queue ) {
            proc.saveLinks();
         }
         else {
            PSMessageQueueServiceLocator.getMessageQueueService().sendMessage(proc.getData(), null);
         }
      }
      catch (PSCmsException e)
      {
         getLogger().debug("An unexpected exception occurred while saving links.",e);
         PSCmsException eNew = null;
         if (ex != null)
         {
            // both action failed and save failed, let us report that
            eNew = new PSCmsException(
               IPSCmsErrors.CROSSSITE_LINK_PROCESS_MULTI_ERROR, new String[]
               {
                  ex.getLocalizedMessage(), e.getLocalizedMessage()
               });
         }
         else
         {
            eNew = new PSCmsException(IPSCmsErrors.ERROR_SAVING_RELATIONSHIPS,
               e.getLocalizedMessage());
         }
         throw eNew;
      }
      finally
      {
         Logger logger = getLogger();
         if (logger.isDebugEnabled())
         {
            getLogger().debug(
               PSXmlDocumentBuilder.toString(proc.getProcessReport()));
         }
      }
      if (ex != null)
         throw ex;
   }

   // see IPSFolderProcessor
   @SuppressWarnings("unchecked")
   public void moveChildren(PSLocator sourceFolderId, List children,
      PSLocator targetFolderId) throws PSCmsException
   {
      moveChildren(sourceFolderId, children, targetFolderId, false);
   }

   // see IPSFolderProcessor
   public PSComponentSummary getSummary(String path) throws PSCmsException
   {
      return getSummaryByPath(PSDbComponent.getComponentType(PSFolder.class),
         path, FOLDER_RELATE_TYPE);
   }

   // see IPSFolderProcessor
   public PSLocator[] getDescendentFolderLocators(PSLocator folderId)
      throws PSCmsException
   {
      PSKey[] keys = getDescendentsLocators(PSDbComponent
         .getComponentType(PSFolder.class),
         FOLDER_RELATE_TYPE, folderId);
      PSLocator[] results = new PSLocator[keys.length];
      System.arraycopy(keys, 0, results, 0, keys.length);
      return results;
   }
   
   // see IPSFolderProcessor
   public PSLocator[] getDescendentFolderLocatorsWithoutFilter(PSLocator folderId)
      throws PSCmsException
   {
      List<PSLocator> results = new ArrayList<PSLocator>();
      getDescendents(folderId, results, false, PSRelationshipConfig.FILTER_TYPE_NONE);

      return results.toArray(new PSLocator[results.size()]);
   }
   
   /*
    * (non-Javadoc)
    *
    * @see com.percussion.cms.objectstore.IPSFolderProcessor#getAncestorLocators(com.percussion.design.objectstore.PSLocator)
    */
   public List<PSLocator> getAncestorLocators(PSLocator folderId)
      throws PSCmsException
   {
      return getDbProcessor().getOwnerLocators(folderId, FOLDER_RELATE_TYPE);
   }

   /**
    * Get the content id from the supplied path list names.
    *
    * @param path fully qualified relationship path as explained in
    * {@link #getFolderLocatorPaths(PSLocator)} must not be <code>null</code>
    * or empty.
    *
    * @return contentid of the dependent item, -1 if the specified there is no
    * such path exist.
    *
    * @throws PSCmsException if an error occurs.
    */
   public int getIdByPath(String path) throws PSCmsException
   {
      return getIdByPath(path, FOLDER_RELATE_TYPE);
   }

   /**
    * Get the content id from the supplied path list names.
    *
    * @param path fully qualified relationship path as explained in
    * {@link #getFolderLocatorPaths(PSLocator)} must not be <code>null</code>
    * or empty.
    *
    * @return contentid of the dependent item, -1 if the specified there is no
    * such path exist.
    *
    * @throws PSCmsException if an error occurs.
    */
   public int getIdByPath(String path, String relationshipTypeName) throws PSCmsException
   {
      return getDbProcessor().getIdByPath(path, relationshipTypeName);
   }

   // see IPSFolderProcessor
   public PSComponentSummary[] getParentSummaries(PSLocator objectId)
      throws PSCmsException
   {
      return getParents(PSDbComponent.getComponentType(PSFolder.class),
         FOLDER_RELATE_TYPE, objectId);
   }

   // see IPSFolderProcessor interface
   @Override
   public String[] getFolderPaths(PSLocator objectId) throws PSCmsException
   {
      return getFolderPaths(objectId, FOLDER_RELATE_TYPE);
   }

   @Override
    public String[] getFolderPaths(PSLocator objectId, String relationshipTypeName) throws PSCmsException
    {
        if (null == objectId)
        {
            throw new IllegalArgumentException("object id cannot be null");
        }
        return getRelationshipOwnerPaths(PSDbComponent
                        .getComponentType(PSFolder.class), objectId,
                relationshipTypeName);
    }

   /**
    * For the object specified by the given locator, find the folder paths and
    * include the item's name in the path.
    *
    * @param locator the locator of the item, never <code>null</code>
    * @return an array of paths, may be empty
    * @throws PSCmsException If path lookup fails for any reason.
    */
   public String[] getItemPaths(PSLocator locator) throws PSCmsException, PSNotFoundException {
      String folderPaths[] = getFolderPaths(locator);
      String itemPaths[] = new String[folderPaths.length];
      
      // get the name (sys_title) of the item/folder
      String name = null;
      PSItemSummaryCache cache = getItemCache();
      if (cache != null)
      {
         IPSItemEntry entry = getItemCache().getItem(locator.getId());
         if (entry == null)
         {
            throw new PSNotFoundException("Item not in cache: " + locator);
         }
         name = entry.getName();
      }
      else
      {
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary sum = cms.loadComponentSummary(locator.getId());

         if (sum == null)
         {
            throw new PSNotFoundException(
                  "Cannot find component summary for locator: " + locator);
         }
         name = sum.getName();
      }
      
      // create the full path from the name
      for (int i = 0; i < folderPaths.length; i++)
      {
         itemPaths[i] = folderPaths[i] + "/" + name;
      }

      return itemPaths;
   }

   /**
    * The same as {@link #copyFolder(PSLocator, PSLocator, PSCloningOptions)}, except 
    * here the current logged in user is considered the author of the new items. 
    * 
    * @param sourceFolderId the site folder or site subfolder locator which needs to be
    *           cloned, not <code>null</code>.
    * @param targetFolderId the folder parent locator into which to clone the source,
    *           not <code>null</code>.
    * @param options the cloning options, not <code>null</code>.
    * @return the name of the log file if there were errors, <code>null</code>
    *         otherwise.
    * @throws PSCmsException for any error.
    * @todo ph: PSCloningOptions includes navigation options which don't belong
    * here.
    */
   public String copyFolderAuthorUser(PSLocator sourceFolderId, PSLocator targetFolderId,
         PSCloningOptions options) throws PSCmsException
   {
      // call the convenience method and use the logged in user
      return copyFolder(sourceFolderId, targetFolderId, options, false);
   }
   
   // see IPSFolderProcessor
    @Transactional
   public String copyFolder(PSLocator source, PSLocator target,
      PSCloningOptions options) throws PSCmsException
   {
      // call the convenience method and use the internal user
      return copyFolder(source, target, options, true);
   }

   /**
    * The same as copyFolder(PSLocator, PSLocator, PSCloningOptions) method, 
    * but depending on the useInternalUser parameter, the author of the new items
    * will be different. 
    * 
    * @param source the site folder or site subfolder locator which needs to be
    *           cloned, not <code>null</code>.
    * @param target the folder parent locator into which to clone the source,
    *           not <code>null</code>.
    * @param options the cloning options, not <code>null</code>.
    * @return the name of the log file if there were errors, <code>null</code>
    *         otherwise.
    * @param useInternalUser If <code>true</code>, the author is the internal user. 
    * If <code>false</code>, the author is the logged in user. 
    * @throws PSCmsException for any error.
    */
   @Transactional
   public String copyFolder(PSLocator source, PSLocator target,
      PSCloningOptions options, boolean useInternalUser) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (target == null)
         throw new IllegalArgumentException("target cannot be null");

      if (options == null)
         throw new IllegalArgumentException("options cannot be null");

      PSRequest request = null;
      IPSRequestContext context = null;
      String logFileName = null;
      try
      {
         Logger logger = getLogger();

         // initialize over all success
         setHadErrors(false);
         logFileName = logger.getName();

         // see if managed navigation is used
         try
         {
            // managed navigation is never used if only folders are copied
            boolean isManagedNavUsed = false;
            if (options.isNavigationContent() || options.isAllContent())
               isManagedNavUsed = PSNavConfig.isManagedNavUsed(source);

            if (isManagedNavUsed)
               logger.info("Managed Navigation is used.");
            else
               logger.info("Managed Navigation is not used.");
            m_isManagedNavUsed.set(isManagedNavUsed ? Boolean.TRUE
               : Boolean.FALSE);
         }
         catch (PSCmsException e)
         {
            logger.error("Managed Navigation is not configured correct. "
               + "You can't copy a folder until you fixed the problem.");
            logger.error("The error was: " + e.getLocalizedMessage());
            setHadErrors(true);
            return logFileName;
         }

         // initializer the relationship tracker
         PSRelationshipTracker tracker = new PSRelationshipTracker();
         m_tracker.set(tracker);

         // start the logging with the cloning option summary
         if (options.isCloneSite())
         {
            logger.info("Start Site copy...");
            logger.info("New Site name: " + options.getSiteName());
            logger.info("New Site Folder name: " + options.getFolderName());
         }
         else
         {
            logger.info("Start Site Subfolder cloning...");
            logger.info("New Site Subolder name: " + options.getFolderName());
         }
         logger.info("Selected copy option: "
            + PSCloningOptions.ms_copyOptionNames[options.getCopyOption()]);
         logger.info("Selected copy content option: "
            + PSCloningOptions.ms_copyContentOptionNames[options
               .getCopyContentOption()]);

         logger.info("Community mappings:");
         Map communityMappings = options.getCommunityMappings();
         Iterator sourceCommunities = communityMappings.keySet().iterator();
         while (sourceCommunities.hasNext())
         {
            Integer sourceCommunity = (Integer) sourceCommunities.next();
            Integer targetCommunity = (Integer) communityMappings
               .get(sourceCommunity);
            if (sourceCommunity == null || targetCommunity == null)
               break;

            logger.info("Source: " + sourceCommunity.toString()
               + "--> Target: " + targetCommunity.toString());
         }

         logger.info("Site mappings:");
         Map siteMappings = options.getSiteMappings();
         Iterator sourceSites = siteMappings.keySet().iterator();
         while (sourceSites.hasNext())
         {
            Integer sourceSite = (Integer) sourceSites.next();
            Integer targetSite = (Integer) siteMappings.get(sourceSite);
            if (sourceSite == null || targetSite == null)
               break;

            logger.info("Source: " + sourceSite.toString() + "--> Target: "
               + targetSite.toString());
         }

         List children = new ArrayList();
         PSLocatorWithName newSource = new PSLocatorWithName(source.getId(),
            source.getRevision(), options.getFolderName());
         children.add(newSource);

         checkHasCopyPermission(children, target, true);

         request = PSThreadRequestUtils.changeToInternalRequest(useInternalUser);
         
         try
         {
            // disable permission checking
            PSFolderSecurityManager.setCheckFolderPermissions(false);

            // disable the nav folder effect
            request.setParameter(
               IPSHtmlParameters.RXS_DISABLE_NAV_FOLDER_EFFECT, "y");

            PSComponentSummaries childSummaries = getComponentSummaries(
               children.iterator(), null, false);

            // make sure target is not a descendent of the copied childen
            validateTargetParentIsNotDescendent(target, childSummaries
               .getComponentList(PSComponentSummary.TYPE_FOLDER), true);

            setSummariesWithNewName(childSummaries, children);

            Map<PSLocator, PSLocator> copiedContent = new HashMap<PSLocator, PSLocator>();
            cloneSiteFolderChildren(request, context, childSummaries, target,
               options, copiedContent);

            createRelatedContent(request, options);

            PSLocator folderTarget = tracker.getFolderTarget(source);
            if (folderTarget != null)
               connectNavigation(request, context, folderTarget, target,
                  options);


            // update site definition home page url
            if (options.isCloneSite())
            {
               final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
               IPSSite site = smgr.loadSite(options.getSiteName());

               smgr.saveSite(site);
            }

            logger.info("...finished copy operation.");

            Boolean hadErrors = (Boolean) m_hadErrors.get();
            if (hadErrors != null && hadErrors.booleanValue())
               return logFileName;

            return null;
         }
         catch (PSException e)
         {
            logger.error("Unexpected error: ", e);
            throw new PSCmsException(e);
         }
         finally
         {
            // enable nav folder effect
            request
               .removeParameter(IPSHtmlParameters.RXS_DISABLE_NAV_FOLDER_EFFECT);
            PSFolderSecurityManager.setCheckFolderPermissions(true);
            //if (request!=null)
            //   PSThreadRequestUtils.restoreOriginalRequest();
            // enable permission checking
         
         }
      }
      finally
      {
         // clean up logger resources
        
         /*Logger logger = getLogger();
         logger.removeAppender(appender);
          */
         m_log.set(null);
      }      
   }
   // see IPSFolderProcessor, additionally this method will throw
   // a PSCmsException if the current user does not have Admin access
   // to the target folder 
   @SuppressWarnings("unchecked")
   public void copyFolderSecurity(PSLocator source, PSLocator target)
      throws PSCmsException
   {
      notNull(source);
      notNull(target);
     
      PSFolder[] folders = openFolder(new PSKey[]{source, target});
      PSFolder srcFolder = folders[0];
      PSFolder tgtFolder = folders[1];
      
      checkHasFolderPermission(tgtFolder, PSObjectPermissions.ACCESS_ADMIN,
            true);
      
      tgtFolder.mergeAclFrom(srcFolder.getAcl());
      
      save(tgtFolder);
   }
   
   /**
    * Convert the supplied home page url to the new copied site, folder and
    * content ids. If parameters or mappings are not found, the url remains
    * unchanged for the paramter, no error will be reported.
    *
    * @param homePageUrl the home page url to be converted, may be
    * <code>null</code> or empty.
    * @param options the copy folder options is used to lookup the source to
    * target siteid mapping, assumed not <code>null</code>.
    * @param tracker the relationship tracker is used to lookup the source to
    * target folderid and contentid mappings, assumed not <code>null</code>.
    * @return the converted string, never <code>null</code>or empty.
    */
   private String convertHomePageUrl(String homePageUrl,
      PSCloningOptions options, PSRelationshipTracker tracker)
   {
      if (homePageUrl != null && homePageUrl.trim().length() > 0)
      {
         String contentId = PSUrlUtils.getUrlParameterValue(homePageUrl,
            IPSHtmlParameters.SYS_CONTENTID);
         if (contentId != null)
         {
            Integer cid = tracker.getItemTargetId(new Integer(contentId));
            if (cid != null)
            {
               homePageUrl = PSUrlUtils.replaceUrlParameterValue(homePageUrl,
                  IPSHtmlParameters.SYS_CONTENTID, cid.toString());
            }
         }

         String folderId = PSUrlUtils.getUrlParameterValue(homePageUrl,
            IPSHtmlParameters.SYS_FOLDERID);
         if (folderId != null)
         {
            PSLocator fid = tracker.getFolderTarget(folderId);
            if (fid != null)
            {
               homePageUrl = PSUrlUtils
                  .replaceUrlParameterValue(homePageUrl,
                     IPSHtmlParameters.SYS_FOLDERID, Integer.toString(fid
                        .getId()));
            }
         }

         String siteId = PSUrlUtils.getUrlParameterValue(homePageUrl,
            IPSHtmlParameters.SYS_SITEID);
         if (siteId != null)
         {
            Integer sid = (Integer) options.getSiteMappings().get(
               new Integer(siteId));
            if (sid != null)
            {
               homePageUrl = PSUrlUtils.replaceUrlParameterValue(homePageUrl,
                  IPSHtmlParameters.SYS_SITEID, sid.toString());
            }
         }
      }

      return homePageUrl;
   }

   /**
    * Get the logger for the current thread. Two possible loggers can be
    * returned, one for the copy folder action and the class logger for all
    * other actions.
    *
    * @return the logger for the copy folder action if set, otherwise the
    * default logger for this class, never <code>null</code>.
    */
   private Logger getLogger()
   {
      return log;
   }

   /**
    * Set the global error flag to the supplied value. This is only used for the
    * copy folder action.
    *
    * @param value the new value for the global error flag usd in the copy
    * folder action.
    */
   @SuppressWarnings("unchecked")
   private void setHadErrors(boolean value)
   {
      m_hadErrors.set(value ? Boolean.TRUE : Boolean.FALSE);
   }

   /**
    * Tests whether managed navigation is used or not.
    *
    * @return always <code>false</code> if the threal local variable is not
    * initialized, otherwise the boolean value of its value object.
    */
   private boolean isManagedNavUsed()
   {
      Boolean isManagedNavUsed = (Boolean) m_isManagedNavUsed.get();
      if (isManagedNavUsed != null)
         return isManagedNavUsed.booleanValue();

      return false;
   }

   // see IPSFolderProcessor
   @SuppressWarnings("unchecked")
   public Set getFolderCommunities(PSLocator source) throws PSCmsException
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      Set communities = new HashSet();

      PSComponentSummaries summaries = new PSComponentSummaries();
      collectComponentSummaries(source, summaries);

      Iterator walker = summaries.iterator();
      while (walker.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) walker.next();
         communities.add(new Integer(summary.getCommunityId()));
      }

      return communities;
   }

   /**
    * Collect all component summaries recursively  down to the bottom starting
    * with the supplied source locator.
    *
    * @param parentFolder the locator of the source component to start the
    * collection from, assumed not <code>null</code>.
    * @param summaries the container for the results, assumed not
    * <code>null</code>.
    * @throws PSCmsException for any error.
    */
   private void collectComponentSummaries(PSLocator parentFolder,
      PSComponentSummaries summaries) throws PSCmsException
   {
      PSComponentSummaries newSummaries = getFolderChildren(
         parentFolder, getFilterFlags(), true, FOLDER_RELATE_TYPE);
      Iterator walker = newSummaries.iterator();
      while (walker.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) walker.next();
         summaries.add(summary);
         if (summary.isFolder())
            collectComponentSummaries(summary.getCurrentLocator(), summaries);
      }
   }

   /**
    * Clone the supplied site folder children recursivly and adds them to the
    * target folder. Duplicates based on their names will be removed from the
    * children before the process is started and an error is logged.
    *
    * @param request the request used for the cloning operation, assumed not
    * <code>null</code>.
    * @param context the request context used for the operation, assumed not
    * <code>null</code>.
    * @param children the component summaries for the children to be cloned,
    * assumed not <code>null</code>, may be empty. All duplicates based on
    * component names will be removed from this list.
    * @param target the target folder into which to clone the children, assumed
    * not <code>null</code>.
    * @param options the cloning options, assumed not <code>null</code>.
    * @param copiedContent maps all copied content. The key identifies the
    * original content the was copied, the value identifies the new created
    * copy, assumed not <code>null</code>, may be empty.
    * @throws PSException for any error.
    */
   @SuppressWarnings("unchecked")
   private void cloneSiteFolderChildren(PSRequest request, IPSRequestContext context, PSComponentSummaries children,
         PSLocator target, PSCloningOptions options, Map<PSLocator, PSLocator> copiedContent) throws PSException
   {
      try
      {
         /*
          * Children with the same name are not allowed in one folder. This
          * walks all supplied children and weeds out the ones which have
          * duplicate names (they will not be cloned). Then an error is logged
          * with a list of all duplicate items to inform the user that there is
          * a problem in his source.
          */
         List duplicateChildren = new ArrayList();
         Map childSummaries = new HashMap();
         Iterator summaries = children.iterator();
         while (summaries.hasNext())
         {
            PSComponentSummary summary = (PSComponentSummary) summaries.next();
            if (childSummaries.get(summary.getName().toLowerCase()) == null)
               childSummaries.put(summary.getName().toLowerCase(), summary);
            else
               duplicateChildren.add(summary);
         }

         if (!duplicateChildren.isEmpty())
         {
            PSFolder targetFolder = loadFolder(target);
            StringBuffer skippedItems = new StringBuffer();
            Iterator duplicates = duplicateChildren.iterator();
            while (duplicates.hasNext())
            {
               PSComponentSummary duplicate = (PSComponentSummary) duplicates.next();

               children.remove(duplicate);

               skippedItems.append(duplicate.getName() + " (" + duplicate.getCurrentLocator().getId() + ")");
               if (duplicates.hasNext())
                  skippedItems.append(",");
            }

            getLogger().error(
                  "Skipped cloning items because they would " + "produce duplicates in the target folder [name (id)]: "
                        + targetFolder.getName() + " (" + target.getId() + "). "
                        + "The skipped items were [name (id), ...]:" + skippedItems.toString());
            setHadErrors(true);
         }

         PSComponentSummaries navigationSummaries = new PSComponentSummaries();
         if (options.isNavigationContent() || options.isAllContent())
         {
            Iterator objects = children.iterator();
            while (objects.hasNext())
            {
               PSComponentSummary summary = (PSComponentSummary) objects.next();
               if (isNavItem(summary))
                  navigationSummaries.add(summary);
            }

            objects = navigationSummaries.iterator();
            while (objects.hasNext())
               children.remove((PSComponentSummary) objects.next());
         }

         // clone all navigation items as new copy
         boolean isAsNewCopy = true;
         cloneItems(request, navigationSummaries, target, isAsNewCopy, options.getCommunityMappings(), copiedContent,
               options.useSrcItemWorkflow());

         // clone all content but navigation
         isAsNewCopy = options.isAllContent() && options.isCopyContentAsNewCopy();
         cloneItems(request, children, target, isAsNewCopy, options.getCommunityMappings(), copiedContent,
               options.useSrcItemWorkflow());

         /*
          * For each folder child, clone a new folder from it and insert the
          * relationship between the new folder child and its parent.
          */
         Iterator childFolders = children.getComponents(PSComponentSummary.TYPE_FOLDER);
         while (childFolders.hasNext())
         {
            // clone a new folder
            PSComponentSummary origComp = (PSComponentSummary) childFolders.next();
            Element[] elements = load(FOLDER_PROXY_TYPE, new PSKey[]
            {origComp.getCurrentLocator()});
            PSFolder folder = new PSFolder(elements[0]);
            PSFolder newFolder = (PSFolder) folder.clone();

            // the folder name may be overridden
            newFolder.setName(origComp.getName());

            // change the community if mapped
            Integer newCommunity = (Integer) options.getCommunityMappings()
                  .get(new Integer(newFolder.getCommunityId()));
            if (newCommunity != null)
            {
               context.setSessionPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, newCommunity.toString());
               newFolder.setCommunityId(newCommunity.intValue());
            }

            // save the new folder
            PSSaveResults results = save(new IPSDbComponent[]
            {newFolder});
            PSLocator newLocator = ((PSFolder) results.getResults()[0]).getLocator();

            PSRelationshipTracker tracker = (PSRelationshipTracker) m_tracker.get();
            if (tracker != null)
               tracker.addFolderMapping(folder.getLocator(), newLocator);

            // add relationship between the inserted folder and the parent
            List childItems = new ArrayList();
            childItems.add(newLocator);
            addChildren(childItems, target);

            /*
             * Recursively add the children of the original folder to the new
             * child folder.
             */
            PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

            List grandChildren = processor.getDependentLocators(FOLDER_RELATE_TYPE, origComp.getCurrentLocator());
            if (grandChildren.size() > 0)
            {
               PSComponentSummaries grandChildSummaries = getComponentSummaries( grandChildren.iterator(),
                     null, false);

               PSComponentSummaries processSummaries = new PSComponentSummaries();
               if (options.isAllContent())
               {
                  // copy all but navigation content
                  Iterator objects = grandChildSummaries.iterator();
                  while (objects.hasNext())
                  {
                     PSComponentSummary summary = (PSComponentSummary) objects.next();
                     if (summary.isItem())
                        processSummaries.add(summary);
                  }
               }
               else if (options.isNavigationContent())
               {
                  // copy navigation content
                  Iterator objects = grandChildSummaries.iterator();
                  while (objects.hasNext())
                  {
                     PSComponentSummary summary = (PSComponentSummary) objects.next();
                     if (isNavItem(summary))
                        processSummaries.add(summary);
                  }
               }

               // copy all folders
               Iterator folders = grandChildSummaries.getComponentList(PSComponentSummary.TYPE_FOLDER).iterator();
               while (folders.hasNext())
                  processSummaries.add((PSComponentSummary) folders.next());

               cloneSiteFolderChildren(request, context, processSummaries, newLocator, options, copiedContent);
            }
         }
      }
      catch (Exception e)
      {

         getLogger().error("Fatal error copying folder" + " removing Items created in process", e);
         setHadErrors(true);
         IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();

         try
         {
            purgeHelper.purgeAll(copiedContent.values());

         }
         catch (Exception ex)
         {
            throw new IllegalArgumentException("Failed to purge all created items with the following ids:"
                  + copiedContent.values(), ex);
         }
      }
   }

   /**
    * Clone all supplied component summaries either as link or as new copy.
    *
    * @param request the request used for the cloning operation, assumed not
    * <code>null</code>.
    * @param children the child component summaries to be cloned, assumed not
    * <code>null</code>, may be empty.
    * @param target the target folder into which to clone the supplied children,
    * assumed not <code>null</code>.
    * @param isAsNewCopy <code>true</code> to clone the children as new copy,
    * <code>false</code> to clone them as link.
    * @param communityMappings a map of source to target community ids. Assumed
    * not <code>null</code>, may be empty.
    * @param copiedContent maps all copied content. The key identifies the
    * original content the was copied, the value identifies the new created
    * copy, assumed not <code>null</code>, may be empty.
    * @param useSrcWorfklow true to use the source item's workflow if valid for
    * the content type and community, <code>false</code> to calculate the workflow
    * as when creating a new item.
    * 
    * @throws PSException for any error.
    */
   @SuppressWarnings("unchecked")
   private void cloneItems(PSRequest request, PSComponentSummaries children,
      PSLocator target, boolean isAsNewCopy, Map communityMappings,
      Map<PSLocator, PSLocator> copiedContent, boolean useSrcWorfklow) throws PSException
   {
      List<PSLocator> childItems = null;
      if (isAsNewCopy)
      {
         // weed out all children which were already copied earlier
         PSComponentSummaries newChildren = new PSComponentSummaries();
         List<PSLocator> existingChildren = new ArrayList<PSLocator>();
         Iterator summaries = children.iterator();
         while (summaries.hasNext())
         {
            PSComponentSummary summary = (PSComponentSummary) summaries.next();
            PSLocator newLocator = copiedContent.get(
               summary.getCurrentLocator());
            if (newLocator == null)
               newChildren.add(summary);
            else
               existingChildren.add(newLocator);
         }

         childItems = cloneItems(newChildren
            .getComponents(PSComponentSummary.TYPE_ITEM), true,
            communityMappings, false, useSrcWorfklow);
         List<PSLocator> originalLocators = newChildren.getComponentLocators(
            PSComponentSummary.TYPE_ITEM,
            PSComponentSummary.GET_CURRENT_LOCATOR);
         for (int i = 0; i < childItems.size(); i++)
         {
            PSLocator originalLocator = originalLocators.get(i);
            PSLocator newLocator = childItems.get(i);
            copiedContent.put(originalLocator, newLocator);
         }

         // just link children which were copied earlier
         if (!existingChildren.isEmpty())
            addChildren(existingChildren, target);
      }
      else
      {
         childItems = children.getComponentLocators(
            PSComponentSummary.TYPE_ITEM,
            PSComponentSummary.GET_CURRENT_LOCATOR);
      }

      addChildren(childItems, target);
   }

   /**
    * Recreated all relationships tracked during a clone site folder action and
    * fixup inline links as needed.
    *
    * @param request the request used for this process, assumed not
    * <code>null</code>.
    * @param options the cloning options are used to get the site id mappings,
    * assumed not <code>null</code>.
    * @throws PSException for any error.
    */
   @SuppressWarnings("unchecked")
   private void createRelatedContent(PSRequest request, PSCloningOptions options)
      throws PSException
   {
      getLogger().debug("Recreating relationships and fixup inline links...");
      PSRelationshipTracker tracker = (PSRelationshipTracker) m_tracker.get();
      if (tracker != null)
      {
         Iterator sources = tracker.getItemSources();
         while (sources.hasNext())
         {
            Integer sourceId = (Integer) sources.next();
            Integer targetId = tracker.getItemTargetId(sourceId);

            PSRelationshipSet newRelationships = new PSRelationshipSet();
            Map inlineRelationships = new HashMap();
            PSLocator processedItem = null;

            try
            {
               Iterator relationships = tracker.getItemRelationships(sourceId);
               while (relationships.hasNext())
               {
                  PSRelationship relationship = (PSRelationship) relationships
                     .next();
                  //TODO : Change this to use the proper relationship configuration cloning options
                  if (relationship.getConfig().getName().equals("LocalContent"))
                     break;
                  PSRelationship newRelationship = (PSRelationship) relationship
                     .clone();
                  newRelationship.setId(-1);
                  newRelationship
                     .setOwner(new PSLocator(targetId.intValue(), 1));

                  // assume we need to update the dependent
                  boolean updateDependentId = true;
                  if (relationship.getConfig().isActiveAssemblyRelationship())
                  {
                     // update relationship to new folder id
                     String originalFolderId = relationship
                        .getProperty(IPSHtmlParameters.SYS_FOLDERID);
                     if (originalFolderId != null
                        && originalFolderId.trim().length() > 0)
                     {
                        PSLocator target = tracker
                           .getFolderTarget(originalFolderId);
                        if (target != null)
                        {
                           String newFolderId = Integer
                              .toString(target.getId());
                           newRelationship.setProperty(
                              IPSHtmlParameters.SYS_FOLDERID, newFolderId);

                           getLogger().debug(
                              "Mapped source folder [" + originalFolderId
                                 + "] to target folder [" + newFolderId + "].");
                        }
                        else
                        {
                           // must be a cross site link
                           updateDependentId = false;
                        }
                     }

                     // update relationship to new site id
                     String originalSiteId = relationship
                        .getProperty(IPSHtmlParameters.SYS_SITEID);
                     if (originalSiteId != null
                        && originalSiteId.trim().length() > 0)
                     {
                        Integer newSiteId = (Integer) options.getSiteMappings()
                           .get(new Integer(originalSiteId));
                        if (newSiteId != null)
                        {
                           newRelationship.setProperty(
                              IPSHtmlParameters.SYS_SITEID, newSiteId
                                 .toString());

                           getLogger().debug(
                              "Mapped source site [" + originalSiteId
                                 + "] to target site [" + newSiteId.toString()
                                 + "].");
                        }
                        
                        /*
                         * If it is a sitesubfolder cloning the site mappings
                         * will be empty and the previous code is causing the
                         * bug RX-12981. updateDependentId needs to be set to
                         * false only when it is a site cloning. This fix does
                         * not cover another case logged a separate bug
                         * RX-12982.
                         */
                        if (options.getType() == PSCloningOptions.TYPE_SITE
                              && newSiteId == null)
                        {
                           // must be a cross site link
                           updateDependentId = false;
                        }
                     }
                  }

                  Integer newDependentId = tracker.getItemTargetId(new Integer(
                     relationship.getDependent().getId()));
                  if (newDependentId != null && updateDependentId)
                     newRelationship.setDependent(new PSLocator(newDependentId
                        .intValue(), -1));

                  getLogger().debug(
                     "Relate content for source " + sourceId.toString());
                  getLogger().debug(
                     "Source relationship "
                        + relationship.getOwner().toString() + ", "
                        + relationship.getDependent().toString());
                  getLogger().debug(
                     "Target relationship "
                        + newRelationship.getOwner().toString() + ", "
                        + newRelationship.getDependent().toString());

                  newRelationships.add(newRelationship);

                  if (newRelationship.isInlineRelationship())
                  {
                     Integer key = new Integer(relationship.getId());
                     inlineRelationships.put(key, newRelationship);
                  }

                  if (processedItem == null)
                     processedItem = newRelationship.getOwner();
               }

               // save the new relationships first
               PSRelationshipProcessor.getInstance().save(newRelationships);
            }
            catch (PSException e)
            {
               getLogger().error(
                  "Error creating related content "
                     + "relationships for item: " + processedItem.getId());
               getLogger().error("The error was: " + e.getLocalizedMessage());
               setHadErrors(true);
               continue;
            }

            try
            {
               // now process inline links if there are any
               if (inlineRelationships.size() > 0)
               {
                  PSInlineLinkProcessor.processInlineLinkItem(request,
                     processedItem, inlineRelationships, -1);
               }
            }
            catch (PSException e)
            {
               getLogger().error(
                  "Error processing inline links for item: " + "for item: "
                     + processedItem.getId());
               getLogger().error("The error was: " + e.getLocalizedMessage());
               setHadErrors(true);
            }
         }
      }
   }

   /**
    * Connect the navigation for copy site subfolder actions. This is required
    * if any navigation content was copied and connects the navigation item of
    * the copied subfolder with the navigation item of the target folder.
    *
    * @param request the request used to perform lookup requests, assumed not
    * <code>null</code>.
    * @param context the request context used to create the navigation
    * connection, assumed not <code>null</code>.
    * @param copiedSubfolder the locator of the copied subfolder, assumed not
    * <code>null</code>.
    * @param target the target folder into which the copy was created, assumed
    * not <code>null</code>.
    * @param options the copy options used to create the copy, assumed not
    * <code>null</code>.
    * @throws PSCmsException for any error.
    */
   private void connectNavigation(PSRequest request, IPSRequestContext context,
      PSLocator copiedSubfolder, PSLocator target, PSCloningOptions options)
      throws PSCmsException
   {
      try
      {
         if (isManagedNavUsed() && options.isCloneSiteSubfolder()
            && (options.isNavigationContent() || options.isAllContent()))
         {
            PSNavConfig config = PSNavConfig.getInstance();
            // get all summaries of the target folder
            int filterFlags = getFilterFlags();
            filterFlags = filterFlags 
               | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
            Iterator summaries = getFolderChildren(target,
               filterFlags, false, FOLDER_RELATE_TYPE).iterator();

            while (summaries.hasNext())
            {
               PSComponentSummary summary = (PSComponentSummary) summaries
                  .next();

               if (summary.getContentTypeId() == config.getNavonType().getUUID())
               {
                  Iterator copies = getFolderChildren(copiedSubfolder,
                     filterFlags, false, FOLDER_RELATE_TYPE).iterator();
                  while (copies.hasNext())
                  {
                     PSComponentSummary copy = (PSComponentSummary) copies
                        .next();
                     if (copy.getContentTypeId() == config.getNavonType().getUUID())
                     {
                        PSNavFolderUtils.addNavonSubmenu(context, summary
                           .getCurrentLocator(), copy.getCurrentLocator());
                        getLogger().debug(
                           "Connected navigation from item "
                              + summary.getCurrentLocator().getId()
                              + " to item " + copy.getCurrentLocator().getId());

                        // there is only one navigation item
                        break;
                     }
                  }

                  // there is only one navigation item
                  break;
               }
            }
         }
      }
      catch (PSCmsException e)
      {
         getLogger().error(
            "Error connecting the navigation from source " + "folder "
               + copiedSubfolder.getId() + " to target folder "
               + target.getId() + ".");
         getLogger().error("The error was: " + e.getLocalizedMessage());
         setHadErrors(true);
      }
   }

   /**
    * Checks if the supplied item is a navigation item.
    *
    * @param summary the item summary to test, assumed not <code>null</code>.
    * @return <code>true</code> if the supplied item is a navigation item,
    * <code>false</code> otherwise or if managed navigation is not used for
    * the copied folder.
    */
   private boolean isNavItem(PSComponentSummary summary)
   {
      if (!isManagedNavUsed())
         return false;

      PSNavConfig config = null;
      try
      {
         config = PSNavConfig.getInstance();
      }
      catch (PSNavException e)
      {
         return false;
      }

      return config.getNavTreeType().getUUID() == summary.getContentTypeId()
         || config.getNavonType().getUUID() == summary.getContentTypeId()
         || config.getNavImageType().getUUID() == summary.getContentTypeId();
   }

   /**
    * Gets all folder paths to the root for the supplied locator. It is similar
    * with {@link #getRelationshipOwnerPaths(String, PSLocator, String)} except
    * each folder path contains a list of locators to the root, where the 1st
    * entry is the locator of its direct parent and the last enty is the locator
    * of the root.
    * <p>
    * The folder is not filtered by community
    *
    * @param itemLocator the item locator, never <code>null</code>.
    *
    * @return a list of <code>List</code> objects, never <code>null</code>,
    * may be empty. Each <code>List</code> object contains a list of
    * <code>PSLocator</code> objects, as the folder locators to the root.
    *
    * @throws PSCmsException if an error occurs.
    */
   public List<List<PSLocator>> getFolderLocatorPaths(PSLocator itemLocator)
      throws PSCmsException
   {
      if (itemLocator == null)
         throw new IllegalArgumentException("itemLocator cannot be null");
      return getFolderLocatorPaths(itemLocator, null);
   }

   /**
    * The utility method used by {@link #getFolderLocatorPaths(PSLocator)}. See
    * {@link #getFolderLocatorPaths(PSLocator)} for detail.
    *
    * @param itemLocator the locator of an item or folder, assume not
    * <code>null</code> and it is a folder locator if the <code>parents</code>
    * is not <code>null</code>.
    * @param parents the parent locators. It is used to collect all parent
    * locators for the supplied folder if it is not <code>null</code>.
    *
    * @return a list of locator paths if <code>parents</code> is
    * <code>null</code>; otherwise returns the <code>parents</code>. Never
    * <code>null</code>, may be empty.
    *
    * @throws PSCmsException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private List getFolderLocatorPaths(PSLocator itemLocator, List parents) throws PSCmsException
   {
      // get immediate parents
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName(FOLDER_RELATE_TYPE);
      filter.setDependent(itemLocator);
      filter.setCommunityFiltering(false);

      PSRelationshipSet relSet = PSRelationshipProcessor.getInstance().getRelationships(filter);
      Iterator rels = relSet.iterator();
      PSRelationship rel;
      if (parents == null) // 1st time come in, get paths for an item/folder
      {
         // get the folder paths (may be more than one) for the item locator.
         List idPaths = new ArrayList(relSet.size());
         while (rels.hasNext())
         {
            rel = (PSRelationship) rels.next();
            parents = new ArrayList();
            parents.add(rel.getOwner());
            idPaths.add(getFolderLocatorPaths(rel.getOwner(), parents));
         }
         return idPaths;
      }

      // get one folder path to the root.
      if (rels.hasNext())
      {
         // folder has only one immediate folder parent
         rel = (PSRelationship) rels.next();
         parents.add(rel.getOwner());
         getFolderLocatorPaths(rel.getOwner(), parents);
      }

      return parents;
   }

  

   /**
    * Validates name of the modified item, which is the value of the
    * "sys_title" field. The item name must be unique within all its
    * parent folders.
    *
    * @param request The current request, assume not <code>null</code>.
    * @param isInsert flag indicating that this is an insert action
    *
    * @throws PSCmsException if an error occurs during the validation process.
    */
   @SuppressWarnings("unchecked")
   public static void validateUniqueDepName(final IPSRequestContext request,
         final boolean isInsert)
      throws PSCmsException
   {
      // get the locator if exist
      PSLocator locator = null;
      String contentId =
            request.getParameter( PSContentEditorHandler.CONTENT_ID_PARAM_NAME);

      List parentList = null;
      PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();

      // get the sys_title, assume it is a required field and has validated
      String sys_title = request.getParameter(IPSHtmlParameters.SYS_TITLE);

       // check if we need to validate at all
      if (sys_title != null                // sys_title is not empty
         && sys_title.trim().length() != 0)
      {
         // see if modifying existing item
         if (!isInsert && contentId != null && contentId.trim().length() != 0)
         {
            int id = Integer.parseInt(contentId);
            int rev =
               Integer.parseInt(
                  request.getParameter(
                     PSContentEditorHandler.REVISION_ID_PARAM_NAME));
            locator = new PSLocator(id, rev);
            // get the parent locators
            int donotfilterby =
               PSRelationshipConfig.FILTER_TYPE_COMMUNITY
                  | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;

            parentList =
               processor.getParents(
                  FOLDER_RELATE_TYPE,
                  locator, donotfilterby);
         }
         else if (isInsert)
         {
            // get the target parent folder id from the redirect url
            String folderId = null;
            String psredirect = request.getParameter(
               IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
            if (psredirect != null && psredirect.trim().length() > 0)
            {
               int index = psredirect.indexOf(IPSHtmlParameters.SYS_FOLDERID);
               if(index >= 0)
               {
                  folderId = psredirect.substring(index +
                     IPSHtmlParameters.SYS_FOLDERID.length() + 1);
                  index = folderId.indexOf('&');
                  if(index > -1)
                     folderId = folderId.substring(0, index);
               }
            }
            
            /*
             * This new parameter is for Cougar Assets to be placed in a folder.
             * TODO: We should duplicate the behavior of this extension using
             * the new request parameter in a new extension.
             * -Adam Gent
             */
            String assetFolderId = request.getParameter(
                  IPSHtmlParameters.SYS_ASSET_FOLDERID);
            if ( isNotBlank(assetFolderId) && isBlank(folderId) ) {
               folderId = assetFolderId;
            }

            if (isNotBlank(folderId))
            {
               parentList = new ArrayList();
               PSLocator parent = new PSLocator(folderId);
               parent.setPersisted(true);
               parentList.add(parent);
            }
         }
      }

      if (parentList == null)
         return; // nothing to validate

      // validate unique names for each parent if there is any
      Iterator parents = parentList.iterator();
      while (parents.hasNext())
      {
         PSLocator parentLocator = (PSLocator) parents.next();
         boolean isUniqueName = validateUniqueDepName(
               parentLocator,
               locator,
               sys_title,
               request);

         if (! isUniqueName) // error
         {
            // get the parent name for the error message
            String parentName = "";
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            PSComponentSummary summary = cms.loadComponentSummary(parentLocator
                  .getId());
            if (summary != null)
               parentName = summary.getName();

            // throw the error
            if (isInsert)
            {
               Object[] args =
               {
                  sys_title,
                  parentName,
               };
               throw new PSCmsException(
                  IPSCmsErrors.FOLDER_REL_INSERT_ERROR_DUPLICATED_CHILDNAME,
                  args);
            }
            Object[] args =
            {
               String.valueOf(locator.getId()),
               String.valueOf(locator.getRevision()),
               sys_title,
               parentName
            };
            throw new PSCmsException(
               IPSCmsErrors.MODIFY_ERROR_DUPLICATED_CHILDNAME,
               args);
         }
      }
   }

   /**
    * Validates the supplied dependent with the following rules:
    * <p>
    * The the dependent's name must be different (case-insensitive) then
    * the names of all other children of the owner (folder) unless the same
    * object is already a child of the supplied owner. A folder does not allow
    * children with duplicate names.
    *
    * @param owner the locator of the owner folder, not <code>null</code>.
    * @param dependent the locator of the dependent item, may be
    *    <code>null</code> if the dependent is being inserted, in which case
    *    <code>depName</code> must be supplied.
    * @param depName The name (or sys_title) of the dependent. It may be
    *    <code>null</code> or empty only if <code>dependent</code> is not
    *    <code>null</code>, in which case it will be looked up using
    *    the supplied dependent locator.
    * @param request the current request, not <code>null</code>.
    * @return <code>true</code> if validated, <code>false</code> otherwise.
    */
   private static boolean validateUniqueDepName(PSLocator owner,
      PSLocator dependent, String depName, PSRequest request)
   {
      return validateUniqueDepName(owner, dependent, depName,
         new PSRequestContext(request));
   }

   /**
    * Validates the supplied dependent with the following rules:
    * <p>
    * The the dependent's name must be different (case-insensitive) then
    * the names of all other children of the owner (folder) unless the same
    * object is already a child of the supplied owner. A folder does not allow
    * children with duplicate names.
    *
    * @param owner the locator of the owner folder, not <code>null</code>.
    * @param dependent the locator of the dependent item, may be
    *    <code>null</code> if the dependent is being inserted, in which case
    *    <code>depName</code> must be supplied.
    * @param depName The name (or sys_title) of the dependent. It may be
    *    <code>null</code> or empty only if <code>dependent</code> is not
    *    <code>null</code>, in which case it will be looked up using
    *    the supplied dependent locator.
    * @param request the current request, not <code>null</code>.
    * @return <code>true</code> if validated, <code>false</code> otherwise.
    */
   private static boolean validateUniqueDepName(PSLocator owner,
      PSLocator dependent, String depName, IPSRequestContext request)
   {
      if (owner == null)
         throw new IllegalArgumentException("owner may not be null");

      if (dependent == null && (depName == null ||
         depName.trim().length() == 0))
            throw new IllegalArgumentException(
               "dependent may not be null if depName not supplied");

      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      PSAttemptResult result = new PSAttemptResult();
      validateUniqueDepName(owner, dependent, depName,
            request, result);

      return (result.getException() == null);
   }

   /**
    * See {@link #validateUniqueDepName(PSLocator, PSLocator, String,
    * PSRequest)} for description.
    *
    * @param ownerLocator the locator of the owner folder, assumed not
    *    <code>null</code>.
    * @param depLocator the locator of the dependent item, assumed not
    *    <code>null</code> if <code>depName</code> is <code>null</code> or
    *    empty.  If <code>null</code>, it is assumed the dependent is being
    *    inserted.
    * @param depName The name (or sys_title) of the dependent. Assumed not
    *    <code>null</code> or empty unless <code>dependent</code> is not
    *    <code>null</code> in which case it will be looked up using
    *    the supplied dependent locator.
    * @param request the current request, not <code>null</code>.
    * @param result the result object into which the result of the validation
    *    will be set, assume it is not <code>null</code>.
    */
   private static void validateUniqueDepName(PSLocator ownerLocator,
      PSLocator depLocator, String depName, IPSRequestContext request,
      PSEffectResult result)
   {
      try
      {
         List<Object[]> children = getChildItems(ownerLocator, request);


         // need to lookup the name if it was not supplied
         if (depName == null || depName.trim().length() == 0)
         {
            Object[] depItem = getItem(depLocator);
            depName = (String)depItem[0];
         }

         Iterator<Object[]> walker = children.iterator();
         while (walker.hasNext())
         {
            Object[] child = walker.next();

            if (((String)child[0]).equalsIgnoreCase(depName))
            {
               // if inserting, then a match is an error
               if (depLocator == null)
               {
                  Object[] summary = getItem(ownerLocator);
                  String parentName = (String)summary[0];
                  Object[] args =
                  {
                     depName,
                     parentName,
                  };
                  PSCmsException exception = new PSCmsException(
                     IPSCmsErrors.FOLDER_REL_INSERT_ERROR_DUPLICATED_CHILDNAME,
                     args);

                  result.setError(exception);
                  result.setKeys(null);

                  return;
               }
               /*
                * If the dependent already exists, then it is only an error if
                * it is not the same item (meaning the same content id). The
                * revision is not considered for this test.
                */
               else if (((Integer)child[1]).intValue() != depLocator.getId())
               {
                  Object[] parent = getItem(ownerLocator);
                  Object[] args =
                  {
                     depName,
                     parent[0],
                     String.valueOf(depLocator.getId()),
                     String.valueOf(depLocator.getRevision())
                  };
                  PSCmsException exception = new PSCmsException(
                     IPSCmsErrors.FOLDER_REL_ERROR_DUPLICATED_CHILDNAME, args);

                  result.setError(exception);
                  result.setKeys(new PSKey[] { depLocator });

                  return;
               }
            }
         }

         result.setSuccess();
      }
      catch (PSException ex)
      {
         result.setError(ex);
      }
   }

   /**
    * Get the child items for the supplied owner.
    *
    * @param owner the locator of the owner, assume not <code>null</code>.
    * @param request the current request object, assume not <code>null</code>.
    *
    * @return a list of child items, never <code>null</code>, but may be empty.
    * where each element contains a name (1st object) and its content id (2nd object).
    *
    * @throws PSCmsException if an error occurs.
    */
   /**
    * Get the child items for the supplied owner.
    *
    * @param owner the locator of the owner, assume not <code>null</code>.
    * @param request the current request object, assume not <code>null</code>.
    *
    * @return a list of child items, never <code>null</code>, but may be empty.
    * where each element contains a name (1st object) and its content id (2nd object).
    *
    * @throws PSCmsException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private static List<Object[]> getChildItems(PSLocator owner,
                                               IPSRequestContext request) throws PSCmsException
   {
      IPSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      List<Object[]> result = new ArrayList<Object[]>();
      if (cache != null)   // get the child items from cache
      {
         PSRelationshipProcessor processor = PSRelationshipProcessor.getInstance();
         int donotfilterby =
                 PSRelationshipConfig.FILTER_TYPE_COMMUNITY
                         | PSRelationshipConfig.FILTER_TYPE_FOLDER_PERMISSIONS;
         List locators = processor.getDependentLocators(
                 PSRelationshipConfig.TYPE_FOLDER_CONTENT, owner, donotfilterby);
         Iterator locs = locators.iterator();
         IPSItemEntry itemEntry;
         while (locs.hasNext())
         {
            int id = ((PSLocator)locs.next()).getId();
            itemEntry = cache.getItem(id);
            if (itemEntry == null)
            {

               log.info("Cannot find item with content id=" + id
                       + ", but it is in a folder relationship as a "
                       + "dependent with owner id=" + owner.getId());
            }
            result.add(new Object[]{itemEntry.getName(), new Integer(id)});
         }
      }
      else              // get the child items from repository
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setOwner(owner);
         filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         PSComponentSummary[] children = relProxy.getSummaries(filter, false)
                 .toArray();
         if (children.length > 0)
         {
            Object[] item;
            for (int i=0; i < children.length; i++)
            {
               item = new Object[]{children[i].getName(), new Integer(children[i]
                       .getCurrentLocator().getId())};
               result.add(item);
            }
         }
      }

      return result;
   }


   private PSRelationshipDbProcessor getDbProcessor()
   {
      if (m_dbProcessor==null)
         return PSRelationshipDbProcessor.getInstance();
      else
         return m_dbProcessor;
   }
   /**
    * Determines if the specified item exist.
    * 
    * @param contentId the content ID the item, not <code>null</code>.
    * 
    * @return <code>true</code> if the item exist; otherwise return
    *         <code>false</code>.
    */
   public boolean doesItemExist(int contentId)
   {
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      if (cache != null)
      {
         return cache.getItem(contentId) != null;
      }
      
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      return cms.loadComponentSummary(contentId) != null;
   }
   
   /**
    * Fast way to determine if the item is a folder or not from a locator.
    * @param locator
    * @return
    * @throws PSCmsException 
    * @throws PSException
    */
   public boolean isItemFolder(PSLocator locator) throws PSCmsException {
      Object[] o = getItem(locator);
      return o[2].equals(PSCmsObject.TYPE_FOLDER);
   }
      
   /**
    * Get the item from the supplied locator.
    *
    * @param locator The locator, never <code>null</code>.
    *
    * @return The summary info with object type as an array, never <code>null</code>,
    * Indice info: 0 = name, 1 = id, 2 = object type (folder or item),
    * 3 = content type id (<code>Long</code>). 
    * @throws PSCmsException 
    *
    * @throws PSException if an error occurs while retrieving the item.
    */
   public static Object[] getItem(PSLocator locator) throws PSCmsException
   {
      notNull(locator);
      
      PSItemSummaryCache cache = PSItemSummaryCache.getInstance();
      Object[] item;

      if (cache != null) // get the item from cache
      {
         IPSItemEntry itemEntry = cache.getItem(locator.getId());
         item = new Object[]{itemEntry.getName(),
            new Integer(itemEntry.getContentId()), itemEntry.getObjectType(),
            new Long(itemEntry.getContentTypeId())};
      }
      else
      {
         PSComponentSummary summary = null;
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         summary = cms.loadComponentSummary(locator.getId());

         if (summary == null)
         {
            Object[] args = { String.valueOf(locator.getId()),
                  String.valueOf(locator.getRevision())};
            throw new PSCmsException(IPSCmsErrors.FAILED_GET_SUMMARY, args);
         }

         item = new Object[]{summary.getName(),
            new Integer(summary.getCurrentLocator()
               .getId()), summary.getObjectType(), 
               new Long(summary.getContentTypeId())};
      }
      return item;
   }
   
   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param items list if items to delete
    * @throws PSCmsException
    */
   public void purgeFolderAndChildItems(List<PSLocator> items) throws PSCmsException
   {
       try
       {
           PSSqlPurgeHelperLocator.getPurgeHelper().purgeAll(items);
       }
       catch (PSException | PSValidationException e)
       {
           throw new PSCmsException(e);
       }
   }

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * This version with the parent is intended to prevent purge of items in
    * parent folder if they are in other folders. The parent folder itself is
    * not actually purged.
    * 
    * @param items list if items to delete
    * @throws PSCmsException
    */
   public void purgeFolderAndChildItems(PSLocator parent, List<PSLocator> items) throws PSCmsException
   {
       IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
       try
       {
           if (parent.getId() > 0)
           {
               purgeHelper.purgeAll(parent, items);
           }
           else
           {
               purgeHelper.purgeAll(items);
           }
       }
       catch (PSException | PSValidationException e)
       {
           throw new PSCmsException(e);
       }
   }

   /**
    * This is a fast Database method for purging Navon and Navtree items from a
    * folder and subfolders. This will prevent inconsistent navigation making
    * sure navon items are not left without a parent.
    * 
    * @param folder
    * @throws PSCmsException
    */
   public void purgeFolderNavigation(PSLocator folder) throws PSCmsException
   {

       try
       {
           PSSqlPurgeHelperLocator.getPurgeHelper().purgeNavigation(folder);
       }
       catch (PSException | PSValidationException e)
       {
           throw new PSCmsException(e);
       }

   }
   
   public static PSServerFolderProcessor getInstance()
   {
       return instance;
   }

   /**
    * The relationship type for folder object
    */
   public static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;

   /**
    * The relationship type for recycled objects
    */
   private static final String RECYCLED_RELATE_TYPE = PSRelationshipConfig.TYPE_RECYCLED_CONTENT;

   /**
    * The processor proxy type for folder object
    */
   public static final String FOLDER_PROXY_TYPE = PSDbComponent
      .getComponentType(PSFolder.class);

   /**
    * The folder object in the PSX_OBJECTS table. Initialized by ctor, never
    * <code>null</code> or modified after that.
    */
   private static PSCmsObject m_folderObject;

   /**
    * The Relationship DB processor object, initialized by ctor, never
    * <code>null</code> after that.
    */
   PSRelationshipDbProcessor m_dbProcessor = null;

   /**
    * The request object, initialized by constructor, never <code>null</code>
    * after that.
    */
   private PSRequest m_request;

   /**
    * The request context object, initialized in the constructor, never
    * <code>null</code> or modified after that.
    */
   private IPSRequestContext m_reqCtx = null;


   /**
    * The logger used to log site and site subfolder cloning operations. This is
    * a thread local storage and should only be accessed through
    * {@link #getLogger()}. The logger is reinitialized with each call to
    * {@link #copyFolder(PSLocator, PSLocator, PSCloningOptions)}.
    */
   private ThreadLocal m_log = new ThreadLocal();

   /**
    * This flag is used to keep track whether a clone site folder action
    * produced errors or not. Initialized to <code>false</code> with each call
    * to (@link #cloneSiteFolder(String, PSLocator, PSLocator,
    * PSCloningOptions), updated in {@link #cloneItems(PSRequest, PSComponentSummaries, PSLocator, boolean, Map, Map, boolean)}.
    */
   private ThreadLocal m_hadErrors = new ThreadLocal();

   /**
    * This flag is used to store whether managed navigation is used in the
    * copied folder or not. Initialized in
    * {@link #copyFolder(PSLocator, PSLocator, PSCloningOptions)}, never
    * changed after that.
    */
   private ThreadLocal m_isManagedNavUsed = new ThreadLocal();

   /**
    * This container is used to collect all related content relationships found
    * for each copied object during a copy site or site subfolder action.
    * Reinitialized in each call to {@link IPSFolderProcessor#copyFolder(
    * PSLocator, PSLocator, PSCloningOptions)}
    */
   private ThreadLocal m_tracker = new ThreadLocal();

   /**
    * The content type id for folder. This is used to cache the content type.
    * Init to <code>-1</code>, lazy assign to real number when needed.
    */
   private static long m_folderContentTypeId = -1;

   /**
    * Item definition for "Folder" content type. It is used to cache the content
    * type. Initialize to <code>null</code>. Lazy assigned when needed later.
    * Never <code>null</code> after that. Assume the folder content type never
    * change since it is used by the system, should not be modified by end user.
    */
   private PSItemDefinition m_folderItemDef = null;

   private static Logger log = LogManager.getLogger(PSServerFolderProcessor.class);
   
   /**
    * The beginning part of a folder path to the root.
    */
   private static final String ROOT_PATH_START = "/" + PSFolder.ROOT_TITLE;

   /**
    * Folder content type.
    */
   private static final String FOLDER_CONTENTTYPE = "Folder";

   /**
    * Field name for the folder item
    */
   private static final String NAME = "sys_title";

   private static final String COMMUNITYID = "sys_communityid";

   private static final String DESCRIPTION = "description";

   private static final String LOCALE = "sys_lang";

   private static final String CHILD_NAME_PROPERTIES = "properties";

   /**
    * Field name for the folder property name field.
    */
   public static final String PROP_NAME = "propertyName";

   /**
    * Field name for the folder property value field.
    */
   public static final String PROP_VALUE = "propertyValue";

   private static final String PROP_DESC = "propertyDescription";

   private static final String CHILD_NAME_ACL = "acl";

   private static final String ACL_TYPE = "type";

   private static final String ACL_NAME = "name";

   private static final String ACL_PERMISSIONS = "permissions";

   /**
    * private constants for table and column names
    */
   private static final int DESCRIPTION_MAX = 255;

   private static final int NAME_MAX = 100;

   private static final int PROP_NAME_MAX = 50;

   private static final int PROP_VALUE_MAX = 4000;

   private static final int PROP_DESC_MAX = 255;
   
   public static final String CROSS_SITE_LINK_QUEUE_THRESHOLD = "crossSiteLinkQueueThreshold";
   
   private static final PSServerFolderProcessor instance = new PSServerFolderProcessor();
   
   private boolean thread_context;
   
}
