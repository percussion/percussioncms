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
package com.percussion.install;

/**
 * The PSSystem class finds the number of processors of a local personal computer
 * with Windows (NT) operating system and determines whether the computer has
 * been rebooted successfully or not.
 * <p>
 * This class can also be used for Unix systems. The only differences are
 * the reboot issue, and the way of loading the shared library. Note that
 * Unix systems seldom reboot. The shared library file has extensions such as
 * <code>so.1</code> for Solaris.
 *
 * Now that we use InstallShield, a wizard panel will be doing the loading.
 * 
 */
public class PSSystem
{
   /**
    * Get the number of a local machine's processors.
    * @return  the number of processors
    */
   public static int getNumberOfProcessors()
   {
      return getProcessors();
   }

   /**
    * Determine whether a local computer rebooted successfully or not.
    * @return  <code>true</code> if rebooted, or <code>false</code> if not
    */
   public static boolean rebootMachine()
   {
      return reboot();
   }
   
   /**
    * Return the short path for Windows
    * @param inPath long path name with spaces
    * @return ShortPath
    */
   public static String jniGetShortPathName(String inPath)
   {
      return getShortPathName(inPath);
   }

/**
    * Set console handler
    * @return void
    */
   public static void  jniSetConsoleCtrlHandler()
   {
      setConsoleCtrlHandler();
   }

   /******************For unix, comment out the following*****************/

   /**
    * Get the number of a local machine's processors.
    * @return  the number of processors
    */
   private static native int getProcessors();

   /**
    * Determine whether a local computer is in reboot status.
    * @return  <code>true</code> if in reboot status, or <code>false</code> if not
    */
   private static native boolean reboot();

   /**
    * Return the short path for Windows
    * @param inPath long path name with spaces
    * @return ShortPath
    */
   private static native String getShortPathName(String inPath);
   
   /**
    * Console handler for the java process so that it masks get logoff events
    * @return void
    */
   private static native void setConsoleCtrlHandler();

}   


