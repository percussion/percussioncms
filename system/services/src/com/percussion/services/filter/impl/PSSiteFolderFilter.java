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
package com.percussion.services.filter.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A item filter that mimics the behavior of authtype 101 in part. This filter
 * passes items that exist on the destination site, and that can be published on
 * the destination site by virtue of having a publishable template available.
 * 
 * @author dougrand
 */
public class PSSiteFolderFilter extends PSBaseFilter
{
   /**
    * Site folder filter logger
    */
   private final static Log ms_log = LogFactory
         .getLog(PSSiteFolderFilter.class);

   /** (non-Javadoc)
    * @see com.percussion.services.filter.impl.PSBaseFilter#filter(java.util.List, java.util.Map)
    */
   @Override
   public List<IPSFilterItem> filter(List<IPSFilterItem> ids,
         Map<String, String> params) throws PSFilterException
   {
      // Get default site id from parameters
      String siteidstr = params.get(IPSHtmlParameters.SYS_SITEID);
         
      List<IPSFilterItem> removals = new ArrayList<IPSFilterItem>();
      PSServerFolderProcessor fproc = PSServerFolderProcessor.getInstance();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      
      IPSGuid defaultsiteid = null;
      if (StringUtils.isNotBlank(siteidstr))
      {
         defaultsiteid = new PSGuid(PSTypeEnum.SITE, siteidstr);
      }

      for (IPSFilterItem id : ids)
      {
         IPSGuid siteid = id.getSiteId();
         if (siteid == null)
         {
            if (defaultsiteid == null)
            {
               ms_log.warn("No default siteid available, removing item "
                     + id.getItemId());
               removals.add(id);
               continue;
            }
            siteid = defaultsiteid;
         }
         IPSSite site = null;
         try
         {
            site = smgr.loadUnmodifiableSite(siteid);
         }
         catch (PSNotFoundException e)
         {
            ms_log.warn("Specified site " + siteid
                  + " doesn't exist, removing item " + id.getItemId());
            removals.add(id);
            continue;
         }
         try
         {
            String paths[];
            if (id.getFolderId() != null)
            {
               PSLegacyGuid flg = (PSLegacyGuid) id.getFolderId();
               paths = fproc.getItemPaths(flg.getLocator());
            }
            else
            {
               paths = fproc.getFolderPaths(((PSLegacyGuid) id.getItemId())
                     .getLocator());
            }
            String match = PSStringUtils.findMatchingLeftSubstring(site
                  .getFolderRoot(), paths);
            if (match == null)
            {
               removals.add(id);
               continue;
            }
         }
         catch (PSCmsException e)
         {
            ms_log.warn("Problem getting paths for folder " + id.getFolderId()
                  + " removing item");
            removals.add(id);
            continue;
         }
      }
      ids.removeAll(removals);
      return ids;
   }

}
