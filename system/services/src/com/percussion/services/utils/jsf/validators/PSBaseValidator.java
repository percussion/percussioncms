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
package com.percussion.services.utils.jsf.validators;

import com.percussion.i18n.PSI18nUtils;

import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for validators, encapsulates message production
 * 
 * @author dougrand
 */
public abstract class PSBaseValidator implements Validator
{
   /**
    * Generate and throw a faces validation error
    * 
    * @param sev the severity of the error
    * @param key a key to look up the error message in the tmx file
    * @throws ValidatorException to signal the problem
    */
   protected void fail(Severity sev, String key)
   {
      String message = PSI18nUtils.getString(key);
      FacesMessage m = new FacesMessage(sev, message, message);
      throw new ValidatorException(m);
   }
   
   /**
    * Generate and throw a faces validation error
    * 
    * @param sev the severity of the error
    * @param key a key to look up the error message in the tmx file
    * @param args an array of arguments
    * @throws ValidatorException to signal the problem
    */
   protected void fail(Severity sev, String key, Object... args)
   {
      String message = PSI18nUtils.getString(key);
      message = MessageFormat.format(message, args);
      FacesMessage m = new FacesMessage(sev, message, message);
      throw new ValidatorException(m);
   }   

   /**
    * Extract string value
    * 
    * @param value the input value
    * @param required if <code>true</code> then a missing value will be a
    *           failure
    * @return the value cast to a string
    */
   protected String getString(Object value, boolean required)
   {
      if (value == null || !(value instanceof String))
      {
         fail(FacesMessage.SEVERITY_ERROR, "jsf@missing_string_value");
      }
      String str = (String) value;
      if (StringUtils.isBlank(str) && required)
      {
         fail(FacesMessage.SEVERITY_ERROR, "jsf@missing_required_value");
      }
      return str;
   }

}
