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

/**
 * This class is used to store the seperate pieces of return data from a <code>
 * PSSearch</code> object. The fields are actually the internal Rhythmyx name of
 * fields with the potential to hold the value returned as well.
 */
 
public class PSField
{
   /**
    * Construct a PSField object, with only a name and sometimes a value. This
    * is used for query result fields and for result fields, @see <code>PSSearch
    * </code> for more information.
    * 
    * @param name the specific field to store, this is the Rhythmyx field name 
    *    used during a search, assumed not <code>null</code> or empty
    * @param value the value of the specified field, 
    *    may be <code>null</code> or empty
    */
   public PSField(String name, String value)
   {
      this(name, value, null, null);
   }
   
   /**
    * Construct a PSField object, with a complete set of information. This is
    * used for query fields, @see <code>PSSearch</code> for more information.
    * 
    * @param name the specific field to store, this is the Rhythmyx field name 
    *    used during a search, assumed not <code>null</code> or empty
    * @param value the value of the specified field, 
    *    may be <code>null</code> or empty
    * @param operator used to compare the value within the system to the value 
    *    for the specified field, the defualt is "like", may be <code>null</code> 
    *    or empty, @see sys_SearchParameters.xsd schema for more information
    * @param connector used to join the query fields together, may be <code>null
    *    </code> or empty, @see sys_SearchParameters.xsd schema for more information
    */
   public PSField(String name, String value, String operator, String connector)
   {
      m_name = name;
      m_value = value;
      if (operator != null && operator.trim().length() != 0)
         m_operator = operator;
      if (connector != null && connector.trim().length() != 0)
         m_connector = connector;
   }
   
   /**
    * Convert this field to it's HTML equivalent. If the headerName parameter is
    * not <code>null</code> or empty, we return column header HTML, otherwise
    * we return the value as table definition (TD) HTML.
    * 
    * @param headerName the name of the column header for this field, may be 
    *    <code>null</code> or empty
    *    
    * @return an HTML string representing this cell within a table, never <code>
    *    null</code> or empty
    */
   public String toHTML(String headerName)
   {
      String data = "";
      String attributes = "";
      
      if (headerName != null)
      {
         data = headerName;
         attributes = m_headerHTMLAttributes;
      }
      else
      {
         data = m_value;
         attributes = m_HTMLAttributes;
      }

      // if no data is found, just set to a non-breaking space
      if (data == null || data.trim().length() == 0)
         data = "&nbsp;";

      return "    <td" + attributes + ">" + data + "</td>\n";
   }

   /**
    * Adds an attribute to the current set of attributes associated with 
    * this field.
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
    * Clears the current attributes for this search field.
    */
   public void clearHTMLAttributes()
   {
      m_HTMLAttributes = "";
   }
   
   /**
    * Adds an attribute to the current set of attributes associated with 
    * this field's header value.
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
    * Clears the current attributes for this search field header.
    */
   public void clearHeaderHTMLAttributes()
   {
      m_headerHTMLAttributes = "";
   }

   /**
    * Get the name of this field, this is the Rhythmyx internal field name.
    * 
    * @return the name string, never <code>null</code> or empty
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the value of this field.
    * 
    * @return the value of this field, may be <code>null</code> or empty
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Get the operator of this field. The default is "like".
    * 
    * @return the operator of this field, may be <code>null</code> or empty
    */
   public String getOperator()
   {
      return m_operator;
   }

   /**
    * Get the connector of this field. The default is "and".
    * 
    * @return the connector of this field, may be <code>null</code> or empty
    */
   public String getConnector()
   {
      return m_connector;
   }
   
   /**
    * Storage for the name of this field, set in the ctor, never <code>null
    * </code>, empty, or modified after that.
    */  
   private String m_name = null;
   
   /**
    * Storage for the value of this field, set in the ctor, may be <code>null
    * </code>, or empty, never modified after that.
    */  
   private String m_value = null;

   /**
    * Storage for the operator of this field, set in the ctor, may be <code>
    * null</code>, or empty, never modified after that. Default is "like".
    */  
   private String m_operator = "like";

   /**
    * Storage for the connector of this field, set in the ctor, may be <code>
    * null </code>, or empty, never modified after that. Default is "and".
    */  
   private String m_connector = "and";

   /**
    * Storage for attributes to be used when exporting this object to HTML.
    * May be empty when there are no attributes defined yet.
    */
   private String m_HTMLAttributes = "";

   /**
    * Storage for attributes to be used when exporting this object to HTML.
    * This set of attributes is specifically used when creating headers for 
    * the columns within the table. May be empty when there are no header 
    * attributes defined yet.
    */
   private String m_headerHTMLAttributes = "";

   /**
    * Constant defining the special field for the action page.
    */
   public static final String ACTION_PAGE_FIELD = "__ACTION_PAGE_FIELD__";
   
   /**
    * Constant defining the special field for full text search
    */
   public static final String FULL_TEXT_FIELD = "__FULL_TEXT_FIELD__";
}
