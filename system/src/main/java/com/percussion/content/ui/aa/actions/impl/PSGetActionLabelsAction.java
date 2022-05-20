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

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a set of action names (case-insensitive), it looks them up and returns
 * the corresponding label.
 *
 * @author paulhoward
 */
public class PSGetActionLabelsAction extends PSAAActionBase
{
   /**
    * For each supplied name, search for a matching <code>PSAction</code> that
    * has that name (case-insensitive.) If found, add the name and label to the
    * result.
    * 
    * @param params An entry called 'names' whose value is a String[] containing
    * the actions of interest.
    * 
    * @return The value is a <code>Map</code> whose key is the proper-cased
    * name and whose value is the label that has been converted to a JSON
    * string.
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      try
      {
         String[] names = (String[]) params.get("names");
         
         IPSUiDesignWs uiMgr = PSUiWsLocator.getUiDesignWebservice();
         List<IPSCatalogSummary> actionSums = 
            uiMgr.findActions(null, null, null);
         Map<String, IPSCatalogSummary> namesToSums = 
            new HashMap<String, IPSCatalogSummary>();
         for (IPSCatalogSummary sum : actionSums)
            namesToSums.put(sum.getName().toLowerCase(), sum);
         
         Map<String, String> namesToLabels = new HashMap<String, String>();
         for (String name : names)
         {
            IPSCatalogSummary sum = namesToSums.get(name.toLowerCase());
            if (sum == null)
               continue;
            namesToLabels.put(name, sum.getLabel());
         }
         JSONArray result = new JSONArray();
         result.put(namesToLabels);
         return new PSActionResponse(result.toString(),
               PSActionResponse.RESPONSE_TYPE_JSON);
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e);
      }
   }
}
