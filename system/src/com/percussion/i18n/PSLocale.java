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

package com.percussion.i18n;

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.i18n.rxlt.PSLocaleHandler;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the object representation of a Locale defintion.
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, region = "PSLocale")
@Table(name = "RXLOCALE")
public class PSLocale implements IPSCatalogSummary 
{

   /**
    * Construct this object from its member data.
    * @param languageString The language string that identifies this locale. May
    * not be <code>null</code> or empty.
    * @param displayName The display name of the locale, may not be 
    * <code>null</code> or empty.
    * @param description A description of this locale, may be <code>null</code> 
    * or empty.
    * @param status One of the <code>STATUS_xxx</code> values.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSLocale(String languageString, String displayName, 
      String description, int status)
   {
      if (languageString == null || languageString.trim().length() == 0)
         throw new IllegalArgumentException(
            "languageString may not be null or empty");

      if (displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName may not be null or empty");
      
      if (!validateStatus(status))
         throw new IllegalArgumentException("invalid status");
      
      m_languageString = languageString;
      m_displayName = displayName;
      m_description = description;
      m_status = status;
   }

   /**
    * No arg constructor used for Hibernate
    */
   public PSLocale()
   {
      
   }
   
   /**
    * Construct this object from its XML representation.  
    * 
    * @param source The source element, may not be <code>null</code>, must 
    * conform to the format defined by {@link #toXml(Document)}.
    * 
    * @throws IllegalArgumentException if <code>source</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>source</code> does not match
    * the expected format.
    */
   public PSLocale(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      fromXml(source);
   }

   /**
    * Construct this object from its DB representation.  Package private since 
    * <code>rowData</code> is coupled to the database.
    * 
    * @param rowData The table data for the row this object represents.  May not
    * be <code>null</code> and must contain the columns expected by this object.
    * Columns expected are:
    * <table>
    * <tr>
    * <th>Column</th><th>Description</th><th>Nullable</th>
    * <td>LANGUAGESTRING</td><td>The language string that identifies this 
    *    locale, may not be empty.</td><td>No</td>
    * <td>DISPLAYNAME</td><td>The display name of the locale, may not be 
    *    empty.</td><td>No</td>
    * <td>DESCRIPTION</td><td>The description of this local, may be empty.</td>
    *    <td>Yes</td>
    * <td>STATUS</td><td>One of the <code>STATUS_xxx</code> values</td>
    * <td>No</td>
    * </tr>
    * </table>
    * Other columns may be provided without error, but will be ignored.
    * 
    * @throws IllegalArgumentException if <code>rowData</code> is 
    * <code>null</code>.
    * @throws PSDataExtractionException if <code>rowData</code> does not contain
    * the expected columns and data.
    */
   PSLocale(PSJdbcRowData rowData) throws PSDataExtractionException
   {
      if (rowData == null)
         throw new IllegalArgumentException("rowData may not be null");
      
      m_languageString = getColumnValue(rowData, PSLocaleHandler.COL_LANGUAGE_STRING, 
         true);
      m_displayName = getColumnValue(rowData, PSLocaleHandler.COL_DISPLAY_NAME, 
         true);
      m_description = getColumnValue(rowData, PSLocaleHandler.COL_DESCRIPTION, false);
      String status = getColumnValue(rowData, PSLocaleHandler.COL_STATUS, true);
      
      m_status = STATUS_UNDEFINED;
      try
      { 
         m_status = Integer.parseInt(status);
      }
      catch (NumberFormatException e){}
      
      if (!validateStatus(m_status))
      {
         Object[] args = {COL_STATUS, status == null ? "null" : status};
         throw new PSDataExtractionException(
            IPSLocaleErrors.INVALID_COLUMN_VALUE, args);
      }
      
   }

   /**
    * Get the unique identifier for this locale.
    * 
    * @return The language string, never <code>null</code> or empty.
    */
   @Basic
   public String getLanguageString()
   {
      return m_languageString;
   }
   
   

   /**
    * @param lang The lang to set.
    */
   public void setLanguageString(String lang)
   {
      m_languageString = lang;
   }
   /**
    * Get the display name of this locale.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   @Basic
   public String getDisplayName()
   {
      return m_displayName;
   }
   
   /**
    * @param dispName The dispName to set.
    */
   public void setDisplayName(String dispName)
   {
      m_displayName = dispName;
   }
   
