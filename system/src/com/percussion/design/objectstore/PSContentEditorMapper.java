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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Implements the PSXContentEditorMapper DTD defined in
 * ContentEditorLocalDef.dtd.
 */
public class PSContentEditorMapper extends PSComponent
{
   /**
    * Creates a new content editor mapper for the provided parameters.
    *
    * @param systemFieldExcludes a list of system field excludes, may be
    *    <code>null</code> or empty.
    * @param sharedFieldIncludes a list of shared field includes, may be
    *    <code>null</code> or empty.
    * @param fieldSet the field set to use for this mapper, not
    *    <code>null</code>.
    * @param uiDefinition the UI definition to used for this mapper, not
    *    <code>null</code>.
    */
   public PSContentEditorMapper(ArrayList systemFieldExcludes,
                                ArrayList sharedFieldIncludes,
                                PSFieldSet fieldSet,
                                PSUIDefinition uiDefinition)
   {
      setSystemFieldExcludes(systemFieldExcludes);
      setSharedFieldIncludes(sharedFieldIncludes);
      setFieldSet(fieldSet);
      setUIDefinition(uiDefinition);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSContentEditorMapper(Element sourceNode, IPSDocument parentDoc,
                                ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSContentEditorMapper()
   {
   }

   /**
    * Recurses all field sets looking for a match and returns it if found.
    *
    * @param ref the field set reference we are looking for, may be
    *    <code>null</code> or empty.
    * @return the field set found or <code>null</code> if not found.
    */
   public PSFieldSet getFieldSet(String ref)
   {
      if (ref == null || ref.trim().length() == 0)
         return null;

      if (m_fieldSet.getName().equals(ref))
         return m_fieldSet;

      return getFieldSet(m_fieldSet, ref);
   }

   /**
    * Recurse all field set looking for the reference one.
    *
    * @param fieldSet the field set to start with, may be <code>null</code>.
    * @param ref the field set reference we are looking for, may be
    *    <code>null</code> or empty.
    * @return the field set found or <code>null</code> if not found.
    */
   private PSFieldSet getFieldSet(PSFieldSet fieldSet, String ref)
   {
      if (fieldSet == null || ref == null)
         return null;

      PSFieldSet found = null;
      Iterator it = fieldSet.getAll();
      while(it.hasNext() && found == null)
      {
         Object o = it.next();
         if (o instanceof PSFieldSet)
         {
            PSFieldSet fs = (PSFieldSet) o;
            if (fs.getName().equals(ref))
               found = fs;
            else
               found = getFieldSet(fs, ref);
         }
      }

      return found;
   }

   /**
    * Get the system field excludes.
    *
    * @return a list of system field excludes (Strings of field
    *    names), never <code>null</code>, may be empty.
    */
   public Iterator getSystemFieldExcludes()
   {
      return m_systemFieldExcludes.iterator();
   }

   /**
    * Set the list of system field name excludes (a list of String objects).
    *
    * @param systemFieldExcludes the new list of system field excludes,
    *    may be <code>null</code> or empty.
    */
   public void setSystemFieldExcludes(ArrayList systemFieldExcludes)
   {
      if (systemFieldExcludes == null)
         m_systemFieldExcludes = new ArrayList();
      else
         m_systemFieldExcludes = systemFieldExcludes;
   }

   /**
    * Get the shared field includes.
    *
    * @return a list of shared field group names (String) included,
    *    never <code>null</code>, may be empty.
    */
   public Iterator getSharedFieldIncludes()
   {
      return m_sharedFieldIncludes.iterator();
   }

   /**
    * Set a new list of shared field group names included (a list of String
    * objects).
    *
    * @param sharedFieldIncludes the new list of shared field group names
    *    (String) included, may be <code>null</code> or empty.
    */
   public void setSharedFieldIncludes(ArrayList sharedFieldIncludes)
   {
      if (sharedFieldIncludes == null)
         m_sharedFieldIncludes = new ArrayList();
      else
         m_sharedFieldIncludes = sharedFieldIncludes;
   }

   /**
    * Get the shared field excludes.
    *
    * @return a list of shared field excludes (Strings of field
    *    names), never <code>null</code>, may be empty.
    */
   public Iterator getSharedFieldExcludes()
   {
      return m_sharedFieldExcludes.iterator();
   }

   /**
    * Set the list of shared field name excludes (a list of String objects).
    *
    * @param sharedFieldExcludes the new list of shared field excludes,
    *    may be <code>null</code> or empty.
    */
   public void setSharedFieldExcludes(ArrayList sharedFieldExcludes)
   {
      if (sharedFieldExcludes == null)
         m_sharedFieldExcludes = new ArrayList();
      else
         m_sharedFieldExcludes = sharedFieldExcludes;
   }

   /**
    * Get the field set.
    *
    * @return the field set used, never <code>null</code>.
    */
   public PSFieldSet getFieldSet()
   {
      return m_fieldSet;
   }

   /**
    * Set the new field set.
    *
    * @param fieldSet the new field set, never <code>null</code>.
    */
   public void setFieldSet(PSFieldSet fieldSet)
   {
      if (fieldSet == null)
         throw new IllegalArgumentException(
            "fieldSet cannot be null");

      m_fieldSet = fieldSet;
   }

   /**
    * Get the UI definition.
    *
    * @return the current UI definition, never
    *    <code>null</code>.
    */
   public PSUIDefinition getUIDefinition()
   {
      return m_uiDefinition;
   }

   /**
    * Set a new UI definition.
    *
    * @param uiDefinition the new UI definition, never <code>null</code>.
    */
   public void setUIDefinition(PSUIDefinition uiDefinition)
   {
      if (uiDefinition == null)
         throw new IllegalArgumentException(
            "uiDefinition cannot be null");

      m_uiDefinition = uiDefinition;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSContentEditorMapper, not <code>null</code>.
    */
   public void copyFrom(PSContentEditorMapper c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setFieldSet(c.getFieldSet());
      setSharedFieldIncludes(c.m_sharedFieldIncludes);
      setSystemFieldExcludes(c.m_systemFieldExcludes);
      setSharedFieldExcludes(c.m_sharedFieldExcludes);
      setUIDefinition(c.getUIDefinition());
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSContentEditorMapper)) return false;
        if (!super.equals(o)) return false;
        PSContentEditorMapper that = (PSContentEditorMapper) o;
        return Objects.equals(m_systemFieldExcludes, that.m_systemFieldExcludes) &&
                Objects.equals(m_sharedFieldIncludes, that.m_sharedFieldIncludes) &&
                Objects.equals(m_sharedFieldExcludes, that.m_sharedFieldExcludes) &&
                Objects.equals(m_fieldSet, that.m_fieldSet) &&
                Objects.equals(m_uiDefinition, that.m_uiDefinition) &&
                Objects.equals(m_addedSystemMandatoryFields, that.m_addedSystemMandatoryFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), m_systemFieldExcludes, m_sharedFieldIncludes, m_sharedFieldExcludes, m_fieldSet, m_uiDefinition, m_addedSystemMandatoryFields);
    }

