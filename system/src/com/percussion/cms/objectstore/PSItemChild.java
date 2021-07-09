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

import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An object representation of the StandardItem.xsd Child element.  Which is a
 * Complex child, meaning it has 1 or more columns and 0 or more rows, which
 * will be referred to in this class as 'childEntries'.  In order for
 * childEntries to be added to the child the childEntries are validated to
 * ensure that they have the same field definitions, if they do they can be
 * added to the Child.  Therefore it is possible that childEntries, defined
 * appropriately, may be shared across Child objects.
 */
public class PSItemChild extends PSItemComponent
{
   /**
    * Construct a new instance from definitions.
    *
    * @param fieldSet - the definition of this object. Must not be <code>null
    * </code>.
    * @param displayMapping - the display mapping defining the ui representation
    * of this object. Must not be <code>null</code>, must contain a child
    * mapper.
    */
   public PSItemChild(PSFieldSet fieldSet, PSDisplayMapping displayMapping)
   {
      if (fieldSet == null)
         throw new IllegalArgumentException("fieldset must not be null");

      if (displayMapping == null)
         throw new IllegalArgumentException("displayMapping must not be null");
      
      if (displayMapping.getDisplayMapper() == null)
         throw new IllegalArgumentException(
            "displayMapping must contian a child mapper");


      m_fieldSetDef = fieldSet;
      m_childId = displayMapping.getDisplayMapper().getId();
      m_uiDef = displayMapping.getUISet();
      
      init();
   }

   /**
    * Initialize lifetime objects.
    */
   private void init()
   {
      m_entryList = new ArrayList<>();
   }

   /**
    * Each <code>PSItemChild</code> has an associated name.  This method
    * returns this <code>PSItemChild</code> object's name.
    *
    * @return child name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_fieldSetDef.getName();
   }

   /**
    * Get the id of this <code>PSItemChild</code>.
    * 
    * @return The id.
    */
   public int getChildId()
   {
      return m_childId;
   }

   /**
    * Each <code>PSItemChild</code> may have an associated a display name.
    * This method returns this <code>PSItemChild</code> objects display name.
    *
    * @return display name may be <code>null</code> never empty.
    */
   public String getDisplayName()
   {
      String displayName = null;

      // not null and not empty:
      if (m_uiDef.getLabel() != null
         && m_uiDef.getLabel().getText().trim().length() != 0)
         displayName = m_uiDef.getLabel().getText();

      return displayName;
   }

   /**
    * The total number of childEntries in this child.
    *
    * @return total number of <code>PSItemChildEntry</code>. If 0, then
    * there are no <code>PSItemChildEntry</code> objects.
    */
   public int childEntryCount()
   {
      return m_entryList.size();
   }

   /**
    * Gets the <code>PSItemChildEntry</code> by the specified row id.  This is
    * called by update.
    *
    * @param childRowId must be >= 0.
    * @return may return <code>null</code>
    */
   public PSItemChildEntry getChildEntryByRowId(int childRowId)
   {
      if (childRowId < 0)
         throw new IllegalArgumentException("childRowId must be >= 0");

      Iterator<PSItemChildEntry> childEntryIter = m_entryList.iterator();
      PSItemChildEntry childEntry = null;
      while (childEntryIter.hasNext())
      {
         childEntry = childEntryIter.next();
         if (childEntry.getChildRowId() == childRowId)
            return childEntry;
      }
      return null;
   }

   /**
    * Creates a valid <code>PSItemChildEntry</code>.  This
    * <code>PSItemChildEntry</code> is not automatically added to this
    * <code>PSItemChild</code>.
    *
    * @return a valid <code>PSItemChildEntry</code>
    */
   public PSItemChildEntry createChildEntry()
   {
      // if the m_template_entry is null, create it and return the template
      // entry, this will be populated by the definition extractor.  The
      // template entry is the entry that has not data only definition, this
      // will be cloned when the user calls this method.

      if (m_template_entry == null)
      {
         m_template_entry = new PSItemChildEntry(m_fieldSetDef);
         return m_template_entry;
      }
      else
         return (PSItemChildEntry)m_template_entry.clone();
   }

