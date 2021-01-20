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
package com.percussion.search.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to represent the <code>SearchField</code> element of the document
 * created from a {@link PSWSSearchRequest} object.  See 
 * {@link #toXml(Document)} for more information.  Instances of this class are
 * immutable after construction.
 */
public class PSWSSearchField
{
   /**
    * Construct a field using an internal operator.
    * 
    * @param name The name of the field, may not be <code>null</code> or empty.
    * @param op The operator to use, must be one of the <code>OP_ATTR_XXX</code>
    * values.
    * @param value The value of the field, may not be <code>null</code>.
    * @param connector The connector used to specify the logical operand used to
    * connect this field with the next field within a list.  Must be one of the
    * <code>CONN_ATTR_XXX</code> values.
    */    
   public PSWSSearchField(String name, int op, String value, int connector)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (op < 0 || op >= OP_ATTR_VALUES.length)
         throw new IllegalArgumentException("invalid operator");
      
      if (connector < 0 || connector >= CONN_ATTR_VALUES.length)
         throw new IllegalArgumentException("invalid connector");

      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      m_name = name;
      m_operator = PSOperatorEnum.valueOf(op);      
      m_value = value;
      m_connector = PSConnectorEnum.valueOf(connector);
   }
   
   /**
    * Construct a field using an external operator.  Same as 
    * {@link #PSWSSearchField(String, int, String, int) 
    * this(name, op, value, connector)} but the <code>op</code> parameter is 
    * replaced with the <code>extOp</code> parameter described below.
    * 
    * @param extOp The external operator to used, passed thru to the external
    * search engine.  May not be <code>null</code> or empty. 
    */    
   public PSWSSearchField(String name, String extOp, String value, 
      int connector)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (connector < 0 || connector >= CONN_ATTR_VALUES.length)
         throw new IllegalArgumentException("invalid connector");
      
      if (extOp == null || extOp.trim().length() == 0)
         throw new IllegalArgumentException("extOp may not be null or empty");

      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      m_name = name;      
      m_externalOperator = extOp.trim();
      m_value = value;
      m_connector = PSConnectorEnum.valueOf(connector);
   }
   
   /**
    * Construct this field from its XML representation.
    * 
    * @param src The source element, may not be <code>null</code>.  See
    * {@link #toXml(Document)} for details on the expected format.
    * 
    * @throws PSUnknownNodeTypeException If the source element does not match
    * the expected format.
    */
   public PSWSSearchField(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      fromXml(src);
   }
      
   /**
    * Serializes this object to its XML representation.  See the 
    * sys_SearchParameters.xsd schema for details and the required format of the
    * <code>SearchParams</code> element.
    * 
    * @param doc The document to use, may not be <code>null</code>.
    *  
    * @return The root element of the search request, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME); 
      PSXmlDocumentBuilder.replaceText(doc, root, m_value);

      root.setAttribute(ATTR_NAME, m_name);
      root.setAttribute(ATTR_OPERATOR, OP_ATTR_VALUES[m_operator.getOrdinal()]);
      root.setAttribute(ATTR_CONNECTOR, 
         CONN_ATTR_VALUES[m_connector.getOrdinal()]);      
      root.setAttribute(ATTR_EXT_OPERATOR, m_externalOperator);
      
      return root;
   }
   
   /**
    * Helper method to get the <code>OP_ATTR_XXX</code> value from its string
    * representation.
    * 
    * @param strOp The string version, may be <code>null</code> or empty.
    * 
    * @return The matching operator value, or <code>-1</code> if no match is
    * found.
    */
   public static int getOperatorFromString(String strOp)
   {
      int op = -1;
      
      for (int i = 0; i < OP_ATTR_VALUES.length; i++)
      {
         if (OP_ATTR_VALUES[i].equals(strOp))
         {
            op = i;
            break;
         }
      }
      
      return op;
   }
   
   /**
    * Get the string representation of the operator as defined by the 
    * sys_SearchParameters.xsd schema.
    * 
    * @return The operator string, never <code>null</code> or empty.
    */
   public String getStringOperator()
   {
      return OP_ATTR_VALUES[m_operator.getOrdinal()];
   }
   
   /**
    * Get the string representation of the connector as defined by the 
    * sys_SearchParameters.xsd schema.
    * 
    * @return The connector string, never <code>null</code> or empty.
    */
   public String getStringConnector()
   {
      return CONN_ATTR_VALUES[m_connector.getOrdinal()];
   }
 
   /**
    * Restores a search request from its XML representation.  
    * 
    * @param src The root element of the request, assumed not <code>null</code>.
    * See {@link #toXml(Document)} for details on the expected format.
    * 
    */
   private void fromXml(Element src) throws PSUnknownNodeTypeException
   {
      // validate root
      PSXMLDomUtil.checkNode(src, XML_NODE_NAME);
      
      // load attrs
      m_name = PSXMLDomUtil.checkAttribute(src, ATTR_NAME, true);
      m_operator = PSOperatorEnum.valueOf(
         PSXMLDomUtil.checkAttributeEnumerated(src, ATTR_OPERATOR, 
            OP_ATTR_VALUES, false));
      m_connector = PSConnectorEnum.valueOf(
         PSXMLDomUtil.checkAttributeEnumerated(src, ATTR_CONNECTOR, 
            CONN_ATTR_VALUES, false));
      m_externalOperator = PSXMLDomUtil.checkAttribute(src, ATTR_EXT_OPERATOR, 
         false).trim();
      m_value = PSXMLDomUtil.getElementData(src);
   }
   
   /**
    * Get the name of this field.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the internal operator supplied during construction.  If one was not 
    * supplied, {@link #getExternalOperator()} will return a non-empty value, 
    * and this value should be ignored, in which case {@link #OP_ATTR_EQUAL} 
    * is returned by default.  Otherwise this field should be used to query
    * the internal search engine using this operator.
    * 
    * @return One of the <code>OP_ATTR_XXX</code> values.
    * @deprecated use {@link #getOperatorEnum()} instead.
    */
   public int getOperator()
   {
      return m_operator.getOrdinal();
   }
   
   /**
    * Get the operator enumeration.
    * 
    * @return the operator enumeration, never <code>null</code>.
    */
   public PSOperatorEnum getOperatorEnum()
   {
      return m_operator;
   }
   
   /**
    * Get the connector supplied during construction.
    * 
    * @return One of the <code>CONN_ATTR_XXX</code> values.
    * @deprecated use {@link #getConnectorEnum()} instead.
    */
   public int getConnector()
   {
      return m_connector.getOrdinal();
   }
   
   /**
    * Get the connector enumeration.
    * 
    * @return the connector enumeration, never <code>null</code>.
    */
   public PSConnectorEnum getConnectorEnum()
   {
      return m_connector;
   }
   
   /**
    * Get the external operator supplied during construction.  If one was not
    * supplied, then an empty string is returned, and the value of 
    * {@link #getOperator()} should be used to construct search criteria from 
    * this field.
    * 
    * @return Never <code>null</code>, may be empty.  If non-empty, this field
    * should be used to query the external search engine.
    */
   public String getExternalOperator()
   {
      return m_externalOperator;
   }
   
   /**
    * Get the value supplied during construction.
    * 
    * @return The value, never <code>null</code>, may be empty.
    */
   public String getValue()
   {
      return m_value;
   }
   
   /**
    * Determine if this field specifies an external operator.
    * 
    * @return <code>true</code> if {@link #getExternalOperator()} would return
    * a non-empty value, <code>false</code> otherwise.
    */
   public boolean isExternal()
   {
      return m_externalOperator.trim().length() > 0;
   }
   
   /**
    * Overriden to properly fufill contract of {@link Object#hashCode()}.
    */
   public int hashCode()
   {
      return (m_name + m_operator + m_externalOperator + m_value + 
         m_connector).hashCode();
   }
   
   /**
    * Overrides {@link Object#equals(Object)} to compare all member data.
    * 
    * @param obj The object to compare, may be <code>null</code>.
    * 
    * @return <code>true</code> if <code>obj</code> is an instance of 
    * {@link PSWSSearchField} with the same member data.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      
      if (!(obj instanceof PSWSSearchField))
         isEqual = false;
      else if (this != obj)
      {
         PSWSSearchField other = (PSWSSearchField)obj;
         if (!m_name.equals(other.m_name))
            isEqual = false;
         else if (m_operator != other.m_operator)
            isEqual = false;
         else if (!m_externalOperator.equals(other.m_externalOperator))
            isEqual = false;
         else if (!m_value.equals(other.m_value))
            isEqual = false;
         else if (m_connector != other.m_connector)
            isEqual = false;
      }
      
      return isEqual;
   }
   
   /**
    * Enumerates all valid search field operators.
    */
   public enum PSOperatorEnum
   {
      EQUAL(OP_ATTR_EQUAL),
      NOTEQUAL(OP_ATTR_NOTEQUAL),
      LESSTHAN(OP_ATTR_LESSTHAN),
      LESSTHANEQUAL(OP_ATTR_LESSTHANEQUAL),
      GREATERTHAN(OP_ATTR_GREATERTHAN),
      GREATERTHANEQUAL(OP_ATTR_GREATERTHANEQUAL),
      ISNULL(OP_ATTR_ISNULL),
      ISNOTNULL(OP_ATTR_ISNOTNULL),
      IN(OP_ATTR_IN),
      NOTIN(OP_ATTR_NOTIN),
      LIKE(OP_ATTR_LIKE),
      NOTLIKE(OP_ATTR_NOTLIKE);
      
      /**
       * Get the ordinal of the enumeration.
       * 
       * @return the ordinal.
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      /**
       * Get the enumeration for the supplied ordinal.
       * 
       * @param ordinal the ordinal for which to get the enumeration.
       * @return the enumeration, never <code>null</code>.
       * @throws IllegalArgumentException if no enumeration exists for the
       *    supplied ordinal.
       */
      public static PSOperatorEnum valueOf(int ordinal)
      {
         for (PSOperatorEnum value : values())
            if (value.getOrdinal() == ordinal)
               return value;

         throw new IllegalArgumentException(
            "No operator is defined for the supplied ordinal.");
      }
      
      /**
       * Constructs an enumeration for the specified ordinal.
       * 
       * @param ordinal the enumeration ordinal.
       */
      private PSOperatorEnum(int ordinal)
      {
         mi_ordinal = ordinal;
      }
      
      /**
       * Stores the enumeration ordinal.
       */
      private int mi_ordinal;
   }
   
   /**
    * Enumerates all valid search field connectors.
    */
   public enum PSConnectorEnum
   {
      AND(CONN_ATTR_AND),
      OR(CONN_ATTR_OR);
      
      /**
       * Get the ordinal of the enumeration.
       * 
       * @return the ordinal.
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      /**
       * Get the enumeration for the supplied ordinal.
       * 
       * @param ordinal the ordinal for which to get the enumeration.
       * @return the enumeration, never <code>null</code>.
       * @throws IllegalArgumentException if no enumeration exists for the
       *    supplied ordinal.
       */
      public static PSConnectorEnum valueOf(int ordinal)
      {
         for (PSConnectorEnum value : values())
            if (value.getOrdinal() == ordinal)
               return value;

         throw new IllegalArgumentException(
            "No connector is defined for the supplied ordinal.");
      }
      
      /**
       * Constructs an enumeration for the specified ordinal.
       * 
       * @param ordinal the enumeration ordinal.
       */
      private PSConnectorEnum(int ordinal)
      {
         mi_ordinal = ordinal;
      }
      
      /**
       * Stores the enumeration ordinal.
       */
      private int mi_ordinal;
   }
   
   /**
    * Name of root element when this object is serialized to and from its XML
    * representation.
    */
   public static final String XML_NODE_NAME = "SearchField";

   /**
    * Name of this field, never <code>null</code> or empty or modified after
    * construction.
    */
   private String m_name;
   
   /**
    * The operator supplied during ctor, or {@link #OP_ATTR_EQUAL} if one is not
    * supplied, never modified after that.
    */
   private PSOperatorEnum m_operator = PSOperatorEnum.EQUAL;
   
   /**
    * The connector supplied during ctor, never modified after that.
    */
   private PSConnectorEnum m_connector = PSConnectorEnum.AND;
   
   /**
    * The external operator supplied during construction, never 
    * <code>null</code> or modified after that, may be empty.
    */
   private String m_externalOperator = "";
   
   /**
    * The value supplied during construction, never <code>null</code> or empty,
    * never modified.
    */
   private String m_value;

   /**
    * Constant for <code>equal</code> operator.
    */
   public static final int OP_ATTR_EQUAL             = 0;

   /**
    * Constant for <code>notEqual</code> operator.
    */
   public static final int OP_ATTR_NOTEQUAL          = 1;

   /**
    * Constant for <code>lessThan</code> operator.
    */
   public static final int OP_ATTR_LESSTHAN          = 2;

   /**
    * Constant for <code>lessThanEqual</code> operator.
    */
   public static final int OP_ATTR_LESSTHANEQUAL     = 3;

   /**
    * Constant for <code>greaterThan</code> operator.
    */
   public static final int OP_ATTR_GREATERTHAN       = 4;

   /**
    * Constant for <code>greaterThanEqual</code> operator.
    */
   public static final int OP_ATTR_GREATERTHANEQUAL  = 5;

   /**
    * Constant for <code>isNull</code> operator.
    */
   public static final int OP_ATTR_ISNULL            = 6;

   /**
    * Constant for <code>isNotNull</code> operator.
    */
   public static final int OP_ATTR_ISNOTNULL         = 7;

   /**
    * Constant for <code>in</code> operator.
    */
   public static final int OP_ATTR_IN                = 8;

   /**
    * Constant for <code>notIn</code> operator.
    */
   public static final int OP_ATTR_NOTIN             = 9;

   /**
    * Constant for <code>like</code> operator.
    */
   public static final int OP_ATTR_LIKE              = 10;

   /**
    * Constant for <code>notLike</code> operator.
    */
   public static final int OP_ATTR_NOTLIKE           = 11;

   /**
    * Enumeration of the string representation of the <code>OP_ATTR_xxx</code>
    * constants, used when serialized to XML.  Each constant value is an index
    * into this array of its string representation.  This array must be modified
    * as operator constants are added, modified, or removed.
    */
   public static final String[] OP_ATTR_VALUES = 
   {
      "equal", "notEqual", "lessThan", "lessThanEqual", "greaterThan", 
      "greaterThanEqual", "isNull", "isNotNull", "in", "notIn", "like", 
      "notLike" 
   };

   /**
    * Constant for the <code>and</code> connector.
    */
   public static final int CONN_ATTR_AND               = 0;

   /**
    * Constant for the <code>or</code> connector.
    */
   public static final int CONN_ATTR_OR                = 1;

   /**
    * Enumeration of the string representation of the <code>CONN_ATTR_xxx</code>
    * constants, used when serialized to XML.  Each constant value is an index
    * into this array of its string representation.  This array must be modified
    * as operator constants are added, modified, or removed.
    */
   public static final String[] CONN_ATTR_VALUES = {"and", "or"};
   
   // private xml constants
   private static final String ATTR_NAME = "name";
   private static final String ATTR_OPERATOR = "operator";
   private static final String ATTR_CONNECTOR = "connector";
   private static final String ATTR_EXT_OPERATOR = "externalOperator";

}
