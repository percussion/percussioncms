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
package com.percussion.util;

import com.percussion.server.IPSRequestContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class to resolve different HTML parameter definitions. This is in
 * view of the fact that there are old and new names for the HTML parameters
 * Rhythmyx uses, e.g. contentid vs sys_contentid.
 */
public class PSHtmlParameters
{
   /**
    * This creates a new map of parameters with standard names out of the
    * supplied map. All parameters found without a known standard name will
    * be recreated like the original.
    *
    * @param params a map of HTML parameters from which this will create a
    *    map using standard parameter names. An empty map will be returned
    *    if <code>null</code> is provided.
    * @return a new map of HTML parameters using standard parameter names.
    *    Never <code>null</code>, might be empty.
    */
   public static Map createStandardParams(Map params)
   {
      Map standardParams = new HashMap();
      if (params != null)
      {
         Iterator entries = params.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry) entries.next();
            standardParams.put(toStandardName((String) entry.getKey()),
               entry.getValue());
         }
      }

      return standardParams;
   }

   /**
    * Get the parameter value from the supplied map for the provided name.
    * The name must be a standard name specified in IPSHtmlParameters.
    * If no parameter is found for the provided name old names are checked
    * and if still not found <code>null</code> will be returned.
    *
    * @param name the of the parameter we are interested in. Use a constant
    *    defined in IPSHtmlParameters. Migth be <code>null</code> or empty.
    * @param request the request to get the parameter from. Might be
    *    <code>null</code> or empty.
    * @return the HTML parameter value as String, <code>null</code> if not
    *    found, might be empty.
    */
   public static String get(String name, IPSRequestContext request)
   {
      if (request == null)
         return null;

      return get(name, request.getParameters());
   }

   /**
    * Get the parameter value from the supplied map for the provided name.
    * The name must be a standard name specified in IPSHtmlParameters.
    * If no parameter is found for the provided name old names are checked
    * and if still not found <code>null</code> will be returned.
    *
    * @param name the of the parameter we are interested in. Use a constant
    *    defined in IPSHtmlParameters. Migth be <code>null</code> or empty.
    * @param params a map of HTML parameters. Might be <code>null</code> or
    *    empty.
    * @return the HTML parameter value as String, <code>null</code> if not
    *    found, might be empty.
    */
   public static String get(String name, Map params)
   {
      if (name == null || params == null)
         return null;

      String value = (String) params.get(name);
      if (value == null)
         value = checkForOldNames(name, params);

      return value;
   }

   /**
    * Checks teh provided HTML parameter map for the old name(s) of the
    * supplied name.
    *
    * @param new name the name of the parameter we need the value for, assumed
    *    not <code>null</code>.
    * @param params the parameter map the get the value from for the supplied
    *    name, assumed not <code>null</code>.
    * @return old name of the HTML parameter.
    */
   private static String checkForOldNames(String name, Map params)
   {
      String value = null;
      for (int i=0; i<OLD_NAMES.length; i++)
      {
         String standard = OLD_NAMES[i][0];
         String old = OLD_NAMES[i][1];
         if (name.equals(standard))
         {
            value = (String) params.get(old);
            if (value != null)
               return value;
         }
      }

      return value;
   }

   /**
    * This converts the provided name to a standard parameter name.
    *
    * @param the name we need the standard name for. If no standard is
    *    defined the original name is returned. Assumed not <code>null</code>.
    * @return the standard parameter name, never <code>null</code>.
    */
   private static String toStandardName(String name)
   {
      for (int i=0; i<OLD_NAMES.length; i++)
      {
         String standard = OLD_NAMES[i][0];
         String old = OLD_NAMES[i][1];
         if (name.equals(old))
            return standard;
      }

      return name;
   }

   /**
    * Do not instantiate this class. Use the static functions instead.
    */
   private PSHtmlParameters()
   {
   }

   /**
    * An array of standard - old name HTML parameter pairs, element[0] is
    * defining the standard name, while element[1] defines an old name.
    */
   private static final String[][] OLD_NAMES =
   {
      { IPSHtmlParameters.SYS_CONTENTID, "contentid" },
      { IPSHtmlParameters.SYS_VARIANTID, "variantid" },
      { IPSHtmlParameters.SYS_CONTEXT, "context" },
      { IPSHtmlParameters.SYS_CONTEXT, "rxcontext" },
      { IPSHtmlParameters.SYS_AUTHTYPE, "authtype" },
      { IPSHtmlParameters.SYS_SITEID, "siteid" },
      { IPSHtmlParameters.SYS_SITEID, "destid" },
      { IPSHtmlParameters.SYS_SLOTID, "slotid" }
   };
}

