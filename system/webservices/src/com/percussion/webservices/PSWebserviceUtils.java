/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.webservices;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSFolderRelationshipCache;
import com.percussion.server.config.PSConfigManager;
import com.percussion.server.webservices.PSWebServicesRequestHandler;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.assembly.IPSAssemblyDesignWs;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.common.ObjectType;
import com.percussion.webservices.common.Property;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.common.RelationshipFilterRelationshipType;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.system.IPSSystemDesignWs;
import com.percussion.webservices.system.PSRelationshipFilterCategory;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Utilities useful for all or multiple webservices.
 */
public class PSWebserviceUtils
{
   
   
   private static final Logger log = LogManager.getLogger(PSWebserviceUtils.class);
   
   /**
    * Converts the supplied catalog summaries into object summaries and adds the
    * lock information if the objects are locked.
    * 
    * @param objects the catalog summaries to convert into object summaries, not
    * <code>null</code>. It must contain a list of {@link IPSCatalogSummary}
    * objects.
    * 
    * @return the object summaries with ACL and the lock information set if the
    * cataloged object is locked, never <code>null</code>.
    */
   public static List<IPSCatalogSummary> toObjectSummaries(Collection objects)
   {

      if (objects.isEmpty())
         return Collections.emptyList();

      Map<IPSGuid,IPSCatalogSummary> summaries = new HashMap<>();

      for (Object obj : objects)
      {
         if (!(obj instanceof IPSCatalogSummary))
            throw new IllegalArgumentException(
                  "objects must contain a list of IPSCatalogSummary objects.");
         IPSCatalogSummary cat = (IPSCatalogSummary) obj;
         summaries.put(cat.getGUID(),cat);
      }

      IPSAclService service = PSAclServiceLocator.getAclService();
      Map<IPSGuid,IPSAcl> objectAcls = new HashMap<>();

      for(IPSGuid objectGuid : summaries.keySet()) {
         objectAcls.put(objectGuid, service.loadAclForObject(objectGuid));
      }

      Map<IPSGuid,PSObjectSummary> objSums = toObjectSummaries(summaries, objectAcls);

      return Arrays.asList(objSums.values().toArray(new IPSCatalogSummary[objSums.size()]));
   }

   /**
    * Converts the supplied catalog summary into an object summary and adds the
    * lock information if the object is locked.
    * 
    * @param values the catalog summaries to convert into object summaries, not
    * <code>null</code>.
    * 
    * @param acls The corresponding acls for the summaries. The length must eq
    * the length of <code>values</code>. Entries may be <code>null</code>.
    * 
    * @return the object summaries with the lock information set if the
    * cataloged object is locked, never <code>null</code>.
    */
   public static Map<IPSGuid,PSObjectSummary> toObjectSummaries(
         Map<IPSGuid,IPSCatalogSummary> values, Map<IPSGuid, IPSAcl> acls)
   {
      ArrayList<IPSGuid> objIds = new ArrayList<>();
      Collections.addAll(objIds, values.keySet().toArray(new IPSGuid[values.size()]));

      IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
      List<PSObjectLock> locks = new ArrayList<>();
      if (!objIds.isEmpty())
      {
         locks = lockService.findLocksByObjectIds(objIds, null, null);
      }

      // add nulls for missing locks
      Map<IPSGuid, PSObjectLock> guidToLock = new HashMap<>();
      for (PSObjectLock lock : locks)
      {
         guidToLock.put(lock.getObjectId(), lock);
      }

      HashMap<IPSGuid,PSObjectSummary> summaries = new HashMap<>();
      IPSAclService aclService = PSAclServiceLocator.getAclService();

      for (IPSGuid catGuid : values.keySet())
      {
         PSObjectSummary summary = new PSObjectSummary(values.get(catGuid));
         PSObjectLock lock = guidToLock.get(summary.getGUID());
         if (lock != null)
            summary.setLockedInfo(lock);

         if(acls.get(summary.getGUID())!=null) {
            summary.setPermissions(aclService.calculateUserAccessLevel(acls
                    .get(summary.getGUID())));
         }else{
            summary.setPermissions(aclService.calculateUserAccessLevel(null));
         }
         summaries.put(summary.getGUID(),summary);
      }

      return summaries;
   }

   /**
    * Get the object summaries for all supplied locks.
    * 
    * @param locks the locks for which to get the object summaries, not
    * <code>null</code> or empty.
    * @return the summaries for all locks, never <code>null</code> or empty.
    * @throws PSErrorResultsException for any error loading the summaries.
    */
   public static List<PSObjectSummary> getObjectSummaries(
         List<PSObjectLock> locks) throws PSErrorResultsException
   {
      // prepare the id lists by object type
      Map<PSTypeEnum, List<IPSGuid>> idsByType = new HashMap<>();
      for (PSObjectLock lock : locks)
      {
         IPSGuid id = lock.getObjectId();
         PSTypeEnum type = PSTypeEnum.valueOf(id.getType());

         List<IPSGuid> ids = idsByType.get(type);
         if (ids == null)
         {
            ids = new ArrayList<>();
            idsByType.put(type, ids);
         }
         ids.add(id);
      }

      // now process the various types and build the summaries
      List<PSObjectSummary> summaries = new ArrayList<>();
      for (PSTypeEnum type : idsByType.keySet())
      {
         List<IPSGuid> ids = idsByType.get(type);
         List<IPSCatalogSummary> objects = loadDesignObjects(type, ids);

         for (IPSCatalogSummary object : objects)
         {
            PSObjectSummary summary = new PSObjectSummary(object);
            for (PSObjectLock lock : locks)
            {
               if (object.getGUID().equals(lock.getObjectId()))
               {
                  summary.setLockedInfo(lock);
                  break;
               }
            }

            summaries.add(summary);
         }
      }

      return summaries;
   }

   /**
    * Load the objects for the supplied ids read-only.
    * 
    * @param type the object type to process the ids for, not <code>null</code>.
    * @param ids the ids of the objects to load, not <code>null</code> or
    * empty.
    * @return the loaded design objects for the supplied ids, never
    * <code>null</code>.
    * @throws PSErrorResultsException for any error loading the design object.
    */
   @SuppressWarnings("unchecked")
   public static List<IPSCatalogSummary> loadDesignObjects(PSTypeEnum type,
         List<IPSGuid> ids) throws PSErrorResultsException
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSAssemblyDesignWs assemblyService = PSAssemblyWsLocator
            .getAssemblyDesignWebservice();
      IPSContentDesignWs contentService = PSContentWsLocator
            .getContentDesignWebservice();
      IPSSecurityDesignWs securityService = PSSecurityWsLocator
            .getSecurityDesignWebservice();
      IPSSystemDesignWs systemService = PSSystemWsLocator
            .getSystemDesignWebservice();
      IPSUiDesignWs uiService = PSUiWsLocator.getUiDesignWebservice();

