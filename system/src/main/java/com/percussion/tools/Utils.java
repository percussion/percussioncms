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

package com.percussion.tools;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
* This class is a place holder for all utility functions that do not need classes
* by themseleves. All methods in this shall be static and public.
*
*/
public class Utils
{
   private Utils()
   {
   }

   /**
   * This method returns the Version.properties file as a Java Properties
   * file from a given JAR file.
   *
   * @param root - the root path of the JAR file location, e.g. d:/Rhythmyx/lib
   *
   * @param sJarFile - the jar file name as string
   *
   * @param path - the path of the file Version.properties in the JAR file, e.g.
   * com/percussion/publisher.
   *
   * @return Java Properties file - never null.
   *
   * @throws - FileNotFoundException
   *
   * @throws - IOException
   *
   */
   static public Properties getVersionPropsFromJAR(String root, String sJarFile,
      String path)
         throws FileNotFoundException, IOException
   {
      Properties props = new Properties();

      File file = new File(root, sJarFile);
      JarFile jfile = new JarFile(file);
      ZipEntry entry = jfile.getEntry(path + "/Version.properties");
      if(null == entry)
         return props;

      try(InputStream is = jfile.getInputStream(entry)) {
         props.load(is);
      }

      return props;
   }

   /**
    * Returns DocumentBuilder object for parsing XML documents
    * @return DocumentBuilder object for parsing XML documents. Never
    * <code>null</code>.
    */
   static public DocumentBuilder getDocumentBuilder()
   {
      try
      {
         DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 new PSXmlSecurityOptions(
                         true,
                         true,
                         true,
                         false,
                         true,
                         false
                 ));

         dbf.setNamespaceAware(true);
         dbf.setValidating(false);
         return dbf.newDocumentBuilder();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage());
      }
   }

}
