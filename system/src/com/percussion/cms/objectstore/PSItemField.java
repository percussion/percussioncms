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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.filestorage.extensions.IPSHashFileInfoExtension;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSXMLDomUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * An object representation of the StandardItem.xsd Field element.
 */
public class PSItemField extends PSItemComponent
{
   /**
    * Constructs a new instance of this object from definition objects.
    *
    * @param fieldDef system definition of this field, must not be
    * <code>null</code>.
    * @param uiDef system definition of this fields ui, must not be
    * <code>null</code>.
    * @param isMultiValue <code>true</code> if it is, otherwise
    * <code>false</code>.
    */
   public PSItemField(PSField fieldDef, PSUISet uiDef, boolean isMultiValue)
   {
      if (fieldDef == null || uiDef == null)
         throw new IllegalArgumentException("argument must not be null");

      m_fieldMeta = new PSItemFieldMeta(fieldDef, uiDef, isMultiValue);

      init();
   }

   /**
    * Initializes object members that we don't own.  Called by ctors and clone.
    */
   private void init()
   {
      m_fieldValues = new ArrayList<>();
   }

   /**
    * Returns the name of the field.
    *
    * @return name of the field.  Will not be <code>null</code> or empty.
    */
   public String getName()
   {
      return m_fieldMeta.getName();
   }

   /**
    * Each <code>PSItemField</code> has an associated <code>
    * PSItemFieldMeta</code> that contains its meta data.  This method
    * allows read access.
    *
    * @return will not return <code>null</code>.
    */
   public PSItemFieldMeta getItemFieldMeta()
   {
      return m_fieldMeta;
   }

   /**
    * Clears all values.
    */
   public void clearValues()
   {
      m_fieldValues.clear();
   }

   /**
    * Removes the first occurence  <code>IPSFieldValue</code> from the
    * collection of field values in this field based on the
    * <code>IPSFieldValue</code> reference passed in as the argument.
    *
    * @param fieldValue - must not be <code>null</code>
    */
   public void clearValue(IPSFieldValue fieldValue)
   {
      if (fieldValue == null)
         throw new IllegalArgumentException("fieldValue must not be null");

      if (m_fieldValues.contains(fieldValue))
      {
         m_fieldValues.remove(fieldValue);
      }
   }

   /**
    * Adds a field value.  If <code>isMultiValue()</code> returns <code>
    * true</code> the value will be added to the list.  If <code>isMultiValue()
    * </code> returns <code>false</code> any previous value will be over
    * ridden by the value <code>content</code>.
    *
    * @param content value that will be added.  Must not be <code>null</code>.
    * Must be IPSFieldValue type supported by this field at run time.  This is
    * determined by this fields corresponding
    * {@link PSItemFieldMeta#getBackendDataType()}.
    *
    * <TABLE BORDER="1">
    * <TR><TH>PSItemFieldMeta.getBackendDataType() returns</TH>
    * <TH>IPSFieldValue supported</TH></TR>
    *
    * <TD>{@link PSItemFieldMeta#DATATYPE_BINARY}</TD>
    * <TR><TD>PSBinaryValue</TD>
    * <TD>Binary data type only accepts binary field value.</TD></TR>
    *
    * <TD>{@link PSItemFieldMeta#DATATYPE_DATE}</TD>
    * <TR><TD>PSDateValue</TD>
    * <TD>Date data type accepts PSDateValue</TD></TR>
    *
    * <TD>{@link PSItemFieldMeta#DATATYPE_NUMERIC}</TD>
    * <TD>Numeric data type accepts PSTextValue.</TD>
    * </TR>
    *
    * <TD>{@link PSItemFieldMeta#DATATYPE_TEXT}</TD>
    * <TR><TD>Text data type Accepts PSTextValue and PSXmlValue</TD></TR>
    * </TABLE>
    *
    * When the field is loaded with data on an open.  The backend types will
    * determine which IPSFieldValue will be used to hold the data.
    */
   @SuppressWarnings("unchecked")
   public void addValue(IPSFieldValue content)
   {
      if (content == null)
         throw new IllegalArgumentException("cannot add null to field");

      // binary must be PSBinaryValue, but any back-end type may be treated as
      // binary:
      if (m_fieldMeta.isBinary())
      {
         if (!(content instanceof PSBinaryValue))
            throw new IllegalArgumentException("Invalid IPSFieldValue. binary value type expected.");
      }
      else
      {
         // datatype text must be PSXmlValue or PSTextValue:
         if ((m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_TEXT)
            && (!(content instanceof PSTextValue)
               && !(content instanceof PSXmlValue)
               && !(m_fieldMeta.getFieldDef().getSubmitName().endsWith(IPSHashFileInfoExtension.HASH_PARAM_SUFFIX))))
            throw new IllegalArgumentException("Invalid IPSFieldValue - text value expected.");

         // date must be PSDateValue
         if ((m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_DATE)
            && (!(content instanceof PSDateValue)))
            throw new IllegalArgumentException("date value type expected.");

         // numeric must be PSTextValue (no need to wrap an integer or float):
         if ((m_fieldMeta.getBackendDataType()
            == PSItemFieldMeta.DATATYPE_NUMERIC)
            && !(content instanceof PSTextValue))
            throw new IllegalArgumentException("text value type expected.");
      }

      // if not multi-value hold only one:
      if (!isMultiValue())
         m_fieldValues.clear();

      // todo when treat as xml is implemented add condition here.
      
      m_fieldValues.add(content);
   }

