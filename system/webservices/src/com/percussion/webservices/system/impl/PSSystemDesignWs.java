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
package com.percussion.webservices.system.impl;

import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterServiceLocator;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The private system design webservice implementations.
 */
@PSBaseBean("sys_systemDesignWs")
public class PSSystemDesignWs extends PSSystemBaseWs implements
   IPSSystemDesignWs
{
   // @see IPSSystemDesignWs#createAcl(IPSGuid)
   @Transactional
   public PSAclImpl createAcl(IPSGuid id, String session, String user)
      throws PSLockErrorException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user may not be null or empty");

      // create the acl
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      PSAclImpl acl = (PSAclImpl) aclService.createAcl(id,
         new PSTypedPrincipal(user, PrincipalTypes.USER));

      // lock it
         PSWebserviceUtils.createLock(acl.getGUID(), session, user, null);
   
      return acl;
   }

   // @see IPSSystemDesignWs#createGuids(PSTypeEnum, int)
   @Transactional
   public List<IPSGuid> createGuids(PSTypeEnum type, int count)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      if (count <= 0)
         throw new IllegalArgumentException("count must be > 0");

      IPSGuidManager manager = PSGuidManagerLocator.getGuidMgr();
      return manager.createGuids(type, count);
   }

   // @see IPSSystemDesignWs#createItemFilters(List, String, String)
   @Transactional
   public List<PSItemFilter> createItemFilters(List<String> names,
      String session, String user)
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<PSItemFilter> filters = new ArrayList<>();
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         IPSFilterService service = PSFilterServiceLocator.getFilterService();

         List<IPSItemFilter> existing = service.findFiltersByName(name);
         if (!existing.isEmpty())
         {
            PSWebserviceUtils.throwObjectExistException(name,
               PSTypeEnum.ITEM_FILTER);
         }

         IPSItemFilter filter = service.createFilter(name, null);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService
               .createLock(filter.getGUID(), session, user, null, false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         filters.add((PSItemFilter) filter);
      }

      return filters;
   }

   // @see IPSSystemDesignWs#createLocks(List, boolean, String, String)
   @Transactional
   public void createLocks(List<IPSGuid> ids, boolean overrideLock,
      String session, String user) throws PSErrorsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSObjectLock lock = lockService.findLockByObjectId(id, session,
               user);
            Integer version = lock == null ? null : lock.getLockedVersion();
            lockService.createLock(id, session, user, version, overrideLock);
            results.addResult(id);
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.CREATE_EXTEND_LOCK_FAILED;
            PSLockErrorException error = new PSLockErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.longValue(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e),
               e.getLocker(), e.getRemainigTime());

            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   // @see IPSSystemDesignWs#createRelationshipType(String, String)
   @Transactional
   public List<PSRelationshipConfig> createRelationshipTypes(
      List<String> names, List<String> categories, String session, String user)
      throws PSErrorException
   {
      PSWebserviceUtils.validateParameters(names, "names", true, session, user);
      PSWebserviceUtils.validateParameters(categories, "categories", true,
         session, user);
      if (names.size() != categories.size())
         throw new IllegalArgumentException(
            "the size of names and types must be equal.");

      PSRelationshipConfigSet configSet = getRelationshipConfigSet();
      final PSRelationshipConfig aaConfig = configSet
         .getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      List<PSRelationshipConfig> result = new ArrayList<>();
      for (int i = 0; i < names.size(); i++)
      {
         if (configSet.getConfig(names.get(i)) != null)
            throw new IllegalArgumentException(
               "The name of the created Relationship Type must be unique.");

         PSRelationshipConfig config = new PSRelationshipConfig(names.get(i),
            PSRelationshipConfig.RS_TYPE_USER, categories.get(i));

         // insert required user properties for Active Assembly configs
         if (config.getCategory().equals(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
         {
            PSPropertySet propSet = new PSPropertySet();
            for (String pname : PSRelationshipConfig
               .getPreDefinedUserPropertyNames())
            {
               if (pname.equals(PSRelationshipConfig.PDU_SORTRANK))
                  propSet.setProperty(pname, "0"); // default to 0
               else
                  propSet.setProperty(pname, null);
               if (aaConfig != null && aaConfig.getProperty(pname) != null)
               {
                  propSet.getProperty(pname).setDescription(
                     aaConfig.getProperty(pname).getDescription());
               }
            }
            config.setUserDefProperties(propSet.iterator());
         }

         int id = (int) PSGuidHelper
            .generateNextLong(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
         config.setId(id);
         PSWebserviceUtils.createLock(config.getGUID(), session, user, null);
         result.add(config);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSystemDesignWs#deleteAcls(List, boolean)
    */
   @Transactional
   public void deleteAcls(List<IPSGuid> ids, boolean ignoreDependencies, 
      String session, String user) throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", true, session, user);

      PSErrorsException results = new PSErrorsException();
      IPSAclService aclService = PSAclServiceLocator.getAclService();

      for (IPSGuid id : ids)
      {
         if (!ignoreDependencies)
         {
            PSErrorException error = 
               PSWebserviceUtils.checkDependencies(id);
            if (error != null)
            {
               results.addError(id, error);
               continue;
            }
         }
         
         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            try
            {
               aclService.deleteAcl(id);
               results.addResult(id);
            }
            catch (PSSecurityException e)
            {
               int code = IPSWebserviceErrors.DELETE_FAILED;
               PSDesignGuid guid = new PSDesignGuid(id);
               PSErrorException error = new PSErrorException(code, 
                  PSWebserviceErrors.createErrorMessage(code, 
                     PSAclImpl.class.getName(), guid.getValue(), 
                     e.getLocalizedMessage()), 
                     ExceptionUtils.getFullStackTrace(e));
               results.addError(id, error);
            }
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, 
               PSRelationshipConfig.class, results);
         }
      }
      
      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSSystemDesignWs#deleteRelationshipTypes(List, boolean)
   @Transactional
   public void deleteRelationshipTypes(List<IPSGuid> ids,
      boolean ignoreDependencies, String session, String user)
      throws PSErrorsException, PSErrorException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", true, session, user);

      PSErrorsException results = new PSErrorsException();
      PSRelationshipConfigSet configSet = getRelationshipConfigSet();

      for (IPSGuid id : ids)
      {
         if (!ignoreDependencies)
            PSWebserviceUtils.checkDependencies(id);

         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            PSRelationshipConfig config = configSet.getConfig(id);
            configSet.remove(config);

            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id,
               PSRelationshipConfig.class, results);
         }
      }

      /*
       * This must happen first since all relationship cconfigs are saved
       * in one step. If this fails this will throw a 
       * <code>PSErrorException</code> and no lock will be released.
       */
      PSWebserviceUtils.saveRelationshipConfigSet(configSet,
            IPSWebserviceErrors.DELETE_FAILED);

      /*
       * This may only have errors if no valid locks exist for any relationship
       * config requested to be deleted.
       */
      if (results.hasErrors())
         throw results;

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);
   }

   /**
    * @see IPSSystemDesignWs#deleteSharedProperties(List, boolean, String, 
    *    String)
    */
   @Transactional
   public void deleteSharedProperties(List<PSSharedProperty> properties,
      boolean ignoreDependencies, String session, String user)
      throws PSErrorsException
   {
      if (properties == null || properties.isEmpty())
         throw new IllegalArgumentException(
            "properties cannot be null or empty");

      IPSSystemService service = PSSystemServiceLocator.getSystemService();

      PSErrorsException results = new PSErrorsException();
      for (PSSharedProperty property : properties)
      {
         IPSGuid id = property.getGUID();

         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            boolean exists = false;
            try
            {
               service.loadSharedProperty(id);
               exists = true;
            }
            catch (PSSystemException e)
            {
               // ignore, just means that the property does not exist
            }

            if (exists)
            {
               // check for dependents if requested
               if (!ignoreDependencies)
               {
                  PSErrorException error = PSWebserviceUtils
                     .checkDependencies(id);
                  if (error != null)
                  {
                     results.addError(id, error);
                     continue;
                  }
               }

               service.deleteSharedProperty(id);
            }

            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id,
               PSSharedProperty.class, results);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSSystemDesignWs#deleteItemFilters(List, boolean, String, String)
   @Transactional
   public void deleteItemFilters(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      /*
       * First load all filters to be deleted, check dependencies if requested.
       * Collect all valid ids and filters into a map to do the real deletes
       * after validation.
       * This has to be done in separate steps because loading a filter which
       * has dependencies will fail if its dependency was deleted before the 
       * lookup.
       */
      Map<IPSGuid, IPSItemFilter> deletes = new HashMap<>();
      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            IPSItemFilter filter = null;
            try
            {
               filter = service.loadFilter(id);
            }
            catch (PSNotFoundException e)
            {
               // ignore, just means that the filter does not exist
            }

            if (filter != null)
            {
               // check for dependents if requested
               if (!ignoreDependencies)
               {
                  PSErrorException error = PSWebserviceUtils
                     .checkDependencies(id);
                  if (error != null)
                  {
                     results.addError(id, error);
                     continue;
                  }
               }

               deletes.put(id, filter);
            }
            else
            {
               /*
                * Filter does not exist, just add the id as successful result.
                */
               results.addResult(id);
            }
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, PSItemFilter.class,
               results);
         }
      }

      /*
       * Now walk all collected deletes and do the actual delete.
       */
      deletes.keySet().forEach(id -> {
         service.deleteFilter(deletes.get(id));
         results.addResult(id);
      });

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSSystemDesignWs#extendLocks(List, String, String)
   @Transactional
   public void extendLocks(List<IPSGuid> ids, String session, String user)
      throws PSErrorsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException errors = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         try
         {
            lockService.extendLock(id, session, user, null);

            errors.addResult(id);
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.CREATE_EXTEND_LOCK_FAILED;
            PSLockErrorException error = new PSLockErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, id.longValue(), e
                  .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e),
               e.getLocker(), e.getRemainigTime());

            errors.addError(id, error);
         }
      }

      if (errors.hasErrors())
         throw errors;
   }

   // @see IPSSystemDesignWs#findDependencies(List)
   public List<PSDependency> findDependencies(List<IPSGuid> ids)
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      return svc.findDependencies(ids);
   }

   // @see IPSSystemDesignWs#findItemFilters(String)
   public List<IPSCatalogSummary> findItemFilters(String name)
   {
      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      if (StringUtils.isBlank(name))
         name = "*";
      name = StringUtils.replaceChars(name, '*', '%');

      Set<IPSItemFilter> filters = new HashSet<>(service
         .findFiltersByName(name));
      return PSWebserviceUtils.toObjectSummaries(filters);
   }

   // @see IPSSystemDesignWs#findRelationshipTypes(String, String)
   public List<IPSCatalogSummary> findRelationshipTypes(String name,
      String category) throws PSErrorException
   {
      IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
      List<PSRelationshipConfig> rlist = service.loadRelationshipTypes(name,
         category);
      return PSWebserviceUtils.toObjectSummaries(rlist);
   }

   // @see IPSSystemDesignWs#findWorkflows(String)
   public List<IPSCatalogSummary> findWorkflows(String name)
   {
      if (!StringUtils.isBlank(name))
         name = name.replace('*', '%');

      return new ArrayList<>(PSWorkflowServiceLocator.getWorkflowService().findWorkflowSummariesByName(name));

   }

   // @see IPSSystemDesignWs#getLockedSummaries(String, String)
   public List<PSObjectSummary> getLockedSummaries(String session, String user)
      throws PSErrorResultsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      List<PSObjectLock> locks = lockService.findLocksByUser(session, user);

      return PSWebserviceUtils.getObjectSummaries(locks);
   }

   // @see IPSSystemDesignWs#isLocked(List)
   public List<PSObjectSummary> isLocked(List<IPSGuid> ids, String user)
      throws PSErrorResultsException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids, null,
         null);

      List<PSObjectSummary> objects = PSWebserviceUtils
         .getObjectSummaries(locks);

      List<PSObjectSummary> summaries = new ArrayList<>();
      for (IPSGuid id : ids)
      {
         boolean found = false;
         for (PSObjectSummary object : objects)
         {
            if (object.getGUID().equals(id))
            {
               summaries.add(object);
               objects.remove(object);
               found = true;
               break;
            }
         }

         if (!found)
            summaries.add(null);
      }

      return summaries;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSystemDesignWs#loadAcls(List, boolean, boolean, 
    *    String, String)
    */
   public List<PSAclImpl> loadAcls(List<IPSGuid> ids, boolean lock, 
      boolean overrideLock, String session, String user) 
      throws PSErrorResultsException
   {
      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");
      
      if (ids != null)
         PSWebserviceUtils.validateParameters(ids, "ids", lock, session, user);

      IPSAclService aclService = PSAclServiceLocator.getAclService();
      PSErrorResultsException results = new PSErrorResultsException();
      
      List<IPSAcl> acls;
      if (ids == null)
      {
         try
         {
            if (lock)
               acls = aclService.loadAclsModifiable(null);
            else
               acls = aclService.loadAcls(null);
         }
         catch (PSSecurityException e)
         {
            //make a fake id
            IPSGuid id = new PSGuid(-1, PSTypeEnum.INVALID, -1);
            results.addError(id, e);
            acls = Collections.emptyList();
         }
      }
      else
      {
         acls = new ArrayList<>();
         List<IPSAcl> existingAcls;
         if (lock)
            existingAcls = aclService.loadAclsForObjectsModifiable(ids);
         else
            existingAcls = aclService.loadAclsForObjects(ids);
         int i = 0;
         for (IPSAcl acl : existingAcls)
         {
            if (acl == null)
            {
               acl = aclService.createAcl(ids.get(i),
                     new PSTypedPrincipal(user, PrincipalTypes.USER));
               configureDefaultAclEntries(acl);
               }
            acls.add(acl);
            i++;
         }
      }
      
      List<IPSGuid> aclIds = new ArrayList<>();
      List<Integer> aclVersions = new ArrayList<>();
      for (IPSAcl acl : acls)
      {
         aclIds.add(acl.getGUID());
         aclVersions.add(((PSAclImpl) acl).getVersion());
      }
      try
      {
         if (lock)
         {
            PSWebserviceUtils.createLocks(aclIds, session, user, aclVersions,
               overrideLock);
         }
      }
      catch (PSLockErrorException e1)
      {
         List<PSObjectLock> success = e1.getResults();
         for (PSObjectLock l : success)
         {
            results.addResult(l.getObjectId(), l);
         }
         Map<IPSGuid, PSLockException> fail = e1.getErrors();
         for (IPSGuid id : fail.keySet())
         {
            results.addError(id, fail.get(id));
         }
         throw results;
      }
      List<PSAclImpl> aclList = new ArrayList<>(acls.size());
      for (IPSAcl acl : acls)
      {
         PSAclImpl aclImpl = (PSAclImpl) acl;
         assert (aclImpl != null);
         results.addResult(aclImpl.getObjectGuid(), aclImpl);

         try {
            aclService.saveAcls(Collections.singletonList(acl));
         } catch (PSSecurityException e) {
            results.addError(acl.getGUID(), e);
         }
         aclList.add(aclImpl);
      }

      
      if (results.hasErrors())
         throw results;
      return aclList;
   }
   /*
    * (non-Javadoc)
    * 
    * @see IPSSystemDesignWs#loadAcls(List, boolean, boolean, 
    *    String, String)
    */

   /**
    * @see IPSSystemDesignWs#loadConfiguration(String, boolean, boolean, 
    *    String, String)
    */
   public PSMimeContentAdapter loadConfiguration(String name, boolean lock,
      boolean overrideLock, String session, String user)
      throws FileNotFoundException, PSLockErrorException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
      PSMimeContentAdapter content;
      content = sysSvc.loadConfiguration(PSConfigurationTypes.valueOf(name));

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService.createLock(content.getGUID(), session, user, null,
               overrideLock);
         }
         catch (PSLockException e)
         {
            throw new PSLockErrorException(e.getErrorCode(), e.getMessage(),
               ExceptionUtils.getFullStackTrace(e));
         }
      }

      return content;
   }

   /**
    * @see IPSSystemDesignWs#loadItemFilters(List, boolean, boolean, String, 
    *    String)
    */
   @SuppressWarnings("unchecked")
   public List<PSItemFilter> loadItemFilters(List<IPSGuid> ids, boolean lock,
      boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            IPSItemFilter filter = service.loadFilter(id);
            results.addResult(id, filter);
         }
         catch (PSNotFoundException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSItemFilter.class
                  .getName(), guid.getValue()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         lockService.createLocks(results, session, user, overrideLock);
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * @see IPSSystemDesignWs#loadRelationshipTypes(List, boolean, boolean, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSRelationshipConfig> loadRelationshipTypes(List<IPSGuid> ids,
      boolean lock, boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      PSErrorResultsException results = new PSErrorResultsException();
      PSRelationshipConfigSet configSet;
      // load all configs
      try
      {
         configSet = getRelationshipConfigSet();
      }
      catch (PSErrorException e)
      {
         for (IPSGuid id : ids)
            results.addError(id, e);
         throw results;
      }
      // look for individual config
      for (IPSGuid id : ids)
      {
         PSRelationshipConfig c = configSet.getConfig(id);
         if (c != null)
         {
            if (lock)
            {
               try
               {
                  // create or extend lock with version = 1
                  lockService.createLock(id, session, user, 1,
                     overrideLock);
                  results.addResult(id, c);
               }
               catch (PSLockException e)
               {
                  log.error(PSExceptionUtils.getMessageForLog(e));
                  results.addError(id, e);
               }
            }
            else
            {
               results.addResult(id, c);
            }
         }
         else
         // cannot find config with the id
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSRelationshipConfig.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(new Exception()));
            results.addError(id, error);
         }
      }
      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * @see IPSSystemDesignWs#loadSharedProperties(String[], boolean, boolean, 
    *    String, String)
    */
   public List<PSSharedProperty> loadSharedProperties(String[] names,
      boolean lock, boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      if (names == null || names.length == 0)
         names = new String[] { "*" };

      IPSSystemService service = PSSystemServiceLocator.getSystemService();

      // load all properties first
      Set<PSSharedProperty> properties = new TreeSet<>();
      for (String name : names)
      {
         boolean done = false;
         if (StringUtils.isBlank(name))
         {
            name = "*";
            done = true;
         }

         name = StringUtils.replaceChars(name, '*', '%');
         properties.addAll(service.findSharedPropertiesByName(name));

         if (done)
            break;
      }

      // lock them if so requested
      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();

         PSErrorResultsException errors = new PSErrorResultsException();
         for (PSSharedProperty property : properties)
         {
            IPSGuid id = property.getGUID();
            try
            {
               lockService.createLock(id, session, user, property.getVersion(),
                  overrideLock);
               errors.addResult(id, property);
            }
            catch (PSLockException e)
            {
               int code = IPSWebserviceErrors.CREATE_LOCK_FAILED;
               PSLockErrorException error = new PSLockErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, property
                     .getClass().getName(), id.longValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e), e.getLocker(), e.getRemainigTime());
               errors.addError(id, error);
            }
         }

         if (errors.hasErrors())
            throw errors;
      }

      return new ArrayList<>(properties);
   }

   // @see IPSSystemDesignWs#releaseLocks(List, String, String)
   @Transactional
   public void releaseLocks(List<IPSGuid> ids, String session, String user)
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids, session,
         user);
      lockService.releaseLocks(locks);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSSystemDesignWs#saveAcls(List, boolean, String, String)
    */
   @SuppressWarnings("unchecked")
   @Transactional
   public List<PSUserAccessLevel> saveAcls(List<PSAclImpl> acls, 
      boolean release, String session, String user) 
      throws PSErrorResultsException
   {
      PSWebserviceUtils.validateParameters(acls, "acls", true, session, user);
      
      IPSObjectLockService lockService = 
         PSObjectLockServiceLocator.getLockingService();
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      
      PSErrorResultsException results = new PSErrorResultsException();
      List<IPSAcl> saveList = new ArrayList<>(1);
      
      List<IPSGuid> aclIds = new ArrayList<>();
      for (IPSAcl acl : acls)
         aclIds.add(acl.getGUID());
      List<PSObjectLock> aclLocks = lockService.findLocksByObjectIds(aclIds,
            session, user);
      Map<IPSGuid, PSObjectLock> aclIdToLock = 
         new HashMap<>();
      for (PSObjectLock l : aclLocks)
         aclIdToLock.put(l.getObjectId(), l);
      
      Map<IPSGuid, Exception> errors = new HashMap<>();
      
      String className = PSAclImpl.class.getName();
      for (PSAclImpl acl : acls)
      {
         IPSGuid id = acl.getGUID();
         PSObjectLock lock;
         lock = aclIdToLock.get(id);
         /* todo: this should consider the session too, but be careful,
          * the session passed in is the server session, but the session in
          * the lock may be a different session. The lock svc should be used
          * to check if a lock matches a user and session
          */
         if (lock != null && lock.getLocker().equalsIgnoreCase(user))
         {
            // set the correct version
            Integer version = lock.getLockedVersion();
            if (version != null)
               acl.setVersion(version);
            saveList.add(acl);
         }
         else
         {
            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               PSDesignGuid guid = new PSDesignGuid(id);
               PSErrorException error = new PSErrorException(code, 
                  PSWebserviceErrors.createErrorMessage(code, className, 
                     guid.getValue()), 
                  ExceptionUtils.getFullStackTrace(new Exception()));
               errors.put(id, error);
            }
            else
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
               PSDesignGuid guid = new PSDesignGuid(id);
               PSErrorException error = new PSErrorException(code, 
                  PSWebserviceErrors.createErrorMessage(code, className, 
                     guid.getValue(), lock.getLocker(), 
                     lock.getRemainingTime()),
                  ExceptionUtils.getFullStackTrace(new Exception()));
               errors.put(id, error);
            }
         }
      }
         
      try
      {
         aclService.saveAcls(saveList);
      }
      catch (PSSecurityException e) 
      {
         /* assign error to all guids that don't already have one and skip 
          * processing below
          */ 
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSDesignGuid guid = new PSDesignGuid(-1);
         PSErrorException error = new PSErrorException(code, 
            PSWebserviceErrors.createErrorMessage(code, 
                  className, guid.getValue()), 
               ExceptionUtils.getFullStackTrace(e));
         for (PSAclImpl acl : acls)
         {
            IPSGuid id = acl.getGUID();
            if (errors.get(id) == null)
               results.addError(id, error);
         }
         log.debug("ACL Error count: {} ACL Size: {} Error: {}",errors.size(),
                 acls.size(), PSExceptionUtils.getDebugMessageForLog(e));
      }
      
      List<PSObjectLock> locksToProcess = new ArrayList<>();
      List<Integer> aclVersions = new ArrayList<>();
      List<IPSGuid> savedAclIds = new ArrayList<>();
      
      for (PSAclImpl acl : acls)
      {
         IPSGuid id = acl.getGUID();
         Exception e = errors.get(id);
         if (e == null)
         {
            savedAclIds.add(id);
            locksToProcess.add(aclIdToLock.get(id));
            aclVersions.add((acl).getVersion());
         }
      }

      if (!savedAclIds.isEmpty())
      {
         try
         {
            lockService.extendLocks(savedAclIds, session, user,
                  aclVersions, PSObjectLock.LOCK_INTERVAL);
         }
         catch (PSLockException e1)
         {
            Map<IPSGuid, PSLockException> extendLockErrors = e1.getErrors();
            for (IPSGuid id : extendLockErrors.keySet())
            {
               errors.put(id, extendLockErrors.get(id));
               //don't release locks on failures
               //FB: GC_UNRELATED_TYPES NC 1-17-16
               locksToProcess.removeAll(extendLockErrors.get(id).getResults());
            }
         }
      }

      for (IPSAcl acl : acls)
      {
         IPSGuid id = acl.getGUID();
         Exception e = errors.get(id);
         if (e == null)
            results.addResult(id, aclService.calculateUserAccessLevel(acl));
      }

      if (release && !locksToProcess.isEmpty())
      {
         lockService.releaseLocks(locksToProcess);
      }
      
      if (results.hasErrors())
         throw results;
      
      return results.getResults(savedAclIds);
   }

   /**
    * @see IPSSystemDesignWs#saveConfiguration(PSMimeContentAdapter, boolean,
    *    String, String)
    */
   @Transactional
   public void saveConfiguration(PSMimeContentAdapter configuration,
      boolean release, String session, String user)
      throws PSLockErrorException, IOException
   {
      if (configuration == null)
         throw new IllegalArgumentException("configuration may not be null");
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      IPSGuid id = configuration.getGUID();
      try
      {
         PSObjectLock lock;
         lock = lockService.findLockByObjectId(id, session, user);
         if (lock != null)
         {

            // save
            IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
            sysSvc.saveConfiguration(configuration);

            // extend the lock 
            if (!release)
               lockService.extendLock(id, session, user, null);
         }
         else
         {
            lock = lockService.findLockByObjectId(id, null, null);

            if (lock == null)
            {
               int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
               PSDesignGuid guid = new PSDesignGuid(id);
               throw new PSLockException(code, guid.getValue());
            }

            int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
            PSDesignGuid guid = new PSDesignGuid(id);
            throw new PSLockException(code, guid.getValue(), lock.getLocker(),
               lock.getRemainingTime());
         }
      }
      catch (PSLockException e)
      {
         throw new PSLockErrorException(e.getErrorCode(), e.getMessage(),
            ExceptionUtils.getFullStackTrace(e));
      }

      if (release)
      {
         PSObjectLock lock = lockService.findLockByObjectId(id, session, user);
         lockService.releaseLock(lock);
      }
   }

   // @see IPSSystemDesignWs#saveItemFilters(List, boolean, String, String)
   @Transactional
   public void saveItemFilters(List<PSItemFilter> filters, boolean release,
      String session, String user) throws PSErrorsException
   {
      if (filters == null || filters.isEmpty())
         throw new IllegalArgumentException("filters may not be null or empty");
      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");
      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();
      IPSFilterService service = PSFilterServiceLocator.getFilterService();

      PSErrorsException results = new PSErrorsException();
      List<IPSGuid> ids = new ArrayList<>(filters.size());
      for (PSItemFilter filter : filters)
      {
         IPSGuid id = filter.getGUID();
         try
         {
            PSObjectLock lock = lockService.findLockByObjectId(id, session,
               user);
            if (lock != null)
            {
               // set the correct version
               Integer version = lockService.getLockedVersion(id);
               if (version != null)
                  filter.setVersion(version);

               // save
               service.saveFilter(filter);

               // extend the lock 
               if (!release)
               {
                  filter = (PSItemFilter) service.loadFilter(id);

                  lockService
                     .extendLock(id, session, user, filter.getVersion());
               }

               // save id for release
               ids.add(id);
               results.addResult(id);
            }
            else
            {
               lock = lockService.findLockByObjectId(id, null, null);

               if (lock == null)
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code,
                        PSItemFilter.class.getName(), guid.getValue()),
                     ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
               else
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code,
                        PSItemFilter.class.getName(), guid.getValue(), lock
                           .getLocker(), lock.getRemainingTime()),
                     ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSItemFilter.class
                  .getName(), guid.getValue()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (release && !ids.isEmpty())
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * @see IPSSystemDesignWs#saveRelationshipTypes(List, boolean, String, 
    *    String)
    */
   @Transactional
   public void saveRelationshipTypes(List<PSRelationshipConfig> configs,
      boolean release, String session, String user) throws PSErrorsException,
      PSErrorException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSRelationshipConfigSet configSet = getRelationshipConfigSet();

      PSErrorsException results = new PSErrorsException();
      List<IPSGuid> releasedIds = new ArrayList<>();
      Class<PSRelationshipConfig> cz = PSRelationshipConfig.class;
      for (PSRelationshipConfig config : configs)
      {
         IPSGuid id = config.getGUID();
         if (lockService.isLockedFor(id, session, user))
         {
            if (lockService.isLockedFor(id, session, user))
            {
               setRelationshipConfig(config, configSet);
               if (!release)
                  PSWebserviceUtils.extendLock(id, cz, session, user,
                          1, results);
               else
                  releasedIds.add(id);

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id, cz, results);
            }
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, cz, results);
         }
      }

      if (release)
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(
            releasedIds, session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;

      PSWebserviceUtils.saveRelationshipConfigSet(configSet,
            IPSWebserviceErrors.SAVE_FAILED);
   }

   // @see IPSSystemDesignWs#saveSharedProperties(List, boolean, String, String)
   @Transactional
   public void saveSharedProperties(List<PSSharedProperty> properties,
      boolean release, String session, String user) throws PSErrorsException
   {
      if (properties == null || properties.isEmpty())
         throw new IllegalArgumentException(
            "properties cannot be null or empty");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSSystemService service = PSSystemServiceLocator.getSystemService();

      List<IPSGuid> ids = new ArrayList<>();

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();
      for (PSSharedProperty property : properties)
      {
         IPSGuid id = property.getGUID();

         try
         {
            Integer version = property.getVersion();
            if (version == null || lockService.isLockedFor(id, session, user))
            {
               // set the correct version
               if (version != null)
                  version = lockService.getLockedVersion(id);
               if (version != null)
                  property.setVersion(version);

               // save the object and extend the lock
               service.saveSharedProperty(property);
               if (!release)
               {
                  if (version == null)
                  {
                     // the id may have changed with the save
                     id = property.getGUID();
                     lockService.createLock(id, session, user, property
                        .getVersion(), false);
                  }
                  else
                  {
                     version = property.getVersion();
                     lockService.extendLock(id, session, user, version);
                  }
               }

               results.addResult(id);
            }
            else
            {
               PSObjectLock lock = lockService.findLockByObjectId(id, null,
                  null);
               if (lock == null)
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code,
                        PSSharedProperty.class.getName(), guid.getValue()),
                     ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
               else
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code,
                     PSWebserviceErrors.createErrorMessage(code,
                        PSSharedProperty.class.getName(), guid.getValue(), lock
                           .getLocker(), lock.getRemainingTime()),
                     ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
            }
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  PSSharedProperty.class.getName(), guid.getValue(), e
                     .getLocalizedMessage()), ExceptionUtils
                  .getFullStackTrace(e));
            results.addError(id, error);
         }

         ids.add(id);
      }

      if (release)
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
            session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * Adds system default entries to the supplied ACL. These entries are such
    * that this acl, if saved, would behave as if it did not exist.
    * 
    * @param acl The default entries are added to this acl. Assumed to be a
    * newly created acl and not <code>null</code>.
    */
   private void configureDefaultAclEntries(IPSAcl acl)
   {
      try
      {
         IPSAclEntry ownerEntry = ((IPSAclEntry) acl.entries().nextElement());
         IPSAclEntry entry = acl.createDefaultEntry(false);
         entry.addPermissions(new PSPermissions[] { PSPermissions.DELETE,
            PSPermissions.OWNER, PSPermissions.READ, PSPermissions.UPDATE });
         acl.addEntry(ownerEntry.getPrincipal(), entry);

         // Default entry has full including owner permission, delete the
         // current user entry
         acl.removeEntry(entry.getPrincipal(), ownerEntry);
         ownerEntry = entry;

         entry = acl.createDefaultEntry(true);
         acl.addEntry(ownerEntry.getPrincipal(), entry);
      }
      catch (NotOwnerException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets the specified relationship config to the specified configuration set.
    * The lock version is <code>null</code> for a new relationship config and
    * it is not <code>null</code> for an existing one. 
    * 
    * @param config the new or existing relationship config, assumed not 
    *    <code>null</code>.
    * @param configSet the configuration set, assumed not <code>null</code>.
    */
   private void setRelationshipConfig(PSRelationshipConfig config,
      PSRelationshipConfigSet configSet)
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      try
      {
         Integer lockVersion = lockService.getLockedVersion(config.getGUID());
         PSRelationshipConfig tgtConfig = configSet.getConfig(config.getName());
         if (lockVersion != null)
         {
            // set an existing config
            if (tgtConfig == null)
               throw new IllegalArgumentException(
                  "Cannot find the to be saved relationship configuration with name \""
                     + config.getName() + "\" in the configuration set.");

            tgtConfig.copyFrom(config);
         }
         else
         // add a new config
         {
            if (tgtConfig != null)
               throw new IllegalArgumentException(
                  "Cannot find the to be saved relationship configuration with name \""
                     + config.getName() + "\" in the configuration set.");

            configSet.add(config);
         }
      }
      catch (PSLockException e)
      {
         // not possible, the lock is already acquired by the caller
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException(e);
      }
   }

}
