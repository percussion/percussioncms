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

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to get the parent ids of the inline links, where the
 * parent/owner of the inline links is one of the managed objects (slot, snippet
 * or field) and the dependent of the links equals to the specified content id.
 *
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>
 * <th>Name</th><th>Allowed Values</th><th>Details</th>
 * </thead>
 * <tbody>
 * <tr>
 * <td>{@link #MANAGED_IDS}</td><td>The ids of the managed objects</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>{@link #DEPENDENT_ID}</td><td>The dependent id</td><td>Required</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class PSGetInlinelinkParentsAction extends PSAAActionBase
{
   /*
    *  (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      int dependentId = getValidatedInt(params, DEPENDENT_ID, true);
      Set<Integer> ids = getManagedIds(params);
      Set<Integer> ownerIds = getInlinelinkOwners(dependentId);

      ownerIds.retainAll(ids);
      JSONArray jarray = new JSONArray(ownerIds);

      return new PSActionResponse(jarray.toString(),
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

   /**
    * Gets the owner ids of the inline links where the dependent id is the given
    * dependent id.
    *
    * @param dependentId the id of the dependent.
    *
    * @return the owner ids, never <code>null</code>, but may be empty.
    */
   private Set<Integer> getInlinelinkOwners(int dependentId)
   {
      Set<Integer> ids = new HashSet<Integer>();

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependentId(dependentId);
      filter.limitToEditOrCurrentOwnerRevision(true);

      IPSContentWs service = PSContentWsLocator.getContentWebservice();
      try
      {
         List<PSAaRelationship> rels = service.loadContentRelations(filter,
                  false);
         String p;
         for (PSAaRelationship r : rels)
         {
            p = r.getProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP);
            if (StringUtils.isNotBlank(p))
               ids.add(new Integer(r.getOwner().getId()));
         }
      }
      catch (PSErrorException e)
      {
         throw new RuntimeException(e); // unknown exception
      }

      return ids;
   }

   /**
    * Gets the managed ids from the given parameters.
    *
    * @param params the parameters that contains the ids, assumed not
    *    <code>null</code>.
    *
    * @return the managed ids, never <code>null</code>, but may be empty.
    */
   private Set<Integer> getManagedIds(Map<String, Object> params)
   {
      String mids = (String)getParameter(params, MANAGED_IDS);
      if (StringUtils.isBlank(mids))
      {
         throw new IllegalArgumentException("Parameter '" + MANAGED_IDS
                  + "' must not be null or empty.");
      }
      JSONArray jarray = null;
      try
      {
         jarray = new JSONArray(mids);
      }
      catch (JSONException e)
      {
         throw new IllegalArgumentException("Parameter '" + MANAGED_IDS
                  + "' must be a JSON array of integers.");
      }

      Set<Integer> ids = new HashSet<Integer>();
      for (int i=0; i<jarray.length(); i++)
      {
         try
         {
            int n = jarray.getInt(i);
            ids.add(new Integer(n));
         }
         catch (JSONException e)
         {
            throw new IllegalArgumentException("Parameter '" + MANAGED_IDS
                     + "' must be a JSON array of integers.");
         }
      }
      return ids;
   }

   /**
    * Input parameter names of this action.
    */
   public static String MANAGED_IDS = "managedIds";
   public static String DEPENDENT_ID = "dependentId";
}
