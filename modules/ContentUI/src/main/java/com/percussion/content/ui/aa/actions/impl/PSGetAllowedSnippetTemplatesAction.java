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
package com.percussion.content.ui.aa.actions.impl;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.StringOutputSegment;
import au.id.jericho.lib.html.Tag;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.PSAAUtils;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.types.PSPair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This takes the ObjectId (JSON string) for a snippet
 * object and returns a json object with html containing each template
 * variation rendered for the passed in snippet and a template count.
 * Each redered template is surrounded by a div tag that has a onclick method to 
 * allow the template to be selected.
 * <pre>
 * The JSON object will contain the following parameters:
 * 
 * templateHtml - The assembled template variations
 * count - The template count
 * </pre>
 *  
 * The assembly service is used to render the snippet so
 * all of the required fields for assembly must be present.
 *
 */
public class PSGetAllowedSnippetTemplatesAction extends PSAAActionBase
{

   // See interface for details
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      JSONObject object = new JSONObject();
      String[] result = null;
      try
      {
         result = getSnippetTemplates(objectId);
         object.append("templateHtml", result[0]);
         object.append("count", result[1]);
      }
      catch (Exception e)
      {
         throw new PSAAClientActionException(e);
      }
      
      
      return new PSActionResponse(object.toString(),
               PSActionResponse.RESPONSE_TYPE_JSON);
   }
   
   /**
    * Helper method to retrieve the templates for this snippet and
    * then assemble each template variation.
    * @param objectId assumed not <code>null</code>.
    * @return the decorated assembled snippet variations and template count.
    * <pre>
    * [0] = assembled snippets
    * [1] = template count
    * </pre>
    * @throws Exception if an assembly error occurs
    */
   private String[] getSnippetTemplates(PSAAObjectId objectId) throws Exception
   {
      String contentid = objectId.getContentId();
      String rid = objectId.getRelationshipId();
      String slotid = objectId.getSlotId();
          

      if (contentid == null && rid == null)
      {
         throw new IllegalArgumentException(
            "sys_contentid or sys_relationshid must be specified" +
            " to load item templates");
      }
      if (contentid == null)
      {
         contentid = getContentIdFromRelId(rid);
      }
      
      IPSTemplateSlot slotObj = PSActionUtil.loadSlot(slotid);
      Collection<IPSAssemblyTemplate> templates = 
         PSGetItemTemplatesForSlotAction.getAssociatedTemplates(objectId);
      List<IPSAssemblyItem> aItems = new ArrayList<IPSAssemblyItem>();
      List<IPSAssemblyResult> aResults = new ArrayList<IPSAssemblyResult>();
      Map<String, String[]> assemblyParams = 
         PSActionUtil.getAssemblyParams(objectId, getCurrentUser());
      
      PSActionUtil.addAssemblyParam(assemblyParams,
               IPSHtmlParameters.SYS_RELATIONSHIPID, rid);
           
      for (IPSAssemblyTemplate template : templates)
      {
         PSActionUtil.addAssemblyParam(assemblyParams,
                  IPSHtmlParameters.SYS_VARIANTID,
                  String.valueOf(template.getGUID().getUUID()));
         PSPair<IPSAssemblyItem, IPSAssemblyResult> result = 
            PSActionUtil.assemble(assemblyParams);

         aItems.add(result.getFirst());
         aResults.add(result.getSecond());
      }
      String[] resultArr = new String[]{
         decorateSnippets(slotObj, aItems, aResults),
         String.valueOf(templates.size())};
      return resultArr;
   }
   
   /**
    * @param rid assumed to be not <code>null</code>.
    * @return the content id for the passed in relationship.
    * @throws PSCmsException
    */
   private String getContentIdFromRelId(String rid) throws PSCmsException
   {
      PSRelationshipProcessor relProc = new PSRelationshipProcessor();
         //(PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST));

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setRelationshipId(Integer.parseInt(rid));
      PSRelationshipSet rels = relProc.getRelationships(filter);
      if (rels.size() == 0)
         throw new RuntimeException("no relationship found with rid=" + rid);
      PSRelationship rel = (PSRelationship) rels.iterator().next();
      return String.valueOf(rel.getDependent().getId());
   }
      
   
   /**
    * Helper method to decorate each of the rendered snippets. Surrounding
    * each with a div tag with a specific class and onclick method
    * so that the template may be selected.
    * @param slotObj assumed not <code>null</code>.
    * @param ais assumed not <code>null</code>.
    * @param results assumed not <code>null</code>.
    * @return the decorated string.never<code>null</code>.
    * @throws Exception
    */
   private String decorateSnippets(IPSTemplateSlot slotObj,
            List<IPSAssemblyItem> ais, List<IPSAssemblyResult> results)
   throws Exception
   {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < results.size(); i++)
      {
         IPSAssemblyResult result = results.get(i);
         IPSAssemblyItem item = ais.get(i);
         String id = PSAAUtils.getSnippetObjectId(
                  (IPSAssemblyResult) item, slotObj.getName());
         sb.append("<div class=\"aaSnippet\" id=");
         sb.append(id);
         sb.append(" onClick=" +
                "\"return ps.aa.controller._onSnippetTemplateSelected(this);\">");
         sb.append(replaceHrefs(PSActionUtil.getBodyContent(result)));
         sb.append("<br/>");
         sb.append("</div>");               
      }
      sb.append("<input type=\"button\" value=\"Cancel\" " +
            "onClick=\"return ps.aa.controller._cancelSnippetTemplateSelectionDialog(this);\"/>");

      return sb.toString();
   }
   
   /**
    * Replaces the href in all A tags with a javascript:void(0)
    * statement so that they do nothing.
    * @param html assumed not <code>null</code>.
    * @return modified string, never <code>null</code>.
    */
   private String replaceHrefs(String html)
   {
      Source source = new Source(html);
      OutputDocument outputDocument = new OutputDocument(source);
      StringBuilder sb = new StringBuilder();
      List aStartTags = source.findAllStartTags(Tag.A);
      for (Iterator i = aStartTags.iterator(); i.hasNext();)
      {
         StartTag startTag = (StartTag)i.next();
         Attributes attributes = startTag.getAttributes();        
         Attribute hrefAttrib = attributes.get("href");
         if (hrefAttrib == null) 
            continue;
         
         sb.setLength(0);
         sb.append("<a");
         Iterator it = attributes.iterator();
         while(it.hasNext())
         {
            Attribute att = (Attribute)it.next();
            if(att.getName().equalsIgnoreCase("href"))
            {
               sb.append(" href=\"javascript:void(0)\"");
            }
            else
            {
               sb.append(" ");
               sb.append(att.getName());
               sb.append("=\"");
               sb.append(att.getValue());
               sb.append("\"");
            }
         }
         sb.append(">");
         outputDocument.add(new StringOutputSegment(startTag, sb.toString()));
      }
      return outputDocument.toString();
   }



}
