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

import com.percussion.data.IPSDataErrors;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSDataHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.data.PSQueryHandler;
import com.percussion.data.PSResultSetXmlConverter;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSCacheContext;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSCachedResultPage;
import com.percussion.util.IOTools;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;


/**
 * This class provides the functionality for making an internal request, and
 * if appropriate, retrieving the results of that request.  Each instance of
 * this class represents a particular resource in an application, and may
 * be used to make a single request to that resource.
 * <p>
 * This class does not implement <code>IPSInternalRequest</code> to reduce the
 * chance that this class is provided to user-space objects (exits).  Those
 * objects will be passed a {@link PSInternalRequestProxy}.
 */
public class PSInternalRequest
{
   /**
    * Constructor for this class.
    *
    * @param req The request object, never <code>null</code>.
    *
    * @param rh The internal request handler, never <code>null</code>.
    *
    * @throws IllegalArgumentException if req or rh is <code>null</code>.
    */
   public PSInternalRequest(PSRequest req, IPSInternalRequestHandler rh)
   {
      if (req == null || rh == null)
         throw new IllegalArgumentException("req or rh may not be null");

      m_request = req;
      m_internalRequestHandler = rh;
   }


   // deprecated (see IPSInternalRequest)
   public void makeRequest()
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      if (m_madeRequest)
         throw new IllegalStateException("This method may only be called once");

