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
import com.percussion.content.ui.browse.PSContentBrowser;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.Map;

/**
 * 
 */
public class PSGetCreateItemUrlAction extends PSAAActionBase
{

   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      Object obj = getParameter(params, PARAM_NAME_PARENT_FOLDER_PATH);
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '"
            + PARAM_NAME_PARENT_FOLDER_PATH
            + "' is required and cannot be empty for this action");
      }
      String parentPath = obj.toString().trim();

      obj = getParameter(params, IPSHtmlParameters.SYS_CONTENTTYPEID);
      if (obj == null || StringUtils.isBlank(obj.toString()))
         throw new IllegalArgumentException("ctypeid must not be ampty");
      String ctypeid = obj.toString();

      obj = getParameter(params, PARAM_NAME_CATEGORY);
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '"
            + PARAM_NAME_CATEGORY
            + "' is required and cannot be empty for this action");
      }

      String category = obj.toString().trim();

      String result = null;
      String url = null;
      try
      {
         if (category.equals(PARAM_CATEGORY_FOLDERS))
         {
            url = PSContentBrowser.getNewItemUrlByFolderPath(parentPath,
               ctypeid);
         }
         else if (category.equals(PARAM_CATEGORY_SITES))
         {
            url = PSContentBrowser.getNewItemUrlBySiteFolderPath(parentPath,
               ctypeid);
         }
         else
         {
            throw new PSAAClientActionException("Unknown category '" + category
               + "' supplied in the request.");
         }
         JSONObject jsonObj = new JSONObject();
         jsonObj.append("url", url);
         result = jsonObj.toString();
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
   }

}
