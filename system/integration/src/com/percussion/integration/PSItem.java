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
package com.percussion.integration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to store a content item from Rhythmyx retrieved by the
 * search action into a <code>PSSearch</code> object. This class is used for
 * both directions on the search, it is used as the query field definition and
 * the result item for each content item returned. @see <code>PSSearch</code>
 * for more information.
 */

public class PSItem
{
   /**
    * Default constructor to create an empty item. Content id is -1, and the
    * title's initial value will be null.
    */
   public PSItem()
   {
      m_fieldMap = new HashMap();
   }

   /**
    * Adds the specified field name to the list of fields to be retrieved. This
    * is used for search when asking for the fields to be returned. This field
    * is considered an INPUT field for search.
    * 
    * @param fieldName the internal Rhythmyx field name, assumed not <code>null
    *    </code> or empty
    */
   public void addSelectField(String fieldName)
   {
      addField(new PSField(fieldName, null));
   }
   
   /**
    * Adds the specified field name and it's value representing a query where
    * clause. This is used for search when creating the query to search for 
    * specific content items. This field is considered an INPUT field for search.
    * 
    * @param fieldName the internal Rhythmyx field name, assumed not <code>null
    *    </code> or empty
    * @param value the value for the specified field to search on, assumed not 
    *    <code>null</code> or empty
    * @param operator the query operator for this where clause, default = "like"
    *    may be <code>null</code> or empty
    * @param connector the query join value, default = "and", may be <code>null
    *    </code> or empty
    */
   public void addQueryField(
      String fieldName, String value, String operator, String connector)
   {
      addField(new PSField(fieldName, value, operator, connector));
   }

   /**
    * Adds the specified field name and it's value representing a result item 
    * from a search. This is used to store content item results from a search.
    * This field is considered an OUTPUT field for search.
    * 
    * @param fieldName the internal Rhythmyx field name, assumed not <code>null
    *    </code> or empty
    * @param value the value of the specified field that was searched, assumed 
    *    not <code>null</code>, may be empty
    */
   public void addResultField(String fieldName, String value)
   {
      addField(new PSField(fieldName, value));
   }

   /**
    * Adds the Action Menu field which is a special field that is identified by
    * the constant <code>ACTION_PAGE_FIELD</code>, it's value is the data 
    * returned from the <code> getActionPageLink</code> method call.
    * This field is considered an OUTPUT field for search.
    * 
    * @param value the action menu html of the specified item that was searched, 
    *    assumed not <code>null</code>, may be empty
    */
   public void addActionPageField(String value)
   {
      addField(new PSField(PSField.ACTION_PAGE_FIELD, value));
   }
   
   /**
    * Private helper for adding fields to this item. This also has the side
    * effect of setting the content id and title if found, usually used for
    * the results of a search.
    * 
    * @see addQueryField
    * @see addResultField
    * @see addSelectField
    * 
    * @param field the <code>PSField</code> to add to this item
    */
   private void addField(PSField field)
   {
      if (m_contentId == -1 && field.getName().equalsIgnoreCase("sys_contentid"))
      {
         try 
         {
            m_contentId = Integer.parseInt(field.getValue());
         }
         catch (NumberFormatException ex) 
         {
            // ignore
         }
      }
      if (m_title == null && field.getName().equalsIgnoreCase("sys_title"))
         m_title = field.getValue();

      m_fieldMap.put(field.getName(), field);
   }

   /**
    * Convert this field to it's HTML equivalent. Effectively returns a table
    * row (TR) of this item.
    * 
    * @return an HTML string representing this row within a table, never <code>
    *    null</code> or empty
    */
   public String toHTML()
   {
      return toHTML(null, false, 1);
   }
 
   /**
    * See {@link #toHTML(List, boolean, int) toHTML(columnList, header)}
    * for details
    */
   public String toHTML(List columnList, boolean header)
   {
      return toHTML(columnList, header, 1);
   }
   
   /**
    * Convert this field to it's HTML equivalent. Effectively returns a table
    * row (TR) of this item. If the columnList parameter is not <code>null</code> 
    * or 0 size, we return each field's HTML in the columnList order, otherwise
    * we return all the fields as HTML.
    * @param columnList a list of internal field names as strings, these are
    *    to determine the order and visibility of each of the fields within
    *    this item, may be <code>null</code> or empty
    * @param header if true, we must output the column header for each field
    *    within this item, otherwise we return the HTML for for each field with
    *    the value
    * @param sortColumn column that is being sorted on, this is used to mark
    * that column's header
    * 
    * @return an HTML string representing this row within a table, never <code>
    *    null</code> or empty
    */
   public String toHTML(List columnList, boolean header, int sortColumn)
   {
      String ret = "  <tr";
      ret += header ? m_headerHTMLAttributes : m_HTMLAttributes;
      ret += ">\n";
      
      if (columnList != null && columnList.size() > 0)
      {
         Iterator iter = columnList.iterator();
         int fieldIndex = 0;
         while (iter.hasNext())
         {
            PSColumn col = (PSColumn)iter.next();
            PSField field = (PSField)m_fieldMap.get(col.getFieldName());
            if (field != null)
            {
               String headerElement = formatHeader(fieldIndex, 
                     col.getLabel(), sortColumn);
               ret += field.toHTML(header ? headerElement : null);
            }
            fieldIndex++;
         }  
      }
      else
      {
         Iterator iter = m_fieldMap.keySet().iterator();
         int fieldIndex = 0;
         while (iter.hasNext())
         {
            String key = (String)iter.next();
            PSField field = (PSField)m_fieldMap.get(key);
            if (field != null)
            {
               String headerElement = formatHeader(fieldIndex, 
                     field.getName(), sortColumn);
               ret += field.toHTML(header ? headerElement : null);
            }
            fieldIndex++;
         }
      }
      ret += "  </tr>\n";
      
      return ret;
   }
   
