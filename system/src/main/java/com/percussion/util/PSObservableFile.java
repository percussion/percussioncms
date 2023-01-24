/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.util;

import java.io.File;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class encapsulates a file and informs the registered file observers
 * whenever the file is modified. Aimed at any situation when an action needs to
 * be taken whenever a file is modified. Polling is done at a specified time
 * interval (the default is one second) to see if the file is modified. An
 * observer of the file must implement the interface {@link Observer} and
 * register with this object using
 * {@link Observable#addObserver(java.util.Observer)} method of the base class.
 * Makes use of {@link Timer} object to poll to track if the file is modified at
 * specified regular interval of time.
 */
public class PSObservableFile extends Observable
{
   /**
    * Convenience method that calls
    * {@link #PSObservable(String,int) this(fileName, 1000)}.
    */
   public PSObservableFile(String s)
   {
      //check every second by default
      this(s, 1000);
   }

   /**
    * Convenience method that calls
    * {@link #PSObservable(File,int) this(file, 1000)}.
    */
   public PSObservableFile(File file)
   {
      //check every second by default
      this(file, 1000);
   }

   /**
    * Convenience method that calls
    * {@link #PSObservable(File,int) this(file, observeInterval)}.
    */
   public PSObservableFile(String fileName, int observeInterval)
   {
      this(new File(fileName), observeInterval);
   }

   /**
    * Ctor taking the file to observed as Java File object and observe interval 
    * in milliseconds.
    * 
    * @param file file object to be observed, must not be <code>null</code> or 
    * empty.
    * @param observeInterval observe interval in milliseconds, must be a 
    * positive value.
    */
   public PSObservableFile(File file, int observeInterval)
   {
      if (file == null)
         throw new IllegalArgumentException("file must not be null");
      if (observeInterval < 1)
         throw new IllegalArgumentException(
               "observeInterval must be greater than 0 milli seconds");
      m_file = file;
      m_lastModified = m_file.lastModified();
      
      //Create a timer, timer task and schedule with initial 
      //delay = period = observeInterval
      new Timer().schedule(new TimerTask()
      {
         public void run()
         {
            long actualLastModified = m_file.lastModified();
            if (m_lastModified != actualLastModified)
            {
               // the file has changed
               m_lastModified = actualLastModified;
               setChanged();
               notifyObservers(m_file);
            }
         }
      }, observeInterval, observeInterval);
   }

   /**
    * Access method for the file to be observed.
    * 
    * @return file to be observed, never <code>null</code>
    */
   public File getFile()
   {
      return m_file;
   }
   
   /**
    * Record the last modifed date to compare with actual modified date of the
    * file to be observed during polling.
    */
   private long m_lastModified;

   /**
    * The file to be observed. Initialized in the ctor
    * {@link #PSObservableFile(String)}and never <code>null</code> after
    * that.
    */
   private File m_file = null;
}
