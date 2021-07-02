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

import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.aop.security.IPSWsPermission;
import com.percussion.webservices.aop.security.IPSWsStrategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for all classes implementing a specific security stragegy for 
 * a class of method calls.  Basic sequence of calls to a strategy is:
 * <ol>
 * <li>{@link #accept(MethodInvocation)} - determine if a particular strategy
 * will handle the current invocation.  If <code>false</code> is returned, 
 * then no further calls are made to that strategy during the processing of that 
 * invocation.</li>
 * <li>{@link #preProcess()} - Filter any incoming guids from the method
 * parameters, with the oportunity to cause the invocation of the method to be
 * skipped.</li>
 * <li>{@link #postProcess(Object)} - Called after the method is invoked or 
 * after proceed if the method invocation is skipped, allows the strategy to 
 * throw any resulting exceptions or filter the results of the method 
 * invocation.</li>
 * <li>{@link #processException(Exception)} - called instead of 
 * {@link #postProcess(Object)} if the method invocation threw an exception,
 * allows the strategy to modify the exception or to throw a different one.</li> 
 * </ol>
 * 
 * All strategy sub-classes must be registered in the static initializer.
 * See the {@link com.percussion.webservices.aop.security.IPSWsMethod}, 
 * {@link com.percussion.webservices.aop.security.IPSWsStrategy}, and 
 * {@link com.percussion.webservices.aop.security.IPSWsPermission} annotations
 * for more information.
 */
public abstract class PSSecurityStrategy
{
   /**
    * Construct a strategy.  Protected so that only the 
    * {@link #getStrategy(MethodInvocation)} factory method can be used.
    */
   protected PSSecurityStrategy()
   {
   }
   
   /**
    * Factory method, returns the first defined strategy for which 
    * {@link #accept(MethodInvocation)} returns <code>true</code>.
    *  
    * @param invocation The method invocation, may not be <code>null</code>.
    * 
    * @return The strategy, or <code>null</code> if no strategy has been
    * defined that will handle the supplied invocation.
    */
   public static PSSecurityStrategy getStrategy(MethodInvocation invocation)
   {
      if (invocation == null)
         throw new IllegalArgumentException("invocation may not be null");
      
      PSSecurityStrategy strategy = null;
      
      // check for custom annotation
      IPSWsStrategy wsStrat = invocation.getMethod().getAnnotation(
         IPSWsStrategy.class);
      if (wsStrat != null)
      {
         Class<? extends PSSecurityStrategy> custClass = wsStrat.value(); 
         try
         {
            strategy = custClass.newInstance();
            
            if (strategy.accept(invocation))
            {
               strategy.setInvocation(invocation);
               return strategy;
            }
            return null;
         }
         catch (Exception e)
         {
            throw new RuntimeException(
               "Error constructing custom strategy class " + custClass.getName()
               + ": " + e.getLocalizedMessage(), e);
         }
      }
      
      for (Class<? extends PSSecurityStrategy> strategyClass : 
         ms_stategyClassList)
      {
         PSSecurityStrategy test;
         try
         {
            test =  strategyClass.newInstance();
         }
         catch (Exception e)
         {
            // a bug
            String msg = "Failed to create security strategy";
            LogManager.getLogger(PSSecurityStrategy.class).error(msg, e);
            throw new RuntimeException(msg, e);
         }
         
         if (test.accept(invocation))
         {
            strategy = test;
            strategy.setInvocation(invocation);
            break;
         }
      }
      
      return strategy;
   }

   
   /**
    * Determine if this strategy will accept processing of the supplied 
    * invocation.
    * 
    * @param invocation The invocation to process, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it will be processed, <code>false</code> if
    * not.
    */
   protected abstract boolean accept(MethodInvocation invocation);
   
   /**
    * Called before the method is invoked, provides the oportunity to filter any 
    * incoming guids from the method parameters.  Call 
    * {@link #setShouldProceed(boolean)} if the filtering results in no reason
    * to call the method's invocation.  Exceptions should not be thrown from
    * this method for security violations, and instead pending errors should be 
    * held until {@link #postProcess(Object)} or 
    * {@link #processException(Exception)} are called and thrown at that time. 
    */
   public abstract void preProcess();
   
   /**
    * Called after the method is invoked.  If input parameters were filtered,
    * the results from the method invocation plus any security exceptions can be
    * thrown as an {@link PSErrorResultsException}.  Otherwise filtering can be
    * done on the results.  
    * 
    * @param result The return value of the method invocation, may be 
    * <code>null</code> if didn't proceed or if the method returns void.
    * 
    * @throws PSErrorResultsException If a list of security violations must be
    * handled and the method allows this exception to be thrown.
    * @throws PSErrorsException If a list of security violations must be
    * handled and the method allows this exception to be thrown. 
    * @throws PSErrorException If a single return value is expected, but
    * a security violation has occurred.
    */
   public abstract void postProcess(Object result) 
      throws PSErrorResultsException, PSErrorsException, PSErrorException;
   
   /**
    * Called if the method invocation throws an exception.  Strategy can modify
    * the exception, or throw a different one (only runtime exceptions may be
    * thrown).
    * 
    * @param e The exception, will be rethrown after this method returns unless
    * this method throws a different exception.
    */
   public abstract void processException(Exception e);
   
   /**
    * Determine if proceed() should be called on the method invocation after
    * {@link #preProcess()} has been called.  
    * 
    * @return <code>true</code> if proceed() should be called, 
    * <code>false</code> if pre-processing has resulted in no valid input. 
    */
   public boolean shouldProceed()
   {
      if (!m_proceed)
         logDebugMsg("skipping proceed");
      return m_proceed;
   }
   
   /**
    * Set if the method invocation should proceed, called from 
    * {@link #preProcess()}.  See {@link #shouldProceed()} for more info.
    * 
    * @param proceed <code>true</code> to proceed, <code>false</code> to skip
    * method invocation.
    */
   protected void setShouldProceed(boolean proceed)
   {
      m_proceed = proceed;
   }
   
   /**
    * Set the current method invocation.
    * 
    * @param invocation The invocation, assumed not <code>null</code>.
    */
   private void setInvocation(MethodInvocation invocation)
   {
      m_invocation = invocation;
   }

   /**
    * Get the current method invocation.
    * 
    * @return The invocation, not <code>null</code> after
    * {@link #accept(MethodInvocation)} is called and returns a 
    * <code>true</code> value.  
    */
   public MethodInvocation getInvocation()
   {
      return m_invocation;
   }

   /**
    * Default accept processing, checks that the name of the method being 
    * invoked matches the supplied prefix.  If accepted, logs a default message
    * to that effect.
    * 
    * @param invocation The invocation being processed, may not be 
    * <code>null</code>.
    * @param prefix The prefix to match, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it is accepted, <code>false</code> if not.
    */
   protected boolean processAccept(MethodInvocation invocation, String prefix)
   {
      if (invocation.getMethod().getName().startsWith(prefix))
      {
         logAccept(invocation);
         
         return true;
      }
      return false;
   }
   
   /**
    * Get the permission specified by the {@link IPSWsPermission} annotation on
    * the current method, if any.
    * 
    * @param defaultPerm The permission to use if one is not specified by the
    * method, may not be <code>null</code>.
    * 
    * @return The permission to use, never <code>null</code>.
    */
   private PSPermissions getRequiredPermission(PSPermissions defaultPerm)
   {
      if (defaultPerm == null)
         throw new IllegalArgumentException("defaultPerm may not be null");
      
      IPSWsPermission perm = m_invocation.getMethod().getAnnotation(
         IPSWsPermission.class);
      if (perm != null)
         return perm.value();
      return defaultPerm;
   }
   
   /**
    * Obtains the value of the specified argument and calls 
    * {@link #filterObject(Object, PSPermissions)}.  If the arg
    * does not exist, the method simply returns.  If the arg is a collection,
    * the argument is replaced with a copy to avoid modifying the original 
    * collection passed by the caller.
    * 
    * @param argIndex The index of the argument to obtain.
    * @param perm The permission to use to filter the arguments, may not be
    * <code>null</code>.
    * 
    * @return The resulting map returned by 
    * {@link #filterObject(Object, PSPermissions)}.
    */
   @SuppressWarnings(value={"unchecked"})
   protected Map<IPSGuid, PSErrorException> filterArg(int argIndex, 
      PSPermissions perm)
   {
      Object[] args = getInvocation().getArguments();
      if (args == null || argIndex >= args.length)
         return null;
      Object arg = args[argIndex];
      if (arg == null)
         return null;
      
      // copy arg if a collection since it will be modified
      if (arg instanceof Collection)
      {
         Collection coll = (Collection) arg;
         try
         {
            Collection newarg;
            if (arg instanceof List)
               newarg = new ArrayList<Object>();
            else if (arg instanceof Set)
               newarg = new HashSet();
            else
               newarg = (Collection) arg.getClass().newInstance();
            
            newarg.addAll(coll);
            arg = newarg;
            args[argIndex] = arg;
         }
         catch (Exception e)
         {
            throw new RuntimeException(
               "Could not copy the collection arg for filtering", e);
         }
      }
      
      Map<IPSGuid, PSErrorException> result = filterObject(arg, perm);
      
      return result;
   }

   /**
    * Filters the specified object against the specified security permission.
    * Filter will be done if the object is an instance of {@link IPSGuid}, has
    * a <code>getGUID()</code> method, or is a {@link Collection} of such
    * objects. Each guid obtained is checked with the current user against the
    * specified permission. If the user does not have that permission, the guid
    * or object is removed from the collection if one is supplied. If a single
    * object is supplied and it fails the check, or if the resulting collection
    * is empty, {@link #shouldProceed()} is set to <code>false</code>.
    * 
    * @param obj The object to filter, may be <code>null</code> in which case
    * the method simply returns.
    * @param perm The permission to check, may not be <code>null</code>.
    * 
    * @return A Map of failed guids to the matching exception, <code>null</code>
    * if no guid(s) could be extracted from the object, otherwise may be empty
    * if no guid(s) failed.  If failures occur, successful guids are also added 
    * with a <code>null</code> value.
    */
   @SuppressWarnings(value={"unchecked"})
   protected Map<IPSGuid, PSErrorException> filterObject(Object obj, 
      PSPermissions perm)
   {
      if (perm == null)
         throw new IllegalArgumentException("perm may not be null");
      
      if (obj == null)
         return null;
      
      // check for annotation override
      perm = getRequiredPermission(perm);
      
      Map<IPSGuid, PSErrorException> failedGuids = new HashMap<IPSGuid, 
         PSErrorException>();

      if (obj instanceof IPSGuid)
      {
         if (checkGuid((IPSGuid)obj, perm, obj, failedGuids))
         {
            m_proceed = false;
         }
      }
      else if (obj instanceof Collection)
      {
         boolean didFilter = false;
         Collection collection = (Collection)obj;
         if (collection.isEmpty())
            return failedGuids;
         List<IPSGuid> resultList = new ArrayList<IPSGuid>();
         Iterator iterator = collection.iterator();
         
         List<IPSGuid> ids = new ArrayList<IPSGuid>();
         for (Object o : collection)
         {
            if (o instanceof IPSGuid)
               ids.add((IPSGuid) o);
            else
            {
               IPSGuid id = extractGuid(o);
               if (id != null)
                  ids.add(id);
            }
            if (o != null && ids.isEmpty())
               return null;
         }
         
         //loading the acls individually is very slow, so batch load them
         IPSAclService aclService = PSAclServiceLocator.getAclService();
         List<IPSAcl> acls = aclService.loadAclsForObjects(ids);
         //convert to a map for later use
         Map<IPSGuid, IPSAcl> guidToAcl = new HashMap<IPSGuid, IPSAcl>();
         for (IPSAcl acl : acls)
         {
            if (acl != null)
               guidToAcl.put(acl.getObjectGuid(), acl);
         }
         
         IPSGuid guid = null;
         while (iterator.hasNext())
         {
            Object collObj = iterator.next();
            if (collObj instanceof IPSGuid)
            {
               guid = (IPSGuid)collObj;
               if (checkGuidWithAcl(guidToAcl.get(guid), guid, perm, collObj,
                     failedGuids))
               {
                  didFilter = true;
                  iterator.remove();
               }
               else
                  resultList.add(guid);
            }
            else
            {
               guid = extractGuid(collObj);
               if (guid != null)
               {
                  if (checkGuidWithAcl(guidToAcl.get(guid), guid, perm,
                        collObj, failedGuids))
                  {
                     didFilter = true;
                     iterator.remove();
                  }
                  else
                     resultList.add(guid);
               }
            }
         }
         
         // if we emptied the collection, don't proceed
         if (didFilter && collection.isEmpty())
            m_proceed = false;
         else if (didFilter)
         {
            for (IPSGuid result : resultList)
            {
               failedGuids.put(result, null);
            }
         }
      }
      else
      {
         IPSGuid guid = extractGuid(obj);
         if (guid != null)
         {
            if (checkGuid(guid, perm, obj, failedGuids))
               m_proceed = false;
         }
         else
         {
            return null;
         }
      }
      
      return failedGuids;
   }
   
   /**
    * Convenience method that loads the ACL for <code>guid</code> and then
    * calls
    * {@link #checkGuidWithAcl(IPSAcl, IPSGuid, PSPermissions, Object, Map)}.
    */
   private boolean checkGuid(IPSGuid guid, PSPermissions perm, Object obj,
         Map<IPSGuid, PSErrorException> failedGuids)
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      IPSAcl acl = aclService.loadAclForObject(guid);
      return checkGuidWithAcl(acl, guid, perm, obj, failedGuids);
   }
   
   /**
    * Check if the current user has the specified permission for the supplied
    * guid.
    * 
    * @param acl Assumed to be the ACL for the object referenced by
    * <code>guid</code>. May be <code>null</code>.
    * @param guid The guid to check against, assumed not <code>null</code>.
    * @param perm The permission to match, assumed not <code>null</code>.
    * @param obj The object being checked, assumed not <code>null</code>. If
    * an instance of {@link PSObjectSummary}, then the user access level is set
    * on the object if it does not fail.
    * @param failedGuids Map to which an entry is added if the check fails.
    * 
    * @return <code>true</code> if the check failed and an entry was added to
    * the map, <code>false</code> if the user had the specified permission.
    */
   private boolean checkGuidWithAcl(IPSAcl acl, IPSGuid guid,
         PSPermissions perm, Object obj,
         Map<IPSGuid, PSErrorException> failedGuids)
   {
      PSUserAccessLevel accessLevel = null;
      PSObjectSummary sum = null;
      
      if (obj instanceof PSObjectSummary)
      {
         sum = (PSObjectSummary) obj;
         if (sum.arePermissionsValid())
            accessLevel = sum.getPermissions();
      } 
      
      if (accessLevel == null)
      {
         IPSAclService aclService = PSAclServiceLocator.getAclService();
         accessLevel = aclService.calculateUserAccessLevel(acl);
      }
      
      boolean isFailed = false;
      if (!hasAccess(perm, accessLevel))
      {
         PSDesignGuid dguid = new PSDesignGuid(guid);
         int code = IPSWebserviceErrors.ACCESS_CONTROL_ERROR;
         PSErrorException error = new PSErrorException(code, 
            PSWebserviceErrors.createErrorMessage(code, 
               dguid.toString(), perm), 
               ExceptionUtils.getFullStackTrace(new Exception()));
         failedGuids.put(guid, error);
         isFailed = true;
      }
      else if (sum != null && !sum.arePermissionsValid())
      {
         sum.setPermissions(accessLevel);
      }
      
      return isFailed;
   }

   /**
    * Called during filtering to determine if the user's access level allows the
    * specified permission. Base class simply checks to see if the supplied user
    * access level contains the specified permission. Derived classes can
    * override this to provide more complex rules based on the current call and
    * permission being checked.
    * 
    * @param perm The permission required by the current strategy for the
    * current operation, never <code>null</code>.
    * @param accessLevel The current user's access level for the object being
    * filtered for the current operation, never <code>null</code>.
    * 
    * @return <code>true</code> if the user can be considered to have access
    * based on the specified permission, <code>false</code> if not.
    */
   protected boolean hasAccess(PSPermissions perm, 
      PSUserAccessLevel accessLevel)
   {
      return accessLevel.getPermissions().contains(perm);
   }

   /**
    * Attempts to find an accessor method for a guid on the supplied object and
    * then invoke it.
    * 
    * @param obj The object to check, may be <code>null</code>.
    * 
    * @return The extracted guid, or <code>null</code> if none found.
    */
   protected IPSGuid extractGuid(Object obj)
   {
      if (obj == null)
         return null;
      
      IPSGuid guid = null;
      
      Method method = null;
      for (Method test : obj.getClass().getMethods())
      {
         if (test.getName().equalsIgnoreCase("getguid") && 
            test.getParameterTypes().length == 0)
         {
            method = test;
            break;
         }
      }
      
      if (method != null)
      {
         try
         {
            guid = (IPSGuid) method.invoke(obj, new Object[0]);
         }
         catch (InvocationTargetException e)
         {
            // should never happen, ignore
         }
         catch (IllegalAccessException e)
         {
            // should never happen, ignore
         }
      }
      return guid;
   }
   
   /**
    * Log the supplied debug message.
    * 
    * @param msg The message may not be <code>null</code> or empty.
    */
   protected void logDebugMsg(String msg)
   {
      if (StringUtils.isBlank(msg))
         throw new IllegalArgumentException("msg may not be null or empty");
      
      m_log.debug(m_invocation.getMethod().getName() + ": " + msg);
   }
   

   /**
    * Logs an accept message for the supplied invocation.
    * 
    * @param invocation The invocation being accepted, may not be 
    * <code>null</code>.
    */
   protected void logAccept(MethodInvocation invocation)
   {
      if (invocation == null)
         throw new IllegalArgumentException("invocation may not be null");
      
      m_log.debug(invocation.getMethod().getName() + ": accepted by " + 
         getClass().getName());
   }   

   /**
    * Process the supplied exception if it is an {@link PSErrorResultsException}
    * or an {@link PSErrorsException}, otherwise a noop
    * 
    * @param e The exception to process, may be <code>null</code>.
    * @param perm The permission to enforce, may not be <code>null</code>.
    */
   protected void processErrorException(Exception e, 
      PSPermissions perm)
   {
      if (perm == null)
         throw new IllegalArgumentException("perm may not be null");
      
      PSErrorExceptionWrapper ex;
      if (e instanceof PSErrorResultsException)
         ex = new PSErrorExceptionWrapper((PSErrorResultsException) e);
      else if (e instanceof PSErrorsException)
         ex = new PSErrorExceptionWrapper((PSErrorsException) e);
      else
         return;

      logDebugMsg("processException by " + getClass().getName() + ": " + 
         e.getClass().getName());

      // if we didn't pre-filter, do it now and remove from results
      Map<IPSGuid, PSErrorException> errors = m_failedGuids;
      if (errors == null)
      {
         errors = filterObject(ex.getResults(), perm);
         for (Map.Entry<IPSGuid, PSErrorException> entry : errors.entrySet())
         {
            if (entry.getValue() != null)
               ex.removeResult(entry.getKey());
         }
      }
      
      // now add back as errors
      addErrorsAndResults(errors, null, ex);
   }   
   
   /**
    * Throws the appropriate exception using the supplied result object and the
    * values in supplied errors which must not be empty.
    * 
    * @param errors Map of failed guids to the resulting exception, not
    * <code>null</code> or empty.  See 
    * {@link #filterObject(Object, PSPermissions)} for more details.
    * @param result The result object, may be <code>null</code>.
    * 
    * @throws PSErrorResultsException If the result is a collection and the
    * method allows this exception to be thrown.
    * @throws PSErrorsException If the result is a collection and the
    * method allows this exception to be thrown.
    * @throws PSErrorException if not throwing a {@link PSErrorResultsException}
    * or {@link PSErrorsException}, using the first exception found in the 
    * failed guids map.
    * 
    */
   protected void handleException(Map<IPSGuid, PSErrorException> errors, 
      Object result) 
      throws PSErrorResultsException, PSErrorException, PSErrorsException
   {
      if (errors == null || errors.isEmpty())
         throw new IllegalStateException("errors may not be null or empty.");
      
      PSErrorExceptionWrapper ex;
      if (throwsException(PSErrorResultsException.class))
      {
         ex = new PSErrorExceptionWrapper(new PSErrorResultsException());
      }
      else if (throwsException(PSErrorsException.class))
      {
         ex = new PSErrorExceptionWrapper(new PSErrorsException());
      }
      else
      {
         // throw the first security exception
         PSErrorException e = errors.values().iterator().next();
         if (throwsException(e.getClass()))
            throw e;
         throw new RuntimeException(e);
      }
      
      addErrorsAndResults(errors, result, ex);
      
      ex.throwException();
   }

   /**
    * Adds any errors from and the supplied results to the supplied exception. 
    * @param errors Map of failed guids to the resulting exception, 
    * not <code>null</code>, may be empty.  Entries with <code>null</code>
    * values are successful guids.
    * @param result The result object, may be <code>null</code>.
    * @param ex The exception to add to, may not be <code>null</code> and to
    * be an instance of either {@link PSErrorResultsException} or
    * {@link PSErrorsException}.
    */
   protected void addErrorsAndResults(Map<IPSGuid, PSErrorException> errors, 
      Object result, PSErrorExceptionWrapper ex)
   {
      if (errors == null)
         throw new IllegalArgumentException("errors may not be null");
      
      if (result instanceof Collection)
      {
         Collection collection = (Collection) result;
         
         for (Object obj : collection)
         {
            IPSGuid guid = extractGuid(obj);
            if (guid != null)
            {
               ex.addResult(guid, obj);
            }
         }
      }
      else
      {
         IPSGuid guid = extractGuid(result);
         if (guid != null)
         {
            ex.addResult(guid, result);
         }
      }
      
      List<IPSGuid> resultList = ex.getResults();
      Set<IPSGuid> errorGuids = ex.getErrorGuids();
      for (Map.Entry<IPSGuid, PSErrorException> failure : 
         errors.entrySet())
      {
         // don't add it if it's already an error
         if (errorGuids.contains(failure.getKey()))
            continue;
         
         // if value is not null, add as an error, else add as a result (assumes
         // the method called returns void) 
         if (failure.getValue() != null)
            ex.addError(failure.getKey(), failure.getValue());
         else if (!resultList.contains(failure.getKey()))
            ex.addResult(failure.getKey(), null);
      }
   }

   /**
    * Determine if the method being invoked throws the specified exception or a
    * one of it's super-classes.
    * 
    * @param exClass The exception to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it's thrown, <code>false</code> if not.
    */
   protected boolean throwsException(Class exClass)
   {
      boolean doesThrow = false;
      Class<?>[] exTypes = getInvocation().getMethod().getExceptionTypes();
      for (Class<?> test : exTypes)
      {
         if (test.isAssignableFrom(exClass))
         {
            doesThrow = true;
            break;
         }
      }
      return doesThrow;
   }

   /**
    * Determine if a design service call is being made.
    * @param invocation The current invocation, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is a design web service call, 
    * <code>false</code> if it is a public call.
    */
   protected boolean isDesignService(MethodInvocation invocation)
   {
      Method method = invocation.getMethod();
      Class clazz = method.getDeclaringClass();
      if (clazz.isInterface())
      {
         String className = clazz.getName();
         return className.endsWith("DesignWs");
      }
      // remove if never needed
      throw new RuntimeException("Interface expected, not concrete class");
   }
   
   /**
    * Throws appropriate exception
    */
   protected void handleBadSecurityConfig()
   {
      // bad security config
      throw new RuntimeException(
         "Failed to determine guid(s) from method inputs or results: " + 
         getInvocation().getMethod().getName() + "().  Method cannot " +
               "be secured and should be annotated to be ignored.");
   }

   /**
    * Invocation set by {@link #setInvocation(MethodInvocation)}, not 
    * <code>null</code> after that.
    */
   private MethodInvocation m_invocation = null;
   
   /**
    * Flag to indicate if the method call should proceed.  Initially 
    * <code>true</code>, may be set to <code>false</code> if pre-processing
    * results in no valid inputs.
    */
   private boolean m_proceed = true;
   
   /**
    * Logger to use, never <code>null</code>.
    */
   protected static final Logger m_log = LogManager.getLogger(PSSecurityStrategy.class);
   
   /**
    * List of defined strategy classes, never <code>null</code>, may be empty.
    */
   private static List<Class<? extends PSSecurityStrategy>> ms_stategyClassList 
      = new ArrayList<Class<? extends PSSecurityStrategy>>();

   /**
    * Map of objects that have failed the security check during filtering, 
    * <code>null</code> only if no objects have been found to filter, empty if 
    * no objects failed.  If any objects failed, guids that passed are also
    * included in the map with <code>null</code> values.
    */
   protected Map<IPSGuid, PSErrorException> m_failedGuids = null;
   
   static
   {
      ms_stategyClassList.add(PSDeleteSecurityStrategy.class);
      ms_stategyClassList.add(PSFindSecurityStrategy.class);
      ms_stategyClassList.add(PSLoadSecurityStrategy.class);
      ms_stategyClassList.add(PSSaveSecurityStrategy.class);
   }
   
   /**
    * Wrapper class to allow interchangeable use of 
    * {@link PSErrorResultsException} and {@link PSErrorsException}
    */
   public class PSErrorExceptionWrapper
   {
      /**
       * Construct with an {@link PSErrorResultsException}
       * 
       * @param e The exception, may not be <code>null</code>.
       */
      public PSErrorExceptionWrapper(PSErrorResultsException e)
      {
         if (e == null)
            throw new IllegalArgumentException("e may not be null");
         
         m_errResEx = e;
      }

      /**
       * Construct with an {@link PSErrorsException}
       * 
       * @param e The exception, may not be <code>null</code>.
       */
      public PSErrorExceptionWrapper(PSErrorsException e)
      {
         if (e == null)
            throw new IllegalArgumentException("e may not be null");
         
         m_errsEx = e;
      }
            
      /**
       * Throw the exception supplied during construction.
       * 
       * @throws PSErrorResultsException if one was supplied.
       * @throws PSErrorsException if one was supplied.
       */
      public void throwException() throws PSErrorResultsException, 
         PSErrorsException
      {
         if (m_errResEx != null)
            throw m_errResEx;
         throw m_errsEx;
      }

      /**
       * Delegates to the exception supplied during construction.
       * See {@link PSErrorResultsException#addError(IPSGuid, Object)} and
       * {@link PSErrorsException#addError(IPSGuid, Object)}.
       * 
       * @param guid 
       * @param error 
       */
      public void addError(IPSGuid guid, PSErrorException error)
      {
         if (m_errResEx != null)
            m_errResEx.addError(guid, error);
         else
            m_errsEx.addError(guid, error);
      }

      /**
       * Gets the list of guids that have errors in the exception supplied 
       * during construction.
       * 
       * @return The guids, never <code>null</code>, may be empty.
       */
      public Set<IPSGuid> getErrorGuids()
      {
         if (m_errResEx != null)
            return m_errResEx.getErrors().keySet();
         else
            return m_errsEx.getErrors().keySet();
      }
      
      /**
       * Delegates to the exception supplied during construction.
       * See {@link PSErrorResultsException#addResult(IPSGuid, Object)} and
       * {@link PSErrorsException#addResult(IPSGuid)}.
       * 
       * @param guid The guid of the result, may not be <code>null</code>.
       * @param obj ignored if an {@link PSErrorsException} was supplied during
       * construction, otherwise ignored if it is <code>null</code> (method is a
       * noop).
       */
      public void addResult(IPSGuid guid, Object obj)
      {
         if (m_errResEx != null)
         {
            if (obj != null)
               m_errResEx.addResult(guid, obj);
         }
         else
            m_errsEx.addResult(guid);
      }

      /**
       * Get a list of successful guids.
       * 
       * @return The list, never <code>null</code>. Modifications to the list
       * do not affect this object.
       */
      public List<IPSGuid> getResults()
      {
         List<IPSGuid> results = new ArrayList<IPSGuid>();
         if (m_errResEx != null)
            results.addAll(m_errResEx.getResults().keySet());
         else
            results.addAll(m_errsEx.getResults());
         
         return results;
      }

      /**
       * Removes the result from the wrapped exception. 
       * 
       * @param guid The guid of the result to remove, may not be 
       * <code>null</code>.
       */
      public void removeResult(IPSGuid guid)
      {
         if (guid == null)
            throw new IllegalArgumentException("guid may not be null");
         
         if (m_errResEx != null)
            m_errResEx.removeResult(guid);
         else
            m_errsEx.getResults().remove(guid);
      }
      
      /**
       * Exception that may have been supplied during construction, only 
       * <code>null</code> if {@link #m_errsEx} is not <code>null</code>.
       */
      PSErrorResultsException m_errResEx = null;
      
      /**
       * Exception that may have been supplied during construction, only 
       * <code>null</code> if {@link #m_errResEx} is not <code>null</code>.
       */
      PSErrorsException m_errsEx = null;

   }   
}

