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

import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
/**
 * An object representation of the StandardItem RelatedItem element.
 */
public class PSItemRelatedItem extends PSItemComponent
{
   public PSItemRelatedItem()
   {
   }

   /**
    * Construct using xml nodes
    */
   public PSItemRelatedItem(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode cannot be null");

      loadXmlData(sourceNode);
   }

   /**
    * Sets the relationship id for this related item. This is the internal id
    * assigned to each relationship.
    *
    * @param relationshipId the id to set this related item to
    */
   public void setRelationshipId(int relationshipId)
   {
      m_relationshipId = relationshipId;
   }

   /**
    * Returns the internal relationship id for this related item.
    *
    * @return the unique internal relationship id.
    */
   public int getRelationshipId()
   {
      return m_relationshipId;
   }
   
   /**
    * Get the relationship which leads to this related item.
    * 
    * @return the relationship, may be <code>null</code>.
    */
   public PSAaRelationship getRelationship()
   {
      return m_relationship;
   }
   
   /**
    * Set the relationship which leads to this related item.
    * 
    * @param relationship the new relationship, may be <code>null</code>.
    */
   public void setRelationship(PSAaRelationship relationship)
   {
      m_relationship = relationship;
   }

   /**
    * Sets an action to be taken on the item.  Only one action per item.
    *
    * @param action the action to take. Must not be <code>null</code>, empty
    *    or invalid action.
    */
   public void setAction(String action)
   {
      m_action = PSRelatedItemAction.valueOf(action.toUpperCase());
   }

   /**
    * Gets an action to be taken on the related item.  Only one action per item
    * related item.
    * 
    * @return the action to take, a value defined in 
    *    <code>PSRelatedItemAction</code>.
    */
   public String getAction()
   {
      return m_action.toString().toLowerCase();
   }

   /**
    * Set the relationship type.
    *
    * @param relatedType the name of the relationship type, not 
    *    <code>null</code> or empty.
    */
   public void setRelatedType(String relatedType)
   {
      if (relatedType == null)
         throw new IllegalArgumentException("relatedType cannot be null");

      relatedType = relatedType.trim();
      if (relatedType.length() == 0)
         throw new IllegalArgumentException("relatedType cannot be empty");
      
      m_relatedType = relatedType;
   }

   /**
    * Get the relationship type of the related item.
    * 
    * @return the relationship type of the related item, never <code>null</code>
    *    or empty.
    */
   public String getRelatedType()
   {
      return m_relatedType;
   }

   /**
    * Adds the specified key and value to the property map, if the key already
    * exists it's value will be overridden.
    *
    * @param key describes the key for the property to be added, must not be
    * <code>null</code> or empty
    *
    * @param value the value for the key, must not be <code>null</code> or empty
    */
   public void addProperty(String key, String value)
   {
      m_propertyMap.put(key, value);
   }

   /**
    * Removes the specified property from the list.
    *
    * @param key the key to remove from the property list, must not be
    * <code>null</code> or empty
    */
   public void removeProperty(String key)
   {
      m_propertyMap.remove(key);
   }

   /**
    * Gets the specified property from the list of properties for this related
    * item.
    *
    * @param key the key to get the data for, must not be <code>null</code>
    * or empty
    *
    * @return the value for the specified key, may be empty
    */
   public String getProperty(String key)
   {
      return m_propertyMap.get(key);
   }

   /**
    * Returns all of the <code>keys</code> as <code>Strings</code>
    *
    * @return unmodifiable <code>Iterator</code> of all of the
    * <code>key</code> names as <code>Strings</code>.
    * May be empty but not <code>null</code>.
    */
   public Iterator getAllProperties()
   {
      return Collections
         .unmodifiableCollection(m_propertyMap.keySet())
         .iterator();
   }

   /**
    * Sets the content id of this related item.
    *
    * @param dependentId the content id of the related item, must be > 0 and
    * a valid content id
    */
   public void setDependentId(int dependentId)
   {
      m_dependentId = dependentId;
   }

