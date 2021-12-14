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
package com.percussion.util;

/**
 *   Tools for asking for OS information.
 */
public class PSOsTool
{
   /**
    * Determine whether the platform is Windows related by examing the system's
    * file or path separator.
    *
    * @return  <code>true</code> if this is Windows platform;
    *          <code>false</code> if this is not Windows platform
    */
   public static boolean isWindowsPlatform()
   {
      return ((getOsType() & OS_DOSWIN) == OS_DOSWIN);
   }

   /**
    * Determine whether the platform is Unix by examing the system's
    * file or path separator. This includes Solaris and Linux OS.
    *
    * @return  <code>true</code> if this is a Unix platform;
    *          <code>false</code> if this is not a Unix platform
    */
   public static boolean isUnixPlatform()
   {
      return ((getOsType() & OS_UNIX) == OS_UNIX);
   }

   /**
    * Determine whether the platform is Solaris by examing the system's
    * file or path separator.
    *
    * @return  <code>true</code> if this is Solaris platform;
    *          <code>false</code> if this is not Solaris platform
    */
   public static boolean isSolarisPlatform()
   {
      return ((getOsType() & OS_SOLARIS) == OS_SOLARIS);
   }

   /**
    * Determine whether the platform is Linux by examing the system's
    * file or path separator.
    *
    * @return  <code>true</code> if this is Linux platform;
    *          <code>false</code> if this is not Linux platform
    */
   public static boolean isLinuxPlatform()
   {
      return ((getOsType() & OS_LINUX) == OS_LINUX);
   }

   /**
    * Get the OS type. This sets the approriate bits in the returned value.
    * For example, if the OS is windows, it will set the first bit, if the
    * OS is unix it will set the second bit. If unix, then it will try to
    * determine if it is linux or Solaris and if it succeeds, it will set the
    * corresponding bits. Use bitwise OR to check for a specific OS.
    *
    * @return a mask with appropriate bits set corresponding to the OS type.
    */
   public static int getOsType()
   {
      int osType = OS_UNKNOWN;

      String fileSep = System.getProperty(ENV_FILE_SEP_STRING);

      if (fileSep.equals(OS_UNIX_FILESEP_STRING))
      {
         osType = osType | OS_UNIX;
      }
      else if (fileSep.equals(OS_DOSWIN_FILESEP_STRING))
      {
         osType = osType | OS_DOSWIN;
      }
      else
      {
         /* Try something else */
         String pathSep = System.getProperty(ENV_PATH_SEP_STRING);

         if (pathSep.equals(OS_UNIX_PATHSEP_STRING))
         {
            osType = osType | OS_UNIX;
         }
         else if (pathSep.equals(OS_DOSWIN_PATHSEP_STRING))
         {
            osType = osType | OS_DOSWIN;
         }
      }

      if ((osType & OS_UNIX) == OS_UNIX)
      {
         // check if it Solaris or Linux
         String osName = System.getProperty("os.name", "").toLowerCase();
         if (osName.indexOf("sunos") != -1)
         {
            // On Solaris, OS name contains String : "SunOS"
            osType = osType | OS_SOLARIS;
         }
         else if (osName.indexOf("linux") != -1)
         {
            // On Linux, OS name contains String : "Linux"
            osType = osType | OS_LINUX;
         }
      }
      return osType;
   }

   /** Specifier for an unknown operating system */
   public static final int OS_UNKNOWN = 0;

   /** Specifier for a Windows operating system */
   public static final int OS_DOSWIN = 1;

   /** Specifier for a Unix operating system
    *  This includes Solaris and Linux OS.
    */
   public static final int OS_UNIX = 2;

   /** Specifier for a Solaris operating system */
   public static final int OS_SOLARIS = 4;

   /** Specifier for a Linux operating system */
   public static final int OS_LINUX = 8;

   private static final String OS_DOSWIN_FILESEP_STRING = "\\";
   private static final String OS_UNIX_FILESEP_STRING = "/";

   private static final String OS_DOSWIN_PATHSEP_STRING = ";";
   private static final String OS_UNIX_PATHSEP_STRING = ":";

   private static final String ENV_FILE_SEP_STRING="file.separator";
   private static final String ENV_PATH_SEP_STRING="path.separator";
}