   /**
    * @return Returns the localeId.
    */
   public int getLocaleId()
   {
      return m_localeId;
   }
   /**
    * @param localeId The localeId to set.
    */
   public void setLocaleId(int localeId)
   {
      m_localeId = localeId;
   }
   /**
    * Get the optional description of this locale.
    * 
    * @return The description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return m_description;
   }
   
   

   /**
    * @param desc The desc to set.
    */
   public void setDescription(String desc)
   {
      m_description = desc;
   }
   /**
    * Get the status of this locale.
    * 
    * @return The status, one of the <code>STATUS_XXX</code> values.
    */
   public int getStatus()
   {
      return m_status;
   }
   
   /**
    * @param status The status to set.
    */
   public void setStatus(int status)
   {
      m_status = status;
   }
   /**
    * Serializes this object's state to its XML representation.  Format is:
    * <pre><code>
    *    &lt;ELEMENT PSXLocale (Description?)>
    *    &lt;ATTLIST PSXLocale 
    *       languageString CDATA #REQUIRED
    *       displayName CDATA #REQUIRED
    *       status CDATA #REQUIRED
    *    >
    *    &lt;ELEMENT Description (#PCDATA)>
    * </code></pre>
    * 
    * @param doc The document to use when serializing the XML, may not be 
    * <code>null</code>.
    * 
    * @return The root element of the XML representation, never 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>doc</code> is 
    * <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ATTR_LANGUAGE_STRING, m_languageString);
      root.setAttribute(ATTR_DISPLAY_NAME, m_displayName);
      root.setAttribute(ATTR_STATUS, STATUS_ENUM[m_status]);
      if (m_version != null)
         root.setAttribute(ATTR_VERSION, m_version.toString());
      
      if (m_description != null)
         PSXmlDocumentBuilder.addElement(doc, root, EL_DESCRIPTION, 
            m_description);
      
      return root;
   }

   /**
    * Restores this object from its XML representation.  
    * 
    * @param source The root element of this object's XML representation. See 
    * {@link #toXml(Document)} for the expected format.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>source</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>source</code> does not match
    * the expected format.
    */
   public void fromXml(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      m_languageString = getRequiredAttribute(source, ATTR_LANGUAGE_STRING);
      m_displayName = getRequiredAttribute(source, ATTR_DISPLAY_NAME);
      String status = getRequiredAttribute(source, ATTR_STATUS);
      m_status = STATUS_UNDEFINED;
      for (int i = 0; i < STATUS_ENUM.length; i++) 
      {
         if (STATUS_ENUM[i].equalsIgnoreCase(status))
         {
            m_status = i;
            break;
         }
      }
      
            
      if (!validateStatus(m_status))
      {
         Object[] args = {source.getTagName(), ATTR_STATUS, status};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      m_version = null;
      int version = PSXMLDomUtil.checkAttributeInt(source, ATTR_VERSION, false);
      if (version != -1)
         m_version = version;
      
      m_description = null;
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element descEl = tree.getNextElement(EL_DESCRIPTION, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (descEl != null)
      {
         m_description = PSXmlTreeWalker.getElementData(descEl);
      }
   }
   
   /**
    * Gets the value of the specified column from the supplied row data.
    * 
    * @param rowData The data from which the column value is extracted, assumed 
    * not <code>null</code>.
    * @param colName The name of the column, assumed not <code>null</code> or 
    * empty. 
    * @param required <code>true</code> to require the value not be 
    * <code>null</code> or empty, <code>false</code> otherwise.
    * 
    * @return The value, may be empty.  May be <code>null</code> only if 
    * <code>required</code> is <code>false</code>.
    * 
    * @throws PSDataExtractionException if the specified column cannot be found
    * in the supplied row data, or if <code>requried</code> is <code>true</code>
    * and the column value is <code>null</code> or empty.
    */
   private String getColumnValue(PSJdbcRowData rowData, String colName, 
      boolean required) throws PSDataExtractionException
   {
      PSJdbcColumnData col;
      String val = null;
      col = rowData.getColumn(colName);
      if (col == null)
         throw new PSDataExtractionException(IPSLocaleErrors.MISSING_COLUMN, 
            colName);
      
      val = col.getValue();
      if (required && (val == null || val.trim().length() == 0))
      {
         Object[] args = {colName, val == null ? "null" : val};
         throw new PSDataExtractionException(
            IPSLocaleErrors.INVALID_COLUMN_VALUE, args);
      }
      
      return val;
   }
   
   /**
    * Check that the supplied status is one of the <code>STATUS_XXX</code> 
    * values.
    * 
    * @param status The status to validate.
    * 
    * @return <code>true</code> if the status is valid, <code>false</code> 
    * otherwise.
    */
   private boolean validateStatus(int status)
   {
      return (status > STATUS_UNDEFINED && status < STATUS_ENUM.length);
   }

   /**
    * Utility method to get a required attibute value, validating that it is
    * not <code>null</code> or empty.
    *
    * @param source Element to get the attribute from, assumed not 
    * <code>null</code>.
    * @param attName The name of the attribute to get, assumed not 
    * <code>null</code> or empty
    *
    * @return The attribute value, never <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the specified attribute cannot be
    * found with a non-empty value.
    */
   public static String getRequiredAttribute(Element source, String attName)
      throws PSUnknownNodeTypeException
   {
      String val = source.getAttribute(attName);
      if (val == null || val.trim().length() == 0)
      {
         Object[] args = {source.getTagName(), attName, "empty"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      return val;
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }
   
   /**
    * Determines if this object is equal to another.  See 
    * {@link java.lang.Object#equals(Object) Object.equals()} for more info.
    */
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }
   
   /**
    * Implementation of {@link IPSCatalogSummary#getGUID()}, constructs and
    * returns a guid from the locale id.
    */
   @IPSXmlSerialization(suppress=true)
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.LOCALE, m_localeId);
   }

   /**
    * Implementation of {@link IPSCatalogSummary#getName()}, returns the 
    * language string.
    */
   public String getName()
   {
      return m_languageString;
   }

   /**
    * Implementation of {@link IPSCatalogSummary#getLabel()}, returns the 
    * display name.
    */
   public String getLabel()
   {
      return m_displayName;
   }   
   
   /**
    * Set the version.  There are only limited cases where this needs to be 
    * used, such as with web services.
    * 
    * @param version The new version, may not be <code>null</code>.
    */
   public void setVersion(Integer version)
   {
      if (version == null)
         throw new IllegalArgumentException("version may not be null");
      
      if (version < 0)
         throw new IllegalArgumentException("version must be >= 0");
      
      m_version = version;   
   }
   
   /**
    * Get the version.  There are only limited cases where this needs to be 
    * used, such as with web services.
    * 
    * @return The version, may be <code>null</code> if it has not been set.
    */
   @IPSXmlSerialization(suppress=true)
   public Integer getVersion()
   {
      return m_version;
   }      
   
   /**
    * Constant to indicate locale's status is inactive.
    */
   public static final int STATUS_INACTIVE = 0;
   
   /**
    * Constant to indicate locale's status is active.
    */
   public static final int STATUS_ACTIVE = 1;
   
   /**
    * Enumeration of status strings, uses the <code>STATUS_xxx</code> constant
    * value as an index into the array to retrieve its corresponding string
    * representation.  This must be maintained if a new public status constant 
    * is added.
    */
   public static final String[] STATUS_ENUM = {"inactive", "active"};
   
   /**
    * Constant for the name of the root element used to serialize this object
    *  to and from its XML format.
    */
   public static final String XML_NODE_NAME = "PSXLocale";
   
   
   /**
    * The language string used to uniquely identify this locale, never 
    * <code>null</code>, empty, or modified after construction.
    */
   @Basic
   @Column(name="LANGUAGESTRING")
   private String m_languageString;
   
   /**
    * The display name of this locale, never <code>null</code>, empty, or 
    * modified after construction.
    */
   @Basic
   @Column(name="DISPLAYNAME")
   private String m_displayName;
   
   /**
    * The optional description of this locale, may be <code>null</code> or 
    * empty, set during construction, never modified after that.
    */
   @Basic
   @Column(name="DESCRIPTION")
   private String m_description;
   
   /**
    * The database id
    */
   @Id
   @Column(name="LOCALEID")
   private int m_localeId;
   
   /**
    * The status of this locale, one of the <code>STATUS_XXX</code> values.
    * Initialized to {@link #STATUS_UNDEFINED}, set to a valid value during 
    * construction, never modified after that.
    */
   @Basic
   @Column(name="STATUS")
   private int m_status = STATUS_UNDEFINED;
   
   @Version
   @Column(name = "VERSION")
   private Integer m_version = null;
   

   /**
    * Constant to inidcate the status is not yet defined.
    */
   private static final int STATUS_UNDEFINED = -1;
   
   // private constants to specify element and attributes when constructing this
   //object from XML
   private static final String ATTR_LANGUAGE_STRING = "languageString";
   private static final String ATTR_DISPLAY_NAME = "displayName";
   private static final String EL_DESCRIPTION = "Description";
   private static final String ATTR_STATUS = "status";
   private static final String ATTR_VERSION = "version";
   
   // private constants to specify column names expected in the row data when
   // constructing from the repository
   private static final String COL_STATUS = "STATUS";
}
