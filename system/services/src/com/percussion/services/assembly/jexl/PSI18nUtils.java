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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

/**
 * Jexl functions that tie into the I18n utils
 * 
 * @author dougrand
 */
public class PSI18nUtils extends PSJexlUtilBase
{
   /**
    * Lookup the text registered for the given key
    * @param key the key to look up
    * @param locale the locale to look up, may be <code>null</code>
    * @return the text
    */
   @IPSJexlMethod(description = "Lookup the text registered for the given key", params =
   {
         @IPSJexlParam(name = "key", description = "the key to look up"),
         @IPSJexlParam(name = "locale", description = "the locale to look up, may be null")})
   public String getString(String key, String locale)
   {
      return com.percussion.i18n.PSI18nUtils.getString(key, locale);
   }
}
