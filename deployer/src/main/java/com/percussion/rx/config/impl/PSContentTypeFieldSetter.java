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
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.utils.PSContentTypeUtils;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.utils.types.PSPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to set properties for a field in a Content Type.
 *
 * @author YuBingChen
 */
public class PSContentTypeFieldSetter extends PSSimplePropertySetter
{
   /**
    * Default constructor, invoked by Spring framework. This can only used for
    * an existing field. Must call {@link #setFieldName(String)} before calling
    * {@link #applyProperty(Object, String, Object)}.
    */
   public PSContentTypeFieldSetter()
   {
   }
   
   /**
    * Creates an instance of the field setter with the given item definition
    * and field name. This can be used to add a system or shared field to the
    * Content Type. However, this cannot be used to add a local field to the
    * Content Type.
    *  
    * @param itemDef the item definition, may not be <code>null</code>.
    * @param fieldName the field name, which may be an existing field in the
    * item definition or an system/shared field to be added into the item 
    * definition.
    */
   public PSContentTypeFieldSetter(PSItemDefinition itemDef, String fieldName)
   {
      if (!itemDef.isFieldExists(fieldName))
         addField(itemDef, fieldName);
      
      setFieldName(fieldName);
   }
   
   /**
    * Gets the type of the field.
    * 
    * @return the field type.
    */
   public int getFieldType()
   {
      PSDisplayMapping mapping;
      if ((mapping = getSystemFieldMapping(m_fieldName)) != null)
      {
         return PSField.TYPE_SYSTEM;
      }
      else
      {
         mapping = getSharedFieldMapping(m_fieldName);
         return (mapping == null) ? PSField.TYPE_LOCAL : PSField.TYPE_SHARED;
      }
   }
   
   /**
    * Convenience method, calls 
    * {@link #validateFieldAndConfigProperty(PSItemDefinition, String, String) 
    * validateFieldAndConfigProperty(itemDef, m_fieldName, propName)}
    * 
    * @param obj the object in question, not <code>null</code>, must be an
    * instance of {@link PSItemDefinition}.
    * @param propName the name of the property, not blank. 
    */
   private void validateObjAndPropertyName(Object obj, String propName)
   {
      if (!(obj instanceof PSItemDefinition))
         throw new IllegalArgumentException(
               "obj type must be an instance of PSItemDefinition.");
      if (StringUtils.isBlank(m_fieldName))
         throw new IllegalStateException("m_fieldName cannot be null or empty.");
      
      PSItemDefinition itemDef = (PSItemDefinition) obj;
      validateFieldAndConfigProperty(itemDef, m_fieldName, propName);
   }
   
