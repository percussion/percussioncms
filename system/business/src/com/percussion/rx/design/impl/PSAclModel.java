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
