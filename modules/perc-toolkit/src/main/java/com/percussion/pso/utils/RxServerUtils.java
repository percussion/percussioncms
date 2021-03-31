/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * com.percussion.pso.utils RxServerUtils.java
 *
 */
package com.percussion.pso.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static Log log = LogFactory.getLog(RxServerUtils.class);
   
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