   /**
    * Creates a valid childEntry and adds it to this child.
    *
    * @return a valid <code>PSItemChildEntry</code>
    */
   public PSItemChildEntry createAndAddChildEntry()
   {
      PSItemChildEntry entry = createChildEntry();
      addEntry(entry);

      return entry;
   }

   /**
    * Every <code>PSItemChild</code> may have a field or a collection
    * of <code>PSItemChildEntry</code> fields.  This returns the entire
    * collection.
    *
    * @return unmodifiable <code>Iterator</code> of
    * <code>PSItemChildEntry</code> objects owned by this class. Any
    * modifications to them will affect the childEntry in this class.  May be
    * empty but not <code>null</code>.
    */
   public Iterator<PSItemChildEntry> getAllEntries()
   {
      return Collections.unmodifiableList(m_entryList).iterator();
   }
   
   /**
    * Moves a <code>PSItemChildEntry</code> up one in the index of all child
    * entries.  If it can't move any higher (meaning index 0) then it won't do
    * anything.
    *
    * @param childEntry the entry to move up.  Must not be <code>null</code>.
    */
   public void moveUp(PSItemChildEntry childEntry)
   {
      move(childEntry, -1);
   }

   /**
    * Moves a <code>PSItemChildEntry</code> down one in the index of all child
    * entries.  If it can't move any lower (meaning the index size) then it
    * won't do anything.
    *
    * @param childEntry the entry to move up.  Must not be <code>null</code>.
    */
   public void moveDown(PSItemChildEntry childEntry)
   {
      move(childEntry, 1);
   }

   /**
    * If sequencing is enabled for this child, the order of the entries is
    * preserved. childEntries are sequenced starting from 0 to n-1,
    * where n is thenumber of entries contained by this child. 0 is considered
    * the 'top' of the list. To move an item towards the top,
    * supply a negative number.
    *
    * @param childEntry - the childEntry object to move.
    * @param upDown -1 is up and 1 is down etc.  The result of this +
    * <code>childEntryCount()</code>, must be > 0 and
    *  < <code>childEntryCount()</code>
    */
   @SuppressWarnings("unchecked")
   public void move(PSItemChildEntry childEntry, int upDown)
   {
      int currentPos = m_entryList.indexOf(childEntry);

      // can we move:
      if (currentPos + upDown == -1
         || currentPos + upDown >= m_entryList.size())
         throw new IllegalArgumentException("upDown is an invalid value");

      // get it:
      childEntry = m_entryList.get(currentPos);

      // remove:
      m_entryList.remove(currentPos);

      // add it to currentPos:
      m_entryList.add(currentPos + upDown, childEntry);
   }
   
   /**
    * Moves the supplied child entry from its current position to the specified
    * position.  {@link #isSequenced()} must return <code>true</code>.
    * 
    * @param childEntry The entry to move, must be found in the current list.
    * @param position The new position, must be > 0 and < 
    * {@link #childEntryCount()}.
    */
   public void moveToPosition(PSItemChildEntry childEntry, int position)
   {
      if (!isSequenced())
         throw new IllegalStateException("sequencing not enabled");
      
      if (position < 0 || position >= m_entryList.size())
         throw new IllegalArgumentException("Invalid position specified");
      
      int currentPos = m_entryList.indexOf(childEntry);
      if (currentPos == -1)
         throw new IllegalArgumentException("childEntry not found in list");
      
      if (currentPos != position)
      {
         m_entryList.add(position, m_entryList.remove(currentPos));
      }
   }

