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

package com.percussion.deployer.client;

import com.percussion.HTTPClient.Codecs;
import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.HTTPClient.HttpOutputStream;
import com.percussion.HTTPClient.NVPair;
import com.percussion.HTTPClient.ProtocolNotSuppException;
import com.percussion.conn.PSServerException;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.error.PSDeployNonUniqueException;
import com.percussion.deployer.error.PSLockedException;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDeploymentServerConnectionInfo;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSServerLockException;
import com.percussion.server.job.PSJobException;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSCryptographer;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


/**
 * Represents a connection to the Rx server and a session theirin.  Is used
 * to execute requests to the deployment and job handlers on the server.
 */
public class PSDeploymentServerConnection
{
   /**
    * Constructs a connection using the http protocol. Calls
    * {@link #PSDeploymentServerConnection(String, String, int, String, String,
    * boolean, boolean) this("http", info.getServer(), info.getPort(),
    * info.getUserid(), info .getPassword(), info.isPwdEncrypted(),
    * overrideLock)}
    * 
    * @param info contains the connection details, assumed not <code>null</code>
    * @param overrideLock If <code>true</code>, then the lock is acquired by
    *           the user no matter what, if <code>false</code> then sessionid
    *           has to match for lock acquisition.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthorizationException If the user is not authorized to access
    *            the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    *            server.
    * @throws PSDeployException For any other errors.
    */
   public PSDeploymentServerConnection(PSDeploymentServerConnectionInfo info,
         boolean overrideLock) throws PSAuthenticationFailedException,
         PSAuthorizationException, PSServerException, PSDeployException {
      this("http", info.getServer(), info.getPort(), info.getUserid(), info
            .getPassword(), info.isPwdEncrypted(), overrideLock);
   }
   
   /**
    * Construct a connection using the http protocol. Calls
    * {@link #PSDeploymentServerConnection(String, String, int, String, String,
    * boolean, boolean) this("http", server, port, userid, password,
    * isPwdEncrypted, overrideLock)}
    * 
    * @param server
    * @param port
    * @param userid
    * @param password
    * @param isPwdEncrypted
    * @param overrideLock
    * @throws PSAuthenticationFailedException
    * @throws PSAuthorizationException
    * @throws PSServerException
    * @throws PSDeployException
    */
   public PSDeploymentServerConnection(String server, int port,
      String userid, String password, boolean isPwdEncrypted,
      boolean overrideLock) throws
         PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException
   {
      this("http", server, port, userid, password, isPwdEncrypted,
         overrideLock);
   }

