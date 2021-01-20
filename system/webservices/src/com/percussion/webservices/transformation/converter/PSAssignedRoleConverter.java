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

import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.webservices.system.PSAssignedRoleAdhocType;
import com.percussion.webservices.system.PSAssignedRoleAssignmentType;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Convert from {@link com.percussion.services.workflow.data.PSAssignedRole} to
 * {@link com.percussion.webservices.system.PSAssignedRole}
 */
public class PSAssignedRoleConverter extends PSConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSAssignedRoleConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("guid");
      m_specialProperties.add("adhocType");
      m_specialProperties.add("assignmentType");
      m_specialProperties.add("workflowId");
      m_specialProperties.add("stateId");
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
    
      PSAssignedRole src = (PSAssignedRole) value;
      com.percussion.webservices.system.PSAssignedRole tgt = 
         (com.percussion.webservices.system.PSAssignedRole) super.convert(
            type, value);
      
      tgt.setId(src.getGUID().longValue());
      tgt.setAdhocType(PSAssignedRoleAdhocType.fromString(
         src.getAdhocType().name().toLowerCase()));
      tgt.setAssignmentType(PSAssignedRoleAssignmentType.fromString(
         src.getAssignmentType().name().toLowerCase()));
      
      return tgt;
   }
}

