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