   /** @todo add method that adds a child entry to a specific position **/
   /**
    * Adds a <code>PSItemChildEntry</code> to the collection.  The
    * <code>PSItemChildEntry</code> must have valid
    * <code>PSItemField</code> objects.  That is one that was created by
    * this object or one that has the same <code>PSItemField</code>
    * as a <code>PSItemChildEntry</code> that is created by this object.
    * <p />
    * To ensure that the <code>PSItemChildEntry</code> is valid,
    * use {@link  #createChildEntry()  createChildEntry()} to create the
    * <code>PSItemChildEntry</code>.
    * <p />
    *
    * @param childEntry the object to add.  Must not be <code>null</code> must
    * have valid <code>PSItemField</code> objects.
    */
   @SuppressWarnings("unchecked")
   public void addEntry(PSItemChildEntry childEntry)
   {
      if (childEntry == null)
         throw new IllegalArgumentException("child entry must not be null");

      if (!validateEntry(childEntry))
         throw new IllegalArgumentException(
            "childEntry must have a valid PSItemField objects");

      m_entryList.add(childEntry);
   }
   
   /**
    * Replaces the current entry with the same childrowid.
    * 
    * @param childEntry The entry with which the current entry is replaced, may
    * not be <code>null</code>, must have a matching field structure and must 
    * have a childRowId that matches an existing entry.
    */
   public void replaceEntry(PSItemChildEntry childEntry)
   {
      if (childEntry == null)
         throw new IllegalArgumentException("childEntry may not be null");
      
      if (!validateEntry(childEntry))
         throw new IllegalArgumentException(
            "childEntry must have valid PSItemField objects");

      int childRowId = childEntry.getChildRowId();
      PSItemChildEntry curChild = null;
      int index = 0;
      for (PSItemChildEntry entry : m_entryList)
      {
         if (entry.getChildRowId() == childRowId)
         {
            curChild = m_entryList.set(index, childEntry);
            break;
         }
         index++;
      }
      
      if (curChild == null)
         throw new IllegalArgumentException("matching childEntry not found");
   }

   /**
    * This method compares all the fields in the Template Entry, to the
    * childEntry argument.  If all of the fields <code>PSItemFieldMeta</code>
    * match then it is a valid entry and <code>true</code> will be returned,
    * otherwise <code>false</code>.
    *
    * @param childEntry the entry to compare against the template.  Assumed
    * not <code>null</code>.
    * @return if the childEntry matches.
    */
   private boolean validateEntry(PSItemChildEntry childEntry)
   {
      Iterator entryFields = childEntry.getAllFields();
      PSItemField entryField = null;
      String fieldName = null;
      PSItemField templateField = null;
      PSItemFieldMeta templateFieldMeta = null;
      while (entryFields.hasNext())
      {
         entryField = (PSItemField)entryFields.next();
         fieldName = entryField.getName();

         templateField = m_template_entry.getFieldByName(fieldName);
         if (templateField == null)
            return false;

         templateFieldMeta = templateField.getItemFieldMeta();
         if (!entryField.getItemFieldMeta().equals(templateFieldMeta))
            return false;
      }
      return true;
   }

   /**
    * Are the child entries sequenced?  Default is <code>false</code>.
    *
    * @return <code>true</code> if yes otherwise <code>false</code>.
    */
   public boolean isSequenced()
   {
      return m_fieldSetDef.isSequencingSupported();
   }

   /**
    * @see PSItemComponent#toXml(Document, PSAcceptElements)
    */
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = createStandardItemElement(doc, EL_CHILD);

      root.setAttribute(ATTR_NAME, getName());

      if (getDisplayName() != null)
         root.setAttribute(ATTR_DISPLAY_NAME, getDisplayName());

      root.setAttribute(ATTR_SEQUENCED, "" + isSequenced());

      // childentry:
      if (!m_entryList.isEmpty())
         toXmlCollection(root, doc, m_entryList, acceptElements);

