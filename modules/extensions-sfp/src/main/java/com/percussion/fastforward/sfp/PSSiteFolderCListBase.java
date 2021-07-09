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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.fastforward.utils.PSRelationshipHelper;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The base class used for generating Rhythmyx content list XML for all content
 * items contained within a specified site folder, or its child folders.
 * Determines which variants to publish for each content type for the given site
 * by referencing a lookup table. Builds delivery location paths by
 * concatenating an item's ancestor folders' names.
 *
 * @author James Schultz
 */
public abstract class PSSiteFolderCListBase
{


   /**
    * Constructs a site-folder-driven, full-publishing, public-items
    * content list builder.
    *
    * @param request the current request context, used to obtain request
    * parameters, logging, and making internal requests. Not <code>null</code>.
    */
   public PSSiteFolderCListBase(IPSRequestContext request)
   {
      initializeMembers(request, false, "y", null, -1, -1, -1,
            "http", PSServer.getHostAddress(),
            Integer.toString(PSServer.getListenerPort()),
            new HashSet());
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
   public PSSiteFolderCListBase(
      IPSRequestContext request,
      boolean isIncremental,
      String publishableContentValidValues,
      int maxRowsPerPage)
   {
      if (request == null)
         throw new IllegalArgumentException(
               "PSSiteFolderContentList's request may not be null");

      if (publishableContentValidValues == null
            || publishableContentValidValues.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "PSSiteFolderContentList's publishableContentValidValues "
                     + "may not be null or empty");
      }

      initializeMembers(
         request,
         isIncremental,
         publishableContentValidValues,
         null,
         maxRowsPerPage,
         -1,
         -1,
         "http", PSServer.getHostAddress(),
         Integer.toString(PSServer.getListenerPort()),
         new HashSet());
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
    *           values for workflow states that are eligible for publishing.
    *           Never <code>null</code> or empty.
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
   public PSSiteFolderCListBase(IPSRequestContext request,
         boolean isIncremental,
         String publishableContentValidValues,
         String contentVariantResourceName,
         int maxRowsPerPage,
         String protocol, String host, String port,
         Set paramSetToPass)
   {
      if (request == null)
         throw new IllegalArgumentException(
               "PSSiteFolderContentList's request may not be null");

      if (publishableContentValidValues == null
            || publishableContentValidValues.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "PSSiteFolderContentList's publishableContentValidValues "
                     + "may not be null or empty");
      }
      if (protocol == null || protocol.trim().length() == 0)
      {
         throw new IllegalArgumentException("protocol may not be null or empty");
      }
      if (host == null || host.trim().length() == 0)
      {
         throw new IllegalArgumentException("host may not be null or empty");
      }
      if (port == null || port.trim().length() == 0)
      {
         throw new IllegalArgumentException("port may not be null or empty");
      }
      initializeMembers(request, isIncremental, publishableContentValidValues,
            contentVariantResourceName, maxRowsPerPage, -1, -1,
            protocol, host, port,
            paramSetToPass);
   }


