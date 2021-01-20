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
package com.percussion.webdav.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.webdav.IPSWebdavConstants;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a supported webdav content type
 */
public class PSWebdavContentType
      extends PSWebdavComponent
      implements IPSWebdavConstants
{

   /**
    * Construct <code>PSWebdavContentType</code> from xml
    * element passed in
    *
    * @param src xml element that represents a webdav content
    * type, cannot be <code>null</code>.
    *
    * @param capture flag indicating that validation exception should not
    * be thrown but instead be collected in a list for later use.
    * 
    * @param captureList the list to be used to store the captured validation
    * exceptions, if <code>null</code>, a default list will be used.
    * 
    * @throws PSWebdavException if an error occurs while creating this object
    * from xml.
    */
   public PSWebdavContentType(Element src, boolean capture, List captureList)
      throws PSWebdavException, PSUnknownNodeTypeException
   {
      setValidationExceptionCapture(capture);
      if(captureList != null)
         setValidationExceptionsList(captureList);
      fromXml(src);
   }

   /**
    * Construct <code>PSWebdavContentType</code> from xml
    * element passed in
    *
    * @param src xml element that represents a webdav content
    * type, cannot be <code>null</code>.
    *
    * @param capture flag indicating that validation exception should not
    * be thrown but instead be collected in a list for later use.
    * 
    * @param captureList the list to be used to store the captured validation
    * exceptions, if <code>null</code>, a default list will be used.
    * 
    * @throws PSWebdavException if an error occurs while creating this object
    * from xml.
    */
   public PSWebdavContentType(Element src)
      throws PSWebdavException, PSUnknownNodeTypeException
   {
      this(src, false, null);
   }


   /**
    * Returns supported mime types for this content type
    *
    * @return iterator of mime types. Never <code>null</code>,
    * may be empty.
    */
   public Iterator getMimeTypes()
   {
      return m_mimetypes.iterator();
   }

   /**
    * Returns properties for this content type
    *
    * @return iterator of <code>PSPropertyFieldNameMapping</code> objects.
    *     Never <code>null</code>, may be empty.
    */
   public Iterator getMappings()
   {
      return m_mappings.iterator();
   }

   /**
    * Get a list of the property-field mappings.
    * 
    * @return An collection over zero or more <code>PSPropertyFieldNameMapping
    *    </code> objects. Never <code>null</code>, may by empty.
    */
   public Collection getMappingList()
   {
      return m_mappings;
   }
   
   /**
    * Get the id of the content type.
    *
    * @return the id
    */
   public long getId()
   {
      return m_id;
   }

   /**
    * Returns the name of this content type
    *
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns the field name for the content.
    * 
    * @return The content field, never <code>null</code> or empty. 
    */
   public String getContentField()
   {
      return m_contentField;
   }
   
   /**
    * Returns the field name for the (lock) owner.
    * 
    * @return The owner field, never <code>null</code> or empty. 
    */
   public String getOwnerField()
   {
      return m_ownerField;
   }


   /**
    * If set to <code>true</code> then MimeTypes may be empty,
    * if set to <code>false</code> then MimeTypes must not
    * be empty.
    * @return default indicator flag.
    */
   public boolean isDefault()
   {
      return m_default;
   }

   /**
    * Get the field name for the specified property
    *
    * @param propertyName the name of the property for which
    *    the field name should be found.
    *
    * @return the field name string. May be <code>null</code> if
    *    the property specified is not found.
    */
   public String getFieldName(String propertyName)
   {
      return (String) m_propertyFieldMap.get(propertyName);
   }

   /**
    * Add the specified property and field name mapping.
    *
    * @param mapping The to be added mapping, it may not be <code>null</code>.
    */
   public void addPropertyFieldMapping(PSPropertyFieldNameMapping mapping)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      m_mappings.add(mapping);
      m_propertyFieldMap.put(mapping.getPropertyName(), mapping.getFieldName());
   }

   // Implement IPSWebdavComponent interface method
   public Element toXml(Document doc)
   {
      if(null == doc)
         throw new IllegalArgumentException("Document cannot be null.");

      Element root = doc.createElement(getNodeName());
      root.setAttribute(IPSRxWebDavDTD.ATTR_NAME, m_name);
      root.setAttribute(IPSRxWebDavDTD.ATTR_ID, String.valueOf(m_id));
      root.setAttribute(IPSRxWebDavDTD.ATTR_CONTENTFIELD, m_contentField);
      root.setAttribute(IPSRxWebDavDTD.ATTR_OWNERFIELD, m_ownerField);
      root.setAttribute(IPSRxWebDavDTD.ATTR_DEFAULT, String.valueOf(m_default));
      // Add mime types if needed
      if(!m_mimetypes.isEmpty())
      {
         Element mimetypes = doc.createElement(IPSRxWebDavDTD.ELEM_MIMETYPES);
         Iterator it = getMimeTypes();
         while(it.hasNext())
         {
            Element mimetype = doc.createElement(IPSRxWebDavDTD.ELEM_MIMETYPE);
            mimetype.appendChild(doc.createTextNode((String)it.next()));
            mimetypes.appendChild(mimetype);
         }
         root.appendChild(mimetypes);
      }
      // Add properties
      Element properties = doc.createElement(IPSRxWebDavDTD.ELEM_PROPERTYMAP);
      Iterator it = getMappings();
      while(it.hasNext())
      {
         PSPropertyFieldNameMapping property =
            (PSPropertyFieldNameMapping)it.next();
         properties.appendChild(property.toXml(doc));
      }
      root.appendChild(properties);

      return root;
   }

   /**
    * Implements fromXml method for
    * {@link com.percussion.cms.objectstore.IPSWebdavComponent}.
    * Expects the following xml format:
    * <p><code><pre>
    * &lt;!ELEMENT PSXWebdavContentType (Mimetypes?, Properties)&gt;
    * &lt;!ATTLIST PSXWebdavContentType
    *   name CDATA #REQUIRED
    *   id CDATA #REQUIRED
    *   contentfield CDATA #REQUIRED
    *   ownerfield CDATA #REQUIRED
    *   default CDATA #REQUIRED
    * &gt;
    * &lt;!--
    *   The supported mime types for a Rhythmyx content type
    * --&gt;
    * &lt;!ELEMENT MimeTypes (MimeType+)&gt;
    * &lt;!--
    *   The mime type that the current NonItemExtractor will accept.
    *   name - the mime type name, e.g. image/gif.
    * --&gt;
    * &lt;!ELEMENT MimeType (#PCDATA)&gt;
    * &lt;!--
    *   This collection of properties specifies all supported WebDAV
    *   properties and its related fields in Rhythmyx.
    * --&gt;
    * &lt;!ELEMENT Properties (PSXWebdavProperty+)&gt;
    *</pre></code></p>
    * @param src xml source element, cannot be <code>null</code>.
    */
   public void fromXml(Element src)
      throws PSWebdavException, PSUnknownNodeTypeException
   {
      if(null == src)
         throw new IllegalArgumentException("Source element cannot be null.");

      String name = src.getAttribute(IPSRxWebDavDTD.ATTR_NAME);
      String id = src.getAttribute(IPSRxWebDavDTD.ATTR_ID);
      String contentfield = src.getAttribute(IPSRxWebDavDTD.ATTR_CONTENTFIELD);
      String ownerfield = src.getAttribute(IPSRxWebDavDTD.ATTR_OWNERFIELD);
      boolean def =
         Boolean.valueOf(
            src.getAttribute(IPSRxWebDavDTD.ATTR_DEFAULT)).booleanValue();

      
      
      // reset collections
      m_mimetypes.clear();
      m_mappings.clear();
      m_propertyFieldMap.clear();
      boolean hasMimeTypeEls = true;
      
      // Get mime types
      Element childEl = PSXMLDomUtil.getFirstElementChild(src);
      if (childEl.getNodeName().equalsIgnoreCase(IPSRxWebDavDTD.ELEM_MIMETYPES))
      {
         Element mimetypeEl = null;
         
         try
         {
            mimetypeEl =
               PSXMLDomUtil.getFirstElementChild(
                  childEl,
                  IPSRxWebDavDTD.ELEM_MIMETYPE);
         }
         catch (PSUnknownNodeTypeException e)
         {
           // If you got here it means there is a MimeTypes element
           // without any MimeType elements
           hasMimeTypeEls = false;
         }
         
         while (mimetypeEl != null)
         {
            PSXMLDomUtil.checkNode(mimetypeEl, IPSRxWebDavDTD.ELEM_MIMETYPE);
            String mimeType = PSXMLDomUtil.getElementData(mimetypeEl);
            m_mimetypes.add(mimeType.trim());
            mimetypeEl = PSXMLDomUtil.getNextElementSibling(mimetypeEl);
         }
      }

      // Get property field mappings
      if (m_mimetypes.isEmpty() && hasMimeTypeEls)
      {
         PSXMLDomUtil.checkNode(childEl, IPSRxWebDavDTD.ELEM_PROPERTYMAP);
      }
      else
      {
         childEl = PSXMLDomUtil.getNextElementSibling(childEl,
               IPSRxWebDavDTD.ELEM_PROPERTYMAP);
      }
      Element propertyEl = PSXMLDomUtil.getFirstElementChild(childEl);
      while (propertyEl != null)
      {
         PSXMLDomUtil.checkNode(propertyEl,
            IPSRxWebDavDTD.ELEM_PROPERTYFIELD_MAPPING);
         PSPropertyFieldNameMapping mapping =
            new PSPropertyFieldNameMapping(
               propertyEl,
               m_captureValidationExceptions,
               getValidationExceptionsList());
         // Check for duplicate properties
         if(getFieldName(mapping.getPropertyName()) != null)
         {
            handleValidationExceptions(
               new PSWebdavException(
                  IPSWebdavErrors.CANNOT_HAVE_DUPLICATE_PROPERTIES));
         }
         addPropertyFieldMapping(mapping);

         propertyEl = PSXMLDomUtil.getNextElementSibling(propertyEl);
      }

      
      // Now we do a bit more validation
      if(name.trim().length() == 0)
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED,
               new Object[] {IPSRxWebDavDTD.ATTR_NAME, getNodeName()}));

      if(id.trim().length() == 0)
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED,
               new Object[] {IPSRxWebDavDTD.ATTR_ID, getNodeName()}));

      if(contentfield.trim().length() == 0)
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED,
               new Object[] {IPSRxWebDavDTD.ATTR_CONTENTFIELD, getNodeName()}));

      if(ownerfield.trim().length() == 0)
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED,
               new Object[] {IPSRxWebDavDTD.ATTR_OWNERFIELD, getNodeName()}));

      validateRequiredProperties(name);

      // If default is false then we need to be sure mimetypes are defined
      if(!def && m_mimetypes.isEmpty())
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.MIMETYPES_REQUIRED));

      try
      {
         m_id = Integer.parseInt(id);
      }
      catch (NumberFormatException e)
      {
         handleValidationExceptions(
            new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED,
               new Object[] {IPSRxWebDavDTD.ATTR_ID, getNodeName()}));
      }
      
      m_name = name;
      m_contentField = contentfield;
      m_ownerField = ownerfield;
      m_default = def;

   }

   // Implement IPSWebdavComponent interface method
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   // Implement IPSWebdavComponent interface method
   public boolean equals(Object obj)
   {
      if(! (obj instanceof PSWebdavContentType))
      {
         return false;
      }
      else
      {
         PSWebdavContentType ct = (PSWebdavContentType) obj;
         return m_name.equals(ct.m_name) 
            && m_contentField.equals(ct.m_contentField) 
            && m_ownerField.equals(ct.m_ownerField) 
            && m_mimetypes.equals(ct.m_mimetypes)
            && m_mappings.equals(ct.m_mappings); 
      }
   }

   // Implement IPSWebdavComponent interface method
   public int hashCode()
   {
       return m_name.hashCode() +
          m_contentField.hashCode() +
          m_ownerField.hashCode() +
          m_mimetypes.hashCode() +
          m_mappings.hashCode();
   }

   /**
    * Validates the properties list to make sure that required properties
    * are present. Required properties are:
    * <ul>
    *    <li>getcontenttype</li>
    *    <li>getcontentlength</li>
    *    <li>psx_content</li>
    * </ul>
    *
    * @param name the content type name
    * @throws PSWebdavException if a required property does not exist.
    */
   private void validateRequiredProperties(String name) 
      throws PSWebdavException
   {
      String[] required = IPSWebdavConstants.REQUIRED_PROPERTIES;

      for (int i = 0; i < required.length; i++)
      {
         if (getFieldName(required[i]) == null)
         {
            
            handleValidationExceptions(
               new PSWebdavException(
                  IPSWebdavErrors.MISSING_REQUIRED_PROPERTY,
                  new Object[] {required[i], name}));      
         }
      }
   }

   /**
    * Adds the default property-field name mappings if any of the default
    * WebDAV property does not have a field name to map with.
    */
   public void addDefaultMappings()
   {
      for (int i=0; i < DEFAULT_MAPPINGS.length; i++)
      {
         if (getFieldName(DEFAULT_MAPPINGS[i].getPropertyName()) == null)
             addPropertyFieldMapping(DEFAULT_MAPPINGS[i]);
      }
   }

   /**
    * The default property and field name mappings array.
    */
   public final static PSPropertyFieldNameMapping[] DEFAULT_MAPPINGS =
   {
      new PSPropertyFieldNameMapping(P_CREATIONDATE, "sys_contentcreateddate"),
      new PSPropertyFieldNameMapping(P_GETLASTMODIFIED,
                                     "sys_contentlastmodifieddate"),
      new PSPropertyFieldNameMapping(P_DISPLAYNAME, "sys_title"),
      new PSPropertyFieldNameMapping(P_GETCONTENTLANGUAGE, "sys_lang")
   };

   /**
    * The default property and field name mappings list.
    */
   public final static List DEFAULT_MAPPING_LIST = Collections.unmodifiableList(
      Arrays.asList(DEFAULT_MAPPINGS)); 
      
   /**
    * The property-field name mapping list for folder component
    */
   public final static List FOLDER_MAPPING_LIST = Collections.unmodifiableList(
      Arrays.asList(DEFAULT_MAPPINGS)); 
   /**
    * The id of the content type in Rhythmyx
    */
   private long m_id;

   /**
    * The name of the content type in Rhythmyx
    */
   private String m_name;

   /**
    * The field name for the content, never <code>null</code> or empty after 
    * the ctor.
    */
   private String m_contentField;

   /**
    * If set to <code>true</code> then MimeTypes may be empty,
    * if set to <code>false</code> then MimeTypes must not
    * be empty.
    */
   private boolean m_default;

   /**
    * Collection of supported mime types for a Rhythmyx content type
    * Never <code>null</code>, may be empty.
    */
   private Collection m_mimetypes = new ArrayList();

   /**
    * Collection of property-field mappings for this content type
    * Never <code>null</code>, may be empty.
    */
   private Collection m_mappings = new ArrayList();

   /**
    * The property and field name map. The key of the map is the property
    * name as <code>String</code> object, the value of the map is the field name
    * as <code>String</code> object. The map is never <code>null</code>.
    */
   private Map m_propertyFieldMap = new HashMap();

   /**
    * The field name which hold the owner of a WebDAV lock. This is a required
    * field, never <code>null</code> or empty after the ctor.
    */
   private String m_ownerField;
   
   /**
    * Xml node name for this class
    */
   private final static String XML_NODE_NAME = IPSRxWebDavDTD.ELEM_CONTENTTYPE;

}