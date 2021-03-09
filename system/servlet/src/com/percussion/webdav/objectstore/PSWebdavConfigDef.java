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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Represents the WebDav XML configuration file
 */
public class PSWebdavConfigDef extends PSWebdavComponent
{

   /**
    * Constructs a <code>PSWebdavConfigDef</code> object from the supplied  
    * XML element. 
    *
    * @param src the XML Element that represents a WebDav configuration.
    * Cannot be <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if the XML element does not compliant
    *    with RxWebDAVConfig.dtd, see {@link #fromXml(Element)}. 
    * @throws PSWebdavException if other error occurs.
    */
   public PSWebdavConfigDef(Element src) throws PSWebdavException,
         PSUnknownNodeTypeException
   {
      fromXml(src);
   }

   /**
    * Constructs a new <code>PSWebdavComponent</code>object from a file who's
    * path is specified.
    *
    * @param xmlpath the path location of the xml configuration file. Cannot
    * be <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException if the XML config file does not 
    *    compliant with RxWebDAVConfig.dtd, see {@link #fromXml(Element)}. 
    * @throws PSWebdavException if any error occurs while reading or parsing
    * the xml file.
    */
   public PSWebdavConfigDef(String xmlpath) throws PSWebdavException,
         PSUnknownNodeTypeException
   {
      this(new File(xmlpath));
   }

