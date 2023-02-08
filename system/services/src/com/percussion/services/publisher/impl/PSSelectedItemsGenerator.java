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
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSLongValue;
import com.percussion.utils.types.PSPair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Value;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;

/**
 * A generator that returns the items selected by the user for demand
 * publishing. The items passed to the action are stored in the http session in
 * an array with the attribute name sys_itemsToPublish. This is taken and each
 * item examined. Items that are folders are expanded, including subfolders, and
 * the items are found in these.
 * 
 * @author dougrand
 * 
 */
public class PSSelectedItemsGenerator extends PSBaseGenerator
{
   @SuppressWarnings("unused")
   public QueryResult generate(Map<String, String> parameters)
   {
      PSQueryResult qr = createQueryResult();

      String edition = parameters.get(IPSHtmlParameters.SYS_EDITIONID);
      if (StringUtils.isBlank(edition))
         return qr;

      int editionid = Integer.parseInt(edition);
      IPSRxPublisherService rpub = PSRxPublisherServiceLocator
         .getRxPublisherService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      Collection<PSDemandWork> work = rpub.getDemandWorkForEdition(editionid);

      for(PSDemandWork item : work)
      {
         for(PSPair<IPSGuid,IPSGuid> pair : item.getContent())
         {
            PSLocator contentloc = gmgr.makeLocator(pair.getSecond());
            PSLocator folderloc = gmgr.makeLocator(pair.getFirst());
            PSComponentSummary sum = ms_cms.loadComponentSummary(
                  contentloc.getId());
            addIdToResult(qr, sum, folderloc.getId());
         }
      }

      return qr;
   }

   /**
    * For the given id, determine if it is a folder or a content item. Folders
    * are enumerated into sub items and recursed. Items are simply added
    * 
    * @param qr the query result
    * @param sum the summary of the item
    * @param folderid the id of the parent folder
    */
   private void addIdToResult(PSQueryResult qr, PSComponentSummary sum,
         int folderid)
   {
      if (sum.getObjectType() == 1)
      {
         // Item
         Map<String, Object> data = new HashMap<>();
         Value idval = new PSLongValue(sum.getContentId());
         Value folderval = new PSLongValue(folderid);
         data.put(IPSContentPropertyConstants.RX_SYS_CONTENTID, idval);
         data.put(IPSContentPropertyConstants.RX_SYS_FOLDERID, folderval);
         PSRow row = new PSRow(data);
         qr.addRow(row);
      }
      else if (sum.getObjectType() == 2)
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
         try
         {
            PSComponentSummary sums[] = proc.getChildSummaries(sum
                  .getCurrentLocator());
            for (PSComponentSummary s : sums)
            {
               addIdToResult(qr, s, sum.getContentId());
            }
         }
         catch (PSCmsException e)
         {
            ms_log.error("Problem extracting child ids from folder: "
                  + sum.getContentId(), e);
         }
      }
   }

}
