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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSInstallation;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author dougrand
 * 
 * Verify that all the proper extensions are present
 */
public class PSVerifyExtensions implements IPSVerify
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#generate(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   public void generate(File rxdir, PSInstallation installation)
         throws Exception
   {
      File extensionfile = findExtensionFile(rxdir);
      if (extensionfile == null)
      {
         throw new IOException("Extensions.xml file not found");
      }
      
      Reader r = new FileReader(extensionfile);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);

      // Get extension elements
      NodeList extensions = doc.getElementsByTagName("Extension");
      int len = extensions.getLength();

      for (int i = 0; i < len; i++)
      {
         Element ext = (Element) extensions.item(i);
         String category = ext.getAttribute("categorystring");
         String exit = ext.getAttribute("name");
         if (category == null || category.trim().length() == 0)
            category = "unknown";
         installation.addExtension(category, exit);
      }
   }

   /**
    * Look for the <code>Extensions.xml</code> file in the appropriate
    * directory. Scans through until the file is found.
    * 
    * @param rxdir the rhythmyx directory, assumed never <code>null</code>
    * @return a {@link File}representing the extensions file or
    *         <code>null</code> if that file simply doesn't exist
    */
   private File findExtensionFile(File rxdir)
   {
      File exdir = new File(rxdir, "Extensions/Handlers/Java");

      if (exdir.exists() == false)
         return null;

      File[] dirs = exdir.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            return pathname.isDirectory();
         }
      });

      for (int i = 0; i < dirs.length; i++)
      {
         File dir = dirs[i];
         File exfile = new File(dir, "Extensions.xml");
         if (exfile.exists())
            return exfile;
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#verify(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   @SuppressWarnings("unchecked")
   public void verify(File rxdir, File originalRxDir, PSInstallation installation) throws Exception
   {
      Logger l = Logger.getLogger(getClass());
      // Call generate on the new directory, then compare the differences
      PSInstallation existing = new PSInstallation();
      generate(rxdir, existing);

      // Compare the two maps and print out information
      l.info("Verify extensions " + rxdir);

      Iterator iter = installation.getExtensionCategories();
      while (iter.hasNext())
      {
         String category = (String) iter.next();
         List<String> bomelements = (List<String>) installation.getExtensions(category);
         List<String> realelements = (List<String>) existing.getExtensions(category);
         Set<String> bomset = new HashSet<String>(bomelements);
         Set<String> realset = null;

         if (realelements != null)
         {
            realset = new HashSet<String>(realelements);
         }
         else
         {
            realset = new HashSet();
         }

         if (realelements == null || realelements.size() == 0)
         {
            l.warn(category + " extensions missing");
         }
         else if (bomset.equals(realset))
         {
            l.info(category + " extensions installed");
         }
         else
         {
            bomset.removeAll(realset);
            l.warn(category + " extensions has missing elements");
            Iterator missing = bomset.iterator();
            while (missing.hasNext())
            {
               String missingelement = (String) missing.next();
               l.debug("Missing: " + missingelement);
            }
         }
      }

   }

}
