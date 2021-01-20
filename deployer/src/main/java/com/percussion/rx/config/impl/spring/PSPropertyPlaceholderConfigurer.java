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
package com.percussion.rx.config.impl.spring;

import com.percussion.rx.config.IPSBeanProperties;
import com.percussion.rx.config.PSBeanPropertiesLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * This class uses {@link PropertyPlaceholderConfigurer} to attempt to resolve
 * a given place-holder 1st, then attempt to resolve the place-holder from 
 * the instance of {@link IPSBeanProperties} if it cannot be resolved by
 * its super class. 
 *
 * @author YuBingChen
 */
public class PSPropertyPlaceholderConfigurer extends
      PropertyPlaceholderConfigurer
{

   @Override
   protected String resolvePlaceholder(String placeholder, Properties props)
   {
      String value = super.resolvePlaceholder(placeholder, props);
      if (value != null)
         return value;
      
      IPSBeanProperties pMgr = PSBeanPropertiesLocator.getBeanProperties();
      String v = pMgr.getString(placeholder);
      if (v == null)
         ms_log.warn("Cannot replace placeholder: \"" + placeholder + "\".");
      
      return v;
   }

   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog("PSPropertyPlaceholderConfigurer");
   
}
