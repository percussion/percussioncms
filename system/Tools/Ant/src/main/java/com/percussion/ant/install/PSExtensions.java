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

package com.percussion.ant.install;

import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionHandler;
import com.percussion.extension.PSExtensionException;
import com.percussion.install.PSLogger;
import com.percussion.install.RxInstallerProperties;
import com.percussion.util.PSExtensionInstallTool;
import com.percussion.xml.PSXmlDocumentBuilder;
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
      /** now look for the extensions.xml files so we can configure the
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
      catch (IOException io)
      {
         PSLogger.logInfo("ERROR : " + io.toString());
         PSLogger.logInfo(io);
      }
      catch(PSExtensionException pse)
      {
         PSLogger.logInfo("ERROR : " + pse.toString());
         PSLogger.logInfo(pse);
      }

      if(m_tool != null)
      {
         String strInstallLocation = m_strRootDir;
         String strMyLocation = getInstallLocation();
         if(strMyLocation != null && strMyLocation.length() > 0)
         {
            strInstallLocation += File.separator + strMyLocation;
         }

         File destDir = new File(strInstallLocation);
         if(destDir != null && destDir.isDirectory())
         {
            findAndConfigureExits(destDir);
         }

         // WE MUST close the extension manager explicitly!!!
         m_tool.close();
      }
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
      if(extensions != null && extensions.exists())
      {
         FileInputStream fIn = null;
         Document doc = null;
         try
         {
            fIn = new FileInputStream(extensions);
            doc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);

            PSLogger.logInfo(
                  RxInstallerProperties.getResources().getString("installext")
                  + " " + extensions.getPath() + " in " + dir.getPath());

            m_tool.installExtensions(doc, dir);
         }
         catch(IOException e)
         {
            PSLogger.logInfo("ERROR : " + e.toString());
            PSLogger.logInfo(e);
         }
         catch(PSExtensionException pse)
         {
            PSLogger.logInfo("ERROR : " +pse.toString());
            PSLogger.logInfo(pse);
         }
         catch(org.xml.sax.SAXException sax)
         {
            PSLogger.logInfo("ERROR : " + sax.toString());
                    ;
            PSLogger.logInfo(sax);
         }
         catch(PSNonUniqueException psnonu)
         {
            PSLogger.logInfo("ERROR : " + psnonu.toString());
            PSLogger.logInfo(psnonu);
         }
         catch(PSNotFoundException psnotf)
         {
            PSLogger.logInfo("ERROR : " + psnotf.toString());
            PSLogger.logInfo(psnotf);
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
         for(int iChild = 0; iChild < children.length; ++iChild)
         {
            String strChild = children[iChild];
            if(strChild != null)
            {
               strChild = dir.getAbsolutePath() + File.separator + strChild;
               File childFile = new File(strChild);
               if(childFile != null && childFile.isDirectory())
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

   /**
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
