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

//java
import com.percussion.util.PSProperties;

import java.io.IOException;

import org.xml.sax.SAXException;


/**
   * RxApplicatonManager is a class that manages the installation and 
  * conversion of applications.
  */
public class RxApplicationManager
{
   /**
   * Constructs an RxApplicationManager
   */
   public RxApplicationManager()
  {
  }
  
  /**
  * Apply repository and port information to the application files.
  * 
  * @param rootDir - The absolute path name of the root directory.
  *      Must not be <CODE>null</CODE>
  *
  * @param strAppFileRoot - The location of the appFileName.
  *      Must not be <CODE>null</CODE>
  * 
  * @param appFileName - The name of the application file.
  *      Must not be <CODE>null</CODE>
  *
  * @param bUpdateNativeStatemnet - <code>true</code> if the native
  *       statement should be updated.
  *
   * @throws IOException - if file is invalid or inaccessible
   *
   * @throws SAXException - if the application file is not parseable XML
   * document
  *
   * @throws IllegalArgumentException if strRootDir or strAppName are <CODE>
  * null</CODE>.
  *
  * @return <CODE>true</CODE> for success, <CODE>false</CODE> for failure.
  */
  static public void applyLocalSettings(String strRootDir,
                                        String strAppFileRoot, 
                                        String strAppFileName,
                                        boolean bUpdateNativeStatement)
     throws IOException, SAXException
  {
      //validate the parameters
      if (strRootDir == null || strAppFileName == null || strAppFileRoot == null)
         throw new IllegalArgumentException();

      //convert file name to appname
      int dotIndex = strAppFileName.indexOf(".");
      String strAppName = strAppFileName.substring(0, dotIndex);
      
      RxFileManager fileManager = new RxFileManager(strRootDir);
      
      //server property file 
      PSProperties serverProps = new PSProperties(fileManager.getServerPropertiesFile());
      //port number
      String strPort = (String)serverProps.get("bindPort");
      
      //repository property file
      PSProperties repositoryProps = new PSProperties(fileManager.getRepositoryFile());
      
      //run utility conversion
      RxAppConverter.updateRxApp(   repositoryProps, 
                           strAppFileRoot, 
                                  strAppName,
                              true, //modify credential
                                  strPort,
                                  false, //enable
                                  bUpdateNativeStatement);
  }
  
}
