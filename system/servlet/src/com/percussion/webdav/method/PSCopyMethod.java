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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Element;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.ws.PSLocatorWithName;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.util.PSCharSets;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;

/**
 * This class implements the COPY WebDAV method.
 */
public class PSCopyMethod extends PSWebdavMethod
{
   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSCopyMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   /**
    * Get the requested destination URL from the {@link #H_DESTINATION} request
    * header.
    *
    * @return the destination URL, never <code>null</code> or empty. It is the
    *    path of the URL. It does not include protocal, host or port. For 
    *    example, it returns "/rxwebdav/files" if the destination header is: 
    *    http://host:port/rxwebdav/files.
    *
    * @throws PSWebdavException if the "destination" header is invalid. 
    *    The error status is set to 400 (Bad request).
    */
   private String getDestinationPath() throws PSWebdavException
   {
      String destination = getRequest().getHeader(H_DESTINATION);
      
      if (destination == null || destination.trim().length() == 0)
      {
         throw new PSWebdavException(IPSWebdavErrors.HEADER_MISSING,
            H_DESTINATION, PSWebdavStatus.SC_BAD_REQUEST);
      }

      try
      {
         destination = URLDecoder.decode(destination, PSCharSets.rxJavaEnc());
         destination = PSWebdavUtils.stripTrailingSlash(destination);
         
         URL url = new URL(destination);
         destination = url.getPath();

      }
      catch (MalformedURLException e)
      {
         String[] args = {destination, H_DESTINATION};
         throw new PSWebdavException(IPSWebdavErrors.MALFORMED_URL_FROM_HEADER,
               args, PSWebdavStatus.SC_BAD_REQUEST);
      }
      catch (UnsupportedEncodingException e)
      {
         // ignore since it should never happen
      }
      
      return destination;
   }

   // Implements PSWebdavMethod.parseRequest()
   @Override
   protected void parseRequest() throws PSWebdavException
   {
      m_sourcePath = getRxVirtualPath();
      m_targetPath = getRxVirtualPath(getDestinationPath());
      
      if (m_sourcePath.equalsIgnoreCase(m_targetPath))
      {
         String[] args = { m_sourcePath, getRequest().getMethod()};
         throw new PSWebdavException(
            IPSWebdavErrors.FORBIDDEN_SRC_TARGET_SAME,
            args,
            PSWebdavStatus.SC_FORBIDDEN);
      }
      m_sourceSummary = getComponentByPathRqd(m_sourcePath);
      m_targetSummary = getComponentByPath(m_targetPath);
      if (m_targetSummary == null)
      {
         // if target not exist, make sure the parent folder exist
         m_targetParent = getParentSummary(m_targetPath);
      }
      else if (! isOverwrite()) 
      {   // if request does not specify overwrite, fail with 412 status code
          throw new PSWebdavException(
             IPSWebdavErrors.METHOD_FAIL_CANNOT_OVERWRITE,
             m_targetPath,
             PSWebdavStatus.SC_PRECONDITION_FAILED);
      }
      if (m_targetParent == null)
         m_targetParent = getParentSummary(m_targetPath);
   }

   /**
    * Get the supported depth by copy and move method.
    * 
    * @return 0 or <code>INFINITY</code>.
    */
   protected int getSupportedDepth()
   {
      int depth = getDepth();
      if (depth > 0)
         depth = INFINITY; // support 0 or INFINITY only
      else
         depth = 0;

      return depth;
   }
   
   /**
    * This need to be called at the beginning of {@link #processRequest()}.
    * It does the prepare work, such as validation and delete target if 
    * override is on.
    * 
    * @return <code>true</code> if successful done the prepare work; 
    *    <code>false</code> if validation failed. Caller must stop current
    *    execution.
    * 
    * @throws IOException if I/O error occurs.
    * @throws PSWebdavException if any other error occurs.
    */
   protected boolean preProcessRequest() throws PSWebdavException, IOException
   {
      // if target exist, then make sure it is not checked out by other user
      if (m_targetSummary != null && (! validateLock(m_targetSummary)))
         return false;
      
      try
      {
         // remove the target or destination if exist
         if (m_targetSummary != null)
            deleteComponent(m_targetParent, m_targetSummary);
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }
      
      return true;
   }
   
