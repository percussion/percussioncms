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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The base class for items in the CMS layer.  This class does all of the heavy
 * lifting by providing the implementation for the majority of functionality .
 * It is database unaware.
 * <P />
 * There are 2 ways to populate an instance w/ existing data, by supplying
 * an xml document in the sys_StandardItem.xsd format, or by calling the various
 * protected and public methods to allow setting of the data.
 */
public class PSCoreItem extends PSItemComponent implements IPSItemAccessor
{

   /**
    * Creates a new <code>PSCoreItem</code> with only definition information and
    * no data information.  This constructor will be used by the remote clients.
    *
    * @param itemDefinition must not be <code>null</code>.
    * @throws PSCmsException - if an extraction error occurs while the
    * definition is being extracted.
    */
   public PSCoreItem(PSItemDefinition itemDefinition) throws PSCmsException
   {
      if (itemDefinition == null)
         throw new IllegalArgumentException("item definition must not be null");

      m_itemDefinition = itemDefinition;
      init();
      extractDef();
   }

   /**
    * Add child to item
    *
    * @param child - assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void addChild(PSItemChild child)
   {
      m_childNameChildMap.put(child.getName(), child);
   }

   /**
    * Populates this item with meta data.
    *
    * @throws PSCmsException if there is an error populating this item.
    */
   private void extractDef() throws PSCmsException
   {
      PSItemDefExtractor.populateItemDefinition(this);
   }

   /**
    * Called by the extractor.  Accepts an <code>IPSVisitor</code>.
    *
    * @param visitor must not be <code>null</code>
    */
   public void accept(IPSVisitor visitor)
   {
      if (visitor == null)
         throw new IllegalArgumentException("visitor must not be null");

      // if getObject returns null, then no field/child will be added.
      if (visitor.getObject() instanceof PSItemField)
         addField((PSItemField)visitor.getObject());

      else if (visitor.getObject() instanceof PSItemChild)
         addChild((PSItemChild)visitor.getObject());
   }

   /**
    * Adds <code>PSItemField</code> to the <code>map</code>.
    *
    * @param field assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void addField(PSItemField field)
   {
      m_fieldNameFieldMap.put(field.getName(), field);
   }

   /**
    * Adds <code>PSItemRelatedItem</code> to the <code>map</code>.
    *
    * @param item assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void addRelatedItem(PSItemRelatedItem item)
   {
      int rId = item.getRelationshipId();
      if (rId == -1)
      {
         rId = item.hashCode(); // some unique identifier
      }
      m_relatedItemsMap.put("" + rId, item);     
   }
   
   /**
    * Once an item has gone public, the revision lock gets set, after which on
    * every update the revision will change.
    *
    * @return  If this item is locked this method will return <code>true</code>
    * and means that it has gone public and the revision id will be incremented
    * upon update.  Otherwise <code>false</code>.
    */
   public boolean isRevisionLocked()
   {
      return m_revisionLock;
   }

   /**
    * Sets the locked state.See {@link #isRevisionLocked() isRevisionLocked()} 
    * for a description of locking.
    *
    * @param locked <code>true</code> if it is locked otherwise 
    *    <code>false</code>.
    */
   public void isRevisionLocked(boolean locked)
   {
      m_revisionLock = locked;
   }

   /**
    * Convenience method calls 
    * {@link #toMinXml(Document,boolean,boolean,boolean,boolean)
    * toMinXml(doc, true, true, true, false)}.
    */
   public Element toXml(Document doc)
   {
      /** @todo: this method should return the entire document **/
      return toMinXml(doc, true, true, true, false);
   }

   /**
    * Not implemented yet.
    * 
    * @return <code>null</code>.
    */
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
     /*
      *  Not implemented.
      *  @todo: Implement this method so that it
      *  the original concept for toMinXml is about not including fields
      *  that have not changed since creation or fromXml was called.
      *  To properly implement it, child classes would need to implement this
      *  as well.
      */

