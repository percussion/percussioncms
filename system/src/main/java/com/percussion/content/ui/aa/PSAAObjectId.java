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
package com.percussion.content.ui.aa;


import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The object id class to recognize active assembly elements. It creates and
 * parses a JSON Array consisting of the values of following parameters in the
 * order mentioned below.
 * <p>
 * 0 - Active assembly node type as given in PSAANodeType
 * </p>
 * <p>
 * 1 - Content id
 * </p>
 * <p>
 * 2 - Template id
 * </p>
 * <p>
 * 3 - Site id
 * </p>
 * <p>
 * 4 - Folder id
 * </p>
 * <p>
 * 5 - Context
 * </p>
 * <p>
 * 6 - Authtype
 * </p>
 * <p>
 * 7 - Contenttype Id
 * </p>
 * <p>
 * 8 - Checkedout status The values will be 
 * 0 if it is not checked out to anyone 
 * 1 if checked out by the login user
 * 2 if it is checked out to someone other than current user 
 * </p>
 * <p>
 * 9 - Slot id
 * </p>
 * <p>
 * 10 - Relationship id
 * </p>
 * <p>
 * 11 - Field name
 * </p>
 * <p>
 * 12 - Parent Content Id
 * </p>
 * <p>
 * 13 - Field label
 * </p>
 * <p>
 * 14 - Sort rank applicable to only snippet object tytpes
 * </p>
 * 
 */
