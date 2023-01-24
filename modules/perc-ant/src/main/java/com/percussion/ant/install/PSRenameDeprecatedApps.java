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

import com.percussion.error.PSExceptionUtils;
import com.percussion.install.PSLogger;
import com.percussion.install.PSPreUpgradePluginDeprecatedSysApps;
import com.percussion.util.PSProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

/**
 * Renames system application files which have been deprecated in the current
 * version.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="renameDeprecatedApps"
 *              class="com.percussion.ant.install.PSRenameDeprecatedApps"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to rename the applications.
 *
 *  <code>
 *  &lt;renameDeprecatedApps/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSRenameDeprecatedApps extends PSAction
{
   private static final Logger log = LogManager.getLogger(PSRenameDeprecatedApps.class);
   // see base class
   @Override
   public void execute()
   {
      try
      {
         PSLogger.logInfo("Renaming deprecated applications");

         int renamedApps = renameApplications();
         PSLogger.logInfo("Renamed " + renamedApps + " applications");
      }
      catch(Exception e)
      {
         PSLogger.logError(e.getMessage());
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

   /**
    * Renames all deprecated system applications specified by
    * {@link PSPreUpgradePluginDeprecatedSysApps#getDeprecatedSysApps()} to
    * "[original app name].bak".  This signifies that the applications are no
    * longer used by the system.
    *
    * @return the number of applications which have been successfully
    * renamed.
    * @throws IOException if an error occurs loading properties
    * @throws FileNotFoundException if properties files can not be found
    */
   @SuppressWarnings("unchecked")
   private int renameApplications() throws FileNotFoundException, IOException
   {
      //Get the server properties
      PSProperties serverProps = new PSProperties(m_strServerPropsFile);

      //Get the objectstore properties
      PSProperties objProps = new PSProperties(
            m_strRxRoot + File.separator +
            serverProps.getProperty(PROPS_OBJECT_STORE_VAR,
                  PROPS_OBJECT_STORE));

      //ObjectStore directory
      File objDir = new File(
            m_strRxRoot + File.separator +
            objProps.getProperty(PROPS_OBJECT_STORE_DIR));
      
      String appName = null;
      File appFile = null;
      String[] apps = objDir.list();
      HashSet depApps =
         (HashSet) PSPreUpgradePluginDeprecatedSysApps.getDeprecatedSysApps();
      int renamedApps = 0;
      
      for (int i = 0; i < apps.length; i++)
      {
         appName = apps[i];
         appFile = new File(objDir, appName);

         if (appFile.isDirectory() || !appName.endsWith(".xml"))
            continue;
         else if (appName.startsWith("sys_"))
         {
            if (depApps.contains(appName))
            {
               String newAppName = appName + ".bak";
               File newAppFile = new File(objDir, newAppName);

               if (newAppFile.exists())
                  newAppFile.delete();

               appFile.renameTo(newAppFile);
               renamedApps++;
            }
         }
      }
      
      return renamedApps;
   }
   
   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The rhythmyx root directory
    */
   private String m_strRxRoot = getRootDir();

   /**
    * Location of server.properties
    */
   private String m_strServerPropsFile = m_strRxRoot + File.separator +
      "rxconfig/Server/server.properties";

   /**
    * The objectstore property name in server.properties
    */
   private final String PROPS_OBJECT_STORE_VAR = "objectStoreProperties";

   /**
    * The default objectstore properties file
    */
   private final String PROPS_OBJECT_STORE =
      "rxconfig/Server/objectstore.properties";

   /**
    * The objectstore directory property name in objectstore.properties
    */
   private final String PROPS_OBJECT_STORE_DIR = "objectDirectory";

}

