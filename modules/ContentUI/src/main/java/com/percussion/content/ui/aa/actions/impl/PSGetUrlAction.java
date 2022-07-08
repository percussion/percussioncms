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

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionParameters;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlParameter;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSParam;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.PSManagedNavServiceLocator;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowException;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import com.percussion.webservices.ui.IPSUiWs;
import com.percussion.webservices.ui.PSUiWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This takes the ObjectId (JSON string) and the action name string and
 * returns a Json object string that contains a URL for the action. 
 * The action names are hard coded like:
 *
 * CE_EDIT
 * CE_VIEW_CONTENT
 * CE_VIEW_PROPERTIES
 * CE_FIELDEDIT
 * CE_VIEW_REVISIONS
 * CE_VIEW_AUDIT_TRAIL
 * CE_LINK
 * PREVIEW_PAGE
 * PREVIEW_MYPAGE
 * RC_SEARCH
 * TOOL_SHOW_AA_RELATIONSHIPS
 * TOOL_LINK_TO_PAGE
 * TOOL_PUBLISH_NOW
 * ACTION_xxx
 * 
 * The returned json object contains the following parameters:
 * <pre>
 * url = the requested url
 * dlg_height = the dialog height (Only exists for the field edit url)
 * dlg_width = the dialog height (Only exists for the field edit url)
 * </pre>
 *
 */
public class PSGetUrlAction extends PSAAActionBase
{
   /* 
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String actionname = (String)getParameter(params, "actionname");      
      String result = null;
      if(StringUtils.isBlank(actionname))
         throw new PSAAClientActionException(
                  "Missing required actionname parameter.");
      final String GENERIC_ACTION_PREFIX = "ACTION_";
      if(!actionname.startsWith(GENERIC_ACTION_PREFIX)
            && !ms_allowedTypes.contains(actionname))
      {
         throw new PSAAClientActionException("Invalid url action name.");
      }
      try
      {
         if (actionname.startsWith("CE_"))
         {
            Method method = getClass().getDeclaredMethod("do" + actionname,
                  new Class[]
            {
               String.class, PSAAObjectId.class
            });
            IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, Long
               .parseLong(objectId.getContentTypeId()));
            PSNodeDefinition def = PSContentTypeHelper.findNodeDef(guid);
            result = (String) method.invoke(this, new Object[]
            {
               def.getQueryRequest(), objectId
            });
         }
         else if (actionname.startsWith(GENERIC_ACTION_PREFIX))
         {
            String realActionName = actionname.substring(GENERIC_ACTION_PREFIX
                  .length());
            result = doGenericAction(realActionName, objectId);
         }
         else
         {
            Method method = getClass().getDeclaredMethod("do" + actionname,
                  new Class[]
            {
               PSAAObjectId.class
            });
            result = (String) method.invoke(this, new Object[]
            {
               objectId
            });
         }
         
      }      
      catch (Exception e)
      {
         if (e instanceof PSAAClientActionException)
            throw (PSAAClientActionException) e;

         throw new PSAAClientActionException(e);
      }      
      return new PSActionResponse(result, PSActionResponse.RESPONSE_TYPE_JSON);
   }
   
   /**
    * Assembles the contenteditor edit url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doCE_EDIT(String queryurl,
            PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "edit");
      url.addParam(IPSHtmlParameters.SYS_VIEW, "sys_All");
      url.addParam("refreshHint", "Selected");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Assembles the contenteditor view content url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doCE_VIEW_CONTENT(String queryurl,
            PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "preview");
      url.addParam(IPSHtmlParameters.SYS_VIEW, "sys_All");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Assembles the contenteditor view properties url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused")
   private String doCE_VIEW_PROPERTIES(String queryurl,
            PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "preview");
      url.addParam(IPSHtmlParameters.SYS_VIEW, "sys_ItemMeta");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      return createJsonReturnString(url.toString(), null);
   }
   
   /** 
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doCE_FIELDEDIT(String queryurl,
            PSAAObjectId objectId) throws Exception
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "edit");
      url.addParam(IPSHtmlParameters.SYS_VIEW, "sys_SingleField:" +
               objectId.getFieldName());
      url.addParam("refreshHint", "Selected");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      
      PSDisplayMapping dmapping = mgr.getDisplayMapping(Long.parseLong(objectId.getContentTypeId()),
    		  objectId.getFieldName());
      if(dmapping == null)
      {
    	  throw new PSAAClientActionException("Error occured while preparing the field for edit, " +
    	  		"falied to find the display mapper for the given field '" 
    			  + objectId.getFieldName() +"'");
      }
      PSControlRef control = dmapping.getUISet().getControl();
      
      int foundCount = 0;
      String height = null;
      String width = null;
      String aaRenderer = null;
      if(control != null)
      {
         Iterator props = control.getParameters();
         while(props.hasNext())
         {
            PSParam param = (PSParam)props.next();
            if(param.getName().equals(DLG_WIDTH))
            {
               width = param.getValue().getValueText();
               foundCount++;
            }
            else if(param.getName().equals(DLG_HEIGHT))
            {
               height = param.getValue().getValueText();
               foundCount++;
            }
            else if(param.getName().equals(AA_RENDERER))
            {
               aaRenderer = param.getValue().getValueText();
               foundCount++;
            }
            if(foundCount == 3)
               break;
         }
         
         
         width = StringUtils.defaultIfEmpty(width,
                  getControlParamValue(dmapping, DLG_WIDTH));
         height = StringUtils.defaultIfEmpty(height,
                  getControlParamValue(dmapping, DLG_HEIGHT));
         aaRenderer = StringUtils.defaultIfEmpty(aaRenderer,
               getControlParamValue(dmapping, AA_RENDERER));
      }
      Map<String,String> params = new HashMap<String, String>();
      params.put(DLG_WIDTH, width);
      params.put(DLG_HEIGHT, height);
      params.put(AA_RENDERER, StringUtils.capitalize(aaRenderer));
      return createJsonReturnString(url.toString(), params);
   }

   /**
    * Helper method to retrieve a control parameter
    * value from the control meta map.
    * @param mapping assumed not <code>null</code>.
    * @param paramname the name of the parameter to be found.
    * @return the parameter value which may be empty.
    */
   @SuppressWarnings("unchecked")
   private String getControlParamValue(PSDisplayMapping mapping,
      String paramname)
   {
      try
      {
         PSControlMeta controlMeta = 
            getControls().get(mapping.getUISet().getControl().getName());
         if(controlMeta == null)
            return "";
         List<PSControlParameter> params = 
            (List<PSControlParameter>)controlMeta.getParams();
         for(PSControlParameter param : params)
         {
            if(param.getName().equals(paramname))
            {
               return param.getDefaultValue();
            }
         }
      }
      catch(Exception ignore)
      {
         ignore.printStackTrace();
      }
      return "";
   }
   
