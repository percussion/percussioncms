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
package com.percussion.services.publisher.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.PSContentChangeServiceLocator;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.publisher.PSPublisherException;
import com.percussion.util.IPSHtmlParameters;

import java.util.List;
import java.util.Map;

import javax.jcr.query.QueryResult;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilizes {@link IPSContentChangeService} to generate a content list of changed content
 * 
 * The {@link #generate(Map)} method expects the parameter contentChangeType and sys_siteid
 * 
 * @author JaySeletz
 *
 */
public class PSChangedContentListGenerator extends PSBaseGenerator
{
   private IPSContentChangeService changeService = PSContentChangeServiceLocator.getContentChangeService();
   private static final Logger log = LogManager.getLogger(PSChangedContentListGenerator.class);
   
   public QueryResult generate(Map<String, String> parameters) throws PSPublisherException
   {
      Validate.notEmpty(parameters);
      
      String siteIdStr = parameters.get(IPSHtmlParameters.SYS_SITEID);
      Validate.notNull(siteIdStr);
      Validate.isTrue(NumberUtils.isNumber(siteIdStr), "Invalid siteId parameter: " + siteIdStr);
      long siteId = Long.parseLong(siteIdStr);
      
      String changeType = parameters.get("contentChangeType");
      Validate.notNull(changeType);
      PSContentChangeType contentChangeType = PSContentChangeType.valueOf(changeType);
      
      PSQueryResult results = createQueryResult();
      
      List<Integer> changes = changeService.getChangedContent(siteId, contentChangeType);
      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      for (Integer contentId : changes)
      {
         
         try
         {
            addToResults(results, contentId, getFolderId(contentId, proc));
         }
         catch (Exception e)
         {
            log.error("Unable to process item with id " + contentId + "for incremental publish: " + e.getLocalizedMessage(), e);
         }
      }
      
      return results;
   }


   private int getFolderId(Integer contentId, PSServerFolderProcessor proc) throws PSCmsException
   {
      List<PSLocator> locs = proc.getAncestorLocators(new PSLocator(contentId));
      if (locs.isEmpty())
         return -1;
      return locs.get(locs.size() - 1).getId();
   }

}
