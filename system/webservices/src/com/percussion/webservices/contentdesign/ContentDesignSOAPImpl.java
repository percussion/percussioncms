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
package com.percussion.webservices.contentdesign;

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
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
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;

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
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
    * @see ContentDesign#createKeywords(String[])
    */
   public PSKeyword[] createKeywords(String[] names) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSNotAuthorizedFault
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
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#createLocales(CreateLocalesRequest)
    */
   public PSLocale[] createLocales(
      CreateLocalesRequest createLocalesRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault, PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
    * @see ContentDesign#deleteContentTypes(DeleteContentTypesRequest)
    */
   public void deleteContentTypes(
      DeleteContentTypesRequest deleteContentTypesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         boolean ignoreDependencies = ignore == null ? 
            false : ignore.booleanValue();
         
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
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         boolean ignoreDependencies = ignore == null ? 
            false : ignore.booleanValue();
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
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         boolean ignoreDependencies = ignore == null ? 
            false : ignore.booleanValue();
         
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
      FindContentTypesRequest findContentTypesRequest) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List sums = service.findContentTypes(
            findContentTypesRequest.getName());
         
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, 
            sums);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findContentTypes");
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#findKeywords(FindKeywordsRequest)
    */
   public PSObjectSummary[] findKeywords(FindKeywordsRequest findKeywordsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findKeywords";
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List summaries = service.findKeywords(findKeywordsRequest.getName());

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#findLocales(FindLocalesRequest)
    */
   public PSObjectSummary[] findLocales(FindLocalesRequest findLocalesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      try
      {
         authenticate();

         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();
         
         List sums = service.findLocales(findLocalesRequest.getCode(), 
            findLocalesRequest.getName());
         
         return (PSObjectSummary[]) convert(PSObjectSummary[].class, 
            sums);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findLocales");
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadContentTypes(LoadContentTypesRequest)
    */
   public PSContentType[] loadContentTypes(
      LoadContentTypesRequest loadContentTypesRequest) throws RemoteException,
      PSErrorResultsFault, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
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
         boolean lock = lockValue == null ? false : lockValue.booleanValue();
         Boolean overrideLockValue = loadContentTypesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue == null ? false : 
            overrideLockValue.booleanValue();
         List defs = service.loadContentTypes(ids, lock, overrideLock, session, 
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
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadKeywords(LoadKeywordsRequest)
    */
   public PSKeyword[] loadKeywords(LoadKeywordsRequest loadKeywordsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         List keywords = service.loadKeywords(ids, lock, overrideLock, 
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
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#loadLocales(LoadLocalesRequest)
    */
   public PSLocale[] loadLocales(LoadLocalesRequest loadLocalesRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         boolean lock = lockValue == null ? false : lockValue.booleanValue();
         Boolean overrideLockValue = loadLocalesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue == null ? false : 
            overrideLockValue.booleanValue();
         List locales = service.loadLocales(ids, lock, overrideLock, session, 
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
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ContentDesign#saveContentTypes(SaveContentTypesRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public void saveContentTypes(
      SaveContentTypesRequest saveContentTypesRequest) throws RemoteException,
      PSErrorsFault, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
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
         Boolean release = saveContentTypesRequest.getRelease() == null ? 
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
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         Boolean release = saveLocalesRequest.getRelease() == null ? 
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
      throws RemoteException, PSInvalidSessionFault, PSLockFault,
      PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
      throws RemoteException, PSInvalidSessionFault, PSLockFault,
      PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
      throws RemoteException, PSInvalidSessionFault, PSLockFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
      throws RemoteException, PSInvalidSessionFault, PSLockFault,
      PSContractViolationFault, PSNotAuthorizedFault
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
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
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
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault, 
         PSContractViolationFault, PSNotAuthorizedFault
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
         boolean lock = lockValue == null ? false : lockValue.booleanValue();
         Boolean overrideLockValue = 
            loadAssociatedTemplatesRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue == null ? false : 
            overrideLockValue.booleanValue();
         IPSGuid guid = null;
         if (contentTypeId != -1)
            guid = new PSGuid(PSTypeEnum.NODEDEF, contentTypeId);
         
         List results = service.loadAssociatedTemplates(guid, lock, 
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
      return null;
   }

   /* (non-Javadoc)
    * @see ContentDesign#saveAssociatedTemplates(SaveAssociatedTemplatesRequest)
    */
   public void saveAssociatedTemplates(
      SaveAssociatedTemplatesRequest saveAssociatedTemplatesRequest) 
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault, 
         PSContractViolationFault, PSNotAuthorizedFault
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
         
         Boolean release = saveAssociatedTemplatesRequest.getRelease() == null ? 
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
      throws RemoteException, PSInvalidSessionFault, PSNotAuthorizedFault, 
      PSLockFault
   {
      final String serviceName = "loadTranslationSettings";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         Boolean lockValue = loadTranslationSettingsRequest.getLock();
         boolean lock = lockValue == null ? false : lockValue.booleanValue();
         Boolean overrideLockValue = 
            loadTranslationSettingsRequest.getOverrideLock();
         boolean overrideLock = overrideLockValue == null ? false : 
            overrideLockValue.booleanValue();
         List ats = service.loadTranslationSettings(lock, overrideLock, 
            session, user);

         return (PSAutoTranslation[]) convert(PSAutoTranslation[].class, ats);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSLockErrorException e)
      {
         PSLockFault fault = (PSLockFault) convert(
            PSLockFault.class, e);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
      
      // will never get here
      return null;
   }

   /* (non-Javadoc)
    * @see ContentDesign#saveTranslationSettings(SaveTranslationSettingsRequest)
    */
   @SuppressWarnings(value={"unchecked"})
   public void saveTranslationSettings(
      SaveTranslationSettingsRequest saveTranslationSettingsRequest) 
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault, 
         PSNotAuthorizedFault, PSLockFault
   {
      final String serviceName = "saveTranslationSettings";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();
         
         IPSContentDesignWs service = 
            PSContentWsLocator.getContentDesignWebservice();

         List ats;
         if (saveTranslationSettingsRequest.getPSAutoTranslation() == null)
         {
            ats = new ArrayList<
               com.percussion.services.content.data.PSAutoTranslation>();
         }
         else
         {
            ats = (List) convert(
               List.class, 
               saveTranslationSettingsRequest.getPSAutoTranslation());
         }
         
         Boolean release = saveTranslationSettingsRequest.getRelease() == null ? 
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
         PSLockFault fault = (PSLockFault) convert(
            PSLockFault.class, e);
         
         throw fault;
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }
}
