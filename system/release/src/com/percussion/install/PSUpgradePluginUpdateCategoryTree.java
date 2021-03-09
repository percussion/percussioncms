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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PSUpgradePluginUpdateCategoryTree implements IPSUpgradePlugin
{

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();
      logger.println("Category tree update process started.");
      
      File rxRoot = new File(RxUpgrade.getRxRoot());
      File categoryFile = new File(rxRoot, "/web_resources/categories/tree.xml");
      File backupFile = new File(rxRoot, "/web_resources/categories/tree_bkp_" + (new Date()).getTime()+ ".xml");
      PSPluginResponse resp = null;
      try
      {
         resp = updateCategories(categoryFile, backupFile);
      }
      catch (Exception e)
      {
         e.printStackTrace(logger);
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e.getLocalizedMessage());
      }
      logger.println("Category tree update process finished.");
      return resp;
   }
   
   protected PSPluginResponse updateCategories(File target, File backup) throws Exception
   {
      if (target == null)
      {
         throw new IllegalArgumentException("target may not be null");
      }
      if (!target.exists())
      {
         throw new IllegalArgumentException("target must exist");
      }

         logger.println("Reading category document");
         // Update the delivery servers file
         try(FileInputStream in = new FileInputStream(target)){
            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  in, false);
            //Back up the file
            try (FileOutputStream bout = new FileOutputStream(backup)) {
               PSXmlDocumentBuilder.write(doc, bout);
               //Update the document
               doc = updateDocument(doc);
               logger.println("Category document has been updated, ids are now same as labels.");
               try(FileOutputStream out = new FileOutputStream(target)) {
                  PSXmlDocumentBuilder.write(doc, out);
                  logger.println("Category document has been written back to disk.");
               }
            }
         }
      return new PSPluginResponse(PSPluginResponse.SUCCESS, "Successfully upgraded category ids with labels.");
   }

   protected Document updateDocument(Document doc)
   {
      NodeList nodes = doc.getElementsByTagName("Node");
      for(int i=0; i<nodes.getLength();i++)
      {
         Element node = (Element) nodes.item(i);
         String label = StringUtils.defaultString(node.getAttribute("label"));
         node.setAttribute("id",label);
      }
      return doc;
   }
   
   private void closeStream(Closeable stream){
      if (stream != null)
      {
         try
         {
            stream.close();
         }
         catch (Exception e)
         {
         }
      }      
   }
   /**
    * Set logger, only used by unit test, otherwise set by process() method
    * 
    * @param out print stream to log to.
    */
   void setLogger(PrintStream out)
   {
      logger = out;
   }
   
   private PrintStream logger;
}
