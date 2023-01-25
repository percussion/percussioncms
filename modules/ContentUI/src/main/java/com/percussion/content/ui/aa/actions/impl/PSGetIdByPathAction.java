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
