/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
package com.percussion.services.filter.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.string.PSFolderStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            log.error("Problem getting folder paths. Error: {}", e.getMessage());
            log.debug(e.getMessage(),e);
         }
      }

      return rval;
   }



}