   @Override
   protected boolean applyProperty(Object obj, ObjectState state,
         List<IPSAssociationSet> aSets, String propName, Object propValue)
      throws Exception
   {
      validateObjAndPropertyName(obj, propName);
      
      PSItemDefinition itemDef = (PSItemDefinition) obj;
      if (LABEL.equals(propName))
      {
         setLabel(itemDef, (String)propValue);
      }
      else if (CONTROL_PARAMS.equals(propName))
      {
         setControlParameters(itemDef, propValue);
      }
      else if (DEFAULT_VALUE.equals(propName))
      {
         setDefaultValue(itemDef, propValue);
      }
      else if (SEQUENCE.equals(propName))
      {
         setSequence(itemDef, (String)propValue);
      }
      else if (REQUIRED.equals(propName))
      {
         PSField fd = getField(itemDef);
         setRequired(fd, (String)propValue);
      }
      else
      {
         PSField fd = getField(itemDef);
         super.applyProperty(fd, state, aSets, propName, propValue);
      }
      return true;
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
      
      if (CONTROL_PARAMS.equals(propName))
      {
         if (pvalue instanceof List)
         {
            addFixmePropertyDefsForList(propName, pvalue, defs);
         }
         else if (pvalue instanceof Map)
         {
            PSItemDefinition itemDef = (PSItemDefinition) obj;
            Map<String, Object> srcMap = new HashMap<String, Object>();
            for (PSPair<String, String> pair : getControlParams(itemDef))
               srcMap.put(pair.getFirst(), pair.getSecond());
            addPropertyDefsForMap(propName, pvalue, srcMap, defs);
         }
      }
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @SuppressWarnings({ "unchecked", "cast" })
   @Override
   protected Object getPropertyValue(Object obj, String propName)
   {
      validateObjAndPropertyName(obj, propName);

      PSItemDefinition itemDef = (PSItemDefinition) obj;
      if (LABEL.equals(propName))
      {
         PSUISet uiset = getFieldUISet(itemDef);
         PSDisplayText text = uiset.getLabel();
         return (text == null) ? m_fieldName : text.getText();
      }
      else if (CONTROL_PARAMS.equals(propName))
      {
         return getControlParams(itemDef);
      }
      else if (DEFAULT_VALUE.equals(propName))
      {
         PSField field = getField(itemDef);
         return field.getDefault().getValueText();
      }
      else if (SEQUENCE.equals(propName))
      {
         PSDisplayMapper mapper = getMapper(itemDef);
         PSPair<PSDisplayMapping, Integer> pair = mapper
               .getMappingAndSequence(m_fieldName);
         return pair == null ? null : pair.getSecond();
      }
      else if (REQUIRED.equals(propName))
      {
         PSField fd = getField(itemDef);
         return PSContentTypeUtils.hasRequiredRule(fd) ? Boolean.TRUE
               : Boolean.FALSE;
      }
      
      return super.getPropertyValue(obj, propName);
   }
   

   private List<PSPair<String, String>> getControlParams(
         PSItemDefinition itemDef)
   {
      PSControlRef control = getControl(itemDef).getFirst();
      List<PSPair<String, String>> result = new ArrayList<PSPair<String, String>>();
      List<PSParam> params = (List<PSParam>) IteratorUtils.toList(control
            .getParameters());
      PSPair<String, String> pair;
      for (PSParam p : params)
      {
         pair = new PSPair<String, String>(p.getName(), p.getValue()
               .getValueText());
         result.add(pair);
      }
      return result; 
   }
   
   /**
    * Validates the existence of a parent or simple child field in the given
    * item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the name of the field in question, assumed not
    * <code>null</code> or empty.
    * @param configPropertyName the name of the to be configured property,
    * assumed not <code>null</code> or empty.
    */
   private void validateFieldAndConfigProperty(PSItemDefinition itemDef,
         String fieldName, String configPropertyName)
   {
      if (!itemDef.isFieldExists(fieldName))
         throw new PSConfigException("Cannot find field \"" + fieldName
               + "\" in Content Type \"" + itemDef.getName() + "\".");

      if (itemDef.getPageId(fieldName) > 0)
         throw new PSConfigException("Cannot configure field \"" + fieldName
               + "\" in Content Type \"" + itemDef.getName()
               + "\", because it is not a parent or simple child field.");

      if ("submitName".equalsIgnoreCase(configPropertyName))
         throw new PSConfigException(
               "\"SubmitName\" property is not a configured property for field \""
                     + fieldName + "\" in Content Type \"" + itemDef.getName()
                     + "\".");
   }
   
   /**
    * Validates a field, disallow to set the {@link #REQUIRED} property to
    * <code>false</code> (or removing required rule) for a system/shared field
    * which already has a required rule.
    * 
    * @param field the field in question, assumed not <code>null</code>.
    * @param isRequired new property value for the {@link #REQUIRED} property.
    */
   private void validateRequiredProperty(PSField field, boolean isRequired)
   {
      if (field.getType() == PSField.TYPE_LOCAL)
         return;
      
      if ((!PSContentTypeUtils.hasRequiredRule(field))
            || (PSContentTypeUtils.hasRequiredRule(field) && isRequired))
         return;
      
      throw new PSConfigException("The property \"" + REQUIRED
            + "\" cannot be configured for a "
            + PSField.TYPE_ENUM[field.getType()] + " field \""
            + field.getSubmitName()
            + "\", because this field already has a required rule.");
   }
   
   /**
    * Added a system or shared field into the specified Content Type.
    * 
    * @param itemDef the item definition of the Content Type, assumed not 
    *    <code>null</code>.
    * @param fieldName the field name of the added field, assumed not 
    *    <code>null</code> or empty.
    */
   private void addField(PSItemDefinition itemDef, String fieldName)
   {
      int fd_type = PSField.TYPE_SYSTEM;
      PSDisplayMapping mapping;
      if ((mapping = getSystemFieldMapping(fieldName)) != null)
      {
         fd_type = PSField.TYPE_SYSTEM;
      }
      else
      {
         mapping = getSharedFieldMapping(fieldName);
         if (mapping == null)
         {
            throw new PSConfigException(
                  "Failed to add field \""
                        + fieldName
                        + "\". Because cannot find a system or shared field with such name");
         }
         
         fd_type = PSField.TYPE_SHARED;
         // get rid of the shared group name (from fieldName) if there is one
         fieldName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];
         updateSharedGroup(itemDef, fieldName);
      }

      removeFieldFromExcludes(itemDef, fieldName, fd_type);
      
      // append display mapping with the default label
      PSUISet uiSet = new PSUISet();
      uiSet.setLabel(mapping.getUISet().getLabel());
      PSDisplayMapping theMapping = new PSDisplayMapping(fieldName, uiSet);
      PSUIDefinition uiDef = itemDef.getContentEditorMapper().getUIDefinition();
      uiDef.appendMapping(uiDef.getDisplayMapper(), theMapping);   
   }

