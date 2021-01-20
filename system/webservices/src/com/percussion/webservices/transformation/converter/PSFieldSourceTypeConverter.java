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

import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.webservices.content.PSFieldSourceType;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link Integer} and 
 * {@link com.percussion.webservices.content.PSFieldSourceType}.
 */
public class PSFieldSourceTypeConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSFieldSourceTypeConverter(BeanUtilsBean beanUtils)
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
      
      if (isClientToServer(value))
      {
         PSFieldSourceType orig = (PSFieldSourceType) value;
         
         Integer dest = 0;
         //FB:EC_UNRELATED_TYPES NC 1-17-16
         if (orig.getValue().equals(PSFieldSourceType.local.getValue()))
            dest = PSItemFieldMeta.SOURCE_TYPE_LOCAL;
         else if (orig.getValue().equals(PSFieldSourceType.shared.getValue()))
            dest = PSItemFieldMeta.SOURCE_TYPE_SHARE;
         else if (orig.getValue().equals(PSFieldSourceType.system.getValue()))
            dest = PSItemFieldMeta.SOURCE_TYPE_SYSTEM;
         else
            dest = PSItemFieldMeta.SOURCE_TYPE_UNKNOWN;
         
         return dest;
      }
      else
      {
         Integer orig = (Integer) value;
         
         switch (orig)
         {
            case PSItemFieldMeta.SOURCE_TYPE_LOCAL:
               return PSFieldSourceType.local;
            case PSItemFieldMeta.SOURCE_TYPE_SHARE:
               return PSFieldSourceType.shared;
            case PSItemFieldMeta.SOURCE_TYPE_SYSTEM:
               return PSFieldSourceType.system;
            default:
               return PSFieldSourceType.unknown;
         }
      }
   }
}
