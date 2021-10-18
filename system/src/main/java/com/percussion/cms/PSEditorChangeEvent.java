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
 
package com.percussion.cms;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Encapsulates the information about an action performed by a content editor
 * on a content item.
 */
public class PSEditorChangeEvent 
{
   /**
    * Convenience ctor for parent item change that calls 
    * {@link #PSEditorChangeEvent(int, int, int, int, int, int, int) 
    * this(actionType, contentId, revisionId, -1, -1, contentTypeId}.
    */
   public PSEditorChangeEvent(int actionType, int contentId, int revisionId, 
      long contentTypeId)
   {
      this(actionType, contentId, revisionId, -1, -1, contentTypeId);
   }
   
   /**
    * Creates an event object using the supplied information.
    * 
    * @param actionType The type of action performed, one of the ACTION_xxx
    * constants.
    * @param contentId The content id of the modified item.
    * @param revisionId The revision id of the modified item.
    * @param childId The id identifying which complex child was modified, 
    * <code>-1</code> if a parent item was modified.
    * @param childRowId The id identifying which child row was modified, 
    * <code>-1</code> if a parent item was modified.
    * @param contentTypeId The contenttype of the modified item.    
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSEditorChangeEvent(int actionType, int contentId, int revisionId, 
      int childId, int childRowId, long contentTypeId)
   {
      if (!isValidActionType(actionType))
         throw new IllegalArgumentException("invalid action type");   
      
      m_action = actionType;
      m_contentId = contentId;
      m_revisionId = revisionId;
      m_childId = childId;
      m_childRowId = childRowId;
      m_contentTypeId = contentTypeId;      
   }
   
   /**
    * Constructs this object from its XML representation.
    * 
    * @param source The source element, may not be <code>null</code>.  See
    * {@link #toXml(Document)} for format expected.
    * 
    * @throws PSUnknownNodeTypeException if <code>source</code> is invalid.
    */
   public PSEditorChangeEvent(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Validates the supplied action type.
    * 
    * @param actionType The action type to validate.
    * 
    * @return <code>true</code> if it is one of the <code>ACTION_XXX</code>
    * type constant values, <code>false</code> if not.
    */
   public boolean isValidActionType(int actionType)
   {
      return (actionType >= 0 && actionType < ACTION_ENUM.length);
   }
   
   /**
    * Gets the type of change that occurred by name
    * 
    * @return The type, one of the ACTION_xxx constants by name.
    */
   public String getActionTypeName()
   {
      return isValidActionType(m_action) ? ACTION_ENUM[m_action] : "Invalid";
   }
   
   /**
    * Gets the type of change that occurred.
    * 
    * @return The type, one of the ACTION_xxx constants.
    */
   public int getActionType()
   {
      return m_action;
   }
   
   /**
    * Gets the content id of the changed item.
    * 
    * @return The content id supplied in the ctor.
    */
   public int getContentId()
   {
      return m_contentId;
   }
   
   /**
    * Gets the revision id of the changed item.
    * 
    * @return The revision id supplied in the ctor.
    */
   public int getRevisionId()
   {
      return m_revisionId;
   }
   
   /**
    * Gets the content type id of the changed item.
    * 
    * @return The content type id supplied in the ctor.
    */
   public long getContentTypeId()
   {
      return m_contentTypeId;
   }
   
   /**
    * Get the id of the changed complex child.
    * 
    * @return The child id supplied in the ctor, <code>-1</code> if a parent 
    * item was modified.
    */
   public int getChildId()
   {
      return m_childId;
   }

   /**
    * Get the id of the changed complex child row.
    * 
    * @return The child row id supplied in the ctor, <code>-1</code> if a parent 
    * item was modified.
    */
   public int getChildRowId()
   {
      return m_childRowId;
   }
   
   /**
    * Get the collection of binary fields modified by this event, never  
    * <code>null</code>.
    * 
    * @return The field names as <code>String</code> objects, never 
    * <code>null</code>, may be empty.  This is an iterator over a read-only 
    * version of the collection set by calls to 
    * {@link #setBinaryFields(Collection)}.
    */
   public Iterator<String> getBinaryFields()
   {      
      return Collections.unmodifiableCollection(m_binaryFields).iterator();      
   }
   
   /**
    * Set the collection of modifed binary fields.  See 
    * {@link #getBinaryFields()} for more info.  
    * 
    * @param fields The collection of field names as <code>String</code> 
    * objects, may be <code>null</code> or empty to clear the list.  This method
    * does not take ownership of the supplied collection; modifications to the
    * collection after calling this method do not have any effect on this 
    * object.
    */
   public void setBinaryFields(Collection<String> fields)
   {      
      m_binaryFields.clear();
      if (fields != null)
         m_binaryFields.addAll(fields);
   }

   /**
    * Get the priority of this event.  Defaults to {@link #DEFAULT_PRIORTY} if 
    * it has not been explicitly set.  The lower the number, the higher the 
    * priority. Events with a higher priority (lower numbers) may be processed 
    * sooner if the subscriber of the event is queuing events and is concerned 
    * with priority.
    * 
    * @return The priority level of this event.
    */
   public int getPriority()
   {
      return m_priority;
   }
   
   /**
    * Sets the priority of this event.  See {@link #getPriority()} for more 
    * info.
    * 
    * @param priority The priority level of this event.  All possible values are 
    * valid.
    */
   public void setPriority(int priority)
   {
      m_priority = priority;
   }

   /**
    * Serializes this object to its XML represetation.  The DTD is:
    * <pre><code>
    * <!ELEMENT PSXEditorChangeEvent (BinaryFields?)>
    * <!ATTLIST PSXEditorChangeEvent
    *    action CDATA #REQUIRED
    *    contentId CDATA #REQUIRED
    *    revisionId CDATA #REQUIRED
    *    contentTypeId CDATA #REQUIRED    
    *    childId CDATA #REQUIRED
    *    childRowId CDATA #REQUIRED
    *    priority CDATA #IMPLIED
    * >
    * <!ELEMENT BinaryFields (BinaryField+)>
    * <!ELEMENT BinaryField (#PCDATA)> 
    * </code></pre>
    * 
    * @param doc The document to use when creating elements, may not be 
    * <code>null</code>.  
    * 
    * @return The root element of the serialized XML, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      
      Element root = doc.createElement(XML_NODE_NAME);      
      root.setAttribute(ACTION_ATTR, ACTION_ENUM[m_action]);
      root.setAttribute(CONTENTID_ATTR, String.valueOf(m_contentId));
      root.setAttribute(REVISIONID_ATTR, String.valueOf(m_revisionId));
      root.setAttribute(CONTENTTYPEID_ATTR, String.valueOf(m_contentTypeId));      
      root.setAttribute(CHILDID_ATTR, String.valueOf(m_childId));
      root.setAttribute(CHILDROW_ATTR, String.valueOf(m_childRowId));
      root.setAttribute(PRIORITY_ATTR, String.valueOf(m_priority));
      
      if (!m_binaryFields.isEmpty())
      {
         Element binFieldsEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            BIN_FIELDS_ELEMENT);
         for (String binField : m_binaryFields) {
            PSXmlDocumentBuilder.addElement(doc, binFieldsEl, BIN_FIELD_ELEMENT,
                    binField);
         }
      }
      
      return root;
   }
   
   /**
    * Restore this object from its XML representation. See 
    * {@link #toXml(Document)} for more info.
    * 
    * @param source The source elment, may not be <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException If the supplied element does not 
    * conform to the expected format.
    */
   public void fromXml(Element source) throws PSUnknownNodeTypeException   
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      PSXMLDomUtil.checkNode(source, XML_NODE_NAME);
      
      m_action = PSXMLDomUtil.checkAttributeEnumerated(source, 
         ACTION_ATTR, ACTION_ENUM, true);
      m_contentId = PSXMLDomUtil.checkAttributeInt(source, CONTENTID_ATTR, 
         true);
      m_revisionId = PSXMLDomUtil.checkAttributeInt(source, REVISIONID_ATTR, 
         true);
      m_contentTypeId = PSXMLDomUtil.checkAttributeInt(source, 
         CONTENTTYPEID_ATTR, true);
      m_childId = PSXMLDomUtil.checkAttributeInt(source, CHILDID_ATTR, 
         true);
      m_childRowId = PSXMLDomUtil.checkAttributeInt(source, CHILDROW_ATTR, 
         true);
      int priority = PSXMLDomUtil.checkAttributeInt(source, PRIORITY_ATTR, 
         false);
      m_priority = (priority == -1) ? DEFAULT_PRIORTY : priority;
            
      m_binaryFields.clear();
      Element binFieldsEl = PSXMLDomUtil.getFirstElementChild(source);
      if (binFieldsEl != null)
      {
         PSXMLDomUtil.checkNode(binFieldsEl, BIN_FIELDS_ELEMENT);         
         Element binFieldEl = PSXMLDomUtil.getFirstElementChild(binFieldsEl, 
            BIN_FIELD_ELEMENT);
         while (binFieldEl != null)
         {
            String binField = PSXMLDomUtil.getElementData(binFieldEl);
            if (binField != null && binField.trim().length() > 0)
               m_binaryFields.add(binField);
            binFieldEl = PSXMLDomUtil.getNextElementSibling(binFieldEl, 
               BIN_FIELD_ELEMENT);   
         }
      }
   }