   /**
    * Creates and returns the appropriate field value for this field based
    * on the backend data type.
    *
    * @param stringValue - The String representation of the value to use.  This
    * method converts the stringValue to the default type for each
    * <code>IPSFieldValue</code>
    * as follows:
    * <UL>
    * <LI>Remains as <code>String</code> for <code>PSTextValue</code>.
    * </LI>
    * <LI>Remains as <code>String</code> for <code>PSXmlValue</code>.
    * </LI>
    * <LI>Base64 decoded and converted to a <code>byte[]</code> for
    * <code>PSBinaryValue</code>.
    * </LI>
    * <LI>Converted to default <code>Date</code> and <code>DateFormat</code>
    *  (using default <code>Locale</code>).
    * </LI>
    * </UL>
    * Cannot be <code>null</code>.
    *
    * @return IPSFieldValue the correct <code>IPSFieldValue</code> to use with
    * this field.  Not <code>null</code>.
    */
   public IPSFieldValue createFieldValue(String stringValue)
   {
      IPSFieldValue fieldValue = null;

      if (stringValue == null)
         throw new IllegalArgumentException("stringValue cannot be null");

      // datatype text must be PSXmlValue or PSTextValue:
      if (m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_TEXT)
         fieldValue = new PSTextValue(stringValue);

      // binary must be PSBinaryValue:
      if (m_fieldMeta.isBinary())
      {
         try
         {
            // expecting base64 encoded string, so decode it:
            ByteArrayInputStream iBuf =
               new ByteArrayInputStream(
                  stringValue.getBytes(PSCharSets.rxJavaEnc()));

            ByteArrayOutputStream oBuf = new ByteArrayOutputStream();

            PSBase64Decoder.decode(iBuf, oBuf);

            fieldValue = new PSBinaryValue(oBuf.toByteArray());
         }
         catch (UnsupportedEncodingException e) // this is not possible
         {
            throw new RuntimeException(e.toString());
         }
         catch (java.io.IOException ioe) // this is not possible
         {
            throw new RuntimeException(ioe.toString());
         }
      }
      else if (
         m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_DATE)
      {
         fieldValue = PSDateValue.getDateValueFromString(stringValue);
      }
      // numeric must be PSTextValue (no need to wrap an integer or float):
      else if (
         m_fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_NUMERIC)
      {
         fieldValue = new PSTextValue(stringValue);
      }

      return fieldValue;
   }

   /**
    * Gets the value of this field.  If <code>isMultiValue()</code>
    * returns <code>true</code> this will return the last value added to this
    * object.
    *
    * @return a value of this field.  May be <code>null</code>.
    */
   public IPSFieldValue getValue()
   {
      int i = 0;
      if (isMultiValue())
         i = m_fieldValues.size() - 1;

      if (i < 0 || i >= m_fieldValues.size())
         return null;

      return m_fieldValues.get(i);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      if (getValue() == null)
      {
         return "name=\"" + getName() + "\", value=\"\"";
      }
      else
      {
         String value = "";
         try
         {
            value = getValue().getValueAsString();
         }
         catch (Exception e)
         {
         }
         return "name=\"" + getName() + "\", value=\"" + value + "\"";
      }
   }
   
