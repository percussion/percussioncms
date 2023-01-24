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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This action provides all workflow related operations, checkin, checkout
 * and transition items.
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>  
 * <th>Name</th><th>Allowed Values</th><th>Details</th> 
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@link #OPERATION}</td>
 * <td>It must be one of the following: {@link  #CHECK_IN}, 
 * {@link  #CHECK_OUT} and {@link  #TRANSITION}</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #CONTENT_ID}</td><td>The content id</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #COMMENT}</td><td>The comment for this operation</td><td>Optional</td>
 * </tr>
 * <tr>
 * <td>{@link #TRIGGER_NAME}</td><td>The trigger name of the transition</td>
 * <td>It is required if the operation is {@link  #TRANSITION}</td>
 * </tr>
 * <tr>
 * <td>{@link #ADHOC_USERS}</td><td>A list of adhoc users for a transition. 
 * It is a string with ';' delimiter between the user names.</td>
 * <td>Optional parameter if the operation is {@link  #TRANSITION}</td>
 * </tr>
 * </tbody>
 * </table>
 *
 */
public class PSWorkflowAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String operation = (String) getParameterRqd(params, OPERATION);
      int contentId = getValidatedInt(params, CONTENT_ID, true);
      String comment = (String) getParameter(params, COMMENT);
      
      List<IPSGuid> ids = Collections.singletonList(getItemGuid(contentId));
      if (operation.equals(CHECK_IN))
      {
         checkinItem(ids, comment);
      }
      else if (operation.equals(CHECK_OUT))
      {
         checkoutItem(ids, comment);
     }
      else if (operation.equals(TRANSITION))
      {
         transitionItem(ids, comment, params);
      }
      else if (operation.equals(TRANSITION_CHECKOUT))
      {
         transitionItem(ids, comment, params);
         checkoutItem(ids, comment);
      }
      else
      {
         throw new IllegalArgumentException("Unknown operation, \"" + operation
                  + "\".");
      }

      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

   /**
    * Check in the specified content ids with the given comment and user name.
    * 
    * @param ids the ids of the to be checked in items, assumed not 
    *    <code>null</code> or empty.
    * @param comment the check in comment, may be <code>null</code> or empty.
    * @throws PSAAClientActionException if an error occurs.
    */
   private void checkinItem(List<IPSGuid>ids, String comment)
      throws PSAAClientActionException
   {
      try
      {
         IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
         cservice.checkinItems(ids, comment);
      }
      catch (PSErrorsException e)
      {
         throw createException(e);
      }      
   }
   
   /**
    * Check out the specified content ids with the given comment and current 
    * user.
    * 
    * @param ids the ids of the to be checked in items, assumed not 
    *    <code>null</code> or empty.
    * @param comment the check out comment, may be <code>null</code> or empty.
    * @throws PSAAClientActionException if an error occurs.
    */
   private void checkoutItem(List<IPSGuid>ids, String comment)
   throws PSAAClientActionException
   {
      try
      {
         IPSContentWs cservice = PSContentWsLocator.getContentWebservice();
         cservice.checkoutItems(ids, comment);
      }
      catch (PSErrorsException e)
      {
         throw createException(e);
      }      
   }

   /**
    * Transition the specified content ids with the given comment,
    * and other parameters.
    * 
    * @param ids the ids of the to be checked in items, assumed not 
    *    <code>null</code> or empty.
    * @param comment the check in comment, may be <code>null</code> or empty.
    * @param params it contains other parameters, assumed not <code>null</code> 
    *    or empty.
    * @throws PSAAClientActionException if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private void transitionItem(List<IPSGuid>ids, String comment, Map<String, 
      Object> params) throws PSAAClientActionException
   {
      try
      {
         IPSSystemWs sysService = PSSystemWsLocator.getSystemWebservice();
         String trigger = (String)getParameterRqd(params, TRIGGER_NAME);
         
         // get the adhoc users if defined.
         List<String> addhocUsers = null;
         String users = (String) getParameter(params, ADHOC_USERS);
         if (users != null && users.length() > 0)
         {
            addhocUsers = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(users, ";");
            while (st.hasMoreTokens()) 
               addhocUsers.add(st.nextToken());            
         }
         
         sysService.transitionItems(ids, trigger, comment, addhocUsers);
      }
      catch (PSErrorsException es)
      {
         throw createException(es);
      }
      catch (PSErrorException e)
      {
         throw createException(e);
      }
   }
   
   /**
    * The name of the operation parameter
    */
   public static String OPERATION = "operation";

   /**
    * The operation value for check in
    */
   public static String CHECK_IN = "checkIn";

   /**
    * The operation value for check out
    */
   public static String CHECK_OUT = "checkOut";

   /**
    * The operation value for transition
    */
   public static String TRANSITION = "transition";

   /**
    * The operation value for transition and checkout
    */
   public static String TRANSITION_CHECKOUT = "transition_checkout";

   /**
    * The name of the content-id parameter
    */
   public static String CONTENT_ID = "contentId";

   /**
    * The name of the comment parameter.
    */
   public static String COMMENT = "comment";

   /**
    * The name of the parameter for (transition) trigger name
    */
   public static String TRIGGER_NAME = "triggerName";

   /**
    * The name of the adhoc users parameter
    */
   public static String ADHOC_USERS = "adHocUsers";
}
