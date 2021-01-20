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
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class PSLegacySecurityProviderInstance extends PSSecurityProviderInstance
{
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
   public PSLegacySecurityProviderInstance(Element sourceNode,
      IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSLegacySecurityProviderInstance()
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
   public PSLegacySecurityProviderInstance(String name, int type)
      throws PSIllegalArgumentException
   {
      super();
      setName(name);
      setType(type);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSLegacySecurityProviderInstance)) return false;
      if (!super.equals(o)) return false;
      PSLegacySecurityProviderInstance that = (PSLegacySecurityProviderInstance) o;
      return Objects.equals(m_groupProviderNames, that.m_groupProviderNames) &&
              Objects.equals(m_roleProvider, that.m_roleProvider);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_groupProviderNames, m_roleProvider);
   }

// **************  IPSComponent Interface Implementation **************

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

      // load the groups, if any
      m_groupProviderNames.clear();
      tree.setCurrent(sourceNode);
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
    * Gets the list of group providers this security provider uses.
    *
    * @return The an Iterator over zero or more group provider names as Strings,
    * never <code>null</code>, may be emtpy.
    */
   public Iterator getGroupProviderNames()
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
   public void setGroupProviderNames(Iterator providers)
   {
      if (providers == null)
         throw new IllegalArgumentException("providers may not be null");

      m_groupProviderNames.clear();
      while (providers.hasNext())
      {
         Object obj = providers.next();
         if (!(obj instanceof String))
            throw new IllegalArgumentException(
               "providers may only contain non-null Strings");

         String provider = (String)obj;
         if (provider.trim().length() == 0)
            throw new IllegalArgumentException(
               "providers may not contain empty Strings");

         m_groupProviderNames.add(provider);
      }
   }
   
   
   /**
    * Get the role catalog provider.
    * 
    * @return the role catalog provider or <code>null</code> if this
    *    security provider does not allow role catalog requests.
    */
   public PSProvider getRoleProvider()
   {
      return m_roleProvider;
   }

   /**
    * Set a new role catalog provider.
    *
    * @param provider the new role provider to use, provide
    *    <code>null</code> if this security provider does not allow role
    *    catalog requests.
    */
   public void setRoleProvider(PSProvider provider)
   {
      m_roleProvider = provider;
   }

   /**
    * List of names of the group providers instances used by this provider,
    * stored as Strings.  Never <code>null</code>, may be empty.
    */
   private List m_groupProviderNames = new ArrayList();
   
   /**
    * The role provider to be used with this security provider instance,
    * may be <code>null</code>.
    */
   private PSProvider m_roleProvider = null;

   // Xml element and attribute names
   private static final String GROUP_PROVIDERS_ELEMENT = "groupProviders";
   private static final String GROUP_NAME_ELEMENT = "groupName";
   
}


