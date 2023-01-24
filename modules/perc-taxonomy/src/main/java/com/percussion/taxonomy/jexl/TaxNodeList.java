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
package com.percussion.taxonomy.jexl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A List of TaxNode objects that provides methods for formatting the contained
 * information.
 * 
 * @author stephenbolton
 * 
 */
public class TaxNodeList extends ArrayList<TaxNode>
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Returns a List of values for a particular attribute name for each node
    * @param name The attribute name
    * @return the list of {@link TaxValues}
    */
   public List<TaxValues> getValuesByAttributeName(String name)
   {
      return getValuesByAttributeName(name, true);
   }

   /**  
    * Returns a List of values for a particular attribute name for each node 
    * @param name The attribute name
    * @param allowMulti do we throw an IllegalArgumentException for multi valued fields 
    * @return the list of {@link TaxValues}
    */
   public List<TaxValues> getValuesByAttributeName(String name, boolean allowMulti)
   {
      List<TaxValues> valueList = new ArrayList<>();
      for (TaxNode node : this)
      {
         TaxValues values = node.getAttributes().get(name);
         if (values != null)
         {
            if (!allowMulti && values.isMultiValued())
               throw new IllegalArgumentException("Cannot get DelimitedString for multi valued attributes");
            valueList.add(values);
         }
      }
      return valueList;
   }

   /** 
    * Get a list of values representing default name attribute for each node in the list
    * @return the List of {@link TaxValues}
    */
   public List<TaxValues> getValues()
   {
      List<TaxValues> valueList = new ArrayList<>();
      for (TaxNode node : this)
      {
         TaxValues taxValues = new TaxValues(node.getName());
         valueList.add(taxValues);
      }
      return valueList;
   }

   /**
    * Output a delimited string for a partiular named attribute  in the list of nodes
    * {@link IllegalArgumentException} will be thrown for a multi valued attribute.
    * @param name the attribute name
    * @param delim the delimiter to use
    * @return the delimiter separated String
    */
   public String getDelimitedString(String name, String delim)
   {
      List<TaxValues> valuesList = getValuesByAttributeName(name, false);
      return StringUtils.join(valuesList, delim);
   }

   /**
    * Output a delimited string for the default name attribute in the list of nodes
    * @param delim The delimiter
    * @return The comma separated string never <code>null</code>
    */
   public String getDelimitedString(String delim)
   {
      List<TaxValues> valuesList = getValues();
      return StringUtils.join(valuesList, delim);
   }

   /**
    * Output a comma delimited string for the default name attribute in the list of nodes.
    * @return The comma separated string never <code>null</code>
    */
   public String getDelimitedString()
   {
      List<TaxValues> valuesList = getValues();
      return StringUtils.join(valuesList, ",");
   }
}
