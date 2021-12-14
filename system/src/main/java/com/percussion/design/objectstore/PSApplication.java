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

package com.percussion.design.objectstore;

import com.percussion.design.objectstore.legacy.IPSComponentConverter;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSConsole;
import com.percussion.util.PSCollection;
import com.percussion.util.PSDocVersionConverter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Level;
import org.apache.xerces.dom.ParentNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The PSApplication class is used to manipulate an E2 application's
 * definition. Use the PSObjectStore class to load a PSApplication object
 * from an E2 server (getApplication) or to build a new PSApplication object
 * using the default server settings (createApplication). The returned object
 * can be read and/or written (assuming you have the apporpriate access).
 * Once you're ready to send the local changes back to the server, call the
 * PSObjectStore's saveApplication method. See the PSObjectStore class for
 * more details.
 *
 * @see         PSObjectStore
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
@SuppressWarnings(value={"unchecked"})
public class PSApplication implements IPSDocument
{
   /**
    * Specialized wrapper class that takes a <code>PSDataSet</code> and
    * uses only the request name and parameters to determine equivalence.
    * Used in validation to determine if two datasets in the application
    * are <q>equivalent</q>.
    */
   private class RequestKey
   {
      /**
       * The dataset that is being wrapped in this key,
       * never <code>null</code>. Thie is assigned during construction
       * and never updated.
       */
      protected PSDataSet mi_dataset;

      /**
       * Constructor
       * @param set passed <code>PSDataSet</code>, must not be
       * <code>null</code>
       */
      public RequestKey(PSDataSet set)
      {
         if (set == null)
         {
            throw new
               IllegalArgumentException("Passed dataset must not be null");
         }

         mi_dataset = set;
      }

      /**
       * Equals for dataset means that both the requestor and the selection
       * criteria for the requestor are equivalent. Criteria are equivalent
       * if each criteria exists in both sets.
       *
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object compareTo)
      {
         if (!(compareTo instanceof RequestKey))
         {
            return false;
         }

         if (compareTo == null)
         {
            return false;
         }

         RequestKey compareKey = (RequestKey) compareTo;
         PSRequestor request = getDataset().getRequestor();
         PSRequestor compareReq = compareKey.getDataset().getRequestor();

         if (request == null || compareReq == null)
         {
            /* If either is null then there are no distinguishing
               characteristics, and there will be an indeterminacy
               regarding which will have precedence */
            return false;
         }

         String reqPage = request.getRequestPage();
         String compReqPage = compareReq.getRequestPage();

         if (reqPage == null)
         {
            return compReqPage == null;
         }

         if (compReqPage == null)
         {
            // If they were both null we would have returned above
            return false;
         }

         /* Equals in this case means that the request matches and each
            <code>PSConditional</code> element in the request matches
            an equivalent <code>PSConditional</code> element in the other
            request */
         Set sourceSet = new HashSet(request.getSelectionCriteria());
         Set compareSet = new HashSet(compareReq.getSelectionCriteria());

