/******************************************************************************
 *
 * [ PSSiteFolderCListBulk.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.sfp;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSqlHelper;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates Rhythmyx content list XML for all content items contained within
 * a specified site folder, or its child folders.  Determines which variants to
 * publish for each content type for the given site by referencing a lookup
 * table.  Builds delivery location paths by concatenating an item's ancestor
 * folders' names.
 * <p>
 * This class collects all items for a given sites, then send a (batch) SQL 
 * statement to the database, let the database figuring out the published items.
 */
public class PSSiteFolderCListBulk extends PSSiteFolderCListBase
{
   /**
    * Constructs a site-folder-driven, full-publishing, public-items
    * content list builder.
    *
    * @param request the current request context, used to obtain request
    * parameters, logging, and making internal requests. Not <code>null</code>.
    */
   public PSSiteFolderCListBulk(IPSRequestContext request)
   {
      super(request);
   }

   /**
    * Constructs a site-folder-driven content list builder.  Whether the content
    * list is full or incremental,
    *
    * Construct a site folder content list builder that will use the specified
    * request for obtaining request parameters, logging, and making internal
    * requests.
    *
    * @param request the current request context, used to obtain request
    * parameters, logging, and making internal requests. Not <code>null</code>.
    * @param isIncremental <code>true</code> to generate an incremental
    * publishing content list, <code>false</code> for full publishing
    * @param publishableContentValidValues comma-delimited list of contentvalid
    * values for workflow states that are eligible for publishing. Never
    * <code>null</code> or empty.
    * @param maxRowsPerPage enables pagination mode by determining the maximum
    * number of content items to appear in a single page of the content list.
    * Use a value of <code>-1</code> to disable pagination mode
    * (unlimited number of items)
    */
   public PSSiteFolderCListBulk(
      IPSRequestContext request,
      boolean isIncremental,
      String publishableContentValidValues,
      int maxRowsPerPage)
   {
      super(request, isIncremental, publishableContentValidValues,
            maxRowsPerPage);
   }

   /**
    * Constructs a site-folder-driven content list builder. Whether the content
    * list is full or incremental,
    * <p>
    * Construct a site folder content list builder that will use the specified
    * request for obtaining request parameters, logging, and making internal
    * requests.
    * <p>
    * @param request
    *           the current request context, used to obtain request parameters,
    *           logging, and making internal requests. Not <code>null</code>.
    * @param isIncremental
    *           <code>true</code> to generate an incremental publishing
    *           content list, <code>false</code> for full publishing
    * @param publishableContentValidValues
    *           comma-delimited list of contentvalid values for workflow states
    *           that are eligible for publishing. Never <code>null</code> or
    *           empty.
    * @param contentResourceName
    *           the name of the application resource used for looking up the
    *           items and its valid variants. Must be supplied in the form
    *           &lt;ApplicationName&gt;/&lt;ResourceName&gt;. It default to
    *           {@link #GET_SFP_CONTENT_LIST}if <code>null</code> or empty.
    * @param maxRowsPerPage
    *           the maximum number of items to publish on a page in the content
    *           list.
    * @param protocol the URL protocol to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           never <code>null</code> or empty
    * @param port the port number to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param paramSetToPass
    *           Set of non-standard HTML parameters to pass from request context
    *           to each content item url in the content list, must not be
    *           <code>null</code>, may be empty.
    */
   public PSSiteFolderCListBulk(IPSRequestContext request,
         boolean isIncremental,
         String publishableContentValidValues,
         String contentResourceName,
         int maxRowsPerPage,
         String protocol, String host, String port,
         Set paramSetToPass)
   {
      super(request, isIncremental, publishableContentValidValues,
            contentResourceName, maxRowsPerPage, protocol, host, port,
            paramSetToPass);
   }


   // implement the abstract method generateContentItems()
   protected void generateContentItems(String parentPath, int folderId,
         String siteFolderPath, String filenameContext, boolean appendFolderName)
         throws PSUnknownNodeTypeException, PSCmsException,
         PSExtensionProcessingException
   {
      Map<Integer, Set<ParentFolder>> siteItems = new HashMap<>();

      /*
       * Assume the root folder is being published, so the path starts with "/".
       * If this exit is extended to publish subfolders, the parentFolderPath
       * should be initialized to the path of this folder's parent.
       */
      appendFolderItems(parentPath, folderId, siteFolderPath, filenameContext,
            appendFolderName, false, siteItems);

      generateContentItems(filenameContext, siteItems);
   }

