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

