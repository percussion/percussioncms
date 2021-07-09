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
package com.percussion.webdav.method;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSWorkflowInfo;
import com.percussion.utils.spring.PSUrlHandlerMapping;
import com.percussion.webdav.IPSWebdavConstants;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities to be used by WebDAV methods.
 */
public class PSWebdavUtils
{
   /**
    * Get the file name and its extension from the specified path.
    *
    * @param path the file path, it may not be <code>null</code>.
    *
    * @return The file name and its extension of the path,
    *    never <code>null</code>.
    */
   public static String getFileName(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      int index = path.lastIndexOf("/");
      if (index != -1)
         path = path.substring(index + 1);
         
      return path;
   }
   
   /**
    * Get the parent path from the supplied path.
    *
    * @param path the given path, it may not be null, but may be empty.
    * 
    * @return the parent path. It may be <code>null</code> if there is no
    *    parent path.
    */
   public static String getParentPath(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      
      int i = path.lastIndexOf("/");
      if (i != -1)
      {
         String parentPath = path.substring(0, i);
         return parentPath;
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Add the specified child to the supplied parent folder.
    *
    * @param child the child locator, to be added to the parent folder,
    *    it may not be <code>null</code>.
    *
    * @param parent the parent locator, it may not be <code>null</code>.
    * 
    * @param remoteRequester The requester to be used to communicate with 
    *    the remote Rhythmyx Server. It may not be <code>null</code>.
    *
    * @throws PSCmsException if error occurs.
    */
   @SuppressWarnings(value={"unchecked"})
   public static void addToParentFolder(
      PSLocator child,
      PSLocator parent,
      IPSRemoteRequester remoteRequester)
      throws PSCmsException
   {
      if (child == null)
         throw new IllegalStateException("child cannot be null");
      if (parent == null)
         throw new IllegalStateException("parent cannot be null");
      if (remoteRequester == null)
         throw new IllegalArgumentException("remoteRequester may not be null");
         
      PSRelationshipProcessorProxy proxy =
         new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE,
            remoteRequester,
            PSWebdavMethod.FOLDER_TYPE);
            
      List children = new ArrayList();
      children.add(child);
      
      proxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         children,
         parent);
   }
   
   /**
    * Get the component by a specified path.
    *
    * @param path The virtual path of the component, it may not not be
    *    <code>null</code> or empty.
    *
    * @param remoteRequester The requester to be used to communicate with 
    *    the remote Rhythmyx Server. It may not be <code>null</code>.
    *
    * @return The retrieved component, never <code>null</code>.
    *
    * @throws PSCmsException if not exist or an error occurs.
    */
   private static PSComponentSummary getComponentByPathFromProxy(
      String path,
      IPSRemoteRequester remoteRequester)
      throws PSCmsException
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
      if (remoteRequester == null)
         throw new IllegalArgumentException("remoteRequester may not be null");
         
