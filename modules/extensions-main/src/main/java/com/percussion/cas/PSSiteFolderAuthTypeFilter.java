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
package com.percussion.cas;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSParseUrlQueryString;
import com.percussion.util.PSXMLDomUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Site Folder Publishing facilitates publishing of Content Items in a Content 
 * Explorer Site Folder tree to an identical folder tree on a delivery site or 
 * multiple delivery sites. As only the site folder items get published in the 
 * site folder publishing the related content links need to be filtered. 
 * This exit is meant for casSupport resources and resources that generate
 * auto related content. When it is used on auto related content resources
 * this exits should be placed after sys_casAutoRelatedContent exit. 
 * The exit filters the linkurl elements based on the following criteria.
 * Site based filtering:
 * If linkurl consists of sys_siteid attribute then checks whether a site with 
 * this id exists in RXSITES table or not. If not removes that linkurl.
 * If siteid attribute does not exist then the sys_originalsiteid parameter from 
 * the request will be used if that does not exist then sys_siteid from
 * the request will be used for filtering.
 * Folder based filtering:
 * If linkurl consists of sys_folderid attribute then checks whether the folder
 * exists or not. If not removes that linkurl. 
 * Then checks whether the folder exists in above site or not. If not removes 
 * the link. 
 * Then checks whether the item exists in the above folder or not. 
 * If not removes the link.
 * Variant based filtering.
 * If the variant is a page variant then checks whether it is a publishable 
 * variant or not. If not removes the link.
 * 
 */
public class PSSiteFolderAuthTypeFilter implements IPSResultDocumentProcessor
{
   /* (non-Javadoc)
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   /* (non-Javadoc)
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument
    * (java.lang.Object[], com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(
      Object[] params,
      IPSRequestContext request,
      Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (resultDoc == null)
         return resultDoc;
      NodeList nl = resultDoc.getElementsByTagName(LINKURL);
      if (nl != null && nl.getLength() > 0)
      {
         if (nl.getLength() == 1
            && ((Element) nl.item(0)).getAttribute(CONTENTID).length() == 0)
            return resultDoc;
         filterLinkUrls(request, resultDoc);
      }
      return resultDoc;
   }
   /* (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
   }
   /**
    * Filters the linkurl elements by calling following helper methods.
    * {@link #isValidLinkBySiteID(IPSRequestContext, PSRelationshipProcessorProxy,
    * Map, Map, Element)} to filter the linkurl Elements by site id in the 
    * linkurl or request.
    * {@link #isValidLinkByFolderID(IPSRequestContext, PSRelationshipProcessorProxy, 
    * Map, Map, Map, Element)} to filter the linkurl Elements by folder id in 
    * the linkurl.
    * {@link #isValidLinkByVariant(IPSRequestContext, PSContentTypeVariantSet, 
    * Map, Element)} to filter the linkurl elements by variant id in the linkurl.
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param doc the resultDoc from which the links needs to be removed
    * assumed not <code>null</code>.
    * @throws PSExtensionProcessingException when there is an error while
    * creating PSRelationshipProcessorProxy object.
    */
   private void filterLinkUrls(IPSRequestContext request, Document doc)
      throws PSExtensionProcessingException
   {
      PSRelationshipProcessor relProxy;
  
         relProxy = PSRelationshipProcessor.getInstance();
         Map siteFolderRoot = initializeSiteFolderRootMap(request);
         if (siteFolderRoot.size() < 1)
         {
            String msg =
               ms_className
                  + ": No sites are available with folder root, "
                  + "Site Folder Authtype filtering can not be done.";
            log.debug(msg);
            request.printTraceMessage(msg);
            throw new PSExtensionProcessingException(0, msg);
         }
         Map<String, String> folderPaths = initializeFolderPathMap(request,
               relProxy, doc);
         NodeList nl = doc.getElementsByTagName(LINKURL);
         for (int i = nl.getLength() - 1; i >= 0; i--)
         {
            Element linkurl = (Element) nl.item(i);
            Map linkparams = validateLinkUrl(request, linkurl);
            if (linkparams == null)
            {
               nl.item(i).getParentNode().removeChild(nl.item(i));
               continue;
            }
            if (!isValidLinkBySiteID(request,
               relProxy,
               siteFolderRoot,
               linkparams,
               linkurl))
            {
               nl.item(i).getParentNode().removeChild(nl.item(i));
               continue;
            }
            if (!isValidLinkByFolderID(request,
               relProxy,
               siteFolderRoot,
               folderPaths,
               linkparams,
               linkurl))
            {
               nl.item(i).getParentNode().removeChild(nl.item(i));
               continue;
            }
            if (!isValidLinkByVariant(request,
               linkparams,
               linkurl))
            {
               nl.item(i).getParentNode().removeChild(nl.item(i));
               continue;
            }
         }
     
   }
   /**
    * Utility method to validate the linkurl element and then build a map
    * of required parameters for filtering the linkurl.
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param linkurl Element which needs to checked for siteid must not be 
    * <code>null</code>.
    * 
    * @return Map of required parameters for filtering the linkurl or <code>
    * null</code> if any of the required parameter is missing.
    */
   private Map validateLinkUrl(IPSRequestContext request, Element linkurl)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      if (linkurl == null)
         throw new IllegalArgumentException("linkurl must not be null");
      Map<String, String> params = null;
      String contentid = linkurl.getAttribute(CONTENTID).trim();
      if (contentid.length() < 1)
      {
         String msg = "Removed linkurl : \nReason: " + CONTENTID
               + " attribute is missing." + "\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return params;
      }
      String variantid = linkurl.getAttribute(VARIANTID).trim();
      if (variantid.length() < 1)
      {
         String msg = "Removed linkurl : \nReason: " + VARIANTID
               + " attribute is missing." + "\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return params;
      }
      String siteid = linkurl.getAttribute(IPSHtmlParameters.SYS_SITEID).trim();
      if (siteid.length() < 1)
         siteid =
            request
               .getParameter(
                  IPSHtmlParameters.SYS_ORIGINALSITEID,
                  request.getParameter(IPSHtmlParameters.SYS_SITEID, ""))
               .trim();
      if (siteid.length() < 1)
      {
         String msg = "Removed linkurl : \nReason: "
               + IPSHtmlParameters.SYS_SITEID
               + " attribute is missing and neither "
               + IPSHtmlParameters.SYS_ORIGINALSITEID + " nor "
               + IPSHtmlParameters.SYS_SITEID
               + " nor present in the request.\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return params;
      }
      String folderid =
         linkurl.getAttribute(IPSHtmlParameters.SYS_FOLDERID).trim();
      
