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

import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.guidmgr.data.PSDesignGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.content.data.PSKeyword</code> and 
 * <code>com.percussion.webservices.content.PSKeyword</code>.
 */
public class PSKeywordConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSKeywordConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("id");
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
         com.percussion.webservices.content.PSKeyword orig = 
            (com.percussion.webservices.content.PSKeyword) value;
         
         PSKeyword dest = (PSKeyword) result;

         dest.setGUID(new PSDesignGuid(orig.getId()));
         dest.setSequence(orig.getSequence());
      }
      else
      {
         PSKeyword orig = (PSKeyword) value;
         
         com.percussion.webservices.content.PSKeyword dest = 
            (com.percussion.webservices.content.PSKeyword) result;
         
         dest.setId(new PSDesignGuid(orig.getGUID()).getValue());
         dest.setSequence(orig.getSequence());
      }
      
      return result;
   }
}

