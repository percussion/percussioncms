/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
