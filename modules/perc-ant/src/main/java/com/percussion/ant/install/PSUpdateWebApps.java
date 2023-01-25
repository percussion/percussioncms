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
import com.percussion.util.IOTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * PSUpdateWebApps deploys existing non-system webapps into the appropriate
 * location during upgrade.  Classloader scoping is also added.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="updateWebApps"
 *              class="com.percussion.ant.install.PSUpdateWebApps"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to update the webapps.
 *
 *  <code>
 *  &lt;updateWebApps sysWebapps="webapp1, webapp2"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpdateWebApps extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      try
      {
         PSLogger.logInfo("Updating webapps");

         File fWeb = new File(m_webappsDir);
         if (!fWeb.exists())
         {
            PSLogger.logInfo("Webapps directory does not exist : " + m_webappsDir);
            return;
         }

         File fDep = new File(m_deployDir);
         if (!fDep.exists())
         {
            PSLogger.logInfo("Deploy directory does not exist : " + m_deployDir);
            return;
         }

         //Deploy webapps
         deployWebapps(fWeb, fDep);
      }
      catch (Exception e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
   }

   /***************************************************************************
    * private functions
    ***************************************************************************/

   /**
    * Deploys all non-system webapps into the new deploy directory.  Adds
    * appropriate classloader scoping to each webapp.
    *
    * @param webappsDir the old webapps directory
    * @param deployDir the new deploy directory
    * @throws ServiceException if error occurs during deployment
    */
   private void deployWebapps(File webappsDir, File deployDir)
   {
      File[] webapps = webappsDir.listFiles();
      String deployPath = deployDir.getAbsolutePath();

      int i;

      for (i=0; i < webapps.length; i++)
      {
         File webapp = webapps[i];

         //Webapps are directories
         if (!webapp.isDirectory())
            continue;

         String webappName = webapp.getName();

         if (!isSysWebapp(webappName))
         {
            //Found a non-system webapp, so deploy it
            PSLogger.logInfo("Deploying webapp: " + webappName);

            String deployAbsPath = deployPath + File.separator +
            webappName + ".war";
            File newWebapp = new File(deployAbsPath);
            if (!newWebapp.exists())
               newWebapp.mkdir();

            File[] webappFiles = webapp.listFiles();
            for (int j=0; j < webappFiles.length; j++)
            {
               try
               {
                  IOTools.copyToDir(webappFiles[j], newWebapp);
               }
               catch (IOException e)
               {
                  PSLogger.logError("Error deploying webapp: " + webappName);
                  PSLogger.logError("Exception: " + e.getMessage());
               }
            }

            //Add classloader scoping
            try
            {
               PSLogger.logInfo("Adding classloader scoping to webapp: " +
                     newWebapp.getName());
               addClassloaderScoping(newWebapp);
            }
            catch (IOException e)
            {
               PSLogger.logError("Classloader scoping failed");
               PSLogger.logError("Exception: " + e.getMessage());
            }
         }
      }
   }

   /**
    * Determines if a webapp is a system webapp.
    *
    * @param webapp the name of the webapp
    * @return <code>true</code> if the webapp is a system webapp, <code>false</code>
    * otherwise
    */
   private boolean isSysWebapp(String webapp)
   {
      boolean sysWebapp = false;
      int i;

      for (i=0; i < sysWebapps.length; i++)
      {
         if (webapp.equalsIgnoreCase(sysWebapps[i]))
         {
            sysWebapp = true;
            break;
         }
      }
      return sysWebapp;
   }

   /**
    * Adds appropriate classloader scoping to a webapp by adding a jboss-web.xml
    * file to the WEB-INF directory of the webapp.
    *
    * @param webapp file representing the webapp
    * @throws IOException if error occurs during scoping
    */
   private void addClassloaderScoping(File webapp)
   throws IOException
   {
      String scopingFilePath = webapp.getAbsolutePath() + File.separator +
      "WEB-INF" + File.separator + scopingFile;
      FileWriter fwriter = new FileWriter(scopingFilePath);

      String contents = "<jboss-web>\n" +
      "   <class-loading>\n" +
      "    <loader-repository>percussion.com:loader=" +
      webapp.getName() + "</loader-repository>\n" +
      "   </class-loading>\n" +
      "</jboss-web>";

      fwriter.write(contents);
      fwriter.close();
   }

   /***************************************************************************
    * Bean properties
    ***************************************************************************/

   /**
    * Returns the names of the pre-6.0 system webapps.
    * @return the names of the system webapps in an array
    */
   public String[] getSysWebapps()
   {
      return sysWebapps;
   }

   /**
    * Sets the names of the pre-6.0 system webapps.
    * @param sysWebapps the array containing the names of the system webapps
    */
   public void setSysWebapps(String sysWebapps)
   {
      this.sysWebapps = convertToArray(sysWebapps);
   }

   /**************************************************************************
    * properties
    **************************************************************************/

   /**
    * Path of the pre-6.0 webapps directory
    */
   private String m_webappsDir = getRootDir() + File.separator
   + "AppServer.bak/webapps";

   /**
    * Path of the 6.0 deploy directory
    */
   private String m_deployDir = getRootDir() + File.separator
   + "AppServer/server/rx/deploy";

   /**
    * Names of the pre-6.0 system webapps
    */
   private String[] sysWebapps = new String[0];

   /**
    * Name of the classloader scoping file
    */
   private String scopingFile = "jboss-web.xml";
}