   /**
    * Returns an Iterator of <code>IPSFieldValue</code> objects.
    *
    * @return <code>IPSFieldValue</code> objects. May be empty but not
    * <code>null</code>.
    */
   public Iterator<IPSFieldValue> getAllValues()
   {
      return m_fieldValues.iterator();
   }

   /**
    * Does this field have multiple values?
    *
    * @return <code>true</code> if yes, other <code>false</code>
    */
   public boolean isMultiValue()
   {
      return m_fieldMeta.isMultiValueField();
   }

   /**
    * Convenience method to <code>toXml(doc, null)</code>.
    * @see #toXml(Document, PSAcceptElements)
    */
   @Override
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      return toXml(doc, null);
   }

   /**
    * @see PSItemComponent#toXml(Document, PSAcceptElements)
    */
   @Override
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // field:
      Element root = createStandardItemElement(doc, EL_FIELD);

      // get meta:
      root.appendChild(m_fieldMeta.toXml(doc));

      // are there fields values
      if (!m_fieldValues.isEmpty())
      {
         // is this binary, if so, check if we should include:

         if (getItemFieldMeta().isBinary()
            && acceptElements != null
            && !acceptElements.includeBinary())
            return root;

         processAllValues(root, doc);
      }
      return root;
   }

   /**
    * Called by toXml.  This iterators through all values in the field value
    * list and creates the appropriate child node and appends to the
    * <code>fieldValue</codd>.
    */
   private void processAllValues(Element rootElement, Document doc)
   {

      String theValueNodeName = null;
      // which node to use:
      // TODO: when treat as xml is implemented unhide:
      //  if(isXmlValue())
      //      theValueNodeName = EL_ANY;
      //   else
      theValueNodeName = EL_RAW_DATA;

      // actual FieldValue element:
      Element fieldValue = null;

      // raw or any:
      Element value = null;

      // the text value node to hold the ips field value:
      Text valueTextNode = null;

      // the ips field value:
      IPSFieldValue theValue = null;

      Iterator allValues = m_fieldValues.iterator();
      while (allValues.hasNext())
      {
         // fieldvalue:
         fieldValue = createStandardItemElement(doc, EL_FIELD_VALUE);
         if (m_fieldValueHref != null && m_fieldValueHref.trim().length() > 0)
            fieldValue.setAttribute(ATTR_HREF, m_fieldValueHref);

         // create the value node raw/any:
         value = createStandardItemElement(doc, theValueNodeName);

         // get the value:
         theValue = (IPSFieldValue)allValues.next();

         // set the value
         try
         {
            valueTextNode = doc.createTextNode(theValue.getValueAsString());
         }
         catch (Exception e)
         {
            // ignore if there's a problem only the XML value will
            // throw an exception.  It shouldn't occur.
         }

         // add text to valueNode:
         value.appendChild(valueTextNode);
         // add to field value:
         fieldValue.appendChild(value);
         rootElement.appendChild(fieldValue);
      }
   }

   //see interface for description
   @Override
   public Object clone()
   {
      PSItemField copy = null;
      copy = (PSItemField)super.clone();

      copy.init();
      copy.m_fieldMeta = (PSItemFieldMeta)m_fieldMeta.clone();
      if (m_fieldValues != null)
      {
         Iterator it = m_fieldValues.iterator();
         IPSFieldValue fieldValue = null;
         while (it.hasNext())
         {
            fieldValue = (IPSFieldValue)it.next();
            copy.addValue((IPSFieldValue)fieldValue.clone());
         }
      }
      return copy;
   }

   //see interface for description
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSItemField comp = (PSItemField)obj;

      if (!compare(m_fieldMeta, comp.m_fieldMeta))
         return false;
      if (!compare(m_fieldValueHref, comp.m_fieldValueHref))
         return false;
      if (!compare(m_fieldValues, comp.m_fieldValues))
         return false;

      return true;
   }

   //see interface for description
   @Override
   public int hashCode()
   {
      int hash = 0;
      // super is abtract, don't call
      hash += hashBuilder(m_fieldMeta);
      hash += hashBuilder(m_fieldValueHref);
      hash += hashBuilder(m_fieldValues);

      return hash;
   }

   /**
    * If this has an attachment this will be point to the location of the
    * attachement.
    *
    * @return never <code>null</code> may be empty.
    */
   public String getHrefLocation()
   {
      return m_fieldValueHref;
   }

   /**
    * If the value is an attachment set the location of the attachement.
    *
    * @param hrefLocation location of the attachment. May not be
    * <code>null</code> or empty.
    */
   public void setHrefLocation(String hrefLocation)
   {
      if (hrefLocation == null || hrefLocation.trim().length() == 0)
         throw new IllegalArgumentException("hrefLcoation must not be null or empty");

      m_fieldValueHref = hrefLocation;
   }

   // @see IPSDataComponent
   @Override
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      loadXmlData(sourceNode);
   }

   /**
    * This method is called to populate an object from its XML representation.
    * It assumes that the object may already have a complete data structure,
    * therefore method only overlays the data onto the existing object.
    * An element node may contain a hierarchical structure, including child
    * objects. The element node can also be a child of another element node.
    * <p> 
    * It is important to note that this implementation of the method creates
    * <code>IPSFieldValue</code> objects based on the backend data type.  If
    * the value in the <code>EL_RAW_DATA</code> elemnt cannot be parsed into a
    * date and therefore the <code>PSDateValue</code> cannot be constructed a
    * <code>PSTextValue</code> will be constructed in its place.
    * 
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by this class.
    */
   @Override
   public void loadXmlData(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, EL_FIELD);

      // skip the field meta (required)
      Element fieldMeta =
         PSXMLDomUtil.getFirstElementChild(
            sourceNode,
            PSItemFieldMeta.EL_FIELD_META);

      // clear the values first      
      clearValues();

      // update all the field values (optional)
      Element fieldValue = PSXMLDomUtil.getNextElementSibling(fieldMeta);
      while (fieldValue != null)
      {
         Element data = PSXMLDomUtil.getFirstElementChild(fieldValue);
         if (data != null)
         {
            if (PSXMLDomUtil.getUnqualifiedNodeName(data).equals(EL_RAW_DATA))
               addValue(createFieldValue(PSXMLDomUtil.getElementData(data)));

            // @todo implement when TreatAsXml is implemented.
            // else
            // addValue(new PSXmlValue(PSXmlDomUtil.getFirstElementChild(data)));

         }
         fieldValue = PSXMLDomUtil.getNextElementSibling(fieldValue);
      }
   }

   /**
    * Gets the name of this field element.
    *
    * @param el the element to retrieve the field name from, must not be <code>
    * null</code>
    *
    * @return the name of this field element
    *
    * @throws PSUnknownNodeTypeException
    */
   static String getName(Element el) throws PSUnknownNodeTypeException
   {
      Element fieldMeta =
         PSXMLDomUtil.getFirstElementChild(el, PSItemFieldMeta.EL_FIELD_META);

      return PSItemFieldMeta.getName(fieldMeta);
   }
   
   /**
    * Get the content type of this field.
    * 
    * @return the content type of this field, may be <code>null</code> or empty.
    */
   public String getContentType()
   {
      return m_contentType;
   }

   /**
    * Set the content type for this field.
    * 
    * @param contentType the new content type to set, may be <code>null</code>
    *    or empty.
    */
   public void setContentType(String contentType)
   {
      m_contentType = contentType;
   }

   /**
    * The field meta for this field.  The field meta holds the meta data
    * for this field, created by ctor, never <code>null</code> and is invariant
    */
   private PSItemFieldMeta m_fieldMeta;

   /**
    * This value will be the attribute of the field value never
    * <code>null</code> may be empty.
    * @see #setHrefLocation(String)
    */
   private String m_fieldValueHref = "";

   /**
    * The field value(s) are stored here, initialized by <code>init()</code>
    * never <code>null</code> may be empty.
    */
   private List<IPSFieldValue> m_fieldValues;
   
   /**
    * The content type set for webservice conversions, may be <code>null</code>
    * or empty.
    */
   private transient String m_contentType;

   /** Name of the elements in this class' XML representation */
   public static final String EL_FIELD = "Field";
   public static final String EL_FIELD_VALUE = "FieldValue";
   public static final String ATTR_HREF = "href";
   public static final String EL_RAW_DATA = "RawData";
   public static final String EL_ANY = "any";
}
