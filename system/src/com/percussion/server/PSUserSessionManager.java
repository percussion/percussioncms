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

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.design.objectstore.server.IPSServerConfigurationListener;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.commons.lang.Validate.isTrue;

/**
 * The PSUserSessionManager class provides access to user sessions. Sessions
 * are used to associate various attributes with users logged in to the
 * system.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUserSessionManager extends Thread
   implements IPSServerConfigurationListener
{
   /**
    * Not for external construction.
    */
   private PSUserSessionManager()
   {
      this.setName("User Session Manager");
      this.setDaemon(true);
   }
   
   private static IPSCmsObjectMgr objMgr = null;

   /**
    * Initialize the user session manager.
    *
    * @param   config      the server configuration defining session setup
    * @param os the objectstore handler to add us to the server listener
    */
   public static synchronized void init(PSServerConfiguration config,
                                        IPSObjectStoreHandler os)
         throws PSAuthorizationException
   {
      if (ms_SessionManager != null)
         throw new IllegalStateException(
               "This object can only be instantiated once.");

      // create and initialize the one and only object
      ms_SessionManager = new PSUserSessionManager();
      fromServerConfiguration(config);

      /*
      register the session manager as a server listener to pick up server
      configuration changes.
      */
      os.addServerListener(ms_SessionManager);

      ms_stopped = false;
      ms_SessionManager.start();
   }


   /**
    * A thread to manage the user sessions -- that is, remove them when the
    * timeout is expired.
    */
   public void run()
   {
      while (!ms_stopped)
      {
         validateSessions();
      }
   }

   /**
    * Loops through the user sessions once.  Any session whose idle time
    * has exceeded the configured timeout is deleted.  Additionally,
    * if we've exceeded the total number of allowed sessions, collects lists
    * of anonymous and credentialed sessions.  Starting from the oldest
    * sessions in each category, deletes enough anonymous
    * sessions, then credentialed sessions, to bring us down under the session
    * limit.
    */
   @SuppressWarnings("unchecked")
   private void validateSessions()
   {
      long idledTimeMS = 0;      // in milliseconds

      /*
      * The minimum sleep time of this thread. This is to make sure we are
      * not run every millisecond or so. A 1 second resolution of this
      * functionality seems to be appropriate.
      */
      long minimumSleep = 1000;
      long sleepTimeMS = ms_userTimeout;
      long shortestSleep = 0;

      try
      {
            HashMap<String,PSUserSession> usersToCleanup = new HashMap<String,PSUserSession>();
          
            
             
            long requestTimeout = ms_userTimeout;
            shortestSleep = requestTimeout;

            Iterator sessionIter = ms_Sessions.values().iterator();
            boolean collectReleases =
                  ms_Sessions.size() > ms_maxOpenUserSessions;

            TreeMap anonymous = null;
            TreeMap authenticated = null;
         
            if (collectReleases)
            {
               // only need these objects if we will be collecting idle sessions
               anonymous = new TreeMap();
               authenticated = new TreeMap();
            }
           
            HashSet<String> userPreserveSet = new HashSet<String>();
            while (sessionIter.hasNext())
            {
               PSUserSession sess = (PSUserSession) sessionIter.next();
               String user = sess.getRealAuthenticatedUserEntry();
               
               long idledAt = sess.getIdleSince();

            
               idledTimeMS = System.currentTimeMillis() - idledAt;

               if ((idledTimeMS > DESIGNER_TIMEOUT
                    && sess.isDesignerSession()) ||
                   (idledTimeMS > requestTimeout &&
                    !sess.isDesignerSession()))
               {
                     
                  sessionIter.remove();
                  usersToCleanup.put(user,sess);
                  cleanUpSession(sess);

               }
               else
               {
                  // User still has other sessions open so prevent force checkin
                  // Does not matter if user session was added and is picked up here before our
                  // poll operation.  Just means user has a new session so we can skip the checkin
                  if (user!=null)
                     userPreserveSet.add(user);
                  
                  sleepTimeMS = shortestSleep - idledTimeMS;
                 
                  if (sleepTimeMS < shortestSleep)
                     shortestSleep = sleepTimeMS;

                  if (collectReleases)
                  {
                     if (sess.hasAuthenticatedUserEntries())
                     {
                        if (!sess.isDesignerSession())
                           authenticated.put(idledAt, sess);
                     }
                     else
                        anonymous.put(idledAt, sess);
                  }
               }
               
            }   // end of while

           
            if (collectReleases)
            {
               // first remove anonymous sessions, then authenticated sessions
               // to get the number of open sessions below the threshold
               int maxSessions = ms_maxOpenUserSessions - ms_releaseOffset;
               while (!anonymous.isEmpty() && ms_Sessions.size() > maxSessions)
               {
                  log.warn("Too many open sessions releasing unauthenticated");
                  releaseUserSession((PSUserSession) anonymous.remove(
                        anonymous.firstKey()));
               }

               while (!authenticated.isEmpty() &&
                      ms_Sessions.size() > maxSessions)
               {
                  log.warn("Too many open sessions releasing authenticated");
                  releaseUserSession((PSUserSession) authenticated.remove(
                        authenticated.firstKey()));
               }
            }

            if (shortestSleep < minimumSleep)
               shortestSleep = minimumSleep;

            if (shortestSleep > ms_maximumSleep)
               shortestSleep = ms_maximumSleep;
            
            // Add sessions removed by log out last to use these session ids instead of an expired sessionid for force checkin
            PSUserSession ruser = null;
            
            // pull released users from queue.
            while ( (ruser = releasedUsers.poll()) != null) 
               usersToCleanup.put(ruser.getRealAuthenticatedUserEntry(), ruser);
            
            // After we have calculated user sessions that may be removed.  We save any users that have remaining active sessions
            for (String user : userPreserveSet)
            {
               usersToCleanup.remove(user);
            }
        
            forceCheckinUserContent(usersToCleanup);
         
      }
      catch (Throwable t)
      {
         // make sure this never dies unexpectedly
         PSConsole.printMsg("UserSessionManager", t);
      }
      
      try {
         Thread.sleep(shortestSleep);
      }
      catch (InterruptedException e)
      {
         // just fall through
      }
    
   }


   private void forceCheckinUserContent(HashMap<String, PSUserSession> usersToCleanUp)
   {
     
      
      log.debug("Checking in content for logged out users "+usersToCleanUp);
      if (usersToCleanUp.size()>0)
      {
         if (objMgr==null)
            objMgr = PSCmsObjectMgrLocator.getObjectManager();
         objMgr.forceCheckinUsers(usersToCleanUp);
      }
         
   }


   /**
    * shutdown the user session manager, clearing all sessions.
    */
   public static void shutdown()
   {
      ms_flags = F_NONE;
      ms_Sessions.clear();

      ms_stopped = true;
   }


   /**
    * Get the session associated with the specified request.
    *
    * @param      request     the request object to locate the session for
    *
    * @return                 the session object (which may be a temporary
    * session object)
    */
   @SuppressWarnings("unchecked")
   public synchronized static PSUserSession getUserSession(PSRequest request)
   {
      isTrue( ! ms_stopped , "Cannot get user session because session manager has been stopped");
      PSUserSession sess = null;

      String sessId = PSUserSession.getIdFromRequest(request);

         if (sessId != null)
            sess = getUserSession(sessId);

         if (sess == null)
         {
            if(request.m_reqPage != null && request.m_reqPage.contains("/cm/gadgets")){
               return sess;
            }
            /* we always create a new ID if we have to create a new session unless
            * a designer client is trying to connect and has a session id -
            * then we reuse the supplied id so they can still use their locks.
            */
            if ((sessId == null) || (!isDesignerRequest(request)))
               sessId = PSUserSession.createSessionId(request);

         /*
            we always create a session, but if there is no real session info
            or user sessions are disabled, we don't add the created session to
            our persistent list.
            we always add it to this list if its a designer connection.
         */
            sess = new PSUserSession(request, sessId);

            if ((sessId != null) &&
                (areSessionsEnabled() || isDesignerRequest(request)))
               ms_Sessions.put(sessId, sess);
         }
     
  
         sess.touchIdle();
 
     
      return sess;
   }

   /**
    * Get the user session timeout.
    * 
    * @return the time after which a user session expires in milli seconds.
    */
   public static long getUserSessionTimeout()
   {
      return ms_userTimeout;
   }

   /**
    * Are sessions enabled overall.
    *
    * @return <code>true</code> if they are enabled, <code>false</code>
    *    otherwise.
    */
   public static boolean areSessionsEnabled()
   {
      return ((ms_flags & F_ENABLED) != 0);
   }


   /**
    * Determines if user has an existing session.
    *
    * @param      request     the request object to locate the session for
    *
    * @return <code>true</code> if there is an existing session,
    * <code>false</code> otherwise.
    */
   public static boolean doesSessionExist(PSRequest request)
   {
      PSUserSession sess = null;

      String sessId = PSUserSession.getIdFromRequest(request);
      if (sessId != null)
         sess = getUserSession(sessId);

      return (sess != null);
   }


   /**
    * Is the provided request a designer (or admin) request.
    *
    * @param request the request to test, if <code>null</code> is provided,
    *    <code>false</code> is returned.
    * @return <code>true</code> if this is a designer request,
    *  <code>false</code> otherwise.
    */
   public static boolean isDesignerRequest(PSRequest request)
   {
      if (request != null)
      {
         String type =
               request.getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE);
         if (type != null)
         {
            type = type.toLowerCase();

            if (type.startsWith("admin") || type.startsWith("design"))
               return true;
         }
      }

      return false;
   }


   /**
    * Is the provided request an attmept to connect from the designer
    * connection?
    *
    * @param request the request to test, if <code>null</code> is provided,
    *    <code>false</code> is returned.
    * @return <code>true</code> if this is a designer request to connect,
    * <code>false</code> otherwise.
    */
   public static boolean isDesignerConnectRequest(PSRequest request)
   {
      if (request != null)
      {
         String type =
               request.getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE);
         if (type != null)
         {
            if (type.equalsIgnoreCase("design-open"))
               return true;
         }
      }

      return false;
   }

   /**
    * This API checks against the maximum number of users allowed in the system
    * if logged in users already reached the maximum allowed, then returns false
    * @return
    */
   public synchronized static boolean checkIfNewUserAllowed()  {
      String maxUserStr = PSServer.getProperty(MAX_USERS_ALLOWED,"0");
      int maxUser = 0;
      try {
         maxUser = Integer.parseInt(maxUserStr);
      }catch (Exception e){
         maxUser = 0;
      }
      if(maxUser == 0) {
         return true;
      }else {
         Iterator<PSUserSession> sessionItr = ms_Sessions.values().iterator();

         while (sessionItr.hasNext()){
            PSUserSession sess = (PSUserSession) sessionItr.next();
            String user = sess.getRealAuthenticatedUserEntry();
            if(user != null){
               //checking for 0 here because there is a default rxserver user in teh system.
               if(maxUser == 0){
                  return false;
               }
               maxUser =  maxUser -1;
            }

         }
         return true;
      }
   }




   /**
    * Get the session associated with the specified session id.
    *
    * @param      sessId      the session id
    *
    * @return                 the session object or <code>null</code> if
    *                         the requested session does not exist
    */
   public static PSUserSession getUserSession(String sessId)
   {
      isTrue( ! ms_stopped , "Cannot get user session because session manager has been stopped");
      if (sessId == null)
         return null;

      PSUserSession sess = (PSUserSession) ms_Sessions.get(sessId);
         if (sess != null)
            sess.touchIdle();
      return sess;
   }


   /**
    * Release the specified user session. This removes it from the
    * list of active sessions.
    *
    * @param sess      the session to release
    */
   public static void releaseUserSession(PSUserSession sess)
   {
      if (sess != null)
      {
         // need to call cleanup before removing the session in case anyone 
         // needs to be able to get the actual session object.
         cleanUpSession(sess);
         String user = sess.getRealAuthenticatedUserEntry();
         if (user!=null)
            releasedUsers.add(sess);
         ms_Sessions.remove(sess.getId());

      }
   }

   /**
    * Does any necessary cleanup to close down a user session.  Assumes that the 
    * session has not yet been removed from the cache.
    *
    * @param sess The user session to close down; assumed non-<code>null</code>
    */
   private static void cleanUpSession(PSUserSession sess)
   {
      PSCacheManager.getInstance().flushSession(sess.getId());
   }

   /**
    * This initializes/resets all settings from the provided server
    * configuration to its local settings. If <code>null</code> is provided
    * the defaults are set.
    *
    * @param config the server configuration, may be <code>null</null>
    */
   private static void fromServerConfiguration(PSServerConfiguration config)
   {
      if (config == null)
      {
         ms_flags = F_NONE;
         ms_userTimeout = 1000 * 900; // in milliseconds
      }
      else
      {
         if (config.isUserSessionEnabled())
            ms_flags = F_ENABLED;
         else
            ms_flags = F_NONE;

            // ms_userTimeout is measured in milliseconds
            ms_userTimeout = 1000 * config.getUserSessionTimeout();
            ms_maxOpenUserSessions = config.getMaxOpenUserSessions();
            ms_releaseOffset = ms_maxOpenUserSessions / 4;
            ms_maximumSleep =
               ms_maxOpenUserSessions /
                  PSServerConfiguration.MINIMAL_REQUIRED_OPEN_SESSIONS * 1000;
      }
   }


   /**
    * Implementation for the IPSServerConfigurationListener interface. If
    * <code>null</code> is provided we will keep the existing settings.
    *
    * @see
    * com.percussion.design.objectstore.server.IPSServerConfigurationListener
    */
   public void configurationUpdated(PSServerConfiguration config)
   {
      if (config != null)
      {
         fromServerConfiguration(config);
      }
   }

   
   /**
    * Get the user session manager status. This includes information about the
    * session manager thread as well as about all open sessions at the time
    * this method is called.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @param full <code>true</code> to request a full status, <code>false</code>
    *    for a summary status only.
    * @return the element containing all user session manager status
    *    information, never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public static Element getUserSessionManagerStatus(Document doc, boolean full)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      int anonymousCount = 0;
      int authenticatedCount = 0;
      int designerCount = 0;
      Element anonymous = doc.createElement("Anonymous");
      Element authenticated = doc.createElement("Authenticated");
      Element designer = doc.createElement("Designer");
      Integer sessionCount = null;

      Iterator sessionIter = ms_Sessions.values().iterator();
      while (sessionIter.hasNext())
      {
         PSUserSession sess = (PSUserSession) sessionIter.next();
         if (sess.hasAuthenticatedUserEntries())
         {
            if (sess.isDesignerSession())
            {
               ++designerCount;
               if (full)
                  designer.appendChild(sess.getUserSessionStatus(doc));
            }
            else
            {
               ++authenticatedCount;
               if (full)
                  authenticated.appendChild(sess.getUserSessionStatus(doc));
            }
         }
         else
         {
            ++anonymousCount;
            if (full)
               anonymous.appendChild(sess.getUserSessionStatus(doc));
         }
      }
      sessionCount = new Integer(ms_Sessions.size());
      
      anonymous.setAttribute("count", Integer.toString(anonymousCount));
      authenticated.setAttribute("count", Integer.toString(authenticatedCount));
      designer.setAttribute("count", Integer.toString(designerCount));

      Element sessions = doc.createElement("UserSessions");
      sessions.setAttribute("open", sessionCount.toString());
      sessions.appendChild(anonymous);
      sessions.appendChild(authenticated);
      sessions.appendChild(designer);

      Element manager = doc.createElement("UserSessionManager");
      manager.setAttribute("isAlive",
         ms_SessionManager.isAlive() ? "yes" : "no");
      manager.setAttribute("enabled", areSessionsEnabled() ? "yes" : "no");
      manager.setAttribute("sessionTimeout", ms_userTimeout + "ms");
      manager.setAttribute("maximalOpenUserSessions",
         new Integer(ms_maxOpenUserSessions).toString());
      manager.appendChild(sessions);

      return manager;
   }

   private static final int F_NONE = 0;
   private static final int F_ENABLED = 0x01;

   /**
    * Singleton instance of the session manager.  Set at init time;
    * never <code>null</code> after that.
    */
   private static PSUserSessionManager ms_SessionManager = null;

   /**
    * Single HashMap for holding user sessions.  All access to
    *  <code>ms_Sessions</code> is synchronized on <code>
    * m_sessionMonitor</code>
    */
   private static ConcurrentHashMap<String, PSUserSession> ms_Sessions = new ConcurrentHashMap<String, PSUserSession>();
   
   private static ConcurrentLinkedQueue<PSUserSession> releasedUsers = new ConcurrentLinkedQueue<PSUserSession>();
   
   private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
   private static final Lock readLock = readWriteLock.readLock();
   private static final Lock writeLock = readWriteLock.writeLock();
   
   private static volatile int ms_flags = F_NONE;
   private static volatile long ms_userTimeout = 0;
   /**
    * The session timeout for designer sessions is fix 12 hours. This is
    * longer then the request timeout since designer sessions are typically
    * open longer than request sessions. The value is specified in milliseconds.
    */
   private static final long DESIGNER_TIMEOUT = 1000 * 60 * 60 * 12;
   /**
    * Defines the maximal allowed open user sessions. If the number of open
    * sessions exceeds this number we will start releasing the anonymous
    * sessions with the longest idle time. If thats still not enough we do the
    * same with authenticated sessions. Designer connections are excluded
    * from this algorithm.
    */
   private static volatile int ms_maxOpenUserSessions =
      PSServerConfiguration.DEFAULT_OPEN_SESSIONS;
   /**
    * Releasing user sessions is an expensive process. Therefor we release more
    * than just the number above the maximal open user sessions. This number
    * specifies how much more we release.
    */
   private static volatile int ms_releaseOffset = ms_maxOpenUserSessions / 4;
   /**
    * The maximum number of requests the Rhythmyx server is able to handle in
    * any case.
    */
   public static final int MAX_REQUESTS_PER_SECOND = 120;
   /*
    * The maximum sleep time of this thread. Assuming we handle around 120
    * requests per second maximum and do not want to have more than n
    * open user sessions, the maximum timeout should not exceed n/120
    * seconds.
    */
   private static volatile long ms_maximumSleep =
      ms_maxOpenUserSessions / MAX_REQUESTS_PER_SECOND * 1000;

   /**  #################################################################
    # The property to define Maximum no of users that can login in CMS at the same time.
    #  0 - unlimited
    #################################################################
    **/
   private final static String MAX_USERS_ALLOWED = "maxUsersAllowed";
   /**
    * Set this flag to <code>true</code> to stop the user session manager.
    */
   private static volatile boolean ms_stopped = false;

   private static Log log = LogFactory.getLog(PSUserSessionManager.class);
}


