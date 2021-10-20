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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation for the PSXSharedFieldGroup DTD in
 * ContentEditorSharedDef.dtd.
 */
@SuppressWarnings("serial")
public class PSSharedFieldGroup extends PSComponent
{
   /**
    * Creates a new shared field group.
    *
    * @param name the name of thiw field group, not <code>null</code> or empty.
    * @param locator the container locator, not <code>null</code>.
    * @param fieldSet the fieldSet, not <code>null</code>.
    * @param uiDefinition the UI definition, not <code>null</code>.
    */
   public PSSharedFieldGroup(String name, PSContainerLocator locator,
                             PSFieldSet fieldSet, PSUIDefinition uiDefinition)
   {
      setName(name);
      setLocator(locator);
      setFieldSet(fieldSet);
      setUIDefinition(uiDefinition);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSSharedFieldGroup(Element sourceNode, IPSDocument parentDoc,
                             List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSSharedFieldGroup()
   {
   }

   /**
    * Creates an empty shared field group with the supplied file name.
    * Appropriate data must be set before the object is persistable.
    * 
    * @param name name of the group, must not be <code>null</code> or empty.
    * fileName
    * @param fileName of the file for this group, must not be <code>null</code>
    */
   public PSSharedFieldGroup(String name, String fileName)
   {
      if (name == null || name.length() == 0)
      {
         throw new IllegalArgumentException("name must not be null or empty");
      }
      if (fileName == null || fileName.length() == 0)
      {
         throw new IllegalArgumentException(
            "fileName must not be null or empty");
      }
      m_name = name;
      m_filename = fileName;
   }

   /**
    * Get the name of this field group.
    *
    * @return the field group name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the new name of this field group.
    *
    * @param name the new name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("the name cannot be null or empty");

      m_name = name;
   }
   
   /**
    * Get the name of the file that stores this shared field group.
    * 
    * @return the filename, may be <code>null</code>, never empty.
    */
   public String getFilename()
   {
      return m_filename;
   }
   
   /**
    * Set the name of the file that stores this shared field group.
    * 
    * @param filename the filename, may be <code>null</code>, not empty.
    */
   public void setFilename(String filename)
   {
      if (filename != null && filename.trim().length() == 0)
         throw new IllegalArgumentException("filename cannot be empty");
      
      m_filename = filename;
   }

   /**
    * Get the container locator of this shared field group.
    *
    * @return the container locator,
    *    never <code>null</code>.
    */
   public PSContainerLocator getLocator()
   {
      return m_locator;
   }

   /**
    * Set the new container locator.
    *
    * @param locator the new container locator, not <code>null</code>.
    */
   public void setLocator(PSContainerLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("the locator cannot be null");

      m_locator = locator;
   }

   /**
    * Get the field set of this shared group.
    *
    * @return the field set, never <code>null</code>.
    */
   public PSFieldSet getFieldSet()
   {
      return m_fieldSet;
   }

   /**
    * Set a new field set for this group.
    *
    * @param fieldSet the new field set, never <code>null</code>.
    */
   public void setFieldSet(PSFieldSet fieldSet)
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("the fieldSet cannot be null");

      m_fieldSet = fieldSet;
   }

   /**
    * Get the UI definition of this group.
    *
    * @return the UI definition, never <code>null</code>.
    */
   public PSUIDefinition getUIDefinition()
   {
      return m_uiDefinition;
   }

   /**
    * Set a new UI definition for this group.
    *
    * @param uiDefinition the new UI definition, not <code>null</code>.
    */
   public void setUIDefinition(PSUIDefinition uiDefinition)
   {
      if (uiDefinition == null)
         throw new IllegalArgumentException("the uiDefinition cannot be null");

      m_uiDefinition = uiDefinition;
   }

   /**
    * Get the validation rules for this group.
    *
    * @return the validation rules, never <code>null</code>, might
    *    be empty.
    */
   public Iterator getValidationRules()
   {
      return m_validationRules.iterator();
   }

   /**
    * Set the validation rules for this group.
    *
    * @param validationRules the new validation rules, might be
    *    <code>null</code> or empty.
    */
   public void setValidationRules(PSValidationRules validationRules)
   {
      if (validationRules == null)
         m_validationRules.clear();
      else
         m_validationRules = validationRules;
   }

   /**
    * Get the input translations for this group.
    *
    * @return the input translations, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getInputTranslations()
   {
      return m_inputTranslations.iterator();
   }

   /**
    * Set new input translations for this group.
    *
    * @param inputTranslations the new input translation, might be
    *    <code>null</code> or empty.
    */
   public void setInputTranslations(PSInputTranslations inputTranslations)
   {
      if (inputTranslations == null)
         m_inputTranslations.clear();
      else
         m_inputTranslations = inputTranslations;
   }

   /**
    * Get the output translations of this group.
    *
    * @return the output translations, never <code>null</code>,
    *    might be empty.
    */
   public Iterator getOutputTranslations()
   {
      return m_outputTranslations.iterator();
   }

   /**
    * Set new output translations for this group.
    *
    * @param outputTranslations the new output translations, might be
    *    <code>null</code> or empty
    */
   public void setOutputTranslations(PSOutputTranslations outputTranslations)
   {
      if (outputTranslations == null)
         m_outputTranslations.clear();
      else
         m_outputTranslations = outputTranslations;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSSharedFieldGroup, not <code>null</code>.
    */
   public void copyFrom(PSSharedFieldGroup c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }

      setFieldSet(c.getFieldSet());
      setInputTranslations(c.m_inputTranslations);
      setLocator(c.getLocator());
      setName(c.getName());
      setOutputTranslations(c.m_outputTranslations);
      setUIDefinition(c.getUIDefinition());
      setValidationRules(c.m_validationRules);
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSSharedFieldGroup))
         return false;

