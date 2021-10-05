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

package com.percussion.cms.objectstore;

import com.percussion.content.PSContentFactory;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSValidationContext;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCollection;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class PSItemDefinition extends PSItemDefSummary implements IPSComponent,
   IPSCloneTuner
{
   /**
    * Create a new definition.
    * 
    * @param appName The owning application. Never <code>null</code> or empty.
    * @param typeDef The type info from the db. Never <code>null</code>. 
    * @param editorDef The type info from the application xml. Never 
    *    <code>null</code>.
    */
   public PSItemDefinition(String appName, PSContentType typeDef, 
         PSContentEditor editorDef)
   {
      super(typeDef.getName(), typeDef.getLabel(), typeDef.getTypeId(),
         typeDef.getEditorUrl(), typeDef.getDescription());

      if (null == appName || appName.trim().length() == 0)
         throw new IllegalArgumentException(
               "Application name cannot be null or empty.");

      if (null == editorDef)
         throw new IllegalArgumentException("editorDef cannot be null");

      m_isHidden = typeDef.isHiddenFromMenu();
      m_contentEditorDef = editorDef;
      m_appName = appName;
      m_objectType = typeDef.getObjectType();
      m_id = typeDef.getTypeId();
   }

   /**
    * Creates a key representing the PSContentType object that would be 
    * associated with this definition.
    *  
    * @return Never <code>null</code>.
    */
   public PSKey getContentEditorKey()
   {
      return PSContentType.createKey(getTypeId()); 
   }

   /**
    * @param    contentTypeDef
    */
   public PSItemDefinition(Element contentTypeDef)
      throws PSUnknownNodeTypeException
   {
      fromXml(contentTypeDef, null, null);
   }
   
   /**
    * @param    contentTypeDef The content type definition.
    * @param    isCopy <code>true</code> if the item definition is being
    * constructed as a copy of an existing definition, <code>false</code>
    * otherwise.
    */
   public PSItemDefinition(Element contentTypeDef, boolean isCopy)
      throws PSUnknownNodeTypeException
   {
      fromXml(contentTypeDef, null, null, isCopy);
   }
   
   /**
    * Ctor to be used by subclasses
    */
   protected PSItemDefinition()
   {
      
   }

   /**
    * The application that contains the content editor definition.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getAppName()
   {
      return m_appName;
   }
   
   /**
    * An enabled editor is visible to end users and participates in searches,
    * a disabled editor does not. Defaults to enabled.
    * <p>
    * Impl note: This property is the enabled property of the associated
    * application.
    */
   public boolean isEnabled()
   {
      return m_enabled;
   }
   
   /**
    * Sets the enabled property. See {@link #isEnabled()} for a description.
    * 
    * @param enabled The new state.
    */
   public void setEnabled(boolean enabled)
   {
      m_enabled = enabled;
   }
   
   /**
    * Get the resource name used to call internal request handlers.
    * 
    * @return the request resource name, never <code>null</code> or empty.
    */
   public String getInternalRequestResource()
   {
      return getAppName() + "/" + getContentEditor().getName();
   }

   /**
    * Get the object type of the content type.
    * 
    * @return The object type.
    */
   public int getObjectType()
   {
      return m_objectType;
   }
   
   /**
    * Returns the definition for this content type.
    *
    * @return the definition of this content type.  Will not be
    * <code>null</code>.
    */
   public PSContentEditor getContentEditor()
   {
      return m_contentEditorDef;
   }
   
   /**
    * Get the list of child display mappers.
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   public List<PSDisplayMapper> getChildMappers()
   {
      List<PSDisplayMapper> childMappers = new ArrayList<>();
      
      PSContentEditorMapper mapper = getContentEditorMapper();
      PSUIDefinition def = mapper.getUIDefinition();
      PSDisplayMapper parent = def.getDisplayMapper();
      Iterator mappings = parent.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
            childMappers.add(childMapper);
      }
      
      return childMappers;
   }

   /**
    * Convenience method that gets the PSField objects associated with 
    * the specified complex child. Only those items that have a mapping are
    * returned.
    * <p>This list also includes all fields that display with the child but
    * whose fields are not directly in the associated fieldset, such 
    * as simple children and SDMP (single data, multi-property) fieldsets.
    * 
    * @param childMapperId The id of the mapper of the complex child of 
    * interest.
    * 
    * @return An iterator over 1 or more PSFields, in document (display) 
    * order. If no mapper matching the supplied id is found, <code>null</code>
    * is returned.
    */
   public Iterator<PSField> getChildFields(int childMapperId)
   {
      PSContentEditorMapper mapper = getContentEditorMapper();
      PSUIDefinition def = mapper.getUIDefinition();
      PSDisplayMapper dmapper = def.getDisplayMapper(childMapperId);
      PSFieldSet fieldSet = mapper.getFieldSet();
      fieldSet = (PSFieldSet) fieldSet.get(dmapper.getFieldSetRef());
      List<PSField> result = null;
      if (null != dmapper)
      {
         result = getFields(fieldSet, dmapper);
      }
      return null == result ? null : result.iterator();
   }

   /**
    * Walks the supplied mapper and gets the matching field definitions. The 
    * result includes fields directly in the related fieldset and children of
    * simple and SDMP field sets.
    * 
    * @param fieldSet The field set associated with the supplied mapper. Either
    * the editor's main field set (if the mapper is the main mapper), or a 
    * complex child. Assumed not <code>null</code>.
    * 
    * @param mapper The display definition to walk. For each entry in the 
    * mapper, the corresponding PSField is added to the returned list.
    * 
    * @return 1 or more PSField objects, in document order.
    */
   private List<PSField> getFields(PSFieldSet fieldSet, PSDisplayMapper mapper)
   {
      List<PSField> fields = new ArrayList<>();
      Iterator mappings = mapper.iterator();
      while(mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldName = mapping.getFieldRef();
      
         Object o = fieldSet.get(fieldName);
      
         /**
          * If the field reference is not found in this fieldset, then check
          * whether it is multiproperty simple child field
          */
         if(o == null)
         {
            o = fieldSet.getChildField(fieldName,
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
         }
      
         /* If field reference is field set, then it might be simplechild or
          * complexchild. In case of simple child, we have to show the mapping
          * in parent mapper only, so get the field reference from it's mapper
          * and get the field.
          */
         if(o instanceof PSFieldSet)
         {
            PSFieldSet childFs = (PSFieldSet)o;
            if(childFs.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
            {
               PSDisplayMapper childMapper = mapping.getDisplayMapper();
               Iterator childMappings = childMapper.iterator();
               while(childMappings.hasNext())
               {
                  PSDisplayMapping childMapping =
                     (PSDisplayMapping) childMappings.next();
                  fieldName = childMapping.getFieldRef();
                  o = fieldSet.getChildField(fieldName,
                     PSFieldSet.TYPE_SIMPLE_CHILD);
               }
            }
            else
               //don't recurse into complex child field sets
               continue;
         } 
         
         if (o instanceof PSField)
         {
            fields.add((PSField) o);
         }
         else if (o == null)
         {
            throw new RuntimeException("Field " + fieldName 
                  + " referenced by mapping not found.");
         }
         else
         {
            throw new RuntimeException("Field " + fieldName 
                  + " referenced by mapping not found." 
                  + " Object of class " + o.getClass() 
                  + " found instead of PSField");
         }
      }
      return fields;
   }

   /**
    * Convenience method that gets the PSField objects associated with 
    * the main item. Only those items that have a mapping. It does NOT include
    * the field that does not have UI mapping. Use {@link #getParentFields()} 
    * to get ALL fields. This method can be used remotely.
    * <p>This list also includes all fields that display with the parent but
    * whose display mappings are not directly in the parent's fieldset, such 
    * as simple children and SDMP (single data, multi-property) fieldsets.
    * 
    * @return 1 or more <code>PSField</code> objects, in document order.
    */
   public List<PSField> getMappedParentFields()
   {
      PSContentEditorMapper mapper = getContentEditorMapper();
      PSFieldSet fieldSet = mapper.getFieldSet();
      PSUIDefinition def = mapper.getUIDefinition();
      PSDisplayMapper dmapper = def.getDisplayMapper();
      List<PSField> fields = getFields(fieldSet, dmapper);
      return fields;
   }

   /**
    * This method returns all user visible fields present in the parent editor
    * that store data as a single value (not as arrays or tables.) and storage
    * type as text.
    * 
    * @return list of field names, never <code>null</code>.
    */
   public List<String> getSingleDimensionParentTextFieldNames()
   {
      List<String> fn = new ArrayList<>();
      PSContentEditorMapper mapper = getContentEditorMapper();
      Iterator iter = mapper.getUIDefinition().getDisplayMapper().iterator();
      PSFieldSet fieldSet = mapper.getFieldSet();
      while(iter.hasNext())
      {
         PSDisplayMapping dm = (PSDisplayMapping) iter.next();
         if(dm.getDisplayMapper() == null)
         {
            String fieldName = dm.getFieldRef();
            PSField fld = fieldSet.getFieldByName(fieldName);
            if(fld!=null && fld.getDataType().equals(PSField.DT_TEXT))
               fn.add(dm.getFieldRef());
         }
      }
      return fn;
   }

   /**
    * Convenient method to get the content editor mapper.
    * 
    * @return Content editor mapper returned by
    *         <code>PSContentEditorPipe#getMapper</code>
    */
   public PSContentEditorMapper getContentEditorMapper()
   {
      PSContentEditorPipe pipe = (PSContentEditorPipe) m_contentEditorDef
            .getPipe();
      if (null == pipe)
      {
         /*
          * Because this is an unexpected condition for the way the system is
          * typically used, I'm just throwing a runtime exception.
          */
         throw new RuntimeException("Missing pipe on content editor.");
      }
      return pipe.getMapper();

   }
   
   /**
    * Convenience method that gets the PSField objects associated with 
    * the main item. Only those items that have a mapping or are system fields
    * stored in the db are returned.
    * <p>This list also includes all fields that display with the parent but
    * whose display mappings are not directly in the parent's fieldset, such 
    * as simple children and SDMP (single data, multi-property) fieldsets.
    * <p>This cannot be used remotely.
    * 
    * @return An iterator over 1 or more PSFields, in document (display) 
    * order.
    */
   public Iterator<PSField> getParentFields()
   {
      List<PSField> fields = getMappedParentFields();
      Collection<PSField> unmappedFields = getUnmappedSystemDbFields();
      fields.addAll(unmappedFields);
      return fields.iterator();
   }

   /**
    * Convenience method that calls {@link PSFieldSet#getFieldByName(String)
    * getFieldByName(fieldName)} on the parent field set of the associated 
    * content editor.
    */
   public PSField getFieldByName(String fieldName)
   {
      PSContentEditorMapper mapper = getContentEditorMapper();
      PSFieldSet fieldSet = mapper.getFieldSet();
      return fieldSet.getFieldByName(fieldName);
   }
   
   /**
    * Determines if a (parent or child) field exists in the item definition.
    * 
    * @param fieldName the field name in question, never <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if the field exists; otherwise return
    * <code>false</code>.
    */
   public boolean isFieldExists(String fieldName)
   {
      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException(
               "fieldName may not be null or empty.");
      
      // getPageId() only covers a local field or a system/shared field with 
      // a defined PSField; use getMapping() for the system/shared field that 
      // does not override PSField.
      return getPageId(fieldName) != -1  
            || getContentEditorMapper().getUIDefinition()
                  .getMapping(fieldName) != null;
   }
   
   /**
    * Get the field set for the supplied name.
    * 
    * @param fieldSetName the name of the field set to get, not 
    *    <code>null</code> or empty.
    * @return the field set or <code>null</code> if not found.
    */
   public PSFieldSet getFieldSet(String fieldSetName)
   {
      PSContentEditorPipe pipe = getPipe();
      
      PSContentEditorMapper mapper = pipe.getMapper();
      return mapper.getFieldSet(fieldSetName);
   }

   public PSContentEditorPipe getPipe()
   {
      PSContentEditorPipe pipe =  
         (PSContentEditorPipe) m_contentEditorDef.getPipe();
      if (pipe == null)
      {
         // bug, this should never happen
         throw new RuntimeException("missing pipe on content editor");
      }
      return pipe;
   }
   
   /**
    * Get the top level field set.
    * 
    * @return the field set, never <code>null</code>.
    */
   public PSFieldSet getFieldSet()
   {
      PSContentEditorPipe pipe = getPipe();      
      PSContentEditorMapper mapper = pipe.getMapper();
      return mapper.getFieldSet();
   }
      
   /**
    * Get the first table set
    * 
    * @return The table set, never <code>null</code>.
    */
   public PSTableSet getTableSet()
   {
      PSContentEditorPipe pipe = getPipe();
      return (PSTableSet) pipe.getLocator().getTableSets().next();
   }
   
   /**
    * Get the display mapper for the specified field set
    * 
    * @param fieldSetRef the field set ref of the mapper
    * 
    * @return The mapper, or <code>null</code> if not found.
    */
   public PSDisplayMapper getDisplayMapper(String fieldSetRef)
   {
      PSContentEditorPipe pipe = getPipe();      
      PSContentEditorMapper mapper = pipe.getMapper();
      
      return mapper.getUIDefinition().getDisplayMapper(fieldSetRef);
   }

   /**
    * Look at all system fields and return those whose read-only property is
    * <code>false</code> and don't have a UI mapping.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   private Collection<PSField> getUnmappedSystemDbFields()
   {
      PSContentEditorSystemDef def = PSServer.getContentEditorSystemDef();
      PSFieldSet fs = def.getFieldSet();
      PSUIDefinition ui = def.getUIDefinition();
      Collection<PSField> results = new ArrayList<>();
      Iterator fields = fs.getAll(false);
      while (fields.hasNext())
      {
         PSField field = (PSField) fields.next();
         if (!field.isReadOnly() 
               && null == ui.getMapping(field.getSubmitName()))
         {               
            results.add(field);
         }
      }
      fields = fs.getAll(true);
      while (fields.hasNext())
      {
         PSField field = (PSField) fields.next();
         if (!field.isReadOnly() 
               && null == ui.getMapping(field.getSubmitName()))
         {               
            results.add(field);
         }
      }
      return results;
   }

   /**
   * Returns clone of Options map
   */
   protected Map getOptionsMap(){
      return null;
   }

   /**
    * Returns the workflowid for this item.
    *
    * @return the workflowid of this item.  Will be > 0.
    */
   public int getWorkflowId()
   {
      return m_contentEditorDef.getWorkflowId();
   }

   /**
    * List of slot names on this type.
    * @return <code>null</code>.
    */
   public Iterator getSlotNames(){
      return null;
   }

   /**
    * List of slot ids on this type.
    * @return <code>null</code>.
    */
   public Iterator getSlotIds(){
      return null;
   }

   /**
    * List of variant names on this type.
    * @param    slotName
    * @return <code>null</code>.
    */
   public Iterator getVariantNames(@SuppressWarnings("unused") String slotName)
   {
      return null;
   }

   /**
    * List of variant ids on this type.
    * @param    slotId
    */
   public Iterator getVariantIds(@SuppressWarnings("unused") int slotId)
   {
      return null;
   }

   // see IPSComponent 
   @Override
   public Element toXml(Document doc)
   {
      Element data = doc.createElement("ItemDefData");
      data.appendChild(super.toXml(doc));
      data.appendChild(m_contentEditorDef.toXml(doc));
      data.setAttribute(ATTR_HIDDEN, m_isHidden ? TRUE : FALSE);
      data.setAttribute(ATTR_APPNAME, m_appName);
      data.setAttribute(ATTR_OBJTYPE, String.valueOf(m_objectType));
      return data;
   }

   // see IPSComponent 
   @Override
   public Object clone()
   {
      final PSItemDefinition copy = (PSItemDefinition) super.clone();
      //m_appName handled by clone

      if (m_contentEditorDef != null)
         copy.m_contentEditorDef = (PSContentEditor)m_contentEditorDef.clone();

      // TODO: need proper clones of the objects within map
      if (m_optionMap != null)
      {
         Iterator<String> i = m_optionMap.keySet().iterator();
         while (i.hasNext())
         {
            String key = i.next();
            PSCollection val = m_optionMap.get(key);
            copy.m_optionMap.put(key, val);
         }
      }
      copy.m_fieldColumnActionMap = 
         new HashMap<>(m_fieldColumnActionMap);
      return copy;
   }
   
   
   
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   @Override
   public int hashCode()
   {
      throw new UnsupportedOperationException("HashCode Not Yet Implemented");
   }

   @Override
   public String toString()
   {
      return m_contentEditorDef.getName();
   }

   /** @see IPSComponent */
   @Override
   public void validate(@SuppressWarnings("unused") IPSValidationContext cxt)
   {}

   /** @see IPSComponent */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents, false);   
   }
      
   /**
    * This method is called to populate an object from its XML representation.
    * An element node may contain a hierarchical structure, including child 
    * objects. The element node can also be a child of another element node.
    * <p>
    * Each component should add itself to <code>parentComponents</code> before
    * constructing its child components, and should restore the original
    * <code>parentComponents</code> before returning.
    *
    * @param sourceNode   the XML element node to populate from, not <code>null
    * </code>.
    * @param parentDoc may be <code>null</code>.
    * @param parentComponents a collection of all the components created in
    * the process of creating this component.  May be <code>null</code>. 
    * @param isCopy <code>true</code> if this method is being used to populate
    * to a copy of an existing item definition, <code>false</code> otherwise.
    * 
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   private void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents, boolean isCopy)
      throws PSUnknownNodeTypeException
   {
      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, "ItemDefData");

      String hidden =
            PSXMLDomUtil.checkAttribute(sourceNode, ATTR_HIDDEN, false).trim();
      m_isHidden = hidden.equalsIgnoreCase(TRUE);

      /* There should be no backwards compatibility issue as these objects 
       * are not persisted beyond a session.
       */
      m_appName =
            PSXMLDomUtil.checkAttribute(sourceNode, ATTR_APPNAME, true).trim();
      
      m_objectType = PSXMLDomUtil.checkAttributeInt(sourceNode, ATTR_OBJTYPE, 
         true);

      Element el1 = PSXMLDomUtil.getFirstElementChild(sourceNode);
      Element el2 = PSXMLDomUtil.getNextElementSibling(el1);

      super.fromXml(el1, null, null);
      if (m_contentEditorDef == null)
      {
         m_contentEditorDef = new PSContentEditor(el2,
                                                  parentDoc,
                                                  parentComponents,
                                                  !isCopy);
      }
      else
      {
         m_contentEditorDef.fromXml(el2, parentDoc, parentComponents);
      }
   }

   /**
    * Get the guid of this object.
    * 
    * @return The guid, never <code>null</code>.
    */
   public IPSGuid getGuid()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_id);
   }

   /** 
    * This is the component id, which should not be confused with the type id.
    * @see IPSComponent 
    */
   @Override
   public int getId()
   {
      return m_id;
   }

   /** 
    * This is the component id, which is not the type id.
    * 
    * @see IPSComponent 
    */
   @Override
   public void setId(int id)
   {
      m_id = id;
   }

   /**
    * A flag to indicate whether this content editor is hidden from lists
    * showing content editors.
    *
    * @return <code>true</code> if it should not be shown to the end user,
    *    <code>false</code> otherwise.
    */
   public boolean isHidden()
   {
      return m_isHidden;
   }
   
   /**
    * Determine if the supplied field name references a complex child.
    * 
    * @param fieldSetName The field name, may not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if it is a complex child, <code>false</code> if
    * it is not, or if the field is not found.
    */
   public boolean isComplexChild(String fieldSetName)
   {
      if (fieldSetName == null || fieldSetName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldSetName may not be null or empty");
         
      // get the dipslay mapper and field set
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe)m_contentEditorDef.getPipe();
      PSContentEditorMapper ceMapper = pipe.getMapper();
      PSFieldSet fs = ceMapper.getFieldSet(fieldSetName); 
      
      return fs != null && fs.getType() == PSFieldSet.TYPE_COMPLEX_CHILD;
   }
   
   /**
    * Gets the fieldset named by the specified field name
    * 
    * @param fieldName The name of the simple child field in question, may not 
    * be <code>null</code> or empty.
    * 
    * @return the field set of the simple child. It may be <code>null</code> if
    * cannot find a simple child with the name.
    */
   @SuppressWarnings("unchecked")
   public PSFieldSet getSimpleChildSet(String fieldName)
   {
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldName may not be null or empty");
         
      if (m_simpleChildFieldSets == null)
      {
         m_simpleChildFieldSets = new HashMap<>();
         
         // get the display mapper and field set
         PSContentEditorPipe pipe = 
            (PSContentEditorPipe)m_contentEditorDef.getPipe();
         PSContentEditorMapper ceMapper = pipe.getMapper();
         PSFieldSet fs = ceMapper.getFieldSet(); // Get parent
         Iterator setiter = fs.getAll();
         while(setiter.hasNext())
         {
            Object x = setiter.next();
            if (x instanceof PSFieldSet)
            {
               PSFieldSet set = (PSFieldSet) x;
               if (set.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
               {
                  Iterator fields = set.getAll();
                  while(fields.hasNext())
                  {
                     Object f = fields.next();
                     if (f instanceof PSField)
                     {
                        PSField field = (PSField) f;
                        m_simpleChildFieldSets.put(field.getSubmitName(), set);
                     }
                  }
               }
            }
         }
      }
      
      return m_simpleChildFieldSets.get(fieldName);
   }   
   
   /**
    * Get the page id for the supplied fieldname.  Page ids are used in 
    * conjuntion with the <code>edit</code> and <code>preview</code> commands
    * to identify the correct editor page.  Parent page (main editor) has a
    * page id of 0.  Each complex child found in mapping order increments this
    * value by 2, first for its summary page, and second for its editor page.
    * Thus for the nth complex child field found, its summary page id will be
    * 2*n-1, and its editor page id will be 2*n. 
    * 
    * @param fieldName The name of the field, may not be <code>null</code> or 
    * empty.
    * 
    * @return If the field is found in the top level mapper, the parent page id
    * is returned.  If the field identifies a complex child, the summary page id 
    * for that child is returned.  If the field is contained within a complex 
    * child, then the editor page id for that child is returned.  If no matching
    * field is found, <code>-1</code> is returned. 
    */
   public int getPageId(String fieldName)
   {      
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldName may not be null or empty");

      // get the dipslay mapper and field set
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe)m_contentEditorDef.getPipe();
      PSContentEditorMapper ceMapper = pipe.getMapper();
      
      PSDisplayMapper mapper = ceMapper.getUIDefinition().getDisplayMapper();
      PSFieldSet fs = ceMapper.getFieldSet();
      
      return getPageId(fieldName, mapper, fs, new int[] {0});
   }
   
   /**
    * Recursive worker method for {@link #getPageId(String)}.  Recusively walks
    * the supplied mapper/field set looking for a match on the field name.  If
    * a match is found, returns the correct page id.
    * 
    * @param fieldName The name of the field to match, assumed not 
    * <code>null</code> or empty
    * @param mapper The display mapper to check, assumed not <code>null</code>.
    * @param fs The field set corresponding to the supplied mapper, assumed not 
    * <code>null</code> and to match the fieldset ref of the supplied mapper.
    * @param curPageId <code>int[1]</code> to pass current page id by reference,
    * incremented by calls to this method as complex child fields are found,
    * assumed not <code>null</code>.  Value of <code>0</code> should be passed
    * for the first call to this method.
    * 
    * @return The page id, or <code>-1</code> if a matching field is not found.
    */
   private int getPageId(String fieldName, PSDisplayMapper mapper, 
      PSFieldSet fs, int[] curPageId)
   {
      int pageId = -1;
      int currentPage = curPageId[0];
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext() && pageId == -1)
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         Object o = fs.get(mapping.getFieldRef());
         if (o == null)
            continue;
         boolean isComplexChild = (o instanceof PSFieldSet && 
            ((PSFieldSet)o).getType() == PSFieldSet.TYPE_COMPLEX_CHILD);
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (mapping.getFieldRef().equals(fieldName))
         {
            // if its a complex child field, return the summary page, otherwise
            // the current editor page
            pageId = currentPage;
            if (isComplexChild)
            {  
               // need to use next available page id
               pageId = curPageId[0] + 1; 
            }
         }
         else if (isComplexChild && childMapper != null)
         {
            /*
             * check the field set for the field - bump the current page id up 
             * to the child editor page
             */ 
            curPageId[0] += 2;
            pageId = getPageId(fieldName, childMapper, (PSFieldSet)o, 
               curPageId);
         }
      }
      
      return pageId;
   }
   
   
   /**
    * Determine if the content type represented by this definition is enabled
    * for indexing and thus may be searched.
    * 
    * @return <code>true</code> if it may be searched, <code>false</code>
    * otherwise.
    */
   public boolean isUserSearcheable()
   {
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe)m_contentEditorDef.getPipe();
      return pipe.getMapper().getFieldSet()
         .isUserSearchable();
   }
   
   /**
    * Determine if the specified child is enabled for indexing and thus may be
    * be searched.  Note that if {@link #isUserSearcheable()} returns
    * <code>false</code>, this method always returns <code>false</code>.
    * 
    * @param childId The child's mapper id.
    * 
    * @return <code>true</code> if the child is searchable, <code>false</code>
    * if it is not, or if this content type is not searchable, or if the 
    * fieldset for the specified child id cannot be located. 
    */
   public boolean isUserSearcheable(int childId)
   {
      // fist see if parent is searcheable
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe)m_contentEditorDef.getPipe();
      
      PSContentEditorMapper mapper = pipe.getMapper();
      boolean isSearcheable = mapper.getFieldSet().isUserSearchable();
      
      // if parent is, then check child
      if (isSearcheable)
      {
         // default to false if child not found
         isSearcheable = false;
         PSDisplayMapper childMapper = 
            mapper.getUIDefinition().getDisplayMapper(childId);
         if (childMapper != null)
         {
            PSFieldSet fs = mapper.getFieldSet(childMapper.getFieldSetRef());
            if (fs != null)
               isSearcheable = fs.isUserSearchable();
         }
      }
      
      return isSearcheable;
   }
   
   /**
    * Replace the content editor defintion with a new one.
    * 
    * @param editorDef The content editor, may not be <code>null</code>.
    */
   public void setContentEditor(PSContentEditor editorDef)
   {
      if (editorDef == null)
         throw new IllegalArgumentException("editorDef may not be null");
      
      m_contentEditorDef = editorDef;
   }
   
   /**
    * Set fields to indicate that their table columns will be deleted or 
    * altered by the Content editor table handler. This set will be cleared
    * when the handler processes the table changes.
    * @param fields fields to have their columns deleted or altered. May
    * be <code>null</code>or empty.
    */
   public void setFieldColumnActions(Map<PSField, Integer> fields)
   {
     m_fieldColumnActionMap.clear();
      if(fields != null && !fields.isEmpty())
         m_fieldColumnActionMap.putAll(fields);
   }
   
   /**
    * @return fields indicating that their table columns will be deleted.
    * Never <code>null</code>, may be empty.
    */
   public Map<PSField, Integer> getFieldColumnActions()
   {
      return m_fieldColumnActionMap;
   }

   /**
    * Removes all db specific information, leaving a dummy entry for the table
    * ref in the container locator (because it can't be left empty.) 
    */
   @SuppressWarnings("unchecked") //PSTableSet iterator
   public Object tuneClone(long newId)
   {
      //todo - remove cast when supporting full guids
      setTypeId((int) newId);
      m_contentEditorDef.setContentType(newId);
      m_contentEditorDef.setName(getName());
      
      setLabel(getName());
      m_appName = PSContentType.createAppName(getName());
      setEnabled(false);
      m_contentEditorDef.getRequestor().setRequestPage(getName());
      setEditorUrl(PSContentType.createRequestUrl(getName()));
      
      PSContentEditorPipe pipe = getPipe();
      
      PSFieldSet parentFieldSet = pipe.getMapper().getFieldSet();
      String fsName = "CT_" + getName();
      parentFieldSet.setName(fsName);
      //keep the mapper in sync
      PSDisplayMapper mapper = pipe.getMapper().getUIDefinition()
            .getDisplayMapper();
      mapper.setFieldSetRef(fsName);
      
      PSContainerLocator locator = pipe.getLocator();
      Iterator<PSTableSet> tableSets = locator.getTableSets();
      PSTableSet tableSet = tableSets.next();
      if (tableSets.hasNext())
      {
         //ph: I'm not aware of any case which is valid 
         throw new RuntimeException(
               "Don't know how to handle multiple table sets.");
      }
      
      PSCollection tableRefs = new PSCollection(PSTableRef.class);
      tableRefs.add(new PSTableRef("dummy"));
      tableSet.setTableRefs(tableRefs);
      
      //clear data locators for all fields
      PSField[] allFields = pipe.getMapper().getFieldSet().getAllFields();
      for (PSField field : allFields)
      {
         if (field.getType() == PSField.TYPE_LOCAL)
            field.setLocator(null);
      }
      
      return this;
   }

   /**
    * Walks all entries in the main mapper and each one that is for a complex
    * child, the associated fieldset is collected. A complex child is a field
    * set with a type of {@link PSFieldSet#TYPE_COMPLEX_CHILD}. All found sets
    * are returned. 
    * <p>
    * The mapper is walked to eliminate field set definitions that could be
    * present but are not actually used.
    * 
    * @return Never <code>null</code>, may be empty. Caller takes ownership of
    * returned list. The sets are ordered in document order.
    */
   public List<PSFieldSet> getComplexChildren()
   {
      PSContentEditorMapper mapper = getContentEditorMapper();
      List<PSFieldSet> results = new ArrayList<>();
      PSUIDefinition def = mapper.getUIDefinition();
      PSDisplayMapper dmapper = def.getDisplayMapper();
      for (Iterator mappingIter = dmapper.iterator(); mappingIter.hasNext();)
      {
         PSDisplayMapping entry = (PSDisplayMapping) mappingIter.next();
         PSDisplayMapper childMapper = entry.getDisplayMapper();
         if (childMapper == null)
            continue;
         
         PSFieldSet fs = getFieldSet(entry.getFieldRef());
         if (fs.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            results.add(fs);
      }
      return results;
   }

   /**
    * Gets the mimetype associated with the field. The mimetype is determined by
    * mimeTypeMode property of the field. If the mode is "From Selection", then
    * the mimetype value is fetched from mimeTypeValue property of the field. If
    * the mode is "From Extension Field" then the mimeTypeValue property
    * provides the name of the field that will hold the extension at runtime.
    * The extension is fetched from the supplied fieldValues map and then the
    * corresponding mimetype for that extension is returned. If the mode is
    * "From Mime Type Field" then mimeTypeValue property provides the name of
    * the field that will hold the mimetype at runtime. The mime type is fetched
    * from the supplied fieldValues map and returned.
    * 
    * If the mode is null or empty then the mimetype is guessed based on the
    * following algorithm. If the data type of the field is binary then the name
    * of the extension field is guessed as "fieldName_ext". The extension is
    * fetched from the supplied fieldValues map and then the corresponding
    * mimetype for that extension is returned. If extension field does not exist
    * or its value is empty then it gets the mimetype value from field with name
    * "fieldName_type". If that is also empty then returns empty mimetype. If
    * the datatype is clob with max data format, then the mime type is guessed
    * as text/html. for all other data types the mime type is returned as
    * text/plain.
    * 
    * @param fieldName The name of the field for which the the mime type needs
    * to be returned. Must not be <code>null</code> or empty and must be a
    * valid field.
    * @param fieldValues The map of field names and values. Must not be
    * <code>null</code>. If empty and the mimetype mode is either from
    * extension field or mimetype field then the returned value will be empty.
    * @return The mime type of the field as per the method description or empty
    * string if is not set properly or could not guess it right, but never
    * <code>null</code>.
    * 
    */
   public String getFieldMimeType(String fieldName,
         Map<String, String> fieldValues)
   {
      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException(
               "fieldName must not be null or empty");
      if (fieldValues == null)
         throw new IllegalArgumentException("fieldValues must not be null");
      return getFieldMimeTypeCommon(fieldName, fieldValues);
   }

   /**
    * Convenient method to get the mime type similar to
    * {@see #getFieldMimeType(String, Map)} except the field value is extracted
    * from request instead of the fieldValues map.
    * 
    */
   public String getFieldMimeType(String fieldName,
         IPSRequestContext request)
   {
      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException(
               "fieldName must not be null or empty");
      if (request == null)
         throw new IllegalArgumentException("request must not be null");
      return getFieldMimeTypeCommon(fieldName, request);
   }

   /**
    * A common method to get the mimetype for the given fieldname and either Map
    * of values or request. For the algorithm
    * {@see #getFieldMimeType(String, Map)
    * 
    * @param fieldName Name of the field assumed not <code>null</code>.
    * @param valueSrc The value source from which the value needs to be
    * extracted. The expected values are either IPSRequestContext request or 
    * Map<String,String>.
    * @return The mime type of the field as per the method description or empty
    * string if is not set properly or could not guess it right, but never
    * <code>null</code>.
    */
   private String getFieldMimeTypeCommon(String fieldName, Object valueSrc)
   {
      String mimetype = "";
      PSField fld = getFieldByName(fieldName);
      
      if (fld == null)
      {
         //Check in system def some mandatory fields are not part of the item def
         //but get added to the item from systemdef.
         PSContentEditorSystemDef def = PSServer.getContentEditorSystemDef();
         PSFieldSet fs = def.getFieldSet();
         fld = fs.getFieldByName(fieldName);
         if(fld == null)
            return mimetype;
      }
      PSField.PSMimeTypeModeEnum modeEnum = fld.getMimeTypeMode();
      int mode = modeEnum == null ? PSField.PSMimeTypeModeEnum.DEFAULT
            .getMode() : modeEnum.getMode();
      
      if (mode == PSField.PSMimeTypeModeEnum.FROM_SELECTION.getMode())
      {
         mimetype = StringUtils.defaultString(fld.getMimeTypeValue());
      }
      else if (mode == PSField.PSMimeTypeModeEnum.FROM_EXT_FIELD.getMode())
      {
         String extfld = fld.getMimeTypeValue();
         if (StringUtils.isBlank(extfld))
            return "";
         String extval = getValueFromSrc(extfld, valueSrc);
         if (StringUtils.isBlank(extval))
            return "";
         mimetype = StringUtils.defaultString(PSContentFactory
               .guessMimeType(extval));
      }
      else if (mode == PSField.PSMimeTypeModeEnum.FROM_MIMETYPE_FIELD.getMode())
      {
         String mtfld = fld.getMimeTypeValue();
         if (StringUtils.isBlank(mtfld))
            return "";
         mimetype = StringUtils
               .defaultString(getValueFromSrc(mtfld, valueSrc));
      }
      else
      {
         String dt = fld.getDataType();
         if (dt.equals(PSField.DT_BINARY) || dt.equals(PSField.DT_IMAGE))
         {
            String extfld = fieldName + "_ext";
            String extval = getValueFromSrc(extfld, valueSrc);
            if (StringUtils.isNotBlank(extval))
               mimetype = PSContentFactory.guessMimeType(extval);

            if (StringUtils.isBlank(mimetype))
            {
               String mtfld = fieldName + "_type";
               if (StringUtils.isNotBlank(mtfld))
                  mimetype = StringUtils.defaultString(getValueFromSrc(mtfld,
                        valueSrc));
            }
         }
         else if (dt.equals(PSField.DT_TEXT))
         {
            String df = fld.getDataFormat();
            if (df != null && df.equals(PSField.MAX_FORMAT))
               mimetype = "text/html";
            else
               mimetype = "text/plain";
         }
         else
         {
            mimetype = "text/plain";
         }
      }
      return mimetype;
   }

   /**
    * 
    * @param name Name of the parameter whose value needs to be returned.
    * Assumed not <code>null</code>.
    * @param valueSrc The value source from which the value needs to be
    * extracted. The expected values are either IPSRequestContext request or 
    * Map<String,String>.
    * @return The value or null if not found.
    */
   private String getValueFromSrc(String name, Object valueSrc)
   {
      String value = null;
      if (valueSrc instanceof IPSRequestContext)
      {
         value = ((IPSRequestContext) valueSrc).getParameter(name);
      }
      else if (valueSrc instanceof Map)
      {
         value = (String) ((Map) valueSrc).get(name);
      }
      return value;
   }
   
    /**
     * Return a list of the content table names related to this type. Includes
     * any shared and child tables.
     * 
     * @return The List of Tables as a PSBackEndTable
     */
    public List<PSBackEndTable> getTypeTables()
    {
        
        List<PSField> fields = new ArrayList<>(getMappedParentFields());
        fields.addAll(getUnmappedSystemDbFields());

        for (PSFieldSet fs : getComplexChildren())
        {
            fields.addAll(Arrays.asList(fs.getAllFields()));
        }

        Map<String, PSBackEndTable> tables = new HashMap<>();

        for (PSField field : fields)
        {
            IPSBackEndMapping beMapping = field.getLocator();
            if (beMapping instanceof PSBackEndColumn)
            {
                PSBackEndColumn column = (PSBackEndColumn) beMapping;
                PSBackEndTable colTable = column.getTable();
                String colTableAlias = column.getTable().getAlias().toUpperCase();

                // be sure this isn't the content status table
                if (!colTableAlias.equalsIgnoreCase(PSContentEditorSystemDef.CONTENT_STATUS_TABLE_ALIAS))
                {
                    // don't add more than once
                    if (!tables.containsKey(colTableAlias))
                        tables.put(colTableAlias, colTable);
                }
            }
        }
        return new ArrayList<>(tables.values());
    }
   
   /**
    * Map with fieldname as the key and a PSCollection of PSEntry objects that
    * define the Value Choices as the value.
    */
   private Map<String,PSCollection> m_optionMap;
   private PSContentEditor m_contentEditorDef;
   
   /**
    * Maps field names to associated simple child fieldsets. Initialized
    * on first use.
    */
   private transient Map<String,PSFieldSet> m_simpleChildFieldSets = null;

   /**
    * See {@link #isHidden()} for details. More specifically, its value is
    * determined by the value of the HIDEFROMMENU column in the CONTENTTYPES
    * table.
    * <p>Defaults to <code>false</code>.
    */
   private boolean m_isHidden = false;

   /**
    * Identifies the application that contains the actual editor definition.
    * Set during std or xml construction, then never <code>null</code> or empty.
    */
   private String m_appName;

   /**
    * See {@link #isEnabled()} for details.
    */
   private boolean m_enabled = true;
   
   /**
    * The object type of the content type, set during std or xml construction, 
    * then never modified after that.  
    */
   private int m_objectType;
   
   /**
    * The component id, set during std or xml construction. 
    */
   private int m_id;
   
   /**
    * Set of fields that should have their database columns altered or deleted.
    * This list will be cleared by the CE table handler after it processes.
    * Never <code>null</code>, may be empty.
    */
   private Map<PSField, Integer> m_fieldColumnActionMap = 
      new HashMap<>();

   private static final String ATTR_HIDDEN = "isHidden";
   private static final String ATTR_APPNAME= "appName";
   private static final String ATTR_OBJTYPE = "objectType";
   private static final String TRUE = "true";
   private static final String FALSE = "false";
   
   /**
    * Action hint to tell the handler that the column associated
    * with the specified field should be altered.
    */
   public static final int COLUMN_ACTION_ALTER = 1;
   
   /**
    * Action hint to tell the handler that the column associated
    * with the specified field should be removed.
    */
   public static final int COLUMN_ACTION_DELETE = 2;
}
