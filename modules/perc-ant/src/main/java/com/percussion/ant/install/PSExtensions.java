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

import com.percussion.error.PSNonUniqueException;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.PSExtensionException;
import com.percussion.install.PSLogger;
import com.percussion.install.RxInstallerProperties;
import com.percussion.util.PSExtensionInstallTool;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * PSExtensions will be used to install extensions.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="extensions"
 *              class="com.percussion.ant.install.PSExtensions"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to install extensions.
 *
 *  <code>
 *  &lt;extensions installLocation="C:/Rhythmyx"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSExtensions extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      /* now look for the extensions.xml files so we can configure the
       *   extensions.
       */

      m_strRootDir = getRootDir();

      if(m_strRootDir == null)
      {
         PSLogger.logInfo("ERROR : " +
               RxInstallerProperties.getResources().getString("rooterr"));
         return;
      }

      try
      {
         m_tool = new PSExtensionInstallTool(new File(m_strRootDir));
      }
      catch (IOException | PSExtensionException io)
      {
         PSLogger.logInfo("ERROR : " + PSExceptionUtils.getMessageForLog(io));
         throw new BuildException(io);
      }

      String strInstallLocation = m_strRootDir;
      String strMyLocation = getInstallLocation();
      if(strMyLocation != null && strMyLocation.length() > 0)
      {
         strInstallLocation += File.separator + strMyLocation;
      }

      File destDir = new File(strInstallLocation);
      if(destDir.isDirectory())
      {
         findAndConfigureExits(destDir);
      }

      // WE MUST close the extension manager explicitly!!!
      m_tool.close();
   }

   /**
    * Function to get the files in a directory and find the Extension.xml.
    * If on is found, then the extensions will be configured.
    *
    * @param dir - Directory to look for Extensions.xml.  Must not be
    *   <CODE>null</CODE> and a directory.
    *
    * @throws IllegalArgumentException if dir is <CODE>null</CODE>.
    */
   private void findAndConfigureExits(File dir)
   {
      //check the directory
      if(dir == null)
         throw new IllegalArgumentException("dir must not be null");

      if(!dir.isDirectory())
         throw new IllegalArgumentException("dir must be a directory");

      //does this directory have an extensions.xml?
      File extensions = new File(dir.getPath() + File.separator
            + IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
      if(extensions.exists())
      {
         FileInputStream fIn = null;
         Document doc;
         try
         {
            fIn = new FileInputStream(extensions);
            doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);

            PSLogger.logInfo(
                  RxInstallerProperties.getResources().getString("installext")
                  + " " + extensions.getPath() + " in " + dir.getPath());

            m_tool.installExtensions(doc, dir);
         }
         catch(PSNotFoundException | PSNonUniqueException|org.xml.sax.SAXException|PSExtensionException|IOException e)
         {
            PSLogger.logError("ERROR : " + PSExceptionUtils.getMessageForLog(e));
            throw new BuildException(e);
         }
         finally
         {
            try
            {
               if (fIn != null)
                  fIn.close();
            }
            catch (IOException e)
            {
               // ignore this
               PSLogger.logInfo(e);
            }
         }
      }

      //look in subdirectories
      String[] children = dir.list();
      if(children != null)
      {
         for (String child : children) {
            String strChild = child;
            if (strChild != null) {
               strChild = dir.getAbsolutePath() + File.separator + strChild;
               File childFile = new File(strChild);
               if (childFile.isDirectory())
                  findAndConfigureExits(childFile);
            }
         }
      }
   }

   /***********************************************************************
    * Property accessors and mutators
    ***********************************************************************/

   public String getInstallLocation()
   {
      return m_strInstallLoc;
   }

   public void setInstallLocation(String strInstallLoc)
   {
      m_strInstallLoc = strInstallLoc;
   }

   /*
    * Variables
    */

   /**
    * The root installation directory.
    */
   private String m_strRootDir = null;

   /**
    * Extension tool.
    */
   private PSExtensionInstallTool m_tool = null;

   /**
    * The install location relative to the root.
    */
   private String m_strInstallLoc = null;
}