   /**
    * Creates a connection to the server.  Use {@link #disconnect()} to
    * disconnect from the server.  This connection may not be used again after
    * it has been disconnected. A daemon locker thread is started which renews
    * the lock 2 minutes before the lock expiration {@link IPSDeployConstants#
    * LOCK_EXPIRATION_DURATION}. If the lock cannot be extended locker thread
    * dies.
    *
    * @param protocol The protocol to use, may not be <code>null</code> or
    * empty. Currently only 'http' and 'https' are supported.
    * @param server The name of the server to connect to, may not be
    * <code>null</code> or empty.
    * @param port The port on the server.  Must be greater than 0.
    * @param userid The user id to connect using, may not be <code>null</code>
    * or empty.
    * @param password The password, may be <code>null</code> or empty.  If
    * <code>null</code>, and empty <code>String</code> is stored.
    * @param isPwdEncrypted If <code>true</code>, the password will be
    * treated as  encrypted.  Otherwise, it is assumed to be clear text and will
    * be encryted for storage or serialization to the server.
    * @param overrideLock If <code>true</code>, then the lock is acquired by the
    * user no matter what, if <code>false</code> then sessionid has to match for
    * lock acquisition.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException If the user cannot be 
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with
    * the server.
    * @throws PSDeployException For any other errors.
    */
   public PSDeploymentServerConnection(String protocol, String server, int port,
      String userid, String password, boolean isPwdEncrypted,
      boolean overrideLock) throws
         PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException
   {
      if (protocol == null || protocol.trim().length() == 0)
         throw new IllegalArgumentException(
            "protocol may not be null or empty");

      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");

      if (port <= 0)
         throw new IllegalArgumentException("port must be greater than zero");

      if (userid == null || userid.trim().length() == 0)
         throw new IllegalArgumentException("userid may not be null or empty");

      m_server = server;
      m_port  = port;
      m_uid = userid;
      m_password = password == null ? "" : password;
      if (!isPwdEncrypted)
         m_password = encryptPwd(m_uid, m_password);

      // create the connection object
      try
      {
         m_conn = new HTTPConnection(protocol, m_server, m_port);
         m_conn.setContext(this);  // associate cookies with this instance
         m_conn.setAllowUserInteraction(false);
         m_conn.setTimeout(0);  // no timeout
         m_conn.addBasicAuthorization("", m_uid, getPassword(false));
      }
      catch (ProtocolNotSuppException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }

      m_isConnected = true;
      Document respDoc = connect(overrideLock);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
      Element root = respDoc.getDocumentElement();
      tree.setCurrent(root);

      String deployVersion = root.getAttribute("deployVersion");
      int deployInterface = -1;
      try
      {
         deployInterface = Integer.parseInt(deployVersion);
      }
      catch (NumberFormatException e)
      {
         m_isConnected = false;
         Object[] args = {"connect", "deployVersion", deployVersion};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }

      // The client's deployment version must be greater than or equal to the
      // server's.  This allows forward compatiblity to be handled by the
      // server.
	  
	  //This will have to be reworked once we allow remote install of the package manager for cougar.
	  // It will need to know the difference of cougar and rhythmyx
      //if (deployInterface >= DEPLOYMENT_INTERFACE_VERSION)
      //{
      //   m_isConnected = false;
      //   throw new PSDeployException(IPSDeploymentErrors.SERVER_VERSION_INVALID,
      //      m_version.getVersionString());
      //}
      
      String licensed = root.getAttribute("licensed");
      if ((licensed != null) && (licensed.trim().length() > 0))
         m_bLicensed = "yes".equalsIgnoreCase(licensed.trim());

      Element versionEl = tree.getNextElement(PSFormatVersion.NODE_TYPE,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (versionEl == null)
      {
         m_isConnected = false;
         Object[] args = {"connect", PSFormatVersion.NODE_TYPE};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
      }
      m_version = PSFormatVersion.createFromXml(versionEl);
      if (m_version == null)
      {
         m_isConnected = false;
         Object[] args = {"connect", PSFormatVersion.NODE_TYPE, "unknown"};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }
      //This will have to be reworked once we allow remote install of the package manager for cougar.
	  // It will need to know the difference of cougar and rhythmyx
      // currently versions prior to 6.0 are not supported
      //if (m_version.getMajorVersion() < 6)
      //{
      //   m_isConnected = false;
      //   throw new PSDeployException(IPSDeploymentErrors.SERVER_VERSION_INVALID,
      //      m_version.getVersionString());
      //}      

      Element repositoryEl = tree.getNextElement(PSDbmsInfo.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (repositoryEl == null)
      {
         m_isConnected = false;
         Object[] args = {"connect", PSDbmsInfo.XML_NODE_NAME};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
      }
      try
      {
         m_serverRepositoryInfo = new PSDbmsInfo(repositoryEl);
      }
      catch (PSUnknownNodeTypeException e)
      {
         m_isConnected = false;
         Object[] args = {"connect", PSDbmsInfo.XML_NODE_NAME,
               e.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }

      //locker thread tries to extend the lock 2 minutes before expiration.
      final int lockTimeoutBuffer = 2;
      Runnable runnable = new Runnable()
      {
         public void run()
         {
            try
            {
               while(m_isConnected)
               {
                  //sleep for lock expiration time less 2 minutes
                  Thread.sleep(((IPSDeployConstants.LOCK_EXPIRATION_DURATION
                     - lockTimeoutBuffer)*60*1000));
                  extendLock();
               }
            }
            catch (InterruptedException e)
            {/*do nothing*/}
            catch(PSDeployException e)
            {/*do nothing*/}
         }
      };
      //Don't even bother to start if locking time is less than 2 minutes.
      if (IPSDeployConstants.LOCK_EXPIRATION_DURATION > lockTimeoutBuffer)
      {
         m_lockerThread = new Thread(runnable);
         m_lockerThread.setDaemon(true);
         m_lockerThread.start();
      }
   }

   /**
    * Flags this connection as disconnected and it is no longer usable.  Any
    * session information is discarded. Kills the locker thread started in the
    * ctor.
    *
    * @throws PSDeployException if errors occur executing the request or if
    * disconnection is being made when the connection has already been lost.
    */
   public void disconnect() throws PSDeployException
   {
      try
      {
         doDisconnect();
      }
      catch (PSDeployException e)
      {
         Object[] args = {m_server + ":" + m_port, e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.LOCK_NOT_RELEASED,
            args);
      }
      finally
      {
         if (m_lockerThread != null)
            m_lockerThread.interrupt();
         m_isConnected = false;
         m_sessionId = "";
      }

   }

   /**
    * Sends a disconnect request to the server so the user's lock may be
    * released if currently held.
    *
    * @throws PSDeployException If there are any errors.
    */
   private void doDisconnect() throws PSDeployException
   {
      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
               "PSXDeployDisconnectRequest");
         root.setAttribute("userId", m_uid);
         root.setAttribute("password", m_password);
         execute(DEPLOY_REQUEST + "disconnect", reqDoc);
      }
      catch(PSAuthorizationException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSServerException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSAuthenticationFailedException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSServerLockException e)
      {
         throw new PSDeployException(e);
      }

   }

   /**
    * Requests lock extension for the locked user.
    *
    * @throws PSDeployException if errors occur executing the request.
    */
   public void extendLock() throws PSDeployException
   {
      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(reqDoc,"PSXDeployExtendLockRequest");
         execute(DEPLOY_REQUEST + "extendlock", reqDoc);
      }
      catch(PSAuthorizationException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSServerException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSAuthenticationFailedException e)
      {
         throw new PSDeployException(e);
      }
      catch(PSServerLockException e)
      {
         throw new PSDeployException(e);
      }
   }

