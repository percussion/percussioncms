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
import com.percussion.util.PSXMLDomUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * An object representation of the StandardItem FieldMeta element.  This class
 * contains all of the descriptive elements of the associated element.  That
 * associated element may be a field, child, child entry or a related item.
 */
public class PSItemFieldMeta extends PSItemComponent
{
   /**
    * Creates a new object.  Package protected constructor.
    *
    * @param fieldDef system definition of this field, must not be
    * <code>null</code>.
    * @param uiDef system definition of this fields ui, must not be
    * <code>null</code>.
    * @param isMultiValue <code>true</code> if it is, otherwise
    * <code>false</code>.
    */
   PSItemFieldMeta(PSField fieldDef, PSUISet uiDef, boolean isMultiValue)
   {
      super();
      if (fieldDef == null || uiDef == null)
         throw new IllegalArgumentException("argument must not be null");

      m_fieldDef = fieldDef;
      m_uiDef = uiDef;
      m_isMultiValueField = isMultiValue;

      init();
   }

   /**
    * Initialize object.
    */
   private void init()
   {
      //@todo: when ui is implemented add accessors for:
      m_mimeType = m_fieldDef.getMimeType();
      m_AllowedValuesOptionMap = new TreeMap();
   }

   /**
    * Add options to fields.  These supply the elements in the
    * ValueChoices/Options element of the StandardItem.xsd.  They will be sorted
    * based on the <code>displayName</code> by the <code>Comparable</code> rules.
    *
    * @param displayName the name that may be used for UI purposes. It may be
    *    <code>null</code> or empty. <code>null</code> will be treated as empty.
    * @param optionValue the value that will be persisted.  It may be
    *    <code>null</code> or empty. <code>null</code> will be treated as empty. 
    */
   public void addOptions(String displayName, String optionValue)
   {
      if (displayName == null)
         displayName = "";

      if (optionValue == null)
         optionValue = "";

      m_AllowedValuesOptionMap.put(displayName, optionValue);
   }

   /**
    * The data type of the column to which the field value will be persisted.
    * Will be one of the following:
    * <ul>
    * <li>{@link PSItemFieldMeta#DATATYPE_DATE} if the backend data type is:
    * date, time or datetime
    * </li>
    * <li>{@link PSItemFieldMeta#DATATYPE_TEXT} if the backend data type is:
    * text, boolean or was not set (default)
    * </li>
    * <li>{@link PSItemFieldMeta#DATATYPE_NUMERIC}if the backend data type is:
    * integer or float
    * </li>
    * <li>{@link PSItemFieldMeta#DATATYPE_BINARY}if the backend data type is:
    * binary.</li>
    * </ul>
    *
    * @return return a valid type.
    */
   public int getBackendDataType()
   {
      int datatype = DATATYPE_TEXT;

      if (m_fieldDef.getDataType().length() == 0)
         datatype = DATATYPE_TEXT;

      if (m_fieldDef.getDataType().equals(PSField.DT_BINARY))
         datatype = DATATYPE_BINARY;

      if (m_fieldDef.getDataType().equals(PSField.DT_TEXT))
         datatype = DATATYPE_TEXT;

      if (m_fieldDef.getDataType().equals(PSField.DT_DATE))
         datatype = DATATYPE_DATE;

      if (m_fieldDef.getDataType().equals(PSField.DT_TIME))
         datatype = DATATYPE_DATE;

      if (m_fieldDef.getDataType().equals(PSField.DT_DATETIME))
         datatype = DATATYPE_DATE;

      if (m_fieldDef.getDataType().equals(PSField.DT_INTEGER))
         datatype = DATATYPE_NUMERIC;

      if (m_fieldDef.getDataType().equals(PSField.DT_BOOLEAN))
         datatype = DATATYPE_TEXT;

      if (m_fieldDef.getDataType().equals(PSField.DT_FLOAT))
         datatype = DATATYPE_NUMERIC;

      return datatype;
   }

   /**
    * Returns a list of the display names in the options.  With display name
    * you can get the option value by calling <code>getOptionValueByDisplayName
    * </code>.
    *
    * @return Iterator of <code>String</code>s that are the display names.
    * May return <code>null</code> if options are empty.
    */
   public Iterator getOptionDisplayNames()
   {
      return m_AllowedValuesOptionMap.keySet().iterator();
   }

   /**
    * Return the specified value for a display name.
    *
    * @param displayName  Must not be <code>null</code> or empty.
    * @return the value corresponding to the display name.  May be
    *  <code>null</code> or empty.
    */
   public String getOptionValueByDisplayName(String displayName)
   {
      if (displayName == null || displayName.length() == 0)
         throw new IllegalArgumentException("displayName must not be null or empty");

      return (String)m_AllowedValuesOptionMap.get(displayName);
   }

