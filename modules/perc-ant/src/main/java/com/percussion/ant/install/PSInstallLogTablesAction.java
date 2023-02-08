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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.install.RxFileManager;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PSInstallLogTablesAction will install the log tables
 *
 * This class will be used temporarily until the table installation
 * structure is in place.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="installLogTablesAction"
 *              class="com.percussion.ant.install.PSInstallLogTablesAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to set the properties.
 *
 *  <code>
 *  &lt;installLogTablesAction/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSInstallLogTablesAction extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      //load the properties from the file
      //get the root dir
      String strRootDir = getRootDir();

      if(strRootDir != null)
      {
         RxFileManager fileManager = new RxFileManager(strRootDir);
         File serverPropFile = new File(fileManager.getServerPropertiesFile());

         //if the server properties file does not exist log error message
         if(!serverPropFile.exists())
         {
            PSLogger.logInfo("ERROR : Properties file " +
                  serverPropFile.getPath() +
            " does not exist.");
            return;
         }

         File repPropFile = new File(fileManager.getRepositoryFile());
         //if the repository properties file does not exist log error message
         if(!repPropFile.exists())
         {
            PSLogger.logInfo("ERROR : Properties file " +
                  repPropFile.getPath() +
            " does not exist.");
            return;
         }

         try
         {

            PSProperties repProp =
               new PSProperties(repPropFile.getPath());

            String strDriver = (String)repProp.getProperty(REP_DRIVER);
            String strClass = (String)repProp.getProperty(REP_CLASS);
            String strServer = (String)repProp.getProperty(REP_SERVER);
            String strId = (String)repProp.getProperty(REP_ID);
            String strPw = (String)repProp.getProperty(REP_PW);
            String strDb = (String)repProp.getProperty(REP_DATABASE);
            String strSchema = (String)repProp.getProperty(REP_SCHEMA);

            PSProperties serverProp =
               new PSProperties(serverPropFile.getAbsolutePath());

            //copy it back to server properties
            serverProp.setProperty(DRIVER, strDriver);
            serverProp.setProperty(CLASS, strClass);
            serverProp.setProperty(SERVER, strServer);
            serverProp.setProperty(ID, strId);
            serverProp.setProperty(PW, strPw);
            serverProp.setProperty(DATABASE, strDb);
            serverProp.setProperty(SCHEMA, strSchema);

            FileOutputStream out = new FileOutputStream(
                  serverPropFile.getAbsolutePath());
            serverProp.store(out, null);
            out.close();
         }
         catch(IOException ioExc)
         {
            PSLogger.logInfo("ERROR : " + ioExc.getMessage());
            PSLogger.logInfo(ioExc);
         }
      }
   }

   /**************************************************************************
    * Static Strings
    *************************************************************************/
   public static String DRIVER = "driverType";
   public static String CLASS = "loggerClassname";
   public static String SERVER = "serverName";
   public static String ID = "loginId";
   public static String PW = "loginPw";
   public static String DATABASE = "databaseName";
   public static String SCHEMA = "schemaName";

   public static String REP_DRIVER = "DB_DRIVER_NAME";
   public static String REP_CLASS = "DB_DRIVER_CLASS_NAME";
   public static String REP_SERVER = "DB_SERVER";
   public static String REP_ID = "UID";
   public static String REP_PW = "PWD";
   public static String REP_DATABASE = "DB_NAME";
   public static String REP_SCHEMA = "DB_SCHEMA";
}
