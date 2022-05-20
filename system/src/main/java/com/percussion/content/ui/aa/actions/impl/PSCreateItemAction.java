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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefSummary;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSDetails;
import com.percussion.design.objectstore.PSDisplayError;
import com.percussion.design.objectstore.PSFieldError;
import com.percussion.design.objectstore.PSFieldValidationException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCms;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Creates a new item or new copy of the supplied item and adds it to the
 * supplied folder corresponding to the folder path. Expects sys_contenttypeid,
 * folderPath as required parameters and itemPath as an optional parameter.
 * Returns a JSONObject consistsing of itemId and folderId parameters. If there
 * is an error adding the item to the folder then returns -1 for folderId. If
 * there is a validation error while creating the item, adds validationError
 * parameter to the JSONObject and returns.
 * 
 */
public class PSCreateItemAction extends PSAAActionBase
{

   /*
    * (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(Map<String, Object> params)
         throws PSAAClientActionException
   {
      int ctypeId = getValidatedInt(params,
            IPSHtmlParameters.SYS_CONTENTTYPEID, true);
      Object folderPath = getParameter(params, "folderPath");
      Object itemPath = getParameter(params, "itemPath");
      Object itemTitle = getParameter(params, "itemTitle");
      if(itemTitle == null || StringUtils.isBlank(itemTitle.toString()))
      {
         throw new PSAAClientActionException("Title must not be empty.");
      }
      JSONObject obj = new JSONObject();
      
      int newItem = -1;
      try
      {
         newItem = createItem(ctypeId, itemPath, itemTitle.toString());
         if(newItem == -1)
            throw new PSAAClientActionException("Failed to create the content item.");
         obj.append("itemId", newItem);
      }
      catch(PSErrorResultsException e)
      {
         ms_log.error(e);
         PSFieldValidationException ve = getValidationError(e);
         if(ve != null)
         {
            try
            {
               PSDisplayError de = ve.getDisplayError();
               String validationError = "";
               for (PSDetails dt : de.getDetails())
               {
                  for (PSFieldError fe : dt.getFieldErrors())
                  {
                     validationError += fe.getErrorText() + "\n";
                  }
               }
               obj.append("validationError", validationError);
            }
            catch (JSONException e1)
            {
               //ignore
            }
            return new PSActionResponse(obj.toString(),
                  PSActionResponse.RESPONSE_TYPE_JSON);
         }
         throw new PSAAClientActionException(e);
      }
      catch(PSAAClientActionException aace)
      {
    	  throw aace;
      }
      catch (Exception e)
      {
         
         ms_log.error(e);
         throw new PSAAClientActionException(e);
      }
      int newFolder = -1;
      try
      {
         newFolder = addItemToFolder(newItem,folderPath.toString());
         obj.append("folderId", newFolder);
      }
      catch(PSErrorException e)
      {
         ms_log.error(e);
         try
         {
            obj.append("folderId", -1);
         }
         catch (JSONException e1)
         {
            //ignore
         }
      }
      catch (JSONException e)
      {
         //ignore
      }
      
      return new PSActionResponse(obj.toString(),
            PSActionResponse.RESPONSE_TYPE_JSON);
   }

   /**
    * Helper method to find whether cause of the supplied
    * PSErrorResultsException is PSFieldValidationException or not.
    * 
    * @param e assumed not <code>null</code>.
    * @return <code>null<code> or an object of PSFieldValidationException.
    */
   private PSFieldValidationException getValidationError(
         PSErrorResultsException e)
   {
      Map<IPSGuid,Object> er = e.getErrors();
      if(er.isEmpty())
         return null;
      Object obj = er.get(er.keySet().iterator().next());
      if(obj instanceof PSErrorException)
      {
         PSErrorException ee = (PSErrorException) obj;
         Throwable t = ee.getCause();
         if(t instanceof PSFieldValidationException)
         {
            return (PSFieldValidationException)t;
         }
      }
      return null;
   }

   /**
    * Creates the new Item or new copy with the supplied parameters.
    * 
    * @param ctypeId Id of the content type whose item will be created assumed
    *           to be a valid content type id.
    * @param itemPath If <code>null</code> or empty creates a new item, other
    *           wise creates a new copy of the item corresponding to the path.
    * @return Id of the newly created item.
    * @throws PSErrorException
    *            {@see IPSContentWs#createItems(String, int, String, String)}
    * @throws PSErrorResultsException
    *            {@see IPSContentWs#saveItems(List, boolean, boolean, String, String)}
    * @throws PSInvalidContentTypeException
    *            {@see PSItemDefManager#getSummary(String, int)}
    * @throws PSErrorsException
    *            {@see IPSContentWs#checkinItems(List, String, String)}
    * @throws PSAuthenticationFailedException 
    * @throws PSAuthorizationException 
    * @throws PSInternalRequestCallException 
    * @throws PSAAClientActionException 
    */
   private int createItem(int ctypeId, Object itemPath, String itemTitle)
      throws PSErrorException, PSErrorResultsException,
      PSInvalidContentTypeException, PSErrorsException,
      PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSAAClientActionException
   {
      int newItem = -1;
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      canCreateItem(ctypeId);

      if (itemPath == null || StringUtils.isBlank(itemPath.toString()))
      {
         PSItemDefManager defMgr = PSItemDefManager.getInstance();
         PSItemDefSummary sum = defMgr.getSummary(ctypeId,
               PSItemDefManager.COMMUNITY_ANY);
         List<PSCoreItem> items = cws.createItems(sum.getName(), 1,
               getRequestContext().getUserSessionId(), getCurrentUser());
         setTitles(items.get(0),itemTitle);
         List<IPSGuid> nitems = cws.saveItems(items, false, false,
               getRequestContext().getUserSessionId(), getCurrentUser());
         newItem = nitems.get(0).getUUID();
      }
      else
      {
         IPSGuid id = cws.getIdByPath(itemPath.toString());
         if(id==null)
            return -1;
         IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = objMgr.loadComponentSummary(id.getUUID());
         PSLocator loc = summary.getCurrentLocator();
         loc.setRevision(summary.getAAViewableRevision(getCurrentUser()));
         IPSGuid iguid = PSGuidManagerLocator.getGuidMgr().makeGuid(loc);
         List<PSCoreItem> items = cws.newCopies(Collections
               .singletonList(iguid), Collections
               .singletonList(""), null, false,
               getRequestContext().getUserSessionId(), getCurrentUser());
         IPSGuid itemGuid = PSGuidManagerLocator.getGuidMgr().makeGuid(
               new PSLocator(items.get(0).getContentId(), 1));
         cws.checkoutItems(Collections.singletonList(itemGuid), "",
               getCurrentUser());
         setTitles(items.get(0),itemTitle);
         List<IPSGuid> nitems = cws.saveItems(items, false, false,
               getRequestContext().getUserSessionId(), getCurrentUser());
         newItem = nitems.get(0).getUUID();
      }

      return newItem;
   }
   
