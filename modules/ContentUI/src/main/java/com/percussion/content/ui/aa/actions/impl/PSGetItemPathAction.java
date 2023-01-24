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

import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Gets the path corresponding to the supplied item id. Builds the path by
 * getting the folder path and then appending the item name to it. Gets the
 * folder id from the supplied objectId, if it is null then gets the sitefolder
 * id of the item. If folder id is null then returns item name as path..
 */
public class PSGetItemPathAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      String itemPath = "";
      PSAAObjectId objectId = getObjectId(params);
      try
      {
         IPSGuid iguid = getItemGuid(Integer.parseInt(objectId
               .getContentId()));
         IPSGuid fguid = null;
         String fid = objectId.getFolderId();
         if(StringUtils.isBlank(fid))
         {
            IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
            fguid = smgr.getSiteFolderId(new PSGuid(PSTypeEnum.SITE, objectId
                  .getSiteId()), iguid);
         }
         else
         {
            //Create a guid corresponding to the fid.
            fguid = getItemGuid(Integer.parseInt(fid));
         }
         String fpath = "";
         if(fguid!=null)
         {
            fpath = getFolderPath(fguid);
            String fname = PSAAObjectId.getItemSummary(fguid.getUUID()).getName();
            fpath += "/" + fname;
         }
         String iname = PSAAObjectId.getItemSummary(iguid.getUUID()).getName();
         itemPath = fpath + "/" + iname;
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(itemPath,
            PSActionResponse.RESPONSE_TYPE_PLAIN);
   }
   
   /**
    * Returns the path of the supplied folder guid
    * 
    * @param fguid assumed not <code>null</code> and assumed to be a folder
    *           guid.
    * @return path of the supplied folder guid or empty if not found.
    * @throws PSErrorException
    */
   private String getFolderPath(IPSGuid fguid) throws PSErrorException
   {
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      String[] paths = cws.findFolderPaths(fguid);
      return StringUtils.defaultString(paths[0]);
   }

}
