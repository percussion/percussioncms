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

package com.percussion.search;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a row in search results. The XML representation of this object has
 * the DTD specified in sys_SearchResponse.dtd file. 
 */
public class PSSearchResultRow implements IPSSearchResultRow
{
   /**
    * Ctor. Give it private access so that it cannot be instantiated from
    * elsewhere
    */
   private PSSearchResultRow()
   {
   }

   /**
    * ctor. Give it package access so that it cannot be instantiated from
    * outside
    * 
    * @param sourceElem source element to construct the object, must not be
    *           <code>null</code>
    * @throws PSUnknownNodeTypeException if source element is invalid.
    */
   PSSearchResultRow(Element sourceElem) throws PSUnknownNodeTypeException
   {
      fromXml(sourceElem);
   }

   /**
    * Add a column to the row. Given package access to hide from external
    * implementers. The added column is owned by the object.
    * 
    * @param field field to be added, must not be <code>null</code>
    */
   void addColumn(PSSearchResultColumn field)
   {
      if (field == null)
         throw new IllegalArgumentException("field must not be null");
      m_fields.put(field.getName(), field);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#cloneRow()
    */
   public IPSSearchResultRow cloneRow()
   {
      PSSearchResultRow clone = new PSSearchResultRow();
      Iterator iter = m_fields.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         PSSearchResultColumn element = 
            (PSSearchResultColumn) m_fields.get(key);
         clone.m_fields.put(element.getName(), 
            (PSSearchResultColumn) element.clone());
      }
      return clone;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#
    * getColumnDisplayValue(java.lang.String)
    */
   public String getColumnDisplayValue(String colName)
   {
      if (colName == null || colName.length() == 0)
      {
         throw new IllegalArgumentException(
               "colName must not be null or empty");
      }
      PSSearchResultColumn field = (PSSearchResultColumn) m_fields.get(colName);
      if (field != null)
      {
         return field.getDisplayValue();
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#
    * setColumnDisplayValue(java.lang.String,
    *      java.lang.String)
    */
   public void setColumnDisplayValue(String colName, String colDisplayValue)
   {
      if (colName == null || colName.length() == 0)
      {
         throw new IllegalArgumentException(
               "colName must not be null or empty");
      }

      if (!hasColumn(colName))
         return;
      PSSearchResultColumn field = (PSSearchResultColumn) m_fields.get(colName);
      field.setDisplayValue(colDisplayValue);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#
    * getColumnValue(java.lang.String)
    */
   public String getColumnValue(String colName)
   {
      if (colName == null || colName.length() == 0)
      {
         throw new IllegalArgumentException(
               "colName must not be null or empty");
      }
      PSSearchResultColumn field = (PSSearchResultColumn) m_fields.get(colName);
      if (field != null)
         return field.getValue();

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#
    * setColumnValue(java.lang.String,
    *      java.lang.String)
    */
   public void setColumnValue(String colName, String colValue)
   {
      if (colName == null || colName.length() == 0)
      {
         throw new IllegalArgumentException(
               "colName must not be null or empty");
      }

      if (!hasColumn(colName))
         return;
      PSSearchResultColumn field = (PSSearchResultColumn) m_fields.get(colName);
      field.setValue(colValue);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.IPSSearchResultRow#hasColumn(java.lang.String)
    */
   public boolean hasColumn(String colName)
   {
      if (colName == null || colName.length() == 0)
      {
         throw new IllegalArgumentException(
               "colName must not be null or empty");
      }

      if (m_fields.containsKey(colName))
         return true;
      return false;
   }

   /* (non-Javadoc)
    * @see com.percussion.search.IPSSearchResultRow#getColumnNames()
    */
   public Set<String> getColumnNames()
   {
      return m_fields.keySet();
   }

   /* (non-Javadoc)
    * @see com.percussion.search.IPSSearchResultRow#getValueMap()
    */
   public Map<String, String> getColumnValueMap()
   {
      Map<String, String> nameValueMap = 
         new HashMap<String, String>(m_fields.size());
      Iterator iter = m_fields.values().iterator();
      while (iter.hasNext())
      {
         PSSearchResultColumn field = (PSSearchResultColumn) iter.next();
         nameValueMap.put(field.getName(), field.getValue());
      }
      return nameValueMap;
   }

   /**
    * Convert the object to its XML representation.
    * 
    * @param doc the parent document for the XML element for the object, must
    *           not be <code>null</code>.
    * @return XML element representing the object. See the DTD specified in teh
    *         class description, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      Element rowElem = doc.createElement(XML_NODE_NAME);
      Iterator iter = m_fields.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         PSSearchResultColumn element = (PSSearchResultColumn) m_fields
               .get(key);
         rowElem.appendChild(element.toXml(doc));
      }
      return rowElem;
   }


   /**
    * Construct the object from XML source element. Look at the DTD for the
    * element specified in the class description.
    * 
    * @param sourceNode source node as per the DTD.
    * @throws PSUnknownNodeTypeException if DTD does not match with the
    *            required.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
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

      m_fields.clear();
      PSXmlTreeWalker propsWalker = new PSXmlTreeWalker(sourceNode);
      Element fieldEl = null;
      do
      {
         if (null != fieldEl)
         {
            PSSearchResultColumn field = new PSSearchResultColumn(fieldEl);
            m_fields.put(field.getName(), field);
         }
         fieldEl = propsWalker
               .getNextElement(PSSearchResultColumn.XML_NODE_NAME);
      }
      while (null != fieldEl);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (!super.equals(obj))
         return false;

      PSSearchResultRow s2 = (PSSearchResultRow) obj;
      if (m_fields.size() != s2.m_fields.size())
         return false;

      Iterator iter = m_fields.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String) iter.next();
         PSSearchResultColumn f1 = (PSSearchResultColumn) m_fields.get(key);
         PSSearchResultColumn f2 = (PSSearchResultColumn) s2.m_fields.get(key);
         if (!f1.equals(f2))
            return false;
      }
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      Iterator iter = m_fields.values().iterator();
      int hash = super.hashCode();
      while (iter.hasNext())
      {
         PSSearchResultColumn f = (PSSearchResultColumn) iter.next();
         hash += f.hashCode();
      }
      return hash;
   }

   /**
    * Return an iterator of colums in the result row. Each entry in the iterator
    * is an object of type {@link PSSearchResultColumn}.
    * 
    * @return iterator of search result columns, never <code>null</code> may
    *    be empty.
    */
   public Iterator<PSSearchResultColumn> getColumns()
   {
      return Collections.unmodifiableMap(m_fields).values().iterator(); 
   }

   /**
    * map of all fields in the search result row. The key in the map is the
    * internal name of the field and the value is {@link PSSearchResultColumn}
    * object. Never <code>null</code> may be empty.
    */
   private Map<String, PSSearchResultColumn> m_fields = 
      new HashMap<String, PSSearchResultColumn>();

   /**
    * The name of the root element of the XML representation of the object. 
    */
   static public final String XML_NODE_NAME = "Result";
}
