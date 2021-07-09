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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a single column used be a given display format.
 */
public class PSDisplayColumn extends PSDbComponent
      implements IPSSequencedComponent
{
   /**
    * ctor, see base for description, sets up this objects
    * key definition, and initializes any class attributes.
    */
   public PSDisplayColumn()
   {
      this(createKey(null, 0, false));
   }

   /**
    * Creates an instance with a supplied key.
    * 
    * @param key the key of the created object, never <code>null</code>.
    */
   public PSDisplayColumn(PSKey key)
   {
      // set up the key
      super(key);
      // allows for creation of dummy
      m_strSource = key.getPart(KEY_COL_SOURCE);
      setDisplayName("Column 1");
   }
   
   /**
    * Required ctor that takes a node suitable for {@link #fromXml}
    */
   public PSDisplayColumn(Element src)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(src);
   }
   
   /**
    * Creates the correct key for this component.
    * 
    * @param sourceId the source id, may be <code>null</code> or empty if 
    *     creating an empty key.
    * @param displayId the (parent) display id.
    * @param persisted <code>true</code> if the object is persisted in the
    *     repository.
    */
   public static PSKey createKey(String sourceId, long displayId, 
         boolean persisted)
   {
      PSKey key = null;
      String[] keyDef = new String[] {KEY_COL_SOURCE, KEY_COL_ID};
      if (sourceId == null || sourceId.trim().length() == 0)
      {
         key = new PSKey(keyDef);
      }
      else
      {
         String[] keyVal = new String[] {sourceId, String.valueOf(displayId)};
         key = new PSKey(keyDef, keyVal, persisted);
      }

      return key;
   }

   /**
    * Convience ctor.
    *
    * @param strName. The fieldName. Never <code>null</code> or empty.
    *
    * @param strLabel. The field label. May be <code>null</code> or empty,
    *    if so, this defaults to <code>strName</code>.
    *
    * @param groupingType. One of the GROUPING_xxx constants.
    *
    * @param strRenderType. The render type. May be <code>null</code> or empty.
    *    if so, will default to type text.
    *    See description <code>m_strRenderType</code> for allowable values.
    *
    * @param strDesc. The description. May be <code>null</code> or empty.
    *
    * @param isAscendingSort The sort order.
    *
    */
   public PSDisplayColumn(String strSource, String strLabel, int groupingType,
      String strRenderType, String strDesc, boolean isAscendingSort)
   {
      this();

      if (strSource == null || strSource.trim().length() == 0)
         throw new IllegalArgumentException(
            "source name must not be null or empty");

      // Validate source length we set it explicitly below
      if (strSource.length() > SOURCE_LENGTH)
         throw new IllegalArgumentException(
            "source must not exceed " + SOURCE_LENGTH +
            " characters");

      // Handle defaults
      if (strLabel == null || strLabel.trim().length() == 0)
         strLabel = strSource;

      setSortOrder(isAscendingSort);

      // strName is part of the primary key, so it must be immutable
      m_strSource = strSource;

      setDescription(strDesc);
      setGroupingType(groupingType);
      setRenderType(strRenderType);
      setDisplayName(strLabel);
   }

   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getSource()};
   }

   // see base class for description
   public void fromXml(Element e)
      throws PSUnknownNodeTypeException
   {
      // Base class fromXml and validation
      super.fromXml(e);

      //save state so we can restore when finished w/ updates
      int state = getState();

      m_strSource = PSXMLDomUtil.checkAttribute(e, NAME_ATTR, true);

      setPosition(PSXMLDomUtil.checkAttributeInt(e, SEQUENCE_ATTR, false));

      String label = PSXMLDomUtil.checkAttribute(e, DISPLAYNAME_ATTR, false);
      if (label.length() == 0)
         label = m_strSource;
      m_strDisplayName = label;

      String grouping = PSXMLDomUtil.checkAttribute(e, GROUPING_ATTR, false);
      int groupingType = 0;
      for (; groupingType < GROUPING_ENUM.length; groupingType++)
      {
         if (GROUPING_ENUM[groupingType].equalsIgnoreCase(grouping))
            break;
      }
      if (groupingType == GROUPING_ENUM.length)
         groupingType = GROUPING_FLAT;
      m_groupingType = groupingType;

      String sortOrder = PSXMLDomUtil.checkAttribute(e, SORTORDER_ATTR, false);
      m_width = PSXMLDomUtil.checkAttributeInt(e, WIDTH_ATTR, false);
      
      m_isAscendingSort = sortOrder.equalsIgnoreCase(SORTORDER_VALUE_ASC);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(e);

      Element typeRenderEl = tree.getNextElement(XML_NODE_RENDERTYPE);
      tree.setCurrent(e);

      if (typeRenderEl == null)
      {
         setRenderType(DATATYPE_TEXT);
      }
      else
      {
         setRenderType(tree.getElementData(typeRenderEl));
      }

      Element descEl = tree.getNextElement(XML_NODE_DESCRIPTION);
      tree.setCurrent(e);

      if (descEl != null)
      {
         setDescription(tree.getElementData(descEl));
      }

      //set state to what was in the source element
      setState(state);
   }

   /**
    * <pre><code>
    * &gt;!ELEMENT getNodeName() (getLocator().getNodeName(), Description,
    *       RenderType)&lt;
    * &gt;!ATTLIST getNodeName()
    *    state CDATA #IMPLIED
    *    sequence CDATA #IMPLIED
    *    displayName CDATA #REQUIRED
    *    type CDATA #REQUIRED
    *    sortOrder (ascending|descending) "ascending"
    *    width CDATA #IMPLIED
    *    &lt;
    * &gt;!ELEMENT Description (#PCDATA)&lt;
    * &gt;!ELEMENT RenderType (#PCDATA)&lt;
    * </code></pre>
    *
    * See base class for further details.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException(
            "doc may not be null");

      Element root = super.toXml(doc);
      root.setAttribute(NAME_ATTR, m_strSource);
      root.setAttribute(SEQUENCE_ATTR, ""+m_headerSequence);
      root.setAttribute(DISPLAYNAME_ATTR, m_strDisplayName);
      root.setAttribute(GROUPING_ATTR, GROUPING_ENUM[m_groupingType]);
      root.setAttribute(SORTORDER_ATTR,
            isAscendingSort() ? SORTORDER_VALUE_ASC : SORTORDER_VALUE_DESC);
      root.setAttribute(WIDTH_ATTR, String.valueOf(m_width));
      
      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DESCRIPTION, 
         m_strDescription);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_RENDERTYPE, 
         getRenderType());

      return root;
   }

   /**
    * This value allows the UI engine to determine how the associated data
    * should be displayed.
    *
    * @return One of the DATATYPE_xxx values.
    */
   public String getRenderType()
   {
      return m_strRenderType;
   }
   
   /**
    * Is the column type text?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isTextType()
   {
      return getRenderType().equalsIgnoreCase(DATATYPE_TEXT);
   }
   
   /**
    * Is the column type number?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isNumberType()
   {
      return getRenderType().equalsIgnoreCase(DATATYPE_NUMBER);
   }
   
   /**
    * Is the column type date?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDateType()
   {
      return getRenderType().equalsIgnoreCase(DATATYPE_DATE);
   }
   
   /**
    * Is the column type image?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isImageType()
   {
      return getRenderType().equalsIgnoreCase(DATATYPE_IMAGE);
   }

   /**
    * See {@link #getRenderType()} for details.
    *
    * @param One of the DATATYPE_xxx values. If <code>null</code> or empty
    *    supplied, DATATYPE_TEXT is used.
    */
   public void setRenderType(String str)
   {
      // threshold - if null or empty default to text value
      if (str == null || str.trim().length() == 0)
         str = DATATYPE_TEXT;

      // Validate length
      if (str.length() > RENDERTYPE_LENGTH)
         throw new IllegalArgumentException(
            "render type must not exceed " + RENDERTYPE_LENGTH +
            " characters");

      // Threshold
      if (m_strRenderType.equalsIgnoreCase(str))
         return;

      String [] container = new String []
         {
            DATATYPE_TEXT,
            DATATYPE_NUMBER,
            DATATYPE_DATE,
            DATATYPE_IMAGE
         };

      boolean bFound = false;

      for (int i=0; i<container.length; i++)
      {
         if (container[i].equalsIgnoreCase(str))
         {
            bFound = true;
            break;
         }
      }

      if (!bFound)
         throw new IllegalArgumentException(
            str + " is not a valid render type for a PSDisplayColumn");

      setDirty();
      m_strRenderType = str;
   }

   /**
    * Gets the diplay id that is a parent of this column.
    *
    * @return string never <code>null</code> or empty.
    */
   public String getDisplayId()
   {
      // return from the key
      return getLocator().getPart(KEY_COL_ID);
   }

   /**
    * Get the source id attribute of this object.
    *
    * @return string never <code>null</code> or empty.
    */
   public String getSource()
   {
      return m_strSource;
   }

   /**
    * Gets the display name of column.
    *
    * @return string never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_strDisplayName;
   }

   /**
    * Sets the display name of the object.
    *
    * @param str never <code>null</code> or empty.
    */
   public void setDisplayName(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "display name must not be null or empty");

      // Validate length
      if (str.length() > DISPLAYNAME_LENGTH)
         throw new IllegalArgumentException(
            "display name must not exceed " + DISPLAYNAME_LENGTH +
            " characters");

      // Threshold
      if (str.equalsIgnoreCase(m_strDisplayName))
         return;

      setDirty();
      m_strDisplayName = str;
   }

   /**
    * Get the description attribute of this object
    *
    * @return string never <code>null</code>, but may be empty.
    */
   public String getDescription()
   {
      return m_strDescription;
   }

   /**
    * Sets the description attribute of this object.
    *
    * @param str may be <code>null</code> to set empty.
    *
    */
   public void setDescription(String str)
   {
      if (str == null)
         str = "";

      // Validate length
      if (str.length() > DESCRIPTION_LENGTH)
         throw new IllegalArgumentException(
            "description must not exceed " + DESCRIPTION_LENGTH +
            " characters");

      // Threshold
      if (str.equalsIgnoreCase(m_strDescription))
         return;

      setDirty();
      m_strDescription = str;
   }

   /**
    * A column can either be 'flat' or categorized. A categorized column allows
    * rows w/ the same value for this property to be grouped together. This
    * is usually represented by a 'virtual' folder in a UI. Non-categorized
    * columns are used in the list view of the UI.
    *
    * @return <code>true</code> if this column has been defined to be
    *    displayed as a category, <code>false</code> if it is a list header.
    */
   public boolean isCategorized()
   {
      return m_groupingType == GROUPING_CATEGORY;
   }


   /**
    * The position of this column relative to other columns being
    * displayed. Columns are sequenced from left to right, with the first
    * index being 0. Defaults to 0. The order of columns that have the same
    * sequence value is implmenentation dependent.
    *
    * @return A value >= 0.
    */
   public int getPosition()
   {
      return m_headerSequence;
   }

   /**
    * See {@link #getPosition()} for details.
    *
    * @param pos Any value is allowed. If a value < 0 is supplied, 0 is used.
    */
   public void setPosition(int pos)
   {
      if (pos < 0)
         pos = 0;
      if (m_headerSequence == pos)
         return;
      m_headerSequence = pos;
      setDirty();
   }

   /**
    * Each column has 0 or more rows associated with it. This value specifies
    * what the default ordering should be. Defaults to <code>true</code>.
    *
    * @return <code>true</code> if ascending, otherwise
    *    <code>false</code>
    */
   public boolean isAscendingSort()
   {
      return m_isAscendingSort;
   }

   /**
    * Opposite of {@link #isAscendingSort()}.
    *
    * @return <code>true</code> if descending, otherwise
    *    <code>false</code>
    */
   public boolean isDescendingSort()
   {
      return !isAscendingSort();
   }

   /**
    * See {@link #isCategorized()} for details. Determines whether this col
    * is categorized or flat.
    *
    * @param groupingType Must be one of the GROUPING_xxx constants.
    */
   public void setGroupingType(int groupingType)
   {
      if (groupingType >= GROUPING_ENUM.length || groupingType < 0)
         throw new IllegalArgumentException("Invalid type supplied.");

      // Threshold
      if (m_groupingType == groupingType)
         return;

      setDirty();
      m_groupingType = groupingType;
   }


   /**
    * See {@link #isAscendingSort()} for details.
    *
    * @param <code>true</code> if you wish the default sorting to be ascending,
    *    <code>false</code> will set the default to descending.
    */
   public void setSortOrder(boolean isAscending)
   {
      // Threshold
      if (m_isAscendingSort == isAscending)
         return;

      setDirty();
      m_isAscendingSort = isAscending;
   }

   /**
    * Get the width to use to when this column is displayed.
    * 
    * @return The width, greater than zero if specified, -1 if no width has
    * been specified.
    */
   public int getWidth()
   {
      return m_width;
   }
   
   /**
    * Sets the width of this column.  See {@link #getWidth()} for more info.
    * 
    * @param width The width to use, must be greater than zero, or -1 to clear
    * the width. 
    */
   public void setWidth(int width)
   {
      if (width <= 0 && width != -1)
         throw new IllegalArgumentException("invalid column width");
      
      if (width == m_width)
         return;
      
      m_width = width;
      setDirty();
   }

   /**
    * Because the field name is immutable, this method will not transfer that
    * property. See base class for further details.
    */
   public void copyFrom(IPSDbComponent src)
   {
      // Threshold - base class handling
      if (null == src || !getClass().isInstance(src))
         throw new IllegalArgumentException(
            "src must be a " + getClass().getName());

      PSDisplayColumn other = (PSDisplayColumn) src;

      // setters handle dirty flagging
      setDescription(other.getDescription());
      setDisplayName(other.getDisplayName());
      setRenderType(other.getRenderType());
      setSortOrder(other.isAscendingSort());
      setGroupingType(other.isCategorized() ? GROUPING_CATEGORY : GROUPING_FLAT);
      setPosition(other.getPosition());
      m_width = other.m_width;
      //don't copy immutable members, namely the name
   }

   // see base class for description
   public boolean equals(Object obj)
   {
      // Base class crack at it.
      if (!super.equals(obj))
         return false;

      PSDisplayColumn col2 = (PSDisplayColumn) obj;

      return m_strDescription.equalsIgnoreCase(col2.m_strDescription)
         && m_strDisplayName.equalsIgnoreCase(col2.m_strDisplayName)
         && m_strRenderType.equalsIgnoreCase(col2.m_strRenderType)
         && (isAscendingSort() == col2.isAscendingSort())
         && m_strSource.equals(col2.m_strSource)
         && (isCategorized() == col2.isCategorized())
         && (getPosition() == col2.getPosition()
         && (m_width == col2.m_width));
   }

   /* we don't need to override clone because all our props are immutable or
      native types */

   // see base class for description
   public int hashCode()
   {
      // Base class crack at it.
      int nHash = super.hashCode();

      return nHash +
         (m_strDescription
         + m_strDisplayName
         + m_groupingType
         + m_isAscendingSort
         + m_headerSequence
         + m_strSource
         + m_strRenderType
         + m_width).hashCode();
   }

   // Private internal defines
   private static final String KEY_COL_ID = "DISPLAYID";
   private static final String KEY_COL_SOURCE = "SOURCE";

   // public static defines
   public static final String NAME_ATTR = "name";
   public static final String DISPLAYNAME_ATTR = "displayName";
   public static final String XML_NODE_RENDERTYPE = "RenderType";

   public static final String SORTORDER_ATTR = "sortOrder";
   public static final String SORTORDER_VALUE_ASC = "ascending";
   public static final String SORTORDER_VALUE_DESC = "descending";

   public static final String GROUPING_ATTR = "groupingType";
   public static final int GROUPING_FLAT = 0;
   public static final int GROUPING_CATEGORY = 1;
   
   public static final String GROUPING_FLAT_NAME = "flat";
   public static final String GROUPING_CATEGORY_NAME = "categorized";
   /**
    * Names for each of the GROUPING_xxx numeric types. The type is used as an
    * index into this array to find its name.
    */
   public static final String[] GROUPING_ENUM =
   {GROUPING_FLAT_NAME, GROUPING_CATEGORY_NAME};

   public static final String XML_NODE_DESCRIPTION = "Description";
   public static final String SEQUENCE_ATTR = "sequence";
   private static final String WIDTH_ATTR = "width";

   /**
    * Description of column, initialized in definition, never <code>null</code>
    * but may be empty.
    */
   private String m_strDescription = "";

   /**
    * See {@link #getPosition()} for details. Always >= 0.
    */
   private int m_headerSequence = 0;

   /**
    * Display name of column, initialized in definition, never <code>null</code>
    * and never empty.
    */
   private String m_strDisplayName = "";

   /**
    * Defaults to <code>true</code>.
    */
   private boolean m_isAscendingSort = true;

   /**
    * Source name, initialized in ctor, not modified after that,
    * part of the primary key. Never <code>null</code> or empty
    */
   private String m_strSource;


   /**
    * See {@link #isCategorized()} for details. One of the GROUPING_xxx values.
    * Defaults to GROUPING_FLAT.
    */
   private int m_groupingType = GROUPING_FLAT;
   
   /**
    * Width the ui should use to display this column.  See {@link #getWidth()}
    * for details.  Initially -1, modified by {@link #setWidth(int)}.
    */
   private int m_width = -1;

   //NOTE: All render types must be less than RENDERTYPE_LENGTH in char count
   /**
    * A hint to the UI that the data associated with this column is a plain
    * text string.
    */
   public static final String DATATYPE_TEXT = PSSearchField.TYPE_TEXT;

   /**
    * A hint to the UI that the data associated with this column is numeric
    * and may need to be rendered differently depending on the locale.
    */
   public static final String DATATYPE_NUMBER = PSSearchField.TYPE_NUMBER;

   /**
    * A hint to the UI that the data associated with this column is a date,
    * and may need to be rendered differently depending on the locale. The
    * date is always returned from this object in ISO8601 format, i.e.,
    * yyyyMMdd HH:mm:ss, where HH is in 24 hour time. The time part is only
    * returned if it is not all 0's. See SimpleDataFormat for more details on
    * format pattern.
    */
   public static final String DATATYPE_DATE = PSSearchField.TYPE_DATE;

   /**
    * When this data type is specified, the engine expects to find a url as
    * the value. It will query the data from the url and display as an image
    * within the column. Only small images should be supplied for this data
    * type.
    */
   public static final String DATATYPE_IMAGE = "Image";

   public static final int SOURCE_LENGTH = 128;
   public static final int DISPLAYNAME_LENGTH = 128;
   public static final int RENDERTYPE_LENGTH = 50;
   public static final int DESCRIPTION_LENGTH = 255;
   public static final int SORTORDER_LENGTH = 50;

   /**
    * See {@link #getRenderType()} for details. Defaults to DATATYPE_TEXT.
    */
   private String m_strRenderType = DATATYPE_TEXT;
}

