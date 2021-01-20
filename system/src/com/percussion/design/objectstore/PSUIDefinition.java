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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for the PSXUIDefinition DTD in BasicObjects.dtd.
 */
public class PSUIDefinition extends PSComponent
{
   /**
    * Creates a new UI definition.
    *
    * @param mapper a display mapper, not <code>null</code>.
    */
   public PSUIDefinition(PSDisplayMapper mapper)
   {
      this(mapper, null);
   }

   /**
    * Creates a new UI definition.
    *
    * @param mapper a display mapper, not <code>null</code>.
    * @param defaultUI a collection of PSUISet objects, may be
    *    <code>null</code> but not empty.
    */
   public PSUIDefinition(PSDisplayMapper mapper, PSCollection defaultUI)
   {
      setDisplayMapper(mapper);
      setDefaultUI(defaultUI);
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
   public PSUIDefinition(Element sourceNode, IPSDocument parentDoc,
                         ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSUIDefinition()
   {
   }

   /**
    * Get a collection of default UI sets.
    *
    * @return a list or default UI sets, may be <code>null</code>. If not <code>
    * null</code>, it is never empty.
    */
   public Iterator getDefaultUI()
   {
      return m_defaultUI == null ? null : m_defaultUI.iterator();
   }

   /**
    * Set a new default UI collection.
    *
    * @param defaultUI the new defaul UI collection, may be <code>null</code>
    *    but not empty.
    * @throws IllegalArgumentExcption if the provided defaultUI is
    *    <code>null</code>, empty or of wrong object type.
    */
   public void setDefaultUI(PSCollection defaultUI)
   {
      if (defaultUI != null && defaultUI.isEmpty())
         throw new IllegalArgumentException("defaultUI cannot be empty");

      if (defaultUI != null
            && m_defaultUI != null
            && !defaultUI.getMemberClassName().equals(
                  m_defaultUI.getMemberClassName()))
      {
         throw new IllegalArgumentException(
            "PSUISet collection expected");
      }

      if(defaultUI == null)
         m_defaultUI = null;
      else
      {
         if(m_defaultUI == null)
            m_defaultUI = new PSCollection(new PSUISet().getClass());
         else
            m_defaultUI.clear();
         m_defaultUI.addAll(defaultUI);
      }
   }

   /**
    * Get the display mapper.
    *
    * @return the display mapper, never <code>null</code>.
    */
   public PSDisplayMapper getDisplayMapper()
   {
      return m_displayMapper;
   }

   /**
    * Set a new display mapper.
    *
    * @param displayMapper the display mapper, not <code>null</code>.
    */
   public void setDisplayMapper(PSDisplayMapper displayMapper)
   {
      if (displayMapper == null)
         throw new IllegalArgumentException("mapper cannot be null");

      m_displayMapper = displayMapper;
   }

   /**
    * Appends mapping to the supplied mapper.
    *
    * @param mapper the mapper to append the mapping to, not <code>null</code>
    * @param mapping the mapping to append to the mapper, not
    *    <code>null</code>.
    * @return the index the mapping was appended at.
    */
   public int appendMapping(PSDisplayMapper mapper,
      PSDisplayMapping mapping)
   {
      if (mapper == null || mapping == null)
         throw new IllegalArgumentException(
            "mapper and/or mapping cannot be null");

      mapper.add(mapping);

      return mapper.size()-1;
   }

   /**
    * Inserts the provided mapping into the mapper supplied at the given
    * position.
    *
    * @param index the index to insert the mapping at, must be a valid
    *    index for the provided mapper.
    * @param mapper the mapper to insert the mapping to, not
    *    <code>null</code>.
    * @param mapping the mapping to insert into the mapper, not
    *    <code>null</code>.
    * @throws ArrayIndexOutOfBoundsException if the position is not valid for
    *    the provieded mapper.
    */
   public void insertMapping(int index, PSDisplayMapper mapper,
                             PSDisplayMapping mapping)
   {
      if (mapper == null || mapping == null)
         throw new IllegalArgumentException(
            "mapper and/or mapping cannot be null");

      mapper.insertElementAt(mapping, index);
   }

   /**
    * Peplaces the provided mapping in the mapper supplied at the given
    * position.
    *
    * @param index the index to replace the mapping at, must be a valid
    *    index for the provided mapper.
    * @param mapper the mapper to replace the mapping in, not
    *    <code>null</code>.
    * @param mapping the mapping to replace in the mapper, not
    *    <code>null</code>.
    * @throws ArrayIndexOutOfBoundsException if the position is not valid for
    *    the provieded mapper.
    */
   public void replaceMapping(int index, PSDisplayMapper mapper,
      PSDisplayMapping mapping)
   {
      if (mapper == null || mapping == null)
         throw new IllegalArgumentException(
            "mapper and/or mapping cannot be null");

      mapper.setElementAt(mapping, index);
   }

   /**
    * Replaces the given mapping for the supplied field reference. Goes
    * through all mappings and replaces the one with a matching fieldRew.
    *
    * @param fieldRef the field reference, not <code>null</code> or empty.
    * @param mapping the new mapping which will replace the referenced
    *    mapping, not <code>null</code>.
    */
   public void replaceMapping(
      String fieldRef, PSDisplayMapping mapping)
   {
      if (fieldRef == null || fieldRef.trim().length() == 0)
         throw new IllegalArgumentException("the field ref must not be null or empty");

      if (mapping == null)
         throw new IllegalArgumentException("the mapping cannot be null");

      PSDisplayMapper mapper = getDisplayMapper();
      for (int i=0; i<mapper.size(); i++)
      {
         PSDisplayMapping m = (PSDisplayMapping) mapper.get(i);
         if (m.getFieldRef().equals(fieldRef))
         {
            replaceMapping(i, mapper, mapping);
            return;
         }
      }

      // could not find the mapping referenced
      throw new IllegalArgumentException(
         "could not find the field, nothing replaced");
   }

   /**
    * Get the mapping for the provided field reference.
    *
    * @param fieldRef the field refernence to get the mapping for.
    * @return the mapping found for the provided field
    *    reference or <code>null</code> if not found.
    */
   public PSDisplayMapping getMapping(String fieldRef)
   {
      Iterator it = getDisplayMapper().iterator();
      while(it.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         if (mapping.getFieldRef().equals(fieldRef))
            return mapping;
      }

      return null;
   }

   /**
    * Get the mapper for the supplied name, recursing all mappings. The match
    * is done as a case sensitive compare of the field set name against the
    * supplied name.
    *
    * @param the mapper fieldSetRef we are looking for
    * @return the mapper found for the provieded id or <code>null</code> if
    *    not found.
    */
   public PSDisplayMapper getDisplayMapper(String fieldSetRef)
   {
      PSDisplayMapper mapper = getDisplayMapper();
      if (mapper.getFieldSetRef().equals(fieldSetRef))
         return mapper;

      return getDisplayMapper(fieldSetRef, mapper);
   }

   /**
    * Get the display mapper for the provided name and display mapper. Checks
    * the supplied mapper and all contained mappers recursively.
    *
    * @param fieldSetRef the name of the display mapper we are looking for.
    * @param mapper the display mapper we are looking in, may be
    *    <code>null</code>.
    * @return the display mapper found or <code>null</code>
    *    if not found.
    */
   private PSDisplayMapper getDisplayMapper(String fieldSetRef,
      PSDisplayMapper mapper)
   {
      if (mapper == null)
         return null;

      PSDisplayMapper found = null;
      Iterator it = mapper.iterator();
      while(it.hasNext() && found == null)
      {
         PSDisplayMapping mapping = (PSDisplayMapping)it.next();
         if (mapper != null && mapper.getFieldSetRef().equals(fieldSetRef))
            found = mapper;
         else
            found = getDisplayMapper(fieldSetRef, mapping.getDisplayMapper());
      }

      return found;
   }

   /**
    * Get the mapper for the supplied id, recursing all mappings.
    *
    * @param the mapper id we are looking for.
    * @return the mapper found for the provieded id or <code>null</code> if
    *    not found.
    */
   public PSDisplayMapper getDisplayMapper(int id)
   {
      PSDisplayMapper mapper = getDisplayMapper();
      if (mapper.getId() == id)
         return mapper;

      return getDisplayMapper(id, mapper);
   }

   /**
    * Get the display mapper for the provided id and display mapper.
    *
    * @param id the id of the display mapper we are looking for.
    * @param mapper the display mapper we are looking in, may be
    *    <code>null</code>.
    * @return the display mapper found or <code>null</code>
    *    if not found.
    */
   private PSDisplayMapper getDisplayMapper(int id , PSDisplayMapper mapper)
   {
      if (mapper == null)
         return null;

      PSDisplayMapper found = null;
      Iterator it = mapper.iterator();
      while(it.hasNext() && found == null)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         PSDisplayMapper test = mapping.getDisplayMapper();
         if (mapper != null && mapper.getId() == id)
            found = mapper;
         else
            found = getDisplayMapper(id, test);
      }

      return found;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSUIDefinition, not <code>null</code>.
    */
   public void copyFrom(PSUIDefinition c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      m_defaultUI = c.m_defaultUI;
      setDisplayMapper(c.getDisplayMapper());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSUIDefinition)) return false;
      if (!super.equals(o)) return false;
      PSUIDefinition that = (PSUIDefinition) o;
      return Objects.equals(m_defaultUI, that.m_defaultUI) &&
              Objects.equals(m_displayMapper, that.m_displayMapper);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_defaultUI, m_displayMapper);
   }

   // see IPSComponent
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

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      int mapperFlags = firstFlags;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         m_defaultUI = null;

         // OPTIONAL: get the default UI
         node = tree.getNextElement(DEFAULT_UI_ELEM, firstFlags);
         if (node != null)
         {
            Node current = tree.getCurrent();

            node = tree.getNextElement(PSUISet.XML_NODE_NAME, firstFlags);
            // if the default is defined we need at least one element
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  DEFAULT_UI_ELEM,
                  "empty"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_defaultUI = new PSCollection(new PSUISet().getClass());
            while(node != null)
            {
               m_defaultUI.add(new PSUISet(node, parentDoc, parentComponents));
               node = tree.getNextElement(PSUISet.XML_NODE_NAME, nextFlags);
            }

            tree.setCurrent(current);
            mapperFlags = nextFlags; // mapper is sibling of PSXUISet
         }

         // REQUIRED: get display mapper
         node = tree.getNextElement(PSDisplayMapper.XML_NODE_NAME, mapperFlags);
         if (node != null)
         {
            m_displayMapper = new PSDisplayMapper(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSDisplayMapper.XML_NODE_NAME,
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
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);

      // OPTIONAL: create the default UI
      Iterator it = getDefaultUI();
      if(it != null)
      {
         if (it.hasNext())
         {
            Element elem = doc.createElement(DEFAULT_UI_ELEM);
            while (it.hasNext())
               elem.appendChild(((IPSComponent) it.next()).toXml(doc));
            root.appendChild(elem);
         }
      }

      // REQUIRED: create the display mapper
      root.appendChild(m_displayMapper.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_displayMapper != null)
            m_displayMapper.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_UI_DEFINITION, null);

         Iterator it = getDefaultUI();
         while (it.hasNext())
            ((IPSComponent) it.next()).validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Gets a copy of this UI definition merged with supplied source UI
    * definition. Merges/adds the mappings corresponding to all the fields of
    * the source fieldset exlcuding the fields defined in <code>fieldExcludes
    * </code>.
    *
    * @param sourceUIDef the source definition to be merged with, may not be
    * <code>null</code>
    * @param mergeChild if <code>true</code> merges the display mapper supplied
    * in sourceUIDef with the child mapper of matching mapping in the main
    * display mapper of this UI Definition, otherwise with the main display
    * mapper.
    * @param fieldExcludes the list of fields as <code>String</code>s to be
    * excluded while merging, may not be <code>null</code>, can be empty.
    * @param mergeDefault if <code>true</code> the UI set provided for
    * placeholder of source field mapping in this definition is merged with its
    * default UI set before merging with the UI set defined in the source
    * definition(the UI set merged with its default), otherwise it simply merges
    * with UI set defined in the source definition.
    *
    * @return the merged UI definition, never <code>null</code>
    *
    * @throws PSValidationException if an error happens in merging.
    */
   public PSUIDefinition merge(PSUIDefinition sourceUIDef, boolean mergeChild,
      List fieldExcludes, boolean mergeDefault)
      throws PSValidationException
   {
      if(sourceUIDef == null)
         throw new IllegalArgumentException("sourceUIDef may not be null.");

      if(fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null.");

      PSDisplayMapper localMapper = getDisplayMapper();

      localMapper = localMapper.merge(mergeDefault, getDefaultUI(),
            sourceUIDef.getDisplayMapper(), sourceUIDef.getDefaultUI(),
            mergeChild, fieldExcludes);

      PSUIDefinition uiDef = null;
      if( getDefaultUI() == null )
         uiDef = new PSUIDefinition(localMapper);
      else
      {
         uiDef = new PSUIDefinition(localMapper,
            new PSCollection(getDefaultUI()));
      }
      return uiDef;
   }

   /**
    * Gets a copy of this UI definition demerged with supplied source UI
    * definition. Demerges the mappings corresponding to all the fields of
    * the source fieldset excluding the fields defined in <code>fieldExcludes
    * </code>.
    *
    * @param sourceUIDef the source definition to demerge from, may not be
    * <code>null</code>
    * @param demergeChild if <code>true</code> demerges the display mapper
    * supplied in sourceUIDef from the child mapper of the matching mapping in
    * the main display mapper of this UI Definition, otherwise with the main
    * display mapper.
    * @param fieldExcludes the list of fields as Strings to be ignored while
    * demerging, may not be <code>null</code>, can be empty.
    * @param demergeDefault supply <code>true</code> if the UI set of the merged
    * source field mapping in this definition is merged with its Default UI set
    * before merging with UI set defined in source definition(the UI set merged
    * with its default), otherwise supply <code>false</code>
    *
    * @return the demerged UI definition, never <code>null</code>
    *
    * @throws PSValidationException if an error happens in merging.
    */
   public PSUIDefinition demerge(PSUIDefinition sourceUIDef,
      boolean demergeChild, List fieldExcludes, boolean demergeDefault)
      throws PSValidationException
   {
      if(sourceUIDef == null)
         throw new IllegalArgumentException("sourceUIDef may not be null.");

      if(fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null.");

      PSDisplayMapper localMapper = getDisplayMapper();

      localMapper = localMapper.demerge(demergeDefault, getDefaultUI(),
            sourceUIDef.getDisplayMapper(), sourceUIDef.getDefaultUI(),
            demergeChild, fieldExcludes);

      PSUIDefinition uiDef = null;
      if( getDefaultUI() == null )
         uiDef = new PSUIDefinition(localMapper);
      else
      {
         uiDef = new PSUIDefinition(localMapper,
            new PSCollection(getDefaultUI()));
      }
      return uiDef;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXUIDefinition";

   /**
    * A collection of PSUISet objects, gets initialized when this object is
    * read from xml, may be <code>null</code> if the xml does not define this
    * element. If it is not <code>null</code> it is never empty.
    */
   private PSCollection m_defaultUI = null;

   /** The display mapper, never <code>null</code> after construction. */
   private PSDisplayMapper m_displayMapper = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String DEFAULT_UI_ELEM = "DefaultUI";
}