      return root;
   }

   /**
    * Convenience method to <code>toXml(doc, null)</code>
    * @see #toXml(Document, PSAcceptElements)
    */
   public Element toXml(Document doc)
   {
      return toXml(doc, null);

   }

   //see interface for description
   public Object clone()
   {
      PSItemChild copy = null;
      copy = (PSItemChild)super.clone();

      // don't clone defs:
      copy.m_fieldSetDef = m_fieldSetDef;
      copy.m_uiDef = m_uiDef;

      copy.init();

      copy.m_template_entry = (PSItemChildEntry)m_template_entry.clone();

      Iterator i = m_entryList.iterator();
      while (i.hasNext())
      {
         PSItemChildEntry obj = (PSItemChildEntry)i.next();
         copy.addEntry((PSItemChildEntry)obj.clone());
      }

      return copy;
   }

   //see interface for description
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSItemChild comp = (PSItemChild)obj;

      if (!compare(m_fieldSetDef, comp.m_fieldSetDef))
         return false;
      if (!compare(m_uiDef, comp.m_uiDef))
         return false;
      if (!compare(m_entryList, comp.m_entryList))
         return false;
      if (!compare(m_template_entry, comp.m_template_entry))
         return false;

      if (isSequenced() != comp.isSequenced())
         return false;
      if (!compare(getName(), comp.getName()))
         return false;
      if (!compare(getDisplayName(), comp.getDisplayName()))
         return false;
      if (m_childId != comp.m_childId)
         return false;
         
      return true;
   }

   //see interface for description
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call
      hash += hashBuilder(m_fieldSetDef);
      hash += hashBuilder(m_uiDef);
      hash += hashBuilder(m_entryList);
      hash += hashBuilder(m_template_entry);
      hash += m_childId;

      return hash;
   }

   // @see IPSDataComponent
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      loadXmlData(sourceNode, true);
   }

   /**
    * This method is called to populate an object from its XML representation.
    * It assumes that the object may already have a complete data structure,
    * therefore method only overlays the data onto the existing object.
    * An element node may contain a hierarchical structure, including child
    * objects. The element node can also be a child of another element node.
    * <p>
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by this class.
    */
   public void loadXmlData(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      loadXmlData(sourceNode, false);
   }

   void loadXmlData(Element sourceNode, boolean clearValues)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, EL_CHILD);

      if (clearValues)
         m_entryList.clear();

      // get all the child entries
      Element el = PSXMLDomUtil.getFirstElementChild(sourceNode);
      while (el != null
         && PSXMLDomUtil.getUnqualifiedNodeName(el).equals(
            PSItemChildEntry.EL_CHILD_ENTRY))
      {
         if (clearValues)
         {
            PSItemChildEntry itemChildEntry = createAndAddChildEntry();
            itemChildEntry.loadXmlData(el, clearValues);
         }
         else
         {
            PSItemChildEntry itemChildEntry = null;
            int id = PSItemChildEntry.getRowId(el);
            if (id < 0)
            {
               itemChildEntry = createAndAddChildEntry();
            }
            else
            {
               itemChildEntry = getChildEntryByRowId(id);
               if (itemChildEntry == null)
                  itemChildEntry = createAndAddChildEntry();
            }
            itemChildEntry.loadXmlData(el, clearValues);
         }
         el = PSXMLDomUtil.getNextElementSibling(el);
      }
   }

   /**
    * Gets the name of this child element.
    *
    * @param el the element to retrieve the child name from, must not be <code>
    * null</code>
    *
    * @return the name of this child element
    */
   static String getName(Element el)
   {
      return el.getAttribute(ATTR_NAME);
   }

   /**
    * Definition of this child fields, initialized in ctor, never
    *  <code>null</code>
    */
   private PSFieldSet m_fieldSetDef;

   /**
    * Definition of child UI data, initialized in ctor, never <code>null</code>
    */
   private PSUISet m_uiDef;
   
   /**
    * The id uniquely identifying this child within its parent item.  Set during
    * the ctor, never modified after that.
    */
   private int m_childId;

   /**
    * List of entries, never <code>null</code> may be empty.
    * @see #getAllEntries()
    */
   private List<PSItemChildEntry> m_entryList;

   /**
    * childEntry from which all are created.  This is initialized when this
    * child get's populated from the definition, never <code>null</code> and
    * is invariant.
    * @see #createEntry()
    */
   private PSItemChildEntry m_template_entry;

   /** Name of the elements in this class' XML representation */
   public static final String ATTR_NAME = "name";
   public static final String ATTR_DISPLAY_NAME = "displayName";
   public static final String EL_CHILD = "Child";
   public static final String ATTR_SEQUENCED = "sequenced";

}
