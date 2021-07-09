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
