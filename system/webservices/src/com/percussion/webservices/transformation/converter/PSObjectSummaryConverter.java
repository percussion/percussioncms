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