   /**
    * Assigns values to member variables
    *
    * @param request the current request context, used to obtain request
    * parameters, logging, and making internal requests. Not <code>null</code>.
    * @param isIncremental <code>true</code> to generate an incremental
    * publishing content list, <code>false</code> for full publishing
    * @param publishableContentValidValues comma-delimited list of contentvalid
    * values for states that are eligible for publishing. Assume not
    * <code>null</code> or empty.
    * @param contentVariantResourceName
    * @param maxRowsPerPage
    * @param maxPages
    * @param maxDisplayedPageLinks
    * @param protocol the URL protocol to use when creating content URLs,
    *           assumed never <code>null</code> or empty
    * @param host the host name or ip address to use when creating content URLs,
    *           assumed never <code>null</code> or empty
    * @param port the port number to use when creating content URLs, never
    *           assumed <code>null</code> or empty. Must be numeric.
    * @param paramSetToPass Set of non-standard HTML parameters to pass from
    * request context to each content item url in the content list, may be
    * <code>null</code> or empty.
    */
   private void initializeMembers(
      IPSRequestContext request,
      boolean isIncremental,
      String publishableContentValidValues,
      String contentVariantResourceName,
      int maxRowsPerPage,
      int maxPages,
      int maxDisplayedPageLinks,
      String protocol, String host, String port,
      Set paramSetToPass)
   {
      if (paramSetToPass != null)
         m_paramSetToPass = paramSetToPass;

      m_request = request;
      m_folderProcessor = PSServerFolderProcessor.getInstance();
      m_isIncremental = isIncremental;
      m_protocol = protocol;
      m_host = host;
      try
      {
         m_port = Integer.parseInt(port);
      }
      catch (NumberFormatException e1)
      {
         throw new IllegalArgumentException("port must be numeric");
      }
      m_publishableContentValidValues = publishableContentValidValues;
      if(contentVariantResourceName != null && contentVariantResourceName.length() > 0)
      {
         m_contentResourceName = contentVariantResourceName;
      }
      else
      {
         m_contentResourceName = getDefaultResource();
      }
      m_maxRowsPerPage = maxRowsPerPage;
      m_maxPages = maxPages;
      m_maxDisplayedPageLinks = maxDisplayedPageLinks;

      // Set folder include mode
      String includeMode =
         request.getParameter(IPSConstants.INCLUDE_FOLDERS);
      if(includeMode == null || includeMode.trim().length() == 0
         || includeMode.equalsIgnoreCase(INCLUDE_MODE_ALL)
         || !(includeMode.equalsIgnoreCase(INCLUDE_MODE_FLAGGED)
            || includeMode.equalsIgnoreCase(INCLUDE_MODE_UNFLAGGED)))
      {
            m_folderIncludeMode = INCLUDE_MODE_ALL;
      }
      else
      {
         m_folderIncludeMode = includeMode.toLowerCase();
         // We only need to get this list for flagged and unflagged
         // include mode
         m_flaggedFolders = getAllPublishFlaggedFolders();
      }

      try
      {
         m_helper = new PSRelationshipHelper(request);
         m_helper.setCommunities(false);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         m_helper = null;
      }
   }

   /**
    * This method is synchronized because the content list is stored as a
    * member variable.  The content list will be session cached (keyed to the
    * publication id) when pagination is enabled.
    *
    * @param siteFolderPath the site folder path, It may not be
    *    <code>null</code> or empty.
    * @param deliveryType delivery type string, may be <code>null</code> or
    *    empty.
    * @param filenameContext publish context allows <code>null</code> or empty.
    * @param indexOfFirstItem index of the forst item in the page, must be
    *    greater than equal to 0
    * @param publishFolderPath the to be published folder path. It is either
    *    a descendent folder of the site or the same as the
    *    <code>siteFolderPath</code> if publishes the whole site. It may not be
    *    <code>null</code> or empty.
    *
    * @return A content list XML document.  Never <code>null</code> but will
    * be empty if member variables are not initialized.
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   public synchronized Document buildContentList(
      final String siteFolderPath,
      final String deliveryType,
      final String filenameContext,
      int indexOfFirstItem,
      final String publishFolderPath)
      throws
         PSUnknownNodeTypeException,
         PSCmsException,
         PSExtensionProcessingException
   {
      if (siteFolderPath == null || siteFolderPath.trim().length() == 0)
         throw new IllegalArgumentException("buildContentList.siteFolderPath may not be null");
      if (publishFolderPath == null || publishFolderPath.trim().length() == 0)
         throw new IllegalArgumentException("buildContentList.publishFolderPath may not be null");

      m_relProcessor = PSRelationshipProcessor.getInstance();
      String sys_publicationid =
         m_request.getParameter(IPSHtmlParameters.SYS_PUBLICATIONID);
      String sys_sort = m_request.getParameter("sys_sort");

      /* Requests from the publisher contain a sys_publicationid.  Do not include
         debug comments for publisher requests.
         Cache paginated publisher requests. */

      m_debugModeOn = (sys_publicationid == null);
      log.debug("m_debugModeOn= {}", m_debugModeOn);
      if (sys_publicationid != null)
      {
         // check for a cached content list
         m_contentList = getCachedContentList(sys_publicationid);
         if (m_contentList != null)
         {
            log.debug("using cached content list for publication id= {} ", sys_publicationid);

            // clear the cached list if the last page has been requested
            if ((indexOfFirstItem + m_maxRowsPerPage) >= m_contentList.size())
               clearCachedContentList(sys_publicationid);

            return m_contentList.getPage(indexOfFirstItem, m_request);
         }
         else
         {
            log.debug("did not find cached content list for publication id= {} ", sys_publicationid);
         }
      }

