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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* The class to represent metadata of a column of the cataloged results.
*/
public class PSCatalogResultColumn implements IPSDeployComponent
{
   /**
    * Constructs this object from supplied values.
    * 
    * @param name the name of the result column, may not be <code>null</code> or
    * empty.
    * @param type the type of data this column represents, must be one of the 
    * TYPE_xxx values.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSCatalogResultColumn(String name, int type)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");
      
      if(!validateType(type))
         throw new IllegalArgumentException("type is invalid");
      
      m_name = name;
      m_type = type;         
   }
   
   /**
    * Validates that the type to be one of the TYPE_xxx values.
    * 
    * @param type the type to check
    * 
    * @return <code>true</code> if the validation is successful, otherwise
    * <code>false</code>
    */
   static boolean validateType(int type)
   {
      return ms_typeObjects.containsValue(new Integer(type));
   }
   
   /**
    * Validates the object as one of the supported types. The supported column 
    * types are {@link #TYPE_TEXT text}, {@link #TYPE_NUMERIC numeric}, {@link 
    * #TYPE_DATE date} and {@link #TYPE_BOOL boolean}. Please see the links for
    * the supported object type(class) for each column type.
    * 
    * @param obj the object to check, assumed not to be <code>null</code>
    * 
    * @return <code>true</code> if it is supported, otherwise <code>false</code>
    */
   static boolean validateObject(Object obj)
   {      
      Iterator supportedTypes = ms_typeObjects.keySet().iterator();
      while(supportedTypes.hasNext())
      {
         Class cl = (Class)supportedTypes.next();
         if(cl.isInstance(obj))
            return true;
      }
      return false;
   }
   
   /**
    * Validates that the object is a supported instance of specified column 
    * type. Please see {@link #validateObject(Object) validateObject} for the 
    * description of the supported column types and corresponding object types.
    * 
    * @param type the type of column, must be one of the TYPE_xxx values.
    * @param obj the object to validate, may not be <code>null</code>
    * 
    * @return <code>true</code> if the type supports the object, otherwise 
    * <code>false</code>
    * 
    * @throws IllegalArgumentException if obj is <code>null</code> or type is 
    * invalid.
    */
   static boolean validateObjectType(int type, Object obj)
   {  
      if(!validateType(type))
         throw new IllegalArgumentException("type is invalid");
         
      if(obj == null)
         throw new IllegalArgumentException("obj may not be null.");         
         
      Integer colType = (Integer)ms_typeObjects.get(obj.getClass());
      if( colType != null)
      {
         return colType.intValue() == type;
      }   
      else
         return false;
   }

   /**
    * Gets the type string for the object based on the object instance.
    * 
    * @param obj the object to test, may not be <code>null</code>
    * 
    * @return one of the TYPE_ENUM values, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if obj is <code>null</code> and obj is 
    * not one of the supported column types instance.
    */   
   static String getTypeString(Object obj)
   {
      if(obj == null)
         throw new IllegalArgumentException("obj may not be null.");
      Integer type = (Integer)ms_typeObjects.get(obj.getClass());
      if( type != null)
         return TYPE_ENUM[type.intValue()];
      else
         throw new IllegalArgumentException(
            "obj is not a supported column type object");
   }
   
