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








