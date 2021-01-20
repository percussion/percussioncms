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
package com.percussion.deployer.catalog;

import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* The class to represent a result row in catalog result set.
*/
public class PSCatalogResult implements IPSDeployComponent, Comparable
{
   /**
    * Constructs the catalog result object.
    * 
    * @param id the identifier of the result object, may not be <code>null
    * </code> or empty.
    * @param displayText the text to display for the result object, may not be 
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSCatalogResult(String id, String displayText)
   {
      if(id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty.");
         
      if(displayText == null || displayText.trim().length() == 0)
         throw new IllegalArgumentException(
         "displayText may not be null or empty.");
      
      m_id = id;
      m_displayText = displayText;
   }
   
   /**
    * Adds the object to the list of column objects.
    * 
    * @param obj the column data object to add, must be one of the supported
    * objects for catalog result column types. Please see {@link 
    * #PSCatalogResultColumn.validateObject(Object) validateObject} for more
    * information on the supported types.
    * 
    * @throws IllegalArgumentException if obj is not a supported type.
    */
   public void addColumn(Object obj)
   {
      if(PSCatalogResultColumn.validateObject(obj))
         m_columns.add(obj);
      else
         throw new IllegalArgumentException(
            "obj is not an instance of supported column types");
   }     
   
   /**
    * Adds a column data with a {@link #PSCatalogResultColumn.TYPE_TEXT text} 
    * type.
    * 
    * @param data the data to add, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if data is <code>null</code>
    */
   public void addTextColumn(String data)
   {
      if(data == null)
         throw new IllegalArgumentException("data may not be null.");
         
      m_columns.add(data);
   }
   
   /**
    * Adds a column data with a {@link #PSCatalogResultColumn.TYPE_NUMERIC 
    * numeric} type.
    * 
    * @param data the data to add.
    */
   public void addNumericColumn(int data)
   {   
      m_columns.add(new Integer(data));
   }
   
   /**
    * Adds a column data with a {@link #PSCatalogResultColumn.TYPE_DATE date}
    * type.
    * 
    * @param data the data to add.
    */
   public void addDateColumn(long data)
   {   
      m_columns.add(new Date(data));
   }   
   
   /**
    * Adds a column data with a {@link #PSCatalogResultColumn.TYPE_BOOL boolean}
    * type.
    * 
    * @param data the data to add.
    */
   public void addBooleanColumn(boolean data)
   {   
      m_columns.add(data);
   }    
   
