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
package com.percussion.rx.config.impl;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class is responsible to set properties for a Content Type.
 *
 * @author YuBingChen
 */
public class PSContentTypeSetter extends PSSimplePropertySetter
{

   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof PSItemDefinition))
         throw new IllegalArgumentException(
               "obj type must be PSItemDefinition.");

      PSItemDefinition itemDef = (PSItemDefinition) obj;
      if (DEFAULT_WORKFLOW.equals(propName))
      {
         setDefaultWorkflow(itemDef, (String) propValue);
      }
      else if (ICON_VALUE.equals(propName))
      {
         setIconValue(itemDef, (String) propValue);
      }
      else if (TEMPLATES.equals(propName))
      {
         setListAssociation(aSets,
               IPSAssociationSet.AssociationType.CONTENTTYPE_TEMPLATE,
               propValue);
      }
      else if (WORKFLOWS.equals(propName))
      {
         setListAssociation(aSets,
               IPSAssociationSet.AssociationType.CONTENTTYPE_WORKFLOW,
               propValue);
      }
      else if (FIELDS.equals(propName))
      {
         setFields(itemDef, state, propValue);
      }
      else
      {
         super.applyProperty(obj, state, aSets, propName, propValue);
      }
      
      return true;
   }

   @Override
   protected boolean deApplyProperty(Object obj,
         @SuppressWarnings("unused")
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      if (!(obj instanceof PSItemDefinition))
         throw new IllegalArgumentException(
               "obj type must be PSItemDefinition.");

      PSItemDefinition itemDef = (PSItemDefinition) obj;
      if (FIELDS.equals(propName))
      {
         Map<String, Map<String, Object>> fieldMap = convertObjectToMap(propValue);
         if (fieldMap.isEmpty())
            return false;
         deleteShareSystemFields(itemDef, fieldMap.keySet());
         return true;
      }
      return false;
   }
   
   /**
    * Gets the name of the specified template.
    * 
    * @param id the ID of the template, assumed not <code>null</code>.
    * 
    * @return the name of the template, it may be <code>null</code>.
    */
   private String getTemplateName(IPSGuid id)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate template = service.findTemplate(id);
      
      return template == null ? null : template.getName();
   }

   /*
    * //see base class method for details
    */
   @Override
   protected boolean addPropertyDefs(Object obj, String propName,
         Object pvalue, Map<String, Object> defs)
   {
      if (super.addPropertyDefs(obj, propName, pvalue, defs))
         return true;
      
      if (TEMPLATES.equals(propName) || WORKFLOWS.equals(propName))
      {
         addFixmePropertyDefsForList(propName, pvalue, defs);
      }
      else if (FIELDS.equals(propName))
      {
         addPropertyDefsForMap(propName, pvalue, null, defs);
      }
      return true;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      if (!(obj instanceof PSItemDefinition))
         throw new IllegalArgumentException(
               "obj type must be PSItemDefinition.");

      PSItemDefinition itemDef = (PSItemDefinition) obj;
      if (DEFAULT_WORKFLOW.equals(propName))
      {
         IPSWorkflowService wfSrv = PSWorkflowServiceLocator.getWorkflowService();
         IPSGuid id = new PSGuid(PSTypeEnum.WORKFLOW, itemDef.getWorkflowId());
         PSWorkflow wkflow = wfSrv.loadWorkflow(id);
         return wkflow.getName();
      }
      else if (ICON_VALUE.equals(propName))
      {
         String iconValue = itemDef.getContentEditor().getIconValue();
         if (StringUtils.isBlank(iconValue))
            return "";
         else
            return iconValue;
      }
      else if (TEMPLATES.equals(propName))
      {
         PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(itemDef
               .getGuid());
         if (nodeDef == null)
            return null;
         List<String> names = new ArrayList<String>();
         for (IPSGuid id : nodeDef.getVariantGuids())
         {
            String name = getTemplateName(id);
            if (name != null)
               names.add(name);
         }
         return names;
      }
      else if (WORKFLOWS.equals(propName))
      {
         PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(itemDef
               .getGuid());
         if (nodeDef == null)
            return null;
         List<String> names = new ArrayList<String>();
         for (IPSGuid id : nodeDef.getWorkflowGuids())
         {
            IPSWorkflowService wfSrv = PSWorkflowServiceLocator.getWorkflowService();
            PSWorkflow wkflow = wfSrv.loadWorkflow(id);
            names.add(wkflow.getName());
         }
         return names;
      }
      else if (FIELDS.equals(propName))
      {
         List<PSField> fields = itemDef.getMappedParentFields();
         if (fields.isEmpty())
            return Collections.emptyMap();

         // return the 1st (parent) field
         Map<String, Object> resultMap = new HashMap<String, Object>();
         Map<String, String> fieldMap = new HashMap<String, String>();
         PSField fd = fields.get(0);
         fieldMap.put(PSContentTypeFieldSetter.SEQUENCE, "0");
         resultMap.put(fd.getSubmitName(), fieldMap);
         
         return resultMap;
      }
      
      return super.getPropertyValue(obj, propName);
   }
   
   /**
    * Processing the {@link #FIELDS} property.
    * 
    * @param itemDef the Content Type, assumed not <code>null</code>.
    * @param fields the value of {@link #FIELDS} property, it may be 
    * <code>null</code>, which is treated as empty map.
    */
   @SuppressWarnings("unchecked")
   private void setFields(PSItemDefinition itemDef, ObjectState state,
         Object fields)
   {
      Map<String, Map<String, Object>> fieldMap = convertObjectToMap(fields);
      Collection<String> prevOnlyNames = getPrevFieldNames(fieldMap.keySet());
      deleteShareSystemFields(itemDef, prevOnlyNames);
      
      for (Map.Entry<String, Map<String, Object>> entry : fieldMap.entrySet())
      {
         String fieldName = entry.getKey();
         Map<String, Object> properties = entry.getValue();
        
         PSContentTypeFieldSetter fsetter = new PSContentTypeFieldSetter(itemDef, fieldName);
         fsetter.setProperties(properties);
         fsetter.applyProperties(itemDef, state, null);
      }
   }

   /**
    * Deletes the specified fields
    * 
    * @param itemDef the item definition that contains the fields, assumed not 
    * <code>null</code>.
    * @param fnames the collection of field names, assumed not <code>null</code>
    * but may be empty.
    */
   private void deleteShareSystemFields(PSItemDefinition itemDef, Collection<String> fnames)
   {
      PSContentTypeFieldSetter fsetter;
      for (String name : fnames)
      {
         if (!itemDef.isFieldExists(name))
            continue;
         fsetter = new PSContentTypeFieldSetter(itemDef, name);
         removeField(itemDef, name, fsetter.getFieldType());
      }
   }
   
   
   /**
    * Removes a field from a given item definition
    * 
    * @param itemDef the item definition that may contain the field, assumed not
    * <code>null</code>.
    * @param fieldName the name of the field, assumed not <code>null</code> or
    * empty.
    * @param type it is either {@link PSField#TYPE_SHARED} or
    * {@link PSField#TYPE_SYSTEM}
    */
   private void removeField(PSItemDefinition itemDef, String fieldName,
         int type)
   {
      if (type == PSField.TYPE_LOCAL)
         return;
      
      // get rid of the shared group name (from fieldName) if there is one
      String justName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];
      
      if (!itemDef.isFieldExists(justName))
         return;

      PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
      if (type == PSField.TYPE_SYSTEM)
      {
         itemDef.getContentEditorMapper().getFieldSet().remove(justName);
      }
      else
      {
         PSSharedFieldGroup shGroup = PSContentTypeHelper
               .getSharedGroup(fieldName);
         PSFieldSet fdSet = mapper.getFieldSet(shGroup.getName());
         if (fdSet != null)
            fdSet.remove(justName);
      }
      itemDef.getContentEditorMapper().getUIDefinition().getDisplayMapper()
            .removeMapping(justName);
      addFieldToExcludes(itemDef, justName, type);
   }
   
   /**
    * Adds a field name to the exclude list of an item definition.
    * 
    * @param itemDef the item definition that contains the field. Assumed not
    * <code>null</code>.
    * @param fieldName the name of the field, assumed not <code>null</code> or
    * empty.
    * @param type the type of the field, assumed returned by PSField.getType().
    */
   @SuppressWarnings("unchecked")
   private void addFieldToExcludes(PSItemDefinition itemDef,
         String fieldName, int type)
   {
      if (type == PSField.TYPE_SYSTEM)
      {
         ArrayList<String> names = new ArrayList<String>();
         Iterator excludes;
         excludes = itemDef.getContentEditorMapper().getSystemFieldExcludes();
         CollectionUtils.addAll(names, excludes);         
         names.add(fieldName);
         itemDef.getContentEditorMapper().setSystemFieldExcludes(names);
      }
      else
      {
         addFieldToSharedExcludes(itemDef, fieldName);
      }
   }

   /**
    * Adds a shared field to the shared includes into the given item definition.
    * However, if all fields of the shared group are in the excludes, then
    * remove the shared group and its names from the item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the name of the shared field in question, assumed not
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   private void addFieldToSharedExcludes(PSItemDefinition itemDef,
         String fieldName)
   {
      ArrayList<String> names = iteratorToList(itemDef.getContentEditorMapper()
            .getSharedFieldExcludes());
      names.add(fieldName);
      
      PSSharedFieldGroup shGrp = PSContentTypeHelper.getSharedGroup(fieldName);
      if (containAllSharedFields(shGrp, names))
         removeSharedGroup(itemDef, shGrp);
      else
         itemDef.getContentEditorMapper().setSharedFieldExcludes(names);
   }
   
   /**
    * Determines if the specified field names are the complete set of
    * a given shared group.
    * 
    * @param shGrp the shared group in question, assumed not <code>null</code>.
    * @param names the field names in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the field names are the complete set of
    * the specified shared group.
    */
   private boolean containAllSharedFields(PSSharedFieldGroup shGrp,
         List<String> names)
   {
      List<String> shNames = iteratorToList(shGrp.getFieldSet().getNames());
      return names.containsAll(shNames);
   }

   /**
    * Removes a shared group (name and its field) from the given item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param shGrp the to be removed shared group, assumed not <code>null</code>.
    */
   private void removeSharedGroup(PSItemDefinition itemDef,
         PSSharedFieldGroup shGrp)
   {
      PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
      
      // remove all field names
      ArrayList<String> shNames = iteratorToList(shGrp.getFieldSet().getNames());
      ArrayList<String> excludes = iteratorToList(mapper
            .getSharedFieldExcludes());
      excludes.removeAll(shNames);
      mapper.setSharedFieldExcludes(excludes);
      
      // remove the shared group 
      ArrayList<String> shGrpNames = iteratorToList(mapper
            .getSharedFieldIncludes());
      shGrpNames.remove(shGrp.getName());
      mapper.setSharedFieldIncludes(shGrpNames);
   }

   /**
    * Converts a Iterator to a list.
    * 
    * @param it the iterator, assumed not <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   private ArrayList<String> iteratorToList(Iterator it)
   {
      ArrayList<String> list = new ArrayList<String>();
      CollectionUtils.addAll(list, it);

      return list;
   }

   /**
    * Gets the field names that exist only in previous applied properties,
    * but not in current fields.
    * 
    * @param curNames current field names, assumed not <code>null</code>,
    * may be empty.
    * 
    * @return a collection of field names, never <code>null</code>, may be
    * empty.
    */
   private Collection<String> getPrevFieldNames(Collection<String> curNames)
   {
      Map<String, Object> props = getPrevProperties();
      if (props == null || props.isEmpty())
         return Collections.emptyList();
      
      Collection<String> prevNames = convertObjectToMap(props.get(FIELDS)).keySet();
      prevNames.removeAll(curNames);
      return prevNames;
   }
   
   /**
    * Converts a specified object to a field map.
    * 
    * @param fields the value of the {@link #FIELDS} property, it may be
    * <code>null</code> or empty.
    * 
    * @return the field map, never <code>null</code>, may be empty if there is
    * no previous only field.
    */
   @SuppressWarnings("unchecked")
   private Map<String, Map<String, Object>> convertObjectToMap(Object fields)
   {
      if (fields == null)
         return Collections.emptyMap();
      
      if (!(fields instanceof Map))
         throw new PSConfigException("The type of \"" + FIELDS
               + "\" property (in PSContentTypeSetter) must be Map");
      
      return (Map<String, Map<String, Object>>) fields;
   }
   
   /**
    * Sets the icon value property for the given Content Type.
    * 
    * @param itemDef the Content Type, assumed not <code>null</code>.
    * @param value the icon value, may not be <code>null</code> or empty.
    */
   private void setIconValue(PSItemDefinition itemDef, String value)
   {
      if (StringUtils.isBlank(value))
      {
         itemDef.getContentEditor().setContentTypeIcon(
               PSContentEditor.ICON_SOURCE_NONE, "");
         return;
      }
      
      // is there a field name that matches the value
      if (itemDef.isFieldExists(value))
      {
         itemDef.getContentEditor().setContentTypeIcon(
               PSContentEditor.ICON_SOURCE_FROMFILEEXT, value);
      }
      else
      {
         itemDef.getContentEditor().setContentTypeIcon(
               PSContentEditor.ICON_SOURCE_SPECIFIED, value);
      }
   }
   
   /**
    * Sets the default workflow for the given Content Type.
    * 
    * @param itemDef the Content Type, assumed not <code>null</code>.
    * @param wfName the name of the default workflow, may not be
    * <code>null</code> or empty.
    */
   private void setDefaultWorkflow(PSItemDefinition itemDef, String wfName)
   {
      if (StringUtils.isBlank(wfName))
         throw new IllegalArgumentException(
               "Default Workflow name must not be null or empty.");
      
      IPSWorkflowService wfSrv = PSWorkflowServiceLocator.getWorkflowService();
      List<PSObjectSummary> sum = wfSrv.findWorkflowSummariesByName(wfName);
      
      if (sum.isEmpty())
         throw new PSConfigException("Cannot find Workflow name \"" + wfName
               + "\"");
      
      int workflowId = sum.get(0).getGUID().getUUID();
      itemDef.getContentEditor().setWorkflowId(workflowId);
   }

   /**
    * The property name for a list of to be added or existing fields and its
    * properties. The value of the property is a
    * {@link Map Map&lt;String, Map&lt;String,String>>}.
    */
   public static final String FIELDS = "fields";
   

   /**
    * The property name for default workflow.
    */
   public static final String DEFAULT_WORKFLOW = "defaultWorkflow";
   
   /**
    * The property name for icon value. This will set both "iconValue" and
    * "iconSource" properties on the Content Type {@link PSContentEditor}.
    */
   public static final String ICON_VALUE = "iconValue";
   
   /**
    * The property name for a list of templates that are associated with the
    * Content Type.
    */
   public static final String TEMPLATES = "templates";
   
   /**
    * The property name for a list of workflows that are associated with the
    * Content Type.
    */
   public static final String WORKFLOWS = "workflows";
}
