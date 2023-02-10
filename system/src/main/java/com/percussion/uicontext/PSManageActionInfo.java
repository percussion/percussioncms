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
package com.percussion.uicontext;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Load and manage action information in the user session. Used by the filtering
 * exit {@link com.percussion.uicontext.PSFilterContextMenu}to obtain
 * information about each action.
 */
public class PSManageActionInfo
{
   /**
    * The component processor proxy. Initialized on demand.
    */
   private PSComponentProcessorProxy m_componentProcessor = null;

   /**
    * Ensure that the list of action ids are known about in the current request
    * context's session.
    * 
    * @param actionids A list of action ids, must never be <code>null</code>
    * @param request The current request context, must never be
    *           <code>null</code>
    * @throws PSException if a problem occurs loading the action information
    */
   public void ensureActionsLoaded(Collection actionids,
         IPSRequestContext request) throws PSException
   {
      if (actionids == null)
      {
         throw new IllegalArgumentException("actionids must never be null");
      }
      if (request == null)
      {
         throw new IllegalArgumentException("request must never be null");
      }
      Collection known = knownActionIds(request);
      actionids.removeAll(known);
      loadActionIds(actionids, request);
   }

   /**
    * Get the visibility for a specific action.
    * 
    * @param actionid the id of the action, must never be <code>null</code> or
    *           empty
    * @param req the current request context, must never be <code>null</code>
    * @return the visibility contexts or <code>null</code> if the action is
    *         not loaded.
    */
   public PSActionVisibilityContexts getActionVisibility(String actionid,
         IPSRequestContext req)
   {
      if (actionid == null || actionid.trim().length() == 0)
      {
         throw new IllegalArgumentException("actionid may not be null or empty");
      }
      if (req == null)
      {
         throw new IllegalArgumentException("req must never be null");
      }

      Map actionMap = getOrCreateActionMap(req);
      PSAction action = (PSAction) actionMap.get(actionid);
      if (action != null)
         return action.getVisibilityContexts();
      else
         return null;
   }
   
   /**
    * Check the given action to see if it is an item. 
    * @param actionid the id of the action, must never be <code>null</code> or
    *           empty
    * @param req the current request context, must never be <code>null</code>
    * @return <code>true</code> if the given action is an item and not a menu
    */
   public boolean isActionAnItem(String actionid, IPSRequestContext req)
   {
      if (actionid == null || actionid.trim().length() == 0)
      {
         throw new IllegalArgumentException("actionid may not be null or empty");
      }
      if (req == null)
      {
         throw new IllegalArgumentException("req must never be null");
      }
      Map actionMap = getOrCreateActionMap(req);
      PSAction action = (PSAction) actionMap.get(actionid);
      if (action != null)
         return action.isMenuItem();
      else
         return false;
   }

   /**
    * Load the specified action ids into internal information.
    * 
    * @param actionids The action ids to load, assume not <code>null</code>
    * @param request The request context to use, assume not <code>null</code>
    * @throws PSException if a problem occurs loading the action information
    */
   private void loadActionIds(Collection actionids, IPSRequestContext request)
         throws PSException
   {
      if (actionids.size() == 0) return;
      
      Map actionMap = getOrCreateActionMap(request);
      PSComponentProcessorProxy cpp = getComponentProcessor(request);
      String actionIdVector[] = new String[actionids.size()];
      
      int i = 0;
      
      for (Iterator iter = actionids.iterator(); iter.hasNext();)
      {
         actionIdVector[i++] = (String) iter.next();
      }
      
      PSAction actions[] = loadActions(cpp, actionIdVector);
      
      for(i = 0; i < actions.length; i++)
      {
         PSAction action = actions[i];
         String actionid = action.getLocator().getPart();
         if (action != null)
         {
            actionMap.put(actionid, action);
         }
      }
   }

   /**
    * Check the passed request context. If the action map isn't there yet,
    * create it and place it in the request context.
    * 
    * @param request request context to check, assume not <code>null</code>
    * @return a {@link Map}, never <code>null</code>
    */
   private Map getOrCreateActionMap(IPSRequestContext request)
   {
      Map rval = (Map) request.getSessionPrivateObject("RX_ACTION_MAP");
      if (rval == null)
      {
         rval = new HashMap();
         request.setSessionPrivateObject("RX_ACTION_MAP", rval);
      }
      return rval;
   }

   /**
    * Returns a list of known action ids, which are the keys in the action map
    * 
    * @param request the request context to use in looking up the known action
    *           ids, assumed not <code>null</code>
    * @return a {@link Collection}of known action ids, never <code>null</code>
    *         but may be empty
    */
   private Collection knownActionIds(IPSRequestContext request)
   {
      Map actionMap = getOrCreateActionMap(request);
      return actionMap.keySet();
   }

   /**
    * Loads the specified actions from the repository.
    * @param proc The processor to use, may not be <code>null</code>.
    * @param ids An array of action ids, may not be <code>null</code> or empty.
    *           Additionally, no element may be empty.
    * @return The actions, may be empty, but never null. Specific elements may
    *         be <code>null</code> if the corresponding action isn't found or
    *         the action does not meet the leaf state passed.
    * @throws PSException if there is a problem with loading the action
    *            elements.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSAction[] loadActions(PSComponentProcessorProxy proc,
         String ids[]) throws PSException
   {
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids may not be null or empty");
      PSKey keys[] = new PSKey[ids.length];
      for (int i = 0; i < ids.length; i++)
      {
         String id = ids[i];
         if (id == null || id.trim().length() == 0)
         {
            throw new IllegalArgumentException("No id may not be null or empty");
         }
         keys[i] = PSAction.createKey(id);
      }

      PSAction result[] = new PSAction[ids.length];
      Element[] elements = proc.load(PSAction.getComponentType(PSAction.class),
            keys);
      for (int j = 0; j < elements.length; j++)
      {
         PSAction action = new PSAction(elements[j]);
         result[j] = action;
      }

      return result;
   }

   /**
    * Determines if the supplied action is a leaf (an item or a dynamic menu,
    * not a cascading category).
    * 
    * @param action The action, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it is a leaf, <code>false</code>
    *         otherwise.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected boolean isLeaf(PSAction action)
   {
      if (action == null)
         throw new IllegalArgumentException("action may not be null");

      return (!action.isCascadedMenu());
   }

   /**
    * Gets the local component processor shared by all dependency handlers. Sets
    * the context using the supplied request.
    * 
    * @param req The request context to use to set the context, assumed not
    *           <code>null</code>.
    * 
    * @return The processor, never <code>null</code>.
    * 
    * @throws PSDeployException if the processor cannot be created.
    */
   private PSComponentProcessorProxy getComponentProcessor(IPSRequestContext req)
         throws PSCmsException
   {
      if (m_componentProcessor == null)
      {
         m_componentProcessor = new PSComponentProcessorProxy(
               PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, req);
      }
      else
      {
         m_componentProcessor.setProcessorContext(req);
      }

      return m_componentProcessor;
   }
}
