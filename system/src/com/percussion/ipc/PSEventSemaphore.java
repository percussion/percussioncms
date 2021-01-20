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

package com.percussion.ipc;


/**
 * Event semaphores provide a way for producers to signal a consumers that
 * they've created something the consumer is interested in. Unlike a mutex,
 * event semaphores act like counters rather than locks. When a producer
 * signals the
 * semaphore the count goes up. The consumer wakes up whenever the count is
 * greater than 0. Once all the produce is consumed, the count goes back down
 * to 0 and the consumer must wait until a producer signals him.
 * <P>
 * The primary use of event semaphores within E2 is for the hook process
 * (eg, ISAPI filter) to signal the E2 server when an incoming request has
 * been added to the shared memory.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSEventSemaphore
{
   /**
    * Create or open a named event semaphore. We use manual reset event
    * semaphores. The concept is that
    * a consumer (or set of consumers) will access the data associated with
    * the event semaphore when it enters a signaled state. The producers
    * will continue to signal the Semaphore as they add more data. Both
    * consumers and producers must lock the shared data before adding or
    * removing data through a PSMutexSemaphore object. 
    *
    * @param      name         the name of the event semaphore to get/create
    *
    * @param      allowCreate   <code>true</code> to create the event
    *                           semaphore if it does not exist;
    *                           <code>false</code> otherwise
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the named object cannot be found and
    *                           <code>allowCreate</code> is <code>false</code>
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSEventSemaphore(String name, boolean allowCreate)
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      super();
      openNamed(name, allowCreate);
   }

   /**
    * Open an unnamed event semaphore. We use manual reset event
    * semaphores. The concept is that
    * a consumer (or set of consumers) will access the data associated with
    * the event semaphore when it enters a signaled state. The producers
    * will continue to signal the Semaphore as they add more data. Both
    * consumers and producers must lock the shared data before adding or
    * removing data through a PSMutexSemaphore object. 
    *
    * @param      id            the id of the semaphore to open
    *
    * @param      ownerPID      the process id of the owner of the event
    *                           semaphore. Various OSs require knowledge of
    *                           the creator in order to access the given
    *                           object.
    *
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the object cannot be found
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSEventSemaphore(int id, int ownerPID)
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      super();
      openUnnamed(id, ownerPID);
   }

   /**
    * Create an unnamed event semaphore. We use manual reset event
    * semaphores. The concept is that
    * a consumer (or set of consumers) will access the data associated with
    * the event semaphore when it enters a signaled state. The producers
    * will continue to signal the Semaphore as they add more data. Both
    * consumers and producers must lock the shared data before adding or
    * removing data through a PSMutexSemaphore object. 
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSEventSemaphore()
      throws PSIpcOSException
   {
      super();
      createUnnamed();
   }

   /**
    * Close the event semaphore. Any subsequent calls on this object will
    * throw an IllegalStateException. If the object has already been
    * closed, this call is silently ignored.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public native void close() throws PSIpcOSException;

   /**
    * Post to the event semaphore. This will cause any threads/processes
    * waiting on the event semaphore to wake up.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    *
    * @exception   IllegalStateException   if a previous call to
    *                                       <code>close</code> was made
    */
   public native void post()
      throws PSIpcOSException, java.lang.IllegalStateException;

   /**
    * Reset the event semaphore to a non-signaled state.
    * This should be called once a consumer has consumed all of the resource.
    * Be sure to protect the resources being
    * produced/consumed with a mutex to avoid loss of events.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    *
    * @exception   IllegalStateException   if a previous call to
    *                                       <code>close</code> was made
    */
   public native void reset()
      throws PSIpcOSException, java.lang.IllegalStateException;

   /**
    * Wait for the event semaphore. This will wait until another thread or
    * process calls post to signal that an event has occurred. The
    * event semaphore remains in a signaled state at this point. When
    * the consumer has consumed all of the resource,
    * <code>reset</code> should be called to reset the event semaphore to
    * a non-signaled state. Be sure to protect the resources being
    * produced/consumed with a mutex to avoid loss of events.
    *
    * @param      waitMS               the amount of time to wait (in
    *                                    milliseconds)
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    *
    * @exception   IllegalStateException   if a previous call to
    *                                       <code>close</code> was made
    *
    * @exception   InterruptedException   if <code>waitMS</code> milliseconds
    *                                    have elapsed without an event
    *                                    occurring
    */
   public native void wait(int waitMS)
      throws PSIpcOSException, java.lang.IllegalStateException,
         java.lang.InterruptedException;

   /**
    * Open the specified event semaphore by name, optionally creating it
    * if it does not exist. Upon success, this method will set the
    * m_semId variable to the OS specific semaphore id.
    *
    * @param      name         the name of the event semaphore to get
    *
    * @param      allowCreate   <code>true</code> to create the event
    *                           semaphore if it does not exist;
    *                           <code>false</code> otherwise
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the named object cannot be found and
    *                           <code>allowCreate</code> is <code>false</code>
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   private native void openNamed(String name, boolean allowCreate)
      throws PSIpcObjectNotFoundException, PSIpcOSException;

   /**
    * Open the specified event semaphore by id.
    * Upon success, this method will set the
    * m_semId variable to the OS specific semaphore id.
    *
    * @param      id            the id of the event semaphore to get
    *
    * @param      ownerPID      the process id of the owner of the event
    *                           semaphore. Various OSs require knowledge of
    *                           the creator in order to access the given
    *                           object.
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the object cannot be found
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   private native void openUnnamed(int id, int ownerPID)
      throws PSIpcObjectNotFoundException, PSIpcOSException;

   /**
    * Create an unnamed event semaphore. We use manual reset event
    * semaphores. The concept is that
    * a consumer (or set of consumers) will access the data associated with
    * the event semaphore when it enters a signaled state. The producers
    * will continue to signal the Semaphore as they add more data. Both
    * consumers and producers must lock the shared data before adding or
    * removing data through a PSMutexSemaphore object. 
    * <P>
    * It is possible for this routine to create an event semaphore
    * object but not attach to it. This happens when <code>allowCreate</code>
    * is <code>false</code> and an event semaphore by the given name does
    * not exist. 
    *
    * @exception   PSIpcOSException       if an OS specific error occurrs
    */
   private native void createUnnamed()
      throws PSIpcOSException;

   /**
    * This method is used to get the OS specific identifier for an
    * invalid semaphore. It is used in the static initializer method
    * to set ms_invalidSemId.
    *
    * @return      the identifier for invalid semaphores
    */
   private static native int getInvalidSemId();


   /**
    * This is the OS specific identifier for an invalid semaphore id.
    */
   private static final int    ms_invalidSemId;

   static {
      // *TODO* load the native library PSUtil (contains C++ classes)

      // *TODO* set ms_invalidSemId to the OS specific id
      ms_invalidSemId = getInvalidSemId();
   }


   /**
    * This is the OS specific id used by the native
    * routines for accessing the event semaphore.
    */
   private int m_semId = ms_invalidSemId;
}

