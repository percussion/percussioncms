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