   // see base class
   public boolean equals(Object o)
   {
      boolean isEqual = true;
      
      if (!(o instanceof PSEditorChangeEvent))
         isEqual = false;
      else
      {
         PSEditorChangeEvent other = (PSEditorChangeEvent)o;
         if (m_action != other.m_action)
            isEqual = false;
         else if (m_contentId != other.m_contentId)
            isEqual = false;
         else if (m_revisionId != other.m_revisionId)
            isEqual = false;
         else if (m_contentTypeId != other.m_contentTypeId)
            isEqual = false;
         else if (m_childId != other.m_childId)
            isEqual = false;
         else if (m_childRowId != other.m_childRowId)
            isEqual = false;
         else if (m_priority != other.m_priority)
            isEqual = false;
         else if (!m_binaryFields.equals(other.m_binaryFields))
            isEqual = false;         
      }
      
      return isEqual;
   }
   
   // see base class
   public int hashCode()
   {      
      return (String.valueOf(m_action) + String.valueOf(m_contentId) + 
         String.valueOf(m_revisionId) + String.valueOf(m_childId) +         
         String.valueOf(m_childRowId) + String.valueOf(m_priority)+ 
         String.valueOf(m_contentTypeId)).hashCode() + 
         m_binaryFields.hashCode();
   }
   
