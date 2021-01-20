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

import com.percussion.util.IOTools;

import java.io.File;

/**
 * This class is launched at the end of the installation in a separate process
 * to complete the cleanup of temporary installation files. 
 * 
 * @author peterfrontiero
 */
public class PSCleanup
{
   /**
    * Deletes the directory specified by the passed in argument.  If the
    * directory is in use, an attempt will be made to delete it for up to
    * {@link #MAX_WAIT_TIME}.
    *
    * @param args The following argument is accepted:
    * 
    * directory - This is the path to the temporary directory used by the
    *             installer.
    */
   public static void main(String args[])
   {
      if (args.length == 0)
         return;
      
      String dirStr = args[0];
      File dirFile = new File(dirStr);
           
      long startTime = System.nanoTime();
      double elapsedTime = 0;
      while (dirFile.exists() && elapsedTime < (60000 * MAX_WAIT_TIME))
      {
         IOTools.deleteFile(dirFile);
         elapsedTime = (System.nanoTime() - startTime)/1E6;
      }
   }
   
   /**
    * The maximum time in minutes to continue trying to delete the directory.
    */
   private static final int MAX_WAIT_TIME = 3;  
}
