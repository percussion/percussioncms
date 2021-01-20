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
 * Shared memory is a piece of memory which can be accessed by multiple
 * processes on the same system. The users of the shared memory must agree
 * upon its structure as well as the access mechanisms to guarantee its
 * integrity. In particular, processes/threads should not read from areas
 * of the shared memory that are being written by other processes/threads.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSSharedMemory
{
   /**
    * Create or open named shared memory.
    *
    * @param      name         the name of the shared memory to get/create
    *
    * @param      allowCreate   <code>true</code> to create the shared
    *                           memory if it does not exist;
    *                           <code>false</code> otherwise
    *
    * @param      size         the size of the shared memory block to
    *                           allocate; ignored unless
    *                           <code>allowCreate</code> is
    *                           <code>true</code> and the object was not found
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the named object cannot be found and
    *                           <code>allowCreate</code> is <code>false</code>
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSSharedMemory(String name, boolean allowCreate, int size)
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      super();
      openNamed(name, allowCreate, size);
   }

   /**
    * Create this object using an existing chunk of shared memory.
    * The creator of this object is responsible for calling <code>dispose</code>
    * when finished to release resources back to the system. The
    * validity of the passed in handle is not checked until it is
    * used the first time.
    *
    * @param id The id of the shared memory to open. The id must be valid
    * in the context of the passed in process id, or the current process if the
    * passed in process id is 0.
    *
    * @param size the number of bytes in this shared memory block
    *
    * @param ownerPID The id of the process in which id was created. If it
    * is 0, id is assumed to be created in the current process.
    */
   public PSSharedMemory( int id, int size, int ownerPID )
      throws PSIpcObjectNotFoundException, PSIpcOSException
   {
      m_shmId = id;
      m_size = size;
      m_pid = ownerPID;
   }

   public int getSize()
   {
      return ( m_size );
   }

   public int getMemoryId()
   {
      return ( m_shmId );
   }


   public void dispose()
   {
      if ( ms_invalidShmId != m_shmId )
      {
         try
         {
            close( m_shmId );
            m_shmId = ms_invalidShmId;
         }
         catch ( PSIpcOSException e )
         {
            // nothing we can do now but log it
            ;
         }
      }
   }

   /**
    * Performs cleanup if the creator forgets to.
   **/
   protected void finalize() throws Throwable
   {
      dispose();
      super.finalize();
   }

   /**
    * Create unnamed shared memory.
    *
    * @param      size         the size of the shared memory block to
    *                           allocate
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSSharedMemory(int size)
      throws PSIpcOSException
   {
      super();
      createUnnamed(size);
   }

   /**
    * Get the input object for reading from this shared memory block.
    *
    * @return                           the shared memory reader
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSSharedMemoryInputStream getInputStream()
      throws PSIpcOSException
   {
      return new PSSharedMemoryInputStream( this );
   }

   /**
    * Get the output object for writing to this shared memory block.
    *
    * @return                           the shared memory writer
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   public PSSharedMemoryOutputStream getOutputStream()
      throws PSIpcOSException
   {
      return new PSSharedMemoryOutputStream( this );
   }

   /**
    * Release all resources associated with the shared memory. Any subsequent
    * calls on this object will throw an IllegalStateException. If the object
    * has already been closed, this call is silently ignored.
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   private native void close( int sharedMemId ) throws PSIpcOSException;


   /**
    * Open the specified shared memory by name, optionally creating it
    * if it does not exist. Upon success, this method will set the
    * m_semId variable to the OS specific shared memory id.
    *
    * @param      name         the name of the shared memory to get
    *
    * @param      allowCreate   <code>true</code> to create the shared
    *                           memory if it does not exist;
    *                           <code>false</code> otherwise
    *
    * @param      size         the size of the shared memory block to
    *                           allocate; ignored unless
    *                           <code>allowCreate</code> is
    *                           <code>true</code> and the object was not found
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the named object cannot be found and
    *                           <code>allowCreate</code> is <code>false</code>
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   private native void openNamed(String name, boolean allowCreate, int size)
      throws PSIpcObjectNotFoundException, PSIpcOSException;

   /**
    * Open the specified shared memory by id.
    * Upon success, this method will set the
    * m_semId variable to the OS specific shared memory id.
    *
    * @param      id            the id of the shared memory to get
    *
    * @param      ownerPID      the process id of the owner of the shared
    *                           memory. Various OSs require knowledge of
    *                           the creator in order to access the given
    *                           object.
    *
    * @exception   PSIpcObjectNotFoundException
    *                           if the object cannot be found
    *
    * @exception   PSIpcOSException   if an OS specific error occurrs
    */
   private native void openUnnamed(int id, int ownerPID)
      throws PSIpcObjectNotFoundException, PSIpcOSException;

   /**
    * Create unnamed shared memory.
    *
    * @param      size         the size of the shared memory block to
    *                           allocate
    *
    * @exception   PSIpcOSException      if an OS specific error occurrs
    */
   private native void createUnnamed(int size)
      throws PSIpcOSException;

   /**
    * This method is used to get the OS specific identifier for an
    * invalid shared memory id. It is used in the static initializer method
    * to set ms_invalidShmId.
    *
    * @return      the identifier for invalid shared memory ids
    */
   private static native int getInvalidShmId();


   /**
    * This is the OS specific identifier for an invalid shared memory id.
    */
   private static final int    ms_invalidShmId;
   private static final boolean ms_libraryLoaded = true;

   static
   {
      ms_invalidShmId = getInvalidShmId();
   }


   /**
    * This is the OS specific id used by the native
    * routines for accessing the shared memory.
    */
   private int m_shmId = ms_invalidShmId;
   private int m_size = 0;
   private int m_pid = 0;
}

