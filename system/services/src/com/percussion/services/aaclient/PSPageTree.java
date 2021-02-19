/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.services.aaclient;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.server.PSRequest;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class PSPageTree implements IPSWidgetHandler
{

   public void handleRequest(HttpServletRequest request,
      HttpServletResponse response) throws PSAssemblyException, RepositoryException, PSNotFoundException, PSFilterException, IOException {
      String resp = "";
      String action = request.getParameter("action");
      if (action.equalsIgnoreCase("getchildren"))
      {
         String d = request.getParameter("data");
         org.json.simple.JSONObject data = (org.json.simple.JSONObject) JSONValue
            .parse(d);
         org.json.simple.JSONObject node = (org.json.simple.JSONObject) data
            .get("node");
         String objectid = node.get("objectId").toString();
         org.json.simple.JSONObject idObj = (org.json.simple.JSONObject) JSONValue
            .parse(objectid);
         String nodeType = idObj.get("nodeType").toString();
         PSWidgetNodeType type = PSWidgetNodeType.valueOf(Integer
            .parseInt(nodeType));
         switch (type)
         {
            case WIDGET_NODE_TYPE_PAGE:
            case WIDGET_NODE_TYPE_SNIPPET:
               resp = getSlots(idObj).toString();
               break;
            case WIDGET_NODE_TYPE_SLOT:
               resp = getSnippets(idObj).toString();
               break;
            default:
         }
         PSAaClientServlet.pushResponse(response, resp, "text/javascript", 200);
      }
   }

   /**
    * todo
    * 
    * @param idObj the object id that is a JSON object with all parameters
    * required obtain slot items, assumed not <code>null</code> or empty.
    * @return Json array of all snippets nodes, never <code>null</code>, may
    * be empty.
    * @throws RepositoryException
    * @throws PSFilterException
    * @throws PSAssemblyException
    * @throws NumberFormatException
    */
   @SuppressWarnings("unchecked")
   private JSONArray getSnippets(JSONObject idObj) throws PSAssemblyException,
           PSFilterException, RepositoryException, PSNotFoundException {
      JSONArray result = new JSONArray();
      IPSTemplateSlot slotObj = PSAssemblyServiceLocator.getAssemblyService()
         .loadSlot(
            new PSGuid(PSTypeEnum.SLOT, Long.parseLong(idObj.get(
               IPSHtmlParameters.SYS_SLOTID).toString())));
      List<IPSAssemblyItem> aItems = getSlotItems(idObj, slotObj);

      for (int i = 0; i < aItems.size(); i++)
      {
         IPSAssemblyItem item = aItems.get(i);
         JSONObject oid = new JSONObject();
         oid.putAll(idObj);
         oid.put(IPSHtmlParameters.SYS_CONTENTID, item.getId().getUUID());
         oid.put(IPSHtmlParameters.SYS_REVISION, item.getParameterValue(
            IPSHtmlParameters.SYS_REVISION, ""));
         String rid = item.getParameterValue(
            IPSHtmlParameters.SYS_RELATIONSHIPID, "");

         oid.put(IPSHtmlParameters.SYS_RELATIONSHIPID, rid);
         oid.put(IPSHtmlParameters.SYS_VARIANTID, item.getTemplate().getGUID()
            .getUUID());
         oid.put(PSWidgetUtils.ATTR_NODETYPE,
            PSWidgetNodeType.WIDGET_NODE_TYPE_SNIPPET.getOrdinal());
         JSONObject r = new JSONObject();
         r.put("widgetId", "snippet:" + rid);
         r.put("objectId", oid);
         r.put("title", item.getNode().getName());
         r.put("isFolder", true);
         JSONArray noActions = new JSONArray();
         noActions.add("addChild");
         r.put("actionsDisabled", noActions);
         result.add(r);
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   private JSONArray getSlots(JSONObject idObj) throws PSAssemblyException
   {
      JSONArray result = new JSONArray();

      String variantid = idObj.get(IPSHtmlParameters.SYS_VARIANTID).toString();
      if (StringUtils.isEmpty(variantid))
      {
         throw new IllegalArgumentException(
            "variantid must not be null or empty");
      }
      IPSGuid templateId = new PSGuid(PSTypeEnum.TEMPLATE, Long
         .parseLong(variantid));

      IPSAssemblyTemplate template = m_assemblySvc.loadTemplate(templateId,
         true);

      Set<IPSTemplateSlot> slots = template.getSlots();
      for (IPSTemplateSlot slot : slots)
      {
         //Skip nav slots for now
         if(slot.getName().startsWith("perc.nav"))
         {
            continue;
         }
         JSONObject slotParams = new JSONObject();
         slotParams.putAll(idObj);
         slotParams.put(IPSHtmlParameters.SYS_SLOTID, slot.getGUID().getUUID());
         slotParams.put("nodeType", PSWidgetNodeType.WIDGET_NODE_TYPE_SLOT
            .getOrdinal());

         JSONObject r = new JSONObject();
         r.put("widgetId", "slot:" + slot.getGUID().getUUID());
         r.put("objectId", slotParams);
         r.put("title", slot.getLabel());
         r.put("isFolder", true);
         r.put("expandLevel", 1);
         r.put("iconNode", "slot");
         JSONArray noActions = new JSONArray();
         noActions.add("move");
         noActions.add("remove");
         r.put("actionsDisabled", noActions);
         result.add(r);
      }
      return result;
   }

   @SuppressWarnings(
   {
      "unchecked", "unused"
   })
   public static String getRootTemplate(HttpServletRequest request)
      throws PSStringTemplateException, PSAssemblyException,
      PSMissingBeanConfigurationException
   {
      String resp = "";
      Map params = PSWidgetUtils.parseObjectId(((PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST)).getParameters(),
         PSWidgetNodeType.WIDGET_NODE_TYPE_PAGE);
      JSONObject jsObj = new JSONObject();
      jsObj.putAll(params);

      int cid;

      try
      {
         cid = Integer.parseInt(params.get(IPSHtmlParameters.SYS_CONTENTID)
            .toString());
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Invalid contentid specified");
      }

      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = objMgr.loadComponentSummary(cid);
      Map<String, String> vars = new HashMap<String, String>();
      vars.put("TITLE", summary.getName());
      vars.put("OBJECTID", jsObj.toString());
      resp = TREE_ROOT_NODE_TEMPLATE.expand(vars);

      return resp;
   }

   @SuppressWarnings(
   {
      "unchecked", "unused"
   })
   private Map getAaParamMap(HttpServletRequest request)
   {
      Map aaParams = new HashMap();
      String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      if (StringUtils.isEmpty(cid))
      {
         throw new IllegalArgumentException(
            "contentid must not be null or empty");
      }
      String vid = request.getParameter(IPSHtmlParameters.SYS_VARIANTID);
      if (StringUtils.isEmpty(vid))
      {
         throw new IllegalArgumentException(
            "variantid must not be null or empty");
      }
      String sid = request.getParameter(IPSHtmlParameters.SYS_SITEID);
      if (StringUtils.isEmpty(sid))
      {
         throw new IllegalArgumentException("siteid must not be null or empty");
      }
      aaParams.put(IPSHtmlParameters.SYS_CONTENTID, cid);
      aaParams.put(IPSHtmlParameters.SYS_VARIANTID, vid);
      aaParams.put(IPSHtmlParameters.SYS_SITEID, sid);

      String rev = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      if (!StringUtils.isEmpty(rev))
         aaParams.put(IPSHtmlParameters.SYS_REVISION, rev);

      String context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      if (StringUtils.isEmpty(context))
         context = "0";
      String authtype = request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE);
      if (StringUtils.isEmpty(authtype))
         authtype = "0";
      aaParams.put(IPSHtmlParameters.SYS_CONTEXT, context);
      aaParams.put(IPSHtmlParameters.SYS_AUTHTYPE, authtype);

      String fid = request.getParameter(IPSHtmlParameters.SYS_FOLDERID);
      if (!StringUtils.isEmpty(fid))
         aaParams.put(IPSHtmlParameters.SYS_FOLDERID, fid);

      return aaParams;
   }

   public List<IPSAssemblyItem> getSlotItems(JSONObject objectId,
      IPSTemplateSlot slotObj) throws NumberFormatException,
           PSAssemblyException, PSFilterException, RepositoryException, PSNotFoundException {
      IPSAssemblyItem aItem = loadAssemblyItem(objectId);
      PSAssemblerUtils autils = new PSAssemblerUtils();
      Map<String, Object> p = new HashMap<String, Object>();
      return autils.getSlotItems(aItem, slotObj, p);
   }

   /**
    * @param objectId
    * @return
    * @throws PSAssemblyException
    */
   private IPSAssemblyItem loadAssemblyItem(JSONObject objectId)
      throws PSAssemblyException
   {
      Map<String, String[]> params = new HashMap<String, String[]>();
      Object temp = objectId.get(IPSHtmlParameters.SYS_CONTENTID);

      if (temp != null)
         params.put(IPSHtmlParameters.SYS_CONTENTID, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_REVISION);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_REVISION, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_VARIANTID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_VARIANTID, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_SLOTID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SLOTID, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_SITEID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SITEID, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_FOLDERID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_FOLDERID, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_AUTHTYPE);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_AUTHTYPE, new String[]
         {
            temp.toString()
         });

      temp = objectId.get(IPSHtmlParameters.SYS_CONTEXT);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_CONTEXT, new String[]
         {
            temp.toString()
         });

      params.put(IPSHtmlParameters.SYS_COMMAND, new String[]
      {
         "editrc"
      });

      IPSAssemblyService aService = PSAssemblyServiceLocator
         .getAssemblyService();
      IPSAssemblyItem aItem = aService.createAssemblyItem();
      aItem.setParameters(params);
      aItem.normalize();
      return aItem;
   }

   private static PSStringTemplate TREE_ROOT_NODE_TEMPLATE = new PSStringTemplate(
      "<div id=\"pageTreeRoot\" actionsDisabled=\"addChild;remove;move\" dojoType=\"TreeNodeV3\" "
         + "expandLevel=\"1\" title=\"{TITLE}\" isFolder=\"true\" "
         + "objectId=\'{OBJECTID}\'></div>");

   /**
    * Assembly Service
    */
   private static IPSAssemblyService m_assemblySvc = PSAssemblyServiceLocator
      .getAssemblyService();
}
