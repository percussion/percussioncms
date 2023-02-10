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

//java

import com.percussion.util.PSProperties;
import org.xml.sax.SAXException;

import java.io.IOException;


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
