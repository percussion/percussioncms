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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rxfix.dbfixes;

import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

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
               log.error("Error encountered when finding items with folderRoot: " + folderRoot, e);
               continue;
            }
            if (ids == null || ids.size() <= 0)
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
                  contentWs.removeFolderChildren(folderRoot + "/" + ".system/PageCatalog", null, true);
               }
               catch (PSErrorsException e)
               {
                  log.error("Error removing child folders from page catalog for site: " + folderRoot, e);
               }
               catch (PSErrorException e)
               {
                  log.error("Error removing child folders from page catalog for site: " + folderRoot, e);
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