      try
      {
         switch (type)
         {
            case ACL:
               return new ArrayList<>(loadAcls(ids));
            case ACTION:
               return new ArrayList<>(uiService.loadActions(
                     ids, false, false, null, null));
            case AUTO_TRANSLATIONS:
               List<PSAutoTranslation> list = new ArrayList();
               try
               {
                  list = contentService.loadTranslationSettings(false, false,
                        null, null);
               }
               catch (PSLockErrorException e)
               {
                  // Should not happen since we do not want to lock
               }
               return new ArrayList<>(list);
            case COMMUNITY_DEF:
               return new ArrayList<>(securityService
                     .loadCommunities(ids, false, false, null, null));
            case DISPLAY_FORMAT:
               return new ArrayList<>(uiService
                     .loadDisplayFormats(ids, false, false, null, null));
            case HIERARCHY_NODE:
               return new ArrayList<>(uiService
                     .loadHierachyNodes(ids, false, false, null, null));
            case KEYWORD_DEF:
               return new ArrayList<>(contentService
                     .loadKeywords(ids, false, false, null, null));
            case LOCALE:
               return new ArrayList<>(contentService
                     .loadLocales(ids, false, false, null, null));
            case SEARCH_DEF:
               return new ArrayList<>(uiService.loadSearches(
                     ids, false, false, null, null));
            case SLOT:
            {
               List<IPSCatalogSummary> summaries = new ArrayList<>();
               for (IPSTemplateSlot slot : assemblyService.loadSlots(ids,
                     false, false, null, null))
               {
                  summaries.add((IPSCatalogSummary) slot);
               }
               return summaries;
            }
            case TEMPLATE:
               return new ArrayList<>(assemblyService
                     .loadAssemblyTemplates(ids, false, false, null, null));
            case VIEW_DEF:
               return new ArrayList<>(uiService.loadViews(
                     ids, false, false, null, null));
            case NODEDEF:
               return new ArrayList<>(contentService
                     .loadContentTypes(ids, false, false, null, null));
            case RELATIONSHIP_CONFIGNAME:
               return new ArrayList<>(systemService
                     .loadRelationshipTypes(ids, false, false, null, null));
            case ITEM_FILTER:
               return new ArrayList<>(systemService
                     .loadItemFilters(ids, false, false, null, null));
            case CONFIGURATION:
            {
               List<IPSCatalogSummary> summaries = new ArrayList<>();
               IPSGuid id = ids.get(0);
               if (id.longValue() == PSContentEditorSharedDef.SHARED_DEF_ID)
                  summaries.add(new PSObjectSummary(id,
                        PSContentEditorSharedDef.XML_NODE_NAME));
               else if (id.longValue() == PSContentEditorSystemDef.SYSTEM_DEF_ID)
                  summaries.add(new PSObjectSummary(id,
                        PSContentEditorSystemDef.XML_NODE_NAME));
               else if (id.longValue() == PSConfigurationTypes.SERVER_PAGE_TAGS
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.SERVER_PAGE_TAGS.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.TIDY_CONFIG
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.TIDY_CONFIG.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.LOG_CONFIG
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.LOG_CONFIG.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.NAV_CONFIG
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.NAV_CONFIG.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.WF_CONFIG
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.WF_CONFIG.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.THUMBNAIL_CONFIG
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.THUMBNAIL_CONFIG.getFileName()));
               else if (id.longValue() == PSConfigurationTypes.SYSTEM_VELOCITY_MACROS
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.SYSTEM_VELOCITY_MACROS
                              .getFileName()));
               else if (id.longValue() == PSConfigurationTypes.USER_VELOCITY_MACROS
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.USER_VELOCITY_MACROS
                              .getFileName()));
               else if (id.longValue() == PSConfigurationTypes.AUTH_TYPES
                     .getId())
                  summaries.add(new PSObjectSummary(id,
                        PSConfigurationTypes.AUTH_TYPES.getFileName()));
               else
                  throw new UnsupportedOperationException(
                        "the supplied id specifies an unsupported "
                              + "configuration type: " + id.longValue());

