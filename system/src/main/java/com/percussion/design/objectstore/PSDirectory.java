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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A component that holds one directory definition used to catalog information
 * from directory servers.
 */
public class PSDirectory extends PSComponent
{
   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSDirectory(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Contructs a new object for the supplied parameters.
    *
    * @param name the directory name, not <code>null</code> or empty.
    * @param catalogOption the cataloging option, not <code>null</code> or
    *    empty, must be one of <code>CATALOG_ENUM</code>.
    * @param factory the fully qualified factory class name used to construct
    *    a JNDI context, not <code>null</code> or empty.
    * @param authenticationRef a reference to the authentication object used
    *    to authenticate against the directory server, not <code>null</code>
    *    or empty.
    * @param providerUrl the url used to connect to the directory, not
    *    <code>null</code> or empty.
    * @param attributes a list of attributes that will be returned with
    *    directory requests, may be <code>null</code> but not empty.
    */
   public PSDirectory(String name, String catalogOption,
      String factory, String authenticationRef, String providerUrl,
      PSCollection attributes)
   {
      setName(name);
      setCatalogOption(catalogOption);
      setFactory(factory);
      setAuthenticationRef(authenticationRef);
      setProviderUrl(providerUrl);
      setAttributes(attributes);
   }

   /**
    * @return the directory name, never <code>null</code> or empty. This name
    *    may be used to reference this directory from other contexts.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set a new directory name.
    *
    * @param name the new name for this directory, not <code>null</code> or
    *    empty.
    */
   public void setName(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");

      m_name = name;
   }

   /**
    * @return <code>true</code> if the selected catalog option is
    *    <code>CATALOG_SHALLOW</code>, <code>false</code> otherwise.
    */
   public boolean isShallowCatalogOption()
   {
      return m_catalogOption.equals(CATALOG_SHALLOW);
   }

   /**
    * @return <code>true</code> if the selected catalog option is
    *    <code>CATALOG_DEEP</code>, <code>false</code> otherwise.
    */
   public boolean isDeepCatalogOption()
   {
      return m_catalogOption.equals(CATALOG_DEEP);
   }

   /**
    * Set a new catalog option. Catalog options are case insensitive and
    * stored in lower case.
    *
    * @param catalogOption the new catalog option, not <code>null</code> or
    *    empty. Must be one of <code>CATALOG_ENUM</code>.
    */
   public void setCatalogOption(String catalogOption)
   {
      if (catalogOption == null)
         throw new IllegalArgumentException("catalogOption cannot be null");

      catalogOption = catalogOption.trim().toLowerCase();
      if (catalogOption.length() == 0)
         throw new IllegalArgumentException("catalogOption cannot be empty");

      boolean valid = false;
      for (int i=0; i<CATALOG_ENUM.length; i++)
      {
         if (CATALOG_ENUM[i].equals(catalogOption))
         {
            valid = true;
            break;
         }
      }
      if (!valid)
         throw new IllegalArgumentException(
            "catalogOption must be one of CATALOG_ENUM");

      m_catalogOption = catalogOption;
   }
   
   /**
    * @return <code>true</code> if debug output is enabled, <code>false</code>
    *    otherwise.
    */
   public boolean isDebug()
   {
      return m_debug;
   }
   
   /**
    * Enable or disable directory service debug output to the system output 
    * stream.
    * 
    * @param enable string that specifies debug output as enabled if it is set to
    *    'yes' (case insensitive), may be <code>null</code> or empty in which
    *    case debug output is disabled.
    */
   public void setDebug(String enable)
   {
      if (enable != null)
      {
         enable = enable.trim().toLowerCase();
         m_debug = enable.equals(DEBUG_YES);
      }
      else
         m_debug = false;
   }

   /**
    * @return the fully qualified directory service context factory class name,
    *    never <code>null</code> or empty.
    */
   public String getFactory()
   {
      return m_factory;
   }

   /**
    * Set a new directory service context factory class name.
    *
    * @param factory the new context factory class name, not <code>null</code>
    *    or empty.
    */
   public void setFactory(String factory)
   {
      if (factory == null)
         throw new IllegalArgumentException("factory cannot be null");

      factory = factory.trim();
      if (factory.length() == 0)
         throw new IllegalArgumentException("factory cannot be empty");

      m_factory = factory;
   }

   /**
    * @return a reference to an authentication spec. used to connect to the
    *    directory server, never <code>null</code>. The caller takes
    *    ownershop of the returned object. He cannot change this with the
    *    returned object.
    */
   public PSReference getAuthenticationRef()
   {
      return new PSReference(m_authenticationRef);
   }

   /**
    * Set a new authentication reference.
    *
    * @param authenticationRef the new authentication reference, not
    *    <code>null</code>, must be a reference of type
    *    <code>PSAuthentication</code>.
    */
   public void setAuthenticationRef(PSReference authenticationRef)
   {
      if (authenticationRef == null)
         throw new IllegalArgumentException("authenticationRef cannot be null");

      if (!authenticationRef.getType().equals(PSAuthentication.class.getName()))
         throw new IllegalArgumentException("must be a PSAuthentication reference");

      m_authenticationRef = new PSReference(authenticationRef);
   }

   /**
    * Set a new authentication reference.
    *
    * @param authenticationRef the name of the new authentication reference, not
    *    <code>null</code> or empty.
    */
   public void setAuthenticationRef(String authenticationRef)
   {
      if (authenticationRef == null)
         throw new IllegalArgumentException("authenticationRef cannot be null");

      authenticationRef = authenticationRef.trim();
      if (authenticationRef.length() == 0)
         throw new IllegalArgumentException("authenticationRef cannot be empty");

      m_authenticationRef = new PSReference(authenticationRef,
         PSAuthentication.class.getName());
   }

   /**
    * Get the directory service provider url.
    * 
    * @return the directory service provider url as <code>String</code>, never
    *    <code>null</code> or empty.  This url is not encoded to handle spaces
    *    or other special characters.
    */
   public String getProviderUrl()
   {
      return m_providerUrl;
   }

   /**
    * Set a new directory service provider url.
    *
    * @param providerUrl the new provider url, never <code>null</code> or
    *    empty.
    */
   public void setProviderUrl(String providerUrl)
   {
      if (providerUrl == null)
         throw new IllegalArgumentException("providerUrl cannot be null");

      providerUrl = providerUrl.trim();
      if (providerUrl.length() == 0)
         throw new IllegalArgumentException("providerUrl cannot be empty");

      m_providerUrl = providerUrl;
   }
   
   /**
    * Attempt to determine the base context from the provider url.
    *
    * @return the base context if there is one specified by the provider url,
    *    or an empty string if there is none specified, or <code>null</code>
    *    if the provider url is <code>null</code> or malformed.
    */
   public String getBaseContext()
   {
      String baseCtx = null;

      try
      {
         URL url = new URL(getProviderUrl());
         baseCtx = url.getFile();
         if (baseCtx.startsWith("/"))
         {
            if (baseCtx.length() > 1)
               baseCtx = baseCtx.substring(1);
            else
               baseCtx = "";
         }
      }
      catch (MalformedURLException e)
      {
         // oh well, we tried
      }

      return baseCtx;
   }
   
   /**
    * @return a collection of attribute names that will be returned with
    *    requests to this directory service, may be <code>null</code> but not
    *    empty. The caller takes ownership of the returned collection. This
    *    object cannot be modified through the returned collection.
    */
   public PSCollection getAttributes()
   {
      if (m_attributes == null)
         return null;

      return new PSCollection(m_attributes.iterator());
   }

   /**
    * Set a new attribute set to be returned with this directory service.
    *
    * @param attributes the new attribute collection. Must be a collection of
    *    <code>String</code> objects. May be <code>null</code> but not empty.
    *    Set this to <code>null</code> to return all known attributes.
    */
   public void setAttributes(PSCollection attributes)
   {
      if (attributes == null)
         m_attributes = null;
      else
      {
         if (attributes.isEmpty())
            throw new IllegalArgumentException("attributes cannot be empty");

         if (!attributes.getMemberClassName().equals(String.class.getName()))
            throw new IllegalArgumentException(
               "attributes mus be a collection of String objects");

         m_attributes = new PSCollection(attributes.iterator());
      }
   }

   /**
    * Gets the list of group providers this security provider uses.
    *
    * @return The an Iterator over zero or more group provider names as Strings,
    * never <code>null</code>, may be emtpy.
    */
   public Iterator<String> getGroupProviderNames()
   {
      return m_groupProviderNames.iterator();
   }

   /**
    * Sets the list of group providers this security provider will use.  This
    * will replace the existing list of provider names.  See
    * {@link #getGroupProviderNames()} for more info.
    *
    * @param providers An iterator over zero or more group Provider names as
    * Strings.  May not be <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if providers is invalid.
    */
   public void setGroupProviderNames(Iterator<String> providers)
   {
      if (providers == null)
         throw new IllegalArgumentException("providers may not be null");

      m_groupProviderNames.clear();
      while (providers.hasNext())
      {
         String provider = providers.next();
         if (provider.trim().length() == 0)
            throw new IllegalArgumentException(
               "providers may not contain empty Strings");

         m_groupProviderNames.add(provider);
      }
   }
   
   /** @see IPSComponent */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      setName(getRequiredElement(tree, XML_ATTR_NAME, false));
      setCatalogOption(getEnumeratedAttribute(tree, XML_ATTR_CATALOG,
         CATALOG_ENUM));
      setDebug(sourceNode.getAttribute(XML_ATTR_DEBUG));

      setFactory(getRequiredElement(tree, XML_ELEM_FACTORY, false));

      Node current = tree.getCurrent();

      Element authentication = tree.getNextElement(XML_ELEM_AUTHENTICATION);
      if (authentication == null)
      {
         Object[] args = { XML_ELEM_AUTHENTICATION, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      Element reference = tree.getNextElement(PSReference.XML_NODE_NAME);
      if (reference == null)
      {
         Object[] args = { PSReference.XML_NODE_NAME, null };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_authenticationRef =
         new PSReference(reference, parentDoc, parentComponents);

      tree.setCurrent(current);

      setProviderUrl(getRequiredElement(tree, XML_ELEM_PROVIDER_URL, false));

      setAttributes(null);
      Element attributes = tree.getNextElement(XML_ELEM_ATTRIBUTES);
      if (attributes != null)
      {
         PSCollection refs = new PSCollection(String.class);
         Element attribute = tree.getNextElement(XML_ELEM_ATTRIBUTE);
         if (attribute != null)
         {
            while (attribute != null)
            {
               refs.add(attribute.getAttribute(XML_ATTR_NAME));
               attribute = tree.getNextElement(XML_ELEM_ATTRIBUTE);
            }
   
            setAttributes(refs);
         }
      }
      
      // load the groups, if any
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;      
      m_groupProviderNames.clear();
      tree.setCurrent(current);
      Element groupProviders = tree.getNextElement(GROUP_PROVIDERS_ELEMENT,
         firstFlags);
      if (groupProviders != null)
      {
         Element groupName = tree.getNextElement(GROUP_NAME_ELEMENT,
            firstFlags);

         while (groupName != null)
         {
            String groupProvider = tree.getElementData(groupName);
            if (groupProvider == null || groupProvider.trim().length() == 0)
            {
               Object[] args = {  GROUP_PROVIDERS_ELEMENT, GROUP_NAME_ELEMENT,
                  "null" };
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            m_groupProviderNames.add(groupProvider);
            groupName = tree.getNextElement(GROUP_NAME_ELEMENT, nextFlags);
         }
      }
   }

   /** @see IPSComponent */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, getName());
      root.setAttribute(XML_ATTR_CATALOG, m_catalogOption);
      if (isDebug())
         root.setAttribute(XML_ATTR_DEBUG, DEBUG_YES);

      PSXmlDocumentBuilder.addElement(doc, root, XML_ELEM_FACTORY,
         getFactory());

      Element authentication = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         XML_ELEM_AUTHENTICATION);
      authentication.appendChild(m_authenticationRef.toXml(doc));

      PSXmlDocumentBuilder.addElement(doc, root, XML_ELEM_PROVIDER_URL,
         getProviderUrl());

      if (m_attributes != null)
      {
         Element attributes = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            XML_ELEM_ATTRIBUTES);

         Iterator attrs = getAttributes().iterator();
         while (attrs.hasNext())
         {
            Element attr= PSXmlDocumentBuilder.addEmptyElement(doc, attributes,
               XML_ELEM_ATTRIBUTE);
            attr.setAttribute(XML_ATTR_NAME, (String) attrs.next());
         }
      }
      
      // store the group names, if any
      if (!m_groupProviderNames.isEmpty())
      {
         Element groupProviders = PSXmlDocumentBuilder.addEmptyElement(doc,
            root, GROUP_PROVIDERS_ELEMENT);
         Iterator<String> gps = m_groupProviderNames.iterator();
         while (gps.hasNext())
         {
            PSXmlDocumentBuilder.addElement(doc, groupProviders,
               GROUP_NAME_ELEMENT, gps.next());
         }
      }
      

      return root;
   }

