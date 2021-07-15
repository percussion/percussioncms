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
package com.percussion.fastforward.sfp;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The objectives of this extension are to filter the content list for
 * publishing by appending the content items/pages that were published earlier
 * and are:
 * <ol>
 * <li>Purged from the system</li>
 * <li>Removed from a folder</li>
 * <li>Moved from one folder to another</li>
 * </ol>
 * to the unpublish content list XML result document. It uses the following
 * algorithm to do the above:
 * <ol>
 * <li>Gets all previously published items/pages to the site by making an
 * internal request to a Rhythmyx resource. These are obtained as a result set
 * for performance reasons.
 * <li>Gets the parent folderids for each of the items. While doing so, it
 * assumes the item can exist in multiple folders.</li>
 * <li>It generates publish paths for each of the folders it exists in.</li>
 * <li>It filters out any item with publish path not matching with the
 * previously published location path.</li>
 * <li>It builds the content list document for these remaining items/pages by
 * making another internal request to a Rhythmyx resource.</li>
 * <li>Finally, this list is merged with the result document which is already
 * an unpublish content list document.</li>
 * </ol>
 * <p>
 * The exit takes one optional parameter to indicate if the publish path
 * comparison should be case sensitive. The default value for this is "no". It
 * assumes the DTD of the result document to be <em>contentlist.dtd</em> and
 * the content list being generated is for unpublishing.
 */