      m_execData = makeInternalRequest(m_request, m_internalRequestHandler);
      m_madeRequest = true;
      m_gotResults = false;
   }


   // see IPSInternalRequest
   public void performUpdate()
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException
   {
      PSExecutionData execData = null;
      try
      {
         execData = makeInternalRequest(m_request, m_internalRequestHandler);
      }
      finally
      {
         if (execData != null)
            execData.release();
      }
   }


   // see IPSInternalResultHandler
   public ByteArrayOutputStream getMergedResult()
      throws PSInternalRequestCallException
   {
      if (m_madeRequest)
      {
         throw new IllegalStateException("This method cannot be called if"
               + " makeRequest() has already been called.");
      }
      else
      {
         try
         {
            ByteArrayOutputStream os = null;            
            boolean storeInCache = false;
            PSCacheManager cacheMgr = PSCacheManager.getInstance();
            PSCacheContext cacheCtx = getCacheContext();
            IPSCacheHandler cacheHandler = null;
            PSCachedResultPage data = null;
            if (cacheCtx != null)
            {               
               cacheHandler = cacheMgr.getCacheHandler(
                  cacheCtx);
               // if not null, then this request is cacheable by the handler
               if (cacheHandler != null)
               {
                  data = cacheHandler.retrieveMergedResults(cacheCtx);
                  if (data == null)
                     storeInCache = true;
                  else
                  {
                     m_computedMimeType = data.getMimeType();
                     os = new ByteArrayOutputStream(data.getLength());
                     os.write(data.getResultData());   
                  }
               }               
            }
            
            // if no cached doc, process the request
            if (os == null)
            {
               m_execData =
                  makeInternalRequest(m_request, m_internalRequestHandler);
               m_madeRequest = true; // required by getResultHandler()
               os = getMergedResult(m_execData, getResultHandler());
               m_madeRequest = false;
               if (m_internalRequestHandler instanceof PSDataHandler)
               {
                  PSDataHandler dh = (PSDataHandler) m_internalRequestHandler;
                  PSDataSet ds = dh.getDataSet();
                  PSRequestor requestor = ds.getRequestor();
                  String ext = m_request.getRequestPageExtension();
                  m_computedMimeType = 
                     PSResultSetXmlConverter.getMimeTypeForRequestor(
                        requestor, ext, m_execData);
                  String encoding = requestor.getCharacterEncoding();
                  if (StringUtils.isNotBlank(encoding))
                  {
                     m_computedMimeType += "; charset=" + encoding;
                  }
               }
               else
               {
                  m_computedMimeType = "text/html";
               }
            }
               
            // if storing result in cache, get bytes from stream and store them
            if (storeInCache)
            {
               data = new PSCachedResultPage(m_computedMimeType, os.toByteArray());
               cacheHandler.storeMergedResults(cacheCtx, data);
               os = new ByteArrayOutputStream(data.getLength());
               os.write(data.getResultData());
            }
                      
            return os;
         }
         catch (PSAuthorizationException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION,
               e.getLocalizedMessage() );
         }
         catch (PSAuthenticationFailedException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION,
               e.getLocalizedMessage() );
         } 
         catch (PSCacheException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
               e.getLocalizedMessage());
         } 
         catch (IOException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
               e.getLocalizedMessage());
         }
         finally
         {
            cleanUp();
         }
      }
   }

   // see IPSInternalResultHandler
   public ByteArrayOutputStream getContent()
      throws PSInternalRequestCallException
   {
      if (m_madeRequest)
      {
         throw new IllegalStateException("This method cannot be called if"
               + " makeRequest() has already been called.");
      }
      else
      {
         try
         {
            ByteArrayOutputStream os = null;            
            m_execData =
               m_internalRequestHandler.makeInternalRequest(m_request);
            m_madeRequest = true; // required by getResultHandler()
            PSMimeContentResult mcr = getResultHandler().getMimeContent(m_execData,false);
            if(mcr == null)
            {
               throw new PSInternalRequestCallException(
                  IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
                  "Request returned a SQL NULL" );
            }
            InputStream is = mcr.getContent();
            os = new ByteArrayOutputStream();
            IOTools.copyStream(is,os);
            m_madeRequest = false;

            return os;
         }
         catch (PSAuthorizationException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION,
               e.getLocalizedMessage() );
         }
         catch (PSAuthenticationFailedException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION,
               e.getLocalizedMessage() );
         } 
         catch (IOException e)
         { 
            throw new PSInternalRequestCallException(
            IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
               e.getLocalizedMessage() );
         }
         finally
         {
            cleanUp();
         }
      }
   }

   // see IPSInternalRequest
   public Document getResultDoc() throws PSInternalRequestCallException
   {
      if (m_madeRequest)
      {
         // retain the old behavior of this method (depends on makeRequest)
         IPSInternalResultHandler irh = getResultHandler(); // checks m_gotResults
         Document result = getResultDoc(m_execData, irh);
         m_gotResults = true;
         return result;
      }
      else
      {
         // new behavior that works with content editors (fixes #Rx-02-02-0007)
         try
         {
            Document result = null;
            boolean storeInCache = false;
            PSCacheManager cacheMgr = PSCacheManager.getInstance();
            PSCacheContext cacheCtx = getCacheContext();
            IPSCacheHandler cacheHandler = null;
            if (cacheCtx != null)
            {               
               cacheHandler = cacheMgr.getCacheHandler(
                  cacheCtx);
               // if not null, then this request is cacheable by the handler
               if (cacheHandler != null)
               {                  
                  result = cacheHandler.retrieveDocument(cacheCtx);
                  if (result == null)
                     storeInCache = true;
               }               
            }
            
            // if no cached doc, process the request
            if (result == null)
               result = getResultDocument(m_request, m_internalRequestHandler);
               
            // if storing result in cache, do it now
            if (storeInCache)
            {
               cacheHandler.storeDocument(cacheCtx, result);
            }
            
            return result;
         } 
         catch (PSAuthorizationException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION,
               e.getLocalizedMessage());
         } 
         catch (PSAuthenticationFailedException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHENTICATION_FAILED_EXCEPTION,
               e.getLocalizedMessage());
         } 
         catch (PSCacheException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_CALL_EXCEPTION,
               e.getLocalizedMessage());
         }
      }
   }

   /**
    * Get a cache context for the current request.
    * 
    * @return The context, may be <code>null</code> if caching cannot currently 
    * be performed.
    */
   private PSCacheContext getCacheContext()
   {
      PSCacheContext ctx = null;
      
      PSApplicationHandler appHandler = m_request.getApplicationHandler();
      if (m_internalRequestHandler instanceof PSDataHandler && 
         appHandler != null)
      {              
         ctx = new PSCacheContext(m_request,
            ((PSDataHandler)m_internalRequestHandler).getDataSet(), 
            appHandler);
      }      
      
      return ctx;
   }

   // see IPSInternalRequest
   public ResultSet getResultSet() throws PSInternalRequestCallException
   {
      if (m_madeRequest)
      {
         // retain the old behavior of this method (depends on makeRequest)
         IPSInternalResultHandler irh = getResultHandler(); // checks m_gotResults
         ResultSet result = irh.getResultSet(m_execData);
         m_gotResults = true;
         return result;
      }
      else
      {
         if (m_execData != null) throw new IllegalStateException(
            "Must cleanUp() this request before reusing it.");

         // new behavior that makes its own request
         try
         {
            m_execData = makeInternalRequest(m_request, m_internalRequestHandler);
            m_madeRequest = true; // required by getResultHandler()
            IPSInternalResultHandler irh = getResultHandler();
            ResultSet result = irh.getResultSet( m_execData );
            m_madeRequest = false;
            return result;
         } 
         catch (PSAuthorizationException e)
         {
            throw new PSInternalRequestCallException(
               IPSDataErrors.INTERNAL_REQUEST_AUTHORIZATION_EXCEPTION,
               e.getLocalizedMessage() );
         } 
         catch (PSAuthenticationFailedException e)
         {
            // should never happen, as the internal request is constructed from
            // a valid request
            throw new RuntimeException("Internal request failed to authenticate");
         }

      }
   }


   /**
    * Checks to see if this request was made against a query resource, and if
    * so, will cast the {@link #m_internalRequestHandler} to an
    * IPSInternalResultHandler and return it.  Also checks to be sure this
    * object is in a valid state to return results.
    *
    * @return An internal result handler, never <code>null</code>.
    *
    * @throws IllegalStateException if this is not a query request, if {@link
    * #makeRequest()} has not been called, or if results have already been
    * retrieved.
    */
   private IPSInternalResultHandler getResultHandler()
   {
      if (!(m_internalRequestHandler instanceof IPSInternalResultHandler))
         throw new IllegalStateException("This is not a query request");

      if (!m_madeRequest)
         throw new IllegalStateException("makeRequest must be called first.");

      if (m_gotResults)
         throw new IllegalStateException(
            "results have already been retrieved.");

      IPSInternalResultHandler irh =
         (IPSInternalResultHandler)m_internalRequestHandler;

      return irh;
   }


   // see IPSInternalRequest
   public void cleanUp()
   {
      if (m_execData != null)
      {
         m_execData.release();
         m_execData = null;
      }
   }


   // see IPSInternalRequest
   public IPSRequestContext getRequestContext()
   {
      return new PSRequestContext( m_request );
   }


   /**
    * Gets the request handler instance provided when this object was
    * constructed.
    *
    * @return the request handler, never <code>null</code>.
    */
   public IPSInternalRequestHandler getInternalRequestHandler()
   {
      return m_internalRequestHandler;
   }


   /**
    * Gets the request instance provided when this object was constructed.
    *
    * @return the request, never <code>null</code>.
    */
   public PSRequest getRequest()
   {
      return m_request;
   }

   /**
    * see {@link IPSInternalRequest#getRequestType()} for details.
    */
   public int getRequestType()
   {
      return m_internalRequestHandler.getRequestType();
   }
   
   public boolean isBinary(PSRequest req)
   {
      return m_internalRequestHandler.isBinary(req);
   }


   /**
    * Overriding the finalizer to call {@link #cleanUp()}.
    */
   protected void finalize() throws Throwable
   {
      cleanUp();
      super.finalize();
   }

   /**
    * <pre>
    *    $USER     |               |   $USER
    * ------------>|  rxuser       |-------------------> 
    * (PSRequest)  |-------------->|  (PSRequest)
    *                IntReqHndlr   |
    * </pre>    
    * Need for this: A request comes in as $USER context, but this makes an 
    * internal request such as switching to rxuser and then execute the request 
    * and then switch back to the $USER context. In some cases as in 
    * PSUriResolver, the actual request context was null.
    * A wrapper for the handler's makeInternalRequest swapping the request 
    * info and resetting it back
    * 
    * @param req the request used by the handler's makeInternalRequest()
    * @param rh the handler 
    * @return The data, may be <code>null</code>. 
    * 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    * @throws PSInternalRequestCallException 
    */
   private  PSExecutionData makeInternalRequest(PSRequest req,
         IPSInternalRequestHandler rh) throws PSAuthorizationException,
         PSAuthenticationFailedException, PSInternalRequestCallException
   {
      PSExecutionData data = null;
      try
      {
         PSThreadRequestUtils.changeToInternalRequest(req, true);
         // execute the internal request
         data = rh.makeInternalRequest(req);
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
      return data;
   }
   
   /**
    * A wrapper for PSDataHandler's getResultDocument for swapping the
    * request info and resetting it back.  See 
    * {@link IPSInternalRequestHandler#getResultDocument(PSRequest)} for more
    * info.
    * @param req The request to use, assumed not <code>null</code>.
    * @param rh The result handler, assumed not <code>null</code>.
    * 
    * @return The result of calling the handler's getResultDocument() method.
    *  
    * @throws PSInternalRequestCallException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    */  
   private Document getResultDocument(PSRequest req, 
      IPSInternalRequestHandler rh)
         throws PSInternalRequestCallException, PSAuthorizationException,
            PSAuthenticationFailedException
   {
      Document data = null;
      try
      {
         PSThreadRequestUtils.changeToInternalRequest(req, false);
         // execute the internal request
         data = rh.getResultDocument(req);
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
      return data;
   }
   
   /**
    * A wrapper for 
    * {@link IPSInternalResultHandler#getResultDoc(PSExecutionData)} for 
    * swapping the request info and resetting it back
    * @param data The execution data to use, assumed not <code>null</code>
    * @param rh The result handler to use, assumed not <code>null</code>.
    * 
    * @return The result of calling the handler's getResultDoc() method. 
    * 
    * @throws PSInternalRequestCallException 
    */  
   private Document getResultDoc(PSExecutionData data, 
      IPSInternalResultHandler rh)
         throws PSInternalRequestCallException
   {
      Document resultDoc = null;
      try
      {
         PSThreadRequestUtils.changeToInternalRequest(data.getRequest(), false);

         // get the result
         resultDoc = rh.getResultDoc(data);
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
      return resultDoc;
   }
   
   /**
    * Ask the internal request handler what the mime type is of the request
    * @return the mime type, could possibly be <code>null</code> or empty.
    */
   public String computeMimeType()
   {
      return m_computedMimeType;
   }
   
   /**
    * A wrapper for 
    * {@link IPSInternalResultHandler#getMergedResult(PSExecutionData)} for 
    * swapping the request info and resetting it back
    * @param data The execution data to use, assumed not <code>null</code>
    * @param rh The result handler to use, assumed not <code>null</code>.
    * 
    * @return The result of calling the handler's getMergedResult() method. 
    * 
    * @throws PSInternalRequestCallException 
    * @throws PSAuthenticationFailedException 
    * @throws PSAuthorizationException 
    */  
   private ByteArrayOutputStream getMergedResult(PSExecutionData data, 
      IPSInternalResultHandler rh)
         throws PSInternalRequestCallException, PSAuthorizationException, 
         PSAuthenticationFailedException
   {
      ByteArrayOutputStream result = null;
      try
      {
         PSThreadRequestUtils.changeToInternalRequest(data.getRequest(), false);
        
         // get the result
         if(rh instanceof PSQueryHandler)
         {
            result = ((PSQueryHandler)rh).getMergedResult(data, true);   
         }
         else
         {
            result = rh.getMergedResult(data);
         }
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
      
      return result;
   }      
   
   // A wrapper for PSDataHandler for swapping the user context and resetting
   // it back
   public Object getDataSet(PSRequest req)
   {
      Object data = null;
      try
      {
         
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      finally
      {
      }
      return data;
   }
   
   /**
    * The request object used to make the internal request.  Initialized by the
    * ctor, never <code>null</code> after that.
    */
   private PSRequest m_request = null;


   /**
    * The internal request handler used to delegate calls to the server.  Set
    * by the ctor, never <code>null</code> after that.
    */
   private IPSInternalRequestHandler m_internalRequestHandler;


   /**
    * The execution data returned from the internal request call. Used to
    * retrieve results if this is a query request.  Is instantiated by a call
    * to {@link #makeRequest()}, and is set to <code>null</code> by a call to
    * {@link #cleanUp()}
    */
   private PSExecutionData m_execData = null;

   /**
    * Initially <code>false</code>, set to <code>true</code> once a call to
    * {@link #makeRequest()} has been made.
    */
   private boolean m_madeRequest = false;

   /**
    * Initially <code>false</code>, set to <code>true</code> once a call to
    * {@link #getResultDoc()} or {@link #getResultSet()} has been made.
    */
   private boolean m_gotResults = false;
   
   /**
    * A mime type computed in the request. Initialized only when using the 
    * merged result methods.
    */
   private String m_computedMimeType = null;
   
   /**
    * Allows for temporary replacement of the request stored in the
    * {@link PSRequestInfo} while making an internal request.  Call 
    * {@link #replaceRequestInfo()} before making an actual internal request,
    * then call {@link #reset()} when completed (usually in a finally block).
    */
   private class PSInternalRequestInfo
   {
      /**
       * Construct this object.
       * 
       * @param req The request to use, assumed not <code>null</code>. 
       */
      private PSInternalRequestInfo(PSRequest req)
      {
         if (req == null)
            throw new IllegalArgumentException("req may not be null");
         
         mi_req = req;
      }
      
      /**
       * Replace the request stored in the {@link PSRequestInfo} before making
       * an internal request.
       */
      public void replaceRequestInfo()
      {
         if (mi_actualReq != null || mi_isActualNull)
         {
            // this method has already been called, don't yet implement true
            // stack behavior
            throw new IllegalStateException("must call reset() first");
         }
         
         mi_actualReq = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         if ( PSRequestInfo.isInited())
         {
            if ( mi_actualReq == null )
               mi_isActualNull = true;
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, 
               mi_req);
         }
      }
      
      /**
       * Reset the request in {@link PSRequestInfo} to its original request.
       */
      public void reset()
      {
         if ( PSRequestInfo.isInited())
         {
            if (!isReplaced())
               throw new IllegalStateException(
                  "Must call replaceRequestInfo() first");
            
            if (mi_isActualNull)
               PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, null);
            else
               PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST,
                     mi_actualReq);
         }
         
         mi_actualReq = null;
         mi_isActualNull = false;
      }
      
      /**
       * Determines if {@link #replaceRequestInfo()} has been called.
       * 
       * @return <code>true</code> if it is replaced, <code>false</code> if not.
       */
      private boolean isReplaced()
      {
         return (mi_actualReq != null || mi_isActualNull);
      }
      
      private PSRequest mi_req = null;
      private PSRequest mi_actualReq = null;
      private boolean mi_isActualNull = false;
      
   }
}
