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

package com.percussion.utils.container;

import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class PSStaticContainerUtils
{
   private static final Logger ms_log = LogManager.getLogger(PSStaticContainerUtils.class);

   public static Properties getProperties(File f)
   {

      Properties prop = new Properties();
      
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
            str = PSEncryptor.decryptString(PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),str);
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
