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
