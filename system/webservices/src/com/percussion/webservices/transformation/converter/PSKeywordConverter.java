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

