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
package com.percussion.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used for searches. I can behave as both the input to a search
 * as well as the output from a search. With the help of the <code>PSItem</code>
 * and <code>PSField</code> classes, it can take the form of the following:
 * 
 * INPUT
 *    - create a new <code>PSItem</code> with query fields and optionally 
 *      result fields to return from the search
 *    
 *    - use the <code>getQuerySearchFields</code> method to return only the 
 *      fields necessary for creating the where clause of the search
 *    
 *    - use the <code>getQueryResultFields</code> method to return only the 
 *      fields that are used for the select statement of the search
 * 
 * OUTPUT from search
 *    - use the <code>getItems</code> method to return all the items from the
 *      search as a list of <code>PSItem</code>s
 *    
 *    - with each <code>PSItem</code> you can retrieve all the fields requested
 *      within the search as well as the default ones
 */

public class PSSearch
{
   /**
    * Default constructor for creating a new search for either INPUT or OUTPUT. 
    * Sets the list of items to an empty list.
    */
   public PSSearch()
   {
      m_itemList =  new ArrayList();
      m_properties = new HashMap();
   }

   /**
    * For INPUT, adding an item is used to create new search criteria or for
    * adding new result fields to be included in the search.
    * 
    * For OUTPUT, adding an item is used to store the results of a search. Each
    * content item is contained within a <code>PSItem</code> object.
    * 
    * @param item the item to be added to the search item list, 
    *    assumed not <code>null</code>
    */
   public void addItem(PSItem item)
   {
      m_itemList.add(item);      
   }
   
   /**
    * For INPUT, returns the items that make up the criteria for the search.
    * 
    * For OUTPUT, returns the result items from the search.
    * 
    * @return a list of <code>PSItem</code> objects, will not be <code>null
    *    </code>, may be an empty list
    */
   public List getItems()
   {
      return m_itemList;  
   }
   
   /**
    * For INPUT, returns the list of fields that are used for building the
    * WHERE clause for the specified search.
    * 
    * For OUTPUT, this method will return an empty list.
    * 
    * @return a list of <code>PSField</code> objects, will not be <code>null
    *    </code>, it may be an empty list
    */
   public List getQuerySearchFields()
   {
      return getFields(true);
   }
   
   /**
    * For INPUT, returns the list of fields that are used for building the
    * SELECT clause for the specified search.
    * 
    * For OUTPUT, this method will return an empty list.
    * 
    * @return a list of <code>PSField</code> objects, will not be <code>null
    *    </code>, it may be an empty list
    */
   public List getQueryResultFields()
   {
      return getFields(false);
   }
   
   /**
    * Private helper method to get the proper fields based on the flag. A field
    * is considered a search field if the "value" of the search field is not
    * <code>null</code>, it may be empty.
    * 
    * @param searchFields flag to indicate what type of fields to get, if <code>
    *    true</code>, returns the search fields, otherwise the select fields
    *    
    * @return the list of fields depending on the specified flag, will not be
    *    <code>null</code>, may be an empty list
    */
   private List getFields(boolean searchFields)
   {
      List retList = new ArrayList();
      
      Iterator itemIter = getItems().iterator();
      while (itemIter.hasNext())
      {
         PSItem item = (PSItem)itemIter.next();
         Map fieldMap = item.getFieldMap();

         Iterator iter = fieldMap.keySet().iterator();
         while (iter.hasNext())
         {
            String key = (String)iter.next();
            PSField field = (PSField)fieldMap.get(key);
            if (field != null)
            {
               if (searchFields)
               {
                  if (field.getValue() != null)
                     retList.add(field);
               }
               else
               {
                  if (field.getValue() == null)
                     retList.add(field);
               }
            }
         }
      }      
      return retList;
   }

