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

import com.percussion.services.content.data.PSKeywordChoice;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.content.data.PSKeywordChoice</code> and 
 * <code>com.percussion.webservices.content.PSKeywordChoice</code>.
 */
public class PSKeywordChoiceConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSKeywordChoiceConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("sequence");
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSKeywordChoice orig = 
            (com.percussion.webservices.content.PSKeywordChoice) value;
         
         PSKeywordChoice dest = (PSKeywordChoice) result;

         dest.setSequence(orig.getSequence());
      }
      else
      {
         PSKeywordChoice orig = (PSKeywordChoice) value;
         
         com.percussion.webservices.content.PSKeywordChoice dest = 
            (com.percussion.webservices.content.PSKeywordChoice) result;
         
         dest.setSequence(orig.getSequence());
      }
      
      return result;
   }
}

