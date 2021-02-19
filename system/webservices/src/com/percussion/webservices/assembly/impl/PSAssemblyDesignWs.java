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
package com.percussion.webservices.assembly.impl;

import com.percussion.services.assembly.*;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManagerInternal;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.*;
import com.percussion.webservices.assembly.IPSAssemblyDesignWs;
import com.percussion.webservices.assembly.IPSAssemblyWs;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * The private assembly design webservice implementations.
 */
@Transactional
@PSBaseBean("sys_assemblyDesignWs")
public class PSAssemblyDesignWs extends PSAssemblyBaseWs implements
   IPSAssemblyDesignWs, IPSNotificationListener
{
   // @see IPSAssemblyDesignWs#createAssemblyTemplates(List, String, String)
   public List<PSAssemblyTemplateWs> createAssemblyTemplates(
      List<String> names, String session, String user)
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<IPSAssemblyTemplate> templates = new ArrayList<>();
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         if (StringUtils.contains(name, ' '))
            throw new IllegalArgumentException("name cannot contain spaces");

         IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

         IPSAssemblyTemplate existing = null;
         try
         {
            existing = service.findTemplateByName(name);
         }
         catch (PSAssemblyException e)
         {
            // expected exception when cannot find the specified template
         }
         if (existing != null)
         {
            PSWebserviceUtils.throwObjectExistException(name,
               PSTypeEnum.TEMPLATE);
         }

         IPSAssemblyTemplate template = service.createTemplate();
         template.setName(name);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService.createLock(template.getGUID(), session, user,
               ((PSAssemblyTemplate) template).getVersion(), false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         templates.add(template);
      }

      return getTemplateWs(templates);
   }

   // @see IPSAssemblyDesignWs#createSlots(List<String>, String, String)
   public List<IPSTemplateSlot> createSlots(List<String> names, String session,
      String user)
   {
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      List<IPSTemplateSlot> slots = new ArrayList<IPSTemplateSlot>();
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         if (StringUtils.contains(name, ' '))
            throw new IllegalArgumentException("name cannot contain spaces");

         IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

         IPSTemplateSlot existing = null;
         try
         {
            existing = service.findSlotByName(name);
         }
         catch (PSAssemblyException e)
         {
            // ignore
         }
         if (existing != null)
         {
            PSWebserviceUtils.throwObjectExistException(name, PSTypeEnum.SLOT);
         }

         PSTemplateSlot slot = (PSTemplateSlot) service.createSlot();
         slot.setName(name);

         IPSObjectLockService lockService = PSObjectLockServiceLocator
            .getLockingService();
         try
         {
            lockService.createLock(slot.getGUID(), session, user, slot
               .getVersion(), false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         slots.add(slot);
      }

      return slots;
   }

   /*
    * @see IPSAssemblyDesignWs#deleteAssemblyTemplates(List, boolean, String,
    *    String)
    */
   public void deleteAssemblyTemplates(List<IPSGuid> ids,
      boolean ignoreDependencies, String session, String user)
      throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSAssemblyService service = PSAssemblyServiceLocator
         .getAssemblyService();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         try
         {
            if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
            {
               boolean exists = false;
               try
               {
                  service.loadTemplate(id, false);
                  exists = true;
               }
               catch (PSAssemblyException e)
               {
                  // ignore, just means that the template does not exist
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

                  deleteTemplateWs(id, session, user);
               }

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id,
                  IPSAssemblyTemplate.class, results);
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  IPSAssemblyTemplate.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   // @see IPSAssemblyDesignWs#deleteSlots(List, boolean, String, String)
   public void deleteSlots(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSAssemblyService service = PSAssemblyServiceLocator
         .getAssemblyService();
      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      PSErrorsException results = new PSErrorsException();

      synchronized (SYNC_SAVE_DELETE_TEMPLATE_AND_SLOT)
      {
         for (IPSGuid id : ids)
         {
            try
            {
               Map<IPSGuid, IPSAssemblyTemplate> templateMap = new HashMap<IPSGuid, IPSAssemblyTemplate>();

               if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
               {
                  IPSTemplateSlot slot = service.findSlot(id);
                  if (slot == null)
                     continue;

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

                  // get a list of templates containing these slots and 
                  // lock them
                  List<IPSGuid> templateLocks = new ArrayList<IPSGuid>();
                  List<IPSAssemblyTemplate> templates = service
                     .findTemplatesBySlot(slot);
                  try
                  {
                     for (IPSAssemblyTemplate template : templates)
                     {
                        IPSGuid templateGuid = template.getGUID();
                        if (!templateMap.containsKey(templateGuid))
                        {
                           try
                           {
                              if (!lockService.isLockedFor(templateGuid,
                                 session, user))
                              {
                                 lockService.createLock(templateGuid, session,
                                    user, ((PSAssemblyTemplate) template)
                                       .getVersion(), false);
                                 templateLocks.add(templateGuid);
                              }
                              templateMap.put(templateGuid, template);
                           }
                           catch (PSLockException e)
                           {
                              results.addError(id, e);
                              break;
                           }
                        }
                     }

                     if (results.getErrors().containsKey(id))
                        continue;

                     // delete the associations to the slots being deleted and
                     // save
                     for (IPSAssemblyTemplate template : templateMap.values())
                     {
                        template.removeSlot(slot);
                        service.saveTemplate(template);
                     }
                  }
                  finally
                  {
                     // unlock the templates
                     if (!templateLocks.isEmpty())
                     {
                        List<PSObjectLock> locks = lockService
                           .findLocksByObjectIds(templateLocks, session, user);
                        lockService.releaseLocks(locks);
                     }
                  }

                  // delete the slot                  
                  service.deleteSlot(id);
                  results.addResult(id);
               }
               else
               {
                  PSWebserviceUtils.handleMissingLockError(id,
                     IPSTemplateSlot.class, results);
               }
            }
            catch (PSAssemblyException e)
            {
               int code = IPSWebserviceErrors.DELETE_FAILED;
               PSDesignGuid guid = new PSDesignGuid(id);
               PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code,
                     IPSTemplateSlot.class.getName(), guid.getValue(), e
                        .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));
               results.addError(id, error);
            }
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   /*
    * @see IPSAssemblyDesignWs#findAssemblyTemplates(String, String, 
    *    Set<IPSAssemblyTemplate.OutputFormat>, 
    *    IPSAssemblyTemplate.TemplateType, Boolean, Boolean, String)
    */
   public List<IPSCatalogSummary> findAssemblyTemplates(String name,
      String contentType, Set<IPSAssemblyTemplate.OutputFormat> outputFormats,
      IPSAssemblyTemplate.TemplateType type, Boolean globalFilter,
      Boolean legacyFilter, String assembler)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
         .getAssemblyService();

      if (!StringUtils.isBlank(name))
         name = StringUtils.replaceChars(name, '*', '%');

      if (!StringUtils.isBlank(contentType))
         contentType = StringUtils.replaceChars(contentType, '*', '%');

      if (!StringUtils.isBlank(assembler))
         assembler = StringUtils.replaceChars(assembler, '*', '%');

      List<IPSAssemblyTemplate> templates;
      try
      {
         templates = service.findTemplates(name, contentType, outputFormats,
            type, globalFilter, legacyFilter, assembler);
      }
      catch (PSAssemblyException e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
      return PSWebserviceUtils.toObjectSummaries(templates);
   }

   // @see IPSAssemblyDesignWs#findSlots(String, IPSGuid)
   public List<IPSCatalogSummary> findSlots(String name,
      IPSGuid associatedTemplateId)
   {
      IPSAssemblyWs service = PSAssemblyWsLocator.getAssemblyWebservice();
      List<IPSTemplateSlot> slots = service.loadSlots(name);

      // filter for associated template id
      if (associatedTemplateId != null)
      {
         List<IPSTemplateSlot> filteredSlots = new ArrayList<IPSTemplateSlot>();
         for (IPSTemplateSlot slot : slots)
         {
            Collection<PSPair<IPSGuid, IPSGuid>> associations = slot
               .getSlotAssociations();
            for (PSPair<IPSGuid, IPSGuid> association : associations)
            {
               if (association.getSecond().equals(associatedTemplateId))
                  filteredSlots.add(slot);
            }
         }

         slots = filteredSlots;
      }

      return PSWebserviceUtils.toObjectSummaries(slots);
   }

   /*
    * @see IPSAssemblyDesignWs#loadAssemblyTemplates(List, boolean, boolean,
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSAssemblyTemplateWs> loadAssemblyTemplates(List<IPSGuid> ids,
      boolean lock, boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            List<IPSAssemblyTemplate> templates = new ArrayList<IPSAssemblyTemplate>();
            templates.add(PSWebserviceUtils.loadTemplate(id, true));
            List<PSAssemblyTemplateWs> wsTemplates = getTemplateWs(templates);
            results.addResult(id, wsTemplates.get(0));
         }
         catch (PSErrorException e)
         {
            results.addError(id, e);
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

   // @see IPSAssemblyDesignWs#loadSlots(List, boolean, boolean, String, String)
   @SuppressWarnings("unchecked")
   public List<IPSTemplateSlot> loadSlots(List<IPSGuid> ids, boolean lock,
      boolean overrideLock, String session, String user)
      throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSAssemblyService service = PSAssemblyServiceLocator
         .getAssemblyService();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            IPSTemplateSlot slot = service.loadSlot(id);
            results.addResult(id, slot);
         }
         catch (PSAssemblyException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
               PSWebserviceErrors.createErrorMessage(code,
                  IPSTemplateSlot.class.getName(), guid.getValue()),
               ExceptionUtils.getFullStackTrace(e));
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

   /*
    * @see IPSAssemblyDesignWs#saveAssemblyTemplates(List, boolean, String,
    *    String)
    */
   public void saveAssemblyTemplates(List<PSAssemblyTemplateWs> templates,
      boolean release, String session, String user) throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(templates, "templates", true,
         session, user);

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      PSErrorsException results = new PSErrorsException();
      for (PSAssemblyTemplateWs templateWs : templates)
      {
         IPSAssemblyTemplate template = templateWs.getTemplate();
         IPSGuid id = template.getGUID();
         ids.add(id);
         try
         {
            if (lockService.isLockedFor(id, session, user))
            {
               Integer version = lockService.getLockedVersion(id);

               // save the object and extend the lock
               saveTemplateWs(templateWs, version);

               if (!release)
               {
                  lockService.extendLock(id, session, user,
                     ((PSAssemblyTemplate) template).getVersion());
               }

               results.addResult(id);
            }
            else
            {
               PSWebserviceUtils.handleMissingLockError(id,
                  IPSAssemblyTemplate.class, results);
            }
         }
         catch (Exception e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code,
                  PSWebserviceErrors.createErrorMessage(code,
                        IPSAssemblyTemplate.class.getName(), guid.getValue(),
                        PSWebserviceErrors.appendMessages(e)), ExceptionUtils
                        .getFullStackTrace(e));
            results.addError(id, error);
         }
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
    * This is used to synchronize the calls that involve the same objects due to
    * the automatic removal of associations during delete operations.  This 
    * includes deleting templates and slots, each of which modifies the other
    * object type, and saving and deleting templates, both of which modify 
    * sites.
    */
   private static final String SYNC_SAVE_DELETE_TEMPLATE_AND_SLOT = "Synchronize Save and Delete Templates and Slots";

   /**
    * Deletes the specified template and the association between the template 
    * and a list of sites and slots.
    * 
    * @param deletedId the to be deleted template id, assumed not 
    *    <code>null</code>.
    * @param session The session id of the client performing the operation. 
    * Assumed not <code>null</code> or empty.
    * @param user The user id of the client performing the operation. 
    * Assumed not <code>null</code> or empty.
    * 
    * @throws PSAssemblyException if an error occurs in the assembly service.
    * @throws PSErrorsException If any problems deleting the associated content
    * type relationships.
    * @throws PSLockException if an associated slot cannot be locked.
    */
   private void deleteTemplateWs(IPSGuid deletedId, String session, String user)
           throws PSAssemblyException, PSErrorsException,
           PSLockException, PSNotFoundException {
      synchronized (SYNC_SAVE_DELETE_TEMPLATE_AND_SLOT)
      {
         // save or load (if delete) the template object first
         IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

         IPSAssemblyTemplate template;
         template = service.loadTemplate(deletedId, true);

         // get site / templates associations
         IPSSiteManagerInternal sitemgr = (IPSSiteManagerInternal) PSSiteManagerLocator
               .getSiteManager();
         Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> siteToTemplates = sitemgr
               .findSiteTemplatesAssociations();

         // save template / sites associations
         for (Map.Entry<PSPair<IPSGuid, String>, Collection<IPSGuid>> entry : siteToTemplates
               .entrySet())
         {
            IPSGuid siteId = entry.getKey().getFirst();
            Collection<IPSGuid> templateIds = entry.getValue();

            // remove template / sites associations
            if (templateIds.contains(template.getGUID()))
            {
               IPSSite s = sitemgr.loadSite(siteId);
               s.getAssociatedTemplates().remove(template);
               sitemgr.saveSite(s);
            }
         }

         IPSContentDesignWs model = PSContentWsLocator
            .getContentDesignWebservice();
         List<PSContentTemplateDesc> ctAssociations;
         //remove any content type associations first. Get all associations 
         // unlocked first and only lock the ctypes that need to be modified.
         try
         {
            ctAssociations = model.loadAssociatedTemplates(null, false, false,
               session, user);
         }
         catch (PSErrorResultsException e)
         {
            throw convertException(e);
         }

         Map<IPSGuid, List<IPSGuid>> ctypeTemplateMap = new HashMap<IPSGuid, List<IPSGuid>>();

         // group by ctype
         for (PSContentTemplateDesc desc : ctAssociations)
         {
            List<IPSGuid> templateRefs = ctypeTemplateMap.get(desc
               .getContentTypeId());
            if (templateRefs == null)
            {
               templateRefs = new ArrayList<IPSGuid>();
               ctypeTemplateMap.put(desc.getContentTypeId(), templateRefs);
            }
            templateRefs.add(desc.getTemplateId());
         }

         for (IPSGuid ctypeRef : ctypeTemplateMap.keySet())
         {
            List<IPSGuid> templateRefs = ctypeTemplateMap.get(ctypeRef);
            if (templateRefs.contains(deletedId))
            {
               List<PSContentTemplateDesc> templateTypePairs;
               try
               {
                  templateTypePairs = model.loadAssociatedTemplates(ctypeRef,
                     true, false, session, user);
               }
               catch (PSErrorResultsException e)
               {
                  throw convertException(e);
               }
               List<IPSGuid> validTemplateRefs = new ArrayList<IPSGuid>();
               for (PSContentTemplateDesc desc : templateTypePairs)
               {
                  if (!desc.getTemplateId().equals(deletedId))
                     validTemplateRefs.add(desc.getTemplateId());
               }
               model.saveAssociatedTemplates(ctypeRef, validTemplateRefs, true,
                  session, user);
            }
         }

         // delete any slot associations
         PSWebserviceUtils.removeSlotAssocations(deletedId, session, user);

         // finally, remove the template
         service.deleteTemplate(deletedId);
      }
   }

   /**
    * Saves the specified template and the association between the template and
    * a list of sites.
    * 
    * @param templateWs the object contains the new template and its association
    *           with a list of sites, assumed not <code>null</code>.
    * @param version version to restore on the template before saving it
    * 
    * @throws PSAssemblyException if an error occurs in the assembly service.
    */
   private synchronized void saveTemplateWs(PSAssemblyTemplateWs templateWs,
      Integer version) throws PSAssemblyException, PSNotFoundException {
      synchronized (SYNC_SAVE_DELETE_TEMPLATE_AND_SLOT)
      {
         // save or load (if delete) the template object first
         IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();

         IPSAssemblyTemplate template;
         template = templateWs.getTemplate();
         try
         {
            IPSGuid id = template.getGUID();
            // Load the existing template and copy data into it
            PSAssemblyTemplate dbtemplate = (PSAssemblyTemplate) service
               .loadTemplate(id, true);

            // Null version and copy data object into "live" object
            dbtemplate.setVersion(null);
            dbtemplate.fromXML(template.toXML());

            /*
             * Slots are suppressed in template XML serialization, we must add
             * them manually.
             */
            dbtemplate.setSlots(template.getSlots());

            template = dbtemplate;
         }
         catch (PSAssemblyException e)
         {
            // No problem, new instance
         }
         catch (IOException e)
         {
            // Fault
            throw new RuntimeException(e);
         }
         catch (SAXException e)
         {
            // Fault
            throw new RuntimeException(e);
         }

         // Restore version
         ((PSAssemblyTemplate) template).setVersion(null);
         ((PSAssemblyTemplate) template).setVersion(version);

         service.saveTemplate(template);

         // get site / templates associations. Has to cast to 
         IPSSiteManagerInternal sitemgr = (IPSSiteManagerInternal) PSSiteManagerLocator
               .getSiteManager();
         Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> siteToTemplates = sitemgr
               .findSiteTemplatesAssociations();
         
         // save template / sites associations
         for (Map.Entry<PSPair<IPSGuid, String>, Collection<IPSGuid>> entry : siteToTemplates
               .entrySet())
         {
            IPSGuid siteId = entry.getKey().getFirst();
            Collection<IPSGuid> templateIds = entry.getValue();
            
            // save specified template / sites associations
            Map<IPSGuid, String> siteRefs = templateWs.getSites();
            if (siteRefs.get(siteId) == null
               && templateIds.contains(template.getGUID()))
            {
               // remove template from the site
               IPSSite s = sitemgr.loadSite(siteId);
               s.getAssociatedTemplates().remove(template);
               sitemgr.saveSite(s);
               logSaveSiteTemplateAssociation(s, template, false);
            }
            else if ((siteRefs.get(siteId) != null)
               && (!templateIds.contains(template.getGUID())))
            {
               // add template to the site
               IPSSite s = sitemgr.loadSite(siteId);
               s.getAssociatedTemplates().add(template);
               sitemgr.saveSite(s);
               logSaveSiteTemplateAssociation(s, template, true);
            }
         }
      }
   }

   /**
    * Logges the save Site/Template association action. Do nothing if debug
    * is not on for this class.
    * 
    * @param site the saved Site, assumed not <code>null</code>.
    * @param template the added/removed template, assumed not <code>null</code>.
    * @param isAdd <code>true</code> if add the template; otherwise remove
    * the template from the site.
    */
   private void logSaveSiteTemplateAssociation(IPSSite site, 
         IPSAssemblyTemplate template, boolean isAdd)
   {
      if (!ms_logger.isDebugEnabled())
         return;
      
      String action = isAdd ? "ADD" : "REMOVE";
      String msgPattern = "{0} Template (id={1}, name=\"{2}\") into Site (id={3}, name=\"{4}\").";
      Object[] args = new Object[]{action, template.getGUID().toString(), 
            template.getName(), site.getGUID().toString(), site.getName()};
      MessageFormat form = new MessageFormat(msgPattern);
      String message = form.format(args);
      ms_logger.debug(message);
   }
   
   /**
    * Converts the supplied extension to the returned one by taking all the
    * results and errors and adding them to the newly created exception,
    * dropping the success objects.
    * 
    * @param e Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   private PSErrorsException convertException(PSErrorResultsException e)
   {
      PSErrorsException ex = new PSErrorsException();
      for (IPSGuid guid : e.getResults().keySet())
      {
         ex.addResult(guid);
      }
      Map<IPSGuid, Object> errors = e.getErrors();
      for (IPSGuid guid : errors.keySet())
      {
         ex.addError(guid, errors.get(guid));
      }
      return ex;
   }

   // @see IPSAssemblyDesignWs#saveSlots(List, boolean, String, String)
   public void saveSlots(List<IPSTemplateSlot> slots, boolean release,
      String session, String user) throws PSErrorsException
   {
      if (slots == null || slots.isEmpty())
         throw new IllegalArgumentException("slots cannot be null or empty");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSAssemblyService service = PSAssemblyServiceLocator
         .getAssemblyService();

      List<IPSGuid> ids = PSGuidUtils.toGuidList(slots);

      IPSObjectLockService lockService = PSObjectLockServiceLocator
         .getLockingService();

      synchronized (SYNC_SAVE_DELETE_TEMPLATE_AND_SLOT)
      {
         PSErrorsException results = new PSErrorsException();
         for (IPSTemplateSlot slot : slots)
         {
            IPSGuid id = slot.getGUID();
            try
            {
               if (lockService.isLockedFor(id, session, user))
               {
                  if (service.findSlot(id) != null)
                  {
                     // after we know it does exist, then load non-cached one
                     PSTemplateSlot dbslot;
                     dbslot = (PSTemplateSlot) service.loadSlotModifiable(id);
                     dbslot.setVersion(null);
                     dbslot.fromXML(slot.toXML());
                     slot = dbslot;
                  }

                  // set the correct version from the lock
                  Integer version = lockService.getLockedVersion(id);
                  if (version != null)
                  {
                     // Null version again before resetting it from the lock
                     // manager
                     ((PSTemplateSlot) slot).setVersion(null);
                     ((PSTemplateSlot) slot).setVersion(version);
                  }

                  // save the object and extend the lock
                  service.saveSlot(slot);

                  // reload the slot to obtain the new version
                  slot = service.loadSlotModifiable(id);

                  if (!release)
                  {
                     lockService.extendLock(id, session, user,
                        ((PSTemplateSlot) slot).getVersion());
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
                           IPSTemplateSlot.class.getName(), guid.getValue()),
                        ExceptionUtils.getFullStackTrace(new Exception()));
                     results.addError(id, error);
                  }
                  else
                  {
                     int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                     PSDesignGuid guid = new PSDesignGuid(id);
                     PSErrorException error = new PSErrorException(code,
                        PSWebserviceErrors.createErrorMessage(code,
                           IPSTemplateSlot.class.getName(), guid.getValue(),
                           lock.getLocker(), lock.getRemainingTime()),
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
                  PSWebserviceErrors.createErrorMessage(code,
                     IPSTemplateSlot.class.getName(), guid.getValue(), e
                        .getLocalizedMessage()), ExceptionUtils
                     .getFullStackTrace(e));
               results.addError(id, error);
            }
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
   }
   
   /*
    * //see base interface method for details
    */
   public List<String> getTemplateThumbImages(List<String> names, String site)
   {
      Map<String, String> imgFileMap = getImgFileMap();
      List<String> images = new ArrayList<String>();
      for (String name : names)
      {
         String thumbImgUrl = PSTemplateImageUtils.getImageUrl(name, site,
               true, imgFileMap);
         images.add(thumbImgUrl.replace('\\', '/'));
      }
      return images;
   }
   
   /*
    * //see base interface method for details
    */
   public void notifyEvent(PSNotificationEvent event)
   {
      if (!event.getType().equals(EventType.FILE))
         return;
   
      // flush the cached image file map if there are any file changes,
      // may be from installed packages.
      getCache().evict(IMG_FILE_MAP, IPSCacheAccess.IN_MEMORY_STORE);
   }
   
   /**
    * Gets the image file mapping, which 
    * @return
    */
   @SuppressWarnings("unchecked")
   private Map<String, String> getImgFileMap()
   {
      IPSCacheAccess cache = getCache();
      Map<String, String> imgMap = (Map<String, String>) cache.get(IMG_FILE_MAP,
            IPSCacheAccess.IN_MEMORY_STORE);
      if (imgMap == null)
      {
         HashMap<String, String> mapImp = new HashMap<String, String>();
         mapImp.putAll(PSTemplateImageUtils.getImageFileNames());
         cache.save(IMG_FILE_MAP, mapImp, IPSCacheAccess.IN_MEMORY_STORE);
         imgMap = mapImp;
      }
      return imgMap;
   }
   
   /**
    * Spring property accessor.
    *
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
   }

   /**
    * Set the cache service.
    *
    * @param cache the service, never <code>null</code>
    */
   @Autowired
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cache = cache;
   }

   /**
    * Cache service, used to invalidate content information.
    */
   IPSCacheAccess m_cache;

   /**
    * The image file mapping, used to cache the mapping since it is not 
    * expected to change after the server started. 
    */
   private final static String IMG_FILE_MAP = "sys_image_file_mappings";
   
   /**
    * The logger for this class
    */
   private static Logger ms_logger = Logger.getLogger("PSAssemblyDesignWs");
}