   /**
    * Updates the includes and excludes of the shared group for the given 
    * item definition and the shared group. It adds the shared group to both
    * includes and excludes list if the given shared group does not exist in
    * the item definition; otherwise do nothing.
    *  
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the shared field name, assumed not <code>null</code>
    * and it is an existing shared field.
    */
   @SuppressWarnings("unchecked")
   private void updateSharedGroup(PSItemDefinition itemDef, String fieldName)
   {
      PSSharedFieldGroup shGroup = PSContentTypeHelper.getSharedGroup(fieldName);
      PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
      Iterator includes = mapper.getSharedFieldIncludes();
      ArrayList<String> names = new ArrayList<String>();
      CollectionUtils.addAll(names, includes);
      
      if (names.contains(shGroup.getName()))
         return;
      
      // update includes
      names.add(shGroup.getName());
      mapper.setSharedFieldIncludes(names);
      
      // update excludes
      ArrayList<String> excNames = new ArrayList<String>();
      Iterator excludes = mapper.getSharedFieldExcludes();
      CollectionUtils.addAll(excNames, excludes);
      for (PSField f : shGroup.getFieldSet().getAllFields())
         excNames.add(f.getSubmitName());
      
      mapper.setSharedFieldExcludes(excNames);
   }
   
   /**
    * Creates a field and add into the field set in the given item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the name of the created field, assumed not
    * <code>null</code> or empty.
    * @param type it is either {@link PSField#TYPE_SHARED} or
    * {@link PSField#TYPE_SYSTEM}.
    * @param shGroup the shared group, assumed not <code>null</code> if the
    * type is {@link PSField#TYPE_SHARED}; otherwise it may be
    * <code>null</code>.
    * 
    * @return the created field, never <code>null</code>.
    */
   private PSField addFieldToFieldSet(PSItemDefinition itemDef,
         String fieldName, int type, PSSharedFieldGroup shGroup)
   {
      PSField field = new PSField(type, fieldName, null);
      if (type == PSField.TYPE_SYSTEM)
      {
         itemDef.getContentEditorMapper().getFieldSet().add(field);
         return field;
      }
      
      // handle shared field in an existing fieldSet
      PSFieldSet fdSet = itemDef.getContentEditorMapper().getFieldSet(
            shGroup.getName());
      if (fdSet != null)
      {
         fdSet.add(field);
         return field;
      }
      
      // create a shared field set, then add the field
      fdSet = new PSFieldSet(shGroup.getName());
      fdSet.copyFrom(shGroup.getFieldSet());
      fdSet.removeAll();
      
      fdSet.add(field);
      return field;
   }
   
