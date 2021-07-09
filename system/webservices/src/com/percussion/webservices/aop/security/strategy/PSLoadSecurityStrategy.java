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
package com.percussion.webservices.aop.security.strategy;

import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.aop.security.IPSWsMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Implements a security strategy for load methods.  The strategy is as follows:
 * <ul>
 * <li>
 * Accepts method names that start with "load".  Determines a public vs design
 * call based on the interface name of the invoking class.  
 * </li>
 * <li>
 * If assumed to be a design call, tries to determine if loading the object(s)
 * locked by looking for a boolean method param.  The first one found is assumed
 * to be the "lock" param and it's value is checked.  If none is found, it is
 * assumed to be locking to be safe.  Filters the results using the READ 
 * permission if locked, UPDATE if unlocked.
 * </li> 
 * <li>
 * If the first argument is a guid, has a getGUID() method (case-insensitive),
 * or is a collection of either one of those, it filters the incoming values 
 * against the appropriate permission.  Currently only instances of 
 * {@link java.util.List} and {@link java.util.Set} are explicitly supported,
 * otherwise the concrete class must have an empty ctor.
 * </li>
 * <li>
 * If no guids are found in the first argument, attempts instead to filter the
 * results of the method call.   
 * </li>
 * <li>
 * For non-design methods, if a collection is returned, failed objects are 
 * removed from the results.  If a single object is returned, then a remote
 * exception is thrown.
 * </li>
 * <li>
 * For design methods, errors and results (if any) are appropriately thrown in a 
 * {@link PSErrorResultsException} if the signature allows this.  If a single 
 * object is returned, or if the method does not throw that exception then a 
 * {@link PSErrorException} is thrown if allowed, otherwise a runtime exception
 * is thrown.
 * </li>
 * </ul>
 * See the {@link com.percussion.webservices.aop.security.IPSWsMethod}, 
 * {@link com.percussion.webservices.aop.security.IPSWsStrategy}, and 
 * {@link com.percussion.webservices.aop.security.IPSWsPermission} annotations
 * for more information.
 */
public class PSLoadSecurityStrategy extends PSSecurityStrategy
{
   @Override
   public boolean accept(MethodInvocation invocation)
   {
      if (processAccept(invocation, "load"))
      {
         m_isDesign = isDesignService(invocation);
         m_isLock = m_isDesign && isLocking(invocation);
         
         m_perm = (m_isLock ? 
            PSPermissions.UPDATE: PSPermissions.READ);
         
         return true;
      }
      return false;
   }

   @Override
   public void preProcess()
   {
      logDebugMsg("preProcess by " + getClass().getName());
      m_failedGuids = filterArg(0, m_perm);
   }

   @Override
   public void postProcess(Object result) throws PSErrorResultsException, 
      PSErrorException, PSErrorsException
   {
      logDebugMsg("postProcess by " + getClass().getName());
      if (m_failedGuids != null)
      {
         if (m_failedGuids.isEmpty())
            return;
         
         // create exception and add errors and results
         handleException(m_failedGuids, result);
      }
      else
      {
         m_failedGuids = filterObject(result, m_perm);
         if (m_failedGuids != null)
         {
            if (m_failedGuids.isEmpty())
               return;
            
            if (!m_isDesign && result instanceof Collection)
            {
               return;
            }
            else if (m_isLock)
            {
               // unlock as required
               unlockFailedGuids(m_failedGuids);
            }

            // throw appropriate exception
            handleException(m_failedGuids, result);
         }
         else
         {
            handleBadSecurityConfig();
         }
      }
   }

   /**
    * Determine if the method invocation specifies locking.  This is done by
    * checking the first boolean param value found.  If none found, then it is
    * assumed we are locking to be save.
    * @param invocation The current invocation, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the method is locking, <code>false</code> if
    * not.
    */
   private boolean isLocking(MethodInvocation invocation)
   {
      Object[] params = invocation.getArguments();
      if (params != null)
      {
         for (Object param : params)
         {
            // assume first boolean is 
            if (param instanceof Boolean)
            {
               return ((Boolean)param).booleanValue();
            }
         }
      }
      
      // no matching param, assume lock
      return true;
   }

   /**
    * Attempts to unlock the list of supplied guids, checking the currently 
    * invoked method for the {@link IPSWsMethod#unlockOnError()} annotation.
    *  
    * @param guidMap The map of guids to errors to use for unlocking, assumed 
    * not <code>null</code>.  Value is <code>null</code> if it is a result, not
    * an error. 
    */
   private void unlockFailedGuids(Map<IPSGuid, PSErrorException> guidMap)
   {
      // check annotation
      IPSWsMethod ws = getInvocation().getMethod().getAnnotation(
         IPSWsMethod.class);
      if (ws != null && ws.unlockOnError() == false)
         return;
      
      IPSObjectLockService locksvc = 
         PSObjectLockServiceLocator.getLockingService();
      List<PSObjectLock> locks = new ArrayList<PSObjectLock>();
      for (Map.Entry<IPSGuid, PSErrorException> entry : guidMap.entrySet())
      {
         if (entry.getValue() == null)
            continue;
         
         PSObjectLock lock = locksvc.findLockByObjectId(entry.getKey());
         if (lock != null)
         {
            locks.add(lock);
         }
      }
      
      if (!locks.isEmpty())
         locksvc.releaseLocks(locks);
   }

   @Override
   public void processException(Exception e)
   {
      processErrorException(e, m_perm);
   }

   /**
    * <code>true</code> if this is handling a design service invocation, 
    * <code>false</code> if it is a public call.  Set during call to 
    * {@link #accept(MethodInvocation)} if it returns <code>true</code>, not
    * modified after that.
    */
   private boolean m_isDesign;
   
   /**
    * <code>true</code> if the invocation will lock the loaded objects,
    * <code>false</code> if not. Set during call to
    * {@link #accept(MethodInvocation)} if it returns <code>true</code>, not
    * modified after that.
    */
   private boolean m_isLock = false;

   /**
    * The permission required for the method invocation. Set during call to
    * {@link #accept(MethodInvocation)} if it returns <code>true</code>, not
    * modified after that.
    */
   PSPermissions m_perm;
}

