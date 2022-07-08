/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.PSActionVisibilityChecker;
import com.percussion.cms.PSActionVisibilityGlobalState;
import com.percussion.cms.PSActionVisibilityObjectState;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemException;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.ui.IPSUiWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class takes a set of <code>PSAction</code> names, an ObjectId and
 * calculates whether each of the requested actions is visible given the
 * supplied context.
 * 
 * @author paulhoward
 */
public class PSGetActionVisibilityAction extends PSAAActionBase
{
   /**
    * For each supplied name, search for a matching <code>PSAction</code> that
    * has that name (case-insensitive.) If found, calculate whether the action
    * would be visible given the context of the supplied id.
    * 
    * @param params Expected: an entry called 'names' whose value is a String[]
    * containing the actions of interest, an entry for the object id. The names
    * are case-insensitive. If an action is not found, <code>false</code> is
    * returned for its value in the map.
    * 
    * @return The value is a <code>Map</code> whose key is the lower-cased
    * name and whose value is a <code>Boolean</code> (<code>true</code> if
    * visible); converted to a JSON string.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      try
      {
         String[] names = (String[]) params.get("names");
         PSAAObjectId itemId = getObjectId(params);
         
         Map<String, Boolean> namesToVisible = new HashMap<String, Boolean>();
         IPSUiWs mgr = PSUiWsLocator.getUiWebservice();
         for (String name : names)
         {
            List<PSAction> actions = mgr.loadActions(name);
            boolean visible = false;
            if (!actions.isEmpty())
            {
               PSAction action = actions.get(0);
               visible = isVisible(itemId, action);
            }
            namesToVisible.put(name.toLowerCase(), visible);
         }

         JSONArray result = new JSONArray();
         result.put(namesToVisible);
         return new PSActionResponse(result.toString(),
               PSActionResponse.RESPONSE_TYPE_JSON);
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e);
      }
   }
   
   /**
    * Does the work of calculating visibility.
    * 
    * @param itemId The context to which the action is to be applied. Assumed
    * not <code>null</code>.
    * 
    * @param action The action to check. Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the supplied action is visible to the
    * supplied item given its context, otherwise <code>false</code>.
    */
   private boolean isVisible(PSAAObjectId itemId, PSAction action)
   {
      PSActionVisibilityChecker visibilityChecker = 
         new PSActionVisibilityChecker(action);
      return visibilityChecker.isVisible(new GlobalState(getRequestContext()),
            new ObjectState(itemId, getRequestContext()));
   }
   
   private class GlobalState extends PSActionVisibilityGlobalState
   {
      /**
       * 
       * @param ctx Assumed not <code>null</code>.
       */
      public GlobalState(IPSRequestContext ctx)
      {
         mi_ctx = ctx;
      }
      
      @Override
      public int getCommunityUuid()
      {
         return getCurrentCommunityUuid();
      }

      @Override
      public String getLocale()
      {
         return mi_ctx.getUserLocale();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Collection<String> getRoles()
      {
         return mi_ctx.getSubjectRoles();
      }
      
      /**
       * Set in ctor, then never null or changed.
       */
      private IPSRequestContext mi_ctx;
   }
   
   private class ObjectState extends PSActionVisibilityObjectState
   {
      /**
       * 
       * @param id Assumed not <code>null</code>.
       */
      public ObjectState(PSAAObjectId id, IPSRequestContext ctx)
      {
         mi_id = id;
         mi_ctx = ctx;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      public int getAssignmentType()
      {
         IPSSystemService svc = PSSystemServiceLocator.getSystemService();
         List<PSAssignmentTypeEnum> types = Collections.emptyList();
         try
         {
            types = svc.getContentAssignmentTypes(
               Collections.singletonList(mi_id.getContentGuid()), 
               getCurrentUser(),
               mi_ctx.getSubjectRoles(),
               getCurrentCommunityUuid());
         }
         catch (PSSystemException e)
         {
            // ignore
            e.printStackTrace();
         }
         return types.isEmpty() ? -1 : types.get(0).getValue();
      }

      @Override
      public String getCheckoutStatus()
      {
         return mi_id.getCheckoutStatus();
      }

      @Override
      public int getContentTypeUuid()
      {
         return Integer.parseInt(mi_id.getContentId());
      }

      @Override
      public PSObjectPermissions getFolderPermissions()
      {
         String s = mi_id.getFolderId();
         if (StringUtils.isBlank(s))
            return null;
         
         int fid = Integer.parseInt(s);
         IPSContentWs cmgr = PSContentWsLocator.getContentWebservice();
         IPSGuid guid = getItemGuid(fid);
         
         PSObjectPermissions perms = null;
         try
         {
            List<PSFolder> folders = 
               cmgr.loadFolders(Collections.singletonList(guid));
            assert(folders.size() == 1);
            perms = folders.get(0).getPermissions();
         }
         catch (PSErrorResultsException e)
         {
            // ignore
            e.printStackTrace();
         }
         return perms;
      }

      @Override
      public int getObjectType()
      {
         return mi_id.getItemSummary().getObjectType();
      }

      @Override
      public String getPublishableType()
      {
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid wfGuid = gmgr.makeGuid(mi_id.getItemSummary()
               .getWorkflowAppId(), PSTypeEnum.WORKFLOW);
         IPSGuid stateGuid = gmgr.makeGuid(mi_id.getItemSummary()
               .getContentStateId(), PSTypeEnum.WORKFLOW_STATE);
         IPSWorkflowService svc = PSWorkflowServiceLocator.getWorkflowService();
         PSState wfState = svc.loadWorkflowState(stateGuid, wfGuid);
         return wfState == null ? StringUtils.EMPTY : 
            wfState.getContentValidValue();
      }

      @Override
      public int getWorkflowAppUuid()
      {
         return mi_id.getItemSummary().getWorkflowAppId();
      }
      
      /**
       * Set in ctor then never <code>null</code> or modified.
       */
      private PSAAObjectId mi_id;
      
      /**
       * Set in ctor then never <code>null</code> or modified.
       */
      private IPSRequestContext mi_ctx;
   }
}