   /**
    * Gets the type based on the type string passed in.
    * 
    * @param typeString the type string to check, may not be <code>null</code> 
    * or empty.
    * 
    * @return one of the TYPE_xxx values. May be TYPE_UNKNOWN if the type string 
    * does not match supported type strings.
    * 
    * @throws IllegalArgumentException if typeString is <code>null</code> 
    */   
   static int getType(String typeString)
   {
      if(typeString == null || typeString.trim().length() == 0)
         throw new IllegalArgumentException(
         "typeString may not be null or empty.");
         
      int type = TYPE_UNKNOWN;
      for (int i=0; i<TYPE_ENUM.length; i++)
      {
         if (TYPE_ENUM[i].equalsIgnoreCase(typeString))
         {
            type = i;
            break;
         }
      }
      
      return type;
   }  
   
      
   /**
    * Constructs the object from the supplied element.
    * 
    * @param sourceNode the element to construct the object from, may not be 
    * <code>null</code>. See {@link #toXml(Document)} for format of XML.
    * 
    * @throws PSUnknownNodeTypeException if the element does not have elements
    * or attributes expected by this class or its children.
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    */
   public PSCatalogResultColumn(Element sourceNode)
      throws PSUnknownNodeTypeException   
   {
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
      
      m_name = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_NAME_ATTR);
      String type = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_TYPE_ATTR);      
      m_type = getType(type);      
      if(m_type == TYPE_UNKNOWN)
      {
         Object[] args = { sourceNode, XML_TYPE_ATTR, type };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }
   
      
   /**
    * Serializes this object's state to its XML representation.  Format is:
    * 
    * <pre><code>
    *    %lt;!-- 
    *    name - name of the result column.
    *    type - type of the data this column supports. 
    *    -->
    *    %lt;!ELEMENT PSXCatalogResultColumn(EMPTY)>
    *    %lt;!ATTLIST Column
    *       name CDATA #REQUIRED
    *       type CDATA #REQUIRED>
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_NAME_ATTR, m_name);
      root.setAttribute(XML_TYPE_ATTR, TYPE_ENUM[m_type]);      

      return root;
   }
   
   //IPSDeployComponent interface implementation 
   public void copyFrom(IPSDeployComponent obj)
   {
      if(!(obj instanceof PSCatalogResultColumn))
         throw new IllegalArgumentException(
            "obj must be an instance of PSCatalogResultColumn");
            
      PSCatalogResultColumn resultColumn = (PSCatalogResultColumn)obj;      
      m_name = resultColumn.m_name;
      m_type = resultColumn.m_type;
   }
   
   //IPSDeployComponent interface implementation
   public int hashCode()
   {
      return m_name.hashCode() + m_type;
   }
   
   //IPSDeployComponent interface implementation
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSCatalogResultColumn))
         return false;
      
      PSCatalogResultColumn resultColumn = (PSCatalogResultColumn)obj;         
      boolean equals = true;

      if(!m_name.equals(resultColumn.m_name))
         equals = false;
      else if(m_type != resultColumn.m_type)         
         equals = false;

      return equals;      
   }

   /**
    * Gets the name of this column.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Gets the type of this column data.
    * 
    * @return the type, one of the TYPE_xxx values.
    */
   public int getType()
   {
      return m_type;
   }
   
   /**
    * Gets the class object that can represent this column data. 
    * 
    * @return the class, never <code>null</code>.
    */
   public Class getColumnClass()
   {
      Iterator iter = ms_typeObjects.entrySet().iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         if( ((Integer)entry.getValue()).intValue() == m_type)
         {
            return (Class)entry.getKey();
         }
      }
      //this will never happen, just for compilation
      return null;
   }
   
   
   /**
    * The name of the column, initialized in constructor and never modified 
    * after that.
    */
   private String m_name;
   
   /**
    * The type of the column data in the result, set in constructor and never 
    * modified after that, will be one of the TYPE_xxx values.
    */
   private int m_type = TYPE_UNKNOWN;   
   
   /**
    * Constant to represent unknown type.
    */
   public static final int TYPE_UNKNOWN = -1;

   /**
    * Constant to indicate that column will contain a <code>String</code> 
    * object.
    */ 
    public static final int TYPE_TEXT = 0;
   
   /**
    * Constant to indicate that column will contain a <code>Integer</code> 
    * object.
    */   
   public static final int TYPE_NUMERIC = 1;
   
   /**
    * Constant to indicate that column will contain a <code>java.util.Date
    * </code> object.
    */
   public static final int TYPE_DATE = 2;
   
   /**
    * Constant to indicate that column will contain a <code>Boolean</code> 
    * object.
    */
   public static final int TYPE_BOOL = 3;
   
   /**
    * An array of XML attribute values for the column type. They are specified 
    * at the index of the specifier. 
    */
   public static final String[] TYPE_ENUM =
   {
      "text", "numeric", "date", "boolean"
   };
   
   /**
    * Map of supported objects for each type, with supported object class as key
    * and the <code>Integer</code> object wrapped with column type as value. 
    * Never <code>null</code> or modified. See {@link #TYPE_TEXT text}, {@link 
    * #TYPE_NUMERIC numeric}, {@link #TYPE_DATE date} and {@link #TYPE_BOOL 
    * boolean} for the corresponding supported object types(classes).
    */
   private static Map<Class, Integer> ms_typeObjects = new HashMap<Class, Integer>();
   static
   {      
      ms_typeObjects.put(String.class,  PSCatalogResultColumn.TYPE_TEXT);
      ms_typeObjects.put(Integer.class, PSCatalogResultColumn.TYPE_NUMERIC);
      ms_typeObjects.put(Date.class,    PSCatalogResultColumn.TYPE_DATE);
      ms_typeObjects.put(Boolean.class, PSCatalogResultColumn.TYPE_BOOL);                           
   }
   
   //XML element names and attributes  
   public static final String XML_NODE_NAME = "PSXCatalogResultColumn";         
   private static final String XML_NAME_ATTR = "name";
   private static final String XML_TYPE_ATTR = "type";   
}
