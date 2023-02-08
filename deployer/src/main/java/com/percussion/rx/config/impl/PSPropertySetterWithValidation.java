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
package com.percussion.rx.config.impl;

import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.services.error.PSNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
         List<PSConfigValidation> result = new ArrayList<>();
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
         String propName, Object propValue, Object otherValue) throws PSNotFoundException {
      return Collections.emptyList();
   }
   
   /**
    * Logger for this class.
    */
   private static final Logger ms_log = LogManager.getLogger("PSPropertySetterWithValidation");
}
