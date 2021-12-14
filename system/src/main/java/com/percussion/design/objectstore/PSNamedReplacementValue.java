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

import java.util.List;

/**
 * The base class for the group of replacement values that use a name as a key
 * to retrieve the runtime value.
 * 
 * @author James Schultz
 */
public abstract class PSNamedReplacementValue extends PSComponent
      implements IPSMutatableReplacementValue, IPSDocumentMapping, 
      IPSBackEndMapping
{
   /**
    * Initializes a newly created <code>PSNamedReplacementValue</code> object,
    * using the specified name.
    * @param name the key to retrieve the runtime value.  May not be empty or
    * <code>null</code>.
    */ 
   public PSNamedReplacementValue(String name)
   {
      setName( name );
   }


   /**
    * Initializes a newly created <code>PSNamedReplacementValue</code> object,
    * from an XML representation.  See {@link #toXml} for the format.
    *
    * @param sourceNode   the XML element node to construct this object from,
    * must not be <code>null</code>.
    * @param parentDoc may be <code>null</code>
    * @param parentComponents may be <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format.
    */
   public PSNamedReplacementValue(Element sourceNode, IPSDocument parentDoc,
                                  List parentComponents)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode may not be null");
      fromXml( sourceNode, parentDoc, parentComponents );
   }


   /**
    * Creates the XML serialization for the subclasses of this class.  The name
    * of the root element is determined at runtime by calling {@link 
    * #getNodeName} (which each subclass must implement).  For example, if
    * that method returned <code>PSXFoo</code>, the structure of the XML 
    * document would conform to this DTD:
    * <pre><code>
    * &lt;!ELEMENT PSXFoo (name)>
    * &lt;!ATTLIST PSXFoo
    *    id CDATA #REQUIRED
    * >
    * &lt;!ELEMENT name (#PCDATA)>
    * </code></pre>
    *
    * @return a newly created Element; never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement( getNodeName() );
      root.setAttribute( ID_ATTR, String.valueOf( m_id ) );
      
      //create the HTML parameter name element
      PSXmlDocumentBuilder.addElement( doc, root, "name", getName() );

      return root;
   }


   /**
    * This method is called to populate this instance from a XML 
    * representation. See the {@link #toXml} method for a 
    * description of the XML object.
    *
    * @param sourceNode   the XML element node to construct this object from,
    * must not be <code>null</code>.
    * @param parentDoc may be <code>null</code>
    * @param parentComponents may be <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML representation is not
    * in the expected format
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName(sourceNode, getNodeName());
      PSXmlTreeWalker tree = new PSXmlTreeWalker( sourceNode );

      String sTemp = tree.getElementData( ID_ATTR );
      try
      {
         m_id = Integer.parseInt( sTemp );
      } catch (Exception e)
      {
         Object[] args = {getNodeName(), ((sTemp == null) ? "null" : sTemp)};
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args );
      }

      try
      {
         setName( tree.getElementData( "name" ) );
      } catch (IllegalArgumentException e)
      {
         Object[] args = { getNodeName(), "name", "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );
      }
   }


   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation( this, null ))
         return;

      if ((null == m_name) || (0 == m_name.trim().length()))
         cxt.validationError( this, getErrorCode(), null );
   }


   /**
    * @return an integer error code which can be used when registering a
    * validation error.
    * @see IPSValidationContext#validationError
    */ 
   protected abstract int getErrorCode();


   /**
    * Indicates whether the specified object is "equal to" this one.
    * 
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if obj is an instance of <code>
    * PSNamedReplacementValue</code> with the same name (case-sensitive) as
    * this instance; <code>false</code> otherwise.
    */ 
   public boolean equals(Object obj)
   {
      if (obj instanceof PSNamedReplacementValue)
      {
         PSNamedReplacementValue other = (PSNamedReplacementValue) obj;
         return compare( m_name, other.m_name );
      }
      return false;
   }

   /**
    * Returns a hash code value for the object. See 
    * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
    */
   public int hashCode()
   {
      return m_id + m_name.hashCode();
   }
   

   /**
    * Gets the name of the XML element used to represent this class.
    * @return the element name, never <code>null</code> or empty
    */ 
   protected abstract String getNodeName();


   /**
    * Gets the text which can be displayed to represent this value, using the
    * format <i>node_type</i>/<i>name</i>.
    * @return the text, never <code>null</code> or empty
    */
   public String getValueDisplayText()
   {
      return getNodeName() + "/" + getName();
   }


   /**
    * Gets the implementation specific text.
    * @return the text, never <code>null</code> or empty
    * @see #getName
    */
   public String getValueText()
   {
      return getName();
   }


   /**
    * Sets the implementation specific text.
    * @param text name of object.  May not be <code>null</code> or empty.
    * @see #setName
    */
   public void setValueText(String text)
   {
      setName( text );
   }


   /**
    * Gets the name of the object whose value will be used as a key when this
    * instance is processed by an extractor at runtime.
    *
    * @return the text, never <code>null</code> or empty
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Sets the name of the object whose value will be used as a key when this
    * instance is processed by an extractor at runtime.
    * 
    * @param name the key to retrieve the runtime value.  May not be empty or
    * <code>null</code>.
    */ 
   protected void setName(String name)
   {
      if ((null == name) || (0 == name.trim().length()))
         throw new IllegalArgumentException( "name may not be null or empty" );

      m_name = name;
   }


   /**
    * Returns the string representation of this object.
    * 
    * @return the display text; never <code>null</code> or empty.
    * @see #getValueDisplayText
    */
   public String toString()
   {
      return getValueDisplayText();
   }


   /**
    * @return <code>null</code>
    */ 
   public String[] getColumnsForSelect()
   {
      return null;
   }


   /**
    * Stores the name of the object whose value will be will be used when this
    * instance resolves itself at runtime.  Never <code>null</code> or empty.
    */
   private String m_name;
}
