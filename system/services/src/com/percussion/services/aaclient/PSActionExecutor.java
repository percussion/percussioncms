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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.ModifyRelatedContentUtils;
import com.percussion.services.assembly.jexl.PSAssemblerUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class PSActionExecutor implements IPSWidgetHandler
{
   private String m_actionName = null;

   JSONObject m_jsObjId = null;

   public void handleRequest(HttpServletRequest request,
      HttpServletResponse response) throws Exception
   {
      String objectId = request.getParameter(PSWidgetUtils.ATTR_OBJECTID);
      String action = request.getParameter(PSWidgetUtils.ATTR_ACTION);
      if (!StringUtils.isBlank(objectId))
      {
         m_jsObjId = (JSONObject) JSONValue.parse(objectId);
      }
      if (action == null)
         action = "";
      m_actionName = action.toLowerCase();
      if (action.startsWith("aa_"))
      {
         try
         {
            executeAssemblyAction(request, response);
         }
         catch (Throwable e)
         {
            throw new Exception(e);
         }
      }
      else if (action.startsWith("ce_"))
      {
         executeCeAction(request, response);
      }
      else
      {
         throw new Exception("Unhandled action '" + action + "'");
      }

   }

   /**
    * @param request
    * @param response
    */
   private void executeCeAction(HttpServletRequest request,
      HttpServletResponse response) throws Exception
   {
      if (m_actionName.equals("ce_setfield"))
      {
         String cidstr = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         String fieldName = request.getParameter("fieldname");
         String fieldValue = request.getParameter("fieldvalue");
         fieldValue = URLDecoder.decode(fieldValue, "UTF-8");
         int cid = Integer.parseInt(cidstr);
         PSLegacyGuid id = new PSLegacyGuid(cid, -1);
         String resp = "";
         resp = setFieldValue(id, fieldName, fieldValue);
         PSAaClientServlet.pushResponse(response, resp, "text/html", 200);
      }
      else if (m_actionName.equals("ce_checkout"))
      {
         String cidstr = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         int cid = Integer.parseInt(cidstr);
         PSLegacyGuid id = new PSLegacyGuid(cid, -1);
         checkOutOrInItem(id, false);
         PSComponentSummary summary = PSWidgetUtils.getItemSummary(Integer
            .parseInt(cidstr));

         PSAaClientServlet.pushResponse(response, "success:"
            + summary.getTipLocator().getRevision(), "text/html", 200);
      }
      else if (m_actionName.equals("ce_checkin"))
      {
         String cidstr = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         int cid = Integer.parseInt(cidstr);
         PSLegacyGuid id = new PSLegacyGuid(cid, -1);
         checkOutOrInItem(id, true);
         PSComponentSummary summary = PSWidgetUtils.getItemSummary(Integer
            .parseInt(cidstr));

         PSAaClientServlet.pushResponse(response, "success:"
            + summary.getTipLocator().getRevision(), "text/html", 200);
      }
      else if (m_actionName.equals("ce_checkoutstatus"))
      {
         String cidstr = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         PSComponentSummary summary = PSWidgetUtils.getItemSummary(Integer
            .parseInt(cidstr));
         if (StringUtils.isBlank(summary.getCheckoutUserName()))
         {
            PSAaClientServlet.pushResponse(response, "checkedin", "text/html",
               200);
         }
         else
         {
            PSAaClientServlet.pushResponse(response, "checkedout", "text/html",
               200);
         }
      }
   }

   /**
    * 
    * @param request
    * @param response
    * @throws Exception
    */
   @SuppressWarnings("unused")
   private void executeAssemblyAction(HttpServletRequest httpRequest,
      HttpServletResponse response) throws Throwable
   {
      PSRequest req = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      IPSRequestContext request = new PSRequestContext(req);
      String tmp = (String) m_jsObjId.get(IPSHtmlParameters.SYS_RELATIONSHIPID);
      int rid = -1;
      try
      {
         rid = Integer.parseInt(tmp);
      }
      catch (NumberFormatException e)
      {
         // ignore all action may not need it
      }

      if (m_actionName.equals("aa_moveup"))
      {
         ModifyRelatedContentUtils.moveUp(rid, request);
         PSAaClientServlet.pushResponse(response, "success", "text/plain", 200);
      }
      else if (m_actionName.equals("aa_movedown"))
      {
          ModifyRelatedContentUtils.moveDown(rid, request);
         PSAaClientServlet.pushResponse(response, "success", "text/plain", 200);
      }
      else if (m_actionName.equals("aa_remove"))
      {
          ModifyRelatedContentUtils.deleteSlotItem(rid, request);
         PSAaClientServlet.pushResponse(response, "success", "text/plain", 200);
      }
      else if (m_actionName.equals("aa_add"))
      {
          ModifyRelatedContentUtils.insertSlotItems(request);
      }
      else if (m_actionName.equals("aa_movetoslot"))
      {
         String newSlotId = (String) m_jsObjId.get("newSlot");
         String newVarId = (String) m_jsObjId.get("newTemplate");
         if (newVarId == null)
            newVarId = "-1";
         String index = m_jsObjId.get("index").toString();
          ModifyRelatedContentUtils.moveToSlot(rid, Integer.parseInt(newSlotId),
            Integer.parseInt(index), Integer.parseInt(newVarId), request);
         PSAaClientServlet.pushResponse(response, "success", "text/plain", 200);
      }
      else if (m_actionName.equals("aa_reorder"))
      {
         String index = m_jsObjId.get("index").toString();
          ModifyRelatedContentUtils.reorder(rid, Integer.parseInt(index), request);
         PSAaClientServlet.pushResponse(response, "success", "text/plain", 200);
      }
      else if (m_actionName.equals("aa_changetemplate"))
      {
         PSAaClientServlet.pushResponse(response, "failure", "text/plain", 500);
      }
      else if (m_actionName.equals("aa_getsnippettemplates"))
      {
         PSAaClientServlet.pushResponse(response, getSnippetTemplates(),
            "text/html", 200);
      }
      else if (m_actionName.equals("aa_getslotitems"))
      {
         Map<String, String[]> params = getAssemblyParams(null);
         IPSAssemblyService aService = PSAssemblyServiceLocator
            .getAssemblyService();
         IPSTemplateSlot slotObj = aService.loadSlot(new PSGuid(
            PSTypeEnum.SLOT, Long.parseLong(params
               .get(IPSHtmlParameters.SYS_SLOTID)[0])));
         IPSAssemblyItem aItem = aService.createAssemblyItem(null, 1000, 0,
            null, null, params, null, false);
         PSAssemblerUtils autils = new PSAssemblerUtils();
         Map<String, Object> p = new HashMap<>();
         p.put(IPSHtmlParameters.SYS_COMMAND, "editrc");
         List<IPSAssemblyItem> ais = autils.getSlotItems(aItem, slotObj, p);
         List<IPSAssemblyResult> results = autils.assemble(aItem, slotObj, p);
         PSAaClientServlet.pushResponse(response, assembleSlotItems(slotObj,
            ais, results, true, false), "text/plain", 200);
      }
   }

   private String assembleSlotItems(IPSTemplateSlot slotObj,
      List<IPSAssemblyItem> ais, List<IPSAssemblyResult> results,
      boolean editrc, boolean forDialog) throws Exception
   {
      PSAssemblerUtils autils = new PSAssemblerUtils();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < results.size(); i++)
      {
         IPSAssemblyResult result = results.get(i);
         IPSAssemblyItem item = ais.get(i);
         String id = PSWidgetUtils.parseSnippetObjectId(
            item, slotObj.getName());
         if (forDialog)
         {
            sb.append("<div class=\"aaSnippet\" id=" + id
               + " onClick=\"return insertSnippet(this);\">");
         }
         String r = autils.getSingleParamValue(item.getParameters(),
            IPSHtmlParameters.SYS_RELATIONSHIPID);
         if (editrc)
         {
            sb
               .append("<a href='javascript:void(0)' onclick='PSActivateSnippet("
                  + r
                  + "); return false'>"
                  + "<img align='absmiddle' border='0' src='../sys_resources/images/item.gif' "
                  + "title='Activate snippet'/></a>");
         }
         sb.append(getSnippetBody(result));
         if (forDialog)
         {
            sb.append("</div>");
         }
      }
      if (forDialog)
         sb.append("<input id=\"hider\" type=\"button\" value=\"Close\"/>");

      return sb.toString();
   }

   /**
    * @return
    */
   private Map<String, String[]> getAssemblyParams(PSComponentSummary summary)
   {
      Map<String, String[]> params = new HashMap<>();
      Object temp = m_jsObjId.get(IPSHtmlParameters.SYS_CONTENTID);
      if (temp == null && summary != null)
         temp = summary.getContentId();
      params.put(IPSHtmlParameters.SYS_CONTENTID, new String[]
      {
         temp.toString()
      });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_REVISION);
      if (temp == null && summary != null)
         temp = summary.getCurrentLocator().getRevision();
      params.put(IPSHtmlParameters.SYS_REVISION, new String[]
      {
         temp.toString()
      });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_VARIANTID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_VARIANTID, new String[]
         {
            temp.toString()
         });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_SLOTID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SLOTID, new String[]
         {
            temp.toString()
         });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_SITEID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_SITEID, new String[]
         {
            temp.toString()
         });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_FOLDERID);
      if (temp != null)
         params.put(IPSHtmlParameters.SYS_FOLDERID, new String[]
         {
            temp.toString()
         });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_AUTHTYPE);
      if (temp == null)
         temp = "0";
      params.put(IPSHtmlParameters.SYS_AUTHTYPE, new String[]
      {
         temp.toString()
      });

      temp = m_jsObjId.get(IPSHtmlParameters.SYS_CONTEXT);
      if (temp == null)
         temp = "0";
      params.put(IPSHtmlParameters.SYS_CONTEXT, new String[]
      {
         temp.toString()
      });
      return params;
   }

   /**
    * Returns content included between body tags. 
    * @param result the result to retrieve body content from.
    * Not <code>null</code>.
    * @return the result content. Not <code>null</code>
    * @throws UnsupportedEncodingException if the result string charset is not
    * supported. 
    */
   String getSnippetBody(IPSAssemblyResult result)
         throws UnsupportedEncodingException
   {
      if (result == null)
      {
         throw new IllegalArgumentException("Result param should not be null");
      }
      byte[] res = result.getResultData();
      String resStr = new String(res, "UTF-8");
      final String lstring = resStr.toLowerCase();
      int index = lstring.indexOf("</body>");
      if (index != -1)
         resStr = resStr.substring(0, index);
      index = lstring.indexOf("<body>");
      if (index != -1)
      {
         resStr = resStr.substring(index + "<body>".length());
      }
      else
      {
         index = lstring.indexOf("<body ");
         if (index != -1)
         {
            index = resStr.indexOf('>', index);
            resStr = resStr.substring(index + 1);
         }
      }
      resStr += "<br/>";
      return resStr;
   }

   /**
    * @todo Desc
    * @param contentId
    * @param fieldName
    * @param value
    * @return
    * @throws PSErrorResultsException
    * @throws PSException
    */
   private String setFieldValue(IPSGuid contentId, String fieldName,
      String value) throws PSErrorResultsException, PSException
   {
      PSRequest req = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      String user = req.getUserSession().getRealAuthenticatedUserEntry();

      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();
      List<IPSGuid> ids = Collections.singletonList(contentId);
      List<PSCoreItem> items = ctService.loadItems(ids, false, false, false,
         false);

      PSCoreItem item = items.get(0);
      String couser = item.getCheckedOutByName();
      if (!StringUtils.isEmpty(couser) && !user.equalsIgnoreCase(couser))
      {
         throw new PSException("Item is checked out to a different user");
      }
      if (StringUtils.isEmpty(couser))
      {
         try
         {
            ctService.checkoutItems(ids, "from aa");
         }
         catch (PSErrorsException e)
         {
            PSErrorException ex = (PSErrorException) e.getErrors().get(
               ids.get(0));
            throw new PSException(ex.getCode(), ex.getMessage());
         }
      }
      items = ctService.loadItems(ids, false, false, false, false);

      item = items.get(0);

      PSItemField field = item.getFieldByName(fieldName);
      if (field == null)
      {
         throw new RuntimeException("Cannot find field name = \"" + fieldName
            + "\" from item with contentId = " + contentId.toString());
      }
      field.clearValues();
      IPSFieldValue fvalue = field.createFieldValue(value);
      field.addValue(fvalue);

      ctService.saveItems(items, false, false);

      // retrieve the updated field value again if it may have been changed
      // after the update.
      PSField fieldDef = field.getItemFieldMeta().getFieldDef();
      if (fieldDef.mayHaveInlineLinks())
      {
         value = getFieldValue(contentId, fieldName);
      }

      return value;
   }

   /**
    * @todo Desc
    * @param contentId
    * @return
    * @throws PSErrorResultsException
    * @throws PSException
    */
   private void checkOutOrInItem(IPSGuid contentId, boolean isCheckin)
      throws PSErrorResultsException, PSException
   {
      PSRequest req = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String user = req.getUserSession().getRealAuthenticatedUserEntry();

      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();
      List<IPSGuid> ids = Collections.singletonList(contentId);
      List<PSCoreItem> items = ctService.loadItems(ids, false, false, false,
         false);

      PSCoreItem item = items.get(0);
      String couser = item.getCheckedOutByName();
      if (!StringUtils.isEmpty(couser) && !user.equalsIgnoreCase(couser))
      {
         throw new PSException("Item is checked out to a different user");
      }
      if (isCheckin)
      {
         // not checked out to anybody return quitely
         if (StringUtils.isEmpty(couser))
            return;
         try
         {
            ctService.checkinItems(ids, "from aa");
         }
         catch (PSErrorsException e)
         {
            PSErrorException ex = (PSErrorException) e.getErrors().get(
               ids.get(0));
            throw new PSException(ex.getCode(), ex.getMessage());
         }
      }
      else
      {
         // already checkoedout to you return quitely
         if (user.equalsIgnoreCase(couser))
         {
            return;
         }
         try
         {
            ctService.checkoutItems(ids, "from aa");
         }
         catch (PSErrorsException e)
         {
            PSErrorException ex = (PSErrorException) e.getErrors().get(
               ids.get(0));
            throw new PSException(ex.getCode(), ex.getMessage());
         }
      }
   }

   /**
    * @todo Desc
    * @param contentId
    * @param fieldName

    * @return
    * @throws PSErrorResultsException
    * @throws PSException
    */
   private String getFieldValue(IPSGuid contentId, String fieldName) throws PSException,
      PSErrorResultsException
   {
      IPSContentWs ctService = PSContentWsLocator.getContentWebservice();
      List<IPSGuid> ids = Collections.singletonList(contentId);
      List<PSCoreItem> items = ctService.loadItems(ids, false, false, false,
         false);

      PSItemField field = items.get(0).getFieldByName(fieldName);
      if (field == null)
      {
         throw new RuntimeException("Cannot find field name = \"" + fieldName
            + "\" from item with contentId = " + contentId.toString());
      }

      IPSFieldValue value = field.getValue();
      return (value != null) ? value.getValueAsString() : null;
   }

   public String getSnippetTemplates() throws Exception
   {
      String cid = null, rid = null, sid = null;
      Object obj = m_jsObjId.get(IPSHtmlParameters.SYS_CONTENTID);
      if (obj != null)
         cid = obj.toString();
      obj = m_jsObjId.get(IPSHtmlParameters.SYS_RELATIONSHIPID);
      if (obj != null)
         rid = obj.toString();

      obj = m_jsObjId.get(IPSHtmlParameters.SYS_SLOTID);
      if (obj != null)
         sid = obj.toString();

      if (cid == null && rid == null)
      {
         throw new IllegalArgumentException(
            "sys_contentid or sys_relationshiid must be specified to load item templates");
      }
      if (cid == null)
      {
         cid = getContentIdFromRelId(rid);
      }
      PSComponentSummary summary = PSWidgetUtils.getItemSummary(Integer
         .parseInt(cid));

      IPSAssemblyService aService = PSAssemblyServiceLocator
         .getAssemblyService();
      IPSTemplateSlot slotObj = aService.loadSlot(new PSGuid(PSTypeEnum.SLOT,
         Long.parseLong(sid)));

      Collection<PSPair<IPSGuid, IPSGuid>> assoc = slotObj
         .getSlotAssociations();
      List<IPSGuid> templateids = new ArrayList<>();
      for (PSPair<IPSGuid, IPSGuid> pair : assoc)
      {
         if (pair.getFirst().equals(summary.getContentTypeGUID()))
            templateids.add(pair.getSecond());
      }
      List<IPSAssemblyItem> aItems = new ArrayList<>();
      List<IPSAssemblyResult> aResults = new ArrayList<>();
      Map<String, String[]> assemblyParams = getAssemblyParams(summary);
      assemblyParams.put(IPSHtmlParameters.SYS_RELATIONSHIPID, new String[]
      {
         rid
      });
      for (IPSGuid guid : templateids)
      {
         List<IPSAssemblyItem> list = new ArrayList<>();
         assemblyParams.put(IPSHtmlParameters.SYS_VARIANTID, new String[]
         {
            String.valueOf(guid.getUUID())
         });
         IPSAssemblyItem aItem = aService.createAssemblyItem(null, 1000, 0,
            null, null, assemblyParams, null, false);
         list.add(aItem);
         List<IPSAssemblyResult> result = aService.assemble(list);

         aItems.addAll(list);
         aResults.addAll(result);
      }
      return assembleSlotItems(slotObj, aItems, aResults, false, true);
   }

   /**
    * @param rid
    * @return
    * @throws PSCmsException
    */
   private String getContentIdFromRelId(String rid) throws PSCmsException
   {
      PSRelationshipProcessor relProc = PSRelationshipProcessor.getInstance();

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setRelationshipId(Integer.parseInt(rid));
      PSRelationshipSet rels = relProc.getRelationships(filter);
      if (rels.size() == 0)
         throw new RuntimeException("no relationship found with rid=" + rid);
      PSRelationship rel = (PSRelationship) rels.iterator().next();
      return String.valueOf(rel.getDependent().getId());
   }

   /**
    * gets the Active Assembly processor proxy for this relationship walker.
    * 
    * @return the Active Assembly Processor Proxy, never <code>null</code>.
    * @throws PSCmsException if the proxy cannot be created.
    */
   public synchronized PSActiveAssemblyProcessorProxy getAaProxy()
      throws PSCmsException
   {
      if (m_aaProxy == null)
      {
         m_aaProxy = new PSActiveAssemblyProcessorProxy(
            PSProcessorProxy.PROCTYPE_SERVERLOCAL, PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST));
      }
      return m_aaProxy;
   }

   /**
    * Gets the Component Processor Proxy for this relationship walker.
    * 
    * @return Returns the m_compProxy, never <code>null</code>.
    * @throws PSCmsException if the proxy cannot be created.
    */
   public synchronized PSComponentProcessorProxy getCompProxy()
      throws PSCmsException
   {
      if (m_compProxy == null)
      {
         m_compProxy = new PSComponentProcessorProxy(
            PSProcessorProxy.PROCTYPE_SERVERLOCAL, PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST));
      }
      return m_compProxy;
   }

   /**
    * Reference to the AA processor proxy initialized during first call to
    * {@link #getAaProxy()}. Never <code>null</code> after that. Don't use
    * this field directly.
    */
   private PSActiveAssemblyProcessorProxy m_aaProxy = null;

   /**
    * Reference to the component processor proxy initialized during first call
    * to {@link #getCompProxy()}. Never <code>null</code> after that. Don't
    * use this field directly.
    */
   private PSComponentProcessorProxy m_compProxy = null;
}