   /**
    * Does this <code>PSItemFieldMeta</code> have options?
    *
    * @return <code>true</code> if there are options, otherwise
    * <code>false</code>.
    */
   public boolean hasOptions()
   {
      return m_uiDef.getChoices() == null ? false : true;
   }

   /**
    * @see PSItemComponent#toXml(Document, PSAcceptElements)
    */
   protected Element toXml(Document doc, PSAcceptElements acceptElements)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = createStandardItemElement(doc, EL_FIELD_META);

      // required:
      root.setAttribute(ATTR_NAME, getName());

      if (getFieldValueType() != FIELD_VALUE_TYPE_UNKNOWN)
         root.setAttribute(
            ATTR_FIELD_VALUE_TYPE,
            PSField.FIELD_VALUE_TYPE_ENUM[getFieldValueType()]);

      if (m_mimeType != null)
         root.setAttribute(ATTR_MIME_TYPE, m_mimeType);

      if (getSourceType() != SOURCE_TYPE_UNKNOWN)
         root.setAttribute(
            ATTR_SOURCE_TYPE,
            PSField.TYPE_ENUM[getSourceType()]);

      root.setAttribute(
         ATTR_TRANSFER_ENCODING,
         ENCODING_TYPE_ENUM[m_transferEncoding]);

      if (getDisplayName() != null)
         root.setAttribute(ATTR_DISPLAY_NAME, getDisplayName());

      root.setAttribute(
         ATTR_SHOW_IN_PREVIEW,
         (showInPreview() == true ? "true" : "false"));

      if (!m_AllowedValuesOptionMap.isEmpty())
         root.appendChild(createOptionElements(doc));

