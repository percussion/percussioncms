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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.webservices.content.PSSearchResultsFields;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.services.content.data.PSSearchSummary} and
 * {@link com.percussion.webservices.content.PSSearchResults}
 */
public class PSSearchSummaryConverter extends PSItemSummaryConverter
{
   /**
    * @see PSConverter#PSConverter(BeanUtilsBean)
    */
   public PSSearchSummaryConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      try
      {
         if (isClientToServer(value))
         {
            com.percussion.webservices.content.PSSearchResults orig = 
               (com.percussion.webservices.content.PSSearchResults) value;
            
            PSSearchSummary dest =  new PSSearchSummary();
            toServer(orig, dest);
            
            Map<String, String> fields = new HashMap<String, String>();
            for (PSSearchResultsFields field : orig.getFields())
               fields.put(field.getName(), field.get_value());
            dest.setFields(fields);

            return dest;
         }
         else
         {
            PSSearchSummary orig = (PSSearchSummary) value;
            
            com.percussion.webservices.content.PSSearchResults dest = 
               new com.percussion.webservices.content.PSSearchResults();
            toClient(orig, dest);
            
            Map<String, String> sourceFields = orig.getFields();
            PSSearchResultsFields[] fields = 
               new PSSearchResultsFields[sourceFields.size()];
            int index = 0;
            for (String name : orig.getFields().keySet())
            {
               PSSearchResultsFields field = new PSSearchResultsFields();
               field.setName(name);
               field.set_value(sourceFields.get(name));
               
               fields[index++] = field;
            }
            dest.setFields(fields);
            
            return dest;
         }
      }
      finally
      {
      }
   }
}