   /**
    * Returns the id of this related content item.
    *
    * @return internal content id of the related item
    */
   public int getDependentId()
   {
      return m_dependentId;
   }

   /**
    * Returns the document of related item data to be inserted if it does
    * not exist. After this data is inserted, we then relate to the new
    * content id.
    *
    * @param relatedItemData the data that is a Standard item, must not be
    * <code>null</code> or empty, may contain just the content key or a
    * complete standard item, see <code>sys_StandardItem.xsd</code> for more
    * info.
    */
   public void setRelatedItemData(Element relatedItemData)
   {
      m_relatedItemData = relatedItemData;
   }

   /**
    * Returns the document of related item data to be inserted if it does
    * not exist. After this data is inserted, we then relate to the new
    * content id.
    *
    * @return This data is a Standard item, must not be <code>null</code> or
    * empty, may contain just the content key or a complete standard item,
    * see <code>sys_StandardItem.xsd</code> for more info.
    */
   public Element getRelatedItemData()
   {
      return m_relatedItemData;
   }

   /**
    * Adds a key field to use for the search.
    *
    * @param key a field name to use for searching for a content item, this may
    * be a system, shared, or local field name, must not be <code>null</code> or
    * empty
    *
    * @param el
    */
   public void addKeyField(String key, Element el)
   {
      m_keyFieldsMap.put(key, el);
   }

   /**
    * Removes the specified key field from the map to use for the search.
    *
    * @param key the field name to remove from the search map, must not be
    * <code>null</code> or empty
    */
   public void removeKeyField(String key)
   {
      m_keyFieldsMap.remove(key);
   }

   /**
    * Gets the specified keyfield from the list of keyfields for this related
    * item.
    *
    * @param key the key to get the data for, must not be <code>null</code>
    * or empty
    *
    * @return the value for the specified key, may be empty
    */
   public Element getKeyField(String key)
   {
      return (Element)m_keyFieldsMap.get(key);
   }

   /**
    * Returns all of the <code>keys</code> of the keyfieldmap as
    * <code>Strings</code>
    *
    * @return unmodifiable <code>Iterator</code> of all of the
    * <code>key</code> names as <code>Strings</code>.
    * May be empty but not <code>null</code>.
    */
   public Iterator getAllKeyFields()
   {
      return Collections
         .unmodifiableCollection(m_keyFieldsMap.keySet())
         .iterator();
   }

   /**
    * @see PSItemComponent#toXml(Document, PSAcceptElements)
    */
   @Override
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = createStandardItemElement(doc, EL_RELATED_ITEM);
      root.setAttribute(ATTR_RELATED_TYPE, m_relatedType);

      root.setAttribute(ATTR_ID, "" + m_relationshipId);
      root.setAttribute(ATTR_ACTION, m_action.toString());

      if (m_relatedItemData != null)
      {
         Node importNode = doc.importNode(m_relatedItemData, true);
         root.appendChild(importNode);
      }

      Iterator propIter = m_propertyMap.keySet().iterator();
      while (propIter.hasNext())
      {
         String key = (String)propIter.next();
         String val = m_propertyMap.get(key);

         Element property = createStandardItemElement(doc, EL_PROPERTY);
         property.setAttribute(ATTR_NAME, key);
         Text tmp = doc.createTextNode(val);
         property.appendChild(tmp);

         root.appendChild(property);
      }