         /* If either set is empty, that is the equivalent of the sets
          * being equals. This is because having no selection crteria
          * means matching under all circumstances.
          */
         if (sourceSet.isEmpty() || compareSet.isEmpty())
            return true;
         else
            return sourceSet.equals(compareSet);
      }

      /**
       * Note that the hash code for this object does <b>not</b> include
       * the selection criteria. This is because a request with no selection
       * criteria effectively matches a request with any selection criteria.
       *
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         PSRequestor req = mi_dataset.getRequestor();

         if (req == null)
         {
            return 0;
         }

         String page = req.getRequestPage();

         return page != null ? page.hashCode() : 0;
      }

      /**
       * @return stored dataset
       */
      public PSDataSet getDataset()
      {
         return mi_dataset;
      }

   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @param      sourceDoc      the XML document to construct this
    *                              object from
    *
    * @exception   PSUnknownDocTypeException
    *                              if the XML document is not of the
    *                              appropriate type
    *
    * @exception   PSUnknownNodeTypeException
    *                              if an XML element node is not of the
    *                              appropriate type
    */
   public PSApplication(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceDoc);
   }

   /**
    * Construct an empty application.
    */
   PSApplication()
   {
      super();
   }

   /**
    * Construct an application with the specified name.
    *
    * @param name   the application name
    *
    * @see       #setName
    */
   protected PSApplication(java.lang.String name)
   {
      this();
      setName(name);
      setRequestRoot(name);
   }

   /**
    * Is this a hidden application?
    *
    * @return  <code>true</code> if it is, <code>false</code> indicates
    *          a normal application.
    */
   public boolean isHidden()
   {
      return m_hidden;
   }

   /**
    * Is this an empty application, i.e. does not have at least one data set?
    *
    * @return  <code>true</code> if it is, <code>false</code> indicates
    *          application with at least one dataset.
    */
   public boolean isEmpty()
   {
      return getDataSets().isEmpty();
   }

   /**
    * Set whether this application is hidden or not.
    *
    * @param hidden  <code>true</code> if it is to be hidden,
    *                <code>false</code> indicates a normal application.
    */
   public void setHidden(boolean hidden)
   {
      m_hidden = hidden;
   }

   /**
    * Get the id assigned to this application.
    *
    * @return        the application id
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Set the id for this application.
    *
    * @param newId the application id
    */
   public void setId(int newId)
   {
      m_id = newId;
   }

   /**
    * Get the name of the application.
    *
    * @return      the name of the application
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * This is used by the server to determine app start order. The higher the
    * value, the earlier the app will be started. The range is from
    * Integer.MIN_VALUE to Integer.MAX_VALUE. The default is 0. This allows
    * apps to be started either before or after the main group of apps.
    * It is stored as an optional attribute of the ms_NodeType element.
    *
    * @return The priority for this app. Most apps will return the default
    *    value.
    */
   public int getStartPriority()
   {
      return m_startPriority;
   }

   /**
    * See {@link #getStartPriority()} for a description. This method is only
    * useful when modifying the design.
    *
    * @param priority Any value is allowed. The larger the value, the higher
    *    the priority. The default is 0.
    */
   public void setStartPriority(int priority)
   {
      m_startPriority = priority;
   }

   /**
    * Set the name of the application.
    *
    * @param name   the new name of the application. This must be a unique
    *             name on the server. If it is non-unique, an exception
    *             will be thrown when the application is saved on the Rhythmyx
    *             server. This is limited to 50 characters.
    */
   public void setName(String name)
   {
      name = name.trim();
      IllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
      setModified(true);
   }

   private static IllegalArgumentException validateName(String name)
   {
      int length = name.length();
      if (length < 1)
         return new IllegalArgumentException("app name is empty");
      else if (length > APP_MAX_NAME_LEN) {
         return new IllegalArgumentException("app name is too big: " +
               APP_MAX_NAME_LEN + " " + length);
      }

      return null;
   }

   /**
    * Get the version of this application.
    *
    * @return   the version string
    */
   public String getVersion()
   {
      return m_version;
   }

   /**
    * Get the description of the application.
    *
    * @return      the description of the application
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Set the description of the application.
    *
    * @param description the new description of the application. This is
    *                     limited to 255 characters.
    */
   public void setDescription(String description)
   {
      IllegalArgumentException ex = validateDescription(description);
      if (ex != null)
         throw ex;

      m_description = description;
      setModified(true);
   }

   /**
    * This method used to restrict the length of description to
    * APP_MAX_DESC_LEN (255) characters. This restriction has been
    * removed so that the description can be of any length.
    * @param description the description of the Application.
    * May be <code>null</code> or empty.
    * @return Always returns <code>null</code>
    */
   private static IllegalArgumentException validateDescription(
      @SuppressWarnings("unused") String description)
   {
      return null;
   }

   /**
    * Determine whether this application is enabled. An enabled application is
    * able to run on the server. Use {@link #isActive() isActive} method to
    * determine an application is currently running.
    *
    * @return   <code>true</code> if the application is enabled,
    *          <code>false</code> otherwise
    */
   public boolean isEnabled()
   {
      return m_enabled;
   }

   /**
    * Enable or disable this application. If the application is enabled, the
    * server will begin executing it upon save. When the server starts up, it
    * also begins executing enabled applications. When the server shuts down,
    * it ends execution of all running applications.
    *
    * @param enabled  <code>true</code> to enable this application;
    *               <code>false</code> to disable it
    */
   public void setEnabled(boolean enabled)
   {
      m_enabled = enabled;
      setModified(true);
   }

   /**
    * Determine whether this application is running on the server.
    *
    * @return           <code>true</code> if the application is running;
    *               <code>false</code> otherwise
    */
   public boolean isActive()
   {
      return m_active;
   }

   /**
    * Get the application's request root. This is combined with the E2 server
    * request root to determine if an incoming request should be serviced.
    * For instance, if the server root is <code>/E2</code> and the
    * application root is <code>/MyApp</code>, only URLs whose path begins
    * with <code>/E2/MyApp</code> will be considered for processing. This
    * helps alleviate strain from the web server as E2 can more quickly
    * determine what is an E2 request rather than a standard web server
    * request.
    *
    * @return      the application's request root
    */
   public String getRequestRoot()
   {
      return m_requestRoot;
   }

   /**
    * Set the application's request root. This is combined with the E2 server
    * request root to determine if an incoming request should be serviced.
    * For instance, if the server root is <code>/E2</code> and the
    * application root is <code>/MyApp</code>, only URLs whose path begins
    * with <code>/E2/MyApp</code> will be considered for processing. This
    * helps alleviate strain from the web server as E2 can more quickly
    * determine what is an E2 request rather than a standard web server
    * request.
    *
    * @param  requestRoot     the new application request root.
    *                           This is limited to 50 characters.
    */
   public void setRequestRoot(String requestRoot)
   {
      requestRoot = requestRoot.trim();
      IllegalArgumentException ex = validateRequestRoot(requestRoot);
      if (ex != null)
         throw ex;

      m_requestRoot = requestRoot;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestRoot(
      String requestRoot)
   {
      int length = requestRoot.length();
      if (length > APP_MAX_REQ_ROOT_LEN)
      {
         return new IllegalArgumentException("app root too big: " +
               length + " " + APP_MAX_REQ_ROOT_LEN);
      }
      return null;
   }

   /**
    * Get the request page which will be returned when a request is made to the
    * server for this application without explicitly specifying a request page.
    * For instance, if Rhythmyx is the server's request root, MyApp is the
    * application  and welcome.html is specified as the default request page, a
    * request for
    * http://myserver/Rhythmyx/MyApp will be routed to
    * http://myserver/Rhythmyx/MyApp/welcome.html
    *
    * @return  the name of the default request page or
    *          null if there is no default
    */
   public String getDefaultRequestPage()
   {
      return m_defaultRequestPage;
   }

   /**
    * Set the request page which will be returned when a request is made to the
    * server for this application without explicitly specifying a request page.
    * For instance, if Rhythmyx is the server's request root, MyApp is the
    * application and welcome.html is specified as the default request page, a
    * request for
    * http://myserver/Rhythmyx/MyApp will be routed to
    * http://myserver/Rhythmyx/MyApp/welcome.html
    *
    * @param   requestPage  the name of the default request page
    */
   public void setDefaultRequestPage(String requestPage)
   {
      if ((requestPage != null) && (requestPage.length() > 0))
         requestPage = requestPage.trim();

      m_defaultRequestPage = requestPage;
      setModified(true);
   }

   /**
    * Get the application's access control list (ACL). This contains the
    * names of users, groups and roles and the type of access they have for
    * this application.
    *
    * @return      the application's ACL
    */
   public PSAcl getAcl()
   {
      return m_acl;
   }

   /**
    * Overwrite the application's ACL with the specified ACL. If you only
    * want to modify a limited number of entries, add an entry, etc. use
    * getAcl to get the existing ACL and modify the returned object directly.
    * <p>
    * The PSAcl object supplied to this method will be stored with the
    * PSApplication object. Any subsequent changes made to the object by the
    * caller will also effect the application.
    *
    * @param acl     the new ACL for the application
    * @see           PSAcl
    */
   public void setAcl(PSAcl acl)
   {
      IllegalArgumentException ex = validateAcl(acl);
      if (ex != null)
         throw ex;

      m_acl = acl;
      setModified(true);
   }

   private static IllegalArgumentException validateAcl(PSAcl acl)
   {
      if (null == acl)
         return new IllegalArgumentException("app acl is null");

      PSCollection entries = acl.getEntries();
      int size = entries.size();
      if (0 == size)
         return new IllegalArgumentException("app acl is empty");

      int fullDesignAccess =   PSAclEntry.AACE_DESIGN_DELETE |
         PSAclEntry.AACE_DESIGN_READ |
         PSAclEntry.AACE_DESIGN_UPDATE |
         PSAclEntry.AACE_DESIGN_MODIFY_ACL;

      ConcurrentHashMap names = new ConcurrentHashMap();
      boolean hasFullDesign = false;
      PSAclEntry ace;
      String key;
      int level;
      for (int i = 0; i < size; i++) {
         ace = (PSAclEntry)entries.get(i);

         key = ace.getName();

         if (names.put(key, ace) != null)   /* we've seen this already */
            return new IllegalArgumentException("acl entry list is duplicate: " +
                     ace.getName());

         level = ace.getAccessLevel();
         if ((level & fullDesignAccess) == fullDesignAccess)
            hasFullDesign = true;
      }

      if (!hasFullDesign)
         return new IllegalArgumentException("app acl no manager");

      return null;
   }

   /**
    * Get the application's data encryption settings. Through this object,
    * E2 can force users to make requests through SSL. It can even be used to
    * enforce the key strength is appropriate for the given application. This
    * allows the application's data to be sent over secure channels. Incoming
    * requests from users, however, can still be sent in the clear. For this
    * reason, care must be taken when designing web pages so that forms
    * containing sensitive data, including user ids and passwords, are
    * submitted using HTTPS, not HTTP.
    *
    * @return        the application's data encrytion settings or
    *            <code>null</code> if one has not been previously defined
    */
   public PSDataEncryptor getDataEncryptor()
   {
      return m_dataEncryptor;
   }

   /**
    * Overwrite the application's data encryption object with the specified
    * data encryption object. If you only want to modify some data
    * encryption settings, use getDataEncryptor to get the existing object
    * and modify the returned object directly.
    * <p>
    * The PSDataEncryptor object supplied to this method will be stored with
    * the PSApplication object. Any subsequent changes made to the object by
    * the caller will also effect the application.
    *
    * @param encryptor     the new data encryptor for the application or
    *                  <code>null</code> to disable this functionality
    *
    * @see                 PSDataEncryptor
    */
   public void setDataEncryptor(PSDataEncryptor encryptor)
   {
      m_dataEncryptor = encryptor;
      setModified(true);
   }

   /**
    * Get the maximum number of threads this application is permitted to
    * consume. The server maintains a pool of threads, which can grow as
    * activity increases. The number of threads can be limited to avoid the
    * resource problems that arise from excessive thread use.
    *
    * @return        the maximum number of threads this application is permitted
    *            to use
    */
   public int getMaxThreads()
   {
      return m_maxThreads;
   }

   /**
    * Set the maximum number of threads this application is permitted to
    * consume. The server maintains a pool of threads, which can grow as
    * activity increases. The number of threads can be limited to avoid the
    * resource problems that arise from excessive thread use.
    * <p>
    * The server can also specify the maximum number of threads any
    * application is permitted to use. The application cannot exceed the
    * server defined limit.
    *
    * @param max     the maximum number of threads this application is permitted
    *            to use
    */
   public void setMaxThreads(int max)
   {
      m_maxThreads = max;
      setModified(true);
   }

   /**
    * Get the maximum amount of time to spend servicing a request.
    *
    * @return        the maximum amount of time to spend servicing a request,
    *          in seconds
    */
   public int getMaxRequestTime()
   {
      return m_maxRequestTime;
   }

   /**
    * Set the maximum amount of time to spend servicing a request.
    * <p>
    * The server can also specify the amount of time any application is
    * permitted to spend servicing a request. The application cannot exceed
    * the server defined limit.
    *
    * @param max     the maximum amount of time to spend servicing a request,
    *            in seconds
    */
   public void setMaxRequestTime(int max)
   {
      m_maxRequestTime = max;
      setModified(true);
   }

   /**
    * Get the maximum number of requests which may be queued for processing
    * by the application. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    *
    * @return        the maximum number of requests to queue
    */
   public int getMaxRequestsInQueue()
   {
      return m_maxRequestsInQueue;
   }

   /**
    * Set the maximum number of requests which may be queued for processing
    * by the application. When this limit is exceeded, the user is notified
    * that the server is too busy (HTTP status code 503).
    * <p>
    * The server can also specify the number of requests any application is
    * permitted to place in the queue. The application cannot exceed the
    * server defined limit.
    *
    * @param max     the maximum number of requests which can be queued. Use 0
    *            to prevent queueing. Use -1 for unlimited queueing.
    */
   public void setMaxRequestsInQueue(int max)
   {
      m_maxRequestsInQueue = max;
      setModified(true);
   }

   /**
    * Are user sessions being maintained by this application?
    *
    * @return        <code>true</code> if sessions are being used,
    *            <code>false</code> otherwise
    */
   public boolean isUserSessionEnabled()
   {
      return m_userSessions;
   }

   /**
    * Enable or disable user session management for this applicaiton. When a
    * user makes a connection to the E2 server, a session can be established
    * to maintain state information across requests.
    * <p>
    * The server can also specify that user session management is not
    * permitted. The application cannot enable user session management when
    * this is the case.
    *
    * @param enable     <code>true</code> to enable user session management,
    *               <code>false</code> to disable it
    */
   public void setUserSessionEnabled(boolean enable)
   {
      m_userSessions = enable;
      setModified(true);
   }

   /**
    * Get the timeout interval for user sessions.
    *
    * @return        the timeout interval for user sessions, in seconds
    */
   public int getUserSessionTimeout()
   {
      return m_sessionTimeout;
   }

   /**
    * Set the timeout interval for user sessions. When a user makes a
    * connection to the E2 server, a session can be established to maintain
    * state information across requests. When sessions are maintained, they
    * must be terminated after a period of inactivity. This setting
    * determines how long to wait before removing the inactive session.
    *
    * @param timeout  the timeout interval for user sessions, in seconds
    */
   public void setUserSessionTimeout(int timeout)
   {
      m_sessionTimeout = timeout;
      setModified(true);
   }

   /**
    * Get the name of the HTML parameter being used to identify the request
    * type.
    *
    * @return     the name of the HTML request type parameter
    * @see        #setRequestTypeHtmlParamName
    */
   public String getRequestTypeHtmlParamName()
   {
      return m_htmlParamName;
   }

   /**
    * Set the name of the HTML parameter used to identify the request type.
    * <p>
    * When using data sets supporting multiple actions (query, insert, etc.)
    * E2 needs to determine the type of action it must perform. This is done
    * by supplying the action type through a parameter. If the name of a
    * parameter is not set, E2 will use PSXActionParam by default. The
    * default values for the various actions are:
    * <table border="1">
    * <tr><th>Action</th><th>Value</th></tr>
    * <tr><td>Query</td> <td>QUERY</td> </tr>
    * <tr><td>Insert</td><td>INSERT</td></tr>
    * <tr><td>Update</td><td>UPDATE</td></tr>
    * <tr><td>Delete</td><td>DELETE</td></tr>
    * </table>
    * <p>
    * Let's assume the parameter is set to <code>action_type</code> and the
    * default values are being used. If we have an order entry system, we may
    * want to add, update or delete an item from the order. To add a new
    * item, the HTML request parameter string may look as follows:
    * <p>
    * <center><code>?action_type=INSERT&order_no=1&item_no=...</code></center>
    *
    * @param    param    the name of the HTML request type parameter
    *
    *                       if param exceeds 50 characters
    *
    * @see              #setRequestTypeValueQuery
    * @see              #setRequestTypeValueInsert
    * @see              #setRequestTypeValueUpdate
    * @see              #setRequestTypeValueDelete
    */
   public void setRequestTypeHtmlParamName(String param)
   {
      IllegalArgumentException ex = validateRequestTypeHtmlParamName(param);
      if (ex != null)
         throw ex;

      m_htmlParamName = param;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestTypeHtmlParamName(String param)
   {
      if ((param != null) && (param.length() > MAX_HTML_REQ_PARAM_NAME_LEN))
      {
         return new IllegalArgumentException("app req param html too big:" +
               MAX_HTML_REQ_PARAM_NAME_LEN + " " + param.length());
      }

      return null;
   }

   /**
    * Get the value being used to identify the request as being a query.
    *
    * @return     the query request type value
    *
    * @see        #setRequestTypeHtmlParamName
    */
   public String getRequestTypeValueQuery()
   {
      return m_requestTypeValueQuery;
   }

   /**
    * Set the value being used to identify the request as being a query. If
    * a data set contains only query pipes, this need not get set. If the
    * data set contains multiple pipes and a value is not set for query
    * type, the default value of "QUERY" is used.
    *
    * @param value the query request type value
    *
    * @see              #setRequestTypeHtmlParamName
    */
   public void setRequestTypeValueQuery(String value)
   {
      IllegalArgumentException ex = validateRequestTypeValueQuery(value);
      if (ex != null)
         throw ex;

      m_requestTypeValueQuery = value;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestTypeValueQuery(String value)
   {
      if ((value != null) && (value.length() > MAX_REQ_TYPE_VALUE_LEN))
      {
         return new IllegalArgumentException("app req type value too big:" +
               MAX_REQ_TYPE_VALUE_LEN + " " + value.length());
      }

      return null;
   }

   /**
    * Get the value being used to identify the request as being a insert.
    *
    * @return     the insert request type value
    *
    * @see        #setRequestTypeHtmlParamName
    */
   public String getRequestTypeValueInsert()
   {
      return m_requestTypeValueInsert;
   }

   /**
    * Set the value being used to identify the request as being a insert. If
    * a data set contains only insert pipes, this need not get set. If the
    * data set contains multiple pipes and a value is not set for insert
    * type, the default value of "INSERT" is used.
    *
    * @param value the insert request type value
    *
    * @see              #setRequestTypeHtmlParamName
    */
   public void setRequestTypeValueInsert(String value)
   {
      IllegalArgumentException ex = validateRequestTypeValueInsert(value);
      if (ex != null)
         throw ex;

      m_requestTypeValueInsert = value;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestTypeValueInsert(String value)
   {
      if ((value != null) && (value.length() > MAX_REQ_TYPE_VALUE_LEN))
      {
         return new IllegalArgumentException("app req type value too big:" +
               MAX_REQ_TYPE_VALUE_LEN + " " + value.length());
      }

      return null;
   }

   /**
    * Get the value being used to identify the request as being a update.
    *
    * @return     the update request type value
    *
    * @see        #setRequestTypeHtmlParamName
    */
   public String getRequestTypeValueUpdate()
   {
      return m_requestTypeValueUpdate;
   }

   /**
    * Set the value being used to identify the request as being a update. If
    * a data set contains only update pipes, this need not get set. If the
    * data set contains multiple pipes and a value is not set for update
    * type, the default value of "UPDATE" is used.
    *
    * @param value the update request type value
    *
    * @see              #setRequestTypeHtmlParamName
    */
   public void setRequestTypeValueUpdate(String value)
   {
      IllegalArgumentException ex = validateRequestTypeValueUpdate(value);
      if (ex != null)
         throw ex;

      m_requestTypeValueUpdate = value;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestTypeValueUpdate(String value)
   {
      if ((value != null) && (value.length() > MAX_REQ_TYPE_VALUE_LEN))
      {
         return new IllegalArgumentException("app req type value too big:" +
               MAX_REQ_TYPE_VALUE_LEN + " " + value.length());
      }

      return null;
   }

   /**
    * Get the value being used to identify the request as being a delete.
    *
    * @return     the delete request type value
    *
    * @see        #setRequestTypeHtmlParamName
    */
   public String getRequestTypeValueDelete()
   {
      return m_requestTypeValueDelete;
   }

   /**
    * Set the value being used to identify the request as being a delete. If
    * a data set contains only delete pipes, this need not get set. If the
    * data set contains multiple pipes and a value is not set for delete
    * type, the default value of "DELETE" is used.
    *
    * @param value the delete request type value
    *
    * @see              #setRequestTypeHtmlParamName
    */
   public void setRequestTypeValueDelete(String value)
   {
      IllegalArgumentException ex = validateRequestTypeValueDelete(value);
      if (ex != null)
         throw ex;

      m_requestTypeValueDelete = value;
      setModified(true);
   }

   private static IllegalArgumentException validateRequestTypeValueDelete(String value)
   {
      if ((value != null) && (value.length() > MAX_REQ_TYPE_VALUE_LEN))
      {
         return new IllegalArgumentException("app req type value too big:" +
               MAX_REQ_TYPE_VALUE_LEN + " " + value.length());
      }

      return null;
   }

   /**
    * Get the log settings for this application. Various levels of logging
    * can be defined, which may be used for a variety of tasks ranging from
    * usage tracking to application debugging.
    *
    * @return     the PSLogger object defining the log settings (may be null)
    */
   public PSLogger getLogger()
   {
      return m_logger;
   }

   /**
    * Overwrite the log settings associated with this application with the
    * specified object. If you only want to modify certain settings, use
    * getLogger to get the existing object and modify the returned object
    * directly. Be sure to check that getLogger did not return null, which
    * signifies log settings have never been created.
    * <p>
    * The PSLogger object supplied to this method will be stored with the
    * PSApplication object. Any subsequent changes made to the object by the
    * caller will also effect the application.
    *
    * @param logger     the new log settings or <code>null</code> to remove
    *               the application's log settings (which causes the
    *               server's log settings to be used)
    *
    * @see              #getLogger
    * @see              PSLogger
    */
   public void setLogger(PSLogger logger)
   {
      m_logger = logger;
      setModified(true);
   }

   /**
    * Get the web page used to login to this application. Application's have
    * ACL's associated with them. Unless a security scheme is being used
    * which does not require the user to login, a login page should be
    * defined.
    *
    * @return     the login page object (may be <code>null</code>)
    */
   public PSLoginWebPage getLoginWebPage()
   {
      return m_loginWebPage;
   }

   /**
    * Overwrite the login page associated with this application with the
    * specified object. If you only want to modify certain settings, use
    * getLoginWebPage to get the existing object and modify the returned
    * object directly. Be sure to check that getLoginWebPage did not return
    * null, which signifies a login page has not been defined.
    * <p>
    * The PSLoginWebPage object supplied to this method will be stored with
    * the PSApplication object. Any subsequent changes made to the object by
    * the caller will also effect the application.
    *
    * @param page  the new login web page
    *
    * @see              #getLoginWebPage
    * @see              PSLoginWebPage
    */
   public void setLoginWebPage(PSLoginWebPage page)
   {
      m_loginWebPage = page;
      setModified(true);
   }

   /**
    * Get the web pages being return on error for this application.
    * Application's can customize the errors they want to return by
    * defining an alternative web page E2 will return when the given error
    * is encountered. When E2 hits an error, it provdes error information
    * in the form of an XML document. To provide diagnostic information to
    * the requestor, it may be preferred to use style sheets which E2 can
    * merge with the XML document to return a descriptive error page.
    * <p>
    * This may be null, in which case E2 will use its default error pages.
    *
    * @return     the error pages object (may be <code>null<code>)
    */
   public PSErrorWebPages getErrorWebPages()
   {
      return m_errorWebPages;
   }

   /**
    * Overwrite the error pages associated with this application with the
    * specified object. If you only want to modify certain settings, use
    * getErrorWebPages to get the existing object and modify the returned
    * object directly. Be sure to check that getErrorWebPage did not return
    * null, which signifies an error page has not been defined.
    * <p>
    * The PSErrorWebPages object supplied to this method will be stored with
    * the PSApplication object. Any subsequent changes made to the object by
    * the caller will also effect the application.
    * <p>
    * This may be null, in which case E2 will use its default error pages.
    *
    * @param page   the new error web pages
    *
    * @see              #getErrorWebPages
    * @see              PSErrorWebPages
    */
   public void setErrorWebPages(PSErrorWebPages page)
   {
      m_errorWebPages = page;
      setModified(true);
   }

   /**
    * Get the data sets defined for accessing data through this application.
    * 
    * @return a collection containing the data sets defined for accessing data
    *         through this application (PSDataSet objects), never
    *         <code>null</code> may be empty.
    */
   public PSCollection getDataSets()
   {
      if (m_dataSets != null)
         return m_dataSets;

      try
      {
         return new PSCollection("com.percussion.design.objectstore.PSDataSet");
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Overwrite the data sets associated with this application with the
    * specified collection. If you only want to modify certain data sets,
    * add a new data set, etc. use getDataSets to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSApplication object. Any subsequent changes made to the object
    * by the caller will also effect the application.
    *
    * @param dataSets the new data sets to use for this application
    *
    * @see                 #getDataSets
    * @see                 PSDataSet
    */
   public void setDataSets(PSCollection dataSets)
   {
      IllegalArgumentException ex = validateDataSets(dataSets);
      if (ex != null)
         throw ex;

      m_dataSets = dataSets;
      setModified(true);
   }

   private static IllegalArgumentException validateDataSets(PSCollection dataSets)
   {
      if (dataSets != null)
   {
         if (!com.percussion.design.objectstore.PSDataSet.
             class.isAssignableFrom(
            dataSets.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type: Data Set" +
                  dataSets.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Returns the extension context previously set with the <code>
    * setExtensionContext</code> method.
    *
    * @return A possibly empty or <code>null</code> string that was set by
    * an external entitity.
    */
   public String getExtensionContext()
   {
      return m_context;
   }

   /**
    * Sets the extension context property of this application. The application
    * itself does not use this value. It just acts as a repository, saving and
    * restoring the value with the app data. The user of applications should
    * set this to a globally unique value. When an app is deleted by the
    * server, the server will request this value and if it is not null, the
    * server will scan all handlers for any extensions that contain this
    * context and delete any that it finds. It is strongly recommended that
    * this be set
    *
    * @param context The new context value. May be <code>null</code> or empty.
    */
   public void setExtensionContext(String context)
   {
      m_context = context;
   }

   /**
    * Get the private roles defined for use exclusively by this application.
    * Shared roles (available to all applications on the server) are not
    * returned through this mechanism. They must be requested through the
    * server.
    *
    * @return     a collection containing the private roles defined for this
    *       application (PSRole objects)
    */
   public PSCollection getRoles()
   {
      return m_roles;
   }

   /**
    * Overwrite the private roles associated with this application with the
    * specified collection. If you only want to modify certain role settings,
    * add new private roles, etc. use getRoles to get the existing collection
    * and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSApplication object. Any subsequent changes made to the object
    * by the caller will also effect the application.
    *
    * @param roles the new private roles to use for this application
    * @see              #getRoles
    * @see              PSRole
    */
   public void setRoles(PSCollection roles)
   {
      IllegalArgumentException ex = validateRoles(roles);
      if (ex != null)
         throw ex;

      m_roles = roles;
      setModified(true);
   }

   private static IllegalArgumentException validateRoles(PSCollection roles)
   {
      if (roles != null)
      {
         if (!com.percussion.design.objectstore.PSRole.class.isAssignableFrom(
            roles.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type: Role" +
                  roles.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Get the mail notification settings associated with the application.
    * Notification can be sent in response to various scenarios. See the
    * PSNotifier object for more info.
    *
    * @return     the mail notification settings associated with the application
    * @see        PSNotifier
    */
   public PSNotifier getNotifier()
   {
      return m_notifier;
   }

   /**
    * Overwrite the application's mail notification object with the specified
    * mail notification object. If you only want to modify some mail
    * notification settings, use getNotifier to get the existing object and
    * modify the returned object directly.
    * <p>
    * The PSNotifier object supplied to this method will be stored with the
    * PSApplication object. Any subsequent changes made to the object by the
    * caller will also effect the application.
    *
    * @param notifier the new mail notification settings for the
    *                application
    * @see                 #getNotifier
    * @see                 PSNotifier
    */
   public void setNotifier(PSNotifier notifier)
   {
      m_notifier = notifier;
      setModified(true);
   }

   private static IllegalArgumentException validateApplicationExtensions(PSCollection extensions)
   {
      if (extensions != null)
      {
         if (!PSExtensionRef.class.isAssignableFrom(
            extensions.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type: Application Extensions" +
                  extensions.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Get the user defined properties associated with the application. By
    * specifying user defined properties, a designer can store context
    * information, etc. on the E2 server. The user defined key/value pairs
    * are stored in the object store along with the application,
    * guaranteeing their existence between uses.
    *
    * @return      the user defined properties associated with the application
    *
    * @deprecated  use the getPropertyTree to get the properties as an XML tree
    */
   public java.util.Properties getUserProperties()
   {
      return m_userProperties;
   }

   /**
    * Overwrite the application's user defined properties with the specified
    * object. If you only want to modify some of the properties, add new
    * properies, etc. use getUserProperties to get the existing object and
    * modify the returned object directly.
    * <p>
    * The Properties object supplied to this method will be stored with the
    * PSApplication object. Any subsequent changes made to the object by the
    * caller will also effect the application.
    *
    * @param   props      the new properties to associate with the application
    *
    * @see               #getUserProperties
    *
    * @deprecated         use the setPropertyTree to store the properties
    *                     as an XML tree
    */
   public void setUserProperties(java.util.Properties props)
   {
      m_userProperties = props;
      setModified(true);
   }

   /**
    * Get the user defined properties associated with the application. By
    * specifying user defined properties, a designer can store context
    * information, etc. on the E2 server. The user defined XML tree structure
    * is stored in the object store along with the application,
    * guaranteeing its existence between uses.
    *
    * @return              the user defined properties associated with the
    *                     application
    *
    * @see               #setPropertyTree
    */
   public org.w3c.dom.Document getPropertyTree()
   {
      return m_propertyTree;
   }

   /**
    * Overwrite the application's user defined properties with the specified
    * object. If you only want to modify some of the properties, add new
    * properties, etc. use getPropertyTree to get the existing object and
    * modify the returned object directly.
    * <p>
    * The Document object supplied to this method will be stored with the
    * PSApplication object. Any subsequent changes made to the object by the
    * caller will also effect the application.
    *
    * @param propTree the new properties to associate with the application
    *
    * @see #getPropertyTree
    */
   public void setPropertyTree(org.w3c.dom.Document propTree)
   {
      m_propertyTree = propTree;
      setModified(true);
   }

   /**
    * Determine if the application has been modified since the last save
    * operation.
    * 
    * @return <code>true</code> if the application is modified, 
    * <code>false</code> otherwise.
    */
   public boolean isModified()
   {
      return m_modified;
   }

   /**
    * This method is used to update the application status to modified. Ex:
    * setAppName chnages the local app name. This will not be returned to server
    * until save on application is called. This flag will be used to find
    * weather any changes  are done to application that needs to be saved. This
    * will be mostly managed by application  class. Made public because, may be
    * required in some cases to explictly set it.
    * @param bModified  <code>true</code> if the application ia changed,
    *                   else <CODE>false</CODE>
    */
   public void setModified(boolean bModified)
   {
      m_modified = bModified;
   }

   /**
    * Compares the supplied application with this one.
    */
   @Override
   public boolean equals(Object o)
   {
      PSApplication other = (PSApplication) o;
      if (m_id != other.m_id)
         return false;

      if (m_beLoginPassthru != other.m_beLoginPassthru)
         return false;

      if (m_enabled != other.m_enabled)
         return false;

      if (m_hidden != other.m_hidden)
         return false;

      if (m_startPriority != other.m_startPriority)
         return false;

      if (m_active != other.m_active)
         return false;

      if (m_maxThreads != other.m_maxThreads)
         return false;

      if (m_maxRequestTime != other.m_maxRequestTime)
         return false;

      if (m_maxRequestsInQueue != other.m_maxRequestsInQueue)
         return false;

      if (m_userSessions != other.m_userSessions)
         return false;

      if (m_sessionTimeout != other.m_sessionTimeout)
         return false;

      if (!compare(m_name, other.m_name))
         return false;

      if (!compare(m_description, other.m_description))
         return false;

      if (!compare(m_requestRoot, other.m_requestRoot))
         return false;

      if (!compare(m_defaultRequestPage, other.m_defaultRequestPage))
         return false;

      if (!compare(m_context, other.m_context))
         return false;

      if (!compare(m_acl, other.m_acl))
         return false;

      if (!compare(m_dataEncryptor, other.m_dataEncryptor))
         return false;

      if (!compare(m_dataSets, other.m_dataSets))
         return false;

      if (!compare(m_logger, other.m_logger))
         return false;

      if (!compare(m_loginWebPage, other.m_loginWebPage))
         return false;

      if (!compare(m_errorWebPages, other.m_errorWebPages))
         return false;

      if (!compare(m_roles, other.m_roles))
         return false;

      if (!compare(m_notifier, other.m_notifier))
         return false;

      if (!compare(m_userProperties, other.m_userProperties))
         return false;

      // can't use standard equals on XML documents
      if (m_propertyTree == null || other.m_propertyTree == null)
      {
         if (m_propertyTree != null || other.m_propertyTree != null)
            return false;
      }
      // both of the nodes can be null at this point,
      // do a deep comparison if both are not null
      else if ((m_propertyTree != null) && (other.m_propertyTree != null))
      {
         // DOM2 interface does not have any function for dooing a deep
         // comparison of nodes (its there in DOM3 Node3 class)
         // currently have to use Xerces's isEqualNode method of
         // org.apache.xerces.dom.ParentNode class for comparison
         // this may be a problem when using a non-xerces implementation of JAXP
         if ((m_propertyTree instanceof ParentNode) &&
            (other.m_propertyTree instanceof ParentNode))
         {
            ParentNode pn = (ParentNode)m_propertyTree;
            ParentNode pnOther = (ParentNode)other.m_propertyTree;
            if (!(pn.isEqualNode(pnOther)))
               return false;
         }
         else
         {
            return false;
         }
      }

      if (!compare(m_htmlParamName, other.m_htmlParamName))
         return false;

      if (!compare(m_requestTypeValueQuery, other.m_requestTypeValueQuery))
         return false;

      if (!compare(m_requestTypeValueInsert, other.m_requestTypeValueInsert))
         return false;

      if (!compare(m_requestTypeValueUpdate, other.m_requestTypeValueUpdate))
         return false;

      if (!compare(m_requestTypeValueDelete, other.m_requestTypeValueDelete))
         return false;

      if (!compare(m_version, other.m_version))
         return false;

      if (!compare(m_history, other.m_history))
         return false;

      return true;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder(61, 157)
            .append(m_id)
            .append(m_name)
            .append(m_description)
            .append(m_requestRoot)
            .toHashCode();
   }
   
   /* *************** IPSDocument Interface Implementation *************** */

   /**
    * This method is called to create a PSXApplication XML document
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *   &lt;!--
    *    PSXApplication is used to manipulate an E2 application's
    *    definition.
    *
    *    Object References:
    *
    *    PSXAcl - the application's access control list (ACL). This
    *    contains the names of users, groups and roles and the type of
    *    access they have for this application.
    *
    *    PSXDataEncryptor - the application's data encryption settings.
    *    Through this object, E2 can force users to make requests through
    *    SSL. It can even be used to enforce the key strength is
    *    appropriate for the given application. This allows the
    *    application's data to be sent over secure channels. Incoming
    *    requests from users, however, can still be sent in the clear. For
    *    this reason, care must be taken when designing web pages so that
    *    forms containing sensitive data, including user ids and
    *    passwords, are submitted using HTTPS, not HTTP.
    *
    *    PSXDataSet - the data sets defined for accessing data through
    *    this application.
    *
    *    PSXLogger - the log settings for this application. Various levels
    *    of logging can be defined, which may be used for a variety of
    *    tasks ranging from usage tracking to application debugging.
    *
    *    PSXLoginWebPage - the web page used to login to this application.
    *    Application's have ACL's associated with them. Unless a security
    *    scheme is being used which does not require the user to login, a
    *    login page should be defined.
    *
    *    PSXErrorWebPages - the web pages being return on error for this
    *    application. Application's can customize the errors they want to
    *    return by defining an alternative web page E2 will return when
    *    the given error is encountered. When E2 hits an error, it provdes
    *    error information in the form of an XML document. To provide
    *    diagnostic information to the requestor, it may be preferred to
    *    use style sheets which E2 can merge with the XML document to
    *    return a descriptive error page.
    *
    *    PSXBackEndCredential - the default back-end credentials which
    *    will be used when accessing data through this application.
    *    Back-end credentials define the login id and password to use when
    *    accessing a specific back-end data store. These can be defined at
    *    the server level, or at the application level. All access to the
    *    back-end will be performed through these ids.
    *
    *
    *    PSXRole - the private roles defined for use exclusively by this
    *    application. Shared roles (available to all applications on the
    *    server) are not returned through this mechanism. They must be
    *    requested through the server.
    *
    *    PSXNotifier - the mail notification settings associated with the
    *    application. Notification can be sent in response to various
    *    scenarios. See the PSXNotifier object for more info.
    *   --&gt;
    *   &lt;!ELEMENT PSXApplication  (name, description?, requestRoot?,
    *                                 defaultRequestPage?, PSXAcl,
    *                                 PSXDataEncryptor?,
    *                                 maxThreads?, maxRequestTime?,
    *                                 maxRequestsInQueue?, userSessionEnabled?,
    *                                 userSessionTimeout?,
    *                                 requestTypeHtmlParamName?,
    *                                 requestTypeValueQuery?,
    *                                 requestTypeValueInsert?,
    *                                 requestTypeValueUpdate?,
    *                                 requestTypeValueDelete?,
    *                                 ApplicationExtensions?,
    *                                 PSXDataSet*, PSXLogger?, PSXLoginWebPage?,
    *                                 PSXErrorWebPages?, 
    *                                 backEndLoginPassthru?, PSXRole*,
    *                                 PSXNotifier?, userProperty*)&gt;
    *
    *   &lt;!--
    *    Attributes associated with the application:
    *
    *    id - the internal identifier for this application. This should
    *    only be set by the E2 engine.
    *
    *    enabled - is the application enabled or disabled? If the
    *    application is enabled, the server will begin executing it upon
    *    save. When the server starts up, it also begins executing enabled
    *    applications. When the server shuts down, it ends execution of
    *    all running applications.
    *
    *    hidden - is the application hidden? If the application is hidden,
    *    it will not be available for external http requests and will be
    *    hidden from the designer.
    *
    *    startPriority - Server starts apps in order based on this value. The
    *    higher the value, the sooner they start. All apps at the same priority
    *    start in a random order.
    *
    *    active - is this application running on the server? This is
    *    ignored when sending the document back to the server for updating.
    *   
    *    appType - the application type for this application
    *   --&gt;
    *   &lt;!ATTLIST
    *    id         ID             #OPTIONAL
    *    enabled   %PSXIsEnabled   #OPTIONAL
    *    hidden    %PSXIsEnabled   #OPTIONAL
    *    startPriority CDATA       #OPTIONAL
    *    active     %PSXIsEnabled  #OPTIONAL
    *    appType    CDATA          #OPTIONAL
    *   &gt;
    *
    *   &lt;!--
    *    the name of the application. This must be a unique name on the
    *    server. This is limited to 50 characters.
    *   --&gt;
    *   &lt;!ELEMENT name                        (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the description of the application. This is limited to 255
    *    characters.
    *   --&gt;
    *   &lt;!ELEMENT description            (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the application's request root. This is combined with the E2
    *    server request root to determine if an incoming request should
    *    be serviced. For instance, if the server root is /E2 and the
    *    application root is /MyApp, only URLs whose path begins with
    *    /E2/MyApp will be considered for processing. This helps alleviate
    *    strain from the web server as E2 can more quickly determine what
    *    is an E2 request rather than a standard web server request. This
    *    is limited to 50 characters.
    *   --&gt;
    *   &lt;!ELEMENT requestRoot            (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the name of the default request page.
    *   --&gt;
    *   &lt;!ELEMENT defaultRequestPage     (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum number of threads this application is permitted to
    *    consume. The server maintains a pool of threads, which can grow
    *    as activity increases. The number of threads can be limited to
    *    avoid the resource problems that arise from excessive thread use.
    *   --&gt;
    *   &lt;!ELEMENT maxThreads                  (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum amount of time to spend servicing a request. The
    *    server can also specify the amount of time any application is
    *    permitted to spend servicing a request. The application cannot
    *    exceed the server defined limit.
    *   --&gt;
    *   &lt;!ELEMENT maxRequestTime         (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the maximum number of requests which may be queued for processing
    *    by the application. When this limit is exceeded, the user is
    *    notified that the server is too busy (HTTP status code 503). The
    *    server can also specify the number of requests any application is
    *    permitted to place in the queue. The application cannot exceed
    *    the server defined limit.
    *   --&gt;
    *   &lt;!ELEMENT maxRequestInQueue      (#PCDATA)&gt;
    *
    *   &lt;!--
    *    is user session management enabled for this applicaiton? When a
    *    user makes a connection to the E2 server, a session can be
    *    established to maintain state information across requests. The
    *    server can also specify that user session management is not
    *    permitted. The application cannot enable user session management
    *    when this is the case.
    *   --&gt;
    *   &lt;!ELEMENT userSessionEnabled     (%PSXIsEnabled)&gt;
    *
    *   &lt;!--
    *    the timeout interval for user sessions. When a user makes a
    *    connection to the E2 server, a session can be established to
    *    maintain state information across requests. When sessions are
    *    maintained, they must be terminated after a period of inactivity.
    *    This setting determines how long to wait before removing the
    *    inactive session.
    *   --&gt;
    *   &lt;!ELEMENT userSessionTimeout     (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the name of the HTML parameter used to identify the request type.
    *    When using data sets supporting multiple actions (query, insert,
    *    etc.) E2 needs to determine the type of action it must perform.
    *    This is done by supplying the action type through a parameter. If
    *    the name of a parameter is not set, E2 will use PSXActionParam by
    *    default. The default values for the various actions are:
    *
    *       Action     Value
    *       ========   ========
    *       Query      QUERY
    *       Insert     INSERT
    *       Update     UPDATE
    *       Delete     DELETE
    *   --&gt;
    *   &lt;!ELEMENT requestTypeHtmlParamName (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the value being used to identify the request as being a query.
    *    If a data set contains only query pipes, this need not get set.
    *    If the data set contains multiple pipes and a value is not set
    *    for query type, the default value of "QUERY" is used.
    *   --&gt;
    *   &lt;!ELEMENT requestTypeValueQuery  (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the value being used to identify the request as being a insert.
    *    If a data set contains only insert pipes, this need not get set.
    *    If the data set contains multiple pipes and a value is not set
    *    for insert type, the default value of "INSERT" is used.
    *   --&gt;
    *   &lt;!ELEMENT requestTypeValueInsert      (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the value being used to identify the request as being a update.
    *    If a data set contains only update pipes, this need not get set.
    *    If the data set contains multiple pipes and a value is not set
    *    for update type, the default value of "UPDATE" is used.
    *   --&gt;
    *   &lt;!ELEMENT requestTypeValueUpdate      (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the value being used to identify the request as being a delete.
    *    If a data set contains only delete pipes, this need not get set.
    *    If the data set contains multiple pipes and a value is not set
    *    for delete type, the default value of "DELETE" is used.
    *   --&gt;
    *   &lt;!ELEMENT requestTypeValueDelete      (#PCDATA)&gt;
    *
    *   &lt;!--
    *         the extensions defined for use by this application.
    *   --&gt;
    *   &lt;!ELEMENT ApplicationExtensions            (extension*)&gt;
    *
    *   &lt;!--
    *    Is back end login passthru enabled for this applicaiton? When a
    *    user makes a connection to the E2 server, their credentials
    *    can be passed to the back end after they have authenticated.
    *      Any value other than "yes" will be treated as "no" or no passthru
    *      allowed, if this element is not present, it will be treated as "no".
    *   --&gt;
    *   &lt;!ELEMENT backEndLoginPassthru     (#PCDATA)&gt;
    *
    *   &lt;!--
    *    a user defined property associated with the application. By
    *    specifying user defined properties, a designer can store context
    *    information, etc. on the E2 server. The user defined key/value
    *    pairs are stored in the object store along with the application,
    *    guaranteeing their existence between uses.
    *   --&gt;
    *   &lt;!ELEMENT userProperty           (#PCDATA)&gt;
    *
    *   &lt;!--
    *    the name (key) associated with this property
    *   --&gt;
    *   &lt;!ATTLIST userProperty
    *    name CDATA     #REQUIRED
    *   &gt;
    *
    *   &lt;!--
    *    the user-defined property tree. This will contain an XML
    *      tree where the "root" node of the user-defined tree is the
    *    child of this node.
    *   --&gt;
    *   &lt;!ELEMENT PropertyTree           (#PCDATA)&gt;
    * </code></pre>
    *
    * @return      the newly PSXApplication XML document
    */
   public Document toXml()
   {
      String      sTemp;
      Document       doc = PSXmlDocumentBuilder.createXmlDocument();

      //create PSXApplication object and add attributes
      Element root = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);

      root.setAttribute ("id", String.valueOf(m_id));
      root.setAttribute ("active", m_active ? "yes" : "no");
      root.setAttribute ("hidden", m_hidden ? "yes" : "no");
      root.setAttribute (STARTPRIORITY_ATTRNAME, ""+m_startPriority);
      root.setAttribute ("enabled", m_enabled ? "yes" : "no");
      root.setAttribute ("version", m_version);
      
      //handle application type
      String appType = "";
      if(m_appType != null)
         appType = m_appType.toString();
      root.setAttribute(ATTR_APPLICATION_TYPE, appType);
         

      //create application name element
      PSXmlDocumentBuilder.addElement(doc, root, "name", m_name);

      //Create description element
      PSXmlDocumentBuilder.addElement(doc, root, "description", m_description);

      //create requestRoot element
      PSXmlDocumentBuilder.addElement(doc, root, "requestRoot", m_requestRoot);

      //create defaultRequestPage element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "defaultRequestPage",
                                      m_defaultRequestPage);

      if (null != m_context)
         PSXmlDocumentBuilder.addElement(
            doc,
            root,
            "appExtensionContext",
            m_context);

      // create revision history element
      if (m_history != null)
         root.appendChild(m_history.toXml(doc));

      //create PSAcl elements
      if (m_acl != null)
         root.appendChild(m_acl.toXml(doc));

      //create PSDataEncryptor elements
      if (m_dataEncryptor != null)
         root.appendChild(m_dataEncryptor.toXml(doc));

      //create maxThreads element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "maxThreads",
         String.valueOf(m_maxThreads));

      //create maxRequestTime element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "maxRequestTime",
         String.valueOf(m_maxRequestTime));

      //create maxRequestInQueue element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "maxRequestsInQueue",
         String.valueOf(m_maxRequestsInQueue));

      //create userSessionEnabled element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "userSessionEnabled",
         (m_userSessions ? "yes" : "no"));

      //create user sessions time out
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "userSessionTimeout",
         String.valueOf(m_sessionTimeout));

      //create requestTypeHtmlParamName element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "requestTypeHtmlParamName",
         m_htmlParamName);

      //create requestTypeValueQuery element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "requestTypeValueQuery",
         m_requestTypeValueQuery);

      //create requestTypeValueInsert element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "requestTypeValueInsert",
         m_requestTypeValueInsert);

      //create requestTypeValueUpdate element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "requestTypeValueUpdate",
         m_requestTypeValueUpdate);

      //create requestTypeValueDelete element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "requestTypeValueDelete",
         m_requestTypeValueDelete);

      // create the data sets now
      PSCollectionComponent.appendCollectionToXml(doc, root, m_dataSets);

      //create PSLogger elements
      if (m_logger != null)
         root.appendChild(m_logger.toXml(doc));

      //create PSTraceInfo elements, if null,
      //one will be added with default values
      root.appendChild(getTraceInfo().toXml(doc));

      //create PSErrorWebPages elements
      if (m_errorWebPages != null)
         root.appendChild(m_errorWebPages.toXml(doc));

      //create backEndLoginPassthru element
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "backEndLoginPassthru",
         (m_beLoginPassthru ? "yes" : "no"));

      //create m_roles elements
      PSCollectionComponent.appendCollectionToXml(doc, root, m_roles);

      //create PSNotifier elements
      if (m_notifier != null)
         root.appendChild(m_notifier.toXml(doc));

      // create the user properties
      if (m_userProperties != null)
      {
         for (Enumeration e = m_userProperties.keys(); e.hasMoreElements();)
         {
            sTemp = (String) e.nextElement();
               Element el =
                  PSXmlDocumentBuilder.addElement(doc, // the parent document
               root,   // where to add
               "userProperty", // element name is "userProperty"
               m_userProperties.getProperty(sTemp)); // element value

            // make sure the name of the user property is set
            el.setAttribute("name", sTemp);
         }
      }

      // if a property tree was specified, copy it over...
      Element propRoot =
         (m_propertyTree == null) ? null : m_propertyTree.getDocumentElement();
      if (propRoot != null)
      {
         // in one op, we can copy the tree as a child of our tree root
         PSXmlDocumentBuilder.copyTree(
            doc,
            PSXmlDocumentBuilder.addEmptyElement(doc, root, "PropertyTree"),
            propRoot);
      }

      return doc;
   }

   /**
    * This method is called to populate a PSApplication Java object
    * from a PSXApplication XML document. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @exception     PSUnknownDocTypeException  if the XML document is not
    *                                       of type PSXApplication
    */
   public void fromXml(Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if (null == sourceDoc)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      //make sure we got the correct root node tag
      if (!ms_NodeType.equals(root.getNodeName()))
      {
         Object[] args = { ms_NodeType, root.getNodeName()};
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      //Read PSXApplication object attributes
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);

      String sTemp = tree.getElementData("version");
      if (sTemp != null)
         m_version = sTemp;

      /* Check for conversion and convert 1.0 to 1.1 if necessary */
      if (m_version.equals("1.0"))
      {
         try
         {
            PSDocVersionConverter cvt = new PSDocVersionConverter("1.1");
            Document doc = cvt.convertOneZeroToOneOne(sourceDoc);

            tree = new PSXmlTreeWalker(doc);

            sTemp = tree.getElementData("version");
            if (sTemp != null)
               m_version = sTemp;
         }
         catch (Exception e)
         {
            Object[] args = { "1.0", "1.1", e.toString()};
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors
               .DOC_CONVERSION_FAILED
            /*code*/
            , args);
         }

      }

      sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp)};
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID,
            args);
      }

      sTemp = tree.getElementData(ATTR_APPLICATION_TYPE);
      if(sTemp != null && sTemp.trim().length() > 0)
      {
         m_appType = PSApplicationType.valueOf(sTemp);
      }
      else
      {
         m_appType = null;
      }
      
      sTemp = tree.getElementData("active");
      if (sTemp != null)
         m_active = sTemp.equalsIgnoreCase("yes");

      sTemp = tree.getElementData("enabled");
      if (sTemp != null)
         m_enabled = sTemp.equalsIgnoreCase("yes");

      sTemp = tree.getElementData("hidden");
      if (sTemp != null)
         m_hidden = sTemp.equalsIgnoreCase("yes");

      sTemp = tree.getElementData(STARTPRIORITY_ATTRNAME);
      try
      {
         if (null != sTemp)
            m_startPriority = Integer.parseInt(sTemp);
      }
      catch (NumberFormatException e)
      {
         Object[] args = { ms_NodeType + "/@" + STARTPRIORITY_ATTRNAME, sTemp };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      //get application name element
      try
      {

         setName(tree.getElementData("name"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType, "name", new PSException (e));
      }

      // set the conversion context now that we know the name
      String ctx = "application \"" + m_name + "\""; 
      for (IPSComponentConverter converter : 
         PSComponent.getComponentConverters())
      {
         converter.setConversionContext(ctx);
      }
      
      //Get description from XML
      try
      {
         setDescription(tree.getElementData("description"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType, "description", new PSException (e.getLocalizedMessage()));
      }

      //Get requestRoot from XML
      try
      {
         setRequestRoot(tree.getElementData("requestRoot"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType, "requestRoot", new PSException (e.getLocalizedMessage()));
      }

      //Get defaultRequestPage from XML
      setDefaultRequestPage(tree.getElementData("defaultRequestPage"));

      m_context = tree.getElementData("appExtensionContext");

      // when walking at this level, we only want to traverse siblings
      int walkerFlags =
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT
            | PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

      // now build the arraylist we'll use in the fromXml calls
      java.util.List parentComponents = new java.util.ArrayList<>();

      // now position the walker on the first child of the application
      tree.getNext();   // we don't actually care what it is

      m_history = null;
      // get the revision history from XML
      if (tree.getNextElement(PSRevisionHistory.ms_nodeType, walkerFlags)
          != null)
      {
         if (m_history == null)
            m_history = new PSRevisionHistory();

         m_history.fromXml(
            (Element) tree.getCurrent(),
            this,
            parentComponents);
      }

      //construct new   PSAcl from xml
      if (tree.getNextElement(PSAcl.ms_NodeType, walkerFlags) != null)
      {
         m_acl =
            new PSAcl((Element) tree.getCurrent(), this, parentComponents);
      }

      //create PSDataEncryptor object from XML node
      if (tree.getNextElement(PSDataEncryptor.ms_NodeType, walkerFlags)
         != null)
      {
         m_dataEncryptor =
            new PSDataEncryptor(
               (Element) tree.getCurrent(),
                                             this,
                                             parentComponents);
      }

      //create maxThreads element
      sTemp = tree.getElementData("maxThreads");
      if (sTemp == null)
         m_maxThreads = 0;
      else
      {
         try
         {
            m_maxThreads = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException e)
         {
            Object[] args = { ms_NodeType, "maxThreads", sTemp };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }
      }

      //create maxRequestTime element
      sTemp = tree.getElementData("maxRequestTime");
      if (sTemp == null)
         m_maxRequestTime = 0;
      else
      {
         try
         {
            m_maxRequestTime = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException e)
         {
            Object[] args = { ms_NodeType, "maxRequestTime", sTemp };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }
      }

      //create maxRequestInQueue element
      sTemp = tree.getElementData("maxRequestsInQueue");
      if (sTemp == null)
         m_maxRequestsInQueue = 0;
      else
      {
         try
         {
            m_maxRequestsInQueue = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException e)
         {
            Object[] args = { ms_NodeType, "maxRequestsInQueue", sTemp };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }
      }

      //create userSessionEnabled element
      sTemp = tree.getElementData("userSessionEnabled");
      if (sTemp != null)
         m_userSessions = sTemp.equalsIgnoreCase("yes");

      //create user sessions time out
      sTemp = tree.getElementData("userSessionTimeout");
      if (sTemp == null)
         m_sessionTimeout = 0;
      else
      {
         try
         {
            m_sessionTimeout = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException e)
         {
            Object[] args = { ms_NodeType, "userSessionTimeout", sTemp };
            throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }
      }

      //create requestTypeHtmlParamName element
      try
      {
         setRequestTypeHtmlParamName(
            tree.getElementData("requestTypeHtmlParamName"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType,
                                             "requestTypeHtmlParamName",
                                             new PSException (e.getLocalizedMessage()));
      }

      //create requestTypeValueQuery element
      try
      {
         setRequestTypeValueQuery(
            tree.getElementData("requestTypeValueQuery"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType,
                                             "requestTypeValueQuery",
                                             new PSException (e.getLocalizedMessage()));
      }

      //create requestTypeValueInsert element
      try
      {
         setRequestTypeValueInsert(
            tree.getElementData("requestTypeValueInsert"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType,
                                             "requestTypeValueInsert",
                                             new PSException (e.getLocalizedMessage()));
      }

      //create requestTypeValueUpdate element
      try
      {
         setRequestTypeValueUpdate(
            tree.getElementData("requestTypeValueUpdate"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType,
                                             "requestTypeValueUpdate",
                                             new PSException (e.getLocalizedMessage()));
      }

      //create requestTypeValueDelete element
      try
      {
         setRequestTypeValueDelete(
            tree.getElementData("requestTypeValueDelete"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownDocTypeException(ms_NodeType,
                                             "requestTypeValueDelete",
                                             new PSException (e.getLocalizedMessage()));
      }

      //create m_dataSets from XML nodes
      if (null != m_dataSets)
         m_dataSets.clear();

      // get all data sets
      Node current = tree.getCurrent();
      if (tree.getNextElement(PSDataSet.ms_NodeType, walkerFlags) != null)
      {
         PSDataSet dataSet;
         if (null == m_dataSets)
         {
            m_dataSets =
               new PSCollection(
               com.percussion.design.objectstore.PSDataSet.class);
         }

         do
         {
            dataSet =
               new PSDataSet(
                  (Element) tree.getCurrent(),
                  this,
                  parentComponents);
            m_dataSets.add(dataSet);
         }
         while (tree.getNextElement(PSDataSet.ms_NodeType, walkerFlags)
            != null);
      }
      tree.setCurrent(current);

      // get all content editors
      if (tree.getNextElement(PSContentEditor.XML_NODE_NAME, walkerFlags)
         != null)
      {
         PSContentEditor dataSet;
         if (null == m_dataSets)
            m_dataSets = new PSCollection(PSDataSet.class);

         do
         {
            dataSet =
               new PSContentEditor(
                  (Element) tree.getCurrent(),
                  this,
                  parentComponents);
            m_dataSets.add(dataSet);
         }
         while (tree.getNextElement(PSContentEditor.XML_NODE_NAME, walkerFlags)
               != null);
      }

      // get PSLogger object from XML
      m_logger = null;
      if (tree.getNextElement(PSLogger.ms_NodeType, walkerFlags) != null)
      {
         m_logger =
            new PSLogger((Element) tree.getCurrent(), this, parentComponents);
      }
      else
         m_logger = new PSLogger();

      // get the PSTraceInfo object from XML
      m_traceInfo = null;
      if (tree.getNextElement(PSTraceInfo.ms_NodeType, walkerFlags) != null)
      {
         m_traceInfo =
            new PSTraceInfo(
               (Element) tree.getCurrent(),
               this,
               parentComponents);
      }
      else
         m_traceInfo = new PSTraceInfo();

      //handle old PSLoginWebPage definition
      m_loginWebPage = null;
      if (tree.getNextElement(PSLoginWebPage.ms_NodeType, walkerFlags)
         != null)
      {
         PSLoginWebPage loginWebPage =
            new PSLoginWebPage(
               (Element) tree.getCurrent(),
               this,
               parentComponents);
         if (loginWebPage.getUrl() != null)
         {
            // warn that custom login page is ignored
            PSConsole.printInfoMsg("Server",
               IPSObjectStoreErrors.APP_LOGIN_PAGE_NOT_SUPPORTED, 
               new Object[] {m_name}, Level.INFO);
         }
      }

      //create PSErrorWebPages from XML
      m_errorWebPages = null;
      if (tree.getNextElement(PSErrorWebPages.ms_NodeType, walkerFlags)
         != null)
      {
         m_errorWebPages =
            new PSErrorWebPages((Element) tree.getCurrent(), this, null);
      }

      // Check for old backend credentials declaration
      if (tree.getNextElement(PSBackEndCredential.ms_NodeType, walkerFlags)
          != null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.APP_BACKEND_CREDS_NOT_SUPPORTED, m_name);
      }

      // are logins allowed to pass through to the back end?
      sTemp = tree.getElementData("backEndLoginPassthru");
      if (sTemp != null && sTemp.equalsIgnoreCase("yes"))
         m_beLoginPassthru = true;

      /* As we don't support application roles any more, if the
       * application files have role elements, throw exception with
       * the message that this application does not support roles.
       */
      if (tree.getNextElement("PSXRole", walkerFlags) != null)
      {
         String roleNames = "";
         do
         {
            if (roleNames.length() > 0)
               roleNames += ",";

            roleNames += getRoleName(tree.getCurrent());
         } while (tree.getNextElement("PSXRole", walkerFlags) != null);

         Object[] args = { roleNames };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.APP_ROLES_NOT_SUPPORTED,
            args);
      }

      //create PSNotifier elements
      if (tree.getNextElement(PSNotifier.ms_NodeType, walkerFlags) != null)
      {
         if (null == m_notifier)
            m_notifier = new PSNotifier();

         m_notifier.fromXml(
            (Element) tree.getCurrent(),
            this,
            parentComponents);
      }

      //create userProperties element
      if (tree.getNextElement("userProperty", walkerFlags) != null)
      {
         m_userProperties = new java.util.Properties();

         do
         {
            m_userProperties.put(
               tree.getElementData("name", false),
               tree.getElementData((Element) tree.getCurrent()));
         }
         while (tree.getNextElement("userProperty", walkerFlags) != null);
      }

      // if a property tree was specified, copy it over...
      m_propertyTree = null;   // reset it first
      Element propRoot = tree.getNextElement("PropertyTree", walkerFlags);
      if (propRoot != null)
      {
         // our one and only child contains the property tree, so get it
         propRoot =
            tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (propRoot != null)
         {
            m_propertyTree = PSXmlDocumentBuilder.createXmlDocument();

            // in one op, we can copy the tree as a child of our doc
            // (which creates the root and all the children)
            PSXmlDocumentBuilder.copyTree(
               m_propertyTree,
                                          m_propertyTree,
                                          propRoot);
         }
      }
      // Guess the app type if not defined
      if (m_appType == null)
         m_appType = guessAppType();

      // clear the conversion context
      for (IPSComponentConverter converter : 
         PSComponent.getComponentConverters())
      {
         converter.setConversionContext(null);
      }
   }

   /**
    * Guess the application type.
    * <ol>
    * <li>check if any of the dataset has a pipe of the type
    * {@link PSContentEditorPipe}. If so return type content editor</li>
    * <li>check if the name of the application starts with "sys_", if so return
    * system type.</li>
    * <li>otherwise return user type.</li>
    * 
    * @return application type evaluated as above.
    */
   private PSApplicationType guessAppType()
   {
      Iterator iter = getDataSets().iterator();
      while (iter.hasNext())
      {
         PSDataSet ds = (PSDataSet) iter.next();
         if(ds.getPipe() instanceof PSContentEditorPipe)
            return PSApplicationType.CONTENT_EDITOR;
      }
      if(m_name.startsWith("sys_"))
      {
         return PSApplicationType.SYSTEM;
      }
      return PSApplicationType.USER;
   }

   /**
    * Gets role name from 'PSXRole' node. If it has multiple 'name' child
    * elements, takes the text of first 'name' child. If it doesn't find the
    * 'name' child element, returns an empty string.
    *
    * @param roleNode the role node to get role name, assumed not
    * <code>null</code>
    *
    * @return the role name, never <code>null</code> may be empty if it doesn't
    * find name.
    **/
   private String getRoleName(Node roleNode)
   {
      String roleName = "";
      if (roleNode instanceof Element)
      {
         Element role = (Element) roleNode;
         NodeList elements = role.getElementsByTagName("name");
         if (elements != null && elements.getLength() >= 1)
         {
            roleName = PSXmlTreeWalker.getElementData(elements.item(0));
         }
      }

      return roleName;
   }

   /**
    * Validates the entire application within the given validation context.
    * <OL>
    * <LI>Static validation on all fields. This means that all tests are
    * performed that can be performed from a static method that takes the
    * field as its sole argument.
    * <BR>
    * This includes: verifying the presence (not-null) of fields that are
    * always required, verifying that the length and value of non-null
    * fields is within preset ranges (such as the length of name strings,
    * the value of integers that can never be negative or zero).
    * <LI>Validate all IPSComponent objects recursively.
    * <LI>Validate the semantics of all collections and relations of
    * IPSComponent objects. This includes checking for uniqueness of
    * things that need to be unique within their collection (like
    * request pages).
    * </OL>
    * @author   chadloder
    *
    * @version 1.26 1999/06/07
    *
    * @param   cxt
    *
    * @throws PSSystemValidationException
    *
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      // static validation on all statically validatable fields
      IllegalArgumentException ex = validateName(m_name);
      ex = validateDescription(m_description);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRequestRoot(m_requestRoot);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateAcl(m_acl);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateDataSets(m_dataSets);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRoles(m_roles);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRequestTypeValueQuery(m_requestTypeValueQuery);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRequestTypeValueInsert(m_requestTypeValueInsert);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRequestTypeValueUpdate(m_requestTypeValueUpdate);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateRequestTypeValueDelete(m_requestTypeValueDelete);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());

      ex = validateVersion(m_version);
      if (ex != null)
         cxt.validationError(null, 0, ex.getLocalizedMessage());


      // now do children
      if (m_acl != null)
         m_acl.validate(cxt);

      if (m_dataEncryptor != null)
         m_dataEncryptor.validate(cxt);

      if (m_dataSets != null)
      {
         if (m_dataSets.size() == 0)
         {
            // no data sets; the app won't do anything useful
            cxt.validationWarning(null,
                                  IPSObjectStoreErrors.APP_NO_DATASETS,
                                  m_name);
         }

         // to keep track of possible request page duplicates
         Set requestKeys = new HashSet();
         Map dsNames = new HashMap(m_dataSets.size());

         for (int i = 0; i < m_dataSets.size(); i++)
         {
            Object o = m_dataSets.get(i);
            PSDataSet set = (PSDataSet) o;
            set.validate(cxt);

            // make sure there are no data sets with duplicate names
            // this renders unnecessary the similar validation in
            // PSApplicationHandler
            String dsName = set.getName().toLowerCase();

            // Check for uniqueness in the request pages. Each request page
            // must not conflict with another by name unless their selection
            // parameters are different
            RequestKey key = new RequestKey(set);
            if (requestKeys.contains(key))
            {
               // Error
               Object[] args =
                  new Object[] { set.getRequestor().getRequestPage(), m_name };

               cxt.validationError(
                  null,
                  IPSObjectStoreErrors.REQUEST_NAME_DUP,
                  args);
            }
            else
            {
               requestKeys.add(key);
            }

            if (null != dsNames.get(dsName))
            {
               Object[] args = new Object[] { m_name, dsName };

               cxt.validationError(null,
                                   IPSObjectStoreErrors.APP_DATASET_NAMES_DUP,
                                   args);
            }
            else
               dsNames.put(dsName, dsName);
         }
      }
      else
      {
         // no data sets; the app won't do anything useful
         cxt.validationWarning(null,
                               IPSObjectStoreErrors.APP_NO_DATASETS,
                               m_name);
      }

      if (m_logger != null)
         m_logger.validate(cxt);

      if (m_loginWebPage != null)
         m_loginWebPage.validate(cxt);


      if (m_roles != null)
      {
         for (int i = 0; i < m_roles.size(); i++)
         {
            Object o = m_roles.get(i);
            PSRole role = (PSRole) o;
            role.validate(cxt);
         }
      }

      if (m_notifier != null)
         m_notifier.validate(cxt);

      if (m_history != null)
         m_history.validate(cxt);
   }

   /**
    *
    * Does this application allow login information to be
    *   passed through to the back end?
    *
    * @return      <code>true</code>      passthru enabled
    *             <code>false</code>   passthru disabled
    *
    */
   public boolean isBeLoginPassthruEnabled()
   {
      return m_beLoginPassthru;
   }

   /**
    *
    * Set whether this application allows login information to be
    *   passed through to the back end.
    *
    * @param   enabled      <code>true</code>      passthru enabled
    *                      <code>false</code>   passthru disabled
    *
    */
   public void setBeLoginPassthru(boolean enabled)
   {
      m_beLoginPassthru = enabled;
   }

   /**
    * Sets the lastest revision information in the revision history. <BR>
    * To be used by the objectstore only.
    *
    * @param   agent       Who or what is responsible for the change.
    *
    * @param   description A description of the change.
    */
   public void setRevision(String agent, String description)
   {
      if (m_history == null)
      {
         m_history = new PSRevisionHistory();
      }

      m_history.setRevision(agent, description, null);
      setModified(true);
   }

   /**
    * Clears the revision history of this application. The next entry
    * set after calling this will be version 1.0, and will be considered
    * the original revision of the application.
    *
    * @author   chadloder
    *
    * @version 1.34 1999/07/22
    *
    */
   public void clearRevisionHistory()
   {
      m_history = null;
   }

   /**
    * Retreives the trace debug options from the application.  If there is no
    * PSTraceInfo object in this application, one will be created with the
    * default  values and returned.
    *
    * @return   an object representing the current trace debug options.
    *           This is a reference to the actual object stored in
    *           PSApplication.  Changes to this object are reflected in
    *           PSApplication.
    */
   public PSTraceInfo getTraceInfo()
   {
      //see if we have one
      if (m_traceInfo == null)
         m_traceInfo = new PSTraceInfo();

      return m_traceInfo;
   }
   
   /**
    * Retrieves the application type for this application
    * @return the <code>PSApplicationType</code> or
    * <code>null</code> if not set
    */
   public PSApplicationType getApplicationType()
   {
      return m_appType;
   }
   
   /**
    * Sets the application type of this application object
    * @param appType the <code>PSApplicationType</code>
    * for this application.
    */
   public void setApplicationType(PSApplicationType appType)
   {
      m_appType = appType;
   }

   /**
    * Sets the lastest revision information in the revision history.
    *
    * @author      chadloder
    *
    * @version 1.34 1999/07/22
    *
    *
    * @param entry  PSRevisionEntry entry for the new latest revision;
    *               will not be set if it is not later than the current
    *
    */
   public void setRevision(PSRevisionEntry entry)
   {
      if (m_history == null)
      {
         m_history = new PSRevisionHistory();
      }

      m_history.setRevision(entry);
      setModified(true);
   }

   /**
    * Returns the revision history for this application.
    * @author   chadloder
    *
    * @version 1.30 1999/07/08
    *
    * @return   PSRevisionHistory The revision history for this application,
    *           or <CODE>null</CODE> if no history exists.
    */
   public PSRevisionHistory getRevisionHistory()
   {
      return m_history;
   }

   private static IllegalArgumentException validateVersion(String version)
   {
      if (version == null)
      {
         return new IllegalArgumentException("app version invalid");
      }

      return null;
   }

   private static boolean compare(Object a, Object b)
   {
      if (a == null || b == null)
      {
         if (a != null || b != null)
            return false;
      }
      else if (!a.equals(b))
         return false;

      return true;
   }
   //application Id generated by server
   private   int                     m_id = 0;
   //name of the application. Max 50 chars
   private   String                  m_name = "";
   //short desription of application. Max 255 chars
   private   String                  m_description = "";
   private   boolean                  m_enabled = false;
   private   boolean                  m_active = false;
   private   String                  m_requestRoot = "";
   private  String                  m_defaultRequestPage = "";

   private   PSAcl                     m_acl = null;            //acl object
   private   PSDataEncryptor         m_dataEncryptor = null;
   private   PSLogger                  m_logger = new PSLogger();
   private   PSLoginWebPage            m_loginWebPage = null;
   private   PSErrorWebPages         m_errorWebPages = null;
   private   PSNotifier               m_notifier = null;
   private   java.util.Properties      m_userProperties = null;
   private   PSRevisionHistory         m_history = null;

   private   PSCollection            m_dataSets = null;

   private   PSCollection            m_roles = null;

   private   int                     m_maxThreads = 0;
   private   int                     m_maxRequestTime = 0;
   private   int                     m_maxRequestsInQueue = 0;
   private   boolean                  m_userSessions = false;
   private   int                     m_sessionTimeout = 0;    //in secods

   private   String                  m_htmlParamName = "";
   private   String                  m_requestTypeValueQuery = "";
   private   String                  m_requestTypeValueInsert = "";
   private   String                  m_requestTypeValueUpdate = "";
   private   String                  m_requestTypeValueDelete = "";
   //Save will use this to update data to server.
   //Can be used to bring Save prompts
   private   boolean                  m_modified = false;

   /**
    *  m_beLoginPassthru will always default to false,
    *  it can be overridden by setting backEndloginPassthry to "yes"
    *  any other value will leave this false
    */
   private   boolean                  m_beLoginPassthru = false;
                                      //Passthru logins to back ends?

   /** what version is this application */
   private   String                  m_version = "2.0";

   private   Document                  m_propertyTree = null;

   /**
    * This is a storage slot for use by other parties. The application makes
    * no use of this value. It is the extension context used to tie an app
    * specific extension to a particular app. If this is not null, the server
    * will use it to delete all extensions that are in this context when the
    * application is removed. It does this by getting all handlers, then
    * getting all extensions on each handler that are within this context.
    * Users should set this to a globally unique value and not change it for
    * the life of the app.
    *
    * @see #getExtensionContext
    * @see #setExtensionContext
    */
   private String m_context = null;

   private static final int         APP_MAX_NAME_LEN                  = 50;
   private static final int         APP_MAX_REQ_ROOT_LEN               = 50;
   private static final int         MAX_HTML_REQ_PARAM_NAME_LEN      = 50;
   private static final int         MAX_REQ_TYPE_VALUE_LEN            = 50;

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXApplication";
   
   /**
    * XML Attribute that represent application type
    */
   static final String ATTR_APPLICATION_TYPE = "appType";

   /**
    * used to store this application's trace options
    */
   private PSTraceInfo m_traceInfo = null;

   /*
    * Indicates whether the application is hidden.  Default is
    * <code>false</code> unless otherwise specified in the xml.
    */
   private boolean m_hidden = false;

   /**
    * See {@link #getStartPriority()} for a description.
    */
   private int m_startPriority = 0;
   
   /**
    * Application type property, set by {@link #fromXml(Document)} and
    * {@link #setApplicationType(PSApplicationType)}. Initially
    * <code>null</code> and set in {@link #fromXml(Document)}. If the
    * application does not have the attribute set, then it is guessed using
    * {@link #guessAppType()}.
    */
   private PSApplicationType m_appType = null;

   /**
    * The attribute name used to store the value of the m_startPriority
    * property.
    */
   private static final String STARTPRIORITY_ATTRNAME = "startPriority";
}
