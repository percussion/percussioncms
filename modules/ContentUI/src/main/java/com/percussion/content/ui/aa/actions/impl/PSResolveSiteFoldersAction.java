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
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Resolves the id values for the passed in site and site folder.
 *
 */
public class PSResolveSiteFoldersAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      Object folderPath = getParameter(params, FOLDER_PATH);
      Object siteName = getParameter(params, SITE_NAME);
      Map<String, IPSGuid> siteFolder;
      try
      {
         siteFolder = PSActionUtil.resolveSiteFolders(siteName, folderPath);
         
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      IPSGuid folderGuid = siteFolder.get(IPSHtmlParameters.SYS_FOLDERID);
      IPSGuid siteGuid = siteFolder.get(IPSHtmlParameters.SYS_SITEID);
      JSONObject result = new JSONObject();      
      try
      {
         if(folderGuid != null)
            result.put(IPSHtmlParameters.SYS_FOLDERID, 
               Integer.toString(folderGuid.getUUID()));
         if(siteGuid != null)
            result.put(IPSHtmlParameters.SYS_SITEID,
               Integer.toString(siteGuid.getUUID()));
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
   }
   
   /**
    * Parameter names of this action.
    */
   public static String FOLDER_PATH = "folderPath";
   public static String SITE_NAME = "siteName";


}