      if (!m_keyFieldsMap.isEmpty())
      {
         Element keyFields = createStandardItemElement(doc, EL_KEY_FIELDS);

         Iterator iter = m_keyFieldsMap.keySet().iterator();
         while (iter.hasNext())
         {
            String key = (String)iter.next();
            Element searchField = (Element)m_keyFieldsMap.get(key);

            keyFields.appendChild(searchField);
         }
         root.appendChild(keyFields);
      }
      return root;
   }

   /**
    * Convenience method to <code>toXml(doc, null)</code>.
    * @see #toXml(Document, PSAcceptElements)
    */
   @Override
   public Element toXml(Document doc)
   {
      return toXml(doc, null);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSItemRelatedItem copy = null;

      copy = (PSItemRelatedItem)super.clone();

      if (m_relatedItemData != null)
         copy.m_relatedItemData = (Element)m_relatedItemData.cloneNode(true);

      if (m_propertyMap != null)
      {
         copy.m_propertyMap = new HashMap<>();
         Iterator i = m_propertyMap.keySet().iterator();
         while (i.hasNext())
         {
            String key = (String)i.next();
            String val = m_propertyMap.get(key);
            copy.m_propertyMap.put(key, val);
         }
      }

      if (m_keyFieldsMap != null)
      {
         copy.m_keyFieldsMap = new HashMap<>();
         Iterator i = m_keyFieldsMap.keySet().iterator();
         while (i.hasNext())
         {
            String key = (String)i.next();
            Element el = (Element)m_keyFieldsMap.get(key);
            copy.m_keyFieldsMap.put(key, el.cloneNode(true));
         }
      }
      return copy;
   }

   //see interface for description
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSItemRelatedItem compMeta = (PSItemRelatedItem)obj;

      // checking references:
      if (!compare(m_propertyMap, compMeta.m_propertyMap))
         return false;
      if (!compare(m_keyFieldsMap, compMeta.m_keyFieldsMap))
         return false;
      if (!equalXmlElements(m_relatedItemData, compMeta.m_relatedItemData))
         return false;
      if (!compare(m_action, compMeta.m_action))
         return false;
      if (!compare(m_relatedType, compMeta.m_relatedType))
         return false;

      // check primitives:
      if (m_relationshipId != compMeta.m_relationshipId)
         return false;
      if (m_dependentId != compMeta.m_dependentId)
         return false;

      return true;
   }
   
   /**
    * Tests if the two supplied xml elements are equal.
    * 
    * @param a the first element to compare, may be <code>null</code>.
    * @param b the second element to compare, may be <code>null</code>.
    * @return true if the two supplied elements are equal, <code>false</code>
    *    otherwise.
    */
   private boolean equalXmlElements(Element a, Element b)
   {
      if ((a == null) && (b != null))
         return false;
      if ((a != null) && (b == null))
         return false;
      if ((a != null) && (b != null))
      {
         if (!PSXmlDocumentBuilder.toString(a).equals(
            PSXmlDocumentBuilder.toString(b)))
            return false;
      }
      
      return true;
   }

   //see interface for description
   @Override
   public int hashCode()
   {
      int hash = 0;
      // super is abtract, don't call
      hash += hashBuilder(m_propertyMap);
      hash += hashBuilder(m_keyFieldsMap);
      hash += hashBuilder(m_relatedItemData);
      hash += hashBuilder(m_action);
      hash += hashBuilder(m_relatedType);
      hash += m_dependentId;
      hash += m_relationshipId;

      return hash;
   }

   // @see IPSDataComponent
   @Override
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
    * @param sourceNode the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by this class.
    */
   @Override
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
      PSXMLDomUtil.checkNode(sourceNode, EL_RELATED_ITEM);
      setRelatedType(
         PSXMLDomUtil.checkAttribute(sourceNode, ATTR_RELATED_TYPE, true));
      setRelationshipId(
         PSXMLDomUtil.checkAttributeInt(sourceNode, ATTR_ID, false));
      setAction(PSXMLDomUtil.checkAttribute(sourceNode, ATTR_ACTION, false));

      // get the item to be related
      Element el =
         PSXMLDomUtil.getFirstElementChild(sourceNode, PSServerItem.EL_ITEM);
      Element contentKey =
         PSXMLDomUtil.getFirstElementChild(el, PSServerItem.EL_CONTENT_KEY);
      int contentId =
         PSXMLDomUtil.checkAttributeInt(
            contentKey,
            PSServerItem.ATTR_CONTENT_ID,
            false);

      setDependentId(contentId);
      setRelatedItemData(el);

      el = PSXMLDomUtil.getNextElementSibling(el);
      while (el != null
         && PSXMLDomUtil.getUnqualifiedNodeName(el).equals(EL_PROPERTY))
      {
         String name = PSXMLDomUtil.checkAttribute(el, ATTR_NAME, true);
         String val = PSXMLDomUtil.getElementData(el);
         m_propertyMap.put(name, val);

         el = PSXMLDomUtil.getNextElementSibling(el);
      }

      if (el != null
         && PSXMLDomUtil.getUnqualifiedNodeName(el).equals(EL_KEY_FIELDS))
      {
         el = PSXMLDomUtil.getFirstElementChild(el);
         while (el != null
            && PSXMLDomUtil.getUnqualifiedNodeName(el).equals(EL_SEARCH_FIELD))
         {
            String name = PSXMLDomUtil.checkAttribute(el, ATTR_NAME, true);
            m_keyFieldsMap.put(name, el);

            el = PSXMLDomUtil.getNextElementSibling(el);
         }
      }
   }
   
   /**
    * Enumerates all valid actions.
    */
   public enum PSRelatedItemAction
   {
      IGNORE,
      INSERT,
      UPDATE,
      DELETE
   }

   /**
    * Storage for the type of related content, never <code>null</code> or
    * empty, defaults to <code>Active Assembly</code>.
    */
   private String m_relatedType = PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY;

   /**
    * Storage for the action to be performed on this particular related
    * content, valid values are defined in <code>sys_StandardItem.xsd</code>
    */
   private PSRelatedItemAction m_action = PSRelatedItemAction.IGNORE;

   /**
    * Storage for the property and values for the related item,
    * contains a list of property values
    */
   private Map<String, String> m_propertyMap = new HashMap<>();

   /**
    * Storage for the list of keyfields for this related item, this is to
    * allow for searching for a related content item, if found insert it,
    * if not insert the item in the relatedItemData variable.
    */
   private Map<String, Node> m_keyFieldsMap = new HashMap<>();

   /**
    * Storage for the relationship id
    */
   private int m_relationshipId = -1;
   
   /**
    * The relationship which leads to this related item, may be 
    * <code>null</code>. This property is not persisted and not part of the
    * XML representation of this object. It is currently only used for the
    * webservice converters.
    */
   private transient PSAaRelationship m_relationship = null;

   /**
    * Storage for the dependent id, will be -1 if inserting a new item
    */
   private int m_dependentId = -1;

   /**
    * Storage for the new inserted Items to be related
    */
   private Element m_relatedItemData = null;

   /** Name of the root element in this class' XML representation */
   public static final String EL_RELATED_ITEM = "RelatedItem";
   public static final String ATTR_RELATED_TYPE = "relatedType";
   public static final String ATTR_ACTION = "action";

   public static final String EL_PROPERTY = "Property";
   public static final String ATTR_NAME = "name";

   public static final String EL_KEY_FIELDS = "KeyFields";
   public static final String EL_SEARCH_FIELD = "SearchField";

   public static final String EL_VALUE = "Value";
   public static final String ATTR_VALUE_TYPE = "valueType";

   /** name of the elements/attributes in the relationship xml */
   public static final String EL_PSXRELATIONSHIPSET = "PSXRelationshipSet";
   public static final String EL_PSXRELATIONSHIP = "PSXRelationship";
   public static final String ATTR_CONFIG = "config";
   public static final String ATTR_ID = "id";
   public static final String EL_PSXLOCATOR = "PSXLocator";
   public static final String EL_OWNER = "Owner";
   public static final String EL_DEPENDENT = "Dependent";
   public static final String EL_PROPERTYSET = "PropertySet";
}
