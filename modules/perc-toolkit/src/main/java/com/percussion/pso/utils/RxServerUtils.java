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

package com.percussion.pso.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

/**
 * A collection of generally useful routines for Percussion CMS Server interactions.
 * 
 *
 * @author DavidBenua
 *
 */
public class RxServerUtils
{
   private static final Logger log = LogManager.getLogger(RxServerUtils.class);
   
   /**
    * Static methods only, never constructed. 
    */
   private RxServerUtils()
   {
      
   }
   
   /**
    * Wait for the server to be ready. 
    * When initializing or performing actions from a service on startup,
    * the Percussion CMS server may not be ready.  This routine polls for the
    * server run lock file on 10 second intervals. 
    * <p>
    * This routine should be called from a new thread only, it should
    * not block the server initialization thread. 
    * <p>
    * <em>WARNING:</em> This method works only on Windows, and should not be 
    * relied upon in cross platform code. 
    * 
    * @throws InterruptedException
    */
   public static void waitForServerReady() throws InterruptedException
   {
      FileLock lock = null; 
      boolean ready  = false; 
      while(!ready) 
      {
         File f = new File(LOCK_FILE_NAME);
         try
         {
         if(f.exists())
         {
            RandomAccessFile lockF = new RandomAccessFile(f, "rw");
            FileChannel channel = lockF.getChannel();       
            lock = channel.tryLock(0, 1, false);
            if(lock == null)
            {
               ready = true;
            }
            else
            {
               lock.release();
            }
         }
         else
         {
            log.debug("lock file does not exist"); 
         }
         }
         catch(Exception e)
         {
            log.debug("error reading lock file"); 
         }
         if(!ready)
         {
            log.debug("sleeping for lock file"); 
            Thread.sleep(LOCK_FILE_INTERVAL);
         }
      }
      log.debug("server is ready now"); 
   }
   
   private static final String LOCK_FILE_NAME = "server_run_lock";
   private static final long LOCK_FILE_INTERVAL = 10000L; 
   private static final String PERCUSSION_HOME="PERCUSSION_HOME";
   
   /***
    * Get the value of the PERCUSSION_HOME environment variable or null if it is not set.
    * 
    * @author natechadwick
    * @return 
    */
   public static String getRhythmyxHome(){
   
	   Map<String, String> env = System.getenv();

	   if(env.containsKey(PERCUSSION_HOME))
		   return env.get(PERCUSSION_HOME);
	   else
		   return null;
   }
   
}
