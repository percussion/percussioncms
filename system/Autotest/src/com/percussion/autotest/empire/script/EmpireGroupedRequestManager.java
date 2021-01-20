/*[ EmpireGroupedRequestManager.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

import com.percussion.test.util.PSResourcePool;
import com.percussion.test.util.IPSPooledResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;



/**
 * This class is used to manage some number of simultaneous requests processed
 * using a limited number of threads. This class creates and manages the
 * threads using a thread pool.
 */
public class EmpireGroupedRequestManager 
{

   /**
    * Creates and initializes the object, creating all needed threads. After
    * construction, the object is ready to process the requests without
    * further initialization.
    *
    * @param actions A collection of 0 or more IPSAction objects. Never <code>
    *    null</code>.
    *
    * @param threadCount The number of threads to use to process the requests
    *    in the supplied list. One thread is used per action.
    */
   public EmpireGroupedRequestManager( Iterator actions, int threadCount )
   {
      if ( null == actions )
         throw new IllegalArgumentException( "null action list supplied" );
      else if (  threadCount < 1 )
      {
         throw new IllegalArgumentException(
               "invalid thread count, must be > 0" );
      }

      Properties props = new Properties();
      props.setProperty( PSResourcePool.PROPS_MIN_COUNT, ""+threadCount );
      props.setProperty( PSResourcePool.PROPS_MAX_COUNT, ""+threadCount );
      m_threadPool = new RequestThreadPool( props );

      m_actions = actions;
   }

   /**
    * Takes each action supplied in the ctor and assigns it to a thread,
    * which then performs the action. The process continues until all actions
    * have been completely processed. If one action throws an exception, it
    * does not affect the others in any way.
    *
    * @return A possibly empty set of Exceptions thrown by the actions
    *    processed by this manager. Never <code>null</code>
    *
    * @throws IllegalStateException if called after <code>shutdown</code> has
    *    been called.
    */
   public Iterator process()
   {
      if ( null == m_threadPool )
         throw new IllegalStateException( "manager has been shutdown" );

      while ( m_actions.hasNext())
      {
         IPSAction action = (IPSAction) m_actions.next();
         RequestThread thread = m_threadPool.getIdleThread( 1000000 );
         synchronized (this)
         {
            m_activeThreads++;
         }
         thread.setAction( action, this );   // causes the action to be performed
      }

      // wait until all threads have completed
      int activeThreads = 1;
      do
      {
         try
         {
            synchronized (this)
            {
               activeThreads = m_activeThreads;
               if ( activeThreads > 0 )
                  wait();
            }
         }
         catch ( InterruptedException ie )
         { /* ignore */ }
      }
      while ( activeThreads > 0 );
      Collection tmp = m_exceptions;
      m_exceptions = new ArrayList();  // reset it
      return tmp.iterator();
   }

   /**
    * Must be called when the creator has finished with this class. It frees
    * all resources currently held by the manager, including shutting down all
    * pooled threads. If the manager is currently waiting on threads to
    * complete a task, the shutdown will not proceed until those threads have
    * completed. Multiple calls are not harmful.
    * <p>The synchronization is not 100% safe, but it is sufficient for the
    * purposes of the testing framework.
    */
   public void shutdown()
   {
      if ( null ==  m_threadPool )
         return;

      while ( m_activeThreads > 0 )
      {
         try
         {
            Thread.sleep(1000);
         }
         catch ( InterruptedException e )
         {
            // force the shutdown
            break;
         }
      }

      m_threadPool.close();
      m_threadPool = null;
   }

   /**
    * Overridden to terminate all the threads in case someone forgot to call
    * <code>shutdown</code>.
    */
   protected void finalize()
      throws Throwable
   {
      shutdown();
      super.finalize();
   }

   /**
    * This method is used as a communication interface between the request
    * manager and the threads actually performing the requests. The
    * manager doesn't want to return from the <code>process</code> method
    * until all threads have completed their respective requests.
    *
    * @param If the request terminated prematurely, the exception thrown by
    *    the request processor should be returned here. Otherwise, pass
    *    <code>null</code>.
    */
   synchronized public void requestFinished( Exception e )
   {
      m_activeThreads--;
      if ( null != e )
         m_exceptions.add( e );
      notify();
   }


   /**
    * This is an implementation of the resource pool specific to {@link
    * EmpireGroupedRequestManager.RequestThread} thread resources.
    */
   private class RequestThreadPool extends PSResourcePool
   {
      /**
       * Creates a thread pool specific for <code>RequestThread</code>
       * resources.
       *
       * @param props See {@link PSResourcePool#PSResourcePool(Properties)
       *    PSResourcePool(props)} for details.
       */
      public RequestThreadPool( Properties props )
      {
         super( props );
      }

