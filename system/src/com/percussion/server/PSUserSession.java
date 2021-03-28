/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.i18n.PSLocale;
import com.percussion.security.IPSEncryptor;
import com.percussion.security.IPSKey;
import com.percussion.security.IPSSecretKey;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptionKeyFactory;
import com.percussion.security.PSEncryptor;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserEntry;
import com.percussion.security.ToDoVulnerability;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSCharSets;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The PSUserSession object defines a user logged in to the system.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUserSession
{
   /**
    * Create a new user session object.
    *
    * @param request the request object making this request
    *
    * @param   sessId the session id to use. Should be from prior call to
    * {@link #getIdFromRequest(PSRequest)}, or if that returns
    * <code>null</code>, then from {@link #createSessionId(PSRequest)}
    */
    PSUserSession(PSRequest request, String sessId)
   {
      m_isDesignerSession = PSUserSessionManager.isDesignerRequest(request);
      m_id = sessId;      
      determineOriginalHostPortProtocol(request);
      
   }
   
   /**
    * Creates a new session for the supplied request based on the current one. 
    * Every member of the new session references to the existing one except for 
    * the authenticated entry list which will be empty.
    * @param request the request object with which the cloned session is 
    * associated with. Must not be <code>null</code>.  
    * @return new session with empty authenticated entry list.
    */
   PSUserSession cloneSessionForRequest(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      
      PSUserSession session = new PSUserSession(request, m_id);
      
      session.m_privateObjects = new ConcurrentHashMap(m_privateObjects);
      session.m_systemObjects = m_systemObjects;
      session.m_UserEntries = new CopyOnWriteArrayList();
      session.m_usrMeta = m_usrMeta;
      session.m_usrProp = m_usrProp;
      
      return session;
   }

   /**
    * Get the host that was used by the request that originated
    * this session.
    *
    * @return the original host, the one who created this session. Never
    *    <code>null</code>, might be empty.
    */
   public String getOriginalHost()
   {
      return m_originalHost;
   }

   /**
    * Get the port that was used by the request that originated
    * this session.
    *
    * @return the original port, the default (80) if none was provided.
    */
   public int getOriginalPort()
   {
      return m_originalPort;
   }

   /**
    * Get the protocol that was used by the request that originated
    * this session.
    *
    * @return the original protocol as a string; either "http" or "https"
    */
   public String getOriginalProtocol()
   {
      return m_originalProtocol;
   }


   /**
    * Get this session's identifier.
    *
    * @return                 this session's identifier
    */
   public String getId()
   {
      return m_id;
   }

   /**
    * Test if this session is for a designer connection. This is an accessor
    * for stored info thats never used within this class.
    *
    * @return <code>true</code> if this is a designer session,
    *    <code>false</code> otherwise.
    */
   public boolean isDesignerSession()
   {
      return m_isDesignerSession;
   }

   /**
    * Get the session identifier string associated with the
    * specified request object.
    *
    * @param      request     the request object
    *
    * @return                 the session identifier to use or
    *                         <code>null</code> if a the request does not
    *                         contain a session id.
    *
    */
   public static String getIdFromRequest(PSRequest request)
   {
      String retId;
      HttpSession s = request.getHttpSession();
      retId = (String) s.getAttribute(IPSHtmlParameters.SYS_SESSIONID);
      
      if (StringUtils.isBlank(retId))
      {
         // do we have a pssessionid html parameter?
         retId = request.getParameter(IPSHtmlParameters.SYS_SESSIONID);
      }
      
      if (StringUtils.isBlank(retId))
      {
         // does this have the standard session cookie?
         retId = request.getCookie(SESSION_COOKIE);
      }
      
      return retId;
   }

   /**
    * Create a new session Id. We will use <host address>, current system time,
    * the user agent and a unique number incremented in each call run through a
    * hash algorithm to generate the session id at this time. In the future we
    * may also want to use the authenticated user name.
    *
    * @param request the request to create the session id for.
    * @return the new created session id or <code>null</code> if a session
    *    id cannot be created or the provided request is <code>null</code>.
    */
   public static String createSessionId(PSRequest request)
   {
      HttpSession s = request.getServletRequest().getSession();
      String retId = null;
      synchronized(s)
      {
         retId = (String) s.getAttribute(IPSHtmlParameters.SYS_SESSIONID);
         if (retId == null)
         {
            retId = buildSessionId(request);
            s.setAttribute(IPSHtmlParameters.SYS_SESSIONID, retId);
         }
      }
      
      return retId;
   }

   /**
    * Construct a unique session id from request information and a monotonically
    * incremented counter.
    * 
    * @param request The current request, assumed not <code>null</code>.
    * 
    * @return The session id, never <code>null</code> or empty.
    */
   private static String buildSessionId(PSRequest request)
   {
      String retId;
      String sessIdStr = request.getServletRequest().getServerName();
      sessIdStr += String.valueOf(System.currentTimeMillis()) +
         request.getCgiVariable(IPSCgiVariables.CGI_REQUESTOR_SOFTWARE, "");
      sessIdStr += getNextSessionNumber();

      try
      {
         MessageDigest md = MessageDigest.getInstance("SHA-1");

         md.update(sessIdStr.getBytes(PSCharSets.rxJavaEnc()));
         byte[] digest = md.digest();

         StringBuffer buf = new StringBuffer(digest.length * 2);
         for (int i = 0; i < digest.length; i++)
         {
            String sTemp = Integer.toHexString(digest[i]);
            if (sTemp.length() == 0)
               sTemp = "00";
            else if (sTemp.length() == 1)
               sTemp = "0" + sTemp;
            else if (sTemp.length() > 2)
               sTemp = sTemp.substring(sTemp.length() - 2);

            buf.append(sTemp);
         }

         retId = buf.toString();
      }
      catch (NoSuchAlgorithmException e)
      {
         /* this should never happen, but let's try an alternate approach */
         retId = sessIdStr;
      }
      catch (UnsupportedEncodingException e)
      {
         // should never happen
         retId = sessIdStr;
      }
      
      return retId;
   }

   /**
    * Get the users the requestor has authenticated as.
    *
    * @return                 the array of user entries
    */
   @SuppressWarnings("unchecked")
   public PSUserEntry[] getAuthenticatedUserEntries()
   {
      int size = m_UserEntries.size();
      PSUserEntry[] ret = new PSUserEntry[size];
      return (PSUserEntry[]) m_UserEntries.toArray(ret);
   }
   
   /**
    * Get a list of the users roles, alpha sorted ascending.
    *  
    * @return The list, never null, may be empty.
    */
   public List<String> getUserRoles()
   {
      List<String> roleList = new ArrayList<String>();
      Iterator userEntries = m_UserEntries.iterator();
      while (userEntries.hasNext())
      {
         PSUserEntry userEntry = (PSUserEntry) userEntries.next();
         PSRoleEntry[] roles = userEntry.getRoles();
         for (int i = 0; i < roles.length; i++)
         {
            roleList.add(roles[i].getName());
         }
      }
      
      Collections.sort(roleList);
      
      return roleList; 
   }
   
   /**
    * Get the user's current community
    * @return the current community name or <code>null</code> if unknown or
    * error
    */
   public String getUserCurrentCommunity()
   {
      IPSBackEndRoleMgr berm = PSRoleMgrLocator.getBackEndRoleManager();
      String cid = (String) getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      if (StringUtils.isBlank(cid))
      {
         return null;
      }
      try
      {
         PSCommunity c = berm.loadCommunity(new PSGuid(PSTypeEnum.COMMUNITY_DEF, cid));
         return c.getName();
      }
      catch (PSSecurityException e)
      {
         return null;
      }
   }

   /**
    * This method retrieves the list user's role-communities, viz. list of all
    * communities via his role membership.
    *
    * @param request The current request, assumed not <code>null</code>.
    *
    * @return list of user communities (community ids) as <code>String</code>
    * objects, never <code>null</code> may be empty.  List is cached in the 
    * user's session after the first call.
    *
    * @throws PSInternalRequestCallException if there is an error retrieving
    * the communities
    */
   @SuppressWarnings(value={"unchecked"})
   public List<String> getUserCommunities(PSRequest request)
      throws PSInternalRequestCallException
   {
      List<String> list = (List<String>) getPrivateObject(USER_COMMUNITIES);
      if (list == null)
      {
         list = new ArrayList<String>();
         
         // Make an internal request to get the user roles.
         PSInternalRequest iReq = PSServer.getInternalRequest(
            IREQ_USERCOMMUNITIES, request, null, true);
         if (iReq == null)
            throw new RuntimeException("Cannot locate system resource: " +
               IREQ_USERCOMMUNITIES);

         Document doc = iReq.getResultDoc();
         NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
         if(nl == null || nl.getLength() < 1)
            return list;

         Element elem = null;
         for(int i=0; i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            String communityId = elem.getAttribute(ATTR_COMMID);
            if (!StringUtils.isBlank(communityId))
               list.add(communityId);
         }
         
         setPrivateObject(USER_COMMUNITIES, list);
      }
      
      return new ArrayList<String>(list);
   }
   
   /**
    * Get the list of the user's communites as a list of names.
    * 
    * @param request The current request to use, may not be <code>null</code>.
    * 
    * @return The list of names, never <code>null</code>.
    * 
    * @throws PSInternalRequestCallException if there is an error.
    */
   @SuppressWarnings(value={"unchecked"})
   public List<String> getUserCommunityNames(PSRequest request) 
      throws PSInternalRequestCallException
   {
      if (request == null)
         throw new IllegalArgumentException("request  may not be null");
      
      List<String> names = (List<String>) getPrivateObject(
         USER_COMMUNITY_NAMES);
      if (names == null)
      {
         names = new ArrayList<String>();
         List<String> ids = getUserCommunities(request);
         if (!ids.isEmpty())
         {
            IPSGuid[] guids = new IPSGuid[ids.size()];
            for (int i = 0; i < guids.length; i++)
            {
               guids[i] = new PSGuid(PSTypeEnum.COMMUNITY_DEF, 
                  Long.parseLong(ids.get(i)));
            }
            
            IPSBackEndRoleMgr beRoleMgr = 
               PSRoleMgrLocator.getBackEndRoleManager();
            for (PSCommunity comm : beRoleMgr.loadCommunities(guids))
            {
               names.add(comm.getName());
            }
         }
         
         setPrivateObject(USER_COMMUNITY_NAMES, names);
      }
      
      return new ArrayList<String>(names);
   }

   /**
    * Looks up the community id for the specified community name
    *
    * @param request The current request, assumed not <code>null</code>.
    * @param name Community name, assumed not <code>null</null> or empty.
    *
    * @return The Community id, or <code>null</code> if the specified community
    * does not exist.
    *
    * @throws PSInternalRequestCallException if there is an error retrieving
    * the community id
    */
   public String getCommunityId(PSRequest request, String name)
      throws PSInternalRequestCallException
   {
      Map<String,String> params = new HashMap<String,String>(1);
      params.put(COMMUNITYNAME, name);

      PSInternalRequest iReq = PSServer.getInternalRequest(IREQ_COMMUNITYLOOKUP,
         request, params, true);
      if (iReq == null)
      {
         throw new RuntimeException("Cannot locate system resource: " +
            IREQ_COMMUNITYLOOKUP);
      }

      String result = null;
      Document doc = iReq.getResultDoc();
      NodeList nl = doc.getElementsByTagName(ELEM_COMMUNITY);
      if (nl != null && nl.getLength() > 0)
      {
         Element elem = (Element)nl.item(0);
         result = elem.getAttribute(ATTR_COMMID);
      }

      return result;
   }

   /**
    * Looks at all authenticated users in this session, and the first one is
    * returned.
    * <p>
    * Note: All authenticated entries will have the same name.
    * 
    * @return A valid, authenticated user name or <code>null</code> if there
    * isn't one.
    * 
    * @todo The Host provider should be removed from the list of authenticated
    * user entries and this method should be removed. The host provider entries
    * should be used as a filter on incoming request rather than as
    * authenticated entries.
    */
   public String getRealAuthenticatedUserEntry()
   {
      String userName = null;
      if ( null != m_UserEntries )
      {
         for ( Iterator iter = m_UserEntries.iterator(); iter.hasNext(); )
         {
            PSUserEntry entry = (PSUserEntry) iter.next();
            userName = entry.getName();
            break;
            
         }
      }
      return userName;
   }

   /**
    * Returns whether or not this session has authenticated user entries.
    *
    * @return <code>true</code> if this session has authenticated users,
    *    <code>false</code> otherwise.
    */
   public boolean hasAuthenticatedUserEntries()
   {
      return m_UserEntries.size() != 0;
   }

   /**
    * Returns whether or not this session has the specified authenticated user 
    * entry.
    * 
    * @param testEntry The entry to check for, may not be <code>null</code>.
    *
    * @return <code>true</code> if this session has the specified authenticated 
    * user entry, <code>false</code> otherwise.
    */
   public boolean hasAuthenticatedUserEntry(PSUserEntry testEntry)
   {
      if (testEntry == null)
         throw new IllegalArgumentException("testEntry may not be null");
      
      PSUserEntry[] entries = getAuthenticatedUserEntries();
      
      if (entries == null)
         return false;
      
      for(int i=0; i<entries.length; i++)
      {
         PSUserEntry entry = entries[i];
         if(entry.equals(testEntry))
            return true;
      }
      return false;
   }
   
   /**
    * Returns whether or not this session has the specified authenticated user 
    * name.
    *
    * @return <code>true</code> if this session has the specified authenticated 
    * user entry, <code>false</code> otherwise.
    */
   public boolean hasAuthenticatedUserEntry(String userName)
   {
      PSUserEntry[] entries = getAuthenticatedUserEntries();
      
      if (entries == null)
         return false;
      
      for(int i=0; i<entries.length; i++)
      {
         PSUserEntry entry = entries[i];
         if(entry.getName().equals(userName))
            return true;
      }
      return false;
   }   

   /**
    * Add an authenticated user entry to this session's identifier. This
    * should only be called by the security engine once a user has been
    * successfully authenticated.
    * Loads all the system and designer persistent properties once.
    */
   @SuppressWarnings("unchecked")
   public void addAuthenticatedUserEntry(PSUserEntry entry)
   {
      if (!m_UserEntries.contains(entry)) // only add it once!
         m_UserEntries.add(entry);
   }


   /**
    * Get the back-end login id/pw the user has supplied through this
    * session (using the ODBC security provider).
    *
    * @param      driver      the back-end driver to get the credentials for
    *
    * @param      server      the back-end server to get the credentials for
    *
    * @return                 If the user has supplied credentials, returns
    *                         an array containing the login id in
    *                         element 0 and the login pw in element 1;
    *                         otherwise <code>null</code> is returned
    */
   public String[] getBackEndCredentials(
         String driver, String server)
   {
      if (driver == null)
         driver = "";
      if (server == null)
         server = "";

      return (String[])m_Credentials.get(driver + "/" + server);
   }

   /**
    * Set the back-end login id/pw the user has supplied through this
    * session (using the ODBC security provider).
    *
    * @param      driver      the back-end driver to set the credentials for
    *
    * @param      server      the back-end server to set the credentials for
    *
    * @param      loginId     the login id to use for connections
    *
    * @param      loginPw     the login password to use for connections
    */
   @SuppressWarnings("unchecked")
   public void setBackEndCredentials(
      String driver, String server, String loginId, String loginPw)
   {
      if (driver == null)
         driver = "";
      if (server == null)
         server = "";
      if (loginId == null)
         loginId = "";
      if (loginPw == null)
         loginPw = "";

      m_Credentials.put(
         (driver + "/" + server), new String[] { loginId, loginPw });
   }

   /**
    * Get the sessions create time stamp
    */
   public Date getCreateTimeStamp()
   {
         return m_createTime;
   }



   /**
    * Touching the idle time resets the sessions time stamp to the current time.
    */
   public void touchIdle()
   {
      Boolean sessionTouch = (Boolean)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_NOSESSIONTOUCH);
      if (sessionTouch==null || sessionTouch!=Boolean.TRUE)
         m_idleFrom = System.currentTimeMillis();
   }

   /**
    * Sets idle from to a number of milliseconds in the past,  will not set before current idleFrom time.
    */
   public void setIdleOffset(long offset)
   {
      long now = System.currentTimeMillis();
      long offsetTime = now - offset;
      if (offsetTime > m_idleFrom)
         m_idleFrom = offsetTime;
   }


   /**
    * Get session idle time.
    */
   public long getIdleSince()
   {
      return m_idleFrom;
   }

   /**
    * Add an authentication to the authentication pool for passthrough
    *
    * @param      uid      the id associated with this credential
    * @param      auth     authentication info associated with this credential
    */
   public void addAuthentication(String uid, String auth)
   {
      try {
         m_authentications.put(uid, PSEncryptor.getInstance(
                 "AES",PSServer.getRxDir().getAbsolutePath().concat(
                         PSEncryptor.SECURE_DIR)
         ).encrypt(auth));
      } catch (PSEncryptionException e) {
         ms_log.error("Error encrypting authentication: " + e.getMessage(),e);
         m_authentications.put(uid,"");
      }
   }

   /**
    * Get an authentication to the authentication pool for passthrough
    *
    * @param      uid      the id associated with this credential
    *
    * @return     authentication info associated with this credential
    */
   public Object getAuthenticationData(String uid)
   {
      return m_authentications.get(uid);
   }

   /**
    * Traverse the keys in the authentication pool for passthrough
    *
    * <em>NOTE:</em> The caller must synchronize on the PSUserSession (the same
    * instance on which authenticationIdIterator() is invoked) before
    * invoking this method and while the Iterator is in use.
    *
    * @return     keyset iterator for authentications
    *
    * @todo Consider changing this to return an iterator to a copied set of
    * keys.  That would prevent the need for the caller to nize.
    * <p>However, it's not clear at this time if an iterator is
    * guaranteed to provide a reference to the container object itself, and
    * thus prevent GC of the copied container.  (It's <em>very</em> likely,
    * but I saw no guarantee in the spec) (dbreslau 12/12/02)
    */
   public Iterator authenticationIdIterator()
   {
      return m_authentications.keySet().iterator();
   }

   /**
    *    Store a back end credential identifier which we know works
    *    for this driver/server.
    *
    * @param      driver   the driver which used this credential
    * @param      server   the server which accepted this credential
    * @param      uid      the id associated with this credential
    */
   @SuppressWarnings("unchecked")
   public void putBeWorkingCredential(
         String driver,
         String server,
         String uid)
   {
      if (driver == null)
         driver = "";

      if (server == null)
         server = "";

      m_dbIds.put(driver + ":" + server, uid);
   }

   /**
    *    Get a back end credential identifier which we know works
    *    for this driver/server.
    *
    * @param      driver   the driver to look up
    * @param      server   the server to look up
    *
    * @return     the id for the credential which was used successfully
    *             for this driver/server, <code>null<code/> if none available
    */
   public Object getBeWorkingCredential(
         String driver, String server)
   {
      if (driver == null)
         driver = "";

      if (server == null)
         server = "";

      return m_dbIds.get(driver + ":" + server);
   }

   /**
    * Get a private object associated with this user session. This
    * is provided as a storage area for exit handlers, etc. to create
    * context information once. This can then be retrieved across
    * requests throught the user's session.
    *
    * @param   key      the key under which the object is stored
    *
    * @return           the private object associated with the key
    *
    * @exception  com.percussion.error.PSRuntimeException   if key is null
    */
   public synchronized Object getPrivateObject(Object key)
      throws com.percussion.error.PSRuntimeException
   {
      loadPersistentProperties();
      if (key == null)
         throw new com.percussion.error.PSRuntimeException(
            IPSDataErrors.EXECDATA_PRIVATE_OBJ_KEY_NULL);

      return m_privateObjects.get(key);
   }

   /**
    * Set a private object associated with this user session. This
    * is provided as a storage area for exit handlers, etc. to create
    * context information once. This can then be retrieved across
    * requests throught the user's session.
    *
    * @param   key      the key under which the object is stored. Be sure to
    *                   to specify a unique name -- that is, something other
    *                   exit handlers, etc. using this mechanism will not
    *                   likely use as a name.  Must not be <code>null</code>.
    *
    * @param   o        the private object associated with the key (may be
    * <code>null</code>)
    *
    * @exception  com.percussion.error.PSRuntimeException   if key is null
    */
   @SuppressWarnings("unchecked")
   public synchronized void setPrivateObject(Object key, Object o)
      throws com.percussion.error.PSRuntimeException
   {
      loadPersistentProperties();
      if (key == null)
         throw new com.percussion.error.PSRuntimeException(
            IPSDataErrors.EXECDATA_PRIVATE_OBJ_KEY_NULL);
      m_privateObjects.put(key, o);
   }

   /**
    * Get the user session status.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @return the element containing all user session status information,
    *    never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public Element getUserSessionStatus(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      Element authentications = doc.createElement("Authentications");
      Iterator auths = m_authentications.keySet().iterator();
      while (auths.hasNext())
      {
         Element authentication = doc.createElement("Authentication");

         String user = (String) auths.next();
         authentication.setAttribute("user", user);
         authentication.setAttribute("password",
            (String) m_authentications.get(user));

         authentications.appendChild(authentication);
      }

      Element credentials = doc.createElement("Credentials");
      Iterator creds = m_Credentials.keySet().iterator();
      while (creds.hasNext())
      {
         Element credential = doc.createElement("Credential");

         String driverServer = (String) creds.next();
         credential.setAttribute("driverServer", driverServer);
         String[] login = (String[]) m_Credentials.get(driverServer);
         credential.setAttribute("user", login[0]);
         credential.setAttribute("password", login[1]);

         credentials.appendChild(credential);
      }

      Element userEntries = doc.createElement("AuthenticatedUsers");
      Iterator entries = m_UserEntries.iterator();
      while (entries.hasNext())
      {
         PSUserEntry entry = (PSUserEntry) entries.next();
         userEntries.appendChild(entry.getUserEntryStatus(doc));
      }

      Element usedDbCredentials = doc.createElement("UsedDatabaseCredentials");
      Iterator usedCreds = m_dbIds.keySet().iterator();
      while (usedCreds.hasNext())
      {
         String driverServer = (String) usedCreds.next();
         Element usedDbCredential = doc.createElement("Credential");
         usedDbCredential.setAttribute("driverServer", driverServer);
         usedDbCredential.setAttribute("user",
            (String) m_dbIds.get(driverServer));

         usedDbCredentials.appendChild(usedDbCredential);
      }

      Element session = doc.createElement("UserSession");
      session.setAttribute("id", m_id);
      session.setAttribute("host", m_originalHost);
      session.setAttribute("port", Integer.toString(m_originalPort));
      session.appendChild(authentications);
      session.appendChild(credentials);
      session.appendChild(userEntries);
      session.appendChild(usedDbCredentials);

      return session;
   }
   
   /**
    * Determine if this is an anonymous session.
    * 
    * @return <code>true</code> if it is anonymous, <code>false</code> if not.
    */
   public boolean isAnonymous()
   {
      return !hasAuthenticatedUserEntries();
   }

   /* Pasta time!
   */
   @ToDoVulnerability
   @Deprecated
   private static String makeLasagna(String uid, String str)
   {
      if ((str == null) || (str.equals("")))
         return "";

      try {
         IPSKey key = PSEncryptionKeyFactory.getKeyGenerator(PSEncryptionKeyFactory.DES_ALGORITHM);
         byte[] encrData = str.getBytes(PSCharSets.rxJavaEnc());

         if ((key != null) && (key instanceof IPSSecretKey))
         {
            IPSSecretKey secretKey = (IPSSecretKey)key;

            int partone = PSLegacyEncrypter.getInstance(
                    PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
            ).OLD_SECURITY_KEY().hashCode();
            int parttwo;
            if (uid == null || uid.equals("")) {
               parttwo = PSLegacyEncrypter.getInstance(
                       PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
               ).OLD_SECURITY_KEY2().hashCode();
            }
            else
               parttwo = uid.hashCode();

            partone /= 7;
            parttwo /= 13;

            long time = new Date().getTime();
            byte[] baInner = new byte[8];
            for (int i = 0; i < 8; i++)
               baInner[i] = (byte)((time >> i) & 0xFF);
            baInner[0] = (byte)(8 - (encrData.length % 8));

            secretKey.setSecret(baInner);

            IPSEncryptor encr = secretKey.getEncryptor();
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            encr.encrypt(new ByteArrayInputStream(encrData), bOut);
            encrData = bOut.toByteArray();
            int innerDataLength = encrData.length;

            for (int i = 0; i < 8; i++)
               baInner[i] ^= (byte) ((1 << i) & innerDataLength);

            byte[] outerData = new byte[baInner.length + innerDataLength];

            System.arraycopy(baInner, 0, outerData, 0, 4);
            System.arraycopy(encrData, 0, outerData, 4, innerDataLength);
            System.arraycopy(baInner, 4, outerData, innerDataLength + 4, 4);

            byte[] baOuter = new byte[8];
            for (int i = 0; i < 4; i++)
               baOuter[i] = (byte)((partone >> i) & 0xFF);
            for (int i = 4; i < 8; i++)
               baOuter[i] = (byte)((parttwo >> (i-4)) & 0xFF);

            secretKey.setSecret(baOuter);
            bOut = new ByteArrayOutputStream();
            encr.encrypt(new ByteArrayInputStream(outerData), bOut);

            encrData = bOut.toByteArray();
            bOut.close();
         }

         /* Base 64 encode and return ... */
         ByteArrayOutputStream bOut2 = null;
         try {
            bOut2 = new ByteArrayOutputStream();
            PSBase64Encoder.encode(
               new ByteArrayInputStream(encrData), bOut2);
            return bOut2.toString();
         } catch (Exception e) {
            bOut2.close();
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   /**
    * @param name whoes value is being sought.
    * @return  <code>null</code> if mapping doesn't exist.
    * @throws IllegalArgumentException if property name is <code>null</code>.
    */
   public synchronized Object getSessionObject( String name )
   {
      loadPersistentProperties();
      if (name == null || name.length() == 0)
         return new IllegalArgumentException("Key has to be supplied");
      return m_systemObjects.get(name);
   }



    /**
    * Persists all the values for a user at the end of request.
    * Decision to persist is made in PSPersistentPropertyManager.
    */
   public synchronized void requestFinished()
   {
      if ( !m_isLoaded )
         return;
      PSPersistentPropertyManager mgr =
         PSPersistentPropertyManager.getInstance();
      updateProperties(SYSTEM, m_systemObjects);
      updateProperties(PRIVATE, m_privateObjects);
      mgr.save(m_usrProp, this);
   }

   /**
    * Updates <code>PSPersistentProperty</code> objects in  m_usrMeta
    * and adds new ones if added.
    *
    * <p><em>NOTE:</em> This method must be called only from
    * <code>synchronized</code> methods.  Since the method is private,
    * it should be easy to verify this.
    *
    * @param context - system or designer
    * @param map - system or private map, assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void updateProperties(String context, Map map)
   {
      if (!m_isLoaded || map.isEmpty())
         return;
      for (Iterator itr = m_usrMeta.iterator();itr.hasNext();)
      {
         PSPersistentPropertyMeta meta =
                          (PSPersistentPropertyMeta)itr.next();
         
         String propName = meta.getPropertyName();
         // if key is not there at all then the property does not exist.
         // this applies to situation where meta exist but property does not
         // exist yet in the database or "map".
         if (!map.containsKey(propName))
         {
            continue;
         }
         String newValue = (String)map.get(propName);
         boolean isNew = isNewProperty(propName);
         
         //check to see if the property is new and exists in the map
         if (newValue != null && newValue.length() != 0 && isNew)
         {
            PSPersistentProperty newProp = new  PSPersistentProperty(
            PSPersistentPropertyManager.getUserName(this), propName,
                      CATEGORY, context, newValue);
            newProp.setExtraParam(PSPersistentPropertyManager.INSERT);
            m_usrProp.add(newProp);
            
            if (ms_log.isDebugEnabled())
            {
               ms_log.debug("Persistent property [" + newProp.getUserName()
                     + ',' + propName + ',' + CATEGORY + ',' + context
                     + ',' + newValue + "] will be inserted");
            }
         }
         else
         {
            if (isNew && newValue == null)
               continue;
            PSPersistentProperty propObj = getProperty(propName);
            if (newValue == null)
            {
               propObj.setExtraParam(PSPersistentPropertyManager.DELETE);
               propObj.setValue(newValue);
               
               if (ms_log.isDebugEnabled())
               {
                  ms_log.debug("Persistent property [" + propName
                        + "] will be deleted");
               }
            }
            else if (!propObj.getValue().equals(newValue))
            {
               propObj.setExtraParam(PSPersistentPropertyManager.UPDATE);
               propObj.setValue(newValue);
               
               if (ms_log.isDebugEnabled())
               {
                  ms_log.debug("Persistent property [" + propName
                        + "] will be updated");
               }
            }
         }
      }
   }

   /**
    * Gets <code>PSPersistentProperty</code> object from m_usrMeta.
    * @param propName property name
    * @return <code>null</code> if not found
    */
   private PSPersistentProperty getProperty(String propName)
   {
      for(Iterator itr = m_usrProp.iterator(); itr.hasNext(); )
      {
         PSPersistentProperty propObj =
                       (PSPersistentProperty)itr.next();
         String name = propObj.getName();
         if (name != null)
            if (name.equals(propName))
               return propObj;
       }
       return null;
   }

   /**
    * Checks if <code>PSPersistentProperty</code> object by a property name
    * exists or not.
    * @param propName
    * @return <code>true</code> if new porperty else <code>true</code>.
    */
   @SuppressWarnings("unchecked")
   private boolean isNewProperty(String propName)
   {
      PSPersistentProperty prop = getProperty(propName);
      if (prop == null)
      {
         if (ms_log.isDebugEnabled())
         {
            ms_log.debug("Persistent property [" + propName + "] does not exist");
            ms_log.debug("Current persistent properties:");
            for(Iterator iter = m_usrProp.iterator(); iter.hasNext(); )
            {
               PSPersistentProperty propObj =
                             (PSPersistentProperty)iter.next();
               ms_log.debug(propObj.getName());
            }
         }
         
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    *
    * @param name of the property value to be cleared.
    * @return value cleared corresponding to the key supplied. <code>null</code>
    * if there is no mapping for key, or if the key had already been cleared.
    *
    * @throws IllegalArgumentException if property name is <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public synchronized Object clearSessionObject( String name )
   {
      loadPersistentProperties();
      if (name == null || name.length() == 0)
         return new IllegalArgumentException("Key has to be supplied");

      // We have to know if the property has been deleted; insert a null value
      // to indicate that the property had existed and is being deleted.
      if (m_systemObjects.containsKey(name))
      {
         Object obj = m_systemObjects.get(name);
         m_systemObjects.put(name, null);
         return obj;
      }
      else
         return null;
   }

   /**
    * Sets the specified key-value mapping.
    * @param name property name; must not be <code>null</code>
    * @param value property value; may be <code>null</code>
    * @return property value set.
    * @throws IllegalArgumentException if property name is <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public synchronized Object setSessionObject( String name, Object value )
     throws  IllegalArgumentException
   {
      loadPersistentProperties();
      if (name == null || name.length() == 0)
         return new IllegalArgumentException("Key has to be supplied");

      if (value instanceof String)
      {
         if (value == null || ((String)value).length() == 0)
            value = null;
      }
      return m_systemObjects.put(name, value);
   }

   /**
    * @param name key for which the property value is being fetched.
    * @param defaultValue
    * @return the default value if the mapping for the key doesn not exist.
    * @throws IllegalArgumentException if property name is <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public synchronized Object getSessionObject(
         String name,
         Object defaultValue ) throws IllegalArgumentException
   {
      loadPersistentProperties();
      if (name == null || name.length() == 0)
         return new IllegalArgumentException("Key has to be supplied");
      String value = (String)m_systemObjects.get(name);
      if ( value == null)
      {
         m_systemObjects.put(name, defaultValue);
         return defaultValue;
      }
      else
         return value;
   }
   
   /**
    * Empty all authenticated user entries. Not exposed outside of this package.
    *
    */
   public void clearAuthenticatedUserEntries()
   {
      m_UserEntries.clear();
   }

   public synchronized Collection getUserPropertyMetadata() {

      if(m_usrMeta == null){
         loadPersistentProperties();
      }
      return m_usrMeta;
   }

   public synchronized void setUserPropertyMetadata(Collection m_usrMeta) {
      this.m_usrMeta = m_usrMeta;
   }

   public synchronized Collection getUserProperties() {
      if(m_usrProp==null){
         loadPersistentProperties();
      }
      return m_usrProp;
   }

   public synchronized void setUserProperties(Collection m_usrProp) {
      this.m_usrProp = m_usrProp;
   }

   /**
    * Ensures that the persistent property cache is loaded.
    *
    * <p><em>NOTE:</em> This method must be called only from
    * <code>synchronized</code> methods. Since the method is private,
    * it should be easy to verify this.
    */
   @SuppressWarnings("unchecked")
   private synchronized void loadPersistentProperties()
   {
      if (m_UserEntries.isEmpty() || m_isLoaded)
         return;

      PSPersistentPropertyManager mgr =
         PSPersistentPropertyManager.getInstance();
         
      m_usrMeta = mgr.getPersistedPropertyMeta(CATEGORY, this, ALL);
      m_usrProp = mgr.getPersistedProperty(CATEGORY, this, ALL);
      
      Iterator properties = m_usrProp.iterator();
      while (properties.hasNext())
      {
         PSPersistentProperty prop = (PSPersistentProperty) properties.next();
         
         String context = prop.getContext();
         if (context == null)
            continue;
            
         if (context.equals(SYSTEM))
            m_systemObjects.put(prop.getName(), prop.getValue());
         if (context.equals(PRIVATE))
            m_privateObjects.put(prop.getName(), prop.getValue());
      }
      
      verifyLanguage();
      
      m_isLoaded = true;
   }

   /**
    * Verifies that the currently persisted language is still valid and 
    * enabled. If not valid or enabled anymore, the language is set as 
    * follows: to <code>en-us</code> if that language is available and enabled
    * or to the first enabled language found otherwise.
    */
   @SuppressWarnings("unchecked")
   private void verifyLanguage()
   {
      Object value = m_privateObjects.get(IPSHtmlParameters.SYS_LANG);
      if (value instanceof String)
      {
         String strValue = (String) value;
         String newValue = null;
         try
         {
            IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
            PSLocale l = mgr.findLocaleByLanguageString(strValue);
            if (l == null || l.getStatus() != PSLocale.STATUS_ACTIVE)
            {
               l = mgr.findLocaleByLanguageString("en-us");
               if (l != null && l.getStatus() != PSLocale.STATUS_ACTIVE)
               {
                  newValue = "en-us";
               }
               else
               {
                  Collection active = mgr.findLocaleByStatus(PSLocale.STATUS_ACTIVE);
                  if (active.size() > 0)
                  {
                     l = (PSLocale) active.iterator().next();
                     newValue = l.getLanguageString();
                  }
                  else
                  {
                     throw new Exception("No locales found");
                  }
               }
            }
         }
         catch (Exception e)
         {
            // Fallback
            newValue = "en-us";
         }
         
      
         if (newValue != null)
            m_privateObjects.put(IPSHtmlParameters.SYS_LANG, newValue);
      }
   }
   
   /**
    * Determines the original host/port/protocol. If the {@link #}
    * exists in the session we parse it to get the host/port/protocol as this 
    * should provide the most accurate original host/port/protocol info.
    * If it does not exist we grab the info from the servlet request.
    * @param request the <code>PSRequest</code> object, 
    * assumed not <code>null</code>.
    */
   private void determineOriginalHostPortProtocol(PSRequest request)
   {      
      try
      {         
         String redirect = request.getParameter(IPSHtmlParameters.SYS_REDIRECT);
         if (StringUtils.isNotBlank(redirect))
         {
            URL rUrl = new URL(redirect);
            m_originalHost = rUrl.getHost();
            m_originalPort = rUrl.getPort();
            m_originalProtocol = rUrl.getProtocol();
            if(m_originalPort == -1)
               m_originalPort = m_originalProtocol.equals(PROTOCOL_HTTPS) ? 443 : 80;
         }
      }
      catch(MalformedURLException ignore){/* This should not happen */}
      finally
      {
         if(StringUtils.isBlank(m_originalHost))
         {
            m_originalHost = request.getServletRequest().getServerName();
            m_originalPort = request.getServletRequest().getServerPort();
            if (request.getServletRequest().isSecure())
            {
               m_originalProtocol = PROTOCOL_HTTPS;
            }
         }
      }
      
   }

   /**
    * Returns the next session number, and increments the "next number"
    * static field.
    *
    * @return Next session number, starting from 0.
    */
   static synchronized int getNextSessionNumber()
   {
      return ms_nextSessionNumber++;
   }

   /**
    * Session as category.
    */
   public static final String CATEGORY = "sys_session";

   /**
    * Tag used for values in the system map
    */
   private static final String SYSTEM = "system";

   /**
    * Tag used for values in the private map
    */
   private static final String PRIVATE = "private";

   /**
    * SQL wildcards
    */
   public static final String ALL = "%";
   public static final String ONE = "_";

   /**
    * Stores <code>PSPersistentPropertyMeta</code> objects returned
    * by PSPersistentPropertyManager. <code>null</code> until <code>m_isLoaded
    * </code> is <code>true</code>, then never <code>null</code> after that.
    */
   private volatile Collection m_usrMeta = null;

   /**
    * Stores <code>PSPersistentProperty</code> objects returned
    * by PSPersistentPropertyManager and those set on the session object.
    * <code>null</code> until <code>m_isLoaded</code> is <code>true</code>,
    * then never <code>null</code> after that.
    */
   private volatile Collection m_usrProp = null;

   /**
    *  Maintains system session properties
    */
   private ConcurrentHashMap  m_systemObjects = new ConcurrentHashMap();

  /**
    * The designer session cookie
    */
   public static final String    DESIGNER_SESSION_COOKIE = "psdsessid";

   /**
    * The standard session cookie
    */
   public static final String    SESSION_COOKIE          = "pssessid";

   /**
    * Session id.  Initialized at construction, never modified after
    * that.
    */
   private String    m_id;

   /**
    * The time this session was created
    */
   private final Date      m_createTime = new Date();

   /**
    * The time this session went idle. This can be used to determine when
    * the session should timeout (it is null if the session is being used)
    */
   private volatile long m_idleFrom = System.currentTimeMillis();

   /**
    * Store the users the requestor has authenticated as.
    * <p>Accessing methods must <code>synchronize</code> on <code>this</code>
    * to ensure thread safety.  Since the field is private,
    * it should be easy to verify this.
    */
   private CopyOnWriteArrayList      m_UserEntries = new CopyOnWriteArrayList();

   /**
    * Store the back-end credentials for this user with
    * key = driver/server and value = String[] { login id, login pw }
    * <p>Accessing methods must <code>synchronize</code> on <code>this</code>
    * to ensure thread safety. Since the field is private,
    * it should be easy to verify this.
    */
   private ConcurrentHashMap   m_Credentials = new ConcurrentHashMap();

   /**
    * Table of database ids.
    *
    * <p>Accessing methods must <code>synchronize</code> on <code>this</code>
    * to ensure thread safety.  Since the field is private,
    * it should be easy to verify this.
    */
   private ConcurrentHashMap     m_dbIds = new ConcurrentHashMap();

   /**
    * Table of database authentications.
    * <p>Accessing methods must <code>synchronize</code> on <code>this</code>
    * to ensure thread safety.  Since the field is private,
    * it should be easy to verify this.
    */

   private Map     m_authentications = new ConcurrentHashMap();

   /**
    * Allows extensions to store user session information in a map.
    * <p>Accessing methods must <code>synchronize</code> on <code>this</code>
    * to ensure thread safety.  Since the field is private,
    * it should be easy to verify this.
    */
   private ConcurrentHashMap  m_privateObjects = new ConcurrentHashMap();

   /**
    * Flag to mark this session as designer session (<code>true</code>). The
    * default is set to <code>false</code>.
    */
   private boolean m_isDesignerSession = false;

   /**
    * Storage for the originating host provided with the request this object
    * was created with. Initialized during construction, never
    * <code>null</code> after that.
    */
   private String m_originalHost = null;

   /**
    * Storage for the originating port provided with the request this object
    * was created with. Initialized during construction.  Defaults to 80 if no
    * port is specified with the request.
    */
   private int m_originalPort = 80;

   /**
    * The server protocol with which the request that originated this object
    * was created. Initialized during construction, never modified after that,
    * and never <code>null</code>.
    */
   private String m_originalProtocol = PROTOCOL_HTTP;

   /**
    * Specifies whether system and private maps have been
    * loaded. <code>true</code>if loaded; else <code>false</code>
    */
   private volatile boolean m_isLoaded = false;

  /**
    * A unique number used when we create a new session id,
    * monotonically incremented each time an id is created.
    * Access to this must be done <em>only</em> via
    * {@link #getNextSessionNumber()}
    */
   private static int ms_nextSessionNumber = 0;

   /**
    * String constant for HTTP protocol
    */
   public static final String PROTOCOL_HTTP = "http";

   /**
    * String constant for HTTPS protocol
    */
   public static final String PROTOCOL_HTTPS = "https";

   /**
    * Name of the internal request to get the user communities.
    */
   private static final String IREQ_USERCOMMUNITIES =
      "sys_commSupport/usercommunities";

   /**
    * Name of the element "Community" in the result document of the internal
    * request for user communities.
    */
   private static final String ELEM_COMMUNITY = "Community";

   /**
    * Name of the attribute of the communityid of the element "Community" in
    * the result document of the internal request for user communities.
    */
   private static final String ATTR_COMMID = "commid";

   /**
    * Name of the internal request to get the community id with a
    * community name. Requires parameter communityname=value, where value is
    * a valid community name.
    */
   private static final String IREQ_COMMUNITYLOOKUP =
      "sys_commSupport/communityidlookup";
   /**
    * Name of the parameter requires for community id lookup. This
    * parameter is added when we lookup the community id.
    */
   private static final String COMMUNITYNAME = "communityname";
   
   
   /**
    * Logger for this class.
    */
   private static final Log ms_log = LogFactory.getLog(PSUserSession.class);
   
   /**
    * The name used to store a client id as session private object.
    */
   public static final String CLIENTID = "clientId";
   
   
   /**
    * The name used to store the user's community ids as session private object.
    */
   public static final String USER_COMMUNITIES = "sys_userCommunities";
   
   /**
    * The name used to store the user's community names as session private 
    * object.
    */
   public static final String USER_COMMUNITY_NAMES = "sys_userCommunityNames";     
  
}