   /**
    * Assembles the contenteditor veiw revisions url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doCE_VIEW_REVISIONS(String queryurl,
            PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "preview");
      url.addParam(SYS_USERVIEW, "sys_Revisions");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Assembles the contenteditor view audit trail url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl the query url from the node def. Cannot
    * be <code>null</code> or empty.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doCE_VIEW_AUDIT_TRAIL(String queryurl,
            PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(queryurl);
      url.addParam(IPSHtmlParameters.SYS_COMMAND, "preview");
      url.addParam(SYS_USERVIEW, "sys_audittrail");
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION,
               getCorrectRevision(objectId));
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Assembles the preview page url. The revision used for this is always the 
    * current revision. 
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty. 
    * @throw if an error occurs creating the JSON object.   
    */
   @SuppressWarnings("unused") //dynamically called
   private String doPREVIEW_PAGE(PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(ASSEMBLY_URL);
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION,
         getCurrentRevision(objectId));
      url.addParam(IPSHtmlParameters.SYS_VARIANTID, objectId.getVariantId());
      url.addParam(IPSHtmlParameters.SYS_AUTHTYPE, objectId.getAuthType());
      url.addParam(IPSHtmlParameters.SYS_CONTEXT, objectId.getContext());
      url.addParam(IPSHtmlParameters.SYS_FOLDERID, objectId.getFolderId());
      url.addParam(IPSHtmlParameters.SYS_SITEID, objectId.getSiteId());
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Assembles the preview may page url. THe revision gets set to -1.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throw if an error occurs creating the json object.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doPREVIEW_MYPAGE(PSAAObjectId objectId) throws JSONException
   {
      SimpleURL url = createSimpleUrl(ASSEMBLY_URL);
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION, "-1");
      url.addParam(IPSHtmlParameters.SYS_VARIANTID, objectId.getVariantId());
      url.addParam(IPSHtmlParameters.SYS_AUTHTYPE, objectId.getAuthType());
      url.addParam(IPSHtmlParameters.SYS_CONTEXT, objectId.getContext());
      url.addParam(IPSHtmlParameters.SYS_FOLDERID, objectId.getFolderId());
      url.addParam(IPSHtmlParameters.SYS_SITEID, objectId.getSiteId());
      return createJsonReturnString(url.toString(), null);
   }
      
   /**
    * Assembles the related content search url.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param objectId the object id object, cannot be <code>null</code> or
    * empty.
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throws PSAAClientActionException 
    * @throw if an error occurs creating the json object.
    */
   private String doRC_SEARCH(PSAAObjectId objectId)
      throws JSONException, PSAAClientActionException
   {
      SimpleURL url = new SimpleURL(RC_SEARCH_URL);
      url.addParam("sys_componentname", "rcsearch");
      try
      {
         IPSTemplateSlot slot = PSActionUtil.loadSlot(objectId.getSlotId());
         url.addParam(IPSHtmlParameters.SYS_SLOTNAME, slot.getName());
      }
      catch (PSNotFoundException | PSAssemblyException e)
      {
         throw new PSAAClientActionException(e);
      }
      if(objectId.getContentId() != null)
      {
         url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
         url.addParam(IPSHtmlParameters.SYS_REVISION, 
               getCorrectRevision(objectId));
      }
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Creates the url needed for inline images and links.
    * This method is only public because it is called via reflection.
    * The method should not be called externally.
    * @param queryurl
    * @param objectId
    * @return the json object string that contains the url never 
    * <code>null</code> or empty.
    * @throws JSONException
    * @throws PSAAClientActionException
    */
   public String doCE_LINK(@SuppressWarnings("unused") String queryurl, PSAAObjectId objectId)
            throws JSONException, PSAAClientActionException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      String assemblyUrl = null;
      IPSRequestContext req = getRequestContext();
      try
      {
         assemblyUrl = mgr.getAssemblerUrl(req, Integer
                  .parseInt(objectId.getVariantId()));
      }
      catch (PSInternalRequestCallException e)
      {
        throw new PSAAClientActionException(e);
      }         
      SimpleURL url = createSimpleUrl(assemblyUrl);
      
      url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
      url.addParam(IPSHtmlParameters.SYS_REVISION,
               getCurrentRevision(objectId));
      url.addParam(IPSHtmlParameters.SYS_VARIANTID, objectId.getVariantId());
      url.addParam(IPSHtmlParameters.SYS_CONTEXT, 
         req.getParameter(IPSHtmlParameters.SYS_CONTEXT, "0"));
      url.addParam(IPSHtmlParameters.SYS_AUTHTYPE,
         req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE, "0"));
      url.addParam(IPSHtmlParameters.SYS_SITEID, 
         StringUtils.isBlank(
            objectId.getSiteId()) ? "" : objectId.getSiteId());
      url.addParam(IPSHtmlParameters.SYS_FOLDERID, 
         StringUtils.isBlank(
            objectId.getFolderId()) ? "" : objectId.getFolderId());
      return createJsonReturnString(url.toString(), null);
   }

   /**
    * Builds a URL that can be used to make a request to the server to launch
    * the Impact analyzer on the item identified by the supplied id. A special
    * property is added that changes the default behavior of the analyzer to
    * only show AA category relationships.
    * 
    * @param objectId Assumed not <code>null</code>.
    * 
    * @return A valid, fully qualified URL. Never <code>null</code> or empty.
    * 
    * @throws JSONException
    * @throws PSAAClientActionException 
    */
   @SuppressWarnings("unused")
   private String doTOOL_SHOW_AA_RELATIONSHIPS(PSAAObjectId objectId)
      throws JSONException, PSAAClientActionException
   {
      SimpleURL url = createUrlFromAction("Item_ViewDependents", objectId);
      //add a special filter that limits the UI to just show AA rels
      url.addParam(IPSHtmlParameters.SYS_RELATIONSHIP_CATEGORY_FILTER,
            "Active Assembly");
      return createJsonReturnString(url.toString(), null);
   }

   /**
    * Attempt to get the requested item into a public state (if it is not already
    * there,) then build the url that will launch the proper edition.
    * 
    * @param objectId Assumed not <code>null</code>;
    * @return A valid, fully qualified URL. Never <code>null</code> or empty.
    * 
    * @throws JSONException If the result cannot be created.
    * @throws PSAAClientActionException If the item is not public, or exactly
    * 1 transition to a non-adhoc state cannot be found or the transition fails.
    */
   @SuppressWarnings("unused") //dynamically called
   private String doTOOL_PUBLISH_NOW(PSAAObjectId objectId)
      throws JSONException, PSAAClientActionException
   {
      String cid = objectId.getContentId();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(cid));
      
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = mgr.loadComponentSummary(Integer.parseInt(cid));
      int wfId = sum.getWorkflowAppId();
      int stateId = sum.getContentStateId();
      
      try
      {
         IPSWorkflowService wfSvc = PSWorkflowServiceLocator.getWorkflowService();
         IPSGuid stateGuid = gmgr.makeGuid(stateId, PSTypeEnum.WORKFLOW_STATE);
         IPSGuid wfGuid = gmgr.makeGuid(wfId, PSTypeEnum.WORKFLOW);
         boolean isPublic = wfSvc.isPublic(stateGuid, wfGuid);
         if (!isPublic)
         {
            getLogger().info(
               "Attempting to checkin/transition item " + itemGuid.getUUID()
               + " for demand publishing.");
            PSWorkflow wf = wfSvc.loadWorkflow(wfGuid);
            IPSSystemWs sws = PSSystemWsLocator.getSystemWebservice();
            PSState startState = wf.findState(stateGuid);
            List<IPSGuid> itemIds = Collections.singletonList(itemGuid);
            Map<String,String> transitionNames = 
               sws.getAllowedTransitions(itemIds);
            List<String> transToPublic = new ArrayList<String>();
            List<String> adhocToPublic = new ArrayList<String>();
            for (String tname : transitionNames.keySet())
            {
               PSTransition tran = startState.findTransitionByName(tname);
               long targetStateId = tran.getToState();
               IPSGuid toStateGuid = gmgr.makeGuid(targetStateId,
                     PSTypeEnum.WORKFLOW_STATE);
               if (wfSvc.isPublic(toStateGuid, wfGuid))
               {
                  PSState targetState = wf.findState(toStateGuid);
                  if (targetState.isAdhocEnabled())
                     adhocToPublic.add(tname);
                  else
                     transToPublic.add(tname);
               }
            }
            if (transToPublic.size() != 1)
            {
               String msg;
               if (transToPublic.size() > 1)
               {
                  msg = 
                     "Found more than 1 transition that goes to a public state."
                     + " The item must be transitioned manually before attempting this action again."
                     + " Action not performed.";
               }
               else if (adhocToPublic.size() > 0)
               {
                  msg = "Only transitions to a public state using adhoc assignment were found."
                     + " The item must be transitioned manually before attempting this action again."
                     + " Action not performed.";
               }
               else
               {
                  msg = 
                     "No transitions found that go to a public state."
                     + " The item must be transitioned manually before attempting this action again."
                     + " Action not performed.";
               }
               getLogger().error(msg);
               throw new PSAAClientActionException(msg);
            }
            
            sws.transitionItems(itemIds, 
                  transToPublic.get(0), 
                  "Auto transitioned by a Publish-now action.", 
                  null, 
                  getRequestContext().getUserName());
         }
         SimpleURL url = createUrlFromAction("Publish_Now", objectId);
         return createJsonReturnString(url.toString(), null);      
      }
      catch (PSWorkflowException e)
      {
         throw new PSAAClientActionException(e.getLocalizedMessage());
      }
      catch (PSErrorsException e)
      {
         throw new PSAAClientActionException(e.getLocalizedMessage());
      }
      catch (PSErrorException e)
      {
         throw new PSAAClientActionException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Builds a URL that can be used to make a request to the server to launch
    * the AA editor on the item identified by the supplied id. The template in
    * the supplied id is used if allowed for the ctype, otherwise, the first
    * page template in numeric ascending order is used. If there are no page
    * templates, the first snippet is used by the same algorithm.
    * 
    * @param objectId Assumed not <code>null</code>.
    * 
    * @return A valid, fully qualified URL. Never <code>null</code> or empty.
    * 
    * @throws JSONException
    * @throws PSAAClientActionException 
    */
   @SuppressWarnings("unused") //dynamically called
   private String doTOOL_LINK_TO_PAGE(PSAAObjectId objectId)
      throws JSONException, PSAAClientActionException
   {
      try
      {
         SimpleURL url = createSimpleUrl(ASSEMBLY_URL);
         url.addParam(IPSHtmlParameters.SYS_CONTENTID, objectId.getContentId());
         url.addParam(IPSHtmlParameters.SYS_REVISION,
               getCorrectRevision(objectId));
         url.addParam(IPSHtmlParameters.SYS_SITEID, objectId.getSiteId());
         url.addParam(IPSHtmlParameters.SYS_AUTHTYPE, String.valueOf(0));
         url.addParam(IPSHtmlParameters.SYS_COMMAND, "editrc");
         url.addParam(IPSHtmlParameters.SYS_FOLDERID, objectId.getFolderId());
         url.addParam(IPSHtmlParameters.SYS_CONTEXT, String.valueOf(0));

         //have to find first page template (use one matching current if possible)
         String ctypeId = objectId.getContentTypeId();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid ctypeGuid = gmgr.makeGuid(ctypeId, PSTypeEnum.NODEDEF);
         IPSContentDesignWs cws = PSContentWsLocator.getContentDesignWebservice();
         List<PSContentTemplateDesc> associations = 
            cws.loadAssociatedTemplates(ctypeGuid, false, false, null, null);
         if (associations.isEmpty())
         {
            throw new PSAAClientActionException(
                  "This content type has no templates.");
         }
         int templateUuid = Integer.parseInt(objectId.getVariantId());
         boolean found = false;
         //first check if the default is acceptable
         for (PSContentTemplateDesc assoc : associations)
         {
            if (assoc.getTemplateId().getUUID() == templateUuid)
            {
               found = true;
               break;
            }
         }
         if (!found)
         {
            templateUuid = -1;
            //order the ids in ascending UUID numeric order
            Collections.sort(associations, new Comparator<PSContentTemplateDesc>()
            {
               /**
                * Sorts in ascending numeric UUID order.
                */
               public int compare(PSContentTemplateDesc o1,
                           PSContentTemplateDesc o2)
               {
                  int uuid1 = o1.getTemplateId().getUUID();
                  int uuid2 = o2.getTemplateId().getUUID();
                  return uuid1 > uuid2 ? 1 : (uuid1 < uuid2 ? -1 : 0) ;
               }
            });
            /*
             * find page template - load them 1 at a time in ascending numeric
             * order hoping that that will be quicker that loading all of them
             */
            IPSTemplateService templateSvc = PSAssemblyServiceLocator
                  .getAssemblyService();
            int defaultSnippetId = -1;
            for (PSContentTemplateDesc assoc : associations)
            {
               IPSAssemblyTemplate t = templateSvc.loadTemplate(assoc
                     .getTemplateId(), false);
               if (t.getOutputFormat() == IPSAssemblyTemplate.OutputFormat.Page)
               {
                  templateUuid = t.getGUID().getUUID();
                  break;
               }
               else if (defaultSnippetId < 0
                     && t.getOutputFormat() 
                        == IPSAssemblyTemplate.OutputFormat.Snippet)
               {
                  defaultSnippetId = t.getGUID().getUUID();
               }
            }
            if (templateUuid == -1)
            {
               if (defaultSnippetId == -1)
               {
                  throw new PSAAClientActionException(
                  "This content type has no page, dispatch or snippet templates assigned.");
               }
               templateUuid = defaultSnippetId;
            }
         }
         url.addParam(IPSHtmlParameters.SYS_VARIANTID, 
               String.valueOf(templateUuid));

         return createJsonReturnString(url.toString(), null);
      }
      catch (PSErrorResultsException e)
      {
         throw new PSAAClientActionException(e.getLocalizedMessage());
      }
      catch (PSAssemblyException ae)
      {
         throw new PSAAClientActionException(ae.getLocalizedMessage());
      }
   }
   
   /**
    * Builds a url to the mananage navaigation edit page.
    * @param objectId assumed not <code>null</code>.
    * @return url, never <code>null</code> or empty.
    * @throws JSONException 
    */
   @SuppressWarnings("unused")
   private String doMANAGE_NAVIGATION(PSAAObjectId objectId) throws JSONException
   {
       IPSManagedNavService managedNavService = 
           PSManagedNavServiceLocator.getContentWebservice();
       String slotId = String.valueOf(managedNavService.getMenuSlotId());
       IPSGuid folderGuid = PSGuidUtils.makeGuid(objectId.getFolderId(), PSTypeEnum.LEGACY_CONTENT);
       IPSGuid navGuid = managedNavService.findNavigationIdFromFolder(folderGuid);
       PSComponentSummary sum = PSAAObjectId.getItemSummary(navGuid.getUUID());
       String currentuser = getCurrentUser();       
       String rev = String.valueOf(sum.getAAViewableRevision(currentuser));
       
       String title = managedNavService.getNavTitle(navGuid);
       if(title == null)
          title = sum.getName();
      
       SimpleURL url = createSimpleUrl("/ui/activeassembly/navigation/managenavedit.jsp");
       url.addParam(IPSHtmlParameters.SYS_CONTENTID, String.valueOf(navGuid.getUUID()));
       url.addParam(IPSHtmlParameters.SYS_SLOTID, slotId);
       url.addParam(IPSHtmlParameters.SYS_REVISION, rev);
       url.addParam("section", title);
       return createJsonReturnString(url.toString(), null);
   }

   /**
    * Builds a url from a <code>PSAction</code> command definition.
    * 
    * @param actionName The internal name of the action. Assumed not blank.
    * @param objectId Assumed not <code>null</code>.
    * @return A url that can be used to perform the action.
    * @throws JSONException
    * @throws PSAAClientActionException If an action by the supplied name cannot
    * be found.
    */
   private String doGenericAction(String actionName, PSAAObjectId objectId)
      throws JSONException, PSAAClientActionException
   {
      SimpleURL url = createUrlFromAction(actionName, objectId); 
      return createJsonReturnString(url.toString(), null);
   }
   
   /**
    * Creates a simple url and Adds common parameters.
    * @param url assumed not <code>null</code>.
    * @return a
    */
   private SimpleURL createSimpleUrl(String url)
   {
      
      SimpleURL sUrl = new SimpleURL(url);
      String sys_aamode = getRequestContext().getParameter(
         IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE);
      if(StringUtils.isNotBlank(sys_aamode))
      {
         sUrl.addParam(IPSHtmlParameters.SYS_ACTIVE_ASSEMBLY_MODE,
            sys_aamode);
      }
      return sUrl;               
   }

   /**
    * The log mechanism for this class.
    * @return Never <code>null</code>.
    */
   private Log getLogger()
   {
      return LogFactory.getLog(getClass());
   }
   
   /**
    * Loads an action by the specified name, takes it's command and params and
    * processes them into the returned object, performing substitutions (a
    * subset of those supported by the CX) as required. If a parameter has a
    * variable value whose name is not recognized, it is skipped and a message
    * is noted in the log.
    * <p>
    * Special handling is done for sys_folderid. If one is not available in the
    * supplied <code>id</code> and one is needed, then all folders for the item
    * are found and sorted. If sys_siteid is present, they are also filtered by
    * that specific site, otherwise, they are filtered by removing folders not
    * in the //Sites hierarchy. The first one in the list is returned. If none
    * is found, the processing continues with no folderid.
    * 
    * @param actionName The name of the <code>PSAction</code> that will be
    * used to build the URL. Assumed not blank. If an action can't be found or 
    * loaded, an exception is thrown.
    * 
    * @param id The identifer for the item for which the URL will be generated.
    * @return Never <code>null</code>.
    * 
    * @throws PSAAClientActionException If the supplied action can't be found or
    * can't be loaded.
    */
   @SuppressWarnings("unchecked")
   private SimpleURL createUrlFromAction(String actionName, PSAAObjectId id)
      throws PSAAClientActionException
   {
      IPSUiWs uiMgr = PSUiWsLocator.getUiWebservice();
      List<PSAction> actions;
      try
      {
         actions = uiMgr.loadActions(actionName);
      }
      catch (PSErrorException e)
      {
         getLogger().error("Failed to load action named " + actionName, e);
         actions = Collections.emptyList();
      }
      if (actions.isEmpty())
      {
         throw new PSAAClientActionException("The action named '" + actionName 
               + "' either doesn't exist or failed to load. If it failed to "
               + "load, additional info will be in the server log.");
      }
      
      PSAction action = actions.get(0);
      SimpleURL url = createSimpleUrl(action.getURL());
      PSActionParameters params = action.getParameters();
      Iterator<PSActionParameter> paramsIter = params.iterator();
      Map<String, String> pairs = new HashMap<String, String>();
      while (paramsIter.hasNext())
      {
         PSActionParameter param = paramsIter.next();
         String value = param.getValue();
         if (value.startsWith("$"))
         {
            String varName = value.substring(1).toLowerCase().trim();
            if (varName.equals(IPSHtmlParameters.SYS_CONTENTID))
               value = id.getContentId();
            else if (varName.equals(IPSHtmlParameters.SYS_REVISION))
               value = getCorrectRevision(id);
            else if (varName.equals(IPSHtmlParameters.SYS_FOLDERID))
               value = id.getFolderId();
            else if (varName.equals(IPSHtmlParameters.SYS_SITEID))
               value = id.getSiteId();
            else if (varName.equals(IPSHtmlParameters.SYS_VARIANTID))
               value = id.getVariantId();
            else if (varName.equals(IPSHtmlParameters.SYS_CONTEXT))
               value = id.getContext();
            else //unsupported dynamic param
            {
               getLogger().info("Skipping unsupported parameter '" + value 
                     + "' for action '" + actionName + ".'" );
               continue;
            }
         }
         pairs.put(param.getName(), value);
      }
      
      /* 
       * fixup folderid. If a siteid is present, limit the possible folders to
       * those in that site. Take first one in ascending alpha order.
       */
      if (StringUtils.isBlank(pairs.get(IPSHtmlParameters.SYS_FOLDERID)))
      {
         IPSContentWs cmgr = PSContentWsLocator.getContentWebservice();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid guid = gmgr.makeGuid(new PSLocator(id.getContentId()));
         try
         {
            String[] paths = cmgr.findFolderPaths(guid);
            //remove non-site paths
            List<String> opaths = new ArrayList<String>();
            for (String path : paths)
            {
               if (path.startsWith("//Sites"))
                  opaths.add(path);
            }
            if (!opaths.isEmpty())
               Collections.sort(opaths);
            
            IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
            
            long siteUuid = -1;
            String s = pairs.get(IPSHtmlParameters.SYS_SITEID);
            if (StringUtils.isNotBlank(s))
            {
               siteUuid = Long.parseLong(s);
               IPSGuid sguid = gmgr.makeGuid(siteUuid, PSTypeEnum.SITE);
               IPSSite site = smgr.loadUnmodifiableSite(sguid);
               String siteRoot = site.getFolderRoot();
               if (StringUtils.isNotBlank(siteRoot))
               {
                  siteRoot = siteRoot.toLowerCase();
                  for (int i=opaths.size()-1; i >= 0; i--)
                  {
                     if (!opaths.get(i).toLowerCase().startsWith(siteRoot))
                        opaths.remove(i);
                  }
               }
            }
            if (opaths.size() > 0)
            {
               String path = opaths.get(0);
               List<IPSGuid> ids = cmgr.findPathIds(path);
               int fid = ids.get(ids.size()-1).getUUID();
               pairs.put(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(fid));
            }
         }
         catch (PSErrorException | PSNotFoundException e)
         {
            //ignore
            getLogger().error(
               "Exception while trying to infer folder path, processing continues.", 
               e);
         }
         catch (NumberFormatException e)
         {
            //ignore
            getLogger().error("Failed to parse siteId, processing continues.", e);
         }
      }
      
      for (String name : pairs.keySet())
         url.addParam(name, pairs.get(name));
      return url;
   }

   /**
    * Helper method to create the jason object return string that is expected by
    * the client.
    * 
    * @param url assumed to be not <code>null</code>.
    * @param params Map of key value pairs that needs to be added to the
    *           returning JSON object may be <code>null</code> or empty.
    * @return the json object string, never <code>null</code> or empty.
    * @throws JSONException if an error occurs while creating the JSON object.
    */
   private String createJsonReturnString(String url, Map<String, String> params)
      throws JSONException
   {
      JSONObject obj = new JSONObject();
      String newurl = getRoot() + url;
      if(newurl.startsWith("//")){
        newurl = StringUtils.replace(newurl,"//","/Rhythmyx/");
      }
      obj.append("url", newurl);

      if(params != null)
      {
         Iterator<String> iter = params.keySet().iterator();
         while(iter.hasNext())
         {
            String key = iter.next();
            String value = params.get(key);
            if(StringUtils.isNotBlank(value))
               obj.append(key, value);
         }
      }
      return obj.toString();
   }
   
   /**
    * Retrieves the control meta from the server or the local
    * cached copy of the meta if it exists.
    * @return control meta, never <code>null</code>.
    * @throws Exception if an error occurs during the request
    * or parsing of the response document.
    */
   private Map<String, PSControlMeta> getControls() throws Exception
   {
      if(m_controls == null)
      {
         
         IPSRequestContext requestCtx = getRequestContext();
         ByteArrayInputStream in = null;
         List<PSControlMeta> ctrlList = new ArrayList<PSControlMeta>();
         Document controlXML;
         String resource = 
            "sys_psxContentEditorCataloger/getControls.xml";
         IPSInternalRequest iRequest = requestCtx.getInternalRequest(resource);      
         try
         {
            byte[] result = iRequest.getMergedResult();
            in = new ByteArrayInputStream(result);
            controlXML = PSXmlDocumentBuilder.createXmlDocument(in, false);
            if (controlXML != null)
            {
               
               NodeList controlNodes = controlXML
               .getElementsByTagName(PSControlMeta.XML_NODE_NAME);
               for (int i = 0; controlNodes != null
               && i < controlNodes.getLength(); i++)
               {
                  ctrlList
                  .add(new PSControlMeta((Element) controlNodes.item(i)));
               }         
            }
            m_controls = new HashMap<String, PSControlMeta>();
            for(PSControlMeta meta : ctrlList)
            {
               m_controls.put(meta.getName(), meta);
            }
         }
         finally
         {
            if(in != null)
            {
               try
               {
                 in.close(); 
               }
               catch(IOException ignore)
               {}
            }
         }
      }
      
      return m_controls;
   }
   
   /**
    * Simple class used to assemble a url
    */
   class SimpleURL
   {
      /**
       * 
       * @param urlbase contains the path and optional query part.
       */
      public SimpleURL(String urlbase)
      {
         //normalize it
         urlbase = urlbase.trim();
         if(urlbase.startsWith(".."))
            urlbase = urlbase.substring(2);
         if (urlbase.endsWith("&") || urlbase.endsWith("?"))
            urlbase = urlbase.substring(0, urlbase.length()-1);
         mi_base = urlbase;
      }
      
      /**
       * Add a parameter to the url.
       * @param name assumed not <code>null</code>.
       * @param value may be <code>null</code>. URL encoded when the URL is 
       * assembled.
       */
      public void addParam(String name, String value)
      {
         mi_params.put(name, value);
      }
      
      /**
       * Returns the url string.
       * @return never <code>null</code> or empty.
       */
      @Override
      public String toString()
      {
         StringBuilder sb = new StringBuilder(mi_base);
         boolean firstParam = mi_base.contains("?") ? false : true;
         if(!mi_params.isEmpty())
         {
            for(String name : mi_params.keySet())
            {
               sb.append(firstParam ? "?" : "&");
               firstParam = false;
               sb.append(name);
               sb.append("=");
               try
               {
                  String value = mi_params.get(name);
                  if (StringUtils.isNotBlank(value))
                     sb.append(URLEncoder.encode(value, "UTF8"));
               }
               catch (UnsupportedEncodingException e)
               {
                  throw new RuntimeException(e);
               }
            }            
         }
         return sb.toString();
        
      }
      
      private String mi_base;
      
      @SuppressWarnings("unchecked")
      private Map<String, String> mi_params = (Map<String, String>)MapUtils.orderedMap(
               new HashMap<String, String>());
   }
   
   /**
    * Cached control meta for all controls in the system. Initialized
    * by {@link #getControls()}.
    */
   private Map<String, PSControlMeta> m_controls;
   
   /**
    * Assembly url constant.
    */
   private static final String ASSEMBLY_URL = "/Rhythmyx/assembler/render";
 
//   /**
//    * The partial URL that is used to launch an error page that reports that a
//    * requested action is missing.
//    * @fixme 
//    */
//   private static final String MISSING_ACTION_URL = "";
//   
//   /**
//    * The partial URL that will reach the Impact analysis applet. Of the form
//    * <code>/app/resource.html</code>
//    */
//   private static final String IMPACT_ANALYSIS_URL = 
//      "/sys_cxDependencyTree/dependencytree.html";
//   
//   /**
//    * The partial URL that will create a promotable version and launch the
//    * content editor. Of the form <code>/app/resource.html</code>
//    */
//   private static final String CREATE_VERSION_URL = 
//      "/sys_cxSupport/contenteditorurls.html";
//   
//   /**
//    * The partial URL that will launch the comparison tool. Of the form
//    * <code>/app/resource.html</code>
//    */
//   private static final String COMPARE_URL = "/sys_Compare/compare.html";
//   
//   /**
//    * The partial URL that will launch a dialog that allows locale selection and
//    * then creates a translation. Of the form <code>/app/resource.html</code>
//    */
//   private static final String CREATE_TRANSLATION_URL = 
//      "/sys_actionTranslate/translate.html";
   
   /**
    * Related content search form url constant.
    */
   private static final String RC_SEARCH_URL = "/sys_searchSupport/getQuery.html";

   /**
    * Constant for sys_userview parameter for content editor revision and 
    * audi trail URLs
    */
   private static final String SYS_USERVIEW = "sys_userview";
   
   /**
    * Constant for dialog height parameter
    */
   private static final String DLG_HEIGHT = "dlg_height";

   /**
    * Constant for dialog height parameter
    */
   private static final String AA_RENDERER = "aarenderer";

   /**
    * Constant for dialog width parameter
    */
   private static final String DLG_WIDTH = "dlg_width";
   
   // Url types that can be returned
   public static final String TYPE_CE_EDIT = "CE_EDIT";
   public static final String TYPE_CE_VIEW_CONTENT = "CE_VIEW_CONTENT";
   public static final String TYPE_CE_VIEW_PROPERTIES = "CE_VIEW_PROPERTIES";
   public static final String TYPE_CE_FIELDEDIT = "CE_FIELDEDIT";
   public static final String TYPE_CE_VIEW_REVISIONS = "CE_VIEW_REVISIONS";
   public static final String TYPE_CE_VIEW_AUDIT_TRAIL = "CE_VIEW_AUDIT_TRAIL";
   public static final String TYPE_MANAGE_NAVIGATION = "MANAGE_NAVIGATION";
   public static final String TYPE_PREVIEW_PAGE = "PREVIEW_PAGE";
   public static final String TYPE_PREVIEW_MYPAGE = "PREVIEW_MYPAGE";   
   public static final String TYPE_RC_SEARCH = "RC_SEARCH";
   public static final String TYPE_CE_LINK = "CE_LINK";
   public static final String TYPE_TOOL_SHOW_AA_RELATIONSHIPS = 
      "TOOL_SHOW_AA_RELATIONSHIPS";
   public static final String TYPE_LINK_TO_PAGE = "TOOL_LINK_TO_PAGE";
   public static final String TYPE_TOOL_PUBLISH_NOW = "TOOL_PUBLISH_NOW";
//   public static final String TYPE_TOOL_COMPARE_REVISIONS = 
//      "TOOL_COMPARE_REVISIONS";
   
   public static final List<String> ms_allowedTypes = new ArrayList<String>();
   
   static
   {
      ms_allowedTypes.add(TYPE_CE_EDIT);
      ms_allowedTypes.add(TYPE_CE_VIEW_CONTENT);
      ms_allowedTypes.add(TYPE_CE_VIEW_PROPERTIES);
      ms_allowedTypes.add(TYPE_CE_FIELDEDIT);
      ms_allowedTypes.add(TYPE_CE_VIEW_REVISIONS);
      ms_allowedTypes.add(TYPE_CE_VIEW_AUDIT_TRAIL);
      ms_allowedTypes.add(TYPE_MANAGE_NAVIGATION);
      ms_allowedTypes.add(TYPE_PREVIEW_PAGE);
      ms_allowedTypes.add(TYPE_PREVIEW_MYPAGE);
      ms_allowedTypes.add(TYPE_RC_SEARCH);
      ms_allowedTypes.add(TYPE_CE_LINK);
      ms_allowedTypes.add(TYPE_TOOL_SHOW_AA_RELATIONSHIPS);
      ms_allowedTypes.add(TYPE_LINK_TO_PAGE);
      ms_allowedTypes.add(TYPE_TOOL_PUBLISH_NOW);
//      ms_allowedTypes.add(TYPE_TOOL_COMPARE_REVISIONS);
   }

}
