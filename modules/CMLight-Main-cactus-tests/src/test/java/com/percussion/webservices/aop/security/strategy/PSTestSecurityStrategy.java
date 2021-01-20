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
package com.percussion.webservices.aop.security.strategy;


import com.percussion.services.security.PSPermissions;

/**
 * A Test security strategy roughly equivalent to 
 * {@link PSFindSecurityStrategy}, but enforeces the delete permission.
 */
public class PSTestSecurityStrategy extends PSCustomSecurityStrategy
{
   @Override
   protected boolean acceptName(String name)
   {
      // any method this is on is fine
      if (name == null);
      
      return true;
   }

   @Override
   protected int getFilterArg()
   {
      return -1;
   }

   @Override
   protected PSPermissions getRequiredPermission()
   {
      return PSPermissions.DELETE;
   }

   @Override
   protected boolean shouldPostProcess()
   {
      return true;
   }

   @Override
   protected boolean shouldReturnResults()
   {
      return true;
   }
}

