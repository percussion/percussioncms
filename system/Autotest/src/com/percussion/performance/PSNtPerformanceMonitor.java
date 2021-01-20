/*[ PSNtPerformanceMonitor.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.performance;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the performance monitor for Windows NT systems.
 */
public class PSNtPerformanceMonitor implements IPSPerformanceMonitor
{
   /**
    * For debugging purposes.
    *
    * @param args Accepts 2 params. The first is the counter path and is
    *    required. The 2nd is the number of samples to collect and display on
    *    a 1 second interval. If not provided, 10 is used.
    */
   public static void main( String args[] )
   {
      try
      {
         if ( args.length < 1 || args.length > 2 )
         {
            System.out.println( "Usage: java"
                  + " com.percussion.performance.PSNtPerformanceMonitor"
                  + " counterPath [seconds]" );
            System.exit(-1);
         }
         PSNtPerformanceMonitor perfmon = new PSNtPerformanceMonitor();
         if ( perfmon.addCounter( args[0] ))
            System.out.println( "Successfully added counter: " + args[0]);
         else
            System.exit(-2);
         int seconds;
         if ( args.length > 1 )
            seconds = Integer.parseInt(args[1]);
         else
            seconds = 10;
         for ( int i = 0; i < seconds; i++ )
         {
            perfmon.collectSample();
            System.out.println( perfmon.getFormattedData( args[0] ));
            Thread.sleep(1000);
         }
         perfmon.shutdown();
      }
      catch ( Exception e )
      {
         System.out.println( e.getLocalizedMessage());
         e.printStackTrace();
      }
   }

   /**
    * Creates an instance of the monitor and loads the associated dll if it
    * hasn't been loaded yet.
    *
    * @throws UnsatisfiedLinkError If the associated dll cannot be loaded.
    */
   public PSNtPerformanceMonitor()
      throws UnsatisfiedLinkError
   {
      if ( !ms_libLoaded )
      {
         System.loadLibrary("perfmonitor");
         ms_libLoaded = true;
      }
   }

   // see interface for desc
   public boolean addCounter( String counterName )
      throws PSPerformanceMonitorException
   {
      if ( null == counterName || counterName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "counter name must be non-empty" );
      }
      if ( !m_isInited )
      {
         m_queryHandle = init();
         m_isInited = true;
      }
      boolean isValid = validateCounterName( counterName );
      if ( isValid )
      {
         int counterHandle = addCounterNative( m_queryHandle, counterName );
         m_counterHandles.put( counterName, new Integer(counterHandle));
      }
      return isValid;
   }

   // see interface for desc
   public void collectSample()
      throws PSPerformanceMonitorException
   {
      if ( m_counterHandles.size() == 0 )
      {
         throw new IllegalStateException(
               "can't collect sample until counter added" );
      }
      collectSampleNative( m_queryHandle );
   }

   // see interface for desc
   public long getFormattedData( String counterName )
      throws PSPerformanceMonitorException
   {
      if ( null == counterName || counterName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "counter name must be non-empty" );
      }

      Object o = m_counterHandles.get( counterName );
      if ( null == o )
      {
         throw new IllegalStateException( "Counter never added: "
               + counterName );
      }
      int counterHandle = ((Integer) o).intValue();
      return getFormattedDataNative( counterHandle );
   }

   // see interface for desc
   public void shutdown()
      throws PSPerformanceMonitorException
   {
      try
      {
         if ( m_isInited )
         {
            shutdownNative( m_queryHandle );
         }
      }
      finally
      {
         m_counterHandles.clear();
         m_queryHandle = 0;
         m_isInited = false;
      }
   }

   /**
    * Checks the supplied name with the operating system to see if it
    * refers to a valid counter.
    *
    * @param counterName The fully qualified counter name, assumed not <code>
    *    null</code> or empty.
    *
    * @return <code>true</code> if the counter is valid, <code>false</code>
    *    otherwise.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native boolean validateCounterName( String counterName )
      throws PSPerformanceMonitorException;

   /**
    * This method does the real work for {@link #addCounter}.
    *
    * @param queryHandle The handle returned by the {@link #init} method.
    *
    * @param counterName The NT format of the counter name, which is:
    *    \\machine\object(instance)\counter, where machine is optional
    *    Example: "\\xena\Processor(1)\% Processor Time" will sample the
    *    load on processor #1.
    *
    * @return If successful, the return value is a handle for the specified
    *    counter. If any problems, an exception is thrown.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native int addCounterNative( int queryHandle, String counterName )
      throws PSPerformanceMonitorException;

   /**
    * This method does the real work for {@link #getFormattedData}.
    *
    * @param queryHandle The handle returned by the {@link #addCounter} method.
    *
    * @return The value for the counter. It must be interpretted according to
    *    the documentation for the particular countr.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native long getFormattedDataNative( int counterHandle )
      throws PSPerformanceMonitorException;

   /**
    * This method tells the query containing all of the counters to collect
    * a sample for all of them.
    *
    * @param queryHandle The handle returned by the {@link #addCounter} method.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native void collectSampleNative( int queryHandle )
      throws PSPerformanceMonitorException;

   /**
    * This method initializes the performance tracking system. It must be
    * called before any counters can be added or samples collected.
    *
    * @return A handle to use when working with the other performance methods.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native int init()
      throws PSPerformanceMonitorException;

   /**
    * This method de-initializes the performance tracking system. It must be
    * called after all processing is complete to release native resources.
    *
    * @param queryHandle The handle returned by the {@link #init} method.
    *
    * @throws PSPerformanceCounterException If any problems occur at the OS
    *    level. The message will contain the error message #.
    */
   private native void shutdownNative( int queryHandle )
      throws PSPerformanceMonitorException;

   /**
    * A latch to indicate when the underlying counter query has been
    * initialized. It gets initialized when the first counter is added.
    */
   private boolean m_isInited = false;

   /**
    * For every successfully added counter, we add its name as the key and
    * its handle as the value (as an Integer) to the map. Never <code>null
    * </code>.
    */
   private Map m_counterHandles = new HashMap();

   /**
    * This is the handle to the counter query object returned by the {@link
    * #init} method. It is used when adding counters and shutting down.
    */
   private int m_queryHandle;

   /**
    * A latch to indicate that the associated dll has been loaded. The library
    * is loaded the first time an instance is created.
    */
   private static boolean ms_libLoaded = false;
}
