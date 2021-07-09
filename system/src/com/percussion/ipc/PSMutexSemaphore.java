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

package com.percussion.ipc;


/**
 * A mutex semaphore is one way to synchronize shared memory. Mutex
 * stands for mutual exclusion. When a thread or process wants access to a 
 * piece of shared memory, it must first acquire the mutex. The mutex only
 * permits a single thread or process to acquire it at any given time. This
 * guarantees serial access to the area of shared memory. When a user is done
 * with the shared memory, they must then release the mutex to allow others
 * access to it. It is important to minimize the amount of time spend holding
 * the mutex lock. The longer the lock is held, the longer other threads and
 * processes must wait. Furthermore, caution must be used when acquiring
 * multiple mutex locks. It is possible to get into deadlock conditions, in
 * which case no one can access the resource as the holder of one mutex is
 * trying to get the other mutex and vice versa.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSMutexSemaphore
{
   /**
    * Create or open a named mutex semaphore.
    *
    * @param      name         the name of the mutex semaphore to get/create
    *
    * @param      allowCreate   <code>true</code> to create the mutex
    *                           semaphore if it does not exist;
    *                           <code>false</code> otherwise
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the named object cannot be found and
    *                           <code>allowCreate</code> is <code>false</code>
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSMutexSemaphore(String name, boolean allowCreate)
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      super();
      openNamed(name, allowCreate);
   }

   /**
    * Open an unnamed mutex semaphore.
    *
    * @param      id            the id of the semaphore to open
    *
    * @param      ownerPID      the process id of the owner of the mutex
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
   public PSMutexSemaphore(int id, int ownerPID)
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      super();
      openUnnamed(id, ownerPID);
   }

   /**
    * Create an unnamed mutex semaphore.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSMutexSemaphore()
      throws PSIpcOSException
   {
      super();
      createUnnamed();
   }

   /**
    * Close the mutex semaphore. Any subsequent calls on this object will
    * throw an IllegalStateException. If the object has already been
    * closed, this call is silently ignored.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public native void close() throws PSIpcOSException;

   /**
    * Release the lock on this mutex semaphore.
    * This will cause any threads/processes
    * waiting on the mutex semaphore to wake up.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    *
    * @exception   IllegalStateException   if a previous call to
    *                                       <code>close</code> was made
    */
   public native void release()
      throws PSIpcOSException, java.lang.IllegalStateException;

   /**
    * Acquire ownership of the mutex semaphore. When the mutex is acquired,
    * it is crucial for the caller to call release when it is no longer
    * needed. While one thread/process holds the mutex, no other
    * thread/process can access resource being protected. This can lead to
    * starvation or deadlocks, if multiple mutexes are being used improperly.
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
    *                                    have elapsed without an mutex
    *                                    occurring
    */
   public native void wait(int waitMS)
      throws PSIpcOSException, java.lang.IllegalStateException,
         java.lang.InterruptedException;

   /**
    * Open the specified mutex semaphore by name, optionally creating it
    * if it does not exist. Upon success, this method will set the
    * m_semId variable to the OS specific semaphore id.
    *
    * @param      name         the name of the mutex semaphore to get
    *
    * @param      allowCreate   <code>true</code> to create the mutex
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
    * Open the specified mutex semaphore by id.
    * Upon success, this method will set the
    * m_semId variable to the OS specific semaphore id.
    *
    * @param      id            the id of the mutex semaphore to get
    *
    * @param      ownerPID      the process id of the owner of the mutex
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
    * Create an unnamed mutex semaphore. We use manual reset mutex
    * semaphores. The concept is that
    * a consumer (or set of consumers) will access the data associated with
    * the mutex semaphore when it enters a signaled state. The producers
    * will continue to signal the Semaphore as they add more data. Both
    * consumers and producers must lock the shared data before adding or
    * removing data through a PSMutexSemaphore object. 
    * <P>
    * It is possible for this routine to create an mutex semaphore
    * object but not attach to it. This happens when <code>allowCreate</code>
    * is <code>false</code> and an mutex semaphore by the given name does
    * not exist.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
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
    * routines for accessing the mutex semaphore.
    */
   private int m_semId = ms_invalidSemId;
}

