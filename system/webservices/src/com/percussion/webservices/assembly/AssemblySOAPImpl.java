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
package com.percussion.webservices.assembly;

import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.assembly.Assembly;
import com.percussion.webservices.assembly.IPSAssemblyWs;
import com.percussion.webservices.assembly.LoadAssemblyTemplatesRequest;
import com.percussion.webservices.assembly.LoadSlotsRequest;
import com.percussion.webservices.assembly.PSAssemblyWsLocator;
import com.percussion.webservices.assembly.data.PSAssemblyTemplate;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyx.wsdl</code> for operations defined in the
 * <code>assemblySOAP</code> bindings.
 */
public class AssemblySOAPImpl extends PSBaseSOAPImpl implements Assembly
{
   /*
    * (non-Javadoc)
    * 
    * @see Assembly#loadAssemblyTemplates(LoadAssemblyTemplatesRequest)
    */
   public PSAssemblyTemplate[] loadAssemblyTemplates(
      LoadAssemblyTemplatesRequest loadAssemblyTemplatesRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      PSAssemblyTemplate[] result = null;
      try
      {
         authenticate();

         IPSAssemblyWs service = PSAssemblyWsLocator.getAssemblyWebservice();
         
         List templates = service.loadAssemblyTemplates(
            loadAssemblyTemplatesRequest.getName(), 
            loadAssemblyTemplatesRequest.getContentType());

         result = (PSAssemblyTemplate[]) convert(PSAssemblyTemplate[].class,
               templates);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadAssemblyTemplates");
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Assembly#loadSlots(LoadSlotsRequest)
    */
   public PSTemplateSlot[] loadSlots(
      LoadSlotsRequest loadSlotsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      PSTemplateSlot[] result = null;
      try
      {
         authenticate();

         IPSAssemblyWs service = PSAssemblyWsLocator.getAssemblyWebservice();
         
         List slots = service.loadSlots(
            loadSlotsRequest.getName());

         result = (PSTemplateSlot[]) convert(PSTemplateSlot[].class, slots);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadSlots");
      }
      
      return result;
   }
}
