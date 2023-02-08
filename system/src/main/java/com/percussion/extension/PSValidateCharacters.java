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
import com.percussion.security.SecureStringUtils;
import com.percussion.server.IPSRequestContext;
import org.apache.commons.collections.IteratorUtils;

import java.io.File;

import static org.apache.commons.lang.StringUtils.containsAny;
import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Field validation extension that checks if the 
 * given field has any of the given invalid characters.
 * <p>
 * <table border="1">
 * <tr><th>Param</th><th>Name</th><th>Description</th></tr>
 * <tr><td>0</td><td>fieldName</td><td>(String) If null <code>{@link #DEFAULT_FIELD_NAME}</code> is used.</td></tr>
 * <tr><td>1</td><td>invalidChars</td><td>(String) If null {@link SecureStringUtils#INVALID_ITEM_NAME_CHARACTERS} is used.</td></tr>
 * </table>
 * @author adamgent
 *
 */
public class PSValidateCharacters implements IPSFieldValidator
{

   /**
    * The name of the field to use if none is provided
    * in the parameters.
    */
   protected static final String DEFAULT_FIELD_NAME = "sys_title";
   /**
    * The runtime parameter names.
    */
   protected String parameterNames[];
     
   /**
    * Validates characer by executing the following methods in order:
    * <ol>
    * <li>{@link #getInvalidCharacters(Object[], IPSRequestContext)}</li>
    * <li>{@link #getFieldValue(Object[], IPSRequestContext)}</li>
    * </ol>
    */
   public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
   {
      String invalidChars = getInvalidCharacters(params, request);
      String fv = getFieldValue(params, request);
      /*
       * Does field value contain any bad characters?
       */
      return  !  containsAny(fv, invalidChars);
   }
   
   /**
    * Gets a string of invalid characters.
    * Each character in the string should be unique (not required) 
    * or otherwise it will be redundant.
    * <p>
    * It is ok to override this method.
    * 
    * @param params never <code>null</code>.
    * @param request never <code>null</code>.
    * @return maybe <code>null</code>.
    */
   protected String getInvalidCharacters(Object[] params, IPSRequestContext request) 
   {
      return getParameter(params, 1, SecureStringUtils.INVALID_ITEM_NAME_CHARACTERS, false);
   }
   
   /**
    * 
    * Gets the field value from the request.
    * <p>
    * It is ok to override this method.
    * 
    * @param params never <code>null</code>.
    * @param request never <code>null</code>.
    * @return maybe <code>null</code>.
    */
   protected String getFieldValue(Object[] params, IPSRequestContext request)
   {
      String fieldName = getFieldName(params, request);
      return request.getParameter(fieldName);
   }
   
   /**
    * The field name to check. Called by {@link #getFieldValue(Object[], IPSRequestContext)}.
    * <p>
    * It is ok to override this method.
    * 
    * @param params never <code>null</code>.
    * @param request never <code>null</code>.
    * @return never <code>null</code> or empty.
    */
   protected String getFieldName(Object[] params, IPSRequestContext request)
   {
      return getParameter(params, 0, DEFAULT_FIELD_NAME, true);
   }
   
   /**
    * Gets a parameter 
    * @param params never <code>null</code>.
    * @param i cannot be less than 0
    * @param defaultValue maybe <code>null</code>.
    * @param notBlank <code>true</code> will guarentee that return value is not an empty string or null.
    * @return maybe <code>null</code>
    */
   protected String getParameter(Object[] params, int i, String defaultValue, boolean notBlank) {
      boolean useParam = params != null && params.length > i  && params[i] instanceof String; 
      String rvalue = useParam ? (String) params[i] : defaultValue;
      String paramName = parameterNames.length > i ? parameterNames[i] : "" + i;
      if (notBlank)
         notEmpty(rvalue, "parameter: " + paramName + " cannot be empty or blank");
      return rvalue;
   }
   

   /* 
    * @see com.percussion.extension.IPSExtension#init(
    * com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public final void init( IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      String[] paramNames = (String[]) 
         IteratorUtils.toArray(def.getRuntimeParameterNames(), String.class);
      this.parameterNames = paramNames;
   }

}
