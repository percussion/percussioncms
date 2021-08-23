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

package com.percussion.server;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSDataHandler;
import com.percussion.data.PSQueryHandler;
import com.percussion.data.PSUpdateHandler;
import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSDebugManager;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSCustomError;
import com.percussion.design.objectstore.PSDataEncryptor;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSErrorWebPages;
import com.percussion.design.objectstore.PSLoginWebPage;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.design.objectstore.server.IPSApplicationListener;
import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.error.PSErrorHandler;
import com.percussion.error.PSErrorManager;
import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSLargeApplicationRequestQueueError;
import com.percussion.error.PSResponseSendError;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionListener;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.log.PSLogApplicationStart;
import com.percussion.log.PSLogApplicationStatistics;
import com.percussion.log.PSLogApplicationStop;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogInformation;
import com.percussion.log.PSLogMultipleHandlers;
import com.percussion.security.PSAclHandler;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSDataEncryptionHandler;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSCacheContext;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The PSApplicationHandler class is used to handle requests for a given
 * application.
 * <p>
 * The handler supports multi-threaded use. The server contains
 * one application handler for each application. Many user threads can
 * access the handler simultaneously to process an application specific
 * request.
 * <p>
 * The application handler makes use of the following
 * {@link com.percussion.design.objectstore.PSApplication PSApplication}
 * info:
 * <ul>
 * <li>the application's access control list (ACL)</li>
 * <li>the application's data encryption settings</li>
 * <li>the maximum amount of time to spend servicing a request</li>
 * <li>the request type parameter name/values</li>
 * <li>the log settings for this application (=> PSLogHandler)</li>
 * <li>the user session management info</li>
 * <li>the web page used to login to this application</li>
 * <li>the web pages being return on error for this application
 *     (=> PSErrorHandler)</li>
 * <li>the data sets defined for accessing data through this application</li>
 * <li>the default back-end credentials to use when accessing data</li>
 * <li>the private roles defined for use exclusively by this application</li>
 * <li>the mail notification settings associated with the application</li>
 * <li>the user defined functions (UDFs) associated with the application</li>
 * </ul>
 * For each data set
 * ({@link com.percussion.design.objectstore.PSDataSet PSDataSet}),
 * the following information is also used:
 * <ul>
 * <li>the name of the data set</li>
 * <li>the type of transaction support</li>
 * <li>the data set's data encryption settings</li>
 * <li>the pipes defining the data associated with this data set</li>
 * <li>the page data tank describing the XML document being used for this
 * data set</li>
 * <li>the request definition for this data set (the URL and
 * input parameter settings</li>
 * <li>the definition of the results being generated by this data set</li>
 * <li>the exits which act upon the entire data set</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSApplicationHandler implements IPSRootedHandler
{
   /**
    * Creates an application handler for the specified application. The
    * application handler creates the query and update handlers associated
    * with each data set.
    *
    * @param app The app to create an application handler for. Must not be
    * <CODE>null</CODE>.
    *
    * @param objectStoreHandler The objectstore handler to use. Must not be
    * <CODE>null</CODE>.
    *
    * @param extMgr The extension manager to use. Must not be
    * <CODE>null</CODE>.
    *
    * @throws  IllegalArgumentException If any param is invalid.
    *
    * @throws PSSystemValidationException If an error occurs validating the contents
    * of the data sets.
    */
   public PSApplicationHandler(
      PSApplication app,
      IPSObjectStoreHandler objectStoreHandler,
      IPSExtensionManager extMgr
      )
      throws PSSystemValidationException
   {
      super();

      if (app == null)
         throw new IllegalArgumentException(PSErrorManager.getErrorText(
         IPSServerErrors.NULL_APPLICATION_ERROR));

      if (objectStoreHandler == null)
         throw new IllegalArgumentException("objectStoreHandler cannot be null");

      if (extMgr == null)
         throw new IllegalArgumentException("extMgr cannot be null");

      m_objectStoreHandler = objectStoreHandler;
      m_extMgr = extMgr;

      init(app);
   }

   /**
    * Get the internal request handler associated with the specified request.
    *
    * @param   request  The name of the request to retrieve (the name of
    *   the dataset). Never <code>null</code>.
    *
    * @return The internal request handler associated with this request name,
    *    or <code>null</code> if a handler could not be located.
    *
    * @throws  IllegalArgumentException if the request string is invalid
    */
   public IPSInternalRequestHandler getInternalRequestHandler(String request)
   {
      if (request == null || request.length() == 0)
         throw new IllegalArgumentException("Invalid request name");

      return (IPSInternalRequestHandler) m_dataHandlers.get(
         request.toLowerCase());
   }

   /**
    * Get the application definition for this handler.
    *
    * @return                          the application definition
    */
   public PSApplication getApplicationDefinition()
   {
      return m_application;
   }

   /**
    * Get the data set definition for the data set of the specified name.
    *
    * @param      name                 the name of the data set to locate
    *
    * @return                          the data set definition
    *
    * @throws  PSNotFoundException  if the target data set does not exist
    *                                  in this application
    */
   public PSDataSet getDataSetDefinition(String name)
      throws PSNotFoundException
   {
      if ((name == null) || (name.length() == 0))
      {
         Object[] args = { "", m_name };
         throw new PSNotFoundException(
            IPSServerErrors.APP_DATASET_DEF_NOT_FOUND, args);
      }

      PSCollection dataSets = m_application.getDataSets();
      if (dataSets != null)
      {
         PSDataSet ds;
         for (Object dataSet : dataSets) {
            ds = (PSDataSet) dataSet;
            if (name.equalsIgnoreCase(ds.getName()))
               return ds;
         }
      }

      Object[] args = { name, m_name };
      throw new PSNotFoundException(
         IPSServerErrors.APP_DATASET_NOT_FOUND, args);
   }

   /**
    * Enable data request handling for the specified data set.
    *
    * @param ds the data set to activate, not <code>null</code>.
    * @throws PSSystemValidationException if an error occurs validating the
    *    contents of the supplied data set.
    */
   public synchronized void addDataHandler(PSDataSet ds)
      throws PSSystemValidationException
   {
      if (ds == null)
         throw new IllegalArgumentException("ds cannot be null");
      
      boolean foundOne = false;
      PSPipe dsPipe = ds.getPipe();
      if (dsPipe instanceof PSQueryPipe)
      {
         // create a query handler
         try
         {
            PSQueryHandler qh = new PSQueryHandler(this, ds);
            m_dataHandlers.put(ds.getName().toLowerCase(), qh);

            addMap(ds, qh);
            PSServer.notifyHandlerInitListeners(qh);
            
            foundOne = true;
         }
         catch (PSIllegalArgumentException | PSNotFoundException e)
         {
            throw new PSSystemValidationException(
               e.getErrorCode(), e.getErrorArguments(), m_application, ds);
         } catch (SQLException e)
         {
            Object[] args = { ds.getName(), m_name, e.toString() };
            throw new PSSystemValidationException(
               IPSServerErrors.APP_DATASET_INVALID, args, m_application, ds);
         }
         catch (PSException e)
         {
            throw new PSSystemValidationException(
               e.getErrorCode(), e.getErrorArguments(), m_application, ds);
         }
      }
      else if (dsPipe instanceof PSUpdatePipe)
      {
         // create an update handler
         try
         {
            PSUpdateHandler uh = new PSUpdateHandler(this, ds);
            m_dataHandlers.put(ds.getName().toLowerCase(), uh);

            addMap(ds, uh);
            PSServer.notifyHandlerInitListeners(uh);
            foundOne = true;
         }
         catch (PSException e)
         {
            throw new PSSystemValidationException(
               e.getErrorCode(), e.getErrorArguments(), m_application, ds);
         }
         catch (SQLException e)
         {
            Object[] args = { ds.getName(), m_name, e.toString() };
            throw new PSSystemValidationException(
               IPSServerErrors.APP_DATASET_INVALID, args, m_application, ds);
         }
      }
      else if (ds instanceof PSContentEditor)
      {
         try
         {
            PSContentEditorHandler ceh = new PSContentEditorHandler(this, ds);
            m_dataHandlers.put(ds.getName().toLowerCase(), ceh);

            addMap(ds, ceh);
            PSServer.notifyHandlerInitListeners(ceh);
            foundOne = true;
            
            PSContentEditorPipe pipe = 
               (PSContentEditorPipe) ceh.getContentEditor().getPipe();
            Collection addedSystemMandatoryFields = 
               pipe.getMapper().getAddedSystemMandatoryFields();
            if (!addedSystemMandatoryFields.isEmpty())
            {
               // warn the user that we added system mandatory fields
               Object[] args = { addedSystemMandatoryFields.toString() };
               PSConsole.printMsg("Server", 
                  IPSServerErrors.APP_ADDED_SYSTEM_MANDATORY_FIELDS, args);
            }
         }
         catch (PSException e)
         {
            throw new PSSystemValidationException(
               e.getErrorCode(), e.getErrorArguments(), m_application, ds);
         }
      }

      // if there's no handlers (thus, no pipes) we have a problem!
      if (!foundOne)
      {
         // two data sets of the same name not allowed!!!
         Object[] args = { ds.getName(), m_name };
         throw new PSSystemValidationException(
            IPSServerErrors.EMPTY_DATASET, args, m_application, ds);
      }
   }

   /**
    * Remove data request handling for the specified data set.
    *
    * @param      ds        the data set to deactivate
    */
   public synchronized void removeDataHandler(PSDataSet ds)
   {
      // remove the handler defined for this data set
      IPSRequestHandler rh =
         (IPSRequestHandler)m_dataHandlers.remove(ds.getName().toLowerCase());
      if (rh != null)
      {
         synchronized (m_dataHandlerMap)
         {
            PSRequestor requestor = ds.getRequestor();
            if (requestor != null)
            {
               String reqPage = requestor.getRequestPage();
               PSRequestPageMap[] maps
                  = (PSRequestPageMap[])m_dataHandlerMap.get(reqPage);
               if (maps != null)
               {
                  for (int i = 0; i < maps.length; i++)
                  {
                     if (maps[i].getRequestHandler() == rh)
                     {
                        if (maps.length == 1)
                        {
                           // if this is the only entry, remove it
                           m_dataHandlerMap.remove(reqPage);
                        }
                        else
                        {
                           // otherwise, we need to adjust the array
                           PSRequestPageMap[] newMaps =
                              new PSRequestPageMap[maps.length - 1];
                           System.arraycopy(
                              maps, 0, newMaps, 0, i);
                           System.arraycopy(
                              maps, i+1, newMaps, i, maps.length - i);
                           m_dataHandlerMap.put(reqPage, newMaps);
                        }
                        break;
                     }
                  }
               }
            }
         }

         PSServer.notifyHandlerShutdownListeners(rh);
         rh.shutdown();
      }
   }

   /**
    * Get the id assigned to this application. This is most commonly used
    * when constructing a PSLogError subclass to report an error through
    * the error handler.
    *
    * @return     the application id
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Get the application's request root. This is the url-request root
    * following the server root and preceeding the request page.
    *
    * @return the request root, never <code>null</code> or empty.
    */
   public String getRequestRoot()
   {
      return m_requestRoot;
   }

   // see IPSRootedHandler for documentation
   public String getName()
   {
      return m_name;
   }

   // see IPSRootedHandler for documentation
   public Iterator getRequestRoots()
   {
      List roots = new ArrayList();
      roots.add(m_requestRoot);

      return roots.iterator();
   }

   /**
    * Get the full request root for this application. This includes the
    * server's request root as well as the application's request root.
    *
    * @return     the request root (may be null)
    */
   public String getFullRequestRoot()
   {
      return m_fullRequestRoot;
   }

   /**
    * Get the log handler being used for this application.
    *
    * @return     the log handler
    */
   public PSDebugLogHandler getLogHandler()
   {
      return m_LogHandler;
   }

   /**
    * Get the error handler being used for this application.
    *
    * @return     the error handler
    */
   public PSErrorHandler getErrorHandler()
   {
      return m_errorHandler;
   }

   /**
    * Report an error through the application's error handler.
    *
    * @param   request     the request object to report the error on
    *
    * @param   err         the PSLogError sub-object defining the error
    */
   public void reportError(PSRequest request, PSLogError err)
   {
      PSResponse resp = request.getResponse();
      resp.setIsErrorResponse(true);
      if (m_errorHandler == null)
      { // this should never happen, but...
         m_LogHandler.write(err);

         resp.setStatus(IPSHttpErrors.HTTP_INTERNAL_SERVER_ERROR,
            err.toString());
      }
      else
      {
         m_errorHandler.reportError(resp, err);
      }
   }

   /**
    * Get the statistics object associated with this application.
    *
    * @return     the statistics object
    */
   public PSApplicationStatistics getStatistics()
   {
      return m_stats;
   }

   /**
    * Get the max time to spend servicing a request.
    * @return     the max time to spend servicing a request, in seconds
    */
   public int getMaxRequestTime()
   {
      return m_maxRequestTime;
   }

   /**
    * Get the max number of requests that may be queued at any time.
    * @return     the max number of requests that may be queued at any time
    */
   public int getMaxRequestsInQueue()
   {
      return m_maxRequestsQueued;
   }

   /**
    * Get the max number of user threads we can consume servicing requests.
    * @return     the max number of user threads we can consume
    *             servicing requests
    */
   public int getMaxThreads()
   {
      return m_maxUserThreads;
   }


   /**
    * The request type could not be determined.
    */
   public static final int    REQUEST_TYPE_UNKNOWN          = 0x00;

   /**
    * The request is for the execution of a query.
    */
   public static final int    REQUEST_TYPE_QUERY            = 0x01;

   /**
    * The request is for the execution of an update.
    */
   public static final int    REQUEST_TYPE_UPDATE           = 0x02;

   /**
    * The request is for the execution of an insert.
    */
   public static final int    REQUEST_TYPE_INSERT           = 0x04;

   /**
    * The request is for the execution of a delete.
    */
   public static final int    REQUEST_TYPE_DELETE           = 0x08;


   /**
    * Determine the request type from the information defined in the
    * request. This can only be used for HTML requests. XML requests
    * may contain many types as the tree may contain several objects,
    * each with its own action.
    *
    * @param   request     the request context
    *
    * @return              the appropriate REQUEST_TYPE_xxx flag
    */
   public int getRequestType(PSRequest request)
   {
      String sReqType = null;
      if (m_requestTypeHtmlParamName != null)
         sReqType = request.getParameter(m_requestTypeHtmlParamName);

      if (sReqType != null)
         return getRequestType(sReqType);

      return REQUEST_TYPE_UNKNOWN;
   }

   /**
    * Determine the request type from the specified action string.
    *
    * @param   actionString   the action type string to check
    *
    * @return                 the appropriate REQUEST_TYPE_xxx flag
    */
   public int getRequestType(String actionString)
   {
      int iReqType = REQUEST_TYPE_UNKNOWN;

      if (actionString != null)
      {
         if (actionString.equalsIgnoreCase(m_requestTypeValueDelete))
            iReqType = REQUEST_TYPE_DELETE;
         else if (actionString.equalsIgnoreCase(m_requestTypeValueInsert))
            iReqType = REQUEST_TYPE_INSERT;
         else if (actionString.equalsIgnoreCase(m_requestTypeValueQuery))
            iReqType = REQUEST_TYPE_QUERY;
         else if (actionString.equalsIgnoreCase(m_requestTypeValueUpdate))
            iReqType = REQUEST_TYPE_UPDATE;
      }

      return iReqType;
   }

   /**
    * Convert the URL to use a format which can be acccessed externally.
    * If this is a file URL, it will be converted to the format
    * /ServerRoot/ApplicationRoot/FileName. If this is any other
    * type of URL (eg, HTTP), the name will be left untouched.
    */
   public String getExternalURLString(URL url)
   {
      return getExternalURLString(m_requestRoot, m_fullRequestRoot, url);
   }

   /**
    * Convert the URL to use a format which can be acccessed externally.
    * If this is a file URL, it will be converted to the format
    * /ServerRoot/ApplicationRoot/FileName. If this is any other
    * type of URL (eg, HTTP), the name will be left untouched.
    */
   public static String getExternalURLString(String requestRoot, String fullRequestRoot, URL url)
   {
      if (url == null)
         return null;

      if (!url.getProtocol().equalsIgnoreCase("file"))
         return url.toExternalForm();

      /* get the URL to point to a resource name that can be accessed
       * through the web server. The best way to do this is to build a
       * URL which is /ServerRoot/ApplicationRoot/FileName
       */

      String newURL = url.getFile();
      if (newURL == null)  // some FILE URLs use the file as host ?!
         newURL = url.getHost();

      StringBuilder buf = new StringBuilder(
         newURL.length() + fullRequestRoot.length());

      int pos = newURL.indexOf(fullRequestRoot);
      if (pos != -1)
      {
         /* 1. see if /ServerRoot/ApplicationRoot is in the URL.
          *    if so, we'll strip any chars before the leading /
          */
         if (pos > 0)
            buf.append(newURL.substring(pos));
         else
            buf.append(newURL);
      }
      else if ((pos = newURL.indexOf(fullRequestRoot.substring(1))) != -1)
      {
         /* 2. see if ServerRoot/ApplicationRoot is in the URL.
          *    if so, we'll strip any chars before ServerRoot and add a
          *    leading /
          */
         buf.append('/');
         if (pos > 0)
            buf.append(newURL.substring(pos));
         else
            buf.append(newURL);
      }
      else if ((pos = newURL.indexOf(requestRoot)) != -1)
      {
         /* 3. see if ApplicationRoot is in the URL.
          *    if so, we'll strip any chars before ApplicationRoot and add
          *    /ServerRoot/ to the start
          */
         buf.append(PSServer.makeRequestRoot(newURL.substring(pos)));
      }
      else
      {
         /* 4. we don't allow them to share files across applications,
          *    so we will now tack on /ServerRoot/ApplicationRoot to
          *    the start. This may produce an incorrect URL, but they
          *    shouldn't be using URLs of this form.
          */
         buf.append(fullRequestRoot);
         if (!newURL.startsWith("/"))
            buf.append('/');
         buf.append(newURL);
      }
      String fixedUrl = fixupDots( buf.toString());
      return fixedUrl;
   }

   /**
    * Convert the URL to use a format which can be acccessed from the local
    * system. If this is a file URL,  and does not reference the rhythmyx
    * DTD directory, it will be converted to the format
    * ApplicationRoot/FileName. If this is any other type of URL (eg, HTTP),
    * the name will be left untouched.
    */
   public URL getLocalizedURL(URL url)
      throws java.net.MalformedURLException
   {
      return getLocalizedURL(m_requestRoot, url);
   }

   /**
    * Convert the URL to use a format which can be acccessed from the local
    * system. If this is a file URL and does not reference the rhythmyx
    * DTD directory, it will be converted to the format
    * ApplicationRoot/FileName. If this is any other type of URL (eg, HTTP),
    * the name will be left untouched.
    */
   public static URL getLocalizedURL(String requestRoot, URL url)
      throws java.net.MalformedURLException
   {
      if (url == null)
         return null;

      if (!url.getProtocol().equalsIgnoreCase("file"))
         return url;
         
      /**
       * If the URL points to the Rhythmyx base directory's DTD directory,
       * then return without modification
       */
      File rxDtd = new File(PSServer.getRxDir(),"DTD");
      String rxDtdPath = rxDtd.toURL().getPath();
      String urlPath = url.getPath();
      
      if (urlPath.startsWith(rxDtdPath)) return url;

      /* get the URL to point to a resource name that can be accessed
       * locally. E2 runs from the directory under which all applications
       * exist. Therefore, the best way to do this is to build a
       * URL which is ApplicationRoot/FileName
       */

      String newURL = url.getFile();
      if (newURL == null)  // some FILE URLs use the file as host ?!
         newURL = url.getHost();

      /* make sure their URL does not include .. We don't want them to
       * break security by going outside of the app root
       */
      checkForParentPath(newURL);

      StringBuilder buf = new StringBuilder(
         newURL.length() + requestRoot.length() + "file:".length());
      buf.append("file:");

      /* 1. see if ApplicationRoot is in the URL.
       *    if so, we'll strip any chars before it
       */
      String appRoot = requestRoot;
      if (!appRoot.endsWith("/"))
         appRoot += "/";

      if (newURL.startsWith(appRoot))
      {
         buf.append(newURL);  // we need everything from app root down
      }
      else
      {
         /* 2. we don't allow them to share files across applications, so we
          *    will now tack on ApplicationRoot to the start. This
          *    may produce an incorrect URL, but they shouldn't be using URLs
          *    of this form.
          */
         buf.append(requestRoot);
         if (!newURL.startsWith("/"))
            buf.append('/');
         buf.append(newURL);
      }

      return new URL(buf.toString());
   }

   /* make sure their URL does not include .. We don't want them to
    * break security by going outside of the app root
    */
   private static void checkForParentPath(String url)
      throws java.net.MalformedURLException
   {
      /* make sure their URL does not include .. We don't want them to
       * break security by going outside of the app root
       */
      if (  url.equals("..") ||
         (url.indexOf("../") != -1) ||
         (url.indexOf("/..") != -1) )
      {
         throw new java.net.MalformedURLException(
            "parent traversal is not permitted in paths");
      }
   }

   /**
    * Initialize the request for use with this application handler,
    * tracing any initial request values, if need be.
    *
    * @param request The current request context.
    */
   private void initializeRequestForAppHandler(PSRequest request)
   {
      /* Set the application context info in the request. We have added
       * the log handler to fix bug id's TGIS-4BL4CZ and TGIS-4BL44N
       */
      request.setApplicationHandler(this);
      request.setErrorHandler(m_errorHandler);
      request.setLogHandler(m_LogHandler);

      // now we can log the basic request info
      m_LogHandler.logBasicUserActivity(request);

      // send trace messages for the data we already have
      if (m_LogHandler.isTraceEnabled())
      {
         m_LogHandler.printTrace(
            PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG, request);
         m_LogHandler.printTrace(
            PSTraceMessageFactory.INIT_HTTP_VAR_FLAG, request);
         m_LogHandler.printTrace(
            PSTraceMessageFactory.FILE_INFO_FLAG, request);

         // trace if session enabled and if exists
         if (m_LogHandler.isTraceEnabled(
            PSTraceMessageFactory.SESSION_INFO_FLAG))
         {
            Object[] args = {false, request};
            m_LogHandler.printTrace(PSTraceMessageFactory.SESSION_INFO_FLAG,
               args);
         }
      }
   }

   /**
    * Initialize the internal request's application security.
    *
    * @param   request  The request's context.  Never <code>null</code>.
    *
    * @param   irh   The internal request handler used to process the
    *    current request.  Never <code>null</code>.
    *
    * @throws  PSAuthorizationException   if the user is not authorized
    *    to perform the requested action or access the requested application
    *
    * @throws  PSAuthenticationFailedException  if the user failed to
    *    authenticate or doesn't have the appropriate access
    *
    * @throws  IllegalArgumentException  if any parameter is invalid
    */
   public void initInternalRequestAppSecurity(PSRequest request,
      IPSInternalRequestHandler irh)
      throws PSAuthorizationException, PSAuthenticationFailedException
   {
      initializeRequestForAppHandler(request);

      // this checks if the user's logged in and stores their
      // access level in the request
      getAccessLevelForRequest(request);

      if (irh != null)
      {
         // now let's see if they have access for the request type
         boolean hasAccess = false;
         if (irh instanceof PSUpdateHandler)
         {
            /* The update handler does the job of checking access as
             * we need to go through the data row by row. They may
             * be permitted to perform some actions (eg, update)
             * but not others (eg, delete). In this case, the update
             * handler applies the changes it can, then returns the
             * partial failure. For this reason, we depend upon the
             * update handler to do the right thing for access.
             */
            hasAccess = true;
         }
         else
            hasAccess = hasAccess(request, REQUEST_TYPE_QUERY, false);

         if (!hasAccess)
         {
            throw new PSAuthorizationException(
               getRequestTypeName(getRequestType(request)), request.getRequestPage(),
               request.getUserSessionId());
         }
      }
   }

   // ************ IPSRequestHandler Interface Implementation ************


   /**
    * Process a request for this application. This includes locating the
    * appropriate data set, checking the ACL and running the
    * request through the appopriate data handler.
    * <p>
    * The following steps are performed to process the request:
    * <ol>
    * <li>if the data set is not set in the request, locate the
    * appropriate data set for this request</li>
    * <li>determine the request type from the selection parameters defined
    * in the PSRequestor</li>
    * <li>execute the validations defined on the data set.
    * These can be accessed through the PSRequestor.</li>
    * <li>verify the user has the appropriate access for what they
    * are trying to do</li>
    * <li>get the handler for the specified request type from the hash</li>
    * <li>call the handler if it was found, app error if not</li>
    * </ol>
    *
    * @param   request     the request object containing all context
    *                      data associated with the request
    */
   public void processRequest(PSRequest request)
   {
      initializeRequestForAppHandler(request);

      // and prepare for statistical reporting
      PSRequestStatistics reqStats = request.getStatistics();

      /* ***** NOTE *****
       * DO NOT USE return AS WE NEED TO DECREMENT m_requestsInProcessing
       * ***** NOTE ***** */
      synchronized(this)
      {
         if (m_requestsInProcessing < m_maxUserThreads)
            m_requestsInProcessing++;
         else
         {
         /* using PSLargeApplicationRequestQueueError instead of
          * PSLargeRequestQueueError to fix bug id TGIS-4BL4YZ
          */
            PSLargeApplicationRequestQueueError err
               = new PSLargeApplicationRequestQueueError(
               m_id, request.getUserSessionId(), m_maxUserThreads);

            // format the error message for this (server too busy)
            try
            {
               reportError(request, err);
            }
            catch (Exception e)
            {
               PSConsole.printMsg(
                  m_name, "reportError failed: " + e.toString(), null);
            }
            return;  // return's ok here as we never bumped the count
         }
      }

      // mark that we have a pending event we're working on
      m_stats.incrementPendingEventCount();
      boolean success = false;  // assume error, clear after processing
      try
      {
         if (PSDataEncryptionHandler.checkEncryption(request, m_encryptor))
         {
            // Originally this step was inside "if (rh != null)", but the problem
            // is without getting through ACL, once "getRequestHandler(request)"
            // is called, the program execution is over, which is not what we
            // wanted in testing application GetUserContext
            getAccessLevelForRequest(request);
            
            // call verify community only attempting to override - otherwise
            // exit will handle community verification.  Once we deprecate the
            // exit, verify community should always be called here, even if not
            // overriding
            String commOverride = request.getParameter(
               IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID);
            if (commOverride != null && commOverride.trim().length() > 0)
               PSServer.verifyCommunity(request);

            // trace the session itself now that we've authenticated
            if (m_LogHandler.isTraceEnabled(
               PSTraceMessageFactory.SESSION_INFO_FLAG))
            {
               Object[] args = {true, request};
               m_LogHandler.printTrace(PSTraceMessageFactory.SESSION_INFO_FLAG,
                  args);
            }

            IPSRequestHandler rh = getRequestHandler(request, true );
            if (rh != null)
            {
               // this checks if the user's logged in and stores their
               // access level in the request
               // getAccessLevelForRequest(request);

               // now let's see if they have access for the request type
               boolean hasAccess = false;
               if (rh instanceof com.percussion.data.PSUpdateHandler)
               {
                  /* The update handler does the job of checking access as
                   * we need to go through the data row by row. They may
                   * be permitted to perform some actions (eg, update)
                   * but not others (eg, delete). In this case, the update
                   * handler applies the changes it can, then returns the
                   * partial failure. For this reason, we depend upon the
                   * update handler to do the right thing for access.
                   */
                  hasAccess = true;
               }
               else
                  hasAccess = hasAccess(request, REQUEST_TYPE_QUERY);

               // see if they have access (it logs any problems)
               if (hasAccess)
               {
                  // see if we've got a cached response
                  PSResponse resp = null;
                  PSCacheContext cacheContext = null;
                  PSCachedResponse cachedResp = null;
                  IPSCacheHandler cacheHandler = null;
                  boolean storeResponse = false;
                  if (rh instanceof PSDataHandler)
                  {
                     cacheContext = new PSCacheContext(request, 
                        ((PSDataHandler)rh).getDataSet(), this);
                     cacheHandler = 
                        PSCacheManager.getInstance().getCacheHandler(
                           cacheContext);
                  }

                  // if not null, then this request is cacheable by the handler
                  if (cacheHandler != null)
                  {
                     // if it's cacheable and there is no response cached, clone
                     // the request in case its changed during processRequest()
                     cachedResp = cacheHandler.retrieveResponse(cacheContext);
                     if (cachedResp != null)
                     {
                        resp = request.getResponse();
                        cachedResp.copyTo(resp);
                        reqStats.incrementCacheHits();
                     }
                     else
                     {
                        cacheContext.cloneRequest();
                        storeResponse = true;
                        reqStats.incrementCacheMisses();
                     }
                  }

                  // if no cached response, process the request
                  if (cachedResp == null)
                  {
                     // call the handler now
                     rh.processRequest(request);
                     resp = request.getResponse();
                  }
                  success = true;

                  // if we are to cache the response, get a cacheable copy
                  // before sending, and then store it after we send
                  try
                  {
                     if (!resp.isErrorResponse())
                     {
                        if (storeResponse)
                        {
                           if (cacheHandler.isResponseCacheable(resp))
                              cachedResp = new PSCachedResponse(resp);
                           else
                              storeResponse = false;
                        }

                        // resp.send();

                        // store using the copy of the request made before
                        // processing the request in case request was changed
                        if (storeResponse)
                           cacheHandler.storeResponse(cacheContext, cachedResp);
                     }
                  }
                  catch (IOException e)
                  {
                     PSLogError respErr = new PSResponseSendError(
                        m_id, request.getUserSessionId(),
                        IPSServerErrors.EXCEPTION_NOT_CAUGHT, e.toString());
                     reportError(request, respErr);
                  }
               }  // else => hasAccess reported the error - we're done
            } // else => getRequestHandler reported the error - we're done
         }  // else => checkEncryption reported the error - we're done
      }
      catch (PSAuthenticationRequiredException authreq)
      {
         handleAuthorizationException(request, authreq);
         success = false;
      }
      catch (PSAuthorizationException auth)
      {
         handleAuthorizationException(request, auth);
         success = false;
      }
      catch ( PSAuthenticationFailedException e )
      {
         handleAuthorizationException(request, e);
         success = false;
      }
      catch (Throwable t)
      {
         /* this is the catch all. Format it as an error and send it
          * back as the output.
          */
         try
         {
            PSLogError err = new com.percussion.error.PSUnknownProcessingError(
                  m_id, request.getUserSessionId(),
                  IPSServerErrors.EXCEPTION_NOT_CAUGHT, t.toString());
            reportError(request, err);
         }
         catch (Throwable t2)
         {
            // this is way too many layers of exceptions!!!
            PSConsole.printMsg(
                  m_name, "reportError failed: " + t2.toString(), null);
            PSConsole.printMsg(
                  m_name, "original exception: " + t.toString(), null);
         }
      }

      if ( !success )
      {
         // mark this as a failure for statistical reporting
         reqStats.setFailure();
      }
      reqStats.setCompletionTime(); // mark the event completed

      /* Update the app's statistics, even if stats logging is disabled.
       * Since we maintain performance metrics about each application, this
       * is an important piece of information to track.
       */
      m_stats.update(reqStats);

      // log the detailed request info
      m_LogHandler.logDetailedUserActivity(request);

      // Make sure trace info is committed to disk
      m_LogHandler.syncOutputStream();

      synchronized(this)
      {
         m_requestsInProcessing--;
      }
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      if (!m_active) // are we already disabled?
         return;
      m_active = false;

      PSConsole.printMsg("Server", "Shutting down " + m_name, null);

      // shut down all the data handlers
      shutdownDataHandlers();

      // tell the OS to stop notifying us of changes
      m_objectStoreHandler.removeApplicationListener(m_changeListener);
      m_changeListener = null;
      m_objectStoreHandler = null;

      // and the file handler
      m_fileRequestHandler.shutdown();
      m_fileRequestHandler = null;

      // flush our items from the cache
      PSCacheManager.getInstance().flushApplication(m_application.getName());

      m_stats.setShutdownTime(); // mark that we've shut down

      // shutdown tracing
      m_LogHandler.shutdownTrace();

      // unregister the loghandler from the PSDebugManager
      PSDebugManager.getDebugManager().unregisterLogHandler(m_name);

      logAppStatistics();  // log the app's statistical info
      logAppStopped();     // and log that we're shutting down

      if (m_errorHandler != null)
      {
         m_errorHandler.shutdown();
         m_errorHandler = null;
      }

      // call acl shutdown, currently it handles role cleanup
      //    m_aclHandler.shutdown(m_name);

      // just clear these to drop the reference counts
      m_application = null;
      m_LogHandler = null;
      m_aclHandler = null;
      m_loginPage = null;
      m_encryptor = null;
   }


   /**
    * Calls {@link IPSRequestHandler#shutdown()} method on any datahandler that
    * has been successfully created.
    */
   private void shutdownDataHandlers()
   {
   // because we can now modify the data handlers while an application is
   // still running we synchronize so that we don't fail going through the enumeration
      synchronized (m_dataHandlers)
      {
         // shut down all the data handlers
         for (Enumeration e = m_dataHandlers.elements(); e.hasMoreElements(); )
         {
            IPSRequestHandler handler = (IPSRequestHandler)e.nextElement();
            PSServer.notifyHandlerShutdownListeners(handler);
            handler.shutdown();
         }
         m_dataHandlers.clear();
         m_dataHandlerMap.clear();
     }
   }


   private void logAppStatistics()
   {
      if (!m_LogHandler.isAppStatisticsLoggingEnabled())
         return;

      Map info = new HashMap();

      info.put("elapsedTime",       String.valueOf(m_stats.getProcessingTime()));
      info.put("eventsProcessed",   String.valueOf(m_stats.getSuccessfulEventCount()));
      info.put("eventsPending",     String.valueOf(m_stats.getPendingEventCount()));
      info.put("eventsFailed",      String.valueOf(m_stats.getFailedEventCount()));
      info.put("cacheHits",         String.valueOf(m_stats.getCacheHits()));
      info.put("cacheMisses",       String.valueOf(m_stats.getCacheMisses()));
      info.put("minProcTime",       String.valueOf(m_stats.getMinimumEventTime()));
      info.put("maxProcTime",       String.valueOf(m_stats.getMaximumEventTime()));
      info.put("avgProcTime",       String.valueOf(m_stats.getAverageEventTime()));

      PSLogApplicationStatistics msg = new PSLogApplicationStatistics(m_id, info);
      m_LogHandler.write(msg);
   }

   private void logAppStopped()
   {
      // always log app start/stop
      PSLogApplicationStop msg = new PSLogApplicationStop(m_id, m_name);
      m_LogHandler.write(msg);
   }


   /**
    * Looks for an internal request handler capable of processing the supplied
    * request by first searching the request page names, then searching the
    * dataset names.
    *
    * @param request request to be handled, not <code>null</code>.
    *
    * @return A handler for the specified request, or <code>null</code> if no
    * handler could be found.
    *
    * @throws IllegalArgumentException if <code>request</code> is <code>null
    * </code>.
    */
   public IPSInternalRequestHandler getInternalRequestHandler(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      IPSRequestHandler rh = getRequestHandler( request, false );
      if (rh != null)
         return (IPSInternalRequestHandler) rh;
      else
      {
         // we didn’t find a request page match, try a dataset name match
         return getInternalRequestHandler( request.getRequestPage( false ) );
      }
   }


   /**
    * Determine the request handler capable of processing this request.
    *
    * @param request the request to handle, assumed not <code>null</code>.
    * @param respondWithError determines behavior when no handler is found.
    * If <code>true</code>, responds to the request with an error message.
    * If <code>false</code>, no reponse is generated.
    *
    * @return the appropriate request handler or <code>null</code> if a handler
    * is not found.
    */
   IPSRequestHandler getRequestHandler(PSRequest request,
                                               boolean respondWithError)
   {
      IPSRequestHandler rh = null;
      int requestType = getRequestType(request);
      String dataSetName =
         request.getCgiVariable(IPSCgiVariables.CGI_PS_DATA_SET_NAME);
      String pageName = null;
      PSRequestPageMap[] maps = null;

      int traceFlagApp = PSTraceMessageFactory.APP_HANDLER_PROC_FLAG;

      //trace app handler name and request type
      if (m_LogHandler.isTraceEnabled(traceFlagApp))
      {
         Object[] args = {getName(), "traceAppHandlerProc_handlername"};
         m_LogHandler.printTrace(traceFlagApp, args);
      }

      if (dataSetName != null)
      {
         // get the handler for the data set/request type from the hash
         rh = (IPSRequestHandler)m_dataHandlers.get(dataSetName.toLowerCase());
      }
      else
      {
         pageName = request.getRequestPage(true);

            /* if the data set name is not set in the request, locate the
             * appropriate data set for this request from the request info
             */
         if ((pageName != null) && (pageName.length() > 0))
         {
            maps = (PSRequestPageMap[])m_dataHandlerMap.get(
                  pageName.toLowerCase());
            if (maps == null)
            {
               /* Try the more generic form, for the new flexible engine */
               pageName = pageName.substring(0,pageName.lastIndexOf('.'));
               maps = (PSRequestPageMap[])m_dataHandlerMap.get(
                     pageName.toLowerCase());
            }
         }

         /* if they've made a request based on a known extension,
          * and we didn't find the map, try to find a match without
          * the extension
          */
         if ((maps == null) &&
             (request.getRequestPageType() != PSRequest.PAGE_TYPE_UNKNOWN))
         {
            pageName = request.getRequestPage(false);

            if ((pageName != null) && (pageName.length() > 0))
            {
               maps = (PSRequestPageMap[])m_dataHandlerMap.get(pageName.toLowerCase());
            }
         }

         //trace resource name
         if ((pageName != null) && (pageName.length() > 0) &&
                     m_LogHandler.isTraceEnabled(traceFlagApp))
         {
            Object[] args = {pageName, getName(),
                                 "traceAppHandlerProc_resourcename"};
            m_LogHandler.printTrace(traceFlagApp, args);
         }


         if (maps != null)
         {
            // trace multiple handler message
            if (m_LogHandler.isTraceEnabled(traceFlagApp) && (maps.length > 1))
            {
               Object[] args = {pageName, getName(),
                                 "traceAppHandlerProc_multipleResources"};
               m_LogHandler.printTrace(traceFlagApp, args);
            }

            String dataSetNames = null;
            int matchingHandlers = 0;
            final boolean logMultipleHandlers =
               m_LogHandler.isMultipleHandlerLoggingEnabled();

            for (int i = 0; i < maps.length; i++)
            {
               String dsname = null;
               if (maps[i].isMatch(this, request))
               {
                  if (rh == null)
                     rh = maps[i].getRequestHandler();
                  matchingHandlers++;

                  if (rh instanceof PSDataHandler)
                     dsname = ((PSDataHandler)rh).getDataSetName();
                  else
                  {
                     PSContentEditor ce =
                        ((PSContentEditorHandler)rh).getContentEditor();
                     dsname = ce.getName();
                  }

                  //trace test dataset succeeded
                  if (m_LogHandler.isTraceEnabled(traceFlagApp))
                  {
                     Object[] args = {pageName, dsname,
                        "traceAppHandlerProc_testhandlerSucceed"};
                     m_LogHandler.printTrace(traceFlagApp, args);
                  }

                  if (!logMultipleHandlers)
                     break;

                  if (dataSetNames == null)
                     dataSetNames = dsname;
                  else
                     dataSetNames += ", " + dsname;
               }
               else
               {
                  //trace test dataset failed
                  if (m_LogHandler.isTraceEnabled(traceFlagApp))
                  {
                     if (maps[i].getRequestHandler() instanceof PSDataHandler)
                        dsname = ((PSDataHandler)
                           maps[i].getRequestHandler()).getDataSetName();
                     else
                     {
                        PSContentEditor ce =
                           ((PSContentEditorHandler)
                              maps[i].getRequestHandler()).getContentEditor();
                        dsname = ce.getName();
                     }

                     Object[] args = {pageName, dsname,
                        "traceAppHandlerProc_testhandlerFail"};
                     m_LogHandler.printTrace(traceFlagApp, args);
                  }
               }
            }

            if (matchingHandlers > 1)
            {
               ConcurrentHashMap info = new ConcurrentHashMap();
               info.put(PSLogMultipleHandlers.PROP_SESS_ID,
                  request.getUserSessionId());
               info.put(PSLogMultipleHandlers.PROP_DATASET_NAMES,
                  dataSetNames);
               m_LogHandler.write(
                  new PSLogMultipleHandlers(m_id, info));
            }
         }
      }

      if (null == rh)
      {
         // look to see if no page was specified and if we have a default
         if ((pageName == null) || (pageName.length() == 0))
         {
            if (null != m_defaultPageRedirectHandler)
            {
               rh = m_defaultPageRedirectHandler;

               //trace file used
               if (m_LogHandler.isTraceEnabled(traceFlagApp))
               {
                  String requestFileURL = m_requestRoot + "/" + pageName;
                  Object[] args = {requestFileURL,
                                   "traceAppHandlerProc_defaultPageRedirectUse"};
                  m_LogHandler.printTrace(traceFlagApp, args);
               }
            }
         }
      }

      if (rh == null)
      {    // last resort, is this a file?!
         if (m_fileRequestHandler.isValidFile(request))
         {
            rh = m_fileRequestHandler;

            //trace file used
            if (m_LogHandler.isTraceEnabled(traceFlagApp))
            {
               String requestFileURL = m_requestRoot + "/" +
                                          request.getRequestPage();
               Object[] args = {requestFileURL,
                                 "traceAppHandlerProc_filehandlerUse"};
               m_LogHandler.printTrace(traceFlagApp, args);
            }
         }
         else
         {  // a standalone static page
            if ((maps == null) && (!StringUtils.isEmpty(pageName)))
            {
               String requestFileURL = m_fullRequestRoot + "/" + pageName;
               request.setRequestFileURL(requestFileURL);
               if (m_fileRequestHandler.isValidFile(request))
               {
                  rh = m_fileRequestHandler;
                  if (m_LogHandler.isTraceEnabled(traceFlagApp))
                  {
                     Object[] args = {requestFileURL,
                                       "traceAppHandlerProc_staticfileUse"};
                     m_LogHandler.printTrace(traceFlagApp, args);
                  }
               }
            }
         }
      }
       // if the handler wasn't found, respond to the request with error message
       if (rh == null && respondWithError)
       {
           if (dataSetName != null)
               PSServerLogHandler.handleDataSetHandlerNotFound(request, m_id,
                       m_name, dataSetName, getRequestTypeName(requestType));
           else
               PSServerLogHandler.handleDataSetNotFound(request, m_id, m_name);
       }


       return rh;
   }

   /**
    * Check if the user associated with the specified request has the
    * specified level of access. If the appropriate access level is not
    * held, an error response will be generated for the request.
    *
    * @param   request        the request context
    *
    * @param   requestType    the REQUEST_TYPE_xxx flag specifying the
    *                         type of request to check access for
    *
    * @return                 <code>true</code> if access is permitted
    */
   public boolean hasAccess(PSRequest request, int requestType)
   {
      return hasAccess(request, requestType, true);
   }

   /**
    * Check if the user associated with the specified request has the
    * specified level of access.
    *
    * @param   request        the request context
    *
    * @param   requestType    the REQUEST_TYPE_xxx flag specifying the
    *                         type of request to check access for
    *
    * @param   replyOnError   <code>true</code> to send an authorization
    *                         error to the caller if the appropriate access
    *                         is not held
    *
    * @return                 <code>true</code> if access is permitted
    */
   public boolean hasAccess(PSRequest request, int requestType,
         boolean replyOnError)
   {
   /* verify the user has the appropriate access for what they
    * are trying to do
    */
      int accessLevel = request.getCurrentApplicationAccessLevel();
      int reqiredAcessLevel = 0;

      boolean hasAccess = false;
      switch (requestType)
      {
      case REQUEST_TYPE_QUERY:
         if ((accessLevel & PSAclEntry.AACE_DATA_QUERY) ==
            PSAclEntry.AACE_DATA_QUERY)
            hasAccess = true;
            reqiredAcessLevel = PSAclEntry.AACE_DATA_QUERY;
         break;

      case REQUEST_TYPE_INSERT:
         if ((accessLevel & PSAclEntry.AACE_DATA_CREATE) ==
            PSAclEntry.AACE_DATA_CREATE)
            hasAccess = true;
            reqiredAcessLevel = PSAclEntry.AACE_DATA_CREATE;
         break;

      case REQUEST_TYPE_UPDATE:
         if ((accessLevel & PSAclEntry.AACE_DATA_UPDATE) ==
            PSAclEntry.AACE_DATA_UPDATE)
            hasAccess = true;
            reqiredAcessLevel = PSAclEntry.AACE_DATA_UPDATE;
         break;

      case REQUEST_TYPE_DELETE:
         if ((accessLevel & PSAclEntry.AACE_DATA_DELETE) ==
            PSAclEntry.AACE_DATA_DELETE)
            hasAccess = true;
            reqiredAcessLevel = PSAclEntry.AACE_DATA_DELETE;
         break;
      }

      // trace app security
      if (m_LogHandler.isTraceEnabled(PSTraceMessageFactory.APP_SECURITY_FLAG))
      {
         Object[] args = {new Integer(reqiredAcessLevel),
                           new Integer(accessLevel)};
         m_LogHandler.printTrace(PSTraceMessageFactory.APP_SECURITY_FLAG,
                                  args);
      }

      // if they don't have access, fail the request now
      if (!hasAccess && replyOnError)
      {
         PSAuthorizationException e = new PSAuthorizationException(
            getRequestTypeName(requestType), request.getRequestPage(),
            request.getUserSessionId());
         handleAuthorizationException(request, e);
      }

      return hasAccess;
   }

   /**
    * Get the access level for this user and assign it in the request object.
    *
    * @param      request     the request context
    *
    * @throws  PSAuthorizationException
    *                         if the user does not have access to this
    *                         application
    *
    * @throws  PSAuthenticationRequiredException
    *                         if the user has not logged in to this
    *                         application
    */
   public void getAccessLevelForRequest(PSRequest request)
   throws   PSAuthorizationException,
      PSAuthenticationFailedException,
      PSAuthenticationRequiredException
   {
   /* verify the user has the appropriate access for what they
    * are trying to do
    */
      int accessLevel = 0;

      accessLevel = m_aclHandler.getUserAccessLevel(request);
      request.setCurrentApplicationAccessLevel(accessLevel);
   }

   /**
    * Used by lower level components (like PSQuery/Update/DataHandler) to
    * prepare extensions for use in this application.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If an error occurred in preparing
    * the named extension.
    *
    * @throws PSNotFoundException If the extension does not exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSRequestPreProcessor prepareRequestPreProcessor(
      PSExtensionRef ref
      )
      throws PSExtensionException, PSNotFoundException
   {
      return (IPSRequestPreProcessor)prepareExtension(ref);
   }

   /**
    * Used by lower level components (like PSQuery/Update/DataHandler) to
    * prepare extensions for use in this application.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If an error occurred in preparing
    * the named extension.
    *
    * @throws PSNotFoundException If the extension does not exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSResultDocumentProcessor prepareResultDocumentProcessor(
      PSExtensionRef ref
      )
      throws PSExtensionException, PSNotFoundException
   {
      return (IPSResultDocumentProcessor)prepareExtension(ref);
   }

   /**
    * Used by lower level components (like PSQuery/Update/DataHandler) to
    * prepare extensions for use in this application.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If an error occurred in preparing
    * the named extension.
    *
    * @throws PSNotFoundException If the extension does not exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSUdfProcessor prepareUdfProcessor(PSExtensionRef ref)
      throws PSExtensionException, PSNotFoundException
   {
      return (IPSUdfProcessor)prepareExtension(ref);
   }

   /**
    * Used by lower level components (like PSQuery/Update/DataHandler) to
    * prepare extensions for use in this application.
    *
    * @param ref The extension ref. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If an error occurred in preparing
    * the named extension.
    *
    * @throws PSNotFoundException If the extension does not exist.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public IPSExtension prepareExtension(PSExtensionRef ref)
      throws PSExtensionException, PSNotFoundException
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");

      return m_extMgr.prepareExtension(ref, m_extListener);
   }

   /**
    * Returns <CODE>true</CODE> if and only if the named extension
    * implements the given interface, <CODE>false</CODE> otherwise.
    *
    * @param ref The extension name. Must not be <CODE>null</CODE>.
    *
    * @param interfaceName The fully qualified Java classname of the
    * interface to test for.
    *
    * @return <CODE>true</CODE> iff the referenced extension implements
    * <CODE>interfaceName</CODE>.
    */
   public boolean extensionImplements(PSExtensionRef ref, String interfaceName)
      throws PSExtensionException, PSNotFoundException
   {
      IPSExtensionDef def = m_extMgr.getExtensionDef(ref);
      try
      {
         Class toTest = Class.forName(interfaceName);
         for (Iterator i = def.getInterfaces(); i.hasNext();)
         {
            String iface = (String) (i.next());
            Class clazz = Class.forName(iface);
            if (toTest.isAssignableFrom(clazz))
            {
               return true;
            }
         }
      }
      catch (ClassNotFoundException e)
      {
         //This should not happen as these are predefined Interfaces.
         throw new RuntimeException(e.getLocalizedMessage());
      }
      return false;
   }


   /**
    * Handles authorization errors, sending the appropriate response.
    *
    * @param request The request that is currently being processed.
    *
    * @param e The exception indicating the type of access failure. The error
    * message of this exception is used as part of the error response text
    */
   public void handleAuthorizationException(PSRequest request, PSException e)
   {
      /*
       * User doesn't have proper access. In this case, return status code 500
       * and indicate the user isn't authorized to perform the requested action.
       */
      String[] args = 
         {request.getUserSession().getRealAuthenticatedUserEntry()};
      PSLogError err = new PSAuthenticationError(
         IPSServerErrors.NO_AUTHORIZATION, args);
      reportError(request, err);
   }

   /**
    * Used to obtain the ConcurrentHashMap containing all cached stylesheets used by the
    * app.
    * 
    * @return a ConcurrentHashMap containing stylesheets cached using a URL object that
    * identifies the stylesheet as a key.
    */
   public ConcurrentHashMap<URL, PSCachedStylesheet> getStylesheetCache()
   {
      return m_ssCache;
   }

   /**
    * Send the login page once a PSAuthenticationRequiredException or
    * PSAuthorizationException has been thrown.
    *
    * @param   request        the request context
    *
    * @param   e              the exception that was thrown
    */
   private void sendLoginPage(PSRequest request, PSException e)
   {
      if (m_loginPage == null)
      {
         PSServerLogHandler.handleAccessError(
            request, m_id, null, null, 0, e.getMessage());
      }
      else
      {
         URL loginURL = m_loginPage.getUrl();
         String loginFile = (loginURL == null) ? null : loginURL.getFile();

         // send them a redirect to the login page if they're not
         // using SSL
         if (!m_loginPage.isSecure() ||
            PSDataEncryptionHandler.checkSecureChannel(
            request, 40, loginFile))
         {
            // they don't need SSL, or they're already using SSL
            // so send the contents directly
            PSResponse resp = request.getResponse();
            long fileLength;
            InputStream fileContent = null;

            try
            {
               if (loginFile == null)
               {  // signifies we should use the default login page
                  java.io.StringWriter buf = new java.io.StringWriter();

                  buf.write("<html>\r\n");
                  buf.write("  <head><title>Login</title></head>\r\n");
                  buf.write("  <body>\r\n");
                  buf.write("    <h2>Login</h2>\r\n");
                  buf.write("    <form method=\"POST\" action=\"");
                  buf.write(request.getCgiVariable(IPSCgiVariables.CGI_SCRIPT_NAME));
                  buf.write("\">\r\n");
                  buf.write("      <table border=\"0\">\r\n");
                  buf.write("        <tr><td colspan=\"2\">Please enter your user id and password to access this application.</td></tr>\r\n");
                  buf.write("        <tr><td>User ID:</td><td><input type=\"TEXT\" name=\"loginid\"></td></tr>\r\n");
                  buf.write("        <tr><td>Password:</td><td><input type=\"PASSWORD\" name=\"loginpw\"></td></tr>\r\n");
                  buf.write("        <tr><td><input type=\"SUBMIT\" name=\"Login\" value=\"Login\"></td></tr>\r\n");
                  buf.write("      </table>\r\n");
                  buf.write("    </form>");
                  buf.write("  </body>\r\n");
                  buf.write("</html>");
                  buf.flush();

                  byte[] data = buf.toString().getBytes();
                  fileLength = data.length;
                  fileContent = new ByteArrayInputStream(data);
               }
               else
               {
                  // we will use a few combos for the file name. First we'll see
                  // if it's been specified as /Rhythmix/App/file. If not, we'll
                  // assume it's just file, so pre-pend the app root

                  String serverRoot = PSServer.makeRequestRoot(null);
                  if (loginFile.startsWith(serverRoot))
                     loginFile = loginFile.substring(serverRoot.length()+1);

                  File file = new File(loginFile);
                  if (!file.exists())
                  {  // do we need to root this from the app directory?
                     file = new File(m_requestRoot + File.separatorChar + loginFile);
                  }

                  fileLength = file.length();
                  fileContent = new FileInputStream(file);
               }

               resp.setContent(fileContent, fileLength,
                  IPSMimeContentTypes.MIME_TYPE_TEXT_HTML);
            }
            catch (Exception error)
            {
               Object[] args = { m_name, loginFile, error.toString() };
               PSLogError err = new com.percussion.error.PSApplicationDesignError(
                  m_id, IPSServerErrors.APP_LOGIN_PAGE_EXCEPTION, args,
                  m_loginPage.toXml(PSXmlDocumentBuilder.createXmlDocument()));
               m_LogHandler.write(err);

               // use the default mechanism for the user, the BASIC
               // authentication dialog box
               PSServerLogHandler.handleAccessError(
                  request, m_id, null, null, 0, e.getMessage());
            } finally {
               if (fileContent != null)
               {
                  try
                  { fileContent.close(); }
                  catch (Exception e2)
                  { /* ignore any exceptions at this point */ }
               }
            }
         }
      }
   }

   public String getRequestTypeName(int requestType)
   {
      switch (requestType)
      {
      case REQUEST_TYPE_QUERY:
         return REQUEST_TYPE_QUERY_STRING;

      case REQUEST_TYPE_INSERT:
         return REQUEST_TYPE_INSERT_STRING;

      case REQUEST_TYPE_UPDATE:
         return REQUEST_TYPE_UPDATE_STRING;

      case REQUEST_TYPE_DELETE:
         return REQUEST_TYPE_DELETE_STRING;
      }

      return REQUEST_TYPE_UNKNOWN_STRING;
   }

   /**
    * Initialize an application handler for the supplied application.
    *
    * @param app the app definition for which to initialize an application 
    *    handler, assumed not <code>null</code>.
    * @throws PSSystemValidationException if an error occurs validating the
    *    contents of the supplied application.
    */
   private void init(PSApplication app) throws PSSystemValidationException
   {
      boolean abnormalExit = true;  // log app stopped if we exception

      // store the application object
      m_application  = app;

      // store the application id and name
      m_id = app.getId();
      m_name = app.getName();

      // now start the normal initialization
      m_requestRoot = app.getRequestRoot();
      m_fullRequestRoot = PSServer.makeRequestRoot(m_requestRoot);

      // create the log handler first, so we can log the app start
      PSTraceInfo traceInfo = app.getTraceInfo();
      m_LogHandler = new PSDebugLogHandler(app.getLogger(), traceInfo, app);

      /* add logHandler as listener to PSTraceInfo to be notified
       * of trace start/stop
       */
      traceInfo.addTraceStateListener(m_LogHandler);

      // register the log handler with the PSDebugManager to enable trace from console
      PSDebugManager debugMgr = PSDebugManager.getDebugManager();
      debugMgr.registerLogHandler(m_LogHandler, m_name);

      // we have enough info, log app start/stop now
      PSLogInformation msg = new PSLogApplicationStart(m_id, m_name);
      m_LogHandler.write(msg);

      try
      {
         /* create the error handler. Custom error pages are stored in
          * the application directory, so we need to add the app root
          * to the name to allow the error handler to locate the files
          * appropriately.
          */
         PSErrorWebPages errPages = app.getErrorWebPages();
         int size = (errPages == null) ? 0 : errPages.size();
         for (int i = 0; i < size; i++)
         {
            PSCustomError err = (PSCustomError)errPages.get(i);
            URL url = err.getURL();
            try
            {
               err.setURL(getLocalizedURL(url));
            }
            catch (MalformedURLException e)
            {
               Object[] args = { m_name, err.getErrorCode(),
                  ((url == null) ? "" : url.toExternalForm()), e.toString() };
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CUSTOM_ERROR_URL_INVALID,
                  args, m_application, errPages);
            }
         }
         m_errorHandler = new PSErrorHandler(
            errPages, app.getNotifier(), app.getLogger());

         // create the basic file handler
         m_fileRequestHandler = new PSFileRequestHandler(m_requestRoot);

         String appDefaultRequestPage = app.getDefaultRequestPage();
         if (appDefaultRequestPage != null && appDefaultRequestPage.length() > 0)
         {
            m_defaultPageRedirectHandler = new PSDefaultPageRedirectHandler
                  (m_fullRequestRoot, appDefaultRequestPage);
         }

         // init private roles, the ACL handler, login page and encryption
         initSecurity(app);

         // the maximum amount of time to spend servicing a request
         m_maxRequestTime = app.getMaxRequestTime();

         // the maximum number of requests that may be queued at any time
         m_maxRequestsQueued = app.getMaxRequestsInQueue();
         if (m_maxRequestsQueued < 0)
            m_maxRequestsQueued = Integer.MAX_VALUE;

         // the maximum number of threads the app may consume
         m_maxUserThreads = app.getMaxThreads();
         if (m_maxUserThreads <= 0)
            m_maxUserThreads = Integer.MAX_VALUE;

         // get the request type parameter name/values
         m_requestTypeHtmlParamName = app.getRequestTypeHtmlParamName();
         m_requestTypeValueDelete   = app.getRequestTypeValueDelete();
         m_requestTypeValueInsert   = app.getRequestTypeValueInsert();
         m_requestTypeValueQuery    = app.getRequestTypeValueQuery();
         m_requestTypeValueUpdate   = app.getRequestTypeValueUpdate();

         // init the data sets defined for accessing data through this app
         PSCollection dataSets = app.getDataSets();
         if (dataSets != null)
         {
            for (int i = 0; i < dataSets.size(); i++)
               addDataHandler((PSDataSet)dataSets.get(i));
         }

         m_changeListener = new PSAppHandlerAppChangeListener();
         try
         {
            m_objectStoreHandler.addApplicationListener(m_changeListener);
         }
         catch (PSAuthorizationException e)
         {
            Object[] args = { app.getName() };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               com.percussion.security.IPSSecurityErrors.USER_NOT_AUTHORIZED,
               args, true, "ApplicationHandler"));
         }

         m_active = true;

         abnormalExit = false;   // we're done, don't log app stopped
      }
      catch (RuntimeException e)
      {
         // for unknown exceptions, it's useful to log the stack trace
         Object[] args = { m_name,
            com.percussion.error.PSException.getStackTraceAsString(e) };
         throw new PSSystemValidationException(
            IPSServerErrors.APPLICATION_INIT_EXCEPTION, args, m_application, null);
      }
      finally
      {
         if (abnormalExit)
         {
            // need to shutdown any datahandlers that we've started
            shutdownDataHandlers();

            logAppStopped();
         }
      }
   }

   /**
    * Initialize the security components for this application. This should
    * only be called by the init method.
    *
    * @param      app         the app definition to use
    *
    * @throws PSSystemValidationException
    *                         if an error occurs validating the contents of
    *                         the data sets
    */
   private void initSecurity(PSApplication app)
      throws PSSystemValidationException
   {
   /* initialize application security by:
    *    - start ACL handler
    */

      m_aclHandler = new PSAclHandler(app.getAcl());

      // the application's data encryption settings
      m_encryptor = app.getDataEncryptor();

      // the web page used to login to this application
      m_loginPage = app.getLoginWebPage();
   }

   private void addMap(PSDataSet ds, IPSRequestHandler rh)
      throws PSSystemValidationException
   {
      try
      {
         PSRequestPageMap pageMap = new PSRequestPageMap(
            ds, rh);

         int len = 1;
         PSRequestPageMap[] curMaps = (PSRequestPageMap[])
            m_dataHandlerMap.get(pageMap.getRequestPage().toLowerCase());
         if (curMaps != null)
            len += curMaps.length;

         PSRequestPageMap[] newMaps = new PSRequestPageMap[len];
         if (curMaps != null)
            System.arraycopy(curMaps, 0, newMaps, 0, len-1);
         newMaps[len-1] = pageMap;

         m_dataHandlerMap.put(pageMap.getRequestPage().toLowerCase(), newMaps);
      }
      catch (PSIllegalArgumentException e)
      {
         Object[] args = { ds.getName(), m_name, e.getMessage() };
         throw new PSSystemValidationException(
            IPSServerErrors.APP_DATASET_INVALID, args, m_application, ds);
      }
   }

   private boolean m_active = false;

   private PSApplicationStatistics  m_stats = new PSApplicationStatistics();

   /**
    * the application object we're handling requests for
    */
   private PSApplication m_application;

   /**
    * the unique id assigned to this application
    */
   private int m_id;

   /**
    * the name assigned to this application
    */
   private String m_name;

   /**
    * The request root (path) used for this application. This does not
    * include the server request root.
    */
   private String m_requestRoot;

   /**
    * The full request root (path) used for this application. This includes
    * the server request root.
    */
   private String m_fullRequestRoot;

   /**
    * The data handlers managed by this object. The handlers are stored
    * using dataSetName as the key, and the handler
    * instance as the value.
    */
   private ConcurrentHashMap m_dataHandlers = new ConcurrentHashMap();

   /**
    * A mapping from request page names to data handlers objects. The
    * handlers are stored using the request page name as the key and
    * the handler instance as the value. For instance, the handler for
    * the "Stock" data set may have "stock.xml" as its key.
    */
   private ConcurrentHashMap m_dataHandlerMap = new ConcurrentHashMap();

   /**
    * The log handler for this application.  this handler also handles
    * tracing.
    */
   private PSDebugLogHandler m_LogHandler;

   /**
    * The error handler for this application
    */
   private PSErrorHandler m_errorHandler;

   /**
    * The ACL handler for this application
    */
   private PSAclHandler m_aclHandler;

   private PSFileRequestHandler m_fileRequestHandler;

   /**
    * Holds the reference to the handler that will process redirects to the
    * application's default page.  Will be <code>null</code> if the application
    * has not defined a default page.
    */
   private IPSRequestHandler m_defaultPageRedirectHandler = null;

   private PSLoginWebPage m_loginPage;

   private PSDataEncryptor m_encryptor;

   /**
    * The max time to spend servicing a request.
    */
   private int m_maxRequestTime;

   /**
    * The max number of requests that may be queued at any time/
    */
   private int m_maxRequestsQueued;

   /**
    * The max number of user threads we can consume servicing requests.
    */
   private int m_maxUserThreads;

   /**
    * The number of requests currently being serviced.
    */
   private int m_requestsInProcessing = 0;

   /**
    * Keep track of the object store we were started with so we can
    * tell it to remove our listener at shutdown.
    */
   IPSObjectStoreHandler m_objectStoreHandler = null;
   IPSApplicationListener m_changeListener = null;
   IPSExtensionManager m_extMgr;
   IPSExtensionListener m_extListener = new PSAppExtensionChangeListener();

   private String m_requestTypeHtmlParamName;
   private String m_requestTypeValueDelete;
   private String m_requestTypeValueInsert;
   private String m_requestTypeValueQuery;
   private String m_requestTypeValueUpdate;

   /**
    * Caches PSCachedStylesheet for use in merging.  This reference is used
    * directly by other objects to maintain the cache. It is stored in this
    * class to provide a cache on an app by app basis so that when the
    * application is restarted, the cache is cleared and any new version of
    * underlying stylesheets are then used.
    */
   private ConcurrentHashMap<URL,PSCachedStylesheet> m_ssCache = new ConcurrentHashMap<>();

   private static final String REQUEST_TYPE_UNKNOWN_STRING   = "-unknown-";
   private static final String REQUEST_TYPE_QUERY_STRING     = "query";
   private static final String REQUEST_TYPE_INSERT_STRING    = "insert";
   private static final String REQUEST_TYPE_UPDATE_STRING    = "update";
   private static final String REQUEST_TYPE_DELETE_STRING    = "delete";

   class PSAppExtensionChangeListener implements IPSExtensionListener
   {
      /**
       * Notification that the given extension has been updated in the given
       * manager.
       *
       * @param ref The extension name and handler name. Never <CODE>null</CODE>.
       * @param mgr The extension manager. Never <CODE>null</CODE>.
       */
      public void extensionUpdated(PSExtensionRef ref, IPSExtensionManager mgr)
      {
         if (mgr == m_extMgr)
         {
            shutdownAndRestart();
         }
      }

      /**
       * Notification that the given extension has been removed from the
       * given manager.
       *
       * @param ref The extension name and handler name. Never <CODE>null</CODE>.
       * @param mgr The extension manager. Never <CODE>null</CODE>.
       */
      public void extensionRemoved(PSExtensionRef ref, IPSExtensionManager mgr)
      {
         if (mgr == m_extMgr)
         {
            shutdownAndRestart();
         }
      }

      /**
       * Notification that the given extension has been disabled somehow,
       * without being removed. Depending on the implementation, this event
       * map imply that the extension will not function correctly.
       *
       * @param ref The extension name and handler name. Never <CODE>null</CODE>.
       * @param mgr The extension manager. Never <CODE>null</CODE>.
       */
      public void extensionShutdown(PSExtensionRef ref, IPSExtensionManager mgr)
      {
         shutdownAndRestart();
      }

      /**
       * Shuts down this application and then re-initializes it. This is usually
       * done after an in-use extension has been updated or removed.
       */
      private void shutdownAndRestart()
      {
         // wait for the number of in process requests to go to zero
         int numTries = 0;
         while (numTries++ < 5)
         {
            synchronized(PSApplicationHandler.this)
            {
               if (0 == m_requestsInProcessing)
               {
                  PSApplication app = m_application;
                  IPSObjectStoreHandler osHandler = m_objectStoreHandler;

                  shutdown();

                  m_objectStoreHandler = osHandler;
                  try
                  {
                     init(app);
                  }
                  catch (PSSystemValidationException e)
                  {
                     PSConsole.printMsg("Server", e);
                  }

                  return; // we did it!
               }
               else
               {
                  try
                  {
                     Thread.sleep(1000); // sleep for a second...
                  }
                  catch (InterruptedException e)
                  {
                     PSConsole.printMsg("Server", e);
                     Thread.currentThread().interrupt();
                  }
               }
            }
         }

         // if we get here, it means that we were not able to wait for requests
         // TODO: how do we handle this?
      }

      public void extensionAdded(PSExtensionRef ref, PSExtensionManager manager)
      {
         // Ignore, this is not a global listener
      }
   }

   /**
    * Gets the Acl entries matching the specified criteria.
    *
    * @param type The type of Acl entry to locate.  Must be one of the
    * <code>PSAclEntry.ACE_TYPE_xxx</code> value.
    *
    * @return An Iterator over <code>0</code> or more <code>PSEntry</code>
    * objects.  The specific type of entry will be determined by the type of Acl
    * entry requested.  Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> does not represent a
    * valid type.
    */
   public Iterator getAclEntries(int type)
   {
      // this will validate the type, don't want to duplicate that code here
      return m_aclHandler.getAclEntries(type);
   }

   /**
    * Scans the supplied string and does the following:
    * <ul>
    *   <li>Each '/./' is replaced with '/'</li>
    *   <li>Each '/name/../' is replaced with '/'</li>
    * </ul>
    * Paths that are invalid (e.g. empty path part) are returned w/o
    * modification. Relative paths with leading ../'s are ok. If any invalid
    * paths result, an exception is thrown.
    *
    * @param url A url of the form protocol//server:port/path?query, where
    *    most pieces are optional (the part is optional if leaving it out
    *    results in a valid url).
    *
    * @return A url normalized to point to the same location as the original
    *    url. Never <code>null</code> unless <code>null</code> was passed. If
    *    the 'fixed' url results in an invalid url, the original url is
    *    returned.
    *
    * @todo This should be moved and integrated with PSUrlUtils.
    */
   private static String fixupDots( String url )
   {
      if ( null == url || url.trim().length() == 0 )
         return url;

      // check special cases
      String dotdotPath = "../";
      // check for /../a/b and /a//b/c
      if ( url.startsWith( "/" + dotdotPath)
            || url.indexOf("//../") >= 0 )
      {
         return url;
      }

      boolean done = false;
      String normalizedUrl = url;
      String dotPath = "/./"; // use leading / to prevent matching ../
      while ( !done )
      {
         // check for single dots
         int pos = normalizedUrl.indexOf( dotPath );
         if ( pos >= 0 )
         {
            String pre = "";
            String post = "";
            if ( pos > 0 )
               pre = normalizedUrl.substring( 0, pos+1 );
            if ( pos < normalizedUrl.length() - dotPath.length())
               post = normalizedUrl.substring( pos + dotPath.length());
            normalizedUrl = pre + post;
         }
         else
            done = true;
      }

      int startPos = 1; // skip leading ../
      while ( true )
      {
         int pos = normalizedUrl.indexOf( dotdotPath, startPos );
         if ( pos < 0 )
            break;
         if ( normalizedUrl.charAt( pos-1 ) != '/' )
         {
            // dotdot in path part is invalid, eg a../b
            return url;
         }
         int leadingPos = normalizedUrl.lastIndexOf( '/', pos-2 );
         // replace intermediate dotdot:  /a/b/../c/d
         String pre = "";     // include trailing slash
         String post = "";    // no leading slash
         if ( leadingPos >= 0 )
            pre = normalizedUrl.substring(0, leadingPos + 1 );
         int trailingPos = pos + dotdotPath.length();
         if ( trailingPos < normalizedUrl.length())
            post = normalizedUrl.substring( trailingPos );
         normalizedUrl = pre + post;
      }
      return normalizedUrl;
   }
   

   class PSAppHandlerAppChangeListener implements IPSApplicationListener
   {
      PSAppHandlerAppChangeListener()
      {
         super();
      }

      // ******* IPSApplicationListener Interface Implementation *******

      /**
       * Changes have been made to the application.
       * <P>
       * If the application has been modified, including a rename, the
       * applicationRenamed method will be called first, then the
       * applicationUpdated method. If a rename did not occur,
       * applicationRenamed will not be called.
       *
       * @param   app         the application object
       */
      public void applicationUpdated(PSApplication app)
      {
      }

      /**
       * A new application has been created.
       *
       * @param   app         the application object
       */
      public void applicationCreated(PSApplication app)
      {
      }

      /**
       * The name of the application has been changed.
       * <P>
       * If additional changes have also been made to the application, the
       * applicationRenamed method will be called first, then the
       * applicationUpdated method.
       *
       * @param   app         the application object
       *
       * @param   oldName     the original name of the application
       *
       * @param   newName     the new name of the application
       */
      public void applicationRenamed(
         PSApplication app, String oldName, String newName)
      {
         if (m_name.equalsIgnoreCase(oldName))
            m_name = newName;
      }

      /**
       * The application has been removed from the object store.
       * It is guaranteed that no other information has changed.
       *
       * @param   app         the application object
       */
      public void applicationRemoved(PSApplication app)
      {
      }
   }
}

