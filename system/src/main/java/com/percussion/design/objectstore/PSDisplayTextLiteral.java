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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Represents a string literal that displays a different value in the user
 * interface than the literal supplies at runtime.
 * 
 * @author James Schultz
 */
public class PSDisplayTextLiteral extends PSLiteral
   implements IPSMutatableReplacementValue
{
   /**
    * Construct a <code>PSDisplayTextLiteral</code> object with the provided
    * values.
    * 
    * @param displayValue String to display in user interface; may not be
    * <code>null</code> or empty.
    * @param value String to supply at runtime; may not be <code>null</code>,
    * but may be empty
    * @throws IllegalArgumentException if either param is invalid
    */
   public PSDisplayTextLiteral(String displayValue, String value)
   {
      if (null == displayValue || 0 == displayValue.trim().length())
         throw new IllegalArgumentException(
               "displayValue may not be null or empty");
      if (null == value)
         throw new IllegalArgumentException( "value may not be null" );
      m_displayValue = displayValue;
      m_value = value;
   }


   /**
    * Construct a <code>PSDisplayTextLiteral</code> object from its XML 
    * representation (see {@link #toXml} for the required format.  This method 
    * is required by {@link PSReplacementValueFactory}.
    * @param sourceNode   the XML element node to construct this object from;
    * cannot be <code>null</code>
    * @param parentDoc the Java object which is the parent of this object;
    * may be <code>null</code>
    * @param parentComponents the parent objects of this object; may be
    * <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML element node is not of the 
    * appropriate type or if sourceNode is <code>null</code>
    */
   public PSDisplayTextLiteral(Element sourceNode, IPSDocument parentDoc,
                               ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      fromXml( sourceNode, parentDoc, parentComponents );
   }


   /**
    * Gets the text which can be displayed to represent this value.
    * @return the text, never <code>null</code> or empty
    */
   public String getValueDisplayText()
   {
      return m_displayValue;
   }


   /**
    * Gets the implementation specific text.
    * @return the text, never <code>null</code>, but may be empty
    */
   public String getValueText()
   {
      return m_value;
   }


   // see interface for description; see toXml() for the format
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName( sourceNode, XML_NODE_NAME );
      m_displayValue = sourceNode.getAttribute( "displayText" );
      m_value = PSXmlTreeWalker.getElementData( sourceNode );
   }


   /**
    * Returns the XML representation of this object, which is in the format:
    * <pre><code>
    * &lt;PSXDisplayTextLiteral displayText="Hello There">
    * hi
    * &lt;/PSXDisplayTextLiteral>
    * </code></pre>
    * 
    * @param doc The XML document being constructed, needed to create new
    * elements.  May not be <code>null</code>.
    * @return a <&lt;PSXDisplayTextLiteral> Element; never <code>null</code>
    * @throws IllegalArgumentException if doc is <code>null</code>
    */ 
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException( "Must provide a valid Document" );
      Element root = doc.createElement( XML_NODE_NAME );
      Text textNode = doc.createTextNode(
            PSXmlDocumentBuilder.normalize( m_value ) );
      root.appendChild( textNode );
      root.setAttribute( "displayText", m_displayValue );
      return root;
   }


   /**
    * Gets the type of replacement value this object represents.
    * @return {@link #VALUE_TYPE}, never <code>null</code> or empty.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   
   /**
    * Sets the implementation specific text which this value will supply.
    * @param text String to supply at runtime; may not be <code>null</code>,
    * but may be empty
    */ 
   public void setValueText(String text)
   {
      if (null == text)
         throw new IllegalArgumentException( "text may not be null" );
      m_value = text;
   }

   
   /**
    * Compares an object to this PSDisplayTextLiteral to see if they are equal.
    * 
    * @return <code>true</code> if object is a PSDisplayTextLiteral with the
    * same display value and value as this PSDisplayTextLiteral; <code>false
    * </code> otherwise.
    */ 
   public boolean equals(Object o)
   {
      if (o instanceof PSDisplayTextLiteral)
      {
         PSDisplayTextLiteral literal = (PSDisplayTextLiteral) o;
         if (m_displayValue.equals( literal.m_displayValue ) &&
               m_value.equals( literal.m_value ))
            return true;
      }
      return false;
   }


   /** The value type associated with instances of this class. */
   public static final String VALUE_TYPE = "DisplayTextLiteral";

   /** 
    * The display value. Set in constructor; never <code>null</code> or empty
    */
   private String m_displayValue;

   /** 
    * The replacement value. Set in constructor; never <code>null</code>, may
    * be empty
    */
   private String m_value;

   /** Name of parent XML element */
   static final String XML_NODE_NAME = "PSXDisplayTextLiteral";

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;
      
      if (m_displayValue != null)
      {
         sum += m_displayValue.hashCode();
      }
      
      if (m_value != null)
      {
         sum += m_value.hashCode();
      }
      
      return sum;
   }

}
