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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implements the PSXContentEditorSharedDef DTD defined in
 * ContentEditorSharedDef.dtd.
 */
@SuppressWarnings("serial")
public class PSContentEditorSharedDef extends PSComponent
      implements
         IPSDocument
{
   /**
    * Creates a new empty shared content editor definition.
    */
   public PSContentEditorSharedDef() {
   }

   /**
    * Creates a new shared content editor definition.
    * 
    * @param fieldGroups a collection of PSSharedFieldGroup objects. Never
    *           <code>null</code>, may be empty.
    */
   public PSContentEditorSharedDef(PSCollection fieldGroups) {
      setFieldGroups(fieldGroups);
   }

   /**
    * Construct a Java object from its XML representation.
    * 
    * @param sourceNode the XML element node to construct this object from, not
    *           <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object, not
    *           <code>null</code>.
    * @param parentComponents the parent objects of this object, not
    *           <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    *            appropriate type
    */
   public PSContentEditorSharedDef(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for this class that takes a source document.
    * 
    * @param sourceDoc The Xml document containing the Content Editor shared
    *           def.
    * 
    * @throws PSUnknownDocTypeException if the XML document is not of the
    *            appropriate type.
    * @throws PSUnknownNodeTypeException if an XML element node is not of the
    *            appropriate type.
    * @see #fromXml(Document)
    */
   public PSContentEditorSharedDef(Document sourceDoc)
         throws PSUnknownDocTypeException, PSUnknownNodeTypeException {
      if (sourceDoc == null)
         throw new IllegalArgumentException("sourceDoc may not be null");

      fromXml(sourceDoc);
   }

   /**
    * Calculates and returns the guid for this object, does not take the 
    * originating filename into account.
    * 
    * @return The guid, the same for all shared def objects, 
    * never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.CONFIGURATION, SHARED_DEF_ID);      
   }
   
   /**
    * Get the command handler stylesheets.
    * 
    * @return the command handler stylesheets, might be <code>null</code>.
    */
   public PSCommandHandlerStylesheets getStylesheetSet()
   {
      return m_stylesheetSet;
   }

   /**
    * Set new command handler stylesheets.
    * 
    * @param stylesheetSet the new command handler stylesheets, might be
    *           <code>null</code>.
    */
   public void setStylesheetSet(PSCommandHandlerStylesheets stylesheetSet)
   {
      m_stylesheetSet = stylesheetSet;
   }

   /**
    * Get the application flow.
    * 
    * @return the application flow, might be <code>null</code>.
    */
   public PSApplicationFlow getApplicationFlow()
   {
      return m_applicationFlow;
   }

   /**
    * Set a new application flow.
    * 
    * @param applicationFlow the new application flow, might be
    *           <code>null</code>.
    */
   public void setApplicationFlow(PSApplicationFlow applicationFlow)
   {
      m_applicationFlow = applicationFlow;
   }

   /**
    * Get the field group collection.
    * 
    * @return a collection of PSSharedFieldGroup objects, never
    *         <code>null</code>, may be empty.
    */
   public Iterator getFieldGroups()
   {
      return m_fieldGroups.iterator();
   }

   /**
    * Looks up and returns the shared field group collection for the given file
    * name.
    * 
    * @param fileName name of the file this group from, must not be
    *           <code>null</code> and case sensitive.
    * @return the shared field corresponding to the file name supplied, never
    *         <code>null</code> may be empty if not found.
    */
   public PSCollection lookupFieldGroupByFileName(String fileName)
   {
      PSCollection fieldGroups = new PSCollection((new PSSharedFieldGroup())
            .getClass());
      Iterator iter = getFieldGroups();
      while (iter.hasNext())
      {
         PSSharedFieldGroup g = (PSSharedFieldGroup) iter.next();
         if (g.getFilename().equals(fileName))
            fieldGroups.add(g);
      }
      return fieldGroups;
   }

   /**
    * Add or replace the shared field groups with the supplied one. Replace is
    * performed only if the group with the file name already exists. Assumes the
    * set in the iterator has the common file name.
    * 
    * @param group shared field group to add or replace. Must not be
    *           <code>null</code>.
    */
   public void setFieldGroupsByFileName(Iterator group)
   {
      if (group == null || !group.hasNext())
         throw new IllegalArgumentException("group must not be null or empty");

      PSSharedFieldGroup first = (PSSharedFieldGroup) group.next();
      PSCollection g = lookupFieldGroupByFileName(first.getFilename());
      Iterator iter = g.iterator();
      while (iter.hasNext())
         m_fieldGroups.remove(iter.next());

      m_fieldGroups.add(first);
      while (group.hasNext())
         m_fieldGroups.add(group.next());
   }

   /**
    * Remove the supplied shared field group from the definition.
    * 
    * @param group shared field group to remove. Must not be <code>null</code>.
    */
   public void removeFieldGroup(PSSharedFieldGroup group)
   {
      if (group == null)
      {
         throw new IllegalArgumentException("group must not be null");
      }
      m_fieldGroups.remove(group);
   }

   /**
    * Set a new collection of field groups.
    * 
    * @param fieldGroups the new collection of PSSharedFieldGroup objects, never
    *           <code>null</code>, may be empty.
    */
   public void setFieldGroups(PSCollection fieldGroups)
   {
      if (fieldGroups == null)
         throw new IllegalArgumentException("the field groups cannot be null");

      if (!fieldGroups.getMemberClassName().equals(
            m_fieldGroups.getMemberClassName()))
         throw new IllegalArgumentException(
               "PSSharedFieldGroup collection expected");

      m_fieldGroups.clear();
      m_fieldGroups.addAll(fieldGroups);
   }

   /**
    * Add a new field group to the existing groups.
    * 
    * @param group the new collection of PSSharedFieldGroup objects, never
    *    <code>null</code>, may be empty.
    */
   public void addFieldGroup(PSSharedFieldGroup group)
   {
      if (group == null)
      {
         throw new IllegalArgumentException("group must not be null");
      }
      m_fieldGroups.add(group);
   }

   /**
    * Get the specified shared group from the def.
    * 
    * @param groupName the name of the group, may not be <code>null</code> or 
    * empty.
    *
    * @return The group, may be <code>null</code> if not found.
    */
   @SuppressWarnings("unchecked")
   public PSSharedFieldGroup getSharedGroup(String groupName)
   {
      if (groupName == null || groupName.trim().length() == 0)
      {
         throw new IllegalArgumentException("groupName may not be null or "
               + "empty");
      }
      
      PSSharedFieldGroup group = null;
      Iterator groups = getFieldGroups();      
      while (groups.hasNext() && group == null) 
      {
         PSSharedFieldGroup test = (PSSharedFieldGroup)groups.next();
         if (groupName.equals(test.getName()))
            group = test;
      }
      
      return group;
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    * 
    * @param c a valid PSContentEditorSharedDef, not <code>null</code>.
    */
   public void copyFrom(PSContentEditorSharedDef c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }

      setApplicationFlow(c.getApplicationFlow());
      setFieldGroups(c.m_fieldGroups);
      setStylesheetSet(c.getStylesheetSet());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentEditorSharedDef)) return false;
      if (!super.equals(o)) return false;
      PSContentEditorSharedDef that = (PSContentEditorSharedDef) o;
      return Objects.equals(m_stylesheetSet, that.m_stylesheetSet) &&
              Objects.equals(m_applicationFlow, that.m_applicationFlow) &&
              Objects.equals(m_fieldGroups, that.m_fieldGroups);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_stylesheetSet, m_applicationFlow, m_fieldGroups);
   }

   // see IPSDocument
   public void fromXml(Document doc) throws PSUnknownDocTypeException,
         PSUnknownNodeTypeException
   {
      if (null == doc)
         throw new PSUnknownDocTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);
      Element sourceNode = doc.getDocumentElement();
      fromXml(sourceNode, null, null);
   }

   /**
    * 
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
         ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args =
         {XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the field groups
         node = tree.getNextElement(PSSharedFieldGroup.XML_NODE_NAME,
               firstFlags);
         while (node != null)
         {
            m_fieldGroups.add(new PSSharedFieldGroup(node, parentDoc,
                  parentComponents));
            node = tree.getNextElement(PSSharedFieldGroup.XML_NODE_NAME,
                  nextFlags);
         }

         // OPTIONAL: get the styesheet set
         node = tree.getNextElement(PSCommandHandlerStylesheets.XML_NODE_NAME,
               nextFlags);
         if (node != null)
            m_stylesheetSet = new PSCommandHandlerStylesheets(node, parentDoc,
                  parentComponents);

         // OPTIONAL: get the application flow
         node = tree.getNextElement(PSApplicationFlow.XML_NODE_NAME, nextFlags);
         if (node != null)
            m_applicationFlow = new PSApplicationFlow(node, parentDoc,
                  parentComponents);
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSDocument
   public Document toXml()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      doc.appendChild(toXml(doc));
      return doc;
   }

   /**
    * 
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);

      // OPTIONAL: shared field groups
      Iterator it = getFieldGroups();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      // OPTIONAL: stylesheet set
      if (getStylesheetSet() != null)
         root.appendChild(getStylesheetSet().toXml(doc));

      // OPTIONAL: application flow
      if (getApplicationFlow() != null)
         root.appendChild(getApplicationFlow().toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
         throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_fieldGroups != null)
         {
            Iterator it = getFieldGroups();
            while (it.hasNext())
               ((IPSComponent) it.next()).validate(context);
         }
         else
            context.validationError(this,
                  IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_SHARED_DEF, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Tests if the supplied field is a shared field or not. If the fields submit
    * name is found in a field contained by this shared def and it is not in the
    * supplied excluded list, then it is considered as a shared field. The check
    * is case sensitive.
    * 
    * @param field the field to test, may not be <code>null</code>.
    * @param groupIncludes the list of shared groups included, may not be
    *           <code>null</code>
    * @param fieldExcludes list of shared field excludes, may not be
    *           <code>null</code>
    * 
    * @return <code>true</code> if the provided field is a shared field,
    *         <code>false</code> otherwise.
    */
   public boolean isSharedField(PSField field, List groupIncludes,
         List fieldExcludes)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");

      if (groupIncludes == null)
         throw new IllegalArgumentException("groupIncludes may not be null");

      if (fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null");

      if (!fieldExcludes.contains(field.getSubmitName()))
      {
         Iterator groups = getFieldGroups();
         while (groups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
            if (groupIncludes.contains(group.getName())
                  && group.getFieldSet().contains(field.getSubmitName()))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Tests if this field set is a shared field set or not. If this field set
    * name is found in names of field sets contained by this shared def and it
    * is found in the list of supplied included shared groups, then it is
    * considered a shared field set. The check is case sensitive.
    * 
    * @param set the fieldset to check, may not be <code>null</code>
    * @param groupIncludes the list of shared group includes, may not be
    *           <code>null</code>
    * @return <code>true</code> if this field set is a shared field set,
    *         <code>false</code> otherwise.
    */
   public boolean isSharedFieldSet(PSFieldSet set, List groupIncludes)
   {
      if (set == null)
         throw new IllegalArgumentException("set may not be null.");

      if (groupIncludes == null)
         throw new IllegalArgumentException("groupIncludes cannot be null");

      Iterator groups = getFieldGroups();
      while (groups.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) groups.next();
         if (groupIncludes.contains(group.getName())
               && group.getFieldSet().getName().equals(set.getName()))
            return true;
      }
      return false;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXContentEditorSharedDef";

   /**
    * The content editor shared id used for locking through web services. See
    * {@link PSContentEditorSystemDef#SYSTEM_DEF_ID} to make sure its unique.
    */
   public static final long SHARED_DEF_ID = 1001;

   /**
    * The command handler stylesheet for this shared definition. Might be
    * <code>null</code>.
    */
   private PSCommandHandlerStylesheets m_stylesheetSet = null;

   /**
    * The application flow for this shared definition. May be <code>null</code>.
    */
   private PSApplicationFlow m_applicationFlow = null;

   /**
    * A collection of PSSharedFieldGroup objects, never <code>null</code>,
    * might be empty after construction.
    */
   private PSCollection m_fieldGroups = new PSCollection(
         (new PSSharedFieldGroup()).getClass());
}
