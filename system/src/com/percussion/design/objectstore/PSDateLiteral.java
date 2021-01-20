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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSDateLiteral class is used to define a replacement value is a
 * static date literal value.
 *
 * @see         IPSReplacementValue
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDateLiteral extends PSLiteral
{
   /**
    * The value type associated with this instances of this class.
    */
   public static final String      VALUE_TYPE      = "DateLiteral";

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSDateLiteral(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs a literal.
    *
    * @param   date      the date to use
    *
    * @param   format   the date format to use when comparing dates
    */
   public PSDateLiteral(java.util.Date date, java.text.SimpleDateFormat format)
   {
      super();
      m_date = date;
      m_format = format;
   }

   /**
    * Default constructor for internal use
    */
   PSDateLiteral()
   {
      super();
   }

   // see interface for description
   public Object clone()
   {
      PSDateLiteral copy = null;
      copy = (PSDateLiteral) super.clone();
      copy.m_date = (Date) m_date.clone();
      copy.m_format = (SimpleDateFormat) m_format.clone();
      return copy;
   }


   /**
    * Get the date associated with this date literal.
    *
    * @return                  the date literal
    */
   public java.util.Date getDate()
   {
      return m_date;
   }

   /**
    * Set the date associated with this date literal.
    *
    * @param      date          the date literal
    */
   public void setDate(java.util.Date date)
   {
      IllegalArgumentException ex = validateDate(date);
      if (ex != null)
         throw ex;

      m_date = date;
   }

   private static IllegalArgumentException  validateDate(java.util.Date date)
   {
      if (date == null) {
         return new IllegalArgumentException ("literal date invalid: null");
      }

      return null;
   }

   /**
    * Get the date format associated with this date literal.
    *
    * @return                  the date format
    */
   public java.text.SimpleDateFormat getDateFormat()
   {
      return m_format;
   }

   /**
    * Set the date format associated with this date literal.
    *
    * @param      format       the date format
    */
   public void setDateFormat(java.text.SimpleDateFormat format)
   {
      IllegalArgumentException ex = validateDateFormat(format);
      if (ex != null)
         throw ex;

      m_format = format;
   }

   private static IllegalArgumentException validateDateFormat(
      SimpleDateFormat format)
   {
      if (format == null) {
         return new IllegalArgumentException ("literal date fmt invalid: null");
      }

      return null;
   }


   /**
    * @author chadloder
    *
    * Tests whether this object is equal to another PSDateLiteral object.
    * Formatting data does not have an effect on the comparison.
    *
    * @param   o   the PSDateLiteral object to test for equality.
    *
    * @return boolean true if this object is equal to o, false otherwise.
    *
    * @since 1.2 1999/5/5
    *
    */
   public boolean equals(Object o)
   {
      if ( !(o instanceof PSDateLiteral) )
         return false;

      PSDateLiteral other = (PSDateLiteral)o;

      if ((m_date != null) && (other.m_date != null))
         return m_date.equals(other.m_date);

      return ((m_date == null) && (other.m_date == null));
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
      return m_format.format(m_date);
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDateLiteral XML element node
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *         PSXDateLiteral is used to define a replacement value is a
    *         static date literal value.
    *    --&gt;
    *    &lt;!ELEMENT PSXDateLiteral   (date, format)&gt;
    *
    *    &lt;!--
    *       the static date literal value formatted using the pattern
    *         defined in format.
    *    --&gt;
    *    &lt;!ELEMENT date               (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the format string (as defined in java.util.SimpleDateFormat)
    *         used to parse text formatted dates for proper comparison
    *    --&gt;
    *    &lt;!ELEMENT format            (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDateLiteral XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //create text element
      PSXmlDocumentBuilder.addElement(doc, root, "date", m_format.format(m_date));
      PSXmlDocumentBuilder.addElement(doc, root, "format", m_format.toPattern());

      return root;
   }

   /**
    * This method is called to populate a PSDateLiteral Java object from a
    * PSXDateLiteral XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDateLiteral
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

      // get the date format object first
      String format = tree.getElementData("format");
      if ((format == null) || (format.length() == 0)) {
         Object[] args = { ms_NodeType, "format", "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      try {
         m_format = new SimpleDateFormat(format);
      } catch (Throwable t) {
         Object[] args = { ms_NodeType,
            "format", (format + " (Exception: " + t.toString() + ")") };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // then get the date and parse it using the formatter
      sTemp = tree.getElementData("date");
      if ((sTemp == null) || (sTemp.length() == 0)) {
         Object[] args = { ms_NodeType, "date", "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      try {
         m_date = m_format.parse(sTemp);
      } catch (Throwable t) {
         Object[] args = { ms_NodeType,
            "date", (format + " (Exception: " + t.toString() + ")") };
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

      IllegalArgumentException ex = validateDate(m_date);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDateFormat(m_format);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
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

   private java.util.Date                  m_date;
   private java.text.SimpleDateFormat      m_format;

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXDateLiteral";


   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;

      if (m_date != null)
      {
         sum += m_date.hashCode();
      }

      if (m_format != null)
      {
         sum += m_format.hashCode();
      }

      return sum;
   }

}

