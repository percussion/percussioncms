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
package com.percussion.webservices.aop.security.impl;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.aop.security.IPSSecurityAopTestImplDesignWs;
import com.percussion.webservices.aop.security.data.PSMockDesignObject;

import java.util.List;

/**
 * Concreate implementation of the AOP test service interfaces. 
 */
public class PSSecurityAopTestImplDesignWs
   extends  PSSecurityAopTestImplBase
   implements IPSSecurityAopTestImplDesignWs
{
   @Override
   @SuppressWarnings(value={"unchecked"})
   public List<PSMockDesignObject> loadDesignObjects(List<IPSGuid> ids,
      boolean lock, boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      return super.loadDesignObjects(ids, lock, overrideLock, session, user);
   }

   @Override
   @SuppressWarnings(value={"unchecked"})
   public List<PSMockDesignObject> loadDesignObjects(String name, boolean lock,
      boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      return super.loadDesignObjects(name, lock, overrideLock, session, user);
   }

   @Override
   public String loadDesignObject(String name, boolean lock,
            boolean overrideLock, String session, String user)
            throws PSLockErrorException
   {
      return super.loadDesignObject(name, lock, overrideLock, session, user);
   }
   @Override
   public PSMockDesignObject loadDesignObject(boolean lock,
      boolean overrideLock, String session, String user)
      throws PSLockErrorException
   {
      return super.loadDesignObject(lock, overrideLock, session, user);
   }

   @Override
   public List<IPSCatalogSummary> findDesignObjects(String name)
   {
      return super.findDesignObjects(name);
   }

   public void saveDesignObject(String name, 
      @SuppressWarnings("unused")String session)
   {
      super.saveDesignObject(name);
   }

   public void saveDesignObjects(Object obj, boolean throwException, 
      @SuppressWarnings("unused")String session) throws PSErrorsException
   {
      super.saveDesignObjects(obj, throwException);
   }

   public void deleteDesignObject(String name, 
      @SuppressWarnings("unused")String session)
   {
      super.deleteDesignObject(name);
   }

   public void deleteDesignObjects(Object obj, boolean throwException, 
      @SuppressWarnings("unused")String session) throws PSErrorsException
   {
      super.deleteDesignObjects(obj, throwException);
   }
   
   @Override
   public List<IPSCatalogSummary> findDesignObjectsPerm(String name)
   {
      return findDesignObjects(name);
   }
}
