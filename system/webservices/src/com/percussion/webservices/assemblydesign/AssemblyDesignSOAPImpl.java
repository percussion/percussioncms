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
package com.percussion.webservices.assemblydesign;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.assembly.IPSAssemblyDesignWs;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyxDesign.wsdl</code> for operations defined in the
 * <code>assemblyDesignSOAP</code> bindings.
 */
public class AssemblyDesignSOAPImpl extends PSBaseSOAPImpl 
   implements AssemblyDesign
{
   /*
    * (non-Javadoc)
    * 
    * @see AssemblyDesign#createAssemblyTemplates(String[])
    */
   public PSAssemblyTemplate[] createAssemblyTemplates(String[] names)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createAssemblyTemplates";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         return (PSAssemblyTemplate[]) convert(PSAssemblyTemplate[].class, 
            service.createAssemblyTemplates(Arrays.asList(names), session, user));
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
    * @see AssemblyDesign#createSlots(String[])
    */
   public PSTemplateSlot[] createSlots(String[] names)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      final String serviceName = "createSlots";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         return (PSTemplateSlot[]) convert(PSTemplateSlot[].class, 
            service.createSlots(Arrays.asList(names), session, user));
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
    * @see AssemblyDesign#deleteAssemblyTemplates(DeleteAssemblyTemplatesRequest)
    */
   public void deleteAssemblyTemplates(
      DeleteAssemblyTemplatesRequest deleteAssemblyTemplatesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteAssemblyTemplate";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteAssemblyTemplatesRequest.getId(), PSTypeEnum.TEMPLATE);
         Boolean ignore = deleteAssemblyTemplatesRequest.getIgnoreDependencies();
         boolean ignoreDependencies = ignore == null ? 
            false : ignore.booleanValue();
         service.deleteAssemblyTemplates(ids, ignoreDependencies, session, 
            user);
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
    * @see AssemblyDesign#deleteSlots(DeleteSlotsRequest)
    */
   public void deleteSlots(DeleteSlotsRequest deleteSlotsRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "deleteSlots";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            deleteSlotsRequest.getId(), PSTypeEnum.SLOT);
         Boolean ignore = deleteSlotsRequest.getIgnoreDependencies();
         boolean ignoreDependencies = ignore == null ? 
            false : ignore.booleanValue();
         service.deleteSlots(ids, ignoreDependencies, session, user);
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
    * @see AssemblyDesign#findAssemblyTemplates(FindAssemblyTemplatesRequest)
    */
   public PSObjectSummary[] findAssemblyTemplates(
      FindAssemblyTemplatesRequest findAssemblyTemplatesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findAssemblyTemplates";
      try
      {
         authenticate();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         com.percussion.webservices.assembly.data.TemplateType clientType = 
            findAssemblyTemplatesRequest.getTemplateType();
         IPSAssemblyTemplate.TemplateType serverType = null;
         if (clientType != null)
         {
            // convert the client to the server type
            serverType = (IPSAssemblyTemplate.TemplateType) convert(
               IPSAssemblyTemplate.TemplateType.class, clientType);
         }
         
         Set<IPSAssemblyTemplate.OutputFormat> outputFormats = null;
         OutputFormatType[] outputFormatTypes = 
            findAssemblyTemplatesRequest.getOutputFormats();
         if (outputFormatTypes != null && outputFormatTypes.length > 0)
         {
            outputFormats = new HashSet<IPSAssemblyTemplate.OutputFormat>();
            for (OutputFormatType outputFormatType : outputFormatTypes)
            {
               outputFormats.add(
                  (IPSAssemblyTemplate.OutputFormat) convert(
                     IPSAssemblyTemplate.OutputFormat.class, outputFormatType));
            }
         }
         
         Boolean globalFilter = findAssemblyTemplatesRequest.getGlobalFilter();
         
         Boolean legacyFilter = findAssemblyTemplatesRequest.getLegacyFilter();
         
         List summaries = service.findAssemblyTemplates(
            findAssemblyTemplatesRequest.getName(), 
            findAssemblyTemplatesRequest.getContentType(),
            outputFormats, serverType, globalFilter, legacyFilter,
            findAssemblyTemplatesRequest.getAssembler());

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see AssemblyDesign#findSlots(FindSlotsRequest)
    */
   public PSObjectSummary[] findSlots(FindSlotsRequest findSlotsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      final String serviceName = "findSlots";
      try
      {
         authenticate();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         PSDesignGuid associatedTemplateId = null;
         if (findSlotsRequest.getAssociatedTemplateId() != null)
            associatedTemplateId = new PSDesignGuid(
               findSlotsRequest.getAssociatedTemplateId());
         List summaries = service.findSlots(findSlotsRequest.getName(), 
            associatedTemplateId);

         return (PSObjectSummary[]) convert(PSObjectSummary[].class, summaries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see AssemblyDesign#loadAssemblyTemplates(LoadAssemblyTemplatesRequest)
    */
   public PSAssemblyTemplate[] loadAssemblyTemplates(
      LoadAssemblyTemplatesRequest loadAssemblyTemplatesRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "loadAssemblyTemplates";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadAssemblyTemplatesRequest.getId(), PSTypeEnum.TEMPLATE);
         boolean lock = extractBooleanValue(
            loadAssemblyTemplatesRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadAssemblyTemplatesRequest.getOverrideLock(), false);
         List templates = service.loadAssemblyTemplates(ids, lock, 
            overrideLock, session, user);

         return (PSAssemblyTemplate[]) convert(PSAssemblyTemplate[].class, 
            templates);
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
         PSErrorResultsFault fault = (PSErrorResultsFault) convert(
            PSErrorResultsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }

      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see AssemblyDesign#loadSlots(LoadSlotsRequest)
    */
   public PSTemplateSlot[] loadSlots(LoadSlotsRequest loadSlotsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "loadSlots";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toGuidList(
            loadSlotsRequest.getId(), PSTypeEnum.SLOT);
         boolean lock = extractBooleanValue(
            loadSlotsRequest.getLock(), false);
         boolean overrideLock = extractBooleanValue(
            loadSlotsRequest.getOverrideLock(), false);
         List slots = service.loadSlots(ids, lock, overrideLock, 
            session, user);

         return (PSTemplateSlot[]) convert(PSTemplateSlot[].class, slots);
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
         PSErrorResultsFault fault = (PSErrorResultsFault) convert(
            PSErrorResultsFault.class, e);
         fault.setService(serviceName);
         
         throw fault;
      }
      
      // will never get here
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see AssemblyDesign#saveAssemblyTemplates(SaveAssemblyTemplatesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveAssemblyTemplates(
      SaveAssemblyTemplatesRequest saveAssemblyTemplatesRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveAssemblyTemplates";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List templates = (List) convert(List.class,
            saveAssemblyTemplatesRequest.getPSAssemblyTemplate());
         Boolean release = saveAssemblyTemplatesRequest.getRelease() == null ? 
            Boolean.TRUE : saveAssemblyTemplatesRequest.getRelease().booleanValue();
         service.saveAssemblyTemplates(templates, release, session, user);
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
    * @see AssemblyDesign#saveSlots(SaveSlotsRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveSlots(SaveSlotsRequest saveSlotsRequest)
      throws RemoteException, PSErrorsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      final String serviceName = "saveSlots";
      try
      {
         String session = authenticate();
         String user = getRemoteUser();

         IPSAssemblyDesignWs service = 
            PSAssemblyWsLocator.getAssemblyDesignWebservice();
         
         List slots = (List) convert(List.class, 
            saveSlotsRequest.getPSTemplateSlot());
         Boolean release = saveSlotsRequest.getRelease() == null ? 
            Boolean.TRUE : saveSlotsRequest.getRelease().booleanValue();
         service.saveSlots(slots, release, session, user);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (RuntimeException e)
      {
         handleRuntimeException(e, serviceName);
      }
   }
}
