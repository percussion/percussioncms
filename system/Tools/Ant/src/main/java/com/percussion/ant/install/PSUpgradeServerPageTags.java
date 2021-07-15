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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * PSUpgradeServerPageTags is a product action task that upgrades
 * the following files if they already exist in a Rhythmyx installation:
 *
 * serverPageTags.xml
 * rxW2KserverPageTags.xml
 *
 * It retrieves the current files from the sandbox from the source tree during
 * the install, then creates a list of all "tag" elements whose "isXslTag"
 * attribute has the value "yes". Then it iterates over this list. For each
 * "tag" element, it loops through all the "tag" elements in the currently
 * installed Xml document looking for a matching "tag" element.  "tag" elements
 * are equal if the value of child elements "opening" and "closing" are equal
 * (comparison is case-sensitive). If a matching "tag" element is found, then
 * the value of its "isXslTag" attribute is set to "yes".
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="upgradeServerPageTags"
 *              class="com.percussion.ant.install.PSUpgradeServerPageTags"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to update the page tag files.
 *
 *  <code>
 *  &lt;upgradeServerPageTags
 *      serverPageTagsXmlE2FilePath=
 *      "${install.dir}/htmlconverter/config/serverPageTags.xml"
 *      serverPageTagsXmlRxFilePath=
 *      "rxconfig/XSpLit/serverPageTags.xml"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpgradeServerPageTags extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String installLoc = getRootDir();
      if (StringUtils.isBlank(getRootDir()))
      {
         throw new IllegalArgumentException("installLoc may not be null or empty");
      }

      try
      {
         File diskXmlFile = new File(installLoc, serverPageTagsXmlRxFilePath);
         if (!diskXmlFile.exists())
         {
            PSLogger.logInfo("Server Page Tags Xml file does not exist : "
                  + diskXmlFile.getAbsolutePath());
            return;
         }
         else
         {
            // create a backup of the existing file
            File backupFile = IOTools.createBackupFile(diskXmlFile);
         }

         // Create a temp file for serverPageTags.xml
         String xmlResFile;
         File archiveXmlFile = new File(serverPageTagsXmlE2FilePath);
         xmlResFile = IOTools.createTempFile(archiveXmlFile).getAbsolutePath();

         InputStream diskStream = null;
         InputStream resStream = null;
         FileWriter fw = null;
         try
         {
            resStream = new FileInputStream(xmlResFile);
            Document resDoc = PSXmlDocumentBuilder.createXmlDocument(
                  resStream, false);

            // list of "tag" elements whose attribute "isXslTag" has the
            // value "yes"
            List processTagNodes = new ArrayList();

            NodeList resNl = resDoc.getElementsByTagName("tag");
            for (int i = 0; i < resNl.getLength(); i++)
            {
               Element resEl  = (Element)resNl.item(i);
               String isXslTagAttrVal = resEl.getAttribute("isXslTag");
               if (isXslTagAttrVal != null)
               {
                  isXslTagAttrVal = isXslTagAttrVal.trim();
                  if (isXslTagAttrVal.equalsIgnoreCase("yes"))
                     processTagNodes.add(resEl);
               }
            }

            if (processTagNodes.size() > 0)
            {
               diskStream = new FileInputStream(diskXmlFile);
               Document diskDoc = PSXmlDocumentBuilder.createXmlDocument(
                     diskStream, false);
               NodeList diskNl = diskDoc.getElementsByTagName("tag");

               // if the attribute of any element is modified then the Xml
               // document should be written back to disk
               boolean processedOne = false;

               for (int i = 0; i < processTagNodes.size(); i++)
               {
                  PSXmlTreeWalker resWalker =
                     new PSXmlTreeWalker((Element)processTagNodes.get(i));
                  String resOpening = resWalker.getElementData("opening", true);
                  String resClosing = resWalker.getElementData("closing", true);

                  if ((resOpening == null) || (resClosing == null))
                     continue;

                  for (int j = 0; j < diskNl.getLength(); j++)
                  {
                     Element diskEl  = (Element)diskNl.item(j);
                     PSXmlTreeWalker diskWalker = new PSXmlTreeWalker(diskEl);
                     String diskOpening = diskWalker.getElementData("opening", true);
                     String diskClosing = diskWalker.getElementData("closing", true);

                     if ((diskOpening == null) || (diskClosing == null))
                        continue;

                     if ((diskOpening.equalsIgnoreCase(resOpening)) &&
                           (diskClosing.equalsIgnoreCase(resClosing)))
                     {
                        String xslTagAttrVal = diskEl.getAttribute("isXslTag");
                        if (xslTagAttrVal == null)
                           xslTagAttrVal = "no";
                        if (!xslTagAttrVal.equalsIgnoreCase("yes"))
                        {
                           diskEl.setAttribute("isXslTag", "yes");
                           processedOne = true;
                        }
                     }
                  }
               }

               if (processedOne)
               {
                  // Already created a backup of this file, so we can safely
                  // delete the file, otherwise if the file is readonly then
                  // write opertaion will throw an exception
                  diskXmlFile.delete();
                  diskXmlFile.createNewFile();
                  fw = new FileWriter(diskXmlFile);
                  PSXmlTreeWalker walker = new PSXmlTreeWalker(diskDoc);
                  walker.write(fw);
               }
            }
         }
         catch (Exception e)
         {
            PSLogger.logInfo("ERROR : " + e.getMessage());
            PSLogger.logInfo(e);
         }
         finally
         {
            if (diskStream != null)
            {
               try
               {
                  diskStream.close();
               }
               catch (Exception e)
               {
                  // no-op
               }
            }
            if (diskStream != null)
            {
               try
               {
                  diskStream.close();
               }
               catch (Exception e)
               {
                  // no-op
               }
            }
            if (fw != null)
            {
               try
               {
                  fw.close();
               }
               catch (Exception e)
               {
                  // no-op
               }
            }
         }
      }
      catch (Exception e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
   }

   /***************************************************************
    * Mutators and Accessors
    ***************************************************************/

   /**
    * Returns the path of the server page tags xml file relative to the
    * installation directory.
    *
    * @return the path of the server page tags xml file relative to the
    * installation directory, never <code>null</code> or empty
    */
   public String getServerPageTagsXmlRxFilePath()
   {
      return serverPageTagsXmlRxFilePath;
   }

   /**
    * Sets the path of the server page tags xml file relative to the
    * installation directory.
    *
    * @param serverPageTagsXmlRxFilePath the path of the server page tags xml
    * file relative to the installation directory, may not be
    * <code>null</code> or empty
    *
    * @throw IllegalArgumentException if serverPageTagsXmlRxFilePath is
    * <code>null</code> or empty
    */
   public void setServerPageTagsXmlRxFilePath(String serverPageTagsXmlRxFilePath)
   {
      if ((serverPageTagsXmlRxFilePath == null) ||
            (serverPageTagsXmlRxFilePath.trim().length() == 0))
         throw new IllegalArgumentException(
         "serverPageTagsXmlRxFilePath may not be null or empty");
      this.serverPageTagsXmlRxFilePath = serverPageTagsXmlRxFilePath;
   }

   /**
    * Returns the path of the server page tags xml file on the build machine.
    *
    * @return the path of the server page tags xml file on the build machine,
    * never <code>null</code> or empty
    */
   public String getServerPageTagsXmlE2FilePath()
   {
      return serverPageTagsXmlE2FilePath;
   }

   /**
    * Sets the path of the server page tags xml file on the build machine.
    *
    * @param serverPageTagsXmlE2FilePath the path of the server page tags xml
    * file on the build machine, may not be <code>null</code> or empty
    *
    * @throw IllegalArgumentException if serverPageTagsXmlE2FilePath is
    * <code>null</code> or empty
    */
   public void setServerPageTagsXmlE2FilePath(String serverPageTagsXmlE2FilePath)
   {
      if ((serverPageTagsXmlE2FilePath == null) ||
            (serverPageTagsXmlE2FilePath.trim().length() == 0))
         throw new IllegalArgumentException(
         "serverPageTagsXmlE2FilePath may not be null or empty");
      this.serverPageTagsXmlE2FilePath = serverPageTagsXmlE2FilePath;
   }

   /***************************************************************
    * Bean properties
    ***************************************************************/

   /**
    * path of the server page tags xml file relative to the installation
    * root directory, never <code>null</code> or empty.
    */
   String serverPageTagsXmlRxFilePath = "rxconfig/XSpLit/serverPageTags.xml";

   /**
    * path of the server page tags xml file on the build machine, never
    * <code>null</code>, may be empty.
    */
   private String serverPageTagsXmlE2FilePath = "";

   /**************************************************************************
    * private function
    **************************************************************************/

}
