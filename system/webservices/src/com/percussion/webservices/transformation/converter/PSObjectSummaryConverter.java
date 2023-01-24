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

import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts between {@link com.percussion.services.catalog.data.PSObjectSummary}
 * and {@link com.percussion.webservices.common.PSObjectSummary}
 */
public class PSObjectSummaryConverter extends PSConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * 
    * @param beanUtils
    */
   public PSObjectSummaryConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("permissions");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.common.PSObjectSummary orig = 
            (com.percussion.webservices.common.PSObjectSummary) value;
         PSObjectSummary dest = (PSObjectSummary) result;
         
         int[] perms = orig.getPermissions();
         if (perms != null)
         {
            Set<PSPermissions> permset = new HashSet<PSPermissions>();
            for (int permission : perms)
            {
               permset.add(PSPermissions.valueOf(permission));
            }
            PSUserAccessLevel accessLevel = new PSUserAccessLevel(permset);
            dest.setPermissions(accessLevel);
         }
      }
      else
      {
         PSObjectSummary orig = (PSObjectSummary) value;
         com.percussion.webservices.common.PSObjectSummary dest = 
            (com.percussion.webservices.common.PSObjectSummary) result;
         
         PSUserAccessLevel accessLevel = orig.getPermissions();

         Set<PSPermissions> permset = accessLevel.getPermissions();
         int[] perms = new int[permset.size()];
         int i = 0;
         for (PSPermissions permission : permset)
         {
            perms[i++] = permission.getOrdinal();
         }
         
         dest.setPermissions(perms);
      }
      
      return result;
   }   
}

