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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