   /**
    * The resource name used to catalog the content items, see
    * {@link #GET_SFP_CONTENT_LIST}for detail
    * 
    * @return the default resource name, never <code>null</code> or empty.
    */
   protected String getDefaultResource()
   {
      return GET_SFP_CONTENT_LIST;
   }
   
   /**
    * Appends to the specified parent element one or more
    * <code>&lt;contentitem></code> child elements for each content item
    * contained in the specified folder (and subfolders).
    * 
    * @param parentFolderPath
    *           the parent folder path, assume not <code>null</code> or empty.
    *           this path is relative to the <code>siteFolderPath</code>. It is
    *           <code>/</code> if the <code>folderId</code> is the id of the
    *           site folder itself or it is an immediate sub-folder of the site.
    *           It is <code>/Files/<code> if <code>folderId</code> is a folder
    *           under <code>siteFolderPath/Files</code>.
    * @param folderId
    *           the to be processed folder id.
    * @param siteFolderPath
    *           the site folder path, assumed not <code>null</code> or empty.
    * @param filenameContext
    *           the file name context, assumed not <code>null</code> or empty.
    * 
    * @param appendFolderName
    *           determines if the folder name will be appended to the path used
    *           in location schemes: true to add the name, false to ignore the
    *           name. Generally, only false on the first call to this method.
    * @param includeOverride
    *           indicating that the folder should be included, this is used to
    *           force inclusion of all child folders of a folder that is flagged
    *           and the include mode is "flagged"
    * @param siteItems
    *           it is used to collect all child items for the specified folder.
    *           The map keys are content ids as <code>Integer</code> objects;
    *           the map values are <code>ParentFolder</code> objects. Assume
    *           never <code>null</code>, may be empty.
    * 
    * @throws PSUnknownNodeTypeException
    * 
    * @throws com.percussion.cms.PSCmsException
    *            propagated from the relationship engine when an exception
    *            occurs fetching the folder's children.
    * @throws PSExtensionProcessingException
    */
   private void appendFolderItems(
      String parentFolderPath,
      int folderId,
      final String siteFolderPath,
      final String filenameContext,
      boolean appendFolderName,
      boolean includeOverride,
      Map<Integer, Set<ParentFolder>> siteItems)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {

      m_log.debug(
         "NEW FOLDER. Path="
         + parentFolderPath
         + ", folderId="
         + folderId
         + ", Site folder path="
         + siteFolderPath
         + ", context="
         + filenameContext);

      String folderPath;
      if (appendFolderName)
      {
         StringBuilder folderPathBuf = new StringBuilder();
         folderPathBuf.append(parentFolderPath);
         folderPathBuf.append(m_folderProcessor.getPubFileName(folderId));
         folderPathBuf.append(PSSite.SITE_PATH_SEPARATOR);
         folderPath = folderPathBuf.toString();
      }
      else
      {
         folderPath = parentFolderPath;
      }

      m_log.debug("Processing: " + folderPath);

      boolean bExclude = !includeOverride && isFolderExcluded(folderId);
      boolean bOverrideInclude = false;
      if(bExclude)
         m_log.debug("Excluding: " + folderPath);
      if(bExclude && m_folderIncludeMode.equals(INCLUDE_MODE_UNFLAGGED))
      {
         // We can just stop here since all child folders are excluded
         // This avoids further recursion and help performance.
         return;
      }
      else if (!bExclude && m_folderIncludeMode.equals(INCLUDE_MODE_FLAGGED))
      {
         //Force include for all child folders
         bOverrideInclude = true;
      }
      PSLocator folderLocator = new PSLocator(folderId);
      PSRelationshipSet rs = m_relProcessor.getDependents(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT, folderLocator,
            PSRelationshipConfig.FILTER_TYPE_COMMUNITY);
      Iterator itRelationships = rs.iterator();
      PSRelationship rel;
      while (itRelationships.hasNext())
      {
         rel = (PSRelationship) itRelationships.next();
         int dependentId = rel.getDependent().getId();
         if (rel.getDependentObjectType() == PSCmsObject.TYPE_ITEM)
         {
            if (!bExclude) // collect the item
            {
               Integer depId = new Integer(dependentId);
               ParentFolder parentFolder = new ParentFolder(folderId,
                     folderPath);
               Set<ParentFolder> pFolderSet = null;
               if(siteItems.get(depId)== null)
               {
                  pFolderSet = new HashSet<>();
               }
               else
               {
                  pFolderSet = siteItems.get(depId);
               }
               pFolderSet.add(parentFolder);
               siteItems.put(depId, pFolderSet);
            }
         }
         else if (rel.getDependentObjectType() == PSCmsObject.TYPE_FOLDER)
         {
            appendFolderItems(
               folderPath,
               dependentId,
               siteFolderPath,
               filenameContext,
               true,
               bOverrideInclude,
               siteItems);
         }
      }
      m_request.setPrivateObject(PSSite.SITE_PATH_NAME, null);
   }

