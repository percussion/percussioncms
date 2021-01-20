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
package com.percussion.server;

import com.percussion.data.IPSDataErrors;
import com.percussion.design.objectstore.*;
import com.percussion.error.PSErrorHandler;
import com.percussion.log.PSLogHandler;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSUserEntry;
import com.percussion.server.content.PSContentParser;
import com.percussion.server.content.PSFormContentParser;
import com.percussion.server.content.PSXmlContentParser;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSServletRequestWrapper;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.*;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.security.PSSecurityCatalogException;
import com.percussion.utils.string.PSStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This version of the PSRequest object is simply a wrapper on a standard
 * <code>HttpServletRequest</code>. 
 */
@SuppressWarnings(value={"unchecked"})
public class PSRequest 
{
   static Log ms_log = LogFactory.getLog(PSRequest.class);


   /**
    * Construct a request object without attempting to parse the request body
    * from the servlet request.  
    * 
    * @param req the servlet request, may be <code>null</code> for internal
    * requests.
    * 
    * @param resp the response, may be <code>null</code> for internal
    * requests.
    * 
    * @param eh the error handler to use in case of error, may be
    * <code>null</code>.
    * 
    * @param lh the log handler to use for logging, may be <code>null</code>.
    */
   public PSRequest(HttpServletRequest req, HttpServletResponse resp,
      PSErrorHandler eh, PSLogHandler lh)
   {      
      if (req == null)
      {
         MockHttpServletRequest mock = new MockHttpServletRequest();

         Subject s = (Subject) PSRequestInfo
               .getRequestInfo(PSRequestInfo.SUBJECT);
         req = new PSServletRequestWrapper(mock, s);
      }
      if (resp == null)
      {
         resp = new MockHttpServletResponse();
      }
      
      // Do this early to avoid timing issues
      m_httpSession = req.getSession(true);
      
      String path = req.getContextPath() + req.getServletPath();
      if (req.getPathInfo()!=null)
          path+= req.getPathInfo();
      setRequestFileURL(path);
      m_servletRequest = req;
      m_servletResponse = resp;
      
      m_reqHookURL   = null;
      m_params       = new HashMap<String, Object>();
      
      // Process request parameters
      Iterator iter = req.getParameterMap().entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry ent = (Map.Entry) iter.next();
         String paramName = (String) ent.getKey();
         String arr[] = (String[]) ent.getValue();
         for(int i = 0; i < arr.length; i++)
         {
            appendParameter(paramName, arr[i]);
         }
      }
      
      // Check for the psxcharacterset parameter & set the character
      // set on the request if this parameter is specified
      String charset = getParameter(REQ_CHARSET_PARAM);
      if (!StringUtils.isBlank(charset))
         setCharacterSet(charset);
      