    /**
    *
    * @see IPSComponent
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents)
   throws PSUnknownNodeTypeException  
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);
      
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;
      
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
         // OPTIONAL: get the system field excludes
         Node current = tree.getCurrent();
         node = tree.getNextElement(SYSTEM_FIELD_EXCLUDES_ELEM, firstFlags);
         if (node != null)
         {
            node = tree.getNextElement(FIELD_REF_ELEM, firstFlags);
            while(node != null)
            {
               m_systemFieldExcludes.add(tree.getElementData(node));
               
               node = tree.getNextElement(FIELD_REF_ELEM, nextFlags);
            }
         }
         tree.setCurrent(current);
         
         // OPTIONAL: get the shared field includes
         current = tree.getCurrent();
         node = tree.getNextElement(SHARED_FIELD_INCLUDES_ELEM, firstFlags);
         if (node != null)
         {
            Element parent = node;
            node = tree.getNextElement(SHARED_FIELD_GROUP_NAME_ELEM, firstFlags);
            while(node != null)
            {
               m_sharedFieldIncludes.add(tree.getElementData(node));
               
               node = tree.getNextElement(SHARED_FIELD_GROUP_NAME_ELEM, nextFlags);
            }
            
            // OPTIONAL: get shared field excludes
            tree.setCurrent(parent);
            node = tree.getNextElement(SHARED_FIELD_EXCLUDES_ELEM, firstFlags);
            if (node != null)
            {
               node = tree.getNextElement(FIELD_REF_ELEM, firstFlags);
               while(node != null)
               {
                  m_sharedFieldExcludes.add(tree.getElementData(node));
                  node = tree.getNextElement(FIELD_REF_ELEM, nextFlags);
               }
            }
         }
         tree.setCurrent(current);
         
         // REQUIRED: get the field set
         node = tree.getNextElement(PSFieldSet.XML_NODE_NAME, firstFlags);
         if (node != null)
         {
            m_fieldSet = new PSFieldSet(
                  node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
                  XML_NODE_NAME,
                  PSFieldSet.XML_NODE_NAME,
                  "null"
            };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         
         // REQUIRED: get the UI definition
         node = tree.getNextElement(PSUIDefinition.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_uiDefinition = new PSUIDefinition(
                  node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
                  XML_NODE_NAME,
                  PSUIDefinition.XML_NODE_NAME,
                  "null"
            };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      Iterator it = null;
      Element elem = null;

      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);

      // create the system field excludes
      it = getSystemFieldExcludes();
      if (it.hasNext())
      {
         elem = doc.createElement(SYSTEM_FIELD_EXCLUDES_ELEM);
         while (it.hasNext())
         {
            PSXmlDocumentBuilder.addElement(
               doc, elem, FIELD_REF_ELEM, (String) it.next());
         }
         root.appendChild(elem);
      }

      // create the shared field includes
      it = getSharedFieldIncludes();
      if (it.hasNext())
      {
         elem = doc.createElement(SHARED_FIELD_INCLUDES_ELEM);
         while (it.hasNext())
         {
            PSXmlDocumentBuilder.addElement(
               doc, elem, SHARED_FIELD_GROUP_NAME_ELEM, (String) it.next());
         }
         root.appendChild(elem);

         // create any shared field excludes
         it = getSharedFieldExcludes();
         if (it.hasNext())
         {
            Element parent = elem;
            elem = doc.createElement(SHARED_FIELD_EXCLUDES_ELEM);
            while (it.hasNext())
            {
               PSXmlDocumentBuilder.addElement(
                  doc, elem, FIELD_REF_ELEM, (String) it.next());
            }
            parent.appendChild(elem);
         }
      }

      // create the field set
      root.appendChild(m_fieldSet.toXml(doc));

      // create the UI definition
      root.appendChild(m_uiDefinition.toXml(doc));

      return root;
   }

   // see IPSComponent
   @Override
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_fieldSet != null)
            m_fieldSet.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_MAPPER, null);

         if (m_uiDefinition != null)
            m_uiDefinition.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_MAPPER, null);
      }
      finally
      {
         context.popParent();
      }
   }
   
   /**
    * Adds the shared fieldset name to all contained fields of
    * the passed in shared definition.
    * @param def the shared definition. Cannot be <code>null</code>.
    */
   public static void addSharedFieldMeta(final PSContentEditorSharedDef def)
   {
      if(def == null)
         throw new IllegalArgumentException("def cannot be null.");
      Iterator groups = def.getFieldGroups();
      while(groups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
         PSFieldSet fieldset = group.getFieldSet();
         for(PSField field : fieldset.getAllFields())
         {
            field.setUserProperty(PSField.SHARED_GROUP_FIELDSET_USER_PROP,
                  fieldset.getName());
         }
         
      }
   }   