   /**
    * Removes a given field name into the system or shared excludes for the 
    * given item definition. Do nothing if the field name exists in the related 
    * excludes.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the system or shared field name, assumed not
    * <code>null</code> or empty.
    * @param type assumed it is either {@link PSField#TYPE_SYSTEM} or
    * {@link PSField#TYPE_SHARED}
    */
   @SuppressWarnings("unchecked")
   private void removeFieldFromExcludes(PSItemDefinition itemDef,
         String fieldName, int type)
   {
      Iterator excludes;
      ArrayList<String> names = new ArrayList<String>();
      if (type == PSField.TYPE_SYSTEM)
         excludes = itemDef.getContentEditorMapper().getSystemFieldExcludes();
      else
         excludes = itemDef.getContentEditorMapper().getSharedFieldExcludes();
      CollectionUtils.addAll(names, excludes);
      
      // if it exists in case insensitive manner.
      String found_name = null;
      for (String name : names)
      {
         if (name.equalsIgnoreCase(fieldName))
         {
            found_name = name;
            break;
         }
      }
      if (found_name != null)
         names.remove(found_name);
      if (type == PSField.TYPE_SYSTEM)
         itemDef.getContentEditorMapper().setSystemFieldExcludes(names);
      else
         itemDef.getContentEditorMapper().setSharedFieldExcludes(names);
   }

   /**
    * Gets the display mapping of a specified system field.
    * 
    * @param fieldName the field name in question, may be <code>null</code> or
    * empty.
    * 
    * @return the display mapping one of the system field. It may be 
    * <code>null</code> if cannot find a system field with the name.
    */
   private PSDisplayMapping getSystemFieldMapping(String fieldName)
   {
      if (StringUtils.isBlank(fieldName))
         return null;
      
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSContentEditorSystemDef sysDef;
      try
      {
         sysDef = os.getContentEditorSystemDef();
      }
      catch (Exception e)
      {
         throw new PSConfigException(
               "Failed to catalog system field definition.", e);
      }
      
      return sysDef.getUIDefinition().getMapping(fieldName);
         
   }

