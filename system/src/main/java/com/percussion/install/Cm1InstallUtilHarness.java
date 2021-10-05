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

package com.percussion.install;

public class Cm1InstallUtilHarness
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      String cm1Dir = args[0];  
      boolean isDerbyUp = InstallUtil.isDerbyRunning(cm1Dir);
      System.out.print("Derby Running: " + Boolean.toString(isDerbyUp)  + "\n");
      boolean isCM1Up = InstallUtil.isServerRunning(cm1Dir);
      System.out.print("CM1 Running: " + Boolean.toString(isCM1Up) + "\n");
      if (args[1] != null){
         String dtsDir = args[1];
         boolean isDtsUp = InstallUtil.checkTomcatServerRunning(dtsDir);
         System.out.print("DTS Running: " + Boolean.toString(isDtsUp));
      }
   }
}
