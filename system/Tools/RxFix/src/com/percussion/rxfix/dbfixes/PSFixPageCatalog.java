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

package com.percussion.rxfix.dbfixes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.rxfix.IPSFix;
import com.percussion.rxfix.PSFixResult.Status;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * 
 * @author chriswright
 *
 */
public class PSFixPageCatalog extends PSFixDBBase implements IPSFix
{
   private static final Logger log = LogManager.getLogger(PSFixPageCatalog.class.getName());

   public PSFixPageCatalog() throws NamingException, SQLException
   {
      super();
   }

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);

      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();

      List<IPSSite> sites = smgr.findAllSites();
      for (int i = 0; i < sites.size(); i++)
      {
         IPSSite site = sites.get(i);
         String folderRoot = site.getFolderRoot();
         List<Integer> ids = null;
         if (StringUtils.isNotBlank(folderRoot))
         {
            try
            {
               ids = contentWs.findItemIdsByFolder(folderRoot + "/" + ".system/PageCatalog");
            }
            catch (PSErrorException e)
            {
               log.error("Error encountered when finding items with folderRoot: {} Message: {}" , folderRoot, e);
               continue;
            }
            if (ids == null || ids.isEmpty())
            {
               logInfo(null, "There are no items in the page catalog that need to be removed for site: " + folderRoot);
               continue;
            }
            else
            {
               for (Integer id : ids)
               {
                  log(preview ? Status.PREVIEW : Status.SUCCESS, Integer.toString(id), (preview
                        ? "Would remove "
                        : "Removed ") + "from page catalog");
               }
            }
            if (!preview)
            {
               try
               {
                  List<IPSGuid> guids = new ArrayList<>();
                  for(Integer id : ids){
                     guids.add(PSGuidUtils.makeGuid(id.longValue(),PSTypeEnum.LEGACY_CONTENT));
                  }
                  log.info("Deleting {} items from Page Catalog cache...",guids.size());
                  contentWs.deleteItems(guids);
               }
               catch (PSErrorsException | PSErrorException e)
               {
                  log.error("Error removing items from page catalog for site: {} Message: {}" , folderRoot, e);
               }

            }
         }
      }
   }

   @Override
   public String getOperation()
   {
      return "Removes items currently left in the page catalog.";
   }

}
