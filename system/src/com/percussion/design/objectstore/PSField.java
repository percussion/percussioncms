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

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation for the PSXField DTD in BasicObjects.dtd.
 *
 * A field element contains all non UI related data required to define a
 * field, including business rules. All DataLocator types except FieldRef
 * are supported. If the DataLocator of a field specifies something other
 * than PSXBackEndColumn, this field cannot be updated, it's for query only.
 */
@SuppressWarnings("unchecked")
public class PSField extends PSComponent
{
   /**
    * 
    */
   private static final long serialVersionUID = -1837223721972561274L;

   /**
    * One of the possible values returned by {@link #getModificationType()}.
    * Means that this field is read only (computed) and is not persisted to
    * the database. If the field was submitted, it would be ignored.
    */
   public static final int MODTYPE_NONE = 0;

   /**
    * One of the possible values returned by {@link #getModificationType()}.
    * Means that this field can be modified during an modification request
    * by the submitter. This is the default.
    */
   public static final int MODTYPE_USER = 1;

   /**
    * One of the possible values returned by {@link #getModificationType()}.
    * Means that this field can be modified by the user only when the item is
    * submitted. It cannot be changed after that.
    */
   public static final int MODTYPE_USERCREATE = 2;

   /**
    * One of the possible values returned by {@link #getModificationType()}.
    * Means that this field can only be modified by the server at any time.
    * If the field was submitted, it would be ignored.
    */
   public static final int MODTYPE_SYSTEM = 3;

   /**
    * One of the possible values returned by {@link #getModificationType()}.
    * Means that this field can only be modified by the server when the item
    * is created. If the field was submitted, it would be ignored.
    */
   public static final int MODTYPE_SYSTEMCREATE = 4;

   /**
    * This array contains the text to use as the modificationType attribute
    * of this field's xml form. The MODTYPE_xxx value is an index into this
    * array.
    */
   public static final String[] MODTYPE_ATTR_VALUES =
   {
      "none",
      "user",
      "userCreate",
      "system",
      "systemCreate"

   };



   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains character data.
    */
   public static final String DT_TEXT = "text";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains only a date, no time component is allowed.
    */
   public static final String DT_DATE = "date";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains only a time, no date component is allowed.
    */
   public static final String DT_TIME = "time";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains a date and time (the time is usually 00:00:00 if not
    * supplied).
    */
   public static final String DT_DATETIME = "datetime";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains a value that should be interpreted as either <code>true
    * </code> or Ccode>false</code>.
    */
   public static final String DT_BOOLEAN = "bool";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains a value that should be interpreted as a whole number.
    */
   public static final String DT_INTEGER = "integer";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains a value that should be interpreted as a whole number.
    * 
    * @deprecated use {@link #DT_INTEGER} instead.
    */
   public static final String DT_NUMBER = "number";

   
   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains a value that should be interpreted as a real number.
    */
   public static final String DT_FLOAT = "float";

   /**
    * One of the indentifiers for the allowed Data types. Indicates that the
    * field contains non human readable data.
    */
   public static final String DT_BINARY = "binary";

   /**
    * A special case of the {@link #DT_BINARY} type which indicates that the
    * field content is some sort of picture, such as gif, tiff or jpg. 
    */
   public static final String DT_IMAGE = "image";
   
   /**
    * Data format value indicating that field can contain maximum
    * amount of data allowed. Used as value with "data format" property.
    */
   public static final String MAX_FORMAT = "max";

   /**
    * Creates a new field for the provided name and data locator.
    *
    * @param name the field name, not <code>null</code> or empty. Must be
    *    unique within this field set.
    * @param locator the data locator, may be <code>null</code>.
    */
   public PSField(String name, IPSBackEndMapping locator)
   {
      setSubmitName(name);
      setLocator(locator);
   }

   /**
    * Creates a new field for the provided DataLocator.
    *
    * @param type the field type to create, must be one of TYPE_SYSTEM |
    *    TYPE_SHARED | TYPE_LOCAL.
    * @param name the field name, not <code>null</code> or empty. Must be
    *    unique within this field set.
    * @param locator the data locator for this field, may be <code>null</code>.
    */
   public PSField(int type, String name, IPSBackEndMapping locator)
   {
      setType(type);
      setSubmitName(name);
      setLocator(locator);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSField(Element sourceNode, IPSDocument parentDoc,
                  ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSField()
   {
   }

   /**
    * See {@link #getSearchProperties() 
    * getSearchProperties().isUserSearchable()}.
    */
   public boolean isUserSearchable()
   {
      return m_searchProps.isUserSearchable();
   }

   /**
    * See {@link #getSearchProperties() 
    * getSearchProperties().setUserSearchable(boolean)}.
    */
   public void setUserSearchable(boolean searchable)
   {
       m_searchProps.setUserSearchable(searchable);
   }   

   /**
    * See {@link #getSearchProperties() 
    * getSearchProperties().isUserCustomizable()}.
    * 
    * @deprecated Use {@link #getSearchProperties()}.isUserCustomizable().
    */
   public boolean isUserCustomizable()
   {
      return m_searchProps.isUserCustomizable();
   }

   /**
    * See {@link #getSearchProperties() 
    * getSearchProperties().getDefaultSearchLabel()}.
    * 
    * @deprecated Use {@link #getSearchProperties()}.getDefaultSearchLabel().
    */
   public String getDefaultSearchLabel()
   {
      return m_searchProps.getDefaultSearchLabel();
   }
   
   /**
    * Indicates where the field originated, in other words whether it was
    * originally defined in the system def, shared def or local def.
    *
    * @return One of the TYPE_xxx values (except TYPE_ENUM).
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * This indicates whether the data stored in this field is interpreted as
    * 'content' or 'meta data'.
    *
    * @return One of the FIELD_VALUE_TYPE_xxx values.
    */
   public int getFieldValueType()
   {
      return m_fieldValueType;
   }

   /**
    * Translates a field value type code into a string suitable for writing to 
    * the output document. 
    *
    * @param fieldValueType One of the FIELD_VALUE_TYPE_XXX types.
    *
    * @return The string to use as the attribute value. One of the values in
    * the PSField.FIELD_VALUE_TYPE_ENUM[] array. Never empty. 
    */
   public String getFieldValueTypeText()
   {
      String value;
      switch ( m_fieldValueType )
      {
         case FIELD_VALUE_TYPE_UNKNOWN:
            value = FIELD_VALUE_TYPE_ENUM[FIELD_VALUE_TYPE_UNKNOWN];
            break;

         case FIELD_VALUE_TYPE_CONTENT:
            value = FIELD_VALUE_TYPE_ENUM[FIELD_VALUE_TYPE_CONTENT];
            break;

         case FIELD_VALUE_TYPE_META:
            value = FIELD_VALUE_TYPE_ENUM[FIELD_VALUE_TYPE_META];
            break;

         default:
            value = FIELD_VALUE_TYPE_ENUM[FIELD_VALUE_TYPE_UNKNOWN];
      }

      return value;
   }

   /**
    * See {@link #getType()}.
    *
    * @param type the field type to set, must be one of TYPE_SYSTEM |
    *    TYPE_SHARED | TYPE_LOCAL.
    */
   public void setType(int type)
   {
      if (type != TYPE_SYSTEM && type != TYPE_SHARED && type != TYPE_LOCAL)
         throw new IllegalArgumentException("unsupported type " + type);

      m_type = type;
   }

   /**
    * See {@link #getFieldValueType()}.
    *
    * @param fieldValueType the field value type to set, must be one of
    *    FIELD_VALUE_TYPE_CONTENT | FIELD_VALUE_TYPE_META.
    */
   public void setFieldValueType(int fieldValueType)
   {
      if (fieldValueType != FIELD_VALUE_TYPE_CONTENT &&
            fieldValueType != FIELD_VALUE_TYPE_META)
         throw new IllegalArgumentException("unsupported field value type");

      m_fieldValueType = fieldValueType;
   }


   /**
    * Indicates who can update the field and when it may be updated. It can
    * either be modified by the server or the user and it can either be
    * modified when the item is inserted only (then never changed) or during
    * any modification request.
    *
    * @return One of the MODTYPE_xxx values. Defaults to MODTYPE_USER.
    */
   public int getModificationType()
   {
      return m_modificationType;
   }

   /**
    * Indicates whether this field is one of the MODTYPE_SYSTEMxxx values.
    *
    * @return <code>true</code> if getModificationType() would return either
    *    MODTYPE_SYSTEM or MODTYPE_SYSTEMCREATE, <code>false</code> otherwise.
    */
   public boolean isSystemModified()
   {
      return (getModificationType() == MODTYPE_SYSTEM)
               || (getModificationType() == MODTYPE_SYSTEMCREATE);
   }

   /**
    * Indicates whether this field is one of the MODTYPE_USERxxx values.
    *
    * @return <code>true</code> if getModificationType() would return either
    *    MODTYPE_USER or MODTYPE_USERCREATE, <code>false</code> otherwise.
    */
   public boolean isUserModified()
   {
      return (getModificationType() == MODTYPE_USER)
               || (getModificationType() == MODTYPE_USERCREATE);
   }

   /**
    * Convenience method to get the status of whether this is a system field
    * or not.
    *
    * @return <code>true</code> if this is a system field, <code>false</code>
    *    otherwise.
    */
   public boolean isSystemField()
   {
      return getType() == TYPE_SYSTEM;
   }

   /**
    * Convenience method to get the status of whether this is a shared field
    * or not.
    *
    * @return <code>true</code> if this is a shared field, <code>false</code>
    *    otherwise.
    */
   public boolean isSharedField()
   {
      return getType() == TYPE_SHARED;
   }

   /**
    * Convenience method to get the status of whether this is a local field
    * or not.
    *
    * @return <code>true</code> if this is a local field, <code>false</code>
    *    otherwise.
    */
   public boolean isLocalField()
   {
      return getType() == TYPE_LOCAL;
   }

   /**
    * Get the submit name.
    *
    * @return the name for this field as it will be submitted in the
    *    URL request, never <code>null</code> or empty.
    */
   public String getSubmitName()
   {
      return m_submitName;
   }

   /**
    * @return submitName, never <code>null</code>
   */
   @Override
   public String toString()
   {
      return m_submitName;
   }

   /**
    * Set the submit name.
    *
    * @param submitName the new name, not <code>null</code> or empty.
    */
   public void setSubmitName(String submitName)
   {
      if (submitName == null || submitName.trim().length() == 0)
         throw new IllegalArgumentException("the name cannot be null or empty");

      m_submitName = submitName;
   }

   /**
    * Get the mime type.
    *
    * @return the mime type for this field as it will be submitted in the
    *    URL request, may be <code>null</code> never empty.
    */
   public String getMimeType()
   {
      return m_mimeType;
   }

   /**
    * Set the mime type.
    *
    * @param mimeType the new type, may be <code>null</code> or empty. If
    *    empty, the value is set to <code>null</code>.
    */
   public void setMimeType(String mimeType)
   {
      if (null != mimeType && mimeType.trim().length() == 0)
         m_mimeType = null;
      else
         m_mimeType = mimeType;
   }

   /**
    * Show this in summary.
    *
    * @return <code>true</code> to show this in the summary view,
    *    <code>false</code> otherwise.
    */
   public boolean isShowInSummary()
   {
      return m_showInSummary;
   }

   /**
    * Set show this in summary.
    *
    * @param showInSummary <code>true</code> to show this in summary,
    *    <code>false</code> otherwise.
    */
   public void setShowInSummary(boolean showInSummary)
   {
      m_showInSummary = showInSummary;
   }
   
   /**
    * Is this field mandatory for the system to work propertly?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isSystemMandatory()
   {
      return m_systemMandatory;
   }

   
   /**
    * Is this field intended for internal system use only?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isSystemInternal()
   {
      return m_systemInternal;
   }   
   /**
    * Get the value for the specified boolean property.
    *
    * @param name the property name, may not be <code>null</code> or empty.
    * 
    * @return the boolean value for the requested property. Returns also
    *    <code>false</code> if the property is not found.
    * @throws ClassCastException if the requested property is not of type
    *    <code>Boolean</code>.
    */
   public boolean getBooleanProperty(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSProperty property = (PSProperty) m_properties.get(name);
      if (property != null)
      {
         if (((Boolean) property.getValue()).booleanValue())
            return true;
      }

      return false;
   }

   /**
    * Set a new value for the addressed property. If the property does not
    * exist this will create and add a new one of type boolean.
    *
    * @param enable <code>true</code> to enable the property, <code>false</code>
    *    otherwise.
    * @param name the property name, assumed not <code>null</code> or empty.
    * @throws ClassCastException if the requested property is not of type
    *    <code>Boolean</code>.
    */
   private void setBooleanProperty(boolean enable, String name)
   {
      String value = enable ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1];

      PSProperty property = (PSProperty) m_properties.get(name);
      if (property == null)
      {
         property =
            new PSProperty(name, PSProperty.TYPE_BOOLEAN, value, true, null);

         m_properties.put(name, property);
      }
      else
      {
         if (property.getType() != PSProperty.TYPE_BOOLEAN)
            throw new ClassCastException(
               "the property is not of type PSProperty.TYPE_BOOLEAN");

         property.setValue(value);
      }
   }

   /**
    * Can this field have inline links? Defaults to <code>false</code> if the
    * property is not specified. If this flag is set, this field will be
    * processed for inline links on each modify request.
    *
    * @return <code>true</code> if it can, <code>false</code> otherwise.
    */
   public boolean mayHaveInlineLinks()
   {
      return getBooleanProperty(MAY_HAVE_INLINE_LINKS_PROPERTY);
   }

   /**
    * Set whether this field can have inline links or not.
    *
    * @param enable <code>true</code> if it can have inline links,
    *    <code>false</code> otherwise.
    */
   public void setMayHaveInlineLinks(boolean enable)
   {
      setBooleanProperty(enable, MAY_HAVE_INLINE_LINKS_PROPERTY);
   }

   /**
    * Should broken inline liks be cleaned up? Defaults to <code>false</code>
    * if the property is not specified.
    *
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean cleanupBrokenInlineLinks()
   {
      return getBooleanProperty(CLEANUP_BROKEN_INLINE_LINKS_PROPERTY);
   }

   /**
    * Set whether broken inline links should be cleaned up for this field.
    *
    * @param enable <code>true</code> to cleanup broken inline links,
    *    <code>false</code> otherwise.
    */
   public void setCleanupBrokenInlineLinks(boolean enable)
   {
      setBooleanProperty(enable, CLEANUP_BROKEN_INLINE_LINKS_PROPERTY);
   }

   /**
    * Should this field be cleaned before each modify? Defaults to
    * <code>false</code> if the property is not specified.
    *
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean shouldCleanupField()
   {
      return getBooleanProperty(CLEANUP_PROPERTY);
   }

   /**
    * Set whether this field is cleaned up before each modify.
    *
    * @param enable <code>true</code> to cleanup, <code>false</code> otherwise.
    */
   public void setShouldCleanupField(boolean enable)
   {
      setBooleanProperty(enable, CLEANUP_PROPERTY);
   }
   
   /**
    * indicates to text cleanup and the assembly system that namespaces should 
    * be selectively removed according to a configuration.
    *
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean isCleanupNamespaces()
   {
      return getBooleanProperty(CLEANUP_NAMESPACES_PROPERTY);
   }
   
   /**
    * Set whether this field should have namespaces cleaned up on submittal
    *
    * @param enable <code>true</code> to cleanup, <code>false</code> otherwise.
    */
   public void setCleanupNamespaces(boolean enable)
   {
      setBooleanProperty(enable, CLEANUP_NAMESPACES_PROPERTY);
   }
   
   /**
    * This tells text cleanup that JSP and ASP tags 
    * may appear in the content. These are escaped by inclosing them in 
    * processing instructions (PIs). These PIs have the name 
    * &quot;psx-activetag&quot;. 
    * The assembly system will selectively remove PIs from around these 
    * tags. It does this if the PIs name is &quot;psx-activetag&quot;. Note that 
    * PHP uses processing instructions already, and will therefore pass 
    * through the assembler untouched.
    *
    * @return <code>true</code> if it should, <code>false</code> otherwise.
    */
   public boolean isAllowActiveTags()
   {
      return getBooleanProperty(ALLOW_ACTIVE_TAGS_PROPERTY);
   }
   
   /**
    * Set whether this field allows JSP/ASP tags.
    *
    * @param enable <code>true</code> to cleanup, <code>false</code> otherwise.
    */
   public void setAllowActiveTags(boolean enable)
   {
      setBooleanProperty(enable, ALLOW_ACTIVE_TAGS_PROPERTY);
   }
   
   /**
    * Get the namespaces that should be defined for this field. These are used
    * in text cleanup to define all allowed namespaces for the field. In the
    * assembly engine, these namespaces may be filtered down to those allowed
    * on a particular site.
    * 
    * @return an array of trimmed namespace names or <code>null</code> if none
    * are defined
    */
   public String[] getDeclaredNamespaces()
   {
      PSProperty allowed = getProperty(DECLARED_NAMESPACES);
      Object allowedvalue = allowed != null ? allowed.getValue() : null;
      String allowedstr = allowedvalue != null ? allowedvalue.toString() : null; 
         
      if (StringUtils.isBlank(allowedstr))
         return null;
      
      String ns[] = allowedstr.split(",");
      
      // Trim
      for(int i = 0; i < ns.length; i++)
      {
         ns[i] = ns[i].trim();
      }
      
      return ns.length > 0 ? ns : null;
   }
   
   
   /**
    * Set the declared namespaces property. 
    * 
    * @param namespaces the namespaces, may be <code>null</code> or empty
    */
   public void setDeclaredNamespaces(String[] namespaces)
   {
      StringBuilder b = new StringBuilder(40);
      if (namespaces != null)
      {
         for(String ns : namespaces)
         {
            if (b.length() > 0)
            {
               b.append(',');
            }
            b.append(ns);
         }
      }
      String val = b.toString();
      PSProperty prop = getProperty(DECLARED_NAMESPACES);
      if (StringUtils.isBlank(val))
      {
         if (prop != null)
         {
            prop.setValue(null);
         }
      }
      else
      {
         if (prop == null)
         {
            prop = new PSProperty(DECLARED_NAMESPACES, PSProperty.TYPE_STRING,
                  val, false, "");
         }
         else
         {
            prop.setValue(val);
         }

      }
      if(prop != null)
         setProperty(prop);
   }

   /**
    * Get the file including the path relative to the Rhythmyx root for the
    * cleanup properties.
    *
    * @return the cleanup properties file, never <code>null</code>
    *    or empty, defaults to 'rxW2Ktidy.properties'. All slashes normalized
    *    to forward slash.
    */
   public String getCleanupPropertiesFile()
   {
      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_PROPERTIES_PROPERTY);
      if (property != null)
         return (String) property.getValue();

      return "rxW2Ktidy.properties";
   }

   /**
    * Set the file relative to the Rhythmyx root for the cleanup properties.
    *
    * @param file the new file, not <code>null</code> or empty. Must be a valid
    *    file within the Rhythmyx root.
    */
   public void setCleanupPropertiesFile(String file)
   {
      if (file == null || file.trim().length() == 0)
         throw new IllegalArgumentException("file cannot be null or empty");

      File test = new File(file);
      if (test.exists())
         throw new IllegalArgumentException("file must exist");

      // normalize all slashes to forward slashes
      file = file.replace('\\', '/');

      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_PROPERTIES_PROPERTY);
      if (property == null)
      {
         property = new PSProperty(CLEANUP_PROPERTIES_PROPERTY,
            PSProperty.TYPE_STRING, file, true, null);

         m_properties.put(CLEANUP_PROPERTIES_PROPERTY, property);
      }
      else
         property.setValue(file);
   }

