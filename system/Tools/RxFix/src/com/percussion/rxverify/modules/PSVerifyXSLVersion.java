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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSInstallation;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

/**
 * @author dougrand
 * 
 * Verify that the installer has updated all XSL stylesheets. This has no
 * generate component, because it is a pass/fail based on what is actually
 * there, rather than a comparison with a known good case.
 */
public class PSVerifyXSLVersion implements IPSVerify
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
      // No op
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#verify(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   public void verify(File rxdir, File originalRxDir,
         PSInstallation installation) throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      // Find all XSL files, and make sure each has the right version. This
      // code is very inefficient since it is reading each stylesheet into
      // a DOM, but the verifier does not need to be overly efficient
      File[] contents = rxdir.listFiles();

      // Three kinds of files, directories, stylesheets and non-stylesheets
      for (int i = 0; i < contents.length; i++)
      {
         File file = contents[i];

         if (file.isDirectory())
         {
            verify(file, originalRxDir, installation);
         }
         else if (file.getName().endsWith(".xsl"))
         {
            Reader r = new FileReader(file);
            try
            {
               Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
               // Check the root element's version attribute
               Element root = doc.getDocumentElement();
               String version = root.getAttribute("version");
               if (version == null || version.trim().length() == 0)
               {
                  l.error("Missing version attribute " + file);
               }
               else if (version.equals("1.1") == false)
               {
                  l.error("Wrong version " + version + " in " + file);
               }
            }
            catch (SAXParseException e)
            {
               l.error("Bad stylesheet file " + file + "\nproblem "
                     + e.getLocalizedMessage());
            }
         }
         else
         {
            // Ignore
         }
      }
   }

}
