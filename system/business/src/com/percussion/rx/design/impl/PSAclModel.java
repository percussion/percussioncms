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
package com.percussion.rx.design.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSSecurityException;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;

public class PSAclModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException("load(IPSGuid) is not currently "
            + "implemented for design objects of type " + getType().name());
   }
   
   @Override
   public Object load(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      IPSAclService service = (IPSAclService) getService();
      try
      {
         IPSGuid guid = new PSGuid(PSTypeEnum.ACL, Long.parseLong(name));
         return service.loadAcl(guid);
      }
      catch (PSSecurityException e)
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSAclService service = (IPSAclService) getService();
      try
      {
         IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
         guid = mgr.makeGuid(Long.parseLong(guid.getUUID() + ""), PSTypeEnum
               .valueOf(guid.getType()));

         service.deleteAcl(guid);
      }
      catch (PSSecurityException e)
      {
         String msg = "Failed to delete the design object for guid {0}";
         Object[] args = { guid };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }
}