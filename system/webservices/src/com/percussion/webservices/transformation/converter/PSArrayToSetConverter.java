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

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;

public class PSArrayToSetConverter extends PSArrayConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSArrayToSetConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see Converter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   public Object convert(Class listType, Object value)
   {
      if (value == null)
         return null;
      
      if (listType == null)
         throw new IllegalArgumentException("listType cannot be null");
      
      if (!value.getClass().isArray())
         throw new IllegalArgumentException("value must be of type array");
      
      Class arrayType = Array.newInstance(Object.class, 0).getClass();
      Object[] convertedArray = (Object[]) super.convert(arrayType, value);
      return new HashSet(Arrays.asList(convertedArray));
   }
}