      PSSharedFieldGroup t = (PSSharedFieldGroup) o;

      boolean equal = true;
      if (!compare(m_fieldSet, t.m_fieldSet))
         equal = false;
      else if (!compare(m_inputTranslations, t.m_inputTranslations))
         equal = false;
      else if (!compare(m_locator, t.m_locator))
         equal = false;
      else if (!compare(getName(), t.getName()))
         equal = false;
      else if (!compare(m_outputTranslations, t.m_outputTranslations))
         equal = false;
      else if (!compare(m_uiDefinition, t.m_uiDefinition))
         equal = false;
      else if (!compare(m_validationRules, t.m_validationRules))
         equal = false;

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_name).toHashCode();
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
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

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the name attribute
         m_name = tree.getElementData(NAME_ATTR);
         if (m_name == null || m_name.trim().length() == 0)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // OPTIONAL: get the filename attribute
         String test = tree.getElementData(FILENAME_ATTR);
         if (test != null && test.trim().length() > 0)
            setFilename(test.trim());
         
         // REQUIRED: get the container locator
         node = tree.getNextElement(
            PSContainerLocator.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSContainerLocator.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         m_locator = new PSContainerLocator(node, parentDoc, parentComponents);

         // REQUIRED: get the field set
         node = tree.getNextElement(PSFieldSet.XML_NODE_NAME, nextFlags);
         if (node == null)
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
         m_fieldSet = new PSFieldSet(node, parentDoc, parentComponents);
         m_fieldSet.setSourceType( PSField.TYPE_SHARED );

         // REQUIRED: get the UI definition
         node = tree.getNextElement(PSUIDefinition.XML_NODE_NAME, nextFlags);
         if (node == null)
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
         m_uiDefinition = new PSUIDefinition(node, parentDoc, parentComponents);

         // OPTIONAL: get the validation rules
         node = tree.getNextElement(PSValidationRules.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_validationRules = new PSValidationRules(
               node, parentDoc, parentComponents);

         // OPTIONAL: get the input translations
         node = tree.getNextElement(PSInputTranslations.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_inputTranslations = new PSInputTranslations(
               node, parentDoc, parentComponents);

         // OPTIONAL: get the validation rules
         node = tree.getNextElement(PSOutputTranslations.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_outputTranslations = new PSOutputTranslations(
               node, parentDoc, parentComponents);
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
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      if (getFilename() != null)
         root.setAttribute(FILENAME_ATTR, m_filename);

      // REQUIRED: the container locator
      root.appendChild(m_locator.toXml(doc));

      // REQUIRED: the FieldSet
      root.appendChild(m_fieldSet.toXml(doc));

      // REQUIRED: the UI definition
      root.appendChild(m_uiDefinition.toXml(doc));

      // OPTIONAL: the validation rules
      if (m_validationRules != null)
         root.appendChild(m_validationRules.toXml(doc));

      // OPTIONAL: the input translations
      if (m_inputTranslations != null)
         root.appendChild(m_inputTranslations.toXml(doc));

      // OPTIONAL: the output translations
      if (m_outputTranslations != null)
         root.appendChild(m_outputTranslations.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_name == null || m_name.trim().length() == 0)
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_SHARED_FIELD_GROUP, null);

      // do children
      context.pushParent(this);
      try
      {
         if (m_locator != null)
            m_locator.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_SHARED_FIELD_GROUP, null);

         if (m_fieldSet != null)
            m_fieldSet.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_SHARED_FIELD_GROUP, null);

         if (m_uiDefinition != null)
            m_uiDefinition.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_SHARED_FIELD_GROUP, null);

         if (m_validationRules != null)
            m_validationRules.validate(context);

         if (m_inputTranslations != null)
            m_inputTranslations.validate(context);

         if (m_outputTranslations != null)
            m_outputTranslations.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXSharedFieldGroup";

   /**
    * The name of this shared field group, never <code>null</code> or empty
    * after construction.
    */
   private String m_name = null;
   
   /**
    * The name of the file storing this shared field group, may be 
    * <code>null</code>, never empty.
    */
   private String m_filename = null;

   /**
    * The conatiner locator of this shared field group, never
    * <code>null</code> after construction.
    */
   private PSContainerLocator m_locator = null;

   /**
    * The field set for this shared group, never <code>null</code> after
    * construction.
    */
   private PSFieldSet m_fieldSet = null;

   /**
    * The UI definition of this shared group, never <code>null</code> after
    * construction.
    */
   private PSUIDefinition m_uiDefinition = null;

   /**
    * The field validation rules, never <code>null</code>, might be empty.
    */
   private PSValidationRules m_validationRules = new PSValidationRules();

   /**
    * The field input translations, never <code>null</code>, might be empty.
    */
   private PSInputTranslations m_inputTranslations =
      new PSInputTranslations();

   /**
    * The field output translations, never <code>null</code>, might be empty.
    */
   private PSOutputTranslations m_outputTranslations =
      new PSOutputTranslations();

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String FILENAME_ATTR = "filename";
}

