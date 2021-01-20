/*[ EmpireTestServerLockManager.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Enumeration;
import com.percussion.util.PSMapClassToObject;

public class EmpireTestServerLockManager
{
   public static final int EXCLUSIVE = 0;

   /**
    * Constructs a lock manager for an XML object store.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/7
    * 
    * @param   lockDir   The base directory for locks
    * 
    * @throws   FileNotFoundException If the directory does not exist.
    * 
    */
   public EmpireTestServerLockManager(File lockDir)
      throws FileNotFoundException
   {
      // make sure the file exists and is a directory
      if (!lockDir.exists() || !lockDir.isDirectory())
         throw new FileNotFoundException(lockDir.getAbsolutePath());

      m_lockDir = lockDir;

      // set up the subdirectories for different lock types
      initDirs();
   }

   /**
    * Returns the uniquely identifying lock key for this object under
    * the given type of lock, throwing an exception if no lock key
    * can be found for this object.
    *
    * @author  chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockOb The object whose lock key you want to obtain.
    *
    * @param   lockType   The type of lock requested, or 0
    * if all applicable access types are requested at once.
    * 
    * @return  Object The lock key for this object. Keys should be
    * lightweight identifier objects that identify a lock. Keys do not
    * act as a lock; synchronizing on (or garbage collection of) a key
    * will not interfere with the lock nor with any other keys that
    * refer to the same lock.
    * 
    */
   public Object getLockKey(Object lockOb, int lockType)
   {
      if (null == lockOb)
      {
         throw new IllegalArgumentException("lock object null");
      }

      return getLockKey(lockOb.getClass(), lockOb, lockType);
   }

   /**
    * Returns the uniquely identifying lock key for this object under
    * the given type of lock, throwing an exception if no lock key
    * can be found for this object.
    *
    * @author  chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockObClass The class of the object whose lock key you
    * want to obtain.
    *
    * @param   unique   A unique indentifier for the object, which
    * may be either the object itself or a String or other identifier,
    * depending on the implementation.
    * 
    * @return  Object The lock key for this object. Keys should be
    * lightweight identifier objects that identify a lock. Keys do not
    * act as a lock; synchronizing on (or garbage collection of) a key
    * will not interfere with the lock nor with any other keys that
    * refer to the same lock.
    *
    */
   public Object getLockKey(Class lockObClass, Object unique, int lockType)
   {
      if (null == lockObClass || null == unique)
      {
         throw new IllegalArgumentException("lock object null");
      }

      // we use lockType as an index into the m_keyGens array
      if (lockType >= m_keyGens.length)
      {
         throw new IllegalArgumentException("Bad lock type: " + lockType);
      }

      // get the lock file for this object
      FileGenerator keyGen
         = (FileGenerator)m_keyGens[lockType].getMapping(lockObClass);

      if (keyGen == null)
      {
         throw new IllegalArgumentException("Bad lock object: " + lockObClass.getName());
      }

      // and return the file inside a key
      return new FileLockKey(
         new File(keyGen.getLockFile(unique).getAbsolutePath()));
   }

   /**
    * Acquires a lock on the object referenced by the given key,
    * which must have been obtained by calling getLockKey for the
    * object you want to lock. If the locker already has a lock,
    * then this method should succeed. If the locker already has
    * a lock and the value of the <CODE>lockExpiresMs</CODE>
    * parameter would cause the lock to expire sooner than it
    * was going to expire before this method was called, then this
    * method should not change the expiration.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/18
    * 
    * @param   lockerId   An object which uniquely identifies the locker.
    * The value should be unique from other IDs.
    *
    * @param   lockKey The lock key, which must have been obtained
    * by calling {@link #getLockKey getLockKey}.
    *
    * @param   lockExpiresMs The period (starting from when the lock
    * is acquired) after which the manager is free
    * release the lock on the session's behalf and grant locks to other
    * sessions that will exclude locks of this type. This parameter
    * should not be construed as a guarantee that the lock will be
    * released in the given amount of time, although implementations
    * should make a best effort to do so.
    *
    * @param   waitTimeoutMs If the lock cannot be acquired immediately,
    * this call will block for approximately the specified number of
    * milliseconds, retrying the acquire at periodic intervals. If this
    * parameter is 0, then this method will return false immediately if
    * the lock cannot be obtained. A value of < 0 means that this
    * call will block until the lock is acquired.
    * 
    * @param   lockResults The manager will fill out this results object
    * with detailed information about the lock acquisition attempt if
    * <CODE>lockResults</CODE> is not <CODE>null</CODE>. If it is
    * <CODE>null</CODE>, no detailed information will be filled out.
    *
    * @return   boolean <CODE>true</CODE>if a lock of the specified type
    * was acquired, <CODE>false</CODE> if not. Note that this method will
    * return true whenever the user session has the lock by the time the
    * method returns, even if the lock was not acquired as a direct consequence
    * of calling this method (for example, if this user session already has
    * the lock of the requested type on the object referenced by the lock key).
    * 
    */
   public boolean acquireLock(
      EmpireLockerId lockerId,
      Object lockKey,
      long lockExpiresMs,
      long waitTimeoutMs)
   {
      if (lockerId == null)
      {
         throw new IllegalArgumentException("Bad locker ID: " + lockerId);
      }

      if (lockKey == null)
      {
         throw new IllegalArgumentException("Bad lock key: " + lockKey);
      }

      if (lockExpiresMs < 0)
      {
         throw new IllegalArgumentException("Bad expiration: " + lockExpiresMs);
      }

      if (lockExpiresMs == 0)
      {
         releaseLock(lockerId, lockKey);
         return true;
      }

      // we only give out instances of FileLockKey, and the user cannot
      // create them because it is a private class, so this is a pretty
      // safe check for a valid key
      if (!(lockKey instanceof FileLockKey))
      {
         throw new IllegalArgumentException("Bad lock key: " + lockKey.getClass().getName());
      }

      System.out.println("Preparing for try/sleep acquisition loop");

      // prepare to acquire the lock in a try/sleep loop

      FileLockKey key = (FileLockKey)lockKey;

      boolean acquired = false;
      long slept = 0; // how long have we slept (total) in the wait loop
      while (!acquired)
      {
         try
         {
            File f = key.getFile();

            // see if we can grab the lock
            acquired = createIfExpired(
               f,
               lockExpiresMs,
               lockerId);
            
            if (!acquired)
            {
               // the lock is still held, so perform the applicable sleep logic
               if (waitTimeoutMs < 0)
               {
                  // they want to keep waiting and retrying forever
               }
               // else, have we timed out (or is waitTimeoutMs == 0)
               else if (slept >= waitTimeoutMs)
               {
                  break; // timed out or wait was 0
               }

               // wait for a little while before retrying
               // TODO: tune this for performance
               System.out.println("sleeping for 1sec...");
               Thread.sleep(1000);
               slept += 1000;
            }
            else
            {
               break; // we acquired the lock
            }
         }
         catch (NumberFormatException nfe)
         {
            throw new RuntimeException("Corrupt lockfile: " + nfe.getMessage());
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Corrupt lockfile: " + ioe.getMessage());
         }
         catch (InterruptedException inte)
         {
            // ignore, acquired will be false
         }
      }

      System.out.println("acquired=" + acquired);

      return acquired;
   }

   /**
    * Creates the lock file only if it didn't exist or if it existed but was expired.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   f
    * @param   expires How many milliseconds from now the lock shall expire in.
    * @param   lockerId
    * 
    * @return   boolean <CODE>true</CODE> if the lock file was created or
    * expired, <CODE>false</CODE> if the lock file existed but was not expired.
    * 
    * @throws   IOException If an error occurred reading or writing the lock file.
    *
    */
   private boolean createIfExpired(
      File f,
      long expires,
      EmpireLockerId lockerId)
      throws IOException
   {
      // we synchronize on the proper object so that delete/lock happens
      // atomically (without dangerous, simultaneous file IO requests)
      synchronized (getSynch(f))
      {
         boolean existed = !f.createNewFile();
         boolean reLock = false; // does the locker already have a lock ?
         
         // if the lock already exists, the system time of its creation
         long created = 0;

         // if the lock already exists, the number of milliseconds from its
         // creation that it expires
         long expiresIn = 0;

         if (existed)
         {
            // see if the lock has expired by reading the file's time information
            Properties props = new Properties();
            FileInputStream fIn = new FileInputStream(f);
            try
            {
               props.load(fIn);
            }
            finally
            {
               fIn.close();
            }

            String createdStr = props.getProperty("created");
            String expiresStr = props.getProperty("expires"); // milliseconds

            EmpireLockerId prevLocker = new EmpireLockerId(props);

            if (createdStr == null || expiresStr == null)
            {
               throw new IOException("Corrupt lockfile (" + f.toString() +
                  " . Could not calculate expiration.");
            }

            try
            {
               created = Long.parseLong(createdStr);
               expiresIn = Long.parseLong(expiresStr);
            }
            catch (NumberFormatException nfe)
            {
               throw new IOException("Corrupt lockfile (" + f.toString() +
                  " . Could not calculate expiration.");
            }

            if (prevLocker.sameId(lockerId))
            {
               // the locker is requesting to re-lock the file, so we let
               // them relock it and refresh the expiration
               reLock = true;
            }
            else if ( (created + expiresIn) <= System.currentTimeMillis() )
            {
               // the lock has expired, so delete the file
               f.delete();

               // and create it again so we can initialize it below
               if (!f.createNewFile())
               {
                  // expired, but could not delete
                  throw new IOException("Could not delete expired lockfile: " + f.toString());
               }
            }
            else // lock hasn't expired
            {
               long minutes = ((created + expiresIn) - System.currentTimeMillis()) / 60000;
               return false; //  the lock hasn't expired, return false
            }
         }

         // Initialize the lock file.
         // If the user is re-locking, then only update the information if the
         // new lock would expire later than the old lock.
         // Otherwise just update the lock unconditionally (both created and
         // expiresIn will be set to 0 if the lock didn't exist, which will
         // cause the test to succeed).
         if (!reLock
            || (created + expiresIn <= (System.currentTimeMillis() + expires)))
         {
            Properties outProps = new Properties();
            lockerId.writeTo(outProps);
            outProps.setProperty("created", "" + System.currentTimeMillis());
            outProps.setProperty("expires", "" + expires);

            FileOutputStream fOut = new FileOutputStream(f);
            try
            {
               outProps.store(fOut, "Lock file generated by autotest. Do not edit.");
            }
            finally
            {
               fOut.close();
            }
         }
      }

      return true; // we created the lock (possibly after deleting an expired)
   }

   /**
    * Releases the lock associated with the given key.
    * <P>IF there is no lock
    * associated with <CODE>lockKey</CODE>
    * <P>OR if there is a lock associated with
    * <CODE>lockKey</CODE> but it does not belong to
    * <CODE>lockerId</CODE>
    * <P>OR if the lock key is of the wrong type
    * <P>THEN nothing will happen and the operation will
    * appear to complete successfully.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/6/18
    *
    * @param   lockerId   An object which uniquely identifies the
    * user requesting lock release. The lock will only be released
    * if this lockerId is the same id as the original locker.
    * 
    * @param   lockKey The lock key, which must have been obtained
    * by calling {@link #getLockKey getLockKey}.
    */
   public void releaseLock(EmpireLockerId lockerId, Object lockKey)
   {
      if (lockerId == null)
         return;

      if (lockKey == null)
         return;

      if (!(lockKey instanceof FileLockKey))
         return;

      FileLockKey key = (FileLockKey)lockKey;

      // get the lockfile associated with this key
      File f = key.getFile();

      if (!f.exists())
         return; // no-one had it locked, so return success

      synchronized (getSynch(f))
      {
         try
         {
            Properties props = new Properties();
            FileInputStream fIn = new FileInputStream(f);
            try
            {
               props.load(fIn);
            }
            finally
            {
               fIn.close();
            }

            String createdStr = props.getProperty("created");
            String expiresStr = props.getProperty("expires"); // milliseconds

            EmpireLockerId prevLocker = new EmpireLockerId(props);

            if (createdStr == null || expiresStr == null)
            {
               System.out.println("Corrupt lockfile...");
               return;
            }

            long created = 0;
            long expiresIn = 0;
            try
            {
               created = Long.parseLong(createdStr);
               expiresIn = Long.parseLong(expiresStr);
            }
            catch (NumberFormatException nfe)
            {
               nfe.printStackTrace();
               return;
            }

            // either:
            // the locker owns the lock or the lock expired
            // so remove the lock by deleting the file
            if (prevLocker.sameId(lockerId)
               || (created + expiresIn) <= System.currentTimeMillis() )
            {
               System.out.println("lock deleted");               
               f.delete();
            }
            else
            {
               // the file is locked by someone other than the entity who is trying
               // to remove the lock
               // TODO: log an error
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         finally
         {

         }
      }
   }

   /**
    * Private utility method used during construction to associate
    * lockable objects with their respective directories.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/7
    * 
    */
   private void initDirs()
   {
      // create one file generator map for each type of lock that can be held
      m_keyGens = new PSMapClassToObject[1];

      m_keyGens[0] = new PSMapClassToObject();
      initDir(m_keyGens[0], EXCLUSIVE_SUFFIX);

      for (int i = 0; i < m_keyGens.length; i++)
      {
         // now create all the necessary subdirectories for each kind of
         // lockable object in the list of file generators
         Enumeration dirs = m_keyGens[i].getMappedObjects();
         while (dirs.hasMoreElements())
         {
            FileGenerator gen = (FileGenerator)dirs.nextElement();
            gen.getDir().mkdirs();
         }
      } // end for
   }
   
   /**
    * Add a file generator to the map for each type of lockable object.
    * To support locking for a new object type, you would first create
    * a file generator-derived class that knows how to map the object
    * type to a unique filename (for example, the file generator class
    * for an application uses the app name as the file name). After
    * creating this class, add code to this method that adds an
    * instance of the class to the map, using a designated subdirectory
    * to store locks for that type (for example, locks for applications
    * are stored in the <CODE>applications</CODE> subdirectory.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   map The map to which the file generator will be added.
    * @param   suffix The file suffix for lockfiles.
    * 
    */
   protected void initDir(
      PSMapClassToObject map,
      String suffix)
   {
      // map string objects to files with that name
      Class c = "".getClass();

      FileGenerator stringGen
         = new FileGenerator(new File(m_lockDir, "locks"), suffix)
      {
         public File getLockFile(Object o)
         {
            String fileBase = o.toString().replace('/', '$').replace(':', '$');
            return new File(m_dir, fileBase + m_suf);
         }
      };

      map.addReplaceMapping(c, stringGen);
   }

   /**
    * Get the synchronization object for the given file. The
    * current implementation (as of 1.0) uses the same synchronization
    * object for all files. Future versions may improve concurrency
    * by using a different synchronization object for each directory.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   f The file
    * 
    * @return   Object An object that, when synchronized on, ensures
    * that all file operations with <CODE>f</CODE> are atomic
    * when inside the synch block.
    */
   private Object getSynch(File f)
   {
      return m_sync;
   }

   /** the base lock directory for the lock manager */
   protected File m_lockDir;

   /** An array of maps, one for each lock type. Each map associates
    *  each lockable object class with a FileGenerator object that
    *  knows how to generate unique filenames for objects of that
    *  class. */
   private PSMapClassToObject[] m_keyGens;

   /** The lock keys that we give out are all instances of this class.
    *  The class is final so that we can be sure that we are the
    *  only agent creating instances of this class
    */
   protected final class FileLockKey
   {
      public FileLockKey(File file)
      {
         m_file = file;
      }

      File getFile()
      {
         return m_file;
      }

      public String toString()
      {
         return m_file.toString();
      }

      private File m_file;
   }

   private abstract class FileGenerator
   {
      public FileGenerator(File dir, String suffix)
      {
         m_dir = dir;
         m_suf = suffix;
      }

      /**
       * Get the directory that will be the parent of any file
       * returned by getLockFile.
       *
       * @author   chad loder
       * 
       * @version 1.0 1999/7/8
       * 
       * @return   File
       */
      public File getDir()
      {
         return m_dir;
      }

      /**
       * Returns the unique lockfile for the given object, which must be
       * of a type that this generator understands. To be implemented
       * by subclasses. Implementors must make sure the returned file is
       * under the directory that was passed into the constructor.
       *
       * @author   chad loder
       * 
       * @version 1.0 1999/7/8
       * 
       * @param   o The object, which must be of a type that this generator
       * understands.
       * 
       * @return   File The unique lockfile.
       */
      public abstract File getLockFile(Object o);

      /** the directory where the lock file will live */
      protected File m_dir;

      /** the lock file */
      protected String m_suf;
   }

   /** the synchronization object for file atomicity */
   private Object m_sync = new Object();

   /** the suffix for lock files */
   private static final String EXCLUSIVE_SUFFIX = ".axl";
}
