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

package com.percussion.server.cache;

import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A container for an object that is cached.  Provides meta data regarding the
 * lifetime of the object in the cache.
 */
class PSCacheItem 
{
   /**
    * Creates a cached item for the supplied <code>object</code>.
    * 
    * @param cacheId Identifies the cache in which this item is stored.
    * @param object The object to cache, may not be <code>null</code>.
    * @param keys The keys used to identify this item.  See 
    * {@link PSMultiLevelCache#addItem(Object[], Object, long) addItem} for a
    * description of keys.  May not be <code>null</code>.
    * @param size The size, in Bytes, that this object should be thought of as
    * occupying in the cache.  May not be less than <code>0</code>.
    * 
    * @throws IllegalArgumentException if <code>object</code> or 
    * <code>keys</code> is <code>null</code> or size is less than 
    * <code>0</code>.
    */
   public PSCacheItem(int cacheId, Object object, Object[] keys, long size)
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");
         
      if (keys == null)
         throw new IllegalArgumentException("keys may not be null");
         
      if (size < 0)
         throw new IllegalArgumentException("size may not be less than zero");
         
      m_cache_id = cacheId;
      m_object = object;
      m_keys = keys;
      m_size = size;   
      
      m_created = new Date();
      m_accessed = m_created;
   }
   
   /**
    * Get the id of the cache in which this item is stored.
    * 
    * @return the id.
    */
   public int getCacheId()
   {
      return m_cache_id;
   }
   
   /**
    * Gets the keys that uniquely identify this object.
    * 
    * @return The keys, never <code>null</code>.  See 
    * {@link PSMultiLevelCache#addItem(Object[], Object, long) addItem} for more
    * info.
    */
   public Object[] getKeys()
   {
      return m_keys;
   }
   
   /**
    * Get a <code>String</code> representation of keys that uniquely
    * idenitifies this item.  See {@link #toString(Object[])} for more info.
    * 
    * @return The <code>String</code> representation, never <code>null</code> 
    * or empty.
    */
   public String toString()
   {
      return toString(getKeys());
   }
   
   /**
    * Creates a string representation of the provided keys in the format
    * <code>"[key1, key2, key3[, keyn]]"</code>.
    * 
    * @param keys An array of keys, may not be <code>null</code>.  
    * <code>toString()</code> is called on each object in the array.
    * 
    * @return The string, never <code>null</code> or empty.  If 
    * <code>keys</code> is empty, <code>"[]"</code> will be returned.  
    * <code>null</code> entries will be displayed with the value 
    * <code>"null"</code>.
    * 
    * @throws IllegalArgumentException if <code>keys</code> is 
    * <code>null</code>.
    */
   public static String toString(Object[] keys)
   {
      if (keys == null)
         throw new IllegalArgumentException("keys may not be null");
         
      StringBuilder buf = new StringBuilder();

      buf.append("[");
      for (int i = 0; i < keys.length; i++) 
      {
         if (i > 0)
            buf.append(", ");
         Object o = keys[i];
         buf.append(o == null ? "null" : o.toString());
      }
      buf.append("]");
      
      return buf.toString();
   }
   
   /**
    * Gets the cached object, which also updates the last accessed time for this 
    * item.
    * 
    * @return The object, may be <code>null</code> if it has been released since
    * caller received the reference to this item.
    * 
    * @throws PSCacheException if there are any unexpected errors.
    * 
    * @see #getLastAccessedDate()
    */
   public Object getObject() throws PSCacheException
   {
      Object object = m_object;
      int action = PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_MEMORY;
      
      // double-check idiom
      if (m_isOnDisk)
      {
         synchronized(m_objectMonitor)
         {
            if (m_isOnDisk && !m_released)
            {
               // load from disk
               try
               {
                  m_object = getObjectFromDisk(m_diskObject);
                  m_diskObject.delete();
                  m_diskObject = null;
                  m_isOnDisk = false;
                  action = PSCacheEvent.CACHE_ITEM_ACCESSED_FROM_DISK;
               }
               catch (IOException e)
               {
                  PSCacheException ce = new PSCacheException(
                     IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, 
                     e.getLocalizedMessage());   
                  
                  throw ce;
               }
               catch (ClassNotFoundException e)
               {
                  PSCacheException ce = new PSCacheException(
                     IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, 
                     e.getLocalizedMessage());   
                  
                  throw ce;
               }
               
            }
            // set this either way in case object was moved from disk before the
            // monitor was acquired.
            object = m_object;
         }
      }

      // make sure we've got one
      if (object != null)
      {
         // set accessed time
         m_accessed = new Date();
         
         // notify listeners
         notifyAccessListeners(action);      
      }
      
      return object;
   }

   /** 
    * Gets the point in time that this item was first created.  
    * 
    * @return The point in time as a <code>Date</code> object, never 
    * <code>null</code>.
    */
   public Date getCreatedDate()
   {
      return m_created;
   }
   
   /** 
    * Gets the point in time that this item was last accessed.  
    * 
    * @return The point in time as a <code>Date</code> object, never 
    * <code>null</code>.
    */
   public Date getLastAccessedDate()
   {
      return m_accessed;
   }
   
   /**
    * Gets the reported size of this object supplied during construction.
    * 
    * @return The size, never less than <code>0</code>.
    */
   public long getSize()
   {
      return m_size;
   }
   
   /**
    * Causes this item to serialize its object to disk.  The next time this 
    * item is accessed, it will automatically retrieve the item from disk and
    * notify any listeners of a retrieve event.  
    * 
    * @param dir The directory in which the file should be stored.  May not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if item was moved to disk, <code>false</code> if
    * item is already on disk or has been released.
    * 
    * @throws IllegalArgumentException if <code>dir</code> is <code>null</code>
    * or does not denote a directory.
    * @throws NotSerializableException if the object or one of its members 
    * does not implement the {@link java.io.Serializable} interface.
    * @throws PSCacheException if there are any other errors.
    * 
    * @see IPSCacheModifiedListener
    */
   public boolean toDisk(File dir) throws NotSerializableException, 
      PSCacheException
   {
      if (dir == null)
         throw new IllegalArgumentException("dir may not be null");
      
      if (dir.exists() && !dir.isDirectory())
         throw new IllegalArgumentException("dir must be a directory");   

      boolean moved = false;      
      synchronized (m_objectMonitor)
      {
         if (!m_isOnDisk && !m_released)
         {               
            try
            {
               m_diskObject = putObjectOnDisk(m_object, dir);
            }
            catch (NotSerializableException e)
            {
               throw e;
            }
            catch (IOException e)
            {
               PSCacheException ce = new PSCacheException(
                  IPSServerErrors.CACHE_UNEXPECTED_EXCEPTION, 
                  e.getLocalizedMessage());   
               
               throw ce;
            }
            
            moved = true;
            m_isOnDisk = true;
            m_object = null;
         }
      }
         
      if (moved)
         notifyModifiedListeners(PSCacheEvent.CACHE_ITEM_STORED_TO_DISK);   
         
      return moved;
   }
   
   /**
    * Determine if item is stored on disk.
    * 
    * @return <code>true</code> if the item is on disk, <code>false</code> if in 
    * memory.
    */
   public boolean isOnDisk()
   {
      return m_isOnDisk;
   }
   
   /**
    * Determine if item is stored in memory.
    * 
    * @return <code>true</code> if the item is stored in memory, 
    * <code>false</code> if on disk.
    */
   public boolean isInMemory()
   {
      return !m_isOnDisk;
   }
   
   /**
    * Adds the supplied listener to this object's notifier list so that it may
    * be notified each time this item is accessed by calling 
    * {@link #getObject()}, and {@link #toDisk(File)}. 
    * 
    * @param listener The listener to notify, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void addCacheAccessedListener(IPSCacheAccessedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      synchronized (m_cacheAccessedListeners)
      {
         m_cacheAccessedListeners.add(listener);
      }
   }   
   
   /**
    * Adds the supplied listener to this object's notifier list so that it may
    * be notified each time this item is moved to disk or retrieved
    * from disk. 
    * 
    * @param listener The listener to notify, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void addCacheModifiedListener(IPSCacheModifiedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      synchronized (m_cacheModifiedListeners)
      {
         m_cacheModifiedListeners.add(listener);
      }
   }   
   
   /**
    * Removes the listener from this object's access notifier list.  See
    * {@link #addCacheAccessedListener(IPSCacheAccessedListener)}
    * 
    * @param listener The listener to remove, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void removeCacheAccessedListener(IPSCacheAccessedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      synchronized (m_cacheAccessedListeners)
      {
         m_cacheAccessedListeners.remove(listener);
      }
   }
   
   /**
    * Removes the listener from this object's modified notifier list.  See
    * {@link #addCacheModifiedListener(IPSCacheModifiedListener)}
    * 
    * @param listener The listener to remove, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if listener is <code>null</code>.
    */
   public void removeCacheModifiedListener(IPSCacheModifiedListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
         
      synchronized (m_cacheModifiedListeners)
      {
         m_cacheModifiedListeners.remove(listener);
      }
   }
   
   /**
    * Frees any resources used by this item such the reference to the object or 
    * any files on disk. Will not trigger any listener events.  This method is
    * idempotent.
    */
   public void release()
   {
      if (!m_released)
      {
         synchronized (m_objectMonitor)
         {
            m_released = true;
            m_object = null;
            if (m_diskObject != null)
            {
               m_diskObject.delete();
               m_diskObject = null;
            }
         }
         
         synchronized (m_cacheAccessedListeners)
         {
            m_cacheAccessedListeners.clear();
         }
         
         synchronized (m_cacheModifiedListeners)
         {
            m_cacheModifiedListeners.clear();
         }
      }
   }
   
   /**
    * Gets this item's object from disk.  
    * 
    * @param file The file containing the object on disk.  Assumed not 
    * <code>null</code> and to exist.
    * 
    * @return The object, never <code>null</code>.
    * 
    * @throws FileNotFoundException if file does not exist.
    * @throws ClassNotFoundException if the object's class cannot be found.
    * @throws IOException if there are any other errors.
    */
   private Object getObjectFromDisk(File file) throws FileNotFoundException, 
      IOException, ClassNotFoundException
   {
      Object o = null;
      try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
         o = in.readObject();
         return o;
      }

   }

   /**
    * Stores the supplied file on disk in a temp file.
    * 
    * @param object The object to store, assumed not <code>null</code> and
    * to implement {the @link java.io.Serializable} interface.
    * @param dir The directory to store it in, assumed not <code>null</code> and
    * to be a reference to a directory.
    * 
    * @return A reference to the file, never <code>null</code>.
    * 
    * @throws NotSerializableException if the object or one of its members 
    * does not implement the {@link java.io.Serializable} interface.
    * @throws IOException if there are any other errors.
    */    
   private File putObjectOnDisk(Object object, File dir) 
      throws IOException, NotSerializableException
   {
      if (!dir.exists())
         dir.mkdirs();
         
     File file = new PSPurgableTempFile("psx", ".csh", dir);

      try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))){
         out.writeObject(object);
      }

      return file;
   }
   
   
   /**
    * Notifes all <code>IPSCacheAccessedListener</code>s of an event with the
    * specified action.
    * 
    * @param action The action, assumed to be a valid action for the listeners.
    */
   private void notifyAccessListeners(int action)
   {
      synchronized (m_cacheAccessedListeners)
      {
         if (!m_cacheAccessedListeners.isEmpty())
         {
            PSCacheEvent event = new PSCacheEvent(action, this);
            Iterator<IPSCacheAccessedListener> i = m_cacheAccessedListeners.iterator();
            while (i.hasNext()) 
            {
               IPSCacheAccessedListener listener = 
                  i.next();
               listener.cacheAccessed(event);
            }
         }
      }
   }
   
   /**
    * Notifes all <code>IPSCacheModifiedListener</code>s of an event with the
    * specified action.
    * 
    * @param action The action, assumed to be a valid action for the listeners.
    */
   private void notifyModifiedListeners(int action)
   {
      synchronized (m_cacheModifiedListeners)
      {
         if (!m_cacheModifiedListeners.isEmpty())
         {
            PSCacheEvent event = new PSCacheEvent(action, this);
            Iterator<IPSCacheModifiedListener> i = m_cacheModifiedListeners.iterator();
            while (i.hasNext()) 
            {
               IPSCacheModifiedListener listener = 
                  i.next();
               listener.cacheModified(event);
            }
         }
      }
   }
   
   /**
    * The id of the cache in which this item is stored.  Set during 
    * construction, never modified after that.
    */
   private int m_cache_id;
   
   /**
    * The object stored by this item.  Intialized during construction, may be
    * <code>null</code> only if the object is currently stored on disk, or if 
    * the <code>release</code> method has been called.
    */
   private Object m_object;
   
   /**
    * The keys used to uniquely identify this object in the cache.  Never 
    * <code>null</code> or modified after construction.
    */
   private Object[] m_keys;
   
   /**
    * The estimated size, in bytes, of the object stored by this item.  Set 
    * during ctor, never modified after that.
    */
   private long m_size;
   
   /**
    * The time at which this item was created, set during ctor, never modified.
    */
   private Date m_created;
   
   /**
    * The time at which this item was last accessed, set initially during ctor, 
    * set to current time each time this item is accessed by calling 
    * {@link #getObject()}.
    */
   private Date m_accessed;
   
   /**
    * <code>true</code> value indicates this item is currently stored on disk,
    * <code>false</code> otherwise.
    */
   private boolean m_isOnDisk = false;
   
   /**
    * If <code>true</code>, the {@link #release()} method has been called,
    * <code>false</code> otherwise.
    */
   private boolean m_released = false;
   
   /**
    * List of <code>IPSCacheModifiedListener</code> objects to notify if this 
    * item is moved to or from disk.  Never <code>null</code>, may be empty.
    */
   private List<IPSCacheModifiedListener> m_cacheModifiedListeners = new ArrayList<>();
   
   /**
    * List of <code>IPSCacheAccessedListener</code> objects to notify if this 
    * item is accessed by calling {@link #getObject()}.  Never 
    * <code>null</code>, may be empty.
    */
   private List<IPSCacheAccessedListener> m_cacheAccessedListeners = new ArrayList<>();
   
   /**
    * Monitor object used to synchronize access to the object while moving it
    * to or from disk.
    */
   private Object m_objectMonitor = new Object();
   
   /**
    * Reference to this item's object when it is stored on disk.  Initially
    * <code>null</code>, will not be <code>null</code> while the object is 
    * stored on disk as a result of calling {@link #toDisk(File)}.
    */
   private File m_diskObject = null;
}
