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

import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation for the PSXDisplayMapper DTD in BasicObjects.dtd.
 */
public class PSDisplayMapper extends PSCollectionComponent
{
   /**
    * Creates a new display mapper collection (a collection of
    * PSDisplayMapping objects) for the provided class name.
    *
    * @param fieldSetRef the name of an existing field set, not
    *    <code>null</code> or empty.
    */
   public PSDisplayMapper(String fieldSetRef)
   {
      this();
      setFieldSetRef(fieldSetRef);
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
    * @throws ClassCastException if the class could be found.
    */
   public PSDisplayMapper(Element sourceNode, IPSDocument parentDoc,
                          ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSDisplayMapper()
   {
      super( PSDisplayMapping.class );
   }


   /**
    * @return a deep-copy clone of this vector.
    */
   public synchronized Object clone()
   {
      PSDisplayMapper copy = (PSDisplayMapper) super.clone();
      copy.removeAllElements();
      for (int i = 0; i < size(); i++)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) elementAt( i );
         copy.add( i, mapping.clone() );
      }
      return copy;
   }


   /**
    * Get the name of the field set reference.
    *
    * @return the name of a field set reference, never
    *    <code>null</code> or empty.
    */
   public String getFieldSetRef()
   {
      return m_fieldSetRef;
   }

