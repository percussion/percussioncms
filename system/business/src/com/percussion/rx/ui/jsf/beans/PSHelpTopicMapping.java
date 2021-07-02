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
