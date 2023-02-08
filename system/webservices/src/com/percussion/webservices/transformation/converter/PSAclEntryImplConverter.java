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

import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

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

