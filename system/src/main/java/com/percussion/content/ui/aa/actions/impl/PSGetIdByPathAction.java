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

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Returns a JSONObject consisting of id and type for the given path. If the
 * object corresponds to the path is folder then returns type as "folder", if it
 * corresponds to item returns "item".
 * 
 * <pre>
 *   id: &lt;content id/folderid&gt;
 *   type:&lt;item:folder&gt;
 * </pre>
 * 
 */
public class PSGetIdByPathAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      Object path = getParameter(params, "path");
      if (path == null || StringUtils.isBlank(path.toString()))
      {
         throw new PSAAClientActionException("path must not be null or empty.");
      }
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      IPSGuid id = null;
      JSONObject obj = new JSONObject();
      try
      {
         id = cws.getIdByPath(path.toString());
         if (id == null)
         {
            throw new PSAAClientActionException(
                  "No item/folder exists with the supplied path: '"
                        + path.toString() + "'");
         }
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = objMgr.loadComponentSummary(id.getUUID());
         obj.append("id", id.getUUID());
         String type = summary.isFolder() ? "folder" : "item";
         obj.append("type", type);
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e);
      }
      catch (JSONException e)
      {
         // ignore
      }
      return new PSActionResponse(obj.toString(),
            PSActionResponse.RESPONSE_TYPE_JSON);
   }
}
