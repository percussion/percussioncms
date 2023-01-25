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

import java.util.Map;

/**
 * Implementation of the create folder action.
 */
public class PSCreateFolderAction extends PSAAActionBase
{
   /**
    * todo document the required and optional parameters in the map.
    */
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
      String parentFolderPath = obj.toString().trim();

      obj = getParameter(params, "folderName");
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '" + "folderName"
            + "' is required and cannot be empty for this action");
      }
      String folderName = obj.toString();

      obj = getParameter(params, PARAM_NAME_CATEGORY);
      if (obj == null || obj.toString().trim().length() == 0)
      {
         throw new PSAAClientActionException("Parameter '"
            + PARAM_NAME_CATEGORY
            + "' is required and cannot be empty for this action");
      }

      String category = obj.toString().trim();

      String result = null;
      if (category.equals(PARAM_CATEGORY_FOLDERS))
      {
         try
         {
            result = PSContentBrowser.createFolder(getRequestContext(),
               parentFolderPath, folderName);
         }
         catch (Exception e)
         {
            throw new PSAAClientActionException(e);
         }
      }
      else if (category.equals(PARAM_CATEGORY_SITES))
      {
         try
         {
            result = PSContentBrowser.createSiteFolder(getRequestContext(),
               parentFolderPath, folderName);
         }
         catch (Exception e)
         {
            throw new PSAAClientActionException(e);
         }
      }
      else
      {
         throw new PSAAClientActionException("Unknown category '" + category
            + "' supplied in the request.");
      }
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
   }
}
