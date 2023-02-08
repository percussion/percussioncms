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
package com.percussion.extensions.i18n;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;

import java.util.Date;

/**
 * This UDF converts a a given date object to given format ans user's locale.
 * The locale is taken from the user context information.
 *
 * The first parameter is the date to format. This can be a java.util.Date
 * object or a date string. If it is <code>null</code> or <code>empty</code>,
 * current date is assumed.
 *
 * Second parameter is the required output date pattern.
 *
 * The return value is the formatted date string. Never <code>null</code>.
 *
 * @see com.percussion.i18n.PSI18nUtils#formatDate
 * @see com.percussion.i18n.PSI18nUtils#getLocaleFromString
 * @see java.util.Date
 */
public class PSLocalizeDateToUser
   extends PSSimpleJavaUdfExtension
{
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws com.percussion.data.PSConversionException
   {
      Object date = params[0];
      if(date == null || date.toString().length() < 1)
      {
         date = new Date();
      }

      String pattern = "";
      Object obj = null;
      if(params.length > 1)
      {
         obj = params[1];
         if(obj != null)
            pattern = obj.toString();
      }
      String result = null;
      try
      {
         String lang = request.getUserContextInformation(
            PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG,
            PSI18nUtils.DEFAULT_LANG).toString();
         if(date instanceof java.util.Date)
         {
            result = PSI18nUtils.formatDate(
               (Date)date, pattern, PSI18nUtils.getLocaleFromString(lang));
         }
         else
         {
            result = PSI18nUtils.formatDate(
               date.toString(), null, null, pattern, lang);
         }
      }
      catch(Exception e)
      {
         int errCode = 0;
         Object[] args = { e.toString(), "PSLocalizeDateToUser/processUdf" };
         throw new PSConversionException(errCode, args);
      }
      return result;
   }
}