   /**
    * Generates the content list from the supplied items and context parameters.
    * 
    * @param siteItems
    *           a list of items, collected by
    *           {@link #appendFolderItems(String, int, String, String, boolean, boolean, Map)}.
    *           Assume never <code>null</code>, but be empty.
    * @param location_context
    *           the (file) location context, which is default to he value of 
    *           {@link IPSHtmlParameters#SYS_CONTEXT}. Assume not 
    *           <code>null</code>.
    * 
    * @throws PSExtensionProcessingException
    *            if cannot find resource.
    * @throws PSCmsException
    *            if other error occurs.
    */
   private void generateContentItems(String location_context, Map siteItems)
         throws PSExtensionProcessingException, PSCmsException
   {
      // get the max number of ids that can be used in the IN clause
      // default to 500. MS SQL server seems perform better with 500. 
      // MS SQL server's performance getting worse with bigger numbers.
      int maxIdLength = 500;
      PSDatabasePool dbPool = PSDatabasePool.getDatabasePool();
      boolean isOracle = PSSqlHelper.isOracle(dbPool.getDefaultDriver());
      if (isOracle)
         maxIdLength = PSSqlHelper.MAX_IN_CLAUSE_4_ORACLE;

      // generate the content items by groups, each group contains up to
      // "maxIdLength" items.
      StringBuilder idBuffer = new StringBuilder();
      Iterator ids = siteItems.keySet().iterator();
      Integer id;
      int collected = 0;

      while (ids.hasNext())
      {
         if (collected < maxIdLength) // collect the item
         {
            id = (Integer)ids.next();
            if (collected > 0)
               idBuffer.append(",");
            idBuffer.append(String.valueOf(id));
            collected++;
         }
         else // generate the content list items that is stored in "idBuffer"
         {
            generateContentItems(location_context, idBuffer.toString(),
                  siteItems);
            // reset for the next round
            idBuffer = new StringBuilder();
            collected = 0;
         }
      }

      // finish up remaining collected items
      if (collected > 0)
         generateContentItems(location_context, idBuffer.toString(), siteItems);

      setLastPublicRevisions();
   }

   /**
    * lookup the last public revision for all items in quick-edit state
    * and set to the item objects.
    *
    * @throws PSCmsException if an error occurs
    */
   private void setLastPublicRevisions() throws PSCmsException
   {
      if (m_quickEditCList.isEmpty())
         return;  // do nothing if there is no items in quick-edit state

      List<Integer> idList = new ArrayList<>(m_quickEditCList.keySet());
      Map<Integer, Integer> fixupItems = null;
      try
      {
         fixupItems = PSMacroUtils.getLastPublicRevisions(idList);
      }
      catch (PSException e)
      {
         throw new PSCmsException(IPSServerErrors.EXCEPTION_NOT_CAUGHT, e
               .toString());
      }
      Iterator entries = fixupItems.entrySet().iterator();
      Map.Entry entry;
      Integer contentid, revision;
      PSContentListItem item;
      while (entries.hasNext())
      {
         entry = (Map.Entry) entries.next();
         contentid = (Integer) entry.getKey();
         revision = (Integer) entry.getValue();
         if (revision != null)
         {
            Iterator itemIter = ((Set) m_quickEditCList.get(contentid)).iterator();
            while(itemIter.hasNext())
            {
               item = (PSContentListItem)itemIter.next();
               item.setLastPublicRevision(revision.toString());
            }
         }
      }
   }

