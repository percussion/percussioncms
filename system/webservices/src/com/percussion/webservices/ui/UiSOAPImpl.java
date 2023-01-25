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
package com.percussion.webservices.ui;

import com.percussion.error.PSExceptionUtils;
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
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
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
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
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
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
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
         throw new RemoteException(PSExceptionUtils.getMessageForLog(e));
      }
      
      return result;
   }
}
