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

import com.percussion.error.PSMissingBeanConfigurationException;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.data.PSCommunityRolesRole;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.security.data.PSCommunity</code> and 
 * <code>com.percussion.webservices.security.data.PSComunity</code>.
 */
public class PSCommunityConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSCommunityConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("id");
      m_specialProperties.add("roleAssociations");
      m_specialProperties.add("roles");
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
         com.percussion.webservices.security.data.PSCommunity orig = 
            (com.percussion.webservices.security.data.PSCommunity) value;
         
         PSCommunity dest = (PSCommunity) result;
         
         // convert id
         Long id = orig.getId();
         if (id != null)
            dest.setGUID(new PSDesignGuid(id));
         
         for (PSCommunityRolesRole role : orig.getRoles())
         {
            if (role != null)
               dest.addRoleAssociation(new PSDesignGuid(role.getId()));
         }
      }
      else
      {
         PSCommunity orig = (PSCommunity) value;
         
         com.percussion.webservices.security.data.PSCommunity dest = 
            (com.percussion.webservices.security.data.PSCommunity) result;
         
         // convert id
         IPSGuid guid = orig.getGUID();
         if (guid != null)
            dest.setId(new PSDesignGuid(guid).getValue());
         
         int i = 0;
         PSCommunityRolesRole[] roleAssociations = 
            new PSCommunityRolesRole[orig.getRoleAssociations().size()];
         for (IPSGuid roleId : orig.getRoleAssociations())
         {
            PSBackEndRole role = loadRole(roleId);
            if (role == null)
               throw new ConversionException(
                  "No role exists for id: " + roleId);
            
            roleAssociations[i++] = new PSCommunityRolesRole(
               new PSDesignGuid(roleId).getValue(), role.getName());
         }
         dest.setRoles(roleAssociations);
         
         i = 0;
      }
      
      return result;
   }

   /**
    * Load the role specified with role GUID. This is a separate method so that
    * the derived class may override it.
    * 
    * @param roleId must not be <code>null</code>.
    * @return the role object, may be <code>null</code> if one does not exist
    * corresponding to the given role id.
    * @throws PSMissingBeanConfigurationException
    */
   protected PSBackEndRole loadRole(IPSGuid roleId)
      throws PSMissingBeanConfigurationException
   {
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      PSBackEndRole[] roles = roleMgr.loadRoles(new IPSGuid[]
      {
         roleId
      });
      if(roles == null || roles.length==0)
      {
         return null;
      }
      return roles[0];
   }
}

