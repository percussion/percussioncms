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

import com.percussion.services.content.data.PSContentTypeSummaryChild;
import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts between
 * <code>com.percussion.services.content.data.PSContentTypeSummaryChild</code> and
 * <code>com.percussion.webservices.content.PSContentTypeSummaryChild</code>
 */
public class PSContentTypeSummaryChildConverter extends PSConverter
{

   /**
    * @param beanUtils
    */
   public PSContentTypeSummaryChildConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @SuppressWarnings("unchecked")
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSContentTypeSummaryChild orig =
            (com.percussion.webservices.content.PSContentTypeSummaryChild) value;
         PSContentTypeSummaryChild dest = new PSContentTypeSummaryChild();
         dest.setName(orig.getName());
         
         Converter converter = factory.getConverter(List.class);
         List<PSFieldDescription> target = (List<PSFieldDescription>) converter.convert(List.class, orig.getChildField());

         dest.setChildFields(target);
         return dest;
      }
      else
      {
         PSContentTypeSummaryChild orig = (PSContentTypeSummaryChild) value;
         com.percussion.webservices.content.PSContentTypeSummaryChild dest =
            new com.percussion.webservices.content.PSContentTypeSummaryChild();
         dest.setName(orig.getName());

         Class targetClass = com.percussion.webservices.content.PSFieldDescription[].class;
         Converter converter = factory.getConverter(targetClass);

         com.percussion.webservices.content.PSFieldDescription[] childFields = 
            (com.percussion.webservices.content.PSFieldDescription[]) converter.convert(targetClass, orig.getChildFields());
         
         dest.setChildField(childFields);
         return dest;
      }
   }

}