public class PSAAObjectId
{
   /**
    * Creates a string that can be passed to the ctor of this class
    * to create an instance targeted as a snippet in a slot.
    * @return Always valid. All values except the supplied ones are null.
    */
   public static String createObjectString(int contentId, int templateId, long contentTypeId, int slotId)
   {
      JSONArray result = new JSONArray();
      try
      {
         result.put(m_indices.get(PARAM_NODE_TYPE).intValue(), PSAANodeType.AA_NODE_TYPE_SNIPPET.ordinal());
         result.put(m_indices.get(IPSHtmlParameters.SYS_CONTENTID).intValue(), contentId);
         result.put(m_indices.get(IPSHtmlParameters.SYS_VARIANTID).intValue(), templateId);
         result.put(m_indices.get(IPSHtmlParameters.SYS_SITEID).intValue(), (Integer) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_FOLDERID).intValue(), (Integer) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_CONTEXT).intValue(), (Integer) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_AUTHTYPE).intValue(), (Integer) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_CONTENTTYPEID).intValue(), contentTypeId);
         result.put(m_indices.get(PARAM_CHECKEDOUT_STATUS).intValue(), (Integer) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_SLOTID).intValue(), slotId);
         result.put(m_indices.get(IPSHtmlParameters.SYS_RELATIONSHIPID).intValue(), (Integer) null);
         result.put(m_indices.get(PARAM_FIELD_NAME).intValue(), (String) null);
         result.put(m_indices.get(PARAM_PARENT_ID).intValue(), (Integer) null);
         result.put(m_indices.get(PARAM_FIELD_LABEL).intValue(), (String) null);
         result.put(m_indices.get(IPSHtmlParameters.SYS_SORTRANK).intValue(), (Integer) null);
      }
      catch (JSONException e)
      {
         throw new RuntimeException(e);
      }

      return result.toString();
   }
   
   /**
    * Constructor that takes a string representation of JSON array of values of
    * the parameters mentioned in the class description.
    * 
    * @param objectId String representing the JSON array of values
    * @throws JSONException
    */
   public PSAAObjectId(String objectId) throws JSONException
   {
      JSONArray obj = new JSONArray(objectId);
      if (obj.length() != m_indices.size())
      {
         throw new IllegalArgumentException(
               "The size of the array does not match with the parameters.");
      }
      m_objectId = obj;
   }

   /**
    * Constructs object id for page type. Calls
    * {@link #PSAAObjectId(PSAANodeType, IPSAssemblyItem, String)} with page
    * node type and null for slot name.
    */
   public PSAAObjectId(IPSAssemblyItem item) throws PSAssemblyException,
         PSMissingBeanConfigurationException, JSONException
   {
      this(PSAANodeType.AA_NODE_TYPE_PAGE, item, null);
   }

   /**
    * Constructs object id for supplied node type. Calls
    * {@link #PSAAObjectId(PSAANodeType, IPSAssemblyItem, String)} with null
    * for slot name.
    */
   public PSAAObjectId(PSAANodeType nodeType, IPSAssemblyItem item)
         throws PSAssemblyException, PSMissingBeanConfigurationException,
         JSONException
   {
      this(nodeType, item, null);
   }

   /**
    * Constructs objectid for the given nodetype.
    * 
    * @param nodeType The type of the node must not be <code>null</code>.
    * @param item The assembly work item must not be <code>null</code>.
    * @param name The name of the slot or field, may be <code>null</code> for
    *           nodetype page but must not be null or empty for node types
    *           slot,snippet and field.
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws JSONException
    */
   public PSAAObjectId(PSAANodeType nodeType, IPSAssemblyItem item,
         String name) throws PSAssemblyException,
         PSMissingBeanConfigurationException, JSONException
   {
      this(nodeType, item, name, null);
   }
   
   /**
    * Constructs objectid for the given nodetype.
    * 
    * @param nodeType The type of the node must not be <code>null</code>.
    * @param item The assembly work item must not be <code>null</code>.
    * @param slotname The name of the slot or field, may be <code>null</code>
    * for nodetype page but must not be null or empty for node types
    * slot,snippet and field.
    * @param sortrank optional sort rank, may be <code>null</code> or empty.
    * @throws JSONException if fails to create JSON array from the assembly item
    * parameters.
    * @throws PSMissingBeanConfigurationException is the assmebly service is not
    * loaded because of incorrect service confgiuration.
    * @throws PSAssemblyException if assembly service throws errors while
    * loading slot or template in the process of building the objectid.
    */
   public PSAAObjectId(PSAANodeType nodeType, IPSAssemblyItem item,
      String slotname, String sortrank) throws PSAssemblyException,
      PSMissingBeanConfigurationException, JSONException
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      if (nodeType == null)
      {
         throw new IllegalArgumentException("nodeType must not be null");
      }
      if ((nodeType.equals(PSAANodeType.AA_NODE_TYPE_SLOT)
         || nodeType.equals(PSAANodeType.AA_NODE_TYPE_SNIPPET) || nodeType
         .equals(PSAANodeType.AA_NODE_TYPE_FIELD))
         && StringUtils.isEmpty(slotname))
         throw new IllegalArgumentException(
            "slotname must not be null or empty");

      m_objectId = new JSONArray();
      addParamsToJsonArray(nodeType, item, slotname, sortrank);
   }

   /**
    * String representation of the JSONArray object this class holds.
    * @return String 
    */
   @Override
   public String toString()
   {
      return m_objectId.toString();
   }
   
   /**
    * 
    * @param nodeType The type of node, assumed not <code>null</code>.
    * @param item The assembly workitem, assumed not <code>null</code>.
    * @param name The name of the slot, may be <code>null</code> or empty.
    * @param sortrank sort rank (applicable to only
    * {@link PSAANodeType#AA_NODE_TYPE_SNIPPET}) as string, may be
    * <code>null</code> or empty.
    * @throws PSAssemblyException
    * @throws PSMissingBeanConfigurationException
    * @throws JSONException
    */
   private void addParamsToJsonArray(PSAANodeType nodeType,
         IPSAssemblyItem item, String name, String sortrank) throws PSAssemblyException,
         PSMissingBeanConfigurationException, JSONException
   {
      Map<String,String> allParams = getParams(item);
      Map<String, String> params = parseCommonParams(item, allParams);
      // Added to all types of nodes is the node type
      params.put(PARAM_NODE_TYPE, String.valueOf(nodeType.getOrdinal()));

      String temp;
      switch (nodeType)
      {
         case AA_NODE_TYPE_SLOT :
            // slotid required
            int slotid = PSAssemblyServiceLocator.getAssemblyService()
            .findSlotByName(name).getGUID().getUUID();
            params.put(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slotid));
            break;
         case AA_NODE_TYPE_SNIPPET :
            // relationshipid required
            temp = parseParam(allParams, IPSHtmlParameters.SYS_RELATIONSHIPID,
                  null, true);
            params.put(IPSHtmlParameters.SYS_RELATIONSHIPID, temp.toString());
            // slotid required
            int snslotid = PSAssemblyServiceLocator.getAssemblyService()
            .findSlotByName(name).getGUID().getUUID();
            params.put(IPSHtmlParameters.SYS_SLOTID, String.valueOf(snslotid));
            params.put(IPSHtmlParameters.SYS_SORTRANK, sortrank);
            break;
         case AA_NODE_TYPE_FIELD :
            // CE field name required
            params.put(PARAM_FIELD_NAME, name);
            try
            {
               IPSNodeDefinition def = 
                  (IPSNodeDefinition)item.getNode().getDefinition();
               PSItemDefManager idManager = PSItemDefManager.getInstance();
               String label = 
                  idManager.getFieldLabel(def.getGUID().longValue(), name);
               params.put(PARAM_FIELD_LABEL, label);
            }
            catch (RepositoryException e)
            {
               e.printStackTrace();
            }
            break;
         case AA_NODE_TYPE_PAGE :
            break;
      }

      addParamToJsonArray(PARAM_NODE_TYPE, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_CONTENTID, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_VARIANTID, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_SITEID, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_FOLDERID, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_CONTEXT, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_AUTHTYPE, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_CONTENTTYPEID, params);
      addParamToJsonArray(PARAM_CHECKEDOUT_STATUS, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_SLOTID, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_RELATIONSHIPID, params);
      addParamToJsonArray(PARAM_FIELD_NAME, params);
      addParamToJsonArray(PARAM_PARENT_ID, params);
      addParamToJsonArray(PARAM_FIELD_LABEL, params);
      addParamToJsonArray(IPSHtmlParameters.SYS_SORTRANK, params);
   }
   
   /**
    * Helper method to add a given param to JSON array object
    * 
    * @param paramname The name of the parameter that needs to be added.
    * @param params The map of parameter name and values. If the value is null
    *           then, JSONObject.NULL is placed in the array.
    * @throws JSONException
    */
   private void addParamToJsonArray(String paramname, Map<String, String> params)
         throws JSONException
   {
      String val = params.get(paramname);
      modifyParam(paramname, val);
   }
   
   /**
    * Modifies or adds a parameter to the object.
    * @param paramname cannot be <code>null</code> or empty and
    * must be valid for this object.
    * @param value may be <code>null</code> or empty.
    * @throws JSONException 
    */
   public void modifyParam(String paramname, String value)
      throws JSONException
   {
      if(StringUtils.isBlank(paramname))
         throw new IllegalArgumentException("paramname cannot be null or empty.");
      Integer index = m_indices.get(paramname);
      if(index == null)
         throw new IllegalArgumentException("The param name is not valid.");
      if (value == null)
         m_objectId.put(index, JSONObject.NULL);
      else
         m_objectId.put(index, value);
   }
   
   /*
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {      
      PSAAObjectId clone = null;
      try
      {
         clone = new PSAAObjectId(this.toString());
      }
      catch (JSONException ignore)
      {
         // Should never happen
      }
      return clone;
   }

   /**
    * Helper method to find the value of check out status for the specified
    * item.
    * 
    * @param sum component summary of the item in question, assumed not
    * <code>null</code>.
    * 
    * @return The String representation of value of:
    * <ol>
    * <li>0 if the item is not checked to by anyone</li>
    * <li>1 if it is checked out to the login user</li>
    * <li>2 if it is checked out to someone other than login user</li>
    * </ol>
    */
   static private String getCheckOutStatusValue(PSComponentSummary sum)
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
      String loginuser = req.getServletRequest().getRemoteUser();

      String chkuser = StringUtils.defaultString(sum.getCheckoutUserName());
      if (StringUtils.isBlank(chkuser))
         return String.valueOf(NOT_CHECKED_OUT);
      else if (chkuser.equalsIgnoreCase(loginuser))
         return String.valueOf(CHECKED_OUT_BYME);
      
      return String.valueOf(CHECKED_OUT_BYOTHERS);
   }

   /**
    * Helper method to get the component summary for the passed in content id.
    * 
    * @param cid Content id
    * @return PSComponentSummary object or <code>null</code>, if the item is
    *         not found.
    * @throws PSMissingBeanConfigurationException
    * @throws NumberFormatException
    */
   static public PSComponentSummary getItemSummary(int cid)
         throws PSMissingBeanConfigurationException, NumberFormatException
   {
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = objMgr.loadComponentSummary(cid);
      return summary;
   }
   
   /**
    * Helper method to get the component summary for the content id contained
    * in this object.
    * 
    * @return PSComponentSummary object or <code>null</code>, if the item is
    *         not found.
    * @throws PSMissingBeanConfigurationException
    * @throws NumberFormatException
    */
   public PSComponentSummary getItemSummary()
         throws PSMissingBeanConfigurationException, NumberFormatException
   {
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = 
         objMgr.loadComponentSummary(Integer.parseInt(getContentId()));
      return summary;
   }

   /**
    * Helper method to get the parameters from the item, adds
    * IPSHtmlParameters.SYS_VARIANTID parameter to the list if there is one in
    * the item.
    * 
    * @param item the current item.
    * @return Map of params never <code>null</code>.
    */
   private Map<String, String> getParams(IPSAssemblyItem item)
   {
      if (item == null)
      {
         throw new IllegalArgumentException("item must not be null");
      }
      Map<String, String> params = new HashMap<String, String>();
      Map oldParams = item.getParameters();
      Iterator iter = oldParams.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         String[] val = (String[]) oldParams.get(key);
         if (val != null && val.length > 0)
            params.put(key, val[0]);
      }

      if (item.getOriginalTemplateGuid() != null)
      {
         params.put(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(item
               .getOriginalTemplateGuid().getUUID()));
      }

      return params;
   }

   /**
    * Helper to parse all parameters common to all node types.
    * 
    * @param item The assembly workitem, assumed not <code>null</code>.
    * @param params parameter map, assumed not <code>null</code>.
    * 
    * @throws PSMissingBeanConfigurationException
    * @throws PSAssemblyException
    */
   static private Map<String, String> parseCommonParams(IPSAssemblyItem item,
      Map params)
      throws PSAssemblyException, PSMissingBeanConfigurationException
   {
      Map<String, String> id = new HashMap<String, String>();
      // ContentId
      String temp = parseParam(params, IPSHtmlParameters.SYS_CONTENTID, null,
            true);
      id.put(IPSHtmlParameters.SYS_CONTENTID, temp);

      // template name / VariantId
      temp = parseParam(params, IPSHtmlParameters.SYS_TEMPLATE, null, false);
      if (!StringUtils.isEmpty(temp))
      {
         int tid = PSAssemblyServiceLocator.getAssemblyService()
               .findTemplateByName(temp).getGUID().getUUID();
         temp = String.valueOf(tid);
      }
      else
      {
         temp = parseParam(params, IPSHtmlParameters.SYS_VARIANTID, null, true);
      }
      id.put(IPSHtmlParameters.SYS_VARIANTID, temp.toString());

      // Optional params
      // Parent Content Id
      IPSAssemblyItem pItem = item.getCloneParentItem();
      temp = null;
      if (pItem == null)
      {
         temp = parseParam(params,
               IPSHtmlParameters.SYS_CLONEDPARENTID, null, false);
      }
      else
      {
         IPSGuid pid = pItem.getId();
         if (pid instanceof PSLegacyGuid)
         {
            int cid = ((PSLegacyGuid)pid).getContentId();
            temp = String.valueOf(cid);
         }
      }
      if (StringUtils.isNotBlank(temp))
      {
         id.put(PARAM_PARENT_ID, temp);
      }

      // Optional params
      // FolderId
      temp = parseParam(params, IPSHtmlParameters.SYS_FOLDERID, null, false);
      if (!StringUtils.isEmpty(temp))
         id.put(IPSHtmlParameters.SYS_FOLDERID, temp.toString());

      // Params with default values
      // SiteId
      temp = parseParam(params, IPSHtmlParameters.SYS_SITEID, "0", false);
      id.put(IPSHtmlParameters.SYS_SITEID, temp);

      // Context
      temp = parseParam(params, IPSHtmlParameters.SYS_CONTEXT, "0", false);
      id.put(IPSHtmlParameters.SYS_CONTEXT, temp.toString());

      // Authtype
      temp = parseParam(params, IPSHtmlParameters.SYS_AUTHTYPE, "0", false);
      id.put(IPSHtmlParameters.SYS_AUTHTYPE, temp);

      // Content type id get it from component summary
      String cid = (String) params.get(IPSHtmlParameters.SYS_CONTENTID);
      PSComponentSummary sum = getItemSummary(Integer.parseInt(cid));
      id.put(IPSHtmlParameters.SYS_CONTENTTYPEID, String.valueOf(sum
            .getContentTypeId()));
      
      // Checkout status
      String chkstatus = getCheckOutStatusValue(sum);
      id.put(PARAM_CHECKEDOUT_STATUS, chkstatus);

      return id;
   }

   /**
    * Helper method to parse parameter with a given name in the supplied
    * parameter map.
    * 
    * @param params parameter map assumed not <code>null</code> or empty.
    * @param name nameof the parameter to parse, assumed not <code>null</code>
    *           or empty.
    * @param defValue defualt value to be used if the parameter does not exist
    *           in the supplied map, may be <code>null</code> or empty.
    * @param isRequired <code>true</code> if the parameter must have a non
    *           <code>null</code> or non empty value directly or via the
    *           default value.
    * @return parsed parameter, may be <code>null</code> or empty.
    * @throws IllegalArgumentException if the value is required and the value is
    *            resolved to <code>null</code> or empty via parameter map or
    *            defualt value.
    */
   static private String parseParam(Map params, String name, String defValue,
         boolean isRequired)
   {
      String val = defValue;
      Object temp = params.get(name);
      if (temp != null && !StringUtils.isEmpty(temp.toString()))
         val = temp.toString();
      if (val == null && isRequired)
         throw new IllegalArgumentException(name + " must not be null or empty");
      return val;
   }

   /**
    * Gets the nodeType value from the JSON array object.
    * 
    * @return The node type value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getNodeType()
   {
      return getValue(m_indices.get(PARAM_NODE_TYPE).intValue());
   }

   /**
    * Same as {@link #getContentId()} except the id is converted to a guid.
    * 
    * @return A revisionless guid. Will be <code>null</code> if the property
    * does not exist.
    */
   public IPSGuid getContentGuid()
   {
      String cid = getContentId();
      if (cid == null)
         return null;
      
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      return gmgr.makeGuid(new PSLocator(getContentId()));
   }

   /**
    * Gets the sys_contentid value from the JSON array object.
    * 
    * @return The content id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getContentId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_CONTENTID).intValue());
   }

   /**
    * Gets the sys_sortrank value from the JSON array object.
    * 
    * @return The sortrank id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getSortRank()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_SORTRANK));
   }

   /**
    * Gets the sys_variantid value from the JSON array object.
    * 
    * @return The variant id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getVariantId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_VARIANTID).intValue());
   }

   /**
    * Gets the sys_siteid value from the JSON array object.
    * 
    * @return The site id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getSiteId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_SITEID).intValue());
   }

   /**
    * Gets the sys_folderid value from the JSON array object.
    * 
    * @return The folder id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getFolderId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_FOLDERID).intValue());
   }

   /**
    * Gets the sys_context value from the JSON array object.
    * 
    * @return The context value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getContext()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_CONTEXT).intValue());
   }

   /**
    * Gets the sys_authtype value from the JSON array object.
    * 
    * @return The authtype value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getAuthType()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_AUTHTYPE).intValue());
   }

   /**
    * Gets the sys_contenttypeid value from the JSON array object.
    * 
    * @return The contenttype id value. Will be <code>null</code> if the
    *         property does not exist.
    */
   public String getContentTypeId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_CONTENTTYPEID)
            .intValue());
   }

   /**
    * Gets the check out status value from the JSON array object.
    * 
    * @return The check out status value. Will be <code>null</code> if the
    *         property does not exist.
    */
   public String getCheckoutStatus()
   {
      return getValue(m_indices.get(PARAM_CHECKEDOUT_STATUS).intValue());
   }

   /**
    * Gets the sys_slotid value from the JSON array object.
    * 
    * @return The slot id value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getSlotId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_SLOTID).intValue());
   }

   /**
    * Gets the sys_relationshipid value from the JSON array object.
    * 
    * @return The relationship id value. Will be <code>null</code> if the
    *         property does not exist.
    */
   public String getRelationshipId()
   {
      return getValue(m_indices.get(IPSHtmlParameters.SYS_RELATIONSHIPID)
            .intValue());
   }

   /**
    * Gets the field name value from the JSON array object.
    * 
    * @return The field name value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getFieldName()
   {
      return getValue(m_indices.get(PARAM_FIELD_NAME).intValue());
   }
   
   /**
    * Gets the field label value from the JSON array object.
    * 
    * @return The field label value. Will be <code>null</code> if the property
    *         does not exist.
    */
   public String getFieldLabel()
   {
      return getValue(m_indices.get(PARAM_FIELD_LABEL).intValue());
   }

   /**
    * Gets the content id of the parent value from the JSON array object.
    * 
    * @return The content id of the parent value. Will be <code>null</code> if 
    *    the property does not exist.
    */
   public String getParentId()
   {
      return getValue(m_indices.get(PARAM_PARENT_ID).intValue());
   }


   /**
    * Gets the value of the parameter at the given index.
    * 
    * @param index int value of the index of the parameter
    * @return the value at the given index, may be <code>null</code>.
    */
   private String getValue(int index)
   {
      Object obj = null;
      try
      {
         obj = m_objectId.get(index);
      }
      catch (JSONException e)
      {
         // There may not be a value return null.
      }      
      if(obj == null || obj.toString().equals("null"))
        return null;          
      return obj.toString();
   }

   /**
    * JSON array object of the parameters mentioned in the class description.
    * Initialized in the constructor and never <code>null</code> after that.
    */
   private JSONArray m_objectId;

   /**
    * Constant for parameter node type.
    */
   private static final String PARAM_NODE_TYPE = "nodeType";

   /**
    * Constant for parameter checked out by me.
    */
   private static final String PARAM_CHECKEDOUT_STATUS = "checkedOutStatus";

   /**
    * Constant for parameter field name.
    */
   private static final String PARAM_FIELD_NAME = "fieldName";
   
   /**
    * Constant for parameter field label.
    */
   private static final String PARAM_FIELD_LABEL = "fieldLabel";

   /**
    * Constant for parameter of the content id of the parent.
    */
   private static final String PARAM_PARENT_ID = "parentContentId";

   /**
    * Constant value for checkout status indicating the item is not checked 
    * out to any one
    */
   private static final String NOT_CHECKED_OUT = "0";

   /**
    * Constant value for checkout status indicating the item is checked to the
    * login user
    */
   private static final String CHECKED_OUT_BYME = "1";
   
   /**
    * Constant value for checkout status indicating the item is checked to some
    * one other than the login user
    */
   private static final String CHECKED_OUT_BYOTHERS = "2";

   /**
    * Constant map for to maintain the indices of the parameters in the JSON
    * array object
    */
   private static final Map<String, Integer> m_indices = new HashMap<String, Integer>();

   //Static initialization of indices with parameters
   static
   {
      m_indices.put(PARAM_NODE_TYPE, new Integer(0));
      m_indices.put(IPSHtmlParameters.SYS_CONTENTID, new Integer(1));
      m_indices.put(IPSHtmlParameters.SYS_VARIANTID, new Integer(2));
      m_indices.put(IPSHtmlParameters.SYS_SITEID, new Integer(3));
      m_indices.put(IPSHtmlParameters.SYS_FOLDERID, new Integer(4));
      m_indices.put(IPSHtmlParameters.SYS_CONTEXT, new Integer(5));
      m_indices.put(IPSHtmlParameters.SYS_AUTHTYPE, new Integer(6));
      m_indices.put(IPSHtmlParameters.SYS_CONTENTTYPEID, new Integer(7));
      m_indices.put(PARAM_CHECKEDOUT_STATUS, new Integer(8));
      m_indices.put(IPSHtmlParameters.SYS_SLOTID, new Integer(9));
      m_indices.put(IPSHtmlParameters.SYS_RELATIONSHIPID, new Integer(10));
      m_indices.put(PARAM_FIELD_NAME, new Integer(11));
      m_indices.put(PARAM_PARENT_ID, new Integer(12));
      m_indices.put(PARAM_FIELD_LABEL, new Integer(13));
      m_indices.put(IPSHtmlParameters.SYS_SORTRANK, new Integer(14));
   }
   
   //For testing
   public static void main(String[] args)
   {
      String pageJsonArray = "[0,335,500,301,306,0,0,311,1,null,null,null,null]";
      String slotJsonArray = "[1,335,505,301,306,0,0,311,1,518,null,null,null]";
      String snippetJsonArray = "[2,372,503,301,null,0,0,311,0,518,1728,null,null]";
      String fieldJsonArray = "[3,372,503,301,null,0,0,311,0,null,null,displaytitle,null]";
      Map<String,String> jsonArrays = new HashMap<String,String>();
      jsonArrays.put("Page", pageJsonArray);
      jsonArrays.put("Slot", slotJsonArray);
      jsonArrays.put("Snippet", snippetJsonArray);
      jsonArrays.put("Field", fieldJsonArray);
      try
      {
         for(String s:jsonArrays.keySet())
         {
            PSAAObjectId objid = new PSAAObjectId(jsonArrays.get(s));
            System.out.println(s);
            System.out.println(PARAM_NODE_TYPE + " : " + objid.getNodeType());
            System.out.println(IPSHtmlParameters.SYS_CONTENTID + " : "
                  + objid.getContentId());
            System.out.println(IPSHtmlParameters.SYS_VARIANTID + " : "
                  + objid.getVariantId());
            System.out.println(IPSHtmlParameters.SYS_SITEID + " : "
                  + objid.getSiteId());
            System.out.println(IPSHtmlParameters.SYS_FOLDERID + " : "
                  + objid.getFolderId());
            System.out.println(IPSHtmlParameters.SYS_CONTEXT + " : "
                  + objid.getContext());
            System.out.println(IPSHtmlParameters.SYS_AUTHTYPE + " : "
                  + objid.getAuthType());
            System.out.println(IPSHtmlParameters.SYS_CONTENTTYPEID + " : "
                  + objid.getContentTypeId());
            System.out.println(PARAM_CHECKEDOUT_STATUS + " : "
                  + objid.getCheckoutStatus());
            System.out.println(IPSHtmlParameters.SYS_SLOTID + " : "
                  + objid.getSlotId());
            System.out.println(IPSHtmlParameters.SYS_RELATIONSHIPID + " : "
                  + objid.getRelationshipId());
            System.out.println(PARAM_FIELD_NAME + " : " + objid.getFieldName());
            System.out.println(objid.toString());
         }
      }
      catch (JSONException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
   }
  
}
