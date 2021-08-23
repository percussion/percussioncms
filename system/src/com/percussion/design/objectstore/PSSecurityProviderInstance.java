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

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

/**
 * The PSSecurityProviderInstance class is used to define a connection
 * to a security provider for authentication of users.
 *
 * @see   PSServerConfiguration
 * @see   PSServerConfiguration#getSecurityProviderInstances
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSSecurityProviderInstance extends PSComponent
{
   private static final Logger logger = LogManager.getLogger(PSSecurityProviderInstance.class);
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object from
    *
    * @param parentDoc the Java object which is the parent of this object
    *
    * @param parentComponents the parent objects of this object
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not of the
    * appropriate type
    */
   public PSSecurityProviderInstance(Element sourceNode,
      IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   protected PSSecurityProviderInstance()
   {
      super();
   }


   /**
    * Constructor to create instance from name and type.
    * @param   name     the unique security provider instance name
    * @param   type     the appropriate SP_TYPE_xxx flag
    *
    * @throws   PSIllegalArgumentException  if name exceeds the specified size
    * limit or if type is invalid.
    */
   public PSSecurityProviderInstance(String name, int type)
      throws PSIllegalArgumentException
   {
      super();
      setName(name);
      setType(type);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSecurityProviderInstance)) return false;
      if (!super.equals(o)) return false;
      PSSecurityProviderInstance that = (PSSecurityProviderInstance) o;
      return m_providerType == that.m_providerType &&
              Objects.equals(m_instanceName, that.m_instanceName) &&
              Objects.equals(m_instanceProps, that.m_instanceProps) &&
              Objects.equals(m_directoryProvider, that.m_directoryProvider);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_providerType, m_instanceName, m_instanceProps, m_directoryProvider);
   }

   /**
    * Get the type of security provider associated with this instance.
    *
    * @return   the appropriate SP_TYPE_xxx flag
    */
   public int getType()
   {
      return m_providerType;
   }

   /**
    * Get the type of security provider associated with this instance.
    *
    * @param   type     the appropriate SP_TYPE_xxx flag
    *
    * @throws   PSIllegalArgumentException if type is invalid
    */
   public void setType(int type)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateType(type);
      if (ex != null)
         throw ex;

      m_providerType = type;
   }

   private static PSIllegalArgumentException validateType(int type)
   {
      if ( !PSSecurityProvider.isSupportedType(type))
      {
         return new PSIllegalArgumentException(
         IPSObjectStoreErrors.SPINST_TYPE_INVALID, String.valueOf(type));
      }
      return null;
   }

   /**
    * Get the provider name.
    */
   public String getProvider()
   {
      int type = m_providerType;
      String provider = "";

      switch(type)
      {
         case PSSecurityProvider.SP_TYPE_WEB_SERVER:
            provider = com.percussion.security.PSWebServerProvider.SP_NAME;
            break;
         case PSSecurityProvider.SP_TYPE_ODBC:
            provider = com.percussion.security.PSOdbcProvider.SP_NAME;
            break;
         case PSSecurityProvider.SP_TYPE_BETABLE:
            provider = com.percussion.security.PSBackEndTableProvider.SP_NAME;
            break;
         case PSSecurityProvider.SP_TYPE_DIRCONN:
            provider = com.percussion.security.PSDirectoryConnProvider.SP_NAME;
            break;
      }
      return provider;
   }

   /**
    * Get the unique name associated with this security provider instance.
    *
    * @return   the security provider instance name
    */
   public String getName()
   {
      return m_instanceName;
   }

   /**
    * Set the unique name associated with this security provider instance.
    * This is limited to 50 characters.
    *
    * @param   name     the unique security provider instance name
    *
    * @throws   PSIllegalArgumentException  if name exceeds the specified size
    * limit
    */
   public void setName(java.lang.String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_instanceName = name;
   }

   private static PSIllegalArgumentException validateName(String name)
   {
      if ((null != name) && (name.length() > MAX_NAME_LEN))
      {
         Object[] args = { new Integer(MAX_NAME_LEN),
            new Integer(name.length()) };
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.SPINST_NAME_TOO_BIG, args);
      }
      return null;
   }

   /**
    * Get the configuration properties associated with this
    * security provider instance.
    *
    * @return   the security provider instance configuration properties (may be
    * <code>null</code>)
    */
   public Properties getProperties()
   {
      return m_instanceProps;
   }

   /**
    * Set the configuration properties associated with this
    * security provider instance.
    *
    * @param props the security provider instance configuration properties
    */
   public void setProperties(Properties props)
      throws PSIllegalArgumentException
   {
      m_instanceProps = props;
   }

   public static String prepareCredentials(String uid, String pw)
   {
      try {
         return PSEncryptor.encryptString(pw);
      } catch (PSEncryptionException e) {
         logger.error("Error encrypting password: {}",e.getMessage());
         logger.debug(e);
         return "";
      }

   }

   // **************  IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXSecurityProviderInstance
    * XML element node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *  &lt;!--
    *    PSXSecurityProviderInstance defines a security provider used
    *    to authenticate users.
    *  --&gt;
    *  &lt;!ELEMENT PSXSecurityProviderInstance  (  name, Properties?,
    *    groupProviders?)&gt;
    *
    *   &lt;!--
    *  Attributes associated with the connection:
    *
    *  id - the internal identifier for this object.
    *
    * type - the security provider type this instance uses
    *   --&gt;
    *   &lt;!ATTLIST
    *    id       ID          #REQUIRED
    *    type     CDATA       #REQUIRED
    *   &gt;
    *
    *  &lt;!--
    *    the name of the instance. This must be unique across
    *      PSXSecurityProviderInstance objects on the given server.
    *      The instance name is limited to 50 characters.
    *  --&gt;
    *  &lt;!ELEMENT name       (#PCDATA)&gt;
    *
    *  &lt;!--
    *    the configuration information for the OS security provider
    *  --&gt;
    *  &lt;!ELEMENT Properties (ANY)*&gt;
    * </code></pre>
    *
    * @return   the newly created PSXBackEndConnection XML element node
    */
   public Element toXml(Document   doc)
   {
      Element root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      String type = PSSecurityProvider.getSecurityProviderTypeString(
         m_providerType );
      if (type != null)
         root.setAttribute("type", type);

      // store the instance name
      PSXmlDocumentBuilder.addElement(
         doc, root, "name", m_instanceName);

      // store the appropriate config object
      Element configNode = PSXmlDocumentBuilder.addEmptyElement(
         doc, root, "Properties");

      if ( (configNode != null) && (m_instanceProps != null) )
      {
         Enumeration keys = m_instanceProps.keys();
         Enumeration values = m_instanceProps.elements();
         while (keys.hasMoreElements())
         {
            PSXmlDocumentBuilder.addElement(
               doc, configNode, (String)keys.nextElement(),
               (String)values.nextElement());
         }
      }

      // store directory provider
      if (m_directoryProvider != null)
      {
         Element directoryProvider = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, DIRECTORY_PROVIDER_ELEMENT);
         directoryProvider.appendChild(m_directoryProvider.toXml(doc));
      }


      return root;
   }

   /**
    * This method is called to populate a PSBackEndConnection Java object
    * from a PSXBackEndConnection XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    * of type PSXBackEndConnection
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      }
      catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      sTemp = tree.getElementData("type");
      if ((sTemp == null) || (sTemp.length() == 0))
      {
         Object[] args = { ms_NodeType, "type", "" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      int providerType =
         PSSecurityProvider.getSecurityProviderTypeFromXmlFlag( sTemp );
      if ( providerType != 0 )
         m_providerType = providerType;
      else
      {
         Object[] args = { ms_NodeType, "type", sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // get the instance name
      sTemp = tree.getElementData("name");
      try
      {
         setName(sTemp);
      }
      catch (PSIllegalArgumentException e)
      {
         throw new PSUnknownNodeTypeException(ms_NodeType, "name", e);
      }

      // store the appropriate config object
      m_instanceProps = new Properties();

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      
      Element properties = tree.getNextElement("Properties", firstFlags);
      if (properties != null && properties.hasChildNodes())
      {
         while (tree.getNextElement(true) != null)
         {
            Element node = (Element) tree.getCurrent();
            String nodeName = node.getNodeName();
            m_instanceProps.put(nodeName, tree.getElementData(nodeName, false));
         }
      }

      // load directory provider
      tree.setCurrent(sourceNode);
      Element directoryProvider = tree.getNextElement(
         DIRECTORY_PROVIDER_ELEMENT, firstFlags);
      if (directoryProvider != null)
      {
         Element provider = tree.getNextElement(
            PSProvider.XML_NODE_NAME, firstFlags);

         m_directoryProvider = new PSProvider(provider, parentDoc,
            parentComponents);
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

      PSException ex = validateType(m_providerType);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      ex = validateName(m_instanceName);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());
   }

   /**
    * Get the directory catalog provider.
    * 
    * @return the directory catalog provider or <code>null</code> if this
    *    security provider does not allow directory catalog requests.
    */
   public PSProvider getDirectoryProvider()
   {
      return m_directoryProvider;
   }

   /**
    * Set a new directory catalog provider.
    *
    * @param provider the new directory provider to use, provide
    *    <code>null</code> if this security provider does not allow directory
    *    catalog requests.
    */
   public void setDirectoryProvider(PSProvider provider)
   {
      m_directoryProvider = provider;
   }

   /**
    * The security provider type, initialized while constructed. Must be
    * a supported type validated through
    */
   protected int m_providerType = -1;

   /**
    * The seccurity provider instance name, initialized while constructed, may
    * be <code>null</code> or empty.
    */
   protected String m_instanceName = null;

   /**
    * The configuration properties associated with this security provider.
    * Initialized in fromXml(Element, IPSDocument, ArrayList) or
    * through calls to setProperties(Properties), may be
    * <code>null</code> or empty.
    */
   protected Properties m_instanceProps = null;

   /**
    * The maximum lenght for security provider instance names.
    */
   public static final int MAX_NAME_LEN = 50;

   // package access on this so they may reference each other in fromXml
   public static final String ms_NodeType = "PSXSecurityProviderInstance";

   /**
    * The directory provider to be used with this security provider instance,
    * may be <code>null</code>.
    */
   protected PSProvider m_directoryProvider = null;

   // Xml element and attribute names
   protected static final String DIRECTORY_PROVIDER_ELEMENT = "DirectoryProvider";
   protected static final String ROLE_PROVIDER_ELEMENT = "RoleProvider";
}


