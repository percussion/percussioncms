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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSNumericLiteral class is used to define a replacement value is a
 * static numeric literal value.
 *
 * @see         IPSReplacementValue
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSNumericLiteral extends PSLiteral
{
   /**
    * The value type associated with this instances of this class.
    */
   public static final String      VALUE_TYPE      = "NumericLiteral";

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                                                                                    object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                                                                                    object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                                                                                    if the XML element node is not of the
    *                                                                                    appropriate type
    */
   public PSNumericLiteral(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs a literal.
    *
    * @param   number   the number to use
    *
    * @param   format   the number format to use when comparing numbers
    */
   public PSNumericLiteral(Number number, java.text.DecimalFormat format)
   {
      super();

      IllegalArgumentException ex = validateNumber(number);
      if (ex != null)
         throw ex;

      ex = validateNumberFormat(format);
      if (ex != null)
         throw ex;

      if (number instanceof BigInteger)
      {
         m_number = new BigDecimal((BigInteger)number);
      }
      else if (number instanceof BigDecimal)
      {
         // java.lang.Number is immutable, so let them refer to the same
         // instance
         m_number = (BigDecimal)number;
      }
      else if (number instanceof Long)
      {
         m_number = BigDecimal.valueOf( ((Long)number).longValue() );
      }
      else
      {
         // all other numbers can fit into a double
         m_number = new BigDecimal(number.doubleValue());
      }
      m_format = format;
   }

   /**
    * Default constructor for internal use
    */
   PSNumericLiteral()
   {
      super();
   }

   // see interface for description
   public Object clone()
   {
      PSNumericLiteral copy = null;
      copy = (PSNumericLiteral) super.clone();
      copy.m_format = (DecimalFormat) m_format.clone();
      copy.m_number = new BigDecimal( m_number.toString() );
      return copy;
   }

   /**
    * Get the number associated with this number literal.
    *
    * @return                  the number literal
    */
   public java.math.BigDecimal getNumber()
   {
      return m_number;
   }

   /**
    * Set the number associated with this number literal.
    *
    * @param      number          the number literal
    */
   public void setNumber(Number number)
   {
      IllegalArgumentException ex = validateNumber(number);
      if (ex != null)
         throw ex;

      if (number instanceof BigInteger)
      {
         m_number = new BigDecimal((BigInteger)number);
      }
      else if (number instanceof BigDecimal)
      {
         // java.lang.Number is immutable, so let them refer to the same
         // instance
         m_number = (BigDecimal)number;
      }
      else
      {
         // all other numbers can fit into a double
         m_number = new BigDecimal(number.doubleValue());
      }
   }

   private static IllegalArgumentException validateNumber(Number number)
   {
      if (number == null) {
         return new IllegalArgumentException ("literal numeric invalid: null");
      }

      return null;
   }

   /**
    * Get the number format associated with this number literal.
    *
    * @return                  the number format
    */
   public java.text.DecimalFormat getNumberFormat()
   {
      return m_format;
   }

   /**
    * Set the number format associated with this number literal.
    *
    * @param      format       the number format
    */
   public void setNumberFormat(java.text.DecimalFormat format)
   {
      IllegalArgumentException ex = validateNumberFormat(format);
      if (ex != null)
         throw ex;

      m_format = format;
   }

   private static IllegalArgumentException validateNumberFormat(
      DecimalFormat format)
   {
      if (format == null) {
         return new IllegalArgumentException("literal numeric fmt invalid: null");
      }

      return null;
   }

   /* *********** IPSReplacementValue Interface Implementation *********** */

   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText()
   {
      return getValueText();
   }

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText()
   {
      return m_format.format(m_number);
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXNumericLiteral XML element node
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *                               &lt;!--
    *                                                               PSXNumericLiteral is used to define a replacement value is a
    *                                                               static number literal value.
    *                               --&gt;
    *                               &lt;!ELEMENT PSXNumericLiteral   (number, format)&gt;
    *
    *                               &lt;!--
    *                                                            the static numeric literal value formatted using the pattern
    *                                                               defined in format.
    *                               --&gt;
    *                               &lt;!ELEMENT number               (#PCDATA)&gt;
    *
    *                               &lt;!--
    *                                                            the format string (as defined in java.util.DecimalFormat)
    *                                                               used to parse text formatted numbers for proper comparison
    *                               --&gt;
    *                               &lt;!ELEMENT format               (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXNumericLiteral XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //create text element
      PSXmlDocumentBuilder.addElement(doc, root, "number", m_format.format(m_number));
      PSXmlDocumentBuilder.addElement(doc, root, "format", m_format.toPattern());

      return root;
   }

   /**
    * This method is called to populate a PSNumericLiteral Java object from a
    * PSXNumericLiteral XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                                                                             of type PSXNumericLiteral
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      //make sure we got the ACL type node
      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      // get the number format object first
      String format = tree.getElementData("format");
      if ((format == null) || (format.length() == 0))
         format = DEFAULT_FORMAT;
      try
      {
         m_format = new DecimalFormat(format);
      }
      catch (Throwable t)
      {
         Object[] args = { ms_NodeType,
            "format", (format + " (Exception: " + t.toString() + ")") };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      // then get the number and parse it using the formatter
      sTemp = tree.getElementData("number");
      if ((sTemp == null) || (sTemp.length() == 0)) {
         Object[] args = { ms_NodeType, "number", "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      try {
         // TODO: support big decimal formatting somehow, this approach
         // is limited to max double
         Number n = m_format.parse(sTemp);
         m_number = new BigDecimal(n.doubleValue());

      } catch (Throwable t) {
         Object[] args = { ms_NodeType,
            "format", (format + " (Exception: " + t.toString() + ")") };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateNumber(m_number);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateNumberFormat(m_format);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      boolean result = true;

      if (!(o instanceof PSNumericLiteral))
         result = false;
      else
      {
         PSNumericLiteral other = (PSNumericLiteral)o;

         if (!m_number.equals(other.m_number))
            result = false;
         else if (!m_format.equals(other.m_format))
            result = false;
      }

      return result;
   }


   /**
    * Returns the display text. This is implemented to assist with
    * storing objects in the GUI controls. The control automatically
    * does a toString() on the object to display the object.
    */
   public String toString()
   {
      return getValueDisplayText();
   }


   private java.math.BigDecimal          m_number;
   private java.text.DecimalFormat      m_format;

   /** The most generic format, used if no format provided*/
   private static final String DEFAULT_FORMAT = "#";

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXNumericLiteral";

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;

      if (m_format != null)
      {
         sum += m_format.hashCode();
      }

      if (m_number != null)
      {
         sum += m_number.hashCode();
      }

      return sum;
   }

}
