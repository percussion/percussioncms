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
package com.percussion.extensions.i18n;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This UDF returns the I18n resource lookup value string for the supplied
 * locale string and lookup sub keys. If locale string is not supplied it is
 * taken  from the user context inofrmation. This does not process the lookup
 * on its  own rather it uses PSI18nUtils.getString(String, String) to process
 * the lookup. It can take a minimum of two parameters, first parameter is the
 * locale or langauge string, if not supplied user's locale is assumed and the
 * othe parameters are the subkeys. At least one subkey must be non empty.
 *
 * The return value is i18n lookup value for the lookup key generated from the
 * supplied sub-kyes. Never <code>null</code> or <code>empty</code>.
 *
 * @see PSI18nUtils#getString
 */
public class PSLocalizedTextLookup
   extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */
   public Object processUdf(Object[] parm1, IPSRequestContext request)
      throws com.percussion.data.PSConversionException
   {
      //Make sure second parameter(lookup key) exists
      if(parm1 == null ||parm1.length < 2)
         throw new PSConversionException(0, "Text lookup key must not be empty");

      String lang = "";
      Object obj = parm1[0];
      if(obj != null)
         lang = obj.toString();
      //If language string is not supplied default to that from user context
      if(lang == null || lang.trim().length() < 1)
      {
         try
         {
            if(request != null)
            {
               lang = request.getUserContextInformation(
                  PSI18nUtils.USER_CONTEXT_VAR_SYS_LANG,
                  PSI18nUtils.DEFAULT_LANG).toString();
            }
         }
         catch(Exception e)
         {
            //Do nothing and use the empyt string -> default language
            request.printTraceMessage(e.getLocalizedMessage());
         }
      }
      List list = new ArrayList(Arrays.asList(parm1));
      //The first parameter is a language string and not one of the keys,
      //remove it
      list.remove(0);

      return PSI18nUtils.getString(PSI18nUtils.makeLookupKey(list), lang);
   }

   /*
    * main method for test purpose
    * @param args  not used
    */
   static public void main(String[] args)
   {
      PSLocalizedTextLookup o = new PSLocalizedTextLookup();
      String[] keys = {"en-us", "psx", "key1", "key2", "key3", "Content Title"};
      try
      {
         System.out.println(o.processUdf(keys, null).toString());
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