      PSRelationshipProcessorProxy proxy =
         new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE,
            remoteRequester);
      PSComponentSummary summary =
         proxy.getSummaryByPath(
            PSWebdavMethod.FOLDER_TYPE,
            path,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      return summary;
   }
   
   /**
    * Get the component by a specified path.
    *
    * @param path The virtual path of the component, it may not not be
    *    <code>null</code> or empty.
    *
    * @param remoteRequester The requester to be used to communicate with 
    *    the remote Rhythmyx Server.  It may not be <code>null</code>.
    *
    * @return The retrieved component, it may be <code>null</code> if it does
    *    not exist or have no access to it.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   public static PSComponentSummary getComponentByPath(
      String path,
      IPSRemoteRequester remoteRequester) throws PSWebdavException
   {
      try
      {
         return getComponentByPathFromProxy(path, remoteRequester);
      }
      catch (PSCmsException e)
      {
         throw new PSWebdavException(e);
      }
   }
   
   /**
    * Get the component by a specified path.
    *
    * @param path The virtual path of the component, it may not not be
    *    <code>null</code> or empty.
    *
    * @param remoteRequester The requester to be used to communicate with 
    *    the remote Rhythmyx Server. It may not be <code>null</code>.
    *
    * @return The retrieved component, never <code>null</code>.
    *
    * @throws PSWebdavException if not exist, the error status is set to 404 
    *    (Not Found); or an error occurs.
    */
   public static PSComponentSummary getComponentByPathRqd(
      String path,
      IPSRemoteRequester remoteRequester)
      throws PSWebdavException
   {
      try
      {
         PSComponentSummary summary =
            getComponentByPathFromProxy(path, remoteRequester);
            
         if (summary == null)
         {
            throw new PSWebdavException(
               IPSWebdavErrors.RESOURCE_NOT_FIND,
               path,
               PSWebdavStatus.SC_NOT_FOUND);
         }
         return summary;
      }
      catch (PSCmsException e)
      {
         LogManager.getLogger(PSWebdavUtils.class).debug(e.getMessage());
         throw new PSWebdavException(e);
      }
   }

   /**
    * Creates a folder with the specified name, the created folder will be
    * a child of the given parent. The created folder will inherit everything
    * from the supplied parent folder, except name, description and the
    * supplied exclude folder properties. The description of the created folder
    * will be empty.
    * 
    * @param folderName The to be created folder name, it may not be 
    *    <code>null</code> or empty.
    * 
    * @param excludeProperties The properties that will not be inherited from
    *    the parent folder. It is an iterator over zero or more 
    *    <code>String</code> objects, never <code>null</code>, but may be empty.
    * 
    * @param parentLocator The locator of the parent, it may not be
    *    <code>null</code>.
    * 
    * @param remoteRequester The requester to be used to communicate with 
    *    the remote Rhythmyx Server. It may not be <code>null</code>.
    * 
    * @return The created folder, never <code>null</code>.
    * 
    * @throws PSWebdavException if any other error occurs.
    */   
   @SuppressWarnings(value={"unchecked"})
   public static PSFolder createFolder(
      String folderName,
      Iterator excludeProperties,
      PSLocator parentLocator,
      IPSRemoteRequester remoteRequester)
      throws PSWebdavException
   {
      if (folderName == null || folderName.trim().length() == 0)
         throw new IllegalArgumentException(
            "folderName may not be null or empty");
      if (excludeProperties == null)
         throw new IllegalArgumentException("excludeProperties may not be null");
      if (parentLocator == null)
         throw new IllegalArgumentException("parentLocator may not be null");
      if (remoteRequester == null)
         throw new IllegalArgumentException("remoteRequester may not be null");
       
      try
      {
         PSComponentProcessorProxy compProxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, remoteRequester);

         PSKey[] keys = {parentLocator};
         Element folderEl =
            compProxy.load(IPSWebdavConstants.FOLDER_TYPE, keys)[0];
         PSFolder parentFolder = new PSFolder(folderEl);
         PSFolder folder = (PSFolder) parentFolder.clone();
         folder.setName(folderName);
         folder.setDescription("");
         while (excludeProperties.hasNext())
            folder.deleteProperty((String)excludeProperties.next());
         
         PSSaveResults results = compProxy.save(new IPSDbComponent[] {folder});
         folder = (PSFolder) results.getResults()[0];
         addToParentFolder(
            folder.getLocator(),
            parentLocator,
            remoteRequester);
            
         return folder;
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }
   }
   
   /**
    * Get the field by the supplied name
    * 
    * @param item The item object which contains the field, it may not be 
    *    <code>null</code>.
    * 
    * @param fieldName The field name, it may not be <code>null</code> or empty.
    * 
    * @return The field, never <code>null</code>.
    * 
    * @throws PSWebdavException if the field does not exist.
    */   
   public static PSItemField getFieldByName(PSClientItem item, String fieldName)
      throws PSWebdavException
   {
      if (item == null)
         throw new IllegalArgumentException("item may not be null");
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException("fieldName may not be null or empty");
         
      PSItemField field = item.getFieldByName(fieldName);
      if (field == null)
      {
         String args[] = {
            fieldName,
            String.valueOf(item.getContentId()),
            String.valueOf(item.getContentTypeId())
         };
         throw new PSWebdavException(IPSWebdavErrors.ITEMFIELD_NOT_EXIST, args);
      }
      else
      {
         return field;
      }
   }
   
   /**
    * Add the specified locator to the given parent folder.
    * 
    * @param parent the parent folder, it may not be <code>null</code>.
    * 
    * @param locator the child locator, to be added to the parent folder, 
    *    it may not be <code>null</code>.
    * 
    * @param remoteRequester The remote requester for communicating with the
    *    remote server. It may not be <code>null</code>.
    * 
    * @throws IllegalStateException if <code>m_parentSummary</code> is 
    *    <code>null</code>.
    * 
    * @throws PSCmsException if error occurs.
    */   
   @SuppressWarnings(value={"unchecked"})
   public static void addToParentFolder(
      PSComponentSummary parent,
      PSLocator locator,
      IPSRemoteRequester remoteRequester)
      throws PSCmsException
   {
      if (parent == null)
         throw new IllegalArgumentException("m_parentSummary cannot be null");
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");
      if (remoteRequester == null)
         throw new IllegalArgumentException("remoteRequester cannot be null");
         
      PSRelationshipProcessorProxy proxy = new PSRelationshipProcessorProxy(
         PSRelationshipProcessorProxy.PROCTYPE_REMOTE, remoteRequester);
      List children = new ArrayList();
      
      children.add(locator);
      proxy.add(
         IPSWebdavConstants.FOLDER_TYPE,
         PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         children,
         parent.getLocator());
   }

   /**
    * Get the servlet root from the supplied servlet request.
    * 
    * @param req the servlet request, it may not be <code>null</code>.
    * 
    * @return the servlet root, which is the ContextPath / ServletPath. It is
    *    never <code>null</code>.
    */
   public static String getServletRoot(HttpServletRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      String contextPath = req.getContextPath();
      String servletPath = req.getServletPath();
      String servletRoot = null;
      
      if (contextPath.length() > 0) 
      {  // the Servlet Context Path is starting with '/'
         if (servletPath.length() == 0)
         {
            servletRoot = contextPath;
         }
         else
         {
            if (servletPath.startsWith("/"))
               servletRoot = contextPath + servletPath;
            else
               servletRoot = contextPath + "/" + servletPath;
         }
      }
      else
      {
         servletRoot = servletPath;
      }

      return servletRoot;
   }
   
   /**
    * Get the virtual path of Rhythmyx from the specified request path.
    *
    * @param requestPath the requested path, it may be <code>null</code> or
    *    empty. This path must be the tail end of a full path, right after 
    *    the servletRoot. For example:
    *    if a full URL path is "/rxwebdav/intranet/folder1",  
    *    the servletRoot is "/rxwebdav/intranet", then the requestPath is
    *    "/folder1".  
    *
    * @param config the WebDAV configure object, it may not be <code>null</code>. 
    * 
    * @return The normalized (with '/' separator) virtual path,
    *    never <code>null</code> or empty.
    */
   public static String getRxVirtualPath(String requestPath,
         PSWebdavConfig config)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      
      String rxVirtualPath = null;

      // If emtpy path, just use the root path
      if (requestPath == null || requestPath.trim().length() == 0)
      {
         rxVirtualPath = config.getRootPath();
      }
      else
      {
         if (requestPath.equals("/") || requestPath.equals("\\"))
            rxVirtualPath = config.getRootPath();
         else
         {
            String pattern = PSUrlHandlerMapping.getUrlPath();
            if (pattern == null)
            {
               // this should never happen
               throw new RuntimeException(
                  "PSUrlHandlerMapping.getUrlPath() returned null");
            }
            
            // walk two paths until first difference, take the rest
            String[] patternPaths = pattern.replace('\\', '/').split("/");
            String[] reqPaths = requestPath.replace('\\', '/').split("/");
            StringBuffer reqBuf = new StringBuffer();
            
            for (int i = 0; i < reqPaths.length; i++)
            {
               if (i >= patternPaths.length || 
                  (!patternPaths[i].equals(reqPaths[i]) && 
                  StringUtils.isNotBlank(reqPaths[i])))
               {
                  if (reqBuf.length() > 0)
                     reqBuf.append('/');
                  reqBuf.append(reqPaths[i]);                  
               }  
            }
            
            String configPath = config.getRootPath().replace('\\', '/');
            if (!configPath.endsWith("/"))
               configPath += '/';
            rxVirtualPath = configPath + reqBuf.toString();
         }
      }
      
      return rxVirtualPath;
   }

   /**
    * Get the pattern root from the specified request path. This is opposite
    * of the {@link #getRxVirtualPath(String, PSWebdavConfig)}.
    *
    * @param requestPath the requested path, it may be <code>null</code> or
    *    empty. This path must be the tail end of a full path, right after 
    *    the servletRoot. For example:
    *    if a request path is "/intranet/folder1",  
    *    the servletRoot is "/intranet".  
    * 
    * @return The normalized (with '/' separator) pattern root,
    *    never <code>null</code> or empty.
    */
   public static String getPatternRoot(String requestPath)
   {
      // If emtpy path, just use the root path
      if (requestPath == null || requestPath.trim().length() == 0)
      {
         return requestPath;
      }
      
      String pattern = PSUrlHandlerMapping.getUrlPath();
      if (pattern == null)
      {
         // this should never happen
         throw new RuntimeException(
            "PSUrlHandlerMapping.getUrlPath() returned null");
      }
      
      // walk two paths until first difference, throw away the rest
      String patternPath = pattern.replace('\\', '/');
      String reqPath = requestPath.replace('\\', '/');
      
      int i = 0;
      for (; i < patternPath.length(); i++)
      {
         if (patternPath.charAt(i) != reqPath.charAt(i))
            break;
      }
         
      return patternPath.substring(0, i);
   }
   
   /**
    * Strips trailing slash '/' for the supplied path
    * 
    * @param path the path which may contain trailing slash, may not be
    *    <code>null</code>, but may be empty.
    * 
    * @return the string without trailing slash, never <code>null</code>.
    */
   public static String stripTrailingSlash(String path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      
      if (path.length() > 0)
      {
         int length = path.length();
         char lastChar = path.charAt(length-1);
         if (lastChar == '/')
            path = path.substring(0, length-1);
      }
      return path;
   }
   
   /**
    * A utility method to get the locator from the given component summary 
    * according to the checkin status for the current user.
    * 
    * @param compSummary The component, it may not be <code>null</code>.
    * @param httpRequest the current servlet request, never <code>null</code>. 
    * 
    * @return The "edit locator" if the component is checked out by the 
    *    current user; otherwise return the "current locator".
    */
   public static PSLocator getLocator(PSComponentSummary compSummary,
         HttpServletRequest httpRequest)
   {
      if (compSummary == null)
         throw new IllegalArgumentException("compSummary may not be null");
      if (httpRequest == null)
         throw new IllegalArgumentException("httpRequest may not be null");
      
      PSLocator locator = null;
      String checkoutUser = compSummary.getCheckoutUserName();
      String currentUser = httpRequest.getRemoteUser();
      if (checkoutUser != null &&      // if checked out by current user
          (checkoutUser.equalsIgnoreCase(currentUser)))
         locator = compSummary.getEditLocator(); 
      else
         locator = compSummary.getCurrentLocator();
         
      return locator;
      
   }
   
   /**
    * Transition the supplied item from public to quick-edit state if the item
    * is in public state.
    * 
    * @param summary the transitioned item, never <code>null</code>.
    * @param config the WebDAV Configuration, never <code>null</code>.
    * @param wfInfo the workflow info, never <code>null</code>.
    * @param remoteAgent the remote agent used to send request to Rhythmyx,
    *    never <code>null</code>.
    * @param httpRequest the current servlet request, never <code>null</code>.
    * 
    * @return <code>true</code> if successfully transitioned the item from 
    *    public to quick-edit state; <code>false</code> otherwise.
    *  
    * @throws PSRemoteException if error occurs while communicate with 
    *    Rhythmyx server.
    * @throws PSWebdavException if other error occurs.
    */
   public static boolean transitionFromPublicToQuickEdit(
         PSComponentSummary summary, PSWebdavConfig config,
         PSWorkflowInfo wfInfo, PSRemoteAgent remoteAgent,
         HttpServletRequest httpRequest) throws PSWebdavException, PSRemoteException
   {
      if (summary == null)
         throw new IllegalArgumentException("summary may not be null");
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      if (wfInfo == null)
         throw new IllegalArgumentException("wfInfo may not be null");
      if (remoteAgent == null)
         throw new IllegalArgumentException("remoteAgent may not be null");
      if (httpRequest == null)
         throw new IllegalArgumentException("httpRequest may not be null");
      
      if (wfInfo.isValidState(summary.getWorkflowAppId(), summary
            .getContentStateId(), config.getPublicValidTokens()))
      {
         /*
          * Item is in fact in the public state - so transistion it.
          * String transitionId
          */
         String transitionId = wfInfo.getTransitionId(summary
               .getWorkflowAppId(), summary.getContentStateId(), config
               .getQEValidTokens(), false);

         if (transitionId == null)
            throw new PSWebdavException(
                  IPSWebdavErrors.NO_PUBLIC_AUTO_TRANSITION);
         
         remoteAgent.transitionItem(getLocator(summary, httpRequest),
               transitionId, WEBDAV_AUTO_COMMENT);
         
         return true;
      }
      else
      {
         return false;
      }
      
   }
   
   /**
    * Transition the supplied item from quick-edit to public state if the item
    * is in quick-edit state and it was transitioned by WebDAV.
    * 
    * @param summary the transitioned item, never <code>null</code>.
    * @param config the WebDAV Configuration, never <code>null</code>.
    * @param wfInfo the workflow info, never <code>null</code>.
    * @param remoteAgent the remote agent used to send request to Rhythmyx,
    *    never <code>null</code>.
    * @param httpRequest the current servlet request, never <code>null</code>.
    * @param doTransition <code>true</code> simply perform the transition 
    *    without validating the state of the item; <code>false</code> if needs
    *    to validate the current state of the item before performing the 
    *    transition.
    * 
    * @return <code>true</code> if successfully transitioned the item from 
    *    quick-edit to public state; <code>false</code> otherwise.
    *  
    * @throws PSRemoteException if error occurs while communicate with 
    *    Rhythmyx server.
    * @throws PSWebdavException if other error occurs.
    */
   public static boolean transitionFromQuickEditToPublic(
         PSComponentSummary summary, PSWebdavConfig config,
         PSWorkflowInfo wfInfo, PSRemoteAgent remoteAgent,
         HttpServletRequest httpRequest, boolean doTransition)
         throws PSWebdavException, PSRemoteException
   {
      if (summary == null)
         throw new IllegalArgumentException("summary may not be null");
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      if (wfInfo == null)
         throw new IllegalArgumentException("wfInfo may not be null");
      if (remoteAgent == null)
         throw new IllegalArgumentException("remoteAgent may not be null");
      if (httpRequest == null)
         throw new IllegalArgumentException("httpRequest may not be null");
      
      // see if we need to validate the state of the item
      if (! doTransition)
      {
         // see if we are in quick-edit
         if (wfInfo.isValidState(summary.getWorkflowAppId(), summary
               .getContentStateId(), config.getQEValidTokens()))
         {
            // we are in QuickEdit
            String comment = remoteAgent.getLastTransitionComment(summary
                  .getContentId());
            if ((comment != null)
                  && (comment
                        .equalsIgnoreCase(WEBDAV_AUTO_COMMENT)))
            {
               doTransition = true;
            }
         }
      }
      
      if (doTransition)
      {
         /*
          * we made the transition to QuickEdit, figure out how to
          * transition out then
          */
         String transitionId = wfInfo.getTransitionId(summary
               .getWorkflowAppId(), summary.getContentStateId(), config
               .getPublicValidTokens(), true);

         if (transitionId == null)
            throw new PSWebdavException(IPSWebdavErrors.NO_QE_AUTO_TRANSITION);

         PSLocator locator = PSWebdavUtils.getLocator(summary, httpRequest);
         remoteAgent.transitionItem(locator, transitionId, null);
         
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * This is the auto transition comment used when transition an item from
    * public state to quick-edit state. 
    */
   private static final String WEBDAV_AUTO_COMMENT = "WEBDAV-AUTO-TRANS";
}
