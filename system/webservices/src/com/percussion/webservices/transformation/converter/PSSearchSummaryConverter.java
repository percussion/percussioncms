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