   private PSDisplayMapping getSharedFieldMapping(String fieldName)
   {
      PSSharedFieldGroup shGroup = PSContentTypeHelper.getSharedGroup(fieldName);
      if (shGroup != null)
      {
         // get rid of the shared group name (from fieldName) if there is one
         fieldName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];
         
         return shGroup.getUIDefinition().getMapping(fieldName);
      }
      return null;
   }
      
   /**
    * Gets the field with the name of {@link #m_fieldName}. The field will be
    * added into the item definition if it does not exist.
    * 
    * @param itemDef the item definition that contains the field, assumed not
    * <code>null</code>.
    * 
    * @return the field with the name, never <code>null</code> or empty.
    */
   private PSField getField(PSItemDefinition itemDef)
   {
      if (StringUtils.isBlank(m_fieldName))
         throw new IllegalStateException(
               "m_fieldName must not be null or empty.");
         
      PSField fd = itemDef.getFieldByName(m_fieldName);

      if (fd != null)
         return fd;
      
      int type;
      PSSharedFieldGroup shGroup = null;
      if (getSystemFieldMapping(m_fieldName) != null)
      {
         type = PSField.TYPE_SYSTEM;
      }
      else
      {
         shGroup = PSContentTypeHelper.getSharedGroup(m_fieldName);
         if (shGroup == null)
         {
            throw new IllegalStateException("The field name \"" + m_fieldName
                  + "\" is not a system or shared field in Content Type \""
                  + itemDef.getName() + "\"");
         }
         
         type = PSField.TYPE_SHARED;
      }
      
      return addFieldToFieldSet(itemDef, m_fieldName, type, shGroup);
   }

   /**
    * Sets the field name property for the current setter.
    * 
    * @param fieldName field name, never <code>null</code> or empty.
    */
   public void setFieldName(String fieldName)
   {
      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException("fieldName must not be null or empty.");
      
      // get rid of the shared group name (from fieldName) if there is one
      m_fieldName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];
   }
   
   /**
    * Set the label property for the field in the specified item def.
    * 
    * @param itemDef the item definition that contains a field with
    * the name of {@link #m_fieldName}
    * @param value the new label, may be <code>null</code> or empty.
    */
   private void setLabel(PSItemDefinition itemDef, String value)
   {
      PSUISet uiset = getFieldUISet(itemDef);
      String label = value == null ? "" : value;
      PSDisplayText text = new PSDisplayText(label);
      uiset.setLabel(text);
   }

   /**
    * Gets the UISet of the current field. The field may be a parent or 
    * simple child field.
    * 
    * @param itemDef the item definition that contains the field, assumed
    * not <code>null</code>.
    * 
    * @return the UISet of the current field, never <code>null</code>.
    */
   private PSUISet getFieldUISet(PSItemDefinition itemDef)
   {
      PSFieldSet fset = itemDef.getSimpleChildSet(m_fieldName);
      String uiSetName = fset == null ? m_fieldName : fset.getName();      
      PSUISet uiset = itemDef.getContentEditor().getFieldUiSet(uiSetName);
      if (uiset == null)
         throw new PSConfigException("Cannot find UISet for field \""
               + m_fieldName + "\"");
      return uiset;
   }

   /**
    * Gets the control and UI set pairs for the specified item definition.
    * 
    * @param itemDef the item definition in question, assumed not 
    * <code>null</code>.
    * 
    * @return the 1st value is the control and the 2nd value is the UI set, 
    * never <code>null</code>.
    */
   private PSPair<PSControlRef, PSUISet> getControl(PSItemDefinition itemDef)
   {
      PSUISet uiSet = getFieldUISet(itemDef);
      PSControlRef control = uiSet.getControl();
      if (control == null) // it must be a system/shared field
         control = getControlRef(m_fieldName);      
      
      return new PSPair<PSControlRef, PSUISet>(control, uiSet);
   }
   
   /**
    * Sets the {@link #CONTROL_PARAMS} property on the current field.
    * 
    * @param itemDef the item definition that contains the current field.
    * Assumed not <code>null</code>.
    * @param params the value of the property. Expecting {@link Map} or
    * {@link List} type.
    */
   @SuppressWarnings("unchecked")
   private void setControlParameters(PSItemDefinition itemDef, Object params)
   {
      if ((!(params instanceof Map)) && (!(params instanceof List)))
         throw new PSConfigException("The value of \"" + CONTROL_PARAMS
               + "\" property must be a \"Map\" or \"List\" type");

      PSPair<PSControlRef, PSUISet> pair = getControl(itemDef); 
      PSControlRef control = pair.getFirst();
      
      PSCollection parameters = new PSCollection( PSParam.class );
      
      if (params instanceof Map)
      {
         Map<String, String> paramMap = (Map<String, String>) params;
         for (Map.Entry<String, String> entry : paramMap.entrySet())
         {
            PSTextLiteral value = new PSTextLiteral(entry.getValue());
            PSParam p = new PSParam(entry.getKey(), value);
            parameters.add(p);
         }
      }
      else
      {
         List<PSPair<String, String>> pairList = (List<PSPair<String, String>>) params;
         for (PSPair<String, String> entry : pairList)
         {
            PSTextLiteral value = new PSTextLiteral(entry.getSecond());
            PSParam p = new PSParam(entry.getFirst(), value);
            parameters.add(p);
         }         
      }
      control.setParameters(parameters);
      PSUISet uiSet = pair.getSecond();
      uiSet.setControl(control);
   }

   /**
    * Gets a control reference for a system or shared field.
    * 
    * @param fieldName the name of the field in question, assumed not 
    * <code>null</code> or empty.
    * 
    * @return the control reference, never <code>null</code>.
    */
   private PSControlRef getControlRef(String fieldName)
   {
      PSDisplayMapping mapping = getSystemFieldMapping(fieldName);
      if (mapping == null)
      {
         mapping = getSharedFieldMapping(fieldName);
         if (mapping == null)
         {
            throw new PSConfigException(
                  "Failed to find a system or shared field \"" + fieldName
                        + "\".");
         }
      }
      PSUISet uiSet = mapping.getUISet();
      return new PSControlRef(uiSet.getControl().getName());
   }
   
   /**
    * Sets the {@link #DEFAULT_VALUE} property.
    * 
    * @param itemDef the item definition that contains the current field.
    * Assumed not <code>null</code>.
    * @param value the value of the property. It may be <code>null</code> or
    * empty.
    */
   private void setDefaultValue(PSItemDefinition itemDef, Object value)
   {
      String text = value == null ? "" : value.toString();
      PSField field = getField(itemDef);
      PSTextLiteral defaultValue = new PSTextLiteral(text);
      field.setDefault(defaultValue);
   }
   
   /**
    * Sets the {@link #REQUIRED} property
    * @param fd the field in question, assumed not <code>null</code>.
    * @param value the new value of the property.
    * @throws PSExtensionException 
    */
   @SuppressWarnings("unchecked")
   private void setRequired(PSField fd, String value)
      throws PSExtensionException
   {
      // convert the boolean value
      Boolean isRequired = (Boolean) super.convertValue(value, Boolean.class);
      
      validateRequiredProperty(fd, isRequired); 
      
      // get all extension the implemented IPSFieldValidator
      IPSExtensionManager mgr = PSServer.getExtensionManager(null);
      Iterator it = mgr.getExtensionNames(null, null,
            com.percussion.extension.IPSFieldValidator.class.getName(), null);
      List<PSExtensionRef> extensions = new ArrayList<PSExtensionRef>();
      CollectionUtils.addAll(extensions, it);
      
      // set the property
      PSContentTypeUtils.setFieldRequiredRule(fd, isRequired, extensions);
   }

   /**
    * Gets the display mapper for the specified item definition.
    * 
    * @param itemDef the item definition in question, assumed not 
    *    <code>null</code>.
    * 
    * @return the mapper, never <code>null</code>.
    */
   private PSDisplayMapper getMapper(PSItemDefinition itemDef)
   {
      PSUIDefinition uiDef = itemDef.getContentEditorMapper().getUIDefinition();
      return uiDef.getDisplayMapper();
   }
   
   /**
    * Reposition the current field according to the {@link #SEQUENCE}
    * property value.
    * 
    * @param itemDef the Content Type contains the current field,
    * assumed not <code>null</code>.
    * @param seqValue the sequence value in String format, may not be
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   private void setSequence(PSItemDefinition itemDef, String seqValue)
   {
      if (StringUtils.isBlank(seqValue))
         throw new IllegalArgumentException("seqVaule may not be null or empty.");
      
      int sequence = Integer.parseInt(seqValue);
     // reposition the display mapping of the field according to "sequence"
      PSDisplayMapper mapper = getMapper(itemDef);

      // simple child uses its FieldSet name for its top level mapping
      PSFieldSet fset = itemDef.getSimpleChildSet(m_fieldName);
      String mappingName = fset == null ? m_fieldName : fset.getName();      

      PSDisplayMapping mapping = mapper.getMapping(mappingName);
      mapper.remove(mapping);

      if (sequence < 0)
         sequence = 0;
      else if (sequence > mapper.size())
         sequence = mapper.size();
 
      mapper.insertElementAt(mapping, sequence);
   }

   /**
    * The field name
    */
   private String m_fieldName = null;
   
   /**
    * The "is required" property name on a field. This property is not 
    * configurable for a system or shared field.
    */
   public static final String REQUIRED = "required";

   /**
    * The property name for a list of control parameters. The supported 
    * parameter type is text ({@link PSTextLiteral}) only.
    */
   public static final String CONTROL_PARAMS = "controlParameters";

   /**
    * The "Default value" property name. The type of the default value is
    * text ({@link PSTextLiteral}) only.
    */
   public static final String DEFAULT_VALUE = "default";

   /**
    * The label property name on a field
    */
   public static final String LABEL = "label";
   
   /**
    * The name of sequence property, which is used to control the
    * location of the field on the Content Type.
    */
   public static final String SEQUENCE = "sequence";
   
   /**
    * A list of configurable property names for a system or shared field.
    */
   public static final String[] SYS_SH_PROPERTIES = new String[] { LABEL,
      CONTROL_PARAMS, DEFAULT_VALUE, "showInPreview", "userSearchable" };
}
