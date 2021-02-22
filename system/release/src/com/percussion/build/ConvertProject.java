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

package com.percussion.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The ConvertProject class contains some utility methods for the installer.
 */
public class ConvertProject
{
   /**
    * Find and replace text in the tutorial. If either <code>strFind</code> or
    * <code>strReplace</code> is null, or if <code>strFind</code> is an empty string,
    * then nothing will happen.
    *
    * @param   strFind     the text to be found
    * @param   strReplace  the text to replace <code>strFind</code>
    * @param   strFile     the path name of a file containing the text to be found
    *                      and replaced with (<code>null</code> path name is not allowed)
    */
   public static void findReplace(String strFind, String strReplace, String strFile)
   {
      if ((strFind == null) || (strReplace == null) || (strFind.length() == 0))
         return;

      try
      {
         if(strFind.equals(strReplace))
            return;
            
         System.out.println("find: + " + strFind  + " replace: " + strReplace);
         File file = new File(strFile);
          String strReadData;
          String strWriteData;

         try(FileInputStream in = new FileInputStream(file)) {


             int iAvail = in.available();
             byte[] bData = new byte[iAvail];
             in.read(bData, 0, iAvail);
             strReadData = new String(bData);

             StringBuilder buffer = new StringBuilder(strReadData);
             int replace = buffer.toString().indexOf(strFind);
             while (replace != -1) {
                 buffer = buffer.replace(replace, replace + strFind.length(), strReplace);
                 replace = buffer.toString().indexOf(strFind);
             }

             strWriteData = buffer.toString();
         }
         try(FileWriter writer = new FileWriter(strFile)) {
             writer.write(strWriteData, 0, strWriteData.length());
         }

      } catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Convert a <code>project</code> file. Same as calling
    * <code>convertProject(project, null, null)</code>.
    *
    * @param   project  the path name of a file (not <code>null</code>)
    */
   public static void convertProject(String project)
      throws java.io.IOException
   {
      convertProject(project, null);   
   }

   /**
    * Convert a <code>project</code> file. In the file, the possible installer drivers
    * will first be found and replaced with the driver given by <code>strCurDir</code>.
    * Then the E2 directory will be replaced with the directory of <code>strCurDir</code>.
    * Finally, if <code>strDocDir</code> is not <code>null</code>, the Docs\E2\Help\V2.0\
    * directory will be replaced with the directory of <code>strDocDir</code>. Otherwise,
    * this final step will be ignored. The result is stored in <code>project</code>.
    *
    * @param   project     the path name of a file (not <code>null</code>)
    * @param   strCurDir   the current directory, if <code>null</code>, then the current
    *                      user directory is assumed
    */
   public static void convertProject(String project, String strCurDir)
      throws java.io.IOException
   {
      if(strCurDir == null)
      {
         File curDir = new File(System.getProperty("user.dir"));
         strCurDir = curDir.getCanonicalPath();
      }

      //replace \\e2
      String[] dirs = getPossibleDirectories();
       for (String strFind : dirs) {
           findReplace(strFind, strCurDir.substring(1) + "\\", project);
       }
  
      //when we add files to the installer, we could have been mapped to a different
      //drive, so switch to the current drive.
      String[] drives = getPossibleDrives();
      String strReplace = strCurDir.charAt(0) + ":\\";
       for (String drive : drives) {
           String strFind = drive + ":\\";
           findReplace(strFind, strReplace, project);
       }
   }

   /**
    * Get the possible drives, such as "M", "N", "S", etc.
    *
    * @return  a string array contains the posible drives
    */
   public static String[] getPossibleDrives()
   {
      String[] drives = new String[8];
      drives[0] = "M";
      drives[1] = "m";
      drives[2] = "Q";
      drives[3] = "q";
      drives[4] = "C";
      drives[5] = "c";
      drives[6] = "N";
      drives[7] = "n";
      return(drives);
   }
  
   /**
    * Get the possible e2 directories.
    *
    * @return  a string array contains the 
    */
  public static String[] getPossibleDirectories()
  {
      String[] dirs = new String[22];
      dirs[0] = ":\\e2.35gold\\";
      dirs[1] = ":\\e2.35eval\\";
      dirs[2] = ":\\e2\\";
      dirs[3] = ":\\E2\\";
      dirs[4] = ":\\e2.40\\";
      dirs[5] = ":\\E2.40\\";
      dirs[6] = ":\\e2.40ug\\";
      dirs[7] = ":\\E2.40UG\\";
      dirs[8] = ":\\e2.40db2\\";
      dirs[9] = ":\\E2.40DB2\\";
      dirs[10] = ":\\e2.40ce\\";
      dirs[11] = ":\\E2.40CE\\";
      dirs[12] = ":\\e2.40cuce\\";
      dirs[13] = ":\\E2.40CUCE\\";
      dirs[14] = ":\\e2.40ui\\";
      dirs[15] = ":\\E2.40UI\\";
      dirs[16] = ":\\redsox\\";
      dirs[17] = ":\\REDSOX\\";
      dirs[18] = ":\\bruins\\";
      dirs[19] = ":\\BRUINS\\";
      dirs[20] = ":\\e2.celtics\\";
      dirs[21] = ":\\E2.CELTICS\\";
      return(dirs);
  }
  
  /**
    * This main method will be called as an entry point.
    */
   public static void main(String[] args)
   {
      //give usage
      if(args.length < 3)
      {
          System.out.println("Usage: java com.percussion.build.ConvertProject n:\\e2\\project.xml n: e2");
          return;
      }
      
      try
      {
          convertProject(args[0], args[1] + File.separator + args[2]);
      }
      catch(IOException e)
      {
          e.printStackTrace();
      }
  }
}
