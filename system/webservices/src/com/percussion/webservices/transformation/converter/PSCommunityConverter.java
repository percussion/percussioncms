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

import com.percussion.services.PSMissingBeanConfigurationException;
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

