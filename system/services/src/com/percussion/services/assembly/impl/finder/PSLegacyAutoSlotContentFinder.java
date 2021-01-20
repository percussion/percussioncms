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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static Log ms_log = LogFactory
         .getLog(PSLegacyAutoSlotContentFinder.class);
   
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
      Set<ContentItem> rval = new TreeSet<ContentItem>(new ContentItemOrder());
      String path = getValue(selectors, PARAM_RESOURCE, null);
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("The resource parameter is "
               + "required for a legacy auto slot content finder");
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      Map<String,Object> overrides = new HashMap<String,Object>();
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
      List<Integer> cids = new ArrayList<Integer>();
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
