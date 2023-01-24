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
package com.percussion.services.assembly.impl.finder;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The auto slot content finder allows a slot to be filled with items returned
 * by a legacy application. In the future this will also allow searches to be
 * specified using some syntax. <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>"legacy" says to use the named application resource</td>
 * </tr>
 * <tr>
 * <td>resource</td>
 * <td>Specifies the path to the resource if the type is "legacy"</td>
 * </tr>
 * <tr>
 * <td>template</td>
 * <td>The template to use to format the returned items</td>
 * </tr>
 * </table>
 * 
 * @author dougrand
 * 
 */
public class PSLegacyAutoSlotContentFinder extends PSSlotContentFinderBase
{
   /**
    * Logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSLegacyAutoSlotContentFinder.class);
   
   /**
    * These selectors and arguments should be removed before calling the 
    * internal request. These are used by the finder. 
    */
   private static final String[] ms_removeFromParams =
      {"resource", "template"};

   /*
    * (non-Javadoc)
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {
      return Type.AUTOSLOT;
   }
   
   protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
   {
      Set<ContentItem> rval = new TreeSet<>(new ContentItemOrder());
      String path = getValue(selectors, PARAM_RESOURCE, null);
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("The resource parameter is "
               + "required for a legacy auto slot content finder");
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      Map<String,Object> overrides = new HashMap<>();
      for(String key : selectors.keySet())
      {
         overrides.put(key, getValue(selectors, key, ""));
      }
      for(String key : ms_removeFromParams)
      {
         overrides.remove(key);
      }
      PSInternalRequest ireq = PSServer.getInternalRequest(path, req,
            overrides, false, null);
      List<Integer> cids = new ArrayList<>();
      try
      {
         Document result = ireq.getResultDoc();
         NodeList linkurls = result.getElementsByTagName("linkurl");
         int count = linkurls.getLength();
         for (int i = 0; i < count; i++)
         {
            Element linkurl = (Element) linkurls.item(i);
            String varid = linkurl.getAttribute("variantid");
            String conid = linkurl.getAttribute("contentid");
            int contentid = Integer.parseInt(conid);
            IPSGuid template = new PSGuid(PSTypeEnum.TEMPLATE, varid);
            cids.clear();
            cids.add(contentid);
            PSComponentSummary sum = cms.loadComponentSummaries(cids)
                  .get(0);
            IPSGuid item = new PSLegacyGuid(sum.getCurrentLocator());
            rval.add(new ContentItem(item, template, i));
         }
      }
      catch (PSInternalRequestCallException e)
      {
         ms_log.error(e);
      }

      return rval;
   }

}
