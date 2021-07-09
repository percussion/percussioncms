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
package com.percussion.extension;

import static org.apache.commons.lang.StringUtils.containsAny;
import static org.apache.commons.lang.Validate.notEmpty;

import java.io.File;

import org.apache.commons.collections.IteratorUtils;

import com.percussion.cms.IPSConstants;
import com.percussion.data.PSConversionException;
import com.percussion.server.IPSRequestContext;

/**
 * Field validation extension that checks if the 
 * given field has any of the given invalid characters.
 * <p>
 * <table border="1">
 * <tr><th>Param</th><th>Name</th><th>Description</th></tr>
 * <tr><td>0</td><td>fieldName</td><td>(String) If null <code>{@link #DEFAULT_FIELD_NAME}</code> is used.</td></tr>
 * <tr><td>1</td><td>invalidChars</td><td>(String) If null {@link IPSConstants#INVALID_ITEM_NAME_CHARACTERS} is used.</td></tr>
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
      return getParameter(params, 1, IPSConstants.INVALID_ITEM_NAME_CHARACTERS, false);
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
