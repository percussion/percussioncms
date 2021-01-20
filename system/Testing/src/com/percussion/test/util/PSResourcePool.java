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
package com.percussion.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class is a generic resource pooling mechanism, such as one might use
 * for thread or database connection pooling. It is designed to be overridden
 * to specify the types of the objects being stored in the pool. This class
 * does the work.
 * <p>The objects in the pool can be in 1 of 2 states, free or in use. Free
 * objects are available for use.
 * <p>The pool has minimum and maximum counts for the number of objects
 * existing in the pool at any given time. When a request for an object is
 * made, if no free objects are available and the current object count is
 * less than the max, a new object is created and returned, otherwise, the
 * thread will wait until an object becomes available.
 * <p>The objects in the pool must implement the {@link IPSPooledResource}
 * interface. The pool does not prune added objects so each object is
 * responsible for removing itself if it is desired to minimize resources.
 * The derived class must provide an interface to the <code>removeResource
 * <code> method with correct data types. The derived class must also provide
 * all public methods for accessing the resources. At a minimum, a method
 * to get an idle resource and return an active resource to the free state
 * (with proper data types) must be provided.
 * <p>In the future, we could add a timeout mechanism to this class rather
 * than rely on the objects.
 */
public abstract class PSResourcePool
{
   /**
    * An identifier that means 'an unlimited number of resources'. Can be
    * passed as the value for <code>PROPS_MIN_COUNT</code> or <code>
    * PROPS_MAX_COUNT</code>.
    */
   public static final int NO_LIMIT = 0;

   /**
    * The name of the property that can be passed to the ctor that specifies
    * the minimum number of objects that must be kept in the pool.
    */
   public static final String PROPS_MIN_COUNT = "resourceCountMin";

   /**
    * The name of the property that can be passed to the ctor that specifies
    * the maximum number of objects that can be kept in the pool.
    */
   public static final String PROPS_MAX_COUNT = "resourceCountMax";

   /**
    * How long idle objects in the pool will be kept around before they are
    * destroyed, if there are more than the minimum # of resources.
    * <p>Not currently implemented.</p>
    */
   public static final String PROPS_TIMEOUT = "idleResourceTimeout";

   /**
    * Ctor for derived classes.
    *
    * @param props Initialization properties for the pool and the objects in
    *    the pool. May be <code>null</code>, in which case reasonable
    *    default values will be used. This class takes ownership of the
    *    passed object, so the caller should not modify it after this method
    *    is called. Recognized properties are of the form PROPS_xxx, as seen
    *    in the publically available static variables.
    */
   protected PSResourcePool( Properties props )
   {
      if (props != null)
      {
         m_props = props;
         m_minCount = getInt( props, PROPS_MIN_COUNT, DEFAULT_MIN_COUNT );
         m_maxCount = getInt( props, PROPS_MAX_COUNT, DEFAULT_MAX_COUNT );
      }
      else
         m_props = new Properties();

      /* create min user thread count PSUserThread objects and add
       * them to the idle queue. Don't forget to keep track of the
       * number of threads created so we don't exceed max count.
       */
      for ( m_resourceCount = 0; m_resourceCount < m_minCount;
            m_resourceCount++)
      {
         m_resourcePool.add( createResource(props));
      }
   }

   /**
    * Derived classes must implement this method so that it returns an
    * object of the type for this pool. It is called by this class to create
    * the object being pooled.
    *
    * @param props Properties passed into this class's ctor. Never <code>
    *    null</code>, may be empty.
    *
    * @return A valid object.
    */
   protected abstract IPSPooledResource createResource( Properties props );


   /**
    * Gets a named value from the property object, supplying a default if
    * it doesn't exist or is empty.
    *
    * @param props A list of 0 or more properties. Never <code>null</code>.
    *
    * @param name The name of the property to find. Never <code>null</code>.
    *
    * @param defaultValue If name is not found in props or is empty, this
    *    value is returned.
    *
    * @return The value assigned to the property whose identifier is 'name',
    *    if it exists, otherwise, defaultValue is returned.
    *
    * @throws IllegalArgumentException If props or name is null or name is
    *    empty.
    *
    * @throws NumberFormatException If the value associtated with the
    *    requested property cannot be converted to an integer.
    */
   protected static int getInt( Properties props, String name,
         int defaultValue )
   {
      if ( null == props || null == name )
         throw new IllegalArgumentException( "one or more params null" );
      if ( name.trim().length() == 0 )
         throw new IllegalArgumentException( "property name can't be empty" );

      int val;
      String valueText = props.getProperty( name );
      if ( null != valueText && valueText.trim().length() > 0 )
         val = Integer.parseInt( valueText );
      else
         val = defaultValue;
      return val;
   }


   /**
    * Shutdown the pool. Sends a close signal to all idle resources. All
    * active resources will not be able to add themselves back to the pool.
    * After this method is called, this pool can never be used again.
    */
   synchronized public void close()
   {
      // go through each idle object and shut it down
      if (isOpen())
      {
         IPSPooledResource obj;
         for (int i = 0; i < m_resourcePool.size(); i++)
         {
            obj = (IPSPooledResource) m_resourcePool.get(i);
            obj.close();
         }

         m_resourcePool.clear();
         m_resourcePool = null;
      }
   }