      m_contentList =
         new PSContentList(
            m_request.getParameter(IPSHtmlParameters.SYS_CONTEXT),
            deliveryType,
            m_maxRowsPerPage,
            m_maxPages,
            m_maxDisplayedPageLinks);

      if (m_request != null && m_helper != null)
      {
         if (m_isIncremental)
            log.debug("creating incremental content list with contentvalid= {}", m_publishableContentValidValues);
         else
            log.debug("creating full content list with contentvalid= {}", m_publishableContentValidValues);

         PublishFolder folder = getPublishFolder(publishFolderPath, siteFolderPath);
         if (folder != null)
         {
            boolean appendFolderName = folder.m_folderId != folder.m_siteRootId;
            String folderPath = folder.m_relativeParentPath;
            if (appendFolderName)
            {
               //We need to find the parent folder path by their published
               // folder names.
               String temp = publishFolderPath;
               List fnLst = new ArrayList();
               temp = temp.substring(0, temp.lastIndexOf("/"));
               while (!temp.equals(siteFolderPath)
                     && temp.lastIndexOf("/") > -1)
               {
                  PSLocator floc = m_helper.getFolderLocatorByPath(temp);
                  String fn = m_folderProcessor.getPubFileName(floc.getId());
                  fnLst.add(fn);
                  temp = temp.substring(0, temp.lastIndexOf("/"));
               }
               StringBuffer folderPathBuf = new StringBuffer();
               for (int i = fnLst.size() - 1; i >= 0; i--)
               {
                  folderPathBuf.append("/");
                  folderPathBuf.append((String) fnLst.get(i));
               }
               folderPathBuf.append("/");
               folderPath = folderPathBuf.toString();
            }
            generateContentItems(folderPath,
                  folder.m_folderId, siteFolderPath, filenameContext,
                  appendFolderName);

            if (sys_sort != null) // for debugging, comparing
               m_contentList.sort();
         }
      }

      // cache the content list if pagination enabled and not on last page
      if (sys_publicationid != null
         && m_maxRowsPerPage > 0
         && (indexOfFirstItem + m_maxRowsPerPage) < m_contentList.size())
      {
         setCachedContentList(sys_publicationid, m_contentList);
      }

