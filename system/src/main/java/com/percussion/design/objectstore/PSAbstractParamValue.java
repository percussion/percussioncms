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
 * The PSAbstractParamValue class is used to set the value associated with a
 * parameter (for example, in a call to an exit or a database function).
 * The value may refer to a literal, CGI variable, HTML parameter, XML field or
 * back-end column.
 */
public abstract class PSAbstractParamValue
   extends PSComponent
   implements IPSParameter
{

   /**
    * Constructs this object from its XML representation. See the
    * {@link #toXml(Document) toXml()} method for the DTD of the
    * <code>sourceNode</code> element.
    *
    * @param sourceNode the XML element node to construct this object from,
    * may not be <code>null</code>
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    *
    * @exception PSUnknownNodeTypeException if <code>sourceNode</code> is
    * <code>null</code> or the XML element node is not of the appropriate type
    */
   public PSAbstractParamValue(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct this object from the specified replacement value.
    *
    * @param value the value to use at run-time for the parameter, may not be
    * <code>null</code>
    *
    * @see setValue()
    */
   public PSAbstractParamValue(IPSReplacementValue value)
   {
      setValue(value);
   }

   /**
    * Creates a clone of this object.
    *
    * @return cloned object, never <code>null</code>
    */
   public Object clone()
   {
      PSAbstractParamValue copy = (PSAbstractParamValue) super.clone();
      copy.m_value = (IPSReplacementValue)m_value.clone();
      return copy;
   }

   /**
    * Is this value coming from a literal?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isLiteralValue()
   {
      return PSLiteral.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * Is this value coming from a CGI variable?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isCgiVariable()
   {
      return PSCgiVariable.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * Is this value coming from an HTML parameter?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isHtmlParameter()
   {
      return (PSHtmlParameter.VALUE_TYPE.equals(m_value.getValueType())
         || PSSingleHtmlParameter.VALUE_TYPE.equals(m_value.getValueType()));
   }

   /**
    * Is this value coming from a cookie?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isCookie()
   {
      return PSCookie.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * Is this value coming from a back-end column?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isBackEndColumn()
   {
      return PSBackEndColumn.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * Is this value coming from an XML field?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isXmlField()
   {
      return PSXmlField.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * Is this value coming from the user context?
    *
    * @return <code>true</code> if this value coming from a literal,
    * <code>false</code> otherwise
    */
   public boolean isUserContext()
   {
      return PSUserContext.VALUE_TYPE.equals(m_value.getValueType());
   }

   /**
    * See {@link IPSParameter#getValue()} for details.
    */
   public IPSReplacementValue getValue()
   {
      return m_value;
   }

   /**
    * See {@link IPSParameter#setValue(IPSReplacementValue) setValue()} for
    * details.
    */
   public void setValue(IPSReplacementValue value)
   {
      if (value == null)
         throw new IllegalArgumentException("replacement value may not be null");
      m_value = value;
   }

   /**
    * This method is called to serialize this object to an XML element.
    *
    * <p>
    * The DTD of the returned XML element is:
    * <pre><code>
    *
    * &lt;!ELEMENT %getNodeName()%   (value)>
    * &lt;!ATTLIST %getNodeName()%
    *   id %UniqueId; #REQUIRED
    * >
    *
    * </code></pre>
    * %getNodeName()% value is replaced by the actual value returned by the
    * <code>getNodeName()</code> method.
    *
    * See the "sys_BasicObjects.dtd" file for the DTD of the "value" element
    *
    * @param doc The document to use when creating elements, may not be
    * <code>null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element   root = doc.createElement(getNodeName());
      root.setAttribute(ATTR_ID, String.valueOf(m_id));

      // add the value, which is a tree of its own
      Element node = PSXmlDocumentBuilder.addEmptyElement(doc, root, EL_VALUE);
      node.appendChild(((IPSComponent)m_value).toXml(doc));

      return root;
   }

   /**
    * Loads this object from the supplied element.
    * See {@link#toXml(Document) toXml()} for the expected form of XML.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @param parentDoc the Java object which is the parent of this object, may
    * be <code>null</code>
    *
    * @param parentComponents   the parent objects of this object, may be
    * <code>null</code> or empty
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is
    * <code>null</code> or does not conform to the DTD specified in
    * {@link #toXml(Document) toXml()}
    */
   public void fromXml(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try
      {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, getNodeName());

         if (false == getNodeName().equals (sourceNode.getNodeName()))
         {
            Object[] args = { getNodeName(), sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         String sTemp = sourceNode.getAttribute(ATTR_ID);
         try
         {
            m_id = Integer.parseInt(sTemp);
         }
         catch (Exception e)
         {
            Object[] args =
               { getNodeName(), ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         if (tree.getNextElement(EL_VALUE, firstFlags) != null)
         {
            // the next element under this had better be one of our objects!
            Element node = tree.getNextElement(true);
            if (node == null)
            {
               Object[] args = { getNodeName(), EL_VALUE, "null" };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            // now figure out which type it is
            IPSReplacementValue value =
               PSReplacementValueFactory.getReplacementValueFromXml(
               parentDoc, parentComponents, node, getNodeName(), EL_VALUE);
            setValue(value);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
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
      if (!cxt.startValidation(this, null))
         return;

      if (m_value instanceof IPSComponent)
      {
         cxt.pushParent(this);
         try
         {
            IPSComponent cpnt = (IPSComponent)m_value;
            cpnt.validate(cxt);
         }
         finally
         {
            cxt.popParent();
         }
      }
   }

   /**
    * Compares this object with the specified object.
    *
    * @param obj the object with which to compare this object, may not be
    * <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and the contained replacement value is equal.
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException(
            "object to be compared may not be null");

      if (!(obj instanceof PSAbstractParamValue))
         return false;

      PSAbstractParamValue other = (PSAbstractParamValue)obj;
      return compare(m_value, other.m_value);
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return m_value != null ? m_value.hashCode() : 0;
   }

   /**
    * Returns the tag name of the root element from which this object can be
    * constructed. The only abstract method of this class. Concrete classes
    * should override this method to return the name of the root element of
    * the XML generated by their <code>toXml()</code> method.
    *
    * @return the name of the root node of the XML document returned by a call
    * to {@link#toXml(Document) toXml()} method.
    *
    * @see toXml(Document)
    */
   protected abstract String getNodeName();

   /**
    * stores the replacement value associated with this parameter, initialized
    * in the ctor, modified using <code>setValue()</code> method, never
    * <code>null</code> after initialization.
    */
   private IPSReplacementValue m_value;

   /**
    * Constants for XML elements and attributes
    */
   private static final String ATTR_ID = "id";
   private static final String EL_VALUE = "value";

}