      //As this exit can be used on either casSupport resources and as well as
      //autorelated content resources. We should try to get the value appropriately. 
      String rcurl = PSXMLDomUtil.getElementData(linkurl);
      if(rcurl==null || rcurl.trim().length()<1)
      {
         NodeList nl = linkurl.getElementsByTagName("Value");
         if(nl!=null && nl.getLength()>0)
            rcurl = ((Element)nl.item(0)).getAttribute("current");
      }
      
      if (rcurl == null || rcurl.trim().length() < 1)
      {
         String msg = "Removed linkurl : \nReason: "
               + "Assembly url is missing from the linkurl element."
               + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return params;
      }      
      
      Map rcurlparams = null;
      try
      {
         rcurlparams = PSParseUrlQueryString.parseParameters(rcurl);
      }
      catch (PSRequestParsingException e)
      {
         String msg = "Removed linkurl : \nReason: "
               + " Error occurred while parsing the url for "
               + IPSHtmlParameters.SYS_REVISION + "\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl) + "\nError:" + e.getMessage();
         log.error(msg);
         log.debug(e.getMessage(),e);
         request.printTraceMessage(msg);
         return params;
      }
      String revision =
         (String) rcurlparams.get(IPSHtmlParameters.SYS_REVISION);
      if (revision == null || revision.trim().length() < 1)
      {
         String msg = "Removed linkurl : \nReason: "
               + IPSHtmlParameters.SYS_REVISION
               + " is missing from the linkurl." + "\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return params;
      }
      params = new HashMap<>();
      params.put(CONTENTID, contentid);
      params.put(VARIANTID, variantid);
      params.put(IPSHtmlParameters.SYS_SITEID, siteid);
      params.put(IPSHtmlParameters.SYS_FOLDERID, folderid);
      params.put(IPSHtmlParameters.SYS_REVISION, revision.trim());
      return params;
   }
   /**
    * Makes an internal request to the @link{LOOKUP_SITE_FOLDER_ROOT}
    * and builds the site and folder root map.
    *  
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @return Map of siteids and folderroot may be <code>empty</code> but never 
    * <code>null</code>.
    * @throws PSExtensionProcessingException, if there are any errors
    * getting the Site and Folder information from Rhythmyx resource
    * {@link #LOOKUP_SITE_FOLDER_ROOT}
    */
   private Map initializeSiteFolderRootMap(IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      Map<String, String> siteFolderRoot = new HashMap<>();
      IPSInternalRequest lookupRequest =
         request.getInternalRequest(LOOKUP_SITE_FOLDER_ROOT, null, false);
      if (lookupRequest == null)
      {
         String msg =
            ms_className
               + ": Unable to locate handler for request: "
               + LOOKUP_SITE_FOLDER_ROOT;
         log.debug(msg);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      try
      {
         Document results = lookupRequest.getResultDoc();
         NodeList nl = results.getElementsByTagName("Site");
         if (nl == null)
         {
            String msg =
               ms_className
                  + ": No sites are registered, "
                  + "Site Folder Authtype filtering can not be done.";
            log.debug(msg);
            request.printTraceMessage(msg);
            throw new PSExtensionProcessingException(0, msg);
         }
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            String siteid = elem.getAttribute("SiteId");
            String folderroot = elem.getAttribute("FolderRoot");
            if (folderroot.trim().length() < 1)
            {
               String msg = "Folder root for the siteid " + siteid
                     + "is null or empty."
                     + "\n Skipping this site id from checking.";
               log.debug(msg);
               request.printTraceMessage(msg);
               continue;
            }
            siteFolderRoot.put(siteid, folderroot);
         }
      }
      catch (PSInternalRequestCallException e)
      {
         String msg =
            ms_className
               + ": Error occurred while making internal request to "
               + LOOKUP_SITE_FOLDER_ROOT
               + " error: "
               + e.getMessage();
         log.error(msg);
         log.debug(e.getMessage(),e);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      return siteFolderRoot;
   }
   /**
    * Builds a folder path map by getting the component summaries for all 
    * the folderids in the linkurls. If a folder does not exist then we do not 
    * get the component summary for that folder and there will not be any entry
    * for that folder in the map.
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param relProxy object of PSRelationshipProcessor must not be 
    * <code>null</code>.
    * @param doc resultDoc Document must not be null.
    * @return Map of folderids and folder paths may be <code>empty</code> but 
    * never <code>null</code>.
    * @throws PSExtensionProcessingException, if there are any errors
    * while getting the folder paths. 
    */
   private Map<String, String> initializeFolderPathMap(
      IPSRequestContext request,
      PSRelationshipProcessor relProxy,
      Document doc)
      throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");
      Map<String, String> folderPaths = new HashMap<>();
      //Get all folderids and the component summary for them.
      NodeList nl = doc.getElementsByTagName(LINKURL);
      Set<Integer> folderIdSet = new HashSet<>();
      String folderId;
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element linkurl = (Element) nl.item(i);
         folderId = linkurl.getAttribute(IPSHtmlParameters.SYS_FOLDERID);
         if (folderId.trim().length() > 0)
         {
            try
            {
               int id = Integer.parseInt(folderId);
               folderIdSet.add(id);
            }
            catch (NumberFormatException ne)
            {} // ginore the bad ids
         }
      }
      //If there are no folderids in the link urls just return and the 
      //map will be empty
      if (folderIdSet.size() < 1)
         return folderPaths;
      
      try
      {
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         List<PSComponentSummary> summaries = cms.loadComponentSummaries(folderIdSet);
         for (PSComponentSummary s : summaries)
         {
            //Get the path for the folder and add the name to it
            String[] relPath =
               relProxy.getRelationshipOwnerPaths(
                  PSDbComponent.getComponentType(PSFolder.class),
                  (PSLocator) s.getLocator(),
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            folderPaths.put(
               Integer.toString(((PSLocator) s.getLocator()).getId()),
               relPath[0] + "/" + s.getName());
         }
         //Build the variantid and publishwhen value map for page variants
      }
      catch (PSCmsException e1)
      {
         String msg =
            ms_className
               + ": Error occurred while getting the component summaries for "
               + "folder ids: "
               + folderIdSet
               + " error: "
               + e1.getMessage();
         log.error(msg);
         log.debug(e1.getMessage(),e1);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      return folderPaths;
   }

   /**
    * Utility method to decide whether a link is valid by site id or not.
    * Gets the site id from linkparams and checks that site id exists or not
    * and checks whether item exists under that site or not.
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param relProxy object of PSRelationshipProcessor must not be 
    * <code>null</code>.
    * @param siteFolderRoot map of site ids and folder roots must not be 
    * <code>null</code>.
    * @param linkparams map of required parameters by which the removal of link 
    * will be decided must not be <code>null</code>.
    * @param linkurl Element which needs to checked for siteid must not be 
    * <code>null</code>.
    * @return <code>true</code> if siteid exits in the sites map otherwise
    * <code>false</code>
    * @throws PSExtensionProcessingException When there is an error while
    * getting the owner paths for item.
    */
   private boolean isValidLinkBySiteID(
      IPSRequestContext request,
      PSRelationshipProcessor relProxy,
      Map siteFolderRoot,
      Map linkparams,
      Element linkurl)
      throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      if (relProxy == null)
         throw new IllegalArgumentException("relProxy must not be null");
      if (siteFolderRoot == null)
         throw new IllegalArgumentException("siteFolderRoot must not be null");
      if (linkparams == null)
         throw new IllegalArgumentException("linkparams must not be null");
      if (linkurl == null)
         throw new IllegalArgumentException("linkurl must not be null");
      String siteid = (String) linkparams.get(IPSHtmlParameters.SYS_SITEID);
      if (!siteFolderRoot.containsKey(siteid))
      {
         String msg = "Removed linkurl : \nReason: No "
               + "Site Folder site exists with the siteid " + siteid
               + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return false;
      }
      String contentId = (String) linkparams.get(CONTENTID);
      String siteFolderPath = (String) siteFolderRoot.get(siteid);
      //Normalize to end with /
      if (!siteFolderPath.endsWith("/"))
         siteFolderPath += "/";
      try
      {
         String[] relPath =
            relProxy.getRelationshipOwnerPaths(
               PSDbComponent.getComponentType(PSFolder.class),
               new PSLocator(contentId),
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         boolean found = false;
         for (int i = 0; i < relPath.length; i++)
         {
            String path = relPath[i];
            //Normalize to end with /
            if (!path.endsWith("/"))
               path += "/";
            if (path.equals(siteFolderPath) || path.startsWith(siteFolderPath))
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            String msg = "Removed linkurl : \nReason: Item(" + contentId
                  + ") does not exist under the specified site(" + siteid
                  + ")." + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
            log.debug(msg);
            request.printTraceMessage(msg);
            return false;
         }
      }
      catch (PSCmsException e)
      {
         String msg =
            ms_className
               + ": Error occurred while getting the owner path for"
               + " content id ("
               + contentId
               + "): \nError : "
               + e.getMessage();
         log.error(msg);
         log.debug(e.getMessage(),e);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      return true;
   }
   /**
    * Utility method to decide whether a link is valid by folder id or not.
    * Gets the folder id from linkurl if does not present then returns 
    * <code>true</code> as we can not filter the link without a folder id.
    * If folder exists then three checks will be performed, wherever it
    * fails <code>false</code> will be returned. If succeeds in all checks 
    * <code>true</code> will be returned.
    * Check 1: Folder exists or not
    * Check 2: Folder exists under the specified site or not
    * Check 3: Item exists under the folder or not. 
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param relProxy object of PSRelationshipProcessor must not be 
    * <code>null</code>.
    * @param siteFolderRoot map of site ids and folder roots must not be 
    * <code>null</code>.
    * @param folderPaths map of folder ids and folder paths must not be 
    * <code>null</code>.
    * @param linkparams map of required parameters by which the removal of 
    * link will be decided must not be <code>null</code>.
    * @param linkurl Element which needs to checked for siteid must not be 
    * <code>null</code>.
    * @return <code>true</code> if it satisfies above rules otherwise
    * <code>false</code>
    * @throws PSExtensionProcessingException When error ouccurs while getting
    * the folder relationships.
    */
   private boolean isValidLinkByFolderID(
      IPSRequestContext request,
      PSRelationshipProcessor relProxy,
      Map siteFolderRoot,
      Map folderPaths,
      Map linkparams,
      Element linkurl)
      throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      if (relProxy == null)
         throw new IllegalArgumentException("relProxy must not be null");
      if (siteFolderRoot == null)
         throw new IllegalArgumentException("siteFolderRoot must not be null");
      if (linkparams == null)
         throw new IllegalArgumentException("linkparams must not be null");
      if (linkurl == null)
         throw new IllegalArgumentException("linkurl must not be null");
      String folderId = (String) linkparams.get(IPSHtmlParameters.SYS_FOLDERID);
      //If there is no sys_folderid attribute on the link we
      //can not validate this linkurl based on the folderid,
      //and we assume it as a right link
      if (folderId.trim().length() < 1)
         return true;
      String folderPath = (String) folderPaths.get(folderId);
      //Check 1: Folder exists or not check
      if (folderPath == null)
      {
         String msg = "Removed linkurl : \nReason: Specified folder("
               + folderId + ") does not exist." + "\nlinkurl: "
               + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return false;
      }
      String siteId = (String) linkparams.get(IPSHtmlParameters.SYS_SITEID);
      String sitePath = (String) siteFolderRoot.get(siteId);
      //Check 2: Folder exists under the specified site or not
      if (!(folderPath.equals(sitePath) || folderPath
            .startsWith(sitePath + "/")))
      {
         String msg = "Removed linkurl : \nReason: Specified folder("
               + folderId + ") does not exist under the specified site("
               + siteId + ")." + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return false;
      }
      String contentId = (String) linkparams.get(CONTENTID);
      //Try to get the folder relationship between the folderid and 
      //contentid and if it exists it is the child of the that folder.
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(folderId));
      filter.setDependent(new PSLocator(contentId));
      filter.setCommunityFiltering(false);
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      try
      {
         PSRelationshipSet rset = relProxy.getRelationships(filter);
         //Check 3:
         if (rset == null || rset.size() < 1)
         {
            String msg = "Removed linkurl : \nReason: Item(" + contentId
                  + ") does not exist under the specified folder(" + folderId
                  + ")." + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
            log.debug(msg);
            request.printTraceMessage(msg);
            return false;
         }
      }
      catch (PSCmsException e)
      {
         String msg = ms_className
               + ": Error occurred while getting the folder relationship"
               + " between folder id (" + folderId + ") and content id ("
               + contentId + ") \nError : " + e.getMessage();
         log.error(msg);
         log.debug(e.getMessage(),e);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      return true;
   }
   /**
    * Utility method to decide whether a link is valid by variant id or not.
    * Gets the variant id from linkurl and if it is a page variant then checks
    * whether the variant is a publishable variant or not. If yes returns
    * <code>true</code> otherwise <code>false</code>. If the variant under
    * consideration is not a page variant then we can not decide and return
    * <code>true</code> in that case.
    * 
    * @param request object of IPSRequestContext must not be <code>null</code>.
    * @param linkparams map of required parameters by which the removal of link
    *           will be decided must not be <code>null</code>.
    * @param linkurl Element which needs to checked for siteid must not be
    *           <code>null</code>.
    * 
    * @return <code>true</code> if it satisfies above rules otherwise
    *         <code>false</code>
    * @throws PSExtensionProcessingException When there is an error making an
    *            internal request to find the publishable variant.
    */
   private boolean isValidLinkByVariant(
      IPSRequestContext request,
      Map linkparams,
      Element linkurl)
      throws PSExtensionProcessingException
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      if (linkparams == null)
         throw new IllegalArgumentException("linkparams must not be null");
      if (linkurl == null)
         throw new IllegalArgumentException("linkurl must not be null");
      String variantId = (String) linkparams.get(VARIANTID);
      IPSAssemblyService assembly = 
         PSAssemblyServiceLocator.getAssemblyService();
      //Get the info of the variant we are interested in
      IPSAssemblyTemplate template;
      try
      {
         template = assembly.loadUnmodifiableTemplate(variantId);
      }
      catch (PSAssemblyException e1)
      {
         log.error(e1.getMessage());
         log.debug(e1.getMessage(),e1);
         throw new PSExtensionProcessingException("PSSiteFolderAuthTypeFilter", e1);
      }
      //This should not happen as we just came from the database with this 
      //variantid in case if it happens remove the link and log it.
      if (template == null)
      {
         String msg = "Removed linkurl : \nReason: "
               + "Variant info is missing for specified variantid(" + variantId
               + ")." + "\nlinkurl: " + PSXMLDomUtil.toString(linkurl);
         log.debug(msg);
         request.printTraceMessage(msg);
         return false;
      }
      //We can filter it only if it is page variant
      if (template.getOutputFormat().ordinal() != 1)
         return true;
      //Now it is the time to see whether the page variant is publishable or not
      String siteId = (String) linkparams.get(IPSHtmlParameters.SYS_SITEID);
      //Get the content id from the linkurl
      String contentId = (String) linkparams.get(CONTENTID);
      String revision = (String) linkparams.get(IPSHtmlParameters.SYS_REVISION);
      //Make use of the publishable variant list from the content list 
      //generator resource to decide the link url variant is a publishable 
      //variant or not
      Map reqParams = new HashMap();
      reqParams.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
      reqParams.put(IPSHtmlParameters.SYS_SITEID, siteId);
      reqParams.put(IPSHtmlParameters.SYS_REVISION, revision);
      IPSInternalRequest lookupRequest =
         request.getInternalRequest(
            LOOKUP_VARIANTS_SITE_ITEM,
            reqParams,
            false);
      if (lookupRequest == null)
      {
         String msg =
            ms_className
               + ": Unable to locate handler for request: "
               + LOOKUP_VARIANTS_SITE_ITEM;
         log.debug(msg);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      try
      {
         Document results = lookupRequest.getResultDoc();
         NodeList nl = results.getElementsByTagName("variant");
         if (nl == null)
         {
            String msg = "Removed linkurl : \nReason: Specified variantid("
                  + variantId
                  + ") is not a publishable variant for the specified site("
                  + siteId + ")." + "\nlinkurl: "
                  + PSXMLDomUtil.toString(linkurl);
            log.debug(msg);
            request.printTraceMessage(msg);
            return false;
         }
         boolean found = false;
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            String vid = elem.getAttribute("id");
            if (vid.equals(variantId))
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            String msg = "Removed linkurl : \nReason: Specified variantid("
                  + variantId
                  + ") is not a publishable variant for the specified site("
                  + siteId + ")." + "\nlinkurl: "
                  + PSXMLDomUtil.toString(linkurl);
            log.debug(msg);
            request.printTraceMessage(msg);
            return false;
         }
      }
      catch (PSInternalRequestCallException e)
      {
         String msg =
            ms_className
               + ": Error occurred while making internal request to "
               + LOOKUP_VARIANTS_SITE_ITEM
               + " with params "
               + reqParams.toString()
               + " \nError: "
               + e.getMessage();
         log.error(msg);
         log.debug(e.getMessage(),e);
         request.printTraceMessage(msg);
         throw new PSExtensionProcessingException(0, msg);
      }
      return true;
   }
   /**
    * Name of the Rhythmyx internal resource used to query the site folder root
    * for a given site id.
    */
   private final String LOOKUP_SITE_FOLDER_ROOT =
      "sys_casSupport/SiteLookup";
   /**
    * Name of the Rhythmyx internal resource used to query the publishable
    * variants for the given item based on the given site id and revision.
    */
   private final String LOOKUP_VARIANTS_SITE_ITEM =
      "rx_supportSiteFolderContentList/lookupVariantsBySiteItem.xml";
   /**
    * The exit name used for error handling
    */
   private static final String ms_className = "PSSiteFolderAuthTypeFilter";
   /**
    * The element name for the relationship links
    */
   private static final String LINKURL = "linkurl";
   /**
    * The name of the attribute for variantid in linkurl
    */
   private static final String VARIANTID = "variantid";
   /**
    * The name of the attribute for contentid in linkurl
    */
   private static final String CONTENTID = "contentid";
   
   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager
         .getLogger(PSSiteFolderAuthTypeFilter.class);
}
