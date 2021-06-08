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
import com.percussion.util.PSProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RxDesignerOnlyPostInstaller
{

   private static final Logger log = LogManager.getLogger(RxDesignerOnlyPostInstaller.class);

   public RxDesignerOnlyPostInstaller() 
   {
      super();
   }

   public static void main(String[] args) 
   {
      String rootDirectory = new String(".");
      //current directory
      if (args.length == 1) {
         rootDirectory = args[0];
      }

      try
      {
         String strFileName = rootDirectory + File.separator + "bin" + File.separator + m_designerPropName;
         File file = new File(strFileName);
         if(!file.exists())
            file.createNewFile();

         PSProperties props =  new PSProperties(strFileName);
         props.setProperty("installRoot", rootDirectory);
         props.store(new FileOutputStream(strFileName), null);
      }
      catch(IOException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
   
   private static String m_designerPropName = "designer.properties";
}