   /**
    * Set the new field set reference name.
    *
    * @param fieldSetRef the new field set reference name, never
    *    <code>null</code> or empty.
    */
   public void setFieldSetRef(String fieldSetRef)
   {
      if (fieldSetRef == null || fieldSetRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "field set reference annot be null or empty");

      m_fieldSetRef = fieldSetRef;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSDisplayMapper, not <code>null</code>.
    */
   public void copyFrom(PSDisplayMapper c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setFieldSetRef(c.getFieldSetRef());
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
      if (!(o instanceof PSDisplayMapper))
         return false;

      PSDisplayMapper t = (PSDisplayMapper) o;

      boolean equal = true;
      if (!PSComponent.compare(m_fieldSetRef, t.m_fieldSetRef))
         equal = false;

      if (size() != t.size())
         equal = false;
      else
      {
         for (int i=0; i<size() && equal; i++)
         {
            IPSComponent c1 = (IPSComponent) get(i);
            IPSComponent c2 = (IPSComponent) t.get(i);
            if (!PSComponent.compare(c1, c2))
               equal = false;
         }
      }

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      //AP: does not feel right - equals is completely redefined, but
      //parent's hashCode is called.  
      return super.hashCode();
   }

   /**
    *
    * @see IPSComponent
    */
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

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the ID attribute
         data = tree.getElementData(PSComponent.ID_ATTR);
         try
         {
           m_id = Integer.parseInt(data);
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         // REQUIRED: get the fieldSetRef attribute
         m_fieldSetRef = tree.getElementData(FIELD_SET_REF_ATTR);
         if (m_fieldSetRef == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               FIELD_SET_REF_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

         node = tree.getNextElement(
            PSDisplayMapping.XML_NODE_NAME, firstFlags);

         // get all mappings
         while (node != null)
         {
            add(new PSDisplayMapping(node, parentDoc, parentComponents));

            node = tree.getNextElement(
               PSDisplayMapping.XML_NODE_NAME, nextFlags);
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
      root.setAttribute(PSComponent.ID_ATTR, Integer.toString(m_id));
      root.setAttribute(FIELD_SET_REF_ATTR, m_fieldSetRef);

      /*
       * TODO: the display mapper contains mappings  which can contain other
       * mappers. Stop the toXml if we already processed the mapper in the
       * mapping (keep a map of mappers processed).
       */

      // create the mappings
      Iterator it = iterator();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (getId() == 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DISPLAY_MAPPER, null);
      }

      if (m_fieldSetRef == null || m_fieldSetRef.trim().length() == 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_DISPLAY_MAPPER, null);
      }

      super.validate(context);
   }

   /**
    * Gets the mapping for the provided field reference recursively checking
    * this mapper and all child mappers of this mapper's mappings. The check is
    * case sensitive.
    *
    * @param fieldRef the field reference to get the mapping for, may not be
    * <code>null</code> or empty.
    *
    * @return the mapping found for the provided field
    *    reference or <code>null</code> if not found.
    */
   public PSDisplayMapping getMapping(String fieldRef)
   {
      PSPair<PSDisplayMapping, Integer> pair = getMappingAndSequence(fieldRef);
      return pair == null ? null : pair.getFirst();
   }

  /**
   * Gets the mapping for the provided field reference recursively checking
   * this mapper and all child mappers of this mapper's mappings. The check is
   * case sensitive.
   *
   * @param fieldRef the field reference to get the mapping for, may not be
   * <code>null</code> or empty.
   *
   * @return a pair, where the 1st value is the mapping found for the provided 
   * field reference and 2nd value is the position of the mapping (0 based).
   * It may be <code>null</code> if not found.
   */
   public PSPair<PSDisplayMapping, Integer> getMappingAndSequence(
         String fieldRef)
   {
      if(fieldRef == null || fieldRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldRef may not be null or empty.");

      PSDisplayMapping matchedMapping = null;

      int sequence = 0;
      Iterator it = iterator();
      while(it.hasNext() && matchedMapping == null)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         if (mapping.getFieldRef().equals(fieldRef))
            matchedMapping = mapping;
         else if(mapping.getDisplayMapper() != null)
         {
            matchedMapping = mapping.getDisplayMapper().getMapping(fieldRef);
         }
         sequence++;
      }
      
      if (matchedMapping == null)
         return null;
      else 
         return new PSPair<>(matchedMapping, sequence);
   }

   /**
    * Checks the mapping for the provided field reference recursively checking
    * this mapper and all child mappers of this mapper's mappings and removes
    * that mapping from the corresponding display mapper. The check is
    * case sensitive.
    *
    * @param fieldRef the field reference of the mapping to remove, may not be
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if it finds and removes the mapping, otherwise
    * <code>false</code>
    */
   public boolean removeMapping(String fieldRef)
   {
      if(fieldRef == null || fieldRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldRef may not be null or empty.");

      boolean removed = false;
      Iterator it = iterator();
      while(it.hasNext() && !removed)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         if ( mapping.getFieldRef().equals(fieldRef) )
         {
            it.remove();
            removed = true;
         }
         else if(mapping.getDisplayMapper() != null)
         {
            removed = mapping.getDisplayMapper().removeMapping(fieldRef);
         }
      }

      return removed;
   }

   /**
    * Replaces the given mapping with the mapping matching the field reference
    * of supplied mapping. Goes through all mappings and replaces the one with a
    * matching field reference. The check is case sensitive.
    *
    * @param mapping the new mapping which will replace the referenced
    *    mapping, not <code>null</code>.
    */
   public void replaceMapping(PSDisplayMapping mapping)
   {
      if (mapping == null)
         throw new IllegalArgumentException("the mapping cannot be null");

      String fieldRef = mapping.getFieldRef();

      for (int i=0; i<size(); i++)
      {
         PSDisplayMapping m = (PSDisplayMapping)get(i);
         if (m.getFieldRef().equals(fieldRef))
         {
            setElementAt(mapping, i);
            return;
         }
      }

      // could not find the corresponding mapping referencing the same field
      throw new IllegalArgumentException(
         "could not find the matching mapping, nothing replaced");
   }

   /**
   * Gets a copy of this mapper merged with all mappings of the source def
   * except the mappings that refer to the fields in the excluded list. If
   * this mapper does not have a place holder for a mapping for the included
   * field, that mapping is added to the mapper. If the mappings have child
   * mappers they are recursively merged.
   * <br>
   * The following steps are taken for merging UI set of a mapping.
   * <ol>
   * <li>The source mapping has any missing values applied from it's
   * UIDef's default uiset</li>
   * <li>The placeholder mapping in this mapper has any missing values applied
   * from provided default uiset if it exists and <code>mergeDefault</code> is
   * <code>true</code> </li>
   * <li>The combined placeholder mapping has any missing values applied
   * from the combined source mapping</li>
   * <li>If there is no placeholder mapping, then the combined source mapping
   * is added to this mapper.</li>
   * </ol>
   *
   * @param mergeDefault if <code>true</code> merges with provided default
   * uiset before merging with source mapping, otherwise it does not merge
   * with default uiset.
   * @param defaultUISet The default UI set to merge with, may be <code>null
   * </code>
   * @param sourceMapper the source display mapper to merge with, may not be
   * <code>null</code>
   * @param sourceDefaultUISet the source default UI set, may be <code>null
   * </code>
   * @param mergeChild if <code>true</code> merges the source display mapper
   * with the child mapper of matching mapping in this display mapper, otherwise
   * with this display mapper.
   * @param fieldExcludes the list of fields as Strings to be ignored while
   * demerging, may not be <code>null</code>, can be empty.
   *
   * @return the copy of this mapper merged with provided source mapper, never
   * <code>null</code>.
   *
   * @throws PSSystemValidationException if there are any errors.
   */
   public PSDisplayMapper merge(boolean mergeDefault, Iterator defaultUISet,
      PSDisplayMapper sourceMapper, Iterator sourceDefaultUISet,
      boolean mergeChild, List fieldExcludes)
      throws PSSystemValidationException
   {
      if(sourceMapper == null)
         throw new IllegalArgumentException("sourceMapper may not be null.");

      if(fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null.");

      PSDisplayMapper mergedMapper = new PSDisplayMapper( getFieldSetRef() );
      mergedMapper.copyFrom(this);

      ArrayList usedMappings = new ArrayList();

      /* first walk this mapper and merge any fields from source
       * def that are used.
       */
      mergeMapper(mergedMapper, mergeDefault, defaultUISet, fieldExcludes,
         sourceMapper, sourceDefaultUISet, mergeChild, usedMappings);

      /* now add any mappings that were not already promoted */
      PSDisplayMapper mapper = removeExcludedMappings(
         sourceMapper, fieldExcludes);

      /* If we are merging with child, then mapper should have local child
       * mapping and that field set reference should be in the used list.
       */
      if (mergeChild)
      {
         String fieldRef = mapper.getFieldSetRef();
         if (!usedMappings.contains(fieldRef))
         {
            /* No placeholder in this mapper for merging with child mapper.
             */
            throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_UNUSED_MAPPER, fieldRef);
         }
      }
      else
      {
         //add mappings that are not already there.
         Iterator mappings = mapper.iterator();
         while (mappings.hasNext())
         {
            PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
            String fieldRef = mapping.getFieldRef();
            if (!usedMappings.contains(fieldRef))
               addMapping(mergedMapper, sourceDefaultUISet, mapping);
         }
      }

      //As the added mappers have '-1' as mapper id fix all here now.
      int nextId = mergedMapper.getHighestMapperId();
      mergedMapper.setMapperIds( ++nextId );

      return mergedMapper;
   }

   /**
   * Gets a copy of this mapper demerged with all mappings of the source def
   * that have placeholders in this mapper. If the mappings have child
   * mappers they are recursively demerged.
   * <br>
   * The following steps are taken for demerging UI set of a mapping.
   * <ol>
   * <li>The source mapping has any missing values applied from it's
   * UIDef's default uiset</li>
   * <li>The place holder mapping on this mapper demerged with source mapping
   * </li>
   * <li>The resulted placeholder mapping demerged from provided default uiset
   * if <code>demergeDefault</code> is <code>true</code> </li>
   * <li>Ultimately the mapping will have a uiset that has overridden attributes
   * from source and its default. Even though the uiset has no overridden
   * attributes the mapping is kept as a place holder.
   * </ol>
   *
   * @param demergeDefault if <code>true</code> demerges from provided default
   * uiset after demerging from source mapping, otherwise it does not demerge
   * from default uiset.
   * @param defaultUISet The default UI set to demerge from, may be <code>null
   * </code>
   * @param sourceMapper the source display mapper to demerge from, may not be
   * <code>null</code>
   * @param sourceDefaultUI the source default UI set, may be <code>null
   * </code>
   * @param demergeChild if <code>true</code> demerges the source display mapper
   * from the child mapper of the matching mapping in this display mapper,
   * otherwise with this display mapper.
   * @param fieldExcludes the list of fields as Strings to be ignored while
   * demerging, may not be <code>null</code>, can be empty.
   *
   * @return the copy of this mapper demerged from provided source mapper, never
   * <code>null</code>.
   *
   * @throws PSSystemValidationException if there are any errors.
   */
   public PSDisplayMapper demerge(boolean demergeDefault, Iterator defaultUISet,
      PSDisplayMapper sourceMapper, Iterator sourceDefaultUI,
      boolean demergeChild, List fieldExcludes)
      throws PSSystemValidationException
   {
      if(sourceMapper == null)
         throw new IllegalArgumentException("sourceMapper may not be null.");

      if(fieldExcludes == null)
         throw new IllegalArgumentException("fieldExcludes may not be null.");

      PSDisplayMapper demergedMapper = new PSDisplayMapper( getFieldSetRef() );
      demergedMapper.copyFrom(this);

      //demerge the mapper from provided source
      demergeMapper(demergedMapper, demergeDefault, defaultUISet, fieldExcludes,
         sourceMapper, sourceDefaultUI, demergeChild);

      return demergedMapper;
   }

   /**
    * Merges any mapping from the source def that has a place holder in the
    * supplied mapper.  Handles overlaying of the default uiset values in the
    * process.  Does not modify mappings that are not referencing source
    * mappings.  Will recurse and promote/merges mappings in any child mappers.
    *
    * @param mergeDefault if <code>true</code> merges with provided target
    * default uiset before merging with source mapping, otherwise it does not
    * merge with default uiset.
    * @param targetDisplayMapper The local def Mapper to walk and promote any
    * uiset required, assumed not to be <code>null</code>
    * @param targetDefaultUISet The target default UI set to merge with, may be
    * <code>null</code>
    * @param sourceMapper the source display mapper to merge with, assumed not
    * <code>null</code>
    * @param sourceDefaultUISet the source default UI set, may be <code>null
    * </code>
    * @param mergeChild if <code>true</code> merges the source display mapper
    * with the child mapper of matching mapping in the target display mapper,
    * otherwise with the target display mapper.
    * @param fieldExcludes the list of fields as <code>String</code>s to be
    * ignored while merging, assumed not to be <code>null</code>, can be empty.
    * @param usedMappings List of mapping names that needs to be added from
    * source mapper. Will be updated with any mappings used from the source def.
    * Assumed not to be <code>null</code> and empty.
    *
    * @throws PSSystemValidationException if there are any errors.
    */
   private void mergeMapper(PSDisplayMapper targetDisplayMapper,
      boolean mergeDefault, Iterator targetDefaultUISet, List fieldExcludes,
      PSDisplayMapper sourceMapper, Iterator sourceDefaultUISet,
      boolean mergeChild, ArrayList usedMappings)
      throws PSSystemValidationException
   {
      /* walk the mapper, and for any "placeholders", create a new merged
       * version and add it to a replacement list.  Then we'll walk the
       * replacement list and replace the actual mappings.  This is so we don't
       * modify the mapper under the iterator while we walk it.
       */
      ArrayList replacementMappings = new ArrayList();

      Iterator mappings = targetDisplayMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();

         //This might be local mapping having same name
         if(fieldExcludes.contains(fieldRef))
            continue;

         // get the matching source mapping
         PSDisplayMapping sourceMapping = null;
         if (sourceMapper != null)
         {
            PSDisplayMapper childMapper = removeExcludedMappings(
               sourceMapper, fieldExcludes);

            if (fieldRef.equalsIgnoreCase(childMapper.getFieldSetRef()) &&
                mergeChild)
            {
               // add the group's mapper as a child mapper to this mapping
               PSDisplayMapping tmpMapping = new PSDisplayMapping(
                  fieldRef, mapping.getUISet());

               if(mapping.getDisplayMapper() != null)
               {
                  tmpMapping.setDisplayMapper(
                     getMergedChildMapper(childMapper,
                     sourceDefaultUISet, mapping.getDisplayMapper(),
                     mergeDefault, targetDefaultUISet) );
               }
               else
                  addUIMapper(tmpMapping, sourceDefaultUISet, childMapper);

               replacementMappings.add(tmpMapping);

               // add to "used list"
               usedMappings.add(fieldRef);
            }
            else
            {
               // if SDMP or MDSP, merge the mapping if it has a match
               sourceMapping = sourceMapper.getMapping(fieldRef);

               if (sourceMapping == null)
                  continue;

               PSDisplayMapping tmpMapping = mergeMapping(sourceDefaultUISet,
                  sourceMapping, mergeDefault, targetDefaultUISet, mapping);

               replacementMappings.add(tmpMapping);

               // add to "used list"
               usedMappings.add(fieldRef);
            }
         }
      }

      // now set all the replacement mappings we've created
      Iterator i = replacementMappings.iterator();
      while (i.hasNext())
      {
         PSDisplayMapping tmpMapping = (PSDisplayMapping)i.next();
         targetDisplayMapper.replaceMapping(tmpMapping);
      }
   }

