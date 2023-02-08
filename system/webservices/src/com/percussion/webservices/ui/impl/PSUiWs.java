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
package com.percussion.webservices.ui.impl;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.ui.IPSUiWs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The public ui webservice implementations.
 */
public class PSUiWs extends PSUiBaseWs implements IPSUiWs
{
   /*
    * (non-Javadoc)
    * 
    * @see IPSUiWs#loadActions(String)
    */
   @SuppressWarnings("unchecked")
   public List<PSAction> loadActions(String name) throws PSErrorException
   {
      List actions = findComponentsByNameLabel(name, null, FIND_ACTIONS,
         PSAction.XML_NODE_NAME, PSAction.class);
      return actions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiWs#loadDisplayFormats(String)
    */
   public List<PSDisplayFormat> loadDisplayFormats(String name)
      throws PSErrorException
   {
      List<IPSDbComponent> dfs = findComponentsByNameLabel(name, null,
         FIND_DISPLAY_FORMAT, "PSXDisplayFormat", PSDisplayFormat.class);

      Map<IPSGuid, String> idNameMap = getAllCommunities();

      List<PSDisplayFormat> results = new ArrayList<PSDisplayFormat>(dfs.size());
      PSDisplayFormat dspFormat;
      for (IPSDbComponent comp : dfs)
      {
         dspFormat = (PSDisplayFormat) comp;
         dspFormat.setAllowedCommunities(idNameMap);

         results.add(dspFormat);
      }

      return results;
   }

   /**
    * Gets the id and name pairs for all communities.
    * 
    * @return the id / name pairs, never <code>null</code>, may be empty.
    */
   private Map<IPSGuid, String> getAllCommunities()
   {
      IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<PSCommunity> allCommunities = roleMgr.findCommunitiesByName(null);
      Map<IPSGuid, String> idNameMap = new HashMap<IPSGuid, String>();
      for (PSCommunity community : allCommunities)
         idNameMap.put(community.getGUID(), community.getName());

      return idNameMap;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiWs#loadSearches(String)
    */
   @SuppressWarnings("unchecked")
   public List<PSSearch> loadSearches(String name) throws PSErrorException
   {
      return loadSearchViews(name, false);
   }

   /**
    * Loads all searches or views for the supplied name.
    * 
    * @param name the name of the search or view to load, may be
    * <code>null</code> or empty, asterisk wildcards are accepted. All
    * searches or views will be loaded if not supplied or empty.
    * @param isView <code>true</code> if loading views; otherwise loading
    * searches.
    * 
    * @return a list with all loaded searches or views, never <code>null</code>,
    * may be empty, ordered in alpha order by name.
    * 
    * @throws PSErrorException if an error occurs while loading the searches or
    * views.
    */
   private List<PSSearch> loadSearchViews(String name, boolean isView)
      throws PSErrorException
   {
      List<IPSDbComponent> searches = findComponentsByNameLabel(name, null,
         FIND_SEARCHES, PSSearch.XML_NODE_NAME, PSSearch.class);

      List<PSSearch> searchOrViews = getSearchOrViews(searches, isView);
      if (searchOrViews.isEmpty())
         return searchOrViews;

      Map<IPSGuid, String> idNameMap = getAllCommunities();
      List<PSSearch> result = new ArrayList<PSSearch>(searchOrViews.size());
      for (IPSDbComponent comp : searchOrViews)
      {
         PSSearch s = (PSSearch) comp;
         s.setAllowedCommunities(idNameMap);

         result.add(s);
      }

      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiWs#loadViews(String)
    */
   public List<PSSearch> loadViews(String name) throws PSErrorException
   {
      return loadSearchViews(name, true);
   }
}
