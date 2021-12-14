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

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * An object representation of the sys_StandardItem.xsd ChildEntry element.
 */
public class PSItemChildEntry extends PSItemComponent 
   implements IPSItemAccessor
{
   /**
    * Construct a new instance from definitions.
    *
    * @param fieldSet must not be <code>null</code> must be of type
    *    <code>PSFieldSet.TYPE_COMPLEX_CHILD</code>
    */
   public PSItemChildEntry(PSFieldSet fieldSet)
   {
      if (fieldSet == null || 
         fieldSet.getType() != PSFieldSet.TYPE_COMPLEX_CHILD)
         throw new IllegalArgumentException(
            "fieldSet must not be null; must be PSFieldSet.TYPE_COMPLEX_CHILD");

      m_fieldSetDef = fieldSet;
      init();
   }

   /**
    * Initializes object members. Called by ctors and clone.
    */
   private void init()
   {
      m_fieldNameFieldMap = new TreeMap<>();

   }

   /**
    * Every <code>PSItemChildEntry</code> may have 0 or more fields.  This
    * returns a field specified by its name.
    *
    * @param fieldName Must not be <code>null</code> or empty.
    * @return May be <code>null</code> if the field doesn't exist.
    */
   public PSItemField getFieldByName(String fieldName)
   {
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldName must not be null or empty");

      return (PSItemField)m_fieldNameFieldMap.get(fieldName);
   }

   /**
    * Every <code>PSItemChildEntry</code> may have a field or a collection
    * of fields.  This returns the entire collection.
    *
    * @return unmodifiable <code>Iterator</code> of <code>PSItemField</code>
    * objects. May be empty but not <code>null</code>.
    */
   public Iterator<PSItemField> getAllFields()
   {
      return Collections.unmodifiableCollection(
         m_fieldNameFieldMap.values()).iterator();
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ChildRowId=" + getChildRowId() + ", Action=" + getAction() + 
         ", AllFieldNames=" + m_fieldNameFieldMap.keySet();
   }
   /**
    * Every <code>PSItemChildEntry</code> may have a field or a
    * collection of fields.  This returns all of the <code>PSItemField
    * </code> names as <code>Strings</code>
    *
    * @return unmodifiable <code>Iterator</code> of the
    * <code>PSItemField</code> names as <code>Strings</code>.
    * May be empty but not <code>null</code>.
    */
   public Iterator<String> getAllFieldNames()
   {
      return Collections.unmodifiableSet(
         m_fieldNameFieldMap.keySet()).iterator();
   }

   /**
    * Adds field to the implemented object from the visitor.
    *
    * @param visitor The visitor to accept, never <code>null</code>.
    */
   public void accept(IPSVisitor visitor)
   {
      if (visitor.getObject() instanceof PSItemField)
         addField((PSItemField)visitor.getObject());

      if (visitor.getObject() instanceof Integer)
         m_childRowId = ((Integer)visitor.getObject()).intValue();
   }

   /**
    * Adds <code>PSItemField</code> to the <code>map</code>.
    *
    * @param field assumed not <code>null</code>.
    */
   private void addField(PSItemField field)
   {
      m_fieldNameFieldMap.put(field.getName(), field);
   }

   /**
    * Gets an action to be taken on the child entry.  Only one action per item
    * entry.
    * 
    * @return The action, never <code>null</code> or empty 
    * 
    * @see #setAction(String)
    */
   public String getAction()
   {
      return m_action;
   }

   /**
    * Sets an action to be taken on the child entry by the system.  Only one
    * action per item entry.
    *
    * <ul>
    * <li>{@link PSItemChildEntry#CHILD_ACTION_IGNORE } - default
    * </li>
    * <li>{@link PSItemChildEntry#CHILD_ACTION_UPDATE }
    * </li>
    * <li>{@link PSItemChildEntry#CHILD_ACTION_DELETE }
    * </li>
    * <li>{@link PSItemChildEntry#CHILD_ACTION_INSERT }
    * </li>
    * </ul>
    * @param childAction - must be one of the value listed above.
    */
   public void setAction(String childAction)
   {
      if (childAction == null || childAction.length() == 0)
         throw new IllegalArgumentException(
            "childAction must not be null or empty");

      if (!childAction.equals(CHILD_ACTION_DELETE)
         && !childAction.equals(CHILD_ACTION_IGNORE)
         && !childAction.equals(CHILD_ACTION_INSERT)
         && !childAction.equals(CHILD_ACTION_UPDATE))
         throw new IllegalArgumentException(
            "childAction must be a valid action");

      m_action = childAction;
   }

   /**
    * Returns the child row id if there is one.  This is the internal id of
    * this childEntry.  This id is set upon first insertion in the system.
    *
    * @return if the child row id has not been set -1 will be returned,
    * otherwise > 0,
    */
   public int getChildRowId()
   {
      return m_childRowId;
   }

   /**
    * Set the child row id to the specified value. This is the internal id of
    * this childEntry object.
    * 
    * @param childRowId the child row id of this childEntry
    */
   public void setChildRowId(int childRowId)
   {
      m_childRowId = childRowId;
   }


   /**
    * @see PSItemComponent#toXml(Document, PSAcceptElements)
    */
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = createStandardItemElement(doc, EL_CHILD_ENTRY);

      if (m_childRowId >= 0)
         root.setAttribute(ATTR_CHILD_ROW_ID, "" + m_childRowId);

      root.setAttribute(ATTR_ACTION, m_action);

      // fields:
      if (!m_fieldNameFieldMap.isEmpty())
         toXmlCollection(root, doc, m_fieldNameFieldMap.values(),
            acceptElements);

      return root;

   }

   /**
    * Convenience method to <code>toXml(doc, null)</code>
    * 
    * @see #toXml(Document, PSAcceptElements)
    */
   public Element toXml(Document doc)
   {
      return toXml(doc, null);

   }

   //see interface for description
   public Object clone()
   {
      PSItemChildEntry copy = null;

      copy = (PSItemChildEntry)super.clone();
      copy.init();

      Iterator i = m_fieldNameFieldMap.keySet().iterator();
      while (i.hasNext())
      {
         String key = (String)i.next();
         PSItemField val = (PSItemField)m_fieldNameFieldMap.get(key);
         copy.addField((PSItemField)val.clone());
      }

      return copy;
   }

   //see interface for description
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSItemChildEntry comp = (PSItemChildEntry)obj;

      if (m_childRowId != comp.m_childRowId)
         return false;
      if (!compare(m_action, comp.m_action))
         return false;
      if (!compare(m_fieldSetDef, comp.m_fieldSetDef))
         return false;
      if (!compare(m_fieldNameFieldMap, comp.m_fieldNameFieldMap))
         return false;

      return true;
   }

   //see interface for description
   public int hashCode()
   {
      int hash = 0;
      // super is abtract, don't call
      hash += hashBuilder(m_action);
      hash += hashBuilder(m_fieldSetDef);
      hash += hashBuilder(m_fieldNameFieldMap);
      hash += m_childRowId;

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

   /**
    * Load this object state from XML data.
    * 
    * @param sourceNode The source node to use, may not be <code>null</code>.
    * @param clearValues Ignored
    * 
    * @throws PSUnknownNodeTypeException If the source node is malformed.
    */
   void loadXmlData(Element sourceNode, boolean clearValues)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, EL_CHILD_ENTRY);

      // set row id if exist
      int rowId = PSXMLDomUtil.checkAttributeInt(sourceNode, 
         ATTR_CHILD_ROW_ID, false);

      // if the row id exist (not -1), make sure the row id from XML element 
      // is the same with the current object itself
      if (rowId != -1 && m_childRowId != -1 && rowId != m_childRowId)
      {
         throw new IllegalStateException(
            "LoadXmlData to wrong child entry: "
               + "m_childRowId("
               + m_childRowId
               + ") != "
               + ATTR_CHILD_ROW_ID
               + "("
               + rowId
               + ")");
      }

      if (rowId != -1 && m_childRowId == -1)
         m_childRowId = rowId;

      // set the action
      setAction(PSXMLDomUtil.checkAttribute(sourceNode, ATTR_ACTION, false));

      // update all the child fields
      Element el = PSXMLDomUtil.getFirstElementChild(sourceNode);
      PSItemField field = null;
      while (el != null && 
         PSXMLDomUtil.getUnqualifiedNodeName(el).equals(PSItemField.EL_FIELD))
      {
         Element fieldMeta =
            PSXMLDomUtil.getFirstElementChild(
               el,
               PSItemFieldMeta.EL_FIELD_META);
         String attrName = PSItemFieldMeta.getName(fieldMeta);
         field = (PSItemField)m_fieldNameFieldMap.get(attrName);
         if (field == null)
         {
            Object[] args = { PSItemFieldMeta.EL_FIELD_META, attrName,
               PSItemFieldMeta.ATTR_NAME };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         field.loadXmlData(el);

         el = PSXMLDomUtil.getNextElementSibling(el);
      }
   }

   /**
    * Gets the action of this child entry.
    *
    * @param el the element to retrieve the action from, must not be <code>
    * null</code>
    *
    * @return the action of this child entry, never <code>null</code>, may be
    * empty
    */
   static String getAction(Element el) throws PSUnknownNodeTypeException
   {
      return PSXMLDomUtil.checkAttribute(el, ATTR_ACTION, false);
   }

   /**
    * Gets the row id of this child entry.
    *
    * @param el the element to retrieve the row id from, must not be <code>
    * null</code>
    *
    * @return the row id of this child entry, if not found returns -1, otherwise
    * returns > 0
    */
   static int getRowId(Element el) throws PSUnknownNodeTypeException
   {
      return PSXMLDomUtil.checkAttributeInt(el, ATTR_CHILD_ROW_ID, false);
   }

   /**
    * Get this entries full guid, which also contains the content type id and 
    * child id.  See {@link #setGUID(PSLegacyGuid)} for more info.
    * 
    * @return The guid, may be <code>null</code> if not set.
    */
   public PSLegacyGuid getGUID()
   {
      return m_guid;
   }
   
   /**
    * Set this entries full guid, which also contains the content type id and 
    * child id, used for web services conversion, value is transient and not
    * considered by {@link #equals(Object)} or {@link #hashCode()} and is not
    * serialized as part of this object's XML representation.
    * 
    * @param guid The guid, may be <code>null</code> to clear it.
    */
   public void setGUID(PSLegacyGuid guid)
   {
      m_guid = guid;
   }
   
   /**
    * Definition of this child entry, set by ctor, never <code>null</code>
    * never changes.
    */
   private PSFieldSet m_fieldSetDef;

   /**
    * The action this child may take. @see #setAction(String)
    */
   private String m_action = CHILD_ACTION_IGNORE;

   /**
    * This is a map of with the fieldName as the key and PSItemField as
    * its value, initialized by <code>init()</code>,
    * @see #getFieldByName(String)
    */
   private Map<String, PSItemField> m_fieldNameFieldMap;
   
   /**
    * Guid set on this object used only for conversion purposes.
    */
   private transient PSLegacyGuid m_guid = null;

   /** child action choices, @see #setAction(String) */
   public final static String CHILD_ACTION_IGNORE = "ignore";
   public final static String CHILD_ACTION_UPDATE = "update";
   public final static String CHILD_ACTION_DELETE = "delete";
   public final static String CHILD_ACTION_INSERT = "insert";

   /**
    * The identifier for this object, default is -1, set when this object is
    * created.
    */
   private int m_childRowId = -1;

   /** Name of the root element in this class' XML representation */
   public static final String ATTR_CHILD_ROW_ID = "childRowId";
   public static final String ATTR_ACTION = "action";
   public static final String EL_CHILD_ENTRY = "ChildEntry";
}