   /**
    * Checks whether an item of the supplied content type can be created by the
    * logged in user if not throws an exception, otherwise does nothing. Makes
    * an internal request to the content editor URL by passing in workflow id
    * and other parameters. If user is not authorized to create item, then the
    * internal request throws an exception with authorization failure message in
    * it. If the message has authorization failure, then throws client exception
    * with authorization error message, for any other exception simply returns
    * that exception wrapping in client exception.
    * 
    * @param ctypeId id of the content type whose item needs to be created, if 
    * not a valid content type then throws exception.
    * @throws PSInternalRequestCallException
    * @throws PSAuthorizationException
    * @throws PSAuthenticationFailedException
    * @throws PSInvalidContentTypeException
    * @throws PSAAClientActionException
    */
   private void canCreateItem(int ctypeId)
      throws PSInternalRequestCallException, PSAuthorizationException,
      PSAuthenticationFailedException, PSInvalidContentTypeException,
      PSAAClientActionException
   {
       PSItemDefManager defMgr = PSItemDefManager.getInstance();
       IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, ctypeId);
       PSNodeDefinition def = PSContentTypeHelper.findNodeDef(guid);
       PSItemDefinition itemDef = defMgr.getItemDef(ctypeId, -1);
      int workflowId = PSCms.getDefaultWorkflowId(PSServer
            .getRequest(getRequestContext()), itemDef.getContentEditor());
       String editorUrl = def.getQueryRequest();
       Map<String,String> params = new HashMap<String, String>();
       params.put(IPSHtmlParameters.SYS_VIEW, IPSConstants.SYS_ALL_VIEW_NAME);
       params.put(IPSHtmlParameters.SYS_COMMAND, "edit");
       params.put("sys_workflowappid", workflowId + "");
       IPSInternalRequest ir = getRequestContext().getInternalRequest(
      		 editorUrl, params, false);
      try
      {
         ir.getResultDoc();
      }
      catch (Exception e)
      {
         ms_log.error(e);
         if (e.getMessage().indexOf("PSAuthorizationException") != -1)
            throw new PSAAClientActionException(
                  "You are not authorized to create the content of the selected type.");
         else
            throw new PSAAClientActionException(e);
      }
   }
   
   /**
    * Helper method to set the values of {@link IPSHtmlParameters#SYS_TITLE}
    * field and displaytitle filed to the supplied itemTitle on the
    * supplied coreItem, if those fields exist. 
    * 
    * @param itemTitle Assumed not <code>null</code>.
    * @param coreItem Assumed not <code>null</code>.
    */
   private void setTitles(PSCoreItem coreItem, String itemTitle)
   {
      Iterator<PSItemField> fields = coreItem.getAllFields();
      PSItemField titleFld = null;
      PSItemField dsTitleFld = null;
      while (fields.hasNext())
      {
         PSItemField fld = fields.next();
         if (fld.getName().equals(IPSHtmlParameters.SYS_TITLE))
         {
            titleFld = fld;
         }
         else if (fld.getName().equals("displaytitle"))
         {
            dsTitleFld = fld;
         }
         // Break if we have both fields
         if (titleFld != null && dsTitleFld != null)
         {
            break;
         }
      }
      if (titleFld != null)
      {
         titleFld.addValue(new PSTextValue(itemTitle));
      }
      //Check whether display title field exists or not and also check whether
      //it is a single value field or not before setting the field value.
      if (dsTitleFld != null && !dsTitleFld.isMultiValue())
      {
         dsTitleFld.addValue(new PSTextValue(itemTitle));
      }
   }
   
   /**
    * Helper method to add the supplied itemid to the folder corresponding to
    * the supplied folder path.
    * 
    * @param itemid assumed as a valid item id.
    * @param folderPath Assumed not <code>null</code> or empty and corresponds
    *           to valid folder.
    * @return folder id corresponding to the supplied older path.
    * @throws PSErrorException Incase of failure to add the item to the folder.
    */
   private int addItemToFolder(int itemid, String folderPath)
         throws PSErrorException
   {
      IPSContentWs cws = PSContentWsLocator.getContentWebservice();
      IPSGuid fid = cws.getIdByPath(folderPath.toString());
      if(fid == null)
         return -1;
      cws.addFolderChildren(getItemGuid(fid.getUUID()), Collections
            .singletonList(getItemGuid(itemid)));
      return fid.getUUID();
   }

   /**
    * Logger to use, never <code>null</code>.
    */
   private static Log ms_log = LogFactory.getLog(PSCreateItemAction.class);

}