   // Implements PSWebdavMethod.processRequest()
   @Override
   protected void processRequest() throws PSWebdavException, IOException
   {
      if (! preProcessRequest())
         return;
      
      int depth = getSupportedDepth(); 
      
      try
      {
         if (m_sourceSummary.isFolder())
         {
            if (depth == 0)
               copyFolderOnly();
            else
               copyComponent();
         }
         else
         {
            copyComponent();
         }
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }
      if (m_targetSummary == null)
         setResponseStatus(PSWebdavStatus.SC_CREATED);
      else
         setResponseStatus(PSWebdavStatus.SC_NO_CONTENT);
   }
   
   /**
    * Copy a folder from the source to target (or destination). It is the
    * folder only, does not include items or sub-folders within the folder.
    * 
    * @throws PSUnknownNodeTypeException if error occurs when constructing
    *    folder object from XML.
    * @throws PSCmsException if any other error occurs.
    */
   private void copyFolderOnly()
      throws PSCmsException, PSUnknownNodeTypeException
   {
      PSComponentProcessorProxy proxy =
         new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE,
            getRemoteRequester());
      // retrieve the source folder
      String folderType = PSDbComponent.getComponentType(PSFolder.class);
      PSKey[] keys = { m_sourceSummary.getCurrentLocator()};
      Element[] folderEl = proxy.load(folderType, keys);
      PSFolder srcFolder = new PSFolder(folderEl[0]);
      // create the target folder from the source folder
      PSFolder targetFolder = (PSFolder) srcFolder.clone();
      // save the target folder
      IPSDbComponent[] comps = { targetFolder };
      proxy.save(comps);
      PSRelationshipProcessorProxy rsProxy = getFolderProxy();
      List<PSLocator> sourceList = new ArrayList<>();
      sourceList.add(m_sourceSummary.getCurrentLocator());
      rsProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         sourceList,
         m_targetParent.getCurrentLocator());
   }
   
   /**
    * Copy an item or folder from the source to target (or destination).
    * 
    * @throws PSCmsException if an error occurs.
    */
   private void copyComponent() throws PSCmsException
   {
      PSRelationshipProcessorProxy proxy = getFolderProxy();
      
      List<PSLocator> sourceList = new ArrayList<>();
      PSLocator srcLoc = m_sourceSummary.getCurrentLocator();
      String targetName = PSWebdavUtils.getFileName(m_targetPath);
      PSLocatorWithName locator = new PSLocatorWithName(srcLoc.getId(), 
            srcLoc.getRevision(), targetName);
      sourceList.add(locator);
      
      proxy.copy(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         sourceList,
         m_targetParent.getCurrentLocator());
   }
   
   /**
    * The component summary of the requested source. It is initialized by
    * {@link #parseRequest()}, never <code>null</code> after that.
    */
   protected PSComponentSummary m_sourceSummary = null;
   
   /**
    * The component summary of the requested destination. It is initialized by
    * {@link #parseRequest()}, it may be <code>null</code> if the target
    * does not exist.
    */
   protected PSComponentSummary m_targetSummary = null;
   
   /**
    * The destination or target parent. It is initialized by
    * {@link #parseRequest()}, it may be <code>null</code> if the target
    * does not exist.
    */
   protected PSComponentSummary m_targetParent = null;
   
   /**
    * The path of the requested source. It is the folder path in Rhythmyx.
    * It is set by {@link #parseRequest()}, never <code>null</code> or 
    * never modified after that.
    */
   protected String m_sourcePath = null;
   
   /**
    * The path of the destination. It is the folder path in Rhythmyx.
    * It is set by {@link #parseRequest()}, never <code>null</code> or 
    * never modified after that.
    */
   protected String m_targetPath = null;
   
   /**
    * <code>true</code> if the current request has been forwarded to another 
    * servlet; otherwise, <code>false</code>. Current servlet must do nothing
    * after the request is forwarded to another servlet. Default to 
    * <code>false</code>.
    */
   protected boolean m_hasForwarded = false;
}