   /**
    * Generates the content list items from the supplied parameters.
    * @param sys_context
    *           the value of {@link IPSHtmlParameters#SYS_CONTEXT}. Assume not
    *           <code>null</code>.
    * @param contentIds
    *           a list of content ids, separated by comma. Assume not
    *           <code>null</code> or empty.
    * @param location_context
    *           the (file) location context, which is default to the value of 
    *           {@link IPSHtmlParameters#SYS_CONTEXT}. Assumed not 
    *           <code>null</code>.
    * @param siteItems
    *           it maps content id (as the map key) to its parent folder (as the
    *           map value). The map keys are <code>Integer</code> objects; the
    *           map values are <code>ParentFolder</code> objects. Assume never
    *           <code>null</code>, but be empty.
    *
    * @throws PSExtensionProcessingException
    *            if cannot find resource.
    * @throws PSCmsException
    *            if other error occurs.
    */
   private void generateContentItems(String location_context, String contentIds,
         Map siteItems)
         throws PSExtensionProcessingException, PSCmsException
   {
      // set required parameters.
      Map<String, String> lookupParams = new HashMap<>(6);
      lookupParams.put(IPSHtmlParameters.SYS_SITEID,
            m_request.getParameter(IPSHtmlParameters.SYS_SITEID));
      // must use IPSHtmlParameters.SYS_CONTEXT to lookup the publishable items
      lookupParams.put(IPSHtmlParameters.SYS_CONTEXT, m_request.getParameter(
            IPSHtmlParameters.SYS_CONTEXT, location_context));
      lookupParams.put(IPSHtmlParameters.SYS_CONTENTID, contentIds);
      lookupParams.put(CONTENTVALID_PARAM, getContentValidValues4SQL());
      if (m_isIncremental)
         lookupParams.put(IS_FULL_PUBLISH, "false");
      else
         lookupParams.put(IS_FULL_PUBLISH, "true");

      IPSInternalRequest ir = m_request.getInternalRequest(
            m_contentResourceName, lookupParams, false);
      
      boolean includeQEItems = doesIncludeQuickEditItems();
      
      if (ir == null)
         throw new PSExtensionProcessingException(
               IPSCmsErrors.REQUIRED_RESOURCE_MISSING,
               m_contentResourceName);
      try
      {
         ResultSet rs = ir.getResultSet();

         int contentId, revision, contentTypeId, variantId;
         Date lastModifiedDate, expiryDate;
         String lastModifier, variantUrl, title;
         ParentFolder parentFolder;
         PSContentListItem item;
         boolean getLastPubRev4QEState;
         char stateFlag;

         while (rs.next())
         {
            // retrieve the data from current row
            contentId = rs.getInt(1);
            revision = rs.getInt(2);
            title = rs.getString(3);
            contentTypeId = rs.getInt(4);
            lastModifiedDate = rs.getDate(5);
            expiryDate = rs.getDate(6);
            lastModifier = rs.getString(7);
            stateFlag = Character.toLowerCase(rs.getString(8).charAt(0));
            variantId = rs.getInt(9);
            variantUrl = rs.getString(10);

            if (includeQEItems)
               getLastPubRev4QEState = 
                  stateFlag == PSContentListItem.CONTENTVALID_FLAG_I;
            else
               getLastPubRev4QEState = false;

            // create the content list item
            Iterator pfIter = ((Set)siteItems
            .get(new Integer(contentId))).iterator();
            while(pfIter.hasNext())
            {
               parentFolder = (ParentFolder) pfIter.next();
               
               item = new PSContentListItem(
                     String.valueOf(contentId),
                     String.valueOf(revision),
                     String.valueOf(variantId),
                     variantUrl,
                     title,
                     String.valueOf(parentFolder.m_folderId),
                     lastModifiedDate,
                     expiryDate,
                     lastModifier,
                     String.valueOf(contentTypeId),
                     location_context,
                     parentFolder.m_folderPath,
                     m_generator,
                     "no",
                      m_protocol, m_host, m_port,
                     m_paramSetToPass,
                     getLastPubRev4QEState);
   
               m_contentList.addItem(item);
               // stateFlag is in lower case character
               if (getLastPubRev4QEState)
               {
                  Set<PSContentListItem> itemSet = null;
                  if (m_quickEditCList.get(new Integer(contentId)) == null)
                  {
                     itemSet = new HashSet<>();
                  }
                  else
                  {
                     itemSet = m_quickEditCList.get(new Integer(contentId));
                  }
                  itemSet.add(item);
                  m_quickEditCList.put(new Integer(contentId), itemSet);
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new PSCmsException(
               IPSServerErrors.EXCEPTION_NOT_CAUGHT, e.toString());
      }
      finally
      {
         ir.cleanUp();
      }

   }

   /**
    * A container class for a folder id and its path.
    */
   private class ParentFolder
   {
      /**
       * Constructs an object from the supplied id and path.
       *
       * @param folderId the id of a folder.
       * @param folderPath the path of the folder. Assume not <code>null</code>.
       */
      ParentFolder(int folderId, String folderPath)
      {
         m_folderId = folderId;
         m_folderPath = folderPath;
      }

      /**
       * The folder path, assume never <code>null</code>. Init by ctor.
       */
      String m_folderPath;

      /**
       * The content id of a folder.
       */
      int m_folderId;
   }

   /**
    * A list of items in quick-edit state are contained in
    * {@link #m_contentList}. The map keys are content ids as
    * <code>Integer</code> objects, which map to the related
    * {@link PSContentListItem} set. Never <code>null</code>.
    */
   private Map<Integer, Set<PSContentListItem>> m_quickEditCList = new HashMap<>();

   /**
    * Name of the Rhythmyx internal resource used to catalog the published
    * items for FULL and INCREMENTAL site folder publishing.
    * <p>
    * The required HTML parameter of the query resource for FULL publishing:
    * <TABLE BORDER="1">
    *<TR><TH>PSXSingleParam</TH><TH>description</TH></TR>
    * <TR><TD>isFullPublish</TD><TD>true</TD></TR>
    * <TR><TD>sys_siteid</TD><TD>The published site id</TD></TR>
    * <TR><TD>sys_contentid</TD><TD>Content ids with comma as delimiter</TD></TR>
    * <TR><TD>valid</TD><TD>The published state flags with comma as the delimiter</TD></TR>
    * </TABLE>
    * <p>
    * The required HTML parameter of the resource for INCREMENTAL publishing:
    * <TABLE BORDER="1">
    *<TR><TH>PSXSingleParam</TH><TH>description</TH></TR>
    * <TR><TD>isFullPublish</TD><TD>false</TD></TR>
    * <TR><TD>sys_context</TD><TD>The context value</TD></TR>
    * <TR><TD>sys_siteid</TD><TD>The published site id</TD></TR>
    * <TR><TD>sys_contentid</TD><TD>Content ids with comma as delimiter</TD></TR>
    * <TR><TD>valid</TD><TD>The published state flags with comma as the delimiter</TD></TR>
    * </TABLE>
    * <p>
    * It returns the result set with the following columns:
    *<TR><TH>Table name</TH><TH>Column name</TH></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CONTENTID</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CURRENTREVISION</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>TITLE</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CONTENTTYPEID</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CONTENTLASTMODIFIEDDATE</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CONTENTEXPIRYDATE</TD></TR>
    * <TR><TD>CONTENTSTATUS</TD><TD>CONTENTLASTMODIFIER</TD></TR>
    * <TR><TD>STATES</TD><TD>CONTENTVALID</TD></TR>
    * <TR><TD>PSX_TEMPLATE</TD><TD>TEMPLATE_ID</TD></TR>
    * <TR><TD>PSX_TEMPLATE</TD><TD>ASSEMBLYURL</TD></TR>
    */
   static final String GET_SFP_CONTENT_LIST =
      "rx_supportSiteFolderContentList/getSFPContentList";

   /**
    * The HTML parameter name, set to "true" for FULL publishing; otherwise
    * set to "false".
    */
   private static final String IS_FULL_PUBLISH = "isFullPublish";
}
