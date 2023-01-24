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

            StringBuilder valueBuffer = new StringBuilder();
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