   /**
    * Add a property to the list of properties. 
    * @param name name of the property to add, must never be <code>null</code>
    * @param value value of the property to add, must never be <code>null</code>
    */
   public void addProperty(String name, String value)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("name must never be null");
      }
      if (value == null)
      {
         throw new IllegalArgumentException("value must never be null");
      }
      m_properties.put(name, value);
   }
   
   /**
    * Accessor for properties for this search.
    * @return a {@link Map} of properties, never <code>null</code>
    */
   public Map getProperties()
   {
      return m_properties;
   }

   /**
    * Convert this search to it's HTML equivalent. Effectively returns a 
    * complete table (TABLE) of this item.
    * 
    * @return an HTML string representing this table, never <code>null</code> 
    *    or empty
    */
   public String toHTML()
   {
      return toHTML(null, 1, null, null);
   }
   
   /**
    * See {@link #toHTML(List, int, String, String) toHTML(columnList)} for
    * details
    */
   public String toHTML(List columnList)
   {
      return toHTML(columnList, 1, null, null);
   }
   
   /**
    * Convert this field to it's HTML equivalent. Effectively returns a complete 
    * table (TABLE) of this item. If the columnList parameter is not <code>null
    * </code> or 0 size, we return each field's HTML in the columnList order, 
    * otherwise we return all the fields as HTML.
    * @param columnList a list of internal field names as strings, these are
    *    to determine the order and visibility of each of the fields within
    *    this item, may be <code>null</code> or empty
    * @param sortColumn sort the items in the search results on the specified
    *    column. The column must be from 1 to the number of available columns.
    * @param oddRowCss If non-<code>null</code>, apply this style to odd
    *    rows
    * @param evenRowCss If non-<code>null</code>, apply this style to even
    *    rows
    * @return an HTML string representing this table, 
    *    never <code>null</code> or empty
    */
   public String toHTML(List columnList, int sortColumn, 
         String oddRowCss, String evenRowCss)
   {
      String ret = "<table" + m_HTMLAttributes + ">\n";
      Object itemsToSort[] = m_itemList.toArray();
      if (sortColumn < 1 || sortColumn >= columnList.size())
      {
         throw new IllegalArgumentException("The sort column must be in the " +
               "range of 1 to the number of columns");
      }
      
      if (itemsToSort.length > 0)
      {
         final String sortColumnName = 
           ((PSColumn) columnList.get(sortColumn)).getFieldName();
         
         Arrays.sort(itemsToSort, new Comparator()
         {
            public int compare(Object o1, Object o2)
            {
               PSItem item1 = (PSItem) o1;
               PSItem item2 = (PSItem) o2;
               
               // Get values from the given sorting column
               PSField f1 = (PSField) item1.getFieldMap().get(sortColumnName);
               PSField f2 = (PSField) item2.getFieldMap().get(sortColumnName);
               
               if (f1 == null || f2 == null) return 0;
               
               String val1 = f1.getValue().toLowerCase();
               String val2 = f2.getValue().toLowerCase();
               
               return val1.compareTo(val2);
            }
         });
         
         // Output header
         ret += ((PSItem) itemsToSort[0]).toHTML(columnList, true, sortColumn);
         
         // Output sorted rows
         for(int i = 0; i < itemsToSort.length; i++)
         {
            PSItem item = (PSItem) itemsToSort[i];
            // Handle odd and even formatting
            if (i%2 == 0)
            {
               if (evenRowCss != null)
               {
                  String style = "class='" + evenRowCss + "'";
                  item.addFieldHTMLAttribute(style);
               }
            }
            else
            {
               if (oddRowCss != null)
               {
                  String style = "class='" + oddRowCss + "'";
                  item.addFieldHTMLAttribute(style);
               }
            }
            ret += item.toHTML(columnList, false, sortColumn);
         }
      }
      ret += "</table>";

      return ret;
   }

   /**
    * Adds an attribute to the current set of attributes associated with this
    * search.
    * 
    * @param attribute the HTML representation of an attribute, assumed not
    *           <code>null</code> or empty, the <code>space</code> separator
    *           will be added by this routine automatically
    */
   public void addHTMLAttribute(String attribute)
   {
      m_HTMLAttributes += " " + attribute;
   }
   
   /**
    * Clears the current attributes for this search item.
    */
   public void clearHTMLAttributes()
   {
      m_HTMLAttributes = "";
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * each item within this search.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addItemHTMLAttribute(String attribute)
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.addHTMLAttribute(attribute);
      }
   }
   
   /**
    * Clears the current attributes for each item within this search.
    */
   public void clearItemHTMLAttributes()
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.clearHTMLAttributes();
      }
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * each field within each item within this search.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addFieldHTMLAttribute(String attribute)
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.addFieldHTMLAttribute(attribute);
      }
   }

   /**
    * Clears the current attributes for each field within 
    * each item within this search.
    */
   public void clearFieldHTMLAttributes()
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.clearFieldHTMLAttributes();
      }
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * the header item for this search.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addHeaderHTMLAttribute(String attribute)
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.addHeaderHTMLAttribute(attribute);
      }
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * each field's header within each item within this search.
    * 
    * @param attribute the HTML representation of an attribute, assumed 
    *    not <code>null</code> or empty, the <code>space</code>separator
    *    will be added by this routine automatically
    */
   public void addHeaderFieldHTMLAttribute(String attribute)
   {
      Iterator iter = m_itemList.iterator();
      while (iter.hasNext())
      {
         PSItem item = (PSItem)iter.next();
         item.addHeaderFieldHTMLAttribute(attribute);
      }
   }
   
   /**
    * List of items to search for, never <code>null</code> after construction.
    */
   private List m_itemList = null;
   
   /**
    * Properties to apply to this search, never <code>null</code> after
    * construction. 
    */
   private Map m_properties = null;
   
   /**
    * Storage for attributes to be used when exporting this object to HTML.
    * May be empty when there are no attributes defined yet.
    */
   private String m_HTMLAttributes = "";
}