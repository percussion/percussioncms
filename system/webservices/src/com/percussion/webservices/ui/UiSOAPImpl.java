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
package com.percussion.webservices.ui;

import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.ui.data.PSAction;
import com.percussion.webservices.ui.data.PSDisplayFormat;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSViewDef;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyx.wsdl</code> for operations defined in the
 * <code>uiSOAP</code> bindings.
 */
public class UiSOAPImpl extends PSBaseSOAPImpl implements Ui
{
   /*
    * (non-Javadoc)
    * 
    * @see Ui#loadActions(LoadActionsRequest)
    */
   public PSAction[] loadActions(LoadActionsRequest loadActionsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();

      IPSUiWs uiws = PSUiWsLocator.getUiWebservice();
      com.percussion.webservices.ui.data.PSAction[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSAction> actions = 
            uiws.loadActions(loadActionsRequest.getName());
         
         result = (PSAction[]) convert(PSAction[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadActions");
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Ui#loadDisplayFormats(LoadDisplayFormatsRequest)
    */
   public PSDisplayFormat[] loadDisplayFormats(
      LoadDisplayFormatsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();

      IPSUiWs uiws = PSUiWsLocator.getUiWebservice();
      com.percussion.webservices.ui.data.PSDisplayFormat[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSDisplayFormat> actions = 
            uiws.loadDisplayFormats(request.getName());
         
         result = (PSDisplayFormat[]) convert(PSDisplayFormat[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadDisplayFormats");
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Ui#loadSearches(LoadSearchesRequest)
    */
   public PSSearchDef[] loadSearches(LoadSearchesRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();

      IPSUiWs uiws = PSUiWsLocator.getUiWebservice();
      PSSearchDef[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSSearch> actions = 
            uiws.loadSearches(request.getName());
         
         result = (PSSearchDef[]) convert(PSSearchDef[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadSearches");
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Ui#loadViews(LoadViewsRequest)
    */
   public PSViewDef[] loadViews(LoadViewsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      authenticate();

      IPSUiWs uiws = PSUiWsLocator.getUiWebservice();
      PSViewDef[] result = null;
      try
      {
         List<com.percussion.cms.objectstore.PSSearch> actions = 
            uiws.loadViews(request.getName());
         
         result = (PSViewDef[]) convert(PSViewDef[].class, actions);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadViews");
      }
      catch (PSErrorException e)
      {
         // unknown error
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return result;
   }
}
