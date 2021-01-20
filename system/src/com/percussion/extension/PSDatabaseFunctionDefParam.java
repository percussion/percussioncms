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
package com.percussion.extension;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.sql.Types;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a database function definition parameter.
 * A database function parameter is uniquely identified by its name
 * (case-insensitive).
 */
public class PSDatabaseFunctionDefParam implements Cloneable
{

   /**
    * Construct the param from the specified name, type and description.
    *
    * @param name name of the parameter, may not be <code>null</code> or
    * empty
    * @param type the type of param, should be one of the <code>TYPE_XXX</code>
    * values
    * @param desc the parameter description, may be <code>null</code> or empty,
    * if <code>null</code> then set to empty
    *
    * @throws IllegalArgumentException if <code>name</code> is
    * <code>null</code> or empty or if <code>type</code> is invalid
    */
   PSDatabaseFunctionDefParam(String name, int type, String desc)
   {
      if ((name == null) || (name.trim().length() < 0))
         throw new IllegalArgumentException("name may not be null");

      if (!((type == TYPE_TEXT) || (type == TYPE_NUMBER)))
         throw new IllegalArgumentException("Invalid parameter type");

      m_name = name;
      m_type = type;
      m_desc = (desc == null ? "" : desc);
   }

   /**
    * Constructs this object from its XML representation.
    *
    * @param sourceNode the XML element from which to load this object, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException If the specified element content does
    * not conform to the DTD specified in {@link #toXml(Document) toXml()}
    */
   public PSDatabaseFunctionDefParam(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode);
   }

   /**
    * Loads the function parameter from the supplied element.
    * See {@link #toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException If the specified element does not
    * conform to the DTD specified in {@link #toXml(Document) toXml()}
    */
   void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      // name - required
      String sTemp = sourceNode.getAttribute(ATTR_NAME);
      if ((sTemp == null) || (sTemp.trim().length() == 0))
      {
         Object[] args =
            {NODE_NAME, ATTR_NAME, sTemp == null ? "null" : sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_name = sTemp;

      // type - optional (defaults to text)
      m_type = TYPE_TEXT;
      sTemp = sourceNode.getAttribute(ATTR_TYPE);
      if (sTemp.equalsIgnoreCase(ATTR_TYPE_VALUE_NUMBER))
         m_type = TYPE_NUMBER;
      else if (sTemp.equalsIgnoreCase(ATTR_TYPE_VALUE_ARRAY))
         m_type = TYPE_ARRAY;

      // staticBind - optional (defaults to true)
      m_isStaticBind = true;
      sTemp = sourceNode.getAttribute(ATTR_STATIC_BIND);
      if (sTemp.equalsIgnoreCase(XML_FALSE))
         m_isStaticBind = false;

      // description
      m_desc = "";
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element el = walker.getNextElement(EL_DESCRIPTION, firstFlags);
      if (el != null)
      {
         sTemp = PSXmlTreeWalker.getElementData(el);
         m_desc = (sTemp == null ? "" : sTemp.trim());
      }
   }

   /**
    * Serializes this object's state to Xml conforming to the DTD of the
    * "Param" element as defined in the "sys_DatabaseFunctionDefs.dtd" file.
    *
    * @param doc The document to use when creating elements, may not be <code>
    *  null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element   root = doc.createElement(NODE_NAME);
      root.setAttribute(ATTR_NAME, m_name);
      root.setAttribute(ATTR_TYPE, PARAM_TYPES[m_type]);
      root.setAttribute(ATTR_STATIC_BIND,
         (m_isStaticBind ? XML_TRUE : XML_FALSE));
      PSXmlDocumentBuilder.addElement(doc, root, EL_DESCRIPTION, m_desc);
      return root;
   }

   /**
    * Sets the internal members of this function parameter equal to
    * the specified function parameter.
    *
    * @param funcParam the function parameter whose values should be
    * used to set the value of the internal members of this object, may not
    * be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>funcParam</code> is
    * <code>null</code>
    */
   public void copyFrom(PSDatabaseFunctionDefParam funcParam)
   {
      if (funcParam == null)
         throw new IllegalArgumentException("funcParam may not be null");

      m_name = funcParam.getName();
      m_type = funcParam.getType();
      m_desc = funcParam.getDescription();
      m_isStaticBind = funcParam.m_isStaticBind;
   }

   /**
    * Compares the name (case-insensitive) and staticBind property value of
    * this object with the specified object. This method excludes the type
    * and description from the comparison.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if the name and staticBind property value of this
    * object matches that of the specified object, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    *
    * @see equalsFull(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException(
            "Cannot compare with a null object");

      boolean equals = true;
      if (!(obj instanceof PSDatabaseFunctionDefParam))
      {
         equals = false;
      }
      else
      {
         PSDatabaseFunctionDefParam other = (PSDatabaseFunctionDefParam)obj;
         if (!(m_name.equalsIgnoreCase(other.m_name)))
            equals = false;
         else if (m_isStaticBind != other.m_isStaticBind)
            equals = false;
      }
      return equals;
   }

   /**
    * Compares the name (case-sensitive), staticBind property value, type and
    * description (case-sensitive) of this object with the specified object.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <false> if the specified object is not an instance of this class.
    * <code>true</code> if the name, type, description and staticBind property
    * value of this object matches that of the specified object,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    *
    * @see equals(Object)
    */
   public boolean equalsFull(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException(
            "Cannot compare with a null object");

      boolean equals = true;
      if (!(obj instanceof PSDatabaseFunctionDefParam))
      {
         equals = false;
      }
      else
      {
         PSDatabaseFunctionDefParam other = (PSDatabaseFunctionDefParam)obj;
         if (!(m_name.equals(other.m_name)))
            equals = false;
         else if (m_type != other.m_type)
            equals = false;
         else if (!(m_desc.equals(other.m_desc)))
            equals = false;
         else if (m_isStaticBind != other.m_isStaticBind)
            equals = false;
      }
      return equals;
   }

   /**
    * Computes the hash code for this object using the name
    * (converted to lowercase) and the staticBind property value.
    *
    * @return the hash code for this object
    *
    * @see hashCodeFull()
    */
   public int hashCode()
   {
      int hash = m_name.toLowerCase().hashCode();
      hash += (m_isStaticBind ? 1 : 0);
      return hash;
   }

   /**
    * Computes the hash code for this object using the name, type,
    * description and the staticBind property value.
    *
    * @return the hash code for this object
    *
    * @see hashCode()
    */
   public int hashCodeFull()
   {
      int hash = m_name.hashCode() + m_type + m_desc.hashCode();
      hash += (m_isStaticBind ? 1 : 0);
      return hash;
   }

   /**
    * Returns the tag name of the root element from which this object can be
    * constructed.
    *
    * @return the name of the root node of the XML document returned by a call
    * to {@link#toXml(Document) toXml()} method.
    *
    * @see toXml(Document)
    */
   public static String getNodeName()
   {
      return NODE_NAME;
   }

   /**
    * Returns the name of this function parameter. The function parameter name
    * is case-insensitive.
    *
    * @return the name of this function parameter, never <code>null</code> or
    * empty
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns the type of function parameter.
    *
    * @return one of the <code>TYPE_XXX</code> values
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Returns the JDBC data type of function parameter.
    * For type "text", <code>java.sql.Types.VARCHAR</code> is returned.
    * For type "number", <code>java.sql.Types.NUMERIC</code> is returned.
    * For type "array", <code>java.sql.Types.ARRAY</code> is returned.
    *
    * @return one of the <code>java.sql.Types.XXX</code> values
    */
   public int getJDBCType()
   {
      return PARAM_JDBC_DATA_TYPES[m_type];
   }

   /**
    * Returns the description of this function parameter.
    *
    * @return the description of this function parameter,
    * never <code>null</code>, may be empty
    */
   public String getDescription()
   {
      return m_desc;
   }

   /**
    * Determine if the value of this function parameter should be a string.
    *
    * @return <code>true</code> if this function parameter expects a string
    * as its value, <code>false</code> otherwise
    */
   public boolean isText()
   {
      return (m_type == TYPE_TEXT);
   }

   /**
    * Determine if the value of this function parameter should be a number.
    *
    * @return <code>true</code> if this parameter expects a number as its value,
    * <code>false</code> otherwise
    */
   public boolean isNumber()
   {
      return (m_type == TYPE_NUMBER);
   }

   /**
    * Determine if the value of this function parameter should be a collection
    * of numbers or strings.
    *
    * @return <code>true</code> if this parameter expects a collection
    * of numbers or strings as its value, <code>false</code> otherwise
    */
   public boolean isArray()
   {
      return (m_type == TYPE_ARRAY);
   }

   /**
    * Whether the database function param value should be bound statically,
    * or dynamically. For static binding, the function parameter value is
    * evaluated and subsituted in the body of the function. For dynamic
    * binding, a "?" is subsituted for the parameter value in the function
    * body and the actual value is bound before statement execution using
    * one of the <code>setXXX()</code> methods (based on the type of the
    * parameter - number, text or array) of
    * <code>java.sql.PreparedStatement</code>.
    *
    * @return <code>true</code> if function param value should be bound
    * statically, <code>false</code> otherwise
    */
   public boolean isStaticBind()
   {
      return m_isStaticBind;
   }

   /**
    * Set whether the database function param value should be bound statically,
    * or dynamically. See {@link#isStaticBind()} for details.
    *
    * @param isStaticBind <code>true</code> if function param value should be
    * bound statically, <code>false</code> otherwise
    */
   public void setStaticBind(boolean isStaticBind)
   {
      m_isStaticBind = isStaticBind;
   }

   // Constants for XML element and attributes
   private static final String NODE_NAME = "Param";
   private static final String EL_DESCRIPTION = "Description";
   private static final String ATTR_NAME = "name";
   private static final String ATTR_TYPE = "type";
   private static final String ATTR_STATIC_BIND = "staticBind";
   private static final String ATTR_TYPE_VALUE_TEXT = "text";
   private static final String ATTR_TYPE_VALUE_NUMBER = "number";
   private static final String ATTR_TYPE_VALUE_ARRAY = "array";
   private static final String XML_TRUE = "y";
   private static final String XML_FALSE = "n";

   /**
    * Constant for function parameter of type "text".
    */
   public static final int TYPE_TEXT = 0;

   /**
    * Constant for function parameter of type "number".
    */
   public static final int TYPE_NUMBER = 1;

   /**
    * Constant for function parameter of type "array".
    */
   public static final int TYPE_ARRAY = 2;

   /**
    * Contains string constants for all the possible types of parameters.
    * If a new type is added to this array, then its corresponding JDBC type
    * must be defined at the same index in <code>PARAM_JDBC_DATA_TYPES</code>
    */
   public static final String[] PARAM_TYPES =
      {ATTR_TYPE_VALUE_TEXT, ATTR_TYPE_VALUE_NUMBER, ATTR_TYPE_VALUE_ARRAY};

   /**
    * Contains constants for the JDBC data types of parameters. If a new type
    * is added to <code>PARAM_TYPES</code>, then its corresponding JDBC type
    * must be defined at the same index in this array.
    */
   public static final int[] PARAM_JDBC_DATA_TYPES =
      {Types.VARCHAR, Types.NUMERIC, Types.ARRAY};

   /**
    * Name of the database function parameter (is case-insensitive),
    * initialized in the ctor, never <code>null</code> or empty, modified in the
    * <code>fromXml()</code> and <code>copyFrom()</code> methods.
    */
   private String m_name;

   /**
    * Stores the type of parameter value. The type of value determines if this
    * parameter should be enclosed in single quotes when being converted to a
    * string (for use in WHERE clause).
    * Initialized to <code>TYPE_TEXT</code>, modified in the
    * <code>copyFrom()</code> and method.
    */
   private int m_type = TYPE_TEXT;

   /**
    * Description for this function parameter, initialized to empty string,
    * then set in the ctor, modified in the <code>fromXml()</code> and
    * <code>copyFrom()</code> methods. Never <code>null</code>, may be empty.
    */
   private String m_desc = "";

   /**
    * Whether the database function param value should be bound statically,
    * or dynamically. See {@link#isStaticBind()} for details.
    * Initialized to <code>true</code>, modified using
    * <code>setStaticBind()</code> method.
    */
   private boolean m_isStaticBind = true;
}






