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