public class PSAppendPurgedOrMovedItems extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * See the class description.
    * 
    * @see IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      boolean caseSensitive = false;
      if(params!=null && params.length > 0)
      {
         String strCaseSensitive = params[0].toString().trim();
         if(strCaseSensitive.equalsIgnoreCase("yes"))
            caseSensitive = true;
      }
      List items = getSiteItems(request);
            
      //Collect all contentids into a set of strings
      Set contentidSet = new HashSet();
      Iterator iter = items.iterator();
      while (iter.hasNext())
      {
         PageData data = (PageData) iter.next();
         String contentid = data.getContentid("");
         if (contentid.length() != 0)
            contentidSet.add(contentid);
      }
      
      PSLocator siteRootLocator = getSiteRootLocator(request);
      PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
      //Get contentids of all folders each items exists in.
      Map cidFolderIdMap = getParentFolderIdMap(request,
            (String[]) contentidSet.toArray(new String[0]));
      // it maps contentId (String) to its parent folder path set (Set), which
      // is a set of locations that suppose to publish to.
      Map cidFolderPathsMap = new HashMap();
      PSSiteFolderContentListLinkGenerator linkGenerator = 
         new PSSiteFolderContentListLinkGenerator();
      int count = items.size();
      for (int i = count - 1; i >= 0; i--)
      {
         PageData data = (PageData) items.get(i);
         String contentid = data.getContentid("");
         if (contentid.length() == 0)
         {
            items.remove(data);
            continue;
         }
         String revision = data.getRevision("");
         String variantid = data.getVariantid("");
         String[] folderIds = (String[]) cidFolderIdMap.get(contentid);
         Set pathSet = new HashSet(folderIds.length);
         PSLocator locator;
         /*
          * Generate pub locations for each parent folder of the item and store
          * in a set
          */
         for (int j = 0; j < folderIds.length; j++)
         {
            // only process the folder under the site
            String folderid = folderIds[j];
            if (!isSiteFolder(siteRootLocator, folderid, processor))
               continue;
            
            String[] udfParams =
               {variantid, contentid, revision, null, null, folderid, null};
            try
            {
               String pubPath = (String) linkGenerator.getGenerator()
                     .processUdf(udfParams, request);
               if(pubPath!=null && pubPath.length() > 0)
               {
                  if(!caseSensitive)
                     pubPath = pubPath.toLowerCase();
                  pathSet.add(pubPath);
               }
            }
            catch (PSConversionException e)
            {
               throw new PSExtensionProcessingException(e.getErrorCode(), e
                     .getErrorArguments());
            }
         }
         /*
          * Remove the page/item if the location matches with one from the
          * generated
          */
         String location = data.getLocation("");
         if(!caseSensitive)
            location = location.toLowerCase();
         
         // is it one of the good/generated (or suppose published) locations 
         if (pathSet.contains(location)) 
         { 
            // remove the item if it was published to a good/generated location
            items.remove(data);
         }
         else
         {  // otherwise, this entry has a bad published location.
            // remember the good paths for the content id, which will be used
            // to do the similar filter later
            cidFolderPathsMap.put(contentid, pathSet);
         }
      }
      //Collect all contentids left in the page list into a set of strings now
      contentidSet = new HashSet();
      iter = items.iterator();
      while (iter.hasNext())
      {
         PageData data = (PageData) iter.next();
         String contentid = data.getContentid("");
         if (contentid.length() != 0)
            contentidSet.add(contentid);
      }

      if (!contentidSet.isEmpty())
      {
         appendContentList(resultDoc, request, contentidSet, cidFolderPathsMap,
               caseSensitive);
      }
      return resultDoc;
   }

   /**
    * Determines whether the supplied folder is under the given site.
    * 
    * @param siteRootLocator the locator of the site root, assumed not
    *    <code>null</code>.
    * @param sFolderId the id of the to be determined folder, assumed not
    *    <code>null</code>.
    * @param processor the folder processor, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the locator is a site folder; otherwise
    *    return <code>false</code>.
    * 
    * @throws PSExtensionProcessingException if error occurs.
    */
   private boolean isSiteFolder(PSLocator siteRootLocator, String sFolderId,
         PSServerFolderProcessor processor)
         throws PSExtensionProcessingException
   {
      PSLocator locator = null;
      try
      {
         int folderId = Integer.parseInt(sFolderId);
         if (folderId == siteRootLocator.getId())
            return true;
         
         locator = new PSLocator(folderId, 1);
         return processor.isDescendent("PSFolder", siteRootLocator, locator,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(
               "Failed to determine if folder (" + locator
                     + ") is a decendent of site ("
                     + siteRootLocator.toString() + ").", e);
      }
   }
   
   /**
    * Get the root locator of the site.
    * 
    * @param request the request context, the site id is specified by 
    *    {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID}. 
    *    Assumed not <code>null</code>.
    * 
    * @return the root locator of the site, never <code>null</code>.
    * 
    * @throws PSExtensionProcessingException if error occurs.
    */
   private PSLocator getSiteRootLocator(IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      String siteId = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      String siteRoot = PSSite.lookupFolderRootForSite(siteId, request);
      PSRelationshipProcessor processor = null;
      int siteFolderId;
      PSLocator siteLocator;
      try
      {
         processor = PSRelationshipProcessor.getInstance();
         siteFolderId = processor.getIdByPath(
               PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, siteRoot,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         return new PSLocator(siteFolderId, 1);
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException("Failed to get folder locator for site id=" + siteId, e);
      }
   }
   
  /**
   * Create unpublished content list from the supplied parameters and append 
   * the list to the supplied result document.
   * 
   * @param resultDoc the result document, assumed not <code>null</code>.
   * @param request request context used to make an internal request, assumed
   *           not <code>null</code>.
   * @param contentidSet set of contentids (as strings), assumed not
   *           <code>null</code>.
   * @param cidFolderPaths it maps contentid (as <code>String</code>) to a set
   *           of folder paths (as <code>Set</code> of <code>String</code>).
   *           The paths are lower case if the <code>caseSensitive</code> is
   *           <code>false</code>. Assumed not <code>null</code>.
   *           The folder paths are the suppose published locations. This is
   *           used to filter out the entries that are returned from 
   *           {@link #buildContentList(Set, IPSRequestContext)}, so that we
   *           filter out the good published entries and keep the bad published
   *           entries. 
   * @param caseSensitive <code>true</code> if the path is case sensitive.
   * 
   * @throws PSExtensionProcessingException if an error while executing a 
   *           request.
   */
   private void appendContentList(Document resultDoc,
         IPSRequestContext request, Set contentidSet, Map cidFolderPaths,
         boolean caseSensitive) throws PSExtensionProcessingException
   {
      Document purgedOrMovedDoc = buildContentList(contentidSet, request);
      if (purgedOrMovedDoc == null)
      {
         return;
      }
      
      NodeList nl = purgedOrMovedDoc
            .getElementsByTagName(IPSDTDPublisherEdition.ELEM_CONTENTITEM);
      int itemCount = nl.getLength();
      PSContentListItem citem;
      Element itemElem;
      String location;
      Set folderPaths;
      String contentId;
      Element root = resultDoc.getDocumentElement();
      for (int i = 0; i < itemCount; i++)
      {
         itemElem = (Element)nl.item(i);
         
         // check if the location of the item/entry is one of the good/generated
         // (or suppose published) locations. Skip this entry if it is; 
         // otherwise add it to the result doc.
         try
         {
            citem = new PSContentListItem(itemElem);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // should not happen here, log the error in case it does.
            ms_logger.error("Failed to create PSContentListItem", e);
            continue; // ignore the "bad" item
         }
         contentId = citem.getContentId();
         location = citem.getDeliveryLocation(request);
         if (!caseSensitive)
            location = location.toLowerCase();
         // get the good (suppose published) locations, which are collected 
         // previously. We are applying the same filtering as we did previously,
         // but this is to filter out the good (or suppose to published) entries
         // and keep the bad (purged or moved) entries.
         folderPaths = (Set) cidFolderPaths.get(contentId);
         if (folderPaths != null && (!folderPaths.contains(location)))
         {
            root.appendChild(resultDoc.importNode(itemElem, true));
         }
      }
   }
   
   /**
    * Helper method to build contentlist document for a given set of content
    * ids. Makes an internal request to a rhythmyx resource to build the
    * contentlist XML document.
    * <p>
    * Note, the returned list may contain more than one entries for each 
    * content id. In this case, the entries may contain both good locations 
    * (the suppose published locations) and bad locations (the moved or purged
    * locations).  
    * 
    * @param contentidSet set of contentids (as strings), assumed not
    *           <code>null</code>.
    * @param request request context used to make an internal request, assumed
    *           not <code>null</code>.
    * @return the content list XML document returned by the resource, may be
    *         <code>null</code>. In not <code>null</code>, will conform to
    *         contentlist.dtd.
    * @throws PSExtensionProcessingException if the resource for the internal
    *            request is missing or any error while executing the request.
    */
   private Document buildContentList(Set contentidSet, 
      IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      String resource = "rx_Support_pub/buildUnpub_clist";
      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_SITEID, request.getParameter(
            IPSHtmlParameters.SYS_SITEID, ""));
      params.put(IPSHtmlParameters.SYS_CONTEXT, request.getParameter(
            IPSHtmlParameters.SYS_CONTEXT, ""));
      params.put(IPSHtmlParameters.SYS_CONTENTID, new ArrayList(contentidSet));
      IPSInternalRequest irq = request.getInternalRequest(resource, params,
            false);
      if (irq == null)
      {
         //Fatal error
         String[] args =
         {resource, ""};
         throw new PSExtensionProcessingException(
               IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
      Document doc = null;
      try
      {
         doc = irq.getResultDoc();
      }
      catch (PSInternalRequestCallException e)
      {
         //Fatal error
         String[] args =
            {resource, e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
               IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
      return doc;
   }

   /**
    * Get all site items that have been successfully published and not
    * successfully unpublished as result set by making an internal request to a
    * Rhythmyx resource. The resource expects two parameters -
    * {@link com.percussion.util.IPSHtmlParameters#SYS_SITEID}and
    * {@link com.percussion.util.IPSHtmlParameters#SYS_CONTEXT}which are
    * assumed to exist in the original request context.
    * 
    * @param request request context, assumed not <code>null</code>.
    * @return list of <code>PageData</code> objects representing the site items
    *         which need to be unpublished, never <code>null</code>, may be
    *         empty.
    * @throws PSExtensionProcessingException if internal requet to Rhythmyx
    *            resource fails for any reason.
    */
   private List getSiteItems(IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      String resource = "rx_Support_pub/siteitem_clist";
      IPSInternalRequest irq = request.getInternalRequest(resource);
      if (irq == null)
      {
         //Fatal error
         String[] args =
            {resource, ""};
         throw new PSExtensionProcessingException(
               IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
      ResultSet rs = null;
      List items = new ArrayList();
      try
      {
         rs = irq.getResultSet();
         
         while (rs != null && rs.next())
         {
            String contentid = rs.getString("CONTENTID");
            String variantid = rs.getString("VARIANTID");
            String location = rs.getString("LOCATION");
            String revision = PSMacroUtils.getLastPublicRevision(contentid);
            items.add(new PageData(
                  contentid, revision, variantid, location));
         }
      }
      catch (PSInternalRequestCallException e)
      {
         //Fatal error
         String[] args =
            {resource, e.getLocalizedMessage()};
         throw new PSExtensionProcessingException(
               IPSCmsErrors.CMS_INTERNAL_REQUEST_ERROR, args);
      }
      catch (SQLException e)
      {
         //Should be very rare
         throw new PSExtensionProcessingException(m_def.getRef()
               .getExtensionName(), e);
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
               rs = null;
            }
            catch (SQLException e)
            {

            }
         }
         
         if (irq != null)
            irq.cleanUp();
      }
           
      return items;
   }

   /**
    * Build a map of contentid and parent folder ids for each of the supplied
    * content ids using relationship API.
    * 
    * @param request request context, assumed not <code>null</code>
    * 
    * @param cids array of contentid's as <code>String</code> objects for
    *           which the parent folder paths are being requested. assumed not
    *           <code>null</code>.
    * @return a map of contentid and parent folder paths. The key is the
    *         contentid as <code>String</code> and the value is a string array
    *         folder paths, which will never be <code>null</code> but may be
    *         empty. Never <code>null</code>, may be empty.
    * @throws PSExtensionProcessingException if the parent folder paths could
    *            not be obtained from server for any reason.
    */
   private Map getParentFolderIdMap(IPSRequestContext request, String[] cids)
         throws PSExtensionProcessingException
   {
      Map map = new HashMap();

      if(cids.length==0)
         return map; 

     
      try
      {
         PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();
         for (int i = 0; i < cids.length; i++)
         {
            String cid = cids[i];
            PSLocator locator = new PSLocator(cid);
            String[] paths = proxy.getRelationshipOwnerPaths(PSFolder
                  .getComponentType(PSFolder.class), locator,
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            map.put(cid, getFolderIdByPath(request, paths));
         }
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
      return map;
   }

   /**
    * Get array of content id of the folders given the array of full paths.
    * 
    * @param request request context, assumed not <code>null</code>.
    * @param paths array of full folder paths, assumed not <code>null</code>
    *           may be empty.
    * @return array of content ids of the folders specified by the paths as
    *         <code>String</code>s, never <code>null</code>, may be empty.
    * @throws PSExtensionProcessingException if it fails to get the id from the
    *            path for any reason.
    */
   private String[] getFolderIdByPath(IPSRequestContext request, String[] paths)
         throws PSExtensionProcessingException
   {
      String[] ids = new String[paths.length];
      if (paths.length == 0)
         return ids;

      PSRelationshipProcessor proc = null;
      try
      {
         proc = PSRelationshipProcessor.getInstance();
         for (int i = 0; i < paths.length; i++)
         {
            int id = proc.getIdByPath(
                  PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE, paths[i],
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            ids[i] = "" + id;
         }
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(e.getErrorCode(), e
               .getErrorArguments());
      }
      return ids;
   }
   
   /**
    * Inner class to hold some item/page specific data.
    */
   class PageData
   {
      /**
       * Content Id of the page, initialized in the ctor. May be
       * <code>null</code>. Never changed after that.
       */
      private String m_contentid = null;

      /**
       * Revision of the page initialized in the ctor. May be
       * <code>null</code>. Never changed after that.
       */
      private String m_revision;

      /**
       * variant Id of the page, initialized in the ctor. May be
       * <code>null</code>. Never changed after that.
       */
      private String m_variantid;

      /**
       * Publish location of the page, initialized in the ctor. May be
       * <code>null</code>. Never changed after that.
       */
      private String m_location;

      /**
       * Ctor.
       * 
       * @param cid content id, may be <code>null</code> or empty.
       * @param rev revision, may be <code>null</code> or empty.
       * @param varid variantid, may be <code>null</code> or empty. 
       * @param location location, may be <code>null</code> or empty. 
       */
      PageData(String cid, String rev, String varid, String location)
      {
         m_contentid = cid;
         m_revision = rev;
         m_variantid = varid;
         m_location = location;
      }

      /**
       * Get the contentid as supplied in the ctor.
       * 
       * @param defaultValue default value to be returned if the value is
       *           <code>null</code>, may be <code>null</code> or empty.
       * @return contentid, may be <code>null</code> or empty.
       */
      String getContentid(String defaultValue)
      {
         return m_contentid == null ? defaultValue : m_contentid;
      }

      /**
       * Get the location as supplied in the ctor.
       * 
       * @param defaultValue default value to be returned if the value is
       *           <code>null</code>, may be <code>null</code> or empty.
       * @return location, may be <code>null</code> or empty.
       */
      String getLocation(String defaultValue)
      {
         return m_location == null ? defaultValue : m_location;
      }

      /**
       * Get the revision as supplied in the ctor.
       * 
       * @param defaultValue default value to be returned if the value is
       *           <code>null</code>, may be <code>null</code> or empty.
       * @return revision, may be <code>null</code> or empty.
       */
      String getRevision(String defaultValue)
      {
         return m_revision == null ? defaultValue : m_revision;
      }

      /**
       * Get the variantid as supplied in the ctor.
       * 
       * @param defaultValue default value to be returned if the value is
       *           <code>null</code>, may be <code>null</code> or empty.
       * @return variantid, may be <code>null</code> or empty.
       */
      String getVariantid(String defaultValue)
      {
         return m_variantid == null ? defaultValue : m_variantid;
      }
   }
   
   /**
    * The logger instance for this class, never <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger("PSAppendPurgeOrMovedItems");
}
