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
package com.percussion.webservices.aop.security;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.aop.security.data.PSMockDesignObject;

import java.rmi.RemoteException;
import java.util.List;

/**
 * A mock webservice service manager to test the design method patterns for AOP 
 * based security processing.
 */
public interface IPSSecurityAopTestImplDesignWs
{
   /**
    * Return mock objects with the supplied guids.
    * 
    * @param ids The guids to use, may not be <code>null</code>.
    * @param lock <code>true</code> to lock the guids, <code>false</code>  
    * otherwise.
    * @param overrideLock used when creating locks
    * @param session used when creating locks
    * @param user used when creating locks
    * 
    * @return The list, never <code>null</code>.
    * 
    * @throws PSErrorResultsException if there are any errors.
    */
   public List<PSMockDesignObject> loadDesignObjects(List<IPSGuid> ids, 
      boolean lock, boolean overrideLock, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Return mock objects for all guids specified by the results of 
    * {@link PSSecurityAopTest#getTestAcls()}.
    * 
    * @param name Placeholder arg, may be <code>null</code> or empty.  If 
    * <code>null</code>, a error is automatically generated for a fake guid
    * with the 
    * {@link com.percussion.webservices.IPSWebserviceErrors#OBJECT_NOT_FOUND} 
    * code.  
    * @param lock <code>true</code> to lock the guids, <code>false</code>  
    * otherwise.
    * @param overrideLock used when creating locks
    * @param session used when creating locks
    * @param user used when creating locks
    * 
    * @return The list, never <code>null</code>.
    * 
    * @throws PSErrorResultsException if there are any errors.
    */
   public List<PSMockDesignObject> loadDesignObjects(String name, boolean lock, 
      boolean overrideLock, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Return a mock object for the first guids specified by the results of 
    * {@link PSSecurityAopTest#getTestAcls()}.
    * 
    * @param lock <code>true</code> to lock the guids, <code>false</code>  
    * otherwise.
    * @param overrideLock used when creating locks
    * @param session used when creating locks
    * @param user used when creating locks
    * 
    * @return The list, never <code>null</code>.
    * 
    * @throws PSLockErrorException if the lock fails.
    * @throws RemoteException if there are any other errors.
    */   
   public PSMockDesignObject loadDesignObject(boolean lock, 
      boolean overrideLock, String session, String user) 
   throws PSLockErrorException, RemoteException;
   
   /**
    * Return first guids specified by the results of 
    * {@link PSSecurityAopTest#getTestAcls()} as a string
    * 
    * @param name Placeholder arg, may be <code>null</code> or empty.
    * @param lock <code>true</code> to lock the guids, <code>false</code>  
    * otherwise.
    * @param overrideLock used when creating locks
    * @param session used when creating locks
    * @param user used when creating locks
    * 
    * @return The guid, never <code>null</code> or empty
    * 
    * @throws PSLockErrorException if the lock fails.
    * @throws RemoteException if there are any other errors.
    */   
   public String loadDesignObject(String name, boolean lock, 
      boolean overrideLock, String session, String user) 
   throws PSLockErrorException, RemoteException;
   
   /**
    * Returns object summaries for all design objects specified by 
    * {@link PSSecurityAopTest#getTestAcls()}.
    *  
    * @param name Placeholder arg, if <code>null</code>, a runtime exception is
    * thrown, otherwise the list is returned unmodified.
    * 
    * @return The list, never <code>null</code>.
    */
   public List<IPSCatalogSummary> findDesignObjects(String name);
   
   /**
    * A noop method used to test that design save methods are protected.
    * 
    * @param name Placeholder arg, should not be <code>null</code>.
    * @param session the current session, may not be <code>null</code>.
    * 
    * @throws PSLockErrorException 
    * @throws RemoteException 
    */
   public void saveDesignObject(String name, String session) 
      throws PSLockErrorException, RemoteException;
   
   /**
    * A noop method used to test that design save methods are protected.
    * 
    * @param obj object to save, not <code>null</code>.
    * @param throwException <code>true</code> to throw an 
    * {@link com.percussion.webservices.IPSWebserviceErrors#OBJECT_NOT_FOUND} 
    * for all objects supplied, <code>false</code> otherwise.
    * @param session the current session, may not be <code>null</code>.
    * 
    * @throws PSErrorsException if specified.    
    */
   public void saveDesignObjects(Object obj, boolean throwException, 
      String session) throws PSErrorsException;   

   /**
    * A noop method used to test that design delete methods are protected.
    * @param session the current session, may not be <code>null</code>.
    * 
    * @param name Placeholder arg, should not be <code>null</code>.
    */
   public void deleteDesignObject(String name, String session);
   
   /**
    * A noop method used to test that design delete methods are protected.
    * 
    * @param obj object to delete, not <code>null</code>. 
    * @param throwException <code>true</code> to throw an 
    * {@link com.percussion.webservices.IPSWebserviceErrors#OBJECT_NOT_FOUND} 
    * for all objects supplied, <code>false</code> otherwise.
    * @param session the current session, may not be <code>null</code>.
    * 
    * @throws PSErrorsException if specified.
    */
   public void deleteDesignObjects(Object obj, boolean throwException, 
      String session) throws PSErrorsException;
   
   /**
    * Same as {@link #findDesignObjects(String)} but with permission specified.
    *  
    * @param name Placeholder arg, if <code>null</code>, a runtime exception is
    * thrown, otherwise the list is returned unmodified.
    * 
    * @return The list, never <code>null</code>.
    */
   @IPSWsPermission(PSPermissions.DELETE)
   public List<IPSCatalogSummary> findDesignObjectsPerm(String name);    
}