   /** @see IPSComponent */
   @Override
   public Object clone()
   {
      return super.clone();
   }

   /** @see PSComponent */
   @Override
   public void copyFrom(PSComponent c)
   {
      super.copyFrom(c);

      if (!(c instanceof PSDirectory))
         throw new IllegalArgumentException("c must be a PSDirectory object");

      PSDirectory o = (PSDirectory) c;

      setName(o.getName());
      setCatalogOption(o.m_catalogOption);
      setFactory(o.getFactory());
      setAuthenticationRef(o.getAuthenticationRef());
      setProviderUrl(o.getProviderUrl());
      setAttributes(o.getAttributes());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDirectory)) return false;
      if (!super.equals(o)) return false;
      PSDirectory that = (PSDirectory) o;
      return m_debug == that.m_debug &&
              Objects.equals(m_name, that.m_name) &&
              Objects.equals(m_catalogOption, that.m_catalogOption) &&
              Objects.equals(m_factory, that.m_factory) &&
              Objects.equals(m_authenticationRef, that.m_authenticationRef) &&
              Objects.equals(m_providerUrl, that.m_providerUrl) &&
              Objects.equals(m_attributes, that.m_attributes) &&
              Objects.equals(m_groupProviderNames, that.m_groupProviderNames);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_catalogOption, m_factory, m_authenticationRef, m_providerUrl, m_attributes, m_groupProviderNames, m_debug);
   }

   /** The XML node name */
   public static final String XML_NODE_NAME = "PSXDirectory";

   /**
    * Constant to specify the cataloging option as shallow, meaning only the
    * addressed object is cataloged.
    */
   public static final String CATALOG_SHALLOW = "shallow";

   /**
    * Constant to specify the cataloging option as deep, meaning that the
    * addressed object and all its children are cataloged.
    */
   public static final String CATALOG_DEEP = "deep";

   /**
    * An array with all validd cataloging options.
    */
   public static final String[] CATALOG_ENUM =
   {
      CATALOG_SHALLOW,
      CATALOG_DEEP
   };
   
   /**
    * The LDAP context factory class.
    */
   public static final String FACTORY_LDAP = "com.sun.jndi.ldap.LdapCtxFactory";
   
   /**
    * The NIS context factory.
    */
   public static final String FACTORY_NIS = "com.sun.jndi.nis.NISCtxFactory";
   
   /**
    * An enumeration with all known context factory classes.
    */
   public static final String[] FACTORY_ENUM =
   {
      FACTORY_LDAP,
      FACTORY_NIS
   };
   
   /**
    * The string constant representing directory service debug output as 
    * enabled.
    */
   public static final String DEBUG_YES = "yes";

   /**
    * Holds the directory name. This name must be unique across all defined
    * directories because its used to reference it from other contexts.
    * Initialized during construction, never <code>null</code> or empty after
    * that.
    */
   private String m_name;

   /**
    * Holds the selected cataloging option. Initialized during construction,
    * never <code>null</code> or empty after that. Must be one of
    * <code>CATALOG_ENUM</code>.
    */
   private String m_catalogOption = CATALOG_SHALLOW;

   /**
    * Holds the fully qualified factory class name used to create the
    * context. Initialized during cconstruction, never <code>null</code> or
    * empty after that.
    */
   private String m_factory;

   /**
    * Holds a reference to the authentication spec. needed to make catalog
    * requests to the directory specified with this class. Initialized
    * during construction, never <code>null</code> after that.
    */
   private PSReference m_authenticationRef;

   /**
    * Holds the complete provider url used to catalog information from this
    * directory service. Initialized during cconstrucction, never
    * <code>null</code> or empty after that.
    */
   private String m_providerUrl;

   /**
    * A <code>String</code> collection of attribute names. This specifies
    * what attributes will be returned with requests to this directory service.
    * May be <code>null</code> in which case all known attributes will be
    * returned but not empty.
    * We use a <code>PSCollection</code> to enforce that all entries are
    * <code>String</code> objects.
    */
   private PSCollection m_attributes;
   

   /**
    * List of names of the group providers instances used by this provider,
    * stored as Strings.  Never <code>null</code>, may be empty.
    */
   private List<String> m_groupProviderNames = new ArrayList<>();
   
   /**
    * A flag that indicates whether or not to output directory service debug 
    * information to the system output stream. Initialized in 
    * {@link #fromXml(Element, IPSDocument, ArrayList)}, never changed after
    * that.
    */
   private boolean m_debug = false;

   // XML element and attribute constants.
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_CATALOG = "catalog";
   private static final String XML_ATTR_DEBUG = "debug";
   private static final String XML_ELEM_FACTORY = "Factory";
   private static final String XML_ELEM_AUTHENTICATION = "Authentication";
   private static final String XML_ELEM_PROVIDER_URL = "ProviderUrl";
   private static final String XML_ELEM_ATTRIBUTES = "Attributes";
   private static final String XML_ELEM_ATTRIBUTE = "Attribute";
   private static final String GROUP_PROVIDERS_ELEMENT = "groupProviders";
   private static final String GROUP_NAME_ELEMENT = "groupName";   
}