   /**
    * Get the file relative to the Rhythmyx root to the cleanup serverPageTags.
    *
    * @return the path to the cleanup serverPageTags file, never
    *    <code>null</code> or empty, defaults to 'rxW2KserverPageTags.xml'.
    *    All slashes normalized to forward slash.
    */
   public String getCleanupServerPageTagFile()
   {
      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_SERVERPAGETAGS_PROPERTY);
      if (property != null)
         return (String) property.getValue();

      return "rxW2KserverPageTags.xml";
   }

   /**
    * Set the file relative to the Rhythmyx root for the cleanup serverPageTags.
    *
    * @param file the new file, not <code>null</code> or empty. Must be a valid
    *    file within the Rhythmyx root.
    */
   public void setCleanupServerPageTagFile(String file)
   {
      if (file == null || file.trim().length() == 0)
         throw new IllegalArgumentException("file cannot be null or empty");

      File test = new File(file);
      if (test.exists())
         throw new IllegalArgumentException("file must exist");

      // normalize all slashes to forward slashes
      file = file.replace('\\', '/');

      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_SERVERPAGETAGS_PROPERTY);
      if (property == null)
      {
         property = new PSProperty(CLEANUP_SERVERPAGETAGS_PROPERTY,
            PSProperty.TYPE_STRING, file, true, null);

         m_properties.put(CLEANUP_SERVERPAGETAGS_PROPERTY, property);
      }
      else
         property.setValue(file);
   }

   /**
    * Get the encoding to use for the cleanup process.
    *
    * @return the encoding to use for the cleanup process, never
    *    <code>null</code> or empty. Defaults to 'UTF8'. Encoding string in Java
    *    format.
    */
   public String getCleanupEncoding()
   {
      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_ENCODING_PROPERTY);
      if (property != null)
         return (String) property.getValue();

      return "UTF8";
   }

   /**
    * Set encoding to be used for the cleanup process.
    *
    * @param encoding the new encoding to use for the cleanup process, not
    *    <code>null</code> or empty.
    */
   public void setCleanupEncoding(String encoding)
   {
      if (encoding == null || encoding.trim().length() == 0)
         throw new IllegalArgumentException("encoding cannot be null or empty");

      PSProperty property = (PSProperty) m_properties.get(
         CLEANUP_ENCODING_PROPERTY);
      if (property == null)
      {
         property = new PSProperty(CLEANUP_ENCODING_PROPERTY,
            PSProperty.TYPE_STRING, encoding, true, null);

         m_properties.put(CLEANUP_ENCODING_PROPERTY, property);
      }
      else
         property.setValue(encoding);
   }

   /**
    * Get all field properties.
    *
    * @return all field properties, never <code>null</code>, may be empty.
    *    The actual properties owned by this object are returned, so any
    *    changes will affect this object. They should be treated read-only.
    */
   public PSPropertySet getProperties()
   {
      PSPropertySet propertySet = new PSPropertySet();
      Iterator properties = m_properties.values().iterator();
      while (properties.hasNext())
         propertySet.add(properties.next());

      return propertySet;
   }

   /**
    * Get all field properties available for the supplied defaults or the
    * defaults if not found.
    *
    * @param defaults the properties we are looking for with the default
    *    values if not found, never <code>null</code>, may be empty.
    * @return all field properties, never <code>null</code>, may be empty.
    */
   public PSPropertySet getProperties(PSPropertySet defaults)
   {
      if (defaults == null)
         throw new IllegalArgumentException("defaults cannot be null");

      PSPropertySet propertySet = new PSPropertySet();
      Iterator properties = defaults.iterator();
      while (properties.hasNext())
      {
         PSProperty property = (PSProperty) properties.next();
         PSProperty exists = (PSProperty) m_properties.get(property.getName());
         if (exists != null)
            propertySet.add(exists);
         else
            propertySet.add(new PSProperty(property.getName(),
               property.getType(), property.getValue(), property.isLocked(),
               property.getDescription()));
      }

      return propertySet;
   }

   /**
    * Set all field properties. Replaces the current property set with the
    * supplied one.
    *
    * @param properties the properties to set, may be <code>null</code> or
    *    empty, in which case all properties are cleared;
    */
   public void setProperties(PSPropertySet properties)
   {
      if (properties == null)
         properties = new PSPropertySet();

      m_properties.clear();
      for (int i=0; i<properties.size(); i++)
      {
         PSProperty property = (PSProperty) properties.get(i);
         m_properties.put(property.getName(), property);
      }
   }

   /**
    * Get the requested property. The name is compared case sensitive.
    *
    * @param name the property name, may be <code>null</code> or empty.
    * @return the requested property or <code>null</code> if not found.
    */
   public PSProperty getProperty(String name)
   {
      if (name != null)
         return (PSProperty) m_properties.get(name);

      return null;
   }

   /**
    * Set the supplied property. If the property is found (case sensitive
    * compare is used), it will be replaced, otherwise a new one will be added.
    *
    * @param property the property to set, not <code>null</code>.
    */
   public void setProperty(PSProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property cannot be null");

      m_properties.put(property.getName(), property);
   }

   /**
    * Show this in preview?
    *
    * @return <code>true</code> to show this in preview,
    *    <code>false</code> otherwise.
    */
   public boolean isShowInPreview()
   {
      return m_showInPreview;
   }

   /**
    * Set show this in preview.
    *
    * @param showInPreview <code>true</code> to show this in preview,
    *    <code>false</code> otherwise.
    */
   public void setShowInPreview(boolean showInPreview)
   {
      m_showInPreview = showInPreview;
   }

   /**
    * A read-only field is one that has a locator that is not a PSBackEndColumn,
    * meaning it can never be persisted. It is calculated when the request
    * is processed.
    *
    * @return <code>true</code> to indicate read only,
    *    <code>false</code> otherwise.
    */
   public boolean isReadOnly()
   {
      return !(m_locator instanceof PSBackEndColumn);
   }


   /**
    * Is binary forced?
    *
    * @return <code>true</code> if binary is forced,
    *    <code>false</code> otherwise.
    */
   public boolean isForceBinary()
   {
      return m_forceBinary;
   }

   /**
    * Set force binary.
    *
    * @param forceBinary <code>true</code> to force binary, <code>false</code>
    *    otherwise.
    */
   public void setForceBinary(boolean forceBinary)
   {
      m_forceBinary = forceBinary;
   }
   
   /**
   * @return <code>true</code> if it is exportable,
   *    <code>false</code> otherwise.
   */
   public boolean isExportable()
   {
      return m_exportable;
   }
   
   /**
    * 
    * @param exportable <code>true</code> to exportable, <code>false</code>
    *  otherwise.
    */
   public void setExportable(boolean exportable)
   {
      m_exportable = exportable;
   }

   /**
    * Get the data type of this field.
    *
    * @return One of the DT_xxx types, never <code>null</code>, might be
    *    empty if a type was never set.
    */
   public String getDataType()
   {
      return m_dataType;
   }

   /**
    * The data type of a field gives an indication of how the content should
    * be interpreted.
    *
    * @param dataType the field data type, not <code>null</code>, may be
    *    empty. Otherwise, should be one of the DT_xxx types (the supplied
    *    value is lower-cased before use).
    */
   public void setDataType(String dataType)
   {
      if (dataType == null)
         throw new IllegalArgumentException("the data type cannot be null");

      dataType = dataType.toLowerCase();
      if ( dataType.length() == 0
            || dataType.equals(DT_TEXT)
            || dataType.equals(DT_BOOLEAN)
            || dataType.equals(DT_INTEGER)
            || dataType.equals(DT_FLOAT)
            || dataType.equals(DT_DATETIME)
            || dataType.equals(DT_DATE)
            || dataType.equals(DT_TIME)
            || dataType.equals(DT_BINARY)
            || dataType.equals(DT_IMAGE))
      {
         m_dataType = dataType;
      }
      else if (dataType.equals(DT_NUMBER))
      {
         m_dataType = DT_INTEGER;
         
         String msg = "Converting the deprecated data type \"" + dataType + 
            "\" to \"" + DT_INTEGER + "\" for field '" + m_submitName + "'.";
         ms_logger.warn(msg);
      }
      else
      {
         throw new IllegalArgumentException(
               "the data type (" + dataType
               + ") doesn't match one of the known DT_xxx types");
      }
   }


   /**
    * Compares the current type against the supplied type. If the current type
    * is not set, or is not storable in the supplied type, then the current
    * type will be overwritten. The type is obtained via the {@link
    * #getDataType()} method. It also sets the search properties based on the
    * type and the presence of an external search engine.
    * <p>This method is public so it is accessible to classes outside this
    * package, but it is not meant for general use.
    *
    * @param jdbcType One of the official JDBC data types found in
    *    {@link java.sql.Types SQL Types class}.
    *
    * @param precision For numeric types, this is the number of significant
    *    digits in base 10, for char types, it is the size of the db column,
    *    in chars, for binary types, it is the number of bytes that can be
    *    stored in the db field. Set to -1 to indicate unknown.
    *
    * @param scale Only applicable to numeric types, it specifies how many
    *    decimal places the db colum has. Set to -1 to indicate unknown.
    *
    * @param typeName The db dependent name of the column for this type. May
    *    be <code>null</code> or empty. This is used when trying to determine
    *    if we can perform searches on this field.
    *
    * @param searchEnabled A flag that indicates whether the full text search
    * engine is installed and enabled. This flag affects how the search
    * properties are configured (which are dependent on the data type).
    * 
    * @return <code>true</code> if the data type was changed,
    *    <code>false</code> otherwise.
    */
   public boolean fixupDataType( int jdbcType, int precision, int scale,
         String typeName, boolean searchEnabled )
   {
      String currentType = getDataType();
      String actualType = getActualDataTypeForJdbcType(jdbcType, scale);
      boolean changed = !currentType.equals(actualType);

      switch (jdbcType)
      {
         case Types.NUMERIC:
         case Types.DECIMAL:
            if (scale > 0)
            {
               break;
            }
            // else fall thru
            //CHECKSTYLE:OFF
         case Types.BIGINT:
         case Types.INTEGER:
         case Types.SMALLINT:
         case Types.TINYINT:
            if (changed  && currentType.equals(DT_BOOLEAN))
            {
               changed = false;
            }
            break;
         case Types.DOUBLE:
         case Types.FLOAT:
         case Types.REAL:
            break;
         case Types.DATE:
            break;
         case Types.TIME:
            break;
         case Types.TIMESTAMP:
            if (changed && (currentType.equals(DT_DATE)
                  || currentType.equals(DT_TIME)))
            {
               changed = false;
            }
            break;
         
         case Types.BLOB :
         case Types.BINARY :
         case Types.VARBINARY :
         case Types.LONGVARBINARY :
            if (searchEnabled)
            {
               // don't reset the flags if has already set to true, which
               // could happen to shared (binary) fields.
               if (!m_searchProps.isEnableTransformation()
                     || !m_searchProps.isEnableTransformationLocked())
               {
                  m_searchProps.setEnableTransformation(true);
                  m_searchProps.setEnableTransformationLocked(true);
               }
            }
            else
               // this is the pre version 5.5 behavior
               m_searchProps.setUserSearchable(false);
            if (changed && currentType.equals(DT_IMAGE))
            {
               changed = false;
            }
            break;
         case Types.CLOB :
            if (searchEnabled)
            {
               /*
                * The goal here is to default CLOBs to be transformed (the
                * assumption being that they contain HTML). This means all
                * existing apps will default to enabled rather than having the
                * implementers change all the apps. Once the implementer changes
                * it (meaning it will be from xml), we don't set it any more.
                */
               if (!m_searchProps.isFromXml())
                  m_searchProps.setEnableTransformation(true);
            }
            else
               // this is the pre version 5.5 behavior
               m_searchProps.setUserSearchable(false);
         default:
            // all types except binary can be represented in a text column
            changed = false;
            if ( currentType.length() == 0 || currentType.equals(DT_BINARY))
               changed = true;
            break;
      }

      if (changed)
      {
         setDataType(actualType);
      }
      if (getDataType().equals(DT_TEXT) || getDataType().equals(DT_BINARY))
      {
         final String precisionStr = Integer.toString(precision);
         if (!MAX_FORMAT.equalsIgnoreCase(getDataFormat())
               && !precisionStr.equals(getDataFormat()) && precision > 0)
         {
            setDataFormat(precisionStr);
            changed = true;
         }
      }
      return changed;
   }

   /**
    * Convenient method to get the actual data type corresponding to the JDBC
    * data type.
    * 
    * @param jdbcType One of the official JDBC data types found in
    *           {@link java.sql.Types SQL Types class}.
    * 
    * @param scale Only applicable to numeric types, it specifies how many
    *           decimal places the db colum has. Set to -1 to indicate unknown.
    * 
    * @return The identifier for the supplied jdbcType.
    */
   public static String getActualDataTypeForJdbcType(int jdbcType, int scale)
   {
      String actualType = "";
      switch (jdbcType)
      {
         /*
          * these are tricky because they can represent ints or floats, we
          * assume type int. In general, if Rx uses these types, it is as int.
          */
         case Types.NUMERIC :
         case Types.DECIMAL :
            if (scale > 0)
            {
               actualType = DT_FLOAT;
               break;
            }
         // else fall thru
         // CHECKSTYLE:OFF
         case Types.BIGINT :
         case Types.INTEGER :
         case Types.SMALLINT :
         case Types.TINYINT :
            // CHECKSTYLE:ON
            actualType = DT_INTEGER;
            break;

         case Types.DOUBLE :
         case Types.FLOAT :
         case Types.REAL :
            actualType = DT_FLOAT;
            break;

         case Types.DATE :
            actualType = DT_DATE;
            break;

         case Types.TIME :
            actualType = DT_TIME;
            break;

         case Types.TIMESTAMP :
            actualType = DT_DATETIME;
            break;

         case Types.BLOB :
         case Types.BINARY :
         case Types.VARBINARY :
         case Types.LONGVARBINARY :
            actualType = DT_BINARY;
            break;

         case Types.BIT :
            actualType = DT_BOOLEAN;
            break;

         default :
            // CHECKSTYLE:ON
            // we consider everything else plain text
            actualType = DT_TEXT;
            break;
      }
      return actualType;
   }

   /**
    * Get the data format of this field.
    *
    * @return the field data format, may be <code>null</code>, never empty.
    */
   public String getDataFormat()
   {
      return m_dataFormat;
   }

   /**
    * Set this field's data format.
    *
    * @param dataFormat the field data type, may be <code>null</code>,
    * never empty.
    */
   public void setDataFormat(String dataFormat)
   {
      if (dataFormat != null && dataFormat.trim().length()<=0)
         throw new IllegalArgumentException("the data format cannot be empty");

      m_dataFormat = dataFormat;
   }

   /**
    * See {@link #setSearchProperties(PSSearchProperties)} for details.
    *  
    * @return Never <code>null</code>. A clone is returned so changes to it
    * do not affect this object.
    */
   public PSSearchProperties getSearchProperties()
   {
      return (PSSearchProperties) m_searchProps.clone();
   }

   /**
    * All attributes that control how the search interface works and how 
    * indexing works with this field. The rules surrounding <code>
    * enableTransformation</code> flag are enforced. Therefore, after this
    * method is executed, it is not guaranteed the all settings in the
    * supplied value have been applied to this object.
    *  
    * @param props <code>null</code> restores to default values.
    */
   public void setSearchProperties(PSSearchProperties props)
   {
      if (null == props)
         props = new PSSearchProperties();
      else
         //make local copy because may need to modify it
         props = (PSSearchProperties) props.clone();

      /* What's this locked thing? If the backend column is binary (e.g. BLOB),
       * we require that the transform be performed (and thus locked, which is 
       * done in a fixup method elsewhere in this class). Once it is locked, 
       * it can never be unlocked unless the underlying column type was 
       * changed, which we don't support dynamically (meaning if the col type 
       * changed while this instance was in existence, it would not pick up 
       * those changes). Similarly in the other direction.
       * We can't use isForceBinary() because this can be true for CLOBs.
       */
      boolean locked = m_searchProps.isEnableTransformationLocked();
      boolean enableTransform = m_searchProps.isEnableTransformation();
      if (locked  != props.isEnableTransformationLocked())
      {
         if (locked)
            props.setEnableTransformation(enableTransform);
         props.setEnableTransformationLocked(locked);
      }
      m_searchProps.copyFrom(props);
   }

   /**
    * Get the data locator for this field.
    *
    * @return the data locator, might be <code>null</code>.
    */
   public IPSBackEndMapping getLocator()
   {
      return m_locator;
   }

   /**
    * Set a new data locator for this field.
    *
    * @param locator the new locator, may be <code>null</code>.
    */
   public void setLocator(IPSBackEndMapping locator)
   {
      m_locator = locator;
   }

   /**
    * Get the default value of this field.
    *
    * @return the default value, might be
    *    <code>null</code>.
    */
   public IPSReplacementValue getDefault()
   {
      return m_default;
   }

   /**
    * Set a new default value, set <code>null</code> to clear a current
    * default.
    *
    * @param defaultValue the new default value, set <code>null</code> to
    *    delete.
    */
   public void setDefault(IPSReplacementValue defaultValue)
   {
      m_default = defaultValue;
   }

   /**
    * Return the validation status.
    *
    * @return <code>true</code> if any validation failed,
    *    <code>false</code> otherwise.
    */
   public boolean hasValidationFailed()
   {
      return m_validationFailed;
   }

   /**
    * Set the validation status.
    *
    * @param validationFailed <code>true</code> if any validation failed,
    *    <code>false</code> otherwise.
    */
   public void setValidationFailed(boolean validationFailed)
   {
      m_validationFailed = validationFailed;
   }

   /**
    * Get the occurrence dimension for the specified transition Id.  If the
    * transitionId is <code>null</code>, then the default occurrence dimension
    * is returned.
    *
    * @param transitionId The transition id, may be <code>null</code>.
    *
    * @return the occurrence dimension, one of the OCCURRENCE_DIMENSION_XXX
    * values.
    */
   public int getOccurrenceDimension(Integer transitionId)
   {
      return (getOccurrenceSetting(transitionId)).getOccurrenceDimension();
   }

   /**
    * Set the occurrence dimension.
    *
    * @param occurrenceDimension the new occurrence dimension. Must be one of
    * the OCCURRENCE_DIMENSION_XXX values.
    * @param transitionId The transition id, may be <code>null</code>.
    * @throws PSValidationException if the provided occurrence setting is not
    *    supported.
    */
   public void setOccurrenceDimension(int occurrenceDimension, Integer
      transitionId)
      throws PSValidationException
   {
      (getOccurrenceSetting(transitionId, true)).setOccurrenceDimension(
         occurrenceDimension);
   }

   /**
    * Get the occurrence multi valued type.
    *
    * @param transitionId The transition id, may be <code>null</code>.
    *
    * @return the occurrence multi valued type.
    */
   public int getOccurrenceMultiValuedType(Integer transitionId)
   {
      return (getOccurrenceSetting(transitionId)).getOccurrenceMultiValuedType();
   }

   /**
    * Set the occurrence multi valued type.
    *
    * @param occurrenceMultiValuedType the new occurrence multi valued type.
    * @param transitionId The transition id, may be <code>null</code>.
    * @throws PSValidationException if the provided multi valued type is not
    *    supported.
    */
   public void setOccurrenceMultiValuedType(int occurrenceMultiValuedType,
      Integer transitionId)
      throws PSValidationException
   {
      (getOccurrenceSetting(transitionId, true)).setOccurrenceMultiValuedType(
         occurrenceMultiValuedType);
   }

   /**
    * Get the occurrence delimiter.
    *
    * @param transitionId The transition id, may be <code>null</code>.
    *
    * @return the delimiter used to separate values in a multi-valued field,
    * never <code>null</code> or empty.
    */
   public String getOccurrenceDelimiter(Integer transitionId)
   {
      return (getOccurrenceSetting(transitionId)).getOccurrenceDelimiter();
   }

   /**
    * Set the occurrence delimiter.
    *
    * @param occurrenceDelimiter the occurrence delimiter, not
    *    <code>null</code> or empty.
    * @param transitionId The transition id, may be <code>null</code>.
    */
   public void setOccurrenceDelimiter(String occurrenceDelimiter, Integer
      transitionId)
   {
      (getOccurrenceSetting(transitionId, true)).setOccurrenceDelimiter(
         occurrenceDelimiter);
   }

   /**
    * Get the visibility rules.
    *
    * @return the visibility rules, might be
    *    <code>null</code>.
    */
   public PSVisibilityRules getVisibilityRules()
   {
      return m_visibilityRules;
   }

   /**
    * Set the visibility rules.
    *
    * @param visibilityRules the new visibility rules, set to
    *    <code>null</code> to clear existing rules.
    */
   public void setVisibilityRules(PSVisibilityRules visibilityRules)
   {
      m_visibilityRules = visibilityRules;
      correctVisibilityRules();
   }

   /**
    * Get the field validation rules.
    *
    * @return the field validation rules, might be
    *    <code>null</code>.
    */
   public PSFieldValidationRules getValidationRules()
   {
      return m_validationRules;
   }
   
   /**
    * Check if this field has validation rules defined.
    *
    * @return <code>true</code> if a non-empty set of validation rules is 
    * defined, <code>false</code> otherwise.
    */
   public boolean hasValidationRules()
   {
      return m_validationRules != null && 
         m_validationRules.getRules().hasNext();
   }   

   /**
    * Set the field validation rules.
    *
    * @param validationRules the new field validation rules, set to
    *    <code>null</code> to clear existing rules.
    */
   public void setValidationRules(PSFieldValidationRules validationRules)
   {
      m_validationRules = validationRules;
   }

   /**
    * Get the input translation.
    *
    * @return the input translation, might be <code>null</code>.
    */
   public PSFieldTranslation getInputTranslation()
   {
      return m_inputTranslation;
   }

   /**
    * Set the input translations.
    *
    * @param inputTranslation the new input translation, set to
    *    <code>null</code> to clear an existing translation.
    */
   public void setInputTranslation(PSFieldTranslation inputTranslation)
   {
      m_inputTranslation = inputTranslation;
   }

   /**
    * Get the output translations.
    *
    * @return the output translations, might be
    *    <code>null</code>.
    */
   public PSFieldTranslation getOutputTranslation()
   {
      return m_outputTranslation;
   }

   /**
    * Set the output translation.
    *
    * @param outputTranslation the new output translation, set to
    *    <code>null</code> to clear an existing translation.
    */
   public void setOutputTranslation(PSFieldTranslation outputTranslation)
   {
      m_outputTranslation = outputTranslation;
   }

   /**
    * Get the number of items that must exist.
    *
    * @param transitionId The transition id, may be <code>null</code>.
    *
    * @return the occurrence count, only valid if occurrence dimension is
    *     OCCURRENCE_DIMENSION_COUNT, -1 means not specified.
    */
   public int getOccurrenceCount(Integer transitionId)
   {
      return (getOccurrenceSetting(transitionId)).getOccurrenceCount();
   }

   /**
    * Set a new occurrence count.
    *
    * @param count the new occurrence count.
    * @param transitionId The transition id, may be <code>null</code>.
    */
   public void setOccurrenceCount(int count, Integer transitionId)
   {
      (getOccurrenceSetting(transitionId, true)).setOccurrenceCount(count);
   }

   /**
    * Gets the name of the HTML parameter that controls if this field is to be
    * cleared on an update, or <code>null</code> if this field should not be
    * cleared.  The parameter's name is formed by appending the value
    * of {@link #CLEAR_BINARY_PARAM_SUFFIX} to this PSField's submit name
    * (see {@link #getSubmitName}).
    *
    * @return The name of the parameter (never empty), or <code>null</code>.
    */
   public String getClearBinaryParam()
   {
      if (m_enableClearBinaryParam)
         return getSubmitName()+CLEAR_BINARY_PARAM_SUFFIX;
      else
         return null;
   }

   /**
    * Sets that this PSField wants to enable processing the clear binary
    * parameter.
    * @param binaryParam a flag that indicates wheter or not to enable processing
    * the clear binary parameter.
    */
   public void setClearBinaryParam(boolean binaryParam)
   {
      m_enableClearBinaryParam = binaryParam;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSField, not <code>null</code>.
    */
   public void copyFrom(PSField c)
   {
      super.copyFrom(c);

      setDataType(c.getDataType());
      setDataFormat(c.getDataFormat());
      setDefault(c.getDefault());
      setForceBinary(c.isForceBinary());
      setExportable(c.isExportable());
      setInputTranslation(c.getInputTranslation());
      setLocator(c.getLocator());
      replaceOccurrenceSettings(c);
      setOutputTranslation(c.getOutputTranslation());
      setShowInPreview(c.isShowInPreview());
      setShowInSummary(c.isShowInSummary());
      setSubmitName(c.getSubmitName());
      setType(c.getType());
      setValidationFailed(c.hasValidationFailed());
      setValidationRules(c.getValidationRules());
      setVisibilityRules(c.getVisibilityRules());
      setClearBinaryParam(c.m_enableClearBinaryParam);
      m_searchProps.setUserCustomizable(c.isUserCustomizable());
      m_searchProps.setUserSearchable(c.isUserSearchable());
      m_modificationType = c.getModificationType();
      m_choices = c.m_choices;
      replaceProperties(c);
      setSearchProperties(c.getSearchProperties());
      for(String key : c.getUserPropertyKeys())
      {
         setUserProperty(key, c.getUserProperty(key));
      }
   }

    /**
    * Test if the meta data of the supplied object and this are equal.
    * This is typically used to determine if there is a need to re-create 
    * hibernate classes for a Content Type.
    * 
    * @param o the object in question, it may be null.
    * 
    * @return <code>true</code> if the meta data of the given object and this
    * are equal.
    */
   public boolean equalMetaData(Object o)
   {
      if (!(o instanceof PSField))
         return false;

      PSField t = (PSField) o;

      boolean equal = true;
      if (!compare(m_dataType, t.m_dataType))
         equal = false;
      if (!compare(m_dataFormat, t.m_dataFormat))
         equal = false;
      else if (!compare(m_locator, t.m_locator))
         equal = false;
      else if (m_systemMandatory != t.m_systemMandatory)
         equal = false;
      else if (m_systemInternal != t.m_systemInternal)
         equal = false;
      else if (!compare(m_submitName, t.m_submitName))
         equal = false;
      else if (!compare(m_mimeType, t.m_mimeType))
         equal = false;
      else if (m_type != t.m_type)
         equal = false;
      else if (m_fieldValueType != t.m_fieldValueType)
         equal = false;
      // the isCleanupNamespaces() property is used while creating hibernate
      // session factory for Content Types.
      else if (isCleanupNamespaces() != t.isCleanupNamespaces())
         equal = false;
      
      return equal;
   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSField)) return false;
        if (!super.equals(o)) return false;
        PSField psField = (PSField) o;
        return m_type == psField.m_type &&
                m_fieldValueType == psField.m_fieldValueType &&
                m_modificationType == psField.m_modificationType &&
                m_showInSummary == psField.m_showInSummary &&
                m_showInPreview == psField.m_showInPreview &&
                m_forceBinary == psField.m_forceBinary &&
                m_exportable == psField.m_exportable &&
                m_systemMandatory == psField.m_systemMandatory &&
                m_validationFailed == psField.m_validationFailed &&
                m_enableClearBinaryParam == psField.m_enableClearBinaryParam &&
                m_systemInternal == psField.m_systemInternal &&
                Objects.equals(m_userProps, psField.m_userProps) &&
                m_submitName.equals(psField.m_submitName) &&
                Objects.equals(m_mimeType, psField.m_mimeType) &&
                Objects.equals(m_dataType, psField.m_dataType) &&
                Objects.equals(m_dataFormat, psField.m_dataFormat) &&
                Objects.equals(m_locator, psField.m_locator) &&
                Objects.equals(m_default, psField.m_default) &&
                Objects.equals(m_visibilityRules, psField.m_visibilityRules) &&
                Objects.equals(m_validationRules, psField.m_validationRules) &&
                Objects.equals(m_inputTranslation, psField.m_inputTranslation) &&
                Objects.equals(m_outputTranslation, psField.m_outputTranslation) &&
                Objects.equals(m_occurrenceSettings, psField.m_occurrenceSettings) &&
                Objects.equals(m_choices, psField.m_choices) &&
                Objects.equals(m_properties, psField.m_properties) &&
                Objects.equals(m_searchProps, psField.m_searchProps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), m_userProps, m_type, m_fieldValueType, m_modificationType, m_submitName, m_mimeType, m_showInSummary, m_showInPreview, m_forceBinary, m_exportable, m_systemMandatory, m_dataType, m_dataFormat, m_locator, m_default, m_validationFailed, m_visibilityRules, m_validationRules, m_inputTranslation, m_outputTranslation, m_enableClearBinaryParam, m_occurrenceSettings, m_choices, m_properties, m_searchProps, m_systemInternal);
    }

    /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_userProps.clear();
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         /* this is cleared so that if we don't find a PSXSearchProps elem,
          * it's absence is used to kick in the backward compatibility code
          */
         m_searchProps = null;
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the field name attribute
         m_submitName = tree.getElementData(NAME_ATTR);
         if (m_submitName == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               NAME_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         // OPTIONAL: get the mime type attribute
         setMimeType(tree.getElementData(MIME_TYPE_ATTR));

         // OPTIONAL: get the field showInSummary attribute
         data = tree.getElementData(SHOW_IN_SUMMARY_ATTR);
         if (data != null)
            m_showInSummary =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;
         
         // OPTIONAL: get the field systemMandatory attribute
         data = tree.getElementData(SYSTEM_MANDATORY_ATTR);
         m_systemMandatory = false;
         if (data != null)
            m_systemMandatory = 
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         // OPTIONAL: get the field systemInternal attribute
         data = tree.getElementData(SYSTEM_INTERNAL_ATTR);
         m_systemInternal = false;
         if (data != null)
            m_systemInternal = 
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;
         
         // OPTIONAL: get the field showInPreview attribute
         data = tree.getElementData(SHOW_IN_PREVIEW_ATTR);
         if (data != null)
            m_showInPreview =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         // OPTIONAL: get the field modificationType attribute
         data = tree.getElementData(MODIFICATION_TYPE_ATTR);
         if (data != null)
         {
            for (int i=0; i < MODTYPE_ATTR_VALUES.length; i++)
            {
               if (data.equalsIgnoreCase(MODTYPE_ATTR_VALUES[i]))
                  m_modificationType = i;
            }
         }

         // OPTIONAL: get the field forceBinary attribute
         data = tree.getElementData(FORCE_BINARY_ATTR);
         if (data != null)
            m_forceBinary =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;
         
         data = tree.getElementData(EXPORT_ATTR);
         if (data != null)
            m_exportable = 
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;
 
         // OPTIONAL: get the field clearBinaryParam attribute
         data = tree.getElementData(CLEAR_BINARY_PARAM_ATTR);
         if (data != null && !(data.equalsIgnoreCase("no")))
            setClearBinaryParam(true);

         // OPTIONAL: get the field type attribute
         data = tree.getElementData(FIELD_TYPE_ATTR);
         if (data != null)
         {
            for (int i=0; i<TYPE_ENUM.length; i++)
            {
               if (TYPE_ENUM[i].equalsIgnoreCase(data))
               {
                  setType(i);
                  break;
               }
            }
         }

         // OPTIONAL: get the fieldvalytype attribute
         data = tree.getElementData(FIELD_VALUE_TYPE_ATTR);
         if (data != null)
         {
            for (int i=0; i<FIELD_VALUE_TYPE_ENUM.length; i++)
            {
               if (FIELD_VALUE_TYPE_ENUM[i].equalsIgnoreCase(data))
               {
                  setFieldValueType(i);
                  break;
               }
            }
         }

         // OPTIONAL: get all optional elements
         node = tree.getNextElement(firstFlags);
         while(node != null)
         {
            String elementName = node.getTagName();

            if (elementName.equals(DATA_LOCATOR_ELEM))
            {
               Node current = tree.getCurrent();

               node = tree.getNextElement(true);
               m_locator = (IPSBackEndMapping)
                  PSReplacementValueFactory.getReplacementValueFromXml(
                  parentDoc, parentComponents, node, XML_NODE_NAME,
                  DATA_LOCATOR_ELEM);

               tree.setCurrent(current);
            }
            else if (elementName.equals(DATA_TYPE_ELEM))
            {
               setDataType(PSXmlTreeWalker.getElementData(node));
            }
            else if (elementName.equals(DATA_FORMAT_ELEM))
            {
               m_dataFormat = PSXmlTreeWalker.getElementData(node);

               if(m_dataFormat != null && m_dataFormat.trim().length()<=0)
                  m_dataFormat = null;
            }
            else if (elementName.equals(DEFAULT_VALUE_ELEM))
            {
               // OPTIONAL: get the default value
               Node current = tree.getCurrent();

               node = tree.getNextElement(true);
               if ( !node.getTagName().equals( DATA_LOCATOR_ELEM ))
               {
                  Object[] args =
                  {
                     DEFAULT_VALUE_ELEM,
                     DATA_LOCATOR_ELEM,
                     "null"
                  };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
               node = tree.getNextElement(true);
               m_default = PSReplacementValueFactory.getReplacementValueFromXml(
                     parentDoc, parentComponents, node, XML_NODE_NAME,
                     DATA_LOCATOR_ELEM);

               tree.setCurrent(current);
            }
            else if (elementName.equals(OCCURRENCE_ELEM))
            {
               PSOccurrenceSetting occurrenceSetting =
                  new PSOccurrenceSetting();

               // OPTIONAL: get the occurrence
               data = node.getAttribute(OCCURRENCE_DIMENSION_ATTR);
               if (data != null && data.trim().length() != 0)
               {
                  boolean found = false;
                  for (int i=0; i<OCCURRENCE_DIMENSION_ENUM.length; i++)
                  {
                     if (OCCURRENCE_DIMENSION_ENUM[i].equalsIgnoreCase(data))
                     {
                        try
                        {
                           occurrenceSetting.setOccurrenceDimension(i);
                           found = true;
                        }
                        catch(PSValidationException e)
                        {
                           // let node error handling deal with it.
                        }

                        break;
                     }
                  }
                  if (!found)
                  {
                     Object[] args =
                     {
                        OCCURRENCE_ELEM,
                        OCCURRENCE_DIMENSION_ATTR,
                        data
                     };
                     throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
                  }
               }

               // OPTIONAL: get the count if neccesary
               if (occurrenceSetting.getOccurrenceDimension() ==
                  OCCURRENCE_DIMENSION_COUNT)
               {
                  data = PSXmlTreeWalker.getElementData(node);
                  if (data != null && data.trim().length() > 0)
                  {
                     try
                     {
                        occurrenceSetting.setOccurrenceCount(Integer.parseInt(
                           data));
                     }
                     catch (NumberFormatException e)
                     {
                        Object[] args =
                        {
                           XML_NODE_NAME,
                           OCCURRENCE_ELEM,
                           data
                        };
                        throw new PSUnknownNodeTypeException(
                           IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
                     }
                  }
               }

               // OPTIONAL: get the occurrence multi valued type attribute
               data = node.getAttribute(OCCURRENCE_MULTI_VALUED_TYPE_ATTR);
               if (data != null && data.trim().length() != 0)
               {
                  boolean found = false;
                  for (int i=0; i<OCCURRENCE_MULTI_VALUED_TYPE_ENUM.length; i++)
                  {
                     if (OCCURRENCE_MULTI_VALUED_TYPE_ENUM[i].equalsIgnoreCase(
                        data))
                     {
                        try
                        {
                           occurrenceSetting.setOccurrenceMultiValuedType(i);
                           found = true;
                        }
                        catch (PSValidationException e)
                        {
                           // let node error handling deal with it.
                        }

                        break;
                     }
                  }
                  if (!found)
                  {
                     Object[] args =
                     {
                        OCCURRENCE_ELEM,
                        OCCURRENCE_MULTI_VALUED_TYPE_ATTR,
                        data
                     };
                     throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
                  }
               }

               data = node.getAttribute(OCCURRENCE_DELIMITER_ATTR);
               if (data != null && data.trim().length() != 0)
               {
                  occurrenceSetting.setOccurrenceDelimiter(data);
               }

               Integer transId = null;
               data = node.getAttribute(TRANSITION_ID_ATTR);
               if (data != null && data.trim().length() != 0)
               {
                  try
                  {
                     transId = new Integer(data);
                  }
                  catch (NumberFormatException e)
                  {
                     Object[] args =
                     {
                        OCCURRENCE_ELEM,
                        TRANSITION_ID_ATTR,
                        data
                     };
                     throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
                  }

               }

               m_occurrenceSettings.put(transId, occurrenceSetting);
            }
            else if(elementName.equals(FIELD_RULES_ELEM))
            {
               // OPTIONAL: get the field rules
               Node current = tree.getCurrent();

               node = tree.getNextElement(firstFlags);
               while(node != null)
               {
                  elementName = node.getTagName();
                  if (elementName.equals(PSFieldValidationRules.XML_NODE_NAME))
                     m_validationRules = new PSFieldValidationRules(
                        node, parentDoc, parentComponents);
                  else if (elementName.equals(PSVisibilityRules.XML_NODE_NAME))
                     m_visibilityRules = new PSVisibilityRules(
                        node, parentDoc, parentComponents);
                  else if(elementName.equals(FIELD_INPUT_TRANSLATIONS_ELEM))
                  {
                     // OPTIONAL: get the field input translations
                     Node cur = tree.getCurrent();

                     node = tree.getNextElement(firstFlags);
                     while(node != null)
                     {
                        elementName = node.getTagName();
                        if (elementName.equals(PSFieldTranslation.XML_NODE_NAME))
                           m_inputTranslation = new PSFieldTranslation(
                              node, parentDoc, parentComponents);

                        node = tree.getNextElement(firstFlags);
                     }

                     tree.setCurrent(cur);
                  }
                  else if (elementName.equals(FIELD_OUTPUT_TRANSLATIONS_ELEM))
                  {
                     // OPTIONAL: get the field output translations
                     Node cur = tree.getCurrent();

                     node = tree.getNextElement(firstFlags);
                     while(node != null)
                     {
                        elementName = node.getTagName();
                        if (elementName.equals(PSFieldTranslation.XML_NODE_NAME))
                           m_outputTranslation = new PSFieldTranslation(
                              node, parentDoc, parentComponents);

                        node = tree.getNextElement(firstFlags);
                     }

                     tree.setCurrent(cur);
                  }

                  node = tree.getNextElement(nextFlags);
               }

               tree.setCurrent(current);
            }
            else if(elementName.equals(PSChoices.XML_NODE_NAME))
            {
               m_choices = new PSChoices(node, null, null);
            }
            else if (elementName.equals(PSPropertySet.XML_NODE_NAME))
            {
               m_properties = new HashMap();

               PSPropertySet propertySet = new PSPropertySet(node);
               Iterator properties = propertySet.iterator();
               while (properties.hasNext())
               {
                  PSProperty property = (PSProperty) properties.next();
                  m_properties.put(property.getName(), property);
               }
            }
            else if (elementName.equals(PSSearchProperties.XML_NODE_NAME))
            {
               m_searchProps = new PSSearchProperties(node);
            }

            node = tree.getNextElement(nextFlags);
         }
         if (null == m_searchProps)
         {
            /* support for backwards compatibility - if the new element is not
             * present (it would be set by now if found in the xml), we look 
             * for attributes that used to exist in the root of this element 
             * and use those to build our search props.
             * Because of the way we do it, we don't need to check if a
             * certain feature is supported as we do when writing the node.
             */
            m_searchProps = new PSSearchProperties();
            data = sourceNode.getAttribute(
                  PSSearchProperties.USER_SEARCHABLE_ATTR);
            if (data != null && data.trim().length() > 0)
            {
               m_searchProps.setUserSearchable(
                     data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
            }

            data = sourceNode.getAttribute(
                  PSSearchProperties.USER_CUSTOMIZABLE_ATTR);
            if (data != null && data.trim().length() > 0)
            {
               m_searchProps.setUserCustomizable(
                     data.equalsIgnoreCase(BOOLEAN_ENUM[0]));
            }

            // OPTIONAL
            m_searchProps.setDefaultSearchLabel(sourceNode.getAttribute(
                  PSSearchProperties.DEF_SEARCH_LABEL_ATTR));
         }
      }
      finally
      {
         correctVisibilityRules();
         resetParentList(parentComponents, parentSize);
      }
   }
   
   /**
    * Corrects the visibility rules for system mandatory fields. System 
    * mandatory fields can only be hidden through XSL, never in XML.
    */
   private void correctVisibilityRules()
   {
      if (m_systemMandatory)
      {
         PSVisibilityRules rules = getVisibilityRules();
         if (rules != null)
            rules.setDataHiding(PSVisibilityRules.DATA_HIDING_XSL);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_submitName);
      root.setAttribute(SHOW_IN_SUMMARY_ATTR,
         m_showInSummary ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);
      root.setAttribute(SHOW_IN_PREVIEW_ATTR,
         m_showInPreview ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);
      root.setAttribute(MODIFICATION_TYPE_ATTR,
            MODTYPE_ATTR_VALUES[m_modificationType]);
      root.setAttribute(FORCE_BINARY_ATTR,
         m_forceBinary ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);
      root.setAttribute(EXPORT_ATTR,
         m_exportable ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]); 
      if(null != m_mimeType)
         root.setAttribute(MIME_TYPE_ATTR, m_mimeType);
      if (m_enableClearBinaryParam)
         root.setAttribute(CLEAR_BINARY_PARAM_ATTR, BOOLEAN_ENUM[0]);
      if (m_type != TYPE_UNKNOWN)
         root.setAttribute(FIELD_TYPE_ATTR, TYPE_ENUM[m_type]);
      if (m_fieldValueType != FIELD_VALUE_TYPE_UNKNOWN)
         root.setAttribute(
            FIELD_VALUE_TYPE_ATTR, FIELD_VALUE_TYPE_ENUM[m_fieldValueType]);
      if (isSystemMandatory())
         root.setAttribute(SYSTEM_MANDATORY_ATTR, BOOLEAN_ENUM[0]);
      if (m_systemInternal)
         root.setAttribute(SYSTEM_INTERNAL_ATTR, BOOLEAN_ENUM[0]);      

      Element elem;

      // create the table locator
      if (m_locator != null)
      {
         elem = doc.createElement(DATA_LOCATOR_ELEM);
         elem.appendChild(((IPSComponent) m_locator).toXml(doc));
         root.appendChild(elem);
      }

      // create the datatype
      PSXmlDocumentBuilder.addElement(doc, root, DATA_TYPE_ELEM, m_dataType);

      // create the dataformat
      if (m_dataFormat != null)
      {
         PSXmlDocumentBuilder.addElement(doc, root,
                                         DATA_FORMAT_ELEM, m_dataFormat);
      }

      // create the default value
      if (m_default != null)
      {
         elem = doc.createElement(DEFAULT_VALUE_ELEM);
         Element locElem = doc.createElement(DATA_LOCATOR_ELEM);
         locElem.appendChild(((IPSComponent) m_default).toXml(doc));
         elem.appendChild(locElem);
         root.appendChild(elem);
      }

      // create occurrence settings
      Iterator settings = m_occurrenceSettings.entrySet().iterator();
      while (settings.hasNext())
      {
         Map.Entry entry = (Map.Entry)settings.next();
         Integer transId = (Integer)entry.getKey();
         PSOccurrenceSetting setting = (PSOccurrenceSetting)entry.getValue();

         if (setting.getOccurrenceDimension() == OCCURRENCE_DIMENSION_COUNT)
         {
            elem = PSXmlDocumentBuilder.addElement(
               doc, root, OCCURRENCE_ELEM, Integer.toString(
                  setting.getOccurrenceCount()));
         }
         else
         {
            elem = PSXmlDocumentBuilder.addEmptyElement(
               doc, root, OCCURRENCE_ELEM);
         }

         elem.setAttribute(OCCURRENCE_DIMENSION_ATTR,
            OCCURRENCE_DIMENSION_ENUM[setting.getOccurrenceDimension()]);
         elem.setAttribute(OCCURRENCE_MULTI_VALUED_TYPE_ATTR,
            OCCURRENCE_MULTI_VALUED_TYPE_ENUM[
               setting.getOccurrenceMultiValuedType()]);
         elem.setAttribute(OCCURRENCE_DELIMITER_ATTR,
            setting.getOccurrenceDelimiter());

         if (transId != null)
            elem.setAttribute(TRANSITION_ID_ATTR, transId.toString());
      }

      // create the field rules
      if (m_validationRules != null || m_visibilityRules != null ||
         m_inputTranslation != null || m_outputTranslation != null)
      {
         elem = doc.createElement(FIELD_RULES_ELEM);
         if (m_visibilityRules != null)
            elem.appendChild(m_visibilityRules.toXml(doc));
         if (m_validationRules != null)
            elem.appendChild(m_validationRules.toXml(doc));

         // create the field input translations
         if (m_inputTranslation != null)
         {
            Element el = doc.createElement(FIELD_INPUT_TRANSLATIONS_ELEM);
            if (m_inputTranslation != null)
               el.appendChild(m_inputTranslation.toXml(doc));

            elem.appendChild(el);
         }

         // create the field output translations
         if (m_outputTranslation != null)
         {
            Element el = doc.createElement(FIELD_OUTPUT_TRANSLATIONS_ELEM);
            if (m_outputTranslation != null)
               el.appendChild(m_outputTranslation.toXml(doc));

            elem.appendChild(el);
         }

         root.appendChild(elem);
      }

      // create choices
      if (m_choices != null)
         root.appendChild(m_choices.toXml(doc));


      // create properties
      if (!m_properties.isEmpty())
      {
         PSPropertySet propertySet = new PSPropertySet();
         Iterator properties = m_properties.values().iterator();
         while (properties.hasNext())
         {
            PSProperty property = (PSProperty) properties.next();
            propertySet.add(property);
         }

         root.appendChild(propertySet.toXml(doc));
      }
      
      /* We moved 3 search props that were attributes of PSXField into the
       * PSXSearchProperties object. This switch allows us to maintain backward
       * compatibility if a new wb hits an old server.
       */
      if (isFeatureSupported("FullTextSearch", 1))
      {
         root.appendChild(m_searchProps.toXml(doc));
      }
      else
      {
         root.setAttribute(PSSearchProperties.USER_SEARCHABLE_ATTR, 
               m_searchProps.isUserSearchable() 
               ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);  
         root.setAttribute(PSSearchProperties.USER_CUSTOMIZABLE_ATTR, 
               m_searchProps.isUserCustomizable() 
               ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);  
         root.setAttribute(PSSearchProperties.DEF_SEARCH_LABEL_ATTR, 
               m_searchProps.getDefaultSearchLabel());  
      }

      return root;
   }

   // see IPSComponent
   @Override
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      try
      {
         validateType(m_type);
      }
      catch (PSValidationException e)
      {
         context.validationError(
            this, e.getErrorCode(), e.getErrorArguments());
      }

      try
      {
         Iterator settings = m_occurrenceSettings.values().iterator();
         while (settings.hasNext())
         {
            PSOccurrenceSetting setting = (PSOccurrenceSetting)settings.next();
            setting.validate();
         }
      }
      catch (PSValidationException e)
      {
         context.validationError(
            this, e.getErrorCode(), e.getErrorArguments());
      }

      if (m_submitName == null || m_submitName.trim().length() == 0)
      {
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_FIELD, null);
      }

      // do children
      context.pushParent(this);
      try
      {
         if (m_locator != null && m_default instanceof IPSComponent)
            ((IPSComponent) m_locator).validate(context);
         if (m_default != null && m_default instanceof IPSComponent)
            ((IPSComponent) m_default).validate(context);
         if (m_inputTranslation != null)
            m_inputTranslation.validate(context);
         if (m_outputTranslation != null)
            m_outputTranslation.validate(context);
         if (m_validationRules != null)
            m_validationRules.validate(context);
         if (m_visibilityRules != null)
            m_visibilityRules.validate(context);
         if (m_choices != null)
            m_choices.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Validates the provided field type.
    *
    * @param type the field type.
    * @throws PSValidationException if the provided field type is not
    *    supported.
    */
   public static void validateType(int type) throws PSValidationException
   {
      if (type != TYPE_SYSTEM && type != TYPE_SHARED && type != TYPE_LOCAL)
         throw new PSValidationException(
            IPSObjectStoreErrors.UNSUPPORTED_FIELD_TYPE, TYPE_ENUM);
   }

   /**
    * Determines if this field has occurrence settings.
    *
    * @return <code>true</code> if there are occurrence settings, <code>false
    * </code> if not.
    */
   public boolean hasOccurrenceSettings()
   {
      return m_occurrenceSettings.size() > 0;
   }

   /**
    * Clears the occurence setting for this field.
    */
   public void clearOccurrenceSettings()
   {
      m_occurrenceSettings.clear();
   }

   /**
    * Returns the array of Occurence settings Transition Ids.
    * 
    * @return Integer[] of Occurence settings Transition Ids.
    */
   public Integer[] getOccurenceSettingsTransitionIds()
   {
      return (Integer[]) m_occurrenceSettings.keySet().toArray(new Integer[0]);
   }

   /**
    * Replaces this field's occurence settings with those of the supplied
    * field.  A shallow copy of is performed, so any changes to the original
    * will also affect this object.
    *
    * @param field The field whose occurrence settings should replace those
    * in this field.  May not be <code>null</code>.
    */
   public void replaceOccurrenceSettings(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");

      m_occurrenceSettings.clear();
      m_occurrenceSettings.putAll(field.m_occurrenceSettings);
   }

   /**
    * Replaces this field's properties with those of the supplied
    * field. A shallow copy of is performed, so any changes to the original
    * will also affect this object.
    *
    * @param field the field whose properties should replace those
    *    in this field. May not be <code>null</code>.
    */
   public void replaceProperties(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");

      m_properties.clear();
      m_properties.putAll(field.m_properties);
   }

   /**
    * Gets shallow copy of this field merged with supplied source. The merged
    * field will have all non-<code>null</code> properties of this field and
    * overlays with properties of source for <code>null</code> properties of
    * this field. Validates the following rules for merging.
    * <ul>
    * <li>If source has validation rules, target cannot also have them</li>
    * <li>If DataTypes are set on both, they must match. Otherwise, the set
    *    data type will be used in the merged field.</li>
    * <li>Occurence Dimension must match</li>
    * <li>ForceBinary must match</li>
    * <li>Locators must match if both have them</li>
    * <li>systemMandatory overrides are ignored</li>
    * </ul>
    *
    * @param source the source field to merge with, may not be <code>null</code>
    * and must have the same name as this field.
    *
    * @return the merged field, never <code>null</code>
    *
    * @throws IllegalArgumentException if source is <code>null</code> or does
    * not have same name as this field name.
    * @throws PSValidationException if the source or this field fails to meet
    * one of the above rules.
    */
   public PSField merge(PSField source) throws PSValidationException
   {
      if(source == null)
         throw new IllegalArgumentException("source may not be null");

      if (!source.getSubmitName().equals( getSubmitName() ))
         throw new IllegalArgumentException(
         "The fields to be merged must have same name.");

       /*
        * Check to be sure no "illegal" values have been overridden: If source
        * has validation rules, target cannot also have them. If DataTypes
        * exist, they must match. ForceBinary must match, and Locators must 
        * match if both have the locators.
        */
      boolean valOverride = (source.hasValidationRules() && 
         hasValidationRules());
      boolean dtOverride = source.getDataType().length() > 0 && 
         getDataType().length() > 0 && !source.getDataType().equals(
            getDataType());
      boolean fbOverride = (source.isForceBinary() != isForceBinary());
      
      if (valOverride || dtOverride || fbOverride)
      {
         String setting = "";
         if (valOverride)
            setting = "validation rules";
         else if (dtOverride)
            setting = "data type";
         else if (fbOverride)
            setting = "force binary";
         throw new PSValidationException(
            IPSObjectStoreErrors.CE_INVALID_FIELD_OVERRIDE, 
            new Object[] {getSubmitName(), TYPE_ENUM[source.getType()], 
               setting});
      }

      if(getLocator() != null && source.getLocator() != null)
      {
         if (getLocator() instanceof PSBackEndColumn &&
            source.getLocator() instanceof PSBackEndColumn)
         {
            /*
             * We should not call equals method on locator
             * object(PSBackEndColumn) because it checks the table location also
             * to be equal, but the overridden system/shared fields may have
             * locators with no information on table location. So we have to
             * make sure that only the column definition is same.
             */
            PSBackEndColumn local = (PSBackEndColumn)getLocator();
            PSBackEndColumn other = (PSBackEndColumn)source.getLocator();

            if(!local.doesMatch(other))
            {
               throw new PSValidationException(
                  IPSObjectStoreErrors.CE_INVALID_FIELD_OVERRIDE,
                  new Object[] { getSubmitName(), TYPE_ENUM[source.getType()], 
                  "backend data locator" });
            }
         }
         else //the locator instances itself didn't match
            throw new PSValidationException(
               IPSObjectStoreErrors.CE_INVALID_FIELD_OVERRIDE, new Object[] 
               { getSubmitName(), TYPE_ENUM[source.getType()], "data locator" });
      }

      // do an overlay of allowed values
      PSField newField = new PSField(getType(), getSubmitName(), getLocator());
      newField.copyFrom(this);
      
      // don't overlay system mandatory, overrides are ignored
      newField.m_systemMandatory = source.m_systemMandatory;
      newField.m_systemInternal = source.m_systemInternal;
      
      /*
       * If system or shared fields are overlayed, the source field may already
       * have a type and format assigned, so keep them.
       */
      if (source.getDataType().length() > 0)
         newField.setDataType(source.getDataType());

      if (source.getDataFormat() != null)
         newField.setDataFormat(source.getDataFormat());
      
      if (getLocator() == null)
         newField.setLocator( source.getLocator() );

      if (getInputTranslation() == null)
         newField.setInputTranslation(source.getInputTranslation());

      if (getOutputTranslation() == null)
         newField.setOutputTranslation(source.getOutputTranslation());

      if (getValidationRules() == null)
         newField.setValidationRules(source.getValidationRules());

      if (getVisibilityRules() == null)
         newField.setVisibilityRules(source.getVisibilityRules());

      if (getDefault() == null)
         newField.setDefault(source.getDefault());      

      if (!hasOccurrenceSettings())
         newField.replaceOccurrenceSettings(source);

      if (getChoices() == null)
         newField.setChoices(source.getChoices());

      if (m_properties.isEmpty())
         newField.replaceProperties(source);

      return newField;
   }


   /**
    * Gets shallow copy of this field demerged with supplied source. The
    * demerged field will have all properties of this field that differ from
    * the properties of source and are allowed to be overriden.  See 
    * {@link #merge(PSField)} for details on what can be overriden.
    *
    * @param source the source field to demerge from, may not be
    * <code>null</code>
    *
    * @return the demerged field, or <code>null</code> if nothing has been 
    * overriden.
    */
   public PSField demerge(PSField source)
   {
      // check whether they are different and keep the original values
      //if they are different
      boolean hasOverrides = false;
      PSField newField = new PSField(getType(), getSubmitName(), null);
      for(String key : source.getUserPropertyKeys())
      {
         newField.setUserProperty(key, source.getUserProperty(key));
      }
      
      if (m_inputTranslation != null &&
         !compare(m_inputTranslation, source.m_inputTranslation))
      {
         newField.m_inputTranslation = m_inputTranslation;
         hasOverrides = true;         
      }
         

      if (m_outputTranslation != null &&
         !compare(m_outputTranslation, source.m_outputTranslation))
      {
         newField.m_outputTranslation = m_outputTranslation;
         hasOverrides = true;         
      }  

      if (hasValidationRules() &&
         !compare(m_validationRules, source.m_validationRules))
      {
         newField.m_validationRules = m_validationRules;
         hasOverrides = true;         
      }

      if (m_visibilityRules != null &&
         !compare(m_visibilityRules, source.m_visibilityRules))
      {
         newField.m_visibilityRules = m_visibilityRules;
         hasOverrides = true;         
      }

      if (m_default != null && !compare(m_default, source.m_default))
      {
         newField.m_default = m_default;
         hasOverrides = true;         
      }  

      if (m_occurrenceSettings != null &&
         !compare(m_occurrenceSettings, source.m_occurrenceSettings))
      {
         newField.m_occurrenceSettings = m_occurrenceSettings;
         hasOverrides = true;         
      }

      
      if (m_choices != null &&
         !compare(m_choices, source.m_choices))
      {
         newField.m_choices = m_choices;
         hasOverrides = true;         
      }

      if (!compare(m_properties, source.m_properties))
      {
         newField.m_properties = m_properties;
         hasOverrides = true;         
      }

      // ignore system mandatory overrides, a false setting the is same as not 
      // setting a value
      newField.m_systemMandatory = false;
      newField.m_systemInternal = false;
         
      newField.m_showInSummary = m_showInSummary;
      if (source.m_showInSummary != m_showInSummary)
         hasOverrides = true;
      
      newField.m_showInPreview = m_showInPreview;
      if (source.m_showInPreview != m_showInPreview)
         hasOverrides = true;

      newField.setSearchProperties(getSearchProperties());
      if (!source.m_searchProps.equals(m_searchProps))
         hasOverrides = true;

      newField.m_forceBinary = m_forceBinary;
      if (source.m_forceBinary != m_forceBinary)
         hasOverrides = true;

      newField.m_enableClearBinaryParam = m_enableClearBinaryParam;
      if (source.m_enableClearBinaryParam != m_enableClearBinaryParam)
         hasOverrides = true;
      
      return hasOverrides ? newField : null;
   }
   

   /**
    * Set the choices to use with this field.
    *
    * @param choices The choices, may be <code>null</code> to clear them.
    */
   public void setChoices(PSChoices choices)
   {
      m_choices = choices;
   }

   /**
    * Get the choices to use with this field.
    *
    * @return The choices, may be <code>null</code>.
    */
   public PSChoices getChoices()
   {
      return m_choices;
   }

   /**
    * Convenience version of {@link #getOccurrenceSetting(Integer, boolean)}
    * that calls <code>getOccurrenceSetting(transitionId, false)</code>.
    */
   private PSOccurrenceSetting getOccurrenceSetting(Integer transitionId)
   {
      return getOccurrenceSetting(transitionId, false);
   }

   /**
    * Retrieves the occurrence settings for the specified id.  If the id is
    * <code>null</code> or if settings have not been specified for the id,
    * then default settings will be returned.  
    *
    * @param transitionId The id to use to retrieve the settings.  If <code>
    * null</code>, the default settings are returned.
    * @param addNew If <code>true</code> and settings were not specified for 
    * the supplied transition id, default settings are created and added to the 
    * map before being returned.
    * 
    * @return The settings for the specified id, never <code>null</code>.
    */
   private PSOccurrenceSetting getOccurrenceSetting(Integer transitionId,
      boolean addNew)
   {
      PSOccurrenceSetting setting =
         (PSOccurrenceSetting)m_occurrenceSettings.get(transitionId);

      boolean bFound = true;
      if (setting == null)
      {
         bFound = false;

         // Try for the default setting if that's not what we're already after
         if (transitionId != null)
            setting = (PSOccurrenceSetting)m_occurrenceSettings.get(null);
         if (setting == null)
         {
            // no default setting specified, get our defaults.
            setting = new PSOccurrenceSetting();
         }

         if (!bFound && addNew)
            m_occurrenceSettings.put(transitionId, setting);
      }

      return setting;
   }
   
   /**
    * Sets a user property value or removes the property completely
    * if the value is <code>null</code>.
    * @param name the name of the property. Cannot be <code>null</code> or
    * empty.
    * @param value the property value, if <code>null</code> then
    * the property will be removed.
    */
   public void setUserProperty(String name, Object value)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      if(value == null)
         m_userProps.remove(name);
      else
         m_userProps.put(name, value);
   }
   
   /**
    * Retrieves the named user property.
    * @param name the name of the property. Cannot be <code>null</code> or
    * empty.
    * @return the value of the requested property or <code>null</code> if
    * not found.
    */
   public Object getUserProperty(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty.");
      return m_userProps.get(name);
   }
   
   /**
    * Retrieve the mime type mode for this field.
    * @return the mime type mode , may be <code>null</code>.
    */
   public PSMimeTypeModeEnum getMimeTypeMode()
   {
      PSProperty prop = getProperty(MIME_TYPE_MODE_PROPERTY);
      if(prop == null)
         return null;
      return PSMimeTypeModeEnum.valueOf(Integer.parseInt((String) prop
            .getValue()));   
   }
   
   /**
    * Retrieve the mime type value for this field. This is dependant on the mime
    * type mode selected and may be either a mime type string or a field name.
    * @return the mime type mode , may be <code>null</code>.
    */
   public String getMimeTypeValue()
   {
      PSProperty prop = getProperty(MIME_TYPE_VALUE_PROPERTY);
      if(prop == null)
         return null;
      return (String)prop.getValue();   
   }
   
   /**
    * Sets the mime type meta data. This consists of both the mime type mode and
    * the mime type value.
    * 
    * @param mode the mime type mode. Must not be <code>null</code>.
    * @param value the mime type. This will be a mime type string if the mode is
    * {@link PSMimeTypeModeEnum#FROM_SELECTION} or a field name for the
    * other modes. Cannot be <code>null</code> or empty.
    */
   public void setMimeType(PSMimeTypeModeEnum modeEnum, String value)
   {
      if (modeEnum == null)
         throw new IllegalArgumentException(
               "modeEnum must not be null.");
      if (StringUtils.isBlank(value))
         throw new IllegalArgumentException(
               "mime type value must not be null or empty.");

      PSProperty prop = new PSProperty(MIME_TYPE_MODE_PROPERTY);
      prop.setValue("" + modeEnum.getMode());
      setProperty(prop);

      prop = new PSProperty(MIME_TYPE_VALUE_PROPERTY);
      prop.setValue(value);
      setProperty(prop);
   }
   
   /**
    * Clears the mime type mode and mime type value properties.
    * 
    */
   public void clearMimeTypeProperty()
   {
      m_properties.remove(MIME_TYPE_MODE_PROPERTY);
      m_properties.remove(MIME_TYPE_VALUE_PROPERTY);
   }
   
   /**
    * Retrieve all user property keys.
    * @return never <code>null</code>, may be empty.
    */
   public Set<String> getUserPropertyKeys()
   {
      return m_userProps.keySet();
   }
   
   /**
    * Properties on this object that are transient and are never
    * persisted. Used for meta data needed at runtime.
    */
   private transient Map<String, Object> m_userProps = 
      new HashMap<String, Object>();

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXField";

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /** Optional occurrence dimension specifier. */
   public static final int OCCURRENCE_DIMENSION_OPTIONAL = 0;

   /** Required occurrence dimension specifier. */
   public static final int OCCURRENCE_DIMENSION_REQUIRED = 1;

   /** OneOrMore occurrence dimension specifier. */
   public static final int OCCURRENCE_DIMENSION_ONE_OR_MORE = 2;

   /** ZeroOrMore occurrence dimension specifier. */
   public static final int OCCURRENCE_DIMENSION_ZERO_OR_MORE = 3;

   /** Count occurrence dimension specifier. */
   public static final int OCCURRENCE_DIMENSION_COUNT = 4;

   /**
    * An array of XML attribute values for the OccurrenceDimension. The value of
    * the OCCURRENCE_DIMENSION_XXX variable is the index into this array.
    */
   private static final String[] OCCURRENCE_DIMENSION_ENUM =
   {
      "optional", "required", "oneOrMore", "zeroOrMore", "count"
   };

   /** Delimited occurrence multiValuedType specifier. */
   public static final int OCCURRENCE_MULTI_VALUED_TYPE_DELIMITED = 0;

   /** Separate occurrence multiValuedType specifier. */
   public static final int OCCURRENCE_MULTI_VALUED_TYPE_SEPARATE = 1;

   /**
    * An array of XML attribute values for the OccurrenceMultiValuedType.
    * The value of the OCCURRENCE_MULTI_VALUED_TYPE_XXX variable is the index
    * into this array.
    */
   private static final String[] OCCURRENCE_MULTI_VALUED_TYPE_ENUM =
   {
      "delimited", "separate"
   };

   /** the default occurrence delimiter */
   public static final String OCCURRENCE_DEFAULT_DELIMITER = ";";

   /**  Unknown field type specifier. */
   public static final int TYPE_UNKNOWN = -1;

   /** System field type specifier */
   public static final int TYPE_SYSTEM = 0;

   /** Shared field type specifier */
   public static final int TYPE_SHARED = 1;

   /** Local field type specifier */
   public static final int TYPE_LOCAL = 2;

   /**
    * An array of XML attribute values for the field type.
    * They are specified at the index of the specifier. Unknown is not
    * considered a valid type.
    */
   public static final String[] TYPE_ENUM =
   {
      "system", "shared", "local"
   };

   /**
    * Specifies the type of this field (TYPE_SYSTEM | TYPE_SHARED |
    * TYPE_LOCAL | TYPE_UNKNOWN)
    */
   private int m_type = TYPE_UNKNOWN;

   /**  Unknown field value type specifier. */
   public static final int FIELD_VALUE_TYPE_UNKNOWN = 0;

   /**  Content field type specifier. */
   public static final int FIELD_VALUE_TYPE_CONTENT = 1;

   /** Meta field type specifier */
   public static final int FIELD_VALUE_TYPE_META = 2;
   /**
    * An array of XML attribute values for the field value type.
    * They are specified at the index of the specifier.
    */
   public static final String[] FIELD_VALUE_TYPE_ENUM =
   {
      "unknown","content", "meta"
   };

   /**
    * Specifies the type of this field (FIELD_VALUE_TYPE_CONTENT |
    * FIELD_VALUE_TYPE_META | FIELD_VALUE_TYPE_UNKNOWN)
    */
   private int m_fieldValueType = FIELD_VALUE_TYPE_UNKNOWN;

   /**
    * See {@link #getModificationType()} for details.
    */
   private int m_modificationType = MODTYPE_USER;

   /**
    * The field name, never <code>null</code> or be empty.
    */
   private String m_submitName = null;

   /**
    * The mime type, never empty, may be <code>null</code>.
    */
   private String m_mimeType = null;

   /** Specifies if this field is included in summary views. */
   private boolean m_showInSummary = true;

   /** Specifies is this field is included in previews */
   private boolean m_showInPreview = true;

   /**
    * This flag overwrites the default behaview of the server. Set this to
    * <code>true</code> if you want the server to treat this as a binary
    * field.
    */
   private boolean m_forceBinary = false;
   
   /**
    * This flag indicates whether this field is exportable. By default it is going to be
    * true.
    */
   private boolean m_exportable = true;
  
   /**
    * This flag indicates whether or not this field is required by the system
    * to work correctly. If this is set to <code>true</code> for fields in the
    * system definition, this field cannot be excluded in local definitions.
    * If this is set to <code>true</code> for fields in shared definitions,
    * this field is always included in local definitions. Further if set to 
    * <code>true</code> this forces hiding rules to XSL (XML hiding will not be
    * allowed).
    */
   private boolean m_systemMandatory = false;

   /** The datatype of this field, never <code>null</code>, might be empty. */
   private String m_dataType = "";

   /** The dataformat of this field, may be <code>null</code>, but never empty. */
   private String m_dataFormat = null;

   /** The value locator for this field. */
   private IPSBackEndMapping m_locator = null;

   /**
    * The default value for this field. Set this to <code>null</code> if
    * not used.
    */
   private IPSReplacementValue m_default = null;

   /** This flag is set to <code>true</code> if any field validation failed */
   private boolean m_validationFailed = false;

   /**
    * The visibility rules for this field. An optional element set to
    * <code>null</code> if not used.
    */
   private PSVisibilityRules m_visibilityRules = null;

   /**
    * The validation rules for this field. An optional element set to
    * <code>null</code> if not used.
    */
   private PSFieldValidationRules m_validationRules = null;

   /**
    * The input translation performed on this field. An optional element set
    * to <code>null</code> if not used.
    */
   private PSFieldTranslation m_inputTranslation = null;

   /**
    * The output transation performed on this field. An optional element set
    * to <code>null</code> if not uesd.
    */
   private PSFieldTranslation m_outputTranslation = null;

   /**
    * Determines if this field wants to enable the clear binary parameter.
    * Defaults to <code>false</code>; will be set to <code>true</code> when
    * the attribute {@link #CLEAR_BINARY_PARAM_ATTR} is present in the XML with
    * a value other than "no".
    */
   private boolean m_enableClearBinaryParam = false;

   /**
    * Map of occurrence settings. The key is the transition id as an Integer,
    * and the value is a PSOccurrenceSetting object.  Never <code>null</code>
    * once this field is constructed. A default occurrence settings (no
    * transition id) may be in the map with a <code>null</code> key.
    */
   private Map m_occurrenceSettings = new HashMap();

   /**
    * Optional set of choices to use for this field.  Modified only by a call to
    * {@link #setChoices(PSChoices)}, may be <code>null</code>.
    */
   private PSChoices m_choices = null;

   /**
    * A map of field properties, initialized while constructed. Never
    * <code>null</code>, may be empty after that.
    */
   private Map m_properties = new HashMap();

   /**
    * Groups all search related attributes. Valid except during <code>fromXml
    * </code>, during which it is <code>null</code> for a period of time.
    * If an exception occurred during this time, it could remain <code>null
    * </code>. However, all methods can assume that it is not <code>null</code>.
    */
   private PSSearchProperties m_searchProps = new PSSearchProperties();
   
   /**
    * <code>true</code> to indicate a field is flagged for internal use only by
    * the system and should not be visible to end users in searches, display
    * formats, or content type definitions.
    */
   private boolean m_systemInternal = false;

   /**
    * The property name used to save whether or not this field may contain
    * inline links. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. Allowed values are specified in
    * <code>BOOLEAN_ENUM</code>. Defaults to 'no'.
    */
   public static final String MAY_HAVE_INLINE_LINKS_PROPERTY =
      "mayHaveInlineLinks";

   /**
    * The property name used to save whether or not to cleanup broken inline
    * links. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. Allowed values are specified in
    * <code>BOOLEAN_ENUM</code>. Defaults to 'no'.
    */
   public static final String CLEANUP_BROKEN_INLINE_LINKS_PROPERTY =
      "cleanupBrokenInlineLinks";

   /**
    * The property name used to save whether or not the field data my contain
    * literal IDs, used by MSM for ID Type support.  The property is always 
    * locked and of type <code>PSProperty.TYPE_STRING</code>. Allowed values are 
    * specified in <code>BOOLEAN_ENUM</code>. Defaults to 'no'.
    */
   public static final String MAY_CONTAIN_IDS_PROPERTY = "mayContainIDs";
   
   /**
    * The property name used to save whether or not to cleanup this
    * field before each insert/update. The only cleanup process currently
    * supported is Tidy. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. Allowed values are specified in
    * <code>BOOLEAN_ENUM</code>. Defaults to 'no'.
    */
   public static final String CLEANUP_PROPERTY = "cleanup";

   /**
    * The property name used to save the properties file used for the field
    * cleanup process. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. The value of this property is a
    * path relative to the Rhythmyx server root. The designer is responsible to
    * make sure that the referenced file exists. Defaults to
    * 'rxW2Ktidy.properties' available in every installation from the Rhythmyx
    * root.
    */
   public static final String CLEANUP_PROPERTIES_PROPERTY = "cleanupProperties";

   /**
    * The property name used to save the serverPageTag file used for the field
    * cleanup process. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. The value of this property is a
    * path relative to the Rhythmyx server root. The designer is responsible to
    * make sure that the referenced file exists. Defaults to
    * 'rxW2KserverPageTags.xml' available in every installation from the
    * Rhythmyx root.
    */
   public static final String CLEANUP_SERVERPAGETAGS_PROPERTY =
      "cleanupServerPageTags";

   /**
    * The property name used to save the encoding used for the field
    * cleanup process. The property is always locked and of type
    * <code>PSProperty.TYPE_STRING</code>. The value of this property must be
    * a valid encoding string. Defaults to 'UTF8'.
    */
   public static final String CLEANUP_ENCODING_PROPERTY = "cleanupEncoding";
   
   /**
    * a boolean property that indicates to text cleanup and the assembly 
    * system that namespaces should be selectively removed according to a 
    * configuration.
    */
   public static final String CLEANUP_NAMESPACES_PROPERTY = "cleanupNamespaces";
   
   /**
    * A string property that lists the namespaces to declare on the field in
    * text cleanup. These will be filtered in assembly to only include the
    * namespaces that are actually defined for the site. If namespaces
    * are included that are not defined for the system, i.e. have uris declared,
    * then they will not be included.
    */
   public static final String DECLARED_NAMESPACES = "declaredNamespaces";
   
   /**
    * a boolean property that tells text cleanup that JSP and ASP tags 
    * may appear in the content. These are escaped by inclosing them in 
    * processing instructions (PIs). These PIs have the name "psx-activetag".
    * The assembly system will selectively remove PIs from around these 
    * tags. It does this if the PIs name is "psx-activetag". Note that
    * PHP uses processing instructions already, and will therefore pass 
    * through the assembler untouched.
    */
   public static final String ALLOW_ACTIVE_TAGS_PROPERTY = "allowActiveTags";
   
   /**
    * A string property that contains the mime type mode for this field. This is
    * one of the <code>PSMimeTypeModeEnum</code> value.
    */
   public static final String MIME_TYPE_MODE_PROPERTY = "mimeTypeMode";
   
   /**
    * A string property that contains the value for the mime type.
    * The value is dependent on the mime type mode and may be either a
    * mime type string or a field name.
    */
   public static final String MIME_TYPE_VALUE_PROPERTY = "mimeTypeValue";
   
   /**
    * The mime type mode enumeration.
    * See {@link PSItemDefinition#getFieldMimeType(String, Map)} how the
    * enumerations get used.
    */
   public enum PSMimeTypeModeEnum
   {
      /**
       * The mime type is guessed from the data type of the field.
       */
      DEFAULT(0,"Default"),
      /**
       * The specified mime type on field is the mime type used.
       */
      FROM_SELECTION(1,"From Selection"),
      /**
       * The value of extension field is used to determine the mimetype
       */
      FROM_EXT_FIELD(2,"From Extension Field"),
      /**
       * The value of mime type filed is used as mime type.
       */
      FROM_MIMETYPE_FIELD(3,"From Mime Type Field");

      /**
       * Get the mode of the enumeration.
       * 
       * @return the mode.
       */
      public int getMode()
      {
         return mi_mode;
      }
      
      /**
       * Get the mode of the enumeration.
       * 
       * @return the mode.
       */
      public static PSMimeTypeModeEnum getModeEnum(String displayName)
      {
         PSMimeTypeModeEnum modeE = null;
         for (PSMimeTypeModeEnum modeEnum : values())
            if (modeEnum.getDisplayName().equals(displayName))
               modeE = modeEnum;

         return modeE;
      }

      /**
       * Get the display name of the enumeration.
       * 
       * @return the display name.
       */
      public String getDisplayName()
      {
         return mi_displayName;
      }

      /**
       * Get the enumeration for the supplied mode.
       * 
       * @param mode the mode for which to get the enumeration.
       * @return the enumeration, may be <code>null</code>.
       */
      public static PSMimeTypeModeEnum valueOf(int mode)
      {
         PSMimeTypeModeEnum modeE = null;
         for (PSMimeTypeModeEnum modeEnum : values())
            if (modeEnum.getMode() == mode)
               modeE = modeEnum;

         return modeE;
      }
      
      /**
       * Constructs an enumeration for the specified mode and displayname.
       */
      private PSMimeTypeModeEnum(int mode, String displayName) 
      {
         mi_mode = mode;
         mi_displayName = displayName;
      }
      
      /**
       * Stores the enumeration mode.
       */
      private int mi_mode; 

      /**
       * Stores the enumeration display name.
       */
      private String mi_displayName; 
   }
   
   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String NAME_ATTR = "name";
   private static final String SHOW_IN_SUMMARY_ATTR = "showInSummary";
   private static final String SYSTEM_MANDATORY_ATTR = "systemMandatory";
   private static final String SHOW_IN_PREVIEW_ATTR = "showInPreview";
   private static final String FORCE_BINARY_ATTR = "forceBinary";
   private static final String EXPORT_ATTR = "export";
   private static final String CLEAR_BINARY_PARAM_ATTR = "clearBinaryParam";
   private static final String FIELD_TYPE_ATTR = "type";
   private static final String FIELD_VALUE_TYPE_ATTR = "fieldvaluetype";
   private static final String MODIFICATION_TYPE_ATTR = "modificationType";
   private static final String MIME_TYPE_ATTR = "mimetype";
   private static final String DATA_TYPE_ELEM = "DataType";
   private static final String DATA_FORMAT_ELEM = "DataFormat";
   public static final String DATA_LOCATOR_ELEM = "DataLocator";
   private static final String DEFAULT_VALUE_ELEM = "DefaultValue";
   public static final String DEFAULT_NAMESPACE = "default";
   private static final String FIELD_RULES_ELEM = "FieldRules";
   private static final String FIELD_INPUT_TRANSLATIONS_ELEM = 
      "FieldInputTranslation";
   private static final String FIELD_OUTPUT_TRANSLATIONS_ELEM = 
      "FieldOutputTranslation";
   private static final String OCCURRENCE_ELEM = "OccurrenceSettings";
   private static final String OCCURRENCE_DIMENSION_ATTR = "dimension";
   private static final String OCCURRENCE_MULTI_VALUED_TYPE_ATTR = 
      "multiValuedType";
   private static final String OCCURRENCE_DELIMITER_ATTR = "delimiter";
   private static final String TRANSITION_ID_ATTR = "transitionId";
   private static final String SYSTEM_INTERNAL_ATTR = "systemInternal";


   /**
    * Constant string that will be appended to the submit name of this field
    * to form the clear binary parameter name
    */
   public static final String CLEAR_BINARY_PARAM_SUFFIX = "_clear";

   /**
    * The dimension enumeration as used with the occurrence settings.
    */
   public enum PSDimensionEnum
   {
      OPTIONAL(OCCURRENCE_DIMENSION_OPTIONAL),
      REQUIRED(OCCURRENCE_DIMENSION_REQUIRED),
      ONEORMORE(OCCURRENCE_DIMENSION_ONE_OR_MORE),
      ZEROORMORE(OCCURRENCE_DIMENSION_ZERO_OR_MORE),
      COUNT(OCCURRENCE_DIMENSION_COUNT);

      /**
       * Get the ordinal of the enumeration.
       * 
       * @return the ordinal.
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      /**
       * Get the enumeration for the supplied ordinal.
       * 
       * @param ordinal the ordinal for which to get the enumeration.
       * @return the enumeration, never <code>null</code>.
       * @throws IllegalArgumentException if no enumeration exists for the
       *    supplied ordinal.
       */
      public static PSDimensionEnum valueOf(int ordinal)
      {
         for (PSDimensionEnum dimension : values())
            if (dimension.getOrdinal() == ordinal)
               return dimension;

         throw new IllegalArgumentException(
            "No dimension is defined for the supplied ordinal.");
      }
      
      /**
       * Constructs an enumeration for the specified ordinal.
       * 
       * @param ordinal the enumeration ordinal.
       */
      private PSDimensionEnum(int ordinal) 
      {
         mi_ordinal = ordinal;
      }
      
      /**
       * Stores the enumeration ordinal.
       */
      private int mi_ordinal; 
   }
   
   /**
    * This class encapsulates a set of OccurenceSettings.
    */
   private class PSOccurrenceSetting implements Serializable
   {
       @Override
       public boolean equals(Object o) {
           if (this == o) return true;
           if (!(o instanceof PSOccurrenceSetting)) return false;
           PSOccurrenceSetting that = (PSOccurrenceSetting) o;
           return m_occurrenceDimension == that.m_occurrenceDimension &&
                   m_occurrenceCount == that.m_occurrenceCount &&
                   m_occurrenceMultiValuedType == that.m_occurrenceMultiValuedType &&
                   Objects.equals(m_occurrenceDelimiter, that.m_occurrenceDelimiter);
       }

       @Override
       public int hashCode() {
           return Objects.hash(m_occurrenceDimension, m_occurrenceCount, m_occurrenceMultiValuedType, m_occurrenceDelimiter);
       }

       /**
       * 
       */
      private static final long serialVersionUID = 6668801176979874215L;

      /**
       * Get the number of values required.
       *
       * @return the occurrence count, only valid if occurrence dimension is
       *    COUNT, -1 means not specified.
       */
      public int getOccurrenceCount()
      {
         return m_occurrenceCount;
      }

      /**
       * Set a new occurrence count.  See {@link #getOccurrenceCount}.
       *
       * @param count the new occurrence count.
       */
      public void setOccurrenceCount(int count)
      {
         m_occurrenceCount = count;
      }

      /**
       * Get the occurrence delimiter used to separate values in a multi-valued
       * field.
       *
       * @return the occurrence delimiter, never <code>null</code> or
       *    empty.
       */
      public String getOccurrenceDelimiter()
      {
         return m_occurrenceDelimiter;
      }

      /**
       * Set the occurrence delimiter.  See {@link #getOccurrenceDelimiter}.
       *
       * @param occurrenceDelimiter the occurrence delimiter, not
       *    <code>null</code> or empty.
       */
      public void setOccurrenceDelimiter(String occurrenceDelimiter)
      {
         if (occurrenceDelimiter == null ||
             occurrenceDelimiter.length() == 0)
            throw new IllegalArgumentException(
               "the delimiter cannot be null or empty");

         m_occurrenceDelimiter = occurrenceDelimiter;
      }

      /**
       * Get the occurrence dimension.
       *
       * @return the occurrence dimension, one of the OCCURRENCE_DIMENSION_XXX
       * values.
       */
      public int getOccurrenceDimension()
      {
         return m_occurrenceDimension;
      }

      /**
       * Set the occurrence dimension. See {@link #getOccurrenceDimension}
       *
       * @param occurrenceDimension the new occurrence dimension.
       * @throws PSValidationException if the provided occurrence setting is not
       *    supported.
       */
      public void setOccurrenceDimension(int occurrenceDimension)
         throws PSValidationException
      {
         validateOccurrenceDimension(occurrenceDimension);

         m_occurrenceDimension = occurrenceDimension;
      }

      /**
       * Get the occurrence multi valued type.
       *
       * @return the occurrence multi valued type.
       */
      public int getOccurrenceMultiValuedType()
      {
         return m_occurrenceMultiValuedType;
      }

      /**
       * Set the occurrence multi valued type.
       *
       * @param occurrenceMultiValuedType the new occurrence multi valued type.
       * @throws PSValidationException if the provided multi valued type is not
       *    supported.
       */
      public void setOccurrenceMultiValuedType(int occurrenceMultiValuedType)
         throws PSValidationException
      {
         validateOccurrenceMultiValuedType(occurrenceMultiValuedType);

         m_occurrenceMultiValuedType = occurrenceMultiValuedType;
      }



      /**
       * Validates the data in this object.
       *
       * @throws PSValidationException if any data is invalid.
       */
      public void validate() throws PSValidationException
      {
         validateOccurrenceDimension(m_occurrenceDimension);
         validateOccurrenceMultiValuedType(m_occurrenceMultiValuedType);
      }

      /**
       * Validates the occurrence dimension.
       *
       * @param dimension occurrence dimension.
       * @throws PSValidationException if the provided occurrence dimension is
       *    not supported.
       */
      public void validateOccurrenceDimension(int dimension)
         throws PSValidationException
      {
         if (dimension != OCCURRENCE_DIMENSION_COUNT &&
             dimension != OCCURRENCE_DIMENSION_ONE_OR_MORE &&
             dimension != OCCURRENCE_DIMENSION_OPTIONAL &&
             dimension != OCCURRENCE_DIMENSION_REQUIRED &&
             dimension != OCCURRENCE_DIMENSION_ZERO_OR_MORE)
            throw new PSValidationException(
               IPSObjectStoreErrors.UNSUPPORTED_OCCURRENCE_DIMENSION,
               OCCURRENCE_DIMENSION_ENUM);
      }

      /**
       * Validates the occurrence multi valued type.
       *
       * @param multiValuedType the occurrence multi valued type to validate.
       * @throws PSValidationException if the provided occurrence multi valued
       *    type is not supported.
       */
      public void validateOccurrenceMultiValuedType(int multiValuedType)
         throws PSValidationException
      {
         if (multiValuedType != OCCURRENCE_MULTI_VALUED_TYPE_DELIMITED &&
             multiValuedType != OCCURRENCE_MULTI_VALUED_TYPE_SEPARATE)
            throw new PSValidationException(
               IPSObjectStoreErrors.UNSUPPORTED_OCCURRENCE_MULTI_VALUED_TYPE,
               OCCURRENCE_MULTI_VALUED_TYPE_ENUM);
      }


      /**
        * Container for the occurrence dimension attribute, specifying how to
        * determine the correct number of values required.  The default is that
        * the value is optional.
        */
      private int m_occurrenceDimension = OCCURRENCE_DIMENSION_OPTIONAL;

      /**
        * Occurrence count, only valid if occurrenceDimension is
        * OCCURRENCE_DIMENSION_COUNT.
        */
      private int m_occurrenceCount = -1;

      /**
        * Container for the occurrence multiValuedType attribute. If multiple
        * values exist, are the separate values, or a single string with
        * delimeted values.  The default value is the latter.
        */
      private int m_occurrenceMultiValuedType =
         OCCURRENCE_MULTI_VALUED_TYPE_DELIMITED;

      /**
        * The occurrence delimiter string.  If occurrenceMultiValuedType is
        * OCCURRENCE_MULTI_VALUED_TYPE_DELIMITED, this is the string used as
        * a delimeter.  Default is a ";".
        */
      private String m_occurrenceDelimiter = OCCURRENCE_DEFAULT_DELIMITER;
   }
   
   /**
    * Constant for the parent fieldset user property key.
    */
   public static final String SHARED_GROUP_FIELDSET_USER_PROP = "sharedgroupfieldset";
   
   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSField");
}

