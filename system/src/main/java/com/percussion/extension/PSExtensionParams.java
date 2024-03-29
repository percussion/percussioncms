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
package com.percussion.extension;

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.util.PSDataTypeConverter;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Extension utilities to extract parameters, etc.
 * 
 * @author dougrand
 */
public class PSExtensionParams
{
   /**
    * The parameters
    */
   private Object m_params[] = null;

   /**
    * The parameter names, used in error messages. May or may not be set.
    */
   private String m_paramNames[] = null;

   /**
    * Creates an instance from the specified parameters.
    * 
    * @param params the specified parameters; it may not be <code>null</code>.
    * 
    * @throws PSConversionException if params is <code>null</code>.
    */
   public PSExtensionParams(Object params[]) throws PSConversionException {
      this(params, null);
   }

   /**
    * Creates an instance from the specified parameters.
    * 
    * @param params the specified parameters; it may not be <code>null</code>.
    * @param paramNames the names of the parameters, may be <code>null</code>
    * 
    * @throws PSConversionException if params is <code>null</code>.
    */
   public PSExtensionParams(Object params[], String paramNames[])
         throws PSConversionException {
      if (params == null)
      {
         throw new PSConversionException(IPSExtensionErrors.INVALID_NULL_PARAMS);
      }
      m_params = params;
      m_paramNames = paramNames;
   }

   /**
    * Get a string value from the parameters
    * 
    * @param index index into the parameter array, must be zero or positive
    * @param defvalue the default value, may be <code>null</code>
    * @param required if <code>true</code> then an exception is thrown when
    *           the given parameter is not present
    * @return the value or default value
    * 
    * @throws PSConversionException if required is <code>true</code> and the
    *            parameter at index is missing
    */
   public String getStringParam(int index, String defvalue, boolean required)
         throws PSConversionException
   {
      checkRequiredParameter(index, required);

      Object value = getValue(index, defvalue);

      if (value != null)
         return value.toString();
      else
         return null;
   }

   /**
    * Get a numeric value from the parameters. If the original parameter is a
    * string, and empty then the default value is returned.
    * 
    * @param index index into the parameter array, must be zero or positive
    * @param defvalue the default value, may be <code>null</code>
    * @param required if <code>true</code> then an exception is thrown when
    *           the given parameter is not present
    * 
    * @return the value or default value.
    * 
    * @throws PSConversionException if required is <code>true</code> and the
    *            parameter at index is missing, or value of parameter at index
    *            is not a number.
    */
   public Number getNumberParam(int index, Object defvalue, boolean required)
         throws PSConversionException
   {
      checkRequiredParameter(index, required);

      Object value = getValue(index, defvalue);

      if (value instanceof Number)
      {
         return (Number) value;
      }
      else if (value instanceof String)
      {
         if (StringUtils.isBlank((String) value))
            return null;
         try
         {
            return Integer.parseInt((String) value);
         }
         catch (NumberFormatException e) {
            throw new PSConversionException(
                  IPSExtensionErrors.INVALID_NUMBER_PARAM,
                  getParamIndicator(index));
         }
      }
      else if (value == null)
      {
         return null;
      }
      else
      {
         throw new PSConversionException(
               IPSExtensionErrors.INVALID_NUMBER_PARAM,
               getParamIndicator(index));
      }
   }

   /**
    * Get a boolean value from the parameters
    * 
    * @param index index into the parameter array, must be zero or positive
    * @param defvalue the default value, may be <code>null</code>
    * @param required if <code>true</code> then an exception is thrown when
    *           the given parameter is not present
    * 
    * @return the value or default value.
    * 
    * @throws PSConversionException if required is <code>true</code> and the
    *            parameter at index is missing, or value of parameter at index
    *            is not a boolean.
    */
   public Boolean getBooleanParam(int index, boolean defvalue, boolean required)
         throws PSConversionException
   {
      checkRequiredParameter(index, required);

      Object value = getValue(index, defvalue);

      if (value instanceof Boolean || (!required && value == null))
      {
         return (Boolean) value;
      }
      else if (value instanceof String)
      {
         String bvalue = (String) value;
         return bvalue.equalsIgnoreCase("true") || bvalue.equalsIgnoreCase("t")
               || bvalue.equalsIgnoreCase("yes");
      }
      else
      {
         throw new PSConversionException(
               IPSExtensionErrors.INVALID_BOOLEAN_PARAM,
               getParamIndicator(index));
      }
   }