   /**
    * Mark a user thread as being idle. When the thread is done processing,
    * it makes this call, adding it back to the available thread pool.
    *
    * @param   thr   the user thread which is now idle
    *
    * @return        <code>true</code> if the user thread should enter a
    *                wait state for new requests; <code>false</code> if
    *                the thread is no longer needed and should now terminate
    */
   synchronized protected boolean makeResourceAvailable(IPSPooledResource obj)
   {
      /* first check if we need to shut this thread down. If so, return
       * false as the shutdown signal.
       */
      if (!isOpen())
         return false;

      m_resourcePool.add(obj);
      notify(); /* wake up anyone waiting in getResource */

      return true;
   }

   /**
    * Get an idle thread to use for processing a request. This may
    * actually cause a new thread to be created.
    *
    * @param   waitMS   the number of milliseconds to wait; 0 returns
    *                   immediately; Long.MAX_VALUE waits indefinitely
    * @return           the user thread capable of handling the request.
    *                   If the number of user threads has been exceeded and
    *                   there are no idle threads, <code>null</code> is
    *                   returned
    */
   synchronized protected IPSPooledResource getResource(long waitMS)
   {
      IPSPooledResource obj = null;

      while (isOpen())
      {
         /* are there any idle threads? */
         if (m_resourcePool.isEmpty())
         {   /* none idle */
            /* can we create a new thread (max count not exceeded)? ??? */
            if ( (m_maxCount == NO_LIMIT) ||
                 (m_resourceCount < m_maxCount) )
            {
               /* if so, create a new object and return it */
               obj = createResource( m_props );
               m_resourceCount++;
               return obj;
            }

            try
            {
               if (waitMS == Long.MAX_VALUE)
                  wait();
               else if (waitMS > 0)
                  wait(waitMS);
            }
            catch (InterruptedException e)
            {
               // guess it's time for us to go now
               break;   // obj is already set to null
            }
         }
         if (m_resourcePool.isEmpty())
         {
            /* if we must wait forever, try the wait again */
            if (waitMS == Long.MAX_VALUE)
               continue;
         }
         else
         {
            /* get the last idle thread (avoid moving elements)
             * and remove it from the idle list (0-based)
             */
            obj = (IPSPooledResource)m_resourcePool.remove(
               m_resourcePool.size()-1);
         }

         break;   /* this is not a wait forever, so don't loop */
      }

      return obj;
   }

   /**
    * When a user thread is shutting down, it notifies us so we can properly
    * manage our internals.
    * <p>
    * Thie method will return <code>false</code> if shutting down the thread
    * would cause the number of active threads to go below the permitted
    * minimum. In this case, the thread should be kept active.
    *
    * @param   thread      the thread that is shutting down
    *
    * @return              <code>true</code> if the thread has been
    *                      removed; <code>false</code> if removing the
    *                      thread would go below the minimum thread count
    */
   synchronized protected boolean removeResource( IPSPooledResource obj )
   {
      if (isOpen())
      {
         // if we're open (active) make sure we don't drop below the min
         if (m_resourceCount <= m_minCount)
            return false;

         /* remove us, if we're in the idle list already */
         int pos = m_resourcePool.indexOf(obj);
         if (pos != -1)
            m_resourcePool.remove(pos);

         /* and lower the live thread count */
         m_resourceCount--;
      }

      return true;
   }

   synchronized public boolean isOpen()
   {
      return null != m_resourcePool;
   }

   /**
    * If no value for <code>PROPS_MIN_COUNT</code> is specified, this value
    * will be used.
    */
   private static final int DEFAULT_MIN_COUNT = 10;

   /**
    * If no value for <code>PROPS_MAX_COUNT</code> is specified, this value
    * will be used.
    */
   private static final int DEFAULT_MAX_COUNT = NO_LIMIT;

   /**
    * This is the collection of idle objects in the pool. Never <code>null
    * </code> until <code>close</code> is called. From then on, it is always
    * <code>null</code>. Its nullness is used as a flag to indicate whether
    * this pool is still valid. A List is used not because we care about order
    * but because it gives us a bit more flexibility in managing the
    * collection.
    */
   private List m_resourcePool = new ArrayList();

   /**
    * This is the total number of objects in the pool, both free and in use.
    * If all objects are idle, this will equal the size of <code>
    * m_resourcePool</code>.
    */
   private int m_resourceCount = 0;

   /**
    * This is the minimum number of objects that will be in the pool.
    * Initialized to <code>DEFAULT_MIN_COUNT</code>. Can be overridden by
    * a property supplied to the ctor.
    */
   private int m_minCount = DEFAULT_MIN_COUNT;
   /**
    * This is the maximum number of objects that can be in the pool.
    * Initialized to <code>DEFAULT_MAX_COUNT</code>. Can be overridden by
    * a property supplied to the ctor.
    */
   private int m_maxCount = DEFAULT_MAX_COUNT;

   /**
    * Initialized in the ctor, never <code>null</code> after this. It contains
    * properties for the pool and the objects in the pool.
    */
   private Properties m_props;
}

