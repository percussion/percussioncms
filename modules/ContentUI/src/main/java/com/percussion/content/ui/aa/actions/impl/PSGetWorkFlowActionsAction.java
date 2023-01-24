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

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Return all available workflow actions for the content item based on
 * the current users permissions and the current community.
 * @author erikserating
 *
 */
public class PSGetWorkFlowActionsAction extends PSAAActionBase
{

    /* (non-Javadoc)
     * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public PSActionResponse execute(Map<String, Object> params) throws PSAAClientActionException
    {
        try
        {
            PSAAObjectId objectId = getObjectId(params);
            List contentids = new ArrayList();
            IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
            contentids.add(guidMgr.makeGuid(new PSLocator(objectId.getContentId())));
            
            IPSRequestContext reqCtx = this.getRequestContext();
            int userCommunity = -1;
             String usercomm = (String)reqCtx.getSessionPrivateObject(
                 IPSHtmlParameters.SYS_COMMUNITY);
             if(usercomm != null)
                 userCommunity = Integer.parseInt(usercomm);
             String userName = reqCtx.getUserName();
             String locale = reqCtx.getUserLocale();
             List userRoles = reqCtx.getSubjectRoles();

            IPSSystemService sysSvc = PSSystemServiceLocator.getSystemService();
            List assignmentTypes = sysSvc.getContentAssignmentTypes(contentids,  
                  userName, userRoles, userCommunity);

             IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
             List actions = svc.getAllWorkflowActions(contentids, 
                 assignmentTypes, userName, userRoles, locale);
             JSONArray results = new JSONArray();
             for(int i=0;i<actions.size();i++)
             {
                 JSONObject current = new JSONObject();
                 PSMenuAction action = (PSMenuAction)actions.get(i);
                 String wfAction = action.getParameter("WFAction");
                 
                 //Add name to the array
                 String name = action.getName();
                 current.put("name", name);
                 
                 //Add comment to array
                 String comment = action.getCommentRequired();
                 String commentVal = "1";//Optional
                 if(comment.equals(PSMenuAction.VAL_BOOLEAN_TRUE))
                     commentVal = "2"; //Required
                 else if(comment.equals(PSMenuAction.VAL_HIDE))
                     commentVal = "0";//Hide
                 current.put("comment", commentVal);
                 
                 //Add adhoc to the array            
                 current.put("adhoc", action.isAdhoc() ? "1" : "0");
                                  
                 //Add action type to array
                 String actionType = "4"; //Transition
                 if(name.equals(PSMenuAction.CHECKIN_ACTION_NAME))
                     actionType = "0"; //Checkin
                 else if(name.equals(PSMenuAction.FORCE_CHECKIN_ACTION_NAME))
                     actionType = "1";//Force Checkin
                 else if(name.equals(PSMenuAction.CHECKOUT_ACTION_NAME))
                     actionType = "2"; //Checkout
                 else if(wfAction.equals("Quick Edit"))
                     actionType = "3"; //Transition and Checkout
                 current.put("actiontype", actionType);
                 
                 //Add wfaction name
                 current.put("actionname", wfAction);
                 
                 //Add transition
                 current.put("transitionid", action.getParameter(IPSHtmlParameters.SYS_TRANSITIONID));
                 
                 //Add action label to array
                 @SuppressWarnings("unused")
                 String label = action.getLabel();
                 if(wfAction.equals("Quick Edit"))
                     label = "Quick Edit Check-out";
                 current.put("label", action.getLabel());                 
                 
                 results.put(current);
             }
             return new PSActionResponse(results.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
        }       
        catch (Exception e)
        {
            throw new PSAAClientActionException(e);
        }
        
    }

}
