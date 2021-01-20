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


