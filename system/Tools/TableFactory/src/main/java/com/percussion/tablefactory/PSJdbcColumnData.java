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
package com.percussion.tablefactory;

import com.percussion.util.IOTools;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.*;
import java.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents the value of a column in a row as a String.  This
 * class is readonly once instantiated intentionally, so that once a
 * PSJdbcTableData is created, it is immutable.  This is so once it is set on
 * a PSJdbcTableSchema object and validated, and cannot later be modified and
 * invalidated.
 */
public class PSJdbcColumnData
{
   /**
    * Creates a column with the specified name, value and encoding.
    *
    * @param name The name of the column, may not be <code>null</code> or empty.
    * @param value The value of this column,  may be <code>null</code> or
    * empty.
    * @param encoding The encoding to use. vlid values are ENC_TEXT,
    * ENC_ESCAPED or ENC_BASE64
    *
    * @throws IllegalArgumentException if the name is <code>null</code>
    * or empty or encoding provided is invalid
    */
   public PSJdbcColumnData(String name, String value, int encoding)
   {
      if ((name == null || name.trim().length() == 0))
         throw new IllegalArgumentException("name is invalid");

      if (!validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding for: " + name);

      m_name = name;
      m_value = value;
      m_encoding = encoding;
   }
   /**
    * Creates a column with the specified name and value. The encoding defaults
    * to ENC_TEXT ("text").
    *
    * @param name The name of the column, may not be <code>null</code> or empty.
    * @param value The value of this column,  may be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if the name is <code>null</code>
    * or empty
    */
   public PSJdbcColumnData(String name, String value)
   {
      this(name, value, ENC_TEXT);
   }

   /**
    * Creates a column from it's Xml source.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the column element in the
    *    tabledata.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcColumnData(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }

   /**
    * Returns this column's name.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns this column's value. If the data value is external it will attempt
    * to lazily load the value from external file path set in the external
    * attribute.
    *
    * @return The value, may be <code>null</code> or empty.
    */
   public String getValue()
   {
      if (m_external==null || m_external.length()==0)
         return m_value;
      
      return getExternalData(m_external, getEncoding());
   }
   
   /**
    * Fetches external data from the external file path supplied by the external
    * attribute. Encodes the data with 
    * 
    * @param externalPath must be a valid path to the file,
    * never <code>null</code> or <code>empty</code>.
    * 
    * @param encoding assumes one of the allowed ENC_* encodings.
    * 
    * @return encoded data extracted from external file,
    * may be <code>null</code> or <code>empty</code>.
    */
   private String getExternalData(String externalPath, int encoding)
   {
      if (externalPath == null)
         throw new IllegalArgumentException("externalPath may not be null");

      File f = new File(externalPath);

      String data;
      try ( FileInputStream is = new FileInputStream(f);
            ByteArrayOutputStream oBuf = new ByteArrayOutputStream()){
         OutputStream os = oBuf;
         if (encoding == ENC_BASE64 )
         {
            os = Base64.getMimeEncoder().wrap(oBuf);
         }

         IOTools.copyStream(is, os);
         data = oBuf.toString(PSCharSets.rxJavaEnc());
      }
      catch (Exception e1)
      {
         throw new IllegalStateException("failed to fetch external resource " +
            externalPath + " exception: " + e1.getLocalizedMessage());
      }
      
      return data;
   }

   /**
    * returns the encoding for this column.
    *
    * @return the encoding, should be one of the ENC_TEXT, ENC_ESCAPED or ENC_BASE64.
    */
   public int getEncoding()
   {
      return m_encoding;
   }

   /**
    * returns the encoding for this column as a String.
    *
    * @return the encoding, should be one of the ENC_TEXT_STR, ENC_ESCAPED_STR
    * or ENC_BASE64_STR.
    */
   public String getEncodingAsString()
   {
      switch (m_encoding)
      {
         case ENC_ESCAPED:
            return ENC_ESCAPED_STR;

         case ENC_BASE64:
            return ENC_BASE64_STR;

         default:
            return ENC_TEXT_STR;
      }
   }

   /**
    *  sets the value of the column
    *
    *  @param value the new value as a string, can be <code>null</code>
    *  or empty
    */
   public void setValue(String value)
   {
      m_value = value;
   }

   /**
    * sets the value and encoding of the column
    *
    * @param value the new value as a string, can be <code>null</code>
    * or empty
    * @param encoding the encoding for this value
    * Valid values are ENC_TEXT, ENC_ESCAPED or ENC_BASE64.
    *
    * @throws IllegalArgumentException if the encoding specified is invalid.
    */
   public void setValue(String value, int encoding)
   {
      m_value = value;

      if (!validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding");
      m_encoding = encoding;
   }

   /**
    * sets the encoding
    *
    * @param encoding the new encoding, should be one of the ENC_TEXT,
    * ENC_ESCAPED or ENC_BASE64
    * @throws IllegalArgumentException if the provided encoding is invalid.
    */
   public void setEncoding(int encoding)
   {
      if (!validEncoding(encoding))
         throw new IllegalArgumentException("invalid encoding");
      m_encoding = encoding;
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the column element in the
    *    tabledata.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_name = PSJdbcTableComponent.getAttribute(tree, NAME_ATTR, true);
      if (m_name == null || m_name.trim().length() == 0)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.INVALID_COLUMN_NAME, NODE_NAME);

      String externalAttr =
         PSJdbcTableComponent.getAttribute(tree, EXTERNAL_ATTR, false);
      
      if (externalAttr != null)
      { 
         //get resolve and validate the external attribute
         String parentName = tree.getCurrent().getNodeName();
         
         m_external = resolveExternalResourcePath(parentName, externalAttr);
      }

      m_encoding = PSJdbcTableComponent.getEnumeratedAttributeIndex(tree,
         ENCODING_ATTR, ENCODING_ENUM);

      m_value = PSXmlTreeWalker.getElementData(sourceNode);
      if (m_value.trim().length() == 0)
      {
         /* Value is empty, need to determine if it should be treated as null.
          * If attribute is not specified or if equals "yes", switch to null,
          * otherwise if specified as "no", leave as empty
          */
         String isEmptyNull = PSJdbcTableComponent.getAttribute(tree,
            EMPTY_NULL_ATTR, false);
         if (isEmptyNull == null || isEmptyNull.trim().equalsIgnoreCase(
            EMPTY_NULL_TRUE))
         {
            m_value = null;
         }
         else if (isEmptyNull != null && !isEmptyNull.trim().equalsIgnoreCase(
            EMPTY_NULL_FALSE))
         {
            // bad value for attribute
            Object[] args = {NODE_NAME, EMPTY_NULL_ATTR, isEmptyNull};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }
   }
   
   /**
    * Resolves and validates external resource file path. 
    * 
    * @param parentNodeName name of the node which has external attribute,
    * never <code>null</code> or <code>empty</code>. 
    * 
    * @param external value of the external attribute that may contain a 
    * resolvable keyword,
    * ie: "{cmstableData.external.root}/external/images/foo.jpg",
    * which purpose is to specify where the resources root is.
    * The keyword value could be set as a System property, if not set
    * defaults to the current directory, never <code>null</code> or
    * <code>empty</code>.
    * 
    * @return a resolved full path to external resource file.
    * 
    * @throws PSJdbcTableFactoryException if the external resource can not
    * be found or the file path was malformed.
    */
   private String resolveExternalResourcePath(String parentNodeName,
      String external)
      throws PSJdbcTableFactoryException
   {
      if (parentNodeName == null || parentNodeName.length()==0)
         throw new IllegalArgumentException("parentName may not be null");
         
      if (external == null)
         throw new IllegalArgumentException("externalAttr may not be null");
      
      //external is only valid if it has a valid path to an external resource
      //ie: external="{cmstableData.external.root}/external/images/foo.jpg"
      //or: external="/external/images/foo.jpg"
      
      //get the replacement root value, ie: {cmstableData.external.root}
      int rootStart = external.indexOf('{');
      String root = ".";
      if (rootStart >= 0)
      {
         int rootEnd = external.indexOf('}');
         if (rootEnd > rootStart)
         {
            String rootKey = external.substring(rootStart, rootEnd + 1);
            String rootVal = System.getProperty(rootKey);

            if (rootVal!=null && rootVal.trim().length()!=0)
               root = rootVal;
            
            external = PSStringOperation.replace(external,
              rootKey, root);
         }
      }
      
      File f = new File(external);
      if (!(f.exists() && f.canRead()))
      {
         Object[] args = {parentNodeName, EXTERNAL_ATTR, external};
         throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      return external;
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledata.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(ENCODING_ATTR, getEncodingAsString());

      if (m_value == null)
      {
         root.appendChild(doc.createTextNode(""));
      }
      else
      {
         root.appendChild(doc.createTextNode(PSXmlDocumentBuilder.normalize(
            m_value)));
         // if value is empty, set attribute to indicate it is not null
         if (m_value.trim().length() == 0)
            root.setAttribute(EMPTY_NULL_ATTR, EMPTY_NULL_FALSE);
      }

      return root;
    }

   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcColumnData with
    *    the same column names and values. Otherwise returns <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcColumnData))
         isMatch = false;
      else
      {
         PSJdbcColumnData other = (PSJdbcColumnData)obj;
         if (!this.m_name.equals(other.m_name))
            isMatch = false;
         else if (!this.m_value.equals(other.m_value))
            isMatch = false;
         else if (this.m_encoding != other.m_encoding)
            isMatch = false;
      }

      return isMatch;
   }


   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      if ( null != m_name )
         hash += m_name.hashCode();
      if ( null != m_value )
         hash += m_value.hashCode();
      return hash;
   }

   /**
    * Validates the supplied encoding string, Should be one of the ENC_TEXT,
    * ENC_ESCAPED or ENC_BASE64
    *
    * @return <code>false</code> if the provided encoding is invalid,
    *    <code>true</code> otherwise.
    */
   public static boolean validEncoding(int enc)
   {
      return ((enc == ENC_TEXT) || (enc == ENC_ESCAPED) || (enc == ENC_BASE64));
   }
   
   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "column";

   /**
    * Constant to identify the encoding as "text".
    */
   public static final int ENC_TEXT = 0;

   /**
    * String representation of the text encoding in XML files.
    */
   public static final String ENC_TEXT_STR = "text";

   /**
    * Constant to identify the encoding as "escaped".
    */
   public static final int ENC_ESCAPED = 1;

   /**
    * String representation of the escaped encoding in XML files.
    */
   public static final String ENC_ESCAPED_STR = "escaped";

   /**
    * Constant to identify the encoding as "base64".
    */
   public static final int ENC_BASE64 = 2;

   /**
    * String representation of the Base64 encoding in XML files.
    */
   public static final String ENC_BASE64_STR = "base64";

   /**
    * An array of XML attribute values for the encoding.
    */
   private static final String[] ENCODING_ENUM = {ENC_TEXT_STR, ENC_ESCAPED_STR, ENC_BASE64_STR};

   /**
    * This column's name, never <code>null</code> or empty once constructed.
    */
   private String m_name = null;

   /**
    * This column's value, may be <code>null</code> or empty.
    */
   private String m_value = null;

   /**
    * This column's encoding type. Defaults to "text" encoding.
    **/
   private int m_encoding = ENC_TEXT;
   
   /**
    * Holds a value found in the 'external' attribute if any,
    * may be <code>null</code>, never <code>empty</code>.
    */
   private String m_external = null;    
   
   
   // Constants for Xml Elements and Attibutes
   private static final String NAME_ATTR = "name";
   private static final String EMPTY_NULL_ATTR = "isEmptyNull";
   private static final String EMPTY_NULL_TRUE = "yes";
   private static final String EMPTY_NULL_FALSE = "no";
   private static final String ENCODING_ATTR = "encoding";
   private static final String EXTERNAL_ATTR = "external";
}