   /**
    * Converts the data to the specified type and returns the object.
    * 
    * @param type the column type based on which the data to be converted to 
    * proper object, assumed to be one of <code>PSCatalogResultColumn.TYPE_XXX
    * </code> values
    * @param data the data object to convert into proper type, may not be 
    * <code>null</code>
    * 
    * @return the converted object, may be <code>null</code> if it is unable to 
    * convert.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   private Object convertObject(int type, String data)
   {      
      if(data == null)
         throw new IllegalArgumentException("data may not be null.");
      
      boolean errorTypeConv = false;
      
      Object obj = null;      
      switch(type)
      {
         case PSCatalogResultColumn.TYPE_TEXT:
            obj = data;
            break;
            
         case PSCatalogResultColumn.TYPE_NUMERIC:
            try {
               obj = new Integer(data);
            }
            catch(NumberFormatException e)
            {
               errorTypeConv = true;
            }
            break;      
                  
         case PSCatalogResultColumn.TYPE_BOOL:
            if(data.equalsIgnoreCase("true")||
               data.equalsIgnoreCase("false"))
               obj = Boolean.valueOf(data);
            else
               errorTypeConv = true;
            break;           
                
         case PSCatalogResultColumn.TYPE_DATE:
            try {
               obj = new Date(Long.parseLong(data));
            }
            catch(NumberFormatException e)
            {
               errorTypeConv = true;   
            }
            break;
      }
      
      if(errorTypeConv)
         return null;
      else
         return obj;
   }
   
   /**
    * Constructs the object from the supplied element.
    * 
    * @param sourceNode the element to construct the object from, may not be 
    * <code>null</code>
    * 
    * @throws PSUnknownNodeTypeException if the element does not have elements
    * or attributes expected by this class or its children.
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    */
   public PSCatalogResult(Element sourceNode)
      throws PSUnknownNodeTypeException   
   {   
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      fromXml(sourceNode);
   }
  
   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      //make sure we got the correct root node tag
      if (false == XML_NODE_NAME.equals (sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);         
      m_id = PSDeployComponentUtils.getRequiredElement(
         tree, XML_NODE_NAME, XML_ID_NODE, true);
      m_displayText = PSDeployComponentUtils.getRequiredElement(
         tree, XML_NODE_NAME, XML_DISP_TEXT_NODE, true);      
      
      int firstFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_RESET_CURRENT |
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;         
         
      m_columns.clear();
      Element columns = tree.getNextElement(XML_COLUMNS_NODE, firstFlags);
      if(columns != null)
      {
         Element column = tree.getNextElement(XML_COLUMN_NODE, firstFlags);
         if(column == null)
         {
            Object[] args =
            { XML_COLUMNS_NODE, XML_COLUMN_NODE, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         } 
         
         while(column != null)
         {
            String data = tree.getElementData(column);
            String typeString = PSDeployComponentUtils.getRequiredAttribute(
               column, XML_COL_TYPE_ATTR);               
            int type = PSCatalogResultColumn.getType(typeString);    
            if(PSCatalogResultColumn.validateType(type))
            {
               Object obj = convertObject(type, data);
               if(obj != null)
               {
                  m_columns.add(obj);
               }
               else
               {
                  Object[] args =
                     { XML_COLUMNS_NODE, XML_COLUMN_NODE, data };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
            else
            {
               Object[] args =
                  { XML_COLUMNS_NODE, XML_COLUMN_NODE, typeString };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            column = tree.getNextElement(XML_COLUMN_NODE, nextFlags);
         }
      }         
   }
      
   /**
    * Serializes this object's state to its XML representation.  Format is:
    * 
    * <pre><code>
    *    %lt;!ELEMENT PSXCatalogResult(ID, DisplayText, Columns?)>
    *    
    *    %lt;!-- 
    *    The identifier of the result that can be used to query the 
    *    object this result represents
    *    -->
    *    %lt;!ELEMENT ID (#PCDATA)>
    *    
    *    %lt;!-- 
    *    The display text that can be used to represent this result to present
    *    to the users in a single column.
    *    -->
    *    %lt;!ELEMENT DisplayText (#PCDATA)>
    *    
    *    %lt;!-- 
    *    Represents result data in multiple columns.
    *    -->
    *    %lt;!ELEMENT Columns (Column+)>
    *    
    *    %lt;!-- 
    *    Data for each column.
    *    -->
    *    %lt;!ELEMENT Column (#PCDATA)>
    *    
    *    %lt;!-- 
    *    Data type of the column data.
    *    -->
    *    %lt;!ATTLIST Column
    *       type #CDATA REQUIRED>
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      PSXmlDocumentBuilder.addElement(doc, root, XML_ID_NODE, m_id);
      PSXmlDocumentBuilder.addElement(doc, root, XML_DISP_TEXT_NODE, 
         m_displayText);
      if(!m_columns.isEmpty())
      {
         Element columns = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, XML_COLUMNS_NODE);
         for(int i=0; i<m_columns.size(); i++)
         {
            String data;
            if(m_columns.get(i) instanceof Date) //if Date write the time
               data = String.valueOf( ((Date)m_columns.get(i)).getTime() );
            else
               data = m_columns.get(i).toString();
               
            Element columnEl = PSXmlDocumentBuilder.addElement(
               doc, columns, XML_COLUMN_NODE, data);
            columnEl.setAttribute(XML_COL_TYPE_ATTR, 
               PSCatalogResultColumn.getTypeString(m_columns.get(i)));            
         }
      }
      
      return root;
   }
   
   //IPSDeployComponent interface implementation 
   public void copyFrom(IPSDeployComponent obj)
   {
      if(!(obj instanceof PSCatalogResult))
         throw new IllegalArgumentException(
            "obj must be an instance of PSCatalogResult");
            
      PSCatalogResult result = (PSCatalogResult)obj;      
      m_id = result.m_id;
      m_displayText = result.m_displayText;
      
      m_columns.clear();
      m_columns.addAll(result.m_columns);
   }
   
   //IPSDeployComponent interface implementation 
   public int hashCode()
   {
      return m_id.hashCode() + m_displayText.hashCode() + m_columns.hashCode();
   }
   
   //IPSDeployComponent interface implementation 
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSCatalogResult))
         return false;
      
      PSCatalogResult result = (PSCatalogResult)obj;            
      boolean equals = true;

      if(!m_id.equals(result.m_id))
         equals = false;
      else if(!m_displayText.equals(result.m_displayText))         
         equals = false;
      else if(!m_columns.equals(result.m_columns))
            equals = false;           

      return equals;      
   }
   

   /**
    * Gets the column data of this result.
    * 
    * @return the column data, never <code>null</code>, may be empty.
    */
   public Object[] getColumns()
   {
      return m_columns.toArray();
   }

   /**
    * Gets the identifier to represent this result, can be used to query the 
    * actual object this result represents.
    * 
     * @return the identifier, never <code>null</code>
    */
   public String getID()
   {
      return m_id;
   }

   /**
    * Gets the display text of this result that can be used to present to the 
    * users in a single value rather than multiple column data.
    * 
     * @return the display text, never <code>null</code> or empty.
    */
   public String getDisplayText()
   {
      return m_displayText;
   }
   
   /**
    * Gets String representation of this object. Uses the display text to 
    * represent this object.
    * 
    * @return the string, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_displayText;
   }
   
   /**
    * Compares the display text of the objects lexicographically. 
    * 
    * @param obj the catalog result object to compare, may not be <code>null
    * </code>
    * 
    * @return  the value <code>0</code> if the supplied object's display text
    * is equal to display text of this object, a value less than <code>0</code> 
    * if the supplied object's display text is lexicographically less than the
    * display text of this object and a value greater than <code>0</code> if the
    * supplied object's display text is lexicographically greater than the 
    * display text of this object.
    * 
    * @throws IllegalArgumentException if obj is <code>null</code>.
    * @throws ClassCastException if obj is not an instanceof <code>
    * PSCatalogResult</code>
    * @see java.lang.Comparable#compareTo(Object)
    */
   public int compareTo(Object obj)
   {
      if(obj == null)
         throw new IllegalArgumentException("obj may not be null.");
         
      PSCatalogResult result = (PSCatalogResult)obj;
      
      return getDisplayText().compareTo(result.getDisplayText());
   }   
   
   /**
    * The identifier which can be used to query the object this result 
    * represents, never <code>null</code> or empty after initialization.
    */
   private String m_id = null;
   
   /**
    * The display text that can be used to represent this result to present
    * to the users, never <code>null</code> or empty after initialization.
    */
   private String m_displayText = null;
   
   /**
    * The column data if this result has data to present in multiple columns.
    * User can add column data using <code>addColumn(Object)</code>. Never 
    * <code>null</code> after initialization.
    */
   private List m_columns = new ArrayList();
      
   //xml element names   
   public static final String XML_NODE_NAME = "PSXCatalogResult";  
   private static final String XML_ID_NODE = "ID";
   private static final String XML_DISP_TEXT_NODE = "DisplayText";   
   private static final String XML_COLUMNS_NODE = "Columns";   
   private static final String XML_COLUMN_NODE = "Column";      
   private static final String XML_COL_TYPE_ATTR = "type";      
}
