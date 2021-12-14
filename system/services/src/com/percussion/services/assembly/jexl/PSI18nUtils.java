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
