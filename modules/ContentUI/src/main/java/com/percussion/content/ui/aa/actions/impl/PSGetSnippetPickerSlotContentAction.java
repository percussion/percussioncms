/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.assembly.jexl.PSDocumentUtils;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Gets the slot content that is used for snippet picker dialog. Builds the slot
 * content by getting the slot items and wrapping them with div tags. This
 * action can be used for getting the slot content as snippets or just titles.
 * If parameter with name "isTitles" exists with a value of true then returns
 * titles.
 */
public class PSGetSnippetPickerSlotContentAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util
    * .Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String showTitlesParam = (String) getParameter(params, "isTitles");
      boolean showTitles = true;
      if (StringUtils.isBlank(showTitlesParam)
            || !showTitlesParam.equalsIgnoreCase("true"))
      {
         showTitles = false;
      }
      String output = null;
      try
      {
         output = showTitles ? getSnippetTitles(objectId)
               : getAssembledSnippets(objectId);
      }
      catch (Throwable e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(output, PSActionResponse.RESPONSE_TYPE_HTML);
   }

   /**
    * Assumes the given object id is of a slot. Gets the assembly results of the
    * snippets for that slot and wraps each snippet body in a div and returns
    * the string. Adds class attribute with value as PSAASnippetPickerItem and
    * rid attribute with relationship id of the snippet as value.
    * 
    * @param objectId assumed to be a valid object id corresponding to a slot.
    * @return a string corresponding to assembly snippets wrapped in divs, never
    * null may be empty.
    * @throws Throwable
    */
   private String getAssembledSnippets(PSAAObjectId objectId) throws Throwable
   {
      Map<String, String[]> assemblyParams = com.percussion.content.ui.aa.actions.impl.PSActionUtil.getAssemblyParams(
            objectId, getCurrentUser());
      PSPair<IPSAssemblyItem, IPSAssemblyResult> pair = com.percussion.content.ui.aa.actions.impl.PSActionUtil
            .assemble(assemblyParams);
      IPSTemplateSlot slot = PSActionUtil.loadSlot(objectId.getSlotId());
      PSAssemblerUtils autils = new PSAssemblerUtils();
      List<IPSAssemblyResult> results = autils.assemble(pair.getFirst(), slot,
            null);
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < results.size(); i++)
      {
         IPSAssemblyResult result = results.get(i);
         String cssClass = "PSAASnippetPickerItem";
         String begin = "<div class=\""
               + cssClass
               + "\" rid=\""
               + result.getParameterValue(
                     IPSHtmlParameters.SYS_RELATIONSHIPID, "") + "\">";
         String end = "</div>";
         sb.append(begin);
         sb.append(new PSDocumentUtils().extractBody(result));
         sb.append(end);
      }
      return sb.toString();
   }

   /**
    * Assumes the given object id is of a slot. Gets the relationship results
    * for that slot and wraps each item's title in a div and returns the string.
    * Adds class attribute with value as PSAASnippetPickerTitle and rid
    * attribute with relationship id of the snippet as value.
    * 
    * @param objectId assumed to be a valid object id corresponding to a slot.
    * @return a string corresponding to titles of the related items wrapped in
    * divs, never null may be empty.
    * @throws Throwable
    */
   private String getSnippetTitles(PSAAObjectId objectId)
      throws PSCmsException
   {
      PSRelationshipProcessor relProc = new PSRelationshipProcessor();
//            (PSRequest) PSRequestInfo
//                  .getRequestInfo(PSRequestInfo.KEY_PSREQUEST));
      PSRelationshipFilter filter = new PSRelationshipFilter();
      PSLocator ownerLocator = new PSLocator(objectId.getContentId(),
            getCorrectRevision(objectId));
      filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
      filter.setOwner(ownerLocator);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, objectId.getSlotId());
      PSRelationshipSet dependents = relProc.getRelationships(filter);
      Iterator<PSRelationship> iter = dependents.iterator();
      List<Integer> ridList = new ArrayList<Integer>();
      List<Integer> idList = new ArrayList<Integer>();
      List<RelatedItems> objects = new ArrayList<RelatedItems>();
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         RelatedItems obj = new RelatedItems();
         obj.contentId = rel.getDependent().getId();
         obj.relId = rel.getId();
         obj.sortRank = Integer.parseInt(rel
               .getProperty(IPSHtmlParameters.SYS_SORTRANK));
         objects.add(obj);
         idList.add(new Integer(rel.getDependent().getId()));
      }
      Collections.sort(objects);
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      List<PSComponentSummary> summarylist = cms
            .loadComponentSummaries(idList);
      Map<Integer,String> idTitleMap = new HashMap<Integer, String>();
      for(PSComponentSummary sum : summarylist)
      {
         idTitleMap.put(sum.getContentId(), sum.getName());
      }
      //Comparator to sort the objects based on the sort rank.
      Comparator<RelatedItems> relObjectComparator = new Comparator<RelatedItems>()
      {
         public int compare(RelatedItems o1, RelatedItems o2)
         {
            return o1.sortRank - o2.sortRank;
         }
      };
      Collections.sort(objects, relObjectComparator);
      StringBuffer sb = new StringBuffer();
      for(RelatedItems obj:objects)
      {
         String cssClass = "PSAASnippetPickerTitle";
         String begin = "<div class=\"" + cssClass + "\" rid=\""
               + obj.relId + "\">";
         String end = "</div>";
         sb.append(begin);
         sb.append(idTitleMap.get(obj.contentId));
         sb.append(end);
         
      }
      return sb.toString();
   }
   
   /**
    * A local data object class to hold the relationship data and it implements
    * Comparable interface so that the contents of the list can be sorted 
    * based on the sort rank.
    */
   private class RelatedItems implements Comparable<RelatedItems>
   {
      int relId;
      int contentId;
      int sortRank;
      public int compareTo(RelatedItems o)
      {
         return this.sortRank - o.sortRank;
      }
   }

}