   /**
    * Constructs a new <code>PSWebdavComponent</code>object from a file who's
    * path is specified.
    *
    * @param xmlfile object containing the path location of the xml
    * configuration file. Cannot be <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException if the XML config file does not 
    *    compliant with RxWebDAVConfig.dtd, see {@link #fromXml(Element)}. 
    * @throws PSWebdavException if any error occurs while reading or parsing
    * the xml file.
    */
   public PSWebdavConfigDef(File xmlfile) throws PSWebdavException,
         PSUnknownNodeTypeException
   {
      if (null == xmlfile)
         throw new IllegalArgumentException("File cannot be null.");
      // Does the specified file actually exist?
      if (!xmlfile.exists() || !xmlfile.isFile())
         throw new PSWebdavException(IPSWebdavErrors.FILE_DOES_NOT_EXIST,
               xmlfile.getAbsolutePath());

      try( FileInputStream in = new FileInputStream(xmlfile)){
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(
               in), false);
         fromXml(doc.getDocumentElement());
      }
      catch (IOException ie)
      {
         ie.printStackTrace();
         throw new PSWebdavException(IPSWebdavErrors.IO_EXCEPTION_OCCURED, ie
               .getMessage());
      }
      catch (SAXException se)
      {
         se.printStackTrace();
         throw new PSWebdavException(IPSWebdavErrors.SAX_EXCEPTION_OCCURED, se
               .getMessage());
      }
   }

   /**
    * Constructs a <code>PSWebdavComponent</code>object from a supplied content
    * of the config file in XML.
    *
    * @param in the content of the XML configuration file. Cannot be 
    *    <code>null</code> or empty.
    *    
    * @throws PSUnknownNodeTypeException if the content of XML config file does 
    *    not compliant with RxWebDAVConfig.dtd, see {@link #fromXml(Element)}. 
    * @throws PSWebdavException if any error occurs while reading or parsing
    * the xml file.
    */
   public PSWebdavConfigDef(InputStream in) throws PSWebdavException,
         PSUnknownNodeTypeException
   {
      this(in, false, null);
   }

   /**
    * Constructs a <code>PSWebdavComponent</code>object from a supplied content
    * of the config file in XML.
    *
    * @param in the content of the xml configuration file. Cannot be 
    *    <code>null</code> or empty.
    * 
    * @param capture flag indicating that validation exception should not
    * be thrown but instead be collected in a list for later use.
    * 
    * @param captureList the list to be used to store the captured validation
    * exceptions, if <code>null</code>, a default list will be used.
    *
    * @throws PSUnknownNodeTypeException if the content of the XML config file 
    *    does not compliant with RxWebDAVConfig.dtd, 
    *    see {@link #fromXml(Element)}. 
    * @throws PSWebdavException if any error occurs while reading or parsing
    *    the xml file.
    */
   public PSWebdavConfigDef(InputStream in, boolean capture, List captureList)
         throws PSWebdavException, PSUnknownNodeTypeException
   {
      if (null == in)
         throw new IllegalArgumentException("InputStream cannot be null.");

      setValidationExceptionCapture(capture);
      if (captureList != null)
         setValidationExceptionsList(captureList);

      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(
               in), false);
         fromXml(doc.getDocumentElement());
      }
      catch (IOException ie)
      {
         ie.printStackTrace();
         throw new PSWebdavException(IPSWebdavErrors.IO_EXCEPTION_OCCURED, ie
               .getMessage());
      }
      catch (SAXException se)
      {
         se.printStackTrace();
         throw new PSWebdavException(IPSWebdavErrors.SAX_EXCEPTION_OCCURED, se
               .getMessage());
      }
   }

   /**
    * Returns all content types for this configuration.
    *
    * @return collection of all <code>PSWebdavContentType</code> objects.
    * Never <code>null</code>, may be empty.
    */
   public Iterator getContentTypes()
   {
      return m_contenttypes.iterator();
   }

   /**
    * Returns the default content type
    * @return the default <code>PSWebdavContentType</code> object, if it
    * exists else <code>null</code>
    */
   public PSWebdavContentType getDefaultContentType()
   {
      PSWebdavContentType contentType = null;
      Iterator it = getContentTypes();
      while (it.hasNext())
      {
         contentType = (PSWebdavContentType) it.next();
         if (contentType.isDefault())
            break;
         else
            contentType = null;
      }
      return contentType;
   }

   /**
    * Returns the community name used for this configuration
    * 
    * @return community string, never <code>null</code> or empty.
    */
   public String getCommunityName()
   {
      return m_communityName;
   }

   /**
    * Returns the community id used for this configuration
    * 
    * @return community id.
    */
   public int getCommunityId()
   {
      return m_communityId;
   }

   /**
    * Get the locale that is specified for this configuration.
    * 
    * @return The locale, never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return m_locale;
   }

   /**
    * Get the exclude folder properties, which should not be inherited from the 
    * parent folder when creating a new folder
    *  
    * @return an iterator over zero or more <code>String</code> objects, 
    *    never <code>null</code>, but may by empty.
    */
   public Iterator getExcludeFolderProperties()
   {
      return m_excludeFolderProperties.iterator();
   }

   /**
    * Returns the root path that is specified in the configuration
    * 
    * @return root path, never <code>null</code> or empty.
    */
   public String getRootPath()
   {
      return m_rootPath;
   }

   /**
    * Indicate the behavior of the DELETE operation (in DELETE, COPY and COPY 
    * methods). 
    * 
    * @return <code>true</code> if the DELETE operation will purge the target 
    * component(s) (items or folders), the target items cannot be recovered 
    * afterwards; otherwise the DELETE operation will only remove the folder 
    * relationship for the target items can be recovered afterwards. 
    */
   public boolean isDeleteAsPurge()
   {
      return m_isDeleteAsPurge;
   }

   /**
    * Returns the comma-separated list of tokens specified in the configuration
    * for publicValid. If the user supplied value is invalid, 
    * the default will be used never <code>null</code> or empty.
    * 
    * @return list of char values for ContentValid, defaults to "y".
    */
   public String getPublicValidTokens()
   {
      return m_publicValidTokens;
   }

   /**
    * Returns the comma-separated list of tokens specified in the configuration
    * for qeValid. If the user supplied value is invalid, the default will 
    * be used never <code>null</code> or empty.
    * 
    * @return list of char values for ContentValid, defaults to "i".
    */
   public String getQEValidTokens()
   {
      return m_qeValidTokens;
   }

   // Implement IPSWebdavComponent interface method
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("Document cannot be null.");

      Element rootEl = doc.createElement(getNodeName());
      rootEl.setAttribute(IPSRxWebDavDTD.ATTR_ROOT, m_rootPath);
      rootEl.setAttribute(IPSRxWebDavDTD.ATTR_COMMUNITY_NAME, m_communityName);
      rootEl.setAttribute(IPSRxWebDavDTD.ATTR_COMMUNITY_ID, String
            .valueOf(m_communityId));
      rootEl.setAttribute(IPSRxWebDavDTD.ATTR_LOCALE, m_locale);
      if (m_isDeleteAsPurge)
      {
         rootEl.setAttribute(IPSRxWebDavDTD.ATTR_DELETEAS,
               IPSRxWebDavDTD.ATTR_VALUE_PURGE);
      }

      Iterator it = getContentTypes();
      while (it.hasNext())
         rootEl.appendChild(((PSWebdavContentType) it.next()).toXml(doc));

      if (!m_excludeFolderProperties.isEmpty())
      {
         Element propertiesElem = doc
               .createElement(IPSRxWebDavDTD.ELEM_EXCLUDE_FOLDER_PROPERTIES);
         Iterator propNames = m_excludeFolderProperties.iterator();
         Element nameElem;
         while (propNames.hasNext())
         {
            nameElem = doc.createElement(IPSRxWebDavDTD.ELEM_PROPERTY_NAME);
            nameElem.appendChild(doc.createTextNode((String) propNames.next()));
            propertiesElem.appendChild(nameElem);
         }
         rootEl.appendChild(propertiesElem);
      }

      return rootEl;
   }

   /**
    * Implements fromXml method for
    * {@link com.percussion.webdav.objectstore.IPSWebdavComponent}.
    * Expects the following xml format:
    * <p><pre>
    * &lt;!-- The WebDAV configuration information
    *
    * "root" - the virtual root path of the Rhythmyx for all resource
    *          specified in the request. It must start from "Site" or "Folder"
    *          For example, root="//Site/intranet", root="//Folder/internet"
    *
    * "community" - the name of the community, which is used to communicate
    *   with Rhythmyx Server.
    * 
    * "locale" - the locale which is used for the created items or folders.
    * 
    * "deleteas" - an optional attribute, indicating the behavior of the 
    *              DELETE operation. The possible values are:
    *              "purge":   indicate the DELETE operation will purge the
    *                         target items and/or folders. The purged items
    *                         will not be able to recovered afterwards.
    *              "remove":  indicate the DELETE operation will remove the
    *                         folder relationships with the target items. The
    *                         removed items can be recovered afterwards. This
    *                         is the default behavior if this attribute is not
    *                         specified.
    * "PublicValidTokens" - comma separated list of chars
    *                 default "y"
    *
    * "QEValidTokens"     - comma separated list of char for quick-edit values
    *                 default "i"        
    * 
    * --&gt;
    * &lt;!ELEMENT PSXWebdavConfig (PSXWebdavContentType+)&gt;
    * &lt;!ATTLIST PSXWebdavConfig
    * root CDATA #REQUIRED
    * community CDATA #REQUIRED
    * &gt;
    * &lt;!--
    * The supported content type for WebDAV
    *
    * "name" - the name of the content type in Rhythmyx
    *
    * "contentfield" - the field name for the content, it cannot be empty
    *
    * "default" - "true" or "false". There must be only one element
    *              with the value "true".
    *              If it is "false", then the "Mimetypes" element must exist;
    *              If it is "true", the "Mimetypes" element is ignored.
    *
    *
    * Required properties:
    *
    * "getcontenttype" - maps to a field for the mime-type.
    *
    * "getcontentlength" - maps to a field for the content length
    *
    * "psx_content" - maps to a field for the content.
    *
    * --&gt;
    * &lt;!ELEMENT PSXWebdavContentType (Mimetypes?, Properties)&gt;
    * &lt;!ATTLIST PSXWebdavContentType
    * name CDATA #REQUIRED
    * contentfield CDATA #REQUIRED
    * default CDATA #REQUIRED
    * &gt;
    * &lt;!--
    * The supported mime types for a Rhythmyx content type
    * --&gt;
    * &lt;!ELEMENT MimeTypes (MimeType+)&gt;
    * &lt;!--
    * The mime type that the current NonItemExtractor will accept.
    *
    * name - the mime type name, e.g. image/gif.
    * --&gt;
    * &lt;!ELEMENT MimeType (#PCDATA)&gt;
    * &lt;!--
    * This collection of properties specifies all supported WebDAV
    * properties and its related fields in Rhythmyx.
    * --&gt;
    * &lt;!ELEMENT Properties (PSXWebdavProperty+)&gt;
    * &lt;!--
    * A property specifies a WebDAV property name and a RX field name.
    *
    * name - the name of the WebDAV property, must be unique within its container.
    * --&gt;
    * &lt;!ELEMENT PSXWebdavProperty (FieldName)&gt;
    * &lt;!ATTLIST Property
    * name CDATA #REQUIRED
    * &gt;
    * &lt;!--
    * A field name
    * --&gt;
    * &lt;!ELEMENT FieldName (#PCDATA)&gt;
    *</pre></p>
    * @param src xml source element, cannot be <code>null</code>.
    */
   public void fromXml(Element src) throws PSWebdavException,
         PSUnknownNodeTypeException
   {
      if (null == src)
         throw new IllegalArgumentException("Source element cannot be null.");
      String rootPath = src.getAttribute(IPSRxWebDavDTD.ATTR_ROOT);
      String community = src.getAttribute(IPSRxWebDavDTD.ATTR_COMMUNITY_NAME);
      String communityId = src.getAttribute(IPSRxWebDavDTD.ATTR_COMMUNITY_ID);
      String locale = src.getAttribute(IPSRxWebDavDTD.ATTR_LOCALE);
      String deleteAs = src.getAttribute(IPSRxWebDavDTD.ATTR_DELETEAS);
      String publicTokens = src.getAttribute(IPSRxWebDavDTD.ATTR_PUBLICFLAGS);
      String qeTokens = src.getAttribute(IPSRxWebDavDTD.ATTR_QEFLAGS);

      // Validate that the required attributes are set
      if (rootPath == null || rootPath.trim().length() == 0)
         handleValidationExceptions(new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED, new Object[] {
                     IPSRxWebDavDTD.ATTR_ROOT, getNodeName() }));

      if (community == null || community.trim().length() == 0)
         handleValidationExceptions(new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED, new Object[] {
                     IPSRxWebDavDTD.ATTR_COMMUNITY_NAME, getNodeName() }));

      if (communityId == null || communityId.trim().length() == 0)
         handleValidationExceptions(new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED, new Object[] {
                     IPSRxWebDavDTD.ATTR_COMMUNITY_ID, getNodeName() }));

      if (locale == null || locale.trim().length() == 0)
         handleValidationExceptions(new PSWebdavException(
               IPSWebdavErrors.XML_ATTRIBUTE_MUST_BE_SPECIFIED, new Object[] {
                     IPSRxWebDavDTD.ATTR_LOCALE, getNodeName() }));

      if (deleteAs != null
            && deleteAs.equalsIgnoreCase(IPSRxWebDavDTD.ATTR_VALUE_PURGE))
         m_isDeleteAsPurge = true;
      else
         m_isDeleteAsPurge = false;

      if (parseTokens(qeTokens))
         m_qeValidTokens = qeTokens.trim();
      else
         m_qeValidTokens = "i";

      if (parseTokens(publicTokens))
         m_publicValidTokens = publicTokens.trim();
      else
         m_publicValidTokens = "y";

      m_rootPath = rootPath;
      m_locale = locale;
      m_communityName = community;
      // The following condition is needed so the config validator
      // doesn't choke on the Number format exception
      if (communityId != null && communityId.trim().length() > 0)
         m_communityId = Integer.parseInt(communityId);

      // Get content types validating that only one default content type
      // exists.
      boolean gotDefault = false;
      m_contenttypes.clear();
      ArrayList typeNames = new ArrayList();
      Element contentTypeEl = PSXMLDomUtil.getFirstElementChild(src,
            IPSRxWebDavDTD.ELEM_CONTENTTYPE);
      while (contentTypeEl != null)
      {
         String nodeName = PSXMLDomUtil.getUnqualifiedNodeName(contentTypeEl);
         if (!nodeName.equalsIgnoreCase(IPSRxWebDavDTD.ELEM_CONTENTTYPE))
            break;

         PSWebdavContentType ct = new PSWebdavContentType(contentTypeEl,
               m_captureValidationExceptions, getValidationExceptionsList());
         if (typeNames.contains(ct.getName()))
         {
            handleValidationExceptions(new PSWebdavException(
                  IPSWebdavErrors.DUPLICATE_CONTENTTYPE_NAMES, ct.getName()));
         }
         typeNames.add(ct.getName());
         if (ct.isDefault())
         {
            if (gotDefault)
            {
               handleValidationExceptions(new PSWebdavException(
                     IPSWebdavErrors.CAN_ONLY_HAVE_ONE_DEFAULT_CONTENTTYPE));
            }
            gotDefault = true;
         }
         m_contenttypes.add(ct);

         contentTypeEl = PSXMLDomUtil.getNextElementSibling(contentTypeEl);
      }

      // get the exclude folder properties
      m_excludeFolderProperties.clear();
      Element properties = contentTypeEl;
      if (properties != null)
      {
         PSXMLDomUtil.checkNode(properties,
               IPSRxWebDavDTD.ELEM_EXCLUDE_FOLDER_PROPERTIES);
         Element nameElem = PSXMLDomUtil.getFirstElementChild(properties,
               IPSRxWebDavDTD.ELEM_PROPERTY_NAME);
         while (nameElem != null)
         {
            PSXMLDomUtil.checkNode(nameElem, IPSRxWebDavDTD.ELEM_PROPERTY_NAME);
            String name = PSXMLDomUtil.getElementData(nameElem);
            m_excludeFolderProperties.add(name);

            nameElem = PSXMLDomUtil.getNextElementSibling(contentTypeEl);
         }
      }
   }

   // Implement IPSWebdavComponent interface method
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   // Implement IPSWebdavComponent interface method
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSWebdavConfigDef))
      {
         return false;
      }
      else
      {
         PSWebdavConfigDef other = (PSWebdavConfigDef) obj;

         return m_rootPath.equals(other.m_rootPath)
               && m_communityName.equals(other.m_communityName)
               && m_contenttypes.equals(other.m_contenttypes)
               && m_locale.equals(other.m_locale)
               && m_communityId == other.m_communityId
               && m_isDeleteAsPurge == other.m_isDeleteAsPurge
               //NEW ABBIE
               && m_publicValidTokens.equals(other.m_publicValidTokens)
               && m_qeValidTokens.equals(other.m_qeValidTokens)
               && m_excludeFolderProperties
                     .equals(other.m_excludeFolderProperties);
      }
   }

   // Implement IPSWebdavComponent interface method
   public int hashCode()
   {
      return m_rootPath.hashCode() + m_communityName.hashCode()
            + m_contenttypes.hashCode() + m_communityId + m_locale.hashCode()
            + (m_isDeleteAsPurge ? 1 : 0)
            + m_excludeFolderProperties.hashCode();
   }

   /**
    * Parses the state flag configuration fields to ensure that they are comma
    * separated char fields.
    * 
    * @param toks
    *           String may be <code>null</code> or empty.
    * @return <code>true</code> if the tokens string is valid.
    */
   public boolean parseTokens(String toks)
   {
      if (toks == null || toks.trim().length() == 0)
         return false;
      char flags[] = toks.toLowerCase().toCharArray();
      if (flags[0] == ',')
         return false;
      return true;
   }

   /**
    * The virtual root path of Rhythmyx for all resources specified in
    * the request. Set in ctor, never <code>null</code> or empty after that.
    */
   private String m_rootPath;

   /**
    * The name of the community, which is used as a reference to the 
    * community id <code>m_communityId</code>.  Set in ctor, never 
    * <code>null</code> or empty after that.
    */
   private String m_communityName;

   /**
    * The id of the community, which is used when communicating with
    * the Rhythmyx server.  Initialized in ctor.
    */
   private int m_communityId;

   /**
    * The locale which is used for creating items or folders. 
    * Initialized by ctor, never <code>null</code> or empty after that.
    */
   private String m_locale;

   /**
    * Collection of content types for this configuration.
    * Never <code>null</code>, may be empty.
    */
   private Collection m_contenttypes = new ArrayList();

   /**
    * Xml node name for this class
    */
   private final static String XML_NODE_NAME = IPSRxWebDavDTD.ELEM_CONFIG;

   /**
    * see {@link #isDeleteAsPurge()} for description. Default 
    * to <code>false</code>.   
    */
   private boolean m_isDeleteAsPurge = false;

   /**
    * Value to be used for STATES.contentValid to indicate a published state.
    * Initialized in ctor.  May contain a comma-separated list of chars but
    * never <code>null</code> or emtpy.
    */
   private String m_publicValidTokens = "y";

   /**
    * Value to be used for STATES.contentValid to indicate a quick-edit state.
    * Initialized in ctor.  May contain a comma-separated list of chars but
    * never <code>null</code> or emtpy.
    */
   private String m_qeValidTokens = "i";

   /**
    * A set of property names that will not be inherited from the parent folder
    * when creating a folder. It is a set of zero or more <code>String</code>
    * objects, never <code>null</code>, but may be empty.
    */
   private Set m_excludeFolderProperties = new HashSet();
}

