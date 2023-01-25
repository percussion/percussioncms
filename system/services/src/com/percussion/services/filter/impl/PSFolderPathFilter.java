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
package com.percussion.services.filter.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.string.PSFolderStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This filter removes items that are not present in the given site folder or
 * subfolders. The folder is specified by the parameter "sys_folderPaths" and
 * may contain one or more paths separated by a semicolon.
 * 
 * @author dougrand
 * 
 */
public class PSFolderPathFilter extends PSBaseFilter
      implements
         IPSItemFilterRule
{
   /**
    * Folder path filter logger
    */
   private static final Logger log = LogManager.getLogger(PSFolderPathFilter.class);

   public List<IPSFilterItem> filter(List<IPSFilterItem> items, Map<String, String> params)
         throws PSFilterException
   {
      List<IPSFilterItem> rval = new ArrayList<>();
      PSRequest req = PSRequest.getContextForRequest();
      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      
      String folderList = params.get("sys_folderPaths");

      if (StringUtils.isBlank(folderList))
      {
         log.warn("No folder path defined, no filtering will occurred");
         return items;
      }

      Pattern[] matchPatterns = PSFolderStringUtils.getFolderPatterns(folderList);

      // Get the folder paths for each id and see if one of the match
      // patterns is a hit, if so add to the return list
      for (IPSFilterItem item : items)
      {         
         // Skip items without folders
         if (item.getFolderId() == null) continue;
         
         if (!(item.getItemId() instanceof PSLegacyGuid))
         {
            log.warn("Found illegal item guid, all must be PSLegacyGuid, skipping {} " ,
                    item.getItemId());
            continue;
         }
         
         if (!(item.getFolderId() instanceof PSLegacyGuid))
         {
            log.warn("Found illegal folder guid, all must be PSLegacyGuid, skipping {}",
                    item.getFolderId());
            continue;
         }

         try
         {
            PSLegacyGuid guid = (PSLegacyGuid) item.getFolderId();
            String[] paths = proc.getItemPaths(guid.getLocator());
            if (PSFolderStringUtils.oneMatched(paths, matchPatterns))
               rval.add(item);
         }
         catch (PSCmsException | PSNotFoundException e)
         {
            log.error("Problem getting folder paths. Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         }
      }

      return rval;
   }



}
