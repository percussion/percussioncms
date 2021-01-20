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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates the default configuration for the supplied config def file path.
 * 
 * @author bjoginipally
 * 
 */
public class PSDefaultConfigGenerator
{
   /**
    * Private constructor to avoid instantiation.
    */
   private PSDefaultConfigGenerator()
   {}

   /**
    * Returns the singleton instance of this class.
    * 
    * @return
    */
   public static PSDefaultConfigGenerator getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSDefaultConfigGenerator();
      return ms_instance;
   }

   /**
    * Generates the default configuration file and returns it as String for the
    * supplied config def file.
    * 
    * @param publisherPrefix The publisher prefix must not be empty. It will be
    * added to the root element as a publisherPrefix attribute. The replacement
    * names that do not start with this name are not processed.
    * @param publisherName The publisher name is added to the default
    * configuration file as an attribute to the root element, must not be empty.
    * @param solutionName The solution name is added as value of name attribute
    * of element SolutionConfig, must not be empty.
    * @param configDefPath the path of the config definition file from which the
    * default config is generated, must not be empty.
    * @return String representation of default config xml document, that gets
    * built as per the <code>localConfig.xsd</code> schema.
    */
   public String generateDefaultConfig(String publisherName,
         String publisherPrefix, String solutionName, String configDefPath)
   {
      if (StringUtils.isBlank(configDefPath))
         throw new IllegalArgumentException("configDefPath must not be empty");
      if (StringUtils.isBlank(publisherName))
         throw new IllegalArgumentException("publisherName must not be empty");
      if (StringUtils.isBlank(publisherPrefix))
         throw new IllegalArgumentException(
               "publisherPrefix must not be empty");
      if (StringUtils.isBlank(solutionName))
         throw new IllegalArgumentException("solutionName must not be empty");
      String packageName = publisherPrefix + "." + solutionName;
      ms_logger.info("Generating  default configuration file for package \""
            + packageName + "\"");
      String defaultConfig = "";

      PSImplConfigLoader loader = new PSImplConfigLoader(configDefPath);
      List<IPSConfigHandler> handlers = new ArrayList<IPSConfigHandler>();
      for (String bean : loader.getAllBeanNames())
      {
         handlers.add(loader.getBean(bean));
      }
      PSConfigMerger merger = new PSConfigMerger();
      PSPair<Map<String, Object>, List<Exception>> propertyDefs = merger
            .getPropertyDefs(handlers);
      PSConfigDeNormalizer cfgDeNormalizer = PSConfigDeNormalizer.getInstance();
      // Log the errors if any and pass the error messages to normalizer to put
      // in the putput xml file.
      List<String> errors = null;
      if (!propertyDefs.getSecond().isEmpty())
      {
         errors = new ArrayList<String>();
         ms_logger.error("The following errors occurred while generating the "
               + "default configuration file for package \"" + packageName
               + "\"");
         for (Exception exception : propertyDefs.getSecond())
         {
            ms_logger.error(exception);
            errors.add(exception.getLocalizedMessage());
         }
      }
      defaultConfig = cfgDeNormalizer.getDeNormalizedXml(propertyDefs
            .getFirst(), errors, publisherName, publisherPrefix, solutionName);
      ms_logger.info("Finished generating  default configuration file for "
            + "package \"" + packageName + "\"");

      return defaultConfig;
   }

   /**
    * The singleton instance of this class initialized in {@link #getInstance()}
    * method.
    */
   private static PSDefaultConfigGenerator ms_instance = null;
   
   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSDefaultConfigGenerator");
   

}
