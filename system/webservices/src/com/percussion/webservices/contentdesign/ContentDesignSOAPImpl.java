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
package com.percussion.webservices.contentdesign;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSAutoTranslation;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.content.PSContentTemplateDesc;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.content.PSKeyword;
import com.percussion.webservices.content.PSLocale;
import com.percussion.webservices.faults.PSLockFault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyxDesign.wsdl</code> for operations defined in the
 * <code>contentDesignSOAP</code> bindings.
 */
public class ContentDesignSOAPImpl extends PSBaseSOAPImpl 
   implements ContentDesign
{
   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#createContentTypes(String[])
    */
   public PSContentType[] createContentTypes(String[] names)
      throws RemoteException
   {
      final String serviceName = "createContentTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         return (PSContentType[]) convert(PSContentType[].class, 
            service.createContentTypes(Arrays.asList(names), session, user));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // Should never get here - return empty array if we do
      return new PSContentType[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#createKeywords(String[])
    */
   public PSKeyword[] createKeywords(String[] names) throws RemoteException
   {
      final String serviceName = "createKeywords";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         return (PSKeyword[]) convert(PSKeyword[].class, 
            service.createKeywords(Arrays.asList(names), session, user));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // Should never get here - return an empty array in case
      return new PSKeyword[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#createLocales(CreateLocalesRequest)
    */
   public PSLocale[] createLocales(
      CreateLocalesRequest createLocalesRequest) throws RemoteException
   {
      final String serviceName = "createLocales";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         String[] codes = createLocalesRequest.getCode();
         if (codes == null)
            throw new IllegalArgumentException("codes may not be null");
         String[] names = createLocalesRequest.getLabel();
         if (names == null)
            throw new IllegalArgumentException("names may not be null");
         
         return (PSLocale[]) convert(PSLocale[].class, 
            service.createLocales(Arrays.asList(codes), Arrays.asList(names), 
               session, user));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return new PSLocale[] {};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#deleteContentTypes(DeleteContentTypesRequest)
    */
   public void deleteContentTypes(
      DeleteContentTypesRequest deleteContentTypesRequest)
      throws RemoteException
   {
      final String serviceName = "deleteContentTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteContentTypesRequest.getId(), PSTypeEnum.NODEDEF);

         Boolean ignore = deleteContentTypesRequest.getIgnoreDependencies();
         boolean ignoreDependencies = ignore != null && ignore;
         
         service.deleteContentTypes(ids, ignoreDependencies, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#deleteKeywords(DeleteKeywordsRequest)
    */
   public void deleteKeywords(DeleteKeywordsRequest deleteKeywordsRequest)
      throws RemoteException
   {
      final String serviceName = "deleteKeywords";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteKeywordsRequest.getId(), PSTypeEnum.KEYWORD_DEF);
         Boolean ignore = deleteKeywordsRequest.getIgnoreDependencies();
         boolean ignoreDependencies = ignore != null && ignore;
         service.deleteKeywords(ids, ignoreDependencies, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#deleteLocales(DeleteLocalesRequest)
    */
   public void deleteLocales(DeleteLocalesRequest deleteLocalesRequest)
      throws RemoteException
   {
      final String serviceName = "deleteLocales";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteLocalesRequest.getId(), PSTypeEnum.LOCALE);

         Boolean ignore = deleteLocalesRequest.getIgnoreDependencies();
         boolean ignoreDependencies = ignore != null && ignore;
         
         service.deleteLocales(ids, ignoreDependencies, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#findContentTypes(FindContentTypesRequest)
    */
   public PSObjectSummary[] findContentTypes(
      FindContentTypesRequest findContentTypesRequest) throws RemoteException
   {
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List<IPSCatalogSummary> sums = service.findContentTypes(
            findContentTypesRequest.getName());
         
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, 
            sums);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findContentTypes");
         return new PSObjectSummary[]{};
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#findKeywords(FindKeywordsRequest)
    */
   public PSObjectSummary[] findKeywords(FindKeywordsRequest findKeywordsRequest)
      throws RemoteException
   {
      final String serviceName = "findKeywords";
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List<IPSCatalogSummary> summaries = service.findKeywords(findKeywordsRequest.getName());

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      return new PSObjectSummary[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#findLocales(FindLocalesRequest)
    */
   public PSObjectSummary[] findLocales(FindLocalesRequest findLocalesRequest)
      throws RemoteException
   {
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List<IPSCatalogSummary> sums = service.findLocales(findLocalesRequest.getCode(),
            findLocalesRequest.getName());
         
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, 
            sums);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findLocales");
      }
      return new PSObjectSummary[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadContentTypes(LoadContentTypesRequest)
    */
   public PSContentType[] loadContentTypes(
      LoadContentTypesRequest loadContentTypesRequest) throws RemoteException
   {
      final String serviceName = "loadContentTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadContentTypesRequest.getId(), PSTypeEnum.NODEDEF);
         Boolean lockValue = loadContentTypesRequest.getLock();
         boolean lock = lockValue != null && lockValue;
         Boolean overrideLockValue = loadContentTypesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue != null && overrideLockValue;
         List<PSItemDefinition> defs = service.loadContentTypes(ids, lock, overrideLock, session,
            user);

         return (PSContentType[]) convert(PSContentType[].class, 
            defs);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      // will never get here
      return new PSContentType[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadKeywords(LoadKeywordsRequest)
    */
   public PSKeyword[] loadKeywords(LoadKeywordsRequest loadKeywordsRequest)
      throws RemoteException
   {
      final String serviceName = "loadKeywords";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadKeywordsRequest.getId(), PSTypeEnum.KEYWORD_DEF);
         boolean lock = extractBooleanValue(
            loadKeywordsRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadKeywordsRequest.getOverrideLock(), false);
         List<com.percussion.services.content.data.PSKeyword> keywords = service.loadKeywords(ids, lock, overrideLock,
            session, user);

         return (PSKeyword[]) convert(PSKeyword[].class, keywords);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      // will never get here
      return new PSKeyword[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadLocales(LoadLocalesRequest)
    */
   public PSLocale[] loadLocales(LoadLocalesRequest loadLocalesRequest)
      throws RemoteException
   {
      final String serviceName = "loadLocales";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadLocalesRequest.getId(), PSTypeEnum.LOCALE);
         Boolean lockValue = loadLocalesRequest.getLock();
         boolean lock = lockValue != null && lockValue;
         Boolean overrideLockValue = loadLocalesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue != null && overrideLockValue;
         List<com.percussion.i18n.PSLocale> locales = service.loadLocales(ids, lock, overrideLock, session,
            user);

         return (PSLocale[]) convert(PSLocale[].class, 
            locales);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      // will never get here
      return new PSLocale[]{};
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveContentTypes(SaveContentTypesRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public void saveContentTypes(
      SaveContentTypesRequest saveContentTypesRequest) throws RemoteException
   {
      final String serviceName = "saveContentTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List contentTypes = (List) convert(
            List.class, 
            saveContentTypesRequest.getPSContentType());
         boolean release = saveContentTypesRequest.getRelease() == null ?
            Boolean.TRUE : saveContentTypesRequest.getRelease().booleanValue();
         
         service.saveContentTypes(contentTypes, release, 
            session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveKeywords(SaveKeywordsRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveKeywords(SaveKeywordsRequest saveKeywordsRequest)
      throws RemoteException
   {
      final String serviceName = "saveKeywords";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List keywords = (List) convert(List.class, 
            saveKeywordsRequest.getPSKeyword());
         boolean release = extractBooleanValue(
            saveKeywordsRequest.getRelease(), true);
         service.saveKeywords(keywords, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveLocales(SaveLocalesRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public void saveLocales(SaveLocalesRequest saveLocalesRequest)
      throws RemoteException
   {
      final String serviceName = "saveLocales";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List locales = (List) convert(
            List.class, 
            saveLocalesRequest.getPSLocale());
         boolean release = saveLocalesRequest.getRelease() == null ?
            Boolean.TRUE : saveLocalesRequest.getRelease().booleanValue();
         
         service.saveLocales(locales, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadSharedDefinition(LoadSharedDefinitionRequest)
    */
   public LoadSharedDefinitionResponse loadSharedDefinition(
      LoadSharedDefinitionRequest loadSharedDefinitionRequest)
      throws RemoteException
   {
      final String serviceName = "loadSharedDefinition";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         boolean lock = extractBooleanValue(
            loadSharedDefinitionRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadSharedDefinitionRequest.getOverrideLock(), false);

         PSContentEditorSharedDef sharedDef = 
            service.loadContentEditorSharedDef(lock, overrideLock, session, 
               user);
         
         PSContentEditorDefinition def = (PSContentEditorDefinition) convert(
            PSContentEditorSystemDef.class, sharedDef);

         return new LoadSharedDefinitionResponse(def);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         throw new PSLockFault(e.getCode(), e.getLocalizedMessage(), 
            e.getStack(), e.getLocker(), e.getRemainingTime());
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadSystemDefinition(LoadSystemDefinitionRequest)
    */
   public LoadSystemDefinitionResponse loadSystemDefinition(
      LoadSystemDefinitionRequest loadSystemDefinitionRequest)
      throws RemoteException
   {
      final String serviceName = "loadSystemDefinition";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         boolean lock = extractBooleanValue(
            loadSystemDefinitionRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadSystemDefinitionRequest.getOverrideLock(), false);

         PSContentEditorSystemDef systemDef = 
            service.loadContentEditorSystemDef(lock, overrideLock, session, 
               user);
         
         PSContentEditorDefinition def = (PSContentEditorDefinition) convert(
            PSContentEditorSystemDef.class, systemDef);

         return new LoadSystemDefinitionResponse(def);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         throw new PSLockFault(e.getCode(), e.getLocalizedMessage(), 
            e.getStack(), e.getLocker(), e.getRemainingTime());
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveSharedDefinition(SaveSharedDefinitionRequest)
    */
   public void saveSharedDefinition(
      SaveSharedDefinitionRequest saveSharedDefinitionRequest)
      throws RemoteException
   {
      final String serviceName = "saveSharedDefinition";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         PSContentEditorSharedDef def = (PSContentEditorSharedDef) convert(
            PSContentEditorDefinition.class, 
            saveSharedDefinitionRequest.getPSContentEditorDefinition());
         boolean release = extractBooleanValue(
            saveSharedDefinitionRequest.getRelease(), true);
         service.saveContentEditorSharedDef(def, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         throw new PSLockFault(e.getCode(), e.getLocalizedMessage(), 
            e.getStack(), e.getLocker(), e.getRemainingTime());
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveSystemDefinition(SaveSystemDefinitionRequest)
    */
   public void saveSystemDefinition(
      SaveSystemDefinitionRequest saveSystemDefinitionRequest)
      throws RemoteException
   {
      final String serviceName = "saveSystemDefinition";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         PSContentEditorSystemDef def = (PSContentEditorSystemDef) convert(
            PSContentEditorDefinition.class, 
            saveSystemDefinitionRequest.getPSContentEditorDefinition());
         boolean release = extractBooleanValue(
            saveSystemDefinitionRequest.getRelease(), true);
         service.saveContentEditorSystemDef(def, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         throw new PSLockFault(e.getCode(), e.getLocalizedMessage(), 
            e.getStack(), e.getLocker(), e.getRemainingTime());
      }
      catch (PSErrorException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see ContentDesign#loadAssociatedTemplates(LoadAssociatedTemplatesRequest)
    */
   public PSContentTemplateDesc[] loadAssociatedTemplates(
      LoadAssociatedTemplatesRequest loadAssociatedTemplatesRequest) 
      throws RemoteException
   {
      final String serviceName = "loadAssociatedTemplates";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         long contentTypeId = loadAssociatedTemplatesRequest.getContentTypeId();
         
         Boolean lockValue = loadAssociatedTemplatesRequest.getLock();
         boolean lock = lockValue != null && lockValue;
         Boolean overrideLockValue = 
            loadAssociatedTemplatesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue != null && overrideLockValue;
         IPSGuid guid = null;
         if (contentTypeId != -1)
            guid = new PSGuid(PSTypeEnum.NODEDEF, contentTypeId);
         
         List<com.percussion.services.contentmgr.data.PSContentTemplateDesc> results = service.loadAssociatedTemplates(guid, lock,
            overrideLock, session, user);

         return (PSContentTemplateDesc[]) convert(PSContentTemplateDesc[].class, 
            results);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      // will never get here
      return new PSContentTemplateDesc[]{};
   }

   /* (non-Javadoc)
    * @see ContentDesign#saveAssociatedTemplates(SaveAssociatedTemplatesRequest)
    */
   public void saveAssociatedTemplates(
      SaveAssociatedTemplatesRequest saveAssociatedTemplatesRequest) 
      throws RemoteException
   {
      final String serviceName = "saveContentTypes";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         long contentTypeId = saveAssociatedTemplatesRequest.getContentTypeId();
         long[] templateIds = saveAssociatedTemplatesRequest.getTemplateId();
         if (templateIds == null)
            templateIds = new long[0];
         
         boolean release = saveAssociatedTemplatesRequest.getRelease() == null ?
            Boolean.TRUE : 
               saveAssociatedTemplatesRequest.getRelease().booleanValue();
         
         service.saveAssociatedTemplates(new PSGuid(PSTypeEnum.NODEDEF, 
            contentTypeId), PSGuidUtils.toGuidList(templateIds, 
               PSTypeEnum.TEMPLATE), release, 
               session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see ContentDesign#loadTranslationSettings(LoadTranslationSettingsRequest)
    */
   public PSAutoTranslation[] loadTranslationSettings(
      LoadTranslationSettingsRequest loadTranslationSettingsRequest) 
      throws RemoteException
   {
      final String serviceName = "loadTranslationSettings";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         Boolean lockValue = loadTranslationSettingsRequest.getLock();
         boolean lock = lockValue != null && lockValue;
         Boolean overrideLockValue = 
            loadTranslationSettingsRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue != null && overrideLockValue;
         List<com.percussion.services.content.data.PSAutoTranslation> ats = service.loadTranslationSettings(lock, overrideLock,
            session, user);

         return (PSAutoTranslation[]) convert(PSAutoTranslation[].class, ats);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         throw (PSLockFault) convert(
                 PSLockFault.class, e);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }

      return new PSAutoTranslation[]{};
   }

   /* (non-Javadoc)
    * @see ContentDesign#saveTranslationSettings(SaveTranslationSettingsRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public void saveTranslationSettings(
      SaveTranslationSettingsRequest saveTranslationSettingsRequest) 
      throws RemoteException
   {
      final String serviceName = "saveTranslationSettings";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List<com.percussion.services.content.data.PSAutoTranslation> ats;
         if (saveTranslationSettingsRequest.getPSAutoTranslation() == null)
         {
            ats = new ArrayList<>();
         }
         else
         {
            ats = (List<com.percussion.services.content.data.PSAutoTranslation>) convert(
               List.class, 
               saveTranslationSettingsRequest.getPSAutoTranslation());
         }
         
         boolean release = saveTranslationSettingsRequest.getRelease() == null ?
            Boolean.TRUE : 
               saveTranslationSettingsRequest.getRelease().booleanValue();
         
         service.saveTranslationSettings(ats, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         
         throw (PSLockFault) convert(
                 PSLockFault.class, e);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }
   
   private static final Logger log = LogManager.getLogger(IPSConstants.WEBSERVICES_LOG);
}
