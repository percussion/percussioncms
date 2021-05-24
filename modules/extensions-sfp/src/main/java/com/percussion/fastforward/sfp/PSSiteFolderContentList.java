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
package com.percussion.fastforward.sfp;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generates Rhythmyx content list XML for all content items contained within a
 * specified site folder, or its child folders. Determines which variants to
 * publish for each content type for the given site by referencing a lookup
 * table. Builds delivery location paths by concatenating an item's ancestor
 * folders' names.
 * 
 * @deprecated This Exit may have poor performance poorly with large amount of 
 *             items. Use {@link PSSiteFolderCListBulk} instead.
 * @author James Schultz
 */
public class PSSiteFolderContentList extends PSSiteFolderCListBase
{
   /**
    * Constructs a site-folder-driven, full-publishing, public-items
    * content list builder.
    *
    * @param request the current request context, used to obtain request
    * parameters, logging, and making internal requests. Not <code>null</code>.
    */
   public PSSiteFolderContentList(IPSRequestContext request)
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
    * values for workflow states that are eligible for publishing
    * @param maxRowsPerPage enables pagination mode by determining the maximum
    * number of content items to appear in a single page of the content list.
    * Use a value of <code>-1</code> to disable pagination mode
    * (unlimited number of items)
    */
   public PSSiteFolderContentList(
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
    * 
    * Construct a site folder content list builder that will use the specified
    * request for obtaining request parameters, logging, and making internal
    * requests.
    * 
    * @param request the current request context, used to obtain request
    *           parameters, logging, and making internal requests. Not
    *           <code>null</code>.
    * @param isIncremental <code>true</code> to generate an incremental
    *           publishing content list, <code>false</code> for full
    *           publishing
    * @param publishableContentValidValues comma-delimited list of contentvalid
    *           values for workflow states that are eligible for publishing
    * @param contentVariantResourceName the name of the application resource
    *           used for looking up the valid variants for this content item.
    *           Must be supplied in the form
    *           &lt;ApplicationName&gt;/&lt;ResourceName&gt;
    * @param maxRowsPerPage the maximum number of items to publish on a page in
    *           the content list.
    * @param protocol the URL protocol to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           never <code>null</code> or empty
    * @param port the port number to use when creating content URLs, never
    *           <code>null</code> or empty
    * @param paramSetToPass Set of non-standard HTML parameters to pass from
    *           request context to each content item url in the content list,
    *           must not be <code>null</code>, may be empty.
    */
   public PSSiteFolderContentList(IPSRequestContext request,
         boolean isIncremental,
         String publishableContentValidValues,
         String contentVariantResourceName, 
         int maxRowsPerPage,
         String protocol, String host, String port,
         Set paramSetToPass)
   {
      super(request, isIncremental, publishableContentValidValues,
            contentVariantResourceName, maxRowsPerPage,
            protocol, host, port,
            paramSetToPass);
   }

   // implement the abstract method generateContentItems()
   protected void generateContentItems(String parentPath, int folderId,
         String siteFolderPath, String filenameContext, boolean appendFolderName)
         throws PSUnknownNodeTypeException, PSCmsException,
         PSExtensionProcessingException
   {
      PSComponentSummary root = m_helper.getComponentSummary(new PSLocator(
            folderId));
      
      appendFolderItems(
            parentPath,
            root,
            siteFolderPath,
            filenameContext,
            appendFolderName,
            false);

   }
   
   /**
    * The resource name used to catalog variant ids, see
    * {@link #LOOKUP_VARIANTS_SITE_ITEM}for detail
    * 
    * @return the default resource name, never <code>null</code> or empty.
    */
   protected String getDefaultResource()
   {
      return LOOKUP_VARIANTS_SITE_ITEM;
   }
   
   /**
    * Appends to the specified parent element one or more
    * <code>&lt;contentitem></code> child elements for each content item
    * contained in the specified folder (and subfolders).
    * @param parentFolderPath
    *           the parent folder path, assume not <code>null</code> or empty.
    *           this path is relative to the <code>siteFolderPath</code>. It is
    *           <code>/</code> if the <code>folderId</code> is the id of the
    *           site folder itself or it is an immediate sub-folder of the site.
    *           It is <code>/Files/<code> if <code>folderId</code> is a folder
    *           under <code>siteFolderPath/Files</code>.
    * @param folder
    * @param siteFolderPath
    * @param filenameContext
    *
    * @param appendFolderName determines if the folder name will be appended
    * to the path used in location schemes:  true to add the name, false to
    * ignore the name.  Generally, only false on the first call to this method.
    * @param includeOverride indicating that the folder should be included, this
    * is used to force inclusion of all child folders of a folder that is 
    * flagged and the include mode is "flagged" 
    * 
    * @throws PSUnknownNodeTypeException
    *
    * @throws com.percussion.cms.PSCmsException propagated from the relationship engine when an
    * exception occurs fetching the folder's children.
    * @throws PSExtensionProcessingException
    */
   private void appendFolderItems(
      String parentFolderPath,
      PSComponentSummary folder,
      final String siteFolderPath,
      final String filenameContext,
      boolean appendFolderName,
      boolean includeOverride)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {

      log.debug("NEW FOLDER. Path={}, Folder={}, Site folder path={}, context={}", parentFolderPath, folder.getName(), siteFolderPath, filenameContext);
      log.debug("Folder id={}", folder.getLocator().getPart(PSLocator.KEY_ID));
   
      String folderPath;
      if (appendFolderName)
      {
         StringBuffer folderPathBuf = new StringBuffer();
         folderPathBuf.append(parentFolderPath);
         folderPathBuf.append(
            PSSite.getFolderFileName(folder.getCurrentLocator()));
         folderPathBuf.append(PSSite.SITE_PATH_SEPARATOR);
         folderPath = folderPathBuf.toString();
      }
      else
      {
         folderPath = parentFolderPath;
      }

      log.debug("Processing: {}", folderPath);
      
      boolean bExclude = !includeOverride
            && isFolderExcluded(folder.getContentId());
      boolean bOverrideInclude = false;
      if(bExclude)
         log.debug("Excluding: {}", folderPath);
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
      Iterator items =
         m_helper.getFolderContents(folder.getCurrentLocator()).iterator();
      while (items.hasNext())
      {
         PSComponentSummary item = (PSComponentSummary) items.next();
         if (item.getType() == PSComponentSummary.TYPE_ITEM)
         {
            if(!bExclude)
               appendItemVariants(folderPath, item, folder, filenameContext);
         }
         else if (item.getType() == PSComponentSummary.TYPE_FOLDER)
         {
            appendFolderItems(
               folderPath,
               item,
               siteFolderPath,
               filenameContext,
               true,
               bOverrideInclude);
         }
      }
      m_request.setPrivateObject(PSSite.SITE_PATH_NAME, null);
   }
   
   /**
    * If the item is in a publishable state, a <code>&lt;contentitem></code>
    * element will be appended to the specified parent element for each
    * publishable variant registered for the current site
    * (in the <code>PSX_VARIANT_SITE</code> repository table).
    * @param folderPath
    * @param item
    *
    * @param folder summary of the folder than contains the item, not
    * null
    * @param filenameContext
    */
   private void appendItemVariants(
      String folderPath,
      PSComponentSummary item,
      final PSComponentSummary folder,
      final String filenameContext)
   {
      // extract the contentstatus information for this item
      PSLocator itemCurrentRevision = item.getCurrentLocator();
      String contentId = Integer.toString(itemCurrentRevision.getId());
      String revision = Integer.toString(itemCurrentRevision.getRevision());
      String contentTypeId = Long.toString(item.getContentTypeId());
      boolean isPublishable = false;
      try
      {
         isPublishable =
            PSCms.isPublishable(
               new Object[] {
                  m_publishableContentValidValues,
                  contentId,
                  revision },
               m_request);
         log.debug("isPublishable {} ? {}", contentId, isPublishable);
      }
      catch (PSCmsException e)
      {
         // the check failed; assume item is not publishable
         log.error("ERROR: while checking contentvalid status");
         log.error(getClass().getName(), e);
         log.debug(e.getMessage(), e);
      }

      if (isPublishable)
      {
         // lookup the publishable variants for this item's site/item
         Set variants = lookupVariantsForItem(contentId, revision, folder,
               filenameContext, contentTypeId);
         PSContentListItem contentListItem;

         if (variants.isEmpty())
         {
            /* only include items with no publishable variants in full publish
               (for debugging) */
            if (m_debugModeOn)
            {
               log.debug("including {} without variants for debugging", contentId);
               contentListItem =
                  generateListItem(
                     contentId,
                     revision,
                     item,
                     null,
                     folder,
                     filenameContext,
                     folderPath,
                     contentTypeId);
               m_contentList.addItem(contentListItem);
            }
         }
         else
         {
            // for each variant, generate a contentitem element
            for (Iterator i = variants.iterator(); i.hasNext();)
            {
               Variant variant = (Variant) i.next();

               contentListItem =
                  generateListItem(
                     contentId,
                     revision,
                     item,
                     variant,
                     folder,
                     filenameContext,
                     folderPath,
                     contentTypeId);

               /* todo: add a mechanism for contenttype specific additions,
                  like adding an article's page count */

               if (m_isIncremental)
               {
                  if (isValid(contentListItem))
                     m_contentList.addItem(contentListItem);
                  else
                     log.debug("this variant/location of {} does not need to be incrementally published", contentId);
               }
               else
               {
                  // always add items when full publish
                  m_contentList.addItem(contentListItem);
               }
            }
         }
      }

   }
   
   /**
    * Checks RXSITEITEMS to see if this item/variant/location has already
    * been published to the current request context/site.  This method is an
    * adaptor; it extracts the relevent values from <code>contentItem</code> and
    * passes them to <code>PSIncrementalContentFilter.isValid()</code> method.
    * @param contentItem the item to be checked, assumed not <code>null</code>
    * @return <code>true</code> if the item should be incrementally published
    * (because it has not yet been published); <code>false</code> otherwise
    */
   private boolean isValid(PSContentListItem contentItem)
   {
      String contentid = contentItem.getContentId();
      String variantid = contentItem.getVariantId();
      String pubOpAttr = contentItem.getUnpublishValue();
      String pubOperation;
      if (pubOpAttr != null && pubOpAttr.equalsIgnoreCase("yes"))
      {
         pubOperation = "unpublish";
      }
      else
      {
         pubOperation = "publish";
      }
      String pubDate = contentItem.getLastModifiedDateAsString();
      String location = contentItem.getDeliveryLocation(m_request);
      String requestContext =
         m_request.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      String requestSiteId =
         m_request.getParameter(IPSHtmlParameters.SYS_SITEID);

      return PSUtils.isValid(
         m_request,
         INCREMENTAL_FILTER_REQUEST_NAME,
         contentid,
         variantid,
         requestContext,
         requestSiteId,
         pubOperation,
         pubDate,
         location);

   }

   /**
    * Builds the contentitem element of the content list from the supplied
    * parameters.
    *
    * @param contentId
    * @param revision
    * @param item
    * @param variant
    * @param folder
    * @param filenameContext
    * @param folderPath
    * @param contentTypeId
    * @return
    */
   private PSContentListItem generateListItem(
      String contentId,
      String revision,
      PSComponentSummary item,
      Variant variant,
      final PSComponentSummary folder,
      String filenameContext,
      String folderPath,
      String contentTypeId)
   {
      String variantId = null;
      String variantBase = null;
      if (variant != null)
      {
         variantId = variant.getId();
         variantBase = variant.getAssemblerBase();
      }

      String folderId = null;
      if (folder != null)
      {
         folderId = Integer.toString(folder.getCurrentLocator().getId());
      }

      return new PSContentListItem(
         contentId,
         revision,
         variantId,
         variantBase,
         item.getName(),
         folderId,
         item.getContentLastModifiedDate(),
         item.getContentExpiryDate(),
         item.getContentLastModifier(),
         contentTypeId,
         filenameContext,
         folderPath,
         m_generator,
         "no",
         m_protocol, m_host, m_port,
         m_paramSetToPass,
         doesIncludeQuickEditItems());
   }

   /**
    * Gets the set of variants registered for the specified content item and
    * for the site being published to, by making an internal request to query
    * the <code>PSX_VARIANT_SITE</code> table.
    * @param contentId
    * @param revision
    * @param folder
    * @param deliveryLocationContext
    * @param contentTypeId
    *
    * @return Never <code>null</code> but will be empty if no variants are
    * registered for the content item in the current site, or if an error
    * occurs while performing the lookup.
    */
   private Set lookupVariantsForItem(
      String contentId,
      String revision,
      PSComponentSummary folder,
      String deliveryLocationContext,
      String contentTypeId)
   {
      if (contentId == null || contentId.trim().length() == 0)
         throw new IllegalArgumentException("contentId may not be null");

      Set variants = null;
      Map lookupParams = new HashMap(6);
      lookupParams.put(IPSHtmlParameters.SYS_SITEID,
            m_request.getParameter(IPSHtmlParameters.SYS_SITEID));
      lookupParams.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      lookupParams.put(IPSHtmlParameters.SYS_REVISION, revision);
      lookupParams.put(IPSHtmlParameters.SYS_CONTEXT, deliveryLocationContext);
      lookupParams.put(IPSHtmlParameters.SYS_CONTENTTYPEID, contentTypeId);
      if (folder != null)
      {
         /* include the folder content id as a parameter */
         int folderId = ((PSLocator) folder.getLocator()).getId();
         lookupParams.put(
               IPSHtmlParameters.SYS_FOLDERID,
            String.valueOf(folderId));
      }

      IPSInternalRequest lookupRequest =
         m_request.getInternalRequest(
            m_contentResourceName,
            lookupParams,
            false);
      if (lookupRequest == null)
      {
         log.error("Cannot find query resource: {}", m_contentResourceName);
      }
      else
      {
         try
         {
            Document results = lookupRequest.getResultDoc();
            variants = parseVariantLookupXML(results);
            log.debug("found {} variants for the site/item", variants.size());
         }
         catch (PSInternalRequestCallException e)
         {
            log.error("ERROR: while making internal request to {}", m_contentResourceName);
            log.error(getClass().getName(), e);
            log.debug(e.getMessage(), e);
            variants = new HashSet(); // never return a null
         }
      }

      return variants;
   }

   /**
    * Parses XML document to extract the variant ids from the following
    * structure:<br>
    * <pre><code>
    * &lt;!ELEMENT lookupVariantsBySite (variant*)>
    * &lt;!ELEMENT variant (#PCDATA)>
    * </code></pre>
    * @param resultXml the XML document to be parsed
    * @return a set of the <code>Variant</code> objects, extracted from the XML
    * document. Never <code>null</code> but will be empty if no variant
    * elements exist in document.
    */
   private Set parseVariantLookupXML(Document resultXml)
   {
      Set variantsSet = new HashSet();
      if (resultXml != null)
      {
         Element root = resultXml.getDocumentElement();
         if (root != null)
         {
            NodeList variants = root.getElementsByTagName("variant");
            Element variant;
            for (int i = 0;(variant = (Element) variants.item(i)) != null; i++)
            {
               String id = variant.getAttribute("id");
               String assembler = variant.getAttribute("assembler");
               if (id != null && assembler != null)
                  variantsSet.add(new Variant(id, assembler));
            }
         }
      }
      return variantsSet;
   }
   
   /**
    * Container for metadata about a particular variant
    */
   private class Variant
   {

      /**
       * Returns the variant id as the value with which to hash.
       *
       * @return the variant id
       */
      public int hashCode()
      {
         return Integer.parseInt(m_id);
      }

      /**
       * Two variants are equal if they have the same id and assembler base
       * @param o object to compare against this object
       * @return true if object is a variant and both variants have the same id
       * and assembler, false otherwise.
       */
      public boolean equals(Object o)
      {
         if (o instanceof Variant)
         {
            Variant v = (Variant) o;
            return v.getId().equals(m_id)
               && v.getAssemblerBase().equals(m_assemblerBase);
         }
         return false;
      }

      /**
       * Ctor that takes teh variantid and the assembly URL.
       * 
       * @param variantid variantid of the variant must not be <code>null</code>
       *           or empty.
       * @param assemblerBase Assembly URL for the variant, must notbe
       *           <code>null</code> or empty.
       */
      public Variant(String variantid, String assemblerBase)
      {
         if (variantid == null || variantid.trim().length() == 0)
            throw new IllegalArgumentException(
                  "variant id may not be null or empty");
         if (assemblerBase == null || assemblerBase.trim().length() == 0)
            throw new IllegalArgumentException(
                  "variant assemblerBase may not be null or empty");

         m_id = variantid;
         m_assemblerBase = assemblerBase;
      }

      /**
       * Gets the id of this variant
       * @return the variant id, never <code>null</code> or empty.
       */
      public String getId()
      {
         return m_id;
      }

      /**
       * Gets the base URL to the assembler resource for this variant
       * @return the assembler base URL, never <code>null</code> or empty.
       */
      public String getAssemblerBase()
      {
         return m_assemblerBase;
      }

      /**
       * Variant id of teh variant, initialized in the ctor and never
       * <code>null</code> or empty after that.
       */
      private String m_id;

      /**
       * Assembly url of the variant, initialized in the ctor and never
       * <code>null</code> or empty after that.
       */
      private String m_assemblerBase;
   }

   /**
    * Name of the Rhythmyx internal resource used to catalog variant ids.  This
    * query resource should accept contentid, revision, and siteid as parameters
    * and return all publishable variantids registered to those parameters.
    */
   static final String LOOKUP_VARIANTS_SITE_ITEM =
      "rx_supportSiteFolderContentList/lookupVariantsBySiteItem.xml";
   
   private static final String INCREMENTAL_FILTER_REQUEST_NAME =
      "rx_Support_pub/folder_itemstatus.xml";
   
   /**
    * Name of the request parameter that indicates the navigation theme
    */
   public static final String NAV_THEME_PARAM_NAME = "nav_theme";
   
}
