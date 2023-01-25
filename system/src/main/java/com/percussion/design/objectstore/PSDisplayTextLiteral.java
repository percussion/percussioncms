/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.List;

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
                               List parentComponents)
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
                       List parentComponents)
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
