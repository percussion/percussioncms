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
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This action is used to remove snippet/s. It removes active assembly
 * relationships corresponding to the comma separated list of relationship ids
 * specified in the (required) <code>relationshipIds</code> parameter.
 * 
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5"> <thead>
 * <th>Name</th>
 * <th>Allowed Values</th>
 * <th>Details</th>
 * </thead> <tbody>
 * <tr>
 * <td>relationshipIds</td>
 * <td>comma separated list of relationship ids</td>
 * <td>Required</td>
 * </tr>
 * </tbody> </table>
 */
public class PSRemoveSnippetAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      String ridParam = (String) getParameterRqd(params, RELATIONSHIP_IDS);
      String[] rids = ridParam.split(",");
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      for (String rid : rids)
      {
         ids.add(mgr.makeGuid(rid,PSTypeEnum.RELATIONSHIP));
      }
      IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
      try
      {
         service.deleteRelationships(ids);
      }
      catch (PSErrorsException es)
      {
         throw createException(es);
      }
      catch (PSErrorException e)
      {
         throw createException(e);
      }
      
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
   
   /**
    * The name of the parameter to specify the to be removed relationship ids.
    */
   public static String RELATIONSHIP_IDS = "relationshipIds";
}
