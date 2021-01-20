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

import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts between {@link com.percussion.services.security.data.PSAclEntryImpl}
 * and {@link com.percussion.webservices.system.data.PSAclEntryImpl}
 */
public class PSAclEntryImplConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * @param beanUtils
    */
   public PSAclEntryImplConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      m_specialProperties.add("permissions");
      m_specialProperties.add("isNegative");
      m_specialProperties.add("owner");
      m_specialProperties.add("type");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.system.PSAclEntryImpl src = 
            (com.percussion.webservices.system.PSAclEntryImpl) value;
         PSAclEntryImpl dest = (PSAclEntryImpl) result;
         
         dest.setType(PrincipalTypes.valueOf(src.getType()));
         for (com.percussion.webservices.system.PSAccessLevelImpl level : 
            src.getPermissions())
         {
            PSAccessLevelImpl tgtlevel = new PSAccessLevelImpl();
            tgtlevel.setPermission(
               PSPermissions.valueOf(level.getPermission()));
            tgtlevel.setAclEntryId(level.getAclEntryId());
            dest.addPermission(tgtlevel);
         }
      }
      else
      {
         PSAclEntryImpl src = (PSAclEntryImpl) value;
         com.percussion.webservices.system.PSAclEntryImpl dest = 
            (com.percussion.webservices.system.PSAclEntryImpl) result;
         
         dest.setType(src.getType().getOrdinal());
         Collection<PSAccessLevelImpl> permset = src.getPermissions();
         List<com.percussion.webservices.system.PSAccessLevelImpl> 
            permissions = new ArrayList<
               com.percussion.webservices.system.PSAccessLevelImpl>(
                  permset.size());
         for (PSAccessLevelImpl level : permset)
         {
            com.percussion.webservices.system.PSAccessLevelImpl tgtlevel =
               new com.percussion.webservices.system.PSAccessLevelImpl();
            tgtlevel.setId(level.getId());
            tgtlevel.setAclEntryId(level.getAclEntryId());
            tgtlevel.setPermission(level.getPermission().getOrdinal());
            permissions.add(tgtlevel);
         }
         dest.setPermissions(permissions.toArray(
            new com.percussion.webservices.system.
               PSAccessLevelImpl[permissions.size()]));
      }
      
      return result;
   }
}

