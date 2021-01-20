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
package com.percussion.search;

import com.percussion.search.lucene.PSSearchQueryImpl;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class is the primary class for a pluggable-search-engine architecture.
 * This 'virtual' search engine may run in the same process or in a client-
 * server mode, this is implementation dependent.
 * <p>The search engine knows about Rhythmyx's units of content, which are the
 * item and its complex children. Each of these are treated as individual units 
 * by the engine when submitting data. However, when data is queried, only the
 * content item is returned in the result set. i.e. a query that matches a child
 * entry causes the parent to be returned in the result set.
 * <p>A search engine is configured at start up and can be reconfigured (and
 * restarted) at nearly any time.
 * <p>There are 3 additional 'interfaces' that make up the entire framework.
 * Instances of each of these can be obtained from this class. The 3 other types
 * are:
 * <ol>
 * <li>{@link PSSearchIndexer}</li>
 * <li>{@link PSSearchQuery}</li>
 * <li>{@link PSSearchAdmin}</li>
 * </ol>
 * <p>The indexer is used to submit content to the engine. The query is used to
 * get results from the engine. Finally, the admin is used to configure the 
 * engine for the different content types and manage indexes.
 * <p>This class should be used in the following manner:
 * <ol>
 * <li>Call {@link #getInstance(Properties)} to initialize the engine</li>
 * <li>Call {@link #getSearchAdmin()} and configure and validate the engine  for
 * the current content editors</li>
 * <li>Call {@link #start()} to activate the engine</li>
 * <li>Submit 'units of content' and perform queries</li>
 * <li>Change the configuration as content editors are modified. {@link
 * #restart()} must be called before the  configuration
 * changes will take effect.</li>
 * <li>When finished, call {@link #shutdown(boolean) shutdown}</li>
 * <ol>
 * <p>Note to implementers of plugins:
 * <p>There are a number of methods of the form "doXXX". These methods are
 * equivalent to XXX, but don't need to check for a running server as that
 * has already been done by this class.
 * @see PSSearchIndexer
 * @see PSSearchQuery
 * @see PSSearchAdmin
 * 
 * @author paulhoward
 */
public abstract class PSSearchEngine
{
   private static final CountDownLatch latch = new CountDownLatch(1);
  
   /**
    * The name used for the search engine subsystem in console messages.
    */
   public static final String SUBSYSTEM_NAME = "Search";

   /**
    * A property by this name must be included in the initial configuration.
    * Its value must be the fully qualified name of a class extending this
    * one and it must have a parameterless ctor. The name is case in-sensitive.
    */
   public static final String PROP_CLASSNAME = "className";

   /* NOTE: if you modify any STATUS_xxx constant, you must make sure the
    * STATUS_STRINGS array is synchronized with those changes.*/

   /**
    * Value returned by <code>getStateCode</code> after {@link 
    * #getInstance(Properties)} is called but before <code>start</code> 
    * is called.
    */
   public static final int STATUS_INITIALIZED = 1;
   
   /**
    * Value returned by <code>getStateCode</code> after initialization 
    * and {@link #start()} has been called. This is the only state in which
    * the search engine will process requests.
    */   
   public static final int STATUS_RUNNING = 2;

   /**
    * Value returned by <code>getStateCode</code> after {@link 
    * #shutdown(boolean)} has been called but before it has finished.
    */   
   public static final int STATUS_TERMINATING = 3;

   /**
    * Value returned by <code>getStateCode</code> after {@link 
    * #shutdown(boolean)} has successfully finished processing. 
    */   
   public static final int STATUS_TERMINATED = 4;

   /**
    * Value returned by <code>getStateCode</code> after {@link 
    * #pause(long, String)} has been called but before {@link #unPause()} has
    * been called. 
    */   
   public static final int STATUS_PAUSED = 5;
   
   /**
    * This method must be called before any other method. It creates the 
    * singleton based on parameter(s) in the supplied config. The derived 
    * class must contain a ctor that takes no parameter.
    * <p>The values in the config are available to the derived class using 
    * the {@link #getProperty(String,String) getProperty} method of this
    * class.
    * <p>If a class by the supplied name cannot be found, or it does not
    * extend this class, an exception is thrown.
    * <p>If this method is called more than once w/o an intervening call to
    * {@link #shutdown(boolean)}, an exception is thrown.
    * 
    * @param config Never <code>null</code>. Must contain at least one property
    * called {@link #PROP_CLASSNAME}. Additional 
    * properties are made available to the derived class. All property names
    * are case-insensitive.
    * <p>A copy of this set is made so that subsequent changes to the
    * supplied config will not affect the engine.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSSearchException If required properties are missing or the 
    * class cannot be instantiated.
    * 
    * @throws IllegalStateException If called after it has successfully 
    * returned once and hasn't been shut down. 
    */
   public synchronized static PSSearchEngine getInstance(Properties config) 
      throws PSSearchException
   {
      
      if (null != ms_instance)
      {
         throw new IllegalStateException(
               "Use getInstance() after the instance is created.");
      }
      
      if (null ==config)
      {
         throw new IllegalArgumentException("config cannot be null");
      }
      
      Enumeration iter = config.propertyNames();
      Properties props = new Properties();
      String className = null;
      while (iter.hasMoreElements())
      {
         String propName = iter.nextElement().toString();
         /*remove this from the props as the implementing class already knows 
          * its name
          */
         if (propName.equalsIgnoreCase(PROP_CLASSNAME))
            className = config.getProperty(propName);
         else if (!propName.startsWith(PSSearchQueryImpl.QUERYPROP_PREFIX))
         {
            /* We do a hack with the custom properties so we can
             * store both engine and query handler props in the same set.
             * Engine props have no prefix, while props targeted at the 
             * query engine have a prefix identified by the constant above.
             * This doesn't belong here, but it's not worth the effort at this
             * time to hide it.
             */
            props.setProperty(propName.toLowerCase(), 
                  config.getProperty(propName));
         }
      }
      if (null == className || className.length() == 0)
      {
         throw new IllegalArgumentException(
               "A config must be supplied with at least the " + PROP_CLASSNAME 
               + " property.");
      }
      ms_props = props;
      
      try
      {
         Class engine = Class.forName(className);
         ms_instance = (PSSearchEngine) engine.newInstance();
         //we use a loop in case multiple instances got started somehow
         int retryCount = 5;
         while (ms_instance.isAvailable(false) && retryCount > 0)
         {
            PrintConsoleMessage("Found running search engine (probably "
                  + "previous instance of Rhythmyx did not shut down cleanly)."
                  + " Shutting it down now.");
            ms_instance.shutdown(true);
            //shutdown() will change the state code and cause problems
            ms_instance.setStateCode(STATUS_INITIALIZED);
            retryCount--;
         }
         if (retryCount == 0 && ms_instance.isAvailable(false))
         {
            PrintConsoleMessage("Failed to shutdown search engine. Manually "
                  + "kill the execd process(es) before attempting to restart "
                  + "the Rhythmyx server.");
            throw new PSSearchException(IPSServerErrors.RAW_DUMP, 
               "Can't start search engine due to previously reported errors.");
         }
      }
      catch (ExceptionInInitializerError eie)
      {
         String [] args =
         {
            className,
            eie.getException().getLocalizedMessage()
         };
         //remap to search exception
         PSSearchException se = new PSSearchException(
               IPSSearchErrors.SEARCH_ENGINE_FAILED_INIT, args); 
         throw se;
      }
      catch (Exception e)
      {
         /*because many exceptions can be thrown and they are treated basically
            the same, we don't break them out in their own catch block*/
         if (e instanceof PSSearchException)
            throw (PSSearchException) e;

         String [] args =
         {
            className,
            e.getLocalizedMessage()
         };
         //remap to search exception
         PSSearchException se = new PSSearchException(
               IPSSearchErrors.SEARCH_ENGINE_FAILED_INIT, args); 
         throw se;
      } finally {
         // unblock calls to getInstance();
         latch.countDown();
      }
     
      return ms_instance;
   }

   /**
    * All search engine classes that need to print to the console should use
    * this method.
    * 
    * @param text The message to display on the Rx console. If <code>null</code>
    * or empty, nothing is printed.
    */
   public static void PrintConsoleMessage(String text)
   {
      if (null != text && text.trim().length() > 0)
         PSConsole.printMsg(SUBSYSTEM_NAME, text);
   }
   
   /**
    * This class implements the singleton pattern. The first call must be 
    * made to the 1 parameter {@link #getInstance(Properties) getInstance}
    * to instantiate the object. From then on, this method should be used.
    * 
    * @return Never <code>null<code>.
    * 
    * @throws IllegalStateException If an instance hasn't been created yet.
    */
   public static PSSearchEngine getInstance()
   {
      try
      {
         // await  initialization
         latch.await(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException(
               "Interrupted PSSearchEngine.getInstance() waiting for initialization",e);
      }
      
      if (null == ms_instance)
      {
         throw new IllegalStateException(
               "Search engine not initialized");
      }
      return ms_instance;
   }
   
   /**
    * If the search engine is not currently running (as reported by 
    * {@link #isAvailable()}, then it is started by calling {@link #restart()}.
    */
   final public void start()
      throws PSSearchException
   {
      if (!isAvailable())
         restart();
   }
   
   /**
    * Convenience method that calls {@link #shutdown(boolean, boolean, String)
    * shutdown(force, <code>true</code>, <code>null</code>)}.
    */
   synchronized final public void shutdown(boolean force)
      throws PSSearchException
   {
      shutdown(force, true, null);
   }
   
   /**
    * Releases any resources associated with the engine and its processing. 
    * If there are any pending updates or queries, this method will wait for
    * a maximal period of time to allow them to complete. After this period,
    * the engine will shut down whether these operations have completed or 
    * not. Any new requests will receive an exception indicating that the 
    * engine is shutting down.  
    * <p>Calling this method on an engine that is not running has no effect.
    * <p>During shutdown, the state is set to <code>STATUS_TERMINATING.</code>
    * When shutdown has finished, the state is set to <code>STATUS_TERMINATED
    * </code>.
    * 
    * @param force If <code>true</code>, any pending update or query requests
    * will not be processed, the engine will terminate immediately. In 
    * general, this should be <code>false</code>.
    * <p>If <code>false</code> and there are objects that have not been 
    * released, an exception is thrown.
    * <p>It is assumed that index events are queued on the Rx side of the 
    * engine and thus if no <code>PSSearchIndexer</code> and <code>PSSearchQuery
    * </code> objects are in use, the engine can safely shut down w/o losing
    * any queued indexing events.
    * 
    * @param checkAdmin If <code>true</code>, the code will wait some period 
    * of time if the admin object is currently in use (it is locked). 
    * Otherwise, this check is skipped. This is for use by the admin object
    * in case it needs to shutdown the server to perform some operation.
    * 
    * @param reason If this shutdown is temporary to perform some operation
    * (such as re-creating indexes), supply a message here that can be 
    * returned to end users that make search queries. It will automatically
    * be cleared when the engine is restarted. Supply <code>null</code>
    * or empty if called while shutting down the Rx server.
    * 
    * @throws PSSearchException If any problems occur. This can be safely
    * ignored. However, you may wish to log it as an exception thrown here
    * may cause problems if {@link #start()} is called again before the
    * server hosting the engine has rebooted. 
    */   
   synchronized final public void shutdown(boolean force, boolean checkAdmin,
         String reason)
      throws PSSearchException
   {
      int status = getStateCode();
      
      if (status == STATUS_TERMINATING || status == STATUS_TERMINATED)
         return;
      try
      {
         if (null != reason && reason.trim().length() > 0)
            m_shutdownMessage = reason;
         pause(60000, "Search engine is shutting down.");
         setStateCode(STATUS_TERMINATING);
         if (checkAdmin && null != m_searchAdminLockedClass)
         {
            PrintConsoleMessage("Waiting " + SHUTDOWN_WAIT/1000 
                  + " seconds for admin to finish.");
            try
            {
               wait(SHUTDOWN_WAIT);
               if (null != m_searchAdminLockedClass)
               {
                  PrintConsoleMessage(
                        "Timed out waiting for admin to be released"
                        + " during search engine shutdown.");
                  force = true;
               }
            }
            catch (InterruptedException ie)
            {
               PrintConsoleMessage(
                     "Interrupted while waiting for admin to be released"
                     + " during search engine shutdown.");
            }
         }
         doShutdown(force);
      }
      finally
      {
         // clean up in preparation for restart
         m_searchAdminLockedClass = null;
         unPause();
         setStateCode(STATUS_TERMINATED);
      }
   }
   
   /**
    * Convenience method that is equivalent to calling {@link 
    * #getSearchAdmin(boolean) getSearchAdmin(false)}.
    */
   public PSSearchAdmin getSearchAdmin()
   {
      int state = getStateCode();
      if (state != STATUS_INITIALIZED && state != STATUS_RUNNING && 
            state != STATUS_PAUSED)
      {
         throw new IllegalStateException("Search engine is not available: " 
               + getStateString());
      }
      return doGetSearchAdmin(false);
   }
   
   
   /**
    * The search configuration handler is a singleton object. It allows the
    * caller to statically or dynamically configure the search engine based on
    * the Rhythmyx content types. This method can be called before the
    * <code>start</code> (or <code>restart</code>) method is called.
    * <p>If a locked admin is obtained, after the user has completed their work, 
    * they must return the object to the engine using the {@link 
    * #releaseSearchAdmin(PSSearchAdmin)} method. Releasing a read-only object
    * is a no-op.
    * <p>You can call {@link #isSearchAdminLocked()} to determine if the admin
    * is available for use.
    * <p>Note: this class uses the class type to differentiate between a read-
    * only admin and a read-write admin, so their class Qnames must differ, but
    * they must derive from the PSSearchAdmin.
    * 
    * @param wantLocked A flag to indicate whether you plan to change the search
    * engine's configuration. Only 1 instance of the locked admin can be
    * obtained at any given time. If <code>false</code>, all modification
    * methods of the returned admin throw an IllegalStateException.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSAdminLockedException If someone has called {@link 
    * #getSearchAdmin(boolean) getSearchAdmin(<code>true</code>)} and hasn't 
    * yet called {@link #releaseSearchAdmin(PSSearchAdmin) releaseSearchAdmin}.
    * @throws IllegalStateException If the server is not initialized or in the
    * process of or completed shutting down.
    */
   synchronized public PSSearchAdmin getSearchAdmin(boolean wantLocked)
      throws PSAdminLockedException
   {
      int state = getStateCode();
      if (state != STATUS_INITIALIZED && state != STATUS_RUNNING && 
            state != STATUS_PAUSED)
      {
         throw new IllegalStateException("Search engine is not available: " 
               + getStateString());
      }
      if (wantLocked && isSearchAdminLocked())
      {
         throw new PSAdminLockedException();
      }
      PSSearchAdmin admin = doGetSearchAdmin(wantLocked);
      if (wantLocked)
      {
         m_searchAdminLockedClass = admin.getClass().getName(); 
      }
      return admin;
   }
   
   /**
    * A flag to indicate whether a call to {@link #getSearchAdmin()} would 
    * succeed or not.
    * 
    * @return <code>true</code> if the call would succeed, <code>false</code>
    * otherwise.
    */
   synchronized public boolean isSearchAdminLocked()
   {
      return null != m_searchAdminLockedClass;
   }
   
   /**
    * After the caller has completed their work, they must pass the admin
    * they received using the {@link #getSearchAdmin(boolean) 
    * getSearchAdmin(true)} method. The admin object will remain unavailable 
    * to other callers until it is released.
    * <p>Calling this method w/ read-only admins is a no-op.
    * 
    * 
    * @param sa Never <code>null</code>.
    */
   synchronized public void releaseSearchAdmin(PSSearchAdmin sa)
   {
      if (null == sa)
      {
         throw new IllegalArgumentException("search admin cannot be null");
      }
      if (sa.getClass().getName().equals(m_searchAdminLockedClass))
         m_searchAdminLockedClass = null; 
      notifyAll();  
   }
   
   /**
    * The query handler is used to make search requests against the search 
    * engine.
    * 
    * @return Never <code>null</code>. When finished processing, the caller 
    * must call {@link #releaseSearchQuery(PSSearchQuery) releaseSearchQuery}. 
    * 
    * @throws IllegalStateException If the search engine is not running.
    * 
    * @throws PSSearchException If the query manager can't be created.
    */
   public PSSearchQuery getSearchQuery()
      throws PSSearchException   
   {
      checkPaused();
      return getSearchQueryInternal();
   }
   
   
   /**
    * This method does the work of {@link #getSearchQuery()}, except it will
    * not block while the search engine is paused. Its purpose is to allow 
    * work to continue that is internal to this package while keeping all 
    * external users blocked. Derived classes can override it to expose it to
    * their classes if needed. 
    * <p>See the referenced method for full details. 
    */
   protected PSSearchQuery getSearchQueryInternal()
      throws PSSearchException
   {
      PSSearchQuery sq;
      if (!isAvailable())
      {
         throw new IllegalStateException("Search server not running. " 
               + "Currently in state: " + getStateString());
      }
      // sync w/ shutdown method
      synchronized (m_searchQueryObjs)
      {
         sq = doGetSearchQuery();
         m_searchQueryObjs.add(sq);
      }
      return sq;
   }
   
   /**
    * After the caller has completed their work, they must pass the query object
    * they received using the {@link #getSearchQuery()} method.  
    * 
    * @param sq Never <code>null</code>.
    * 
    * @throws PSSearchException If any errors occur while freeing resources.
    * 
    * @throws IllegalStateException If the supplied query object was not 
    * originally obtained from {@link #getSearchQuery()} or the same object
    * was freed more than once.
    */
   public void releaseSearchQuery(PSSearchQuery sq)
      throws PSSearchException
   {
      // sync w/ shutdown method
      synchronized (m_searchQueryObjs)
      {
         if (!m_searchQueryObjs.remove(sq))
         {
            throw new IllegalStateException(
               "The supplied indexer object was not obtained from this engine, "
               + "or an attempt was made to free the same object more than once.");
         }
         doReleaseSearchQuery(sq);
         m_searchQueryObjs.notifyAll();
      }
   }
   
   /**
    * The search index handler is used to submit data to the search engine's 
    * indexes. Data must be submitted through this interface before it can be 
    * retrieved through the PSSearchQuery interface.
    * 
    * @return Never <code>null</code>. When finished processing, the caller 
    * must call {@link #releaseSearchIndexer(PSSearchIndexer) 
    * releaseSearchIndexer}. 
    * 
    * @throws IllegalStateException If the search engine is not running.
    * 
    * @throws PSSearchException If no content type that is being indexed
    * matches the supplied key.
    */
   public PSSearchIndexer getSearchIndexer()
      throws PSSearchException
   {
      checkPaused();
      return getSearchIndexerInternal();
   }
   
   /**
    * This method does the work of {@link #getSearchIndexer()}, except it will
    * not block while the search engine is paused. Its purpose is to allow 
    * work to continue that is internal to this package while keeping all 
    * external users blocked. Derived classes can override it to expose it to
    * their classes if needed.
    * <p>See the referenced method for full details. 
    */
   protected PSSearchIndexer getSearchIndexerInternal()
      throws PSSearchException
   {
      PSSearchIndexer si;
      // sync w/ shutdown method
      synchronized (m_searchIndexerObjs)
      {
         if (!isAvailable())
         {
            throw new IllegalStateException("Server not running. Currently in" +
               "state: " + getStateCode());
         }
         si = doGetSearchIndexer();
         m_searchIndexerObjs.add(si);
      }
      return si;
   }
   
   /**
    * After the caller has completed their work, they must pass the indexer
    * they received using the {@link #getSearchIndexer()}.  
    * 
    * @param si Never <code>null</code>.
    * 
    * @throws PSSearchException If any errors occur while freeing resources.
    * 
    * @throws IllegalStateException If the supplied query object was not 
    * originally obtained from {@link #getSearchQuery()} or the same object
    * was freed more than once.
    */
   public void releaseSearchIndexer(PSSearchIndexer si)
      throws PSSearchException
   {
      // sync w/ shutdown method
      synchronized (m_searchIndexerObjs)
      {
         if (!m_searchIndexerObjs.remove(si))
         {
            throw new IllegalStateException(
               "The supplied indexer object was not obtained from this engine, "
               + "or an attempt was made to free the same object more than once.");
         }
         doReleaseSearchIndexer(si);
         m_searchIndexerObjs.notifyAll();
      }
   }
   
      
   /**
    * Activates the search engine and makes it ready to accept index and query
    * requests. If the server is already running, it calls shutdown before
    * activating. The method is synchronous, meaning all queues are processed
    * and the server is re-activated before this method returns.
    * <p>This method must be called to cause any changes made w/ the admin
    * interface to take effect. 
    *  
    * @throws PSSearchException If the server fails to shut down or come back
    * up. The first time an exception is thrown, call this method again to 
    * re-attempt the start. 
    */
   synchronized public void restart()
      throws PSSearchException
   {
      if (isAvailable(false))
         shutdown(false);
      
      int status = getStateCode();
      if (status == STATUS_TERMINATING)
      {
         throw new IllegalStateException(
               "Server cannot be restarted while it is shutting down.");   
      }
      PrintConsoleMessage("Starting search engine process");
      doStart();
      m_shutdownMessage = "";
   }
    
   /**
    * The shutdown message is supplied to the
    * {@link #shutdown(boolean, boolean, String)} method. This method retrieves
    * it. It is cleared by the {@link  #restart()} method.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public String getShutdownMessage()
   {
      return m_shutdownMessage;
   }
    
   /**
    * Tracing controls whether extra debugging information is output. It
    * is controlled by a flag in the {@link 
    * com.percussion.design.objectstore.PSSearchConfig PSSearchConfig} object.
    * All logging should be done using log4j's {@link org.apache.log4j.Logger
    * Logger} class once it is determined that tracing is enabled.
    * <p>If it is very expensive to generate the message, then the caller
    * may also wish to check the priority set on the logger before doing
    * that work. By default, the priority is set to INFO.
    * <p>The log4j package is initialized by the server during startup.
    * 
    * @return <code>true</code> if extra output is desired, <code>false</code>
    * otherwise.
    */ 
   public boolean isTraceEnabled()
   {
      return 
         PSServer.getServerConfiguration().getSearchConfig().isTraceEnabled(); 
   }
   
   /**
    * Convenience method that calls {@link #isAvailable(boolean) 
    * isAvailable(true)}.
    */
   public boolean isAvailable()
      throws PSSearchException
   {
      return isAvailable(true);
   }
   
   
   /**
    * This method is used to check if the engine is running. See {@link
    * #getStateCode()} for details of the states.
    * 
    * @param resync If <code>true</code> and the engine state is not 
    * consistent w/ the actual state, it will either start or shutdown the 
    * engine to match the known state. 
    * 
    * @return <code>true</code> if the search engine is initialized, started 
    *    and ready to process requests, otherwise, <code>false</code>.
    * 
    * @throws PSSearchException If any problems while checking the server.
    */
   abstract public boolean isAvailable(boolean resync)
      throws PSSearchException;
   
   /**
    * Gets an implementation dependent snapshot of the engine's state.
    * <p>The returned element conforms to the following DTD:
    * <code><pre>
    * <!ELEMENT SearchStatus (Message+)>
    * <!ATTLIST SearchStatus
    *    runningStatus  CDATA #REQUIRED
    *    queueLength    CDATA #REQUIRED
    * >
    * <!ELEMENT Message (#PCDATA)> 
    * <!ATTLIST Message 
    *    type CDATA #IMPLIED
    *    >
    * </pre></code>
    * The runningStatus attribute must be one of the {@link #STATUS_STRINGS}
    * values.
    * <p>The following format is suggested for displaying the text to the
    * recipient:
    * <pre>
    * Full text search engine status: currently {runningStatus}
    * Index queue length - 3
    * [type]: MessageText
    * [type]: MessageText
    * </pre>
    * 
    * @param doc The context within which to create this element. Never 
    * <code>null</code>.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSSearchException If the engine is running but status cannot be
    *    obtained.
    */
   public Element getStatus(Document doc)
      throws PSSearchException
   {
      if (null == doc)
      {
         throw new IllegalArgumentException("doc cannot be null");
      }
      Element status = doc.createElement(EL_STATUS);
      status.setAttribute(ATTR_RUNNING_STATUS, getStateString());
      status.setAttribute("queueStatus", 
            String.valueOf(PSSearchIndexEventQueue.getInstance().getStatus()));
      status.setAttribute("queueLength", 
            String.valueOf(PSSearchIndexEventQueue.getInstance().size()));
      Element result = status;
      int stateCode = getStateCode();
      if (stateCode == STATUS_RUNNING)
         result = doGetStatus(doc, status);
      return result;
   }
   
   /**
    * Causes the server to stop processing all requests. It waits until all
    * current processing is completed, then returns. Processing is on hold 
    * until the {@link #unPause()} method is called. The server should not
    * be left in a paused state very long. Subsequent requests to this method 
    * while paused increments a counter. Therefore, there must be a matching
    * <code>unPause</code> for every <code>pause</code> call.
    * <p>While the server is paused, the {@link #getSearchIndexer()} and
    * {@link #getSearchQuery()} methods will block until the server is
    * unpaused.
    * 
    * @param wait How many milliseconds to wait before forcing unfinished 
    * actions to complete. &lt;=0 means don't wait.
    * 
    * @param message Requests that arrive while the search engine is paused
    * will be queued. If they timeout, this message will be used in the
    * exception that is thrown. If <code>null</code> or empty, "Search server
    * unavailable, try again in a minute." will be used. If the system is 
    * already paused and this value is not <code>null</code> or empty, this 
    * message will replace the current message until this method is called 
    * with a different message or the server becomes enabled again.
    * 
    * @throws PSSearchException If any problems while trying to forcefully
    * shutdown unfinished operations.
    */
   protected void pause(long wait, String message)
      throws PSSearchException
   {
      synchronized (m_pauseMonitor)
      {
         m_pausedCount++;
         if (null != message && message.trim().length() > 0)
            m_pauseMessage = message;
         else if (null == m_pauseMessage)
         {
            m_pauseMessage = 
                  "Search server unavailable, try again in a minute.";
         }
         if (m_pausedCount == 1)
         {
            m_stateCodeBeforePause = getStateCode();
            setStateCode(STATUS_PAUSED);
            finishQueriesAndIndexers(wait);
         }
      }           
   }
   
   /**
    * Checks whether any indexers or query objects are currently in use. If 
    * there are any, waits at most <code>wait</code> milliseconds for them 
    * to finish. If they haven't finished by then, they are forcefully 
    * closed. The collections containing these objects are cleared.
    *  
    * @param wait How long to wait before forcefully terminating any current
    * operations, in milliseconds. &lt;=0 means don't wait.
    * 
    * @return <code>true</code> if all objects finish before the timeout
    * period, <code>false</code> if any have to be forcefully closed.
    * 
    * @throws PSSearchException If any errors occur while freeing resources.
    */
   private boolean finishQueriesAndIndexers(long wait)
      throws PSSearchException
   {
      if (wait < 0)
         wait = 0;
      boolean result = true;
      long halfWait = wait/2;
      long start = System.currentTimeMillis();
      try
      {
         int objCount = 0;
         synchronized (m_searchIndexerObjs)
         {
            objCount = m_searchIndexerObjs.size();
            if (objCount > 0)
            {
               PrintConsoleMessage( "Waiting for " + objCount 
                     + " search indexers to finish.");
            }
            wait = halfWait;
            while (!m_searchIndexerObjs.isEmpty())
            {
               try
               {
                  m_searchIndexerObjs.wait(wait);
                  if (m_searchIndexerObjs.size() == objCount)
                  {
                     // got here because we timed out
                     PrintConsoleMessage(
                           "Timed out waiting for indexers to be released"
                           + " during search engine shutdown.");
                     break;
                  }
                  else
                  {
                     objCount = m_searchIndexerObjs.size();
                     wait -= (System.currentTimeMillis() - start);
                     if (wait <= 0)
                        break;                        
                  }
               }
               catch (InterruptedException ie)
               {
                  //if someone interrupted us, skip this processing and cont
                  break;
               }
            }
            if (m_searchIndexerObjs.size() > 0)
            {
               result = false;
               for (Iterator iter = m_searchIndexerObjs.iterator(); 
                     iter.hasNext();)
               {
                  PSSearchIndexer si = (PSSearchIndexer) iter.next();
                  releaseSearchIndexer(si);
               }
            }
         }
         synchronized (m_searchQueryObjs)
         {
            objCount = m_searchQueryObjs.size();
            if (objCount > 0)
            {
               PrintConsoleMessage("Waiting for " + objCount 
                     + " search queries to finish.");
            }
            wait = halfWait;
            while (!m_searchQueryObjs.isEmpty())
            {
               try
               {
                  m_searchQueryObjs.wait(wait);
                  if (m_searchQueryObjs.size() == objCount)
                  {
                     PrintConsoleMessage(
                           "Timed out waiting for queries to be released"
                           + " during search engine shutdown.");
                     break;
                  }
                  else
                  {
                     objCount = m_searchQueryObjs.size();
                     wait -= (System.currentTimeMillis() - start);
                     if (wait <= 0)
                        break;                        
                  }
               }
               catch (InterruptedException ie)
               {
                  //if someone interrupted us, skip this processing and cont
                  break;
               }
            }
            if (m_searchQueryObjs.size() > 0)
            {
               result = false;
               for (Iterator iter = m_searchQueryObjs.iterator(); 
                     iter.hasNext();)
               {
                  PSSearchQuery sq = (PSSearchQuery) iter.next();
                  releaseSearchQuery(sq);
               }
            }
         }
      }
      finally
      {
         //shouldn't be necessary unless an exception occurs during cleanup
         m_searchIndexerObjs.clear();
         m_searchQueryObjs.clear();
      }
      return result;
   }

   /**
    * This method will block until the server is unpaused. If the server is 
    * not unpaused w/in 1 minute of first being called, an exception is thrown. 
    * 
    * @throws PSSearchException If the server doesn't un-pause before the
    * timeout is reached or if the wait is interrupted. 
    */
   private void checkPaused()
      throws PSSearchException
   {
      synchronized (m_pauseMonitor)
      {
         try
         {
            if (m_pausedCount > 0)
               m_pauseMonitor.wait(60000);
            if (m_pausedCount > 0)
            {
               throw new PSSearchException(IPSServerErrors.RAW_DUMP, 
                     m_pauseMessage);
            }
         }
         catch (InterruptedException e)
         {
            //since this shouldn't happen, don't i18n
            throw new PSSearchException(IPSServerErrors.RAW_DUMP, 
                  "Timed out waiting for search engine to un-pause.");
         }
      }
   }

   /**
    * Decrements the pause counter. If the pause counter reaches 0, the search
    * engine is enabled and the pause message is cleared. If the counter is 
    * already 0, nothing is done. 
    */
   protected void unPause()
   {
      synchronized (m_pauseMonitor)
      {
         if (m_pausedCount > 0)
         {
            m_pausedCount--;
            if (m_pausedCount == 0)
            {
               m_pauseMonitor.notifyAll();
               m_pauseMessage = null;
               setStateCode(m_stateCodeBeforePause);
            }
         }
      }
   }
   
   /**
    * Derived classes are responsible for managing this flag except for the
    * paused and terminating states. This class will set the state to 
    * <code>STATUS_PAUSED</code> after the <code>pause</code> method has been
    * called and restore it to the state it was in before pausing when the
    * <code>unPause</code> method is called. During the shutdown process, the
    * appropriate shutting/shut down flag is set.
    * 
    * @param status One of the STATUS_xxx values.
    */
   abstract protected void setStateCode(int status);
   
   /**
    * This is a singleton, so the ctor is only available to the derived class.
    * 
    * @see PSSearchEngine#getInstance()
    */
   protected PSSearchEngine()
   {}
   
   /**
    * Same as {@link #getSearchAdmin(boolean)}, but it doesn't need to track 
    * locking. The admin object should be created up front so this method 
    * should never throw an exception. If the flag is not locked, then the 
    * returned object should not allow any modifications.
    * 
    * @return Never <code>null</code>.
    */
   protected abstract PSSearchAdmin doGetSearchAdmin(boolean wantLocked);
   
   /**
    * See {@link #getStatus(Document)} for details. This method should add the
    * Message tags to the root element of the supplied base. This method is
    * only called if the current state code is <code>STATUS_RUNNING</code> or
    * <code>STATUS_TERMINATING</code>.
    * 
    * @param doc Used to generate nodes. Never <code>null</code>.
    * 
    * @param base The root element w/ the appropriate runningStatus set.
    * Never <code>null</code>.
    * 
    * @return The supplied element, with additional Message elements optionally
    * added.
    * 
    * @throws PSSearchException 
    */
   protected abstract Element doGetStatus(Document doc, Element base)
      throws PSSearchException; 
   
   /**
    * Same as {@link #getSearchIndexer()} but doesn't need to perform check
    * for IllegalStateException.
    */
   protected abstract PSSearchIndexer doGetSearchIndexer()
      throws PSSearchException;
 
   /**
    * Same as {@link #releaseSearchIndexer(PSSearchIndexer)}. This class first 
    * verifies that the supplied object was obtained from this engine. It 
    * maintains a list of all allocated indexers so it can cleanup properly.
    * 
    * @param si Never <code>null</code>.
    */
   protected abstract void doReleaseSearchIndexer(PSSearchIndexer si)
      throws PSSearchException;
   
   /**
    * Same as {@link #getSearchQuery()} but doesn't need to perform check
    * for IllegalStateException.
    */
   protected abstract PSSearchQuery doGetSearchQuery()
      throws PSSearchException;
 
   /**
    * Same as {@link #releaseSearchQuery(PSSearchQuery)}. This class first 
    * verifies that the supplied object was obtained from this engine. It 
    * maintains a list of all allocated query objs so it can cleanup properly.
    */
   protected abstract void doReleaseSearchQuery(PSSearchQuery sq)
      throws PSSearchException;
   
   /**
    * Same as {@link #shutdown(boolean)} but doesn't need to perform check
    * for IllegalStateException.
    * 
    * @param force If <code>true</code>, it means all queries and indexers
    * were forcefully closed either because the caller forced it, or we 
    * timed out waiting for them to finish. If <code>false</code>, all queries
    * and indexers successfully completed.
    */
   protected abstract void doShutdown(boolean force)
      throws PSSearchException;
   
   /**
    * Same as {@link #restart()}, but doesn't perform a shutdown (this is 
    * already done by this class). 
    */
   protected abstract void doStart()
      throws PSSearchException;
   
   /**
    * Provides access to the properties provided during initialization or 
    * restart. The {@link #PROP_CLASSNAME} property is not available through
    * this method. These properties are available when the derived class
    * ctor is processing.
    * 
    * @param name The property you desire. Never <code>null</code> or empty.
    * 
    * @param defaultValue If a property by the requested name can't be found,
    * the defaultValue will be returned. May be <code>null</code> or empty.
    * 
    * @return The value of the requested property, or "" if the property is 
    * not found. If the property is present with no value, "" is returned. 
    * The property value is guaranteed not to contain leading or trailing 
    * whitespace.
    */
   protected String getProperty(String name, String defaultValue)
   {
      return ms_props.getProperty(name.toLowerCase(), defaultValue);
   }
   
   /**
    * The search engine can be in several possible states. These states are
    * described w/ their definition. All state values have a name of the 
    * form STATUS_xxx.
    * 
    * @return One of the STATUS_xxx values.
    */
   abstract public int getStateCode();
   
   /**
    * Returns a string representation of the constant returned by 
    * <code>getStateCode</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getStateString()
   {
      return STATUS_STRINGS[getStateCode()];
   }
   
   /**
    * Text representations of all the STATUS_xxx constants. Use the constant
    * as an index into this array to get the text.
    */
   private static String[] STATUS_STRINGS =
   {
      "",
      "Initialized",
      "Running",
      "Shutting down",
      "Shut down",
      "Paused"
   };
   
   
   /**
    * Stores the normalized list of properties supplied in the ctor. All 
    * keys have been lower-cased and all values have been trimmed. Initialized 
    * in ctor, then never <code>null</code> and entries never changed.
    */
   private static Properties ms_props = null;
   
   /**
    * This is the message supplied to the
    * {@link #shutdown(boolean, boolean, String)} method. It is cleared when the
    * server is restarted. Never <code>null
    * </code> may be empty if there is no
    * message or the server isn't currently shut-down.
    */
   private String m_shutdownMessage = "";
   
   /**
    * The one and only instance of this class. Initialized by {@link 
    * #getInstance(Properties)}, then never <code>null</code> until {@link
    * #shutdown(boolean)} is called. Then <code>null</code> until <code>
    * getInstance</code> is called again.
    */
   private static PSSearchEngine ms_instance = null;
   
   /**
    * Stores PSSearchQuery objects between the time they are allocated with the
    * {@link #getSearchQuery()} and they are freed with the {@link 
    * #releaseSearchQuery(PSSearchQuery)}. Never <code>null</code>, may be
    * empty. We store these so that we can clean up properly.
    * <p>Note, access to this object must be synchronized.
    */
   private Set<PSSearchQuery> m_searchQueryObjs = new HashSet<PSSearchQuery>();
   
   /**
    * Stores PSSearchIndexer objects between the time they are allocated with 
    * the {@link #getSearchIndexer()} and they are freed with the 
    * {@link #releaseSearchIndexer(PSSearchIndexer)}. Never <code>null</code>,
    * may be empty. We store these so that we can clean up properly.
    * <p>Note, access to this object must be synchronized.
    */
   private Set<PSSearchIndexer> m_searchIndexerObjs = 
      new HashSet<PSSearchIndexer>(); 
   
   /**
    * Used to track whether anyone has locked the admin. Contains the string
    * Qname of the locked class while it is locked.
    * Set in {@link #getSearchAdmin(boolean) getSearchAdmin(<code>true</code>)} 
    * method and cleared in {@link #releaseSearchAdmin(PSSearchAdmin)}.
    * <p>Default value is <code>null</code>.
    */
   private String m_searchAdminLockedClass = null;

   /**
    * This is used strictly as a monitor when pausing the search engine. 
    * Never <code>null</code> or modified after class initialized.
    */
   private Object m_pauseMonitor = new Object();
   
   /**
    * This counter indicates how many times the {@link #pause(long, String) 
    * pause} method has been called without a corresponding call to {@link 
    * #unPause()}. It is decremented by calls to @link #unPause()}. The server 
    * remains paused while this value is non-zero. See 
    * {@link #pause(long, String)} for more details.
    */
   private int m_pausedCount = 0;
   
   /**
    * The first time <code>pause</code> is called, the current state code is
    * stored here. The state code is set to this value after the last 
    * <code>unPause</code> method call. It is not used otherwise.
    */
   private int m_stateCodeBeforePause;
   
   /**
    * If requests made after the server has been paused time out, then the
    * exception thrown will contain this message.
    */
   private String m_pauseMessage;
   
   // xml element/attr names
   private static final String EL_STATUS = "SearchStatus";
   private static final String ATTR_RUNNING_STATUS = "runningStatus";

   /**
    * During shutdown, we will wait this long for any indexer, query or admin 
    * object to finish processing. In milliseconds.
    */
   private static final int SHUTDOWN_WAIT = 10000;
}