      /**
       * Creates a {@link EmpireGroupedRequestManager.RequestThread} object
       * and starts it. All created threads have a name of the form "Pn",
       * where n is a unique number relative to all other threads created with
       * this pool.
       *
       * @param props Unused.
       *
       * @return The created thread.
       */
      protected IPSPooledResource createResource( Properties props )
      {
         RequestThread thread = new RequestThread( this );
         thread.setName("P" + m_id++);
         thread.start();
         return thread;
      }

      // see base class
      public RequestThread getIdleThread( long waitMs )
      {
         return (RequestThread) super.getResource( waitMs );
      }

      // see base class
      public boolean setIdleThread( RequestThread thread )
      {
         return super.makeResourceAvailable( thread );
      }

      /**
       * This value is used to assign a unique name to each created thread.
       * Increment after each use.
       */
      private int m_id = 0;
   }


   /**
    * This class is used to process an {@link IPSAction}. The action is passed
    * in via the ctor. It is processed by calling the <code>start</code>
    * method.
    */
   private class RequestThread extends Thread implements IPSPooledResource
   {
      /**
       * Creates a new pooled thread, associated with the supplied pool.
       *
       * @param pool The pool containing this thread, assumed not <code>null
       *    </code>.
       */
      public RequestThread( RequestThreadPool pool )
      {
         m_pool = pool;
         setDaemon(true);
      }

      /**
       * Immediately executes the supplied action, then re-adds itself to
       * the thread pool.
       *
       * @param action The action to be taken by this thread as soon as
       *    possible. Assumed not <code>null</code>.
       *
       * @param listener Will be notified upon completion of the action. If
       *    <code>null</code>, no notification will take place.
       *
       * @throws IllegalArgumentException if action is <code>null</code>.
       */
      synchronized public void setAction( IPSAction action,
            EmpireGroupedRequestManager listener )
      {
         if ( null == action )
            throw new IllegalArgumentException( "action must be supplied" );
         m_listener = listener;
         m_action = action;
         notify();
      }

      /**
       * Sets flag to notify run loop to terminate.
       */
      public void close()
      {
         m_finished = true;
      }

      /**
       * Sits in a tight loop, waiting for actions to be set with the <code>
       * setAction</code> method. When an action has been set it wakes up
       * and executes the action, then re-adds itself to its parent pool.
       * Wakes up every second to check if the thread has been terminated
       * with the <code>close</code> method.
       */
      synchronized public void run()
      {
         while ( !m_finished )
         {
            try
            {
               if ( null == m_action )
                  wait(1000);

               if ( !m_finished && null != m_action )
               {
                  Exception ex = null;
                  try
                  {
                     m_action.perform();
                  }
                  catch ( Exception e )
                  {
                     ex = e;
                  }
                  finally
                  {
                     if ( null != m_listener )
                        m_listener.requestFinished(ex);
                     m_action = null;
                     m_pool.setIdleThread( this );
                  }
               }
            }
            catch ( InterruptedException ie )
            {
               m_finished = true;
            }
         }
      }

      /**
       * This is used to communicate with the object that initiated the
       * action. It's <code>requestFinished</code> method is called at the
       * end of the request. Set by the <code>setAction</code> method. May be
       * <code>null</code>.
       */
      private EmpireGroupedRequestManager m_listener = null;

      /**
       * The action that will be executed when this thread is run. <code>
       * null</code> until <code>setAction</code> is called. Then reset to
       * <code>null</code> after the action has finished its execution.
       */
      private IPSAction m_action;

      /**
       * A flag that is set when the <code>close</code> method has been
       * called. The <code>run</code> method polls this flag every second
       * to determine when to terminate.
       */
      private boolean m_finished = false;

      /**
       * The pool to which this thread is a member. Never <code>null</code>
       * after construction.
       */
      private RequestThreadPool m_pool;
   }

   /**
    * The actions to execute with the thread pool. Never <code>null</code>
    * after construction (may be empty).
    */
   private Iterator m_actions;

   /**
    * The thread pool manager. The supplied actions are processed using
    * threads from this pool. Never <code>null</code> after construction until
    * <code>shutdown</code> is called.
    */
   private RequestThreadPool m_threadPool;

   /**
    * The number of threads that are currently executing requests. This value
    * is used to know when all threads have finished. We don't return until
    * that happens.
    */
   private int m_activeThreads = 0;

   /**
    * Contains the exceptions thrown by any of the actions processed by this
    * manager. They are returned by the <code>process</code> method.
    */
   private Collection m_exceptions = new ArrayList();
}