   /**
    * Validates the mapper and gets copy of this mapper merged with system and
    * shared definitions. The merged mapper will have corrected field excludes
    * list as user may specify a system/shared field as included which does not
    * have mapping defined either in the local or in the respective definition.
    * Does not modify the local fields or corresponding mappings in any way.
    * <br>
    * For example if one of the system field/mapping is overridden in the local
    * content editor mapper, the returned mapper will have an entry for that
    * system field merged with its definition keeping its overridden attributes
    * or if it is just included then that field and mapping gets added to this
    * local definition.
    *
    * @param sysDef the content editor system definition, may not be <code>null
    * </code>
    * @param sharedDef the content editor shared definition, may not be <code>
    * null</code>
    * @param mergeDefault if <code>true</code> the UI set provided for local
    * system/shared field mapping merged with its Default UI set before merging
    * with UI set defined in system/shared definition(the UI set merged with
    * its default), otherwise it simply merges with UI set defined in
    * system/shared definition
    *
    * @return the merged content editor mapper, never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in merging.
    */
   public PSContentEditorMapper getMergedMapper(
      PSContentEditorSystemDef sysDef, PSContentEditorSharedDef sharedDef,
      boolean mergeDefault)
      throws PSSystemValidationException
   {
      if (sysDef == null)
         throw new IllegalArgumentException("sysDef may not be null.");
      
      if (sharedDef == null)
         throw new IllegalArgumentException("sharedDef may not be null.");
      addSharedFieldMeta(sharedDef);
      
      /*
       * Validate that the shared includes exist and that the shared excludes
       * are actual fields in these groups.
       */
      validateSharedGroups(sharedDef);

      // Validate system excludes are actual fields in the system def fieldset.
      validateSystemFieldExcludes(sysDef);
      
      m_addedSystemMandatoryFields = new ArrayList();
      List systemFieldExcludes = getSystemFieldExcludes(sysDef);
      List sharedFieldExcludes = new ArrayList();
      List sharedFieldIncludes = getSharedFieldIncludes(sharedDef, 
         sharedFieldExcludes);

      /*
       * The fields in the fieldset may not be set with proper field type as
       * this object is constructed from xml ('type' attribute is an optional
       * attribute of PSXField). The field types should be fixed for proper
       * merging.
       */
      getFieldSet().fixSourceTypes(sysDef, sharedDef, 
         systemFieldExcludes, sharedFieldIncludes, sharedFieldExcludes);

      // Gets the merged fieldset and UI definition.
      PSFieldSet mergedFieldSet = getMergedFieldSet(sysDef, sharedDef, 
         systemFieldExcludes, sharedFieldIncludes, sharedFieldExcludes);
      PSUIDefinition mergedUIDef = getMergedUIDefinition(
         sysDef, sharedDef, systemFieldExcludes, sharedFieldIncludes, 
         sharedFieldExcludes, mergeDefault);

      // create new merged mapper
      PSContentEditorMapper mergedMapper = new PSContentEditorMapper(
         (ArrayList) systemFieldExcludes, (ArrayList) sharedFieldIncludes, 
         mergedFieldSet, mergedUIDef);
      mergedMapper.m_addedSystemMandatoryFields = m_addedSystemMandatoryFields;

      /*
       * Even though some system/shared fields are specified as included, they
       * might have not been mapped because there is no local or system mapping
       * defined for them. So we have to update the field excludes from the
       * merged ui def and fieldset.
       */
      mergedMapper.updateExcludes(sysDef, sharedDef, false);

      return mergedMapper;
   }

