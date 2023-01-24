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