      return root;
   }

   /**
    * Convenience method to <code>toXml(doc, null)</code>.
    * @see #toXml(Document, PSAcceptElements)
    */
   public Element toXml(Document doc)
   {
      return toXml(doc, null);
   }

   /**
    * Create ValueChoices and Options elements.
    * @param doc document on which to create.  Assumed not <code>null</code>
    * @return ValueChoices
    */
   private Element createOptionElements(Document doc)
   {
      Element choices = createStandardItemElement(doc, EL_VALUE_CHOICES);

      Set keySet = m_AllowedValuesOptionMap.keySet();
      Iterator it = keySet.iterator();
      Element option = null;
      Text valueTextNode = null;
      String displayName = null;
      String value = null;
      while (it.hasNext())
      {
         displayName = (String)it.next();
         if (displayName == null)
            continue;
         value = (String)m_AllowedValuesOptionMap.get(displayName);

         option = createStandardItemElement(doc, EL_OPTION);
         option.setAttribute(ATTR_DISPLAY_NAME, displayName);

         valueTextNode = doc.createTextNode(value);
         option.appendChild(valueTextNode);

         choices.appendChild(option);
      }
      return choices;
   }

   //see interface for description
   public Object clone()
   {
      PSItemFieldMeta copy = null;

      copy = (PSItemFieldMeta)super.clone();

      // do not clone defs:
      copy.m_fieldDef = m_fieldDef;
      copy.m_uiDef = m_uiDef;

      copy.init();

      if (m_AllowedValuesOptionMap != null)
      {
         Iterator i = m_AllowedValuesOptionMap.keySet().iterator();
         while (i.hasNext())
         {
            Object key = i.next();
            String val = (String)m_AllowedValuesOptionMap.get(key);
            copy.addOptions((String)key, (String)val);
         }
      }
      return copy;
   }

   //see interface for description
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSItemFieldMeta compMeta = (PSItemFieldMeta)obj;

      // checking references:
      if (!compare(m_AllowedValuesOptionMap,
         compMeta.m_AllowedValuesOptionMap))
         return false;
      if (!compare(m_fieldDef, compMeta.m_fieldDef))
         return false;
      if (!compare(m_uiDef, compMeta.m_uiDef))
         return false;

      // check values:
      if (!compare(getDisplayName(), compMeta.getDisplayName()))
         return false;
      if (!compare(m_mimeType, compMeta.m_mimeType))
         return false;
      if (!compare(getName(), compMeta.getName()))
         return false;

      // check primitives:
      if (getBackendDataType() != compMeta.getBackendDataType())
         return false;
      if (hasOptions() != compMeta.hasOptions())
         return false;
      if (m_isMultiValueField != compMeta.m_isMultiValueField)
         return false;
      if (showInPreview() != compMeta.showInPreview())
         return false;
      if (getFieldValueType() != compMeta.getFieldValueType())
         return false;
      if (getSourceType() != compMeta.getSourceType())
         return false;
      if (m_transferEncoding != compMeta.m_transferEncoding)
         return false;

      return true;
   }

   //see interface for description
   public int hashCode()
   {
      int hash = 0;

      // super is abtract, don't call
      hash += hashBuilder(m_AllowedValuesOptionMap);
      hash += hashBuilder(m_fieldDef);
      hash += hashBuilder(m_uiDef);
      hash += hashBuilder(m_mimeType);
      hash += hashBuilder(m_isMultiValueField);
      hash += m_transferEncoding;

      return hash;
   }

   // @see IPSDataComponent
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      loadXmlData(sourceNode, true);
   }

   /**
    * This method is called to populate an object from its XML representation.
    * It assumes that the object may already have a complete data structure,
    * therefore method only overlays the data onto the existing object.
    * An element node may contain a hierarchical structure, including child
    * objects. The element node can also be a child of another element node.
    * <p>
    * @param sourceNode   the XML element node from which to populate.  Must not
    * be <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by this class.
    */
   public void loadXmlData(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      loadXmlData(sourceNode, false);
   }

   void loadXmlData(Element sourceNode, boolean clearValues)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // validate the root element
      PSXMLDomUtil.checkNode(sourceNode, EL_FIELD_META);
   }

   /**
    * Returns the unique name of the field.
    *
    * @return name of the field.  Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_fieldDef.getSubmitName();
   }

   /**
    * Is this a multi value field?
    *
    * @return <code>true</code> if it is.
    */
   public boolean isMultiValueField()
   {
      return m_isMultiValueField;
   }

   /**
    * // TODO: Implement for treat as xml.
    *  Returns the mime type of this field.
    *
    * @return the mime type of this field.
    *
   public String getMimeType()
   {
      return m_mimeType;
   }
   */

   /**
    * The source type of the field.  Will be one of the following types:
    *
    * <ul>
    * <li>{@link PSItemFieldMeta#SOURCE_TYPE_UNKNOWN} - default
    * </li>
    * <li>{@link PSItemFieldMeta#SOURCE_TYPE_LOCAL}
    * </li>
    * <li>{@link PSItemFieldMeta#SOURCE_TYPE_SHARE}
    * </li>
    * <li>{@link PSItemFieldMeta#SOURCE_TYPE_SYSTEM}
    * </ul>
    * @return valid source value.
    */
   public int getSourceType()
   {
      return m_fieldDef.getType();
   }

   /**
    * Gets the transfer encoding type.  Will be one of the following types:
    *
    * <ul>
    * <li>{@link PSItemFieldMeta#ENCODING_TYPE_NONE} - default.  No encoding
    * specified.
    * </li>
    * <li>{@link PSItemFieldMeta#ENCODING_TYPE_BASE64} - base64 encodes the
    * field value before transfering.
    * </ul>
    *
    * @return transfer encoding of this item. Wwill not be <code>null</code> or
    *  empty.
    */
   public int getTransferEncoding()
   {
      return m_transferEncoding;
   }

   /**
    * Sets the transfer encoding type.  Must be one of the following types:
    *
    * <ul>
    * <li>{@link PSItemFieldMeta#ENCODING_TYPE_NONE} - default.  No encoding
    * specified.
    * </li>
    * <li>{@link PSItemFieldMeta#ENCODING_TYPE_BASE64} - base64 encodes the
    * field value before transfering.
    * </ul>
    *
    * @param encodingType must be a valid type.
    */
   public void setTransferEncoding(int encodingType)
   {
      if (encodingType != ENCODING_TYPE_BASE64
         || encodingType != ENCODING_TYPE_NONE)
         throw new IllegalArgumentException("invalid argument");

      m_transferEncoding = encodingType;
   }

   /**
    * The field value type of the field.  Will be one of the following types:
    *
    * <ul>
    * <li>{@link PSItemFieldMeta#FIELD_VALUE_TYPE_UNKNOWN} - default.
    * </li>
    * <li>{@link PSItemFieldMeta#FIELD_VALUE_TYPE_CONTENT}
    * </li>
    * <li>{@link PSItemFieldMeta#FIELD_VALUE_TYPE_META}
    * </ul>
    * @return valid source value.
    */
   public int getFieldValueType()
   {
      return m_fieldDef.getFieldValueType();

   }
   
   /**
    * The definitions for this fieldmeta, set in the ctor, invariant and never
    * <code>null</code>
    */
   public PSField getFieldDef()
   {
      return m_fieldDef;
   }
   
   /**
    * Returns the display name for this field.  A display name is not required.
    *
    * @return display name may be <code>null</code> never empty.
    */
   public String getDisplayName()
   {
      String displayName = null;

      // not null and not empty:
      if (m_uiDef.getLabel() != null
         && m_uiDef.getLabel().getText().trim().length() != 0)
         displayName = m_uiDef.getLabel().getText();

      return displayName;
   }

   /**
    * Should we display this field value in preview?
    *
    * @return <code>true</code> the field value will be displayed in preview
    * mode in the CMS, <code>false</code> it will not be.  Default is
    * <code>false</code>.
    */
   public boolean showInPreview()
   {
      return m_fieldDef.isShowInPreview();
   }

   /**
    * Is this either a binary field, or a treat as binary field?
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>.
    */
   public boolean isBinary()
   {
      return m_fieldDef.isForceBinary() || getBackendDataType() == 
         DATATYPE_BINARY;
   }

   /** todo whem treat as xml is implemented use this:
    * Is this field treat as xml?
    *
    * @return <code>true</code> if it is, otherwise <code>false</code>.
    *
   public boolean isXmlValue()
   {
      // todo: if the mime type = xml then true,
      // this should work just like isBinary, the value will be in the def.
      // and we just proxy
      return false;
   }
   */

   /**
    * Gets the name of this fieldmeta element.
    *
    * @param el the element to retrieve the name from, must not be <code>
    * null</code>
    *
    * @return the name of this field meta element
    *
    * @throws PSUnknownNodeTypeException
    */
   static String getName(Element el) throws PSUnknownNodeTypeException
   {
      return el.getAttribute(ATTR_NAME);
   }

   /**
    * Set by the ctor, default is false, is invariant.
    */
   private boolean m_isMultiValueField;

   /**
    * The mime type of the field, get from def, //TODO: when UI is completed
    * for Treat As XML, accessors should be made for this field:
    */
   private String m_mimeType;

   /**
    * Specifies how to encode the values of the field.
    * TODO: does this have a default?
    */
   private int m_transferEncoding = ENCODING_TYPE_NONE;

   /**
   * The Map of allowed values.  The displayName is the <code>key</code>
   * and the #TEXT child of the Option element is the <code>value</code>,
   * initialized in <code>init()</code>, never <code>null</code> may be
   * empty.
   */
   private Map m_AllowedValuesOptionMap;

   /**
    * The definitions for this fieldmeta, set in the ctor, invariant and never
    * <code>null</code>
    */
   private PSField m_fieldDef;

   /**
    * The ui definitions for this fieldmeta, set in the ctor, invariant and
    * never <code>null</code>
    */
   private PSUISet m_uiDef;

   /** data type values */
   public final static int DATATYPE_DATE = 0;
   public final static int DATATYPE_TEXT = 1;
   public final static int DATATYPE_NUMERIC = 2;
   public final static int DATATYPE_BINARY = 3;

   public final static int ENCODING_TYPE_NONE = 0;
   // todo uncomment when treat as xml is implemented.
   //   public final static int ENCODING_TYPE_XML_ESCAPED = 1;
   public final static int ENCODING_TYPE_BASE64 = 1;

   /**
    * An array of XML attribute values for the field value type.
    * They are specified at the index of the specifier.
    */
   public static final String[] ENCODING_TYPE_ENUM = { "none", "base64" };

   /** "local", "shared" or "system" */
   public static final int SOURCE_TYPE_UNKNOWN = PSField.TYPE_UNKNOWN;
   public static final int SOURCE_TYPE_SYSTEM = PSField.TYPE_SYSTEM;
   public static final int SOURCE_TYPE_SHARE = PSField.TYPE_SHARED;
   public static final int SOURCE_TYPE_LOCAL = PSField.TYPE_LOCAL;

   /** field value types */
   public static final int FIELD_VALUE_TYPE_UNKNOWN =
      PSField.FIELD_VALUE_TYPE_UNKNOWN;

   public static final int FIELD_VALUE_TYPE_CONTENT =
      PSField.FIELD_VALUE_TYPE_CONTENT;

   public static final int FIELD_VALUE_TYPE_META =
      PSField.FIELD_VALUE_TYPE_META;

   /** Name of the elements in this class' XML representation */
   public static final String EL_FIELD_META = "FieldMeta";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_FIELD_VALUE_TYPE = "fieldValueType";
   public static final String ATTR_MIME_TYPE = "mimeType";
   public static final String ATTR_SOURCE_TYPE = "sourceType";
   public static final String ATTR_TRANSFER_ENCODING = "transferEncoding";
   public static final String ATTR_DISPLAY_NAME = "displayName";
   public static final String ATTR_SHOW_IN_PREVIEW = "showInPreview";

   public static final String EL_VALUE_CHOICES = "ValueChoices";
   public static final String EL_OPTION = "Option";

}
