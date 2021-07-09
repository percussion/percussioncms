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
package com.percussion.install;

//java
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



/**
 * This plugin has been written to convert all the applications xml files in
 * clients rhythmyx objectstore directory to use RXDUAL table instead of DUAL
 * table.
 *
 */

public class PSUpgradePluginConvertAppsToUseRxDual implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginConvertAppsToUseRxDual()
   {
   }

   /**
    * Implements the process function of IPSUpgardePlugin. Converts all the
    * application files to use RXDUAL table instead of DUAL table.
    * Gets all table,alias,tableAlias nodes and if node value is DUAL then
    * changes its value to RXDUAL. After conversion the file will be saved.
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {

      config.getLogStream().println("Converting application files to use " +
         "RXDUAL table.");
      File appsDir = new File(RxUpgrade.getRxRoot() + OBJECT_STORE_DIRECTORY);
      File[] appFiles = appsDir.listFiles();
      PrintWriter pw = null;
      File appFile = null;
      String targetFile = null;
      Document doc = null;
      Node node = null;
      NodeList nl = null;
      int modified1,modified2,modified3;
      try
      {
         for(int iApp = 0; iApp < appFiles.length; ++iApp)
         {
            appFile = appFiles[iApp];
            if(appFile.isFile() && ((appFile.getName().indexOf(
               ".xml")+4)==appFile.getName().length()))
            {
               modified1 = modified2 = modified3 = 0;
               targetFile = appFile.getAbsolutePath();
               config.getLogStream().println(
                  "Processing file: " + targetFile + "...");
               doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(
                  appFile.toURL().toString()), false);
               nl = doc.getElementsByTagName("table");
               modified1 = dualToRxDual(nl);
               nl = doc.getElementsByTagName("alias");
               modified2 = dualToRxDual(nl);
               nl = doc.getElementsByTagName("tableAlias");
               modified3 = dualToRxDual(nl);
               if(modified1==1||modified2==1||modified3==1)
               {
                  config.getLogStream().println("Saving the transformed file: "
                     + targetFile + "...");
                  pw = new PrintWriter(new FileOutputStream(targetFile));
                  pw.print(PSXmlDocumentBuilder.toString(doc));
                  pw.flush();
                  config.getLogStream().println("Saved the transformed file: "
                     + targetFile + "...");
               }
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         try
         {
            if(pw != null)
            {
               pw.close();
               pw =null;
            }
         }
         catch(Throwable t)
         {
         }
      }
      config.getLogStream().println(
         "Finished process() of the plugin DUAL To RXDUAL...");
      return null;
   }

    /**
    * Helper function that converts the node value to RXDUAL if it is DUAL.
    *
    * @param nl - DOM NodeList, can not be <code>null</code>.
    *
    * @return - int flag 0 if DUAL not found 1 if found.
    *
    */

   static public int dualToRxDual(NodeList nl)
   {
      int flag = 0;
      Node node = null;
      if(nl != null && nl.getLength()!=0)
      {
         for(int i=0; i<nl.getLength(); i++)
         {
            node = nl.item(i);
            if(node.getFirstChild().getNodeType()==Node.TEXT_NODE &&
               node.getFirstChild().getNodeValue().equalsIgnoreCase("DUAL"))
            {
               node.getFirstChild().setNodeValue("RXDUAL");
               flag = 1;
            }
         }
      }
      return flag;
   }

  /**
    * String constant for objectstore directory.
    */
   private static final String OBJECT_STORE_DIRECTORY = "ObjectStore";

   /**
    * String constant for dual to rxdual conversion xsl.
    */
   private static final String DUAL_TO_RXDUAL_XSL_FILE = "dualtorxdual.xsl";

}
