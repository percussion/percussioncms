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
