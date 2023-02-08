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
