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

import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PSCommunityModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSBackEndRoleMgr roleMgr = (IPSBackEndRoleMgr) getService();
      try
      {
         return roleMgr.loadCommunity(guid);
      }
      catch (PSSecurityException e)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }

   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSBackEndRoleMgr roleMgr = (IPSBackEndRoleMgr) getService();
      try
      {
         roleMgr.deleteCommunity(guid);
      }
      catch (Exception e)
      {
         String msg = "Failed to delete the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }      
   }
   
   @Override
   public void delete(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
      IPSBackEndRoleMgr roleMgr = (IPSBackEndRoleMgr) getService();
      try
      {
         List<PSCommunity> comms = roleMgr.findCommunitiesByName(name);
         if(!comms.isEmpty())
         {
            delete(comms.get(0).getGUID());
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to delete the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }      
      
   }
   
   @Override
   public String guidToName(IPSGuid guid)
   {
      PSCommunity community = (PSCommunity) load(guid);
      return community.getName();
   }
}
