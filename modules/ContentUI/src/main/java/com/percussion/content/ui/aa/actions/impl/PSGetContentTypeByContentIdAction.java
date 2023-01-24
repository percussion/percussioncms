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
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.util.IPSHtmlParameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Returns the content type of the content item id passed in.
 * Expects one parameter sys_contentid which is the content id of
 * the content item in question.
 */
public class PSGetContentTypeByContentIdAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      Object contentId = getParameter(params, IPSHtmlParameters.SYS_CONTENTID);
      JSONObject result = new JSONObject();      
      try
      {
         PSComponentSummary summary = 
            PSAAObjectId.getItemSummary(Integer.parseInt(contentId.toString()));
         result.put(IPSHtmlParameters.SYS_CONTENTTYPEID,
            summary.getContentTypeGUID().getUUID());
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
   }

}
