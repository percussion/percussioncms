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
