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

package com.percussion.services.menus;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSUserSession;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.uicontext.PSManageActionInfo;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.workflow.IPSStatesContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PSActionMenuUtils {

    public static IPSRequestContext checkRequestContext(IPSRequestContext request){
        if(request == null){
            return new PSRequestContext(PSSecurityFilter.getCurrentRequest());
        }else{
            return request;
        }
    }

    public Document processResultDocument(Object[] params,
                                          IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException
    {
        PSManageActionInfo infomgr = new PSManageActionInfo();
        String contentid = request.getParameter("sys_contentid");
        String assignmenttype = request.getParameter("sys_assignmenttype");
        String checkedoutstate = "SomeOneElse";
        String username = null;
        PSRequest req = (PSRequest) PSRequestInfo
                .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        PSUserSession sess = req.getUserSession();

        // Get user identification
        username = req.getServletRequest().getRemoteUser();

        // Don't filter if contentid is not passed
        if (contentid == null)
        {
            return resultDoc;
        }

        // Get the user's roles
        Collection roles = (Collection) sess.getUserRoles();

        IPSStatesContext state = null;
        try
        {
            IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
            List<Integer> ids = new ArrayList<>();
            ids.add(new Integer(contentid));
            List<PSComponentSummary> summaries = cms.loadComponentSummaries(ids);
            if (ids.size() == 0)
            {
                throw new RuntimeException("Could not find content item for id " + contentid);
            }
            PSComponentSummary contentItem = summaries.get(0);

            String whocheckedout = contentItem.getCheckoutUserName();
            if (whocheckedout != null && whocheckedout.equals(username))
            {
                checkedoutstate = IPSConstants.CHECKOUT_STATUS_MYSELF;
            }
            else if (whocheckedout != null && whocheckedout.trim().length() > 0)
            {
                checkedoutstate = IPSConstants.CHECKOUT_STATUS_SOMEONEELSE;
            }
            else
            {
                checkedoutstate = IPSConstants.CHECKOUT_STATUS_NOBODY;
            }

            state = cms.loadWorkflowState(contentItem.getWorkflowAppId(),
                    contentItem.getContentStateId());
        }
        catch (Exception e)
        {
            throw new PSExtensionProcessingException(
                    "Problem loading state information", e);
        }

        String contentvalid = state != null ? state.getContentValidValue() : "n";

        // Get a list of all action ids and ensure that these ids are all
        // loaded into the current user session. The actions and ids are merged
        // with the current information stored in the session.
        Set actionids = new HashSet();
        NodeList list = resultDoc.getElementsByTagName("Action");
        extractActionIds(actionids, list);
        list = resultDoc.getElementsByTagName("ActionList");
        extractActionIds(actionids, list);

        try
        {
            infomgr.ensureActionsLoaded(actionids, request);
        }
        catch (PSException e)
        {
            throw new PSExtensionProcessingException("Problem loading actions", e);
        }

        // Now run through the actions, and for each get the action object.
        // From this we check the visibility of the action and remove the
        // node if appropriate
        Set actionsToRemove = new HashSet();
        list = resultDoc.getElementsByTagName("Action");
        checkActionsForRemoval(request, infomgr, contentvalid, roles,
                assignmenttype, list, actionsToRemove, checkedoutstate);
        Set actionListsToRemove = new HashSet();
        list = resultDoc.getElementsByTagName("ActionList");
        checkActionsForRemoval(request, infomgr, contentvalid, roles,
                assignmenttype, list, actionListsToRemove, checkedoutstate);

        removeUnwantedActions(actionsToRemove);
        removeUnwantedActions(actionListsToRemove);

        return resultDoc;
    }

    /**
     * Remove the elements that were picked out for removal.
     *
     * @param actionsToRemove a {@link Collection}of elements to be removed,
     *           never <code>null</code> but might be empty
     */
    private void removeUnwantedActions(Collection actionsToRemove)
    {
        if (actionsToRemove.size() > 0)
        {
            for (Iterator iter = actionsToRemove.iterator(); iter.hasNext();)
            {
                Element action = (Element) iter.next();
                Node parent = action.getParentNode();
                parent.removeChild(action);
            }
        }
    }

    /**
     * Apply a series of visibility checks to the actions. Actions that fail one
     * or more checks are added to the removal list.
     *
     * @param request the original request context, assumed non-
     *           <code>null</code>
     * @param infomgr the info manager for action information, assumed non-
     *           <code>null</code>
     * @param contentvalid the public state of the content id, assumed non-
     *           <code>null</code>
     * @param roles The user's roles, no assumptions are made, could be
     *           <code>null</code>
     * @param assignmentType The user's assignment type given the roles, assumed
     *           non- <code>null</code>
     * @param list the list of nodes to check, assumed non- <code>null</code>
     * @param actionsToRemove the list of actions to remove, will be modified by
     *           this method, assumed non- <code>null</code>
     * @param checkedoutstate This is assimed to have a value from
     * {@link IPSConstants} indicating the checked out state of the item.
     */
    private void checkActionsForRemoval(IPSRequestContext request,
                                        PSManageActionInfo infomgr, String contentvalid, Collection roles,
                                        String assignmentType, NodeList list, Set actionsToRemove,
                                        String checkedoutstate)
    {
        int count = list.getLength();
        for (int i = 0; i < count; i++)
        {
            Element actionnode = (Element) list.item(i);
            String id = actionnode.getAttribute("actionid");
            if (id != null && id.trim().length() > 0)
            {
                PSActionVisibilityContexts ctxs = infomgr.getActionVisibility(id,
                        request);
                PSActionVisibilityContext visctx = null;
                boolean remove = false;
                visctx = ctxs
                        .getContext(PSActionVisibilityContext.VIS_CONTEXT_CHECKOUT_STATUS);

                // If a user is not in the workflow or has no access through
                // the workflow, then all item actions should be suppressed.
                if (assignmentType.equalsIgnoreCase("1") /* None */ ||
                        assignmentType.equalsIgnoreCase("Default"))
                {
                    if (infomgr.isActionAnItem(id, request)) remove = true;
                }

                if (visctx != null)
                {
                    remove |= visctx.contains(checkedoutstate);
                    if (checkedoutstate.equals(IPSConstants.CHECKOUT_STATUS_SOMEONEELSE))
                    {
                        // TODO: Remove this workaround when "SomeOneElse" has
                        // been removed properly
                        remove |= visctx.contains("SomeOneElse");
                    }
                }

                // Check that the current item's publishable type is contained
                if (contentvalid != null)
                {
                    visctx = ctxs
                            .getContext(PSActionVisibilityContext.VIS_CONTEXT_PUBLISHABLE_TYPE);
                    if (visctx != null)
                    {
                        remove |= visctx.contains(contentvalid);
                    }
                }
                // Check community information
                int community = request.getSecurityToken().getCommunityId();
                visctx = ctxs
                        .getContext(PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY);
                if (visctx != null)
                {
                    String communityid = Integer.toString(community);
                    remove |= visctx.contains(communityid);
                }

                visctx = ctxs
                        .getContext(PSActionVisibilityContext.VIS_CONTEXT_ROLES_TYPE);
                if (visctx != null && roles != null)
                {
                    // Visible if any role is not in the hidden list. For example:
                    // a user is in Admin and Author, but Author is hidden. The user
                    // should have access.
                    boolean allfound = true;
                    for (Iterator iter = roles.iterator(); iter.hasNext();)
                    {
                        String rolename = (String) iter.next();
                        if (! visctx.contains(rolename))
                        {
                            allfound = false;
                            break;
                        }
                    }
                    remove |= allfound;
                }

                visctx = ctxs
                        .getContext(PSActionVisibilityContext.VIS_CONTEXT_ASSIGNMENT_TYPE);

                if (visctx != null)
                {
                    remove |= visctx.contains(assignmentType);
                }

                if (remove)
                {
                    actionsToRemove.add(actionnode);
                }
            }
        }
    }

    /**
     * From a nodelist of action or actionlist nodes, get the actionid attributes
     * and populate the passed in collection.
     *
     * @param actionids a {@link Collection}to populate with the action ids that
     *           are found, assumed non- <code>null</code>
     * @param list the list of dom nodes to examine
     */
    private void extractActionIds(Collection actionids, NodeList list)
    {
        int count = list.getLength();
        for (int i = 0; i < count; i++)
        {
            Element actionnode = (Element) list.item(i);
            String id = actionnode.getAttribute("actionid");
            if (id != null && id.trim().length() > 0)
                actionids.add(id);
        }
    }
}