   /**
    * Gets the copy of this editor's parent field set merged with system and
    * shared definition. Validates the fields and fieldset after merging have
    * unique names.
    *
    * <br>
    * For example, if a field from system definition is included then that field
    * gets merged if that field is overridden in the local definition, otherwise
    * gets added to the local definition. Same for fieldset too.
    *
    * @param sysDef the content editor system definition, assumed not to be
    *    <code>null</code>
    * @param sharedDef the content editor shared definition, assumed not to be
    *    <code>null</code>
    * @param systemFieldExcludes the list of all system field excludes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @param sharedFieldIncludes the list of all shared field includes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @param sharedFieldExcludes the list of all shared field excludes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @return the merged field set, never <code>null</code>
    * @throws PSSystemValidationException if an error happens in merging.
    */
   private PSFieldSet getMergedFieldSet(PSContentEditorSystemDef sysDef, 
      PSContentEditorSharedDef sharedDef, List systemFieldExcludes, 
      List sharedFieldIncludes, List sharedFieldExcludes)
      throws PSSystemValidationException
   {
      PSFieldSet mergedFieldSet = new PSFieldSet(getFieldSet());

      // Add system fields, weeding out excludes
      PSFieldSet sysFieldSet = sysDef.getFieldSet();
      if (sysFieldSet != null)
      {
         // get copy from system fieldset and remove any excluded fields
         sysFieldSet = sysFieldSet.removeFields(systemFieldExcludes, true);

         // now merge with system fields we have
         mergedFieldSet = mergedFieldSet.merge(sysFieldSet);
      }

      //Get upper case of included shared groups for case insensitive check
      List sharedGroupIncludes = convertToUpper(sharedFieldIncludes);

      // add shared fields, only processing included groups
      if (sharedDef != null)
      {
         Iterator groups = sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
            
            if (!sharedGroupIncludes.contains(group.getName().toUpperCase()))
               continue;

            PSFieldSet groupFields = group.getFieldSet();
            if (groupFields.getType() == PSFieldSet.TYPE_PARENT)
            {
               Object[] args = {group.getName(), groupFields.getName()};
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_INVALID_SHARED_FIELDSET_TYPE, args);
            }

            // remove exluded shared fields
            groupFields = groupFields.removeFields(sharedFieldExcludes, false);

            // see if there is a placeholder child set in the content editor
            Object o = mergedFieldSet.get(groupFields.getName());
            if (o != null && o instanceof PSFieldSet)
            {
               PSFieldSet childSharedFieldSet = (PSFieldSet) o;
               groupFields = childSharedFieldSet.merge(groupFields);
            }
            
            mergedFieldSet.add(groupFields);
         }
      }

      return mergedFieldSet;
   }
   
   /**
    * Get all system field excludes for the supplied system definition. This
    * makes sure that the returned list does NOT contain any system mandatory
    * fields.
    * 
    * @param sysDef the system definition for which to get the excludes,
    *    may be <code>null</code> in which case the excludes from this object
    *    will be returned.
    * @return a list with all excluded field names as <code>String</code>,
    *    never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private List getSystemFieldExcludes(PSContentEditorSystemDef sysDef)
   {
      if (sysDef != null)
      {
         PSFieldSet fieldSet = sysDef.getFieldSet();
         if (fieldSet != null)
            return getRealExcludes(fieldSet, m_systemFieldExcludes);
      }
      
      return new ArrayList(m_systemFieldExcludes);
   }
   
   /**
    * Get all shared field includes and excludes. This makes sure that the
    * returned list of shared includes does include all shared groups with
    * system mandatory fields. This also makes sure that the shared field
    * excludes do NOT contain system mandatory fields.
    * 
    * @param sharedDef the shared definition for which to get the includes
    *    and excludes, may be <code>null</code> in which case the includes
    *    and excludes from this object will be returned.
    * @param sharedFieldExcludes the list into which the shared excludes will
    *    be collected, assumed not <code>null</code>.
    * @return the list of shared includes, never <code>null</code>, may be
    *    empty.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private List getSharedFieldIncludes(PSContentEditorSharedDef sharedDef, 
      List sharedFieldExcludes)
   {
      if (sharedDef != null)
      {
         List<String> sharedFieldIncludes = new ArrayList<>();
         
         List sharedFieldIncludesUpper = convertToUpper(m_sharedFieldIncludes);
         Iterator groups = sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
            
            PSFieldSet sharedFieldSet = group.getFieldSet();
            if (!sharedFieldIncludesUpper.contains(
               group.getName().toUpperCase()))
            {
               if (sharedFieldSet.hasMandatorySystemFields())
               {
                  sharedFieldIncludes.add(group.getName());
                  
                  List excludeNames = new ArrayList();
                  Iterator excludes = sharedFieldSet.getAll();
                  while (excludes.hasNext())
                  {
                     Object test = excludes.next();
                     if (test instanceof PSField)
                     {
                        PSField exclude = (PSField) test;
                        excludeNames.add(exclude.getSubmitName());
                     }
                  }
                  sharedFieldExcludes.addAll(getRealExcludes(sharedFieldSet, 
                     excludeNames));
               }
            }
            else
            {
               sharedFieldIncludes.add(group.getName());
               sharedFieldExcludes.addAll(getRealExcludes(sharedFieldSet, 
                  m_sharedFieldExcludes));
            }
         }
         
         return sharedFieldIncludes;
      }

      sharedFieldExcludes.addAll(m_sharedFieldExcludes);
      return new ArrayList(m_sharedFieldIncludes);
   }
   
   /**
    * Get the real excludes for the supplied field set. This makes sure that 
    * no system mandatory fields are excluded because they are required by the 
    * system to work properly.
    * 
    * @param fieldSet the field set for which to get the excludes,
    *    assumed not <code>null</code>.
    * @param excludes the current excludes list, assumed not <code>null</code>.
    * @return the real list of excluded fields, never <code>null</code>,
    *    may be empty.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private List getRealExcludes(PSFieldSet fieldSet, List excludes)
   {
      List realExcludes = new ArrayList();
      
      Iterator fieldNames = excludes.iterator();
      while (fieldNames.hasNext())
      {
         String fieldName = (String) fieldNames.next();
         Object test = fieldSet.get(fieldName);
         if (test instanceof PSField)
         {
            PSField field = (PSField) test;
            if (!field.isSystemMandatory())
               realExcludes.add(fieldName);
            else
               m_addedSystemMandatoryFields.add(fieldName);
         }
      }
      
      return realExcludes;
   }

   /**
    * Gets the copy of this editor's UI definition merged with the UI definition
    * for included system and shared fields defined in system and shared
    * definition. If the mapping is defined for an included system/shared field
    * in the local definition, then that mapping is merged with the mapping
    * defined in the respective definition, otherwise adds the mapping to local
    * definition.
    *
    * @param sysDef the content editor system definition, assumed not to be
    *    <code>null</code>
    * @param sharedDef the content editor shared definition, assumed not to be
    *    <code>null</code>
    * @param systemFieldExcludes the list of all system field excludes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @param sharedFieldIncludes the list of all shared field includes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @param sharedFieldExcludes the list of all shared field excludes as 
    *    <code>String</code>, assumed not <code>null</code>.
    * @param mergeDefault if <code>true</code> the UI set provided for local
    *    system/shared field mapping merged with it's Default UI set before 
    *    merging with UI set defined in system/shared definition(the UI set 
    *    merged with its default), otherwise it simply merges with UI set 
    *    defined in system/shared definition
    * @return the merged UI definition, never <code>null</code>
    * @throws PSSystemValidationException if an error happens in merging.
    */
   private PSUIDefinition getMergedUIDefinition(PSContentEditorSystemDef sysDef, 
      PSContentEditorSharedDef sharedDef, List systemFieldExcludes, 
      List sharedFieldIncludes, List sharedFieldExcludes, boolean mergeDefault)
      throws PSSystemValidationException
   {
      PSUIDefinition localUIDef = getUIDefinition();
      setSourceType(localUIDef.getDisplayMapper(), LOCAL);
      
      /*
       * Get the System UI definition and merge the mappings defined or add the
       * mappings for included system fields.
       */
      PSUIDefinition sysUIDef = sysDef.getUIDefinition();
      setSourceType(sysUIDef.getDisplayMapper(), SYSTEM);
      PSUIDefinition mergedUIDef = localUIDef.merge(sysUIDef, false, 
         systemFieldExcludes, mergeDefault);

      //Get upper case of included shared groups for case insensitive check
      List sharedGroupIncludes = convertToUpper(sharedFieldIncludes);
      /*
       * Get the Shared UI definition and merge the mappings defined or add the
       * mappings for included shared fields.
       */
      Iterator groups = sharedDef.getFieldGroups();
      while (groups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
         if (!sharedGroupIncludes.contains(group.getName().toUpperCase()))
            continue;

         PSUIDefinition sharedUIDef = group.getUIDefinition();
         setSourceType(sharedUIDef.getDisplayMapper(), SHARED);
         PSFieldSet sharedFieldSet = group.getFieldSet();

         /*
          * If we are merging with ui def/display mapper referring to the
          * complex child fieldset, then we have to merge with the child mapper
          * of the child mapping that referring to this fieldset instead of the
          * parent mapper. So we will send this flag to UI def to define how to
          * merge.
          */
         boolean mergeChild = false;
         if (sharedFieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            mergeChild = true;

         mergedUIDef = mergedUIDef.merge(sharedUIDef, mergeChild,
            sharedFieldExcludes, mergeDefault);
      }

      return mergedUIDef;
   }

   /**
    * Recurses through the supplied mapper and sets the source type on various
    * elements.
    *
    * @param dispMapper to be recursed. Assumed not <code>null</code>.
    * @param type the source type. Assumed to be a valid type which are
    *    "sys_system", "sys_local", "sys_shared".
    */
   private void setSourceType(PSDisplayMapper dispMapper, String type)
   {
      Iterator itr = dispMapper.iterator();
      while(itr.hasNext())
      {
         PSDisplayMapping dispMapping = (PSDisplayMapping)itr.next();
         PSDisplayMapper mapper = dispMapping.getDisplayMapper();
         if (null != mapper)
         {
            PSUISet uiset = dispMapping.getUISet();

            // set the source type of the label
            if (uiset.getLabel() != null)
               uiset.setLabelSourceType(type);

            setSourceType(mapper, type);
         }
         else
         {
            PSUISet uiset = dispMapping.getUISet();

            // set the source type of the label
            if (uiset.getLabel() != null)
               uiset.setLabelSourceType(type);

            // set the source type for all custom parameters
            PSControlRef control = uiset.getControl();
            if (control != null)
            {
               Iterator parameters = control.getParameters();
               while (parameters.hasNext())
               {
                  PSParam parameter = (PSParam) parameters.next();
                  parameter.setSourceType(type);
               }
            }

            // set the source type for all choices
            PSChoices choices = uiset.getChoices();
            if (choices != null)
            {
               if (choices.getType() == PSChoices.TYPE_LOCAL)
               {
                  Iterator entries = choices.getLocal();
                  while (entries.hasNext())
                  {
                     PSEntry entry = (PSEntry) entries.next();
                     entry.setSourceType(type);
                  }
               }
               PSNullEntry nullEntry = choices.getNullEntry();
               if (nullEntry != null)
                  nullEntry.setSourceType(type);
            }
         }
      }
   }

   /**
    * Determines the system field excludes, shared group includes and shared
    * field excludes from this mapper's fieldset and UI definition and updates
    * those. This should be called for a mapper which is merged with system and
    * shared definition.
    *
    * @param sysDef the content editor system definition, may not be <code>null
    * </code>
    * @param sharedDef the content editor shared definition, may not be <code>
    * null</code>
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void updateExcludes(PSContentEditorSystemDef sysDef,
      PSContentEditorSharedDef sharedDef, boolean save)
   {
      if(sysDef == null)
         throw new IllegalArgumentException("sysDef may not be null.");
      if(sharedDef == null)
         throw new IllegalArgumentException("sharedDef may not be null.");

      PSFieldSet fieldset = getFieldSet();
      PSUIDefinition uiDef = getUIDefinition();

      //get system fields that are not used
      ArrayList sysFieldExcludes = new ArrayList();
      sysFieldExcludes.addAll( getFieldExcludes(sysDef.getFieldSet(),
         uiDef, fieldset, PSField.TYPE_SYSTEM) );

      //get shared fields that are not used
      Iterator fieldGroups = sharedDef.getFieldGroups();
      ArrayList sharedGroupIncludes = new ArrayList();
      ArrayList sharedFieldExcludes = new ArrayList();
      while(fieldGroups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)fieldGroups.next();

         if (!save && !m_sharedFieldIncludes.contains(group.getName()))
         {
            //this whole shared group is NOT included, skip it entirely
            continue;
         }

         PSFieldSet groupFieldSet = group.getFieldSet();
         String fieldRef = groupFieldSet.getName();

         //Get the fields that are not used by this content editor mappings.
         List fieldExcludes = getFieldExcludes(
               groupFieldSet, uiDef, fieldset, PSField.TYPE_SHARED);

         /* Check whether any mapping is referring to this fieldset. If it
          * refers include that group. (satisfies for simple and complex child
          * fieldsets if they are included).
          */
         if(uiDef.getDisplayMapper().getMapping(fieldRef) != null)
         {
            Object object = fieldset.get(fieldRef);
            if(object instanceof PSFieldSet &&
               ((PSFieldSet)object).isSharedFieldSet())
            {
               sharedGroupIncludes.add( group.getName() );
               sharedFieldExcludes.addAll( fieldExcludes );
            }
         }
         /* In case of multiproperty simple child fieldset, we have mappings
          * only for fields, so if all fields of fieldset are not excluded, then
          * we include that group.
          */
         else if( !fieldExcludes.containsAll( new PSCollection(
            groupFieldSet.getNames() ) ) )
         {
            sharedGroupIncludes.add( group.getName() );
            sharedFieldExcludes.addAll( fieldExcludes );
         }
      }

      setSystemFieldExcludes(sysFieldExcludes);
      setSharedFieldIncludes(sharedGroupIncludes);
      setSharedFieldExcludes(sharedFieldExcludes);
   }

   /**
    * Finds the fields that are in the <code>sourceSet</code> and not used by
    * the provided target UI definition and the target set.
    *
    * @param sourceSet the source fieldset to be checked, cannot be
    * <code>null</code>
    * @param targetUIDef the UI definition to check whether the fields of source
    * set are used, cannot be <code>null</code>
    * @param targetSet the target fieldset to check whether the field is
    * included or not, cannot be <code>null</code>
    * @param type the type of the fields in the source set
    *
    * @return the list of fields that are not used (excluded), never <code>null
    * </code>, may be empty.
    */
   public static List<String> getFieldExcludes(PSFieldSet sourceSet,
      PSUIDefinition targetUIDef, PSFieldSet targetSet, int type)
   {
      if(sourceSet == null)
         throw new IllegalArgumentException("sourceSet cannot be null.");
      if(targetUIDef == null)
         throw new IllegalArgumentException("targetUIDef cannot be null.");
      if(targetSet == null)
         throw new IllegalArgumentException("targetSet cannot be null.");
      ArrayList<String> fieldExcludes = new ArrayList<>();

      Iterator sourceFields = sourceSet.getAll();
      while(sourceFields.hasNext())
      {
         Object obj = sourceFields.next();
         if(obj instanceof PSField)
         {
            String name = ((PSField)obj).getSubmitName();
            Object fieldRef = null;
            if(targetUIDef.getDisplayMapper().getMapping(name) != null)
            {
               fieldRef = targetSet.get(name);
               if(fieldRef == null)
               {
                  fieldRef = targetSet.getChildField(
                     name, PSFieldSet.TYPE_SIMPLE_CHILD);
                  if(fieldRef == null)
                  {
                     fieldRef = targetSet.getChildField(
                        name, PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);

                     if(fieldRef == null)
                     {
                        fieldRef = targetSet.getChildField(
                           name, PSFieldSet.TYPE_COMPLEX_CHILD);
                     }
                  }
               }
            }
            boolean excluded = true;
            if(fieldRef instanceof PSField)
            {
               PSField field = (PSField)fieldRef;
               String groupFsName = (String)field.getUserProperty(
                     PSField.SHARED_GROUP_FIELDSET_USER_PROP);

               if(type == field.getType())
                  if (StringUtils.isNotBlank(groupFsName) && 
                      !(groupFsName.equals(sourceSet.getName())))
                     excluded = true;
                  else
                     excluded = false;
            }

            if(excluded)
               fieldExcludes.add(name);
         }
      }

      return fieldExcludes;
   }

   /**
    * Gets copy of this mapper containing local fieldset and UI definition that
    * is demerged from system and shared definition. The demerged mapper will
    * contain the fields or mappings with overridden attributes only for
    * included system or shared fields. This should be called for a mapper that
    * is already merged with system and shared definition. Does not modify the
    * local fields or corresponding mappings in any way.
    *
    * @param sysDef the content editor system definition, may not be <code>null
    * </code>
    * @param sharedDef the content editor shared definition, may not be <code>
    * null</code>
    * @param demergeDefault supply <code>true</code> if the UI set of the merged    *
    * system/shared field mapping is merged with it's Default UI set before
    * merging with UI set defined in system/shared definition(the UI set merged
    * with its default), otherwise supply <code>false</code>
    *
    * @return the demerged content editor mapper, never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in demerging.
    */
   public PSContentEditorMapper getDemergedMapper(
      PSContentEditorSystemDef sysDef, PSContentEditorSharedDef sharedDef,
      boolean demergeDefault)
      throws PSSystemValidationException
   {
      if(sysDef == null)
         throw new IllegalArgumentException("sysDef may not be null.");
      if(sharedDef == null)
         throw new IllegalArgumentException("sharedDef may not be null.");


      //Get the fieldset and UI definition demerged from
      //system and shared definition.
      PSFieldSet demergedFieldSet = getDemergedFieldSet(sysDef, sharedDef);
      PSUIDefinition demergedUIDef =
         getDemergedUIDefinition(sysDef, sharedDef, demergeDefault);

      PSContentEditorMapper ceMapper =  new PSContentEditorMapper(
         m_systemFieldExcludes, m_sharedFieldIncludes, demergedFieldSet,
         demergedUIDef);

      ceMapper.setSharedFieldExcludes( m_sharedFieldExcludes );
      return ceMapper;
   }

    /**
    * Gets the copy of this editor's parent field set demerged with system and
    * shared definition. The demerged mapper will contain the system/shared
    * fields that are overridden. If none of the fields of the fieldset is
    * overridden, then that fieldset is removed from the local fieldset.
    *
    * @param sysDef the content editor system definition, assumed not to be
    * <code>null</code>
    * @param sharedDef the content editor shared definition, assumed not to be
    * <code>null</code>
    *
    * @return the demerged field set, never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in demerging.
    */
    private PSFieldSet getDemergedFieldSet(
      PSContentEditorSystemDef sysDef, PSContentEditorSharedDef sharedDef)
      throws PSSystemValidationException
   {
      PSFieldSet demergedFieldSet = new PSFieldSet( getFieldSet() );

      // Demerge the system fields
      PSFieldSet sysFieldSet = sysDef.getFieldSet();
      if (sysFieldSet != null)
      {
         sysFieldSet = sysFieldSet.removeFields(m_systemFieldExcludes, true);
         demergedFieldSet = demergedFieldSet.demerge(sysFieldSet);
      }

      //Get upper case of included shared groups for case insensitive check
      List sharedGroupIncludes = convertToUpper(m_sharedFieldIncludes);

      // Demerge the shared fields
      if (sharedDef != null)
      {
         Iterator groups = sharedDef.getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
            if (!sharedGroupIncludes.contains(group.getName().toUpperCase()))
               continue;

            PSFieldSet groupFields = group.getFieldSet();
            if (groupFields.getType() == PSFieldSet.TYPE_PARENT)
            {
               Object[] args = {group.getName(), groupFields.getName()};
               throw new PSSystemValidationException(
                  IPSObjectStoreErrors.CE_INVALID_SHARED_FIELDSET_TYPE, args);
            }

            groupFields = groupFields.removeFields(m_sharedFieldExcludes, false);

            // see if there is a placeholder child set in the content editor
            // and demerge.
            Object o = demergedFieldSet.get(groupFields.getName());
            if (o != null && o instanceof PSFieldSet)
            {
               PSFieldSet childSharedFieldSet = (PSFieldSet)o;
               childSharedFieldSet = childSharedFieldSet.demerge(groupFields);
               /* If the fieldset has any fields overridden or the fieldset has
                * any overridden attributes add the fieldset. Since all
                * attributes are required, we add all
                */
               if(childSharedFieldSet.getAll().hasNext() ||
                  !childSharedFieldSet.equalSettings(groupFields))
                  demergedFieldSet.add(childSharedFieldSet);
               else
                  demergedFieldSet.remove(childSharedFieldSet.getName());
            }
         }
      }

      return demergedFieldSet;
   }

   /**
    * Gets the copy of this editor's UI definition demerged with the UI
    * definition for included system and shared fields defined in system and
    * shared definition.
    *
    * @param sysDef the content editor system definition, assumed not to be
    * <code>null</code>
    * @param sharedDef the content editor shared definition, assumed not to be
    * @param demergeDefault supply <code>true</code> if the UI set of the merged    *
    * system/shared field mapping is merged with it's Default UI set before
    * merging with UI set defined in system/shared definition(the UI set merged
    * with its default), otherwise supply <code>false</code>
    *
    * @return the demerged UI Definition, never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in demerging.
    */
   private PSUIDefinition getDemergedUIDefinition(
      PSContentEditorSystemDef sysDef, PSContentEditorSharedDef sharedDef,
      boolean demergeDefault)
      throws PSSystemValidationException
   {
      PSUIDefinition localUIDef = getUIDefinition();

      PSUIDefinition sysUIDef = sysDef.getUIDefinition();
      PSUIDefinition demergedUIDef = localUIDef.demerge(
         sysUIDef, false, m_systemFieldExcludes, demergeDefault);

      //Get upper case of included shared groups for case insensitive check
      List sharedGroupIncludes = convertToUpper(m_sharedFieldIncludes);

      Iterator groups = sharedDef.getFieldGroups();
      while (groups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
         if (!sharedGroupIncludes.contains(group.getName().toUpperCase()))
            continue;

         PSUIDefinition sharedUIDef = group.getUIDefinition();
         PSFieldSet sharedFieldSet = group.getFieldSet();

         /* If we are demerging with ui def/display mapper referring to the
          * complex child fieldset, then we have to demerge the child mapper
          * of the child mapping that referring to this fieldset instead of the
          * parent mapper. So we will send this flag to UI def to define how to
          * demerge.
          */
         boolean demergeChild = false;
         if(sharedFieldSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
            demergeChild = true;

         demergedUIDef = demergedUIDef.demerge(sharedUIDef, demergeChild,
            m_sharedFieldExcludes, demergeDefault);

      }

      return demergedUIDef;
   }

   /**
    * Ensures that all shared groups included can be found in the shared def,
    * and that any shared field excludes can be found in those groups' fieldsets.
    * Also does the following validations:
    * <ul>
    *    <li>Shared group name and it's fieldSet name match.</li>
    *    <li>FieldSet name and display mapper's fieldSetRef match.</li>
    *    <li>Simple child fields have the required child mapping
    *    defined in the fields ui definition.</li>
    * </ul>
    * If any of these validations fail they will be returned in an iterator of
    * <code>PSSystemValidationException</code> objects that can be sent to the console.
    *
    * @param sharedDef the content editor shared def, assumed not to be <code>
    * null</code>
    *
    * @return iterator of <code>PSSystemValidationException</code> objects to capture
    * warnings. Never <code>null</code>, may be empty.
    *
    * @throws PSSystemValidationException if the above conditions are not met.
    */
   public Iterator validateSharedGroups(PSContentEditorSharedDef sharedDef)
      throws PSSystemValidationException
   {
      List<Exception> warnings = new ArrayList<>();
      
      Iterator includes = m_sharedFieldIncludes.iterator();
      PSCollection tmpExcludes = new PSCollection(
         m_sharedFieldExcludes.iterator());
      String include = "";
      if (includes.hasNext())
      {
         if (null == sharedDef)
         {
            // get name for error message
            include = (String) includes.next();
            throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_SHARED_GROUP_NO_DEF, include);
         }
         else
         {
            PSCollection sharedGroups = new PSCollection(
               sharedDef.getFieldGroups());
            while(includes.hasNext())
            {
               boolean hasMatch = false;
               include = (String) includes.next();
               Iterator groups = sharedGroups.iterator();
               while (groups.hasNext())
               {
                  PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
                  if (group.getName().equalsIgnoreCase(include))
                  {
                     hasMatch = true;

                     PSUIDefinition ui = group.getUIDefinition();
                     int type = group.getFieldSet().getType();
                     PSDisplayMapper mapper = ui.getDisplayMapper();
                     String groupName = group.getName();
                     String fieldSetRef = mapper.getFieldSetRef();
                     String fieldSetName = group.getFieldSet().getName();

                     // Validate that shared group name matches fieldset name
                     if(!groupName.equalsIgnoreCase(fieldSetName))
                     {
                        Object[] args = { groupName };
                        warnings.add( new PSSystemValidationException(
                           IPSObjectStoreErrors.
                              CE_GROUPNAME_AND_FIELDSETNAME_MUST_MATCH,
                           args ));
                     }

                     // Validate that the fieldSet name matches the Display
                     // mappers' fieldSetRef
                     if(!fieldSetName.equalsIgnoreCase(fieldSetRef))
                     {
                        Object[] args = { groupName };
                        warnings.add( new PSSystemValidationException(
                           IPSObjectStoreErrors.
                              CE_FIELDSETNAME_AND_FIELDSETREF_MUST_MATCH,
                           args ));
                     }
                     else if(type == PSFieldSet.TYPE_SIMPLE_CHILD)
                     {
                        // Validate that simple child fields have the required
                        // child display mapping.
                        PSDisplayMapping childMapping =
                           mapper.getMapping(fieldSetRef);
                        boolean bInvalid = false;
                        if(childMapping == null)
                        {
                           bInvalid = true;
                        }
                        else
                        {
                           // Does required child mapper exist?
                           PSDisplayMapper childMapper =
                              childMapping.getDisplayMapper();
                           if(childMapper != null)
                           {
                              // Does fieldSetRef match fieldSetName?
                              if(!childMapper.getFieldSetRef().
                                 equalsIgnoreCase(fieldSetName))
                                 bInvalid = true;

                              // Is there a mapping that matches the fieldRef
                              // for the single field in the fieldSet?
                              Iterator it =
                                 group.getFieldSet().getAll();
                              PSField field = null;
                              while(it.hasNext())
                              {
                                Object obj = it.next();
                                if(obj instanceof PSField)
                                {
                                   field = (PSField)obj;
                                   break;
                                }
                              }
                              if(field == null ||
                                 (childMapper.getMapping(
                                    field.getSubmitName()) == null))
                              {
                                 bInvalid = true;
                              }

                           }
                           else
                           {
                               bInvalid = true;
                           }
                        }

                        if(bInvalid)
                        {
                           Object[] args = { fieldSetRef };
                           warnings.add( new PSSystemValidationException(
                              IPSObjectStoreErrors.
                                 CE_MISSING_OR_INVALID_CHILD_DISPLAY_MAPPING,
                              args ));
                        }

                     }

                     // remove any excluded fields found from the temp list
                     PSFieldSet groupFields = group.getFieldSet();
                     Iterator excludes = tmpExcludes.iterator();
                     while (excludes.hasNext())
                     {
                        Object o = groupFields.get((String)excludes.next());
                        if (o != null && o instanceof PSField) {
                           tmpExcludes.remove(((PSField) o).getSubmitName());
                        }
                     }
                     break;
                  }
               }

               if (!hasMatch)
               {
                  throw new PSSystemValidationException(
                     IPSObjectStoreErrors.CE_INCLUDED_GROUP_INVALID, include);
               }
            }
         }
      }

      // see if any shared excludes were not found in an included group
      if (tmpExcludes.size() > 0)
      {
         StringBuffer buf = new StringBuffer();
         Iterator excludes = tmpExcludes.iterator();
         if (excludes.hasNext())
            buf.append((String)excludes.next());
         while (excludes.hasNext())
         {
            buf.append(", ");
            buf.append((String)excludes.next());
         }
         Object[] args = {buf.toString()};
         throw new PSSystemValidationException(
            IPSObjectStoreErrors.CE_SHARED_EXCLUDE_INVALID, args);
      }

      return warnings.iterator();
   }

   /**
    * Validates that none of the mapped shared fields is defined in more than
    * one included shared groups. The reason that this validation is needed
    * is due to a problem that the CE shared field doesn't have an explicit refe-
    * rence to a shared group name where it is defined, hence if more than
    * one included shared group defines a duplicate shared field, it then poses
    * a problem to determine which shared group contains a definition of a given
    * shared field.
    *
    * @param sharedDef all server CE shared groups with all shared fields defined
    * @param itSharedGroupIncludes a list of shared group names that a given CE
    * includes as the <SharedFieldIncludes><SharedFieldGroupName>... nodes.
    *
    * @throws PSMinorValidationException in case if there are more then one included
    * shared fields with a duplicate name.
   */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void validateSharedFieldDuplication(PSContentEditorSharedDef sharedDef,
                                              Iterator itSharedGroupIncludes)
      throws PSMinorValidationException
   {
      if (sharedDef==null)
         throw new IllegalArgumentException("sharedDef==null");

      if (itSharedGroupIncludes==null)
         throw new IllegalArgumentException("itSharedGroupIncludes==null");

      PSDisplayMapper dispMapper = getUIDefinition().getDisplayMapper();

      Map mapSharedField2SharedGroups = new HashMap();

      while(itSharedGroupIncludes.hasNext())
      {
         String includedGroupName = (String)itSharedGroupIncludes.next();

         if (includedGroupName==null)
            throw new IllegalStateException("includedGroupName==null");

         for (Iterator fieldGroups = sharedDef.getFieldGroups();
              fieldGroups!=null && fieldGroups.hasNext();)
         {
            PSSharedFieldGroup shfg = (PSSharedFieldGroup)fieldGroups.next();

            if (shfg==null)
               throw new IllegalStateException("shfg==null");

            String sharedGroupName = shfg.getName();

            if (sharedGroupName==null)
               throw new IllegalStateException("sharedGroupName==null");

            if (includedGroupName.compareToIgnoreCase(sharedGroupName)==0)
            {
               PSFieldSet shfgFieldSet = shfg.getFieldSet();

               if (shfgFieldSet==null)
                  throw new IllegalStateException("shfgFieldSet==null");

               Iterator itShfgAllFields = shfgFieldSet.getAll();

               if (itShfgAllFields==null)
                  throw new IllegalStateException("itShfgAllFields==null");

               while(itShfgAllFields.hasNext())
               {
                  PSField psField = (PSField)itShfgAllFields.next();

                  if (psField==null)
                     throw new IllegalStateException("psField==null");

                  String sharedFieldName = psField.getSubmitName();

                  if (dispMapper.getMapping(sharedFieldName)==null)
                     continue; //not mapped, shouldn't matter even if duplicate

                  if (sharedFieldName==null)
                     throw new IllegalStateException("sharedFieldName==null");

                  HashSet<String> sharedGroupNames = (HashSet<String>)
                     mapSharedField2SharedGroups.get(sharedFieldName);

                  if (sharedGroupNames != null)
                  {
                     sharedGroupNames.add(sharedGroupName);

                     if (sharedGroupNames.size() > 1)
                     {
                        //found at least one mapped duplicate shared field
                        //this is enough to raise an ambiguity flag here.
                        String duplicateGroups = "";
                        Iterator it = sharedGroupNames.iterator();
                        while(it.hasNext())
                        {
                           duplicateGroups = duplicateGroups + (String)it.next();

                           if (it.hasNext())
                              duplicateGroups += ", ";
                        }

                        Object args[] = {sharedFieldName, duplicateGroups};

                        throw new PSMinorValidationException(IPSObjectStoreErrors.
                           DUPLICATE_SHARED_FIELD_VALIDATION_WARNING, args);
                     }
                  }
                  else
                  {
                     sharedGroupNames = new HashSet<>();
                     sharedGroupNames.add(sharedGroupName);

                     mapSharedField2SharedGroups.put(sharedFieldName,
                        sharedGroupNames);
                  }

               }//while shared group fields

            }//if included shared group

         }//for all shared field groups

      }//while included shared groups
   }

   /**
    * Validates that all system field excludes defined exists in system fieldset
    *
    * @param systemDef the content editor system def, assumed not to be <code>
    * null</code>
    *
    * @throws PSSystemValidationException if any of the excludes are not defined.
    */
   private void validateSystemFieldExcludes(PSContentEditorSystemDef systemDef)
      throws PSSystemValidationException
   {
      PSFieldSet sysFieldSet = systemDef.getFieldSet();

      List<String> notFound = new ArrayList<>();

      Iterator excludes = m_systemFieldExcludes.iterator();
      while(excludes.hasNext())
      {
         String exclude = (String) excludes.next();
         if( !sysFieldSet.contains(exclude) )
            notFound.add(exclude);
      }

      if(!notFound.isEmpty())
      {
         StringBuffer buf = new StringBuffer();
         excludes = notFound.iterator();
         if (excludes.hasNext())
            buf.append((String)excludes.next());
         while (excludes.hasNext())
         {
            buf.append(", ");
            buf.append((String)excludes.next());
         }
         Object[] args = {buf.toString()};
         throw new PSSystemValidationException(
            IPSObjectStoreErrors.CE_SYSTEM_EXCLUDE_INVALID, args);

      }

   }

   /**
    * Returns a new list with all elements in supplied list converted to upper
    * case.
    *
    * @param list the list of <code>String</code>s, may not be <code>null</code>
    * If it is empty, the returned list will be empty.
    *
    * @return the new converted list, never <code>null</code>
    */
   private List convertToUpper(List list)
   {
      if(list == null)
         throw new IllegalArgumentException("list may not be null.");

      List<String> newList = new ArrayList<>();
      for (int i = 0; i < list.size(); i++)
      {
         Object name = list.get(i);
         if(name instanceof String)
            newList.add( ((String)name).toUpperCase() );
         else
         {
            throw new IllegalArgumentException(
               "The elements in the list must be a String.");
         }
      }
      return newList;
   }

   /**
    * Validates the supplied source type.
    *
    * @param sourceType the source type to validate, may be <code>null</code>
    *    or must be one of <code>SYSTEM</code>, <code>SHARED</code>
    *    or <code>LOCAL</code>.
    */
   public static void validateSourceType(String sourceType)
   {
      if (sourceType == null)
         return;

      for (int i=0; i<SOURCE_TYPES.length; i++)
      {
         if (sourceType.equals(SOURCE_TYPES[i]))
            return;
      }

      throw new IllegalArgumentException("invalid sourceType, type was: " +
         sourceType);
   }
   
   /**
    * Get the list of system mandatory fields that have been added while
    * getting the merged content editor mapper.
    * 
    * @return a collection of system mandatory field names added as 
    *    <code>String</code> objects, never <code>null</code>, may be empty.
    */
   public Collection getAddedSystemMandatoryFields()
   {
      return m_addedSystemMandatoryFields;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXContentEditorMapper";

   /**
    * Used to indicate an element was defined in the system defintion.
    */
   public static final String SYSTEM = "sys_system";

   /**
    * Used to indicate an element was defined or overridden in a shared defintion.
    */
   public static final String SHARED = "sys_shared";

   /**
    * Used to indicate an element was defined or overridden in a local defintion.
    */
   public static final String LOCAL = "sys_local";

   /**
    * An array of all defined source types.
    */
   public static final String[] SOURCE_TYPES =
   {
      SYSTEM,
      SHARED,
      LOCAL
   };

   /**
    * A list of field refs (String) excluded from the system field
    * efinition. Always valid after construction.
    */
   private ArrayList m_systemFieldExcludes = new ArrayList();

   /**
    * A list of field group names (String) included from the shared field
    * definitions. Always valid after construction.
    */
   private ArrayList m_sharedFieldIncludes = new ArrayList();

   /**
    * A list of field refs (String) excluded from any of the include shared
    * groups' field sets. Always valid after construction, may be empty.
    */
   private ArrayList m_sharedFieldExcludes = new ArrayList();

   /** The field set used, always valid after construction. */
   private PSFieldSet m_fieldSet = null;

   /** The UI definition used, always valid after construction. */
   private PSUIDefinition m_uiDefinition = null;
   
   /**
    * A list of system mandatory field names added while getting the merged 
    * content editor mapper as <code>String</code> objects. Reset for each call 
    * to {@link #getMergedMapper(PSContentEditorSystemDef, 
    * PSContentEditorSharedDef, boolean)}, never <code>null</code>, may be 
    * empty.
    * This value is not available in the XML representation of this object nor
    * is it copied during clone / ccopy from actions.
    */
   transient private Collection m_addedSystemMandatoryFields = new ArrayList();

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String SYSTEM_FIELD_EXCLUDES_ELEM = 
      "SystemFieldExcludes";
   private static final String FIELD_REF_ELEM = "FieldRef";
   private static final String SHARED_FIELD_INCLUDES_ELEM = 
      "SharedFieldIncludes";
   private static final String SHARED_FIELD_GROUP_NAME_ELEM = 
      "SharedFieldGroupName";
   private static final String SHARED_FIELD_EXCLUDES_ELEM = 
      "SharedFieldExcludes";
}

