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

package com.percussion.utils.container;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.tools.SortedProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.percussion.utils.jdbc.PSJdbcUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSStaticContainerUtils
{
   private static final Logger ms_log = LogManager.getLogger(PSStaticContainerUtils.class);

   public static Properties getProperties(File f)
   {

      SortedProperties prop = new SortedProperties();
      
      if (f.exists())
      {
         try (FileReader reader = new FileReader(f))
         {
            // load a properties file
            prop.load(reader);
         }
         catch (IOException ex)
         {
            throw new RuntimeException("Failed to load properties file " + f.getAbsolutePath());
         }
         
      }
      return prop;
   }

   public static String decrypt(String str, String encrypted)
   {
      if (StringUtils.equalsIgnoreCase(encrypted, "Y"))
      {
         try{
            str = PSEncryptor.getInstance("AES",
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                            PSEncryptor.SECURE_DIR)
            ).decrypt(str);
         } catch (PSEncryptionException e) {
            str = PSLegacyEncrypter.getInstance(
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                    PSEncryptor.SECURE_DIR)
            ).decrypt(str, PSLegacyEncrypter.getInstance(
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                    PSEncryptor.SECURE_DIR)).getPartOneKey(),null);
         }

      }
      return str;
   }

   public static String connectionTestQuery(String driver, String override)
   {
      if (driver.equalsIgnoreCase(PSJdbcUtils.JTDS_DRIVER))
      {
         return "select 1";
      }

      return null;
   }

   public static void saveProperties(Properties props, File propertyFile) throws IOException
   {

      File tempFile = PSStaticContainerUtils.getTempFile(propertyFile);
      boolean saved = false;
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile)))
      {
         // save properties to project root folder
         props.store(writer, null);
         saved = true;
      }
      catch (IOException e)
      {
         ms_log.error("error saving properties file propertyFile", e);
      }
      if (saved)
         FileUtils.copyFile(tempFile, propertyFile);
      Files.deleteIfExists(tempFile.toPath());
   }

   public static String buildConnectionUrl(Properties dbProps)
   {
      String url = "jdbc:" + dbProps.getProperty(IPSJdbcDbmsDefConstants.DB_DRIVER_NAME_PROPERTY) + ":"
            + dbProps.getProperty(IPSJdbcDbmsDefConstants.DB_SERVER_PROPERTY);
      return url;
   }

   /**
    * @param propertyFile
    * @return
    * @throws IOException
    */
   public static File getTempFile(File propertyFile) throws IOException
   {
      File tempFile = File.createTempFile(FilenameUtils.removeExtension(propertyFile.getAbsolutePath().toString()),
            ".tmp");
      tempFile.deleteOnExit();
      return tempFile;
   }
  

}
