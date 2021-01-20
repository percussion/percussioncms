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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;

import java.util.HashMap;
import java.util.Iterator;

/**
 * The PSUserAttributes class is a hashMap of user attributes, as
 * defined by the security provider using the attributes.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSUserAttributes extends HashMap
{
   /**
    * Construct an empty set of attributes.
    */
   public PSUserAttributes()
   {
   }

   /**
    * Constructs a new user attributes hash map from the supplied attribute
    * list.
    *
    * @param attributes the attribute list to construct this map from, may
    *    be <code>null</code>. Attributes that have multiple values will be
    *    stored as coma separated <code>String</code> objects.
    */
   public PSUserAttributes(PSAttributeList attributes)
   {
      if (attributes != null)
      {
         for (int i=0; i<attributes.size(); i++)
         {
            PSAttribute attribute = (PSAttribute) attributes.get(i);

            StringBuffer valueBuffer = new StringBuffer();
            Iterator values = attribute.getValues().iterator();
            while (values.hasNext())
            {
               String value = (String) values.next();
               valueBuffer.append(value);

               if (values.hasNext())
                  valueBuffer.append(",");
            }
            
            put(attribute.getName(), valueBuffer.toString());
         }
      }
   }

   /**
    * Get an attribute as a String (avoid type casting get results).
    *
    * @param      key      the attribute to get (from the hash)
    *
    * @return               the attribute value, or <code>null</code> if
    *                        the attribute does not exist
    */
   public String getString(String key)
   {
      return (String) super.get((Object)key);
   }
}

