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

import com.percussion.services.workflow.data.PSTransition;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.system.PSTransitionComment;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Convert from {@link com.percussion.services.workflow.data.PSTransition} to
 * {@link com.percussion.webservices.system.PSTransition}
 */
public class PSTransitionConverter extends PSTransitionBaseConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSTransitionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      m_specialProperties.add("requiresComment"); 
      m_specialProperties.add("roles"); // handled by workflow conv
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         // only reading from server is supported
         throw new ConversionException(
            "Conversion not supported from client to server");
      }
      
      PSTransition src = (PSTransition) value; 
      com.percussion.webservices.system.PSTransition tgt = 
         (com.percussion.webservices.system.PSTransition) super.convert(
            type, value);
      tgt.setComment(PSTransitionComment.fromString(PSStringUtils.toCamelCase(
         src.getRequiresComment().name())));
      
      return tgt;
   }   
}