   /**
    * Get a boolean value from the parameters
    * 
    * @param index index into the parameter array, must be zero or positive
    * @param defvalue the default value, may be <code>null</code>
    * @param required if <code>true</code> then an exception is thrown when
    *           the given parameter is not present
    * 
    * @return the value or default value.
    * 
    * @throws PSConversionException if required is <code>true</code> and the
    *            parameter at index is missing, or value of parameter at index
    *            is not a valid date.
    */
   public Date getDateParam(int index, Object defvalue, boolean required)
         throws PSConversionException
   {
      checkRequiredParameter(index, required);

      Object value = getValue(index, defvalue);

      if (value instanceof Date || (!required && value == null))
      {
         return (Date) value;
      }
      else if (value instanceof String)
      {
         Date date = PSDataTypeConverter.parseStringToDate((String) value);
         if (date == null)
         {
            throw new PSConversionException(
                  IPSExtensionErrors.INVALID_DATE_PARAM,
                  getParamIndicator(index));
         }
         return date;
      }
      else
      {
         throw new PSConversionException(IPSExtensionErrors.INVALID_DATE_PARAM,
               getParamIndicator(index));
      }
   }

   /**
    * Get the numbered parameter or <code>null</code> if the parameter is
    * missing
    * 
    * @param index the index of the parameter, must be zero or positive
    * 
    * @return the value of the specified parameter.
    * 
    * @throws PSConversionException if the index is invalid.
    */
   public Object getUncheckedParam(int index) throws PSConversionException
   {
      return getValue(index, null);
   }

   /**
    * Get the value or the default value
    * 
    * @param index the index, must be zero or greater
    * @param defvalue the default value if the parameter at the specified index
    *           is not specified (empty or <code>null</code>). t
    * @return the object at the specified index if exit; otherwise return the
    *         specified default value.
    * 
    * @throws PSConversionException if the index is less than zero.
    */
   private Object getValue(int index, Object defvalue)
         throws PSConversionException
   {
      if (index < 0)
      {
         throw new PSConversionException(
               IPSExtensionErrors.INVALID_INDEX_VALUE, getParamIndicator(index));
      }
      Object rval = isParamMissing(index) ? defvalue : m_params[index];

      if (rval instanceof IPSReplacementValue)
      {
         rval = ((IPSReplacementValue) rval).getValueText();

         if (rval instanceof String && StringUtils.isBlank((String) rval))
         {
            rval = defvalue;
         }
      }

      return rval;
   }

   /**
    * Get the indicator, either the param name if known or the index if not
    * 
    * @param index the index, assumed to be &gt;= <code>0</code>
    * @return the indicator, never <code>null</code>
    */
   private Object getParamIndicator(int index)
   {
      if (m_paramNames != null && m_paramNames.length > index)
      {
         return m_paramNames[index];
      }
      else
      {
         return new Integer(index);
      }
   }

   /**
    * Check to see if a required parameter is present
    * 
    * @param index the index
    * @param required if the param is required
    * 
    * @throws PSConversionException if the required is <code>true</code> and
    *            the index's parameter is missing.
    */
   private void checkRequiredParameter(int index, boolean required)
         throws PSConversionException
   {
      if (required && isParamMissing(index))
      {

         throw new PSConversionException(
               IPSExtensionErrors.MISSING_REQUIRED_PARAM_NO,
               getParamIndicator(index));
      }
   }

   /**
    * Discover if a parameter is missing
    * 
    * @param index
    * @return <code>true</code> if the parameter is not present
    */
   private boolean isParamMissing(int index)
   {
      return index >= m_params.length || m_params[index] == null;
   }
}
