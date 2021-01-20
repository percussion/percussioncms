/*[ IPSPerformanceMonitor.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.performance;


/**
 * Classes implementing this interface are used to collect performance
 * statistics about a computer. The interface is designed to be very generic
 * so that various types of information could be collected. It is modelled
 * after the NT performance counters and uses similar terminology.
 * <p>In typical usage, you would call <code>addCounter</code>, then
 * repeatedly call <code>collectSample</code> and <code>getFormattedData
 * </code>. Finally, after you have completed your tests, call the <code>
 * shutdown</code> method to cleanup.
 */
public interface IPSPerformanceMonitor
{
   /**
    * Adds a counter to the set of counters that is currently being monitored
    * by this class. All counters that have been added get sampled when the
    * {@link #collectSample} method is called. Note that it is possible to
    * specify names that don't actually define an existing counter. If
    * possible, the name is validated when the counter is added. However, it
    * may not be known until you try to get formatted data.
    *
    * @param counterName A non-empty string that is interpretted by the
    *    implementing class. Usually it will identify a specific counter,
    *    although it could identify multiple counters if the implementing
    *    class supports some form of wildcard.
    *
    * @return <code>true</code> If the counter name validated successfully
    *    (if possible) and was added. <code>false</code> if the name failed
    *    validation.
    *
    * @throws IllegalArgumentException if counterName is <code>null</code> or
    *    empty.
    *
    * @throws PSPerformanceMonitorException If any problems occur adding the
    *    counter.
    */
   public boolean addCounter( String counterName )
      throws PSPerformanceMonitorException;

   /**
    * Forces a single sample to be collected for all counters in this monitor.
    * Any previously collected sample is lost. You must call {@link
    * #getFormattedData} to read the sample before collecting another one.
    *
    * @throws IllegalStateException If called before any counters have been
    *    added.
    *
    * @throws PSPerformanceMonitorException If any problems occur while
    *    collecting the sample.
    */
   public void collectSample()
      throws PSPerformanceMonitorException;

   /**
    * Used to request the last collected sample from the specified counter.
    *
    * @param counterName A name that matches the name passed to the <code>
    *    addCounter</code> method.
    *
    * @return The collected sample, formatted according to the counter type.
    *
    * @throws IllegalArgumentException if counterName is <code>null</code> or
    *    empty.
    *
    * @throws IllegalStateException If the supplied counter was never added.
    *
    * @throws PSPerformanceMonitorException If the counter is not valid.
    */
   public long getFormattedData( String counterName )
      throws PSPerformanceMonitorException;

   /**
    * This method must be called after the user has finished collecting
    * samples. It frees up native resources used by the class. If called
    * before any counters are successfully added, nothing is done. After
    * shutting down, this object has the same state it had when it was
    * initially created.
    *
    * @throws PSPerformanceMonitorException If any problems occur while
    *    cleaning up.
    */
   public void shutdown()
      throws PSPerformanceMonitorException;
}
