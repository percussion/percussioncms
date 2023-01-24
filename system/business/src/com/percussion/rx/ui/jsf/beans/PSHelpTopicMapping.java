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
package com.percussion.rx.ui.jsf.beans;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to manage the mapping between the help topic to its
 * related HTML file name. 
 */
public class PSHelpTopicMapping
{

   /**
    * Get the actual HTML help file name from the specified help topic.
    * @param topic the help topic, never <code>null</code> or empty.
    * @return the HTML help file name, never <code>null</code> or empty.
    */
   public static String getFileName(String topic)
   {
      if (StringUtils.isBlank(topic))
         throw new IllegalArgumentException("topic may not be null or empty.");
      
      ResourceBundle bundle = getBundle();
      String fileName = bundle.getString(topic);
      if (fileName == null)
      {
         String msg = "Cannot find topic: '" + topic + "'";
         ms_log.error(msg);
         throw new IllegalArgumentException(msg);
      }
      return fileName;
   }
   
   /**
    * Get the mapping that maps help topic to the actual file name.
    * @return the mapping, never <code>null</code>.
    */
   private static ResourceBundle getBundle()
   {
      if (ms_helpTopicToFile != null)
         return ms_helpTopicToFile;
      
      try
      {
            ms_helpTopicToFile = ResourceBundle
               .getBundle("com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping");
         return ms_helpTopicToFile;
      }
      catch( MissingResourceException mre )
      {
         ms_log.error("Failed to load PSHelpTopicMapping.properties file");
         throw mre;
      }
   }

   /**
    * The bundle contains the mapping that maps the help topic to the
    * actual HTML help file name.
   **/
   private static ResourceBundle ms_helpTopicToFile = null;

   /**
    * The logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSHelpTopicMapping.class);  
}