      return m_contentList.getPage(indexOfFirstItem, m_request);
   }

   /**
    * Get the folder id and its relative parent path from the supplied folder
    * and site path.
    *
    * @param publishFolder the to be published folder path, assumed not
    *    <code>null</code> or empty. This must be a folder path under the site.
    * @param siteFolderPath the site root path, assumed not <code>null</code> or
    *    empty.
    *
    * @return the folder info, which include folder id and its parent's relative
    *    path to the site. It is <code>null</code> if one of the supplied path
    *    is invalid.
    *
    * @throws PSCmsException if error occurs.
    */
   private PublishFolder getPublishFolder(String publishFolder,
         String siteFolderPath) throws PSCmsException
   {
      PublishFolder folder = null;
      int folderId = -1;

      PSLocator rootLocator =
         m_helper.getFolderLocatorByPath(siteFolderPath);
      PSLocator folderLocator = null;
      if (siteFolderPath.equalsIgnoreCase(publishFolder))
         folderLocator = rootLocator;
      else
         folderLocator =
            m_helper.getFolderLocatorByPath(publishFolder);

      if (rootLocator == null)
      {
         log.warn("The supplied folder path does not resolve to a folder: {}", siteFolderPath);
      }
      else if (folderLocator == null)
      {
         log.warn("The supplied folder path does not resolve to a folder: {}", publishFolder);
      }
      // publish the while site
      else if (folderLocator.getId() == rootLocator.getId())
      {
         return new PublishFolder(rootLocator.getId(), "/", rootLocator.getId());
      }
      // publish a sub folder of the site
      else
      {
         if (! m_helper.isFolderDescendent(rootLocator, folderLocator))
         {
            log.warn("The supplied folder path, '{}', is not a decendent of the site folder path, '{}'", publishFolder, siteFolderPath);
         }
         else
         {
            List parents = m_helper.getRelationshipProcessor().getParents(
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT, folderLocator);
            // has to have only one parent folder
            PSLocator parentLocator = (PSLocator) parents.get(0);

            // to be published folder is an immediate sub folder of the site.
            if (parentLocator.getId() == rootLocator.getId())
            {
               return new PublishFolder(folderLocator.getId(), "/", rootLocator
                     .getId());
            }

            // remove trailing slash from the siteFolderPath if any
            if (siteFolderPath.charAt(siteFolderPath.length()-1) == '/')
               siteFolderPath = siteFolderPath.substring(0, siteFolderPath.length()-1);

            String parentFolder = publishFolder.substring(0, publishFolder
                  .lastIndexOf('/'));
            String relativeParentPath = parentFolder.substring(siteFolderPath
                  .length(), parentFolder.length()) + "/";

            return new PublishFolder(folderLocator.getId(), relativeParentPath,
                  rootLocator.getId());
         }
      }

      return folder;
   }

   /**
    * This is used to collect the data for the to be published folder.
    */
   private class PublishFolder
   {
      /*
       * Construct an instance from the supplied parameters.
       *
       * @param folderId the to be published folder id;
       * @param parentPath the relative parent path.
       *    see {@link #m_relativeParentPath}.
       * @param siteRootId the site root id.
       */
      private PublishFolder(int folderId, String parentPath, int siteRootId)
      {
         m_folderId = folderId;
         m_relativeParentPath = parentPath;
         m_siteRootId = siteRootId;
      }

      /**
       * The root folder id of the site.
       */
      private int m_siteRootId;

      /**
       * the folder id.
       */
      private int m_folderId;

      /**
       * The relative (to the site root) parent folder path. It is
       * <code>/</code> if the folder id is the site folder itself or it is an
       * immediate sub-folder of the site. It is <code>/Files/<code>
       * if <code>folderId</code> is a folder under the site, for example,
       * <code>siteFolderPath/Files</code>.
       */
      private String m_relativeParentPath;
   }

   /**
    * Determines if the content list includes the items in quick edit state
    * where the CONTENTVALID column of the state is
    * {@link PSContentListItem#CONTENTVALID_FLAG_I}
    *
    * @return <code>true</code> if the parameter of {@link #CONTENTVALID_PARAM}
    *    includes the {@link PSContentListItem#CONTENTVALID_FLAG_I} character;
    *    otherwise return <code>false</code>.
    */
   protected boolean doesIncludeQuickEditItems()
   {
      String valid = m_request.getParameter(CONTENTVALID_PARAM, "").trim()
            .toLowerCase();
      StringTokenizer tokenizer = new StringTokenizer(valid, ",");
      while (tokenizer.hasMoreTokens())
      {
         String element = tokenizer.nextToken().trim();
         if (element.length() > 0
               && element.charAt(0) == PSContentListItem.CONTENTVALID_FLAG_I)
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Retrieves the content list from backend repository and appends into
    * {@link #m_contentList}. This has to be implemented by the derived class.
    * Assumes the {@link #m_contentList}has been initialized.
    *
    * @param parentFolderPath
    *           the parent folder path, assumed not <code>null</code> or empty.
    *           This path is relative to the <code>siteFolderPath</code>. It is
    *           <code>/</code> if the <code>folderId</code> is the id of the
    *           site folder itself or it is an immediate sub-folder of the site.
    *           It is <code>/Files/<code> if <code>folderId</code> is a folder
    *           under <code>siteFolderPath/Files</code>. This path is actual
    *           published path not the content explorer folder path.
    * @param folderId
    *           the to be published folder id of the site.
    * @param siteFolderPath
    *           the site folder path, may not be <code>null</code>.
    * @param filenameContext
    *           the value of <code>sys_content</code> HTML parameter.
    * @param appendFolderName
    *           determines if the folder name will be appended to the path used
    *           in location schemes: true to add the name, false to ignore the
    *           name. Generally, only false on the first call to this method.
    *
    * @throws PSUnknownNodeTypeException
    * @throws PSCmsException
    * @throws PSExtensionProcessingException
    */
   protected abstract void generateContentItems(String parentFolderPath,
         int folderId,
         String siteFolderPath, String filenameContext, boolean appendFolderName)
         throws PSUnknownNodeTypeException, PSCmsException,
         PSExtensionProcessingException;

   /**
    * Returns the default resource name used by the derived class.
    *
    * @return the resource name, never <code>null</code> or empty.
    */
   protected abstract String getDefaultResource();

   private PSContentList getCachedContentList(String sys_publicationid)
   {
      return (PSContentList) m_request.getSessionPrivateObject(
         CONTENT_LIST_CACHE_KEY + sys_publicationid);
   }

   private void setCachedContentList(
      String sys_publicationid,
      PSContentList list)
   {
      log.debug("caching content list for publication id=" + sys_publicationid);
      m_request.setSessionPrivateObject(
         CONTENT_LIST_CACHE_KEY + sys_publicationid,
         list);
   }

   private void clearCachedContentList(String sys_publicationid)
   {
      log.debug("clearing cached content list for publication id={}", sys_publicationid);
      m_request.setSessionPrivateObject(
         CONTENT_LIST_CACHE_KEY + sys_publicationid,
         null);
   }


   /**
    * Returns the publishable content valid values in the format that can be
    * used in a IN clause for SQL statement. For example, if the value of
    * {@link #m_publishableContentValidValues} is "y,i", then the returned
    * value will be: "'y','i'". Do nothing and return the
    * {@link #m_publishableContentValidValues} if it contains a single quote.
    *
    * @return the string in the format described above, never <code>null</code>
    *    or empty.
    */
   protected String getContentValidValues4SQL()
   {
      if (m_publishableContentValidValues.indexOf("'") >= 0)
         return m_publishableContentValidValues; // no need to convert

      StringBuffer values = new StringBuffer();

      StringTokenizer tokenizer = new StringTokenizer(
            m_publishableContentValidValues, ",");
      boolean isFirst = true;
      while (tokenizer.hasMoreTokens())
      {
         if (! isFirst)
            values.append(",");
         else
            isFirst = false;
         String tok = tokenizer.nextToken().trim();
         values.append("'");
         values.append(tok);
         values.append("'");
      }

      return values.toString();
   }

   /**
    * Indicates if folder should be excluded from publishing
    * based on its publish flag setting and the current contentlist
    * include mode.
    *
    * @param folderId the folder id.
    *
    * @return <code>true</code> if the folder should be excluded
    */
   protected boolean isFolderExcluded(int folderId)
   {
      if(m_folderIncludeMode.equals(INCLUDE_MODE_ALL))
         return false;

      String folderid = String.valueOf(folderId);
      if(m_folderIncludeMode.equals(INCLUDE_MODE_FLAGGED))
      {
         return m_flaggedFolders.contains(folderid) ? false : true;
      }
      else
      {
         return m_flaggedFolders.contains(folderid) ? true : false;
      }
   }

   /**
    * Returns a set of all folders that have the folder publish flag
    * set to a "true" value.
    *
    * @return a set of folder id's of all folders that have the publish
    * flag set to "true"
    */
   private Set getAllPublishFlaggedFolders()
   {
      Set flags = new HashSet();
      IPSInternalRequest iRequest =
         m_request.getInternalRequest(
            LOOKUP_FOLDER_PUBLISH_FLAGS,
            new HashMap(),
            false);
      if (iRequest == null)
      {
         log.error("Cannot find query resource: {}", LOOKUP_FOLDER_PUBLISH_FLAGS);
      }
      else
      {
         try
         {
            Document results = iRequest.getResultDoc();
            NodeList nl = results.getElementsByTagName(ELEM_PUBLISH_FLAG);
            Element flag = null;
            String value = null;
            int len = nl.getLength();
            for(int i = 0; i < len; i++)
            {
                flag = (Element)nl.item(i);
                value = flag.getAttribute(ATTR_VALUE);
                if(value.equalsIgnoreCase("yes")
                   || value.equalsIgnoreCase("true"))
                {
                   flags.add(flag.getAttribute(ATTR_FOLDERID));
                }
            }

         }
         catch (PSInternalRequestCallException e)
         {
            log.error("ERROR: while making internal request to {}", LOOKUP_FOLDER_PUBLISH_FLAGS);
            log.error(getClass().getName(), e);
            log.debug(e.getMessage(), e);
         }
      }
      return flags;
   }

   /**
    * Represents the entire content list.  Assigned in the (synchronized)
    * buildContentList method.
    */
   protected PSContentList m_contentList = null;

   /**
    * The current request object, used to obtain request parameters, logging,
    * and internal requests.  Never <code>null</code> after construction.
    */
   protected IPSRequestContext m_request;

   /**
    * The folder processor, init by ctor, never <code>null</code> after that.
    */
   protected PSServerFolderProcessor m_folderProcessor;

   /**
    * The relationship processor, init by ctor, never <code>null</code> after
    * that.
    */
   protected PSRelationshipProcessor m_relProcessor;

   /**
    * Set of all folders whose publish flag is set to "true".
    * Never <code>null</code>, may be empty.
    */
   protected Set m_flaggedFolders = new HashSet();

   /**
    * The relationship engine adaptor.  Initialized by
    * <code>initializeMembers</code> during construction, but will be
    * <code>null</code> if an error occurs.
    */
   protected PSRelationshipHelper m_helper;

   /**
    * Resource for looking up content and/or variants for this site.
    */
   protected String m_contentResourceName;

   protected final Logger log = LogManager.getLogger(PSSiteFolderCListBase.class);

   /**
    * Caches the sys_casGeneratePubLocation UDF used to build pub locations
    */
   protected PSSiteFolderContentListLinkGenerator m_generator =
      new PSSiteFolderContentListLinkGenerator();

   /**
    * When true, an incremental publish content list will be generated.
    * When false, a full publish content list will be generated.
    * Never <code>null</code> after construction.
    */
   protected boolean m_isIncremental;

   /**
    * Comma-delimited list of contentvalid values for states that are eligible
    * for publishing.  Never <code>null</code> after construction.
    */
   protected String m_publishableContentValidValues;

   /**
    * Name of the Rhythmyx internal resource used to retrieve all folder
    * publish flags.
    */
   static final String LOOKUP_FOLDER_PUBLISH_FLAGS =
      "sys_psxObjectSupport/getAllFolderPublishFlags.xml";

   /**
    * Is debug on? Evaluated based on if the context is real publishing or
    * preview which in turn based on whether the publication id exists in the
    * request or not. This will be <code>false</code> if the context is real
    * publishing <code>true</code> otherwise.
    */
   protected boolean m_debugModeOn;

   /**
    * Max rows per page of the content list, default is -1 (unlimited).
    */
   protected int m_maxRowsPerPage = -1;

   /**
    * Max pages of the content list, default is -1 (unlimited).
    */
   protected int m_maxPages = -1;

   /**
    * Max displayed page links of the content list, default is -1 (unlimited).
    * TODO what is DisplayedPageLinks???
    */
   protected int m_maxDisplayedPageLinks;

   protected String m_folderIncludeMode = "all";

   /**
    * Set of non-standard HTML parameters to pass from  request context to each
    * content item url in the content list, may be <code>null</code> or empty.
    */
   protected Set m_paramSetToPass = null;

   /**
    * Protocol for the HTTP request to be constructed for the content URL.
    * Never <code>null</code> or empty after construction. Never modified
    * after construction.
    */
   protected String m_protocol;

   /**
    * Host ip address or name for the HTTP request to be constructed for the
    * content URL. Never <code>null</code> or empty after construction. Never
    * modified after construction.
    */
   protected String m_host;

   /**
    * Port number for the HTTP request to be constructed for the content URL.
    * Never modified after construction.
    */
   protected int m_port;

   /**
    * String constant for the content list cache key used to cache the content
    * list.
    */
   public static final String CONTENT_LIST_CACHE_KEY =
      "com.percussion.fastforward.sfp.PSSiteFolderContentList.";

   /**
    * Folder include mode that will include all folders flagged
    * or not.
    */
   public static final String INCLUDE_MODE_ALL = "all";

   /**
    * Folder include mode that only include folders which are
    * flagged
    */
   public static final String INCLUDE_MODE_FLAGGED = "flagged";

   /**
    * Folder include mode that will only include folders which are
    * not flagged.
    */
   public static final String INCLUDE_MODE_UNFLAGGED = "unflagged";

   /**
    * XML Element representing a folder publish flag
    */
   protected static final String ELEM_PUBLISH_FLAG = "PublishFlag";

   /**
    * XML Attribute representing a folderid
    */
   private static final String ATTR_FOLDERID = "folderid";

   /**
    * XML Attribute representing a value
    */
   private static final String ATTR_VALUE = "value";

   /**
    * The HTML parameter name which contains a list of state flags and
    * comma delimited characters.
    */
   protected static final String CONTENTVALID_PARAM = "valid";

   /**
    * The HTML parameter name, set to "true" for Oracle database; otherwise
    * set to "false".
    */
   protected static final String IS_ORACLE_PARAM = "is_oracle";
}
