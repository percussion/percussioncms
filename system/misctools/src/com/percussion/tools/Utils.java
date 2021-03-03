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

package com.percussion.tools;

import com.percussion.security.xml.PSSecureXMLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

      InputStream is = jfile.getInputStream(entry);
      props.load(is);
      is.close();

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
          false);
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