   /**
    * Removes any display mappings that reference fields in the exclude list.
    *
    * @param mapper The display mapper to remove mappings from, assumed not
    * <code>null</code>.  This mapper is not modified in any way.
    * @param excludes The list of field names as Strings to exclude.  Assumed
    * not <code>null</code>.
    *
    * @return A copy of the supplied mapper with the excluded mappings removed,
    * never <code>null</code>
    */
   private PSDisplayMapper removeExcludedMappings(PSDisplayMapper mapper,
      List excludes)
   {
      PSDisplayMapper newMapper = new PSDisplayMapper(mapper.getFieldSetRef());
      Iterator mappings = mapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         if(excludes.contains(mapping.getFieldRef()))
            continue;
         newMapper.add(mapping);
      }

      return newMapper;
   }

   /**
    * Adds the source mapper as a child of the target mapping, first
    * applying the source default uiset to each mapping.  Will also append all
    * child mappers of those mappings in the same fashion.  All params assumed
    * not to be <code>null</code>. The ids of added mappers are -1 and these
    * need to be fixed later.
    *
    * @param targetMapping The mapping to add the child mapper to.
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param sourceMapper The mapper to add as the child of the target mapping.
    * @throws PSSystemValidationException if there are any errors.
    */
   private void addUIMapper(PSDisplayMapping targetMapping,
      Iterator sourceDefaultUI, PSDisplayMapper sourceMapper)
         throws PSSystemValidationException
   {
      // create the new mapper
      PSDisplayMapper targetMapper = null;
      targetMapper = new PSDisplayMapper(sourceMapper.getFieldSetRef());
      //always set the id as -1
      targetMapper.setId(-1);

      // walk the source and merge the mappings
      Iterator i = sourceMapper.iterator();
      while (i.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)i.next();

         // add the mapping with merged default values
         PSDisplayMapping tmpMapping = addMapping(targetMapper, sourceDefaultUI,
            mapping);

         // do the same for any child mappers
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
            addUIMapper(tmpMapping, sourceDefaultUI, childMapper);

      }

      // set the mapper into the mapping
      targetMapping.setDisplayMapper(targetMapper);
   }

   /**
    * Adds a copy of the source mapping to the target mapper, applying any
    * defaults specified by the source uidef.  If this mapping contains a child
    * mapper, will recurse that mapper and add it's mapping and their children
    * as well, appyling the appropriate defaults.  All params assumed not to be
    * <code>null</code>.
    *
    * @param targetMapper The mapper to add the mapping to.
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param sourceMapping The mapping to add to the targetMapper.
    *
    * @return the new target mapping, never <code>null</code>.
    *
    * @throws PSSystemValidationException if there are any errors.
    */
   private PSDisplayMapping addMapping(PSDisplayMapper targetMapper,
      Iterator sourceDefaultUI, PSDisplayMapping sourceMapping)
         throws PSSystemValidationException
   {
      // create new mapping and merge default values
      String fieldRef = sourceMapping.getFieldRef();
      PSDisplayMapping tmpMapping = new PSDisplayMapping(fieldRef,
         mergeDefaultUISet(sourceDefaultUI, sourceMapping.getUISet()));

      // add mappings from any child mappers
      PSDisplayMapper childMapper = sourceMapping.getDisplayMapper();
      if (childMapper != null)
         addUIMapper(tmpMapping, sourceDefaultUI, childMapper);

      // add the new mapping
      targetMapper.add(tmpMapping);

      return tmpMapping;
   }

   /**
    * Merges the two supplied mappings as follows:
    * <ol>
    * <li>Overlays the source mapping's uiset on top of the correct source uidef
    * default uiset</li>
    * <li>Does the same for the target mapping if <code>mergeDefault</code> is
    * <code>true</code></li>
    * <li>Overlays the merged target uiset onto the merged source uiset</li>
    * <li>Sets the target mappings uiSet with this new composite uiset</li>
    * <li>Will recurse any child mappers and do the same for them</li>
    * <ol>
    * All params assumed not to be <code>null</code>.
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param sourceMapping The display mapping to retrieve the source uiset
    * from, assumed not to be <code>null</code>.
    * @param mergeDefault if <code>true</code> merges target mapping uiset with
    * its default, otherwise not
    * @param targetDefaultUI The defaultUI for target uiset to merge with, may
    * be <code>null</code>
    * @param targetMapping The display mapping to retrieve the target uiset
    * from, assumed not to be <code>null</code>.
    *
    * @return A copy of the target updated with the source as described, never
    * <code>null</code>
    *
    * @throws PSSystemValidationException if there are any errors.
    */
   private PSDisplayMapping mergeMapping(Iterator sourceDefaultUI,
      PSDisplayMapping sourceMapping, boolean mergeDefault,
      Iterator targetDefaultUI, PSDisplayMapping targetMapping)
      throws PSSystemValidationException
   {
      PSDisplayMapping resultMapping = null;

      // merge source with it's default
      PSUISet sourceUISet = mergeDefaultUISet(sourceDefaultUI,
         sourceMapping.getUISet());

      /* overlay with target uiset if supplied, after merging it with it's
       * default.
       */
      PSUISet tmpUISet = null;
      // first merge target's mapping with it's default if it is required
      if(mergeDefault)
      {
         tmpUISet = mergeDefaultUISet(
            targetDefaultUI, targetMapping.getUISet());
      }
      else
         tmpUISet = targetMapping.getUISet();

      // now merge target with source
      tmpUISet = tmpUISet.merge(sourceUISet);

      // create our result mapping
      resultMapping = new PSDisplayMapping(targetMapping.getFieldRef(),
         tmpUISet);

      // see if we have a child to deal with
      PSDisplayMapper sourceChildMapper = sourceMapping.getDisplayMapper();
      PSDisplayMapper targetChildMapper = targetMapping.getDisplayMapper();

      if ((targetChildMapper != null) && (sourceChildMapper == null))
      {
         // target cannot have a child if source does not
         throw new PSSystemValidationException(
            IPSObjectStoreErrors.CE_MAPPING_INVALID_CHILD,
               targetMapping.getFieldRef());
      }
      else if (sourceChildMapper != null)
      {
         if (targetChildMapper == null)
         {
            // recursively add, which will apply source defaults as well
            addUIMapper(resultMapping, sourceDefaultUI, sourceChildMapper);
         }
         else
         {
            // add target child to our result, then merge source child mappings
            resultMapping.setDisplayMapper( getMergedChildMapper(
               sourceChildMapper, sourceDefaultUI, targetChildMapper,
               mergeDefault, targetDefaultUI) );
         }
      }

      return resultMapping;

   }

   /**
    * Merges all mappings of the target child mapper with source child mapper.
    *
    * @param sourceChildMapper the source child mapper to get the source
    * mappings, assumed not to be <code>null</code>
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param targetChildMapper The child mapper to merge its mappings with
    * source, assumed not to be <code>null</code>.
    * @param mergeDefault if <code>true</code> merges target mapping uiset
    * with its default, otherwise not
    * @param targetDefaultUI The defaultUI for target uiset to merge with, may
    * be <code>null</code>
    *
    * @return copy of target child mapper merged with source childmapper,
    * never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in merging.
    */
   private PSDisplayMapper getMergedChildMapper(
      PSDisplayMapper sourceChildMapper, Iterator sourceDefaultUI,
      PSDisplayMapper targetChildMapper, boolean mergeDefault,
      Iterator targetDefaultUI)
      throws PSSystemValidationException
   {
      // build map of source mappings
      HashMap sourceMappings = new HashMap();
      Iterator mappings = sourceChildMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         sourceMappings.put(mapping.getFieldRef(), mapping);
      }

      // walk target and merge or add
      mappings = targetChildMapper.iterator();
      ArrayList replacementMappings = new ArrayList();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();
         PSDisplayMapping sourceChildMapping =
            (PSDisplayMapping)sourceMappings.get(fieldRef);

         if (sourceChildMapping != null)
         {
            /* merge with field in target, this will recurse any children
             * and merge or add them as well
             */
            PSDisplayMapping tmpMapping = mergeMapping(sourceDefaultUI,
               sourceChildMapping, mergeDefault, targetDefaultUI, mapping);

            // remove from source list so we know we've processed it
            sourceMappings.remove(fieldRef);

            replacementMappings.add(tmpMapping);
         }
         else
         {
            // target may not contain fields not in the source
           throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_MAPPING_INVALID_CHILD_FIELDS,
                  targetChildMapper.getFieldSetRef());

         }
      }

      //Now add all merged mappings
      PSDisplayMapper resultMapper = new PSDisplayMapper(
         targetChildMapper.getFieldSetRef());
      resultMapper.setId( targetChildMapper.getId() );
      resultMapper.addAll(replacementMappings);

      // finally, add mappings from source not already in target
      mappings = sourceMappings.values().iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         addMapping(resultMapper, sourceDefaultUI, mapping);
      }
      return resultMapper;
   }

   /**
    * Merges the uiset with the appropriate default uiset from provided <code>
    * defaultUISet</code> if the default set name is specified in uiset. If the
    * uiSet does not have a default specified, return the original uiset
    * unchanged.
    *
    * @param defaultUISet The collection of default uisets to check, may be
    * <code>null</code>
    * @param uiSet The uiset to be merged with default, may not be <code>null
    * </code>
    *
    * @return a new ui set, merged from the default and the supplied uiset, or
    * the uiSet passed in if it is not modified in any way. Never
    * <code>null</code>.
    *
    * @throws PSSystemValidationException if the default specified in the uiSet is not
    * found in the collection of the default uisets.
    */
   private PSUISet mergeDefaultUISet(Iterator defaultUISet, PSUISet uiSet)
      throws PSSystemValidationException
   {
      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      PSUISet result = null;

      String defaultName = uiSet.getDefaultSet();

      if (defaultName == null )
      {
         // no default specified
         result = uiSet;
      }
      else
      {
         boolean isMatch = false;

         if(defaultUISet != null)
         {
            while (defaultUISet.hasNext())
            {
               PSUISet defUI = (PSUISet)defaultUISet.next();
               if (defUI.getName().equals(defaultName))
               {
                  //Merge the cloned versions so that it does not effect the
                  //original versions in anyway.
                  result = ((PSUISet)uiSet.clone()).merge(
                     (PSUISet)defUI.clone());
                  isMatch = true;
                  break;
               }
            }
         }

         if (!isMatch)
         {
            // a default set specified that does not exist!
            throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_MAPPING_INVALID_DEFAULT_UISET, defaultName);
         }
      }

      return result;
   }

   /**
    * Recursively sets the id to this mapper and its children if they have
    * undefined id. The provided id is incremented by 1 to set to the next
    * child with undefined id.
    *
    * @param nextId the id to set, assumed >= 1
    *
    * @return the next highest(available) id after setting ids to this mapper
    * and its children.
    */
   private int setMapperIds(int nextId)
   {
      if(getId() == -1)
         setId(nextId++);

      Iterator mappings = iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
         {
            nextId = childMapper.setMapperIds( nextId );
         }
      }

      return nextId;
   }

   /**
    * Gets the highest mapper id used in this mapper and child mappers of its
    * mappings.
    *
    * @return the highest mapper id
    */
   private int getHighestMapperId()
   {
      int highId = getId();
      Iterator mappings = iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         PSDisplayMapper childMapper = mapping.getDisplayMapper();
         if (childMapper != null)
         {
            int childId = childMapper.getHighestMapperId();
            highId = highId > childId ? highId : childId;
         }
      }
      return highId;
   }

   /**
    * Worker method for {@link #demerge(boolean, Iterator, PSUIDefinition,
    * PSFieldSet, List) demerge} method. Here lists the additional parameter
    * description. Please see the above link for more description.
    *
    * @param localMapper the mapper that needs to be demerged from source def,
    * assumed not to be <code>null</code>
    *
    */
   private void demergeMapper(PSDisplayMapper localMapper,
      boolean demergeDefault, Iterator defaultUISet,
      List fieldExcludes, PSDisplayMapper sourceMapper,
      Iterator sourceDefaultUI, boolean demergeChild)
      throws PSSystemValidationException
   {
       /* walk the mapper, and for any "placeholders", create a new merged
       * version and add it to a replacement list.  Then we'll walk the
       * replacement list and replace the actual mappings.  This is so we don't
       * modify the mapper under the iterator while we walk it.
       */
      ArrayList replacementMappings = new ArrayList();

      Iterator mappings = localMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();

         //If it is in the fieldExcludes section, then it might be a local field
         //having same name as system/shared field
         if (fieldExcludes.contains(fieldRef))
            continue;

         // get the matching source mapping
         PSDisplayMapping sourceMapping = null;
         if (sourceMapper != null)
         {
            PSDisplayMapper childMapper = removeExcludedMappings(
               sourceMapper, fieldExcludes);

            if (fieldRef.equalsIgnoreCase(childMapper.getFieldSetRef()) &&
                demergeChild)
            {
               //demerge the child mapper and add it to the main mapping in parent
               PSDisplayMapping tmpMapping = new PSDisplayMapping(
                  fieldRef, mapping.getUISet());

               replacementMappings.add(tmpMapping);

               if(mapping.getDisplayMapper() != null)
               {
                  tmpMapping.setDisplayMapper(
                     getDemergedChildMapper(childMapper, sourceDefaultUI,
                     mapping.getDisplayMapper(), demergeDefault, defaultUISet) );
               }
            }
            else
            {
               sourceMapping = sourceMapper.getMapping(fieldRef);

               if (sourceMapping == null)
                  continue;

               PSDisplayMapping tmpMapping = demergeMapping(sourceDefaultUI,
                  sourceMapping, demergeDefault, defaultUISet, mapping);

               replacementMappings.add(tmpMapping);
            }
         }
      }

      // now set all the replacement mappings we've created
      Iterator i = replacementMappings.iterator();
      while (i.hasNext())
      {
         PSDisplayMapping tmpMapping = (PSDisplayMapping)i.next();
         localMapper.replaceMapping(tmpMapping);
      }

   }

   /**
    * Demerges the target mapping from source mapping and recursively demerges
    * all child mappers. See {@link #demerge(boolean, Iterator, PSUIDefinition,
    * PSFieldSet, List) demerge} for more description demerging process.
    *
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param sourceMapping The display mapping to retrieve the source uiset
    * from, assumed not to be <code>null</code>.
    * @param demergeDefault if <code>true</code> demerges target mapping uiset
    * from its default, otherwise not
    * @param targetDefaultUI The defaultUI for target uiset to demerge from, may
    * be <code>null</code>
    * @param targetMapping The display mapping to retrieve the target uiset
    * from, assumed not to be <code>null</code>.
    *
    * @return A copy of the target updated with the source as described, never
    * <code>null</code>
    *
    * @throws PSSystemValidationException if there are any errors.
    */
   private PSDisplayMapping demergeMapping(Iterator sourceDefaultUI,
      PSDisplayMapping sourceMapping, boolean demergeDefault,
      Iterator targetDefaultUI, PSDisplayMapping targetMapping)
      throws PSSystemValidationException
   {
      PSDisplayMapping resultMapping = null;

      // merge source with it's default
      PSUISet sourceUISet = mergeDefaultUISet(sourceDefaultUI,
         sourceMapping.getUISet());

      /* demerge target ui set from source first and then with default ui
       */
      PSUISet tmpUISet = null;
      // now get the UI set with the overrides alone
      tmpUISet = targetMapping.getUISet().demerge(sourceUISet);

      // then  demerge target's mapping with it's default
      if(demergeDefault)
      {
         tmpUISet = demergeDefaultUISet(targetDefaultUI, tmpUISet);
      }

      // create our result mapping
      resultMapping = new PSDisplayMapping(targetMapping.getFieldRef(),
         tmpUISet);

      // see if we have a child to deal with
      PSDisplayMapper sourceChildMapper = sourceMapping.getDisplayMapper();
      PSDisplayMapper targetChildMapper = targetMapping.getDisplayMapper();

      if ((targetChildMapper != null) && (sourceChildMapper == null))
      {
         // target cannot have a child if source does not
         throw new PSSystemValidationException(
            IPSObjectStoreErrors.CE_MAPPING_INVALID_CHILD,
               targetMapping.getFieldRef());
      }
      else if (sourceChildMapper != null && targetChildMapper != null)
      {
         resultMapping.setDisplayMapper( getDemergedChildMapper(
            sourceChildMapper, sourceDefaultUI, targetChildMapper,
            demergeDefault, targetDefaultUI) );
      }

      return resultMapping;
   }

   /**
    * Demerges all mappings of the target child mapper from source child mapper.
    *
    * @param sourceChildMapper the source child mapper to get the source
    * mappings, assumed not to be <code>null</code>
    * @param sourceDefaultUI The source default ui to merge uiset in source
    * mapping, may be <code>null</code>
    * @param targetChildMapper The child mapper to demerge its mappings from
    * source, assumed not to be <code>null</code>.
    * @param demergeDefault if <code>true</code> demerges target mapping uiset
    * from its default, otherwise not
    * @param targetDefaultUI The defaultUI for target uiset to demerge from, may
    * be <code>null</code>
    *
    * @return copy of target child mapper demerged from source childmapper,
    * never <code>null</code>
    *
    * @throws PSSystemValidationException if an error happens in demerging.
    */
   private PSDisplayMapper getDemergedChildMapper(
      PSDisplayMapper sourceChildMapper, Iterator sourceDefaultUI,
      PSDisplayMapper targetChildMapper, boolean demergeDefault,
      Iterator targetDefaultUI)
      throws PSSystemValidationException
   {
      // build map of source mappings
      HashMap sourceMappings = new HashMap();
      Iterator mappings = sourceChildMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         sourceMappings.put(mapping.getFieldRef(), mapping);
      }

      // walk target and demerge
      mappings = targetChildMapper.iterator();
      ArrayList replacementMappings = new ArrayList();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping)mappings.next();
         String fieldRef = mapping.getFieldRef();
         PSDisplayMapping sourceChildMapping =
            (PSDisplayMapping)sourceMappings.get(fieldRef);

         if (sourceChildMapping != null)
         {
            /* demerge with field in target, this will recurse any children
             * and demerge
             */
            PSDisplayMapping tmpMapping = demergeMapping(sourceDefaultUI,
               sourceChildMapping, demergeDefault, targetDefaultUI, mapping);

            // remove from source list so we know we've processed it
            sourceMappings.remove(fieldRef);

            replacementMappings.add(tmpMapping);
         }
         else
         {
            //give a message the child mapper with fieldset ref have a mapping
            //for a field which don't have a mapping in corresponding source
            //definition of this child mapper
            // target may not contain fields not in the source
            throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_MAPPING_INVALID_CHILD_FIELDS,
                  targetChildMapper.getFieldSetRef());
         }
      }

      //Now add all demerged mappings and return mapper
      PSDisplayMapper resultMapper = new PSDisplayMapper(
         targetChildMapper.getFieldSetRef());
      resultMapper.setId( targetChildMapper.getId() );
      resultMapper.addAll(replacementMappings);

      return resultMapper;
   }

   /**
    * Demerges the uiset from the appropriate default uiset from provided <code>
    * defaultUISet</code> if the default set name is specified in uiset. If the
    * uiSet does not have a default specified, return the original uiset
    * unchanged.
    *
    * @param defaultUISet The collection of default uisets to check, may be
    * <code>null</code>
    * @param uiSet The uiset to be demerged with default, may not be <code>null
    * </code>
    *
    * @return a new ui set, demerged the supplied uiset from default, or
    * the uiSet passed in if it is not modified in any way. Never
    * <code>null</code>.
    *
    * @throws PSSystemValidationException if the default specified in the uiSet is not
    * found in the collection of the default uisets.
    */
   private PSUISet demergeDefaultUISet(Iterator defaultUISet, PSUISet uiSet)
      throws PSSystemValidationException
   {
      if (uiSet == null)
         throw new IllegalArgumentException("uiSet may not be null");

      PSUISet result = null;

      String defaultName = uiSet.getDefaultSet();

      if (defaultName == null )
      {
         // no default specified
         result = uiSet;
      }
      else
      {
         boolean isMatch = false;
         if(defaultUISet != null)
         {
            while (defaultUISet.hasNext())
            {
               PSUISet defUI = (PSUISet)defaultUISet.next();
               if (defUI.getName().equals(defaultName))
               {
                  //Demerge the cloned versions so that it does not effect the
                  //original versions in anyway.
                  result = ((PSUISet)uiSet.clone()).demerge(
                     (PSUISet)defUI.clone());
                  isMatch = true;
                  break;
               }
            }
         }

         if (!isMatch)
         {
            // a default set specified that does not exist!
            throw new PSSystemValidationException(
               IPSObjectStoreErrors.CE_MAPPING_INVALID_DEFAULT_UISET, defaultName);
         }
      }

      return result;
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXDisplayMapper";

   /**
    * The name of a field set this is referenceing, never <code>null</code>
    * or empty after construction.
    */
   private String m_fieldSetRef;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String FIELD_SET_REF_ATTR = "fieldSetRef";
}

