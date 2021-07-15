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