               return summaries;
            }

            default:
               throw new UnsupportedOperationException(
                     "the supplied id specifies an unsupported object type: "
                           + type);
         }
      }
      catch (PSErrorResultsException e)
      {
         // create a new error results exception
         PSErrorResultsException newException = new PSErrorResultsException();

         // copy all success results from the one catched
         Map<IPSGuid, Object> results = e.getResults();
         for (Map.Entry<IPSGuid, Object> ipsGuidObjectEntry : results.entrySet()) {
            Map.Entry entry = ipsGuidObjectEntry;
            newException.addResult((IPSGuid) entry.getKey(), entry.getValue());
         }

         // deal with errors now
         Map<IPSGuid, Object> errors = e.getErrors();
         for (IPSGuid id : errors.keySet())
         {
            Object error = errors.get(id);
            if (error instanceof PSErrorException)
            {
               if (((PSErrorException) error).getCode() != IPSWebserviceErrors.OBJECT_NOT_FOUND)
               {
                  // error is for a persisted object, pass it through
                  newException.addError(id, error);
               }
               else
               {
                  // non-persisted object, add a dummy summary for this
                  PSObjectSummary dummy = new PSObjectSummary(id, "dummy");
                  dummy.setDescription("This must be a non-persisted object.");
                  newException.addResult(id, dummy);
               }
            }
         }

         if (newException.hasErrors())
            throw newException;

         return newException.getResults(ids);
      }
   }

   /**
    * Convenience method that wraps the guid and version in an array and calls
    * {@link #createLocks(List, String, String, List, boolean)}.
    * 
    * @param version May be <code>null</code> if the object doesn't support
    * versions.
    */
   public static void createLock(IPSGuid guid, String session, String user,
         Integer version, boolean overrideLock) throws PSLockErrorException
   {
      createLocks(Collections.singletonList(guid), session, user, Collections
            .singletonList(version), overrideLock);
   }

   /**
    * Create a lock from the specified parameters.
    * 
    * @param guids the GUID property of the lock, never <code>null</code>.
    * @param session the session property of the lock, may be <code>null</code>
    * or empty, in which case returns immediately.
    * @param user the user property of the lock, it may not be <code>null</code>
    * or empty.
    * @param versions the version property of the lock. There must be 1 entry
    * for each entry in <code>guids</code> and the entry may be
    * <code>null</code> if that object doesn't support versions. Never
    * <code>null</code> or empty.
    * @param overrideLock <code>true</code> to override existing locks for the
    * same user but different session.
    * 
    * @throws PSLockErrorException if failed to create a lock. Any successfully
    * created locks will be included in the exception.
    */
   public static void createLocks(List<IPSGuid> guids, String session,
         String user, List<Integer> versions, boolean overrideLock)
      throws PSLockErrorException
   {
      if (guids == null || guids.isEmpty())
         return;
      if (session == null || session.length() == 0)
         throw new IllegalArgumentException(
               "session may not be null or empty.");
      if (user == null || user.length() == 0)
         throw new IllegalArgumentException("user may not be null or empty.");
      if (guids.size() != versions.size())
      {
         throw new IllegalArgumentException(
               "guids and versions list must be the same size");
      }

      IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
      try
      {
         lockService.createLocks(guids, session, user, versions, overrideLock);
      }
      catch (PSLockException e)
      {
         PSLockErrorException error = new PSLockErrorException(e.getResults(),
               e.getErrors());
         throw error;
      }
   }

   /**
    * Calls {@link #createLock(IPSGuid, String, String, Integer, boolean)
    * createLock(guid, session, user, version, false)}
    * 
    * @param guid the GUID property of the lock, never <code>null</code>.
    * @param session the session property of the lock, it may not be
    * <code>null</code> or empty.
    * @param user the user property of the lock, it may not be <code>null</code>
    * or empty.
    * @param version the version property of the lock, it may be
    * <code>null</code>.
    * @throws PSLockErrorException if failed to create a lock.
    */
   public static void createLock(IPSGuid guid, String session, String user,
         Integer version) throws PSLockErrorException
   {
      createLock(guid, session, user, version, false);
   }

   /**
    * Validates supplied parameters. Throws {@link IllegalArgumentException} if
    * one of the parameters violated its contract.
    * 
    * @param objects a list of objects, may not be <code>null</code> or empty.
    * @param objName the name of the above object, assumed not <code>null</code>
    * or empty.
    * @param lock <code>true</code> if need to lock the object.
    * @param session session if the current request, may not be
    * <code>null</code> or empty if the "lock" is <code>true</code>.
    * @param user the login user of the current request, may not be
    * <code>null</code> or empty if the "lock" is <code>true</code>.
    */
   public static void validateParameters(List objects, String objName,
         boolean lock, String session, String user)
   {
      if (objects == null || objects.isEmpty())
         throw new IllegalArgumentException(objName
               + " cannot be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");
   }

   /**
    * This is called when failed to get a lock for the specified id. It records
    * this failure to the specified result object.
    * 
    * @param id the id that failed to get a lock. It may not be
    * <code>null</code>.
    * @param cz the class of the failed object. It may not be <code>null</code>.
    * @param results the object to record the failure. It may be
    * <code>null</code>.
    */
   public static void handleMissingLockError(IPSGuid id, Class cz,
         PSErrorsException results)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null.");
      if (cz == null)
         throw new IllegalArgumentException("cz may not be null.");
      if (results == null)
         throw new IllegalArgumentException("results may not be null.");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();

      PSObjectLock lock = lockService.findLockByObjectId(id, null, null);

      if (lock == null)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
         PSDesignGuid guid = new PSDesignGuid(id);
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, cz.getName(), guid
                     .getValue()), ExceptionUtils
                     .getFullStackTrace(new Exception()));
         results.addError(id, error);
      }
      else
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
         PSDesignGuid guid = new PSDesignGuid(id);
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, cz.getName(), guid
                     .getValue(), lock.getLocker(), lock.getRemainingTime()),
               ExceptionUtils.getFullStackTrace(new Exception()));
         results.addError(id, error);
      }
   }

   /**
    * Extends the lock for the to be saved object. Records the error to the
    * supplied result object should the operation failed.
    * 
    * @param id the id of the to be saved object, may not be <code>null</code>.
    * @param cz the class of the object, may not be <code>null</code>.
    * @param session the rhythmyx session for which to lock the returned
    * objects, may not be <code>null</code>.
    * @param user the user for which to lock the returned objects, may not be
    * <code>null</code>.
    * @param version the version property of the lock, may be <code>null</code>.
    * @param results the object to add the failure info, may not be
    * <code>null</code>.
    */
   public static void extendLock(IPSGuid id, Class cz, String session,
         String user, Integer version, PSErrorsException results)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null.");
      if (cz == null)
         throw new IllegalArgumentException("cz may not be null.");
      if (session == null)
         throw new IllegalArgumentException("session may not be null.");
      if (user == null)
         throw new IllegalArgumentException("user may not be null.");
      if (results == null)
         throw new IllegalArgumentException("results may not be null.");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();

      try
      {
         lockService.extendLock(id, session, user, version);
      }
      catch (PSLockException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSDesignGuid guid = new PSDesignGuid(id);
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, cz.getName(), guid
                     .getValue(), e.getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));
         results.addError(id, error);
      }
   }

   /**
    * This method has been moved
    * 
    * @param id The id to check, may not be <code>null</code>.
    * 
    * @return The error, or <code>null</code> if there are no dependents.
    */
   public static PSErrorException checkDependencies(IPSGuid id)
   {
      PSErrorException error = null;
      String depTypes = PSDesignModelUtils.checkDependencies(id);
      if (depTypes != null)
      {
         int code = IPSWebserviceErrors.DELETE_FAILED_DEPENDENTS;
         error = new PSErrorException(code, PSWebserviceErrors
               .createErrorMessage(code, PSTypeEnum.valueOf(id.getType())
                     .getDisplayName(), id.longValue(), depTypes),
               ExceptionUtils.getFullStackTrace(new Exception()));

      }
      return error;
   }

   /**
    * Checks to see if the supplied associations have any dependents, and if so,
    * returns an appropriate error exception.
    * 
    * @param parent The id of the owner of the associations to check, may not be
    * <code>null</code>.
    * @param children The child ids of the associations to check, may not be
    * <code>null</code> or empty.
    * 
    * @return The error, or <code>null</code> if there are no dependents.
    */
   public static PSErrorException checkAssociationDependencies(IPSGuid parent,
         List<IPSGuid> children)
   {
      PSPair<List<String>, String> pair = PSDesignModelUtils
            .checkAssociationDependencies(parent, children);

      PSErrorException error = null;

      if (!pair.getFirst().isEmpty())
      {
         int code = IPSWebserviceErrors.DELETE_ASSOCIATION_FAILED_DEPENDENTS;
         error = new PSErrorException(code, PSWebserviceErrors
               .createErrorMessage(code, PSTypeEnum.valueOf(parent.getType())
                     .getDisplayName(), parent.longValue(), PSTypeEnum
                     .valueOf(children.get(0).getType()).getDisplayName(),
                     PSStringUtils.listToString(pair.getFirst(), ", "), pair
                           .getSecond()), ExceptionUtils
               .getFullStackTrace(new Exception()));

      }
      return error;
   }

   /**
    * Tests if the supplied session and user has a valid lock to delete the
    * object for the supplied id.
    * 
    * @param id the id of the object to test for, not <code>null</code>.
    * @param session the session to test for, not <code>null</code> or empty.
    * @param user the user to test for, not <code>null</code> or empty.
    * @return true if the referenced object is not locked at all or its locked
    * for the supplied session and user, <code>false</code> otherwise.
    */
   public static boolean hasValidLockForDelete(IPSGuid id, String session,
         String user)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();

      PSObjectLock lock = lockService.findLockByObjectId(id);
      if (lock == null)
         return true;

      return lock.getLockSession().equals(session)
            && lock.getLocker().equals(user);
   }

   /**
    * Releases all locks for the supplied objects, session and user. Does
    * nothing if no corresponding locks exist.
    * 
    * @param ids the ids of the objects for which to delete the locks, not
    * <code>null</code>, may be empty.
    * @param session the session which must own the lock beeing deleted, not
    * <code>null</code> or empty.
    * @param user the user which must own the lock beeing deleted, not
    * <code>null</code> or empty.
    */
   public static void releaseLocks(List<IPSGuid> ids, String session,
         String user)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      if (!ids.isEmpty())
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
               .getLockingService();
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids,
               session, user);
         lockService.releaseLocks(locks);
      }
   }

   /**
    * Loads the summary for the specified item.
    * 
    * @param id the id of the item. It must be an existing item.
    * 
    * @return the loaded summary, never <code>null</code>.
    * 
    * @throws PSErrorException if the specified item does not exist.
    */
   public static PSComponentSummary getItemSummary(int id,boolean refresh)
      throws PSErrorException
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(id,refresh);
      if (summary == null)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     PSComponentSummary.class.getName(), id), ExceptionUtils
                     .getFullStackTrace(new Exception()));
         throw error;
      }

      return summary;
   }

   public static PSComponentSummary getItemSummary(int id)
           throws PSErrorException
   {
      return getItemSummary(id,false);
   }

   /**
    * Gets the locator from the specified item id. If the revision of the id is
    * undefined, <code>-1</code>, then the revision of the locator is the
    * Edit Revision if the item is checked out by the current user; otherwise
    * the revision of the locator is the Current Revision.
    * 
    * @param id the id of the item.
    * @return the locator as described above, never <code>null</code>.
    * 
    * @throws PSErrorException if the specified item does not exist.
    */
   public static PSLocator getItemLocator(PSLegacyGuid id)
      throws PSErrorException
   {
      if (id == null)
         throw new IllegalArgumentException("id must not be null.");

      if (id.getRevision() >= 0)
      {
         return new PSLocator(id.getContentId(), id.getRevision());
      }
      else
      {
         PSComponentSummary summary = getItemSummary(id.getContentId());
         if (isItemCheckedOutToUser(summary))
            return summary.getEditLocator();
         else
            return summary.getCurrentLocator();
      }
   }

   /**
    * Gets the owner or dependent locator from the specified id and relationship
    * config. The revision of the locator will be the Edit Revision if the item
    * is checked out; otherwise the revision will be the Current Revision. The
    * revision of the owner locator will be <code>-1</code> if the
    * {@link PSRelationshipConfig#useOwnerRevision()} is <code>false</code>;
    * the revision of the dependent locator will be <code>-1</code> if the
    * {@link PSRelationshipConfig#useDependentRevision()} is <code>false</code>.
    * 
    * @param id the owner or dependent id, not <code>null</code>. The
    * revision of the id must be either <code>-1</code> or the head revision
    * (described above) if the useXXXRevsion flag is <code>true</code> for the
    * specified relationship type.
    * @param isOwner <code>true</code> if the above is an owner id; otherwise
    * the above is a dependent id.
    * @param config the relationship config, not <code>null</code>.
    * 
    * @return the created locator as described above, never <code>null</code>.
    * 
    * @throws PSErrorException if the specified item does not exist.
    */
   public static PSLocator getHeadLocator(PSLegacyGuid id, boolean isOwner,
         PSRelationshipConfig config) throws PSErrorException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null.");
      if (config == null)
         throw new IllegalArgumentException("config may not be null.");

      boolean useRevision = isOwner ? config.useOwnerRevision() : config
            .useDependentRevision();

      if (useRevision)
      {
         PSComponentSummary summary = getItemSummary(id.getUUID());
         if (id.getRevision() == -1)
            return summary.getHeadLocator();
         else if (id.getRevision() == summary.getHeadLocator().getRevision())
            return id.getLocator();
         else
            throw new IllegalArgumentException("The revision ("
                  + id.getRevision() + ") of the id (" + id.getContentId()
                  + ") must be either -1 or the head revision ("
                  + summary.getHeadLocator() + ").");
      }
      else
      {
         return new PSLocator(id.getContentId(), -1);
      }
   }

   /**
    * Gets the slot or template id from the specified name.
    * 
    * @param name the name of the slot or template. It may not be
    * <code>null</code> or empty.
    * @param isSlot <code>true</code> if getting the slot id; otherwise
    * getting the template id.
    * 
    * @return the specified slot or template, never <code>null</code>.
    * 
    * @throws PSErrorException if the specified slot or template does not exist.
    */
   public static IPSCatalogItem getSlotOrTemplateFromName(String name,
         boolean isSlot) throws PSErrorException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");

      try
      {
         IPSAssemblyService service = PSAssemblyServiceLocator
               .getAssemblyService();
         if (isSlot)
         {
            IPSTemplateSlot slot = service.findSlotByName(name);
            return slot;
         }
         else
         {
            IPSAssemblyTemplate template = service.findTemplateByName(name);
            return template;
         }
      }
      catch (PSAssemblyException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND_BY_NAME;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code, PSSlotType.class
                     .getName(), name), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Creates a relationship processor instance from the current request, which
    * is hidden in the current thread.
    * 
    * @return the created relationship processor, never <code>null</code>.
    */
   public static PSRelationshipProcessor getRelationshipProcessor()
   {
      return PSRelationshipProcessor.getInstance();
   }

   /**
    * Get the current user's request.
    * 
    * @return The request, never <code>null</code>.
    * 
    * @throws IllegalStateException if the current thread has not had a request
    * initialized.
    */
   public static PSRequest getRequest()
   {
      PSRequest request = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      
      if (request == null)
         throw new IllegalStateException(
            "No request initialized for the current thread");
      
      return request;
   }
      
   /**
    * Loads the specified slot.
    * 
    * @param slotId the id of the to be loaded slot.
    * 
    * @return the specified slot, never <code>null</code>.
    * 
    * @throws PSErrorException if failed to load the specified slot.
    */
   public static IPSTemplateSlot loadSlot(IPSGuid slotId)
      throws PSErrorException
   {
      if (slotId == null)
         throw new IllegalArgumentException("slotId must not be null.");

      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      try
      {
         return service.loadSlot(slotId);
      }
      catch (PSAssemblyException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSTemplateSlot.class.getName(), slotId), ExceptionUtils
                     .getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Loads the specified template.
    * 
    * @param templateId the id of the to be loaded template, never
    * <code>null</code>.
    * @param loadSlots pass <code>true</code> if the related slot objects
    * should be loaded with the template, otherwise they may not be accessed
    * from the loaded template object.
    * 
    * @return the specified template, never <code>null</code>.
    * 
    * @throws PSErrorException if the template does not exist.
    */
   public static IPSAssemblyTemplate loadTemplate(IPSGuid templateId,
         boolean loadSlots) throws PSErrorException
   {
      if (templateId == null)
         throw new IllegalArgumentException("templateId must not be null.");

      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      try
      {
         return service.loadTemplate(templateId, loadSlots);
      }
      catch (PSAssemblyException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSDesignGuid guid = new PSDesignGuid(templateId);
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSAssemblyTemplate.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Loads the specified template which will not be modified.
    * 
    * @param templateId the id of the to be loaded template, never
    * <code>null</code>.
    * 
    * @return the specified template, never <code>null</code>.
    * 
    * @throws PSErrorException if the template does not exist.
    */
   public static IPSAssemblyTemplate loadUnmodifiableTemplate(
         IPSGuid templateId) throws PSErrorException
   {
      if (templateId == null)
         throw new IllegalArgumentException("templateId must not be null.");

      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      try
      {
         return service.loadUnmodifiableTemplate(templateId);
      }
      catch (PSAssemblyException e)
      {
         int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
         PSDesignGuid guid = new PSDesignGuid(templateId);
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     IPSAssemblyTemplate.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Gets the next available relationship id.
    * 
    * @return the next available id, never <code>null</code>.
    * 
    * @throws IllegalStateException if failed to get the next id.
    */
   public static int getNextRelationshipId()
   {
      try
      {
         return PSRelationshipCommandHandler.getNextId();
      }
      catch (PSCmsException e)
      {
         // not possible in a healthy server
         e.printStackTrace();
         throw new IllegalStateException("Failed to get next relationship id",
               e);
      }
   }

   /**
    * Validates if the specified id is an instance of {@link PSLegacyGuid}.
    * 
    * @param id the id in question.
    * 
    * @throws IllegalArgumentException if the specified id is <code>null</code>
    * or is not {@link PSLegacyGuid}.
    */
   public static void validateLegacyGuid(IPSGuid id)
   {
      if (id == null || (!(id instanceof PSLegacyGuid)))
         throw new IllegalArgumentException("id must be an instance of "
               + PSLegacyGuid.class.getName());
   }

   /**
    * Just like {@link #validateLegacyGuid(IPSGuid)}, except it validates a
    * list of ids.
    * 
    * @param ids the list of ids in question.
    * 
    * @throws IllegalArgumentException if the specified ids is <code>null</code>,
    * empty or is not {@link PSLegacyGuid}.
    */
   public static void validateLegacyGuids(List<IPSGuid> ids)
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids must not be null or empty.");
      for (IPSGuid id : ids)
         validateLegacyGuid(id);
   }

   /**
    * Saves a specified relationship.
    * 
    * @param rel the to be saved relationship, never <code>null</code>.
    * 
    * @throws PSErrorException if failed to save the relationship.
    */
   public static void saveRelationship(PSRelationship rel)
      throws PSErrorException
   {
      if (rel == null)
         throw new IllegalArgumentException("rel must not be null.");

      try
      {
         PSRelationshipSet rels = new PSRelationshipSet();
         rels.add(rel);
         getRelationshipProcessor().save(rels);
         PSFolderRelationshipCache cache = PSFolderRelationshipCache.getInstance();
         cache.update(rels);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();

         int errorCode = IPSWebserviceErrors.FAILED_SAVE_RELATIONSHIPS;
         PSErrorException error = new PSErrorException(errorCode,
               PSWebserviceErrors.createErrorMessage(errorCode, e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));

         throw error;
      }

   }

   /**
    * Saves specified relationships. Do nothing if the specified relationships
    * is empty.
    * 
    * @param rels the to be saved relationships, never <code>null</code>, be
    * may empty.
    * 
    * @throws PSErrorsException if failed to save any of the specified
    * relationships.
    */
   public static void saveRelationships(List<PSRelationship> rels)
      throws PSErrorsException
   {
      PSErrorsException results = new PSErrorsException();
      for (PSRelationship rel : rels)
      {
         try
         {
            saveRelationship(rel);
            results.addResult(rel.getGuid());
         }
         catch (PSErrorException e)
         {
            results.addError(rel.getGuid(), e);
         }
      }
      if (results.hasErrors())
         throw results;
   }

   /**
    * Saves the supplied Active Assembly relationships.
    * 
    * @param rels the to be saved relationships; Never <code>null</code>, but
    * it may be empty.
    * 
    * @throws PSErrorException if failed to save the supplied relationships.
    */
   public static void saveAaRelationships(List<PSAaRelationship> rels)
      throws PSErrorException
   {
      if (rels == null)
         throw new IllegalArgumentException("rels may not be null.");
      if (rels.isEmpty())
         return;

      try
      {
         PSRelationshipSet relset = new PSRelationshipSet();
         relset.addAll(rels);
         getRelationshipProcessor().save(relset);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();

         int errorCode = IPSWebserviceErrors.FAILED_SAVE_RELATIONSHIPS;
         PSErrorException error = new PSErrorException(errorCode,
               PSWebserviceErrors.createErrorMessage(errorCode, e
                     .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));

         throw error;
      }
   }

   /**
    * Deletes the specified relationships.
    * 
    * @param ids the ids of deleted relationship, never <code> or empty.
    * @param validateCheckedOut <code>true</code> to validate the owner of the 
    * relationship is checked out by the current user, <code>false</code> 
    * otherwise.
    * 
    * @throws PSErrorsException if failed to remove any specified relationships
    * @throws PSErrorException if any error occurs.
    */
   public static void deleteRelationships(List<IPSGuid> ids,
         boolean validateCheckedOut)
      throws PSErrorException, PSErrorsException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids may not be null or empty.");

      List<PSRelationship> relationships = new ArrayList<>();
      PSErrorsException results = new PSErrorsException();

      // load the relationship instances from the ids
      IPSRelationshipService relsvc = PSRelationshipServiceLocator
            .getRelationshipService();
      PSRelationship rel;
      for (IPSGuid id : ids)
      {
         try
         {
            rel = relsvc.loadRelationship(id.getUUID());
            if (validateCheckedOut)
               validateItemCheckoutByUser(rel.getOwner());
         }
         catch (PSException e)
         {
            e.printStackTrace();

            int errorCode = IPSWebserviceErrors.FAILED_LOAD_RELATIONSHIP;
            PSErrorException error = new PSErrorException(errorCode,
                  PSWebserviceErrors.createErrorMessage(errorCode, id
                        .longValue(), e.getLocalizedMessage()), ExceptionUtils
                        .getFullStackTrace(e));

            throw error;
         }
         if (rel == null)
         {
            int errorCode = IPSWebserviceErrors.CANNOT_FIND_RELATIONSHIP;
            PSErrorException error = new PSErrorException(errorCode,
                  PSWebserviceErrors.createErrorMessage(errorCode, id
                        .longValue()), ExceptionUtils
                        .getFullStackTrace(new Exception()));

            throw error;
         }
         relationships.add(rel);
      }

      // Delete the relationship instances one at a time.
      // Must use the PSRelationshipProcessor, so that it will also
      // update the folder relationship cache accordingly.
      for (PSRelationship r : relationships)
      {
         try
         {
            PSRelationshipSet rels = new PSRelationshipSet();
            rels.add(r);
            PSWebserviceUtils.getRelationshipProcessor().delete(rels);
            results.addResult(r.getGuid());
         }
         catch (PSCmsException e)
         {
            e.printStackTrace();
            int errorCode = IPSWebserviceErrors.FAILED_DELETE_RELATIONSHIPS;
            PSErrorException error = new PSErrorException(errorCode,
                  PSWebserviceErrors.createErrorMessage(errorCode, e
                        .getLocalizedMessage()), ExceptionUtils
                        .getFullStackTrace(e));

            results.addError(r.getGuid(), error);
         }
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * Validates the specified item has been checked out by the current user.
    * 
    * @param locator the locator of the specified item. The revision of it must
    * be the edit revision; otherwise it must be <code>-1</code>.
    * @return the locator of the item. The revision of the item is always the
    * edit revision.
    * 
    * @throws PSErrorException the validation failed or any error occurs.
    */
   public static PSLocator validateItemCheckoutByUser(PSLocator locator)
      throws PSErrorException
   {
      if (locator == null)
         throw new IllegalArgumentException("locator must not be null.");

      PSComponentSummary summary = PSWebserviceUtils.getItemSummary(locator
            .getId());
      if (!isItemCheckedOutToUser(summary))
      {
         int errorCode = IPSWebserviceErrors.ITEM_NOT_CHECKOUT_BY_USER;
         PSErrorException error = new PSErrorException(errorCode,
               PSWebserviceErrors.createErrorMessage(errorCode, locator
                     .getId(), getUserName()), ExceptionUtils
                     .getFullStackTrace(new Exception()));
         throw error;
      }
      else
      {
         PSLegacyGuid guid = new PSLegacyGuid(locator);
         return handleRevision(guid, summary).getLocator();
      }
   }

   /**
    * Checks the revision of the supplied guid. If it is -1, then returns a guid
    * with the edit revision if the item is checked out by the current user, and
    * the current revision if the item is not checked out by the current user.
    * If a revision is specified, then it is validated to be the edit revision
    * if the user has the item checked out, and the current revision if the user
    * does not have the item checked out.
    * 
    * @param lguid The guid to validate, may not be <code>null</code>.
    * @param summary the summary of the specified item, may not be
    * <code>null</code>.
    * @return A guid with the correct revision if one was not supplied.
    * 
    * @throws IllegalArgumentException if the revision is specified and it does
    * not pass validation.
    */
   public static PSLegacyGuid handleRevision(PSLegacyGuid lguid,
         PSComponentSummary summary)
   {
      if (lguid == null)
         throw new IllegalArgumentException("lguid may not be null.");

      if (summary == null)
         throw new IllegalArgumentException("summary may not be null.");

      int revision = lguid.getRevision();
      int editRev = summary.getEditLocator().getRevision();
      int curRev = summary.getCurrentLocator().getRevision();

      boolean checkedOutByUser = isItemCheckedOutToUser(summary);

      if (revision == -1)
      {
         if (checkedOutByUser)
            revision = editRev;
         else
            revision = curRev;
      }
      else
      {
         // if checkout by user must be edit rev
         if (checkedOutByUser && revision != editRev)
         {
            throw new IllegalArgumentException(PSWebserviceErrors
                  .createErrorMessage(
                        IPSWebserviceErrors.INVALID_EDIT_REVISION, lguid
                              .getContentId(), revision, getUserName()));
         }
         // if not checked, must be current rev
         else if ((!checkedOutByUser) && revision != curRev)
         {
            throw new IllegalArgumentException(PSWebserviceErrors
                  .createErrorMessage(
                        IPSWebserviceErrors.INVALID_CURRENT_REVISION, lguid
                              .getContentId(), revision, getUserName()));
         }
      }

      return new PSLegacyGuid(lguid.getContentId(), revision);
   }

   /**
    * Helper method to check if the supplied item is checked out to the current
    * user. Check is done by comparing the checkout user name in the item
    * summary to the current user. Additionally, if the current user is internal
    * user, then it compares the checkout user name with the logged in user (on
    * whose behalf the internal user is operating) name. Comparison is case
    * insensitive.
    * 
    * @param item component summary of the item to check, must not be
    * <code>null</code>
    * @return <code>true</code> if item is checked out to the supplied user,
    * <code>false</code> otherwise.
    */
   public static boolean isItemCheckedOutToUser(PSComponentSummary item)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }

      // is the item workflowable
      PSCmsObject obj = PSServer.getCmsObject(item.getObjectType());
      if (!obj.isWorkflowable())
         return true; // ignore check in/out if not workflowable
      
      String user = getUserName();
      boolean checkedOutByUser = user.equalsIgnoreCase(item
            .getCheckoutUserName());
      // If not checkedout to supplied user and the current is the rxserver
      // then check if it is checked out to the original logged in user. This is
      // required specially when this is invoked by an effect
      if (!checkedOutByUser
            && user.equals(PSSecurityProvider.INTERNAL_USER_NAME))
      {
         PSRequest req = getRequest();
         String originalUser = "";
         PSUserEntry[] entries = req.getUserSession()
               .getAuthenticatedUserEntries();
         if (entries.length > 0)
            originalUser = entries[0].getName();
         checkedOutByUser = originalUser.equalsIgnoreCase(item
               .getCheckoutUserName());
      }
      return checkedOutByUser;
   }
   
   /**
    * Helper method to check if the supplied item is checked out to the someone else, apart of current user
    * user. Check is done by comparing the checkout user name in the item
    * summary to the current user. Additionally, if the current user is internal
    * user, then it compares the checkout user name with the logged in user (on
    * whose behalf the internal user is operating) name. Comparison is case
    * insensitive.
    * 
    * @param item component summary of the item to check, must not be
    * <code>null</code>
    * @return <code>true</code> if item is checked out to someone else, distinct of current user,
    * <code>false</code> otherwise.
    */
   public static boolean isItemCheckedOutToSomeoneElse(PSComponentSummary item)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (StringUtils.isBlank(item.getCheckoutUserName()) || isItemCheckedOutToUser(item))
         return false;
      
      return true;
   }


   /**
    * Loads specified relationships.
    * 
    * @param filter the select criteria for the loaded relationships, never
    * <code>null</code>.
    * 
    * @return the relationships that meet the specified criteria, never
    * <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if failed to load relationships.
    */
   public static List<PSRelationship> loadRelationships(
         PSRelationshipFilter filter) throws PSErrorException
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null.");

      List<PSRelationship> rels;
      try
      {
         rels = getRelationshipProcessor().getRelationshipList(filter);
         return rels;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.LOAD_OBJECTS_ERROR;
         PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                     PSRelationship.class.getName(), filter.toString(), e
                           .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Converts the specified AA Relationship Filter to
    * {@link PSRelationshipFilter}.
    * <p>
    * Note, the owner revision will be <code>-1</code> if the
    * isLimitToOwnerRevisions of the source filter is <code>false</code>;
    * otherwise, the owner revision will be the Edit (or Tip) revision if the
    * item is checked out by the current user; otherwise the owner revision is
    * the current revision of the owner item.
    * 
    * @param src the to be converted AA Filter, may be <code>null</code>.
    * @return the converted filter, never <code>null</code>.
    * 
    * @throws PSErrorException if any of properties in the source filter is
    * invalid.
    */
   public static PSRelationshipFilter getRelationshipFilter(
         com.percussion.webservices.common.RelationshipFilter src)
      throws PSErrorException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      if (src == null)
         return filter;

      // set relationship id
      if (src.getId() != null)
      {
         PSGuid id = new PSGuid(PSTypeEnum.RELATIONSHIP, src.getId()
               .longValue());
         filter.setRelationshipId(id.getUUID());
      }
      // set owner
      if (src.getOwner() != null)
      {
         PSLegacyGuid id = new PSLegacyGuid(src.getOwner().longValue());
         PSLocator owner;
         if (src.isLimitToOwnerRevisions())
            owner = getItemLocator(id);
         else
            owner = new PSLocator(id.getContentId(), -1);
         filter.setOwner(owner);
      }
      // set dependents
      if (src.getDependent() != null && src.getDependent().length > 0)
      {
         List<PSLocator> ids = new ArrayList<>();
         for (long id : src.getDependent())
         {
            PSLegacyGuid guid = new PSLegacyGuid(id);
            ids.add(guid.getLocator());
         }
         filter.setDependents(ids);
      }

      // set owner content type
      if (src.getOwnerContentType() != null)
      {
         PSGuid id = new PSGuid(PSTypeEnum.NODEDEF, src.getOwnerContentType()
               .getId());
         filter.setOwnerContentTypeId(id.getUUID());
      }

      // set dependent content types
      if (src.getDependentContentType() != null
            && src.getDependentContentType().length > 0)
      {
         List<Long> contentTypes = new ArrayList<>();
         for (Reference contentType : src.getDependentContentType())
         {
            PSGuid id = new PSGuid(PSTypeEnum.NODEDEF, contentType.getId());
            contentTypes.add(id.longValue());
         }
         filter.setDependentContentTypeIds(contentTypes);
      }

      // set owner object type
      if (src.getOwnerObjectType() != null)
      {
         if (src.getOwnerObjectType().getValue().equals(ObjectType._item))
            filter.setOwnerObjectType(PSCmsObject.TYPE_ITEM);
         else
            filter.setOwnerObjectType(PSCmsObject.TYPE_FOLDER);
      }

      // set dependent object type
      if (src.getDependentObjectType() != null)
      {
         if (src.getDependentObjectType().getValue().equals(ObjectType._item))
            filter.setDependentObjectType(PSCmsObject.TYPE_ITEM);
         else
            filter.setDependentObjectType(PSCmsObject.TYPE_FOLDER);
      }

      // set relationship type (system / user)
      if (src.getRelationshipType() != null)
      {
         RelationshipFilterRelationshipType type = src.getRelationshipType();
         if (type.getValue()
               .equals(RelationshipFilterRelationshipType._system))
            filter.setType(PSRelationshipFilter.FILTER_TYPE_SYSTEM);
         else
            filter.setType(PSRelationshipFilter.FILTER_TYPE_USER);
      }

      // set relationship category, if supplied
      if (src instanceof com.percussion.webservices.system.PSRelationshipFilter)
      {
         com.percussion.webservices.system.PSRelationshipFilter f = (com.percussion.webservices.system.PSRelationshipFilter) src;
         if (f.getCategory() != null)
         {
            filter.setCategory(ms_wsCategoryToRelationshipCategory.get(f
                  .getCategory()));
         }
      }

      // set all boolean properties
      filter.setCommunityFiltering(src.isEnableCommunityFilter());
      filter.limitToOwnerRevision(src.isLimitToOwnerRevisions());
      filter.limitToEditOrCurrentOwnerRevision(src
            .isLimitToEditOrCurrentOwnerRevision());

      // set relationship names
      if (src.getConfigurations() != null
            && src.getConfigurations().length != 0)
      {
         filter.setNames(Arrays.asList(src.getConfigurations()));
      }

      // set properties
      if (src.getProperties() != null && src.getProperties().length != 0)
      {
         for (Property prop : src.getProperties())
            filter.setProperty(prop.getName(), prop.getValue());
      }

      return filter;
   }

   /**
    * Loads the workflow with the specified workflow id.
    * 
    * @param workflowId the workflow id.
    * 
    * @return the specified workflow, never <code>null</code>.
    * 
    * @throws PSErrorException if failed to load the specified workflow.
    */
   public static PSWorkflow getWorkflow(int workflowId)
      throws PSErrorException
   {
      IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      PSWorkflow wf = null;
      try{
          wf = svc.loadWorkflow(gmgr.makeGuid(workflowId,
            PSTypeEnum.WORKFLOW));
      }catch(Exception e){
         log.error("Unable to load workflow with id: {} Error:{}" , workflowId,
                 PSExceptionUtils.getMessageForLog(e));
      }
      
      if (wf == null)
      {
         throw new PSErrorException(IPSWebserviceErrors.FAILED_LOAD_WORKFLOW,
               PSWebserviceErrors.createErrorMessage(IPSWebserviceErrors.FAILED_LOAD_WORKFLOW,
                     workflowId), ExceptionUtils
                     .getFullStackTrace(new Exception()));
      }

      return wf;
   }

   /**
    * Gets the specified state from the specified workflow.
    * 
    * @param wf the workflow, assumed not <code>null</code>.
    * @param id the if the of searched state.
    * 
    * @return the state with the specified id, never <code>null</code>.
    * 
    * @throws PSErrorException if cannot find the specified state.
    */
   public static PSState getStateById(PSWorkflow wf, long id)
      throws PSErrorException
   {
      for (PSState s : wf.getStates())
      {
         if (id == s.getStateId())
            return s;
      }

      throw new PSErrorException(IPSWebserviceErrors.CANNOT_FIND_WORKFLOW_STATE_ID,
            PSWebserviceErrors.createErrorMessage(IPSWebserviceErrors.CANNOT_FIND_WORKFLOW_STATE_ID, id, wf.getGUID()
                  .longValue(), wf.getName()), ExceptionUtils
                  .getFullStackTrace(new Exception()));
   }

   /**
    * Transitions the specified item according to the specified trigger.
    * 
    * @param id the to be transitioned item.
    * @param trigger the trigger of the transition.
    * @param comment the comment for this transition. It may be
    * <code>null</code> or empty.
    * @param addhocUsers the list of add hoc users. It may be <code>null</code>
    * or empty.
    * 
    * @throws PSErrorException if the transition failed.
    */
   public static void transitionItem(int id, String trigger, String comment,
         List<String> addhocUsers) throws PSErrorException
   {
      PSWebServicesRequestHandler ws = PSWebServicesRequestHandler
            .getInstance();

      PSRequest req = getRequest();

      Map<String, Object> oldParams = req.getParameters();
      req.setParameters( Collections.synchronizedMap(new HashMap<>()));

      String addhocList = null;
      // concatenate a list of user names with ';' delimiter
      if (addhocUsers != null && (!addhocUsers.isEmpty()))
      {
         StringBuilder users = new StringBuilder();
         for (int i = 0; i < addhocUsers.size(); i++)
         {
            if (i > 0)
               users.append(";");
            users.append(addhocUsers.get(i));
         }
         addhocList = users.toString();
      }

      try
      {
         req.setParameter(IPSHtmlParameters.SYS_CONTENTID, Integer
               .toString(id));
         ws.transitionItem(req, trigger, comment, addhocList);
      }
      catch (PSException e)
      {
         Throwable rootCause = PSExceptionHelper.findRootCause(e,false);
         PSErrorException error = new PSErrorException(IPSWebserviceErrors.FAILED_TRANSITION_ITEM,
               PSWebserviceErrors.createErrorMessage(IPSWebserviceErrors.FAILED_TRANSITION_ITEM, trigger, id),
               ExceptionUtils.getFullStackTrace(rootCause),e);
         throw new PSErrorException("Failed transition due to server error.",error);

      }
      finally
      {
         req.setParameters(oldParams);
      }
   }

   /**
    * Gets the actual long value for a list of specified GUID objects.
    * 
    * @param ids the list of GUID objects, not <code>null</code>, may be
    * empty.
    * 
    * @return the actual long values of the GUIDs, never <code>null</code>,
    * but may be empty.
    */
   public static long[] getLongsFromGuids(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null.");

      long[] result = new long[ids.size()];
      for (int i = 0; i < result.length; i++)
         result[i] = new PSDesignGuid(ids.get(i)).getValue();

      return result;
   }

   /**
    * Converts the specified Guid values to a list of {@link PSLegacyGuid}
    * objects.
    * 
    * @param longIds a list of Guid values, it may not be <code>null</code> or
    * empty.
    * 
    * @return the converted {@link PSLegacyGuid} objects, never
    * <code>null</code> or empty.
    */
   public static List<IPSGuid> getLegacyGuidFromLong(long[] longIds)
   {
      if (longIds == null || longIds.length == 0)
         throw new IllegalArgumentException(
               "longIds must not be null or empty.");

      List<IPSGuid> ids = new ArrayList<>(longIds.length);
      for (long guidId : longIds)
         ids.add(new PSLegacyGuid(guidId));

      return ids;
   }

   /**
    * Converts the supplied array of long ids to a list of design guids.
    * 
    * @param ids the array of ids to convert, not <code>null</code> or empty.
    * @return a list of design guids, never <code>null</code> or empty.
    */
   public static List<IPSGuid> getDesignGuidsFromLongs(long[] ids)
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      List<IPSGuid> guids = new ArrayList<>(ids.length);
      for (long id : ids)
         guids.add(new PSDesignGuid(id));

      return guids;
   }

   /**
    * Load all ACL's for the specified ids.
    * 
    * @param aclIds list of ids of the ACL's to be loaded, must not be
    * <code>null</code> or empty.
    * @return list of ACL objects corresponding to the supplied ACL ids, never
    * <code>null</code> or empty.
    * @throws PSErrorResultsException if any ACL object corresponding to ACL id
    * is not found in the system. Each entry in the result will be the ACL
    * object if loaded with success or exception if failed to load.
    */
   public static List<PSAclImpl> loadAcls(List<IPSGuid> aclIds)
      throws PSErrorResultsException
   {
      if (aclIds == null || aclIds.isEmpty())
         throw new IllegalArgumentException("aclIds cannot be null or empty");

      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<PSAclImpl> aclList = new ArrayList<>(aclIds.size());
      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : aclIds)
      {
         PSAclImpl acl = null;
         try
         {
            acl = (PSAclImpl) aclService.loadAclForObject(id);
            if (acl == null)
               throw new RuntimeException("Object not found");
            results.addResult(id, acl);
            aclList.add(acl);
         }
         catch (RuntimeException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code, PSAclImpl.class
                        .getName(), id.longValue()), ExceptionUtils
                        .getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;

      return aclList;
   }

   /**
    * Remove slot associations that reference the supplied id. If a slot is to
    * be modified and it is not already locked by the specified session and
    * user, it is locked, modified, and then unlocked. Pre-existing locks are
    * not released.
    * 
    * @param id The guid to check for associations to. If it specifies a content
    * type or template to which a slot has any associations, the slot will be
    * modified to remove the association. May not be <code>null</code>.
    * @param session The session used for locking purposes, may not be
    * <code>null</code> or empty.
    * @param user The user name to use for locking purposes, may not be
    * <code>null</code> or empty.
    * 
    * @throws PSLockException If a necessary lock on a slot cannot be obtained.
    * @throws PSAssemblyException If there are any errors saving a modified
    * slot.
    */
   public static void removeSlotAssocations(IPSGuid id, String session,
         String user) throws PSLockException, PSAssemblyException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session may not be null or empty");

      if (StringUtils.isBlank(user))
         throw new IllegalArgumentException("user may not be null or empty");

      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      List<IPSTemplateSlot> allSlots = service.findSlotsByName(null);
      List<IPSTemplateSlot> modSlots = new ArrayList<>();
      for (IPSTemplateSlot slot : allSlots)
      {
         Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = slot
               .getSlotAssociations();
         Iterator<PSPair<IPSGuid, IPSGuid>> iter = slotAssociations.iterator();
         boolean modified = false;
         while (iter.hasNext())
         {
            PSPair<IPSGuid, IPSGuid> assoc = iter.next();
            if (id.equals(assoc.getFirst()) || id.equals(assoc.getSecond()))
            {
               iter.remove();
               modified = true;
            }
         }
         if (modified)
         {
            slot.setSlotAssociations(slotAssociations);
            modSlots.add(slot);
         }
      }

      if (!modSlots.isEmpty())
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator
               .getLockingService();
         List<IPSGuid> slotLocks = new ArrayList<>();
         try
         {
            for (IPSTemplateSlot slot : modSlots)
            {
               IPSGuid slotGuid = slot.getGUID();
               if (!lockService.isLockedFor(slotGuid, session, user))
               {
                  lockService.createLock(slotGuid, session, user,
                        ((PSTemplateSlot) slot).getVersion(), false);
                  slotLocks.add(slotGuid);
               }
            }

            // now save the slots
            for (IPSTemplateSlot slot : modSlots)
            {
               service.saveSlot(slot);
            }
         }
         finally
         {
            if (!slotLocks.isEmpty())
            {
               List<PSObjectLock> locks = lockService.findLocksByObjectIds(
                     slotLocks, session, user);
               lockService.releaseLocks(locks);
            }
         }
      }
   }

   /**
    * Throws {@link IllegalArgumentException} for the duplicated name and type.
    * 
    * @param name the name of the existing object; it may not be
    * <code>null</code> or empty.
    * @param type the type of the existing object; it may not be
    * <code>null</code>.
    */
   public static void throwObjectExistException(String name, PSTypeEnum type)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null or empty.");
      if (type == null)
         throw new IllegalArgumentException("type must not be null.");

      int code = IPSWebserviceErrors.OBJECT_ALREADY_EXISTS;
      throw new IllegalArgumentException(PSWebserviceErrors
            .createErrorMessage(code, type, name));
   }

   /**
    * Gets the community ID from current user session.
    * 
    * @return the community ID. 
    * 
    * @throws PSErrorException if current session does not have a community.
    */
   public static int getUserCommunityId() throws PSErrorException
   {
      String usercomm = (String) getRequest().getUserSession()
            .getPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
      if (StringUtils.isNotBlank(usercomm))
      {
         int communityId = Integer.parseInt(usercomm);
         return communityId;
      }
      else
      {
         PSErrorException e = new PSErrorException();
         e.setErrorMessage("Cannot find community from current session.");
         throw e;
      }
   }
   
   /**
    * Gets a list of role names that the login user is a member of.
    * 
    * @return the list of roles, never <code>null</code>, may be empty.
    */
   public static List<String> getUserRoles()
   {
      PSRequest req = getRequest();
      IPSRequestContext ctx = new PSRequestContext(req);
      List<String> roles = new ArrayList<>();
      roles.addAll(ctx.getSubjectRoles());
      return roles;

   }
   /**
    * Get the current user's name.
    * 
    * @return The name, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the current thread's request info does
    *             not have a user name set on it.
    */
   public static String getUserName()
   {
      String user = (String) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_USER);

      if (StringUtils.isBlank(user))
         throw new IllegalStateException(
               "No user name set in current request information");

      return user;
   }
   
   
   /**
    * Set the current user's name.
    * 
    * @param userName the user name.
    * 
    */
   public static void setUserName(String userName)
   {
      notEmpty(userName, "userName");
      PSRequestInfo
            .setRequestInfo(PSRequestInfo.KEY_USER, userName);
   }

   /**
    * Saves the specified relationship configurations into the repository
    *  
    * @param configSet the to be saved relationship config set, may not be 
    *    <code>null</code>.
    * @param errorCode the error code should an error occurs while saving
    *    the relationship configs.
    * 
    * @throws PSErrorException
    */
   public static void saveRelationshipConfigSet(
      PSRelationshipConfigSet configSet, int errorCode) throws PSErrorException
   {
      if (configSet == null)
         throw new IllegalArgumentException("configSet may not be null");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = configSet.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, root);
      try
      {
         PSConfigManager.getInstance().saveConfig(
            PSConfigurationFactory.RELATIONSHIPS_CFG, doc);

         // reset the cached relationship configs
         PSRelationshipCommandHandler.reloadConfigs();
         IPSRelationshipService relsvc = PSRelationshipServiceLocator
            .getRelationshipService();
         relsvc.reloadConfigs();
      }
      catch (PSException e)
      {
         throw new PSErrorException(errorCode,
            PSWebserviceErrors.createErrorMessage(errorCode, e
               .getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));

      }
   }
   
   /**
    * Check if there is a root cause of an exception and if so if it is an
    * instance of {@link PSException} that matches a specified error code.
    * 
    * @param e The error to check, may not be <code>null</code>.
    * @param errorCode The code to match
    * 
    * @return <code>true</code> if the root cause matches, <code>false</code>
    *         otherwise.
    */
   public static boolean isRootCauseOfType(PSErrorException e, int errorCode)
   {
      Throwable rootCause = PSExceptionHelper.findRootCause(e, true);      
      return rootCause instanceof PSException && ((PSException) rootCause).getErrorCode() == errorCode;
   }

   
   /**
    * This map is used to convert the relationship filter string used with
    * category filtering from the web service form to the internal relationship
    * engine form. The key is one of the
    * <code>PSRelationshipFilterCategory.xxx</code> values and the value is
    * the corresponding <code>PSRelationshipFilter.FILTER_CATEGORY_xxx</code>
    * value.
    * <p>
    * Initialized when class is loaded, then never modified.
    */
   private static final Map<PSRelationshipFilterCategory, String> ms_wsCategoryToRelationshipCategory = new HashMap<>();

   static
   {
      ms_wsCategoryToRelationshipCategory.put(
            PSRelationshipFilterCategory.activeassembly,
            PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      ms_wsCategoryToRelationshipCategory.put(
            PSRelationshipFilterCategory.newcopy,
            PSRelationshipFilter.FILTER_CATEGORY_COPY);
      ms_wsCategoryToRelationshipCategory.put(
            PSRelationshipFilterCategory.foldercontent,
            PSRelationshipFilter.FILTER_CATEGORY_FOLDER);
      ms_wsCategoryToRelationshipCategory.put(
            PSRelationshipFilterCategory.promotableversion,
            PSRelationshipFilter.FILTER_CATEGORY_PROMOTABLE);
      ms_wsCategoryToRelationshipCategory.put(
            PSRelationshipFilterCategory.translation,
            PSRelationshipFilter.FILTER_CATEGORY_TRANSLATION);
   }
}