      return null;
   }

   /**
    * Return the minimum XML constrained to the sys_StandardItem.xsd schema
    * based on the following flags:
    *
    * @param doc the parent doc from which to create the elements, never
    * <code>null</code>
    * @param includeFields include all fields in the return doc
    * @param includeChilds include all the child fields in the return doc
    * @param includeRelated include all the related fields in the return doc
    * @param includeBinary include binary fields in the return doc - does not
    * apply to binary fields in children.
    * @return a document representation of a subset of this Core Item
    */
   public Element toMinXml(
      Document doc,
      boolean includeFields,
      boolean includeChilds,
      boolean includeRelated,
      boolean includeBinary)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = createStandardItemRootElement(doc, EL_ITEM);
      
      root.setAttribute(ATTR_CONTENTTYPE, "" + getContentTypeId());

      // only include if set:
      if (m_requestedrevision > 0)
         root.setAttribute(ATTR_REQUESTED_REVISION, "" + m_requestedrevision);

      if (m_currentrevision > 0)
         root.setAttribute(ATTR_CURRENT_REVISION, "" + m_currentrevision);

      if (m_editrevision > 0)
         root.setAttribute(ATTR_EDIT_REVISION, "" + m_editrevision);

      if (m_editrevision > 0)
         root.setAttribute(ATTR_REVISION_COUNT, "" + m_editrevision);

      // not counting on JRE to create attribute value, done explicitely:
      root.setAttribute(
         ATTR_REVISION_LOCK,
         (m_revisionLock == true ? "true" : "false"));

      if (m_systemLocale != null)
         root.setAttribute(ATTR_SYSTEM_LOCALE, "" + m_systemLocale);

      if (m_dataLocale != null)
         root.setAttribute(ATTR_DATA_LOCALE, "" + m_dataLocale);

      // just check for null, if empty, then set empty:
      if (m_checkedOutBy.trim().length() != 0)
         root.setAttribute(ATTR_CHECKED_OUT_BY, m_checkedOutBy);

      Element e = createStandardItemElement(doc, EL_CONTENT_KEY);

      if (m_contentId > 0)
         e.setAttribute(ATTR_CONTENT_ID, "" + m_contentId);

      if (m_revision > 0)
         e.setAttribute(ATTR_REVISION, "" + m_revision);

      root.appendChild(e);

      // build accept elements rules:
      PSAcceptElements acceptElements = new PSAcceptElements(includeFields,
         includeChilds, includeRelated, includeBinary);

      // fields:
      if (includeFields && !m_fieldNameFieldMap.isEmpty())
         toXmlCollection(root, doc, m_fieldNameFieldMap.values(),
            acceptElements);

      // children:
      if (includeChilds && !m_childNameChildMap.isEmpty())
         toXmlCollection(root, doc, m_childNameChildMap.values(),
            acceptElements);

      // related items:
      if (includeRelated && !m_relatedItemsMap.isEmpty())
      {
         Element relatedItems = 
            createStandardItemElement(doc, EL_RELATED_ITEMS);
         root.appendChild(relatedItems);
         toXmlCollection(relatedItems, doc, m_relatedItemsMap.values(),
            acceptElements);
      }

      return root;
   }

   /**
    * Initialize objects.
    */
   private void init()
   {
      m_fieldNameFieldMap = new HashMap<>();
      m_childNameChildMap = new HashMap<>();
      m_relatedItemsMap = new HashMap<>();
   }

   // see interface for description
   @SuppressWarnings("unchecked")
   public Object clone()
   {
      PSCoreItem copy = null;
      copy = (PSCoreItem)super.clone();

      copy.init();

      // clone fields:
      Iterator i = m_fieldNameFieldMap.keySet().iterator();
      while (i.hasNext())
      {
         Object key = i.next();
         PSItemField val = (PSItemField)m_fieldNameFieldMap.get(key);
         copy.addField((PSItemField)val.clone());
      }

      // clone children:
      Iterator k = m_childNameChildMap.keySet().iterator();
      while (k.hasNext())
      {
         Object key = k.next();
         PSItemChild val = (PSItemChild)m_childNameChildMap.get(key);
         copy.addChild((PSItemChild)val.clone());
      }

      if (m_dataLocale != null)
         copy.m_dataLocale = (Locale)m_dataLocale.clone();

      if (m_systemLocale != null)
         copy.m_systemLocale = (Locale)m_systemLocale.clone();

      // do not clone the definition:
      if (m_itemDefinition != null)
         copy.m_itemDefinition = m_itemDefinition;

      if (m_relatedItemsMap != null)
      {
         Iterator l = m_relatedItemsMap.keySet().iterator();
         while (l.hasNext())
         {
            String key = (String) l.next();
            PSItemRelatedItem val = 
               (PSItemRelatedItem) m_relatedItemsMap.get(key);
            copy.m_relatedItemsMap.put(key, (PSItemRelatedItem)val.clone());
         }
      }

      return copy;
   }

   // see interface for description
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSCoreItem comp = (PSCoreItem)obj;

      if (!compare(m_itemDefinition, comp.m_itemDefinition))
         return false;
      if (!compare(m_checkedOutBy, comp.m_checkedOutBy))
         return false;
      if (!compare(m_childNameChildMap, comp.m_childNameChildMap))
         return false;
      if (m_contentId != comp.m_contentId)
         return false;
      if (getContentTypeId() != comp.getContentTypeId())
         return false;
      if (m_currentrevision != comp.m_currentrevision)
         return false;
      if (!compare(m_dataLocale, comp.m_dataLocale))
         return false;
      if (!compare(m_dataVariant, comp.m_dataVariant))
         return false;
      if (m_editrevision != comp.m_editrevision)
         return false;
      if (!compare(m_fieldNameFieldMap, comp.m_fieldNameFieldMap))
         return false;
      if (!compare(m_relatedItemsMap, comp.m_relatedItemsMap))
         return false;
      if (m_requestedrevision != comp.m_requestedrevision)
         return false;
      if (m_revision != comp.m_revision)
         return false;
      if (m_revisionLock != comp.m_revisionLock)
         return false;
      if (!compare(m_systemLocale, comp.m_systemLocale))
         return false;

      return true;
   }

   // see interface for description
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call

      hash += hashBuilder(m_checkedOutBy);
      hash += hashBuilder(m_childNameChildMap);
      hash += m_contentId;
      hash += m_currentrevision;
      hash += hashBuilder(m_dataLocale);
      hash += hashBuilder(m_dataVariant);
      hash += m_editrevision;
      hash += hashBuilder(m_fieldNameFieldMap);
      hash += hashBuilder(m_itemDefinition);
      hash += hashBuilder(m_relatedItemsMap);
      hash += m_requestedrevision;
      hash += m_revision;
      hash += hashBuilder(m_revisionLock);
      hash += hashBuilder(m_systemLocale);

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

   @SuppressWarnings("unchecked") 
   void loadXmlData(Element sourceNode, boolean clearValues)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, EL_ITEM);

      int currentRevision = PSXMLDomUtil.checkAttributeInt(sourceNode, 
         ATTR_CURRENT_REVISION, false);
      if (currentRevision > 0)
         setCurrentRevision(currentRevision);

      int editRevision = PSXMLDomUtil.checkAttributeInt(sourceNode, 
         ATTR_EDIT_REVISION, false);
      if (editRevision > 0)
         setEditRevision(editRevision);

      int requestedRevision = PSXMLDomUtil.checkAttributeInt(sourceNode, 
         ATTR_REQUESTED_REVISION, false);
      if (requestedRevision > 0)
         setRequestedRevision(requestedRevision);

      String systemLocale = PSXMLDomUtil.checkAttribute(sourceNode, 
         ATTR_SYSTEM_LOCALE, false);
      if (!StringUtils.isBlank(systemLocale))
         setSystemLocale(new Locale(systemLocale));

      String dataLocale = PSXMLDomUtil.checkAttribute(sourceNode, 
         ATTR_DATA_LOCALE, false);
      if (!StringUtils.isBlank(dataLocale))
         setDataLocale(new Locale(dataLocale));

      String checkedOutBy = PSXMLDomUtil.checkAttribute(sourceNode, 
         ATTR_CHECKED_OUT_BY, false);
      if (!StringUtils.isBlank(checkedOutBy))
         setCheckedOutByName(checkedOutBy);

      // content key element (required)
      Element el = PSXMLDomUtil.getFirstElementChild(sourceNode, 
         EL_CONTENT_KEY);

      // set up the content id and revision, we do this mainly for the client
      // item which would not have done a "load" before calling loadXmlData
      int contentId = PSXMLDomUtil.checkAttributeInt(el, ATTR_CONTENT_ID, 
         false);
      if (contentId > 0)
         setContentId(contentId);

      int revision = PSXMLDomUtil.checkAttributeInt(el, ATTR_REVISION, false);
      if (revision > 0)
         setRevision(revision);

      el = PSXMLDomUtil.getNextElementSibling(el);

      PSItemChild child = null;
      PSItemField field = null;
      while (el != null)
      {
         String name = PSXMLDomUtil.getUnqualifiedNodeName(el);
         if (name.equals(PSItemField.EL_FIELD))
         {
            String fieldName = PSItemField.getName(el);
            field = (PSItemField)m_fieldNameFieldMap.get(fieldName);
            if (field == null)
            {
               Object[] args = { name, fieldName, PSItemFieldMeta.ATTR_NAME };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
                  args);
            }
            field.loadXmlData(el);
         }
         else if (name.equals(PSItemChild.EL_CHILD))
         {
            String childName = PSItemChild.getName(el);
            child = (PSItemChild)m_childNameChildMap.get(childName);
            if (child == null)
            {
               Object[] args = { name, childName, PSItemChild.ATTR_NAME };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
                  args);
            }
            child.loadXmlData(el, clearValues);
         }
         else if (name.equals(EL_RELATED_ITEMS))
         {
            if (clearValues)
            {
               m_relatedItemsMap.clear();
            }
            Element relEl =
               PSXMLDomUtil.getFirstElementChild(el, EL_RELATED_ITEM);

            while (relEl != null)
            {
               PSItemRelatedItem relatedItem = null;
               String relId = el.getAttribute(PSItemRelatedItem.ATTR_ID);
               if (relId != null && relId.trim().length() != 0)
               {
                  relatedItem = (PSItemRelatedItem)m_relatedItemsMap.get(relId);
               }

               if (relatedItem == null)
               {
                  // if not found or inserting a new one, add to the list
                  PSItemRelatedItem tmp = new PSItemRelatedItem(relEl);
                  int rId = tmp.getRelationshipId();
                  if (rId == -1)
                  {
                     rId = tmp.hashCode(); // some unique identifier
                  }
                  m_relatedItemsMap.put("" + rId, tmp);
               }
               else
               {
                  // updated existing relationship
                  relatedItem.loadXmlData(relEl);
               }
               relEl = PSXMLDomUtil.getNextElementSibling(relEl);
            }
            break;
         }
         el = PSXMLDomUtil.getNextElementSibling(el);
      }
   }

   /**
    * This method is called to populate an object from another core item.
    * It assumes that the object may already have a complete data structure,
    * therefore method only overlays the data onto the existing object.
    *
    * @param item the core item from which to merge, may not be
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void loadData(PSCoreItem item)
   {
      if (item == null)
         throw new IllegalArgumentException("item must not be null");
      
      int currentRev = item.getCurrentRevision();
      if (currentRev > 0)
      {
         setCurrentRevision(currentRev);
      }
      setEditRevision(item.getEditRevision());
      int requestedRev = item.getRequestedRevision();
      if (requestedRev > 0)
      {
         setRequestedRevision(requestedRev);
      }
      Locale sysLocale = item.getSystemLocale();
      if (sysLocale != null)
      {
         setSystemLocale(new Locale(sysLocale.toString()));
      }
      Locale dataLocale = item.getDataLocale();
      if (dataLocale != null)
      {
         setDataLocale(new Locale(dataLocale.toString()));
      }
      String checkedOutBy = item.getCheckedOutByName();
      if (!StringUtils.isBlank(checkedOutBy))
      {
         setCheckedOutByName(checkedOutBy);
      }
      int contentId = item.getContentId();
      if (contentId > 0)
      {
         setContentId(item.getContentId());
      }
      int revision = item.getRevision();
      if (revision > 0)
      {
         setRevision(revision);
      }
      
      // populate fields
      Iterator<PSItemField> fieldIter = item.getAllFields();
      while (fieldIter.hasNext())
      {
         PSItemField itemField = fieldIter.next();
         String itemFieldName = itemField.getName();
         PSItemField field = getFieldByName(itemFieldName);
         if (field == null)
         {
            // should not happen
            throw new RuntimeException("Field '" + itemFieldName
                  + "' could not be found.");
         }
         field.clearValues();
         Iterator<IPSFieldValue> values = itemField.getAllValues();
         while (values.hasNext())
         {
            field.addValue(values.next());
         }
      }
      
      // merge children
      Iterator<PSItemChild> childIter = item.getAllChildren();
      while (childIter.hasNext())
      {
         PSItemChild itemChild = childIter.next();
         String itemChildName = itemChild.getName();
         PSItemChild child = getChildByName(itemChildName);
         if (child == null)
         {
            // should not happen
            throw new RuntimeException("Child '" + itemChildName
                  + "' could not be found.");
         }
         Iterator<PSItemChildEntry> entryIter = itemChild.getAllEntries();
         while (entryIter.hasNext())
         {
            PSItemChildEntry entry = entryIter.next();
            int childRowId = entry.getChildRowId();
            if (childRowId < 0)
            {
               child.addEntry(entry);
            }
            else
            {
               PSItemChildEntry serverEntry = child.getChildEntryByRowId(childRowId);
               if (serverEntry == null)
               {
                  child.addEntry(entry);
               }
               else
               {
                  child.replaceEntry(entry);
               }
            }
         }
      }
      
      // merge related items
      Iterator<PSItemRelatedItem> relatedIter = item.getAllRelatedItems();
      while (relatedIter.hasNext())
      {
         PSItemRelatedItem relItem = relatedIter.next();
         PSItemRelatedItem serverItem = getRelatedItem(relItem.getRelationshipId());
         if (serverItem == null)
         {
            // if not found, add to the list
            addRelatedItem(relItem);
         }
         else
         {
            // update existing relationship
            serverItem.setRelatedType(relItem.getRelatedType());
            serverItem.setRelationshipId(relItem.getRelationshipId());
            serverItem.setAction(relItem.getAction());
            serverItem.setDependentId(relItem.getDependentId());
            serverItem.setRelatedItemData(relItem.getRelatedItemData());
            Iterator propsIter = relItem.getAllProperties();
            while (propsIter.hasNext())
            {
               String prop = (String) propsIter.next();
               serverItem.addProperty(prop, relItem.getProperty(prop));
            }
            Iterator keyFieldsIter = relItem.getAllKeyFields();
            while (keyFieldsIter.hasNext())
            {
               String keyField = (String) keyFieldsIter.next();
               serverItem.addKeyField(keyField, relItem.getKeyField(keyField));
            }
         }
      }
   }
   
   /**
    * Returns all field names of this item.,
    *
    * @return an unmodifiable <code>Iterator</code> with all field names 
    *    as <code>Strings</code>. May be empty but not <code>null</code>.
    */
   public Iterator<String> getAllFieldNames()
   {
      return Collections.unmodifiableSet(
         m_fieldNameFieldMap.keySet()).iterator();
   }

   /**
    * Returns the requested field.
    *
    * @param fieldName the case sensitive name of the field that will be
    *    returned if it exists in this item. Must not be empty or 
    *    <code>null</code>.
    *
    * @return the requested field, <code>null</code> if not found.
    */
   public PSItemField getFieldByName(String fieldName)
   {
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldname must not be null or empty");

      return (PSItemField)m_fieldNameFieldMap.get(fieldName);
   }

   /**
    * Returns all fields of this item.
    *
    * @return an unmodifiable <code>Iterator</code> of <code>PSItemField</code>
    *    objects. May be empty but not <code>null</code>.
    */
   public Iterator<PSItemField> getAllFields()
   {
      if (m_fieldNameFieldMap == null || m_fieldNameFieldMap.values() == null)
      {
         List<PSItemField> emptyList = Collections.emptyList();
         return emptyList.iterator();
      }

      return Collections.unmodifiableCollection(
         m_fieldNameFieldMap.values()).iterator();
   }

   /**
    * Returns all children of this item.
    *
    * @return an unmodifiable <code>Iterator</code> of <code>PSItemChild</code>
    *    objects. May be empty but not <code>null</code>.
    */
   public Iterator<PSItemChild> getAllChildren()
   {
      if (m_childNameChildMap == null || m_childNameChildMap.values() == null)
      {
         List<PSItemChild> emptyList = Collections.emptyList();
         return emptyList.iterator();
      }

      return Collections.unmodifiableCollection(
         m_childNameChildMap.values()).iterator();
   }

   /**
    * Returns all child names.
    *
    * @return unmodifiable <code>Iterator</code> of all of the
    *    <code>PSItemChild</code> names as <code>Strings</code>. May be empty 
    *    but not <code>null</code>.
    */
   public Iterator<String> getAllChildNames()
   {
      return Collections.unmodifiableSet(
         m_childNameChildMap.keySet()).iterator();
   }

   /**
    * Returns all related items.
    *
    * @return an unmodifiable <code>Iterator</code> with all 
    *    <code>PSItemRelatedItem</code> objects, never <code>null</code>,
    *    may be empty.
    */
   public Iterator<PSItemRelatedItem> getAllRelatedItems()
   {
      if (m_relatedItemsMap == null || m_relatedItemsMap.values() == null)
      {
         List<PSItemRelatedItem> emptyList = Collections.emptyList();
         return emptyList.iterator();
      }
         
      return Collections.unmodifiableCollection(
         m_relatedItemsMap.values()).iterator();
   }
   
   /**
    * Returns the requested related item.
    *
    * @param id of the relationship between the related item and this 
    *    item.  
    * 
    * @return PSItemRelatedItem may be <code>null</code> if it does not
    *    exist.
    */
   private PSItemRelatedItem getRelatedItem(int id)
   {
      return m_relatedItemsMap.get(String.valueOf(id));
   }
   
   /**
    * Set the supplied related items.
    * 
    * @param relatedItems the related item, may be <code>null</code> or empty.
    */
   public void setRelatedItems(Map<String, PSItemRelatedItem> relatedItems)
   {
      m_relatedItemsMap = relatedItems;
   }

   /** @todo: we need a mechanism for checking out an item **/

   /**
    * Returns the name of the user who has checked this item out.  This value
    * is set by the system.  If not checked out by the user, this item
    * cannot be saved.
    *
    * @return the name of the user who has checked this item out.  May be
    * empty if this is a new item or if <code>loadXmlData()</code> or
    *  <code>ServerItem.load()</code> hasn't been called.  Never
    *  <code>null</code>.
    */
   public String getCheckedOutByName()
   {
      return m_checkedOutBy;
   }

   /**
    * Sets the name of the user who has checked this item out.
    *
    * @see #getCheckedOutByName() getCheckedOutByName()
    * @param checkedOutBy a username of the person that has this item checked
    * out, may be empty, never <code>null</code>.
    */
   public void setCheckedOutByName(String checkedOutBy)
   {
      if (checkedOutBy == null)
         throw new IllegalArgumentException("checkedOutBy must not be null");

      m_checkedOutBy = checkedOutBy;
   }

   /**
    * Returns the <code>PSItemChild</code> specified by the <code>fieldName
    * </code> argument.
    *
    * @param childName the case sensitive name of the child that will be
    * returned if exists in this item.  Must not be empty or <code>null</code>.
    * @return PSItemChild may be <code>null</code> of it does not exist.
    */
   public PSItemChild getChildByName(String childName)
   {
      if (childName == null || childName.length() == 0)
         throw new IllegalArgumentException("childName must not be null or empty");

      return (PSItemChild)m_childNameChildMap.get(childName);
   }
   
   /**
    * Returns the <code>PSItemChild</code> specified by the supplied childId.
    *
    * @param childId the id of the child that will be returned if exists in this 
    * item.  
    * 
    * @return PSItemChild may be <code>null</code> of it does not exist.
    */
   public PSItemChild getChildById(int childId)
   {
      PSItemChild child = null;
      
      Iterator children = getAllChildren();
      while (children.hasNext() && child == null)
      {
         PSItemChild test = (PSItemChild)children.next();
         if (test.getChildId() == childId)
            child = test;
      }
      
      return child;
   }   

   /**
    * Returns the <code>contentid</code> of this item.
    *
    * @return int the contentid.  -1 if new item.
    */
   public int getContentId()
   {
      return m_contentId;
   }

   /**
    * Sets the <code>contentid</code> of this item.
    *
    * @param contentId must be > 0 (todo - this isn't validated, but I don't want
    * to change change the code now and possibly break something.)
    */
   public void setContentId(int contentId)
   {
      if (getContentId() >= 0 && contentId < 1)
         throw new IllegalArgumentException("contentid can't be < 1");

      m_contentId = contentId < 1 ? INVALID_CONTENTID : contentId;
      resetLocator();
   }
   
   /**
    * Resets the locator based on the content id and revision of the member data.
    * This should be called whenever one of the member data {@link #m_contentId}
    * and {@link #m_revision} is changed.
    */
   private void resetLocator()
   {
      if (m_contentId == INVALID_CONTENTID)
      {
         setLocator(new PSLocator());
      }
      else
      {
         if (m_revision == INVALID_REVISION)
            setLocator(new PSLocator(m_contentId));
         else
            setLocator(new PSLocator(m_contentId, m_revision));
      }
      
   }
   
   /**
    * Resets all revisions to {@link #INVALID_REVISION}. The new Java API and
    * web services do not expose the revisios anymore to the user and will be
    * reset before its returned.
    */
   public void resetRevisions()
   {
      m_revision = INVALID_REVISION;
      m_currentrevision = INVALID_REVISION;
      m_editrevision = INVALID_REVISION;
      
      resetLocator();
   }

   /**
    * Returns the <code>revision</code> of this item.  The revision cannot
    * be incremented.
    *
    * @see #INVALID_REVISION INVALID_REVISION
    * @return the revision for this item. If a new items is requested the
    *    INVALID_REVISION will be returned.
    */
   public int getRevision()
   {
      return m_revision;
   }

   /**
    * Returns the <code>dataVariant</code> of this item.  For now it will only
    * return <code>sys_All</code>
    *
    * @return never <code>null</code> or empty.
    */
   public String getDataVariant()
   {
      return m_dataVariant;
   }

   /**
    * Returns the <code>PSItemDefinition</code> that defines this object.
    * It should be treated as Read only.
    *
    * @return never <code>null</code>
    */
   public PSItemDefinition getItemDefinition()
   {
      return m_itemDefinition;
   }

   /**
    * Sets the <code>revision</code> of this item.
    *
    * @param revision must be > 0;
    *
    * @see #getRevision()
    */
   public void setRevision(int revision)
   {
      if (getContentId() >= 0 && revision < 1)
         throw new IllegalArgumentException("revision can't be < 1");

      m_revision = revision;
      resetLocator();
   }

   /**
    * Returns the <code>contentTypeId</code> of this item.
    *
    * @return int must be >= 0.
    */
   public long getContentTypeId()
   {
      return m_itemDefinition.getContentEditor().getContentType();
   }

   /** @todo: move requested revision methods to derived classes,
    *  the set should be removed/moved into serveritem **/
   /**
    * Returns the <code>revisionid</code> that was requested.  It may not be
    * the same revision that is returned.  If the requested revision is
    * has the revision lock set, a new version of the item will be cloned and
    * returned with the new <code>revisionid</code>.  If a new items is
    * requested the INVALID_REVISION will be returned.
    *
    * @see #INVALID_REVISION INVALID_REVISION
    * @return see this methods description.
    */
   public int getRequestedRevision()
   {
      return m_requestedrevision;
   }

   /**
    * Sets the revision provided for in the original request for the item.
    *
    * @see #getRequestedRevision()
    * @param requestedRevision must be > 0.
    */
   public void setRequestedRevision(int requestedRevision)
   {
      if (getContentId() >= 0 && requestedRevision < 1)
         throw new IllegalArgumentException("requestedRevision must be > 0");

      m_requestedrevision = requestedRevision;
   }

   /**
    * Returns the current <code>revisionid</code>.
    *
    * @see #INVALID_REVISION INVALID_REVISION
    * @return the current revision is the revision in the contentstatus table,
    * if there is none the default {@link #INVALID_REVISION } will be returned.
    */
   public int getCurrentRevision()
   {
      return m_currentrevision;
   }

   /**
    * Sets the current <code>revisionid</code>.  The current revision is
    * the revision in the contentstatus table.
    *
    * @param currentRevision must be > 0.
    */
   public void setCurrentRevision(int currentRevision)
   {
      if (getContentId() >= 0 && currentRevision < 1)
         throw new IllegalArgumentException("currentRevision must be > 0");

      m_currentrevision = currentRevision;
   }

   /**
    * Returns the edit <code>revisionid</code>.   The edit revision is tip
    * revision.  The revision being returned for editing.
    *
    * @see #INVALID_REVISION INVALID_REVISION
    * @return if this is a new item the default {@link #INVALID_REVISION }
    * will be returned.
    */
   public int getEditRevision()
   {
      return m_editrevision;
   }

   /**
    * Sets the edit <code>revisionid</code>.   The edit revision is tip
    * revision.  The revision being returned for editing.
    *
    * @see #getEditRevision()
    * @param editRevision may be < 0
    */
   public void setEditRevision(int editRevision)
   {
      m_editrevision = editRevision;
   }

   /**
    * Returns the total number of revisions.  This is the same value as the
    * edit revision.
    *
    * @return if there are not any revisions INVALID_REVISION will be returned.
    */
   public int getRevisionCount()
   {
      return m_editrevision;
   }

   /**
    * Returns the <code>Locale</code> used by the system.
    *
    * @return Locale may be <code>null</code>.
    */
   public Locale getSystemLocale()
   {
      return m_systemLocale;
   }

   /**
    * Sets the <code>Locale</code> of the system that is using this item.  This
    * is expected to be set by the client.  This is not the "Rhythmyx"
    * <code>Locale</code>.
    *
    * @param systemLocale the <code>Locale</code> to set.  Must not be
    * <code>null</code>.
    */
   public void setSystemLocale(Locale systemLocale)
   {
      if (systemLocale == null)
         throw new IllegalArgumentException("systemLocale cannot be null");

      m_systemLocale = systemLocale;
   }

   /**
    * Returns the <code>Locale</code> of this items data.  This is the value
    * that's set in the contentstatus table.
    *
    * @return Locale may be <code>null</code>.
    */
   public Locale getDataLocale()
   {
      return m_dataLocale;
   }

   /**
    * Sets the <code>Locale</code> of the data of the item.
    *
    * @param dataLocale the <code>Locale</code> to set.  Must not be <code>null
    * </code>.
    */
   public void setDataLocale(Locale dataLocale)
   {
      if (dataLocale == null)
         throw new IllegalArgumentException("dataLocale cannot be null");

      m_dataLocale = dataLocale;
   }
   
   /**
    * Set the folder paths of this item.
    * 
    * @param paths a list of fully qualified folder paths of all folders to
    *    which this item is attached, may be <code>null</code> or empty.
    */
   public void setFolderPaths(List<String> paths)
   {
      m_folderPaths = paths;
   }
   
   /**
    * Get all folder paths of this item.
    * 
    * @return a list of fully qualified folder paths of all folders to
    *    which this item is attached, may be <code>null</code> or empty.
    */
   public List<String> getFolderPaths()
   {
      return m_folderPaths;
   }

   /**
    * Sets the text field value for the specified field name if it exists.
    * 
    * @param fieldName the field name, may not be blank.
    * @param value the new value of the field, may be blank.
    */
   public void setTextField(String fieldName, String value)
   {
      if (StringUtils.isBlank(fieldName))
         throw new IllegalArgumentException("fieldName may not be blank");
      
      PSItemField textFld = getFieldByName(fieldName);
      if (textFld != null)
      {
         textFld.addValue(new PSTextValue(value));
      }
   }
   
   /**
    * The <code>Locale</code> requested by the caller.
    */
   private Locale m_systemLocale;

   /**
    * @see #getDataLocale()
    */
   private Locale m_dataLocale;

   /**
    * The content id of this item, default is -1 that will be used for new
    * items, this value will change if this item is an existing item.
    */
   private int m_contentId = INVALID_CONTENTID;

   /**
    * The revision of this item, INVALID_REVISION if new item, otherwise will
    * be > 0.
    */
   private int m_revision = INVALID_REVISION;

   /**
    * @see #getRequestedRevision()
    */
   private int m_requestedrevision = INVALID_REVISION;

   /**
    * The revision that is in the contentstatus table for the current content
    * item, default is INVALID_REVISION, will not be invalid if this is an
    * existing item.
    */
   private int m_currentrevision = INVALID_REVISION;

   /**
    * @see #getEditRevision()
    */
   private int m_editrevision = INVALID_REVISION;

   /**
    * The data variant to use for this item.
    */
   private String m_dataVariant = IPSConstants.SYS_ALL_VIEW_NAME;

   /**
    * The definition used to define this item.  Never <code>null</code>.
    * @see #getItemDefinition()
    */
   private PSItemDefinition m_itemDefinition;

   /**
    * Map of the fields in the item.  Field name is key <code>PSItemField
    * </code> is the value, never <code>null</code>, may be empty, initialized
    * by <code>init()</code>.
    * @see #getFieldByName(String)
    */
   private Map<String, PSItemField> m_fieldNameFieldMap = null;

   /**
    * Map of the childs in the item, child name is key <code>PSItemChild
    * </code> is the value, never <code>null</code>, may be empty, initialized
    * by <code>init()</code>.
    * @see #getChildByName(String)
    */
   private Map<String, PSItemChild> m_childNameChildMap = null;

   /**
    * @see #getCheckedOutByName() getCheckedOutByName()
    */
   private String m_checkedOutBy = "";

   /**
    * @see #isRevisionLocked()
    */
   private boolean m_revisionLock = false;
   
   /**
    * A list of folder paths to which this item belongs, may be 
    * <code>null</code> or empty. This is not persisted nor is it included
    * in the XML representation of this object. It is currently only used
    * to pass the folder paths to the webservice converter.
    */
   private transient List<String> m_folderPaths = null;

   /**
    * Map of the related items in the item.  Relationship id is the key
    * <code>PSItemRelatedItem</code> is the value.
    */
   public Map<String, PSItemRelatedItem> m_relatedItemsMap = null;

   /** Name of the elements  in this class' XML representation */
   public static final String EL_ITEM = "Item";
   public static final String ATTR_CONTENTTYPE = "contentType";
   public static final String ATTR_REQUESTED_REVISION = "requestedRevision";
   public static final String ATTR_CURRENT_REVISION = "currentRevision";
   public static final String ATTR_EDIT_REVISION = "editRevision";
   public static final String ATTR_REVISION_COUNT = "revisionCount";
   public static final String ATTR_REVISION_LOCK = "revisionLock";
   public static final String ATTR_SYSTEM_LOCALE = "systemLocale";
   public static final String ATTR_DATA_LOCALE = "dataLocale";
   public static final String ATTR_CHECKED_OUT_BY = "checkedOutBy";

   public static final String EL_CONTENT_KEY = "ContentKey";
   public static final String ATTR_CONTENT_ID = "contentId";
   public static final String ATTR_REVISION = "revision";
   public static final String ATTR_DATAVARIANT = "dataVariant";
   public static final String EL_RELATED_ITEMS = "RelatedItems";
   public static final String EL_RELATED_ITEM = "RelatedItem";

   /**
    * The value to incidate that a revision is not valid to the item and that
    * it may be ignored.  For example new items do not have any revision value
    *  and empty or <code>null</code> cannot be used for Java primitives like
    *  <code>int</code>, therefore this value will be used to indicate that a
    *  revision is invalid.
    */
   public static final int INVALID_REVISION = -1;
   
   /**
    * This value is used to indicate an id has not been set on this object yet.
    * This occurs for new objects.
    */
   public static final int INVALID_CONTENTID = -1;
}
