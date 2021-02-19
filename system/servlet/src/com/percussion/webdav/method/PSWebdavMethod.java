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

package com.percussion.webdav.method;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.client.IPSRemoteErrors;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.tools.PSCopyStream;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSWorkflowInfo;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.util.servlet.PSServletRequester;
import com.percussion.webdav.IPSWebdavConstants;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavConfig;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * This is the base class for all WebDAV methods. It provides convenient methods
 * for the derived (WebDAV method) class.
 *
 */
public abstract class PSWebdavMethod
   implements IPSWebdavConstants
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSWebdavMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      if (resp == null)
         throw new IllegalArgumentException("resp may not be null");
      if (servlet == null)
         throw new IllegalArgumentException("servlet may not be null");

      m_request = req;
      m_response = resp;
      m_webdavServlet = servlet;
      m_servletContext = servlet.getServletContext();
      
   }

   /**
    * Executes the methods.
    * 
    * @throws PSWebdavException if known error occurs.
    * @throws ServletException if caught unknown exception. 
    */
   public void execute() throws PSWebdavException, ServletException
   {
      try
      {
         logRequestInfo();
         parseRequest();
         processRequest();
      }
      catch (PSWebdavException we)
      {
         throw we;
      }
      catch (Exception ex)
      {
         ms_logger.error("Caught exception: ", ex);          
         throw new ServletException(ex);
      }
   }

   /**
    * Log the request info if the debug is turned on.
    * 
    * @throws IOException if I/O error occurs.
    */
   private void logRequestInfo() throws IOException
   {
      if (! ms_logger.isDebugEnabled())
         return;
      
        
      ms_logger.debug(""); 
      ms_logger.debug("RECEIVED REQUEST: =============== BEGIN ============="); 
      ms_logger.debug("REQUEST METHOD: <<<<<<<<<<" + m_request.getMethod() 
                     + ">>>>>>>>>>");
      ms_logger.debug("URL: " + m_request.getRequestURL());
      ms_logger.debug("QUERY STRING: " + m_request.getQueryString());
      ms_logger.debug("REMOTE USER: " + m_request.getRemoteUser());
      ms_logger.debug("CONTENT TYPE: " + m_request.getContentType());
      ms_logger.debug("CONTENT LENGTH: " + m_request.getContentLength());
      Enumeration names = m_request.getHeaderNames();
      while (names.hasMoreElements())
      {
         String name = (String)names.nextElement();
         ms_logger.debug("HEADER: " + name + "=" + m_request.getHeader(name));
      }
      String bodyString = null;
      if (m_request.getContentLength() > 0)
      {
         String mimeType = getMimeType();
         if (mimeType.toLowerCase().startsWith("text"))
         {
            bodyString = new String(getContent());
         }
         else
         {
            bodyString =
               "... binary content, " + getContent().length + " bytes ...";
         }
      }
      if (bodyString != null)
         ms_logger.debug("BODY:\n" + bodyString + "\n");
      ms_logger.debug("RECEIVED REQUEST: =============== END =============\n"); 
   }

   /**
    * Log the response info if the debug is turned on.
    * 
    * @param body The response body, may be <code>null</code> if the response
    *    does not have body part.
    * 
    * @param mimetype The mime type of the <code>body</code>, assume it is not 
    *    <code>null</code>.
    * 
    * @param enc The encoding of the <code>body</code>. It may be 
    *    <code>null</code>.
    * 
    * @param status The response status.
    * 
    * @throws IOException if I/O error occurs.
    */
   private void logResponseInfo(
      byte[] body,
      String mimetype,
      String enc,
      int status)
      throws IOException
   {
      if (! ms_logger.isDebugEnabled())
         return;

      ms_logger.debug("");
      ms_logger.debug("RESPONSE: =============== BEGIN =============");
      if (body != null && body.length > 0)
      {
         if (mimetype.toLowerCase().startsWith("text"))
         {
            if (enc == null)
               ms_logger.debug("RESPONSE BODY: \n" + new String(body) + "\n");
            else
               ms_logger.debug(
                  "RESPONSE BODY: \n" + new String(body, enc) + "\n");
         }
         else
         {
            ms_logger.debug(
               "RESPONSE BODY: ...binary content, "
                  + body.length
                  + " bytes...\n");
         }
      }
      
      if (status >= 0)
      {      
         ms_logger.debug("RESPONSE STATUS: " + status + " ("
               + PSWebdavStatus.getStatusText(status) + ")");
      }

      ms_logger.debug("RESPONSE: =============== END =============\n"); 
   }      
   
   /**
    * Contains the requested body. Initialized by {@link #getContent()}. It may
    * be <code>null</code> if has not been initialized yet.
    */
   private byte[] m_contentInByte = null;
   
   /**
    * Get the requested virtual path of Rhythmyx. It may be accessed directly,
    * which should only be done to handle cases where methods are being
    * called to access a folder that is not part of the virtual paths
    * laid out by the configuration.
    *
    * @return The normalized (with '/' separator) virtual path, the
    *    trailing '/' will be removed if exists, never <code>null</code>
    *    or empty.
    */
   protected String getRxVirtualPath()
   {
      if (m_rxVirtualPath == null)
      {
         String pathInfo = m_request.getPathInfo();
         
         m_rxVirtualPath = PSWebdavUtils.getRxVirtualPath(pathInfo, getConfig());
         
         m_rxVirtualPath = PSWebdavUtils.stripTrailingSlash(m_rxVirtualPath);
      }

      return m_rxVirtualPath;
   }
   
   /**
    * Get the Rhythmyx virtual path from the specified servlet path. 
    * For example, if the servlet patten is /Rhythmyx/rxwebdav, the servlet 
    * path would look like, /Rhythmyx/rxwebdav/Sites/EnterpriseInvestments/Files, 
    * then the Rhythmyx virtual path will be: //Sites/EnterpriseInvestments/Files.
    * 
    * @param srcPath the servlet path from which to get the Rhythmyx virtual
    *    path. It may not be <code>null</code> or empty.
    *    
    * @return the converted Rhythmyx virtual path, never <code>null</code> or
    *    empty.
    */
   protected String getRxVirtualPath(String srcPath)
   {
      String servletRoot = getServletRoot();
      
      if (srcPath.startsWith(servletRoot))
         srcPath = srcPath.substring(servletRoot.length());      

      srcPath = PSWebdavUtils.getRxVirtualPath(srcPath, getConfig());
      
      srcPath = PSWebdavUtils.stripTrailingSlash(srcPath);

      return srcPath;
   }
   
   /**
    * Determines the overwrite behavior from the "Overwrite" header of the
    * current request.
    *
    * @return <code>true</code> if the "Overwrite" header is "T";
    *    otherwise return <code>false</code>.
    */
   protected boolean isOverwrite()
   {
      String overwrite = m_request.getHeader(H_OVERWRITE);
      return overwrite != null && overwrite.equalsIgnoreCase("T");
   }

   /**
    * Get the file name and its extension from the requested virtual path.
    *
    * @return The file name and its extension of the source path,
    *    never <code>null</code>.
    */
   protected String getSourceFileName()
   {
      return PSWebdavUtils.getFileName(getRxVirtualPath());
   }

   /**
    * Read and return the contents from the request input stream.
    *
    * @return the content, never <code>null</code>, might be empty.
    * 
    * @throws IOException if any IO operation fails.
    */
   protected byte[] getContent() throws IOException
   {
      if (m_contentInByte == null)
      {
         if (m_request.getContentLength() <= 0)
         {
            m_contentInByte = new byte[0];
         }
         else
         {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try
            {
               PSCopyStream.copyStream(m_request.getInputStream(), os);
            }
            finally
            {
               try
               {
                  os.flush();
                  os.close();
               }
               catch (IOException e)
               {
                  // ignore, at least we tried
               }
            }
      
            m_contentInByte = os.toByteArray();
         }
      }
      
      return m_contentInByte;
   }

   /**
    * Get the request content and convert it to an XML document.
    *
    * @return The request content as an XML document, never <code>null</code>,
    *    may be empty if the content is empty.
    *
    * @throws PSWebdavException if failed to create XML document from the
    *    requested content.
    */
   protected Document getRequestDocument()
      throws PSWebdavException
   {
      if (m_requestDoc == null)
      {
         int contentLength = m_request.getContentLength();
         
         if (contentLength <= 0)
         {
            m_requestDoc = PSXmlDocumentBuilder.createXmlDocument();
         }
         else
         {
            try
            {
               InputStream in = null;
               if (m_contentInByte == null)
                  in = m_request.getInputStream();
               else
                  in = new ByteArrayInputStream(m_contentInByte);
               
               m_requestDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            }
            catch (Throwable e)
            {
               e.printStackTrace();
               
               throw new PSWebdavException(
                  IPSWebdavErrors.XML_FAILED_CREATE_DOC_FROM_CONTENT,
                  e.getLocalizedMessage(),
                  PSWebdavStatus.SC_BAD_REQUEST);
            }
         }
      }

      return m_requestDoc;
   }

   /**
    * Get the remote requester, which can be used to communicate with the
    * remote Rhythmyx Server.
    *
    * @return The remote requester, never <code>null</code>.
    */
   protected IPSRemoteRequester getRemoteRequester()      
   {
      if (m_remoteRequester == null)
      {
          PSServletRequester requester = new PSServletRequester(
            m_request,
            m_response,
            m_servletContext.getContext(getConfig().getRxServletURI()),
            getConfig().getRxUriPrefix());
            
            requester.setOverrideCommunityId(
               Integer.toString(getConfig().getCommunityId()));
               
            m_remoteRequester = requester;   
      }
      return m_remoteRequester;
   }
   
   /**
    * Gets the remote agent, which is used to perform remote operations
    * on cms item in Rhythmyx. All operations will override the 
    * community to the community id in the webdav config.
    * @return the remote agent, never <code>null</code>.
    */
   protected PSRemoteAgent getRemoteAgent()      
   {
      PSRemoteAgent agent = new PSRemoteAgent(getRemoteRequester());      
      return agent;
   }
   
   /**
    * Gets the PSWorkflowInfo instance which contains the information on states,
    * transitions, and contentValid. This information remains cached in the 
    * servlet.
    * 
    * @return an instance of the PSWorkflowInfo, never <code>null</code>.
    * @throws PSRemoteException if problems occur obtaining document.
    */
   protected PSWorkflowInfo getWorkflowInfo() throws PSRemoteException
   {
      synchronized (PSWorkflowInfo.class)
      {
         try
         {
            if (ms_workflowInstance == null)
            {
               Document doc = getRemoteRequester().getDocument(
                     "sys_psxWorkflowCataloger/workflowinfo.xml", null);
               ms_workflowInstance = new PSWorkflowInfo(doc);
            }
         }
         catch (Exception e)
         {
            throw new PSRemoteException(
                  IPSRemoteErrors.REMOTE_UNEXPECTED_ERROR,
                  e.toString());
         }
         return ms_workflowInstance;
      }
   }

   /**
    * Get the folder processor to handle the folder relationship.
    * 
    * @return The folder processor, never <code>null</code>.
    * 
    * @throws PSCmsException
    *            if an error occurs.
    */
   protected PSRelationshipProcessorProxy getFolderProxy()
      throws PSCmsException
   {
      return new PSRelationshipProcessorProxy(
         PSRelationshipProcessorProxy.PROCTYPE_REMOTE,
         getRemoteRequester(),
         FOLDER_TYPE);
   }

   /**
    * Get the component by a specified path.
    *
    * @param path The virtual path of the component, it may not not be
    *    <code>null</code> or empty.
    *
    * @return The retrieved component, it may be <code>null</code> if it does
    *    not exist or have no access to it.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   protected PSComponentSummary getComponentByPath(String path)
      throws PSWebdavException
   {
      return PSWebdavUtils.getComponentByPath(path, getRemoteRequester());
   }

   /**
    * Get the component by a specified path.
    *
    * @param path The virtual path of the component, it may not not be
    *    <code>null</code> or empty.
    *
    * @return The retrieved component, never <code>null</code>.
    *
    * @throws PSWebdavException if not exist, the error status is set to 404 
    *    (Not Found); or an error occurs.
    */
   protected PSComponentSummary getComponentByPathRqd(String path)
      throws PSWebdavException
   {
      return PSWebdavUtils.getComponentByPathRqd(path, getRemoteRequester());
   }

   /**
    * Get the mime type of the current request. If the mime type is not defined
    * then the <code>GENERIC_MIMETYPE</code> will be returned.
    * 
    * @return The mime type, never <code>null</code>.
    */
   protected String getMimeType()
   {
      String mimetype = null;
      HttpServletRequest request = getRequest();
      
      mimetype = request.getContentType();
      if (mimetype == null || mimetype.trim().length() == 0)
      {
         mimetype = getMimeTypeFromReqPathInfo();
      }
      else
      {
         int index = mimetype.indexOf(";");
         if (index != -1)
            mimetype = mimetype.substring(0, index);
      }
      
      return mimetype;
   }
   
   /**
    * Get the mime type from PathInfo of the request.
    * 
    * @return The mime type. return <code>GENERIC_MEMTYPE</code> if fail
    *    to get the mime type from PathInfo. Never <code>null</code> or empty.
    */
   protected String getMimeTypeFromReqPathInfo()
   {
      // Always use lowercase path (or file extension) to search for the mimetype
      // this is assuming that mimetype/file-extension mapping of the current
      // servlet and/or App Server contains lowercase file-extension only.
      //
      // The mimetype/file mapping is defined by <mime-mapping> element in the
      // web.xml of the current servlet or App Server. The "Default MIME Type 
      // Mappings" for Tomcat is defined in $TomcatHome/conf/web.xml
      String pathInfo = getRequest().getPathInfo().toLowerCase();
      
      String mimetype = getContext().getMimeType(pathInfo);
      if (mimetype == null)
         mimetype = GENERIC_MIMETYPE;
      
      return mimetype;
   }
   
   /**
    * Retrieves the <code>Depth</code> header from the request.
    *
    * @return The retrieved depth. It is <code>INFINITY</code>
    *    if the depth header not exist.
    */
   protected int getDepth()
   {
      String depthStr = getRequest().getHeader(H_DEPTH);
      int depth = INFINITY;

      if (depthStr == null)
      {
         depth = INFINITY;
      }
      else if (depthStr.equalsIgnoreCase("infinity"))
      {
         depth = INFINITY;
      }
      else
      {
         try
         {
            depth = Integer.parseInt(depthStr);
            if (depth < 0)
               depth = 0;
         }
         catch (NumberFormatException ex)
         {
             depth = INFINITY;
         }
      }

      return depth;
   }

   /**
    * creates the root element with the WebDAV namespace and the specified
    * node name. The created element always have an attribute
    * xmlns:D="DAV:" as a workaround for some of the XML transformers which 
    * have problem to handle namespace, for example xalan. Add the extra 
    * attribute seems to be ok for both xalan and saxon
    *
    * @param doc The document to be used to create the XML element, it may
    *    not be <code>null</code>.
    *
    * @param nodeName The XML node name, it may not be <code>null</code> or
    *    empty.
    *
    * @return The created XML element, never <code>null</code>.
    */
   protected Element createWebdavRootElement(Document doc, String nodeName)
   {
      return PSXmlDocumentBuilder.createRoot(
         doc,
         S_DAV_NAMESPACE,
         S_DAV_ALIAS,
         nodeName);
   }

   /**
    * creates an XML element with the WebDAV namespace and the specified
    * node name.
    *
    * @param doc The document to be used to create the XML element, it may
    *    not be <code>null</code>.
    *
    * @param nodeName The XML node name, it may not be <code>null</code> or
    *    empty.
    *
    * @return The created XML element, never <code>null</code>.
    */
   protected Element createWebdavElement(Document doc, String nodeName)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (nodeName == null || nodeName.trim().length() == 0)
         throw new IllegalArgumentException("nodeName may not be null or empty");

      // The current implementation of XML serializer (come with saxan) have 
      // trouble to handle a default namespace without any prefix. 
      // Add prefix "D:" as a workaround for now.  
      return doc.createElementNS(S_DAV_NAMESPACE, S_DAV_ALIAS + ":" + nodeName);      
   }

   /**
    * Set the response with the specified content and status. This should by
    * the last call to make for setting response data.
    *
    * @param contentEl The element for the response content, it may not be
    *    <code>null</code>.
    *
    * @param status The status of the response.
    *
    * @throws IOException if an I/O error occurs.
    */
   protected void setResponse(Element contentEl, int status) throws IOException
   {
      HttpServletResponse response = getResponse();

      response.setContentType(TEXT_XML_UTF_8);

      String docString = PSXmlDocumentBuilder.toString(contentEl);
      byte[] content = docString.getBytes(PSCharSets.rxJavaEnc());
      
      setResponse(content, TEXT_XML_UTF_8, PSCharSets.rxJavaEnc(), status);
      
   }

   /**
    * Set the response with the specified content and status. This should by
    * the last call to make for setting response data.
    *
    * @param content The response content, it may not be
    *    <code>null</code>.
    *
    * @param mimetype The mime type of the <code>content</code>. It may not be 
    *    <code>null</code>, but may be empty.
    * 
    * @param enc The encoding of the <code>content</code>. It may be 
    *    <code>null</code> if unknown.
    * 
    * @param status The status of the response.
    *
    * @throws IOException if an I/O error occurs.
    */
   protected void setResponse(
      byte[] content,
      String mimetype,
      String enc,
      int status)
      throws IOException
   {
      if (mimetype == null)
         throw new IllegalArgumentException("mimetype may not be null");
         
      logResponseInfo(content, mimetype, enc, status);
            
      HttpServletResponse response = getResponse();
      
      // must set the headers and status before write 
      // anything to the ServletOutputStream 
      response.setContentLength(content.length);
      setResponseStatus(status, false);

      ServletOutputStream out = response.getOutputStream();
      out.write(content);
      out.flush();
   }


   /**
    * Set the response status to the specified status.
    *
    * @param status The to be set status for the servlet response.
    *
    * @throws IOException if an I/O error occurs.
    */
   protected void setResponseStatus(int status) throws IOException
   {
      setResponseStatus(status, true);
   }
   
   /**
    * Set the response status to the specified status.
    *
    * @param status The to be set status for the servlet response.
    * 
    * @param logResponse <code>true</code> if wants to log the response status.
    *
    * @throws IOException if an I/O error occurs.
    */
   private void setResponseStatus(int status, boolean logResponse)
      throws IOException
   {
      if (logResponse)
         logResponseInfo(null, "", null, status);
         
      m_response.setStatus(status);
   }
   
   /**
    * Get the parent component summary of the specified component path.
    * 
    * @param compPath The component path, it may not be <code>null</code> or
    *    empty.
    * 
    * @return The parent component summary, never <code>null</code>.
    * 
    * @throws PSWebdavException if the specified path has no parent or
    *    fail to get the component summary of the parent, that is the 
    *    parent may not exist. 
    *    The status will be set to <code>PSWebdavStatus.SC_CONFLICT</code>. 
    */
   protected PSComponentSummary getParentSummary(String compPath)
      throws PSWebdavException
   {
      if (compPath == null || compPath.trim().length() == 0)
         throw new IllegalArgumentException("compPath may not be null or empty.");
         
      String parentPath = PSWebdavUtils.getParentPath(compPath);
      PSComponentSummary parentSummary = null;
      
      if (parentPath != null)
      {
         parentSummary = getComponentByPath(parentPath);
         if (parentSummary == null) // the parent component may not exists 
         {
            throw new PSWebdavException(
               IPSWebdavErrors.RESOURCE_NOT_FIND, 
               parentPath, 
               PSWebdavStatus.SC_CONFLICT);
         }
      }
      else // there is no parent path
      {
         throw new PSWebdavException(
            IPSWebdavErrors.RESOURCE_NOT_FIND, 
            compPath, 
            PSWebdavStatus.SC_CONFLICT);
      }
      
      return parentSummary;
   }

   /**
    * Creates the "lockdiscovery" element for the specified lock owner
    * 
    * @param doc The XML document, it may not be <code>null</code>.
    * 
    * @param lockOwner The owner of the lock, it may be <code>null</code> or
    *    empty if there is no lock owner. 
    * 
    * @return The created XML element, never <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    * @throws SAXException if failed to create XML document from lockOwner.
    */
   protected Element getLockdiscoveryElement(Document doc, String lockOwner)
      throws IOException, SAXException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      if (lockOwner == null || lockOwner.trim().length() == 0)
         return createWebdavElement(doc, E_LOCKDISCOVERY);
       
      Element ownerEl = null;
      if (lockOwner.startsWith("<?xml version"))
      {  
         StringReader reader = new StringReader(lockOwner);
         Document ownerDoc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
         ownerEl = ownerDoc.getDocumentElement();
         String nodeName = PSXMLDomUtil.getUnqualifiedNodeName(ownerEl);
         if (! nodeName.equals(E_OWNER))
         {
            ownerEl = createWebdavElement(ownerDoc, E_OWNER);
            ownerEl.appendChild(doc.createTextNode(lockOwner));
         }
      }
      else
      {
         ownerEl =
            createWebdavElement(
               PSXmlDocumentBuilder.createXmlDocument(),
               E_OWNER);
         ownerEl.appendChild(doc.createTextNode(lockOwner));
      }
      return getLockdiscoveryElement(doc, ownerEl);
   }
   
   /**
    * Creates the "lockdiscovery" element for the specified document and owner
    * 
    * @param doc The document that is used to create the XML element, it may
    *    not be <code>null</code>.
    * 
    * @param ownerEl The owner information for the created element.
    *    It may be <code>null</code>.
    * 
    * @return The created element, never <code>null</code>.
    */
   protected Element getLockdiscoveryElement(Document doc, Element ownerEl)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element lockdiscoveryEl = createWebdavElement(doc, E_LOCKDISCOVERY);
      
      if (ownerEl == null)
         return lockdiscoveryEl;

      // create the "lockdiscovery" element
      Element activelockEl = createWebdavElement(doc, E_ACTIVELOCK);
      lockdiscoveryEl.appendChild(activelockEl);
      
      // create the "locktype" element
      Element locktypeEl = createWebdavElement(doc, E_LOCKTYPE);
      Element writeEl = createWebdavElement(doc, E_WRITE);
      locktypeEl.appendChild(writeEl);
      activelockEl.appendChild(locktypeEl);
      
      // create the "lockscope" element
      Element lockscopeEl = createWebdavElement(doc, E_LOCKSCOPE);
      Element exclusiveEl = createWebdavElement(doc, E_EXCLUSIVE);
      lockscopeEl.appendChild(exclusiveEl);
      activelockEl.appendChild(lockscopeEl);

      // create the "depth" element
      Element depthEl = createWebdavElement(doc, E_DEPTH);
      depthEl.appendChild(doc.createTextNode("0"));
      activelockEl.appendChild(depthEl);

      // create the "owner" element      
      ownerEl = importOwnerElement(activelockEl.getOwnerDocument(), ownerEl);
      activelockEl.appendChild(ownerEl);
      
      // create the "timeout" element
      Element timeoutEl = createWebdavElement(doc, E_TIMEOUT);
      //String timeout = "Infinite";
      String timeout = "Second-" + Integer.MAX_VALUE; // always INFINITE
      timeoutEl.appendChild(doc.createTextNode(timeout));
      activelockEl.appendChild(timeoutEl);
      
      // create the "locktoken" element
      Element locktokenEl = createWebdavElement(doc, E_LOCKTOKEN);
      Element hrefEl = createWebdavElement(doc, E_HREF);
      locktokenEl.appendChild(hrefEl);
      String locktoken = S_LOCK_TOKEN + getRxVirtualPath();
      hrefEl.appendChild(doc.createTextNode(locktoken));
      activelockEl.appendChild(locktokenEl);

      return lockdiscoveryEl;
   }

   /**
    * Import the specified owner element to the supplied document. The
    * "owner-document" of the owner element may not be the same as the
    * supplied document.
    * 
    * @param doc The "owner-document" of the returned element, assume it
    *    is not <code>null</code>.  
    * 
    * @param ownerEl The to be imported element, assume it is not 
    *    <code>null</code>.
    * 
    * @return The imported element, never <code>null</code>.
    */
   private Element importOwnerElement(Document doc, Element ownerEl)
   {
      // Create the <owner> element, then import all its child nodes.
      // We do not import the whole element, otherwise
      // the imported element may looks like:
      //
      //  <owner xmlns="DEV:">Administrator</owner>
      //
      // MS-Word doesn't like the above.
      Element importOwner = createWebdavElement(doc, E_OWNER);
      
      NodeList nodes = ownerEl.getChildNodes();
      int length = nodes.getLength();
      for (int i=0; i<length; i++)
      {
         Node node = doc.importNode(nodes.item(i), true);
         importOwner.appendChild(node);
      }
      
      return importOwner;
   }
   
   /**
    * Get the current request object.
    *
    * @return The request, never <code>null</code>.
    */
   protected HttpServletRequest getRequest()
   {
      return m_request;
   }

   /**
    * Get the current response object.
    *
    * @return The response, never <code>null</code>.
    */
   protected HttpServletResponse getResponse()
   {
      return m_response;
   }

   /**
    * Get the configuration information.
    *
    * @return the configuration, never <code>null</code>.
    */
   protected PSWebdavConfig getConfig() 
   {
      try
      {
         return m_webdavServlet.getWebdavConfig();
      }
      catch (ServletException e)
      {
         throw new RuntimeException(e.getMessage());
      }
   }

   /**
    * Get the servlet context of the current servlet instance.
    *
    * @return The servlet context, never <code>null</code>.
    */
   protected ServletContext getContext()
   {
      return m_servletContext;
   }
   
   /**
    * Get the current webdav servlet instance.
    *
    * @return The servlet instance, never <code>null</code>.
    */
   protected PSWebdavServlet getServlet()
   {
      return m_webdavServlet;
   }

   /**
    * Parse WebDAV XML query. The default behavior is do nothing. Override this
    * if derived class need to parse the request.
    *
    * @exception PSWebdavException if an error occurs.
    */
   @SuppressWarnings("unused")
   protected void parseRequest() throws PSWebdavException
   {
   }

   /**
    * Validates the lock property of the supplied component. The response 
    * status will be set to 423 (LOCKED) if the component is locked by other
    * user and caller must stop current execution. 
    * 
    * @param comp The validated component, may not be <code>null</code>.
    * 
    * @return <code>false</code> if the component is locked (or checked out) by
    *    different user; otherwise return <code>true</code>. 
    *    NOTE: Comparing lock user is using case insensitive string comparison. 
    * 
    * @throws IOException if I/O exception occurs.
    */
   protected boolean validateLock(PSComponentSummary comp) throws IOException
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");
      
      String checkoutUser = comp.getCheckoutUserName();
      if (checkoutUser != null && checkoutUser.trim().length() > 0
            && (!checkoutUser.equalsIgnoreCase(getRequest().getRemoteUser())))
      {
         ms_logger.debug("\"" + comp.getName()
               + "\" has been checked out by another user, " + checkoutUser
               + ". Stop current execution.");

         setResponseStatus(PSWebdavStatus.SC_LOCKED);
         return false;
      }
      else
      {
         return true;
      }
   }

   /**
    * Deletes the supplied target component. The delete behavior will based
    * on the returned value of 
    * {@link com.percussion.webdav.objectstore.PSWebdavConfig#isDeleteAsPurge()}
    *  
    * @param parent the parent of the target component. It is not used if
    *    the delete behavior is purge. It must not be <code>null</code> if
    *    the delete behavior is remove folder relationship.
    * 
    * @param target the removed component. It may not be <code>null</code>.
    * 
    * @throws PSRemoteException if an error occurs.
    */
   protected void deleteComponent(PSComponentSummary parent,
         PSComponentSummary target) throws PSRemoteException
   {
      if (target == null)
         throw new IllegalArgumentException("target may not be null");
      
      PSRemoteAgent agent = getRemoteAgent();
      if (getConfig().isDeleteAsPurge())
      {
         agent.purgeTree(target);
      }
      else
      {
         if (parent == null)
            throw new IllegalArgumentException("parent may not be null");
         
         ArrayList<PSLocator> children = new ArrayList<>();
         children.add((PSLocator)target.getLocator());
         agent.removeComponentsFromFolder(parent.getLocator(),
               children);
      }
   }
   
   /**
    * Process the WebDAV XML request.
    *
    * @exception IOException an I/O error occurs.
    *
    * @exception PSWebdavException if an error occurs.
    */
   protected abstract void processRequest()
      throws PSWebdavException,
             IOException;


   /**
    * Gets the servlet root. The servlet root is the application pattern 
    * (/Rhythmyx) plus the servlet pattern (/rxwebdav). There is only one
    * servlet root path since there is only one WebDAV servlet in the
    * entire Rhythmyx application.
    * 
    * @return the servlet root, never <code>null</code> or empty.
    */
   private String getServletRoot()
   {
      if (ms_servletRoot != null)
         return ms_servletRoot;
      
      // pathInfo looks like: /EIFiles/New Folder
      String pathInfo = m_request.getPathInfo();
      
      // The pattern root looks like: /EIFiles/
      String patternRoot = PSWebdavUtils.getPatternRoot(pathInfo);

      // The URI looks like: /Rhythmyx/rxwebdav/EIFiles/New%20Folder
      String uri = m_request.getRequestURI();
      int index = uri.indexOf(patternRoot);
      
      // the servlet root looks like: /Rhythmyx/rxwebdav  
      ms_servletRoot = uri.substring(0, index);
      
      return ms_servletRoot;
   }
   
   /**
    * The servlet root, defaults to <code>null</code>, initialized by 
    * {@link #getServletRoot()}.
    */
   private static String ms_servletRoot = null;
   
   /**
    * Servlet request, initialized by ctor, never <code>null</code> after that.
    */
   private HttpServletRequest m_request;


   /**
    * Servlet response, initialized by ctor, never <code>null</code> after that.
    */
   private HttpServletResponse m_response;

   /**
    * The servlet context, initialized by ctor, never <code>null</code>
    * after that.
    */
   private ServletContext m_servletContext;

   /**
    * The XML document of the request content.
    */
   private Document m_requestDoc = null;
   
   /**
    * The Webdav servlet that called this method, never <code>null</code>.
    */
   private PSWebdavServlet m_webdavServlet;

   /**
    * The remote requester used to communicate with the remote Rhythmyx server.
    * It may be <code>null</code> if has not been set yet.
    */
   private IPSRemoteRequester m_remoteRequester = null;

   /**
    * Maintains singlton PSWorkflowInfo for WebDAV, 
    * see {@link #getWorkflowInfo()} for detail. Should be <code>null</code> 
    * until set once.
    */
   private static PSWorkflowInfo  ms_workflowInstance = null;
   
   /**
    * It is set by {@link #getRxVirtualPath()}, never <code>null</code> after
    * that. It may be <code>null</code> if has not been initialized.
    * See {@link #getRxVirtualPath()} for detail
    */
   protected String m_rxVirtualPath = null;

   /**
    * String constant for <code>text/xml; charset="UTF-8"</code>.
    */
   public static final String TEXT_XML_UTF_8 = "text/xml; charset=UTF-8";

   /**
    * The default depth.
    */
   public static final int INFINITY = Integer.MAX_VALUE;

   private Logger ms_logger = Logger.getLogger(PSWebdavMethod.class);
}