   /**
    * Name of root node of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXEditorChangeEvent";
  
   /**
    * Constant to indicate the item was inserted.
    */
   public static final int ACTION_INSERT = 0;
   
   /**
    * Constant to indicate the item was modified.
    */
   public static final int ACTION_UPDATE = 1;
   
   /**
    * Constant to indicate the item was deleted.
    */
   public static final int ACTION_DELETE = 2;
   
   /**
    * Constant to indicate the item was checked in.
    */
   public static final int ACTION_CHECKIN = 3;
   
   /**
    * Constant to indicate the item was checked out.
    */
   public static final int ACTION_CHECKOUT = 4;
   
   /**
    * Constant to indicate the item was transitioned.
    */
   public static final int ACTION_TRANSITION = 5;
   
   /**
    * Constant to indicate the item was submitted for reindexing by the search
    * engine.
    */
   public static final int ACTION_REINDEX = 6;
   
   /**
    * Constant to indicate undefined action.
    */
   public static final int ACTION_UNDEFINED = -1;
   
   /**
    * Enumeration of the string representation of each <code>ACTION_xxx</code>
    * constants, where the index into the array is the value of the constant.
    * When a new constant is added, its string representation must be appended
    * to this enumeration. 
    */
   public static final String[] ACTION_ENUM = 
   {
      "insert", 
      "update", 
      "delete", 
      "checkin", 
      "checkout", 
      "transition",
      "reindex"
   };

   /**
    * Constant for default priority of 20.
    */
   public static final int DEFAULT_PRIORTY = 20;
   
   /**
    * The content id of the modified item.  Set during ctor, never modified
    * after that.
    */
   private int m_contentId;
   
   /**
    * The revision id of the modified item.  Set during ctor, never modified
    * after that.
    */
   private int m_revisionId;
   
   /**
    * The id representing the complex child modified.  <code>-1</code> if a 
    * parent item was modified.  Set during the ctor, never modified after that.
    */
   private int m_childId;
   
   /**
    * The id of the child row that was modified.  <code>-1</code> if a 
    * parent item was modified.  Set during the ctor, never modified after that.
    */
   private int m_childRowId;

   /**
    * The action that was taken when the content item was modified.  One of the
    * <code>ACTION_xxx</code> values, set during ctor, never <code>null</code>
    * or modified after that.
    */
   private int m_action;
   
   /**
    * Collection of binary field names that were modified by this event, as 
    * <code>String</code> objects.  Never <code>null</code>, modified by
    * calls to {@link #setBinaryFields(Collection)}.
    */
   private Collection<String> m_binaryFields = new HashSet<>();
   
   /**
    * Content type id of the item, set during the ctor, never modified after 
    * that.
    */
   private long m_contentTypeId;
   
   
   /**
    * The priority of this event, defaults to {@link #DEFAULT_PRIORTY} if not 
    * explicitly modified by {@link #setPriority(int)}.
    */
   private int m_priority = DEFAULT_PRIORTY;
   
   // xml constants
   private static final String BIN_FIELDS_ELEMENT = "BinaryFields";
   private static final String BIN_FIELD_ELEMENT = "BinaryField";
   private static final String ACTION_ATTR = "action";
   private static final String CONTENTID_ATTR = "contentId";
   private static final String REVISIONID_ATTR = "revisionId";
   private static final String CONTENTTYPEID_ATTR = "contentTypeId";
   private static final String CHILDID_ATTR = "childId";
   private static final String CHILDROW_ATTR = "childRowId";
   private static final String PRIORITY_ATTR = "priority";

}