      m_inData = null;
      m_response     = null;
      m_session      = null;
      m_errorHandler = eh;
      m_logHandler = lh;
      m_stats        = new PSRequestStatistics();
      m_appHandler   = null;
      setPrivateObject(ASSEMBLY_RECURSION_MAP_KEY, new HashMap());
      m_requestTimer = new PSStopwatch();
      m_requestTimer.start();      
   }
   
   /**
    * Construct a request object and parse the request body.  Same as 
    * {@link #PSRequest(HttpServletRequest, HttpServletResponse, PSErrorHandler, 
    * PSLogHandler)} with additional details noted below.
    * 
    * @param parseBody when <code>true</code>, read and parse the input
    * stream from the servlet request, otherwise assume this will be handled
    * externally.  Ignored if the request was not a post.
    *
    * @throws IOException if there is an error reading the inputstream from the
    * request
    * @throws PSRequestParsingException if there are any errors parsing the 
    * request body.
    */
   public PSRequest(HttpServletRequest req, HttpServletResponse resp,
      PSErrorHandler eh, PSLogHandler lh, boolean parseBody) 
         throws PSRequestParsingException, IOException
   {      
      this(req, resp, eh, lh);
      
      if (!parseBody)
         return;


      parseBody();
   }   

   /**
    * Construct a request from a security token.  The request that is 
    * constructed will only contain session related information, and as such
    * should only be used for internal requests.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>tok</code> is <code>null</code>.
    */
   public PSRequest(PSSecurityToken tok)
   {
      this();
      
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      m_session = tok.getUserSession();
   }
   
   /**
    * Get a copy of this request object, including clones of the CGI variable
    * map, HTML parameter map, the cookie map, request file url, hook url
    * application handler and session. Private objects are copied shallow.
    *
    * @return a clone of this request, never <code>null</code>.
    */
   public PSRequest cloneRequest()
   {
      HashMap<String, Object> htmlParams = m_params == null ? null : (HashMap<String, Object>) m_params.clone();

      /* Some htmlParam entries can be ArrayList instead of string, these must
         be cloned individually for safety */
      if (htmlParams != null)
      {
         Iterator i = htmlParams.entrySet().iterator();
         while (i.hasNext())
         {
            Map.Entry ent = (Map.Entry) i.next();
            Object value = ent.getValue();
            if (value instanceof ArrayList)
               ent.setValue(((ArrayList)value).clone());
         }
      }

      PSRequest req = new PSRequest(null, null, null, null);
 
      req.putAllParameters(htmlParams);
      req.setApplicationHandler(getApplicationHandler());
      req.m_session = m_session;
      req.setInputDocument(getInputDocument());
      req.setErrorHandler(getErrorHandler());
      req.setLogHandler(getLogHandler());
      req.setRequestFileURL(getRequestFileURL());
      
      // Copy information to mock servlet request
      ServletRequestWrapper wrapper = (ServletRequestWrapper) req
            .getServletRequest();
      MockHttpServletRequest mockreq = (MockHttpServletRequest) wrapper
            .getRequest();      
      mockreq.setAuthType(m_servletRequest.getAuthType());
      mockreq.setCharacterEncoding(m_servletRequest.getCharacterEncoding());
      mockreq.setContentType(m_servletRequest.getContentType());
      mockreq.setContextPath(m_servletRequest.getContextPath());
      // Don't copy cookies!
      mockreq.setMethod(m_servletRequest.getMethod());
      mockreq.setPathInfo(m_servletRequest.getPathInfo());
      mockreq.setProtocol(m_servletRequest.getProtocol());
      mockreq.setQueryString(m_servletRequest.getQueryString());
      mockreq.setScheme(m_servletRequest.getScheme());
      mockreq.setSecure(m_servletRequest.isSecure());
      mockreq.setServerName(m_servletRequest.getServerName());
      mockreq.setServerPort(m_servletRequest.getServerPort());
      mockreq.setServletPath(m_servletRequest.getServletPath());
      mockreq.setRemoteAddr(m_servletRequest.getRemoteAddr());
      mockreq.setRemoteHost(m_servletRequest.getRemoteHost());
      mockreq.setRemoteUser(m_servletRequest.getRemoteUser());   
      mockreq.setRequestURI(m_servletRequest.getRequestURI());
      mockreq.setUserPrincipal(m_servletRequest.getUserPrincipal());
      
      if(m_privateObjects!=null)
      {
         req.m_privateObjects = new HashMap<Object, Object>();
         req.m_privateObjects.putAll(m_privateObjects);
      }
      
      if (m_clones != null)
      {
         req.m_clones = new HashMap<Serializable, Serializable>();
         req.m_clones.putAll(m_clones);
      }
      if (m_relationships != null)
      {
         req.m_relationships = new ArrayList<Serializable>();
         req.m_relationships.addAll((m_relationships));
      }

      //we want internal requests to share the same cache with parent callers
      req.m_contentItemStatusCache = m_contentItemStatusCache;

      //Set the root request for the cloned one.
      req.m_cloneParentRequest = isOriginalRequest() ? this : 
         m_cloneParentRequest;

      return req;
   }

   /**
    * Adds the supplied object to the list of objects cloned during this
    * request.
    *
    * @param originalId the content id of the original item to add, it may be 
    *    less than <code>0</code>.
    *    
    * @param clone the locator of the cloned item to add, not <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public void addClone(int originalId, PSLocator clone)
   {
      if (originalId < 0)
         throw new IllegalArgumentException("originalId cannot be < 0");

      if (m_clones == null)
         m_clones = new HashMap<Serializable, Serializable>();

      m_clones.put(new Integer(originalId), clone);
   }

   /**
    * Get the cloned locator for the supplied original content id.
    *
    * @param originalId the original content id to get the clone for, not
    *    <code>-1</code>.
    * @return the locator of the clone for the supplied original, may be
    *    <code>null</code> if the original was not cloned during this request.
    */
   public PSLocator getClonedLocator(int originalId)
   {
      if (m_clones == null)
         return null;

      return (PSLocator) m_clones.get(new Integer(originalId));
   }
   
   /**
    * Get the list of all original objects that have been cloned so far
    * during this request.
    *
    * @return the list of all original objects that have been cloned during
    *    this request, never <code>null</code>, might be empty.
    */
   public Iterator<Serializable> getOriginals()
   {
      if (m_clones == null)
         return PSIteratorUtils.emptyIterator();

      return m_clones.keySet().iterator();
   }

   /**
    * Get the list of all clones that have been created so far during this
    * request.
    *
    * @return the list of all clones that have been created during this request,
    *    never <code>null</code>, might be empty.
    */
   public Iterator<Serializable> getClones()
   {
      if (m_clones == null)
         return PSIteratorUtils.emptyIterator();

      return m_clones.values().iterator();
   }

   /**
    * Adds all supplied relationships to the list of relationships created
    * during this request.
    *
    * @param relationships the relationships to add, not <code>null</code>, may
    *    be empty.
    * @throws IllegalArgumentException if the supplied relationship is
    *    <code>null</code>.
    */
   public void addRelationships(PSRelationshipSet relationships)
   {
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");

      if (m_relationships == null)
         m_relationships = new ArrayList<Serializable>();

      m_relationships.addAll(relationships);
   }

   /**
    * Get the list of all relationships that have been created so far during
    * this request.
    *
    * @return the list of all relationships that have been created during
    *    this request, never <code>null</code>, might be empty.
    */
   public Iterator<Serializable> getRelationships()
   {
      if (m_relationships == null)
         return PSIteratorUtils.emptyIterator();

      return m_relationships.iterator();
   }

   /**
    * Get the user session assigned to this request.
    *
    * @return              the user session, or <code>null</code> if
    *                      session management is disabled
    */
   public synchronized PSUserSession getUserSession()
   {
      /* if we haven't gotten the session yet, do so now */
      if (m_session == null)
         m_session = PSUserSessionManager.getUserSession(this);

      return m_session;
   }
   
   /**
    * If this request has a user session stored, it will be refreshed in case
    * the user session referenced by the servlet request has been modified.
    */
   public synchronized void refreshUserSession()
   {
      /* if we haven't gotten the session yet, do so now */
      if (m_session != null)
      {
         m_session = null;
         m_session = getUserSession();
      }
   }   

   /**
    * Convenience method to get the session ID associated with this
    * object.
    *
    * @return              the user's session ID, or "" if
    *                      session management is disabled
    */
   public String getUserSessionId()
   {
      String ret = "";

      PSUserSession sess = getUserSession();
      if (sess != null)
      {
         ret = sess.getId();
         if (ret == null)
            ret = "";
      }

      return ret;
   }

   /**
    * Request a header setting. Calls {@link #getCgiVariable(String, String)}
    * with the arguments header and <code>null</code>
    */
   public String getCgiVariable(String header)
   {
      return getCgiVariable(header, null);
   }
   
   /**
    * Request a header setting 
    * @param header the header name, never <code>null</code> or empty
    * @param def the default value, may be <code>null</code>
    * @return the header, or the default if one doesn't exist
    */
   public String getCgiVariable(String header, String def)
   {
      if (header == null || header.trim().length() == 0)
      {
         throw new IllegalArgumentException("header may not be null or empty");
      }
      
      /*
       * First check the attributes for any data that was intentionally 
       * stored with the name of the header. Otherwise check the request
       * headers. 
       */
      String val = (String) m_servletRequest.getAttribute(header);
      if (val == null)
      {
         val = m_servletRequest.getHeader(header);
      }
      
      if (val != null)
      {
         return val;
      }
      else
      {
         return def;
      }
   }
   

   /**
    * Store information in the request's attributes that can be retrieved later
    * 
    * @param attribute the name of the attribute, never <code>null</code> or
    *           empty
    * @param value the value to store, never <code>null</code> or empty
    */
   public void setCgiVariable(String attribute, String value)
   {
      if (StringUtils.isEmpty(attribute))
      {
         throw new IllegalArgumentException("attribute name may not be null or empty");
      }
      if (value == null)
      {
         throw new IllegalArgumentException("value may not be null");
      }
      m_servletRequest.setAttribute(attribute, value);
   }   
   

   /**
    * Remove information from request's attributes
    * 
    * @param attribute the name of the attribute, never <code>null</code> or empty
    */
   public void removeCgiVariable(String attribute)
   {
      if (StringUtils.isEmpty(attribute))
      {
         throw new IllegalArgumentException("attribute may not be null or empty");
      }
      m_servletRequest.removeAttribute(attribute);
   }
   
   /**
    * Get the file portion of the request URL which was specified when
    * making this request.
    *
    * @return              the file portion of the request URL
    */
   public String getRequestFileURL()
   {
      return m_reqRoot + "/" + m_reqPage + getRequestPageExtension();
   }

   public void setRequestFileURL(String reqFileURL)
   {
      if (reqFileURL == null) {
         m_reqRoot      = "";
         m_reqPage      = "";
         m_reqPageType  = PAGE_TYPE_XML;
         return;
      }
      
      HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) 
         m_servletRequest;
      if (wrapper != null && 
            wrapper.getRequest() instanceof MockHttpServletRequest)
      {
         MockHttpServletRequest mockreq = (MockHttpServletRequest) 
            wrapper.getRequest();
         // Strip the first part of the path
         int cut = PSServer.getRequestRoot().length();
         if (cut < reqFileURL.length())
         {
            mockreq.setServletPath(reqFileURL.substring(cut));
         }
      }

      String subReq = "";

      int extPos = -1;
      int slashPos = reqFileURL.lastIndexOf('/');
      if ((slashPos > 0) && (slashPos < reqFileURL.length()))
      {
         subReq = reqFileURL.substring(slashPos+1);

         extPos = subReq.lastIndexOf('.');
         if (extPos > 0) {
            m_reqExt = subReq.substring(extPos);   /* include the . */
            extPos+=slashPos+1;
         }
         else
            m_reqExt = null;
      } else
         m_reqExt = null;

      /* there are four cases for the components of the URL:
       *    1. /serverRoot/appRoot/requestPage
       *    2. /serverRoot/requestPage
       *    3. /appRoot/requestPage
       *    4. /requestPage
       */
      String serverRoot = PSServer.makeRequestRoot(null);
      if (serverRoot != null) serverRoot = serverRoot.toLowerCase();

      int pagePos;
      if (serverRoot == null) {
         /* we always require a server root, so this is really an impossible
          * case. Since this can't happen anyway, we'll treat this as
          * /requestPage
          */
         pagePos = -1;  // assuming the whole name is the file for now
      }
      else if (!reqFileURL.toLowerCase().startsWith(serverRoot)) {
         /* this is not an E2 request?! - use the whole name for file */
         pagePos = -1;
      }
      else {
         /* we have a server root, which leaves two possibilities:
          *    1. /serverRoot/appRoot/requestPage
          *    2. /serverRoot/requestPage
          */
         /* if there's at least one more /, assume it's the app root */
         pagePos = reqFileURL.indexOf('/', serverRoot.length() + 1);

         /* JianHuang's added code and comment in the following block -- 11/19/1999 */
         // This block plays partial role in URL defaults implementation.
         // Even if pagePos = -1, it could be reqFileURL="/rhythmyx/default_urltest"
         // which has an app root called default_urltest
         if (pagePos == -1){
            int temp = reqFileURL.indexOf('/', serverRoot.length() - 1);
            if ((temp != -1) && (m_reqExt == null)){
               pagePos = reqFileURL.length();  // right after the reqFileURL string
               /*
               int rootLen = serverRoot.length();
               if ((pagePos == rootLen) || (pagePos == rootLen + 1)){
                  // The case could be reqFileURL="/rhythmyx" or "/rhythmyx/"
                  // Need to find default app name as appRoot. To make model simple,
                  // we ignore it here and deal with it in PSServer.getRequestHandler
                  // by calling setRequestFileURL method with the right argument.
               } */
            }
            else
               pagePos = serverRoot.length();   /* obviously no app root */
         }
      }

      /* now break the URL up into its parts */
      if (pagePos == -1) {
         m_reqRoot = "";
         if (extPos == -1)
            m_reqPage = reqFileURL;
         else
            m_reqPage = reqFileURL.substring(0, extPos);
      }
      else {
         m_reqRoot = reqFileURL.substring(0, pagePos);
         if (pagePos == reqFileURL.length())
            m_reqPage = "";
         else if (extPos < pagePos)
            m_reqPage = reqFileURL.substring(pagePos+1);
         else
            m_reqPage = reqFileURL.substring(pagePos+1, extPos);
      }


      if (extPos != -1) {
         String reqExtension = reqFileURL.substring(extPos+1);

         if (reqExtension.equalsIgnoreCase("XML"))
            m_reqPageType = PAGE_TYPE_XML;
         else if (reqExtension.equalsIgnoreCase("HTML"))
            m_reqPageType = PAGE_TYPE_HTML;
         else if (reqExtension.equalsIgnoreCase("HTM"))
            m_reqPageType = PAGE_TYPE_HTML;
         else if (reqExtension.equalsIgnoreCase("TXT"))
            m_reqPageType = PAGE_TYPE_TEXT;
         else
            m_reqPageType = PAGE_TYPE_UNKNOWN;
      }
      else
         m_reqPageType = PAGE_TYPE_XML;
   }
   /**
    * Get the root of the request URL. This often contains the E2 server
    * request root and the application request root.
    *
    * @return              the request root
    */
   public String getRequestRoot()
   {
      return m_reqRoot;
   }

   /**
    * Get the page of the request URL, including the file extension.
    *
    * @return              the request page
    */
   public String getRequestPage()
   {
      return getRequestPage(true);
   }

   /**
    * Get the page of the request URL.
    *
    * @param   includeExtension  <code>true</code> to include the file
    *                            extension in the returned page name
    *
    * @return                       the request page
    */
   public String getRequestPage(boolean includeExtension)
   {
      if (includeExtension)
         return m_reqPage + getRequestPageExtension();

      return m_reqPage;
   }

   /**
    * Get the type of page requested.
    *
    * @return              the PSRequest.PAGE_TYPE_xxx page type
    */
   public int getRequestPageType()
   {
      return m_reqPageType;
   }

   /**
    * Get the file extension associated with teh request page. The
    * returned extension includes the "." extension separator.
    *
    * @return                 the file extension, or "" if no extension was
    *                      specified
    */
   public String getRequestPageExtension()
   {
      if (m_reqExt != null)
         return m_reqExt;

      String reqExtension = "";

      switch (m_reqPageType) {
         case PAGE_TYPE_XML:
            reqExtension = ".xml";
            break;

         case PAGE_TYPE_HTML:
            reqExtension = ".html";
            break;

         case PAGE_TYPE_TEXT:
            reqExtension = ".txt";
            break;
      }

      return reqExtension;
   }

   /**
    * Get the URL of the hook through which the request was submitted.
    *
    * @return              the hook URL
    */
   public String getHookURL()
   {
      return m_reqHookURL;
   }

   /**
    * Set the URL of the hook through which the request was submitted.
    *
    * @param   reqHookURL  the hook URL
    */
   public void setHookURL(String reqHookURL)
   {
      m_reqHookURL = reqHookURL;
   }

   /**
    * Get value of a parameter that was passed in with the request.
    * <p>
    * Parameters are usually sent as part of the URL when issuing an HTTP
    * GET request.  They are also possibly able to be sent as the body
    * of an HTTP POST request.
    * <p>
    * When parsing the parameters specified in the URL (or in the POST data),
    * parameter names may be repeated. Rhythmyx stores the lists of data
    * as a <code>List</code> object. This method will only return the first list
    * entry for the matched item. If access to all the data in the list is
    * required, then use the {@link #getParameterList(String) getParameterList}
    * method.
    * <p>
    * For any non-string data, <code>toString()</code> will be called on the
    * object.
    * For objects such as file attachments, this will return the file name, not
    * the contents, which may not be desirable.
    *
    * @param name the name of the parameter to retrieve, may not be
    * <code>null</code> or empty.
    * @param defValue the value to return if a parameter of the specified name
    * does not exist, may be <code>null</code> or empty.
    *
    * @return the parameter's value, or <code>null</code> if it is not found and
    * default value is not provided.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public String getParameter(
      String name, String defValue)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty");

      if(m_params.isEmpty())
         return defValue;

      Object o = m_params.get(name);

      if (o instanceof List)
      {
         // if we actually have some elements in the list
         // get the first one and return that as a string
         // otherwise just return the default value
         if (((List<?>)o).size() > 0)
            o = ((List<?>)o).get(0);
         else
            return defValue;
      }

      return (o != null) ? o.toString() : defValue;
   }

   /**
    * Get the balanced object for a parameter which was passed in with the
    * request.
    * <p>
    * Parameters are usually sent as part of the URL when issuing a
    * HTTP GET request. It is also possible to send parameters as
    * the body of a HTTP POST request.
    * <P>
    * When processing form data for insert/update/delete, there may
    * be lists of data, which are stored as a <code>List</code> object.
    * This method will return list entry for the matched
    * item. If access to just the first item in the list is required, use the
    * <code>{@link #getSingleParameterObject(String, Object)
    * getSingleParameterObject}</code> method.
    *
    * @param name the name of the parameter to retrieve, may not be <code>null
    * </code> or empty.
    *
    * @param defValue the default value to return if the requested entry is not
    * found, may be <code>null</code>
    *
    * @return the requested parameter's value or <code>defValue</code>
    * if it is not found. May be <code>null</code> if <code>defValue</code> is
    * <code>null</code> and value is not found for the specified parameter.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public Object getParameterObject(String name, Object defValue)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty");

      Object retValue;

      if (m_params.isEmpty())
         retValue = defValue;
      else
      {
         Object o = m_params.get(name);
         if(o == null)
            retValue = defValue;
         else
            retValue = getBalancedValue( o, getMaxListValuesSize() );
      }

      return retValue;
   }

   /**
    * Gets the value balanced to the specified size. If size is <code>1</code>
    * the balanced value will be a single object in any case (either list object
    * or any object). Otherwise it will be a list object balanced to the
    * <code>size</code>.
    * <br.
    * Criteria for balancing the parameter list:
    * <ol>
    * <li>If value is not a list, then it returns the list of <code>size</code>
    * with each entry referring to the same object. </li>
    * <li>If value is a list and size of list is less than <code>size</code>,
    * then it returns the list of <code>size</code> filled remaining entries
    * with the last object in the list. </li>
    * <li>If value is a list and size of list is greater than <code>size</code>,
    * then it return the list with entries up to the <code>size</code>. </li>
    * <li>If we are returning a single object and value is list, then it gets
    * the first object in the list. </li>
    * </ol>
    *
    * @param value the parameter value to be balanced, assumed not to be
    * <code>null</code>
    * @param size the size to balance, assumed to be >= 1
    */
   private Object getBalancedValue(Object value, int size)
   {
      Object retValue = null;

      if (value instanceof List)
      {
         List<?> valList = (List<?>)value;
         int listSize = valList.size();

         if (size == 1) //requires a single object
         {
            if (listSize > 0)
               retValue = valList.get(0);
            else
               retValue =  null;  // not likely
         }
         else  //requires a list
         {
            // Truncate or Fill the list based on the list size and specified
            // size.
            if (listSize >= size)
            {
               /* Note: The object in the parameter map for lists is always
                * checked as ArrayList instance, not a List instance make an
                * array list.
                */
               retValue = new ArrayList(valList.subList(0, size)) ;
            }
            else
            {
               List<Object> balanceList;
               if(listSize > 0 )
               {
                  balanceList = new ArrayList<Object>(valList);
                  balanceList.addAll( createList(
                     valList.get(listSize-1), size-valList.size() ) );
               }
               else
                  balanceList = createList(null, size);  // not likely

               retValue = balanceList;
            }
         }
      }
      else
      {
         if(size == 1)   //requires a single object
            retValue =  value;
         else //requires a list
            retValue = createList(value, size);
      }

      return retValue;
   }

   /**
    * Get the object for a parameter which was passed in  with the request.  If
    * the parameter map contains lists, then the first object in the list is
    * returned.  Will return <code>null</code> if the list is empty or if the
    * first item in the list is <code>null</code>, and no default value has been
    * provided. See {@link #getParameterObject(String, Object)} for more info.
    *
    * @return The requested parameter's value, may be <code>null</code> if list.
    */
   public Object getSingleParameterObject(String name, Object defValue)
   {
      if (m_params.isEmpty())
         return defValue;

      Object o = m_params.get(name);
      if (o instanceof List)  // false if it is null
      {
         List<?> sourceList = (List<?>)o;
         if (sourceList.size() > 0)
            o = sourceList.get(0);
         else
            o = null;
      }

      return (o == null) ? defValue : o;
   }

   /**
    * Convenience method, calls {@link #getSingleParameterObject(String, Object)
    * getSingleParameterObject(name, null)}.
    */
   public Object getSingleParameterObject(String name)
   {
      return getSingleParameterObject(name, null);
   }

   /**
    * Convenience method, calls {@link #getParameter(String, String)
    * getParameter(name, null)}.
    */
   public String getParameter(String name)
   {
      return getParameter(name, null);
   }

   /**
    * Get the parameters which were passed in with the request.
    * <p>
    * Parameters are usually sent as part of the URL when issuing a
    * HTTP GET request. It is also possible to send parameters as
    * the body of a HTTP POST request.
    * <p>
    * Modifying the returned HashMap will directly modify this object.
    * Any subsequent use of the getParameter or getParameters methods
    * will see the changed data.
    *
    * @return the request parameters, never <code>null</code>, may be empty.
    */
   public HashMap getParameters()
   {
      return m_params;
   }

   /**
    * Get values of a parameter that was passed in with the request as an array
    * of objects.
    * <br>
    *
    * @param name the name of the parameter to retrieve, may not be
    * <code>null</code> or empty.
    *
    * @return the array of values for the specified parameter, or
    * <code>null</code> if it is not found.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
    public Object[] getParameterList(String name)
    {
       Object value = getParameterObject(name, null);

       if(value instanceof List)
       {
         List<?> values = (List<?>)value;
         return values.toArray();
       }
       else if (value != null)
       {
         return new Object[]{ value };
       }
       else
          return null;
    }

   /**
    * Removes a parameter with specified name from the request parameters if it
    * is found.
    *
    * @param name the name of the parameter, may be <code>null</code> or empty.
    *
    * @return the value of the parameter. If name is <code>null</code> or empty
    * or parameter is not found for the specified name, <code>null</code> will
    * be returned.
    */
   public Object removeParameter(String name)
   {
      if(name == null || name.trim().length() == 0)
         return null;

      return m_params.remove(name);
   }

   /**
    * Appends the supplied value to the list of values of the parameter with
    * supplied name. If the parameter does not exist it creates an entry for it
    * and sets the value as parameter value.
    *
    * @param name the name of the parameter, may not be <code>null</code> or
    * empty.
    * @param value object to be appended to the list of values, may be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    * @throws IllegalStateException if the type of objects in the parameter
    * values does not match the type of object being appended.
    */
   public void appendParameter(String name, Object value)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty");

      Object paramValue = m_params.get(name);
      if(paramValue instanceof List)
      {
         List<Object> values = (List<Object>)paramValue;
         if(!values.isEmpty())
         {
            if( !values.get(0).getClass().isAssignableFrom(value.getClass()) )
               throw new IllegalStateException(
                  "The value of parameter to append is not of correct type.");
         }
         values.add(value);
      }
      else if(paramValue != null)
      {
         if( !paramValue.getClass().isAssignableFrom(value.getClass()) )
            throw new IllegalStateException(
               "The value of parameter to append is not of correct type.");

         List<Object> values = new ArrayList<Object>();
         values.add(paramValue);
         values.add(value);
         m_params.put(name, values);
      }
      else
      {
         m_params.put(name, value);
      }
   }

   /**
    * Checks whether any of the parameters have multiple values.
    *
    * @return <code>true</code> if any of the parameters have multiple values,
    * otherwise <code>false</code>
    */
   public boolean hasMultiValuesForAnyParameter()
   {
      Iterator<Object> iter = m_params.values().iterator();

      while(iter.hasNext())
      {
         Object value = iter.next();
         if(value instanceof List && !((List<?>)value).isEmpty())
            return true;
      }
      return false;
   }

   /**
    * Checks whether a param by the specified name is the only multi-valued
    * param in the set of all params.
    * 
    * @param name The name of the param to check. Never <code>null</code> or
    * empty.
    * 
    * @return If the param is present in the param map and its value
    * is a list, but all other values are not lists, then <code>true</code> is
    * returned, otherwise, <code>false</code>.
    */
   public boolean isOnlyMultiValueParam(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name can not be null or empty");
      Object o = m_params.get(name);
      if (o instanceof List)
      {
         for (String key : (Set<String>) m_params.keySet())
         {
            if (key.equals(name))
               continue;
            if (m_params.get(key) instanceof List)
               return false;
         }
         return true;
      }
      return false;
   }
   
   /**
    * Get the character set associated with the request.  The value
    * returned will be either the request-specific encoding for this request
    * or the Rhythmyx server's default character encoding if one has not
    * been set.
    *
    * @return The request's character encoding,
    *         never <code>null</code> or empty.
    */
   public String getCharacterSet()
   {
      return m_encoding == null ? PSServer.getDefaultServerHttpCharset() :
         m_encoding;
   }

   /**
    * Retrieve a temp file resource for this request.
    *
    * @param key The key for the file.  Must not be <code>null</code> or empty.
    *
    * @return the file stored for the specified key, may be <code>null</code>.
    *
    * @throws IllegalArgumentException if the key is <code>null</code>
    *
    * @see #addTempFileResource(PSPurgableTempFile, String)
    */
   public PSPurgableTempFile getTempFileResource(String key)
   {
      if ((key == null) || (key.length() == 0))
         throw new IllegalArgumentException("Must supply a key");

      if (m_tempFileResources == null)
         return null;
      else
         return (PSPurgableTempFile) m_tempFileResources.get(key);
   }

   /**
    * Add a temp file resource to this request.  This allows purgable temp
    * files to be retrieved directly by the xml field extractor, which allows
    * the information held by the purgable temp file object to be accessed
    * so that character set information will not be lost from the file upload.
    *
    * @param f The file resource, must not be <code>null</code>.
    *
    * @param key The string to key the file off of, never <code>null</code> or
    *          empty.
    *
    * @throws IllegalArgumentException if either parameter is <code>null</code>,
    *          or <code>key</code> is empty.
    */
   public void addTempFileResource(PSPurgableTempFile f, String key)
   {
      if ((key == null) || (key.length() == 0))
         throw new IllegalArgumentException("Must supply a key.");

      if (f == null)
         throw new IllegalArgumentException("Must supply a file resource.");

      if (m_tempFileResources == null)
         m_tempFileResources = new HashMap<String, Object>();

      m_tempFileResources.put(key, f);
   }

   /**
    * Get the "file-upload" character set associated with the request.  The
    * value returned will be either the request-specific encoding for this
    * request or the Rhythmyx server's default character encoding if one has not
    * been set.
    *
    * @return The request's character encoding,
    *         <code>null</code> indicates to use the Java system's default
    *         file encoding.  Never empty.
    */
   public String getFileCharacterSet()
   {
      return m_fileEncoding == null ? PSServer.getDefaultServerFileCharset() :
         m_fileEncoding;
   }

   /**
    * Set the file-upload character set associated with the request.  This
    * should be either the IANA MIME-preferred registry name of the character
    * set or Java's canonical name for the character set (preferred).
    *
    * @param encoding The request's character encoding, use <code>null</code>
    *          or empty to indicate to use the Java system's default
    *          file encoding.
    */
   public void setFileCharacterSet(String encoding)
   {
      if (encoding != null && encoding.length() == 0)
         encoding = null;

      m_fileEncoding = encoding;
   }

   /**
    * Set the character set associated with the request.  This should be
    * either the IANA MIME-preferred registry name of the character set or
    * Java's canonical name for the character set (preferred).
    *
    * @param encoding The request's character encoding, use <code>null</code>
    *          or empty to indicate to use the Rhythmyx server's default
    *          encoding.
    */
   public void setCharacterSet(String encoding)
   {
      if (encoding != null && encoding.length() == 0)
         encoding = null;

      m_encoding = encoding;
   }

   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have single values.  If the parameter values are
    * ArrayLists, they will be truncated to the first Object in the list.
    *
    * Modifying the returned Map will not directly modify this object.
    *
    * @return The request parameters map containing single values.  The key is
    * the String representation of the parameter name, and the value is an
    * Object, usually a String, but possibly something else (e.g. a File
    * object), but no entries should have List objects for their values. Never
    * <code>null</code>, may be empty.
    */
   public Map<Object, Object> getTruncatedParameters()
   {
      Map<Object, Object> result = null;

      if (m_params.isEmpty())
         result = new HashMap<Object, Object>();
      else
         result = getBalancedParameters( 1 );

      return result;
   }


   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have the same number of values as the specified
    * parameter name.  If the parameter values are  Lists, and the specified
    * parameter is not, they will be truncated to the first Object in the list.
    * Otherwise they will remain Lists and will be trunctated to the same number
    * of elements as the specified parameter originally contained. Modifying the
    * returned Map will not directly modify this object.
    *
    * @param origParam The parameter to use as a guide when truncating lists.
    * May not be <code>null</code> or empty.  Must be an existing parameter.
    *
    * @return The request parameters map containing truncated values.
    *
    * @throws IllegalArgumentException if origParam is <code>null</code> or
    * empty.
    *
    * @throws IllegalStateException if the origParam does not exist in the
    * request parameters map.
    *
    * @deprecated Use {@link #getBalancedParameters(String origParam) } instead.
    */
   public Map<Object, Object> getTruncatedParameters(String origParam)
   {
      if(origParam == null || origParam.trim().length() == 0)
         throw new IllegalArgumentException(
         "origParam may not be null or empty");

      Map<Object, Object> result =  getBalancedParameters( origParam );

      return result;
   }


   /**
    * Copies all of the mappings from the specified map to this request's
    * parameters map. These mappings will replace any mappings that this map
    * had for any of the keys currently in the specified map.
    *
    * @param params Additional mappings to be stored in this request's
    * parameters map, not <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>params</code> is
    * <code>null</code>.
    */
   public void putAllParameters(Map<String, Object> params)
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");
      m_params.putAll( params );
   }

   /**
    * Set the parameters map of this request with the supplied parameters. 
    * Existing parameters will be overwitten.
    * 
    * @param params an iterator over <code>Map.Entry</code> objects with
    *    the key as parameter name and the value as parameter value, not
    *    <code>null</code>, may be empty.
    */
   public void setParameters(Iterator params)
   {
      if (params == null)
         throw new IllegalArgumentException("params can not be null");
         
      m_params = new HashMap<String, Object>();
      while (params.hasNext())
      {
         Map.Entry<String,Object> entry = (Map.Entry<String, Object>) params.next();
         m_params.put(entry.getKey(), entry.getValue());
      }
      
      if (m_serverRequest != null && m_serverRequest != this)
         m_serverRequest.setParameters(params);
   }

   /**
    * Set the parameters which were passed in with the request.
    * <p>
    * Parameters are usually sent as part of the URL when issuing a
    * HTTP GET request. It is also possible to send parameters as
    * the body of a HTTP POST request.
    * <p>
    * Any subsequent use of the getParameter or getParameters methods
    * will see the changed data.
    *
    * @param   params the parameters sent with the request URL, may not be
    * <code>null</code>, can be empty.
    *
    * @throws IllegalArgumentException if params is <code>null</code>
    */
   public void setParameters(HashMap<String, Object> params)
   {
      if (params == null)
         throw new IllegalArgumentException("params can not be null");

      m_params = params;
      if (m_serverRequest != null && m_serverRequest != this)
         m_serverRequest.setParameters(params);
   }

   /**
    * Replaces the value of an existing entry or creates a new entry in the
    * parameter map.
    * <p>
    * Parameters are usually sent as part of the URL when issuing a
    * HTTP GET request. It is also possible to send parameters as
    * the body of a HTTP POST request. When parsing the parameters specified in
    * the URL (or in the POST data), parameter names may be repeated. Rhythmyx
    * stores the lists of data as a <code>List</code> object. So to set such
    * type of parameter use value as <code>List</code> object.
    * <p>
    * Note: If this method is used in the existing code(in the exits before
    * v4.1) to set a parameter value which is already balanced, server treats
    * the copies in the balanced list as new values rather than repeated values.
    *
    * @param name The name of the parameter to add/modify.  May not be
    * <code>null</code> or empty.
    *
    * @param value An Object, the value to set it to.  May be <code>null</code>.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public void setParameter(String name, Object value)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty.");

      m_params.put(name, value);
      
      if (m_serverRequest != null && m_serverRequest != this)
         m_serverRequest.setParameter(name, value);

   }
   /**
    * Gets the list of parameters with each element as an entry set with
    * parameter name as key and parameter value as value. Key is a <code>String
    * </code> object, whereas the value can be any object. This iterator cannot
    * be modified.
    *
    * @return the request parameters, never <code>null</code>, may be empty.
    */
   public Iterator<?> getParametersIterator()
   {
      return PSIteratorUtils.protectedIterator(
         m_params.entrySet().iterator() );
   }

   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have the same number of values as the specified
    * parameter name.
    * <br>
    * If the value of the specified parameter is a <code>List</code>, then the
    * returned map will have values of all parameters as <code>List</code> with
    * its size equal to the size of specified parameter value, otherwise the
    * returned map will have values of all parameters as single object. In doing
    * this, if any parameter value is not a <code>List</code> and we are
    * returning <code>List</code>, then it creates the <code>List</code> with
    * each entry referring to the same object. If we are returning a single
    * object and any parameter value is <code>List</code>, then it gets the
    * first object in the <code>List</code>.
    * <br>
    * Modifying the returned Map will not directly modify this object.
    *
    * @param controlParam The parameter to use as a guide to get balanced list.
    * May not be <code>null</code> or empty.  Must be an existing parameter.
    *
    * @return The request parameters map containing balanced values, never
    * <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if controlParam is <code>null</code> or
    * empty.
    *
    * @throws IllegalStateException if the controlParam does not exist in the
    * request parameters map.
    */
   public Map<Object, Object> getBalancedParameters(String controlParam)
   {
      if(controlParam == null || controlParam.trim().length() == 0)
         throw new IllegalArgumentException(
            "controlParam can not be null or empty.");

      if(m_params.containsKey(controlParam))
      {
         Object value = m_params.get(controlParam);
         if(value instanceof List && !((List<?>)value).isEmpty())
            return getBalancedParameters( ((List<?>)value).size() );
         else
            return getBalancedParameters( 1 );
      }
      else
         throw new IllegalStateException(
            "The parameter to use as a guide for balancing other parameter " +
            "values is not found. Cannot balance.");
   }

   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have the same number of values. If none of the
    * parameters has multiple values, then all values are single objects,
    * otherwise they will be lists of size equal to maximum size of list values.
    * <br>
    * Modifying the returned Map will not directly modify this object.
    *
    * @return The request parameters map containing balanced values, may not be
    * <code>null</code>, may be empty.
    */
   public Map<Object, Object> getBalancedParameters()
   {
      int maxListSize = getMaxListValuesSize();

      return getBalancedParameters( maxListSize );
   }

   /**
    * Gets the maximum size of list values of parameters. If the parameters does
    * not have any list values, then it returns <code>1</code> to indicate all
    * parameter values are single objects.
    *
    * @return the maximum size of listvalues
    */
   public int getMaxListValuesSize()
   {
      int max = 1;

      Iterator<Object> iter = m_params.values().iterator();

      while(iter.hasNext())
      {
         Object value = iter.next();
         if(value instanceof List)
         {
            List<?> listValue = (List<?>)value;
            if(max < listValue.size())
               max = listValue.size();
         }
      }

      return max;
   }

   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have the values as list with the specified size. If
    * size is equal to 1, then the values are single objects.
    * <br>
    * Criteria for balancing the parameter list:
    * <ol>
    * <li>If parameter value is not a list, then it creates the list with each
    * entry referring to the same object. </li>
    * <li>If parameter value is a list and size of list is less than <code>
    * size</code>, then it fills the list to the <code>size</code> with each
    * entry referring to the last object in the list. </li>
    * <li>If parameter value is a list and size of list is greater than <code>
    * size</code>, then the list with entries up to the <code>size</code> is
    * set. </li>
    * <li>If we are returning a single object and any parameter value is list,
    * then it gets the first object in the list. </li>
    * </ol>
    * <br>
    * Modifying the returned Map will not directly modify this object.
    *
    * @param size the size of balanced parameter values to be, assumed to be >=1
    * @return The request parameters map containing balanced values, may not be
    * <code>null</code>, may be empty.
    */
   private Map<Object, Object> getBalancedParameters(int size)
   {
      Map<Object, Object> result = new HashMap<Object, Object>(m_params.size());

      Iterator<?> entries = m_params.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry<Object,Object> entry = (Map.Entry<Object, Object>)entries.next();
         Object key = entry.getKey();
         Object value = entry.getValue();

         result.put(key, getBalancedValue(value, size) );
      }

      return result;
   }

   /**
    * Get a cookie which was passed in the header of the request.
    * <p>
    * Cookies are often used to provide context information.
    *
    * @param   name        the name of the cookie to retrieve
    *
    * @param   defValue    the default value to return if the requested
    *                      entry is not found
    *
    * @return              the requested cookie's value
    */
   public String getCookie(
      String name, String defValue)
   {
      Cookie cookies[] = m_servletRequest.getCookies();
      
      if (cookies != null)
      {
         for (int i = 0; i < cookies.length; i++)
         {
            Cookie c = cookies[i];
            if (c.getName().equals(name))
            {
               return c.getValue();
            }
         }
      }
      return defValue;
   }

   /**
    * Get a cookie which was passed in the header of the request.
    * <p>
    * Cookies are often used to provide context information.
    *
    * @param   name        the name of the cookie to retrieve
    *
    * @return              the requested cookie's value
    */
   public String getCookie(String name)
   {
      return getCookie(name, null);
   }

   /**
    * Get the XML document sent as input for the request. If an XML
    * document was not sent with the request, <code>null</code> is
    * returned.
    * <p>
    * Modifying the returned XML document will directly modify this object.
    * Any subsequent use of the XML document or the getInputDocument method
    * will see the changed data.
    *
    * @return              the input document (may be <code>null</code>)
    */
   public Document getInputDocument()
   {
      return m_inData;
   }

   /**
    * Set the XML document sent as input for the request. If an XML
    * document was not sent with the request, <code>null</code> is
    * returned.
    * <p>
    * Modifying the returned XML document will directly modify this object.
    * Any subsequent use of the XML document or the getInputDocument method
    * will see the changed data.
    *
    * @param   inData      the input data associated with the request
    *                      (may be <code>null</code>)
    */
   public void setInputDocument(Document inData)
   {
      m_inData = inData;
   }

   /**
    * Get the response object through which the results should be written.
    *
    * @param   inError   Set to <code>true</code> if and only if creating an
    * HTTP error response.  In this case, we won't try to add the Session ID
    * cookie to the response. (We may not have been able to parse it
    * out of the request.  Even if it does exist, the client must already
    * have it, so sending it here would be redundant.)
    *
    * @return    The response object, never <code>null</code>
    */
   PSResponse getResponse(boolean inError)
   {
      if (m_response == null) {
         String keepAlive = null;
         m_response = new PSResponse(keepAlive, getPreferredLocale());

         // store the response date in the HTTP format
         m_response.setGeneralHeader(PSResponse.GHDR_DATE,
            m_httpDateFormatter.format(new java.util.Date()));

         if (!inError)
         {
            String sCookie = null;
            PSUserSession userSession = getUserSession();
            if (userSession != null && !userSession.isAnonymous())
            {
               sCookie = userSession.getId();
            }

            if ((sCookie != null) && (sCookie.length() != 0))
            {
               // Cookie is mapped to /Rhythmyx but cm1 users /cm  cookie needs to work for both
               //String root = PSServer.makeRequestRoot(null);
               String root=null;
               if ((root == null) || (root.length() == 0))
                  root = "/";
               sCookie += ";path=" + root;
               m_response.setCookie("pssessid", sCookie);
            }
         }
      }

      return m_response;
   }

   /**
    * Get the response object through which the results should be written.
    * 
    * @return The response object, never <code>null</code>
    */

   public PSResponse getResponse()
   {
      return getResponse(false);
   }

   /**
    * Get the error handler associated with this object.
    *
    * @return              the error handler to use
    */
   PSErrorHandler getErrorHandler()
   {
      return m_errorHandler;
   }

   /**
    * Set the error handler associated with this object.
    *
    * @param      eh       the error handler to use
    */
   void setErrorHandler(PSErrorHandler eh)
   {
      m_errorHandler = eh;
   }

   /**
    * Get the log handler associated with this object.
    *
    * @return              the log handler to use
    */
   public PSLogHandler getLogHandler()
   {
      /* Associating a log handler with the request was added
       * to fix bug id's TGIS-4BL4CZ and TGIS-4BL44N
       */
      return m_logHandler;
   }

   /**
    * Set the log handler associated with this object.
    *
    * @param      lh       the log handler to use
    */
   public void setLogHandler(PSLogHandler lh)
   {
      /* Associating a log handler with the request was added
       * to fix bug id's TGIS-4BL4CZ and TGIS-4BL44N
       */
      m_logHandler = lh;
   }

   /**
    * Get the application handler servicing this request.
    *
    * @return              the application handler servicing this request
    */
   public PSApplicationHandler getApplicationHandler()
   {
      return m_appHandler;
   }

   /**
    * Set the application handler servicing this request.
    *
    * @param      ah       the application handler servicing this request
    */
   void setApplicationHandler(PSApplicationHandler ah)
   {
      m_appHandler = ah;
   }

   /**
    * Get the statistics object associated with this request.
    *
    * @return     the statistics object
    */
   public PSRequestStatistics getStatistics()
   {
      return m_stats;
   }

   /**
    * Get the requestor's preferred locale by checking the language
    * settings and using the best fit.
    * <p>
    * We need to send the response to the client in their prefered
    * locale. This method can be used to determine the locale of the user.
    *
    * @return        the requestor's preferred locale
    */
   public Locale getPreferredLocale()
   {
      return m_servletRequest.getLocale();
   }

   /**
    * Get the access level assigned to this user for the appliction
    * processing this request.
    *
    * @return           the access level in the current application context
    */
   public int getCurrentApplicationAccessLevel()
   {
      return m_accessLevel;
   }

   /**
    * Saves request parameters by making a clone of the param map and
    * saving it as a saved param map. Saved parameters can then be restored
    * by calling {@link #restoreParams()} method
    * If params were already saved then this method does nothing - this idea
    * is to insure that we save them only once per request, not doing so will
    * potentially allow saving already modified params, which we don't want.
   */
   public void saveParams()
   {
      if (m_params!=null && isSavedParams()==false)
         m_savedParams = (HashMap<String, Object>) m_params.clone();
   }

   /**
    * Restores previously saved request params by {@link #saveParams()} call
   */
   public void restoreParams()
   {
      if (isSavedParams()==true)
         m_params = m_savedParams;
   }

   /**
    * @return <code>true</code> if parameters were saved by a call to
    * {@link #saveParams()} method, <code>false</code> otherwise
    */
   public boolean isSavedParams()
   {
      return m_savedParams != null;
   }

   /**
    * Set the access level assigned to this user for the appliction
    * processing this request.
    *
    * @param   level    the access level in the current application context
    */
   void setCurrentApplicationAccessLevel(int level)
   {
      m_accessLevel = level;
   }

   /**
    * Get a private object associated with this request. This
    * is provided as a storage area for exit handlers, etc. to create
    * context information once. This can then be retrieved throughout the
    * request processing.
    *
    * @param   key      the key under which the object is stored
    *
    * @return           the private object associated with the key
    *
    * @exception  com.percussion.error.PSRuntimeException   if key is null
    */
   public Object getPrivateObject(Object key)
      throws com.percussion.error.PSRuntimeException
   {
      if (key == null)
         throw new com.percussion.error.PSRuntimeException(
            IPSDataErrors.EXECDATA_PRIVATE_OBJ_KEY_NULL);

      // we do a lazy init of this as we may never need it
      if (m_privateObjects == null)
         m_privateObjects = new HashMap<Object, Object>();

      return m_privateObjects.get(key);
   }

   /**
    * Set a private object associated with this request. This
    * is provided as a storage area for exit handlers, etc. to create
    * context information once. This can then be retrieved throughout the
    * request processing.
    *
    * @param   key      the key under which the object is stored. Be sure to
    *                   to specify a unique name -- that is, something other
    *                   exit handlers, etc. using this mechanism will not
    *                   likely use as a name
    *
    * @param   o        the private object associated with the key
    *
    * @exception  com.percussion.error.PSRuntimeException   if key is null
    */
   public void setPrivateObject(Object key, Object o)
      throws com.percussion.error.PSRuntimeException
   {
      if (key == null)
         throw new com.percussion.error.PSRuntimeException(
            IPSDataErrors.EXECDATA_PRIVATE_OBJ_KEY_NULL);

      // we do a lazy init of this as we may never need it
      if (m_privateObjects == null)
         m_privateObjects = new HashMap<Object, Object>();

      m_privateObjects.put(key, o);
   }

   /**
    * Get the content header override string.  If this value is
    *    <code>null</code>, no override is to be made.  If this value
    *    is the empty string, then the content header will be cleared.
    *    Use the requestor and result page as specified in the
    *    application.
    *
    * @return     the full content header to use for this request
    */
   public String getContentHeaderOverride()
   {
      return m_contentHeader;
   }

   /**
    * Set the content header override string.  To disable overriding
    *    the content header, set to <code>null</code>.  To use no
    *    content header, set to the empty string.  All other strings
    *    will be passed as the complete content header as specified.
    *
    * @param   header   The string to use as the content header for this
    *                   request, or <code>null</code> if no override is
    *                   to be used.
    */
   public void setContentHeaderOverride(String header)
   {
      m_contentHeader = header;
   }

   /**
    * Set whether or not this request should show hidden applications
    * when listing applications.
    *
    * @param showHiddenSetting The setting for showing hidden apps,
    * <code>true</code> indicates hidden apps are to be listed,
    * <code>false</code> indicates otherwise.
    */
   public void setShowHiddenApplicationSetting(boolean showHiddenSetting)
   {
      m_showHiddenApplications = showHiddenSetting;
   }

   /**
    * Should this request list hidden applications?
    *
    * @return <code>true</code> to list hidden applications, <code>false</code>
    * otherwise.
    */
   public boolean showHiddenApplications()
   {
      return m_showHiddenApplications;
   }

   /**
    * Returns a security token which must be used to access secure resources
    * in the object store.  This method should not be called before the session
    * has been created and we have attempted to authenticate the user.  Session
    * support does not need to be enabled as a session is created no matter
    * what.
    * 
    * @return The token, never <code>null</code>.
    * 
    * @throws IllegalStateException if a user session does not already exist.
    */
   public PSSecurityToken getSecurityToken()
   {
      // see if we've already gotten a session, or if one does not exists at all
      if (!hasUserSession())
         throw new IllegalStateException("User session does not exist");

      // we know we have one, so get it
      PSUserSession sess = getUserSession();

      // make a token
      PSSecurityToken token = new PSSecurityToken(sess);

      return token;
   }
   
   /**
    * Determine if this request has an associated user session.  The user 
    * session may not be considered an active user session by the
    * {@link PSUserSessionManager}.
    * 
    * @return <code>true</code> if it does, <code>false</code> not.
    */
   public boolean hasUserSession()
   {
      return m_session != null || PSUserSessionManager.doesSessionExist(this);
   }

   /**
    * Attempts to determine the application name specified by this request.
    *
    * @return The name, never <code>null</code>, may be <code>empty</code>.
    * If server is not using case-sensitive urls, then name will be lowercased.
    */
   public String getAppName()
   {
      /* Request root is theoretically one of the following:
       * rxroot/appname
       * rxroot - happens when first coming into the system (uses the default
       *   app)
       * appName - unless the appname was the same as rxroot, this shouldn't
       *    happen
       */
      String reqRoot = m_reqRoot;
      if (!PSServer.isCaseSensitiveURL())
         reqRoot = reqRoot.toLowerCase();

      // will be lowercase if not case sensitive
      String rxRoot = PSServer.getRequestRoot();
      // string leading "/"
      if (rxRoot.startsWith(URL_SEP) && rxRoot.length() > 1)
         rxRoot = rxRoot.substring(1);
      String appName = null;
      StringTokenizer tok = new StringTokenizer(reqRoot, URL_SEP);

      // skip first token if it is the rxroot
      String token = "";
      if (tok.hasMoreTokens())
         token = tok.nextToken();
      if (token.equals(rxRoot) && tok.hasMoreTokens())
      {
         token = tok.nextToken();
      }
      appName = token;

      return appName;
   }

   /**
    * See {@link #getContextForRequest(boolean, boolean)}
    * @param forceLocal
    * @return not null.
    * @see #getContextForRequest(boolean, boolean)
    */
   public static PSRequest getContextForRequest(boolean forceLocal)
   {      
      return getContextForRequest(forceLocal, true);
   }
   
   /**
    * Builds a context that can be used to make internal requests. Normally,
    * request contexts are only available when remote request is initiated.
    * This allows us to make a request w/o having a remote context.
    * <p>The HTTP_HOST CGI variable is set to to 127.0.0.1:[server_port]
    * <p>A single authenticated user entry is added to the session. The user
    * id is {@link PSSecurityProvider#INTERNAL_USER_NAME}
    * and the provider and provider instance are
    * {@link PSSecurityProvider#XML_FLAG_SP_INTERNAL}.
    * <p>To use this, add the internal user name to the app with the
    * necessary access privileges. No other access is needed unless you want
    * external requests to be serviced by the app.
    *
    * @param forceLocal if set to <code>true</code> then set the host as
    * the local IP (127.0.0.1) otherwise we get the serverName.
    * @param useInternalSession <code>true</code> will use the internal 
    * cached sessions. <code>false</code> will create a new session of
    * which you can safely replace security credentials.
    * 
    * @return A valid context that can be used to make an internal request.
    */
   public static PSRequest getContextForRequest(boolean forceLocal, boolean useInternalSession)
   {
      PSRequest req = new PSRequest();

      // set server host and port here
      boolean behindProxyServer = false;
      boolean isSecure = false;
      String host = "127.0.0.1";
      int port = 9992;
      String scheme = "http";
      String publicHostName=host;
      ServletRequest servletReq = req.getServletRequest();
      isSecure = servletReq.isSecure();

      try {
         behindProxyServer = Boolean.parseBoolean(PSServer.getProperty("requestBehindProxy", "false").toString());
      }catch(Exception e){
         behindProxyServer = false;
      }

      //This is an internal request
      if(forceLocal){
         //local request
         port = PSServer.getListenerPort();
         host = "127.0.0.1";
      }else if(behindProxyServer){
         //proxy request
         String proxyPort = PSServer.getProperty("proxyPort",
                 Integer.toString(servletReq.getServerPort()));

         String proxyScheme = PSServer.getProperty("proxyScheme",
                 servletReq.getScheme());

         publicHostName = PSServer.getProperty("publicCmsHostname",PSServer.getHostName());
         port  = Integer.valueOf(proxyPort);
         scheme = proxyScheme;
         host = publicHostName;

      }else{
         //Direct request
         host = PSServer.getServerName(req.m_servletRequest);
         port = PSServer.getListenerPort(req.m_servletRequest);
      }

      if (servletReq instanceof PSServletRequestWrapper)
         servletReq = ((PSServletRequestWrapper)servletReq).getRequest();
      if (servletReq instanceof MockHttpServletRequest)
      {
         MockHttpServletRequest mockReq = (MockHttpServletRequest)servletReq;
         mockReq.setServerName(host);
         mockReq.setServerPort(port);
         mockReq.setScheme(scheme);
      }
      if (useInternalSession)
      {

         String key = ms_internalUserSessionIdMap.get(host);
         PSUserSession sess = null;
         if (key!=null)
         {
            sess =
                    PSUserSessionManager.getUserSession(key);
         }
         if (sess == null)
         {
            // only synchronize when we need to create a new session
            synchronized (PSRequest.class)
            {
               // If we can now get key then another thread beat us and should have set
               // new session id
               String newKey = ms_internalUserSessionIdMap.get(host);
               if (key==null || newKey.equals(key))
               {
                  sess = createInternalUserSession(req, host);
               }
               else
                  sess =
                          PSUserSessionManager.getUserSession(newKey);

            }
         }

         req.m_session = sess;

      }

      return req;
   }

   /**
    * @return the session from the servlet request, initialized in the ctor
    */
   HttpSession getHttpSession()
   {
      return m_httpSession;
   }

   /**
    * Builds a context that can be used to make internal requests. Normally,
    * request contexts are only available when remote request is initiated.
    * This allows us to make a request w/o having a remote context.
    * <p>The HTTP_HOST CGI variable is set to to 127.0.0.1:[server_port]
    * <p>A single authenticated user entry is added to the session. The user
    * id is {@link PSSecurityProvider#INTERNAL_USER_NAME}
    * and the provider and provider instance are
    * {@link PSSecurityProvider#XML_FLAG_SP_INTERNAL}.
    * <p>To use this, add the internal user name to the app with the
    * necessary access privileges. No other access is needed unless you want
    * external requests to be serviced by the app.
    * <p>
    * Use this only when you do not have an existing {@link PSRequest} object.
    * Use {@link #getServerRequest()} if you have a request object and want to 
    * create a new one with internal authenticated user.
    *
    * @return A valid context that can be used to make an internal request.
    */
   public static PSRequest getContextForRequest()
   {
      return getContextForRequest(true);
   }

   /**
    * Release all resources. Deletes all temp files if there is any.
    * NOTE: This should only be called after this object is no longer needed.
    */
   public void release()
   {
      // delete all temp files from "m_params" if any
      deletePurgableTempFile(m_params.values().iterator());

      // delete all temp files from "m_tempFileResources" if any
      if (m_tempFileResources != null)
         deletePurgableTempFile(m_tempFileResources.values().iterator());
      // if session is anonymous, remove it from the cache
      if (m_session != null && m_session.isAnonymous())
      {
         PSUserSessionManager.releaseUserSession(m_session);
      }
   }

   /**
    * Deletes purgable temp files in the given object list if there are any
    * <code>PSPurgableTempFile</code> objects exists.
    *
    * @param objList The object list, assume not <code>null</code>.
    */
   private void deletePurgableTempFile(Iterator<Object> objList)
   {
      while (objList.hasNext())
      {
         Object value = objList.next();
         if (value instanceof PSPurgableTempFile)
            ((PSPurgableTempFile)value).release();
      }
   }
      
   /**
    * This was added to work-around a behavior in the PS[Update,Query]Handlers.
    * They were always cloning the passed request for internal requests.
    * Unfortunately, this meant the caller had no access to the statistics from
    * the processing. To gain access to the stats, a mechanism for preventing
    * the clone had to be added. This is that mechanism. Default value is
    * <code>true</code>.
    */
   public boolean allowsCloning()
   {
      return m_allowsCloning;
   }

   /**
    * See {@link #allowsCloning()} for details.
    *
    * @param allow Supply <code>true</code> to allow this request to be
    *    cloned when making an internal request, <code>false</code> otherwise.
    */
   public void setAllowsCloning(boolean allow)
   {
      m_allowsCloning  = allow;
   }
   
   /**
    * Caches content id status document in this request.
    * @param contentid contentid key, never <code>null</code>
    * or <code>empty</code>.
    * @param doc content item status document to cache,
    * may be <code>null</code>.
    */
   public void setContentItemStatus(String contentid, Document doc)
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid may not be null");
         
      if (contentid.trim().length() < 1)
         throw new IllegalArgumentException("contentid may not be empty");
      
      if (m_contentItemStatusCache == null)
      {
         m_contentItemStatusCache = new HashMap<String, Document>();
      }
      
      m_contentItemStatusCache.put(contentid, doc);
   }
   
   /**
    * Returns a cached content item status document,
    * may be <code>null</code>.
    * @param contentid contentid key, never <code>null</code>
    * or <code>empty</code>. 
    * @return cached content item status document, 
    * may be <code>null</code>.
    */
   public Document getContentItemStatus(String contentid)
   {
      if (contentid == null)
         throw new IllegalArgumentException("contentid may not be null");
         
      if (contentid.trim().length() < 1)
         throw new IllegalArgumentException("contentid may not be empty");
      
      if (m_contentItemStatusCache==null)
         return null;
      
      return  (Document)m_contentItemStatusCache.get(contentid);
   }
   
   /**
    * Removes all cached content item status documents.
    */
   public void clearContentItemStatusCache()
   {
      
      if (m_contentItemStatusCache!=null)
         m_contentItemStatusCache.clear();
   }  

   /**
    * Gets the request timer. The caller may pause or stop the timer. The 
    * expectation is that this timer will be started by the request handler
    * and paused (ideally) by server code that sends requests externally to
    * Rhythmyx. Initially this will just be paused for stylesheet 
    * transformations. 
    * @return the {@link PSStopwatch} associated with this request.
    */
   public PSStopwatch getRequestTimer()
   {
      return m_requestTimer;
   }

   /**
    * Creates a user session used to make internal requests. A single
    * authenticated user entry is added to the session. The user id is
    * {@link PSSecurityProvider#INTERNAL_USER_NAME} and the provider and
    * provider instance are {@link PSSecurityProvider#XML_FLAG_SP_INTERNAL}.
    * The session id for the created user session is cached so it can be
    * reused for other internal request.
    *
    * @param req the request to create the internal user session for, assumed
    *    not <code>null</code>.
    */
   private static PSUserSession createInternalUserSession(PSRequest req, String host)
   {
      PSUserSession sess = req.getUserSession();

      PSUserEntry entry = createInternalUserEntry();
      sess.addAuthenticatedUserEntry(entry);
      PSSecurityFilter.updateHttpSession(req.getServletRequest(), sess);

      ms_log.debug("Creating interal user session for host :"+host);
      
      ms_internalUserSessionIdMap.put(host, sess.getId());
      return sess;
   }

   /**
    * Creates a user entry for the internal server user, adding role entries for
    * any roles the user is a member of.
    * 
    * @return The entry, never <code>null</code>.
    */
   private static PSUserEntry createInternalUserEntry()
   {
      // get roles for user
      PSRoleEntry[] roleEntries = null;
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      try
      {
         List<PSRoleEntry> roleList = new ArrayList<PSRoleEntry>();
         Set<String> roles = roleMgr.getUserRoles(
            PSTypedPrincipal.createSubject(
               PSSecurityProvider.INTERNAL_USER_NAME));
         for (String role : roles)
         {
            roleList.add(new PSRoleEntry(role, 0));
         }
         
         roleEntries = roleList.toArray(new PSRoleEntry[roleList.size()]);
      }
      catch (PSSecurityCatalogException e)
      {
         ms_log.error("Failed to load roles for internal server user", e);
      }
      
      PSUserEntry entry = new PSUserEntry(
         PSSecurityProvider.INTERNAL_USER_NAME, 0, null, roleEntries, null, 
         PSUserEntry.createSignature(PSSecurityProvider.INTERNAL_USER_NAME, 
            ""));
      
      return entry;
   }
   
   /**
    * Get the subject who made the original request.
    * 
    * @return the subject who made the original request, never 
    *    <code>null</code>.
    */
   public PSSubject getOriginalSubject()
   {
      PSRequest originalRequest = getOriginalRequest();
      PSUserEntry[] users = originalRequest.getSecurityToken().
         getUserSession().getAuthenticatedUserEntries();
         
      // there is always only one entry
      PSUserEntry user = users[0];
      
      PSAttributeList attributes = new PSAttributeList(user.getAttributes());
      PSSubject subject = new PSGlobalSubject(user.getName(), 
         PSSubject.SUBJECT_TYPE_USER, attributes);
      
      return subject;
   }

   /**
    * Get the original request.
    * 
    * @return the original request, never <code>null</code>.
    */
   public PSRequest getOriginalRequest()
   {
      return isOriginalRequest() ? this : 
         m_cloneParentRequest.getOriginalRequest();
   }

   /**
    * Is this the original request? Decided by checking if it has any clone
    * parent in which case it is not the original.
    * 
    * @return <code>true</code> if this is the original request, 
    *    <code>false</code> otherwise.
    */
   public boolean isOriginalRequest()
   {
      return m_cloneParentRequest == null;
   }

   /**
    * Sets the originating relationship. By definition, originating relationship
    * is set only once in the requests life time no matter how many clones are
    * created during execution of the original request. This method makes sure
    * that this behavior is achieved.
    * @param relationship originating relationship, must not be <code>null</code>.
    */
   public void setOriginatingRelationship(PSRelationship relationship)
   {
      if(relationship == null)
         throw new IllegalArgumentException(
         "relationship must not be null in setOriginatingRelationship()");

      if(isOriginalRequest())
      {
         if(m_originatingRelationship == null)
            m_originatingRelationship = relationship;
         //else it is already set previously - do not reset
         return;
      }
      m_cloneParentRequest.setOriginatingRelationship(relationship);
   }

   /**
    * Get originating relationship if there is one.
    * @return
    */
   public PSRelationship getOriginatingRelationship()
   {
      return (isOriginalRequest())?
         m_originatingRelationship:
         m_cloneParentRequest.getOriginatingRelationship();
   }
   
   /**
    * Get a server verison {@link PSRequest} object from this request. This new 
    * request uses the internal user as authenticated entry and can be used to 
    * perform operations on behalf of server. It is built only once for the life 
    * time of the user's request. The following rules are followed:
    * <ol>
    * <li>All request's objects are deep or shallow cloned as per 
    * {@link #cloneRequest()</li>
    * <li>All existing authenticated entry list is emptied and internal user 
    * entry is added</li>
    * <li>new session for internal is created based on the user session. See
    * {@link PSUserSession#cloneSessionForRequest(PSRequest)} for more details.
    * </li>
    * <li>Reference to the originating is also copied to the new request</li>
    * </ol>
    * 
    * @return server request object, never <code>null</code>.
    */
   public PSRequest getServerRequest()
   {
      //It was already built, use it.
      if (m_serverRequest != null)
         return m_serverRequest;

      if(PSSecurityProvider.INTERNAL_USER_NAME.equals(
         getUserSession().getRealAuthenticatedUserEntry()))
      {
         //The current request is a server request
         m_serverRequest = this;
      }
      else
      {
         //Create new request with internal user as authenticated user entry.
         PSUserSession newSession = m_session.cloneSessionForRequest(this);
         newSession.addAuthenticatedUserEntry(createInternalUserEntry());
         
         m_serverRequest = cloneRequest();
         m_serverRequest.m_session = newSession;
         m_serverRequest.m_originatingRelationship = m_originatingRelationship;
         PSSecurityFilter.updateHttpSession(m_serverRequest.getServletRequest(), 
            newSession);
      }
      return m_serverRequest;
   }
   
   
   /**
    * @return Returns the servletRequest.
    */
   public HttpServletRequest getServletRequest()
   {
      return m_servletRequest;
   }
   
   /**
    * Set the current servlet request
    * 
    * @param req The request, may not be <code>null</code>.
    */
   public void setServletRequest(HttpServletRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      m_servletRequest = req;
   } 
   
   /**
    * @return Returns the servletResponse.
    */
   public HttpServletResponse getServletResponse()
   {
      return m_servletResponse;
   }

   /**
    * Get the protocol that was used by this request.
    *
    * @return the protocol as a string; either "http" or "https"
    */
   public String getProtocol()
   {
      if (getServletRequest().isSecure())
         return PSUserSession.PROTOCOL_HTTPS;
      else
         return PSUserSession.PROTOCOL_HTTP;
   }

   /**
    * The storage to cache the session ids for the internal user session.
    * This is a Map so it can hold the session id of both the local IP and
    * the external server name (if needed).
    * The key is the host name as a String and the value is the session id
    * as a String.
    * This is never <code>null</code>, but may be empty.
    */
   private static Map<String,String> ms_internalUserSessionIdMap = new ConcurrentHashMap<String,String>(8, 0.9f, 1);



   /**
    * This ctor creates an empty request context that can be used for internal
    * requests independent of a request coming into the server. After getting
    * the request, you need to set up the security by adding validated
    * credentials. See {@link #getContextForRequest}.
    */
   protected PSRequest()
   {
      this( null, null, PSServer.getErrorHandler(), PSServer.getLogHandler());
   }


   /**
    * Takes the Object provided and creates a List with the specified
    * number of entries, setting the Object into each entry.
    *
    * @param value The value to expand into a list. May be <code>null</code>.
    * @param size The number of entries to create in the list.
    *
    * @return The list of entries.
    */
   private List<Object> createList(Object value, int size)
   {
      List<Object> newEntry = new ArrayList<Object>(size);
      for (int i = 0; i < size; i++)
         newEntry.add(value);

      return newEntry;
   }
   
   /**
    * Parses the request body from the servlet request supplied during 
    * construction if the request method was a post, otherwise a noop.
    * 
    * @throws PSRequestParsingException If there is an error parsing the request
    * body from the supplied stream.
    * @throws IOException If there is an error reading from the request input 
    * stream.
    */
   public void parseBody() throws PSRequestParsingException, IOException
   {
      InputStream in = m_servletRequest.getInputStream(); 
      String contentType = m_servletRequest.getContentType(); 
      int contentLength = m_servletRequest.getContentLength();
      
      if (contentLength <= 0)
         return;

      String mediaType = null;
      String charSet = null;

      HashMap<String, Object> contentParams = new HashMap<String, Object>();
      mediaType = PSBaseHttpUtils.parseContentType(contentType,
         contentParams);
      charSet = (String)contentParams.get("charset");
      if (StringUtils.isNotBlank(charSet))
      {
         // If the charSet is presented with quotes, remove them (Dreamweaver)
         charSet = PSStringUtils.stripQuotes(charSet);
      }

      // If the sender doesn't specify charset, check the request
      // Note:  This previously defaulted to ISO8859-1
      if ((charSet == null) || (charSet.length() == 0))
      {
         charSet = getCharacterSet();
      }

      /* verify this is a content type we understand */
      PSContentParser parser =
               (PSContentParser)ms_ContentParsers.get(mediaType);
      if (parser == null) {
         Object[] args = { contentType };
         throw new PSRequestParsingException(
                        IPSServerErrors.INVALID_CONTENT_TYPE, args);
      }

      PSInputStreamReader reader = new PSInputStreamReader(in, false,
         PSContentParser.MIN_PUSHBACK_BUF_SIZE );      
      parser.parse(this, contentType, charSet, reader, contentLength);
   
   }
   
   /**
    * Determine if the user is a member of the specified role.  Checks against
    * the list of roles supplied during JAAS authentication.
    * 
    * @param roleName The role name to check, may not be <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if the user is a member of the role, 
    * <code>false</code> if not.
    */
   public boolean isUserInRole(String roleName)
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");
      
      return m_servletRequest.isUserInRole(roleName);
   }
   
   /**
    * Checks the servlet request attributes to see if the the 
    * "javax.servlet.include.request_uri" request attribute is not 
    * <code>null</code>, and if not, creates a new request using the supplied
    * request and response, that references the same session, error handler, and
    * log handler as this request.
    * 
    * @param req The request to use, never <code>null</code>. 
    * @param resp The response to use, never <code>null</code>.
    *  
    * @return The new request, or <code>null</code> if the http servlet request
    * referenced by this request does not indicate an include.
    */
   public PSRequest getRequestForIncludeURI(HttpServletRequest req, 
      HttpServletResponse resp)
   {
      PSRequest newReq = null;

      String includePath = (String)req.getAttribute(
         "javax.servlet.include.request_uri");

      if (StringUtils.isNotBlank(includePath))
      {
         newReq = new PSRequest(req, resp, m_errorHandler, m_logHandler);
         newReq.setRequestFileURL(includePath);
         newReq.m_session = m_session;
      }

      
      return newReq;
   }
   
   /**
    * Get the request represented by the supplied request context.
    * 
    * @param ctx The context to get the request from, may not be 
    * <code>null</code>.
    * 
    * @return The request, never <code>null</code>.
    */
   public static PSRequest getRequest(IPSRequestContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      return ((PSRequestContext)ctx).getRequest();
   }   
   
   /**
    * This map is used to cache content item status doc in the
    * user session. It is lazily instantiated on the first set,
    * never <code>null</code> after that.  
    */
   protected HashMap<String, Document>  m_contentItemStatusCache;

   /**
    * The content header override string.  This will allow user exits
    *    to set the content header regardless of the settings in the
    *    requestor and the result page.  A value of <code>null</code>
    *    (the default) indicates that there will be no override, and
    *    a value of empty string indicates that the content header will
    *    not be set.
    */
   private String m_contentHeader = null;

   /**
    * The request page type is unknown.
    */
   public static final int    PAGE_TYPE_UNKNOWN = 0x00000000;

   /**
    * The desired format for the request page is XML.
    */
   public static final int    PAGE_TYPE_XML     = 0x00000001;

   /**
    * The desired format for the request page is HTML.
    */
   public static final int    PAGE_TYPE_HTML    = 0x00000002;

   /**
    * The desired format for the request page is text.
    */
   public static final int    PAGE_TYPE_TEXT    = 0x00000004;


   /**
    * A HashMap to store temp file resources, so that PurgeableTempFile
    * information can be retrieved such as character set encoding.  Can
    * be <code>null</code>. Never empty.  Initialized lazily when a file
    * resource addition is first attempted.
    */
   private HashMap<String, Object> m_tempFileResources = null;

   protected final com.percussion.util.PSDateFormatHttp
      m_httpDateFormatter = new com.percussion.util.PSDateFormatHttp();

   protected static final String CGI_CONNECTION = "HTTP_CONNECTION";

   protected PSUserSession          m_session;
   protected String                 m_reqRoot;
   protected String                 m_reqPage;
   protected String                 m_reqExt;
   protected int                    m_reqPageType;

   /**
    * The map of parameter names and values that were passed in with the request.
    * Initialized in constructor and never <code>null</code> after that. May be
    * empty.
    */
   protected HashMap<String, Object>                m_params;

    /**
    * The map of parameter names and values that were saved prior to the
    * execution of an exit that modifies params. This allows to restore all
    * the original params in case if there is some failure and we want to
    * rollback to the original parameters.
    * Is <code>null</code> by default, initilized with cloned m_params when
    * the {@link #saveParams()} method is called.
    */
   private HashMap<String, Object>                m_savedParams;

   /**
    * The character set for the current request, may be <code>null</code>,
    * never empty.  Initialized/modified with the <code>setCharacterSet</code>
    * call.
    */
   private String                   m_encoding = null;

   /**
    * The file character set for the current request, may be <code>null</code>,
    * never empty.  Initialized/modified with the
    * <code>setFileCharacterSet</code> call.
    */
   private String                   m_fileEncoding = null;

   protected String                 m_reqHookURL;
   protected Document               m_inData;
   protected PSResponse             m_response;
   protected PSErrorHandler         m_errorHandler;
   protected PSLogHandler           m_logHandler;
   protected PSApplicationHandler   m_appHandler;
   
   protected HttpServletRequest     m_servletRequest;
   protected HttpServletResponse    m_servletResponse;
   protected HttpSession            m_httpSession;
   
   /*
    * When a request is being made for an application list, should hidden
    * applications be shown?  <code>true</code> the default is
    * <code>false</code>.
    */
   protected boolean                m_showHiddenApplications = false;

   protected PSRequestStatistics    m_stats;
   // the access level to the application processing this request
   protected int                    m_accessLevel = 0;

   /**
    * Constant for / separator used in urls
    */
   private static final String URL_SEP = "/";

   private   HashMap<Object, Object>                m_privateObjects = null;

   /**
    * A map of all clones made during this request, the key is the original id
    * (as <code>Integer</code> object) while the value is the clone locator (as
    * <code>PSLocator</code> object).
    * Initialized during the first call to <code>addClone</code>. Might be
    * <code>null</code> before.
    */
   private Map<Serializable, Serializable> m_clones = null;

   /**
    * A list of all relationships that have been created during this request.
    * Initialized during the first call to <code>addRelationship</code>. Might
    * be <code>null</code> before.
    */
   private List<Serializable> m_relationships = null;

   /**
    * See {@link #allowsCloning()} for details. Set by {@link
    * setAllowsCloning(boolean)}.
    */
   private boolean m_allowsCloning = true;

   /**
    * Refernce to the parent request when this request is cloned to create a new
    * one. This will be <code>null</code> if this is the original request and
    * non-<code>null</code> for all cloned requests. Set in the
    * {@link #cloneRequest()} method.
    */
   private PSRequest m_cloneParentRequest = null;

   /**
    * Reference to the originating relationship. Will be <code>null</code> for
    * all cloned requests. Will be <code>null</code> for even for the clone root
    * request till it is set using
    * {@link #setOriginatingRelationship(PSRelationship)}.
    */
   private PSRelationship m_originatingRelationship = null;

   /**
    * Reference to the server or local request. Will be <code>null</code> 
    * initially and will be constructed when the method 
    * {@link #getServerRequest()} is called for the first time. It will be
    * reused during life time of the request.
    */
   private PSRequest m_serverRequest = null;
   
   /**
    * This timer allows monitoring of a requests elapsed time. The timer
    * should be manipulated through the exposed getter. The timer is 
    * initialized in the ctor and never <code>null</code> afterward.
    */
   private PSStopwatch m_requestTimer = null;

   /**
    * The html parameter that is looked for by the request parser in
    * the initial status line to drive the character interpretation of the
    * associated request.
    */
   public static final String REQ_CHARSET_PARAM = "psxcharacterset";

   /**
    * String constant to indicate the name of the request private object which 
    * is an empty map created upon creation of the request for anybody's use. 
    * This map is created specifically in the process of detecting recursive 
    * assembly.
    */
   public static final String ASSEMBLY_RECURSION_MAP_KEY = 
      "sys_assemblyRecursionMapKey";

   /**
    * Refer to {@link IPSHtmlParameters#REQ_XML_DOC_FLAG}.
    */
   public static final String REQ_XML_DOC_FLAG = IPSHtmlParameters.REQ_XML_DOC_FLAG;

   /**
    * The constant to define that the xml document uploaded should be validated.
    */
   public static final String XML_DOC_VALIDATE = "useValidating";

   /**
    * The constant to define that the xml document uploaded should not be
    * validated.
    */
   public static final String XML_DOC_NONVALIDATE = "useNonValidating";

   /**
    * Refer to {@link IPSHtmlParameters#XML_DOC_AS_TEXT}.
    */
   public static final String XML_DOC_AS_TEXT = IPSHtmlParameters.XML_DOC_AS_TEXT;


   public static final String REQ_URL_PARAM = "psrequrl";   

   /**
    * Constant for the "GET" HTTP request method.
    */
   private static final String REQ_METHOD_GET = "GET";

   /**
    * Constant for the "POST" HTTP request method.
    */
   private static final String REQ_METHOD_POST = "POST";
   
   /**
    * Map of content parsers by type.  Key is the content type as a 
    * <code>String</code>, value is an instance of the appropriate sub-class of 
    * {@link PSContentParser} to use, initialized by a static intializer, never
    * <code>null</code> or modified after that.
    */
   private static Map<String, PSContentParser> ms_ContentParsers;
   
   static 
   {
      ms_ContentParsers = new HashMap<String, PSContentParser>();

      /* with JDK 1.2, we can ask the content package what classes it
       * contains. For now, we'll just hard-code them.
       */
      addParser((PSContentParser)new PSXmlContentParser());
      addParser((PSContentParser)new PSFormContentParser());
   }
   
   /**
    * Adds a parser to the {@link #ms_ContentParsers} map for each of the 
    * content types it supports.  Intended only to be called from the static
    * initializer.
    * 
    * @param parser The parser to add, assumed not <code>null</code>.
    */
   private static void addParser(PSContentParser parser) {
      String[] types = parser.getSupportedContentTypes();
      for (int i = 0; i < types.length; i++) {
         ms_ContentParsers.put(types[i], parser);
      }
   }
}

