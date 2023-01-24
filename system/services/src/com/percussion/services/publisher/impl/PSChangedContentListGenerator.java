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