   /**
    * Format the header string
    * @param fieldIndex the index of this header, used for column
    * sorting.
    * @param label the label for this header
    * @param sortColumn the column being sorted, used to mark the
    * appropriate column
    * @return a formatted header string, never <code>null</code> or
    * empty.
    */
   private String formatHeader(int fieldIndex, String label, int sortColumn)
   {
      StringBuilder headerElement = new StringBuilder(40);
      headerElement.append("<a onclick='sortOn(this, ");
      headerElement.append(fieldIndex);
      headerElement.append(")' style='cursor: hand'>");
      if (sortColumn == fieldIndex)
      {
         headerElement.append("<u>");
      }
      headerElement.append(label);
      if (sortColumn == fieldIndex)
      {
         headerElement.append("</u>");
      }
      headerElement.append("</A>");
      return headerElement.toString();
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * this item.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addHTMLAttribute(String attribute)
   {
      m_HTMLAttributes += " " + attribute;
   }
   
   /**
    * Clears the current attributes for this item.
    */
   public void clearHTMLAttributes()
   {
      m_HTMLAttributes = "";
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * the header within this item.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addHeaderHTMLAttribute(String attribute)
   {
      m_headerHTMLAttributes += " " + attribute;
   }

   /**
    * Clears the current attributes for this item.
    */
   public void clearHeaderHTMLAttributes()
   {
      m_headerHTMLAttributes = "";
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * each field's header within this item.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addHeaderFieldHTMLAttribute(String attribute)
   {
      Iterator iter = m_fieldMap.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String)iter.next();
         PSField field = (PSField)m_fieldMap.get(key);
         field.addHeaderHTMLAttribute(attribute);
      }
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * each field within this item.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addFieldHTMLAttribute(String attribute)
   {
      Iterator iter = m_fieldMap.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String)iter.next();
         PSField field = (PSField)m_fieldMap.get(key);
         field.addHTMLAttribute(attribute);
      }
   }

   /**
    * Clears the current attributes for each field within 
    * each item within this search.
    */
   public void clearFieldHTMLAttributes()
   {
      Iterator iter = m_fieldMap.keySet().iterator();
      while (iter.hasNext())
      {
         String key = (String)iter.next();
         PSField field = (PSField)m_fieldMap.get(key);
         field.clearHTMLAttributes();
      }
   }

   /**
    * Get the content id of this item, this is the Rhythmyx content id.
    * 
    * @return the content id as an int, -1 indicates it has not been set
    */
   public int getContentId()
   {
      return m_contentId;  
   }
   
   /**
    * Get the title of this item, this is the Rhythmyx title of the content item.
    * 
    * @return the title string, may be <code>null</code> or empty
    */
   public String getTitle()
   {
      return m_title;
   }
   
   /**
    * Get the field map for this item, this contains string keys for the 
    * internal Rhythmyx field names.
    * 
    * @return the map of fields for this item, will not be <code>null</code>,
    *    may be an empty map
    */
   public Map getFieldMap()
   {
      return m_fieldMap;  
   }
   
   /**
    * Storage for the content id for this item. Set in <code>addField</code>
    * method when the specified field to be added is "sys_contentid", otherwise
    * it will be -1.
    */  
   private int m_contentId = -1;

   /**
    * Storage for the title of this item. Set in <code>addField</code> method
    * when the specified field to be added is "sys_title", otherwise it will
    * be <code>null</code>.
    */
   private String m_title = null;

   /**
    * Storage for the fields of this item. Fields are added in the <code>
    * addField</code> method. Initialized in the ctor, never <code>null</code>
    * after that.
    */
   private Map m_fieldMap = null;

   /**
    * Storage for attributes to be used when exporting this object to HTML.
    * May be empty when there are no attributes defined yet.
    */
   private String m_HTMLAttributes = "";

   /**
    * Storage for attributes to be used when exporting this object to HTML, used
    * for defining the attributes for the header.
    * May be empty when there are no attributes defined yet.
    */
   private String m_headerHTMLAttributes = "";
}