   /**
    * Executes the specified request against the server.
    * Convenience version that calls
    * {@link #execute(String, Document, Map) execute(type, req, null)}
    */
   public Document execute(String type, Document req)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException, PSServerLockException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (!type.startsWith(DEPLOY_REQUEST) && !type.startsWith(JOB_REQUEST))
         throw new IllegalArgumentException("invalid type");

      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      return execute(type, req, null);
   }

   /**
    * Executes the specified request against the server.
    * 
    * @param type Specifies the request type. Must be a type expected by the
    * handler specified by the type prefix. May not be <code>null</code> or
    * empty and must begin with one of the supported handler prefixes. Currently
    * supported handler prefixes are <code>"deploy-"</code> and
    * <code>"job-"</code>.
    * 
    * @param req The body of the request, the format of which is defined by the
    * handler and request type. May not be <code>null</code>.
    * 
    * @param params Map of http params to include in the request. Key is the
    * param name as a <code>String</code>, value is the param value as a
    * <code>String</code>. May be <code>null</code> or empty, may not
    * contain an entry with a <code>null</code> key.
    * 
    * @return The response document from the request, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException If the user cannot be
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    * server.
    * @throws PSServerLockException if a required server lock cannot be
    * obtained.
    * @throws PSDeployException if the connection is not valid or any other
    * errors occur executing the request.
    */
   public Document execute(String type, Document req, Map params)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException, PSServerLockException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      if (!m_isConnected)
      {
         throw new PSDeployException(IPSDeploymentErrors.NOT_CONNECTED_ERROR,
            m_server);
      }
      return execute(type, req, params, true);
   }

   /**
    * Executes the specified request against the server.  Private version of
    * {@link #execute(String, Document, Map)} with an additional
    * <code>reconnect</code> parameter described below.
    *
    * @param reconnect If <code>true</code>, an attempt is made to reconnect and
    * re-execute the request if an exception is returned by the server that
    * could indicate that the user's session has timed out.  If
    * <code>false</code>, no attempt to reconnect and re-execute is made.
    */
   private Document execute(String type, Document req, Map params,
      boolean reconnect)
         throws PSAuthenticationFailedException, PSAuthorizationException,
            PSServerException, PSDeployException, PSServerLockException
   {
      String requestPage = getRequestPage(type);

      // add session id if we have one
      Element root = req.getDocumentElement();
      root.setAttribute("sessionId", m_sessionId);

      // make request
      byte[] respData = null;
      Document respDoc = null;
      int status = -1;
      PSPurgableTempFile reqFile = null;
      byte[] data = null;
      HTTPResponse resp = null; 
      // add reqtype header after first header (will get set with
      // content-type header automatically by the formDataEncode call)
      NVPair[] hdrs = new NVPair[2];
      try
      {
         // add the params
         NVPair[] opts = getParams(params);

         // add the request doc as an attachment
         reqFile = createAttachmentFile(req);

         NVPair[] file = new NVPair[1];
         file[0] = new NVPair(reqFile.getName(), reqFile.getPath());
         
         hdrs[1] = new NVPair(IPSCgiVariables.CGI_PS_REQUEST_TYPE, type);
         data = Codecs.mpFormDataEncode(opts, file, hdrs);

         // send the request to the Rx server        
         synchronized(m_mutexObject)
         {
            resp = m_conn.Post(requestPage, data, hdrs);
         }
         // get the response code
         status = resp.getStatusCode();
         respData = resp.getData();
      }
      catch ( IOException ioe)
      {
         System.out.println("3. IOException occurred, rePOSTing: " + 
            ioe.getLocalizedMessage());
         try
         {
            resp = m_conn.Post(requestPage, data, hdrs);
            status = resp.getStatusCode();
            respData = resp.getData();
         }
         catch (Exception e)
         {
            ms_log.error(e);
            if (data != null && ms_log.isDebugEnabled())
            {
               try
               {
                  // log the request data to make debugging easier
                  ms_log.debug(getParams(params));
                  String request = new String(data, "ISO-8859-1");
                  ms_log.debug(request);
               }
               catch (UnsupportedEncodingException uee)
               {
                  ms_log.error(uee);
               }
            }
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }        
      }
      catch (Exception e)
      {
         ms_log.error(e);
         if (data != null && ms_log.isDebugEnabled())
         {
            try
            {
               // log the request data to make debugging easier
               ms_log.debug(getParams(params));
               String request = new String(data, "ISO-8859-1");
               ms_log.debug(request);
            }
            catch (UnsupportedEncodingException uee)
            {
               ms_log.error(uee);
            }
         }

         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (reqFile != null)
            reqFile.release();
      }

      // handle error response
      try
      {
         respDoc = parseServerResponse(type, status, respData);
      }
      catch (PSAuthorizationException e)
      {
         if (reconnect && m_sessionId.trim().length() > 0)
         {
            reconnect();
            respDoc = execute(type, req, params, false);
         }
         else
            throw e;
      }
      catch (PSLockedException e)
      {
         if (reconnect && m_sessionId.trim().length() > 0)
         {
            reconnect();
            respDoc = execute(type, req, params, false);
         }
         else
            throw e;
      }

      return respDoc;
   }


   /**
    * Executes the request, gets the body from the supplied input stream, not
    * the request.
    * 
    * @param type Specifies the request type. Must be a type expected by the
    * handler specified by the type prefix. May not be <code>null</code> or
    * empty and must begin with one of the supported handler prefixes. Currently
    * supported handler prefixes are <code>"deploy-"</code> and
    * <code>"job-"</code>.
    * @param params Map of http params to include in the request. Key is the
    * param name as a <code>String</code>, value is the param value as a
    * <code>String</code>. May be <code>null</code>, may not contain an
    * entry with a <code>null</code> key.
    * @param body The file to post. May not be <code>null</code> and must
    * reference an existing file. Data will be sent to the server as an
    * attachment using the file name minus the extension as the parameter name.
    * @param controller The file job controller to set the input stream on
    * before copying the file. May not be <code>null</code>.
    * 
    * @return The response document from the request, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException If the user cannot be
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    * server.
    * @throws PSServerLockException if a required server lock cannot be
    * obtained.
    * @throws PSDeployException if the connection is not valid or any other
    * errors occur executing the request.
    */
   public Document execute(String type, Map params, File body,
      PSDeployFileJobControl controller)
         throws PSAuthenticationFailedException, PSAuthorizationException,
            PSServerException, PSDeployException, PSServerLockException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      if (body == null || !body.exists())
         throw new IllegalArgumentException(
            "body may not be null and must exist");

      if (controller == null)
         throw new IllegalArgumentException("controller may not be null");

      if (!m_isConnected)
      {
         throw new PSDeployException(IPSDeploymentErrors.NOT_CONNECTED_ERROR,
            m_server);
      }

      return execute(type, params, body, controller, true, true);
   }

   /**
    * Executes the specified request against the server.  Private version of
    * {@link #execute(String, Document, OutputStream, PSDeployFileJobControl)} 
    * with an additional <code>reconnect</code> parameter described below.
    *
    * @param reconnect If <code>true</code>, an attempt is made to reconnect and
    * re-execute the request if an exception is returned by the server that
    * could indicate that the user's session has timed out.  If
    * <code>false</code>, no attempt to reconnect and re-execute is made.
    * @param repost If <code>true</code>, an attempt is made to re-execute the 
    * request if an IO exception is returned by the server.  If
    * <code>false</code>, no attempt to re-execute is made.
    */
   private Document execute(String type, Map params, File body,
      PSDeployFileJobControl controller, boolean reconnect, boolean repost)
         throws PSAuthenticationFailedException, PSAuthorizationException,
            PSServerException, PSDeployException, PSServerLockException
   {
      String requestPage = getRequestPage(type);

      // make request
      byte[] respData = null;
      Document respDoc = null;
      HttpOutputStream out = null;
      int status = -1;
      // add reqtype header after first header (will get set with
      // content-type header automatically by the formDataEncode call)
      NVPair[] hdrs = new NVPair[3];

      HTTPResponse resp = null;
      boolean doRepost = false;

      try
      {
         // add the params
         NVPair[] opts = getParams(params);

         // Read in file and create multipart form
         NVPair[] file = new NVPair[1];
         file[0] = new NVPair(body.getName(), body.getPath());

         hdrs[1] = new NVPair(IPSCgiVariables.CGI_PS_REQUEST_TYPE, type);
         byte[] data = Codecs.mpFormDataEncode(opts, file, hdrs);

         // keep alive connection header
         hdrs[2] = new NVPair("Connection", "Keep-Alive");
         // send the request to the Rx server
         out = new HttpOutputStream(data.length);

         synchronized(m_mutexObject)
         {
            resp= m_conn.Post(requestPage, out, hdrs);
         }
         ByteArrayInputStream bIn = new ByteArrayInputStream(data);
         PSInputStreamCounter counter = new PSInputStreamCounter(bIn);
         controller.setStream(counter, data.length);

         // copy the data
         copyStream(counter, out, controller);
         // get the response code
         status = resp.getStatusCode();
         respData = resp.getData();
      }
      catch ( IOException ioe)
      {
         if (repost)
         {
            System.out.println("6. IOException occurred rePOSTing: " + 
               ioe.getLocalizedMessage());
            doRepost = true;
         }
         else
         {
            ms_log.error(ioe);
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               ioe.getLocalizedMessage());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try{ out.close();} catch(IOException e){}
      }
      
      if (repost)
      {
         return execute(type, params, body, controller, reconnect, false);
      }

      // handle error response
      try
      {
         respDoc = parseServerResponse(type, status, respData);
      }
      catch (PSAuthorizationException e)
      {
         //String type, Map params, File body, PSDeployFileJobControl controller
         if (reconnect && m_sessionId.trim().length() > 0)
         {
            reconnect();
            respDoc = execute(type, params, body, controller, false, repost);
         }
         else
            throw e;
      }
      catch (PSLockedException e)
      {
         if (reconnect && m_sessionId.trim().length() > 0)
         {
            reconnect();
            respDoc = execute(type, params, body, controller, false, repost);
         }
         else
            throw e;
      }


      return respDoc;
   }


   /**
    * Executes the request, writing the results to the supplied output stream
    * rather than returning a document.
    * 
    * @param type Specifies the request type. Must be a type expected by the
    * handler specified by the type prefix. May not be <code>null</code> or
    * empty and must begin with one of the supported handler prefixes. Currently
    * supported handler prefixes are <code>"deploy-"</code> and
    * <code>"job-"</code>. The request handler on the server must return a
    * non-200 result if the resulting content contains an exception document
    * rather than the expected content.
    * @param req The body of the request, the format of which is defined by the
    * handler and request type. May not be <code>null</code>.
    * @param out The stream to which the body of the response is written, may
    * not be <code>null</code> or closed. Will be closed by this method.
    * @param controller The file job controller to set the output stream on
    * before copying the file. May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSAuthenticationFailedException If the user cannot be
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    * server.
    * @throws PSServerLockException if a required server lock cannot be
    * obtained.
    * @throws PSDeployException if the connection is not valid or any other
    * errors occur executing the request.
    */
   public void execute(String type, Document req, OutputStream out,
      PSDeployFileJobControl controller)
         throws PSAuthenticationFailedException, PSAuthorizationException,
            PSServerException, PSDeployException, PSServerLockException
   {
      if (type == null)
         throw new IllegalArgumentException(" may not be null");

      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      if (out == null)
         throw new IllegalArgumentException("out may not be null");

      if (controller == null)
         throw new IllegalArgumentException("controller may not be null");

      execute(type, req, out, controller, true);
   }

   /**
    * Executes the specified request against the server.  Private version of
    * {@link #execute(String, Document, OutputStream, PSDeployFileJobControl)}
    * with an additional <code>reconnect</code> parameter described below.
    *
    * @param reconnect If <code>true</code>, an attempt is made to reconnect and
    * re-execute the request if an exception is returned by the server that
    * could indicate that the user's session has timed out.  If
    * <code>false</code>, no attempt to reconnect and re-execute is made.
    */
   private void execute(String type, Document req, OutputStream out,
      PSDeployFileJobControl controller, boolean reconnect)
         throws PSAuthenticationFailedException, PSAuthorizationException,
            PSServerException, PSDeployException, PSServerLockException
   {
      String requestPage = getRequestPage(type);

      // add session id if we have one
      Element root = req.getDocumentElement();
      root.setAttribute("sessionId", m_sessionId);

      // make request
      byte[] respData = null;
      InputStream in = null;
      int status = -1;
      PSPurgableTempFile reqFile = null;
      try
      {
         // no options
         NVPair[] opts = null;

         // add the request doc as an attachment
         reqFile = createAttachmentFile(req);

         NVPair[] file = new NVPair[1];
         file[0] = new NVPair(reqFile.getName(), reqFile.getPath());

         // add reqtype header after first header (will get set with
         // content-type header automatically by the formDataEncode call)
         NVPair[] hdrs = new NVPair[2];
         hdrs[1] = new NVPair(IPSCgiVariables.CGI_PS_REQUEST_TYPE, type);
         byte[] data = Codecs.mpFormDataEncode(opts, file, hdrs);

         // send the request to the Rx server
         HTTPResponse resp = null;
         synchronized(m_mutexObject)
         {
            resp = m_conn.Post(requestPage, data, hdrs);
         }
         // get the response code
         status = resp.getStatusCode();
         int contentLength = resp.getHeaderAsInt("Content-Length");
         if (status == 200)
         {
            in = resp.getInputStream();
            PSInputStreamCounter cIn = new PSInputStreamCounter(in);
            controller.setStream(cIn, contentLength);
            copyStream(cIn, out, controller);
         }
         else
         {
            // in this case we should have gotten back an exception in an XML
            // doc
            respData = resp.getData();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try{ in.close();} catch(IOException e){}
         if (out != null)
            try {out.close();} catch(IOException e){}
         if (reqFile != null)
            reqFile.release();
      }

      if (status != 200 || respData != null)
      {
         try
         {
            parseServerResponse(type, status, respData);
         }
         catch (PSAuthorizationException e)
         {
            if (reconnect && m_sessionId.trim().length() > 0)
            {
               reconnect();
               execute(type, req, out, controller, false);
            }
            else
               throw e;
         }
         catch (PSLockedException e)
         {
            if (reconnect && m_sessionId.trim().length() > 0)
            {
               reconnect();
               execute(type, req, out, controller, false);
            }
            else
               throw e;
         }
      }
   }


   /**
    * Disconnects, clears the sessionid, and calls
    * {@link #connect(boolean) connect(false)}.
    * 
    * @throws PSAuthenticationFailedException If the user cannot be
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    * server.
    * @throws PSDeployException for any other errors.
    */
   private void reconnect()
      throws PSAuthenticationFailedException, PSAuthorizationException,
      PSServerException, PSDeployException
   {
      doDisconnect();
      m_sessionId = "";
      connect(false);
   }


   /**
    * Determine if this connection is still valid for use.
    *
    * @return <code>true</code> if the connection is valid for use,
    * <code>false</code> if it is not.
    */
   public boolean isConnected()
   {
      return m_isConnected;
   }

   /**
    * Get the server version
    *
    * @return The server version, may be <code>null</code> if not connected,
    * never empty.
    */
   public String getServerVersion()
   {
      return m_isConnected ? m_version.getVersionString() : null;
   }

   /**
    * Gets the server build number.
    *
    * @return The number, may be <code>null</code> if not connected,
    * never empty.
    */
   public String getServerBuildNumber()
   {
      return m_isConnected ? m_version.getBuildNumber() : null;
   }

   /**
    * Gets the server's build date.
    *
    * @return The date, may be <code>null</code> if not connected.
    */
   public Date getServerBuildDate()
   {
      return m_isConnected ? m_version.getBuildDate() : null;
   }

   /**
    * Gets the password used to connect to the server.
    *
    * @param encrypted If <code>true</code>, the password will be returned in
    * its encrypted form.  Otherwise, the clear text version of the password is
    * returned.
    *
    * @return The password supplied by the ctor, possibly encrypted, never
    * <code>null</code>, may be empty.
    */
   public String getPassword(boolean encrypted)
   {
      return encrypted ? m_password : decryptPwd(m_uid, m_password);
   }

   /**
    * Get the dbms info for the server's cms repository.
    *
    * @return The dbms info.  UserId and Password will be empty to
    * allow comparisons without regard to credentials.  May be <code>null</code>
    *  if not connected.
    */
   public PSDbmsInfo getRepositoryInfo()
   {
      return m_isConnected ? m_serverRepositoryInfo : null;
   }

   /**
    * Returns a boolean indicating whether the Rx server to which this object
    * represents a connection is licensed for Enterprise Manager.
    *
    * @return <code>true</code> if the Rx server to which this object represents
    * a connection is licensed for Enterprise Manager, <code>false</code>
    * otherwise.
    */
   public boolean isServerLicensed()
   {
      return m_bLicensed;
   }

   /**
    * Encrypts the supplied password if it is non-<code>null</code> and not
    * empty.
    *
    * @param uid The user id, may be <code>null</code> or empty.
    * @param pwd The password to encrypt, may be <code>null</code> or empty.
    *
    * @return The encrypted password, or an empty string if the supplied
    * password is <code>null</code> or empty.
    */
   public static String encryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      String key = uid == null || uid.trim().length() == 0 ? PSLegacyEncrypter.INVALID_DRIVER() :
         uid;

      try {
         return PSEncryptor.getInstance().encrypt(pwd);
      } catch (PSEncryptionException e) {
         ms_log.error("Error encrypting password: " + e.getMessage(),e);
         return "";
      }

   }

   /**
    * Decrypts the supplied password if it is non-<code>null</code> and not
    * empty.
    *
    * @param uid The user id, may be <code>null</code> or empty.
    * @param pwd The password to decrypt, may be <code>null</code> or empty.
    *
    * @return The decrypted password, or an empty string if the supplied
    * password is <code>null</code> or empty.
    */
   public static String decryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      String key = uid == null || uid.trim().length() == 0 ? PSLegacyEncrypter.INVALID_DRIVER() :
         uid;

      try {
         return PSEncryptor.getInstance().decrypt(pwd);
      } catch (PSEncryptionException e) {
         return PSCryptographer.decrypt(PSLegacyEncrypter.INVALID_CRED(), key, pwd);
      }

   }

   /**
    * Makes connection request, stores the returned session id, and returns the
    * resulting response document.
    * 
    * @param overrideLock If <code>true</code>, then the lock is acquired by
    * the user no matter what, if <code>false</code> then sessionid has to
    * match for lock acquisition.
    * 
    * @return The response doc, never <code>null</code>.
    * 
    * @throws PSAuthenticationFailedException If the user cannot be
    * authenticated by the server.
    * @throws PSAuthorizationException If the user is not authorized to access
    * the server for deployment operations.
    * @throws PSServerException if there are any errors communicating with the
    * server.
    * @throws PSDeployException For any other errors.
    */
   private Document connect(boolean overrideLock)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException
   {
      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
         "PSXDeployConnectRequest");
      root.setAttribute("userId", m_uid);
      root.setAttribute("password", m_password);
      root.setAttribute("overrideLock", (overrideLock ? "yes" : "no"));
      root.setAttribute("enforceLicense", "no");

      Document respDoc;
      try
      {
         respDoc = execute(DEPLOY_REQUEST + "connect", reqDoc);
      }
      catch (PSServerLockException e)
      {
         throw new PSDeployException(e);
      }

      root = respDoc.getDocumentElement();
      m_sessionId = root.getAttribute("sessionId");

      return respDoc;
   }

   /**
    * Writes doc out to a temp file to be used as an attachment on a request.
    *
    * @param doc The document to store in the file.  Assumed not
    * <code>null</code>.
    *
    * @return The file, never <code>null</code>.
    *
    * @throws IOException If there are any errors.
    */
   private PSPurgableTempFile createAttachmentFile(Document doc)
      throws IOException
   {
      FileOutputStream out = null;
      try
      {
         PSPurgableTempFile reqFile = new PSPurgableTempFile("dpl_", ".xml",
            null);
         out = new FileOutputStream(reqFile);
         PSXmlDocumentBuilder.write(doc, out);
         return reqFile;
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (IOException ex){}
      }

   }

   /**
    * Constant to indicate the deployment version.  Used to determine
    * compatibility between the client and server.  If the server is modified
    * such that it is no longer compatible with older clients, this
    * value should be incremented.
    */
   public static final int DEPLOYMENT_INTERFACE_VERSION = 0;

   /**
    * Constant for prefix when making deployment handler requests.
    */
   public static final String DEPLOY_REQUEST = "deploy-";

   /**
    * Constant for prefix when making job handler requests.
    */
   public static final String JOB_REQUEST = "job-";

   /**
    * Attempts to parse the response returned by the server. Checks the supplied
    * <code>status</code> and <code>response</code> to see if the server has
    * returned any errors and if so throws the appropriate exception. If no
    * errors are found, the result document is returned.
    * 
    * @param type The request type, assumed not <code>null</code> or empty.
    * @param status The status returned by the request.
    * @param response The response to check, may be <code>null</code>.
    * 
    * @return The result document, never <code>null</code>.
    * 
    * @throws PSAuthenticationFailedException If the response is an Xml doc
    * containing a PSXDeployException element with this exception nested within
    * it.
    * @throws PSAuthorizationException If the response is an Xml doc containing
    * a PSXDeployException element with this exception nested within it.
    * @throws PSAuthorizationException If the response is an Xml doc containing
    * a PSXDeployException element with this exception nested within it.
    * @throws PSServerLockException if a required server lock cannot be
    * obtained.
    * @throws PSDeployException If the the <code>respDoc</code> is
    * <code>null</code>, the status is not <code>200</code>, or the data
    * contains a PSXDeployException element with none of the other exceptions
    * nested within it.
    */
   private Document parseServerResponse(String type, int status,
      byte[] response) throws PSAuthenticationFailedException,
         PSAuthorizationException, PSServerException, PSDeployException,
         PSServerLockException
   {
      Document respDoc = null;

      // handle case if server is still starting
      if (status == 503)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_NOT_AVAILABLE);
      }

      // make sure we got back some kind of response
      if (response == null || response.length == 0)
      {
         Object[] args = {type, String.valueOf(status)};
         throw new PSDeployException(IPSDeploymentErrors.SERVER_RESPONSE_EMPTY,
            args);
      }

      // try to parse the response as XML
      ByteArrayInputStream bIn = null;
      try
      {
         bIn = new ByteArrayInputStream(response);
         respDoc = PSXmlDocumentBuilder.createXmlDocument(bIn, false);
      }
      catch (Exception e)
      {
         // not valid xml
         if (status == 200)
         {
            // must have valid response for a 200 error.
            Object[] args = {type, e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_PARSE_ERROR, args);
         }
      }
      finally
      {
         if (bIn != null)
         {
            try { bIn.close(); } catch (IOException e) {}
         }
      }

      // if we parsed the response and status is 200, we're all done.
      if (status == 200)
         return respDoc;

      // see if we've got an error from the server that wasn't handled by the
      // deployment or job handlers.
      if (respDoc == null)
      {
         // can't handle it, so throw exception with the body as a string
         String msg = "";
         try
         {
            msg = new String(response, PSCharSetsConstants.rxJavaEnc());
         }
         catch (UnsupportedEncodingException e)
         {
            // we've done our best
         }

         Object[] args = {type, String.valueOf(status), msg};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_ERROR_RESPONSE, args);
      }

      // We must have an error that we can handle in some way.
      Element root = respDoc.getDocumentElement();
      PSDeployException de = null;
      String className = null;
      int errorCode = -1;
      Object[] errorArgs = null;
      if (root.getNodeName().equals(PSDeployException.XML_NODE_NAME))
      {
         try
         {
            de = new PSDeployException(root);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // malformed exception xml (should not happen)
            Object[] args = {type, PSDeployException.XML_NODE_NAME,
                  e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }

         className = de.getOriginalExceptionClass();
         if (className != null)
         {
            errorCode = de.getErrorCode();
            errorArgs = de.getErrorArguments();
         }
      }
      else if (root.getNodeName().equals(PSJobException.XML_NODE_NAME))
      {
         PSJobException je = null;
         try
         {
            je = new PSJobException(root);
            de = new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               je.getLocalizedMessage());
         }
         catch (PSUnknownNodeTypeException e)
         {
            // malformed exception xml (should not happen)
            Object[] args = {type, PSDeployException.XML_NODE_NAME,
                  e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }

         className = je.getOriginalExceptionClass();
         if (className != null)
         {
            errorCode = je.getErrorCode();
            errorArgs = je.getErrorArguments();
         }
      }
      else
      {
         Object[] args = {type, String.valueOf(status),
            PSXmlDocumentBuilder.toString(respDoc)};
         throw new PSDeployException(IPSDeploymentErrors.SERVER_ERROR_RESPONSE,
            args);
      }

      if (PSAuthenticationFailedException.class.getName().equals(
         className))
      {
         throw new PSAuthenticationFailedException(errorCode, errorArgs);
      }
      else if (PSAuthorizationException.class.getName().equals(className))
      {
         throw new PSAuthorizationException(errorCode, errorArgs);
      }
      else if (PSAuthenticationRequiredException.class.getName().equals(
         className))
      {
         throw new PSAuthenticationRequiredException(errorCode, errorArgs);
      }
      else if (PSServerException.class.getName().equals(className))
      {
         throw new PSServerException(errorCode, errorArgs);
      }
      else if (PSDeployNonUniqueException.class.getName().equals(className))
      {
         throw new PSDeployNonUniqueException(errorCode, errorArgs);
      }
      else if (PSLockedException.class.getName().equals(className))
      {
         throw new PSLockedException(errorCode, errorArgs);
      }
      else if (PSServerLockException.class.getName().equals(className))
      {
         throw new PSServerLockException(errorCode, errorArgs);
      }
      else if (de != null)
      {
         throw de;
      }

      // should never get here, but compiler complains without it
      return respDoc;
   }

   /**
    * Copies the supplied input stream to the output stream, using the
    * controller to detect cancellation and to mark copy as completed.
    *
    * @param in The input stream, assumed not <code>null</code>.
    * @param out The output stream, assumed not <code>null</code>.
    * @param controller The job controller, with the appropriate stream already
    * set on it, assumed not <code>null</code>.
    *
    * @throws IOException If any errors occur.
    */
   private void copyStream(InputStream in, OutputStream out,
      PSDeployFileJobControl controller) throws IOException
   {
         byte[] buf = new byte[8192];
         while (true)
         {
            int read = in.read(buf);
            if (read < 0 || controller.getCancelledStatus() != -1)
               break;
            out.write(buf, 0, read);
         }
         out.flush();
         out.close();
         out = null;
         in.close();
   }

   /**
    * Gets the appropriate request page for the given request type.
    *
    * @param requestType The type, may not be <code>null</code> or empty, and
    * must be a known type.
    *
    * @return The page, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>requestType</code> is invalid.
    */
   private String getRequestPage(String requestType)
   {
      if (requestType == null || requestType.trim().length() == 0)
         throw new IllegalArgumentException(
            "requestType may not be null or empty");

      if (m_reqRoot == null)
      {
         String reqRoot = System.getProperty(IPSDeployConstants.PROP_RX_ROOT);
         if (reqRoot == null || reqRoot.trim().length() == 0)
            reqRoot = "/Rhythmyx";
         else
         {
            // make sure it starts with a slash, and doesn't end with one
            String slash = "/";
            if (!reqRoot.startsWith(slash))
               reqRoot = slash + reqRoot;
            if (reqRoot.endsWith(slash))
               reqRoot = reqRoot.substring(0, reqRoot.length() - 1);
         }
         m_reqRoot = reqRoot;
      }

      String page;
      if (requestType.startsWith(DEPLOY_REQUEST))
         page = m_reqRoot + DEPLOY_REQUEST_PAGE;
      else if (requestType.startsWith(JOB_REQUEST))
         page = m_reqRoot + JOB_REQUEST_PAGE;
      else
         throw new IllegalArgumentException("invalid type");

      return page;
   }

   /**
    * Converts params to an <code>NVPair[]</code> array.
    *
    * @param params Map of params, where param name is key as
    * <code>String</code> and value is the param value.  May be
    * <code>null</code>, may not contain a <code>null</code> key.
    *
    * @return The array, will be <code>null</code> if <code>params</code> is
    * <code>null</code> or emtpy.
    */
   private NVPair[] getParams(Map params)
   {
      // add the params
      NVPair[] opts = null;
      if (params != null && params.size() > 0)
      {
         opts = new NVPair[params.size()];
         Iterator i = params.entrySet().iterator();
         int onParam = 0;
         while (i.hasNext())
         {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getKey() == null)
               throw new IllegalArgumentException(
                  "params may not contain a null key");

            String val = null;
            if (entry.getValue() != null)
               val = entry.getValue().toString();
            else
               val = "";

            opts[onParam] = new NVPair(entry.getKey().toString(), val);
            onParam++;
         }
      }

      return opts;
   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger.getLogger(PSDeploymentServerConnection.class);

   /**
    * Constant for the page to use when executing deployment requests against
    * the server.
    */
   private static final String DEPLOY_REQUEST_PAGE =
      "/sys_deployerHandler";

   /**
    * Constant for the page to use when executing job requests against
    * the server.
    */
   private static final String JOB_REQUEST_PAGE =
      "/sys_jobHandler";

   /**
    * Determines if this object can be used to execute requests against the
    * server.  Intialized during the ctor, may be modified by calls to
    * {@link #reconnect()} and {@link #disconnect()}.
    */
   private volatile boolean m_isConnected;


   /**
    * The name of the server to connect to, set during ctor, never
    * <code>null</code>, empty or modified after that.
    */
   private String m_server;

   /**
    * The port on the server to connect to, set during ctor, never modified
    * after that.
    */
   private int m_port;

   /**
    * The server version info.  Obtained during connection attempt from ctor,
    * may be <code>null</code> if connection request fails, may be reset on a
    * subsequent attempt to reconnect.
    */
   private PSFormatVersion m_version;

   /**
    * The session id obtained from the intial connection.  May be modified if
    * the user's session on the server times out and we must reconnect.
    */
   private String m_sessionId = "";

   /**
    * The server's repository information.  Obtained during connection attempt
    * from ctor, may be <code>null</code> if connection request fails, may be
    * reset on a subsequent attempt to reconnect.
    */
   private PSDbmsInfo m_serverRepositoryInfo;

   /**
    * The user's id.  Set during ctor, never <code>null</code> or empty or
    * modified after that.
    */
   private String m_uid;

   /**
    * The user's password.  Set during ctor, may be empty, never
    * <code>null</code> or modified after that.
    */
   private String m_password;

   /**
    * The http connection object constructed on first attempt to connect to the
    * server.  Never modified or <code>null</code> after that.
    */
   private HTTPConnection m_conn;

   /**
    * Mutex preventing concurrent access to a critical section by having threads
    * synchronize on it. Here it's preventing concurrent access to <code>m_conn
    * </code>. Never modified.
    */
   private Object m_mutexObject = new Object();

   /**
    * Runs as a daemon thread. Lock is granted for 30 mins. Locker thread
    * extends the lock after 28 mins. Initialised in the ctor, never <code>null
    * </code> or modified.
    * Thread dies whenever <code>m_isConnected</code> is <code>false</code>.
    */
   private Thread m_lockerThread;

   /**
    * Rhythmyx server request root, initialzied by first call to
    * {@link #getRequestPage(String)}, never <code>null</code>, empty, or
    * modified after that.
    *
    */
   private String m_reqRoot = null;

   /**
    * <code>true</code> if the Rx server to which this object represents a
    * connection is licensed for Enterprise Manager, <code>false</code>
    * otherwise. Initialized to <code>true</code>, then set in the constructor
    * based on the response document obtained from the Rx Server in the
    * <code>connect()</code> method, never modified after that.
    */
   private boolean m_bLicensed = true;
}


