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

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class provides convenience methods for setters that implementing
 * property validation. The derived class must override
 * {@link #validate(String, ObjectState, String, Object, Object)}
 * 
 * @author YuBingChen
 */
public class PSPropertySetterWithValidation extends PSSimplePropertySetter
{
   @Override
   public List<PSConfigValidation> validate(String objName, ObjectState state,
         IPSPropertySetter setter)
   {
      Map<String, Object> properties = getProperties();
      if (properties == null || properties.isEmpty())
         return Collections.emptyList();
      
      Map<String, Object> otherProps = setter.getProperties();
      if (otherProps == null || otherProps.isEmpty())
         return Collections.emptyList();
      
      try
      {
         List<PSConfigValidation> result = new ArrayList<PSConfigValidation>();
         for (Map.Entry<String, Object> prop : properties.entrySet())
         {
            List<PSConfigValidation> subResult = validate(objName, state, prop
                  .getKey(), prop.getValue(), otherProps.get(prop.getKey()));
            result.addAll(subResult);
         }
         return result;
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to validate the name \""
               + objName + "\"";
         ms_log.error(errorMsg, e);
         throw new PSConfigException(errorMsg, e);
      }
   }
   
   /**
    * Validates a specified property for a design object.
    * 
    * @param objName the design object name, not blank.
    * @param state the state of the object, not <code>null</code>.
    * @param propName the specified property name, not blank.
    * @param propValue the property value of the current setter, it may be
    * <code>null</code>.
    * @param otherValue the property value of the other setter, it may be
    * <code>null</code>.
    * 
    * @return a list of validation result, never <code>null</code>, may be
    * <code>null</code> if there is no error or warning.
    */
   protected List<PSConfigValidation> validate(String objName, ObjectState state,
         String propName, Object propValue, Object otherValue)
   {
      return Collections.emptyList();
   }
   
   /**
    * Logger for this class.
    */
   private static Log ms_log = LogFactory.getLog("PSPropertySetterWithValidation");   
}
