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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.services.aaclient;

import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class PSActionBar implements IPSWidgetHandler
{
   public void handleRequest(HttpServletRequest request,
      HttpServletResponse response) throws IOException
   {
      String objectId = request.getParameter(PSWidgetUtils.ATTR_OBJECTID);
      JSONObject jsObjId = (JSONObject) JSONValue.parse(objectId);
      String nt = (String) jsObjId.get(PSWidgetUtils.ATTR_NODETYPE);
      PSWidgetNodeType nType = PSWidgetNodeType.valueOf(Short.parseShort(nt));
      String resp = "";
      try
      {
         switch (nType)
         {
            case WIDGET_NODE_TYPE_PAGE:
               resp = buildPageActions(request, jsObjId);
               break;
            case WIDGET_NODE_TYPE_SLOT:
               try
               {
                  resp = buildSlotActions(request, jsObjId);
               }
               catch (Exception e)
               {
                  throw new IOException(e.getLocalizedMessage());
               }
               break;
            case WIDGET_NODE_TYPE_SNIPPET:
               resp = buildSnippetActions(request, jsObjId);
               break;
            case WIDGET_NODE_TYPE_FIELD:
               resp = buildFieldActions(request, jsObjId);
               break;
            default:
               throw new IOException("Unknown node type");
         }
      }
      catch (Exception e)
      {
         throw new IOException(e.getLocalizedMessage());
      }
      PSAaClientServlet.pushResponse(response, resp, "text/html", 200);
   }

   /**
    * @param request
    * @param jsObjId
    * @return
    * @throws PSStringTemplateException
    */
   @SuppressWarnings("unused")
   private String buildPageActions(HttpServletRequest request,
      JSONObject jsObjId) throws PSStringTemplateException
   {
      String cid = (String) jsObjId.get(IPSHtmlParameters.SYS_CONTENTID);
      String title = new PSAssemblerUtils().getTitle(new PSLegacyGuid(Long
         .parseLong(cid)));
      Map vars = new HashMap();
      vars
         .put("IMAGE_URL", PSWidgetNodeType.WIDGET_NODE_TYPE_PAGE.getIconUrl());
      vars.put("TITLE", title);
      vars.put("EDIT_LABEL", "Edit");
      String activeCaption = (String) jsObjId.get("activeCaption");
      if (!StringUtils.isEmpty(activeCaption))
         vars.put("ACTIVATE_CAPTION", activeCaption);
      vars.putAll(buildWorkflowActions(request, jsObjId));
      PSStringTemplate template = PSAAStubUtil.getPageActions();
      String result = template.expand(vars);
      return result;
   }

   /**
    * @param request
    * @param jsObjId
    * @return
    * @throws PSStringTemplateException
    */
   private String buildSnippetActions(HttpServletRequest request,
      JSONObject jsObjId) throws PSStringTemplateException
   {
      String cid = (String) jsObjId.get(IPSHtmlParameters.SYS_CONTENTID);
      String title = new PSAssemblerUtils().getTitle(new PSLegacyGuid(Long
         .parseLong(cid)));
      PSStringTemplate template = PSAAStubUtil.getSnippetActions();
      Map vars = new HashMap();
      vars.put("IMAGE_URL", PSWidgetNodeType.WIDGET_NODE_TYPE_SNIPPET
         .getIconUrl());
      vars.put("TITLE", title);
      vars.put("TEMPLATE_NAME", "another template");
      String activeCaption = (String) jsObjId.get("activeCaption");
      if (!StringUtils.isEmpty(activeCaption))
         vars.put("ACTIVATE_CAPTION", activeCaption);
      vars.putAll(buildWorkflowActions(request, jsObjId));
      String result = template.expand(vars);
      return result;
   }

   /**
    * @param request
    * @param jsObjId
    * @return
    * @throws PSStringTemplateException
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    * @throws NumberFormatException
    */
   private String buildSlotActions(HttpServletRequest request,
      JSONObject jsObjId) throws PSStringTemplateException,
           NumberFormatException, PSMissingBeanConfigurationException, PSAssemblyException {
      String slotid = (String) jsObjId.get(IPSHtmlParameters.SYS_SLOTID);
      IPSTemplateSlot slot = PSAssemblyServiceLocator.getAssemblyService()
         .loadSlot(new PSGuid(PSTypeEnum.SLOT, Long.parseLong(slotid)));
      PSStringTemplate template = PSAAStubUtil.getSlotActions();
      Map<String,String> vars = new HashMap<>();
      vars
         .put("IMAGE_URL", PSWidgetNodeType.WIDGET_NODE_TYPE_SLOT.getIconUrl());
      vars.put("TITLE", slot.getLabel());
      String activeCaption = (String) jsObjId.get("activeCaption");
      if (!StringUtils.isEmpty(activeCaption))
         vars.put("ACTIVATE_CAPTION", activeCaption);
      return template.expand(vars);
   }

   /**
    * 
    * @param request
    * @param jsObjId
    * @return
    */
   private Map<String,String> buildWorkflowActions(HttpServletRequest request,
      JSONObject jsObjId)
   {
      String cid = (String) jsObjId.get(IPSHtmlParameters.SYS_CONTENTID);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element rootElem = PSXmlDocumentBuilder
         .createRoot(doc, "workflowactions");
      rootElem.setAttribute(ATTRIB_CONTENTID, cid);

      Map<String,String> vars = new HashMap<>();

     boolean isPublic = PSWorkFlowUtils.isPublic(Integer.parseInt(cid));
     if (isPublic)
     {
        vars.put("EDIT_LABEL", "Quick Edit");
        vars.put("EDIT_FUNCTION", "PSQuickEditContent();");
     }
     else
     {
        vars.put("EDIT_LABEL", "Edit");
        vars.put("EDIT_FUNCTION", "PSEditContent();");
     }
     String div1 = "<div dojoType=\"MenuItem2\" caption=\"";
     String div2 = "onClick='PSBuildWFAction(";
     String div2c = "onClick='PSCheckinCheckout(";
     String div3 = "></div>";
     NodeList nl = doc.getElementsByTagName("ActionLink");
     String wfActionDivs = "";
     for (int i = 0; nl != null && i < nl.getLength(); i++)
     {
        Element elem = (Element) nl.item(i);
        String isTrans = elem.getAttribute("isTransition");
        String acName = elem.getAttribute("name");
        Element dElem = (Element) elem.getElementsByTagName("DisplayLabel")
           .item(0);
        String displayLabel = PSXmlTreeWalker.getElementData(dElem);
        if (acName.equalsIgnoreCase("checkout"))
        {
           wfActionDivs += div1 + displayLabel + "\" " + div2c + "\"" + cid
              + "\",true" + ")'" + div3;

        }
        else if (acName.equalsIgnoreCase("checkin"))
        {
           wfActionDivs += div1 + displayLabel + "\" " + div2c + "\"" + cid
              + "\",false" + ")'" + div3;
        }
        else if (isTrans.equalsIgnoreCase("yes"))
        {
           String commentReq = elem.getAttribute("commentRequired");
           String transId = "";
           String wfAction = "";
           NodeList nl1 = elem.getElementsByTagName("Param");
           for (int j = 0; nl1 != null && j < nl1.getLength(); j++)
           {
              Element pElem = (Element) nl1.item(j);
              String pName = pElem.getAttribute("name");
              String pValue = PSXmlTreeWalker.getElementData(pElem);
              if (pName.equalsIgnoreCase("sys_transitionid"))
                 transId = pValue;
              else if (pName.equalsIgnoreCase("sys_transitionid"))
                 wfAction = pValue;
           }
           wfActionDivs += div1 + displayLabel + "\" " + div2 + "\""
              + displayLabel + "\",\"" + commentReq + "\",\"" + transId
              + "\",\"" + wfAction + "\")'" + div3;
        }
     }
     vars.put("WORKFLOW_ACTIONS", wfActionDivs);

      return vars;
   }

   /**
    * @param request
    * @param jsObjId
    * @return
    */
   private String buildFieldActions(HttpServletRequest request,
      JSONObject jsObjId)
   {
      // fixme Auto-generated method stub
      return "not implemented";
   }

   static private final String ELEMENT_ITEM = "Item";

   static private final String ATTRIB_CONTENTID = "contentid";
}
